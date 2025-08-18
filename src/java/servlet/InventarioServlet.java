package servlet;

import dao.CatalogoEmpaqueDAO;
import dao.InventarioEmpaquetadoDAO;
import modelo.CatalogoEmpaque;
import modelo.InventarioEmpaquetado;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class InventarioServlet extends HttpServlet {
    private InventarioEmpaquetadoDAO inventarioDAO;
    private CatalogoEmpaqueDAO empaqueDAO;

    // Claves de “flash” por módulo
    private static final String FLASH_INV    = "flashInv";
    private static final String FLASH_SALIDA = "flashSalida";
    private static final String FLASH_NOTAS  = "flashNotas";

    @Override
    public void init() throws ServletException {
        try {
            inventarioDAO = new InventarioEmpaquetadoDAO();
            empaqueDAO    = new CatalogoEmpaqueDAO();
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";

        try {
            // 🔒 Al entrar al módulo, limpia flashes AJENAS para que no “viajen”
            HttpSession ses = request.getSession(false);
            if (ses != null) {
                ses.removeAttribute(FLASH_SALIDA);
                ses.removeAttribute(FLASH_NOTAS);
                // por compatibilidad con mensajes antiguos genéricos:
                ses.removeAttribute("flashMsg");
                ses.removeAttribute("mensaje");
            }

            // Carga de empaques y mapa de stock actual
            List<CatalogoEmpaque> empaques = empaqueDAO.findAll();
            Map<Integer, Integer> stockMap = new HashMap<>();
            for (CatalogoEmpaque c : empaques) {
                int stock = inventarioDAO.obtenerCantidadActual(c.getIdEmpaque());
                stockMap.put(c.getIdEmpaque(), stock);
            }
            request.setAttribute("empaques", empaques);
            request.setAttribute("stockMap", stockMap);

            if ("listar".equals(accion)) {
                List<InventarioEmpaquetado> movimientos = inventarioDAO.listarMovimientos();
                request.setAttribute("movimientos", movimientos);
                request.getRequestDispatcher("/jsp/inventario/inventarioList.jsp")
                       .forward(request, response);

            } else if ("nuevo".equals(accion)) {
                request.getRequestDispatcher("/jsp/inventario/inventarioForm.jsp")
                       .forward(request, response);

            } else {
                // default
                List<InventarioEmpaquetado> movimientos = inventarioDAO.listarMovimientos();
                request.setAttribute("movimientos", movimientos);
                request.getRequestDispatcher("/jsp/inventario/inventarioList.jsp")
                       .forward(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            int    idEmpaque = Integer.parseInt(request.getParameter("idEmpaque"));
            int    cantidad  = Integer.parseInt(request.getParameter("cantidad"));
            String motivo    = request.getParameter("motivo");
            if (motivo == null) motivo = "";
            motivo = motivo.trim();

            // Ajuste de signo para salidas/mermas
            String motivoLC = motivo.toLowerCase();
            if (motivoLC.contains("salida") || motivoLC.contains("merma")) {
                cantidad = -Math.abs(cantidad);
            } else {
                cantidad =  Math.abs(cantidad);
            }

            int cantidadActual = inventarioDAO.obtenerCantidadActual(idEmpaque);
            int nuevaCantidad  = cantidadActual + cantidad;

            if (nuevaCantidad < 0) {
                request.getSession().setAttribute(FLASH_INV,
                        "No hay suficiente inventario para esta operación.");
                response.sendRedirect("InventarioServlet");
                return;
            }

            // Registrar el movimiento
            InventarioEmpaquetado movimiento = new InventarioEmpaquetado();
            movimiento.setIdEmpaque(idEmpaque);
            movimiento.setCantidad(cantidad);
            movimiento.setFecha(LocalDateTime.now());
            movimiento.setMotivo(motivo);
            movimiento.setCantidadActual(nuevaCantidad);
            inventarioDAO.registrarMovimiento(movimiento);

            // Mensaje modal (solo para Inventario)
            String signo    = (cantidad >= 0 ? "+" : "");
            String mensaje  = "<strong>Stock anterior:</strong> " + cantidadActual + " unidades<br>"
                            + "<strong>Movimiento:</strong> " + signo + cantidad + " unidades<br>"
                            + "<strong>Stock resultante:</strong> " + nuevaCantidad + " unidades";
            request.getSession().setAttribute(FLASH_INV, mensaje);

        } catch (Exception e) {
            request.getSession().setAttribute(FLASH_INV,
                    "Error al registrar movimiento: " + e.getMessage());
        }

        response.sendRedirect("InventarioServlet");
    }
}
