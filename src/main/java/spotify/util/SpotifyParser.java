package spotify.util;

import spotify.model.SpotifyPlaybackEntry;
import org.json.JSONObject;

public class SpotifyParser {

    public static SpotifyPlaybackEntry fromJson(JSONObject json) {
        SpotifyPlaybackEntry entry = new SpotifyPlaybackEntry();

        entry.setTimestamp(json.optString("ts", null));
        entry.setPlatform(json.optString("platform", null));
        entry.setMsPlayed(json.optInt("ms_played", 0));
        entry.setCountry(json.optString("conn_country", null));
        entry.setIpAddress(json.optString("ip_addr", null));
        entry.setTrackName(json.optString("master_metadata_track_name", null));
        entry.setArtistName(json.optString("master_metadata_album_artist_name", null));
        entry.setAlbumName(json.optString("master_metadata_album_album_name", null));
        entry.setSpotifyTrackUri(json.optString("spotify_track_uri", null));
        entry.setReasonStart(json.optString("reason_start", null));
        entry.setReasonEnd(json.optString("reason_end", null));
        entry.setShuffle(json.optBoolean("shuffle", false));
        entry.setSkipped(json.optBoolean("skipped", false));
        entry.setOffline(json.optBoolean("offline", false));
        entry.setIncognitoMode(json.optBoolean("incognito_mode", false));
        entry.setPodcastName(json.optString("episode_show_name", null));
        entry.setPodcastEpisodeName(json.optString("episode_name", null));
        return entry;
    }
}
