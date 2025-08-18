package spotify.model;


import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class DailyStatsCollector {
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

