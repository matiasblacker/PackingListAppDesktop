package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderNoteDetailModel {
    private String id;
    private String orderNoteId;
    private String productId;
    private Integer cantidadPedida;
    private Integer cantidadDespachada;
    private Double precioUnitario;

    @Builder.Default
    private Integer cantidadReservada = 0;
    @Builder.Default
    private Integer cantidadBackOrder = 0;

    @Builder.Default
    private java.util.Map<String, String> atributosPersonalizados = new java.util.HashMap<>();
}
