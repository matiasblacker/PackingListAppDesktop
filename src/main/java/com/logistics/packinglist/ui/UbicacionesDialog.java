package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.LocationModel;
import com.logistics.packinglist.model.StockModel;
import com.logistics.packinglist.model.WarehouseModel;
import com.logistics.packinglist.model.WarehouseZoneModel;
import com.logistics.packinglist.service.MantenimientoService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.logistics.packinglist.utils.ScreenUtil;
import java.util.*;

public class UbicacionesDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

                private final ObservableList<LocationModel> locationsList;

    private TreeView<Object> treeView;
    private TableView<LocationModel> tabla;

    // Formulario Ubicación
    private ComboBox<String> cbTipoSoporte;
    private TextField txtPasillo;
    private TextField txtAltura;
    private TextField txtPosicion;
    private TextField txtCodigoGenerado;

    private Button btnGuardarLoc;
    private Button btnEliminarLoc;
    private Button btnLimpiarLoc;
    private Button btnGenerarLote;

    // Contexto activo
    private WarehouseModel selectedWarehouseContext = null;
    private WarehouseZoneModel selectedZoneContext = null;
    private LocationModel selectedLocation = null;

    private final Map<String, WarehouseModel> warehouseMap = new HashMap<>();
    private final Map<String, List<WarehouseZoneModel>> warehouseZonesMap = new HashMap<>();

    public UbicacionesDialog(Window owner) {
                                this.locationsList = FXCollections.observableArrayList();

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Diseño de Espacios y Ubicaciones Físicas (WMS)");

        setMinWidth(950);
        setMinHeight(520);

        construirUI();
        cargarDatos();
    }

    private void construirUI() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header Box
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label("Diseño de Espacios y Ubicaciones Físicas");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label lblSub = new Label("Gestión de estructura de bodegas, sectores y matriz de coordenadas de almacenamiento.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // --- LADO IZQUIERDO: Estructura de Bodega (TreeView) ---
        VBox leftPane = new VBox(10);
        leftPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        Label lblTreeTitle = new Label("Estructura de Bodegas");
        lblTreeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");

        treeView = new TreeView<>();
        treeView.setShowRoot(false);
        treeView.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        VBox.setVgrow(treeView, Priority.ALWAYS);

        // Barra de acciones para Zonas/Sectores (Íconos Blancos Solo Ícono)
        FontAwesomeIconView iconPlusZ = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        iconPlusZ.setFill(Color.WHITE);
        Button btnNuevaZona = new Button("", iconPlusZ);
        btnNuevaZona.setStyle("-fx-background-color: #28a745; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnNuevaZona.setOnAction(e -> crearNuevaZona());
        Tooltip.install(btnNuevaZona, new Tooltip("Nueva Zona / Sector"));

        FontAwesomeIconView iconEditZ = new FontAwesomeIconView(FontAwesomeIcon.EDIT);
        iconEditZ.setFill(Color.WHITE);
        Button btnEditarZona = new Button("", iconEditZ);
        btnEditarZona.setStyle("-fx-background-color: #ffc107; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnEditarZona.setOnAction(e -> editarZonaSeleccionada());
        Tooltip.install(btnEditarZona, new Tooltip("Editar Zona / Sector"));

        FontAwesomeIconView iconDelZ = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDelZ.setFill(Color.WHITE);
        Button btnEliminarZona = new Button("", iconDelZ);
        btnEliminarZona.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnEliminarZona.setOnAction(e -> eliminarZonaSeleccionada());
        Tooltip.install(btnEliminarZona, new Tooltip("Eliminar Zona / Sector"));

        HBox zoneActions = new HBox(5, btnNuevaZona, btnEditarZona, btnEliminarZona);
        zoneActions.setAlignment(Pos.CENTER);

        leftPane.getChildren().addAll(lblTreeTitle, treeView, zoneActions);

        // --- LADO DERECHO: Tabla de Ubicaciones e Formulario ---
        VBox rightPane = new VBox(12);
        rightPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        Label lblContextPath = new Label("Seleccione una bodega o zona del árbol");
        lblContextPath.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #334155;");

        // Tabla de ubicaciones
        tabla = new TableView<>();
        tabla.setItems(locationsList);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        TableColumn<LocationModel, String> colCodigo = new TableColumn<>("Código Ubicación");
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigoUbicacion()));
        colCodigo.setPrefWidth(180);

        TableColumn<LocationModel, String> colSoporte = new TableColumn<>("Tipo Soporte");
        colSoporte.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoSoporte()));
        colSoporte.setPrefWidth(120);

        TableColumn<LocationModel, String> colPasillo = new TableColumn<>("Pasillo");
        colPasillo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPasillo()));
        colPasillo.setPrefWidth(100);

        TableColumn<LocationModel, String> colAltura = new TableColumn<>("Nivel / Altura");
        colAltura.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAltura()));
        colAltura.setPrefWidth(100);

        TableColumn<LocationModel, String> colPosicion = new TableColumn<>("Posición");
        colPosicion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPosicion()));
        colPosicion.setPrefWidth(100);

        tabla.getColumns().addAll(colCodigo, colSoporte, colPasillo, colPosicion, colAltura);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        // FORMULARIO DE EDICIÓN / DETALLE
        TitledPane formContainer = new TitledPane("Detalle de Ubicación Física", crearFormularioUbicacion());
        formContainer.setCollapsible(false);
        formContainer.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: bold;");

        rightPane.getChildren().addAll(lblContextPath, tabla, formContainer);

        ScrollPane rightScroll = new ScrollPane(rightPane);
        rightScroll.setFitToWidth(true);
        rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(leftPane, rightScroll);
        mainSplit.setDividerPositions(0.3);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        root.getChildren().addAll(headerBox, mainSplit);

        // Eventos del TreeView
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Object value = newVal.getValue();
                if (value instanceof WarehouseModel wh) {
                    selectedWarehouseContext = wh;
                    selectedZoneContext = null;
                    lblContextPath.setText("Bodega: " + wh.getNombre() + " (Todos los sectores)");
                    cargarUbicacionesDeBodega(wh.getId());
                } else if (value instanceof WarehouseZoneModel zone) {
                    selectedZoneContext = zone;
                    selectedWarehouseContext = warehouseMap.get(zone.getWarehouseId());
                    lblContextPath.setText("Bodega: " + selectedWarehouseContext.getNombre() + " > Zona: " + zone.getNombre());
                    cargarUbicacionesDeZona(zone.getId());
                }
            } else {
                selectedWarehouseContext = null;
                selectedZoneContext = null;
                lblContextPath.setText("Seleccione una bodega o zona del árbol");
                locationsList.clear();
            }
            limpiarFormularioUbicacion();
        });

        // Eventos de la Tabla
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedLocation = newVal;
            if (newVal != null) {
                cbTipoSoporte.setValue(newVal.getTipoSoporte());
                txtPasillo.setText(newVal.getPasillo() != null ? newVal.getPasillo() : "");
                txtAltura.setText(newVal.getAltura() != null ? newVal.getAltura() : "");
                txtPosicion.setText(newVal.getPosicion() != null ? newVal.getPosicion() : "");
                txtCodigoGenerado.setText(newVal.getCodigoUbicacion() != null ? newVal.getCodigoUbicacion() : "");
            }
        });

        Scene scene = new Scene(root, 1080, 640);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setScene(scene);
        ScreenUtil.fitDialogToScreen(this, getOwner(), 1080, 640, 950, 520);
    }

    private GridPane crearFormularioUbicacion() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        cbTipoSoporte = new ComboBox<>(FXCollections.observableArrayList("RACK", "ESTANTE", "SUELO", "CONTENEDOR"));
        cbTipoSoporte.setValue("RACK");
        cbTipoSoporte.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px; -fx-border-radius: 4px;");
        cbTipoSoporte.setMaxWidth(Double.MAX_VALUE);

        txtPasillo = new TextField();
        txtPasillo.setPromptText("Ej: A, B, 01");
        txtPasillo.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");

        txtAltura = new TextField();
        txtAltura.setPromptText("Ej: Nivel 1");
        txtAltura.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");

        txtPosicion = new TextField();
        txtPosicion.setPromptText("Ej: A3, B1");
        txtPosicion.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");

        txtCodigoGenerado = new TextField();
        txtCodigoGenerado.setEditable(false);
        txtCodigoGenerado.setPromptText("Generado por coordenadas");
        txtCodigoGenerado.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        // Listeners para previsualizar código
        Runnable updateCodePreview = () -> {
            String p = txtPasillo.getText().trim();
            String a = txtAltura.getText().trim();
            String pos = txtPosicion.getText().trim();
            List<String> parts = new ArrayList<>();
            if (!p.isEmpty()) parts.add(p);
            if (!pos.isEmpty()) parts.add("P" + pos);
            if (!a.isEmpty()) parts.add("N" + a);
            txtCodigoGenerado.setText(String.join("-", parts));
        };

        txtPasillo.textProperty().addListener((o, ov, nv) -> updateCodePreview.run());
        txtAltura.textProperty().addListener((o, ov, nv) -> updateCodePreview.run());
        txtPosicion.textProperty().addListener((o, ov, nv) -> updateCodePreview.run());

        Label lblSop = new Label("Soporte:");
        lblSop.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblPas = new Label("Pasillo:");
        lblPas.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblPos = new Label("Posición:");
        lblPos.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblAlt = new Label("Nivel/Altura:");
        lblAlt.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        Label lblCod = new Label("Código Ubic.:");
        lblCod.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");

        grid.add(lblSop, 0, 0);
        grid.add(cbTipoSoporte, 1, 0);
        grid.add(lblPas, 2, 0);
        grid.add(txtPasillo, 3, 0);

        grid.add(lblPos, 0, 1);
        grid.add(txtPosicion, 1, 1);
        grid.add(lblAlt, 2, 1);
        grid.add(txtAltura, 3, 1);

        grid.add(lblCod, 0, 2);
        grid.add(txtCodigoGenerado, 1, 2, 3, 1);

        ColumnConstraints c1 = new ColumnConstraints(90);
        ColumnConstraints c2 = new ColumnConstraints(180);
        ColumnConstraints c3 = new ColumnConstraints(90);
        ColumnConstraints c4 = new ColumnConstraints(180);
        grid.getColumnConstraints().addAll(c1, c2, c3, c4);

        // Acciones
        FontAwesomeIconView iconSaveLoc = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSaveLoc.setFill(Color.WHITE);
        btnGuardarLoc = new Button("", iconSaveLoc);
        btnGuardarLoc.setStyle("-fx-background-color: cornflowerblue; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnGuardarLoc.setOnAction(e -> guardarUbicacion());
        Tooltip.install(btnGuardarLoc, new Tooltip("Guardar Ubicación"));

        FontAwesomeIconView iconClearLoc = new FontAwesomeIconView(FontAwesomeIcon.UNDO);
        iconClearLoc.setFill(Color.WHITE);
        btnLimpiarLoc = new Button("", iconClearLoc);
        btnLimpiarLoc.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnLimpiarLoc.setOnAction(e -> limpiarFormularioUbicacion());
        Tooltip.install(btnLimpiarLoc, new Tooltip("Limpiar Formulario"));

        FontAwesomeIconView iconDelLoc = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDelLoc.setFill(Color.WHITE);
        btnEliminarLoc = new Button("", iconDelLoc);
        btnEliminarLoc.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnEliminarLoc.setOnAction(e -> eliminarUbicacion());
        Tooltip.install(btnEliminarLoc, new Tooltip("Eliminar Ubicación"));

        FontAwesomeIconView iconBatch = new FontAwesomeIconView(FontAwesomeIcon.COGS);
        iconBatch.setFill(Color.WHITE);
        btnGenerarLote = new Button("", iconBatch);
        btnGenerarLote.setStyle("-fx-background-color: #28a745; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnGenerarLote.setOnAction(e -> abrirGeneradorLote());
        Tooltip.install(btnGenerarLote, new Tooltip("Generación en Lote"));

        HBox btnBoxLoc = new HBox(8, btnGuardarLoc, btnLimpiarLoc, btnEliminarLoc, btnGenerarLote);
        btnBoxLoc.setAlignment(Pos.CENTER_RIGHT);
        grid.add(btnBoxLoc, 0, 3, 4, 1);

        return grid;
    }

    // ==========================================
    // CARGA DE DATOS & ÁRBOL
    // ==========================================
    private void cargarDatos() {
        new Thread(() -> {
            try {
                List<WarehouseModel> bodegas = service.obtenerBodegas();
                warehouseMap.clear();
                warehouseZonesMap.clear();

                for (WarehouseModel wh : bodegas) {
                    warehouseMap.put(wh.getId(), wh);
                    List<WarehouseZoneModel> zones = service.obtenerZonasPorBodega(wh.getId());
                    warehouseZonesMap.put(wh.getId(), zones);
                }

                javafx.application.Platform.runLater(() -> construirArbol(bodegas));
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> mostrarError("Error de carga", "No se pudieron obtener los datos del servidor: " + e.getMessage()));
            }
        }).start();
    }

    private void construirArbol(List<WarehouseModel> bodegas) {
        TreeItem<Object> rootItem = new TreeItem<>("Bodegas");
        treeView.setRoot(rootItem);

        for (WarehouseModel wh : bodegas) {
            TreeItem<Object> whNode = new TreeItem<>(wh, new FontAwesomeIconView(FontAwesomeIcon.HOME));
            rootItem.getChildren().add(whNode);

            List<WarehouseZoneModel> zones = warehouseZonesMap.getOrDefault(wh.getId(), Collections.emptyList());
            // Agregar recursivamente a partir de parentZoneId = null
            agregarNodosZonas(whNode, zones, null);
            whNode.setExpanded(true);
        }
    }

    private void agregarNodosZonas(TreeItem<Object> parentNode, List<WarehouseZoneModel> zones, String parentZoneId) {
        for (WarehouseZoneModel z : zones) {
            boolean matches = (parentZoneId == null && z.getParentZoneId() == null)
                    || (parentZoneId != null && parentZoneId.equals(z.getParentZoneId()));
            if (matches) {
                TreeItem<Object> zoneNode = new TreeItem<>(z, new FontAwesomeIconView(FontAwesomeIcon.FOLDER));
                parentNode.getChildren().add(zoneNode);
                agregarNodosZonas(zoneNode, zones, z.getId());
            }
        }
    }

    private void cargarUbicacionesDeBodega(String warehouseId) {
        new Thread(() -> {
            try {
                List<LocationModel> locs = service.obtenerUbicacionesPorBodega(warehouseId);
                javafx.application.Platform.runLater(() -> {
                    locationsList.clear();
                    locationsList.addAll(locs);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void cargarUbicacionesDeZona(String zoneId) {
        new Thread(() -> {
            try {
                List<LocationModel> locs = service.obtenerUbicacionesPorZona(zoneId);
                javafx.application.Platform.runLater(() -> {
                    locationsList.clear();
                    locationsList.addAll(locs);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ==========================================
    // CRUD UBICACIONES
    // ==========================================
    private void guardarUbicacion() {
        if (selectedWarehouseContext == null) {
            mostrarWarning("Validación", "Debe seleccionar una Bodega o Zona en el árbol.");
            return;
        }

        String pasillo = txtPasillo.getText().trim();
        String altura = txtAltura.getText().trim();
        String posicion = txtPosicion.getText().trim();

        if (pasillo.isEmpty()) {
            mostrarWarning("Validación", "El campo Pasillo es obligatorio para guardar.");
            return;
        }

        String targetCode = txtCodigoGenerado.getText().trim();
        if (targetCode.isEmpty()) {
            mostrarWarning("Validación", "Debe generar o ingresar las coordenadas para el código de ubicación.");
            return;
        }

        // Validar que el código no se repita en la bodega activa
        try {
            List<LocationModel> existingLocs = service.obtenerUbicacionesPorBodega(selectedWarehouseContext.getId());
            if (existingLocs != null) {
                for (LocationModel existing : existingLocs) {
                    if (existing.getCodigoUbicacion() != null && existing.getCodigoUbicacion().equalsIgnoreCase(targetCode)) {
                        if (selectedLocation == null || !existing.getId().equalsIgnoreCase(selectedLocation.getId())) {
                            mostrarWarning("Código Duplicado", "Ya existe una ubicación física registrada con el código '" + targetCode + "' en esta bodega.");
                            return;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        LocationModel model = selectedLocation != null ? selectedLocation : new LocationModel();
        model.setWarehouseId(selectedWarehouseContext.getId());
        model.setCompanyId(selectedWarehouseContext.getCompanyId());
        model.setZoneId(selectedZoneContext != null ? selectedZoneContext.getId() : null);
        model.setTipoSoporte(cbTipoSoporte.getValue());
        model.setPasillo(pasillo);
        model.setEstanteria(null);
        model.setAltura(altura.isEmpty() ? null : altura);
        model.setPosicion(posicion.isEmpty() ? null : posicion);
        model.setCodigoUbicacion(txtCodigoGenerado.getText());

        new Thread(() -> {
            try {
                if (selectedLocation == null) {
                    LocationModel created = service.crearUbicacion(model);
                    javafx.application.Platform.runLater(() -> {
                        locationsList.add(created);
                        mostrarInformacion("Éxito", "Ubicación física creada exitosamente.");
                        limpiarFormularioUbicacion();
                    });
                } else {
                    LocationModel updated = service.actualizarUbicacion(model.getId(), model);
                    javafx.application.Platform.runLater(() -> {
                        int idx = locationsList.indexOf(selectedLocation);
                        if (idx >= 0) {
                            locationsList.set(idx, updated);
                        }
                        mostrarInformacion("Éxito", "Ubicación física actualizada exitosamente.");
                        limpiarFormularioUbicacion();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> mostrarError("Error", "No se pudo guardar la ubicación: " + e.getMessage()));
            }
        }).start();
    }

    private void eliminarUbicacion() {
        if (selectedLocation == null) {
            mostrarWarning("Validación", "Seleccione una ubicación de la tabla.");
            return;
        }

        new Thread(() -> {
            try {
                List<StockModel> stocks = service.obtenerStocksPorUbicacion(selectedLocation.getId());
                int totalStock = 0;
                if (stocks != null) {
                    for (StockModel s : stocks) {
                        if (s.getCantidad() != null && s.getCantidad() > 0) {
                            totalStock += s.getCantidad();
                        }
                    }
                }
                final int fTotalStock = totalStock;
                javafx.application.Platform.runLater(() -> {
                    if (fTotalStock > 0) {
                        mostrarWarning("Imposible Eliminar", "No se puede eliminar la ubicación '" + selectedLocation.getCodigoUbicacion() + "' porque contiene " + fTotalStock + " unidad(es) de productos en stock.");
                        return;
                    }

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar la ubicación " + selectedLocation.getCodigoUbicacion() + "?", ButtonType.YES, ButtonType.NO);
                    alert.setTitle("Confirmar eliminación");
                    alert.setHeaderText(null);
                    if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                        new Thread(() -> {
                            try {
                                service.eliminarUbicacion(selectedLocation.getId());
                                javafx.application.Platform.runLater(() -> {
                                    locationsList.remove(selectedLocation);
                                    limpiarFormularioUbicacion();
                                    mostrarInformacion("Éxito", "Ubicación eliminada correctamente.");
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                javafx.application.Platform.runLater(() -> mostrarError("Error", "No se pudo eliminar la ubicación: " + e.getMessage()));
                            }
                        }).start();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> mostrarError("Error", "No se pudo verificar el stock de la ubicación: " + ex.getMessage()));
            }
        }).start();
    }

    private void limpiarFormularioUbicacion() {
        selectedLocation = null;
        tabla.getSelectionModel().clearSelection();
        cbTipoSoporte.setValue("RACK");
        txtPasillo.clear();
        txtAltura.clear();
        txtPosicion.clear();
        txtCodigoGenerado.clear();
    }

    // ==========================================
    // CRUD ZONAS / SECTORES
    // ==========================================
    private void crearNuevaZona() {
        if (selectedWarehouseContext == null) {
            mostrarWarning("Estructura", "Debe seleccionar la Bodega o Zona padre del árbol donde desea agregar la nueva zona.");
            return;
        }
        mostrarDialogoZona(null, selectedWarehouseContext, selectedZoneContext != null ? selectedZoneContext.getId() : null);
    }

    private void editarZonaSeleccionada() {
        if (selectedZoneContext == null) {
            mostrarWarning("Estructura", "Debe seleccionar una Zona en el árbol para editar.");
            return;
        }
        mostrarDialogoZona(selectedZoneContext, selectedWarehouseContext, selectedZoneContext.getParentZoneId());
    }

    private void eliminarZonaSeleccionada() {
        if (selectedZoneContext == null) {
            mostrarWarning("Estructura", "Debe seleccionar una Zona en el árbol para eliminar.");
            return;
        }

        new Thread(() -> {
            try {
                List<WarehouseZoneModel> allZonesInWh = warehouseZonesMap.get(selectedWarehouseContext.getId());
                List<String> allZoneIdsInTree = obtenerTodasLasSubzonasIds(selectedZoneContext.getId(), allZonesInWh);

                int totalZoneStock = 0;
                for (String zid : allZoneIdsInTree) {
                    List<LocationModel> locsInZone = service.obtenerUbicacionesPorZona(zid);
                    if (locsInZone != null) {
                        for (LocationModel loc : locsInZone) {
                            List<StockModel> stocks = service.obtenerStocksPorUbicacion(loc.getId());
                            if (stocks != null) {
                                for (StockModel s : stocks) {
                                    if (s.getCantidad() != null && s.getCantidad() > 0) {
                                        totalZoneStock += s.getCantidad();
                                    }
                                }
                            }
                        }
                    }
                }
                final int fTotalZoneStock = totalZoneStock;
                javafx.application.Platform.runLater(() -> {
                    if (fTotalZoneStock > 0) {
                        mostrarWarning("Imposible Eliminar Zona", "No se puede eliminar la zona/sector '" + selectedZoneContext.getNombre() + "' ni sus sub-zonas porque contienen ubicaciones con un total de " + fTotalZoneStock + " unidad(es) de productos en stock.");
                        return;
                    }

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar el sector o zona '" + selectedZoneContext.getNombre() + "'? Se eliminarán recursivamente sus subzonas.", ButtonType.YES, ButtonType.NO);
                    alert.setTitle("Eliminar Zona");
                    alert.setHeaderText(null);
                    if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                        new Thread(() -> {
                            try {
                                service.eliminarZona(selectedZoneContext.getId());
                                javafx.application.Platform.runLater(() -> {
                                    cargarDatos();
                                    mostrarInformacion("Éxito", "Zona eliminada correctamente.");
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                javafx.application.Platform.runLater(() -> mostrarError("Error", "No se pudo eliminar el sector: " + e.getMessage()));
                            }
                        }).start();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> mostrarError("Error", "No se pudo verificar el stock de las ubicaciones de la zona: " + ex.getMessage()));
            }
        }).start();
    }

    private List<String> obtenerTodasLasSubzonasIds(String zoneId, List<WarehouseZoneModel> allZonesInWh) {
        List<String> result = new ArrayList<>();
        result.add(zoneId);
        if (allZonesInWh != null) {
            for (WarehouseZoneModel z : allZonesInWh) {
                if (zoneId.equals(z.getParentZoneId())) {
                    result.addAll(obtenerTodasLasSubzonasIds(z.getId(), allZonesInWh));
                }
            }
        }
        return result;
    }

    private void mostrarDialogoZona(WarehouseZoneModel toEdit, WarehouseModel wh, String parentZoneId) {
        Stage stage = new Stage();
        stage.initOwner(this);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(toEdit == null ? "Crear Zona / Sector" : "Editar Zona / Sector");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f8f9fa;");

        TextField txtNombre = new TextField(toEdit != null ? toEdit.getNombre() : "");
        txtNombre.setPromptText("Nombre de la zona/sector");

        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList(
                "SECTOR", "ZONA_ALMACENAMIENTO", "ZONA_RECEPCION", "ZONA_DESPACHO", "ZONA_DEVOLUCIONES"
        ));
        cbTipo.setValue(toEdit != null ? toEdit.getTipoZona() : "ZONA_ALMACENAMIENTO");
        cbTipo.setMaxWidth(Double.MAX_VALUE);

        // Ámbito de stock logic (COMERCIAL vs PRIMARIA)
        String initialAmbito = "COMERCIAL";
        boolean disableAmbito = false;

        if (toEdit != null) {
            initialAmbito = toEdit.getAmbitoStock() != null ? toEdit.getAmbitoStock() : "COMERCIAL";
            disableAmbito = true;
        } else if (parentZoneId != null) {
            List<WarehouseZoneModel> zones = warehouseZonesMap.get(wh.getId());
            if (zones != null) {
                for (WarehouseZoneModel z : zones) {
                    if (z.getId().equals(parentZoneId)) {
                        initialAmbito = z.getAmbitoStock() != null ? z.getAmbitoStock() : "COMERCIAL";
                        break;
                    }
                }
            }
            disableAmbito = true;
        }

        ComboBox<String> cbAmbito = new ComboBox<>(FXCollections.observableArrayList("COMERCIAL", "PRIMARIA"));
        cbAmbito.setValue(initialAmbito);
        cbAmbito.setDisable(disableAmbito);
        cbAmbito.setMaxWidth(Double.MAX_VALUE);

        Button btnGuardar = new Button("Guardar", new FontAwesomeIconView(FontAwesomeIcon.SAVE));
        btnGuardar.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-font-weight: bold;");
        btnGuardar.setOnAction(e -> {
            String nom = txtNombre.getText().trim();
            if (nom.isEmpty()) {
                mostrarWarning("Validación", "El nombre es obligatorio.");
                return;
            }
            WarehouseZoneModel model = toEdit != null ? toEdit : new WarehouseZoneModel();
            model.setNombre(nom);
            model.setTipoZona(cbTipo.getValue());
            model.setWarehouseId(wh.getId());
            model.setCompanyId(wh.getCompanyId());
            model.setParentZoneId(parentZoneId);
            model.setAmbitoStock(cbAmbito.getValue());

            new Thread(() -> {
                try {
                    if (toEdit == null) {
                        service.crearZona(model);
                    } else {
                        service.actualizarZona(model.getId(), model);
                    }
                    javafx.application.Platform.runLater(() -> {
                        stage.close();
                        cargarDatos();
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    javafx.application.Platform.runLater(() -> mostrarError("Error", "No se pudo guardar la zona: " + ex.getMessage()));
                }
            }).start();
        });

        txtNombre.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setMaxWidth(Double.MAX_VALUE);

        layout.getChildren().addAll(
                new Label("Nombre del Sector/Zona:"), txtNombre,
                new Label("Tipo de Zona:"), cbTipo,
                new Label("Ámbito de Stock:"), cbAmbito,
                btnGuardar
        );

        Scene scene = new Scene(layout, 350, 270);
        ScreenUtil.applyResponsiveTheme(scene);
        stage.setScene(scene);
        stage.setResizable(false);
        ScreenUtil.centerOnOwner(stage, this);
        stage.showAndWait();
    }

    // ==========================================
    // GENERADOR EN LOTE
    // ==========================================
    private void abrirGeneradorLote() {
        if (selectedWarehouseContext == null) {
            mostrarWarning("Generador", "Debe seleccionar un contexto de Bodega o Zona del árbol.");
            return;
        }

        Stage stage = new Stage();
        stage.initOwner(this);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Generación Matricial de Ubicaciones");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f8f9fa;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        ComboBox<String> cbSoporteLote = new ComboBox<>(FXCollections.observableArrayList("RACK", "ESTANTE", "SUELO", "CONTENEDOR"));
        cbSoporteLote.setValue("RACK");
        cbSoporteLote.setMaxWidth(Double.MAX_VALUE);

        // Prefijo inteligente sugerido si hay zona seleccionada
        String defaultPrefix = "C1";
        if (selectedZoneContext != null) {
            String zoneName = selectedZoneContext.getNombre().toUpperCase();
            if (zoneName.contains("CALLE")) {
                defaultPrefix = zoneName.replace("CALLE", "C").replaceAll("\\s+", "").trim();
            } else if (zoneName.contains("ZONA")) {
                defaultPrefix = zoneName.replace("ZONA", "Z").replaceAll("\\s+", "").trim();
            } else {
                defaultPrefix = zoneName.substring(0, Math.min(3, zoneName.length())).trim();
            }
        }
        TextField txtPrefijo = new TextField(defaultPrefix);
        txtPrefijo.setPromptText("Ej: C1");
        txtPrefijo.setMaxWidth(Double.MAX_VALUE);

        Spinner<Integer> spVertical = new Spinner<>(1, 100, 5);
        spVertical.setMaxWidth(Double.MAX_VALUE);
        Spinner<Integer> spHorizontal = new Spinner<>(1, 100, 5);
        spHorizontal.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("Tipo Soporte:"), 0, 0);
        grid.add(cbSoporteLote, 1, 0);
        grid.add(new Label("Prefijo de Ubicación:"), 0, 1);
        grid.add(txtPrefijo, 1, 1);

        grid.add(new Label("Racks en Vertical (Alto):"), 0, 2);
        grid.add(spVertical, 1, 2);
        grid.add(new Label("Racks en Horizontal (Ancho):"), 0, 3);
        grid.add(spHorizontal, 1, 3);

        ColumnConstraints c1 = new ColumnConstraints(180);
        ColumnConstraints c2 = new ColumnConstraints(200);
        grid.getColumnConstraints().addAll(c1, c2);

        Label lblCalculo = new Label("Total a crear: 25 ubicaciones");
        lblCalculo.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");

        Runnable recalcular = () -> {
            int total = spVertical.getValue() * spHorizontal.getValue();
            lblCalculo.setText("Total a crear: " + total + " ubicaciones");
        };

        spVertical.valueProperty().addListener((o, ov, nv) -> recalcular.run());
        spHorizontal.valueProperty().addListener((o, ov, nv) -> recalcular.run());

        recalcular.run();

        Button btnGenerar = new Button("Generar Ubicaciones", new FontAwesomeIconView(FontAwesomeIcon.COGS));
        btnGenerar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        btnGenerar.setMaxWidth(Double.MAX_VALUE);
        btnGenerar.setOnAction(e -> {
            Map<String, Object> body = new HashMap<>();
            body.put("companyId", selectedWarehouseContext.getCompanyId());
            body.put("warehouseId", selectedWarehouseContext.getId());
            body.put("zoneId", selectedZoneContext != null ? selectedZoneContext.getId() : null);
            body.put("tipoSoporte", cbSoporteLote.getValue());
            body.put("prefijo", txtPrefijo.getText().trim());
            body.put("cantidadVertical", spVertical.getValue());
            body.put("cantidadHorizontal", spHorizontal.getValue());

            new Thread(() -> {
                try {
                    List<LocationModel> result = service.generarUbicacionesEnLote(body);
                    javafx.application.Platform.runLater(() -> {
                        locationsList.addAll(result);
                        stage.close();
                        mostrarInformacion("Generador", "Se han generado " + result.size() + " ubicaciones correctamente.");
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    javafx.application.Platform.runLater(() -> mostrarError("Error", "No se pudieron generar las ubicaciones: " + ex.getMessage()));
                }
            }).start();
        });

        layout.getChildren().addAll(grid, lblCalculo, btnGenerar);

        Scene scene = new Scene(layout, 410, 260);
        ScreenUtil.applyResponsiveTheme(scene);
        stage.setScene(scene);
        stage.setResizable(false);
        ScreenUtil.centerOnOwner(stage, this);
        stage.showAndWait();
    }

    private void mostrarInformacion(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void mostrarWarning(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
