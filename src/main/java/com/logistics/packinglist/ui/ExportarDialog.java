package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import com.itextpdf.text.DocumentException;
import com.logistics.packinglist.model.Bulto;
import com.logistics.packinglist.model.PackingItem;
import com.logistics.packinglist.model.PackingList;
import com.logistics.packinglist.service.PdfExportService;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Ventana modal para seleccionar pallets y exportarlos a PDF.
 */
public class ExportarDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    public static class FilaBulto {
        private final Bulto bulto;
        private final SimpleBooleanProperty seleccionado = new SimpleBooleanProperty(true);

        public FilaBulto(Bulto b) {
            this.bulto = b;
        }

        public Bulto getBulto() {
            return bulto;
        }

        public boolean isSeleccionado() {
            return seleccionado.get();
        }

        public SimpleBooleanProperty seleccionadoProperty() {
            return seleccionado;
        }
    }

    private final PackingList packingList;
    private final PdfExportService pdfService;

    private TableView<FilaBulto> tabla;
    private Label lblResumen;
    private Button btnExportarPDF;

    public ExportarDialog(Window owner, PackingList pl, PdfExportService pdfService) {
        this.packingList = pl;
        this.pdfService = pdfService;

        initModality(Modality.WINDOW_MODAL);
        // initOwner(owner); // Comentado para evitar bug de X11/Mutter en Linux que achica el padre
        setTitle("Seleccionar pallets para exportar");
        setMinWidth(680);
        setMinHeight(480);
        setScene(new Scene(construir(), 720, 520));
    }

    // ===== UI =====

    private BorderPane construir() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f7fb;");
        root.setTop(crearEncabezado());
        root.setCenter(crearTabla());
        root.setBottom(crearPanel());
        return root;
    }

    private Node crearEncabezado() {
        Label titulo = new Label("Selecciona los pallets que quieres exportar");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        titulo.setStyle("-fx-text-fill: #0F3E6E;");

        Label sub = new Label("Todos seleccionados por defecto. Desmarca los que no necesitas.");
        sub.setStyle("-fx-text-fill: #567; -fx-font-size: 12px;");

        VBox enc = new VBox(4, titulo, sub);
        enc.setPadding(new Insets(16, 16, 10, 16));
        enc.setStyle("-fx-background-color: white; -fx-border-color: #dde4ee; -fx-border-width: 0 0 1 0;");
        return enc;
    }

    @SuppressWarnings("unchecked")
    private Node crearTabla() {
        var filas = FXCollections.observableArrayList(
                packingList.getBultos().stream().map(FilaBulto::new).toList());

        tabla = new TableView<>(filas);
        tabla.setEditable(true);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Checkbox
        TableColumn<FilaBulto, Boolean> colCheck = new TableColumn<>("");
        colCheck.setCellValueFactory(c -> c.getValue().seleccionadoProperty());
        colCheck.setCellFactory(CheckBoxTableCell.forTableColumn(colCheck));
        colCheck.setEditable(true);
        colCheck.setMaxWidth(44);
        colCheck.setMinWidth(44);

        // Nombre bulto
        TableColumn<FilaBulto, String> colNombre = new TableColumn<>("Bulto");
        colNombre.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBulto().getEtiqueta()));
        colNombre.setPrefWidth(110);

        // Part numbers resumidos
        TableColumn<FilaBulto, String> colPN = new TableColumn<>("Part Numbers");
        colPN.setCellValueFactory(c -> {
            List<String> pns = c.getValue().getBulto().getItems().stream()
                    .map(PackingItem::getPartNumber).distinct().toList();
            String txt = pns.size() <= 2
                    ? String.join(", ", pns)
                    : pns.get(0) + ", " + pns.get(1) + " (+" + (pns.size() - 2) + ")";
            return new javafx.beans.property.SimpleStringProperty(txt);
        });
        colPN.setPrefWidth(210);

        // Unidades
        TableColumn<FilaBulto, Number> colUnd = new TableColumn<>("Unidades");
        colUnd.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getBulto().getTotalUnidades()));
        colUnd.setStyle("-fx-alignment: CENTER;");
        colUnd.setPrefWidth(75);

        // Peso
        TableColumn<FilaBulto, String> colPeso = new TableColumn<>("Peso bruto");
        colPeso.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.1f kg", c.getValue().getBulto().getPesoTotal())));
        colPeso.setStyle("-fx-alignment: CENTER-RIGHT;");
        colPeso.setPrefWidth(90);

        // Dimensiones
        TableColumn<FilaBulto, String> colDims = new TableColumn<>("Dimensiones (L×A×H)");
        colDims.setCellValueFactory(c -> {
            Bulto b = c.getValue().getBulto();
            String d = b.getLargo() > 0
                    ? String.format("%.0f×%.0f×%.0f cm", b.getLargo(), b.getAncho(), b.getAlto())
                    : "—";
            return new javafx.beans.property.SimpleStringProperty(d);
        });
        colDims.setPrefWidth(145);

        tabla.getColumns().addAll(colCheck, colNombre, colPN, colUnd, colPeso, colDims);

        // Actualizar resumen al cambiar checkboxes
        filas.forEach(f -> f.seleccionadoProperty().addListener((obs, old, val) -> actualizarResumen()));

        VBox cont = new VBox(tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        cont.setPadding(new Insets(8, 8, 0, 8));
        VBox.setVgrow(cont, Priority.ALWAYS);
        return cont;
    }

    private Node crearPanel() {
        lblResumen = new Label();
        lblResumen.setStyle("-fx-text-fill: #0F3E6E; -fx-font-size: 12px;");
        actualizarResumen();

        // Seleccionar / deseleccionar todos
        Button btnTodos = new Button("☑  Todos");
        btnTodos.setStyle(estilo(false));
        btnTodos.setOnAction(e -> tabla.getItems().forEach(f -> f.seleccionadoProperty().set(true)));

        Button btnNinguno = new Button("☐  Ninguno");
        btnNinguno.setStyle(estilo(false));
        btnNinguno.setOnAction(e -> tabla.getItems().forEach(f -> f.seleccionadoProperty().set(false)));

        btnExportarPDF = new Button("📄  Exportar PDF seleccionados");
        btnExportarPDF.setStyle(estilo(true));
        btnExportarPDF.setOnAction(e -> exportarPDF());

        Button btnCancelar = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.TIMES));
        btnCancelar.getStyleClass().add("btn-cancelar");
        btnCancelar.setStyle(estilo(false));
        btnCancelar.setOnAction(e -> close());

        HBox filaOpciones = new HBox(8, btnTodos, btnNinguno);
        filaOpciones.setAlignment(Pos.CENTER_LEFT);

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        HBox filaBotones = new HBox(8, lblResumen, esp, btnCancelar, btnExportarPDF);
        filaBotones.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(10, new Separator(), filaOpciones, filaBotones);
        panel.setPadding(new Insets(10, 16, 16, 16));
        panel.setStyle("-fx-background-color: white;");
        return panel;
    }

    // ===== Lógica =====

    private List<FilaBulto> getSeleccionados() {
        return tabla.getItems().stream().filter(FilaBulto::isSeleccionado).toList();
    }

    private void actualizarResumen() {
        List<FilaBulto> sel = getSeleccionados();
        boolean haySeleccion = !sel.isEmpty();
        if (btnExportarPDF != null)
            btnExportarPDF.setDisable(!haySeleccion);

        if (sel.isEmpty()) {
            lblResumen.setText("Ningún pallet seleccionado");
            return;
        }
        double pesoTotal = sel.stream().mapToDouble(f -> f.getBulto().getPesoTotal()).sum();
        int unidades = sel.stream().mapToInt(f -> f.getBulto().getTotalUnidades()).sum();
        lblResumen.setText(String.format(
                "%d pallet(s) seleccionado(s)  |  %d unidades  |  %.1f kg",
                sel.size(), unidades, pesoTotal));
    }

    /** Construye un PackingList temporal solo con los pallets seleccionados */
    private PackingList construirMini() {
        List<FilaBulto> sel = getSeleccionados();
        PackingList mini = new PackingList();
        mini.setNumeroOrden(packingList.getNumeroOrden());
        mini.setFecha(packingList.getFecha());
        sel.forEach(f -> mini.addBulto(f.getBulto()));
        mini.setTotalPiezas(sel.stream().mapToInt(f -> f.getBulto().getTotalUnidades()).sum());
        mini.setPesoTotalKg(sel.stream().mapToDouble(f -> f.getBulto().getPesoTotal()).sum());
        mini.setPesoNetoKg(sel.stream().mapToDouble(f -> f.getBulto().getPesoNeto()).sum());
        return mini;
    }

    private void exportarPDF() {
        if (getSeleccionados().isEmpty())
            return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        // Sugerir nombre con los pallets seleccionados
        List<FilaBulto> sel = getSeleccionados();
        String np = packingList.getNumeroOrden().replace(" ", "_");
        String nombre = sel.size() == 1
                ? sel.get(0).getBulto().getNombreArchivo() + "_" + np + ".pdf"
                : "PALLETS_SELECCIONADOS_" + np + ".pdf";
        fc.setInitialFileName(nombre);

        File destino = fc.showSaveDialog(getScene().getWindow());
        if (destino == null)
            return;

        try {
            pdfService.exportar(construirMini(), destino);
            close();
        } catch (IOException | DocumentException ex) {
            mostrarError("Error al exportar PDF", ex.getMessage());
        }
    }

    // ===== Utilidades =====

    private String estilo(boolean primario) {
        return primario
                ? "-fx-background-color: #1a6aa8; -fx-text-fill: white; -fx-padding: 7 14; -fx-background-radius: 4; -fx-cursor: hand;"
                : "-fx-padding: 7 14; -fx-background-radius: 4; -fx-cursor: hand;";
    }

    private void mostrarError(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
