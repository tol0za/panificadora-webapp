<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Repartidores con Salidas</title>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600&display=swap" rel="stylesheet"/>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet"/>
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
  <style>
    body { font-family: 'Inter', sans-serif; background: #f8f9fa; }
  </style>
</head>
<body>
  <div class="container py-4" style="max-width:700px;">

    <h4 class="mb-4 text-secondary fw-semibold">
      Repartidores con salidas el
      <fmt:parseDate var="fd" value="${fechaSeleccionada}" pattern="yyyy-MM-dd"/>
      <fmt:formatDate value="${fd}" pattern="dd/MM/yyyy"/>
    </h4>

    <c:choose>
      <c:when test="${empty salidasPorRepartidor}">
        <div class="alert alert-info text-center">
          No hay salidas registradas este día.
        </div>
      </c:when>
      <c:otherwise>
        <table class="table table-bordered table-hover shadow-sm bg-white rounded">
          <thead class="table-light">
            <tr>
              <th class="text-start">Repartidor</th>
              <th class="text-center" style="width:300px;">Acción</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="entry" items="${salidasPorRepartidor}">
              <c:set var="rid" value="${entry.key}"/>
              <c:set var="list" value="${entry.value}"/>
              <tr>
                <td class="text-start align-middle">
                  <strong>
                    <c:out value="${list[0].nombreRepartidor}"/>
                    <c:out value="${list[0].apellidoRepartidor}"/>
                  </strong>
                </td>
                <td class="text-center align-middle">
                  <div class="d-flex justify-content-center gap-2">
                    <a href="${ctx}/SalidaServlet?accion=verDetalle&idRepartidor=${rid}&fecha=${fechaSeleccionada}"
                       class="btn btn-outline-primary btn-sm">
                      <i class="bi bi-eye"></i> 
                    </a>
                    <a href="${ctx}/SalidaServlet?accion=editarMultiple&idRepartidor=${rid}&fecha=${fechaSeleccionada}"
                       class="btn btn-sm btn-outline-success me-1">
                      <i class="bi bi-pencil-square"></i>
                    </a>
                    <button type="button"
                            class="btn btn-sm btn-outline-danger btn-eliminar" 
                            onclick="confirmarEliminarSalida('${ctx}','${rid}','${fechaSeleccionada}')">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>

    <div class="text-center mt-3">
      <a href="${ctx}/SalidaServlet?accion=calendario" class="btn btn-outline-primary">
        <i class="bi bi-calendar"></i> Volver al calendario
      </a>
    </div>

  </div>

  <!-- Alerta SweetAlert -->
  <c:if test="${not empty sessionScope.mensaje}">
    <script>
      Swal.fire({
        icon: '${sessionScope.mensaje.startsWith("Error") ? "error" : "success"}',
        title: '${sessionScope.mensaje.startsWith("Error") ? "Error" : "Listo"}',
        html: `${sessionScope.mensaje}`,
        confirmButtonText: 'Aceptar'
      });
    </script>
    <c:remove var="mensaje" scope="session"/>
  </c:if>

  <script>
    function confirmarEliminarSalida(ctx, idRepartidor, fecha) {
      Swal.fire({
        title: '¿Eliminar toda la salida?',
        text: "Se eliminarán todos los artículos de este repartidor en la fecha seleccionada.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#888',
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar'
      }).then((result) => {
        if (result.isConfirmed) {
          // Llama al servlet con los dos parámetros
          window.location = `${ctx}/SalidaServlet?accion=eliminarSalida`
                           + `&idRepartidor=${idRepartidor}`
                           + `&fecha=${fecha}`;
        }
      });
    }
  </script>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
