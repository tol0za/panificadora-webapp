// /static/js/editNota.js — EDICIÓN con soporte de fila "nueva" (resta merma)
(function () {
  "use strict";

  document.addEventListener("DOMContentLoaded", () => {
    const nf    = new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" });
    const money = (n)=> nf.format(Number(n)||0);
    const toInt = (v)=>{ v=(''+(v??'')).replace(/[^\d-]/g,''); const n=parseInt(v||'0',10); return Number.isFinite(n)?Math.max(0,n):0; };

    // --- Inventario para opciones y fallback de precio ---
    let INV = {};
    try {
      const raw = document.getElementById('inventarioJson')?.textContent
               || document.getElementById('invJson')?.textContent
               || '{}';
      INV = JSON.parse(raw);
    } catch { INV = {}; }

    // --- UI ---
    const table  = document.querySelector('#tblDet, #tblEditar, #tblDetalle, .table-editar');
    if (!table) return;

    let   tbody  = table.querySelector('tbody');  // puede ser reemplazado
    const tdTotal= document.getElementById('tdTotal')
                || document.getElementById('mdTotal')
                || document.getElementById('totalEditar')
                || table.parentElement.querySelector('#tdTotal, #mdTotal, #totalEditar');
    const btnAdd = document.getElementById('btnAdd') || document.getElementById('btnAgregar');
    const btnSave= document.getElementById('btnSave') || document.querySelector('button[type="submit"].btn-primary');
    const form   = document.getElementById('formEdit');
    const field  = document.getElementById('lineas');

    // Cache de precios por empaque (sobrevive a re-renders)
    const PRICE_CACHE = Object.create(null);

    // Helpers
    const selVend = '.qtyV, .inpVendidas, input[name="vendidas"], input[name="vendidas[]"]';
    const selMer  = '.qtyM, .inpMerma,    input[name="merma"],    input[name="merma[]"]';
    function getVendInp(tr){ return tr.querySelector(selVend); }
    function getMerInp (tr){ return tr.querySelector(selMer);  }
    function getSubEl  (tr){ return tr.querySelector('.sub, .subCell') || tr.querySelector('td:nth-last-child(2)'); }

    // ---- Fila nueva (con <select>) ----
    function optionsEmpaqueHTML() {
      const ids = Object.keys(INV);
      if (!ids.length) return '<option value="" disabled>Sin inventario</option>';
      return ids.map(id => {
        const e = INV[id] || {};
        return `
          <option value="${id}"
                  data-precio="${e.precio ?? 0}"
                  data-iddist="${e.idDistribucion ?? 0}"
                  data-restante="${e.restante ?? 0}">
            ${e.nombre ?? ('Empaque ' + id)} — disp. ${e.restante ?? 0}
          </option>`;
      }).join('');
    }

    function applySelToDataset(tr){
      const opt = tr.querySelector('.selEmp')?.selectedOptions?.[0];
      const id  = parseInt(opt?.value || '0', 10) || 0;
      const price = Number(opt?.dataset?.precio) || 0;
      const dist  = parseInt(opt?.dataset?.iddist || opt?.dataset?.idDist || '0', 10) || 0;
      tr.dataset.id    = id || '';
      tr.dataset.price = price || '';
      tr.dataset.dist  = dist || '';
      tr.dataset.mode  = 'capture';     // marca la fila como "nueva" (captura)
      if (id && price > 0) PRICE_CACHE[id] = price;
    }

    function addRow(){
      const hasInv = Object.keys(INV).length > 0;
      const html = `
        <tr data-id="" data-dist="" data-price="" data-orig="0" data-mode="capture">
          <td>
            <select class="form-select form-select-sm selEmp" ${hasInv?'':'disabled'}>
              ${optionsEmpaqueHTML()}
            </select>
          </td>
          <td><input type="number" class="form-control qtyV" min="0" step="1" value="0"></td>
          <td><input type="number" class="form-control qtyM" min="0" step="1" value="0"></td>
          <td class="sub" data-val="0">0.00</td>
          <td class="text-center">
            <button type="button" class="btn btn-sm btn-outline-danger btn-eliminar">
              <i class="bi bi-trash"></i>
            </button>
          </td>
        </tr>`;
      tbody.insertAdjacentHTML('beforeend', html);
      const tr = tbody.lastElementChild;
      applySelToDataset(tr);
      calcRow(tr); calcTotal();
    }

    // ---- Precio robusto ----
    function resolvePrice(tr){
      // 1) dataset.price
      let price = Number(tr.dataset.price);
      // 2) option (si es fila nueva)
      if (!Number.isFinite(price) || price <= 0) {
        price = Number(tr.querySelector('.selEmp')?.selectedOptions?.[0]?.dataset?.precio);
      }
      // 3) caché
      if (!Number.isFinite(price) || price <= 0) {
        const id = parseInt(tr.dataset.id || '0', 10) || 0;
        price = Number(PRICE_CACHE[id]);
      }
      // 4) inventario
      if (!Number.isFinite(price) || price <= 0) {
        const id = parseInt(tr.dataset.id || '0', 10) || 0;
        price = Number(INV[id]?.precio);
      }
      // 5) último recurso: subtotal mostrado / vendidas
      if (!Number.isFinite(price) || price <= 0) {
        const vend = toInt(getVendInp(tr)?.value);
        const shown= parseFloat((getSubEl(tr)?.textContent || '').replace(/[^\d.]/g,''));
        if (vend > 0 && Number.isFinite(shown)) price = shown / vend;
      }
      if (Number.isFinite(price) && price > 0) {
        tr.dataset.price = price;
        const id = parseInt(tr.dataset.id || '0', 10) || 0;
        if (id) PRICE_CACHE[id] = price;
      }
      return price || 0;
    }

    // ---- Modo por fila: 'edit' = existente, 'capture' = nueva ----
    function rowMode(tr){
      return tr.dataset.mode || (tr.querySelector('.selEmp') ? 'capture' : 'edit');
    }

    // ---- Cálculo de fila ----
    function calcRow(tr){
      const vend = toInt(getVendInp(tr)?.value);
      const mer  = toInt(getMerInp(tr)?.value);
      const price= resolvePrice(tr);

      // nuevas → cobradas = vendidas − merma ; existentes → cobradas = vendidas
      const cobr = (rowMode(tr)==='capture') ? Math.max(0, vend - mer) : vend;
      const sub  = cobr * price;

      const subEl = getSubEl(tr);
      if (subEl){ subEl.dataset.val = String(sub); subEl.textContent = sub.toFixed(2); }
      return true;
    }

    function calcTotal(){
      let tot = 0;
      tbody.querySelectorAll('tr').forEach(tr=>{
        calcRow(tr);
        const v = Number(getSubEl(tr)?.dataset?.val || 0);
        tot += Number.isFinite(v) ? v : 0;
      });
      if (tdTotal) tdTotal.textContent = tot.toFixed(2);
      if (btnSave) btnSave.disabled = tot <= 0;
    }

    // ---- Serialización para el servlet ----
 // --- Serialización para el servlet (vendidas SIEMPRE brutas) ---
function serialize(){
  const rows=[];
  tbody.querySelectorAll('tr').forEach(tr=>{
    // id/dist: de data-* (existentes) o del <select> (nuevas)
    let id   = parseInt(tr.dataset.id || '0', 10) || 0;
    let dist = parseInt(tr.dataset.dist || '0', 10) || 0;
    const sel = tr.querySelector('.selEmp');
    if (sel){
      const opt = sel.selectedOptions[0];
      id   = parseInt(opt?.value || id || '0', 10) || 0;
      dist = parseInt(opt?.dataset?.iddist || opt?.dataset?.idDist || dist || '0', 10) || 0;
      if (!tr.dataset.id)   tr.dataset.id   = id;
      if (!tr.dataset.dist) tr.dataset.dist = dist;
    }

    const price = Number(tr.dataset.price) || resolvePrice(tr);
    const vUI   = toInt(getVendInp(tr)?.value);  // lo que ve el usuario en la columna "Vendidas (cobradas)" para existentes
    const mUI   = toInt(getMerInp(tr)?.value);

    // EXISTENTE => "cobradas" en UI => convertir a brutas para BD
    // NUEVA     => UI ya captura brutas => se envía tal cual
    const mode  = rowMode(tr); // 'edit' | 'capture'
    const vendidasBrutas = (mode === 'edit') ? (vUI + mUI) : vUI;

    if (id) rows.push({
      idEmpaque:      id,
      idDistribucion: dist,
      vendidas:       vendidasBrutas,
      merma:          mUI,
      precio:         price
    });
  });
  if (field) field.value = JSON.stringify(rows);
}


    // ---- Delegación de eventos ----
    tbody.addEventListener('change', ev=>{
      if (ev.target.matches('.selEmp')) { applySelToDataset(ev.target.closest('tr')); calcRow(ev.target.closest('tr')); calcTotal(); }
    });
    tbody.addEventListener('input', ev=>{
      if (ev.target.matches('input[type="number"]')) { calcRow(ev.target.closest('tr')); calcTotal(); }
    });
    tbody.addEventListener('click', ev=>{
      const del = ev.target.closest('.btn-eliminar');
      if (del){ ev.preventDefault(); del.closest('tr')?.remove(); calcTotal(); }
    });

    if (btnAdd){ btnAdd.addEventListener('click', (e)=>{ e.preventDefault(); addRow(); }); }
    if (form){  form.addEventListener('submit', ()=>{ serialize(); }); }

    // ---- Observa reemplazos de TBODY y filas nuevas ----
    const mo = new MutationObserver(muts=>{
      let replaced=false, added=false;
      muts.forEach(m=>{
        m.addedNodes?.forEach(n=>{
          if (n.nodeType===1 && n.tagName==='TBODY'){ tbody = n; replaced=true; }
          if (n.nodeType===1 && n.matches?.('tr')) added=true;
        });
      });
      if (replaced || added){
        tbody.querySelectorAll('tr').forEach(tr=>{ if(!tr.dataset.price) resolvePrice(tr); calcRow(tr); });
        calcTotal();
        setTimeout(calcTotal, 0);
      }
    });
    mo.observe(table, { childList:true, subtree:true });

    // ---- Primer pase ----
    tbody.querySelectorAll('tr').forEach(tr=>{ if(!tr.dataset.mode) tr.dataset.mode='edit'; if(!tr.dataset.price) resolvePrice(tr); });
    calcTotal();
  });
})();
