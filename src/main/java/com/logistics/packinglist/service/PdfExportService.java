package com.logistics.packinglist.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.logistics.packinglist.model.Bulto;
import com.logistics.packinglist.model.PackingItem;
import com.logistics.packinglist.model.PackingList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Genera un PDF en formato Carta, optimizado para impresión B&N.
 * Diseño limpio: fondo blanco, texto negro/azul marino, sin rellenos de color.
 */
public class PdfExportService {

    // Paleta monocromatica apta para impresoras B&N
    private static final BaseColor NEGRO        = BaseColor.BLACK;
    private static final BaseColor AZUL_MARINO  = new BaseColor(10, 40, 80);
    private static final BaseColor GRIS_OSCURO  = new BaseColor(60, 60, 60);
    private static final BaseColor GRIS_MEDIO   = new BaseColor(130, 130, 130);
    private static final BaseColor GRIS_CLARO   = new BaseColor(220, 220, 220);  // solo bordes
    private static final BaseColor BLANCO       = BaseColor.WHITE;

    // Fuentes
    private static final Font F_TITULO    = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,   AZUL_MARINO);
    private static final Font F_ORDEN     = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, GRIS_OSCURO);
    private static final Font F_BULTO_NOM = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD,   AZUL_MARINO);
    private static final Font F_BULTO_DET = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, GRIS_OSCURO);
    private static final Font F_TH        = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   AZUL_MARINO);
    private static final Font F_CELDA_PN  = new Font(Font.FontFamily.COURIER,    8, Font.NORMAL, NEGRO);
    private static final Font F_CELDA     = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, NEGRO);
    private static final Font F_TOTAL_LBL = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   AZUL_MARINO);
    private static final Font F_TOTAL_VAL = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   NEGRO);
    private static final Font F_PIE       = new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, GRIS_MEDIO);

    private File logoFile = null;

    public void setLogo(File logo) { this.logoFile = logo; }

    public void exportar(PackingList pl, File destino) throws IOException, DocumentException {
        Document doc = new Document(PageSize.LETTER, 40, 40, 40, 44);
        FileOutputStream fos = new FileOutputStream(destino);

        try {
            PdfWriter writer = PdfWriter.getInstance(doc, fos);
            writer.setPageEvent(new PieDePagina(pl));
            doc.open();

            for (int i = 0; i < pl.getBultos().size(); i++) {
                if (i > 0) doc.newPage();
                Bulto bulto = pl.getBultos().get(i);
                agregarEncabezado(doc, pl, writer);
                agregarSeparador(doc);
                agregarCabeceraBulto(doc, bulto, i + 1, pl.getBultos().size());
                doc.add(Chunk.NEWLINE);
                agregarTablaItems(doc, bulto);
                doc.add(Chunk.NEWLINE);
                agregarTotalBulto(doc, bulto);
            }

            // Última pagina: resumen global
            doc.newPage();
            agregarEncabezado(doc, pl, writer);
            agregarSeparador(doc);
            agregarResumenGlobal(doc, pl);

            doc.close();
        } finally {
            fos.close();
        }
    }

    // ===== Encabezado =====

    private void agregarEncabezado(Document doc, PackingList pl, PdfWriter writer)
            throws DocumentException, IOException {

        PdfPTable header = new PdfPTable(new float[]{3f, 5f});
        header.setWidthPercentage(100);

        // Celda izquierda: LOGO (fondo blanco)
        PdfPCell celdaLogo = new PdfPCell();
        celdaLogo.setBorder(Rectangle.NO_BORDER);
        celdaLogo.setBackgroundColor(BLANCO);
        celdaLogo.setPadding(4);
        celdaLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);

        if (logoFile != null && logoFile.exists()) {
            try {
                Image logo = Image.getInstance(logoFile.getAbsolutePath());
                logo.scaleToFit(140, 60);
                celdaLogo.addElement(logo);
            } catch (Exception e) {
                celdaLogo.addElement(new Paragraph("[ LOGO ]", F_BULTO_DET));
            }
        }
        // Si no hay logo, la celda queda en blanco (espacio reservado)

        // Celda derecha: titulo y datos (fondo blanco, texto azul marino / negro)
        PdfPCell celdaTitulo = new PdfPCell();
        celdaTitulo.setBorder(Rectangle.NO_BORDER);
        celdaTitulo.setBackgroundColor(BLANCO);
        celdaTitulo.setPadding(4);
        celdaTitulo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celdaTitulo.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph titulo = new Paragraph("PACKING LIST", F_TITULO);
        titulo.setAlignment(Element.ALIGN_RIGHT);
        celdaTitulo.addElement(titulo);

        if (!pl.getNumeroOrden().isBlank()) {
            Paragraph orden = new Paragraph(pl.getNumeroOrden(), F_ORDEN);
            orden.setAlignment(Element.ALIGN_RIGHT);
            celdaTitulo.addElement(orden);
        }

        Paragraph fecha = new Paragraph("Fecha: " + pl.getFecha(), F_ORDEN);
        fecha.setAlignment(Element.ALIGN_RIGHT);
        celdaTitulo.addElement(fecha);

        header.addCell(celdaLogo);
        header.addCell(celdaTitulo);
        doc.add(header);
    }

    private void agregarSeparador(Document doc) throws DocumentException {
        // Linea delgada azul marino como divisor visual
        PdfPTable sep = new PdfPTable(1);
        sep.setWidthPercentage(100);
        sep.setSpacingBefore(2);
        sep.setSpacingAfter(6);
        PdfPCell c = new PdfPCell();
        c.setBorderWidthBottom(1.5f);
        c.setBorderColorBottom(AZUL_MARINO);
        c.setBorderWidthTop(0);
        c.setBorderWidthLeft(0);
        c.setBorderWidthRight(0);
        c.setPadding(0);
        c.setFixedHeight(1);
        sep.addCell(c);
        doc.add(sep);
    }

    // ===== Cabecera del bulto =====

    private void agregarCabeceraBulto(Document doc, Bulto bulto, int idx, int total)
            throws DocumentException {

        // Nombre del bulto con recuadro simple
        PdfPTable tNombre = new PdfPTable(1);
        tNombre.setWidthPercentage(100);
        tNombre.setSpacingAfter(4);

        PdfPCell cNombre = new PdfPCell();
        cNombre.setBackgroundColor(BLANCO);
        cNombre.setBorderColor(AZUL_MARINO);
        cNombre.setBorderWidth(1f);
        cNombre.setPadding(8);

        Paragraph pNombre = new Paragraph(bulto.getEtiqueta(), F_BULTO_NOM);
        Paragraph pSub    = new Paragraph(idx + " de " + total + " bultos", F_BULTO_DET);
        cNombre.addElement(pNombre);
        cNombre.addElement(pSub);
        tNombre.addCell(cNombre);
        doc.add(tNombre);

        // Datos del bulto en una fila de 3 celdas (sin color de fondo)
        PdfPTable tDatos = new PdfPTable(new float[]{1f, 1f, 1f});
        tDatos.setWidthPercentage(100);
        tDatos.setSpacingAfter(4);

        tDatos.addCell(celdaDato("PESO BRUTO",
                String.format("%.1f kg", bulto.getPesoTotal())));
        tDatos.addCell(celdaDato("PESO NETO",
                String.format("%.1f kg", bulto.getPesoNeto())));

        String dims = bulto.getLargo() > 0
                ? String.format("%.0f × %.0f × %.0f cm", bulto.getLargo(), bulto.getAncho(), bulto.getAlto())
                : "—";
        tDatos.addCell(celdaDato("LARGO × ANCHO × ALTO", dims));

        doc.add(tDatos);
    }

    private PdfPCell celdaDato(String label, String valor) {
        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(BLANCO);
        c.setBorderColor(GRIS_CLARO);
        c.setBorderWidth(0.5f);
        c.setPadding(6);
        c.addElement(new Paragraph(label, F_BULTO_DET));
        Paragraph pVal = new Paragraph(valor, F_BULTO_NOM);
        pVal.getFont().setSize(11);
        c.addElement(pVal);
        return c;
    }

    // ===== Tabla de items =====

    private void agregarTablaItems(Document doc, Bulto bulto) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[]{3f, 8f, 2f});
        tabla.setWidthPercentage(100);

        // Encabezados: fondo blanco, texto azul marino, borde inferior azul marino
        for (String enc : new String[]{"PART NUMBER", "DESCRIPCIÓN", "CANTIDAD"}) {
            PdfPCell c = new PdfPCell(new Phrase(enc, F_TH));
            c.setBackgroundColor(BLANCO);
            c.setBorderWidthBottom(1f);
            c.setBorderColorBottom(AZUL_MARINO);
            c.setBorderWidthTop(0);
            c.setBorderWidthLeft(0);
            c.setBorderWidthRight(0);
            c.setPaddingBottom(5);
            c.setPaddingTop(2);
            c.setHorizontalAlignment(enc.equals("CANTIDAD") ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
            tabla.addCell(c);
        }

        // Filas alternas con linea gris muy suave (sin relleno de color)
        int fila = 0;
        for (PackingItem item : bulto.getItems()) {
            // Fila par: fondo muy levemente gris (casi blanco) para legibilidad
            BaseColor bg = (fila % 2 == 0) ? BLANCO : new BaseColor(248, 248, 248);

            tabla.addCell(celdaItem(item.getPartNumber(), F_CELDA_PN, bg, Element.ALIGN_LEFT));
            tabla.addCell(celdaItem(item.getDescripcion(), F_CELDA,   bg, Element.ALIGN_LEFT));
            tabla.addCell(celdaItem(String.valueOf(item.getCantidad()), F_CELDA, bg, Element.ALIGN_CENTER));
            fila++;
        }

        doc.add(tabla);
    }

    private PdfPCell celdaItem(String texto, Font font, BaseColor bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "", font));
        c.setBackgroundColor(bg);
        c.setBorderColor(GRIS_CLARO);
        c.setBorderWidth(0.5f);
        c.setPadding(5);
        c.setHorizontalAlignment(align);
        return c;
    }

    // ===== Total del bulto =====

    private void agregarTotalBulto(Document doc, Bulto bulto) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{8f, 2f});
        t.setWidthPercentage(100);

        PdfPCell cLabel = new PdfPCell(new Phrase("TOTAL UNIDADES EN ESTE BULTO", F_TOTAL_LBL));
        cLabel.setBackgroundColor(BLANCO);
        cLabel.setBorderColor(AZUL_MARINO);
        cLabel.setBorderWidth(0.5f);
        cLabel.setPadding(6);
        cLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(cLabel);

        PdfPCell cVal = new PdfPCell(new Phrase(String.valueOf(bulto.getTotalUnidades()), F_TOTAL_VAL));
        cVal.setBackgroundColor(BLANCO);
        cVal.setBorderColor(AZUL_MARINO);
        cVal.setBorderWidth(0.5f);
        cVal.setPadding(6);
        cVal.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(cVal);

        doc.add(t);
    }

    // ===== Resumen global =====

    private void agregarResumenGlobal(Document doc, PackingList pl) throws DocumentException {
        // Titulo
        Paragraph tit = new Paragraph("RESUMEN GENERAL", F_BULTO_NOM);
        tit.setSpacingAfter(8);
        doc.add(tit);

        PdfPTable tabla = new PdfPTable(new float[]{2f, 2f, 2f, 2f, 2f, 2f});
        tabla.setWidthPercentage(100);

        for (String enc : new String[]{"BULTO", "TIPO", "ÍTEMS", "UNIDADES", "PESO BRUTO", "DIMENSIONES"}) {
            PdfPCell c = new PdfPCell(new Phrase(enc, F_TH));
            c.setBackgroundColor(BLANCO);
            c.setBorderWidthBottom(1f);
            c.setBorderColorBottom(AZUL_MARINO);
            c.setBorderWidthTop(0);
            c.setBorderWidthLeft(0);
            c.setBorderWidthRight(0);
            c.setPaddingBottom(5);
            tabla.addCell(c);
        }

        int fila = 0;
        for (Bulto b : pl.getBultos()) {
            BaseColor bg = (fila % 2 == 0) ? BLANCO : new BaseColor(248, 248, 248);
            tabla.addCell(celdaItem(b.getEtiqueta(), F_CELDA, bg, Element.ALIGN_LEFT));
            tabla.addCell(celdaItem(b.getTipo(), F_CELDA, bg, Element.ALIGN_LEFT));
            tabla.addCell(celdaItem(String.valueOf(b.getItems().size()), F_CELDA, bg, Element.ALIGN_CENTER));
            tabla.addCell(celdaItem(String.valueOf(b.getTotalUnidades()), F_CELDA, bg, Element.ALIGN_CENTER));
            tabla.addCell(celdaItem(String.format("%.1f kg", b.getPesoTotal()), F_CELDA, bg, Element.ALIGN_RIGHT));
            String dims = b.getLargo() > 0
                    ? String.format("%.0fx%.0fx%.0f", b.getLargo(), b.getAncho(), b.getAlto())
                    : "—";
            tabla.addCell(celdaItem(dims, F_CELDA, bg, Element.ALIGN_CENTER));
            fila++;
        }
        doc.add(tabla);
        doc.add(Chunk.NEWLINE);

        // Totales
        PdfPTable totales = new PdfPTable(new float[]{4f, 2f});
        totales.setWidthPercentage(35);
        totales.setHorizontalAlignment(Element.ALIGN_RIGHT);

        agregarFilaTotales(totales, "Total bultos:",    String.valueOf(pl.getNumeroBultos()));
        agregarFilaTotales(totales, "Total piezas:",    String.valueOf(pl.getTotalPiezas()));
        agregarFilaTotales(totales, "Peso total (kg):", String.format("%.1f", pl.getPesoTotalKg()));
        agregarFilaTotales(totales, "Peso neto (kg):",  String.format("%.1f", pl.getPesoNetoKg()));
        doc.add(totales);
    }

    private void agregarFilaTotales(PdfPTable t, String label, String valor) {
        PdfPCell cL = new PdfPCell(new Phrase(label, F_TOTAL_LBL));
        cL.setBackgroundColor(BLANCO);
        cL.setBorderColor(GRIS_CLARO);
        cL.setBorderWidth(0.5f);
        cL.setPadding(5);
        t.addCell(cL);

        PdfPCell cV = new PdfPCell(new Phrase(valor, F_TOTAL_VAL));
        cV.setBackgroundColor(BLANCO);
        cV.setBorderColor(GRIS_CLARO);
        cV.setBorderWidth(0.5f);
        cV.setPadding(5);
        cV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(cV);
    }

    // ===== Pie de pagina =====

    static class PieDePagina extends PdfPageEventHelper {
        private final PackingList pl;
        PieDePagina(PackingList pl) { this.pl = pl; }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font fPie = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, new BaseColor(130, 130, 130));
            String texto = pl.getNumeroOrden() + "   |   Página " + writer.getPageNumber();
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(texto, fPie),
                    (document.right() + document.left()) / 2,
                    document.bottom() - 18, 0);

            // Linea fina sobre el pie
            cb.setLineWidth(0.5f);
            cb.setColorStroke(new BaseColor(200, 200, 200));
            cb.moveTo(document.left(), document.bottom() - 8);
            cb.lineTo(document.right(), document.bottom() - 8);
            cb.stroke();
        }
    }
}
