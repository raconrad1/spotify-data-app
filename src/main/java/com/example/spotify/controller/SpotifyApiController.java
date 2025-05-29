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
    public List<String> getTopTracks() {
        return dataService.getTopTrackNames();
    }

    @GetMapping("/top-artists")
    public List<String> getTopArtists() {
        return dataService.getTopArtistsNames();
    }

    @GetMapping("/top-albums")
    public Map<String, Integer> getTopAlbums() {
        return dataService.getTopAlbumsMap();
    }
}
