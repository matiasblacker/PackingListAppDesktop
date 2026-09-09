import re

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

# Add missing imports
imports = """import java.io.File;
import com.logistics.packinglist.model.SupplierModel;"""

content = content.replace("import com.logistics.packinglist.model.ProductFieldDefinitionModel;", "import com.logistics.packinglist.model.ProductFieldDefinitionModel;\n" + imports)

# Add supplierMap field
content = content.replace("private final Map<String, WarehouseModel> warehouseMap = new HashMap<>();", "private final Map<String, SupplierModel> supplierMap = new HashMap<>();\n    private final Map<String, WarehouseModel> warehouseMap = new HashMap<>();")

# Populate supplierMap in cargarDatos
old_cargar_datos = "List<WarehouseModel> warehouses = service.obtenerBodegas();"
new_cargar_datos = """List<SupplierModel> suppliers = service.obtenerProveedores();
                List<WarehouseModel> warehouses = service.obtenerBodegas();"""

content = content.replace(old_cargar_datos, new_cargar_datos)

old_plat_run = "warehouseMap.clear();"
new_plat_run = """supplierMap.clear();
                    for (SupplierModel s : suppliers) {
                        supplierMap.put(s.getId(), s);
                    }
                    warehouseMap.clear();"""

content = content.replace(old_plat_run, new_plat_run)

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content)

print("Fixed imports and supplierMap in RecepcionesDialog")
