package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyModel {
    private String id;
    private String razonSocial;
    private String rut;
    private String giro;
    private String direccion;
    private String comuna;
    private String region;
    private String telefono;
    private String email;
    private String logoUrl;
    private String logo;
    private String tipoMoneda;
    private Double valorCambio;
    private String usuarioCambio;
    private String fechaCambio;
    private String permisos;

    @Override
    public String toString() {
        return razonSocial + " (" + rut + ")";
    }
}
