package com.logistics.packinglist.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un ítem individual dentro de un bulto/pallet.
 */
@Data
@NoArgsConstructor
public class PackingItem {
    private String partNumber;
    private String descripcion;
    private int    cantidad;

    public PackingItem(String partNumber, String descripcion, int cantidad) {
        this.partNumber  = partNumber != null ? partNumber.trim() : "";
        this.descripcion = limpiarDescripcion(descripcion);
        this.cantidad    = cantidad;
    }

    private String limpiarDescripcion(String raw) {
        if (raw == null) return "";
        return raw.replace("\n", " ")
                  .replaceAll("\\(PENDIENTE DESPACHO\\)", "")
                  .replaceAll("\\s{2,}", " ")
                  .trim();
    }
}
