# 1. Update AnunciosDialog.java
with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content_a = f.read()

target_a1 = "ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, warehouseId, supplierId, this::cargarCamposPersonalizados);"
replacement_a1 = "ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, \"ANUNCIO\", warehouseId, supplierId, this::cargarCamposPersonalizados);"

target_a2 = "List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, warehouseId, supplierId);"
replacement_a2 = "List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, \"ANUNCIO\", warehouseId, supplierId);"

target_a3 = """if (("TEXT".equals(t) || "NUMBER".equals(t) || "DATE".equals(t) || "BOOLEAN".equals(t)) && valid.size() < 4) {"""
replacement_a3 = """if (f.isActive() && ("TEXT".equals(t) || "NUMBER".equals(t) || "DATE".equals(t) || "BOOLEAN".equals(t)) && valid.size() < 4) {"""

content_a = content_a.replace(target_a1, replacement_a1).replace(target_a2, replacement_a2).replace(target_a3, replacement_a3)
with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content_a)

# 2. Update RecepcionesDialog.java
with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content_r = f.read()

target_r1 = "ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, warehouseId, supplierId, this::cargarCamposPersonalizados);"
replacement_r1 = "ConfiguracionCamposDialog diag = new ConfiguracionCamposDialog(this, companyId, \"RECEPCION\", warehouseId, supplierId, this::cargarCamposPersonalizados);"

target_r2 = "List<com.logistics.packinglist.model.ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, warehouseId, supplierId);"
replacement_r2 = "List<com.logistics.packinglist.model.ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, \"RECEPCION\", warehouseId, supplierId);"

target_r3 = "List<ProductFieldDefinitionModel> fieldDefs = service.obtenerDefinicionesCampos(companyId, warehouseId, supplierId);"
replacement_r3 = "List<ProductFieldDefinitionModel> fieldDefs = service.obtenerDefinicionesCampos(companyId, \"RECEPCION\", warehouseId, supplierId);"

content_r = content_r.replace(target_r1, replacement_r1).replace(target_r2, replacement_r2).replace(target_r3, replacement_r3).replace(target_a3, replacement_a3)
with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content_r)

print("Updated AnunciosDialog.java and RecepcionesDialog.java scopes and active filters successfully!")
