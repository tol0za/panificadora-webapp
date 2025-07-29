<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Formulario Tienda</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
</head>
<body>
<div class="container py-4">
  <h3 class="mb-4 text-secondary">
    <c:choose>
      <c:when test="${accion eq 'editar'}">Editar Tienda</c:when>
      <c:otherwise>Nueva Tienda</c:otherwise>
    </c:choose>
  </h3>

  <form action="${ctx}/CatalogoTiendaServlet" method="post" class="needs-validation" novalidate>
    <input type="hidden" name="accion" value="${accion}" />
    <input type="hidden" name="idTienda" value="${tienda.idTienda}" />

    <div class="mb-3">
      <label for="nombre" class="form-label">Nombre de la Tienda</label>
      <input type="text" class="form-control" name="nombre" id="nombre" value="${tienda.nombre}" required maxlength="100">
      <div class="invalid-feedback">Ingresa el nombre de la tienda.</div>
    </div>

    <div class="mb-3">
      <label for="direccion" class="form-label">Dirección</label>
      <input type="text" class="form-control" name="direccion" id="direccion" value="${tienda.direccion}" required maxlength="150">
      <div class="invalid-feedback">Ingresa la dirección de la tienda.</div>
    </div>

    <div class="mb-3">
      <label for="telefono" class="form-label">Teléfono</label>
      <input type="tel" class="form-control" name="telefono" id="telefono" value="${tienda.telefono}" required maxlength="20" pattern="[0-9]{7,20}">
      <div class="invalid-feedback">Ingresa un teléfono válido (solo números).</div>
    </div>

    <div class="d-flex justify-content-between">
      <a href="${ctx}/CatalogoTiendaServlet?accion=listar" class="btn btn-secondary">
        <i class="bi bi-arrow-left-circle"></i> Cancelar
      </a>
      <button type="submit" class="btn btn-primary">
        <i class="bi bi-save"></i> Guardar
      </button>
    </div>
  </form>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
  (() => {
    'use strict';
    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
      form.addEventListener('submit', event => {
        if (!form.checkValidity()) {
          event.preventDefault();
          event.stopPropagation();
        }
        form.classList.add('was-validated');
      }, false);
    });
  })();
</script>

<jsp:include page="/includes/alerta.jsp" />
</body>
</html>
