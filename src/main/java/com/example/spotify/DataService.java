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
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

@Component
public class DataService {

    @Value("${extended.streaming.history.path}")
    private String folderPath;

    @PostConstruct
    public void init() {
        JSONArray data = collectExtendedData();

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
        Map<String, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);

            String artist = entry.getArtistName();
            if (artist != null) {
                int msPlayed = entry.getMsPlayed();
                int minutes = msPlayed / 60000;
                map.put(artist, map.containsKey(artist) ? map.get(artist) + minutes : minutes);
            }
        }
        return map;
    }

    public static Map<String, Integer> totalTimeListened(JSONArray array) {
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
            int minutes = ms / 1000;
            int hours = minutes / 60;
            int days = hours / 24;
            map.put("minutes", minutes);
            map.put("hours", hours);
            map.put("days", days);
        }
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
            if (track != null) {
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
            if (artist != null) {
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
            if (album != null) {
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
            if (artist != null && track != null && !seenTracks.contains(track)) {
                seenTracks.add(track);
                map.put(artist, map.getOrDefault(artist, 0) + 1);
            }
        }
        return map;
    }

    public static JSONArray singleTrackData(JSONArray array, String track) {
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

//    Functions below are used in API Controller
    public Map<String, Integer> getTopTrackNames() {
        JSONArray data = collectExtendedData();
        Map<String, Integer> tracksMap = topTracksByPlays(data);
        Map<String, Integer> tracksMapSorted = sortAndSizeMap(tracksMap, 50);
        return tracksMapSorted;
    }

    public Map<String, Integer> getTopArtistsNames() {
        JSONArray data = collectExtendedData();
        Map<String, Integer> artistsMap = topArtistsByPlays(data);
        Map<String, Integer> artistsMapSorted = sortAndSizeMap(artistsMap, 50);
        return artistsMapSorted;
    }

    public Map<String, Integer> getTopAlbumsMap() {
        JSONArray data = collectExtendedData();
        Map<String, Integer> albumsMap = topAlbumsByPlays(data);
        Map<String, Integer> albumsMapSorted = sortAndSizeMap(albumsMap, 50);
        return albumsMapSorted;
    }

    public Map<String, Integer> getTopSkippedTracks() {
        JSONArray data = collectExtendedData();
        Map<String, Integer> skippedMap = skippedTracks(data);
        Map<String, Integer> skippedMapSorted = sortAndSizeMap(skippedMap, 50);
        return skippedMapSorted;
    }

    public Integer getTotalEntries() {
        JSONArray data = collectExtendedData();
        return data.length();
    }

    public Integer getTotalUniqueEntries() {
        JSONArray data = collectExtendedData();
        Set<String> uniqueTracks = new HashSet<String>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.getJSONObject(i);
            SpotifyPlaybackEntry entry = SpotifyParser.fromJson(obj);
            String track = entry.getTrackName();
            uniqueTracks.add(track);
        }
        return uniqueTracks.size();
    }

    public Integer getTotalTracksSkipped() {
        JSONArray data = collectExtendedData();
        Map<String, Integer> skippedMap = totalSkippedTracks(data, 5);
        int res = skippedMap.values().stream().mapToInt(Integer::intValue).sum();
        return res;
    }

    public Map<String, Integer> getTopArtistsByUniquePlays() {
        JSONArray data = collectExtendedData();
        Map<String, Integer> uniqueArtistMap = topArtistsByUniquePlays(data);
        Map<String, Integer> uniqueArtistMapSorted = sortAndSizeMap(uniqueArtistMap, 50);
        return uniqueArtistMapSorted;
    }

    public Map<String, Integer> getTotalTimeListened() {
        JSONArray data = collectExtendedData();
        return totalTimeListened(data);
    }

}