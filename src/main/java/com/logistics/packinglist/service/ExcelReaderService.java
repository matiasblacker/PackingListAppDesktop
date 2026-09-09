package com.logistics.packinglist.service;

import com.logistics.packinglist.model.Bulto;
import com.logistics.packinglist.model.PackingItem;
import com.logistics.packinglist.model.PackingList;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Lee el Excel de logística con el formato Mitracont.
 *
 * Lógica de agrupación:
 *   - Una fila tiene "Tipo" (ej: "1 PALLET") → es la cabecera de un bulto nuevo.
 *     El primer ítem de ese bulto es la misma fila.
 *   - Las filas siguientes sin "Tipo" se agregan al bulto actual.
 *   - Filas de totales (Part Number = "TOTAL PIEZAS", etc.) se ignoran.
 *
 * Columnas esperadas (fila 1):
 *   A: Part number | B: Descripcion | C: Unidades totales | D: Tipo
 *   E: Peso Total [Kg] | F: Peso Neto [Kg] | G: Largo | H: Ancho | I: Alto
 */
public class ExcelReaderService {

    private static final int COL_PART   = 0;
    private static final int COL_DESC   = 1;
    private static final int COL_CANT   = 2;
    private static final int COL_TIPO   = 3;
    private static final int COL_PTOTAL = 4;
    private static final int COL_PNETO  = 5;
    private static final int COL_LARGO  = 6;
    private static final int COL_ANCHO  = 7;
    private static final int COL_ALTO   = 8;

    public PackingList leer(File archivo) throws IOException {
        PackingList pl = new PackingList();
        pl.setNombreArchivo(archivo.getName());
        pl.setNumeroOrden(extraerNumeroPedido(archivo.getName()));
        pl.setFecha(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        String hoy = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String usuario = System.getProperty("user.name", "Usuario");
        pl.setFechaCreacion(hoy);
        pl.setUsuarioCreacion(usuario);
        pl.setFechaEdicion(hoy);
        pl.setUsuarioEdicion(usuario);

        try (FileInputStream fis = new FileInputStream(archivo);
             Workbook wb = new XSSFWorkbook(fis)) {

            // Buscar la hoja "P. list" o usar la primera
            Sheet sheet = wb.getSheet("P. list");
            if (sheet == null) sheet = wb.getSheetAt(0);

            Bulto bultoActual = null;
            int numeroBulto   = 0;
            boolean primeraFila = true;

            for (Row row : sheet) {
                if (primeraFila) { primeraFila = false; continue; } // saltar encabezado

                String partNum = getCellStr(row, COL_PART);
                String desc    = getCellStr(row, COL_DESC);
                int    cant    = (int) getCellNum(row, COL_CANT);
                String tipo    = getCellStr(row, COL_TIPO);

                // Saltar filas de totales o vacías
                if (partNum.isEmpty() && desc.isEmpty()) continue;
                if (esFila_Total(partNum)) {
                    // Capturar totales globales
                    if (partNum.equalsIgnoreCase("TOTAL PIEZAS"))
                        pl.setTotalPiezas(cant);
                    if (partNum.equalsIgnoreCase("PESO TOTAL KG"))
                        pl.setPesoTotalKg(getCellNum(row, COL_CANT));
                    if (partNum.equalsIgnoreCase("PESO NETO KG"))
                        pl.setPesoNetoKg(getCellNum(row, COL_CANT));
                    continue;
                }

                if (!tipo.isEmpty()) {
                    // Nueva cabecera de bulto
                    numeroBulto++;
                    double pesoTotal = getCellNum(row, COL_PTOTAL);
                    double pesoNeto  = getCellNum(row, COL_PNETO);
                    double largo     = getCellNum(row, COL_LARGO);
                    double ancho     = getCellNum(row, COL_ANCHO);
                    double alto      = getCellNum(row, COL_ALTO);

                    bultoActual = new Bulto(numeroBulto, tipo, pesoTotal, pesoNeto, largo, ancho, alto);
                    pl.addBulto(bultoActual);
                }

                // Agregar ítem al bulto actual (o crear uno genérico si no hay bulto definido)
                if (bultoActual == null) {
                    numeroBulto++;
                    bultoActual = new Bulto(numeroBulto, numeroBulto + " BULTO", 0, 0, 0, 0, 0);
                    pl.addBulto(bultoActual);
                }

                if (!partNum.isEmpty() || !desc.isEmpty()) {
                    bultoActual.addItem(new PackingItem(partNum, desc, cant));
                }
            }
        }

        return pl;
    }

    // ---- Utilidades ----

    private boolean esFila_Total(String partNum) {
        String up = partNum.toUpperCase();
        return up.startsWith("TOTAL") || up.startsWith("CANTIDAD") || up.startsWith("PESO");
    }

    private String extraerNumeroPedido(String nombreArchivo) {
        // Ej: "Packing_list_Mitracont_S_A_NP_2025-1549.xlsx" → "NP 2025-1549"
        String sin = nombreArchivo.replaceAll("\\.xlsx?$", "");
        int idx = sin.indexOf("NP");
        if (idx >= 0) {
            return sin.substring(idx).replace("_", " ").trim();
        }
        return sin;
    }

    private String getCellStr(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case FORMULA -> {
                try { yield cell.getStringCellValue().trim(); }
                catch (Exception e) {
                    try { yield String.valueOf((long) cell.getNumericCellValue()); }
                    catch (Exception e2) { yield ""; }
                }
            }
            default -> "";
        };
    }

    private double getCellNum(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING  -> {
                try { yield Double.parseDouble(cell.getStringCellValue().replace(",", ".")); }
                catch (Exception e) { yield 0; }
            }
            case FORMULA -> {
                try { yield cell.getNumericCellValue(); }
                catch (Exception e) { yield 0; }
            }
            default -> 0;
        };
    }
}
