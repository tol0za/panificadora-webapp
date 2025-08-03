<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>Editar nota</title>
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body data-ctx="${pageContext.request.contextPath}">
<div class="container py-3">
  <h4>Editar nota folio ${nota.folio}</h4>

  <form id="formEdit"
        action="${pageContext.request.contextPath}/NotaVentaServlet"
        method="post">
    <input type="hidden" name="accion"  value="actualizarNota">
    <input type="hidden" name="id_nota" value="${nota.idNota}">

    <div class="row g-2 mb-3">
      <div class="col-md-4">
        <label class="form-label">Folio</label>
        <input required class="form-control"
               name="folio"
               value="${nota.folio}">
      </div>

      <div class="col-md-8">
        <label class="form-label">Tienda</label>
        <!-- ‼️ name=id_tienda para que el servlet lo reciba -->
        <select required name="id_tienda" class="form-select">
          <c:forEach items="${tiendas}" var="t">
            <option value="${t.idTienda}"
                    ${t.idTienda == nota.idTienda ? 'selected' : ''}>
              ${t.nombre}
            </option>
          </c:forEach>
        </select>
      </div>
    </div>

    <!-- (Si más adelante editas líneas, iría una tabla aquí) -->

    <button type="submit" class="btn btn-primary">Actualizar</button>
  </form>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
