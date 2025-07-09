import { useState } from 'react';
import axios from 'axios';
import JSZip from "jszip";

export default function Upload({ onUploadComplete }) {
    const [error, setError] = useState("");
    const [isHovered, setIsHovered] = useState(false);


    const labelStyle = {
        display: "block",
        width: "120px",
        margin: "20px auto",
        textAlign: "center",
        padding: "16px 24px",
        fontSize: "16px",
        borderRadius: "10px",
        cursor: "pointer",
        backgroundColor: isHovered ? "#03deff" : "#7feafa",
        transition: "background-color 0.3s"
    };

    const handleFileChange = async (e) => {
        setError("");
        const files = Array.from(e.target.files);
        if (!files.length) return;

        try {
            const zip = new JSZip();

            for (const file of files) {
                zip.file(file.name, file);
            }

            const zippedBlob = await zip.generateAsync({ type: "blob" });

            const formData = new FormData();
            const zippedFile = new File([zippedBlob], "spotify-files.zip", {
                type: "application/zip",
            });
            formData.append("file", zippedFile);

            const res = await axios.post("/api/upload", formData, {
                headers: {
                    "Content-Type": "multipart/form-data",
                },
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
            <div style={{ margin: "3% 0" }}>
                <h3>If you don't yet have your Spotify data</h3>
                <p>Go to <a href="https://www.spotify.com/us/account/privacy/" target="_blank">Spotify's privacy page</a> and scroll down to request your Extended Streaming History.</p>
                <p>This includes every track and podcast you've every played, when you played them, for how long, etc. For more information, visit <a href="https://support.spotify.com/us/article/understanding-my-data/#_gl=1*13r3kp5*_gcl_au*MjA4MDgyNTQ4Mi4xNzQ2MTA1NDI1" target="_blank">Spotify's page on understanding the data.</a></p>
                <p>Note: This app is <b>NOT</b> compatible with the data that you would receive if you requested the "Account data", which takes about 5 days to retrieve.</p>
                <p>Once requested, Spotify will send you data for the lifetime of your account in about 3 weeks time.</p>
            </div>

            <div style={{ width: "45%", borderBottom: "dashed black", margin: "1.5rem auto" }}/>

            <div style={{ margin: "3% 0" }}>
                <p>When Spotify sends you an email that your data is ready to be downloaded, follow their instructions in the email and retrieve your .zip file.</p>
                <p>Inside this .zip file is a bunch of json files that are virtually useless on their own. Messy data that is not very readable, and that isn't compiled in any way.</p>
                <h4>That's where this app comes in handy! Upload the entire .zip file below and we'll do the rest.</h4>
            </div>
            <label style={labelStyle}
                   htmlFor="file-upload"
                   onMouseEnter={() => setIsHovered(true)}
                   onMouseLeave={() => setIsHovered(false)}
            >
            Upload ZIP
            <input id="file-upload"
                   style={{display: "none"}}
                   type="file"
                   accept=".zip"
                   onChange={handleFileChange} />
            </label>

            {error && <p style={{ color: 'red' }}>{error}</p>}
        </div>
    );
}
