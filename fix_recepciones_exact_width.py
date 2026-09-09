import re

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

# Replace the formGrid definition completely with fixed 350px column width and individual rows
target_pattern = r"GridPane formGrid = new GridPane\(\);.*?formGrid\.getColumnConstraints\(\)\.addAll\(gc1, gc2\);"

replacement_code = """GridPane formGrid = new GridPane();
        formGrid.setHgap(8);
        formGrid.setVgap(8);

        cbBodega = new ComboBox<>();
        cbBodega.setPromptText("Seleccione Bodega");
        cbBodega.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");

        cbAnuncio = new ComboBox<>();
        cbAnuncio.setPromptText("Seleccione Anuncio de Carga");
        cbAnuncio.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
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

        txtNumeroDocumento = new TextField();
        txtNumeroDocumento.setPromptText("N° Documento (Ej: 12345)");
        txtNumeroDocumento.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");

        cbZonaDestino = new ComboBox<>(FXCollections.observableArrayList("COMERCIAL", "PRIMARIA"));
        cbZonaDestino.setValue("COMERCIAL");
        cbZonaDestino.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        cbZonaDestino.setOnAction(e -> {
            actualizarFiltroZonas();
            actualizarFiltroUbicaciones();
        });

        txtObservaciones = new TextArea();
        txtObservaciones.setPromptText("Observaciones de la recepción...");
        txtObservaciones.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtObservaciones.setPrefHeight(45);

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

new_content, count = re.subn(target_pattern, replacement_code, content, flags=re.DOTALL)

if count > 0:
    with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
        f.write(new_content)
    print(f"Successfully replaced formGrid layout in RecepcionesDialog ({count} occurrence)")
else:
    print("Pattern not matched!")
