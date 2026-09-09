package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    private String id;
    private String email;
    private String nombre;
    private String apellido;
    private String role;
    private String companyId;
    private String warehouseId;
    private String estado;
    private Boolean conectado;

    @Override
    public String toString() {
        return nombre + " " + apellido + " (" + email + ")";
    }
}
