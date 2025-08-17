<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    java.time.LocalDate hoy = java.time.LocalDate.now();
    int mesActual = request.getParameter("mes") != null
        ? Integer.parseInt(request.getParameter("mes"))
        : hoy.getMonthValue();
    int anioActual = request.getParameter("anio") != null
        ? Integer.parseInt(request.getParameter("anio"))
        : hoy.getYear();

    int prevMonth = (mesActual == 1) ? 12 : mesActual - 1;
    int prevYear  = (mesActual == 1) ? anioActual - 1 : anioActual;
    int nextMonth = (mesActual == 12) ? 1 : mesActual + 1;
    int nextYear  = (mesActual == 12) ? anioActual + 1 : anioActual;

    java.time.LocalDate primerDiaMes = java.time.LocalDate.of(anioActual, mesActual, 1);
    int diasMes = primerDiaMes.lengthOfMonth();
    int primerDiaSemana = primerDiaMes.getDayOfWeek().getValue();
    if (primerDiaSemana == 7) primerDiaSemana = 0;

    java.util.HashSet<String> diasConSalida = new java.util.HashSet<>();
    java.util.List salidas = (java.util.List) request.getAttribute("salidas");
    if (salidas != null) {
        for (Object o : salidas) {
            modelo.Salida s = (modelo.Salida) o;
            java.time.LocalDate f = s.getFechaDistribucionDate()
                .toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            diasConSalida.add(f.toString());
        }
    }
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Calendario de Salidas</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css"/>
  <style>
    .calendar { display: grid; grid-template-columns: repeat(7,1fr); gap: .25rem; }
    .day { position: relative; border: 1px solid #ddd; border-radius: 8px; min-height: 75px;
           padding: .5rem; background: #fff; cursor: pointer; }
    .day.today { border: 2px solid #E65100; }
    .dot { position: absolute; bottom: 8px; left: 50%; transform: translateX(-50%);
           font-size: 1.2rem; color: #E65100; }
    .day-header { font-weight: 600; text-align: center; }
  </style>
</head>
<body>
  <div class="container py-4" style="max-width:800px;">

    <!-- Navegación de meses -->
    <div class="d-flex justify-content-between align-items-center mb-2">
      <a href="${ctx}/SalidaServlet?accion=calendario&mes=<%=prevMonth%>&anio=<%=prevYear%>"
         class="btn btn-outline-primary btn-sm">
        <i class="bi bi-chevron-left"></i> Mes anterior
      </a>
      <h4 class="mb-0">
        <%= primerDiaMes.getMonth()
              .getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es","ES"))
        %> <%= anioActual %>
      </h4>
      <a href="${ctx}/SalidaServlet?accion=calendario&mes=<%=nextMonth%>&anio=<%=nextYear%>"
         class="btn btn-outline-primary btn-sm">
        Mes siguiente <i class="bi bi-chevron-right"></i>
      </a>
    </div>

    <!-- Cabecera días -->
    <div class="calendar mb-2">
      <div class="day-header">Dom</div><div class="day-header">Lun</div>
      <div class="day-header">Mar</div><div class="day-header">Mié</div>
      <div class="day-header">Jue</div><div class="day-header">Vie</div>
      <div class="day-header">Sáb</div>
    </div>

    <!-- Días del mes -->
    <div class="calendar mb-4">
      <% for (int i = 0; i < primerDiaSemana; i++) { %>
        <div></div>
      <% }
         for (int dia = 1; dia <= diasMes; dia++) {
           java.time.LocalDate fechaActual = java.time.LocalDate.of(anioActual, mesActual, dia);
           String iso = fechaActual.toString();
           boolean esHoy = fechaActual.equals(hoy);
           boolean tiene = diasConSalida.contains(iso);
      %>
        <div class="day <%= esHoy ? "today" : "" %>"
             onclick="window.location='${ctx}/SalidaServlet?accion=verDia&fecha=<%=iso%>'">
          <div><%= dia %></div>
          <% if (tiene) { %>
            <i class="bi bi-calendar-check-fill dot text-success"></i>
          <% } %>
        </div>
      <% } %>
    </div>

    <!-- Botones Ver listado / Nueva salida (inferior) -->
    <div class="d-flex justify-content-end">
      <a href="${pageContext.request.contextPath}/SalidaServlet?accion=listar" class="btn btn-secondary btn-sm me-2">
        <i class="bi bi-list"></i> Ver listado
      </a>
      <a href="${ctx}/SalidaServlet?accion=nuevo" class="btn btn-success btn-sm">
        <i class="bi bi-plus-circle"></i> Nueva salida
      </a>
    </div>

  </div>
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
