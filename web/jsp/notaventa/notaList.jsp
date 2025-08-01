<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Notas de Venta</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
  <style>
    .table-fixed tbody {
      height: 500px;
      display: block;
      overflow-y: auto;
    }
    .table-fixed thead, .table-fixed tbody tr {
      display: table;
      width: 100%;
      table-layout: fixed;
    }
  </style>
</head>
<body>
<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="text-secondary">Notas de Venta</h3>
    <a href="${ctx}/NotasVentaServlet?accion=nuevo" class="btn btn-success">
      <i class="bi bi-plus-circle"></i> Nueva Nota
    </a>
  </div>

  <input type="text" id="busqueda" class="form-control mb-3" placeholder="Buscar por folio, tienda o repartidor...">

  <div class="table-responsive">
    <table class="table table-bordered table-hover table-fixed">
      <thead class="table-light">
        <tr>
          <th>Folio</th>
          <th>Tienda</th>
          <th>Repartidor</th>
          <th>Fecha</th>
        </tr>
      </thead>
      <tbody id="notaBody">
        <!-- Utilizamos 'notasVenta' como atributo principal, pero también se soporta 'notas' desde el servlet -->
        <c:forEach var="nv" items="${notasVenta}" varStatus="estado">
          <tr>
            <!-- Mostramos el ID de la nota como folio ya que la clase NotaVenta no tiene propiedad folio -->
            <td>${nv.idNota}</td>
            <!-- La clase NotaVenta expone directamente el nombre de la tienda y el nombre/apellido del repartidor -->
            <td>${nv.nombreTienda}</td>
            <td>${nv.nombreRepartidor} ${nv.apellidoRepartidor}</td>
            <!-- La propiedad 'fecha' de NotaVenta se utiliza para la fecha de la nota -->
            <td><fmt:formatDate value="${nv.fecha}" pattern="dd/MM/yyyy HH:mm:ss"/></td>
          </tr>
        </c:forEach>
        <c:forEach var="i" begin="1" end="${10 - fn:length(notasVenta)}">
          <tr><td colspan="4">&nbsp;</td></tr>
        </c:forEach>
      </tbody>
    </table>
  </div>

  <nav class="mt-3">
    <ul class="pagination justify-content-center" id="paginacion">
      <!-- Se insertan dinámicamente con JS -->
    </ul>
  </nav>
</div>

<script>
  const filas = document.querySelectorAll("#notaBody tr");
  const porPagina = 10;
  let paginaActual = 1;

  function mostrarPagina(pagina) {
    paginaActual = pagina;
    let inicio = (pagina - 1) * porPagina;
    let fin = inicio + porPagina;
    filas.forEach((fila, index) => {
      fila.style.display = (index >= inicio && index < fin) ? "" : "none";
    });
    generarPaginacion();
  }

  function generarPaginacion() {
    let totalPaginas = Math.ceil(filas.length / porPagina);
    const pag = document.getElementById("paginacion");
    pag.innerHTML = "";

    for (let i = 1; i <= totalPaginas; i++) {
      const li = document.createElement("li");
      li.className = "page-item " + (i === paginaActual ? "active" : "");
      li.innerHTML = `<a class="page-link" href="#">${i}</a>`;
      li.onclick = () => mostrarPagina(i);
      pag.appendChild(li);
    }
  }

  document.getElementById("busqueda").addEventListener("input", function () {
    const valor = this.value.toLowerCase();
    let visibles = 0;

    filas.forEach(fila => {
      const texto = fila.innerText.toLowerCase();
      const coincide = texto.includes(valor);
      fila.style.display = coincide ? "" : "none";
      if (coincide) visibles++;
    });

    generarPaginacion();
  });

  // Mostrar página inicial
  mostrarPagina(1);

  <c:if test="${not empty sessionScope.mensaje}">
    Swal.fire({
      icon: 'info',
      title: 'Mensaje',
      text: '${sessionScope.mensaje}'
    });
    <c:remove var="mensaje" scope="session"/>
  </c:if>
</script>
</body>
</html>
