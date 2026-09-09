package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class AcercaDeDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    public AcercaDeDialog(Window owner) {
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Acerca de");

        // Fijar tamaño a nivel de ventana para evitar overrides del WM en Linux
        setWidth(400);
        setHeight(400);
        setMinWidth(400);
        setMinHeight(400);
        setMaxWidth(400);
        setMaxHeight(400);
        setResizable(false);

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white;");

        // Icono de la App
        try {
            ImageView imgApp = new ImageView(new Image(getClass().getResourceAsStream("/icon-lightbg.png")));
            imgApp.setFitHeight(60);
            imgApp.setPreserveRatio(true);
            root.getChildren().add(imgApp);
        } catch (Exception ignored) {
        }

        Label lblNombre = new Label("PackingList APP");
        lblNombre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        Label lblVersion = new Label("Versión 2.0.0");
        lblVersion.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        VBox infoDesarrollo = new VBox(2);
        infoDesarrollo.setAlignment(Pos.CENTER);
        infoDesarrollo.setPadding(new Insets(5, 0, 5, 0));

        Label lblDev = new Label("Desarrollado por:");
        lblDev.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        Label lblNombreDev = new Label("Matias Perez M.");
        Label lblEmail = new Label("matias.perez.sd94@gmail.com");
        lblEmail.setStyle("-fx-text-fill: #1a6aa8; -fx-font-size: 11px;");

        infoDesarrollo.getChildren().addAll(lblDev, lblNombreDev, lblEmail);

        HBox powered = new HBox(8);
        powered.setAlignment(Pos.CENTER);
        Label lblPowered = new Label("Powered by Java");
        lblPowered.setStyle("-fx-font-style: italic; -fx-font-size: 11px;");

        try {
            ImageView imgJava = new ImageView(new Image(getClass().getResourceAsStream("/java_logo.png")));
            imgJava.setFitHeight(40);
            imgJava.setPreserveRatio(true);
            powered.getChildren().addAll(lblPowered, imgJava);
        } catch (Exception e) {
            powered.getChildren().add(lblPowered);
        }

        root.getChildren().addAll(lblNombre, lblVersion, infoDesarrollo, powered);

        Scene scene = new Scene(root, 400, 400);
        setScene(scene);

        // Ensure it displays in the middle of the screen
        centerOnScreen();
    }
}
