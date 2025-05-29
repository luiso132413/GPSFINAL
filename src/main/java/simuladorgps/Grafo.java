package simuladorgps;
import java.io.Serializable;
import java.util.*;
import javax.swing.*;

public class Grafo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<Ciudad, List<Ruta>> ady = new HashMap<>();
    private List<Ciudad> ciudades = new ArrayList<>();
    private Map<Integer, Ciudad> ciudadPorId = new HashMap<>();

    public void agregarCiudad(Ciudad ciudad) {
        if (ciudad == null) {
            throw new IllegalArgumentException("La ciudad no puede ser null");
        }
        if (!ady.containsKey(ciudad)) {
            ady.put(ciudad, new ArrayList<>());
            ciudades.add(ciudad);
            ciudadPorId.put(ciudad.getId(), ciudad);
        }
    }

    public void conectarRuta(List<Ciudad> ciudades) {
        if (ciudades == null || ciudades.size() < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 ciudades para formar una ruta");
        }

        // Verificar que todas las ciudades existan en el grafo
        for (Ciudad ciudad : ciudades) {
            if (!ady.containsKey(ciudad)) {
                throw new IllegalArgumentException("La ciudad " + ciudad.getNombre() + " no existe en el grafo");
            }
        }

        // Conectar cada ciudad con la siguiente (unidireccional)
        for (int i = 0; i < ciudades.size() - 1; i++) {
            Ciudad origen = ciudades.get(i);
            Ciudad destino = ciudades.get(i + 1);

            if (!rutaExiste(origen, destino)) {
                double distancia = Funciones.recorrido(origen.getLatitud(), origen.getLongitud(),
                        destino.getLatitud(), destino.getLongitud());
                double tiempo = distancia / 60; // Tiempo estimado (60 km/h velocidad promedio)

                ady.get(origen).add(new Ruta(origen, destino, distancia, tiempo));
            }
        }
    }

    private boolean rutaExiste(Ciudad origen, Ciudad destino) {
        return ady.get(origen).stream()
                .anyMatch(ruta -> ruta.getDestino().equals(destino));
    }

    public List<Ciudad> getCiudades() {
        return new ArrayList<>(ciudades);
    }

    public Ciudad getCiudadPorId(int id) {
        return ciudadPorId.get(id);
    }

    public List<Ruta> getRutasDesde(Ciudad ciudad) {
        return new ArrayList<>(ady.getOrDefault(ciudad, new ArrayList<>()));
    }

    // Resto de los métodos permanecen exactamente iguales
    public List<Ciudad> rutaMasRapida(Ciudad origen, Ciudad destino, double velocidad) {
        Map<Ciudad, Double> distancias = new HashMap<>();
        Map<Ciudad, Ciudad> anteriores = new HashMap<>();
        PriorityQueue<Ciudad> cola = new PriorityQueue<>(Comparator.comparingDouble(distancias::get));
        Set<Ciudad> visitados = new HashSet<>();

        for(Ciudad ciudad : ciudades) {
            distancias.put(ciudad, Double.MAX_VALUE);
        }
        distancias.put(origen, 0.0);
        cola.add(origen);

        while(!cola.isEmpty()) {
            Ciudad actual = cola.poll();
            if(visitados.contains(actual)) continue;
            visitados.add(actual);

            if(actual.equals(destino)) break;

            for(Ruta ruta : ady.get(actual)) {
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
        for(Ciudad ciudad : ady.keySet()) {
            sb.append("Rutas desde ").append(ciudad.getNombre()).append(":\n");
            for(Ruta ruta : ady.get(ciudad)) {
                sb.append(String.format("  -> %s (%.2f km, %.2f min a las 12:00)%n",
                        ruta.getDestino().getNombre(),
                        ruta.getDistancia(),
                        Funciones.calcularTiempo(ruta.getDistancia(), 12, 0)));
            }
        }
        return sb.toString();
    }

    public String dijkstra(Ciudad inicio, Ciudad destino, int hora, int minuto) {
        Map<Ciudad, Double> distancias = new HashMap<>();
        Map<Ciudad, Ciudad> anteriores = new HashMap<>();
        PriorityQueue<Ciudad> cola = new PriorityQueue<>(Comparator.comparingDouble(distancias::get));
        Set<Ciudad> visitados = new HashSet<>();

        for(Ciudad ciudad : ciudades) {
            distancias.put(ciudad, Double.MAX_VALUE);
        }
        distancias.put(inicio, 0.0);
        cola.add(inicio);

        while(!cola.isEmpty()) {
            Ciudad actual = cola.poll();
            if(visitados.contains(actual)) continue;
            visitados.add(actual);

            if(actual.equals(destino)) break;

            for(Ruta ruta : ady.get(actual)) {
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
            for(Ruta r : ady.get(actual)) {
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
    public void conectarCiudad(Ciudad origen, Ciudad destino) {
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Las ciudades no pueden ser null");
        }
        if (!ady.containsKey(origen) || !ady.containsKey(destino)) {
            throw new IllegalArgumentException("Una o ambas ciudades no existen en el grafo");
        }
        if (rutaExiste(origen, destino)) {
            return; // Ya existe la conexión
        }

        double distancia = Funciones.recorrido(origen.getLatitud(), origen.getLongitud(),
                destino.getLatitud(), destino.getLongitud());
        double tiempo = distancia / 60; // Tiempo estimado (60 km/h velocidad promedio)

        ady.get(origen).add(new Ruta(origen, destino, distancia, tiempo));
    }

}