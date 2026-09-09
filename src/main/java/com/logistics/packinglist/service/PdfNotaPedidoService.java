package com.logistics.packinglist.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.logistics.packinglist.model.OrderNoteModel;
import com.logistics.packinglist.model.OrderNoteDetailModel;
import com.logistics.packinglist.model.ProductModel;
import com.logistics.packinglist.model.CustomerModel;
import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.SupplierModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Genera un PDF de la Nota de Pedido con un diseño premium y estructurado.
 */
public class PdfNotaPedidoService {

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
    private static final Font F_CELDA_SUB = new Font(Font.FontFamily.HELVETICA, 7, Font.ITALIC, GRIS_OSCURO);
    private static final Font F_TOTAL     = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD,   AZUL_MARINO);

    public void exportar(OrderNoteModel note, CompanyModel company, CustomerModel customer, SupplierModel supplier, Map<String, ProductModel> productMap, File destino) throws IOException, DocumentException {
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

            // Celda central: Nombre, giro, dirección, emitido por, día y hora
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

            String direccion = company != null && company.getDireccion() != null ? company.getDireccion() : "Dirección No Definida";
            Paragraph pDir = new Paragraph(direccion, F_SUBTITULO);
            pDir.setAlignment(Element.ALIGN_CENTER);
            cellMiddle.addElement(pDir);

            String activeUser = com.logistics.packinglist.service.AuthService.getInstance().getNombre() + " " + com.logistics.packinglist.service.AuthService.getInstance().getApellido();
            Paragraph pEmitido = new Paragraph("Emitido por: " + activeUser, F_SUBTITULO);
            pEmitido.setAlignment(Element.ALIGN_CENTER);
            cellMiddle.addElement(pEmitido);

            String diaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            Paragraph pFechaHora = new Paragraph(diaHora, F_SUBTITULO);
            pFechaHora.setAlignment(Element.ALIGN_CENTER);
            cellMiddle.addElement(pFechaHora);

            // Celda derecha: Logo de la app, "creado en Packing List App"
            PdfPCell cellRight = new PdfPCell();
            cellRight.setBorder(Rectangle.NO_BORDER);
            cellRight.setVerticalAlignment(Element.ALIGN_MIDDLE);
            
            Image appLogo = getAppLogo();
            if (appLogo != null) {
                appLogo.scaleToFit(35, 35);
                appLogo.setAlignment(Element.ALIGN_RIGHT);
                cellRight.addElement(appLogo);
            }
            Paragraph pAppCred = new Paragraph("creado en Packing List App", new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, GRIS_OSCURO));
            pAppCred.setAlignment(Element.ALIGN_RIGHT);
            cellRight.addElement(pAppCred);

            header.addCell(cellLeft);
            header.addCell(cellMiddle);
            header.addCell(cellRight);
            doc.add(header);

            // Separador
            agregarSeparador(doc);

            // Título principal
            Paragraph pTitle = new Paragraph("NOTA DE PEDIDO N° " + (note.getFolio() != null ? note.getFolio().replace("NP-", "") : ""), F_TITULO);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            pTitle.setSpacingAfter(15);
            doc.add(pTitle);

            // 2. BLOQUE METADATOS CLIENTE / NP
            PdfPTable metaTable = new PdfPTable(new float[]{2f, 3f, 2f, 3f});
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            String formattedFecha = "-";
            if (note.getFecha() != null) {
                try {
                    LocalDateTime ldt = LocalDateTime.parse(note.getFecha());
                    formattedFecha = ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                } catch (Exception e) {
                    formattedFecha = note.getFecha().replace("T", " ");
                }
            }
            addMetaCell(metaTable, "Fecha:", formattedFecha);
            addMetaCell(metaTable, "N° Orden Compra:", note.getAtributosPersonalizados() != null ? note.getAtributosPersonalizados().getOrDefault("orden_compra", "-") : "-");

            // Fila 2: Cliente / RUT Cliente
            String clientName = customer != null ? customer.getRazonSocial() : "-";
            String clientRut = customer != null ? customer.getRut() : "-";
            addMetaCell(metaTable, "Cliente:", clientName);
            addMetaCell(metaTable, "RUT Cliente:", clientRut);

            // Fila 3: Dirección / Comuna
            String clientDir = customer != null ? customer.getDireccion() : "-";
            String clientComuna = customer != null ? customer.getComuna() : "-";
            addMetaCell(metaTable, "Dirección Cliente:", clientDir);
            addMetaCell(metaTable, "Comuna:", clientComuna);

            // Fila 4: Atención / Teléfono
            String clientAtencion = customer != null ? customer.getAtencion() : "-";
            String clientTel = customer != null ? customer.getTelefono() : "-";
            addMetaCell(metaTable, "Atención:", clientAtencion);
            addMetaCell(metaTable, "Teléfono:", clientTel);

            // Fila 5: Distribuidor / Proveedor
            addMetaCell(metaTable, "Distribuidor:", company != null ? company.getRazonSocial() : "-");
            addMetaCell(metaTable, "Proveedor:", supplier != null ? supplier.getRazonSocial() : "-");

            // Fila 6: Agente Comercial / (vacío)
            String creador = note.getCreadorNombre() != null ? note.getCreadorNombre() : "-";
            addMetaCell(metaTable, "Agente Comercial:", creador);
            addMetaCell(metaTable, "", "");

            doc.add(metaTable);

            // Separador
            agregarSeparador(doc);

            // 3. TABLA DEL DETALLE
            PdfPTable table = new PdfPTable(new float[]{1.5f, 4.5f, 1.5f, 1.2f, 1.5f, 1.5f, 1.5f});
            table.setWidthPercentage(100);
            table.setSpacingAfter(15);

            // Encabezados de tabla
            String[] headers = {"SKU", "Descripción", "Precio Lista", "Cant. Pedida", "Cant. Back Order", "Valor Unitario", "Total"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Phrase(h, F_TH));
                c.setBackgroundColor(BLANCO);
                c.setBorderWidthBottom(1f);
                c.setBorderColorBottom(AZUL_MARINO);
                c.setBorderWidthTop(0);
                c.setBorderWidthLeft(0);
                c.setBorderWidthRight(0);
                c.setPaddingBottom(5);
                c.setPaddingTop(2);
                c.setHorizontalAlignment(h.contains("Cant.") || h.equals("Total") || h.equals("Precio Lista") || h.equals("Valor Unitario") ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
                table.addCell(c);
            }

            // Cargar líneas
            int filaIdx = 0;
            double totalNota = 0.0;
            for (OrderNoteDetailModel detail : note.getDetails()) {
                BaseColor bg = (filaIdx % 2 == 0) ? BLANCO : new BaseColor(248, 248, 248);

                ProductModel prod = productMap.get(detail.getProductId());
                String sku = prod != null ? prod.getSku() : "-";

                // Celda descripción multi-línea
                PdfPCell cDesc = new PdfPCell();
                cDesc.setBackgroundColor(bg);
                cDesc.setBorderColor(GRIS_CLARO);
                cDesc.setBorderWidth(0.5f);
                cDesc.setPadding(5);

                String prodNombre = prod != null ? prod.getNombre() : "Producto Desconocido";
                cDesc.addElement(new Paragraph(prodNombre, F_CELDA));

                String packing = prod != null && prod.getPacking() != null ? prod.getPacking() : "-";
                cDesc.addElement(new Paragraph("Packing: " + packing, F_CELDA_SUB));

                String pais = prod != null && prod.getPaisOrigen() != null ? prod.getPaisOrigen() : "-";
                cDesc.addElement(new Paragraph("País Origen: " + pais, F_CELDA_SUB));

                // SKU cell
                table.addCell(celdaTexto(sku, F_CELDA, bg, Element.ALIGN_LEFT));

                // Descripción cell
                table.addCell(cDesc);

                // Precio Lista cell
                double precioLista = prod != null && prod.getPrecio() != null ? prod.getPrecio() : 0.0;
                table.addCell(celdaTexto(formatCurrency(precioLista), F_CELDA, bg, Element.ALIGN_RIGHT));

                // Cantidad Pedida cell (Total solicitada por el cliente)
                int pedida = detail.getCantidadPedida() != null ? detail.getCantidadPedida() : 0;
                String cantSolicitadaStr = detail.getAtributosPersonalizados() != null ? detail.getAtributosPersonalizados().get("cant_solicitada") : null;
                int cantSolicitada = cantSolicitadaStr != null ? Integer.parseInt(cantSolicitadaStr) : pedida;
                table.addCell(celdaTexto(String.valueOf(cantSolicitada), F_CELDA, bg, Element.ALIGN_RIGHT));

                // Cantidad Back Order cell
                String cantBOStr = detail.getAtributosPersonalizados() != null ? detail.getAtributosPersonalizados().get("cant_back_order") : null;
                int backOrder = cantBOStr != null ? Integer.parseInt(cantBOStr) : (detail.getCantidadBackOrder() != null ? detail.getCantidadBackOrder() : 0);
                table.addCell(celdaTexto(String.valueOf(backOrder), F_CELDA, bg, Element.ALIGN_RIGHT));

                // Valor Unitario cell
                double valorUnitario = detail.getPrecioUnitario() != null ? detail.getPrecioUnitario() : precioLista;
                table.addCell(celdaTexto(formatCurrency(valorUnitario), F_CELDA, bg, Element.ALIGN_RIGHT));

                // Total cell - RN-SYNC-003 & RN-SYNC-004: BO does not impact NP total. Calculate on reserved units only.
                int reservada = (detail.getCantidadReservada() != null && detail.getCantidadReservada() > 0) ? detail.getCantidadReservada() : Math.max(0, cantSolicitada - backOrder);
                double totalLinea = reservada * valorUnitario;
                totalNota += totalLinea;
                table.addCell(celdaTexto(formatCurrency(totalLinea), F_CELDA, bg, Element.ALIGN_RIGHT));

                filaIdx++;
            }
            doc.add(table);

            // Total General de la Nota
            PdfPTable totalsTable = new PdfPTable(new float[]{8.2f, 1.8f});
            totalsTable.setWidthPercentage(100);
            
            PdfPCell cTotLbl = new PdfPCell(new Phrase("TOTAL NOTA DE PEDIDO:", F_TOTAL));
            cTotLbl.setBorder(Rectangle.NO_BORDER);
            cTotLbl.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cTotLbl.setPadding(5);
            totalsTable.addCell(cTotLbl);
            
            PdfPCell cTotVal = new PdfPCell(new Phrase(formatCurrency(totalNota), new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, AZUL_MARINO)));
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
            java.net.URL resource = getClass().getResource("/icon-lightbg.png");
            if (resource == null) {
                resource = getClass().getResource("/icon.png");
            }
            if (resource != null) {
                return Image.getInstance(resource);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void agregarSeparador(Document doc) throws DocumentException {
        PdfPTable sep = new PdfPTable(1);
        sep.setWidthPercentage(100);
        sep.setSpacingBefore(2);
        sep.setSpacingAfter(6);
        PdfPCell c = new PdfPCell();
        c.setBorderWidthBottom(1f);
        c.setBorderColorBottom(AZUL_MARINO);
        c.setBorderWidthTop(0);
        c.setBorderWidthLeft(0);
        c.setBorderWidthRight(0);
        c.setFixedHeight(1);
        sep.addCell(c);
        doc.add(sep);
    }

    private void addMetaCell(PdfPTable table, String label, String value) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, F_LABEL));
        cLabel.setBorder(Rectangle.NO_BORDER);
        cLabel.setPadding(3);
        table.addCell(cLabel);

        PdfPCell cVal = new PdfPCell(new Phrase(value, F_VALOR));
        cVal.setBorder(Rectangle.NO_BORDER);
        cVal.setPadding(3);
        table.addCell(cVal);
    }

    private PdfPCell celdaTexto(String texto, Font font, BaseColor bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "", font));
        c.setBackgroundColor(bg);
        c.setBorderColor(GRIS_CLARO);
        c.setBorderWidth(0.5f);
        c.setPadding(5);
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private String formatCurrency(double val) {
        if (val == (long) val) {
            return String.format("$ %,.0f", val);
        } else {
            return String.format("$ %,.2f", val);
        }
    }

    public void exportarHojaPickingWMS(OrderNoteModel note, CompanyModel company, CustomerModel customer, SupplierModel supplier, Map<String, ProductModel> productMap, java.util.List<com.logistics.packinglist.model.StockModel> stockList, Map<String, com.logistics.packinglist.model.LocationModel> locationMap, java.util.List<com.logistics.packinglist.model.ReceptionModel> recepciones, File destino) throws IOException, DocumentException {
        Document doc = new Document(PageSize.LETTER, 40, 40, 40, 44);
        FileOutputStream fos = new FileOutputStream(destino);

        try {
            PdfWriter.getInstance(doc, fos);
            doc.open();

            // 1. ENCABEZADO IDÉNTICO A NP
            PdfPTable header = new PdfPTable(new float[]{2f, 5f, 2f});
            header.setWidthPercentage(100);
            header.setSpacingAfter(10);

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

            String direccion = company != null && company.getDireccion() != null ? company.getDireccion() : "Dirección No Definida";
            Paragraph pDir = new Paragraph(direccion, F_SUBTITULO);
            pDir.setAlignment(Element.ALIGN_CENTER);
            cellMiddle.addElement(pDir);

            String activeUser = com.logistics.packinglist.service.AuthService.getInstance().getNombre() + " " + com.logistics.packinglist.service.AuthService.getInstance().getApellido();
            Paragraph pEmitido = new Paragraph("Emitido por: " + activeUser, F_SUBTITULO);
            pEmitido.setAlignment(Element.ALIGN_CENTER);
            cellMiddle.addElement(pEmitido);

            String diaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            Paragraph pFechaHora = new Paragraph(diaHora, F_SUBTITULO);
            pFechaHora.setAlignment(Element.ALIGN_CENTER);
            cellMiddle.addElement(pFechaHora);

            PdfPCell cellRight = new PdfPCell();
            cellRight.setBorder(Rectangle.NO_BORDER);
            cellRight.setVerticalAlignment(Element.ALIGN_MIDDLE);
            
            Image appLogo = getAppLogo();
            if (appLogo != null) {
                appLogo.scaleToFit(35, 35);
                appLogo.setAlignment(Element.ALIGN_RIGHT);
                cellRight.addElement(appLogo);
            }
            Paragraph pAppCred = new Paragraph("creado en Packing List App", new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, GRIS_OSCURO));
            pAppCred.setAlignment(Element.ALIGN_RIGHT);
            cellRight.addElement(pAppCred);

            header.addCell(cellLeft);
            header.addCell(cellMiddle);
            header.addCell(cellRight);
            doc.add(header);

            agregarSeparador(doc);

            // Título principal Hoja de Picking
            Paragraph pTitle = new Paragraph("HOJA DE PICKING - NOTA DE PEDIDO N° " + (note.getFolio() != null ? note.getFolio().replace("NP-", "") : ""), F_TITULO);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            pTitle.setSpacingAfter(15);
            doc.add(pTitle);

            // 2. BLOQUE METADATOS METADATA
            PdfPTable metaTable = new PdfPTable(new float[]{2f, 3f, 2f, 3f});
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            String formattedFecha = "-";
            if (note.getFecha() != null) {
                try {
                    LocalDateTime ldt = LocalDateTime.parse(note.getFecha());
                    formattedFecha = ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                } catch (Exception e) {
                    formattedFecha = note.getFecha().replace("T", " ");
                }
            }
            addMetaCell(metaTable, "Fecha Pedido:", formattedFecha);
            addMetaCell(metaTable, "N° Orden Compra:", note.getAtributosPersonalizados() != null ? note.getAtributosPersonalizados().getOrDefault("orden_compra", "-") : "-");

            String clientName = customer != null ? customer.getRazonSocial() : "-";
            String clientRut = customer != null ? customer.getRut() : "-";
            addMetaCell(metaTable, "Cliente:", clientName);
            addMetaCell(metaTable, "RUT Cliente:", clientRut);

            String clientDir = customer != null ? customer.getDireccion() : "-";
            String clientComuna = customer != null ? customer.getComuna() : "-";
            addMetaCell(metaTable, "Dirección Cliente:", clientDir);
            addMetaCell(metaTable, "Comuna:", clientComuna);

            String clientAtencion = customer != null ? customer.getAtencion() : "-";
            String clientTel = customer != null ? customer.getTelefono() : "-";
            addMetaCell(metaTable, "Atención:", clientAtencion);
            addMetaCell(metaTable, "Teléfono:", clientTel);

            addMetaCell(metaTable, "Distribuidor:", company != null ? company.getRazonSocial() : "-");
            addMetaCell(metaTable, "Proveedor:", supplier != null ? supplier.getRazonSocial() : "-");

            String creador = note.getCreadorNombre() != null ? note.getCreadorNombre() : "-";
            addMetaCell(metaTable, "Agente Comercial:", creador);
            addMetaCell(metaTable, "", "");

            doc.add(metaTable);
            agregarSeparador(doc);

            // 3. TABLA EXCLUSIVA DE 5 COLUMNAS SOLICITADAS
            PdfPTable table = new PdfPTable(new float[]{2f, 4f, 2.5f, 2f, 1.5f});
            table.setWidthPercentage(100);
            table.setSpacingAfter(15);

            String[] pickingHeaders = {"SKU", "Descripción o Nombre", "Recepción a Despachar", "Ubicación", "Cantidad a Despachar"};
            for (String h : pickingHeaders) {
                PdfPCell c = new PdfPCell(new Phrase(h, F_TH));
                c.setBackgroundColor(BLANCO);
                c.setBorderWidthBottom(1f);
                c.setBorderColorBottom(AZUL_MARINO);
                c.setBorderWidthTop(0);
                c.setBorderWidthLeft(0);
                c.setBorderWidthRight(0);
                c.setPaddingBottom(5);
                c.setPaddingTop(2);
                c.setHorizontalAlignment(h.contains("Cantidad") ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
                table.addCell(c);
            }

            int filaIdx = 0;
            int totalUnidadesPicking = 0;

            for (OrderNoteDetailModel detail : note.getDetails()) {
                ProductModel prod = productMap != null ? productMap.get(detail.getProductId()) : null;
                String sku = prod != null ? prod.getSku() : "-";
                String prodNombre = prod != null ? prod.getNombre() : "Producto Desconocido";

                int pedida = detail.getCantidadPedida() != null ? detail.getCantidadPedida() : 0;
                int qtyToDispatch = Math.max(0, pedida);

                // Buscar recepciones reales que contengan este producto
                java.util.List<String[]> allocatedRows = new java.util.ArrayList<>();
                if (recepciones != null) {
                    int remaining = qtyToDispatch;
                    for (com.logistics.packinglist.model.ReceptionModel r : recepciones) {
                        if (remaining <= 0) break;
                        if (r.getDetails() != null) {
                            for (com.logistics.packinglist.model.ReceptionDetailModel rd : r.getDetails()) {
                                if (detail.getProductId().equals(rd.getProductId())) {
                                    int recQty = rd.getCantidad() != null ? rd.getCantidad() : 0;
                                    if (recQty > 0) {
                                        int take = Math.min(remaining, recQty);
                                        com.logistics.packinglist.model.LocationModel loc = locationMap != null ? locationMap.get(rd.getLocationId()) : null;
                                        String ubiStr = loc != null ? loc.toString() : "Bodega Central";
                                        String recFolio = r.getFolio() != null ? r.getFolio() : "REC-GENERAL";
                                        allocatedRows.add(new String[]{recFolio, ubiStr, String.valueOf(take)});
                                        remaining -= take;
                                    }
                                }
                            }
                        }
                    }
                }

                if (allocatedRows.isEmpty()) {
                    // Sin desglose de recepciones específico
                    BaseColor bg = (filaIdx % 2 == 0) ? BLANCO : new BaseColor(248, 248, 248);
                    table.addCell(celdaTexto(sku, F_CELDA, bg, Element.ALIGN_LEFT));
                    table.addCell(celdaTexto(prodNombre, F_CELDA, bg, Element.ALIGN_LEFT));
                    table.addCell(celdaTexto("REC-GENERAL", F_CELDA, bg, Element.ALIGN_LEFT));
                    table.addCell(celdaTexto("Bodega Central", F_CELDA, bg, Element.ALIGN_LEFT));
                    table.addCell(celdaTexto(String.valueOf(qtyToDispatch), F_CELDA, bg, Element.ALIGN_RIGHT));
                    totalUnidadesPicking += qtyToDispatch;
                    filaIdx++;
                } else {
                    for (String[] row : allocatedRows) {
                        BaseColor bg = (filaIdx % 2 == 0) ? BLANCO : new BaseColor(248, 248, 248);
                        table.addCell(celdaTexto(sku, F_CELDA, bg, Element.ALIGN_LEFT));
                        table.addCell(celdaTexto(prodNombre, F_CELDA, bg, Element.ALIGN_LEFT));
                        table.addCell(celdaTexto(row[0], F_CELDA, bg, Element.ALIGN_LEFT));
                        table.addCell(celdaTexto(row[1], F_CELDA, bg, Element.ALIGN_LEFT));
                        table.addCell(celdaTexto(row[2], F_CELDA, bg, Element.ALIGN_RIGHT));

                        totalUnidadesPicking += Integer.parseInt(row[2]);
                        filaIdx++;
                    }
                }
            }
            doc.add(table);

            // Total Unidades a Despachar
            PdfPTable totalsTable = new PdfPTable(new float[]{8.2f, 1.8f});
            totalsTable.setWidthPercentage(100);
            
            PdfPCell cTotLbl = new PdfPCell(new Phrase("TOTAL UNIDADES A DESPACHAR:", F_TOTAL));
            cTotLbl.setBorder(Rectangle.NO_BORDER);
            cTotLbl.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cTotLbl.setPadding(5);
            totalsTable.addCell(cTotLbl);
            
            PdfPCell cTotVal = new PdfPCell(new Phrase(String.valueOf(totalUnidadesPicking) + " u.", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, AZUL_MARINO)));
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
}
