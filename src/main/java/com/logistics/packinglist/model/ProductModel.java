package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductModel {
    private String id;
    private String companyId;
    private String supplierId;
    
    private String sku;
    private String nombre;
    private String categoria;
    private String packing;
    private Double precio;
    private String moneda; // 'CLP' or 'USD'
    private String paisOrigen;
    
    private String imagenUrl;
    @Builder.Default
    private Double peso = 0.0;
    @Builder.Default
    private Double largo = 0.0;
    @Builder.Default
    private Double ancho = 0.0;
    @Builder.Default
    private Double alto = 0.0;
    @Builder.Default
    private Double volumen = 0.0;

    @Builder.Default
    private String unidadMedida = "UNIDADES";
    private String barcode;

    @Builder.Default
    private java.util.Map<String, String> atributosPersonalizados = new java.util.HashMap<>();

    // Compatibility delegates
    public Double getPrecioLista() {
        return precio != null ? precio : 0.0;
    }

    public void setPrecioLista(Double precioLista) {
        this.precio = precioLista;
    }

    @Override
    public String toString() {
        String skuVal = sku != null ? sku : "";
        String nomVal = nombre != null ? nombre : "";
        if (!skuVal.isEmpty() || !nomVal.isEmpty()) {
            return skuVal + " - " + nomVal;
        }
        return id != null ? id : "Producto Nuevo";
    }
}
