const video = document.getElementById('webcam');
const status = document.getElementById('status-bar');
const canvas = document.getElementById('photo-canvas');

// 1. Start the camera
navigator.mediaDevices.getUserMedia({ video: true })
    .then(stream => video.srcObject = stream);

// 2. The "Sweet Voice" (Native Browser API)
function speak(text) {
    const synth = window.speechSynthesis;
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.pitch = 1.2; // Makes it sound "sweeter"
    synth.speak(utterance);
}

// 3. Capture frame and send to Backend
async function sendAction(endpoint, name = "") {
    status.innerText = "ANALYZING...";

    // Draw current video frame to hidden canvas
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    canvas.getContext('2d').drawImage(video, 0, 0);

    // Convert to Base64 image string
    const imageData = canvas.toDataURL('image/png');

    const response = await fetch(`/${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ image: imageData, name: name })
    });

    const result = await response.json();
    status.innerText = result.message;
    speak(result.voiceMessage);
}

function registerUser() {
    const name = prompt("Enter Name for Registration:");
    if (name) sendAction('register', name);
}