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
public class ReceptionModel {
    private String id;
    private String companyId;
    private String warehouseId;
    private String announcementId;
    private String folio;
    private String fecha;
    private String estado;
    private String observaciones;
    private String tipoDocumento;
    private String numeroDocumento;
    private String zonaDestino;
    @Builder.Default
    private java.util.Map<String, String> atributosPersonalizados = new java.util.HashMap<>();

    @Builder.Default
    private List<ReceptionDetailModel> details = new ArrayList<>();
}
