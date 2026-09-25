package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.DepositModel;
import com.logistics.packinglist.model.RoleModel;
import com.logistics.packinglist.model.WarehouseModel;
import com.logistics.packinglist.service.MantenimientoService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.*;

public class PermisosDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();
    private Runnable onCloseCallback;

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    public PermisosDialog(Window owner) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Gestión de Permisos");
        setMinWidth(750);
        setMinHeight(600);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(crearTabEmpresa());
        tabPane.getTabs().add(crearTabDeposito());
        tabPane.getTabs().add(crearTabBodega());
        tabPane.getTabs().add(crearTabRol());

        VBox root = new VBox(10, tabPane);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Scene scene = new Scene(root);
        if (getClass().getResource("/styles.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        }
        setScene(scene);
    }

    // MAPA DE PERMISOS
    private Map<String, String> getPermisosMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("FILE_PACKING_LISTS", "Ver Picking & Packing NP");
        map.put("FILE_LOAD_EXCEL", "Cargar desde Excel");
        map.put("FILE_CREATE_MANUAL", "Crear Picking & Packing Manual");
        map.put("FILE_EDIT_PACKING", "Editar Picking & Packing");
        map.put("FILE_AGRUPAR", "Agrupar Pallets");
        map.put("FILE_CONTENEDOR", "Modelador de Contenedores");
        map.put("OP_UBICACIONES", "Ubicaciones Físicas (WH)");
        map.put("OP_STOCK", "Control de Stock (WH)");
        map.put("OP_NOTAS", "Notas de Pedido (WH)");
        map.put("OP_DESPACHOS", "Despachos (WH)");
        map.put("OP_ANUNCIOS", "Anuncios de Carga (WH)");
        map.put("OP_RECEPCIONES", "Recepciones de Carga (WH)");
        return map;
    }

    private GridPane crearGridPermisos(Map<String, CheckBox> checkboxMap) {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(15, 0, 15, 0));

        int row = 0;
        int col = 0;
        for (Map.Entry<String, String> entry : getPermisosMap().entrySet()) {
            CheckBox cb = new CheckBox(entry.getValue());
            cb.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");
            checkboxMap.put(entry.getKey(), cb);
            grid.add(cb, col, row);
            
            row++;
            if (row >= 6) {
                row = 0;
                col++;
            }
        }
        return grid;
    }

    private void toggleAll(Map<String, CheckBox> map, boolean state) {
        for (CheckBox cb : map.values()) {
            cb.setSelected(state);
        }
    }

    private void setPermisosToCheckboxes(Map<String, CheckBox> map, String permisosCsv) {
        Set<String> set = new HashSet<>();
        if (permisosCsv != null && !permisosCsv.isEmpty()) {
            set.addAll(Arrays.asList(permisosCsv.split(",")));
        }
        for (Map.Entry<String, CheckBox> entry : map.entrySet()) {
            entry.getValue().setSelected(set.contains(entry.getKey()));
        }
    }

    private String getPermisosFromCheckboxes(Map<String, CheckBox> map) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, CheckBox> entry : map.entrySet()) {
            if (entry.getValue().isSelected()) {
                list.add(entry.getKey());
            }
        }
        return String.join(",", list);
    }

    // --- TAB EMPRESA ---
    private Tab crearTabEmpresa() {
        Tab tab = new Tab("Empresa");
        tab.setClosable(false);
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        
        ComboBox<CompanyModel> cb = new ComboBox<>();
        cb.setPromptText("Seleccione Empresa...");
        cb.setMaxWidth(Double.MAX_VALUE);
        
        Map<String, CheckBox> map = new LinkedHashMap<>();
        GridPane grid = crearGridPermisos(map);
        
        cb.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) setPermisosToCheckboxes(map, val.getPermisos());
        });

        Button btnSave = new Button("Guardar Permisos");
        btnSave.getStyleClass().addAll("btn-action-sm", "btn-action-sm-save");
        btnSave.setOnAction(e -> {
            CompanyModel c = cb.getValue();
            if (c != null) {
                c.setPermisos(getPermisosFromCheckboxes(map));
                new Thread(() -> {
                    try {
                        service.actualizarEmpresa(c.getId(), c);
                        Platform.runLater(() -> informacion("Éxito", "Permisos guardados."));
                    } catch (Exception ex) {
                        Platform.runLater(() -> alerta("Error", ex.getMessage()));
                    }
                }).start();
            }
        });

        Button btnSelectAll = new Button("Sel. Todos");
        btnSelectAll.setOnAction(e -> toggleAll(map, true));
        Button btnDeselectAll = new Button("Desel. Todos");
        btnDeselectAll.setOnAction(e -> toggleAll(map, false));

        HBox btnBox = new HBox(10, btnSelectAll, btnDeselectAll);
        btnBox.setAlignment(Pos.CENTER_LEFT);
        
        content.getChildren().addAll(new Label("Seleccione Empresa:"), cb, grid, btnBox, btnSave);

        new Thread(() -> {
            try {
                List<CompanyModel> res = service.obtenerEmpresas();
                Platform.runLater(() -> cb.getItems().setAll(res));
            } catch (Exception ignored) {}
        }).start();
        
        tab.setContent(content);
        return tab;
    }

    // --- TAB DEPOSITO ---
    private Tab crearTabDeposito() {
        Tab tab = new Tab("Depósito");
        tab.setClosable(false);
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        
        ComboBox<DepositModel> cb = new ComboBox<>();
        cb.setPromptText("Seleccione Depósito...");
        cb.setMaxWidth(Double.MAX_VALUE);
        
        Map<String, CheckBox> map = new LinkedHashMap<>();
        GridPane grid = crearGridPermisos(map);
        
        cb.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) setPermisosToCheckboxes(map, val.getPermisos());
        });

        Button btnSave = new Button("Guardar Permisos");
        btnSave.getStyleClass().addAll("btn-action-sm", "btn-action-sm-save");
        btnSave.setOnAction(e -> {
            DepositModel c = cb.getValue();
            if (c != null) {
                c.setPermisos(getPermisosFromCheckboxes(map));
                new Thread(() -> {
                    try {
                        service.actualizarDeposito(c.getId(), c);
                        Platform.runLater(() -> informacion("Éxito", "Permisos guardados."));
                    } catch (Exception ex) {
                        Platform.runLater(() -> alerta("Error", ex.getMessage()));
                    }
                }).start();
            }
        });

        Button btnSelectAll = new Button("Sel. Todos");
        btnSelectAll.setOnAction(e -> toggleAll(map, true));
        Button btnDeselectAll = new Button("Desel. Todos");
        btnDeselectAll.setOnAction(e -> toggleAll(map, false));

        HBox btnBox = new HBox(10, btnSelectAll, btnDeselectAll);
        btnBox.setAlignment(Pos.CENTER_LEFT);
        
        content.getChildren().addAll(new Label("Seleccione Depósito:"), cb, grid, btnBox, btnSave);

        new Thread(() -> {
            try {
                List<DepositModel> res = service.obtenerDepositos();
                Platform.runLater(() -> cb.getItems().setAll(res));
            } catch (Exception ignored) {}
        }).start();
        
        tab.setContent(content);
        return tab;
    }

    // --- TAB BODEGA ---
    private Tab crearTabBodega() {
        Tab tab = new Tab("Bodega");
        tab.setClosable(false);
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        
        ComboBox<WarehouseModel> cb = new ComboBox<>();
        cb.setPromptText("Seleccione Bodega...");
        cb.setMaxWidth(Double.MAX_VALUE);
        
        Map<String, CheckBox> map = new LinkedHashMap<>();
        GridPane grid = crearGridPermisos(map);
        
        cb.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) setPermisosToCheckboxes(map, val.getPermisos());
        });

        Button btnSave = new Button("Guardar Permisos");
        btnSave.getStyleClass().addAll("btn-action-sm", "btn-action-sm-save");
        btnSave.setOnAction(e -> {
            WarehouseModel c = cb.getValue();
            if (c != null) {
                c.setPermisos(getPermisosFromCheckboxes(map));
                new Thread(() -> {
                    try {
                        service.actualizarBodega(c.getId(), c);
                        Platform.runLater(() -> informacion("Éxito", "Permisos guardados."));
                    } catch (Exception ex) {
                        Platform.runLater(() -> alerta("Error", ex.getMessage()));
                    }
                }).start();
            }
        });

        Button btnSelectAll = new Button("Sel. Todos");
        btnSelectAll.setOnAction(e -> toggleAll(map, true));
        Button btnDeselectAll = new Button("Desel. Todos");
        btnDeselectAll.setOnAction(e -> toggleAll(map, false));

        HBox btnBox = new HBox(10, btnSelectAll, btnDeselectAll);
        btnBox.setAlignment(Pos.CENTER_LEFT);
        
        content.getChildren().addAll(new Label("Seleccione Bodega:"), cb, grid, btnBox, btnSave);

        new Thread(() -> {
            try {
                List<WarehouseModel> res = service.obtenerBodegas();
                Platform.runLater(() -> cb.getItems().setAll(res));
            } catch (Exception ignored) {}
        }).start();
        
        tab.setContent(content);
        return tab;
    }

    // --- TAB ROL ---
    private Tab crearTabRol() {
        Tab tab = new Tab("Rol");
        tab.setClosable(false);
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        
        ComboBox<RoleModel> cb = new ComboBox<>();
        cb.setPromptText("Seleccione Rol...");
        cb.setMaxWidth(Double.MAX_VALUE);
        
        Map<String, CheckBox> map = new LinkedHashMap<>();
        GridPane grid = crearGridPermisos(map);
        
        cb.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) setPermisosToCheckboxes(map, val.getPermisos());
        });

        Button btnSave = new Button("Guardar Permisos");
        btnSave.getStyleClass().addAll("btn-action-sm", "btn-action-sm-save");
        btnSave.setOnAction(e -> {
            RoleModel c = cb.getValue();
            if (c != null) {
                c.setPermisos(getPermisosFromCheckboxes(map));
                new Thread(() -> {
                    try {
                        service.actualizarRol(c.getId(), c);
                        Platform.runLater(() -> informacion("Éxito", "Permisos guardados."));
                    } catch (Exception ex) {
                        Platform.runLater(() -> alerta("Error", ex.getMessage()));
                    }
                }).start();
            }
        });

        Button btnSelectAll = new Button("Sel. Todos");
        btnSelectAll.setOnAction(e -> toggleAll(map, true));
        Button btnDeselectAll = new Button("Desel. Todos");
        btnDeselectAll.setOnAction(e -> toggleAll(map, false));

        HBox btnBox = new HBox(10, btnSelectAll, btnDeselectAll);
        btnBox.setAlignment(Pos.CENTER_LEFT);
        
        content.getChildren().addAll(new Label("Seleccione Rol:"), cb, grid, btnBox, btnSave);

        new Thread(() -> {
            try {
                List<RoleModel> res = service.obtenerRoles(null);
                Platform.runLater(() -> cb.getItems().setAll(res));
            } catch (Exception ignored) {}
        }).start();
        
        tab.setContent(content);
        return tab;
    }

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
}
