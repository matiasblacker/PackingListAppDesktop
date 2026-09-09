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
    private TextField txtCodigo, txtNombre, txtDireccion, txtRegion, txtComuna;

    private Button btnAdd, btnDelete, btnClear;

    private CompanyModel selectedCompany = null;
    private DepositModel selectedDeposit = null;

    public DepositosDialog(javafx.stage.Window owner) {
        initOwner(owner);
        this.isAdminSis = "ADMINSIS".equals(com.logistics.packinglist.service.AuthService.getInstance().getRole());

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
            txtBusquedaEmpresas.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
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
        txtBusquedaDepositos.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        txtBusquedaDepositos.textProperty().addListener((obs, old, newVal) -> {
            filteredDeposits.setPredicate(d -> {
                if (isAdminSis && (selectedCompany == null || !selectedCompany.getId().equals(d.getCompanyId()))) return false;
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
            if (newSel != null) {
                selectedDeposit = newSel;
                txtCodigo.setText(newSel.getCodigo());
                txtNombre.setText(newSel.getNombre());
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
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 10, 0));

        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del Depósito");
        txtNombre.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        
        txtCodigo = new TextField();
        txtCodigo.setPromptText("Código (e.g. DEP-01)");
        txtCodigo.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        
        txtDireccion = new TextField();
        txtDireccion.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        txtRegion = new TextField();
        txtRegion.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        txtComuna = new TextField();
        txtComuna.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");

        grid.add(crearLabel("Código:"), 0, 0); grid.add(txtCodigo, 0, 1);
        grid.add(crearLabel("Nombre:"), 0, 2); grid.add(txtNombre, 0, 3);
        grid.add(crearLabel("Dirección:"), 0, 4); grid.add(txtDireccion, 0, 5);
        grid.add(crearLabel("Región:"), 0, 6); grid.add(txtRegion, 0, 7);
        grid.add(crearLabel("Comuna:"), 0, 8); grid.add(txtComuna, 0, 9);

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

        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String dir = txtDireccion.getText().trim();
        String reg = txtRegion.getText().trim();
        String com = txtComuna.getText().trim();

        if (codigo.isEmpty() || nombre.isEmpty()) {
            alerta("Datos requeridos", "Código y Nombre son campos obligatorios.");
            return;
        }

        DepositModel model = selectedDeposit != null ? selectedDeposit : new DepositModel();
        model.setCodigo(codigo);
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
        txtCodigo.clear();
        txtNombre.clear();
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
        lbl.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        return lbl;
    }
}
