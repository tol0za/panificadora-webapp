<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Inicio - Panificadora Del Valle</title>
  <link 
    href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
    rel="stylesheet"/>
  <link rel="stylesheet" href="${ctx}/css/global.css"/>
  <style>
    body {
      background: #fafafa;
    }
    /* Elevamos el bloque un poco hacia arriba */
    .welcome-container {
      transform: translateY(-25px);
    }
    /* Logo más grande */
    .welcome-logo {
      height: 600px;
      margin-bottom: 1rem;
    }
  </style>
</head>
<body class="d-flex vh-100 justify-content-center align-items-center">

  <div class="text-center welcome-container">
    <!-- Logo agrandado -->
    <img src="${ctx}/img/logo.png"
        draggable="false"
         alt="Logo Panificadora Del Valle"
         class="welcome-logo"/>

    <!-- Título -->
    <h1 class="h3 mb-2" style="font-family:'Poppins',sans-serif;">
      Sistema Panificadora Del Valle
    </h1>

    <!-- Usuario -->
    <c:if test="${not empty sessionScope.usuario}">
      <p class="text-secondary mb-0">
        Sesión como 
         <strong>${sessionScope.usuario.nombreUsuario} ${sessionScope.usuario.apellidoUsuario}</strong>
      </p>
    </c:if>
  </div>

  <script 
    src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js">
  </script>
</body>
</html>
