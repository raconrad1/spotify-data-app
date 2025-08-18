package spotify.model;

public class YearlyStats {

    public int streams = 0;
    public double musicHours = 0;
    public int uniqueStreams = 0;
    public int podcastPlays = 0;
    public double podcastHours = 0;



    public void addPlay(int ms, boolean uniquePlay) {
        streams++;
        musicHours += ms / 1000.0 / 60.0 / 60.0;
        if(uniquePlay) {
            uniqueStreams++;
        }
    }

    public void addPodcastPlay(int ms) {
        podcastPlays++;
        podcastHours += ms / 1000.0 / 60.0 / 60.0;
    }
}

