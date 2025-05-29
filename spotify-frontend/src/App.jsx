import { useState, useEffect } from 'react'
import axios from 'axios'
import './App.css'

function App() {
  const [count, setCount] = useState(0)
    const [data, setData] = useState(null)
    useEffect(() => {
        axios.get('/api/top-artists')
            .then(res => setData(res.data))
            .catch(err => console.error(err))
    }, [])
  return (
    <>
      <h1>Spotify App Data</h1>
      <div className="card">
        <button onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
      </div>
        {data ? (
            <pre>{JSON.stringify(data, null, 2)}</pre>
        ) : (
        <p>Loading...</p>
        )}
    </>
  )
}

export default App
