package util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.awt.Color;

public class PdfUtil {

    public static void reporteDiarioVentas(HttpServletResponse resp,
                                           LocalDate fecha,
                                           List<String[]> filas, // [folio, tienda, total]
                                           double totalDia,
                                           String empresa) throws Exception {
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "inline; filename=reporte_"+fecha+".pdf");
        Document doc = new Document(PageSize.LETTER, 36, 36, 54, 36);
        OutputStream os = resp.getOutputStream();
        PdfWriter.getInstance(doc, os);
        doc.open();

        Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font th = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font td = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph title = new Paragraph(empresa+"\nREPORTE DE VENTAS – "+fecha, h1);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(12f);
        doc.add(title);

        PdfPTable t = new PdfPTable(new float[]{20, 60, 20});
        t.setWidthPercentage(100);

        addCell(t, "Folio", th, true);
        addCell(t, "Tienda", th, true);
        addCell(t, "Total", th, true);

        for (String[] f: filas){
            addCell(t, f[0], td, false);
            addCell(t, f[1], td, false);
            addCell(t, f[2], td, false);
        }

        PdfPCell tot = new PdfPCell(new Phrase("TOTAL DIA: $"+String.format("%,.2f", totalDia), th));
        tot.setColspan(3); tot.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tot.setPadding(6f);
        t.addCell(tot);

        doc.add(t);
        doc.close();
        os.flush();
    }

    private static void addCell(PdfPTable t, String s, Font f, boolean header){
        PdfPCell c = new PdfPCell(new Phrase(s, f));
        c.setPadding(4f);
        if (header){ c.setBackgroundColor(new Color(240,240,240)); }
        t.addCell(c);
    }
}
