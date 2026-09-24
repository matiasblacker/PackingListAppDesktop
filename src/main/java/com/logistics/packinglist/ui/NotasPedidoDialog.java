package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.OrderNoteModel;
import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.SupplierModel;
import com.logistics.packinglist.service.PdfNotaPedidoService;
import javafx.stage.FileChooser;
import java.io.File;
import com.logistics.packinglist.model.OrderNoteDetailModel;
import com.logistics.packinglist.model.CustomerModel;
import com.logistics.packinglist.model.ProductModel;
import com.logistics.packinglist.model.WarehouseModel;
import com.logistics.packinglist.model.LocationModel;
import com.logistics.packinglist.model.ReceptionModel;
import com.logistics.packinglist.model.StockModel;
import com.logistics.packinglist.service.MantenimientoService;
import com.logistics.packinglist.service.WebSocketManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.logistics.packinglist.model.BackOrderModel;

public class NotasPedidoDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

                    private final ObservableList<OrderNoteModel> observableList;
    private final javafx.collections.transformation.FilteredList<OrderNoteModel> filteredList;
    private final ObservableList<OrderNoteDetailModel> detailList;
    private final Map<String, String> tempNoteAttributes = new HashMap<>();

    private TableView<OrderNoteModel> tablaNotas;
    private TableView<OrderNoteDetailModel> tablaDetalles;
    private TextField txtSearch;

    // Campos formulario nota de pedido
    private ComboBox<com.logistics.packinglist.model.DepositModel> cbDeposito;
    private ComboBox<WarehouseModel> cbBodega;
    private TextField txtFolio;
    private AutocompleteComboBox<CustomerModel> cbCliente;
    private ComboBox<String> cbEstado;
    private ComboBox<String> cbZonaReserva;
    private Label lblFechaHora;
    private Label lblCreadoPor;
    private Label lblModificadoPor;
    private Label lblEditIndicator;
    private TextField txtOrdenCompra;
    private AutocompleteComboBox<SupplierModel> cbProveedor;
    private final Map<String, SupplierModel> supplierMap = new HashMap<>();
    private CompanyModel activeCompany;
    private ComboBox<String> cbFiltroEstado;

    // Campos formulario línea detalle
    private AutocompleteComboBox<ProductModel> cbProducto;
    private Label lblProductSupplierWarning;
    private TextField txtCantidad;
    private Label lblTotal;

    private Button btnGuardarNota;
    private Button btnExtrasNota;
    private Button btnEliminarNota;
    private Button btnAddLine;
    private Button btnDelLine;
    private Button btnExcel;
    private Button btnExtrasLinea;

    private OrderNoteModel selectedNote = null;
    private boolean isPopulatingForm = false;
    private final Map<String, CustomerModel> customerMap = new HashMap<>();
    private final Map<String, WarehouseModel> warehouseMap = new HashMap<>();
    private final Map<String, ProductModel> productMap = new HashMap<>();
    private final Map<String, LocationModel> locationMap = new HashMap<>();
    private final List<StockModel> rawStockList = new ArrayList<>();

    public NotasPedidoDialog(Window owner) {
                                        this.observableList = FXCollections.observableArrayList();
        this.filteredList = new javafx.collections.transformation.FilteredList<>(observableList, p -> true);
        this.detailList = FXCollections.observableArrayList();

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Gestión de Notas de Pedido (WMS)");

        setMinWidth(950);
        setMinHeight(520);

        construirUI();
        cargarDatos();

        WebSocketManager.UpdateListener orderNoteListener = action -> {
            System.out.println("[NotasPedidoDialog] Received real-time update event: " + action);
            cargarDatos();
        };
        WebSocketManager.getInstance().subscribe("ORDER_NOTE", orderNoteListener);
        WebSocketManager.getInstance().subscribe("STOCK", orderNoteListener);
        WebSocketManager.getInstance().subscribe("CUSTOMER", orderNoteListener);
        WebSocketManager.getInstance().subscribe("PRODUCT", orderNoteListener);
        WebSocketManager.getInstance().subscribe("WAREHOUSE", orderNoteListener);

        setOnHiding(e -> {
            WebSocketManager.getInstance().unsubscribe("ORDER_NOTE", orderNoteListener);
            WebSocketManager.getInstance().unsubscribe("STOCK", orderNoteListener);
            WebSocketManager.getInstance().unsubscribe("CUSTOMER", orderNoteListener);
            WebSocketManager.getInstance().unsubscribe("PRODUCT", orderNoteListener);
            WebSocketManager.getInstance().unsubscribe("WAREHOUSE", orderNoteListener);
        });
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header Box
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label("Gestión de Notas de Pedido");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Emisión, revisión y administración de notas de pedido por cliente y bodega.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // --- LADO IZQUIERDO: LISTADO (WHITE CARD) ---
        VBox leftPane = new VBox(10);
        leftPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        Label lblLeftTitle = new Label("Notas de Pedido Registradas");
        lblLeftTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        txtSearch = new TextField();
        txtSearch.setPromptText("Filtrar por folio o cliente...");
        txtSearch.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");
        txtSearch.setMaxWidth(180);

        cbFiltroEstado = new ComboBox<>(FXCollections.observableArrayList("Todos", "Pendientes", "Confirmadas", "Completadas", "Anuladas"));
        cbFiltroEstado.setValue("Todos");
        cbFiltroEstado.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        // Helper to update filtered list
        Runnable updateFiltros = () -> {
            String query = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
            String estadoFiltro = cbFiltroEstado.getValue();
            filteredList.setPredicate(note -> {
                if (!query.isEmpty()) {
                    boolean matchesText = (note.getFolio() != null && note.getFolio().toLowerCase().contains(query));
                    CustomerModel cli = customerMap.get(note.getCustomerId());
                    if (cli != null && cli.getRazonSocial() != null && cli.getRazonSocial().toLowerCase().contains(query)) {
                        matchesText = true;
                    }
                    if (!matchesText) return false;
                }

                if (estadoFiltro != null && !"Todos".equalsIgnoreCase(estadoFiltro)) {
                    String est = note.getEstado();
                    if ("Pendientes".equalsIgnoreCase(estadoFiltro)) {
                        return "PENDIENTE".equalsIgnoreCase(est);
                    } else if ("Confirmadas".equalsIgnoreCase(estadoFiltro)) {
                        return "CONFIRMADA".equalsIgnoreCase(est);
                    } else if ("Anuladas".equalsIgnoreCase(estadoFiltro)) {
                        return "ANULADA".equalsIgnoreCase(est);
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

        tablaNotas = new TableView<>(filteredList);
        tablaNotas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaNotas.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<OrderNoteModel, String> colFolio = new TableColumn<>("Folio NP");
        colFolio.setCellValueFactory(c -> {
            String val = c.getValue().getFolio();
            if (c.getValue().getNpModificada() != null && c.getValue().getNpModificada()) {
                val = "⚠️ " + val;
            }
            return new javafx.beans.property.SimpleStringProperty(val);
        });
        colFolio.setPrefWidth(85);

        TableColumn<OrderNoteModel, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(c -> {
            CustomerModel cli = customerMap.get(c.getValue().getCustomerId());
            return new SimpleStringProperty(cli != null ? cli.getRazonSocial() : "-");
        });
        colCliente.setPrefWidth(85);

        TableColumn<OrderNoteModel, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> {
            String f = c.getValue().getFecha();
            if (f != null && f.length() >= 10) {
                return new SimpleStringProperty(f.substring(0, 10));
            }
            return new SimpleStringProperty(f);
        });
        colFecha.setPrefWidth(75);

        TableColumn<OrderNoteModel, String> colEstado = new TableColumn<>("Estado NP");
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado() != null ? c.getValue().getEstado() : "-"));
        colEstado.setPrefWidth(75);

        TableColumn<OrderNoteModel, String> colEstadoTracking = new TableColumn<>("Estado Tracking");
        colEstadoTracking.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstadoTracking() != null ? c.getValue().getEstadoTracking() : "-"));
        colEstadoTracking.setPrefWidth(100);

        TableColumn<OrderNoteModel, String> colTracking = new TableColumn<>("N° Tracking");
        colTracking.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrackingNumber() != null ? c.getValue().getTrackingNumber() : "-"));
        colTracking.setPrefWidth(100);

        // Apply visual styling policy
        StatusColumnHelper.applyStatusStyling(colFolio, true);
        StatusColumnHelper.applyStatusStyling(colEstado, false);
        StatusColumnHelper.applyRowFactory(tablaNotas);

        TableColumn<OrderNoteModel, String> colEstadoDespacho = new TableColumn<>("Est. Despacho");
        colEstadoDespacho.setCellValueFactory(c -> {
            if (c.getValue().getDetails() == null || c.getValue().getDetails().isEmpty()) return new SimpleStringProperty("-");
            boolean allDispatched = true;
            boolean anyDispatched = false;
            for (com.logistics.packinglist.model.OrderNoteDetailModel detail : c.getValue().getDetails()) {
                int requested = detail.getCantidadPedida() != null ? detail.getCantidadPedida() : 0;
                int bo = detail.getCantidadBackOrder() != null ? detail.getCantidadBackOrder() : 0;
                int effective = requested - bo;
                int dispatched = detail.getCantidadDespachada() != null ? detail.getCantidadDespachada() : 0;
                if (effective > 0) {
                    if (dispatched < effective) {
                        allDispatched = false;
                    }
                    if (dispatched > 0) {
                        anyDispatched = true;
                    }
                }
            }
            if (allDispatched && anyDispatched) return new SimpleStringProperty("DESPACHADO");
            return new SimpleStringProperty("-");
        });
        colEstadoDespacho.setPrefWidth(100);
        StatusColumnHelper.applyStatusStyling(colEstadoDespacho, false);

        TableColumn<OrderNoteModel, String> colFolioBO = new TableColumn<>("Folio BO");
        colFolioBO.setCellValueFactory(c -> {
            java.util.Map<String, String> attrs = c.getValue().getAtributosPersonalizados();
            if (attrs != null && attrs.containsKey("origen_bo")) {
                return new SimpleStringProperty(attrs.get("origen_bo"));
            }
            return new SimpleStringProperty("-");
        });
        colFolioBO.setPrefWidth(90);

        tablaNotas.getColumns().addAll(colFolio, colCliente, colFecha, colEstado, colEstadoDespacho, colTracking, colEstadoTracking, colFolioBO);
        VBox.setVgrow(tablaNotas, Priority.ALWAYS);

        leftPane.getChildren().addAll(lblLeftTitle, filterBox, tablaNotas);

        // --- LADO DERECHO: FORMULARIO (WHITE CARD EN SCROLLPANE) ---
        VBox rightPane = new VBox(12);
        rightPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(8);

        cbDeposito = new ComboBox<>();
        cbDeposito.setPromptText("Seleccione Depósito");
        cbDeposito.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbDeposito.setMaxWidth(Double.MAX_VALUE);
        cbDeposito.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbBodega.getItems().clear();
            cbBodega.setValue(null);
            if (newVal != null) {
                new Thread(() -> {
                    try {
                        List<WarehouseModel> bodegas = service.obtenerBodegasPorDeposito(newVal.getId());
                        javafx.application.Platform.runLater(() -> {
                            cbBodega.getItems().setAll(bodegas);
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            System.err.println("Error cargando bodegas de depósito: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });

        cbBodega = new ComboBox<>();
        cbBodega.setPromptText("Seleccione Bodega");
        cbBodega.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbBodega.setMaxWidth(Double.MAX_VALUE);

        txtFolio = new TextField();
        txtFolio.setPromptText("Folio del pedido (Ej: NP-1002)");
        txtFolio.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtFolio.setMaxWidth(Double.MAX_VALUE);

        cbCliente = new AutocompleteComboBox<>();
        cbCliente.setPromptText("Seleccione Cliente");
        cbCliente.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbCliente.setMaxWidth(Double.MAX_VALUE);
        cbCliente.setFilterPredicate((c, text) -> {
            return (c.getRazonSocial() != null && c.getRazonSocial().toLowerCase().contains(text))
                || (c.getRut() != null && c.getRut().toLowerCase().contains(text))
                || (c.getId() != null && c.getId().toLowerCase().contains(text));
        });

        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("PENDIENTE", "CONFIRMADA", "COMPLETADA", "ANULADA");
        cbEstado.setValue("PENDIENTE");
        cbEstado.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbEstado.setMaxWidth(Double.MAX_VALUE);
        cbEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            // No auto-bloquear la UI solo por cambiar el select. El bloqueo real 
            // depende del estado guardado en la base de datos de la nota actual.
        });

        cbZonaReserva = new ComboBox<>();
        cbZonaReserva.getItems().addAll("COMERCIAL", "PRIMARIA");
        cbZonaReserva.setValue("COMERCIAL");
        cbZonaReserva.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbZonaReserva.setMaxWidth(Double.MAX_VALUE);

        Label lblDeposito = new Label("Depósito:");
        lblDeposito.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblBodega = new Label("Bodega:");
        lblBodega.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblFolioNP = new Label("Folio NP:");
        lblFolioNP.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblClienteNP = new Label("Cliente:");
        lblClienteNP.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblEstadoNP = new Label("Estado:");
        lblEstadoNP.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblZonaNP = new Label("Zona Reserva:");
        lblZonaNP.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblProvNP = new Label("Proveedor:");
        lblProvNP.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblOCNP = new Label("N° Orden Compra:");
        lblOCNP.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");

        formGrid.add(lblDeposito, 0, 0);
        formGrid.add(cbDeposito, 1, 0);
        formGrid.add(lblBodega, 2, 0);
        formGrid.add(cbBodega, 3, 0);

        formGrid.add(lblFolioNP, 0, 1);
        formGrid.add(txtFolio, 1, 1);
        formGrid.add(lblClienteNP, 2, 1);
        formGrid.add(cbCliente, 3, 1);
        formGrid.add(lblEstadoNP, 2, 1);
        formGrid.add(cbEstado, 3, 1);

        lblFechaHora = new Label("-");
        lblFechaHora.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        lblCreadoPor = new Label("-");
        lblCreadoPor.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        lblModificadoPor = new Label("-");
        lblModificadoPor.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");

        txtOrdenCompra = new TextField();
        txtOrdenCompra.setPromptText("N° Orden Compra");
        txtOrdenCompra.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtOrdenCompra.setMaxWidth(Double.MAX_VALUE);

        cbProveedor = new AutocompleteComboBox<>();
        cbProveedor.setPromptText("Seleccione Proveedor");
        cbProveedor.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbProveedor.setMaxWidth(Double.MAX_VALUE);
        cbProveedor.setFilterPredicate((s, text) -> {
            return (s.getRazonSocial() != null && s.getRazonSocial().toLowerCase().contains(text))
                || (s.getRut() != null && s.getRut().toLowerCase().contains(text))
                || (s.getId() != null && s.getId().toLowerCase().contains(text));
        });

        cbProveedor.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isPopulatingForm) {
                if (newVal != null) {
                    filtrarProductosPorProveedor(newVal.getId());
                }
                return;
            }
            if (newVal != null) {
                if (oldVal != null && !oldVal.getId().equals(newVal.getId()) && !detailList.isEmpty()) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Cambio de Proveedor");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Si cambia de proveedor, se vaciará el detalle actual de productos. ¿Desea continuar?");
                    ButtonType btnAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
                    ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
                    confirm.getButtonTypes().setAll(btnAceptar, btnCancelar);
                    Optional<ButtonType> res = confirm.showAndWait();
                    if (res.isPresent() && res.get() == btnAceptar) {
                        detailList.clear();
                        actualizarTotal();
                    } else {
                        javafx.application.Platform.runLater(() -> cbProveedor.selectItem(oldVal));
                        return;
                    }
                }
                filtrarProductosPorProveedor(newVal.getId());
                cbProducto.setDisable(false);
                if (btnAddLine != null) btnAddLine.setDisable(false);
                if (btnExcel != null) btnExcel.setDisable(false);
                if (lblProductSupplierWarning != null) {
                    lblProductSupplierWarning.setVisible(false);
                    lblProductSupplierWarning.setManaged(false);
                }
            } else {
                cbProducto.selectItem(null);
                cbProducto.getItems().clear();
                cbProducto.setDisable(true);
                if (btnAddLine != null) btnAddLine.setDisable(true);
                if (btnExcel != null) btnExcel.setDisable(true);
                if (lblProductSupplierWarning != null) {
                    lblProductSupplierWarning.setVisible(true);
                    lblProductSupplierWarning.setManaged(true);
                }
            }
        });

        formGrid.add(lblZonaNP, 0, 2);
        formGrid.add(cbZonaReserva, 1, 2);
        formGrid.add(lblProvNP, 2, 2);
        formGrid.add(cbProveedor, 3, 2);

        formGrid.add(lblOCNP, 0, 3);
        formGrid.add(txtOrdenCompra, 1, 3);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(15);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(35);
        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(15);
        ColumnConstraints c4 = new ColumnConstraints();
        c4.setPercentWidth(35);
        formGrid.getColumnConstraints().addAll(c1, c2, c3, c4);

        // Sección Detalle (Productos y Cantidades)
        VBox detailBox = new VBox(8);
        detailBox.setPadding(new Insets(10));
        detailBox.setStyle("-fx-border-color: #cbd5e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-color: white;");
        VBox.setVgrow(detailBox, Priority.ALWAYS);

        HBox lineInputRow = new HBox(8);
        lineInputRow.setAlignment(Pos.CENTER_LEFT);

        cbProducto = new AutocompleteComboBox<>();
        cbProducto.setPromptText("Seleccione Producto...");
        cbProducto.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px; -fx-border-radius: 4px;");
        cbProducto.setPrefWidth(250);
        cbProducto.setDisable(true);
        cbProducto.setFilterPredicate((p, text) -> {
            String codeCliente = p.getAtributosPersonalizados() != null ? p.getAtributosPersonalizados().get("codigo_cliente") : null;
            return (p.getSku() != null && p.getSku().toLowerCase().contains(text))
                || (p.getNombre() != null && p.getNombre().toLowerCase().contains(text))
                || (p.getId() != null && p.getId().toLowerCase().contains(text))
                || (p.getBarcode() != null && p.getBarcode().toLowerCase().contains(text))
                || (codeCliente != null && codeCliente.toLowerCase().contains(text));
        });

        lblProductSupplierWarning = new Label("Debe seleccionar un proveedor antes de agregar productos.");
        lblProductSupplierWarning.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        lblProductSupplierWarning.setVisible(true);
        lblProductSupplierWarning.setManaged(true);

        cbProducto.setCellFactory(lv -> new ListCell<ProductModel>() {
            @Override
            protected void updateItem(ProductModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    int stock = getStockForProduct(item.getId());
                    setText(item.getSku() + " - " + item.getNombre() + " (Stock: " + stock + ")");
                }
            }
        });

        cbProducto.setButtonCell(new ListCell<ProductModel>() {
            @Override
            protected void updateItem(ProductModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    int stock = getStockForProduct(item.getId());
                    setText(item.getSku() + " - " + item.getNombre() + " (Stock: " + stock + ")");
                }
            }
        });

        cbBodega.valueProperty().addListener((obs, oldVal, newVal) -> refrescarComboboxProducto());
        cbZonaReserva.valueProperty().addListener((obs, oldVal, newVal) -> refrescarComboboxProducto());

        txtCantidad = new TextField();
        txtCantidad.setPromptText("Cant.");
        txtCantidad.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");
        txtCantidad.setPrefWidth(60);

        FontAwesomeIconView iconAdd = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        iconAdd.setFill(Color.WHITE);
        btnAddLine = new Button(null, iconAdd);
        btnAddLine.setStyle("-fx-background-color: #3182ce; -fx-cursor: hand;");
        btnAddLine.setOnAction(e -> agregarLinea());

        FontAwesomeIconView iconDel = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDel.setFill(Color.WHITE);
        btnDelLine = new Button(null, iconDel);
        btnDelLine.setStyle("-fx-background-color: #e53e3e; -fx-cursor: hand;");
        btnDelLine.setOnAction(e -> eliminarLinea());

        FontAwesomeIconView iconExcel = new FontAwesomeIconView(FontAwesomeIcon.FILE_EXCEL_ALT);
        iconExcel.setFill(Color.WHITE);
        btnExcel = new Button("SKU", iconExcel);
        btnExcel.setStyle("-fx-background-color: #2f855a; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnExcel.setOnAction(e -> cargarExcelDeSKUs());

        FontAwesomeIconView iconExtrasProd = new FontAwesomeIconView(FontAwesomeIcon.TAGS);
        iconExtrasProd.setFill(Color.WHITE);
        btnExtrasLinea = new Button("Extras Prod", iconExtrasProd);
        btnExtrasLinea.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnExtrasLinea.setOnAction(e -> abrirExtrasLinea());

        Label lblProd = new Label("Prod:");
        lblProd.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblCant = new Label("Cant:");
        lblCant.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");

        lineInputRow.getChildren().addAll(lblProd, cbProducto, lblCant, txtCantidad, btnAddLine, btnDelLine, btnExcel, btnExtrasLinea);

        tablaDetalles = new TableView<>(detailList);
        tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaDetalles.setMinHeight(140);
        tablaDetalles.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<OrderNoteDetailModel, String> colLineSku = new TableColumn<>("SKU");
        colLineSku.setCellValueFactory(c -> {
            ProductModel p = productMap.get(c.getValue().getProductId());
            return new SimpleStringProperty(p != null && p.getSku() != null ? p.getSku() : "-");
        });
        colLineSku.setPrefWidth(80);

        TableColumn<OrderNoteDetailModel, String> colLineProd = new TableColumn<>("Producto");
        colLineProd.setCellValueFactory(c -> {
            ProductModel p = productMap.get(c.getValue().getProductId());
            return new SimpleStringProperty(p != null && p.getNombre() != null ? p.getNombre() : "-");
        });
        colLineProd.setPrefWidth(150);

        TableColumn<OrderNoteDetailModel, String> colLinePais = new TableColumn<>("País de origen");
        colLinePais.setCellValueFactory(c -> {
            ProductModel p = productMap.get(c.getValue().getProductId());
            return new SimpleStringProperty(p != null && p.getPaisOrigen() != null ? p.getPaisOrigen() : "-");
        });
        colLinePais.setPrefWidth(90);

        TableColumn<OrderNoteDetailModel, String> colLinePrecio = new TableColumn<>("Precio");
        colLinePrecio.setCellValueFactory(c -> new SimpleStringProperty("$ " + String.format("%,.2f", c.getValue().getPrecioUnitario())));
        colLinePrecio.setPrefWidth(80);

        TableColumn<OrderNoteDetailModel, String> colLineCantPed = new TableColumn<>("Cantidad");
        colLineCantPed.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantidadPedida())));
        colLineCantPed.setPrefWidth(70);

        TableColumn<OrderNoteDetailModel, String> colLineStock = new TableColumn<>("Stock");
        colLineStock.setCellValueFactory(c -> {
            int stock = getStockForProduct(c.getValue().getProductId());
            return new SimpleStringProperty(String.valueOf(stock));
        });
        colLineStock.setPrefWidth(90);

        TableColumn<OrderNoteDetailModel, String> colLineReserved = new TableColumn<>("Reservado");
        colLineReserved.setCellValueFactory(c -> {
            Integer res = c.getValue().getCantidadReservada();
            return new SimpleStringProperty(String.valueOf(res != null ? res : 0));
        });
        colLineReserved.setPrefWidth(75);

        TableColumn<OrderNoteDetailModel, String> colLineBO = new TableColumn<>("Cantidad BO");
        colLineBO.setCellValueFactory(c -> {
            Integer bo = c.getValue().getCantidadBackOrder();
            // Para líneas nuevas (aún no persistidas), calcular dinámicamente
            if (bo == null || (c.getValue().getId() == null && bo == 0)) {
                int stock = getStockForProduct(c.getValue().getProductId());
                int solicitado = c.getValue().getCantidadPedida() != null ? c.getValue().getCantidadPedida() : 0;
                int calculatedBO = Math.max(0, solicitado - stock);
                return new SimpleStringProperty(String.valueOf(calculatedBO));
            }
            return new SimpleStringProperty(String.valueOf(bo));
        });
        colLineBO.setPrefWidth(85);

        TableColumn<OrderNoteDetailModel, String> colLineExtras = new TableColumn<>("Cargos / Descuentos");
        colLineExtras.setCellValueFactory(c -> {
            Map<String, String> attrs = c.getValue().getAtributosPersonalizados();
            if (attrs == null || attrs.isEmpty()) return new SimpleStringProperty("-");
            List<String> list = new ArrayList<>();
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                String label = entry.getKey();
                if (label.startsWith("charge_")) {
                    list.add("+" + entry.getValue() + " (" + label.substring(7) + ")");
                } else if (label.startsWith("discount_")) {
                    list.add("-" + entry.getValue() + " (" + label.substring(9) + ")");
                } else {
                    list.add(label + ": " + entry.getValue());
                }
            }
            return new SimpleStringProperty(String.join(", ", list));
        });
        colLineExtras.setPrefWidth(140);

        TableColumn<OrderNoteDetailModel, String> colLineSubTotal = new TableColumn<>("Subtotal");
        colLineSubTotal.setCellValueFactory(c -> {
            OrderNoteDetailModel line = c.getValue();
            int pedida = line.getCantidadPedida() != null ? line.getCantidadPedida() : 0;
            int bo = line.getCantidadBackOrder() != null ? line.getCantidadBackOrder() : 0;
            if (line.getId() == null && bo == 0) {
                int stock = getStockForProduct(line.getProductId());
                bo = Math.max(0, pedida - stock);
            }
            int cantEfectiva = Math.max(0, pedida - bo);
            double baseSub = cantEfectiva * line.getPrecioUnitario();
            double cargos = 0;
            double descuentos = 0;
            if (line.getAtributosPersonalizados() != null) {
                for (Map.Entry<String, String> entry : line.getAtributosPersonalizados().entrySet()) {
                    String key = entry.getKey();
                    String valStr = entry.getValue();
                    boolean isPercent = valStr.endsWith("%");
                    double val = 0;
                    try {
                        String cleanVal = isPercent ? valStr.substring(0, valStr.length() - 1) : valStr;
                        val = Double.parseDouble(cleanVal);
                    } catch (NumberFormatException ignored) {}

                    if (key.startsWith("charge_")) {
                        if (isPercent) cargos += (val / 100.0) * baseSub;
                        else cargos += val;
                    } else if (key.startsWith("discount_")) {
                        if (isPercent) descuentos += (val / 100.0) * baseSub;
                        else descuentos += val;
                    }
                }
            }
            double finalLineSub = baseSub + cargos - descuentos;
            if (finalLineSub < 0) finalLineSub = 0;
            return new SimpleStringProperty("$ " + String.format("%,.2f", finalLineSub));
        });
        colLineSubTotal.setPrefWidth(90);

        tablaDetalles.getColumns().addAll(colLineSku, colLineProd, colLinePais, colLinePrecio, colLineCantPed, colLineStock, colLineReserved, colLineBO, colLineExtras, colLineSubTotal);
        VBox.setVgrow(tablaDetalles, Priority.ALWAYS);

        HBox footerRow = new HBox(15);
        footerRow.setAlignment(Pos.CENTER_RIGHT);
        lblTotal = new Label("Total Pedido: $ 0.00");
        lblTotal.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        footerRow.getChildren().add(lblTotal);

        detailBox.getChildren().addAll(lblProductSupplierWarning, lineInputRow, tablaDetalles, footerRow);

        // Botones guardar/limpiar principal
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSave.setFill(Color.WHITE);
        iconSave.setSize("16");
        btnGuardarNota = new Button(null, iconSave);
        btnGuardarNota.setStyle("-fx-background-color: #0F3E6E; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnGuardarNota.setOnAction(e -> guardarNota());
        Tooltip.install(btnGuardarNota, new Tooltip("Guardar Nota de Pedido"));

        FontAwesomeIconView iconExtrasNP = new FontAwesomeIconView(FontAwesomeIcon.TAGS);
        iconExtrasNP.setFill(Color.WHITE);
        btnExtrasNota = new Button("Extras NP", iconExtrasNP);
        btnExtrasNota.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnExtrasNota.setOnAction(e -> abrirExtrasNota());

        FontAwesomeIconView iconClose = new FontAwesomeIconView(FontAwesomeIcon.CLOSE);
        iconClose.setFill(Color.WHITE);
        iconClose.setSize("16");
        btnEliminarNota = new Button(null, iconClose);
        btnEliminarNota.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnEliminarNota.setOnAction(e -> eliminarNota());
        Tooltip.install(btnEliminarNota, new Tooltip("Anular Nota de Pedido"));

        FontAwesomeIconView iconRefresh = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        iconRefresh.setFill(Color.WHITE);
        iconRefresh.setSize("16");
        Button btnLimpiarNota = new Button(null, iconRefresh);
        btnLimpiarNota.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnLimpiarNota.setOnAction(e -> limpiarFormulario());
        Tooltip.install(btnLimpiarNota, new Tooltip("Limpiar Formulario"));

        Button btnPdf = UiComponentFactory.createPdfButton(this::descargarPDF);
        Button btnImprimir = UiComponentFactory.createPrintButton(this::imprimirNota);

        actionRow.getChildren().addAll(btnPdf, btnImprimir, btnGuardarNota, btnExtrasNota, btnEliminarNota, btnLimpiarNota);

        Label lblDocTitle = new Label("Datos del Documento");
        lblDocTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        lblEditIndicator = new Label("Modificación de Nota de Pedido");
        lblEditIndicator.setStyle("-fx-text-fill: #ea580c; -fx-font-weight: bold; -fx-font-size: 12px;");
        lblEditIndicator.setVisible(false);
        lblEditIndicator.setManaged(false);

        Label lblDetailTitle = new Label("Detalles de Productos");
        lblDetailTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        rightPane.getChildren().addAll(lblDocTitle, lblEditIndicator, formGrid, lblDetailTitle, detailBox, actionRow);

        ScrollPane rightScroll = new ScrollPane(rightPane);
        rightScroll.setFitToWidth(true);
        rightScroll.setFitToHeight(true);
        rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(leftPane, rightScroll);
        mainSplit.setDividerPositions(0.5);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        root.getChildren().addAll(headerBox, mainSplit);

        // Listeners
        tablaNotas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                isPopulatingForm = true;
                selectedNote = newSel;
                if (newSel.getDepositId() != null) {
                    cbDeposito.getItems().stream()
                        .filter(d -> d.getId().equals(newSel.getDepositId()))
                        .findFirst()
                        .ifPresent(cbDeposito::setValue);
                }
                cbBodega.setValue(warehouseMap.get(newSel.getWarehouseId()));
                txtFolio.setText(newSel.getFolio());
                cbCliente.setValue(customerMap.get(newSel.getCustomerId()));
                cbEstado.setValue(newSel.getEstado());

                tempNoteAttributes.clear();
                if (newSel.getAtributosPersonalizados() != null) {
                    tempNoteAttributes.putAll(newSel.getAtributosPersonalizados());
                }

                if (tempNoteAttributes.containsKey("zona_reserva")) {
                    cbZonaReserva.setValue(tempNoteAttributes.get("zona_reserva"));
                } else {
                    cbZonaReserva.setValue("COMERCIAL");
                }

                txtOrdenCompra.setText(tempNoteAttributes.getOrDefault("orden_compra", ""));
                String provId = tempNoteAttributes.get("proveedor_id");
                if (provId != null && supplierMap.containsKey(provId)) {
                    cbProveedor.setValue(supplierMap.get(provId));
                    filtrarProductosPorProveedor(provId);
                    cbProducto.setDisable(false);
                    btnAddLine.setDisable(false);
                    btnExcel.setDisable(false);
                    lblProductSupplierWarning.setVisible(false);
                    lblProductSupplierWarning.setManaged(false);
                } else {
                    cbProveedor.getSelectionModel().clearSelection();
                    cbProducto.getItems().clear();
                    cbProducto.setDisable(true);
                    btnAddLine.setDisable(true);
                    btnExcel.setDisable(true);
                    lblProductSupplierWarning.setVisible(true);
                    lblProductSupplierWarning.setManaged(true);
                }

                lblFechaHora.setText(DateFormatterUtil.format(newSel.getFecha()));
                lblCreadoPor.setText(newSel.getCreadorNombre() != null ? newSel.getCreadorNombre() : "-");
                lblModificadoPor.setText(newSel.getEditorNombre() != null ? newSel.getEditorNombre() : "-");
                
                lblEditIndicator.setVisible(true);
                lblEditIndicator.setManaged(true);

                detailList.clear();
                if (newSel.getDetails() != null) {
                    detailList.addAll(newSel.getDetails());
                }
                actualizarTotal();

                if ("ANULADA".equalsIgnoreCase(newSel.getEstado())) {
                    toggleEditingFields(false);
                } else if ("COMPLETADA".equalsIgnoreCase(newSel.getEstado())) {
                    toggleEditingFields(false);
                    // Permitir anular incluso si está COMPLETADA (el backend validará que no tenga despacho)
                    if (btnEliminarNota != null) btnEliminarNota.setDisable(false);
                } else {
                    toggleEditingFields(true);
                }
                isPopulatingForm = false;
            }
        });

        Scene scene = new Scene(root, 1200, 640);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setScene(scene);
        ScreenUtil.fitDialogToScreen(this, getOwner(), 1200, 640, 950, 520);
    }

    private void refrescarComboboxProducto() {
        if (cbProducto == null) return;
        ProductModel selected = cbProducto.getValue();
        List<ProductModel> items = new ArrayList<>(cbProducto.getItems());
        cbProducto.getItems().clear();
        cbProducto.getItems().addAll(items);
        if (selected != null && items.contains(selected)) {
            cbProducto.setValue(selected);
        }
    }

    private void filtrarProductosPorProveedor(String supplierId) {
        if (cbProducto == null) return;
        List<ProductModel> filtered = new ArrayList<>();
        for (ProductModel p : productMap.values()) {
            if (supplierId != null && supplierId.equals(p.getSupplierId())) {
                filtered.add(p);
            }
        }
        cbProducto.setAllItems(filtered);
    }

    private int getStockForProduct(String productId) {
        WarehouseModel wh = cbBodega.getValue();
        if (wh == null) return 0;
        String zone = cbZonaReserva.getValue();
        if (zone == null) zone = "COMERCIAL";

        int totalPhysical = 0;
        for (StockModel s : rawStockList) {
            if (productId.equals(s.getProductId())) {
                LocationModel loc = locationMap.get(s.getLocationId());
                if (loc != null && wh.getId().equals(loc.getWarehouseId())) {
                    String stockType = s.getTipoStock() != null ? s.getTipoStock() : "COMERCIAL";
                    if (zone.equalsIgnoreCase(stockType)) {
                        totalPhysical += s.getCantidad() != null ? s.getCantidad() : 0;
                    }
                }
            }
        }

        int reserved = 0;
        if (observableList != null) {
            for (OrderNoteModel np : observableList) {
                if (np.getWarehouseId() != null && np.getWarehouseId().equals(wh.getId()) &&
                    ("PENDIENTE".equalsIgnoreCase(np.getEstado()) || "CONFIRMADA".equalsIgnoreCase(np.getEstado()))) {
                    if (np.getDetails() != null) {
                        for (OrderNoteDetailModel det : np.getDetails()) {
                            if (productId.equals(det.getProductId())) {
                                reserved += (det.getCantidadReservada() != null ? det.getCantidadReservada() : 0);
                            }
                        }
                    }
                }
            }
        }

        return Math.max(0, totalPhysical - reserved);
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {
                List<com.logistics.packinglist.model.DepositModel> depositos = service.obtenerDepositos();
                List<WarehouseModel> bodegas = service.obtenerBodegas();
                List<CustomerModel> clientes = service.obtenerClientes();
                List<ProductModel> productos = service.obtenerProductos();
                List<LocationModel> ubicaciones = service.obtenerUbicaciones();
                List<StockModel> stocks = service.obtenerStocks();
                List<OrderNoteModel> notas = service.obtenerNotasPedido();
                List<SupplierModel> proveedores = service.obtenerProveedores();

                String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
                CompanyModel compObj = null;
                if (companyId != null) {
                    try {
                        compObj = service.obtenerEmpresaPorId(companyId);
                    } catch (Exception ignored) {}
                }

                final CompanyModel finalComp = compObj;
                javafx.application.Platform.runLater(() -> {
                    activeCompany = finalComp;

                    supplierMap.clear();
                    for (SupplierModel s : proveedores) {
                        supplierMap.put(s.getId(), s);
                    }
                    cbProveedor.setAllItems(proveedores);

                    cbDeposito.getItems().setAll(depositos);

                    warehouseMap.clear();
                    // We don't populate cbBodega with all bodegas anymore, cbDeposito listener will do it.
                    cbBodega.getItems().clear();
                    for (WarehouseModel w : bodegas) {
                        warehouseMap.put(w.getId(), w);
                    }

                    customerMap.clear();
                    for (CustomerModel c : clientes) {
                        customerMap.put(c.getId(), c);
                    }
                    cbCliente.setAllItems(clientes);

                    productMap.clear();
                    for (ProductModel p : productos) {
                        productMap.put(p.getId(), p);
                    }
                    cbProducto.setAllItems(new java.util.ArrayList<>());

                    locationMap.clear();
                    for (LocationModel l : ubicaciones) {
                        locationMap.put(l.getId(), l);
                    }

                    rawStockList.clear();
                    rawStockList.addAll(stocks);

                    // Preserve selected item in table
                    OrderNoteModel selected = tablaNotas.getSelectionModel().getSelectedItem();
                    String selectedId = selected != null ? selected.getId() : null;

                    observableList.clear();
                    observableList.addAll(notas);

                    if (selectedId != null) {
                        for (OrderNoteModel n : observableList) {
                            if (n.getId().equals(selectedId)) {
                                tablaNotas.getSelectionModel().select(n);
                                break;
                            }
                        }
                    }

                    String userWhId = com.logistics.packinglist.service.AuthService.getInstance().getWarehouseId();
                    if (userWhId != null && warehouseMap.containsKey(userWhId) && cbBodega.getValue() == null) {
                        cbBodega.setValue(warehouseMap.get(userWhId));
                    }
                    if (txtFolio.getText() == null || txtFolio.getText().trim().isEmpty()) {
                        cargarSiguienteFolio();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error", "No se pudieron cargar los datos: " + e.getMessage());
                });
            }
        }).start();
    }

    private void cargarSiguienteFolio() {
        new Thread(() -> {
            try {
                String nextFolio = service.obtenerSiguienteFolio();
                javafx.application.Platform.runLater(() -> {
                    if (selectedNote == null) {
                        txtFolio.setText(nextFolio);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void agregarLinea() {
        ProductModel prod = cbProducto.getValue();
        String cantStr = txtCantidad.getText().trim();

        if (prod == null || cantStr.isEmpty()) {
            mostrarWarning("Línea de Pedido", "Debe seleccionar un producto y especificar la cantidad.");
            return;
        }

        int cantidadVal;
        try {
            cantidadVal = Integer.parseInt(cantStr);
        } catch (NumberFormatException e) {
            mostrarWarning("Línea de Pedido", "La cantidad debe ser un entero válido.");
            return;
        }

        // No stock limits here - excess will be split into a BO note upon saving.

        // Revisar si ya existe en las líneas
        for (OrderNoteDetailModel dl : detailList) {
            if (dl.getProductId().equals(prod.getId())) {
                dl.setCantidadPedida(dl.getCantidadPedida() + cantidadVal);
                tablaDetalles.refresh();
                actualizarTotal();
                return;
            }
        }

        OrderNoteDetailModel line = OrderNoteDetailModel.builder()
                .productId(prod.getId())
                .cantidadPedida(cantidadVal)
                .cantidadDespachada(0)
                .precioUnitario(prod.getPrecio())
                .atributosPersonalizados(new HashMap<>())
                .build();

        detailList.add(line);
        actualizarTotal();
    }

    private void eliminarLinea() {
        OrderNoteDetailModel selectedLine = tablaDetalles.getSelectionModel().getSelectedItem();
        if (selectedLine != null) {
            detailList.remove(selectedLine);
            actualizarTotal();
        } else {
            mostrarWarning("Quitar línea", "Debe seleccionar una línea del detalle de la tabla.");
        }
    }

    private void actualizarTotal() {
        double subtotalLineas = 0;
        double totalCargosLineas = 0;
        double totalDescuentosLineas = 0;

        for (OrderNoteDetailModel line : detailList) {
            int pedida = line.getCantidadPedida() != null ? line.getCantidadPedida() : 0;
            int bo = line.getCantidadBackOrder() != null ? line.getCantidadBackOrder() : 0;
            if (line.getId() == null && bo == 0) {
                int stock = getStockForProduct(line.getProductId());
                bo = Math.max(0, pedida - stock);
            }
            int cantEfectiva = Math.max(0, pedida - bo);
            double baseSub = cantEfectiva * line.getPrecioUnitario();
            double cargos = 0;
            double descuentos = 0;
            
            if (line.getAtributosPersonalizados() != null) {
                for (Map.Entry<String, String> entry : line.getAtributosPersonalizados().entrySet()) {
                    String key = entry.getKey();
                    String valStr = entry.getValue();
                    boolean isPercent = valStr.endsWith("%");
                    double val = 0;
                    try {
                        String cleanVal = isPercent ? valStr.substring(0, valStr.length() - 1) : valStr;
                        val = Double.parseDouble(cleanVal);
                    } catch (NumberFormatException ignored) {}

                    if (key.startsWith("charge_")) {
                        if (isPercent) {
                            cargos += (val / 100.0) * baseSub;
                        } else {
                            cargos += val;
                        }
                    } else if (key.startsWith("discount_")) {
                        if (isPercent) {
                            descuentos += (val / 100.0) * baseSub;
                        } else {
                            descuentos += val;
                        }
                    }
                }
            }
            subtotalLineas += baseSub;
            totalCargosLineas += cargos;
            totalDescuentosLineas += descuentos;
        }

        double subtotalConExtrasLineas = subtotalLineas + totalCargosLineas - totalDescuentosLineas;

        double cargosGlobales = 0;
        double descuentosGlobales = 0;

        for (Map.Entry<String, String> entry : tempNoteAttributes.entrySet()) {
            String key = entry.getKey();
            String valStr = entry.getValue();
            boolean isPercent = valStr.endsWith("%");
            double val = 0;
            try {
                String cleanVal = isPercent ? valStr.substring(0, valStr.length() - 1) : valStr;
                val = Double.parseDouble(cleanVal);
            } catch (NumberFormatException ignored) {}

            if (key.startsWith("charge_")) {
                if (isPercent) {
                    cargosGlobales += (val / 100.0) * subtotalConExtrasLineas;
                } else {
                    cargosGlobales += val;
                }
            } else if (key.startsWith("discount_")) {
                if (isPercent) {
                    descuentosGlobales += (val / 100.0) * subtotalConExtrasLineas;
                } else {
                    descuentosGlobales += val;
                }
            }
        }

        double totalFinal = subtotalConExtrasLineas + cargosGlobales - descuentosGlobales;
        if (totalFinal < 0) totalFinal = 0;

        lblTotal.setText(String.format(
            "Subtotal: $ %,.2f  |  Cargos (+): $ %,.2f  |  Descuentos (-): $ %,.2f  |  Total: $ %,.2f",
            subtotalLineas, (totalCargosLineas + cargosGlobales), (totalDescuentosLineas + descuentosGlobales), totalFinal
        ));
    }

    private void guardarNota() {
        WarehouseModel wh = cbBodega.getValue();
        String folio = txtFolio.getText().trim();
        CustomerModel customer = cbCliente.getValue();
        String estado = cbEstado.getValue();

        if (selectedNote != null && !estado.equalsIgnoreCase(selectedNote.getEstado()) && !"ANULADA".equalsIgnoreCase(estado)) {
            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar cambio de estado");
            confirm.setHeaderText("Modificación de Estado");
            confirm.setContentText("¿Está seguro que desea cambiar el estado de la Nota de Pedido de " + selectedNote.getEstado() + " a " + estado + "?");
            java.util.Optional<javafx.scene.control.ButtonType> res = confirm.showAndWait();
            if (res.isPresent() && res.get() != javafx.scene.control.ButtonType.OK) {
                return;
            }
        }

        if ("ANULADA".equalsIgnoreCase(estado)) {
            if (selectedNote == null) {
                mostrarWarning("Validación", "No se puede crear una Nota de Pedido en estado ANULADA directamente.");
                return;
            }
            eliminarNota();
            return;
        }

        if (wh == null || folio.isEmpty() || customer == null) {
            mostrarWarning("Validación", "Los campos Bodega, Folio y Cliente son obligatorios.");
            return;
        }

        if (detailList.isEmpty()) {
            mostrarWarning("Validación", "La nota de pedido debe tener al menos una línea de producto.");
            return;
        }

        OrderNoteModel model = selectedNote != null ? selectedNote : new OrderNoteModel();
        model.setCompanyId(com.logistics.packinglist.service.AuthService.getInstance().getCompanyId());
        if (cbDeposito.getValue() != null) {
            model.setDepositId(cbDeposito.getValue().getId());
        }
        model.setWarehouseId(wh.getId());
        model.setFolio(folio);
        model.setCustomerId(customer.getId());
        model.setEstado(estado);
        model.setDetails(new ArrayList<>(detailList));
        
        if (cbZonaReserva.getValue() != null) {
            tempNoteAttributes.put("zona_reserva", cbZonaReserva.getValue());
        }
        tempNoteAttributes.put("orden_compra", txtOrdenCompra.getText().trim());
        tempNoteAttributes.put("proveedor_id", cbProveedor.getValue() != null ? cbProveedor.getValue().getId() : "");
        model.setAtributosPersonalizados(new HashMap<>(tempNoteAttributes));

        new Thread(() -> {
            try {
                if (selectedNote == null) {
                    model.setFecha(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    OrderNoteModel created = service.crearNotaPedido(model);
                    javafx.application.Platform.runLater(() -> {
                        observableList.add(created);
                        mostrarInformacion("Éxito", "Nota de pedido guardada con éxito.");
                        limpiarFormulario();
                    });
                } else {
                    OrderNoteModel updated = service.actualizarNotaPedido(model.getId(), model);
                    javafx.application.Platform.runLater(() -> {
                        int idx = observableList.indexOf(selectedNote);
                        if (idx >= 0) {
                            observableList.set(idx, updated);
                        }
                        mostrarInformacion("Éxito", "Nota de pedido actualizada con éxito.");
                        limpiarFormulario();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error", "No se pudo guardar la nota de pedido: " + e.getMessage());
                });
            }
        }).start();
    }

    private void eliminarNota() {
        if (selectedNote == null) {
            mostrarWarning("Selección", "Debe seleccionar una nota de pedido de la lista.");
            return;
        }

        if ("ANULADA".equalsIgnoreCase(selectedNote.getEstado())) {
            mostrarWarning("Validación", "Esta nota de pedido ya está anulada.");
            return;
        }

        new Thread(() -> {
            try {
                List<BackOrderModel> backOrders = service.obtenerBackOrders();
                boolean hasAssociatedBO = backOrders.stream().anyMatch(bo -> 
                    selectedNote.getId().equals(bo.getOrderNoteId()) &&
                    ("PENDIENTE".equalsIgnoreCase(bo.getEstado()) || "PARCIAL".equalsIgnoreCase(bo.getEstado()))
                );

                javafx.application.Platform.runLater(() -> {
                    if (hasAssociatedBO) {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Confirmar anulación");
                        confirm.setHeaderText("La Nota de Pedido posee un Back Order pendiente.");
                        confirm.setContentText("Si continúa, el Back Order también será anulado.\n\nEsta acción es irreversible.\n\n¿Desea continuar?");
                        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                            return;
                        }
                    } else {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Confirmar anulación");
                        confirm.setHeaderText(null);
                        confirm.setContentText("¿Está seguro de que desea anular la nota de pedido " + selectedNote.getFolio() + "?");
                        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                            return;
                        }
                    }

                    // Solicitar motivo
                    TextInputDialog motiveDialog = new TextInputDialog();
                    motiveDialog.setTitle("Motivo de anulación");
                    motiveDialog.setHeaderText("Ingrese el motivo de la anulación para la Nota de Pedido " + selectedNote.getFolio() + ":");
                    motiveDialog.setContentText("Motivo:");
                    Optional<String> motiveResult = motiveDialog.showAndWait();
                    if (!motiveResult.isPresent() || motiveResult.get().trim().isEmpty()) {
                        mostrarWarning("Requerido", "Debe especificar un motivo para anular la nota de pedido.");
                        return;
                    }

                    String motivo = motiveResult.get().trim();

                    new Thread(() -> {
                        try {
                            service.eliminarNotaPedido(selectedNote.getId(), motivo);
                            javafx.application.Platform.runLater(() -> {
                                selectedNote.setEstado("ANULADA");
                                selectedNote.setEstadoTracking("ANULADO");
                                selectedNote.setMotivoAnulacion(motivo);
                                cbEstado.setValue("ANULADA");
                                tablaNotas.refresh();
                                toggleEditingFields(false);
                                mostrarInformacion("Éxito", "Nota de pedido anulada exitosamente.");
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            javafx.application.Platform.runLater(() -> {
                                mostrarError("Error", "No se pudo anular: " + e.getMessage());
                            });
                        }
                    }).start();
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error", "No se pudieron validar los Back Orders asociados: " + e.getMessage());
                });
            }
        }).start();
    }

    private void limpiarFormulario() {
        isPopulatingForm = true;
        selectedNote = null;
        tablaNotas.getSelectionModel().clearSelection();
        txtFolio.clear();
        cbCliente.selectItem(null);
        cbEstado.setValue("PENDIENTE");
        if (lblEditIndicator != null) {
            lblEditIndicator.setVisible(false);
            lblEditIndicator.setManaged(false);
        }
        detailList.clear();
        cbProducto.selectItem(null);
        cbProducto.getItems().clear();
        cbProducto.setDisable(true);
        if (btnAddLine != null) btnAddLine.setDisable(true);
        if (btnExcel != null) btnExcel.setDisable(true);
        if (lblProductSupplierWarning != null) {
            lblProductSupplierWarning.setVisible(true);
            lblProductSupplierWarning.setManaged(true);
        }
        txtCantidad.clear();
        tempNoteAttributes.clear();

        String userWhId = com.logistics.packinglist.service.AuthService.getInstance().getWarehouseId();
        if (userWhId != null && warehouseMap.containsKey(userWhId)) {
            cbBodega.setValue(warehouseMap.get(userWhId));
        } else {
            cbDeposito.getSelectionModel().clearSelection();
            cbBodega.getSelectionModel().clearSelection();
        }
        cbZonaReserva.setValue("COMERCIAL");
        txtOrdenCompra.clear();
        cbProveedor.selectItem(null);
        lblFechaHora.setText("-");
        lblCreadoPor.setText("-");
        lblModificadoPor.setText("-");

        cargarSiguienteFolio();
        actualizarTotal();
        toggleEditingFields(true);
        isPopulatingForm = false;
    }

    private void toggleEditingFields(boolean editable) {
        if (btnGuardarNota != null) btnGuardarNota.setDisable(!editable);
        if (btnExtrasNota != null) btnExtrasNota.setDisable(!editable);
        if (btnEliminarNota != null) btnEliminarNota.setDisable(!editable);
        if (btnAddLine != null) btnAddLine.setDisable(!editable);
        if (btnDelLine != null) btnDelLine.setDisable(!editable);
        if (btnExcel != null) btnExcel.setDisable(!editable);
        if (btnExtrasLinea != null) btnExtrasLinea.setDisable(!editable);

        cbDeposito.setDisable(!editable);
        cbBodega.setDisable(!editable);
        txtFolio.setDisable(!editable);
        cbCliente.setDisable(!editable);
        cbEstado.setDisable(!editable);
        cbZonaReserva.setDisable(!editable);
        txtOrdenCompra.setDisable(!editable);
        cbProveedor.setDisable(!editable);

        cbProducto.setDisable(!editable);
        txtCantidad.setDisable(!editable);
    }

    private void abrirExtrasNota() {
        ExtrasDialog dialog = new ExtrasDialog(this, "Extras de la Nota de Pedido", tempNoteAttributes);
        dialog.showAndWait();
        if (dialog.isAccepted()) {
            tempNoteAttributes.clear();
            tempNoteAttributes.putAll(dialog.getAtributos());
            actualizarTotal();
        }
    }

    private void abrirExtrasLinea() {
        OrderNoteDetailModel selectedLine = tablaDetalles.getSelectionModel().getSelectedItem();
        if (selectedLine == null) {
            mostrarWarning("Selección Requerida", "Debe seleccionar una línea de producto de la tabla.");
            return;
        }
        if (selectedLine.getAtributosPersonalizados() == null) {
            selectedLine.setAtributosPersonalizados(new HashMap<>());
        }
        ExtrasDialog dialog = new ExtrasDialog(this, "Extras del Producto seleccionado", selectedLine.getAtributosPersonalizados());
        dialog.showAndWait();
        if (dialog.isAccepted()) {
            selectedLine.getAtributosPersonalizados().clear();
            selectedLine.getAtributosPersonalizados().putAll(dialog.getAtributos());
            tablaDetalles.refresh();
            actualizarTotal();
        }
    }

    private ProductModel encontrarProductoPorSKU(String sku) {
        for (ProductModel p : productMap.values()) {
            if (p.getSku() != null && p.getSku().equalsIgnoreCase(sku.trim())) {
                return p;
            }
        }
        return null;
    }

    private String getCellStringValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield (d == Math.floor(d)) ? String.format("%.0f", d) : String.valueOf(d);
            }
            default -> "";
        };
    }

    private void cargarExcelDeSKUs() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Seleccionar Excel de SKUs");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        java.io.File file = fc.showOpenDialog(this);
        if (file == null) return;

        try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
             org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(fis)) {
            
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            int importados = 0;
            int noEncontrados = 0;
            
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;
                
                if (i == 0) {
                    String col0 = getCellStringValue(row.getCell(0));
                    if (col0.equalsIgnoreCase("sku") || col0.equalsIgnoreCase("part number") || col0.equalsIgnoreCase("partnumber")) {
                        continue;
                    }
                }
                
                String sku = getCellStringValue(row.getCell(0));
                String cantStr = getCellStringValue(row.getCell(1));
                
                if (sku.isEmpty()) continue;
                
                int cantidad = 0;
                try {
                    if (!cantStr.isEmpty()) {
                        cantidad = (int) Double.parseDouble(cantStr);
                    }
                } catch (NumberFormatException ignored) {}
                
                if (cantidad <= 0) continue;
                
                ProductModel product = encontrarProductoPorSKU(sku);
                if (product != null) {
                    boolean existe = false;
                    for (OrderNoteDetailModel dl : detailList) {
                        if (dl.getProductId().equals(product.getId())) {
                            dl.setCantidadPedida(dl.getCantidadPedida() + cantidad);
                            existe = true;
                            break;
                        }
                    }
                    if (!existe) {
                        OrderNoteDetailModel line = OrderNoteDetailModel.builder()
                                .productId(product.getId())
                                .cantidadPedida(cantidad)
                                .cantidadDespachada(0)
                                .precioUnitario(product.getPrecio())
                                .atributosPersonalizados(new HashMap<>())
                                .build();
                        detailList.add(line);
                    }
                    importados++;
                } else {
                    noEncontrados++;
                }
            }
            
            tablaDetalles.refresh();
            actualizarTotal();
            
            mostrarInformacion("Carga Masiva", 
                String.format("Proceso finalizado.\nLíneas importadas/actualizadas: %d\nSKUs no encontrados: %d", importados, noEncontrados));
                
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al cargar Excel", e.getMessage());
        }
    }

    private static class ExtrasDialog extends Stage {
        private final Map<String, String> atributos;
        private final ObservableList<String> listItems = FXCollections.observableArrayList();
        private ListView<String> listView;
        private boolean accepted = false;

        public ExtrasDialog(Window owner, String titulo, Map<String, String> currentAttributes) {
            this.atributos = new HashMap<>(currentAttributes);
            initOwner(owner);
            initModality(Modality.APPLICATION_MODAL);
            setTitle(titulo);
            setMinWidth(420);
            setMinHeight(400);
            setResizable(false);

            VBox root = new VBox(10);
            root.setPadding(new Insets(15));
            root.getStyleClass().add("dialog-root");
            root.setStyle("-fx-background-color: #f5f7fb;");

            Label lblCurrent = new Label("Cargos y Descuentos Aplicados:");
            lblCurrent.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

            listView = new ListView<>(listItems);
            listView.setPrefHeight(150);
            refreshList();

            // Formulario de agregar
            GridPane grid = new GridPane();
            grid.setHgap(8);
            grid.setVgap(8);

            ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("Cargo (+)", "Descuento (-)"));
            cbTipo.setValue("Cargo (+)");
            cbTipo.setMaxWidth(Double.MAX_VALUE);

            ComboBox<String> cbFormato = new ComboBox<>(FXCollections.observableArrayList("Porcentaje (%)", "Monto Fijo ($)"));
            cbFormato.setValue("Porcentaje (%)");
            cbFormato.setMaxWidth(Double.MAX_VALUE);

            TextField txtValor = new TextField();
            txtValor.setPromptText("Ej. 19 o 5000");

            TextField txtMotivo = new TextField();
            txtMotivo.setPromptText("Ej. IVA, Flete");

            grid.add(new Label("Tipo:"), 0, 0);
            grid.add(cbTipo, 1, 0);
            grid.add(new Label("Formato:"), 2, 0);
            grid.add(cbFormato, 3, 0);

            grid.add(new Label("Valor:"), 0, 1);
            grid.add(txtValor, 1, 1);
            grid.add(new Label("Motivo:"), 2, 1);
            grid.add(txtMotivo, 3, 1);

            Button btnAdd = new Button("Agregar", new FontAwesomeIconView(FontAwesomeIcon.PLUS));
            btnAdd.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnAdd.setOnAction(e -> {
                String tipo = cbTipo.getValue();
                String formato = cbFormato.getValue();
                String valorStr = txtValor.getText().trim();
                String motivo = txtMotivo.getText().trim().replace(" ", "_");

                if (valorStr.isEmpty() || motivo.isEmpty()) {
                    mostrarAlerta("Campos Requeridos", "Debe especificar el valor y el motivo.");
                    return;
                }

                double valor;
                try {
                    valor = Double.parseDouble(valorStr);
                } catch (NumberFormatException ex) {
                    mostrarAlerta("Valor Inválido", "El valor debe ser un número válido.");
                    return;
                }

                if (valor <= 0) {
                    mostrarAlerta("Valor Inválido", "El valor debe ser mayor a cero.");
                    return;
                }

                String keyPrefix = tipo.contains("Cargo") ? "charge_" : "discount_";
                String key = keyPrefix + motivo;
                String suffix = formato.contains("Porcentaje") ? "%" : "";
                
                String finalVal = (valor == Math.floor(valor)) ? String.format("%.0f", valor) : String.valueOf(valor);
                atributos.put(key, finalVal + suffix);
                
                txtValor.clear();
                txtMotivo.clear();
                refreshList();
            });

            Button btnDelete = new Button("Eliminar Seleccionado", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            btnDelete.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnDelete.setOnAction(e -> {
                int selectedIdx = listView.getSelectionModel().getSelectedIndex();
                if (selectedIdx >= 0) {
                    String selected = listItems.get(selectedIdx);
                    String key = selected.split(" - ")[0];
                    atributos.remove(key);
                    refreshList();
                }
            });

            HBox btnRow = new HBox(10);
            btnRow.setAlignment(Pos.CENTER_RIGHT);
            
            Button btnAceptar = new Button("Aceptar", new FontAwesomeIconView(FontAwesomeIcon.CHECK));
            btnAceptar.setStyle("-fx-background-color: #2f855a; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnAceptar.setOnAction(e -> {
                accepted = true;
                close();
            });

            Button btnCancelar = new Button("Cancelar", new FontAwesomeIconView(FontAwesomeIcon.TIMES));
            btnCancelar.setStyle("-fx-background-color: #718096; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnCancelar.setOnAction(e -> close());

            btnRow.getChildren().addAll(btnAceptar, btnCancelar);

            root.getChildren().addAll(lblCurrent, listView, btnDelete, new Separator(), grid, btnAdd, new Separator(), btnRow);

            Scene scene = new Scene(root);
            try {
                scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            setScene(scene);
            ScreenUtil.fitDialogToScreen(this, getOwner(), 450, 480, 400, 380);
        }

        private void refreshList() {
            listItems.clear();
            for (Map.Entry<String, String> entry : atributos.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key.startsWith("charge_")) {
                    listItems.add(key + " - Cargo (+): " + value + " [" + key.substring(7) + "]");
                } else if (key.startsWith("discount_")) {
                    listItems.add(key + " - Descuento (-): " + value + " [" + key.substring(9) + "]");
                } else {
                    listItems.add(key + " - " + value);
                }
            }
        }

        private void mostrarAlerta(String titulo, String msg) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        }

        public boolean isAccepted() {
            return accepted;
        }

        public Map<String, String> getAtributos() {
            return atributos;
        }
    }

    private void mostrarInformacion(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarWarning(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void descargarPDF() {
        if (selectedNote == null) {
            mostrarWarning("Exportar PDF", "Debe seleccionar una Nota de Pedido de la lista.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar PDF de Nota de Pedido");
        fileChooser.setInitialFileName(selectedNote.getFolio() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));

        File file = fileChooser.showSaveDialog(this);
        if (file != null) {
            new Thread(() -> {
                try {
                    PdfNotaPedidoService pdfService = new PdfNotaPedidoService();
                    CustomerModel customer = customerMap.get(selectedNote.getCustomerId());
                    
                    String provId = selectedNote.getAtributosPersonalizados() != null ? selectedNote.getAtributosPersonalizados().get("proveedor_id") : null;
                    SupplierModel supplier = provId != null ? supplierMap.get(provId) : null;
                    
                    pdfService.exportar(selectedNote, activeCompany, customer, supplier, productMap, file);
                    
                    javafx.application.Platform.runLater(() -> {
                        mostrarInformacion("PDF Creado", "El PDF de la Nota de Pedido se ha exportado con éxito.");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        mostrarError("Error", "No se pudo generar el PDF: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void imprimirNota() {
        if (selectedNote == null) {
            mostrarWarning("Imprimir", "Debe seleccionar una Nota de Pedido de la lista.");
            return;
        }
        new Thread(() -> {
            try {
                File tempFile = File.createTempFile("PrintNP_" + selectedNote.getFolio() + "_", ".pdf");
                tempFile.deleteOnExit();
                PdfNotaPedidoService pdfService = new PdfNotaPedidoService();
                CustomerModel customer = customerMap.get(selectedNote.getCustomerId());
                String provId = selectedNote.getAtributosPersonalizados() != null ? selectedNote.getAtributosPersonalizados().get("proveedor_id") : null;
                SupplierModel supplier = provId != null ? supplierMap.get(provId) : null;
                
                pdfService.exportar(selectedNote, activeCompany, customer, supplier, productMap, tempFile);
                
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.PRINT)) {
                    java.awt.Desktop.getDesktop().print(tempFile);
                } else {
                    java.awt.Desktop.getDesktop().open(tempFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error", "No se pudo imprimir la Nota de Pedido: " + e.getMessage());
                });
            }
        }).start();
    }

    // Descarga de PDF de Picking movida al módulo Picking & Packing NP
}
