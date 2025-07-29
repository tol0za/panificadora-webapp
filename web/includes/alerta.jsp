<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${not empty sessionScope.mensaje}">
  <c:set var="icono" value="info" />
  <c:set var="titulo" value="Acción realizada" />

  <c:choose>
    <c:when test="${sessionScope.mensaje eq 'registrado'}">
      <c:set var="icono" value="success" />
      <c:set var="titulo" value="¡Datos ingresados Exitosamente!" />
    </c:when>
    <c:when test="${sessionScope.mensaje eq 'actualizado'}">
      <c:set var="icono" value="info" />
      <c:set var="titulo" value="¡Datos actualizados!" />
    </c:when>
    <c:when test="${sessionScope.mensaje eq 'eliminado'}">
      <c:set var="icono" value="error" />
      <c:set var="titulo" value="¡Registro eliminado!" />
    </c:when>
  </c:choose>

  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
  <script>
    Swal.fire({
      icon: "${icono}",
      title: "${titulo}",
      showConfirmButton: false,
      timer: 1800
    });
  </script>
  <c:remove var="mensaje" scope="session"/>
</c:if>
