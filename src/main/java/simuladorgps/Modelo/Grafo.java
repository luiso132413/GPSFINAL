package simuladorgps.Modelo;

import java.io.Serializable;
import java.util.*;
import java.util.Objects;

public class Grafo implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<Ciudad, List<Ruta>> adyacencia = new HashMap<>();
    private final Map<Integer, Ciudad> ciudadPorId = new HashMap<>();

    // Métodos básicos de gestión del grafo
    public void agregarCiudad(Ciudad ciudad) {
        Objects.requireNonNull(ciudad, "La ciudad no puede ser null");
        adyacencia.putIfAbsent(ciudad, new ArrayList<>());
        ciudadPorId.putIfAbsent(ciudad.getId(), ciudad);
    }

    public boolean existeRuta(Ciudad origen, Ciudad destino) {
        validarCiudades(origen, destino);
        return !ejecutarDijkstra(origen, destino, (r, h, m) -> r.getDistancia()).isEmpty();
    }

    public void conectarCiudad(Ciudad origen, Ciudad destino) {
        validarConexion(origen, destino);

        if (!existeRutaDirecta(origen, destino)) {
            double distancia = Funciones.recorrido(origen.getLatitud(), origen.getLongitud(),
                    destino.getLatitud(), destino.getLongitud());
            adyacencia.get(origen).add(new Ruta(origen, destino, distancia, distancia / 60));
        }
    }

    private boolean existeRutaDirecta(Ciudad origen, Ciudad destino) {
        return adyacencia.getOrDefault(origen, Collections.emptyList())
                .stream()
                .anyMatch(ruta -> ruta.getDestino().equals(destino));
    }

    public void conectarRuta(List<Ciudad> ruta) {
        Objects.requireNonNull(ruta, "La lista de ciudades no puede ser null");
        if (ruta.size() < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 ciudades para formar una ruta");
        }

        for (int i = 0; i < ruta.size() - 1; i++) {
            conectarCiudad(ruta.get(i), ruta.get(i + 1));
        }
    }

    // Métodos de consulta
    public List<Ciudad> getCiudades() {
        return new ArrayList<>(adyacencia.keySet());
    }

    public List<Ruta> getRutasDesde(Ciudad ciudad) {
        return new ArrayList<>(adyacencia.getOrDefault(ciudad, Collections.emptyList()));
    }

    // Algoritmos de ruta
    public List<Ciudad> rutaMasRapida(Ciudad origen, Ciudad destino, double velocidad) {
        validarCiudades(origen, destino);
        return ejecutarDijkstra(origen, destino, (r, h, m) -> r.getDistancia() / velocidad * 60);
    }

    public String dijkstra(Ciudad inicio, Ciudad destino, int hora, int minuto) {
        validarCiudades(inicio, destino);
        validarHora(hora, minuto);

        List<Ciudad> camino = ejecutarDijkstra(inicio, destino,
                (r, h, m) -> Funciones.calcularTiempo(r.getDistancia(), h, m));

        if (camino.isEmpty()) {
            return "No existe ruta entre " + inicio.getNombre() + " y " + destino.getNombre();
        }

        return construirReporteRuta(inicio, destino, hora, minuto, camino);
    }

    // Métodos privados de ayuda
    private void validarConexion(Ciudad origen, Ciudad destino) {
        Objects.requireNonNull(origen, "La ciudad origen no puede ser null");
        Objects.requireNonNull(destino, "La ciudad destino no puede ser null");

        if (origen.equals(destino)) {
            throw new IllegalArgumentException("No se puede conectar una ciudad consigo misma");
        }
        if (!adyacencia.containsKey(origen) || !adyacencia.containsKey(destino)) {
            throw new IllegalArgumentException("Una o ambas ciudades no existen en el grafo");
        }
    }

    private void validarCiudades(Ciudad origen, Ciudad destino) {
        Objects.requireNonNull(origen, "La ciudad origen no puede ser null");
        Objects.requireNonNull(destino, "La ciudad destino no puede ser null");
        if (!adyacencia.containsKey(origen) || !adyacencia.containsKey(destino)) {
            throw new IllegalArgumentException("Una o ambas ciudades no existen en el grafo");
        }
    }

    private void validarHora(int hora, int minuto) {
        if (hora < 0 || hora > 23 || minuto < 0 || minuto > 59) {
            throw new IllegalArgumentException("Hora inválida. Use valores entre 0-23 para horas y 0-59 para minutos");
        }
    }

    private List<Ciudad> ejecutarDijkstra(Ciudad inicio, Ciudad destino,
                                          TriFunction<Ruta, Integer, Integer, Double> calculadorTiempo) {
        Map<Ciudad, Double> distancias = new HashMap<>();
        Map<Ciudad, Ciudad> anteriores = new HashMap<>();
        PriorityQueue<Ciudad> cola = new PriorityQueue<>(Comparator.comparingDouble(distancias::get));

        // Inicialización
        adyacencia.keySet().forEach(ciudad -> distancias.put(ciudad, Double.MAX_VALUE));
        distancias.put(inicio, 0.0);
        cola.add(inicio);

        while (!cola.isEmpty()) {
            Ciudad actual = cola.poll();
            if (actual.equals(destino)) break;

            // Si la ciudad actual tiene accidente, ignoramos sus rutas (no expandimos desde aquí)
            if (actual.tieneAccidente()) {
                continue;
            }

            for (Ruta ruta : adyacencia.get(actual)) {
                Ciudad vecino = ruta.getDestino();

                // Ignorar rutas hacia ciudades con accidente
                if (vecino.tieneAccidente()) {
                    continue;
                }

                double tiempo = calculadorTiempo.apply(ruta, 0, 0);
                double nuevaDistancia = distancias.get(actual) + tiempo;

                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    anteriores.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        return reconstruirCamino(destino, anteriores);
    }

    private List<Ciudad> reconstruirCamino(Ciudad destino, Map<Ciudad, Ciudad> anteriores) {
        List<Ciudad> camino = new ArrayList<>();

        // Si no hay camino al destino
        if (!anteriores.containsKey(destino)) {
            return Collections.emptyList();
        }

        // Reconstruir el camino desde el destino hasta el inicio
        for (Ciudad at = destino; at != null; at = anteriores.get(at)) {
            camino.add(at);
        }

        // Invertir para tener inicio -> destino
        Collections.reverse(camino);
        return camino;
    }

    private String construirReporteRuta(Ciudad inicio, Ciudad destino,
                                        int hora, int minuto, List<Ciudad> camino) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- RUTA ÓPTIMA ---\n")
                .append(String.format("De: %s a %s%n", inicio.getNombre(), destino.getNombre()))
                .append(String.format("Hora de salida: %02d:%02d%n", hora, minuto))
                .append("\nDetalle del recorrido:\n");

        int horaActual = hora;
        int minutoActual = minuto;
        double distanciaTotal = 0.0;
        int tiempoTotalMinutos = 0;
        Map<String, Double> velocidadesPorTramo = new LinkedHashMap<>();

        for (int i = 0; i < camino.size() - 1; i++) {
            Ciudad actual = camino.get(i);
            Ciudad siguiente = camino.get(i + 1);

            for (Ruta ruta : adyacencia.get(actual)) {
                if (ruta.getDestino().equals(siguiente)) {
                    // Tiempo y velocidad para este tramo, según la hora actual
                    double velocidad = Funciones.velocidadPorHora(horaActual, minutoActual);
                    double tiempoTramoMin = Funciones.calcularTiempo(ruta.getDistancia(), horaActual, minutoActual);
                    int minutosTramo = (int) Math.round(tiempoTramoMin);
                    double velocidadTramo = ruta.getDistancia() / (tiempoTramoMin / 60.0);

                    velocidadesPorTramo.put(
                            actual.getNombre() + " → " + siguiente.getNombre(),
                            velocidadTramo
                    );

                    tiempoTotalMinutos += minutosTramo;
                    distanciaTotal += ruta.getDistancia();

                    sb.append(String.format("%s → %s%n", actual.getNombre(), siguiente.getNombre()))
                            .append(String.format("  Distancia: %.2f km%n", ruta.getDistancia()))
                            .append(String.format("  Tiempo estimado: %d min%n", minutosTramo))
                            .append(String.format("  Velocidad media: %.1f km/h%n", velocidadTramo))
                            .append(String.format("  Salida: %02d:%02d%n", horaActual, minutoActual));

                    // Actualizar hora de llegada
                    minutoActual += minutosTramo;
                    horaActual += minutoActual / 60;
                    minutoActual %= 60;
                    horaActual %= 24;

                    sb.append(String.format("  Llegada estimada: %02d:%02d%n%n", horaActual, minutoActual));
                    break;
                }
            }
        }

        // Velocidad media global
        double velocidadMediaGlobal = (tiempoTotalMinutos > 0)
                ? (distanciaTotal / (tiempoTotalMinutos / 60.0))
                : 0.0;

        // Hora final
        int horaLlegada = hora;
        int minutoLlegada = minuto + tiempoTotalMinutos;
        horaLlegada += minutoLlegada / 60;
        minutoLlegada %= 60;
        horaLlegada %= 24;

        sb.append("--- RESUMEN DEL VIAJE ---\n")
                .append(String.format("Distancia total: %.2f km%n", distanciaTotal))
                .append(String.format("Tiempo total: %d min%n", tiempoTotalMinutos))
                .append(String.format("Hora estimada de llegada: %02d:%02d%n", horaLlegada, minutoLlegada))
                .append(String.format("Velocidad media global: %.1f km/h%n", velocidadMediaGlobal));

        return sb.toString();
    }


    public void agregarRutaDirecta(Ciudad origen, Ciudad destino, double distancia, double tiempo) {
        validarConexion(origen, destino);

        if (!existeRutaDirecta(origen, destino)) {
            Ruta ruta = new Ruta(origen, destino, distancia, tiempo);
            adyacencia.get(origen).add(ruta);
        }
    }

    @FunctionalInterface
    private interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }
}