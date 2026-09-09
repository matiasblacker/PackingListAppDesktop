package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.NotificationModel;
import com.logistics.packinglist.service.MantenimientoService;
import com.logistics.packinglist.service.WebSocketManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificacionesDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

        private final ObservableList<NotificationModel> listNotificaciones;
    private TableView<NotificationModel> tablaNotificaciones;

    private ComboBox<String> cbTipo;
    private DatePicker dpInicio;
    private DatePicker dpFin;
    
    private Button btnFiltrar;
    private Button btnLimpiar;
    
    private Button btnAnterior;
    private Button btnSiguiente;
    private Label lblPagina;

    private int currentPage = 0;
    private final int pageSize = 15;

    public NotificacionesDialog(Window owner) {
                this.listNotificaciones = FXCollections.observableArrayList();

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Historial de Notificaciones");
        setMinWidth(900);
        setMinHeight(600);

        construirUI();
        cargarDatos();

        // Escucha notificaciones en tiempo real para recargar si estamos en la primera página
        WebSocketManager.NotificationListener updateListener = (entity, action, user, msg, id) -> {
            if (currentPage == 0) {
                Platform.runLater(this::cargarDatos);
            }
        };
        WebSocketManager.getInstance().subscribeNotifications(updateListener);
        setOnCloseRequest(e -> WebSocketManager.getInstance().unsubscribeNotifications(updateListener));
    }

    private void construirUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #ffffff;");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblTitle = new Label("Historial de Notificaciones", new FontAwesomeIconView(FontAwesomeIcon.BELL));
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");
        header.getChildren().add(lblTitle);

        // Barra de Filtros
        HBox filtersBar = new HBox(12);
        filtersBar.setAlignment(Pos.CENTER_LEFT);
        filtersBar.setPadding(new Insets(10));
        filtersBar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-background-radius: 4;");

        // Combo Tipo Entidad
        VBox colTipo = new VBox(4);
        Label lblTipo = new Label("Tipo de Evento:");
        lblTipo.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        cbTipo = new ComboBox<>();
        cbTipo.getItems().addAll("TODOS", "ORDER_NOTE", "DISPATCH", "ANNOUNCEMENT", "RECEPTION");
        cbTipo.setValue("TODOS");
        cbTipo.setPrefWidth(150);
        cbTipo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(traducirTipo(item));
                }
            }
        });
        cbTipo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(traducirTipo(item));
                }
            }
        });
        colTipo.getChildren().addAll(lblTipo, cbTipo);

        // DatePicker Inicio
        VBox colInicio = new VBox(4);
        Label lblInicio = new Label("Fecha Desde:");
        lblInicio.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        dpInicio = new DatePicker();
        dpInicio.setPrefWidth(130);
        colInicio.getChildren().addAll(lblInicio, dpInicio);

        // DatePicker Fin
        VBox colFin = new VBox(4);
        Label lblFin = new Label("Fecha Hasta:");
        lblFin.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        dpFin = new DatePicker();
        dpFin.setPrefWidth(130);
        colFin.getChildren().addAll(lblFin, dpFin);

        // Botones de filtro
        HBox btnsBox = new HBox(8);
        btnsBox.setAlignment(Pos.BOTTOM_LEFT);
        
        btnFiltrar = new Button("Filtrar", new FontAwesomeIconView(FontAwesomeIcon.FILTER));
        btnFiltrar.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnFiltrar.setOnAction(e -> {
            currentPage = 0;
            cargarDatos();
        });

        btnLimpiar = new Button("Limpiar", new FontAwesomeIconView(FontAwesomeIcon.REFRESH));
        btnLimpiar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnLimpiar.setOnAction(e -> {
            cbTipo.setValue("TODOS");
            dpInicio.setValue(null);
            dpFin.setValue(null);
            currentPage = 0;
            cargarDatos();
        });
        
        btnsBox.getChildren().addAll(btnFiltrar, btnLimpiar);

        filtersBar.getChildren().addAll(colTipo, colInicio, colFin, btnsBox);

        // Tabla de Notificaciones
        tablaNotificaciones = new TableView<>();
        tablaNotificaciones.setPlaceholder(new Label("No se encontraron notificaciones con los filtros aplicados."));

        TableColumn<NotificationModel, String> colFecha = new TableColumn<>("Fecha / Hora");
        colFecha.setCellValueFactory(data -> {
            String dateStr = data.getValue().getCreatedAt();
            if (dateStr == null) return new SimpleStringProperty("");
            try {
                LocalDateTime dt = LocalDateTime.parse(dateStr);
                return new SimpleStringProperty(dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            } catch (Exception e) {
                return new SimpleStringProperty(dateStr);
            }
        });
        colFecha.setPrefWidth(160);

        TableColumn<NotificationModel, String> colModulo = new TableColumn<>("Módulo");
        colModulo.setCellValueFactory(data -> new SimpleStringProperty(traducirTipo(data.getValue().getEntityType())));
        colModulo.setPrefWidth(140);

        TableColumn<NotificationModel, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));
        colUsuario.setPrefWidth(150);

        TableColumn<NotificationModel, String> colMsg = new TableColumn<>("Mensaje");
        colMsg.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMessageText()));
        colMsg.setPrefWidth(400);

        tablaNotificaciones.getColumns().addAll(colFecha, colModulo, colUsuario, colMsg);
        tablaNotificaciones.setItems(listNotificaciones);
        VBox.setVgrow(tablaNotificaciones, Priority.ALWAYS);

        // Paginación
        HBox paginationBar = new HBox(15);
        paginationBar.setAlignment(Pos.CENTER);
        paginationBar.setPadding(new Insets(10, 0, 0, 0));

        btnAnterior = new Button(null, new FontAwesomeIconView(FontAwesomeIcon.CHEVRON_LEFT));
        btnAnterior.setStyle("-fx-cursor: hand;");
        btnAnterior.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                cargarDatos();
            }
        });

        lblPagina = new Label("Página 1");
        lblPagina.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        btnSiguiente = new Button(null, new FontAwesomeIconView(FontAwesomeIcon.CHEVRON_RIGHT));
        btnSiguiente.setStyle("-fx-cursor: hand;");
        btnSiguiente.setOnAction(e -> {
            currentPage++;
            cargarDatos();
        });

        paginationBar.getChildren().addAll(btnAnterior, lblPagina, btnSiguiente);

        root.getChildren().addAll(header, filtersBar, tablaNotificaciones, paginationBar);

        Scene scene = new Scene(root);
        setScene(scene);
    }

    private void cargarDatos() {
        String entityType = cbTipo.getValue();
        LocalDate startVal = dpInicio.getValue();
        LocalDate endVal = dpFin.getValue();

        String startStr = startVal != null ? startVal.toString() : null;
        String endStr = endVal != null ? endVal.toString() : null;

        btnAnterior.setDisable(currentPage == 0);

        new Thread(() -> {
            try {
                List<NotificationModel> data = service.obtenerNotificacionesFiltradas(entityType, startStr, endStr, currentPage, pageSize);
                Platform.runLater(() -> {
                    listNotificaciones.clear();
                    listNotificaciones.addAll(data);
                    lblPagina.setText("Página " + (currentPage + 1));
                    
                    // Si recibimos menos datos de los solicitados, desactivamos el botón siguiente
                    btnSiguiente.setDisable(data.size() < pageSize);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    System.err.println("Error al cargar notificaciones históricas: " + ex.getMessage());
                    btnSiguiente.setDisable(true);
                });
            }
        }).start();
    }

    private String traducirTipo(String type) {
        if (type == null) return "";
        switch (type.toUpperCase()) {
            case "ORDER_NOTE":
                return "Nota de Pedido";
            case "DISPATCH":
                return "Despacho";
            case "ANNOUNCEMENT":
                return "Anuncio de Carga";
            case "RECEPTION":
                return "Recepción";
            case "TODOS":
                return "Todos";
            default:
                return type;
        }
    }
}
