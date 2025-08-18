package spotify.util;

import java.util.HashMap;
import java.util.Map;

public class SpotifyTimeMapConverter {
    public static Map<String, Integer> convertMillisToTimeMap(int ms) {
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

}
