package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.ProductFieldDefinitionModel;
import com.logistics.packinglist.model.ProductModel;
import com.logistics.packinglist.model.SupplierModel;
import com.logistics.packinglist.model.WarehouseModel;
import com.logistics.packinglist.model.LocationModel;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CargarExcelDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

        private final String companyId;
    private final WarehouseModel warehouse;
    private final SupplierModel supplier;
    private final List<ProductFieldDefinitionModel> activeFieldDefinitions;
    private final Runnable onSuccess;

    private TableView<ColumnSpec> tablaColumnas;
    private ObservableList<ColumnSpec> listSpecs;

    public CargarExcelDialog(Window owner, String companyId, WarehouseModel warehouse, SupplierModel supplier,
                              List<ProductFieldDefinitionModel> activeFieldDefinitions, Runnable onSuccess) {
                this.companyId = companyId;
        this.warehouse = warehouse;
        this.supplier = supplier;
        this.activeFieldDefinitions = activeFieldDefinitions;
        this.onSuccess = onSuccess;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Importación Masiva de Productos");

        setMinWidth(750);
        setMinHeight(500);

        construirUI();
        cargarEspecificaciones();
    }

    private void construirUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Cabecera
        Label lblTitulo = new Label("Carga Masiva de Productos (Excel)");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");
        
        Label lblInstruccion = new Label(
                "Descargue la plantilla, rellene los registros y cargue el archivo Excel. " +
                "El archivo debe seguir estrictamente el orden de columnas mostrado abajo."
        );
        lblInstruccion.setWrapText(true);
        lblInstruccion.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        VBox header = new VBox(5, lblTitulo, lblInstruccion);

        // Tabla de especificación de columnas
        tablaColumnas = new TableView<>();
        tablaColumnas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ColumnSpec, String> colLetter = new TableColumn<>("Columna");
        colLetter.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getColumnLetter()));
        colLetter.setPrefWidth(80);

        TableColumn<ColumnSpec, String> colName = new TableColumn<>("Nombre de Campo");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getColumnName()));
        colName.setPrefWidth(220);

        TableColumn<ColumnSpec, String> colType = new TableColumn<>("Tipo de Dato");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataType()));
        colType.setPrefWidth(120);

        TableColumn<ColumnSpec, String> colReq = new TableColumn<>("Requerido");
        colReq.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRequired()));
        colReq.setPrefWidth(100);

        tablaColumnas.getColumns().addAll(colLetter, colName, colType, colReq);
        VBox.setVgrow(tablaColumnas, Priority.ALWAYS);

        listSpecs = FXCollections.observableArrayList();
        tablaColumnas.setItems(listSpecs);

        // Botonera de acciones (Íconos blancos, botón subir en cornflowerblue)
        FontAwesomeIconView iconDl = new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD);
        iconDl.setFill(Color.WHITE);
        Button btnDescargar = new Button("", iconDl);
        btnDescargar.setStyle("-fx-background-color: #28a745; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnDescargar.setOnAction(e -> descargarFormatoExcel());
        Tooltip.install(btnDescargar, new Tooltip("Descargar Plantilla Excel"));

        FontAwesomeIconView iconUp = new FontAwesomeIconView(FontAwesomeIcon.UPLOAD);
        iconUp.setFill(Color.WHITE);
        Button btnSubir = new Button("", iconUp);
        btnSubir.setStyle("-fx-background-color: cornflowerblue; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnSubir.setOnAction(e -> seleccionarYCargarExcel());
        Tooltip.install(btnSubir, new Tooltip("Subir y Procesar Excel"));

        FontAwesomeIconView iconCancel = new FontAwesomeIconView(FontAwesomeIcon.TIMES);
        iconCancel.setFill(Color.WHITE);
        Button btnCancelar = new Button("", iconCancel);
        btnCancelar.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnCancelar.setOnAction(e -> close());
        Tooltip.install(btnCancelar, new Tooltip("Cancelar"));

        HBox btnRow = new HBox(12, btnDescargar, btnSubir, btnCancelar);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(header, tablaColumnas, btnRow);

        Scene scene = new Scene(root, 750, 500);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception ignored) {}
        setScene(scene);
    }

    private void cargarEspecificaciones() {
        listSpecs.clear();
        // Columnas base estándar
        listSpecs.add(new ColumnSpec("A", "SKU", "Texto", "Sí"));
        listSpecs.add(new ColumnSpec("B", "Nombre", "Texto", "Sí"));
        listSpecs.add(new ColumnSpec("C", "Categoría", "Texto", "Sí"));
        listSpecs.add(new ColumnSpec("D", "Packing", "Texto", "Sí"));
        listSpecs.add(new ColumnSpec("E", "País Origen", "Texto", "Sí"));
        listSpecs.add(new ColumnSpec("F", "Precio", "Número", "Sí"));
        listSpecs.add(new ColumnSpec("G", "Moneda (CLP o USD)", "Texto", "Sí"));
        listSpecs.add(new ColumnSpec("H", "Peso (kg)", "Número", "No"));
        listSpecs.add(new ColumnSpec("I", "Largo (cm)", "Número", "No"));
        listSpecs.add(new ColumnSpec("J", "Ancho (cm)", "Número", "No"));
        listSpecs.add(new ColumnSpec("K", "Alto (cm)", "Número", "No"));
        listSpecs.add(new ColumnSpec("L", "Imagen URL", "Texto", "No"));

        // Columnas dinámicas de atributos personalizados
        for (int i = 0; i < activeFieldDefinitions.size(); i++) {
            ProductFieldDefinitionModel field = activeFieldDefinitions.get(i);
            String letter = getColumnLetter(12 + i);
            String type = switch (field.getFieldType()) {
                case "TEXT" -> "Texto";
                case "NUMBER" -> "Número";
                case "DATE" -> "Fecha";
                case "BOOLEAN" -> "Sí/No";
                default -> field.getFieldType();
            };
            listSpecs.add(new ColumnSpec(letter, field.getFieldLabel() + " (Personalizado)", type, field.isRequired() ? "Sí" : "No"));
        }
    }

    private String getColumnLetter(int colIndex) {
        StringBuilder letter = new StringBuilder();
        int temp = colIndex;
        while (temp >= 0) {
            letter.insert(0, (char) ('A' + (temp % 26)));
            temp = (temp / 26) - 1;
        }
        return letter.toString();
    }

    private void descargarFormatoExcel() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Guardar Plantilla de Importación");
        fileChooser.setInitialFileName("plantilla_productos_" + supplier.getRazonSocial().toLowerCase().replaceAll("\\s+", "_") + ".xlsx");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Archivos Excel (*.xlsx)", "*.xlsx"));
        File file = fileChooser.showSaveDialog(this);

        if (file == null) return;

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Productos");

            // Estilo cabecera
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.MEDIUM);

            Font headerFont = wb.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < listSpecs.size(); i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(listSpecs.get(i).getColumnName());
                cell.setCellStyle(headerStyle);
            }

            // Fila de ejemplo / Dummy
            Row dummyRow = sheet.createRow(1);
            dummyRow.createCell(0).setCellValue("PROD-001");
            dummyRow.createCell(1).setCellValue("Producto Ejemplo");
            dummyRow.createCell(2).setCellValue("GENERAL");
            dummyRow.createCell(3).setCellValue("Caja x10");
            dummyRow.createCell(4).setCellValue("CL");
            dummyRow.createCell(5).setCellValue(2500.0);
            dummyRow.createCell(6).setCellValue("CLP");
            dummyRow.createCell(7).setCellValue(1.5);
            dummyRow.createCell(8).setCellValue(10.0);
            dummyRow.createCell(9).setCellValue(20.0);
            dummyRow.createCell(10).setCellValue(15.0);
            dummyRow.createCell(11).setCellValue("");

            // Valores de ejemplo para atributos personalizados
            for (int i = 0; i < activeFieldDefinitions.size(); i++) {
                ProductFieldDefinitionModel field = activeFieldDefinitions.get(i);
                org.apache.poi.ss.usermodel.Cell cell = dummyRow.createCell(12 + i);
                switch (field.getFieldType()) {
                    case "NUMBER" -> cell.setCellValue(10.5);
                    case "DATE" -> cell.setCellValue("2026-07-10");
                    case "BOOLEAN" -> cell.setCellValue("true");
                    default -> cell.setCellValue("Texto de prueba");
                }
            }

            // Autoajustar columnas
            for (int i = 0; i < listSpecs.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText("Plantilla descargada correctamente.");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al Guardar", "No se pudo generar la plantilla: " + e.getMessage());
        }
    }

    private void seleccionarYCargarExcel() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Seleccionar Archivo Excel de Carga");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Archivos Excel (*.xlsx, *.xls)", "*.xlsx", "*.xls"));
        File file = fileChooser.showOpenDialog(this);

        if (file == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Carga");
        confirm.setHeaderText("Carga Masiva de Productos");
        confirm.setContentText("¿Está seguro de que desea cargar los registros desde el archivo seleccionado?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    procesarExcel(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        mostrarError("Error de Carga", "Ocurrió un error al procesar el Excel: " + ex.getMessage());
                    });
                }
            }).start();
        }
    }

    private void procesarExcel(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() <= 1) {
                throw new RuntimeException("El archivo Excel está vacío o solo contiene la cabecera.");
            }

            List<ProductModel> tempProducts = new ArrayList<>();

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
                String precioStr = getCellStr(row, 5);
                String moneda = getCellStr(row, 6);

                if (sku.isEmpty() && nombre.isEmpty()) {
                    continue;
                }

                if (sku.isEmpty() || nombre.isEmpty() || cat.isEmpty() || packing.isEmpty() || paisOrigen.isEmpty() || precioStr.isEmpty() || moneda.isEmpty()) {
                    throw new RuntimeException("Fila incompleta: SKU, Nombre, Categoría, Packing, País Origen, Precio y Moneda son obligatorios.");
                }

                double precioVal = 0;
                try {
                    precioVal = Double.parseDouble(precioStr);
                } catch (Exception e) {
                    throw new RuntimeException("El precio en la fila con SKU '" + sku + "' no es un número válido.");
                }

                String pesoStr = getCellStr(row, 7);
                String largoStr = getCellStr(row, 8);
                String anchoStr = getCellStr(row, 9);
                String altoStr = getCellStr(row, 10);
                String imgUrlStr = getCellStr(row, 11);

                double pesoVal = 0.0, largoVal = 0.0, anchoVal = 0.0, altoVal = 0.0;
                try {
                    if (!pesoStr.isEmpty()) pesoVal = Double.parseDouble(pesoStr);
                    if (!largoStr.isEmpty()) largoVal = Double.parseDouble(largoStr);
                    if (!anchoStr.isEmpty()) anchoVal = Double.parseDouble(anchoStr);
                    if (!altoStr.isEmpty()) altoVal = Double.parseDouble(altoStr);
                } catch (Exception ignored) {}
                double volVal = largoVal * anchoVal * altoVal;

                // Mapear campos personalizados
                Map<String, String> atributosValores = new HashMap<>();

                for (int i = 0; i < activeFieldDefinitions.size(); i++) {
                    ProductFieldDefinitionModel field = activeFieldDefinitions.get(i);
                    String val = getCellStr(row, 12 + i);

                    if (field.isRequired() && val.isEmpty()) {
                        throw new RuntimeException("El campo '" + field.getFieldLabel() + "' es obligatorio y se encuentra vacío en la fila con SKU: " + sku);
                    }
                    atributosValores.put(field.getFieldKey(), val);
                }

                ProductModel pm = new ProductModel();
                pm.setSku(sku);
                pm.setNombre(nombre);
                pm.setPrecio(precioVal);
                pm.setPrecioLista(precioVal);
                pm.setMoneda(moneda);
                pm.setPacking(packing);
                pm.setPaisOrigen(paisOrigen);
                pm.setUnidadMedida("UNIDADES");
                pm.setBarcode(sku);
                pm.setCategoria(cat.toUpperCase());
                pm.setPeso(pesoVal);
                pm.setLargo(largoVal);
                pm.setAncho(anchoVal);
                pm.setAlto(altoVal);
                pm.setVolumen(volVal);
                if (!imgUrlStr.isEmpty()) pm.setImagenUrl(imgUrlStr);
                pm.setAtributosPersonalizados(atributosValores);

                tempProducts.add(pm);
            }

            if (tempProducts.isEmpty()) {
                throw new RuntimeException("No se encontraron registros válidos en el archivo.");
            }

            List<ProductModel> createdProducts = new ArrayList<>();
            for (ProductModel pm : tempProducts) {
                pm.setCompanyId(companyId);
                pm.setSupplierId(supplier.getId());

                ProductModel created = service.crearProducto(pm);
                createdProducts.add(created);
            }

            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Éxito");
                alert.setHeaderText(null);
                alert.setContentText("Se cargaron " + createdProducts.size() + " productos exitosamente.");
                alert.showAndWait();
                
                if (onSuccess != null) {
                    onSuccess.run();
                }
                close();
            });
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

    private void mostrarError(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static class ColumnSpec {
        private final String columnLetter;
        private final String columnName;
        private final String dataType;
        private final String required;

        public ColumnSpec(String columnLetter, String columnName, String dataType, String required) {
            this.columnLetter = columnLetter;
            this.columnName = columnName;
            this.dataType = dataType;
            this.required = required;
        }

        public String getColumnLetter() { return columnLetter; }
        public String getColumnName() { return columnName; }
        public String getDataType() { return dataType; }
        public String getRequired() { return required; }
    }
}
