import re

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content = f.read()

target_pattern = r"GridPane formGrid = new GridPane\(\);.*?formGrid\.getColumnConstraints\(\)\.addAll\(gc1, gc2\);"

replacement_code = """GridPane formGrid = new GridPane();
        formGrid.setHgap(8);
        formGrid.setVgap(8);

        cbBodega = new ComboBox<>();
        cbBodega.setPromptText("Seleccione Bodega");
        cbBodega.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");
        cbBodega.setMaxWidth(Double.MAX_VALUE);

        cbProveedor = new AutocompleteComboBox<>();
        cbProveedor.setPromptText("Seleccione Proveedor");
        cbProveedor.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");
        cbProveedor.setMaxWidth(Double.MAX_VALUE);
        cbProveedor.setFilterPredicate((s, text) -> {
            return (s.getRazonSocial() != null && s.getRazonSocial().toLowerCase().contains(text))
                || (s.getRut() != null && s.getRut().toLowerCase().contains(text))
                || (s.getId() != null && s.getId().toLowerCase().contains(text));
        });

        txtNumeroBl = new TextField();
        txtNumeroBl.setPromptText("N° BL (Opcional)");
        txtNumeroBl.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");

        txtOrdenCompra = new TextField();
        txtOrdenCompra.setPromptText("Orden de Compra (Opcional)");
        txtOrdenCompra.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");

        txtObservaciones = new TextArea();
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
        GridPane.setColumnSpan(txtObservaciones, 3);

        ColumnConstraints gc1 = new ColumnConstraints(75);
        ColumnConstraints gc2 = new ColumnConstraints(155);
        ColumnConstraints gc3 = new ColumnConstraints(90);
        ColumnConstraints gc4 = new ColumnConstraints(155);
        formGrid.getColumnConstraints().addAll(gc1, gc2, gc3, gc4);"""

new_content, count = re.subn(target_pattern, replacement_code, content, flags=re.DOTALL)

if count > 0:
    with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
        f.write(new_content)
    print(f"Successfully updated AnunciosDialog to side-by-side layout ({count} occurrence)")
else:
    print("Pattern not matched in AnunciosDialog!")
