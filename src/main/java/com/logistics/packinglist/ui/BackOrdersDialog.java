package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.BackOrderModel;
import com.logistics.packinglist.model.BackOrderDetailModel;
import com.logistics.packinglist.model.BackOrderHistoryModel;
import com.logistics.packinglist.model.CustomerModel;
import com.logistics.packinglist.model.ProductModel;
import com.logistics.packinglist.model.WarehouseModel;
import com.logistics.packinglist.model.LocationModel;
import com.logistics.packinglist.model.StockModel;
import com.logistics.packinglist.model.OrderNoteModel;
import com.logistics.packinglist.service.MantenimientoService;
import com.logistics.packinglist.service.WebSocketManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Diálogo para visualizar y gestionar las cantidades pendientes (Back Orders) de las Notas de Pedido.
 * Implementa un diseño premium simétrico de proporción 50/50 y soporte para actualizaciones en tiempo real.
 */
public class BackOrdersDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    private final ObservableList<BackOrderModel> allBOsList;
    private final FilteredList<BackOrderModel> filteredBOsList;

    private TableView<BackOrderModel> tablaBOs;
    private TextField txtSearch;
    private ComboBox<String> cbFiltroEstado;
    private Button btnAnularBO;
    private Button btnProcesarBO;

    // Selected BO details labels
    private Label lblFolio;
    private Label lblCliente;
    private Label lblBodega;
    private Label lblFecha;
    private Label lblEstado;
    private Label lblZonaReserva;

    private TableView<BackOrderDetailModel> tablaDetalles;
    private final ObservableList<BackOrderDetailModel> detailList;

    private TableView<BackOrderHistoryModel> tablaHistorial;
    private final ObservableList<BackOrderHistoryModel> historyList;

    private final Map<String, CustomerModel> customerMap = new HashMap<>();
    private final Map<String, ProductModel> productMap = new HashMap<>();
    private final Map<String, LocationModel> locationMap = new HashMap<>();
    private final Map<String, WarehouseModel> warehouseMap = new HashMap<>();
    private final Map<String, OrderNoteModel> orderNoteMap = new HashMap<>();
    private final List<StockModel> rawStockList = new ArrayList<>();

    public BackOrdersDialog(Window owner) {
        this.allBOsList = FXCollections.observableArrayList();
        this.filteredBOsList = new FilteredList<>(allBOsList, p -> true);
        this.detailList = FXCollections.observableArrayList();
        this.historyList = FXCollections.observableArrayList();

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Gestión de Back Orders");

        setMinWidth(1280);
        setMinHeight(750);
        setWidth(1280);
        setHeight(750);

        construirUI();
        cargarDatos();

        // Registrar WebSocket listeners
        WebSocketManager.UpdateListener orderNoteListener = action -> {
            System.out.println("[BackOrdersDialog] Received real-time update event: " + action);
            javafx.application.Platform.runLater(this::cargarDatos);
        };
        WebSocketManager.getInstance().subscribe("ORDER_NOTE", orderNoteListener);

        WebSocketManager.UpdateListener backOrderListener = action -> {
            System.out.println("[BackOrdersDialog] Received back order update event: " + action);
            javafx.application.Platform.runLater(this::cargarDatos);
        };
        WebSocketManager.getInstance().subscribe("BACK_ORDER", backOrderListener);

        WebSocketManager.UpdateListener stockUpdateListener = action -> {
            System.out.println("[BackOrdersDialog] Received stock update event: " + action);
            javafx.application.Platform.runLater(this::cargarDatos);
        };
        WebSocketManager.getInstance().subscribe("STOCK", stockUpdateListener);

        setOnCloseRequest(e -> {
            WebSocketManager.getInstance().unsubscribe("ORDER_NOTE", orderNoteListener);
            WebSocketManager.getInstance().unsubscribe("BACK_ORDER", backOrderListener);
            WebSocketManager.getInstance().unsubscribe("STOCK", stockUpdateListener);
        });
    }

    private void construirUI() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Cabecera superior
        VBox headerBox = new VBox(6);
        headerBox.setPadding(new Insets(0, 0, 15, 0));
        Label lblTitle = new Label("Gestión de Back Orders");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Monitoreo y administración de pedidos pendientes de stock por zona de reserva.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);
        root.setTop(headerBox);

        // Contenedor principal 50/50
        SplitPane splitMain = new SplitPane();
        VBox.setVgrow(splitMain, Priority.ALWAYS);
        splitMain.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");

        // --- LADO IZQUIERDO: LISTADO ---
        VBox leftBox = new VBox(10);
        leftBox.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 12px;");
        GridPane.setVgrow(leftBox, Priority.ALWAYS);

        Label lblListTitle = new Label("Documentos Pendientes (BO-*)");
        lblListTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        txtSearch = new TextField();
        txtSearch.setPromptText("Buscar por Folio, Cliente o Fecha...");
        txtSearch.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        txtSearch.setMaxWidth(180);

        cbFiltroEstado = new ComboBox<>(FXCollections.observableArrayList("Todos", "Pendientes", "Parciales", "Procesados (Completados)", "Anulados"));
        cbFiltroEstado.setValue("Todos");
        cbFiltroEstado.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        // Helper to update filtered list
        Runnable updateFiltros = () -> {
            String query = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
            String estadoFiltro = cbFiltroEstado.getValue();
            filteredBOsList.setPredicate(bo -> {
                if (!query.isEmpty()) {
                    boolean matchesText = (bo.getFolio() != null && bo.getFolio().toLowerCase().contains(query));
                    CustomerModel cli = customerMap.get(bo.getCustomerId());
                    if (cli != null && cli.getRazonSocial().toLowerCase().contains(query)) {
                        matchesText = true;
                    }
                    if (bo.getFecha() != null && bo.getFecha().toLowerCase().contains(query)) {
                        matchesText = true;
                    }
                    if (!matchesText) return false;
                }

                if (estadoFiltro != null && !"Todos".equalsIgnoreCase(estadoFiltro)) {
                    String est = bo.getEstado();
                    if ("Pendientes".equalsIgnoreCase(estadoFiltro)) {
                        return "PENDIENTE".equalsIgnoreCase(est);
                    } else if ("Parciales".equalsIgnoreCase(estadoFiltro)) {
                        return "PARCIAL".equalsIgnoreCase(est);
                    } else if ("Procesados (Completados)".equalsIgnoreCase(estadoFiltro)) {
                        return "PROCESADO".equalsIgnoreCase(est);
                    } else if ("Anulados".equalsIgnoreCase(estadoFiltro)) {
                        return "ANULADO".equalsIgnoreCase(est);
                    }
                }
                return true;
            });
        };

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> updateFiltros.run());
        cbFiltroEstado.valueProperty().addListener((obs, oldVal, newVal) -> updateFiltros.run());

        HBox filterBox = new HBox(8);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        filterBox.getChildren().addAll(txtSearch, cbFiltroEstado);

        tablaBOs = new TableView<>(filteredBOsList);
        tablaBOs.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaBOs.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<BackOrderModel, String> colBOFolio = new TableColumn<>("Folio BO");
        colBOFolio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolio()));
        colBOFolio.setPrefWidth(120);

        TableColumn<BackOrderModel, String> colBOOrig = new TableColumn<>("Folio NP Original");
        colBOOrig.setCellValueFactory(c -> {
            OrderNoteModel parent = orderNoteMap.get(c.getValue().getOrderNoteId());
            return new SimpleStringProperty(parent != null ? parent.getFolio() : "-");
        });
        colBOOrig.setPrefWidth(120);

        TableColumn<BackOrderModel, String> colBOCliente = new TableColumn<>("Cliente");
        colBOCliente.setCellValueFactory(c -> {
            CustomerModel cli = customerMap.get(c.getValue().getCustomerId());
            return new SimpleStringProperty(cli != null ? cli.getRazonSocial() : "-");
        });
        colBOCliente.setPrefWidth(180);

        TableColumn<BackOrderModel, String> colBOFecha = new TableColumn<>("Fecha");
        colBOFecha.setCellValueFactory(c -> {
            String f = c.getValue().getFecha();
            return new SimpleStringProperty(f != null ? f.replace("T", " ") : "-");
        });
        colBOFecha.setPrefWidth(140);

        TableColumn<BackOrderModel, String> colBOEstado = new TableColumn<>("Estado");
        colBOEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
        colBOEstado.setPrefWidth(90);

        // Apply visual styling policy
        StatusColumnHelper.applyStatusStyling(colBOFolio, true);
        StatusColumnHelper.applyStatusStyling(colBOEstado, false);
        StatusColumnHelper.applyRowFactory(tablaBOs);

        tablaBOs.getColumns().addAll(colBOFolio, colBOOrig, colBOCliente, colBOFecha, colBOEstado);
        VBox.setVgrow(tablaBOs, Priority.ALWAYS);

        leftBox.getChildren().addAll(lblListTitle, filterBox, tablaBOs);
        

        // --- LADO DERECHO: DETALLES ---
        VBox rightBox = new VBox(12);
        rightBox.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 12px;");
        GridPane.setVgrow(rightBox, Priority.ALWAYS);

        Label lblDetailTitle = new Label("Detalle del Documento");
        lblDetailTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        // Info Grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(8);

        lblFolio = new Label("-");
        lblFolio.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        lblCliente = new Label("-");
        lblCliente.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        lblBodega = new Label("-");
        lblBodega.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        lblFecha = new Label("-");
        lblFecha.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        lblEstado = new Label("-");
        lblEstado.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        lblZonaReserva = new Label("-");
        lblZonaReserva.setStyle("-fx-font-weight: bold; -fx-text-fill: #16a34a;");

        Label l1 = new Label("Folio BO:"); l1.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label l2 = new Label("Cliente:"); l2.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        infoGrid.add(l1, 0, 0);
        infoGrid.add(lblFolio, 1, 0);
        infoGrid.add(l2, 2, 0);
        infoGrid.add(lblCliente, 3, 0);

        Label l3 = new Label("Bodega:"); l3.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label l4 = new Label("Zona Reserva:"); l4.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        infoGrid.add(l3, 0, 1);
        infoGrid.add(lblBodega, 1, 1);
        infoGrid.add(l4, 2, 1);
        infoGrid.add(lblZonaReserva, 3, 1);

        Label l5 = new Label("Fecha:"); l5.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label l6 = new Label("Estado BO:"); l6.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        infoGrid.add(l5, 0, 2);
        infoGrid.add(lblFecha, 1, 2);
        infoGrid.add(l6, 2, 2);
        infoGrid.add(lblEstado, 3, 2);

        // Column Constraints for infoGrid
        ColumnConstraints ic1 = new ColumnConstraints(90);
        ColumnConstraints ic2 = new ColumnConstraints(150);
        ColumnConstraints ic3 = new ColumnConstraints(100);
        ColumnConstraints ic4 = new ColumnConstraints(220);
        infoGrid.getColumnConstraints().addAll(ic1, ic2, ic3, ic4);

        // Tabla Detalles
        tablaDetalles = new TableView<>(detailList);
        tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaDetalles.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        tablaDetalles.setFixedCellSize(28);
        tablaDetalles.prefHeightProperty().bind(javafx.beans.binding.Bindings.max(1, javafx.beans.binding.Bindings.size(tablaDetalles.getItems())).multiply(tablaDetalles.getFixedCellSize()).add(30));
        tablaDetalles.minHeightProperty().bind(tablaDetalles.prefHeightProperty());
        tablaDetalles.maxHeightProperty().bind(tablaDetalles.prefHeightProperty());

        TableColumn<BackOrderDetailModel, String> colSku = new TableColumn<>("SKU");
        colSku.setCellValueFactory(c -> {
            ProductModel p = productMap.get(c.getValue().getProductId());
            return new SimpleStringProperty(p != null ? p.getSku() : "-");
        });
        colSku.setPrefWidth(90);

        TableColumn<BackOrderDetailModel, String> colProducto = new TableColumn<>("Producto");
        colProducto.setCellValueFactory(c -> {
            ProductModel p = productMap.get(c.getValue().getProductId());
            return new SimpleStringProperty(p != null ? p.getNombre() : "Producto Desconocido");
        });
        colProducto.setPrefWidth(180);

        TableColumn<BackOrderDetailModel, Integer> colPedida = new TableColumn<>("Original BO");
        colPedida.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCantidadOriginal()));
        colPedida.setPrefWidth(85);

        TableColumn<BackOrderDetailModel, Integer> colDesp = new TableColumn<>("Procesado");
        colDesp.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCantidadProcesada()));
        colDesp.setPrefWidth(90);

        TableColumn<BackOrderDetailModel, Integer> colBack = new TableColumn<>("Pendiente");
        colBack.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCantidadPendiente()));
        colBack.setPrefWidth(95);
        colBack.setCellFactory(column -> new TableCell<BackOrderDetailModel, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    if (item > 0) {
                        setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<BackOrderDetailModel, Integer> colStock = new TableColumn<>("Stock Disp.");
        colStock.setCellValueFactory(c -> {
            BackOrderModel selBO = tablaBOs.getSelectionModel().getSelectedItem();
            if (selBO == null) return new SimpleObjectProperty<>(0);
            OrderNoteModel parent = orderNoteMap.get(selBO.getOrderNoteId());
            if (parent == null) return new SimpleObjectProperty<>(0);
            String zone = parent.getAtributosPersonalizados() != null ? parent.getAtributosPersonalizados().getOrDefault("zona_reserva", "COMERCIAL") : "COMERCIAL";
            int stock = getStockForProductInWarehouseAndZone(c.getValue().getProductId(), parent.getWarehouseId(), zone);
            return new SimpleObjectProperty<>(stock);
        });
        colStock.setPrefWidth(90);
        colStock.setCellFactory(column -> new TableCell<BackOrderDetailModel, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    if (getTableView().getItems().size() > getIndex()) {
                        BackOrderDetailModel row = getTableView().getItems().get(getIndex());
                        int req = row.getCantidadPendiente() != null ? row.getCantidadPendiente() : 0;
                        if (item >= req && req > 0) {
                            setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                        } else if (req > 0) {
                            setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            }
        });

        tablaDetalles.getColumns().addAll(colSku, colProducto, colPedida, colDesp, colBack, colStock);
        

        // Historial
        Label lblHistTitle = new Label("Historial de Procesamiento y Auditoría");
        lblHistTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a; -fx-padding: 10px 0 2px 0;");

        tablaHistorial = new TableView<>(historyList);
        tablaHistorial.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaHistorial.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        tablaHistorial.setFixedCellSize(28);
        tablaHistorial.prefHeightProperty().bind(javafx.beans.binding.Bindings.max(1, javafx.beans.binding.Bindings.size(tablaHistorial.getItems())).multiply(tablaHistorial.getFixedCellSize()).add(30));
        tablaHistorial.minHeightProperty().bind(tablaHistorial.prefHeightProperty());
        tablaHistorial.maxHeightProperty().bind(tablaHistorial.prefHeightProperty());

        TableColumn<BackOrderHistoryModel, String> colHistFecha = new TableColumn<>("Fecha/Hora");
        colHistFecha.setCellValueFactory(c -> new SimpleStringProperty(DateFormatterUtil.format(c.getValue().getFechaHora())));
        colHistFecha.setPrefWidth(140);

        TableColumn<BackOrderHistoryModel, String> colHistNp = new TableColumn<>("NP Generada");
        colHistNp.setCellValueFactory(c -> {
            OrderNoteModel np = orderNoteMap.get(c.getValue().getCreatedNpId());
            return new SimpleStringProperty(np != null ? np.getFolio() : "Cargando...");
        });
        colHistNp.setPrefWidth(120);

        TableColumn<BackOrderHistoryModel, String> colHistDet = new TableColumn<>("Cantidades Procesadas");
        colHistDet.setCellValueFactory(c -> {
            String jsonRaw = c.getValue().getDetallesProcesados();
            if (jsonRaw == null || jsonRaw.isEmpty()) return new SimpleStringProperty("-");
            try {
                int total = 0;
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\":\\s*(\\d+)").matcher(jsonRaw);
                while(m.find()) {
                    total += Integer.parseInt(m.group(1));
                }
                return new SimpleStringProperty(String.valueOf(total));
            } catch(Exception e) {
                return new SimpleStringProperty(jsonRaw);
            }
        });
        colHistDet.setPrefWidth(220);

        tablaHistorial.getColumns().addAll(colHistFecha, colHistNp, colHistDet);

        // Envolver tablas en cajas con borde como en Notas de Pedido
        VBox detailBox = new VBox(8);
        detailBox.setPadding(new Insets(10));
        detailBox.setStyle("-fx-border-color: #cbd5e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-color: white;");
        detailBox.getChildren().add(tablaDetalles);

        VBox histBox = new VBox(8);
        histBox.setPadding(new Insets(10));
        histBox.setStyle("-fx-border-color: #cbd5e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-color: white;");
        histBox.getChildren().add(tablaHistorial);

        rightBox.getChildren().addAll(lblDetailTitle, infoGrid, detailBox, lblHistTitle, histBox);
        
        ScrollPane scrollRight = new ScrollPane(rightBox);
        scrollRight.setFitToWidth(true);
        scrollRight.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        
        splitMain.getItems().addAll(leftBox, scrollRight);
        splitMain.setDividerPositions(0.45);

        root.setCenter(splitMain);
        
        // Barra inferior de acciones (como el dise\u00f1o original)
        HBox bottomBar = new HBox(12);
        bottomBar.setPadding(new Insets(15, 0, 0, 0));
        bottomBar.setAlignment(Pos.CENTER_RIGHT);

        FontAwesomeIconView iconClose = new FontAwesomeIconView(FontAwesomeIcon.CLOSE);
        iconClose.setFill(Color.WHITE);
        iconClose.setSize("16");
        btnAnularBO = new Button(null, iconClose);
        btnAnularBO.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnAnularBO.setOnAction(e -> confirmarAnulacionBO());
        Tooltip.install(btnAnularBO, new Tooltip("Anular Back Order"));

        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSave.setFill(Color.WHITE);
        iconSave.setSize("16");
        btnProcesarBO = new Button(null, iconSave);
        btnProcesarBO.setStyle("-fx-background-color: #0F3E6E; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnProcesarBO.setOnAction(e -> abrirDialogoProcesarBO());
        Tooltip.install(btnProcesarBO, new Tooltip("Procesar BO \u2794 Crear NP"));

        bottomBar.getChildren().addAll(btnProcesarBO, btnAnularBO);
        root.setBottom(bottomBar);

        // Listener de selección de tablaBOs
        tablaBOs.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                mostrarDetalle(newSel);
                String est = newSel.getEstado();
                boolean readOnly = "ANULADO".equalsIgnoreCase(est) || "PROCESADO".equalsIgnoreCase(est);
                btnAnularBO.setDisable(readOnly);
                btnProcesarBO.setDisable(readOnly);
            } else {
                limpiarDetalle();
                btnAnularBO.setDisable(true);
                btnProcesarBO.setDisable(true);
            }
        });

        Scene scene = new Scene(root, 1280, 750);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setScene(scene);
    }

    private void mostrarDetalle(BackOrderModel bo) {
        lblFolio.setText(bo.getFolio());

        CustomerModel cli = customerMap.get(bo.getCustomerId());
        lblCliente.setText(cli != null ? cli.getRazonSocial() : "-");

        OrderNoteModel parent = orderNoteMap.get(bo.getOrderNoteId());
        WarehouseModel wh = parent != null ? warehouseMap.get(parent.getWarehouseId()) : null;
        lblBodega.setText(wh != null ? wh.getNombre() : "-");

        lblFecha.setText(DateFormatterUtil.format(bo.getFecha()));
        lblEstado.setText(bo.getEstado());

        String zone = parent != null && parent.getAtributosPersonalizados() != null ? parent.getAtributosPersonalizados().getOrDefault("zona_reserva", "COMERCIAL") : "COMERCIAL";
        lblZonaReserva.setText(zone);

        detailList.clear();
        if (bo.getDetails() != null) {
            detailList.addAll(bo.getDetails());
        }
        tablaDetalles.refresh();

        // Cargar historial en hilo secundario
        historyList.clear();
        new Thread(() -> {
            try {
                List<BackOrderHistoryModel> hist = service.obtenerHistorialBackOrder(bo.getId());
                javafx.application.Platform.runLater(() -> {
                    historyList.addAll(hist);
                    tablaHistorial.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void limpiarDetalle() {
        lblFolio.setText("-");
        lblCliente.setText("-");
        lblBodega.setText("-");
        lblFecha.setText("-");
        lblEstado.setText("-");
        lblZonaReserva.setText("-");
        detailList.clear();
        tablaDetalles.refresh();
        historyList.clear();
        tablaHistorial.refresh();
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {
                BackOrderModel selectedBo = tablaBOs.getSelectionModel().getSelectedItem();
                String selectedFolio = selectedBo != null ? selectedBo.getFolio() : null;

                List<WarehouseModel> bodegas = service.obtenerBodegas();
                List<CustomerModel> clientes = service.obtenerClientes();
                List<ProductModel> productos = service.obtenerProductos();
                List<LocationModel> ubicaciones = service.obtenerUbicaciones();
                List<StockModel> stocks = service.obtenerStocks();
                List<OrderNoteModel> notas = service.obtenerNotasPedido();
                List<BackOrderModel> backOrders = service.obtenerBackOrders();

                javafx.application.Platform.runLater(() -> {
                    customerMap.clear();
                    for (CustomerModel c : clientes) {
                        customerMap.put(c.getId(), c);
                    }

                    productMap.clear();
                    for (ProductModel p : productos) {
                        productMap.put(p.getId(), p);
                    }

                    locationMap.clear();
                    for (LocationModel l : ubicaciones) {
                        locationMap.put(l.getId(), l);
                    }

                    warehouseMap.clear();
                    for (WarehouseModel w : bodegas) {
                        warehouseMap.put(w.getId(), w);
                    }

                    orderNoteMap.clear();
                    for (OrderNoteModel n : notas) {
                        orderNoteMap.put(n.getId(), n);
                    }

                    rawStockList.clear();
                    rawStockList.addAll(stocks);

                    allBOsList.clear();
                    allBOsList.addAll(backOrders);

                    // Restaurar selección
                    if (selectedFolio != null) {
                        for (BackOrderModel bo : allBOsList) {
                            if (selectedFolio.equals(bo.getFolio())) {
                                tablaBOs.getSelectionModel().select(bo);
                                break;
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error al cargar datos");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private int getStockForProductInWarehouseAndZone(String productId, String warehouseId, String zone) {
        if (warehouseId == null || zone == null) return 0;
        int total = 0;
        for (StockModel s : rawStockList) {
            if (productId.equals(s.getProductId()) && zone.equalsIgnoreCase(s.getTipoStock())) {
                LocationModel loc = locationMap.get(s.getLocationId());
                if (loc != null && warehouseId.equals(loc.getWarehouseId())) {
                    total += s.getCantidad() != null ? s.getCantidad() : 0;
                }
            }
        }
        return total;
    }

    private void confirmarAnulacionBO() {
        BackOrderModel selectedBO = tablaBOs.getSelectionModel().getSelectedItem();
        if (selectedBO == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Debe seleccionar un Back Order para anular.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        if ("PROCESADO".equalsIgnoreCase(selectedBO.getEstado()) || "ANULADO".equalsIgnoreCase(selectedBO.getEstado())) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No se puede anular un Back Order en estado " + selectedBO.getEstado(), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Anulación de Back Order");
        dialog.setHeaderText("Anular Back Order " + selectedBO.getFolio());
        dialog.setContentText("Motivo de anulación:");
        dialog.initOwner(this);

        dialog.showAndWait().ifPresent(motivo -> {
            if (motivo.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Debe ingresar un motivo para anular.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            new Thread(() -> {
                try {
                    service.anularBackOrder(selectedBO.getId(), motivo.trim());
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Back Order anulado exitosamente.", ButtonType.OK);
                        alert.showAndWait();
                        cargarDatos();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Error al anular: " + e.getMessage(), ButtonType.OK);
                        alert.showAndWait();
                    });
                }
            }).start();
        });
    }

    private void abrirDialogoProcesarBO() {
        BackOrderModel selectedBO = tablaBOs.getSelectionModel().getSelectedItem();
        if (selectedBO == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Debe seleccionar un Back Order de la lista para procesar.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        if ("PROCESADO".equalsIgnoreCase(selectedBO.getEstado()) || "ANULADO".equalsIgnoreCase(selectedBO.getEstado())) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Este Back Order está en estado " + selectedBO.getEstado() + " y no se puede procesar.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        OrderNoteModel parent = orderNoteMap.get(selectedBO.getOrderNoteId());
        if (parent == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se encontró la NP original vinculada al Back Order.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // Calcular stock disponible por producto
        Map<String, Integer> availStock = new HashMap<>();
        for (StockModel s : rawStockList) {
            if (s.getProductId() != null) {
                availStock.put(s.getProductId(), availStock.getOrDefault(s.getProductId(), 0) + (s.getCantidad() != null ? s.getCantidad() : 0));
            }
        }

        ProcesarBODialog dialog = new ProcesarBODialog(this, service, selectedBO, parent, productMap, availStock, this::cargarDatos);
        dialog.showAndWait();
    }
}
