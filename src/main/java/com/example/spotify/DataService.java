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

    //    Methods used in SpotifyApiController.java

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

    public Map<String, TrackStats> getTrackStatsMap(String folderPath) {
        Map<String, TrackStats> map = new LinkedHashMap<>();

        processSessionFolder(folderPath, entry -> {
            String track = entry.getTrackName();
            String artist = entry.getArtistName();
            String playedAt = entry.getTimestamp();
            int ms = entry.getMsPlayed();

            if (track == null || artist == null) return;

            String key = track + " - " + artist;

            map.putIfAbsent(key, new TrackStats(track, artist, playedAt, null));
            TrackStats stats = map.get(key);

            if (ms >= 30000) {
                stats.addStream(playedAt, null);
            }

            if (entry.isSkipped()) {
                stats.incrementSkip();
            }
        });

        return map;
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

    public Map<String, ArtistStats> getArtistStatsMap(String folderPath) {
        Map<String, ArtistStats> map = new LinkedHashMap<>();

        processSessionFolder(folderPath, entry -> {
            String artist = entry.getArtistName();
            String playedAt = entry.getTimestamp();
            int ms = entry.getMsPlayed();

            if (artist != null) {
                map.putIfAbsent(artist, new ArtistStats(artist, playedAt, entry));
                ArtistStats stats = map.get(artist);

                if (ms >= 30000) {
                    stats.addStream(playedAt, entry);
                }

                if (entry.isSkipped()) {
                    stats.incrementSkip();
                }
            }
        });
        return map;
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
//            playbackHistory.add(entry);
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

    public Map<String, AlbumStats> getAlbumStatsMap(String folderPath) {
        Map<String, AlbumStats> map = new LinkedHashMap<>();

        processSessionFolder(folderPath, entry -> {
            String album = entry.getAlbumName();
            String artist = entry.getArtistName();
            String playedAt = entry.getTimestamp();
            int ms = entry.getMsPlayed();

            if (album != null) {
                map.putIfAbsent(album, new AlbumStats(album, artist, playedAt, entry));
                AlbumStats stats = map.get(album);

                if (ms >= 30000) {
                    stats.addStream(ms, playedAt, entry);
                }

                if (entry.isSkipped()) {
                    stats.incrementSkip();
                }
            }
        });
        return map;
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

        public GeneralStats(int totalEntries, int totalStreams, int totalUniqueStreams, int totalSkippedTracks, Map<String, Integer> totalMusicTime, int percentageTimeShuffled, Map<String, Integer> totalPodcastTime, String totalArtistRevenue) {
            this.totalEntries = totalEntries;
            this.totalStreams = totalStreams;
            this.totalUniqueStreams = totalUniqueStreams;
            this.totalSkippedTracks = totalSkippedTracks;
            this.totalMusicTime = totalMusicTime;
            this.percentageTimeShuffled = percentageTimeShuffled;
            this.totalPodcastTime = totalPodcastTime;
            this.totalArtistRevenue = totalArtistRevenue;
        }

        public int getTotalEntries() { return totalEntries; }
        public int getTotalStreams() { return totalStreams; }
        public int getTotalUniqueStreams() { return totalUniqueStreams; }
        public int getTotalSkippedTracks() { return totalSkippedTracks; }
        public int getPercentageTimeShuffled() { return percentageTimeShuffled; }
        public Map<String, Integer> getTotalMusicTime() { return totalMusicTime; }
        public Map<String, Integer> getTotalPodcastTime() { return totalPodcastTime; }
        public String getTotalArtistRevenue() { return totalArtistRevenue; }
    }

    public GeneralStats getGeneralStats(String folderPath) {
        int[] totalEntries = {0};
        int[] totalStreams = {0};
        int[] totalSkippedTracks = {0};
        int[] totalMusicTime = {0};
        int[] totalPodcastTime = {0};
        int[] tracksOnShuffle = {0};
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
        });
        int percentageTimeShuffled = totalEntries[0] == 0 ? 0 : (int) Math.round(((double) tracksOnShuffle[0] / totalEntries[0]) * 100);
        int totalUniqueStreams = uniqueTracks.size();

//      Total Artist Revenue Logic
        float totalStreamsFloat = (float) totalStreams[0];
        DecimalFormat df = new DecimalFormat("#.00");
        double royalties = totalStreamsFloat * 0.004;
        String totalArtistRevenue = df.format(royalties);

        Map<String, Integer> totalMusicTimeMap = convertMillisToTimeMap(totalMusicTime[0]);
        Map<String, Integer> totalPodcastTimeMap = convertMillisToTimeMap(totalPodcastTime[0]);

        return new GeneralStats(
                totalEntries[0],
                totalStreams[0],
                totalUniqueStreams,
                totalSkippedTracks[0],
                totalMusicTimeMap,
                percentageTimeShuffled,
                totalPodcastTimeMap,
                totalArtistRevenue
        );
    }

    public Map<String, Integer> getTopPodcastsByPlays(String folderPath) {
        Map<String, Integer> map = new LinkedHashMap<>();

        processSessionFolder(folderPath, entry -> {
            String podcast = entry.getPodcastName();
            if (podcast != null) {
                map.put(podcast, map.containsKey(podcast) ? map.get(podcast) + 1 : 1);
            }
        });
        return map;
    }

    public Map<String, String> getFirstTrackEver(String folderPath) {
        Map <String, String> map = new HashMap<>();
        final String[] firstTimeStamp = {null};
        final String[] firstTrack = {null};
        final String[] firstArtist = {null};

        processSessionFolder(folderPath, entry -> {
            String track = entry.getTrackName();
            String timeStamp = entry.getTimestamp();
            String artist = entry.getArtistName();

            if (track != null && timeStamp != null) {
                if (firstTimeStamp[0] == null) {
                    firstTimeStamp[0] = timeStamp;
                    firstTrack[0] = track;
                    firstArtist[0] = artist;
                } else {
                    Instant earliest = Instant.parse(firstTimeStamp[0]);
                    Instant current = Instant.parse(timeStamp);
                    if (current.isBefore(earliest)) {
                        firstTrack[0] = track;
                        firstTimeStamp[0] = timeStamp;
                        firstArtist[0] = artist;
                    }
                }
            }
        });
        if (firstTimeStamp[0] == null || firstTrack[0] == null || firstArtist[0] == null) {
            map.put("track", "N/A");
            map.put("timeStamp", "N/A");
            map.put("artist", "N/A");
            return map;
        }

        ZonedDateTime zdt = ZonedDateTime.parse(firstTimeStamp[0]);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        String readableTimeStamp = zdt.format(formatter);
        map.put("track", firstTrack[0]);
        map.put("timeStamp", readableTimeStamp);
        map.put("artist", firstArtist[0]);
        return map;
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