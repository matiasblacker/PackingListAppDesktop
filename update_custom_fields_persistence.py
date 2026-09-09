# 1. Update ConfiguracionCamposDialog.java
with open("src/main/java/com/logistics/packinglist/ui/ConfiguracionCamposDialog.java", "r") as f:
    content_cfg = f.read()

# Make cargarCampos fetch all fields for company & targetEntity (null, null for warehouse/supplier)
content_cfg = content_cfg.replace(
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, targetEntity, warehouseId, supplierId);',
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, targetEntity, null, null);'
)

# Store new fields with null warehouseId and supplierId so they are global across documents
content_cfg = content_cfg.replace(
    '.warehouseId(warehouseId == null || warehouseId.isEmpty() ? null : warehouseId)',
    '.warehouseId(null)'
)
content_cfg = content_cfg.replace(
    '.supplierId(supplierId == null || supplierId.isEmpty() ? null : supplierId)',
    '.supplierId(null)'
)

with open("src/main/java/com/logistics/packinglist/ui/ConfiguracionCamposDialog.java", "w") as f:
    f.write(content_cfg)

# 2. Update AnunciosDialog.java
with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content_an = f.read()

content_an = content_an.replace(
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_DOC", warehouseId, supplierId);',
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_DOC", null, null);'
)
content_an = content_an.replace(
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_PROD", warehouseId, supplierId);',
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_PROD", null, null);'
)

# Remove && valid.size() < 4 limit
content_an = content_an.replace(
    'if (f.isActive() && ("TEXT".equals(t) || "NUMBER".equals(t) || "DATE".equals(t) || "BOOLEAN".equals(t)) && valid.size() < 4) {',
    'if (f.isActive() && ("TEXT".equals(t) || "NUMBER".equals(t) || "DATE".equals(t) || "BOOLEAN".equals(t))) {'
)

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content_an)

# 3. Update RecepcionesDialog.java
with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content_rec = f.read()

content_rec = content_rec.replace(
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "RECEPCION_DOC", warehouseId, supplierId);',
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "RECEPCION_DOC", null, null);'
)
content_rec = content_rec.replace(
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_PROD", warehouseId, supplierId);',
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_PROD", null, null);'
)

# Remove && valid.size() < 4 limit
content_rec = content_rec.replace(
    'if (f.isActive() && ("TEXT".equals(t) || "NUMBER".equals(t) || "DATE".equals(t) || "BOOLEAN".equals(t)) && valid.size() < 4) {',
    'if (f.isActive() && ("TEXT".equals(t) || "NUMBER".equals(t) || "DATE".equals(t) || "BOOLEAN".equals(t))) {'
)

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content_rec)

print("Updated custom fields persistence across ConfiguracionCamposDialog, AnunciosDialog, and RecepcionesDialog successfully!")
