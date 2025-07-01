package com.example.spotify;

import jakarta.annotation.PostConstruct;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.spotify.model.SpotifyPlaybackEntry;
import com.example.spotify.util.SpotifyParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

@Component
public class DataService {

//    @Value("${extended.streaming.history.path}")

    private JSONArray cachedData;

    public void loadSessionFolder(String folderPath) {
        this.cachedData = collectExtendedData(folderPath);
    }

    public static Map<String, Integer> sortAndSizeMap(Map<String, Integer> map, int size) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(size)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public static void prettyPrintMap(Map<String, Integer> map) {
        map.forEach((k, v) -> System.out.println(k + ": " + v));
    }

    private JSONArray collectExtendedData(String folderPath) {
        JSONArray res = new JSONArray();
        File folder = new File(folderPath);

        try {
            for (File file : folder.listFiles()) {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".json") && fileName.contains("audio")) {
                    String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
                    JSONArray jsonArray = new JSONArray(content);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        res.put(jsonArray.getJSONObject(i));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return res;
    }


//    Methods used in SpotifyApiController.java
    public Integer getTotalEntries() {
        return this.cachedData.length();
    }

    public Integer getTotalStreams() {
        int totalStreams = 0;
        for (int i = 0; i < this.cachedData.length(); i++) {
            JSONObject obj = this.cachedData.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String track = entry.getTrackName();
            int ms = entry.getMsPlayed();
            if (track != null && ms >= 30000) {
                totalStreams++;
            }
        }
        return totalStreams;
    }

    public Integer getTotalUniqueEntries() {
        Set<String> uniqueTracks = new HashSet<String>();
        for (int i = 0; i < this.cachedData.length(); i++) {
            JSONObject obj = this.cachedData.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String track = entry.getTrackName();
            uniqueTracks.add(track);
        }
        return uniqueTracks.size();
    }

    public Integer getTotalSkippedTracks() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < this.cachedData.length(); i++) {
            JSONObject obj = this.cachedData.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String artist = entry.getArtistName();
            int msPlayed = entry.getMsPlayed();
            if (artist != null && msPlayed >= 5000) {
                    map.put(artist, map.getOrDefault(artist, 0) + 1);
            }
        }
        int res = map.values().stream().mapToInt(Integer::intValue).sum();
        return res;
    }

    public Map<String, Integer> getTotalMusicTime() {
        Map<String, Integer> map = new LinkedHashMap<>();
        int ms = 0;
        for (int i = 0; i < this.cachedData.length(); i++) {
            JSONObject obj = this.cachedData.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String track = entry.getTrackName();
            if (track != null) {
                int msPlayed = entry.getMsPlayed();
                ms += msPlayed;
            }
        }
        int minutes = ms / 60000;
        int hours = minutes / 60;
        int days = hours / 24;
        map.put("minutes", minutes);
        map.put("hours", hours);
        map.put("days", days);
        return map;
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

    public Map<String, TrackStats> getTrackStatsMap() {
        Map<String, TrackStats> map = new LinkedHashMap<>();

        for (int i = 0; i < this.cachedData.length(); i++) {
            JSONObject obj = this.cachedData.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String track = entry.getTrackName();
            String artist = entry.getArtistName();
            String playedAt = entry.getTimestamp();
            int ms = entry.getMsPlayed();

            if (track == null || artist == null) continue;

            map.putIfAbsent(track, new TrackStats(track, artist, playedAt, entry));
            TrackStats stats = map.get(track);

            if (ms >= 30000) {
                stats.addStream(playedAt, entry);
            }

            if (entry.isSkipped()) {
                stats.incrementSkip();
            }
        }

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

    public Map<String, ArtistStats> getArtistStatsMap() {
        Map<String, ArtistStats> map = new LinkedHashMap<>();

        for (int i = 0; i < this.cachedData.length(); i++) {
            JSONObject obj = this.cachedData.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String artist = entry.getArtistName();
            String playedAt = entry.getTimestamp();
            int ms = entry.getMsPlayed();

            if (artist == null) continue;

            map.putIfAbsent(artist, new ArtistStats(artist, playedAt, entry));
            ArtistStats stats = map.get(artist);

            if (ms >= 30000) {
                stats.addStream(playedAt, entry);
            }

            if (entry.isSkipped()) {
                stats.incrementSkip();
            }
        }

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

    public Map<String, AlbumStats> getAlbumStatsMap() {
        Map<String, AlbumStats> map = new LinkedHashMap<>();

        for (int i = 0; i < this.cachedData.length(); i++) {
            JSONObject obj = this.cachedData.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String album = entry.getAlbumName();
            String artist = entry.getArtistName();
            String playedAt = entry.getTimestamp();
            int ms = entry.getMsPlayed();

            if (artist == null) continue;

            map.putIfAbsent(album, new AlbumStats(album, artist, playedAt, entry));
            AlbumStats stats = map.get(album);

            if (ms >= 30000) {
                stats.addStream(ms, playedAt, entry);
            }

            if (entry.isSkipped()) {
                stats.incrementSkip();
            }
        }

        return map;
    }

    public Integer getPercentageTimeShuffled() {
        int tracksOnShuffle = 0;
        int totalTracks = this.cachedData.length();

        for (int i = 0; i < totalTracks; i++) {
            JSONObject obj = this.cachedData.getJSONObject(i); // use 'i', not '0'
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            if (entry.isShuffle()) {
                tracksOnShuffle++;
            }
        }

        if (totalTracks == 0) return 0;

        double percentage = ((double) tracksOnShuffle / totalTracks) * 100;
        return (int) Math.round(percentage);
    }

    public Map<String, Integer> getTotalPodcastTime() {
        Map<String, Integer> map = new LinkedHashMap<>();
        int ms = 0;
        for (int i = 0; i < this.cachedData.length(); i++) {
            JSONObject obj = this.cachedData.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String podcast = entry.getPodcastName();
            if (podcast != null) {
                int msPlayed = entry.getMsPlayed();
                ms += msPlayed;
            }
        }
        int minutes = ms / 60000;
        int hours = minutes / 60;
        int days = hours / 24;
        map.put("minutes", minutes);
        map.put("hours", hours);
        map.put("days", days);
        return map;
    }

    public Map<String, Integer> getTopPodcastsByPlays() {
        Map<String, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < this.cachedData.length(); i++) {
            JSONObject obj = this.cachedData.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String podcast = entry.getPodcastName();
            if (podcast != null) {
                map.put(podcast, map.containsKey(podcast) ? map.get(podcast) + 1 : 1);
            }
        }
        return map;
    }

    public Map<String, String> getFirstTrackEver() {
        Map <String, String> map = new HashMap<>();
        String firstTimeStamp = null;
        String firstTrack = null;
        String firstArtist = null;
        for (int i = 0; i < this.cachedData.length(); i++) {
            JSONObject obj = this.cachedData.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String track = entry.getTrackName();
            String timeStamp = entry.getTimestamp();
            String artist = entry.getArtistName();

            if (track != null) {
                if (firstTimeStamp == null) {
                    firstTimeStamp = timeStamp;
                    continue;
                }
                Instant earliest = Instant.parse(firstTimeStamp);
                Instant current = Instant.parse(timeStamp);
                if (current.isBefore(earliest)) {
                    firstTrack = track;
                    firstTimeStamp = timeStamp;
                    firstArtist = artist;
                }

            }
        }
        ZonedDateTime zdt = ZonedDateTime.parse(firstTimeStamp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        String readableTimeStamp = zdt.format(formatter);
        map.put("track", firstTrack);
        map.put("timeStamp", readableTimeStamp);
        map.put("artist", firstArtist);
        return map;
    }

    public String getTotalArtistRevenue() {
        float totalStreams = 0;
        for (int i = 0; i < this.cachedData.length(); i++) {
            JSONObject obj = this.cachedData.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            float msplayed = entry.getMsPlayed();
            if (msplayed >= 30000) {
                totalStreams++;
            }
        }
        DecimalFormat df = new DecimalFormat("#.00");
        double royalties = totalStreams * 0.004;
        String formattedRoyalties = df.format(royalties);
        return formattedRoyalties;
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


    public Map<String, DailyStats> getTopDays() {
        Map<String, DailyStats> map = new LinkedHashMap<>();
        for (int i = 0; i < this.cachedData.length(); i++) {
            JSONObject obj = this.cachedData.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String timeStamp = entry.getTimestamp();
            int ms = entry.getMsPlayed();

            if(ms >= 30000) {
                ZonedDateTime zdt = ZonedDateTime.parse(timeStamp);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                String readableTimeStamp = zdt.format(formatter);
                DailyStats daily = map.computeIfAbsent(readableTimeStamp, k -> new DailyStats());
                daily.addPlay(entry);
            }
        }
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

    public Map<String, YearlyStats> getTopYears () {
        Map<String, YearlyStats> map = new LinkedHashMap<>();
        Set<String> songsSeen = new HashSet<>();
        for (int i = 0; i < this.cachedData.length(); i++) {
            JSONObject obj = this.cachedData.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String timeStamp = entry.getTimestamp();
            int ms = entry.getMsPlayed();
            String track = entry.getSpotifyTrackUri();

            if(ms >= 30000) {
                ZonedDateTime zdt = ZonedDateTime.parse(timeStamp);
                String year = String.valueOf(zdt.getYear());

                YearlyStats yearly = map.computeIfAbsent(year, k -> new YearlyStats());
                if (songsSeen.contains(track)) {
                    yearly.addPlay(ms, false);
                    continue;
                }
                yearly.addPlay(ms, true);
                songsSeen.add(track);
            }
        }
        return map;
    }



//    Not used quite yet
    public Map<String, Integer> getTopTimeListened() {
    Map<String, Double> map = new LinkedHashMap<>();

    for (int i = 0; i < this.cachedData.length(); i++) {
        JSONObject obj = this.cachedData.getJSONObject(i);
        SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);

        String artist = entry.getArtistName();
        if (artist != null) {
            double hoursPlayed = entry.getMsPlayed() / 3600000.0;
            map.put(artist, map.getOrDefault(artist, 0.0) + hoursPlayed);
        }
    }
    return map.entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> (int) Math.round(e.getValue()),
                    (e1, e2) -> e1,
                    LinkedHashMap::new
            ));
}

    public static Map<String, Integer> reasonStart(JSONArray array) {
        Map<String, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String reason = entry.getReasonStart();
            if (reason != null) {
                map.put(reason, map.getOrDefault(reason, 0) + 1);
            }
        }
        return map;
    }

    public static Map<String, Integer> reasonSkipped(JSONArray array) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String reason_end = entry.getReasonEnd();
            Boolean skipped = entry.isSkipped();
            if (skipped) {
                map.put(reason_end, map.getOrDefault(reason_end, 0) + 1);
            }
        }
        return map;
    }

    public static JSONArray singleSongData(JSONArray array, String track) {
        JSONArray res = new JSONArray();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String entryTrack = entry.getTrackName();
            if (entryTrack != null && entryTrack.equalsIgnoreCase(track)) {
                res.put(obj);
            }
        }
        return res;
    }

    public static JSONArray singleAlbumData(JSONArray array, String album) {
        JSONArray res = new JSONArray();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String entryAlbum = entry.getAlbumName();
            if (entryAlbum != null && entryAlbum.equalsIgnoreCase(album)) {
                res.put(obj);
            }
        }
        return res;
    }

    public static JSONArray singleArtistData(JSONArray array, String artist) {
        JSONArray res = new JSONArray();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String entryArtist = entry.getArtistName();
            if (entryArtist != null && entryArtist.equalsIgnoreCase(artist)) {
                res.put(obj);
            }
        }
        return res;
    }
}