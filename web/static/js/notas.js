/**
 * static/js/notas.js – Módulo Notas de venta (Bootstrap 5)
 * ▸ Mantiene TODAS las funciones originales.
 * ▸ Añade validación de stock fila-a-fila en tiempo real.
 */
(() => {
  /* ---------- contexto y datos ---------- */
  const ctx = document.body.dataset.ctx || '';
  const inv = JSON.parse(document.getElementById('inventarioJson')?.textContent || '{}');

  /* ---------- NUEVA NOTA (modal) ---------- */
  const modalNotaEl = document.getElementById('modalNota');
  const modalNota   = modalNotaEl ? new bootstrap.Modal(modalNotaEl) : null;
  document.getElementById('btnNueva')?.addEventListener('click', () => modalNota?.show());

  const tblBody   = document.querySelector('#tblDetalle tbody');
  const totalTd   = document.getElementById('tdTotal');
  const btnAdd    = document.getElementById('btnAddLinea');
  const btnSave   = document.getElementById('btnSave') ||
                    document.querySelector('#formNota button[type="submit"]');
  const selTienda = document.getElementById('selTienda');

  /* ---------- validación folio (AJAX) ---------- */
  const inpFolio = document.getElementById('inpFolio');
  const helpBad  = document.getElementById('folioHelpBad');
  const helpGood = document.getElementById('folioHelpGood');

  let debounce;
  function setState(st){
    if (st === 'dup') {
      helpBad .classList.remove('d-none'); helpGood.classList.add   ('d-none');
      inpFolio.classList.add('is-invalid'); inpFolio.classList.remove('is-valid');
      disableCtrls(true);
    } else if (st === 'ok') {
      helpBad .classList.add('d-none'); helpGood.classList.remove('d-none');
      inpFolio.classList.remove('is-invalid'); inpFolio.classList.add('is-valid');
      disableCtrls(false);
    } else { // reset
      helpBad.classList.add('d-none'); helpGood.classList.add('d-none');
      inpFolio.classList.remove('is-invalid','is-valid');
      disableCtrls(true);
    }
  }
  function disableCtrls(d){
    selTienda.disabled = d; btnAdd.disabled = d; btnSave.disabled = d;
  }
  inpFolio?.addEventListener('input', ()=>{
    clearTimeout(debounce);
    const v = inpFolio.value.trim();
    if (v === '') { setState('empty'); return; }
    debounce = setTimeout(async()=>{
      let dup = true;
      try{
        const r = await fetch(`${ctx}/NotaVentaServlet?accion=folioCheck&folio=`+v);
        dup = (await r.text()) === '1';
      }catch{}
      setState(dup ? 'dup':'ok');
    },300);
  });

  /* reset modal */
  modalNotaEl?.addEventListener('hidden.bs.modal', ()=>{
    document.getElementById('formNota').reset();
    tblBody.innerHTML=''; totalTd.textContent='0.00';
    setState('empty');
  });

  /* ---------- agregar línea ---------- */
  btnAdd?.addEventListener('click', ()=>{
    if (!Object.keys(inv).length){ alert('Sin empaques disponibles'); return; }
    const tr = tblBody.insertRow();
    tr.innerHTML = makeRowHTML(inv);
    const sel = tr.querySelector('.selEmp');
    syncDataset(tr, sel.value);
    attachQtyListeners(tr);
    sel.addEventListener('change',  e=>{ syncDataset(tr,e.target.value); validarFila({target:tr.querySelector('.inpVendidas')}); });
    tr.querySelector('.btn-del').onclick = ()=>{ tr.remove(); calcTotal(); validarFormulario(); };
    calcSub(tr); calcTotal(); validarFormulario();
  });

  function makeRowHTML(map){
    let opts=''; for(const [id,o] of Object.entries(map))
      opts+=`<option value="${id}">${o.nombre}</option>`;
    return `
      <td><select class="form-select selEmp">${opts}</select></td>
      <td><input type="number" class="form-control inpVendidas inpQty" min="0" value="0"></td>
      <td><input type="number" class="form-control inpMerma   inpQty" min="0" value="0"></td>
      <td class="sub">0.00</td>
      <td><button type="button" class="btn btn-sm btn-outline-danger p-0 btn-del"
                 title="Eliminar"><i class="bi bi-trash"></i></button></td>`;
  }
  function syncDataset(tr,id){
    tr.dataset.idEmpaque   = id;
    tr.dataset.dist        = inv[id].idDistribucion || 0;
    tr.dataset.price       = inv[id].precio;
  }

  /* ---------- cálculo de subtotales / total ---------- */
  function calcSub(tr){
    const vend = +tr.querySelector('.inpVendidas').value || 0;
    const price= +tr.dataset.price || 0;
    tr.querySelector('.sub').textContent = (vend*price).toFixed(2);
  }
  function calcTotal(){
    let t=0;
    tblBody.querySelectorAll('.sub').forEach(td=> t += +td.textContent);
    totalTd.textContent = t.toFixed(2);
  }

  /* ---------- listeners de cantidad ---------- */
  function attachQtyListeners(row){
    row.querySelectorAll('.inpQty').forEach(inp=>{
      inp.addEventListener('input', validarFila);
    });
  }

  /* ---------- validación stock fila-a-fila ---------- */
  function validarFila(e){
    const tr       = e.target.closest('tr');
    const idEmp    = +tr.dataset.idEmpaque;
    const vendidas = +tr.querySelector('.inpVendidas').value || 0;
    const merma    = +tr.querySelector('.inpMerma').value    || 0;
    const total    = vendidas + merma;
    const restante = inv[idEmp].restante;

    let help = tr.querySelector('.help-stock');
    if (!help){
      help = document.createElement('div');
      help.className = 'help-stock text-danger small';
      tr.cells[3].appendChild(help);
    }

    if (total > restante){
      help.textContent = `Máximo ${restante} piezas disponibles`;
      tr.classList.add('table-danger'); tr.dataset.ok='0';
    }else{
      help.textContent=''; tr.classList.remove('table-danger'); tr.dataset.ok='1';
    }
    calcSub(tr); calcTotal(); validarFormulario();
  }

  /* ---------- habilita / deshabilita guardar ---------- */
  function validarFormulario(){
    const ok = [...tblBody.querySelectorAll('tr')].every(tr=>tr.dataset.ok!=='0');
    btnSave.disabled = !ok || tblBody.rows.length===0;
  }

  /* ---------- serializar y enviar ---------- */
  document.getElementById('formNota')?.addEventListener('submit',e=>{
    if(btnSave.disabled){ e.preventDefault(); return; }
    const lines=[];
    tblBody.querySelectorAll('tr').forEach(tr=>{
      const v=+tr.querySelector('.inpVendidas').value,
            m=+tr.querySelector('.inpMerma').value;
      if(v===0 && m===0) return;
      lines.push({
        idEmpaque:      +tr.dataset.idEmpaque || 0,
        idDistribucion: +tr.dataset.dist      || 0,
        vendidas: v, merma: m, precio: +tr.dataset.price
      });
    });
    if(!lines.length){ e.preventDefault(); alert('Debe agregar al menos un paquete'); }
    else document.getElementById('lineas').value = JSON.stringify(lines);
  });

  /* ---------- cerrar ruta ---------- */
  window.cerrarRuta = id=>{
    if(confirm('¿Cerrar ruta y devolver sobrante?')){
      location.href = `${ctx}/NotaVentaServlet?inFrame=1&accion=cerrarRuta&id_repartidor=${id}`;
    }
  };

  /* ---------- DETALLE NOTA (modal) ---------- */
  const modalDetEl = document.getElementById('modalDet');
  const modalDet   = modalDetEl ? new bootstrap.Modal(modalDetEl) : null;

  document.querySelectorAll('.btn-det').forEach(btn=>{
    btn.addEventListener('click', async e=>{
      e.preventDefault();
      const idNota  = btn.dataset.idnota,
            folio   = btn.dataset.folio,
            tienda  = btn.dataset.tienda;

      document.getElementById('mdFolio').textContent  = `#${folio}`;
      document.getElementById('mdTienda').textContent = tienda;
      document.getElementById('mdEditar').href =
          `${ctx}/NotaVentaServlet?inFrame=1&accion=editarNota&id=`+idNota;

      const cuerpo = document.getElementById('mdBody');
      cuerpo.innerHTML=''; document.getElementById('mdTotal').textContent='0.00';

      try{
        const r   = await fetch(`${ctx}/NotaVentaServlet?accion=detalleJson&id=`+idNota);
        const dat = await r.json();
        let tot=0;
        dat.forEach(l=>{
          cuerpo.insertAdjacentHTML('beforeend',`
            <tr>
              <td>${l.nombreEmpaque}</td>
              <td>${l.cantidadVendida}</td>
              <td>${l.merma}</td>
              <td>${(+l.totalLinea).toFixed(2)}</td>
            </tr>`);
          tot += l.totalLinea;
        });
        document.getElementById('mdTotal').textContent = tot.toFixed(2);
        modalDet.show();
      }catch{ alert('No se pudo cargar el detalle'); }
    });
  });
})();