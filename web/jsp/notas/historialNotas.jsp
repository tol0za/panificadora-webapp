<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Historial de Notas</title>
  <link  href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link  href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body class="p-3" data-ctx="${pageContext.request.contextPath}">
<div class="container-fluid">

  <h4 class="mb-3">Historial de notas por fechas</h4>

  <form class="row g-2 mb-3" method="get" action="${pageContext.request.contextPath}/NotaVentaServlet">
    <input type="hidden" name="accion" value="histBuscar">
    <input type="hidden" name="inFrame" value="1">
    <div class="col-auto">
      <label class="form-label">Desde</label>
      <input type="date" name="desde" class="form-control" value="${desde}">
    </div>
    <div class="col-auto">
      <label class="form-label">Hasta</label>
      <input type="date" name="hasta" class="form-control" value="${hasta}">
    </div>
    <div class="col-auto align-self-end">
      <button class="btn btn-primary"><i class="bi bi-search"></i> Buscar</button>
    </div>
  </form>

  <!-- GRÁFICAS -->
  <div class="row g-3 mb-3">
    <div class="col-lg-6">
      <div class="card card-body">
        <h6 class="mb-2">Ventas por día</h6>
        <canvas id="chartDiario" height="120"></canvas>
        <div id="msgDiario" class="text-muted small d-none">Sin datos en el rango.</div>
      </div>
    </div>
    <div class="col-lg-6">
      <div class="card card-body">
        <h6 class="mb-2">Ventas por repartidor</h6>
        <canvas id="chartReps" height="120"></canvas>
        <div id="msgReps" class="text-muted small d-none">Sin datos en el rango.</div>
      </div>
    </div>
  </div>

  <!-- TABLAS -->
  <div class="row g-3 mb-3">
    <div class="col-md-6">
      <div class="card card-body">
        <h6 class="mb-2">Resumen por día</h6>
        <div class="table-responsive">
          <table class="table table-sm mb-0">
            <thead class="table-light"><tr><th>Fecha</th><th class="text-end">Notas</th><th class="text-end">Total</th></tr></thead>
            <tbody>
              <c:forEach items="${resumenDiario}" var="d">
                <tr>
                  <td>${d.fecha}</td>
                  <td class="text-end">${d.notas}</td>
                  <td class="text-end">${d.totalDia}</td>
                </tr>
              </c:forEach>
              <c:if test="${empty resumenDiario}">
                <tr><td colspan="3" class="text-center text-muted">Sin datos en el rango.</td></tr>
              </c:if>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="col-md-6">
      <div class="card card-body">
        <h6 class="mb-2">Resumen por repartidor</h6>
        <div class="table-responsive">
          <table class="table table-sm mb-0">
            <thead class="table-light"><tr><th>Repartidor</th><th class="text-end">Notas</th><th class="text-end">Total</th></tr></thead>
            <tbody>
              <c:forEach items="${resumenReps}" var="r">
                <tr>
                  <td>${r.repartidor}</td>
                  <td class="text-end">${r.notas}</td>
                  <td class="text-end">${r.total}</td>
                </tr>
              </c:forEach>
              <c:if test="${empty resumenReps}">
                <tr><td colspan="3" class="text-center text-muted">Sin datos en el rango.</td></tr>
              </c:if>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <div class="card card-body">
    <div class="d-flex justify-content-between align-items-center">
      <h6 class="mb-2">Notas encontradas</h6>
      <button id="btnCsv" class="btn btn-outline-secondary btn-sm">Exportar CSV</button>
    </div>
    <div class="table-responsive">
      <table id="tblNotas" class="table table-striped">
        <thead class="table-light">
          <tr>
            <th>Fecha</th><th>Folio</th><th>Repartidor</th><th>Tienda</th><th class="text-end">Total</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach items="${listaNotas}" var="n">
            <tr>
              <td>${n.fechaHora}</td>
              <td>${n.folio}</td>
              <td>${n.repartidor}</td>
              <td>${n.tienda}</td>
              <td class="text-end">${n.total}</td>
            </tr>
          </c:forEach>
          <c:if test="${empty listaNotas}">
            <tr><td colspan="5" class="text-center text-muted">Sin notas en el rango.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </div>

</div>

<script>
(function() {
  const fmtMoney = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN', maximumFractionDigits: 2 });
  const fmtInt   = new Intl.NumberFormat('es-MX', { maximumFractionDigits: 0 });

  // Diario
  const labD = [
    <c:forEach var="d" items="${resumenDiario}">
      '${d.fecha}',
    </c:forEach>
  ];
  const valD = [
    <c:forEach var="d" items="${resumenDiario}">
      ${d.totalDia},
    </c:forEach>
  ];

  // Repartidores
  const labR = [
    <c:forEach var="r" items="${resumenReps}">
      '${r.repartidor}',
    </c:forEach>
  ];
  const valRT = [
    <c:forEach var="r" items="${resumenReps}">
      ${r.total},
    </c:forEach>
  ];
  const valRN = [
    <c:forEach var="r" items="${resumenReps}">
      ${r.notas},
    </c:forEach>
  ];

  // Línea: ventas por día
  const ctxD = document.getElementById('chartDiario');
  if (labD.length && valD.length && ctxD) {
    new Chart(ctxD, {
      type: 'line',
      data: { labels: labD, datasets: [{ label: 'Total', data: valD, borderWidth: 2, tension: .25, fill: false }] },
      options: {
        responsive: true,
        plugins: {
          legend: { display: true },
          tooltip: { callbacks: { label: (c)=> ' ' + fmtMoney.format(c.parsed.y) } }
        },
        scales: { y: { beginAtZero: true, ticks: { callback: (v)=> fmtMoney.format(v) } } }
      }
    });
  } else { document.getElementById('msgDiario')?.classList.remove('d-none'); }

  // Barras/Línea: ventas por repartidor
  const ctxR = document.getElementById('chartReps');
  if (labR.length && valRT.length && ctxR) {
    new Chart(ctxR, {
      data: {
        labels: labR,
        datasets: [
          { type:'bar',  label:'Total',  data: valRT, borderWidth:1, yAxisID:'yMoney' },
          { type:'line', label:'# Notas', data: valRN, borderWidth:2, tension:.25, yAxisID:'yNotes' }
        ]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { display:true },
          tooltip: {
            callbacks: {
              label: (c)=> c.dataset.yAxisID==='yMoney'
                ? (' '+fmtMoney.format(c.parsed.y))
                : (' '+fmtInt.format(c.parsed.y)+' nota(s)')
            }
          }
        },
        scales: {
          yMoney: { position:'left',  beginAtZero:true, ticks:{ callback:(v)=>fmtMoney.format(v) } },
          yNotes: { position:'right', beginAtZero:true, grid:{ drawOnChartArea:false }, ticks:{ callback:(v)=>fmtInt.format(v) } },
          x: { ticks:{ autoSkip:false, maxRotation:60, minRotation:0 } }
        }
      }
    });
  } else { document.getElementById('msgReps')?.classList.remove('d-none'); }

  // Exportar CSV rápido de la tabla inferior
document.getElementById('btnCsv')?.addEventListener('click', function () {
  // Toma cada fila del cuerpo de la tabla
  var filas = Array.prototype.slice.call(
    document.querySelectorAll('#tblNotas tbody tr')
  ).map(function (tr) {
    // Toma cada celda y la envuelve entre comillas escapando comillas internas
    var celdas = Array.prototype.slice.call(tr.children).map(function (td) {
      var txt = (td.textContent || '').trim().replace(/"/g, '""');
      return '"' + txt + '"';
    });
    return celdas.join(',');
  });
    var encabezado = 'Fecha,Folio,Repartidor,Tienda,Total';
  var csv = encabezado + '\n' + filas.join('\n');

  var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  var url  = URL.createObjectURL(blob);
  var a    = document.createElement('a');
  a.href = url;
  a.download = 'notas_' + (new Date().toISOString().slice(0,10)) + '.csv';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
});
})();
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
