package spotify.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YearlyStats {

    public int streams = 0;
    public double musicHours = 0;
    public int uniqueStreams = 0;
    public int podcastPlays = 0;
    public double podcastHours = 0;
    public Map<String, List<SpotifyPlaybackEntry>> entriesOfTheYear = new HashMap<>();


    public void addPlay(int ms, boolean uniquePlay, String year, SpotifyPlaybackEntry entry) {
        streams++;
        musicHours += ms / 1000.0 / 60.0 / 60.0;
        if (uniquePlay) {
            uniqueStreams++;
        }
        entriesOfTheYear
                .computeIfAbsent(year, y -> new ArrayList<>())
                .add(entry);
    }

    public void addPodcastPlay(int ms, String year, SpotifyPlaybackEntry entry) {
        podcastPlays++;
        podcastHours += ms / 1000.0 / 60.0 / 60.0;
        entriesOfTheYear
                .computeIfAbsent(year, y -> new ArrayList<>())
                .add(entry);
    }
}

