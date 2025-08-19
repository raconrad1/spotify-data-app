package spotify.model;

import spotify.util.SpotifyTimeMapConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrackStats {
    String trackName;
    String artist;
    String album;
    int streamCount = 0;
    int skipCount = 0;
    int rawTotalTimeListened = 0;
    String firstPlayedDate = null;
    List<SpotifyPlaybackEntry> playbackHistory = new ArrayList<>();


    public TrackStats(String trackName, String artist, String album, String playedAt, SpotifyPlaybackEntry entry) {
        this.trackName = trackName;
        this.artist = artist;
        this.album = album;
        this.firstPlayedDate = playedAt;
//        this.playbackHistory.add(entry);
    }

    public void addStream(String playedAt, SpotifyPlaybackEntry entry) {
        streamCount++;
        rawTotalTimeListened += entry.getMsPlayed();
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
    public String getAlbum() { return album; }
    public int getStreamCount() { return streamCount; }
    public int getSkipCount() { return skipCount; }
    public Map<String, Integer> getTotalTimeListened() { return SpotifyTimeMapConverter.convertMillisToTimeMap(rawTotalTimeListened); }
    public String getFirstPlayedDate() { return firstPlayedDate; }
    public List<SpotifyPlaybackEntry> getPlaybackHistory() { return playbackHistory; }
}

