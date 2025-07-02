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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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
        Map<String, DataService.TrackStats> stats = dataService.getTrackStatsMap();
        return ResponseEntity.ok(stats);
    }

    @GetMapping ("/artist-stats")
    public ResponseEntity<Map<String, DataService.ArtistStats>> getArtistStats() {
        Map<String, DataService.ArtistStats> stats = dataService.getArtistStatsMap();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/album-stats")
    public ResponseEntity<Map<String, DataService.AlbumStats>> getAlbumStats() {
        Map<String, DataService.AlbumStats> stats = dataService.getAlbumStatsMap();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top-podcasts")
    public ResponseEntity<Map<String, Integer>> getTopPodcasts() {
        Map<String, Integer> podcasts = dataService.getTopPodcastsByPlays();
        return ResponseEntity.ok(podcasts);
    }

    @GetMapping("/total-entries")
    public ResponseEntity<Integer> getTotalEntries() {
        Integer count = dataService.getTotalEntries();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total-streams")
    public ResponseEntity<Integer> getTotalStreams() {
        Integer count = dataService.getTotalStreams();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total-unique-entries")
    public ResponseEntity<Integer> getTotalUniqueEntries() {
        Integer count = dataService.getTotalUniqueEntries();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total-skipped-tracks")
    public ResponseEntity<Integer> getTotalSkippedTracks() {
        Integer count = dataService.getTotalSkippedTracks();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total-music-time")
    public ResponseEntity<Map<String, Integer>> getTotalMusicTime() {
        Map<String, Integer> musicTime = dataService.getTotalMusicTime();
        return ResponseEntity.ok(musicTime);
    }

    @GetMapping("/total-podcast-time")
    public ResponseEntity<Map<String, Integer>> getTotalPodcastTime() {
        Map<String, Integer> podcastTime = dataService.getTotalPodcastTime();
        return ResponseEntity.ok(podcastTime);
    }


    @GetMapping("/percentage-time-shuffled")
    public ResponseEntity<Integer> getPercentageTimeShuffled() {
        Integer percentage = dataService.getPercentageTimeShuffled();
        return ResponseEntity.ok(percentage);
    }


    @GetMapping("/first-track-ever")
    public ResponseEntity<Map<String, String>> getFirstTrackEver() {
        Map<String, String> firstTrack = dataService.getFirstTrackEver();
        return ResponseEntity.ok(firstTrack);
    }

    @GetMapping("/total-royalties")
    public ResponseEntity<String> getTotalRoyalties() {
        String royalties = dataService.getTotalArtistRevenue();
        return ResponseEntity.ok(royalties);
    }

    @GetMapping("/top-days")
    public ResponseEntity<Map<String, DataService.DailyStats>> getTopDays() {
        Map<String, DataService.DailyStats> topDays = dataService.getTopDays();
        return ResponseEntity.ok(topDays);
    }


    @GetMapping("/top-years")
    public ResponseEntity<Map<String, DataService.YearlyStats>> getTopYears() {
        Map<String, DataService.YearlyStats> topYears = dataService.getTopYears();
        return ResponseEntity.ok(topYears);
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            String sessionId = UUID.randomUUID().toString(); // or get from cookie
            Path sessionDir = Paths.get(System.getProperty("java.io.tmpdir"), "spotify", sessionId);
            Files.createDirectories(sessionDir);

            for (MultipartFile file : files) {
                Path filePath = sessionDir.resolve(file.getOriginalFilename());
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, file.getBytes());

            }

            dataService.loadSessionFolder(sessionDir.toString());
            System.out.println("Calling loadSessionFolder with: " + sessionDir.toString());


            return ResponseEntity.ok(sessionId);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to upload files.");
        }
    }

}
