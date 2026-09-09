with open("src/main/java/com/logistics/packinglist/ui/UbicacionesDialog.java", "r") as f:
    content = f.read()

# Fix root.getChildren() in UbicacionesDialog
content = content.replace("root.getChildren().addAll(headerBox, mainSplit, actionRow);", "root.getChildren().addAll(headerBox, mainSplit);")

# Update buttons in crearFormularioUbicacion
old_btns = """        // Acciones
        btnGuardarLoc = new Button("Guardar", new FontAwesomeIconView(FontAwesomeIcon.SAVE));
        btnGuardarLoc.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnGuardarLoc.setOnAction(e -> guardarUbicacion());

        btnEliminarLoc = new Button("Eliminar", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
        btnEliminarLoc.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnEliminarLoc.setOnAction(e -> eliminarUbicacion());

        btnLimpiarLoc = new Button("Limpiar", new FontAwesomeIconView(FontAwesomeIcon.ERASER));
        btnLimpiarLoc.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnLimpiarLoc.setOnAction(e -> limpiarFormularioUbicacion());

        btnGenerarLote = new Button("Generación en Lote", new FontAwesomeIconView(FontAwesomeIcon.COGS));
        btnGenerarLote.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnGenerarLote.setOnAction(e -> abrirGeneradorLote());

        HBox actionRow = new HBox(8, btnAdd, btnClear, btnDelete);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        grid.add(btnBox, 0, 3, 4, 1);"""

new_btns = """        // Acciones
        FontAwesomeIconView iconSaveLoc = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSaveLoc.setFill(Color.WHITE);
        btnGuardarLoc = new Button("", iconSaveLoc);
        btnGuardarLoc.setStyle("-fx-background-color: cornflowerblue; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnGuardarLoc.setOnAction(e -> guardarUbicacion());
        Tooltip.install(btnGuardarLoc, new Tooltip("Guardar Ubicación"));

        FontAwesomeIconView iconClearLoc = new FontAwesomeIconView(FontAwesomeIcon.UNDO);
        iconClearLoc.setFill(Color.WHITE);
        btnLimpiarLoc = new Button("", iconClearLoc);
        btnLimpiarLoc.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnLimpiarLoc.setOnAction(e -> limpiarFormularioUbicacion());
        Tooltip.install(btnLimpiarLoc, new Tooltip("Limpiar Formulario"));

        FontAwesomeIconView iconDelLoc = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDelLoc.setFill(Color.WHITE);
        btnEliminarLoc = new Button("", iconDelLoc);
        btnEliminarLoc.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnEliminarLoc.setOnAction(e -> eliminarUbicacion());
        Tooltip.install(btnEliminarLoc, new Tooltip("Eliminar Ubicación"));

        FontAwesomeIconView iconBatch = new FontAwesomeIconView(FontAwesomeIcon.COGS);
        iconBatch.setFill(Color.WHITE);
        btnGenerarLote = new Button("", iconBatch);
        btnGenerarLote.setStyle("-fx-background-color: #28a745; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnGenerarLote.setOnAction(e -> abrirGeneradorLote());
        Tooltip.install(btnGenerarLote, new Tooltip("Generación en Lote"));

        HBox btnBoxLoc = new HBox(8, btnGuardarLoc, btnLimpiarLoc, btnEliminarLoc, btnGenerarLote);
        btnBoxLoc.setAlignment(Pos.CENTER_RIGHT);
        grid.add(btnBoxLoc, 0, 3, 4, 1);"""

content = content.replace(old_btns, new_btns)

with open("src/main/java/com/logistics/packinglist/ui/UbicacionesDialog.java", "w") as f:
    f.write(content)

print("Fixed UbicacionesDialog.java!")
