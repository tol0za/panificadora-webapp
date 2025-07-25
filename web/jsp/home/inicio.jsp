<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Sistema Panificadora</title>
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css"/>
  <link rel="stylesheet" href="<c:url value='/css/global.css'/>"/>
</head>
<body class="d-flex vh-100">

  <jsp:include page="/includes/menu.jsp"/>
  <div class="flex-grow-1 d-flex flex-column">
    <jsp:include page="/includes/header.jsp"/>
    <div class="flex-grow-1 overflow-auto">
      <iframe name="contentFrame" src="${pageContext.request.contextPath}/jsp/bienvenida.jsp" frameborder="0"
        class="w-100 h-100"></iframe>

    </div>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
