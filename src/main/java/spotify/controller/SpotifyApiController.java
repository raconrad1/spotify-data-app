package spotify.controller;
import spotify.DataService;
import spotify.StatsAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SpotifyApiController {

    private final DataService dataService;
    private final StatsAggregator statsAggregator;

    @Autowired
    public SpotifyApiController(DataService dataService, StatsAggregator statsAggregator) {
        this.dataService = dataService;
        this.statsAggregator = statsAggregator;
    }

    private void ensureStatsAreLoaded() {
        String folderPath = dataService.getCurrentSessionFolder();
        dataService.generateStatsIfNeeded(folderPath);
    }

    @GetMapping("/all-stats")
    public ResponseEntity<StatsAggregator.CombinedStatsCollector> getStats() {
        ensureStatsAreLoaded();
        return ResponseEntity.ok(statsAggregator.getStats());
    }

    @GetMapping("/top-stats")
    public ResponseEntity<StatsAggregator.TopStatsCollector> getTopStats() {
        ensureStatsAreLoaded();
        return ResponseEntity.ok(statsAggregator.getStats().getTopStats());
    }

    @GetMapping("/general-stats")
    public ResponseEntity<StatsAggregator.GeneralStatsCollector> getGeneralStats() {
        ensureStatsAreLoaded();
        return ResponseEntity.ok(statsAggregator.getStats().getGeneralStats());
    }

    @GetMapping("/top-days")
    public ResponseEntity<Map<String, StatsAggregator.DailyStats>> getTopDays() {
        ensureStatsAreLoaded();
        return ResponseEntity.ok(statsAggregator.getStats().getDailyStats().getDailyStatsMap());
    }

    @GetMapping("/top-years")
    public ResponseEntity<Map<String, StatsAggregator.YearlyStats>> getTopYears() {
        ensureStatsAreLoaded();
        return ResponseEntity.ok(statsAggregator.getStats().getYearlyStats().getYearlyStatsMap());
    }


    @PostMapping("/upload")
    public ResponseEntity<String> handleUpload(@RequestParam("file") MultipartFile zipFile) {
        if (zipFile.isEmpty() || !zipFile.getOriginalFilename().endsWith(".zip")) {
            return ResponseEntity.badRequest().body("Invalid zip file.");
        }

        try {
            Path tempDir = Files.createTempDirectory("spotify");
            File zipPath = Files.createTempFile(tempDir, "uploaded-", ".zip").toFile();
            zipFile.transferTo(zipPath);

            unzip(zipPath, tempDir.toFile());

            dataService.loadSessionFolder(tempDir.toString());

            zipPath.delete();

            return ResponseEntity.ok("Upload and processing complete.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to process upload.");
        }
    }

    private void unzip(File zipFile, File destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                if (entryName.startsWith("__MACOSX") || entryName.startsWith("._")) {
                    continue;
                }

                String safeName = entryName.replace(":", "-").replaceAll("[^\\w\\-./ ]", "").trim();
                File newFile = new File(destDir, safeName).getCanonicalFile();

                if (!newFile.getPath().startsWith(destDir.getCanonicalPath())) {
                    throw new IOException("Entry is outside of target dir: " + entryName);
                }

                if (entry.isDirectory()) {
                    if (!newFile.exists() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory: " + parent);
                    }

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }

                    if (newFile.getName().toLowerCase().endsWith(".zip")) {
                        File nestedDestDir = new File(newFile.getParentFile(), newFile.getName() + "_unzipped");
                        if (!nestedDestDir.exists() && !nestedDestDir.mkdirs()) {
                            throw new IOException("Failed to create directory for nested unzip: " + nestedDestDir);
                        }
                        unzip(newFile, nestedDestDir);

                        if (!newFile.delete()) {
                            System.err.println("Warning: Failed to delete nested zip file " + newFile.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }
}
