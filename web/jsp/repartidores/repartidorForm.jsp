<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>
    <c:choose>
      <c:when test="${repartidor != null}">Editar</c:when>
      <c:otherwise>Nuevo</c:otherwise>
    </c:choose> Repartidor
  </title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>
<body class="bg-light">

<div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-md-6 bg-white p-4 rounded shadow-sm">

      <h4 class="mb-4 text-secondary">
        <c:choose>
          <c:when test="${repartidor != null}">Editar Repartidor</c:when>
          <c:otherwise>Nuevo Repartidor</c:otherwise>
        </c:choose>
      </h4>

      <form action="${ctx}/RepartidorServlet" method="post">
        <c:if test="${repartidor != null}">
          <input type="hidden" name="idRepartidor" value="${repartidor.idRepartidor}" />
        </c:if>

        <div class="mb-3">
          <label for="nombreRepartidor" class="form-label">Nombre</label>
          <input type="text" class="form-control" name="nombreRepartidor" id="nombreRepartidor" required
                 value="${repartidor != null ? repartidor.nombreRepartidor : ''}">
        </div>

        <div class="mb-3">
          <label for="apellidoRepartidor" class="form-label">Apellido</label>
          <input type="text" class="form-control" name="apellidoRepartidor" id="apellidoRepartidor" required
                 value="${repartidor != null ? repartidor.apellidoRepartidor : ''}">
        </div>

        <div class="mb-3">
          <label for="telefono" class="form-label">Teléfono</label>
          <input type="text" class="form-control" name="telefono" id="telefono" required
                 value="${repartidor != null ? repartidor.telefono : ''}">
        </div>

        <div class="d-flex justify-content-between">
          <a href="${ctx}/RepartidorServlet" class="btn btn-secondary">Cancelar</a>
          <button type="submit" class="btn btn-primary">
            <c:choose>
              <c:when test="${repartidor != null}">Actualizar</c:when>
              <c:otherwise>Guardar</c:otherwise>
            </c:choose>
          </button>
        </div>
      </form>

    </div>
  </div>
</div>

<!-- Alerta flotante si acceso denegado -->
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
