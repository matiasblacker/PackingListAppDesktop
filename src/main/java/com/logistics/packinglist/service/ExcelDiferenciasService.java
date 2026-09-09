package com.logistics.packinglist.service;

import com.logistics.packinglist.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExcelDiferenciasService {

    public void generarReporteDiferencias(ReceptionAnnouncementModel announcement,
                                         ReceptionModel reception,
                                         SupplierModel supplier,
                                         WarehouseModel warehouse,
                                         Map<String, ProductModel> productMap,
                                         List<ProductFieldDefinitionModel> fieldDefs,
                                         File destFile) throws Exception {

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Diferencias Recepción");

            // Estilos
            XSSFFont fontTitle = wb.createFont();
            fontTitle.setBold(true);
            fontTitle.setFontHeightInPoints((short) 14);
            fontTitle.setColor(IndexedColors.DARK_BLUE.getIndex());

            CellStyle styleTitle = wb.createCellStyle();
            styleTitle.setFont(fontTitle);

            XSSFFont fontHeader = wb.createFont();
            fontHeader.setBold(true);
            fontHeader.setColor(IndexedColors.WHITE.getIndex());

            CellStyle styleHeader = wb.createCellStyle();
            styleHeader.setFont(fontHeader);
            styleHeader.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 10, (byte) 40, (byte) 80}, null));
            styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleHeader.setAlignment(HorizontalAlignment.CENTER);
            styleHeader.setBorderBottom(BorderStyle.THIN);

            CellStyle styleData = wb.createCellStyle();
            styleData.setBorderBottom(BorderStyle.THIN);
            styleData.setBorderTop(BorderStyle.THIN);
            styleData.setBorderLeft(BorderStyle.THIN);
            styleData.setBorderRight(BorderStyle.THIN);

            CellStyle styleFaltante = wb.createCellStyle();
            styleFaltante.cloneStyleFrom(styleData);
            XSSFFont fontRed = wb.createFont();
            fontRed.setColor(IndexedColors.RED.getIndex());
            fontRed.setBold(true);
            styleFaltante.setFont(fontRed);

            CellStyle styleSobrante = wb.createCellStyle();
            styleSobrante.cloneStyleFrom(styleData);
            XSSFFont fontGreen = wb.createFont();
            fontGreen.setColor(IndexedColors.GREEN.getIndex());
            fontGreen.setBold(true);
            styleSobrante.setFont(fontGreen);

            int rowIdx = 0;

            // Título
            Row rowTitle = sheet.createRow(rowIdx++);
            Cell cellTitle = rowTitle.createCell(0);
            cellTitle.setCellValue("INFORME DE DIFERENCIAS DE RECEPCIÓN / PROVEEDOR");
            cellTitle.setCellStyle(styleTitle);
            rowIdx++;

            // Metadatos
            String folioAnuncio = announcement != null ? announcement.getFolio() : "-";
            String folioRec = reception != null ? reception.getFolio() : "-";
            String suppName = supplier != null ? supplier.getRazonSocial() : "-";
            String whName = warehouse != null ? warehouse.getNombre() : "-";
            String diaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            String blStr = announcement != null && announcement.getNumeroBl() != null ? announcement.getNumeroBl() : "-";
            String ocStr = announcement != null && announcement.getOrdenCompra() != null ? announcement.getOrdenCompra() : "-";
            addMetaRow(sheet, rowIdx++, "Folio Anuncio:", folioAnuncio, "Folio Recepción:", folioRec);
            addMetaRow(sheet, rowIdx++, "Proveedor:", suppName, "Bodega:", whName);
            addMetaRow(sheet, rowIdx++, "N° BL:", blStr, "Orden Compra:", ocStr);
            addMetaRow(sheet, rowIdx++, "Fecha Emisión:", diaHora, "", "");
            rowIdx++;

            // Determinar columnas de atributos
            List<ProductFieldDefinitionModel> activeFields = fieldDefs != null ? fieldDefs : Collections.emptyList();

            // Encabezados de tabla
            Row headerRow = sheet.createRow(rowIdx++);
            int colIdx = 0;

            Cell c1 = headerRow.createCell(colIdx++);
            c1.setCellValue("SKU");
            c1.setCellStyle(styleHeader);

            Cell c2 = headerRow.createCell(colIdx++);
            c2.setCellValue("Descripción del Producto");
            c2.setCellStyle(styleHeader);

            for (ProductFieldDefinitionModel f : activeFields) {
                Cell cAttr = headerRow.createCell(colIdx++);
                cAttr.setCellValue(f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey());
                cAttr.setCellStyle(styleHeader);
            }

            Cell cAnunciado = headerRow.createCell(colIdx++);
            cAnunciado.setCellValue("Cant. Anunciada");
            cAnunciado.setCellStyle(styleHeader);

            Cell cRecibido = headerRow.createCell(colIdx++);
            cRecibido.setCellValue("Cant. Recibida");
            cRecibido.setCellStyle(styleHeader);

            Cell cDif = headerRow.createCell(colIdx++);
            cDif.setCellValue("Diferencia");
            cDif.setCellStyle(styleHeader);

            Cell cEstado = headerRow.createCell(colIdx++);
            cEstado.setCellValue("Estado");
            cEstado.setCellStyle(styleHeader);

            // Mapear líneas de anuncio y recepcionadas
            List<AnnouncementDetailModel> annDetails = announcement != null && announcement.getDetails() != null ? announcement.getDetails() : Collections.emptyList();
            List<ReceptionDetailModel> recDetails = reception != null && reception.getDetails() != null ? reception.getDetails() : Collections.emptyList();

            // Mapear cantidad recibida por productId
            Map<String, Integer> recMapByProd = new HashMap<>();
            for (ReceptionDetailModel rd : recDetails) {
                if (rd.getProductId() != null) {
                    recMapByProd.put(rd.getProductId(), recMapByProd.getOrDefault(rd.getProductId(), 0) + (rd.getCantidad() != null ? rd.getCantidad() : 0));
                }
            }

            // Para cada línea de anuncio, contrastar
            for (AnnouncementDetailModel ad : annDetails) {
                Row dataRow = sheet.createRow(rowIdx++);
                int cCount = 0;

                ProductModel prod = productMap != null ? productMap.get(ad.getProductId()) : null;
                String sku = prod != null && prod.getSku() != null ? prod.getSku() : "-";
                String nombre = prod != null && prod.getNombre() != null ? prod.getNombre() : "Producto Desconocido";

                int cantAnunciada = ad.getCantidad() != null ? ad.getCantidad() : 0;
                int cantRecibida = recMapByProd.getOrDefault(ad.getProductId(), 0);
                int dif = cantRecibida - cantAnunciada;

                createCell(dataRow, cCount++, sku, styleData);
                createCell(dataRow, cCount++, nombre, styleData);

                // Atributos personalizados
                Map<String, String> attrs = ad.getAtributosPersonalizados() != null ? ad.getAtributosPersonalizados() : Collections.emptyMap();
                for (ProductFieldDefinitionModel f : activeFields) {
                    String val = attrs.getOrDefault(f.getFieldKey(), "-");
                    createCell(dataRow, cCount++, val, styleData);
                }

                createCell(dataRow, cCount++, cantAnunciada, styleData);
                createCell(dataRow, cCount++, cantRecibida, styleData);

                Cell cD = dataRow.createCell(cCount++);
                cD.setCellValue(dif);
                cD.setCellStyle(dif < 0 ? styleFaltante : (dif > 0 ? styleSobrante : styleData));

                String estStr = dif < 0 ? "FALTANTE (" + dif + ")" : (dif > 0 ? "SOBRANTE (+" + dif + ")" : "COMPLETO");
                Cell cE = dataRow.createCell(cCount++);
                cE.setCellValue(estStr);
                cE.setCellStyle(dif < 0 ? styleFaltante : (dif > 0 ? styleSobrante : styleData));
            }

            // Autoajustar columnas
            for (int i = 0; i < colIdx; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                wb.write(fos);
            }
        }
    }

    private void addMetaRow(Sheet sheet, int rIdx, String lbl1, String val1, String lbl2, String val2) {
        Row row = sheet.createRow(rIdx);
        Cell c1 = row.createCell(0); c1.setCellValue(lbl1);
        Cell c2 = row.createCell(1); c2.setCellValue(val1);
        if (!lbl2.isEmpty()) {
            Cell c3 = row.createCell(3); c3.setCellValue(lbl2);
            Cell c4 = row.createCell(4); c4.setCellValue(val2);
        }
    }

    private void createCell(Row row, int col, Object val, CellStyle style) {
        Cell c = row.createCell(col);
        if (val instanceof Number) {
            c.setCellValue(((Number) val).doubleValue());
        } else {
            c.setCellValue(val != null ? val.toString() : "-");
        }
        c.setCellStyle(style);
    }
}
