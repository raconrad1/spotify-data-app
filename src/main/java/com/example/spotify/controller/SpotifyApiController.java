package com.example.spotify.controller;

import com.example.spotify.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SpotifyApiController {

    private final DataService dataService;

    @Autowired
    public SpotifyApiController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/top-tracks")
    public Map<String, Integer> getTopTracks() {
        return dataService.getTopTrackNames();
    }

    @GetMapping("/top-artists")
    public Map<String, Integer> getTopArtists() {
        return dataService.getTopArtistsNames();
    }

    @GetMapping("/top-albums")
    public Map<String, Integer> getTopAlbums() {
        return dataService.getTopAlbumsMap();
    }

    @GetMapping("/top-skipped-tracks")
    public Map<String, Integer> getTopSkippedTracks() {
        return dataService.getTopSkippedTracks();
    }

    @GetMapping("/total-entries")
    public Integer getTotalEntries() {
        return dataService.getTotalEntries();
    }

    @GetMapping("/total-unique-entries")
    public Integer getTotalUniqueEntries() {
        return dataService.getTotalUniqueEntries();
    }

    @GetMapping("/top-artists-unique-plays")
    public Map<String, Integer> getTopArtistsUniquePlays() {
        return dataService.getTopArtistsByUniquePlays();
    }

    @GetMapping("/total-skipped-tracks")
    public Integer getTotalSkippedTracks() {
        return dataService.getTotalTracksSkipped();
    }

    @GetMapping("/total-time-listened")
    public Map<String, Integer> getTotalTimeListened() {
        return dataService.getTotalTimeListened();
    }
}
