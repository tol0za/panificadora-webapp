<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    String ctx = request.getContextPath();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
%>
 <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css"/>
<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="text-secondary mb-0">
      <i class="bi bi-receipt text-primary me-2"></i>Listado de Notas de Venta
    </h3>
    <a href="<%= ctx %>/NotaVentaServlet?accion=nuevo" class="btn btn-success shadow-sm">
      <i class="bi bi-plus-circle me-1"></i> Nueva Nota
    </a>
  </div>

  <div class="input-group mb-3 shadow-sm">
    <span class="input-group-text"><i class="bi bi-search"></i></span>
    <input type="text" id="searchInput" class="form-control" placeholder="Buscar por folio, tienda o repartidor...">
  </div>

  <div class="table-responsive">
    <table class="table table-bordered table-hover align-middle shadow-sm">
      <thead class="table-light text-center">
        <tr>
          <th>Folio</th>
          <th>Fecha</th>
          <th>Tienda</th>
          <th>Repartidor</th>
          <th class="text-end">Total</th>
          <th class="text-center">Acciones</th>
        </tr>
      </thead>
      <tbody id="notaTableBody">
        <c:forEach var="n" items="${listaNotas}" varStatus="loop">
          <tr class="nota-row">
            <td>${n.folio}</td>
            <td>${n.fechaFormateada}</td>
            <td>${n.nombreTienda}</td>
            <td>${n.nombreRepartidor}</td>
            <td class="text-end">
              $ <fmt:formatNumber value="${n.totalNota}" type="number" minFractionDigits="2" />
            </td>
            <td class="text-center">
              <div class="btn-group" role="group">
                <a href="${pageContext.request.contextPath}/NotaVentaServlet?accion=ver&id=${n.idNotaVenta}" 
     class="btn btn-outline-primary btn-sm" title="Ver Detalle">
    <i class="bi bi-eye"></i>
  </a>
                <a href="#" class="btn btn-sm btn-outline-primary" title="Editar"><i class="bi bi-pencil"></i></a>
                <button class="btn btn-sm btn-outline-danger" title="Eliminar" onclick="confirmarEliminacion('${n.idNotaVenta}')">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </td>
          </tr>
        </c:forEach>

        <!-- Relleno visual hasta 10 filas -->
        <c:forEach begin="1" end="${10 - fn:length(listaNotas)}">
          <tr class="nota-row" style="height:48px;">
            <td colspan="6"></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>

  <!-- Paginación -->
  <div class="d-flex justify-content-center mt-3">
    <nav>
      <ul class="pagination" id="paginacionNotas"></ul>
    </nav>
  </div>
</div>

<script>
  const rowsPerPage = 10;
  const rows = document.querySelectorAll('.nota-row');
  const paginacion = document.getElementById('paginacionNotas');
  const searchInput = document.getElementById('searchInput');
  let currentPage = 1;

  function mostrarPagina(pagina) {
    const inicio = (pagina - 1) * rowsPerPage;
    const fin = inicio + rowsPerPage;
    rows.forEach((row, index) => {
      row.style.display = index >= inicio && index < fin ? '' : 'none';
    });
    currentPage = pagina;
    actualizarPaginacion();
  }

  function actualizarPaginacion() {
    const totalPaginas = Math.ceil(rows.length / rowsPerPage);
    paginacion.innerHTML = '';
    for (let i = 1; i <= totalPaginas; i++) {
      const li = document.createElement('li');
      li.className = 'page-item' + (i === currentPage ? ' active' : '');
      const link = document.createElement('a');
      link.className = 'page-link';
      link.textContent = i;
      link.href = '#';
      link.onclick = e => {
        e.preventDefault();
        mostrarPagina(i);
      };
      li.appendChild(link);
      paginacion.appendChild(li);
    }
  }

  function filtrarTabla() {
    const filtro = searchInput.value.toLowerCase();
    let visibles = 0;
    rows.forEach(row => {
      const texto = row.innerText.toLowerCase();
      const coincide = texto.includes(filtro);
      row.style.display = coincide ? '' : 'none';
      if (coincide) visibles++;
    });
    if (visibles <= rowsPerPage) {
      paginacion.style.display = 'none';
    } else {
      paginacion.style.display = '';
    }
    mostrarPagina(1);
  }

  function confirmarEliminacion(id) {
    Swal.fire({
      title: '¿Eliminar esta nota?',
      text: "Esta acción no se puede deshacer.",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
        window.location.href = '<%= ctx %>/NotaVentaServlet?accion=eliminar&id=' + id;
      }
    });
  }

  searchInput.addEventListener('input', filtrarTabla);
  mostrarPagina(1);
</script>