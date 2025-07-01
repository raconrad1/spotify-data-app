import { useState } from 'react';
import axios from 'axios';

export default function Upload({ onUploadComplete }) {
    const [error, setError] = useState("");

    const handleFileChange = async (e) => {
        const files = Array.from(e.target.files);
        const formData = new FormData();

        files.forEach(file => formData.append("files", file));

        try {
            const res = await axios.post("/upload", formData, {
                headers: { "Content-Type": "multipart/form-data" }
            });

            // You could store session ID or just move on
            onUploadComplete(res.data); // Pass session ID to app if needed
        } catch (err) {
            setError("Failed to upload files.");
            console.error(err);
        }
    };

    return (
        <div style={{ textAlign: "center", marginTop: "4rem" }}>
            <h1>Upload Your Spotify History</h1>
            <input type="file" webkitdirectory="true" directory="true" multiple onChange={handleFileChange} />
            {error && <p style={{ color: 'red' }}>{error}</p>}
        </div>
    );
}
