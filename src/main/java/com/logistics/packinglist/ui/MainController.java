package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import com.itextpdf.text.DocumentException;
import com.logistics.packinglist.model.Bulto;
import com.logistics.packinglist.model.PackingItem;
import com.logistics.packinglist.model.PackingList;
import com.logistics.packinglist.service.ExcelReaderService;
import com.logistics.packinglist.service.PdfExportService;
import com.logistics.packinglist.service.PackingStorageService;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import com.logistics.packinglist.service.AuthService;
import com.logistics.packinglist.service.WebSocketManager;
import com.logistics.packinglist.service.SalesTargetStorageService;
import com.logistics.packinglist.service.ExchangeRateStorageService;
import com.logistics.packinglist.model.SalesTargetModel;
import com.logistics.packinglist.model.ExchangeRateModel;
import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.WarehouseModel;
import com.logistics.packinglist.model.UserModel;
import com.logistics.packinglist.model.NotificationModel;
import com.logistics.packinglist.model.StockModel;
import com.logistics.packinglist.model.OrderNoteModel;
import com.logistics.packinglist.model.ReceptionAnnouncementModel;
import com.logistics.packinglist.model.LocationModel;
import java.time.LocalDateTime;

public class MainController extends BorderPane {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    private final ExcelReaderService excelService = new ExcelReaderService();
    private final PdfExportService pdfService = new PdfExportService();
    private final com.logistics.packinglist.service.InventarioService inventarioService = new com.logistics.packinglist.service.InventarioService();
    private final PackingStorageService storageService = new PackingStorageService();

    private PackingList packingListActual = null;
    private File logoFile = null;
    private final java.util.Set<String> activePermissions = new java.util.HashSet<>();

    private TabPane tabPane;
    private BorderPane homePane;
    private final java.util.Map<String, Tab> openDialogTabs = new java.util.HashMap<>();
    private Label lblEstado;
    private Label lblOrden;
    private ImageView imgLogoPreview;
    private MenuItem itemExportarPDF;
    private MenuItem itemExportarTodos;
    private MenuItem itemAgrupar;
    private MenuItem itemContenedor;
    private MenuItem itemExportarSeleccion;
    private MenuItem itemEditarPacking;

    private MenuItem itemPackingLists;
    private MenuItem itemCrearManual;
    private MenuButton menuWMS;
    private MenuButton menuCOM;
    private MenuButton menuArchivo;
    private MenuButton menuExportar;
    private MenuItem itemPackingList;
    private MenuItem itemUbicaciones;
    private MenuItem itemStock;
    private MenuItem itemNotas;
    private MenuItem itemBackOrders;
    private MenuItem itemDespachos;
    private MenuItem itemAnuncios;
    private MenuItem itemRecepciones;
    private MenuItem itemInventarioVisual;
    private MenuItem itemTracking;

    private MenuButton menuNotificaciones;
    private Label lblNotificationBadge;
    private int unreadNotificationCount = 0;
    private CustomMenuItem headerNotifMenuItem;
    private Label lblHeaderNotifTitle;
    private Button btnDescartarTodasNotif;
    private CustomMenuItem emptyNotifMenuItem;
    private CustomMenuItem footerNotifMenuItem;
    private final List<CustomMenuItem> notifItemsList = new ArrayList<>();
    
    // Dashboard Inicio KPIs & Feed
    private Label lblKpiStock;
    private Label lblKpiStockSub;
    private Label lblKpiNotas;
    private Label lblKpiNotasSub;
    private Label lblKpiRecepciones;
    private Label lblKpiRecepcionesSub;
    private Label lblKpiPackings;
    private Label lblKpiPackingsSub;
    private ProgressBar pbOcupacion;
    private Label lblOcupacionDetalle;
    private VBox vboxActivityFeed;
    private Tab dashboardTab;

    // Metas de Ventas Loader & Controls
    private ProgressBar pbMetaVentas;
    private Label lblMetaVentasTitulo;
    private Label lblMetaVentasDetalle;

    // Right Info Panel Controls (Home & Dashboard)
    private ComboBox<CompanyModel> cbEmpresaActivaHome, cbEmpresaActivaDashboard;
    private ComboBox<WarehouseModel> cbBodegaActivaHome, cbBodegaActivaDashboard;
    private Label lblEmpNameHome, lblEmpNameDashboard;
    private Label lblEmpRutHome, lblEmpRutDashboard;
    private Label lblBodNameHome, lblBodNameDashboard;
    private Label lblBodDirHome, lblBodDirDashboard;
    private Label lblExchangeRateHome, lblExchangeRateDashboard;
    private Label lblExchangeRateUpdatedHome, lblExchangeRateUpdatedDashboard;
    private VBox vboxUsersListHome, vboxUsersListDashboard;
    private boolean isSyncingSelection = false;

    private final javafx.stage.Stage stage;
    private final Runnable onLogout;

    public MainController(javafx.stage.Stage stage, Runnable onLogout) {
        this.stage = stage;
        this.onLogout = onLogout;
        construirUI();
        cargarConfiguracion();
        configurarCierreVentana();
    }

    private void configurarCierreVentana() {
        // Escuchamos cuando este componente se agrega a una ventana para capturar el
        // cierre de la "X"
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsW, oldW, newW) -> {
                    if (newW instanceof javafx.stage.Stage) {
                        ((javafx.stage.Stage) newW).setOnCloseRequest(e -> {
                            e.consume(); // Detenemos el cierre inmediato
                            confirmarYSalir();
                        });
                    }
                });
            }
        });
    }

    private void confirmarYSalir() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Salida");
        alert.setHeaderText("¿Estás seguro de que deseas salir?");
        alert.setContentText("Guarde cualquier trabajo pendiente antes de cerrar.");

        ButtonType btnSi = new ButtonType("Sí, Salir");
        ButtonType btnNo = new ButtonType("No, Seguir trabajando", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSi, btnNo);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnSi) {
                guardarActual();
                com.logistics.packinglist.service.AuthService.getInstance().logout();
                Platform.exit();
            }
        });
    }

    private void confirmarYCerrarSesion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Cierre de Sesión");
        alert.setHeaderText("¿Estás seguro de que deseas cerrar la sesión?");
        alert.setContentText("Cualquier cambio no guardado en la orden actual se guardará localmente.");

        ButtonType btnSi = new ButtonType("Sí, cerrar sesión");
        ButtonType btnNo = new ButtonType("No, seguir aquí", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSi, btnNo);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnSi) {
                guardarActual();
                com.logistics.packinglist.service.AuthService.getInstance().logout();
                if (onLogout != null) {
                    onLogout.run();
                }
            }
        });
    }

    private void mostrarPerfil() {
        com.logistics.packinglist.service.AuthService auth = com.logistics.packinglist.service.AuthService
                .getInstance();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Perfil de Usuario");
        alert.setHeaderText("Información de Usuario");

        String content = String.format(
                "Nombre completo: %s %s\n" +
                        "Correo electrónico: %s\n" +
                        "Rol: %s\n" +
                        "Compañía ID: %s\n" +
                        "Bodega ID: %s",
                auth.getNombre(),
                auth.getApellido(),
                auth.getEmail(),
                auth.getRole(),
                auth.getCompanyId() != null ? auth.getCompanyId() : "N/A",
                auth.getWarehouseId() != null ? auth.getWarehouseId() : "N/A");

        alert.setContentText(content);
        alert.showAndWait();
    }

    private void construirUI() {
        setTop(crearBarra());
        setCenter(crearCentro());
    }

    private Node crearBarra() {
        ImageView iconPrincipal = new ImageView(new Image(getClass().getResourceAsStream("/icon-lightbg.png")));
        iconPrincipal.setFitHeight(25);
        iconPrincipal.setPreserveRatio(true);

        imgLogoPreview = new ImageView();
        imgLogoPreview.setFitHeight(25);
        imgLogoPreview.setPreserveRatio(true);

        Text txtTitulo = new Text("Picking & Packing NP");
        txtTitulo.setFont(Font.font("System", FontWeight.BOLD, 13));
        txtTitulo.setFill(Color.WHITE);

        HBox hboxTitulo = new HBox(8, iconPrincipal, txtTitulo, imgLogoPreview);
        hboxTitulo.setAlignment(Pos.CENTER_LEFT);

        lblOrden = new Label("");
        lblOrden.setStyle("-fx-text-fill: rgba(200,220,255,0.9); -fx-font-size: 10px;");

        VBox vTitulo = new VBox(1, hboxTitulo, lblOrden);
        vTitulo.setAlignment(Pos.CENTER_LEFT);

        // --- Menu Archivo ---
        menuArchivo = crearMenuDesplegable("Archivo", FontAwesomeIcon.FILE_TEXT);
        itemPackingLists = crearItem("Picking & Packings", FontAwesomeIcon.LIST);
        itemPackingLists.setOnAction(e -> {
            PackingBrowserDialog dialog = new PackingBrowserDialog(
                    getScene().getWindow(),
                    storageService,
                    packingListActual,
                    () -> resetUI());
            dialog.showAndWait();
            if (dialog.getResultado() != null) {
                mostrarPackingList(dialog.getResultado());
            }
        });

        itemCrearManual = crearItem("Crear Packing Manual...", FontAwesomeIcon.PLUS);
        itemCrearManual.setOnAction(e -> {
            ManualPackingDialog dialog = new ManualPackingDialog(getScene().getWindow(), inventarioService);
            dialog.showAndWait();
            if (dialog.getResultado() != null) {
                guardarYMostrar(dialog.getResultado());
            }
        });

        itemEditarPacking = crearItem("Editar Packing Actual...", FontAwesomeIcon.EDIT);
        itemEditarPacking.setDisable(true);
        itemEditarPacking.setOnAction(e -> {
            if (packingListActual != null) {
                ManualPackingDialog dialog = new ManualPackingDialog(getScene().getWindow(), inventarioService,
                        packingListActual);
                dialog.showAndWait();
                if (dialog.getResultado() != null) {
                    guardarYMostrar(dialog.getResultado());
                }
            }
        });

        itemAgrupar = crearItem("Agrupar Pallets", FontAwesomeIcon.TH_LARGE);
        itemAgrupar.setDisable(true);
        itemAgrupar.setOnAction(e -> abrirAgrupador());

        itemContenedor = crearItem("Modelador Contenedor", FontAwesomeIcon.CUBE);
        itemContenedor.setDisable(true);
        itemContenedor.setOnAction(e -> abrirContenedor());

        menuArchivo.getItems().addAll(itemPackingLists, itemCrearManual, itemEditarPacking,
                new SeparatorMenuItem(),
                itemAgrupar,
                itemContenedor);

        // --- Menu Exportar ---
        menuExportar = crearMenuDesplegable("Exportar", FontAwesomeIcon.UPLOAD);
        itemExportarPDF = crearItem("Exportar Bulto Activo a PDF", FontAwesomeIcon.FILE_PDF_ALT);
        itemExportarPDF.setDisable(true);
        itemExportarPDF.setOnAction(e -> exportarPDFBultoActivo());
        itemExportarSeleccion = crearItem("Exportar Selección...", FontAwesomeIcon.CHECK_SQUARE);
        itemExportarSeleccion.setDisable(true);
        itemExportarSeleccion.setOnAction(e -> abrirExportarSeleccion());
        itemExportarTodos = crearItem("Exportar PDF Completo a archivo", FontAwesomeIcon.FILE_PDF_ALT);
        itemExportarTodos.setDisable(true);
        itemExportarTodos.setOnAction(e -> exportarPDFCompleto());
        menuExportar.getItems().addAll(itemExportarPDF, itemExportarSeleccion, new SeparatorMenuItem(),
                itemExportarTodos);

        // --- Menu Ayuda ---
        MenuButton menuAyuda = crearMenuDesplegable("Ayuda", FontAwesomeIcon.QUESTION_CIRCLE);
        MenuItem mGuia = crearItem("Guía", FontAwesomeIcon.BOOK);
        mGuia.setOnAction(e -> abrirTabGuia());
        MenuItem mAcerca = crearItem("Acerca de...", FontAwesomeIcon.INFO_CIRCLE);
        mAcerca.setOnAction(e -> new AcercaDeDialog(getScene().getWindow()).show());
        menuAyuda.getItems().addAll(mGuia, mAcerca);

        // --- Menu Mantenimiento (ADMIN / WMS) ---
        com.logistics.packinglist.service.AuthService auth = com.logistics.packinglist.service.AuthService
                .getInstance();
        boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(auth.getRole());
        boolean isWMSUser = "ADMINSIS".equalsIgnoreCase(auth.getRole())
                || "ADMIN_BODEGA".equalsIgnoreCase(auth.getRole())
                || "ASISTENTE_DOCUMENTAL".equalsIgnoreCase(auth.getRole())
                || "ASISTENTE_BODEGA".equalsIgnoreCase(auth.getRole())
                || "GESTOR_COMERCIAL".equalsIgnoreCase(auth.getRole());
        MenuButton menuMantenimiento = null;

        if (isWMSUser) {
            menuMantenimiento = crearMenuDesplegable("Mantenimiento", FontAwesomeIcon.WRENCH);

            if (isAdminSis) {
                MenuItem itemEmpresas = crearItem("Empresas", FontAwesomeIcon.BUILDING);
                itemEmpresas.setOnAction(e -> abrirTab("Empresas", () -> new EmpresasDialog(getScene().getWindow())));
                menuMantenimiento.getItems().add(itemEmpresas);

                MenuItem itemUsuarios = crearItem("Usuarios", FontAwesomeIcon.USERS);
                itemUsuarios.setOnAction(e -> abrirTab("Usuarios", () -> new UsuariosDialog(getScene().getWindow())));
                menuMantenimiento.getItems().add(itemUsuarios);

                MenuItem itemDepositos = crearItem("Depósitos", FontAwesomeIcon.BUILDING);
                itemDepositos.setOnAction(e -> abrirTab("Depósitos", () -> new DepositosDialog(getScene().getWindow())));
                menuMantenimiento.getItems().add(itemDepositos);

                MenuItem itemBodegas = crearItem("Bodegas", FontAwesomeIcon.ARCHIVE);
                itemBodegas.setOnAction(e -> abrirTab("Bodegas", () -> new BodegasDialog(getScene().getWindow())));
                menuMantenimiento.getItems().add(itemBodegas);

                MenuItem itemPermisos = crearItem("Permisos", FontAwesomeIcon.SHIELD);
                itemPermisos.setOnAction(e -> abrirTab("Permisos", () -> new PermisosDialog(getScene().getWindow())));
                menuMantenimiento.getItems().add(itemPermisos);


            }

            MenuItem itemProveedores = crearItem("Proveedores", FontAwesomeIcon.TRUCK);
            itemProveedores.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Proveedores", () -> new ProveedoresDialog(getScene().getWindow()));
                }
            });

            MenuItem itemClientes = crearItem("Clientes", FontAwesomeIcon.USERS);
            itemClientes.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Clientes", () -> new ClientesDialog(getScene().getWindow()));
                }
            });

            MenuItem itemProductos = crearItem("Productos", FontAwesomeIcon.TAGS);
            itemProductos.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Productos", () -> new ProductosDialog(getScene().getWindow()));
                }
            });

            itemUbicaciones = crearItem("Ubicaciones Físicas", FontAwesomeIcon.MAP_MARKER);
            itemUbicaciones.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Ubicaciones Físicas", () -> new UbicacionesDialog(getScene().getWindow()));
                }
            });

            MenuItem itemCarriers = crearItem("Transportistas / Couriers", FontAwesomeIcon.TRUCK);
            itemCarriers.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Transportistas / Couriers", () -> new CarriersDialog(getScene().getWindow()));
                }
            });

            menuMantenimiento.getItems().addAll(itemProveedores, itemClientes, itemProductos, itemUbicaciones, itemCarriers);
        }

        // --- Menu Operaciones WMS ---
        menuWMS = null;
        menuCOM = null;


        if (isWMSUser) {
            menuCOM = crearMenuDesplegable("Operaciones COM", de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.BRIEFCASE);
            menuWMS = crearMenuDesplegable("Operaciones WH", de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.CUBES);

            itemStock = crearItem("Inventario General", FontAwesomeIcon.DATABASE);
            itemStock.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Inventario General", () -> new StockDialog(getScene().getWindow()));
                }
            });

            itemNotas = crearItem("Notas de Pedido", FontAwesomeIcon.FILE_TEXT_ALT);
            itemNotas.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Notas de Pedido", () -> new NotasPedidoDialog(getScene().getWindow()));
                }
            });

            itemBackOrders = crearItem("Back Orders NP", FontAwesomeIcon.HOURGLASS_HALF);
            itemBackOrders.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Back Orders", () -> new BackOrdersDialog(getScene().getWindow()));
                }
            });

            itemDespachos = crearItem("Despachos NP", FontAwesomeIcon.TRUCK);
            itemDespachos.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Despachos", () -> new DespachosDialog(getScene().getWindow()));
                }
            });

            itemAnuncios = crearItem("Anuncios de Carga", FontAwesomeIcon.BULLHORN);
            itemAnuncios.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Anuncios de Carga", () -> new AnunciosDialog(getScene().getWindow()));
                }
            });

            itemRecepciones = crearItem("Recepciones de Carga", FontAwesomeIcon.DOWNLOAD);
            itemRecepciones.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Recepciones de Carga", () -> new RecepcionesDialog(getScene().getWindow()));
                }
            });

            itemInventarioVisual = crearItem("Inventario Visual", FontAwesomeIcon.TH);
            itemInventarioVisual.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Inventario Visual", () -> new InventarioVisualDialog(getScene().getWindow()));
                }
            });

            itemPackingList = crearItem("Picking & Packing NP", FontAwesomeIcon.LIST);
            itemPackingList.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Picking & Packing NP", () -> new PackingListDialog(getScene().getWindow(), this));
                }
            });

            itemTracking = crearItem("Tracking de Envíos NP", FontAwesomeIcon.MAP_MARKER);
            itemTracking.setOnAction(e -> {
                if (validarContextoAdminSis()) {
                    abrirTab("Tracking de Envíos", () -> new TrackingDialog(getScene().getWindow()));
                }
            });

            menuCOM.getItems().addAll(
                    itemNotas,
                    itemBackOrders,
                    itemDespachos,
                    itemAnuncios);

            menuWMS.getItems().addAll(
                    itemStock,
                    itemInventarioVisual,
                    itemRecepciones,
                    itemPackingList,
                    itemTracking);
        }

        // Usuario desde AuthService
        Label lblNombre = new Label();
        Label lblRol = new Label();

        if (auth.isLoggedIn()) {
            lblNombre.setText(auth.getNombre() + " " + auth.getApellido());
            String rolStr = auth.getRole();
            if ("ADMINSIS".equalsIgnoreCase(rolStr)) {
                lblRol.setText("Administrador de Sistema");
            } else if ("ADMIN_BODEGA".equalsIgnoreCase(rolStr)) {
                lblRol.setText("Administrador de Bodega");
            } else if ("ASISTENTE_DOCUMENTAL".equalsIgnoreCase(rolStr)) {
                lblRol.setText("Asistente Documental");
            } else if ("ASISTENTE_BODEGA".equalsIgnoreCase(rolStr)) {
                lblRol.setText("Asistente de Bodega");
            } else if ("GESTOR_COMERCIAL".equalsIgnoreCase(rolStr)) {
                lblRol.setText("Gestor Comercial");
            } else {
                lblRol.setText(rolStr);
            }
        } else {
            lblNombre.setText(System.getProperty("user.name", "Usuario"));
            lblRol.setText("");
        }

        lblNombre.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        lblRol.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 9px; -fx-padding: 0;");

        VBox userProfileBox = new VBox(0, lblNombre, lblRol);
        userProfileBox.setAlignment(Pos.CENTER_LEFT);

        MenuButton menuUsuario = new MenuButton();
        menuUsuario.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-max-height: 24px; " +
                        "-fx-alignment: center; " +
                        "-fx-cursor: hand;");
        // Quitar la flecha del MenuButton
        menuUsuario.getStylesheets().add("data:text/css," +
                ".menu-button > .arrow-button { -fx-padding: 0; } " +
                ".menu-button > .arrow-button > .arrow { -fx-background-color: transparent; } " +
                ".menu-button { -fx-padding: 0; }");

        FontAwesomeIconView userProfileIcon = new FontAwesomeIconView(FontAwesomeIcon.USER);
        userProfileIcon.setFill(Color.WHITE);
        userProfileIcon.setSize("18px");
        menuUsuario.setGraphic(userProfileIcon);

        MenuItem itemPerfil = crearItem("Perfil", FontAwesomeIcon.USER);
        itemPerfil.setOnAction(e -> mostrarPerfil());

        MenuItem itemLogout = crearItem("Cerrar Sesión", FontAwesomeIcon.SIGN_OUT);
        itemLogout.setOnAction(e -> confirmarYCerrarSesion());

        menuUsuario.getItems().addAll(itemPerfil, itemLogout);

        // --- Notificaciones en Barra Superior ---
        menuNotificaciones = new MenuButton();
        menuNotificaciones.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-max-height: 24px; " +
                        "-fx-alignment: center; " +
                        "-fx-cursor: hand;");
        menuNotificaciones.getStylesheets().add("data:text/css," +
                ".menu-button > .arrow-button { -fx-padding: 0; } " +
                ".menu-button > .arrow-button > .arrow { -fx-background-color: transparent; } " +
                ".menu-button { -fx-padding: 0; } " +
                ".context-menu { -fx-background-color: white; -fx-background-radius: 12px; -fx-border-radius: 12px; -fx-border-color: #e2e8f0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 12, 0, 0, 4); -fx-padding: 0; }");

        StackPane bellStack = new StackPane();
        bellStack.setStyle("-fx-cursor: hand;");

        FontAwesomeIconView bellIcon = new FontAwesomeIconView(FontAwesomeIcon.BELL);
        bellIcon.setFill(Color.WHITE);
        bellIcon.setSize("16px");

        lblNotificationBadge = new Label("0");
        lblNotificationBadge.setStyle(
                "-fx-background-color: #e53e3e; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 8px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 1 4;");
        lblNotificationBadge.setVisible(false);
        lblNotificationBadge.setMouseTransparent(true);

        StackPane.setAlignment(lblNotificationBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(lblNotificationBadge, new Insets(-5, -5, 0, 0));

        bellStack.getChildren().addAll(bellIcon, lblNotificationBadge);
        menuNotificaciones.setGraphic(bellStack);

        menuNotificaciones.setOnShowing(e -> {
            unreadNotificationCount = 0;
            lblNotificationBadge.setText("0");
            lblNotificationBadge.setVisible(false);
        });

        // Header Item Desplegable
        HBox headerRow = new HBox(8);
        headerRow.setPrefWidth(310);
        headerRow.setPadding(new Insets(8, 12, 8, 12));
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        lblHeaderNotifTitle = new Label("Notificaciones");
        lblHeaderNotifTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        FontAwesomeIconView trashAllIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        trashAllIcon.setSize("11px");
        trashAllIcon.setFill(Color.web("#dc2626"));

        btnDescartarTodasNotif = new Button("Descartar todas", trashAllIcon);
        btnDescartarTodasNotif.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;");
        btnDescartarTodasNotif.setOnAction(e -> descartarTodasNotificaciones());

        headerRow.getChildren().addAll(lblHeaderNotifTitle, headerSpacer, btnDescartarTodasNotif);
        headerNotifMenuItem = new CustomMenuItem(headerRow);
        headerNotifMenuItem.setHideOnClick(false);

        // Empty State Item
        VBox emptyBox = new VBox(6);
        emptyBox.setPrefWidth(310);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(20, 12, 20, 12));

        FontAwesomeIconView emptyIcon = new FontAwesomeIconView(FontAwesomeIcon.BELL_SLASH);
        emptyIcon.setSize("24px");
        emptyIcon.setFill(Color.web("#cbd5e1"));

        Label lblEmpty = new Label("Sin notificaciones pendientes");
        lblEmpty.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        emptyBox.getChildren().addAll(emptyIcon, lblEmpty);
        emptyNotifMenuItem = new CustomMenuItem(emptyBox);
        emptyNotifMenuItem.setHideOnClick(false);

        // Footer Item
        HBox footerRow = new HBox();
        footerRow.setPrefWidth(310);
        footerRow.setAlignment(Pos.CENTER);
        footerRow.setPadding(new Insets(8, 12, 8, 12));
        footerRow.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        Button btnVerTodas = new Button("Ver historial completo", new FontAwesomeIconView(FontAwesomeIcon.LIST_ALT));
        btnVerTodas.setStyle("-fx-background-color: transparent; -fx-text-fill: #1e3a8a; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
        btnVerTodas.setOnAction(e -> {
            menuNotificaciones.hide();
            abrirTabNotificaciones();
        });
        footerRow.getChildren().add(btnVerTodas);
        footerNotifMenuItem = new CustomMenuItem(footerRow);
        footerNotifMenuItem.setHideOnClick(false);

        reconstruirMenuNotificaciones();

        // Preload latest 10 notifications from historical DB
        Platform.runLater(() -> {
            try {
                List<NotificationModel> list = service.obtenerNotificacionesFiltradas(null, null, null, true, 0, 10);
                for (int i = list.size() - 1; i >= 0; i--) {
                    NotificationModel m = list.get(i);
                    String timeOnly = "";
                    if (m.getCreatedAt() != null) {
                        try {
                            LocalDateTime dt = LocalDateTime.parse(m.getCreatedAt());
                            timeOnly = dt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                        } catch (Exception ex) {
                            timeOnly = m.getCreatedAt();
                        }
                    }
                    CustomMenuItem item = crearNotifMenuItem(m.getEntityType(), m.getUserName(), m.getMessageText(), timeOnly, m.getId());
                    notifItemsList.add(0, item);
                }
                while (notifItemsList.size() > 10) {
                    notifItemsList.remove(notifItemsList.size() - 1);
                }
                reconstruirMenuNotificaciones();
            } catch (Exception ex) {
                System.err.println("Error fetching initial notifications: " + ex.getMessage());
            }
        });

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        Region spacerRight = new Region();
        spacerRight.setPrefWidth(10);

        HBox barra = new HBox(8);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(5, 12, 5, 12));
        barra.setStyle("-fx-background-color: #0F3E6E;");

        // Agregar elementos dinámicamente a la barra
        barra.getChildren().addAll(vTitulo, esp);
        // Archivo y Exportar ya no estan en el menu superior, se manejan desde el Tab
        // de Packing List.
        if (menuCOM != null) {
            barra.getChildren().add(menuCOM);
        }
        if (menuWMS != null) {
            barra.getChildren().add(menuWMS);
        }

        if (menuMantenimiento != null) {
            barra.getChildren().add(menuMantenimiento);
        }

        barra.getChildren().addAll(menuAyuda, spacerRight, menuNotificaciones, userProfileBox, menuUsuario);
        return barra;
    }

    private MenuButton crearMenuDesplegable(String texto, FontAwesomeIcon icon) {
        MenuButton mb = new MenuButton(texto);
        if (icon != null) {
            FontAwesomeIconView iv = new FontAwesomeIconView(icon);
            iv.setFill(Color.WHITE);
            iv.setSize("11px");
            mb.setGraphic(iv);
        }
        mb.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2 6;");
        mb.getStylesheets().add("data:text/css," +
                ".menu-button > .label { -fx-text-fill: white; -fx-font-size: 11px; } " +
                ".menu-button:hover { -fx-background-color: rgba(255,255,255,0.15) !important; -fx-background-radius: 4; } "
                +
                ".menu-button:showing { -fx-background-color: rgba(255,255,255,0.2) !important; -fx-background-radius: 4; }");
        return mb;
    }

    private MenuItem crearItem(String texto, FontAwesomeIcon icon) {
        FontAwesomeIconView iv = new FontAwesomeIconView(icon);
        iv.setFill(Color.web("#0F3E6E"));
        iv.setSize("11px");
        return new MenuItem(texto, iv);
    }

    private Node crearCentro() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.setStyle("-fx-background-color: #f5f7fb;");

        this.homePane = new BorderPane();
        this.homePane.setStyle("-fx-background-color: #f5f7fb;");

        // Welcome / Center area (Inicio original)
        VBox welcomeBox = new VBox(20);
        welcomeBox.setAlignment(Pos.CENTER);
        welcomeBox.setPadding(new Insets(40));

        ImageView logoWMS = new ImageView();
        try {
            logoWMS.setImage(new Image(getClass().getResourceAsStream("/icon-lightbg.png")));
            logoWMS.setFitHeight(120);
            logoWMS.setPreserveRatio(true);
        } catch (Exception ignored) {
        }

        Label welcomeTitle = new Label("Bienvenido al WMS Packing List");
        welcomeTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        Label welcomeSubtitle = new Label("Use el menú superior para gestionar operaciones.");
        welcomeSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");

        welcomeBox.getChildren().addAll(logoWMS, welcomeTitle, welcomeSubtitle);
        homePane.setCenter(welcomeBox);

        // Info Panel / Right area
        homePane.setRight(crearPanelInformacionGeneral(false));

        WebSocketManager.NotificationListener notificationListener = (entity, action, user, msg, id) -> {
            addWebSocketNotificationToDropdown(entity, user, msg, id);
            Platform.runLater(() -> {
                if (vboxActivityFeed != null) {
                    String timeNow = java.time.LocalTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                    HBox item = crearFilaActivityFeed(entity, user, msg, timeNow);
                    vboxActivityFeed.getChildren().add(0, item);
                    if (vboxActivityFeed.getChildren().size() > 8) {
                        vboxActivityFeed.getChildren().remove(8);
                    }
                }
            });
        };
        WebSocketManager.getInstance().subscribeNotifications(notificationListener);

        // Subscribe to ExchangeRateStorageService updates
        Consumer<ExchangeRateModel> exchangeRateListener = model -> Platform.runLater(() -> actualizarVistaTipoCambio(model));
        ExchangeRateStorageService.getInstance().addChangeListener(exchangeRateListener);

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                WebSocketManager.getInstance().unsubscribeNotifications(notificationListener);
                ExchangeRateStorageService.getInstance().removeChangeListener(exchangeRateListener);
            }
        });

        Tab homeTab = new Tab("Inicio", homePane);
        homeTab.setClosable(false);

        dashboardTab = new Tab("Dashboard", crearDashboardInicio());
        dashboardTab.setClosable(false);

        tabPane.getTabs().addAll(homeTab, dashboardTab);

        // Load details asynchronously
        new Thread(() -> {
            try {
                AuthService auth = AuthService.getInstance();
                boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(auth.getRole());
                String companyId = auth.getCompanyId();
                String warehouseId = auth.getWarehouseId();

                List<CompanyModel> allCompanies = new ArrayList<>();
                List<WarehouseModel> allWarehouses = new ArrayList<>();

                if (isAdminSis) {
                    try {
                        allCompanies = service.obtenerEmpresas();
                        allWarehouses = service.obtenerBodegas();
                    } catch (Exception e) {
                        System.err.println("Error al obtener empresas/bodegas para ADMINSIS: " + e.getMessage());
                    }
                }

                String companyNameStr = "No asignado";
                String companyRutStr = "No asignado";
                CompanyModel tempCompany = null;
                if (!isAdminSis && companyId != null) {
                    try {
                        tempCompany = service.obtenerEmpresaPorId(companyId);
                        if (tempCompany != null) {
                            companyNameStr = tempCompany.getRazonSocial();
                            companyRutStr = tempCompany.getRut();
                        }
                    } catch (Exception e) {
                        System.err.println("Error al obtener empresa por ID: " + e.getMessage());
                    }
                }
                final CompanyModel finalCompany = tempCompany;

                String warehouseNameStr = "No asignado";
                String warehouseDirStr = "No asignado";
                if (!isAdminSis && warehouseId != null) {
                    try {
                        WarehouseModel w = service.obtenerBodegaPorId(warehouseId);
                        if (w != null) {
                            warehouseNameStr = w.getNombre();
                            warehouseDirStr = w.getDireccion();
                        }
                    } catch (Exception e) {
                        System.err.println("Error al obtener bodega por ID: " + e.getMessage());
                    }
                }

                List<UserModel> warehouseUsers = new ArrayList<>();
                if (!isAdminSis && warehouseId != null) {
                    try {
                        List<UserModel> allUsers = service.obtenerUsuarios();
                        for (UserModel u : allUsers) {
                            if (warehouseId.equals(u.getWarehouseId()) && Boolean.TRUE.equals(u.getConectado())) {
                                warehouseUsers.add(u);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error al obtener usuarios: " + e.getMessage());
                    }
                }

                final String fCompanyName = companyNameStr;
                final String fCompanyRut = companyRutStr;
                final String fWarehouseName = warehouseNameStr;
                final String fWarehouseDir = warehouseDirStr;
                final List<UserModel> fUsers = warehouseUsers;
                final List<CompanyModel> finalAllCompanies = allCompanies;
                final List<WarehouseModel> finalAllWarehouses = allWarehouses;

                Platform.runLater(() -> {
                    if (isAdminSis) {
                        configurarListenersEmpresaYBodega(finalAllCompanies, finalAllWarehouses);
                    } else {
                        if (lblEmpNameHome != null) lblEmpNameHome.setText(fCompanyName);
                        if (lblEmpNameDashboard != null) lblEmpNameDashboard.setText(fCompanyName);
                        if (lblEmpRutHome != null) lblEmpRutHome.setText("RUT: " + fCompanyRut);
                        if (lblEmpRutDashboard != null) lblEmpRutDashboard.setText("RUT: " + fCompanyRut);
                        if (lblBodNameHome != null) lblBodNameHome.setText(fWarehouseName);
                        if (lblBodNameDashboard != null) lblBodNameDashboard.setText(fWarehouseName);
                        if (lblBodDirHome != null) lblBodDirHome.setText(fWarehouseDir);
                        if (lblBodDirDashboard != null) lblBodDirDashboard.setText(fWarehouseDir);
                        aplicarPermisosEmpresa(finalCompany);

                        actualizarListaUsuariosConectados(fUsers);
                    }
                });

                // Cargar métricas del Dashboard
                try {
                    List<StockModel> stocks = service.obtenerStocks();
                    int totalUnidades = 0;
                    int totalSkus = (stocks != null) ? stocks.size() : 0;
                    if (stocks != null) {
                        for (StockModel s : stocks) {
                            if (s.getCantidad() != null) {
                                totalUnidades += s.getCantidad();
                            }
                        }
                    }
                    final int fTotalUnidades = totalUnidades;
                    final int fTotalSkus = totalSkus;

                    List<OrderNoteModel> notas = service.obtenerNotasPedido();
                    int notasPendientes = 0;
                    int totalNotas = (notas != null) ? notas.size() : 0;
                    double totalVentasUsd = 0;

                    if (notas != null) {
                        for (OrderNoteModel n : notas) {
                            String est = n.getEstado() != null ? n.getEstado().toUpperCase() : "";
                            if ("PENDIENTE".equals(est) || "EN_PROCESO".equals(est) || "PROCESANDO".equals(est)) {
                                notasPendientes++;
                            }
                            if ("DESPACHADO".equals(est) || "COMPLETADO".equals(est) || "FINALIZADO".equals(est) || "ENTREGADO".equals(est)) {
                                if (n.getDetails() != null) {
                                    for (com.logistics.packinglist.model.OrderNoteDetailModel d : n.getDetails()) {
                                        if (d.getPrecioUnitario() != null && d.getCantidadPedida() != null) {
                                            totalVentasUsd += d.getPrecioUnitario() * d.getCantidadPedida();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    final int fNotasPendientes = notasPendientes;
                    final int fTotalNotas = totalNotas;

                    // Obtener meta de ventas configurada manualmente para el mes/año actual
                    java.time.LocalDate dateNow = java.time.LocalDate.now();
                    SalesTargetModel salesTargetModel = SalesTargetStorageService.getInstance().getTarget(dateNow.getYear(), dateNow.getMonthValue());
                    final double fTargetUsd = salesTargetModel.getTargetUsd();
                    final double fTargetClp = salesTargetModel.getTargetClp();
                    final double fExchangeRate = salesTargetModel.getExchangeRate();
                    final String fMonthName = salesTargetModel.getMonthName();

                    final double fTotalVentasUsd = totalVentasUsd;

                    List<ReceptionAnnouncementModel> anuncios = service.obtenerAnunciosPendientes();
                    final int fAnunciosPendientes = (anuncios != null) ? anuncios.size() : 0;

                    List<PackingList> packings = service.obtenerPackingLists();
                    final int fPackings = (packings != null) ? packings.size() : 0;

                    java.util.Set<String> locsOcupadasSet = new java.util.HashSet<>();
                    if (stocks != null) {
                        for (StockModel s : stocks) {
                            if (s.getLocationId() != null && s.getCantidad() != null && s.getCantidad() > 0) {
                                locsOcupadasSet.add(s.getLocationId());
                            }
                        }
                    }

                    List<LocationModel> ubicaciones = (warehouseId != null)
                            ? service.obtenerUbicacionesPorBodega(warehouseId)
                            : service.obtenerUbicaciones();
                    int totalUbicaciones = (ubicaciones != null) ? ubicaciones.size() : 0;
                    int ocupadas = locsOcupadasSet.size();
                    final int fTotalUbicaciones = totalUbicaciones;
                    final int fOcupadas = ocupadas;

                    List<NotificationModel> recentNotifs = service.obtenerNotificacionesFiltradas(null, null,
                            null, true, 0, 6);

                    Platform.runLater(() -> {
                        if (lblKpiStock != null) {
                            lblKpiStock.setText(String.format("%,d pcs", fTotalUnidades));
                            lblKpiStockSub.setText(fTotalSkus + " referencias (SKUs)");
                        }
                        if (lblKpiNotas != null) {
                            lblKpiNotas.setText(fNotasPendientes + " pendientes");
                            lblKpiNotasSub.setText("De " + fTotalNotas + " órdenes registradas");
                        }
                        if (lblKpiRecepciones != null) {
                            lblKpiRecepciones.setText(fAnunciosPendientes + " cargas");
                            lblKpiRecepcionesSub.setText("Anuncios por ingresar");
                        }
                        if (lblKpiPackings != null) {
                            lblKpiPackings.setText(fPackings + " documentos");
                            lblKpiPackingsSub.setText("Packing Lists en sistema");
                        }
                        if (lblMetaVentasTitulo != null) {
                            lblMetaVentasTitulo.setText(String.format(
                                    "Meta de ventas mes de %s en USD: $%,.0f  |  CLP: $%,.0f",
                                    fMonthName, fTargetUsd, fTargetClp));
                        }
                        if (pbMetaVentas != null) {
                            double pctVentas = (fTargetUsd > 0) ? fTotalVentasUsd / fTargetUsd : 0;
                            pbMetaVentas.setProgress(pctVentas);
                            double totalVentasClp = fTotalVentasUsd * fExchangeRate;
                            if (pctVentas >= 1.0) {
                                pbMetaVentas.setStyle("-fx-accent: #38a169;"); // Progreso VERDE si se supera la meta
                                lblMetaVentasDetalle.setText(String.format(
                                        "🎉 ¡Meta Superada! Se ha logrado: $%,.0f USD / $%,.0f CLP (%.1f%% de la meta)",
                                        fTotalVentasUsd, totalVentasClp, pctVentas * 100));
                            } else {
                                pbMetaVentas.setStyle("-fx-accent: #3182ce;");
                                lblMetaVentasDetalle.setText(String.format(
                                        "Se ha logrado: $%,.0f USD / $%,.0f CLP (%.1f%% de la meta)",
                                        fTotalVentasUsd, totalVentasClp, pctVentas * 100));
                            }
                        }
                        if (pbOcupacion != null) {
                            if (fTotalUbicaciones > 0) {
                                double pct = (double) fOcupadas / fTotalUbicaciones;
                                pbOcupacion.setProgress(pct);
                                lblOcupacionDetalle.setText(String.format("%d de %d ubicaciones ocupadas (%.1f%%)",
                                        fOcupadas, fTotalUbicaciones, pct * 100));
                            } else {
                                pbOcupacion.setProgress(0);
                                lblOcupacionDetalle.setText("No hay ubicaciones registradas en la bodega activa.");
                            }
                        }
                        if (vboxActivityFeed != null) {
                            vboxActivityFeed.getChildren().clear();
                            if (recentNotifs == null || recentNotifs.isEmpty()) {
                                vboxActivityFeed.getChildren().add(new Label("No hay actividad reciente."));
                            } else {
                                for (NotificationModel n : recentNotifs) {
                                    String timeOnly = "";
                                    if (n.getCreatedAt() != null) {
                                        try {
                                            LocalDateTime dt = LocalDateTime.parse(n.getCreatedAt());
                                            timeOnly = dt
                                                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                                        } catch (Exception ex) {
                                            timeOnly = n.getCreatedAt();
                                        }
                                    }
                                    vboxActivityFeed.getChildren().add(crearFilaActivityFeed(n.getEntityType(),
                                            n.getUserName(), n.getMessageText(), timeOnly));
                                }
                            }
                        }
                    });
                } catch (Exception ex) {
                    System.err.println("Error al cargar métricas del Dashboard: " + ex.getMessage());
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (lblEmpNameHome != null) lblEmpNameHome.setText("Error al cargar");
                    if (lblEmpNameDashboard != null) lblEmpNameDashboard.setText("Error al cargar");
                    if (lblBodNameHome != null) lblBodNameHome.setText("Error al cargar");
                    if (lblBodNameDashboard != null) lblBodNameDashboard.setText("Error al cargar");
                    actualizarListaUsuariosConectadosText("Error de conexión");
                });
            }
        }).start();

        return tabPane;
    }

    private ScrollPane crearPanelInformacionGeneral(boolean isDashboard) {
        VBox infoPanel = new VBox(15);
        infoPanel.setPadding(new Insets(20));
        infoPanel.setStyle("-fx-background-color: transparent;");

        Label lblInfoTitle = new Label("INFORMACIÓN GENERAL", new FontAwesomeIconView(FontAwesomeIcon.INFO_CIRCLE));
        lblInfoTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        AuthService auth = AuthService.getInstance();
        boolean isAdminSis = "ADMINSIS".equalsIgnoreCase(auth.getRole());

        ComboBox<CompanyModel> cbEmp = new ComboBox<>();
        ComboBox<WarehouseModel> cbBod = new ComboBox<>();
        Label lblEmpName = new Label("Cargando...");
        Label lblEmpRut = new Label("");
        lblEmpRut.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        Label lblBodName = new Label("Cargando...");
        Label lblBodDir = new Label("");
        lblBodDir.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        VBox vboxUsersList = new VBox(4);
        vboxUsersList.getChildren().add(new Label("Cargando..."));

        if (isDashboard) {
            this.cbEmpresaActivaDashboard = cbEmp;
            this.cbBodegaActivaDashboard = cbBod;
            this.lblEmpNameDashboard = lblEmpName;
            this.lblEmpRutDashboard = lblEmpRut;
            this.lblBodNameDashboard = lblBodName;
            this.lblBodDirDashboard = lblBodDir;
            this.vboxUsersListDashboard = vboxUsersList;
        } else {
            this.cbEmpresaActivaHome = cbEmp;
            this.cbBodegaActivaHome = cbBod;
            this.lblEmpNameHome = lblEmpName;
            this.lblEmpRutHome = lblEmpRut;
            this.lblBodNameHome = lblBodName;
            this.lblBodDirHome = lblBodDir;
            this.vboxUsersListHome = vboxUsersList;
        }

        // Company Card
        VBox cardEmpresa = new VBox(5);
        cardEmpresa.setStyle(
                "-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-background-radius: 4;");
        Label lblEmpTitle = new Label("Empresa", new FontAwesomeIconView(FontAwesomeIcon.BRIEFCASE));
        lblEmpTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F3E6E;");
        if (isAdminSis) {
            cbEmp.setPromptText("Seleccione Empresa");
            cbEmp.setMaxWidth(Double.MAX_VALUE);
            cardEmpresa.getChildren().addAll(lblEmpTitle, cbEmp);
        } else {
            cardEmpresa.getChildren().addAll(lblEmpTitle, lblEmpName, lblEmpRut);
        }

        // Warehouse Card
        VBox cardBodega = new VBox(5);
        cardBodega.setStyle(
                "-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-background-radius: 4;");
        Label lblBodTitle = new Label("Bodega", new FontAwesomeIconView(FontAwesomeIcon.BUILDING));
        lblBodTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F3E6E;");
        if (isAdminSis) {
            cbBod.setPromptText("Seleccione Bodega");
            cbBod.setMaxWidth(Double.MAX_VALUE);
            cbBod.setDisable(true);
            cardBodega.getChildren().addAll(lblBodTitle, cbBod);
        } else {
            cardBodega.getChildren().addAll(lblBodTitle, lblBodName, lblBodDir);
        }

        // Exchange Rate Card
        VBox cardTipoCambio = new VBox(5);
        cardTipoCambio.setStyle(
                "-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-background-radius: 4;");
        Label lblRateTitle = new Label("Tipo de Cambio Bodega", new FontAwesomeIconView(FontAwesomeIcon.MONEY));
        lblRateTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        Button btnEditRate = new Button("⚙️ Editar");
        btnEditRate.setStyle("-fx-background-color: #edf2f7; -fx-text-fill: #2d3748; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4; -fx-cursor: hand;");
        btnEditRate.setOnAction(e -> {
            ConfigurarTipoCambioDialog dialog = new ConfigurarTipoCambioDialog();
            dialog.show(getScene().getWindow());
        });

        Region rSpacerRate = new Region();
        HBox.setHgrow(rSpacerRate, Priority.ALWAYS);
        HBox rateHeader = new HBox(6, lblRateTitle, rSpacerRate, btnEditRate);
        rateHeader.setAlignment(Pos.CENTER_LEFT);

        ExchangeRateModel currentRateModel = ExchangeRateStorageService.getInstance().getModel();

        Label lblRateVal = new Label(String.format("1 USD = $%,.0f CLP", currentRateModel.getExchangeRate()));
        lblRateVal.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2b6cb0;");

        Label lblRateUpdated = new Label("Actualizado: " + (currentRateModel.getUpdatedAt() != null ? currentRateModel.getUpdatedAt() : "Reciente"));
        lblRateUpdated.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");

        if (isDashboard) {
            this.lblExchangeRateDashboard = lblRateVal;
            this.lblExchangeRateUpdatedDashboard = lblRateUpdated;
        } else {
            this.lblExchangeRateHome = lblRateVal;
            this.lblExchangeRateUpdatedHome = lblRateUpdated;
        }

        cardTipoCambio.getChildren().addAll(rateHeader, lblRateVal, lblRateUpdated);

        // Current User Card
        VBox cardUsuario = new VBox(5);
        cardUsuario.setStyle(
                "-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-background-radius: 4;");
        Label lblUserTitle = new Label("Usuario Actual", new FontAwesomeIconView(FontAwesomeIcon.USER));
        lblUserTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F3E6E;");
        Label lblUserName = new Label(auth.getNombre() + " " + auth.getApellido());
        Label lblUserRole = new Label("Rol: " + auth.getRole());
        lblUserRole.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblUserEmail = new Label(auth.getEmail());
        lblUserEmail.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        cardUsuario.getChildren().addAll(lblUserTitle, lblUserName, lblUserRole, lblUserEmail);

        // Connected/Warehouse Users Card
        VBox cardUsuariosBodega = new VBox(5);
        cardUsuariosBodega.setStyle(
                "-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-background-radius: 4;");
        Label lblUsersTitle = new Label("Usuarios Conectados", new FontAwesomeIconView(FontAwesomeIcon.USERS));
        lblUsersTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F3E6E;");
        cardUsuariosBodega.getChildren().addAll(lblUsersTitle, vboxUsersList);

        infoPanel.getChildren().addAll(lblInfoTitle, cardEmpresa, cardBodega, cardTipoCambio, cardUsuario, cardUsuariosBodega);

        ScrollPane scrollPane = new ScrollPane(infoPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefWidth(320);
        scrollPane.setStyle(
                "-fx-background: #ffffff; -fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 0 0 0 1;");

        return scrollPane;
    }

    @SuppressWarnings("unchecked")
    private void configurarListenersEmpresaYBodega(List<CompanyModel> finalAllCompanies, List<WarehouseModel> finalAllWarehouses) {
        AuthService auth = AuthService.getInstance();

        ComboBox<CompanyModel>[] companyBoxes = new ComboBox[]{cbEmpresaActivaHome, cbEmpresaActivaDashboard};
        ComboBox<WarehouseModel>[] warehouseBoxes = new ComboBox[]{cbBodegaActivaHome, cbBodegaActivaDashboard};

        for (ComboBox<CompanyModel> box : companyBoxes) {
            if (box != null) {
                box.getItems().setAll(finalAllCompanies);
                box.setOnAction(e -> {
                    if (isSyncingSelection) return;
                    isSyncingSelection = true;
                    try {
                        CompanyModel selectedCompany = box.getValue();
                        for (ComboBox<CompanyModel> b : companyBoxes) {
                            if (b != null && b != box) {
                                b.setValue(selectedCompany);
                            }
                        }
                        if (selectedCompany != null) {
                            auth.setCompanyId(selectedCompany.getId());
                            List<WarehouseModel> filtered = new ArrayList<>();
                            for (WarehouseModel w : finalAllWarehouses) {
                                if (selectedCompany.getId().equals(w.getCompanyId())) {
                                    filtered.add(w);
                                }
                            }
                            for (ComboBox<WarehouseModel> wBox : warehouseBoxes) {
                                if (wBox != null) {
                                    wBox.getItems().setAll(filtered);
                                    wBox.setDisable(false);
                                    wBox.getSelectionModel().clearSelection();
                                }
                            }
                            auth.setWarehouseId(null);
                            aplicarPermisosEmpresa(selectedCompany);
                        } else {
                            for (ComboBox<WarehouseModel> wBox : warehouseBoxes) {
                                if (wBox != null) {
                                    wBox.getItems().clear();
                                    wBox.setDisable(true);
                                }
                            }
                            auth.setCompanyId(null);
                            auth.setWarehouseId(null);
                        }
                    } finally {
                        isSyncingSelection = false;
                    }
                });
            }
        }

        for (ComboBox<WarehouseModel> wBox : warehouseBoxes) {
            if (wBox != null) {
                wBox.setOnAction(e -> {
                    if (isSyncingSelection) return;
                    isSyncingSelection = true;
                    try {
                        WarehouseModel selectedWarehouse = wBox.getValue();
                        for (ComboBox<WarehouseModel> b : warehouseBoxes) {
                            if (b != null && b != wBox) {
                                b.setValue(selectedWarehouse);
                            }
                        }
                        if (selectedWarehouse != null) {
                            auth.setWarehouseId(selectedWarehouse.getId());
                            new Thread(() -> {
                                try {
                                    List<UserModel> allUsers = service.obtenerUsuarios();
                                    List<UserModel> wUsers = new ArrayList<>();
                                    for (UserModel u : allUsers) {
                                        if (selectedWarehouse.getId().equals(u.getWarehouseId())
                                                && Boolean.TRUE.equals(u.getConectado())) {
                                            wUsers.add(u);
                                        }
                                    }
                                    Platform.runLater(() -> actualizarListaUsuariosConectados(wUsers));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }).start();
                        } else {
                            auth.setWarehouseId(null);
                            actualizarListaUsuariosConectadosText("Seleccione una bodega activa");
                        }
                    } finally {
                        isSyncingSelection = false;
                    }
                });
            }
        }

        actualizarListaUsuariosConectadosText("Seleccione empresa y bodega activa");
    }

    private void actualizarListaUsuariosConectados(List<UserModel> wUsers) {
        VBox[] boxes = new VBox[]{vboxUsersListHome, vboxUsersListDashboard};
        for (VBox vboxUsersList : boxes) {
            if (vboxUsersList != null) {
                vboxUsersList.getChildren().clear();
                if (wUsers == null || wUsers.isEmpty()) {
                    vboxUsersList.getChildren().add(new Label("No hay otros usuarios conectados"));
                } else {
                    for (UserModel u : wUsers) {
                        Label lblU = new Label(u.getNombre() + " " + u.getApellido() + " (" + u.getRole() + ")");
                        lblU.setStyle("-fx-font-size: 11px; -fx-text-fill: #333;");
                        FontAwesomeIconView dot = new FontAwesomeIconView(FontAwesomeIcon.CIRCLE);
                        dot.setSize("8px");
                        dot.setFill(Color.web("#28a745"));
                        lblU.setGraphic(dot);
                        lblU.setGraphicTextGap(6);
                        vboxUsersList.getChildren().add(lblU);
                    }
                }
            }
        }
    }

    private void actualizarListaUsuariosConectadosText(String texto) {
        VBox[] boxes = new VBox[]{vboxUsersListHome, vboxUsersListDashboard};
        for (VBox vboxUsersList : boxes) {
            if (vboxUsersList != null) {
                vboxUsersList.getChildren().clear();
                vboxUsersList.getChildren().add(new Label(texto));
            }
        }
    }

    private Node crearDashboardInicio() {
        BorderPane dashboardPane = new BorderPane();
        dashboardPane.setStyle("-fx-background-color: #f5f7fb;");

        VBox root = new VBox(20);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #f5f7fb;");

        // 1. Saludo y Encabezado Ejecutivo (Hero Box)
        AuthService auth = AuthService.getInstance();
        Label lblSaludo = new Label("¡Hola, " + auth.getNombre() + "! 👋");
        lblSaludo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label lblSubsaludo = new Label(
                "Panel de Control Operativo WMS • Resumen ejecutivo en tiempo real.");
        lblSubsaludo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.88);");

        VBox titleBox = new VBox(4, lblSaludo, lblSubsaludo);

        Label lblBadge = new Label("🟢 BODEGA ACTIVA", new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE));
        lblBadge.setStyle("-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6px 14px; -fx-background-radius: 20px;");

        Region heroSpacer = new Region();
        HBox.setHgrow(heroSpacer, Priority.ALWAYS);

        HBox headerBox = new HBox(12, titleBox, heroSpacer, lblBadge);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(20, 24, 20, 24));
        headerBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #0F3E6E, #1E508C);" +
                "-fx-background-radius: 12px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(15,62,110,0.25), 10, 0, 0, 4);"
        );

        // 2. Tarjetas KPI Operativos (Row of 4)
        lblKpiStock = new Label("Calculando...");
        lblKpiStock.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblKpiStockSub = new Label("Cargando inventario...");
        lblKpiStockSub.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        VBox cardKpiStock = crearCardKpi("Stock General", FontAwesomeIcon.DATABASE, "cornflowerblue", lblKpiStock,
                lblKpiStockSub);

        lblKpiNotas = new Label("Calculando...");
        lblKpiNotas.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblKpiNotasSub = new Label("Cargando notas...");
        lblKpiNotasSub.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        VBox cardKpiNotas = crearCardKpi("Notas de Pedido", FontAwesomeIcon.FILE_TEXT, "#FF8A65", lblKpiNotas,
                lblKpiNotasSub);

        lblKpiRecepciones = new Label("Calculando...");
        lblKpiRecepciones.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblKpiRecepcionesSub = new Label("Cargando recepciones...");
        lblKpiRecepcionesSub.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        VBox cardKpiRecepciones = crearCardKpi("Recepciones", FontAwesomeIcon.INBOX, "#38a169", lblKpiRecepciones,
                lblKpiRecepcionesSub);

        lblKpiPackings = new Label("Calculando...");
        lblKpiPackings.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblKpiPackingsSub = new Label("Cargando packings...");
        lblKpiPackingsSub.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        VBox cardKpiPackings = crearCardKpi("Packing Lists", FontAwesomeIcon.LIST_ALT, "#805ad5", lblKpiPackings,
                lblKpiPackingsSub);

        HBox kpiGrid = new HBox(12, cardKpiStock, cardKpiNotas, cardKpiRecepciones, cardKpiPackings);
        HBox.setHgrow(cardKpiStock, Priority.ALWAYS);
        HBox.setHgrow(cardKpiNotas, Priority.ALWAYS);
        HBox.setHgrow(cardKpiRecepciones, Priority.ALWAYS);
        HBox.setHgrow(cardKpiPackings, Priority.ALWAYS);

        // 3. Accesos Rápidos
        Label lblAccesoTitle = new Label("ACCESOS RÁPIDOS", new FontAwesomeIconView(FontAwesomeIcon.BOLT));
        lblAccesoTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        VBox cardAcc1 = crearCardAccesoRapido("Crear Packing Manual", "Crear documento packing list manualmente",
                FontAwesomeIcon.PLUS_SQUARE, "#FF8A65", () -> {
                    ManualPackingDialog dialog = new ManualPackingDialog(getScene().getWindow(), inventarioService);
                    dialog.showAndWait();
                    if (dialog.getResultado() != null) {
                        guardarYMostrar(dialog.getResultado());
                    }
                });
        VBox cardAcc2 = crearCardAccesoRapido("Recepciones de Carga", "Ingresar y verificar cargas entrantes",
                FontAwesomeIcon.INBOX, "cornflowerblue", () -> {
                    if (validarContextoAdminSis()) {
                        abrirTab("Recepciones de Carga", () -> new RecepcionesDialog(getScene().getWindow()));
                    }
                });
        VBox cardAcc3 = crearCardAccesoRapido("Inventario General", "Consultar stock y movimientos",
                FontAwesomeIcon.DATABASE, "#FF8A65", () -> {
                    if (validarContextoAdminSis()) {
                        abrirTab("Inventario General", () -> new StockDialog(getScene().getWindow()));
                    }
                });
        VBox cardAcc4 = crearCardAccesoRapido("Notas de Pedido", "Gestionar ordenes y despachos",
                FontAwesomeIcon.FILE_TEXT, "cornflowerblue", () -> {
                    if (validarContextoAdminSis()) {
                        abrirTab("Notas de Pedido", () -> new NotasPedidoDialog(getScene().getWindow()));
                    }
                });

        HBox accesosGrid = new HBox(12, cardAcc1, cardAcc2, cardAcc3, cardAcc4);
        HBox.setHgrow(cardAcc1, Priority.ALWAYS);
        HBox.setHgrow(cardAcc2, Priority.ALWAYS);
        HBox.setHgrow(cardAcc3, Priority.ALWAYS);
        HBox.setHgrow(cardAcc4, Priority.ALWAYS);

        VBox accesosSection = new VBox(8, lblAccesoTitle, accesosGrid);

        // 4. Seccion Metas de Ventas por Mes (Loader arriba de Ocupacion de Ubicaciones)
        Label lblMetaVentasSectionTitle = new Label("METAS DE VENTAS POR MES",
                new FontAwesomeIconView(FontAwesomeIcon.LINE_CHART));
        lblMetaVentasSectionTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        Button btnConfigurarMeta = new Button("⚙️ Configurar Meta");
        btnConfigurarMeta.setStyle("-fx-background-color: #edf2f7; -fx-text-fill: #2d3748; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 9; -fx-background-radius: 4; -fx-cursor: hand;");
        btnConfigurarMeta.setOnAction(e -> {
            ConfigurarMetaVentasDialog dialog = new ConfigurarMetaVentasDialog();
            dialog.setOnTargetSavedListener(target -> actualizarMetaVentasDashboard(target));
            dialog.show(getScene().getWindow());
        });

        Region rSpacer = new Region();
        HBox.setHgrow(rSpacer, Priority.ALWAYS);
        HBox headerMetas = new HBox(8, lblMetaVentasSectionTitle, rSpacer, btnConfigurarMeta);
        headerMetas.setAlignment(Pos.CENTER_LEFT);

        java.time.LocalDate nowLocalDate = java.time.LocalDate.now();
        SalesTargetModel initialTargetModel = SalesTargetStorageService.getInstance().getTarget(nowLocalDate.getYear(), nowLocalDate.getMonthValue());

        lblMetaVentasTitulo = new Label(String.format(
                "Meta de ventas mes de %s en USD: $%,.0f  |  CLP: $%,.0f",
                initialTargetModel.getMonthName(), initialTargetModel.getTargetUsd(), initialTargetModel.getTargetClp()));
        lblMetaVentasTitulo.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        pbMetaVentas = new ProgressBar(0.0);
        pbMetaVentas.setMaxWidth(Double.MAX_VALUE);
        pbMetaVentas.setPrefHeight(16);
        pbMetaVentas.setStyle("-fx-accent: #3182ce;");

        lblMetaVentasDetalle = new Label("Calculando meta de ventas y avances...");
        lblMetaVentasDetalle.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");

        VBox metasVentasBox = new VBox(6, headerMetas, lblMetaVentasTitulo, pbMetaVentas, lblMetaVentasDetalle);
        metasVentasBox.setStyle(
                "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 14;");

        // 5. Seccion Ocupacion de Bodega
        Label lblOcupacionTitle = new Label("OCUPACIÓN DE UBICACIONES EN BODEGA",
                new FontAwesomeIconView(FontAwesomeIcon.MAP_MARKER));
        lblOcupacionTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        pbOcupacion = new ProgressBar(0.0);
        pbOcupacion.setMaxWidth(Double.MAX_VALUE);
        pbOcupacion.setPrefHeight(16);
        pbOcupacion.setStyle("-fx-accent: #3182ce;");

        lblOcupacionDetalle = new Label("Calculando ubicaciones disponibles...");
        lblOcupacionDetalle.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");

        VBox ocupacionBox = new VBox(6, lblOcupacionTitle, pbOcupacion, lblOcupacionDetalle);
        ocupacionBox.setStyle(
                "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 14;");

        // 6. Feed de Actividad Reciente
        Label lblFeedTitle = new Label("ACTIVIDAD RECIENTE EN BODEGA",
                new FontAwesomeIconView(FontAwesomeIcon.HISTORY));
        lblFeedTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        vboxActivityFeed = new VBox(6);
        vboxActivityFeed.getChildren().add(new Label("Cargando actividad..."));

        VBox feedBox = new VBox(10, lblFeedTitle, vboxActivityFeed);
        feedBox.setStyle(
                "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 14;");

        root.getChildren().addAll(headerBox, kpiGrid, accesosSection, metasVentasBox, ocupacionBox, feedBox);

        ScrollPane scrollCenter = new ScrollPane(root);
        scrollCenter.setFitToWidth(true);
        scrollCenter.setStyle("-fx-background: #f5f7fb; -fx-background-color: #f5f7fb; -fx-border-color: transparent;");

        dashboardPane.setCenter(scrollCenter);
        dashboardPane.setRight(crearPanelInformacionGeneral(true));

        return dashboardPane;
    }

    private VBox crearCardKpi(String titulo, FontAwesomeIcon icon, String colorHex, Label lblValor, Label lblSubtext) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setMinHeight(125);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-radius: 10px;" +
                "-fx-border-width: 3px 0px 0px 0px;" +
                "-fx-border-color: " + colorHex + " transparent transparent transparent;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("18px");
        iconView.setFill(Color.WHITE);

        StackPane iconBg = new StackPane(iconView);
        iconBg.setPrefSize(36, 36);
        iconBg.setMaxSize(36, 36);
        iconBg.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 8px;");

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        HBox topRow = new HBox(10, iconBg, lblTitle);
        topRow.setAlignment(Pos.CENTER_LEFT);

        lblValor.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblSubtext.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        card.getChildren().addAll(topRow, lblValor, lblSubtext);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-radius: 10px;" +
                "-fx-border-width: 3px 0px 0px 0px;" +
                "-fx-border-color: " + colorHex + " transparent transparent transparent;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 12, 0, 0, 5);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-radius: 10px;" +
                "-fx-border-width: 3px 0px 0px 0px;" +
                "-fx-border-color: " + colorHex + " transparent transparent transparent;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        ));

        return card;
    }

    private VBox crearCardAccesoRapido(String titulo, String subtext, FontAwesomeIcon icon, String colorHex,
            Runnable action) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setMinHeight(115);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-radius: 10px;" +
                "-fx-border-width: 0px 0px 0px 4px;" +
                "-fx-border-color: transparent transparent transparent " + colorHex + ";" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("18px");
        iconView.setFill(Color.web(colorHex));

        String pastelBg = "#eff6ff";
        if ("#FF8A65".equalsIgnoreCase(colorHex)) pastelBg = "#fff7ed";
        else if ("#38a169".equalsIgnoreCase(colorHex)) pastelBg = "#f0fdf4";
        else if ("#805ad5".equalsIgnoreCase(colorHex)) pastelBg = "#faf5ff";

        StackPane iconBg = new StackPane(iconView);
        iconBg.setPrefSize(38, 38);
        iconBg.setMaxSize(38, 38);
        iconBg.setStyle("-fx-background-color: " + pastelBg + "; -fx-background-radius: 10px;");

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        FontAwesomeIconView iconArrow = new FontAwesomeIconView(FontAwesomeIcon.ARROW_RIGHT);
        iconArrow.setSize("13px");
        iconArrow.setFill(Color.web("#94a3b8"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(10, iconBg, lblTitle, spacer, iconArrow);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblSub = new Label(subtext);
        lblSub.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        lblSub.setWrapText(true);

        card.getChildren().addAll(topRow, lblSub);

        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #f8fafc;" +
                    "-fx-background-radius: 10px;" +
                    "-fx-border-radius: 10px;" +
                    "-fx-border-width: 0px 0px 0px 4px;" +
                    "-fx-border-color: transparent transparent transparent " + colorHex + ";" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 12, 0, 0, 4);"
            );
            iconArrow.setTranslateX(4);
            iconArrow.setFill(Color.web(colorHex));
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 10px;" +
                    "-fx-border-radius: 10px;" +
                    "-fx-border-width: 0px 0px 0px 4px;" +
                    "-fx-border-color: transparent transparent transparent " + colorHex + ";" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2);"
            );
            iconArrow.setTranslateX(0);
            iconArrow.setFill(Color.web("#94a3b8"));
        });
        card.setOnMouseClicked(e -> action.run());

        return card;
    }

    private HBox crearFilaActivityFeed(String entityType, String user, String msg, String timeStr) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 8, 4, 8));
        row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 4;");

        FontAwesomeIcon icon = FontAwesomeIcon.INFO_CIRCLE;
        String iconColor = "#3182ce";
        if ("STOCK".equalsIgnoreCase(entityType) || "INVENTARIO".equalsIgnoreCase(entityType)) {
            icon = FontAwesomeIcon.DATABASE;
            iconColor = "#3182ce";
        } else if ("RECEPCION".equalsIgnoreCase(entityType) || "ANUNCIO".equalsIgnoreCase(entityType)) {
            icon = FontAwesomeIcon.DOWNLOAD;
            iconColor = "#38a169";
        } else if ("NOTA_PEDIDO".equalsIgnoreCase(entityType) || "BACK_ORDER".equalsIgnoreCase(entityType)) {
            icon = FontAwesomeIcon.FILE_TEXT_ALT;
            iconColor = "#dd6b20";
        } else if ("PACKING_LIST".equalsIgnoreCase(entityType)) {
            icon = FontAwesomeIcon.LIST;
            iconColor = "#805ad5";
        }

        FontAwesomeIconView iv = new FontAwesomeIconView(icon);
        iv.setSize("12px");
        iv.setFill(Color.web(iconColor));

        Label lblTime = new Label(timeStr != null ? timeStr : "");
        lblTime.setStyle("-fx-font-size: 10px; -fx-text-fill: #a0aec0; -fx-font-weight: bold;");

        Label lblUser = new Label((user != null && !user.isEmpty()) ? user + ":" : "");
        lblUser.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");

        Label lblMsg = new Label(msg);
        lblMsg.setStyle("-fx-font-size: 11px; -fx-text-fill: #2d3748;");

        row.getChildren().addAll(iv, lblTime, lblUser, lblMsg);
        return row;
    }

    // ===== Acciones =====

    void cargarExcel() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar Excel de logística");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        File archivo = fc.showOpenDialog(getScene().getWindow());
        if (archivo == null)
            return;

        lblEstado.setText("Cargando " + archivo.getName() + "...");

        new Thread(() -> {
            try {
                PackingList pl = excelService.leer(archivo);
                guardarYMostrar(pl);
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    lblEstado.setText("❌ Error: " + ex.getMessage());
                    alerta("Error al leer Excel", ex.getMessage());
                });
            }
        }).start();
    }

    void guardarYMostrar(PackingList pl) {
        try {
            storageService.guardar(pl);
            Platform.runLater(() -> mostrarPackingList(pl));
        } catch (IOException ex) {
            Platform.runLater(() -> {
                lblEstado.setText("❌ Error al guardar packing localmente: " + ex.getMessage());
                alerta("Error de persistencia", ex.getMessage());
                mostrarPackingList(pl);
            });
        }
    }

    /** Refresca todas las tabs a partir del estado actual del PackingList */
    void mostrarPackingList(PackingList pl) {
        this.packingListActual = pl;
        java.util.List<Tab> tabsToKeep = new java.util.ArrayList<>(openDialogTabs.values());
        tabPane.getTabs().clear();

        for (Bulto bulto : pl.getBultos()) {
            Tab t = new Tab();
            t.setText(bulto.getEtiqueta());
            t.setContent(crearVistaBulto(bulto, t));
            t.setClosable(false);
            tabPane.getTabs().add(t);
        }
        Tab resumenTab = new Tab("📋 Resumen", crearVistaResumen(pl));
        resumenTab.setClosable(false);
        tabPane.getTabs().add(resumenTab);

        tabPane.getTabs().addAll(tabsToKeep);

        lblOrden.setText(pl.getNumeroOrden());
        lblEstado.setText("✔ " + archivo(pl)
                + "   |   " + pl.getNumeroBultos() + " bultos"
                + "   |   " + pl.getTotalPiezas() + " piezas"
                + "   |   " + String.format("%.1f kg", pl.getPesoTotalKg()));

        itemExportarPDF.setDisable(false);
        itemExportarTodos.setDisable(false);
        itemAgrupar.setDisable(pl.getNumeroBultos() < 2);
        itemContenedor.setDisable(false);
        itemExportarSeleccion.setDisable(false);
        itemEditarPacking.setDisable(false);
    }

    void resetUI() {
        this.packingListActual = null;
        java.util.List<Tab> tabsToKeep = new java.util.ArrayList<>(openDialogTabs.values());
        tabPane.getTabs().clear();

        Tab homeTab = new Tab("Inicio", homePane);
        homeTab.setClosable(false);

        if (dashboardTab == null) {
            dashboardTab = new Tab("Dashboard", crearDashboardInicio());
            dashboardTab.setClosable(false);
        }

        tabPane.getTabs().addAll(homeTab, dashboardTab);

        tabPane.getTabs().addAll(tabsToKeep);
        tabPane.getSelectionModel().select(homeTab);

        if (lblOrden != null) lblOrden.setText("");
        if (lblEstado != null) lblEstado.setText("Sin archivo cargado");

        if (itemExportarPDF != null) itemExportarPDF.setDisable(true);
        if (itemExportarTodos != null) itemExportarTodos.setDisable(true);
        if (itemAgrupar != null) itemAgrupar.setDisable(true);
        if (itemContenedor != null) itemContenedor.setDisable(true);
        if (itemExportarSeleccion != null) itemExportarSeleccion.setDisable(true);
        if (itemEditarPacking != null) itemEditarPacking.setDisable(true);
    }

    private void actualizarMetaVentasDashboard(SalesTargetModel target) {
        if (target == null) return;
        new Thread(() -> {
            try {
                List<OrderNoteModel> notas = service.obtenerNotasPedido();
                double totalUsd = 0;
                if (notas != null) {
                    for (OrderNoteModel n : notas) {
                        String est = n.getEstado() != null ? n.getEstado().toUpperCase() : "";
                        if ("DESPACHADO".equals(est) || "COMPLETADO".equals(est) || "FINALIZADO".equals(est) || "ENTREGADO".equals(est)) {
                            if (n.getDetails() != null) {
                                for (com.logistics.packinglist.model.OrderNoteDetailModel d : n.getDetails()) {
                                    if (d.getPrecioUnitario() != null && d.getCantidadPedida() != null) {
                                        totalUsd += d.getPrecioUnitario() * d.getCantidadPedida();
                                    }
                                }
                            }
                        }
                    }
                }
                final double currentVentasUsd = totalUsd;

                Platform.runLater(() -> {
                    java.time.LocalDate now = java.time.LocalDate.now();
                    if (target.getYear() == now.getYear() && target.getMonth() == now.getMonthValue()) {
                        if (lblMetaVentasTitulo != null) {
                            lblMetaVentasTitulo.setText(String.format(
                                    "Meta de ventas mes de %s en USD: $%,.0f  |  CLP: $%,.0f",
                                    target.getMonthName(), target.getTargetUsd(), target.getTargetClp()));
                        }
                        if (pbMetaVentas != null && lblMetaVentasDetalle != null) {
                            double targetUsd = target.getTargetUsd();
                            double pct = (targetUsd > 0) ? currentVentasUsd / targetUsd : 0;
                            pbMetaVentas.setProgress(pct);
                            double totalVentasClp = currentVentasUsd * target.getExchangeRate();
                            if (pct >= 1.0) {
                                pbMetaVentas.setStyle("-fx-accent: #38a169;");
                                lblMetaVentasDetalle.setText(String.format(
                                        "🎉 ¡Meta Superada! Se ha logrado: $%,.0f USD / $%,.0f CLP (%.1f%% de la meta)",
                                        currentVentasUsd, totalVentasClp, pct * 100));
                            } else {
                                pbMetaVentas.setStyle("-fx-accent: #3182ce;");
                                lblMetaVentasDetalle.setText(String.format(
                                        "Se ha logrado: $%,.0f USD / $%,.0f CLP (%.1f%% de la meta)",
                                        currentVentasUsd, totalVentasClp, pct * 100));
                            }
                        }
                    }
                });
            } catch (Exception ex) {
                System.err.println("Error al actualizar meta de ventas dashboard: " + ex.getMessage());
            }
        }).start();
    }

    private void actualizarVistaTipoCambio(ExchangeRateModel model) {
        if (model == null) return;
        String textVal = String.format("1 USD = $%,.0f CLP", model.getExchangeRate());
        String textUpd = "Actualizado: " + (model.getUpdatedAt() != null ? model.getUpdatedAt() : "Reciente");

        if (lblExchangeRateHome != null) lblExchangeRateHome.setText(textVal);
        if (lblExchangeRateDashboard != null) lblExchangeRateDashboard.setText(textVal);
        if (lblExchangeRateUpdatedHome != null) lblExchangeRateUpdatedHome.setText(textUpd);
        if (lblExchangeRateUpdatedDashboard != null) lblExchangeRateUpdatedDashboard.setText(textUpd);

        // Actualizar la meta de ventas del mes activo con el nuevo tipo de cambio global si corresponde
        java.time.LocalDate now = java.time.LocalDate.now();
        SalesTargetModel currentTarget = SalesTargetStorageService.getInstance().getTarget(now.getYear(), now.getMonthValue());
        if (currentTarget != null) {
            currentTarget.setExchangeRate(model.getExchangeRate());
            currentTarget.setTargetClp(currentTarget.getTargetUsd() * model.getExchangeRate());
            try {
                SalesTargetStorageService.getInstance().saveTarget(currentTarget);
            } catch (Exception ignored) {}
            actualizarMetaVentasDashboard(currentTarget);
        }
    }

    private void abrirTab(String titulo, java.util.function.Supplier<javafx.stage.Stage> supplier) {
        if (openDialogTabs.containsKey(titulo)) {
            tabPane.getSelectionModel().select(openDialogTabs.get(titulo));
            return;
        }

        javafx.stage.Stage dialog = supplier.get();
        javafx.scene.Scene scene = dialog.getScene();
        if (scene != null) {
            javafx.scene.Parent root = scene.getRoot();
            scene.setRoot(new javafx.scene.Group()); // Desacoplar raíz de la escena original

            Tab tab = new Tab(titulo, root);
            tab.setClosable(true);

            if (dialog instanceof PermisosDialog) {
                ((PermisosDialog) dialog).setOnCloseCallback(() -> {
                    tabPane.getTabs().remove(tab);
                    openDialogTabs.remove(titulo);
                    dialog.close();
                });
            }

            tab.setOnClosed(e -> {
                openDialogTabs.remove(titulo);
                dialog.close(); // Limpiar recursos
            });

            openDialogTabs.put(titulo, tab);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        }
    }

    void abrirAgrupador() {
        if (packingListActual == null || packingListActual.getNumeroBultos() < 2)
            return;

        AgrupadorDialog dialog = new AgrupadorDialog(
                getScene().getWindow(),
                packingListActual,
                () -> {
                    actualizarMetadataEdicion();
                    mostrarPackingList(packingListActual);
                    guardarActual();
                });
        dialog.showAndWait();
    }

    // ===== Vistas de tabs =====

    private Node crearVistaBulto(Bulto bulto, Tab tabOwner) {
        Label lblNombre = new Label(bulto.getEtiqueta());
        lblNombre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        Label lblAlias = new Label("  Alias (opcional):");
        lblAlias.setStyle("-fx-font-size: 13px; -fx-text-fill: #667;");

        TextField txtAlias = new TextField(bulto.getAlias());
        txtAlias.setPromptText("Ej. compresores + molido");
        txtAlias.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #bbcce0; -fx-border-width: 0 0 1 0; -fx-font-size: 13px;");
        txtAlias.setPrefWidth(250);

        txtAlias.textProperty().addListener((obs, oldV, newV) -> {
            bulto.setAlias(newV);
        });

        HBox topFila = new HBox(lblNombre, lblAlias, txtAlias);
        topFila.setAlignment(Pos.CENTER_LEFT);

        Label lblDims = new Label(String.format(
                "Peso bruto: %.1f kg  |  Peso neto: %.1f kg  |  Dimensiones: %.0f × %.0f × %.0f cm  |  Volumen: %.3f m³",
                bulto.getPesoTotal(), bulto.getPesoNeto(),
                bulto.getLargo(), bulto.getAncho(), bulto.getAlto(), bulto.getVolumenM3()));
        lblDims.setStyle("-fx-text-fill: #456; -fx-font-size: 12px;");

        VBox header = new VBox(4, topFila, lblDims);
        header.setPadding(new Insets(12, 16, 10, 16));
        header.setStyle("-fx-background-color: #e8eef8; -fx-border-color: #c5d0e8; -fx-border-width: 0 0 1 0;");

        TableView<PackingItem> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PackingItem, String> colPN = new TableColumn<>("Part Number");
        colPN.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPartNumber()));
        colPN.setPrefWidth(160);

        TableColumn<PackingItem, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colDesc.setPrefWidth(400);

        TableColumn<PackingItem, Number> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCantidad()));
        colCant.setPrefWidth(80);
        colCant.setStyle("-fx-alignment: CENTER;");

        tabla.getColumns().addAll(colPN, colDesc, colCant);
        tabla.getItems().addAll(bulto.getItems());

        Label lblTotal = new Label("Total unidades: " + bulto.getTotalUnidades());
        lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F3E6E; -fx-font-size: 12px; -fx-padding: 6 16;");
        HBox footer = new HBox(lblTotal);
        footer.setStyle("-fx-background-color: #dce8f8; -fx-border-color: #c5d0e8; -fx-border-width: 1 0 0 0;");

        BorderPane vista = new BorderPane();
        vista.setTop(header);
        vista.setCenter(tabla);
        vista.setBottom(footer);
        return vista;
    }

    private Node crearVistaResumen(PackingList pl) {
        TableView<Bulto> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Bulto, String> cEt = new TableColumn<>("Bulto");
        cEt.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEtiqueta()));

        TableColumn<Bulto, String> cTipo = new TableColumn<>("Tipo");
        cTipo.setCellValueFactory(c -> {
            String t = c.getValue().getTipo();
            if (t == null)
                return new SimpleStringProperty("");
            // Remover números iniciales y espacios, ej: "1 PALLET" -> "PALLET"
            String sanitized = t.replaceAll("^\\d+\\s*", "").toUpperCase().trim();
            return new SimpleStringProperty(sanitized);
        });

        TableColumn<Bulto, Number> cItems = new TableColumn<>("Ítems");
        cItems.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getItems().size()));
        cItems.setStyle("-fx-alignment: CENTER;");

        TableColumn<Bulto, Number> cUnd = new TableColumn<>("Unidades");
        cUnd.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTotalUnidades()));
        cUnd.setStyle("-fx-alignment: CENTER;");

        TableColumn<Bulto, String> cPeso = new TableColumn<>("Peso bruto");
        cPeso.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.1f kg", c.getValue().getPesoTotal())));
        cPeso.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Bulto, String> cDims = new TableColumn<>("Dimensiones");
        cDims.setCellValueFactory(c -> {
            Bulto b = c.getValue();
            return new SimpleStringProperty(b.getLargo() > 0
                    ? String.format("%.0f×%.0f×%.0f cm", b.getLargo(), b.getAncho(), b.getAlto())
                    : "—");
        });

        tabla.getColumns().addAll(cEt, cTipo, cItems, cUnd, cPeso, cDims);
        tabla.getItems().addAll(pl.getBultos());

        Label lblTotales = new Label(String.format(
                "Total: %d bultos  |  %d piezas  |  Peso total: %.1f kg  |  Peso neto: %.1f kg",
                pl.getNumeroBultos(), pl.getTotalPiezas(), pl.getPesoTotalKg(), pl.getPesoNetoKg()));
        lblTotales.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F3E6E; -fx-font-size: 12px; -fx-padding: 8 16;");

        HBox footer = new HBox(lblTotales);
        footer.setStyle("-fx-background-color: #dce8f8; -fx-border-color: #c5d0e8; -fx-border-width: 1 0 0 0;");

        BorderPane vista = new BorderPane();
        vista.setCenter(tabla);
        vista.setBottom(footer);
        return vista;
    }

    // ===== Logo y exportación =====

    private void cargarConfiguracion() {
        new Thread(() -> {
            try {
                // Intentar obtener el logo local descargado, con reintentos de hasta 3 segundos
                int retries = 30;
                File f = null;
                while (retries > 0) {
                    f = com.logistics.packinglist.service.AuthService.getInstance().getLogoLocalFile();
                    if (f != null && f.exists() && f.length() > 0) {
                        break;
                    }
                    Thread.sleep(100);
                    retries--;
                }

                if (f != null && f.exists()) {
                    final File finalFile = f;
                    logoFile = f;
                    pdfService.setLogo(f);
                    javafx.application.Platform.runLater(() -> {
                        if (imgLogoPreview != null) {
                            imgLogoPreview.setImage(new Image(finalFile.toURI().toString()));
                        }
                    });
                } else {
                    logoFile = null;
                    pdfService.setLogo(null);
                    javafx.application.Platform.runLater(() -> {
                        if (imgLogoPreview != null) {
                            imgLogoPreview.setImage(null);
                        }
                    });
                }
            } catch (Exception e) {
                System.err.println("Error al cargar configuración de logo: " + e.getMessage());
            }
        }).start();
    }

    void exportarPDFBultoActivo() {
        if (packingListActual == null)
            return;
        int idx = tabPane.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= packingListActual.getBultos().size()) {
            exportarPDFCompleto();
            return;
        }
        Bulto bulto = packingListActual.getBultos().get(idx);

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        String np = packingListActual.getNumeroOrden().replace(" ", "_");
        fc.setInitialFileName(bulto.getNombreArchivo() + "_" + np + ".pdf");
        File destino = fc.showSaveDialog(getScene().getWindow());
        if (destino == null)
            return;

        PackingList mini = new PackingList();
        mini.setNumeroOrden(packingListActual.getNumeroOrden());
        mini.setFecha(packingListActual.getFecha());
        mini.addBulto(bulto);
        mini.setTotalPiezas(bulto.getTotalUnidades());
        mini.setPesoTotalKg(bulto.getPesoTotal());
        mini.setPesoNetoKg(bulto.getPesoNeto());

        try {
            pdfService.exportar(mini, destino);
            lblEstado.setText("✔ PDF exportado: " + destino.getName());
        } catch (IOException | DocumentException ex) {
            alerta("Error al exportar PDF", ex.getMessage());
        }
    }

    void exportarPDFCompleto() {
        if (packingListActual == null)
            return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar PDF completo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        String np = packingListActual.getNumeroOrden().replace(" ", "_");
        fc.setInitialFileName("PACKING_LIST_COMPLETO_" + np + ".pdf");
        File destino = fc.showSaveDialog(getScene().getWindow());
        if (destino == null)
            return;

        try {
            pdfService.exportar(packingListActual, destino);
            lblEstado.setText("✔ PDF completo exportado: " + destino.getName());
        } catch (IOException | DocumentException ex) {
            alerta("Error al exportar PDF", ex.getMessage());
        }
    }

    // ===== Utilidades =====

    private String archivo(PackingList pl) {
        return pl.getNombreArchivo().length() > 40
                ? pl.getNombreArchivo().substring(0, 40) + "..."
                : pl.getNombreArchivo();
    }

    void abrirExportarSeleccion() {
        if (packingListActual == null)
            return;
        new ExportarDialog(getScene().getWindow(), packingListActual, pdfService)
                .showAndWait();
    }

    void abrirContenedor() {
        if (packingListActual == null)
            return;
        new ContenedorDialog(getScene().getWindow(), packingListActual, logoFile)
                .showAndWait();

        // Al cerrar, guardamos el estado (que fue actualizado por el diálogo en el
        // objeto)
        actualizarMetadataEdicion();
        guardarActual();
    }

    private void actualizarMetadataEdicion() {
        if (packingListActual == null)
            return;
        String hoy = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String usuario = System.getProperty("user.name", "Usuario");
        packingListActual.setFechaEdicion(hoy);
        packingListActual.setUsuarioEdicion(usuario);
    }

    private void guardarActual() {
        if (packingListActual == null)
            return;
        try {
            storageService.guardar(packingListActual);
        } catch (IOException ex) {
            alerta("Error al guardar", ex.getMessage());
        }
    }

    private void alerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private boolean validarContextoAdminSis() {
        AuthService auth = AuthService.getInstance();
        if ("ADMINSIS".equalsIgnoreCase(auth.getRole())) {
            if (auth.getCompanyId() == null || auth.getWarehouseId() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selección Requerida");
                alert.setHeaderText("Falta Contexto de Operación");
                alert.setContentText(
                        "Debe seleccionar una Empresa y una Bodega activas en el panel lateral de 'Inicio' para poder realizar operaciones.");
                alert.showAndWait();
                return false;
            }
        }
        return true;
    }

    public boolean userHasPermission(String perm) {
        com.logistics.packinglist.service.AuthService auth = com.logistics.packinglist.service.AuthService.getInstance();
        if ("ADMINSIS".equalsIgnoreCase(auth.getRole())) {
            return true;
        }
        return activePermissions.isEmpty() || activePermissions.contains(perm);
    }

    private void aplicarPermisosEmpresa(com.logistics.packinglist.model.CompanyModel company) {
        activePermissions.clear();
        if (company == null) {
            setPermissionStatus(true);
            return;
        }
        String permStr = company.getPermisos();
        if (permStr == null || permStr.trim().isEmpty()) {
            // Enable/show all by default
            setPermissionStatus(true);
            return;
        }

        java.util.Set<String> perms = new java.util.HashSet<>(java.util.Arrays.asList(permStr.split(",")));
        activePermissions.addAll(perms);

        // Archivo menu items
        if (itemPackingLists != null)
            itemPackingLists.setVisible(perms.contains("FILE_PACKING_LISTS"));
        if (itemCrearManual != null)
            itemCrearManual.setVisible(perms.contains("FILE_CREATE_MANUAL"));
        if (itemEditarPacking != null)
            itemEditarPacking.setVisible(perms.contains("FILE_EDIT_PACKING"));
        if (itemAgrupar != null)
            itemAgrupar.setVisible(perms.contains("FILE_AGRUPAR"));
        if (itemContenedor != null)
            itemContenedor.setVisible(perms.contains("FILE_CONTENEDOR"));

        // WMS items
        if (itemUbicaciones != null)
            itemUbicaciones.setVisible(perms.contains("OP_UBICACIONES"));
        if (itemStock != null)
            itemStock.setVisible(perms.contains("OP_STOCK"));
        if (itemNotas != null)
            itemNotas.setVisible(perms.contains("OP_NOTAS"));
        if (itemDespachos != null)
            itemDespachos.setVisible(perms.contains("OP_DESPACHOS"));
        if (itemAnuncios != null)
            itemAnuncios.setVisible(perms.contains("OP_ANUNCIOS"));
        if (itemRecepciones != null)
            itemRecepciones.setVisible(perms.contains("OP_RECEPCIONES"));

        // Hide entire menuWMS if no WMS features are visible
        if (menuWMS != null) {
            boolean hasAnyWms = perms.contains("OP_STOCK")
                    || perms.contains("OP_NOTAS") || perms.contains("OP_DESPACHOS")
                    || perms.contains("OP_ANUNCIOS") || perms.contains("OP_RECEPCIONES")
                    || perms.contains("FILE_PACKING_LISTS");
            menuWMS.setVisible(hasAnyWms);
        }
        if (itemPackingList != null) {
            itemPackingList.setVisible(perms.contains("FILE_PACKING_LISTS"));
        }
        if (menuArchivo != null) {
            boolean hasAnyArchivo = perms.contains("FILE_PACKING_LISTS")
                    || perms.contains("FILE_CREATE_MANUAL")
                    || perms.contains("FILE_EDIT_PACKING")
                    || perms.contains("FILE_AGRUPAR")
                    || perms.contains("FILE_CONTENEDOR");
            menuArchivo.setVisible(hasAnyArchivo);
        }
        if (menuExportar != null) {
            boolean hasAnyExport = perms.contains("FILE_PACKING_LISTS")
                    || perms.contains("FILE_CREATE_MANUAL");
            menuExportar.setVisible(hasAnyExport);
        }
    }

    private void setPermissionStatus(boolean visible) {
        if (itemPackingLists != null)
            itemPackingLists.setVisible(visible);
        if (itemCrearManual != null)
            itemCrearManual.setVisible(visible);
        if (itemEditarPacking != null)
            itemEditarPacking.setVisible(visible);
        if (itemAgrupar != null)
            itemAgrupar.setVisible(visible);
        if (itemContenedor != null)
            itemContenedor.setVisible(visible);

        if (itemUbicaciones != null)
            itemUbicaciones.setVisible(visible);
        if (itemStock != null)
            itemStock.setVisible(visible);
        if (itemNotas != null)
            itemNotas.setVisible(visible);
        if (itemDespachos != null)
            itemDespachos.setVisible(visible);
        if (itemAnuncios != null)
            itemAnuncios.setVisible(visible);
        if (itemRecepciones != null)
            itemRecepciones.setVisible(visible);

        if (menuCOM != null)
            menuCOM.setVisible(visible);
        if (menuWMS != null)
            menuWMS.setVisible(visible);
        if (itemPackingList != null)
            itemPackingList.setVisible(visible);
        if (menuArchivo != null)
            menuArchivo.setVisible(visible);
        if (menuExportar != null)
            menuExportar.setVisible(visible);
    }

    private void reconstruirMenuNotificaciones() {
        if (menuNotificaciones == null) return;
        menuNotificaciones.getItems().clear();
        if (headerNotifMenuItem != null) {
            menuNotificaciones.getItems().add(headerNotifMenuItem);
        }

        int notifCount = notifItemsList.size();
        if (notifCount > 0) {
            if (lblHeaderNotifTitle != null) {
                lblHeaderNotifTitle.setText("Notificaciones (" + notifCount + ")");
            }
            if (btnDescartarTodasNotif != null) {
                btnDescartarTodasNotif.setVisible(true);
                btnDescartarTodasNotif.setManaged(true);
            }
            for (CustomMenuItem item : notifItemsList) {
                menuNotificaciones.getItems().add(item);
            }
        } else {
            if (lblHeaderNotifTitle != null) {
                lblHeaderNotifTitle.setText("Notificaciones");
            }
            if (btnDescartarTodasNotif != null) {
                btnDescartarTodasNotif.setVisible(false);
                btnDescartarTodasNotif.setManaged(false);
            }
            if (emptyNotifMenuItem != null) {
                menuNotificaciones.getItems().add(emptyNotifMenuItem);
            }
        }

        if (footerNotifMenuItem != null) {
            menuNotificaciones.getItems().add(footerNotifMenuItem);
        }
    }

    private void descartarTodasNotificaciones() {
        notifItemsList.clear();
        unreadNotificationCount = 0;
        if (lblNotificationBadge != null) {
            lblNotificationBadge.setText("0");
            lblNotificationBadge.setVisible(false);
        }
        reconstruirMenuNotificaciones();
        
        new Thread(() -> {
            try {
                service.descartarTodasNotificaciones();
            } catch (Exception ex) {
                System.err.println("Error clearing all notifications: " + ex.getMessage());
            }
        }).start();
    }

    private void descartarNotificacionIndividual(CustomMenuItem item) {
        notifItemsList.remove(item);
        reconstruirMenuNotificaciones();
        
        String notifId = (String) item.getUserData();
        if (notifId != null) {
            new Thread(() -> {
                try {
                    service.descartarNotificacion(notifId);
                } catch (Exception ex) {
                    System.err.println("Error discarding notification: " + ex.getMessage());
                }
            }).start();
        }
    }

    private CustomMenuItem crearNotifMenuItem(String entity, String user, String msg, String timeStr, String id) {
        HBox row = new HBox(10);
        row.setPrefWidth(310);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        FontAwesomeIcon iconType = FontAwesomeIcon.INFO_CIRCLE;
        String iconColor = "#3b82f6"; // Blue
        if ("ORDER_NOTE".equals(entity)) {
            iconType = FontAwesomeIcon.FILE_TEXT_ALT;
            iconColor = "#d97706"; // Amber
        } else if ("DISPATCH".equals(entity)) {
            iconType = FontAwesomeIcon.TRUCK;
            iconColor = "#dc2626"; // Red
        } else if ("ANNOUNCEMENT".equals(entity)) {
            iconType = FontAwesomeIcon.BULLHORN;
            iconColor = "#7c3aed"; // Purple
        } else if ("RECEPTION".equals(entity)) {
            iconType = FontAwesomeIcon.DOWNLOAD;
            iconColor = "#16a34a"; // Green
        }

        FontAwesomeIconView iconView = new FontAwesomeIconView(iconType);
        iconView.setSize("14px");
        iconView.setFill(Color.web(iconColor));

        VBox textContainer = new VBox(2);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        HBox userTimeRow = new HBox(6);
        userTimeRow.setAlignment(Pos.CENTER_LEFT);

        Label lblUser = new Label(user != null ? user : "Sistema");
        lblUser.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #0f172a;");
        HBox.setHgrow(lblUser, Priority.ALWAYS);

        Label lblTime = new Label(timeStr != null ? timeStr : "");
        lblTime.setStyle("-fx-font-size: 9px; -fx-text-fill: #94a3b8;");

        userTimeRow.getChildren().addAll(lblUser, lblTime);

        Label lblText = new Label(msg);
        lblText.setWrapText(true);
        lblText.setMaxWidth(220);
        lblText.setStyle("-fx-font-size: 10px; -fx-text-fill: #334155;");

        textContainer.getChildren().addAll(userTimeRow, lblText);

        FontAwesomeIconView trashIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        trashIcon.setSize("11px");
        trashIcon.setFill(Color.web("#94a3b8"));

        Button btnTrash = new Button(null, trashIcon);
        btnTrash.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2 4;");
        btnTrash.setOnMouseEntered(e -> trashIcon.setFill(Color.web("#ef4444")));
        btnTrash.setOnMouseExited(e -> trashIcon.setFill(Color.web("#94a3b8")));

        CustomMenuItem[] itemHolder = new CustomMenuItem[1];
        btnTrash.setOnAction(e -> {
            if (itemHolder[0] != null) {
                descartarNotificacionIndividual(itemHolder[0]);
            }
        });

        row.getChildren().addAll(iconView, textContainer, btnTrash);

        CustomMenuItem item = new CustomMenuItem(row);
        item.setHideOnClick(false);
        item.setUserData(id);
        itemHolder[0] = item;
        return item;
    }

    private void addWebSocketNotificationToDropdown(String entity, String user, String msg, String id) {
        Platform.runLater(() -> {
            if (menuNotificaciones == null)
                return;

            if (!menuNotificaciones.isShowing()) {
                unreadNotificationCount++;
                lblNotificationBadge.setText(String.valueOf(unreadNotificationCount));
                lblNotificationBadge.setVisible(true);
            }

            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
            String timeStr = java.time.LocalTime.now().format(dtf);

            CustomMenuItem item = crearNotifMenuItem(entity, user, msg, timeStr, id);

            notifItemsList.add(0, item);

            while (notifItemsList.size() > 10) {
                notifItemsList.remove(notifItemsList.size() - 1);
            }

            reconstruirMenuNotificaciones();
        });
    }

    private void abrirTabNotificaciones() {
        abrirTab("Notificaciones", () -> new NotificacionesDialog(getScene().getWindow()));
    }

    PackingStorageService getStorageService() {
        return storageService;
    }

    PackingList getPackingListActual() {
        return packingListActual;
    }

    com.logistics.packinglist.service.InventarioService getInventarioService() {
        return inventarioService;
    }

    void crearPackingManual() {
        ManualPackingDialog dialog = new ManualPackingDialog(getScene().getWindow(), inventarioService);
        dialog.showAndWait();
        if (dialog.getResultado() != null) {
            guardarYMostrar(dialog.getResultado());
        }
    }

    public void abrirPackingListTab(PackingList pl) {
        String titulo = "Picking & Packing - " + pl.getNumeroOrden();
        if (openDialogTabs.containsKey(titulo)) {
            tabPane.getSelectionModel().select(openDialogTabs.get(titulo));
            return;
        }

        PackingListTabContent content = new PackingListTabContent(getScene().getWindow(), pl, this);
        Tab tab = new Tab(titulo, content);
        tab.setClosable(true);
        tab.setOnClosed(e -> openDialogTabs.remove(titulo));

        openDialogTabs.put(titulo, tab);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    public void abrirTabGuia() {
        String titulo = "Guía de Uso WMS";
        if (openDialogTabs.containsKey(titulo)) {
            tabPane.getSelectionModel().select(openDialogTabs.get(titulo));
            return;
        }

        GuiaTabContent content = new GuiaTabContent();
        Tab tab = new Tab(titulo, content);
        FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.BOOK);
        iconView.setSize("12px");
        iconView.setFill(Color.web("#1e3a8a"));
        tab.setGraphic(iconView);
        tab.setClosable(true);
        tab.setOnClosed(e -> openDialogTabs.remove(titulo));

        openDialogTabs.put(titulo, tab);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    public PdfExportService getPdfService() {
        return pdfService;
    }

    public File getLogoFile() {
        return logoFile;
    }
}
