package spotify.model;

import spotify.StatsAggregator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DailyStats {

    public int streams = 0;
    public double hours = 0;
    public Map<TrackInfo, Integer> topTracks = new LinkedHashMap<>();
    public Map<String, Integer> topArtists = new LinkedHashMap<>();
    public Map<String, Integer> topPodcasts = new LinkedHashMap<>();

    public DailyStats() {}

    void addPlay(SpotifyPlaybackEntry entry) {
        streams++;
        hours += entry.getMsPlayed() / 1000.0 / 60.0 / 60.0;

        String trackName = entry.getTrackName();
        String artistName = entry.getArtistName();
        String podcastName = entry.getPodcastName();

        if (trackName != null && artistName != null) {
            TrackInfo trackInfo = new TrackInfo(trackName, artistName);
            topTracks.merge(trackInfo, 1, Integer::sum);
        }
        if (artistName != null) {
            topArtists.merge(artistName, 1, Integer::sum);
        }
        if (podcastName != null) {
            topPodcasts.merge(podcastName, 1, Integer::sum);
        }
    }

    void finalizeStats() {
        topTracks = topTracks.entrySet().stream()
                .sorted(Map.Entry.<TrackInfo, Integer>comparingByValue().reversed())
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
        topPodcasts = topPodcasts.entrySet().stream()
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
