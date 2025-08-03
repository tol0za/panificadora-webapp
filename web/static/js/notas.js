/**
 *  JS para el módulo Notas — Bootstrap 5 requerido.
 *  Usa window.ctx (inyectado desde atributo data-ctx) para
 *  formar URLs absolutas dentro del contexto.
 */
(() => {
  /* ---------- helpers ---------- */
  const ctx = document.body.dataset.ctx || '';
  const inv = JSON.parse(document.getElementById('inventarioJson')?.textContent || '{}');

  /* ---------- modal nueva nota ---------- */
  const modalEl = document.getElementById('modalNota');
  const modal   = modalEl ? new bootstrap.Modal(modalEl) : null;

  document.getElementById('btnNueva')?.addEventListener('click', () => modal.show());

  const tblDetalle = document.querySelector('#tblDetalle tbody');
  const totalCell  = document.getElementById('tdTotal');
  document.getElementById('btnAddLinea')?.addEventListener('click', addLinea);

  function addLinea(){
    const tr  = tblDetalle.insertRow();
    const sel = document.createElement('select');
    sel.className = 'form-select';
    sel.innerHTML = Object.entries(inv)
                          .map(([id, o]) => `<option value="${id}">${o.nombre}</option>`)
                          .join('');
    tr.insertCell().appendChild(sel);
    tr.insertCell().innerHTML = '<input type="number" min="0" class="form-control qtyV" value="0">';
    tr.insertCell().innerHTML = '<input type="number" min="0" class="form-control qtyM" value="0">';
    tr.insertCell().textContent = '0.00';
    tr.insertCell().innerHTML = '<button type="button" class="btn btn-sm btn-link text-danger">✕</button>';
    tr.querySelector('button').onclick = () => { tr.remove(); calcTotal(); };
    tr.querySelectorAll('input,select').forEach(el => el.oninput = calcTotal);
  }

  function calcTotal(){
    let total = 0;
    tblDetalle.querySelectorAll('tr').forEach(tr=>{
      const id     = tr.querySelector('select').value;
      const vend   = +tr.querySelector('.qtyV').value;
      const precio = inv[id].precio;
      const sub    = vend * precio; // merma no se cobra
      tr.cells[3].textContent = sub.toFixed(2);
      total += sub;
    });
    totalCell.textContent = total.toFixed(2);
  }

  /* empaquetar líneas a JSON antes de enviar */
  document.getElementById('formNota')?.addEventListener('submit', () => {
    const líneas = [];
    tblDetalle.querySelectorAll('tr').forEach(tr=>{
      líneas.push({
        idEmpaque:      +tr.querySelector('select').value,
        idDistribucion: inv[tr.querySelector('select').value].idDistribucion || 0,
        vendidas:       +tr.querySelector('.qtyV').value,
        merma:          +tr.querySelector('.qtyM').value,
        precio:         inv[tr.querySelector('select').value].precio
      });
    });
    document.getElementById('lineas').value = JSON.stringify(líneas);
  });

  /* cerrar ruta */
  window.cerrarRuta = id => {
    if (confirm('¿Cerrar ruta y devolver sobrante?')) {
      location.href = `${ctx}/NotaVentaServlet?inFrame=1&accion=cerrarRuta&id_repartidor=${id}`;
    }
  };
})();
