package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import com.logistics.packinglist.model.InventarioItem;
import com.logistics.packinglist.model.PackingItem;
import com.logistics.packinglist.service.InventarioService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class SeleccionProductoDialog extends Stage {
    private final InventarioService service;
    private PackingItem resultado = null;

    public SeleccionProductoDialog(Window owner, InventarioService service) {
        this.service = service;

        // initOwner(owner); // Comentado para evitar bug de X11/Mutter en Linux que
        // achica el padre
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Seleccionar Producto");
        setResizable(false);
        setMinWidth(600);
        setMinHeight(500);

        construirUI();
    }

    private void construirUI() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f5f7fb;");

        ObservableList<InventarioItem> items = FXCollections.observableArrayList(service.getItems());
        FilteredList<InventarioItem> filtered = new FilteredList<>(items, p -> true);

        TextField txtBusqueda = new TextField();
        txtBusqueda.setPromptText("Buscar producto...");
        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            filtered.setPredicate(item -> {
                if (newVal == null || newVal.isEmpty())
                    return true;
                String lower = newVal.toLowerCase();
                String simplificado = service.simplificarSKU(newVal);

                return service.simplificarSKU(item.getPartNumber()).contains(simplificado)
                        || item.getDescripcion().toLowerCase().contains(lower);
            });
        });

        TableView<InventarioItem> tabla = new TableView<>();
        tabla.setItems(filtered);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<InventarioItem, String> colSku = new TableColumn<>("SKU");
        colSku.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPartNumber()));
        colSku.setPrefWidth(120);

        TableColumn<InventarioItem, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));

        tabla.getColumns().addAll(colSku, colDesc);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TextField txtCantidad = new TextField();
        txtCantidad.setPromptText("Cant.");
        txtCantidad.setPrefWidth(80);
        // Only allow numbers
        txtCantidad.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtCantidad.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        Button btnAceptar = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.SAVE));
        btnAceptar.getStyleClass().add("btn-guardar");
        btnAceptar.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-font-weight: bold;");

        Runnable accionAgregar = () -> {
            InventarioItem seleccionado = tabla.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                alerta("Debe seleccionar un producto.");
                return;
            }
            if (txtCantidad.getText().trim().isEmpty()) {
                alerta("Debe ingresar la cantidad.");
                return;
            }
            int cant = Integer.parseInt(txtCantidad.getText().trim());
            if (cant <= 0) {
                alerta("La cantidad debe ser mayor a 0.");
                return;
            }
            resultado = new PackingItem(seleccionado.getPartNumber(), seleccionado.getDescripcion(), cant);
            close();
        };

        btnAceptar.setOnAction(e -> accionAgregar.run());

        // Atajo teclado
        tabla.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                txtCantidad.requestFocus();
            }
        });
        txtCantidad.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                accionAgregar.run();
            }
        });

        HBox bottomBox = new HBox(10, new Label("Catidad:"), txtCantidad, btnAceptar);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(new Label("Buscar producto:"), txtBusqueda, tabla, bottomBox);

        Scene scene = new Scene(root, 600, 500);
        setScene(scene);
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public PackingItem getResultado() {
        return resultado;
    }
}
