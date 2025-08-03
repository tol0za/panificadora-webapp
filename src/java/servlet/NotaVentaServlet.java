package servlet;
import dao.*;
import modelo.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class NotaVentaServlet extends HttpServlet {
    private NotaVentaDAO notaVentaDAO;
    private DetalleNotaVentaDAO detalleNotaDAO;
    private RepartidorDAO repartidorDAO;
    private TiendaDAO tiendaDAO;
    private CatalogoEmpaqueDAO empaqueDAO;
    private DistribucionDAO distribucionDAO;

    @Override
    public void init() throws ServletException {
        try {
            notaVentaDAO = new NotaVentaDAO();
            detalleNotaDAO = new DetalleNotaVentaDAO();
            repartidorDAO = new RepartidorDAO();
            tiendaDAO = new TiendaDAO();
            empaqueDAO = new CatalogoEmpaqueDAO();
            distribucionDAO = new DistribucionDAO();
        } catch (SQLException e) {
            throw new ServletException("Error al inicializar DAO", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";
        try {
            switch (accion) {
                case "nuevo":
                    mostrarFormulario(request, response);
                    break;
                case "ver":
                    mostrarDetalle(request, response);
                    break;
                case "listar":
                default:
                    listarNotas(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("mensaje", "Error: " + e.getMessage());
            request.getRequestDispatcher("/jsp/notaventa/notaList.jsp").forward(request, response);
        }
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Repartidor> repartidores = repartidorDAO.findAll();
            List<Tienda> tiendas = tiendaDAO.findAll();
            request.setAttribute("repartidores", repartidores);
            request.setAttribute("tiendas", tiendas);
            request.getRequestDispatcher("/jsp/notaventa/notaForm.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("mensaje", "Error al cargar el formulario");
            request.getRequestDispatcher("/jsp/notaventa/notaForm.jsp").forward(request, response);
        }
    }

    private void listarNotas(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<NotaVenta> listaNotas = notaVentaDAO.listarTodas();
            request.setAttribute("listaNotas", listaNotas);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("mensaje", "Error al obtener las notas");
        }
        request.getRequestDispatcher("/jsp/notaventa/notaList.jsp").forward(request, response);
    }

    private void mostrarDetalle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int idNota = Integer.parseInt(request.getParameter("id"));
            NotaVenta nota = notaVentaDAO.buscarPorId(idNota);
            if (nota != null) {
                List<DetalleNotaVenta> detalles = detalleNotaDAO.buscarPorNota(idNota);
                for (DetalleNotaVenta d : detalles) {
                    CatalogoEmpaque e = empaqueDAO.buscarPorId(d.getIdEmpaque());
                    d.setNombreEmpaque(e != null ? e.getNombreEmpaque() : "");
                }
                nota.setDetalles(detalles);
                request.setAttribute("nota", nota);
                request.getRequestDispatcher("/jsp/notaventa/notaDetalle.jsp").forward(request, response);
            } else {
                request.getSession().setAttribute("mensaje", "Nota no encontrada");
                response.sendRedirect("NotaVentaServlet?accion=listar");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("mensaje", "Error al cargar el detalle: " + e.getMessage());
            response.sendRedirect("NotaVentaServlet?accion=listar");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String referer = request.getHeader("referer");
        if (referer == null || !referer.contains("/jsp/home/inicio.jsp")) {
            response.sendRedirect(request.getContextPath() + "/jsp/home/inicio.jsp?vista=jsp/notaventa/notaList.jsp");
            return;
        }

        try {
            NotaVenta nota = new NotaVenta();
            nota.setFolio(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            nota.setFecha(LocalDateTime.now());
            nota.setIdDistribucion(Integer.parseInt(request.getParameter("id_distribucion")));
            nota.setIdRepartidor(Integer.parseInt(request.getParameter("id_repartidor")));
            nota.setIdTienda(Integer.parseInt(request.getParameter("id_tienda")));

            double total = 0;
            List<DetalleNotaVenta> detalles = new ArrayList<>();
            String[] idEmpaques = request.getParameterValues("id_empaque[]");
            String[] cantidades = request.getParameterValues("cantidad[]");
            String[] mermas = request.getParameterValues("merma[]");
            String[] precios = request.getParameterValues("precio_unitario[]");

            for (int i = 0; i < idEmpaques.length; i++) {
                DetalleNotaVenta det = new DetalleNotaVenta();
                det.setIdEmpaque(Integer.parseInt(idEmpaques[i]));
                det.setCantidadVendida(Integer.parseInt(cantidades[i]));
                det.setMerma(Integer.parseInt(mermas[i]));
                det.setPrecioUnitario(Double.parseDouble(precios[i]));
                double totalLinea = det.getCantidadVendida() * det.getPrecioUnitario();
                det.setTotalLinea(totalLinea);
                total += totalLinea;
                detalles.add(det);
            }

            nota.setTotalNota(total);
            nota.setDetalles(detalles);

            boolean guardado = notaVentaDAO.insertarNotaVenta(nota);
            if (guardado) {
                InventarioEmpaquetadoDAO inventarioDAO = new InventarioEmpaquetadoDAO();
                for (DetalleNotaVenta d : detalles) {
                    inventarioDAO.actualizarStockDespuesVenta(
                        nota.getIdDistribucion(),
                        d.getIdEmpaque(),
                        d.getCantidadVendida(),
                        d.getMerma()
                    );

                    InventarioEmpaquetado movimiento = new InventarioEmpaquetado();
                    movimiento.setIdDistribucion(nota.getIdDistribucion());
                    movimiento.setIdEmpaque(d.getIdEmpaque());
                    movimiento.setCantidad(-(d.getCantidadVendida() + d.getMerma()));
                    movimiento.setFecha(nota.getFecha());
                    movimiento.setMotivo("Salida por Venta");
                    movimiento.setCantidadActual(inventarioDAO.obtenerCantidadActualPorRepartidorYEmpaque(
                        nota.getIdRepartidor(), d.getIdEmpaque()));
                    inventarioDAO.registrarMovimiento(movimiento);
                }
                request.getSession().setAttribute("mensaje", "Nota de venta registrada correctamente.");
            } else {
                request.getSession().setAttribute("mensaje", "No se pudo guardar la nota.");
            }
            response.sendRedirect(request.getContextPath() + "/NotaVentaServlet?accion=listar");

        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("mensaje", "Error al guardar: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/NotaVentaServlet?accion=nuevo");
        }
    }
}
