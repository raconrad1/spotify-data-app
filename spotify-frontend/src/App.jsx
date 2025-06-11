import { useState, useEffect } from 'react'
import axios from 'axios'
import './App.css'
import { Box, Tabs, Tab } from '@mui/material'
import { TabContext, TabPanel } from '@mui/lab'

function addNumberCommas(value) {
    const formattedValue = Number(value).toLocaleString('en-us');
    return formattedValue;
}

function GeneralStats({ totalEntriesData, totalUniqueEntriesData, totalSkipsData, totalMusicTimeData, totalPodcastTimeData, shufflePercentData, firstTrackEverData, totalRoyaltiesData }) {
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

    const totalSkipsContent = totalSkipsData ? (
        <p>Total tracks skipped: {addNumberCommas(totalSkipsData)}</p>
    ) : (
        <p>Loading total skips...</p>
    )

    const totalMusicTimeContent = totalMusicTimeData ? (
        <p>You've listened to music for {addNumberCommas(totalMusicTimeData["minutes"])} minutes, which is {addNumberCommas(totalMusicTimeData["hours"])} hours, or {addNumberCommas(totalMusicTimeData["days"])} days.</p>
    ) : (
        <p>Loading total time listened to music...</p>
    )

    const totalPodcastTimeContent = totalPodcastTimeData ? (
        <p>You've listened to podcasts for {addNumberCommas(totalPodcastTimeData["minutes"])} minutes, which is {addNumberCommas(totalPodcastTimeData["hours"])} hours, or {addNumberCommas(totalPodcastTimeData["days"])} days.</p>
    ) : (
        <p>Loading total time listened to podcasts...</p>
    )

    const shufflePercentContent = shufflePercentData ? (
        <p>You have listened to music on shuffle {shufflePercentData}% of the time.</p>
    ) : (
        <p>Loading shuffle percent...</p>
    )

    const firstTrackEverContent = firstTrackEverData ? (
        <p>The first track you've ever listened to on Spotify was {firstTrackEverData["track"]} by {firstTrackEverData["artist"]}. It was played on {firstTrackEverData["timeStamp"]}.</p>
    ) : (
        <p>Loading first track ever...</p>
    )

    const totalRoyaltiesContent = totalRoyaltiesData ? (
        <p>On Spotify, a track counts as a stream if it's played for at least 30 seconds. Artists earn an average of $0.004 per stream. That means you have contributed approximately <b>${totalRoyaltiesData}</b> to artists through your listening. However, this estimate assumes that artists receive the full amount, which often isn't the case - most labels take a significant share of the streaming revenue.</p>
    ) : (
        <p>Loading total royalties to artists...</p>
    )

    return (
        <div>
            {totalEntriesContent}
            {totalUniqueEntriesContent}
            {totalSkipsContent}
            {totalMusicTimeContent}
            {totalPodcastTimeContent}
            {shufflePercentContent}
            {firstTrackEverContent}
            {totalRoyaltiesContent}
        </div>
    )
}

function DataTabs({ topTracksData, topArtistData, topAlbumsData, topSkippedTracksData, topArtistsUniquePlaysData, topPodcastsData }) {
    const [value, setValue] = useState('1');

    const handleChange = (event, newValue) => {
        setValue(newValue);
    };
    const topTracksContent = topTracksData ? (
        <ul>
            {Object.entries(topTracksData)
                .sort((a, b) => b[1] - a[1])
                .map(([track, count], index) => (
                <li key={track}>{index + 1}. {track}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>    ) : (
        <p>Loading tracks...</p>
    );

    const topArtistContent = topArtistData ? (
        <ul>
            {Object.entries(topArtistData).map(([artist, count], index) => (
              <li key={artist}>{index + 1}. {artist}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading artists...</p>
    );

    const topArtistsUniquePlaysContent = topArtistsUniquePlaysData ? (
        <ul>
            {Object.entries(topArtistsUniquePlaysData).map(([artist, count], index) => (
                <li key={artist}>{index + 1}. {artist}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading artists...</p>
    );

    const topAlbumsContent = topAlbumsData ? (
        <ul>
            {Object.entries(topAlbumsData).map(([album, count], index) => (
                <li key={album}>{index + 1}. {album}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading albums...</p>
    );

    const topSkippedContent = topSkippedTracksData ? (
        <ul>
            {Object.entries(topSkippedTracksData).map(([track, skips], index) => (
                <li key={track}>{index + 1}. {track}: {addNumberCommas(skips)} skips</li>
            ))}
        </ul>
    ) : (
        <p>Loading skipped tracks...</p>
    );

    const topPodcastsContent = topPodcastsData ? (
        <ul>
            {Object.entries(topPodcastsData).map(([podcast, plays], index) => (
                <li key={podcast}>{index + 1}. {podcast}: {addNumberCommas(plays)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading podcasts...</p>
    )

    return (
        <Box sx={{ width: '100%' }}>
            <TabContext value={value}>
            <Tabs
                value={value}
                onChange={handleChange}
                textColor="secondary"
                indicatorColor="secondary"
                centered
                sx={{
                    mb: 2,
                    '& .MuiTab-root': {
                        minWidth: '120px',
                        padding: '12px 16px',
                    },
                    '& .MuiTabs-flexContainer': {
                        justifyContent: 'center',
                    },
                }}
            >
                <Tab value="1" label="Top Tracks" />
                <Tab value="2" label="Top Artists" />
                <Tab value="3" label="Top Artists (Unique Plays)" />
                <Tab value="4" label="Top Albums" />
                <Tab value="5" label="Top Skipped Tracks" />
                <Tab value="6" label="Top Podcasts" />
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

            <TabPanel value="6">
                Here are your top podcasts and how many episodes you've listened to
                {topPodcastsContent}
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

    const [totalSkipsData, setTotalSkipsData] = useState(null);
    useEffect(() => {
        axios.get('/api/total-skipped-tracks')
            .then(res => setTotalSkipsData(res.data))
            .catch(err => console.error(err));
    }, []);

    const [totalMusicTimeData, setTotalMusicTimeData] = useState(null);
    useEffect(() => {
        axios.get('/api/total-music-time')
            .then(res => setTotalMusicTimeData(res.data))
            .catch(err => console.error(err));
    }, []);

    const [totalPodcastTimeData, setTotalPodcastTimeData] = useState(null);
    useEffect(() => {
        axios.get('/api/total-podcast-time')
            .then(res => setTotalPodcastTimeData(res.data))
            .catch(err => console.error(err));
    }, []);

    const [shufflePercentData, setShufflePercentData] = useState(null);
    useEffect(() => {
        axios.get('/api/percentage-time-shuffled')
            .then(res => setShufflePercentData(res.data))
            .catch(err => console.error(err));
    }, []);

    const [firstTrackEverData, setFirstTrackEverData] = useState(null);
    useEffect(() => {
        axios.get('/api/first-track-ever')
            .then(res => setFirstTrackEverData(res.data))
            .catch(err => console.error(err));
    })

    const [topPodcastsData, setTopPodcastsData] = useState(null);
    useEffect(() => {
        axios.get('/api/top-podcasts')
            .then(res => setTopPodcastsData(res.data))
            .catch(err => console.error(err));
    })

    const [totalRoyaltiesData, setTotalRoyaltiesData] = useState(null);
    useEffect(() => {
        axios.get('/api/total-royalties')
            .then(res => setTotalRoyaltiesData(res.data))
            .catch(err => console.error(err));
    })

    return (
      <>
          <h1>Your Extended Spotify Streaming History</h1>
          <h2>General stats</h2>
          <GeneralStats
              totalEntriesData={totalEntriesData}
              totalUniqueEntriesData={totalUniqueEntriesData}
              totalSkipsData={totalSkipsData}
              totalMusicTimeData={totalMusicTimeData}
              totalPodcastTimeData={totalPodcastTimeData}
              shufflePercentData={shufflePercentData}
              firstTrackEverData={firstTrackEverData}
              totalRoyaltiesData={totalRoyaltiesData}
          />
          <br/>
          <h2>Other fun stuff</h2>
          <DataTabs
              topTracksData={topTracksData}
              topArtistData={topArtistData}
              topArtistsUniquePlaysData={topArtistsUniquePlaysData}
              topAlbumsData={topAlbumsData}
              topSkippedTracksData={topSkippedTracksData}
              topPodcastsData={topPodcastsData}
          />
      </>
  )
}