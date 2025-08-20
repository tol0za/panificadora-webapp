// /static/js/editNota.js — EDICIÓN con soporte de fila "nueva" + VALIDACIÓN DE STOCK + SWEETALERT2
(function () {
  "use strict";

  document.addEventListener("DOMContentLoaded", () => {
    const nf    = new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" });
    const money = (n)=> nf.format(Number(n)||0);
    const toInt = (v)=>{ v=(''+(v??'')).replace(/[^\d-]/g,''); const n=parseInt(v||'0',10); return Number.isFinite(n)?Math.max(0,n):0; };

    // Inventario (para precio, stock y nombre)
    let INV = {};
    try {
      const raw = document.getElementById('inventarioJson')?.textContent
               || document.getElementById('invJson')?.textContent
               || '{}';
      INV = JSON.parse(raw);
    } catch { INV = {}; }

    // UI
    const table  = document.querySelector('#tblDet, #tblEditar, #tblDetalle, .table-editar');
    if (!table) return;

    let   tbody  = table.querySelector('tbody');
    const tdTotal= document.getElementById('tdTotal')
                || document.getElementById('mdTotal')
                || document.getElementById('totalEditar')
                || table.parentElement.querySelector('#tdTotal, #mdTotal, #totalEditar');
    const btnAdd = document.getElementById('btnAdd') || document.getElementById('btnAgregar');
    const btnSave= document.getElementById('btnSave') || document.querySelector('button[type="submit"].btn-primary');
    const form   = document.getElementById('formEdit');
    const field  = document.getElementById('lineas');

    // Caché de precios
    const PRICE_CACHE = Object.create(null);

    // Selectores/Helpers
    const selVend = '.qtyV, .inpVendidas, input[name="vendidas"], input[name="vendidas[]"]';
    const selMer  = '.qtyM, .inpMerma,    input[name="merma"],    input[name="merma[]"]';
    function getVendInp(tr){ return tr.querySelector(selVend); }
    function getMerInp (tr){ return tr.querySelector(selMer);  }
    function getSubEl  (tr){ return tr.querySelector('.sub, .subCell') || tr.querySelector('td:nth-last-child(2)'); }

    function empName(id){ return (INV[id] && INV[id].nombre) ? INV[id].nombre : ('Empaque #'+id); }
    function showAlert(title, html){
      if (window.Swal && Swal.fire) Swal.fire({icon:'error', title, html});
      else alert((title?title+'\n':'') + (typeof html==='string'?html:JSON.stringify(html)));
    }

    // ==== Fila nueva ====
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
      tr.dataset.mode  = 'capture';
      if (!('orig' in tr.dataset)) tr.dataset.orig = '0';
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
      calcRow(tr);
      validateAll(); // valida y recalcula total
      
    }

    // ==== Precio robusto ====
    function resolvePrice(tr){
      let price = Number(tr.dataset.price);
      if (!Number.isFinite(price) || price <= 0) price = Number(tr.querySelector('.selEmp')?.selectedOptions?.[0]?.dataset?.precio);
      if (!Number.isFinite(price) || price <= 0) price = Number(PRICE_CACHE[parseInt(tr.dataset.id||'0',10)||0]);
      if (!Number.isFinite(price) || price <= 0) price = Number(INV[parseInt(tr.dataset.id||'0',10)||0]?.precio);
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

    // ==== Modo por fila ====
    function rowMode(tr){ return tr.dataset.mode || (tr.querySelector('.selEmp') ? 'capture' : 'edit'); }

    // ==== Subtotal ====
    function calcRow(tr){
      const vend = toInt(getVendInp(tr)?.value);
      const mer  = toInt(getMerInp(tr)?.value);
      const price= resolvePrice(tr);
      const cobr = (rowMode(tr)==='capture') ? Math.max(0, vend - mer) : vend; // nuevas restan merma; existentes ya son cobradas
      const sub  = cobr * price;
      const subEl = getSubEl(tr);
      if (subEl){ subEl.dataset.val = String(sub); subEl.textContent = sub.toFixed(2); }
      return true;
    }
    function calcTotal(){
      let tot = 0;
      tbody.querySelectorAll('tr').forEach(tr=>{
        const v = Number(getSubEl(tr)?.dataset?.val || 0);
        tot += Number.isFinite(v) ? v : 0;
      });
      if (tdTotal) tdTotal.textContent = tot.toFixed(2);
      return tot;
    }

    // ===== VALIDACIÓN DE STOCK =====
    function validateAll(){
      // brutas originales por empaque (vendidas+merma al guardar anteriormente)
      const origByEmp = {};
      tbody.querySelectorAll('tr').forEach(tr=>{
        const id = toInt(tr.dataset.id);
        const o  = toInt(tr.dataset.orig);
        origByEmp[id] = (origByEmp[id]||0) + o;
      });

      // brutas nuevas propuestas (edit: cobradas+merma; capture: vendidas+merma)
      const useByEmp = {};
      tbody.querySelectorAll('tr').forEach(tr=>{
        const id  = toInt(tr.dataset.id);
        const vendUI = toInt(getVendInp(tr)?.value);
        const merUI  = toInt(getMerInp(tr)?.value);
        const brutas = vendUI + merUI;
        useByEmp[id] = (useByEmp[id]||0) + brutas;
      });

      let hasInvalid = false;
      const reasons = []; // para SweetAlert

      tbody.querySelectorAll('tr').forEach(tr=>{
        const id  = toInt(tr.dataset.id);
        const vendUI = toInt(getVendInp(tr)?.value);
        const merUI  = toInt(getMerInp(tr)?.value);
        const restante = (INV[id] && Number.isFinite(+INV[id].restante)) ? Number(INV[id].restante) : 0;
        const permitido = restante + (origByEmp[id]||0);
        const uso = (useByEmp[id]||0);
        const mode = rowMode(tr);

        // merma válida: en nuevas merma<=vendidas; en existentes solo no negativa
        const mermaOk = (mode==='edit') ? (merUI >= 0) : (merUI <= vendUI);

        const invalid = !mermaOk || (uso > permitido) || vendUI < 0 || merUI < 0;
        tr.classList.toggle('table-danger', invalid);
        if (invalid) {
          hasInvalid = true;
          let msg;
          if (!mermaOk) {
            msg = (mode==='edit')
                ? `Merma inválida en ${empName(id)}.`
                : `Merma (${merUI}) no puede ser mayor que Vendidas (${vendUI}) en ${empName(id)}.`;
          } else if (uso > permitido) {
            msg = `${empName(id)} excede stock: solicitado (vend+mer) ${uso} > permitido ${permitido} (restante ${restante} + original ${origByEmp[id]||0}).`;
          } else if (vendUI < 0 || merUI < 0) {
            msg = `Valores negativos en ${empName(id)}.`;
          }
          if (msg) reasons.push(msg);
          tr.title = msg || '';
        } else {
          tr.removeAttribute('title');
        }
        // actualiza subtotal
        calcRow(tr);
      });

      const tot = calcTotal();
      if (btnSave) {
        btnSave.disabled = hasInvalid || tot <= 0;
        // guarda razones en dataset para mostrarlas al intentar guardar
        btnSave.dataset.errors = hasInvalid ? JSON.stringify(reasons) : '';
      }
    }

    // ===== Serialización para backend (vendidas SIEMPRE brutas) =====
    function serialize(){
      const rows=[];
      tbody.querySelectorAll('tr').forEach(tr=>{
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
        const vUI   = toInt(getVendInp(tr)?.value);
        const mUI   = toInt(getMerInp(tr)?.value);
        const vendidasBrutas = vUI + mUI; // (edit: cobradas+merma) | (capture: vendidas+merma)

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

    // Delegación
    tbody.addEventListener('change', ev=>{
      if (ev.target.matches('.selEmp')) { applySelToDataset(ev.target.closest('tr')); validateAll(); }
    });
    tbody.addEventListener('input', ev=>{
      if (ev.target.matches('input[type="number"]')) { validateAll(); }
    });
    tbody.addEventListener('click', ev=>{
      const del = ev.target.closest('.btn-eliminar');
      if (del){ ev.preventDefault(); del.closest('tr')?.remove(); validateAll(); }
    });

    if (btnAdd){ btnAdd.addEventListener('click', (e)=>{ e.preventDefault(); addRow(); }); }

    if (form){
      form.addEventListener('submit', (ev)=>{
        // Si hay errores preparados, muestra SweetAlert y no envía
        if (btnSave && btnSave.disabled) {
          const reasons = btnSave.dataset.errors ? JSON.parse(btnSave.dataset.errors) : ['Revise filas marcadas en rojo.'];
          const html = '<ul style="text-align:left;margin:0;padding-left:18px;">' +
                       reasons.map(r=>`<li>${r}</li>`).join('') + '</ul>';
          showAlert('No se puede guardar', html);
          ev.preventDefault(); return false;
        }
        // Serializa y envia
        serialize();
      });
    }

    // Observer por si reemplazan <tbody>
    const mo = new MutationObserver(muts=>{
      let replaced=false, added=false;
      muts.forEach(m=>{
        m.addedNodes?.forEach(n=>{
          if (n.nodeType===1 && n.tagName==='TBODY'){ tbody = n; replaced=true; }
          if (n.nodeType===1 && n.matches?.('tr')) added=true;
        });
      });
      if (replaced || added){
        tbody.querySelectorAll('tr').forEach(tr=>{
          if(!tr.dataset.mode) tr.dataset.mode='edit';
          if(!tr.dataset.price) resolvePrice(tr);
        });
        validateAll();
      }
    });
    mo.observe(table, { childList:true, subtree:true });

    // Primer pase
    tbody.querySelectorAll('tr').forEach(tr=>{
      if(!tr.dataset.mode) tr.dataset.mode='edit';
      if(!tr.dataset.price) resolvePrice(tr);
    });
    validateAll();
  });
})();
