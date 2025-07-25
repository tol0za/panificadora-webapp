<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
  // Prepara la fecha actual
  pageContext.setAttribute("fechaActual", new java.util.Date());
%>
<header class="main-header d-flex align-items-center"
        style="width:100%; padding:1.1rem 2.2rem; background:#EDE0D4; border-bottom:2px solid #e6d5ad; box-shadow:0 3px 14px rgba(150,98,9,.06); border-radius: 0 0 20px 20px;">
  <!-- Título y crédito -->
  <div>
    <strong style="font-family:'Poppins',sans-serif; font-size:1.12rem; color:#493910;">
      Sistema Panificadora Del Valle
    </strong>
    <div style="font-family:'Poppins',sans-serif; font-size:0.95rem; color:#937944;">
      Creado por: Ing Luis Ángel Toloza Hernández
    </div>
  </div>

  <!-- Usuario, fecha y logout -->
  <div class="d-flex align-items-center ms-auto">
    <div class="text-end me-3" style="font-family:'Poppins',sans-serif; font-size:0.92rem; color:#5a4a36;">
      <div>
       <strong>${sessionScope.usuario.nombreUsuario} ${sessionScope.usuario.apellidoUsuario}</strong>

      </div>
      <div>
        <fmt:formatDate value="${fechaActual}" pattern="dd 'de' MMMM 'del' yyyy"/>
      </div>
    </div>
    <a href="<c:url value='${pageContext.request.contextPath}/LogoutServlet'/>" class="btn btn-outline-danger btn-sm">
      <i class="bi bi-box-arrow-right"></i>
    </a>
  </div>
</header>
