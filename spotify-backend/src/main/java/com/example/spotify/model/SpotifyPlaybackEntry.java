package com.example.spotify.model;

public class SpotifyPlaybackEntry {
    private String timestamp;
    private String platform;
    private int msPlayed;
    private String country;
    private String ipAddress;
    private String trackName;
    private String artistName;
    private String albumName;
    private String spotifyTrackUri;
    private String reasonStart;
    private String reasonEnd;
    private boolean shuffle;
    private boolean skipped;
    private boolean offline;
    private boolean incognitoMode;

    // Getters and Setters

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public int getMsPlayed() { return msPlayed; }
    public void setMsPlayed(int msPlayed) { this.msPlayed = msPlayed; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getTrackName() { return trackName; }
    public void setTrackName(String trackName) { this.trackName = trackName; }

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public String getAlbumName() { return albumName; }
    public void setAlbumName(String albumName) { this.albumName = albumName; }

    public String getSpotifyTrackUri() { return spotifyTrackUri; }
    public void setSpotifyTrackUri(String spotifyTrackUri) { this.spotifyTrackUri = spotifyTrackUri; }

    public String getReasonStart() { return reasonStart; }
    public void setReasonStart(String reasonStart) { this.reasonStart = reasonStart; }

    public String getReasonEnd() { return reasonEnd; }
    public void setReasonEnd(String reasonEnd) { this.reasonEnd = reasonEnd; }

    public boolean isShuffle() { return shuffle; }
    public void setShuffle(boolean shuffle) { this.shuffle = shuffle; }

    public boolean isSkipped() { return skipped; }
    public void setSkipped(boolean skipped) { this.skipped = skipped; }

    public boolean isOffline() { return offline; }
    public void setOffline(boolean offline) { this.offline = offline; }

    public boolean isIncognitoMode() { return incognitoMode; }
    public void setIncognitoMode(boolean incognitoMode) { this.incognitoMode = incognitoMode; }
}
