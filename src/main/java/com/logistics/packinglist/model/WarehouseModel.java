package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseModel {
    private String id;
    private String companyId;
    private String nombre;
    private String codigo;
    private String direccion;
    private String region;
    private String comuna;
    private String depositId;
    private String depositNombre;

    @Override
    public String toString() {
        return nombre + " (" + codigo + ")";
    }
}
