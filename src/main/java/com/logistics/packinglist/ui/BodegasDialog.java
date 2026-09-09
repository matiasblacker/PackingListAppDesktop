package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.DepositModel;
import com.logistics.packinglist.model.WarehouseModel;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;

public class BodegasDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();
    private final boolean isAdminSis;

    private final ObservableList<DepositModel> depositsList;
    private final FilteredList<DepositModel> filteredDeposits;
    
    private final ObservableList<WarehouseModel> warehousesList;
    private final FilteredList<WarehouseModel> filteredWarehouses;


    private TableView<DepositModel> tablaDepositos;
    private TextField txtBusquedaDepositos;

    private TableView<WarehouseModel> tablaBodegas;
    private TextField txtBusquedaBodegas;

    // Campos de formulario
    private TextField txtNombre;
    private TextField txtCodigo;
    private TextField txtDireccion;

    private Button btnAdd;
    private Button btnDelete;
    private Button btnClear;

    private DepositModel selectedDeposit = null;
    private WarehouseModel selectedWarehouse = null;

    public BodegasDialog(javafx.stage.Window owner) {
        initOwner(owner);
        this.isAdminSis = "ADMINSIS".equals(com.logistics.packinglist.service.AuthService.getInstance().getRole());

        depositsList = FXCollections.observableArrayList();
        filteredDeposits = new FilteredList<>(depositsList, p -> true);

        warehousesList = FXCollections.observableArrayList();
        filteredWarehouses = new FilteredList<>(warehousesList, p -> true);

        initUI();
        cargarDatos();
    }

    private void initUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header
        VBox headerBox = new VBox(5);
        Label lblTitle = new Label("Bodegas y Zonas");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Seleccione un depósito para ver y administrar sus bodegas.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // --- MASTER LIST (DEPÓSITOS) ---
        VBox masterPane = new VBox(10);
        masterPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        
        Label lblMasterTitle = new Label("Depósitos Registrados");
        lblMasterTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        txtBusquedaDepositos = new TextField();
        txtBusquedaDepositos.setPromptText("Buscar Depósito...");
        txtBusquedaDepositos.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        txtBusquedaDepositos.textProperty().addListener((obs, old, newVal) -> {
            filteredDeposits.setPredicate(d -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return (d.getNombre() != null && d.getNombre().toLowerCase().contains(lower))
                        || (d.getCodigo() != null && d.getCodigo().toLowerCase().contains(lower));
            });
        });

        tablaDepositos = new TableView<>();
        tablaDepositos.setItems(filteredDeposits);
        tablaDepositos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaDepositos.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<DepositModel, String> colDepCodigo = new TableColumn<>("Código");
        colDepCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        TableColumn<DepositModel, String> colDepNombre = new TableColumn<>("Depósito");
        colDepNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        
        tablaDepositos.getColumns().addAll(colDepCodigo, colDepNombre);
        VBox.setVgrow(tablaDepositos, Priority.ALWAYS);

        tablaDepositos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedDeposit = newSel;
            selectedWarehouse = null;
            limpiarFormulario();
            
            if (newSel != null) {
                cargarBodegasDeDeposito(newSel.getId());
            } else {
                warehousesList.clear();
            }
        });

        masterPane.getChildren().addAll(lblMasterTitle, txtBusquedaDepositos, tablaDepositos);

        // --- DETAIL LIST (BODEGAS) ---
        VBox detailPane = new VBox(10);
        detailPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        
        Label lblDetailTitle = new Label("Bodegas del Depósito");
        lblDetailTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        txtBusquedaBodegas = new TextField();
        txtBusquedaBodegas.setPromptText("Buscar Bodega...");
        txtBusquedaBodegas.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        txtBusquedaBodegas.textProperty().addListener((obs, old, newVal) -> {
            filteredWarehouses.setPredicate(w -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return (w.getNombre() != null && w.getNombre().toLowerCase().contains(lower))
                        || (w.getCodigo() != null && w.getCodigo().toLowerCase().contains(lower));
            });
        });

        tablaBodegas = new TableView<>();
        tablaBodegas.setItems(filteredWarehouses);
        tablaBodegas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaBodegas.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<WarehouseModel, String> colBodCodigo = new TableColumn<>("Código");
        colBodCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        TableColumn<WarehouseModel, String> colBodNombre = new TableColumn<>("Bodega");
        colBodNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        
        tablaBodegas.getColumns().addAll(colBodCodigo, colBodNombre);
        VBox.setVgrow(tablaBodegas, Priority.ALWAYS);

        tablaBodegas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedWarehouse = newSel;
                txtCodigo.setText(newSel.getCodigo());
                txtNombre.setText(newSel.getNombre());
                txtDireccion.setText(newSel.getDireccion() != null ? newSel.getDireccion() : "");
            }
        });

        detailPane.getChildren().addAll(lblDetailTitle, txtBusquedaBodegas, tablaBodegas);

        // --- FORMULARIO (DERECHA) ---
        VBox formPane = new VBox(12);
        formPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        
        Label lblFormTitle = new Label("Registro de Bodega");
        lblFormTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 10, 0));

        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre de la Bodega");
        txtNombre.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        
        txtCodigo = new TextField();
        txtCodigo.setPromptText("Código (e.g. BOD-01)");
        txtCodigo.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        
        txtDireccion = new TextField();
        txtDireccion.setPromptText("Dirección (Opcional)");
        txtDireccion.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");

        grid.add(crearLabel("Código:"), 0, 0); grid.add(txtCodigo, 0, 1);
        grid.add(crearLabel("Nombre:"), 0, 2); grid.add(txtNombre, 0, 3);
        grid.add(crearLabel("Dirección:"), 0, 4); grid.add(txtDireccion, 0, 5);

        ColumnConstraints colF = new ColumnConstraints();
        colF.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().add(colF);

        // Botones de acción
        btnAdd = new Button(" Guardar", new FontAwesomeIconView(FontAwesomeIcon.SAVE));
        btnAdd.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setOnAction(e -> guardar());

        btnClear = new Button(" Limpiar", new FontAwesomeIconView(FontAwesomeIcon.ERASER));
        btnClear.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setOnAction(e -> limpiarFormulario());

        btnDelete = new Button(" Eliminar", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnDelete.setMaxWidth(Double.MAX_VALUE);
        btnDelete.setOnAction(e -> eliminar());

        VBox actionBox = new VBox(8, btnAdd, btnClear, btnDelete);
        actionBox.setPadding(new Insets(15, 0, 0, 0));

        formPane.getChildren().addAll(lblFormTitle, grid, actionBox);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(masterPane, detailPane, formPane);
        mainSplit.setDividerPositions(0.3, 0.65);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        root.getChildren().addAll(headerBox, mainSplit);

        Scene scene = new Scene(root, 1080, 680);
        setScene(scene);
        setTitle("Bodegas y Zonas");
    }

    private void cargarBodegasDeDeposito(String depositId) {
        new Thread(() -> {
            try {
                List<WarehouseModel> bodegas = service.obtenerBodegasPorDeposito(depositId);
                javafx.application.Platform.runLater(() -> {
                    warehousesList.setAll(bodegas);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> alerta("Error al cargar bodegas", e.getMessage()));
            }
        }).start();
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {
                List<DepositModel> depositos = service.obtenerDepositos();
                javafx.application.Platform.runLater(() -> {
                    depositsList.setAll(depositos);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> alerta("Error al cargar datos", e.getMessage()));
            }
        }).start();
    }

    private void guardar() {
        if (selectedDeposit == null) {
            alerta("Datos requeridos", "Debe seleccionar un Depósito de la lista.");
            return;
        }

        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String dir = txtDireccion.getText().trim();

        if (codigo.isEmpty() || nombre.isEmpty()) {
            alerta("Datos requeridos", "Código y Nombre son campos obligatorios.");
            return;
        }

        WarehouseModel warehouse = selectedWarehouse != null ? selectedWarehouse : new WarehouseModel();
        warehouse.setCodigo(codigo);
        warehouse.setNombre(nombre);
        warehouse.setDireccion(dir);
        warehouse.setDepositId(selectedDeposit.getId());

        if (isAdminSis && selectedDeposit.getCompanyId() != null) {
            warehouse.setCompanyId(selectedDeposit.getCompanyId());
        }

        new Thread(() -> {
            try {
                if (selectedWarehouse == null) {
                    service.crearBodega(warehouse);
                } else {
                    service.actualizarBodega(selectedWarehouse.getId(), warehouse);
                }
                javafx.application.Platform.runLater(() -> {
                    limpiarFormulario();
                    cargarDatos();
                    informacion("Éxito", "Bodega guardada correctamente.");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    alerta("Error al guardar", e.getMessage());
                });
            }
        }).start();
    }

    private void eliminar() {
        if (selectedWarehouse == null) {
            alerta("Selección requerida", "Debe seleccionar una bodega de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de eliminar esta bodega?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            new Thread(() -> {
                try {
                    service.eliminarBodega(selectedWarehouse.getId());
                    javafx.application.Platform.runLater(() -> {
                        limpiarFormulario();
                        cargarDatos();
                        informacion("Éxito", "Bodega eliminada.");
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
        selectedWarehouse = null;
        tablaBodegas.getSelectionModel().clearSelection();
        txtCodigo.clear();
        txtNombre.clear();
        txtDireccion.clear();
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
        lbl.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        return lbl;
    }
}
