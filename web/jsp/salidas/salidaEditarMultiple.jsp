<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Editar Salida (Múltiple)</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css"/>
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>
<body>
<div class="container py-4" style="max-width: 650px;">
  <h4 class="mb-3 text-secondary">
    Editar salida de <strong>${nombreRepartidor}</strong> el
    <fmt:parseDate var="fd" value="${fechaSeleccionada}" pattern="yyyy-MM-dd"/>
    <fmt:formatDate value="${fd}" pattern="dd/MM/yyyy"/>
  </h4>

  <!-- Form principal para actualizar múltiples -->
  <form id="formEditarMultiple" action="${ctx}/SalidaServlet" method="post">
    <input type="hidden" name="accion" value="actualizarMultiple"/>
    <input type="hidden" name="idRepartidor" value="${idRepartidor}"/>
    <input type="hidden" name="fecha" value="${fechaSeleccionada}"/>

    <table class="table table-sm table-bordered align-middle mb-3">
      <thead>
        <tr>
          <th>Empaque</th>
          <th>Cantidad</th>
          <th>Acción</th>
        </tr>
      </thead>
      <tbody>
        <c:choose>
          <c:when test="${empty detalles}">
            <tr>
              <td colspan="3" class="text-center text-muted">No hay artículos en esta salida.</td>
            </tr>
          </c:when>
          <c:otherwise>
            <c:forEach var="s" items="${detalles}">
              <tr>
                <td>${s.nombreEmpaque}</td>
                <td>
                  <input type="hidden" name="idDistribucion[]" value="${s.idDistribucion}"/>
                  <input type="number" class="form-control form-control-sm" 
                         name="cantidad[]" value="${s.cantidad}" min="1" required 
                         style="max-width:80px;">
                </td>
                <td>
                  <!-- Form individual para eliminar artículo -->
                  <form id="elimForm${s.idDistribucion}" action="${ctx}/SalidaServlet" method="post" class="d-inline">
                    <input type="hidden" name="accion" value="eliminarArticulo"/>
                    <input type="hidden" name="idRepartidor" value="${idRepartidor}"/>
                    <input type="hidden" name="fecha" value="${fechaSeleccionada}"/>
                    <input type="hidden" name="idDistribucion" value="${s.idDistribucion}"/>
                    <button type="button"
                            class="btn btn-outline-danger btn-sm"
                            onclick="confirmarEliminarArticuloForm('elimForm${s.idDistribucion}')">
                      <i class="bi bi-trash"></i>
                    </button>
                  </form>
                </td>
              </tr>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </tbody>
    </table>

    <div class="d-flex justify-content-between">
      <button type="submit" class="btn btn-success" 
              <c:if test="${empty detalles}">disabled</c:if>>
        <i class="bi bi-save"></i> Guardar cambios
      </button>
      <a href="${ctx}/SalidaServlet?accion=verDia&fecha=${fechaSeleccionada}" class="btn btn-secondary">
        <i class="bi bi-arrow-left"></i> Volver a repartidores
      </a>
    </div>
  </form>
</div>

<!-- Mensajes -->
<c:if test="${not empty sessionScope.mensaje}">
  <script>
    Swal.fire({ icon: 'success', title: 'Listo', html: `${sessionScope.mensaje}`, confirmButtonText: 'Aceptar' });
  </script>
  <c:remove var="mensaje" scope="session"/>
</c:if>
<c:if test="${not empty requestScope.mensajeError}">
  <script>
    Swal.fire({ icon: 'error', title: 'Error', html: `${requestScope.mensajeError}`, confirmButtonText: 'Aceptar' });
  </script>
</c:if>

<script>
// Función que confirma y envía el form de eliminación
function confirmarEliminarArticuloForm(formId) {
  Swal.fire({
    title: '¿Eliminar artículo?',
    text: "Esta acción no se puede deshacer.",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    cancelButtonColor: '#888',
    confirmButtonText: 'Sí, eliminar',
    cancelButtonText: 'Cancelar'
  }).then((result) => {
    if (result.isConfirmed) {
      document.getElementById(formId).submit();
    }
  });
}
</script>
</body>
</html>
