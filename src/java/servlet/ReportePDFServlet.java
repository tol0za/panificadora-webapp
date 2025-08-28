package servlet;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import dao.NotaVentaDAO; // usa tus DAOs existentes
import modelo.NotaVenta;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


public class ReportePDFServlet extends HttpServlet {
    private final NotaVentaDAO dao = new NotaVentaDAO();
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            LocalDate desde = LocalDate.parse(req.getParameter("desde"));
            LocalDate hasta = LocalDate.parse(req.getParameter("hasta"));
            List<NotaVenta> lista = dao.listarPorRango(desde, hasta);

            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "inline; filename=historial.pdf");

            Document doc = new Document(PageSize.LETTER.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, resp.getOutputStream());
            doc.open();
            Font h = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font t = new Font(Font.HELVETICA, 10);

            doc.add(new Paragraph("Historial de Notas ("+desde+" a "+hasta+")", h));
            doc.add(new Paragraph(" "));

            Table table = new Table(5); table.setWidth(100);
            table.addCell("Fecha"); table.addCell("Folio"); table.addCell("Repartidor"); table.addCell("Tienda"); table.addCell("Total");
            for (NotaVenta n : lista) {
                table.addCell(n.getFechaNota().toLocalDate().toString());
                table.addCell(String.valueOf(n.getFolio()));
                table.addCell(String.valueOf(n.getIdRepartidor())); // o nombre si lo mapeas
                table.addCell(String.valueOf(n.getIdTienda()));
                table.addCell(String.format("$%,.2f", n.getTotal()));
            }
            doc.add(table);
            doc.close();
        } catch (Exception ex) {
            resp.sendError(500, ex.getMessage());
        }
    }
}
