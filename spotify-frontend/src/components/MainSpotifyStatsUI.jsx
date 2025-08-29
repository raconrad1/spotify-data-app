import { useState, useEffect } from 'react'
import axios from 'axios'
import '../App.css'
import { Box, Tabs, Tab } from '@mui/material'
import { TabContext, TabPanel } from '@mui/lab'
import { styled } from '@mui/material/styles';
import { LineWobble } from 'ldrs/react'
import 'ldrs/react/LineWobble.css';


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

const StyledRow = styled(Box)(({theme}) => ({
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    borderBottom: '1px solid #e0e0e0',
}));

function GeneralStatBox({ children }) {
    return (
        <div style={{
            border: "1px solid #c4c4c4",
            padding: "8px",
            borderRadius: "30px",
            margin: "0 5px 8px 5px",
            flexGrow: "1",
            boxShadow: "0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 8px 0 rgba(0, 0, 0, 0.19)"
        }}>
            {children}
        </div>
    );
}

function GeneralStats({ generalStatsData }) {

    const totalEntriesContent = generalStatsData ? (
        <GeneralStatBox>
            <h4>Total entries</h4>
            <p>{addNumberCommas(generalStatsData.totalEntries)}</p>
        </GeneralStatBox>
    ) : (
        <p>Loading total entries...</p>
    )

    const totalStreamsContent = generalStatsData ? (
        <GeneralStatBox>
            <h4>Total streams</h4>
            <p>{addNumberCommas(generalStatsData.totalStreams)}</p>
        </GeneralStatBox>
    ) : (
        <p>Loading total streams...</p>
    )

    const totalUniqueEntriesContent = generalStatsData ? (
        <GeneralStatBox>
            <h4>Total unique streams</h4>
            <p>{addNumberCommas(generalStatsData.totalUniqueStreams)}</p>
        </GeneralStatBox>
    ) : (
        <p>Loading total unique streams...</p>
    )

    const totalSkipsContent = generalStatsData ? (
        <GeneralStatBox>
            <h4>Total tracks skipped</h4>
            <p>{addNumberCommas(generalStatsData.totalSkippedTracks)}</p>
        </GeneralStatBox>
    ) : (
        <p>Loading total skips...</p>
    )

    const totalMusicTimeContent = generalStatsData?.totalMusicTime ? (
        <GeneralStatBox>
            <p>You've listened to <b>music</b> for {addNumberCommas(generalStatsData.totalMusicTime.minutes)} minutes, which is {addNumberCommas(generalStatsData.totalMusicTime.hours)} hours, or {addNumberCommas(generalStatsData.totalMusicTime.days)} days.</p>
        </GeneralStatBox>
    ) : (
        <p>Loading total time listened to music...</p>
    )

    const totalPodcastTimeContent = generalStatsData?.totalPodcastTime ? (
        <GeneralStatBox>
            <p>You've listened to <b>podcasts</b> for {addNumberCommas(generalStatsData.totalPodcastTime.minutes)} minutes, which is {addNumberCommas(generalStatsData.totalPodcastTime.hours)} hours, or {addNumberCommas(generalStatsData.totalPodcastTime.days)} days.</p>
        </GeneralStatBox>
    ) : (
        <p>Loading total time listened to podcasts...</p>
    )

    const shufflePercentContent = generalStatsData ? (
        <GeneralStatBox>
            <p><b>{generalStatsData.percentageTimeShuffled}%</b> of the time you are listening to music on shuffle.</p></GeneralStatBox>
    ) : (
        <p>Loading shuffle percent...</p>
    )

    const firstTrackEverContent = generalStatsData?.firstTrackEver ? (
        <GeneralStatBox>
            <p>The first track you've ever listened to on Spotify was <b>{generalStatsData.firstTrackEver["track"]} by <em>{generalStatsData.firstTrackEver["artist"]}</em></b>. It was played on {generalStatsData.firstTrackEver["timeStamp"]}.</p>
        </GeneralStatBox>
    ) : (
        <p>Loading first track ever...</p>
    )

    const totalRoyaltiesContent = generalStatsData ? (
        <GeneralStatBox>
            <p>On Spotify, artists earn an average of $0.004 per stream. That means you have contributed approximately <b>${generalStatsData.totalArtistRevenue}</b> to artists through your listening. However, this estimate assumes that artists receive the full amount, which often isn't the case - most labels take a significant share of the streaming revenue.</p>
        </GeneralStatBox>
    ) : (
        <p>Loading total revenue to artists...</p>
    )

    return (
        <div style={{ display: "flex", flexWrap: "wrap", alignContent: "space-around" }}>
            <div style={{ display: "flex", justifyContent: "space-evenly", width: "100%" }}>
                {totalEntriesContent}
                {totalStreamsContent}
                {totalUniqueEntriesContent}
                {totalSkipsContent}
            </div>
            <div style={{ display: "flex", justifyContent: "space-evenly", width: "100%" }}>
                {totalMusicTimeContent}
                {totalPodcastTimeContent}
            </div>
            {shufflePercentContent}
            {firstTrackEverContent}
            {totalRoyaltiesContent}
        </div>
    )
}

function DayStatRow({ index, day, data }) {
    const [expanded, setExpanded] = useState(false);

    return (
        <Box key={day} style={{ padding: "5px 0 5px 0"}}>
            <StyledRow>
                <div style={{
                    display: "flex",
                    margin: "auto",
                    justifyContent: "space-between",
                    width: "100%",
                }}>
                    <span><b>{index + 1}.</b> {day}</span>
                    <span>{addNumberCommas(data.streams)} streams</span>
                    <span>{(data.hours ?? 0).toFixed(1)} hours listened</span>
                    <button size="small" onClick={() => setExpanded(!expanded)}>
                        {expanded ? 'Hide Details' : 'Show Details'}
                    </button>
                </div>
            </StyledRow>
            {expanded && (
                <Box style={{ marginLeft: '24px', marginTop: '8px' }}>
                    {Object.keys(data.topTracks || {}).length === 0 ? (
                        <div style={{ marginTop: '0.5rem' }}><b>No tracks played</b></div>
                    ) : (
                        <div>
                            <div style={{ marginTop: '0.5rem' }}><b>Top Songs</b></div>
                            <ul>
                                {Object.entries(data.topTracks || {}).map(([track, count]) => (
                                    <li key={track}>{track} — {count} stream{count !== 1 ? 's' : ''}</li>
                                ))}
                            </ul>

                            <div style={{ marginTop: '0.5rem' }}><b>Top Artists</b></div>
                            <ul>
                                {Object.entries(data.topArtists || {}).map(([artist, count]) => (
                                    <li key={artist}>{artist} — {count} stream{count !== 1 ? 's' : ''}</li>
                                ))}
                            </ul>
                        </div>
                    )}

                    {Object.keys(data.topPodcasts || {}).length === 0 ? (
                        <div style={{ marginTop: '0.5rem' }}><b>No Podcasts played</b></div>
                    ) : (
                        <div>
                            <ul>
                                <div style={{ marginTop: '0.5rem' }}><b>Top Podcasts</b></div>
                                {Object.entries(data.topPodcasts || {}).map(([podcast, count]) => (
                                    <li key={podcast}>{podcast} — {count} episode{count !== 1 ? 's' : ''}</li>
                                ))}
                            </ul>
                        </div>
                    )}
                </Box>
            )}
        </Box>
    );
}

function YearStatRow({ year, data}) {
    const [expanded, setExpanded] = useState(false);

    return (
        <Box key={year} style={{ padding: "20px 0 20px 0"}}>
            <StyledRow>
                <div><b>{year}</b></div>
                <div style={{
                    display: "flex",
                    margin: "auto",
                    justifyContent: "space-evenly",
                    width: "60%",
                    borderRight: "1px solid rgba(66, 135, 245, 0.5)"
                }}>
                    <span>{addNumberCommas(data.streams)} streams</span>
                    <span>{(data.musicHours ?? 0).toFixed(1)} hours listened</span>
                    <span>{addNumberCommas(data.uniqueStreams)} unique streams</span>
                </div>
                <div style={{
                    display: "flex",
                    margin: "auto",
                    justifyContent: "space-evenly",
                    width: "60%",
                }}>
                    <span>{addNumberCommas(data.podcastPlays)} podcast plays</span>
                    <span>{(data.podcastHours ?? 0).toFixed(1)} hours listened</span>
                </div>
                <div>
                    <button onClick={() => setExpanded(!expanded)}>
                        {expanded ? 'Hide Details' : 'Show Details'}
                    </button>
                </div>
            </StyledRow>
        {expanded && (
            <p>hey this is a test</p>
        )}
        </Box>
    )
}

function DataTabs({ topStatsData, topYearsData, topDaysData }) {
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

    const RowItem = ({ children, position }) => {
        let style = { display: "flex", flex: "1" };

        if (position === "middle") {
            style.flexDirection = "column";
            style.flexGrow = 1;
            style.alignItems = "center";
            style.textAlign = "center";
        }
        if (position === "last") {
            style.justifyContent = "flex-end";
            style.textAlign = "right";
        }

        return <div style={style}>{children}</div>;
    };
    const topTracksContent = topStatsData?.trackStats ? (
        <Box>
            {Object.entries(topStatsData.trackStats)
                .sort((a, b) => b[1].streamCount - a[1].streamCount)
                .map(([trackName, stats], index) => (
                    <StyledRow key={trackName}>
                        <RowItem position="first"><b>{index + 1}.</b></RowItem>
                        <RowItem position="middle">
                            <span>{stats.trackName}</span>
                            <span><i>{stats.artist}</i></span>
                        </RowItem>
                        <RowItem position="last">{addNumberCommas(stats.streamCount)} streams</RowItem>
                    </StyledRow>
                ))}
        </Box>
    ) : (
        <p>Loading tracks...</p>
    );

    const topNoSkipsContent = topStatsData?.trackStats ? (
        <Box>
            {Object.entries(topStatsData.trackStats)
                .filter(([_, data]) => data.skipCount === 0)
                .sort((a, b) => b[1].streamCount - a[1].streamCount)
                .map(([trackName, stats], index) => (
                    <StyledRow key={trackName}>
                        <RowItem position="first"><b>{index + 1}.</b></RowItem>
                        <RowItem position="middle">
                            <span>{stats.trackName}</span>
                            <span><i>{stats.artist}</i></span>
                        </RowItem>
                        <RowItem position="last">{addNumberCommas(stats.streamCount)} streams</RowItem>
                    </StyledRow>
                ))}
        </Box>
    ) : (
        <p>Loading no skips...</p>
    );

    const topArtistContent = topStatsData?.artistStats ? (
        <Box>
            {Object.entries(topStatsData.artistStats)
                .sort((a, b) => b[1].streamCount - a[1].streamCount)
                .map(([artist, stats], index) => (
                    <StyledRow key={artist}>
                        <RowItem position="first"><b>{index + 1}.</b></RowItem>
                        <RowItem position="middle">{artist}</RowItem>
                        <RowItem position="last">{addNumberCommas(stats.streamCount)} streams</RowItem>
                    </StyledRow>
                ))}
        </Box>
    ) : (
        <p>Loading artists...</p>
    );

    const topArtistsUniquePlaysContent = topStatsData?.artistStats ? (
        <Box>
            {Object.entries(topStatsData.artistStats)
                .sort((a, b) => b[1].uniqueStreamCount - a[1].uniqueStreamCount)
                .map(([artist, stats], index) => (
                    <StyledRow>
                        <RowItem position="first"><b>{index + 1}.</b></RowItem>
                        <RowItem position="middle">{artist}</RowItem>
                        <RowItem position="last">{addNumberCommas(stats.uniqueStreamCount)} unique streams</RowItem>
                    </StyledRow>
                ))}
        </Box>
    ) : (
        <p>Loading artists...</p>
    );

    const topAlbumsContent = topStatsData?.albumStats ? (
        <Box>
            {Object.entries(topStatsData.albumStats)
                .sort((a, b) => b[1].streamCount - a[1].streamCount)
                .map(([album, stats], index) => (
                    <StyledRow>
                        <RowItem position="first"><b>{index + 1}.</b></RowItem>
                        <RowItem position="middle">
                            <span>{stats.album}</span>
                            <span><i>{stats.artist}</i></span>
                        </RowItem>
                        <RowItem position="last">{addNumberCommas(stats.streamCount)} streams</RowItem>
                    </StyledRow>
                ))}
        </Box>
    ) : (
        <p>Loading albums...</p>
    );

    const topSkippedContent = topStatsData?.trackStats ? (
        <Box>
            {Object.entries(topStatsData.trackStats)
                .sort((a, b) => b[1].skipCount - a[1].skipCount)
                .map(([trackName, stats], index) => (
                    <StyledRow>
                        <RowItem position="first"><b>{index + 1}.</b></RowItem>
                        <RowItem position="middle">
                            <span>{stats.trackName}</span>
                            <span><i>{stats.artist}</i></span>
                        </RowItem>
                        <RowItem position="last">{addNumberCommas(stats.skipCount)} skips</RowItem>
                    </StyledRow>
                ))}
        </Box>
    ) : (
        <p>Loading skipped tracks...</p>
    );

    const topPodcastsContent = topStatsData?.podcastStats ? (
        <Box>
            {Object.entries(topStatsData.podcastStats)
                .sort((a, b) => b[1] - a[1])
                .map(([podcast, plays], index) => (
                    <StyledRow>
                        <RowItem position="first"><b>{index + 1}.</b></RowItem>
                        <RowItem position="middle">{podcast}</RowItem>
                        <RowItem position="last">{addNumberCommas(plays)} plays</RowItem>
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
                .map(([year, data]) => (
                    <YearStatRow key={year} year={year} data={data}></YearStatRow>
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
                    <Tab value="6" label="No Skip" />
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
                    <TabPanelContent description="These are the albums you've streamed the most, or maybe they include your favorite tracks by this artist.">
                        {topAlbumsContent}
                    </TabPanelContent>
                </TabPanel>

                <TabPanel value="5">
                    <TabPanelContent description="Here are songs that you've skipped the most.">
                        {topSkippedContent}
                    </TabPanelContent>
                </TabPanel>

                <TabPanel value="6">
                    <TabPanelContent description="Your top songs that have zero skips.">
                        {topNoSkipsContent}
                    </TabPanelContent>
                </TabPanel>

                <TabPanel value="7">
                    <TabPanelContent description="Here are your top podcasts and how many episodes you've listened to.">
                        {topPodcastsContent}
                    </TabPanelContent>
                </TabPanel>

                <TabPanel value="8">
                    <TabPanelContent description="Here are some stats for each year that you've used Spotify, starting from the beginning! Music on the left, podcasts on the right.">
                        {topYearsContent}
                    </TabPanelContent>
                </TabPanel>

                <TabPanel value="9">
                    <TabPanelContent description="Days that you've streamed the most music.">
                        {topDaysContent}
                    </TabPanelContent>
                </TabPanel>
            </TabContext>
        </Box>
    );
}

export default function App() {
    const [topStatsData, setTopStatsData] = useState(null)
    const [generalStatsData, setGeneralStatsData] = useState(null)
    const [topYearsData, setTopYearsData] = useState(null)
    const [topDaysData, setTopDaysData] = useState(null)



    useEffect(() => {
        const fetchAllData = async () => {
            try {
                const [
                    topStats,
                    generalStats,
                    topYears,
                    topDays
                ] = await Promise.all([
                    axios.get('/api/top-stats'),
                    axios.get('/api/general-stats'),
                    axios.get('/api/top-years'),
                    axios.get('/api/top-days')
                ])

                setTopStatsData(topStats.data)
                setGeneralStatsData(generalStats.data)
                setTopYearsData(topYears.data)
                setTopDaysData(topDays.data)
            } catch (err) {
                console.error('Failed to fetch data:', err)
            }
        }

        fetchAllData()
    }, [])

    if (!topStatsData || !generalStatsData || !topYearsData || !topDaysData) {
        return (
            <div style={{ textAlign: 'center', marginTop: '50px' }}>
                <h2>Compiling your data...</h2>
                <LineWobble
                    size="80"
                    stroke="5"
                    bgOpacity="0.1"
                    speed="1.75"
                    color="black"
                />
            </div>
        );
    }


    return (
        <>
            <h1>Your Extended Spotify Streaming History</h1>
            <p>Spotify considers a <b>stream</b> as a track that was played for at least 30 seconds.</p>
            <GeneralStats
                generalStatsData={generalStatsData}
            />
            <br/>
            <h2>Top Stats of All Time</h2>
            <DataTabs
                topStatsData={topStatsData}
                topYearsData={topYearsData}
                topDaysData={topDaysData}
            />
            <BackToTopButton></BackToTopButton>
        </>
    )
}