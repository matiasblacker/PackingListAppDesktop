import re

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content = f.read()

# Add field declarations
field_decl_target = "private Button btnGuardarAnuncio;"
field_decl_replacement = """private CheckBox chkEsContenedor;
    private TextField txtNumeroContenedor;
    private TextField txtDigitoContenedor;
    private Button btnGuardarAnuncio;"""
content = content.replace(field_decl_target, field_decl_replacement)

# Update formGrid construction
old_grid = """        txtObservaciones = new TextArea();
        txtObservaciones.setPromptText("Observaciones...");
        txtObservaciones.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtObservaciones.setPrefHeight(36);

        Label lblBod = new Label("Bodega:");
        lblBod.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblProv = new Label("Proveedor:");
        lblProv.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblBl = new Label("N° BL:");
        lblBl.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblOc = new Label("Orden Compra:");
        lblOc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblObs = new Label("Observaciones:");
        lblObs.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");

        // Fila 0: Bodega (col 0,1) | Proveedor (col 2,3)
        formGrid.add(lblBod, 0, 0);
        formGrid.add(cbBodega, 1, 0);
        formGrid.add(lblProv, 2, 0);
        formGrid.add(cbProveedor, 3, 0);

        // Fila 1: N° BL (col 0,1) | Orden Compra (col 2,3)
        formGrid.add(lblBl, 0, 1);
        formGrid.add(txtNumeroBl, 1, 1);
        formGrid.add(lblOc, 2, 1);
        formGrid.add(txtOrdenCompra, 3, 1);

        // Fila 2: Observaciones
        formGrid.add(lblObs, 0, 2);
        formGrid.add(txtObservaciones, 1, 2);
        GridPane.setColumnSpan(txtObservaciones, 3);"""

new_grid = """        chkEsContenedor = new CheckBox("Contenedor");
        chkEsContenedor.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        txtNumeroContenedor = new TextField();
        txtNumeroContenedor.setPromptText("N° Contenedor");
        txtNumeroContenedor.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");
        txtNumeroContenedor.setDisable(true);

        txtDigitoContenedor = new TextField();
        txtDigitoContenedor.setPromptText("DV");
        txtDigitoContenedor.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");
        txtDigitoContenedor.setPrefWidth(45);
        txtDigitoContenedor.setDisable(true);

        chkEsContenedor.selectedProperty().addListener((obs, oldV, newV) -> {
            txtNumeroContenedor.setDisable(!newV);
            txtDigitoContenedor.setDisable(!newV);
            if (!newV) {
                txtNumeroContenedor.clear();
                txtDigitoContenedor.clear();
            }
        });

        HBox containerBox = new HBox(6, txtNumeroContenedor, txtDigitoContenedor);
        HBox.setHgrow(txtNumeroContenedor, Priority.ALWAYS);

        txtObservaciones = new TextArea();
        txtObservaciones.setPromptText("Observaciones...");
        txtObservaciones.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtObservaciones.setPrefHeight(72);

        Label lblBod = new Label("Bodega:");
        lblBod.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblProv = new Label("Proveedor:");
        lblProv.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblBl = new Label("N° BL:");
        lblBl.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblOc = new Label("Orden Compra:");
        lblOc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblObs = new Label("Observaciones:");
        lblObs.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");

        // Fila 0: Bodega (col 0,1) | Proveedor (col 2,3)
        formGrid.add(lblBod, 0, 0);
        formGrid.add(cbBodega, 1, 0);
        formGrid.add(lblProv, 2, 0);
        formGrid.add(cbProveedor, 3, 0);

        // Fila 1: N° BL (col 0,1) | Orden Compra (col 2,3)
        formGrid.add(lblBl, 0, 1);
        formGrid.add(txtNumeroBl, 1, 1);
        formGrid.add(lblOc, 2, 1);
        formGrid.add(txtOrdenCompra, 3, 1);

        // Fila 2: Check Contenedor (col 0,1) | N° Contenedor + DV (col 2,3)
        formGrid.add(chkEsContenedor, 0, 2);
        GridPane.setColumnSpan(chkEsContenedor, 2);
        formGrid.add(containerBox, 2, 2);
        GridPane.setColumnSpan(containerBox, 2);

        // Fila 3: Observaciones
        formGrid.add(lblObs, 0, 3);
        formGrid.add(txtObservaciones, 1, 3);
        GridPane.setColumnSpan(txtObservaciones, 3);"""

content = content.replace(old_grid, new_grid)

# Update guardarAnuncio()
old_guardar = """        String ocVal = txtOrdenCompra.getText().trim();
        model.setOrdenCompra(ocVal.isEmpty() ? null : ocVal);
        model.setObservaciones(obs.isEmpty() ? null : obs);"""

new_guardar = """        String ocVal = txtOrdenCompra.getText().trim();
        model.setOrdenCompra(ocVal.isEmpty() ? null : ocVal);
        boolean isCont = chkEsContenedor.isSelected();
        model.setEsContenedor(isCont);
        String numCont = txtNumeroContenedor.getText().trim();
        model.setNumeroContenedor(isCont && !numCont.isEmpty() ? numCont : null);
        String digCont = txtDigitoContenedor.getText().trim();
        model.setDigitoContenedor(isCont && !digCont.isEmpty() ? digCont : null);
        model.setObservaciones(obs.isEmpty() ? null : obs);"""

content = content.replace(old_guardar, new_guardar)

# Update selectionListener
old_select = """                txtNumeroBl.setText(newSel.getNumeroBl() != null ? newSel.getNumeroBl() : "");
                txtOrdenCompra.setText(newSel.getOrdenCompra() != null ? newSel.getOrdenCompra() : "");
                txtObservaciones.setText(newSel.getObservaciones() != null ? newSel.getObservaciones() : "");"""

new_select = """                txtNumeroBl.setText(newSel.getNumeroBl() != null ? newSel.getNumeroBl() : "");
                txtOrdenCompra.setText(newSel.getOrdenCompra() != null ? newSel.getOrdenCompra() : "");
                boolean esCont = newSel.getEsContenedor() != null && newSel.getEsContenedor();
                chkEsContenedor.setSelected(esCont);
                txtNumeroContenedor.setText(esCont && newSel.getNumeroContenedor() != null ? newSel.getNumeroContenedor() : "");
                txtDigitoContenedor.setText(esCont && newSel.getDigitoContenedor() != null ? newSel.getDigitoContenedor() : "");
                txtObservaciones.setText(newSel.getObservaciones() != null ? newSel.getObservaciones() : "");"""

content = content.replace(old_select, new_select)

old_disable = """                txtNumeroBl.setDisable(!isPending);
                txtOrdenCompra.setDisable(!isPending);
                txtObservaciones.setDisable(!isPending);"""

new_disable = """                txtNumeroBl.setDisable(!isPending);
                txtOrdenCompra.setDisable(!isPending);
                chkEsContenedor.setDisable(!isPending);
                txtNumeroContenedor.setDisable(!isPending || !chkEsContenedor.isSelected());
                txtDigitoContenedor.setDisable(!isPending || !chkEsContenedor.isSelected());
                txtObservaciones.setDisable(!isPending);"""

content = content.replace(old_disable, new_disable)

# Update limpiarFormulario()
old_limpiar = """        cbBodega.getSelectionModel().clearSelection();
        cbProveedor.selectItem(null);
        txtObservaciones.clear();"""

new_limpiar = """        cbBodega.getSelectionModel().clearSelection();
        cbProveedor.selectItem(null);
        chkEsContenedor.setSelected(false);
        txtNumeroContenedor.clear();
        txtDigitoContenedor.clear();
        chkEsContenedor.setDisable(false);
        txtNumeroContenedor.setDisable(true);
        txtDigitoContenedor.setDisable(true);
        txtObservaciones.clear();"""

content = content.replace(old_limpiar, new_limpiar)

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content)

print("Updated AnunciosDialog with container fields & double height observations!")
