<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <title>Catálogo de Empaques</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet" />
</head>
<body class="bg-light">

<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="text-secondary">Catálogo de Empaques</h3>
    <a href="${ctx}/CatalogoEmpaqueServlet?accion=nuevo" class="btn btn-success" draggable="false">
      <i class="bi bi-plus-circle" draggable="false"></i> Nuevo Empaque
    </a>
  </div>

  <input type="text" id="buscarInput" class="form-control mb-3" placeholder="Buscar por nombre...">

  <div class="table-responsive shadow-sm rounded">
    <table class="table table-bordered table-hover bg-white align-middle" id="tablaEmpaques">
      <thead class="table-light">
        <tr>
          <th>ID</th>
          <th>Nombre</th>
          <th>Precio Unitario</th>
          <th class="text-center">Acciones</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="e" items="${listaEmpaques}">
          <tr>
            <td>${e.idEmpaque}</td>
            <td>${e.nombreEmpaque}</td>
            <td>$${e.precioUnitario}</td>
            <td class="text-center">
              <a href="${ctx}/CatalogoEmpaqueServlet?accion=editar&id=${e.idEmpaque}" class="btn btn-sm btn-outline-primary me-1">
                <i class="bi bi-pencil-square"></i>
              </a>
              <a href="${ctx}/CatalogoEmpaqueServlet?accion=eliminar&id=${e.idEmpaque}" class="btn btn-sm btn-outline-danger btn-eliminar">
                <i class="bi bi-trash"></i>
              </a>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>

  <!-- Paginación -->
  <nav class="mt-3">
    <ul class="pagination justify-content-center" id="paginacion"></ul>
  </nav>
</div>

<!-- SweetAlert2 -->
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

<script>
  document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById("buscarInput");
    const tablaBody = document.getElementById("tablaEmpaques").getElementsByTagName("tbody")[0];
    const allRows = Array.from(tablaBody.rows).map(row => row.cloneNode(true));
    const rowsPerPage = 10;
    let currentPage = 1;

    const renderTabla = (rows) => {
      tablaBody.innerHTML = '';
      const start = (currentPage - 1) * rowsPerPage;
      const end = start + rowsPerPage;
      const pageRows = rows.slice(start, end);

      pageRows.forEach(row => tablaBody.appendChild(row));

      // Completar con filas vacías
      const faltan = rowsPerPage - pageRows.length;
      for (let i = 0; i < faltan; i++) {
        const empty = document.createElement('tr');
        empty.innerHTML = '<td colspan="4" style="height:48px;"></td>';
        tablaBody.appendChild(empty);
      }
    };

    const renderPaginacion = (rows) => {
      const totalPages = Math.ceil(rows.length / rowsPerPage) || 1;
      const paginacion = document.getElementById("paginacion");
      paginacion.innerHTML = '';

      for (let i = 1; i <= totalPages; i++) {
        const li = document.createElement("li");
        li.className = 'page-item' + (i === currentPage ? ' active' : '');
        const a = document.createElement("a");
        a.className = "page-link";
        a.href = "#";
        a.textContent = i;
        a.addEventListener("click", (e) => {
          e.preventDefault();
          currentPage = i;
          renderTabla(rows);
          renderPaginacion(rows);
        });
        li.appendChild(a);
        paginacion.appendChild(li);
      }
    };

    const aplicarFiltroYPaginacion = () => {
      const filtro = input.value.toLowerCase();
      const filtradas = allRows.filter(row => row.innerText.toLowerCase().includes(filtro));
      currentPage = 1;
      renderTabla(filtradas);
      renderPaginacion(filtradas);
    };

    input.addEventListener("input", aplicarFiltroYPaginacion);
    aplicarFiltroYPaginacion();

    // SweetAlert2 para eliminar
    document.querySelectorAll('.btn-eliminar').forEach(btn => {
      btn.addEventListener('click', function (e) {
        e.preventDefault();
        const url = this.getAttribute('href');
        Swal.fire({
          title: '¿Estás seguro?',
          text: "¡No podrás revertir esto!",
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

    // Acceso denegado desde sesión
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
  });
</script>

<jsp:include page="/includes/alerta.jsp" />
</body>
</html>
