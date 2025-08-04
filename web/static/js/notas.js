/**
 * static/js/notas.js
 * Funciones del módulo Notas de venta – Bootstrap 5
 */
(() => {
  /* ----- contexto app & data ----- */
  const ctx = document.body.dataset.ctx || '';
  const inv = JSON.parse(
    document.getElementById('inventarioJson')?.textContent || '{}'
  );

  /* ==========  NUEVA NOTA ========== */
  const modalNotaEl = document.getElementById('modalNota');
  const modalNota   = modalNotaEl ? new bootstrap.Modal(modalNotaEl) : null;
  document.getElementById('btnNueva')?.addEventListener('click', () => modalNota?.show());

  /* controles */
  const tblBody   = document.querySelector('#tblDetalle tbody');
  const totalTd   = document.getElementById('tdTotal');
  const btnAdd    = document.getElementById('btnAddLinea');
  const btnSave   = document.getElementById('btnSave') ||
                    document.querySelector('#formNota button[type="submit"]');
  const selTienda = document.getElementById('selTienda');

  /* Folio + ayudas */
  const inpFolio = document.getElementById('inpFolio');
  const helpBad  = document.getElementById('folioHelpBad');
  const helpGood = document.getElementById('folioHelpGood');

  /* ===== validación folio ===== */
  let debounce;
  function setState(st){
    if (st === 'dup') {
      helpBad .classList.remove('d-none');
      helpGood.classList.add   ('d-none');
      inpFolio.classList.add   ('is-invalid');
      inpFolio.classList.remove('is-valid');
      disableCtrls(true);
    } else if (st === 'ok') {
      helpBad .classList.add   ('d-none');
      helpGood.classList.remove('d-none');
      inpFolio.classList.remove('is-invalid');
      inpFolio.classList.add   ('is-valid');
      disableCtrls(false);
    } else {                                 // empty/reset
      helpBad .classList.add('d-none');
      helpGood.classList.add('d-none');
      inpFolio.classList.remove('is-invalid','is-valid');
      disableCtrls(true);
    }
  }
  function disableCtrls(d){
    if (selTienda) selTienda.disabled = d;
    if (btnAdd)    btnAdd.disabled    = d;
    if (btnSave)   btnSave.disabled   = d;
  }
  if (inpFolio){
    inpFolio.addEventListener('input', () => {
      clearTimeout(debounce);
      const v = inpFolio.value.trim();
      if (v === '') { setState('empty'); return; }

      debounce = setTimeout(async () => {
        let dup = true;
        try{
          const r = await fetch(`${ctx}/NotaVentaServlet?accion=folioCheck&folio=`+v);
          dup = (await r.text()) === '1';
        }catch{}
        setState(dup?'dup':'ok');
      },300);
    });
  }

  /* reset modal */
  modalNotaEl?.addEventListener('hidden.bs.modal', () => {
    document.getElementById('formNota').reset();
    tblBody.innerHTML = '';
    totalTd.textContent = '0.00';
    setState('empty');
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
      opts+=`<option value="${id}">${o.nombre}</option>`;
    return `
      <td><select class="form-select selEmp">${opts}</select></td>
      <td><input type="number" class="form-control qtyV" min="0" value="0"></td>
      <td><input type="number" class="form-control qtyM" min="0" value="0"></td>
      <td class="sub">0.00</td>
      <td><button type="button" class="btn btn-sm btn-outline-danger p-0 btn-del"  title="Eliminar"><i class="bi bi-trash"></i></button></td>`;
  }
  function syncDataset(tr,id){
    tr.dataset.id    = id;
    tr.dataset.dist  = inv[id].idDistribucion || 0;
    tr.dataset.price = inv[id].precio;
  }
  function onRowChange(e){
    const tr=e.currentTarget;
    if(e.target.matches('.selEmp')) syncDataset(tr,e.target.value);
    calcSub(tr); calcTotal();
  }
  function calcSub(tr){
    const vend=+tr.querySelector('.qtyV').value, p=+tr.dataset.price||0;
    tr.querySelector('.sub').textContent=(vend*p).toFixed(2);
  }
  function calcTotal(){
    let t=0; tblBody.querySelectorAll('.sub').forEach(td=>t+=+td.textContent);
    totalTd.textContent=t.toFixed(2);
  }

  /* ===== serializar y enviar ===== */
  document.getElementById('formNota')?.addEventListener('submit', e=>{
    if(btnSave?.disabled){ e.preventDefault(); return; }
    const lines=[];
    tblBody.querySelectorAll('tr').forEach(tr=>{
      const v=+tr.querySelector('.qtyV').value,
            m=+tr.querySelector('.qtyM').value;
      if(v===0&&m===0) return;
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
  window.cerrarRuta = id=>{
    if(confirm('¿Cerrar ruta y devolver sobrante?')){
      location.href=`${ctx}/NotaVentaServlet?inFrame=1&accion=cerrarRuta&id_repartidor=${id}`;
    }
  };

  /* ==========  DETALLE NOTA (modal) ========== */
  const modalDetEl = document.getElementById('modalDet');
  const modalDet   = modalDetEl ? new bootstrap.Modal(modalDetEl) : null;

 document.querySelectorAll('.btn-det').forEach(btn => {
  btn.addEventListener('click', e => {
    e.preventDefault();

    const idNota  = btn.dataset.idnota;   // ← minúsculas todas
    const folio   = btn.dataset.folio;
    const tienda  = btn.dataset.tienda;

    // coloca texto en el encabezado del modal
    document.getElementById('mdFolio').textContent  = `#${folio}`;
    document.getElementById('mdTienda').textContent = tienda;
    document.getElementById('mdEditar').href =
        `${ctx}/NotaVentaServlet?inFrame=1&accion=editarNota&id=` + idNota;

    // limpia tabla detalle
    const cuerpo = document.getElementById('mdBody');
    cuerpo.innerHTML = '';
    document.getElementById('mdTotal').textContent = '0.00';

    // llama al servlet
    fetch(`${ctx}/NotaVentaServlet?accion=detalleJson&id=`+idNota)
      .then(r => r.ok ? r.json() : Promise.reject())
      .then(data => {
        let total = 0;
        data.forEach(l => {
          const tr = document.createElement('tr');
          tr.innerHTML = `
             <td>${l.nombreEmpaque}</td>
             <td>${l.cantidadVendida}</td>
             <td>${l.merma}</td>
             <td>${l.totalLinea.toFixed(2)}</td>`;
          cuerpo.appendChild(tr);
          total += l.totalLinea;
        });
        document.getElementById('mdTotal')
                .textContent = total.toFixed(2);
        bootstrap.Modal.getOrCreateInstance(
          document.getElementById('modalDet')).show();
      })
      .catch(()=>alert('No se pudo cargar el detalle'));
  });
});

  async function mostrarDetalle(id){
    try{
      const r = await fetch(`${ctx}/NotaVentaServlet?accion=detalleJson&id=`+id);
      const d = await r.json();

      document.getElementById('mdFolio').textContent  = '#'+d.folio;
      document.getElementById('mdTienda').textContent = d.tienda;
      document.getElementById('mdTotal').textContent  = (+d.total).toFixed(2);

      const tb = document.getElementById('mdBody');
      tb.innerHTML='';
      d.lineas.forEach(l=>{
        tb.insertAdjacentHTML('beforeend',`
          <tr>
            <td>${l.empaque}</td>
            <td>${l.vendidas}</td>
            <td>${l.merma}</td>
            <td>${(+l.sub).toFixed(2)}</td>
          </tr>`);
      });

      document.getElementById('mdEditar').href =
        `${ctx}/NotaVentaServlet?inFrame=1&accion=editarNota&id=`+id;

      modalDet.show();
    }catch{ alert('No se pudo cargar el detalle'); }
  }
})();
