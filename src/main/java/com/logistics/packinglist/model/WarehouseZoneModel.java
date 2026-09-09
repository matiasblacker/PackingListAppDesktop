package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseZoneModel {
    private String id;
    private String companyId;
    private String warehouseId;
    private String parentZoneId;
    private String nombre;
    private String tipoZona;
    private String ambitoStock;


    @Override
    public String toString() {
        return nombre;
    }
}
