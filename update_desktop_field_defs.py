import re

# 1. Update ProductFieldDefinitionModel.java
model_code = """package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFieldDefinitionModel {
    private String id;
    private String companyId;
    private String warehouseId;
    private String supplierId;
    private String fieldKey;
    private String fieldLabel;
    private String fieldType; // TEXT, NUMBER, DATE, BOOLEAN
    private boolean required;

    @Builder.Default
    private String targetEntity = "ANUNCIO";

    @Builder.Default
    private boolean active = true;
}
"""
with open("src/main/java/com/logistics/packinglist/model/ProductFieldDefinitionModel.java", "w") as f:
    f.write(model_code)

# 2. Update ProductFieldApiService.java
with open("src/main/java/com/logistics/packinglist/service/ProductFieldApiService.java", "r") as f:
    content = f.read()

old_api_method = """    public List<ProductFieldDefinitionModel> obtenerDefinicionesCampos(String companyId, String warehouseId, String supplierId) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(baseUrl).append("/api/v1/products/fields-definition?companyId=").append(companyId);
        if (warehouseId != null && !warehouseId.isEmpty()) {
            urlBuilder.append("&warehouseId=").append(warehouseId);
        }
        if (supplierId != null && !supplierId.isEmpty()) {
            urlBuilder.append("&supplierId=").append(supplierId);
        }"""

new_api_method = """    public List<ProductFieldDefinitionModel> obtenerDefinicionesCampos(String companyId, String warehouseId, String supplierId) throws Exception {
        return obtenerDefinicionesCampos(companyId, null, warehouseId, supplierId);
    }

    public List<ProductFieldDefinitionModel> obtenerDefinicionesCampos(String companyId, String targetEntity, String warehouseId, String supplierId) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(baseUrl).append("/api/v1/products/fields-definition?companyId=").append(companyId);
        if (targetEntity != null && !targetEntity.isEmpty()) {
            urlBuilder.append("&targetEntity=").append(targetEntity);
        }
        if (warehouseId != null && !warehouseId.isEmpty()) {
            urlBuilder.append("&warehouseId=").append(warehouseId);
        }
        if (supplierId != null && !supplierId.isEmpty()) {
            urlBuilder.append("&supplierId=").append(supplierId);
        }"""

content = content.replace(old_api_method, new_api_method)
with open("src/main/java/com/logistics/packinglist/service/ProductFieldApiService.java", "w") as f:
    f.write(content)

# 3. Update MantenimientoService.java
with open("src/main/java/com/logistics/packinglist/service/MantenimientoService.java", "r") as f:
    content = f.read()

old_mantenimiento = """    public List<ProductFieldDefinitionModel> obtenerDefinicionesCampos(String companyId, String warehouseId, String supplierId) throws Exception {
        return productFieldApiService.obtenerDefinicionesCampos(companyId, warehouseId, supplierId);
    }"""

new_mantenimiento = """    public List<ProductFieldDefinitionModel> obtenerDefinicionesCampos(String companyId, String warehouseId, String supplierId) throws Exception {
        return productFieldApiService.obtenerDefinicionesCampos(companyId, null, warehouseId, supplierId);
    }

    public List<ProductFieldDefinitionModel> obtenerDefinicionesCampos(String companyId, String targetEntity, String warehouseId, String supplierId) throws Exception {
        return productFieldApiService.obtenerDefinicionesCampos(companyId, targetEntity, warehouseId, supplierId);
    }"""

content = content.replace(old_mantenimiento, new_mantenimiento)
with open("src/main/java/com/logistics/packinglist/service/MantenimientoService.java", "w") as f:
    f.write(content)

print("Updated ProductFieldDefinitionModel, ProductFieldApiService and MantenimientoService successfully!")
