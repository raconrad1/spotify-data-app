package spotify;

import org.springframework.stereotype.Component;
import spotify.model.SpotifyPlaybackEntry;
import spotify.util.SpotifyEntryParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

@Component
public class DataService {

    private String currentSessionFolder;
    private String lastParsedFolder = null;

    public void loadSessionFolder(String folderPath) {
        this.currentSessionFolder = folderPath;
    }

    public String getCurrentSessionFolder() {
        return this.currentSessionFolder;
    }

    public synchronized void generateStatsIfNeeded(String folderPath) {
        if (StatsAggregator.cachedStats == null || !folderPath.equals(lastParsedFolder)) {
            System.out.println("Parsing new stats for: " + folderPath);
            StatsAggregator.cachedStats = StatsAggregator.computeStats(folderPath);
            lastParsedFolder = folderPath;
        } else {
            System.out.println("Using cached stats for: " + folderPath);
        }
    }

    public static void processSessionFolder(String folderPath, List<Consumer<SpotifyPlaybackEntry>> entryConsumers) {
        if (folderPath == null) {
            throw new IllegalArgumentException("folderPath is null");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();

        try {
            Files.walk(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> parseFile(path, factory, mapper, entryConsumers));
        } catch (IOException e) {
            System.err.println("Error walking folder: " + e.getMessage());
        }
    }

    private static void parseFile(Path path, JsonFactory factory, ObjectMapper mapper, List<Consumer<SpotifyPlaybackEntry>> entryConsumers) {
        String fileName = path.getFileName().toString().toLowerCase();
        if (!fileName.endsWith(".json") || fileName.startsWith("._")) return;

        System.out.println("Parsing: " + fileName);

        try (InputStream in = Files.newInputStream(path);
             JsonParser parser = factory.createParser(in)) {

            if (parser.nextToken() != JsonToken.START_ARRAY) {
                System.err.println("Expected JSON array in file: " + fileName);
                return;
            }

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                ObjectNode node = mapper.readTree(parser);
                SpotifyPlaybackEntry entry = SpotifyEntryParser.fromJson(new org.json.JSONObject(node.toString()));
                for (Consumer<SpotifyPlaybackEntry> consumer : entryConsumers) {
                    consumer.accept(entry);
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to parse " + fileName + ": " + e.getMessage());
        }
    }
}