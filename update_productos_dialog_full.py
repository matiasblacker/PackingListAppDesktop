import re

with open("src/main/java/com/logistics/packinglist/ui/ProductosDialog.java", "r") as f:
    content = f.read()

# Add imports if missing
if "javafx.stage.FileChooser" not in content:
    content = content.replace("import javafx.stage.Stage;", "import javafx.stage.Stage;\nimport javafx.stage.FileChooser;\nimport javafx.scene.image.Image;\nimport javafx.scene.image.ImageView;")

# Add field declarations
fields_to_add = """    private TextField txtImagenUrl;
    private Button btnSeleccionarImagen;
    private ImageView imgPreview;
    private File selectedImageFile = null;

    private TextField txtPeso;
    private TextField txtLargo;
    private TextField txtAncho;
    private TextField txtAlto;
    private TextField txtVolumen;"""

content = content.replace("private TextField txtPaisOrigen;", "private TextField txtPaisOrigen;\n" + fields_to_add)

# Initialize new text fields in construirUI()
init_controls = """        txtPaisOrigen = new TextField();
        txtPaisOrigen.setPromptText("Ej: CL");
        txtPaisOrigen.setDisable(true);

        txtImagenUrl = new TextField();
        txtImagenUrl.setPromptText("URL de la Imagen (Firebase)");
        txtImagenUrl.setDisable(true);

        imgPreview = new ImageView();
        imgPreview.setFitWidth(40);
        imgPreview.setFitHeight(40);
        imgPreview.setPreserveRatio(true);

        txtPeso = new TextField();
        txtPeso.setPromptText("0.00");
        txtPeso.setText("0.0");
        txtPeso.setDisable(true);

        txtLargo = new TextField();
        txtLargo.setPromptText("Largo (cm)");
        txtLargo.setText("0.0");
        txtLargo.setDisable(true);

        txtAncho = new TextField();
        txtAncho.setPromptText("Ancho (cm)");
        txtAncho.setText("0.0");
        txtAncho.setDisable(true);

        txtAlto = new TextField();
        txtAlto.setPromptText("Alto (cm)");
        txtAlto.setText("0.0");
        txtAlto.setDisable(true);

        txtVolumen = new TextField();
        txtVolumen.setPromptText("Volumen (cm³)");
        txtVolumen.setText("0.0");
        txtVolumen.setEditable(false);
        txtVolumen.setDisable(true);

        javafx.beans.value.ChangeListener<String> calcVol = (obs, oldV, newV) -> {
            try {
                double l = Double.parseDouble(txtLargo.getText().trim());
                double a = Double.parseDouble(txtAncho.getText().trim());
                double h = Double.parseDouble(txtAlto.getText().trim());
                double vol = l * a * h;
                txtVolumen.setText(String.format(java.util.Locale.US, "%.2f", vol));
            } catch (Exception ignored) {
                txtVolumen.setText("0.00");
            }
        };
        txtLargo.textProperty().addListener(calcVol);
        txtAncho.textProperty().addListener(calcVol);
        txtAlto.textProperty().addListener(calcVol);"""

content = content.replace("txtPaisOrigen = new TextField();\n        txtPaisOrigen.setPromptText(\"Ej: CL\");\n        txtPaisOrigen.setDisable(true);", init_controls)

# Update action buttons in construirUI()
old_btn_code = """        // Botones de acción
        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSave.setFill(Color.WHITE);
        btnAdd = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.SAVE));
        btnAdd.getStyleClass().add("btn-guardar");
        btnAdd.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnAdd.setOnAction(e -> guardar());

        FontAwesomeIconView iconDel = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDel.setFill(Color.WHITE);
        btnDelete = new Button("Eliminar", iconDel);
        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnDelete.setOnAction(e -> eliminar());

        FontAwesomeIconView iconClear = new FontAwesomeIconView(FontAwesomeIcon.ERASER);
        iconClear.setFill(Color.WHITE);
        btnClear = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.UNDO));
        btnClear.getStyleClass().add("btn-limpiar");
        btnClear.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnClear.setOnAction(e -> limpiarFormulario());

        HBox btnRow = new HBox(10, btnAdd, btnDelete, btnClear);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(10, 0, 0, 0));"""

new_btn_code = """        // Botones de acción de pie de página
        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSave.setFill(Color.WHITE);
        btnAdd = new Button("", iconSave);
        btnAdd.setStyle("-fx-background-color: cornflowerblue; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnAdd.setOnAction(e -> guardar());
        Tooltip.install(btnAdd, new Tooltip("Guardar Producto"));

        FontAwesomeIconView iconClear = new FontAwesomeIconView(FontAwesomeIcon.UNDO);
        iconClear.setFill(Color.WHITE);
        btnClear = new Button("", iconClear);
        btnClear.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnClear.setOnAction(e -> limpiarFormulario());
        Tooltip.install(btnClear, new Tooltip("Limpiar Formulario"));

        FontAwesomeIconView iconDel = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDel.setFill(Color.WHITE);
        btnDelete = new Button("", iconDel);
        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnDelete.setOnAction(e -> eliminar());
        Tooltip.install(btnDelete, new Tooltip("Eliminar Producto"));

        HBox actionRow = new HBox(8, btnAdd, btnClear, btnDelete);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(15, 0, 0, 0));"""

content = content.replace(old_btn_code, new_btn_code)

# Remove btnRow from rightPane and add actionRow to root
content = content.replace("rightPane.getChildren().addAll(lblFormTitle, grid, btnRow);", "rightPane.getChildren().addAll(lblFormTitle, grid);")
content = content.replace("root.getChildren().addAll(headerBox, mainSplit);", "root.getChildren().addAll(headerBox, mainSplit, actionRow);")

# Update abrirConfiguracionCampos() targetEntity = "PRODUCTO"
content = content.replace(
    'ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, warehouseId, supplierId, this::actualizarCamposDinamicos);',
    'ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, "PRODUCTO", warehouseId, supplierId, this::actualizarCamposDinamicos);'
)

# Update obtenerDefinicionesCampos in actualizarCamposDinamicos() to pass "PRODUCTO"
content = content.replace(
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(finalCompanyId, warehouseId, supplierId);',
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(finalCompanyId, "PRODUCTO", warehouseId, null);'
)

# Update form rendering in actualizarCamposDinamicos()
old_render_static = """                    Label lblPais = crearLabel("País Origen *");
                    txtPaisOrigen.setDisable(false);
                    txtPaisOrigen.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
                    txtPaisOrigen.setMaxWidth(Double.MAX_VALUE);
                    grid.add(lblPais, 2, currentRow);
                    grid.add(txtPaisOrigen, 3, currentRow);

                    currentRow++; // Campos personalizados empiezan en la siguiente fila"""

new_render_static = """                    Label lblPais = crearLabel("País Origen *");
                    txtPaisOrigen.setDisable(false);
                    txtPaisOrigen.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
                    txtPaisOrigen.setMaxWidth(Double.MAX_VALUE);
                    grid.add(lblPais, 2, currentRow);
                    grid.add(txtPaisOrigen, 3, currentRow);

                    currentRow++;

                    // Imagen
                    Label lblImg = crearLabel("Imagen");
                    txtImagenUrl.setDisable(false);
                    txtImagenUrl.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");

                    btnSeleccionarImagen = new Button("Subir...", new FontAwesomeIconView(FontAwesomeIcon.IMAGE));
                    btnSeleccionarImagen.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                    btnSeleccionarImagen.setOnAction(e -> seleccionarYSubirImagen());

                    HBox imgBox = new HBox(6, txtImagenUrl, btnSeleccionarImagen, imgPreview);
                    HBox.setHgrow(txtImagenUrl, Priority.ALWAYS);
                    grid.add(lblImg, 0, currentRow);
                    grid.add(imgBox, 1, currentRow);
                    GridPane.setColumnSpan(imgBox, 3);

                    currentRow++;

                    // Peso y Dimensiones
                    Label lblPeso = crearLabel("Peso (kg) *");
                    txtPeso.setDisable(false);
                    txtPeso.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
                    txtPeso.setMaxWidth(Double.MAX_VALUE);
                    grid.add(lblPeso, 0, currentRow);
                    grid.add(txtPeso, 1, currentRow);

                    Label lblDim = crearLabel("Dimensiones (L x A x A cm) *");
                    txtLargo.setDisable(false);
                    txtLargo.setPrefWidth(65);
                    txtLargo.setStyle("-fx-background-radius: 4px; -fx-border-color: #cbd5e1;");
                    txtAncho.setDisable(false);
                    txtAncho.setPrefWidth(65);
                    txtAncho.setStyle("-fx-background-radius: 4px; -fx-border-color: #cbd5e1;");
                    txtAlto.setDisable(false);
                    txtAlto.setPrefWidth(65);
                    txtAlto.setStyle("-fx-background-radius: 4px; -fx-border-color: #cbd5e1;");

                    HBox dimBox = new HBox(4, txtLargo, new Label("x"), txtAncho, new Label("x"), txtAlto);
                    dimBox.setAlignment(Pos.CENTER_LEFT);
                    grid.add(lblDim, 2, currentRow);
                    grid.add(dimBox, 3, currentRow);

                    currentRow++;

                    Label lblVol = crearLabel("Volumen (cm³) *");
                    txtVolumen.setDisable(false);
                    txtVolumen.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9; -fx-text-fill: #0f172a;");
                    txtVolumen.setMaxWidth(Double.MAX_VALUE);
                    grid.add(lblVol, 0, currentRow);
                    grid.add(txtVolumen, 1, currentRow);

                    currentRow++;"""

content = content.replace(old_render_static, new_render_static)

# Add seleccionarYSubirImagen method
upload_method_code = """    private void seleccionarYSubirImagen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar Imagen de Producto");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File f = chooser.showOpenDialog(this);
        if (f != null) {
            selectedImageFile = f;
            try {
                Image img = new Image(f.toURI().toString(), 40, 40, true, true);
                imgPreview.setImage(img);
            } catch (Exception ignored) {}

            String whId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
            String prodId = selectedProduct != null ? selectedProduct.getId() : null;
            new Thread(() -> {
                try {
                    String url = service.subirImagenProducto(whId, prodId, f);
                    javafx.application.Platform.runLater(() -> {
                        txtImagenUrl.setText(url);
                        mostrarInformacion("Éxito", "Imagen subida correctamente a Firebase Storage.");
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        mostrarError("Error al subir imagen", ex.getMessage());
                    });
                }
            }).start();
        }
    }\n"""

content = content.replace("private Label crearLabel(String text) {", upload_method_code + "\n    private Label crearLabel(String text) {")

# Update guardar() to process peso, largo, ancho, alto, volumen, imagenUrl
old_guardar_code = """        if (paisOrigenVal.isEmpty()) {
            mostrarWarning("Validación", "Debe ingresar el País de Origen.");
            return;
        }"""

new_guardar_code = """        if (paisOrigenVal.isEmpty()) {
            mostrarWarning("Validación", "Debe ingresar el País de Origen.");
            return;
        }

        String pesoStr = txtPeso.getText().trim();
        String largoStr = txtLargo.getText().trim();
        String anchoStr = txtAncho.getText().trim();
        String altoStr = txtAlto.getText().trim();
        String imgUrlVal = txtImagenUrl.getText().trim();

        double pesoVal = 0.0;
        double largoVal = 0.0;
        double anchoVal = 0.0;
        double altoVal = 0.0;
        try {
            if (!pesoStr.isEmpty()) pesoVal = Double.parseDouble(pesoStr);
            if (!largoStr.isEmpty()) largoVal = Double.parseDouble(largoStr);
            if (!anchoStr.isEmpty()) anchoVal = Double.parseDouble(anchoStr);
            if (!altoStr.isEmpty()) altoVal = Double.parseDouble(altoStr);
        } catch (Exception e) {
            mostrarWarning("Validación", "Los valores numéricos de peso y dimensiones deben ser válidos.");
            return;
        }
        double volumenVal = largoVal * anchoVal * altoVal;

        final double finalPeso = pesoVal;
        final double finalLargo = largoVal;
        final double finalAncho = anchoVal;
        final double finalAlto = altoVal;
        final double finalVolumen = volumenVal;
        final String finalImgUrl = imgUrlVal;"""

content = content.replace(old_guardar_code, new_guardar_code)

old_set_models = """                    model.setSupplierId(prov != null ? prov.getId() : null);
                    model.setCategoria(finalCategoria);
                    model.setAtributosPersonalizados(atributosValores);"""

new_set_models = """                    model.setSupplierId(prov != null ? prov.getId() : null);
                    model.setCategoria(finalCategoria);
                    model.setImagenUrl(finalImgUrl);
                    model.setPeso(finalPeso);
                    model.setLargo(finalLargo);
                    model.setAncho(finalAncho);
                    model.setAlto(finalAlto);
                    model.setVolumen(finalVolumen);
                    model.setAtributosPersonalizados(atributosValores);"""

content = content.replace(old_set_models, new_set_models)

# Update selection listener to populate new fields
old_sel_listener = """                if (newSel.getSupplierId() != null) {"""
new_sel_listener = """                txtImagenUrl.setText(newSel.getImagenUrl() != null ? newSel.getImagenUrl() : "");
                txtPeso.setText(String.valueOf(newSel.getPeso() != null ? newSel.getPeso() : 0.0));
                txtLargo.setText(String.valueOf(newSel.getLargo() != null ? newSel.getLargo() : 0.0));
                txtAncho.setText(String.valueOf(newSel.getAncho() != null ? newSel.getAncho() : 0.0));
                txtAlto.setText(String.valueOf(newSel.getAlto() != null ? newSel.getAlto() : 0.0));
                txtVolumen.setText(String.valueOf(newSel.getVolumen() != null ? newSel.getVolumen() : 0.0));
                if (newSel.getImagenUrl() != null && !newSel.getImagenUrl().isEmpty()) {
                    try {
                        imgPreview.setImage(new Image(newSel.getImagenUrl(), 40, 40, true, true));
                    } catch (Exception ignored) {}
                } else {
                    imgPreview.setImage(null);
                }

                if (newSel.getSupplierId() != null) {"""

content = content.replace(old_sel_listener, new_sel_listener)

# Update limpiarFormulario to clear new fields
old_limpiar = """            txtPaisOrigen.clear();
            txtPaisOrigen.setDisable(true);"""

new_limpiar = """            txtPaisOrigen.clear();
            txtPaisOrigen.setDisable(true);
            txtImagenUrl.clear();
            txtImagenUrl.setDisable(true);
            txtPeso.setText("0.0");
            txtPeso.setDisable(true);
            txtLargo.setText("0.0");
            txtLargo.setDisable(true);
            txtAncho.setText("0.0");
            txtAncho.setDisable(true);
            txtAlto.setText("0.0");
            txtAlto.setDisable(true);
            txtVolumen.setText("0.0");
            txtVolumen.setDisable(true);
            imgPreview.setImage(null);"""

content = content.replace(old_limpiar, new_limpiar)

with open("src/main/java/com/logistics/packinglist/ui/ProductosDialog.java", "w") as f:
    f.write(content)

print("Updated ProductosDialog.java completely successfully!")
