package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationModel {
    private String id;
    private String companyId;
    private String warehouseId;
    private String zoneId;
    private String tipoSoporte;
    private String codigoUbicacion;
    private String pasillo;
    private String estanteria;
    private String altura;
    private String posicion;

    @Override
    public String toString() {
        if (codigoUbicacion != null && !codigoUbicacion.trim().isEmpty()) {
            return codigoUbicacion;
        }
        return "Pasillo: " + pasillo + ", Estantería: " + estanteria + ", Nivel: " + altura + ", Pos: " + posicion;
    }

    public static final java.util.Comparator<LocationModel> NATURAL_ORDER_COMPARATOR = (l1, l2) -> {
        if (l1 == null && l2 == null) return 0;
        if (l1 == null) return -1;
        if (l2 == null) return 1;
        
        String s1 = l1.toString();
        String s2 = l2.toString();
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return -1;
        if (s2 == null) return 1;
        
        int i = 0, j = 0;
        while (i < s1.length() && j < s2.length()) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(j);
            if (Character.isDigit(c1) && Character.isDigit(c2)) {
                StringBuilder num1 = new StringBuilder();
                while (i < s1.length() && Character.isDigit(s1.charAt(i))) {
                    num1.append(s1.charAt(i++));
                }
                StringBuilder num2 = new StringBuilder();
                while (j < s2.length() && Character.isDigit(s2.charAt(j))) {
                    num2.append(s2.charAt(j++));
                }
                int val1 = 0;
                try {
                    val1 = Integer.parseInt(num1.toString());
                } catch (NumberFormatException ignored) {}
                int val2 = 0;
                try {
                    val2 = Integer.parseInt(num2.toString());
                } catch (NumberFormatException ignored) {}
                if (val1 != val2) {
                    return val1 - val2;
                }
            } else {
                if (c1 != c2) {
                    return c1 - c2;
                }
                i++;
                j++;
            }
        }
        return s1.length() - s2.length();
    };
}
