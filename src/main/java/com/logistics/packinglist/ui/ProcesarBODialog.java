package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.BackOrderModel;
import com.logistics.packinglist.model.BackOrderDetailModel;
import com.logistics.packinglist.model.OrderNoteModel;
import com.logistics.packinglist.model.ProductModel;
import com.logistics.packinglist.service.MantenimientoService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.HashMap;
import java.util.Map;

public class ProcesarBODialog extends Stage {
    private final MantenimientoService service;
    private final BackOrderModel boNote;
    private final OrderNoteModel parentNP;
    private final Map<String, ProductModel> productMap;
    private final Map<String, Integer> availableStockMap;
    private final Runnable onSuccessCallback;

    private TableView<ProcessRow> tablaAsignacion;
    private final ObservableList<ProcessRow> processList = FXCollections.observableArrayList();

    public ProcesarBODialog(Window owner, MantenimientoService service, BackOrderModel boNote, OrderNoteModel parentNP, Map<String, ProductModel> productMap, Map<String, Integer> availableStockMap, Runnable onSuccessCallback) {
        this.service = service;
        this.boNote = boNote;
        this.parentNP = parentNP;
        this.productMap = productMap;
        this.availableStockMap = availableStockMap;
        this.onSuccessCallback = onSuccessCallback;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Procesar Back Order " + (boNote != null ? boNote.getFolio() : "") + " ➔ Crear Nueva NP");

        setMinWidth(850);
        setMinHeight(550);

        construirUI();
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Cabecera
        VBox header = new VBox(4);
        Label lblTitle = new Label("Convertir Back Order a Nueva Nota de Pedido", new FontAwesomeIconView(FontAwesomeIcon.EXCHANGE));
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        String origFolio = parentNP != null ? parentNP.getFolio() : "-";
        Label lblSub = new Label("Folio BO: " + (boNote != null ? boNote.getFolio() : "") + " | Folio NP Original: " + origFolio);
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        header.getChildren().addAll(lblTitle, lblSub);

        // Cargar filas
        if (boNote != null && boNote.getDetails() != null) {
            for (BackOrderDetailModel det : boNote.getDetails()) {
                ProductModel p = productMap.get(det.getProductId());
                String sku = p != null ? p.getSku() : "-";
                String nom = p != null ? p.getNombre() : "Producto Desconocido";
                int pendBO = det.getCantidadPendiente() != null ? det.getCantidadPendiente() : 0;
                int stockDisp = availableStockMap != null ? availableStockMap.getOrDefault(det.getProductId(), 0) : 0;
                int defaultAssign = Math.min(pendBO, stockDisp);

                processList.add(new ProcessRow(det.getProductId(), sku, nom, pendBO, stockDisp, defaultAssign));
            }
        }

        // Tabla de asignación
        tablaAsignacion = new TableView<>(processList);
        tablaAsignacion.setEditable(true);
        tablaAsignacion.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaAsignacion.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        VBox.setVgrow(tablaAsignacion, Priority.ALWAYS);

        TableColumn<ProcessRow, String> colSku = new TableColumn<>("SKU");
        colSku.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSku()));
        colSku.setPrefWidth(100);

        TableColumn<ProcessRow, String> colNom = new TableColumn<>("Producto");
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colNom.setPrefWidth(220);

        TableColumn<ProcessRow, Integer> colPend = new TableColumn<>("Pendiente en BO");
        colPend.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPendienteBO()));
        colPend.setPrefWidth(120);

        TableColumn<ProcessRow, Integer> colDisp = new TableColumn<>("Stock Disponible");
        colDisp.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getStockDisponible()));
        colDisp.setPrefWidth(120);

        TableColumn<ProcessRow, Integer> colAssign = new TableColumn<>("Cantidad a Asignar a NP");
        colAssign.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCantidadAsignada()));
        colAssign.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(0, 9999, 0);

            {
                spinner.setEditable(true);
                spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        ProcessRow row = getTableRow().getItem();
                        row.setCantidadAsignada(newVal != null ? newVal : 0);
                    }
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    ProcessRow row = getTableRow().getItem();
                    SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, row.getPendienteBO(), item != null ? item : 0);
                    spinner.setValueFactory(factory);
                    setGraphic(spinner);
                }
            }
        });
        colAssign.setPrefWidth(160);

        tablaAsignacion.getColumns().addAll(colSku, colNom, colPend, colDisp, colAssign);

        // Botones de acción
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancelar = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.TIMES));
        btnCancelar.getStyleClass().add("btn-cancelar");
        btnCancelar.setOnAction(e -> close());

        Button btnGenerarNP = new Button("Generar Nueva NP", new FontAwesomeIconView(FontAwesomeIcon.CHECK));
        btnGenerarNP.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnGenerarNP.setOnAction(e -> procesarGeneracionNP());

        actions.getChildren().addAll(btnCancelar, btnGenerarNP);

        root.getChildren().addAll(header, tablaAsignacion, actions);

        Scene scene = new Scene(root, 850, 550);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception ignored) {}
        setScene(scene);
    }

    private void procesarGeneracionNP() {
        Map<String, Integer> qtyMap = new HashMap<>();
        int totalQty = 0;
        for (ProcessRow r : processList) {
            if (r.getCantidadAsignada() > 0) {
                qtyMap.put(r.getProductId(), r.getCantidadAsignada());
                totalQty += r.getCantidadAsignada();
            }
        }

        if (totalQty <= 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Debe indicar al menos 1 unidad a asignar para crear la nueva Nota de Pedido.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        new Thread(() -> {
            try {
                OrderNoteModel newNP = service.procesarBackOrder(boNote.getId(), qtyMap);
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Nueva Nota de Pedido " + newNP.getFolio() + " generada exitosamente desde el Back Order " + boNote.getFolio() + ".", ButtonType.OK);
                    alert.setTitle("Éxito");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    if (onSuccessCallback != null) onSuccessCallback.run();
                    close();
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error al procesar el Back Order: " + e.getMessage(), ButtonType.OK);
                    alert.showAndWait();
                });
            }
        }).start();
    }

    public static class ProcessRow {
        private final String productId;
        private final String sku;
        private final String nombre;
        private final int pendienteBO;
        private final int stockDisponible;
        private int cantidadAsignada;

        public ProcessRow(String productId, String sku, String nombre, int pendienteBO, int stockDisponible, int cantidadAsignada) {
            this.productId = productId;
            this.sku = sku;
            this.nombre = nombre;
            this.pendienteBO = pendienteBO;
            this.stockDisponible = stockDisponible;
            this.cantidadAsignada = cantidadAsignada;
        }

        public String getProductId() { return productId; }
        public String getSku() { return sku; }
        public String getNombre() { return nombre; }
        public int getPendienteBO() { return pendienteBO; }
        public int getStockDisponible() { return stockDisponible; }
        public int getCantidadAsignada() { return cantidadAsignada; }
        public void setCantidadAsignada(int cantidadAsignada) { this.cantidadAsignada = cantidadAsignada; }
    }
}
