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
import java.util.*;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

@Component
public class DataService {

    @Value("${extended.streaming.history.path}")
    private String folderPath;

    private JSONArray cachedData;

    @PostConstruct
    public void init() {
        this.cachedData = collectExtendedData();
    }

    private JSONArray collectExtendedData() {
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

    public static Integer totalStreams(JSONArray data) {
        int totalStreams = 0;
        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String track = entry.getTrackName();
            int ms = entry.getMsPlayed();
            if (track != null && ms >= 30000) {
                totalStreams++;
            }
        }
        return totalStreams;
    }

    public static Map<String, Integer> totalSkippedTracks(JSONArray array, int seconds) {
        Map<String, Integer> map = new LinkedHashMap<>();
        seconds = seconds * 1000;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String artist = entry.getArtistName();
            if (artist != null) {
                int msPlayed = entry.getMsPlayed();
                if(msPlayed <= seconds) {
                    map.put(artist, map.getOrDefault(artist, 0) + 1);
            }
            }
        }
        return map;
    }

    public static Map<String, Integer> topTimeListened(JSONArray array) {
        Map<String, Double> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
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

    public static Map<String, Integer> totalMusicTime(JSONArray array) {
        Map<String, Integer> map = new LinkedHashMap<>();
        int ms = 0;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
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

    public static Map<String, Integer> topTracksByPlays(JSONArray array) {
        Map<String, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String track = entry.getTrackName();
            int ms = entry.getMsPlayed();
            if (track != null && ms >= 30000) {
                map.put(track, map.containsKey(track) ? map.get(track) + 1 : 1);
            }
        }
        return map;
    }

    public static Map<String, Integer> topArtistsByPlays(JSONArray array) {
        Map<String, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String artist = entry.getArtistName();
            int ms = entry.getMsPlayed();
            if (artist != null && ms >= 30000) {
                map.put(artist, map.containsKey(artist) ? map.get(artist) + 1 : 1);
            }
        }
        return map;
    }

    public static Map<String, Integer> topAlbumsByPlays(JSONArray array) {
        Map<String, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String album = entry.getAlbumName();
            int ms = entry.getMsPlayed();
            if (album != null && ms >= 30000) {
                map.put(album, map.containsKey(album) ? map.get(album) + 1 : 1);
            }
        }
        return map;
    }

    public static Map<String, Integer> skippedTracks(JSONArray array) {
        Map<String, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            boolean skipped = entry.isSkipped();
            String track = entry.getTrackName();
            if (skipped && track != null) {
                map.put(track, map.getOrDefault(track, 0) + 1);
            }
        }
        return map;
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

    public static Map<String, Integer> topArtistsByUniquePlays(JSONArray array) {
        Map<String, Integer> map = new LinkedHashMap<>();
        Set<String> seenTracks = new HashSet<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String artist = entry.getArtistName();
            String track = entry.getTrackName();
            int ms = entry.getMsPlayed();
            if (artist != null && track != null && ms >= 30000 && !seenTracks.contains(track)) {
                seenTracks.add(track);
                map.put(artist, map.getOrDefault(artist, 0) + 1);
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

    public static Integer percentageTimeShuffled(JSONArray array) {
        int tracksOnShuffle = 0;
        int totalTracks = array.length();

        for (int i = 0; i < totalTracks; i++) {
            JSONObject obj = array.getJSONObject(i); // use 'i', not '0'
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            if (entry.isShuffle()) {
                tracksOnShuffle++;
            }
        }

        if (totalTracks == 0) return 0;

        double percentage = ((double) tracksOnShuffle / totalTracks) * 100;
        return (int) Math.round(percentage);
    }

    public static Map<String, Integer> totalPodcastTime(JSONArray array) {
        Map<String, Integer> map = new LinkedHashMap<>();
        int ms = 0;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
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

    public static Map<String, Integer> topPodcastsByPlays(JSONArray array) {
        Map<String, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String podcast = entry.getPodcastName();
            if (podcast != null) {
                map.put(podcast, map.containsKey(podcast) ? map.get(podcast) + 1 : 1);
            }
        }
        return map;
    }

    public static Map<String, String> firstTrackEver(JSONArray array) {
        Map <String, String> map = new HashMap<>();
        String firstTimeStamp = null;
        String firstTrack = null;
        String firstArtist = null;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
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

    public static String artistRoyalties(JSONArray array) {
        float totalStreams = 0;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
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

    public static Map<String, Integer> topDays(JSONArray array) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String timeStamp = entry.getTimestamp();
            int ms = entry.getMsPlayed();

            if(ms >= 30000) {
                ZonedDateTime zdt = ZonedDateTime.parse(timeStamp);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                String readableTimeStamp = zdt.format(formatter);
                map.put(readableTimeStamp, map.containsKey(readableTimeStamp) ? map.get(readableTimeStamp) + 1 : 1);
            }
        }
        return map;
    }



    //    Functions below are used in SpotifyApiController.java

//    Tabs
    public Map<String, Integer> getTopTrackNames() {
        Map<String, Integer> tracksMap = topTracksByPlays(this.cachedData);
        return sortAndSizeMap(tracksMap, 50);
    }

    public Map<String, Integer> getTopArtistsNames() {
        Map<String, Integer> artistsMap = topArtistsByPlays(this.cachedData);
        return sortAndSizeMap(artistsMap, 50);
    }

    public Map<String, Integer> getTopArtistsNamesByTime() {
        Map<String, Integer> artistMap = topTimeListened(this.cachedData);
        return sortAndSizeMap(artistMap, 50);
    }

    public Map<String, Integer> getTopAlbumsMap() {
        Map<String, Integer> albumsMap = topAlbumsByPlays(this.cachedData);
        return sortAndSizeMap(albumsMap, 50);
    }

    public Map<String, Integer> getTopSkippedTracks() {
        Map<String, Integer> skippedMap = skippedTracks(this.cachedData);
        Map<String, Integer> skippedMapSorted = sortAndSizeMap(skippedMap, 50);
        return skippedMapSorted;
    }

    public Map<String, Integer> getTopPodcasts() {
        Map<String, Integer> podcastMap = topPodcastsByPlays(this.cachedData);
        return sortAndSizeMap(podcastMap, 50);
    }

    public Map<String, Integer> getTopDays() {
        Map<String, Integer> daysMap = topDays(this.cachedData);
        return sortAndSizeMap(daysMap, 50);
    }

//    General info section
    public Integer getTotalEntries() {
        return this.cachedData.length();
    }

    public Integer getTotalStreams() {
        return totalStreams(this.cachedData);
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

    public Integer getTotalTracksSkipped() {
        Map<String, Integer> skippedMap = totalSkippedTracks(this.cachedData, 30);
        int res = skippedMap.values().stream().mapToInt(Integer::intValue).sum();
        return res;
    }

    public Map<String, Integer> getTopArtistsByUniquePlays() {
        Map<String, Integer> uniqueArtistMap = topArtistsByUniquePlays(this.cachedData);
        return sortAndSizeMap(uniqueArtistMap, 50);
    }

    public Map<String, Integer> getTotalMusicTime() {
        return totalMusicTime(this.cachedData);
    }

    public Map<String, Integer> getTotalPodcastTime() {
        return totalPodcastTime(this.cachedData);
    }

    public Integer getPercentageTimeShuffled() {
        return percentageTimeShuffled(this.cachedData);
    }

    public Map<String, String> getFirstTrackEver() {
        return firstTrackEver(this.cachedData);
    }

    public String getTotalRoyalties() {
        return artistRoyalties(this.cachedData);
    }

}