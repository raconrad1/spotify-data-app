package spotify.util;

import spotify.model.SpotifyPlaybackEntry;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class SpotifyTimeStampFormatter {
    public static Map<String, String> cleanFirstTrackEver(SpotifyPlaybackEntry firstEntry) {
        Map<String, String> firstTrackEver = new HashMap<>();
        if (firstEntry == null) {
            firstTrackEver.put("track", "N/A");
            firstTrackEver.put("timeStamp", "N/A");
            firstTrackEver.put("artist", "N/A");
            return firstTrackEver;
        }
        ZonedDateTime zdt = ZonedDateTime.parse(firstEntry.getTimestamp());
        String formatted = zdt.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a"));

        firstTrackEver.put("track", firstEntry.getTrackName());
        firstTrackEver.put("artist", firstEntry.getArtistName());
        firstTrackEver.put("timeStamp", formatted);
        return firstTrackEver;
    }

}
