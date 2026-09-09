import re

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

# Add field declarations
field_decl_target = "private HBox customFieldsPane;"
field_decl_replacement = """private HBox docCustomFieldsPane;
    private HBox customFieldsPane;
    private final List<ProductFieldDefinitionModel> activeDocFieldDefinitions = new ArrayList<>();
    private final Map<String, Control> docCustomFieldInputMap = new HashMap<>();"""
content = content.replace(field_decl_target, field_decl_replacement)

# Update top header button action & text
content = content.replace(
    'Button btnConfigAtributos = new Button("Configurar Atributos", iconCog);',
    'Button btnConfigAtributos = new Button("Configurar Atributos Documento", iconCog);'
)
content = content.replace(
    'btnConfigAtributos.setOnAction(e -> abrirConfiguracionAtributos());',
    'btnConfigAtributos.setOnAction(e -> abrirConfiguracionAtributosDoc());'
)

# Update items header button
old_items_header = """        Label lblItems = new Label("Detalle de Carga");
        lblItems.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");"""

new_items_header = """        Label lblItems = new Label("Detalle de Carga");
        lblItems.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

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

old_items_box_add = """itemsBox.getChildren().addAll(lblItems, tblItems, itemModifyGrid);"""
new_items_box_add = """itemsBox.getChildren().addAll(itemsHeaderBox, customFieldsPane, tblItems, itemModifyGrid);"""
content = content.replace(old_items_box_add, new_items_box_add)

old_right_pane_children = """rightPane.getChildren().addAll(formHeaderBox, formGrid, customFieldsPane, itemsBox);"""
new_right_pane_children = """rightPane.getChildren().addAll(formHeaderBox, formGrid, docCustomFieldsPane, itemsBox);"""
content = content.replace(old_right_pane_children, new_right_pane_children)

# Update abrirConfiguracionAtributos and cargarCamposPersonalizados methods
old_rec_methods = """    private void abrirConfiguracionAtributos() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        ReceptionAnnouncementModel ann = cbAnuncio.getValue();
        String supplierId = ann != null ? ann.getSupplierId() : null;

        ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, "RECEPCION", warehouseId, supplierId, this::cargarCamposPersonalizados);
        diag.showAndWait();
    }

    private void cargarCamposPersonalizados() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        ReceptionAnnouncementModel ann = cbAnuncio.getValue();
        String supplierId = ann != null ? ann.getSupplierId() : null;

        new Thread(() -> {
            try {
                List<com.logistics.packinglist.model.ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "RECEPCION", warehouseId, supplierId);
                List<com.logistics.packinglist.model.ProductFieldDefinitionModel> valid = new ArrayList<>();
                if (fields != null) {
                    for (com.logistics.packinglist.model.ProductFieldDefinitionModel f : fields) {
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

new_rec_methods = """    private void abrirConfiguracionAtributosDoc() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        ReceptionAnnouncementModel ann = cbAnuncio.getValue();
        String supplierId = ann != null ? ann.getSupplierId() : null;

        ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, "RECEPCION_DOC", warehouseId, supplierId, this::cargarCamposPersonalizadosDocumento);
        diag.showAndWait();
    }

    private void abrirConfiguracionAtributosProd() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        ReceptionAnnouncementModel ann = cbAnuncio.getValue();
        String supplierId = ann != null ? ann.getSupplierId() : null;

        ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, "RECEPCION_PROD", warehouseId, supplierId, this::cargarCamposPersonalizadosProducto);
        diag.showAndWait();
    }

    private void cargarCamposPersonalizadosDocumento() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        ReceptionAnnouncementModel ann = cbAnuncio.getValue();
        String supplierId = ann != null ? ann.getSupplierId() : null;

        new Thread(() -> {
            try {
                List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "RECEPCION_DOC", warehouseId, supplierId);
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
        ReceptionAnnouncementModel ann = cbAnuncio.getValue();
        String supplierId = ann != null ? ann.getSupplierId() : null;

        new Thread(() -> {
            try {
                List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "RECEPCION_PROD", warehouseId, supplierId);
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

content = content.replace(old_rec_methods, new_rec_methods)

# Replace legacy call cargarCamposPersonalizados();
content = content.replace("cargarCamposPersonalizados();", "cargarCamposPersonalizadosDocumento();\n        cargarCamposPersonalizadosProducto();")

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content)

print("Updated RecepcionesDialog.java with dual custom attributes (RECEPCION_DOC vs RECEPCION_PROD) successfully!")
