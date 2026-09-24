package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.AnnouncementDetailModel;
import com.logistics.packinglist.model.LocationModel;
import com.logistics.packinglist.model.ProductModel;
import com.logistics.packinglist.model.ReceptionAnnouncementModel;
import com.logistics.packinglist.model.ReceptionDetailModel;
import com.logistics.packinglist.model.ReceptionModel;
import com.logistics.packinglist.model.StockModel;
import com.logistics.packinglist.model.WarehouseModel;
import com.logistics.packinglist.model.WarehouseZoneModel;
import com.logistics.packinglist.service.MantenimientoService;
import com.logistics.packinglist.service.ExcelDiferenciasService;
import com.logistics.packinglist.service.PdfAnuncioService;
import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.ProductFieldDefinitionModel;
import java.io.File;
import com.logistics.packinglist.model.SupplierModel;
import com.logistics.packinglist.service.WebSocketManager;
import com.logistics.packinglist.utils.ScreenUtil;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecepcionesDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

            private final ObservableList<ReceptionModel> receptionsList = FXCollections.observableArrayList();
    private final ObservableList<ReceptionRow> receptionRows = FXCollections.observableArrayList();

    private TabPane tabPaneRecepciones;
    private TableView<ReceptionModel> tblComercial;
    private TableView<ReceptionModel> tblPrimaria;
    private ComboBox<WarehouseModel> cbBodega;
    private ComboBox<ReceptionAnnouncementModel> cbAnuncio;
    private ComboBox<String> cbZonaDestino;
    private TextArea txtObservaciones;
    private TextField txtNumeroBl;
    private TextField txtOrdenCompra;
    private ComboBox<String> cbTipoDocumento;
    private TextField txtNumeroDocumento;

    // Items table and inputs
    private TableView<ReceptionRow> tblItems;
    private TextField txtCantRecibida;
    private CheckBox chkUbicacionExistente;
    private ComboBox<String> cbUbicacionExistente;
    private ComboBox<WarehouseZoneModel> cbZonaFisica;
    private ComboBox<LocationModel> cbUbicacion;
    private Button btnApplyItem;
    private CheckBox chkEsContenedor;
    private TextField txtNumeroContenedor;
    private TextField txtDigitoContenedor;
    private HBox docCustomFieldsPane;
    private HBox customFieldsPane;
    private final List<ProductFieldDefinitionModel> activeDocFieldDefinitions = new ArrayList<>();
    private final Map<String, Control> docCustomFieldInputMap = new HashMap<>();
    private final List<com.logistics.packinglist.model.ProductFieldDefinitionModel> activeFieldDefinitions = new ArrayList<>();
    private final Map<String, Control> customFieldInputMap = new HashMap<>();
    private Button btnGuardarRecepcion;
    private Button btnLimpiarRecepcion;
    private ComboBox<String> cbFiltroEstado;

    private final Map<String, SupplierModel> supplierMap = new HashMap<>();
    private final Map<String, WarehouseModel> warehouseMap = new HashMap<>();
    private final Map<String, ProductModel> productMap = new HashMap<>();
    private final Map<String, LocationModel> locationMap = new HashMap<>();
    private final Map<String, WarehouseZoneModel> zoneMap = new HashMap<>();
    private final List<LocationModel> allLocationsOfWarehouse = new ArrayList<>();
    private final ObservableList<WarehouseZoneModel> filteredZones = FXCollections.observableArrayList();
    private final ObservableList<LocationModel> filteredLocationItems = FXCollections.observableArrayList();
    private final Map<String, ReceptionAnnouncementModel> announcementMap = new HashMap<>();
    private final List<ReceptionAnnouncementModel> pendingAnnouncements = new ArrayList<>();

    public RecepcionesDialog(Window owner) {
                        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Recepciones de Carga");
        setMinWidth(950);
        setMinHeight(520);

        construirUI();
        cargarDatos();

        WebSocketManager.UpdateListener receptionListener = action -> {
            System.out.println("[RecepcionesDialog] Received real-time update event: " + action);
            cargarDatos();
        };
        WebSocketManager.getInstance().subscribe("ANNOUNCEMENT", receptionListener);
        WebSocketManager.getInstance().subscribe("RECEPTION", receptionListener);
        WebSocketManager.getInstance().subscribe("WAREHOUSE", receptionListener);
        WebSocketManager.getInstance().subscribe("ZONE", receptionListener);
        WebSocketManager.getInstance().subscribe("LOCATION", receptionListener);
        WebSocketManager.getInstance().subscribe("PRODUCT", receptionListener);
        WebSocketManager.getInstance().subscribe("SUPPLIER", receptionListener);

        setOnHiding(e -> {
            WebSocketManager.getInstance().unsubscribe("ANNOUNCEMENT", receptionListener);
            WebSocketManager.getInstance().unsubscribe("RECEPTION", receptionListener);
            WebSocketManager.getInstance().unsubscribe("WAREHOUSE", receptionListener);
            WebSocketManager.getInstance().unsubscribe("ZONE", receptionListener);
            WebSocketManager.getInstance().unsubscribe("LOCATION", receptionListener);
            WebSocketManager.getInstance().unsubscribe("PRODUCT", receptionListener);
            WebSocketManager.getInstance().unsubscribe("SUPPLIER", receptionListener);
        });
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header Box
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label("Gestión de Recepciones de Carga");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Recepción física de mercancía, conteo de stock y asignación de ubicaciones.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // --- LADO IZQUIERDO: LISTADO CON TABS (WHITE CARD) ---
        VBox leftPane = new VBox(10);
        leftPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        Label lblListado = new Label("Recepciones Procesadas");
        lblListado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        cbFiltroEstado = new ComboBox<>(FXCollections.observableArrayList("Todos", "Pendientes", "Completadas"));
        cbFiltroEstado.setValue("Todos");
        cbFiltroEstado.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        FilteredList<ReceptionModel> filteredComercial = new FilteredList<>(receptionsList, 
            r -> r.getZonaDestino() == null || "COMERCIAL".equalsIgnoreCase(r.getZonaDestino()));
        FilteredList<ReceptionModel> filteredPrimaria = new FilteredList<>(receptionsList, 
            r -> "PRIMARIA".equalsIgnoreCase(r.getZonaDestino()));

        Runnable updateFiltros = () -> {
            String estadoFiltro = cbFiltroEstado.getValue();
            filteredComercial.setPredicate(r -> {
                boolean matchesZone = (r.getZonaDestino() == null || "COMERCIAL".equalsIgnoreCase(r.getZonaDestino()));
                if (!matchesZone) return false;
                if (estadoFiltro != null && !"Todos".equalsIgnoreCase(estadoFiltro)) {
                    String est = r.getEstado();
                    if ("Pendientes".equalsIgnoreCase(estadoFiltro)) {
                        return "PENDIENTE".equalsIgnoreCase(est);
                    } else if ("Completadas".equalsIgnoreCase(estadoFiltro)) {
                        return "COMPLETADA".equalsIgnoreCase(est);
                    }
                }
                return true;
            });
            filteredPrimaria.setPredicate(r -> {
                boolean matchesZone = "PRIMARIA".equalsIgnoreCase(r.getZonaDestino());
                if (!matchesZone) return false;
                if (estadoFiltro != null && !"Todos".equalsIgnoreCase(estadoFiltro)) {
                    String est = r.getEstado();
                    if ("Pendientes".equalsIgnoreCase(estadoFiltro)) {
                        return "PENDIENTE".equalsIgnoreCase(est);
                    } else if ("Completadas".equalsIgnoreCase(estadoFiltro)) {
                        return "COMPLETADA".equalsIgnoreCase(est);
                    }
                }
                return true;
            });
        };

        cbFiltroEstado.valueProperty().addListener((obs, oldVal, newVal) -> updateFiltros.run());

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        Label lblFiltro = new Label("Filtrar por Estado:");
        lblFiltro.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
        filterBox.getChildren().addAll(lblFiltro, cbFiltroEstado);

        tblComercial = crearTablaRecepciones(filteredComercial);
        tblPrimaria = crearTablaRecepciones(filteredPrimaria);

        tabPaneRecepciones = new TabPane();
        Tab tabComercial = new Tab("Comercial", tblComercial);
        tabComercial.setClosable(false);
        Tab tabPrimaria = new Tab("Primaria", tblPrimaria);
        tabPrimaria.setClosable(false);
        tabPaneRecepciones.getTabs().addAll(tabComercial, tabPrimaria);
        VBox.setVgrow(tabPaneRecepciones, Priority.ALWAYS);

        leftPane.getChildren().addAll(lblListado, filterBox, tabPaneRecepciones);

        // --- LADO DERECHO: FORMULARIO (WHITE CARD EN SCROLLPANE) ---
        VBox rightPane = new VBox(12);
        rightPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        Label lblNuevo = new Label("Registrar Recepción Física");
        lblNuevo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Region spacerForm = new Region();
        HBox.setHgrow(spacerForm, Priority.ALWAYS);

        FontAwesomeIconView iconCog = new FontAwesomeIconView(FontAwesomeIcon.COG);
        iconCog.setFill(Color.web("#0F3E6E"));
        Button btnConfigAtributos = new Button("Configurar Documento", iconCog);
        btnConfigAtributos.setStyle("-fx-background-color: transparent; -fx-text-fill: #0F3E6E; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        btnConfigAtributos.setOnAction(e -> abrirConfiguracionAtributosDoc());

        HBox formHeaderBox = new HBox(8, lblNuevo, spacerForm, btnConfigAtributos);
        formHeaderBox.setAlignment(Pos.CENTER_LEFT);

        docCustomFieldsPane = new HBox(8);
        docCustomFieldsPane.setAlignment(Pos.CENTER_LEFT);
        docCustomFieldsPane.setPadding(new Insets(4, 0, 4, 0));
        customFieldsPane = new HBox(8);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(8);
        formGrid.setVgap(8);

        cbBodega = new ComboBox<>();
        cbBodega.setPromptText("Seleccione Bodega");
        cbBodega.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbBodega.setMaxWidth(Double.MAX_VALUE);
        cbBodega.setOnAction(e -> cargarAnunciosYLocationsDeBodega());

        cbAnuncio = new ComboBox<>();
        cbAnuncio.setPromptText("Seleccione Anuncio de Carga");
        cbAnuncio.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbAnuncio.setMaxWidth(Double.MAX_VALUE);
        cbAnuncio.setOnAction(e -> cargarItemsDeAnuncio());

        txtNumeroBl = new TextField();
        txtNumeroBl.setPromptText("N° BL (Auto)");
        txtNumeroBl.setEditable(false);
        txtNumeroBl.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9;");

        txtOrdenCompra = new TextField();
        txtOrdenCompra.setPromptText("Orden Compra (Auto)");
        txtOrdenCompra.setEditable(false);
        txtOrdenCompra.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9;");

        cbTipoDocumento = new ComboBox<>(FXCollections.observableArrayList("Guía de Despacho", "Factura", "DUS", "Sin Documento", "Otro"));
        cbTipoDocumento.setValue("Guía de Despacho");
        cbTipoDocumento.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbTipoDocumento.setMaxWidth(Double.MAX_VALUE);

        txtNumeroDocumento = new TextField();
        txtNumeroDocumento.setPromptText("N° Documento (Ej: 12345)");
        txtNumeroDocumento.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");

        cbZonaDestino = new ComboBox<>(FXCollections.observableArrayList("COMERCIAL", "PRIMARIA"));
        cbZonaDestino.setValue("COMERCIAL");
        cbZonaDestino.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbZonaDestino.setMaxWidth(Double.MAX_VALUE);
        cbZonaDestino.setOnAction(e -> {
            actualizarFiltroZonas();
            actualizarFiltroUbicaciones();
        });

        txtObservaciones = new TextArea();
        txtObservaciones.setPromptText("Observaciones...");
        txtObservaciones.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtObservaciones.setPrefHeight(36);
        txtObservaciones.setMaxWidth(Double.MAX_VALUE);

        Label lblBod = new Label("Bodega:");
        lblBod.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblAnun = new Label("Anuncio:");
        lblAnun.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblBl = new Label("N° BL:");
        lblBl.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblOc = new Label("Orden Compra:");
        lblOc.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblTipoDoc = new Label("Tipo Doc:");
        lblTipoDoc.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblNumDoc = new Label("N° Doc:");
        lblNumDoc.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblZona = new Label("Zona Destino:");
        lblZona.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblObs = new Label("Observaciones:");
        lblObs.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");

        // Fila 0: Bodega (col 0,1) | Anuncio (col 2,3)
        formGrid.add(lblBod, 0, 0);
        formGrid.add(cbBodega, 1, 0);
        formGrid.add(lblAnun, 2, 0);
        formGrid.add(cbAnuncio, 3, 0);

        // Fila 1: N° BL (col 0,1) | Orden Compra (col 2,3)
        formGrid.add(lblBl, 0, 1);
        formGrid.add(txtNumeroBl, 1, 1);
        formGrid.add(lblOc, 2, 1);
        formGrid.add(txtOrdenCompra, 3, 1);

        // Fila 2: Tipo Doc (col 0,1) | N° Doc (col 2,3)
        formGrid.add(lblTipoDoc, 0, 2);
        formGrid.add(cbTipoDocumento, 1, 2);
        formGrid.add(lblNumDoc, 2, 2);
        formGrid.add(txtNumeroDocumento, 3, 2);

        chkEsContenedor = new CheckBox("Contenedor");
        chkEsContenedor.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        chkEsContenedor.setDisable(true);

        txtNumeroContenedor = new TextField();
        txtNumeroContenedor.setPromptText("N° Contenedor");
        txtNumeroContenedor.setEditable(false);
        txtNumeroContenedor.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9;");

        txtDigitoContenedor = new TextField();
        txtDigitoContenedor.setPromptText("DV");
        txtDigitoContenedor.setEditable(false);
        txtDigitoContenedor.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9;");
        txtDigitoContenedor.setPrefWidth(45);

        HBox containerBox = new HBox(6, chkEsContenedor, txtNumeroContenedor, txtDigitoContenedor);
        HBox.setHgrow(txtNumeroContenedor, Priority.ALWAYS);

        txtObservaciones.setPrefHeight(90);

        // Fila 3: Zona Destino (col 0,1) | Contenedor (col 2,3)
        formGrid.add(lblZona, 0, 3);
        formGrid.add(cbZonaDestino, 1, 3);
        formGrid.add(containerBox, 2, 3);
        GridPane.setColumnSpan(containerBox, 2);

        // Fila 4: Observaciones (col 0) | txtObservaciones (col 1, span 3)
        formGrid.add(lblObs, 0, 4);
        formGrid.add(txtObservaciones, 1, 4);
        GridPane.setColumnSpan(txtObservaciones, 3);

        ColumnConstraints gc1 = new ColumnConstraints(85);
        ColumnConstraints gc2 = new ColumnConstraints(155);
        ColumnConstraints gc3 = new ColumnConstraints(90);
        ColumnConstraints gc4 = new ColumnConstraints(155);
        formGrid.getColumnConstraints().addAll(gc1, gc2, gc3, gc4);

        // Items de recepción
        VBox itemsBox = new VBox(8);
        itemsBox.setPadding(new Insets(8));
        itemsBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e0; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label lblItems = new Label("Detalle de Carga");
        lblItems.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        customFieldsPane = new HBox(8);
        customFieldsPane.setAlignment(Pos.CENTER_LEFT);

        tblItems = new TableView<>();
        tblItems.setItems(receptionRows);
        tblItems.setMinHeight(160);
        tblItems.setPrefHeight(200);
        tblItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblItems.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<ReceptionRow, String> colIProd = new TableColumn<>("Producto");
        colIProd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductoDesc()));
        colIProd.setPrefWidth(160);

        TableColumn<ReceptionRow, String> colIEst = new TableColumn<>("Anunciado");
        colIEst.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantAnunciada())));
        colIEst.setPrefWidth(80);

        TableColumn<ReceptionRow, String> colIRec = new TableColumn<>("Recibido");
        colIRec.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantRecibida())));
        colIRec.setPrefWidth(80);

        TableColumn<ReceptionRow, String> colILoc = new TableColumn<>("Ubicación");
        colILoc.setCellValueFactory(c -> {
            LocationModel loc = locationMap.get(c.getValue().getLocationId());
            return new SimpleStringProperty(loc != null ? loc.toString() : "");
        });
        colILoc.setPrefWidth(120);

        TableColumn<ReceptionRow, Void> colActions = new TableColumn<>("Acción");
        colActions.setPrefWidth(70);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnSplit;
            {
                FontAwesomeIconView iconCut = new FontAwesomeIconView(FontAwesomeIcon.SCISSORS);
                iconCut.setFill(Color.WHITE);
                btnSplit = new Button(null, iconCut);
                btnSplit.setStyle("-fx-background-color: #0d6efd; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-cursor: hand;");
                btnSplit.setTooltip(new Tooltip("Dividir Cantidad en Partes"));
                btnSplit.setOnAction(e -> {
                    ReceptionRow row = getTableView().getItems().get(getIndex());
                    abrirDialogoDivision(row);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnSplit);
                }
            }
        });

        tblItems.getColumns().addAll(colIProd, colIEst, colIRec, colILoc, colActions);

        // Inputs para modificar fila seleccionada
        GridPane itemModifyGrid = new GridPane();
        itemModifyGrid.setHgap(10);
        itemModifyGrid.setVgap(8);
        itemModifyGrid.setPadding(new Insets(8, 0, 8, 0));

        txtCantRecibida = new TextField();
        txtCantRecibida.setPromptText("Cant. Recibida");
        txtCantRecibida.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtCantRecibida.setMaxWidth(Double.MAX_VALUE);

        chkUbicacionExistente = new CheckBox("Con Stock Previo");
        chkUbicacionExistente.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #475569;");

        cbUbicacionExistente = new ComboBox<>();
        cbUbicacionExistente.setPromptText("Ubicación Existente (Stock)");
        cbUbicacionExistente.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbUbicacionExistente.setMaxWidth(Double.MAX_VALUE);
        cbUbicacionExistente.setDisable(true);

        cbZonaFisica = new ComboBox<>();
        cbZonaFisica.setPromptText("Todas las zonas...");
        cbZonaFisica.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbZonaFisica.setMaxWidth(Double.MAX_VALUE);
        configurarComboBoxSearchGeneric(cbZonaFisica, filteredZones);
        cbZonaFisica.valueProperty().addListener((obs, oldVal, newVal) -> {
            actualizarFiltroUbicaciones();
        });

        cbUbicacion = new ComboBox<>();
        cbUbicacion.setPromptText("Nueva Ubicación (WH)");
        cbUbicacion.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbUbicacion.setMaxWidth(Double.MAX_VALUE);
        configurarComboBoxSearchGeneric(cbUbicacion, filteredLocationItems);

        FontAwesomeIconView iconCheck = new FontAwesomeIconView(FontAwesomeIcon.CHECK);
        iconCheck.setFill(Color.WHITE);
        btnApplyItem = new Button(null, iconCheck);
        btnApplyItem.setStyle("-fx-background-color: #0F3E6E; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-cursor: hand;");
        btnApplyItem.setTooltip(new Tooltip("Aplicar Cambios a Fila Seleccionada"));
        btnApplyItem.setMaxWidth(Double.MAX_VALUE);
        btnApplyItem.setOnAction(e -> actualizarFilaItem());

        Label lblCantRec = new Label("Cant. Recibida:");
        lblCantRec.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblZonaFis = new Label("Zona/Sector:");
        lblZonaFis.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblAsignLoc = new Label("Asignar Ubicación:");
        lblAsignLoc.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");

        itemModifyGrid.add(lblCantRec, 0, 0);
        itemModifyGrid.add(txtCantRecibida, 1, 0);
        itemModifyGrid.add(btnApplyItem, 2, 0);

        itemModifyGrid.add(chkUbicacionExistente, 0, 1);
        itemModifyGrid.add(cbUbicacionExistente, 1, 1);

        itemModifyGrid.add(lblZonaFis, 0, 2);
        itemModifyGrid.add(cbZonaFisica, 1, 2);

        itemModifyGrid.add(lblAsignLoc, 0, 3);
        itemModifyGrid.add(cbUbicacion, 1, 3);

        ColumnConstraints mc1 = new ColumnConstraints(125);
        ColumnConstraints mc2 = new ColumnConstraints(280);
        ColumnConstraints mc3 = new ColumnConstraints(40);
        itemModifyGrid.getColumnConstraints().addAll(mc1, mc2, mc3);

        // Helper method to sync cbUbicacionExistente -> cbUbicacion
        Runnable syncExistente = () -> {
            String val = cbUbicacionExistente.getValue();
            if (val != null) {
                String locCode = val.split(" \\(")[0];
                for (LocationModel l : allLocationsOfWarehouse) {
                    if (l.toString().equals(locCode)) {
                        WarehouseZoneModel zone = zoneMap.get(l.getZoneId());
                        if (zone != null) {
                            cbZonaFisica.setValue(zone);
                        }
                        cbUbicacion.setValue(l);
                        break;
                    }
                }
            }
        };

        cbUbicacionExistente.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && chkUbicacionExistente.isSelected()) {
                syncExistente.run();
            }
        });

        chkUbicacionExistente.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (cbUbicacionExistente.getItems().isEmpty()) {
                    mostrarWarning("Sin stock previo", "El producto seleccionado no cuenta con ubicaciones previas registradas.");
                    javafx.application.Platform.runLater(() -> chkUbicacionExistente.setSelected(false));
                    return;
                }
                cbUbicacionExistente.setDisable(false);
                cbZonaFisica.setDisable(true);
                cbUbicacion.setDisable(true);
                syncExistente.run();
            } else {
                cbUbicacionExistente.setDisable(true);
                cbZonaFisica.setDisable(false);
                cbUbicacion.setDisable(false);
            }
        });

        tblItems.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtCantRecibida.setText(String.valueOf(newSel.getCantRecibida()));
                chkUbicacionExistente.setSelected(false);
                cargarUbicacionesExistentes(newSel.getProductId());
                
                LocationModel loc = locationMap.get(newSel.getLocationId());
                if (loc != null) {
                    WarehouseZoneModel zone = zoneMap.get(loc.getZoneId());
                    if (zone != null) {
                        cbZonaFisica.setValue(zone);
                    }
                    cbUbicacion.setValue(loc);
                } else {
                    resetZoneFilter();
                    cbUbicacion.setValue(null);
                }
            } else {
                cbUbicacionExistente.getItems().clear();
                cbUbicacionExistente.setDisable(true);
                chkUbicacionExistente.setSelected(false);
                resetZoneFilter();
                cbUbicacion.setValue(null);
            }
        });

        itemsBox.getChildren().addAll(lblItems, tblItems, itemModifyGrid);

        rightPane.getChildren().addAll(formHeaderBox, formGrid, docCustomFieldsPane, itemsBox);

        // Botones de acción inferior idénticos a Anuncios
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setPadding(new Insets(15, 0, 0, 0));

        Button btnPdf = UiComponentFactory.createPdfButton(this::descargarPDF);

        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.TRUCK);
        iconSave.setFill(Color.WHITE);
        iconSave.setSize("16");
        btnGuardarRecepcion = new Button(null, iconSave);
        btnGuardarRecepcion.setStyle("-fx-background-color: cornflowerblue; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnGuardarRecepcion.setOnAction(e -> procesarRecepcion());
        Tooltip.install(btnGuardarRecepcion, new Tooltip("Procesar Recepción Física"));

        FontAwesomeIconView iconRefresh = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        iconRefresh.setFill(Color.WHITE);
        iconRefresh.setSize("16");
        btnLimpiarRecepcion = new Button(null, iconRefresh);
        btnLimpiarRecepcion.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnLimpiarRecepcion.setOnAction(e -> limpiarFormulario());
        Tooltip.install(btnLimpiarRecepcion, new Tooltip("Limpiar Formulario"));

        actionButtons.getChildren().addAll(btnPdf, btnGuardarRecepcion, btnLimpiarRecepcion);

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

        Scene scene = new Scene(root, 1080, 640);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setScene(scene);
        ScreenUtil.fitDialogToScreen(this, getOwner(), 1080, 640, 950, 520);
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {
                String myCompanyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
                List<SupplierModel> suppliers = service.obtenerProveedores();
                List<WarehouseModel> warehouses = service.obtenerBodegas();
                List<ProductModel> products = service.obtenerProductos();
                List<ReceptionAnnouncementModel> announcements = service.obtenerAnuncios();
                List<ReceptionModel> receptions = service.obtenerRecepciones();

                javafx.application.Platform.runLater(() -> {
                    announcementMap.clear();
                    pendingAnnouncements.clear();
                    for (ReceptionAnnouncementModel a : announcements) {
                        announcementMap.put(a.getId(), a);
                        if (a.getEstado() == null || "PENDIENTE".equalsIgnoreCase(a.getEstado())) {
                            pendingAnnouncements.add(a);
                        }
                    }

                    productMap.clear();
                    for (ProductModel p : products) {
                        productMap.put(p.getId(), p);
                    }

                    supplierMap.clear();
                    for (SupplierModel s : suppliers) {
                        supplierMap.put(s.getId(), s);
                    }
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
                    } else if (!cbBodega.getItems().isEmpty()) {
                        cbBodega.setValue(cbBodega.getItems().get(0));
                    }

                    cargarAnunciosYLocationsDeBodega();

                    receptionsList.clear();
                    receptionsList.addAll(receptions);
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error al cargar datos", e.getMessage());
                });
            }
        }).start();
    }

    private void cargarAnunciosYLocationsDeBodega() {
        WarehouseModel bod = cbBodega.getValue();
        cbAnuncio.getItems().clear();
        filteredLocationItems.clear();
        allLocationsOfWarehouse.clear();
        zoneMap.clear();
        receptionRows.clear();
        if (bod == null) return;

        // Cargar anuncios de esta bodega
        for (ReceptionAnnouncementModel a : pendingAnnouncements) {
            if (bod.getId().equals(a.getWarehouseId())) {
                cbAnuncio.getItems().add(a);
            }
        }

        // Cargar zonas y ubicaciones de esta bodega
        new Thread(() -> {
            try {
                List<WarehouseZoneModel> zones = service.obtenerZonasPorBodega(bod.getId());
                List<LocationModel> locs = new ArrayList<>(service.obtenerUbicacionesPorBodega(bod.getId()));
                locs.sort(LocationModel.NATURAL_ORDER_COMPARATOR);
                javafx.application.Platform.runLater(() -> {
                    for (WarehouseZoneModel z : zones) {
                        zoneMap.put(z.getId(), z);
                    }
                    allLocationsOfWarehouse.addAll(locs);
                    for (LocationModel l : locs) {
                        locationMap.put(l.getId(), l);
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
        
        WarehouseZoneModel allZonesItem = new WarehouseZoneModel();
        allZonesItem.setId(null);
        allZonesItem.setNombre("--- Todas las Zonas ---");
        filteredZones.add(allZonesItem);

        String selectedDest = cbZonaDestino.getValue();
        if (selectedDest == null) selectedDest = "COMERCIAL";

        List<WarehouseZoneModel> matchingZones = new ArrayList<>();
        for (WarehouseZoneModel z : zoneMap.values()) {
            String ambito = determinarAmbitoUbicacion(z.getId());
            if (selectedDest.equalsIgnoreCase(ambito)) {
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
        cbZonaFisica.setValue(allZonesItem);
    }

    private TableView<ReceptionModel> crearTablaRecepciones(FilteredList<ReceptionModel> filteredList) {
        TableView<ReceptionModel> table = new TableView<>(filteredList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ReceptionModel, String> colFolio = new TableColumn<>("Folio");
        colFolio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolio()));
        colFolio.setPrefWidth(100);

        TableColumn<ReceptionModel, String> colAnuncio = new TableColumn<>("Anuncio de Carga");
        colAnuncio.setCellValueFactory(c -> {
            ReceptionAnnouncementModel a = announcementMap.get(c.getValue().getAnnouncementId());
            return new SimpleStringProperty(a != null ? a.getFolio() : "-");
        });
        colAnuncio.setPrefWidth(110);

        TableColumn<ReceptionModel, String> colBod = new TableColumn<>("Bodega");
        colBod.setCellValueFactory(c -> {
            WarehouseModel w = warehouseMap.get(c.getValue().getWarehouseId());
            return new SimpleStringProperty(w != null ? w.getNombre() : "-");
        });
        colBod.setPrefWidth(100);

        TableColumn<ReceptionModel, String> colFecha = new TableColumn<>("Fecha Recepción");
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
        colFecha.setPrefWidth(130);

        TableColumn<ReceptionModel, String> colEst = new TableColumn<>("Estado");
        colEst.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
        colEst.setPrefWidth(80);

        TableColumn<ReceptionModel, String> colZona = new TableColumn<>("Zona");
        colZona.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getZonaDestino() != null ? c.getValue().getZonaDestino() : "-"));
        colZona.setPrefWidth(90);

        // Apply visual styling policy
        StatusColumnHelper.applyStatusStyling(colFolio, true);
        StatusColumnHelper.applyStatusStyling(colEst, false);
        StatusColumnHelper.applyRowFactory(table);

        table.getColumns().addAll(colFolio, colAnuncio, colBod, colFecha, colZona, colEst);
        return table;
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
            WarehouseZoneModel zone = zoneMap.get(currentId);
            if (zone == null) {
                break;
            }
            currentId = zone.getParentZoneId();
        }
        return false;
    }

    private void actualizarFiltroUbicaciones() {
        filteredLocationItems.clear();
        String selectedDest = cbZonaDestino.getValue();
        if (selectedDest == null) selectedDest = "COMERCIAL";

        WarehouseZoneModel rawZone = cbZonaFisica.getValue();
        String selectedZoneId = (rawZone != null) ? rawZone.getId() : null;

        List<LocationModel> matchingLocs = new ArrayList<>();
        for (LocationModel l : allLocationsOfWarehouse) {
            String ambito = determinarAmbitoUbicacion(l.getZoneId());
            if (selectedDest.equalsIgnoreCase(ambito)) {
                if (selectedZoneId == null || esZonaOAncestro(l.getZoneId(), selectedZoneId)) {
                    matchingLocs.add(l);
                }
            }
        }
        matchingLocs.sort(LocationModel.NATURAL_ORDER_COMPARATOR);
        filteredLocationItems.addAll(matchingLocs);
        cbUbicacion.getSelectionModel().clearSelection();
        ReceptionRow selected = tblItems.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cargarUbicacionesExistentes(selected.getProductId());
        }
    }

    private String determinarAmbitoUbicacion(String zoneId) {
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

    
    private void abrirDialogoDivision(ReceptionRow row) {
        if (row == null) return;
        DividirCantidadDialog diag = new DividirCantidadDialog(
            this,
            row.getProductoDesc(),
            row.getCantAnunciada(),
            filteredLocationItems
        );
        List<DividirCantidadDialog.PartitionRow> result = diag.showAndGetResult();
        if (result != null && !result.isEmpty()) {
            int idx = receptionRows.indexOf(row);
            if (idx >= 0) {
                receptionRows.remove(idx);
                int insertPos = idx;
                for (DividirCantidadDialog.PartitionRow p : result) {
                    String locId = p.getUbicacion() != null ? p.getUbicacion().getId() : null;
                    ReceptionRow splitRow = new ReceptionRow(
                        row.getProductId(),
                        row.getProductoDesc(),
                        p.getCantidad(),
                        p.getCantidad(),
                        locId
                    );
                    receptionRows.add(insertPos++, splitRow);
                }
                tblItems.refresh();
            }
        }
    }

    private void cargarItemsDeAnuncio() {
        receptionRows.clear();
        ReceptionAnnouncementModel a = cbAnuncio.getValue();
        if (a == null) {
            txtNumeroBl.clear();
            txtOrdenCompra.clear();
            chkEsContenedor.setSelected(false);
            txtNumeroContenedor.clear();
            txtDigitoContenedor.clear();
            return;
        }

        txtNumeroBl.setText(a.getNumeroBl() != null ? a.getNumeroBl() : "");
        txtOrdenCompra.setText(a.getOrdenCompra() != null ? a.getOrdenCompra() : "");
        boolean esCont = a.getEsContenedor() != null && a.getEsContenedor();
        chkEsContenedor.setSelected(esCont);
        txtNumeroContenedor.setText(esCont && a.getNumeroContenedor() != null ? a.getNumeroContenedor() : "");
        txtDigitoContenedor.setText(esCont && a.getDigitoContenedor() != null ? a.getDigitoContenedor() : "");
        cargarCamposPersonalizadosDocumento();
        cargarCamposPersonalizadosProducto();

        for (AnnouncementDetailModel d : a.getDetails()) {
            ProductModel prod = productMap.get(d.getProductId());
            String desc = prod != null ? prod.toString() : "Producto Desconocido";
            receptionRows.add(new ReceptionRow(d.getProductId(), desc, d.getCantidad(), d.getCantidad(), null));
        }
    }

    private void cargarUbicacionesExistentes(String productId) {
        cbUbicacionExistente.getItems().clear();
        cbUbicacionExistente.setDisable(true);
        cbUbicacionExistente.setPromptText("Buscando stock...");
        if (productId == null) return;

        new Thread(() -> {
            try {
                List<com.logistics.packinglist.model.StockModel> stocks = service.obtenerStocksPorProducto(productId);
                javafx.application.Platform.runLater(() -> {
                    boolean found = false;
                    String selectedDest = cbZonaDestino.getValue();
                    if (selectedDest == null) selectedDest = "COMERCIAL";

                    for (com.logistics.packinglist.model.StockModel s : stocks) {
                        LocationModel loc = locationMap.get(s.getLocationId());
                        if (loc != null) {
                            String ambito = determinarAmbitoUbicacion(loc.getZoneId());
                            if (selectedDest.equalsIgnoreCase(ambito)) {
                                cbUbicacionExistente.getItems().add(loc.toString() + " (" + s.getCantidad() + " un.)");
                                found = true;
                            }
                        }
                    }
                    if (found) {
                        cbUbicacionExistente.setPromptText("Ubicaciones de Stock");
                    } else {
                        cbUbicacionExistente.setPromptText("Sin ubicaciones registradas");
                        if (chkUbicacionExistente.isSelected()) {
                            chkUbicacionExistente.setSelected(false);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    cbUbicacionExistente.setPromptText("Error al consultar stock");
                });
            }
        }).start();
    }

    private void actualizarFilaItem() {
        ReceptionRow row = tblItems.getSelectionModel().getSelectedItem();
        String cantStr = txtCantRecibida.getText().trim();
        LocationModel loc = null;
        Object rawVal = cbUbicacion.getValue();
        if (rawVal instanceof LocationModel) {
            loc = (LocationModel) rawVal;
        } else if (rawVal instanceof String) {
            String text = ((String) rawVal).trim();
            for (LocationModel item : filteredLocationItems) {
                if (item.toString().equalsIgnoreCase(text)) {
                    loc = item;
                    break;
                }
            }
        }

        if (loc == null && cbUbicacion.getEditor().getText() != null) {
            String text = cbUbicacion.getEditor().getText().trim();
            for (LocationModel item : filteredLocationItems) {
                if (item.toString().equalsIgnoreCase(text)) {
                    loc = item;
                    break;
                }
            }
        }

        if (row == null) {
            mostrarWarning("Selección", "Debe seleccionar un producto del detalle.");
            return;
        }

        int cant;
        try {
            cant = Integer.parseInt(cantStr);
            if (cant < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarWarning("Validación", "La cantidad recibida debe ser un número entero mayor o igual a 0.");
            return;
        }

        if (loc == null) {
            mostrarWarning("Validación", "Debe seleccionar una ubicación de almacenamiento.");
            return;
        }

        row.setCantRecibida(cant);
        row.setLocationId(loc.getId());
        tblItems.refresh();
    }

    private void dividirItem() {
        ReceptionRow row = tblItems.getSelectionModel().getSelectedItem();
        if (row == null) {
            mostrarWarning("Selección", "Debe seleccionar un producto del detalle.");
            return;
        }

        String cantStr = txtCantRecibida.getText().trim();
        int cantSplit;
        try {
            cantSplit = Integer.parseInt(cantStr);
            if (cantSplit <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarWarning("Validación", "Ingrese la cantidad (entero > 0) a separar en el campo 'Cant. Recibida'.");
            return;
        }

        if (cantSplit >= row.getCantRecibida()) {
            mostrarWarning("Validación", "La cantidad a dividir debe ser menor que la cantidad recibida actual de la fila (" + row.getCantRecibida() + ").");
            return;
        }

        int nuevaCantOriginal = row.getCantRecibida() - cantSplit;
        row.setCantRecibida(nuevaCantOriginal);

        ReceptionRow newRow = new ReceptionRow(
                row.getProductId(),
                row.getProductoDesc(),
                0,
                cantSplit,
                null
        );

        receptionRows.add(newRow);
        tblItems.refresh();
        tblItems.getSelectionModel().select(newRow);
        
        mostrarInformacion("Fila Dividida", "Se han extraído " + cantSplit + " unidades del item. Asigne una ubicación a la nueva fila creada.");
    }

    private void eliminarDividido() {
        ReceptionRow row = tblItems.getSelectionModel().getSelectedItem();
        if (row == null) {
            mostrarWarning("Selección", "Debe seleccionar una fila del detalle.");
            return;
        }

        if (row.getCantAnunciada() > 0) {
            mostrarWarning("Validación", "No se puede eliminar la fila original del anuncio. Solo puede eliminar filas creadas por división.");
            return;
        }

        ReceptionRow originalRow = null;
        for (ReceptionRow r : receptionRows) {
            if (r.getProductId().equals(row.getProductId()) && r.getCantAnunciada() > 0) {
                originalRow = r;
                break;
            }
        }

        if (originalRow != null) {
            originalRow.setCantRecibida(originalRow.getCantRecibida() + row.getCantRecibida());
        }

        receptionRows.remove(row);
        tblItems.refresh();
        if (originalRow != null) {
            tblItems.getSelectionModel().select(originalRow);
        }
        
        mostrarInformacion("Fila Eliminada", "Se eliminó la fila dividida y su cantidad se reintegró a la fila original.");
    }

    private void procesarRecepcion() {
        WarehouseModel bod = cbBodega.getValue();
        ReceptionAnnouncementModel a = cbAnuncio.getValue();
        String obs = txtObservaciones.getText().trim();

        if (bod == null || a == null) {
            mostrarWarning("Validación", "Debe seleccionar bodega y anuncio.");
            return;
        }

        if (receptionRows.isEmpty()) {
            mostrarWarning("Validación", "No hay productos que recepcionar.");
            return;
        }

        // Validar que todos los items tengan una ubicación asignada (solo si cantRecibida > 0)
        for (ReceptionRow row : receptionRows) {
            if (row.getCantRecibida() > 0 && row.getLocationId() == null) {
                mostrarWarning("Validación", "Debe asignar una ubicación a todos los productos recibidos antes de procesar.");
                return;
            }
        }

        ReceptionModel model = new ReceptionModel();
        model.setCompanyId(bod.getCompanyId());
        model.setWarehouseId(bod.getId());
        model.setAnnouncementId(a.getId());
        model.setObservaciones(obs.isEmpty() ? null : obs);
        model.setZonaDestino(cbZonaDestino.getValue());

        List<ReceptionDetailModel> details = new ArrayList<>();
        for (ReceptionRow row : receptionRows) {
            if (row.getCantRecibida() > 0) {
                ReceptionDetailModel d = new ReceptionDetailModel();
                d.setProductId(row.getProductId());
                d.setCantidad(row.getCantRecibida());
                d.setLocationId(row.getLocationId());
                d.setAnnouncementDetailId(row.getAnnouncementDetailId());
                d.setAtributosPersonalizados(new HashMap<>(row.getAtributosPersonalizados()));
                details.add(d);
            }
        }
        model.setDetails(details);

        new Thread(() -> {
            try {
                ReceptionModel created = service.crearRecepcion(model);
                javafx.application.Platform.runLater(() -> {
                    mostrarInformacion("Éxito", "Recepción física procesada correctamente con Folio: " + created.getFolio());
                    
                    // Verificar discrepancias netas por SKU
                    Map<String, Integer> announcedBySku = new HashMap<>();
                    Map<String, Integer> receivedBySku = new HashMap<>();
                    if (a.getDetails() != null) {
                        for (AnnouncementDetailModel ad : a.getDetails()) {
                            announcedBySku.put(ad.getProductId(), announcedBySku.getOrDefault(ad.getProductId(), 0) + (ad.getCantidad() != null ? ad.getCantidad() : 0));
                        }
                    }
                    if (created.getDetails() != null) {
                        for (ReceptionDetailModel rd : created.getDetails()) {
                            receivedBySku.put(rd.getProductId(), receivedBySku.getOrDefault(rd.getProductId(), 0) + (rd.getCantidad() != null ? rd.getCantidad() : 0));
                        }
                    }

                    boolean hayDiferencias = false;
                    for (Map.Entry<String, Integer> entry : announcedBySku.entrySet()) {
                        int annQ = entry.getValue();
                        int recQ = receivedBySku.getOrDefault(entry.getKey(), 0);
                        if (annQ != recQ) {
                            hayDiferencias = true;
                            break;
                        }
                    }

                    limpiarFormulario();
                    cargarDatos();

                    if (hayDiferencias) {
                        Alert alertDiff = new Alert(Alert.AlertType.CONFIRMATION);
                        alertDiff.setTitle("Informe de Diferencias");
                        alertDiff.setHeaderText("Se detectaron diferencias en la recepción");
                        alertDiff.setContentText("Existen diferencias entre las cantidades anunciadas y recepcionadas. ¿Desea exportar la planilla Excel de Diferencias para enviar al proveedor?");
                        if (alertDiff.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                            exportarExcelDiferencias(a, created);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error al procesar", e.getMessage());
                });
            }
        }).start();
    }

    private void limpiarFormulario() {
        cbBodega.getSelectionModel().clearSelection();
        cbAnuncio.getSelectionModel().clearSelection();
        cbZonaDestino.setValue("COMERCIAL");
        txtObservaciones.clear();
        receptionRows.clear();
        txtCantRecibida.clear();
        cbUbicacionExistente.getItems().clear();
        cbUbicacionExistente.setDisable(true);
        if (chkUbicacionExistente != null) {
            chkUbicacionExistente.setSelected(false);
        }
        filteredZones.clear();
        cbZonaFisica.getSelectionModel().clearSelection();
        cbUbicacion.getSelectionModel().clearSelection();
    }

    private void resetZoneFilter() {
        if (!filteredZones.isEmpty()) {
            cbZonaFisica.setValue(filteredZones.get(0));
        } else {
            cbZonaFisica.setValue(null);
        }
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
                javafx.application.Platform.runLater(() -> {
                    comboBox.getEditor().setText(newVal.toString());
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

            if (!comboBox.isShowing() && comboBox.getEditor().isFocused() && !filteredList.isEmpty()) {
                comboBox.show();
            }
        });

        comboBox.getEditor().setOnMouseClicked(e -> {
            if (!comboBox.isShowing()) {
                comboBox.show();
            }
        });

        comboBox.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused && !comboBox.isShowing()) {
                comboBox.show();
            }
        });
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
    public static class ReceptionRow {
        private final String productId;
        private final String productoDesc;
        private final int cantAnunciada;
        private int cantRecibida;
        private String locationId;
        private String announcementDetailId;
        private java.util.Map<String, String> atributosPersonalizados;

        public ReceptionRow(String productId, String productoDesc, int cantAnunciada, int cantRecibida, String locationId) {
            this(productId, productoDesc, cantAnunciada, cantRecibida, locationId, null, new java.util.HashMap<>());
        }

        public ReceptionRow(String productId, String productoDesc, int cantAnunciada, int cantRecibida, String locationId, String announcementDetailId, java.util.Map<String, String> atributosPersonalizados) {
            this.productId = productId;
            this.productoDesc = productoDesc;
            this.cantAnunciada = cantAnunciada;
            this.cantRecibida = cantRecibida;
            this.locationId = locationId;
            this.announcementDetailId = announcementDetailId;
            this.atributosPersonalizados = atributosPersonalizados != null ? atributosPersonalizados : new java.util.HashMap<>();
        }

        public String getProductId() { return productId; }
        public String getProductoDesc() { return productoDesc; }
        public int getCantAnunciada() { return cantAnunciada; }
        public int getCantRecibida() { return cantRecibida; }
        public void setCantRecibida(int cantRecibida) { this.cantRecibida = cantRecibida; }
        public String getLocationId() { return locationId; }
        public void setLocationId(String locationId) { this.locationId = locationId; }
        public String getAnnouncementDetailId() { return announcementDetailId; }
        public java.util.Map<String, String> getAtributosPersonalizados() { return atributosPersonalizados; }
    }

    private void exportarExcelDiferencias(ReceptionAnnouncementModel announcement, ReceptionModel reception) {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Guardar Excel de Diferencias");
        String fileName = "Diferencias_" + (reception != null && reception.getFolio() != null ? reception.getFolio() : "RECEPCION") + ".xlsx";
        fc.setInitialFileName(fileName);
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Planilla Excel (*.xlsx)", "*.xlsx"));

        File file = fc.showSaveDialog(this);
        if (file != null) {
            new Thread(() -> {
                try {
                    ExcelDiferenciasService excelService = new ExcelDiferenciasService();
                    SupplierModel supplier = supplierMap.get(announcement != null ? announcement.getSupplierId() : null);
                    WarehouseModel warehouse = warehouseMap.get(announcement != null ? announcement.getWarehouseId() : null);
                    
                    String companyId = announcement != null ? announcement.getCompanyId() : null;
                    String warehouseId = warehouse != null ? warehouse.getId() : null;
                    String supplierId = supplier != null ? supplier.getId() : null;
                    List<ProductFieldDefinitionModel> fieldDefs = service.obtenerDefinicionesCampos(companyId, "RECEPCION", warehouseId, supplierId);

                    excelService.generarReporteDiferencias(announcement, reception, supplier, warehouse, productMap, fieldDefs, file);

                    javafx.application.Platform.runLater(() -> mostrarInformacion("Excel Exportado", "El reporte de diferencias se exportó correctamente."));
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> mostrarError("Error Excel", "No se pudo generar el Excel de diferencias: " + e.getMessage()));
                }
            }).start();
        }
    }


    private void abrirConfiguracionAtributosDoc() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        ReceptionAnnouncementModel ann = cbAnuncio.getValue();
        String supplierId = ann != null ? ann.getSupplierId() : null;

        ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, "RECEPCION_DOC", warehouseId, supplierId, this::cargarCamposPersonalizadosDocumento);
        diag.showAndWait();
    }


    private void cargarCamposPersonalizadosDocumento() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        ReceptionAnnouncementModel ann = cbAnuncio.getValue();
        String supplierId = ann != null ? ann.getSupplierId() : null;

        new Thread(() -> {
            try {
                List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "RECEPCION_DOC", warehouseId, null);
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
        ReceptionAnnouncementModel ann = cbAnuncio.getValue();
        String supplierId = ann != null ? ann.getSupplierId() : null;

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
            lbl.setStyle(f.isRequired() ? "-fx-font-size: 9.5px; -fx-text-fill: #dc2626; -fx-font-weight: bold;" : "-fx-font-size: 9.5px; -fx-text-fill: #475569;");

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

        for (com.logistics.packinglist.model.ProductFieldDefinitionModel f : activeFieldDefinitions) {
            VBox box = new VBox(2);
            String labelText = (f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey());
            Label lbl = new Label(labelText + (f.isRequired() ? " *" : ""));
            lbl.setStyle(f.isRequired() ? "-fx-font-size: 9.5px; -fx-text-fill: #dc2626; -fx-font-weight: bold;" : "-fx-font-size: 9.5px; -fx-text-fill: #475569;");

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
        if (tblItems == null) return;
        tblItems.getColumns().clear();

        TableColumn<ReceptionRow, String> colIProd = new TableColumn<>("Producto");
        colIProd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductoDesc()));
        colIProd.setPrefWidth(160);

        TableColumn<ReceptionRow, String> colIEst = new TableColumn<>("Anunciado");
        colIEst.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantAnunciada())));
        colIEst.setPrefWidth(80);

        TableColumn<ReceptionRow, String> colIRec = new TableColumn<>("Recibido");
        colIRec.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantRecibida())));
        colIRec.setPrefWidth(80);

        TableColumn<ReceptionRow, String> colILoc = new TableColumn<>("Ubicación");
        colILoc.setCellValueFactory(c -> {
            LocationModel loc = locationMap.get(c.getValue().getLocationId());
            return new SimpleStringProperty(loc != null ? loc.toString() : "");
        });
        colILoc.setPrefWidth(120);

        tblItems.getColumns().addAll(colIProd, colIEst, colIRec, colILoc);

        for (com.logistics.packinglist.model.ProductFieldDefinitionModel f : activeFieldDefinitions) {
            TableColumn<ReceptionRow, String> colAttr = new TableColumn<>(f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey());
            colAttr.setCellValueFactory(c -> {
                Map<String, String> m = c.getValue().getAtributosPersonalizados();
                return new SimpleStringProperty(m != null ? m.getOrDefault(f.getFieldKey(), "-") : "-");
            });
            colAttr.setPrefWidth(110);
            tblItems.getColumns().add(colAttr);
        }

        TableColumn<ReceptionRow, Void> colActions = new TableColumn<>("Acción");
        colActions.setPrefWidth(70);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnSplit;
            {
                FontAwesomeIconView iconCut = new FontAwesomeIconView(FontAwesomeIcon.SCISSORS);
                iconCut.setFill(Color.WHITE);
                btnSplit = new Button(null, iconCut);
                btnSplit.setStyle("-fx-background-color: #0d6efd; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-cursor: hand;");
                btnSplit.setTooltip(new Tooltip("Dividir Cantidad en Partes"));
                btnSplit.setOnAction(e -> {
                    ReceptionRow row = getTableView().getItems().get(getIndex());
                    abrirDialogoDivision(row);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnSplit);
                }
            }
        });

        tblItems.getColumns().add(colActions);
    }


    private void descargarPDF() {
        ReceptionAnnouncementModel announcement = cbAnuncio.getValue();
        if (announcement == null) {
            mostrarWarning("Exportar PDF", "Debe seleccionar un Anuncio de Carga o cargar una recepción para exportar el documento.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Guardar PDF de Recepción de Carga");
        String folioName = (announcement.getFolio() != null ? "REC-" + announcement.getFolio() : "RECEPCION");
        fileChooser.setInitialFileName(folioName + ".pdf");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));

        File file = fileChooser.showSaveDialog(this);
        if (file != null) {
            new Thread(() -> {
                try {
                    PdfAnuncioService pdfService = new PdfAnuncioService();
                    SupplierModel supplier = supplierMap.get(announcement.getSupplierId());
                    WarehouseModel warehouse = warehouseMap.get(announcement.getWarehouseId());
                    
                    CompanyModel compObj = null;
                    String companyId = announcement.getCompanyId() != null ? announcement.getCompanyId() : (warehouse != null ? warehouse.getCompanyId() : null);
                    if (companyId != null) {
                        try {
                            compObj = service.obtenerEmpresaPorId(companyId);
                        } catch (Exception ignored) {}
                    }

                    List<com.logistics.packinglist.model.ProductFieldDefinitionModel> fieldDefs = activeFieldDefinitions;
                    pdfService.exportar(announcement, compObj, supplier, warehouse, productMap, null, null, fieldDefs, file);

                    javafx.application.Platform.runLater(() -> mostrarInformacion("Éxito", "Documento PDF generado correctamente en: " + file.getAbsolutePath()));
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> mostrarError("Error PDF", "No se pudo generar el documento PDF: " + e.getMessage()));
                }
            }).start();
        }
    }

}
