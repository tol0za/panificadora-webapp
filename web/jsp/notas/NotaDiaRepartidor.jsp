<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>Notas – ${repartidor.nombreRepartidor}</title>
  <!-- Bootstrap 5 -->
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body data-ctx="${pageContext.request.contextPath}">
    <fmt:setLocale value="es_MX"/>

<div class="container-fluid py-3">

  <!-- Cabecera -->
  <div class="d-flex justify-content-between align-items-center flex-wrap">
    <h4 class="mb-3">Notas de venta – ${repartidor.nombreRepartidor} (${hoy})</h4>
    <button class="btn btn-success" id="btnNueva">+ Nueva nota</button>
  </div>

  <!-- Alerta flash -->
  <c:if test="${not empty sessionScope.flashMsg}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
      ${sessionScope.flashMsg}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <c:remove var="flashMsg" scope="session"/>
  </c:if>

  <!-- Inventario disponible -->
  <div class="card card-body mb-4">
    <h6 class="card-title">Inventario disponible</h6>
    <div class="table-responsive">
      <table class="table table-sm mb-0">
        <thead class="table-light">
          <tr><th>Empaque</th><th>Precio</th><th>Restante</th></tr>
        </thead>
        <tbody>
          <c:forEach items="${inventario}" var="i">
            <tr>
              <td>${i.nombre}</td>
              <td>${i.precio}</td>
              <td>${i.restante}</td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Notas registradas -->
  <div class="card card-body">
    <h6 class="card-title">Notas registradas</h6>
    <div class="table-responsive">
      <table class="table table-striped" id="tblNotas">
        <thead class="table-light">
          <tr><th>Folio</th><th>Tienda</th><th>Total</th><th>Opciones</th></tr>
        </thead>
        <tbody>
          <c:forEach items="${listaNotas}" var="n">
            <tr>
              <td>${n.folio}</td>
              <td>
                <c:choose>
                  <c:when test="${not empty n.nombreTienda}">${n.nombreTienda}</c:when>
                  <c:otherwise>ID&nbsp;${n.idTienda}</c:otherwise>
                </c:choose>
              </td>
              <td>
                <fmt:formatNumber value="${n.total}"
                                  type="currency"/>
              </td>
              <td>
                <a class="btn btn-outline-primary btn-sm"
                   href="${pageContext.request.contextPath}/NotaVentaServlet?inFrame=1&accion=editarNota&id=${n.idNotaVenta}">
                  Editar
                </a>
                <a class="btn btn-outline-danger btn-sm"
                   href="${pageContext.request.contextPath}/NotaVentaServlet?inFrame=1&accion=eliminarNota&id=${n.idNotaVenta}"
                   onclick="return confirm('¿Eliminar nota?');">
                  Borrar
                </a>
              </td>
            </tr>
          </c:forEach>
        </tbody>
        <tfoot class="table-secondary fw-semibold">
          <tr>
            <td colspan="2">Total día</td>
          
            <td><fmt:formatNumber value="${totalDia}" type="currency"/></td>
            <td></td>
          </tr>
        </tfoot>
      </table>
    </div>
  </div>

  <button class="btn btn-warning mt-4"
          onclick="cerrarRuta(${repartidor.idRepartidor})">
    Cerrar ruta y devolver sobrante
  </button>
</div>

<!-- Modal Nueva Nota -->
<div class="modal fade" id="modalNota" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg"><div class="modal-content">
    <form id="formNota" action="${pageContext.request.contextPath}/NotaVentaServlet" method="post">
      <input type="hidden" name="accion"        value="guardarNota">
      <input type="hidden" name="id_repartidor" value="${repartidor.idRepartidor}">
      <input type="hidden" id="lineas" name="lineas">

      <div class="modal-header">
        <h5 class="modal-title">Nueva nota</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>

      <div class="modal-body">
        <!-- Encabezado nota -->
        <div class="row g-2 mb-3">
       <div class="col-md-4">
  <label class="form-label">Folio</label>
  <input required name="folio" id="inpFolio" class="form-control">
  <div id="folioHelpBad"  class="form-text text-danger  d-none">El folio ya existe</div>
  <div id="folioHelpGood" class="form-text text-success d-none">Folio disponible ✓</div>
</div>
          <div class="col-md-8">
            <label class="form-label">Tienda</label>
            <select required name="id_tienda" id="selTienda" class="form-select" disabled>
              <option value="" disabled selected>Seleccione…</option>
              <c:forEach items="${tiendas}" var="t">
                <option value="${t.idTienda}">${t.nombre}</option>
              </c:forEach>
            </select>
          </div>
        </div>

        <!-- Detalle de paquetes -->
        <div class="table-responsive mb-2">
          <table class="table table-bordered" id="tblDetalle">
            <thead class="table-light">
              <tr><th>Empaque</th><th>Vendidas</th><th>Merma</th><th>Subtotal</th><th></th></tr>
            </thead>
            <tbody></tbody>
            <tfoot class="table-secondary">
              <tr><td colspan="3" class="text-end">Total</td><td id="tdTotal">0.00</td><td></td></tr>
            </tfoot>
          </table>
        </div>

        <button type="button" class="btn btn-outline-secondary w-100"
        id="btnAddLinea" disabled>+ Agregar paquete</button>
      </div>

      <div class="modal-footer">
        <button class="btn btn-primary" id="btnSave" disabled>Guardar nota</button>
      </div>
    </form>
  </div></div>
</div>

<!-- Inventario JSON para JS -->
<script id="inventarioJson" type="application/json">${inventarioJson}</script>

<!-- Scripts -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/static/js/notas.js"></script>
</body>
</html>
