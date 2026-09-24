package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.Bulto;
import com.logistics.packinglist.model.PackingItem;
import com.logistics.packinglist.model.PackingList;
import com.logistics.packinglist.model.OrderNoteModel;
import com.logistics.packinglist.model.OrderNoteDetailModel;
import com.logistics.packinglist.service.InventarioService;
import com.logistics.packinglist.service.MantenimientoService;
import com.logistics.packinglist.service.WebSocketManager;
import com.logistics.packinglist.service.PdfNotaPedidoService;
import com.logistics.packinglist.model.CustomerModel;
import com.logistics.packinglist.model.SupplierModel;
import com.logistics.packinglist.model.LocationModel;
import com.logistics.packinglist.model.StockModel;
import com.logistics.packinglist.model.ReceptionModel;
import com.logistics.packinglist.model.CompanyModel;
import java.io.File;
import javafx.stage.FileChooser;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.logistics.packinglist.utils.ScreenUtil;
import javafx.util.converter.IntegerStringConverter;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public class PackingListDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    private final MainController mainController;
    private final InventarioService inventarioService;

    // Left Panel Lists
    private final ObservableList<OrderNoteModel> listaNPs = FXCollections.observableArrayList();
    private final javafx.collections.transformation.FilteredList<OrderNoteModel> filteredNPs = new javafx.collections.transformation.FilteredList<>(
            listaNPs, p -> true);
    private final ObservableList<PackingList> listaPackings = FXCollections.observableArrayList();

    private TableView<OrderNoteModel> tablaNPs;
    private TableView<PackingList> tablaPackings;
    private TextField txtSearchNP;

    // Right Panel Form state
    private final ObservableList<Bulto> listaBultos = FXCollections.observableArrayList();
    private Bulto bultoSeleccionado = null;
    private boolean bloqueandoUpdates = false;

    private TextField txtOrden;
    private TextField txtFecha;
    private ListView<Bulto> listBultos;
    private TableView<PackingItem> tablaItems;
    private final ObservableList<PackingItem> itemsBultoActual = FXCollections.observableArrayList();
    private TextField txtLargo, txtAncho, txtAlto, txtPesoBruto, txtPesoNeto;
    private TextField txtObservaciones;
    private Button btnDescargarPicking;
    private Button btnImprimirPicking;
    private final List<String> uploadedPhotoUrls = new java.util.ArrayList<>();
    private FlowPane photoPreviewPane;
    private Label lblPhotoCount;
    private ComboBox<String> cmbTipo;
    private VBox boxDerechoForm;
    private VBox colBultosLeft;
    private OrderNoteModel selectedNP = null;
    private final java.util.Map<String, com.logistics.packinglist.model.ProductModel> productMap = new java.util.HashMap<>();

    // WS listeners
    private final WebSocketManager.UpdateListener orderNoteListener;
    private final WebSocketManager.UpdateListener packingListListener;

    public PackingListDialog(Window owner, MainController mainController) {
        this.mainController = mainController;
        this.inventarioService = new InventarioService();

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Picking & Packing NP (WMS)");

        setMinWidth(950);
        setMinHeight(540);

        construirUI();

        // WebSocket bindings for real-time updates
        orderNoteListener = action -> {
            System.out.println("[PackingListDialog] Real-time NP update event: " + action);
            cargarNPs();
        };
        packingListListener = action -> {
            System.out.println("[PackingListDialog] Real-time Packing List update event: " + action);
            cargarPackingLists();
        };
        WebSocketManager.getInstance().subscribe("ORDER_NOTE", orderNoteListener);
        WebSocketManager.getInstance().subscribe("PACKING_LIST", packingListListener);

        setOnCloseRequest(e -> {
            WebSocketManager.getInstance().unsubscribe("ORDER_NOTE", orderNoteListener);
            WebSocketManager.getInstance().unsubscribe("PACKING_LIST", packingListListener);
        });

        // Load data in background threads
        cargarNPs();
        cargarPackingLists();
    }

    private void construirUI() {
        SplitPane mainSplit = new SplitPane();
        mainSplit.getStyleClass().add("dialog-root");
        mainSplit.setStyle("-fx-background-color: white; -fx-padding: 15;");

        // ==========================================
        // PANEL IZQUIERDO: TabPane (NPs / Historial)
        // ==========================================
        TabPane tabPaneLeft = new TabPane();
        tabPaneLeft.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPaneLeft.setStyle("-fx-background-color: white; -fx-padding: 0;");

        // Tab 1: Notas de Pedido (NP)
        Tab tabNPs = new Tab("Notas de Pedido (NP)");
        VBox boxNPs = new VBox(10);
        boxNPs.setPadding(new Insets(10));
        boxNPs.setStyle("-fx-background-color: white;");

        txtSearchNP = new TextField();
        txtSearchNP.setPromptText("Buscar NP por folio...");
        txtSearchNP.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal == null ? "" : newVal.trim().toLowerCase();
            filteredNPs.setPredicate(note -> {
                if (query.isEmpty())
                    return true;
                return note.getFolio() != null && note.getFolio().toLowerCase().contains(query);
            });
        });

        tablaNPs = new TableView<>(filteredNPs);
        tablaNPs.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<OrderNoteModel, String> colNPFolio = new TableColumn<>("Folio NP");
        colNPFolio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolio()));
        colNPFolio.setPrefWidth(90);

        TableColumn<OrderNoteModel, String> colNPFecha = new TableColumn<>("Fecha");
        colNPFecha.setCellValueFactory(c -> new SimpleStringProperty(DateFormatterUtil.format(c.getValue().getFecha())));
        colNPFecha.setPrefWidth(110);

        TableColumn<OrderNoteModel, String> colNPEstado = new TableColumn<>("Estado NP");
        colNPEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
        colNPEstado.setPrefWidth(85);
        StatusColumnHelper.applyStatusStyling(colNPEstado, false);

        TableColumn<OrderNoteModel, String> colEstadoPicking = new TableColumn<>("Estado Picking");
        colEstadoPicking.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getEstadoPicking() != null ? c.getValue().getEstadoPicking() : "NO_INICIADO"
        ));
        colEstadoPicking.setPrefWidth(100);
        colEstadoPicking.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label();
                    lbl.setStyle("-fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4; -fx-text-fill: white;");
                    if ("PREPARADO".equalsIgnoreCase(item)) {
                        lbl.setText("Preparado");
                        lbl.setStyle(lbl.getStyle() + " -fx-background-color: #16a34a;");
                    } else if ("EN_PROCESO".equalsIgnoreCase(item)) {
                        lbl.setText("En proceso");
                        lbl.setStyle(lbl.getStyle() + " -fx-background-color: #d97706;");
                    } else {
                        lbl.setText("No iniciado");
                        lbl.setStyle(lbl.getStyle() + " -fx-background-color: #64748b;");
                    }
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        TableColumn<OrderNoteModel, Boolean> colModificacion = new TableColumn<>("Modificación NP");
        colModificacion.setCellValueFactory(c -> new javafx.beans.value.ObservableValueBase<>() {
            @Override
            public Boolean getValue() {
                return c.getValue().getNpModificada();
            }
        });
        colModificacion.setPrefWidth(130);
        colModificacion.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label();
                    lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 9.5px;");
                    if (item) {
                        lbl.setText("⚠ Cambios sin revisar");
                        lbl.setStyle(lbl.getStyle() + " -fx-text-fill: #ea580c;");
                    } else {
                        lbl.setText("✔ Revisada");
                        lbl.setStyle(lbl.getStyle() + " -fx-text-fill: #16a34a;");
                    }
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        tablaNPs.getColumns().addAll(colNPFolio, colNPFecha, colNPEstado, colEstadoPicking, colModificacion);
        VBox.setVgrow(tablaNPs, Priority.ALWAYS);

        // Selection listener to prefill order folio in right form
        tablaNPs.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedNP = newVal;
                txtOrden.setText(newVal.getFolio());
                listaBultos.clear();
                seleccionarBulto(null);
                actualizarInventarioLocalConNP(newVal);

                boolean isAnulada = "ANULADA".equalsIgnoreCase(newVal.getEstado()) || "ANULADO".equalsIgnoreCase(newVal.getEstado());
                colBultosLeft.setDisable(isAnulada);

                if (isAnulada) {
                    btnDescargarPicking.setDisable(true);
                    btnImprimirPicking.setDisable(true);
                    boxDerechoForm.setDisable(true);
                } else {
                    boolean isAuthorized = "CONFIRMADA".equalsIgnoreCase(newVal.getEstado()) 
                            || "EN_PROCESO".equalsIgnoreCase(newVal.getEstadoPicking()) 
                            || "PREPARADO".equalsIgnoreCase(newVal.getEstadoPicking());
                    btnDescargarPicking.setDisable(!isAuthorized);
                    btnImprimirPicking.setDisable(!isAuthorized);

                    if (newVal.getNpModificada() != null && newVal.getNpModificada()) {
                        alerta("Nota de Pedido Modificada", 
                            "La Nota de Pedido fue modificada posteriormente al Picking.\n\nSe recomienda revisar y actualizar el Picking antes de continuar.");
                    }
                }
            } else {
                selectedNP = null;
                txtOrden.setText("");
                inventarioService.getItems().clear();
                btnDescargarPicking.setDisable(true);
                btnImprimirPicking.setDisable(true);
                colBultosLeft.setDisable(false);
            }
        });

        HBox boxPickingAction = new HBox(10);
        boxPickingAction.setAlignment(Pos.CENTER_LEFT);

        FontAwesomeIconView iconDownload = new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD);
        iconDownload.setFill(javafx.scene.paint.Color.WHITE);
        btnDescargarPicking = new Button(null, iconDownload);
        btnDescargarPicking.setStyle("-fx-background-color: #FA8072; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnDescargarPicking.setDisable(true);
        btnDescargarPicking.setOnAction(e -> descargarHojaPicking());
        javafx.scene.control.Tooltip.install(btnDescargarPicking, new javafx.scene.control.Tooltip("Descargar Hoja de Picking"));
        
        FontAwesomeIconView iconPrint = new FontAwesomeIconView(FontAwesomeIcon.PRINT);
        iconPrint.setFill(javafx.scene.paint.Color.WHITE);
        btnImprimirPicking = new Button(null, iconPrint);
        btnImprimirPicking.setStyle("-fx-background-color: #FA8072; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnImprimirPicking.setDisable(true);
        btnImprimirPicking.setOnAction(e -> imprimirHojaPicking());
        javafx.scene.control.Tooltip.install(btnImprimirPicking, new javafx.scene.control.Tooltip("Imprimir Hoja de Picking"));
        
        // RN-TRK-001 & RN-TRK-002: Check module permission
        btnDescargarPicking.setVisible(mainController.userHasPermission("FILE_PACKING_LISTS"));
        
        btnImprimirPicking.setVisible(mainController.userHasPermission("FILE_PACKING_LISTS"));
        
        boxPickingAction.getChildren().addAll(btnDescargarPicking, btnImprimirPicking);

        boxNPs.getChildren().addAll(txtSearchNP, tablaNPs, boxPickingAction);
        tabNPs.setContent(boxNPs);

        // Tab 2: Historial Packing Lists
        Tab tabHistorial = new Tab("Historial Packing Lists");
        VBox boxHistorial = new VBox(10);
        boxHistorial.setPadding(new Insets(10));
        boxHistorial.setStyle("-fx-background-color: white;");

        tablaPackings = new TableView<>(listaPackings);
        tablaPackings.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PackingList, String> colPO = new TableColumn<>("Nota Pedido");
        colPO.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroOrden()));
        colPO.setPrefWidth(110);

        TableColumn<PackingList, String> colHistFecha = new TableColumn<>("Fecha");
        colHistFecha.setCellValueFactory(c -> new SimpleStringProperty(DateFormatterUtil.format(c.getValue().getFecha())));
        colHistFecha.setPrefWidth(90);

        TableColumn<PackingList, Number> colHistBultos = new TableColumn<>("Bultos");
        colHistBultos.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumeroBultos()));
        colHistBultos.setStyle("-fx-alignment: CENTER;");
        colHistBultos.setPrefWidth(60);

        TableColumn<PackingList, String> colHistPeso = new TableColumn<>("Peso Total");
        colHistPeso.setCellValueFactory(
                c -> new SimpleStringProperty(String.format("%.1f kg", c.getValue().getPesoTotalKg())));
        colHistPeso.setStyle("-fx-alignment: CENTER-RIGHT;");
        colHistPeso.setPrefWidth(90);

        tablaPackings.getColumns().addAll(colPO, colHistFecha, colHistBultos, colHistPeso);
        VBox.setVgrow(tablaPackings, Priority.ALWAYS);

        // Open on double click
        tablaPackings.setRowFactory(tv -> {
            TableRow<PackingList> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    abrirPackingList(row.getItem());
                }
            });
            return row;
        });

        HBox histActions = new HBox(10);
        Button btnAbrirTab = new Button("Abrir en Pestaña", new FontAwesomeIconView(FontAwesomeIcon.EXTERNAL_LINK));
        btnAbrirTab.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAbrirTab.setOnAction(e -> {
            PackingList sel = tablaPackings.getSelectionModel().getSelectedItem();
            if (sel != null)
                abrirPackingList(sel);
        });

        Button btnEliminarPL = new Button("Eliminar", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
        btnEliminarPL.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");
        btnEliminarPL.setOnAction(e -> eliminarPackingList());

        histActions.getChildren().addAll(btnAbrirTab, btnEliminarPL);
        boxHistorial.getChildren().addAll(tablaPackings, histActions);
        tabHistorial.setContent(boxHistorial);

        tabPaneLeft.getTabs().addAll(tabNPs, tabHistorial);

        // ==========================================
        // PANEL DERECHO: Formulario de Creación Manual
        // ==========================================
        BorderPane rightPane = new BorderPane();
        rightPane.setPadding(new Insets(15));
        rightPane.setStyle(
                "-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 6; -fx-background-radius: 6;");

        // Top Cabecera Formulario
        HBox cabeceraForm = new HBox(15);
        cabeceraForm.setPadding(new Insets(0, 0, 15, 0));
        cabeceraForm.setAlignment(Pos.CENTER_LEFT);
        cabeceraForm.setStyle("-fx-background-color: white;");

        txtOrden = new TextField();
        txtOrden.setPromptText("Ej. NP-2026-10");
        txtOrden.setEditable(false);
        txtOrden.setStyle("-fx-background-color: #eee;");

        txtFecha = new TextField(
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        txtFecha.setEditable(false);
        txtFecha.setStyle("-fx-background-color: #eee;");

        cabeceraForm.getChildren().addAll(
                new Label("Nota de Pedido (NP):"), txtOrden,
                new Label("Fecha:"), txtFecha);
        rightPane.setTop(cabeceraForm);

        // Center Split (Bultos / Detalles + Items)
        SplitPane formSplit = new SplitPane();
        formSplit.setStyle("-fx-background-color: white; -fx-box-border: transparent; -fx-padding: 0;");

        // Left Column of FormSplit: Pallets List
        colBultosLeft = new VBox(10);
        colBultosLeft.setPrefWidth(220);
        colBultosLeft.setPadding(new Insets(10, 15, 10, 10));
        colBultosLeft.setStyle("-fx-background-color: white;");

        Label lblBultosHeader = new Label("Bultos / Pallets");
        lblBultosHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        listBultos = new ListView<>(listaBultos);
        listBultos.setCellFactory(param -> new ListCell<Bulto>() {
            @Override
            protected void updateItem(Bulto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getEtiqueta());
                }
            }
        });
        listBultos.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> seleccionarBulto(newVal));

        Button btnNewPallet = new Button("+ Pallet");
        btnNewPallet.setOnAction(e -> crearBulto("1 PALLET"));

        Button btnNewBulto = new Button("+ Bulto");
        btnNewBulto.setOnAction(e -> crearBulto("1 BULTO"));

        Button btnEliminarBulto = new Button("Eliminar");
        btnEliminarBulto.setStyle("-fx-text-fill: red;");
        btnEliminarBulto.setOnAction(e -> {
            Bulto sel = listBultos.getSelectionModel().getSelectedItem();
            if (sel != null) {
                listaBultos.remove(sel);
                renumerarBultos();
                if (!listaBultos.isEmpty()) {
                    listBultos.getSelectionModel().selectFirst();
                } else {
                    seleccionarBulto(null);
                }
            }
        });

        HBox addButtons = new HBox(5, btnNewPallet, btnNewBulto);
        colBultosLeft.getChildren().addAll(lblBultosHeader, addButtons, listBultos, btnEliminarBulto);
        VBox.setVgrow(listBultos, Priority.ALWAYS);

        // Right Column of FormSplit: Details and Items Table
        boxDerechoForm = new VBox(10);
        boxDerechoForm.setPadding(new Insets(10, 10, 10, 20));
        boxDerechoForm.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 0 0 0 1;");
        boxDerechoForm.setDisable(true);

        Label lblBultoDetail = new Label("Detalle del Bulto Seleccionado");
        lblBultoDetail.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        GridPane gridBulto = new GridPane();
        gridBulto.setHgap(10);
        gridBulto.setVgap(8);

        cmbTipo = new ComboBox<>(FXCollections.observableArrayList("PALLET", "BULTO", "CAJA", "CARGA SUELTA"));
        cmbTipo.setPromptText("Seleccione");

        txtLargo = new TextField();
        txtLargo.setPrefWidth(60);
        txtLargo.setPromptText("0.0");
        txtAncho = new TextField();
        txtAncho.setPrefWidth(60);
        txtAncho.setPromptText("0.0");
        txtAlto = new TextField();
        txtAlto.setPrefWidth(60);
        txtAlto.setPromptText("0.0");
        txtPesoBruto = new TextField();
        txtPesoBruto.setPrefWidth(70);
        txtPesoBruto.setPromptText("0.0");
        txtPesoNeto = new TextField();
        txtPesoNeto.setPrefWidth(70);
        txtPesoNeto.setPromptText("0.0");
        txtObservaciones = new TextField();
        txtObservaciones.setPrefWidth(200);
        txtObservaciones.setPromptText("Detalles / Observaciones");

        configurarValidacionNumerica(txtLargo);
        configurarValidacionNumerica(txtAncho);
        configurarValidacionNumerica(txtAlto);
        configurarValidacionNumerica(txtPesoBruto);
        configurarValidacionNumerica(txtPesoNeto);

        javafx.beans.value.ChangeListener<String> textUpdater = (obs, oldV, newV) -> guardarDetalleBulto();
        cmbTipo.valueProperty().addListener((obs, oldV, newV) -> guardarDetalleBulto());
        txtLargo.textProperty().addListener(textUpdater);
        txtAncho.textProperty().addListener(textUpdater);
        txtAlto.textProperty().addListener(textUpdater);
        txtPesoBruto.textProperty().addListener(textUpdater);
        txtPesoNeto.textProperty().addListener(textUpdater);
        txtObservaciones.textProperty().addListener(textUpdater);

        Button btnReplicar = new Button("Replicar");
        btnReplicar.setOnAction(e -> replicarBultoActual());

        Label lblTipo = new Label("Tipo:");
        lblTipo.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        gridBulto.add(lblTipo, 0, 0);
        gridBulto.add(new HBox(10, cmbTipo, btnReplicar), 1, 0);
        HBox dimsBox = new HBox(5, txtLargo, new Label("x"), txtAncho, new Label("x"), txtAlto, new Label("cm"));
        dimsBox.setAlignment(Pos.CENTER_LEFT);
        Label lblDims = new Label("Dimensiones:");
        lblDims.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        gridBulto.add(lblDims, 0, 1);
        gridBulto.add(dimsBox, 1, 1);

        HBox pesosBox = new HBox(10, new Label("Bruto:"), txtPesoBruto, new Label("Neto:"), txtPesoNeto,
                new Label("kg"));
        pesosBox.setAlignment(Pos.CENTER_LEFT);
        Label lblPesos = new Label("Pesos:");
        lblPesos.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        gridBulto.add(lblPesos, 0, 2);
        gridBulto.add(pesosBox, 1, 2);

        Label lblObs = new Label("Observaciones:");
        lblObs.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        gridBulto.add(lblObs, 0, 3);
        gridBulto.add(txtObservaciones, 1, 3);

        // Table of items in Bulto
        Label lblItemsHeader = new Label("Contenido (Ítems)");
        lblItemsHeader.setStyle("-fx-font-weight: bold;");

        tablaItems = new TableView<>(itemsBultoActual);
        tablaItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PackingItem, String> cPn = new TableColumn<>("SKU");
        cPn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPartNumber()));

        TableColumn<PackingItem, String> cDesc = new TableColumn<>("Descripción");
        cDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));

        TableColumn<PackingItem, Integer> cCant = new TableColumn<>("Cantidad");
        cCant.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getCantidad()));
        cCant.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        cCant.setOnEditCommit(e -> {
            PackingItem item = e.getRowValue();
            int nuevaCant = e.getNewValue();

            int yaAgregado = 0;
            for (Bulto b : listaBultos) {
                List<PackingItem> items = (b == bultoSeleccionado) ? itemsBultoActual : b.getItems();
                for (PackingItem it : items) {
                    if (it.getPartNumber().equalsIgnoreCase(item.getPartNumber())) {
                        if (it == item) {
                            continue;
                        }
                        yaAgregado += it.getCantidad();
                    }
                }
            }

            int nuevoTotal = yaAgregado + nuevaCant;

            com.logistics.packinglist.model.OrderNoteDetailModel detail = null;
            if (selectedNP != null && selectedNP.getDetails() != null) {
                for (com.logistics.packinglist.model.OrderNoteDetailModel d : selectedNP.getDetails()) {
                    String pId = d.getProductId();
                    com.logistics.packinglist.model.ProductModel p = (pId != null) ? productMap.get(pId.toLowerCase().trim()) : null;
                    if (p != null && p.getSku().equalsIgnoreCase(item.getPartNumber())) {
                        detail = d;
                        break;
                    }
                }
            }

            int bo = detail != null && detail.getCantidadBackOrder() != null ? detail.getCantidadBackOrder() : 0;
            int efectiva = detail != null ? detail.getCantidadPedida() - bo : 0;

            if (detail != null && nuevoTotal > efectiva) {
                alerta("Límite superado", "No se puede cambiar la cantidad. " +
                        "El producto " + item.getPartNumber() + " ya tiene " + yaAgregado
                        + " unidades en otros bultos, " +
                        "y el pedido efectivo (sin backorder) de la NP es de máximo " + efectiva + " unidades.");
                tablaItems.refresh();
                return;
            }

            item.setCantidad(nuevaCant);
            guardarDetalleBulto();
        });

        tablaItems.setEditable(true);
        tablaItems.getColumns().addAll(cPn, cDesc, cCant);
        VBox.setVgrow(tablaItems, Priority.ALWAYS);

        Button btnAddProducto = new Button("➕ Añadir Producto");
        btnAddProducto.setOnAction(e -> abrirSeleccionProducto());

        Button btnLector = new Button("🔫 Usar Lector");
        btnLector.setOnAction(e -> abrirLector());

        Button btnEliminarProducto = new Button("Eliminar Seleccionado");
        btnEliminarProducto.setOnAction(e -> {
            PackingItem sel = tablaItems.getSelectionModel().getSelectedItem();
            if (sel != null && bultoSeleccionado != null) {
                bultoSeleccionado.getItems().remove(sel);
                itemsBultoActual.remove(sel);
            }
        });

        HBox itemToolbar = new HBox(10, btnAddProducto, btnLector, btnEliminarProducto);
        boxDerechoForm.getChildren().addAll(lblBultoDetail, gridBulto, new Separator(), lblItemsHeader, itemToolbar,
                tablaItems);

        formSplit.getItems().addAll(colBultosLeft, boxDerechoForm);
        formSplit.setDividerPositions(0.35);
        rightPane.setCenter(formSplit);

        // Footer Actions
        HBox photoHeader = new HBox(10);
        photoHeader.setAlignment(Pos.CENTER_LEFT);
        lblPhotoCount = new Label("Fotos Evidencia (0/6):");
        lblPhotoCount.setStyle("-fx-font-weight: bold; -fx-font-size: 9.5px;");
        Button btnAddPhoto = new Button("Adjuntar Fotos", new FontAwesomeIconView(FontAwesomeIcon.CAMERA));
        btnAddPhoto.setStyle("-fx-background-color: #319795; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnAddPhoto.setOnAction(e -> adjuntarFotos());
        photoHeader.getChildren().addAll(lblPhotoCount, btnAddPhoto);

        photoPreviewPane = new FlowPane();
        photoPreviewPane.setHgap(8);
        photoPreviewPane.setVgap(8);
        photoPreviewPane.setPrefHeight(60);
        photoPreviewPane.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 4; -fx-padding: 5;");
        
        Button btnFinalizar = new Button("Finalizar y Ver Packing");
        btnFinalizar.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;");
        btnFinalizar.setOnAction(e -> compilarYGuardar());

        HBox btnRow = new HBox(btnFinalizar);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        VBox bottomBox = new VBox(5, photoHeader, photoPreviewPane, btnRow);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));
        bottomBox.setStyle("-fx-background-color: white;");
        rightPane.setBottom(bottomBox);

        // Append Left and Right to Main Split
        mainSplit.getItems().addAll(tabPaneLeft, rightPane);
        mainSplit.setDividerPositions(0.40);

        Scene scene = new Scene(mainSplit, 1200, 640);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setScene(scene);
        ScreenUtil.fitDialogToScreen(this, getOwner(), 1200, 640, 950, 540);
    }

    private void cargarNPs() {
        new Thread(() -> {
            try {
                List<com.logistics.packinglist.model.ProductModel> prods = service.obtenerProductos();
                System.out.println("[PackingListDialog] Loaded products: " + (prods != null ? prods.size() : "null"));
                javafx.application.Platform.runLater(() -> {
                    productMap.clear();
                    for (com.logistics.packinglist.model.ProductModel p : prods) {
                        if (p.getId() != null) {
                            productMap.put(p.getId().toLowerCase().trim(), p);
                        }
                    }
                });

                List<OrderNoteModel> nps = service.obtenerNotasPedido();
                System.out.println("[PackingListDialog] Loaded nps size: " + nps.size());
                for (OrderNoteModel np : nps) {
                    System.out.println("[PackingListDialog] NP folio: " + np.getFolio() + ", details size: "
                            + (np.getDetails() != null ? np.getDetails().size() : "null"));
                    if (np.getDetails() != null) {
                        for (OrderNoteDetailModel d : np.getDetails()) {
                            System.out.println("  - Detail: id=" + d.getId() + ", productId=" + d.getProductId()
                                    + ", cantPedida=" + d.getCantidadPedida());
                        }
                    }
                }
                javafx.application.Platform.runLater(() -> {
                    listaNPs.setAll(nps);
                    if (!listaNPs.isEmpty()) {
                        tablaNPs.getSelectionModel().selectFirst();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void cargarPackingLists() {
        new Thread(() -> {
            try {
                List<PackingList> packings = service.obtenerPackingLists();
                javafx.application.Platform.runLater(() -> {
                    listaPackings.setAll(packings);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void abrirPackingList(PackingList pl) {
        // Open it in workspace tab!
        mainController.abrirPackingListTab(pl);
        close();
    }

    private void eliminarPackingList() {
        PackingList sel = tablaPackings.getSelectionModel().getSelectedItem();
        if (sel == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("Eliminar Packing List");
        alert.setContentText("¿Está seguro que desea eliminar permanentemente el packing list para la orden "
                + sel.getNumeroOrden() + "?");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    service.eliminarPackingList(sel.getId());
                    listaPackings.remove(sel);
                    // Also notify main controller if opened tab matches
                    // MainController handles closed tab bindings
                } catch (Exception ex) {
                    alerta("Error al eliminar", ex.getMessage());
                }
            }
        });
    }

    private void crearBulto(String tipo) {
        int numero = listaBultos.size() + 1;
        Bulto b = new Bulto(numero, tipo, 0, 0, 0, 0, 0);
        b.setObservaciones("");
        listaBultos.add(b);
        listBultos.getSelectionModel().select(b);
    }

    private void replicarBultoActual() {
        if (bultoSeleccionado == null)
            return;
        int numero = listaBultos.size() + 1;
        Bulto copia = new Bulto(numero, bultoSeleccionado.getTipo(), bultoSeleccionado.getPesoTotal(),
                bultoSeleccionado.getPesoNeto(), bultoSeleccionado.getLargo(), bultoSeleccionado.getAncho(),
                bultoSeleccionado.getAlto());
        copia.setAlias(bultoSeleccionado.getAlias());
        copia.setObservaciones(bultoSeleccionado.getObservaciones());
        for (PackingItem item : bultoSeleccionado.getItems()) {
            copia.addItem(new PackingItem(item.getPartNumber(), item.getDescripcion(), item.getCantidad()));
        }
        listaBultos.add(copia);
        listBultos.getSelectionModel().select(copia);
    }

    private void seleccionarBulto(Bulto b) {
        this.bultoSeleccionado = b;
        if (b == null) {
            boxDerechoForm.setDisable(true);
            itemsBultoActual.clear();
            return;
        }

        if (selectedNP != null && ("ANULADA".equalsIgnoreCase(selectedNP.getEstado()) || "ANULADO".equalsIgnoreCase(selectedNP.getEstado()))) {
            boxDerechoForm.setDisable(true);
            return;
        }

        boxDerechoForm.setDisable(false);
        bloqueandoUpdates = true;

        cmbTipo.setValue(b.getTipo().replaceAll("^\\d+\\s*", ""));
        txtLargo.setText(formatDouble(b.getLargo()));
        txtAncho.setText(formatDouble(b.getAncho()));
        txtAlto.setText(formatDouble(b.getAlto()));
        txtPesoBruto.setText(formatDouble(b.getPesoTotal()));
        txtPesoNeto.setText(formatDouble(b.getPesoNeto()));
        txtObservaciones.setText(b.getObservaciones() != null ? b.getObservaciones() : "");

        itemsBultoActual.setAll(b.getItems());

        bloqueandoUpdates = false;
    }

    private void guardarDetalleBulto() {
        if (bloqueandoUpdates || bultoSeleccionado == null)
            return;

        try {
            double pb = txtPesoBruto.getText().isEmpty() ? 0 : Double.parseDouble(txtPesoBruto.getText().trim());
            double pn = txtPesoNeto.getText().isEmpty() ? 0 : Double.parseDouble(txtPesoNeto.getText().trim());
            double l = txtLargo.getText().isEmpty() ? 0 : Double.parseDouble(txtLargo.getText().trim());
            double an = txtAncho.getText().isEmpty() ? 0 : Double.parseDouble(txtAncho.getText().trim());
            double al = txtAlto.getText().isEmpty() ? 0 : Double.parseDouble(txtAlto.getText().trim());

            String tipoLimpio = cmbTipo.getValue() != null ? cmbTipo.getValue().toUpperCase() : "BULTO";

            Bulto actualizado = new Bulto(bultoSeleccionado.getNumero(), tipoLimpio, pb, pn, l, an, al);
            actualizado.getItems().addAll(bultoSeleccionado.getItems());
            actualizado.setAlias(bultoSeleccionado.getAlias());
            actualizado.setObservaciones(txtObservaciones.getText());

            int index = listaBultos.indexOf(bultoSeleccionado);
            if (index >= 0) {
                bloqueandoUpdates = true;
                listaBultos.set(index, actualizado);
                this.bultoSeleccionado = actualizado;
                listBultos.getSelectionModel().select(actualizado);
                bloqueandoUpdates = false;
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void renumerarBultos() {
        for (int i = 0; i < listaBultos.size(); i++) {
            Bulto b = listaBultos.get(i);
            int nuevoNum = i + 1;
            if (b.getNumero() != nuevoNum) {
                Bulto rb = new Bulto(nuevoNum, b.getTipo(), b.getPesoTotal(), b.getPesoNeto(), b.getLargo(),
                        b.getAncho(), b.getAlto());
                rb.setAlias(b.getAlias());
                rb.getItems().addAll(b.getItems());
                listaBultos.set(i, rb);
            }
        }
    }

    private void abrirSeleccionProducto() {
        if (bultoSeleccionado == null)
            return;

        SeleccionProductoDialog dialog = new SeleccionProductoDialog(this, inventarioService);
        dialog.showAndWait();

        PackingItem pi = dialog.getResultado();
        if (pi != null) {
            agregarOIncrementarProducto(pi);
        }
    }

    private void abrirLector() {
        if (bultoSeleccionado == null)
            return;
        LectorSKUDialog dialog = new LectorSKUDialog(this, inventarioService, itemInv -> {
            PackingItem pi = new PackingItem(itemInv.getPartNumber(), itemInv.getDescripcion(), 1);
            agregarOIncrementarProducto(pi);
        });
        dialog.show();
    }

    private void actualizarInventarioLocalConNP(OrderNoteModel np) {
        inventarioService.getItems().clear();
        System.out.println(
                "[PackingListDialog] actualizarInventarioLocalConNP: np=" + (np != null ? np.getFolio() : "null")
                        + ", details=" + (np != null && np.getDetails() != null ? np.getDetails().size() : "null")
                        + ", productMap size=" + productMap.size());
        if (np == null || np.getDetails() == null)
            return;
        for (com.logistics.packinglist.model.OrderNoteDetailModel detail : np.getDetails()) {
            String pId = detail.getProductId();
            com.logistics.packinglist.model.ProductModel p = (pId != null) ? productMap.get(pId.toLowerCase().trim()) : null;
            System.out.println("[PackingListDialog] lookup product ID: " + pId + " -> found: "
                    + (p != null ? p.getSku() : "null"));
            if (p != null) {
                int bo = detail.getCantidadBackOrder() != null ? detail.getCantidadBackOrder() : 0;
                int efectiva = detail.getCantidadPedida() - bo;
                String desc = p.getNombre() + " (Pedido: " + efectiva + ")";
                inventarioService.getItems()
                        .add(new com.logistics.packinglist.model.InventarioItem(p.getSku(), desc, "NP"));
            }
        }
    }

    private void agregarOIncrementarProducto(PackingItem pi) {
        if (selectedNP == null) {
            alerta("Error", "Debe seleccionar una Nota de Pedido (NP).");
            return;
        }

        com.logistics.packinglist.model.OrderNoteDetailModel detail = null;
        if (selectedNP.getDetails() != null) {
            for (com.logistics.packinglist.model.OrderNoteDetailModel d : selectedNP.getDetails()) {
                String pId = d.getProductId();
                com.logistics.packinglist.model.ProductModel p = (pId != null) ? productMap.get(pId.toLowerCase().trim()) : null;
                if (p != null && p.getSku().equalsIgnoreCase(pi.getPartNumber())) {
                    detail = d;
                    break;
                }
            }
        }

        if (detail == null) {
            alerta("Producto no en NP",
                    "El producto con SKU " + pi.getPartNumber() + " no pertenece a la Nota de Pedido seleccionada.");
            return;
        }

        int yaAgregado = 0;
        for (Bulto b : listaBultos) {
            List<PackingItem> items = (b == bultoSeleccionado) ? itemsBultoActual : b.getItems();
            for (PackingItem item : items) {
                if (item.getPartNumber().equalsIgnoreCase(pi.getPartNumber())) {
                    yaAgregado += item.getCantidad();
                }
            }
        }

        int bo = detail.getCantidadBackOrder() != null ? detail.getCantidadBackOrder() : 0;
        int efectiva = detail.getCantidadPedida() - bo;

        int nuevoTotal = yaAgregado + pi.getCantidad();
        if (nuevoTotal > efectiva) {
            alerta("Límite superado", "No se puede añadir. " +
                    "El producto " + pi.getPartNumber() + " ya tiene " + yaAgregado + " unidades en el packing, " +
                    "y el pedido efectivo (sin backorder) de la NP es de máximo " + efectiva + " unidades.");
            return;
        }

        boolean existia = false;
        for (PackingItem item : itemsBultoActual) {
            if (item.getPartNumber().equals(pi.getPartNumber())) {
                item.setCantidad(item.getCantidad() + pi.getCantidad());
                existia = true;
                break;
            }
        }
        if (!existia) {
            itemsBultoActual.add(pi);
        }
        bultoSeleccionado.getItems().clear();
        bultoSeleccionado.getItems().addAll(itemsBultoActual);
        guardarDetalleBulto();
        tablaItems.refresh();
    }

    private void compilarYGuardar() {
        if (selectedNP != null && selectedNP.getNpModificada() != null && selectedNP.getNpModificada()) {
            alerta("Cambios Sin Revisar", 
                "La Nota de Pedido fue modificada posteriormente al Picking.\n\nSe recomienda revisar y actualizar el Picking antes de continuar.");
            return;
        }

        if (listaBultos.isEmpty()) {
            alerta("Error", "Debe agregar al menos un bulto.");
            return;
        }

        String orden = txtOrden.getText().trim();
        if (orden.isEmpty()) {
            alerta("Error", "Debe seleccionar una Nota de Pedido (NP) del listado izquierdo.");
            return;
        }

        // 1. Validar dimensiones y peso por tipo de bulto (RN-PP-007)
        for (Bulto b : listaBultos) {
            String tipo = b.getTipo() != null ? b.getTipo().toUpperCase() : "";
            if ("PALLET".equals(tipo) || "BULTO".equals(tipo) || "CAJA".equals(tipo)) {
                if (b.getLargo() <= 0 || b.getAncho() <= 0 || b.getAlto() <= 0 || b.getPesoTotal() <= 0) {
                    alerta("Error de Validación", "Para Pallet, Caja y Bulto, las dimensiones (largo, ancho, alto) and el peso son obligatorios y deben ser mayores a 0. Revise el bulto N° " + b.getNumero());
                    return;
                }
            }
        }

        // 2. Validar Picking vs Packing (RN-PP-010, RN-PP-011)
        if (selectedNP != null) {
            java.util.Map<String, Integer> preparedQtyMap = new java.util.HashMap<>();
            for (Bulto b : listaBultos) {
                for (PackingItem item : b.getItems()) {
                    String sku = item.getPartNumber() != null ? item.getPartNumber().trim().toUpperCase() : "";
                    preparedQtyMap.put(sku, preparedQtyMap.getOrDefault(sku, 0) + item.getCantidad());
                }
            }

            java.util.Map<String, Integer> expectedQtyMap = new java.util.HashMap<>();
            for (OrderNoteDetailModel d : selectedNP.getDetails()) {
                String pId = d.getProductId() != null ? d.getProductId().toLowerCase().trim() : "";
                com.logistics.packinglist.model.ProductModel p = productMap.get(pId);
                String sku = p != null && p.getSku() != null ? p.getSku().trim().toUpperCase() : "";
                if (!sku.isEmpty()) {
                    int bo = d.getCantidadBackOrder() != null ? d.getCantidadBackOrder() : 0;
                    int efectiva = d.getCantidadPedida() - bo;
                    expectedQtyMap.put(sku, expectedQtyMap.getOrDefault(sku, 0) + efectiva);
                }
            }

            java.util.List<String> diffs = new java.util.ArrayList<>();
            for (String sku : expectedQtyMap.keySet()) {
                int expected = expectedQtyMap.get(sku);
                int prepared = preparedQtyMap.getOrDefault(sku, 0);
                if (expected != prepared) {
                    diffs.add("- SKU " + sku + ": Pedido: " + expected + ", Preparado: " + prepared);
                }
            }
            for (String sku : preparedQtyMap.keySet()) {
                if (!expectedQtyMap.containsKey(sku)) {
                    int prepared = preparedQtyMap.get(sku);
                    diffs.add("- SKU " + sku + ": Extra preparado: " + prepared);
                }
            }

            if (!diffs.isEmpty()) {
                alerta("Inconsistencias Detectadas", 
                    "No se puede finalizar el Packing porque no coincide con la Nota de Pedido:\n\n" 
                    + String.join("\n", diffs));
                return;
            }
        }

        PackingList pl = new PackingList();
        String usuarioActual = System.getProperty("user.name", "Usuario");
        String fechaHoy = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        pl.setFechaCreacion(fechaHoy);
        pl.setUsuarioCreacion(usuarioActual);
        pl.setFechaEdicion(fechaHoy);
        pl.setUsuarioEdicion(usuarioActual);
        pl.setNumeroOrden(orden);
        pl.setFecha(fechaHoy);
        pl.setNombreArchivo("Packing_" + orden + "_" + System.currentTimeMillis());
        pl.setFotosUrls(uploadedPhotoUrls.isEmpty() ? "" : String.join(",", uploadedPhotoUrls));

        int totalA = 0;
        double tp = 0;
        double tn = 0;

        for (Bulto b : listaBultos) {
            pl.addBulto(b);
            totalA += b.getTotalUnidades();
            tp += b.getPesoTotal();
            tn += b.getPesoNeto();
        }

        pl.setTotalPiezas(totalA);
        pl.setPesoTotalKg(tp);
        pl.setPesoNetoKg(tn);

        if (selectedNP != null) {
            pl.setWarehouseId(selectedNP.getWarehouseId());
        }

        try {
            PackingList saved = service.crearPackingList(pl);
            mainController.abrirPackingListTab(saved);

            alerta("Picking & Packing Finalizado", "Picking & Packing finalizado correctamente. El pedido se encuentra preparado para despacho.");

            // Reset right panel state
            listaBultos.clear();
            seleccionarBulto(null);
            txtOrden.clear();
            tablaNPs.getSelectionModel().clearSelection();
            uploadedPhotoUrls.clear();
            refrescarFotos();

            cargarPackingLists();
        } catch (Exception ex) {
            alerta("Error al guardar Packing List", ex.getMessage());
        }
    }

    private void adjuntarFotos() {
        String trk = selectedNP != null ? selectedNP.getTrackingNumber() : null;
        if (trk == null || trk.isEmpty()) {
            alerta("Fotos de Evidencia", "Seleccione una Nota de Pedido antes de adjuntar fotos.");
            return;
        }

        if (uploadedPhotoUrls.size() >= 6) {
            alerta("Límite Alcanzado", "Se ha alcanzado el límite máximo de 6 fotos.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar Fotos de Evidencia");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes (*.jpg, *.png)", "*.jpg", "*.jpeg", "*.png"));
        List<File> files = chooser.showOpenMultipleDialog(this);

        if (files != null && !files.isEmpty()) {
            new Thread(() -> {
                for (File file : files) {
                    if (uploadedPhotoUrls.size() >= 6) break;
                    try {
                        String url = service.subirFotoTracking(trk, file);
                        javafx.application.Platform.runLater(() -> {
                            uploadedPhotoUrls.add(url);
                            refrescarFotos();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        javafx.application.Platform.runLater(() -> alerta("Error de Carga", "No se pudo subir la foto: " + e.getMessage()));
                    }
                }
            }).start();
        }
    }

    private void refrescarFotos() {
        photoPreviewPane.getChildren().clear();
        lblPhotoCount.setText("Fotos Evidencia (" + uploadedPhotoUrls.size() + "/6):");

        for (String url : uploadedPhotoUrls) {
            HBox thumb = new HBox(4);
            thumb.setAlignment(Pos.CENTER_LEFT);
            thumb.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e0; -fx-padding: 3; -fx-border-radius: 4;");

            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
            imgView.setFitWidth(40);
            imgView.setFitHeight(40);
            imgView.setPreserveRatio(true);
            try {
                imgView.setImage(new javafx.scene.image.Image(url, true));
            } catch (Exception ignored) {}

            Button btnDel = new Button("×");
            btnDel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-padding: 0 4; -fx-background-color: transparent; -fx-cursor: hand;");
            btnDel.setOnAction(e -> {
                uploadedPhotoUrls.remove(url);
                refrescarFotos();
            });

            thumb.getChildren().addAll(imgView, btnDel);
            photoPreviewPane.getChildren().add(thumb);
        }
    }

    private void descargarHojaPicking() {
        OrderNoteModel selectedNote = tablaNPs.getSelectionModel().getSelectedItem();
        if (selectedNote == null) {
            alerta("Hoja de Picking PDF", "Debe seleccionar una Nota de Pedido de la lista.");
            return;
        }

        String pickingState = selectedNote.getEstadoPicking();
        if (pickingState == null || "NO_INICIADO".equalsIgnoreCase(pickingState)) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Iniciar proceso de Picking");
            confirm.setHeaderText(null);
            confirm.setContentText("¿Desea comenzar el proceso de Picking?");
            
            ButtonType btnAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(btnAceptar, btnCancelar);
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != btnAceptar) {
                return;
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Hoja de Picking PDF");
        fileChooser.setInitialFileName("Picking_" + selectedNote.getFolio() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));

        File file = fileChooser.showSaveDialog(this);
        if (file != null) {
            new Thread(() -> {
                try {
                    // 1. Registrar impresión en backend
                    service.registrarImpresionPicking(selectedNote.getId());

                    // 2. Cargar metadatos para PDF
                    List<CompanyModel> companies = service.obtenerEmpresas();
                    CompanyModel activeCompany = null;
                    if (companies != null && !companies.isEmpty()) {
                        activeCompany = companies.get(0);
                        if (com.logistics.packinglist.service.AuthService.getInstance().getCompanyId() != null) {
                            String activeCompId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
                            for (CompanyModel c : companies) {
                                if (c.getId().equals(activeCompId)) {
                                    activeCompany = c;
                                    break;
                                }
                            }
                        }
                    }

                    List<CustomerModel> clientes = service.obtenerClientes();
                    java.util.Map<String, CustomerModel> customerMap = new java.util.HashMap<>();
                    for (CustomerModel c : clientes) {
                        customerMap.put(c.getId(), c);
                    }

                    String provId = selectedNote.getAtributosPersonalizados() != null ? selectedNote.getAtributosPersonalizados().get("proveedor_id") : null;
                    SupplierModel supplier = null;
                    if (provId != null) {
                        List<SupplierModel> proveedores = service.obtenerProveedores();
                        for (SupplierModel s : proveedores) {
                            if (s.getId().equals(provId)) {
                                supplier = s;
                                break;
                            }
                        }
                    }

                    List<LocationModel> ubicaciones = service.obtenerUbicaciones();
                    java.util.Map<String, LocationModel> locationMap = new java.util.HashMap<>();
                    for (LocationModel l : ubicaciones) {
                        locationMap.put(l.getId(), l);
                    }

                    List<StockModel> rawStockList = service.obtenerStocks();
                    List<ReceptionModel> recepciones = service.obtenerRecepciones();

                    // 3. Generar PDF
                    PdfNotaPedidoService pdfService = new PdfNotaPedidoService();
                    pdfService.exportarHojaPickingWMS(
                        selectedNote, 
                        activeCompany, 
                        customerMap.get(selectedNote.getCustomerId()), 
                        supplier, 
                        productMap, 
                        rawStockList, 
                        locationMap, 
                        recepciones, 
                        file
                    );

                    // 4. Refrescar datos
                    cargarNPs();

                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Picking PDF Creado");
                        alert.setHeaderText(null);
                        alert.setContentText("La Hoja de Picking WMS se ha exportado y registrado con éxito.");
                        alert.showAndWait();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> alerta("Error", "No se pudo generar la Hoja de Picking PDF: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void imprimirHojaPicking() {
        OrderNoteModel selectedNote = tablaNPs.getSelectionModel().getSelectedItem();
        if (selectedNote == null) {
            alerta("Imprimir Hoja de Picking", "Debe seleccionar una Nota de Pedido de la lista.");
            return;
        }

        String pickingState = selectedNote.getEstadoPicking();
        if (pickingState == null || "NO_INICIADO".equalsIgnoreCase(pickingState)) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Iniciar proceso de Picking");
            confirm.setHeaderText(null);
            confirm.setContentText("¿Desea comenzar el proceso de Picking?");
            
            ButtonType btnAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(btnAceptar, btnCancelar);
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != btnAceptar) {
                return;
            }
        }

        new Thread(() -> {
            try {
                File tempFile = File.createTempFile("Picking_" + selectedNote.getFolio() + "_", ".pdf");
                tempFile.deleteOnExit();
                
                // 1. Registrar impresión en backend
                service.registrarImpresionPicking(selectedNote.getId());

                // 2. Cargar metadatos para PDF
                List<CompanyModel> companies = service.obtenerEmpresas();
                CompanyModel activeCompany = null;
                if (companies != null && !companies.isEmpty()) {
                    activeCompany = companies.get(0);
                    if (com.logistics.packinglist.service.AuthService.getInstance().getCompanyId() != null) {
                        String activeCompId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
                        for (CompanyModel c : companies) {
                            if (c.getId().equals(activeCompId)) {
                                activeCompany = c;
                                break;
                            }
                        }
                    }
                }

                List<CustomerModel> clientes = service.obtenerClientes();
                java.util.Map<String, CustomerModel> customerMap = new java.util.HashMap<>();
                for (CustomerModel c : clientes) {
                    customerMap.put(c.getId(), c);
                }

                String provId = selectedNote.getAtributosPersonalizados() != null ? selectedNote.getAtributosPersonalizados().get("proveedor_id") : null;
                SupplierModel supplier = null;
                if (provId != null) {
                    List<SupplierModel> proveedores = service.obtenerProveedores();
                    for (SupplierModel s : proveedores) {
                        if (s.getId().equals(provId)) {
                            supplier = s;
                            break;
                        }
                    }
                }

                List<LocationModel> ubicaciones = service.obtenerUbicaciones();
                java.util.Map<String, LocationModel> locationMap = new java.util.HashMap<>();
                for (LocationModel l : ubicaciones) {
                    locationMap.put(l.getId(), l);
                }

                List<StockModel> rawStockList = service.obtenerStocks();
                List<ReceptionModel> recepciones = service.obtenerRecepciones();

                // 3. Generar PDF
                PdfNotaPedidoService pdfService = new PdfNotaPedidoService();
                pdfService.exportarHojaPickingWMS(
                    selectedNote, 
                    activeCompany, 
                    customerMap.get(selectedNote.getCustomerId()), 
                    supplier, 
                    productMap, 
                    rawStockList, 
                    locationMap, 
                    recepciones, 
                    tempFile
                );

                // 4. Refrescar datos en UI
                javafx.application.Platform.runLater(() -> cargarNPs());

                // 5. Abrir PDF en el visor del sistema (lo que abre el diálogo de impresión nativamente)
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                    java.awt.Desktop.getDesktop().open(tempFile);
                } else if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.PRINT)) {
                    java.awt.Desktop.getDesktop().print(tempFile);
                }

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> alerta("Error", "No se pudo preparar la Hoja de Picking PDF para impresión: " + e.getMessage()));
            }
        }).start();
    }

    private void configurarValidacionNumerica(TextField tf) {
        tf.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.matches("\\d*(\\.\\d*)?")) {
                tf.setText(newV.replaceAll("[^\\d\\.]", ""));
            }
        });
    }

    private String formatDouble(double d) {
        if (d == 0)
            return "";
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.valueOf(d);
    }

    private void alerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
