package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.DepositModel;
import com.logistics.packinglist.model.RegionModel;
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

import java.util.ArrayList;
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
    private CheckBox chkUsarDireccionDeposito;
    private TextField txtDireccion;
    private ComboBox<String> cbRegion;
    private ComboBox<String> cbComuna;
    private List<RegionModel> listaRegiones = new ArrayList<>();
    private VBox formContainer;
    private Label lblSeleccionDeposito;

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
                return (d.getNombre() != null && d.getNombre().toLowerCase().contains(lower));
            });
        });

        tablaDepositos = new TableView<>();
        tablaDepositos.setItems(filteredDeposits);
        tablaDepositos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaDepositos.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<DepositModel, String> colDepNombre = new TableColumn<>("Depósito");
        colDepNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        
        tablaDepositos.getColumns().addAll(colDepNombre);
        VBox.setVgrow(tablaDepositos, Priority.ALWAYS);

        tablaDepositos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedDeposit = newSel;
            selectedWarehouse = null;
            limpiarFormulario();
            actualizarEstadoFormulario();
            
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
                        || (w.getDireccion() != null && w.getDireccion().toLowerCase().contains(lower));
            });
        });

        tablaBodegas = new TableView<>();
        tablaBodegas.setItems(filteredWarehouses);
        tablaBodegas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaBodegas.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<WarehouseModel, String> colBodNombre = new TableColumn<>("Bodega");
        colBodNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        TableColumn<WarehouseModel, String> colBodDir = new TableColumn<>("Dirección");
        colBodDir.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDireccion() != null ? c.getValue().getDireccion() : ""));
        TableColumn<WarehouseModel, String> colBodReg = new TableColumn<>("Región");
        colBodReg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegion() != null ? c.getValue().getRegion() : ""));
        TableColumn<WarehouseModel, String> colBodCom = new TableColumn<>("Comuna");
        colBodCom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getComuna() != null ? c.getValue().getComuna() : ""));
        
        tablaBodegas.getColumns().addAll(colBodNombre, colBodDir, colBodReg, colBodCom);
        VBox.setVgrow(tablaBodegas, Priority.ALWAYS);

        tablaBodegas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedWarehouse = newSel;
                txtNombre.setText(newSel.getNombre());

                boolean mismaDireccion = selectedDeposit != null 
                        && java.util.Objects.equals(selectedDeposit.getDireccion(), newSel.getDireccion())
                        && java.util.Objects.equals(selectedDeposit.getRegion(), newSel.getRegion())
                        && java.util.Objects.equals(selectedDeposit.getComuna(), newSel.getComuna());

                chkUsarDireccionDeposito.setSelected(mismaDireccion);
                txtDireccion.setDisable(mismaDireccion);
                cbRegion.setDisable(mismaDireccion);
                cbComuna.setDisable(mismaDireccion);

                txtDireccion.setText(newSel.getDireccion() != null ? newSel.getDireccion() : "");
                if (newSel.getRegion() != null && !newSel.getRegion().isEmpty()) {
                    cbRegion.getSelectionModel().select(newSel.getRegion());
                    if (newSel.getComuna() != null) {
                        cbComuna.getSelectionModel().select(newSel.getComuna());
                    }
                } else {
                    cbRegion.getSelectionModel().clearSelection();
                    cbComuna.getSelectionModel().clearSelection();
                }
                actualizarEstadoFormulario();
            }
        });

        detailPane.getChildren().addAll(lblDetailTitle, txtBusquedaBodegas, tablaBodegas);

        // --- FORMULARIO (DERECHA) ---
        VBox formPane = new VBox(12);
        formPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        
        Label lblFormTitle = new Label("Registro de Bodega");
        lblFormTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        lblSeleccionDeposito = new Label("← Seleccione un depósito obligatoriamente");
        lblSeleccionDeposito.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
        lblSeleccionDeposito.setManaged(true);
        lblSeleccionDeposito.setVisible(true);

        formContainer = new VBox(12);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 10, 0));

        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre de la Bodega");
        txtNombre.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-font-size: 11px;");
        
        chkUsarDireccionDeposito = new CheckBox("Usar dirección del depósito");
        chkUsarDireccionDeposito.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");
        chkUsarDireccionDeposito.setOnAction(e -> {
            if (chkUsarDireccionDeposito.isSelected()) {
                txtDireccion.setDisable(true);
                cbRegion.setDisable(true);
                cbComuna.setDisable(true);
                if (selectedDeposit != null) {
                    txtDireccion.setText(selectedDeposit.getDireccion() != null ? selectedDeposit.getDireccion() : "");
                    if (selectedDeposit.getRegion() != null) {
                        cbRegion.getSelectionModel().select(selectedDeposit.getRegion());
                        if (selectedDeposit.getComuna() != null) {
                            cbComuna.getSelectionModel().select(selectedDeposit.getComuna());
                        }
                    }
                }
            } else {
                txtDireccion.setDisable(false);
                cbRegion.setDisable(false);
                cbComuna.setDisable(false);
                txtDireccion.clear();
                cbRegion.getSelectionModel().clearSelection();
                cbComuna.getSelectionModel().clearSelection();
            }
        });

        txtDireccion = new TextField();
        txtDireccion.setPromptText("Dirección");
        txtDireccion.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-font-size: 11px;");

        cbRegion = new ComboBox<>();
        cbRegion.setPromptText("Seleccione Región");
        cbRegion.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbRegion.setMaxWidth(Double.MAX_VALUE);

        cbComuna = new ComboBox<>();
        cbComuna.setPromptText("Seleccione Comuna");
        cbComuna.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbComuna.setMaxWidth(Double.MAX_VALUE);

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

        int row = 0;
        grid.add(crearLabel("Nombre de la Bodega:"), 0, row++); grid.add(txtNombre, 0, row++);
        grid.add(chkUsarDireccionDeposito, 0, row++);
        grid.add(crearLabel("Dirección:"), 0, row++); grid.add(txtDireccion, 0, row++);
        grid.add(crearLabel("Región:"), 0, row++); grid.add(cbRegion, 0, row++);
        grid.add(crearLabel("Comuna:"), 0, row++); grid.add(cbComuna, 0, row++);

        ColumnConstraints colF = new ColumnConstraints();
        colF.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().add(colF);

        // Botones de acción
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
        actionRow.setPadding(new Insets(10, 0, 0, 0));

        formContainer.getChildren().add(grid);
        formPane.getChildren().addAll(lblFormTitle, lblSeleccionDeposito, formContainer);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(masterPane, detailPane, formPane);
        mainSplit.setDividerPositions(0.3, 0.65);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        root.getChildren().addAll(headerBox, mainSplit, actionRow);

        Scene scene = new Scene(root, 1080, 640);
        setScene(scene);
        setTitle("Bodegas y Zonas");
        com.logistics.packinglist.utils.ScreenUtil.fitDialogToScreen(this, getOwner(), 1080, 640, 850, 520);

        actualizarEstadoFormulario();
    }

    private void actualizarEstadoFormulario() {
        boolean noDeposit = selectedDeposit == null;
        formContainer.setDisable(noDeposit);
        btnAdd.setDisable(noDeposit);
        btnClear.setDisable(noDeposit);
        btnDelete.setDisable(noDeposit || selectedWarehouse == null);
        lblSeleccionDeposito.setVisible(noDeposit);
        lblSeleccionDeposito.setManaged(noDeposit);
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
                List<RegionModel> regiones = service.obtenerRegiones();
                List<DepositModel> depositos = service.obtenerDepositos();
                javafx.application.Platform.runLater(() -> {
                    listaRegiones = regiones;
                    cbRegion.getItems().clear();
                    for (RegionModel r : regiones) {
                        cbRegion.getItems().add(r.getRegion());
                    }
                    depositsList.setAll(depositos);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> alerta("Error al cargar datos", e.getMessage()));
            }
        }).start();
    }

    private void guardar() {
        if (selectedDeposit == null) {
            alerta("Depósito requerido", "Debe seleccionar un Depósito de la lista obligatoriamente.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String dir = txtDireccion.getText().trim();
        String reg = cbRegion.getValue() != null ? cbRegion.getValue() : "";
        String com = cbComuna.getValue() != null ? cbComuna.getValue() : "";

        if (nombre.isEmpty()) {
            alerta("Datos requeridos", "El Nombre de la bodega es obligatorio.");
            return;
        }

        WarehouseModel warehouse = selectedWarehouse != null ? selectedWarehouse : new WarehouseModel();
        if (warehouse.getCodigo() == null || warehouse.getCodigo().trim().isEmpty()) {
            warehouse.setCodigo(generarCodigo(nombre));
        }
        warehouse.setNombre(nombre);
        warehouse.setDireccion(dir);
        warehouse.setRegion(reg);
        warehouse.setComuna(com);
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
                    if (selectedDeposit != null) {
                        cargarBodegasDeDeposito(selectedDeposit.getId());
                    }
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
                        if (selectedDeposit != null) {
                            cargarBodegasDeDeposito(selectedDeposit.getId());
                        }
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
        txtNombre.clear();
        chkUsarDireccionDeposito.setSelected(false);
        txtDireccion.setDisable(false);
        cbRegion.setDisable(false);
        cbComuna.setDisable(false);
        txtDireccion.clear();
        cbRegion.getSelectionModel().clearSelection();
        cbComuna.getSelectionModel().clearSelection();
        actualizarEstadoFormulario();
    }

    private String generarCodigo(String nombre) {
        String prefijo = "BOD";
        if (nombre != null && !nombre.trim().isEmpty()) {
            String[] parts = nombre.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String p : parts) {
                if (!p.isEmpty() && Character.isLetterOrDigit(p.charAt(0))) {
                    sb.append(Character.toUpperCase(p.charAt(0)));
                }
            }
            if (sb.length() >= 2) {
                prefijo = sb.substring(0, Math.min(sb.length(), 4));
            }
        }
        return prefijo + "-" + (int)(Math.random() * 9000 + 1000);
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
