<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
 
  <meta charset="UTF-8" />
  <title>Nueva Nota de Venta</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet"/>
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
     <script>
  const ctx = '${ctx}';
</script>
</head>
<body>
<div class="container py-4">
  <h3 class="mb-4 text-secondary">Nueva Nota de Venta</h3>

  <form action="${ctx}/NotaVentaServlet" method="post" onsubmit="return validarFormulario()">
    <div class="row mb-3">
      <div class="col-md-6">
        <label for="id_repartidor" class="form-label">Repartidor</label>
        <select name="id_repartidor" id="id_repartidor" class="form-select" required>
          <option value="" disabled selected>Seleccione</option>
          <c:forEach var="r" items="${repartidores}">
            <option value="${r.idRepartidor}">${r.nombreRepartidor}</option>
          </c:forEach>
        </select>
      </div>
      <div class="col-md-6">
        <label for="id_tienda" class="form-label">Tienda</label>
        <select name="id_tienda" id="id_tienda" class="form-select" required>
          <option value="" disabled selected>Seleccione</option>
          <c:forEach var="t" items="${tiendas}">
            <option value="${t.idTienda}">${t.nombreTienda}</option>
          </c:forEach>
        </select>
      </div>
    </div>

    <input type="hidden" name="id_distribucion" id="id_distribucion" />

    <table class="table table-bordered align-middle">
      <thead class="table-light text-center">
        <tr>
          <th>Producto</th>
          <th>Stock</th>
          <th>Cantidad</th>
          <th>Merma</th>
          <th>Precio</th>
          <th>Total Línea</th>
          <th></th>
        </tr>
      </thead>
      <tbody id="detalleTabla"></tbody>
    </table>

    <button type="button" class="btn btn-outline-primary mb-3" onclick="agregarFila()">
      <i class="bi bi-plus-circle"></i> Agregar Producto
    </button>

    <div class="d-flex justify-content-end">
      <button type="submit" class="btn btn-success">
        <i class="bi bi-save"></i> Guardar Nota
      </button>
    </div>
  </form>
</div>

<script>
let opcionesEmpaque = [];

document.getElementById('id_repartidor').addEventListener('change', function () {
  const id = this.value;
  fetch(`${ctx}/EmpaquesPorRepartidorServlet?idRepartidor=${id}`)
    .then(res => res.json())
   .then(data => {
  console.log("Productos recibidos del servlet:", data); // 👈 Verifica aquí
  opcionesEmpaque = data;
  document.getElementById('id_distribucion').value = data.length > 0 ? data[0].id_distribucion : "";
  document.getElementById("detalleTabla").innerHTML = "";
})
    .catch(error => {
      console.error("❌ Error al cargar productos:", error);
      Swal.fire("Error", "No se pudieron cargar los productos", "error");
    });
});

function agregarFila() {
  if (opcionesEmpaque.length === 0) {
    Swal.fire("Aviso", "Primero seleccione un repartidor con productos registrados", "info");
    return;
  }

  const tr = document.createElement("tr");

  // select producto
  const tdEmpaque = document.createElement("td");
  const select = document.createElement("select");
  select.name = "id_empaque[]";
  select.classList.add("form-select");
  select.required = true;
  select.innerHTML = '<option disabled selected value="">Seleccione</option>';
  opcionesEmpaque.forEach(e => {
    const option = document.createElement("option");
    option.value = e.id_empaque;
    option.textContent = e.nombre_empaque;
    option.dataset.precio = e.precio_unitario;
    option.dataset.stock = e.stock;
    select.appendChild(option);
  });

  select.addEventListener("change", function () {
    const precio = this.selectedOptions[0].dataset.precio;
    const stock = this.selectedOptions[0].dataset.stock;

    console.log("📦 Producto seleccionado:", { precio, stock }); // <-- DEBUG 2

    tr.querySelector(".precio").value = precio;
    tr.querySelector(".stock").textContent = stock;
    actualizarTotal(tr);
  });

  tdEmpaque.appendChild(select);
  tr.appendChild(tdEmpaque);

  // stock
  const tdStock = document.createElement("td");
  tdStock.classList.add("text-center", "stock");
  tdStock.textContent = "-";
  tr.appendChild(tdStock);

  // cantidad
  const tdCantidad = document.createElement("td");
  const inputCantidad = document.createElement("input");
  inputCantidad.name = "cantidad[]";
  inputCantidad.type = "number";
  inputCantidad.classList.add("form-control");
  inputCantidad.min = 0;
  inputCantidad.value = 0;
  inputCantidad.required = true;
  inputCantidad.addEventListener("input", () => actualizarTotal(tr));
  tdCantidad.appendChild(inputCantidad);
  tr.appendChild(tdCantidad);

  // merma
  const tdMerma = document.createElement("td");
  const inputMerma = document.createElement("input");
  inputMerma.name = "merma[]";
  inputMerma.type = "number";
  inputMerma.classList.add("form-control");
  inputMerma.min = 0;
  inputMerma.value = 0;
  tdMerma.appendChild(inputMerma);
  tr.appendChild(tdMerma);

  // precio
  const tdPrecio = document.createElement("td");
  const inputPrecio = document.createElement("input");
  inputPrecio.name = "precio_unitario[]";
  inputPrecio.type = "number";
  inputPrecio.classList.add("form-control", "precio");
  inputPrecio.min = 0;
  inputPrecio.step = "0.01";
  inputPrecio.readOnly = true;
  tdPrecio.appendChild(inputPrecio);
  tr.appendChild(tdPrecio);

  // total línea
  const tdTotal = document.createElement("td");
  tdTotal.classList.add("text-end", "fw-bold", "total");
  tdTotal.textContent = "$0.00";
  tr.appendChild(tdTotal);

  // eliminar
  const tdEliminar = document.createElement("td");
  const btnEliminar = document.createElement("button");
  btnEliminar.type = "button";
  btnEliminar.classList.add("btn", "btn-sm", "btn-danger");
  btnEliminar.innerHTML = '<i class="bi bi-x-lg"></i>';
  btnEliminar.onclick = () => tr.remove();
  tdEliminar.appendChild(btnEliminar);
  tr.appendChild(tdEliminar);

  document.getElementById("detalleTabla").appendChild(tr);
}
</script>
</body>
</html>