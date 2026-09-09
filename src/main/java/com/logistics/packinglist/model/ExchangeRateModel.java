package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateModel {
    private String id;
    private String companyId;
    private String warehouseId;

    @Builder.Default
    private double exchangeRate = 800.0;

    @Builder.Default
    private String currencyFrom = "USD";

    @Builder.Default
    private String currencyTo = "CLP";

    private String updatedAt;
    private String updatedBy;
}
