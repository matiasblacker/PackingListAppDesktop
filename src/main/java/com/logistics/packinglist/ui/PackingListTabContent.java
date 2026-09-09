package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.Bulto;
import com.logistics.packinglist.model.PackingItem;
import com.logistics.packinglist.model.PackingList;
import com.logistics.packinglist.service.InventarioService;
import com.logistics.packinglist.service.MantenimientoService;
import com.logistics.packinglist.service.PdfExportService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;

public class PackingListTabContent extends VBox {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    private final Window owner;
    private PackingList pl;
    private final MainController mainController;
        private final PdfExportService pdfService;

    // Header labels
    private Label lblOrden;
    private Label lblFecha;
    private Label lblCreador;
    private Label lblEditor;
    private Label lblPesoTotal;
    private Label lblPesoNeto;
    private Label lblTotalPiezas;
    private Label lblTotalBultos;
    private Label lblStatus;

    // TabPane Cuerpo
    private TabPane tabPaneCuerpo;

    public PackingListTabContent(Window owner, PackingList pl, MainController mainController) {
        this.owner = owner;
        this.pl = pl;
        this.mainController = mainController;
                this.pdfService = mainController.getPdfService();

        setSpacing(15);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: white;");

        construirHeader();
        construirCuerpo();
        construirFooter();

        refreshUI();
    }

    private void construirHeader() {
        VBox headerCard = new VBox(10);
        headerCard.setPadding(new Insets(15));
        headerCard.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 6; -fx-background-radius: 6;");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Detalles de Packing List");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        lblOrden = new Label();
        lblOrden.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Tool buttons
        HBox tools = new HBox(8);
        tools.setAlignment(Pos.CENTER_RIGHT);

        Button btnEditar = crearBoton("Editar", FontAwesomeIcon.EDIT, "#0F3E6E");
        btnEditar.setOnAction(e -> editarPacking());

        Button btnAgrupar = crearBoton("Agrupar Pallets", FontAwesomeIcon.CUBES, "#7B1FA2");
        btnAgrupar.setOnAction(e -> agruparPallets());

        Button btnModelador = crearBoton("Modelador Contenedor", FontAwesomeIcon.TH_LARGE, "#E65100");
        btnModelador.setOnAction(e -> abrirModelador());

        Button btnExportarPdf = crearBoton("PDF Completo", FontAwesomeIcon.FILE_PDF_ALT, "#D32F2F");
        btnExportarPdf.setOnAction(e -> exportarPDFCompleto());

        Button btnExportarSel = crearBoton("Exportar Selección", FontAwesomeIcon.EXTERNAL_LINK, "#388E3C");
        btnExportarSel.setOnAction(e -> exportarSeleccion());

        tools.getChildren().addAll(btnEditar, btnAgrupar, btnModelador, btnExportarSel, btnExportarPdf);
        topRow.getChildren().addAll(lblTitle, new Label("  -  "), lblOrden, spacer, tools);

        // Metadata grid
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 0, 0, 0));

        // Row 0
        grid.add(crearMetadataLabel("Fecha Creación:"), 0, 0);
        lblFecha = new Label();
        grid.add(lblFecha, 1, 0);

        grid.add(crearMetadataLabel("Creado Por:"), 2, 0);
        lblCreador = new Label();
        grid.add(lblCreador, 3, 0);

        grid.add(crearMetadataLabel("Modificado Por:"), 4, 0);
        lblEditor = new Label();
        grid.add(lblEditor, 5, 0);

        // Row 1
        grid.add(crearMetadataLabel("Peso Bruto Total:"), 0, 1);
        lblPesoTotal = new Label();
        grid.add(lblPesoTotal, 1, 1);

        grid.add(crearMetadataLabel("Peso Neto Total:"), 2, 1);
        lblPesoNeto = new Label();
        grid.add(lblPesoNeto, 3, 1);

        grid.add(crearMetadataLabel("Totales (Piezas / Bultos):"), 4, 1);
        lblTotalPiezas = new Label();
        grid.add(lblTotalPiezas, 5, 1);

        headerCard.getChildren().addAll(topRow, new Separator(), grid);
        getChildren().add(headerCard);
    }

    private void construirCuerpo() {
        tabPaneCuerpo = new TabPane();
        tabPaneCuerpo.getStyleClass().add("pallet-tab-pane");
        tabPaneCuerpo.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPaneCuerpo.setStyle("-fx-background-color: white;");
        VBox.setVgrow(tabPaneCuerpo, Priority.ALWAYS);
        getChildren().add(tabPaneCuerpo);
    }

    private void construirFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(5, 10, 5, 10));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-radius: 4;");

        lblStatus = new Label("Listo");
        lblStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");

        footer.getChildren().add(lblStatus);
        getChildren().add(footer);
    }

    private void refreshUI() {
        lblOrden.setText(pl.getNumeroOrden());
        lblFecha.setText(pl.getFecha());
        lblCreador.setText(pl.getCreadorNombre() != null ? pl.getCreadorNombre() : "N/A");
        lblEditor.setText(pl.getEditorNombre() != null ? pl.getEditorNombre() : "N/A");
        lblPesoTotal.setText(pl.getPesoTotalKg() + " kg");
        lblPesoNeto.setText(pl.getPesoNetoKg() + " kg");
        lblTotalPiezas.setText(pl.getTotalPiezas() + " piezas / " + (pl.getBultos() != null ? pl.getBultos().size() : 0) + " bultos");

        rebuildTabs();
    }

    private void rebuildTabs() {
        tabPaneCuerpo.getTabs().clear();
        if (pl.getBultos() == null) return;

        for (Bulto bulto : pl.getBultos()) {
            Tab tab = new Tab("PALLET " + bulto.getNumero());
            
            VBox contentBox = new VBox(10);
            contentBox.setPadding(new Insets(15));
            contentBox.setStyle("-fx-background-color: white;");
            
            // Alias row
            HBox aliasRow = new HBox(10);
            aliasRow.setAlignment(Pos.CENTER_LEFT);
            
            Label lblPalletTitle = new Label("PALLET " + bulto.getNumero());
            lblPalletTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");
            
            Label lblAliasLabel = new Label("Alias (opcional):");
            lblAliasLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");
            
            TextField txtAlias = new TextField(bulto.getAlias() != null ? bulto.getAlias() : "");
            txtAlias.setPromptText("Ej. compresores + molido");
            txtAlias.setPrefWidth(250);
            txtAlias.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e0; -fx-border-radius: 4; -fx-padding: 4 6; -fx-font-size: 12px;");
            txtAlias.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) { // Lost focus
                    String oldAlias = bulto.getAlias() != null ? bulto.getAlias() : "";
                    String newAlias = txtAlias.getText().trim();
                    if (!oldAlias.equals(newAlias)) {
                        bulto.setAlias(newAlias);
                        try {
                            service.actualizarPackingList(pl.getId(), pl);
                            lblStatus.setText("✔ Alias del pallet " + bulto.getNumero() + " actualizado.");
                        } catch (Exception ex) {
                            lblStatus.setText("❌ Error al guardar alias: " + ex.getMessage());
                        }
                    }
                }
            });
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button btnExportarBulto = crearBoton("Exportar PDF Bulto", FontAwesomeIcon.FILE_PDF_ALT, "#D32F2F");
            btnExportarBulto.setOnAction(e -> exportarBultoEspecifico(bulto));
            
            aliasRow.getChildren().addAll(lblPalletTitle, lblAliasLabel, txtAlias, spacer, btnExportarBulto);
            
            // Info row
            double vol = (bulto.getLargo() * bulto.getAncho() * bulto.getAlto()) / 1000000.0;
            String volStr = String.format("%.3f", vol);
            String infoText = String.format("Peso bruto: %.1f kg | Peso neto: %.1f kg | Dimensiones: %.0f x %.0f x %.0f cm | Volumen: %s m³",
                    bulto.getPesoTotal(), bulto.getPesoNeto(), bulto.getLargo(), bulto.getAncho(), bulto.getAlto(), volStr);
            Label lblInfo = new Label(infoText);
            lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-padding: 2 0;");
            
            // Table View for items in this bulto
            ObservableList<PackingItem> itemsObs = FXCollections.observableArrayList(bulto.getItems() != null ? bulto.getItems() : new java.util.ArrayList<>());
            TableView<PackingItem> tablaItemsBulto = new TableView<>(itemsObs);
            tablaItemsBulto.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            VBox.setVgrow(tablaItemsBulto, Priority.ALWAYS);
            
            TableColumn<PackingItem, String> colPart = new TableColumn<>("Part Number");
            colPart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPartNumber()));
            colPart.setPrefWidth(120);
            
            TableColumn<PackingItem, String> colDesc = new TableColumn<>("Descripción");
            colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
            colDesc.setPrefWidth(250);
            
            TableColumn<PackingItem, Integer> colCant = new TableColumn<>("Cantidad");
            colCant.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCantidad()).asObject());
            colCant.setPrefWidth(80);
            
            tablaItemsBulto.getColumns().addAll(colPart, colDesc, colCant);
            
            // Footer for total units
            int totalUnits = bulto.getItems() != null ? bulto.getItems().stream().mapToInt(PackingItem::getCantidad).sum() : 0;
            Label lblTotalUnidadesBulto = new Label("Total unidades: " + totalUnits);
            lblTotalUnidadesBulto.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F3E6E; -fx-padding: 6 12; -fx-background-color: #e6effa; -fx-background-radius: 4; -fx-font-size: 12px;");
            
            contentBox.getChildren().addAll(aliasRow, lblInfo, tablaItemsBulto, lblTotalUnidadesBulto);
            tab.setContent(contentBox);
            tabPaneCuerpo.getTabs().add(tab);
        }
        
        // Resumen Tab
        Tab tabResumen = new Tab();
        Label lblResumenHeader = new Label("Resumen");
        FontAwesomeIconView iconResumen = new FontAwesomeIconView(FontAwesomeIcon.TAG);
        iconResumen.setFill(Color.web("#0F3E6E"));
        iconResumen.setSize("12px");
        lblResumenHeader.setGraphic(iconResumen);
        tabResumen.setGraphic(lblResumenHeader);
        
        VBox resumenBox = new VBox(15);
        resumenBox.setPadding(new Insets(15));
        resumenBox.setStyle("-fx-background-color: white;");
        
        Label lblResumenTitle = new Label("Resumen General de Carga");
        lblResumenTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");
        
        TableView<Bulto> tablaResumenBultos = new TableView<>(FXCollections.observableArrayList(pl.getBultos()));
        tablaResumenBultos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tablaResumenBultos, Priority.ALWAYS);
        
        TableColumn<Bulto, Integer> colNumR = new TableColumn<>("Nº");
        colNumR.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumero()).asObject());
        colNumR.setPrefWidth(40);
        
        TableColumn<Bulto, String> colTipoR = new TableColumn<>("Tipo");
        colTipoR.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo()));
        colTipoR.setPrefWidth(90);
        
        TableColumn<Bulto, String> colAliasR = new TableColumn<>("Alias");
        colAliasR.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAlias()));
        colAliasR.setPrefWidth(150);
        
        TableColumn<Bulto, Double> colPesoBR = new TableColumn<>("Peso Bruto (kg)");
        colPesoBR.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPesoTotal()).asObject());
        colPesoBR.setPrefWidth(110);
        
        TableColumn<Bulto, Double> colPesoNR = new TableColumn<>("Peso Neto (kg)");
        colPesoNR.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPesoNeto()).asObject());
        colPesoNR.setPrefWidth(110);
        
        TableColumn<Bulto, String> colDimR = new TableColumn<>("Dimensiones");
        colDimR.setCellValueFactory(c -> {
            Bulto b = c.getValue();
            return new SimpleStringProperty(b.getLargo() + " x " + b.getAncho() + " x " + b.getAlto() + " cm");
        });
        colDimR.setPrefWidth(130);
        
        TableColumn<Bulto, String> colVolR = new TableColumn<>("Volumen (m³)");
        colVolR.setCellValueFactory(c -> {
            Bulto b = c.getValue();
            double vol = (b.getLargo() * b.getAncho() * b.getAlto()) / 1000000.0;
            return new SimpleStringProperty(String.format("%.3f m³", vol));
        });
        colVolR.setPrefWidth(110);
        
        TableColumn<Bulto, Integer> colCantR = new TableColumn<>("Cant. Items");
        colCantR.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getItems() != null ? c.getValue().getItems().size() : 0).asObject());
        colCantR.setPrefWidth(100);
        
        tablaResumenBultos.getColumns().addAll(colNumR, colTipoR, colAliasR, colPesoBR, colPesoNR, colDimR, colVolR, colCantR);
        
        resumenBox.getChildren().addAll(lblResumenTitle, tablaResumenBultos);
        tabResumen.setContent(resumenBox);
        tabPaneCuerpo.getTabs().add(tabResumen);
    }

    private Label crearMetadataLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
        return label;
    }

    private Button crearBoton(String text, FontAwesomeIcon icon, String colorHex) {
        Button btn = new Button(text);
        FontAwesomeIconView iv = new FontAwesomeIconView(icon);
        iv.setFill(Color.web(colorHex));
        iv.setSize("12px");
        btn.setGraphic(iv);
        btn.setStyle("-fx-background-color: white; -fx-border-color: " + colorHex + "; -fx-border-radius: 4; -fx-background-radius: 4; -fx-text-fill: " + colorHex + "; -fx-font-size: 12px;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + colorHex + "; -fx-border-color: " + colorHex + "; -fx-border-radius: 4; -fx-background-radius: 4; -fx-text-fill: white;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-border-color: " + colorHex + "; -fx-border-radius: 4; -fx-background-radius: 4; -fx-text-fill: " + colorHex + ";"));
        return btn;
    }

    private void editarPacking() {
        try {
            InventarioService invService = new InventarioService();
            ManualPackingDialog dialog = new ManualPackingDialog(owner, invService, pl);
            dialog.showAndWait();
            PackingList res = dialog.getResultado();
            if (res != null) {
                res.setId(pl.getId()); // preserve ID
                res.setCompanyId(pl.getCompanyId());
                lblStatus.setText("Guardando cambios en la base de datos...");
                PackingList updated = service.actualizarPackingList(pl.getId(), res);
                this.pl = updated;
                refreshUI();
                lblStatus.setText("✔ Cambios guardados con éxito.");
            }
        } catch (Exception ex) {
            alerta("Error al editar packing", ex.getMessage());
        }
    }

    private void agruparPallets() {
        try {
            new AgrupadorDialog(owner, pl, () -> {
                try {
                    lblStatus.setText("Guardando agrupación en la base de datos...");
                    PackingList updated = service.actualizarPackingList(pl.getId(), pl);
                    this.pl = updated;
                    refreshUI();
                    lblStatus.setText("✔ Pallets agrupados guardados.");
                } catch (Exception ex) {
                    alerta("Error al guardar agrupación", ex.getMessage());
                }
            }).showAndWait();
        } catch (Exception ex) {
            alerta("Error al agrupar pallets", ex.getMessage());
        }
    }

    private void abrirModelador() {
        try {
            File logo = mainController.getLogoFile();
            new ContenedorDialog(owner, pl, logo).showAndWait();
            
            lblStatus.setText("Guardando estado del contenedor en la base de datos...");
            PackingList updated = service.actualizarPackingList(pl.getId(), pl);
            this.pl = updated;
            refreshUI();
            lblStatus.setText("✔ Carga del contenedor guardada.");
        } catch (Exception ex) {
            alerta("Error al abrir modelador", ex.getMessage());
        }
    }

    private void exportarPDFCompleto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar PDF completo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        String np = pl.getNumeroOrden().replace(" ", "_");
        fc.setInitialFileName("PACKING_LIST_COMPLETO_" + np + ".pdf");
        File destino = fc.showSaveDialog(owner);
        if (destino == null) return;

        try {
            pdfService.exportar(pl, destino);
            lblStatus.setText("✔ PDF completo exportado: " + destino.getName());
        } catch (Exception ex) {
            alerta("Error al exportar PDF completo", ex.getMessage());
        }
    }

    private void exportarSeleccion() {
        try {
            new ExportarDialog(owner, pl, pdfService).showAndWait();
            lblStatus.setText("✔ Operación de exportación de selección completada.");
        } catch (Exception ex) {
            alerta("Error al exportar selección", ex.getMessage());
        }
    }

    private void exportarBultoEspecifico(Bulto bulto) {
        if (bulto == null) {
            alerta("Atención", "Debe seleccionar un bulto para exportar.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar PDF de Bulto");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        String np = pl.getNumeroOrden().replace(" ", "_");
        fc.setInitialFileName(bulto.getNombreArchivo() + "_" + np + ".pdf");
        File destino = fc.showSaveDialog(owner);
        if (destino == null) return;

        PackingList mini = new PackingList();
        mini.setNumeroOrden(pl.getNumeroOrden());
        mini.setFecha(pl.getFecha());
        mini.addBulto(bulto);
        mini.setTotalPiezas(bulto.getTotalUnidades());
        mini.setPesoTotalKg(bulto.getPesoTotal());
        mini.setPesoNetoKg(bulto.getPesoNeto());

        try {
            pdfService.exportar(mini, destino);
            lblStatus.setText("✔ PDF de bulto exportado: " + destino.getName());
        } catch (Exception ex) {
            alerta("Error al exportar PDF de bulto", ex.getMessage());
        }
    }

    private void alerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
