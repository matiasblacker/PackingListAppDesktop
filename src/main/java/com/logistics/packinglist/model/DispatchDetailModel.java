package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchDetailModel {
    private String id;
    private String dispatchId;
    private String productId;
    private Integer cantidadDespachada;
}
