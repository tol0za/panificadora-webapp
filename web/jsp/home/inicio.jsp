<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!-- Determinamos la URL inicial del iframe -->
<c:set var="iframeSrc">
  <c:choose>
    <c:when test="${not empty param.vista}">
      ${ctx}/jsp/${param.vista}
    </c:when>
    <c:otherwise>
      ${ctx}/jsp/bienvenida.jsp
    </c:otherwise>
  </c:choose>
</c:set>

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Sistema Panificadora</title>

  <!-- CSS -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet"/>
  <link rel="stylesheet" href="${ctx}/css/global.css"/>
</head>

<body class="d-flex vh-100">

  <!-- Menú lateral -->
  <jsp:include page="/includes/menu.jsp"/>

  <!-- Área principal -->
  <div class="flex-grow-1 d-flex flex-column">
    <jsp:include page="/includes/header.jsp"/>

    <div class="flex-grow-1 overflow-auto">
      <iframe name="contentFrame"
              src="${iframeSrc}"
              frameborder="0"
              class="w-100 h-100"></iframe>
    </div>
  </div>

  <!-- JS -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</body>
</html>
