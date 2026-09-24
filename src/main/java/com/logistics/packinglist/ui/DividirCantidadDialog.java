package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.LocationModel;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.logistics.packinglist.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

public class DividirCantidadDialog extends Stage {

    public static class PartitionRow {
        private int cantidad;
        private LocationModel ubicacion;

        public PartitionRow(int cantidad, LocationModel ubicacion) {
            this.cantidad = cantidad;
            this.ubicacion = ubicacion;
        }

        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public LocationModel getUbicacion() { return ubicacion; }
        public void setUbicacion(LocationModel ubicacion) { this.ubicacion = ubicacion; }
    }

    private final String skuInfo;
    private final int totalCantidad;
    private final List<LocationModel> availableLocations;
    private List<PartitionRow> result = null;

    private Spinner<Integer> spnPartes;
    private TableView<PartitionRow> tblSplits;
    private ObservableList<PartitionRow> listSplits;
    private Label lblSum;

    public DividirCantidadDialog(Window owner, String skuInfo, int totalCantidad, List<LocationModel> availableLocations) {
        this.skuInfo = skuInfo;
        this.totalCantidad = totalCantidad;
        this.availableLocations = availableLocations != null ? availableLocations : new ArrayList<>();

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("División Equitativa / Personalizada de Item");

        setMinWidth(650);
        setMinHeight(500);

        construirUI();
        generarParticionEquitativa(2);
    }

    private void construirUI() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header
        Label lblTitle = new Label("División de Cantidad Recepcionada");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");
        Label lblSub = new Label("Producto: " + skuInfo + " | Cantidad Total a Recibir: " + totalCantidad);
        lblSub.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        VBox headerBox = new VBox(4, lblTitle, lblSub);

        // Control de División Equitativa
        HBox divBox = new HBox(12);
        divBox.setAlignment(Pos.CENTER_LEFT);
        divBox.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-background-radius: 6px; -fx-border-color: #e2e8f0;");

        Label lblPartes = new Label("N° de Partes Equitativas:");
        lblPartes.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 9.5px;");

        spnPartes = new Spinner<>(2, 20, 2);
        spnPartes.setPrefWidth(80);
        spnPartes.setEditable(true);

        FontAwesomeIconView iconMagic = new FontAwesomeIconView(FontAwesomeIcon.MAGIC);
        iconMagic.setFill(Color.WHITE);
        Button btnGenerar = new Button(" Dividir Equitativamente", iconMagic);
        btnGenerar.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4px;");
        btnGenerar.setOnAction(e -> generarParticionEquitativa(spnPartes.getValue()));

        divBox.getChildren().addAll(lblPartes, spnPartes, btnGenerar);

        // Tabla de Particiones
        tblSplits = new TableView<>();
        tblSplits.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblSplits.setStyle("-fx-background-radius: 6px; -fx-border-color: #e2e8f0;");

        TableColumn<PartitionRow, String> colPart = new TableColumn<>("Partición");
        colPart.setCellValueFactory(c -> new SimpleStringProperty("Parte #" + (tblSplits.getItems().indexOf(c.getValue()) + 1)));
        colPart.setPrefWidth(90);

        TableColumn<PartitionRow, Integer> colCant = new TableColumn<>("Cantidad");
        colCant.setPrefWidth(120);
        colCant.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 99999, 1);
            {
                spinner.setEditable(true);
                spinner.setPrefWidth(100);
                spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (getItem() != null && getTableRow() != null && getTableRow().getItem() != null) {
                        getTableRow().getItem().setCantidad(newVal);
                        actualizarSumaTotal();
                    }
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    spinner.getValueFactory().setValue(getTableRow().getItem().getCantidad());
                    setGraphic(spinner);
                }
            }
        });

        TableColumn<PartitionRow, LocationModel> colUbic = new TableColumn<>("Ubicación Destino");
        colUbic.setPrefWidth(220);
        colUbic.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<LocationModel> cbLoc = new ComboBox<>();
            {
                cbLoc.setItems(FXCollections.observableArrayList(availableLocations));
                cbLoc.setPromptText("Seleccione Ubicación");
                cbLoc.setMaxWidth(Double.MAX_VALUE);
                cbLoc.setStyle("-fx-background-radius: 4px;");
                cbLoc.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        getTableRow().getItem().setUbicacion(newVal);
                    }
                });
            }

            @Override
            protected void updateItem(LocationModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    cbLoc.setValue(getTableRow().getItem().getUbicacion());
                    setGraphic(cbLoc);
                }
            }
        });

        tblSplits.getColumns().addAll(colPart, colCant, colUbic);
        VBox.setVgrow(tblSplits, Priority.ALWAYS);

        listSplits = FXCollections.observableArrayList();
        tblSplits.setItems(listSplits);

        // Suma acumulada y footer
        lblSum = new Label("Suma asignada: 0 / " + totalCantidad);
        lblSum.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        FontAwesomeIconView iconCancel = new FontAwesomeIconView(FontAwesomeIcon.TIMES);
        iconCancel.setFill(Color.WHITE);
        Button btnCancelar = new Button(null, iconCancel);
        btnCancelar.setStyle("-fx-background-color: #6c757d; -fx-padding: 6px 14px; -fx-background-radius: 4px; -fx-cursor: hand;");
        btnCancelar.setTooltip(new Tooltip("Cancelar"));
        btnCancelar.setOnAction(e -> {
            result = null;
            close();
        });

        FontAwesomeIconView iconCheck = new FontAwesomeIconView(FontAwesomeIcon.CHECK);
        iconCheck.setFill(Color.WHITE);
        Button btnAceptar = new Button(null, iconCheck);
        btnAceptar.setStyle("-fx-background-color: #28a745; -fx-padding: 6px 14px; -fx-background-radius: 4px; -fx-cursor: hand;");
        btnAceptar.setTooltip(new Tooltip("Aplicar División"));
        btnAceptar.setOnAction(e -> aplicarParticion());

        footer.getChildren().addAll(lblSum, new Region(), btnCancelar, btnAceptar);
        HBox.setHgrow(footer.getChildren().get(1), Priority.ALWAYS);

        root.getChildren().addAll(headerBox, divBox, tblSplits, footer);

        Scene scene = new Scene(root, 650, 480);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception ignored) {}
        setScene(scene);
        ScreenUtil.fitDialogToScreen(this, getOwner(), 650, 480, 600, 420);
    }

    private void generarParticionEquitativa(int numPartes) {
        if (numPartes <= 1) numPartes = 2;
        listSplits.clear();

        int base = totalCantidad / numPartes;
        int residuo = totalCantidad % numPartes;

        for (int i = 0; i < numPartes; i++) {
            int qty = base + (i == numPartes - 1 ? residuo : 0);
            LocationModel defaultLoc = (i < availableLocations.size()) ? availableLocations.get(i) : null;
            listSplits.add(new PartitionRow(qty, defaultLoc));
        }

        actualizarSumaTotal();
    }

    private void actualizarSumaTotal() {
        int sum = 0;
        for (PartitionRow r : listSplits) {
            sum += r.getCantidad();
        }
        lblSum.setText("Suma asignada: " + sum + " / " + totalCantidad);
        if (sum == totalCantidad) {
            lblSum.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
        } else {
            lblSum.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
        }
    }

    private void aplicarParticion() {
        int sum = 0;
        for (PartitionRow r : listSplits) {
            if (r.getCantidad() <= 0) {
                mostrarError("Validación", "Todas las particiones deben tener una cantidad mayor a 0.");
                return;
            }
            sum += r.getCantidad();
        }

        if (sum != totalCantidad) {
            mostrarError("Validación", "La suma de las particiones (" + sum + ") debe coincidir con la cantidad total (" + totalCantidad + ").");
            return;
        }

        result = new ArrayList<>(listSplits);
        close();
    }

    public List<PartitionRow> showAndGetResult() {
        showAndWait();
        return result;
    }

    private void mostrarError(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
