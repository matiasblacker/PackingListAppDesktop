package com.logistics.packinglist;

import com.logistics.packinglist.ui.MainController;
import com.logistics.packinglist.utils.ScreenUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Packing List — Logística");

        // Escuchar y reportar cambios de DPI de salida
        stage.outputScaleXProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("[DPI] Escala X de salida de pantalla modificada de " + oldVal + " a " + newVal);
        });
        stage.outputScaleYProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("[DPI] Escala Y de salida de pantalla modificada de " + oldVal + " a " + newVal);
        });

        try {
            stage.getIcons().add(new Image(getClass().getResource("/icon.png").toExternalForm()));
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }

        mostrarLogin(stage);
    }

    private void mostrarLogin(Stage stage) {
        stage.setMaximized(false);
        stage.setResizable(false);
        com.logistics.packinglist.ui.LoginController loginRoot = new com.logistics.packinglist.ui.LoginController(stage, () -> {
            mostrarMain(stage);
        });
        
        Scene scene = new Scene(loginRoot, 400, 500);
        ScreenUtil.applyResponsiveTheme(scene);
        stage.setScene(scene);
        stage.setWidth(400);
        stage.setHeight(520);
        stage.setMinWidth(400);
        stage.setMinHeight(500);
        stage.show();
        
        javafx.application.Platform.runLater(() -> {
            stage.setMaximized(false);
            stage.setWidth(400);
            stage.setHeight(520);
            ScreenUtil.centerOnActiveScreen(stage);
        });
    }

    private void mostrarMain(Stage stage) {
        stage.setResizable(true);
        stage.setMaxWidth(Double.MAX_VALUE);
        stage.setMaxHeight(Double.MAX_VALUE);
        
        MainController root = new MainController(stage, () -> {
            mostrarLogin(stage);
        });
        Scene scene = new Scene(root, 950, 600);
        ScreenUtil.applyResponsiveTheme(scene);
        
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        
        stage.show();
        
        stage.setOnCloseRequest(e -> {
            com.logistics.packinglist.service.AuthService.getInstance().logout();
        });
        
        // Reforzar maximizado despues del show para mayor compatibilidad
        javafx.application.Platform.runLater(() -> {
            stage.setMaximized(true);
        });
    }

    public static void main(String[] args) {
        // Forzar y optimizar escalamiento HiDPI de JavaFX para pantallas de alta resolución
        System.setProperty("prism.allowhidpi", "true");
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            System.setProperty("glass.win.uiScale", "true");
        }
        launch(args);
    }
}
