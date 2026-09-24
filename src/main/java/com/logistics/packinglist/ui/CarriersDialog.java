package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.CarrierModel;
import com.logistics.packinglist.service.MantenimientoService;
import com.logistics.packinglist.service.WebSocketManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

public class CarriersDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

        private final ObservableList<CarrierModel> observableList;
    private final FilteredList<CarrierModel> filteredList;
    private final WebSocketManager.UpdateListener wsListener = action -> cargarDatos();

    private TableView<CarrierModel> tabla;
    private TextField txtBusqueda;

    // Form fields
    private TextField txtNombre;
    private TextField txtRut;
    private TextField txtTelefono;
    private TextField txtEmail;
    private TextField txtTrackingUrl;
    private CheckBox chkActivo;

    private Button btnAdd;
    private Button btnDelete;
    private Button btnClear;

    private CarrierModel selectedCarrier = null;

    public CarriersDialog(Window owner) {
                this.observableList = FXCollections.observableArrayList();
        this.filteredList = new FilteredList<>(observableList, p -> true);

        if (owner != null) {
            initOwner(owner);
        }
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Gestión de Transportistas y Couriers");

        setMinWidth(850);
        setMinHeight(520);

        construirUI();
        cargarDatos();

        WebSocketManager.getInstance().subscribe("CARRIER", wsListener);
        setOnHiding(e -> WebSocketManager.getInstance().unsubscribe("CARRIER", wsListener));
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header Box
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label("Gestión de Transportistas y Couriers");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Administración de empresas de transporte, couriers y plantillas de tracking.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // Top Search Bar
        txtBusqueda = new TextField();
        txtBusqueda.setPromptText("Buscar por Nombre, RUT o Email...");
        txtBusqueda.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        txtBusqueda.setPrefWidth(300);
        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(c -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return (c.getNombre() != null && c.getNombre().toLowerCase().contains(lower))
                        || (c.getRut() != null && c.getRut().toLowerCase().contains(lower))
                        || (c.getEmail() != null && c.getEmail().toLowerCase().contains(lower));
            });
        });

        HBox topRow = new HBox(10, new Label("Buscar:"), txtBusqueda);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Table
        tabla = new TableView<>();
        tabla.setItems(filteredList);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<CarrierModel, String> colNombre = new TableColumn<>("Nombre / Courier");
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colNombre.setPrefWidth(180);

        TableColumn<CarrierModel, String> colRut = new TableColumn<>("RUT");
        colRut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRut()));
        colRut.setPrefWidth(100);

        TableColumn<CarrierModel, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefono()));
        colTelefono.setPrefWidth(120);

        TableColumn<CarrierModel, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colEmail.setPrefWidth(160);

        TableColumn<CarrierModel, String> colTrackingUrl = new TableColumn<>("URL Plantilla Tracking");
        colTrackingUrl.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrackingUrlTemplate()));
        colTrackingUrl.setPrefWidth(200);

        TableColumn<CarrierModel, String> colActivo = new TableColumn<>("Estado");
        colActivo.setCellValueFactory(c -> new SimpleStringProperty(Boolean.TRUE.equals(c.getValue().getActivo()) ? "ACTIVO" : "INACTIVO"));
        colActivo.setPrefWidth(90);

        tabla.getColumns().addAll(colNombre, colRut, colTelefono, colEmail, colTrackingUrl, colActivo);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                seleccionarCarrier(newSel);
            }
        });

        // Form Section
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        txtNombre = new TextField();
        txtNombre.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtNombre.setMaxWidth(Double.MAX_VALUE);

        txtRut = new TextField();
        txtRut.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtRut.setMaxWidth(Double.MAX_VALUE);

        txtTelefono = new TextField();
        txtTelefono.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtTelefono.setMaxWidth(Double.MAX_VALUE);

        txtEmail = new TextField();
        txtEmail.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtEmail.setMaxWidth(Double.MAX_VALUE);

        txtTrackingUrl = new TextField();
        txtTrackingUrl.setPromptText("Ej: https://tracking.courier.com/shipment?code={TRACKING}");
        txtTrackingUrl.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtTrackingUrl.setMaxWidth(Double.MAX_VALUE);

        chkActivo = new CheckBox("Activo");
        chkActivo.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        chkActivo.setSelected(true);

        grid.add(crearLabel("Nombre / Courier:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(crearLabel("RUT:"), 2, 0);
        grid.add(txtRut, 3, 0);

        grid.add(crearLabel("Teléfono:"), 0, 1);
        grid.add(txtTelefono, 1, 1);
        grid.add(crearLabel("Email:"), 2, 1);
        grid.add(txtEmail, 3, 1);

        grid.add(crearLabel("Plantilla Tracking URL:"), 0, 2);
        grid.add(txtTrackingUrl, 1, 2, 3, 1);

        grid.add(chkActivo, 1, 3);

        // Buttons
        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSave.setFill(Color.WHITE);
        btnAdd = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.SAVE));
        btnAdd.getStyleClass().add("btn-guardar");
        btnAdd.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnAdd.setOnAction(e -> guardar());

        FontAwesomeIconView iconDel = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDel.setFill(Color.WHITE);
        btnDelete = new Button("", iconDel);
        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnDelete.setOnAction(e -> eliminar());
        Tooltip.install(btnDelete, new Tooltip("Eliminar"));

        FontAwesomeIconView iconClear = new FontAwesomeIconView(FontAwesomeIcon.ERASER);
        iconClear.setFill(Color.WHITE);
        btnClear = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.UNDO));
        btnClear.getStyleClass().add("btn-limpiar");
        btnClear.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnClear.setOnAction(e -> limpiarFormulario());

        HBox actionRow = new HBox(8, btnAdd, btnClear, btnDelete);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        // --- LADO IZQUIERDO: LISTADO (WHITE CARD) ---
        VBox leftPane = new VBox(10);
        leftPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        VBox.setVgrow(tabla, Priority.ALWAYS);

        Label lblListTitle = new Label("Transportistas Registrados");
        lblListTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        leftPane.getChildren().addAll(lblListTitle, topRow, tabla);

        // --- LADO DERECHO: FORMULARIO (WHITE CARD EN SCROLLPANE) ---
        VBox rightPane = new VBox(12);
        rightPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        
        Label lblFormTitle = new Label("Detalle / Registro de Transportista");
        lblFormTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        
        rightPane.getChildren().addAll(lblFormTitle, grid);

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

        root.getChildren().addAll(headerBox, mainSplit, actionRow);

        Scene scene = new Scene(root, 1080, 640);
        setScene(scene);
        com.logistics.packinglist.utils.ScreenUtil.fitDialogToScreen(this, getOwner(), 1080, 640, 850, 520);
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {
                List<CarrierModel> lista = service.obtenerCarriers();
                Platform.runLater(() -> {
                    observableList.setAll(lista);
                    limpiarFormulario();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Error al cargar transportistas: " + e.getMessage());
                });
            }
        }).start();
    }

    private void seleccionarCarrier(CarrierModel c) {
        this.selectedCarrier = c;
        txtNombre.setText(c.getNombre());
        txtRut.setText(c.getRut());
        txtTelefono.setText(c.getTelefono());
        txtEmail.setText(c.getEmail());
        txtTrackingUrl.setText(c.getTrackingUrlTemplate());
        chkActivo.setSelected(Boolean.TRUE.equals(c.getActivo()));
        btnAdd.setText("Actualizar");
    }

    private void limpiarFormulario() {
        this.selectedCarrier = null;
        txtNombre.clear();
        txtRut.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtTrackingUrl.clear();
        chkActivo.setSelected(true);
        btnAdd.setText("Guardar");
        tabla.getSelectionModel().clearSelection();
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "El nombre del transportista es obligatorio.");
            return;
        }

        CarrierModel model = selectedCarrier != null ? selectedCarrier : new CarrierModel();
        model.setNombre(nombre);
        model.setRut(txtRut.getText().trim());
        model.setTelefono(txtTelefono.getText().trim());
        model.setEmail(txtEmail.getText().trim());
        model.setTrackingUrlTemplate(txtTrackingUrl.getText().trim());
        model.setActivo(chkActivo.isSelected());

        new Thread(() -> {
            try {
                if (selectedCarrier == null) {
                    service.crearCarrier(model);
                } else {
                    service.actualizarCarrier(selectedCarrier.getId(), model);
                }
                Platform.runLater(() -> {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Transportista guardado exitosamente.");
                    cargarDatos();
                });
            } catch (Exception e) {
                Platform.runLater(() -> mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar transportista: " + e.getMessage()));
            }
        }).start();
    }

    private void eliminar() {
        if (selectedCarrier == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione un transportista para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de eliminar el transportista " + selectedCarrier.getNombre() + "?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    service.eliminarCarrier(selectedCarrier.getId());
                    Platform.runLater(() -> {
                        mostrarAlerta(Alert.AlertType.INFORMATION, "Transportista eliminado exitosamente.");
                        cargarDatos();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> mostrarAlerta(Alert.AlertType.ERROR, "Error al eliminar transportista: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String msg) {
        Alert a = new Alert(tipo, msg);
        a.setHeaderText(null);
        a.show();
    }

    private Label crearLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("form-label");
        lbl.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        return lbl;
    }
}
