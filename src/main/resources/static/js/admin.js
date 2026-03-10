/**
 * admin.js - Script optimizado para el panel de administración
 */

document.addEventListener('DOMContentLoaded', function() {
    // Variables globales
    const charts = {};
    let dashboardData = {};

    // Inicializar componentes
    initNavigation();
    initAlerts();
    initFilters();
    initSearch();
    initViewButtons();
    initCharts();
    initEventListeners();

    /**
     * Inicializa la navegación entre secciones
     */
    function initNavigation() {
        const menuItems = document.querySelectorAll('.admin-menu-item');
        const sections = document.querySelectorAll('.admin-section');

        menuItems.forEach(item => {
            item.addEventListener('click', function(e) {
                e.preventDefault();

                // Remover clase active de todos los items y secciones
                menuItems.forEach(i => i.classList.remove('active'));
                sections.forEach(s => s.classList.remove('active'));

                // Agregar clase active al item clickeado
                this.classList.add('active');

                // Mostrar la sección correspondiente
                const sectionId = this.getAttribute('data-section');
                document.getElementById(sectionId).classList.add('active');

                // Si es dashboard, actualizar estadísticas
                if (sectionId === 'dashboard') {
                    actualizarDashboard();
                }
            });
        });

        // Verificar si hay un hash en la URL al cargar
        if (window.location.hash) {
            const hash = window.location.hash.substring(1);
            const targetMenuItem = document.querySelector(`.admin-menu-item[data-section="${hash}"]`);
            if (targetMenuItem) {
                targetMenuItem.click();
            }
        }
    }

    /**
     * Inicializa las alertas
     */
    function initAlerts() {
        const closeButtons = document.querySelectorAll('.close-alert');
        closeButtons.forEach(button => {
            button.addEventListener('click', function() {
                this.parentElement.style.display = 'none';
            });
        });

        // Auto-ocultar alertas después de 5 segundos
        document.querySelectorAll('.admin-alert').forEach(alert => {
            setTimeout(() => {
                alert.style.display = 'none';
            }, 5000);
        });
    }

    /**
     * Inicializa los filtros
     */
    function initFilters() {
        const filterBtn = document.getElementById('filterBtn');
        const filterMenu = document.getElementById('filterMenu');

        if (filterBtn && filterMenu) {
            filterBtn.addEventListener('click', function() {
                filterMenu.classList.toggle('active');
            });

            // Cerrar el menú al hacer clic fuera
            document.addEventListener('click', function(e) {
                if (!filterBtn.contains(e.target) && !filterMenu.contains(e.target)) {
                    filterMenu.classList.remove('active');
                }
            });

            // Aplicar filtros
            const filterOptions = filterMenu.querySelectorAll('input[type="checkbox"]');
            filterOptions.forEach(option => {
                option.addEventListener('change', function() {
                    aplicarFiltros();
                });
            });
        }
    }

    /**
     * Inicializa la búsqueda
     */
    function initSearch() {
        const searchInput = document.getElementById('searchCitas');
        if (searchInput) {
            let timeoutId;

            searchInput.addEventListener('input', function() {
                clearTimeout(timeoutId);

                const termino = this.value.trim();

                // Debounce para evitar muchas peticiones
                timeoutId = setTimeout(() => {
                    if (termino.length >= 2) {
                        buscarCitas(termino);
                    } else if (termino.length === 0) {
                        // Restaurar tabla original sin recargar la página
                        cargarTodasLasCitas();
                    }
                }, 300);
            });
        }
    }

    /**
     * Carga todas las citas sin recargar la página
     */
    function cargarTodasLasCitas() {
        mostrarCargando(true);

        fetch('/admin/citas/todas')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar las citas');
                }
                return response.json();
            })
            .then(citas => {
                actualizarTablaCitas(citas);
                mostrarCargando(false);
            })
            .catch(error => {
                console.error('Error al cargar todas las citas:', error);
                mostrarAlerta('Error al cargar las citas. Intente nuevamente.', 'error');
                mostrarCargando(false);
            });
    }

    /**
     * Muestra u oculta el indicador de carga
     */
    function mostrarCargando(mostrar) {
        const loadingIndicator = document.getElementById('loadingIndicator');
        if (!loadingIndicator) return;

        if (mostrar) {
            loadingIndicator.style.display = 'flex';
        } else {
            loadingIndicator.style.display = 'none';
        }
    }

    /**
     * Inicializa los botones de ver detalles
     */
    function initViewButtons() {
        const viewButtons = document.querySelectorAll('.view-btn');
        viewButtons.forEach(button => {
            button.addEventListener('click', function() {
                const citaId = this.getAttribute('data-id');
                mostrarDetalleCita(citaId);
            });
        });
    }

    /**
     * Inicializa los gráficos
     */
    function initCharts() {
        actualizarDashboard();
    }

    /**
     * Inicializa todos los event listeners adicionales
     */
    function initEventListeners() {
        // Botón para actualizar manualmente el dashboard
        const refreshDashboardBtn = document.getElementById('refreshDashboardBtn');
        if (refreshDashboardBtn) {
            refreshDashboardBtn.addEventListener('click', function() {
                actualizarDashboard();
                mostrarAlerta('Dashboard actualizado correctamente', 'success');
            });
        }

        // Botón para exportar historial de citas
        const exportarHistorialBtn = document.getElementById('exportarHistorialBtn');
        if (exportarHistorialBtn) {
            exportarHistorialBtn.addEventListener('click', function() {
                exportarHistorialCitas();
            });
        }
    }

    /**
     * Exporta el historial de citas a CSV
     */
    function exportarHistorialCitas() {
        mostrarCargando(true);

        fetch('/admin/citas/exportar')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al exportar las citas');
                }
                return response.blob();
            })
            .then(blob => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.style.display = 'none';
                a.href = url;
                a.download = 'historial-citas.csv';
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                mostrarCargando(false);
                mostrarAlerta('Historial de citas exportado correctamente', 'success');
            })
            .catch(error => {
                console.error('Error al exportar historial:', error);
                mostrarAlerta('Error al exportar historial de citas', 'error');
                mostrarCargando(false);
            });
    }

    /**
     * Actualiza el dashboard completo
     */
    function actualizarDashboard() {
        mostrarCargando(true);

        fetch('/admin/dashboard/datos')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al obtener datos del dashboard');
                }
                return response.json();
            })
            .then(data => {
                // Guardar datos para uso posterior
                dashboardData = data;

                // Actualizar contadores
                actualizarContadores(data);

                // Actualizar gráficos
                actualizarGraficoCitas(data.citasPorDia);
                actualizarGraficoServicios(data.serviciosPopulares);

                // Actualizar estadísticas adicionales
                actualizarEstadisticasAdicionales(data);

                mostrarCargando(false);
            })
            .catch(error => {
                console.error('Error al actualizar dashboard:', error);
                mostrarAlerta('Error al cargar datos del dashboard', 'error');
                mostrarCargando(false);
            });
    }

    /**
     * Actualiza los contadores del dashboard
     */
    function actualizarContadores(data) {
        // Actualizar contador de citas totales
        const totalCitasElement = document.querySelector('.stat-card:nth-child(1) .stat-value');
        if (totalCitasElement) {
            totalCitasElement.textContent = data.totalCitas || 0;
        }

        // Actualizar contador de citas programadas
        const citasProgramadasElement = document.querySelector('.stat-card:nth-child(2) .stat-value');
        if (citasProgramadasElement) {
            citasProgramadasElement.textContent = data.citasProgramadas || 0;
        }

        // Actualizar contador de citas completadas
        const citasCompletadasElement = document.querySelector('.stat-card:nth-child(3) .stat-value');
        if (citasCompletadasElement) {
            citasCompletadasElement.textContent = data.citasCompletadas || 0;
        }

        // Actualizar contador de citas canceladas
        const citasCanceladasElement = document.querySelector('.stat-card:nth-child(4) .stat-value');
        if (citasCanceladasElement) {
            citasCanceladasElement.textContent = data.citasCanceladas || 0;
        }
    }

    /**
     * Actualiza el gráfico de citas por día
     */
    function actualizarGraficoCitas(citasPorDia) {
        const citasCtx = document.getElementById('citasChart');
        if (!citasCtx) return;

        // Destruir gráfico existente si hay uno
        if (charts.citasChart) {
            charts.citasChart.destroy();
        }

        const fechas = Object.keys(citasPorDia).sort();
        const valores = fechas.map(fecha => citasPorDia[fecha]);

        // Formatear fechas para mostrar
        const fechasFormateadas = fechas.map(fecha => {
            const date = new Date(fecha);
            return date.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit' });
        });

        // Colores para el gráfico
        const colores = valores.map((valor, index) => {
            // Destacar el día con más citas
            const maxValor = Math.max(...valores);
            return valor === maxValor ? '#2980b9' : '#3498db';
        });

        charts.citasChart = new Chart(citasCtx, {
            type: 'bar',
            data: {
                labels: fechasFormateadas,
                datasets: [{
                    label: 'Citas',
                    data: valores,
                    backgroundColor: colores,
                    borderColor: colores,
                    borderWidth: 1,
                    borderRadius: 4,
                    barThickness: 25
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        titleFont: {
                            size: 14,
                            weight: 'bold'
                        },
                        bodyFont: {
                            size: 13
                        },
                        padding: 10,
                        callbacks: {
                            label: function(context) {
                                return `Citas: ${context.raw}`;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1,
                            precision: 0
                        },
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                },
                animation: {
                    duration: 1000,
                    easing: 'easeOutQuart'
                }
            }
        });
    }

    /**
     * Actualiza el gráfico de servicios populares
     */
    function actualizarGraficoServicios(serviciosPopulares) {
        const serviciosCtx = document.getElementById('serviciosChart');
        if (!serviciosCtx) return;

        // Destruir gráfico existente si hay uno
        if (charts.serviciosChart) {
            charts.serviciosChart.destroy();
        }

        const nombres = serviciosPopulares.map(s => s.nombre);
        const cantidades = serviciosPopulares.map(s => s.cantidad);
        const totalCitas = cantidades.reduce((a, b) => a + b, 0);

        // Colores para el gráfico
        const colores = [
            '#3498db',
            '#2ecc71',
            '#f39c12',
            '#9b59b6',
            '#e74c3c',
            '#1abc9c'
        ];

        charts.serviciosChart = new Chart(serviciosCtx, {
            type: 'doughnut',
            data: {
                labels: nombres,
                datasets: [{
                    data: cantidades,
                    backgroundColor: colores.slice(0, nombres.length),
                    borderColor: 'white',
                    borderWidth: 2,
                    hoverOffset: 15
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '60%',
                plugins: {
                    legend: {
                        position: 'right',
                        labels: {
                            font: {
                                size: 12
                            },
                            padding: 15,
                            usePointStyle: true,
                            pointStyle: 'circle'
                        }
                    },
                    tooltip: {
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        titleFont: {
                            size: 14,
                            weight: 'bold'
                        },
                        bodyFont: {
                            size: 13
                        },
                        padding: 10,
                        callbacks: {
                            label: function(context) {
                                const value = context.raw;
                                const percentage = ((value / totalCitas) * 100).toFixed(1);
                                return `${context.label}: ${value} citas (${percentage}%)`;
                            }
                        }
                    }
                },
                animation: {
                    animateRotate: true,
                    animateScale: true,
                    duration: 1000,
                    easing: 'easeOutQuart'
                }
            },
            plugins: [{
                id: 'centerText',
                beforeDraw: function(chart) {
                    const width = chart.width;
                    const height = chart.height;
                    const ctx = chart.ctx;

                    ctx.restore();
                    ctx.font = 'bold 20px Arial';
                    ctx.textBaseline = 'middle';
                    ctx.textAlign = 'center';

                    const text = totalCitas.toString();
                    const textX = width / 2;
                    const textY = height / 2 - 10;

                    ctx.fillStyle = '#3498db';
                    ctx.fillText(text, textX, textY);

                    ctx.font = '14px Arial';
                    ctx.fillStyle = '#767676';
                    ctx.fillText('Total de citas', textX, textY + 20);

                    ctx.save();
                }
            }]
        });
    }

    /**
     * Actualiza estadísticas adicionales en el dashboard
     */
    function actualizarEstadisticasAdicionales(data) {
        // Actualizar tasa de completadas
        const tasaCompletadasElement = document.getElementById('tasaCompletadas');
        if (tasaCompletadasElement && data.totalCitas > 0) {
            const tasaCompletadas = ((data.citasCompletadas / data.totalCitas) * 100).toFixed(1);
            tasaCompletadasElement.textContent = `${tasaCompletadas}%`;

            // Actualizar color según el valor
            if (tasaCompletadas > 70) {
                tasaCompletadasElement.className = 'stat-highlight positive';
            } else if (tasaCompletadas > 40) {
                tasaCompletadasElement.className = 'stat-highlight neutral';
            } else {
                tasaCompletadasElement.className = 'stat-highlight negative';
            }
        }

        // Actualizar tasa de canceladas
        const tasaCanceladasElement = document.getElementById('tasaCanceladas');
        if (tasaCanceladasElement && data.totalCitas > 0) {
            const tasaCanceladas = ((data.citasCanceladas / data.totalCitas) * 100).toFixed(1);
            tasaCanceladasElement.textContent = `${tasaCanceladas}%`;

            // Actualizar color según el valor (inverso a completadas)
            if (tasaCanceladas < 10) {
                tasaCanceladasElement.className = 'stat-highlight positive';
            } else if (tasaCanceladas < 30) {
                tasaCanceladasElement.className = 'stat-highlight neutral';
            } else {
                tasaCanceladasElement.className = 'stat-highlight negative';
            }
        }
    }

    /**
     * Busca citas por término
     */
    function buscarCitas(termino) {
        mostrarCargando(true);

        fetch(`/admin/citas/buscar?termino=${encodeURIComponent(termino)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en la búsqueda');
                }
                return response.json();
            })
            .then(citas => {
                actualizarTablaCitas(citas);
                mostrarCargando(false);

                // Actualizar contador de resultados
                const resultadosElement = document.getElementById('resultadosBusqueda');
                if (resultadosElement) {
                    resultadosElement.textContent = `${citas.length} resultados encontrados`;
                    resultadosElement.style.display = 'block';
                }
            })
            .catch(error => {
                console.error('Error al buscar citas:', error);
                mostrarAlerta('Error al buscar citas. Intente con otros términos.', 'error');
                mostrarCargando(false);
            });
    }

    /**
     * Aplica filtros a las citas
     */
    function aplicarFiltros() {
        mostrarCargando(true);

        // Obtener valores de los filtros
        const today = document.getElementById('today')?.checked || false;
        const week = document.getElementById('week')?.checked || false;
        const month = document.getElementById('month')?.checked || false;
        const completed = document.getElementById('completed')?.checked || false;
        const pending = document.getElementById('pending')?.checked || false;
        const canceled = document.getElementById('canceled')?.checked || false;

        // Construir URL con parámetros
        const params = new URLSearchParams();
        if (today) params.append('today', 'true');
        if (week) params.append('week', 'true');
        if (month) params.append('month', 'true');
        if (completed) params.append('estado', 'COMPLETADA');
        if (pending) params.append('estado', 'PROGRAMADA');
        if (canceled) params.append('estado', 'CANCELADA');

        fetch(`/admin/citas/filtrar?${params.toString()}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al aplicar filtros');
                }
                return response.json();
            })
            .then(citas => {
                actualizarTablaCitas(citas);
                mostrarCargando(false);

                // Mostrar filtros aplicados
                mostrarFiltrosAplicados(today, week, month, completed, pending, canceled);
            })
            .catch(error => {
                console.error('Error al filtrar citas:', error);
                mostrarAlerta('Error al aplicar filtros. Intente nuevamente.', 'error');
                mostrarCargando(false);
            });
    }

    /**
     * Muestra los filtros aplicados
     */
    function mostrarFiltrosAplicados(today, week, month, completed, pending, canceled) {
        const filtrosAplicadosElement = document.getElementById('filtrosAplicados');
        if (!filtrosAplicadosElement) return;

        let filtrosTexto = [];

        // Añadir filtros de tiempo
        if (today) filtrosTexto.push('Hoy');
        if (week) filtrosTexto.push('Esta semana');
        if (month) filtrosTexto.push('Este mes');

        // Añadir filtros de estado
        if (completed) filtrosTexto.push('Completadas');
        if (pending) filtrosTexto.push('Programadas');
        if (canceled) filtrosTexto.push('Canceladas');

        if (filtrosTexto.length > 0) {
            filtrosAplicadosElement.innerHTML = `
                <div class="filtros-aplicados">
                    <span>Filtros aplicados:</span>
                    ${filtrosTexto.map(f => `<span class="filtro-tag">${f}</span>`).join('')}
                    <button id="limpiarFiltros" class="btn-clear-filters">
                        <i class="ri-close-line"></i> Limpiar
                    </button>
                </div>
            `;
            filtrosAplicadosElement.style.display = 'block';

            // Añadir evento para limpiar filtros
            document.getElementById('limpiarFiltros').addEventListener('click', function() {
                // Desmarcar todos los checkboxes
                document.querySelectorAll('#filterMenu input[type="checkbox"]').forEach(cb => {
                    cb.checked = false;
                });

                // Cargar todas las citas
                cargarTodasLasCitas();

                // Ocultar filtros aplicados
                filtrosAplicadosElement.style.display = 'none';
            });
        } else {
            filtrosAplicadosElement.style.display = 'none';
        }
    }

    /**
     * Actualiza la tabla de citas con los datos recibidos
     */
    function actualizarTablaCitas(citas) {
        const tbody = document.getElementById('citasTableBody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (citas.length === 0) {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td colspan="10" class="no-data-message">
                    <div>
                        <i class="ri-calendar-line"></i>
                        <p>No hay citas que coincidan con los criterios</p>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
            return;
        }

        citas.forEach(cita => {
            const tr = document.createElement('tr');
            tr.id = `cita-${cita.id}`;
            tr.dataset.id = cita.id;

            const fecha = new Date(cita.fecha);
            const fechaFormateada = fecha.toLocaleDateString('es-ES', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric'
            });

            const hora = cita.hora ? cita.hora.substring(0, 5) : '00:00';

            const hoy = new Date();
            const esHoy = fecha.getDate() === hoy.getDate() &&
                          fecha.getMonth() === hoy.getMonth() &&
                          fecha.getFullYear() === hoy.getFullYear();

            tr.innerHTML = `
                <td>${cita.id.substring(0, 8)}...</td>
                <td>
                    <div class="user-info-cell">
                        <div class="user-avatar">
                            <i class="ri-user-line"></i>
                        </div>
                        <div class="user-details">
                            <span>${cita.nombre} ${cita.apellido}</span>
                        </div>
                    </div>
                </td>
                <td>${cita.cedula}</td>
                <td>
                    <div class="contact-info">
                        <div class="contact-item" title="${cita.telefono}">
                            <i class="ri-phone-line"></i>
                            <span>${cita.telefono}</span>
                        </div>
                        <div class="contact-item" title="${cita.correo}">
                            <i class="ri-mail-line"></i>
                            <span>${cita.correo}</span>
                        </div>
                    </div>
                </td>
                <td>
                    <div class="date-badge ${esHoy ? 'today' : ''}">
                        <span>${fechaFormateada}</span>
                    </div>
                </td>
                <td>
                    <div class="time-badge">
                        <i class="ri-time-line"></i>
                        <span>${hora}</span>
                    </div>
                </td>
                <td>
                    ${cita.doctorNombre ? `<span>Dr. ${cita.doctorNombre} ${cita.doctorApellido}</span>` : '<span class="no-data">No asignado</span>'}
                </td>
                <td>
                    ${cita.servicioNombre ? `<span>${cita.servicioNombre}</span>` : '<span class="no-data">No asignado</span>'}
                </td>
                <td>
                    <span class="status-badge ${(cita.estado || 'programada').toLowerCase()}">${cita.estado || 'PROGRAMADA'}</span>
                </td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn view-btn" data-id="${cita.id}" title="Ver detalles">
                            <i class="ri-eye-line"></i>
                        </button>
                        <button class="action-btn edit-btn" data-id="${cita.id}" title="Editar cita">
                            <i class="ri-edit-line"></i>
                        </button>
                        <button class="action-btn delete-btn" data-id="${cita.id}" title="Eliminar cita">
                            <i class="ri-delete-bin-line"></i>
                        </button>
                    </div>
                </td>
            `;

            tbody.appendChild(tr);
        });

        // Volver a agregar event listeners a los botones
        initTableButtons();
    }

    /**
     * Inicializa los botones de la tabla de citas
     */
    function initTableButtons() {
        // Botones de ver detalles
        document.querySelectorAll('.view-btn').forEach(button => {
            button.addEventListener('click', function() {
                const citaId = this.getAttribute('data-id');
                mostrarDetalleCita(citaId);
            });
        });

        // Botones de editar
        document.querySelectorAll('.edit-btn').forEach(button => {
            button.addEventListener('click', function() {
                const citaId = this.getAttribute('data-id');
                editarCita(citaId);
            });
        });

        // Botones de eliminar
        document.querySelectorAll('.delete-btn').forEach(button => {
            button.addEventListener('click', function() {
                const citaId = this.getAttribute('data-id');
                confirmarEliminarCita(citaId);
            });
        });
    }

    /**
     * Muestra el detalle de una cita
     */
    function mostrarDetalleCita(citaId) {
        mostrarCargando(true);

        fetch(`/admin/citas/${citaId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al obtener los detalles de la cita');
                }
                return response.json();
            })
            .then(cita => {
                mostrarCargando(false);

                // Crear modal para mostrar detalles
                const modal = document.createElement('div');
                modal.className = 'modal';

                const fecha = new Date(cita.fecha);
                const fechaFormateada = fecha.toLocaleDateString('es-ES', {
                    day: '2-digit',
                    month: '2-digit',
                    year: 'numeric'
                });

                modal.innerHTML = `
                    <div class="modal-content">
                        <div class="modal-header">
                            <h3>Detalles de la Cita</h3>
                            <button type="button" class="close-modal">
                                <i class="ri-close-line"></i>
                            </button>
                        </div>
                        <div class="modal-body">
                            <div class="cita-details-grid">
                                <div class="cita-details-column">
                                    <h4>Información del Paciente</h4>
                                    <p><strong>Nombre:</strong> ${cita.nombre} ${cita.apellido}</p>
                                    <p><strong>Cédula:</strong> ${cita.cedula}</p>
                                    <p><strong>Dirección:</strong> ${cita.direccion || 'No disponible'}</p>
                                    <p><strong>Teléfono:</strong> ${cita.telefono}</p>
                                    <p><strong>Correo:</strong> ${cita.correo}</p>
                                </div>
                                <div class="cita-details-column">
                                    <h4>Información de la Cita</h4>
                                    <p><strong>Fecha:</strong> ${fechaFormateada}</p>
                                    <p><strong>Hora:</strong> ${cita.hora}</p>
                                    <p><strong>Doctor:</strong> ${cita.doctorNombre ? 'Dr. ' + cita.doctorNombre + ' ' + cita.doctorApellido : 'No asignado'}</p>
                                    <p><strong>Servicio:</strong> ${cita.servicioNombre ? cita.servicioNombre : 'No asignado'}</p>
                                    <p><strong>Estado:</strong> <span class="status-badge ${cita.estado ? cita.estado.toLowerCase() : 'programada'}">${cita.estado ? cita.estado : 'PROGRAMADA'}</span></p>
                                </div>
                            </div>
                            <div class="cita-details-notes">
                                <h4>Notas</h4>
                                <p>${cita.notas || 'No hay notas disponibles para esta cita.'}</p>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn-secondary delete-cita" data-id="${cita.id}">
                                <i class="ri-delete-bin-line"></i> Eliminar Cita
                            </button>
                            <button type="button" class="btn-primary cambiar-estado" data-id="${cita.id}" data-estado="COMPLETADA">
                                <i class="ri-check-line"></i> Marcar como Completada
                            </button>
                            <button type="button" class="btn-secondary cambiar-estado" data-id="${cita.id}" data-estado="CANCELADA">
                                <i class="ri-close-line"></i> Cancelar Cita
                            </button>
                        </div>
                    </div>
                `;

                document.body.appendChild(modal);

                // Cerrar modal
                modal.querySelector('.close-modal').addEventListener('click', function() {
                    document.body.removeChild(modal);
                });

                // Eliminar cita
                modal.querySelector('.delete-cita').addEventListener('click', function() {
                    const id = this.getAttribute('data-id');
                    document.body.removeChild(modal);
                    confirmarEliminarCita(id);
                });

                // Cambiar estado de cita
                const botonesEstado = modal.querySelectorAll('.cambiar-estado');
                botonesEstado.forEach(boton => {
                    boton.addEventListener('click', function() {
                        const id = this.getAttribute('data-id');
                        const estado = this.getAttribute('data-estado');

                        cambiarEstadoCita(id, estado, modal);
                    });
                });
            })
            .catch(error => {
                console.error('Error al obtener detalles de la cita:', error);
                mostrarAlerta('Error al cargar los detalles de la cita', 'error');
                mostrarCargando(false);
            });
    }

    /**
     * Editar una cita
     */
    function editarCita(citaId) {
        mostrarCargando(true);

        fetch(`/admin/citas/${citaId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al obtener los datos de la cita');
                }
                return response.json();
            })
            .then(cita => {
                mostrarCargando(false);

                // Crear modal para editar
                const modal = document.createElement('div');
                modal.className = 'modal';

                modal.innerHTML = `
                    <div class="modal-content">
                        <div class="modal-header">
                            <h3>Editar Cita</h3>
                            <button type="button" class="close-modal">
                                <i class="ri-close-line"></i>
                            </button>
                        </div>
                        <div class="modal-body">
                            <form id="editarCitaForm" class="form-grid">
                                <input type="hidden" name="id" value="${cita.id}">

                                <div class="form-group">
                                    <label for="editNombre">Nombre</label>
                                    <input type="text" id="editNombre" name="nombre" value="${cita.nombre}" required>
                                </div>

                                <div class="form-group">
                                    <label for="editApellido">Apellido</label>
                                    <input type="text" id="editApellido" name="apellido" value="${cita.apellido}" required>
                                </div>

                                <div class="form-group">
                                    <label for="editCedula">Cédula</label>
                                    <input type="text" id="editCedula" name="cedula" value="${cita.cedula}" required>
                                </div>

                                <div class="form-group">
                                    <label for="editTelefono">Teléfono</label>
                                    <input type="text" id="editTelefono" name="telefono" value="${cita.telefono}" required>
                                </div>

                                <div class="form-group">
                                    <label for="editCorreo">Correo</label>
                                    <input type="email" id="editCorreo" name="correo" value="${cita.correo}" required>
                                </div>

                                <div class="form-group">
                                    <label for="editFecha">Fecha</label>
                                    <input type="date" id="editFecha" name="fecha" value="${cita.fecha}" required>
                                </div>

                                <div class="form-group">
                                    <label for="editHora">Hora</label>
                                    <input type="time" id="editHora" name="hora" value="${cita.hora}" required>
                                </div>

                                <div class="form-group">
                                    <label for="editDoctor">Doctor</label>
                                    <select id="editDoctor" name="doctorId">
                                        <option value="">Seleccionar doctor</option>
                                        <!-- Se cargará dinámicamente -->
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label for="editServicio">Servicio</label>
                                    <select id="editServicio" name="servicioId">
                                        <option value="">Seleccionar servicio</option>
                                        <!-- Se cargará dinámicamente -->
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label for="editEstado">Estado</label>
                                    <select id="editEstado" name="estado" required>
                                        <option value="PROGRAMADA" ${cita.estado === 'PROGRAMADA' ? 'selected' : ''}>Programada</option>
                                        <option value="COMPLETADA" ${cita.estado === 'COMPLETADA' ? 'selected' : ''}>Completada</option>
                                        <option value="CANCELADA" ${cita.estado === 'CANCELADA' ? 'selected' : ''}>Cancelada</option>
                                    </select>
                                </div>

                                <div class="form-group full-width">
                                    <label for="editNotas">Notas</label>
                                    <textarea id="editNotas" name="notas" rows="3">${cita.notas || ''}</textarea>
                                </div>
                            </form>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn-secondary" id="cancelarEdicion">
                                <i class="ri-close-line"></i> Cancelar
                            </button>
                            <button type="button" class="btn-primary" id="guardarEdicion">
                                <i class="ri-save-line"></i> Guardar Cambios
                            </button>
                        </div>
                    </div>
                `;

                document.body.appendChild(modal);

                // Cargar doctores y servicios
                cargarDoctoresYServicios(cita.doctorId, cita.servicioId);

                // Cerrar modal
                modal.querySelector('.close-modal').addEventListener('click', function() {
                    document.body.removeChild(modal);
                });

                // Cancelar edición
                document.getElementById('cancelarEdicion').addEventListener('click', function() {
                    document.body.removeChild(modal);
                });

                // Guardar cambios
                document.getElementById('guardarEdicion').addEventListener('click', function() {
                    guardarCambiosCita(modal);
                });
            })
            .catch(error => {
                console.error('Error al cargar datos para editar cita:', error);
                mostrarAlerta('Error al cargar datos para editar la cita', 'error');
                mostrarCargando(false);
            });
    }

    /**
     * Carga doctores y servicios para el formulario de edición
     */
    function cargarDoctoresYServicios(doctorIdSeleccionado, servicioIdSeleccionado) {
        // Cargar doctores
        fetch('/admin/doctores/lista')
            .then(response => response.json())
            .then(doctores => {
                const selectDoctor = document.getElementById('editDoctor');
                doctores.forEach(doctor => {
                    const option = document.createElement('option');
                    option.value = doctor.id;
                    option.textContent = `Dr. ${doctor.nombre} ${doctor.apellido} - ${doctor.especialidad}`;
                    option.selected = doctor.id === doctorIdSeleccionado;
                    selectDoctor.appendChild(option);
                });
            })
            .catch(error => {
                console.error('Error al cargar doctores:', error);
            });

        // Cargar servicios
        fetch('/admin/servicios/lista')
            .then(response => response.json())
            .then(servicios => {
                const selectServicio = document.getElementById('editServicio');
                servicios.forEach(servicio => {
                    const option = document.createElement('option');
                    option.value = servicio.id;
                    option.textContent = servicio.nombre;
                    option.selected = servicio.id === servicioIdSeleccionado;
                    selectServicio.appendChild(option);
                });
            })
            .catch(error => {
                console.error('Error al cargar servicios:', error);
            });
    }

    /**
     * Guarda los cambios de una cita
     */
    function guardarCambiosCita(modal) {
        const form = document.getElementById('editarCitaForm');
        const formData = new FormData(form);

        mostrarCargando(true);

        fetch(`/admin/citas/actualizar/${formData.get('id')}`, {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al actualizar la cita');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                // Cerrar modal
                document.body.removeChild(modal);

                // Actualizar tabla y dashboard
                cargarTodasLasCitas();
                actualizarDashboard();

                // Mostrar mensaje de éxito
                mostrarAlerta('Cita actualizada correctamente', 'success');
            } else {
                throw new Error(data.message || 'Error al actualizar la cita');
            }
            mostrarCargando(false);
        })
        .catch(error => {
            console.error('Error al guardar cambios:', error);
            mostrarAlerta(error.message || 'Error al actualizar la cita', 'error');
            mostrarCargando(false);
        });
    }

    /**
     * Confirma la eliminación de una cita
     */
    function confirmarEliminarCita(citaId) {
        const modal = document.createElement('div');
        modal.className = 'modal';

        modal.innerHTML = `
            <div class="modal-content confirm-modal">
                <div class="modal-header">
                    <h3>Confirmar Eliminación</h3>
                    <button type="button" class="close-modal">
                        <i class="ri-close-line"></i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="confirm-icon">
                        <i class="ri-error-warning-line"></i>
                    </div>
                    <p>¿Estás seguro de que deseas eliminar esta cita?</p>
                    <p class="confirm-warning">Esta acción no se puede deshacer.</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-secondary" id="cancelarEliminacion">
                        <i class="ri-close-line"></i> Cancelar
                    </button>
                    <button type="button" class="btn-danger" id="confirmarEliminacion">
                        <i class="ri-delete-bin-line"></i> Eliminar
                    </button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        // Cerrar modal
        modal.querySelector('.close-modal').addEventListener('click', function() {
            document.body.removeChild(modal);
        });

        // Cancelar eliminación
        document.getElementById('cancelarEliminacion').addEventListener('click', function() {
            document.body.removeChild(modal);
        });

        // Confirmar eliminación
        document.getElementById('confirmarEliminacion').addEventListener('click', function() {
            eliminarCita(citaId, modal);
        });
    }

    /**
     * Elimina una cita
     */
    function eliminarCita(citaId, modal) {
        mostrarCargando(true);

        fetch(`/admin/citas/eliminar/${citaId}`, {
            method: 'POST'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al eliminar la cita');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                // Cerrar modal
                document.body.removeChild(modal);

                // Eliminar fila de la tabla
                const citaRow = document.getElementById(`cita-${citaId}`);
                if (citaRow) {
                    citaRow.remove();
                }

                // Actualizar dashboard
                actualizarDashboard();

                // Mostrar mensaje de éxito
                mostrarAlerta('Cita eliminada correctamente', 'success');
            } else {
                throw new Error(data.message || 'Error al eliminar la cita');
            }
            mostrarCargando(false);
        })
        .catch(error => {
            console.error('Error al eliminar cita:', error);
            mostrarAlerta(error.message || 'Error al eliminar la cita', 'error');
            mostrarCargando(false);
        });
    }

    /**
     * Cambia el estado de una cita
     */
    function cambiarEstadoCita(id, estado, modal) {
        mostrarCargando(true);

        const formData = new FormData();
        formData.append('estado', estado);

        fetch(`/admin/citas/${id}/estado`, {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al cambiar el estado de la cita');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                // Cerrar el modal si existe
                if (modal) {
                    document.body.removeChild(modal);
                }

                // Actualizar la tabla
                const citaRow = document.getElementById(`cita-${id}`);
                if (citaRow) {
                    const estadoCell = citaRow.querySelector('.status-badge');
                    if (estadoCell) {
                        estadoCell.className = `status-badge ${estado.toLowerCase()}`;
                        estadoCell.textContent = estado;
                    }
                }

                // Actualizar dashboard
                actualizarDashboard();

                // Mostrar mensaje de éxito
                mostrarAlerta('Estado de la cita actualizado correctamente', 'success');
            } else {
                throw new Error(data.message || 'Error al actualizar el estado de la cita');
            }
            mostrarCargando(false);
        })
        .catch(error => {
            console.error('Error al cambiar el estado de la cita:', error);
            mostrarAlerta(error.message || 'Error al actualizar el estado de la cita', 'error');
            mostrarCargando(false);
        });
    }

    /**
     * Muestra una alerta en la interfaz
     */
    function mostrarAlerta(mensaje, tipo) {
        const alertDiv = document.createElement('div');
        alertDiv.className = `admin-alert ${tipo}`;
        alertDiv.innerHTML = `
            <i class="${tipo === 'success' ? 'ri-check-line' : 'ri-error-warning-line'}"></i>
            <p>${mensaje}</p>
            <button class="close-alert"><i class="ri-close-line"></i></button>
        `;

        const adminContent = document.querySelector('.admin-content');
        adminContent.insertBefore(alertDiv, adminContent.firstChild);

        // Agregar evento para cerrar la alerta
        alertDiv.querySelector('.close-alert').addEventListener('click', function() {
            alertDiv.style.display = 'none';
        });

        // Ocultar la alerta después de 5 segundos
        setTimeout(() => {
            alertDiv.style.display = 'none';
            // Eliminar la alerta del DOM después de ocultarla
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.parentNode.removeChild(alertDiv);
                }
            }, 300);
        }, 5000);
    }
});