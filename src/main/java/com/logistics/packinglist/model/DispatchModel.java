package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchModel {
    private String id;
    private String companyId;
    private String orderNoteId;
    private String guiaDespacho;
    private String fechaDespacho;
    private String transportista;
    private String patente;
    private String estado; // Siempre DESPACHADO según nuevas reglas
    private String factura;
    private String comentario;

    @Builder.Default
    private List<DispatchDetailModel> details = new ArrayList<>();

    @Override
    public String toString() {
        return "Guía: " + guiaDespacho + " (" + transportista + ")";
    }
}
