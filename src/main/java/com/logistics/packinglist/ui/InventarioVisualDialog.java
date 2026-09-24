package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.*;
import com.logistics.packinglist.service.MantenimientoService;
import com.logistics.packinglist.service.WebSocketManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
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

import java.util.*;

public class InventarioVisualDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

                private final Map<String, WarehouseModel> warehouseMap = new HashMap<>();
    private final Map<String, List<WarehouseZoneModel>> warehouseZonesMap = new HashMap<>();
    private final Map<String, ProductModel> productMap = new HashMap<>();
    private final Map<String, LocationModel> locationMap = new HashMap<>();
    private final List<StockModel> allStocksList = new ArrayList<>();
    
    // In-memory stock snapshot for color coding
    private final Map<String, List<StockModel>> stockByLocationMap = new HashMap<>();

    // UI Components
    private TreeView<Object> treeView;
    private ComboBox<String> cbPasillo;
    private ScrollPane gridScrollPane;
    private VBox detailPane;
    private Label lblSelectedLocation;
    private Label lblLocationStatus;
    private TableView<StockModel> tblProducts;
    private VBox emptyDetailPlaceholder;
    private VBox detailContentBox;
    private ProgressIndicator progressIndicator;
    private Label lblGridTitle;
    private ComboBox<String> cbRangoNiveles;
    private ComboBox<String> cbRangoPosiciones;
    private boolean isUpdatingPagination = false;
    
    // UI product details inside visual inventory
    private Button btnReubicarStock;
    private VBox productDetailBox;
    private Label lblProductTitle;
    private Label lblTotalProductStock;
    private ListView<String> lvProductLocations;

    // Active state
    private WarehouseZoneModel selectedZoneContext = null;
    private WarehouseModel selectedWarehouseContext = null;
    private LocationModel selectedLocation = null;
    private Pane selectedCellNode = null;

    public InventarioVisualDialog(Window owner) {
                        
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Mapa e Inventario Visual de Bodega (WMS)");

        setMinWidth(950);
        setMinHeight(520);

        construirUI();
        cargarDatosIniciales();

        WebSocketManager.UpdateListener stockUpdateListener = action -> {
            if (selectedZoneContext != null) {
                System.out.println("[InventarioVisualDialog] Received real-time update event. Reloading zone: " + selectedZoneContext.getId());
                cargarDatosDeZona(selectedZoneContext.getId());
            }
        };
        WebSocketManager.getInstance().subscribe("STOCK", stockUpdateListener);
        setOnCloseRequest(e -> WebSocketManager.getInstance().unsubscribe("STOCK", stockUpdateListener));
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header Box
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label("Mapa e Inventario Visual de Bodega");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Navegación gráfica por matriz de pasillos, estantes y consulta de stock en tiempo real.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // --- PANEL IZQUIERDO: Estructura de Bodega (TreeView - WHITE CARD) ---
        VBox leftPane = new VBox(10);
        leftPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        Label lblTreeTitle = new Label("Sectores y Zonas", new FontAwesomeIconView(FontAwesomeIcon.MAP_SIGNS));
        lblTreeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");

        treeView = new TreeView<>();
        treeView.setShowRoot(false);
        treeView.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        VBox.setVgrow(treeView, Priority.ALWAYS);

        leftPane.getChildren().addAll(lblTreeTitle, treeView);

        // --- PANEL CENTRAL: Rejilla / Mapa Visual (WHITE CARD) ---
        VBox centerPane = new VBox(10);
        centerPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        VBox.setVgrow(centerPane, Priority.ALWAYS);

        // Barra de control superior
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 8, 0));
        topBar.setStyle("-fx-border-color: transparent transparent #e2e8f0 transparent; -fx-border-width: 0 0 1px 0;");

        lblGridTitle = new Label("Seleccione una zona en el panel izquierdo");
        lblGridTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        HBox.setHgrow(lblGridTitle, Priority.ALWAYS);

        Label lblFilter = new Label("Pasillo:", new FontAwesomeIconView(FontAwesomeIcon.FILTER));
        lblFilter.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        cbPasillo = new ComboBox<>();
        cbPasillo.setPromptText("Pasillo");
        cbPasillo.setPrefWidth(120);
        cbPasillo.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        cbPasillo.setOnAction(e -> renderizarRejillaActiva());

        cbRangoNiveles = new ComboBox<>();
        cbRangoNiveles.setPromptText("Niveles");
        cbRangoNiveles.setPrefWidth(140);
        cbRangoNiveles.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        cbRangoNiveles.setVisible(false);
        cbRangoNiveles.setManaged(false);
        cbRangoNiveles.setOnAction(e -> renderizarRejillaActiva());

        cbRangoPosiciones = new ComboBox<>();
        cbRangoPosiciones.setPromptText("Posiciones");
        cbRangoPosiciones.setPrefWidth(140);
        cbRangoPosiciones.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        cbRangoPosiciones.setVisible(false);
        cbRangoPosiciones.setManaged(false);
        cbRangoPosiciones.setOnAction(e -> renderizarRejillaActiva());

        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(18, 18);
        progressIndicator.setVisible(false);

        topBar.getChildren().addAll(lblGridTitle, progressIndicator, lblFilter, cbPasillo, cbRangoNiveles, cbRangoPosiciones);

        // Rejilla de ubicaciones
        gridScrollPane = new ScrollPane();
        gridScrollPane.setFitToWidth(true);
        gridScrollPane.setFitToHeight(true);
        gridScrollPane.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6px; -fx-background-radius: 6px;");
        VBox.setVgrow(gridScrollPane, Priority.ALWAYS);

        centerPane.getChildren().addAll(topBar, gridScrollPane);

        // --- PANEL DERECHO: Detalle de la Ubicación (WHITE CARD EN SCROLLPANE) ---
        detailPane = new VBox(15);
        detailPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        // Placeholder para cuando no hay ubicación seleccionada
        emptyDetailPlaceholder = new VBox(20);
        emptyDetailPlaceholder.setAlignment(Pos.CENTER);
        emptyDetailPlaceholder.setPadding(new Insets(40, 10, 40, 10));
        VBox.setVgrow(emptyDetailPlaceholder, Priority.ALWAYS);

        FontAwesomeIconView placeholderIcon = new FontAwesomeIconView(FontAwesomeIcon.HAND_POINTER_ALT);
        placeholderIcon.setSize("48px");
        placeholderIcon.setFill(Color.web("#a0aec0"));

        Label lblPlaceholderText = new Label("Seleccione una casilla de la rejilla\npara ver el inventario detallado.");
        lblPlaceholderText.setStyle("-fx-text-alignment: center; -fx-text-fill: #a0aec0; -fx-font-size: 13px; -fx-line-spacing: 4px;");

        emptyDetailPlaceholder.getChildren().addAll(placeholderIcon, lblPlaceholderText);

        // Contenedor de información activa
        detailContentBox = new VBox(12);
        VBox.setVgrow(detailContentBox, Priority.ALWAYS);
        detailContentBox.setVisible(false);
        detailContentBox.setManaged(false);

        // Encabezado de la ubicación
        HBox locationHeader = new HBox(8);
        locationHeader.setAlignment(Pos.CENTER_LEFT);
        FontAwesomeIconView locIcon = new FontAwesomeIconView(FontAwesomeIcon.MAP_MARKER);
        locIcon.setSize("20px");
        locIcon.setFill(Color.web("#1e3a8a"));

        lblSelectedLocation = new Label("Ubicación");
        lblSelectedLocation.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        Region locSpacer = new Region();
        HBox.setHgrow(locSpacer, Priority.ALWAYS);

        btnReubicarStock = new Button("Reubicar Stock", new FontAwesomeIconView(FontAwesomeIcon.EXCHANGE));
        btnReubicarStock.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 4;");
        btnReubicarStock.setDisable(true);
        btnReubicarStock.setOnAction(e -> abrirReubicarStock());

        locationHeader.getChildren().addAll(locIcon, lblSelectedLocation, locSpacer, btnReubicarStock);

        // Estado/Tipo de soporte
        lblLocationStatus = new Label("Soporte: -");
        lblLocationStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569; -fx-background-color: #f1f5f9; -fx-padding: 3 8; -fx-background-radius: 12;");

        // Tabla de Productos
        tblProducts = new TableView<>();
        tblProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblProducts.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        VBox.setVgrow(tblProducts, Priority.ALWAYS);

        TableColumn<StockModel, String> colSku = new TableColumn<>("SKU");
        colSku.setCellValueFactory(c -> {
            ProductModel prod = productMap.get(c.getValue().getProductId());
            return new SimpleStringProperty(prod != null ? prod.getSku() : "-");
        });
        colSku.setPrefWidth(90);

        TableColumn<StockModel, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(c -> {
            ProductModel prod = productMap.get(c.getValue().getProductId());
            return new SimpleStringProperty(prod != null ? prod.getNombre() : "ID: " + c.getValue().getProductId());
        });
        colNombre.setPrefWidth(160);

        TableColumn<StockModel, String> colCant = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantidad())));
        colCant.setPrefWidth(60);
        colCant.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        tblProducts.getColumns().addAll(colSku, colNombre, colCant);

        // Sección inferior para detalles del producto seleccionado
        productDetailBox = new VBox(8);
        productDetailBox.setPadding(new Insets(10, 0, 0, 0));
        productDetailBox.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1px 0 0 0;");
        productDetailBox.setVisible(false);
        productDetailBox.setManaged(false);

        lblProductTitle = new Label("Detalle de Stock");
        lblProductTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 13px;");

        lblTotalProductStock = new Label("Stock Total: -");
        lblTotalProductStock.setStyle("-fx-font-weight: bold; -fx-text-fill: #2563eb;");

        lvProductLocations = new ListView<>();
        lvProductLocations.setPrefHeight(100);
        lvProductLocations.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");

        Label lblDist = new Label("Distribución física en bodega:");
        lblDist.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");

        productDetailBox.getChildren().addAll(lblProductTitle, lblTotalProductStock, lblDist, lvProductLocations);

        tblProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                productDetailBox.setVisible(true);
                productDetailBox.setManaged(true);
                
                String productId = newVal.getProductId();
                ProductModel prod = productMap.get(productId);
                if (prod != null) {
                    lblProductTitle.setText(prod.getSku() + " - " + prod.getNombre());
                } else {
                    lblProductTitle.setText("Producto ID: " + productId);
                }

                int totalQty = 0;
                lvProductLocations.getItems().clear();
                for (StockModel s : allStocksList) {
                    if (productId.equals(s.getProductId())) {
                        LocationModel loc = locationMap.get(s.getLocationId());
                        String locStr = loc != null ? loc.toString() : "Ubicación Desconocida";
                        lvProductLocations.getItems().add(locStr + ": " + s.getCantidad() + " un.");
                        totalQty += s.getCantidad() != null ? s.getCantidad() : 0;
                    }
                }
                lblTotalProductStock.setText("Stock Total en Bodega: " + totalQty + " un.");
            } else {
                productDetailBox.setVisible(false);
                productDetailBox.setManaged(false);
            }
        });

        detailContentBox.getChildren().addAll(locationHeader, lblLocationStatus, new Separator(), tblProducts, productDetailBox);
        detailPane.getChildren().addAll(emptyDetailPlaceholder, detailContentBox);

        ScrollPane rightScroll = new ScrollPane(detailPane);
        rightScroll.setFitToWidth(true);
        rightScroll.setFitToHeight(true);
        rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(leftPane, centerPane, rightScroll);
        mainSplit.setDividerPositions(0.22, 0.68);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        root.getChildren().addAll(headerBox, mainSplit);

        // Listeners del TreeView
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Object val = newVal.getValue();
                if (val instanceof WarehouseZoneModel zone) {
                    selectedZoneContext = zone;
                    selectedWarehouseContext = warehouseMap.get(zone.getWarehouseId());
                    lblGridTitle.setText(selectedWarehouseContext.getNombre() + " ❯ " + zone.getNombre());
                    cargarDatosDeZona(zone.getId());
                } else if (val instanceof WarehouseModel wh) {
                    selectedZoneContext = null;
                    selectedWarehouseContext = wh;
                    lblGridTitle.setText(wh.getNombre() + " (Seleccione un sector)");
                    limpiarRejillaYDetalles();
                }
            } else {
                limpiarRejillaYDetalles();
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

    private void cargarDatosIniciales() {
        showLoading(true);
        new Thread(() -> {
            try {
                // 1. Cargar productos para mapear nombres
                List<ProductModel> prods = service.obtenerProductos();
                productMap.clear();
                for (ProductModel p : prods) {
                    productMap.put(p.getId(), p);
                }

                // 2. Cargar bodegas y sus zonas
                List<WarehouseModel> bodegas = service.obtenerBodegas();
                warehouseMap.clear();
                warehouseZonesMap.clear();

                for (WarehouseModel wh : bodegas) {
                    warehouseMap.put(wh.getId(), wh);
                    List<WarehouseZoneModel> zones = service.obtenerZonasPorBodega(wh.getId());
                    warehouseZonesMap.put(wh.getId(), zones);
                }

                Platform.runLater(() -> {
                    construirArbol(bodegas);
                    showLoading(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showLoading(false);
                    mostrarError("Error de carga", "No se pudieron obtener los datos iniciales de la bodega: " + e.getMessage());
                });
            }
        }).start();
    }

    private void construirArbol(List<WarehouseModel> bodegas) {
        TreeItem<Object> rootItem = new TreeItem<>("Bodegas");
        treeView.setRoot(rootItem);

        for (WarehouseModel wh : bodegas) {
            TreeItem<Object> whNode = new TreeItem<>(wh, new FontAwesomeIconView(FontAwesomeIcon.HOME));
            rootItem.getChildren().add(whNode);

            List<WarehouseZoneModel> zones = warehouseZonesMap.getOrDefault(wh.getId(), Collections.emptyList());
            agregarNodosZonas(whNode, zones, null);
            whNode.setExpanded(true);
        }
    }

    private void agregarNodosZonas(TreeItem<Object> parentNode, List<WarehouseZoneModel> zones, String parentZoneId) {
        for (WarehouseZoneModel z : zones) {
            boolean matches = (parentZoneId == null && z.getParentZoneId() == null)
                    || (parentZoneId != null && parentZoneId.equals(z.getParentZoneId()));
            if (matches) {
                TreeItem<Object> zoneNode = new TreeItem<>(z, new FontAwesomeIconView(FontAwesomeIcon.FOLDER));
                parentNode.getChildren().add(zoneNode);
                agregarNodosZonas(zoneNode, zones, z.getId());
            }
        }
    }

    private Map<String, List<LocationModel>> activeLocationsByAisle = new HashMap<>();

    private void cargarDatosDeZona(String zoneId) {
        showLoading(true);
        String warehouseId = selectedWarehouseContext != null ? selectedWarehouseContext.getId() : null;
        new Thread(() -> {
            try {
                // 1. Obtener ubicaciones de la zona
                List<LocationModel> locs = service.obtenerUbicacionesPorZona(zoneId);

                // 2. Obtener todas las ubicaciones de la bodega para mapear
                List<LocationModel> allWarehouseLocations = warehouseId != null 
                        ? service.obtenerUbicacionesPorBodega(warehouseId) 
                        : Collections.emptyList();

                // 3. Obtener el stock total para saber ocupación y pre-cargar
                List<StockModel> stocks = service.obtenerStocks();
                
                Platform.runLater(() -> {
                    // Mapear existencias por ID de ubicación
                    stockByLocationMap.clear();
                    for (StockModel s : stocks) {
                        stockByLocationMap.computeIfAbsent(s.getLocationId(), k -> new ArrayList<>()).add(s);
                    }

                    allStocksList.clear();
                    allStocksList.addAll(stocks);
                    
                    locationMap.clear();
                    for (LocationModel loc : allWarehouseLocations) {
                        locationMap.put(loc.getId(), loc);
                    }

                    // Agrupar ubicaciones por Pasillo
                    activeLocationsByAisle.clear();
                    cbPasillo.getItems().clear();

                    for (LocationModel loc : locs) {
                        String aisle = loc.getPasillo() != null ? loc.getPasillo().trim() : "General";
                        activeLocationsByAisle.computeIfAbsent(aisle, k -> new ArrayList<>()).add(loc);
                    }

                    List<String> sortedAisles = new ArrayList<>(activeLocationsByAisle.keySet());
                    // Ordenar pasillos
                    sortedAisles.sort(String::compareToIgnoreCase);

                    cbPasillo.getItems().addAll(sortedAisles);
                    showLoading(false);

                    if (!cbPasillo.getItems().isEmpty()) {
                        cbPasillo.getSelectionModel().selectFirst();
                    } else {
                        // No hay ubicaciones en esta zona
                        limpiarRejillaYDetalles();
                        Label lblEmpty = new Label("Este sector no tiene ubicaciones físicas creadas.");
                        lblEmpty.setStyle("-fx-text-fill: #718096; -fx-font-size: 14px; -fx-font-weight: bold;");
                        VBox box = new VBox(lblEmpty);
                        box.setAlignment(Pos.CENTER);
                        gridScrollPane.setContent(box);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showLoading(false);
                    mostrarError("Error", "Error al cargar los datos de la zona: " + e.getMessage());
                });
            }
        }).start();
    }

    private void renderizarRejillaActiva() {
        String pasilloSeleccionado = cbPasillo.getValue();
        if (pasilloSeleccionado == null || activeLocationsByAisle.get(pasilloSeleccionado) == null) {
            return;
        }

        List<LocationModel> locs = activeLocationsByAisle.get(pasilloSeleccionado);

        // Despejar detalles activos al cambiar de pasillo
        resetLocationDetailPane();

        // 1. Extraer y ordenar niveles y columnas
        Set<String> alturas = new HashSet<>();
        Set<String> posiciones = new HashSet<>();

        for (LocationModel l : locs) {
            alturas.add(l.getAltura() != null ? l.getAltura().trim() : "Default");
            posiciones.add(l.getPosicion() != null ? l.getPosicion().trim() : "Default");
        }

        List<String> heightsSorted = sortNumericallyOrAlphabetically(alturas);
        List<String> positionsSorted = sortNumericallyOrAlphabetically(posiciones);

        // Actualizar paginación si no estamos ya en medio de una actualización
        if (!isUpdatingPagination) {
            isUpdatingPagination = true;

            // Construir rangos de niveles (máx 5 por página)
            List<String> heightRanges = new ArrayList<>();
            for (int i = 0; i < heightsSorted.size(); i += 5) {
                int end = Math.min(i + 5, heightsSorted.size());
                heightRanges.add("Niveles " + heightsSorted.get(i) + " - " + heightsSorted.get(end - 1));
            }
            String prevSelNivel = cbRangoNiveles.getValue();
            cbRangoNiveles.getItems().setAll(heightRanges);
            if (heightRanges.contains(prevSelNivel)) {
                cbRangoNiveles.setValue(prevSelNivel);
            } else if (!heightRanges.isEmpty()) {
                cbRangoNiveles.getSelectionModel().selectFirst();
            }
            cbRangoNiveles.setVisible(heightRanges.size() > 1);
            cbRangoNiveles.setManaged(heightRanges.size() > 1);

            // Construir rangos de posiciones (máx 5 por página)
            List<String> positionRanges = new ArrayList<>();
            for (int i = 0; i < positionsSorted.size(); i += 5) {
                int end = Math.min(i + 5, positionsSorted.size());
                positionRanges.add("Posiciones " + positionsSorted.get(i) + " - " + positionsSorted.get(end - 1));
            }
            String prevSelPos = cbRangoPosiciones.getValue();
            cbRangoPosiciones.getItems().setAll(positionRanges);
            if (positionRanges.contains(prevSelPos)) {
                cbRangoPosiciones.setValue(prevSelPos);
            } else if (!positionRanges.isEmpty()) {
                cbRangoPosiciones.getSelectionModel().selectFirst();
            }
            cbRangoPosiciones.setVisible(positionRanges.size() > 1);
            cbRangoPosiciones.setManaged(positionRanges.size() > 1);

            isUpdatingPagination = false;
        }

        // Obtener índices de página seleccionados
        int heightPageIndex = Math.max(0, cbRangoNiveles.getSelectionModel().getSelectedIndex());
        int posPageIndex = Math.max(0, cbRangoPosiciones.getSelectionModel().getSelectedIndex());

        // Obtener sublistas (páginas 5x5)
        int fromHeight = heightPageIndex * 5;
        int toHeight = Math.min(fromHeight + 5, heightsSorted.size());
        List<String> pageHeights = (fromHeight < heightsSorted.size()) 
                ? heightsSorted.subList(fromHeight, toHeight) 
                : heightsSorted;

        int fromPos = posPageIndex * 5;
        int toPos = Math.min(fromPos + 5, positionsSorted.size());
        List<String> pagePositions = (fromPos < positionsSorted.size()) 
                ? positionsSorted.subList(fromPos, toPos) 
                : positionsSorted;

        // Crear una copia invertida para renderizado de arriba a abajo
        List<String> pageHeightsReversed = new ArrayList<>(pageHeights);
        Collections.reverse(pageHeightsReversed);

        // 2. Construir la cuadrícula
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #f7fafc;");

        // Crear mapa para búsquedas rápidas
        Map<String, LocationModel> coordsToLoc = new HashMap<>();
        for (LocationModel l : locs) {
            String alt = l.getAltura() != null ? l.getAltura().trim() : "Default";
            String pos = l.getPosicion() != null ? l.getPosicion().trim() : "Default";
            coordsToLoc.put(alt + "_" + pos, l);
        }

        // Agregar etiquetas de Altura en la columna 0
        for (int r = 0; r < pageHeightsReversed.size(); r++) {
            String alt = pageHeightsReversed.get(r);
            Label lblHeight = new Label("Nivel " + alt);
            lblHeight.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568; -fx-padding: 0 10 0 0; -fx-font-size: 11px;");
            grid.add(lblHeight, 0, r);
        }

        // Rellenar las casillas del Rack
        for (int r = 0; r < pageHeightsReversed.size(); r++) {
            String alt = pageHeightsReversed.get(r);
            for (int c = 0; c < pagePositions.size(); c++) {
                String pos = pagePositions.get(c);
                LocationModel loc = coordsToLoc.get(alt + "_" + pos);

                Pane cellNode;
                if (loc != null) {
                    cellNode = crearCeldaUbicacion(loc);
                } else {
                    // Celda vacía en la estantería (hueco vacío sin ubicación física)
                    cellNode = new Pane();
                    cellNode.setPrefSize(90, 80);
                    cellNode.setStyle("-fx-background-color: #edf2f7; -fx-border-color: #cbd5e0; -fx-border-radius: 4; -fx-border-style: dashed;");
                }
                // Las ubicaciones van corridas en 1 columna por las etiquetas de fila
                grid.add(cellNode, c + 1, r);
            }
        }

        // Agregar etiquetas de Posición al final de cada columna
        int bottomRowIndex = pageHeightsReversed.size();
        for (int c = 0; c < pagePositions.size(); c++) {
            String pos = pagePositions.get(c);
            Label lblPos = new Label("Pos " + pos);
            lblPos.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568; -fx-alignment: center; -fx-font-size: 10px;");
            
            HBox posContainer = new HBox(lblPos);
            posContainer.setAlignment(Pos.CENTER);
            posContainer.setPrefWidth(90);
            
            grid.add(posContainer, c + 1, bottomRowIndex);
        }

        // Contenedor centrado
        VBox layoutBox = new VBox(grid);
        layoutBox.setAlignment(Pos.CENTER);
        layoutBox.setPadding(new Insets(10));
        layoutBox.setStyle("-fx-background-color: #f7fafc;");

        gridScrollPane.setContent(layoutBox);
    }

    private Pane crearCeldaUbicacion(LocationModel loc) {
        VBox cell = new VBox(4);
        cell.setPrefSize(90, 80);
        cell.setAlignment(Pos.CENTER);
        cell.setPadding(new Insets(6));
        cell.setCursor(javafx.scene.Cursor.HAND);

        Label lblCode = new Label(loc.getCodigoUbicacion());
        lblCode.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        FontAwesomeIconView cellIcon = new FontAwesomeIconView(FontAwesomeIcon.CUBE);
        cellIcon.setSize("14px");

        Label lblQty = new Label();
        lblQty.setStyle("-fx-font-size: 9px;");

        // Determinar ocupación
        List<StockModel> stocks = stockByLocationMap.get(loc.getId());
        boolean hasStock = stocks != null && !stocks.isEmpty();

        int totalQty = 0;
        if (hasStock) {
            for (StockModel s : stocks) {
                totalQty += s.getCantidad() != null ? s.getCantidad() : 0;
            }
        }

        if (hasStock) {
            // Celda ocupada (azul)
            cell.setStyle("-fx-background-color: #EBF8FF; -fx-border-color: #3182CE; -fx-border-width: 2px; -fx-border-radius: 6px; -fx-background-radius: 6px;");
            lblCode.setStyle("-fx-text-fill: #2B6CB0; -fx-font-weight: bold; -fx-font-size: 11px;");
            cellIcon.setFill(Color.web("#3182CE"));
            lblQty.setText(totalQty + " un.");
            lblQty.setStyle("-fx-text-fill: #2B6CB0; -fx-font-weight: bold; -fx-font-size: 10px;");
        } else {
            // Celda vacía (gris claro)
            cell.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px;");
            lblCode.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 11px;");
            cellIcon.setFill(Color.web("#cbd5e0"));
            lblQty.setText("Vacío");
            lblQty.setStyle("-fx-text-fill: #718096; -fx-font-size: 9px;");
        }

        cell.getChildren().addAll(lblCode, cellIcon, lblQty);

        // Tooltip descriptivo
        Tooltip tooltip = new Tooltip(String.format(
                "Ubicación: %s\nSoporte: %s\nPasillo: %s\nPosición: %s\nNivel: %s\nArtículos: %s",
                loc.getCodigoUbicacion(),
                loc.getTipoSoporte(),
                loc.getPasillo(),
                loc.getPosicion() != null ? loc.getPosicion() : "-",
                loc.getAltura() != null ? loc.getAltura() : "-",
                hasStock ? totalQty + " unidades" : "Vacío"
        ));
        Tooltip.install(cell, tooltip);

        // Micro-animación de hover
        cell.setOnMouseEntered(e -> {
            cell.setScaleX(1.05);
            cell.setScaleY(1.05);
            if (loc != selectedLocation) {
                cell.setStyle(hasStock 
                    ? "-fx-background-color: #EBF8FF; -fx-border-color: #2b6cb0; -fx-border-width: 2px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);"
                    : "-fx-background-color: #edf2f7; -fx-border-color: #4a5568; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);"
                );
            }
        });

        cell.setOnMouseExited(e -> {
            cell.setScaleX(1.0);
            cell.setScaleY(1.0);
            if (loc != selectedLocation) {
                if (hasStock) {
                    cell.setStyle("-fx-background-color: #EBF8FF; -fx-border-color: #3182CE; -fx-border-width: 2px; -fx-border-radius: 6px; -fx-background-radius: 6px;");
                } else {
                    cell.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px;");
                }
            }
        });

        cell.setOnMouseClicked(e -> {
            // Desmarcar celda previa
            if (selectedCellNode != null && selectedLocation != null) {
                boolean prevHasStock = stockByLocationMap.containsKey(selectedLocation.getId());
                if (prevHasStock) {
                    selectedCellNode.setStyle("-fx-background-color: #EBF8FF; -fx-border-color: #3182CE; -fx-border-width: 2px; -fx-border-radius: 6px; -fx-background-radius: 6px;");
                } else {
                    selectedCellNode.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px;");
                }
            }

            // Marcar nueva celda activa con borde destacado (Dorado)
            selectedLocation = loc;
            selectedCellNode = cell;
            cell.setStyle(hasStock 
                ? "-fx-background-color: #EBF8FF; -fx-border-color: #d69e2e; -fx-border-width: 3px; -fx-border-radius: 6px; -fx-background-radius: 6px;"
                : "-fx-background-color: #ffffff; -fx-border-color: #d69e2e; -fx-border-width: 3px; -fx-border-radius: 6px; -fx-background-radius: 6px;"
            );

            mostrarDetallesUbicacion(loc);
        });

        return cell;
    }

    private void mostrarDetallesUbicacion(LocationModel loc) {
        lblSelectedLocation.setText("Ubicación " + loc.getCodigoUbicacion());
        lblLocationStatus.setText("SOPORTE: " + loc.getTipoSoporte() + "  |  PASILLO: " + loc.getPasillo() 
                + "  |  POS: " + (loc.getPosicion() != null ? loc.getPosicion() : "-")
                + "  |  NIVEL: " + (loc.getAltura() != null ? loc.getAltura() : "-"));

        emptyDetailPlaceholder.setVisible(false);
        emptyDetailPlaceholder.setManaged(false);
        detailContentBox.setVisible(true);
        detailContentBox.setManaged(true);

        tblProducts.getItems().clear();

        // Consulta en tiempo real al backend para obtener stock fresco
        showLoading(true);
        new Thread(() -> {
            try {
                List<StockModel> details = service.obtenerStocksPorUbicacion(loc.getId());
                Platform.runLater(() -> {
                    tblProducts.getItems().addAll(details);
                    btnReubicarStock.setDisable(details.isEmpty());
                    showLoading(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showLoading(false);
                    // Fallback a los datos precargados en memoria en caso de error
                    List<StockModel> cached = stockByLocationMap.getOrDefault(loc.getId(), Collections.emptyList());
                    tblProducts.getItems().addAll(cached);
                    btnReubicarStock.setDisable(cached.isEmpty());
                });
            }
        }).start();
    }

    private void resetLocationDetailPane() {
        selectedLocation = null;
        selectedCellNode = null;
        if (btnReubicarStock != null) {
            btnReubicarStock.setDisable(true);
        }
        emptyDetailPlaceholder.setVisible(true);
        emptyDetailPlaceholder.setManaged(true);
        detailContentBox.setVisible(false);
        detailContentBox.setManaged(false);
        tblProducts.getItems().clear();
        if (productDetailBox != null) {
            productDetailBox.setVisible(false);
            productDetailBox.setManaged(false);
        }
    }

    public void refrescarDatosDespuesDeReubicacion() {
        if (selectedZoneContext != null) {
            cargarDatosDeZona(selectedZoneContext.getId());
        }
        if (selectedLocation != null) {
            mostrarDetallesUbicacion(selectedLocation);
        }
    }

    private void abrirReubicarStock() {
        if (selectedLocation == null) {
            mostrarWarning("Selección requerida", "Debe seleccionar una ubicación en la rejilla visual.");
            return;
        }

        StockModel selectedStock = tblProducts.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            if (tblProducts.getItems().size() == 1) {
                selectedStock = tblProducts.getItems().get(0);
            } else if (tblProducts.getItems().isEmpty()) {
                mostrarWarning("Sin Stock", "Esta ubicación no tiene stock disponible para reubicar.");
                return;
            } else {
                mostrarWarning("Selección de Producto", "Por favor seleccione el producto que desea reubicar de la lista.");
                return;
            }
        }

        ProductModel product = productMap.get(selectedStock.getProductId());
        if (product == null) {
            mostrarError("Error", "No se encontró la información del producto seleccionado.");
            return;
        }

        String tipoStock = determinarAmbitoZona(selectedLocation.getZoneId());
        String currentWarehouseId = selectedLocation.getWarehouseId();

        List<LocationModel> destLocations = new ArrayList<>();
        for (LocationModel loc : locationMap.values()) {
            if (currentWarehouseId != null && currentWarehouseId.equals(loc.getWarehouseId()) && !loc.getId().equals(selectedLocation.getId())) {
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
                selectedLocation,
                selectedStock.getCantidad(),
                tipoStock,
                destLocations
        );
        dialog.showAndWait();

        refrescarDatosDespuesDeReubicacion();
    }

    private String determinarAmbitoZona(String zoneId) {
        if (zoneId == null) return "COMERCIAL";
        String currentId = zoneId;
        while (currentId != null) {
            WarehouseZoneModel zone = findZoneById(currentId);
            if (zone == null) break;
            String ambito = zone.getAmbitoStock();
            if (ambito != null && !ambito.trim().isEmpty()) {
                return ambito.toUpperCase();
            }
            currentId = zone.getParentZoneId();
        }
        return "COMERCIAL";
    }

    private WarehouseZoneModel findZoneById(String zoneId) {
        for (List<WarehouseZoneModel> zones : warehouseZonesMap.values()) {
            for (WarehouseZoneModel z : zones) {
                if (z.getId().equals(zoneId)) return z;
            }
        }
        return null;
    }

    private void mostrarWarning(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void limpiarRejillaYDetalles() {
        cbPasillo.getItems().clear();
        if (cbRangoNiveles != null) {
            cbRangoNiveles.getItems().clear();
            cbRangoNiveles.setVisible(false);
            cbRangoNiveles.setManaged(false);
        }
        if (cbRangoPosiciones != null) {
            cbRangoPosiciones.getItems().clear();
            cbRangoPosiciones.setVisible(false);
            cbRangoPosiciones.setManaged(false);
        }
        gridScrollPane.setContent(null);
        resetLocationDetailPane();
    }

    private void showLoading(boolean show) {
        progressIndicator.setVisible(show);
    }

    private List<String> sortNumericallyOrAlphabetically(Collection<String> items) {
        List<String> list = new ArrayList<>(items);
        list.sort((a, b) -> {
            try {
                int ia = Integer.parseInt(a);
                int ib = Integer.parseInt(b);
                return Integer.compare(ia, ib);
            } catch (NumberFormatException e) {
                // Si contiene números mezclados con letras, intentar extraer el número
                Integer na = extractNumber(a);
                Integer nb = extractNumber(b);
                if (na != null && nb != null) {
                    return na.compareTo(nb);
                }
                return a.compareToIgnoreCase(b);
            }
        });
        return list;
    }

    private Integer extractNumber(String s) {
        try {
            String digits = s.replaceAll("\\D+", "");
            if (!digits.isEmpty()) {
                return Integer.parseInt(digits);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void mostrarError(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
