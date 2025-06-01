import { useState, useEffect } from 'react'
import axios from 'axios'
import './App.css'
import { Box, Tabs, Tab } from '@mui/material'
import { TabContext, TabPanel } from '@mui/lab'

function addNumberCommas(value) {
    const formattedValue = Number(value).toLocaleString('en-us');
    return formattedValue;
}

function GeneralStats({ totalEntriesData, totalUniqueEntriesData }) {
    const totalEntriesContent = totalEntriesData ? (
        <p>Total tracks played: {addNumberCommas(totalEntriesData)}</p>
    ) : (
        <p>Loading total tracks played...</p>
    )

    const totalUniqueEntriesContent = totalUniqueEntriesData ? (
        <p>Total unique tracks played: {addNumberCommas(totalUniqueEntriesData)}</p>
    ) : (
        <p>Loading total unique tracks played...</p>
    )

    return (
        <div>
            {totalEntriesContent}
            {totalUniqueEntriesContent}
        </div>
    )
}

function DataTabs({ topTracksData, topArtistData, topAlbumsData, topSkippedTracksData, topArtistsUniquePlaysData }) {
    const [value, setValue] = useState('1');

    const handleChange = (event, newValue) => {
        setValue(newValue);
    };
    const topTracksContent = topTracksData ? (
        <ul>
            {Object.entries(topTracksData).map(([track, count]) => (
                <li key={track}>{track}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>    ) : (
        <p>Loading tracks...</p>
    );

    const topArtistContent = topArtistData ? (
        <ul>
            {Object.entries(topArtistData).map(([artist, count]) => (
              <li key={artist}>{artist}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading artists...</p>
    );

    const topArtistsUniquePlaysContent = topArtistsUniquePlaysData ? (
        <ul>
            {Object.entries(topArtistsUniquePlaysData).map(([artist, count]) => (
                <li key={artist}>{artist}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading artists...</p>
    );

    const topAlbumsContent = topAlbumsData ? (
        <ul>
            {Object.entries(topAlbumsData).map(([album, count]) => (
                <li key={album}>{album}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading albums...</p>
    );

    const topSkippedContent = topSkippedTracksData ? (
        <ul>
            {Object.entries(topSkippedTracksData).map(([track, skips]) => (
                <li key={track}>{track}: {addNumberCommas(skips)} skips</li>
            ))}
        </ul>
    ) : (
        <p>Loading skipped tracks...</p>
    );

    return (
        <Box sx={{ width: '100%' }}>
            <TabContext value={value}>
            <Tabs
                value={value}
                onChange={handleChange}
                textColor="secondary"
                indicatorColor="secondary"
            >
                <Tab value="1" label="Top Tracks" />
                <Tab value="2" label="Top Artists" />
                <Tab value="3" label="Top Artists (Unique Plays)" />
                <Tab value="4" label="Top Albums" />
                <Tab value="5" label="Top Skipped Tracks" />
            </Tabs>
            <TabPanel value="1">
                These are your top tracks of all time, and how many times they've been played
                {topTracksContent}
            </TabPanel>

            <TabPanel value="2">
                Here are the artists you've listened to the most, and how many times you played a song of theirs
                {topArtistContent}
            </TabPanel>

            <TabPanel value="3">
                Here are your top artists again, but only including unique songs. This is to show the variety of songs that you've heard from a given artist
                {topArtistsUniquePlaysContent}
            </TabPanel>

            <TabPanel value="4">
                These are the albums you've listened to the most, or maybe the songs from this artist that you've listen to the most are on this album
                {topAlbumsContent}
            </TabPanel>

            <TabPanel value="5">
                These are the songs on your playlists that you hate the most... or you've just skipped them the most... is there a correlation there?
                {topSkippedContent}
            </TabPanel>
            </TabContext>
        </Box>
    );
}

export default function App() {
    const [topTracksData, setTopTracksData] = useState(null)
    useEffect(() => {
        axios.get('/api/top-tracks')
            .then(res => setTopTracksData(res.data))
            .catch(err => console.error(err))
    }, [])

    const [topArtistData, setTopArtistData] = useState(null)
    useEffect(() => {
        axios.get('/api/top-artists')
            .then(res => setTopArtistData(res.data))
            .catch(err => console.error(err))
    }, []);

    const [topArtistsUniquePlaysData, setTopArtistsUniquePlaysData] = useState(null)
    useEffect(() => {
        axios.get('/api/top-artists-unique-plays')
            .then(res => setTopArtistsUniquePlaysData(res.data))
            .catch(err => console.error(err))
    }, []);

    const [topAlbumsData, setTopAlbumsData] = useState(null)
    useEffect(() => {
        axios.get('/api/top-albums')
            .then(res => setTopAlbumsData(res.data))
            .catch(err => console.error(err))
    }, []);

    const [topSkippedTracksData, setTopSkippedTracksData] = useState(null)
    useEffect(() => {
        axios.get('/api/top-skipped-tracks')
            .then(res => setTopSkippedTracksData(res.data))
            .catch(err => console.error(err))
    }, []);

    const [totalEntriesData, setTotalEntriesData] = useState(null);
    const [totalUniqueEntriesData, setTotalUniqueEntriesData] = useState(null);
    useEffect(() => {
        axios.get('/api/total-entries')
            .then(res => setTotalEntriesData(res.data))
            .catch(err => console.error(err));

        axios.get('/api/total-unique-entries')
            .then(res => setTotalUniqueEntriesData(res.data))
            .catch(err => console.error(err));
    }, []);

    return (
      <>
          <h1>Your Extended Spotify Streaming History</h1>
          <h2>General stats</h2>
          <GeneralStats
              totalEntriesData={totalEntriesData}
              totalUniqueEntriesData={totalUniqueEntriesData}
          />
          <br/>
          <DataTabs
              topTracksData={topTracksData}
              topArtistData={topArtistData}
              topArtistsUniquePlaysData={topArtistsUniquePlaysData}
              topAlbumsData={topAlbumsData}
              topSkippedTracksData={topSkippedTracksData}
          />
      </>
  )
}