/**
 *  JS para el módulo Notas  (Bootstrap 5)
 *  • Validación AJAX de Folio (rojo/verde)
 *  • Cálculo de subtotales / total
 *  • Serializa líneas en JSON
 *  • Cerrar ruta
 */
(() => {
  /* ---------- contexto & datos ---------- */
  const ctx = document.body.dataset.ctx || '';
  const inv = JSON.parse(document.getElementById('inventarioJson')?.textContent || '{}');

  /* ---------- modal ---------- */
  const modalEl = document.getElementById('modalNota');
  const modal   = modalEl ? new bootstrap.Modal(modalEl) : null;

  /* ---------- controles globales ---------- */
  document.getElementById('btnNueva')?.addEventListener('click', () => modal?.show());

  /* ---------- refs dentro del modal ---------- */
  const tblBody  = document.querySelector('#tblDetalle tbody');
  const totalTd  = document.getElementById('tdTotal');

  const btnAdd   = document.getElementById('btnAddLinea');
  const btnSave  = document.getElementById('btnSave') ||
                   document.querySelector('#formNota button[type="submit"]');
  const selTienda= document.getElementById('selTienda');

  const inpFolio = document.getElementById('inpFolio');
  const helpBad  = document.getElementById('folioHelpBad');   // rojo
  const helpGood = document.getElementById('folioHelpGood');  // verde

  /* ===== validación en vivo del Folio ===== */
  if (inpFolio) {
    let db;
    inpFolio.addEventListener('input', () => {
      clearTimeout(db);
      const v = inpFolio.value.trim();

      /* vacío → estado 'empty' */
      if (v === '') { setState('empty'); return; }

      db = setTimeout(async () => {
        let dup = true;                                       // por defecto
        try {
          const r = await fetch(`${ctx}/NotaVentaServlet?accion=folioCheck&folio=`+v);
          dup = (await r.text()) === '1';
        } catch {}                                            // red error mantiene dup=true
        setState(dup ? 'dup' : 'ok');
      }, 300);
    });
  }

  function setState(st){
    if (st === 'dup'){                                        // duplicado
      helpBad .classList.remove('d-none');
      helpGood.classList.add   ('d-none');
      inpFolio.classList.add   ('is-invalid');
      inpFolio.classList.remove('is-valid');
      disableCtrls(true);
    } else if (st === 'ok'){                                  // disponible
      helpBad .classList.add   ('d-none');
      helpGood.classList.remove('d-none');
      inpFolio.classList.remove('is-invalid');
      inpFolio.classList.add   ('is-valid');
      disableCtrls(false);
    } else {                                                  // empty / reset
      helpBad .classList.add('d-none');
      helpGood.classList.add('d-none');
      inpFolio.classList.remove('is-invalid','is-valid');
      disableCtrls(true);
    }
  }

  function disableCtrls(d){
    selTienda && (selTienda.disabled = d);
    btnAdd    && (btnAdd.disabled   = d);
    btnSave   && (btnSave.disabled  = d);
  }

  /* reset total formulario al cerrar modal */
  modalEl?.addEventListener('hidden.bs.modal', () => {
    document.getElementById('formNota').reset();
    tblBody.innerHTML = '';
    totalTd.textContent = '0.00';
    setState('empty');               // limpia mensajes/estados
  });

  /* ===== agregar línea ===== */
  btnAdd?.addEventListener('click', () => {
    if (!Object.keys(inv).length){ alert('Sin empaques disponibles'); return; }

    const tr = tblBody.insertRow();
    tr.innerHTML = makeRowHTML(inv);

    const sel = tr.querySelector('.selEmp');
    syncDataset(tr, sel.value);

    tr.addEventListener('input',  onRowChange);
    tr.addEventListener('change', onRowChange);
    tr.querySelector('.btn-del').onclick = () => { tr.remove(); calcTotal(); };

    calcSub(tr); calcTotal();
  });

  function makeRowHTML(map){
    let opts=''; for(const [id,o] of Object.entries(map))
      opts += `<option value="${id}">${o.nombre}</option>`;
    return `
      <td><select class="form-select selEmp">${opts}</select></td>
      <td><input type="number" class="form-control qtyV" min="0" value="0"></td>
      <td><input type="number" class="form-control qtyM" min="0" value="0"></td>
      <td class="sub">0.00</td>
      <td><button type="button" class="btn btn-link p-0 btn-del">✕</button></td>`;
  }
  function syncDataset(tr,id){
    tr.dataset.id    = id;
    tr.dataset.dist  = inv[id].idDistribucion || 0;
    tr.dataset.price = inv[id].precio;
  }

  /* cálculos */
  function onRowChange(e){
    const tr = e.currentTarget;
    if (e.target.matches('.selEmp')) syncDataset(tr, e.target.value);
    calcSub(tr); calcTotal();
  }
  function calcSub(tr){
    const vend = +tr.querySelector('.qtyV').value;
    const p    = +tr.dataset.price || 0;
    tr.querySelector('.sub').textContent = (vend*p).toFixed(2);
  }
  function calcTotal(){
    let t=0; tblBody.querySelectorAll('.sub').forEach(td=>t+=+td.textContent);
    totalTd.textContent = t.toFixed(2);
  }

  /* ===== serializar líneas y enviar ===== */
  document.getElementById('formNota')?.addEventListener('submit', e=>{
    if (btnSave?.disabled){ e.preventDefault(); return; }

    const lines=[];
    tblBody.querySelectorAll('tr').forEach(tr=>{
      const v=+tr.querySelector('.qtyV').value;
      const m=+tr.querySelector('.qtyM').value;
      if (v===0 && m===0) return;
      lines.push({
        idEmpaque:+tr.dataset.id||0,
        idDistribucion:+tr.dataset.dist||0,
        vendidas:v, merma:m, precio:+tr.dataset.price
      });
    });
    if(!lines.length){ e.preventDefault(); alert('Debe agregar al menos un paquete'); }
    else document.getElementById('lineas').value = JSON.stringify(lines);
  });

  /* ===== cerrar ruta ===== */
  window.cerrarRuta = id =>{
    if(confirm('¿Cerrar ruta y devolver sobrante?')){
      location.href = `${ctx}/NotaVentaServlet?inFrame=1&accion=cerrarRuta&id_repartidor=${id}`;
    }
  };
})();
