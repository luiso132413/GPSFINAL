package simuladorgps.Controlador;

import simuladorgps.Modelo.*;
import simuladorgps.ui.InfoPanel;
import simuladorgps.ui.MapPanel;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GPSController {
    private Grafo grafo = new Grafo();
    private int nextId = 1;
    private Ciudad ciudadSeleccionada = null;
    private Ciudad ciudadOrigenConexion = null;
    private Ciudad ciudadArrastrando = null;
    private boolean modoArrastreActivo = false;
    private boolean modoAccidente = false;

    private MapPanel mapPanel;
    private InfoPanel infoPanel;

    public void setComponents(MapPanel mapPanel, InfoPanel infoPanel) {
        this.mapPanel = mapPanel;
        this.infoPanel = infoPanel;
    }

    // Métodos de dibujo
    public void dibujarTodo(Graphics g, int width, int height) {
        dibujarRutas(g);
        dibujarCiudades(g);
        dibujarConexionPendiente(g);
    }

    private void dibujarCiudades(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Ciudad ciudad : grafo.getCiudades()) {
            int x = ciudad.getXVisual();
            int y = ciudad.getYVisual();

            g2d.setColor(new Color(100, 100, 100, 50));
            g2d.fillOval(x - 9, y - 6, 20, 20);

            // Color del nodo según estado
            if (ciudad.tieneAccidente()) {
                g2d.setColor(Color.RED);
            } else if (ciudad == ciudadSeleccionada) {
                g2d.setColor(new Color(255, 100, 100));
            } else if (ciudad == ciudadArrastrando) {
                g2d.setColor(new Color(255, 200, 0));
            } else {
                g2d.setColor(new Color(70, 130, 180));
            }

            g2d.fillOval(x - 10, y - 10, 20, 20);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - 10, y - 10, 20, 20);

            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(ciudad.getNombre(), x + 15, y + 5);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString("ID: " + ciudad.getId(), x + 15, y + 20);
        }
    }

    private void dibujarRutas(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(100, 100, 100));

        Set<Map.Entry<Ciudad, Ciudad>> rutasDibujadas = new HashSet<>();

        for (Ciudad origen : grafo.getCiudades()) {
            for (Ruta ruta : grafo.getRutasDesde(origen)) {
                Ciudad destino = ruta.getDestino();

                if (rutasDibujadas.contains(new AbstractMap.SimpleEntry<>(destino, origen))) {
                    continue;
                }
                rutasDibujadas.add(new AbstractMap.SimpleEntry<>(origen, destino));

                int x1 = origen.getXVisual();
                int y1 = origen.getYVisual();
                int x2 = destino.getXVisual();
                int y2 = destino.getYVisual();

                g2d.drawLine(x1, y1, x2, y2);

                if (Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2)) > 50) {
                    String distancia = String.format("%.1f km", ruta.getDistancia());
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2d.drawString(distancia, (x1 + x2) / 2, (y1 + y2) / 2);
                    g2d.setColor(new Color(100, 100, 100));
                }
            }
        }
    }

    private void dibujarConexionPendiente(Graphics g) {
        if (ciudadOrigenConexion != null && ciudadSeleccionada != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(50, 200, 50, 150));
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1.0f, new float[]{5, 5}, 0));

            int x1 = ciudadOrigenConexion.getXVisual();
            int y1 = ciudadOrigenConexion.getYVisual();
            int x2 = ciudadSeleccionada.getXVisual();
            int y2 = ciudadSeleccionada.getYVisual();

            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    // Metodos de manejo de eventos
    public void handleMousePress(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            mostrarDialogoNuevaCiudad(e.getX(), e.getY());
            return;
        }

        Ciudad ciudadClicada = obtenerCiudadCercana(e.getX(), e.getY());

        if (modoArrastreActivo && ciudadSeleccionada != null) {
            if (ciudadClicada != null && ciudadClicada.equals(ciudadSeleccionada)) {
                ciudadArrastrando = ciudadClicada;
                mapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
            return;
        }

        if (ciudadClicada != null) {
            ciudadSeleccionada = ciudadClicada;
            infoPanel.agregarMensaje("Ciudad seleccionada: " + ciudadSeleccionada.getNombre());

            if (modoAccidente) {
                boolean estadoActual = ciudadClicada.tieneAccidente();
                ciudadClicada.setAccidente(!estadoActual);

                if (ciudadClicada.tieneAccidente()) {
                    infoPanel.agregarMensaje("⚠️ Accidente marcado en: " + ciudadClicada.getNombre());
                } else {
                    infoPanel.agregarMensaje("✅ Accidente quitado en: " + ciudadClicada.getNombre());
                }

                modoAccidente = false;
                mapPanel.repaint();
                return;
            }

            if (ciudadOrigenConexion != null) {
                manejarConexionCiudades(ciudadClicada);
            }
        }
        mapPanel.repaint();
    }

    private void manejarConexionCiudades(Ciudad ciudadDestino) {
        if (ciudadDestino.equals(ciudadOrigenConexion)) {
            infoPanel.agregarMensaje("No se puede conectar una ciudad consigo misma");
        } else {
            if (!grafo.existeRuta(ciudadOrigenConexion, ciudadDestino)) {
                grafo.conectarCiudad(ciudadOrigenConexion, ciudadDestino);
                actualizarDistanciasRutas(ciudadOrigenConexion);
                infoPanel.agregarMensaje("Ruta creada: " + ciudadOrigenConexion.getNombre() +
                        " → " + ciudadDestino.getNombre());
            } else {
                infoPanel.agregarMensaje("Ya existe una ruta desde " + ciudadOrigenConexion.getNombre() +
                        " hasta " + ciudadDestino.getNombre());
            }
        }
        ciudadOrigenConexion = null;
        mapPanel.setCursor(Cursor.getDefaultCursor());
    }

    public void handleMouseRelease(MouseEvent e) {
        if (ciudadArrastrando != null) {
            ciudadArrastrando.setXVisual(e.getX());
            ciudadArrastrando.setYVisual(e.getY());
            ciudadArrastrando = null;
            mapPanel.setCursor(Cursor.getDefaultCursor());
            mapPanel.repaint();
        }
    }

    public void handleMouseDrag(MouseEvent e) {
        if (ciudadArrastrando != null) {
            ciudadArrastrando.setXVisual(e.getX());
            ciudadArrastrando.setYVisual(e.getY());
            mapPanel.repaint();
        }
    }

    // Metodos de acciones
    public void calcularRutaOptima() {
        if (ciudadSeleccionada == null) {
            JOptionPane.showMessageDialog(mapPanel,
                    "Seleccione una ciudad de origen primero",
                    "Calcular Ruta",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Ciudad> ciudades = grafo.getCiudades();
        ciudades.remove(ciudadSeleccionada);

        if (ciudades.isEmpty()) {
            JOptionPane.showMessageDialog(mapPanel,
                    "No hay ciudades destino disponibles",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Ciudad destino = (Ciudad) JOptionPane.showInputDialog(
                mapPanel,
                "Seleccione ciudad destino:",
                "Calcular Ruta Óptima",
                JOptionPane.QUESTION_MESSAGE,
                null,
                ciudades.toArray(),
                ciudades.get(0));

        if (destino == null) return;

        String horaStr = JOptionPane.showInputDialog(mapPanel,
                "Hora de salida (HH:MM):",
                "12:00");

        if (horaStr == null || horaStr.trim().isEmpty()) return;

        try {
            String[] partes = horaStr.split(":");
            int hora = Integer.parseInt(partes[0]);
            int minuto = partes.length > 1 ? Integer.parseInt(partes[1]) : 0;

            if (hora < 0 || hora > 23 || minuto < 0 || minuto > 59) {
                throw new IllegalArgumentException("Hora inválida");
            }

            String resultado = grafo.dijkstra(ciudadSeleccionada, destino, hora, minuto);

            JTextArea textArea = new JTextArea(resultado, 15, 40);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(mapPanel, scrollPane,
                    "Ruta Óptima: " + ciudadSeleccionada.getNombre() + " → " + destino.getNombre(),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mapPanel,
                    "Formato de hora inválido. Use HH:MM (ej. 08:30)",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void iniciarModoConexion() {
        if (ciudadSeleccionada == null) {
            JOptionPane.showMessageDialog(mapPanel, "Seleccione una ciudad primero",
                    "Conectar Ciudades", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ciudadOrigenConexion = ciudadSeleccionada;
        infoPanel.agregarMensaje("Modo conexión: Seleccione ciudad destino para " +
                ciudadOrigenConexion.getNombre());
        mapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        mapPanel.repaint();
    }

    public void toggleModoArrastre() {
        if (ciudadSeleccionada == null) {
            JOptionPane.showMessageDialog(mapPanel, "Seleccione una ciudad primero",
                    "Modo Arrastre", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modoArrastreActivo = !modoArrastreActivo;
        if (modoArrastreActivo) {
            infoPanel.agregarMensaje("Modo arrastre ACTIVADO para " + ciudadSeleccionada.getNombre());
        } else {
            infoPanel.agregarMensaje("Modo arrastre DESACTIVADO");
        }
    }

    public void guardarDatos() {
        if (grafo.getCiudades().isEmpty()) {
            JOptionPane.showMessageDialog(mapPanel, "No hay datos para guardar",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
        } else {
            Archivo.guardarDatos(grafo);
            infoPanel.agregarMensaje("Datos guardados correctamente");
        }
    }

    public void cargarDatos() {
        Grafo grafoCargado = Archivo.cargarDatos();
        if (grafoCargado != null) {
            this.grafo = grafoCargado;
            this.nextId = calcularNextId();
            ciudadSeleccionada = null;
            ciudadOrigenConexion = null;
            mapPanel.repaint();
            infoPanel.agregarMensaje("Datos cargados correctamente");
        }
    }

    public void activarModoAccidente() {
        modoAccidente = true;
        infoPanel.agregarMensaje("Haz clic en una ciudad para marcarla como con accidente.");
    }

    // Metodos auxiliares
    private Ciudad obtenerCiudadCercana(int x, int y) {
        final int RADIO_DETECCION = 20;

        for (Ciudad ciudad : grafo.getCiudades()) {
            double distancia = Math.sqrt(Math.pow(ciudad.getXVisual() - x, 2) +
                    Math.pow(ciudad.getYVisual() - y, 2));
            if (distancia <= RADIO_DETECCION) {
                return ciudad;
            }
        }
        return null;
    }

    private void mostrarDialogoNuevaCiudad(int x, int y) {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField nombreField = new JTextField();
        JTextField latitudField = new JTextField();
        JTextField longitudField = new JTextField();

        panel.add(new JLabel("Nombre:"));
        panel.add(nombreField);
        panel.add(new JLabel("Latitud:"));
        panel.add(latitudField);
        panel.add(new JLabel("Longitud:"));
        panel.add(longitudField);

        int result = JOptionPane.showConfirmDialog(mapPanel, panel, "Agregar Nueva Ciudad",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = nombreField.getText().trim();
                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(mapPanel, "El nombre no puede estar vacío.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double lat = Double.parseDouble(latitudField.getText().trim());
                double lon = Double.parseDouble(longitudField.getText().trim());

                if (lat < -90 || lat > 90) {
                    JOptionPane.showMessageDialog(mapPanel,
                            "La latitud debe estar entre -90 y 90 grados.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (lon < -180 || lon > 180) {
                    JOptionPane.showMessageDialog(mapPanel,
                            "La longitud debe estar entre -180 y 180 grados.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Ciudad nuevaCiudad = new Ciudad(nextId++, nombre, lat, lon);
                nuevaCiudad.setXVisual(x);
                nuevaCiudad.setYVisual(y);
                grafo.agregarCiudad(nuevaCiudad);
                mapPanel.repaint();
                infoPanel.agregarMensaje("Ciudad agregada: " + nombre);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mapPanel,
                        "Latitud y longitud deben ser valores numéricos válidos.\n" +
                                "Ejemplos:\n" +
                                "Latitud: -34.603722\n" +
                                "Longitud: -58.381592",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actualizarDistanciasRutas(Ciudad ciudad) {
        for (Ruta ruta : grafo.getRutasDesde(ciudad)) {
            double nuevaDistancia = Funciones.recorrido(
                    ciudad.getLatitud(), ciudad.getLongitud(),
                    ruta.getDestino().getLatitud(), ruta.getDestino().getLongitud());
            ruta.setDistancia(nuevaDistancia);
            ruta.setTiempo(nuevaDistancia / 60);
        }

        for (Ciudad origen : grafo.getCiudades()) {
            for (Ruta ruta : grafo.getRutasDesde(origen)) {
                if (ruta.getDestino().equals(ciudad)) {
                    double nuevaDistancia = Funciones.recorrido(
                            origen.getLatitud(), origen.getLongitud(),
                            ciudad.getLatitud(), ciudad.getLongitud());
                    ruta.setDistancia(nuevaDistancia);
                    ruta.setTiempo(nuevaDistancia / 60);
                }
            }
        }
    }

    private int calcularNextId() {
        return grafo.getCiudades().stream()
                .mapToInt(Ciudad::getId)
                .max()
                .orElse(0) + 1;
    }
}