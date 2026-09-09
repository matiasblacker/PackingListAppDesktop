import re

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content = f.read()

# Add editingDetailRow and iconAddProduct fields
fields_to_add = """    private DetailRow editingDetailRow = null;
    private FontAwesomeIconView iconAddProduct;"""

content = content.replace("private FlowPane customFieldsPane;", "private FlowPane customFieldsPane;\n" + fields_to_add)

# Replace iconAdd creation in UI setup
old_icon_add = """        FontAwesomeIconView iconAdd = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        iconAdd.setFill(Color.WHITE);
        Button btnAddProduct = new Button(null, iconAdd);"""

new_icon_add = """        iconAddProduct = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        iconAddProduct.setFill(Color.WHITE);
        Button btnAddProduct = new Button(null, iconAddProduct);"""

content = content.replace(old_icon_add, new_icon_add)

# Add listener to tblDetails after reconstruirColumnasTabla()
old_reconstruir_call = """        reconstruirColumnasTabla();

        itemsBox.getChildren().addAll(itemsHeaderBox, inputsWrapper, tblDetails);"""

new_reconstruir_call = """        reconstruirColumnasTabla();

        tblDetails.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && !isPopulatingForm) {
                cargarFilaParaEdicion(newSel);
            }
        });

        itemsBox.getChildren().addAll(itemsHeaderBox, inputsWrapper, tblDetails);"""

content = content.replace(old_reconstruir_call, new_reconstruir_call)

# Add listener to cbProducto to populate defaults when creating line
old_cb_prod_listener = """        cbProducto.setFilterPredicate((p, text) -> {"""

new_cb_prod_listener = """        cbProducto.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (editingDetailRow == null && newVal != null) {
                Map<String, String> defaultAttrs = newVal.getAtributosPersonalizados();
                for (ProductFieldDefinitionModel f : activeFieldDefinitions) {
                    Control ctrl = customFieldInputMap.get(f.getFieldKey());
                    if (ctrl == null) continue;
                    String val = defaultAttrs != null ? defaultAttrs.get(f.getFieldKey()) : null;

                    if (ctrl instanceof TextField) {
                        ((TextField) ctrl).setText(val != null ? val : "");
                    } else if (ctrl instanceof ComboBox) {
                        ((ComboBox<String>) ctrl).setValue(val != null ? val : null);
                    } else if (ctrl instanceof DatePicker) {
                        if (val != null && !val.trim().isEmpty()) {
                            try {
                                java.time.LocalDate ld;
                                if (val.contains("/")) {
                                    ld = java.time.LocalDate.parse(val, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                } else {
                                    ld = java.time.LocalDate.parse(val);
                                }
                                ((DatePicker) ctrl).setValue(ld);
                            } catch (Exception e) {
                                ((DatePicker) ctrl).setValue(null);
                            }
                        } else {
                            ((DatePicker) ctrl).setValue(null);
                        }
                    }
                }
            }
        });

        cbProducto.setFilterPredicate((p, text) -> {"""

content = content.replace(old_cb_prod_listener, new_cb_prod_listener)

# Add cargarFilaParaEdicion and limpiarInputsDetalle methods, and update agregarFilaDetalle
helper_methods = """
    private void cargarFilaParaEdicion(DetailRow row) {
        editingDetailRow = row;
        ProductModel prod = productMap.get(row.getProductId());
        if (prod != null) {
            cbProducto.selectItem(prod);
        }
        txtCantidad.setText(String.valueOf(row.getCantidad()));

        Map<String, String> attrs = row.getAtributosPersonalizados();
        for (ProductFieldDefinitionModel f : activeFieldDefinitions) {
            Control ctrl = customFieldInputMap.get(f.getFieldKey());
            if (ctrl == null) continue;
            String val = attrs != null ? attrs.get(f.getFieldKey()) : null;

            if (ctrl instanceof TextField) {
                ((TextField) ctrl).setText(val != null ? val : "");
            } else if (ctrl instanceof ComboBox) {
                ((ComboBox<String>) ctrl).setValue(val != null ? val : null);
            } else if (ctrl instanceof DatePicker) {
                if (val != null && !val.trim().isEmpty()) {
                    try {
                        java.time.LocalDate ld;
                        if (val.contains("/")) {
                            ld = java.time.LocalDate.parse(val, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        } else {
                            ld = java.time.LocalDate.parse(val);
                        }
                        ((DatePicker) ctrl).setValue(ld);
                    } catch (Exception e) {
                        ((DatePicker) ctrl).setValue(null);
                    }
                } else {
                    ((DatePicker) ctrl).setValue(null);
                }
            }
        }
        if (iconAddProduct != null) {
            iconAddProduct.setIcon(FontAwesomeIcon.SAVE);
        }
    }

    private void limpiarInputsDetalle() {
        editingDetailRow = null;
        if (tblDetails != null) {
            tblDetails.getSelectionModel().clearSelection();
        }
        cbProducto.selectItem(null);
        txtCantidad.clear();
        limpiarCamposPersonalizados();
        if (iconAddProduct != null) {
            iconAddProduct.setIcon(FontAwesomeIcon.PLUS);
        }
    }
"""

content = content.replace("private void limpiarCamposPersonalizados() {", helper_methods + "\n    private void limpiarCamposPersonalizados() {")

# Replace agregarFilaDetalle method
old_agregar_fila = """    private void agregarFilaDetalle() {
        ProductModel prod = cbProducto.getValue();
        String cantStr = txtCantidad.getText().trim();

        if (prod == null || cantStr.isEmpty()) {
            mostrarWarning("Validación", "Debe seleccionar un producto y especificar una cantidad.");
            return;
        }

        int cant;
        try {
            cant = Integer.parseInt(cantStr);
            if (cant <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarWarning("Validación", "La cantidad debe ser un número entero mayor a 0.");
            return;
        }

        Map<String, String> newAttrs = new HashMap<>();
        for (ProductFieldDefinitionModel f : activeFieldDefinitions) {
            Control ctrl = customFieldInputMap.get(f.getFieldKey());
            String val = "";
            if (ctrl instanceof TextField) {
                val = ((TextField) ctrl).getText().trim();
            } else if (ctrl instanceof ComboBox) {
                Object obj = ((ComboBox<?>) ctrl).getValue();
                val = obj != null ? obj.toString() : "";
            } else if (ctrl instanceof DatePicker) {
                java.time.LocalDate ld = ((DatePicker) ctrl).getValue();
                val = ld != null ? ld.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            }
            if (f.isRequired() && val.isEmpty()) {
                mostrarWarning("Validación", "El campo '" + (f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey()) + "' es requerido.");
                return;
            }
            if (!val.isEmpty()) {
                newAttrs.put(f.getFieldKey(), val);
            }
        }

        // Verificar si existe en la lista con el MISMO SKU y MISMOS ATRIBUTOS para consolidar
        for (DetailRow row : detailRows) {
            if (row.getProductId().equals(prod.getId()) && row.getAtributosPersonalizados().equals(newAttrs)) {
                row.setCantidad(row.getCantidad() + cant);
                tblDetails.refresh();
                cbProducto.getSelectionModel().clearSelection();
                txtCantidad.clear();
                limpiarCamposPersonalizados();
                return;
            }
        }

        detailRows.add(new DetailRow(prod.getId(), prod.toString(), cant, newAttrs));
        cbProducto.getSelectionModel().clearSelection();
        txtCantidad.clear();
        limpiarCamposPersonalizados();
    }"""

new_agregar_fila = """    private void agregarFilaDetalle() {
        ProductModel prod = cbProducto.getValue();
        String cantStr = txtCantidad.getText().trim();

        if (prod == null || cantStr.isEmpty()) {
            mostrarWarning("Validación", "Debe seleccionar un producto y especificar una cantidad.");
            return;
        }

        int cant;
        try {
            cant = Integer.parseInt(cantStr);
            if (cant <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarWarning("Validación", "La cantidad debe ser un número entero mayor a 0.");
            return;
        }

        Map<String, String> newAttrs = new HashMap<>();
        for (ProductFieldDefinitionModel f : activeFieldDefinitions) {
            Control ctrl = customFieldInputMap.get(f.getFieldKey());
            String val = "";
            if (ctrl instanceof TextField) {
                val = ((TextField) ctrl).getText().trim();
            } else if (ctrl instanceof ComboBox) {
                Object obj = ((ComboBox<?>) ctrl).getValue();
                val = obj != null ? obj.toString() : "";
            } else if (ctrl instanceof DatePicker) {
                java.time.LocalDate ld = ((DatePicker) ctrl).getValue();
                val = ld != null ? ld.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            }
            if (f.isRequired() && val.isEmpty()) {
                mostrarWarning("Validación", "El campo '" + (f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey()) + "' es requerido.");
                return;
            }
            if (!val.isEmpty()) {
                newAttrs.put(f.getFieldKey(), val);
            }
        }

        if (editingDetailRow != null) {
            editingDetailRow.setCantidad(cant);
            editingDetailRow.setAtributosPersonalizados(newAttrs);
            tblDetails.refresh();
            limpiarInputsDetalle();
            return;
        }

        // Verificar si existe en la lista con el MISMO SKU y MISMOS ATRIBUTOS para consolidar
        for (DetailRow row : detailRows) {
            if (row.getProductId().equals(prod.getId()) && row.getAtributosPersonalizados().equals(newAttrs)) {
                row.setCantidad(row.getCantidad() + cant);
                tblDetails.refresh();
                limpiarInputsDetalle();
                return;
            }
        }

        detailRows.add(new DetailRow(prod.getId(), prod.toString(), cant, newAttrs));
        limpiarInputsDetalle();
    }"""

content = content.replace(old_agregar_fila, new_agregar_fila)

# Update btnRemove action in reconstruirColumnasTabla to clear editing state if removed row was being edited
old_btn_remove = """                btnRemove.setOnAction(e -> {
                    DetailRow row = getTableView().getItems().get(getIndex());
                    detailRows.remove(row);
                });"""

new_btn_remove = """                btnRemove.setOnAction(e -> {
                    DetailRow row = getTableView().getItems().get(getIndex());
                    if (editingDetailRow == row) {
                        limpiarInputsDetalle();
                    }
                    detailRows.remove(row);
                });"""

content = content.replace(old_btn_remove, new_btn_remove)

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content)

print("Successfully updated AnunciosDialog with line editing & auto-population")
