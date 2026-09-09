package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.LocationModel;
import com.logistics.packinglist.model.StockModel;
import com.logistics.packinglist.service.PdfAnuncioService;
import javafx.stage.FileChooser;
import java.io.File;

import com.logistics.packinglist.model.AnnouncementDetailModel;
import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.ProductModel;
import com.logistics.packinglist.model.ProductFieldDefinitionModel;
import com.logistics.packinglist.model.ReceptionAnnouncementModel;
import com.logistics.packinglist.model.SupplierModel;
import com.logistics.packinglist.model.WarehouseModel;
import com.logistics.packinglist.service.MantenimientoService;
import com.logistics.packinglist.service.WebSocketManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
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
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnunciosDialog extends Stage {
    private final List<ProductFieldDefinitionModel> activeFieldDefinitions = new ArrayList<>();
    private final Map<String, Control> customFieldInputMap = new HashMap<>();
    private final List<ProductFieldDefinitionModel> activeDocFieldDefinitions = new ArrayList<>();
    private final Map<String, Control> docCustomFieldInputMap = new HashMap<>();
    private HBox docCustomFieldsPane;
    private FlowPane customFieldsPane;
    private DetailRow editingDetailRow = null;
    private FontAwesomeIconView iconAddProduct;
    private final MantenimientoService service = MantenimientoService.getInstance();

    

        private final ObservableList<ReceptionAnnouncementModel> announcementsList = FXCollections.observableArrayList();
    private final FilteredList<ReceptionAnnouncementModel> filteredAnnouncements;
    private final ObservableList<DetailRow> detailRows = FXCollections.observableArrayList();

    private TableView<ReceptionAnnouncementModel> tblComercial;
    private ComboBox<WarehouseModel> cbBodega;
    private AutocompleteComboBox<SupplierModel> cbProveedor;
    private TextArea txtObservaciones;
    private TextField txtNumeroBl;
    private TextField txtOrdenCompra;
    private ComboBox<String> cbFiltroEstado;

    // Formulario de items
    private AutocompleteComboBox<ProductModel> cbProducto;
    private TextField txtCantidad;
    private TableView<DetailRow> tblDetails;

    private CheckBox chkEsContenedor;
    private TextField txtNumeroContenedor;
    private TextField txtDigitoContenedor;
    private Button btnGuardarAnuncio;
    private Button btnEliminarAnuncio;
    private Button btnLimpiarAnuncio;
    private boolean isPopulatingForm = false;

    private final Map<String, WarehouseModel> warehouseMap = new HashMap<>();
    private final Map<String, SupplierModel> supplierMap = new HashMap<>();
    private final Map<String, ProductModel> productMap = new HashMap<>();

    public AnunciosDialog(Window owner) {
        this.filteredAnnouncements = new FilteredList<>(announcementsList, a -> true);
                initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Anuncios de Carga");
        setMinWidth(950);
        setMinHeight(650);

        construirUI();
        cargarCamposPersonalizadosDocumento();
        cargarCamposPersonalizadosProducto();
        cargarDatos();

        WebSocketManager.UpdateListener announcementListener = action -> {
            System.out.println("[AnunciosDialog] Received real-time update event: " + action);
            cargarCamposPersonalizadosDocumento();
        cargarCamposPersonalizadosProducto();
        cargarDatos();
        };
        WebSocketManager.getInstance().subscribe("ANNOUNCEMENT", announcementListener);
        WebSocketManager.getInstance().subscribe("SUPPLIER", announcementListener);
        WebSocketManager.getInstance().subscribe("PRODUCT", announcementListener);
        WebSocketManager.getInstance().subscribe("WAREHOUSE", announcementListener);

        setOnHiding(e -> {
            WebSocketManager.getInstance().unsubscribe("ANNOUNCEMENT", announcementListener);
            WebSocketManager.getInstance().unsubscribe("SUPPLIER", announcementListener);
            WebSocketManager.getInstance().unsubscribe("PRODUCT", announcementListener);
            WebSocketManager.getInstance().unsubscribe("WAREHOUSE", announcementListener);
        });
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header Box
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label("Gestión de Anuncios de Carga");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Programación de recepción de mercadería y control de entregas por proveedor.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // --- LADO IZQUIERDO: LISTADO (WHITE CARD) ---
        VBox leftPane = new VBox(10);
        leftPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        Label lblListado = new Label("Anuncios Registrados");
        lblListado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        cbFiltroEstado = new ComboBox<>(FXCollections.observableArrayList("Todos", "Pendientes", "Parciales", "Completados", "Anulados"));
        cbFiltroEstado.setValue("Todos");
        cbFiltroEstado.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbFiltroEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredAnnouncements.setPredicate(a -> {
                if (newVal == null || "Todos".equalsIgnoreCase(newVal)) return true;
                String est = a.getEstado();
                if ("Pendientes".equalsIgnoreCase(newVal)) {
                    return "PENDIENTE".equalsIgnoreCase(est);
                } else if ("Parciales".equalsIgnoreCase(newVal)) {
                    return "PARCIAL".equalsIgnoreCase(est);
                } else if ("Completados".equalsIgnoreCase(newVal)) {
                    return "COMPLETADO".equalsIgnoreCase(est);
                } else if ("Anulados".equalsIgnoreCase(newVal)) {
                    return "ANULADO".equalsIgnoreCase(est);
                }
                return true;
            });
        });

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        Label lblFiltro = new Label("Filtrar por Estado:");
        lblFiltro.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
        filterBox.getChildren().addAll(lblFiltro, cbFiltroEstado);

        tblComercial = crearTablaAnuncios(filteredAnnouncements);
        VBox.setVgrow(tblComercial, Priority.ALWAYS);

        leftPane.getChildren().addAll(lblListado, filterBox, tblComercial);

        // --- LADO DERECHO: FORMULARIO (WHITE CARD EN SCROLLPANE) ---
        VBox rightPane = new VBox(12);
        rightPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        Label lblNuevo = new Label("Detalle del Anuncio");
        lblNuevo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Region spacerTop = new Region();
        HBox.setHgrow(spacerTop, Priority.ALWAYS);

        FontAwesomeIconView iconCogDoc = new FontAwesomeIconView(FontAwesomeIcon.COG);
        iconCogDoc.setFill(Color.web("#0F3E6E"));
        Button btnConfigAtributosDoc = new Button("Configurar Documento", iconCogDoc);
        btnConfigAtributosDoc.setStyle("-fx-background-color: transparent; -fx-text-fill: #0F3E6E; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        btnConfigAtributosDoc.setOnAction(e -> abrirConfiguracionAtributosDoc());

        HBox topHeaderBox = new HBox(8, lblNuevo, spacerTop, btnConfigAtributosDoc);
        topHeaderBox.setAlignment(Pos.CENTER_LEFT);

        docCustomFieldsPane = new HBox(8);
        docCustomFieldsPane.setAlignment(Pos.CENTER_LEFT);
        docCustomFieldsPane.setPadding(new Insets(4, 0, 4, 0));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(8);
        formGrid.setVgap(8);

        cbBodega = new ComboBox<>();
        cbBodega.setPromptText("Seleccione Bodega");
        cbBodega.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");
        cbBodega.setMaxWidth(Double.MAX_VALUE);

        cbProveedor = new AutocompleteComboBox<>();
        cbProveedor.setPromptText("Seleccione Proveedor");
        cbProveedor.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");
        cbProveedor.setMaxWidth(Double.MAX_VALUE);
        cbProveedor.setFilterPredicate((s, text) -> {
            return (s.getRazonSocial() != null && s.getRazonSocial().toLowerCase().contains(text))
                || (s.getRut() != null && s.getRut().toLowerCase().contains(text))
                || (s.getId() != null && s.getId().toLowerCase().contains(text));
        });

        txtNumeroBl = new TextField();
        txtNumeroBl.setPromptText("N° BL (Opcional)");
        txtNumeroBl.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");

        txtOrdenCompra = new TextField();
        txtOrdenCompra.setPromptText("Orden de Compra (Opcional)");
        txtOrdenCompra.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");

        chkEsContenedor = new CheckBox("Contenedor");
        chkEsContenedor.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        txtNumeroContenedor = new TextField();
        txtNumeroContenedor.setPromptText("N° Contenedor");
        txtNumeroContenedor.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");
        txtNumeroContenedor.setDisable(true);

        txtDigitoContenedor = new TextField();
        txtDigitoContenedor.setPromptText("DV");
        txtDigitoContenedor.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");
        txtDigitoContenedor.setPrefWidth(45);
        txtDigitoContenedor.setDisable(true);

        chkEsContenedor.selectedProperty().addListener((obs, oldV, newV) -> {
            txtNumeroContenedor.setDisable(!newV);
            txtDigitoContenedor.setDisable(!newV);
            if (!newV) {
                txtNumeroContenedor.clear();
                txtDigitoContenedor.clear();
            }
        });

        HBox containerBox = new HBox(6, txtNumeroContenedor, txtDigitoContenedor);
        HBox.setHgrow(txtNumeroContenedor, Priority.ALWAYS);

        txtObservaciones = new TextArea();
        txtObservaciones.setPromptText("Observaciones...");
        txtObservaciones.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtObservaciones.setPrefHeight(72);

        Label lblBod = new Label("Bodega:");
        lblBod.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblProv = new Label("Proveedor:");
        lblProv.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblBl = new Label("N° BL:");
        lblBl.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblOc = new Label("Orden Compra:");
        lblOc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblObs = new Label("Observaciones:");
        lblObs.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");

        // Fila 0: Bodega (col 0,1) | Proveedor (col 2,3)
        formGrid.add(lblBod, 0, 0);
        formGrid.add(cbBodega, 1, 0);
        formGrid.add(lblProv, 2, 0);
        formGrid.add(cbProveedor, 3, 0);

        // Fila 1: N° BL (col 0,1) | Orden Compra (col 2,3)
        formGrid.add(lblBl, 0, 1);
        formGrid.add(txtNumeroBl, 1, 1);
        formGrid.add(lblOc, 2, 1);
        formGrid.add(txtOrdenCompra, 3, 1);

        // Fila 2: Check Contenedor (col 0,1) | N° Contenedor + DV (col 2,3)
        formGrid.add(chkEsContenedor, 0, 2);
        GridPane.setColumnSpan(chkEsContenedor, 2);
        formGrid.add(containerBox, 2, 2);
        GridPane.setColumnSpan(containerBox, 2);

        // Fila 3: Observaciones
        formGrid.add(lblObs, 0, 3);
        formGrid.add(txtObservaciones, 1, 3);
        GridPane.setColumnSpan(txtObservaciones, 3);

        ColumnConstraints gc1 = new ColumnConstraints(85);
        ColumnConstraints gc2 = new ColumnConstraints(155);
        ColumnConstraints gc3 = new ColumnConstraints(90);
        ColumnConstraints gc4 = new ColumnConstraints(155);
        formGrid.getColumnConstraints().addAll(gc1, gc2, gc3, gc4);

        // Sub-formulario para agregar productos
        VBox itemsBox = new VBox(8);
        itemsBox.setPadding(new Insets(8));
        itemsBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e0; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label lblItems = new Label("Agregar Productos");
        lblItems.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        Region spacerItems = new Region();
        HBox.setHgrow(spacerItems, Priority.ALWAYS);

        FontAwesomeIconView iconCog = new FontAwesomeIconView(FontAwesomeIcon.COG);
        iconCog.setFill(Color.web("#0F3E6E"));
        Button btnConfigAtributos = new Button("Configurar Atributos", iconCog);
        btnConfigAtributos.setStyle("-fx-background-color: transparent; -fx-text-fill: #0F3E6E; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        btnConfigAtributos.setOnAction(e -> abrirConfiguracionAtributosProd());

        HBox itemsHeaderBox = new HBox(8, lblItems, spacerItems, btnConfigAtributos);
        itemsHeaderBox.setAlignment(Pos.CENTER_LEFT);

        VBox inputsWrapper = new VBox(6);
        HBox itemInputs = new HBox(8);
        itemInputs.setAlignment(Pos.CENTER_LEFT);
        
        customFieldsPane = new FlowPane(8, 8);
        customFieldsPane.setAlignment(Pos.CENTER_LEFT);

        cbProducto = new AutocompleteComboBox<>();
        cbProducto.setPromptText("Seleccione Producto");
        cbProducto.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbProducto.setPrefWidth(180);
        cbProducto.setDisable(true);
        cbProducto.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (editingDetailRow == null && newVal != null) {
                Map<String, String> defaultAttrs = newVal.getAtributosPersonalizados();
                for (ProductFieldDefinitionModel f : activeFieldDefinitions) {
                    Control ctrl = customFieldInputMap.get(f.getFieldKey());
                    if (ctrl == null) continue;
                    String val = defaultAttrs != null ? defaultAttrs.get(f.getFieldKey()) : null;

                    if (ctrl instanceof TextField) {
                        ((TextField) ctrl).setText(val != null ? val : "");
                    } else if (ctrl instanceof ComboBox) {
                        ((ComboBox<String>) ctrl).setValue(val != null ? val : null);
                    } else if (ctrl instanceof DatePicker) {
                        if (val != null && !val.trim().isEmpty()) {
                            try {
                                java.time.LocalDate ld;
                                if (val.contains("/")) {
                                    ld = java.time.LocalDate.parse(val, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                } else {
                                    ld = java.time.LocalDate.parse(val);
                                }
                                ((DatePicker) ctrl).setValue(ld);
                            } catch (Exception e) {
                                ((DatePicker) ctrl).setValue(null);
                            }
                        } else {
                            ((DatePicker) ctrl).setValue(null);
                        }
                    }
                }
            }
        });

        cbProducto.setFilterPredicate((p, text) -> {
            String codeCliente = p.getAtributosPersonalizados() != null ? p.getAtributosPersonalizados().get("codigo_cliente") : null;
            return (p.getSku() != null && p.getSku().toLowerCase().contains(text))
                || (p.getNombre() != null && p.getNombre().toLowerCase().contains(text))
                || (p.getId() != null && p.getId().toLowerCase().contains(text))
                || (p.getBarcode() != null && p.getBarcode().toLowerCase().contains(text))
                || (codeCliente != null && codeCliente.toLowerCase().contains(text));
        });

        cbProveedor.valueProperty().addListener((obs, oldVal, newVal) -> {
            cargarCamposPersonalizadosDocumento();
            cargarCamposPersonalizadosProducto();
            cbProducto.selectItem(null);
            cbProducto.getItems().clear();
            if (!isPopulatingForm) {
                detailRows.clear();
            }
            if (newVal != null) {
                cbProducto.setDisable(false);
                List<ProductModel> filtered = new java.util.ArrayList<>();
                for (ProductModel p : productMap.values()) {
                    if (newVal.getId().equals(p.getSupplierId())) {
                        filtered.add(p);
                    }
                }
                cbProducto.setAllItems(filtered);
            } else {
                cbProducto.setDisable(true);
            }
        });

        txtCantidad = new TextField();
        txtCantidad.setPromptText("Cant.");
        txtCantidad.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        txtCantidad.setPrefWidth(50);

        iconAddProduct = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        iconAddProduct.setFill(Color.WHITE);
        Button btnAddProduct = new Button(null, iconAddProduct);
        btnAddProduct.setStyle("-fx-background-color: #0d6efd; -fx-padding: 6px 12px; -fx-background-radius: 4px; -fx-cursor: hand;");
        btnAddProduct.setOnAction(e -> agregarFilaDetalle());
        btnAddProduct.disableProperty().bind(cbProducto.disabledProperty());

        FontAwesomeIconView iconExcel = new FontAwesomeIconView(FontAwesomeIcon.FILE_EXCEL_ALT);
        iconExcel.setFill(Color.WHITE);
        Button btnExcel = new Button(null, iconExcel);
        btnExcel.setStyle("-fx-background-color: #28a745; -fx-padding: 6px 12px; -fx-background-radius: 4px; -fx-cursor: hand;");
        btnExcel.setOnAction(e -> cargarExcelDeSKUs());
        btnExcel.disableProperty().bind(cbProveedor.valueProperty().isNull());

        itemInputs.getChildren().addAll(cbProducto, txtCantidad, btnAddProduct, btnExcel);
        inputsWrapper.getChildren().addAll(itemInputs, customFieldsPane);

        tblDetails = new TableView<>();
        tblDetails.setItems(detailRows);
        tblDetails.setMinHeight(240);
        tblDetails.setPrefHeight(280);
        tblDetails.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblDetails.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        reconstruirColumnasTabla();

        tblDetails.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && !isPopulatingForm) {
                cargarFilaParaEdicion(newSel);
            }
        });

        itemsBox.getChildren().addAll(itemsHeaderBox, inputsWrapper, tblDetails);

        // Botones de acción inferior
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER);

        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSave.setFill(Color.WHITE);
        iconSave.setSize("16");
        btnGuardarAnuncio = new Button(null, iconSave);
        btnGuardarAnuncio.setStyle("-fx-background-color: #0F3E6E; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnGuardarAnuncio.setOnAction(e -> guardarAnuncio());
        Tooltip.install(btnGuardarAnuncio, new Tooltip("Registrar Anuncio de Carga"));

        FontAwesomeIconView iconClose = new FontAwesomeIconView(FontAwesomeIcon.CLOSE);
        iconClose.setFill(Color.WHITE);
        iconClose.setSize("16");
        btnEliminarAnuncio = new Button(null, iconClose);
        btnEliminarAnuncio.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnEliminarAnuncio.setVisible(false);
        btnEliminarAnuncio.setManaged(false);
        btnEliminarAnuncio.setOnAction(e -> eliminarAnuncioSeleccionado());
        Tooltip.install(btnEliminarAnuncio, new Tooltip("Anular Anuncio de Carga"));

        FontAwesomeIconView iconRefresh = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        iconRefresh.setFill(Color.WHITE);
        iconRefresh.setSize("16");
        btnLimpiarAnuncio = new Button(null, iconRefresh);
        btnLimpiarAnuncio.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnLimpiarAnuncio.setOnAction(e -> {
            clearAnnouncementsSelection();
            limpiarFormulario();
        });
        Tooltip.install(btnLimpiarAnuncio, new Tooltip("Limpiar Formulario"));

        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setPadding(new Insets(15, 0, 0, 0));
        Button btnPdf = UiComponentFactory.createPdfButton(this::descargarPDF);
        actionButtons.getChildren().addAll(btnPdf, btnGuardarAnuncio, btnEliminarAnuncio, btnLimpiarAnuncio);

        rightPane.getChildren().addAll(topHeaderBox, formGrid, docCustomFieldsPane, itemsBox);

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

        root.getChildren().addAll(headerBox, mainSplit, actionButtons);

        // Listener de selección
        javafx.beans.value.ChangeListener<ReceptionAnnouncementModel> selectionListener = (obs, oldSel, newSel) -> {
            if (newSel != null) {
                isPopulatingForm = true;
                cbBodega.setValue(warehouseMap.get(newSel.getWarehouseId()));
                cbProveedor.setValue(supplierMap.get(newSel.getSupplierId()));
                txtNumeroBl.setText(newSel.getNumeroBl() != null ? newSel.getNumeroBl() : "");
                txtOrdenCompra.setText(newSel.getOrdenCompra() != null ? newSel.getOrdenCompra() : "");
                boolean esCont = newSel.getEsContenedor() != null && newSel.getEsContenedor();
                chkEsContenedor.setSelected(esCont);
                txtNumeroContenedor.setText(esCont && newSel.getNumeroContenedor() != null ? newSel.getNumeroContenedor() : "");
                txtDigitoContenedor.setText(esCont && newSel.getDigitoContenedor() != null ? newSel.getDigitoContenedor() : "");
                txtObservaciones.setText(newSel.getObservaciones() != null ? newSel.getObservaciones() : "");

                Map<String, String> docAttrs = newSel.getAtributosPersonalizados() != null ? newSel.getAtributosPersonalizados() : new HashMap<>();
                for (ProductFieldDefinitionModel f : activeDocFieldDefinitions) {
                    Control ctrl = docCustomFieldInputMap.get(f.getFieldKey());
                    String val = docAttrs.getOrDefault(f.getFieldKey(), "");
                    if (ctrl instanceof TextField) {
                        ((TextField) ctrl).setText(val);
                    } else if (ctrl instanceof ComboBox) {
                        ((ComboBox<String>) ctrl).setValue(val.isEmpty() ? null : val);
                    } else if (ctrl instanceof DatePicker) {
                        if (!val.isEmpty()) {
                            try {
                                ((DatePicker) ctrl).setValue(java.time.LocalDate.parse(val, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            } catch (Exception ignored) {}
                        } else {
                            ((DatePicker) ctrl).setValue(null);
                        }
                    }
                }

                detailRows.clear();
                if (newSel.getDetails() != null) {
                    for (AnnouncementDetailModel d : newSel.getDetails()) {
                        ProductModel prod = productMap.get(d.getProductId());
                        String desc = prod != null ? prod.toString() : "Producto Desconocido";
                        detailRows.add(new DetailRow(d.getProductId(), desc, d.getCantidad(), d.getAtributosPersonalizados()));
                    }
                }
                isPopulatingForm = false;

                boolean isPending = "PENDIENTE".equalsIgnoreCase(newSel.getEstado());
                cbBodega.setDisable(!isPending);
                cbProveedor.setDisable(!isPending);
                txtNumeroBl.setDisable(!isPending);
                txtOrdenCompra.setDisable(!isPending);
                chkEsContenedor.setDisable(!isPending);
                txtNumeroContenedor.setDisable(!isPending || !chkEsContenedor.isSelected());
                txtDigitoContenedor.setDisable(!isPending || !chkEsContenedor.isSelected());
                txtObservaciones.setDisable(!isPending);
                txtObservaciones.setDisable(!isPending);
                cbProducto.setDisable(!isPending || cbProveedor.getValue() == null);
                txtCantidad.setDisable(!isPending);

                if (isPending) {
                    FontAwesomeIconView saveIcon = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
                    saveIcon.setFill(Color.WHITE);
                    saveIcon.setSize("16");
                    btnGuardarAnuncio.setGraphic(saveIcon);
                    btnGuardarAnuncio.setDisable(false);
                    Tooltip.install(btnGuardarAnuncio, new Tooltip("Actualizar Anuncio"));
                    btnEliminarAnuncio.setVisible(true);
                    btnEliminarAnuncio.setManaged(true);
                } else {
                    btnGuardarAnuncio.setText(null);
                    FontAwesomeIconView iconLock = new FontAwesomeIconView(FontAwesomeIcon.LOCK);
                    iconLock.setFill(Color.WHITE);
                    iconLock.setSize("16");
                    btnGuardarAnuncio.setGraphic(iconLock);
                    btnGuardarAnuncio.setDisable(true);
                    Tooltip.install(btnGuardarAnuncio, new Tooltip("Anuncio Procesado (Lectura)"));
                    btnEliminarAnuncio.setVisible(false);
                    btnEliminarAnuncio.setManaged(false);
                }
            }
        };

        tblComercial.getSelectionModel().selectedItemProperty().addListener(selectionListener);

        Scene scene = new Scene(root, 950, 650);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setScene(scene);
    }

    private TableView<ReceptionAnnouncementModel> crearTablaAnuncios(FilteredList<ReceptionAnnouncementModel> filteredList) {
        TableView<ReceptionAnnouncementModel> table = new TableView<>(filteredList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ReceptionAnnouncementModel, String> colFolio = new TableColumn<>("Folio Anuncio");
        colFolio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolio()));
        colFolio.setPrefWidth(100);

        TableColumn<ReceptionAnnouncementModel, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> {
            String f = c.getValue().getFecha();
            if (f != null && f.contains("T")) {
                f = f.replace("T", " ");
                if (f.contains(".")) {
                    f = f.substring(0, f.indexOf("."));
                }
            }
            return new SimpleStringProperty(f != null ? f : "-");
        });
        colFecha.setPrefWidth(120);

        TableColumn<ReceptionAnnouncementModel, String> colBod = new TableColumn<>("Bodega");
        colBod.setCellValueFactory(c -> {
            WarehouseModel w = warehouseMap.get(c.getValue().getWarehouseId());
            return new SimpleStringProperty(w != null ? w.getNombre() : "-");
        });
        colBod.setPrefWidth(120);

        TableColumn<ReceptionAnnouncementModel, String> colProv = new TableColumn<>("Proveedor");
        colProv.setCellValueFactory(c -> {
            SupplierModel s = supplierMap.get(c.getValue().getSupplierId());
            return new SimpleStringProperty(s != null ? s.getRazonSocial() : "-");
        });
        colProv.setPrefWidth(120);

        TableColumn<ReceptionAnnouncementModel, String> colEst = new TableColumn<>("Estado");
        colEst.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
        colEst.setPrefWidth(80);

        TableColumn<ReceptionAnnouncementModel, String> colRecFolio = new TableColumn<>("Folio Rec.");
        colRecFolio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReceptionFolio() != null ? c.getValue().getReceptionFolio() : "-"));
        colRecFolio.setPrefWidth(100);

        // Apply visual styling policy
        StatusColumnHelper.applyStatusStyling(colFolio, true);
                colEst.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("RECEPCIONADO".equalsIgnoreCase(item)) {
                        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);
                        icon.setFill(Color.web("#16a34a"));
                        icon.setSize("14");
                        setGraphic(icon);
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else if ("ANULADO".equalsIgnoreCase(item)) {
                        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.TIMES_CIRCLE);
                        icon.setFill(Color.web("#dc2626"));
                        icon.setSize("14");
                        setGraphic(icon);
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    } else {
                        setGraphic(null);
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    }
                }
            }
        });
        StatusColumnHelper.applyRowFactory(table);

        TableColumn<ReceptionAnnouncementModel, String> colBl = new TableColumn<>("N° BL");
        colBl.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroBl() != null ? c.getValue().getNumeroBl() : "-"));
        colBl.setPrefWidth(90);

        TableColumn<ReceptionAnnouncementModel, String> colOc = new TableColumn<>("Orden Compra");
        colOc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOrdenCompra() != null ? c.getValue().getOrdenCompra() : "-"));
        colOc.setPrefWidth(90);

        table.getColumns().addAll(colFolio, colFecha, colBod, colProv, colBl, colOc, colEst, colRecFolio);
        return table;
    }

    private ReceptionAnnouncementModel getSelectedAnnouncement() {
        return tblComercial.getSelectionModel().getSelectedItem();
    }

    private void clearAnnouncementsSelection() {
        tblComercial.getSelectionModel().clearSelection();
    }

    private ProductModel encontrarProductoPorSKU(String sku) {
        if (cbProveedor.getValue() == null) return null;
        String provId = cbProveedor.getValue().getId();
        for (ProductModel p : productMap.values()) {
            if (provId.equals(p.getSupplierId())) {
                if (p.getSku() != null && p.getSku().equalsIgnoreCase(sku.trim())) {
                    return p;
                }
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
        if (cbProveedor.getValue() == null) {
            mostrarWarning("Validación", "Debe seleccionar primero el proveedor del anuncio.");
            return;
        }
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Seleccionar Excel de SKUs");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        java.io.File file = fc.showOpenDialog(this);
        if (file == null) return;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            int importados = 0;
            int noEncontrados = 0;
            
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                // Ignorar primera fila si parece encabezado
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
                    for (DetailRow detailRow : detailRows) {
                        if (detailRow.getProductId().equals(product.getId())) {
                            detailRow.setCantidad(detailRow.getCantidad() + cantidad);
                            existe = true;
                            break;
                        }
                    }
                    if (!existe) {
                        detailRows.add(new DetailRow(product.getId(), product.toString(), cantidad));
                    }
                    importados++;
                } else {
                    noEncontrados++;
                }
            }
            
            tblDetails.refresh();
            mostrarInformacion("Importación Completa", 
                String.format("Se importaron %d productos correctamente. %d SKUs no fueron encontrados en el sistema para este proveedor.", 
                    importados, noEncontrados));
                    
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError("Error al importar Excel", ex.getMessage());
        }
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {
                String myCompanyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
                List<WarehouseModel> warehouses = service.obtenerBodegas();
                List<SupplierModel> suppliers = service.obtenerProveedores();
                List<ProductModel> products = service.obtenerProductos();
                List<ReceptionAnnouncementModel> announcements = service.obtenerAnuncios();

                javafx.application.Platform.runLater(() -> {
                    warehouseMap.clear();
                    cbBodega.getItems().clear();
                    WarehouseModel defaultWarehouse = null;
                    String myWarehouseId = com.logistics.packinglist.service.AuthService.getInstance().getWarehouseId();
                    for (WarehouseModel w : warehouses) {
                        if (myCompanyId == null || myCompanyId.equals(w.getCompanyId())) {
                            warehouseMap.put(w.getId(), w);
                            cbBodega.getItems().add(w);
                            if (myWarehouseId != null && myWarehouseId.equals(w.getId())) {
                                defaultWarehouse = w;
                            }
                        }
                    }
                    if (defaultWarehouse != null) {
                        cbBodega.setValue(defaultWarehouse);
                    }

                    supplierMap.clear();
                    List<SupplierModel> supplierList = new java.util.ArrayList<>();
                    for (SupplierModel s : suppliers) {
                        if (myCompanyId == null || myCompanyId.equals(s.getCompanyId())) {
                            supplierMap.put(s.getId(), s);
                            supplierList.add(s);
                        }
                    }
                    cbProveedor.setAllItems(supplierList);

                    productMap.clear();
                    cbProducto.selectItem(null);
                    cbProducto.getItems().clear();
                    cbProducto.setDisable(true);
                    for (ProductModel p : products) {
                        if (myCompanyId == null || myCompanyId.equals(p.getCompanyId())) {
                            productMap.put(p.getId(), p);
                        }
                    }

                    announcementsList.clear();
                    announcementsList.addAll(announcements);
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error al cargar datos", e.getMessage());
                });
            }
        }).start();
    }

    
        private void abrirConfiguracionAtributosDoc() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        String supplierId = cbProveedor.getValue() != null ? cbProveedor.getValue().getId() : null;

        ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, "ANUNCIO_DOC", warehouseId, supplierId, this::cargarCamposPersonalizadosDocumento);
        diag.showAndWait();
    }

    private void abrirConfiguracionAtributosProd() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        String supplierId = cbProveedor.getValue() != null ? cbProveedor.getValue().getId() : null;

        ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, "ANUNCIO_PROD", warehouseId, supplierId, this::cargarCamposPersonalizadosProducto);
        diag.showAndWait();
    }

    private void cargarCamposPersonalizadosDocumento() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        String supplierId = cbProveedor.getValue() != null ? cbProveedor.getValue().getId() : null;

        new Thread(() -> {
            try {
                List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_DOC", warehouseId, null);
                List<ProductFieldDefinitionModel> valid = new ArrayList<>();
                if (fields != null) {
                    for (ProductFieldDefinitionModel f : fields) {
                        String t = f.getFieldType() != null ? f.getFieldType().toUpperCase() : "TEXT";
                        if (f.isActive() && ("TEXT".equals(t) || "NUMBER".equals(t) || "DATE".equals(t) || "BOOLEAN".equals(t))) {
                            valid.add(f);
                        }
                    }
                }
                javafx.application.Platform.runLater(() -> {
                    activeDocFieldDefinitions.clear();
                    activeDocFieldDefinitions.addAll(valid);
                    renderDocCustomFieldInputs();
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void cargarCamposPersonalizadosProducto() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        String supplierId = cbProveedor.getValue() != null ? cbProveedor.getValue().getId() : null;

        new Thread(() -> {
            try {
                List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_PROD", warehouseId, null);
                List<ProductFieldDefinitionModel> valid = new ArrayList<>();
                if (fields != null) {
                    for (ProductFieldDefinitionModel f : fields) {
                        String t = f.getFieldType() != null ? f.getFieldType().toUpperCase() : "TEXT";
                        if (f.isActive() && ("TEXT".equals(t) || "NUMBER".equals(t) || "DATE".equals(t) || "BOOLEAN".equals(t))) {
                            valid.add(f);
                        }
                    }
                }
                javafx.application.Platform.runLater(() -> {
                    activeFieldDefinitions.clear();
                    activeFieldDefinitions.addAll(valid);
                    renderCustomFieldInputs();
                    reconstruirColumnasTabla();
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void renderDocCustomFieldInputs() {
        if (docCustomFieldsPane == null) return;
        docCustomFieldsPane.getChildren().clear();
        docCustomFieldInputMap.clear();

        for (ProductFieldDefinitionModel f : activeDocFieldDefinitions) {
            VBox box = new VBox(2);
            String labelText = (f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey());
            Label lbl = new Label(labelText + (f.isRequired() ? " *" : ""));
            lbl.setStyle(f.isRequired() ? "-fx-font-size: 11px; -fx-text-fill: #dc2626; -fx-font-weight: bold;" : "-fx-font-size: 11px; -fx-text-fill: #475569;");

            String t = f.getFieldType() != null ? f.getFieldType().toUpperCase() : "TEXT";
            Control ctrl;
            if ("BOOLEAN".equals(t)) {
                ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList("Sí", "No"));
                cb.setPromptText("Seleccione");
                cb.setStyle("-fx-background-radius: 4px;");
                ctrl = cb;
            } else if ("DATE".equals(t)) {
                DatePicker dp = new DatePicker();
                dp.setPromptText("dd/MM/yyyy");
                dp.setStyle("-fx-background-radius: 4px;");
                dp.setPrefWidth(125);
                ctrl = dp;
            } else {
                TextField txt = new TextField();
                txt.setPromptText(labelText);
                txt.setStyle("-fx-background-radius: 4px; -fx-border-color: #cbd5e1;");
                txt.setPrefWidth(120);
                ctrl = txt;
            }
            docCustomFieldInputMap.put(f.getFieldKey(), ctrl);
            box.getChildren().addAll(lbl, ctrl);
            docCustomFieldsPane.getChildren().add(box);
        }
    }

    private void renderCustomFieldInputs() {
        if (customFieldsPane == null) return;
        customFieldsPane.getChildren().clear();
        customFieldInputMap.clear();

        for (ProductFieldDefinitionModel f : activeFieldDefinitions) {
            VBox box = new VBox(2);
            String labelText = (f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey());
            Label lbl = new Label(labelText + (f.isRequired() ? " *" : ""));
            lbl.setStyle(f.isRequired() ? "-fx-font-size: 11px; -fx-text-fill: #dc2626; -fx-font-weight: bold;" : "-fx-font-size: 11px; -fx-text-fill: #475569;");

            String t = f.getFieldType() != null ? f.getFieldType().toUpperCase() : "TEXT";
            Control ctrl;
            if ("BOOLEAN".equals(t)) {
                ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList("Sí", "No"));
                cb.setPromptText("Seleccione");
                cb.setStyle("-fx-background-radius: 4px;");
                ctrl = cb;
            } else if ("DATE".equals(t)) {
                DatePicker dp = new DatePicker();
                dp.setPromptText("dd/MM/yyyy");
                dp.setStyle("-fx-background-radius: 4px;");
                dp.setPrefWidth(125);
                ctrl = dp;
            } else {
                TextField txt = new TextField();
                txt.setPromptText(labelText);
                txt.setStyle("-fx-background-radius: 4px; -fx-border-color: #cbd5e1;");
                txt.setPrefWidth(110);
                ctrl = txt;
            }
            customFieldInputMap.put(f.getFieldKey(), ctrl);
            box.getChildren().addAll(lbl, ctrl);
            customFieldsPane.getChildren().add(box);
        }
    }

    private void reconstruirColumnasTabla() {
        if (tblDetails == null) return;
        tblDetails.getColumns().clear();

        TableColumn<DetailRow, String> colDProd = new TableColumn<>("Producto");
        colDProd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductoDesc()));
        colDProd.setPrefWidth(180);

        TableColumn<DetailRow, String> colDCant = new TableColumn<>("Cantidad");
        colDCant.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantidad())));
        colDCant.setPrefWidth(70);

        tblDetails.getColumns().addAll(colDProd, colDCant);

        for (ProductFieldDefinitionModel f : activeFieldDefinitions) {
            TableColumn<DetailRow, String> colAttr = new TableColumn<>(f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey());
            colAttr.setCellValueFactory(c -> {
                Map<String, String> m = c.getValue().getAtributosPersonalizados();
                return new SimpleStringProperty(m != null ? m.getOrDefault(f.getFieldKey(), "-") : "-");
            });
            colAttr.setPrefWidth(110);
            tblDetails.getColumns().add(colAttr);
        }

        TableColumn<DetailRow, Void> colDActions = new TableColumn<>("Acciones");
        colDActions.setPrefWidth(70);
        colDActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnRemove;
            {
                FontAwesomeIconView iconTrash = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                iconTrash.setFill(Color.WHITE);
                btnRemove = new Button(null, iconTrash);
                btnRemove.setStyle("-fx-background-color: #dc3545; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-cursor: hand;");
                btnRemove.setOnAction(e -> {
                    DetailRow row = getTableView().getItems().get(getIndex());
                    if (editingDetailRow == row) {
                        limpiarInputsDetalle();
                    }
                    detailRows.remove(row);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ReceptionAnnouncementModel sel = getSelectedAnnouncement();
                    boolean isPending = sel == null || "PENDIENTE".equalsIgnoreCase(sel.getEstado());
                    btnRemove.setDisable(!isPending);
                    setGraphic(btnRemove);
                }
            }
        });
        tblDetails.getColumns().add(colDActions);
    }

    
    private void cargarFilaParaEdicion(DetailRow row) {
        editingDetailRow = row;
        ProductModel prod = productMap.get(row.getProductId());
        if (prod != null) {
            cbProducto.selectItem(prod);
        }
        txtCantidad.setText(String.valueOf(row.getCantidad()));

        Map<String, String> attrs = row.getAtributosPersonalizados();
        for (ProductFieldDefinitionModel f : activeFieldDefinitions) {
            Control ctrl = customFieldInputMap.get(f.getFieldKey());
            if (ctrl == null) continue;
            String val = attrs != null ? attrs.get(f.getFieldKey()) : null;

            if (ctrl instanceof TextField) {
                ((TextField) ctrl).setText(val != null ? val : "");
            } else if (ctrl instanceof ComboBox) {
                ((ComboBox<String>) ctrl).setValue(val != null ? val : null);
            } else if (ctrl instanceof DatePicker) {
                if (val != null && !val.trim().isEmpty()) {
                    try {
                        java.time.LocalDate ld;
                        if (val.contains("/")) {
                            ld = java.time.LocalDate.parse(val, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        } else {
                            ld = java.time.LocalDate.parse(val);
                        }
                        ((DatePicker) ctrl).setValue(ld);
                    } catch (Exception e) {
                        ((DatePicker) ctrl).setValue(null);
                    }
                } else {
                    ((DatePicker) ctrl).setValue(null);
                }
            }
        }
        if (iconAddProduct != null) {
            iconAddProduct.setIcon(FontAwesomeIcon.SAVE);
        }
    }

    private void limpiarInputsDetalle() {
        editingDetailRow = null;
        if (tblDetails != null) {
            tblDetails.getSelectionModel().clearSelection();
        }
        cbProducto.selectItem(null);
        txtCantidad.clear();
        limpiarCamposPersonalizados();
        if (iconAddProduct != null) {
            iconAddProduct.setIcon(FontAwesomeIcon.PLUS);
        }
    }

    private void limpiarCamposPersonalizados() {
        for (Control ctrl : customFieldInputMap.values()) {
            if (ctrl instanceof TextField) {
                ((TextField) ctrl).clear();
            } else if (ctrl instanceof ComboBox) {
                ((ComboBox<?>) ctrl).getSelectionModel().clearSelection();
            } else if (ctrl instanceof DatePicker) {
                ((DatePicker) ctrl).setValue(null);
            }
        }
    }

    private void agregarFilaDetalle() {
        ProductModel prod = cbProducto.getValue();
        String cantStr = txtCantidad.getText().trim();

        if (prod == null || cantStr.isEmpty()) {
            mostrarWarning("Validación", "Debe seleccionar un producto y especificar una cantidad.");
            return;
        }

        int cant;
        try {
            cant = Integer.parseInt(cantStr);
            if (cant <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarWarning("Validación", "La cantidad debe ser un número entero mayor a 0.");
            return;
        }

        Map<String, String> newAttrs = new HashMap<>();
        for (ProductFieldDefinitionModel f : activeFieldDefinitions) {
            Control ctrl = customFieldInputMap.get(f.getFieldKey());
            String val = "";
            if (ctrl instanceof TextField) {
                val = ((TextField) ctrl).getText().trim();
            } else if (ctrl instanceof ComboBox) {
                Object obj = ((ComboBox<?>) ctrl).getValue();
                val = obj != null ? obj.toString() : "";
            } else if (ctrl instanceof DatePicker) {
                java.time.LocalDate ld = ((DatePicker) ctrl).getValue();
                val = ld != null ? ld.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            }
            if (f.isRequired() && val.isEmpty()) {
                mostrarWarning("Validación", "El campo '" + (f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey()) + "' es requerido.");
                return;
            }
            if (!val.isEmpty()) {
                newAttrs.put(f.getFieldKey(), val);
            }
        }

        if (editingDetailRow != null) {
            editingDetailRow.setCantidad(cant);
            editingDetailRow.setAtributosPersonalizados(newAttrs);
            tblDetails.refresh();
            limpiarInputsDetalle();
            return;
        }

        // Verificar si existe en la lista con el MISMO SKU y MISMOS ATRIBUTOS para consolidar
        for (DetailRow row : detailRows) {
            if (row.getProductId().equals(prod.getId()) && row.getAtributosPersonalizados().equals(newAttrs)) {
                row.setCantidad(row.getCantidad() + cant);
                tblDetails.refresh();
                limpiarInputsDetalle();
                return;
            }
        }

        detailRows.add(new DetailRow(prod.getId(), prod.toString(), cant, newAttrs));
        limpiarInputsDetalle();
    }

    private void guardarAnuncio() {
        WarehouseModel bod = cbBodega.getValue();
        SupplierModel prov = cbProveedor.getValue();
        String obs = txtObservaciones.getText().trim();

        if (bod == null || prov == null) {
            mostrarWarning("Validación", "Debe seleccionar bodega y proveedor.");
            return;
        }

        if (detailRows.isEmpty()) {
            mostrarWarning("Validación", "Debe ingresar al menos un producto al anuncio.");
            return;
        }

        ReceptionAnnouncementModel selected = getSelectedAnnouncement();
        ReceptionAnnouncementModel model = selected != null ? selected : new ReceptionAnnouncementModel();
        model.setCompanyId(bod.getCompanyId());
        model.setWarehouseId(bod.getId());
        model.setSupplierId(prov.getId());
        String blVal = txtNumeroBl.getText().trim();
        model.setNumeroBl(blVal.isEmpty() ? null : blVal);
        String ocVal = txtOrdenCompra.getText().trim();
        model.setOrdenCompra(ocVal.isEmpty() ? null : ocVal);
        boolean isCont = chkEsContenedor.isSelected();
        model.setEsContenedor(isCont);
        String numCont = txtNumeroContenedor.getText().trim();
        model.setNumeroContenedor(isCont && !numCont.isEmpty() ? numCont : null);
        String digCont = txtDigitoContenedor.getText().trim();
        model.setDigitoContenedor(isCont && !digCont.isEmpty() ? digCont : null);
        model.setObservaciones(obs.isEmpty() ? null : obs);

        Map<String, String> docAttrs = new HashMap<>();
        for (ProductFieldDefinitionModel f : activeDocFieldDefinitions) {
            Control ctrl = docCustomFieldInputMap.get(f.getFieldKey());
            String val = "";
            if (ctrl instanceof TextField) {
                val = ((TextField) ctrl).getText().trim();
            } else if (ctrl instanceof ComboBox) {
                Object obj = ((ComboBox<?>) ctrl).getValue();
                val = obj != null ? obj.toString() : "";
            } else if (ctrl instanceof DatePicker) {
                java.time.LocalDate ld = ((DatePicker) ctrl).getValue();
                val = ld != null ? ld.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            }
            if (f.isRequired() && val.isEmpty()) {
                mostrarWarning("Validación", "El campo de documento '" + (f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey()) + "' es requerido.");
                return;
            }
            if (!val.isEmpty()) {
                docAttrs.put(f.getFieldKey(), val);
            }
        }
        model.setAtributosPersonalizados(docAttrs);
        
        List<AnnouncementDetailModel> details = new ArrayList<>();
        for (DetailRow row : detailRows) {
            AnnouncementDetailModel d = new AnnouncementDetailModel();
            d.setProductId(row.getProductId());
            d.setCantidad(row.getCantidad());
            d.setAtributosPersonalizados(new HashMap<>(row.getAtributosPersonalizados()));
            details.add(d);
        }
        model.setDetails(details);

        new Thread(() -> {
            try {
                if (selected == null) {
                    ReceptionAnnouncementModel created = service.crearAnuncio(model);
                    javafx.application.Platform.runLater(() -> {
                        mostrarInformacion("Éxito", "Anuncio registrado correctamente con Folio: " + created.getFolio());
                        clearAnnouncementsSelection();
                        limpiarFormulario();
                        cargarCamposPersonalizadosDocumento();
        cargarCamposPersonalizadosProducto();
        cargarDatos();
                    });
                } else {
                    ReceptionAnnouncementModel updated = service.actualizarAnuncio(model.getId(), model);
                    javafx.application.Platform.runLater(() -> {
                        mostrarInformacion("Éxito", "Anuncio actualizado correctamente.");
                        clearAnnouncementsSelection();
                        limpiarFormulario();
                        cargarCamposPersonalizadosDocumento();
        cargarCamposPersonalizadosProducto();
        cargarDatos();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error al guardar", e.getMessage());
                });
            }
        }).start();
    }

    private void eliminarAnuncioSeleccionado() {
        ReceptionAnnouncementModel selected = getSelectedAnnouncement();
        if (selected == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText(null);
        alert.setContentText("¿Está seguro que desea eliminar el anuncio con Folio: " + selected.getFolio() + "?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    service.eliminarAnuncio(selected.getId());
                    javafx.application.Platform.runLater(() -> {
                        mostrarInformacion("Éxito", "Anuncio eliminado correctamente.");
                        clearAnnouncementsSelection();
                        limpiarFormulario();
                        cargarCamposPersonalizadosDocumento();
        cargarCamposPersonalizadosProducto();
        cargarDatos();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        mostrarError("Error al eliminar", e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void limpiarFormulario() {
        isPopulatingForm = true;
        cbBodega.getSelectionModel().clearSelection();
        cbProveedor.selectItem(null);
        chkEsContenedor.setSelected(false);
        txtNumeroContenedor.clear();
        txtDigitoContenedor.clear();
        chkEsContenedor.setDisable(false);
        txtNumeroContenedor.setDisable(true);
        txtDigitoContenedor.setDisable(true);
        txtObservaciones.clear();
        for (Control ctrl : docCustomFieldInputMap.values()) {
            if (ctrl instanceof TextField) ((TextField) ctrl).clear();
            else if (ctrl instanceof ComboBox) ((ComboBox<?>) ctrl).setValue(null);
            else if (ctrl instanceof DatePicker) ((DatePicker) ctrl).setValue(null);
        }
        detailRows.clear();
        cbProducto.selectItem(null);
        cbProducto.getItems().clear();
        txtCantidad.clear();

        cbBodega.setDisable(false);
        cbProveedor.setDisable(false);
        txtObservaciones.setDisable(false);
        cbProducto.setDisable(true);
        txtCantidad.setDisable(false);

        if (btnGuardarAnuncio != null) {
            btnGuardarAnuncio.setText(null);
            FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
            iconSave.setFill(Color.WHITE);
            iconSave.setSize("16");
            btnGuardarAnuncio.setGraphic(iconSave);
            btnGuardarAnuncio.setDisable(false);
            Tooltip.install(btnGuardarAnuncio, new Tooltip("Registrar Anuncio de Carga"));
        }
        if (btnEliminarAnuncio != null) {
            btnEliminarAnuncio.setVisible(false);
            btnEliminarAnuncio.setManaged(false);
        }
        isPopulatingForm = false;
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

    // Helper row class
    public static class DetailRow {
        private final String productId;
        private final String productoDesc;
        private int cantidad;
        private java.util.Map<String, String> atributosPersonalizados;

        public DetailRow(String productId, String productoDesc, int cantidad) {
            this(productId, productoDesc, cantidad, new java.util.HashMap<>());
        }

        public DetailRow(String productId, String productoDesc, int cantidad, java.util.Map<String, String> atributosPersonalizados) {
            this.productId = productId;
            this.productoDesc = productoDesc;
            this.cantidad = cantidad;
            this.atributosPersonalizados = atributosPersonalizados != null ? atributosPersonalizados : new java.util.HashMap<>();
        }

        public String getProductId() { return productId; }
        public String getProductoDesc() { return productoDesc; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public java.util.Map<String, String> getAtributosPersonalizados() { return atributosPersonalizados; }
        public void setAtributosPersonalizados(java.util.Map<String, String> atributosPersonalizados) { this.atributosPersonalizados = atributosPersonalizados; }
    }

    private void descargarPDF() {
        ReceptionAnnouncementModel selected = getSelectedAnnouncement();
        
        if (selected == null) {
            WarehouseModel bod = cbBodega.getValue();
            SupplierModel prov = cbProveedor.getValue();
            if (bod == null || prov == null || detailRows.isEmpty()) {
                mostrarWarning("Exportar PDF", "Debe seleccionar un Anuncio de Carga de la lista o ingresar datos en el formulario.");
                return;
            }
            selected = new ReceptionAnnouncementModel();
            selected.setFolio("AN-NUEVO");
            selected.setWarehouseId(bod.getId());
            selected.setSupplierId(prov.getId());
            selected.setObservaciones(txtObservaciones.getText().trim());
            selected.setEstado("PENDIENTE");
            selected.setFecha(java.time.LocalDateTime.now().toString());

            List<AnnouncementDetailModel> details = new ArrayList<>();
            for (DetailRow row : detailRows) {
                AnnouncementDetailModel d = new AnnouncementDetailModel();
                d.setProductId(row.getProductId());
                d.setCantidad(row.getCantidad());
                details.add(d);
            }
            selected.setDetails(details);
        }

        final ReceptionAnnouncementModel targetAnnouncement = selected;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar PDF de Anuncio de Carga");
        String folioName = targetAnnouncement.getFolio() != null ? targetAnnouncement.getFolio() : "ANUNCIO";
        fileChooser.setInitialFileName(folioName + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));

        File file = fileChooser.showSaveDialog(this);
        if (file != null) {
            new Thread(() -> {
                try {
                    PdfAnuncioService pdfService = new PdfAnuncioService();
                    
                    SupplierModel supplier = supplierMap.get(targetAnnouncement.getSupplierId());
                    WarehouseModel warehouse = warehouseMap.get(targetAnnouncement.getWarehouseId());
                    
                    CompanyModel compObj = null;
                    String companyId = targetAnnouncement.getCompanyId() != null ? targetAnnouncement.getCompanyId() : (warehouse != null ? warehouse.getCompanyId() : null);
                    if (companyId != null) {
                        try {
                            compObj = service.obtenerEmpresaPorId(companyId);
                        } catch (Exception ignored) {}
                    }

                    List<StockModel> stocks = new ArrayList<>();
                    List<LocationModel> locs = new ArrayList<>();
                    try {
                        stocks = service.obtenerStocks();
                        locs = service.obtenerUbicaciones();
                    } catch (Exception ignored) {}

                    Map<String, LocationModel> locationMap = new java.util.HashMap<>();
                    for (LocationModel l : locs) {
                        locationMap.put(l.getId(), l);
                    }

                    pdfService.exportar(targetAnnouncement, compObj, supplier, warehouse, productMap, stocks, locationMap, activeFieldDefinitions, file);
                    
                    javafx.application.Platform.runLater(() -> {
                        mostrarInformacion("PDF Creado", "El PDF del Anuncio de Carga se ha exportado con éxito.");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        mostrarError("Error", "No se pudo generar el PDF del Anuncio: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

}
