import re

with open("src/main/java/com/logistics/packinglist/ui/MainController.java", "r") as f:
    content = f.read()

# 1. Add private MenuButton menuCOM;
content = content.replace("private MenuButton menuWMS;", "private MenuButton menuWMS;\n    private MenuButton menuCOM;")

# 2. Initialize menuCOM
content = content.replace("menuWMS = null;", "menuWMS = null;\n        menuCOM = null;")

# 3. Create menuCOM
creation_code = """
        if (isWMSUser) {
            menuCOM = crearMenuDesplegable("Operaciones COM", de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.BRIEFCASE);
            menuWMS = crearMenuDesplegable("Operaciones WH", de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.CUBES);"""
            
content = content.replace("""        if (isWMSUser) {
            menuWMS = crearMenuDesplegable("Operaciones WH", FontAwesomeIcon.CUBES);""", creation_code)

# 4. Modify menuWMS.getItems().addAll and add menuCOM.getItems().addAll
# First, remove them from menuWMS.getItems().addAll
old_wms_add = """            menuWMS.getItems().addAll(
                    itemStock,
                    itemInventarioVisual,
                    itemNotas,
                    itemBackOrders,
                    itemDespachos,
                    itemAnuncios,
                    itemRecepciones,
                    itemPackingList,
                    itemTracking);"""

new_wms_add = """            menuCOM.getItems().addAll(
                    itemNotas,
                    itemBackOrders,
                    itemDespachos,
                    itemAnuncios);

            menuWMS.getItems().addAll(
                    itemStock,
                    itemInventarioVisual,
                    itemRecepciones,
                    itemPackingList,
                    itemTracking);"""

content = content.replace(old_wms_add, new_wms_add)

# 5. Add menuCOM to barra
old_barra_add = """        if (menuWMS != null) {
            barra.getChildren().add(menuWMS);
        }"""

new_barra_add = """        if (menuCOM != null) {
            barra.getChildren().add(menuCOM);
        }
        if (menuWMS != null) {
            barra.getChildren().add(menuWMS);
        }"""
        
content = content.replace(old_barra_add, new_barra_add)

# 6. Update visibility in actualizarPermisosUI (around line 2132)
old_vis1 = """        // Hide entire menuWMS if no WMS features are visible
        if (menuWMS != null) {
            boolean hasAnyWms = itemStock.isVisible() || itemNotas.isVisible() || itemBackOrders.isVisible() ||
                    itemDespachos.isVisible() || itemAnuncios.isVisible() || itemRecepciones.isVisible() ||
                    itemPackingList.isVisible() || itemTracking.isVisible() || itemInventarioVisual.isVisible();
            menuWMS.setVisible(hasAnyWms);
        }"""

new_vis1 = """        // Hide entire menuWMS if no WMS features are visible
        if (menuWMS != null) {
            boolean hasAnyWms = itemStock.isVisible() || itemRecepciones.isVisible() ||
                    itemPackingList.isVisible() || itemTracking.isVisible() || itemInventarioVisual.isVisible();
            menuWMS.setVisible(hasAnyWms);
        }
        if (menuCOM != null) {
            boolean hasAnyCom = itemNotas.isVisible() || itemBackOrders.isVisible() ||
                    itemDespachos.isVisible() || itemAnuncios.isVisible();
            menuCOM.setVisible(hasAnyCom);
        }"""

content = content.replace(old_vis1, new_vis1)

# 7. Update visibility in setMenusVisible (around line 2183)
old_vis2 = """        if (menuWMS != null)
            menuWMS.setVisible(visible);"""

new_vis2 = """        if (menuCOM != null)
            menuCOM.setVisible(visible);
        if (menuWMS != null)
            menuWMS.setVisible(visible);"""

content = content.replace(old_vis2, new_vis2)

with open("src/main/java/com/logistics/packinglist/ui/MainController.java", "w") as f:
    f.write(content)

print("MainController.java updated successfully.")
