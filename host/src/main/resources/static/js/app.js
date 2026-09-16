// Cliente del tablero: estado en vivo por WebSocket y consultas REST.
// Las acciones de operador requieren sesion iniciada y el permiso que
// corresponda; el backend vuelve a verificarlo en cada peticion.

const PERMISO_POR_CONTROL = {
    barrera: "BARRERA_CONTROL",
    emergencia: "EMERGENCIA_CONTROL",
    configuracion: "CONFIG_UPDATE",
    cupos: "CUPOS_SYNC"
};

let sesion = { autenticado: false, permisos: [] };

const elementos = {
    cuposDisponibles: document.getElementById("cuposDisponibles"),
    capacidadTotal: document.getElementById("capacidadTotal"),
    cuposOcupados: document.getElementById("cuposOcupados"),
    estadoOperativo: document.getElementById("estadoOperativo"),
    estadoAlarma: document.getElementById("estadoAlarma"),
    estadoDispositivo: document.getElementById("estadoDispositivo"),
    estadoTablero: document.getElementById("estadoTablero"),
    tarjetaCupos: document.querySelector(".tarjeta-cupos"),
    mensaje: document.getElementById("mensaje"),
    estadoSesion: document.getElementById("estadoSesion"),
    formularioAcceso: document.getElementById("formularioAcceso"),
    usuarioAcceso: document.getElementById("usuarioAcceso"),
    claveAcceso: document.getElementById("claveAcceso"),
    cerrarSesion: document.getElementById("cerrarSesion"),
    controles: document.getElementById("controles"),
    cuerpoRegistros: document.querySelector("#tablaRegistros tbody"),
    cuerpoEventos: document.querySelector("#tablaEventos tbody")
};

let graficoOcupacion = null;
let graficoIngresos = null;

function avisar(texto, clase) {
    elementos.mensaje.textContent = texto;
    elementos.mensaje.className = "mensaje " + (clase || "");
}

function formatearFecha(iso) {
    if (!iso) {
        return "-";
    }
    return new Date(iso).toLocaleString("es-CO");
}

async function pedir(ruta) {
    const respuesta = await fetch(ruta);
    if (!respuesta.ok) {
        throw new Error("No se pudo consultar " + ruta);
    }
    return respuesta.json();
}

async function enviar(ruta, cuerpo) {
    const respuesta = await fetch(ruta, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "same-origin",
        body: JSON.stringify(cuerpo)
    });
    if (!respuesta.ok) {
        const detalle = await respuesta.json().catch(() => ({}));
        throw new Error(detalle.mensaje || "La operacion fue rechazada (" + respuesta.status + ")");
    }
    return respuesta;
}

function pintarEstado(estado) {
    elementos.cuposDisponibles.textContent = estado.cuposDisponibles;
    elementos.capacidadTotal.textContent = "de " + estado.capacidadTotal + " cupos";
    elementos.cuposOcupados.textContent = estado.cuposOcupados;
    elementos.tarjetaCupos.classList.toggle("sin-cupos", estado.cuposDisponibles === 0);

    elementos.estadoOperativo.textContent = estado.estado;
    elementos.estadoOperativo.className = "valor";
    if (estado.estado === "EMERGENCIA") {
        elementos.estadoOperativo.classList.add("alerta");
    } else if (estado.estado === "LLENO") {
        elementos.estadoOperativo.classList.add("aviso");
    }

    elementos.estadoAlarma.textContent = estado.alarmaActiva ? "SONANDO" : "EN SILENCIO";
    elementos.estadoAlarma.className = "valor" + (estado.alarmaActiva ? " alerta" : "");

    pintarConexionDispositivo(estado.dispositivoConectado);
    actualizarGraficoOcupacion(estado);
}

function pintarConexionDispositivo(conectado) {
    elementos.estadoDispositivo.textContent = conectado
        ? "Dispositivo: conectado"
        : "Dispositivo: sin conexion";
    elementos.estadoDispositivo.className = "pastilla " + (conectado ? "pastilla-ok" : "pastilla-alerta");
}

function actualizarGraficoOcupacion(estado) {
    if (!graficoOcupacion) {
        return;
    }
    graficoOcupacion.data.datasets[0].data = [estado.cuposOcupados, estado.cuposDisponibles];
    graficoOcupacion.update();
}

function pintarRegistros(registros) {
    elementos.cuerpoRegistros.innerHTML = "";
    registros.forEach((registro) => {
        const fila = document.createElement("tr");
        [
            registro.id,
            registro.placa || "-",
            formatearFecha(registro.horaEntrada),
            formatearFecha(registro.horaSalida),
            registro.estadoVisita,
            registro.duracionMinutos !== null ? registro.duracionMinutos + " min" : "-"
        ].forEach((texto) => {
            const celda = document.createElement("td");
            celda.textContent = texto;
            fila.appendChild(celda);
        });
        elementos.cuerpoRegistros.appendChild(fila);
    });
    actualizarGraficoIngresos(registros);
}

function pintarEventos(eventos) {
    elementos.cuerpoEventos.innerHTML = "";
    eventos.forEach((evento) => {
        const fila = document.createElement("tr");
        [
            evento.id,
            evento.nivelGas,
            evento.umbral,
            formatearFecha(evento.timestamp),
            evento.requiereEvacuacion ? "Si" : "No"
        ].forEach((texto) => {
            const celda = document.createElement("td");
            celda.textContent = texto;
            fila.appendChild(celda);
        });
        elementos.cuerpoEventos.appendChild(fila);
    });
}

function actualizarGraficoIngresos(registros) {
    if (!graficoIngresos) {
        return;
    }
    const porHora = new Array(24).fill(0);
    registros.forEach((registro) => {
        if (registro.horaEntrada) {
            porHora[new Date(registro.horaEntrada).getHours()] += 1;
        }
    });
    graficoIngresos.data.datasets[0].data = porHora;
    graficoIngresos.update();
}

function crearGraficos() {
    if (typeof Chart === "undefined") {
        return;
    }
    const colorTexto = "#93a1b0";
    const colorBorde = "#2f3944";

    graficoOcupacion = new Chart(document.getElementById("graficoOcupacion"), {
        type: "doughnut",
        data: {
            labels: ["Ocupados", "Disponibles"],
            datasets: [{
                data: [0, 0],
                backgroundColor: ["#e0524a", "#35c37d"],
                borderColor: "#1a2027",
                borderWidth: 2
            }]
        },
        options: {
            maintainAspectRatio: false,
            plugins: {
                legend: { labels: { color: colorTexto } },
                title: { display: true, text: "Ocupacion actual", color: colorTexto }
            }
        }
    });

    graficoIngresos = new Chart(document.getElementById("graficoIngresos"), {
        type: "bar",
        data: {
            labels: Array.from({ length: 24 }, (valor, hora) => hora + "h"),
            datasets: [{
                label: "Ingresos",
                data: new Array(24).fill(0),
                backgroundColor: "#35c37d"
            }]
        },
        options: {
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                title: { display: true, text: "Ingresos por hora", color: colorTexto }
            },
            scales: {
                x: { ticks: { color: colorTexto }, grid: { color: colorBorde } },
                y: { beginAtZero: true, ticks: { color: colorTexto, precision: 0 }, grid: { color: colorBorde } }
            }
        }
    });
}

async function refrescarTodo() {
    try {
        const [estado, registros, eventos, configuracion] = await Promise.all([
            pedir("/api/estado"),
            pedir("/api/registros?limite=100"),
            pedir("/api/eventos-seguridad?limite=100"),
            pedir("/api/configuracion")
        ]);
        pintarEstado(estado);
        pintarRegistros(registros);
        pintarEventos(eventos);
        document.getElementById("capacidadNueva").value = configuracion.capacidadTotal;
        document.getElementById("umbralNuevo").value = configuracion.umbralHumo;
    } catch (error) {
        avisar(error.message, "error");
    }
}

async function refrescarEstado() {
    try {
        pintarEstado(await pedir("/api/estado"));
    } catch (error) {
        avisar(error.message, "error");
    }
}

// Cada evento del backend refresca lo que corresponde; el estado es siempre el del servidor.
function alRecibirEvento(sobre) {
    switch (sobre.tipo) {
        case "CuposCambiadosEvento":
        case "EstadoSistemaCambiadoEvento":
        case "AlarmaCambiadaEvento":
            refrescarEstado();
            break;
        case "VehiculoIngresadoEvento":
        case "VehiculoEgresadoEvento":
            refrescarTodo();
            break;
        case "UmbralHumoSuperadoEvento":
            avisar("Umbral de humo superado: nivel " + sobre.datos.nivel, "error");
            refrescarTodo();
            break;
        case "BarreraBloqueadaEvento":
            avisar("Vehiculo detenido en " + sobre.datos.punto + ": la barrera sigue abierta", "error");
            break;
        case "DispositivoConexionCambiadaEvento":
            pintarConexionDispositivo(sobre.datos.conectado);
            break;
        default:
            break;
    }
}

function conectarTablero() {
    const protocolo = window.location.protocol === "https:" ? "wss" : "ws";
    const socket = new WebSocket(protocolo + "://" + window.location.host + "/tablero");

    socket.onopen = () => {
        elementos.estadoTablero.textContent = "Tablero: en vivo";
        elementos.estadoTablero.className = "pastilla pastilla-ok";
        refrescarTodo();
    };
    socket.onmessage = (mensaje) => alRecibirEvento(JSON.parse(mensaje.data));
    socket.onclose = () => {
        elementos.estadoTablero.textContent = "Tablero: reconectando";
        elementos.estadoTablero.className = "pastilla pastilla-alerta";
        setTimeout(conectarTablero, 3000);
    };
}

function tienePermiso(codigo) {
    return sesion.autenticado && sesion.permisos.includes(codigo);
}

function pintarSesion(nueva) {
    sesion = nueva;

    if (sesion.autenticado) {
        elementos.estadoSesion.textContent = sesion.usuario + " (" + sesion.roles.join(", ") + ")";
        elementos.estadoSesion.className = "pastilla pastilla-ok";
    } else {
        elementos.estadoSesion.textContent = "Sin sesion";
        elementos.estadoSesion.className = "pastilla pastilla-gris";
    }

    elementos.formularioAcceso.hidden = sesion.autenticado;
    elementos.cerrarSesion.hidden = !sesion.autenticado;
    elementos.controles.hidden = !sesion.autenticado;

    // Cada grupo se habilita solo si el rol trae su permiso.
    Object.entries(PERMISO_POR_CONTROL).forEach(([grupo, codigo]) => {
        const permitido = tienePermiso(codigo);
        document.querySelectorAll('[data-permiso="' + grupo + '"] button').forEach((boton) => {
            boton.disabled = !permitido;
            boton.title = permitido ? "" : "Su rol no tiene el permiso " + codigo;
        });
    });
}

async function refrescarSesion() {
    try {
        const respuesta = await fetch("/api/auth/sesion", { credentials: "same-origin" });
        pintarSesion(await respuesta.json());
    } catch (error) {
        pintarSesion({ autenticado: false, permisos: [], roles: [] });
    }
}

function conectarAcceso() {
    elementos.formularioAcceso.addEventListener("submit", async (evento) => {
        evento.preventDefault();
        try {
            const respuesta = await fetch("/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "same-origin",
                body: JSON.stringify({
                    usuario: elementos.usuarioAcceso.value.trim(),
                    contrasena: elementos.claveAcceso.value
                })
            });
            if (!respuesta.ok) {
                avisar("Usuario o contrasena incorrectos", "error");
                return;
            }
            elementos.claveAcceso.value = "";
            pintarSesion(await respuesta.json());
            avisar("Sesion iniciada", "exito");
        } catch (error) {
            avisar(error.message, "error");
        }
    });

    elementos.cerrarSesion.addEventListener("click", async () => {
        await fetch("/api/auth/logout", { method: "POST", credentials: "same-origin" });
        pintarSesion({ autenticado: false, permisos: [], roles: [] });
        avisar("Sesion cerrada", "");
    });
}

function conectarControles() {
    document.querySelectorAll("button.barrera").forEach((boton) => {
        boton.addEventListener("click", async () => {
            try {
                await enviar("/api/control/barrera", {
                    punto: boton.dataset.punto,
                    accion: boton.dataset.accion
                });
                avisar(boton.textContent + ": orden enviada", "exito");
            } catch (error) {
                avisar(error.message, "error");
            }
        });
    });

    document.getElementById("activarEmergencia").addEventListener("click", async () => {
        try {
            await enviar("/api/control/emergencia", { accion: "ACTIVAR" });
            avisar("Emergencia activada", "exito");
        } catch (error) {
            avisar(error.message, "error");
        }
    });

    document.getElementById("limpiarEmergencia").addEventListener("click", async () => {
        try {
            await enviar("/api/control/emergencia", { accion: "LIMPIAR" });
            avisar("Emergencia limpiada", "exito");
        } catch (error) {
            avisar(error.message, "error");
        }
    });

    document.getElementById("guardarConfiguracion").addEventListener("click", async () => {
        try {
            await enviar("/api/configuracion", {
                capacidadTotal: Number(document.getElementById("capacidadNueva").value),
                umbralHumo: Number(document.getElementById("umbralNuevo").value)
            });
            avisar("Configuracion actualizada", "exito");
            refrescarEstado();
        } catch (error) {
            avisar(error.message, "error");
        }
    });

    document.getElementById("sincronizarCupos").addEventListener("click", async () => {
        try {
            await enviar("/api/control/sincronizar-cupos", {
                cuposOcupados: Number(document.getElementById("ocupadosReales").value)
            });
            avisar("Conteo recalibrado", "exito");
            refrescarTodo();
        } catch (error) {
            avisar(error.message, "error");
        }
    });
}

crearGraficos();
conectarAcceso();
conectarControles();
conectarTablero();
refrescarSesion();
refrescarTodo();
