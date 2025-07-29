package com.example.spotify;

import org.springframework.stereotype.Component;
import com.example.spotify.model.SpotifyPlaybackEntry;
import com.example.spotify.util.SpotifyParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;


@Component
public class DataService {

    private String currentSessionFolder;
    private CombinedStatsCollector cachedStats = null;
    private String lastParsedFolder = null;

    public void loadSessionFolder(String folderPath) {
        this.currentSessionFolder = folderPath;
    }

    public String getCurrentSessionFolder() {
        return this.currentSessionFolder;
    }

    public static void processSessionFolder(String folderPath, List<Consumer<SpotifyPlaybackEntry>> entryConsumers) {
        if (folderPath == null) {
            throw new IllegalArgumentException("folderPath is null");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();

        try {
            Files.walk(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> parseFile(path, factory, mapper, entryConsumers));
        } catch (IOException e) {
            System.err.println("Error walking folder: " + e.getMessage());
        }
    }

    private static void parseFile(Path path, JsonFactory factory, ObjectMapper mapper, List<Consumer<SpotifyPlaybackEntry>> entryConsumers) {
        String fileName = path.getFileName().toString().toLowerCase();
        if (!fileName.endsWith(".json") || fileName.startsWith("._")) return;

        System.out.println("Parsing: " + fileName);

        try (InputStream in = Files.newInputStream(path);
             JsonParser parser = factory.createParser(in)) {

            if (parser.nextToken() != JsonToken.START_ARRAY) {
                System.err.println("Expected JSON array in file: " + fileName);
                return;
            }

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                ObjectNode node = mapper.readTree(parser);
                SpotifyPlaybackEntry entry = SpotifyParser.fromJson(new org.json.JSONObject(node.toString()));
                for (Consumer<SpotifyPlaybackEntry> consumer : entryConsumers) {
                    consumer.accept(entry);
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to parse " + fileName + ": " + e.getMessage());
        }
    }

    private static Map<String, Integer> convertMillisToTimeMap(int ms) {
        int minutes = ms / 60000;
        int hours = ms / (1000 * 60 * 60);
        int days = ms / (1000 * 60 * 60 * 24);
//
        Map<String, Integer> timeMap = new HashMap<>();
        timeMap.put("minutes", minutes);
        timeMap.put("hours", hours);
        timeMap.put("days", days);
        return timeMap;
    }

    private static Map<String, String> cleanFirstTrackEver(SpotifyPlaybackEntry firstEntry) {
        Map<String, String> firstTrackEver = new HashMap<>();
        if (firstEntry == null) {
            firstTrackEver.put("track", "N/A");
            firstTrackEver.put("timeStamp", "N/A");
            firstTrackEver.put("artist", "N/A");
            return firstTrackEver;
        }
        ZonedDateTime zdt = ZonedDateTime.parse(firstEntry.getTimestamp());
        String formatted = zdt.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a"));

        firstTrackEver.put("track", firstEntry.getTrackName());
        firstTrackEver.put("artist", firstEntry.getArtistName());
        firstTrackEver.put("timeStamp", formatted);
        return firstTrackEver;
    }

    private static String cleanArtistRevenue(float totalStreamsFloat) {
        DecimalFormat df = new DecimalFormat("#.00");
        double royalties = totalStreamsFloat * 0.004;
        return df.format(royalties);
    }

    public static class CombinedStatsCollector {
        public TopStatsCollector topStats;
        public GeneralStatsCollector generalStats;
        DailyStatsCollector dailyStats;
        YearlyStatsCollector yearlyStats;

        public CombinedStatsCollector(TopStatsCollector topStats, GeneralStatsCollector generalStats, DailyStatsCollector dailyStats, YearlyStatsCollector yearlyStats) {
            this.topStats = topStats;
            this.generalStats = generalStats;
            this.dailyStats = dailyStats;
            this.yearlyStats = yearlyStats;
        }

        public TopStatsCollector getTopStats() { return topStats; }
        public GeneralStatsCollector getGeneralStats() { return generalStats; }
        public DailyStatsCollector getDailyStats() { return dailyStats; }
        public YearlyStatsCollector getYearlyStats() { return yearlyStats; }

    }

    public synchronized void generateStatsIfNeeded(String folderPath) {
        if (cachedStats == null || !folderPath.equals(lastParsedFolder)) {
            System.out.println("Parsing new stats for: " + folderPath);
            cachedStats = computeStats(folderPath);
            lastParsedFolder = folderPath;
        } else {
            System.out.println("Using cached stats for: " + folderPath);
        }
    }

    public CombinedStatsCollector getStats() {
        return cachedStats;
    }

    public CombinedStatsCollector computeStats(String folderPath) {
        TopStatsCollector topStats = new TopStatsCollector();
        GeneralStatsCollector generalStats = new GeneralStatsCollector();
        DailyStatsCollector dailyStats = new DailyStatsCollector();
        YearlyStatsCollector yearlyStats = new YearlyStatsCollector();

        processSessionFolder(folderPath, List.of(
                topStats::processEntry,
                generalStats::processEntry,
                dailyStats::processEntry,
                yearlyStats::processEntry
        ));
        generalStats.finalizeStats();
        dailyStats.finalizeStats();

        return new CombinedStatsCollector(topStats, generalStats, dailyStats, yearlyStats);
    }

    public static class TopStatsCollector {
        Map<String, TrackStats> trackStatsMap = new LinkedHashMap<>();
        Map<String, ArtistStats> artistStatsMap = new LinkedHashMap<>();
        Map<String, AlbumStats> albumStatsMap = new LinkedHashMap<>();
        Map<String, Integer> podcastStatsMap = new LinkedHashMap<>();


        public void processEntry(SpotifyPlaybackEntry entry) {
            String track = entry.getTrackName();
            String artist = entry.getArtistName();
            String album = entry.getAlbumName();
            String playedAt = entry.getTimestamp();
            int ms = entry.getMsPlayed();
            String podcast = entry.getPodcastName();


            if (track != null || artist != null) {

                // TrackStats
                String trackKey = track + " - " + artist;
                trackStatsMap.putIfAbsent(trackKey, new TrackStats(track, artist, playedAt, entry));
                TrackStats trackStats = trackStatsMap.get(trackKey);
                if (ms >= 30000) {
                    trackStats.addStream(playedAt, entry);
                }
                if (entry.getMsPlayed() <= 5000 && (entry.isSkipped() || !entry.getReasonEnd().equals("trackdone"))) {
                    trackStats.incrementSkip();
                }

                // ArtistStats
                artistStatsMap.putIfAbsent(artist, new ArtistStats(artist, playedAt, entry));
                ArtistStats artistStats = artistStatsMap.get(artist);
                if (ms >= 30000) {
                    artistStats.addStream(playedAt, entry);
                }
                if (entry.getMsPlayed() <= 5000 && (entry.isSkipped() || !entry.getReasonEnd().equals("trackdone"))) {
                    artistStats.incrementSkip();
                }

                // AlbumStats
                Set<String> artistSet = new HashSet<>();
                albumStatsMap.putIfAbsent(album, new AlbumStats(album, artistSet, playedAt, entry));
                AlbumStats albumStats = albumStatsMap.get(album);
                if (ms >= 30000) {
                    albumStats.addStream(ms, playedAt, entry);
                }
                if (entry.getMsPlayed() <= 5000 && (entry.isSkipped() || !entry.getReasonEnd().equals("trackdone"))) {
                    albumStats.incrementSkip();
                }

            } else if (podcast != null && ms > 5000) {
                podcastStatsMap.put(podcast, podcastStatsMap.containsKey(podcast) ? podcastStatsMap.get(podcast) + 1 : 1);
            }
        }

        public Map<String, TrackStats> getTrackStats() { return trackStatsMap; }
        public Map<String, ArtistStats> getArtistStats() { return artistStatsMap; }
        public Map<String, AlbumStats> getAlbumStats() { return albumStatsMap; }
        public Map<String, Integer> getPodcastStats() { return podcastStatsMap; }
    }

    public static class TrackStats {
        String trackName;
        String artist;
        int streamCount = 0;
        int skipCount = 0;
        String firstPlayedDate = null;
        List<SpotifyPlaybackEntry> playbackHistory = new ArrayList<>();

        public TrackStats(String trackName, String artist, String playedAt, SpotifyPlaybackEntry entry) {
            this.trackName = trackName;
            this.artist = artist;
            this.streamCount = 1;
            this.firstPlayedDate = playedAt;
            this.playbackHistory.add(entry);
        }

        public void addStream(String playedAt, SpotifyPlaybackEntry entry) {
            streamCount++;
            if (firstPlayedDate == null || playedAt.compareTo(firstPlayedDate) < 0) {
                firstPlayedDate = playedAt;
            }
//            playbackHistory.add(entry);
        }

        public void incrementSkip() {
            skipCount++;
        }
        public String getTrackName() { return trackName; }
        public String getArtist() { return artist; }
        public int getStreamCount() { return streamCount; }
        public int getSkipCount() { return skipCount; }
        public String getFirstPlayedDate() { return firstPlayedDate; }
        public List<SpotifyPlaybackEntry> getPlaybackHistory() { return playbackHistory; }
    }

    public static class ArtistStats {
        String artist;
        int streamCount = 0;
        int uniqueStreamCount = 0;
        int skipCount = 0;
        String firstPlayedDate = null;
        List<SpotifyPlaybackEntry> playbackHistory = new ArrayList<>();
        List<String> uniqueStreamsSeen = new ArrayList<>();

        public ArtistStats(String artist, String playedAt, SpotifyPlaybackEntry entry) {
            this.artist = artist;
            this.streamCount = 1;
            this.firstPlayedDate = playedAt;
            this.playbackHistory.add(entry);
        }

        public void addStream(String playedAt, SpotifyPlaybackEntry entry) {
            streamCount++;
            if (firstPlayedDate == null || playedAt.compareTo(firstPlayedDate) < 0) {
                firstPlayedDate = playedAt;
            }
            if (!uniqueStreamsSeen.contains(entry.getTrackName())) {
                uniqueStreamsSeen.add(entry.getTrackName());
                uniqueStreamCount++;
            }
//            playbackHistory.add(entry);
        }

        public void incrementSkip() {
            skipCount++;
        }
        public String getArtist() { return artist; }
        public int getStreamCount() { return streamCount; }
        public int getUniqueStreamCount() { return uniqueStreamCount; }
        public int getSkipCount() { return skipCount; }
        public String getFirstPlayedDate() { return firstPlayedDate; }
        public List<SpotifyPlaybackEntry> getPlaybackHistory() { return playbackHistory; }
        public List<String> getUniqueStreamsSeen() { return uniqueStreamsSeen; }
    }

    public static class AlbumStats {
        String album;
        Set<String> artist = new HashSet<>();
        int streamCount = 0;
        int skipCount = 0;
        double hours = 0;
        String firstPlayedDate = null;
        List<SpotifyPlaybackEntry> playbackHistory = new ArrayList<>();

        public AlbumStats(String album, Set<String> artist, String playedAt, SpotifyPlaybackEntry entry) {
            this.album = album;
            this.artist = artist;
            this.streamCount = 1;
            this.hours = 0;
            this.firstPlayedDate = playedAt;
            this.playbackHistory.add(entry);
        }

        public void addStream(int ms, String playedAt, SpotifyPlaybackEntry entry) {
            streamCount++;
            if (firstPlayedDate == null || playedAt.compareTo(firstPlayedDate) < 0) {
                firstPlayedDate = playedAt;
            }
            artist.add(entry.getArtistName());
            hours += ms / 1000.0 / 60.0 / 60.0;
//            playbackHistory.add(entry);
        }

        public void incrementSkip() {
            skipCount++;
        }
        public String getAlbum() { return album; }
        public String getArtist() { return String.join(", ", artist); }
        public int getStreamCount() { return streamCount; }
        public double getHours() { return hours; }
        public int getSkipCount() { return skipCount; }
        public String getFirstPlayedDate() { return firstPlayedDate; }
        public List<SpotifyPlaybackEntry> getPlaybackHistory() { return playbackHistory; }
    }

    public static class GeneralStatsCollector {
        private int totalEntries;
        private int totalStreams;
        private int totalUniqueStreams;
        private int totalSkippedTracks;
        private int percentageTimeShuffled;
        private Map<String, Integer> totalMusicTime;
        private Map<String, Integer> totalPodcastTime;
        private String totalArtistRevenue;
        private Map<String, String> firstTrackEver;

        private int rawMusicTime = 0;
        private int rawPodcastTime = 0;
        private int shuffleCount = 0;
        private Set<String> uniqueTracks = new HashSet<>();
        private SpotifyPlaybackEntry firstEntry = null;

        public void processEntry(SpotifyPlaybackEntry entry) {
            if (entry.getTrackName() != null) {
                totalEntries++;
                rawMusicTime += entry.getMsPlayed();

                if (entry.isShuffle()) shuffleCount++;

                if (entry.getMsPlayed() >= 30000) {
                    totalStreams++;
                    uniqueTracks.add(entry.getTrackName());
                }

                if (entry.getMsPlayed() <= 5000 && (entry.isSkipped() || !entry.getReasonEnd().equals("trackdone"))) {
                    totalSkippedTracks++;
                }

                if (firstEntry == null || Instant.parse(entry.getTimestamp()).isBefore(Instant.parse(firstEntry.getTimestamp()))) {
                    firstEntry = entry;
                }
            }

            if (entry.getPodcastName() != null) {
                rawPodcastTime += entry.getMsPlayed();
            }
        }

        public void finalizeStats() {
            totalUniqueStreams = uniqueTracks.size();
            percentageTimeShuffled = totalEntries == 0 ? 0 : (int) Math.round((double) shuffleCount / totalEntries * 100);
            totalArtistRevenue = cleanArtistRevenue((float) totalStreams);
            totalMusicTime = convertMillisToTimeMap(rawMusicTime);
            totalPodcastTime = convertMillisToTimeMap(rawPodcastTime);
            firstTrackEver = cleanFirstTrackEver(firstEntry);
        }

        public int getTotalEntries() { return totalEntries; }
        public int getTotalStreams() { return totalStreams; }
        public int getTotalUniqueStreams() { return totalUniqueStreams; }
        public int getTotalSkippedTracks() { return totalSkippedTracks; }
        public int getPercentageTimeShuffled() { return percentageTimeShuffled; }
        public Map<String, Integer> getTotalMusicTime() { return totalMusicTime; }
        public Map<String, Integer> getTotalPodcastTime() { return totalPodcastTime; }
        public String getTotalArtistRevenue() { return totalArtistRevenue; }
        public Map<String, String> getFirstTrackEver() { return firstTrackEver; }

    }

    public static class DailyStatsCollector {
        private final Map<String, DailyStats> dailyStatsMap = new LinkedHashMap<>();

        public void processEntry(SpotifyPlaybackEntry entry) {
            int ms = entry.getMsPlayed();
            if (ms >= 30000) {
                String timestamp = entry.getTimestamp();
                ZonedDateTime zdt = ZonedDateTime.parse(timestamp);
                String date = zdt.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

                DailyStats daily = dailyStatsMap.computeIfAbsent(date, k -> new DailyStats());
                daily.addPlay(entry);
            }
        }

        public void finalizeStats() {
            for (DailyStats stats : dailyStatsMap.values()) {
                stats.finalizeStats();
            }
        }

        public Map<String, DailyStats> getDailyStatsMap() {
            return dailyStatsMap;
        }
    }

    public static class DailyStats {

        public int streams = 0;
        public double hours = 0;
        public Map<TrackInfo, Integer> topTracks = new LinkedHashMap<>();
        public Map<String, Integer> topArtists = new LinkedHashMap<>();

        private DailyStats() {}

        void addPlay(SpotifyPlaybackEntry entry) {
            streams++;
            hours += entry.getMsPlayed() / 1000.0 / 60.0 / 60.0;

            String trackName = entry.getTrackName();
            String artistName = entry.getArtistName();
            if (trackName != null && artistName != null) {
                TrackInfo trackInfo = new TrackInfo(trackName, artistName);
                topTracks.merge(trackInfo, 1, Integer::sum);
            }
            if (artistName != null) {
                topArtists.merge(artistName, 1, Integer::sum);
            }
        }

        void finalizeStats() {
            topTracks = topTracks.entrySet().stream()
                    .sorted(Map.Entry.<TrackInfo, Integer>comparingByValue().reversed())
                    .limit(5)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            topArtists = topArtists.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(5)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
        }
    }

    public static class YearlyStatsCollector {

        private final Map<String, YearlyStats> yearlyStatsMap = new LinkedHashMap<>();
        private final Set<String> songsSeen = new HashSet<>();


        public void processEntry(SpotifyPlaybackEntry entry) {
            String timeStamp = entry.getTimestamp();
            int ms = entry.getMsPlayed();
            String track = entry.getSpotifyTrackUri();
            String podcast = entry.getPodcastName();

            if((track != null && ms >= 30000) || (podcast != null && ms > 5000)) {
                ZonedDateTime zdt = ZonedDateTime.parse(timeStamp);
                String year = String.valueOf(zdt.getYear());

                YearlyStats stats = yearlyStatsMap.computeIfAbsent(year, k -> new YearlyStats());
                if(track != null) {
                    boolean isUnique = songsSeen.add(track);
                    stats.addPlay(ms, isUnique);
                }
                if(podcast != null) {
                    stats.addPodcastPlay(ms);
                }
            }
        }

        public Map<String, YearlyStats> getYearlyStatsMap() {
            return yearlyStatsMap;
        }
    }

    public static class YearlyStats {

        public int streams = 0;
        public double musicHours = 0;
        public int uniqueStreams = 0;
        public int podcastPlays = 0;
        public double podcastHours = 0;



        private void addPlay(int ms, boolean uniquePlay) {
            streams++;
            musicHours += ms / 1000.0 / 60.0 / 60.0;
            if(uniquePlay) {
                uniqueStreams++;
            }
        }

        private void addPodcastPlay(int ms) {
            podcastPlays++;
            podcastHours += ms / 1000.0 / 60.0 / 60.0;
        }
    }

    public record TrackInfo(String name, String artist) {
        @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof TrackInfo)) return false;
                TrackInfo that = (TrackInfo) o;
                return name.equals(that.name) && artist.equals(that.artist);
            }

        @Override
            public String toString() {
                return name + " (" + artist + ")";
            }
        }
}