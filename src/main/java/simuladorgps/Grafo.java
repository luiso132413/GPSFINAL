package simuladorgps;
import java.io.Serializable;
import java.util.*;
import java.util.Objects;
import javax.swing.*;

public class Grafo implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<Ciudad, List<Ruta>> adyacencia = new HashMap<>();
    private final Map<Integer, Ciudad> ciudadPorId = new HashMap<>();

    /**
     * Verifica si existe una ruta directa de una ciudad a otra
     * @param origen Ciudad de origen
     * @param destino Ciudad de destino
     * @return true si existe una ruta directa, false en caso contrario
     */
    public boolean existeRuta(Ciudad origen, Ciudad destino) {
        Objects.requireNonNull(origen, "La ciudad origen no puede ser null");
        Objects.requireNonNull(destino, "La ciudad destino no puede ser null");

        if (!adyacencia.containsKey(origen)) {
            return false;
        }

        for (Ruta ruta : adyacencia.get(origen)) {
            if (ruta.getDestino().equals(destino)) {
                return true;
            }
        }

        return false;
    }

    // Resto de los métodos permanecen igual...
    public void agregarCiudad(Ciudad ciudad) {
        Objects.requireNonNull(ciudad, "La ciudad no puede ser null");

        if (!adyacencia.containsKey(ciudad)) {
            adyacencia.put(ciudad, new ArrayList<>());
            ciudadPorId.put(ciudad.getId(), ciudad);
        }
    }

    public void conectarRuta(List<Ciudad> ruta) {
        Objects.requireNonNull(ruta, "La lista de ciudades no puede ser null");
        if (ruta.size() < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 ciudades para formar una ruta");
        }

        for (Ciudad ciudad : ruta) {
            if (!adyacencia.containsKey(ciudad)) {
                throw new IllegalArgumentException("La ciudad " + ciudad.getNombre() + " no existe en el grafo");
            }
        }

        for (int i = 0; i < ruta.size() - 1; i++) {
            conectarDirectamente(ruta.get(i), ruta.get(i + 1));
        }
    }

    public void conectarCiudad(Ciudad origen, Ciudad destino) {
        Objects.requireNonNull(origen, "La ciudad origen no puede ser null");
        Objects.requireNonNull(destino, "La ciudad destino no puede ser null");

        if (!adyacencia.containsKey(origen) || !adyacencia.containsKey(destino)) {
            throw new IllegalArgumentException("Una o ambas ciudades no existen en el grafo");
        }

        conectarDirectamente(origen, destino);
    }

    private void conectarDirectamente(Ciudad origen, Ciudad destino) {
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("No se puede conectar una ciudad consigo misma");
        }

        if (!existeRuta(origen, destino)) {
            double distancia = Funciones.recorrido(origen.getLatitud(), origen.getLongitud(),
                    destino.getLatitud(), destino.getLongitud());
            double tiempo = distancia / 60;

            adyacencia.get(origen).add(new Ruta(origen, destino, distancia, tiempo));
        }
    }

    public List<Ciudad> getCiudades() {
        return new ArrayList<>(adyacencia.keySet());
    }

    public Ciudad getCiudadPorId(int id) {
        return ciudadPorId.get(id);
    }

    public List<Ruta> getRutasDesde(Ciudad ciudad) {
        return new ArrayList<>(adyacencia.getOrDefault(ciudad, Collections.emptyList()));
    }

    public List<Ciudad> rutaMasRapida(Ciudad origen, Ciudad destino, double velocidad) {
        Map<Ciudad, Double> distancias = new HashMap<>();
        Map<Ciudad, Ciudad> anteriores = new HashMap<>();
        PriorityQueue<Ciudad> cola = new PriorityQueue<>(Comparator.comparingDouble(distancias::get));
        Set<Ciudad> visitados = new HashSet<>();

        for(Ciudad ciudad : adyacencia.keySet()) {
            distancias.put(ciudad, Double.MAX_VALUE);
        }
        distancias.put(origen, 0.0);
        cola.add(origen);

        while(!cola.isEmpty()) {
            Ciudad actual = cola.poll();
            if(visitados.contains(actual)) continue;
            visitados.add(actual);

            if(actual.equals(destino)) break;

            for(Ruta ruta : adyacencia.get(actual)) {
                Ciudad vecino = ruta.getDestino();
                double tiempoViaje = ruta.getDistancia() / velocidad * 60;
                double nuevaDistancia = distancias.get(actual) + tiempoViaje;

                if(nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    anteriores.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        if(distancias.get(destino) == Double.MAX_VALUE) {
            return Collections.emptyList();
        }

        List<Ciudad> camino = new ArrayList<>();
        for(Ciudad at = destino; at != null; at = anteriores.get(at)) {
            camino.add(at);
        }
        Collections.reverse(camino);

        return camino;
    }

    public String mostrarTodasLasRutas() {
        StringBuilder sb = new StringBuilder();
        for(Ciudad ciudad : adyacencia.keySet()) {
            sb.append("Rutas desde ").append(ciudad.getNombre()).append(":\n");
            for(Ruta ruta : adyacencia.get(ciudad)) {
                sb.append(String.format("  -> %s (%.2f km, %.2f min a las 12:00)%n",
                        ruta.getDestino().getNombre(),
                        ruta.getDistancia(),
                        Funciones.calcularTiempo(ruta.getDistancia(), 12, 0)));
            }
        }
        return sb.toString();
    }

    public String dijkstra(Ciudad inicio, Ciudad destino, int hora, int minuto) {
        Objects.requireNonNull(inicio, "La ciudad de inicio no puede ser null");
        Objects.requireNonNull(destino, "La ciudad destino no puede ser null");

        if (hora < 0 || hora > 23 || minuto < 0 || minuto > 59) {
            throw new IllegalArgumentException("Hora o minuto inválidos");
        }

        Map<Ciudad, Double> distancias = new HashMap<>();
        Map<Ciudad, Ciudad> anteriores = new HashMap<>();
        PriorityQueue<Ciudad> cola = new PriorityQueue<>(Comparator.comparingDouble(distancias::get));
        Set<Ciudad> visitados = new HashSet<>();

        for(Ciudad ciudad : adyacencia.keySet()) {
            distancias.put(ciudad, Double.MAX_VALUE);
        }
        distancias.put(inicio, 0.0);
        cola.add(inicio);

        while(!cola.isEmpty()) {
            Ciudad actual = cola.poll();
            if(visitados.contains(actual)) continue;
            visitados.add(actual);

            if(actual.equals(destino)) break;

            for(Ruta ruta : adyacencia.get(actual)) {
                Ciudad vecino = ruta.getDestino();
                double tiempoViaje = Funciones.calcularTiempo(ruta.getDistancia(), hora, minuto);
                double nuevaDistancia = distancias.get(actual) + tiempoViaje;

                if(nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    anteriores.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        if(distancias.get(destino) == Double.MAX_VALUE) {
            sb.append("No existe ruta entre las ciudades seleccionadas\n");
            return sb.toString();
        }

        List<Ciudad> camino = new ArrayList<>();
        for(Ciudad at = destino; at != null; at = anteriores.get(at)) {
            camino.add(at);
        }
        Collections.reverse(camino);

        sb.append("\n--- RUTA ÓPTIMA ---\n");
        sb.append(String.format("De %s a %s%n", inicio.getNombre(), destino.getNombre()));
        sb.append(String.format("Hora de salida: %02d:%02d%n", hora, minuto));
        sb.append(String.format("Tiempo estimado: %.2f minutos%n%n", distancias.get(destino)));

        sb.append("Detalle del recorrido:\n");
        double tiempoAcumulado = 0;
        for(int i = 0; i < camino.size() - 1; i++) {
            Ciudad actual = camino.get(i);
            Ciudad siguiente = camino.get(i + 1);

            Ruta ruta = null;
            for(Ruta r : adyacencia.get(actual)) {
                if(r.getDestino().equals(siguiente)) {
                    ruta = r;
                    break;
                }
            }

            if(ruta != null) {
                double tiempoTramo = Funciones.calcularTiempo(ruta.getDistancia(),
                        (hora + (int)tiempoAcumulado/60) % 24,
                        (minuto + (int)tiempoAcumulado%60) % 60);

                sb.append(String.format("%s -> %s (%.2f km, %.2f min)%n",
                        actual.getNombre(), siguiente.getNombre(),
                        ruta.getDistancia(), tiempoTramo));

                tiempoAcumulado += tiempoTramo;
            }
        }

        return sb.toString();
    }
}