import { useState, useEffect } from 'react'
import axios from 'axios'
import '../App.css'
import { Box, Tabs, Tab } from '@mui/material'
import { TabContext, TabPanel } from '@mui/lab'
import { styled } from '@mui/material/styles';
import { LineWobble } from 'ldrs/react'
import 'ldrs/react/LineWobble.css';
import { motion, AnimatePresence } from "framer-motion";


function addNumberCommas(value) {
    return Number(value).toLocaleString('en-us');
}

function Tooltip({ children, content }) {
    const [visible, setVisible] = useState(false);

    const tooltipVariants = {
        hidden: { opacity: 0, x: -5, y: -5 },
        visible: { opacity: 1, x: 0, y: 0, transition: { duration: 0.2 } },
    };

    return (
        <div
            style={{ position: "relative", display: "inline-block" }}
            onMouseEnter={() => setVisible(true)}
            onMouseLeave={() => setVisible(false)}
        >
            {/* Children with hover effect */}
            <div
                style={{
                    display: "inline-block",
                    cursor: "pointer",
                    transition: "all 0.2s",
                    borderRadius: "4px",
                    padding: "2px 4px",
                    boxShadow: visible ? "0 0 0 1px rgba(0,0,0,0.3)" : "none",
                    backgroundColor: visible ? "rgba(0,0,0,0.05)" : "transparent",
                }}
            >
                {children}
            </div>

            {/* Tooltip */}
            <AnimatePresence>
                {visible && (
                    <motion.div
                        initial="hidden"
                        animate="visible"
                        exit="hidden"
                        variants={tooltipVariants}
                        style={{
                            position: "absolute",
                            top: "50%",
                            left: "100%",
                            marginLeft: "8px",
                            transform: "translateY(-50%)",
                            backgroundColor: "rgba(0,0,0,0.8)",
                            color: "#fff",
                            padding: "6px 10px",
                            borderRadius: "4px",
                            whiteSpace: "nowrap",
                            fontSize: "0.8rem",
                            zIndex: 10,
                        }}
                    >
                        {content}

                        {/* Arrow pointing to children */}
                        <div
                            style={{
                                position: "absolute",
                                top: "50%",
                                left: "-5px",
                                marginTop: "-5px",
                                width: 0,
                                height: 0,
                                borderTop: "5px solid transparent",
                                borderBottom: "5px solid transparent",
                                borderRight: "5px solid rgba(0,0,0,0.8)",
                            }}
                        />
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
}

function formatDate(isoString) {
    const date = new Date(isoString);

    return new Intl.DateTimeFormat("en-US", {
        timeZone: "America/Chicago",
        year: "numeric",
        month: "numeric",
        day: "numeric",
        hour: "numeric",
        minute: "2-digit",
        hour12: true,
    }).format(date);
}

function groupByDay(entries) {
    const grouped = entries.reduce((acc, entry) => {
        const dateObj = new Date(entry.timestamp);

        const options = {
            timeZone: "America/Chicago",
            weekday: "long",
            year: "numeric",
            month: "long",
            day: "numeric",
        };
        const formattedParts = new Intl.DateTimeFormat("en-US", options).formatToParts(dateObj);

        const dayPart = formattedParts.find(p => p.type === "day");
        const day = parseInt(dayPart.value, 10);

        const suffix =
            day >= 11 && day <= 13
                ? "th"
                : ["st", "nd", "rd"][(day % 10) - 1] || "th";

        let formatted = formattedParts
            .map(p => (p.type === "day" ? `${day}${suffix}` : p.value))
            .join("");

        if (!acc[formatted]) {
            acc[formatted] = { entries: [], dayTotalMs: 0, dateObj };
        }
        acc[formatted].entries.push(entry);
        acc[formatted].dayTotalMs += entry.msPlayed || 0;

        return acc;
    }, {});

    return Object.entries(grouped)
        .map(([dayLabel, data]) => ({
            dayLabel,
            ...data,
        }))
        .sort((a, b) => a.dateObj - b.dateObj);
}

function DayEntryItem({ entry }) {
    const [showInfo, setShowInfo] = useState(false);

    return (
        <li
            style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                padding: "8px 0",
                borderBottom: "1px solid #ccc",
                flexDirection: "column"
            }}
        >
            <div style={{ display: "flex", width: "100%", justifyContent: "space-between", alignItems: "center" }}>
                <div style={{ flex: 3, display: "flex", flexDirection: "column", gap: "4px" }}>
                    <span style={{ fontWeight: "bold" }}>
                      <Tooltip content="test">
                        {entry.trackName || entry.podcastEpisodeName}
                      </Tooltip>
                    </span>
                    <span style={{ color: "#555" }}>{entry.artistName || entry.podcastName}</span>
                </div>

                <div style={{ flex: 1, textAlign: "right" }}>
                    <span>{msToTimeListened(entry.msPlayed)}</span>
                </div>

                <div style={{ flex: 2, display: "flex", justifyContent: "flex-end", gap: "10px", alignItems: "center" }}>
                    <span>{formatDate(entry.timestamp)}</span>
                    <button onClick={() => setShowInfo(!showInfo)}>
                        {showInfo ? "Hide Info" : "Entry Info"}
                    </button>
                </div>
            </div>

            {showInfo && (
                <motion.div
                    style={{ marginTop: "8px", fontSize: "0.9rem", color: "#333", textAlign: "left" }}
                    initial={{ opacity: 0, y: -5 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -5 }}
                    transition={{ duration: 0.2 }}
                >
                    {[
                        <p key="1" style={{ textAlign: "center"}}><a href={`${entry.spotifyTrackUri}`}>Play Track</a></p>,
                        <p key="2">Platform: {entry.platform}</p>,
                        <p key="3">Country: {entry.country}</p>,
                        <p key="4">Reason Start: {entry.reasonStart}</p>,
                        <p key="5">Reason End: {entry.reasonEnd}</p>,
                        <p key="6">Shuffle Toggled: {entry.shuffle ? "Yes" : "No"}</p>,
                        <p key="7">Track Skipped: {entry.skipped ? "Yes" : "No"}</p>,
                        <p key="8">Offline Mode Toggled: {entry.offline ? "Yes" : "No"}</p>,
                        <p key="9">Incognito Mode Toggled: {entry.incognitoMode ? "Yes" : "No"}</p>,
                    ].map((item, index) => (
                        <motion.div
                            key={index}
                            initial={{ opacity: 0, x: -10 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ delay: index * 0.1 }}
                        >
                            {item}
                        </motion.div>
                    ))}
                </motion.div>
            )}
        </li>
    );
}

function msToTimeListened(ms) {
    const hours = Math.floor(ms / 3600000);
    const minutes = Math.floor((ms % 3600000) / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);

    let result = "";
    if (hours > 0) result += `${hours}h `;
    if (minutes > 0 || hours > 0) result += `${minutes}m `;
    result += `${seconds}s`;

    return result.trim();
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
        <motion.div
            style={{ display: "flex", flexWrap: "wrap", alignContent: "space-around" }}
            variants={containerVariants}
            initial="hidden"
            animate="show"
        >
            <motion.div
                style={{ display: "flex", justifyContent: "space-evenly", width: "100%" }}
                variants={containerVariants}
            >
                <motion.div style={{ width: "100%" }} variants={bubbleVariants}>{totalEntriesContent}</motion.div>
                <motion.div style={{ width: "100%" }} variants={bubbleVariants}>{totalStreamsContent}</motion.div>
                <motion.div style={{ width: "100%" }} variants={bubbleVariants}>{totalUniqueEntriesContent}</motion.div>
                <motion.div style={{ width: "100%" }} variants={bubbleVariants}>{totalSkipsContent}</motion.div>
            </motion.div>

            <motion.div
                style={{ display: "flex", justifyContent: "space-evenly", width: "100%" }}
                variants={containerVariants}
            >
                <motion.div style={{ width: "100%" }} variants={bubbleVariants}>{totalMusicTimeContent}</motion.div>
                <motion.div style={{ width: "100%" }} variants={bubbleVariants}>{totalPodcastTimeContent}</motion.div>
            </motion.div>

            <motion.div style={{ width: "100%" }} variants={bubbleVariants}>{shufflePercentContent}</motion.div>
            <motion.div style={{ width: "100%" }} variants={bubbleVariants}>{firstTrackEverContent}</motion.div>
            <motion.div style={{ width: "100%" }} variants={bubbleVariants}>{totalRoyaltiesContent}</motion.div>
        </motion.div>
    );
}

const containerVariants = {
    hidden: { opacity: 0 },
    show: {
        opacity: 1,
        transition: {
            staggerChildren: 0.15,
        },
    },
};

const bubbleVariants = {
    hidden: { opacity: 0, y: 20 },
    show: { opacity: 1, y: 0, transition: { duration: 0.4 } },
};

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

function DayEntries({ day, entries, dayTotalMs }) {
    const [expanded, setExpanded] = useState(false);

    const visibleEntries = expanded ? entries : entries.slice(0, 10);
    const hiddenCount = entries.length - visibleEntries.length;

    return (
        <div key={day} style={{ borderBottom: "1px solid gray", }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <h4 style={{ fontSize: "1.9rem" }}>{day}</h4>
                <h1 style={{ fontSize: "1.5rem"}}>{msToTimeListened(dayTotalMs)}</h1>
            </div>
            <ul style={{ listStyle: "none", padding: 0 }}>
                {visibleEntries.map((entry, i) => (
                    <DayEntryItem key={i} entry={entry} />
                ))}
            </ul>

            {entries.length > 10 && (
                <button
                    onClick={() => setExpanded(!expanded)}
                    style={{ margin: "8px auto 8px auto" }}
                >
                    {expanded ? "▲ Show Less" : `▼ Show ${hiddenCount} more`}
                </button>
            )}
        </div>
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
            <div>
                {Object.entries(data.entriesOfTheYear || {}).map(([year, entries]) => (
                    <div key={year}>
                        {groupByDay(entries).map(({ dayLabel, entries, dayTotalMs }) => (
                            <DayEntries
                                key={dayLabel}
                                day={dayLabel}
                                entries={entries}
                                dayTotalMs={dayTotalMs}
                            />
                        ))}

                    </div>
            ))}
        </div>
    )}
    </Box>
)}

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
            <p>Info: Spotify considers a <b>stream</b> as a track that was played for at least 30 seconds.</p>
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