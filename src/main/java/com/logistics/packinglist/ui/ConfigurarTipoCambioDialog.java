package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import com.logistics.packinglist.model.ExchangeRateModel;
import com.logistics.packinglist.service.AuthService;
import com.logistics.packinglist.service.ExchangeRateStorageService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.logistics.packinglist.utils.ScreenUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ConfigurarTipoCambioDialog {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    private final ExchangeRateStorageService storageService = ExchangeRateStorageService.getInstance();

    public void show(Window ownerWindow) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (ownerWindow != null) {
            stage.initOwner(ownerWindow);
        }
        stage.setTitle("Configurar Tipo de Cambio de la Bodega");
        stage.setResizable(false);
        stage.setWidth(460);
        stage.setHeight(360);

        VBox root = new VBox(18);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #0f172a; -fx-font-family: 'Segoe UI', sans-serif;");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblIcon = new Label("💱");
        lblIcon.setStyle("-fx-font-size: 28px;");

        VBox headerText = new VBox(2);
        Label lblTitle = new Label("Tipo de Cambio Oficial");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Label lblSub = new Label("Define la tasa global de conversión CLP/USD para la bodega.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        headerText.getChildren().addAll(lblTitle, lblSub);
        header.getChildren().addAll(lblIcon, headerText);

        // Current model
        ExchangeRateModel currentModel = storageService.getModel();

        // Input
        TextField txtRate = new TextField();
        txtRate.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #f8fafc; -fx-border-color: #334155; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8;");
        
        DecimalFormat df = new DecimalFormat("#,##0.##", new DecimalFormatSymbols(Locale.US));
        txtRate.setText(df.format(currentModel.getExchangeRate()));

        Label lblExample = new Label();
        lblExample.setStyle("-fx-font-size: 12px; -fx-text-fill: #38bdf8; -fx-font-style: italic;");

        Runnable updateExample = () -> {
            try {
                String cleanStr = txtRate.getText().replaceAll("[^0-9.]", "");
                if (!cleanStr.isEmpty()) {
                    double r = Double.parseDouble(cleanStr);
                    DecimalFormat dfClp = new DecimalFormat("#,##0", new DecimalFormatSymbols(Locale.US));
                    lblExample.setText(String.format("💡 1,000 USD = $%s CLP", dfClp.format(1000 * r)));
                } else {
                    lblExample.setText("Ingrese una tasa válida.");
                }
            } catch (Exception ex) {
                lblExample.setText("Formato inválido.");
            }
        };

        txtRate.textProperty().addListener((obs, oldV, newV) -> updateExample.run());
        updateExample.run();

        GridPane formGrid = new GridPane();
        formGrid.setHgap(12);
        formGrid.setVgap(10);

        Label lblRateTitle = new Label("Tasa CLP por 1 USD ($):");
        lblRateTitle.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px; -fx-font-weight: bold;");

        formGrid.add(lblRateTitle, 0, 0);
        formGrid.add(txtRate, 1, 0);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(45);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(55);
        formGrid.getColumnConstraints().addAll(col1, col2);

        // Buttons
        Button btnSave = new Button("💾 Guardar Tasa");
        btnSave.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnSave.setMaxWidth(Double.MAX_VALUE);

        Button btnCancel = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.TIMES));
        btnCancel.getStyleClass().add("btn-cancelar");
        btnCancel.setStyle("-fx-background-color: #334155; -fx-text-fill: #cbd5e1; -fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnCancel.setMaxWidth(Double.MAX_VALUE);

        btnCancel.setOnAction(e -> stage.close());

        btnSave.setOnAction(e -> {
            try {
                String cleanStr = txtRate.getText().replaceAll("[^0-9.]", "");
                double newRate = Double.parseDouble(cleanStr);
                if (newRate <= 0) {
                    throw new IllegalArgumentException("La tasa debe ser mayor a 0.");
                }

                AuthService auth = AuthService.getInstance();
                String userName = auth.getNombre() + " " + auth.getApellido();
                storageService.updateExchangeRate(newRate, userName);
                stage.close();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Por favor ingrese un tipo de cambio válido: " + ex.getMessage(), ButtonType.OK);
                alert.showAndWait();
            }
        });

        HBox btnBox = new HBox(12, btnCancel, btnSave);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(btnCancel, Priority.ALWAYS);
        HBox.setHgrow(btnSave, Priority.ALWAYS);

        root.getChildren().addAll(header, new Separator(), formGrid, lblExample, new Separator(), btnBox);

        Scene scene = new Scene(root, 460, 360);
        stage.setScene(scene);
        ScreenUtil.centerOnOwner(stage, ownerWindow);
        stage.show();
    }
}
