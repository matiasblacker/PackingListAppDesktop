package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import com.logistics.packinglist.model.PackingList;
import com.logistics.packinglist.service.PackingStorageService;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

public class PackingBrowserDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    private final PackingStorageService storageService;
    private PackingList packingSeleccionado = null;
    private final PackingList plAbierto;
    private final Runnable onEliminarActivo;

    private TableView<PackingList> tabla;
    private ObservableList<PackingList> listaPackings;

    public PackingBrowserDialog(Window owner, PackingStorageService storageService, 
                                PackingList plAbierto, Runnable onEliminarActivo) {
        this.storageService = storageService;
        this.plAbierto = plAbierto;
        this.onEliminarActivo = onEliminarActivo;

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Packing Lists creados");
        setMinWidth(950); // Aumentado para las nuevas columnas
        setMinHeight(400);
        construirUI();
        cargarDatos();
    }

    private void construirUI() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f5f7fb;");

        Label titulo = new Label("Seleccione un Packing List para abrir:");
        titulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");
        VBox top = new VBox(titulo);
        top.setPadding(new Insets(0, 0, 10, 0));
        root.setTop(top);

        tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PackingList, String> colPO = new TableColumn<>("Nota Pedido");
        colPO.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroOrden()));
        colPO.setPrefWidth(120);

        TableColumn<PackingList, String> colCreacion = new TableColumn<>("Creación");
        colCreacion.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getFechaCreacion() + " (" + c.getValue().getUsuarioCreacion() + ")"
        ));
        colCreacion.setPrefWidth(180);

        TableColumn<PackingList, String> colEdicion = new TableColumn<>("Última Edición");
        colEdicion.setCellValueFactory(c -> {
            String fe = c.getValue().getFechaEdicion();
            String ue = c.getValue().getUsuarioEdicion();
            if (fe == null || fe.isEmpty()) return new SimpleStringProperty("-");
            return new SimpleStringProperty(fe + " (" + (ue == null ? "?" : ue) + ")");
        });
        colEdicion.setPrefWidth(180);

        TableColumn<PackingList, Number> colBultos = new TableColumn<>("Bultos");
        colBultos.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumeroBultos()));
        colBultos.setStyle("-fx-alignment: CENTER;");
        colBultos.setPrefWidth(60);

        TableColumn<PackingList, String> colPeso = new TableColumn<>("Peso Total");
        colPeso.setCellValueFactory(
                c -> new SimpleStringProperty(String.format("%.1f kg", c.getValue().getPesoTotalKg())));
        colPeso.setStyle("-fx-alignment: CENTER-RIGHT;");
        colPeso.setPrefWidth(100);

        tabla.getColumns().addAll(colPO, colCreacion, colEdicion, colBultos, colPeso);

        tabla.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tabla.getSelectionModel().getSelectedItem() != null) {
                seleccionarYSalir();
            }
        });

        root.setCenter(tabla);

        Button btnEliminar = new Button("🗑️ Eliminar");
        btnEliminar.setStyle("-fx-text-fill: red;");
        btnEliminar.setOnAction(e -> eliminarSeleccionado());

        Button btnAbrir = new Button("Abrir Packing");
        btnAbrir.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12;");
        btnAbrir.setOnAction(e -> seleccionarYSalir());

        HBox bottom = new HBox(15, btnEliminar, btnAbrir);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(10, 0, 0, 0));

        Region p = new Region();
        HBox.setHgrow(p, javafx.scene.layout.Priority.ALWAYS);
        bottom.getChildren().add(0, p); // empuja botones a los lados

        root.setBottom(bottom);

        Scene scene = new Scene(root, 950, 450);
        setScene(scene);
    }

    private void cargarDatos() {
        List<PackingList> db = storageService.listarTodos();
        listaPackings = FXCollections.observableArrayList(db);
        tabla.setItems(listaPackings);
    }

    private void seleccionarYSalir() {
        PackingList sel = tabla.getSelectionModel().getSelectedItem();
        if (sel != null) {
            this.packingSeleccionado = sel;
            close();
        }
    }

    private void eliminarSeleccionado() {
        PackingList sel = tabla.getSelectionModel().getSelectedItem();
        if (sel != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("Eliminar Packing List");
            alert.setContentText(
                    "¿Está seguro que desea eliminar el archivo del packing " + sel.getNumeroOrden() + "?");

            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    storageService.eliminar(sel);
                    listaPackings.remove(sel);
                    
                    // Si el packing eliminado es el que está abierto, avisar al controlador
                    if (plAbierto != null && plAbierto.getNombreArchivo().equals(sel.getNombreArchivo())) {
                        if (onEliminarActivo != null) {
                            onEliminarActivo.run();
                        }
                    }
                }
            });
        }
    }

    public PackingList getResultado() {
        return packingSeleccionado;
    }
}
