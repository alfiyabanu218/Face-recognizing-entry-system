package com.entry.system;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.opencv.core.Core;

public class MainApp extends Application {
    // Crucial: Load the OpenCV library before anything else
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    @Override
    public void start(Stage primaryStage) {
        // 1. Layer: Background Map
        Image mapImg = new Image(getClass().getResourceAsStream("/background.jpg"));
        ImageView background = new ImageView(mapImg);
        background.setOpacity(0.15); // Very subtle map background
        background.setFitWidth(1200);
        background.setPreserveRatio(true);

        // 2. Layer: Camera Feed
        ImageView cameraView = new ImageView();
        cameraView.getStyleClass().add("camera-view");
        cameraView.setFitWidth(800);
        cameraView.setFitHeight(500);

        // 3. Layer: HUD Text
        Text statusText = new Text("INITIALIZING SECURITY SCAN...");
        statusText.getStyleClass().add("status-text");

        // 4. Layout: Stacked layers
        StackPane root = new StackPane();
        root.getChildren().addAll(background, cameraView, statusText);
        StackPane.setAlignment(statusText, Pos.BOTTOM_CENTER);

        // Final Stage setup
        Scene scene = new Scene(root, 1100, 750);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Smart Department Entry System");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Pass everything to the logic controller
        SystemController controller = new SystemController(cameraView, statusText);
        controller.startSystem();
    }

    public static void main(String[] args) {
        launch(args);
    }
}