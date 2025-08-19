<%@ taglib uri="http://java.sun.com/jsp/jstl/core"       prefix="c"   %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"        prefix="fmt" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<fmt:setLocale value="es_MX"/>

<!DOCTYPE html>
<html>
<head>
  <title>Repartidores del día</title>
  <link  href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <style>
    /* Tarjeta tipo KPI (igual estilo que historial) */
    .kpi-card {
      display:block; text-decoration:none; color:inherit;
      border-radius:14px; padding:16px 18px;
      background:linear-gradient(180deg,#ffffff,#f9f7f3);
      border:1px solid rgba(0,0,0,.06);
      box-shadow:0 2px 10px rgba(0,0,0,.03);
      transition:transform .12s ease, box-shadow .12s ease, background .2s ease;
      height:100%;
    }
    .kpi-card:hover { transform: translateY(-2px); box-shadow:0 8px 18px rgba(0,0,0,.08); background:linear-gradient(180deg,#ffffff,#f3efe7); }
    .kpi-card:focus-visible { outline:none; box-shadow:0 0 0 .2rem rgba(110,139,61,.25); }
    .kpi-title { color:#6c757d; font-size:.9rem; margin-bottom:.25rem; }
    .kpi-value { font-weight:700; font-size:1.2rem; }
    .kpi-sub   { color:#8b8b8b; font-size:.85rem; margin-top:.35rem; }
    .kpi-icon {
      width:38px; height:38px; border-radius:10px; background:#eef2e6; color:#2f3a18;
      display:flex; align-items:center; justify-content:center; font-weight:700; margin-right:.65rem; flex:0 0 auto;
    }
    .badge-soft { background:#eef2e6; color:#2f3a18; border:1px solid rgba(0,0,0,.06); }
    .kpi-row { row-gap:16px; }
  </style>
</head>

<body data-ctx="${pageContext.request.contextPath}">
<div class="container my-4">
  <h4 class="mb-3">Repartidores con salida – ${hoy}</h4>

  <c:choose>
    <c:when test="${empty listaRepartidores}">
      <div class="alert alert-info">Ningún repartidor reportó salida hoy.</div>
    </c:when>
    <c:otherwise>

      <!-- GRID de tarjetas -->
      <div class="row kpi-row">
        <c:forEach items="${listaRepartidores}" var="r">

          <!-- Enlace a la vista del repartidor -->
          <c:url var="urlVista" value="/NotaVentaServlet">
            <c:param name="inFrame" value="1"/>
            <c:param name="accion"  value="vistaRepartidor"/>
            <c:param name="id"      value="${r.idRepartidor}"/>
          </c:url>

          <!-- Toma métricas si vienen del servlet -->
          <c:set var="id" value="${r.idRepartidor}"/>
          <c:set var="cntNotas" value="${empty notasPorRep ? 0 : (empty notasPorRep[id] ? 0 : notasPorRep[id])}"/>
          <c:set var="totHoy"   value="${empty totalPorRep ? 0 : (empty totalPorRep[id] ? 0 : totalPorRep[id])}"/>

          <div class="col-12 col-sm-6 col-lg-4 col-xl-3">
            <a href="${urlVista}" class="kpi-card" aria-label="Ver notas de ${r.nombreRepartidor}">
              <div class="d-flex align-items-center mb-1">
                <div class="kpi-icon">
                  <span><c:out value="${fn:substring(r.nombreRepartidor,0,1)}"/></span>
                </div>
                <div class="kpi-title mb-0">Repartidor</div>
              </div>

              <div class="kpi-value"><c:out value="${r.nombreRepartidor}"/></div>

              <div class="d-flex gap-2 mt-2">
                <span class="badge badge-soft">
                  <i class="bi bi-receipt"></i>
                  &nbsp;<c:out value="${cntNotas}"/> nota(s)
                </span>
                <span class="badge badge-soft">
                  <i class="bi bi-cash-coin"></i>
                  &nbsp;<fmt:formatNumber value="${totHoy}" type="currency"/>
                </span>
              </div>

              <div class="kpi-sub">Clic para ver y capturar notas del día</div>
            </a>
          </div>
        </c:forEach>
      </div>

    </c:otherwise>
  </c:choose>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
