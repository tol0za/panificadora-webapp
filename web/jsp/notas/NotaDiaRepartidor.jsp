<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>Notas – ${repartidor.nombreRepartidor}</title>

  <!-- Bootstrap + Bootstrap-icons -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
</head>

<body data-ctx="${pageContext.request.contextPath}">
<div class="container-fluid py-3">

  <!-- CABECERA -->
  <div class="d-flex justify-content-between align-items-center flex-wrap">
    <h4 class="mb-3">
      Notas de venta – ${repartidor.nombreRepartidor} (${hoy})
    </h4>
    <button id="btnNueva" class="btn btn-success">
      <i class="bi bi-plus-circle"></i> Nueva nota
    </button>
  </div>

  <!-- FLASH -->

<c:if test="${not empty sessionScope.flashMsg}">
    <!-- Determina color: rojo si contiene “Stock insuficiente” o “Error” -->
    <c:set var="alertClass"
           value="${fn:contains(sessionScope.flashMsg,'Stock insuficiente')
                    or fn:contains(sessionScope.flashMsg,'Error')
                      ? 'alert-danger'
                      : 'alert-success'}" />

    <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
        <c:out value="${sessionScope.flashMsg}" escapeXml="false"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>

    <c:remove var="flashMsg" scope="session"/>
</c:if>

  <!-- INVENTARIO DEL REPARTIDOR -->
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

  <!-- LISTA DE NOTAS -->
  <div class="card card-body">
    <h6 class="card-title">Notas registradas</h6>
    <div class="table-responsive">
      <table id="tblNotas" class="table table-striped">
        <thead class="table-light">
          <tr>
            <th>Folio</th><th>Tienda</th><th>Total</th>
            <th class="text-center">Opciones</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach items="${listaNotas}" var="n">
            <tr>
              <td>${n.folio}</td>
              <td>
                <c:choose>
                  <c:when test="${not empty n.nombreTienda}">${n.nombreTienda}</c:when>
                  <c:otherwise>ID ${n.idTienda}</c:otherwise>
                </c:choose>
              </td>
              <td>${n.total}</td>
              <td class="text-center">

                <!-- Ver -->
              <a href="#"
   class="btn btn-sm btn-outline-info me-1 btn-det"
   data-idNota="${n.idNotaVenta}"
   data-folio ="${n.folio}"
   data-tienda="${n.nombreTienda}"
   title="Ver detalle">
  <i class="bi bi-eye"></i>
</a>

                <!-- Editar -->
                <a href="${pageContext.request.contextPath}/NotaVentaServlet?inFrame=1&accion=editarNota&id=${n.idNotaVenta}"
                   class="btn btn-sm btn-outline-primary me-1" title="Editar">
                  <i class="bi bi-pencil-square"></i>
                </a>

                <!-- Eliminar -->
                <a href="${pageContext.request.contextPath}/NotaVentaServlet?inFrame=1&accion=eliminarNota&id=${n.idNotaVenta}"
                   class="btn btn-sm btn-outline-danger"
                   onclick="return confirm('¿Eliminar nota?');"
                   title="Eliminar">
                  <i class="bi bi-trash"></i>
                </a>

              </td>
            </tr>
          </c:forEach>
        </tbody>
        <tfoot class="table-secondary fw-semibold">
          <tr><td colspan="2">Total día</td><td>${totalDia}</td><td></td></tr>
        </tfoot>
      </table>
    </div>
  </div>

  <!-- CERRAR RUTA -->
  <button class="btn btn-warning mt-4"
          onclick="cerrarRuta(${repartidor.idRepartidor})">
    Cerrar ruta y devolver sobrante
  </button>
      <!-- NUEVO ▸ REABRIR RUTA (solo se muestra si el inventario quedó vacío) -->
  <c:if test="${empty inventario}">
    <form class="d-inline" method="post"
          action="${pageContext.request.contextPath}/NotaVentaServlet">
      <input type="hidden" name="inFrame"       value="1"/>
      <input type="hidden" name="accion"        value="reabrirRuta"/><!-- coincide con ACC_REABRIR_RUTA -->
      <input type="hidden" name="id_repartidor" value="${repartidor.idRepartidor}"/>
      <button type="submit"
              class="btn btn-outline-warning mt-4 ms-2"
              onclick="return confirm('¿Reabrir la ruta para seguir capturando notas?');">
        <i class="bi bi-arrow-counterclockwise"></i> Reabrir ruta
      </button>
    </form>
  </c:if>
</div>

<!-- ============ MODAL NUEVA NOTA ============ -->
<div id="modalNota" class="modal fade" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg"><div class="modal-content">
    <form id="formNota" action="${pageContext.request.contextPath}/NotaVentaServlet" method="post">
      <input type="hidden" name="accion"        value="guardarNota">
      <input type="hidden" name="id_repartidor" value="${repartidor.idRepartidor}">
      <input type="hidden" id="lineas" name="lineas">

      <div class="modal-header">
        <h5 class="modal-title">Nueva nota</h5>
        <button class="btn-close" data-bs-dismiss="modal"></button>
      </div>

      <div class="modal-body">
        <div class="row g-2 mb-3">
          <!-- Folio -->
          <div class="col-md-4">
            <label class="form-label">Folio</label>
            <input id="inpFolio" name="folio" required class="form-control">
            <div id="folioHelpBad"  class="form-text text-danger  d-none">El folio ya existe</div>
            <div id="folioHelpGood" class="form-text text-success d-none">Folio disponible ✓</div>
          </div>
          <!-- Tienda -->
          <div class="col-md-8">
            <label class="form-label">Tienda</label>
            <select id="selTienda" name="id_tienda" class="form-select" disabled required>
              <option value="" disabled selected>Seleccione…</option>
              <c:forEach items="${tiendas}" var="t">
                <option value="${t.idTienda}">${t.nombre}</option>
              </c:forEach>
            </select>
          </div>
        </div>

        <!-- Detalle -->
        <div class="table-responsive mb-2">
          <table id="tblDetalle" class="table table-bordered">
            <thead class="table-light">
              <tr><th>Empaque</th><th>Vendidas</th><th>Merma</th><th>Subtotal</th><th></th></tr>
            </thead>
            <tbody></tbody>
            <tfoot class="table-secondary">
              <tr><td colspan="3" class="text-end">Total</td><td id="tdTotal">0.00</td><td></td></tr>
            </tfoot>
          </table>
        </div>

        <button id="btnAddLinea" type="button"
                class="btn btn-outline-secondary w-100" disabled>
          + Agregar paquete
        </button>
      </div>

      <div class="modal-footer">
        <button id="btnSave" class="btn btn-primary" disabled>Guardar nota</button>
      </div>
    </form>
  </div></div>
</div>

<!-- ============ MODAL DETALLE NOTA ============ -->
<div id="modalDet" class="modal fade" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg"><div class="modal-content">
    <div class="modal-header">
      <h5 class="modal-title">Detalle nota <span id="mdFolio"></span></h5>
      <button class="btn-close" data-bs-dismiss="modal"></button>
    </div>
    <div class="modal-body">
      <p><strong>Tienda:</strong> <span id="mdTienda"></span></p>

      <div class="table-responsive">
        <table class="table table-bordered">
          <thead class="table-light">
            <tr><th>Empaque</th><th>Vendidas</th><th>Merma</th><th>Subtotal</th></tr>
          </thead>
          <tbody id="mdBody"></tbody>
          <tfoot class="table-secondary">
            <tr><td colspan="3" class="text-end">Total</td><td id="mdTotal">0.00</td></tr>
          </tfoot>
        </table>
      </div>
    </div>
    <div class="modal-footer">
      <a id="mdEditar" class="btn btn-outline-primary">
        <i class="bi bi-pencil-square"></i> Editar
      </a>
      <button class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
    </div>
  </div></div>
</div>

<!-- INVENTARIO JSON OCULTO -->
<script id="inventarioJson" type="application/json">${inventarioJson}</script>

<!-- Bootstrap bundle + JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/static/js/notas.js"></script>
</body>
</html>