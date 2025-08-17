/* =========================================================================
 *  static/js/distribucion.js  (v7)
 *  – Envía siempre X-Requested-With: fetch
 * =========================================================================*/
(()=>{
  const ctx = document.body.dataset.ctx || "";
  const $   = (sel, ctx=document) => ctx.querySelector(sel);
  const $$  = (sel, ctx=document) => ctx.querySelectorAll(sel);

  /* ---- fecha encabezado ---- */
  $("#fechaHoy").textContent = new Date().toLocaleDateString(
      "es-MX",{day:"2-digit",month:"2-digit",year:"numeric"});

  /* ==============================================================
   *  AGREGAR LÍNEA AL MODAL
   * ==============================================================*/
  $("#btnAgregarLinea").addEventListener("click", async ()=>{
    const r = await fetch(`${ctx}/InventarioDisponibleServlet`);
    if(!r.ok)  return Swal.fire("Error","Inventario no disponible","error");

    const lista = await r.json();
    if(!lista.length) return Swal.fire("Sin stock","No hay empaques","info");

    const tr=document.createElement("tr");
    tr.innerHTML=`
      <td><select name="id_empaque[]" class="form-select form-select-sm">
            ${lista.map(e=>`<option value="${e.id_empaque}">${e.nombre_empaque}</option>`).join("")}
          </select></td>
      <td class="stockDisp">${lista[0].stock}</td>
      <td><input name="cantidad[]" type="number" min="1" max="${lista[0].stock}"
                 value="1" class="form-control form-control-sm"></td>
      <td><button type="button" class="btn btn-outline-danger btn-sm quitar">
            <i class="bi bi-x-lg"></i></button></td>`;
    $("#tblLineas tbody").appendChild(tr);

    tr.querySelector(".quitar").onclick = ()=>tr.remove();

    const sel = tr.querySelector("select");
    const inp = tr.querySelector("input");
    sel.onchange = ()=>{
      const emp = lista.find(e=>e.id_empaque==sel.value);
      tr.querySelector(".stockDisp").textContent = emp.stock;
      inp.max = emp.stock;
      if(+inp.value>emp.stock) inp.value = emp.stock;
    };
  });

  /* ==============================================================
   *  GUARDAR SALIDA  (POST AJAX con encabezado)
   * ==============================================================*/
  $("#btnGuardar").onclick = async ()=>{
    const form = $("#frmNuevaSalida");
    if(!form.checkValidity()){ form.reportValidity(); return; }

    const data = new URLSearchParams(new FormData(form));
    const r = await fetch(`${ctx}/DistribucionServlet`,{
      method:"POST",
      headers:{ "X-Requested-With":"fetch" },      // 👈 clave
      body:data
    });

  if(r.ok){
  bootstrap.Modal.getInstance($("#mdlNuevaSalida")).hide();
  location.reload();
}else{
  const msg = await r.text();                    // NEW
  Swal.fire("Error", msg || "No se pudo guardar","error");
}
  };

  /* ==============================================================
   *  EDITAR CANTIDAD inline
   * ==============================================================*/
  $$(".editarCantidad").forEach(inp=>{
    inp.onchange = ()=>{
      if(inp.value<=0) return Swal.fire("Cantidad inválida","","error");
      fetch(`${ctx}/DistribucionServlet`,{
        method:"POST",
        headers:{
          "Content-Type":"application/x-www-form-urlencoded",
          "X-Requested-With":"fetch"              // 👈 igualmente
        },
        body:new URLSearchParams({
          accion:"editarLinea",
          idDistribucion: inp.dataset.id,
          cantidad: inp.value
        })
      }).then(()=>location.reload());
    };
  });

  /* ==============================================================
   *  ELIMINAR SALIDA
   * ==============================================================*/
  $$(".eliminarSalida").forEach(btn=>{
    btn.onclick = ()=>{
      Swal.fire({
        icon:"warning",
        title:"Eliminar salida",
        text:"¿Seguro? El stock regresará a bodega.",
        showCancelButton:true,
        confirmButtonText:"Sí, eliminar"
      }).then(r=>{
        if(r.isConfirmed){
          location.href=`${ctx}/DistribucionServlet?accion=delete`
                       +`&idRepartidor=${btn.dataset.rep}`
                       +`&fecha=${btn.dataset.fecha}`;
        }
      });
    };
  });

  /* ==============================================================
   *  VER DETALLE
   * ==============================================================*/
  $$(".verDetalle").forEach(btn=>{
    btn.onclick = async ()=>{
      const r = await fetch(`${ctx}/DistribucionServlet?accion=detalle`
                 +`&idRepartidor=${btn.dataset.rep}`
                 +`&fecha=${btn.dataset.fecha}`,
                 { headers:{ "X-Requested-With":"fetch" }});
      if(!r.ok) return Swal.fire("Error","Sin detalle","error");

      const data = await r.json();
      $("#tblDetalle").innerHTML = data.map(e=>`
         <tr><td>${e.empaque}</td><td class="text-end">${e.cantidad}</td></tr>`
      ).join("");
      new bootstrap.Modal($("#mdlDetalle")).show();
    };
  });
})();
