<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<nav class="d-flex flex-column p-3" style="width:250px;">
  <div class="text-center mb-4">
    <img src="<c:url value='/img/logo.png' />"
         draggable="false"
         alt="Logo Panificadora"
         style="max-width:150px;"/>
  </div>
  <ul class="nav nav-pills flex-column" id="mainMenu">
    <li class="nav-item">
      <a class="nav-link active"
         href="${pageContext.request.contextPath}/jsp/bienvenida.jsp"
         draggable="false"
         target="contentFrame">
        <i class="bi bi-house"></i> Inicio
      </a>
    </li>
    <li class="nav-item">
      <a class="nav-link"
         href="catalogoPan"
         draggable="false"
         target="contentFrame">
        <i class="bi bi-bag"></i> Catálogo de Pan
      </a>
    </li>
    <li class="nav-item">
      <a class="nav-link"
         href="catalogoEmpaque"
         draggable="false"
         target="contentFrame">
        <i class="bi bi-box-seam"></i> Catálogo de Empaque
      </a>
    </li>
    <li class="nav-item">
      <a class="nav-link"
         href="repartidores"
         draggable="false"
         target="contentFrame">
        <i class="bi bi-truck"></i> Repartidores
      </a>
    </li>
    <!-- Nueva entrada: Inventario Empacado -->
    <li class="nav-item">
      <a class="nav-link"
         href="inventarioEmpaquetado"
         draggable="false"
         target="contentFrame">
        <i class="bi bi-archive"></i> Inventario Empacado
      </a>
    </li>
    <li class="nav-item">
      <a class="nav-link"
         href="distribuciones"
         draggable="false"
         target="contentFrame">
        <i class="bi bi-receipt"></i> Salidas Diarias
      </a>
    </li>
        
    <li class="nav-item">
      <a class="nav-link"
         href="NotasVentaServlet"
         draggable="false"
         target="contentFrame">
        <i class="bi bi-journal-check"></i> Notas de Venta
      </a>
    </li>
    
    <li class="nav-item">
      <a class="nav-link"
         href="${pageContext.request.contextPath}/Salidas"
         draggable="false"
         target="contentFrame">
        <i class="bi bi-basket-fill"></i> Salidas
      </a>
    </li>
    <!-- más opciones aquí -->
  </ul>
</nav>

<script>
  // Mantener el 'active' en la opción clicada
  document.querySelectorAll('#mainMenu .nav-link').forEach(link => {
    link.addEventListener('click', function() {
      document.querySelectorAll('#mainMenu .nav-link.active')
              .forEach(active => active.classList.remove('active'));
      this.classList.add('active');
    });
  });
</script>
