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

function DataTabs({ topTracksData, topArtistData, topAlbumsData, topSkippedTracksData, topArtistsUniquePlaysData, topPodcastsData, topYearsData, topDaysData }) {
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
            {Object.entries(topArtistData)
                .sort((a, b) => b[1] - a[1])
                .map(([artist, count], index) => (
                <li key={artist}><b>{index + 1}</b>. {artist}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading artists...</p>
    );

    const topArtistsUniquePlaysContent = topArtistsUniquePlaysData ? (
        <ul>
            {Object.entries(topArtistsUniquePlaysData)
                .sort((a, b) => b[1] - a[1])
                .map(([artist, count], index) => (
                <li key={artist}><b>{index + 1}</b>. {artist}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading artists...</p>
    );

    const topAlbumsContent = topAlbumsData ? (
        <ul>
            {Object.entries(topAlbumsData)
                .sort((a, b) => b[1] - a[1])
                .map(([album, count], index) => (
                <li key={album}><b>{index + 1}</b>. {album}: {addNumberCommas(count)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading albums...</p>
    );

    const topSkippedContent = topSkippedTracksData ? (
        <ul>
            {Object.entries(topSkippedTracksData)
                .sort((a, b) => b[1] - a[1])
                .map(([track, skips], index) => (
                <li key={track}><b>{index + 1}</b>. {track}: {addNumberCommas(skips)} skips</li>
            ))}
        </ul>
    ) : (
        <p>Loading skipped tracks...</p>
    );

    const topPodcastsContent = topPodcastsData ? (
        <ul>
            {Object.entries(topPodcastsData)
                .sort((a, b) => b[1] - a[1])
                .map(([podcast, plays], index) => (
                <li key={podcast}><b>{index + 1}</b>. {podcast}: {addNumberCommas(plays)} plays</li>
            ))}
        </ul>
    ) : (
        <p>Loading podcasts...</p>
    )

    const topYearsContent = topYearsData ? (
        <ul>
            {Object.entries(topYearsData)
                .sort((a, b) => b[1].year - a[1].year)
                .map(([year, { streams, hours, uniqueStreams }], index) => (
                    <li key={year}><b>{index + 1}</b>. {year}: {addNumberCommas(streams)} streams, {hours.toFixed(1)} hours listened, {addNumberCommas(uniqueStreams)} unique streams</li>
                ))}
        </ul>
    ) : (
        <p>Loading top years...</p>
    )

    const topDaysContent = topDaysData ? (
        <ul>
            {Object.entries(topDaysData)
                .sort((a, b) => b[1].hours - a[1].hours)
                .map(([day, { streams, hours }], index) => (
                <li key={day}><b>{index + 1}</b>. {day}: {addNumberCommas(streams)} plays, {hours.toFixed(1)} hours listened</li>
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
                <Tab value="7" label="Years Most Listened" />
                <Tab value="8" label="Days Most Listened" />
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
                Here are the years that you've streamed the most music, as well as the number of new songs you streamed that year!
                {topYearsContent}
            </TabPanel>

            <TabPanel value="8">
                Here are the days that you've streamed the most music.
                {topDaysContent}
            </TabPanel>
            </TabContext>
        </Box>
    );
}

export default function App() {
        const [topTracksData, setTopTracksData] = useState(null)
        const [topArtistData, setTopArtistData] = useState(null)
        const [topArtistsUniquePlaysData, setTopArtistsUniquePlaysData] = useState(null)
        const [topAlbumsData, setTopAlbumsData] = useState(null)
        const [topSkippedTracksData, setTopSkippedTracksData] = useState(null)
        const [totalEntriesData, setTotalEntriesData] = useState(null)
        const [totalUniqueEntriesData, setTotalUniqueEntriesData] = useState(null)
        const [totalStreamsData, setTotalStreamsData] = useState(null)
        const [totalSkipsData, setTotalSkipsData] = useState(null)
        const [totalMusicTimeData, setTotalMusicTimeData] = useState(null)
        const [totalPodcastTimeData, setTotalPodcastTimeData] = useState(null)
        const [shufflePercentData, setShufflePercentData] = useState(null)
        const [firstTrackEverData, setFirstTrackEverData] = useState(null)
        const [topPodcastsData, setTopPodcastsData] = useState(null)
        const [totalRoyaltiesData, setTotalRoyaltiesData] = useState(null)
        const [topYearsData, setTopYearsData] = useState(null)
        const [topDaysData, setTopDaysData] = useState(null)

        useEffect(() => {
            const fetchAllData = async () => {
                try {
                    const [
                        topTracks,
                        topArtists,
                        topArtistsUnique,
                        topAlbums,
                        topSkipped,
                        totalEntries,
                        totalUniqueEntries,
                        totalStreams,
                        totalSkips,
                        totalMusicTime,
                        totalPodcastTime,
                        shufflePercent,
                        firstTrack,
                        topPodcasts,
                        totalRoyalties,
                        topYears,
                        topDays
                    ] = await Promise.all([
                        axios.get('/api/top-tracks'),
                        axios.get('/api/top-artists'),
                        axios.get('/api/top-artists-unique-plays'),
                        axios.get('/api/top-albums'),
                        axios.get('/api/top-skipped-tracks'),
                        axios.get('/api/total-entries'),
                        axios.get('/api/total-unique-entries'),
                        axios.get('/api/total-streams'),
                        axios.get('/api/total-skipped-tracks'),
                        axios.get('/api/total-music-time'),
                        axios.get('/api/total-podcast-time'),
                        axios.get('/api/percentage-time-shuffled'),
                        axios.get('/api/first-track-ever'),
                        axios.get('/api/top-podcasts'),
                        axios.get('/api/total-royalties'),
                        axios.get('api/top-years'),
                        axios.get('/api/top-days')
                    ])

                    setTopTracksData(topTracks.data)
                    setTopArtistData(topArtists.data)
                    setTopArtistsUniquePlaysData(topArtistsUnique.data)
                    setTopAlbumsData(topAlbums.data)
                    setTopSkippedTracksData(topSkipped.data)
                    setTotalEntriesData(totalEntries.data)
                    setTotalUniqueEntriesData(totalUniqueEntries.data)
                    setTotalStreamsData(totalStreams.data)
                    setTotalSkipsData(totalSkips.data)
                    setTotalMusicTimeData(totalMusicTime.data)
                    setTotalPodcastTimeData(totalPodcastTime.data)
                    setShufflePercentData(shufflePercent.data)
                    setFirstTrackEverData(firstTrack.data)
                    setTopPodcastsData(topPodcasts.data)
                    setTotalRoyaltiesData(totalRoyalties.data)
                    setTopYearsData(topYears.data)
                    setTopDaysData(topDays.data)
                } catch (err) {
                    console.error('Failed to fetch data:', err)
                }
            }

            fetchAllData()
        }, [])

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
              topYearsData={topYearsData}
              topDaysData={topDaysData}
          />
      </>
  )
    }