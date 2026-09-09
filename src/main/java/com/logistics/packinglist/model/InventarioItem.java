package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InventarioItem {
    private String partNumber;
    private String descripcion;
    private String ubicacion = "";

    public InventarioItem(String partNumber, String descripcion) {
        this.partNumber = partNumber;
        this.descripcion = descripcion;
        this.ubicacion = "";
    }

    public InventarioItem(String partNumber, String descripcion, String ubicacion) {
        this.partNumber = partNumber;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion != null ? ubicacion : "";
    }

    @Override
    public String toString() {
        return partNumber + " - " + descripcion;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventarioItem that = (InventarioItem) o;
        return partNumber != null && partNumber.equals(that.partNumber);
    }

    @Override
    public int hashCode() {
        return partNumber != null ? partNumber.hashCode() : 0;
    }
}
