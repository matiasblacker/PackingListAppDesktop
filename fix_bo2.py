import re

with open("src/main/java/com/logistics/packinglist/ui/BackOrdersDialog.java", "r") as f:
    content = f.read()

# 1. Update prefHeight binding for tablaDetalles
old_bind_det = """tablaDetalles.prefHeightProperty().bind(javafx.beans.binding.Bindings.size(tablaDetalles.getItems()).multiply(tablaDetalles.getFixedCellSize()).add(30));"""
new_bind_det = """tablaDetalles.prefHeightProperty().bind(javafx.beans.binding.Bindings.max(1, javafx.beans.binding.Bindings.size(tablaDetalles.getItems())).multiply(tablaDetalles.getFixedCellSize()).add(30));"""
content = content.replace(old_bind_det, new_bind_det)

# 2. Update prefHeight binding for tablaHistorial
old_bind_hist = """tablaHistorial.prefHeightProperty().bind(javafx.beans.binding.Bindings.size(tablaHistorial.getItems()).multiply(tablaHistorial.getFixedCellSize()).add(30));"""
new_bind_hist = """tablaHistorial.prefHeightProperty().bind(javafx.beans.binding.Bindings.max(1, javafx.beans.binding.Bindings.size(tablaHistorial.getItems())).multiply(tablaHistorial.getFixedCellSize()).add(30));"""
content = content.replace(old_bind_hist, new_bind_hist)

# 3. Update SplitPane and ScrollPane styles
old_scroll_style = """scrollRight.setStyle("-fx-background-color: transparent; -fx-background: #f8fafc;");"""
new_scroll_style = """scrollRight.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");"""
content = content.replace(old_scroll_style, new_scroll_style)

old_split_style = """splitMain.setStyle("-fx-background-color: transparent;");"""
new_split_style = """splitMain.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");"""
content = content.replace(old_split_style, new_split_style)

with open("src/main/java/com/logistics/packinglist/ui/BackOrdersDialog.java", "w") as f:
    f.write(content)

print("Applied fixes")
