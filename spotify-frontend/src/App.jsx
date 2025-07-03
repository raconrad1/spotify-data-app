import { useState } from 'react'
import Upload from './components/Upload'
import MainSpotifyStatsUI from './components/MainSpotifyStatsUI'

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