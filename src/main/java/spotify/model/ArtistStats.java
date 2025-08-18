package spotify.model;

import java.util.ArrayList;
import java.util.List;

public class ArtistStats {
    String artist;
    int streamCount = 0;
    int uniqueStreamCount = 0;
    int skipCount = 0;
    String firstPlayedDate = null;
    List<SpotifyPlaybackEntry> playbackHistory = new ArrayList<>();
    List<String> uniqueStreamsSeen = new ArrayList<>();

    public ArtistStats(String artist, String playedAt, SpotifyPlaybackEntry entry) {
        this.artist = artist;
        this.firstPlayedDate = playedAt;
        this.playbackHistory.add(entry);
    }

    public void addStream(String playedAt, SpotifyPlaybackEntry entry) {
        streamCount++;
        if (firstPlayedDate == null || playedAt.compareTo(firstPlayedDate) < 0) {
            firstPlayedDate = playedAt;
        }
        if (!uniqueStreamsSeen.contains(entry.getTrackName())) {
            uniqueStreamsSeen.add(entry.getTrackName());
            uniqueStreamCount++;
        }
//            playbackHistory.add(entry);
    }

    public void incrementSkip() {
        skipCount++;
    }
    public String getArtist() { return artist; }
    public int getStreamCount() { return streamCount; }
    public int getUniqueStreamCount() { return uniqueStreamCount; }
    public int getSkipCount() { return skipCount; }
    public String getFirstPlayedDate() { return firstPlayedDate; }
    public List<SpotifyPlaybackEntry> getPlaybackHistory() { return playbackHistory; }
    public List<String> getUniqueStreamsSeen() { return uniqueStreamsSeen; }
}

