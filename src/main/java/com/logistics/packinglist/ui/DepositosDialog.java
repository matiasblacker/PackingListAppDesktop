package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.DepositModel;
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

public class DepositosDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();
    private final boolean isAdminSis;

    private final ObservableList<CompanyModel> companiesList;
    private final FilteredList<CompanyModel> filteredCompanies;

    private final ObservableList<DepositModel> depositsList;
    private final FilteredList<DepositModel> filteredDeposits;

    private TableView<CompanyModel> tablaEmpresas;
    private TextField txtBusquedaEmpresas;

    private TableView<DepositModel> tablaDepositos;
    private TextField txtBusquedaDepositos;

    // Campos de formulario
    private TextField txtNombre, txtDireccion, txtRegion, txtComuna;
    private CheckBox chkUsarDireccionEmpresa;
    private VBox formContainer;
    private Label lblSeleccionEmpresa;

    private Button btnAdd, btnDelete, btnClear;

    private CompanyModel selectedCompany = null;
    private DepositModel selectedDeposit = null;

    public DepositosDialog(javafx.stage.Window owner) {
        initOwner(owner);
        this.isAdminSis = "ADMINSIS".equalsIgnoreCase(com.logistics.packinglist.service.AuthService.getInstance().getRole());

        companiesList = FXCollections.observableArrayList();
        filteredCompanies = new FilteredList<>(companiesList, p -> true);

        depositsList = FXCollections.observableArrayList();
        filteredDeposits = new FilteredList<>(depositsList, p -> true);

        initUI();
        cargarDatos();
    }

    private void initUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header
        VBox headerBox = new VBox(5);
        Label lblTitle = new Label("Depósitos y Áreas");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label(isAdminSis ? "Seleccione una empresa para administrar sus depósitos." : "Administre los depósitos de su empresa.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        // --- MASTER LIST (EMPRESAS) (Solo AdminSis) ---
        if (isAdminSis) {
            VBox masterPane = new VBox(10);
            masterPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
            
            Label lblMasterTitle = new Label("Empresas");
            lblMasterTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

            txtBusquedaEmpresas = new TextField();
            txtBusquedaEmpresas.setPromptText("Buscar Empresa...");
            txtBusquedaEmpresas.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-font-size: 11px;");
            txtBusquedaEmpresas.textProperty().addListener((obs, old, newVal) -> {
                filteredCompanies.setPredicate(c -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String lower = newVal.toLowerCase();
                    return (c.getRazonSocial() != null && c.getRazonSocial().toLowerCase().contains(lower))
                            || (c.getRut() != null && c.getRut().toLowerCase().contains(lower));
                });
            });

            tablaEmpresas = new TableView<>();
            tablaEmpresas.setItems(filteredCompanies);
            tablaEmpresas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tablaEmpresas.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

            TableColumn<CompanyModel, String> colRut = new TableColumn<>("RUT");
            colRut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRut()));
            TableColumn<CompanyModel, String> colRazon = new TableColumn<>("Razón Social");
            colRazon.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRazonSocial()));
            
            tablaEmpresas.getColumns().addAll(colRut, colRazon);
            VBox.setVgrow(tablaEmpresas, Priority.ALWAYS);

            tablaEmpresas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                selectedCompany = newSel;
                selectedDeposit = null;
                limpiarFormulario();
                actualizarEstadoFormulario();
                
                if (newSel != null) {
                    filteredDeposits.setPredicate(d -> newSel.getId().equals(d.getCompanyId()));
                } else {
                    filteredDeposits.setPredicate(d -> false);
                }
            });

            masterPane.getChildren().addAll(lblMasterTitle, txtBusquedaEmpresas, tablaEmpresas);
            mainSplit.getItems().add(masterPane);
        }

        // --- DETAIL LIST (DEPÓSITOS) ---
        VBox detailPane = new VBox(10);
        detailPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        
        Label lblDetailTitle = new Label("Depósitos Registrados");
        lblDetailTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        txtBusquedaDepositos = new TextField();
        txtBusquedaDepositos.setPromptText("Buscar Depósito...");
        txtBusquedaDepositos.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-font-size: 11px;");
        txtBusquedaDepositos.textProperty().addListener((obs, old, newVal) -> {
            filteredDeposits.setPredicate(d -> {
                if (isAdminSis && (selectedCompany == null || !selectedCompany.getId().equals(d.getCompanyId()))) return false;
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return (d.getNombre() != null && d.getNombre().toLowerCase().contains(lower));
            });
        });

        tablaDepositos = new TableView<>();
        tablaDepositos.setItems(filteredDeposits);
        tablaDepositos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaDepositos.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<DepositModel, String> colDepNombre = new TableColumn<>("Nombre");
        colDepNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        TableColumn<DepositModel, String> colDepDir = new TableColumn<>("Dirección");
        colDepDir.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDireccion()));
        TableColumn<DepositModel, String> colDepReg = new TableColumn<>("Región");
        colDepReg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegion()));
        TableColumn<DepositModel, String> colDepCom = new TableColumn<>("Comuna");
        colDepCom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getComuna()));
        
        tablaDepositos.getColumns().addAll(colDepNombre, colDepDir, colDepReg, colDepCom);
        VBox.setVgrow(tablaDepositos, Priority.ALWAYS);

        tablaDepositos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedDeposit = newSel;
                txtNombre.setText(newSel.getNombre());
                chkUsarDireccionEmpresa.setSelected(false);
                txtDireccion.setText(newSel.getDireccion() != null ? newSel.getDireccion() : "");
                txtRegion.setText(newSel.getRegion() != null ? newSel.getRegion() : "");
                txtComuna.setText(newSel.getComuna() != null ? newSel.getComuna() : "");
            }
        });

        detailPane.getChildren().addAll(lblDetailTitle, txtBusquedaDepositos, tablaDepositos);
        mainSplit.getItems().add(detailPane);

        // --- FORMULARIO (DERECHA) ---
        VBox formPane = new VBox(12);
        formPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        
        Label lblFormTitle = new Label("Registro de Depósito");
        lblFormTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        
        lblSeleccionEmpresa = new Label("← Seleccione una empresa");
        lblSeleccionEmpresa.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
        lblSeleccionEmpresa.setManaged(false);
        lblSeleccionEmpresa.setVisible(false);

        formContainer = new VBox(12);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10); // reduced slightly
        grid.setPadding(new Insets(10, 0, 10, 0));

        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del Depósito");
        txtNombre.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-font-size: 11px;");
        
        chkUsarDireccionEmpresa = new CheckBox("Usar dirección de empresa");
        chkUsarDireccionEmpresa.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");
        chkUsarDireccionEmpresa.setOnAction(e -> {
            if (chkUsarDireccionEmpresa.isSelected()) {
                txtDireccion.setDisable(true);
                txtRegion.setDisable(true);
                txtComuna.setDisable(true);
                if (selectedCompany != null) {
                    txtDireccion.setText(selectedCompany.getDireccion() != null ? selectedCompany.getDireccion() : "");
                    txtRegion.setText(selectedCompany.getRegion() != null ? selectedCompany.getRegion() : "");
                    txtComuna.setText(selectedCompany.getComuna() != null ? selectedCompany.getComuna() : "");
                }
            } else {
                txtDireccion.setDisable(false);
                txtRegion.setDisable(false);
                txtComuna.setDisable(false);
                txtDireccion.clear();
                txtRegion.clear();
                txtComuna.clear();
            }
        });
        
        txtDireccion = new TextField();
        txtDireccion.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-font-size: 11px;");
        txtRegion = new TextField();
        txtRegion.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-font-size: 11px;");
        txtComuna = new TextField();
        txtComuna.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-font-size: 11px;");

        int row = 0;
        grid.add(crearLabel("Nombre del Depósito:"), 0, row++); grid.add(txtNombre, 0, row++);
        grid.add(chkUsarDireccionEmpresa, 0, row++);
        grid.add(crearLabel("Dirección:"), 0, row++); grid.add(txtDireccion, 0, row++);
        grid.add(crearLabel("Región:"), 0, row++); grid.add(txtRegion, 0, row++);
        grid.add(crearLabel("Comuna:"), 0, row++); grid.add(txtComuna, 0, row++);

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

        formContainer.getChildren().addAll(grid, actionBox);
        formPane.getChildren().addAll(lblFormTitle, lblSeleccionEmpresa, formContainer);
        mainSplit.getItems().add(formPane);

        if (isAdminSis) {
            mainSplit.setDividerPositions(0.3, 0.65);
        } else {
            mainSplit.setDividerPositions(0.5);
        }

        root.getChildren().addAll(headerBox, mainSplit);

        Scene scene = new Scene(root, 1080, 680);
        setScene(scene);
        setTitle("Depósitos y Áreas");

        actualizarEstadoFormulario();
    }

    private void actualizarEstadoFormulario() {
        if (isAdminSis && selectedCompany == null) {
            formContainer.setDisable(true);
            lblSeleccionEmpresa.setManaged(true);
            lblSeleccionEmpresa.setVisible(true);
        } else {
            formContainer.setDisable(false);
            lblSeleccionEmpresa.setManaged(false);
            lblSeleccionEmpresa.setVisible(false);
        }
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {
                if (isAdminSis) {
                    List<CompanyModel> empresas = service.obtenerEmpresas();
                    javafx.application.Platform.runLater(() -> companiesList.setAll(empresas));
                }

                List<DepositModel> depositos = service.obtenerDepositos();
                javafx.application.Platform.runLater(() -> {
                    depositsList.setAll(depositos);
                    
                    if (isAdminSis) {
                        if (selectedCompany == null) {
                            filteredDeposits.setPredicate(d -> false);
                        } else {
                            filteredDeposits.setPredicate(d -> selectedCompany.getId().equals(d.getCompanyId()));
                        }
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> alerta("Error al cargar datos", e.getMessage()));
            }
        }).start();
    }

    private void guardar() {
        if (isAdminSis && selectedCompany == null) {
            alerta("Datos requeridos", "Debe seleccionar una Empresa de la lista.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String dir = txtDireccion.getText().trim();
        String reg = txtRegion.getText().trim();
        String com = txtComuna.getText().trim();

        if (nombre.isEmpty()) {
            alerta("Datos requeridos", "El Nombre es un campo obligatorio.");
            return;
        }

        DepositModel model = selectedDeposit != null ? selectedDeposit : new DepositModel();
        model.setNombre(nombre);
        model.setDireccion(dir);
        model.setRegion(reg);
        model.setComuna(com);

        if (isAdminSis) {
            model.setCompanyId(selectedCompany.getId());
        }

        new Thread(() -> {
            try {
                if (selectedDeposit == null) {
                    service.crearDeposito(model);
                } else {
                    service.actualizarDeposito(selectedDeposit.getId(), model);
                }
                javafx.application.Platform.runLater(() -> {
                    limpiarFormulario();
                    cargarDatos();
                    informacion("Éxito", "Depósito guardado correctamente.");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> alerta("Error al guardar", e.getMessage()));
            }
        }).start();
    }

    private void eliminar() {
        if (selectedDeposit == null) {
            alerta("Selección requerida", "Debe seleccionar un depósito de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de eliminar este depósito?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            new Thread(() -> {
                try {
                    service.eliminarDeposito(selectedDeposit.getId());
                    javafx.application.Platform.runLater(() -> {
                        limpiarFormulario();
                        cargarDatos();
                        informacion("Éxito", "Depósito eliminado.");
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> alerta("Error al eliminar", e.getMessage()));
                }
            }).start();
        }
    }

    private void limpiarFormulario() {
        selectedDeposit = null;
        tablaDepositos.getSelectionModel().clearSelection();
        txtNombre.clear();
        chkUsarDireccionEmpresa.setSelected(false);
        txtDireccion.setDisable(false);
        txtRegion.setDisable(false);
        txtComuna.setDisable(false);
        txtDireccion.clear();
        txtRegion.clear();
        txtComuna.clear();
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
        lbl.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        return lbl;
    }
}
