<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>Notas – ${repartidor.nombreRepartidor}</title>
  <!-- Bootstrap + Bootstrap-icons -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
</head>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<%-- Calcular stock total disponible del repartidor para hoy --%>
<c:set var="totalRestante" value="0" />
<c:forEach items="${inventario}" var="i">
  <c:set var="totalRestante" value="${totalRestante + i.restante}" />
</c:forEach>
<c:set var="puedeNueva" value="${not rutaCerrada and (totalRestante gt 0)}" />

<body data-ctx="${ctx}"
      data-ruta-cerrada="${rutaCerrada}"
      data-stock-total="${totalRestante}">
<div class="container-fluid py-3">

  <!-- CABECERA -->
  <div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
    <h4 class="mb-2">
      Notas de venta – ${repartidor.nombreRepartidor} (${hoy})
    </h4>
    <div class="d-flex gap-2 align-items-center">
      <c:if test="${not empty listaNotas}">
        <a class="btn btn-outline-success"
           href="${ctx}/NotaVentaServlet?inFrame=1&amp;accion=imprimirNotasDia&amp;id_repartidor=${repartidor.idRepartidor}">
          <i class="bi bi-printer"></i> Imprimir día
        </a>
      </c:if>

      <c:choose>
        <c:when test="${puedeNueva}">
          <button id="btnNueva" class="btn btn-success">
            <i class="bi bi-plus-circle"></i> Nueva nota
          </button>
        </c:when>
        <c:otherwise>
          <button id="btnNueva" class="btn btn-success" disabled
                  title="${rutaCerrada ? 'La ruta está cerrada. Reábrela para capturar notas.' : 'Sin stock disponible en el repartidor.'}">
            <i class="bi bi-plus-circle"></i> Nueva nota
          </button>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <!-- FLASH -->
  <c:if test="${not empty sessionScope.flashMsg}">
    <c:set var="alertClass"
           value="${fn:contains(sessionScope.flashMsg,'Stock insuficiente')
                    or fn:contains(sessionScope.flashMsg,'Error')
                    ? 'alert-danger' : 'alert-success'}"/>
    <div class="alert ${alertClass} alert-dismissible fade show mb-3" role="alert">
      <c:out value="${sessionScope.flashMsg}" escapeXml="false"/>
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <c:remove var="flashMsg" scope="session"/>
  </c:if>

  <!-- INVENTARIO DISPONIBLE -->
  <div class="card card-body mb-4">
    <div class="d-flex justify-content-between align-items-center">
      <h6 class="card-title mb-2">Inventario disponible</h6>
      <span class="badge bg-${totalRestante > 0 ? 'success' : 'secondary'}">
        Total disponible: ${totalRestante}
      </span>
    </div>
    <div class="table-responsive">
      <table class="table table-sm mb-0">
        <thead class="table-light">
          <tr><th>Empaque</th><th class="text-end">Precio</th><th class="text-end">Restante</th></tr>
        </thead>
        <tbody>
          <c:forEach items="${inventario}" var="i">
            <tr>
              <td>${i.nombre}</td>
              <td class="text-end">${i.precio}</td>
              <td class="text-end">${i.restante}</td>
            </tr>
          </c:forEach>
          <c:if test="${empty inventario}">
            <tr><td colspan="3" class="text-center text-muted">Sin registros</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </div>

  <!-- LISTA DE NOTAS -->
  <div class="card card-body">
    <h6 class="card-title mb-2">Notas registradas</h6>
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
                <!-- Ver (siempre disponible) -->
                <a href="#" class="btn btn-sm btn-outline-info me-1 btn-det"
                   data-idNota="${n.idNotaVenta}"
                   data-folio ="${n.folio}"
                   data-tienda="${n.nombreTienda}">
                  <i class="bi bi-eye"></i>
                </a>

                <!-- Editar: sólo cuando ruta ABIERTA -->
                <c:choose>
                  <c:when test="${rutaCerrada}">
                    <button class="btn btn-sm btn-outline-primary me-1" disabled
                            title="Reabre la ruta para editar esta nota">
                      <i class="bi bi-pencil-square"></i>
                    </button>
                  </c:when>
                  <c:otherwise>
                    <a href="${ctx}/NotaVentaServlet?inFrame=1&accion=editarNota&id=${n.idNotaVenta}"
                       class="btn btn-sm btn-outline-primary me-1">
                      <i class="bi bi-pencil-square"></i>
                    </a>
                  </c:otherwise>
                </c:choose>

                <!-- Eliminar: sólo cuando ruta ABIERTA -->
                <c:choose>
                  <c:when test="${rutaCerrada}">
                    <button class="btn btn-sm btn-outline-danger" disabled
                            title="Reabre la ruta para eliminar esta nota">
                      <i class="bi bi-trash"></i>
                    </button>
                  </c:when>
                  <c:otherwise>
                    <a href="${ctx}/NotaVentaServlet?inFrame=1&accion=eliminarNota&id=${n.idNotaVenta}"
                       class="btn btn-sm btn-outline-danger"
                       onclick="return confirm('¿Eliminar nota?');">
                      <i class="bi bi-trash"></i>
                    </a>
                  </c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty listaNotas}">
            <tr><td colspan="4" class="text-center text-muted">Sin notas</td></tr>
          </c:if>
        </tbody>
        <tfoot class="table-secondary fw-semibold">
          <tr><td colspan="2">Total día</td><td>${totalDia}</td><td></td></tr>
        </tfoot>
      </table>
    </div>
  </div>

  <!-- CERRAR / REABRIR RUTA -->
  <div class="mt-4">
    <c:choose>
      <c:when test="${not rutaCerrada}">
        <form class="d-inline" method="post" action="${ctx}/NotaVentaServlet">
          <input type="hidden" name="inFrame"       value="1"/>
          <input type="hidden" name="accion"        value="cerrarRuta"/>
          <input type="hidden" name="id_repartidor" value="${repartidor.idRepartidor}"/>
          <button type="submit" class="btn btn-warning"
                  onclick="return confirm('¿Cerrar ruta y devolver sobrante a bodega?');">
            <i class="bi bi-box-arrow-in-left"></i> Cerrar ruta y devolver sobrante
          </button>
        </form>
      </c:when>
      <c:otherwise>
        <form class="d-inline" method="post" action="${ctx}/NotaVentaServlet">
          <input type="hidden" name="inFrame"       value="1"/>
          <input type="hidden" name="accion"        value="reabrirRuta"/>
          <input type="hidden" name="id_repartidor" value="${repartidor.idRepartidor}"/>
          <button type="submit" class="btn btn-outline-warning"
                  onclick="return confirm('¿Reabrir la ruta para seguir capturando notas?');">
            <i class="bi bi-arrow-counterclockwise"></i> Reabrir ruta
          </button>
        </form>
      </c:otherwise>
    </c:choose>
  </div>

</div>

<!-- ============ MODAL NUEVA NOTA ============ -->
<div id="modalNota" class="modal fade" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg"><div class="modal-content">
    <form id="formNota" action="${ctx}/NotaVentaServlet" method="post">
      <input type="hidden" name="accion"        value="guardarNota">
      <input type="hidden" name="id_repartidor" value="${repartidor.idRepartidor}">
      <input type="hidden" id="lineas" name="lineas">
      <div class="modal-header">
        <h5 class="modal-title">Nueva nota</h5>
        <button class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="row g-2 mb-3">
          <div class="col-md-4">
            <label class="form-label">Folio</label>
            <input id="inpFolio" name="folio" required class="form-control">
            <div id="folioHelpBad"  class="form-text text-danger  d-none">El folio ya existe</div>
            <div id="folioHelpGood" class="form-text text-success d-none">Folio disponible ✓</div>
          </div>
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

<!-- JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/static/js/notas.js"></script>

</body>
</html>
