<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Detalle de Nota de Venta</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet"/>
</head>
<body>
<div class="container py-4">
  <!-- Título y botón regresar -->
  <div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="text-primary fw-semibold">
      <i class="bi bi-receipt"></i> Detalle de Nota de Venta
    </h3>
    <a href="${pageContext.request.contextPath}/NotaVentaServlet?accion=listar"
       class="btn btn-outline-secondary btn-sm">
      <i class="bi bi-arrow-left"></i> Volver al listado
    </a>
  </div>

  <!-- Información general de la nota -->
  <div class="card shadow-sm mb-4 border-0">
    <div class="card-body">
      <div class="row mb-2">
        <div class="col-md-4">
          <strong>Folio:</strong> ${nota.folio}
        </div>
        <div class="col-md-4">
          <strong>Fecha:</strong>
          <%= (request.getAttribute("nota") != null && ((modelo.NotaVenta) request.getAttribute("nota")).getFecha() != null)
                ? ((modelo.NotaVenta) request.getAttribute("nota")).getFecha().format(formatter)
                : "" %>
        </div>
        <div class="col-md-4">
          <strong>Total:</strong>
          <span class="text-success">
            $ <fmt:formatNumber value="${nota.totalNota}" minFractionDigits="2" />
          </span>
        </div>
      </div>
      <div class="row">
        <div class="col-md-6"><strong>Tienda:</strong> ${nota.nombreTienda}</div>
        <div class="col-md-6"><strong>Repartidor:</strong> ${nota.nombreRepartidor}</div>
      </div>
    </div>
  </div>

  <!-- Alerta si no hay detalles -->
  <c:if test="${empty nota.detalles}">
    <div class="alert alert-warning">No hay detalles disponibles para esta nota.</div>
  </c:if>

  <!-- Tabla de detalles -->
  <c:if test="${not empty nota.detalles}">
    <div class="table-responsive">
      <table class="table table-bordered align-middle shadow-sm">
        <thead class="table-light text-center">
          <tr>
            <th>Producto</th>
            <th>Cantidad Vendida</th>
            <th>Merma</th>
            <th>Precio Unitario</th>
            <th>Total Línea</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="d" items="${nota.detalles}">
            <tr>
              <td>${d.nombreEmpaque}</td>
              <td class="text-center">${d.cantidadVendida}</td>
              <td class="text-center">${d.merma}</td>
              <td class="text-end text-muted">
                $ <fmt:formatNumber value="${d.precioUnitario}" minFractionDigits="2" />
              </td>
              <td class="text-end fw-bold text-success">
                $ <fmt:formatNumber value="${d.totalLinea}" minFractionDigits="2" />
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </c:if>
</div>
</body>
</html>
