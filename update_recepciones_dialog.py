import re

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

# Add field declarations
field_decl_target = "private Button btnGuardarRecepcion;"
field_decl_replacement = """private CheckBox chkEsContenedor;
    private TextField txtNumeroContenedor;
    private TextField txtDigitoContenedor;
    private HBox customFieldsPane;
    private final List<com.logistics.packinglist.model.ProductFieldDefinitionModel> activeFieldDefinitions = new ArrayList<>();
    private final Map<String, Control> customFieldInputMap = new HashMap<>();
    private Button btnGuardarRecepcion;"""

content = content.replace(field_decl_target, field_decl_replacement)

# Update formGrid construction in RecepcionesDialog
old_grid = """        // Fila 3: Zona Destino (col 0,1) | Observaciones (col 2,3)
        formGrid.add(lblZona, 0, 3);
        formGrid.add(cbZonaDestino, 1, 3);
        formGrid.add(lblObs, 2, 3);
        formGrid.add(txtObservaciones, 3, 3);"""

new_grid = """        chkEsContenedor = new CheckBox("Contenedor");
        chkEsContenedor.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        chkEsContenedor.setDisable(true);

        txtNumeroContenedor = new TextField();
        txtNumeroContenedor.setPromptText("N° Contenedor");
        txtNumeroContenedor.setEditable(false);
        txtNumeroContenedor.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9;");

        txtDigitoContenedor = new TextField();
        txtDigitoContenedor.setPromptText("DV");
        txtDigitoContenedor.setEditable(false);
        txtDigitoContenedor.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9;");
        txtDigitoContenedor.setPrefWidth(45);

        HBox containerBox = new HBox(6, chkEsContenedor, txtNumeroContenedor, txtDigitoContenedor);
        HBox.setHgrow(txtNumeroContenedor, Priority.ALWAYS);

        txtObservaciones.setPrefHeight(72);

        // Fila 3: Zona Destino (col 0,1) | Contenedor (col 2,3)
        formGrid.add(lblZona, 0, 3);
        formGrid.add(cbZonaDestino, 1, 3);
        formGrid.add(containerBox, 2, 3);
        GridPane.setColumnSpan(containerBox, 2);

        // Fila 4: Observaciones (col 0) | txtObservaciones (col 1, span 3)
        formGrid.add(lblObs, 0, 4);
        formGrid.add(txtObservaciones, 1, 4);
        GridPane.setColumnSpan(txtObservaciones, 3);"""

content = content.replace(old_grid, new_grid)

# Update itemsBox header to include btnConfigAtributos
old_items_header = """        Label lblItems = new Label("Detalle de Carga");
        lblItems.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");"""

new_items_header = """        Label lblItems = new Label("Detalle de Carga");
        lblItems.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        Region spacerItems = new Region();
        HBox.setHgrow(spacerItems, Priority.ALWAYS);

        FontAwesomeIconView iconCog = new FontAwesomeIconView(FontAwesomeIcon.COG);
        iconCog.setFill(Color.web("#0F3E6E"));
        Button btnConfigAtributos = new Button("Configurar Atributos", iconCog);
        btnConfigAtributos.setStyle("-fx-background-color: transparent; -fx-text-fill: #0F3E6E; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        btnConfigAtributos.setOnAction(e -> abrirConfiguracionAtributos());

        HBox itemsHeaderBox = new HBox(8, lblItems, spacerItems, btnConfigAtributos);
        itemsHeaderBox.setAlignment(Pos.CENTER_LEFT);

        customFieldsPane = new HBox(8);
        customFieldsPane.setAlignment(Pos.CENTER_LEFT);"""

content = content.replace(old_items_header, new_items_header)

old_items_add = """itemsBox.getChildren().addAll(lblItems, tblItems, itemModifyGrid);"""
new_items_add = """itemsBox.getChildren().addAll(itemsHeaderBox, customFieldsPane, tblItems, itemModifyGrid);"""
content = content.replace(old_items_add, new_items_add)

# Update cargarItemsDeAnuncio() to populate container fields
old_load_ann = """        txtNumeroBl.setText(a.getNumeroBl() != null ? a.getNumeroBl() : "");
        txtOrdenCompra.setText(a.getOrdenCompra() != null ? a.getOrdenCompra() : "");"""

new_load_ann = """        txtNumeroBl.setText(a.getNumeroBl() != null ? a.getNumeroBl() : "");
        txtOrdenCompra.setText(a.getOrdenCompra() != null ? a.getOrdenCompra() : "");
        boolean esCont = a.getEsContenedor() != null && a.getEsContenedor();
        chkEsContenedor.setSelected(esCont);
        txtNumeroContenedor.setText(esCont && a.getNumeroContenedor() != null ? a.getNumeroContenedor() : "");
        txtDigitoContenedor.setText(esCont && a.getDigitoContenedor() != null ? a.getDigitoContenedor() : "");
        cargarCamposPersonalizados();"""

content = content.replace(old_load_ann, new_load_ann)

old_clear_ann = """        if (a == null) {
            txtNumeroBl.clear();
            txtOrdenCompra.clear();
            return;
        }"""

new_clear_ann = """        if (a == null) {
            txtNumeroBl.clear();
            txtOrdenCompra.clear();
            chkEsContenedor.setSelected(false);
            txtNumeroContenedor.clear();
            txtDigitoContenedor.clear();
            return;
        }"""

content = content.replace(old_clear_ann, new_clear_ann)

# Add custom attributes methods to RecepcionesDialog
methods_to_add = """
    private void abrirConfiguracionAtributos() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        ReceptionAnnouncementModel ann = cbAnuncio.getValue();
        String supplierId = ann != null ? ann.getSupplierId() : null;

        ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, warehouseId, supplierId, this::cargarCamposPersonalizados);
        diag.showAndWait();
    }

    private void cargarCamposPersonalizados() {
        String companyId = com.logistics.packinglist.service.AuthService.getInstance().getCompanyId();
        String warehouseId = cbBodega.getValue() != null ? cbBodega.getValue().getId() : null;
        ReceptionAnnouncementModel ann = cbAnuncio.getValue();
        String supplierId = ann != null ? ann.getSupplierId() : null;

        new Thread(() -> {
            try {
                List<com.logistics.packinglist.model.ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, warehouseId, supplierId);
                List<com.logistics.packinglist.model.ProductFieldDefinitionModel> valid = new ArrayList<>();
                if (fields != null) {
                    for (com.logistics.packinglist.model.ProductFieldDefinitionModel f : fields) {
                        String t = f.getFieldType() != null ? f.getFieldType().toUpperCase() : "TEXT";
                        if (("TEXT".equals(t) || "NUMBER".equals(t) || "DATE".equals(t) || "BOOLEAN".equals(t)) && valid.size() < 4) {
                            valid.add(f);
                        }
                    }
                }
                javafx.application.Platform.runLater(() -> {
                    activeFieldDefinitions.clear();
                    activeFieldDefinitions.addAll(valid);
                    renderCustomFieldInputs();
                    reconstruirColumnasTabla();
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void renderCustomFieldInputs() {
        if (customFieldsPane == null) return;
        customFieldsPane.getChildren().clear();
        customFieldInputMap.clear();

        for (com.logistics.packinglist.model.ProductFieldDefinitionModel f : activeFieldDefinitions) {
            VBox box = new VBox(2);
            String labelText = (f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey());
            Label lbl = new Label(labelText + (f.isRequired() ? " *" : ""));
            lbl.setStyle(f.isRequired() ? "-fx-font-size: 11px; -fx-text-fill: #dc2626; -fx-font-weight: bold;" : "-fx-font-size: 11px; -fx-text-fill: #475569;");

            String t = f.getFieldType() != null ? f.getFieldType().toUpperCase() : "TEXT";
            Control ctrl;
            if ("BOOLEAN".equals(t)) {
                ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList("Sí", "No"));
                cb.setPromptText("Seleccione");
                cb.setStyle("-fx-background-radius: 4px;");
                ctrl = cb;
            } else if ("DATE".equals(t)) {
                DatePicker dp = new DatePicker();
                dp.setPromptText("dd/MM/yyyy");
                dp.setStyle("-fx-background-radius: 4px;");
                dp.setPrefWidth(125);
                ctrl = dp;
            } else {
                TextField txt = new TextField();
                txt.setPromptText(labelText);
                txt.setStyle("-fx-background-radius: 4px; -fx-border-color: #cbd5e1;");
                txt.setPrefWidth(110);
                ctrl = txt;
            }
            customFieldInputMap.put(f.getFieldKey(), ctrl);
            box.getChildren().addAll(lbl, ctrl);
            customFieldsPane.getChildren().add(box);
        }
    }

    private void reconstruirColumnasTabla() {
        if (tblItems == null) return;
        tblItems.getColumns().clear();

        TableColumn<ReceptionRow, String> colIProd = new TableColumn<>("Producto");
        colIProd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductoDesc()));
        colIProd.setPrefWidth(160);

        TableColumn<ReceptionRow, String> colIEst = new TableColumn<>("Anunciado");
        colIEst.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantAnunciada())));
        colIEst.setPrefWidth(80);

        TableColumn<ReceptionRow, String> colIRec = new TableColumn<>("Recibido");
        colIRec.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantRecibida())));
        colIRec.setPrefWidth(80);

        TableColumn<ReceptionRow, String> colILoc = new TableColumn<>("Ubicación");
        colILoc.setCellValueFactory(c -> {
            LocationModel loc = locationMap.get(c.getValue().getLocationId());
            return new SimpleStringProperty(loc != null ? loc.toString() : "");
        });
        colILoc.setPrefWidth(120);

        tblItems.getColumns().addAll(colIProd, colIEst, colIRec, colILoc);

        for (com.logistics.packinglist.model.ProductFieldDefinitionModel f : activeFieldDefinitions) {
            TableColumn<ReceptionRow, String> colAttr = new TableColumn<>(f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey());
            colAttr.setCellValueFactory(c -> {
                Map<String, String> m = c.getValue().getAtributosPersonalizados();
                return new SimpleStringProperty(m != null ? m.getOrDefault(f.getFieldKey(), "-") : "-");
            });
            colAttr.setPrefWidth(110);
            tblItems.getColumns().add(colAttr);
        }

        TableColumn<ReceptionRow, Void> colActions = new TableColumn<>("Acción");
        colActions.setPrefWidth(70);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnSplit;
            {
                FontAwesomeIconView iconCut = new FontAwesomeIconView(FontAwesomeIcon.SCISSORS);
                iconCut.setFill(Color.WHITE);
                btnSplit = new Button(null, iconCut);
                btnSplit.setStyle("-fx-background-color: #0d6efd; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-cursor: hand;");
                btnSplit.setTooltip(new Tooltip("Dividir Cantidad en Partes"));
                btnSplit.setOnAction(e -> {
                    ReceptionRow row = getTableView().getItems().get(getIndex());
                    abrirDialogoDivision(row);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnSplit);
                }
            }
        });

        tblItems.getColumns().add(colActions);
    }
"""

last_brace = content.rfind("}")
content = content[:last_brace] + methods_to_add + "\n}\n"

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content)

print("Updated RecepcionesDialog.java successfully!")
