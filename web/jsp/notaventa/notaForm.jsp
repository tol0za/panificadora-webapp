<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8"/>
  <title>Registrar Nota de Venta</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet"/>
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
<div class="container py-4">
  <h3 class="mb-4">Registrar Nota de Venta</h3>
  <form action="${ctx}/NotasVentaServlet" method="post">
    <input type="hidden" name="accion" value="guardar"/>
    <!-- Encabezado: Folio, Tienda, Repartidor -->
    <div class="row mb-3">
      <div class="col-md-3">
        <label>Folio</label>
        <input type="number" name="folio" class="form-control" readonly value="${folio}"/>
      </div>
      <div class="col-md-5">
        <label>Tienda</label>
        <select id="tiendaSelect" name="idTienda" class="form-select" required>
          <option value="">Seleccione...</option>
          <c:forEach var="t" items="${tiendas}">
            <option value="${t.idTienda}">${t.nombre}</option>
          </c:forEach>
        </select>
      </div>
      <div class="col-md-4">
        <label>Repartidor</label>
        <select id="repartidorSelect" name="idRepartidor" class="form-select" required>
          <option value="">Seleccione...</option>
          <c:forEach var="r" items="${repartidores}">
            <option value="${r.idRepartidor}">${r.nombreRepartidor} ${r.apellidoRepartidor}</option>
          </c:forEach>
        </select>
      </div>
    </div>
    <hr/>

    <!-- Tabla dinámica -->
    <h5>Detalle de Productos</h5>
    <table class="table table-bordered" id="tablaProductos">
      <thead class="table-light">
        <tr>
          <th>Empaque</th><th>Stock</th><th>Cant.</th><th>Merma</th><th>Precio</th><th>Total</th><th></th>
        </tr>
      </thead>
      <tbody id="detalleBody"></tbody>
    </table>

    <!-- Botones -->
    <button type="button" class="btn btn-outline-primary mb-3" onclick="agregarFila()">  
      <i class="bi bi-plus-circle"></i> Agregar Producto
    </button>
    <div>
      <button type="submit" class="btn btn-success"><i class="bi bi-save"></i> Guardar Nota</button>
      <a href="${ctx}/NotasVentaServlet" class="btn btn-secondary">Cancelar</a>
    </div>
  </form>
</div>

<script>
  let opcionesEmpaque = [];

  // Escuchar cambios en repartidor y tienda para cargar empaques
  $('#repartidorSelect').on('change', cargarEmpaques);
  $('#tiendaSelect').on('change', cargarEmpaques);

  function cargarEmpaques() {
    const idRep = $('#repartidorSelect').val();
    const idTienda = $('#tiendaSelect').val();
    opcionesEmpaque = [];
    $('#detalleBody').empty();
    if (!idRep || !idTienda) {
      return;
    }
    $.getJSON(`${ctx}/NotasVentaServlet`, { accion: 'obtenerEmpaques', idRepartidor: idRep, idTienda: idTienda }, function(data) {
      opcionesEmpaque = data || [];
      opcionesEmpaque.forEach(function(e) {
        crearFilaConEmpaque(e);
      });
    }).fail(function() {
      Swal.fire('Error','No se pudieron cargar los empaques','error');
    });
  }

  // 2) Función genérica para crear una fila dado un objeto empaque
  function crearFilaConEmpaque(e) {
    // Normalizar nombres de propiedades recibidas
    const idEmpaque = e.idEmpaque || e.id_empq || e.id_empaque || e.id || e.idEmpaques || '';
    const nombreEmpaque = e.nombreEmpaque || e.nombre_empaque || e.nombre || '';
    const precio = parseFloat(e.precioUnitario || e.precio_unitario || e.precio || 0);
    const stock = (typeof e.stock !== 'undefined') ? e.stock : '—';
    const idx = Date.now() + Math.random();
    const fila = `
      <tr id="row-${idx}">
        <td>
          <select name="idEmpaque[]" class="form-select" onchange="actualizarDatos('${idx}', this)">
            <option value="${idEmpaque}" data-stock="${stock}" data-precio="${precio}" selected>
              ${nombreEmpaque}
            </option>
          </select>
        </td>
        <td><span id="stock-${idx}">${stock}</span></td>
        <td><input type="number" name="cantidad[]" class="form-control" min="0" value="0"
                   oninput="calcularTotal('${idx}')"/></td>
        <td><input type="number" name="merma[]" class="form-control" min="0" value="0"
                   oninput="calcularTotal('${idx}')"/></td>
        <td><input type="text" name="precioUnitario[]" id="precio-${idx}" class="form-control" readonly
                   value="${precio.toFixed(2)}"/></td>
        <td><input type="text" id="total-${idx}" class="form-control" readonly/></td>
        <td><button type="button" class="btn btn-sm btn-danger" onclick="eliminarFila('${idx}')">
              <i class="bi bi-trash"></i>
            </button></td>
      </tr>`;
    $('#detalleBody').append(fila);
  }

  // 3) Permite agregar filas extra manualmente
  function agregarFila() {
    // Solo permite agregar filas si se han cargado empaques (repartidor y tienda seleccionados)
    if (opcionesEmpaque.length === 0) {
      Swal.fire('Atención','Selecciona primero un repartidor y una tienda','warning');
      return;
    }
    crearFilaConEmpaque(opcionesEmpaque[0]);
  }

  // 4) Al cambiar select de empaque en una fila existente
  function actualizarDatos(index, sel) {
    const opt = sel.selectedOptions[0];
    const stock = opt.dataset.stock || '—';
    const precio = opt.dataset.precio || 0;
    $(`#stock-${index}`).text(stock);
    $(`#precio-${index}`).val(parseFloat(precio).toFixed(2));
    calcularTotal(index);
  }

  // 5) Calcula total por línea
  function calcularTotal(index) {
    const cant = parseFloat($(`#row-${index} input[name='cantidad[]']`).val() || 0);
    const merma = parseFloat($(`#row-${index} input[name='merma[]']`).val() || 0);
    const precio = parseFloat($(`#precio-${index}`).val() || 0);
    const vendido = Math.max(cant - merma, 0);
    const totalLinea = vendido * precio;
    $(`#total-${index}`).val(totalLinea.toFixed(2));
  }

  function eliminarFila(index) {
    $(`#row-${index}`).remove();
  }
</script>
</body>
</html>
