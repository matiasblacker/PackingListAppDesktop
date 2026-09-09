import re

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

# Add btnLimpiarRecepcion declaration if needed
content = content.replace("private Button btnGuardarRecepcion;", "private Button btnGuardarRecepcion;\n    private Button btnLimpiarRecepcion;")

# Update formGrid setup in construirUI to match Anuncios (one row per field)
old_form_grid = """        cbBodega = new ComboBox<>();
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
        lblObs.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");

        HBox blOcBox = new HBox(8, txtNumeroBl, txtOrdenCompra);
        HBox.setHgrow(txtNumeroBl, Priority.ALWAYS);
        HBox.setHgrow(txtOrdenCompra, Priority.ALWAYS);

        HBox docBox = new HBox(8, cbTipoDocumento, txtNumeroDocumento);
        HBox.setHgrow(cbTipoDocumento, Priority.ALWAYS);
        HBox.setHgrow(txtNumeroDocumento, Priority.ALWAYS);

        formGrid.add(lblBod, 0, 0);
        formGrid.add(cbBodega, 1, 0);
        formGrid.add(lblAnun, 0, 1);
        formGrid.add(cbAnuncio, 1, 1);
        formGrid.add(lblBl, 0, 2);
        formGrid.add(blOcBox, 1, 2);
        formGrid.add(lblTipoDoc, 0, 3);
        formGrid.add(docBox, 1, 3);
        formGrid.add(lblZona, 0, 4);
        formGrid.add(cbZonaDestino, 1, 4);
        formGrid.add(lblObs, 0, 5);
        formGrid.add(txtObservaciones, 1, 5);

        ColumnConstraints gc1 = new ColumnConstraints();
        gc1.setPrefWidth(135);
        ColumnConstraints gc2 = new ColumnConstraints();
        gc2.setHgrow(Priority.ALWAYS);
        formGrid.getColumnConstraints().addAll(gc1, gc2);"""

new_form_grid = """        cbBodega = new ComboBox<>();
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
        Label lblBl = new Label("N° BL:");
        lblBl.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblOc = new Label("Orden Compra:");
        lblOc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblTipoDoc = new Label("Tipo Documento:");
        lblTipoDoc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblNumDoc = new Label("N° Documento:");
        lblNumDoc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblZona = new Label("Zona Destino:");
        lblZona.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblObs = new Label("Observaciones:");
        lblObs.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");

        formGrid.add(lblBod, 0, 0);
        formGrid.add(cbBodega, 1, 0);
        formGrid.add(lblAnun, 0, 1);
        formGrid.add(cbAnuncio, 1, 1);
        formGrid.add(lblBl, 0, 2);
        formGrid.add(txtNumeroBl, 1, 2);
        formGrid.add(lblOc, 0, 3);
        formGrid.add(txtOrdenCompra, 1, 3);
        formGrid.add(lblTipoDoc, 0, 4);
        formGrid.add(cbTipoDocumento, 1, 4);
        formGrid.add(lblNumDoc, 0, 5);
        formGrid.add(txtNumeroDocumento, 1, 5);
        formGrid.add(lblZona, 0, 6);
        formGrid.add(cbZonaDestino, 1, 6);
        formGrid.add(lblObs, 0, 7);
        formGrid.add(txtObservaciones, 1, 7);

        ColumnConstraints gc1 = new ColumnConstraints(110);
        ColumnConstraints gc2 = new ColumnConstraints(350);
        formGrid.getColumnConstraints().addAll(gc1, gc2);"""

content = content.replace(old_form_grid, new_form_grid)

# Replace giant green button with actionButtons HBox matching Anuncios
old_save_btn = """        // Guardar
        btnGuardarRecepcion = new Button("Procesar Recepción", new FontAwesomeIconView(FontAwesomeIcon.TRUCK));
        btnGuardarRecepcion.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnGuardarRecepcion.setMaxWidth(Double.MAX_VALUE);
        btnGuardarRecepcion.setOnAction(e -> procesarRecepcion());

        rightPane.getChildren().addAll(lblNuevo, formGrid, itemsBox, btnGuardarRecepcion);"""

new_save_btn = """        // Botones de acción inferior matching Anuncios
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

        rightPane.getChildren().addAll(lblNuevo, formGrid, itemsBox, actionButtons);"""

content = content.replace(old_save_btn, new_save_btn)

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content)

print("Successfully updated RecepcionesDialog formGrid to match AnunciosDialog exactly!")
