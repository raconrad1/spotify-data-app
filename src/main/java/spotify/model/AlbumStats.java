package spotify.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlbumStats {
    String album;
    Set<String> artist = new HashSet<>();
    int streamCount = 0;
    int skipCount = 0;
    double hours = 0;
    String firstPlayedDate = null;
    List<SpotifyPlaybackEntry> playbackHistory = new ArrayList<>();

    public AlbumStats(String album, Set<String> artist, String playedAt, SpotifyPlaybackEntry entry) {
        this.album = album;
        this.artist = artist;
        this.hours = 0;
        this.firstPlayedDate = playedAt;
        this.playbackHistory.add(entry);
    }

    public void addStream(int ms, String playedAt, SpotifyPlaybackEntry entry) {
        streamCount++;
        if (firstPlayedDate == null || playedAt.compareTo(firstPlayedDate) < 0) {
            firstPlayedDate = playedAt;
        }
        artist.add(entry.getArtistName());
        hours += ms / 1000.0 / 60.0 / 60.0;
//            playbackHistory.add(entry);
    }

    public void incrementSkip() {
        skipCount++;
    }
    public String getAlbum() { return album; }
    public String getArtist() { return String.join(", ", artist); }
    public int getStreamCount() { return streamCount; }
    public double getHours() { return hours; }
    public int getSkipCount() { return skipCount; }
    public String getFirstPlayedDate() { return firstPlayedDate; }
    public List<SpotifyPlaybackEntry> getPlaybackHistory() { return playbackHistory; }
}

