<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
  String rol = (String) session.getAttribute("rol");
  if (rol == null || !rol.equals("administrador")) {
%>
  <%-- Inyección de alerta si no es admin --%>
  <c:set var="ctx" value="${pageContext.request.contextPath}" />
  <!DOCTYPE html>
  <html lang="es">
  <head>
    <meta charset="UTF-8">
    <title>Acceso Restringido</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
  </head>
  <body>
    <script>
      Swal.fire({
        icon: 'error',
        title: 'Acceso denegado',
        text: 'No tienes permisos para acceder a este formulario.',
        timer: 1500,
        showConfirmButton: false
      });
    </script>
  </body>
  </html>
  <%
    return;
  }
  boolean esNuevo = (request.getAttribute("pan") == null);
%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title><%= esNuevo ? "Nuevo Pan" : "Editar Pan" %></title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-4">
  <h3 class="text-secondary mb-3"><%= esNuevo ? "Registrar Nuevo Pan" : "Editar Pan" %></h3>

  <form action="${ctx}/CatalogoPanServlet" method="post">
    <input type="hidden" name="idPan" value="${pan.idPan}"/>

    <table class="table bg-white shadow-sm rounded">
      <tr>
        <td style="width:200px;"><label for="nombrePan" class="form-label">Nombre del Pan</label></td>
        <td><input type="text" class="form-control" name="nombrePan" id="nombrePan" value="${pan.nombrePan}" required></td>
      </tr>
    </table>

    <button type="submit" class="btn btn-primary">
      <i class="bi bi-save"></i> Guardar
    </button>
    <a href="${ctx}/CatalogoPanServlet?accion=listar" class="btn btn-secondary">Cancelar</a>
  </form>
</div>
</body>
</html>
