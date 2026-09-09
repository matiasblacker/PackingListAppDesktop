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
public class BackOrderModel {
    private String id;
    private String companyId;
    private String orderNoteId;
    private String customerId;
    private String folio;
    private String fecha;
    private String estado; // PENDIENTE, PARCIAL, PROCESADO, ANULADO
    private String motivoAnulacion;

    @Builder.Default
    private List<BackOrderDetailModel> details = new ArrayList<>();
}
