<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>Editar nota</title>
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body data-ctx="${pageContext.request.contextPath}">
<div class="container py-3">

  <h4 class="mb-4">Editar nota – folio ${nota.folio}</h4>

  <form id="formEdit"
        action="${pageContext.request.contextPath}/NotaVentaServlet"
        method="post">
    <input type="hidden" name="accion"  value="actualizarNota">
    <input type="hidden" name="id_nota" value="${nota.idNotaVenta}">
    <input type="hidden" id="lineas"    name="lineas">

    <!-- Encabezado -->
    <div class="row g-2 mb-3">
      <div class="col-md-4">
        <label class="form-label">Folio</label>
        <input required class="form-control" name="folio" value="${nota.folio}">
      </div>
      <div class="col-md-8">
        <label class="form-label">Tienda</label>
        <select required name="id_tienda" class="form-select">
          <c:forEach items="${tiendas}" var="t">
            <option value="${t.idTienda}"
                    ${t.idTienda == nota.idTienda ? 'selected' : ''}>
              ${t.nombre}
            </option>
          </c:forEach>
        </select>
      </div>
    </div>

    <!-- Detalle -->
    <div class="table-responsive mb-3">
      <table class="table table-bordered" id="tblDet">
        <thead class="table-light">
          <tr><th>Empaque</th><th>Vendidas</th><th>Merma</th><th>Subtotal</th><th></th></tr>
        </thead>
        <tbody>
          <!-- líneas existentes -->
          <c:forEach items="${detalle}" var="d">
            <tr data-id="${d.idEmpaque}"
                data-dist="${d.idDistribucion}"
                data-price="${d.precioUnitario}">
              <td>${d.nombreEmpaque}</td>
              <td><input type="number" class="form-control qtyV" min="0"
                         value="${d.cantidadVendida}"></td>
              <td><input type="number" class="form-control qtyM" min="0"
                         value="${d.merma}"></td>
              <td class="sub">
                <fmt:formatNumber value="${d.totalLinea}" type="number"
                                  minFractionDigits="2" maxFractionDigits="2"/>
              </td>
              <td><button type="button"
                          class="btn btn-link text-danger p-0">✕</button></td>
            </tr>
          </c:forEach>
        </tbody>
        <tfoot class="table-secondary">
          <tr><td colspan="3" class="text-end">Total</td>
              <td id="tdTotal">
                <fmt:formatNumber value="${nota.total}" type="number"
                                  minFractionDigits="2" maxFractionDigits="2"/>
              </td><td></td></tr>
        </tfoot>
      </table>
    </div>

    <button type="button" class="btn btn-outline-secondary w-100 mb-3"
            id="btnAdd">+ Agregar paquete</button>

    <button class="btn btn-primary">Actualizar nota</button>
    <a class="btn btn-secondary ms-2"
       href="${pageContext.request.contextPath}/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=${nota.idRepartidor}">
       Cancelar
    </a>
  </form>
</div>

<!-- JSON inventario -->
<script id="invJson" type="application/json">${inventarioJson}</script>

<!-- Bootstrap & script -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/static/js/editNota.js?v=${System.currentTimeMillis()}"></script>
</body>
</html>
