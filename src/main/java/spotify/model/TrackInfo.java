package spotify.model;

public record TrackInfo(String name, String artist) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrackInfo)) return false;
        TrackInfo that = (TrackInfo) o;
        return name.equals(that.name) && artist.equals(that.artist);
    }

    @Override
    public String toString() {
        return name + " (" + artist + ")";
    }
}

