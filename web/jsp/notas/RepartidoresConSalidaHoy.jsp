<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>Repartidores del día</title>
  <link  href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body data-ctx="${pageContext.request.contextPath}">
<div class="container my-4">
  <h4 class="mb-3">Repartidores con salida – ${hoy}</h4>

  <c:choose>
    <c:when test="${empty listaRepartidores}">
      <div class="alert alert-info">Ningún repartidor reportó salida hoy.</div>
    </c:when>
    <c:otherwise>
      <div class="table-responsive">
        <table class="table table-hover align-middle" id="tblReps">
          <thead class="table-light">
            <tr><th>Repartidor</th></tr>
          </thead>
          <tbody>

            <c:forEach items="${listaRepartidores}" var="r">
              <%-- 1) Nombre base (seguro) --%>
              <c:set var="nom" value="${r.nombreRepartidor}" />

              <%-- 2) Apellido (opcional): probamos varios nombres de propiedad sin romper --%>
              <c:set var="ap" value=""/>
              <c:catch var="e1"><c:set var="ap" value="${r.apellidoRepartidor}"/></c:catch>
             

              <tr role="button" title="Seleccionar"
                  onclick="location.href='${pageContext.request.contextPath}/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=${r.idRepartidor}'">
                <td>
                  <c:choose>
                    <c:when test="${not empty ap}">
                      <c:out value="${nom}"/> <c:out value="${ap}"/>
                    </c:when>
                    <c:otherwise>
                      <c:out value="${nom}"/>
                    </c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>

          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
