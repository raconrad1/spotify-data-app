import { useState, useEffect } from 'react'
import axios from 'axios'
import './App.css'
import { Box, Tabs, Tab } from '@mui/material'
import { TabContext, TabPanel } from '@mui/lab'

function DataTabs({ topTracksData, topArtistData, topAlbumsData }) {
    const [value, setValue] = useState('1');

    const handleChange = (event, newValue) => {
        setValue(newValue);
    };
    const topTracksContent = topTracksData ? (
        <ul>
            {Object.entries(topTracksData).map(([track, count]) => (
                <li key={track}>{track}: {count} plays</li>
            ))}
        </ul>    ) : (
        <p>Loading tracks...</p>
    );

    const topArtistContent = topArtistData ? (
        <ul>
            {Object.entries(topArtistData).map(([artist, count]) => (
              <li key={artist}>{artist}: {count} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading artists...</p>
    );

    const topAlbumsContent = topAlbumsData ? (
        <ul>
            {Object.entries(topAlbumsData).map(([album, count]) => (
                <li key={album}>{album}: {count} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading albums...</p>
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
                <Tab value="3" label="Top Albums" />
            </Tabs>
            <TabPanel value="1">{topTracksContent}</TabPanel>
            <TabPanel value="2">{topArtistContent}</TabPanel>
            <TabPanel value="3">{topAlbumsContent}</TabPanel>
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

    const [topAlbumsData, setTopAlbumsData] = useState(null)
    useEffect(() => {
        axios.get('/api/top-albums')
            .then(res => setTopAlbumsData(res.data))
            .catch(err => console.error(err))
    }, []);

  return (
      <>
          <h1>Your Extended Spotify Streaming History</h1>
          <DataTabs
              topTracksData={topTracksData}
              topArtistData={topArtistData}
              topAlbumsData={topAlbumsData}
          />
      </>
  )
}