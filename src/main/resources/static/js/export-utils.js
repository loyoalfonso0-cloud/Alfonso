/**
 * Utilidades para exportación de datos a CSV y Excel
 * Sistema Tributario Empresarial
 */

// Función para exportar tabla a CSV
function exportToCSV(tableId, filename) {
    const table = document.getElementById(tableId);
    if (!table) {
        showToast('Tabla no encontrada', 'error');
        return;
    }

    let csv = [];
    const rows = table.querySelectorAll('tr');
    
    for (let i = 0; i < rows.length; i++) {
        const row = rows[i];
        const cols = row.querySelectorAll('td, th');
        let csvRow = [];
        
        for (let j = 0; j < cols.length - 1; j++) { // Excluir columna de acciones
            let cellText = cols[j].textContent.trim();
            // Limpiar texto de badges y elementos innecesarios
            cellText = cellText.replace(/\s+/g, ' ');
            csvRow.push('"' + cellText + '"');
        }
        
        if (csvRow.length > 0) {
            csv.push(csvRow.join(','));
        }
    }
    
    downloadCSV(csv.join('\n'), filename);
}

// Función para descargar CSV
function downloadCSV(csvContent, filename) {
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    
    if (link.download !== undefined) {
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
}

// Función para exportar datos específicos a Excel (usando SheetJS)
function exportToExcel(data, filename, sheetName = 'Datos') {
    // Esta función requiere la librería SheetJS (xlsx.js)
    // Para implementación completa, incluir: <script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js"></script>
    
    if (typeof XLSX === 'undefined') {
        // Fallback a CSV si no está disponible XLSX
        console.warn('XLSX no disponible, exportando como CSV');
        exportArrayToCSV(data, filename);
        return;
    }
    
    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, sheetName);
    
    XLSX.writeFile(wb, filename);
}

// Función para exportar array de objetos a CSV
function exportArrayToCSV(data, filename) {
    if (!data || data.length === 0) {
        showToast('No hay datos para exportar', 'warning');
        return;
    }
    
    const headers = Object.keys(data[0]);
    let csv = headers.map(h => `"${h}"`).join(',') + '\n';
    
    data.forEach(row => {
        const values = headers.map(header => {
            const value = row[header] || '';
            return `"${String(value).replace(/"/g, '""')}"`;
        });
        csv += values.join(',') + '\n';
    });
    
    downloadCSV(csv, filename);
}

// Funciones específicas para cada módulo

// Exportar declaraciones
function exportarDeclaraciones() {
    const fecha = new Date().toISOString().split('T')[0];
    const filename = `declaraciones_${fecha}.csv`;
    
    // Si hay datos dinámicos, usar fetch para obtenerlos
    fetch('/tributario/declaraciones/export-data')
        .then(response => response.json())
        .then(data => {
            exportArrayToCSV(data, filename);
            showToast('Declaraciones exportadas exitosamente', 'success');
        })
        .catch(error => {
            // Fallback: exportar tabla visible
            exportToCSV('declaracionesTable', filename);
            showToast('Declaraciones exportadas desde tabla', 'success');
        });
}

// Exportar contribuyentes
function exportarContribuyentes() {
    const fecha = new Date().toISOString().split('T')[0];
    const filename = `contribuyentes_${fecha}.csv`;
    
    fetch('/tributario/contribuyentes/export-data')
        .then(response => response.json())
        .then(data => {
            exportArrayToCSV(data, filename);
            showToast('Contribuyentes exportados exitosamente', 'success');
        })
        .catch(error => {
            exportToCSV('contribuyentesTable', filename);
            showToast('Contribuyentes exportados desde tabla', 'success');
        });
}

// Exportar impuestos
function exportarImpuestos() {
    const fecha = new Date().toISOString().split('T')[0];
    const filename = `impuestos_${fecha}.csv`;
    
    fetch('/tributario/impuestos/export-data')
        .then(response => response.json())
        .then(data => {
            exportArrayToCSV(data, filename);
            showToast('Impuestos exportados exitosamente', 'success');
        })
        .catch(error => {
            exportToCSV('impuestosTable', filename);
            showToast('Impuestos exportados desde tabla', 'success');
        });
}

// Exportar retenciones
function exportarRetenciones() {
    const fecha = new Date().toISOString().split('T')[0];
    const filename = `retenciones_${fecha}.csv`;
    
    fetch('/tributario/retenciones/export-data')
        .then(response => response.json())
        .then(data => {
            exportArrayToCSV(data, filename);
            showToast('Retenciones exportadas exitosamente', 'success');
        })
        .catch(error => {
            exportToCSV('retencionesTable', filename);
            showToast('Retenciones exportadas desde tabla', 'success');
        });
}

// Exportar comprobantes
function exportarComprobantes() {
    const fecha = new Date().toISOString().split('T')[0];
    const filename = `comprobantes_${fecha}.csv`;
    
    fetch('/tributario/comprobantes/export-data')
        .then(response => response.json())
        .then(data => {
            exportArrayToCSV(data, filename);
            showToast('Comprobantes exportados exitosamente', 'success');
        })
        .catch(error => {
            exportToCSV('comprobantesTable', filename);
            showToast('Comprobantes exportados desde tabla', 'success');
        });
}

// Función para exportar con filtros aplicados
function exportarConFiltros(tableId, filename) {
    const table = document.getElementById(tableId);
    if (!table) return;
    
    const visibleRows = Array.from(table.querySelectorAll('tr')).filter(row => 
        row.style.display !== 'none'
    );
    
    let csv = [];
    
    visibleRows.forEach(row => {
        const cols = row.querySelectorAll('td, th');
        let csvRow = [];
        
        for (let j = 0; j < cols.length - 1; j++) {
            let cellText = cols[j].textContent.trim();
            cellText = cellText.replace(/\s+/g, ' ');
            csvRow.push('"' + cellText + '"');
        }
        
        if (csvRow.length > 0) {
            csv.push(csvRow.join(','));
        }
    });
    
    downloadCSV(csv.join('\n'), filename);
    showToast('Datos filtrados exportados exitosamente', 'success');
}

// Función para generar reporte completo
function generarReporteCompleto() {
    const modal = `
        <form id="reporteForm">
            <div class="form-group">
                <label class="form-label">Tipo de Reporte *</label>
                <select class="form-control" name="tipoReporte" required>
                    <option value="">Seleccionar tipo...</option>
                    <option value="declaraciones">Declaraciones</option>
                    <option value="contribuyentes">Contribuyentes</option>
                    <option value="impuestos">Impuestos</option>
                    <option value="retenciones">Retenciones</option>
                    <option value="comprobantes">Comprobantes</option>
                    <option value="completo">Reporte Completo</option>
                </select>
            </div>
            
            <div class="grid grid-cols-2" style="gap:16px">
                <div class="form-group">
                    <label class="form-label">Fecha Desde</label>
                    <input type="date" class="form-control" name="fechaDesde">
                </div>
                <div class="form-group">
                    <label class="form-label">Fecha Hasta</label>
                    <input type="date" class="form-control" name="fechaHasta">
                </div>
            </div>
            
            <div class="form-group">
                <label class="form-label">Formato *</label>
                <select class="form-control" name="formato" required>
                    <option value="csv">CSV</option>
                    <option value="excel">Excel</option>
                    <option value="pdf">PDF</option>
                </select>
            </div>
            
            <div class="form-group">
                <label style="display:flex;align-items:center;gap:8px;cursor:pointer">
                    <input type="checkbox" name="incluirFiltros" checked>
                    <span>Aplicar filtros actuales</span>
                </label>
            </div>
        </form>
    `;
    
    openModal('📊 Generar Reporte', modal, function() {
        const form = document.getElementById('reporteForm');
        const formData = new FormData(form);
        
        const tipoReporte = formData.get('tipoReporte');
        const formato = formData.get('formato');
        const fechaDesde = formData.get('fechaDesde');
        const fechaHasta = formData.get('fechaHasta');
        
        // Construir URL con parámetros
        let url = `/tributario/reportes/${tipoReporte}?formato=${formato}`;
        if (fechaDesde) url += `&fechaDesde=${fechaDesde}`;
        if (fechaHasta) url += `&fechaHasta=${fechaHasta}`;
        
        window.open(url, '_blank');
        showToast('Generando reporte...', 'success');
    });
}

// Función auxiliar para mostrar toast (debe estar definida en cada página)
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;
    
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : '⚠️';
    toast.innerHTML = `
        <span style="font-size:16px">${icon}</span>
        <span>${message}</span>
    `;
    
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 4000);
}
