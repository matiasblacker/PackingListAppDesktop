package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.StockModel;
import com.logistics.packinglist.model.ProductModel;
import com.logistics.packinglist.model.LocationModel;
import com.logistics.packinglist.model.WarehouseModel;
import com.logistics.packinglist.model.SupplierModel;
import com.logistics.packinglist.model.ReceptionAnnouncementModel;
import com.logistics.packinglist.model.AnnouncementDetailModel;
import com.logistics.packinglist.model.WarehouseZoneModel;
import com.logistics.packinglist.service.MantenimientoService;
import com.logistics.packinglist.service.AuthService;
import com.logistics.packinglist.service.WebSocketManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import com.logistics.packinglist.model.ReceptionModel;
import com.logistics.packinglist.model.ReceptionDetailModel;
import com.logistics.packinglist.model.OrderNoteModel;
import com.logistics.packinglist.model.OrderNoteDetailModel;
import com.logistics.packinglist.model.CustomerModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import java.util.*;
import java.util.stream.Collectors;

public class StockDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

                    private final ObservableList<StockModel> masterStockListComercial;
    private final FilteredList<StockModel> filteredStockListComercial;

    private final ObservableList<StockModel> masterStockListPrimaria;
    private final FilteredList<StockModel> filteredStockListPrimaria;

    private final ObservableList<StockModel> masterStockListTotal;
    private final FilteredList<StockModel> filteredStockListTotal;

    private TableView<StockModel> tblStockComercial;
    private TableView<StockModel> tblStockPrimaria;
    private TableView<StockModel> tblStockTotal;
    private TableView<ProductHistoryRow> tblHistorial;
    private final ObservableList<ProductHistoryRow> historialList = FXCollections.observableArrayList();
    private boolean isChangingSelection = false;

    private TextField txtSkuFilter;
    private ComboBox<SupplierModel> cbProveedorFilter;

    // Detalle del Producto
    private Label lblProdSkuName;
    private Label lblProdCategory;
    private Label lblProdSupplier;
    private Label lblProdPrice;
    private Label lblProdPais;
    private Label lblProdPacking;
    private Label lblProdTotalStock;
    private VBox vboxAtributosPersonalizados;

    // Tablas de detalles
    private TableView<LocationStockRow> tblLocations;
    private TableView<AnnouncementRow> tblAnnouncements;

    private final Map<String, ProductModel> productMap = new HashMap<>();
    private final Map<String, LocationModel> locationMap = new HashMap<>();
    private final Map<String, WarehouseModel> warehouseMap = new HashMap<>();
    private final Map<String, SupplierModel> supplierMap = new HashMap<>();
    private final Map<String, WarehouseZoneModel> zoneMap = new HashMap<>();
    private final List<ReceptionAnnouncementModel> allAnnouncements = new ArrayList<>();
    private final List<StockModel> rawStockList = new ArrayList<>();
    private String selectedProductId;

    public StockDialog(Window owner) {
                                        this.masterStockListComercial = FXCollections.observableArrayList();
        this.filteredStockListComercial = new FilteredList<>(masterStockListComercial, p -> true);

        this.masterStockListPrimaria = FXCollections.observableArrayList();
        this.filteredStockListPrimaria = new FilteredList<>(masterStockListPrimaria, p -> true);

        this.masterStockListTotal = FXCollections.observableArrayList();
        this.filteredStockListTotal = new FilteredList<>(masterStockListTotal, p -> true);

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Inventario General");

        setMinWidth(950);
        setMinHeight(520);

        construirUI();
        cargarDatos();

        WebSocketManager.UpdateListener stockUpdateListener = action -> {
            System.out.println("[StockDialog] Received real-time update event: " + action);
            cargarDatos();
        };
        WebSocketManager.getInstance().subscribe("STOCK", stockUpdateListener);
        setOnCloseRequest(e -> WebSocketManager.getInstance().unsubscribe("STOCK", stockUpdateListener));
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // --- ENCABEZADO ---
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label("Inventario General de Existencias", new FontAwesomeIconView(FontAwesomeIcon.DATABASE));
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSubtitle = new Label("Consulte, filtre y realice trazabilidad del stock físico por proveedor y SKU.");
        lblSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSubtitle);

        // --- FILTROS ---
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(10, 14, 10, 14));
        filterBar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        txtSkuFilter = new TextField();
        txtSkuFilter.setPromptText("Buscar por SKU o descripción...");
        txtSkuFilter.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtSkuFilter.setPrefWidth(250);
        txtSkuFilter.textProperty().addListener((obs, old, newVal) -> aplicarFiltros());

        cbProveedorFilter = new ComboBox<>();
        cbProveedorFilter.setPromptText("Seleccione Proveedor");
        cbProveedorFilter.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px; -fx-border-radius: 4px;");
        cbProveedorFilter.setPrefWidth(280);
        cbProveedorFilter.valueProperty().addListener((obs, old, newVal) -> aplicarFiltros());

        Button btnClear = new Button("Limpiar Filtros", new FontAwesomeIconView(FontAwesomeIcon.REFRESH));
        btnClear.setStyle("-fx-font-size: 10px; -fx-padding: 3px 8px; -fx-background-color: #e9ecef; -fx-text-fill: #495057; -fx-cursor: hand; -fx-font-weight: bold;");
        btnClear.setOnAction(e -> {
            txtSkuFilter.clear();
            if (!cbProveedorFilter.getItems().isEmpty()) {
                cbProveedorFilter.getSelectionModel().select(0);
            }
        });

        Label lblBuscar = new Label("Buscar SKU/Nombre:");
        lblBuscar.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblProv = new Label("Proveedor:");
        lblProv.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");

        filterBar.getChildren().addAll(
                lblBuscar, txtSkuFilter,
                lblProv, cbProveedorFilter,
                btnClear
        );

        // --- SPLIT PANE 50/50 ---
        SplitPane mainSplit = new SplitPane();
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.setDividerPositions(0.5);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        // --- LADO IZQUIERDO: TABLA DE STOCK CON TABPANE (WHITE CARD) ---
        VBox leftBox = new VBox(10);
        leftBox.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-tab-drag-policy: REORDER;");
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        tblStockComercial = crearTablaStock(filteredStockListComercial, "COMERCIAL");
        tblStockPrimaria = crearTablaStock(filteredStockListPrimaria, "PRIMARIA");
        tblStockTotal = crearTablaStock(filteredStockListTotal, "TOTAL");
        tblHistorial = crearTablaHistorial();

        Tab tabComercial = new Tab("Zona Comercial", tblStockComercial);
        tabComercial.setClosable(false);
        Tab tabPrimaria = new Tab("Zona Primaria", tblStockPrimaria);
        tabPrimaria.setClosable(false);
        Tab tabTotal = new Tab("Stock Total", tblStockTotal);
        tabTotal.setClosable(false);
        Tab tabHistorial = new Tab("Historial del Producto", tblHistorial);
        tabHistorial.setClosable(false);

        tabPane.getTabs().addAll(tabComercial, tabPrimaria, tabTotal, tabHistorial);
        leftBox.getChildren().addAll(tabPane);

        // --- LADO DERECHO: PANEL DE DETALLES (WHITE CARD EN SCROLLPANE) ---
        VBox rightBox = new VBox(12);
        rightBox.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        ScrollPane rightScroll = new ScrollPane(rightBox);
        rightScroll.setFitToWidth(true);
        rightScroll.setFitToHeight(true);
        rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Ficha del Producto
        VBox cardProduct = new VBox(10);
        cardProduct.setPadding(new Insets(12));
        cardProduct.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");

        lblProdSkuName = new Label("Seleccione un producto");
        lblProdSkuName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        GridPane prodGrid = new GridPane();
        prodGrid.setHgap(10);
        prodGrid.setVgap(6);

        Label lblCatTitle = new Label("Categoría:");
        lblCatTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        lblProdCategory = new Label("-");
        lblProdCategory.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblProvTitle = new Label("Proveedor:");
        lblProvTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        lblProdSupplier = new Label("-");
        lblProdSupplier.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblPriceTitle = new Label("Precio Lista:");
        lblPriceTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        lblProdPrice = new Label("-");
        lblProdPrice.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblPaisTitle = new Label("País Origen:");
        lblPaisTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        lblProdPais = new Label("-");
        lblProdPais.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblPackTitle = new Label("Packing:");
        lblPackTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        lblProdPacking = new Label("-");
        lblProdPacking.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblTotalTitle = new Label("Stock Total:");
        lblTotalTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        lblProdTotalStock = new Label("-");
        lblProdTotalStock.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #2B6CB0;");

        prodGrid.add(lblCatTitle, 0, 0); prodGrid.add(lblProdCategory, 1, 0);
        prodGrid.add(lblProvTitle, 0, 1); prodGrid.add(lblProdSupplier, 1, 1);
        prodGrid.add(lblPriceTitle, 0, 2); prodGrid.add(lblProdPrice, 1, 2);
        prodGrid.add(lblPaisTitle, 0, 3); prodGrid.add(lblProdPais, 1, 3);
        prodGrid.add(lblPackTitle, 0, 4); prodGrid.add(lblProdPacking, 1, 4);
        prodGrid.add(lblTotalTitle, 0, 5); prodGrid.add(lblProdTotalStock, 1, 5);

        vboxAtributosPersonalizados = new VBox(4);
        vboxAtributosPersonalizados.setPadding(new Insets(5, 0, 0, 0));

        Label lblAttrTitle = new Label("Atributos Personalizados:");
        lblAttrTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        cardProduct.getChildren().addAll(lblProdSkuName, prodGrid, new Separator(), lblAttrTitle, vboxAtributosPersonalizados);

        // Card 2: Ubicaciones Físicas en Bodega
        VBox cardLocations = new VBox(6);
        HBox locHeader = new HBox(10);
        locHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblLocTitle = new Label("Ubicaciones Físicas (Stock)");
        lblLocTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1a202c;");
        HBox.setHgrow(lblLocTitle, Priority.ALWAYS);

        Button btnReubicar = new Button("Reubicar Stock", new FontAwesomeIconView(FontAwesomeIcon.EXCHANGE));
        btnReubicar.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        btnReubicar.setOnAction(e -> abrirDialogoReubicacion());
        locHeader.getChildren().addAll(lblLocTitle, btnReubicar);

        tblLocations = new TableView<>();
        tblLocations.setPrefHeight(120);
        tblLocations.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LocationStockRow, String> colLocName = new TableColumn<>("Ubicación (Ámbito)");
        colLocName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocationName()));

        TableColumn<LocationStockRow, Integer> colLocQty = new TableColumn<>("Cantidad");
        colLocQty.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getQuantity()));

        tblLocations.getColumns().addAll(colLocName, colLocQty);
        cardLocations.getChildren().addAll(locHeader, tblLocations);

        // Card 3: Anuncios de Carga Relacionados
        VBox cardAnnouncements = new VBox(6);
        Label lblAnunTitle = new Label("Anuncios de Carga Relacionados");
        lblAnunTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1a202c;");

        tblAnnouncements = new TableView<>();
        tblAnnouncements.setPrefHeight(120);
        tblAnnouncements.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<AnnouncementRow, String> colAnunFolio = new TableColumn<>("Folio Anuncio");
        colAnunFolio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolio()));

        TableColumn<AnnouncementRow, String> colAnunFecha = new TableColumn<>("Fecha");
        colAnunFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate()));

        TableColumn<AnnouncementRow, String> colAnunEstado = new TableColumn<>("Estado");
        colAnunEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        TableColumn<AnnouncementRow, Integer> colAnunQty = new TableColumn<>("Cant. Anunciada");
        colAnunQty.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getAnnouncedQty()));

        tblAnnouncements.getColumns().addAll(colAnunFolio, colAnunFecha, colAnunEstado, colAnunQty);
        cardAnnouncements.getChildren().addAll(lblAnunTitle, tblAnnouncements);

        rightBox.getChildren().addAll(cardProduct, cardLocations, cardAnnouncements);

        mainSplit.getItems().addAll(leftBox, rightScroll);

        tblStockComercial.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (isChangingSelection) return;
            if (newVal != null) {
                isChangingSelection = true;
                try {
                    tblStockPrimaria.getSelectionModel().clearSelection();
                    tblStockTotal.getSelectionModel().clearSelection();
                } finally {
                    isChangingSelection = false;
                }
                mostrarDetallesProducto(newVal.getProductId());
            } else if (!isChangingSelection && tblStockPrimaria.getSelectionModel().getSelectedItem() == null &&
                       tblStockTotal.getSelectionModel().getSelectedItem() == null) {
                limpiarDetallesProducto();
            }
        });
        tblStockPrimaria.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (isChangingSelection) return;
            if (newVal != null) {
                isChangingSelection = true;
                try {
                    tblStockComercial.getSelectionModel().clearSelection();
                    tblStockTotal.getSelectionModel().clearSelection();
                } finally {
                    isChangingSelection = false;
                }
                mostrarDetallesProducto(newVal.getProductId());
            } else if (!isChangingSelection && tblStockComercial.getSelectionModel().getSelectedItem() == null &&
                       tblStockTotal.getSelectionModel().getSelectedItem() == null) {
                limpiarDetallesProducto();
            }
        });
        tblStockTotal.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (isChangingSelection) return;
            if (newVal != null) {
                isChangingSelection = true;
                try {
                    tblStockComercial.getSelectionModel().clearSelection();
                    tblStockPrimaria.getSelectionModel().clearSelection();
                } finally {
                    isChangingSelection = false;
                }
                mostrarDetallesProducto(newVal.getProductId());
            } else if (!isChangingSelection && tblStockComercial.getSelectionModel().getSelectedItem() == null &&
                       tblStockPrimaria.getSelectionModel().getSelectedItem() == null) {
                limpiarDetallesProducto();
            }
        });

        tblHistorial.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (isChangingSelection) return;
            if (newVal != null) {
                isChangingSelection = true;
                try {
                    tblStockComercial.getSelectionModel().clearSelection();
                    tblStockPrimaria.getSelectionModel().clearSelection();
                    tblStockTotal.getSelectionModel().clearSelection();
                } finally {
                    isChangingSelection = false;
                }
                mostrarDetallesProducto(newVal.getProductId());
            }
        });

        root.getChildren().addAll(headerBox, filterBar, mainSplit);

        Scene scene = new Scene(root, 1100, 640);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception ignored) {}
        setScene(scene);
        ScreenUtil.fitDialogToScreen(this, getOwner(), 1100, 640, 950, 520);
    }

    public void cargarDatos() {
        new Thread(() -> {
            try {
                String activeCompanyId = AuthService.getInstance().getCompanyId();
                String activeWarehouseId = AuthService.getInstance().getWarehouseId();

                List<WarehouseModel> warehouses = service.obtenerBodegas();
                List<ProductModel> products = service.obtenerProductos();
                List<LocationModel> locations;
                if (activeWarehouseId != null && !activeWarehouseId.isEmpty()) {
                    locations = new ArrayList<>(service.obtenerUbicacionesPorBodega(activeWarehouseId));
                } else {
                    locations = new ArrayList<>(service.obtenerUbicaciones());
                }
                locations.sort(LocationModel.NATURAL_ORDER_COMPARATOR);
                List<SupplierModel> suppliers = service.obtenerProveedores();
                List<ReceptionAnnouncementModel> announcements = service.obtenerAnuncios();
                List<StockModel> stocks = service.obtenerStocks();
                List<WarehouseZoneModel> zones = new ArrayList<>();
                if (activeWarehouseId != null && !activeWarehouseId.isEmpty()) {
                    zones = service.obtenerZonasPorBodega(activeWarehouseId);
                }

                final List<WarehouseZoneModel> finalZones = zones;
                javafx.application.Platform.runLater(() -> {
                    warehouseMap.clear();
                    for (WarehouseModel w : warehouses) {
                        warehouseMap.put(w.getId(), w);
                    }

                    productMap.clear();
                    for (ProductModel p : products) {
                        productMap.put(p.getId(), p);
                    }
                    System.out.println("[StockDialog] CargarDatos: " + productMap.size() + " productos cargados en productMap.");

                    locationMap.clear();
                    for (LocationModel l : locations) {
                        locationMap.put(l.getId(), l);
                    }

                    zoneMap.clear();
                    for (WarehouseZoneModel z : finalZones) {
                        zoneMap.put(z.getId(), z);
                    }

                    supplierMap.clear();
                    for (SupplierModel s : suppliers) {
                        supplierMap.put(s.getId(), s);
                    }

                    allAnnouncements.clear();
                    allAnnouncements.addAll(announcements);

                    // Poblar ComboBox de Proveedores
                    cbProveedorFilter.getItems().clear();
                    SupplierModel allSuppliers = new SupplierModel();
                    allSuppliers.setId(null);
                    allSuppliers.setRazonSocial("--- Todos los Proveedores ---");
                    cbProveedorFilter.getItems().add(allSuppliers);

                    List<SupplierModel> tenantSuppliers = suppliers.stream()
                            .filter(s -> activeCompanyId == null || activeCompanyId.equals(s.getCompanyId()))
                            .collect(Collectors.toList());
                    cbProveedorFilter.getItems().addAll(tenantSuppliers);
                    cbProveedorFilter.setValue(allSuppliers);

                    // Filtrar la lista de stock por empresa y bodega activa
                    List<StockModel> filteredStocks = stocks.stream()
                            .filter(s -> {
                                if (activeCompanyId != null && !activeCompanyId.equals(s.getCompanyId())) return false;
                                LocationModel loc = locationMap.get(s.getLocationId());
                                return loc != null && activeWarehouseId != null && activeWarehouseId.equals(loc.getWarehouseId());
                            })
                            .collect(Collectors.toList());

                    rawStockList.clear();
                    rawStockList.addAll(filteredStocks);

                    // Segmentar stock por tipoStock
                    List<StockModel> commercialStocks = new ArrayList<>();
                    List<StockModel> primaryStocks = new ArrayList<>();

                    for (StockModel s : filteredStocks) {
                        String dest = s.getTipoStock();
                        if (dest == null) dest = "COMERCIAL";
                        if ("PRIMARIA".equalsIgnoreCase(dest)) {
                            primaryStocks.add(s);
                        } else {
                            commercialStocks.add(s);
                        }
                    }

                    // Agregar por producto
                    Map<String, StockModel> aggregatedComercial = new java.util.LinkedHashMap<>();
                    for (StockModel s : commercialStocks) {
                        StockModel existing = aggregatedComercial.get(s.getProductId());
                        if (existing == null) {
                            StockModel copy = new StockModel();
                            copy.setId(s.getId());
                            copy.setProductId(s.getProductId());
                            copy.setCompanyId(s.getCompanyId());
                            copy.setLocationId(s.getLocationId());
                            copy.setCantidad(s.getCantidad() != null ? s.getCantidad() : 0);
                            aggregatedComercial.put(s.getProductId(), copy);
                        } else {
                            existing.setCantidad(existing.getCantidad() + (s.getCantidad() != null ? s.getCantidad() : 0));
                        }
                    }

                    Map<String, StockModel> aggregatedPrimaria = new java.util.LinkedHashMap<>();
                    for (StockModel s : primaryStocks) {
                        StockModel existing = aggregatedPrimaria.get(s.getProductId());
                        if (existing == null) {
                            StockModel copy = new StockModel();
                            copy.setId(s.getId());
                            copy.setProductId(s.getProductId());
                            copy.setCompanyId(s.getCompanyId());
                            copy.setLocationId(s.getLocationId());
                            copy.setCantidad(s.getCantidad() != null ? s.getCantidad() : 0);
                            aggregatedPrimaria.put(s.getProductId(), copy);
                        } else {
                            existing.setCantidad(existing.getCantidad() + (s.getCantidad() != null ? s.getCantidad() : 0));
                        }
                    }

                    Map<String, StockModel> aggregatedTotal = new java.util.LinkedHashMap<>();
                    for (StockModel s : filteredStocks) {
                        StockModel existing = aggregatedTotal.get(s.getProductId());
                        if (existing == null) {
                            StockModel copy = new StockModel();
                            copy.setId(s.getId());
                            copy.setProductId(s.getProductId());
                            copy.setCompanyId(s.getCompanyId());
                            copy.setLocationId(s.getLocationId());
                            copy.setCantidad(s.getCantidad() != null ? s.getCantidad() : 0);
                            aggregatedTotal.put(s.getProductId(), copy);
                        } else {
                            existing.setCantidad(existing.getCantidad() + (s.getCantidad() != null ? s.getCantidad() : 0));
                        }
                    }

                    masterStockListComercial.clear();
                    masterStockListComercial.addAll(aggregatedComercial.values());

                    masterStockListPrimaria.clear();
                    masterStockListPrimaria.addAll(aggregatedPrimaria.values());

                    masterStockListTotal.clear();
                    masterStockListTotal.addAll(aggregatedTotal.values());
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error de carga", "No se pudieron cargar los datos del inventario: " + e.getMessage());
                });
            }
        }).start();
    }

    private void aplicarFiltros() {
        String skuQuery = txtSkuFilter.getText().toLowerCase().trim();
        SupplierModel selectedSupplier = cbProveedorFilter.getValue();
        String supplierId = (selectedSupplier != null) ? selectedSupplier.getId() : null;

        java.util.function.Predicate<StockModel> predicate = stock -> {
            ProductModel prod = productMap.get(stock.getProductId());
            if (prod == null) return false;

            if (!skuQuery.isEmpty()) {
                boolean matchesSku = prod.getSku() != null && prod.getSku().toLowerCase().contains(skuQuery);
                boolean matchesName = prod.getNombre() != null && prod.getNombre().toLowerCase().contains(skuQuery);
                if (!matchesSku && !matchesName) return false;
            }

            if (supplierId != null) {
                if (prod.getSupplierId() == null || !supplierId.equals(prod.getSupplierId())) {
                    return false;
                }
            }

            return true;
        };

        filteredStockListComercial.setPredicate(predicate);
        filteredStockListPrimaria.setPredicate(predicate);
        filteredStockListTotal.setPredicate(predicate);
    }

    private void mostrarDetallesProducto(String productId) {
        this.selectedProductId = productId;
        System.out.println("[StockDialog] Solicitando detalles para producto ID: '" + productId + "'");
        ProductModel prod = productMap.get(productId);
        if (prod == null) {
            System.out.println("[StockDialog] ERROR: Producto ID '" + productId + "' no encontrado en el mapa!");
            limpiarDetallesProducto();
            return;
        }
        System.out.println("[StockDialog] Producto encontrado: SKU=" + prod.getSku() + ", Nombre=" + prod.getNombre() + ", Categoria=" + prod.getCategoria() + ", Pais=" + prod.getPaisOrigen());

        SupplierModel sup = supplierMap.get(prod.getSupplierId());

        Platform.runLater(() -> {
            lblProdSkuName.setText(prod.getSku() + " - " + prod.getNombre());
            lblProdCategory.setText(prod.getCategoria() != null ? prod.getCategoria() : "GENERAL");
            lblProdSupplier.setText(sup != null ? sup.getRazonSocial() : "Desconocido");
            lblProdPrice.setText("$ " + prod.getPrecioLista() + " " + (prod.getMoneda() != null ? prod.getMoneda() : ""));
            lblProdPais.setText(prod.getPaisOrigen() != null ? prod.getPaisOrigen() : "No especificado");
            lblProdPacking.setText(prod.getPacking() != null ? prod.getPacking() : "No especificado");

            // Atributos personalizados
            vboxAtributosPersonalizados.getChildren().clear();
            if (prod.getAtributosPersonalizados() != null && !prod.getAtributosPersonalizados().isEmpty()) {
                for (Map.Entry<String, String> entry : prod.getAtributosPersonalizados().entrySet()) {
                    String key = entry.getKey();
                    if (Arrays.asList("sku", "nombre", "precio", "precio_lista", "categoria", "packing", "pais_origen", "unidad_medida", "barcode").contains(key)) {
                        continue;
                    }
                    Label lblAttr = new Label(key.toUpperCase() + ": " + entry.getValue());
                    lblAttr.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #555; -fx-padding: 2 6 2 6; -fx-background-color: #e9ecef; -fx-background-radius: 3;");
                    vboxAtributosPersonalizados.getChildren().add(lblAttr);
                }
            }

            // Distribución física (Otras ubicaciones del producto en stock)
            int totalStock = 0;
            List<LocationStockRow> locRows = new ArrayList<>();
            for (StockModel s : rawStockList) {
                if (productId.equals(s.getProductId())) {
                    LocationModel loc = locationMap.get(s.getLocationId());
                    String dest = s.getTipoStock() != null ? s.getTipoStock() : "COMERCIAL";
                    String locStr = loc != null ? loc.toString() + " (" + dest + ")" : "Ubicación Desconocida";
                    locRows.add(new LocationStockRow(locStr, s.getCantidad()));
                    totalStock += s.getCantidad() != null ? s.getCantidad() : 0;
                }
            }
            tblLocations.getItems().setAll(locRows);
            lblProdTotalStock.setText(totalStock + " unidades");

            // Anuncios de carga relacionados
            List<AnnouncementRow> annRows = new ArrayList<>();
            for (ReceptionAnnouncementModel a : allAnnouncements) {
                for (AnnouncementDetailModel d : a.getDetails()) {
                    if (productId.equals(d.getProductId())) {
                        String cleanFecha = a.getFecha();
                        if (cleanFecha != null && cleanFecha.contains("T")) {
                            cleanFecha = cleanFecha.split("T")[0];
                        }
                        annRows.add(new AnnouncementRow(a.getFolio(), cleanFecha, a.getEstado(), d.getCantidad()));
                        break;
                    }
                }
            }
            tblAnnouncements.getItems().setAll(annRows);
        });

        // Cargar Historial del Producto
        cargarHistorialProducto(productId);
    }

    private void limpiarDetallesProducto() {
        this.selectedProductId = null;
        lblProdSkuName.setText("Seleccione un producto");
        lblProdCategory.setText("-");
        lblProdSupplier.setText("-");
        lblProdPrice.setText("-");
        lblProdPais.setText("-");
        lblProdPacking.setText("-");
        lblProdTotalStock.setText("-");
        vboxAtributosPersonalizados.getChildren().clear();
        tblLocations.getItems().clear();
        tblAnnouncements.getItems().clear();
        historialList.clear();
    }

    private void cargarHistorialProducto(String productId) {
        if (productId == null) {
            historialList.clear();
            return;
        }

        ProductModel prod = productMap.get(productId);
        if (prod == null) {
            historialList.clear();
            return;
        }

        new Thread(() -> {
            try {
                List<ReceptionAnnouncementModel> anuncios = service.obtenerAnuncios();
                List<ReceptionModel> recepciones = service.obtenerRecepciones();
                List<OrderNoteModel> orderNotes = service.obtenerNotasPedido();
                List<CustomerModel> clientes = service.obtenerClientes();

                Map<String, CustomerModel> custMap = new HashMap<>();
                if (clientes != null) {
                    for (CustomerModel c : clientes) custMap.put(c.getId(), c);
                }

                Map<String, ReceptionAnnouncementModel> anuncioMap = new HashMap<>();
                if (anuncios != null) {
                    for (ReceptionAnnouncementModel a : anuncios) anuncioMap.put(a.getId(), a);
                }

                List<ProductHistoryRow> history = new ArrayList<>();

                // 1. Recepciones / Ingresos
                if (recepciones != null) {
                    for (ReceptionModel r : recepciones) {
                        if (r.getDetails() != null) {
                            for (ReceptionDetailModel d : r.getDetails()) {
                                if (productId.equals(d.getProductId())) {
                                    String bodNom = warehouseMap.containsKey(r.getWarehouseId()) ? warehouseMap.get(r.getWarehouseId()).getNombre() : "Bodega";
                                    ReceptionAnnouncementModel anc = anuncioMap.get(r.getAnnouncementId());
                                    String folioAnc = anc != null ? anc.getFolio() : "-";
                                    String fechaRec = r.getFecha() != null ? r.getFecha() : (anc != null ? anc.getFecha() : "");
                                    String anio = "-";
                                    if (fechaRec != null && fechaRec.length() >= 4) {
                                        anio = fechaRec.substring(0, 4);
                                    }
                                    int cantRec = d.getCantidad() != null ? d.getCantidad() : 0;

                                    history.add(new ProductHistoryRow(
                                            productId,
                                            prod.getSku(),
                                            prod.getNombre(),
                                            prod.getCategoria() != null ? prod.getCategoria() : "GENERAL",
                                            bodNom,
                                            folioAnc,
                                            r.getFolio(),
                                            "Ingreso",
                                            cantRec,
                                            0,
                                            0,
                                            "-",
                                            "-",
                                            anio,
                                            fechaRec
                                    ));
                                }
                            }
                        }
                    }
                }

                // 2. Notas de Pedido / Reservas, Salidas y Back Orders (RN-SYNC-006)
                if (orderNotes != null) {
                    for (OrderNoteModel np : orderNotes) {
                        if ("ANULADA".equalsIgnoreCase(np.getEstado()) || "ANULADO".equalsIgnoreCase(np.getEstado())) continue;
                        if (np.getDetails() != null) {
                            for (OrderNoteDetailModel d : np.getDetails()) {
                                if (productId.equals(d.getProductId())) {
                                    String bodNom = warehouseMap.containsKey(np.getWarehouseId()) ? warehouseMap.get(np.getWarehouseId()).getNombre() : "Bodega";
                                    CustomerModel cust = custMap.get(np.getCustomerId());
                                    String cliNom = cust != null ? cust.getRazonSocial() : "-";
                                    String fechaNP = np.getFecha() != null ? np.getFecha() : "";

                                    int pedida = d.getCantidadPedida() != null ? d.getCantidadPedida() : 0;
                                    int bo = d.getCantidadBackOrder() != null ? d.getCantidadBackOrder() : 0;
                                    int cantRes = (d.getCantidadReservada() != null && d.getCantidadReservada() > 0) ? d.getCantidadReservada() : Math.max(0, pedida - bo);

                                    String movType = "CONFIRMADA".equalsIgnoreCase(np.getEstado()) ? "Salida" : "Reserva";

                                    if (cantRes > 0) {
                                        history.add(new ProductHistoryRow(
                                                productId,
                                                prod.getSku(),
                                                prod.getNombre(),
                                                prod.getCategoria() != null ? prod.getCategoria() : "GENERAL",
                                                bodNom,
                                                "-",
                                                "-",
                                                movType,
                                                0,
                                                cantRes,
                                                0,
                                                np.getFolio(),
                                                cliNom,
                                                "-",
                                                fechaNP
                                        ));
                                    }


                                }
                            }
                        }
                    }
                }

                // RN-SYNC-005: Ordenar cronológicamente por Fecha -> Hora -> Movimiento (ASC)
                history.sort((a, b) -> {
                    int cmpDate = a.getFechaHora().compareTo(b.getFechaHora());
                    if (cmpDate != 0) return cmpDate;
                    return a.getMovimiento().compareTo(b.getMovimiento());
                });

                // Recalcular saldo acumulado en secuencia cronológica
                int runningBalance = 0;
                for (ProductHistoryRow row : history) {
                    runningBalance = runningBalance + row.getEntrada() - row.getSalida();
                    if (runningBalance < 0) runningBalance = 0;
                    row.setStockSaldo(runningBalance);
                }

                Platform.runLater(() -> {
                    historialList.clear();
                    historialList.addAll(history);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private TableView<ProductHistoryRow> crearTablaHistorial() {
        TableView<ProductHistoryRow> table = new TableView<>(historialList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductHistoryRow, String> colFechaHora = new TableColumn<>("Fecha / Hora");
        colFechaHora.setCellValueFactory(c -> {
            String f = c.getValue().getFechaHora();
            if (f != null && f.contains("T")) {
                f = f.replace("T", " ");
                if (f.length() > 19) f = f.substring(0, 19);
            }
            return new SimpleStringProperty(f != null && !f.isEmpty() ? f : "-");
        });
        colFechaHora.setPrefWidth(120);

        TableColumn<ProductHistoryRow, String> colSku = new TableColumn<>("SKU");
        colSku.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSku()));
        colSku.setPrefWidth(85);

        TableColumn<ProductHistoryRow, String> colNom = new TableColumn<>("Descripción / Nombre");
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colNom.setPrefWidth(140);

        TableColumn<ProductHistoryRow, String> colCat = new TableColumn<>("Categoría");
        colCat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoria()));
        colCat.setPrefWidth(90);

        TableColumn<ProductHistoryRow, String> colBod = new TableColumn<>("Bodega");
        colBod.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBodega()));
        colBod.setPrefWidth(100);

        TableColumn<ProductHistoryRow, String> colAnun = new TableColumn<>("Folio Anuncio");
        colAnun.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolioAnuncio()));
        colAnun.setPrefWidth(100);

        TableColumn<ProductHistoryRow, String> colRec = new TableColumn<>("Folio Recepción");
        colRec.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolioRecepcion()));
        colRec.setPrefWidth(100);

        TableColumn<ProductHistoryRow, String> colMov = new TableColumn<>("Movimiento");
        colMov.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMovimiento()));
        colMov.setPrefWidth(80);

        TableColumn<ProductHistoryRow, Integer> colEnt = new TableColumn<>("Entrada");
        colEnt.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getEntrada()));
        colEnt.setPrefWidth(65);

        TableColumn<ProductHistoryRow, Integer> colSal = new TableColumn<>("Salida");
        colSal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getSalida()));
        colSal.setPrefWidth(65);

        TableColumn<ProductHistoryRow, Integer> colSaldo = new TableColumn<>("Stock (Saldo)");
        colSaldo.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getStockSaldo()));
        colSaldo.setPrefWidth(85);

        TableColumn<ProductHistoryRow, String> colNP = new TableColumn<>("N° NP");
        colNP.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolioNP()));
        colNP.setPrefWidth(90);

        TableColumn<ProductHistoryRow, String> colCli = new TableColumn<>("Cliente");
        colCli.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCliente()));
        colCli.setPrefWidth(110);

        TableColumn<ProductHistoryRow, String> colAnio = new TableColumn<>("Año Anuncio");
        colAnio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAnioAnuncio()));
        colAnio.setPrefWidth(80);

        table.getColumns().addAll(colFechaHora, colSku, colNom, colCat, colBod, colAnun, colRec, colMov, colEnt, colSal, colSaldo, colNP, colCli, colAnio);
        return table;
    }

    private TableView<StockModel> crearTablaStock(FilteredList<StockModel> list, String tipo) {
        TableView<StockModel> table = new TableView<>();
        table.setItems(list);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<StockModel, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(c -> {
            ProductModel prod = productMap.get(c.getValue().getProductId());
            return new SimpleStringProperty(prod != null ? prod.getSku() + " - " + prod.getNombre() : "Desconocido");
        });
        colProd.setPrefWidth(220);

        TableColumn<StockModel, String> colLoc = new TableColumn<>("Ubicación Física");
        colLoc.setCellValueFactory(c -> {
            String productId = c.getValue().getProductId();
            List<String> locNames = new ArrayList<>();
            for (StockModel s : rawStockList) {
                if (productId.equals(s.getProductId())) {
                    LocationModel loc = locationMap.get(s.getLocationId());
                    if (loc != null) {
                        String locAmbito = s.getTipoStock() != null ? s.getTipoStock() : "COMERCIAL";
                        if ("TOTAL".equals(tipo) || tipo.equalsIgnoreCase(locAmbito)) {
                            locNames.add(loc.toString());
                        }
                    }
                }
            }
            Collections.sort(locNames);
            return new SimpleStringProperty(locNames.isEmpty() ? "-" : String.join(", ", locNames));
        });
        colLoc.setPrefWidth(120);

        TableColumn<StockModel, Integer> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getCantidad()));
        colCant.setPrefWidth(80);

        table.getColumns().addAll(colProd, colLoc, colCant);
        return table;
    }

    private String determinarAmbitoZona(String zoneId) {
        if (zoneId == null) {
            return "COMERCIAL";
        }
        String currentId = zoneId;
        while (currentId != null) {
            WarehouseZoneModel zone = zoneMap.get(currentId);
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

    private void mostrarError(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void abrirDialogoReubicacion() {
        if (selectedProductId == null) {
            StockModel sel = tblStockComercial.getSelectionModel().getSelectedItem();
            if (sel == null) sel = tblStockPrimaria.getSelectionModel().getSelectedItem();
            if (sel == null) sel = tblStockTotal.getSelectionModel().getSelectedItem();
            if (sel != null) {
                mostrarDetallesProducto(sel.getProductId());
            }
        }

        if (selectedProductId == null) {
            mostrarError("Selección requerida", "Debe seleccionar un producto del listado de inventario para reubicar.");
            return;
        }

        LocationStockRow selectedLocRow = tblLocations.getSelectionModel().getSelectedItem();
        if (selectedLocRow == null) {
            if (!tblLocations.getItems().isEmpty()) {
                selectedLocRow = tblLocations.getItems().get(0);
                tblLocations.getSelectionModel().select(0);
            } else {
                mostrarError("Sin Ubicaciones", "El producto seleccionado no posee existencias físicas en ubicaciones para reubicar.");
                return;
            }
        }

        ProductModel product = productMap.get(selectedProductId);
        if (product == null) return;

        String fullLocName = selectedLocRow.getLocationName();
        String locNameOnly = fullLocName.split(" \\(")[0];
        String tipoStock = fullLocName.contains("(PRIMARIA)") ? "PRIMARIA" : "COMERCIAL";

        LocationModel fromLocation = null;
        for (LocationModel loc : locationMap.values()) {
            if (loc.toString().equals(locNameOnly)) {
                fromLocation = loc;
                break;
            }
        }

        if (fromLocation == null) {
            mostrarError("Error", "No se pudo identificar la ubicación de origen: " + locNameOnly);
            return;
        }

        List<LocationModel> destLocations = new ArrayList<>();
        String currentWarehouseId = fromLocation.getWarehouseId();
        for (LocationModel loc : locationMap.values()) {
            if (currentWarehouseId.equals(loc.getWarehouseId()) && !loc.getId().equals(fromLocation.getId())) {
                String locAmbito = determinarAmbitoZona(loc.getZoneId());
                if (tipoStock.equalsIgnoreCase(locAmbito)) {
                    destLocations.add(loc);
                }
            }
        }
        destLocations.sort(LocationModel.NATURAL_ORDER_COMPARATOR);

        ReubicarStockDialog dialog = new ReubicarStockDialog(
                this, 
                service, 
                product, 
                fromLocation, 
                selectedLocRow.getQuantity(), 
                tipoStock, 
                destLocations
        );
        dialog.showAndWait();
        cargarDatos();
    }

    // Helper row classes
    public static class LocationStockRow {
        private final String locationName;
        private final int quantity;

        public LocationStockRow(String locationName, int quantity) {
            this.locationName = locationName;
            this.quantity = quantity;
        }

        public String getLocationName() { return locationName; }
        public int getQuantity() { return quantity; }
    }

    public static class AnnouncementRow {
        private final String folio;
        private final String date;
        private final String status;
        private final int announcedQty;

        public AnnouncementRow(String folio, String date, String status, int announcedQty) {
            this.folio = folio;
            this.date = date;
            this.status = status;
            this.announcedQty = announcedQty;
        }

        public String getFolio() { return folio; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
        public int getAnnouncedQty() { return announcedQty; }
    }

    public static class ProductHistoryRow {
        private final String productId;
        private final String sku;
        private final String nombre;
        private final String categoria;
        private final String bodega;
        private final String folioAnuncio;
        private final String folioRecepcion;
        private final String movimiento;
        private final int entrada;
        private final int salida;
        private int stockSaldo;
        private final String folioNP;
        private final String cliente;
        private final String anioAnuncio;
        private final String fechaHora;

        public ProductHistoryRow(String productId, String sku, String nombre, String categoria, String bodega, String folioAnuncio, String folioRecepcion, String movimiento, int entrada, int salida, int stockSaldo, String folioNP, String cliente, String anioAnuncio, String fechaHora) {
            this.productId = productId;
            this.sku = sku;
            this.nombre = nombre;
            this.categoria = categoria;
            this.bodega = bodega;
            this.folioAnuncio = folioAnuncio;
            this.folioRecepcion = folioRecepcion;
            this.movimiento = movimiento;
            this.entrada = entrada;
            this.salida = salida;
            this.stockSaldo = stockSaldo;
            this.folioNP = folioNP;
            this.cliente = cliente;
            this.anioAnuncio = anioAnuncio;
            this.fechaHora = fechaHora != null ? fechaHora : "";
        }

        public String getProductId() { return productId; }
        public String getSku() { return sku; }
        public String getNombre() { return nombre; }
        public String getCategoria() { return categoria; }
        public String getBodega() { return bodega; }
        public String getFolioAnuncio() { return folioAnuncio != null ? folioAnuncio : "-"; }
        public String getFolioRecepcion() { return folioRecepcion != null ? folioRecepcion : "-"; }
        public String getMovimiento() { return movimiento; }
        public int getEntrada() { return entrada; }
        public int getSalida() { return salida; }
        public int getStockSaldo() { return stockSaldo; }
        public void setStockSaldo(int stockSaldo) { this.stockSaldo = stockSaldo; }
        public String getFolioNP() { return folioNP != null ? folioNP : "-"; }
        public String getCliente() { return cliente != null ? cliente : "-"; }
        public String getAnioAnuncio() { return anioAnuncio != null ? anioAnuncio : "-"; }
        public String getFechaHora() { return fechaHora; }
    }
}
