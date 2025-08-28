<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet"/>

<style>
  .kpi-card{
    display:block;border-radius:14px;padding:16px 18px;text-decoration:none;color:inherit;
    background:linear-gradient(180deg,#fff,#f9f7f3); border:1px solid rgba(0,0,0,.06);
    box-shadow:0 2px 10px rgba(0,0,0,.03); transition:.15s; height:100%;
  }
  .kpi-card:hover{ transform:translateY(-2px); box-shadow:0 8px 18px rgba(0,0,0,.08); }
  .kpi-title{ color:#6c757d; font-size:.9rem; }
  .kpi-value{ font-weight:700; font-size:1.2rem; }
  .badge-soft{ background:#eef2e6; color:#2f3a18; border:1px solid rgba(0,0,0,.06); }
</style>

<div class="container py-3" data-ctx="${pageContext.request.contextPath}">
  <h4 class="mb-3"><i class="bi bi-truck"></i> Salidas de hoy – ${hoy}</h4>

  <c:choose>
    <c:when test="${empty repartidoresConSalida}">
      <div class="alert alert-info">No hay salidas hoy.</div>
    </c:when>
    <c:otherwise>
      <div class="row g-3">
        <c:forEach items="${repartidoresConSalida}" var="r">
          <div class="col-12 col-sm-6 col-lg-4 col-xl-3">
            <a href="${pageContext.request.contextPath}/DistribucionServlet?accion=listar&fecha=${r.fecha}"
               class="kpi-card">
              <div class="kpi-title">Repartidor</div>
              <div class="kpi-value">${r.nombreRepartidor}</div>
              <div class="mt-2 d-flex gap-2">
                <span class="badge badge-soft"><i class="bi bi-box2"></i> ${r.totalEmpaques} empaques</span>
                <span class="badge badge-soft"><i class="bi bi-123"></i> ${r.totalPiezas} piezas</span>
              </div>
              <div class="small text-muted mt-2">Clic para ver detalles</div>
            </a>
          </div>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>
</div>
