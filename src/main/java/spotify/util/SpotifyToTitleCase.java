package spotify.util;

public class SpotifyToTitleCase {

    public static String toTitleCase(String str) {
        String part1 = str.substring(0, 1).toUpperCase();
        String part2 = str.substring(1).toLowerCase();
        return part1 + part2;
    }
}
