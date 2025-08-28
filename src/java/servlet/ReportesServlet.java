package servlet;

import util.PdfUtil;
import util.MailUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;


public class ReportesServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String fechaStr = req.getParameter("fecha");
            LocalDate fecha = (fechaStr==null||fechaStr.isBlank()) ? LocalDate.now() : LocalDate.parse(fechaStr);

            // TODO: arma los datos reales desde tu NotaVentaDAO (folio, tienda, total)
            List<String[]> filas = List.of(
                new String[]{"1001","Tienda X","$1,500.00"},
                new String[]{"1002","Tienda Y","$2,300.00"}
            );
            double totalDia = 3800.0;

            PdfUtil.reporteDiarioVentas(resp, fecha, filas, totalDia, "PANIFICADORA");
        } catch (Exception e){
            throw new ServletException(e);
        }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // ejemplo simple para enviar el PDF generado previamente como adjunto (puedes extenderlo)
        try {
            String to = req.getParameter("to");
            String asunto = req.getParameter("subject");
            String cuerpo = req.getParameter("body");

            // Config SMTP (mueve a config)
            MailUtil.enviar("smtp.tu-host.com", 587, "toloza.com@gmail.com", "pass", to, asunto, cuerpo);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().print("OK");
        } catch (Exception ex){
            resp.setStatus(400);
            resp.getWriter().print(ex.getMessage());
        }
    }
}
