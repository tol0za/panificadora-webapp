/**
 *  JS para el módulo Notas – Bootstrap 5
 *  • Alta de nota con validación AJAX de Folio duplicado
 *  • Cálculo de subtotales / total
 *  • Serializa detalle en JSON
 *  • Cerrar ruta
 */
(() => {
  /* ---------- contexto y datos ---------- */
  const ctx = document.body.dataset.ctx || '';
  const inv = JSON.parse(document.getElementById('inventarioJson')?.textContent || '{}');

  const modalEl = document.getElementById('modalNota');
  const modal   = modalEl ? new bootstrap.Modal(modalEl) : null;

  /* ---------- controles globales ---------- */
  document.getElementById('btnNueva')?.addEventListener('click', () => modal?.show());

  /* ---------- elementos del modal ---------- */
  const tblDetalle = document.querySelector('#tblDetalle tbody');
  const totalCell  = document.getElementById('tdTotal');
  const btnAdd     = document.getElementById('btnAddLinea');
  const btnSave    = document.getElementById('btnSave') ||
                     document.querySelector('#formNota button[type="submit"]');
  const selTienda  = document.getElementById('selTienda');
  const inpFolio   = document.getElementById('inpFolio');
  const helpFolio  = document.getElementById('folioHelp');

  /* ---------- validación en vivo del Folio ---------- */
  if (inpFolio) {
    let debounce;
    inpFolio.addEventListener('input', () => {
      clearTimeout(debounce);

      const f = inpFolio.value.trim();

      /* campo vacío -> ocultar alerta pero dejar bloqueado */
      if (f === '') {
        helpFolio.classList.add('d-none');
        inpFolio.classList.remove('is-invalid');
        disableControls(true);
        return;
      }

      debounce = setTimeout(async () => {
        let dup = true;                       // por defecto: duplicado
        try {
          const r = await fetch(`${ctx}/NotaVentaServlet?accion=folioCheck&folio=` + f);
          dup = (await r.text()) === '1';
        } catch { /* error de red → mantiene dup=true */ }

        if (dup) {
          helpFolio.classList.remove('d-none');
          inpFolio.classList.add('is-invalid');
          disableControls(true);
        } else {
          helpFolio.classList.add('d-none');
          inpFolio.classList.remove('is-invalid');
          disableControls(false);
        }
      }, 300);
    });
  }

  function disableControls(dis) {
    if (selTienda) selTienda.disabled  = dis;
    if (btnAdd)    btnAdd.disabled     = dis;
    if (btnSave)   btnSave.disabled    = dis;
  }

  /* ---------- agregar línea ---------- */
  btnAdd?.addEventListener('click', addLinea);

  function addLinea() {
    if (!Object.keys(inv).length) { alert('No hay empaques disponibles'); return; }

    const tr = tblDetalle.insertRow();
    /* genera select con todas las opciones */
    let opts = '';
    for (const [id, obj] of Object.entries(inv)) {
      opts += `<option value="${id}">${obj.nombre}</option>`;
    }

    tr.innerHTML = `
      <td><select class="form-select selEmp">${opts}</select></td>
      <td><input type="number" class="form-control qtyV" min="0" value="0"></td>
      <td><input type="number" class="form-control qtyM" min="0" value="0"></td>
      <td class="sub">0.00</td>
      <td><button type="button" class="btn btn-link p-0 btn-del">✕</button></td>`;

    const sel = tr.querySelector('.selEmp');
    setDatasets(tr, sel.value);

    /* listeners */
    tr.addEventListener('input',  onRowChange);
    tr.addEventListener('change', onRowChange);
    tr.querySelector('.btn-del').onclick = () => { tr.remove(); calcTotal(); };

    calcSub(tr);
    calcTotal();
  }

  function setDatasets(tr, id){
    tr.dataset.id    = id;
    tr.dataset.dist  = inv[id].idDistribucion || 0;
    tr.dataset.price = inv[id].precio;
  }

  /* ---------- cálculo ---------- */
  function onRowChange(e){
    const tr = e.currentTarget;
    if (e.target.matches('.selEmp')) {               // cambió empaque
      setDatasets(tr, e.target.value);
    }
    calcSub(tr);
    calcTotal();
  }
  function calcSub(tr){
    const vend  = +tr.querySelector('.qtyV').value;
    const price = +tr.dataset.price || 0;
    tr.querySelector('.sub').textContent = (vend * price).toFixed(2);
  }
  function calcTotal(){
    let total = 0;
    tblDetalle.querySelectorAll('.sub').forEach(td => total += +td.textContent);
    totalCell.textContent = total.toFixed(2);
  }

  /* ---------- serializar líneas ---------- */
  document.getElementById('formNota')?.addEventListener('submit', e => {
    if (btnSave?.disabled) { e.preventDefault(); return; }    // folio inválido

    const lines = [];
    tblDetalle.querySelectorAll('tr').forEach(tr => {
      const vend = +tr.querySelector('.qtyV').value;
      const mer  = +tr.querySelector('.qtyM').value;
      if (vend === 0 && mer === 0) return;

      lines.push({
        idEmpaque:      +tr.dataset.id || 0,
        idDistribucion: +(tr.dataset.dist || 0),
        vendidas:       vend,
        merma:          mer,
        precio:         +tr.dataset.price
      });
    });
    if (!lines.length){
      e.preventDefault();
      alert('Debe agregar al menos un paquete');
    } else {
      document.getElementById('lineas').value = JSON.stringify(lines);
    }
  });

  /* ---------- cerrar ruta ---------- */
  window.cerrarRuta = id => {
    if (confirm('¿Cerrar ruta y devolver sobrante?')){
      location.href = `${ctx}/NotaVentaServlet?inFrame=1&accion=cerrarRuta&id_repartidor=${id}`;
    }
  };
})();
