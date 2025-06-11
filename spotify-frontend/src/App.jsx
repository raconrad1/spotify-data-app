import { useState, useEffect } from 'react'
import axios from 'axios'
import './App.css'
import { Box, Tabs, Tab } from '@mui/material'
import { TabContext, TabPanel } from '@mui/lab'

function addNumberCommas(value) {
    const formattedValue = Number(value).toLocaleString('en-us');
    return formattedValue;
}

function GeneralStats({ totalEntriesData, totalUniqueEntriesData, totalStreamsData, totalSkipsData, totalMusicTimeData, totalPodcastTimeData, shufflePercentData, firstTrackEverData, totalRoyaltiesData }) {
    const totalEntriesContent = totalEntriesData ? (
        <p>Total entries: {addNumberCommas(totalEntriesData)}</p>
    ) : (
        <p>Loading total entries...</p>
    )

    const totalStreamsContent = totalStreamsData ? (
        <p>Total streams: {addNumberCommas(totalStreamsData)}</p>
    ) : (
        <p>Loading total streams...</p>
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
        <p>{shufflePercentData}% of the time you are listening to music on shuffle.</p>
    ) : (
        <p>Loading shuffle percent...</p>
    )

    const firstTrackEverContent = firstTrackEverData ? (
        <p>The first track you've ever listened to on Spotify was {firstTrackEverData["track"]} by {firstTrackEverData["artist"]}. It was played on {firstTrackEverData["timeStamp"]}.</p>
    ) : (
        <p>Loading first track ever...</p>
    )

    const totalRoyaltiesContent = totalRoyaltiesData ? (
        <p>On Spotify, artists earn an average of $0.004 per stream. That means you have contributed approximately <b>${totalRoyaltiesData}</b> to artists through your listening. However, this estimate assumes that artists receive the full amount, which often isn't the case - most labels take a significant share of the streaming revenue.</p>
    ) : (
        <p>Loading total revenue to artists...</p>
    )

    return (
        <div>
            {totalEntriesContent}
            {totalStreamsContent}
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

function DataTabs({ topTracksData, topArtistData, topAlbumsData, topSkippedTracksData, topArtistsUniquePlaysData, topPodcastsData, topDaysData }) {
    const [value, setValue] = useState('1');

    const handleChange = (event, newValue) => {
        setValue(newValue);
    };
    const topTracksContent = topTracksData ? (
        <ul>
            {Object.entries(topTracksData)
                .sort((a, b) => b[1] - a[1])
                .map(([track, count], index) => (
                    <li key={track}><b>{index + 1}</b>. {track}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>    ) : (
        <p>Loading tracks...</p>
    );

    const topArtistContent = topArtistData ? (
        <ul>
            {Object.entries(topArtistData).map(([artist, count], index) => (
                <li key={artist}><b>{index + 1}</b>. {artist}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading artists...</p>
    );

    const topArtistsUniquePlaysContent = topArtistsUniquePlaysData ? (
        <ul>
            {Object.entries(topArtistsUniquePlaysData).map(([artist, count], index) => (
                <li key={artist}><b>{index + 1}</b>. {artist}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading artists...</p>
    );

    const topAlbumsContent = topAlbumsData ? (
        <ul>
            {Object.entries(topAlbumsData).map(([album, count], index) => (
                <li key={album}><b>{index + 1}</b>. {album}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading albums...</p>
    );

    const topSkippedContent = topSkippedTracksData ? (
        <ul>
            {Object.entries(topSkippedTracksData).map(([track, skips], index) => (
                <li key={track}><b>{index + 1}</b>. {track}: {addNumberCommas(skips)} skips</li>
            ))}
        </ul>
    ) : (
        <p>Loading skipped tracks...</p>
    );

    const topPodcastsContent = topPodcastsData ? (
        <ul>
            {Object.entries(topPodcastsData).map(([podcast, plays], index) => (
                <li key={podcast}><b>{index + 1}</b>. {podcast}: {addNumberCommas(plays)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading podcasts...</p>
    )

    const topDaysContent = topDaysData ? (
        <ul>
            {Object.entries(topDaysData).map(([day, plays], index) => (
                <li key={day}><b>{index + 1}</b>. {day}: {addNumberCommas(plays)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading top days...</p>
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
                <Tab value="7" label="Days Most Listened" />
            </Tabs>
            <TabPanel value="1">
                These are your top tracks of all time, and how many times they've been streamed
                {topTracksContent}
            </TabPanel>

            <TabPanel value="2">
                Here are the artists you've listened to the most, and how many times you streamed a song of theirs
                {topArtistContent}
            </TabPanel>

            <TabPanel value="3">
                Here are your top artists again, but only including unique songs. This is to show the variety of songs that you've streamed from a given artist
                {topArtistsUniquePlaysContent}
            </TabPanel>

            <TabPanel value="4">
                These are the songs you've streamed the most, or they include your favorite tracks by this artist.
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

            <TabPanel value="7">
                Here are the days that you've streamed the most music.
                {topDaysContent}
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
    useEffect(() => {
        axios.get('/api/total-entries')
            .then(res => setTotalEntriesData(res.data))
            .catch(err => console.error(err));
    }, []);

    const [totalUniqueEntriesData, setTotalUniqueEntriesData] = useState(null);
    useEffect( () => {
        axios.get('/api/total-unique-entries')
            .then(res => setTotalUniqueEntriesData(res.data))
            .catch(err => console.error(err));
    }, []);

    const [totalStreamsData, setTotalStreamsData] = useState(null);
    useEffect(() => {
        axios.get('/api/total-streams')
            .then(res => setTotalStreamsData(res.data))
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

    const [topDaysData, setTopDaysData] = useState(null);
    useEffect(() => {
        axios.get('/api/top-days')
            .then(res => setTopDaysData(res.data))
            .catch(err => console.error(err));
    })

    return (
      <>
          <h1>Your Extended Spotify Streaming History</h1>
          <h2>General stats</h2>
          <p><b>Important:</b> Spotify considers a <b>stream</b> as a track that was played for 30 seconds or more. Anything else here that is not referred to as a stream could have been played for only a few seconds before being skipped, for example.</p>
          <GeneralStats
              totalEntriesData={totalEntriesData}
              totalStreamsData={totalStreamsData}
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
              topDaysData={topDaysData}
          />
      </>
  )
}