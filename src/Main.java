import org.json.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;



public class Main {
    public static void main(String[] args) {
        JSONArray array = collectData();

//        Map<String, Integer> top = topTimeListened(array, 10);
//        prettyPrintMap(top);
//        System.out.println("---------------------------------------");
        Map<String, Integer> top1 = getTopArtists(array, 10);
        prettyPrintMap(top1);
        System.out.println("---------------------------------------");
//        System.out.println(totalSkipped(array, 5));
        Map<String, Integer> top = topSkippedTracks(array, 10, 30);
        prettyPrintMap(top);
    }

    private static JSONArray collectData() {
        JSONArray res = new JSONArray();
        try {
            for (int i = 0; i <= 2; i++){
                String filePath = "spotify-data/StreamingHistory_music_" + i + ".json";
                String content = new String(Files.readAllBytes(Paths.get(filePath)));
                JSONArray jsonArray = new JSONArray(content);

                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject entry = jsonArray.getJSONObject(j);
                    res.put(entry);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        } catch (JSONException e) {
            System.out.println("Invalid JSON: " + e.getMessage());
        }
        return res;
    }

//    This doesn't work. I guess it's just another way to see who your top listened is
    public static Integer totalSkipped(JSONArray array, int seconds) {
        int count = 0;
        seconds = seconds * 1000;
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.getJSONObject(i);
            int msPlayed = entry.getInt("msPlayed");
            if(msPlayed <= seconds) {
                count++;
            }
        }
        return count;
    }

    public static Map<String, Integer> topSkippedTracks(JSONArray array, int size, int seconds) {
        Map<String, Integer> map = new LinkedHashMap<>();
        seconds = seconds * 1000;
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.getJSONObject(i);
            String artist = entry.getString("artistName");
            int msPlayed = entry.getInt("msPlayed");
            if(msPlayed <= seconds) {
                map.put(artist, map.getOrDefault(artist, 0) + 1);
            }
        }
        sortAndSizeMap(map, size);
        return map;
    }

    public static Map<String, Integer> topTimeListened(JSONArray array, int size) {
        Map<String, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.getJSONObject(i);
            String artist = entry.getString("artistName");
            int msPlayed = entry.getInt("msPlayed");
            int minutes = msPlayed / 60000;
            map.put(artist, map.containsKey(artist) ? map.get(artist) + minutes : minutes);
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
            String track = entry.getString("trackName");
            map.put(track, map.containsKey(track) ? map.get(track) + 1 : 1);
        }
        sortAndSizeMap(map, size);
        return map;
    }

    public static Map<String, Integer> getTopArtists(JSONArray array, int size) {
        Map<String, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.getJSONObject(i);
            String artist = entry.getString("artistName");
            map.put(artist, map.containsKey(artist) ? map.get(artist) + 1 : 1);
        }
        sortAndSizeMap(map, size);
        return map;
    }
}


// Type ./run.sh in terminal to run
// View run.sh to see how that works