with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

# 1. Update top header button text to "Configurar Atributos"
content = content.replace(
    'Button btnConfigAtributos = new Button("Configurar Atributos Documento", iconCog);',
    'Button btnConfigAtributos = new Button("Configurar Atributos", iconCog);'
)

# 2. Revert itemsBox header in Recepciones (NO config button for product level in Recepciones)
old_items_header = """        Label lblItems = new Label("Detalle de Carga");
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

new_items_header = """        Label lblItems = new Label("Detalle de Carga");
        lblItems.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");"""

content = content.replace(old_items_header, new_items_header)

old_items_box_children = """itemsBox.getChildren().addAll(itemsHeaderBox, customFieldsPane, tblItems, itemModifyGrid);"""
new_items_box_children = """itemsBox.getChildren().addAll(lblItems, tblItems, itemModifyGrid);"""
content = content.replace(old_items_box_children, new_items_box_children)

# 3. Remove abrirConfiguracionAtributosProd method from RecepcionesDialog
content = content.replace(
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "RECEPCION_PROD", warehouseId, supplierId);',
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_PROD", warehouseId, supplierId);'
)

# Remove abrirConfiguracionAtributosProd unused method
old_open_prod = """    private void abrirConfiguracionAtributosProd() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        ReceptionAnnouncementModel ann = cbAnuncio.getValue();
        String supplierId = ann != null ? ann.getSupplierId() : null;

        ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, "RECEPCION_PROD", warehouseId, supplierId, this::cargarCamposPersonalizadosProducto);
        diag.showAndWait();
    }\n"""

content = content.replace(old_open_prod, "")

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content)

print("Updated RecepcionesDialog.java (omitted product-level custom attribute config) successfully!")
