<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <title>Registrar Salida</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>
<body>
<div class="container py-4">
  <h3 class="text-secondary mb-4">Registrar Nueva Salida</h3>

  <form action="${ctx}/SalidaServlet" method="post" class="border p-4 rounded bg-light">
    <div class="mb-3">
      <label for="idRepartidor" class="form-label">Repartidor</label>
      <select name="idRepartidor" id="idRepartidor" class="form-select" required>
        <option value="">Seleccione un repartidor</option>
        <c:forEach var="rep" items="${repartidores}">
          <option value="${rep.idRepartidor}">${rep.nombreRepartidor} ${rep.apellidoRepartidor}</option>
        </c:forEach>
      </select>
    </div>

    <div id="contenedorEmpaques">
      <div class="row align-items-end mb-3 empaque-row">
        <div class="col-md-6">
          <label class="form-label">Empaque</label>
          <select name="idEmpaque[]" class="form-select" required>
            <option value="">Seleccione un empaque</option>
            <c:forEach var="emp" items="${empaques}">
              <option value="${emp.idEmpaque}">
                ${emp.nombreEmpaque} (Stock: ${stockMap[emp.idEmpaque]})
              </option>
            </c:forEach>
          </select>
        </div>
        <div class="col-md-4">
          <label class="form-label">Cantidad</label>
          <input type="number" name="cantidad[]" class="form-control" min="1" required />
        </div>
        <div class="col-md-2">
          <button type="button" class="btn btn-danger btn-remove">X</button>
        </div>
      </div>
    </div>

    <div class="mb-3">
      <button type="button" class="btn btn-secondary" onclick="agregarFila()">+ Agregar otro empaque</button>
    </div>

    <div class="d-flex justify-content-between">
      <a href="${ctx}/SalidaServlet?accion=listar" class="btn btn-secondary">Cancelar</a>
      <button type="submit" class="btn btn-primary">Guardar Salida</button>
    </div>
  </form>
</div>

<script>
  function agregarFila() {
    const fila = document.querySelector('.empaque-row');
    const nuevaFila = fila.cloneNode(true);
    nuevaFila.querySelectorAll('input, select').forEach(el => {
      el.value = '';
    });
    document.getElementById('contenedorEmpaques').appendChild(nuevaFila);
    nuevaFila.querySelector('.btn-remove').addEventListener('click', function() {
      nuevaFila.remove();
    });
  }

  document.querySelectorAll('.btn-remove').forEach(btn => {
    btn.addEventListener('click', function() {
      this.closest('.empaque-row').remove();
    });
  });
</script>
</body>
</html>
