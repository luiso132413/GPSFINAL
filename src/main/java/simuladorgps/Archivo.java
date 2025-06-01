package simuladorgps;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.*;

public class Archivo {

    public static void guardarDatos(Grafo grafo) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar datos del grafo");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();

            if (!filePath.toLowerCase().endsWith(".txt")) {
                fileToSave = new File(filePath + ".txt");
            }

            try (PrintWriter writer = new PrintWriter(fileToSave)) {
                // Escribir ciudades
                writer.println("=== CIUDADES ===");
                for (Ciudad ciudad : grafo.getCiudades()) {
                    writer.println(ciudad.toString());
                }

                // Escribir conexiones
                writer.println("\n=== RUTAS ===");
                for (Ciudad ciudad : grafo.getCiudades()) {
                    for (Ruta ruta : grafo.getRutasDesde(ciudad)) {
                        writer.println(String.format("%d -> %d (Distancia: %.2f km, Tiempo: %.2f min)",
                                ruta.getOrigen().getId(),
                                ruta.getDestino().getId(),
                                ruta.getDistancia(),
                                ruta.getTiempo()));
                    }
                }

                JOptionPane.showMessageDialog(null, "Datos guardados correctamente en:\n" + fileToSave.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error al guardar:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static Grafo cargarDatos() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Cargar datos del grafo");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            Grafo grafo = new Grafo();
            Map<Integer, Ciudad> ciudadesMap = new HashMap<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(fileToLoad))) {
                String line;
                boolean enCiudades = false;
                boolean enRutas = false;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    if (line.equals("=== CIUDADES ===")) {
                        enCiudades = true;
                        enRutas = false;
                        continue;
                    } else if (line.equals("=== RUTAS ===")) {
                        enCiudades = false;
                        enRutas = true;
                        continue;
                    }

                    if (enCiudades) {
                        Ciudad ciudad = parsearCiudad(line);
                        if (ciudad != null) {
                            grafo.agregarCiudad(ciudad);
                            ciudadesMap.put(ciudad.getId(), ciudad);
                        }
                    } else if (enRutas) {
                        parsearRuta(line, grafo, ciudadesMap);
                    }
                }

                // Distribuir ciudades si no tienen coordenadas visuales
                distribuirCiudadesVisualmente(grafo);

                JOptionPane.showMessageDialog(null,
                        "Datos cargados exitosamente desde: " + fileToLoad.getAbsolutePath());
                return grafo;

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                        "Error al cargar los datos: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    private static Ciudad parsearCiudad(String linea) {
        try {
            String[] partes = linea.split(" \\(");
            String nombre = partes[0].trim();

            String[] datos = partes[1].replace(")", "").split(", ");
            int id = Integer.parseInt(datos[0].replace("ID: ", ""));
            double latitud = Double.parseDouble(datos[1].replace("Lat: ", ""));
            double longitud = Double.parseDouble(datos[2].replace("Long: ", ""));

            Ciudad ciudad = new Ciudad(id, nombre, latitud, longitud);

            // Si el archivo incluye coordenadas visuales
            if (datos.length > 3) {
                int x = Integer.parseInt(datos[3].replace("X: ", ""));
                int y = Integer.parseInt(datos[4].replace("Y: ", ""));
                ciudad.setXVisual(x);
                ciudad.setYVisual(y);
            }

            return ciudad;
        } catch (Exception e) {
            System.err.println("Error al parsear ciudad: " + linea);
            return null;
        }
    }

    private static void parsearRuta(String linea, Grafo grafo, Map<Integer, Ciudad> ciudadesMap) {
        try {
            // Formato esperado: "1 -> 2 (Distancia: 6.63 km, Tiempo: 0.11 min)"
            String[] partes = linea.split(" -> ");
            if (partes.length < 2) {
                System.err.println("Formato de ruta inválido: " + linea);
                return;
            }

            int idOrigen = Integer.parseInt(partes[0].trim());

            String segundaParte = partes[1].trim();
            int idDestino = Integer.parseInt(segundaParte.substring(0, segundaParte.indexOf(" ")).trim());

            String datosRuta = segundaParte.substring(segundaParte.indexOf("(") + 1, segundaParte.indexOf(")"));
            String[] valores = datosRuta.split(", ");

            double distancia = Double.parseDouble(valores[0].replace("Distancia: ", "").replace(" km", ""));
            double tiempo = Double.parseDouble(valores[1].replace("Tiempo: ", "").replace(" min", ""));

            Ciudad origen = ciudadesMap.get(idOrigen);
            Ciudad destino = ciudadesMap.get(idDestino);

            if (origen != null && destino != null) {
                grafo.agregarRutaDirecta(origen, destino, distancia, tiempo);
            } else {
                System.err.println("Ciudades no encontradas para ruta: " + linea);
            }
        } catch (Exception e) {
            System.err.println("Error al parsear ruta: " + linea);
            e.printStackTrace();
        }
    }

    private static void distribuirCiudadesVisualmente(Grafo grafo) {
        List<Ciudad> ciudades = grafo.getCiudades();
        int centerX = 400; // Centro X del panel
        int centerY = 300; // Centro Y del panel
        int radius = Math.min(centerX, centerY) - 50; // Radio del círculo

        double angleStep = 2 * Math.PI / ciudades.size();
        double currentAngle = 0;

        for (Ciudad ciudad : ciudades) {
            // Si la ciudad ya tiene coordenadas visuales, no las cambiamos
            if (ciudad.getXVisual() == 0 && ciudad.getYVisual() == 0) {
                int x = (int)(centerX + radius * Math.cos(currentAngle));
                int y = (int)(centerY + radius * Math.sin(currentAngle));
                ciudad.setXVisual(x);
                ciudad.setYVisual(y);
                currentAngle += angleStep;
            }
        }
    }
}