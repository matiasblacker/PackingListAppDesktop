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
public class ReceptionAnnouncementModel {
    private String id;
    private String companyId;
    private String warehouseId;
    private String folio;
    private String supplierId;
    private String fecha;
    private String estado;
    private String observaciones;
    private String numeroBl;
    private String ordenCompra;
    private String receptionFolio;
    private String zonaDestino;
    private Boolean esContenedor;
    private String numeroContenedor;
    private String digitoContenedor;
    @Builder.Default
    private java.util.Map<String, String> atributosPersonalizados = new java.util.HashMap<>();

    @Builder.Default
    private List<AnnouncementDetailModel> details = new ArrayList<>();

    @Override
    public String toString() {
        return folio + " - " + estado;
    }
}
