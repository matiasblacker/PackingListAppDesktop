package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.SupplierModel;
import com.logistics.packinglist.model.RegionModel;
import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.service.MantenimientoService;
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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.logistics.packinglist.service.WebSocketManager;

public class ProveedoresDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

        private final ObservableList<SupplierModel> observableList;
    private final FilteredList<SupplierModel> filteredList;
    private final WebSocketManager.UpdateListener wsListener = action -> cargarDatos();

    private TableView<SupplierModel> tabla;
    private TextField txtBusqueda;

    // Campos de formulario
    private TextField txtRazonSocial;
    private Label lblRut;
    private TextField txtRut;
    private TextField txtGiro;
    private TextField txtDireccion;
    private Label lblComuna;
    private ComboBox<String> cbComuna;
    private Label lblRegion;
    private ComboBox<String> cbRegion;
    private Label lblPais;
    private TextField txtPais;
    private CheckBox chkExtranjero;
    private TextField txtTelefono;
    private TextField txtEmail;
    private List<RegionModel> listaRegiones = new ArrayList<>();

    // Multi-company fields for ADMINSIS
    private ScrollPane spEmpresas;
    private VBox vboxEmpresas;
    private ScrollPane spLigadas;
    private VBox vboxLigadas;
    private final List<CheckBox> checkBoxesEmpresas = new ArrayList<>();
    private final Map<String, CompanyModel> companyMap = new HashMap<>();

    private Button btnAdd;
    private Button btnDelete;
    private Button btnClear;

    private SupplierModel selectedSupplier = null;

    public ProveedoresDialog(Window owner) {
                this.observableList = FXCollections.observableArrayList();
        this.filteredList = new FilteredList<>(observableList, p -> true);

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Gestión de Proveedores");

        setMinWidth(850);
        setMinHeight(520);

        construirUI();
        cargarDatos();

        WebSocketManager.getInstance().subscribe("SUPPLIER", wsListener);
        setOnHiding(e -> WebSocketManager.getInstance().unsubscribe("SUPPLIER", wsListener));
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header Box
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label("Gestión de Proveedores");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Administración de proveedores, datos de contacto y empresas asignadas.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // Top toolbar
        txtBusqueda = new TextField();
        txtBusqueda.setPromptText("Buscar por Razón Social o RUT...");
        txtBusqueda.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        txtBusqueda.setPrefWidth(280);
        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(supplier -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return supplier.getRazonSocial().toLowerCase().contains(lower)
                        || supplier.getRut().toLowerCase().contains(lower);
            });
        });

        HBox topRow = new HBox(10, new Label("Buscar:"), txtBusqueda);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Tabla
        tabla = new TableView<>();
        tabla.setItems(filteredList);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(com.logistics.packinglist.service.AuthService.getInstance().getRole());

        if (isAdminSis) {
            TableColumn<SupplierModel, String> colEmpresa = new TableColumn<>("Empresa");
            colEmpresa.setCellValueFactory(c -> {
                CompanyModel comp = companyMap.get(c.getValue().getCompanyId());
                return new SimpleStringProperty(comp != null ? comp.getRazonSocial() : "-");
            });
            colEmpresa.setPrefWidth(120);
            tabla.getColumns().add(colEmpresa);
        }

        TableColumn<SupplierModel, String> colRut = new TableColumn<>("RUT");
        colRut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRut()));
        colRut.setPrefWidth(120);

        TableColumn<SupplierModel, String> colRazon = new TableColumn<>("Razón Social");
        colRazon.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRazonSocial()));
        colRazon.setPrefWidth(200);

        TableColumn<SupplierModel, String> colGiro = new TableColumn<>("Giro");
        colGiro.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGiro()));
        colGiro.setPrefWidth(150);

        TableColumn<SupplierModel, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDireccion() + (c.getValue().getComuna() != null ? ", " + c.getValue().getComuna() : "")
        ));
        colDireccion.setPrefWidth(200);

        TableColumn<SupplierModel, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colEmail.setPrefWidth(150);

        TableColumn<SupplierModel, String> colPais = new TableColumn<>("País");
        colPais.setCellValueFactory(c -> new SimpleStringProperty(Boolean.TRUE.equals(c.getValue().getExtranjero()) ? (c.getValue().getPais() != null && !c.getValue().getPais().isEmpty() ? c.getValue().getPais() : "Extranjero") : "Chile"));
        colPais.setPrefWidth(100);

        tabla.getColumns().addAll(colRut, colRazon, colPais, colGiro, colDireccion, colEmail);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        // Formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 0, 10, 0));

        txtRazonSocial = new TextField();
        txtRazonSocial.setPromptText("Razón Social *");
        txtRazonSocial.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtRazonSocial.setMaxWidth(Double.MAX_VALUE);

        lblRut = crearLabel("RUT:");
        txtRut = new TextField();
        txtRut.setPromptText("RUT (e.g. 76.123.456-7) *");
        txtRut.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtRut.setMaxWidth(Double.MAX_VALUE);

        txtGiro = new TextField();
        txtGiro.setPromptText("Giro comercial");
        txtGiro.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtGiro.setMaxWidth(Double.MAX_VALUE);

        txtDireccion = new TextField();
        txtDireccion.setPromptText("Dirección");
        txtDireccion.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtDireccion.setMaxWidth(Double.MAX_VALUE);

        lblRegion = crearLabel("Región:");
        cbRegion = new ComboBox<>();
        cbRegion.setPromptText("Seleccione Región");
        cbRegion.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbRegion.setMaxWidth(Double.MAX_VALUE);

        lblComuna = crearLabel("Comuna:");
        cbComuna = new ComboBox<>();
        cbComuna.setPromptText("Seleccione Comuna");
        cbComuna.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbComuna.setMaxWidth(Double.MAX_VALUE);

        lblPais = crearLabel("País:");
        txtPais = new TextField();
        txtPais.setPromptText("Nombre del País *");
        txtPais.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtPais.setMaxWidth(Double.MAX_VALUE);
        lblPais.setVisible(false);
        lblPais.setManaged(false);
        txtPais.setVisible(false);
        txtPais.setManaged(false);

        cbRegion.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            cbComuna.getItems().clear();
            if (newVal != null) {
                for (RegionModel r : listaRegiones) {
                    if (r.getRegion().equals(newVal)) {
                        cbComuna.getItems().addAll(r.getComunas());
                        break;
                    }
                }
            }
        });

        txtTelefono = new TextField();
        txtTelefono.setPromptText("Teléfono");
        txtTelefono.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtTelefono.setMaxWidth(Double.MAX_VALUE);

        txtEmail = new TextField();
        txtEmail.setPromptText("Email");
        txtEmail.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtEmail.setMaxWidth(Double.MAX_VALUE);

        chkExtranjero = new CheckBox("Proveedor Extranjero");
        chkExtranjero.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        chkExtranjero.selectedProperty().addListener((obs, oldVal, newVal) -> {
            boolean isExtranjero = Boolean.TRUE.equals(newVal);
            lblRut.setText(isExtranjero ? "Tax ID / RUT Extranjero:" : "RUT:");
            txtRut.setPromptText(isExtranjero ? "Tax ID / ID Extranjero *" : "RUT (e.g. 76.123.456-7) *");

            lblRegion.setVisible(!isExtranjero);
            lblRegion.setManaged(!isExtranjero);
            cbRegion.setVisible(!isExtranjero);
            cbRegion.setManaged(!isExtranjero);

            lblComuna.setVisible(!isExtranjero);
            lblComuna.setManaged(!isExtranjero);
            cbComuna.setVisible(!isExtranjero);
            cbComuna.setManaged(!isExtranjero);

            lblPais.setVisible(isExtranjero);
            lblPais.setManaged(isExtranjero);
            txtPais.setVisible(isExtranjero);
            txtPais.setManaged(isExtranjero);

            if (isExtranjero) {
                cbRegion.getSelectionModel().clearSelection();
                cbComuna.getSelectionModel().clearSelection();
            } else {
                txtPais.clear();
            }
        });

        grid.add(crearLabel("Razón Social *:"), 0, 0);
        grid.add(txtRazonSocial, 1, 0);
        grid.add(lblRut, 2, 0);
        grid.add(txtRut, 3, 0);

        grid.add(crearLabel("Giro:"), 0, 1);
        grid.add(txtGiro, 1, 1);
        grid.add(crearLabel("Dirección:"), 2, 1);
        grid.add(txtDireccion, 3, 1);

        grid.add(lblRegion, 0, 2);
        grid.add(cbRegion, 1, 2);
        grid.add(lblComuna, 2, 2);
        grid.add(cbComuna, 3, 2);

        grid.add(lblPais, 0, 2);
        grid.add(txtPais, 1, 2);

        grid.add(crearLabel("Teléfono:"), 0, 3);
        grid.add(txtTelefono, 1, 3);
        grid.add(crearLabel("Email:"), 2, 3);
        grid.add(txtEmail, 3, 3);

        grid.add(chkExtranjero, 0, 4, 2, 1);

        HBox panelEmpresas = null;
        if (isAdminSis) {
            vboxEmpresas = new VBox(5);
            vboxEmpresas.setPadding(new Insets(5));
            spEmpresas = new ScrollPane(vboxEmpresas);
            spEmpresas.setPrefHeight(120);
            spEmpresas.setPrefWidth(220);
            spEmpresas.setFitToWidth(true);
            spEmpresas.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e0; -fx-border-radius: 4;");

            vboxLigadas = new VBox(5);
            vboxLigadas.setPadding(new Insets(5));
            spLigadas = new ScrollPane(vboxLigadas);
            spLigadas.setPrefHeight(120);
            spLigadas.setPrefWidth(220);
            spLigadas.setFitToWidth(true);
            spLigadas.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e0; -fx-border-radius: 4;");

            panelEmpresas = new HBox(15);
            VBox boxAsignar = new VBox(5, new Label("Asignar a Empresas:"), spEmpresas);
            VBox boxLigadas = new VBox(5, new Label("Empresas Ligadas:"), spLigadas);
            panelEmpresas.getChildren().addAll(boxAsignar, boxLigadas);
            panelEmpresas.setPadding(new Insets(10, 0, 10, 0));
        }

        // Alinear columnas en el formulario
        ColumnConstraints col1 = new ColumnConstraints(90);
        ColumnConstraints col2 = new ColumnConstraints(250);
        ColumnConstraints col3 = new ColumnConstraints(80);
        ColumnConstraints col4 = new ColumnConstraints(250);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);

        // Botones de acción
        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSave.setFill(Color.WHITE);
        btnAdd = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.SAVE));
        btnAdd.getStyleClass().add("btn-guardar");
        btnAdd.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnAdd.setOnAction(e -> guardar());

        FontAwesomeIconView iconDel = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDel.setFill(Color.WHITE);
        btnDelete = new Button("", iconDel);
        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 14px;");
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
        actionRow.setPadding(new Insets(15, 0, 0, 0));

        // Evento selección de tabla
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedSupplier = newSel;
                txtRazonSocial.setText(newSel.getRazonSocial());
                txtRut.setText(newSel.getRut());
                txtGiro.setText(newSel.getGiro() != null ? newSel.getGiro() : "");
                txtDireccion.setText(newSel.getDireccion() != null ? newSel.getDireccion() : "");
                txtTelefono.setText(newSel.getTelefono() != null ? newSel.getTelefono() : "");
                txtEmail.setText(newSel.getEmail() != null ? newSel.getEmail() : "");
                txtPais.setText(newSel.getPais() != null ? newSel.getPais() : "");

                boolean isExtranjero = Boolean.TRUE.equals(newSel.getExtranjero());
                chkExtranjero.setSelected(isExtranjero);

                if (!isExtranjero) {
                    if (newSel.getRegion() != null) {
                        cbRegion.getSelectionModel().select(newSel.getRegion());
                    } else {
                        cbRegion.getSelectionModel().clearSelection();
                    }
                    if (newSel.getComuna() != null) {
                        cbComuna.getSelectionModel().select(newSel.getComuna());
                    } else {
                        cbComuna.getSelectionModel().clearSelection();
                    }
                }

                actualizarEmpresasSeleccionadasYLigadas(newSel.getRut());
            }
        });

        HBox formContainer = new HBox(20);
        formContainer.setAlignment(Pos.TOP_LEFT);
        formContainer.getChildren().add(grid);
        if (isAdminSis && panelEmpresas != null) {
            formContainer.getChildren().add(panelEmpresas);
        }

        // --- LADO IZQUIERDO: LISTADO (WHITE CARD) ---
        VBox leftPane = new VBox(10);
        leftPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        VBox.setVgrow(tabla, Priority.ALWAYS);

        Label lblListTitle = new Label("Proveedores Registrados");
        lblListTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        leftPane.getChildren().addAll(lblListTitle, topRow, tabla);

        // --- LADO DERECHO: FORMULARIO (WHITE CARD EN SCROLLPANE) ---
        VBox rightPane = new VBox(12);
        rightPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        
        Label lblFormTitle = new Label("Detalle / Registro de Proveedor");
        lblFormTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        
        rightPane.getChildren().addAll(lblFormTitle, formContainer);

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
                boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(com.logistics.packinglist.service.AuthService.getInstance().getRole());
                List<CompanyModel> companies = new ArrayList<>();
                if (isAdminSis) {
                    companies = service.obtenerEmpresas();
                }
                List<RegionModel> regiones = service.obtenerRegiones();
                List<SupplierModel> suppliers = service.obtenerProveedores();

                final List<CompanyModel> finalCompanies = companies;
                javafx.application.Platform.runLater(() -> {
                    listaRegiones = regiones;
                    cbRegion.getItems().clear();
                    for (RegionModel r : regiones) {
                        cbRegion.getItems().add(r.getRegion());
                    }

                    if (isAdminSis) {
                        companyMap.clear();
                        vboxEmpresas.getChildren().clear();
                        checkBoxesEmpresas.clear();
                        for (CompanyModel c : finalCompanies) {
                            companyMap.put(c.getId(), c);
                            CheckBox cb = new CheckBox(c.getRazonSocial() + " (" + c.getRut() + ")");
                            cb.setUserData(c);
                            checkBoxesEmpresas.add(cb);
                            vboxEmpresas.getChildren().add(cb);
                        }
                    }

                    observableList.clear();
                    observableList.addAll(suppliers);
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error de carga", "No se pudieron cargar los datos: " + e.getMessage());
                });
            }
        }).start();
    }

    private void guardar() {
        String razon = txtRazonSocial.getText().trim();
        String rut = txtRut.getText().trim();
        String giro = txtGiro.getText().trim();
        String dir = txtDireccion.getText().trim();
        boolean isExtr = chkExtranjero.isSelected();
        String reg = isExtr ? null : cbRegion.getValue();
        String comunaVal = isExtr ? null : cbComuna.getValue();
        String paisVal = isExtr ? txtPais.getText().trim() : null;
        String tel = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();

        if (razon.isEmpty() || rut.isEmpty()) {
            mostrarWarning("Validación", "Los campos Razón Social y RUT / Tax ID son obligatorios.");
            return;
        }

        if (isExtr && (paisVal == null || paisVal.isEmpty())) {
            mostrarWarning("Validación", "El campo País es obligatorio para proveedores extranjeros.");
            return;
        }

        boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(com.logistics.packinglist.service.AuthService.getInstance().getRole());
        List<String> companyIds = new ArrayList<>();
        if (isAdminSis) {
            for (CheckBox cb : checkBoxesEmpresas) {
                if (cb.isSelected()) {
                    CompanyModel comp = (CompanyModel) cb.getUserData();
                    companyIds.add(comp.getId());
                }
            }
        }

        if (selectedSupplier == null && isAdminSis && companyIds.isEmpty()) {
            mostrarWarning("Validación", "Debe seleccionar al menos una empresa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Guardar");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de que desea " + (selectedSupplier == null ? "crear" : "actualizar") + " este proveedor?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        String oldRut = selectedSupplier != null ? selectedSupplier.getRut() : rut;
        List<SupplierModel> linkedSuppliers = new ArrayList<>();
        List<String> previouslyLinked = new ArrayList<>();
        for (SupplierModel s : observableList) {
            if (s.getRut().equalsIgnoreCase(oldRut)) {
                linkedSuppliers.add(s);
                previouslyLinked.add(s.getCompanyId());
            }
        }

        List<String> toAdd = new ArrayList<>();
        for (String cid : companyIds) {
            if (!previouslyLinked.contains(cid)) {
                toAdd.add(cid);
            }
        }

        List<SupplierModel> toDelete = new ArrayList<>();
        for (SupplierModel s : linkedSuppliers) {
            if (!companyIds.contains(s.getCompanyId())) {
                toDelete.add(s);
            }
        }

        List<SupplierModel> toUpdate = new ArrayList<>();
        for (SupplierModel s : linkedSuppliers) {
            if (companyIds.contains(s.getCompanyId())) {
                toUpdate.add(s);
            }
        }

        new Thread(() -> {
            try {
                if (selectedSupplier == null) {
                    if (isAdminSis) {
                        List<SupplierModel> createdList = new ArrayList<>();
                        for (String cid : companyIds) {
                            SupplierModel model = new SupplierModel();
                            model.setCompanyId(cid);
                            model.setRazonSocial(razon);
                            model.setRut(rut);
                            model.setGiro(giro.isEmpty() ? null : giro);
                            model.setDireccion(dir.isEmpty() ? null : dir);
                            model.setRegion(reg);
                            model.setComuna(comunaVal);
                            model.setExtranjero(isExtr);
                            model.setPais(paisVal);
                            model.setTelefono(tel.isEmpty() ? null : tel);
                            model.setEmail(email.isEmpty() ? null : email);

                            createdList.add(service.crearProveedor(model));
                        }
                        javafx.application.Platform.runLater(() -> {
                            observableList.addAll(createdList);
                            mostrarInformacion("Éxito", "Proveedor registrado exitosamente.");
                            limpiarFormulario();
                        });
                    } else {
                        SupplierModel model = new SupplierModel();
                        model.setRazonSocial(razon);
                        model.setRut(rut);
                        model.setGiro(giro.isEmpty() ? null : giro);
                        model.setDireccion(dir.isEmpty() ? null : dir);
                        model.setRegion(reg);
                        model.setComuna(comunaVal);
                        model.setExtranjero(isExtr);
                        model.setPais(paisVal);
                        model.setTelefono(tel.isEmpty() ? null : tel);
                        model.setEmail(email.isEmpty() ? null : email);

                        SupplierModel created = service.crearProveedor(model);
                        javafx.application.Platform.runLater(() -> {
                            observableList.add(created);
                            mostrarInformacion("Éxito", "Proveedor registrado exitosamente.");
                            limpiarFormulario();
                        });
                    }
                } else {
                    if (isAdminSis) {
                        for (SupplierModel s : toDelete) {
                            service.eliminarProveedor(s.getId());
                        }
                        List<SupplierModel> createdList = new ArrayList<>();
                        for (String cid : toAdd) {
                            SupplierModel model = new SupplierModel();
                            model.setCompanyId(cid);
                            model.setRazonSocial(razon);
                            model.setRut(rut);
                            model.setGiro(giro.isEmpty() ? null : giro);
                            model.setDireccion(dir.isEmpty() ? null : dir);
                            model.setRegion(reg);
                            model.setComuna(comunaVal);
                            model.setExtranjero(isExtr);
                            model.setPais(paisVal);
                            model.setTelefono(tel.isEmpty() ? null : tel);
                            model.setEmail(email.isEmpty() ? null : email);

                            createdList.add(service.crearProveedor(model));
                        }
                        List<SupplierModel> updatedList = new ArrayList<>();
                        for (SupplierModel s : toUpdate) {
                            s.setRazonSocial(razon);
                            s.setRut(rut);
                            s.setGiro(giro.isEmpty() ? null : giro);
                            s.setDireccion(dir.isEmpty() ? null : dir);
                            s.setRegion(reg);
                            s.setComuna(comunaVal);
                            s.setExtranjero(isExtr);
                            s.setPais(paisVal);
                            s.setTelefono(tel.isEmpty() ? null : tel);
                            s.setEmail(email.isEmpty() ? null : email);

                            updatedList.add(service.actualizarProveedor(s.getId(), s));
                        }

                        javafx.application.Platform.runLater(() -> {
                            observableList.removeAll(toDelete);
                            observableList.addAll(createdList);
                            for (SupplierModel updated : updatedList) {
                                int idx = -1;
                                for (int i = 0; i < observableList.size(); i++) {
                                    if (observableList.get(i).getId().equals(updated.getId())) {
                                        idx = i;
                                        break;
                                    }
                                }
                                if (idx >= 0) {
                                    observableList.set(idx, updated);
                                }
                            }
                            mostrarInformacion("Éxito", "Proveedor actualizado exitosamente.");
                            limpiarFormulario();
                        });
                    } else {
                        SupplierModel model = selectedSupplier;
                        model.setRazonSocial(razon);
                        model.setRut(rut);
                        model.setGiro(giro.isEmpty() ? null : giro);
                        model.setDireccion(dir.isEmpty() ? null : dir);
                        model.setRegion(reg);
                        model.setComuna(comunaVal);
                        model.setExtranjero(isExtr);
                        model.setPais(paisVal);
                        model.setTelefono(tel.isEmpty() ? null : tel);
                        model.setEmail(email.isEmpty() ? null : email);

                        SupplierModel updated = service.actualizarProveedor(model.getId(), model);
                        javafx.application.Platform.runLater(() -> {
                            int idx = observableList.indexOf(selectedSupplier);
                            if (idx >= 0) {
                                observableList.set(idx, updated);
                            }
                            mostrarInformacion("Éxito", "Proveedor actualizado exitosamente.");
                            limpiarFormulario();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error", "No se pudo guardar: " + e.getMessage());
                });
            }
        }).start();
    }

    private void eliminar() {
        if (selectedSupplier == null) {
            mostrarWarning("Selección", "Debe seleccionar un proveedor de la lista.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de que desea eliminar al proveedor " + selectedSupplier.getRazonSocial() + "?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    service.eliminarProveedor(selectedSupplier.getId());
                    javafx.application.Platform.runLater(() -> {
                        observableList.remove(selectedSupplier);
                        mostrarInformacion("Éxito", "Proveedor eliminado exitosamente.");
                        limpiarFormulario();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        mostrarError("Error", "No se pudo eliminar: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void limpiarFormulario() {
        selectedSupplier = null;
        tabla.getSelectionModel().clearSelection();
        txtRazonSocial.clear();
        txtRut.clear();
        txtGiro.clear();
        txtDireccion.clear();
        chkExtranjero.setSelected(false);
        txtPais.clear();
        cbRegion.getSelectionModel().clearSelection();
        cbComuna.getItems().clear();
        txtTelefono.clear();
        txtEmail.clear();

        actualizarEmpresasSeleccionadasYLigadas(null);
    }

    private void actualizarEmpresasSeleccionadasYLigadas(String rut) {
        if (!"ADMINSIS".equalsIgnoreCase(com.logistics.packinglist.service.AuthService.getInstance().getRole())) {
            return;
        }

        for (CheckBox cb : checkBoxesEmpresas) {
            cb.setSelected(false);
        }
        vboxLigadas.getChildren().clear();

        if (rut == null || rut.trim().isEmpty()) {
            return;
        }

        for (SupplierModel s : observableList) {
            if (rut.equalsIgnoreCase(s.getRut())) {
                CompanyModel comp = companyMap.get(s.getCompanyId());
                if (comp != null) {
                    for (CheckBox cb : checkBoxesEmpresas) {
                        CompanyModel temp = (CompanyModel) cb.getUserData();
                        if (temp.getId().equals(comp.getId())) {
                            cb.setSelected(true);
                            break;
                        }
                    }
                    Label lblComp = new Label(comp.getRazonSocial());
                    lblComp.setStyle("-fx-font-size: 11px; -fx-text-fill: #2d3748;");
                    vboxLigadas.getChildren().add(lblComp);
                }
            }
        }
        if (vboxLigadas.getChildren().isEmpty()) {
            vboxLigadas.getChildren().add(new Label("Ninguna empresa ligada"));
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

    private Label crearLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("form-label");
        lbl.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        return lbl;
    }
}
