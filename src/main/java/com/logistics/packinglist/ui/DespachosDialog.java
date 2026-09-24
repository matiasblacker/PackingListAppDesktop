package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.DispatchModel;
import com.logistics.packinglist.model.DispatchDetailModel;
import com.logistics.packinglist.model.OrderNoteModel;
import com.logistics.packinglist.model.OrderNoteDetailModel;
import com.logistics.packinglist.model.ProductModel;
import com.logistics.packinglist.service.MantenimientoService;
import com.logistics.packinglist.service.WebSocketManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DespachosDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

            private final ObservableList<DispatchModel> observableList;
    private final FilteredList<DispatchModel> filteredList;
    private final ObservableList<OrderNoteDetailModel> itemsToDispatchList;
    private final javafx.collections.ObservableList<OrderNoteModel> npConfirmadasList;

    private TableView<DispatchModel> tablaDespachos;
    private TableView<OrderNoteModel> tablaNPsConfirmadas;
    private OrderNoteModel selectedNP = null;
    private TableView<OrderNoteDetailModel> tablaItems;

    // Campos formulario despacho
    private Label lblNPSelected;
    private TextField txtGuia;
    private TextField txtFactura;
    private TextField txtTransportista;
    private TextField txtPatente;
    private TextArea txtComentario;
    private Button btnGuardar;

    private DispatchModel selectedDispatch = null;
    private final Map<String, OrderNoteModel> noteMap = new HashMap<>();
    private final Map<String, ProductModel> productMap = new HashMap<>();

    public DespachosDialog(Window owner) {
                        this.observableList = FXCollections.observableArrayList();
                        this.filteredList = new FilteredList<>(observableList, d -> true);
        this.itemsToDispatchList = FXCollections.observableArrayList();
        this.npConfirmadasList = FXCollections.observableArrayList();

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Gestión de Despachos (WMS)");

        setMinWidth(950);
        setMinHeight(520);

        construirUI();
        cargarDatos();

        WebSocketManager.UpdateListener dispatchListener = action -> {
            System.out.println("[DespachosDialog] Received real-time update event: " + action);
            cargarDatos();
        };
        WebSocketManager.getInstance().subscribe("DISPATCH", dispatchListener);
        WebSocketManager.getInstance().subscribe("ORDER_NOTE", dispatchListener);
        WebSocketManager.getInstance().subscribe("STOCK", dispatchListener);
        WebSocketManager.getInstance().subscribe("LOCATION", dispatchListener);
        WebSocketManager.getInstance().subscribe("CUSTOMER", dispatchListener);
        WebSocketManager.getInstance().subscribe("CARRIER", dispatchListener);
        WebSocketManager.getInstance().subscribe("WAREHOUSE", dispatchListener);

        setOnHiding(e -> {
            WebSocketManager.getInstance().unsubscribe("DISPATCH", dispatchListener);
            WebSocketManager.getInstance().unsubscribe("ORDER_NOTE", dispatchListener);
            WebSocketManager.getInstance().unsubscribe("STOCK", dispatchListener);
            WebSocketManager.getInstance().unsubscribe("LOCATION", dispatchListener);
            WebSocketManager.getInstance().unsubscribe("CUSTOMER", dispatchListener);
            WebSocketManager.getInstance().unsubscribe("CARRIER", dispatchListener);
            WebSocketManager.getInstance().unsubscribe("WAREHOUSE", dispatchListener);
        });
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header Box
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label("Gestión de Despachos de Carga");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Emisión de guías de despacho, asignación de transportistas y control de entregas.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // --- LADO IZQUIERDO: LISTADO (WHITE CARD) ---
        VBox leftPane = new VBox(10);
        leftPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        Label lblLeftTitle = new Label("Despachos Realizados");
        lblLeftTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        // Sin selector de estado. Todos los despachos son definitivos.

        
        TabPane leftTabPane = new TabPane();
        leftTabPane.setStyle("-fx-background-color: transparent;");

        // Tab 1: NPs Confirmadas
        Tab tabNPs = new Tab("NPs por Despachar");
        tabNPs.setClosable(false);
        VBox boxNPs = new VBox(10);
        boxNPs.setPadding(new Insets(10,0,0,0));
        
        tablaNPsConfirmadas = new TableView<>(npConfirmadasList);
        tablaNPsConfirmadas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaNPsConfirmadas.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        
        TableColumn<OrderNoteModel, String> colNpFolio = new TableColumn<>("Folio");
        colNpFolio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolio()));
        
        TableColumn<OrderNoteModel, String> colNpFecha = new TableColumn<>("Fecha");
        colNpFecha.setCellValueFactory(c -> new SimpleStringProperty(DateFormatterUtil.format(c.getValue().getFecha())));
        
        tablaNPsConfirmadas.getColumns().addAll(colNpFolio, colNpFecha);
        VBox.setVgrow(tablaNPsConfirmadas, Priority.ALWAYS);
        boxNPs.getChildren().add(tablaNPsConfirmadas);
        tabNPs.setContent(boxNPs);

        // Tab 2: Historial de Despachos
        Tab tabHistorial = new Tab("Historial Despachos");
        tabHistorial.setClosable(false);
        VBox boxHistorial = new VBox(10);
        boxHistorial.setPadding(new Insets(10,0,0,0));

        tablaDespachos = new TableView<>(filteredList);
        tablaDespachos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaDespachos.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<DispatchModel, String> colGuia = new TableColumn<>("Nº Guía");
        colGuia.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGuiaDespacho()));
        colGuia.setPrefWidth(90);
        
        TableColumn<DispatchModel, String> colFactura = new TableColumn<>("Factura");
        colFactura.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFactura()));
        colFactura.setPrefWidth(90);

        TableColumn<DispatchModel, String> colTrans = new TableColumn<>("Transportista");
        colTrans.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransportista()));
        colTrans.setPrefWidth(140);

        TableColumn<DispatchModel, String> colFolioNP = new TableColumn<>("Folio NP");
        colFolioNP.setCellValueFactory(c -> {
            OrderNoteModel np = noteMap.get(c.getValue().getOrderNoteId());
            return new SimpleStringProperty(np != null ? np.getFolio() : "-");
        });
        colFolioNP.setPrefWidth(90);

        TableColumn<DispatchModel, String> colEstadoNP = new TableColumn<>("Estado NP");
        colEstadoNP.setCellValueFactory(c -> {
            OrderNoteModel np = noteMap.get(c.getValue().getOrderNoteId());
            return new SimpleStringProperty(np != null ? np.getEstado() : "-");
        });
        colEstadoNP.setPrefWidth(90);
        StatusColumnHelper.applyStatusStyling(colEstadoNP, false);

        TableColumn<DispatchModel, String> colFechaNP = new TableColumn<>("Fecha NP");
        colFechaNP.setCellValueFactory(c -> {
            OrderNoteModel np = noteMap.get(c.getValue().getOrderNoteId());
            return new SimpleStringProperty(np != null ? np.getFecha() : "-");
        });
        colFechaNP.setPrefWidth(90);

        TableColumn<DispatchModel, String> colEstado = new TableColumn<>("Estado Despacho");
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getEstado() != null && !c.getValue().getEstado().isEmpty() ? c.getValue().getEstado() : "-"
        ));
        colEstado.setPrefWidth(110);
        StatusColumnHelper.applyStatusStyling(colEstado, false);

        // Apply visual styling policy
        StatusColumnHelper.applyStatusStyling(colGuia, true);
        StatusColumnHelper.applyRowFactory(tablaDespachos);

        tablaDespachos.getColumns().addAll(colFolioNP, colEstadoNP, colFechaNP, colEstado, colGuia, colFactura, colTrans);
        VBox.setVgrow(tablaDespachos, Priority.ALWAYS);

        
        boxHistorial.getChildren().add(tablaDespachos);
        tabHistorial.setContent(boxHistorial);
        leftTabPane.getTabs().addAll(tabNPs, tabHistorial);
        leftPane.getChildren().addAll(lblLeftTitle, leftTabPane);


        // --- LADO DERECHO: FORMULARIO (WHITE CARD EN SCROLLPANE) ---
        VBox rightPane = new VBox(12);
        rightPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(8);

        lblNPSelected = new Label("Seleccione una NP de la izquierda");
        lblNPSelected.setStyle("-fx-font-weight: bold; -fx-text-fill: #2563eb;");


        txtGuia = new TextField();
        txtGuia.setPromptText("Nº de Guía de Despacho (Opcional si hay Factura)");
        txtGuia.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtGuia.setMaxWidth(Double.MAX_VALUE);

        txtFactura = new TextField();
        txtFactura.setPromptText("Nº de Factura (Opcional si hay Guía)");
        txtFactura.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtFactura.setMaxWidth(Double.MAX_VALUE);

        txtTransportista = new TextField();
        txtTransportista.setPromptText("Nombre del chofer / Courier");
        txtTransportista.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtTransportista.setMaxWidth(Double.MAX_VALUE);

        txtPatente = new TextField();
        txtPatente.setPromptText("Ej: ABCD-12 o AB-1234");
        txtPatente.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtPatente.setMaxWidth(Double.MAX_VALUE);

        txtComentario = new TextArea();
        txtComentario.setPromptText("Comentarios adicionales sobre el despacho...");
        txtComentario.setPrefRowCount(2);
        txtComentario.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtComentario.setMaxWidth(Double.MAX_VALUE);

        Label lblNP = new Label("Nota Pedido:");
        lblNP.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblGuia = new Label("Guía Despacho:");
        lblGuia.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblFactura = new Label("Factura:");
        lblFactura.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblTrans = new Label("Chofer / Transp:");
        lblTrans.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblPat = new Label("Patente:");
        lblPat.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblComentario = new Label("Comentario:");
        lblComentario.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");

        formGrid.add(lblNP, 0, 0);
        formGrid.add(lblNPSelected, 1, 0);
        formGrid.add(lblTrans, 2, 0);
        formGrid.add(txtTransportista, 3, 0);

        formGrid.add(lblGuia, 0, 1);
        formGrid.add(txtGuia, 1, 1);
        formGrid.add(lblFactura, 2, 1);
        formGrid.add(txtFactura, 3, 1);

        formGrid.add(lblPat, 0, 2);
        formGrid.add(txtPatente, 1, 2);
        formGrid.add(lblComentario, 2, 2);
        formGrid.add(txtComentario, 3, 2);

        ColumnConstraints c1 = new ColumnConstraints(100);
        ColumnConstraints c2 = new ColumnConstraints(220);
        ColumnConstraints c3 = new ColumnConstraints(100);
        ColumnConstraints c4 = new ColumnConstraints(220);
        formGrid.getColumnConstraints().addAll(c1, c2, c3, c4);

        // Sección Detalle
        VBox detailBox = new VBox(8);
        detailBox.setPadding(new Insets(10));
        detailBox.setStyle("-fx-border-color: #cbd5e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-color: white;");
        VBox.setVgrow(detailBox, Priority.ALWAYS);

        tablaItems = new TableView<>(itemsToDispatchList);
        tablaItems.setEditable(true);
        tablaItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaItems.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<OrderNoteDetailModel, String> colItemProd = new TableColumn<>("Producto");
        colItemProd.setCellValueFactory(c -> {
            ProductModel p = productMap.get(c.getValue().getProductId());
            return new SimpleStringProperty(p != null ? p.getSku() + " - " + p.getNombre() : "-");
        });
        colItemProd.setPrefWidth(250);

        TableColumn<OrderNoteDetailModel, String> colItemPed = new TableColumn<>("Cant. a Despachar");
        colItemPed.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantidadDespachada())));
        colItemPed.setPrefWidth(160);

        tablaItems.getColumns().addAll(colItemProd, colItemPed);
        VBox.setVgrow(tablaItems, Priority.ALWAYS);

        Label lblHelp = new Label("Indica la cantidad a despachar en cada fila (Doble Clic para editar):");
        lblHelp.setStyle("-fx-text-fill: #64748b; -fx-font-size: 9.5px;");

        detailBox.getChildren().addAll(lblHelp, tablaItems);

        // Botones guardar/limpiar
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSave.setFill(Color.WHITE);
        iconSave.setSize("16");
        btnGuardar = new Button(null, iconSave);
        btnGuardar.setStyle("-fx-background-color: #0F3E6E; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnGuardar.setOnAction(e -> guardarDespacho());
        Tooltip.install(btnGuardar, new Tooltip("Confirmar Despacho"));

        FontAwesomeIconView iconRefresh = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        iconRefresh.setFill(Color.WHITE);
        iconRefresh.setSize("16");
        Button btnLimpiar = new Button(null, iconRefresh);
        btnLimpiar.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnLimpiar.setOnAction(e -> limpiarFormulario());
        Tooltip.install(btnLimpiar, new Tooltip("Limpiar Formulario"));

        actionRow.getChildren().addAll(btnGuardar, btnLimpiar);

        Label lblFormHeader = new Label("Datos del Despacho");
        lblFormHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblItemsHeader = new Label("Items a Despachar");
        lblItemsHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        rightPane.getChildren().addAll(lblFormHeader, formGrid, lblItemsHeader, detailBox);

        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(15, 0, 0, 0));

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

        root.getChildren().addAll(headerBox, mainSplit, actionRow);

        
        tablaNPsConfirmadas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedDispatch = null;
                selectedNP = newSel;
                tablaDespachos.getSelectionModel().clearSelection();
                lblNPSelected.setText(newSel.getFolio());
                txtGuia.clear();
                txtFactura.clear();
                txtTransportista.clear();
                txtPatente.clear();
                txtComentario.clear();
                
                itemsToDispatchList.clear();
                if (newSel.getDetails() != null) {
                    for (OrderNoteDetailModel ond : newSel.getDetails()) {
                        int bo = ond.getCantidadBackOrder() != null ? ond.getCantidadBackOrder() : 0;
                        int efectiva = ond.getCantidadPedida() - bo;
                        int pendiente = efectiva - (ond.getCantidadDespachada() != null ? ond.getCantidadDespachada() : 0);
                        if (pendiente > 0) {
                            itemsToDispatchList.add(OrderNoteDetailModel.builder()
                                    .productId(ond.getProductId())
                                    .cantidadDespachada(pendiente) // Use this field for what we WILL dispatch
                                    .precioUnitario(ond.getPrecioUnitario())
                                    .build());
                        }
                    }
                }
                toggleEditingFields(true);
            }
        });

        // Listeners selección de tabla despacho

        tablaDespachos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tablaNPsConfirmadas.getSelectionModel().clearSelection();
                selectedDispatch = newSel;
                
                OrderNoteModel note = noteMap.get(newSel.getOrderNoteId());
                selectedNP = note;
                lblNPSelected.setText(note != null ? note.getFolio() : "-");

                txtGuia.setText(newSel.getGuiaDespacho() != null ? newSel.getGuiaDespacho() : "");
                txtFactura.setText(newSel.getFactura() != null ? newSel.getFactura() : "");
                txtTransportista.setText(newSel.getTransportista() != null ? newSel.getTransportista() : "");
                txtPatente.setText(newSel.getPatente() != null ? newSel.getPatente() : "");
                txtComentario.setText(newSel.getComentario() != null ? newSel.getComentario() : "");

                // Cargar items del despacho
                itemsToDispatchList.clear();
                if (newSel.getDetails() != null) {
                    for (DispatchDetailModel dd : newSel.getDetails()) {
                        itemsToDispatchList.add(OrderNoteDetailModel.builder()
                                .productId(dd.getProductId())
                                .cantidadPedida(0) // No aplica directamente en esta vista
                                .cantidadDespachada(dd.getCantidadDespachada())
                                .precioUnitario(0.0)
                                .build());
                    }
                }

                // Despachos son inmutables una vez creados, bloqueamos la edición
                toggleEditingFields(false);
            } else {
                toggleEditingFields(true);
            }
        });

        Scene scene = new Scene(root, 1080, 620);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setScene(scene);
        ScreenUtil.fitDialogToScreen(this, getOwner(), 1080, 620, 950, 520);
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {
                List<ProductModel> productos = service.obtenerProductos();
                List<OrderNoteModel> notas = service.obtenerNotasPedido();
                List<DispatchModel> despachos = service.obtenerDespachos();

                javafx.application.Platform.runLater(() -> {
                    productMap.clear();
                    for (ProductModel p : productos) {
                        productMap.put(p.getId(), p);
                    }

                    noteMap.clear();
                    
                    for (OrderNoteModel n : notas) {
                        noteMap.put(n.getId(), n);
                        
                    }

                    observableList.clear();
                    observableList.addAll(despachos);
                    
                    npConfirmadasList.clear();
                    for (OrderNoteModel n : notas) {
                        if ("COMPLETADA".equalsIgnoreCase(n.getEstado())) {
                            // Check if pending to dispatch
                            boolean pending = false;
                            if (n.getDetails() != null) {
                                for (OrderNoteDetailModel det : n.getDetails()) {
                                    int bo = det.getCantidadBackOrder() != null ? det.getCantidadBackOrder() : 0;
                                    int req = det.getCantidadPedida() != null ? det.getCantidadPedida() : 0;
                                    int efec = req - bo;
                                    int des = det.getCantidadDespachada() != null ? det.getCantidadDespachada() : 0;
                                    if (efec > des) { pending = true; break; }
                                }
                            }
                            if (pending) {
                                npConfirmadasList.add(n);
                            }
                        }
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

    private void guardarDespacho() {
        OrderNoteModel note = selectedNP;
        String guia = txtGuia.getText().trim();
        String factura = txtFactura.getText().trim();
        String trans = txtTransportista.getText().trim();
        String patente = txtPatente.getText().trim();
        String comentario = txtComentario.getText().trim();

        if (note == null) {
            mostrarWarning("Validación", "Debe seleccionar una Nota de Pedido.");
            return;
        }

        if (guia.isEmpty() && factura.isEmpty()) {
            mostrarWarning("Validación", "Debe ingresar al menos una Guía de Despacho o Factura.");
            return;
        }

        // Crear modelo de despacho
        DispatchModel model = new DispatchModel(); // Siempre creamos uno nuevo porque no hay edición
        model.setOrderNoteId(note.getId());
        model.setGuiaDespacho(guia.isEmpty() ? null : guia);
        model.setFactura(factura.isEmpty() ? null : factura);
        model.setTransportista(trans.isEmpty() ? null : trans);
        model.setPatente(patente.isEmpty() ? null : patente);
        model.setComentario(comentario.isEmpty() ? null : comentario);
        model.setEstado("DESPACHADO"); // Forzado

        List<DispatchDetailModel> details = new ArrayList<>();
        for (OrderNoteDetailModel item : itemsToDispatchList) {
            if (item.getCantidadDespachada() > 0) {
                details.add(DispatchDetailModel.builder()
                        .productId(item.getProductId())
                        .cantidadDespachada(item.getCantidadDespachada())
                        .build());
            }
        }

        if (details.isEmpty()) {
            mostrarWarning("Validación", "Debe indicar al menos un producto con cantidad mayor a 0 para despachar.");
            return;
        }

        model.setDetails(details);

        new Thread(() -> {
            try {
                if (selectedDispatch == null) {
                    model.setFechaDespacho(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    DispatchModel created = service.crearDespacho(model);
                    javafx.application.Platform.runLater(() -> {
                        observableList.add(created);
                        mostrarInformacion("Éxito", "Despacho guardado exitosamente y stock actualizado.");
                        limpiarFormulario();
                        cargarDatos(); // Recargar datos para ver notas de pedido actualizadas en despacho
                    });
                } else {
                    DispatchModel updated = service.actualizarDespacho(model.getId(), model);
                    javafx.application.Platform.runLater(() -> {
                        int idx = observableList.indexOf(selectedDispatch);
                        if (idx >= 0) {
                            observableList.set(idx, updated);
                        }
                        mostrarInformacion("Éxito", "Despacho actualizado exitosamente.");
                        limpiarFormulario();
                        cargarDatos();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error", "No se pudo guardar el despacho: " + e.getMessage());
                });
            }
        }).start();
    }

    private void eliminarDespacho() {
        mostrarWarning("Acción No Permitida", "Los despachos son definitivos y no pueden ser anulados ni eliminados.");
    }

    private void limpiarFormulario() {
        selectedDispatch = null;
        tablaDespachos.getSelectionModel().clearSelection();
        lblNPSelected.setText("Seleccione una NP de la izquierda");
        selectedNP = null;
        txtGuia.clear();
        txtFactura.clear();
        txtTransportista.clear();
        txtPatente.clear();
        txtComentario.clear();
        itemsToDispatchList.clear();
        toggleEditingFields(true);
    }

    private void toggleEditingFields(boolean editable) {
        if (btnGuardar != null) btnGuardar.setDisable(!editable);

        
        txtGuia.setDisable(!editable);
        txtFactura.setDisable(!editable);
        txtTransportista.setDisable(!editable);
        txtPatente.setDisable(!editable);
        txtComentario.setDisable(!editable);
        tablaItems.setEditable(editable);
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
}
