package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesTargetModel {
    private String monthKey; // e.g. "2026-08"
    private int year;        // e.g. 2026
    private int month;       // 1 to 12
    private String monthName; // e.g. "Agosto"
    private double targetUsd;
    private double targetClp;
    private double exchangeRate; // CLP per USD
    private String updatedAt;
}
