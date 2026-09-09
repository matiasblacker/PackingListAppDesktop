package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockModel {
    private String id;
    private String companyId;
    private String productId;
    private String locationId;
    private Integer cantidad;
    private String tipoStock;
}
