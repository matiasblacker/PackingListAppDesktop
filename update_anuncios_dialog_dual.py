import re

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content = f.read()

# Add field declarations
field_decl_target = "private HBox customFieldsPane;"
field_decl_replacement = """private HBox docCustomFieldsPane;
    private HBox customFieldsPane;
    private final List<ProductFieldDefinitionModel> activeDocFieldDefinitions = new ArrayList<>();
    private final Map<String, Control> docCustomFieldInputMap = new HashMap<>();"""
content = content.replace(field_decl_target, field_decl_replacement)

# Update top header
old_top_header = """        Label lblNuevo = new Label("Detalle del Anuncio");
        lblNuevo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");"""

new_top_header = """        Label lblNuevo = new Label("Detalle del Anuncio");
        lblNuevo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Region spacerTop = new Region();
        HBox.setHgrow(spacerTop, Priority.ALWAYS);

        FontAwesomeIconView iconCogDoc = new FontAwesomeIconView(FontAwesomeIcon.COG);
        iconCogDoc.setFill(Color.web("#0F3E6E"));
        Button btnConfigAtributosDoc = new Button("Configurar Atributos Documento", iconCogDoc);
        btnConfigAtributosDoc.setStyle("-fx-background-color: transparent; -fx-text-fill: #0F3E6E; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        btnConfigAtributosDoc.setOnAction(e -> abrirConfiguracionAtributosDoc());

        HBox topHeaderBox = new HBox(8, lblNuevo, spacerTop, btnConfigAtributosDoc);
        topHeaderBox.setAlignment(Pos.CENTER_LEFT);

        docCustomFieldsPane = new HBox(8);
        docCustomFieldsPane.setAlignment(Pos.CENTER_LEFT);
        docCustomFieldsPane.setPadding(new Insets(4, 0, 4, 0));"""

content = content.replace(old_top_header, new_top_header)

# Update items header
old_items_header = """        Label lblItems = new Label("Detalle de Carga");
        lblItems.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");"""

new_items_header = """        Label lblItems = new Label("Detalle de Carga");
        lblItems.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Region spacerItems = new Region();
        HBox.setHgrow(spacerItems, Priority.ALWAYS);

        FontAwesomeIconView iconCogProd = new FontAwesomeIconView(FontAwesomeIcon.COG);
        iconCogProd.setFill(Color.web("#0F3E6E"));
        Button btnConfigAtributosProd = new Button("Configurar Atributos Producto", iconCogProd);
        btnConfigAtributosProd.setStyle("-fx-background-color: transparent; -fx-text-fill: #0F3E6E; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        btnConfigAtributosProd.setOnAction(e -> abrirConfiguracionAtributosProd());

        HBox itemsHeaderBox = new HBox(8, lblItems, spacerItems, btnConfigAtributosProd);
        itemsHeaderBox.setAlignment(Pos.CENTER_LEFT);"""

content = content.replace(old_items_header, new_items_header)

old_right_add = """rightPane.getChildren().addAll(lblNuevo, formGrid, itemsBox);"""
new_right_add = """rightPane.getChildren().addAll(topHeaderBox, formGrid, docCustomFieldsPane, itemsBox);"""
content = content.replace(old_right_add, new_right_add)

old_cb_bod = """cbBodega.setOnAction(e -> cargarCamposPersonalizados());"""
new_cb_bod = """cbBodega.setOnAction(e -> {
            cargarCamposPersonalizadosDocumento();
            cargarCamposPersonalizadosProducto();
        });"""
content = content.replace(old_cb_bod, new_cb_bod)

old_cb_prov = """cbProveedor.valueProperty().addListener((obs, oldV, newV) -> cargarCamposPersonalizados());"""
new_cb_prov = """cbProveedor.valueProperty().addListener((obs, oldV, newV) -> {
            cargarCamposPersonalizadosDocumento();
            cargarCamposPersonalizadosProducto();
        });"""
content = content.replace(old_cb_prov, new_cb_prov)

# Update abrirConfiguracionAtributos / cargarCamposPersonalizados methods
old_methods_block = """    private void abrirConfiguracionAtributos() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        String supplierId = cbProveedor.getValue() != null ? cbProveedor.getValue().getId() : null;

        ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, "ANUNCIO", warehouseId, supplierId, this::cargarCamposPersonalizados);
        diag.showAndWait();
    }

    private void cargarCamposPersonalizados() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        String supplierId = cbProveedor.getValue() != null ? cbProveedor.getValue().getId() : null;

        new Thread(() -> {
            try {
                List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO", warehouseId, supplierId);
                List<ProductFieldDefinitionModel> valid = new ArrayList<>();
                if (fields != null) {
                    for (ProductFieldDefinitionModel f : fields) {
                        String t = f.getFieldType() != null ? f.getFieldType().toUpperCase() : "TEXT";
                        if (f.isActive() && ("TEXT".equals(t) || "NUMBER".equals(t) || "DATE".equals(t) || "BOOLEAN".equals(t)) && valid.size() < 4) {
                            valid.add(f);
                        }
                    }
                }
                javafx.application.Platform.runLater(() -> {
                    activeFieldDefinitions.clear();
                    activeFieldDefinitions.addAll(valid);
                    renderCustomFieldInputs();
                    reconstruirColumnasTabla();
                });
            } catch (Exception ignored) {}
        }).start();
    }"""

new_methods_block = """    private void abrirConfiguracionAtributosDoc() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        String supplierId = cbProveedor.getValue() != null ? cbProveedor.getValue().getId() : null;

        ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, "ANUNCIO_DOC", warehouseId, supplierId, this::cargarCamposPersonalizadosDocumento);
        diag.showAndWait();
    }

    private void abrirConfiguracionAtributosProd() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        String supplierId = cbProveedor.getValue() != null ? cbProveedor.getValue().getId() : null;

        ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, "ANUNCIO_PROD", warehouseId, supplierId, this::cargarCamposPersonalizadosProducto);
        diag.showAndWait();
    }

    private void cargarCamposPersonalizadosDocumento() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        String supplierId = cbProveedor.getValue() != null ? cbProveedor.getValue().getId() : null;

        new Thread(() -> {
            try {
                List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_DOC", warehouseId, supplierId);
                List<ProductFieldDefinitionModel> valid = new ArrayList<>();
                if (fields != null) {
                    for (ProductFieldDefinitionModel f : fields) {
                        String t = f.getFieldType() != null ? f.getFieldType().toUpperCase() : "TEXT";
                        if (f.isActive() && ("TEXT".equals(t) || "NUMBER".equals(t) || "DATE".equals(t) || "BOOLEAN".equals(t)) && valid.size() < 4) {
                            valid.add(f);
                        }
                    }
                }
                javafx.application.Platform.runLater(() -> {
                    activeDocFieldDefinitions.clear();
                    activeDocFieldDefinitions.addAll(valid);
                    renderDocCustomFieldInputs();
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void cargarCamposPersonalizadosProducto() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        String supplierId = cbProveedor.getValue() != null ? cbProveedor.getValue().getId() : null;

        new Thread(() -> {
            try {
                List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_PROD", warehouseId, supplierId);
                List<ProductFieldDefinitionModel> valid = new ArrayList<>();
                if (fields != null) {
                    for (ProductFieldDefinitionModel f : fields) {
                        String t = f.getFieldType() != null ? f.getFieldType().toUpperCase() : "TEXT";
                        if (f.isActive() && ("TEXT".equals(t) || "NUMBER".equals(t) || "DATE".equals(t) || "BOOLEAN".equals(t)) && valid.size() < 4) {
                            valid.add(f);
                        }
                    }
                }
                javafx.application.Platform.runLater(() -> {
                    activeFieldDefinitions.clear();
                    activeFieldDefinitions.addAll(valid);
                    renderCustomFieldInputs();
                    reconstruirColumnasTabla();
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void renderDocCustomFieldInputs() {
        if (docCustomFieldsPane == null) return;
        docCustomFieldsPane.getChildren().clear();
        docCustomFieldInputMap.clear();

        for (ProductFieldDefinitionModel f : activeDocFieldDefinitions) {
            VBox box = new VBox(2);
            String labelText = (f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey());
            Label lbl = new Label(labelText + (f.isRequired() ? " *" : ""));
            lbl.setStyle(f.isRequired() ? "-fx-font-size: 11px; -fx-text-fill: #dc2626; -fx-font-weight: bold;" : "-fx-font-size: 11px; -fx-text-fill: #475569;");

            String t = f.getFieldType() != null ? f.getFieldType().toUpperCase() : "TEXT";
            Control ctrl;
            if ("BOOLEAN".equals(t)) {
                ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList("Sí", "No"));
                cb.setPromptText("Seleccione");
                cb.setStyle("-fx-background-radius: 4px;");
                ctrl = cb;
            } else if ("DATE".equals(t)) {
                DatePicker dp = new DatePicker();
                dp.setPromptText("dd/MM/yyyy");
                dp.setStyle("-fx-background-radius: 4px;");
                dp.setPrefWidth(125);
                ctrl = dp;
            } else {
                TextField txt = new TextField();
                txt.setPromptText(labelText);
                txt.setStyle("-fx-background-radius: 4px; -fx-border-color: #cbd5e1;");
                txt.setPrefWidth(120);
                ctrl = txt;
            }
            docCustomFieldInputMap.put(f.getFieldKey(), ctrl);
            box.getChildren().addAll(lbl, ctrl);
            docCustomFieldsPane.getChildren().add(box);
        }
    }"""

content = content.replace(old_methods_block, new_methods_block)

# Save document custom attributes in guardarAnuncio()
old_guardar_attrs = "model.setObservaciones(obs.isEmpty() ? null : obs);"
new_guardar_attrs = """model.setObservaciones(obs.isEmpty() ? null : obs);

        Map<String, String> docAttrs = new HashMap<>();
        for (ProductFieldDefinitionModel f : activeDocFieldDefinitions) {
            Control ctrl = docCustomFieldInputMap.get(f.getFieldKey());
            String val = "";
            if (ctrl instanceof TextField) {
                val = ((TextField) ctrl).getText().trim();
            } else if (ctrl instanceof ComboBox) {
                Object obj = ((ComboBox<?>) ctrl).getValue();
                val = obj != null ? obj.toString() : "";
            } else if (ctrl instanceof DatePicker) {
                java.time.LocalDate ld = ((DatePicker) ctrl).getValue();
                val = ld != null ? ld.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            }
            if (f.isRequired() && val.isEmpty()) {
                mostrarWarning("Validación", "El campo de documento '" + (f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey()) + "' es requerido.");
                return;
            }
            if (!val.isEmpty()) {
                docAttrs.put(f.getFieldKey(), val);
            }
        }
        model.setAtributosPersonalizados(docAttrs);"""

content = content.replace(old_guardar_attrs, new_guardar_attrs)

# Load document custom attributes on selectionListener
old_select_listener = "txtObservaciones.setText(newSel.getObservaciones() != null ? newSel.getObservaciones() : \"\");"
new_select_listener = """txtObservaciones.setText(newSel.getObservaciones() != null ? newSel.getObservaciones() : "");

                Map<String, String> docAttrs = newSel.getAtributosPersonalizados() != null ? newSel.getAtributosPersonalizados() : new HashMap<>();
                for (ProductFieldDefinitionModel f : activeDocFieldDefinitions) {
                    Control ctrl = docCustomFieldInputMap.get(f.getFieldKey());
                    String val = docAttrs.getOrDefault(f.getFieldKey(), "");
                    if (ctrl instanceof TextField) {
                        ((TextField) ctrl).setText(val);
                    } else if (ctrl instanceof ComboBox) {
                        ((ComboBox<String>) ctrl).setValue(val.isEmpty() ? null : val);
                    } else if (ctrl instanceof DatePicker) {
                        if (!val.isEmpty()) {
                            try {
                                ((DatePicker) ctrl).setValue(java.time.LocalDate.parse(val, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            } catch (Exception ignored) {}
                        } else {
                            ((DatePicker) ctrl).setValue(null);
                        }
                    }
                }"""

content = content.replace(old_select_listener, new_select_listener)

# Clear document custom attributes in limpiarFormulario()
old_limpiar_form = "txtObservaciones.clear();"
new_limpiar_form = """txtObservaciones.clear();
        for (Control ctrl : docCustomFieldInputMap.values()) {
            if (ctrl instanceof TextField) ((TextField) ctrl).clear();
            else if (ctrl instanceof ComboBox) ((ComboBox<?>) ctrl).setValue(null);
            else if (ctrl instanceof DatePicker) ((DatePicker) ctrl).setValue(null);
        }"""

content = content.replace(old_limpiar_form, new_limpiar_form)

# Call custom attributes loading in init
old_init_load = "cargarDatos();"
new_init_load = """cargarCamposPersonalizadosDocumento();
        cargarCamposPersonalizadosProducto();
        cargarDatos();"""

content = content.replace(old_init_load, new_init_load)

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content)

print("Updated AnunciosDialog.java with dual custom attributes (Doc vs Prod) successfully!")
