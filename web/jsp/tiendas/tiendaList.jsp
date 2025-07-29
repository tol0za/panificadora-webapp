<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Listado de Tiendas</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
  <style>
    a, img, button, i {
      -webkit-user-drag: none;
      user-drag: none;
    }
  </style>
</head>
<body>
<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="text-secondary">Listado de Tiendas</h3>
    <a href="${ctx}/CatalogoTiendaServlet?accion=nuevo" class="btn btn-success" draggable="false">
      <i class="bi bi-plus-circle"></i> Nueva Tienda
    </a>
  </div>

  <input type="text" class="form-control mb-3" id="buscarInput" placeholder="Buscar tienda por nombre o dirección...">

  <div class="table-responsive rounded shadow-sm">
    <table class="table table-bordered table-hover bg-white align-middle" id="tablaTiendas">
      <thead class="table-light">
        <tr>
          <th>ID</th>
          <th>Nombre</th>
          <th>Dirección</th>
          <th>Teléfono</th>
          <th class="text-center">Acciones</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="t" items="${listaTiendas}">
          <tr>
            <td>${t.idTienda}</td>
            <td>${t.nombre}</td>
            <td>${t.direccion}</td>
            <td>${t.telefono}</td>
            <td class="text-center">
              <a href="${ctx}/CatalogoTiendaServlet?accion=editar&id=${t.idTienda}" 
                 class="btn btn-sm btn-outline-primary me-1" draggable="false">
                <i class="bi bi-pencil-square" draggable="false"></i>
              </a>
              <a href="${ctx}/CatalogoTiendaServlet?accion=eliminar&id=${t.idTienda}" 
                 class="btn btn-sm btn-outline-danger btn-eliminar" draggable="false">
                <i class="bi bi-trash" draggable="false"></i>
              </a>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>

  <nav class="mt-3">
    <ul class="pagination justify-content-center" id="paginacion"></ul>
  </nav>
</div>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
  // Evita que cualquier cosa sea arrastrada
  document.addEventListener("dragstart", function(e) {
    e.preventDefault();
  });

  const tabla = document.getElementById("tablaTiendas").getElementsByTagName("tbody")[0];
  const buscarInput = document.getElementById("buscarInput");
  const filasOriginales = Array.from(tabla.rows).map(row => row.cloneNode(true));
  const filasPorPagina = 10;
  let paginaActual = 1;

  const renderTabla = (filas) => {
    tabla.innerHTML = '';
    const inicio = (paginaActual - 1) * filasPorPagina;
    const fin = inicio + filasPorPagina;
    const filasPagina = filas.slice(inicio, fin);
    filasPagina.forEach(row => tabla.appendChild(row));
    for (let i = filasPagina.length; i < filasPorPagina; i++) {
      const tr = document.createElement('tr');
      tr.innerHTML = '<td colspan="5" style="height:48px;"></td>';
      tabla.appendChild(tr);
    }
  };

  const renderPaginacion = (filas) => {
    const totalPaginas = Math.ceil(filas.length / filasPorPagina) || 1;
    const paginacion = document.getElementById("paginacion");
    paginacion.innerHTML = '';
    for (let i = 1; i <= totalPaginas; i++) {
      const li = document.createElement("li");
      li.className = 'page-item' + (i === paginaActual ? ' active' : '');
      const a = document.createElement("a");
      a.className = "page-link";
      a.href = "#";
      a.textContent = i;
      a.addEventListener("click", (e) => {
        e.preventDefault();
        paginaActual = i;
        renderTabla(filas);
        renderPaginacion(filas);
      });
      li.appendChild(a);
      paginacion.appendChild(li);
    }
  };

  const aplicarFiltro = () => {
    const filtro = buscarInput.value.toLowerCase();
    const filtradas = filasOriginales.filter(row => row.innerText.toLowerCase().includes(filtro));
    paginaActual = 1;
    renderTabla(filtradas);
    renderPaginacion(filtradas);
  };

  buscarInput.addEventListener("input", aplicarFiltro);
  aplicarFiltro();

  document.querySelectorAll('.btn-eliminar').forEach(btn => {
    btn.addEventListener('click', function(e) {
      e.preventDefault();
      const url = this.getAttribute('href');
      Swal.fire({
        title: '¿Estás seguro?',
        text: "Esta acción no se puede deshacer.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Sí, eliminar'
      }).then((result) => {
        if (result.isConfirmed) {
          window.location.href = url;
        }
      });
    });
  });

  <c:if test="${sessionScope.accesoDenegado}">
    Swal.fire({
      icon: 'error',
      title: 'Acceso denegado',
      text: 'No tienes permiso para acceder a este módulo',
      showConfirmButton: false,
      timer: 1500
    });
    <c:remove var="accesoDenegado" scope="session" />
    setTimeout(() => {
      window.location.href = '${ctx}/jsp/bienvenida.jsp';
    }, 1500);
  </c:if>
</script>

<jsp:include page="/includes/alerta.jsp" />
</body>
</html>
