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
            Map<Integer, Ciudad> ciudadesMap = new HashMap<>(); // Usamos ID como clave

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
                        try {
                            // Formato: "1 -> 2 (Distancia: 123.45 km, Tiempo: 67.89 min)"
                            String[] partes = line.split(" -> | \\(");
                            int idOrigen = Integer.parseInt(partes[0].trim());
                            int idDestino = Integer.parseInt(partes[1].trim());

                            String[] datos = partes[2].replace(")", "").split(", ");
                            double distancia = Double.parseDouble(datos[0].replace("Distancia: ", "").replace(" km", ""));
                            double tiempo = Double.parseDouble(datos[1].replace("Tiempo: ", "").replace(" min)", ""));

                            Ciudad origen = ciudadesMap.get(idOrigen);
                            Ciudad destino = ciudadesMap.get(idDestino);

                            if (origen != null && destino != null) {
                                // Creamos una lista temporal para usar conectarRuta
                                List<Ciudad> ruta = new ArrayList<>();
                                ruta.add(origen);
                                ruta.add(destino);
                                grafo.conectarRuta(ruta);
                            }
                        } catch (Exception e) {
                            System.err.println("Error al parsear ruta: " + line);
                        }
                    }
                }

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

            return new Ciudad(id, nombre, latitud, longitud);
        } catch (Exception e) {
            System.err.println("Error al parsear ciudad: " + linea);
            return null;
        }
    }
}