package servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dao.CatalogoEmpaqueDAO;
import dao.DistribucionDAO;
import dao.InventarioEmpaquetadoDAO;
import modelo.CatalogoEmpaque;
import modelo.Distribucion;
import modelo.InventarioEmpaquetado;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


public class EmpaquesPorRepartidorServlet extends HttpServlet {

    private InventarioEmpaquetadoDAO inventarioDAO;
    private CatalogoEmpaqueDAO empaqueDAO;
    private DistribucionDAO distribucionDAO;

    @Override
    public void init() throws ServletException {
        try {
            inventarioDAO = new InventarioEmpaquetadoDAO();
            empaqueDAO = new CatalogoEmpaqueDAO();
            distribucionDAO = new DistribucionDAO();
        } catch (Exception e) {
            throw new ServletException("Error al inicializar DAOs", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idRepartidorStr = request.getParameter("idRepartidor");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        JsonArray jsonArray = new JsonArray();

        try {
            if (idRepartidorStr == null || idRepartidorStr.isEmpty()) {
                out.write("[]");
                return;
            }

            int idRepartidor = Integer.parseInt(idRepartidorStr);

            Distribucion distribucion = distribucionDAO.obtenerUltimaDistribucionPorRepartidor(idRepartidor);

            if (distribucion == null) {
                out.write("[]");
                return;
            }

            int idDistribucion = distribucion.getIdDistribucion();
            List<InventarioEmpaquetado> inventario = inventarioDAO.obtenerPorDistribucion(idDistribucion);

            for (InventarioEmpaquetado item : inventario) {
                CatalogoEmpaque empaque = empaqueDAO.buscarPorId(item.getIdEmpaque());

                if (empaque != null && item.getCantidadActual() > 0) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id_empaque", empaque.getIdEmpaque());
                    obj.addProperty("nombre_empaque", empaque.getNombreEmpaque());
                    obj.addProperty("precio_unitario", empaque.getPrecioUnitario());
                    obj.addProperty("stock", item.getCantidadActual());
                    obj.addProperty("id_distribucion", idDistribucion);
                    jsonArray.add(obj);
                }
            }

            // 🚧 Log para depurar en navegador si deseas (útil en consola del navegador)
            System.out.println("📦 JSON enviado al cliente:");
            System.out.println(jsonArray.toString());

            out.write(jsonArray.toString());

        } catch (Exception e) {
            e.printStackTrace();
            out.write("[]");
        } finally {
            out.close();
        }
    }
}
