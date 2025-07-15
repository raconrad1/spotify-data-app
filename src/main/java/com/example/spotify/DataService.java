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

    public void loadSessionFolder(String folderPath) {
        this.currentSessionFolder = folderPath;
    }

    public String getCurrentSessionFolder() {
        return this.currentSessionFolder;
    }

    public static void processSessionFolder(String folderPath, Consumer<SpotifyPlaybackEntry> entryConsumer) {
        if (folderPath == null) {
            throw new IllegalArgumentException("folderPath is null");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();

        try {
            Files.walk(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> parseFile(path, factory, mapper, entryConsumer));
        } catch (IOException e) {
            System.err.println("Error walking folder: " + e.getMessage());
        }
    }

    private static void parseFile(Path path, JsonFactory factory, ObjectMapper mapper, Consumer<SpotifyPlaybackEntry> entryConsumer) {
        String fileName = path.getFileName().toString().toLowerCase();
        if (!fileName.endsWith(".json") || !fileName.contains("audio") || fileName.startsWith("._")) return;

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
                entryConsumer.accept(entry);
            }

        } catch (Exception e) {
            System.err.println("Failed to parse " + fileName + ": " + e.getMessage());
        }
    }

    private Map<String, Integer> convertMillisToTimeMap(int ms) {
        int minutes = ms / 60000;
        int hours = minutes / 60;
        int days = hours / 24;

        Map<String, Integer> timeMap = new HashMap<>();
        timeMap.put("minutes", minutes);
        timeMap.put("hours", hours);
        timeMap.put("days", days);

        return timeMap;
    }

    private Map<String, String> cleanFirstTrackEver(SpotifyPlaybackEntry firstEntry) {
        Map<String, String> firstTrackEver = new HashMap<>();
        if (firstEntry == null) {
            firstTrackEver.put("track", "N/A");
            firstTrackEver.put("timeStamp", "N/A");
            firstTrackEver.put("artist", "N/A");
        }
        ZonedDateTime zdt = ZonedDateTime.parse(firstEntry.getTimestamp());
        String formatted = zdt.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a"));

        firstTrackEver.put("track", firstEntry.getTrackName());
        firstTrackEver.put("artist", firstEntry.getArtistName());
        firstTrackEver.put("timeStamp", formatted);
        return firstTrackEver;
    }

    private String cleanArtistRevenue(float totalStreamsFloat) {
        DecimalFormat df = new DecimalFormat("#.00");
        double royalties = totalStreamsFloat * 0.004;
        return df.format(royalties);
    }

    //    Methods used in SpotifyApiController.java

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
                if (entry.isSkipped()) {
                    trackStats.incrementSkip();
                }

                // ArtistStats
                artistStatsMap.putIfAbsent(artist, new ArtistStats(artist, playedAt, entry));
                ArtistStats artistStats = artistStatsMap.get(artist);
                if (ms >= 30000) {
                    artistStats.addStream(playedAt, entry);
                }
                if (entry.isSkipped()) {
                    artistStats.incrementSkip();
                }

                // AlbumStats
                String albumKey = album + " - " + artist;
                albumStatsMap.putIfAbsent(albumKey, new AlbumStats(album, artist, playedAt, entry));
                AlbumStats albumStats = albumStatsMap.get(albumKey);
                if (ms >= 30000) {
                    albumStats.addStream(ms, playedAt, entry);
                }
                if (entry.isSkipped()) {
                    albumStats.incrementSkip();
                }

            } else if (podcast != null) {
                podcastStatsMap.put(podcast, podcastStatsMap.containsKey(podcast) ? podcastStatsMap.get(podcast) + 1 : 1);
            }
        }

        public Map<String, TrackStats> getTrackStats() { return trackStatsMap; }
        public Map<String, ArtistStats> getArtistStats() { return artistStatsMap; }
        public Map<String, AlbumStats> getAlbumStats() { return albumStatsMap; }
        public Map<String, Integer> getPodcastStats() { return podcastStatsMap; }
    }

    public TopStatsCollector getTopStats(String folderPath) {
        TopStatsCollector aggregator = new TopStatsCollector();

        processSessionFolder(folderPath, aggregator::processEntry);

        return aggregator;
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
            playbackHistory.add(entry);
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
            playbackHistory.add(entry);
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
        String artist;
        int streamCount = 0;
        int skipCount = 0;
        double hours = 0;
        String firstPlayedDate = null;
        List<SpotifyPlaybackEntry> playbackHistory = new ArrayList<>();

        public AlbumStats(String album, String artist, String playedAt, SpotifyPlaybackEntry entry) {
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
            hours += ms / 1000.0 / 60.0 / 60.0;
            playbackHistory.add(entry);
        }

        public void incrementSkip() {
            skipCount++;
        }
        public String getAlbum() { return album; }
        public String getArtist() { return artist; }
        public int getStreamCount() { return streamCount; }
        public double getHours() { return hours; }
        public int getSkipCount() { return skipCount; }
        public String getFirstPlayedDate() { return firstPlayedDate; }
        public List<SpotifyPlaybackEntry> getPlaybackHistory() { return playbackHistory; }
    }

    public static class GeneralStats {
        int totalEntries;
        int totalStreams;
        int totalUniqueStreams;
        int totalSkippedTracks;
        int percentageTimeShuffled;
        Map<String, Integer> totalMusicTime;
        Map<String, Integer> totalPodcastTime;
        String totalArtistRevenue;
        Map<String, String> firstTrackEver;

        public GeneralStats(int totalEntries, int totalStreams, int totalUniqueStreams, int totalSkippedTracks, Map<String, Integer> totalMusicTime, int percentageTimeShuffled, Map<String, Integer> totalPodcastTime, String totalArtistRevenue, Map<String, String> firstTrackEver) {
            this.totalEntries = totalEntries;
            this.totalStreams = totalStreams;
            this.totalUniqueStreams = totalUniqueStreams;
            this.totalSkippedTracks = totalSkippedTracks;
            this.totalMusicTime = totalMusicTime;
            this.percentageTimeShuffled = percentageTimeShuffled;
            this.totalPodcastTime = totalPodcastTime;
            this.totalArtistRevenue = totalArtistRevenue;
            this.firstTrackEver = firstTrackEver;
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

    public GeneralStats getGeneralStats(String folderPath) {
        int[] totalEntries = {0};
        int[] totalStreams = {0};
        int[] totalSkippedTracks = {0};
        int[] totalMusicTime = {0};
        int[] totalPodcastTime = {0};
        int[] tracksOnShuffle = {0};
        SpotifyPlaybackEntry[] firstEntry = {null};
        Set<String> uniqueTracks = new HashSet<>();

        processSessionFolder(folderPath, entry -> {
            if (entry.getTrackName() != null) {
                totalEntries[0]++;
                totalMusicTime[0] += entry.getMsPlayed();
                if (entry.isShuffle()) {
                    tracksOnShuffle[0]++;
                }
            }
            if (entry.getTrackName() != null && entry.getMsPlayed() >= 30000) {
                totalStreams[0]++;
                uniqueTracks.add(entry.getTrackName());
            }
            if (entry.getTrackName() != null && entry.getMsPlayed() <= 5000 && entry.isSkipped()) {
                totalSkippedTracks[0]++;
            }
            if (entry.getPodcastName() != null) {
                totalPodcastTime[0] += entry.getMsPlayed();
            }

//           First track ever logic
            if (entry.getTrackName() == null || entry.getTimestamp() == null) return;

            if (firstEntry[0] == null || Instant.parse(entry.getTimestamp()).isBefore(Instant.parse(firstEntry[0].getTimestamp()))) {
                firstEntry[0] = entry;
            }
        });


//      Start data formatting/manipulation
        int percentageTimeShuffled = totalEntries[0] == 0 ? 0 : (int) Math.round(((double) tracksOnShuffle[0] / totalEntries[0]) * 100);
        int totalUniqueStreams = uniqueTracks.size();
        String totalArtistRevenue = cleanArtistRevenue((float) totalStreams[0]);
        Map<String, Integer> totalMusicTimeMap = convertMillisToTimeMap(totalMusicTime[0]);
        Map<String, Integer> totalPodcastTimeMap = convertMillisToTimeMap(totalPodcastTime[0]);
        Map <String, String> firstTrackEver = cleanFirstTrackEver(firstEntry[0]);

        return new GeneralStats(
                totalEntries[0],
                totalStreams[0],
                totalUniqueStreams,
                totalSkippedTracks[0],
                totalMusicTimeMap,
                percentageTimeShuffled,
                totalPodcastTimeMap,
                totalArtistRevenue,
                firstTrackEver
        );
    }

    public static class DailyStats {
        public int streams = 0;
        public double hours = 0;
        public Map<String, Integer> topTracks = new LinkedHashMap<>();
        public Map<String, Integer> topArtists = new LinkedHashMap<>();

        void addPlay(SpotifyPlaybackEntry entry) {
            streams++;
            hours += entry.getMsPlayed() / 1000.0 / 60.0 / 60.0;

            String trackName = entry.getTrackName();
            String artistName = entry.getArtistName();
            if (trackName != null) {
                topTracks.merge(trackName, 1, Integer::sum);
            }
            if (artistName != null) {
                topArtists.merge(artistName, 1, Integer::sum);
            }
        }
        void finalizeStats() {
            topTracks = topTracks.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
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

    public Map<String, DailyStats> getTopDays(String folderPath) {
        Map<String, DailyStats> map = new LinkedHashMap<>();

        processSessionFolder(folderPath, entry -> {
            String timeStamp = entry.getTimestamp();
            int ms = entry.getMsPlayed();

            if(ms >= 30000) {
                ZonedDateTime zdt = ZonedDateTime.parse(timeStamp);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                String readableTimeStamp = zdt.format(formatter);
                DailyStats daily = map.computeIfAbsent(readableTimeStamp, k -> new DailyStats());
                daily.addPlay(entry);
            }
        });
        for (DailyStats dailyStats : map.values()) {
            dailyStats.finalizeStats();
        }
        return map;
    }

    public static class YearlyStats {
        public int streams = 0;
        public double hours = 0;
        public int uniqueStreams = 0;

        void addPlay(int ms, boolean uniquePlay) {
            streams++;
            hours += ms / 1000.0 / 60.0 / 60.0;
            if(uniquePlay) {
                uniqueStreams++;
            }
        }
    }

    public Map<String, YearlyStats> getTopYears (String folderPath) {
        Map<String, YearlyStats> map = new LinkedHashMap<>();
        Set<String> songsSeen = new HashSet<>();

        processSessionFolder(folderPath, entry -> {
            String timeStamp = entry.getTimestamp();
            int ms = entry.getMsPlayed();
            String track = entry.getSpotifyTrackUri();

            if(ms >= 30000) {
                ZonedDateTime zdt = ZonedDateTime.parse(timeStamp);
                String year = String.valueOf(zdt.getYear());

                YearlyStats yearly = map.computeIfAbsent(year, k -> new YearlyStats());
                if (songsSeen.contains(track)) {
                    yearly.addPlay(ms, false);
                } else {
                    yearly.addPlay(ms, true);
                    songsSeen.add(track);
                }
            }
        });

        return map;
    }
}