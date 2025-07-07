package com.example.spotify.controller;
import com.example.spotify.DataService;
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

    @Autowired
    public SpotifyApiController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/track-stats")
    public ResponseEntity<Map<String, DataService.TrackStats>> getTrackStats() {
        String folderPath = dataService.getCurrentSessionFolder();
        Map<String, DataService.TrackStats> stats = dataService.getTrackStatsMap(folderPath);
        return ResponseEntity.ok(stats);
    }

    @GetMapping ("/artist-stats")
    public ResponseEntity<Map<String, DataService.ArtistStats>> getArtistStats() {
        String folderPath = dataService.getCurrentSessionFolder();
        Map<String, DataService.ArtistStats> stats = dataService.getArtistStatsMap(folderPath);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/album-stats")
    public ResponseEntity<Map<String, DataService.AlbumStats>> getAlbumStats() {
        String folderPath = dataService.getCurrentSessionFolder();
        Map<String, DataService.AlbumStats> stats = dataService.getAlbumStatsMap(folderPath);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/general-stats")
    public ResponseEntity<DataService.GeneralStats> getGeneralStats() {
        String folderPath = dataService.getCurrentSessionFolder();
        DataService.GeneralStats stats = dataService.getGeneralStats(folderPath); // renamed method for clarity
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top-podcasts")
    public ResponseEntity<Map<String, Integer>> getTopPodcasts() {
        String folderPath = dataService.getCurrentSessionFolder();
        Map<String, Integer> podcasts = dataService.getTopPodcastsByPlays(folderPath);
        return ResponseEntity.ok(podcasts);
    }

    @GetMapping("/first-track-ever")
    public ResponseEntity<Map<String, String>> getFirstTrackEver() {
        String folderPath = dataService.getCurrentSessionFolder();
        Map<String, String> firstTrack = dataService.getFirstTrackEver(folderPath);
        return ResponseEntity.ok(firstTrack);
    }

    @GetMapping("/top-days")
    public ResponseEntity<Map<String, DataService.DailyStats>> getTopDays() {
        String folderPath = dataService.getCurrentSessionFolder();
        Map<String, DataService.DailyStats> topDays = dataService.getTopDays(folderPath);
        return ResponseEntity.ok(topDays);
    }

    @GetMapping("/top-years")
    public ResponseEntity<Map<String, DataService.YearlyStats>> getTopYears() {
        String folderPath = dataService.getCurrentSessionFolder();
        Map<String, DataService.YearlyStats> topYears = dataService.getTopYears(folderPath);
        return ResponseEntity.ok(topYears);
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

                // Skip macOS metadata entries
                if (entryName.startsWith("__MACOSX") || entryName.startsWith("._")) {
                    continue;
                }

                // Sanitize name to avoid invalid characters
                String safeName = entryName.replace(":", "-").replaceAll("[^\\w\\-./ ]", "").trim();
                File newFile = new File(destDir, safeName).getCanonicalFile();

                // Prevent zip slip
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

                    // Handle nested zip
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
