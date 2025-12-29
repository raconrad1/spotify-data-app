package spotify.model;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import spotify.util.SpotifyToTitleCase;

public class YearlyStatsCollector {

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
            String boldMonth = String.valueOf(zdt.getMonth());
            String month = SpotifyToTitleCase.toTitleCase(boldMonth);

            YearlyStats stats = yearlyStatsMap.computeIfAbsent(year, k -> new YearlyStats());
            if(track != null) {
                boolean isUnique = songsSeen.add(track);
                stats.addPlay(ms, isUnique, year, month, entry);
            }
            if(podcast != null) {
                stats.addPodcastPlay(ms, year, month, entry);
            }
        }
    }

    public Map<String, YearlyStats> getYearlyStatsMap() {
        return yearlyStatsMap;
    }
}

