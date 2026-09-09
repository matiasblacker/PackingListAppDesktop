package com.logistics.packinglist.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.logistics.packinglist.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Genera un PDF del Anuncio de Carga con diseño estructurado, soportando atributos personalizados por línea.
 */
public class PdfAnuncioService {

    private static final BaseColor NEGRO        = BaseColor.BLACK;
    private static final BaseColor AZUL_MARINO  = new BaseColor(10, 40, 80);
    private static final BaseColor GRIS_OSCURO  = new BaseColor(60, 60, 60);
    private static final BaseColor GRIS_CLARO   = new BaseColor(220, 220, 220);
    private static final BaseColor BLANCO       = BaseColor.WHITE;

    private static final Font F_TITULO    = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD,   AZUL_MARINO);
    private static final Font F_SUBTITULO = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, GRIS_OSCURO);
    private static final Font F_LABEL     = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD,   AZUL_MARINO);
    private static final Font F_VALOR     = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, NEGRO);
    private static final Font F_TH        = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD,   AZUL_MARINO);
    private static final Font F_CELDA     = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, NEGRO);
    private static final Font F_TOTAL     = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD,   AZUL_MARINO);

    public void exportar(ReceptionAnnouncementModel announcement,
                         CompanyModel company,
                         SupplierModel supplier,
                         WarehouseModel warehouse,
                         Map<String, ProductModel> productMap,
                         List<StockModel> stockList,
                         Map<String, LocationModel> locationMap,
                         List<ProductFieldDefinitionModel> activeFieldDefs,
                         File destino) throws IOException, DocumentException {
                         
        Document doc = new Document(PageSize.LETTER, 40, 40, 40, 44);
        FileOutputStream fos = new FileOutputStream(destino);

        try {
            PdfWriter.getInstance(doc, fos);
            doc.open();

            // 1. ENCABEZADO
            PdfPTable header = new PdfPTable(new float[]{2f, 5f, 2f});
            header.setWidthPercentage(100);
            header.setSpacingAfter(10);

            // Celda izquierda: Logo empresa
            PdfPCell cellLeft = new PdfPCell();
            cellLeft.setBorder(Rectangle.NO_BORDER);
            cellLeft.setVerticalAlignment(Element.ALIGN_MIDDLE);
            Image compLogo = tryLoadImage(company != null ? company.getLogoUrl() : null);
            if (compLogo != null) {
                compLogo.scaleToFit(90, 55);
                cellLeft.addElement(compLogo);
            } else {
                cellLeft.addElement(new Paragraph("[Logo Empresa]", F_SUBTITULO));
            }

            // Celda central: Datos empresa
            PdfPCell cellMiddle = new PdfPCell();
            cellMiddle.setBorder(Rectangle.NO_BORDER);
            cellMiddle.setVerticalAlignment(Element.ALIGN_MIDDLE);
            
            String nombreEmpresa = company != null && company.getRazonSocial() != null ? company.getRazonSocial() : "Empresa Distribuidora";
            Paragraph pNombre = new Paragraph(nombreEmpresa, new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, AZUL_MARINO));
            pNombre.setAlignment(Element.ALIGN_CENTER);
            cellMiddle.addElement(pNombre);

            String giro = company != null && company.getGiro() != null ? company.getGiro() : "Giro No Definido";
            Paragraph pGiro = new Paragraph(giro, F_SUBTITULO);
            pGiro.setAlignment(Element.ALIGN_CENTER);
            cellMiddle.addElement(pGiro);

            String activeUser = AuthService.getInstance().getNombre() + " " + AuthService.getInstance().getApellido();
            Paragraph pEmitido = new Paragraph("Emitido por: " + activeUser, F_SUBTITULO);
            pEmitido.setAlignment(Element.ALIGN_CENTER);
            cellMiddle.addElement(pEmitido);

            String diaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            Paragraph pFechaHora = new Paragraph(diaHora, F_SUBTITULO);
            pFechaHora.setAlignment(Element.ALIGN_CENTER);
            cellMiddle.addElement(pFechaHora);

            // Celda derecha: App Logo / Info
            PdfPCell cellRight = new PdfPCell();
            cellRight.setBorder(Rectangle.NO_BORDER);
            cellRight.setVerticalAlignment(Element.ALIGN_MIDDLE);
            
            Image appLogo = getAppLogo();
            if (appLogo != null) {
                appLogo.scaleToFit(35, 35);
                appLogo.setAlignment(Element.ALIGN_RIGHT);
                cellRight.addElement(appLogo);
            }
            Paragraph pAppCred = new Paragraph("Packing List App", new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, GRIS_OSCURO));
            pAppCred.setAlignment(Element.ALIGN_RIGHT);
            cellRight.addElement(pAppCred);

            header.addCell(cellLeft);
            header.addCell(cellMiddle);
            header.addCell(cellRight);
            doc.add(header);

            // Separador
            agregarSeparador(doc);

            // Título principal
            String folioStr = announcement.getFolio() != null ? announcement.getFolio() : "ANUNCIO";
            Paragraph pTitle = new Paragraph("ANUNCIO DE CARGA N° " + folioStr, F_TITULO);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            pTitle.setSpacingAfter(15);
            doc.add(pTitle);

            // Metadatos
            PdfPTable metaTable = new PdfPTable(new float[]{2f, 3f, 2f, 3f});
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            String formattedFecha = "-";
            if (announcement.getFecha() != null) {
                try {
                    LocalDateTime ldt = LocalDateTime.parse(announcement.getFecha().replace(" ", "T"));
                    formattedFecha = ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                } catch (Exception e) {
                    formattedFecha = announcement.getFecha();
                }
            }

            addMetaCell(metaTable, "Folio Anuncio:", folioStr);
            addMetaCell(metaTable, "Fecha Anuncio:", formattedFecha);

            String suppName = supplier != null ? supplier.getRazonSocial() : "-";
            String whName = warehouse != null ? warehouse.getNombre() : "-";
            addMetaCell(metaTable, "Proveedor:", suppName);
            addMetaCell(metaTable, "Bodega:", whName);

            String estadoStr = announcement.getEstado() != null ? announcement.getEstado() : "PENDIENTE";
            String recFolio = announcement.getReceptionFolio() != null ? announcement.getReceptionFolio() : "-";
            String blStr = announcement.getNumeroBl() != null ? announcement.getNumeroBl() : "-";
            String ocStr = announcement.getOrdenCompra() != null ? announcement.getOrdenCompra() : "-";
            addMetaCell(metaTable, "Estado:", estadoStr);
            addMetaCell(metaTable, "N° BL:", blStr);
            addMetaCell(metaTable, "Orden Compra:", ocStr);
            addMetaCell(metaTable, "Folio Recepción:", recFolio);

            doc.add(metaTable);

            // Sección Observaciones si las hay
            if (announcement.getObservaciones() != null && !announcement.getObservaciones().trim().isEmpty()) {
                PdfPTable obsTable = new PdfPTable(1);
                obsTable.setWidthPercentage(100);
                obsTable.setSpacingAfter(15);

                PdfPCell obsCell = new PdfPCell();
                obsCell.setBorderColor(GRIS_CLARO);
                obsCell.setBorderWidth(0.5f);
                obsCell.setPadding(8);
                obsCell.setBackgroundColor(new BaseColor(250, 250, 250));

                Paragraph pObsHead = new Paragraph("Observaciones:", F_LABEL);
                Paragraph pObsText = new Paragraph(announcement.getObservaciones(), F_VALOR);
                obsCell.addElement(pObsHead);
                obsCell.addElement(pObsText);
                obsTable.addCell(obsCell);

                doc.add(obsTable);
            }

            // Recolectar atributos dinámicos presentes en las líneas
            Map<String, String> attributeKeysToLabels = new LinkedHashMap<>();
            if (activeFieldDefs != null) {
                for (ProductFieldDefinitionModel f : activeFieldDefs) {
                    attributeKeysToLabels.put(f.getFieldKey(), f.getFieldLabel() != null ? f.getFieldLabel() : f.getFieldKey());
                }
            }
            if (announcement.getDetails() != null) {
                for (AnnouncementDetailModel d : announcement.getDetails()) {
                    if (d.getAtributosPersonalizados() != null) {
                        for (String key : d.getAtributosPersonalizados().keySet()) {
                            if (!attributeKeysToLabels.containsKey(key)) {
                                String label = key.replace("_", " ");
                                label = label.substring(0, 1).toUpperCase() + label.substring(1);
                                attributeKeysToLabels.put(key, label);
                            }
                        }
                    }
                }
            }

            // Configurar columnas de la tabla: SKU, Descripción, [Atributos], Ubicación, Cantidad
            int customColsCount = attributeKeysToLabels.size();
            float[] colWidths = new float[4 + customColsCount];
            colWidths[0] = 2.5f; // SKU
            colWidths[1] = 4.0f; // Desc
            int idx = 2;
            for (int i = 0; i < customColsCount; i++) {
                colWidths[idx++] = 2.0f; // Atributos
            }
            colWidths[idx++] = 2.0f; // Ubicación
            colWidths[idx++] = 1.5f; // Cantidad

            PdfPTable table = new PdfPTable(colWidths);
            table.setWidthPercentage(100);
            table.setSpacingAfter(15);

            addHeaderCell(table, "SKU", Element.ALIGN_LEFT);
            addHeaderCell(table, "Descripción", Element.ALIGN_LEFT);
            for (String label : attributeKeysToLabels.values()) {
                addHeaderCell(table, label, Element.ALIGN_LEFT);
            }
            addHeaderCell(table, "Ubicación", Element.ALIGN_LEFT);
            addHeaderCell(table, "Cantidad", Element.ALIGN_RIGHT);

            int filaIdx = 0;
            int totalCantidad = 0;

            if (announcement.getDetails() != null) {
                for (AnnouncementDetailModel detail : announcement.getDetails()) {
                    BaseColor bg = (filaIdx % 2 == 0) ? BLANCO : new BaseColor(248, 248, 248);

                    ProductModel prod = productMap != null ? productMap.get(detail.getProductId()) : null;
                    String sku = prod != null && prod.getSku() != null ? prod.getSku() : "-";
                    String prodNombre = prod != null && prod.getNombre() != null ? prod.getNombre() : "Producto Desconocido";

                    String ubiCode = resolverPrimeraUbicacion(detail.getProductId(), stockList, locationMap);

                    int qty = detail.getCantidad() != null ? detail.getCantidad() : 0;
                    totalCantidad += qty;

                    table.addCell(celdaTexto(sku, F_CELDA, bg, Element.ALIGN_LEFT));
                    table.addCell(celdaTexto(prodNombre, F_CELDA, bg, Element.ALIGN_LEFT));

                    Map<String, String> lineAttrs = detail.getAtributosPersonalizados() != null ? detail.getAtributosPersonalizados() : Collections.emptyMap();
                    for (String key : attributeKeysToLabels.keySet()) {
                        String val = lineAttrs.getOrDefault(key, "-");
                        table.addCell(celdaTexto(val, F_CELDA, bg, Element.ALIGN_LEFT));
                    }

                    table.addCell(celdaTexto(ubiCode, F_CELDA, bg, Element.ALIGN_LEFT));
                    table.addCell(celdaTexto(String.valueOf(qty), F_CELDA, bg, Element.ALIGN_RIGHT));

                    filaIdx++;
                }
            }

            doc.add(table);

            // Total Unidades
            PdfPTable totalsTable = new PdfPTable(new float[]{8.5f, 1.5f});
            totalsTable.setWidthPercentage(100);

            PdfPCell cTotLbl = new PdfPCell(new Phrase("TOTAL UNIDADES:", F_TOTAL));
            cTotLbl.setBorder(Rectangle.NO_BORDER);
            cTotLbl.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cTotLbl.setPadding(5);
            totalsTable.addCell(cTotLbl);

            PdfPCell cTotVal = new PdfPCell(new Phrase(String.valueOf(totalCantidad) + " u.", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, AZUL_MARINO)));
            cTotVal.setBorder(Rectangle.NO_BORDER);
            cTotVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cTotVal.setPadding(5);
            totalsTable.addCell(cTotVal);

            doc.add(totalsTable);

            doc.close();
        } finally {
            fos.close();
        }
    }

    private void addHeaderCell(PdfPTable table, String text, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, F_TH));
        c.setBackgroundColor(BLANCO);
        c.setBorderWidthBottom(1f);
        c.setBorderColorBottom(AZUL_MARINO);
        c.setBorderWidthTop(0);
        c.setBorderWidthLeft(0);
        c.setBorderWidthRight(0);
        c.setPaddingBottom(5);
        c.setPaddingTop(2);
        c.setHorizontalAlignment(align);
        table.addCell(c);
    }

    private String resolverPrimeraUbicacion(String productId, List<StockModel> stockList, Map<String, LocationModel> locationMap) {
        if (productId == null || stockList == null || locationMap == null) {
            return "";
        }
        for (StockModel stock : stockList) {
            if (productId.equals(stock.getProductId()) && stock.getLocationId() != null) {
                LocationModel loc = locationMap.get(stock.getLocationId());
                if (loc != null && loc.getCodigoUbicacion() != null && !loc.getCodigoUbicacion().isEmpty()) {
                    return loc.getCodigoUbicacion();
                }
            }
        }
        return "";
    }

    private Image tryLoadImage(String urlStr) {
        if (urlStr == null || urlStr.trim().isEmpty()) return null;
        try {
            return Image.getInstance(urlStr);
        } catch (Exception e) {
            return null;
        }
    }

    private Image getAppLogo() {
        try {
            java.net.URL res = getClass().getResource("/images/logo.png");
            if (res != null) {
                return Image.getInstance(res);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void agregarSeparador(Document doc) throws DocumentException {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidthTop(1.5f);
        cell.setBorderColorTop(AZUL_MARINO);
        cell.setBorderWidthBottom(0);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setFixedHeight(5f);
        line.addCell(cell);
        doc.add(line);
    }

    private void addMetaCell(PdfPTable table, String label, String valor) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, F_LABEL));
        cLabel.setBorder(Rectangle.NO_BORDER);
        cLabel.setPadding(3);
        table.addCell(cLabel);

        PdfPCell cValor = new PdfPCell(new Phrase(valor != null ? valor : "-", F_VALOR));
        cValor.setBorder(Rectangle.NO_BORDER);
        cValor.setPadding(3);
        table.addCell(cValor);
    }

    private PdfPCell celdaTexto(String texto, Font fuente, BaseColor bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "", fuente));
        c.setBackgroundColor(bg);
        c.setBorderColor(GRIS_CLARO);
        c.setBorderWidth(0.5f);
        c.setPadding(5);
        c.setHorizontalAlignment(align);
        return c;
    }
}
