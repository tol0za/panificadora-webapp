<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <title><c:choose><c:when test="${empaque != null}">Editar</c:when><c:otherwise>Nuevo</c:otherwise></c:choose> Empaque</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
</head>
<body class="bg-light">

<div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-md-6 bg-white p-4 rounded shadow-sm">

      <h4 class="mb-4 text-secondary">
        <c:choose>
          <c:when test="${empaque != null}">Editar Empaque</c:when>
          <c:otherwise>Nuevo Empaque</c:otherwise>
        </c:choose>
      </h4>

      <form action="${ctx}/CatalogoEmpaqueServlet" method="post">
        <c:if test="${empaque != null}">
          <input type="hidden" name="idEmpaque" value="${empaque.idEmpaque}" />
        </c:if>

        <div class="mb-3">
          <label for="nombreEmpaque" class="form-label">Nombre del Empaque</label>
          <input type="text" name="nombreEmpaque" id="nombreEmpaque" class="form-control" required
                 value="${empaque != null ? empaque.nombreEmpaque : ''}" />
        </div>

        <div class="mb-3">
          <label for="precioUnitario" class="form-label">Precio Unitario</label>
          <input type="number" name="precioUnitario" id="precioUnitario" class="form-control" step="0.01" required
                 value="${empaque != null ? empaque.precioUnitario : ''}" />
        </div>

        <div class="d-flex justify-content-between">
          <a href="${ctx}/CatalogoEmpaqueServlet" class="btn btn-secondary">Cancelar</a>
          <button type="submit" class="btn btn-primary">
            <c:choose>
              <c:when test="${empaque != null}">Actualizar</c:when>
              <c:otherwise>Guardar</c:otherwise>
            </c:choose>
          </button>
        </div>
      </form>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<c:if test="${sessionScope.accesoDenegado}">
  <script>
    Swal.fire({
      icon: 'error',
      title: 'Acceso denegado',
      text: 'No tienes permiso para acceder a este módulo',
      showConfirmButton: false,
      timer: 1500
    });
    setTimeout(() => {
      window.location.href = '${ctx}/jsp/bienvenida.jsp';
    }, 1500);
  </script>
  <c:remove var="accesoDenegado" scope="session" />
</c:if>

</body>
</html>
