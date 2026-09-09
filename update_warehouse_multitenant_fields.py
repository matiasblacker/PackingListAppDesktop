# 1. Update ConfiguracionCamposDialog.java
with open("src/main/java/com/logistics/packinglist/ui/ConfiguracionCamposDialog.java", "r") as f:
    content_cfg = f.read()

content_cfg = content_cfg.replace(
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, targetEntity, null, null);',
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, targetEntity, warehouseId, null);'
)

content_cfg = content_cfg.replace(
    '.warehouseId(null)',
    '.warehouseId(warehouseId == null || warehouseId.isEmpty() ? null : warehouseId)'
)

with open("src/main/java/com/logistics/packinglist/ui/ConfiguracionCamposDialog.java", "w") as f:
    f.write(content_cfg)

# 2. Update AnunciosDialog.java
with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content_an = f.read()

content_an = content_an.replace(
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_DOC", null, null);',
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_DOC", warehouseId, null);'
)
content_an = content_an.replace(
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_PROD", null, null);',
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_PROD", warehouseId, null);'
)

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content_an)

# 3. Update RecepcionesDialog.java
with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content_rec = f.read()

content_rec = content_rec.replace(
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "RECEPCION_DOC", null, null);',
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "RECEPCION_DOC", warehouseId, null);'
)
content_rec = content_rec.replace(
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_PROD", null, null);',
    'List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, "ANUNCIO_PROD", warehouseId, null);'
)

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content_rec)

print("Updated warehouse multitenancy scoping across ConfiguracionCamposDialog, AnunciosDialog, and RecepcionesDialog successfully!")
