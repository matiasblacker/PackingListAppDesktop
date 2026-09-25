package com.logistics.packinglist.ui;
import com.logistics.packinglist.model.RoleModel;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import java.util.Optional;
import javafx.application.Platform;

import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.DepositModel;
import com.logistics.packinglist.model.UserModel;
import com.logistics.packinglist.model.WarehouseModel;
import com.logistics.packinglist.service.AuthService;
import com.logistics.packinglist.service.MantenimientoService;
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
import com.logistics.packinglist.utils.ScreenUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import com.logistics.packinglist.service.WebSocketManager;

public class UsuariosDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

        private final ObservableList<UserModel> observableList;
    private final FilteredList<UserModel> filteredList;
    private final ObservableList<CompanyModel> companiesList;
    private final List<WarehouseModel> allWarehouses;
    private final ObservableList<WarehouseModel> filteredWarehouses;
    private final WebSocketManager.UpdateListener wsListener = action -> cargarDatos();

    private TableView<UserModel> tabla;
    private TextField txtBusqueda;

    // Campos de formulario
    private TextField txtNombre;
    private TextField txtApellido;
    private TextField txtEmail;
    private PasswordField txtPassword;
    private ComboBox<RoleModel> cmbRol;
    private Button btnNuevoRol;
    private ComboBox<String> cmbEstado;
    private ComboBox<CompanyModel> cmbEmpresa;
    private ComboBox<DepositModel> cmbDeposito;
    private ComboBox<WarehouseModel> cmbBodega;

    private Label lblPassword;
    private Label lblEstado;
    private Label lblEmpresa;

    private Button btnAdd;
    private Button btnDelete;
    private Button btnClear;

    private UserModel selectedUser = null;
    private final boolean isAdminSis;

    public UsuariosDialog(Window owner) {
                this.observableList = FXCollections.observableArrayList();
        this.filteredList = new FilteredList<>(observableList, p -> true);
        this.companiesList = FXCollections.observableArrayList();
        this.allWarehouses = new ArrayList<>();
        this.filteredWarehouses = FXCollections.observableArrayList();
        this.isAdminSis = "ADMINSIS".equalsIgnoreCase(AuthService.getInstance().getRole());

        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }
        setTitle("Gestión de Usuarios y Roles");

        setMinWidth(950);
        setMinHeight(520);

        construirUI();
        cargarDatos();

        WebSocketManager.getInstance().subscribe("USER", wsListener);
        setOnHiding(e -> WebSocketManager.getInstance().unsubscribe("USER", wsListener));
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header Box
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label("Gestión de Usuarios y Roles");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Administración de usuarios del sistema, asignación de roles y bodegas.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // Top toolbar
        txtBusqueda = new TextField();
        txtBusqueda.setPromptText("Buscar por Nombre, Apellido o Email...");
        txtBusqueda.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");
        txtBusqueda.setPrefWidth(300);
        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(u -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return u.getNombre().toLowerCase().contains(lower)
                        || u.getApellido().toLowerCase().contains(lower)
                        || u.getEmail().toLowerCase().contains(lower);
            });
        });

        Label lblBuscar = new Label("Buscar:");
        lblBuscar.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        HBox topRow = new HBox(10, lblBuscar, txtBusqueda);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Tabla
        tabla = new TableView<>();
        tabla.setItems(filteredList);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<UserModel, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colEmail.setPrefWidth(180);

        TableColumn<UserModel, String> colNombre = new TableColumn<>("Nombre Completo");
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre() + " " + c.getValue().getApellido()));
        colNombre.setPrefWidth(180);

        TableColumn<UserModel, String> colRol = new TableColumn<>("Rol");
        colRol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));
        colRol.setPrefWidth(120);

        TableColumn<UserModel, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
        colEstado.setPrefWidth(100);

        TableColumn<UserModel, String> colEmpresa = new TableColumn<>("Empresa");
        colEmpresa.setCellValueFactory(c -> {
            String companyId = c.getValue().getCompanyId();
            return new SimpleStringProperty(getNombreEmpresa(companyId));
        });
        colEmpresa.setPrefWidth(150);

        TableColumn<UserModel, String> colBodega = new TableColumn<>("Bodega");
        colBodega.setCellValueFactory(c -> {
            String warehouseId = c.getValue().getWarehouseId();
            return new SimpleStringProperty(getNombreBodega(warehouseId));
        });
        colBodega.setPrefWidth(150);

        tabla.getColumns().addAll(colEmail, colNombre, colRol, colEstado, colEmpresa, colBodega);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        // Formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 10, 0));

        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre");
        txtNombre.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");
        txtNombre.setMaxWidth(Double.MAX_VALUE);

        txtApellido = new TextField();
        txtApellido.setPromptText("Apellido");
        txtApellido.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");
        txtApellido.setMaxWidth(Double.MAX_VALUE);

        txtEmail = new TextField();
        txtEmail.setPromptText("Correo Electrónico");
        txtEmail.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");
        txtEmail.setMaxWidth(Double.MAX_VALUE);

        lblPassword = crearLabel("Contraseña:");
        txtPassword = new PasswordField();
        txtPassword.setPromptText("Mínimo 6 caracteres");
        txtPassword.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 10px; -fx-padding: 2.5px 5px;");
        txtPassword.setMaxWidth(Double.MAX_VALUE);

        cmbRol = new ComboBox<>();
        cmbRol.setPromptText("Seleccione Rol...");
        cmbRol.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 10px;");
        cmbRol.setMaxWidth(Double.MAX_VALUE);
        
        de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView iconPlusRole = new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.PLUS);
        iconPlusRole.setFill(javafx.scene.paint.Color.WHITE);
        btnNuevoRol = new Button("", iconPlusRole);
        btnNuevoRol.getStyleClass().addAll("btn-action-sm");
        btnNuevoRol.setStyle("-fx-background-color: #28a745; -fx-cursor: hand; -fx-padding: 2px 5px;");
        btnNuevoRol.setOnAction(e -> abrirDialogoNuevoRol());
        Tooltip.install(btnNuevoRol, new Tooltip("Nuevo Rol"));

        HBox rolBox = new HBox(5, cmbRol, btnNuevoRol);
        HBox.setHgrow(cmbRol, javafx.scene.layout.Priority.ALWAYS);


        lblEstado = crearLabel("Estado:");
        cmbEstado = new ComboBox<>(FXCollections.observableArrayList("ACTIVO", "INACTIVO"));
        cmbEstado.setValue("ACTIVO");
        cmbEstado.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 10px;");
        cmbEstado.setMaxWidth(Double.MAX_VALUE);

        cmbEmpresa = new ComboBox<>(companiesList);
        cmbEmpresa.setPromptText("Seleccione Empresa...");
        cmbEmpresa.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 10px;");
        cmbEmpresa.setMaxWidth(Double.MAX_VALUE);
        lblEmpresa = crearLabel("Empresa:");

        cmbDeposito = new ComboBox<>();
        cmbDeposito.setPromptText("Seleccione Depósito...");
        cmbDeposito.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 10px;");
        cmbDeposito.setMaxWidth(Double.MAX_VALUE);

        cmbBodega = new ComboBox<>(filteredWarehouses);
        cmbBodega.setPromptText("Seleccione Bodega...");
        cmbBodega.setStyle("-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 10px;");
        cmbBodega.setMaxWidth(Double.MAX_VALUE);

        cmbEmpresa.valueProperty().addListener((obs, oldVal, newVal) -> {
            cmbDeposito.setValue(null);
            cmbDeposito.getItems().clear();
            cmbBodega.setValue(null);
            if (newVal != null) {
                cargarDepositosPorEmpresa(newVal.getId());
            }
        });

        cmbDeposito.valueProperty().addListener((obs, oldVal, newVal) -> {
            cmbBodega.setValue(null);
            filteredWarehouses.clear();
            if (newVal != null) {
                cargarBodegasPorDeposito(newVal.getId());
            }
        });

        // Posicionar controles en el formulario
        grid.add(crearLabel("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(crearLabel("Apellido:"), 2, 0);
        grid.add(txtApellido, 3, 0);

        grid.add(crearLabel("Email:"), 0, 1);
        grid.add(txtEmail, 1, 1);
        grid.add(lblPassword, 2, 1);
        grid.add(txtPassword, 3, 1);

        grid.add(crearLabel("Rol:"), 0, 2);
        grid.add(rolBox, 1, 2);

        int rowOffset = 3;
        if (isAdminSis) {
            grid.add(lblEmpresa, 0, rowOffset);
            grid.add(cmbEmpresa, 1, rowOffset);
            grid.add(crearLabel("Depósito:"), 2, rowOffset);
            grid.add(cmbDeposito, 3, rowOffset);
            
            rowOffset++;
            grid.add(crearLabel("Bodega:"), 0, rowOffset);
            grid.add(cmbBodega, 1, rowOffset);
            grid.add(lblEstado, 2, rowOffset);
            grid.add(cmbEstado, 3, rowOffset);
        } else {
            grid.add(crearLabel("Depósito:"), 0, rowOffset);
            grid.add(cmbDeposito, 1, rowOffset);
            grid.add(crearLabel("Bodega:"), 2, rowOffset);
            grid.add(cmbBodega, 3, rowOffset);
            
            rowOffset++;
            grid.add(lblEstado, 0, rowOffset);
            grid.add(cmbEstado, 1, rowOffset);
        }

        // Constraints
        ColumnConstraints col1 = new ColumnConstraints(80);
        ColumnConstraints col2 = new ColumnConstraints(220);
        ColumnConstraints col3 = new ColumnConstraints(80);
        ColumnConstraints col4 = new ColumnConstraints(220);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);

        // Botones
        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSave.setFill(Color.WHITE);
        btnAdd = new Button("", iconSave);
        btnAdd.getStyleClass().addAll("btn-action-sm", "btn-action-sm-save");
        btnAdd.setOnAction(e -> guardar());
        Tooltip.install(btnAdd, new Tooltip("Guardar"));

        FontAwesomeIconView iconClear = new FontAwesomeIconView(FontAwesomeIcon.ERASER);
        iconClear.setFill(Color.WHITE);
        btnClear = new Button("", iconClear);
        btnClear.getStyleClass().addAll("btn-action-sm", "btn-action-sm-clear");
        btnClear.setOnAction(e -> limpiarFormulario());
        Tooltip.install(btnClear, new Tooltip("Limpiar"));

        FontAwesomeIconView iconDel = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDel.setFill(Color.WHITE);
        btnDelete = new Button("", iconDel);
        btnDelete.getStyleClass().addAll("btn-action-sm", "btn-action-sm-delete");
        btnDelete.setOnAction(e -> eliminar());
        Tooltip.install(btnDelete, new Tooltip("Eliminar"));

        HBox actionRow = new HBox(8, btnAdd, btnClear, btnDelete);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(15, 0, 0, 0));

        // Evento selección
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedUser = newSel;
                txtNombre.setText(newSel.getNombre());
                txtApellido.setText(newSel.getApellido());
                txtEmail.setText(newSel.getEmail());
                txtEmail.setDisable(true); // Email no editable
                
                for (RoleModel r : cmbRol.getItems()) {
                    if (r.getName().equals(newSel.getRole())) {
                        cmbRol.setValue(r);
                        break;
                    }
                }

                cmbEstado.setValue(newSel.getEstado());

                txtPassword.clear();
                txtPassword.setDisable(true);
                lblPassword.setDisable(true);

                if (isAdminSis) {
                    CompanyModel targetComp = companiesList.stream()
                            .filter(c -> c.getId().equals(newSel.getCompanyId()))
                            .findFirst()
                            .orElse(null);
                    cmbEmpresa.setValue(targetComp);
                }

                // Esperar a que se filtren las bodegas y seleccionar la correcta
                javafx.application.Platform.runLater(() -> {
                    WarehouseModel targetWarehouse = allWarehouses.stream()
                            .filter(w -> w.getId().equals(newSel.getWarehouseId()))
                            .findFirst()
                            .orElse(null);

                    if (targetWarehouse != null && targetWarehouse.getDepositId() != null) {
                        DepositModel depTarget = cmbDeposito.getItems().stream()
                            .filter(d -> d.getId().equals(targetWarehouse.getDepositId()))
                            .findFirst().orElse(null);
                        if (depTarget != null) {
                            cmbDeposito.setValue(depTarget);
                        }
                    }

                    javafx.application.Platform.runLater(() -> {
                        WarehouseModel filteredTarget = filteredWarehouses.stream()
                                .filter(w -> w.getId().equals(newSel.getWarehouseId()))
                                .findFirst()
                                .orElse(null);
                        cmbBodega.setValue(filteredTarget);
                    });
                });
            }
        });

        // --- LADO IZQUIERDO: LISTADO (WHITE CARD) ---
        VBox leftPane = new VBox(10);
        leftPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        VBox.setVgrow(tabla, Priority.ALWAYS);

        Label lblListTitle = new Label("Usuarios Registrados");
        lblListTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        leftPane.getChildren().addAll(lblListTitle, topRow, tabla);

        // --- LADO DERECHO: FORMULARIO (WHITE CARD EN SCROLLPANE) ---
        VBox rightPane = new VBox(12);
        rightPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        
        Label lblFormTitle = new Label("Detalle / Registro de Usuario");
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
        if (getClass().getResource("/styles.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        }
        setScene(scene);
        ScreenUtil.fitDialogToScreen(this, getOwner(), 1080, 640, 950, 520);
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {
                // Cargar empresas si es ADMINSIS
                if (isAdminSis) {
                    List<CompanyModel> empresas = service.obtenerEmpresas();
                    javafx.application.Platform.runLater(() -> {
                        companiesList.setAll(empresas);
                    });
                }

                // Cargar todas las bodegas
                List<WarehouseModel> bodegas = service.obtenerBodegas();
                synchronized (allWarehouses) {
                    allWarehouses.clear();
                    allWarehouses.addAll(bodegas);
                }

                // Cargar usuarios
                List<UserModel> usuarios = service.obtenerUsuarios();
                javafx.application.Platform.runLater(() -> {
                    observableList.setAll(usuarios);
                    filtrarBodegas();
                    tabla.refresh();
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    alerta("Error al cargar datos", e.getMessage());
                });
            }
        }).start();
    }

    private void cargarDepositosPorEmpresa(String companyId) {
        new Thread(() -> {
            try {
                List<DepositModel> depositos = service.obtenerDepositos().stream()
                        .filter(d -> companyId.equals(d.getCompanyId()))
                        .toList();
                javafx.application.Platform.runLater(() -> cmbDeposito.getItems().setAll(depositos));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> System.err.println("Error cargando depósitos: " + e.getMessage()));
            }
        }).start();
    }

    private void cargarBodegasPorDeposito(String depositId) {
        new Thread(() -> {
            try {
                List<WarehouseModel> bodegas = service.obtenerBodegasPorDeposito(depositId);
                javafx.application.Platform.runLater(() -> filteredWarehouses.setAll(bodegas));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> System.err.println("Error cargando bodegas: " + e.getMessage()));
            }
        }).start();
    }

    private void filtrarBodegas() {
        // Ahora el filtro se hace via API en cargarBodegasPorDeposito
        filteredWarehouses.clear();
    }

    private String getNombreEmpresa(String companyId) {
        if (companyId == null) return "";
        return companiesList.stream()
                .filter(c -> companyId.equals(c.getId()))
                .map(CompanyModel::getRazonSocial)
                .findFirst()
                .orElse(companyId);
    }

    private String getNombreBodega(String warehouseId) {
        if (warehouseId == null) return "Sin asignar";
        synchronized (allWarehouses) {
            return allWarehouses.stream()
                    .filter(w -> warehouseId.equals(w.getId()))
                    .map(WarehouseModel::getNombre)
                    .findFirst()
                    .orElse("Sin asignar");
        }
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String rol = cmbRol.getValue() != null ? cmbRol.getValue().getName() : null;
        String estado = cmbEstado.getValue();
        WarehouseModel bodega = cmbBodega.getValue();
        CompanyModel empresa = cmbEmpresa.getValue();

        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || rol == null) {
            alerta("Datos requeridos", "Nombre, Apellido, Email y Rol son campos obligatorios.");
            return;
        }

        if (isAdminSis && empresa == null) {
            alerta("Datos requeridos", "Debe seleccionar una Empresa.");
            return;
        }

        if (selectedUser == null && (password == null || password.trim().length() < 6)) {
            alerta("Contraseña requerida", "La contraseña es obligatoria y debe tener al menos 6 caracteres.");
            return;
        }

        String bodegaId = bodega != null ? bodega.getId() : null;
        String empresaId = isAdminSis ? (empresa != null ? empresa.getId() : null) : AuthService.getInstance().getCompanyId();

        new Thread(() -> {
            try {
                if (selectedUser == null) {
                    service.crearUsuario(email, password, nombre, apellido, rol, bodegaId, empresaId);
                } else {
                    service.actualizarUsuario(selectedUser.getId(), nombre, apellido, rol, bodegaId, estado);
                }
                javafx.application.Platform.runLater(() -> {
                    limpiarFormulario();
                    cargarDatos();
                    informacion("Éxito", "Usuario guardado correctamente.");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    alerta("Error al guardar", e.getMessage());
                });
            }
        }).start();
    }

    private void eliminar() {
        if (selectedUser == null) {
            alerta("Selección requerida", "Debe seleccionar un usuario de la tabla para eliminar.");
            return;
        }

        if (selectedUser.getEmail().equalsIgnoreCase(AuthService.getInstance().getEmail())) {
            alerta("Acción denegada", "No puede eliminarse a sí mismo.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de eliminar este usuario?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            new Thread(() -> {
                try {
                    service.eliminarUsuario(selectedUser.getId());
                    javafx.application.Platform.runLater(() -> {
                        limpiarFormulario();
                        cargarDatos();
                        informacion("Éxito", "Usuario eliminado.");
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
        selectedUser = null;
        tabla.getSelectionModel().clearSelection();
        txtNombre.clear();
        txtApellido.clear();
        txtEmail.clear();
        txtEmail.setDisable(false);
        txtPassword.clear();
        txtPassword.setDisable(false);
        lblPassword.setDisable(false);
        cmbRol.setValue(null);
        cmbEstado.setValue("ACTIVO");
        cmbEmpresa.setValue(null);
        cmbDeposito.setValue(null);
        cmbDeposito.getItems().clear();
        cmbBodega.setValue(null);
        filtrarBodegas();
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
        lbl.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        return lbl;
    }

    private void cargarRoles(String companyId) {
        new Thread(() -> {
            try {
                List<RoleModel> roles = com.logistics.packinglist.service.MantenimientoService.getInstance().obtenerRoles(companyId);
                if (isAdminSis && companyId == null) {
                    RoleModel adminSis = new RoleModel();
                    adminSis.setName("ADMINSIS");
                    roles.add(0, adminSis);
                }
                Platform.runLater(() -> {
                    cmbRol.setItems(javafx.collections.FXCollections.observableArrayList(roles));
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void abrirDialogoNuevoRol() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Rol");
        dialog.setHeaderText("Crear un nuevo rol personalizado");
        dialog.setContentText("Nombre del Rol:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nombre -> {
            TextInputDialog descDialog = new TextInputDialog();
            descDialog.setTitle("Descripción");
            descDialog.setHeaderText("Descripción del Rol (opcional)");
            descDialog.setContentText("Descripción:");
            Optional<String> descResult = descDialog.showAndWait();
            
            RoleModel nuevoRol = new RoleModel();
            nuevoRol.setName(nombre.toUpperCase());
            descResult.ifPresent(nuevoRol::setDescription);
            
            if (isAdminSis && cmbEmpresa.getValue() != null) {
                nuevoRol.setCompanyId(cmbEmpresa.getValue().getId());
            } else if (!isAdminSis) {
                nuevoRol.setCompanyId(com.logistics.packinglist.service.AuthService.getInstance().getCompanyId());
            }
            
            new Thread(() -> {
                try {
                    RoleModel guardado = com.logistics.packinglist.service.MantenimientoService.getInstance().crearRol(nuevoRol);
                    Platform.runLater(() -> {
                        cmbRol.getItems().add(guardado);
                        cmbRol.setValue(guardado);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
    }

}