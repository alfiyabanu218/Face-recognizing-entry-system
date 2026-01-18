package com.entry.system;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
public class EntryController {

    private static final Size FACE_SIZE = new Size(250, 250);

    @PostMapping("/register")
    public Map<String, String> registerFace(@RequestBody Map<String, String> payload) {
        Map<String, String> response = new HashMap<>();
        try {
            String name = payload.get("name");
            String imagePart = payload.get("image").split(",")[1];
            byte[] imageBytes = Base64.getDecoder().decode(imagePart);
            Mat faceMat = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);

            Imgproc.resize(faceMat, faceMat, FACE_SIZE);

            File folder = new File("registered_faces");
            if (!folder.exists()) folder.mkdir();

            // We save with underscores to prevent file path errors
            String filePath = "registered_faces/" + name.trim().replace(" ", "_") + ".jpg";
            Imgcodecs.imwrite(filePath, faceMat);

            response.put("message", "Successfully registered " + name);
        } catch (Exception e) {
            response.put("message", "Reg Error: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/verify")
    public Map<String, String> verifyFace(@RequestBody Map<String, String> payload) {
        Map<String, String> response = new HashMap<>();
        try {
            String base64Image = payload.get("image");
            String imagePart = base64Image.split(",")[1];
            byte[] imageBytes = Base64.getDecoder().decode(imagePart);
            Mat liveFrame = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);

            Imgproc.resize(liveFrame, liveFrame, FACE_SIZE);
            Mat liveGray = new Mat();
            Imgproc.cvtColor(liveFrame, liveGray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(liveGray, liveGray);

            File folder = new File("registered_faces");
            File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));

            if (listOfFiles == null || listOfFiles.length == 0) {
                response.put("message", "No users registered yet.");
                return response;
            }

            String recognizedUser = null;
            double highestScore = 0;

            for (File file : listOfFiles) {
                Mat savedFace = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                if (savedFace.empty()) continue;

                Imgproc.resize(savedFace, savedFace, FACE_SIZE);
                Imgproc.equalizeHist(savedFace, savedFace);

                double score = getMatchScore(liveGray, savedFace);
                System.out.println("User: " + file.getName() + " | Score: " + String.format("%.2f", score));

                if (score > 0.55 && score > highestScore) {
                    highestScore = score;
                    recognizedUser = file.getName().replace(".jpg", "").replace("_", " ");
                }
            }

            if (recognizedUser != null) {
                response.put("message", "Access Granted: " + recognizedUser.toUpperCase());
                response.put("voiceMessage", "Access granted. Welcome " + recognizedUser);
            } else {
                response.put("message", "Access Denied: Unknown User");
                response.put("voiceMessage", "Identity not verified.");
            }
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // --- NEW DELETE METHOD ADDED HERE ---
    @DeleteMapping("/delete")
    public Map<String, String> deleteUser(@RequestParam String name) {
        Map<String, String> response = new HashMap<>();
        try {
            // Match the format used in /register (trim and replace spaces with underscores)
            String fileName = name.trim().replace(" ", "_") + ".jpg";
            File file = new File("registered_faces/" + fileName);

            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("SYSTEM: Deleted user file " + fileName);
                    response.put("message", "Successfully deleted: " + name);
                } else {
                    response.put("message", "Failed to delete file. Check permissions.");
                }
            } else {
                response.put("message", "User '" + name + "' not found in database.");
            }
        } catch (Exception e) {
            response.put("message", "Delete Error: " + e.getMessage());
        }
        return response;
    }

    private double getMatchScore(Mat liveGray, Mat targetGray) {
        Mat result = new Mat();
        Imgproc.matchTemplate(liveGray, targetGray, result, Imgproc.TM_CCOEFF_NORMED);
        return Core.minMaxLoc(result).maxVal;
    }
}