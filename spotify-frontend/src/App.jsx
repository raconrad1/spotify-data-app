import { useState, useEffect } from 'react'
import axios from 'axios'
import './App.css'
import { Box, Tabs, Tab, Button } from '@mui/material'
import { TabContext, TabPanel } from '@mui/lab'
import { styled } from '@mui/material/styles';

function addNumberCommas(value) {
    return Number(value).toLocaleString('en-us');
}

const BackToTopButton = () => {
    const [showButton, setShowButton] = useState(false);

    useEffect(() => {
        const handleScroll = () => {
            if (window.scrollY > 2000) {
                setShowButton(true);
            } else {
                setShowButton(false);
            }
        };

        window.addEventListener('scroll', handleScroll);

        return () => {
            window.removeEventListener('scroll', handleScroll);
        };
    }, []);

    const scrollToTop = () => {
        window.scrollTo({
            top: 670,
            behavior: 'smooth'
        });
    };

    return (
        <>
            {showButton && (
                <button
                    onClick={scrollToTop}
                    style={{
                        position: 'fixed',
                        bottom: '20px',
                        right: '20px',
                    }}
                >
                    Back to Top
                </button>
            )}
        </>
    );
};

const StyledRow = styled(Box)(({ theme }) => ({
    display: 'flex',
    justifyContent: 'space-between',
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    borderBottom: '1px solid #e0e0e0',
}));

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

function DayStatRow({ index, day, data }) {
    const [expanded, setExpanded] = useState(false);

    return (
        <Box key={day} sx={{ mb: 2 }}>
            <StyledRow>
                <span><b>{index + 1}.</b> {day}</span>
                <span>{addNumberCommas(data.streams)} streams</span>
                <span>{data.hours.toFixed(1)} hours listened</span>
                <button size="small" onClick={() => setExpanded(!expanded)}>
                    {expanded ? 'Hide Details' : 'Show Details'}
                </button>
            </StyledRow>
            {expanded && (
                <Box sx={{ ml: 3, mt: 1 }}>
                    <div><b>Top Artists:</b></div>
                    <ul>
                        {Object.entries(data.topArtists || {}).map(([artist, count]) => (
                            <li key={artist}>{artist} — {count} stream{count !== 1 ? 's' : ''}</li>
                        ))}
                    </ul>
                    <div style={{ marginTop: '0.5rem' }}><b>Top Tracks:</b></div>
                    <ul>
                        {Object.entries(data.topTracks || {}).map(([track, count]) => (
                            <li key={track}>{track} — {count} stream{count !== 1 ? 's' : ''}</li>
                        ))}
                    </ul>
                </Box>
            )}
        </Box>
    );
}



function DataTabs({ tracksData, artistData, albumData, topPodcastsData, topYearsData, topDaysData }) {
    const [value, setValue] = useState('1');

    const handleChange = (event, newValue) => {
        setValue(newValue);
    };

    const TabPanelContent = ({ description, children }) => (
        <div>
            <div style={{marginBottom: "40px", fontSize: "1.1rem"}}>{description}</div>
            {children}
        </div>
    )

    const topTracksContent = tracksData ? (
        <Box>
            {Object.entries(tracksData)
                .sort((a, b) => b[1].streamCount - a[1].streamCount)
                .map(([trackName, stats], index) => (
                    <StyledRow key={trackName}>
                        <span><b>{index + 1}.</b></span>
                        <div style={{ display: "flex", flexDirection: "column" }}>
                            <span>{trackName}</span>
                            <span><i>{stats.artist}</i></span>
                        </div>
                        <span>{addNumberCommas(stats.streamCount)} streams</span>
                    </StyledRow>
                ))}
        </Box>
    ) : (
        <p>Loading tracks...</p>
    );

    const topNoSkipsContent = tracksData ? (
        <Box>
            {Object.entries(tracksData)
                .filter(([_, data]) => data.skipCount === 0)
                .sort((a, b) => b[1].streamCount - a[1].streamCount)
                .map(([trackName, stats], index) => (
                    <StyledRow key={trackName}>
                        <span><b>{index + 1}.</b></span>
                        <div style={{ display: "flex", flexDirection: "column" }}>
                            <span>{trackName}</span>
                            <span><i>{stats.artist}</i></span>
                        </div>
                        <span>{addNumberCommas(stats.streamCount)} streams</span>
                    </StyledRow>
                ))}
        </Box>
    ) : (
        <p>Loading no skips...</p>
    );


    // Used to sort by stream count AND skip count
    // sort((a, b) => {
    //     if (b[1].streamCount !== a[1].streamCount) {
    //         return b[1].streamCount - a[1].streamCount;
    //     }
    //     return a[1].skipCount - b[1].skipCount;
    // })

    const topArtistContent = artistData ? (
        <Box>
            {Object.entries(artistData)
                .sort((a, b) => b[1].streamCount - a[1].streamCount)
                .map(([artist, stats], index) => (
                    <StyledRow key={artist}>
                        <span><b>{index + 1}.</b></span>
                        <span>{artist}</span>
                        <span>{addNumberCommas(stats.streamCount)} streams</span>
                    </StyledRow>
                ))}
        </Box>
    ) : (
        <p>Loading artists...</p>
    );

    const topArtistsUniquePlaysContent = artistData ? (
        <Box>
            {Object.entries(artistData)
                .sort((a, b) => b[1].uniqueStreamCount - a[1].uniqueStreamCount)
                .map(([artist, stats], index) => (
                    <StyledRow>
                        <span><b>{index + 1}.</b></span>
                        <span>{artist}</span>
                        <span>{addNumberCommas(stats.uniqueStreamCount)} streams</span>
                    </StyledRow>
            ))}
        </Box>
    ) : (
        <p>Loading artists...</p>
    );

    const topAlbumsContent = albumData ? (
        <Box>
            {Object.entries(albumData)
                .sort((a, b) => b[1].streamCount - a[1].streamCount)
                .map(([album, stats], index) => (
                    <StyledRow>
                        <span><b>{index + 1}.</b></span>
                        <div style={{ display: "flex", flexDirection: "column" }}>
                            <span>{album}</span>
                            <span><i>{stats.artist}</i></span>
                        </div>
                        <span>{addNumberCommas(stats.streamCount)} streams</span>
                    </StyledRow>
            ))}
        </Box>
    ) : (
        <p>Loading albums...</p>
    );

    const topSkippedContent = tracksData ? (
        <Box>
            {Object.entries(tracksData)
                .sort((a, b) => b[1].skipCount - a[1].skipCount)
                .map(([trackName, stats], index) => (
                    <StyledRow>
                        <span><b>{index + 1}.</b></span>
                        <div style={{ display: "flex", flexDirection: "column" }}>
                            <span>{trackName}</span>
                            <span><i>{stats.artist}</i></span>
                        </div>
                        <span>{addNumberCommas(stats.skipCount)} skips</span>
                    </StyledRow>
            ))}
        </Box>
    ) : (
        <p>Loading skipped tracks...</p>
    );

    const topPodcastsContent = topPodcastsData ? (
        <Box>
            {Object.entries(topPodcastsData)
                .sort((a, b) => b[1] - a[1])
                .map(([podcast, plays], index) => (
                    <StyledRow>
                        <span><b>{index + 1}.</b></span>
                        <span>{podcast}</span>
                        <span>{addNumberCommas(plays)} plays</span>
                    </StyledRow>
            ))}
        </Box>
    ) : (
        <p>Loading podcasts...</p>
    )

    const topYearsContent = topYearsData ? (
        <Box>
            {Object.entries(topYearsData)
                .sort((a, b) => b[1].year - a[1].year)
                .map(([year, { streams, hours, uniqueStreams }]) => (
                    <StyledRow>
                        <span><b>{year}</b></span>
                        <span>{addNumberCommas(streams)} streams</span>
                        <span>{hours.toFixed(1)} hours listened</span>
                        <span>{addNumberCommas(uniqueStreams)} unique streams</span>
                    </StyledRow>
                ))}
        </Box>
    ) : (
        <p>Loading top years...</p>
    )

    const topDaysContent = topDaysData ? (
        <Box>
            {Object.entries(topDaysData)
                .sort((a, b) => b[1].hours - a[1].hours)
                .map(([day, data], index) => (
                    <DayStatRow key={day} index={index} day={day} data={data} />
                ))}
        </Box>
    ) : (
        <p>Loading top days...</p>
    );


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
                        fontSize: ".77rem",
                        fontWeight: "800",
                    },
                    '& .MuiTabs-flexContainer': {
                        justifyContent: 'center',
                    },
                }}
            >
                <Tab value="1" label="Tracks" />
                <Tab value="2" label="Artists" />
                <Tab value="3" label="Artists (Unique Streams)" />
                <Tab value="4" label="Albums" />
                <Tab value="5" label="Skipped Tracks" />
                <Tab value="6" label="No Skips" />
                <Tab value="7" label="Podcasts" />
                <Tab value="8" label="Yearly Stats" />
                <Tab value="9" label="Days Most Listened" />
            </Tabs>
            <TabPanel value="1">
                <TabPanelContent description="These are your top tracks of all time, and how many times they've been streamed.">
                    {topTracksContent}
                </TabPanelContent>
            </TabPanel>

            <TabPanel value="2">
                <TabPanelContent description="Here are the artists you've listened to the most, and how many times you streamed a song of theirs.">
                    {topArtistContent}
                </TabPanelContent>
            </TabPanel>

            <TabPanel value="3">
                <TabPanelContent description="Here are your top artists again, but only including unique songs. This is to show the variety of songs that you've streamed from a given artist.">
                    {topArtistsUniquePlaysContent}
                </TabPanelContent>
            </TabPanel>

            <TabPanel value="4">
                <TabPanelContent description="These are the songs you've streamed the most, or they include your favorite tracks by this artist.">
                {topAlbumsContent}
                </TabPanelContent>
            </TabPanel>

            <TabPanel value="5">
                <TabPanelContent description="These are the songs on your playlists that you hate the most... or you've just skipped them the most... is there a correlation there?">
                    {topSkippedContent}
                </TabPanelContent>
            </TabPanel>

            <TabPanel value="6">
                <TabPanelContent description="Here are the songs you've listend to the most that you've NEVER skipped.">
                    {topNoSkipsContent}
                </TabPanelContent>
            </TabPanel>

            <TabPanel value="7">
                <TabPanelContent description="Here are your top podcasts and how many episodes you've listened to.">
                    {topPodcastsContent}
                </TabPanelContent>
            </TabPanel>

            <TabPanel value="8">
                <TabPanelContent description="Here are some stats for each year that you've used Spotify, starting from the beginning!">
                    {topYearsContent}
                </TabPanelContent>
            </TabPanel>

            <TabPanel value="9">
                <TabPanelContent description="Here are the days that you've streamed the most music.">
                    {topDaysContent}
                </TabPanelContent>
            </TabPanel>
            </TabContext>
        </Box>
    );
}

export default function App() {
        const [tracksData, setTracksData] = useState(null)
        const [artistData, setArtistData] = useState(null)
        const [albumData, setAlbumData]  = useState(null)
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
                        trackStats,
                        artistStats,
                        albumStats,
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
                        axios.get('/api/track-stats'),
                        axios.get('/api/artist-stats'),
                        axios.get('/api/album-stats'),
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
                        axios.get('/api/top-years'),
                        axios.get('/api/top-days')
                    ])

                    setTracksData(trackStats.data)
                    setArtistData(artistStats.data)
                    setAlbumData(albumStats.data)
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
          <h2>General Stats</h2>
          <p><b>Note:</b> Spotify considers a <b>stream</b> as a track that was played for 30 seconds or more.</p>
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
          <h2>Top Stats of All Time</h2>
          <DataTabs
              tracksData={tracksData}
              artistData={artistData}
              albumData={albumData}
              topPodcastsData={topPodcastsData}
              topYearsData={topYearsData}
              topDaysData={topDaysData}
          />
          <BackToTopButton></BackToTopButton>
      </>
  )
    }