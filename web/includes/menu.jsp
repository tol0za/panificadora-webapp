<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    String rol = (String) session.getAttribute("rol");
    pageContext.setAttribute("rol", rol);
%>

<nav class="d-flex flex-column p-3" style="width:250px;">
  <div class="text-center mb-4">
    <img src="<c:url value='/img/logo.png' />"
         draggable="false"
         alt="Logo Panificadora"
         style="max-width:150px;"/>
  </div>
  
  <ul class="nav nav-pills flex-column" id="mainMenu">
    
    <!-- Inicio (visible para todos) -->
    <li class="nav-item">
      <a class="nav-link active"
         href="${pageContext.request.contextPath}/jsp/bienvenida.jsp"
         draggable="false"
         target="contentFrame">
        <i class="bi bi-house"></i> Inicio
      </a>
    </li>

    <!-- ADMINISTRADOR: Opciones de gestión -->
    <c:if test="${rol == 'administrador'}">
      <li class="nav-item">
        <a class="nav-link"
           href="${pageContext.request.contextPath}/RepartidorServlet?accion=listar"
           draggable="false"
           target="contentFrame">
          <i class="bi bi-truck"></i> Repartidores
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link"
           href="${pageContext.request.contextPath}/CatalogoPanServlet?accion=listar"
           draggable="false"
           target="contentFrame">
          <i class="bi bi-bag-plus"></i> Catálogo de Pan
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link"
           href="${pageContext.request.contextPath}/CatalogoEmpaqueServlet?accion=listar"
           draggable="false"
           target="contentFrame">
          <i class="bi bi-box"></i> Catálogo de Empaque
        </a>
      </li>
    </c:if>

    <!-- Aquí puedes agregar más secciones si aplican -->

  </ul>
</nav>

<script>
  // Resalta el ítem activo
  document.querySelectorAll('#mainMenu .nav-link').forEach(link => {
    link.addEventListener('click', function() {
      document.querySelectorAll('#mainMenu .nav-link.active')
              .forEach(active => active.classList.remove('active'));
      this.classList.add('active');
    });
  });
</script>
