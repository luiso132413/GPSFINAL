package simuladorgps;

import java.util.*;
import javax.swing.*;

public class Grafo {
    private Map<Ciudad, List<Ruta>> ady = new HashMap<>();
    private List<Ciudad> ciudades = new ArrayList<>();

    public void agregarCiudad(Ciudad ciudad) {
        ady.putIfAbsent(ciudad, new ArrayList<>());
        ciudades.add(ciudad);
    }

    public void conectarCiudad(Ciudad origen, Ciudad destino) {
        double distancia = Funciones.recorrido(origen.getLatitud(), origen.getLongitud(),
                destino.getLatitud(), destino.getLongitud());
        ady.get(origen).add(new Ruta(origen, destino, distancia));
        ady.get(destino).add(new Ruta(destino, origen, distancia));
    }

    public List<Ciudad> getCiudades() {
        return new ArrayList<>(ciudades);
    }

    public Ciudad getCiudadPorId(int id) {
        for(Ciudad ciudad : ciudades) {
            if(ciudad.getId() == id) {
                return ciudad;
            }
        }
        return null;
    }

    public List<Ruta> getRutasDesde(Ciudad ciudad) {
        return ady.getOrDefault(ciudad, new ArrayList<>());
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
        // Implementación mejorada del algoritmo Dijkstra
        Map<Ciudad, Double> distancias = new HashMap<>();
        Map<Ciudad, Ciudad> anteriores = new HashMap<>();
        PriorityQueue<Ciudad> cola = new PriorityQueue<>(Comparator.comparingDouble(distancias::get));
        Set<Ciudad> visitados = new HashSet<>();

        // Inicialización
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

        // Reconstruir y mostrar el camino
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

            // Encontrar la ruta entre estas ciudades
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
}