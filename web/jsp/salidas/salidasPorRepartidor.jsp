<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Detalle de salida</title>
  <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css"/>
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
  <style>
    body { font-family: 'Montserrat', Arial, sans-serif; }
  </style>
</head>
<body class="bg-light">
<div class="container py-4" style="max-width: 580px;">
  <div class="card shadow-sm border-0">
    <div class="card-body">
      <h4 class="mb-4 text-secondary fw-semibold">
        Detalles de salida de <strong>${nombreRepartidor}</strong> el
        <fmt:parseDate var="fd" value="${fechaSeleccionada}" pattern="yyyy-MM-dd"/>
        <fmt:formatDate value="${fd}" pattern="dd/MM/yyyy"/>
      </h4>

      <table class="table table-hover table-bordered align-middle mb-4">
        <thead class="table-light">
          <tr>
            <th><i class="bi bi-box"></i> Empaque</th>
            <th><i class="bi bi-123"></i> Cantidad</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${empty detalles}">
              <tr>
                <td colspan="2" class="text-center text-muted fst-italic">No hay artículos en esta salida.</td>
              </tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="s" items="${detalles}">
                <tr>
                  <td>${s.nombreEmpaque}</td>
                  <td>${s.cantidad}</td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>

      <div class="d-flex justify-content-between">
        <a href="${ctx}/SalidaServlet?accion=editarMultiple&idRepartidor=${param.idRepartidor}&fecha=${fechaSeleccionada}"
           class="btn btn-primary">
          <i class="bi bi-pencil"></i> Editar todos los artículos
        </a>
        <a href="${ctx}/SalidaServlet?accion=verDia&fecha=${fechaSeleccionada}" class="btn btn-secondary">
          <i class="bi bi-arrow-left"></i> Volver a repartidores
        </a>
      </div>
    </div>
  </div>
</div>

<c:if test="${not empty sessionScope.mensaje}">
  <script>
    Swal.fire({
      icon: 'success',
      title: 'Listo',
      html: `${sessionScope.mensaje}`,
      confirmButtonText: 'Aceptar'
    });
  </script>
  <c:remove var="mensaje" scope="session"/>
</c:if>
</body>
</html>
