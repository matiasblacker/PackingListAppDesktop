# 1. Update CargarExcelDialog.java
with open("src/main/java/com/logistics/packinglist/ui/CargarExcelDialog.java", "r") as f:
    content_c = f.read()

# Update buttons in CargarExcelDialog
old_btn_row = """        // Botonera de acciones
        Button btnDescargar = new Button("Descargar Plantilla", new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD));
        btnDescargar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnDescargar.setOnAction(e -> descargarFormatoExcel());

        Button btnSubir = new Button("Subir y Procesar Excel", new FontAwesomeIconView(FontAwesomeIcon.UPLOAD));
        btnSubir.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnSubir.setOnAction(e -> seleccionarYCargarExcel());

        Button btnCancelar = new Button("Cancelar", new FontAwesomeIconView(FontAwesomeIcon.TIMES));
        btnCancelar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand;");
        btnCancelar.setOnAction(e -> close());"""

new_btn_row = """        // Botonera de acciones (Íconos blancos, botón subir en cornflowerblue)
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
        Tooltip.install(btnCancelar, new Tooltip("Cancelar"));"""

content_c = content_c.replace(old_btn_row, new_btn_row)

# Update specifications to include base columns + active fields
old_specs = """        listSpecs.add(new ColumnSpec("A", "SKU", "Texto", "Sí"));
        listSpecs.add(new ColumnSpec("B", "Nombre", "Texto", "Sí"));
        listSpecs.add(new ColumnSpec("C", "Categoría", "Texto", "Sí"));
        listSpecs.add(new ColumnSpec("D", "Packing", "Texto", "Sí"));
        listSpecs.add(new ColumnSpec("E", "País Origen", "Texto", "Sí"));
        listSpecs.add(new ColumnSpec("F", "Precio", "Número", "Sí"));
        listSpecs.add(new ColumnSpec("G", "Moneda (CLP o USD)", "Texto", "Sí"));

        // Columnas dinámicas de atributos personalizados
        for (int i = 0; i < activeFieldDefinitions.size(); i++) {
            ProductFieldDefinitionModel field = activeFieldDefinitions.get(i);
            String letter = getColumnLetter(7 + i);"""

new_specs = """        listSpecs.add(new ColumnSpec("A", "SKU", "Texto", "Sí"));
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
            String letter = getColumnLetter(12 + i);"""

content_c = content_c.replace(old_specs, new_specs)

# Update template dummy row
old_dummy = """            dummyRow.createCell(0).setCellValue("PROD-001");
            dummyRow.createCell(1).setCellValue("Producto Ejemplo");
            dummyRow.createCell(2).setCellValue("GENERAL");
            dummyRow.createCell(3).setCellValue("Caja x10");
            dummyRow.createCell(4).setCellValue("CL");
            dummyRow.createCell(5).setCellValue(2500.0);
            dummyRow.createCell(6).setCellValue("CLP");

            // Valores de ejemplo para atributos personalizados
            for (int i = 0; i < activeFieldDefinitions.size(); i++) {
                ProductFieldDefinitionModel field = activeFieldDefinitions.get(i);
                org.apache.poi.ss.usermodel.Cell cell = dummyRow.createCell(7 + i);"""

new_dummy = """            dummyRow.createCell(0).setCellValue("PROD-001");
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
                org.apache.poi.ss.usermodel.Cell cell = dummyRow.createCell(12 + i);"""

content_c = content_c.replace(old_dummy, new_dummy)

# Update Excel parser to read new columns + dynamic fields
old_parse = """                // Mapear campos personalizados
                Map<String, String> atributosValores = new HashMap<>();

                for (int i = 0; i < activeFieldDefinitions.size(); i++) {
                    ProductFieldDefinitionModel field = activeFieldDefinitions.get(i);
                    String val = getCellStr(row, 7 + i);"""

new_parse = """                String pesoStr = getCellStr(row, 7);
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
                    String val = getCellStr(row, 12 + i);"""

content_c = content_c.replace(old_parse, new_parse)

old_pm_set = """                pm.setCategoria(cat.toUpperCase());
                pm.setAtributosPersonalizados(atributosValores);"""

new_pm_set = """                pm.setCategoria(cat.toUpperCase());
                pm.setPeso(pesoVal);
                pm.setLargo(largoVal);
                pm.setAncho(anchoVal);
                pm.setAlto(altoVal);
                pm.setVolumen(volVal);
                if (!imgUrlStr.isEmpty()) pm.setImagenUrl(imgUrlStr);
                pm.setAtributosPersonalizados(atributosValores);"""

content_c = content_c.replace(old_pm_set, new_pm_set)

with open("src/main/java/com/logistics/packinglist/ui/CargarExcelDialog.java", "w") as f:
    f.write(content_c)

# 2. Update ProductosDialog.java
with open("src/main/java/com/logistics/packinglist/ui/ProductosDialog.java", "r") as f:
    content_p = f.read()

# Update btnExcel in topRow (Icon-only, WHITE icon)
old_excel_btn = """        btnExcel = new Button("Cargar Excel...", new FontAwesomeIconView(FontAwesomeIcon.FILE_EXCEL_ALT));
        btnExcel.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");"""

new_excel_btn = """        FontAwesomeIconView iconExcel = new FontAwesomeIconView(FontAwesomeIcon.FILE_EXCEL_ALT);
        iconExcel.setFill(Color.WHITE);
        btnExcel = new Button("", iconExcel);
        btnExcel.setStyle("-fx-background-color: #28a745; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        Tooltip.install(btnExcel, new Tooltip("Cargar Excel..."));"""

content_p = content_p.replace(old_excel_btn, new_excel_btn)

# Update btnConfigCampos to text-link style
old_cfg_btn = """        btnConfigCampos = new Button("Atributos Personalizados", new FontAwesomeIconView(FontAwesomeIcon.COG));
        btnConfigCampos.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand;");
        btnConfigCampos.setOnAction(e -> abrirConfiguracionCampos());
        btnConfigCampos.setDisable(true);"""

new_cfg_btn = """        FontAwesomeIconView iconCogProd = new FontAwesomeIconView(FontAwesomeIcon.COG);
        iconCogProd.setFill(Color.web("#0F3E6E"));
        btnConfigCampos = new Button("Configurar Productos", iconCogProd);
        btnConfigCampos.setStyle("-fx-background-color: transparent; -fx-text-fill: #0F3E6E; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        btnConfigCampos.setOnAction(e -> abrirConfiguracionCampos());
        btnConfigCampos.setDisable(true);"""

content_p = content_p.replace(old_cfg_btn, new_cfg_btn)

# Remove btnConfigCampos from topRow
content_p = content_p.replace(
    "HBox topRow = new HBox(10, new Label(\"Buscar:\"), txtBusqueda, spacer, btnConfigCampos, btnExcel);",
    "HBox topRow = new HBox(10, new Label(\"Buscar:\"), txtBusqueda, spacer, btnExcel);"
)

# Add formHeaderBox to rightPane header with btnConfigCampos
old_form_title = """        Label lblFormTitle = new Label("Detalle / Registro de Producto");
        lblFormTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        
        rightPane.getChildren().addAll(lblFormTitle, grid);"""

new_form_title = """        Label lblFormTitle = new Label("Detalle / Registro de Producto");
        lblFormTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Region spacerForm = new Region();
        HBox.setHgrow(spacerForm, Priority.ALWAYS);

        HBox formHeaderBox = new HBox(8, lblFormTitle, spacerForm, btnConfigCampos);
        formHeaderBox.setAlignment(Pos.CENTER_LEFT);
        
        rightPane.getChildren().addAll(formHeaderBox, grid);"""

content_p = content_p.replace(old_form_title, new_form_title)

with open("src/main/java/com/logistics/packinglist/ui/ProductosDialog.java", "w") as f:
    f.write(content_p)

print("Updated CargarExcelDialog and ProductosDialog successfully!")
