package com.entry.system;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.util.Base64;

@SpringBootApplication
public class Main extends Application {

    static {
        nu.pattern.OpenCV.loadShared();
    }

    private static ConfigurableApplicationContext springContext;
    private Text statusText;

    @Override
    public void init() throws Exception {
        // Starts Spring Boot in the background
        springContext = SpringApplication.run(Main.class);
    }

    @Override
    public void start(Stage primaryStage) {
        // --- CAMERA HARDWARE REMOVED FROM JAVAFX ---
        // We no longer call 'new VideoCapture(0)' here because
        // the Browser (index.html) will use the camera instead.

        statusText = new Text("SERVER ONLINE: WAITING FOR BROWSER...");
        statusText.setFill(Color.web("#00f2ff"));
        statusText.setFont(Font.font("Courier New", 20));

        // --- UI PANEL ---
        VBox controlPanel = new VBox(15);
        controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setPadding(new Insets(20));
        controlPanel.setStyle("-fx-background-color: #050a12; -fx-border-color: #00f2ff; -fx-border-width: 2;");

        Label infoLabel = new Label("AI Backend Control");
        infoLabel.setTextFill(Color.WHITE);

        Button btnList = createStyledButton("View Database", "#ffcc00");
        btnList.setOnAction(e -> showRegisteredList());

        controlPanel.getChildren().addAll(infoLabel, statusText, btnList);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(controlPanel);
        mainLayout.setStyle("-fx-background-color: #050a12;");

        primaryStage.setTitle("AI Entry System - Backend Status");
        primaryStage.setScene(new Scene(mainLayout, 600, 400));

        primaryStage.setOnCloseRequest(e -> {
            if (springContext != null) springContext.close();
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    // --- HELPER: DECODE BASE64 (Used by your EntryController) ---
    public static Mat decodeBase64ToMat(String base64Image) {
        try {
            String base64Data = base64Image.split(",")[1];
            byte[] data = Base64.getDecoder().decode(base64Data);
            return Imgcodecs.imdecode(new MatOfByte(data), Imgcodecs.IMREAD_COLOR);
        } catch (Exception e) {
            System.err.println("Decode error: " + e.getMessage());
            return null;
        }
    }

    private void showRegisteredList() {
        Stage stage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: #050a12;");

        // This checks your 'registered_faces' folder
        File folder = new File("registered_faces");
        if (folder.exists() && folder.listFiles() != null) {
            for (File f : folder.listFiles()) {
                Text t = new Text("USER: " + f.getName());
                t.setFill(Color.WHITE);
                layout.getChildren().add(t);
            }
        }
        stage.setScene(new Scene(layout, 300, 400));
        stage.show();
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color + "; -fx-border-color: " + color + "; -fx-border-radius: 5;");
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}