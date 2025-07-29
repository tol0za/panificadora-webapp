<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Acceso Denegado</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light d-flex align-items-center justify-content-center" style="height: 100vh;">
  <div class="text-center">
    <h1 class="text-danger">403</h1>
    <h3 class="mb-3">Acceso Restringido</h3>
    <p>No tienes permisos para acceder a este recurso.</p>
    <a href="${pageContext.request.contextPath}/jsp/bienvenida.jsp" class="btn btn-primary">Volver al inicio</a>
  </div>
</body>
</html>
