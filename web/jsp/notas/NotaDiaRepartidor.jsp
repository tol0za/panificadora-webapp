<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>Notas – ${repartidor.nombreRepartidor}</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
</head>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="totalRestante" value="0" />
<c:forEach items="${inventario}" var="i">
  <c:set var="totalRestante" value="${totalRestante + i.restante}" />
</c:forEach>
<c:set var="puedeNueva" value="${not rutaCerrada and (totalRestante gt 0)}" />

<body data-ctx="${ctx}"
      data-ruta-cerrada="${rutaCerrada}"
      data-stock-total="${totalRestante}">
<div class="container-fluid py-3">

  <div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
    <h4 class="mb-2">
      Notas de venta – ${repartidor.nombreRepartidor} (${hoy})
    </h4>
    <div class="d-flex gap-2 align-items-center">
      <c:if test="${not empty listaNotas}">
        <a class="btn btn-outline-success"
           href="${ctx}/NotaVentaServlet?inFrame=1&amp;accion=imprimirNotasDia&amp;id_repartidor=${repartidor.idRepartidor}">
          <i class="bi bi-printer"></i> Imprimir día
        </a>
      </c:if>

      <c:choose>
        <c:when test="${puedeNueva}">
          <button id="btnNueva" class="btn btn-success">
            <i class="bi bi-plus-circle"></i> Nueva nota
          </button>
        </c:when>
        <c:otherwise>
          <button id="btnNueva" class="btn btn-success" disabled
                  title="${rutaCerrada ? 'La ruta está cerrada. Reábrela para capturar notas.' : 'Sin stock disponible en el repartidor.'}">
            <i class="bi bi-plus-circle"></i> Nueva nota
          </button>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <c:if test="${not empty sessionScope.flashMsg}">
    <c:set var="flashKindAuto"
           value="${fn:contains(sessionScope.flashMsg,'Error') or fn:contains(sessionScope.flashMsg,'insuficiente') ? 'error' : 'success'}"/>
    <div id="flashBridge" class="d-none"
         data-kind ="${empty sessionScope.flashKind ? flashKindAuto : sessionScope.flashKind}"
         data-title="${empty sessionScope.flashTitle ? 'Operación realizada' : sessionScope.flashTitle}">
      <span id="flashMessage"><c:out value="${sessionScope.flashMsg}"/></span>
    </div>
    <c:remove var="flashMsg"   scope="session"/>
    <c:remove var="flashKind"  scope="session"/>
    <c:remove var="flashTitle" scope="session"/>
  </c:if>

  <div class="card card-body mb-4">
    <div class="d-flex justify-content-between align-items-center">
      <h6 class="card-title mb-2">Inventario disponible</h6>
      <span class="badge bg-${totalRestante > 0 ? 'success' : 'secondary'}">
        Total disponible: ${totalRestante}
      </span>
    </div>
    <div class="table-responsive">
      <table class="table table-sm mb-0">
        <thead class="table-light">
          <tr><th>Empaque</th><th class="text-end">Precio</th><th class="text-end">Restante</th></tr>
        </thead>
        <tbody>
          <c:forEach items="${inventario}" var="i">
            <tr>
              <td>${i.nombre}</td>
              <td class="text-end">${i.precio}</td>
              <td class="text-end">${i.restante}</td>
            </tr>
          </c:forEach>
          <c:if test="${empty inventario}">
            <tr><td colspan="3" class="text-center text-muted">Sin registros</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </div>

  <div class="card card-body">
    <h6 class="card-title mb-2">Notas registradas</h6>
    <div class="table-responsive">
      <table id="tblNotas" class="table table-striped">
        <thead class="table-light">
          <tr>
            <th>Folio</th><th>Tienda</th><th>Total</th>
            <th class="text-center">Opciones</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach items="${listaNotas}" var="n">
            <tr>
              <td>${n.folio}</td>
              <td>
                <c:choose>
                  <c:when test="${not empty n.nombreTienda}">${n.nombreTienda}</c:when>
                  <c:otherwise>ID ${n.idTienda}</c:otherwise>
                </c:choose>
              </td>
              <td>${n.total}</td>
              <td class="text-center">
                <a href="#" class="btn btn-sm btn-outline-info me-1 btn-det"
                   data-idNota="${n.idNotaVenta}"
                   data-folio ="${n.folio}"
                   data-tienda="${n.nombreTienda}">
                  <i class="bi bi-eye"></i>
                </a>

                <c:choose>
                  <c:when test="${rutaCerrada}">
                    <button class="btn btn-sm btn-outline-primary me-1" disabled
                            title="Reabre la ruta para editar esta nota">
                      <i class="bi bi-pencil-square"></i>
                    </button>
                  </c:when>
                  <c:otherwise>
                    <a href="${ctx}/NotaVentaServlet?inFrame=1&accion=editarNota&id=${n.idNotaVenta}"
                       class="btn btn-sm btn-outline-primary me-1">
                      <i class="bi bi-pencil-square"></i>
                    </a>
                  </c:otherwise>
                </c:choose>

                <c:choose>
                  <c:when test="${rutaCerrada}">
                    <button class="btn btn-sm btn-outline-danger" disabled
                            title="Reabre la ruta para eliminar esta nota">
                      <i class="bi bi-trash"></i>
                    </button>
                  </c:when>
                  <c:otherwise>
                    <a href="#"
                       class="btn btn-sm btn-outline-danger js-confirm-goto"
                       data-url="${ctx}/NotaVentaServlet?inFrame=1&accion=eliminarNota&id=${n.idNotaVenta}"
                       data-title="Eliminar nota"
                       data-msg="¿Eliminar la nota con folio ${n.folio}?">
                      <i class="bi bi-trash"></i>
                    </a>
                  </c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty listaNotas}">
            <tr><td colspan="4" class="text-center text-muted">Sin notas</td></tr>
          </c:if>
        </tbody>
        <tfoot class="table-secondary fw-semibold">
          <tr><td colspan="2">Total día</td><td>${totalDia}</td><td></td></tr>
        </tfoot>
      </table>
    </div>
  </div>

  <div class="mt-4">
    <c:choose>
      <c:when test="${not rutaCerrada}">
        <form class="d-inline" method="post" action="${ctx}/NotaVentaServlet">
          <input type="hidden" name="inFrame"       value="1"/>
          <input type="hidden" name="accion"        value="cerrarRuta"/>
          <input type="hidden" name="id_repartidor" value="${repartidor.idRepartidor}"/>
          <button type="button"
                  class="btn btn-warning js-confirm-submit"
                  data-title="Cerrar ruta"
                  data-msg="¿Cerrar ruta y devolver sobrante a bodega?">
            <i class="bi bi-box-arrow-in-left"></i> Cerrar ruta y devolver sobrante
          </button>
        </form>
      </c:when>
      <c:otherwise>
        <form class="d-inline" method="post" action="${ctx}/NotaVentaServlet">
          <input type="hidden" name="inFrame"       value="1"/>
          <input type="hidden" name="accion"        value="reabrirRuta"/>
          <input type="hidden" name="id_repartidor" value="${repartidor.idRepartidor}"/>
          <button type="button"
                  class="btn btn-outline-warning js-confirm-submit"
                  data-title="Reabrir ruta"
                  data-msg="¿Reabrir la ruta para seguir capturando notas?">
            <i class="bi bi-arrow-counterclockwise"></i> Reabrir ruta
          </button>
        </form>
      </c:otherwise>
    </c:choose>
  </div>

</div>

<!-- ========= Modales ========= -->

<div id="modalNota" class="modal fade" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg"><div class="modal-content">
    <form id="formNota" action="${ctx}/NotaVentaServlet" method="post">
      <input type="hidden" name="accion"        value="guardarNota">
      <input type="hidden" name="id_repartidor" value="${repartidor.idRepartidor}">
      <input type="hidden" id="lineas" name="lineas">

      <div class="modal-header">
        <h5 class="modal-title">Nueva nota</h5>
        <button class="btn-close" data-bs-dismiss="modal"></button>
      </div>

      <div class="modal-body">
        <div class="row g-2 mb-3">
          <div class="col-md-4">
            <label class="form-label">Folio</label>
            <input id="inpFolio" name="folio" required class="form-control">
            <div id="folioHelpBad"  class="form-text text-danger  d-none">El folio ya existe</div>
            <div id="folioHelpGood" class="form-text text-success d-none">Folio disponible ✓</div>
          </div>
          <div class="col-md-8">
            <label class="form-label">Tienda</label>
            <select id="selTienda" name="id_tienda" class="form-select" disabled required>
              <option value="" disabled selected>Seleccione…</option>
              <c:forEach items="${tiendas}" var="t">
                <option value="${t.idTienda}">${t.nombre}</option>
              </c:forEach>
            </select>
          </div>
        </div>

        <div class="table-responsive mb-2">
          <table id="tblDetalle" class="table table-bordered">
            <thead class="table-light">
              <!-- Agregamos la columna COBRADAS -->
              <tr><th>Empaque</th><th>Vendidas</th><th>Merma</th><th>Cobradas</th><th>Subtotal</th><th></th></tr>
            </thead>
            <tbody></tbody>
            <tfoot class="table-secondary">
              <tr><td colspan="4" class="text-end">Total</td><td id="tdTotal">0.00</td><td></td></tr>
            </tfoot>
          </table>
        </div>

        <button id="btnAddLinea" type="button"
                class="btn btn-outline-secondary w-100" disabled>
          + Agregar paquete
        </button>
      </div>

      <div class="modal-footer">
        <button id="btnSave" class="btn btn-primary" disabled>Guardar nota</button>
      </div>
    </form>
  </div></div>
</div>

<div id="modalDet" class="modal fade" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg"><div class="modal-content">
    <div class="modal-header">
      <h5 class="modal-title">Detalle nota <span id="mdFolio"></span></h5>
      <button class="btn-close" data-bs-dismiss="modal"></button>
    </div>
    <div class="modal-body">
      <p><strong>Tienda:</strong> <span id="mdTienda"></span></p>
      <div class="table-responsive">
        <table class="table table-bordered">
          <thead class="table-light">
            <tr><th>Empaque</th><th>Cobradas</th><th>Merma</th><th>Subtotal</th></tr>
          </thead>
          <tbody id="mdBody"></tbody>
          <tfoot class="table-secondary">
            <tr><td colspan="3" class="text-end">Total</td><td id="mdTotal">0.00</td></tr>
          </tfoot>
        </table>
      </div>
    </div>
    <div class="modal-footer">
      <a id="mdEditar" class="btn btn-outline-primary">
        <i class="bi bi-pencil-square"></i> Editar
      </a>
      <button class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
    </div>
  </div></div>
</div>

<!-- Modal de confirmación -->
<div class="modal fade" id="mdlConfirm" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog"><div class="modal-content">
    <div class="modal-header">
      <h5 id="confirmTitle" class="modal-title">Confirmación</h5>
      <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
    </div>
    <div class="modal-body">
      <p id="confirmMsg" class="mb-0">¿Deseas continuar?</p>
    </div>
    <div class="modal-footer">
      <button id="btnConfirmOk" type="button" class="btn btn-primary">Sí, continuar</button>
      <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
    </div>
  </div></div>
</div>

<!-- Modal de notificación -->
<div class="modal fade" id="mdlFlash" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog"><div class="modal-content border">
    <div id="flashHeader" class="modal-header">
      <h5 class="modal-title d-flex align-items-center gap-2">
        <i id="flashIcon" class="bi"></i>
        <span id="flashTitle">Operación realizada</span>
      </h5>
      <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
    </div>
    <div class="modal-body">
      <p id="flashMsg" class="mb-0"></p>
    </div>
    <div class="modal-footer">
      <button class="btn btn-primary" data-bs-dismiss="modal">Aceptar</button>
    </div>
  </div></div>
</div>

<script id="inventarioJson" type="application/json">${inventarioJson}</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/static/js/notas.js?v=fix-merma-1"></script>

<!-- === Overlay: COBRADAS = vendidas - merma; Subtotal = cobradas * precio  === -->
<script>
(function(){
  const fmtMX = new Intl.NumberFormat('es-MX',{style:'currency',currency:'MXN'});
  const fmt = n => fmtMX.format(Number(n)||0);
  const clamp = v => { v=(''+(v??'')).replace(/[^\d-]/g,''); const n=parseInt(v,10); return Number.isFinite(n)?Math.max(0,n):0; };
  let INV={}; try{ INV=JSON.parse(document.getElementById('inventarioJson')?.textContent||'{}'); }catch(_){ INV={}; }

  const tbody = document.querySelector('#tblDetalle tbody');
  const tdTot = document.getElementById('tdTotal');
  const btnSave = document.getElementById('btnSave');
  const selTienda = document.getElementById('selTienda');

  function ensureCobradasCell(tr){
    if (!tr.querySelector('.cobradasCell')) {
      const subCell = tr.querySelector('.sub, .subCell') || tr.querySelector('td:nth-last-child(2)');
      const td = document.createElement('td');
      td.className = 'text-end cobradasCell';
      td.textContent = '0';
      if (subCell && subCell.parentNode) {
        subCell.parentNode.insertBefore(td, subCell);
      } else {
        tr.insertBefore(td, tr.lastElementChild); // fallback
      }
    }
  }

  function getOpt(tr){ const sel=tr.querySelector('select'); return sel? sel.selectedOptions[0] : null; }
  function getInfo(tr){
    const opt = getOpt(tr);
    const id  = parseInt(opt?.value || tr.getAttribute('data-id') || tr.dataset.id || '0',10) || 0;
    let precio   = Number(opt?.dataset?.precio);
    let restante = parseInt(opt?.dataset?.restante || '',10);
    if (!Number.isFinite(precio)   || precio<=0) precio   = Number(INV[id]?.precio)||0;
    if (!Number.isFinite(restante) || restante<0) restante = parseInt(INV[id]?.restante||'0',10)||0;
    return { precio, restante };
  }

  function recalcRow(tr){
    ensureCobradasCell(tr);

    const vendInp = tr.querySelector('.inpVendidas') || tr.querySelector('input[name="vendidas"],input[name="vendidas[]"]');
    const merInp  = tr.querySelector('.in-merma')    || tr.querySelector('input[name="merma"],input[name="merma[]"]');
    const cobrTd  = tr.querySelector('.cobradasCell');
    const subEl   = tr.querySelector('.sub') || tr.querySelector('.subCell') || tr.querySelector('td:nth-last-child(2)');

    const vend = clamp(vendInp?.value);
    const mer  = clamp(merInp?.value);
    if (vendInp) vendInp.value = vend;
    if (merInp)  merInp.value  = mer;

    const { precio, restante } = getInfo(tr);

    const excedeStock = vend > restante;
    const mermaMayor  = mer  > vend;
    const invalid = (vend < 0) || (mer < 0) || excedeStock || mermaMayor;
    tr.classList.toggle('table-danger', invalid);
    vendInp?.classList.toggle('is-invalid', invalid);
    merInp?.classList.toggle('is-invalid', invalid);

    const cobradas = Math.max(0, vend - mer);
    if (cobrTd) cobrTd.textContent = cobradas;

    const sub = cobradas * (Number.isFinite(precio) ? precio : 0);
    if (subEl) { subEl.dataset.val = String(sub); subEl.textContent = fmt(sub); }
    return !invalid;
  }

  function recalcTotal(){
    let total=0, ok=true, rows=0;
    (tbody?.querySelectorAll('tr')||[]).forEach(tr=>{
      rows++;
      if(!recalcRow(tr)) ok=false;
      const v = Number(tr.querySelector('.sub')?.dataset.val || tr.querySelector('.subCell')?.dataset.val || 0);
      total += Number.isFinite(v) ? v : 0;
    });
    if (tdTot) tdTot.textContent = fmt(total);
    const tiendaOk = !!(selTienda?.value);
    btnSave?.toggleAttribute('disabled', !(rows>0 && ok && total>0 && tiendaOk));
  }

  function bindRow(tr){
    ensureCobradasCell(tr);
    tr.addEventListener('input',  ev => { if (ev.target.matches('input[type="number"], select')) { recalcRow(tr); recalcTotal(); } });
    tr.addEventListener('change', ev => { if (ev.target.matches('select')) { recalcRow(tr); recalcTotal(); } });
    recalcRow(tr);
  }

  // enlaza existentes
  (tbody?.querySelectorAll('tr')||[]).forEach(bindRow);

  // observa nuevas filas que agregue notas.js
  const mo = new MutationObserver(muts=>{
    muts.forEach(m=> m.addedNodes.forEach(n=>{
      if(n.nodeType===1 && n.tagName==='TR'){ bindRow(n); recalcTotal(); }
    }));
  });
  if (tbody) mo.observe(tbody, {childList:true});

  // al abrir el modal, recalcula
  document.getElementById('modalNota')?.addEventListener('shown.bs.modal', recalcTotal);
})();
</script>

<!-- Script de flash / confirmación (sin cambios) -->
<script>
(function () {
  const fb = document.getElementById('flashBridge');
  if (fb) {
    const kind  = fb.dataset.kind || 'success';
    const title = fb.dataset.title || 'Operación realizada';
    const msg   = fb.querySelector('#flashMessage')?.textContent || '';
    const map = {
      success: { icon: 'bi-check-circle', header: 'bg-success-subtle border-success', title: 'text-success' },
      error:   { icon: 'bi-x-circle',     header: 'bg-danger-subtle border-danger',   title: 'text-danger'  },
      warning: { icon: 'bi-exclamation-triangle', header: 'bg-warning-subtle border-warning', title: 'text-warning' },
      info:    { icon: 'bi-info-circle',  header: 'bg-info-subtle border-info',       title: 'text-info'    }
    };
    const conf = map[kind] || map.success;
    document.getElementById('flashIcon').className  = 'bi ' + conf.icon;
    document.getElementById('flashTitle').className = conf.title;
    document.getElementById('flashHeader').className= 'modal-header ' + conf.header;
    document.getElementById('flashTitle').textContent = title;
    document.getElementById('flashMsg').textContent   = msg;
    new bootstrap.Modal(document.getElementById('mdlFlash')).show();
  }

  const mdlEl = document.getElementById('mdlConfirm');
  const mdl   = new bootstrap.Modal(mdlEl);
  let onOk = null;

  document.getElementById('btnConfirmOk').addEventListener('click', function () {
    if (typeof onOk === 'function') onOk();
    onOk = null;
    mdl.hide();
  });

  function openConfirm(opts) {
    document.getElementById('confirmTitle').textContent = opts.title || 'Confirmación';
    document.getElementById('confirmMsg').textContent   = opts.msg   || '¿Deseas continuar?';
    onOk = opts.onOk || null;
    mdl.show();
  }

  document.querySelectorAll('.js-confirm-submit').forEach(function (btn) {
    btn.addEventListener('click', function (ev) {
      ev.preventDefault();
      const form = btn.closest('form');
      openConfirm({
        title: btn.dataset.title || 'Confirmación',
        msg:   btn.dataset.msg   || '¿Deseas continuar?',
        onOk:  function () { form.submit(); }
      });
    });
  });

  document.querySelectorAll('.js-confirm-goto').forEach(function (a) {
    a.addEventListener('click', function (ev) {
      ev.preventDefault();
      const url = a.dataset.url || a.getAttribute('href');
      openConfirm({
        title: a.dataset.title || 'Confirmación',
        msg:   a.dataset.msg   || '¿Deseas continuar?',
        onOk:  function () { window.location.href = url; }
      });
    });
  });
})();
</script>

</body>
</html>
