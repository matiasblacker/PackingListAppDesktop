import re

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content = f.read()

# 1. Rename colFolio to "Folio Anuncio"
content = content.replace('TableColumn<ReceptionAnnouncementModel, String> colFolio = new TableColumn<>("Folio");', 'TableColumn<ReceptionAnnouncementModel, String> colFolio = new TableColumn<>("Folio Anuncio");')

# 2. Style colEst with Icons
old_status_style = 'StatusColumnHelper.applyStatusStyling(colEst, false);'
new_status_style = """        colEst.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("RECEPCIONADO".equalsIgnoreCase(item)) {
                        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);
                        icon.setFill(Color.web("#16a34a"));
                        icon.setSize("14");
                        setGraphic(icon);
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else if ("ANULADO".equalsIgnoreCase(item)) {
                        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.TIMES_CIRCLE);
                        icon.setFill(Color.web("#dc2626"));
                        icon.setSize("14");
                        setGraphic(icon);
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    } else {
                        setGraphic(null);
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    }
                }
            }
        });"""
content = content.replace(old_status_style, new_status_style)

# 3. Add Line Product Button (blue, + icon)
old_add_prod = """        Button btnAddProduct = new Button("Agregar");
        btnAddProduct.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");"""
new_add_prod = """        FontAwesomeIconView iconAdd = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        iconAdd.setFill(Color.WHITE);
        Button btnAddProduct = new Button(null, iconAdd);
        btnAddProduct.setStyle("-fx-background-color: #0d6efd; -fx-padding: 6px 12px; -fx-background-radius: 4px; -fx-cursor: hand;");"""
content = content.replace(old_add_prod, new_add_prod)

# 4. Excel Button (light green, icon only)
old_btn_excel = """        Button btnExcel = new Button("Excel", new FontAwesomeIconView(FontAwesomeIcon.FILE_EXCEL_ALT));
        btnExcel.setStyle("-fx-background-color: #1f7246; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");"""
new_btn_excel = """        FontAwesomeIconView iconExcel = new FontAwesomeIconView(FontAwesomeIcon.FILE_EXCEL_ALT);
        iconExcel.setFill(Color.WHITE);
        Button btnExcel = new Button(null, iconExcel);
        btnExcel.setStyle("-fx-background-color: #28a745; -fx-padding: 6px 12px; -fx-background-radius: 4px; -fx-cursor: hand;");"""
content = content.replace(old_btn_excel, new_btn_excel)

# 5. Trash Icon in Action Column
old_btn_remove = """private final Button btnRemove = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            {
                btnRemove.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");"""
new_btn_remove = """private final Button btnRemove;
            {
                FontAwesomeIconView iconTrash = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                iconTrash.setFill(Color.WHITE);
                btnRemove = new Button(null, iconTrash);
                btnRemove.setStyle("-fx-background-color: #dc3545; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-cursor: hand;");"""
content = content.replace(old_btn_remove, new_btn_remove)

# 6. Bottom actionButtons updates
old_action_btns = """        btnGuardarAnuncio.setStyle("-fx-background-color: #0F3E6E; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        HBox.setHgrow(btnGuardarAnuncio, Priority.ALWAYS);
        btnGuardarAnuncio.setMaxWidth(Double.MAX_VALUE);
        btnGuardarAnuncio.setOnAction(e -> guardarAnuncio());
        Tooltip.install(btnGuardarAnuncio, new Tooltip("Registrar Anuncio de Carga"));

        FontAwesomeIconView iconClose = new FontAwesomeIconView(FontAwesomeIcon.CLOSE);
        iconClose.setFill(Color.WHITE);
        iconClose.setSize("16");
        btnEliminarAnuncio = new Button(null, iconClose);
        btnEliminarAnuncio.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnEliminarAnuncio.setVisible(false);
        btnEliminarAnuncio.setManaged(false);
        btnEliminarAnuncio.setOnAction(e -> eliminarAnuncioSeleccionado());
        Tooltip.install(btnEliminarAnuncio, new Tooltip("Anular Anuncio de Carga"));

        FontAwesomeIconView iconRefresh = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        iconRefresh.setFill(Color.WHITE);
        iconRefresh.setSize("16");
        btnLimpiarAnuncio = new Button(null, iconRefresh);
        btnLimpiarAnuncio.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnLimpiarAnuncio.setOnAction(e -> {
            clearAnnouncementsSelection();
            limpiarFormulario();
        });
        Tooltip.install(btnLimpiarAnuncio, new Tooltip("Limpiar Formulario"));

        actionButtons.getChildren().addAll(btnGuardarAnuncio, btnEliminarAnuncio, btnLimpiarAnuncio);

        rightPane.getChildren().addAll(lblNuevo, formGrid, itemsBox, actionButtons);

        ScrollPane rightScroll = new ScrollPane(rightPane);"""

new_action_btns = """        btnGuardarAnuncio.setStyle("-fx-background-color: #0F3E6E; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnGuardarAnuncio.setOnAction(e -> guardarAnuncio());
        Tooltip.install(btnGuardarAnuncio, new Tooltip("Registrar Anuncio de Carga"));

        FontAwesomeIconView iconClose = new FontAwesomeIconView(FontAwesomeIcon.CLOSE);
        iconClose.setFill(Color.WHITE);
        iconClose.setSize("16");
        btnEliminarAnuncio = new Button(null, iconClose);
        btnEliminarAnuncio.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnEliminarAnuncio.setVisible(false);
        btnEliminarAnuncio.setManaged(false);
        btnEliminarAnuncio.setOnAction(e -> eliminarAnuncioSeleccionado());
        Tooltip.install(btnEliminarAnuncio, new Tooltip("Anular Anuncio de Carga"));

        FontAwesomeIconView iconRefresh = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        iconRefresh.setFill(Color.WHITE);
        iconRefresh.setSize("16");
        btnLimpiarAnuncio = new Button(null, iconRefresh);
        btnLimpiarAnuncio.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnLimpiarAnuncio.setOnAction(e -> {
            clearAnnouncementsSelection();
            limpiarFormulario();
        });
        Tooltip.install(btnLimpiarAnuncio, new Tooltip("Limpiar Formulario"));

        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setPadding(new Insets(15, 0, 0, 0));
        actionButtons.getChildren().addAll(btnGuardarAnuncio, btnEliminarAnuncio, btnLimpiarAnuncio);

        rightPane.getChildren().addAll(lblNuevo, formGrid, itemsBox);

        ScrollPane rightScroll = new ScrollPane(rightPane);"""
content = content.replace(old_action_btns, new_action_btns)

# Now, ensure actionButtons is placed in root.setBottom()
old_root_set = """        root.setCenter(mainSplit);"""
new_root_set = """        root.setCenter(mainSplit);
        root.setBottom(actionButtons);"""
content = content.replace(old_root_set, new_root_set)

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content)
print("Updated AnunciosDialog")
