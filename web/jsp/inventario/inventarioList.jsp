<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <title>Inventario</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>
<body>
<div class="container py-4">
  <h3 class="mb-4">Registro Rápido de Movimientos de Inventario</h3>

  <form id="formInventario"
        action="${ctx}/InventarioServlet" method="post"
        class="row g-3 mb-4 needs-validation"
        novalidate>

    <!-- Empaque + Stock Display -->
    <div class="col-md-5">
      <label for="selectEmpaque" class="form-label">Empaque</label>
      <select name="idEmpaque" id="selectEmpaque" class="form-select" required>
        <option value="" disabled selected>Selecciona un empaque</option>
        <c:forEach var="empaque" items="${empaques}">
          <c:set var="stockVal" value="${stockMap[empaque.idEmpaque] != null ? stockMap[empaque.idEmpaque] : 0}" />
          <option value="${empaque.idEmpaque}" data-stock="${stockVal}">
            ${empaque.nombreEmpaque}
          </option>
        </c:forEach>
      </select>
      <div class="invalid-feedback">Selecciona un empaque.</div>
      <div class="form-text text-secondary" id="stockDisplay">
        Total de piezas disponibles: —
      </div>
    </div>

    <!-- Cantidad -->
    <div class="col-md-2">
      <label for="inputCantidad" class="form-label">Cantidad</label>
      <input type="number" id="inputCantidad" name="cantidad"
             min="1" class="form-control"
             placeholder="Cantidad" required />
      <div class="invalid-feedback">Ingresa una cantidad válida.</div>
    </div>

    <!-- Motivo -->
    <div class="col-md-3">
      <label for="selectMotivo" class="form-label">Motivo</label>
      <select name="motivo" id="selectMotivo"
              class="form-select" required>
        <option value="Ingreso de Mercancia">Ingreso de Mercancía</option>
        <option value="Salida de Mercancia">Salida de Mercancía</option>
        <option value="Merma de Mercancia">Merma de Mercancía</option>
      </select>
      <div class="invalid-feedback">Selecciona un motivo.</div>
    </div>

    <!-- Botón -->
    <div class="col-md-2 d-grid align-items-end">
      <button type="submit" class="btn btn-primary">Registrar</button>
    </div>
  </form>

  <!-- Movimientos de Inventario -->
  <h3>Movimientos de Inventario</h3>
  <table class="table table-striped table-hover mb-2">
    <thead class="table-light">
      <tr>
        <th>Empaque</th>
        <th>Cantidad</th>
        <th>Fecha</th>
        <th>Motivo</th>
        <th>Cantidad Actual</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="mov" items="${movimientos}">
        <tr>
          <td>${mov.nombreEmpaque}</td>
          <td>${mov.cantidad}</td>
          <td><fmt:formatDate value="${mov.fechaDate}" pattern="dd/MM/yyyy HH:mm:ss"/></td>
          <td>${mov.motivo}</td>
          <td>${mov.cantidadActual}</td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <!-- Total stock general -->
  <c:set var="totalStock" value="0" />
  <c:forEach var="entry" items="${stockMap}">
    <c:set var="totalStock" value="${totalStock + entry.value}" />
  </c:forEach>
  <div class="fw-bold">Total stock general: ${totalStock} unidades</div>
</div>

<!-- Validación Bootstrap -->
<script>
  (() => {
    'use strict';
    document.querySelectorAll('.needs-validation').forEach(form => {
      form.addEventListener('submit', e => {
        if (!form.checkValidity()) {
          e.preventDefault();
          e.stopPropagation();
        }
        form.classList.add('was-validated');
      }, false);
    });
  })();
</script>

<!-- Mostrar stock dinámicamente y validar cantidad -->
<script>
  document.addEventListener("DOMContentLoaded", function() {
    var selectEmpaque = document.getElementById("selectEmpaque");
    var stockDisplay  = document.getElementById("stockDisplay");

    function actualizarStock() {
      var opt   = selectEmpaque.selectedOptions[0];
      var stock = opt ? opt.getAttribute("data-stock") : null;
      stockDisplay.innerHTML = (stock !== null && stock !== "")
        ? "Total de piezas disponibles: <strong>" + stock + "</strong>"
        : "Total de piezas disponibles: —";
    }

    selectEmpaque.addEventListener("change", actualizarStock);
    actualizarStock();

    // Validar cantidad contra stock al enviar el formulario (para salidas/mermas)
    document.getElementById("formInventario").addEventListener("submit", function(e) {
      var motivo   = document.getElementById("selectMotivo").value.toLowerCase();
      var cantidad = +document.getElementById("inputCantidad").value;
      var opt      = selectEmpaque.selectedOptions[0];
      var stock    = +((opt && opt.getAttribute("data-stock")) || 0);

      if ((motivo.includes("salida") || motivo.includes("merma")) && cantidad > stock) {
        e.preventDefault();
        Swal.fire({
          icon: 'error',
          title: 'Inventario insuficiente',
          text: "Solo hay " + stock + " unidades disponibles.",
          confirmButtonText: 'Aceptar'
        });
      }
    });
  });
</script>

<!-- 🔔 Alerta post-movimiento SOLO de Inventario -->
<c:if test="${not empty sessionScope.flashInv}">
  <script>
    document.addEventListener('DOMContentLoaded', function(){
      Swal.fire({
        icon: 'success',
        title: 'Movimiento registrado',
        html: `${sessionScope.flashInv}`,
        confirmButtonText: 'Aceptar'
      });
    });
  </script>
  <c:remove var="flashInv" scope="session"/>
</c:if>

<!-- 🧹 Limpieza defensiva de flashes AJENOS (por si llegaste navegando) -->
<c:remove var="flashSalida" scope="session"/>
<c:remove var="flashNotas"  scope="session"/>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
