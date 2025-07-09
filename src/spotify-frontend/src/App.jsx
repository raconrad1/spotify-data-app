import { useState } from 'react'
import Upload from './components/Upload.jsx'
import MainSpotifyStatsUI from './components/MainSpotifyStatsUI.jsx'

export default function App() {
    const [uploadComplete, setUploadComplete] = useState(false)

    return (
        uploadComplete ? (
            <MainSpotifyStatsUI />
        ) : (
            <Upload onUploadComplete={() => setUploadComplete(true)} />
        )
    )
}