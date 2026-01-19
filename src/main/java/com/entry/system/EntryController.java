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

    // --- H O D APPROVAL STATES ---
    private boolean isHODPermissionGranted = false;
    private String hodStatus = "Available";
    private String pendingUser = null;

    @PostMapping("/register")
    public Map<String, String> registerFace(@RequestBody Map<String, String> payload) {
        Map<String, String> response = new HashMap<>();
        try {
            String name = payload.get("name").trim();
            String imagePart = payload.get("image").split(",")[1];
            byte[] imageBytes = Base64.getDecoder().decode(imagePart);
            Mat faceMat = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);

            Imgproc.resize(faceMat, faceMat, FACE_SIZE);

            File folder = new File("registered_faces");
            if (!folder.exists()) folder.mkdir();

            String filePath = "registered_faces/" + name.replace(" ", "_") + ".jpg";
            Imgcodecs.imwrite(filePath, faceMat);

            response.put("message", "Successfully registered " + name);
            // ADDED VOICE FOR REGISTRATION
            response.put("voiceMessage", "Registration complete. " + name + " has been added to the database.");
        } catch (Exception e) {
            response.put("message", "Reg Error: " + e.getMessage());
            response.put("voiceMessage", "Registration failed due to a system error.");
        }
        return response;
    }

    @PostMapping("/verify")
    public Map<String, String> verifyFace(@RequestBody Map<String, String> payload) {
        Map<String, String> response = new HashMap<>();
        isHODPermissionGranted = false;
        pendingUser = null;

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
                response.put("voiceMessage", "The database is empty. Please register a user first.");
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
                if (score > 0.55 && score > highestScore) {
                    highestScore = score;
                    recognizedUser = file.getName().replace(".jpg", "").replace("_", " ");
                }
            }

            if (recognizedUser != null) {
                pendingUser = recognizedUser.toUpperCase();
                response.put("status", "PENDING");
                response.put("message", "IDENTIFIED: " + pendingUser);

                // H O D Pronunciation spacing
                String voiceMsg = "Identity confirmed as " + pendingUser + ". Waiting for H O D permission. ";
                if (!hodStatus.equals("Available")) {
                    voiceMsg += "Note: " + hodStatus.replace("HOD", "H O D");
                }

                response.put("voiceMessage", voiceMsg);
                response.put("hodStatus", hodStatus);
            } else {
                response.put("status", "DENIED");
                response.put("message", "Access Denied: Unknown User");
                response.put("voiceMessage", "Identity not verified. Access is denied.");
            }
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/hod/grant")
    public Map<String, String> grantPermission() {
        Map<String, String> response = new HashMap<>();
        if (pendingUser != null) {
            isHODPermissionGranted = true;
            response.put("message", "Permission Granted for " + pendingUser);
            response.put("voiceMessage", "Access granted by H O D. Welcome, " + pendingUser);
        } else {
            response.put("message", "No pending visitor.");
        }
        return response;
    }

    @PostMapping("/hod/set-busy")
    public Map<String, String> setBusyStatus(@RequestParam String time) {
        Map<String, String> response = new HashMap<>();
        this.hodStatus = "HOD is Busy until " + time;
        response.put("message", "Status updated: " + hodStatus);
        response.put("voiceMessage", "Status updated. H O D is now marked as busy until " + time);
        return response;
    }

    @PostMapping("/hod/clear-status")
    public Map<String, String> clearStatus() {
        Map<String, String> response = new HashMap<>();
        this.hodStatus = "Available";
        this.isHODPermissionGranted = false;
        this.pendingUser = null;
        response.put("message", "System Reset: Available");
        response.put("voiceMessage", "System reset. H O D is now available.");
        return response;
    }

    @GetMapping("/hod/check-status")
    public Map<String, Object> checkStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("granted", isHODPermissionGranted);
        response.put("hodStatus", hodStatus);
        response.put("visitor", pendingUser);
        return response;
    }

    @DeleteMapping("/delete")
    public Map<String, String> deleteUser(@RequestParam String name) {
        Map<String, String> response = new HashMap<>();
        try {
            String fileName = name.trim().replace(" ", "_") + ".jpg";
            File file = new File("registered_faces/" + fileName);
            if (file.exists() && file.delete()) {
                response.put("message", "Successfully deleted: " + name);
                // ADDED VOICE FOR DELETION
                response.put("voiceMessage", "User " + name + " has been successfully deleted from the system.");
            } else {
                response.put("message", "User not found.");
                response.put("voiceMessage", "User not found. No data was deleted.");
            }
        } catch (Exception e) {
            response.put("message", "Delete Error.");
        }
        return response;
    }

    private double getMatchScore(Mat liveGray, Mat targetGray) {
        Mat result = new Mat();
        Imgproc.matchTemplate(liveGray, targetGray, result, Imgproc.TM_CCOEFF_NORMED);
        return Core.minMaxLoc(result).maxVal;
    }
}