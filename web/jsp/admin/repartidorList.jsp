<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Repartidores</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>
<body class="bg-light">

<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="text-secondary">Listado de Repartidores</h3>
    <a href="${ctx}/RepartidorServlet?accion=nuevo" class="btn btn-success" draggable="false">
      <i class="bi bi-plus-circle" draggable="false"></i> Nuevo Repartidor
    </a>
  </div>

  <input type="text" class="form-control mb-3" id="buscarInput" placeholder="Buscar por nombre, apellido o teléfono...">

  <div class="table-responsive rounded shadow-sm">
    <table class="table table-bordered table-hover bg-white align-middle" id="tablaRepartidores">
      <thead class="table-light">
        <tr>
          <th>ID</th>
          <th>Nombre</th>
          <th>Apellido</th>
          <th>Teléfono</th>
          <th class="text-center">Acciones</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="r" items="${listaRepartidores}">
          <tr>
            <td>${r.idRepartidor}</td>
            <td>${r.nombreRepartidor}</td>
            <td>${r.apellidoRepartidor}</td>
            <td>${r.telefono}</td>
            <td class="text-center">
              <a href="${ctx}/RepartidorServlet?accion=editar&id=${r.idRepartidor}" class="btn btn-sm btn-outline-primary me-1" draggable="false">
                <i class="bi bi-pencil-square" draggable="false"></i>
              </a>
              <a href="${ctx}/RepartidorServlet?accion=eliminar&id=${r.idRepartidor}" class="btn btn-sm btn-outline-danger btn-eliminar"
                 data-id="${r.idRepartidor}" draggable="false">
                <i class="bi bi-trash" draggable="false"></i>
              </a>
            </td>
          </tr>
        </c:forEach>

        <!-- Mensaje si la lista viene vacía -->
        <c:if test="${empty listaRepartidores}">
          <tr>
            <td colspan="5" class="text-center text-muted">No hay repartidores registrados.</td>
          </tr>
        </c:if>
      </tbody>
    </table>
  </div>
</div>

<!-- Filtro de búsqueda -->
<script>
  const input = document.getElementById("buscarInput");
  const tabla = document.getElementById("tablaRepartidores").getElementsByTagName("tbody")[0];
  input.addEventListener("keyup", function () {
    const filtro = input.value.toLowerCase();
    Array.from(tabla.rows).forEach(row => {
      const texto = row.innerText.toLowerCase();
      row.style.display = texto.includes(filtro) ? "" : "none";
    });
  });
</script>

<!-- Confirmación de eliminación -->
<script>
  document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.btn-eliminar').forEach(function(btn) {
      btn.addEventListener('click', function(e) {
        e.preventDefault();
        const url = this.getAttribute('href');
        Swal.fire({
          title: '¿Estás seguro?',
          text: "¡No podrás revertir esto!",
          icon: 'warning',
          showCancelButton: true,
          confirmButtonColor: '#3085d6',
          cancelButtonColor: '#d33',
          confirmButtonText: 'Sí, eliminar'
        }).then((result) => {
          if (result.isConfirmed) {
            window.location.href = url;
          }
        });
      });
    });
  });
</script>

<!-- Mensajes SweetAlert2 por acción -->
<jsp:include page="/includes/alerta.jsp" />

<!-- Alerta de acceso denegado -->
<c:if test="${sessionScope.accesoDenegado}">
  <script>
    Swal.fire({
      icon: 'error',
      title: 'Acceso denegado',
      text: 'No tienes permiso para acceder a esta sección.',
      timer: 1000,
      showConfirmButton: false
    });
  </script>
  <c:remove var="accesoDenegado" scope="session"/>
</c:if>

</body>
</html>
