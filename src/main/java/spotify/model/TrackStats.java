package spotify.model;

import java.util.ArrayList;
import java.util.List;

public class TrackStats {
    String trackName;
    String artist;
    int streamCount = 0;
    int skipCount = 0;
    String firstPlayedDate = null;
    List<SpotifyPlaybackEntry> playbackHistory = new ArrayList<>();

    public TrackStats(String trackName, String artist, String playedAt, SpotifyPlaybackEntry entry) {
        this.trackName = trackName;
        this.artist = artist;
        this.firstPlayedDate = playedAt;
        this.playbackHistory.add(entry);
    }

    public void addStream(String playedAt, SpotifyPlaybackEntry entry) {
        streamCount++;
        if (firstPlayedDate == null || playedAt.compareTo(firstPlayedDate) < 0) {
            firstPlayedDate = playedAt;
        }
//            playbackHistory.add(entry);
    }

    public void incrementSkip() {
        skipCount++;
    }
    public String getTrackName() { return trackName; }
    public String getArtist() { return artist; }
    public int getStreamCount() { return streamCount; }
    public int getSkipCount() { return skipCount; }
    public String getFirstPlayedDate() { return firstPlayedDate; }
    public List<SpotifyPlaybackEntry> getPlaybackHistory() { return playbackHistory; }
}

