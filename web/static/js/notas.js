// /static/js/notas.js  —  Nueva nota (modal)
(function () {
  "use strict";

  // ========= Formato / helpers =========
  const fmtMoney = (n) =>
    new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" })
      .format(Number(n) || 0);

  const clampInt = (v) => {
    v = (v ?? "").toString().replace(/[^\d-]/g, "");
    const n = parseInt(v || "0", 10);
    return Number.isFinite(n) ? Math.max(0, n) : 0;
  };

  // ========= Inventario inyectado (fallback si el <option> no trae data-*) =========
  let INV = {};
  try {
    const raw = document.getElementById("inventarioJson")?.textContent
             || document.getElementById("invJson")?.textContent
             || "{}";
    INV = JSON.parse(raw);
  } catch { INV = {}; }

  // ========= Elementos UI =========
  const modalEl     = document.getElementById("modalNota");
  const modal       = (modalEl && typeof bootstrap !== "undefined") ? new bootstrap.Modal(modalEl) : null;
  const tbody       = document.querySelector("#tblDetalle tbody, #tblDet tbody");
  const tdTotal     = document.getElementById("tdTotal")
                   || document.getElementById("mdTotal")
                   || document.getElementById("totalEditar")
                   || (tbody && tbody.closest("table")?.parentElement?.querySelector("#tdTotal, #mdTotal, #totalEditar"));
  const btnNueva    = document.getElementById("btnNueva");
  const btnAddLinea = document.getElementById("btnAddLinea");
  const btnSave     = document.getElementById("btnSave") || document.querySelector('.btn-primary[type="submit"]');
  const selTienda   = document.getElementById("selTienda");
  const inpFolio    = document.getElementById("inpFolio");
  const lineasField = document.getElementById("lineas");

  const helpBad  = document.getElementById("folioHelpBad");
  const helpGood = document.getElementById("folioHelpGood");
  let folioOk = true;

  // ========= Construcción de filas =========
  function opcionesEmpaqueHTML() {
    const ids = Object.keys(INV);
    if (!ids.length) return '<option value="" disabled>Sin inventario</option>';
    return ids.map(id => {
      const e = INV[id] || {};
      return `
        <option value="${id}"
                data-precio="${e.precio ?? 0}"
                data-restante="${e.restante ?? 0}"
                data-iddist="${e.idDistribucion ?? 0}">
          ${e.nombre ?? ('ID ' + id)} — disp. ${e.restante ?? 0}
        </option>`;
    }).join("");
  }

  function rowHTML() {
    return `
      <tr>
        <td><select class="form-select form-select-sm selEmp">${opcionesEmpaqueHTML()}</select></td>
        <td><input class="form-control form-control-sm inpVendidas" type="number" min="0" value="0"></td>
        <td><input class="form-control form-control-sm inpMerma"    type="number" min="0" value="0"></td>
        <td class="text-end cobradasCell">0</td>
        <td class="text-end"><span class="sub" data-val="0">0.00</span></td>
        <td class="text-center">
          <button type="button" class="btn btn-sm btn-outline-danger btnDel"><i class="bi bi-trash"></i></button>
        </td>
      </tr>`;
  }

  // ========= Info precio/stock con fallback a INV =========
  function getInfo(tr) {
    const sel = tr.querySelector(".selEmp");
    const opt = sel?.selectedOptions?.[0];
    const id  = parseInt(opt?.value || tr.dataset.id || "0", 10) || 0;

    let precio = Number(opt?.dataset?.precio);
    let rest   = parseInt(opt?.dataset?.restante || "", 10);

    if (!Number.isFinite(precio) || precio <= 0) precio = Number(INV[id]?.precio)   || 0;
    if (!Number.isFinite(rest)   || rest   <  0) rest   = parseInt(INV[id]?.restante || "0", 10) || 0;

    return { precio, rest, id };
  }

  // ========= Recalc fila: COBRADAS = vendidas - merma; Subtotal = cobradas * precio =========
  function recalcRow(tr) {
    const vInp = tr.querySelector(".inpVendidas");
    const mInp = tr.querySelector(".inpMerma");
    const sub  = tr.querySelector(".sub");
    const cobC = tr.querySelector(".cobradasCell");
    if (!vInp || !mInp || !sub) return true;

    const vend = clampInt(vInp.value);
    const mer  = clampInt(mInp.value);
    vInp.value = vend; mInp.value = mer;

    const { precio, rest } = getInfo(tr);

    const excedeStock = (vend + mer) > rest; // lo que realmente se mueve del reparto
    const mermaMayor  = mer > vend;
    const invalid     = excedeStock || mermaMayor || vend < 0 || mer < 0;

    tr.classList.toggle("table-danger", invalid);
    vInp.classList.toggle("is-invalid", invalid);
    mInp.classList.toggle("is-invalid", invalid);

    const cobradas = Math.max(0, vend - mer);
    if (cobC) cobC.textContent = String(cobradas);

    const subtotal = cobradas * (Number.isFinite(precio) ? precio : 0);
    sub.dataset.val = String(subtotal);
    sub.textContent = fmtMoney(subtotal);

    return !invalid;
  }

  function recalcTotal() {
    if (!tbody || !tdTotal || !btnSave) return;
    let tot = 0, ok = true, filas = 0;
    tbody.querySelectorAll("tr").forEach(tr => {
      filas++;
      if (!recalcRow(tr)) ok = false;
      const v = Number(tr.querySelector(".sub")?.dataset.val || 0);
      tot += Number.isFinite(v) ? v : 0;
    });
    tdTotal.textContent = fmtMoney(tot);
    const tiendaOk = !!selTienda?.value;
    btnSave.disabled = !(filas > 0 && ok && tot > 0 && tiendaOk && folioOk);
  }

  // ========= Agregar fila (evita condición de carrera) =========
  function addRow() {
    if (!tbody) return;
    tbody.insertAdjacentHTML("beforeend", rowHTML());

    // Recalcular inmediatamente la nueva fila y toda la tabla
    const last = tbody.lastElementChild;
    if (last) {
      // valores seguros
      const vi = last.querySelector(".inpVendidas"); if (vi && !vi.value) vi.value = "0";
      const mi = last.querySelector(".inpMerma");    if (mi && !mi.value) mi.value = "0";
    }
    recalcTotal();                 // 1) inmediato
    setTimeout(recalcTotal, 0);    // 2) siguiente tick (por si otro script recalculó mal)
    if (typeof requestAnimationFrame === "function") {
      requestAnimationFrame(recalcTotal); // 3) tras el reflow de la nueva fila
    }
  }

  // ========= Delegación: garantiza escuchar SIEMPRE los cambios =========
  if (tbody) {
    tbody.addEventListener("input",  (ev) => {
      if (ev.target.matches('input[type="number"], select')) {
        const tr = ev.target.closest("tr");
        if (tr) { recalcRow(tr); recalcTotal(); }
      }
    });
    tbody.addEventListener("change", (ev) => {
      if (ev.target.matches('select')) {
        const tr = ev.target.closest("tr");
        if (tr) { recalcRow(tr); recalcTotal(); }
      }
    });
    tbody.addEventListener("click", (ev) => {
      if (ev.target.closest(".btnDel")) { ev.preventDefault(); ev.target.closest("tr")?.remove(); recalcTotal(); }
    });

    // Observa inserciones (si otro script agrega filas)
    const mo = new MutationObserver((muts) => {
      let added = false;
      muts.forEach(m => m.addedNodes?.forEach(n => {
        if (n.nodeType === 1 && n.matches("tr")) {
          added = true;
          const vi = n.querySelector(".inpVendidas"); if (vi && !vi.value) vi.value = "0";
          const mi = n.querySelector(".inpMerma");    if (mi && !mi.value) mi.value = "0";
          recalcRow(n);
        }
      }));
      if (added) { recalcTotal(); setTimeout(recalcTotal,0); }
    });
    mo.observe(tbody, { childList: true });
  }

  // ========= Nueva nota =========
  btnNueva?.addEventListener("click", () => {
    if (selTienda) selTienda.disabled = false;
    if (btnAddLinea) btnAddLinea.disabled = false;

    if (tbody) {
      tbody.innerHTML = "";
      addRow();
    }
    recalcTotal();
    modal?.show();
    if (inpFolio?.value) debounceFolio();
  });

  btnAddLinea?.addEventListener("click", addRow);
  selTienda?.addEventListener("change", recalcTotal);

  // ========= Guardar =========
  btnSave?.addEventListener("click", (ev) => {
    ev.preventDefault();
    if (!tbody || !lineasField || btnSave.disabled) return;

    const lineas = [];
    tbody.querySelectorAll("tr").forEach(tr => {
      const sel   = tr.querySelector(".selEmp");
      const opt   = sel?.selectedOptions?.[0];
      const idEmp = parseInt(sel?.value || "0", 10);
      if (!idEmp) return;

      const vendidas = clampInt(tr.querySelector(".inpVendidas")?.value);
      const merma    = clampInt(tr.querySelector(".inpMerma")?.value);

      let precio = Number(opt?.dataset?.precio);
      let idDist = parseInt(opt?.dataset?.iddist || opt?.dataset?.idDist || "0", 10) || 0;
      if (!Number.isFinite(precio) || precio <= 0) precio = Number(INV[idEmp]?.precio) || 0;
      if (!idDist) idDist = parseInt(INV[idEmp]?.idDistribucion || "0", 10) || 0;

      lineas.push({ idEmpaque:idEmp, idDistribucion:idDist, vendidas, merma, precio });
    });

    if (!lineas.length) return;
    lineasField.value = JSON.stringify(lineas);
    document.getElementById("formNota")?.submit();
  });

  // ========= Folio duplicado =========
  function setFolioState(ok) {
    folioOk = !!ok;
    helpBad?.classList.toggle("d-none", ok);
    helpGood?.classList.toggle("d-none", !ok);
    recalcTotal();
  }
  let tFolio;
  function debounceFolio() {
    clearTimeout(tFolio);
    const folio = (inpFolio?.value || "").trim();
    if (!folio) { setFolioState(true); return; }
    tFolio = setTimeout(() => {
      fetch(`${document.body.dataset.ctx || ""}/NotaVentaServlet?accion=folioCheck&folio=${encodeURIComponent(folio)}`)
        .then(r => r.text())
        .then(t => setFolioState((t || "").trim() !== "1"))
        .catch(() => setFolioState(true));
    }, 250);
  }
  inpFolio?.addEventListener("input", debounceFolio);

  // ========= Modal Detalle (ver) — “Cobradas = Vendidas − Merma” =========
  document.querySelectorAll(".btn-det").forEach(btn => {
    btn.addEventListener("click", (e) => {
      e.preventDefault();
      const id = btn.dataset.idnota;
      fetch(`${document.body.dataset.ctx || ""}/NotaVentaServlet?accion=detalleJson&id=${id}`)
        .then(r => r.json())
        .then(arr => {
          const md = document.getElementById("modalDet");
          if (!md) return;

          md.querySelector("#mdFolio").textContent  = btn.dataset.folio || "";
          md.querySelector("#mdTienda").textContent = btn.dataset.tienda || "";

          const body = md.querySelector("#mdBody");
          const tdT  = md.querySelector("#mdTotal");
          body.innerHTML = "";
          let total = 0;

          (arr || []).forEach(d => {
            const vend   = Number(d.cantidadVendida || 0);
            const mer    = Number(d.merma || 0);
            const cobr   = Math.max(0, vend - mer);
            const precio = Number(d.precioUnitario || 0);
            const sub = cobr * precio;
            total += sub;

            body.insertAdjacentHTML("beforeend", `
              <tr>
                <td>${d.nombreEmpaque || d.idEmpaque}</td>
                <td>${cobr}</td>
                <td>${mer}</td>
                <td class="text-end">${fmtMoney(sub)}</td>
              </tr>`);
          });

          tdT.textContent = fmtMoney(total);
          const btnEdit = md.querySelector("#mdEditar");
          if (btnEdit && id) btnEdit.href =
            `${document.body.dataset.ctx || ""}/NotaVentaServlet?inFrame=1&accion=editarNota&id=${id}`;

          if (typeof bootstrap !== "undefined") new bootstrap.Modal(md).show();
        });
    });
  });

  // Recalcular al mostrar la modal
  modalEl?.addEventListener("shown.bs.modal", recalcTotal);
})();
