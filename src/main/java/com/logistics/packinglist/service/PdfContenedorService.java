package com.logistics.packinglist.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.logistics.packinglist.model.PackingList;

import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class PdfContenedorService {

    // Paleta monocromatica
    private static final BaseColor NEGRO        = BaseColor.BLACK;
    private static final BaseColor AZUL_MARINO  = new BaseColor(10, 40, 80);
    private static final BaseColor GRIS_OSCURO  = new BaseColor(60, 60, 60);
    private static final BaseColor GRIS_MEDIO   = new BaseColor(130, 130, 130);
    private static final BaseColor BLANCO       = BaseColor.WHITE;

    private static final Font F_TITULO    = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD,   AZUL_MARINO);
    private static final Font F_ORDEN     = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, GRIS_OSCURO);
    private static final Font F_TEXTO     = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, NEGRO);
    private static final Font F_NOTA      = new Font(Font.FontFamily.HELVETICA,  9, Font.ITALIC, GRIS_MEDIO);
    
    private File logoFile = null;

    public void setLogo(File logo) { this.logoFile = logo; }

    public void generarReporteMultiple(PackingList pl, List<Object[]> metadataContenedores, File target) throws Exception {
        Document doc = new Document(PageSize.LETTER.rotate(), 20, 20, 20, 20);
        FileOutputStream fos = new FileOutputStream(target);
        PdfWriter writer = PdfWriter.getInstance(doc, fos);
        writer.setPageEvent(new PieDePagina(pl));
        doc.open();

        for (int i = 0; i < metadataContenedores.size(); i++) {
            if (i > 0) doc.newPage();

            Object[] meta = metadataContenedores.get(i);
            String nombre = (String) meta[0];
            boolean is40ft = (boolean) meta[1];
            Pane workspace = (Pane) meta[2];
            List<String> listaCarga = (List<String>) meta[3];

            agregarEncabezado(doc, pl, nombre);
            agregarSeparador(doc);

            String tipoContenedor = is40ft ? "40" : "20";
            String nOrden = (pl.getNumeroOrden() != null && !pl.getNumeroOrden().isEmpty()) ? pl.getNumeroOrden() : "S/N";
            String explicacion = String.format(
                "Se certifica gráfica y dimensionalmente que la carga correspondiente a la orden de trabajo %s ha sido dispuesta espacialmente en un contenedor de %s pies. Las siguientes proyecciones demuestran la cabida y estiba de los bultos:",
                nOrden,
                tipoContenedor
            );
            Paragraph pExplicacion = new Paragraph(explicacion, F_TEXTO);
            pExplicacion.setAlignment(Element.ALIGN_JUSTIFIED);
            pExplicacion.setSpacingAfter(15);
            doc.add(pExplicacion);

            // Snapshot del contenedor
            WritableImage snapshot = workspace.snapshot(null, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", baos);
            Image imgCont = Image.getInstance(baos.toByteArray());
            imgCont.setAlignment(Element.ALIGN_CENTER);
            imgCont.scaleToFit(doc.getPageSize().getWidth() - 100, 280);
            imgCont.setBorder(Rectangle.BOX);
            imgCont.setBorderColor(AZUL_MARINO);
            doc.add(imgCont);

            // Tabla de carga al lado o abajo
            doc.add(new Paragraph("\nLista de Carga - " + (is40ft ? "40'" : "20'"), F_TEXTO));
            
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            for (String item : listaCarga) {
                PdfPCell cell = new PdfPCell(new Phrase("• " + item, F_NOTA));
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingLeft(20);
                table.addCell(cell);
            }
            doc.add(table);
        }

        doc.close();
        fos.close();
    }

    private void agregarEncabezado(Document doc, PackingList pl, String subTitulo) throws DocumentException, java.io.IOException {
        PdfPTable header = new PdfPTable(new float[]{3f, 5f});
        header.setWidthPercentage(100);

        PdfPCell celdaLogo = new PdfPCell();
        celdaLogo.setBorder(Rectangle.NO_BORDER);
        celdaLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);

        if (logoFile != null && logoFile.exists()) {
            try {
                Image logo = Image.getInstance(logoFile.getAbsolutePath());
                logo.scaleToFit(140, 60);
                celdaLogo.addElement(logo);
            } catch (Exception e) {}
        }

        PdfPCell celdaTitulo = new PdfPCell();
        celdaTitulo.setBorder(Rectangle.NO_BORDER);
        celdaTitulo.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph titulo = new Paragraph("CERTIFICADO DE ESTIBA", F_TITULO);
        titulo.setAlignment(Element.ALIGN_RIGHT);
        celdaTitulo.addElement(titulo);

        Paragraph st = new Paragraph(subTitulo, F_TEXTO);
        st.setAlignment(Element.ALIGN_RIGHT);
        celdaTitulo.addElement(st);

        Paragraph orden = new Paragraph("Orden: " + pl.getNumeroOrden(), F_ORDEN);
        orden.setAlignment(Element.ALIGN_RIGHT);
        celdaTitulo.addElement(orden);

        header.addCell(celdaLogo);
        header.addCell(celdaTitulo);
        doc.add(header);
    }

    private void agregarSeparador(Document doc) throws DocumentException {
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

    static class PieDePagina extends PdfPageEventHelper {
        private final PackingList pl;
        PieDePagina(PackingList pl) { this.pl = pl; }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font fPie = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, new BaseColor(130, 130, 130));
            String orden = pl.getNumeroOrden() != null ? pl.getNumeroOrden() : "";
            String texto = orden + "   |   Página " + writer.getPageNumber();
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(texto, fPie),
                    (document.right() + document.left()) / 2,
                    document.bottom() - 18, 0);

            cb.setLineWidth(0.5f);
            cb.setColorStroke(new BaseColor(200, 200, 200));
            cb.moveTo(document.left(), document.bottom() - 8);
            cb.lineTo(document.right(), document.bottom() - 8);
            cb.stroke();
        }
    }
}
