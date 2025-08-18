package spotify.util;

import java.text.DecimalFormat;

public class SpotifyArtistRevenueFormatter {
    public static String cleanArtistRevenue(float totalStreamsFloat) {
        DecimalFormat df = new DecimalFormat("#.00");
        double royalties = totalStreamsFloat * 0.004;
        return df.format(royalties);
    }

}
