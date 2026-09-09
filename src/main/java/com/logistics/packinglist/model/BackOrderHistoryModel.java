package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackOrderHistoryModel {
    private String id;
    private String backOrderId;
    private String createdNpId;
    private String userId;
    private String fechaHora;
    private String detallesProcesados;
}
