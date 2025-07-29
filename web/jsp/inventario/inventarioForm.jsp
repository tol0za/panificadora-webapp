<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <title>Nuevo Movimiento de Inventario</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
</head>
<body>
<div class="container py-4">
    <h3>Registrar Movimiento de Inventario</h3>
    <form action="${ctx}/InventarioServlet" method="post" class="needs-validation" novalidate>
        <div class="mb-3">
            <label for="idEmpaque" class="form-label">Empaque</label>
            <select id="idEmpaque" name="idEmpaque" class="form-select" required>
                <option value="" disabled selected>Selecciona un empaque</option>
                <c:forEach var="empaque" items="${empaques}">
                    <option value="${empaque.idEmpaque}">${empaque.nombreEmpaque}</option>
                </c:forEach>
            </select>
            <div class="invalid-feedback">Selecciona un empaque.</div>
        </div>

        <div class="mb-3">
            <label for="cantidad" class="form-label">Cantidad (usar negativo para disminuir)</label>
            <input type="number" id="cantidad" name="cantidad" class="form-control" required />
            <div class="invalid-feedback">Ingresa una cantidad válida.</div>
        </div>

        <div class="mb-3">
            <label for="motivo" class="form-label">Motivo</label>
            <input type="text" id="motivo" name="motivo" class="form-control" required />
            <div class="invalid-feedback">Indica el motivo del movimiento.</div>
        </div>

        <div class="mb-3">
            <label for="idRepartidor" class="form-label">ID Repartidor (opcional)</label>
            <input type="number" id="idRepartidor" name="idRepartidor" class="form-control" />
        </div>

        <button type="submit" class="btn btn-primary">Registrar</button>
        <a href="${ctx}/InventarioServlet" class="btn btn-secondary">Cancelar</a>
    </form>
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
