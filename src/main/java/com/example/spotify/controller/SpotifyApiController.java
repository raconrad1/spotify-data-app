package com.example.spotify.controller;

import com.example.spotify.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

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

    @GetMapping("/top-artists-by-time")
    public Map<String, Integer> getTopArtistsByTime() {
        return dataService.getTopArtistsNamesByTime();
    }

    @GetMapping("/top-albums")
    public Map<String, Integer> getTopAlbums() {
        return dataService.getTopAlbumsMap();
    }

    @GetMapping("/top-skipped-tracks")
    public Map<String, Integer> getTopSkippedTracks() {
        return dataService.getTopSkippedTracks();
    }

    @GetMapping("/top-podcasts")
    public Map<String, Integer> getTopPodcasts() {
        return dataService.getTopPodcasts();
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

    @GetMapping("/top-artists-unique-plays")
    public Map<String, Integer> getTopArtistsUniquePlays() {
        return dataService.getTopArtistsByUniquePlays();
    }

    @GetMapping("/total-skipped-tracks")
    public Integer getTotalSkippedTracks() {
        return dataService.getTotalTracksSkipped();
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
        return dataService.getTotalRoyalties();
    }

    @GetMapping ("/top-days")
    public Map<String, Integer> getTopDays() {
        return dataService.getTopDays();
    }
}
