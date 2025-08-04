/* -------------------------------------------------------
 *  Editar Nota de Venta  (modal NotaFormEditar.jsp)
 * ----------------------------------------------------- */
(() => {
  const inv     = JSON.parse(document.getElementById('invJson').textContent);
  const tbody   = document.querySelector('#tblDet tbody');
  const totalTD = document.getElementById('tdTotal');

  /* ---------- render inicial ---------- */
  tbody.querySelectorAll('tr').forEach(calcSub);
  calcTotal();

  /* ---------- delegación de eventos ---------- */
  tbody.addEventListener('input',  handleEdit, true);   // capturando
  tbody.addEventListener('change', handleEdit, true);   // incluye <select>

  /* eliminar fila */
  tbody.addEventListener('click', e => {
    if (e.target.matches('.btn-del, .btn-del *')) {
      e.target.closest('tr').remove();
      calcTotal();
    }
  });

  /* agregar nueva fila */
  document.getElementById('btnAdd').addEventListener('click', () => {
    const id = Object.keys(inv)[0];
    if (!id) { alert('Inventario vacío'); return; }
    insertRow(id);
    calcTotal();
  });

  /* serializar y enviar */
  document.getElementById('formEdit').addEventListener('submit', e => {
    const lines = [];
    tbody.querySelectorAll('tr').forEach(tr => {
      const vend = +tr.querySelector('.qtyV').value;
      const mer  = +tr.querySelector('.qtyM').value;
      if (vend === 0 && mer === 0) return;
lines.push({
  idEmpaque:      +tr.dataset.id,           // ← + fuerza a number
  idDistribucion: +(tr.dataset.dist || 0),  // nunca null
  vendidas:       +tr.querySelector('.qtyV').value,
  merma:          +tr.querySelector('.qtyM').value,
  precio:         +tr.dataset.price
});
    });
    if (!lines.length) { alert('Debe agregar al menos un paquete'); e.preventDefault(); }
    else document.getElementById('lineas').value = JSON.stringify(lines);
  });

/* listener delegado: vendidas, merma 𝗼 cambio de empaque */
function handleEdit(e){
  const tr = e.target.closest('tr');
  if (!tr) return;

  /* si cambió el <select> actualiza precio + nombre */
  if (e.target.matches('.selEmp')) {
    const id = e.target.value;
    tr.dataset.id    = id;
    tr.dataset.price = inv[id].precio;
    // cuando es select, el nombre ya es el valor visible
  }
  calcSub(tr);      // recalcula el subtotal de esa fila
  calcTotal();      // y el total general
}
/* crea una nueva fila con <select> de empaques ------------------- */
function insertRow(initId){
  /* crea <option> usando la KEY del objeto inv */
  let opts = '';
  for (const [id, obj] of Object.entries(inv)){
    opts += `<option value="${id}">${obj.nombre}</option>`;
  }

  const id = initId;
  const tr = tbody.insertRow();
  tr.dataset.id    = id;
  tr.dataset.dist  = inv[id].idDistribucion || 0;
  tr.dataset.price = inv[id].precio;

  tr.innerHTML = `
    <td>
      <select class="form-select selEmp">
        ${opts}
      </select>
    </td>
    <td><input type="number" class="form-control qtyV" min="0" value="0"></td>
    <td><input type="number" class="form-control qtyM" min="0" value="0"></td>
    <td class="sub">0.00</td>
    <td><button type="button" class="btn btn-link p-0 btn-del">✕</button></td>`;

  /* selecciona el empaque inicial */
  tr.querySelector('.selEmp').value = id;
}

  function calcSub(tr){
    const vend  = +tr.querySelector('.qtyV').value;
    const price = +tr.dataset.price || 0;
    const sub   = vend * price;
    tr.querySelector('.sub').textContent = sub.toFixed(2);
  }

  function calcTotal(){
    let tot = 0;
    tbody.querySelectorAll('.sub').forEach(td => tot += +td.textContent);
    totalTD.textContent = tot.toFixed(2);
  }
})();