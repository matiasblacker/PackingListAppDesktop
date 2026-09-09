package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.LocationModel;
import com.logistics.packinglist.model.ProductModel;
import com.logistics.packinglist.service.MantenimientoService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

public class ReubicarStockDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

        private final ProductModel product;
    private final LocationModel fromLocation;
    private int currentMaxQuantity;
    private final String tipoStock;
    private final List<LocationModel> destLocations;
    private final ObservableList<LocationModel> filteredDestLocations = FXCollections.observableArrayList();
    private final ObservableList<com.logistics.packinglist.model.WarehouseZoneModel> filteredZones = FXCollections.observableArrayList();
    private final java.util.Map<String, com.logistics.packinglist.model.WarehouseZoneModel> zoneMap = new java.util.HashMap<>();

    private ComboBox<com.logistics.packinglist.model.WarehouseZoneModel> cbZona;
    private ComboBox<LocationModel> cbDestino;
    private TextField txtCantidad;
    private Button btnConfirmar;
    private Button btnCancelar;
    private Label lblStatus;
    private Label lblDispV;

    public ReubicarStockDialog(
            Window owner,
            MantenimientoService service,
            ProductModel product,
            LocationModel fromLocation,
            int maxQuantity,
            String tipoStock,
            List<LocationModel> destLocations
    ) {
                this.product = product;
        this.fromLocation = fromLocation;
        this.currentMaxQuantity = maxQuantity;
        this.tipoStock = tipoStock;
        this.destLocations = destLocations;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Reubicar Stock - " + product.getSku());
        setResizable(false);

        Scene scene = new Scene(crearContenido(), 450, 450);
        setScene(scene);
        
        setOnShown(e -> {
            if (txtCantidad != null) {
                txtCantidad.requestFocus();
            }
        });

        cargarZonas();
    }

    private Parent crearContenido() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Cabecera
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 15, 10, 15));
        header.setStyle("-fx-background-color: linear-gradient(to right, #0F3E6E, #1b5a9e); -fx-background-radius: 6;");

        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.EXCHANGE);
        icon.setSize("24");
        icon.setFill(javafx.scene.paint.Color.WHITE);

        Label lblTitle = new Label("Reubicación de Stock");
        lblTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        header.getChildren().addAll(icon, lblTitle);

        // Formulario
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 5, 10, 5));

        // Estilos de labels
        String labelStyle = "-fx-font-weight: bold; -fx-text-fill: #495057;";
        String valueStyle = "-fx-text-fill: #212529; -fx-font-family: monospace;";

        // Fila 1: Producto
        Label lblProdL = new Label("Producto:");
        lblProdL.setStyle(labelStyle);
        Label lblProdV = new Label(product.getSku() + " - " + product.getNombre());
        lblProdV.setStyle(valueStyle);
        lblProdV.setWrapText(true);
        grid.add(lblProdL, 0, 0);
        grid.add(lblProdV, 1, 0);

        // Fila 2: Origen
        Label lblOrigL = new Label("Ubicación Origen:");
        lblOrigL.setStyle(labelStyle);
        Label lblOrigV = new Label(fromLocation.toString());
        lblOrigV.setStyle(valueStyle);
        grid.add(lblOrigL, 0, 1);
        grid.add(lblOrigV, 1, 1);

        // Fila 3: Tipo Stock
        Label lblTipoL = new Label("Tipo de Stock:");
        lblTipoL.setStyle(labelStyle);
        Label lblTipoV = new Label(tipoStock);
        lblTipoV.setStyle(valueStyle + "-fx-font-weight: bold;");
        grid.add(lblTipoL, 0, 2);
        grid.add(lblTipoV, 1, 2);

        // Fila 4: Cantidad Disponible
        Label lblDispL = new Label("Stock Disponible:");
        lblDispL.setStyle(labelStyle);
        lblDispV = new Label(currentMaxQuantity + " unidades");
        lblDispV.setStyle(valueStyle + "-fx-text-fill: #28a745; -fx-font-weight: bold;");
        grid.add(lblDispL, 0, 3);
        grid.add(lblDispV, 1, 3);

        // Fila 5: Zona Destino
        Label lblZonaL = new Label("Zona Destino:");
        lblZonaL.setStyle(labelStyle);
        cbZona = new ComboBox<>();
        cbZona.setPromptText("Todas las zonas...");
        cbZona.setMaxWidth(Double.MAX_VALUE);
        configurarComboBoxSearchGeneric(cbZona, filteredZones);
        cbZona.valueProperty().addListener((obs, oldVal, newVal) -> {
            actualizarFiltroUbicaciones();
        });
        grid.add(lblZonaL, 0, 4);
        grid.add(cbZona, 1, 4);

        // Fila 6: Destino
        Label lblDestL = new Label("Ubicación Destino:");
        lblDestL.setStyle(labelStyle);
        cbDestino = new ComboBox<>();
        cbDestino.setPromptText("Seleccione ubicación...");
        cbDestino.setMaxWidth(Double.MAX_VALUE);
        configurarComboBoxSearchGeneric(cbDestino, filteredDestLocations);
        grid.add(lblDestL, 0, 5);
        grid.add(cbDestino, 1, 5);

        // Fila 7: Cantidad a Mover
        Label lblCantL = new Label("Cantidad a Mover:");
        lblCantL.setStyle(labelStyle);
        txtCantidad = new TextField();
        txtCantidad.setPromptText("Ej. " + currentMaxQuantity);
        grid.add(lblCantL, 0, 6);
        grid.add(txtCantidad, 1, 6);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(130);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        // Barra de estado / carga
        lblStatus = new Label();
        lblStatus.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
        lblStatus.setMaxWidth(Double.MAX_VALUE);
        lblStatus.setAlignment(Pos.CENTER);

        // Botones de acción
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        btnConfirmar = new Button("Confirmar", new FontAwesomeIconView(FontAwesomeIcon.CHECK));
        btnConfirmar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnConfirmar.setOnAction(e -> confirmarReubicacion());

        btnCancelar = new Button("Cerrar", new FontAwesomeIconView(FontAwesomeIcon.TIMES));
        btnCancelar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnCancelar.setOnAction(e -> close());

        actions.getChildren().addAll(btnCancelar, btnConfirmar);

        root.getChildren().addAll(header, grid, lblStatus, actions);
        return root;
    }

    private void cargarZonas() {
        new Thread(() -> {
            try {
                List<com.logistics.packinglist.model.WarehouseZoneModel> zones = service.obtenerZonasPorBodega(fromLocation.getWarehouseId());
                Platform.runLater(() -> {
                    zoneMap.clear();
                    for (com.logistics.packinglist.model.WarehouseZoneModel z : zones) {
                        zoneMap.put(z.getId(), z);
                    }
                    actualizarFiltroZonas();
                    actualizarFiltroUbicaciones();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void actualizarFiltroZonas() {
        filteredZones.clear();
        
        com.logistics.packinglist.model.WarehouseZoneModel allZonesItem = new com.logistics.packinglist.model.WarehouseZoneModel();
        allZonesItem.setId(null);
        allZonesItem.setNombre("--- Todas las Zonas ---");
        filteredZones.add(allZonesItem);

        List<com.logistics.packinglist.model.WarehouseZoneModel> matchingZones = new java.util.ArrayList<>();
        for (com.logistics.packinglist.model.WarehouseZoneModel z : zoneMap.values()) {
            String ambito = determinarAmbitoUbicacion(z.getId());
            if (tipoStock.equalsIgnoreCase(ambito)) {
                matchingZones.add(z);
            }
        }

        matchingZones.sort((z1, z2) -> {
            String n1 = z1.getNombre() != null ? z1.getNombre() : "";
            String n2 = z2.getNombre() != null ? z2.getNombre() : "";
            LocationModel l1 = new LocationModel();
            l1.setCodigoUbicacion(n1);
            LocationModel l2 = new LocationModel();
            l2.setCodigoUbicacion(n2);
            return LocationModel.NATURAL_ORDER_COMPARATOR.compare(l1, l2);
        });

        filteredZones.addAll(matchingZones);
        cbZona.setValue(allZonesItem);
    }

    private boolean esZonaOAncestro(String zoneId, String targetZoneId) {
        if (targetZoneId == null) {
            return false;
        }
        String currentId = zoneId;
        while (currentId != null) {
            if (currentId.equals(targetZoneId)) {
                return true;
            }
            com.logistics.packinglist.model.WarehouseZoneModel zone = zoneMap.get(currentId);
            if (zone == null) {
                break;
            }
            currentId = zone.getParentZoneId();
        }
        return false;
    }

    private void actualizarFiltroUbicaciones() {
        filteredDestLocations.clear();

        com.logistics.packinglist.model.WarehouseZoneModel rawZone = cbZona.getValue();
        String selectedZoneId = (rawZone != null) ? rawZone.getId() : null;

        List<LocationModel> matchingLocs = new java.util.ArrayList<>();
        for (LocationModel l : destLocations) {
            if (selectedZoneId == null || esZonaOAncestro(l.getZoneId(), selectedZoneId)) {
                matchingLocs.add(l);
            }
        }
        matchingLocs.sort(LocationModel.NATURAL_ORDER_COMPARATOR);
        filteredDestLocations.addAll(matchingLocs);
        cbDestino.getSelectionModel().clearSelection();
    }

    private String determinarAmbitoUbicacion(String zoneId) {
        if (zoneId == null) {
            return "COMERCIAL";
        }
        String currentId = zoneId;
        while (currentId != null) {
            com.logistics.packinglist.model.WarehouseZoneModel zone = zoneMap.get(currentId);
            if (zone == null) {
                break;
            }
            String ambito = zone.getAmbitoStock();
            if (ambito != null && !ambito.trim().isEmpty()) {
                return ambito.toUpperCase();
            }
            currentId = zone.getParentZoneId();
        }
        return "COMERCIAL";
    }

    private <T> void configurarComboBoxSearchGeneric(ComboBox<T> comboBox, ObservableList<T> itemsOriginales) {
        comboBox.setEditable(true);

        comboBox.setConverter(new javafx.util.StringConverter<T>() {
            @Override
            public String toString(T object) {
                return object != null ? object.toString() : "";
            }

            @Override
            public T fromString(String string) {
                if (string == null || string.trim().isEmpty()) return null;
                String text = string.trim();
                for (T item : itemsOriginales) {
                    if (item != null && item.toString().equalsIgnoreCase(text)) {
                        return item;
                    }
                }
                return null;
            }
        });

        FilteredList<T> filteredList = new FilteredList<>(itemsOriginales, p -> true);
        comboBox.setItems(filteredList);

        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Platform.runLater(() -> {
                    if (comboBox.getEditor() != null) {
                        comboBox.getEditor().setText(newVal.toString());
                    }
                });
            }
        });

        comboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (comboBox.getSelectionModel().getSelectedItem() != null 
                    && comboBox.getSelectionModel().getSelectedItem().toString().equals(newText)) {
                return;
            }

            if (newText == null || newText.trim().isEmpty()) {
                filteredList.setPredicate(p -> true);
            } else {
                String filterText = newText.toLowerCase().trim();
                filteredList.setPredicate(item -> item != null && item.toString().toLowerCase().contains(filterText));
            }

            if (!comboBox.isShowing() && comboBox.getEditor().isFocused() && !filteredList.isEmpty() && newText != null && !newText.trim().isEmpty()) {
                comboBox.show();
            }
        });

        comboBox.getEditor().setOnMouseClicked(e -> {
            if (!comboBox.isShowing()) {
                comboBox.show();
            }
        });
    }

    private void confirmarReubicacion() {
        LocationModel selectedDest = cbDestino.getValue();

        if (selectedDest == null && cbDestino.getEditor().getText() != null) {
            String text = cbDestino.getEditor().getText().trim();
            for (LocationModel item : destLocations) {
                if (item.toString().equalsIgnoreCase(text)) {
                    selectedDest = item;
                    break;
                }
            }
        }

        if (selectedDest == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Debe seleccionar una ubicación de destino válida.");
            return;
        }

        String cantStr = txtCantidad.getText().trim();
        int cant;
        try {
            cant = Integer.parseInt(cantStr);
            if (cant <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "La cantidad debe ser un número entero mayor que 0.");
            return;
        }

        if (cant > currentMaxQuantity) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "La cantidad ingresada (" + cant + ") supera la cantidad disponible (" + currentMaxQuantity + ").");
            return;
        }

        // Ejecutar petición en background
        btnConfirmar.setDisable(true);
        btnCancelar.setDisable(true);
        cbZona.setDisable(true);
        cbDestino.setDisable(true);
        txtCantidad.setDisable(true);
        lblStatus.setText("Procesando reubicación física...");

        final LocationModel finalDest = selectedDest;
        new Thread(() -> {
            try {
                service.transferirStock(product.getId(), fromLocation.getId(), finalDest.getId(), cant, tipoStock);
                Platform.runLater(() -> {
                    currentMaxQuantity -= cant;
                    lblDispV.setText(currentMaxQuantity + " unidades");
                    txtCantidad.clear();
                    cbDestino.setValue(null);
                    if (cbDestino.getEditor() != null) {
                        cbDestino.getEditor().clear();
                    }

                    if (getOwner() instanceof StockDialog) {
                        ((StockDialog) getOwner()).cargarDatos();
                    } else if (getOwner() instanceof InventarioVisualDialog) {
                        ((InventarioVisualDialog) getOwner()).refrescarDatosDespuesDeReubicacion();
                    }

                    if (currentMaxQuantity <= 0) {
                        btnConfirmar.setDisable(true);
                        btnCancelar.setDisable(false);
                        cbZona.setDisable(true);
                        cbDestino.setDisable(true);
                        txtCantidad.setDisable(true);
                        lblStatus.setText("Todo el stock de esta ubicación de origen ha sido reubicado.");
                    } else {
                        btnConfirmar.setDisable(false);
                        btnCancelar.setDisable(false);
                        cbZona.setDisable(false);
                        cbDestino.setDisable(false);
                        txtCantidad.setDisable(false);
                        txtCantidad.setPromptText("Ej. " + currentMaxQuantity);
                        lblStatus.setText("Stock transferido exitosamente (" + cant + " un. a " + finalDest.toString() + ").");
                    }

                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Se transfirieron " + cant + " unidades a " + finalDest.toString() + " exitosamente.\nStock restante en origen: " + currentMaxQuantity + " un.");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    btnConfirmar.setDisable(false);
                    btnCancelar.setDisable(false);
                    cbZona.setDisable(false);
                    cbDestino.setDisable(false);
                    txtCantidad.setDisable(false);
                    lblStatus.setText("");
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al transferir stock: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.initOwner(this);
        alert.showAndWait();
    }
}
