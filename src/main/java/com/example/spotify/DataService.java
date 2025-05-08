package com.example.spotify;

import jakarta.annotation.PostConstruct;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.print.DocFlavor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.LinkedHashMap;

@Component
public class DataService {

    @Value("${extended.streaming.history.path}")
    private String folderPath;

    @PostConstruct
    public void init() {
        JSONArray data = collectExtendedData();
        System.out.println("Total JSON entries: " + data.length());

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
    public static Map<String, Integer> topSkippedTracks(JSONArray array, int size, int seconds) {
        Map<String, Integer> map = new LinkedHashMap<>();
        seconds = seconds * 1000;
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.getJSONObject(i);
            String artist = entry.optString("master_metadata_album_artist_name", null);
            if (artist != null) {
                int msPlayed = entry.getInt("ms_played");
                if(msPlayed <= seconds) {
                    map.put(artist, map.getOrDefault(artist, 0) + 1);
            }
            }
        }
        sortAndSizeMap(map, size);
        return map;
    }

    public static Map<String, Integer> topTimeListened(JSONArray array, int size) {
        Map<String, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.getJSONObject(i);
            String artist = entry.optString("master_metadata_album_artist_name", null);
            if (artist != null) {
                int msPlayed = entry.getInt("ms_played");
                int minutes = msPlayed / 60000;
                map.put(artist, map.containsKey(artist) ? map.get(artist) + minutes : minutes);
            }
        }
        sortAndSizeMap(map, size);
        return map;
    }

    public static void sortAndSizeMap(Map<String, Integer> map, int size) {
        Map<String, Integer> limitedMap = map.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // sort descending
                .limit(size)
                .collect(
                        LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        LinkedHashMap::putAll
                );
        map.clear();
        map.putAll(limitedMap);
    }

    public static void prettyPrintMap(Map<String, Integer> map) {
        map.forEach((k, v) -> System.out.println(k + ": " + v));
    }

    public static Map<String, Integer> getTopTracks(JSONArray array, int size) {
        Map<String, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.getJSONObject(i);
            String track = entry.optString("master_metadata_track_name", null);
            if (track != null) {
                map.put(track, map.containsKey(track) ? map.get(track) + 1 : 1);
            }
        }
        sortAndSizeMap(map, size);
        return map;
    }

    public static Map<String, Integer> getTopArtists(JSONArray array, int size) {
        Map<String, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.getJSONObject(i);
            String artist = entry.optString("master_metadata_album_artist_name", null);
            if (artist != null) {
                map.put(artist, map.containsKey(artist) ? map.get(artist) + 1 : 1);
            }
        }
        sortAndSizeMap(map, size);
        return map;
    }
}


// mvn spring-boot:run from root directory to run