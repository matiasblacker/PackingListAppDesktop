package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import com.logistics.packinglist.model.Bulto;
import com.logistics.packinglist.model.PackingList;
import com.logistics.packinglist.service.PdfContenedorService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContenedorDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    private final PackingList packingList;
    private final PdfContenedorService pdfService = new PdfContenedorService();

    private final double SCALE = 0.8;
    private final double MARGIN_CM = 1;

    private RadioButton rb20;
    private RadioButton rb40;
    private CheckBox chkSnap;

    // --- Estado Multi-Contenedor ---
    private final List<ContenedorInstance> contenedores = new ArrayList<>();
    private int indiceActual = 0;

    private VBox workspacesWrap;
    private Label lblEstadoContenedor;
    private Label lblContenedorReferencia;
    private TextField txtNombreContenedor;
    private ListView<BultoItem> listPendientes;
    private ListView<String> lvCarga;

    private BultoItem bultoSeleccionado = null;
    private final List<BultoItem> todosLosBultos = new ArrayList<>();

    public ContenedorDialog(Window owner, PackingList pl, File logoFile) {
        initOwner(owner);
        this.packingList = pl;
        if (logoFile != null) {
            pdfService.setLogo(logoFile);
        }
        initModality(Modality.APPLICATION_MODAL); // Mejor modal para evitar inconsistencias
        setTitle("Planificador de Contenedores");
        setResizable(false);
        initStyle(javafx.stage.StageStyle.UTILITY);
        setOnCloseRequest(e -> guardarEstadoEnModel());

        // Inicializar bultos (estado global)
        for (Bulto b : pl.getBultos()) {
            todosLosBultos.add(new BultoItem(b));
        }

        StackPane rootCarga = new StackPane();
        ProgressIndicator pin = new ProgressIndicator();
        pin.setMaxSize(50, 50);
        Label lblCargando = new Label("Cargando y restaurando estado...");
        lblCargando.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        VBox boxLoader = new VBox(15, pin, lblCargando);
        boxLoader.setAlignment(Pos.CENTER);
        rootCarga.getChildren().add(boxLoader);
        rootCarga.setStyle("-fx-background-color: white;");

        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        setX(bounds.getMinX());
        setY(bounds.getMinY());
        setWidth(bounds.getWidth());
        setHeight(bounds.getHeight());
        setScene(new Scene(rootCarga, bounds.getWidth(), bounds.getHeight()));

        // Ejecutar carga pesada en segundo plano
        javafx.concurrent.Task<Void> taskCarga = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                cargarEstadoPersistenteDatos(pl);
                return null;
            }
        };

        taskCarga.setOnSucceeded(e -> {
            cargarEstadoPersistenteUI();
            BorderPane ui = construirUI();
            getScene().setRoot(ui);
            refrescarTodo();
        });

        taskCarga.setOnFailed(e -> {
            Throwable ex = taskCarga.getException();
            if (ex != null) ex.printStackTrace();
            // Fallback en caso de error
            cargarEstadoPersistenteUI();
            BorderPane ui = construirUI();
            getScene().setRoot(ui);
            refrescarTodo();
        });

        Thread th = new Thread(taskCarga);
        th.setDaemon(true);
        th.start();
    }

    private void cargarEstadoPersistenteDatos(PackingList pl) {
        if (pl.getContenedores() != null && !pl.getContenedores().isEmpty()) {
            for (PackingList.ContainerState cs : pl.getContenedores()) {
                ContenedorInstance instance = new ContenedorInstance();
                instance.nombre = cs.getNombre();
                instance.is40ft = cs.is40ft();
                contenedores.add(instance);

                for (PackingList.BultoSnapshot bs : cs.getItemsColocados()) {
                    BultoItem bit = todosLosBultos.stream()
                            .filter(bi -> bi.bulto.getNumero() == bs.getBultoNumero())
                            .findFirst().orElse(null);
                    if (bit != null) {
                        bit.enContenedor = true;
                        bit.rotado = bs.isRotado();
                        bit.x = bs.getX();
                        bit.y = bs.getY();
                        bit.zCm = bs.getZ();
                        bit.contenedorPadre = instance;
                        instance.itemsColocados.add(bit);
                    }
                }
            }
            
            // Vincular apoyos en segunda pasada
            for (int i=0; i<pl.getContenedores().size(); i++) {
                PackingList.ContainerState cs = pl.getContenedores().get(i);
                ContenedorInstance instance = contenedores.get(i);
                for (int j=0; j<cs.getItemsColocados().size(); j++) {
                    PackingList.BultoSnapshot bs = cs.getItemsColocados().get(j);
                    BultoItem bit = instance.itemsColocados.get(j);
                    if (bs.getApoyadoSobreBultoNumero() != null) {
                        BultoItem apoyado = instance.itemsColocados.stream()
                                .filter(b -> b.bulto.getNumero() == bs.getApoyadoSobreBultoNumero())
                                .findFirst().orElse(null);
                        bit.apoyadoSobre = apoyado;
                    }
                }
            }
        } else {
            ContenedorInstance initial = new ContenedorInstance();
            initial.nombre = "Contenedor 1";
            contenedores.add(initial);
        }
    }

    private void cargarEstadoPersistenteUI() {
        for (ContenedorInstance instance : contenedores) {
            configurarDimensionesInstancia(instance);
            for (BultoItem bit : instance.itemsColocados) {
                crearVistaBloque(bit);
                bit.node.setLayoutX(bit.x);
                bit.node.setLayoutY(bit.y);
                
                if (bit.node.getParent() != null) ((Pane)bit.node.getParent()).getChildren().remove(bit.node);
                if (bit.nodeLateral.getParent() != null) ((Pane)bit.nodeLateral.getParent()).getChildren().remove(bit.nodeLateral);
                
                instance.workspaceAereo.getChildren().add(bit.node);
                instance.workspaceLateral.getChildren().add(bit.nodeLateral);
                
                double latH = instance.workspaceLateral.getPrefHeight();
                double miAlto = bit.bulto.getAlto() > 0 ? bit.bulto.getAlto() : 100;
                bit.nodeLateral.setLayoutY(latH - (bit.zCm * SCALE) - (miAlto * SCALE));
                bit.nodeLateral.setLayoutX(bit.x);
            }
        }
    }

    private BorderPane construirUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f7fb;");

        // --- Barra superior ---
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(10, 15, 10, 15));
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #dde4ee; -fx-border-width: 0 0 1 0;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label("🚢  Planificador de Flota");
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0F3E6E;");

        MenuButton btnAcciones = new MenuButton("⚙ Acciones de Flota");
        btnAcciones.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-cursor: hand;");
        MenuItem miNuevo = new MenuItem("➕ Nuevo Contenedor");
        miNuevo.setOnAction(e -> agregarNuevoContenedor());
        MenuItem miEliminar = new MenuItem("🗑 Eliminar Contenedor");
        miEliminar.setStyle("-fx-text-fill: #d32f2f;");
        miEliminar.setOnAction(e -> eliminarContenedorActual());
        MenuItem miExportar = new MenuItem("📄 Exportar Certificado Completo");
        miExportar.setOnAction(e -> exportar());
        btnAcciones.getItems().addAll(miNuevo, miEliminar, new SeparatorMenuItem(), miExportar);

        Button btnAnterior = new Button("◀");
        btnAnterior.setOnAction(e -> navegar(-1));

        lblContenedorReferencia = new Label("Contenedor 1 / 1");
        lblContenedorReferencia.setStyle("-fx-font-weight: bold;");

        Button btnSiguiente = new Button("▶");
        btnSiguiente.setOnAction(e -> navegar(1));

        txtNombreContenedor = new TextField();
        txtNombreContenedor.setPromptText("Nombre del contenedor...");
        txtNombreContenedor.setPrefWidth(180);
        txtNombreContenedor.textProperty().addListener((obs, oldV, newV) -> {
            if (!contenedores.isEmpty()) {
                getActual().nombre = newV;
                lblContenedorReferencia.setText(String.format("%s (%d / %d)",
                        newV.isEmpty() ? "S/N" : newV, indiceActual + 1, contenedores.size()));
                actualizarListaCarga();
                guardarEstadoEnModel();
            }
        });

        ToggleGroup tg = new ToggleGroup();
        rb20 = new RadioButton("20'");
        rb20.setToggleGroup(tg);
        rb40 = new RadioButton("40'");
        rb40.setToggleGroup(tg);

        tg.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && getActual() != null) {
                getActual().is40ft = rb40.isSelected();
                configurarContenedorVista();
                guardarEstadoEnModel();
            }
        });

        Region espTop = new Region();
        HBox.setHgrow(espTop, Priority.ALWAYS);

        chkSnap = new CheckBox("Snap 5cm");
        chkSnap.setSelected(false);
        chkSnap.setStyle("-fx-text-fill: #0F3E6E;");

        topBar.getChildren().addAll(lblTitulo, new Separator(javafx.geometry.Orientation.VERTICAL),
                btnAcciones, new Separator(javafx.geometry.Orientation.VERTICAL),
                btnAnterior, lblContenedorReferencia, btnSiguiente,
                new Label("Nombre:"), txtNombreContenedor, rb20, rb40,
                chkSnap,
                espTop);

        // --- Izquierda: Pendientes ---
        listPendientes = new ListView<>();
        listPendientes.setPrefWidth(220);
        listPendientes.setCellFactory(lv -> new ListCell<BultoItem>() {
            @Override
            protected void updateItem(BultoItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    setOnDragDetected(null);
                } else {
                    String aliasTx = item.bulto.getAlias().isBlank() ? "" : " - " + item.bulto.getAlias();
                    setText(item.bulto.getEtiqueta() + aliasTx + "\n(" + (int) item.bulto.getLargo() + "x" + (int) item.bulto.getAncho() + " cm)");
                    setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-padding: 8;");
                    setOnDragDetected(e -> {
                        javafx.scene.input.Dragboard db = startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                        content.putString(String.valueOf(item.bulto.getNumero()));
                        db.setContent(content);
                        e.consume();
                    });
                }
            }
        });

        VBox pnlIzq = new VBox(5, new Label("Pendientes (Arrastra):"), listPendientes);
        pnlIzq.setPadding(new Insets(10));
        pnlIzq.setStyle("-fx-background-color: white; -fx-border-color: #dde4ee; -fx-border-width: 0 1 0 0;");
        VBox.setVgrow(listPendientes, Priority.ALWAYS);

        // --- Derecha: Lista de Carga ---
        lvCarga = new ListView<>();
        lvCarga.setPrefWidth(250);
        
        Button btnQuitarDerecha = new Button("↩ Quitar seleccionado");
        btnQuitarDerecha.setStyle("-fx-background-color: #fce4e4; -fx-text-fill: #d32f2f; -fx-border-color: #f5c2c7; -fx-cursor: hand;");
        btnQuitarDerecha.setMaxWidth(Double.MAX_VALUE);
        btnQuitarDerecha.setOnAction(e -> {
            String selected = lvCarga.getSelectionModel().getSelectedItem();
            if (selected != null && bultoSeleccionado != null) {
                // Confirm that the selected item matches bultoSeleccionado roughly
                if (selected.startsWith(bultoSeleccionado.bulto.getEtiqueta())) {
                    quitarDeContenedor(bultoSeleccionado);
                }
            }
        });
        
        lvCarga.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                ContenedorInstance cur = getActual();
                if (cur != null) {
                    BultoItem match = cur.itemsColocados.stream()
                        .filter(i -> newV.startsWith(i.bulto.getEtiqueta()))
                        .findFirst().orElse(null);
                    if (match != null) seleccionarBulto(match);
                }
            }
        });

        VBox pnlDer = new VBox(5, new Label("Carga en este contenedor:"), lvCarga, btnQuitarDerecha);
        pnlDer.setPadding(new Insets(10));
        pnlDer.setStyle("-fx-background-color: white; -fx-border-color: #dde4ee; -fx-border-width: 0 0 0 1;");
        VBox.setVgrow(lvCarga, Priority.ALWAYS);

        // --- Centro: Workspaces ---
        // Los workspaces se inicializan vacíos y se inyectan desde ContenedorInstance
        workspacesWrap = new VBox(20);
        workspacesWrap.setAlignment(Pos.CENTER);

        StackPane pnlCentro = new StackPane(workspacesWrap);
        pnlCentro.setPadding(new Insets(20));

        ScrollPane scrollCentro = new ScrollPane(pnlCentro);
        scrollCentro.setPannable(true);
        scrollCentro.setFitToWidth(true);
        scrollCentro.setFitToHeight(true);

        lblEstadoContenedor = new Label();
        lblEstadoContenedor.setStyle("-fx-text-fill: #567; -fx-padding: 5 10;");

        BorderPane centroWrap = new BorderPane();
        centroWrap.setCenter(scrollCentro);
        centroWrap.setBottom(lblEstadoContenedor);

        root.setTop(topBar);
        root.setLeft(pnlIzq);
        root.setRight(pnlDer);
        root.setCenter(centroWrap);

        return root;
    }

    private void agregarNuevoContenedor() {
        ContenedorInstance next = new ContenedorInstance();
        next.nombre = "Contenedor " + (contenedores.size() + 1);
        contenedores.add(next);
        indiceActual = contenedores.size() - 1;
        refrescarTodo();
        guardarEstadoEnModel();
    }

    private void eliminarContenedorActual() {
        if (contenedores.size() <= 1) {
            new Alert(Alert.AlertType.WARNING, "Debe haber al menos un contenedor.").show();
            return;
        }

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar este contenedor y devolver su carga a pendientes?");
        conf.setHeaderText("Confirmar eliminación");
        conf.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                ContenedorInstance cur = getActual();
                // Liberar bultos
                List<BultoItem> aQuitar = new ArrayList<>(cur.itemsColocados);
                for (BultoItem item : aQuitar) {
                    quitarDeContenedor(item);
                }
                contenedores.remove(indiceActual);
                if (indiceActual >= contenedores.size())
                    indiceActual = contenedores.size() - 1;
                refrescarTodo();
                guardarEstadoEnModel();
            }
        });
    }

    private void navegar(int delta) {
        int target = indiceActual + delta;
        if (target >= 0 && target < contenedores.size()) {
            indiceActual = target;
            refrescarTodo();
        }
    }

    private void refrescarTodo() {
        ContenedorInstance cur = getActual();

        // Actualizar Controles
        lblContenedorReferencia.setText(String.format("%s (%d / %d)",
                cur.nombre, indiceActual + 1, contenedores.size()));
        txtNombreContenedor.setText(cur.nombre);

        // Sincronizar RadioButtons
        if (rb20 != null && rb40 != null) {
            if (cur.is40ft) {
                rb40.setSelected(true);
            } else {
                rb20.setSelected(true);
            }
        }

        // Actualizar Workspace
        workspacesWrap.getChildren().clear();
        workspacesWrap.getChildren().addAll(
                new VBox(5, new Label("Vista Aérea (Planta):"), cur.workspaceAereo),
                new VBox(5, new Label("Vista Lateral (Perfil):"), cur.workspaceLateral));

        configurarContenedorVista();
        refrescarPendientes();
        actualizarListaCarga();
    }

    private void configurarContenedorVista() {
        configurarDimensionesInstancia(getActual());
    }

    private void configurarDimensionesInstancia(ContenedorInstance cur) {
        if (cur == null) return;
        double wCm = cur.is40ft ? 1203 : 589;
        double aCm = cur.is40ft ? 235 : 238;
        double hCm = cur.is40ft ? 239 : 238;

        double wPx = wCm * SCALE;
        double hAereoPx = aCm * SCALE;
        double hLatPx = hCm * SCALE;

        cur.workspaceAereo.setPrefSize(wPx, hAereoPx);
        cur.workspaceAereo.setMaxSize(wPx, hAereoPx);
        cur.workspaceAereo.setMinSize(wPx, hAereoPx);

        cur.workspaceLateral.setPrefSize(wPx, hLatPx);
        cur.workspaceLateral.setMaxSize(wPx, hLatPx);
        cur.workspaceLateral.setMinSize(wPx, hLatPx);

        // Limpieza de bultos fuera de rango si se achica
        double marginPx = MARGIN_CM * SCALE;
        for (BultoItem item : cur.itemsColocados) {
            if (item.node != null) {
                if (item.node.getLayoutX() + item.node.getPrefWidth() > wPx - marginPx) {
                    item.node.setLayoutX(wPx - marginPx - item.node.getPrefWidth());
                    item.nodeLateral.setLayoutX(item.node.getLayoutX());
                    item.x = item.node.getLayoutX(); // keep model updated
                }
                if (item.node.getLayoutY() + item.node.getPrefHeight() > hAereoPx - marginPx) {
                    item.node.setLayoutY(hAereoPx - marginPx - item.node.getPrefHeight());
                    item.y = item.node.getLayoutY();
                }
            } else {
                double w = (item.rotado ? item.bulto.getAncho() : item.bulto.getLargo()) * SCALE;
                double h = (item.rotado ? item.bulto.getLargo() : item.bulto.getAncho()) * SCALE;
                if (item.x + w > wPx - marginPx) {
                    item.x = wPx - marginPx - w;
                }
                if (item.y + h > hAereoPx - marginPx) {
                    item.y = hAereoPx - marginPx - h;
                }
            }
            // Update Lateral Y based on the new lateral height
            if (item.nodeLateral != null) {
                double miAlto = item.bulto.getAlto() > 0 ? item.bulto.getAlto() : 100;
                item.nodeLateral.setLayoutY(hLatPx - (item.zCm * SCALE) - (miAlto * SCALE));
            }
        }
        actualizarEstado();
    }

    private void refrescarPendientes() {
        List<BultoItem> pendientes = new ArrayList<>();
        for (BultoItem item : todosLosBultos) {
            if (!item.enContenedor) {
                pendientes.add(item);
            }
        }
        listPendientes.getItems().setAll(pendientes);
        actualizarEstado();
    }

    private void colocarEnContenedor(BultoItem item, double x, double y) {
        ContenedorInstance cur = getActual();
        item.enContenedor = true;
        item.contenedorPadre = cur;
        cur.itemsColocados.add(item);

        crearVistaBloque(item);
        item.node.setLayoutX(x);
        item.node.setLayoutY(y);

        cur.workspaceAereo.getChildren().add(item.node);
        cur.workspaceLateral.getChildren().add(item.nodeLateral);

        seleccionarBulto(item);
        ajustarLímitesPosDrag(item);

        refrescarPendientes();
        actualizarListaCarga();
        guardarEstadoEnModel();
    }

    private void quitarDeContenedor(BultoItem item) {
        if (item.contenedorPadre != null) {
            item.contenedorPadre.itemsColocados.remove(item);
            item.contenedorPadre.workspaceAereo.getChildren().remove(item.node);
            item.contenedorPadre.workspaceLateral.getChildren().remove(item.nodeLateral);
        }
        item.enContenedor = false;
        item.contenedorPadre = null;
        item.apoyadoSobre = null;

        refrescarPendientes();
        recalcularZGlobal();
        actualizarListaCarga();
        guardarEstadoEnModel();
    }

    private void crearVistaBloque(BultoItem item) {
        Bulto b = item.bulto;
        if (item.node == null)
            item.node = new StackPane();
        if (item.nodeLateral == null)
            item.nodeLateral = new StackPane();

        item.node.getChildren().clear();
        item.nodeLateral.getChildren().clear();

        double lCm = b.getLargo() > 0 ? b.getLargo() : 100;
        double aCm = b.getAncho() > 0 ? b.getAncho() : 100;
        double hCm = b.getAlto() > 0 ? b.getAlto() : 100;

        double wPxAereo = (item.rotado ? aCm : lCm) * SCALE;
        double hPxAereo = (item.rotado ? lCm : aCm) * SCALE;
        double hPxLat = hCm * SCALE;

        item.node.setPrefSize(wPxAereo, hPxAereo);
        item.nodeLateral.setPrefSize(wPxAereo, hPxLat);

        Rectangle rA = new Rectangle(wPxAereo, hPxAereo);
        rA.setFill(Color.web("#cce0ff"));
        rA.setStroke(bultoSeleccionado == item ? Color.web("#f59e0b") : Color.web("#1a6aa8"));
        rA.setStrokeWidth(bultoSeleccionado == item ? 2.5 : 1.5);
        item.rectAereo = rA;

        Label lblA = new Label(b.getEtiqueta());
        lblA.setStyle("-fx-font-size: 9px; -fx-font-weight: bold;");
        item.node.getChildren().addAll(rA, lblA);

        Rectangle rL = new Rectangle(wPxAereo, hPxLat);
        rL.setFill(Color.web("#e6f2ff"));
        rL.setStroke(bultoSeleccionado == item ? Color.web("#f59e0b") : Color.web("#1a6aa8"));
        item.rectLateral = rL;
        Label lblL = new Label(b.getEtiqueta());
        lblL.setStyle("-fx-font-size: 9px;");
        item.nodeLateral.getChildren().addAll(rL, lblL);

        Tooltip t = new Tooltip(b.getEtiqueta() + "\n" + 
                                b.getLargo() + "x" + b.getAncho() + "x" + b.getAlto() + "cm\n" + 
                                b.getPesoTotal() + "kg");
        Tooltip.install(item.node, t);
        Tooltip.install(item.nodeLateral, t);

        // Movimiento
        item.node.setOnMousePressed(e -> {
            item.prevX = item.node.getLayoutX();
            item.prevY = item.node.getLayoutY();
            seleccionarBulto(item);
            item.node.toFront();
            item.nodeLateral.toFront();
            if (e.isSecondaryButtonDown() || e.getClickCount() == 2) {
                item.rotado = !item.rotado;
                crearVistaBloque(item);
                ajustarLímitesPosDrag(item);
                guardarEstadoEnModel();
            } else {
                item.dragOffsetX = e.getX();
                item.dragOffsetY = e.getY();
                item.node.getScene().setCursor(javafx.scene.Cursor.CLOSED_HAND);
            }
        });

        item.node.setOnMouseDragged(e -> {
            if (e.isSecondaryButtonDown())
                return;
            javafx.geometry.Point2D p = item.node.getParent().sceneToLocal(e.getSceneX(), e.getSceneY());
            item.node.setLayoutX(p.getX() - item.dragOffsetX);
            item.node.setLayoutY(p.getY() - item.dragOffsetY);
            ajustarLímites(item.node, getActual().workspaceAereo);
            item.nodeLateral.setLayoutX(item.node.getLayoutX());
        });

        item.node.setOnMouseReleased(e -> {
            item.node.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            ajustarLímitesPosDrag(item);
            actualizarListaCarga();
            guardarEstadoEnModel();
        });

        item.node.setOnContextMenuRequested(e -> quitarDeContenedor(item));
        actualizarColorZ(item);
    }

    private void ajustarLímitesPosDrag(BultoItem item) {
        ContenedorInstance cur = getActual();
        ajustarLímites(item.node, cur.workspaceAereo);

        // Snapping configurable
        double snapGrid = chkSnap != null && chkSnap.isSelected() ? 5 * SCALE : SCALE;
        double snappedX = Math.round(item.node.getLayoutX() / snapGrid) * snapGrid;
        double snappedY = Math.round(item.node.getLayoutY() / snapGrid) * snapGrid;
        item.node.setLayoutX(snappedX);
        item.node.setLayoutY(snappedY);
        ajustarLímites(item.node, cur.workspaceAereo);

        item.x = item.node.getLayoutX();
        item.y = item.node.getLayoutY();
        double nw = (item.rotado ? item.bulto.getAncho() : item.bulto.getLargo()) * SCALE;
        double nh = (item.rotado ? item.bulto.getLargo() : item.bulto.getAncho()) * SCALE;

        recalcularZ(item, item.x, item.y, nw, nh);
    }

    private void recalcularZ(BultoItem item, double nx, double ny, double nw, double nh) {
        ContenedorInstance cur = getActual();
        double maxZ = 0;
        BultoItem base = null;

        for (BultoItem otro : cur.itemsColocados) {
            if (otro == item)
                continue;

            double ox = otro.x;
            double oy = otro.y;
            double ow = (otro.rotado ? otro.bulto.getAncho() : otro.bulto.getLargo()) * SCALE;
            double oh = (otro.rotado ? otro.bulto.getLargo() : otro.bulto.getAncho()) * SCALE;

            if (nx < ox + ow && nx + nw > ox && ny < oy + oh && ny + nh > oy) {
                double topeOtro = otro.zCm + (otro.bulto.getAlto() > 0 ? otro.bulto.getAlto() : 100);
                if (topeOtro > maxZ) {
                    maxZ = topeOtro;
                    base = otro;
                }
            }
        }

        double miAlto = item.bulto.getAlto() > 0 ? item.bulto.getAlto() : 100;
        double techo = cur.is40ft ? 239 : 238;

        if (maxZ + miAlto > techo) {
            javafx.application.Platform.runLater(() -> lblEstadoContenedor.setText("⚠️ " + item.bulto.getEtiqueta() + " excede el techo."));
            if (item.prevX != -1 && (item.prevX != nx || item.prevY != ny)) {
                item.x = item.prevX;
                item.y = item.prevY;
                item.node.setLayoutX(item.prevX);
                item.node.setLayoutY(item.prevY);
                recalcularZ(item, item.prevX, item.prevY, nw, nh);
            } else {
                javafx.application.Platform.runLater(() -> quitarDeContenedor(item));
            }
            return;
        }

        item.zCm = maxZ;
        item.apoyadoSobre = base;

        double latH = cur.workspaceLateral.getPrefHeight();
        item.nodeLateral.setLayoutY(latH - (item.zCm * SCALE) - (miAlto * SCALE));
        item.nodeLateral.setLayoutX(nx);

        item.x = nx;
        item.y = ny;
        actualizarColorZ(item);
    }

    private void recalcularZGlobal() {
        for (ContenedorInstance c : contenedores) {
            // Sort a copy by Z so bottom items are processed first
            List<BultoItem> ordenados = new ArrayList<>(c.itemsColocados);
            ordenados.sort((a, b) -> Double.compare(a.zCm, b.zCm));
            
            // Reset Z to 0 to prevent circular inflation
            for (BultoItem i : ordenados) {
                i.zCm = 0;
            }

            for (BultoItem i : ordenados) {
                // Pasamos el contenedor explícitamente para evitar usar el "actual" erróneo durante carga
                recalcularZConContenedor(c, i, i.node.getLayoutX(), i.node.getLayoutY(), i.node.getPrefWidth(), i.node.getPrefHeight());
            }
        }
    }

    private void recalcularZConContenedor(ContenedorInstance cur, BultoItem item, double nx, double ny, double nw, double nh) {
        if (cur == null) return;
        double maxZ = 0;
        BultoItem base = null;
        double miAlto = item.bulto.getAlto();
        double techo = cur.is40ft ? 239 : 238;

        for (BultoItem otro : cur.itemsColocados) {
            if (otro == item) continue;

            double ox = otro.x;
            double oy = otro.y;
            double ow = (otro.rotado ? otro.bulto.getAncho() : otro.bulto.getLargo()) * SCALE;
            double oh = (otro.rotado ? otro.bulto.getLargo() : otro.bulto.getAncho()) * SCALE;

            // Intersección 2D
            if (nx < ox + ow && nx + nw > ox && ny < oy + oh && ny + nh > oy) {
                double topZ = otro.zCm + otro.bulto.getAlto();
                if (topZ > maxZ) {
                    maxZ = topZ;
                    base = otro;
                }
            }
        }

        if (maxZ + miAlto > techo) {
            System.err.println("⚠️ " + item.bulto.getEtiqueta() + " excede el techo al cargar. Ignorando eliminación automática y respetando apilamiento.");
        }

        item.zCm = maxZ;
        item.apoyadoSobre = base;
        
        if (item.nodeLateral != null && cur.workspaceLateral != null) {
            double latH = cur.workspaceLateral.getPrefHeight();
            item.nodeLateral.setLayoutY(latH - (item.zCm * SCALE) - (miAlto * SCALE));
            item.nodeLateral.setLayoutX(nx);
        }
        
        item.x = nx;
        item.y = ny;
        actualizarColorZ(item);
    }

    private void ajustarLímites(StackPane node, Pane space) {
        double m = MARGIN_CM * SCALE;
        if (node.getLayoutX() < m)
            node.setLayoutX(m);
        if (node.getLayoutX() > space.getPrefWidth() - m - node.getPrefWidth())
            node.setLayoutX(space.getPrefWidth() - m - node.getPrefWidth());
        if (node.getLayoutY() < m)
            node.setLayoutY(m);
        if (node.getLayoutY() > space.getPrefHeight() - m - node.getPrefHeight())
            node.setLayoutY(space.getPrefHeight() - m - node.getPrefHeight());
    }

    private void actualizarListaCarga() {
        lvCarga.getItems().clear();
        ContenedorInstance cur = getActual();
        for (BultoItem i : cur.itemsColocados) {
            String txt = i.bulto.getEtiqueta();
            if (i.apoyadoSobre != null) {
                txt += " (sobre " + i.apoyadoSobre.bulto.getEtiqueta() + ")";
            }
            lvCarga.getItems().add(txt);
        }
    }

    private void seleccionarBulto(BultoItem item) {
        if (bultoSeleccionado != null && bultoSeleccionado.rectAereo != null) {
            bultoSeleccionado.rectAereo.setStroke(Color.web("#1a6aa8"));
            bultoSeleccionado.rectAereo.setStrokeWidth(1.5);
        }
        bultoSeleccionado = item;
        if (item != null && item.rectAereo != null) {
            item.rectAereo.setStroke(Color.web("#f59e0b"));
            item.rectAereo.setStrokeWidth(2.5);
        }
    }

    private void actualizarEstado() {
        if (lblEstadoContenedor == null)
            return;
        int cargados = (int) todosLosBultos.stream().filter(i -> i.enContenedor).count();
        int total = todosLosBultos.size();
        lblEstadoContenedor.setText(String.format("Total Cargado: %d / %d  |  Contenedor Activo: %s",
                cargados, total, getActual().is40ft ? "40'" : "20'"));
    }

    private void actualizarColorZ(BultoItem item) {
        if (item.rectAereo == null || item.rectLateral == null) return;
        Color fill;
        if (item.zCm == 0) fill = Color.web("#cce0ff"); // Suelo: azul claro
        else if (item.zCm < 100) fill = Color.web("#d1fae5"); // Apilado nivel 1: verde
        else fill = Color.web("#ffedd5"); // Apilado nivel 2+: naranja
        item.rectAereo.setFill(fill);
        item.rectLateral.setFill(fill);
    }

    private ContenedorInstance getActual() {
        if (contenedores.isEmpty())
            return null;
        return contenedores.get(indiceActual);
    }

    private void guardarEstadoEnModel() {
        packingList.getContenedores().clear();
        for (ContenedorInstance ci : contenedores) {
            PackingList.ContainerState cs = new PackingList.ContainerState();
            cs.setNombre(ci.nombre);
            cs.set40ft(ci.is40ft);

            for (BultoItem bi : ci.itemsColocados) {
                PackingList.BultoSnapshot bs = new PackingList.BultoSnapshot();
                bs.setBultoNumero(bi.bulto.getNumero());
                bs.setX(bi.x);
                bs.setY(bi.y);
                bs.setZ(bi.zCm);
                bs.setRotado(bi.rotado);
                if (bi.apoyadoSobre != null) {
                    bs.setApoyadoSobreBultoNumero(bi.apoyadoSobre.bulto.getNumero());
                }
                cs.getItemsColocados().add(bs);
            }
            packingList.getContenedores().add(cs);
        }
    }

    private void exportar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exportar Certificado de Estiba");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fc.setInitialFileName("CONTENEDORES_" + packingList.getNumeroOrden() + ".pdf");
        File dest = fc.showSaveDialog(getScene().getWindow());
        if (dest != null) {
            try {
                // Pasamos la lista de contenedores al servicio
                List<Object[]> snapshots = new ArrayList<>();
                for (ContenedorInstance c : contenedores) {
                    VBox wrap = new VBox(10, c.workspaceAereo, c.workspaceLateral);
                    wrap.setPadding(new Insets(10));
                    List<String> carga = c.itemsColocados.stream()
                            .map(i -> i.bulto.getEtiqueta()
                                    + (i.apoyadoSobre != null ? " (sobre " + i.apoyadoSobre.bulto.getEtiqueta() + ")"
                                            : ""))
                            .collect(Collectors.toList());
                    snapshots.add(new Object[] { c.nombre, c.is40ft, wrap, carga });
                }
                pdfService.generarReporteMultiple(packingList, snapshots, dest);
                new Alert(Alert.AlertType.INFORMATION, "PDF generado con éxito").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).show();
            }
        }
    }

    private class ContenedorInstance {
        String nombre = "";
        boolean is40ft = false;
        Pane workspaceAereo = new Pane();
        Pane workspaceLateral = new Pane();
        List<BultoItem> itemsColocados = new ArrayList<>();

        ContenedorInstance() {
            workspaceAereo.setStyle("-fx-background-color: #e2e8f0; -fx-border-color: #94a3b8; -fx-border-width: 2;");
            workspaceLateral.setStyle("-fx-background-color: #e2e8f0; -fx-border-color: #94a3b8; -fx-border-width: 2;");

            workspaceAereo.setOnDragOver(e -> {
                if (e.getDragboard().hasString())
                    e.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                e.consume();
            });
            workspaceAereo.setOnDragDropped(e -> {
                try {
                    int bNum = Integer.parseInt(e.getDragboard().getString());
                    BultoItem item = todosLosBultos.stream().filter(i -> i.bulto.getNumero() == bNum && !i.enContenedor)
                            .findFirst().orElse(null);
                    if (item != null) {
                        // Calcular el centro aproximado para que se posicione centrado en el cursor
                        double w = (item.rotado ? item.bulto.getAncho() : item.bulto.getLargo()) * SCALE;
                        double h = (item.rotado ? item.bulto.getLargo() : item.bulto.getAncho()) * SCALE;
                        colocarEnContenedor(item, e.getX() - (w/2), e.getY() - (h/2));
                    }
                } catch (Exception ex) {
                }
                e.setDropCompleted(true);
                e.consume();
            });
        }
    }

    private class BultoItem {
        Bulto bulto;
        boolean enContenedor = false;
        boolean rotado = false;
        ContenedorInstance contenedorPadre;
        StackPane node;
        StackPane nodeLateral;
        Rectangle rectAereo;
        Rectangle rectLateral;
        double dragOffsetX = 0, dragOffsetY = 0;
        double prevX = -1, prevY = -1;
        double x = 0, y = 0; // Posicion actual en px
        double zCm = 0;
        BultoItem apoyadoSobre = null;

        BultoItem(Bulto b) {
            this.bulto = b;
        }
    }
}
