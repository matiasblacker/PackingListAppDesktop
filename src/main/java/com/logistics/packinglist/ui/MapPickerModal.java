package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.logistics.packinglist.utils.ScreenUtil;
import netscape.javascript.JSObject;

public class MapPickerModal extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    private Double selectedLat = null;
    private Double selectedLng = null;
    private boolean confirmed = false;

    private TextField txtLat;
    private TextField txtLng;
    private WebView webView;

    public MapPickerModal(Window owner, Double initialLat, Double initialLng) {
        this.selectedLat = initialLat != null ? initialLat : -33.4489; // Default Santiago
        this.selectedLng = initialLng != null ? initialLng : -70.6693;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Selección de Coordenadas Geográficas");

        setMinWidth(750);
        setMinHeight(550);

        construirUI();
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header Title
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.MAP_MARKER);
        icon.setSize("22px");
        icon.setFill(Color.web("#0F3E6E"));

        Label lblTitle = new Label("Haz clic en el mapa para marcar la ubicación del evento de tracking");
        lblTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");
        header.getChildren().addAll(icon, lblTitle);

        // WebView Map Container
        webView = new WebView();
        VBox.setVgrow(webView, Priority.ALWAYS);

        WebEngine engine = webView.getEngine();
        String htmlContent = buildLeafletPickerHtml(selectedLat, selectedLng);
        engine.loadContent(htmlContent, "text/html");

        // Bridge from JS to JavaFX to capture map clicks
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaApp", new JavaBridge());
            }
        });

        // Bottom Controls
        HBox bottomBox = new HBox(12);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(10));
        bottomBox.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;");

        txtLat = new TextField(String.format("%.6f", selectedLat));
        txtLat.setPrefWidth(140);
        txtLat.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");
        txtLng = new TextField(String.format("%.6f", selectedLng));
        txtLng.setPrefWidth(140);
        txtLng.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");

        Button btnConfirm = new Button("Confirmar Coordenadas", new FontAwesomeIconView(FontAwesomeIcon.CHECK));
        btnConfirm.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 14; -fx-font-size: 10px;");
        btnConfirm.setOnAction(e -> {
            try {
                selectedLat = Double.parseDouble(txtLat.getText().replace(",", ".").trim());
                selectedLng = Double.parseDouble(txtLng.getText().replace(",", ".").trim());
                confirmed = true;
                close();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Ingrese valores numéricos válidos para Latitud y Longitud.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        Button btnCancel = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.TIMES));
        btnCancel.getStyleClass().add("btn-cancelar");
        btnCancel.setOnAction(e -> close());

        Label lblLat = new Label("Latitud:");
        lblLat.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        Label lblLng = new Label("Longitud:");
        lblLng.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #475569;");

        bottomBox.getChildren().addAll(
                lblLat, txtLat,
                lblLng, txtLng,
                new Region(), btnCancel, btnConfirm
        );
        HBox.setHgrow(bottomBox.getChildren().get(4), Priority.ALWAYS);

        root.getChildren().addAll(header, webView, bottomBox);

        Scene scene = new Scene(root, 750, 520);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception ignored) {}
        setScene(scene);
        ScreenUtil.fitDialogToScreen(this, getOwner(), 750, 520, 700, 450);
    }

    public class JavaBridge {
    private final MantenimientoService service = MantenimientoService.getInstance();

    
        public void setCoordinates(double lat, double lng) {
            javafx.application.Platform.runLater(() -> {
                selectedLat = lat;
                selectedLng = lng;
                txtLat.setText(String.format("%.6f", lat));
                txtLng.setText(String.format("%.6f", lng));
            });
        }
    }

    private String buildLeafletPickerHtml(double lat, double lng) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='utf-8'/>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>body,html,#map{height:100%;margin:0;padding:0;}</style>" +
                "</head><body><div id='map'></div>" +
                "<script>" +
                "var map = L.map('map').setView([" + lat + ", " + lng + "], 13);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "   attribution: '&copy; OpenStreetMap'" +
                "}).addTo(map);" +
                "var marker = L.marker([" + lat + ", " + lng + "], {draggable: true}).addTo(map);" +
                "function updateJava(lat, lng) {" +
                "   if (window.javaApp) window.javaApp.setCoordinates(lat, lng);" +
                "}" +
                "map.on('click', function(e) {" +
                "   marker.setLatLng(e.latlng);" +
                "   updateJava(e.latlng.lat, e.latlng.lng);" +
                "});" +
                "marker.on('dragend', function(e) {" +
                "   var pos = marker.getLatLng();" +
                "   updateJava(pos.lat, pos.lng);" +
                "});" +
                "</script></body></html>";
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Double getSelectedLat() {
        return selectedLat;
    }

    public Double getSelectedLng() {
        return selectedLng;
    }
}
