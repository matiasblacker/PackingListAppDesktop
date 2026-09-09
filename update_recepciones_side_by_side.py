import re

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

target_pattern = r"GridPane formGrid = new GridPane\(\);.*?formGrid\.getColumnConstraints\(\)\.addAll\(gc1, gc2\);"

replacement_code = """GridPane formGrid = new GridPane();
        formGrid.setHgap(8);
        formGrid.setVgap(8);

        cbBodega = new ComboBox<>();
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
        txtObservaciones.setPromptText("Observaciones...");
        txtObservaciones.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtObservaciones.setPrefHeight(36);
        txtObservaciones.setMaxWidth(Double.MAX_VALUE);

        Label lblBod = new Label("Bodega:");
        lblBod.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblAnun = new Label("Anuncio:");
        lblAnun.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblBl = new Label("N° BL:");
        lblBl.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblOc = new Label("Orden Compra:");
        lblOc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblTipoDoc = new Label("Tipo Doc:");
        lblTipoDoc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblNumDoc = new Label("N° Doc:");
        lblNumDoc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblZona = new Label("Zona Destino:");
        lblZona.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblObs = new Label("Observaciones:");
        lblObs.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");

        // Fila 0: Bodega (col 0,1) | Anuncio (col 2,3)
        formGrid.add(lblBod, 0, 0);
        formGrid.add(cbBodega, 1, 0);
        formGrid.add(lblAnun, 2, 0);
        formGrid.add(cbAnuncio, 3, 0);

        // Fila 1: N° BL (col 0,1) | Orden Compra (col 2,3)
        formGrid.add(lblBl, 0, 1);
        formGrid.add(txtNumeroBl, 1, 1);
        formGrid.add(lblOc, 2, 1);
        formGrid.add(txtOrdenCompra, 3, 1);

        // Fila 2: Tipo Doc (col 0,1) | N° Doc (col 2,3)
        formGrid.add(lblTipoDoc, 0, 2);
        formGrid.add(cbTipoDocumento, 1, 2);
        formGrid.add(lblNumDoc, 2, 2);
        formGrid.add(txtNumeroDocumento, 3, 2);

        // Fila 3: Zona Destino (col 0,1) | Observaciones (col 2,3)
        formGrid.add(lblZona, 0, 3);
        formGrid.add(cbZonaDestino, 1, 3);
        formGrid.add(lblObs, 2, 3);
        formGrid.add(txtObservaciones, 3, 3);

        ColumnConstraints gc1 = new ColumnConstraints(75);
        ColumnConstraints gc2 = new ColumnConstraints(155);
        ColumnConstraints gc3 = new ColumnConstraints(90);
        ColumnConstraints gc4 = new ColumnConstraints(155);
        formGrid.getColumnConstraints().addAll(gc1, gc2, gc3, gc4);"""

new_content, count = re.subn(target_pattern, replacement_code, content, flags=re.DOTALL)

if count > 0:
    with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
        f.write(new_content)
    print(f"Successfully updated RecepcionesDialog to side-by-side layout ({count} occurrence)")
else:
    print("Pattern not matched in RecepcionesDialog!")
