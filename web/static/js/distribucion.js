/* =========================================================================
 *  static/js/distribucion.js  (v12)
 *  – Sin duplicados, botón Agregar se oculta si ya no hay stock libre
 * =========================================================================*/
(()=>{
  const ctx = document.body.dataset.ctx || "";
  const $   = (sel, ctx=document) => ctx.querySelector(sel);
  const $$  = (sel, ctx=document) => ctx.querySelectorAll(sel);

  const MODAL   = $('#mdlNuevaSalida');
  const TBL     = $('#tblLineas tbody');
  const BTN_ADD = $('#btnAgregarLinea');
  const BTN_SAVE= $('#btnGuardar');
  const ALERT   = $('#alertLineaInvalida');
  const SEL_REP = $('#frmNuevaSalida select[name="idRepartidor"]');

  const SEL_NAME = 'select[name="id_empaque[]"]';
  const QTY_NAME = 'input[name="cantidad[]"]';

  let INV_LIST = null;
  let INV_MAP  = {};

  function resetCache(){
    INV_LIST = null;
    INV_MAP  = {};
  }

  async function loadInventario(){
    if (INV_LIST) return INV_LIST;
    const r = await fetch(`${ctx}/InventarioDisponibleServlet`, {headers:{'X-Requested-With':'fetch'}});
    if(!r.ok) throw new Error('Inventario no disponible');
    INV_LIST = await r.json();
    INV_MAP  = {};
    INV_LIST.forEach(e => { INV_MAP[e.id_empaque] = e; });
    return INV_LIST;
  }

  function parseIntSafe(v){ const n = parseInt((v||'').toString().trim(),10); return Number.isFinite(n)?n:0; }

  function refreshStockCell(tr){
    const sel   = tr.querySelector(SEL_NAME);
    const stock = parseIntSafe(sel?.selectedOptions?.[0]?.dataset?.restante);
    const cell  = tr.querySelector('.stockDisp');
    const qty   = tr.querySelector(QTY_NAME);
    if (cell) cell.textContent = String(stock||0);
    if (qty){
      qty.max = stock||0;
      if (+qty.value > (stock||0)) qty.value = stock||0;
      if (+qty.value < 1) qty.value = stock>0 ? 1 : 0;
    }
  }

  function validateRow(tr){
    const sel  = tr.querySelector(SEL_NAME);
    const qty  = tr.querySelector(QTY_NAME);
    const stock = parseIntSafe(sel?.selectedOptions?.[0]?.dataset?.restante);
    const id    = parseIntSafe(sel?.value);

    const invalid = (!id) || (parseIntSafe(qty?.value) <= 0) || (parseIntSafe(qty?.value) > stock);
    tr.classList.toggle('table-danger', invalid);
    if (qty) qty.classList.toggle('is-invalid', invalid);
    return !invalid;
  }

  function validateAll(){
    const rows = Array.from(TBL?.querySelectorAll('tr') || []);
    if (!rows.length){
      BTN_SAVE?.setAttribute('disabled','disabled');
      ALERT?.classList.add('d-none');
      return false;
    }
    let ok = true;
    rows.forEach(tr=>{ if(!validateRow(tr)) ok=false; });
    if (!ok){
      BTN_SAVE?.setAttribute('disabled','disabled');
      ALERT?.classList.remove('d-none');
    } else {
      BTN_SAVE?.removeAttribute('disabled');
      ALERT?.classList.add('d-none');
    }
    return ok;
  }

  // Ocultar/mostrar botón Agregar según stock disponible
  function toggleAddButton(){
    if (!INV_LIST) return;
    const used = new Set(Array.from(TBL.querySelectorAll(SEL_NAME)).map(s=>parseIntSafe(s.value)).filter(Boolean));
    const libres = INV_LIST.filter(e=>!used.has(e.id_empaque));
    if (!libres.length) BTN_ADD?.classList.add("d-none");
    else BTN_ADD?.classList.remove("d-none");
  }

  /* ================================
   *  AGREGAR LÍNEA
   * ================================*/
  if (BTN_ADD){
    BTN_ADD.addEventListener("click", async ()=>{
      try{
        await loadInventario();
        const rows    = Array.from(TBL.querySelectorAll('tr'));
        const chosen  = new Set(rows.map(r=>parseIntSafe(r.querySelector(SEL_NAME)?.value)).filter(Boolean));
        const free    = INV_LIST.filter(e=>!chosen.has(e.id_empaque));
        if (!free.length){
          toggleAddButton();
          return Swal.fire("Sin stock disponible","Todos los empaques ya fueron agregados.","info");
        }

        const first   = free[0];
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>
            <select name="id_empaque[]" class="form-select form-select-sm">
              ${free.map(e => `<option value="${e.id_empaque}" data-restante="${e.stock}" ${e.id_empaque===first.id_empaque?'selected':''}>${e.nombre_empaque}</option>`).join('')}
            </select>
          </td>
          <td class="stockDisp">${first.stock}</td>
          <td><input name="cantidad[]" type="number" min="1" max="${first.stock}" value="1" class="form-control form-control-sm"></td>
          <td><button type="button" class="btn btn-outline-danger btn-sm quitar"><i class="bi bi-x-lg"></i></button></td>`;
        TBL.appendChild(tr);

        const sel = tr.querySelector(SEL_NAME);
        const qty = tr.querySelector(QTY_NAME);

        tr.querySelector(".quitar").onclick = ()=>{ tr.remove(); validateAll(); toggleAddButton(); };
        sel.onchange = ()=>{ refreshStockCell(tr); validateAll(); toggleAddButton(); };
        qty.oninput  = ()=> validateAll();

        refreshStockCell(tr);
        validateAll();
        toggleAddButton();

      }catch(err){
        Swal.fire("Error","Inventario no disponible","error");
      }
    });
  }

  /* ================================
   *  RESET al abrir/cambiar repartidor
   * ================================*/
  if (MODAL) MODAL.addEventListener('show.bs.modal', ()=>{ resetCache(); if (TBL) TBL.innerHTML=""; toggleAddButton(); validateAll(); });
  if (SEL_REP) SEL_REP.addEventListener('change', ()=>{ resetCache(); if (TBL) TBL.innerHTML=""; toggleAddButton(); validateAll(); });

  /* ================================
   *  GUARDAR SALIDA
   * ================================*/
  const FRM = $("#frmNuevaSalida");
  if (BTN_SAVE){
    BTN_SAVE.onclick = async ()=>{
      if (!FRM) return;
      if(!FRM.checkValidity()){ FRM.reportValidity(); return; }
      if(!validateAll()) return;

      const data = new URLSearchParams(new FormData(FRM));
      const r = await fetch(`${ctx}/DistribucionServlet`,{
        method:"POST",
        headers:{ "X-Requested-With":"fetch" },
        body:data
      });
      if(r.ok){
        bootstrap.Modal.getInstance($("#mdlNuevaSalida")).hide();
        location.reload();
      }else{
        const msg = await r.text();
        Swal.fire("Error", msg || "No se pudo guardar","error");
      }
    };
  }

  /* ================================
   *  EDITAR INLINE / ELIMINAR / VER DETALLE
   * ================================*/
  $$(".editarCantidad").forEach(inp=>{
    inp.onchange = ()=>{
      if(inp.value<=0) return Swal.fire("Cantidad inválida","","error");
      fetch(`${ctx}/DistribucionServlet`,{
        method:"POST",
        headers:{ "Content-Type":"application/x-www-form-urlencoded","X-Requested-With":"fetch"},
        body:new URLSearchParams({accion:"editarLinea", idDistribucion: inp.dataset.id, cantidad: inp.value})
      }).then(()=>location.reload());
    };
  });

  $$(".eliminarSalida").forEach(btn=>{
    btn.onclick = ()=>{
      Swal.fire({
        icon:"warning", title:"Eliminar salida",
        text:"¿Seguro? El stock regresará a bodega.",
        showCancelButton:true, confirmButtonText:"Sí, eliminar"
      }).then(r=>{
        if(r.isConfirmed){
          location.href=`${ctx}/DistribucionServlet?accion=delete&idRepartidor=${btn.dataset.rep}&fecha=${btn.dataset.fecha}`;
        }
      });
    };
  });

  $$(".verDetalle").forEach(btn=>{
    btn.onclick = async ()=>{
      const r = await fetch(`${ctx}/DistribucionServlet?accion=detalle&idRepartidor=${btn.dataset.rep}&fecha=${btn.dataset.fecha}`, { headers:{ "X-Requested-With":"fetch" }});
      if(!r.ok) return Swal.fire("Error","Sin detalle","error");
      const data = await r.json();
      $("#tblDetalle").innerHTML = data.map(e=>`<tr><td>${e.empaque}</td><td class="text-end">${e.cantidad}</td></tr>`).join("");
      new bootstrap.Modal($("#mdlDetalle")).show();
    };
  });

})();
