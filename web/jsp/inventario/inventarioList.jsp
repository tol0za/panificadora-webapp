<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <title>Inventario</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet"/>
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>
<body>
<div class="container py-4">

  <!-- ===== Registro rápido ===== -->
  <h3 class="mb-3">Registro Rápido de Movimientos de Inventario</h3>

  <form id="formInventario"
        action="${ctx}/InventarioServlet" method="post"
        class="row g-3 mb-4 needs-validation" novalidate>

    <!-- Empaque + Stock -->
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
             min="1" class="form-control" placeholder="Cantidad" required />
      <div class="invalid-feedback">Ingresa una cantidad válida.</div>
    </div>

    <!-- Motivo -->
    <div class="col-md-3">
      <label for="selectMotivo" class="form-label">Motivo</label>
      <select name="motivo" id="selectMotivo" class="form-select" required>
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

  <!-- ===== Maestro: empaques con stock ===== -->
  <div class="d-flex align-items-center mb-2">
    <h3 class="mb-0">Inventario por Empaque</h3>
    <div class="form-check form-switch ms-auto">
      <input class="form-check-input" type="checkbox" id="swVerTodo">
      <label class="form-check-label" for="swVerTodo">Ver también sin stock</label>
    </div>
  </div>

  <div class="table-responsive mb-3">
    <table class="table table-hover align-middle" id="tblStock">
      <thead class="table-light">
        <tr>
          <th>Empaque</th>
          <th class="text-end">Stock</th>
          <th class="text-center">Acciones</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="em" items="${empaques}">
          <c:set var="stk" value="${stockMap[em.idEmpaque] != null ? stockMap[em.idEmpaque] : 0}" />
          <tr data-id="${em.idEmpaque}"
              data-nombre="${em.nombreEmpaque}"
              data-stock="${stk}"
              class="${stk == 0 ? 'row-zero d-none' : ''}">
            <td>${em.nombreEmpaque}</td>
            <td class="text-end">
              <span class="badge bg-${stk > 0 ? 'success' : 'secondary'}">${stk}</span>
            </td>
            <td class="text-center">
              <button type="button"
                      class="btn btn-sm btn-outline-info btn-ver"
                      ${stk == 0 ? 'disabled' : ''}>
                <i class="bi bi-clock-history"></i> Movimientos
              </button>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${fn:length(empaques) == 0}">
          <tr><td colspan="3" class="text-center text-muted">Sin registros</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <!-- Total stock general -->
  <c:set var="totalStock" value="0" />
  <c:forEach var="entry" items="${stockMap}">
    <c:set var="totalStock" value="${totalStock + entry.value}" />
  </c:forEach>
  <div class="fw-bold">Total stock general: ${totalStock} unidades</div>
</div>

<!-- ===== Modal Detalle (scrollable) ===== -->
<div class="modal fade" id="mdlMovs" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg modal-dialog-scrollable">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">
          Movimientos – <span id="movNombre"></span>
        </h5>
        <button class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
      </div>
      <div class="modal-body">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <div>Stock actual: <span class="badge bg-secondary" id="movStock">0</span></div>
          <div class="input-group input-group-sm" style="width: 280px;">
            <span class="input-group-text"><i class="bi bi-filter"></i></span>
            <select id="movFiltro" class="form-select">
              <option value="">Todos</option>
              <option value="INGRESO">Ingresos</option>
              <option value="SALIDA">Salidas</option>
              <option value="MERMA">Mermas</option>
              <option value="REABRIR_RUTA">Reabrir ruta</option>
              <option value="ENTRADA_RETORNO">Retornos</option>
              <option value="AJUSTE">Ajustes</option>
            </select>
          </div>
        </div>
        <div class="table-responsive" style="max-height:60vh;">
          <table class="table table-sm table-striped">
            <thead class="table-light">
              <tr>
                <th style="width: 180px;">Fecha</th>
                <th>Motivo</th>
                <th class="text-end">Δ</th>
                <th class="text-end">Saldo</th>
              </tr>
            </thead>
            <tbody id="movBody"></tbody>
          </table>
        </div>
      </div>
      <div class="modal-footer">
        <small class="text-muted me-auto" id="movCount"></small>
        <button class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
      </div>
    </div>
  </div>
</div>

<!-- ===== Validación Bootstrap ===== -->
<script>
(() => {
  'use strict';
  document.querySelectorAll('.needs-validation').forEach(form => {
    form.addEventListener('submit', e => {
      if (!form.checkValidity()) { e.preventDefault(); e.stopPropagation(); }
      form.classList.add('was-validated');
    }, false);
  });
})();
</script>

<!-- ===== Lógica maestro–detalle, filtros y validaciones ===== -->

 <script>
document.addEventListener("DOMContentLoaded", function() {
  const ctx = "${ctx}";
  const selectEmpaque = document.getElementById("selectEmpaque");
  const stockDisplay  = document.getElementById("stockDisplay");
  const swVerTodo     = document.getElementById("swVerTodo");

  /* -------------------- Stock en el formulario -------------------- */
  function actualizarStockForm() {
    const opt   = selectEmpaque?.selectedOptions?.[0] || null;
    const stock = opt ? opt.getAttribute("data-stock") : null;
    stockDisplay.innerHTML = (stock !== null && stock !== "")
      ? "Total de piezas disponibles: <strong>" + stock + "</strong>"
      : "Total de piezas disponibles: —";
  }
  selectEmpaque.addEventListener("change", actualizarStockForm);
  actualizarStockForm();

  /* ---- Validación contra stock para Salida/Merma en el submit ---- */
  document.getElementById("formInventario").addEventListener("submit", function(e) {
    const motivo   = (document.getElementById("selectMotivo")?.value || "").toLowerCase();
    const cantidad = +document.getElementById("inputCantidad").value;
    const opt      = selectEmpaque?.selectedOptions?.[0] || null;
    const stock    = +((opt && opt.getAttribute("data-stock")) || 0);
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

  /* --------------- Mostrar también empaques sin stock -------------- */
  swVerTodo.addEventListener('change', () => {
    document.querySelectorAll('#tblStock tbody tr.row-zero').forEach(tr => {
      tr.classList.toggle('d-none', !swVerTodo.checked);
      const btn = tr.querySelector('.btn-ver');
      if (btn) btn.disabled = !swVerTodo.checked; // permitir ver movimientos aunque stock=0
    });
  });

  /* ----------------- Modal movimientos maestro→detalle ------------- */
  const movModal  = new bootstrap.Modal(document.getElementById('mdlMovs'));
  const movNombre = document.getElementById('movNombre');
  const movStock  = document.getElementById('movStock');
  const movBody   = document.getElementById('movBody');
  const movCount  = document.getElementById('movCount');
  const movFiltro = document.getElementById('movFiltro');
  let cacheMovs = []; // cache local para filtrar en el modal

  function renderMovs(list) {
    // Construimos una sola vez el HTML (más rápido y sin conflictos con JSP/EL)
    let html = '';
    for (const m of list) {
      const fecha  = m.fecha || '';
      const motivo = m.motivo || '';
      const delta  = (Number(m.cantidad) >= 0 ? '+' : '') + m.cantidad;
      const saldo  = (m.cantidadActual != null ? m.cantidadActual : '');

      html += '<tr>'
           +    '<td>' + fecha + '</td>'
           +    '<td>' + motivo + '</td>'
           +    '<td class="text-end">' + delta + '</td>'
           +    '<td class="text-end">' + saldo + '</td>'
           +  '</tr>';
    }
    movBody.innerHTML = html;           // Pintamos de golpe
    movCount.textContent = list.length + ' movimientos';
  }

  function applyFilter() {
    const f = (movFiltro.value || "").toUpperCase();
    if (!f) { renderMovs(cacheMovs); return; }
    const filtered = cacheMovs.filter(m => (m.motivo || "").toUpperCase().includes(f));
    renderMovs(filtered);
  }
  movFiltro.addEventListener('change', applyFilter);

  /* ------------------- Click en botón “Movimientos” ---------------- */
  document.querySelectorAll('#tblStock .btn-ver').forEach(btn => {
    btn.addEventListener('click', async () => {
      const tr    = btn.closest('tr[data-id]');
      const idStr = tr ? String(tr.dataset.id || "").trim() : "";
      const idNum = parseInt(idStr, 10);

      if (!/^\d+$/.test(idStr) || !Number.isFinite(idNum) || idNum <= 0) {
        Swal.fire({icon:'error', title:'No se pudo determinar el empaque seleccionado'});
        return;
      }

      // Cabecera del modal
      movNombre.textContent = tr.getAttribute('data-nombre') || '';
      movStock.textContent  = tr.getAttribute('data-stock')  || '0';

      // Query string con inFrame=1 (bypass del guard del iframe) + anticache defensivo
      const qs  = new URLSearchParams({
        accion: 'movsByEmpaque',
        idEmpaque: String(idNum),
        inFrame: '1',
        t: Date.now().toString()
      });
      const url = `${ctx}/InventarioServlet?${qs.toString()}`;

      try {
        const res  = await fetch(url, {
          cache: 'no-store',
          headers: {
            // algunos filtros reconocen sólo "XMLHttpRequest"
            'X-Requested-With': 'XMLHttpRequest',
            // declaramos que esperamos JSON
            'Accept': 'application/json',
            // respaldo del ID por header (si un filtro limpia la query)
            'X-IdEmpaque': String(idNum)
          }
        });

        const ctyp = (res.headers.get('content-type') || '').toLowerCase();
        const body = await res.text();

        if (!ctyp.includes('application/json')) {
          // Llegó HTML (ej: inicio.jsp); muestra texto para diagnóstico
          throw new Error('Respuesta no JSON: ' + body.slice(0, 300));
        }
        if (!res.ok) {
          // Si vino JSON de error, muéstralo
          try { throw new Error(JSON.parse(body)?.error || body); }
          catch { throw new Error(body); }
        }

        const data = JSON.parse(body); // [{fecha, motivo, cantidad, cantidadActual}, ...]
        data.sort((a,b) => (a.fecha < b.fecha ? 1 : -1)); // orden desc
        cacheMovs = data;
        movFiltro.value = "";
        renderMovs(cacheMovs);
        movModal.show();

      } catch (err) {
        Swal.fire({
          icon: 'error',
          title: 'No se pudieron cargar los movimientos',
          text: String(err).substring(0, 300),
          confirmButtonText: 'Aceptar'
        });
      }
    });
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

<!-- 🧹 Limpieza defensiva de flashes AJENOS -->
<c:remove var="flashSalida" scope="session"/>
<c:remove var="flashNotas"  scope="session"/>


<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>