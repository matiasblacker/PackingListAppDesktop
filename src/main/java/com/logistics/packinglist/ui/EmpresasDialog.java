package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.RegionModel;
import com.logistics.packinglist.service.MantenimientoService;
import java.util.ArrayList;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.paint.Color;

import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import com.logistics.packinglist.service.WebSocketManager;

public class EmpresasDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

        private final ObservableList<CompanyModel> observableList;
    private final FilteredList<CompanyModel> filteredList;
    private final WebSocketManager.UpdateListener wsListener = action -> cargarDatos();

    private TableView<CompanyModel> tabla;
    private TextField txtBusqueda;

    // Campos de formulario
    private TextField txtRazonSocial;
    private TextField txtRut;
    private TextField txtGiro;
    private TextField txtDireccion;
    private ComboBox<String> cbComuna;
    private ComboBox<String> cbRegion;
    private TextField txtTelefono;
    private TextField txtEmail;
    private List<RegionModel> listaRegiones = new ArrayList<>();

    private Button btnAdd;
    private Button btnDelete;
    private Button btnClear;

    private CompanyModel selectedCompany = null;

    // Campos de Logo
    private Button btnSeleccionarLogo;
    private ImageView imgLogoPreview;
    private File logoSeleccionado = null;
    private String currentLogoUrl = null;

    public EmpresasDialog(Window owner) {
                this.observableList = FXCollections.observableArrayList();
        this.filteredList = new FilteredList<>(observableList, p -> true);

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Gestión de Empresas");

        setMinWidth(850);
        setMinHeight(520);

        construirUI();
        cargarDatos();

        WebSocketManager.getInstance().subscribe("COMPANY", wsListener);
        setOnHiding(e -> WebSocketManager.getInstance().unsubscribe("COMPANY", wsListener));
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header Box
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label("Gestión de Empresas");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Administración de empresas clientes, logos y datos de facturación.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // Top toolbar
        txtBusqueda = new TextField();
        txtBusqueda.setPromptText("Buscar por Razón Social o RUT...");
        txtBusqueda.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        txtBusqueda.setPrefWidth(280);
        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(company -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return company.getRazonSocial().toLowerCase().contains(lower)
                        || company.getRut().toLowerCase().contains(lower);
            });
        });

        HBox topRow = new HBox(10, new Label("Buscar:"), txtBusqueda);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Tabla
        tabla = new TableView<>();
        tabla.setItems(filteredList);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<CompanyModel, String> colRut = new TableColumn<>("RUT");
        colRut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRut()));
        colRut.setPrefWidth(120);

        TableColumn<CompanyModel, String> colRazon = new TableColumn<>("Razón Social");
        colRazon.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRazonSocial()));
        colRazon.setPrefWidth(200);

        TableColumn<CompanyModel, String> colGiro = new TableColumn<>("Giro");
        colGiro.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGiro()));
        colGiro.setPrefWidth(150);

        TableColumn<CompanyModel, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDireccion() + ", " + c.getValue().getComuna()));
        colDireccion.setPrefWidth(200);

        TableColumn<CompanyModel, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colEmail.setPrefWidth(150);

        tabla.getColumns().addAll(colRut, colRazon, colGiro, colDireccion, colEmail);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        // Formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 10, 0));

        txtRazonSocial = new TextField();
        txtRazonSocial.setPromptText("Razón Social");
        txtRazonSocial.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtRazonSocial.setMaxWidth(Double.MAX_VALUE);

        txtRut = new TextField();
        txtRut.setPromptText("RUT (e.g. 76.123.456-7)");
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
        
        cbRegion = new ComboBox<>();
        cbRegion.setPromptText("Seleccione Región");
        cbRegion.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbRegion.setMaxWidth(Double.MAX_VALUE);
        
        cbComuna = new ComboBox<>();
        cbComuna.setPromptText("Seleccione Comuna");
        cbComuna.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbComuna.setMaxWidth(Double.MAX_VALUE);
        
        // Listener para actualizar comunas al cambiar de región
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

        grid.add(crearLabel("Razón Social:"), 0, 0);
        grid.add(txtRazonSocial, 1, 0);
        grid.add(crearLabel("RUT:"), 2, 0);
        grid.add(txtRut, 3, 0);

        grid.add(crearLabel("Giro:"), 0, 1);
        grid.add(txtGiro, 1, 1);
        grid.add(crearLabel("Dirección:"), 2, 1);
        grid.add(txtDireccion, 3, 1);

        grid.add(crearLabel("Región:"), 0, 2);
        grid.add(cbRegion, 1, 2);
        grid.add(crearLabel("Comuna:"), 2, 2);
        grid.add(cbComuna, 3, 2);

        grid.add(crearLabel("Teléfono:"), 0, 3);
        grid.add(txtTelefono, 1, 3);
        grid.add(crearLabel("Email:"), 2, 3);
        grid.add(txtEmail, 3, 3);

        grid.add(crearLabel("Logo Empresa:"), 0, 4);
        btnSeleccionarLogo = new Button("Seleccionar Logo...");
        imgLogoPreview = new ImageView();
        imgLogoPreview.setFitWidth(60);
        imgLogoPreview.setFitHeight(60);
        imgLogoPreview.setPreserveRatio(true);
        HBox logoBox = new HBox(10, btnSeleccionarLogo, imgLogoPreview);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(logoBox, 1, 4);

        btnSeleccionarLogo.setOnAction(evt -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Logo de Empresa");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(getScene().getWindow());
            if (file != null) {
                logoSeleccionado = file;
                try {
                    imgLogoPreview.setImage(new Image(file.toURI().toString()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

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
        actionRow.setPadding(new Insets(15, 0, 0, 0));

        // Evento selección de tabla
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedCompany = newSel;
                txtRazonSocial.setText(newSel.getRazonSocial());
                txtRut.setText(newSel.getRut());
                txtGiro.setText(newSel.getGiro() != null ? newSel.getGiro() : "");
                txtDireccion.setText(newSel.getDireccion() != null ? newSel.getDireccion() : "");
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
                txtTelefono.setText(newSel.getTelefono() != null ? newSel.getTelefono() : "");
                txtEmail.setText(newSel.getEmail() != null ? newSel.getEmail() : "");
                
                logoSeleccionado = null;
                if (newSel.getLogoUrl() != null && !newSel.getLogoUrl().isEmpty()) {
                    currentLogoUrl = newSel.getLogoUrl();
                    try {
                        imgLogoPreview.setImage(new Image(newSel.getLogoUrl(), true));
                    } catch (Exception ex) {
                        imgLogoPreview.setImage(null);
                    }
                } else {
                    currentLogoUrl = null;
                    imgLogoPreview.setImage(null);
                }
            }
        });

        // --- LADO IZQUIERDO: LISTADO (WHITE CARD) ---
        VBox leftPane = new VBox(10);
        leftPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        VBox.setVgrow(tabla, Priority.ALWAYS);

        Label lblListTitle = new Label("Empresas Registradas");
        lblListTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        leftPane.getChildren().addAll(lblListTitle, topRow, tabla);

        // --- LADO DERECHO: FORMULARIO (WHITE CARD EN SCROLLPANE) ---
        VBox rightPane = new VBox(12);
        rightPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        
        Label lblFormTitle = new Label("Detalle / Registro de Empresa");
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
                List<RegionModel> regiones = service.obtenerRegiones();
                List<CompanyModel> empresas = service.obtenerEmpresas();
                javafx.application.Platform.runLater(() -> {
                    listaRegiones = regiones;
                    String selectedReg = cbRegion.getValue();
                    String selectedCom = cbComuna.getValue();
                    
                    cbRegion.getItems().clear();
                    for (RegionModel r : regiones) {
                        cbRegion.getItems().add(r.getRegion());
                    }
                    
                    // Restaurar selecciones si existían
                    if (selectedReg != null) {
                        cbRegion.getSelectionModel().select(selectedReg);
                        if (selectedCom != null) {
                            cbComuna.getSelectionModel().select(selectedCom);
                        }
                    }
                    
                    observableList.setAll(empresas);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    alerta("Error al cargar datos", e.getMessage());
                });
            }
        }).start();
    }

    private void guardar() {
        String razon = txtRazonSocial.getText().trim();
        String rut = txtRut.getText().trim();
        String giro = txtGiro.getText().trim();
        String dir = txtDireccion.getText().trim();
        String reg = cbRegion.getValue() != null ? cbRegion.getValue() : "";
        String com = cbComuna.getValue() != null ? cbComuna.getValue() : "";
        String tel = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();

        if (razon.isEmpty() || rut.isEmpty()) {
            alerta("Datos requeridos", "Razón Social y RUT son campos obligatorios.");
            return;
        }

        new Thread(() -> {
            try {
                String finalLogoUrl = currentLogoUrl;
                if (logoSeleccionado != null) {
                    // Subir logo primero y obtener URL
                    byte[] bytes = Files.readAllBytes(logoSeleccionado.toPath());
                    String base64Image = Base64.getEncoder().encodeToString(bytes);
                    String ext = "png";
                    String name = logoSeleccionado.getName();
                    int lastDot = name.lastIndexOf('.');
                    if (lastDot > 0) {
                        ext = name.substring(lastDot + 1);
                    }
                    finalLogoUrl = service.subirLogo(razon, base64Image, ext);
                }

                CompanyModel company = selectedCompany != null ? selectedCompany : new CompanyModel();
                company.setRazonSocial(razon);
                company.setRut(rut);
                company.setGiro(giro);
                company.setDireccion(dir);
                company.setComuna(com);
                company.setRegion(reg);
                company.setTelefono(tel);
                company.setEmail(email);
                company.setLogoUrl(finalLogoUrl);

                if (selectedCompany == null) {
                    service.crearEmpresa(company);
                } else {
                    service.actualizarEmpresa(selectedCompany.getId(), company);
                }
                javafx.application.Platform.runLater(() -> {
                    limpiarFormulario();
                    cargarDatos();
                    informacion("Éxito", "Empresa guardada correctamente.");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    alerta("Error al guardar", e.getMessage());
                });
            }
        }).start();
    }

    private void eliminar() {
        if (selectedCompany == null) {
            alerta("Selección requerida", "Debe seleccionar una empresa de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de eliminar esta empresa?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            new Thread(() -> {
                try {
                    service.eliminarEmpresa(selectedCompany.getId());
                    javafx.application.Platform.runLater(() -> {
                        limpiarFormulario();
                        cargarDatos();
                        informacion("Éxito", "Empresa eliminada.");
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        alerta("Error al eliminar", e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void limpiarFormulario() {
        selectedCompany = null;
        tabla.getSelectionModel().clearSelection();
        txtRazonSocial.clear();
        txtRut.clear();
        txtGiro.clear();
        txtDireccion.clear();
        cbRegion.getSelectionModel().clearSelection();
        cbComuna.getSelectionModel().clearSelection();
        txtTelefono.clear();
        txtEmail.clear();
        logoSeleccionado = null;
        currentLogoUrl = null;
        if (imgLogoPreview != null) {
            imgLogoPreview.setImage(null);
        }
    }

    private void alerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void informacion(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
