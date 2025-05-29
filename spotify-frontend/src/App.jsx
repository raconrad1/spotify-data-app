import { useState, useEffect } from 'react'
import axios from 'axios'
import './App.css'

function App() {
    const [topTracksData, setTopTracksData] = useState(null)
    useEffect(() => {
        axios.get('/api/top-tracks')
            .then(res => setTopTracksData(res.data))
            .catch(err => console.error(err))
    }, [])

    const [topAlbumsData, setTopAlbumsData] = useState(null)
    useEffect(() => {
        axios.get('/api/top-albums')
            .then(res => setTopAlbumsData(res.data))
            .catch(err => console.error(err))
    }, []);

  return (
    <>
      <h1>Spotify App Data</h1>
        <h2>------- Top tracks -------</h2>
        {topTracksData ? (
            <pre>{JSON.stringify(topTracksData, null, 2)}</pre>
        ) : (
        <p>Loading tracks...</p>
        )}
        <h2>------- Top Albums -------</h2>
        {topAlbumsData ? (
            <pre>{JSON.stringify(topAlbumsData, null, 2)}</pre>
        ) : (
            <p>Loading albums...</p>
        )}
    </>
  )
}

export default App
