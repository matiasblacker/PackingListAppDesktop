package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionDetailModel {
    private String id;
    private String receptionId;
    private String productId;
    private Integer cantidad;
    private String locationId;
    private String announcementDetailId;

    @Builder.Default
    private Map<String, String> atributosPersonalizados = new HashMap<>();
}
