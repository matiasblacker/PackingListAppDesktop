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
public class OrderNoteModel {
    private String id;
    private String companyId;
    private String depositId;
    private String warehouseId;
    private String folio;
    private String customerId;
    private String fecha;
    private String estado; // PENDIENTE, CONFIRMADA, COMPLETADA, ANULADA
    private String estadoPicking; // NO_INICIADO, EN_PROCESO, PREPARADO
    private String estadoTracking; // REGISTRADO, EN_PREPARACION, PREPARADO, EN_TRANSITO, ENTREGADO, ANULADO
    private String trackingNumber;
    private String motivoAnulacion;
    
    @Builder.Default
    private Integer version = 1;
    private Integer pickingVersion;
    @Builder.Default
    private Boolean npModificada = false;

    private String creadorNombre;
    private String editorNombre;

    @Builder.Default
    private List<OrderNoteDetailModel> details = new ArrayList<>();

    @Builder.Default
    private java.util.Map<String, String> atributosPersonalizados = new java.util.HashMap<>();

    @Override
    public String toString() {
        return "Folio: " + folio + " [" + estado + "]";
    }
}
