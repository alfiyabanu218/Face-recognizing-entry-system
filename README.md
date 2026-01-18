# Smart Department Entry System with Face Recognition 🤖🚪

A Java-based intelligent gatekeeper designed for a Department Head (HOD) cabin. This system uses computer vision to recognize faces and greets registered users with a sweet female voice.

## ✨ Key Features
* **Face Registration:** Register users via webcam or image upload.
* **Smart Recognition:** Real-time face detection using OpenCV Haar Cascades.
* **Voice Welcome:** Personalized greeting using FreeTTS (Text-to-Speech).
* **Security:** Displays a warning for unknown/unregistered visitors.

## 🛠️ Technology Stack
* **Language:** Java
* **AI/Vision:** OpenCV 4.x
* **Audio:** FreeTTS (Java Text-to-Speech)
* **GUI:** JavaFX / Swing

## 📁 Project Structure
* `src/`: Java source code (Controllers, Launcher, and Logic).
* `data/`: Local storage for registered faculty, students, and guests.
* `resources/`: Pre-trained Haar Cascade models for face and eye detection.

## 🚀 How to Run
1. Clone this repository to your local machine.
2. Ensure you have **JDK 17+** installed.
3. Add the OpenCV `.jar` and Native Library to your project path.
4. Run `AppLauncher.java`.

## 📸 Sample Output
> "Welcome Prof. Alfiya" - *Spoken in a sweet female voice when recognized.*
