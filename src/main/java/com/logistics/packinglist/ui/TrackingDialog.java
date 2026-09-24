package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.CustomerModel;
import com.logistics.packinglist.model.OrderNoteModel;
import com.logistics.packinglist.model.PackingList;
import com.logistics.packinglist.service.MantenimientoService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.logistics.packinglist.utils.ScreenUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TrackingDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    private final ObservableList<OrderNoteModel> observableList;
    private final FilteredList<OrderNoteModel> filteredList;
    private final Map<String, CustomerModel> customerMap = new HashMap<>();
    private final Map<String, CompanyModel> companyMap = new HashMap<>();

    private TableView<OrderNoteModel> tablaTrackings;
    private TextField txtSearch;

    // Ficha derecha
    private Label lblFolio;
    private Label lblTrackingNum;
    private Label lblCliente;
    private Label lblEstadoNP;
    private Label lblEstadoTracking;
    private TextField txtLinkPublico;

    // Historial cronológico (Tema 5)
    private VBox boxHistorialCronologico;

    // Consolidaciones (Tema 7)
    private VBox boxConsolidadas;

    // Formulario de hito
    private VBox formBox;
    private ComboBox<String> cbEstado;

    private ComboBox<String> cbCourier;
    private TextField txtNumeroCourier;

    private TextField txtRetiradoNombre;
    private TextField txtRetiradoRut;
    private TextField txtRetiradoPatente;

    private TextArea txtComentario;
    private FlowPane photoPreviewPane;
    private Label lblPhotoCount;

    private final List<String> uploadedPhotoUrls = new ArrayList<>();

    // Botón Anular Tracking (Tema 2)
    private Button btnAnularTracking;

    // Historial texto
    private ListView<String> listHistorialEvents;

    private OrderNoteModel selectedNote = null;

    public TrackingDialog(Window owner) {
        this.observableList = FXCollections.observableArrayList();
        this.filteredList = new FilteredList<>(observableList, p -> true);

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Módulo de Tracking y Seguimiento de Envíos (Operaciones WH)");

        setMinWidth(950);
        setMinHeight(540);

        construirUI();
        cargarDatos();
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header Box
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label("Gestión y Tracking de Envíos");
        lblTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Control de hitos logísticos, asignación de couriers y trazabilidad de despacho.");
        lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // --- LADO IZQUIERDO: TABLA DE TRACKINGS (WHITE CARD) ---
        VBox leftPane = new VBox(10);
        leftPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        Label lblLeftTitle = new Label("Envíos Registrados en Bodega");
        lblLeftTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        txtSearch = new TextField();
        txtSearch.setPromptText("Buscar por tracking N°, folio, cliente o estado...");
        txtSearch.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filteredList.setPredicate(note -> {
                if (q.isEmpty()) return true;
                if (note.getTrackingNumber() != null && note.getTrackingNumber().toLowerCase().contains(q)) return true;
                if (note.getFolio() != null && note.getFolio().toLowerCase().contains(q)) return true;
                if (note.getEstado() != null && note.getEstado().toLowerCase().contains(q)) return true;
                if (note.getEstadoTracking() != null && note.getEstadoTracking().toLowerCase().contains(q)) return true;
                CustomerModel cli = customerMap.get(note.getCustomerId());
                if (cli != null && cli.getRazonSocial() != null && cli.getRazonSocial().toLowerCase().contains(q)) return true;
                return false;
            });
        });

        tablaTrackings = new TableView<>(filteredList);
        tablaTrackings.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tablaTrackings.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaTrackings.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        StatusColumnHelper.applyRowFactory(tablaTrackings);

        TableColumn<OrderNoteModel, String> colFolio = new TableColumn<>("Folio NP");
        colFolio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolio()));
        colFolio.setPrefWidth(70);
        StatusColumnHelper.applyStatusStyling(colFolio, true);

        TableColumn<OrderNoteModel, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(c -> {
            CustomerModel cli = customerMap.get(c.getValue().getCustomerId());
            return new SimpleStringProperty(cli != null ? cli.getRazonSocial() : "-");
        });
        colCliente.setPrefWidth(110);
        StatusColumnHelper.applyStatusStyling(colCliente, false);

        TableColumn<OrderNoteModel, String> colEstadoNP = new TableColumn<>("Estado NP");
        colEstadoNP.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado() != null ? c.getValue().getEstado() : "-"));
        colEstadoNP.setPrefWidth(85);
        StatusColumnHelper.applyStatusStyling(colEstadoNP, false);

        TableColumn<OrderNoteModel, String> colTracking = new TableColumn<>("Código Tracking");
        colTracking.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrackingNumber() != null ? c.getValue().getTrackingNumber() : "-"));
        colTracking.setPrefWidth(130);
        StatusColumnHelper.applyStatusStyling(colTracking, false);

        TableColumn<OrderNoteModel, String> colEstadoTracking = new TableColumn<>("Estado Tracking");
        colEstadoTracking.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstadoTracking() != null ? c.getValue().getEstadoTracking() : "-"));
        colEstadoTracking.setPrefWidth(110);
        StatusColumnHelper.applyStatusStyling(colEstadoTracking, false);

        TableColumn<OrderNoteModel, String> colPicking = new TableColumn<>("Picking & Packing");
        colPicking.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstadoPicking() != null ? c.getValue().getEstadoPicking() : "-"));
        colPicking.setPrefWidth(110);
        StatusColumnHelper.applyStatusStyling(colPicking, false);

        tablaTrackings.getColumns().addAll(colFolio, colCliente, colEstadoNP, colPicking, colTracking, colEstadoTracking);
        VBox.setVgrow(tablaTrackings, Priority.ALWAYS);

        // Acciones columna izquierda
        HBox leftActions = new HBox(10);
        leftActions.setAlignment(Pos.CENTER_LEFT);

        FontAwesomeIconView iconConsolidar = new FontAwesomeIconView(FontAwesomeIcon.COMPRESS);
        iconConsolidar.setFill(Color.WHITE);
        Button btnConsolidar = new Button("", iconConsolidar);
        btnConsolidar.setStyle("-fx-background-color: #6f42c1; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnConsolidar.setOnAction(e -> consolidarSeleccionados());
        Tooltip.install(btnConsolidar, new Tooltip("Consolidar Trackings"));

        FontAwesomeIconView iconRefrescar = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        iconRefrescar.setFill(Color.WHITE);
        Button btnRefrescar = new Button("", iconRefrescar);
        btnRefrescar.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnRefrescar.setOnAction(e -> cargarDatos());
        Tooltip.install(btnRefrescar, new Tooltip("Refrescar Lista"));

        leftActions.getChildren().addAll(btnConsolidar, btnRefrescar);
        leftPane.getChildren().addAll(lblLeftTitle, txtSearch, tablaTrackings, leftActions);

        // --- LADO DERECHO: DETALLES Y HITOS (WHITE CARD EN SCROLLPANE) ---
        VBox rightPane = new VBox(12);
        rightPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        Label lblRightTitle = new Label("Detalles del Envío y Gestión de Hitos");
        lblRightTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        // Ficha Informativa
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(6);
        infoGrid.setPadding(new Insets(10));
        infoGrid.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");

        lblFolio = new Label("-");
        lblFolio.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblTrackingNum = new Label("-");
        lblTrackingNum.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #2b6cb0;");
        lblCliente = new Label("-");
        lblCliente.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblEstadoNP = new Label("-");
        lblEstadoNP.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
        lblEstadoTracking = new Label("-");
        lblEstadoTracking.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #2f855a;");

        Label lblFNP = new Label("Folio NP:");
        lblFNP.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblTRK = new Label("Tracking N°:");
        lblTRK.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblCLI = new Label("Cliente:");
        lblCLI.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblENP = new Label("Estado NP:");
        lblENP.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblETRK = new Label("Estado Tracking:");
        lblETRK.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");

        infoGrid.add(lblFNP, 0, 0);
        infoGrid.add(lblFolio, 1, 0);
        infoGrid.add(lblTRK, 2, 0);
        infoGrid.add(lblTrackingNum, 3, 0);

        infoGrid.add(lblCLI, 0, 1);
        infoGrid.add(lblCliente, 1, 1);
        infoGrid.add(lblENP, 2, 1);
        infoGrid.add(lblEstadoNP, 3, 1);

        infoGrid.add(lblETRK, 0, 2);
        infoGrid.add(lblEstadoTracking, 1, 2, 3, 1);

        // Enlace Público
        HBox linkBox = new HBox(8);
        linkBox.setAlignment(Pos.CENTER_LEFT);
        txtLinkPublico = new TextField();
        txtLinkPublico.setEditable(false);
        txtLinkPublico.setPromptText("Seleccione un envío para ver su URL pública...");
        txtLinkPublico.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e40af; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");
        HBox.setHgrow(txtLinkPublico, Priority.ALWAYS);

        FontAwesomeIconView iconCopy = new FontAwesomeIconView(FontAwesomeIcon.COPY);
        iconCopy.setFill(Color.WHITE);
        Button btnCopyLink = new Button("", iconCopy);
        btnCopyLink.setStyle("-fx-background-color: #3182ce; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        Tooltip.install(btnCopyLink, new Tooltip("Copiar URL Pública"));
        btnCopyLink.setOnAction(e -> {
            if (txtLinkPublico.getText() != null && !txtLinkPublico.getText().isEmpty()) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(txtLinkPublico.getText());
                clipboard.setContent(content);
                mostrarInfo("Enlace Copiado", "La URL de seguimiento del cliente ha sido copiada al portapapeles.");
            }
        });
        Label lblURL = new Label("URL Pública:");
        lblURL.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        linkBox.getChildren().addAll(lblURL, txtLinkPublico, btnCopyLink);

        // --- Tema 5: Historial Cronológico de Estados ---
        Label lblHistCron = new Label("Historial de Estados");
        lblHistCron.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 12px;");

        boxHistorialCronologico = new VBox(4);
        boxHistorialCronologico.setPadding(new Insets(8));
        boxHistorialCronologico.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-color: #f8fafc; -fx-background-radius: 6;");

        Label lblHistPlaceholder = new Label("(Seleccione un envío para ver el historial)");
        lblHistPlaceholder.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        boxHistorialCronologico.getChildren().add(lblHistPlaceholder);

        VBox histCronCard = new VBox(4, lblHistCron, boxHistorialCronologico);

        // --- Tema 7: Consolidadas ---
        Label lblConsTitle = new Label("Notas de Pedido Consolidadas");
        lblConsTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 12px;");

        boxConsolidadas = new VBox(4);
        boxConsolidadas.setPadding(new Insets(8));
        boxConsolidadas.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-color: #f8fafc; -fx-background-radius: 6;");
        boxConsolidadas.setVisible(false);
        boxConsolidadas.setManaged(false);

        VBox consCard = new VBox(4, lblConsTitle, boxConsolidadas);
        consCard.setVisible(false);
        consCard.setManaged(false);
        // Keep reference to consCard for show/hide logic
        boxConsolidadas.setUserData(consCard);

        // --- Formulario de Registro de Nuevo Hito (Tema 3 & 4: simplificado, sin dirección/coords/foto/fecha) ---
        formBox = new VBox(10);
        formBox.setPadding(new Insets(12));
        formBox.setStyle("-fx-border-color: #cbd5e0; -fx-border-radius: 6; -fx-background-color: #ffffff;");

        Label lblFormTitle = new Label("Registrar Nuevo Hito de Seguimiento");
        lblFormTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 13px;");

        Label lblFormInfo = new Label("La fecha y hora del hito se registran automáticamente usando el reloj del servidor.");
        lblFormInfo.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(8);

        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll(
                "REGISTRADO",
                "EN_PREPARACION",
                "PREPARADO",
                "EN_TRANSITO",
                "ENTREGADO",
                "ANULADO"
        );
        cbEstado.setValue("REGISTRADO");
        cbEstado.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 10px;");
        cbEstado.setMaxWidth(Double.MAX_VALUE);
        cbEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switch (newVal) {
                    case "REGISTRADO":
                        txtComentario.setText("Nota de Pedido registrada en el sistema.");
                        break;
                    case "EN_PREPARACION":
                        txtComentario.setText("Pedido en preparación en bodega.");
                        break;
                    case "PREPARADO":
                        txtComentario.setText("Pedido preparado, listo para despachar al cliente.");
                        break;
                    case "EN_TRANSITO":
                        txtComentario.setText("Pedido en tránsito hacia el destino.");
                        break;
                    case "ENTREGADO":
                        txtComentario.setText("Pedido entregado al cliente.");
                        break;
                    case "ANULADO":
                        txtComentario.setText("Nota de Pedido anulada.");
                        break;
                }
            }
        });

        cbCourier = new ComboBox<>();
        cbCourier.setEditable(true);
        cbCourier.getItems().addAll("Starken", "Chilexpress", "DHL", "FedEx", "RETIRO_CLIENTE", "Otro Courier");
        cbCourier.setPromptText("Courier / Transportista");
        cbCourier.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 10px;");
        cbCourier.setMaxWidth(Double.MAX_VALUE);

        txtNumeroCourier = new TextField();
        txtNumeroCourier.setPromptText("N° Seguimiento Courier");
        txtNumeroCourier.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");

        txtRetiradoNombre = new TextField();
        txtRetiradoNombre.setPromptText("Nombre quien retira");
        txtRetiradoNombre.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");

        txtRetiradoRut = new TextField();
        txtRetiradoRut.setPromptText("RUT Persona");
        txtRetiradoRut.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");

        txtRetiradoPatente = new TextField();
        txtRetiradoPatente.setPromptText("Patente Vehículo");
        txtRetiradoPatente.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");

        HBox retiradoBox = new HBox(6, txtRetiradoNombre, txtRetiradoRut, txtRetiradoPatente);

        Label lblETrk = new Label("Estado Tracking:");
        lblETrk.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblCour = new Label("Courier (Opcional):");
        lblCour.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblNCour = new Label("N° Courier (Opcional):");
        lblNCour.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblRet = new Label("Retirado por (Opc):");
        lblRet.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");

        formGrid.add(lblETrk, 0, 0);
        formGrid.add(cbEstado, 1, 0, 3, 1);

        formGrid.add(lblCour, 0, 1);
        formGrid.add(cbCourier, 1, 1);
        formGrid.add(lblNCour, 2, 1);
        formGrid.add(txtNumeroCourier, 3, 1);

        formGrid.add(lblRet, 0, 2);
        formGrid.add(retiradoBox, 1, 2, 3, 1);

        txtComentario = new TextArea();
        txtComentario.setPromptText("Observaciones o notas adicionales del cambio de estado...");
        txtComentario.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");
        txtComentario.setPrefRowCount(2);

        // Fotos de Evidencia (de PackingList — solo lectura)
        HBox photoHeader = new HBox(10);
        photoHeader.setAlignment(Pos.CENTER_LEFT);
        lblPhotoCount = new Label("Evidencia Fotográfica del Packing:");
        lblPhotoCount.setStyle("-fx-font-weight: bold; -fx-font-size: 9.5px; -fx-text-fill: #475569;");
        photoHeader.getChildren().addAll(lblPhotoCount);

        photoPreviewPane = new FlowPane();
        photoPreviewPane.setHgap(8);
        photoPreviewPane.setVgap(8);
        photoPreviewPane.setPrefHeight(65);
        photoPreviewPane.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 4; -fx-padding: 5;");

        FontAwesomeIconView iconSaveHito = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSaveHito.setFill(Color.WHITE);
        Button btnRegistrarHito = new Button("", iconSaveHito);
        btnRegistrarHito.setStyle("-fx-background-color: cornflowerblue; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnRegistrarHito.setOnAction(e -> registrarHito());
        Tooltip.install(btnRegistrarHito, new Tooltip("Registrar Hito de Tracking"));

        // Tema 2: Botón Anular Tracking
        FontAwesomeIconView iconBan = new FontAwesomeIconView(FontAwesomeIcon.BAN);
        iconBan.setFill(Color.WHITE);
        btnAnularTracking = new Button("", iconBan);
        btnAnularTracking.setStyle("-fx-background-color: #dc2626; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnAnularTracking.setOnAction(e -> confirmarAnulacionTracking());
        btnAnularTracking.setDisable(true);
        Tooltip.install(btnAnularTracking, new Tooltip("Anular Tracking"));

        HBox btnBox = new HBox(10, btnRegistrarHito, btnAnularTracking);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Label lblCom = new Label("Comentario:");
        lblCom.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");

        formBox.getChildren().addAll(lblFormTitle, lblFormInfo, formGrid, lblCom, txtComentario, photoHeader, photoPreviewPane, btnBox);

        // Historial texto
        Label lblHistorialTitle = new Label("Historial de Eventos del Envío");
        lblHistorialTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");

        listHistorialEvents = new ListView<>();
        listHistorialEvents.setPrefHeight(180);
        listHistorialEvents.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        rightPane.getChildren().addAll(
                lblRightTitle,
                infoGrid,
                linkBox,
                histCronCard,
                consCard,
                formBox,
                lblHistorialTitle,
                listHistorialEvents
        );

        ScrollPane rightScroll = new ScrollPane(rightPane);
        rightScroll.setFitToWidth(true);
        rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(leftPane, rightScroll);
        mainSplit.setDividerPositions(0.5);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        root.getChildren().addAll(headerBox, mainSplit);

        // Selection Listener
        tablaTrackings.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                cargarFichaEnvio(newSel);
            }
        });

        Scene scene = new Scene(root, 1200, 640);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setScene(scene);
        ScreenUtil.fitDialogToScreen(this, getOwner(), 1200, 640, 950, 540);
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {
                List<CustomerModel> clientes = new ArrayList<>();
                try {
                    clientes = service.obtenerClientes();
                } catch (Exception ignored) {}

                List<CompanyModel> empresas = new ArrayList<>();
                try {
                    String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
                    if (companyId != null) {
                        CompanyModel myComp = service.obtenerEmpresaPorId(companyId);
                        if (myComp != null) empresas.add(myComp);
                    }
                } catch (Exception ignored) {}
                if (empresas.isEmpty()) {
                    try {
                        empresas = service.obtenerEmpresas();
                    } catch (Exception ignored) {}
                }

                List<OrderNoteModel> notas = service.obtenerNotasPedido();

                final List<CustomerModel> finalClientes = clientes;
                final List<CompanyModel> finalEmpresas = empresas;

                Platform.runLater(() -> {
                    customerMap.clear();
                    for (CustomerModel c : finalClientes) {
                        customerMap.put(c.getId(), c);
                    }
                    companyMap.clear();
                    for (CompanyModel comp : finalEmpresas) {
                        companyMap.put(comp.getId(), comp);
                    }
                    observableList.clear();
                    observableList.addAll(notas);
                    tablaTrackings.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void cargarFichaEnvio(OrderNoteModel note) {
        this.selectedNote = note;
        lblFolio.setText(note.getFolio() != null ? note.getFolio() : "-");
        lblTrackingNum.setText(note.getTrackingNumber() != null ? note.getTrackingNumber() : "-");

        CustomerModel cli = customerMap.get(note.getCustomerId());
        lblCliente.setText(cli != null ? cli.getRazonSocial() : "-");

        lblEstadoNP.setText(note.getEstado() != null ? note.getEstado() : "-");
        lblEstadoTracking.setText(note.getEstadoTracking() != null ? note.getEstadoTracking() : "-");

        // Empresa Usuaria (Tenant Company) para la Vanity URL
        CompanyModel tenantCompany = companyMap.get(note.getCompanyId());
        if (tenantCompany == null && !companyMap.isEmpty()) {
            tenantCompany = companyMap.values().iterator().next();
        }

        String companySlug = "empresa";
        if (tenantCompany != null && tenantCompany.getRazonSocial() != null && !tenantCompany.getRazonSocial().trim().isEmpty()) {
            companySlug = tenantCompany.getRazonSocial().toLowerCase().replaceAll("[^a-z0-9]", "");
            if (companySlug.isEmpty()) companySlug = "empresa";
        }

        if (note.getTrackingNumber() != null) {
            txtLinkPublico.setText("http://localhost:8080/" + companySlug + "/tracking?code=" + note.getTrackingNumber());
        } else {
            txtLinkPublico.clear();
        }

        // Tema 2: Verificar estado ANULADO
        boolean isAnulado = "ANULADO".equalsIgnoreCase(note.getEstadoTracking()) 
                || "ANULADO".equalsIgnoreCase(note.getEstado()) 
                || "ANULADA".equalsIgnoreCase(note.getEstado());
        btnAnularTracking.setDisable(isAnulado);
        formBox.setDisable(isAnulado);
        if (isAnulado) {
            formBox.setStyle("-fx-border-color: #fca5a5; -fx-border-radius: 6; -fx-background-color: #fff5f5; -fx-opacity: 0.7;");
        } else {
            formBox.setStyle("-fx-border-color: #cbd5e0; -fx-border-radius: 6; -fx-background-color: #ffffff;");
        }

        cargarHistorialEvents(note);

        // Cargar fotos desde el Packing List de la orden
        new Thread(() -> {
            try {
                List<PackingList> plist = service.obtenerPackingLists();
                PackingList matchingPL = null;
                for (PackingList pl : plist) {
                    if (pl.getNumeroOrden() != null && pl.getNumeroOrden().equalsIgnoreCase(note.getFolio())) {
                        matchingPL = pl;
                        break;
                    }
                }
                final PackingList plToDisplay = matchingPL;
                Platform.runLater(() -> {
                    uploadedPhotoUrls.clear();
                    if (plToDisplay != null && plToDisplay.getFotosUrls() != null && !plToDisplay.getFotosUrls().isEmpty()) {
                        for (String url : plToDisplay.getFotosUrls().split(",")) {
                            if (!url.trim().isEmpty()) {
                                uploadedPhotoUrls.add(url.trim());
                            }
                        }
                    }
                    refrescarFotos();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Tema 7: Cargar notas consolidadas si tiene master tracking
        cargarConsolidadas(note);
    }

    private void cargarHistorialEvents(OrderNoteModel note) {
        listHistorialEvents.getItems().clear();
        boxHistorialCronologico.getChildren().clear();
        if (note.getId() == null) return;

        new Thread(() -> {
            try {
                List<com.logistics.packinglist.model.TrackingEventModel> events = service.obtenerEventosTracking(note.getId());
                Platform.runLater(() -> {
                    // Historial texto (existente)
                    listHistorialEvents.getItems().add("[" + note.getFecha() + "] Estado Inicial NP: " + note.getEstado() + " - Tracking N°: " + note.getTrackingNumber());
                    for (com.logistics.packinglist.model.TrackingEventModel ev : events) {
                        String txt = "[" + ev.getFechaHora() + "] " + ev.getEstado() + " - " + ev.getDescripcion();
                        if (ev.getUbicacionNombre() != null && !ev.getUbicacionNombre().isEmpty()) {
                            txt += " (Ubicación: " + ev.getUbicacionNombre() + ")";
                        }
                        if (ev.getNombreCourier() != null && !ev.getNombreCourier().isEmpty()) {
                            txt += " (Courier: " + ev.getNombreCourier() + " / Seguimiento: " + ev.getNumeroSeguimientoCourier() + ")";
                        }
                        listHistorialEvents.getItems().add(txt);
                    }

                    // Tema 5: Historial cronológico visual
                    boxHistorialCronologico.getChildren().clear();
                    if (events.isEmpty()) {
                        Label lbl = new Label("(Sin hitos registrados)");
                        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                        boxHistorialCronologico.getChildren().add(lbl);
                    } else {
                        DateTimeFormatter fmtDisplay = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        for (int i = 0; i < events.size(); i++) {
                            com.logistics.packinglist.model.TrackingEventModel ev = events.get(i);
                            HBox rowBox = new HBox(8);
                            rowBox.setAlignment(Pos.CENTER_LEFT);

                            String estadoColor = getEstadoColor(ev.getEstado());
                            Label badgeEstado = new Label(ev.getEstado());
                            badgeEstado.setStyle("-fx-background-color: " + estadoColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 8; -fx-background-radius: 4;");

                            String fechaStr = "-";
                            try {
                                if (ev.getFechaHora() != null && !ev.getFechaHora().isEmpty()) {
                                    LocalDateTime ldt = LocalDateTime.parse(ev.getFechaHora().replace(" ", "T").substring(0, 19));
                                    fechaStr = ldt.format(fmtDisplay);
                                }
                            } catch (Exception ignored) {}

                            Label lblFecha = new Label(fechaStr);
                            lblFecha.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

                            rowBox.getChildren().addAll(badgeEstado, lblFecha);
                            boxHistorialCronologico.getChildren().add(rowBox);

                            if (i < events.size() - 1) {
                                Label arrow = new Label("    ↓");
                                arrow.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
                                boxHistorialCronologico.getChildren().add(arrow);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String getEstadoColor(String estado) {
        if (estado == null) return "#94a3b8";
        switch (estado.toUpperCase()) {
            case "REGISTRADO": return "#64748b";
            case "EN_PREPARACION": return "#0284c7";
            case "EN_TRANSITO": return "#d97706";
            case "ENTREGADO_A_CLIENTE": return "#16a34a";
            case "ANULADO": return "#dc2626";
            case "CONSOLIDADO": return "#7c3aed";
            default: return "#64748b";
        }
    }

    private void cargarConsolidadas(OrderNoteModel note) {
        // Obtener el VBox contenedor padre que envuelve el título y la caja
        VBox consCard = (VBox) boxConsolidadas.getUserData();

        // Intentar detectar master tracking desde atributos personalizados
        // Los datos del modelo de escritorio no siempre incluyen atributosPersonalizados, así que lo hacemos en background
        new Thread(() -> {
            try {
                // Obtener datos frescos del backend para la nota
                String noteId = note.getId();
                if (noteId == null) return;

                // Buscar en la lista cargada si hay master tracking en el folio de la nota
                // Consultamos el endpoint de consolidadas pasando el trackingNumber de la nota
                // Si el tracking tiene prefijo MST, es un consolidado master
                String trackingNum = note.getTrackingNumber();
                if (trackingNum == null) {
                    Platform.runLater(() -> {
                        consCard.setVisible(false);
                        consCard.setManaged(false);
                    });
                    return;
                }

                // Intentar obtener notas consolidadas asociadas al mismo master tracking
                // Usamos el tracking de la nota como posible master; el backend filtrará si existe
                List<OrderNoteModel> consolidadas = new ArrayList<>();
                try {
                    consolidadas = service.obtenerNotasPedidoConsolidadas(trackingNum);
                } catch (Exception ignored) {}

                // Si la lista tiene más de 1 resultado, hay consolidación
                if (consolidadas.size() <= 1) {
                    // También probar si el trackingNumber empieza con MST, que es el master key
                    if (trackingNum.startsWith("MST-")) {
                        // Es el propio master, cargar el listado
                    } else {
                        final List<OrderNoteModel> empty = new ArrayList<>();
                        Platform.runLater(() -> {
                            consCard.setVisible(false);
                            consCard.setManaged(false);
                        });
                        return;
                    }
                }

                final List<OrderNoteModel> finalConsolidadas = consolidadas;

                Platform.runLater(() -> {
                    boxConsolidadas.getChildren().clear();
                    if (finalConsolidadas.isEmpty()) {
                        consCard.setVisible(false);
                        consCard.setManaged(false);
                    } else {
                        consCard.setVisible(true);
                        consCard.setManaged(true);
                        boxConsolidadas.setVisible(true);
                        boxConsolidadas.setManaged(true);

                        for (OrderNoteModel n : finalConsolidadas) {
                            HBox row = new HBox(8);
                            row.setAlignment(Pos.CENTER_LEFT);

                            boolean anulada = "ANULADO".equalsIgnoreCase(n.getEstado());
                            Label lblFolioC = new Label(n.getFolio() != null ? n.getFolio() : "-");
                            Label lblEstC = new Label(n.getEstado() != null ? n.getEstado() : "-");

                            if (anulada) {
                                lblFolioC.setStyle("-fx-strikethrough: true; -fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                                lblEstC.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 10px; -fx-font-style: italic;");
                            } else {
                                lblFolioC.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-font-size: 11px;");
                                lblEstC.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 10px;");
                            }

                            Label iconLabel = new Label(anulada ? "✗" : "✓");
                            iconLabel.setStyle("-fx-text-fill: " + (anulada ? "#dc2626" : "#16a34a") + "; -fx-font-weight: bold;");

                            row.getChildren().addAll(iconLabel, lblFolioC, new Label("—"), lblEstC);
                            boxConsolidadas.getChildren().add(row);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void refrescarFotos() {
        photoPreviewPane.getChildren().clear();
        lblPhotoCount.setText("Evidencia Fotográfica del Packing (" + uploadedPhotoUrls.size() + "):");

        for (String url : uploadedPhotoUrls) {
            HBox thumb = new HBox(4);
            thumb.setAlignment(Pos.CENTER_LEFT);
            thumb.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e0; -fx-padding: 3; -fx-border-radius: 4;");

            ImageView imgView = new ImageView();
            imgView.setFitWidth(40);
            imgView.setFitHeight(40);
            imgView.setPreserveRatio(true);
            try {
                imgView.setImage(new Image(url, true));
            } catch (Exception ignored) {}

            thumb.getChildren().addAll(imgView);
            photoPreviewPane.getChildren().add(thumb);
        }
    }

    private void registrarHito() {
        if (selectedNote == null) {
            mostrarWarn("Validación", "Debe seleccionar un envío de la lista.");
            return;
        }

        String estado = cbEstado.getValue();
        String courier = cbCourier.getValue() != null ? cbCourier.getValue().trim() : null;
        String numCourier = txtNumeroCourier.getText().trim();
        String comentario = txtComentario.getText().trim();

        String retNombre = txtRetiradoNombre.getText().trim();
        String retRut = txtRetiradoRut.getText().trim();
        String retPatente = txtRetiradoPatente.getText().trim();

        // Tema 4: Timestamp automático del servidor — no enviamos fechaHora
        new Thread(() -> {
            try {
                service.registrarEventoTracking(
                        selectedNote.getId(),
                        selectedNote.getTrackingNumber(),
                        estado,
                        comentario,
                        "Bodega Central",
                        null,
                        null,
                        courier,
                        numCourier.isEmpty() ? null : numCourier,
                        null,
                        retNombre.isEmpty() ? null : retNombre,
                        retRut.isEmpty() ? null : retRut,
                        retPatente.isEmpty() ? null : retPatente,
                        null  // fechaHora null → el servidor asigna el timestamp
                );

                Platform.runLater(() -> {
                    mostrarInfo("Éxito", "Hito de tracking registrado correctamente.");
                    txtComentario.clear();
                    txtNumeroCourier.clear();
                    txtRetiradoNombre.clear();
                    txtRetiradoRut.clear();
                    txtRetiradoPatente.clear();
                    cargarDatos();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarWarn("Error", "No se pudo registrar el hito: " + e.getMessage()));
            }
        }).start();
    }

    // Tema 2: Anulación de Tracking
    private void confirmarAnulacionTracking() {
        if (selectedNote == null) {
            mostrarWarn("Validación", "Debe seleccionar un envío de la lista.");
            return;
        }

        if ("ANULADO".equalsIgnoreCase(selectedNote.getEstadoTracking()) || "ANULADO".equalsIgnoreCase(selectedNote.getEstado())) {
            mostrarWarn("Operación No Permitida", "El tracking de esta nota ya se encuentra en estado ANULADO.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Anular Tracking");
        dialog.setHeaderText("Anular Tracking del Folio: " + selectedNote.getFolio());
        dialog.setContentText("Ingrese el motivo de anulación:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(motivo -> {
            if (motivo.trim().isEmpty()) {
                mostrarWarn("Requerido", "Debe ingresar un motivo para anular el tracking.");
                return;
            }

            new Thread(() -> {
                try {
                    service.registrarEventoTracking(
                            selectedNote.getId(),
                            selectedNote.getTrackingNumber(),
                            "ANULADO",
                            "Tracking anulado. Motivo: " + motivo.trim(),
                            "Bodega Central",
                            null, null, null, null, null, null, null, null,
                            null  // server timestamp
                    );
                    Platform.runLater(() -> {
                        mostrarInfo("Tracking Anulado", "El tracking ha sido marcado como ANULADO exitosamente.");
                        cargarDatos();
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> mostrarWarn("Error", "No se pudo anular el tracking: " + ex.getMessage()));
                }
            }).start();
        });
    }

    private void consolidarSeleccionados() {
        ObservableList<OrderNoteModel> selectedItems = tablaTrackings.getSelectionModel().getSelectedItems();
        if (selectedItems == null || selectedItems.size() < 2) {
            mostrarWarn("Consolidación", "Debe seleccionar al menos 2 notas de pedido de la tabla (manteniendo pulsada la tecla Ctrl/Cmd).");
            return;
        }

        String firstCustomerId = selectedItems.get(0).getCustomerId();
        for (OrderNoteModel item : selectedItems) {
            if (!Objects.equals(item.getCustomerId(), firstCustomerId)) {
                mostrarWarn("Consolidación Denegada", "Todos los envíos a consolidar deben pertenecer al mismo cliente.");
                return;
            }
        }

        List<String> ids = new ArrayList<>();
        for (OrderNoteModel item : selectedItems) {
            ids.add(item.getId());
        }

        new Thread(() -> {
            try {
                service.consolidarTrackings(ids);
                Platform.runLater(() -> {
                    mostrarInfo("Consolidación Exitosa", "Se han consolidado " + ids.size() + " envíos bajo el estado 'CONSOLIDADO'.");
                    cargarDatos();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarWarn("Error de Consolidación", e.getMessage()));
            }
        }).start();
    }

    private void mostrarInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void mostrarWarn(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
