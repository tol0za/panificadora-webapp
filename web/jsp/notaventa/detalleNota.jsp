<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8"/>
  <title>Detalle Nota de Venta</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
</head>
<body class="bg-light">
<div class="container py-4" style="max-width:800px;">
  <h3 class="mb-3">Detalle Nota de Venta <small class="text-muted">Folio ${nota.folio}</small></h3>
  <p>
    <strong>Repartidor:</strong> ${nota.nombreRepartidor} ${nota.apellidoRepartidor}<br/>
    <strong>Tienda:</strong> ${nota.nombreTienda}<br/>
    <strong>Fecha:</strong> <fmt:formatDate value="${nota.fechaNota}" pattern="dd/MM/yyyy HH:mm"/>
  </p>
  <table class="table table-bordered table-hover">
    <thead class="table-light">
      <tr>
        <th>Empaque</th>
        <th class="text-end">Vendido</th>
        <th class="text-end">Merma</th>
        <th class="text-end">Precio Unit.</th>
        <th class="text-end">Total Línea</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="det" items="${detalles}">
        <tr>
          <td>${det.nombreEmpaque}</td>
          <td class="text-end">${det.cantidadVendida}</td>
          <td class="text-end">${det.merma}</td>
          <td class="text-end"><fmt:formatNumber value="${det.precioUnitario}" type="currency"/></td>
          <td class="text-end"><fmt:formatNumber value="${det.totalLinea}" type="currency"/></td>
        </tr>
      </c:forEach>
    </tbody>
    <tfoot>
      <tr>
        <th colspan="4" class="text-end">Total Nota:</th>
        <th class="text-end">
          <fmt:formatNumber value="${totalGeneral}" type="currency"/>
        </th>
      </tr>
    </tfoot>
  </table>
  <div class="text-end">
    <a href="${ctx}/NotaVentaServlet?accion=listar" class="btn btn-secondary">
      <i class="bi bi-arrow-left"></i> Volver
    </a>
  </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
