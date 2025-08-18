package spotify.model;


import java.util.*;

public class TopStatsCollector {
    Map<String, TrackStats> trackStatsMap = new LinkedHashMap<>();
    Map<String, ArtistStats> artistStatsMap = new LinkedHashMap<>();
    Map<String, AlbumStats> albumStatsMap = new LinkedHashMap<>();
    Map<String, Integer> podcastStatsMap = new LinkedHashMap<>();


    public void processEntry(SpotifyPlaybackEntry entry) {
        String track = entry.getTrackName();
        String artist = entry.getArtistName();
        String album = entry.getAlbumName();
        String playedAt = entry.getTimestamp();
        int ms = entry.getMsPlayed();
        String podcast = entry.getPodcastName();
        Set<String> trueSkipCategories = new HashSet<>(Arrays.asList("backbtn", "unknown", "endplay", "fwdbtn"));


        if (track != null || artist != null) {

            // TrackStats
            String trackKey = track + " - " + artist;
            trackStatsMap.putIfAbsent(trackKey, new TrackStats(track, artist, playedAt, entry));
            TrackStats trackStats = trackStatsMap.get(trackKey);
            if (ms >= 30000) {
                trackStats.addStream(playedAt, entry);
            }
            if (entry.getMsPlayed() <= 5000 && (entry.isSkipped() || trueSkipCategories.contains(entry.getReasonEnd()))) {
                trackStats.incrementSkip();
            }

            // ArtistStats
            artistStatsMap.putIfAbsent(artist, new ArtistStats(artist, playedAt, entry));
            ArtistStats artistStats = artistStatsMap.get(artist);
            if (ms >= 30000) {
                artistStats.addStream(playedAt, entry);
            }
            if (entry.getMsPlayed() <= 5000 && (entry.isSkipped() || trueSkipCategories.contains(entry.getReasonEnd()))) {
                artistStats.incrementSkip();
            }

            // AlbumStats
            Set<String> artistSet = new HashSet<>();
            albumStatsMap.putIfAbsent(album, new AlbumStats(album, artistSet, playedAt, entry));
            AlbumStats albumStats = albumStatsMap.get(album);
            if (ms >= 30000) {
                albumStats.addStream(ms, playedAt, entry);
            }
            if (entry.getMsPlayed() <= 5000 && (entry.isSkipped() || trueSkipCategories.contains(entry.getReasonEnd()))) {
                albumStats.incrementSkip();
            }

        } else if (podcast != null && ms > 5000) {
            podcastStatsMap.put(podcast, podcastStatsMap.containsKey(podcast) ? podcastStatsMap.get(podcast) + 1 : 1);
        }
    }

    public Map<String, TrackStats> getTrackStats() { return trackStatsMap; }
    public Map<String, ArtistStats> getArtistStats() { return artistStatsMap; }
    public Map<String, AlbumStats> getAlbumStats() { return albumStatsMap; }
    public Map<String, Integer> getPodcastStats() { return podcastStatsMap; }
}

