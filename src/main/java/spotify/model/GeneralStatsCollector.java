package spotify.model;

import spotify.util.SpotifyArtistRevenueFormatter;
import spotify.util.SpotifyTimeMapConverter;
import spotify.util.SpotifyTimeStampFormatter;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GeneralStatsCollector {
    private int totalEntries;
    private int totalStreams;
    private int totalUniqueStreams;
    private int totalSkippedTracks;
    private int percentageTimeShuffled;
    private Map<String, Integer> totalMusicTime;
    private Map<String, Integer> totalPodcastTime;
    private String totalArtistRevenue;
    private Map<String, String> firstTrackEver;

    private int rawMusicTime = 0;
    private int rawPodcastTime = 0;
    private int shuffleCount = 0;
    private Set<String> uniqueTracks = new HashSet<>();
    private SpotifyPlaybackEntry firstEntry = null;
    Set<String> trueSkipCategories = new HashSet<>(Arrays.asList("backbtn", "unknown", "endplay", "fwdbtn"));

    public void processEntry(SpotifyPlaybackEntry entry) {
        if (entry.getTrackName() != null) {
            totalEntries++;
            rawMusicTime += entry.getMsPlayed();

            if (entry.isShuffle()) shuffleCount++;

            if (entry.getMsPlayed() >= 30000) {
                totalStreams++;
                uniqueTracks.add(entry.getTrackName());
            }

            if (entry.getMsPlayed() <= 5000 && (entry.isSkipped() || trueSkipCategories.contains(entry.getReasonEnd()))) {
                totalSkippedTracks++;
            }

            if (firstEntry == null || Instant.parse(entry.getTimestamp()).isBefore(Instant.parse(firstEntry.getTimestamp()))) {
                firstEntry = entry;
            }
        }

        if (entry.getPodcastName() != null) {
            rawPodcastTime += entry.getMsPlayed();
        }
    }

    public void finalizeStats() {
        totalUniqueStreams = uniqueTracks.size();
        percentageTimeShuffled = totalEntries == 0 ? 0 : (int) Math.round((double) shuffleCount / totalEntries * 100);
        totalArtistRevenue = SpotifyArtistRevenueFormatter.cleanArtistRevenue((float) totalStreams);
        totalMusicTime = SpotifyTimeMapConverter.convertMillisToTimeMap(rawMusicTime);
        totalPodcastTime = SpotifyTimeMapConverter.convertMillisToTimeMap(rawPodcastTime);
        firstTrackEver = SpotifyTimeStampFormatter.cleanFirstTrackEver(firstEntry);
    }

    public int getTotalEntries() { return totalEntries; }
    public int getTotalStreams() { return totalStreams; }
    public int getTotalUniqueStreams() { return totalUniqueStreams; }
    public int getTotalSkippedTracks() { return totalSkippedTracks; }
    public int getPercentageTimeShuffled() { return percentageTimeShuffled; }
    public Map<String, Integer> getTotalMusicTime() { return totalMusicTime; }
    public Map<String, Integer> getTotalPodcastTime() { return totalPodcastTime; }
    public String getTotalArtistRevenue() { return totalArtistRevenue; }
    public Map<String, String> getFirstTrackEver() { return firstTrackEver; }

}
