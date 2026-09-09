import re

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

# Update formGrid setup with 135px label column width and compact styles
old_form_grid = """        cbBodega = new ComboBox<>();
        cbBodega.setPromptText("Seleccione Bodega");
        cbBodega.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbBodega.setMaxWidth(Double.MAX_VALUE);
        cbBodega.setOnAction(e -> cargarAnunciosYLocationsDeBodega());

        cbAnuncio = new ComboBox<>();
        cbAnuncio.setPromptText("Seleccione Anuncio de Carga");
        cbAnuncio.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbAnuncio.setMaxWidth(Double.MAX_VALUE);
        cbAnuncio.setOnAction(e -> cargarItemsDeAnuncio());

        txtNumeroBl = new TextField();
        txtNumeroBl.setPromptText("N° BL (Auto)");
        txtNumeroBl.setEditable(false);
        txtNumeroBl.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9;");

        txtOrdenCompra = new TextField();
        txtOrdenCompra.setPromptText("Orden de Compra (Auto)");
        txtOrdenCompra.setEditable(false);
        txtOrdenCompra.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9;");

        cbTipoDocumento = new ComboBox<>(FXCollections.observableArrayList("Guía de Despacho", "Factura", "DUS", "Sin Documento", "Otro"));
        cbTipoDocumento.setValue("Guía de Despacho");
        cbTipoDocumento.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbTipoDocumento.setMaxWidth(Double.MAX_VALUE);

        txtNumeroDocumento = new TextField();
        txtNumeroDocumento.setPromptText("N° Documento (Ej: 12345)");
        txtNumeroDocumento.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");

        cbZonaDestino = new ComboBox<>(FXCollections.observableArrayList("COMERCIAL", "PRIMARIA"));
        cbZonaDestino.setValue("COMERCIAL");
        cbZonaDestino.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbZonaDestino.setMaxWidth(Double.MAX_VALUE);
        cbZonaDestino.setOnAction(e -> {
            actualizarFiltroZonas();
            actualizarFiltroUbicaciones();
        });

        txtObservaciones = new TextArea();
        txtObservaciones.setPromptText("Observaciones de la recepción...");
        txtObservaciones.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtObservaciones.setPrefHeight(45);
        txtObservaciones.setMaxWidth(Double.MAX_VALUE);

        Label lblBod = new Label("Bodega:");
        lblBod.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblAnun = new Label("Anuncio:");
        lblAnun.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblBl = new Label("N° BL / O.C.:");
        lblBl.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblTipoDoc = new Label("Tipo / N° Doc:");
        lblTipoDoc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblZona = new Label("Zona Destino:");
        lblZona.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblObs = new Label("Observaciones:");
        lblObs.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");"""

new_form_grid = """        cbBodega = new ComboBox<>();
        cbBodega.setPromptText("Seleccione Bodega");
        cbBodega.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");
        cbBodega.setMaxWidth(Double.MAX_VALUE);
        cbBodega.setOnAction(e -> cargarAnunciosYLocationsDeBodega());

        cbAnuncio = new ComboBox<>();
        cbAnuncio.setPromptText("Seleccione Anuncio de Carga");
        cbAnuncio.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");
        cbAnuncio.setMaxWidth(Double.MAX_VALUE);
        cbAnuncio.setOnAction(e -> cargarItemsDeAnuncio());

        txtNumeroBl = new TextField();
        txtNumeroBl.setPromptText("N° BL (Auto)");
        txtNumeroBl.setEditable(false);
        txtNumeroBl.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9;");

        txtOrdenCompra = new TextField();
        txtOrdenCompra.setPromptText("Orden Compra (Auto)");
        txtOrdenCompra.setEditable(false);
        txtOrdenCompra.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9;");

        cbTipoDocumento = new ComboBox<>(FXCollections.observableArrayList("Guía de Despacho", "Factura", "DUS", "Sin Documento", "Otro"));
        cbTipoDocumento.setValue("Guía de Despacho");
        cbTipoDocumento.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");
        cbTipoDocumento.setMaxWidth(Double.MAX_VALUE);

        txtNumeroDocumento = new TextField();
        txtNumeroDocumento.setPromptText("N° Documento (Ej: 12345)");
        txtNumeroDocumento.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");

        cbZonaDestino = new ComboBox<>(FXCollections.observableArrayList("COMERCIAL", "PRIMARIA"));
        cbZonaDestino.setValue("COMERCIAL");
        cbZonaDestino.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");
        cbZonaDestino.setMaxWidth(Double.MAX_VALUE);
        cbZonaDestino.setOnAction(e -> {
            actualizarFiltroZonas();
            actualizarFiltroUbicaciones();
        });

        txtObservaciones = new TextArea();
        txtObservaciones.setPromptText("Observaciones de la recepción...");
        txtObservaciones.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtObservaciones.setPrefHeight(36);
        txtObservaciones.setMaxWidth(Double.MAX_VALUE);

        Label lblBod = new Label("Bodega:");
        lblBod.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblAnun = new Label("Anuncio:");
        lblAnun.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblBl = new Label("N° BL / O.C.:");
        lblBl.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblTipoDoc = new Label("Tipo / N° Doc:");
        lblTipoDoc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblZona = new Label("Zona Destino:");
        lblZona.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblObs = new Label("Observaciones:");
        lblObs.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");"""

content = content.replace(old_form_grid, new_form_grid)

# Update ColumnConstraints for formGrid
old_col_const = """        ColumnConstraints gc1 = new ColumnConstraints();
        gc1.setPrefWidth(120);"""

new_col_const = """        ColumnConstraints gc1 = new ColumnConstraints();
        gc1.setPrefWidth(135);"""

content = content.replace(old_col_const, new_col_const)

# Update itemModifyGrid labels and controls for compact style
old_item_modify = """        txtCantRecibida = new TextField();
        txtCantRecibida.setPromptText("Cant. Recibida");
        txtCantRecibida.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtCantRecibida.setMaxWidth(Double.MAX_VALUE);

        chkUbicacionExistente = new CheckBox("Con Stock Previo");
        chkUbicacionExistente.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        cbUbicacionExistente = new ComboBox<>();
        cbUbicacionExistente.setPromptText("Ubicación Existente (Stock)");
        cbUbicacionExistente.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbUbicacionExistente.setMaxWidth(Double.MAX_VALUE);
        cbUbicacionExistente.setDisable(true);

        cbZonaFisica = new ComboBox<>();
        cbZonaFisica.setPromptText("Todas las zonas...");
        cbZonaFisica.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbZonaFisica.setMaxWidth(Double.MAX_VALUE);
        configurarComboBoxSearchGeneric(cbZonaFisica, filteredZones);
        cbZonaFisica.valueProperty().addListener((obs, oldVal, newVal) -> {
            actualizarFiltroUbicaciones();
        });

        cbUbicacion = new ComboBox<>();
        cbUbicacion.setPromptText("Nueva Ubicación (WH)");
        cbUbicacion.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbUbicacion.setMaxWidth(Double.MAX_VALUE);
        configurarComboBoxSearchGeneric(cbUbicacion, filteredLocationItems);

        FontAwesomeIconView iconCheck = new FontAwesomeIconView(FontAwesomeIcon.CHECK);
        iconCheck.setFill(Color.WHITE);
        btnApplyItem = new Button(null, iconCheck);
        btnApplyItem.setStyle("-fx-background-color: #0F3E6E; -fx-padding: 6px 12px; -fx-background-radius: 4px; -fx-cursor: hand;");
        btnApplyItem.setTooltip(new Tooltip("Aplicar Cambios a Fila Seleccionada"));
        btnApplyItem.setMaxWidth(Double.MAX_VALUE);
        btnApplyItem.setOnAction(e -> actualizarFilaItem());

        Label lblCantRec = new Label("Cant. Recibida:");
        lblCantRec.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblZonaFis = new Label("Zona/Sector:");
        lblZonaFis.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblAsignLoc = new Label("Asignar Ubicación:");
        lblAsignLoc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");"""

new_item_modify = """        txtCantRecibida = new TextField();
        txtCantRecibida.setPromptText("Cant. Recibida");
        txtCantRecibida.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtCantRecibida.setMaxWidth(Double.MAX_VALUE);

        chkUbicacionExistente = new CheckBox("Con Stock Previo");
        chkUbicacionExistente.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        cbUbicacionExistente = new ComboBox<>();
        cbUbicacionExistente.setPromptText("Ubicación Existente (Stock)");
        cbUbicacionExistente.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");
        cbUbicacionExistente.setMaxWidth(Double.MAX_VALUE);
        cbUbicacionExistente.setDisable(true);

        cbZonaFisica = new ComboBox<>();
        cbZonaFisica.setPromptText("Todas las zonas...");
        cbZonaFisica.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");
        cbZonaFisica.setMaxWidth(Double.MAX_VALUE);
        configurarComboBoxSearchGeneric(cbZonaFisica, filteredZones);
        cbZonaFisica.valueProperty().addListener((obs, oldVal, newVal) -> {
            actualizarFiltroUbicaciones();
        });

        cbUbicacion = new ComboBox<>();
        cbUbicacion.setPromptText("Nueva Ubicación (WH)");
        cbUbicacion.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");
        cbUbicacion.setMaxWidth(Double.MAX_VALUE);
        configurarComboBoxSearchGeneric(cbUbicacion, filteredLocationItems);

        FontAwesomeIconView iconCheck = new FontAwesomeIconView(FontAwesomeIcon.CHECK);
        iconCheck.setFill(Color.WHITE);
        btnApplyItem = new Button(null, iconCheck);
        btnApplyItem.setStyle("-fx-background-color: #0F3E6E; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-cursor: hand;");
        btnApplyItem.setTooltip(new Tooltip("Aplicar Cambios a Fila Seleccionada"));
        btnApplyItem.setMaxWidth(Double.MAX_VALUE);
        btnApplyItem.setOnAction(e -> actualizarFilaItem());

        Label lblCantRec = new Label("Cant. Recibida:");
        lblCantRec.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblZonaFis = new Label("Zona/Sector:");
        lblZonaFis.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblAsignLoc = new Label("Asignar Ubicación:");
        lblAsignLoc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");"""

content = content.replace(old_item_modify, new_item_modify)

# Update ColumnConstraints for itemModifyGrid
old_mc_const = """        ColumnConstraints mc1 = new ColumnConstraints();
        mc1.setPrefWidth(125);"""

new_mc_const = """        ColumnConstraints mc1 = new ColumnConstraints();
        mc1.setPrefWidth(135);"""

content = content.replace(old_mc_const, new_mc_const)

# Update btnSplit in table cell for compact styling
old_btn_split = """                btnSplit.setStyle("-fx-background-color: #0d6efd; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-cursor: hand;");"""
new_btn_split = """                btnSplit.setStyle("-fx-background-color: #0d6efd; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-cursor: hand;");"""

content = content.replace(old_btn_split, new_btn_split)

# Update bottom action buttons padding & size
old_btn_save = """btnGuardarRecepcion.setStyle("-fx-background-color: #0F3E6E; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");"""
new_btn_save = """btnGuardarRecepcion.setStyle("-fx-background-color: #0F3E6E; -fx-cursor: hand; -fx-padding: 5px 10px; -fx-background-radius: 4px;");"""

content = content.replace(old_btn_save, new_btn_save)

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content)

print("Updated RecepcionesDialog with compact inputs & visible lateral labels")
