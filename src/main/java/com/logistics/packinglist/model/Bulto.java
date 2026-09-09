package com.logistics.packinglist.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un bulto o pallet del packing list.
 */
@Data
@NoArgsConstructor
public class Bulto {

    private int    numero;
    private String tipo;
    private double pesoTotal;
    private double pesoNeto;
    private double largo;
    private double ancho;
    private double alto;
    private String alias = "";
    private String observaciones;

    private final List<PackingItem> items = new ArrayList<>();

    public Bulto(int numero, String tipo, double pesoTotal, double pesoNeto,
                 double largo, double ancho, double alto) {
        this.numero    = numero;
        this.tipo      = tipo != null ? tipo.trim() : "BULTO";
        this.pesoTotal = pesoTotal;
        this.pesoNeto  = pesoNeto;
        this.largo     = largo;
        this.ancho     = ancho;
        this.alto      = alto;
    }

    public void addItem(PackingItem item) { items.add(item); }

    public String getEtiqueta() {
        if (tipo == null) return "BULTO " + numero;
        String t = tipo.toUpperCase().trim();
        String[] partes = t.split("\\s+");
        String palabra = "";
        for (String p : partes) {
            if (!p.matches("\\d+")) {
                palabra = p;
                break;
            }
        }
        if (palabra.isEmpty()) palabra = "BULTO";
        return palabra + " " + numero;
    }

    public String getNombreArchivo() {
        return getEtiqueta().replace(" ", "_");
    }

    public int getTotalUnidades() {
        return items.stream().mapToInt(PackingItem::getCantidad).sum();
    }

    public double getVolumenM3() {
        if (largo == 0 || ancho == 0 || alto == 0) return 0;
        return (largo / 100.0) * (ancho / 100.0) * (alto / 100.0);
    }
}
