package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.DepositModel;
import com.logistics.packinglist.model.RoleModel;
import com.logistics.packinglist.service.MantenimientoService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.*;

public class PermisosDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();
    private Runnable onCloseCallback;

    // Left panel controls
    private ComboBox<CompanyModel> cbEmpresa;
    private ComboBox<DepositModel> cbDeposito;
    private ListView<RoleModel> listViewRoles;
    private final ObservableList<RoleModel> allRoles = FXCollections.observableArrayList();

    // Permission checkboxes: key → CheckBox
    private final Map<String, CheckBox> permisosMap = new LinkedHashMap<>();

    // Module definition: module label → list of (key, label) pairs
    private final List<ModulePermisos> modules = buildModules();

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    public PermisosDialog(Window owner) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Permisos por Rol");
        setMinWidth(900);
        setMinHeight(600);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(buildLeftPanel(), buildRightPanel());
        splitPane.setDividerPositions(0.30);

        VBox root = new VBox(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        root.setStyle("-fx-background-color: #f8fafc;");

        Scene scene = new Scene(root, 960, 620);
        if (getClass().getResource("/styles.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        }
        setScene(scene);

        com.logistics.packinglist.utils.ScreenUtil.fitDialogToScreen(this, owner, 960, 620, 860, 560);
        cargarEmpresas();
    }

    // ─── LEFT PANEL ──────────────────────────────────────────────────────────────

    private VBox buildLeftPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0;");

        Label lblTitle = new Label("Gestión de Permisos por Rol");
        lblTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        // Empresa
        Label lblEmpresa = new Label("Empresa:");
        lblEmpresa.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        cbEmpresa = new ComboBox<>();
        cbEmpresa.setPromptText("Seleccione empresa...");
        cbEmpresa.setMaxWidth(Double.MAX_VALUE);
        cbEmpresa.setStyle("-fx-font-size: 11px;");

        // Depósito
        Label lblDeposito = new Label("Depósito (opcional):");
        lblDeposito.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        cbDeposito = new ComboBox<>();
        cbDeposito.setPromptText("Filtrar por depósito...");
        cbDeposito.setMaxWidth(Double.MAX_VALUE);
        cbDeposito.setStyle("-fx-font-size: 11px;");
        cbDeposito.setDisable(true);

        // Roles
        Label lblRoles = new Label("Roles (multi-selección):");
        lblRoles.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        listViewRoles = new ListView<>(allRoles);
        listViewRoles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewRoles.setMaxWidth(Double.MAX_VALUE);
        listViewRoles.setStyle("-fx-font-size: 11px;");
        VBox.setVgrow(listViewRoles, Priority.ALWAYS);

        // Empresa selection → load deposits and roles
        cbEmpresa.getSelectionModel().selectedItemProperty().addListener((obs, old, empresa) -> {
            cbDeposito.getItems().clear();
            cbDeposito.setDisable(empresa == null);
            allRoles.clear();
            clearPermisosCheckboxes();
            if (empresa != null) {
                cargarDepositos(empresa.getId());
                cargarRoles(empresa.getId());
            }
        });

        // Deposit selection → filter roles (optional refinement)
        cbDeposito.getSelectionModel().selectedItemProperty().addListener((obs, old, dep) -> {
            // Deposit filter is informational; roles are per company not per deposit
            // Just clear selection when deposit changes
            listViewRoles.getSelectionModel().clearSelection();
            clearPermisosCheckboxes();
        });

        // Role selection → populate checkboxes
        listViewRoles.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            List<RoleModel> selected = listViewRoles.getSelectionModel().getSelectedItems();
            poblarCheckboxesDesdRoles(selected);
        });

        panel.getChildren().addAll(
            lblTitle,
            new Separator(),
            lblEmpresa, cbEmpresa,
            lblDeposito, cbDeposito,
            lblRoles, listViewRoles
        );

        return panel;
    }

    // ─── RIGHT PANEL ─────────────────────────────────────────────────────────────

    private VBox buildRightPanel() {
        // Build permission groups
        VBox groupsContainer = new VBox(8);
        groupsContainer.setPadding(new Insets(12));

        for (ModulePermisos mod : modules) {
            // Module header
            Label modLabel = new Label(mod.moduleName);
            modLabel.setMaxWidth(Double.MAX_VALUE);
            modLabel.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a; " +
                "-fx-background-color: #f1f5f9; -fx-padding: 6px 10px; " +
                "-fx-background-radius: 4px;"
            );

            // Checkboxes grid (2 columns)
            GridPane grid = new GridPane();
            grid.setHgap(20);
            grid.setVgap(8);
            grid.setPadding(new Insets(6, 0, 10, 12));

            int col = 0, row = 0;
            for (Map.Entry<String, String> entry : mod.permisos.entrySet()) {
                CheckBox cb = new CheckBox(entry.getValue());
                cb.setStyle("-fx-font-size: 11px; -fx-cursor: hand;");
                permisosMap.put(entry.getKey(), cb);
                grid.add(cb, col, row);
                col++;
                if (col >= 2) { col = 0; row++; }
            }

            groupsContainer.getChildren().addAll(modLabel, grid);
        }

        ScrollPane scrollPane = new ScrollPane(groupsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Bottom buttons
        Button btnSelAll = new Button("Seleccionar Todo");
        btnSelAll.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
        btnSelAll.setOnAction(e -> permisosMap.values().forEach(cb -> cb.setSelected(true)));

        Button btnDeselAll = new Button("Deseleccionar Todo");
        btnDeselAll.setStyle("-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
        btnDeselAll.setOnAction(e -> permisosMap.values().forEach(cb -> cb.setSelected(false)));

        Button btnGuardar = new Button("Guardar Permisos");
        btnGuardar.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 7px 18px;");
        btnGuardar.setOnAction(e -> guardarPermisos());

        HBox btnBar = new HBox(10, btnSelAll, btnDeselAll);
        btnBar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        btnBar.getChildren().addAll(spacer, btnGuardar);
        btnBar.setPadding(new Insets(10, 12, 10, 12));
        btnBar.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        VBox rightPanel = new VBox(scrollPane, btnBar);
        VBox.setVgrow(rightPanel, Priority.ALWAYS);
        rightPanel.setStyle("-fx-background-color: white;");

        return rightPanel;
    }

    // ─── DATA LOADING ─────────────────────────────────────────────────────────────

    private void cargarEmpresas() {
        new Thread(() -> {
            try {
                List<CompanyModel> res = service.obtenerEmpresas();
                Platform.runLater(() -> cbEmpresa.getItems().setAll(res));
            } catch (Exception e) {
                Platform.runLater(() -> alerta("Error", "No se pudieron cargar las empresas: " + e.getMessage()));
            }
        }).start();
    }

    private void cargarDepositos(String companyId) {
        new Thread(() -> {
            try {
                List<DepositModel> todos = service.obtenerDepositos();
                List<DepositModel> filtrados = new ArrayList<>();
                // Add empty option for "todos"
                DepositModel todos_opt = new DepositModel();
                todos_opt.setNombre("— Todos los depósitos —");
                filtrados.add(todos_opt);
                for (DepositModel d : todos) {
                    if (companyId == null || companyId.equals(d.getCompanyId())) {
                        filtrados.add(d);
                    }
                }
                Platform.runLater(() -> {
                    cbDeposito.getItems().setAll(filtrados);
                    cbDeposito.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> alerta("Error", "No se pudieron cargar los depósitos: " + e.getMessage()));
            }
        }).start();
    }

    private void cargarRoles(String companyId) {
        new Thread(() -> {
            try {
                List<RoleModel> res = service.obtenerRoles(companyId);
                Platform.runLater(() -> allRoles.setAll(res));
            } catch (Exception e) {
                Platform.runLater(() -> alerta("Error", "No se pudieron cargar los roles: " + e.getMessage()));
            }
        }).start();
    }

    // ─── LOGIC ───────────────────────────────────────────────────────────────────

    private void poblarCheckboxesDesdRoles(List<RoleModel> roles) {
        clearPermisosCheckboxes();
        if (roles == null || roles.isEmpty()) return;

        // Union of all permissions from selected roles
        Set<String> union = new HashSet<>();
        for (RoleModel rol : roles) {
            if (rol.getPermisos() != null && !rol.getPermisos().isEmpty()) {
                union.addAll(Arrays.asList(rol.getPermisos().split(",")));
            }
        }
        for (Map.Entry<String, CheckBox> entry : permisosMap.entrySet()) {
            entry.getValue().setSelected(union.contains(entry.getKey()));
        }
    }

    private void clearPermisosCheckboxes() {
        permisosMap.values().forEach(cb -> cb.setSelected(false));
    }

    private String getPermisosSeleccionados() {
        List<String> sel = new ArrayList<>();
        for (Map.Entry<String, CheckBox> entry : permisosMap.entrySet()) {
            if (entry.getValue().isSelected()) sel.add(entry.getKey());
        }
        return String.join(",", sel);
    }

    private void guardarPermisos() {
        List<RoleModel> selectedRoles = new ArrayList<>(listViewRoles.getSelectionModel().getSelectedItems());
        if (selectedRoles.isEmpty()) {
            alerta("Sin selección", "Debe seleccionar al menos un Rol de la lista.");
            return;
        }

        String permisos = getPermisosSeleccionados();
        new Thread(() -> {
            List<String> errores = new ArrayList<>();
            for (RoleModel rol : selectedRoles) {
                try {
                    rol.setPermisos(permisos);
                    service.actualizarRol(rol.getId(), rol);
                } catch (Exception ex) {
                    errores.add(rol.getNombre() + ": " + ex.getMessage());
                }
            }
            Platform.runLater(() -> {
                if (errores.isEmpty()) {
                    informacion("Éxito", "Permisos guardados en " + selectedRoles.size() + " rol(es) correctamente.");
                } else {
                    alerta("Errores al guardar", String.join("\n", errores));
                }
            });
        }).start();
    }

    // ─── MODULE DEFINITIONS ──────────────────────────────────────────────────────

    private List<ModulePermisos> buildModules() {
        List<ModulePermisos> list = new ArrayList<>();

        LinkedHashMap<String, String> anuncios = new LinkedHashMap<>();
        anuncios.put("ANUNCIOS_VER", "Ver Anuncios");
        anuncios.put("ANUNCIOS_CREAR", "Crear Anuncio");
        anuncios.put("ANUNCIOS_EDITAR", "Editar Anuncio");
        anuncios.put("ANUNCIOS_ANULAR", "Anular Anuncio");
        anuncios.put("ANUNCIOS_PDF", "Descargar PDF");
        list.add(new ModulePermisos("📢 Anuncios de Carga", anuncios));

        LinkedHashMap<String, String> recepciones = new LinkedHashMap<>();
        recepciones.put("RECEPCIONES_VER", "Ver Recepciones");
        recepciones.put("RECEPCIONES_CREAR", "Registrar Recepción");
        recepciones.put("RECEPCIONES_EDITAR", "Editar Recepción");
        list.add(new ModulePermisos("📥 Recepciones de Carga", recepciones));

        LinkedHashMap<String, String> notas = new LinkedHashMap<>();
        notas.put("NOTAS_VER", "Ver Notas de Pedido");
        notas.put("NOTAS_CREAR", "Crear Nota de Pedido");
        notas.put("NOTAS_EDITAR", "Editar Nota de Pedido");
        notas.put("NOTAS_ANULAR", "Anular Nota de Pedido");
        notas.put("NOTAS_PDF", "Descargar PDF");
        list.add(new ModulePermisos("📋 Notas de Pedido", notas));

        LinkedHashMap<String, String> despachos = new LinkedHashMap<>();
        despachos.put("DESPACHOS_VER", "Ver Despachos");
        despachos.put("DESPACHOS_CREAR", "Crear Despacho");
        despachos.put("DESPACHOS_EDITAR", "Editar Despacho");
        despachos.put("DESPACHOS_ANULAR", "Anular Despacho");
        despachos.put("DESPACHOS_PDF", "Descargar PDF");
        list.add(new ModulePermisos("🚚 Despachos", despachos));

        LinkedHashMap<String, String> packing = new LinkedHashMap<>();
        packing.put("PACKING_VER", "Ver Picking & Packing");
        packing.put("PACKING_CREAR_EXCEL", "Cargar desde Excel");
        packing.put("PACKING_CREAR_MANUAL", "Crear Manual");
        packing.put("PACKING_EDITAR", "Editar Packing");
        packing.put("PACKING_AGRUPAR", "Agrupar Pallets");
        packing.put("PACKING_CONTENEDOR", "Modelador Contenedores");
        list.add(new ModulePermisos("📦 Picking & Packing Lists", packing));

        LinkedHashMap<String, String> stock = new LinkedHashMap<>();
        stock.put("STOCK_VER", "Ver Stock");
        stock.put("STOCK_AJUSTAR", "Ajustar Stock");
        stock.put("STOCK_TRANSFERIR", "Transferir Stock");
        list.add(new ModulePermisos("📊 Control de Stock", stock));

        LinkedHashMap<String, String> ubicaciones = new LinkedHashMap<>();
        ubicaciones.put("UBICACIONES_VER", "Ver Ubicaciones");
        ubicaciones.put("UBICACIONES_CREAR", "Crear Ubicaciones");
        ubicaciones.put("UBICACIONES_EDITAR", "Editar Ubicaciones");
        ubicaciones.put("UBICACIONES_ELIMINAR", "Eliminar Ubicaciones");
        list.add(new ModulePermisos("🗂️ Ubicaciones Físicas", ubicaciones));

        return list;
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────────

    private void alerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void informacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // ─── INNER CLASS ─────────────────────────────────────────────────────────────

    private static class ModulePermisos {
        final String moduleName;
        final Map<String, String> permisos;

        ModulePermisos(String moduleName, Map<String, String> permisos) {
            this.moduleName = moduleName;
            // Use LinkedHashMap to preserve insertion order
            this.permisos = new LinkedHashMap<>(permisos);
        }
    }
}
