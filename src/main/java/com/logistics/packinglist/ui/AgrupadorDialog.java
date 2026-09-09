package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import com.logistics.packinglist.model.Bulto;
import com.logistics.packinglist.model.PackingItem;
import com.logistics.packinglist.model.PackingList;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

/**
 * Ventana modal para seleccionar pallets del Excel y fusionarlos en uno nuevo.
 *
 * Lógica de dimensiones al fusionar N pallets iguales apilados:
 * - Peso total = suma de todos los pesos
 * - Largo = largo del primer pallet (todos iguales)
 * - Ancho = ancho del primer pallet (todos iguales)
 * - Alto = suma de todos los altos (se apilan)
 */
public class AgrupadorDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    // Wrapper para mostrar bultos en la tabla con checkbox
    public static class FilaBulto {
        private final Bulto bulto;
        private final SimpleBooleanProperty seleccionado = new SimpleBooleanProperty(false);

        public FilaBulto(Bulto bulto) {
            this.bulto = bulto;
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
    private final Runnable onAgrupacionCreada;

    private TableView<FilaBulto> tabla;
    private Label lblResumen;
    private Button btnFusionar;

    public AgrupadorDialog(Window owner, PackingList pl, Runnable onAgrupacionCreada) {
        this.packingList = pl;
        this.onAgrupacionCreada = onAgrupacionCreada;

        initModality(Modality.WINDOW_MODAL);
        // initOwner(owner); // Comentado para evitar bug de X11/Mutter en Linux que achica el padre
        setTitle("Agrupar pallets / bultos");
        setMinWidth(750);
        setMinHeight(450);

        setScene(new Scene(construirContenido(), 800, 500));
    }

    // ===== UI =====

    private BorderPane construirContenido() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f7fb;");

        root.setTop(crearEncabezado());
        root.setCenter(crearTabla());
        root.setBottom(crearPanel());

        return root;
    }

    private javafx.scene.Node crearEncabezado() {
        Label titulo = new Label("Selecciona los pallets que quieres fusionar en uno solo");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        titulo.setStyle("-fx-text-fill: #0F3E6E;");

        Label sub = new Label(
                "Selecciona 2 o más pallets. El peso se suma y el alto se acumula (apilado).\nSe tomará el primer pallet seleccionado como referencia base.");
        sub.setStyle("-fx-text-fill: #567; -fx-font-size: 12px;");

        VBox enc = new VBox(4, titulo, sub);
        enc.setPadding(new Insets(16, 16, 10, 16));
        enc.setStyle("-fx-background-color: white; -fx-border-color: #dde4ee; -fx-border-width: 0 0 1 0;");
        return enc;
    }

    @SuppressWarnings("unchecked")
    private javafx.scene.Node crearTabla() {
        ObservableList<FilaBulto> filas = FXCollections.observableArrayList();
        for (Bulto b : packingList.getBultos())
            filas.add(new FilaBulto(b));

        tabla = new TableView<>(filas);
        tabla.setEditable(true);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Checkbox selección
        TableColumn<FilaBulto, Boolean> colCheck = new TableColumn<>("");
        colCheck.setCellValueFactory(c -> c.getValue().seleccionadoProperty());
        colCheck.setCellFactory(CheckBoxTableCell.forTableColumn(colCheck));
        colCheck.setEditable(true);
        colCheck.setMaxWidth(40);
        colCheck.setMinWidth(40);

        // Nombre bulto
        TableColumn<FilaBulto, String> colNombre = new TableColumn<>("Bulto");
        colNombre.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBulto().getEtiqueta()));
        colNombre.setPrefWidth(90);

        // Alias
        TableColumn<FilaBulto, String> colAlias = new TableColumn<>("Alias");
        colAlias.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBulto().getAlias()));
        colAlias.setPrefWidth(150);

        // Part numbers (resumido)
        TableColumn<FilaBulto, String> colPN = new TableColumn<>("Part Numbers");
        colPN.setCellValueFactory(c -> {
            List<String> pns = c.getValue().getBulto().getItems().stream()
                    .map(PackingItem::getPartNumber)
                    .distinct().toList();
            String texto = pns.size() <= 2
                    ? String.join(", ", pns)
                    : pns.get(0) + ", " + pns.get(1) + " (+" + (pns.size() - 2) + ")";
            return new javafx.beans.property.SimpleStringProperty(texto);
        });
        colPN.setPrefWidth(200);

        // Cantidad items
        TableColumn<FilaBulto, Number> colItems = new TableColumn<>("Unidades");
        colItems.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getBulto().getTotalUnidades()));
        colItems.setStyle("-fx-alignment: CENTER;");
        colItems.setPrefWidth(70);

        // Peso
        TableColumn<FilaBulto, String> colPeso = new TableColumn<>("Peso bruto");
        colPeso.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.1f kg", c.getValue().getBulto().getPesoTotal())));
        colPeso.setStyle("-fx-alignment: CENTER-RIGHT;");
        colPeso.setPrefWidth(90);

        // Dimensiones
        TableColumn<FilaBulto, String> colDims = new TableColumn<>("Dimensiones (L×A×H cm)");
        colDims.setCellValueFactory(c -> {
            Bulto b = c.getValue().getBulto();
            String d = b.getLargo() > 0
                    ? String.format("%.0f × %.0f × %.0f", b.getLargo(), b.getAncho(), b.getAlto())
                    : "—";
            return new javafx.beans.property.SimpleStringProperty(d);
        });
        colDims.setPrefWidth(160);

        tabla.getColumns().addAll(colCheck, colNombre, colAlias, colPN, colItems, colPeso, colDims);

        // Actualizar resumen al cambiar selección
        for (FilaBulto fb : filas) {
            fb.seleccionadoProperty().addListener((obs, old, val) -> actualizarResumen());
        }

        VBox contenedor = new VBox(tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        contenedor.setPadding(new Insets(8, 8, 0, 8));
        VBox.setVgrow(contenedor, Priority.ALWAYS);
        return contenedor;
    }

    private javafx.scene.Node crearPanel() {
        // Resumen de lo que se va a fusionar
        lblResumen = new Label("Selecciona 2 o más pallets para ver el resumen");
        lblResumen.setStyle("-fx-text-fill: #0F3E6E; -fx-font-size: 12px;");
        lblResumen.setWrapText(false);
        lblResumen.setTextOverrun(OverrunStyle.ELLIPSIS);

        // Botones
        btnFusionar = new Button("🔗  Fusionar seleccionados");
        btnFusionar.setStyle("-fx-background-color: #1a6aa8; -fx-text-fill: white; " +
                "-fx-padding: 7 16; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");
        btnFusionar.setDisable(true);
        btnFusionar.setOnAction(e -> fusionar());

        Button btnCancelar = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.TIMES));
        btnCancelar.getStyleClass().add("btn-cancelar");
        btnCancelar.setStyle("-fx-padding: 7 16; -fx-background-radius: 4; -fx-cursor: hand;");
        btnCancelar.setOnAction(e -> close());

        // Botón seleccionar todos
        Button btnTodos = new Button("Seleccionar todos");
        btnTodos.setStyle("-fx-padding: 6 12; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11px;");
        btnTodos.setOnAction(e -> tabla.getItems().forEach(f -> f.seleccionadoProperty().set(true)));

        Button btnNinguno = new Button("Deseleccionar");
        btnNinguno.setStyle("-fx-padding: 6 12; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11px;");
        btnNinguno.setOnAction(e -> tabla.getItems().forEach(f -> f.seleccionadoProperty().set(false)));

        HBox fila1 = new HBox(8, btnTodos, btnNinguno);
        fila1.setAlignment(Pos.CENTER_LEFT);

        HBox botonera = new HBox(12, btnCancelar, btnFusionar);
        botonera.setAlignment(Pos.CENTER_RIGHT);
        botonera.setMinWidth(Region.USE_PREF_SIZE);

        BorderPane fila2 = new BorderPane();
        fila2.setLeft(lblResumen);
        fila2.setRight(botonera);
        BorderPane.setAlignment(lblResumen, Pos.CENTER_LEFT);
        BorderPane.setAlignment(botonera, Pos.CENTER_RIGHT);

        VBox panel = new VBox(10, new Separator(), fila1, fila2);
        panel.setPadding(new Insets(10, 16, 16, 16));
        panel.setStyle("-fx-background-color: white;");
        return panel;
    }

    // ===== Lógica =====

    private List<FilaBulto> getSeleccionados() {
        return tabla.getItems().stream()
                .filter(FilaBulto::isSeleccionado)
                .toList();
    }

    private void actualizarResumen() {
        List<FilaBulto> sel = getSeleccionados();
        btnFusionar.setDisable(sel.size() < 2);

        if (sel.isEmpty()) {
            lblResumen.setText("Selecciona 2 o más pallets para ver el resumen");
            return;
        }
        if (sel.size() == 1) {
            lblResumen.setText("Selecciona al menos 1 más para poder fusionar");
            return;
        }

        double pesoTotal = sel.stream().mapToDouble(f -> f.getBulto().getPesoTotal()).sum();
        double pesoNeto = sel.stream().mapToDouble(f -> f.getBulto().getPesoNeto()).sum();
        int unidades = sel.stream().mapToInt(f -> f.getBulto().getTotalUnidades()).sum();

        // Dimensiones: largo y ancho del primero con dims; alto = suma de todos
        double largo = 0, ancho = 0, alto = 0;
        for (FilaBulto f : sel) {
            Bulto b = f.getBulto();
            if (b.getLargo() > 0 && largo == 0) {
                largo = b.getLargo();
                ancho = b.getAncho();
            }
            alto += b.getAlto();
        }

        String dims = largo > 0
                ? String.format("%.0f × %.0f × %.0f cm", largo, ancho, alto)
                : "sin dimensiones";

        lblResumen.setText(String.format(
                "%d pallets → Peso: %.1f kg bruto / %.1f kg neto  |  %d unidades  |  %s",
                sel.size(), pesoTotal, pesoNeto, unidades, dims));
    }

    private void fusionar() {
        List<FilaBulto> sel = getSeleccionados();
        if (sel.size() < 2)
            return;

        // Calcular totales
        double pesoTotal = sel.stream().mapToDouble(f -> f.getBulto().getPesoTotal()).sum();
        double pesoNeto = sel.stream().mapToDouble(f -> f.getBulto().getPesoNeto()).sum();
        double largo = 0, ancho = 0, alto = 0;
        for (FilaBulto f : sel) {
            Bulto b = f.getBulto();
            if (b.getLargo() > 0 && largo == 0) {
                largo = b.getLargo();
                ancho = b.getAncho();
            }
            alto += b.getAlto();
        }

        // Número del nuevo bulto = siguiente disponible
        int nuevoNum = packingList.getBultos().stream()
                .mapToInt(Bulto::getNumero).max().orElse(0) + 1;

        Bulto fusionado = new Bulto(nuevoNum, nuevoNum + " PALLET FUSIONADO",
                pesoTotal, pesoNeto, largo, ancho, alto);

        // Agregar todos los ítems de los pallets seleccionados
        for (FilaBulto f : sel) {
            for (PackingItem item : f.getBulto().getItems()) {
                fusionado.addItem(item);
            }
        }

        // Quitar los pallets originales del packing list y agregar el fusionado
        List<Bulto> originales = sel.stream().map(FilaBulto::getBulto).toList();
        packingList.getBultos().removeAll(originales);
        packingList.addBulto(fusionado);

        // Renumerar todos los bultos para que queden consecutivos
        renumerarBultos();

        // Notificar a la UI principal para que refresque las tabs
        onAgrupacionCreada.run();
        close();
    }

    /**
     * Renumera los bultos del packing list para que queden 1, 2, 3...
     * sin huecos tras una fusión.
     */
    private void renumerarBultos() {
        List<Bulto> bultos = packingList.getBultos();
        List<Bulto> nuevos = new ArrayList<>();
        for (int i = 0; i < bultos.size(); i++) {
            Bulto original = bultos.get(i);
            int nuevoNum = i + 1;
            // Extraer solo la palabra del tipo para renumerar
            String tipoPalabra = original.getEtiqueta()
                    .replaceAll("\\d+$", "").trim();
            Bulto renumerado = new Bulto(nuevoNum,
                    nuevoNum + " " + tipoPalabra,
                    original.getPesoTotal(), original.getPesoNeto(),
                    original.getLargo(), original.getAncho(), original.getAlto());
            renumerado.setAlias(original.getAlias());
            original.getItems().forEach(renumerado::addItem);
            nuevos.add(renumerado);
        }
        bultos.clear();
        bultos.addAll(nuevos);
    }
}
