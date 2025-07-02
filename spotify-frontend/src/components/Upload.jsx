import { useState } from 'react';
import axios from 'axios';

export default function Upload({ onUploadComplete }) {
    const [error, setError] = useState("");

    const handleFileChange = async (e) => {
        const files = Array.from(e.target.files);
        const formData = new FormData();

        files.forEach(file => formData.append("files", file));
        try {
            const res = await axios.post("/api/upload", formData, {
                headers: { "Content-Type": "multipart/form-data" }
            });

            onUploadComplete(res.data);
        } catch (err) {
            setError("Failed to upload files.");
            console.error(err);
        }
    };

    return (
        <div style={{ textAlign: "center", marginTop: "4rem" }}>
            <h1>Upload Your Spotify History</h1>
            <p>You should upload the entire folder that you downloaded from the Spotify website.</p>
            <p>It'll be named something like "Spotify Extended Streaming History"</p>
            <input type="file" webkitdirectory="true" directory="true" multiple onChange={handleFileChange} />
            {error && <p style={{ color: 'red' }}>{error}</p>}
        </div>
    );
}
