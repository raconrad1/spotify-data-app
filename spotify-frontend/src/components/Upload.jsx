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

            <h1>Explore your Spotify listening history!</h1>
            <h3>If you don't yet have your Spotify data</h3>
            <p>Go to <a href="https://www.spotify.com/us/account/privacy/" target="_blank">Spotify's privacy page</a> and scroll down to request your Extended Streaming History.</p>
            <p>This includes every track and podcast you've every played, when you played them, for how long, etc. For more information, visit <a href="https://support.spotify.com/us/article/understanding-my-data/#_gl=1*13r3kp5*_gcl_au*MjA4MDgyNTQ4Mi4xNzQ2MTA1NDI1" target="_blank">Spotify's page on understanding the data.</a></p>
            <p>Note: This app is <b>NOT</b> compatible with the data that you would receive if you requested the "Account data", which takes about 5 days to retrieve.</p>
            <p>Once requested, Spotify will send you data for the lifetime of your account in about 3 weeks time.</p>

            <div style={{ width: "45%", borderBottom: "solid black", margin: "1.5rem auto" }}/>

            <p>When Spotify sends you an email that your data is ready to be downloaded, follow their instructions in the email and retrieve your .zip file.</p>
            <p>Unzip the file (<a href="https://support.microsoft.com/en-us/windows/zip-and-unzip-files-8d28fa72-f2f9-712f-67df-f80cf89fd4e5" target="_blank">instructions here</a>) and you'll be left with a folder probably called something like "Spotify Extended Streaming History".</p>
            <p>Inside this folder is a bunch of json files that are virtually useless on their own. Messy data that is not very readable, and that isn't compiled in any way.</p>
            <h4>That's where this app comes in handy! Upload the entire folder below and we'll do the rest.</h4>

            <input type="file" webkitdirectory="true" directory="true" multiple onChange={handleFileChange} />
            {error && <p style={{ color: 'red' }}>{error}</p>}
        </div>
    );
}
