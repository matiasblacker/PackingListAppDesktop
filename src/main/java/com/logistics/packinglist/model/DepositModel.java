package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositModel {
    private String id;
    private String companyId;
    private String companyNombre;
    private String nombre;
    private String direccion;
    private String region;
    private String comuna;
    private String createdAt;
    private String updatedAt;

    @Override
    public String toString() {
        return nombre != null ? nombre : "";
    }
}
