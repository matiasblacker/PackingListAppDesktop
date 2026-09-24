package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import com.logistics.packinglist.model.Bulto;
import com.logistics.packinglist.model.PackingItem;
import com.logistics.packinglist.model.PackingList;
import com.logistics.packinglist.model.ProductModel;
import com.logistics.packinglist.model.OrderNoteModel;
import com.logistics.packinglist.model.OrderNoteDetailModel;
import com.logistics.packinglist.model.InventarioItem;
import com.logistics.packinglist.service.InventarioService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.logistics.packinglist.utils.ScreenUtil;
import javafx.util.converter.IntegerStringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ManualPackingDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    private final InventarioService inventarioService;
    private PackingList resultado;
    private PackingList packingListActualParaCargar;

    // Estado interno
    private final ObservableList<Bulto> listaBultos = FXCollections.observableArrayList();
    private Bulto bultoSeleccionado = null;

    private TextField txtOrden;
    private TextField txtFecha;

    private ListView<Bulto> listBultos;
    private TableView<PackingItem> tablaItems;
    private ObservableList<PackingItem> itemsBultoActual = FXCollections.observableArrayList();

    // Controles de detalle bulto
    private TextField txtLargo, txtAncho, txtAlto, txtPesoBruto, txtPesoNeto;
    private TextField txtObservaciones;
    private ComboBox<String> cmbTipo;

    private String originalFileName = null;

    public ManualPackingDialog(Window owner, InventarioService inventarioService) {
        this.inventarioService = inventarioService;
        // initOwner(owner); // Comentado para evitar bug de X11/Mutter en Linux que
        // achica el padre
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Crear Packing Manual");
        setMinWidth(800);
        setMinHeight(500);

        construirUI();
        cargarProductosNPParaEdicion(null);
    }

    public ManualPackingDialog(Window owner, InventarioService inventarioService, PackingList plToEdit) {
        this(owner, inventarioService);
        setTitle("Editar Packing");
        if (plToEdit != null) {
            this.packingListActualParaCargar = plToEdit;
            txtOrden.setText(plToEdit.getNumeroOrden());
            txtFecha.setText(plToEdit.getFecha());
            txtFecha.setEditable(false);
            this.originalFileName = plToEdit.getNombreArchivo();

            listaBultos.addAll(plToEdit.getBultos());
            if (!listaBultos.isEmpty()) {
                listBultos.getSelectionModel().selectFirst();
            }
            cargarProductosNPParaEdicion(plToEdit.getNumeroOrden());
        }
    }

    private void construirUI() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f5f7fb;");

        root.setTop(construirCabecera());
        root.setLeft(construirPanelIzquierdo());
        root.setCenter(construirPanelDerecho());
        root.setBottom(construirPie());

        Scene scene = new Scene(root, 850, 540);
        if (getClass().getResource("/styles.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        }
        setScene(scene);
        ScreenUtil.fitDialogToScreen(this, getOwner(), 850, 540, 800, 480);
    }

    private Node construirCabecera() {
        HBox box = new HBox(15);
        box.setPadding(new Insets(0, 0, 15, 0));
        box.setAlignment(Pos.CENTER_LEFT);

        txtOrden = new TextField();
        txtOrden.setPromptText("Ej. PO-2023-45");

        txtFecha = new TextField(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        txtFecha.setEditable(false);
        txtFecha.setStyle("-fx-background-color: #eee;");

        Label lblNumOrden = new Label("Número de Orden:");
        lblNumOrden.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        Label lblFechaTit = new Label("Fecha:");
        lblFechaTit.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #475569;");

        box.getChildren().addAll(lblNumOrden, txtOrden, lblFechaTit, txtFecha);
        return box;
    }

    private Node construirPanelIzquierdo() {
        VBox box = new VBox(10);
        box.setPrefWidth(200);
        box.setPadding(new Insets(0, 10, 0, 0));

        Label lbl = new Label("Bultos / Pallets");
        lbl.setStyle("-fx-font-weight: bold;");

        listBultos = new ListView<>(listaBultos);
        listBultos.setCellFactory(param -> new ListCell<Bulto>() {
            @Override
            protected void updateItem(Bulto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getEtiqueta());
                }
            }
        });

        listBultos.getSelectionModel().selectedItemProperty().addListener((obs, onldVal, newVal) -> {
            seleccionarBulto(newVal);
        });

        Button btnNewPallet = new Button("+ Pallet");
        btnNewPallet.setOnAction(e -> crearBulto("1 PALLET"));

        Button btnNewBulto = new Button("+ Bulto");
        btnNewBulto.setOnAction(e -> crearBulto("1 BULTO"));

        Button btnEliminarBulto = new Button("Eliminar");
        btnEliminarBulto.setStyle("-fx-text-fill: red;");
        btnEliminarBulto.setOnAction(e -> {
            Bulto sel = listBultos.getSelectionModel().getSelectedItem();
            if (sel != null) {
                listaBultos.remove(sel);
                renumerarBultos();
                if (!listaBultos.isEmpty()) {
                    listBultos.getSelectionModel().selectFirst();
                } else {
                    seleccionarBulto(null);
                }
            }
        });

        HBox btns = new HBox(5, btnNewPallet, btnNewBulto);
        box.getChildren().addAll(lbl, btns, listBultos, btnEliminarBulto);
        VBox.setVgrow(listBultos, Priority.ALWAYS);

        return box;
    }

    private Node construirPanelDerecho() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(0, 0, 0, 10));
        box.setStyle("-fx-border-color: #ddd; -fx-border-width: 0 0 0 1;");

        // Dimensiones del Bulto
        Label lbl = new Label("Detalle del bulto seleccionado");
        lbl.setStyle("-fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        cmbTipo = new ComboBox<>(FXCollections.observableArrayList("PALLET", "BULTO", "CAJA", "CARGA SUELTA"));
        cmbTipo.setPromptText("Seleccione tipo");

        txtLargo = new TextField();
        txtLargo.setPrefWidth(60);
        txtLargo.setPromptText("0.0");
        txtAncho = new TextField();
        txtAncho.setPrefWidth(60);
        txtAncho.setPromptText("0.0");
        txtAlto = new TextField();
        txtAlto.setPrefWidth(60);
        txtAlto.setPromptText("0.0");
        txtPesoBruto = new TextField();
        txtPesoBruto.setPrefWidth(70);
        txtPesoBruto.setPromptText("0.0");
        txtPesoNeto = new TextField();
        txtPesoNeto.setPrefWidth(70);
        txtPesoNeto.setPromptText("0.0");
        txtObservaciones = new TextField();
        txtObservaciones.setPrefWidth(200);
        txtObservaciones.setPromptText("Detalles / Observaciones");

        // Validación numérica
        configurarValidacionNumerica(txtLargo);
        configurarValidacionNumerica(txtAncho);
        configurarValidacionNumerica(txtAlto);
        configurarValidacionNumerica(txtPesoBruto);
        configurarValidacionNumerica(txtPesoNeto);

        // Actualizar dims si cambia texto
        javafx.beans.value.ChangeListener<String> updater = (obs, oldV, newV) -> guardarDetalleBulto();
        cmbTipo.valueProperty().addListener(updater);
        txtLargo.textProperty().addListener(updater);
        txtAncho.textProperty().addListener(updater);
        txtAlto.textProperty().addListener(updater);
        txtPesoBruto.textProperty().addListener(updater);
        txtPesoNeto.textProperty().addListener(updater);
        txtObservaciones.textProperty().addListener(updater);

        Button btnReplicar = new Button("Replicar");
        btnReplicar.setOnAction(e -> replicarBultoActual());
        
        Label lblTipo = new Label("Tipo:");
        lblTipo.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        Label lblVol = new Label("Volumen:");
        lblVol.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        Label lblPesos = new Label("Pesos:");
        lblPesos.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        Label lblBruto = new Label("Bruto:");
        lblBruto.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        Label lblNeto = new Label("Neto:");
        lblNeto.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        Label lblObs = new Label("Observaciones:");
        lblObs.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #475569;");

        grid.add(lblTipo, 0, 0);
        grid.add(new HBox(10, cmbTipo, btnReplicar), 1, 0);
        HBox dims = new HBox(5, txtLargo, new Label("x"), txtAncho, new Label("x"), txtAlto, new Label("cm"));
        dims.setAlignment(Pos.CENTER_LEFT);
        grid.add(lblVol, 0, 1);
        grid.add(dims, 1, 1);

        HBox pesos = new HBox(10, lblBruto, txtPesoBruto, lblNeto, txtPesoNeto, new Label("kg"));
        pesos.setAlignment(Pos.CENTER_LEFT);
        grid.add(lblPesos, 0, 2);
        grid.add(pesos, 1, 2);

        grid.add(lblObs, 0, 3);
        grid.add(txtObservaciones, 1, 3);

        // Tabla Items
        Label lblItems = new Label("Contenido (Ítems)");
        lblItems.setStyle("-fx-font-weight: bold;");

        tablaItems = new TableView<>(itemsBultoActual);
        tablaItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PackingItem, String> cPn = new TableColumn<>("SKU");
        cPn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPartNumber()));

        TableColumn<PackingItem, String> cDesc = new TableColumn<>("Descripción");
        cDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));

        TableColumn<PackingItem, Integer> cCant = new TableColumn<>("Cantidad");
        cCant.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getCantidad()));
        cCant.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        cCant.setOnEditCommit(e -> {
            PackingItem item = e.getRowValue();
            item.setCantidad(e.getNewValue());
            guardarDetalleBulto(); // Recalcular pesos si es necesario
        });

        tablaItems.setEditable(true);
        tablaItems.getColumns().addAll(cPn, cDesc, cCant);

        Button btnAddProducto = new Button("➕ Añadir Producto");
        btnAddProducto.setOnAction(e -> abrirSeleccionProducto());

        Button btnLector = new Button("🔫 Usar Lector");
        btnLector.setOnAction(e -> abrirLector());

        Button btnEliminarProducto = new Button("Eliminar Seleccionado");
        btnEliminarProducto.setOnAction(e -> {
            PackingItem sel = tablaItems.getSelectionModel().getSelectedItem();
            if (sel != null && bultoSeleccionado != null) {
                bultoSeleccionado.getItems().remove(sel);
                itemsBultoActual.remove(sel);
            }
        });

        HBox tbr = new HBox(10, btnAddProducto, btnLector, btnEliminarProducto);

        box.getChildren().addAll(lbl, grid, new Separator(), lblItems, tbr, tablaItems);
        VBox.setVgrow(tablaItems, Priority.ALWAYS);

        // Deshabilitar derecho por defecto
        box.setDisable(true);
        this.boxDerecho = box;

        return box;
    }

    private VBox boxDerecho;

    private Node construirPie() {
        HBox box = new HBox();
        box.setPadding(new Insets(15, 0, 0, 0));
        box.setAlignment(Pos.CENTER_RIGHT);

        Button btnFinalizar = new Button("Finalizar y Ver Packing");
        btnFinalizar.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        btnFinalizar.setOnAction(e -> compilarYGuardar());

        box.getChildren().add(btnFinalizar);
        return box;
    }

    private void crearBulto(String tipo) {
        int numero = listaBultos.size() + 1;
        Bulto b = new Bulto(numero, tipo, 0, 0, 0, 0, 0);
        b.setObservaciones("");
        listaBultos.add(b);
        listBultos.getSelectionModel().select(b);
    }

    private void replicarBultoActual() {
        if (bultoSeleccionado == null) return;
        int numero = listaBultos.size() + 1;
        Bulto copia = new Bulto(numero, bultoSeleccionado.getTipo(), bultoSeleccionado.getPesoTotal(), bultoSeleccionado.getPesoNeto(), bultoSeleccionado.getLargo(), bultoSeleccionado.getAncho(), bultoSeleccionado.getAlto());
        copia.setAlias(bultoSeleccionado.getAlias());
        copia.setObservaciones(bultoSeleccionado.getObservaciones());
        for (PackingItem item : bultoSeleccionado.getItems()) {
            copia.addItem(new PackingItem(item.getPartNumber(), item.getDescripcion(), item.getCantidad()));
        }
        listaBultos.add(copia);
        listBultos.getSelectionModel().select(copia);
    }

    private boolean bloqueandoUpdates = false;

    private void seleccionarBulto(Bulto b) {
        this.bultoSeleccionado = b;
        if (b == null) {
            boxDerecho.setDisable(true);
            itemsBultoActual.clear();
            return;
        }

        boxDerecho.setDisable(false);
        bloqueandoUpdates = true;

        cmbTipo.setValue(b.getTipo());
        txtLargo.setText(formatDouble(b.getLargo()));
        txtAncho.setText(formatDouble(b.getAncho()));
        txtAlto.setText(formatDouble(b.getAlto()));
        txtPesoBruto.setText(formatDouble(b.getPesoTotal()));
        txtPesoNeto.setText(formatDouble(b.getPesoNeto()));
        txtObservaciones.setText(b.getObservaciones() != null ? b.getObservaciones() : "");

        itemsBultoActual.setAll(b.getItems());

        bloqueandoUpdates = false;
    }

    private void guardarDetalleBulto() {
        if (bloqueandoUpdates || bultoSeleccionado == null)
            return;

        try {
            double pb = txtPesoBruto.getText().isEmpty() ? 0 : Double.parseDouble(txtPesoBruto.getText().trim());
            double pn = txtPesoNeto.getText().isEmpty() ? 0 : Double.parseDouble(txtPesoNeto.getText().trim());
            double l = txtLargo.getText().isEmpty() ? 0 : Double.parseDouble(txtLargo.getText().trim());
            double an = txtAncho.getText().isEmpty() ? 0 : Double.parseDouble(txtAncho.getText().trim());
            double al = txtAlto.getText().isEmpty() ? 0 : Double.parseDouble(txtAlto.getText().trim());

            String tipoLimpio = cmbTipo.getValue() != null ? cmbTipo.getValue().replaceAll("^\\d+\\s*", "").toUpperCase() : "BULTO";
            
            Bulto actualizado = new Bulto(bultoSeleccionado.getNumero(), tipoLimpio, pb, pn, l, an, al);
            actualizado.getItems().addAll(bultoSeleccionado.getItems());
            actualizado.setAlias(bultoSeleccionado.getAlias());
            actualizado.setObservaciones(txtObservaciones.getText());

            int index = listaBultos.indexOf(bultoSeleccionado);
            if (index >= 0) {
                bloqueandoUpdates = true;
                listaBultos.set(index, actualizado);
                this.bultoSeleccionado = actualizado;
                listBultos.getSelectionModel().select(actualizado);
                bloqueandoUpdates = false;
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void renumerarBultos() {
        for (int i = 0; i < listaBultos.size(); i++) {
            Bulto b = listaBultos.get(i);
            int nuevoNum = i + 1;
            if (b.getNumero() != nuevoNum) {
                // Como Bulto no tiene setNumero, creamos uno nuevo con las mismas propiedades
                Bulto rb = new Bulto(nuevoNum, b.getTipo(), b.getPesoTotal(), b.getPesoNeto(), b.getLargo(), b.getAncho(), b.getAlto());
                rb.setAlias(b.getAlias());
                rb.getItems().addAll(b.getItems());
                listaBultos.set(i, rb);
            }
        }
    }

    private void abrirSeleccionProducto() {
        if (bultoSeleccionado == null)
            return;

        SeleccionProductoDialog dialog = new SeleccionProductoDialog(this, inventarioService);
        dialog.showAndWait();

        PackingItem pi = dialog.getResultado();
        if (pi != null) {
            agregarOIncrementarProducto(pi);
        }
    }

    private void abrirLector() {
        if (bultoSeleccionado == null) return;
        
        LectorSKUDialog dialog = new LectorSKUDialog(this, inventarioService, itemInv -> {
            PackingItem pi = new PackingItem(itemInv.getPartNumber(), itemInv.getDescripcion(), 1);
            agregarOIncrementarProducto(pi);
        });
        dialog.show(); 
    }

    private void agregarOIncrementarProducto(PackingItem pi) {
        if (bultoSeleccionado == null) return;
        
        // Lógica de suma/fusión si el SKU ya existe
        boolean existe = false;
        for (PackingItem itemExistente : bultoSeleccionado.getItems()) {
            if (itemExistente.getPartNumber().equalsIgnoreCase(pi.getPartNumber())) {
                itemExistente.setCantidad(itemExistente.getCantidad() + pi.getCantidad());
                existe = true;
                break;
            }
        }

        if (!existe) {
            bultoSeleccionado.addItem(pi);
        }

        itemsBultoActual.setAll(bultoSeleccionado.getItems());
        guardarDetalleBulto();
    }

    private void compilarYGuardar() {
        if (listaBultos.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setContentText("Debe agregar al menos un bulto para crear un Packing List.");
            a.showAndWait();
            return;
        }

        // 1. Validar dimensiones y peso por tipo de bulto (RN-PP-007)
        for (Bulto b : listaBultos) {
            String tipo = b.getTipo() != null ? b.getTipo().toUpperCase() : "";
            if ("PALLET".equals(tipo) || "BULTO".equals(tipo) || "CAJA".equals(tipo)) {
                if (b.getLargo() <= 0 || b.getAncho() <= 0 || b.getAlto() <= 0 || b.getPesoTotal() <= 0) {
                    Alert a = new Alert(Alert.AlertType.WARNING);
                    a.setContentText("Para Pallet, Caja y Bulto, las dimensiones (largo, ancho, alto) y el peso son obligatorios y deben ser mayores a 0. Revise el bulto N° " + b.getNumero());
                    a.showAndWait();
                    return;
                }
            }
        }

        PackingList pl = new PackingList();
        String usuarioActual = System.getProperty("user.name", "Usuario");
        String fechaHoy = txtFecha.getText().trim();

        if (originalFileName != null && !originalFileName.isEmpty()) {
            // Edición
            pl.setFechaCreacion(packingListActualParaCargar != null ? packingListActualParaCargar.getFechaCreacion() : ""); 
            pl.setUsuarioCreacion(packingListActualParaCargar != null ? packingListActualParaCargar.getUsuarioCreacion() : "");
            pl.setFechaEdicion(fechaHoy);
            pl.setUsuarioEdicion(usuarioActual);
        } else {
            // Creación
            pl.setFechaCreacion(fechaHoy);
            pl.setUsuarioCreacion(usuarioActual);
            pl.setFechaEdicion(fechaHoy);
            pl.setUsuarioEdicion(usuarioActual);
        }

        pl.setNumeroOrden(txtOrden.getText().trim());
        pl.setFecha(fechaHoy);

        if (this.originalFileName != null && !this.originalFileName.isEmpty()) {
            pl.setNombreArchivo(this.originalFileName);
        } else {
            pl.setNombreArchivo("Packing_Manual_" + System.currentTimeMillis());
        }

        int totalA = 0;
        double tp = 0;
        double tn = 0;

        for (Bulto b : listaBultos) {
            pl.addBulto(b);
            totalA += b.getTotalUnidades();
            tp += b.getPesoTotal();
            tn += b.getPesoNeto();
        }

        pl.setTotalPiezas(totalA);
        pl.setPesoTotalKg(tp);
        pl.setPesoNetoKg(tn);

        this.resultado = pl;
        close();
    }

    public PackingList getResultado() {
        return resultado;
    }

    private String formatDouble(double d) {
        if (d == 0) return "";
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.valueOf(d);
    }

    private void configurarValidacionNumerica(TextField tf) {
        tf.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.matches("\\d*(\\.\\d*)?")) {
                tf.setText(oldV);
            }
        });
    }

    private void cargarProductosNPParaEdicion(String numeroOrden) {
        new Thread(() -> {
            try {
                com.logistics.packinglist.service.MantenimientoService service = new com.logistics.packinglist.service.MantenimientoService();
                List<ProductModel> prods = service.obtenerProductos();
                java.util.Map<String, ProductModel> productMap = new java.util.HashMap<>();
                for (ProductModel p : prods) {
                    if (p.getId() != null) {
                        productMap.put(p.getId().toLowerCase().trim(), p);
                    }
                }

                OrderNoteModel npEncontrada = null;
                if (numeroOrden != null && !numeroOrden.trim().isEmpty()) {
                    List<OrderNoteModel> nps = service.obtenerNotasPedido();
                    for (OrderNoteModel np : nps) {
                        if (numeroOrden.trim().equalsIgnoreCase(np.getFolio())) {
                            npEncontrada = np;
                            break;
                        }
                    }
                }

                final OrderNoteModel selectedNP = npEncontrada;
                javafx.application.Platform.runLater(() -> {
                    inventarioService.getItems().clear();
                    if (selectedNP != null && selectedNP.getDetails() != null && !selectedNP.getDetails().isEmpty()) {
                        for (OrderNoteDetailModel detail : selectedNP.getDetails()) {
                            String pId = detail.getProductId();
                            ProductModel p = (pId != null) ? productMap.get(pId.toLowerCase().trim()) : null;
                            if (p != null) {
                                String desc = p.getNombre() + " (Pedido: " + detail.getCantidadPedida() + ")";
                                inventarioService.getItems().add(new InventarioItem(p.getSku(), desc, "NP"));
                            }
                        }
                    } else {
                        // Fallback: load all products
                        for (ProductModel p : prods) {
                            inventarioService.getItems().add(new InventarioItem(p.getSku(), p.getNombre(), "STOCK"));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
