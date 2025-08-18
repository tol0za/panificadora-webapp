<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- Carga la lista al entrar (solo si aún no viene del servlet) -->
<c:if test="${empty requestScope.salidas}">
  <jsp:include page="/DistribucionServlet">
    <jsp:param name="accion" value="listar"/>
    <jsp:param name="fecha"  value="${param.fecha}"/>
  </jsp:include>
</c:if>

<!-- ▬▬ CSS ▬▬ -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet"/>

<body data-ctx="${pageContext.request.contextPath}" class="p-3">

<h4><i class="bi bi-truck text-primary"></i>
  Salidas de Distribución – <span id="fechaHoy"></span>
</h4>

<button class="btn btn-primary mb-3" data-bs-toggle="modal" data-bs-target="#mdlNuevaSalida">
  <i class="bi bi-plus-circle"></i> Nueva salida
</button>

<div class="table-responsive shadow-sm rounded">
  <table class="table table-striped align-middle mb-0">
    <thead class="table-light">
      <tr>
        <th>#</th><th>Repartidor</th><th>Empaque</th><th>Cantidad</th><th>Fecha</th>
        <th class="text-center">Acciones</th>
      </tr>
    </thead>
    <tbody id="tblSalidas">
      <c:forEach items="${salidas}" var="s" varStatus="i">
        <tr>
          <td>${i.index + 1}</td>
          <td>${s.nombreRepartidorCompleto}</td>
          <td>${s.nombreEmpaque}</td>
          <td>
            <input type="number" min="1"
                   class="form-control form-control-sm w-75 d-inline editarCantidad"
                   data-id="${s.idDistribucion}" value="${s.cantidad}">
          </td>
          <td>${s.fechaFmt}</td>
          <td class="text-center">
            <button class="btn btn-sm btn-info verDetalle"
                    data-rep="${s.idRepartidor}"
                    data-fecha="${s.fechaDistribucion}">
              <i class="bi bi-eye"></i>
            </button>
            <button class="btn btn-sm btn-danger eliminarSalida"
                    data-rep="${s.idRepartidor}"
                    data-fecha="${s.fechaDistribucion}">
              <i class="bi bi-trash"></i>
            </button>
          </td>
        </tr>
      </c:forEach>

      <!-- filas extra para altura -->
      <c:forEach begin="${fn:length(salidas)+1}" end="10">
        <tr><td colspan="6">&nbsp;</td></tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<!-- ▬▬ MODAL DETALLE ▬▬ -->
<div class="modal fade" id="mdlDetalle" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header"><h5 class="modal-title">Detalle de salida</h5></div>
      <div class="modal-body">
        <table class="table table-sm mb-0">
          <thead><tr><th>Empaque</th><th class="text-end">Cantidad</th></tr></thead>
          <tbody id="tblDetalle"></tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<!-- ▬▬ MODAL NUEVA SALIDA ▬▬ -->
<div class="modal fade" id="mdlNuevaSalida" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <form id="frmNuevaSalida" class="modal-content"
          action="${pageContext.request.contextPath}/DistribucionServlet" method="post">
      <input type="hidden" name="accion" value="crear">
      <div class="modal-header">
        <h5 class="modal-title"><i class="bi bi-truck"></i> Registrar salida</h5>
      </div>
      <div class="modal-body">
        <!-- Repartidor -->
        <div class="mb-3">
          <label class="form-label"><i class="bi bi-person-badge"></i> Repartidor</label>
          <select name="idRepartidor" class="form-select" required>
            <option value="" hidden>-- Seleccione --</option>
            <c:forEach items="${sessionScope.listaRepartidores}" var="r">
              <option value="${r.idRepartidor}">
                ${r.nombreRepartidor} ${r.apellidoRepartidor}
              </option>
            </c:forEach>
          </select>
        </div>
        <hr class="my-3"/>
        <!-- líneas -->
        <table class="table" id="tblLineas">
          <thead>
            <tr><th>Empaque</th><th>Stock disp.</th><th style="width:120px">Cantidad</th><th></th></tr>
          </thead>
          <tbody></tbody>
        </table>
        <button type="button" class="btn btn-outline-secondary btn-sm" id="btnAgregarLinea">
          <i class="bi bi-plus-circle"></i> Agregar línea
        </button>
      </div>
      <div class="modal-footer">
        <button type="button" id="btnGuardar" class="btn btn-success">
          <i class="bi bi-save"></i> Guardar
        </button>
      </div>
    </form>
  </div>
</div>

<!-- ▬▬ SCRIPTS ▬▬ -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="<c:url value='/static/js/distribucion.js?v=7'/>"></script>
<script>
  document.getElementById('fechaHoy').textContent =
      new Date().toLocaleDateString('es-MX',{day:'2-digit',month:'2-digit',year:'numeric'});
</script>

<!-- ▬▬ MODAL DE NOTIFICACIÓN (SOLO PARA SALIDAS) ▬▬ -->
<c:if test="${not empty sessionScope.flashSalida}">
  <div class="modal fade" id="mdlSalidaMsg" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-sm">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="bi bi-check-circle text-success"></i> Listo
          </h5>
          <button class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
          <c:out value="${sessionScope.flashSalida}" />
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" data-bs-dismiss="modal">Aceptar</button>
        </div>
      </div>
    </div>
  </div>
  <script>
    document.addEventListener('DOMContentLoaded', function(){
      var m = new bootstrap.Modal(document.getElementById('mdlSalidaMsg'));
      m.show();
    });
  </script>
  <c:remove var="flashSalida" scope="session"/>
</c:if>

</body>
