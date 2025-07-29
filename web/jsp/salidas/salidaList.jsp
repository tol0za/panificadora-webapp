<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <title>Listado de Salidas</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet"/>
</head>
<body>
<div class="container py-4">
    <div class="d-flex justify-content-between mb-3">
        <h3 class="text-secondary">Listado de Salidas</h3>
        <a href="${ctx}/SalidaServlet?accion=nuevo" class="btn btn-success">
            <i class="bi bi-plus-circle"></i> Nueva Salida
        </a>
    </div>

    <input type="text" id="busqueda" class="form-control mb-3" placeholder="Buscar...">

    <table class="table table-bordered table-hover">
        <thead class="table-light">
        <tr>
            <th>ID Distribución</th>
            <th>ID Repartidor</th>
            <th>ID Empaque</th>
            <th>Cantidad</th>
            <th>Fecha Distribución</th>
        </tr>
        </thead>
        <tbody id="tablaSalidas">
        <c:forEach var="salida" items="${salidas}">
            <tr>
                <td>${salida.idDistribucion}</td>
                <td>${salida.idRepartidor}</td>
                <td>${salida.idEmpaque}</td>
                <td>${salida.cantidad}</td>
                <td><fmt:formatDate value="${salida.fechaDistribucionDate}" pattern="dd/MM/yyyy HH:mm:ss"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<script>
    document.getElementById('busqueda').addEventListener('input', function () {
        let filtro = this.value.toLowerCase();
        document.querySelectorAll("#tablaSalidas tr").forEach(function (fila) {
            fila.style.display = fila.textContent.toLowerCase().includes(filtro) ? '' : 'none';
        });
    });
</script>
</body>
</html>
