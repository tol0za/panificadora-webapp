<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Login - Panificadora Del Valle</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
</head>
<body class="login-body">
    <div class="card login-card">
        <div class="text-center">
            <img src="${pageContext.request.contextPath}/img/logo.png" alt="Logo" class="logo">
            <h4 class="mb-3">Panificadora Del Valle</h4>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/LoginServlet">
            <div class="mb-3">
                <label class="form-label">Usuario</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-person-fill"></i></span>
                    <input type="text" name="usuario" class="form-control" required autofocus>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label">Contraseña</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                    <input type="password" name="password" class="form-control" id="inputPassword" required>
                    <button class="btn btn-outline-secondary" type="button" id="togglePassword" tabindex="-1">
                        <i class="bi bi-eye" id="iconEye"></i>
                    </button>
                </div>
            </div>
            <div class="d-grid">
                <button class="btn btn-primary">Ingresar</button>
            </div>
            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger mt-3 text-center">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>
        </form>
    </div>

    <script>
        // Script para mostrar/ocultar contraseña
        document.addEventListener("DOMContentLoaded", function() {
            const togglePassword = document.getElementById('togglePassword');
            const passwordInput = document.getElementById('inputPassword');
            const iconEye = document.getElementById('iconEye');

            togglePassword.addEventListener('click', function () {
                if (passwordInput.type === 'password') {
                    passwordInput.type = 'text';
                    iconEye.classList.remove('bi-eye');
                    iconEye.classList.add('bi-eye-slash');
                } else {
                    passwordInput.type = 'password';
                    iconEye.classList.remove('bi-eye-slash');
                    iconEye.classList.add('bi-eye');
                }
            });
        });
    </script>
</body>
</html>
