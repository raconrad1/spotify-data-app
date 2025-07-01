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
    public Map<String, DataService.TrackStats> getTrackStats() {
        return dataService.getTrackStatsMap();
    }

    @GetMapping ("/artist-stats")
    public Map<String, DataService.ArtistStats> getArtistStats() {
        return dataService.getArtistStatsMap();
    }

    @GetMapping("/album-stats")
    public Map<String, DataService.AlbumStats> getAlbumStats() {
        return dataService.getAlbumStatsMap();
    }

    @GetMapping("/top-podcasts")
    public Map<String, Integer> getTopPodcasts() {
        return dataService.getTopPodcastsByPlays();
    }

    @GetMapping("/total-entries")
    public Integer getTotalEntries() {
        return dataService.getTotalEntries();
    }

    @GetMapping("/total-streams")
    public Integer getTotalStreams() {
        return dataService.getTotalStreams();
    }

    @GetMapping("/total-unique-entries")
    public Integer getTotalUniqueEntries() {
        return dataService.getTotalUniqueEntries();
    }

    @GetMapping("/total-skipped-tracks")
    public Integer getTotalSkippedTracks() {
        return dataService.getTotalSkippedTracks();
    }

    @GetMapping("/total-music-time")
    public Map<String, Integer> getTotalMusicTime() {
        return dataService.getTotalMusicTime();
    }

    @GetMapping("/total-podcast-time")
    public Map<String, Integer> getTotalPodcastTime() {
        return dataService.getTotalPodcastTime();
    }

    @GetMapping("/percentage-time-shuffled")
    public Integer getPercentageTimeShuffled() {
        return dataService.getPercentageTimeShuffled();
    }

    @GetMapping("/first-track-ever")
    public Map<String, String> getFirstTrackEver() {
        return dataService.getFirstTrackEver();
    }

    @GetMapping ("/total-royalties")
    public String getTotalRoyalties() {
        return dataService.getTotalArtistRevenue();
    }

    @GetMapping ("/top-days")
    public Map<String, DataService.DailyStats> getTopDays() {
        return dataService.getTopDays();
    }

    @GetMapping ("/top-years")
    public Map<String, DataService.YearlyStats> getTopYears() {
        return dataService.getTopYears();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            String sessionId = UUID.randomUUID().toString(); // or get from cookie
            Path sessionDir = Paths.get(System.getProperty("java.io.tmpdir"), "spotify", sessionId);
            Files.createDirectories(sessionDir);

            for (MultipartFile file : files) {
                Path filePath = sessionDir.resolve(file.getOriginalFilename());
                Files.write(filePath, file.getBytes());
            }

            dataService.loadSessionFolder(sessionDir.toString()); // new method you'll add below

            return ResponseEntity.ok(sessionId);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload files.");
        }
    }

}
