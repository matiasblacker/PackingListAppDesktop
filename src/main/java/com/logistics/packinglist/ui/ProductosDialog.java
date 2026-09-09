package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.ProductModel;
import com.logistics.packinglist.model.SupplierModel;
import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.WarehouseModel;
import com.logistics.packinglist.model.LocationModel;
import com.logistics.packinglist.model.StockModel;
import com.logistics.packinglist.model.ProductFieldDefinitionModel;
import com.logistics.packinglist.service.MantenimientoService;
import com.logistics.packinglist.service.WebSocketManager;
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
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductosDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

                    private final ObservableList<ProductModel> observableList;
    private final FilteredList<ProductModel> filteredList;
    private final WebSocketManager.UpdateListener wsListener = action -> cargarDatos();

    private TableView<ProductModel> tabla;
    private TextField txtBusqueda;

    // Campos de formulario
    private ComboBox<SupplierModel> cbProveedor;

    // Selectores para Empresa y Bodega
    private ComboBox<CompanyModel> cbEmpresa;
    private ComboBox<WarehouseModel> cbBodega;

    // Campos estáticos de producto
    private TextField txtSku;
    private TextField txtNombre;
    private TextField txtCategoria;
    private TextField txtPacking;
    private TextField txtPrecio;
    private ComboBox<String> cbMoneda;
    private TextField txtPaisOrigen;
    private TextField txtImagenUrl;
    private Button btnSeleccionarImagen;
    private ImageView imgPreview;
    private File selectedImageFile = null;

    private TextField txtPeso;
    private TextField txtLargo;
    private TextField txtAncho;
    private TextField txtAlto;
    private TextField txtVolumen;

    private Button btnAdd;
    private Button btnDelete;
    private Button btnClear;

    private ProductModel selectedProduct = null;
    private final Map<String, SupplierModel> supplierMap = new HashMap<>();
    private final List<SupplierModel> allSuppliers = new ArrayList<>();
    private final Map<String, CompanyModel> companyMap = new HashMap<>();
    private final Map<String, WarehouseModel> warehouseMap = new HashMap<>();
    private final List<WarehouseModel> allWarehouses = new ArrayList<>();
    private final Map<String, String> locationToWarehouseMap = new HashMap<>();

    private GridPane grid;
    private Button btnConfigCampos;
    private Button btnExcel;
    private final List<ProductFieldDefinitionModel> activeFieldDefinitions = new ArrayList<>();
    private final Map<String, javafx.scene.Node> dynamicControlsMap = new HashMap<>();

    public ProductosDialog(Window owner) {
                                        this.observableList = FXCollections.observableArrayList();
        this.filteredList = new FilteredList<>(observableList, p -> true);

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Catálogo de Productos");

        setMinWidth(900);
        setMinHeight(650);

        construirUI();
        cargarDatos();

        WebSocketManager.getInstance().subscribe("PRODUCT", wsListener);
        WebSocketManager.getInstance().subscribe("SUPPLIER", wsListener);
        WebSocketManager.getInstance().subscribe("COMPANY", wsListener);

        setOnHiding(e -> {
            WebSocketManager.getInstance().unsubscribe("PRODUCT", wsListener);
            WebSocketManager.getInstance().unsubscribe("SUPPLIER", wsListener);
            WebSocketManager.getInstance().unsubscribe("COMPANY", wsListener);
        });
    }

    private void construirUI() {
        boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(com.logistics.packinglist.service.AuthService.getInstance().getRole());
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header Box
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label("Catálogo de Productos");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Gestión del catálogo maestro, precios, categorías y atributos dinámicos.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // Inicializar campos estáticos
        txtSku = new TextField();
        txtSku.setPromptText("Ej: SKU-100");
        txtSku.setDisable(true);

        txtNombre = new TextField();
        txtNombre.setPromptText("Ej: Nombre del Producto");
        txtNombre.setDisable(true);

        txtCategoria = new TextField();
        txtCategoria.setPromptText("Ej: CATEGORIA");
        txtCategoria.setDisable(true);

        txtPacking = new TextField();
        txtPacking.setPromptText("Ej: Caja x10");
        txtPacking.setDisable(true);

        txtPrecio = new TextField();
        txtPrecio.setPromptText("0.00");
        txtPrecio.setDisable(true);
        txtPrecio.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("\\d*(\\.\\d*)?")) {
                txtPrecio.setText(oldVal);
            }
        });

        cbMoneda = new ComboBox<>();
        cbMoneda.getItems().addAll("CLP", "USD");
        cbMoneda.setValue("CLP");
        cbMoneda.setDisable(true);

                txtPaisOrigen = new TextField();
        txtPaisOrigen.setPromptText("Ej: CL");
        txtPaisOrigen.setDisable(true);

        txtImagenUrl = new TextField();
        txtImagenUrl.setPromptText("URL de la Imagen");
        txtImagenUrl.setDisable(true);

        imgPreview = new ImageView();
        imgPreview.setFitWidth(40);
        imgPreview.setFitHeight(40);
        imgPreview.setPreserveRatio(true);

        txtPeso = new TextField();
        txtPeso.setPromptText("0.00");
        txtPeso.setText("0.0");
        txtPeso.setDisable(true);

        txtLargo = new TextField();
        txtLargo.setPromptText("Largo (cm)");
        txtLargo.setText("0.0");
        txtLargo.setDisable(true);

        txtAncho = new TextField();
        txtAncho.setPromptText("Ancho (cm)");
        txtAncho.setText("0.0");
        txtAncho.setDisable(true);

        txtAlto = new TextField();
        txtAlto.setPromptText("Alto (cm)");
        txtAlto.setText("0.0");
        txtAlto.setDisable(true);

        txtVolumen = new TextField();
        txtVolumen.setPromptText("Volumen (cm³)");
        txtVolumen.setText("0.0");
        txtVolumen.setEditable(false);
        txtVolumen.setDisable(true);

        javafx.beans.value.ChangeListener<String> calcVol = (obs, oldV, newV) -> {
            try {
                double l = Double.parseDouble(txtLargo.getText().trim());
                double a = Double.parseDouble(txtAncho.getText().trim());
                double h = Double.parseDouble(txtAlto.getText().trim());
                double vol = l * a * h;
                txtVolumen.setText(String.format(java.util.Locale.US, "%.2f", vol));
            } catch (Exception ignored) {
                txtVolumen.setText("0.00");
            }
        };
        txtLargo.textProperty().addListener(calcVol);
        txtAncho.textProperty().addListener(calcVol);
        txtAlto.textProperty().addListener(calcVol);
        root.setStyle("-fx-background-color: #f5f7fb;");


        // Top toolbar
        txtBusqueda = new TextField();
        txtBusqueda.setPromptText("Buscar por SKU o Nombre...");
        txtBusqueda.setPrefWidth(280);
        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(product -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return product.getSku().toLowerCase().contains(lower)
                        || (product.getNombre() != null && product.getNombre().toLowerCase().contains(lower));
            });
        });

        FontAwesomeIconView iconExcel = new FontAwesomeIconView(FontAwesomeIcon.FILE_EXCEL_ALT);
        iconExcel.setFill(Color.WHITE);
        btnExcel = new Button("", iconExcel);
        btnExcel.setStyle("-fx-background-color: #28a745; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        Tooltip.install(btnExcel, new Tooltip("Cargar Excel..."));
        btnExcel.setDisable(true);
        btnExcel.setOnAction(e -> {
            CompanyModel comp = null;
            if (isAdminSis) {
                comp = cbEmpresa.getValue();
            } else {
                String myCompanyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
                if (myCompanyId != null) {
                    comp = CompanyModel.builder().id(myCompanyId).build();
                }
            }
            WarehouseModel bod = cbBodega.getValue();
            SupplierModel prov = cbProveedor.getValue();
            if (comp == null || bod == null || prov == null) {
                mostrarWarning("Validación", "Debe seleccionar Empresa, Bodega y Proveedor antes de proceder.");
                return;
            }
            CargarExcelDialog diag = new CargarExcelDialog(this, comp.getId(), bod, prov, activeFieldDefinitions, () -> {
                cargarDatos();
                limpiarFormulario();
            });
            diag.showAndWait();
        });

        FontAwesomeIconView iconCogProd = new FontAwesomeIconView(FontAwesomeIcon.COG);
        iconCogProd.setFill(Color.web("#0F3E6E"));
        btnConfigCampos = new Button("Configurar Productos", iconCogProd);
        btnConfigCampos.setStyle("-fx-background-color: transparent; -fx-text-fill: #0F3E6E; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        btnConfigCampos.setOnAction(e -> abrirConfiguracionCampos());
        btnConfigCampos.setDisable(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(10, new Label("Buscar:"), txtBusqueda, spacer, btnExcel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Tabla
        tabla = new TableView<>();
        tabla.setItems(filteredList);
        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        VBox.setVgrow(tabla, Priority.ALWAYS);


        // Formulario
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 0, 10, 0));

        cbProveedor = new ComboBox<>();
        cbProveedor.setPromptText("Seleccione Proveedor");
        cbProveedor.setMaxWidth(Double.MAX_VALUE);
        cbProveedor.setOnAction(e -> {
            actualizarCamposDinamicos();
            actualizarTablaSegunSeleccion();
        });

        cbBodega = new ComboBox<>();
        cbBodega.setPromptText("Seleccione Bodega");
        cbBodega.setMaxWidth(Double.MAX_VALUE);
        cbBodega.setOnAction(e -> {
            actualizarCamposDinamicos();
            actualizarTablaSegunSeleccion();
        });

        if (isAdminSis) {
            cbEmpresa = new ComboBox<>();
            cbEmpresa.setPromptText("Seleccione Empresa");
            cbEmpresa.setMaxWidth(Double.MAX_VALUE);
            cbEmpresa.setOnAction(e -> {
                CompanyModel comp = cbEmpresa.getValue();
                if (comp != null) {
                    cbBodega.setDisable(false);
                    filtrarBodegasPorEmpresa();
                } else {
                    cbBodega.getSelectionModel().clearSelection();
                    cbBodega.setDisable(true);
                }
                filtrarProveedoresPorEmpresa();
                actualizarCamposDinamicos();
                actualizarTablaSegunSeleccion();
            });

            grid.add(crearLabel("Empresa:"), 0, 0);
            grid.add(cbEmpresa, 1, 0);

            grid.add(crearLabel("Bodega:"), 2, 0);
            grid.add(cbBodega, 3, 0);

            grid.add(crearLabel("Proveedor:"), 0, 1);
            grid.add(cbProveedor, 1, 1);
        } else {
            grid.add(crearLabel("Bodega:"), 0, 0);
            grid.add(cbBodega, 1, 0);

            grid.add(crearLabel("Proveedor:"), 2, 0);
            grid.add(cbProveedor, 3, 0);
        }


        // Alinear columnas
        ColumnConstraints col1 = new ColumnConstraints(90);
        ColumnConstraints col2 = new ColumnConstraints(250);
        ColumnConstraints col3 = new ColumnConstraints(90);
        ColumnConstraints col4 = new ColumnConstraints(250);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);

        // Botones de acción de pie de página
        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSave.setFill(Color.WHITE);
        btnAdd = new Button("", iconSave);
        btnAdd.setStyle("-fx-background-color: cornflowerblue; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnAdd.setOnAction(e -> guardar());
        Tooltip.install(btnAdd, new Tooltip("Guardar"));
        Tooltip.install(btnAdd, new Tooltip("Guardar Producto"));

        FontAwesomeIconView iconClear = new FontAwesomeIconView(FontAwesomeIcon.UNDO);
        iconClear.setFill(Color.WHITE);
        btnClear = new Button("", iconClear);
        btnClear.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnClear.setOnAction(e -> limpiarFormulario());
        Tooltip.install(btnClear, new Tooltip("Limpiar"));
        Tooltip.install(btnClear, new Tooltip("Limpiar Formulario"));

        FontAwesomeIconView iconDel = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDel.setFill(Color.WHITE);
        btnDelete = new Button("", iconDel);
        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnDelete.setOnAction(e -> eliminar());
        Tooltip.install(btnDelete, new Tooltip("Eliminar"));
        Tooltip.install(btnDelete, new Tooltip("Eliminar Producto"));

        HBox actionRow = new HBox(8, btnAdd, btnClear, btnDelete);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(15, 0, 0, 0));

        // Evento selección de tabla
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedProduct = newSel;
                cbBodega.setDisable(false);
                cbProveedor.setDisable(false);



                txtImagenUrl.setText(newSel.getImagenUrl() != null ? newSel.getImagenUrl() : "");
                txtPeso.setText(String.valueOf(newSel.getPeso() != null ? newSel.getPeso() : 0.0));
                txtLargo.setText(String.valueOf(newSel.getLargo() != null ? newSel.getLargo() : 0.0));
                txtAncho.setText(String.valueOf(newSel.getAncho() != null ? newSel.getAncho() : 0.0));
                txtAlto.setText(String.valueOf(newSel.getAlto() != null ? newSel.getAlto() : 0.0));
                txtVolumen.setText(String.valueOf(newSel.getVolumen() != null ? newSel.getVolumen() : 0.0));
                if (newSel.getImagenUrl() != null && !newSel.getImagenUrl().isEmpty()) {
                    try {
                        imgPreview.setImage(new Image(newSel.getImagenUrl(), 40, 40, true, true));
                    } catch (Exception ignored) {}
                } else {
                    imgPreview.setImage(null);
                }

                if (newSel.getSupplierId() != null) {
                    cbProveedor.setValue(supplierMap.get(newSel.getSupplierId()));
                } else {
                    cbProveedor.getSelectionModel().clearSelection();
                }

                if (isAdminSis) {
                    CompanyModel comp = companyMap.get(newSel.getCompanyId());
                    cbEmpresa.setValue(comp);
                    filtrarBodegasPorEmpresa();
                }

                // Cargar la bodega asociada al stock de este producto
                new Thread(() -> {
                    try {
                        List<StockModel> stocks = service.obtenerStocksPorProducto(newSel.getId());
                        if (!stocks.isEmpty()) {
                            String locId = stocks.get(0).getLocationId();
                            String whId = locationToWarehouseMap.get(locId);
                            if (whId != null) {
                                javafx.application.Platform.runLater(() -> {
                                    cbBodega.setValue(warehouseMap.get(whId));
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });

        grid.setPadding(new Insets(10, 0, 10, 0));
        VBox.setVgrow(grid, Priority.NEVER);

        // --- LADO IZQUIERDO: LISTADO (WHITE CARD) ---
        VBox leftPane = new VBox(10);
        leftPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        VBox.setVgrow(tabla, Priority.ALWAYS);

        Label lblListTitle = new Label("Catálogo de Productos");
        lblListTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        leftPane.getChildren().addAll(lblListTitle, topRow, tabla);

        // --- LADO DERECHO: FORMULARIO (WHITE CARD EN SCROLLPANE) ---
        VBox rightPane = new VBox(12);
        rightPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");
        
        Label lblFormTitle = new Label("Detalle / Registro de Producto");
        lblFormTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Region spacerForm = new Region();
        HBox.setHgrow(spacerForm, Priority.ALWAYS);

        HBox formHeaderBox = new HBox(8, lblFormTitle, spacerForm, btnConfigCampos);
        formHeaderBox.setAlignment(Pos.CENTER_LEFT);
        
        rightPane.getChildren().addAll(formHeaderBox, grid);

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

        Scene scene = new Scene(root, 1080, 680);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setScene(scene);
    }

    private void filtrarBodegasPorEmpresa() {
        if (cbEmpresa == null) return;
        CompanyModel comp = cbEmpresa.getValue();
        cbBodega.getItems().clear();
        if (comp != null) {
            for (WarehouseModel w : allWarehouses) {
                if (comp.getId().equals(w.getCompanyId())) {
                    cbBodega.getItems().add(w);
                }
            }
        }
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {
                boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(com.logistics.packinglist.service.AuthService.getInstance().getRole());
                List<CompanyModel> companies = new ArrayList<>();
                if (isAdminSis) {
                    companies = service.obtenerEmpresas();
                }
                List<SupplierModel> suppliers = service.obtenerProveedores();
                List<ProductModel> products = service.obtenerProductos();
                List<WarehouseModel> warehouses = service.obtenerBodegas();
                List<LocationModel> locations = service.obtenerUbicaciones();

                final List<CompanyModel> finalCompanies = companies;
                javafx.application.Platform.runLater(() -> {
                    supplierMap.clear();
                    allSuppliers.clear();
                    for (SupplierModel s : suppliers) {
                        if (s.getId() != null) {
                            supplierMap.put(s.getId(), s);
                            supplierMap.put(s.getId().toLowerCase(), s);
                        }
                        allSuppliers.add(s);
                    }
                    filtrarProveedoresPorEmpresa();


                    allWarehouses.clear();
                    allWarehouses.addAll(warehouses);
                    warehouseMap.clear();
                    cbBodega.getItems().clear();
                    for (WarehouseModel w : warehouses) {
                        warehouseMap.put(w.getId(), w);
                    }

                    if (isAdminSis) {
                        companyMap.clear();
                        cbEmpresa.getItems().clear();
                        for (CompanyModel c : finalCompanies) {
                            companyMap.put(c.getId(), c);
                            cbEmpresa.getItems().add(c);
                        }
                    } else {
                        // Cargar bodegas del tenant directamente
                        String myCompanyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
                        String myWarehouseId = com.logistics.packinglist.service.AuthService.getInstance().getWarehouseId();
                        WarehouseModel selectedWh = null;
                        for (WarehouseModel w : warehouses) {
                            if (myCompanyId.equals(w.getCompanyId())) {
                                cbBodega.getItems().add(w);
                                if (myWarehouseId != null && myWarehouseId.equalsIgnoreCase(w.getId())) {
                                    selectedWh = w;
                                }
                            }
                        }
                        if (selectedWh != null) {
                            cbBodega.setValue(selectedWh);
                        }
                    }

                    locationToWarehouseMap.clear();
                    for (LocationModel loc : locations) {
                        locationToWarehouseMap.put(loc.getId(), loc.getWarehouseId());
                    }

                    cbProveedor.setDisable(false);
                    cbBodega.setDisable(false);
                    filtrarProveedoresPorEmpresa();
                    actualizarCamposDinamicos();
                    actualizarTablaSegunSeleccion();
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
        SupplierModel prov = cbProveedor.getValue();
        WarehouseModel bod = cbBodega.getValue();

        boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(com.logistics.packinglist.service.AuthService.getInstance().getRole());
        String companyId = null;
        if (isAdminSis) {
            CompanyModel comp = cbEmpresa.getValue();
            if (comp != null) {
                companyId = comp.getId();
            }
        } else {
            companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        }

        if (companyId == null) {
            mostrarWarning("Validación", "Debe seleccionar una empresa.");
            return;
        }

        // Recoger y validar valores estándar
        String skuVal = txtSku.getText().trim();
        String nombreVal = txtNombre.getText().trim();
        String categoriaVal = txtCategoria.getText().trim();
        String packingVal = txtPacking.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String monedaVal = cbMoneda.getValue();
        String paisOrigenVal = txtPaisOrigen.getText().trim();

        if (skuVal.isEmpty()) {
            mostrarWarning("Validación", "Debe ingresar el SKU del producto.");
            return;
        }
        if (nombreVal.isEmpty()) {
            mostrarWarning("Validación", "Debe ingresar el Nombre/Descripción del producto.");
            return;
        }
        if (categoriaVal.isEmpty()) {
            mostrarWarning("Validación", "Debe ingresar la Categoría del producto.");
            return;
        }
        if (packingVal.isEmpty()) {
            mostrarWarning("Validación", "Debe ingresar el Packing del producto.");
            return;
        }
        if (precioStr.isEmpty()) {
            mostrarWarning("Validación", "Debe ingresar el Precio del producto.");
            return;
        }
        double precioVal;
        try {
            precioVal = Double.parseDouble(precioStr);
        } catch (Exception e) {
            mostrarWarning("Validación", "El precio ingresado no es válido.");
            return;
        }
        if (paisOrigenVal.isEmpty()) {
            mostrarWarning("Validación", "Debe ingresar el País de Origen.");
            return;
        }

        String pesoStr = txtPeso.getText().trim();
        String largoStr = txtLargo.getText().trim();
        String anchoStr = txtAncho.getText().trim();
        String altoStr = txtAlto.getText().trim();
        String imgUrlVal = txtImagenUrl.getText().trim();

        double pesoVal = 0.0;
        double largoVal = 0.0;
        double anchoVal = 0.0;
        double altoVal = 0.0;
        try {
            if (!pesoStr.isEmpty()) pesoVal = Double.parseDouble(pesoStr);
            if (!largoStr.isEmpty()) largoVal = Double.parseDouble(largoStr);
            if (!anchoStr.isEmpty()) anchoVal = Double.parseDouble(anchoStr);
            if (!altoStr.isEmpty()) altoVal = Double.parseDouble(altoStr);
        } catch (Exception e) {
            mostrarWarning("Validación", "Los valores numéricos de peso y dimensiones deben ser válidos.");
            return;
        }
        double volumenVal = largoVal * anchoVal * altoVal;

        final double finalPeso = pesoVal;
        final double finalLargo = largoVal;
        final double finalAncho = anchoVal;
        final double finalAlto = altoVal;
        final double finalVolumen = volumenVal;
        final String finalImgUrl = imgUrlVal;

        // Recoger y validar valores de campos dinámicos
        Map<String, String> atributosValores = new HashMap<>();
        for (ProductFieldDefinitionModel field : activeFieldDefinitions) {
            javafx.scene.Node ctrl = dynamicControlsMap.get(field.getFieldKey());
            String val = "";
            if (ctrl instanceof CheckBox cb) {
                val = String.valueOf(cb.isSelected());
            } else if (ctrl instanceof DatePicker dp) {
                val = dp.getValue() != null ? dp.getValue().toString() : "";
            } else if (ctrl instanceof TextField tf) {
                val = tf.getText().trim();
            }

            if (field.isRequired() && val.isEmpty()) {
                mostrarWarning("Validación", "El campo personalizado '" + field.getFieldLabel() + "' es obligatorio.");
                return;
            }
            atributosValores.put(field.getFieldKey(), val);
        }

        final String finalSku = skuVal;
        final String finalNombre = nombreVal;
        final double finalPrecio = precioVal;
        final String finalMoneda = monedaVal;
        final String finalCategoria = categoriaVal;
        final String finalPacking = packingVal;
        final String finalPaisOrigen = paisOrigenVal;
        final String finalCompanyId = companyId;

        new Thread(() -> {
            try {
                if (selectedProduct == null) {
                    ProductModel model = new ProductModel();
                    model.setCompanyId(finalCompanyId);
                    model.setSku(finalSku);
                    model.setNombre(finalNombre);
                    model.setPrecio(finalPrecio);
                    model.setMoneda(finalMoneda);
                    model.setPacking(finalPacking);
                    model.setPaisOrigen(finalPaisOrigen);
                    model.setUnidadMedida("UNIDADES");
                    model.setBarcode(finalSku);
                    model.setSupplierId(prov != null ? prov.getId() : null);
                    model.setCategoria(finalCategoria);
                    model.setImagenUrl(finalImgUrl);
                    model.setPeso(finalPeso);
                    model.setLargo(finalLargo);
                    model.setAncho(finalAncho);
                    model.setAlto(finalAlto);
                    model.setVolumen(finalVolumen);
                    model.setAtributosPersonalizados(atributosValores);

                    ProductModel created = service.crearProducto(model);

                    javafx.application.Platform.runLater(() -> {
                        mostrarInformacion("Éxito", "Producto registrado exitosamente.");
                        limpiarFormulario();
                        cargarDatos();
                    });
                } else {
                    ProductModel model = selectedProduct;
                    model.setSku(finalSku);
                    model.setNombre(finalNombre);
                    model.setPrecio(finalPrecio);
                    model.setMoneda(finalMoneda);
                    model.setPacking(finalPacking);
                    model.setPaisOrigen(finalPaisOrigen);
                    model.setUnidadMedida("UNIDADES");
                    model.setBarcode(finalSku);
                    model.setSupplierId(prov != null ? prov.getId() : null);
                    model.setCategoria(finalCategoria);
                    model.setImagenUrl(finalImgUrl);
                    model.setPeso(finalPeso);
                    model.setLargo(finalLargo);
                    model.setAncho(finalAncho);
                    model.setAlto(finalAlto);
                    model.setVolumen(finalVolumen);
                    model.setAtributosPersonalizados(atributosValores);

                    ProductModel updated = service.actualizarProducto(model.getId(), model);

                    javafx.application.Platform.runLater(() -> {
                        mostrarInformacion("Éxito", "Producto actualizado exitosamente.");
                        limpiarFormulario();
                        cargarDatos();
                    });
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
        if (selectedProduct == null) {
            mostrarWarning("Selección", "Debe seleccionar un producto de la lista.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de que desea eliminar el producto " + selectedProduct.getNombre() + "?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    service.eliminarProducto(selectedProduct.getId());
                    javafx.application.Platform.runLater(() -> {
                        mostrarInformacion("Éxito", "Producto eliminado exitosamente.");
                        limpiarFormulario();
                        actualizarTablaSegunSeleccion();
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

    private void cargarExcelProductos(File file) {
        boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(com.logistics.packinglist.service.AuthService.getInstance().getRole());
        String companyId = null;
        if (isAdminSis) {
            CompanyModel comp = cbEmpresa.getValue();
            if (comp != null) {
                companyId = comp.getId();
            }
        } else {
            companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        }

        if (companyId == null) {
            mostrarWarning("Validación", "Debe seleccionar una empresa antes de cargar el Excel.");
            return;
        }

        WarehouseModel bod = cbBodega.getValue();
        final String finalCompanyId = companyId;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {
             
            Sheet sheet = wb.getSheetAt(0);
            List<ProductModel> tempProducts = new ArrayList<>();
            List<Integer> initialStocks = new ArrayList<>();
            List<String> supplierRuts = new ArrayList<>();

            boolean primeraFila = true;
            for (Row row : sheet) {
                if (primeraFila) {
                    primeraFila = false;
                    continue;
                }
                String sku = getCellStr(row, 0);
                String nombre = getCellStr(row, 1);
                String cat = getCellStr(row, 2);
                String packing = getCellStr(row, 3);
                String paisOrigen = getCellStr(row, 4);
                String precioListaStr = getCellStr(row, 5);
                String rutProv = getCellStr(row, 6);
                String cantStr = getCellStr(row, 7);

                if (sku.isEmpty() || nombre.isEmpty()) {
                    continue;
                }

                double precioListaVal = 0;
                try {
                    precioListaVal = Double.parseDouble(precioListaStr);
                } catch (Exception ignored) {}

                int cant = 0;
                try {
                    cant = (int) Double.parseDouble(cantStr);
                } catch (Exception ignored) {}

                ProductModel pm = new ProductModel();
                pm.setSku(sku);
                pm.setNombre(nombre);
                pm.setPrecio(precioListaVal); // Default sell price to list price
                pm.setPrecioLista(precioListaVal);
                pm.setPacking(packing.isEmpty() ? null : packing);
                pm.setPaisOrigen(paisOrigen.isEmpty() ? null : paisOrigen);
                pm.setUnidadMedida("UNIDADES");
                pm.setBarcode(sku); // Barcode derived from SKU
                pm.setCategoria(cat.isEmpty() ? "OTROS" : cat.toUpperCase());
                
                tempProducts.add(pm);
                initialStocks.add(cant);
                supplierRuts.add(rutProv);
            }

            if (tempProducts.isEmpty()) {
                mostrarWarning("Excel Vacío", "No se encontraron productos válidos en el archivo.");
                return;
            }

            // Alerta de confirmación antes de la carga
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar Carga de Productos");
            confirm.setHeaderText("Carga Masiva desde Excel");
            confirm.setContentText(String.format("Se detectaron %d productos en el archivo.\n" +
                    "¿Está seguro de que desea cargarlos en la empresa y bodega seleccionada?", tempProducts.size()));

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        List<SupplierModel> sups = service.obtenerProveedores();
                        Map<String, String> rutToSupplierId = new HashMap<>();
                        for (SupplierModel s : sups) {
                            if (finalCompanyId.equals(s.getCompanyId())) {
                                rutToSupplierId.put(s.getRut().replace(".", "").replace("-", "").toLowerCase(), s.getId());
                            }
                        }

                        List<ProductModel> createdProducts = new ArrayList<>();
                        for (int i = 0; i < tempProducts.size(); i++) {
                            ProductModel pm = tempProducts.get(i);
                            pm.setCompanyId(finalCompanyId);
                            
                            String rutVal = supplierRuts.get(i);
                            if (rutVal != null && !rutVal.isEmpty()) {
                                String cleanRut = rutVal.replace(".", "").replace("-", "").toLowerCase();
                                pm.setSupplierId(rutToSupplierId.get(cleanRut));
                            }

                            ProductModel created = service.crearProducto(pm);
                            createdProducts.add(created);

                            // Desacoplado: No inicializar stock durante la carga de Excel
                        }

                        javafx.application.Platform.runLater(() -> {
                            mostrarInformacion("Éxito", "Se cargaron " + createdProducts.size() + " productos exitosamente.");
                            cargarDatos();
                            limpiarFormulario();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            mostrarError("Error al cargar Excel", "Ocurrió un error: " + ex.getMessage());
                        });
                    }
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error de archivo", "No se pudo leer el archivo Excel: " + e.getMessage());
        }
    }

    private String getCellStr(Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case FORMULA -> {
                try { yield cell.getStringCellValue().trim(); }
                catch (Exception e) {
                    try { yield String.valueOf((long) cell.getNumericCellValue()); }
                    catch (Exception e2) { yield ""; }
                }
            }
            default -> "";
        };
    }

    private void limpiarFormulario() {
        selectedProduct = null;
        tabla.getSelectionModel().clearSelection();

        txtSku.clear();
        txtNombre.clear();
        txtCategoria.clear();
        txtPacking.clear();
        txtPrecio.clear();
        cbMoneda.setValue("CLP");
        txtPaisOrigen.clear();

        for (javafx.scene.Node ctrl : dynamicControlsMap.values()) {
            if (ctrl instanceof CheckBox cb) {
                cb.setSelected(false);
            } else if (ctrl instanceof DatePicker dp) {
                dp.setValue(null);
            } else if (ctrl instanceof TextField tf) {
                tf.clear();
            }
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

    private void abrirConfiguracionCampos() {
        boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(com.logistics.packinglist.service.AuthService.getInstance().getRole());
        String companyId = null;
        if (isAdminSis) {
            CompanyModel comp = cbEmpresa.getValue();
            if (comp != null) {
                companyId = comp.getId();
            }
        } else {
            companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        }

        if (companyId == null) {
            mostrarWarning("Validación", "Debe seleccionar una empresa para configurar campos.");
            return;
        }

        WarehouseModel bod = cbBodega.getValue();
        SupplierModel prov = cbProveedor.getValue();

        String warehouseId = bod != null ? bod.getId() : null;
        String supplierId = prov != null ? prov.getId() : null;

        ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, "PRODUCTO", warehouseId, supplierId, this::actualizarCamposDinamicos);
        diag.showAndWait();
    }

    private void actualizarCamposDinamicos() {
        boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(com.logistics.packinglist.service.AuthService.getInstance().getRole());
        CompanyModel comp = null;
        if (isAdminSis) {
            comp = cbEmpresa.getValue();
        } else {
            String myCompanyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
            if (myCompanyId != null) {
                comp = CompanyModel.builder().id(myCompanyId).build();
            }
        }

        WarehouseModel bod = cbBodega.getValue();
        SupplierModel prov = cbProveedor.getValue();

        final int startRow = isAdminSis ? 2 : 1;

        // Si no se han seleccionado todos los datos, limpiamos los campos y columnas dinámicas inmediatamente
        if (comp == null || bod == null || prov == null) {
            if (grid != null) {
                grid.getChildren().removeIf(node -> {
                    Integer rowIndex = GridPane.getRowIndex(node);
                    return rowIndex != null && rowIndex >= startRow;
                });
            }
            activeFieldDefinitions.clear();
            dynamicControlsMap.clear();
            tabla.getColumns().clear();
            if (btnConfigCampos != null) {
                btnConfigCampos.setDisable(true);
            }
            if (btnExcel != null) {
                btnExcel.setDisable(true);
            }
            txtSku.clear();
            txtSku.setDisable(true);
            txtNombre.clear();
            txtNombre.setDisable(true);
            txtCategoria.clear();
            txtCategoria.setDisable(true);
            txtPacking.clear();
            txtPacking.setDisable(true);
            txtPrecio.clear();
            txtPrecio.setDisable(true);
            cbMoneda.setValue("CLP");
            cbMoneda.setDisable(true);
            txtPaisOrigen.clear();
            txtPaisOrigen.setDisable(true);
            txtImagenUrl.clear();
            txtImagenUrl.setDisable(true);
            txtPeso.setText("0.0");
            txtPeso.setDisable(true);
            txtLargo.setText("0.0");
            txtLargo.setDisable(true);
            txtAncho.setText("0.0");
            txtAncho.setDisable(true);
            txtAlto.setText("0.0");
            txtAlto.setDisable(true);
            txtVolumen.setText("0.0");
            txtVolumen.setDisable(true);
            imgPreview.setImage(null);
            return;
        }

        if (btnConfigCampos != null) {
            btnConfigCampos.setDisable(false);
        }

        final String finalCompanyId = comp.getId();
        final String warehouseId = bod.getId();
        final String supplierId = prov.getId();

        new Thread(() -> {
            try {
                List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(finalCompanyId, "PRODUCTO", warehouseId, null);
                javafx.application.Platform.runLater(() -> {
                    if (grid != null) {
                        grid.getChildren().removeIf(node -> {
                            Integer rowIndex = GridPane.getRowIndex(node);
                            return rowIndex != null && rowIndex >= startRow;
                        });
                    }
                    dynamicControlsMap.clear();
                    activeFieldDefinitions.clear();
                    activeFieldDefinitions.addAll(fields);
                    actualizarColumnasTabla(fields);
                    if (btnExcel != null) {
                        btnExcel.setDisable(false);
                    }

                    // Renderizar campos estándar obligatorios
                    int currentRow = startRow;

                    Label lblSku = crearLabel("SKU *");
                    txtSku.setDisable(false);
                    txtSku.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
                    txtSku.setMaxWidth(Double.MAX_VALUE);
                    grid.add(lblSku, 0, currentRow);
                    grid.add(txtSku, 1, currentRow);

                    Label lblNombre = crearLabel("Nombre/Desc. *");
                    txtNombre.setDisable(false);
                    txtNombre.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
                    txtNombre.setMaxWidth(Double.MAX_VALUE);
                    grid.add(lblNombre, 2, currentRow);
                    grid.add(txtNombre, 3, currentRow);

                    currentRow++;

                    Label lblCat = crearLabel("Categoría *");
                    txtCategoria.setDisable(false);
                    txtCategoria.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
                    txtCategoria.setMaxWidth(Double.MAX_VALUE);
                    grid.add(lblCat, 0, currentRow);
                    grid.add(txtCategoria, 1, currentRow);

                    Label lblPacking = crearLabel("Packing *");
                    txtPacking.setDisable(false);
                    txtPacking.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
                    txtPacking.setMaxWidth(Double.MAX_VALUE);
                    grid.add(lblPacking, 2, currentRow);
                    grid.add(txtPacking, 3, currentRow);

                    currentRow++;

                    Label lblPrecio = crearLabel("Precio *");
                    txtPrecio.setDisable(false);
                    txtPrecio.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
                    cbMoneda.setDisable(false);
                    cbMoneda.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
                    HBox priceBox = new HBox(5, txtPrecio, cbMoneda);
                    txtPrecio.setPrefWidth(160);
                    cbMoneda.setPrefWidth(80);
                    grid.add(lblPrecio, 0, currentRow);
                    grid.add(priceBox, 1, currentRow);

                    Label lblPais = crearLabel("País Origen *");
                    txtPaisOrigen.setDisable(false);
                    txtPaisOrigen.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
                    txtPaisOrigen.setMaxWidth(Double.MAX_VALUE);
                    grid.add(lblPais, 2, currentRow);
                    grid.add(txtPaisOrigen, 3, currentRow);

                    currentRow++;

                    // Imagen
                    Label lblImg = crearLabel("Imagen");
                    txtImagenUrl.setDisable(false);
                    txtImagenUrl.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");

                    btnSeleccionarImagen = new Button("Subir...", new FontAwesomeIconView(FontAwesomeIcon.IMAGE));
                    btnSeleccionarImagen.setStyle("-fx-background-color: cornflowerblue; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                    btnSeleccionarImagen.getGraphic().setStyle("-fx-fill: white;");
                    btnSeleccionarImagen.setOnAction(e -> seleccionarYSubirImagen());

                    HBox imgBox = new HBox(6, txtImagenUrl, btnSeleccionarImagen, imgPreview);
                    HBox.setHgrow(txtImagenUrl, Priority.ALWAYS);
                    grid.add(lblImg, 0, currentRow);
                    grid.add(imgBox, 1, currentRow);
                    GridPane.setColumnSpan(imgBox, 3);

                    currentRow++;

                    // Peso y Dimensiones
                    Label lblPeso = crearLabel("Peso (kg) *");
                    txtPeso.setDisable(false);
                    txtPeso.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
                    txtPeso.setMaxWidth(Double.MAX_VALUE);
                    grid.add(lblPeso, 0, currentRow);
                    grid.add(txtPeso, 1, currentRow);

                    Label lblDim = crearLabel("Dimensiones (L x A x A cm) *");
                    txtLargo.setDisable(false);
                    txtLargo.setPrefWidth(65);
                    txtLargo.setStyle("-fx-background-radius: 4px; -fx-border-color: #cbd5e1;");
                    txtAncho.setDisable(false);
                    txtAncho.setPrefWidth(65);
                    txtAncho.setStyle("-fx-background-radius: 4px; -fx-border-color: #cbd5e1;");
                    txtAlto.setDisable(false);
                    txtAlto.setPrefWidth(65);
                    txtAlto.setStyle("-fx-background-radius: 4px; -fx-border-color: #cbd5e1;");

                    HBox dimBox = new HBox(4, txtLargo, new Label("x"), txtAncho, new Label("x"), txtAlto);
                    dimBox.setAlignment(Pos.CENTER_LEFT);
                    grid.add(lblDim, 2, currentRow);
                    grid.add(dimBox, 3, currentRow);

                    currentRow++;

                    Label lblVol = crearLabel("Volumen (cm³) *");
                    txtVolumen.setDisable(false);
                    txtVolumen.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9; -fx-text-fill: #0f172a;");
                    txtVolumen.setMaxWidth(Double.MAX_VALUE);
                    grid.add(lblVol, 0, currentRow);
                    grid.add(txtVolumen, 1, currentRow);

                    currentRow++;
                    int currentCol = 0;

                    for (ProductFieldDefinitionModel field : fields) {
                        Label label = crearLabel(field.getFieldLabel() + (field.isRequired() ? " *" : ""));

                        javafx.scene.Node control;
                        switch (field.getFieldType()) {
                            case "BOOLEAN" -> {
                                CheckBox cb = new CheckBox();
                                control = cb;
                            }
                            case "DATE" -> {
                                DatePicker dp = new DatePicker();
                                dp.setMaxWidth(Double.MAX_VALUE);
                                control = dp;
                            }
                            case "NUMBER" -> {
                                TextField tf = new TextField();
                                tf.setPromptText("Ingrese número");
                                tf.textProperty().addListener((obs, oldVal, newVal) -> {
                                    if (!newVal.matches("\\d*(\\.\\d*)?")) {
                                        tf.setText(oldVal);
                                    }
                                });
                                control = tf;
                            }
                            default -> {
                                TextField tf = new TextField();
                                tf.setPromptText("Ingrese texto");
                                control = tf;
                            }
                        }

                        dynamicControlsMap.put(field.getFieldKey(), control);
                        
                        grid.add(label, currentCol, currentRow);
                        grid.add(control, currentCol + 1, currentRow);

                        currentCol += 2;
                        if (currentCol >= 4) {
                            currentCol = 0;
                            currentRow++;
                        }
                    }

                    // Rellenar valores si hay producto seleccionado
                    if (selectedProduct != null) {
                        txtSku.setText(selectedProduct.getSku() != null ? selectedProduct.getSku() : "");
                        txtNombre.setText(selectedProduct.getNombre() != null ? selectedProduct.getNombre() : "");
                        txtCategoria.setText(selectedProduct.getCategoria() != null ? selectedProduct.getCategoria() : "");
                        txtPacking.setText(selectedProduct.getPacking() != null ? selectedProduct.getPacking() : "");
                        txtPrecio.setText(selectedProduct.getPrecio() != null ? String.valueOf(selectedProduct.getPrecio()) : "");
                        cbMoneda.setValue(selectedProduct.getMoneda() != null ? selectedProduct.getMoneda() : "CLP");
                        txtPaisOrigen.setText(selectedProduct.getPaisOrigen() != null ? selectedProduct.getPaisOrigen() : "");

                        Map<String, String> values = selectedProduct.getAtributosPersonalizados();
                        if (values != null) {
                            for (Map.Entry<String, javafx.scene.Node> entry : dynamicControlsMap.entrySet()) {
                                String key = entry.getKey();
                                javafx.scene.Node ctrl = entry.getValue();
                                String val = values.getOrDefault(key, "");

                                if (ctrl instanceof CheckBox cb) {
                                    cb.setSelected(Boolean.parseBoolean(val));
                                } else if (ctrl instanceof DatePicker dp) {
                                    if (val != null && !val.isEmpty()) {
                                        try {
                                            dp.setValue(java.time.LocalDate.parse(val));
                                        } catch (Exception ignored) {}
                                    } else {
                                        dp.setValue(null);
                                    }
                                } else if (ctrl instanceof TextField tf) {
                                    tf.setText(val);
                                }
                            }
                        }
                    }
                    tabla.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void actualizarColumnasTabla(List<ProductFieldDefinitionModel> fields) {
        // Desvincular bindings anteriores para evitar fugas de memoria y conflictos de diseño
        for (TableColumn<ProductModel, ?> col : tabla.getColumns()) {
            col.prefWidthProperty().unbind();
        }
        tabla.getColumns().clear();

        int totalCols = 6 + fields.size();
        final double colShare = 1.0 / totalCols;

        // 1. Columnas estándar fijas
        TableColumn<ProductModel, String> colSku = new TableColumn<>("SKU");
        colSku.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSku()));
        colSku.prefWidthProperty().bind(tabla.widthProperty().subtract(15).multiply(colShare));
        tabla.getColumns().add(colSku);

        TableColumn<ProductModel, String> colNombre = new TableColumn<>("Nombre/Descripción");
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colNombre.prefWidthProperty().bind(tabla.widthProperty().subtract(15).multiply(colShare));
        tabla.getColumns().add(colNombre);

        TableColumn<ProductModel, String> colCat = new TableColumn<>("Categoría");
        colCat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoria()));
        colCat.prefWidthProperty().bind(tabla.widthProperty().subtract(15).multiply(colShare));
        tabla.getColumns().add(colCat);

        TableColumn<ProductModel, String> colPacking = new TableColumn<>("Packing");
        colPacking.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPacking()));
        colPacking.prefWidthProperty().bind(tabla.widthProperty().subtract(15).multiply(colShare));
        tabla.getColumns().add(colPacking);

        TableColumn<ProductModel, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(c -> {
            Double p = c.getValue().getPrecio();
            String m = c.getValue().getMoneda();
            if (p == null) return new SimpleStringProperty("-");
            return new SimpleStringProperty("$ " + p + " " + (m != null ? m : "CLP"));
        });
        colPrecio.prefWidthProperty().bind(tabla.widthProperty().subtract(15).multiply(colShare));
        tabla.getColumns().add(colPrecio);

        TableColumn<ProductModel, String> colPais = new TableColumn<>("País Origen");
        colPais.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaisOrigen()));
        colPais.prefWidthProperty().bind(tabla.widthProperty().subtract(15).multiply(colShare));
        tabla.getColumns().add(colPais);

        // 2. Columnas dinámicas según los atributos personalizados configurados
        for (ProductFieldDefinitionModel field : fields) {
            TableColumn<ProductModel, String> col = new TableColumn<>(field.getFieldLabel());
            col.setCellValueFactory(c -> {
                Map<String, String> attrs = c.getValue().getAtributosPersonalizados();
                String val = (attrs != null) ? attrs.getOrDefault(field.getFieldKey(), "") : "";
                
                // Mapear booleanos a "Sí" o "No"
                if ("BOOLEAN".equals(field.getFieldType())) {
                    if ("true".equalsIgnoreCase(val)) return new SimpleStringProperty("Sí");
                    if ("false".equalsIgnoreCase(val)) return new SimpleStringProperty("No");
                }
                return new SimpleStringProperty(val != null && !val.isEmpty() ? val : "-");
            });
            col.prefWidthProperty().bind(tabla.widthProperty().subtract(15).multiply(colShare));
            tabla.getColumns().add(col);
        }
    }

    private void actualizarTablaSegunSeleccion() {
        boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(com.logistics.packinglist.service.AuthService.getInstance().getRole());
        
        CompanyModel comp = null;
        if (isAdminSis) {
            comp = cbEmpresa.getValue();
        } else {
            String myCompanyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
            if (myCompanyId != null) {
                comp = CompanyModel.builder().id(myCompanyId).build();
            }
        }
        
        SupplierModel prov = cbProveedor.getValue();
        
        final String targetCompanyId = comp != null ? comp.getId() : null;
        final String targetSupplierId = prov != null ? prov.getId() : null;
        
        new Thread(() -> {
            try {
                javafx.application.Platform.runLater(() -> {
                    tabla.setPlaceholder(new javafx.scene.control.ProgressIndicator());
                    observableList.clear();
                });
                
                List<ProductModel> allProducts = service.obtenerProductos();
                
                List<ProductModel> filtered = new java.util.ArrayList<>();
                for (ProductModel p : allProducts) {
                    if (targetCompanyId != null && p.getCompanyId() != null && !targetCompanyId.equalsIgnoreCase(p.getCompanyId())) {
                        continue;
                    }
                    if (targetSupplierId != null && (p.getSupplierId() == null || !targetSupplierId.equalsIgnoreCase(p.getSupplierId()))) {
                        continue;
                    }
                    filtered.add(p);
                }
                
                javafx.application.Platform.runLater(() -> {
                    observableList.addAll(filtered);
                    tabla.setPlaceholder(new javafx.scene.control.Label("No se encontraron productos registrados."));
                    tabla.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    tabla.setPlaceholder(new javafx.scene.control.Label("Error al cargar los datos: " + e.getMessage()));
                });
            }
        }).start();
    }

    private void filtrarProveedoresPorEmpresa() {
        if (cbProveedor == null) return;
        boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(com.logistics.packinglist.service.AuthService.getInstance().getRole());
        String companyId = null;
        if (isAdminSis) {
            CompanyModel comp = cbEmpresa.getValue();
            if (comp != null) {
                companyId = comp.getId();
            }
        } else {
            companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        }

        cbProveedor.getItems().clear();
        if (companyId != null) {
            for (SupplierModel s : allSuppliers) {
                if (companyId.equals(s.getCompanyId())) {
                    cbProveedor.getItems().add(s);
                }
            }
        }
    }

        private void seleccionarYSubirImagen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar Imagen de Producto");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File f = chooser.showOpenDialog(this);
        if (f != null) {
            selectedImageFile = f;
            try {
                Image img = new Image(f.toURI().toString(), 40, 40, true, true);
                imgPreview.setImage(img);
            } catch (Exception ignored) {}

            String whId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
            String prodId = selectedProduct != null ? selectedProduct.getId() : null;
            new Thread(() -> {
                try {
                    String url = service.subirImagenProducto(whId, prodId, f);
                    javafx.application.Platform.runLater(() -> {
                        txtImagenUrl.setText(url);
                        mostrarInformacion("Éxito", "Imagen subida correctamente a Firebase Storage.");
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        mostrarError("Error al subir imagen", ex.getMessage());
                    });
                }
            }).start();
        }
    }

    private Label crearLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        return lbl;
    }
}

