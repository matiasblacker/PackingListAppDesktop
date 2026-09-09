package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerModel {
    private String id;
    private String companyId;
    private String rut;
    private String razonSocial;
    private String direccion;
    private String region;
    private String comuna;
    private String telefono;
    private String email;
    private String atencion;
    private Boolean extranjero;
    private String pais;

    @Override
    public String toString() {
        return razonSocial + " (" + rut + ")";
    }
}
