package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import com.logistics.packinglist.model.InventarioItem;
import com.logistics.packinglist.service.InventarioService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public class LectorSKUDialog extends Stage {
    private final InventarioService service;
    private final Consumer<InventarioItem> onLectura;

    public LectorSKUDialog(Window owner, InventarioService service, Consumer<InventarioItem> onLectura) {
        this.service = service;
        this.onLectura = onLectura;

        initModality(Modality.NONE); // Permite interactuar con el padre si es necesario, pero es mejor que capture
                                     // el foco
        setTitle("Modo Lector SKU");
        setResizable(false);
        setAlwaysOnTop(true);

        construirUI();
    }

    private void construirUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c3e50;");

        Label lblInstruccion = new Label("ESCANEE EL CÓDIGO SKU");
        lblInstruccion.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        TextField txtLector = new TextField();
        txtLector.setPromptText("Esperando lectura...");
        txtLector.setPrefWidth(250);
        txtLector.setStyle("-fx-font-size: 16px; -fx-alignment: center;");

        // Corrección en tiempo real: comillas -> guiones y quitar espacios
        txtLector.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                String corrected = newV.replace("'", "-").replaceAll("\\s+", "");
                if (!newV.equals(corrected)) {
                    txtLector.setText(corrected);
                }
            }
        });

        Label lblMensaje = new Label("Listo para leer...");
        lblMensaje.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 12px;");

        txtLector.setOnAction(e -> {
            String sku = txtLector.getText().trim();
            if (!sku.isEmpty()) {
                InventarioItem item = service.buscarPorSKU(sku);
                if (item != null) {
                    onLectura.accept(item);
                    lblMensaje.setText("✓ Escaneado: " + item.getPartNumber());
                    lblMensaje.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 12px;");
                } else {
                    lblMensaje.setText("✗ SKU no encontrado: " + sku);
                    lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
                }
                txtLector.clear();
            }
        });

        root.getChildren().addAll(lblInstruccion, txtLector, lblMensaje);

        Scene scene = new Scene(root, 350, 180);
        setScene(scene);

        // Asegurar foco inicial
        setOnShown(e -> txtLector.requestFocus());
    }
}
