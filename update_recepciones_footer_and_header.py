import re

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

# 1. Update title and header in rightPane
old_header_block = """        Label lblNuevo = new Label("Registrar Recepción Física");
        lblNuevo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");"""

new_header_block = """        Label lblNuevo = new Label("Registrar Recepción Física");
        lblNuevo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Region spacerForm = new Region();
        HBox.setHgrow(spacerForm, Priority.ALWAYS);

        FontAwesomeIconView iconCog = new FontAwesomeIconView(FontAwesomeIcon.COG);
        iconCog.setFill(Color.web("#0F3E6E"));
        Button btnConfigAtributos = new Button("Configurar Atributos", iconCog);
        btnConfigAtributos.setStyle("-fx-background-color: transparent; -fx-text-fill: #0F3E6E; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        btnConfigAtributos.setOnAction(e -> abrirConfiguracionAtributos());

        HBox formHeaderBox = new HBox(8, lblNuevo, spacerForm, btnConfigAtributos);
        formHeaderBox.setAlignment(Pos.CENTER_LEFT);

        customFieldsPane = new HBox(8);
        customFieldsPane.setAlignment(Pos.CENTER_LEFT);
        customFieldsPane.setPadding(new Insets(4, 0, 4, 0));"""

content = content.replace(old_header_block, new_header_block)

# 2. Update Observaciones height to 90px
content = content.replace("txtObservaciones.setPrefHeight(72);", "txtObservaciones.setPrefHeight(90);")

# 3. Clean up itemsBox header (keep Configurar Atributos ONLY at top form header)
old_items_box_header = """        Label lblItems = new Label("Detalle de Carga");
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

new_items_box_header = """        Label lblItems = new Label("Detalle de Carga");
        lblItems.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");"""

content = content.replace(old_items_box_header, new_items_box_header)

old_items_children = """itemsBox.getChildren().addAll(itemsHeaderBox, customFieldsPane, tblItems, itemModifyGrid);"""
new_items_children = """itemsBox.getChildren().addAll(lblItems, tblItems, itemModifyGrid);"""
content = content.replace(old_items_children, new_items_children)

# 4. Update rightPane assembly to include formHeaderBox and customFieldsPane above itemsBox
old_right_assembly = """rightPane.getChildren().addAll(lblNuevo, formGrid, itemsBox, actionButtons);"""
new_right_assembly = """rightPane.getChildren().addAll(formHeaderBox, formGrid, customFieldsPane, itemsBox);"""
content = content.replace(old_right_assembly, new_right_assembly)

# 5. Fix bottom footer: Cornflowerblue button, Limpiar button, PDF button fixed at page footer!
old_footer_split = """        // Botones de acción inferior matching Anuncios
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));

        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.TRUCK);
        iconSave.setFill(Color.WHITE);
        iconSave.setSize("16");
        btnGuardarRecepcion = new Button(null, iconSave);
        btnGuardarRecepcion.setStyle("-fx-background-color: #0F3E6E; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnGuardarRecepcion.setOnAction(e -> procesarRecepcion());
        Tooltip.install(btnGuardarRecepcion, new Tooltip("Procesar Recepción Física"));

        FontAwesomeIconView iconRefresh = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        iconRefresh.setFill(Color.WHITE);
        iconRefresh.setSize("16");
        btnLimpiarRecepcion = new Button(null, iconRefresh);
        btnLimpiarRecepcion.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnLimpiarRecepcion.setOnAction(e -> limpiarFormulario());
        Tooltip.install(btnLimpiarRecepcion, new Tooltip("Limpiar Formulario"));

        actionButtons.getChildren().addAll(btnGuardarRecepcion, btnLimpiarRecepcion);

        rightPane.getChildren().addAll(formHeaderBox, formGrid, customFieldsPane, itemsBox);

        ScrollPane rightScroll = new ScrollPane(rightPane);
        rightScroll.setFitToWidth(true);
        rightScroll.setFitToHeight(true);
        rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(leftPane, rightScroll);
        mainSplit.setDividerPositions(0.5);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);"""

new_footer_split = """        ScrollPane rightScroll = new ScrollPane(rightPane);
        rightScroll.setFitToWidth(true);
        rightScroll.setFitToHeight(true);
        rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(rightScroll, Priority.ALWAYS);

        // Botones de acción inferior FIJOS en el pie de página
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setPadding(new Insets(8, 14, 8, 14));
        actionButtons.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        FontAwesomeIconView iconPdf = new FontAwesomeIconView(FontAwesomeIcon.FILE_PDF_ALT);
        iconPdf.setFill(Color.WHITE);
        iconPdf.setSize("16");
        Button btnPdf = new Button(null, iconPdf);
        btnPdf.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnPdf.setOnAction(e -> descargarPDF());
        Tooltip.install(btnPdf, new Tooltip("Exportar Documento a PDF"));

        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.TRUCK);
        iconSave.setFill(Color.WHITE);
        iconSave.setSize("16");
        btnGuardarRecepcion = new Button(null, iconSave);
        btnGuardarRecepcion.setStyle("-fx-background-color: cornflowerblue; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnGuardarRecepcion.setOnAction(e -> procesarRecepcion());
        Tooltip.install(btnGuardarRecepcion, new Tooltip("Procesar Recepción Física"));

        FontAwesomeIconView iconRefresh = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        iconRefresh.setFill(Color.WHITE);
        iconRefresh.setSize("16");
        btnLimpiarRecepcion = new Button(null, iconRefresh);
        btnLimpiarRecepcion.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnLimpiarRecepcion.setOnAction(e -> limpiarFormulario());
        Tooltip.install(btnLimpiarRecepcion, new Tooltip("Limpiar Formulario"));

        actionButtons.getChildren().addAll(btnPdf, btnGuardarRecepcion, btnLimpiarRecepcion);

        VBox rightSideContainer = new VBox(8, rightScroll, actionButtons);
        VBox.setVgrow(rightScroll, Priority.ALWAYS);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(leftPane, rightSideContainer);
        mainSplit.setDividerPositions(0.45);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);"""

content = content.replace(old_footer_split, new_footer_split)

# Add descargarPDF() method to RecepcionesDialog
descargar_pdf_code = """
    private void descargarPDF() {
        ReceptionAnnouncementModel announcement = cbAnuncio.getValue();
        if (announcement == null) {
            mostrarWarning("Exportar PDF", "Debe seleccionar un Anuncio de Carga o cargar una recepción para exportar el documento.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Guardar PDF de Recepción de Carga");
        String folioName = (announcement.getFolio() != null ? "REC-" + announcement.getFolio() : "RECEPCION");
        fileChooser.setInitialFileName(folioName + ".pdf");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));

        File file = fileChooser.showSaveDialog(this);
        if (file != null) {
            new Thread(() -> {
                try {
                    PdfAnuncioService pdfService = new PdfAnuncioService();
                    SupplierModel supplier = supplierMap.get(announcement.getSupplierId());
                    WarehouseModel warehouse = warehouseMap.get(announcement.getWarehouseId());
                    
                    CompanyModel compObj = null;
                    String companyId = announcement.getCompanyId() != null ? announcement.getCompanyId() : (warehouse != null ? warehouse.getCompanyId() : null);
                    if (companyId != null) {
                        try {
                            compObj = service.obtenerEmpresaPorId(companyId);
                        } catch (Exception ignored) {}
                    }

                    List<com.logistics.packinglist.model.ProductFieldDefinitionModel> fieldDefs = activeFieldDefinitions;
                    pdfService.generarPdfAnuncio(announcement, supplier, warehouse, compObj, productMap, fieldDefs, file);

                    javafx.application.Platform.runLater(() -> mostrarInformacion("Éxito", "Documento PDF generado correctamente en: " + file.getAbsolutePath()));
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> mostrarError("Error PDF", "No se pudo generar el documento PDF: " + e.getMessage()));
                }
            }).start();
        }
    }
"""

last_brace = content.rfind("}")
content = content[:last_brace] + descargar_pdf_code + "\n}\n"

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content)

print("Updated RecepcionesDialog.java with header config attributes, cornflowerblue button, fixed footer and PDF button!")
