<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="es_MX"/>

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Historial de Notas</title>
  <link  href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link  href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

  <style>
    /* ====== Estética general ====== */
    body{ background:#fbfbfb; }
    .card{ border:1px solid rgba(0,0,0,.07); box-shadow:0 2px 10px rgba(0,0,0,.03); }
    .kpi{
      border-radius:14px; padding:14px 16px;
      background:linear-gradient(180deg,#fff, #f9f7f3);
      border:1px solid rgba(0,0,0,.06);
      box-shadow:0 2px 10px rgba(0,0,0,.03);
    }
    .kpi .label{ color:#6c757d; font-size:.85rem; }
    .kpi .value{ font-size:1.25rem; font-weight:700; }
    .kpi .sub{ color:#8b8b8b; font-size:.8rem; }

    .table thead th{ position:sticky; top:0; background:#f6f6f6; z-index:1; }
    .table-sm td, .table-sm th{ padding:.45rem .6rem; }

    .toolbar .btn{ border-radius:10px; }
    .toolbar .btn-check:checked + .btn-outline-secondary{ 
      background:#eef2e6; border-color:#6e8b3d; color:#2f3a18;
    }

    /* Colores para charts */
    :root{
      --olive: #6e8b3d;
      --olive-300: #8faa5c;
      --caramel: #d4a373;
      --grid: rgba(0,0,0,.08);
    }
  </style>
</head>
<body class="p-3" data-ctx="${pageContext.request.contextPath}">
<div class="container-fluid">

  <h4 class="mb-3">Historial de notas por fechas</h4>

  <!-- ====== Toolbar de fechas ====== -->
  <form class="row gy-2 gx-3 align-items-end mb-3 toolbar" method="get" action="${pageContext.request.contextPath}/NotaVentaServlet">
    <input type="hidden" name="accion" value="histBuscar">
    <input type="hidden" name="inFrame" value="1">
    <div class="col-auto">
      <label class="form-label mb-1">Desde</label>
      <input type="date" name="desde" class="form-control" value="${desde}">
    </div>
    <div class="col-auto">
      <label class="form-label mb-1">Hasta</label>
      <input type="date" name="hasta" class="form-control" value="${hasta}">
    </div>
    <div class="col-auto">
      <button class="btn btn-primary"><i class="bi bi-search"></i> Buscar</button>
    </div>
    <div class="col-auto ms-auto">
      <div class="btn-group" role="group" aria-label="rango rápido">
        <input type="radio" class="btn-check" name="quick" id="qHoy" autocomplete="off">
        <label class="btn btn-sm btn-outline-secondary" for="qHoy">Hoy</label>

        <input type="radio" class="btn-check" name="quick" id="q7" autocomplete="off">
        <label class="btn btn-sm btn-outline-secondary" for="q7">7 días</label>

        <input type="radio" class="btn-check" name="quick" id="q30" autocomplete="off">
        <label class="btn btn-sm btn-outline-secondary" for="q30">30 días</label>
      </div>
    </div>
  </form>

  <!-- ====== KPIs ====== -->
  <c:set var="k_total" value="0" />
  <c:set var="k_notas" value="0" />
  <c:forEach items="${resumenDiario}" var="d">
    <c:set var="k_total" value="${k_total + d.totalDia}" />
    <c:set var="k_notas" value="${k_notas + d.notas}" />
  </c:forEach>
  <c:set var="k_dias" value="${fn:length(resumenDiario)}" />
  <c:set var="k_reps" value="${fn:length(resumenReps)}" />
  <div class="row g-3 mb-3">
    <div class="col-sm-6 col-lg-3">
      <div class="kpi">
        <div class="label">Total ventas</div>
        <div class="value"><fmt:formatNumber value="${k_total}" type="currency"/></div>
        <div class="sub">Rango: ${desde} a ${hasta}</div>
      </div>
    </div>
    <div class="col-sm-6 col-lg-3">
      <div class="kpi">
        <div class="label">Notas emitidas</div>
        <div class="value"><fmt:formatNumber value="${k_notas}" type="number"/></div>
        <div class="sub">Promedio/día: 
          <fmt:formatNumber value="${k_dias>0 ? (k_notas / k_dias) : 0}" maxFractionDigits="1"/>
        </div>
      </div>
    </div>
    <div class="col-sm-6 col-lg-3">
      <div class="kpi">
        <div class="label">Ticket promedio por día</div>
        <div class="value">
          <fmt:formatNumber value="${k_dias>0 ? (k_total / k_dias) : 0}" type="currency"/>
        </div>
        <div class="sub">Días con ventas: ${k_dias}</div>
      </div>
    </div>
    <div class="col-sm-6 col-lg-3">
      <div class="kpi">
        <div class="label">Repartidores activos</div>
        <div class="value"><fmt:formatNumber value="${k_reps}" type="number"/></div>
        <div class="sub">En el rango consultado</div>
      </div>
    </div>
  </div>

  <!-- ====== GRÁFICAS ====== -->
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

  <!-- ====== TABLAS ====== -->
  <div class="row g-3 mb-3">
    <div class="col-md-6">
      <div class="card card-body">
        <h6 class="mb-2">Resumen por día</h6>
        <div class="table-responsive" style="max-height: 340px;">
          <table class="table table-sm mb-0">
            <thead class="table-light"><tr><th>Fecha</th><th class="text-end">Notas</th><th class="text-end">Total</th></tr></thead>
            <tbody>
              <c:forEach items="${resumenDiario}" var="d">
                <tr>
                  <td>${d.fecha}</td>
                  <td class="text-end"><fmt:formatNumber value="${d.notas}" type="number"/></td>
                  <td class="text-end"><fmt:formatNumber value="${d.totalDia}" type="currency"/></td>
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
        <div class="table-responsive" style="max-height: 340px;">
          <table class="table table-sm mb-0">
            <thead class="table-light"><tr><th>Repartidor</th><th class="text-end">Notas</th><th class="text-end">Total</th></tr></thead>
            <tbody>
              <c:forEach items="${resumenReps}" var="r">
                <tr>
                  <td>${r.repartidor}</td>
                  <td class="text-end"><fmt:formatNumber value="${r.notas}" type="number"/></td>
                  <td class="text-end"><fmt:formatNumber value="${r.total}" type="currency"/></td>
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

  <!-- ====== Tabla detalle + Export ====== -->
  <div class="card card-body">
    <div class="d-flex justify-content-between align-items-center">
      <h6 class="mb-2">Notas encontradas</h6>
      <button id="btnCsv" class="btn btn-outline-secondary btn-sm">
        <i class="bi bi-filetype-csv"></i> Exportar CSV
      </button>
    </div>
    <div class="table-responsive" style="max-height: 420px;">
      <table id="tblNotas" class="table table-striped table-sm">
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
              <td class="text-end"><fmt:formatNumber value="${n.total}" type="currency"/></td>
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
  // Rango rápido (en LOCAL, sin toISOString)
  (function(){
    const qHoy = document.getElementById('qHoy');
    const q7   = document.getElementById('q7');
    const q30  = document.getElementById('q30');

    const form = document.querySelector('form.toolbar');
    const inpD = form.querySelector('input[name="desde"]');
    const inpH = form.querySelector('input[name="hasta"]');

    // YYYY-MM-DD en horario local
    function toLocalYMD(d){
      const y = d.getFullYear();
      const m = String(d.getMonth()+1).padStart(2,'0');
      const day = String(d.getDate()).padStart(2,'0');
      return y + '-' + m + '-' + day;
    }

    function setRange(days){
      // “End” = hoy local (sin horas)
      const end = new Date();
      end.setHours(0,0,0,0);

      // “Start” = end - (days-1)
      const start = new Date(end);
      start.setDate(end.getDate() - (days - 1));

      inpH.value = toLocalYMD(end);
      inpD.value = toLocalYMD(start);
      form.submit();
    }

    qHoy?.addEventListener('click', ()=> setRange(1));
    q7  ?.addEventListener('click', ()=> setRange(7));
    q30 ?.addEventListener('click', ()=> setRange(30));
  })();
</script>


<script>
(function() {
  const fmtMoney = new Intl.NumberFormat('es-MX',{style:'currency',currency:'MXN',maximumFractionDigits:2});
  const fmtInt   = new Intl.NumberFormat('es-MX',{maximumFractionDigits:0});

  // Datos desde JSTL → JS
  const labD = [
    <c:forEach var="d" items="${resumenDiario}">'${d.fecha}',</c:forEach>
  ];
  const valD = [
    <c:forEach var="d" items="${resumenDiario}">${d.totalDia},</c:forEach>
  ];
  const labR = [
    <c:forEach var="r" items="${resumenReps}">'${r.repartidor}',</c:forEach>
  ];
  const valRT = [
    <c:forEach var="r" items="${resumenReps}">${r.total},</c:forEach>
  ];
  const valRN = [
    <c:forEach var="r" items="${resumenReps}">${r.notas},</c:forEach>
  ];

  // Línea: Ventas por día (con degradado)
  const cD = document.getElementById('chartDiario');
  if (labD.length && valD.length && cD) {
    const ctx = cD.getContext('2d');
    const grad = ctx.createLinearGradient(0, 0, 0, cD.height);
    grad.addColorStop(0, 'rgba(110,139,61,0.30)');   // olive 30%
    grad.addColorStop(1, 'rgba(110,139,61,0.02)');   // fade
    new Chart(ctx, {
      type: 'line',
      data: {
        labels: labD,
        datasets: [{
          label: 'Total (MXN)',
          data: valD,
          borderColor: getComputedStyle(document.documentElement).getPropertyValue('--olive').trim() || '#6e8b3d',
          backgroundColor: grad,
          pointRadius: 3,
          pointHoverRadius: 5,
          borderWidth: 2,
          tension: .25,
          fill: true
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { display: false },
          tooltip: { callbacks: { label: (c)=> ' ' + fmtMoney.format(c.parsed.y) } }
        },
        scales: {
          x: { grid: { color:'var(--grid)' } },
          y: { beginAtZero:true, grid:{ color:'var(--grid)' },
               ticks:{ callback:(v)=> fmtMoney.format(v) } }
        }
      }
    });
  } else { document.getElementById('msgDiario')?.classList.remove('d-none'); }

  // Barras + Línea: Ventas por repartidor
  const cR = document.getElementById('chartReps');
  if (labR.length && valRT.length && cR) {
    new Chart(cR.getContext('2d'), {
      data: {
        labels: labR,
        datasets: [
          {
            type:'bar', label:'Total (MXN)', data: valRT,
            backgroundColor: 'rgba(212,163,115,0.55)', // caramel
            borderColor:    'rgba(212,163,115,1)',
            borderWidth: 1, yAxisID:'yMoney'
          },
          {
            type:'line', label:'# Notas', data: valRN,
            borderColor: 'rgba(110,139,61,1)',
            pointBackgroundColor: 'rgba(110,139,61,1)',
            borderWidth: 2, tension:.25, yAxisID:'yNotes'
          }
        ]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { display:true },
          tooltip: {
            callbacks: {
              label: (c)=> c.dataset.yAxisID==='yMoney'
                  ? (' ' + fmtMoney.format(c.parsed.y))
                  : (' ' + fmtInt.format(c.parsed.y) + ' nota(s)')
            }
          }
        },
        scales: {
          yMoney: { position:'left',  beginAtZero:true,
                    grid:{ color:'var(--grid)' },
                    ticks:{ callback:(v)=> fmtMoney.format(v) } },
          yNotes: { position:'right', beginAtZero:true,
                    grid:{ drawOnChartArea:false },
                    ticks:{ callback:(v)=> fmtInt.format(v) } },
          x: { ticks:{ autoSkip:false, maxRotation:60, minRotation:0 }, grid:{ color:'var(--grid)' } }
        }
      }
    });
  } else { document.getElementById('msgReps')?.classList.remove('d-none'); }

  // Exportar CSV (seguro sin EL en JS)
  document.getElementById('btnCsv')?.addEventListener('click', function () {
    var filas = Array.prototype.slice.call(
      document.querySelectorAll('#tblNotas tbody tr')
    ).map(function (tr) {
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
