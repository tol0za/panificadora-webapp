<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Listado de Salidas</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css"/>
</head>
<body>
<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="mb-0 text-secondary"><i class="bi bi-card-list"></i> Listado General de Salidas</h4>
    <div>
      <a href="${ctx}/SalidaServlet?accion=calendario" class="btn btn-outline-secondary btn-sm"><i class="bi bi-calendar-week"></i> Calendario</a>
      <a href="${ctx}/SalidaServlet?accion=nuevo" class="btn btn-success btn-sm"><i class="bi bi-plus-circle"></i> Nueva Salida</a>
    </div>
  </div>
  <c:if test="${not empty sessionScope.mensaje}">
    <div class="alert alert-success">${sessionScope.mensaje}</div>
    <c:remove var="mensaje" scope="session"/>
  </c:if>
  <table class="table table-sm table-bordered table-hover align-middle">
    <thead>
      <tr>
        <th>Repartidor</th>
        <th>Empaque</th>
        <th>Cantidad</th>
        <th>Fecha</th>
        <th>Acciones</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="s" items="${salidas}">
        <tr>
          <td>${s.nombreRepartidor} ${s.apellidoRepartidor}</td>
          <td>${s.nombreEmpaque}</td>
          <td>${s.cantidad}</td>
          <td><fmt:formatDate value="${s.fechaDistribucionDate}" pattern="dd/MM/yyyy HH:mm:ss"/></td>
          <td>
            <a href="${ctx}/SalidaServlet?accion=editar&id=${s.idDistribucion}" class="btn btn-sm btn-outline-primary"><i class="bi bi-pencil"></i></a>
            <a href="${ctx}/SalidaServlet?accion=eliminar&id=${s.idDistribucion}" class="btn btn-sm btn-outline-danger"
              onclick="return confirm('¿Eliminar este registro?')"><i class="bi bi-trash"></i></a>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>
</body>
</html>
