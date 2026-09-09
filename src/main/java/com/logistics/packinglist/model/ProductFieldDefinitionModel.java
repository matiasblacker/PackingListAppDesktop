package com.logistics.packinglist.model;

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
