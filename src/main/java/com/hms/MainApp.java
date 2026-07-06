package com.hms;

import com.hms.util.DBConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlUrl = getClass().getResource("/fxml/Main.fxml");
        if (fxmlUrl == null) {
            throw new RuntimeException("Cannot find /fxml/Main.fxml on classpath");
        }

        Parent root = FXMLLoader.load(fxmlUrl);

        Scene scene = new Scene(root, 1200, 750);
        URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle("Hospital Management System — HMS");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DBConnection.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
