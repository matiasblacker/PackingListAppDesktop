package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackOrderDetailModel {
    private String id;
    private String backOrderId;
    private String productId;
    private Integer cantidadOriginal;
    private Integer cantidadProcesada;
    private Integer cantidadPendiente;
}
