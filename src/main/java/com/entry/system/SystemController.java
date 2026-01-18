package com.entry.system;

import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import java.io.ByteArrayInputStream;

public class SystemController {
    private VideoCapture capture;
    private CascadeClassifier faceDetector;
    private ImageView imageView;
    private Text statusLabel;
    private long lastGreetingTime = 0;

    public SystemController(ImageView view, Text status) {
        this.imageView = view;
        this.statusLabel = status;

        // Load the Face detection model from resources
        String xmlPath = getClass().getResource("/haarcascade_frontalface_alt.xml").getPath();
        if (xmlPath.startsWith("/")) xmlPath = xmlPath.substring(1); // Fix for Windows

        this.faceDetector = new CascadeClassifier(xmlPath);
        this.capture = new VideoCapture(0); // Opens default webcam
    }

    public void startSystem() {
        // This loop runs every frame (approx 30-60 times per second)
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                Mat frame = new Mat();
                if (capture.read(frame)) {
                    processFrame(frame);
                }
            }
        }.start();
    }

    private void processFrame(Mat frame) {
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        // Optimization: Convert to Grayscale for faster math
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        faceDetector.detectMultiScale(grayFrame, faces);

        Rect[] detectedFaces = faces.toArray();
        if (detectedFaces.length > 0) {
            statusLabel.setText("--- FACE DETECTED: ACCESS GRANTED ---");

            for (Rect rect : detectedFaces) {
                // Draw a Cyber-Cyan rectangle on the screen
                Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(255, 242, 0), 3);
            }

            // Logic: Voice Welcome only if 10 seconds have passed since last one
            if (System.currentTimeMillis() - lastGreetingTime > 10000) {
                playVoice("Access Granted. Welcome to the Department.");
                lastGreetingTime = System.currentTimeMillis();
            }
        } else {
            statusLabel.setText("SCANNING FOR AUTHORIZED PERSONNEL...");
        }

        // Convert the OpenCV Matrix back to a JavaFX Image to show in the window
        imageView.setImage(convertMatToImage(frame));
    }

    private void playVoice(String message) {
        new Thread(() -> {
            try {
                // Uses Windows Native PowerShell Voice (No extra library needed)
                String script = "Add-Type -AssemblyName System.Speech; " +
                        "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "$s.Speak('" + message + "')";
                ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", script);
                pb.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Image convertMatToImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}