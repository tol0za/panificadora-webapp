<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <title>Inventario</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
</head>
<body>
<div class="container py-4">

    <h3 class="mb-4">Registro Rápido de Movimientos de Inventario</h3>
    <form action="${ctx}/InventarioServlet" method="post" class="row g-3 mb-4 needs-validation" novalidate>
        <div class="col-md-5">
            <select name="idEmpaque" class="form-select" required>
                <option value="" disabled selected>Selecciona un empaque</option>
                <c:forEach var="empaque" items="${empaques}">
                    <option value="${empaque.idEmpaque}">${empaque.nombreEmpaque}</option>
                </c:forEach>
            </select>
            <div class="invalid-feedback">Selecciona un empaque.</div>
        </div>
        <div class="col-md-2">
            <input type="number" name="cantidad" min="1" class="form-control" placeholder="Cantidad" required />
            <div class="invalid-feedback">Ingresa una cantidad válida.</div>
        </div>
        <div class="col-md-3">
            <select name="motivo" class="form-select" required>
                <option value="Ingreso de Mercancía">Ingreso de Mercancía</option>
                <option value="Salida de Mercancía">Salida de Mercancía</option>
                <option value="Merma de Mercancía">Merma de Mercancía</option>
            </select>
            <div class="invalid-feedback">Selecciona un motivo.</div>
        </div>
        <div class="col-md-2 d-grid">
            <button type="submit" class="btn btn-primary">Registrar</button>
        </div>
    </form>

    <c:if test="${not empty sessionScope.mensaje}">
        <div class="alert alert-info alert-dismissible fade show" role="alert">
            ${sessionScope.mensaje}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
        </div>
        <c:set var="sessionScope.mensaje" value="" />
    </c:if>

    <h3>Movimientos de Inventario</h3>
    <table class="table table-striped table-hover">
        <thead class="table-light">
            <tr>
                <th>ID</th>
                <th>Empaque</th>
                <th>Cantidad</th>
                <th>Fecha</th>
                <th>Motivo</th>
                <th>Cantidad Actual</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="mov" items="${movimientos}">
                <tr>
                    <td>${mov.idInventario}</td>
                    <td>${mov.idEmpaque}</td>
                    <td>${mov.cantidad}</td>
                    <td><fmt:formatDate value="${mov.fechaDate}" pattern="dd/MM/yyyy HH:mm:ss"/></td>
                    <td>${mov.motivo}</td>
                    <td>${mov.cantidadActual}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<script>
    (() => {
        'use strict'
        const forms = document.querySelectorAll('.needs-validation')
        Array.from(forms).forEach(form => {
            form.addEventListener('submit', event => {
                if (!form.checkValidity()) {
                    event.preventDefault()
                    event.stopPropagation()
                }
                form.classList.add('was-validated')
            }, false)
        })
    })()
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
