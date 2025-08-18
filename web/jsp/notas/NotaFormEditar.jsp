<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>Editar nota</title>
  <!-- Bootstrap + Bootstrap-icons -->
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
</head>
<body data-ctx="${pageContext.request.contextPath}">
<div class="container py-3">

  <h4 class="mb-4">Editar nota – folio ${nota.folio}</h4>

  <form id="formEdit"
        action="${pageContext.request.contextPath}/NotaVentaServlet"
        method="post">
    <input type="hidden" name="accion"  value="actualizarNota">
    <input type="hidden" name="id_nota" value="${nota.idNotaVenta}">
    <input type="hidden" id="lineas"    name="lineas"><%-- tu JS existente lo rellena --%>

    <!-- Encabezado -->
    <div class="row g-2 mb-3">
      <div class="col-md-4">
        <label class="form-label">Folio</label>
        <input required class="form-control" name="folio" value="${nota.folio}">
      </div>
      <div class="col-md-8">
        <label class="form-label">Tienda</label>
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

    <!-- Detalle -->
    <div class="table-responsive mb-3">
      <table class="table table-bordered" id="tblDet">
        <thead class="table-light">
          <tr><th>Empaque</th><th>Vendidas</th><th>Merma</th><th>Subtotal</th><th></th></tr>
        </thead>
        <tbody>
          <!-- líneas existentes -->
          <c:forEach items="${detalle}" var="d">
            <tr data-id="${d.idEmpaque}"
                data-dist="${d.idDistribucion}"
                data-price="${d.precioUnitario}"
                data-orig="${d.cantidadVendida + d.merma}">
              <td>${d.nombreEmpaque}</td>
              <td><input type="number" class="form-control qtyV" min="0"
                         value="${d.cantidadVendida}"></td>
              <td><input type="number" class="form-control qtyM" min="0"
                         value="${d.merma}"></td>
              <td class="sub">
                <fmt:formatNumber value="${d.totalLinea}" type="number"
                                  minFractionDigits="2" maxFractionDigits="2"/>
              </td>
              <td>
                <button type="button"
                        class="btn btn-sm btn-outline-danger btn-eliminar"
                        title="Quitar línea"
                        onclick="return confirm('¿Quitar esta línea?');">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>
          </c:forEach>
        </tbody>
        <tfoot class="table-secondary">
          <tr><td colspan="3" class="text-end">Total</td>
              <td id="tdTotal">
                <fmt:formatNumber value="${nota.total}" type="number"
                                  minFractionDigits="2" maxFractionDigits="2"/>
              </td><td></td></tr>
        </tfoot>
      </table>
    </div>

    <button type="button" class="btn btn-outline-secondary w-100 mb-3"
            id="btnAdd">+ Agregar paquete</button>

    <button id="btnSave" class="btn btn-primary">Actualizar nota</button>
    <a class="btn btn-secondary ms-2"
       href="${pageContext.request.contextPath}/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=${nota.idRepartidor}">
       Cancelar
    </a>
  </form>
</div>

<!-- JSON inventario (formato: { "idEmpaque": {nombre, precio, restante, idDistribucion}, ... }) -->
<script id="invJson" type="application/json">${inventarioJson}</script>

<!-- Bootstrap & script del proyecto -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/static/js/editNota.js?v=${System.currentTimeMillis()}"></script>

<!-- Validación visual de filas + bloqueo del guardar -->
<script>
(function(){
  // --- Inventario restante de hoy (por empaque) ---
  var INV = {};
  try {
    INV = JSON.parse(document.getElementById('invJson').textContent || '{}');
  } catch(e) { INV = {}; }

  var tbl   = document.getElementById('tblDet');
  var tBody = tbl.querySelector('tbody');
  var btnSave = document.getElementById('btnSave');
  var tdTotal = document.getElementById('tdTotal');

  function toInt(v){ v = (v||'').toString().trim(); return v === '' ? 0 : parseInt(v,10)||0; }
  function toNum(v){ v = (v||'').toString().trim(); return v === '' ? 0 : parseFloat(v)||0; }

  // Recalcula subtotal por fila
  function recalcRow(tr){
    var v = toInt(tr.querySelector('.qtyV')?.value);
    var m = toInt(tr.querySelector('.qtyM')?.value);
    var p = toNum(tr.dataset.price);
    tr.querySelector('.sub').textContent = ((v+m)*p).toFixed(2);
  }

  // Total general
  function recalcTotal(){
    var total = 0;
    tBody.querySelectorAll('tr').forEach(function(tr){
      total += toNum(tr.querySelector('.sub')?.textContent);
    });
    if (tdTotal) tdTotal.textContent = total.toFixed(2);
  }

  // Valida TODAS las filas:
  // permitido por empaque = restante(INV) + piezas originales de ESTA nota
  function validateAll(){
    // 1) originales por empaque
    var origByEmp = {};
    tBody.querySelectorAll('tr').forEach(function(tr){
      var id = toInt(tr.dataset.id);
      var o  = toInt(tr.dataset.orig);
      origByEmp[id] = (origByEmp[id]||0) + o;
    });

    // 2) uso actual por empaque (suma vendidas+merma de las filas visibles)
    var useByEmp = {};
    tBody.querySelectorAll('tr').forEach(function(tr){
      var id = toInt(tr.dataset.id);
      var v  = toInt(tr.querySelector('.qtyV')?.value);
      var m  = toInt(tr.querySelector('.qtyM')?.value);
      useByEmp[id] = (useByEmp[id]||0) + v + m;
    });

    // 3) pinta filas inválidas y determina si hay alguna
    var hasInvalid = false;
    tBody.querySelectorAll('tr').forEach(function(tr){
      var id = toInt(tr.dataset.id);
      var restante = (INV[id] && typeof INV[id].restante === 'number') ? INV[id].restante : 0;
      var permitido = restante + (origByEmp[id]||0);
      var uso = useByEmp[id]||0;

      // también invalida si hay negativos
      var v = toInt(tr.querySelector('.qtyV')?.value);
      var m = toInt(tr.querySelector('.qtyM')?.value);
      var invalid = (uso > permitido) || v < 0 || m < 0;

      tr.classList.toggle('table-danger', invalid);
      if (invalid) hasInvalid = true;

      // Tooltip simple en title
      if (uso > permitido) {
        tr.title = 'Excede stock disponible: ' + uso + ' / ' + permitido;
      } else {
        tr.removeAttribute('title');
      }

      // Recalcula subtotal por si cambió
      recalcRow(tr);
    });

    // 4) recalcula total y bloquea guardar si hay inválidas
    recalcTotal();
    if (hasInvalid) {
      btnSave.setAttribute('disabled','disabled');
    } else {
      btnSave.removeAttribute('disabled');
    }
  }

  // Wire de eventos para filas existentes
  function wireRow(tr){
    var qv = tr.querySelector('.qtyV');
    var qm = tr.querySelector('.qtyM');
    ['input','change'].forEach(function(ev){
      qv && qv.addEventListener(ev, validateAll);
      qm && qm.addEventListener(ev, validateAll);
    });

    // botón quitar línea (sólo UI; tu submit/JS generará JSON final)
    var del = tr.querySelector('.btn-eliminar');
    if (del){
      del.addEventListener('click', function(){
        // si confirm() devuelve true, quitamos la fila y volvemos a validar
        // (respetamos tu confirm nativo actual)
        if (!confirm('¿Quitar esta línea?')) return;
        tr.parentNode.removeChild(tr);
        validateAll();
      });
    }

    // calcula primer subtotal
    recalcRow(tr);
  }

  // Vincula filas iniciales
  tBody.querySelectorAll('tr').forEach(wireRow);

  // Observa nuevas filas agregadas por tu editNota.js y las cablea
  var mo = new MutationObserver(function(muts){
    muts.forEach(function(m){
      m.addedNodes.forEach(function(n){
        if (n.nodeType === 1 && n.tagName === 'TR') wireRow(n);
      });
    });
    validateAll();
  });
  mo.observe(tBody, {childList:true});

  // Primera validación al cargar
  validateAll();

  // Si tu script agrega filas con #btnAdd, revalidamos después
  var btnAdd = document.getElementById('btnAdd');
  if (btnAdd){
    btnAdd.addEventListener('click', function(){
      // deja que tu editNota.js agregue la fila; el MutationObserver hará el resto
      setTimeout(validateAll, 50);
    });
  }
})();
</script>
</body>
</html>
