import { useState } from 'react';
import axios from 'axios';
import JSZip from "jszip";

export default function Upload({ onUploadComplete }) {
    const [error, setError] = useState("");
    const [isUploadHovered, setIsUploadHovered] = useState(false);

    const textBox = {
        border: "solid #bfbfbf",
        borderRadius: "15px",
        margin: "10px",
        padding: "1rem",
        width: "60%",
        // boxShadow: "12px 12px 2px 1px rgb(0 0 255 / 0.2)",
        boxShadow: "0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19)",
        // boxShadow: "0 4px 8px 0 #1db954, 0 6px 20px 0 #20d660"
    }

    const mainBox = {
        backgroundColor: "white",
        marginLeft: "auto",
        marginRight: "auto",
        width: "83%",
        textAlign: "center",
        marginTop: "1rem",
        display: "flex",
        justifyContent: "center",
        flexDirection: "column",
        alignItems: "center",
        borderRadius: "50px",
        boxShadow: "5px 4px 10px 5px rgba(0, 0, 0, 0.2), 5px 6px 15px 5px rgba(0, 0, 0, 0.19)"
    }

    function HoverLink({ href, children }) {
        const [hover, setHover] = useState(false);

        const style = {
            color: hover ? "#005f99" : "#0099ff",
            textDecoration: hover ? "underline" : "none",
            transition: "color 0.3s, text-decoration 0.3s"
        };

        return (
            <a
                href={href}
                target="_blank"
                rel="noopener noreferrer"
                style={style}
                onMouseEnter={() => setHover(true)}
                onMouseLeave={() => setHover(false)}
            >
                {children}
            </a>
        );
    }

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

    const uploadButton = (
        <label
            style={{
                display: "block",
                width: "140px",
                margin: "20px auto",
                textAlign: "center",
                padding: "16px 24px",
                fontSize: "17px",
                borderRadius: "60px",
                cursor: "pointer",
                backgroundColor: isUploadHovered ? "#09ed5a" : "#2af572",
                transition: "all 0.2s ease-out",
                transform: isUploadHovered ? "scale(1.05)" : "scale(1)",
                boxShadow: "0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19)"
            }}
            htmlFor="file-upload"
            onMouseEnter={() => setIsUploadHovered(true)}
            onMouseLeave={() => setIsUploadHovered(false)}
        >
            Upload ZIP
            <input
                id="file-upload"
                style={{ display: "none" }}
                type="file"
                accept=".zip"
                onChange={handleFileChange}
            />
        </label>
    );

    return (
        <div style={mainBox}>
            <h1>Explore your Spotify listening history!</h1>
            <div style={{ ...textBox, marginTop: "2rem"}}>
                <h2>How it works</h2>
                <p>
                    Spotify allows users to download their data for <b>every single song and podcast</b> that they have ever played.
                    This data includes when you played those songs, for how long, if they've been skipped, whether you were listening on shuffle mode, and more!
                </p>
                <p>
                    Using this data, this application will show you a bunch of fun statistics about your entire listening history such as top artists, songs, podcasts, yearly data, and more.
                </p>
            </div>
            <div style={{ ...textBox, marginTop: "2rem", marginBottom: "2rem"}}>
                <h2>Obtain your Spotify data</h2>
                <ol>
                    <li>
                        Go to {" "}
                        <HoverLink href="https://www.spotify.com/us/account/privacy/" target="_blank">
                            Spotify's privacy page
                        </HoverLink>{" "} and scroll down to the "Download your data" section.
                    </li>
                    <li>
                        Uncheck "Select account data", and check "Select extended streaming history".
                    </li>

                </ol>
                <p>
                    Once requested, Spotify will send your extended listening history data in ~3 weeks or less.
                    It could take a while, but it's worth the wait!
                </p>
            </div>

            <div style={textBox}>
                <h2>Upload your data</h2>
                <p>
                    When Spotify sends you the email that your data is ready to be downloaded, follow their instructions to download your data.
                    Inside the .zip file is a bunch of JSON files that are virtually useless on their own. It's data that is not very readable, and that isn't compiled in any way.
                </p>
                <h4>
                    That's where this app comes in handy! Upload your .zip file below and we'll do the rest.
                </h4>
                <p>
                    <em>Your data will not be uploaded into any database.</em>
                </p>
            </div>
            {uploadButton}
            <p style={{ width: "55%", fontSize: ".85rem" }}>
                If you want to do a deep dive on how the raw data looks, visit{" "}
                <HoverLink href="https://support.spotify.com/us/article/understanding-my-data/#_gl=1*13r3kp5*_gcl_au*MjA4MDgyNTQ4Mi4xNzQ2MTA1NDI1">
                    Spotify's page on understanding the data
                </HoverLink>{" "}
                and find the "Extended Streaming History" section.
            </p>
            {error && <p style={{ color: 'red' }}>{error}</p>}
        </div>
    );
}
