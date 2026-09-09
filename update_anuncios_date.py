import re

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content = f.read()

# Update cargarCamposPersonalizados filter
content = content.replace('"TEXT".equals(t) || "NUMBER".equals(t) || "BOOLEAN".equals(t)', '"TEXT".equals(t) || "NUMBER".equals(t) || "DATE".equals(t) || "BOOLEAN".equals(t)')

# Update renderCustomFieldInputs to add DatePicker support
old_render = """            if ("BOOLEAN".equals(t)) {
                ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList("Sí", "No"));
                cb.setPromptText("Seleccione");
                cb.setStyle("-fx-background-radius: 4px;");
                ctrl = cb;
            } else {
                TextField txt = new TextField();
                txt.setPromptText(labelText);
                txt.setStyle("-fx-background-radius: 4px; -fx-border-color: #cbd5e1;");
                txt.setPrefWidth(110);
                ctrl = txt;
            }"""

new_render = """            if ("BOOLEAN".equals(t)) {
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
            }"""

content = content.replace(old_render, new_render)

# Update agregarFilaDetalle value reading for DatePicker
old_read_val = """            if (ctrl instanceof TextField) {
                val = ((TextField) ctrl).getText().trim();
            } else if (ctrl instanceof ComboBox) {
                Object obj = ((ComboBox<?>) ctrl).getValue();
                val = obj != null ? obj.toString() : "";
            }"""

new_read_val = """            if (ctrl instanceof TextField) {
                val = ((TextField) ctrl).getText().trim();
            } else if (ctrl instanceof ComboBox) {
                Object obj = ((ComboBox<?>) ctrl).getValue();
                val = obj != null ? obj.toString() : "";
            } else if (ctrl instanceof DatePicker) {
                java.time.LocalDate ld = ((DatePicker) ctrl).getValue();
                val = ld != null ? ld.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            }"""

content = content.replace(old_read_val, new_read_val)

# Update limpiarCamposPersonalizados
old_clear = """            if (ctrl instanceof TextField) {
                ((TextField) ctrl).clear();
            } else if (ctrl instanceof ComboBox) {
                ((ComboBox<?>) ctrl).getSelectionModel().clearSelection();
            }"""

new_clear = """            if (ctrl instanceof TextField) {
                ((TextField) ctrl).clear();
            } else if (ctrl instanceof ComboBox) {
                ((ComboBox<?>) ctrl).getSelectionModel().clearSelection();
            } else if (ctrl instanceof DatePicker) {
                ((DatePicker) ctrl).setValue(null);
            }"""

content = content.replace(old_clear, new_clear)

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content)

print("Updated AnunciosDialog with DATE support successfully")
