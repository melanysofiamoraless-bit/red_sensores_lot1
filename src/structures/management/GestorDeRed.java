package structures.management;

import model.LecturaSensor;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GestorDeRed - Módulo Integrador de Estructuras de Datos
 */
public class GestorDeRed {
    // 1. Historial de Eventos: Registro lineal de todas las actividades
    private List<String> historialEventos;

    // 2. Navegador de Alertas: Permite moverse adelante y atrás entre alertas
    private Deque<String> navegadorAlertas;
    private String alertaActual;

    // 3. Ciclo de Monitoreo: Gestión de la rotación de sensores a consultar
    private Queue<String> colaMonitoreo;

    // 4. Topología de la Red: Grafo de conexiones entre estaciones (Lista de adyacencia)
    private Map<String, List<String>> topologiaRed;

    // 5. Buffer de Ingesta: Cola de espera para procesar lecturas entrantes
    private Queue<LecturaSensor> bufferIngesta;

    public GestorDeRed() {
        this.historialEventos = new ArrayList<>();
        this.navegadorAlertas = new ArrayDeque<>();
        this.colaMonitoreo = new ArrayDeque<>();
        this.topologiaRed = new HashMap<>();
        this.bufferIngesta = new ArrayDeque<>();
    }

    // =========================================================================
    // SECCIÓN 1: HISTORIAL Y EVENTOS
    // =========================================================================

    public void agregarEvento(String evento) {
        // TODO 1: marca de tiempo + descripción
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        historialEventos.add("[" + fecha + "] " + evento);
    }

    public List<String> obtenerEventosRecientes(int limite) {
        if (historialEventos.size() <= limite) return new ArrayList<>(historialEventos);
        return historialEventos.subList(historialEventos.size() - limite, historialEventos.size());
    }

    public void limpiarHistorialAntiguo(int mantener) {
        // TODO 10: eliminar los más antiguos (posición 0) hasta quedar con 'mantener'
        int limite = Math.max(mantener, 0);
        while (historialEventos.size() > limite) {
            historialEventos.remove(0);
        }
    }

    // =========================================================================
    // SECCIÓN 2: NAVEGACIÓN DE ALERTAS
    // =========================================================================

    public void registrarAlerta(String alerta) {
        navegadorAlertas.addLast(alerta);
        alertaActual = alerta;
    }

    public String navegarAlSiguiente() {
        // TODO 2: sacar del frente y reinsertar al final (ciclo)
        if (navegadorAlertas.isEmpty()) return null;
        String alerta = navegadorAlertas.pollFirst();
        navegadorAlertas.addLast(alerta);
        alertaActual = alerta;
        return alerta;
    }

    public String navegarAlAnterior() {
        // TODO 3: sentido inverso: sacar del final y reinsertar al frente
        if (navegadorAlertas.isEmpty()) return null;
        String alerta = navegadorAlertas.pollLast();
        navegadorAlertas.addFirst(alerta);
        alertaActual = alerta;
        return alerta;
    }

    public String getAlertaActual() { return alertaActual; }

    // =========================================================================
    // SECCIÓN 3: CICLO DE MONITOREO
    // =========================================================================

    public void programarConsulta(String idSensor) {
        // TODO 4: sin duplicados
        if (!colaMonitoreo.contains(idSensor)) {
            colaMonitoreo.add(idSensor);
        }
    }

    public String obtenerSiguienteSensor() {
        // TODO 5: rotación (poll + add); null si está vacía
        String sensor = colaMonitoreo.poll();
        if (sensor == null) return null;
        colaMonitoreo.add(sensor);
        return sensor;
    }

    // =========================================================================
    // SECCIÓN 4: TOPOLOGÍA DE RED
    // =========================================================================

    public void registrarConexion(String sensorA, String sensorB) {
        // TODO 6: conexión bidireccional sin duplicados
        List<String> vecinosA = topologiaRed.computeIfAbsent(sensorA, k -> new ArrayList<>());
        List<String> vecinosB = topologiaRed.computeIfAbsent(sensorB, k -> new ArrayList<>());
        if (sensorA.equals(sensorB)) return; // sin auto-conexiones
        if (!vecinosA.contains(sensorB)) vecinosA.add(sensorB);
        if (!vecinosB.contains(sensorA)) vecinosB.add(sensorA);
    }

    public List<String> obtenerVecinos(String idSensor) {
        // TODO 7: lista vacía si no existe
        return topologiaRed.getOrDefault(idSensor, new ArrayList<>());
    }

    public List<String> detectarEstacionesAisladas() {
        // TODO 9: sensores registrados sin vecinos
        List<String> aisladas = new ArrayList<>();
        for (Map.Entry<String, List<String>> entrada : topologiaRed.entrySet()) {
            if (entrada.getValue().isEmpty()) {
                aisladas.add(entrada.getKey());
            }
        }
        return aisladas;
    }

    // =========================================================================
    // SECCIÓN 5: BUFFER DE INGESTA
    // =========================================================================

    public void recibirLectura(LecturaSensor lectura) {
        bufferIngesta.add(lectura);
    }

    public LecturaSensor procesarSiguienteLectura() {
        // TODO 8: FIFO; poll() devuelve null si está vacío
        return bufferIngesta.poll();
    }

    public List<LecturaSensor> filtrarAlertasCriticas(double limitePm25) {
        // TODO EXTRA: Streams (ajusta el getter si tu clase lo llama distinto)
        return bufferIngesta.stream()
                .filter(l -> l.getPm25() > limitePm25)
                .collect(Collectors.toList());
    }
}