<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Editar Salida</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
</head>
<body>
<div class="container py-4" style="max-width:400px;">
  <h5 class="mb-3 text-secondary">Editar artículo de salida</h5>
  <form action="${ctx}/SalidaServlet" method="post">
    <input type="hidden" name="accion" value="actualizar"/>
    <input type="hidden" name="idDistribucion" value="${salida.idDistribucion}"/>
    <div class="mb-3">
      <label>Repartidor</label>
      <input class="form-control form-control-sm" value="${salida.nombreRepartidor} ${salida.apellidoRepartidor}" readonly>
    </div>
    <div class="mb-3">
      <label>Empaque</label>
      <input class="form-control form-control-sm" value="${salida.nombreEmpaque}" readonly>
    </div>
    <div class="mb-3">
      <label>Cantidad</label>
      <input type="number" name="cantidad" value="${salida.cantidad}" class="form-control form-control-sm" min="1" required>
    </div>
    <div class="d-flex justify-content-between">
      <button type="submit" class="btn btn-success"><i class="bi bi-save"></i> Guardar</button>
      <a href="${ctx}/SalidaServlet" class="btn btn-secondary">Cancelar</a>
    </div>
  </form>
</div>
</body>
</html>
