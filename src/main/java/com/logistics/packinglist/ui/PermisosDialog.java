package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.service.MantenimientoService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.*;

public class PermisosDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

        private ComboBox<CompanyModel> cbEmpresa;
    
    // Checkboxes mapped to permission keys
    private final Map<String, CheckBox> checkboxMap = new LinkedHashMap<>();
    private Runnable onCloseCallback;

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    public PermisosDialog(Window owner) {
                initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Permisos de Empresa");
        
        setMinWidth(600);
        setMinHeight(500);

        construirUI();
        cargarDatos();
    }

    private void construirUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f7fb;");

        // Cabecera / Selección de Empresa
        Label lblEmpresa = new Label("Seleccione Empresa:");
        lblEmpresa.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0F3E6E;");
        
        cbEmpresa = new ComboBox<>();
        cbEmpresa.setPromptText("Seleccionar...");
        cbEmpresa.setMaxWidth(Double.MAX_VALUE);
        cbEmpresa.setOnAction(e -> cargarPermisosDeEmpresaSeleccionada());

        HBox topRow = new HBox(10, lblEmpresa, cbEmpresa);
        topRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(cbEmpresa, Priority.ALWAYS);

        // Sección de Permisos
        Label lblPermisos = new Label("Permisos e ítems disponibles:");
        lblPermisos.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0F3E6E;");

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(12);
        grid.setPadding(new Insets(15, 20, 15, 20));
        grid.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-background-radius: 4;");

        // Definición de permisos
        checkboxMap.put("FILE_PACKING_LISTS", new CheckBox("Ver Picking & Packing NP"));
        checkboxMap.put("FILE_LOAD_EXCEL", new CheckBox("Cargar desde Excel"));
        checkboxMap.put("FILE_CREATE_MANUAL", new CheckBox("Crear Picking & Packing Manual"));
        checkboxMap.put("FILE_EDIT_PACKING", new CheckBox("Editar Picking & Packing"));
        checkboxMap.put("FILE_AGRUPAR", new CheckBox("Agrupar Pallets"));
        checkboxMap.put("FILE_CONTENEDOR", new CheckBox("Modelador de Contenedores"));
        checkboxMap.put("OP_UBICACIONES", new CheckBox("Ubicaciones Físicas (WH)"));
        checkboxMap.put("OP_STOCK", new CheckBox("Control de Stock (WH)"));
        checkboxMap.put("OP_NOTAS", new CheckBox("Notas de Pedido (WH)"));
        checkboxMap.put("OP_DESPACHOS", new CheckBox("Despachos (WH)"));
        checkboxMap.put("OP_ANUNCIOS", new CheckBox("Anuncios de Carga (WH)"));
        checkboxMap.put("OP_RECEPCIONES", new CheckBox("Recepciones de Carga (WH)"));

        int row = 0;
        int col = 0;
        for (Map.Entry<String, CheckBox> entry : checkboxMap.entrySet()) {
            CheckBox cb = entry.getValue();
            cb.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");
            grid.add(cb, col, row);
            
            row++;
            if (row >= 6) {
                row = 0;
                col++;
            }
        }

        // Botones de Selección Rápida
        Button btnSelectAll = new Button("Seleccionar Todos");
        btnSelectAll.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
        btnSelectAll.setOnAction(e -> toggleAll(true));

        Button btnDeselectAll = new Button("Deseleccionar Todos");
        btnDeselectAll.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
        btnDeselectAll.setOnAction(e -> toggleAll(false));

        HBox selectRow = new HBox(10, btnSelectAll, btnDeselectAll);
        selectRow.setAlignment(Pos.CENTER_LEFT);

        // Botones de acción
        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSave.setFill(Color.WHITE);
        Button btnGuardar = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.SAVE));
        btnGuardar.getStyleClass().add("btn-guardar");
        btnGuardar.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnGuardar.setOnAction(e -> guardar());

        FontAwesomeIconView iconClose = new FontAwesomeIconView(FontAwesomeIcon.TIMES);
        iconClose.setFill(Color.WHITE);
        Button btnCerrar = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.TIMES));
        btnCerrar.getStyleClass().add("btn-cancelar");
        btnCerrar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnCerrar.setOnAction(e -> close());

        HBox btnRow = new HBox(10, btnGuardar, btnCerrar);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(topRow, lblPermisos, grid, selectRow, btnRow);
        VBox.setVgrow(grid, Priority.ALWAYS);

        Scene scene = new Scene(root, 600, 500);
        setScene(scene);
    }

    private void toggleAll(boolean selected) {
        for (CheckBox cb : checkboxMap.values()) {
            cb.setSelected(selected);
        }
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {
                List<CompanyModel> empresas = service.obtenerEmpresas();
                javafx.application.Platform.runLater(() -> {
                    cbEmpresa.getItems().setAll(empresas);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    alerta("Error", "No se pudieron obtener las empresas: " + e.getMessage());
                });
            }
        }).start();
    }

    private void cargarPermisosDeEmpresaSeleccionada() {
        CompanyModel company = cbEmpresa.getValue();
        if (company == null) {
            toggleAll(false);
            return;
        }

        String permisosStr = company.getPermisos();
        if (permisosStr == null || permisosStr.trim().isEmpty()) {
            // Por defecto, habilitar todo
            toggleAll(true);
        } else {
            Set<String> permisosSet = new HashSet<>(Arrays.asList(permisosStr.split(",")));
            for (Map.Entry<String, CheckBox> entry : checkboxMap.entrySet()) {
                entry.getValue().setSelected(permisosSet.contains(entry.getKey()));
            }
        }
    }

    private void guardar() {
        CompanyModel company = cbEmpresa.getValue();
        if (company == null) {
            alerta("Validación", "Debe seleccionar una empresa.");
            return;
        }

        // Construir string de permisos
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, CheckBox> entry : checkboxMap.entrySet()) {
            if (entry.getValue().isSelected()) {
                list.add(entry.getKey());
            }
        }
        String permisosStr = String.join(",", list);

        // Guardar en la base de datos
        new Thread(() -> {
            try {
                company.setPermisos(permisosStr);
                service.actualizarEmpresa(company.getId(), company);
                javafx.application.Platform.runLater(() -> {
                    informacion("Éxito", "Permisos de la empresa actualizados correctamente.");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    alerta("Error", "No se pudieron guardar los permisos: " + e.getMessage());
                });
            }
        }).start();
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

    @Override
    public void close() {
        super.close();
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }
}
