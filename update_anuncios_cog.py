import re

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content = f.read()

# Replace header box in itemsBox
old_lbl_items = """        Label lblItems = new Label("Agregar Productos");
        lblItems.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");"""

new_lbl_items = """        Label lblItems = new Label("Agregar Productos");
        lblItems.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        Region spacerItems = new Region();
        HBox.setHgrow(spacerItems, Priority.ALWAYS);

        FontAwesomeIconView iconCog = new FontAwesomeIconView(FontAwesomeIcon.COG);
        iconCog.setFill(Color.web("#0F3E6E"));
        Button btnConfigAtributos = new Button("Configurar Atributos", iconCog);
        btnConfigAtributos.setStyle("-fx-background-color: transparent; -fx-text-fill: #0F3E6E; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        btnConfigAtributos.setOnAction(e -> abrirConfiguracionAtributos());

        HBox itemsHeaderBox = new HBox(8, lblItems, spacerItems, btnConfigAtributos);
        itemsHeaderBox.setAlignment(Pos.CENTER_LEFT);"""

content = content.replace(old_lbl_items, new_lbl_items)

content = content.replace("itemsBox.getChildren().addAll(lblItems, inputsWrapper, tblDetails);", "itemsBox.getChildren().addAll(itemsHeaderBox, inputsWrapper, tblDetails);")

# Add abrirConfiguracionAtributos method before cargarCamposPersonalizados
abrir_method = """    private void abrirConfiguracionAtributos() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        String supplierId = cbProveedor.getValue() != null ? cbProveedor.getValue().getId() : null;

        ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, warehouseId, supplierId, this::cargarCamposPersonalizados);
        diag.showAndWait();
    }
"""

content = content.replace("private void cargarCamposPersonalizados() {", abrir_method + "\n    private void cargarCamposPersonalizados() {")

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content)

print("Added Configurar Atributos button to AnunciosDialog")
