import re

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content = f.read()

# Add txtOrdenCompra field declaration
content = content.replace("private TextField txtNumeroBl;", "private TextField txtNumeroBl;\n    private TextField txtOrdenCompra;")

# Setup txtOrdenCompra in formGrid
old_form_grid = """        txtNumeroBl = new TextField();
        txtNumeroBl.setPromptText("N° BL (Opcional)");
        txtNumeroBl.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");

        txtObservaciones = new TextArea();
        txtObservaciones.setPromptText("Observaciones...");
        txtObservaciones.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtObservaciones.setPrefHeight(50);

        Label lblBod = new Label("Bodega:");
        lblBod.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblProv = new Label("Proveedor:");
        lblProv.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblBl = new Label("N° BL:");
        lblBl.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblObs = new Label("Observaciones:");
        lblObs.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");

        formGrid.add(lblBod, 0, 0);
        formGrid.add(cbBodega, 1, 0);
        formGrid.add(lblProv, 0, 1);
        formGrid.add(cbProveedor, 1, 1);
        formGrid.add(lblBl, 0, 2);
        formGrid.add(txtNumeroBl, 1, 2);
        formGrid.add(lblObs, 0, 3);
        formGrid.add(txtObservaciones, 1, 3);"""

new_form_grid = """        txtNumeroBl = new TextField();
        txtNumeroBl.setPromptText("N° BL (Opcional)");
        txtNumeroBl.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");

        txtOrdenCompra = new TextField();
        txtOrdenCompra.setPromptText("Orden de Compra (Opcional)");
        txtOrdenCompra.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");

        txtObservaciones = new TextArea();
        txtObservaciones.setPromptText("Observaciones...");
        txtObservaciones.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");
        txtObservaciones.setPrefHeight(45);

        Label lblBod = new Label("Bodega:");
        lblBod.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblProv = new Label("Proveedor:");
        lblProv.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblBl = new Label("N° BL:");
        lblBl.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblOc = new Label("Orden Compra:");
        lblOc.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label lblObs = new Label("Observaciones:");
        lblObs.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");

        formGrid.add(lblBod, 0, 0);
        formGrid.add(cbBodega, 1, 0);
        formGrid.add(lblProv, 0, 1);
        formGrid.add(cbProveedor, 1, 1);
        formGrid.add(lblBl, 0, 2);
        formGrid.add(txtNumeroBl, 1, 2);
        formGrid.add(lblOc, 0, 3);
        formGrid.add(txtOrdenCompra, 1, 3);
        formGrid.add(lblObs, 0, 4);
        formGrid.add(txtObservaciones, 1, 4);"""

content = content.replace(old_form_grid, new_form_grid)

# Update selectionListener to load txtOrdenCompra
old_sel_listener = """                txtNumeroBl.setText(newSel.getNumeroBl() != null ? newSel.getNumeroBl() : "");
                txtObservaciones.setText(newSel.getObservaciones() != null ? newSel.getObservaciones() : "");"""

new_sel_listener = """                txtNumeroBl.setText(newSel.getNumeroBl() != null ? newSel.getNumeroBl() : "");
                txtOrdenCompra.setText(newSel.getOrdenCompra() != null ? newSel.getOrdenCompra() : "");
                txtObservaciones.setText(newSel.getObservaciones() != null ? newSel.getObservaciones() : "");"""

content = content.replace(old_sel_listener, new_sel_listener)

# Update disable state in selectionListener
old_disable_state = """                txtNumeroBl.setDisable(!isPending);
                txtObservaciones.setDisable(!isPending);"""

new_disable_state = """                txtNumeroBl.setDisable(!isPending);
                txtOrdenCompra.setDisable(!isPending);
                txtObservaciones.setDisable(!isPending);"""

content = content.replace(old_disable_state, new_disable_state)

# Update guardarAnuncio to set ordenCompra
old_guardar = """        String blVal = txtNumeroBl.getText().trim();
        model.setNumeroBl(blVal.isEmpty() ? null : blVal);
        model.setObservaciones(obs.isEmpty() ? null : obs);"""

new_guardar = """        String blVal = txtNumeroBl.getText().trim();
        model.setNumeroBl(blVal.isEmpty() ? null : blVal);
        String ocVal = txtOrdenCompra.getText().trim();
        model.setOrdenCompra(ocVal.isEmpty() ? null : ocVal);
        model.setObservaciones(obs.isEmpty() ? null : obs);"""

content = content.replace(old_guardar, new_guardar)

# Update limpiarFormulario to clear txtOrdenCompra
old_limpiar = """        txtNumeroBl.clear();
        txtNumeroBl.setDisable(false);
        txtObservaciones.clear();
        txtObservaciones.setDisable(false);"""

new_limpiar = """        txtNumeroBl.clear();
        txtNumeroBl.setDisable(false);
        txtOrdenCompra.clear();
        txtOrdenCompra.setDisable(false);
        txtObservaciones.clear();
        txtObservaciones.setDisable(false);"""

content = content.replace(old_limpiar, new_limpiar)

# Update tblComercial columns to add Orden Compra column
old_col_add = "table.getColumns().addAll(colFolio, colFecha, colBod, colProv, colBl, colEst, colRecFolio);"
new_col_add = """TableColumn<ReceptionAnnouncementModel, String> colOc = new TableColumn<>("Orden Compra");
        colOc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOrdenCompra() != null ? c.getValue().getOrdenCompra() : "-"));
        colOc.setPrefWidth(90);

        table.getColumns().addAll(colFolio, colFecha, colBod, colProv, colBl, colOc, colEst, colRecFolio);"""

content = content.replace(old_col_add, new_col_add)

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content)

print("Updated AnunciosDialog with Orden de Compra field")
