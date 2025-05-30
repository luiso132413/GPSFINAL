package simuladorgps;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.border.EmptyBorder;

public class GPSPanel extends JFrame {
    private Grafo grafo = new Grafo();
    private int nextId = 1;
    private JPanel mapPanel;
    private JTextArea infoArea;
    private Ciudad ciudadSeleccionada = null;
    private Ciudad ciudadOrigenConexion = null;
    private Ciudad ciudadArrastrando = null;
    private boolean modoArrastreActivo = false;

    public GPSPanel() {
        setTitle("Simulador GPS con Nodos Movibles");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());

        // Panel superior con botones
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton conectarBtn = new JButton("Conectar Ciudades");
        JButton modoArrastreBtn = new JButton("Modo Arrastre");
        JButton guardarBtn = new JButton("Guardar");
        JButton cargarBtn = new JButton("Cargar");
        JButton calcularRutaBtn = new JButton("Calcular Ruta Óptima");


        controlPanel.add(calcularRutaBtn);
        controlPanel.add(conectarBtn);
        controlPanel.add(modoArrastreBtn);
        controlPanel.add(guardarBtn);
        controlPanel.add(cargarBtn);

        add(controlPanel, BorderLayout.NORTH);

        // Panel central para el mapa
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarCiudades(g);
                dibujarRutas(g);
                dibujarConexionPendiente(g);
            }
        };
        mapPanel.setBackground(new Color(240, 240, 240));

        // Configurar eventos del mouse
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
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
                    infoArea.append("Ciudad seleccionada: " + ciudadSeleccionada.getNombre() + "\n");

                    if (ciudadOrigenConexion != null) {
                        if (ciudadClicada.equals(ciudadOrigenConexion)) {
                            infoArea.append("No se puede conectar una ciudad consigo misma\n");
                        } else {
                            if (!grafo.existeRuta(ciudadOrigenConexion, ciudadClicada)) {
                                grafo.conectarCiudad(ciudadOrigenConexion, ciudadClicada);
                                actualizarDistanciasRutas(ciudadOrigenConexion);
                                infoArea.append("Ruta creada: " + ciudadOrigenConexion.getNombre() +
                                        " → " + ciudadClicada.getNombre() + "\n");
                            } else {
                                infoArea.append("Ya existe una ruta desde " + ciudadOrigenConexion.getNombre() +
                                        " hasta " + ciudadClicada.getNombre() + "\n");
                            }
                        }
                        ciudadOrigenConexion = null;
                        mapPanel.setCursor(Cursor.getDefaultCursor());
                    }
                }
                mapPanel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (ciudadArrastrando != null) {
                    ciudadArrastrando.setXVisual(e.getX());
                    ciudadArrastrando.setYVisual(e.getY());
                    ciudadArrastrando = null;
                    mapPanel.setCursor(Cursor.getDefaultCursor());
                    mapPanel.repaint();
                }
            }
        });
        add(new JScrollPane(mapPanel), BorderLayout.CENTER);

        // Panel inferior para información
        infoArea = new JTextArea(5, 80);
        infoArea.setEditable(false);
        infoArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(new JScrollPane(infoArea), BorderLayout.SOUTH);

        // Configurar acciones de los botones
        conectarBtn.addActionListener(e -> {
            if (ciudadSeleccionada == null) {
                JOptionPane.showMessageDialog(this, "Seleccione una ciudad primero",
                        "Conectar Ciudades", JOptionPane.WARNING_MESSAGE);
            } else {
                ciudadOrigenConexion = ciudadSeleccionada;
                infoArea.append("Modo conexión: Seleccione ciudad destino para " +
                        ciudadOrigenConexion.getNombre() + "\n");
                mapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                mapPanel.repaint();
            }
        });

        modoArrastreBtn.addActionListener(e -> {
            toggleModoArrastre();  // Agregar paréntesis y punto y coma
        });

        guardarBtn.addActionListener(e -> {
            if (grafo.getCiudades().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay datos para guardar", "Advertencia", JOptionPane.WARNING_MESSAGE);
            } else {
                Archivo.guardarDatos(grafo);
            }
        });

        cargarBtn.addActionListener(e -> {
            Grafo grafoCargado = Archivo.cargarDatos();
            if (grafoCargado != null) {
                this.grafo = grafoCargado;
                this.nextId = calcularNextId();
                ciudadSeleccionada = null;
                ciudadOrigenConexion = null;
                mapPanel.repaint();
                infoArea.append("Datos cargados correctamente\n");
            }
        });

        calcularRutaBtn.addActionListener(e -> {
            if (ciudadSeleccionada == null) {
                JOptionPane.showMessageDialog(this,
                        "Seleccione una ciudad de origen primero",
                        "Calcular Ruta",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Mostrar diálogo para seleccionar ciudad destino
            List<Ciudad> ciudades = grafo.getCiudades();
            ciudades.remove(ciudadSeleccionada); // Quitamos la ciudad origen

            Ciudad destino = (Ciudad) JOptionPane.showInputDialog(
                    this,
                    "Seleccione ciudad destino:",
                    "Calcular Ruta Óptima",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    ciudades.toArray(),
                    ciudades.isEmpty() ? null : ciudades.get(0));

            if (destino == null) return; // El usuario canceló

            // Mostrar diálogo para hora de salida
            String horaStr = JOptionPane.showInputDialog(this,
                    "Hora de salida (HH:MM):",
                    "12:00");

            if (horaStr == null || horaStr.trim().isEmpty()) return;

            try {
                String[] partes = horaStr.split(":");
                int hora = Integer.parseInt(partes[0]);
                int minuto = partes.length > 1 ? Integer.parseInt(partes[1]) : 0;

                // Validar hora
                if (hora < 0 || hora > 23 || minuto < 0 || minuto > 59) {
                    throw new IllegalArgumentException("Hora inválida");
                }

                // Calcular ruta óptima
                String resultado = grafo.dijkstra(ciudadSeleccionada, destino, hora, minuto);

                // Mostrar resultados
                JTextArea textArea = new JTextArea(resultado, 15, 40);
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);

                JScrollPane scrollPane = new JScrollPane(textArea);
                JOptionPane.showMessageDialog(this, scrollPane,
                        "Ruta Óptima: " + ciudadSeleccionada.getNombre() + " → " + destino.getNombre(),
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Formato de hora inválido. Use HH:MM (ej. 08:30)",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }

    private int calcularNextId() {
        return grafo.getCiudades().stream()
                .mapToInt(Ciudad::getId)
                .max()
                .orElse(0) + 1;
    }

    private Ciudad obtenerCiudadCercana(int x, int y) {
        final int RADIO_DETECCION = 20;

        for (Ciudad ciudad : grafo.getCiudades()) {
            int ciudadX = ciudad.getXVisual() != 0 ? ciudad.getXVisual() : convertirLongitudAX(ciudad.getLongitud());
            int ciudadY = ciudad.getYVisual() != 0 ? ciudad.getYVisual() : convertirLatitudAY(ciudad.getLatitud());

            double distancia = Math.sqrt(Math.pow(ciudadX - x, 2) + Math.pow(ciudadY - y, 2));
            if (distancia <= RADIO_DETECCION) {
                return ciudad;
            }
        }
        return null;
    }

    private void dibujarCiudades(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Ciudad ciudad : grafo.getCiudades()) {
            int x, y;

            if (ciudad.getXVisual() != 0 && ciudad.getYVisual() != 0) {
                x = ciudad.getXVisual();
                y = ciudad.getYVisual();
            } else {
                x = convertirLongitudAX(ciudad.getLongitud());
                y = convertirLatitudAY(ciudad.getLatitud());
                ciudad.setXVisual(x);
                ciudad.setYVisual(y);
            }

            g2d.setColor(new Color(100, 100, 100, 50));
            g2d.fillOval(x - 9, y - 6, 20, 20);

            if (ciudad == ciudadSeleccionada) {
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

        // Usar un Set para evitar dibujar rutas duplicadas
        Set<Map.Entry<Ciudad, Ciudad>> rutasDibujadas = new HashSet<>();

        for (Ciudad origen : grafo.getCiudades()) {
            for (Ruta ruta : grafo.getRutasDesde(origen)) {
                Ciudad destino = ruta.getDestino();

                // Verificar si ya dibujamos esta conexión
                if (rutasDibujadas.contains(new AbstractMap.SimpleEntry<>(destino, origen))) {
                    continue;
                }
                rutasDibujadas.add(new AbstractMap.SimpleEntry<>(origen, destino));

                // Usar coordenadas visuales si existen
                int x1 = origen.getXVisual() != 0 ? origen.getXVisual() : convertirLongitudAX(origen.getLongitud());
                int y1 = origen.getYVisual() != 0 ? origen.getYVisual() : convertirLatitudAY(origen.getLatitud());
                int x2 = destino.getXVisual() != 0 ? destino.getXVisual() : convertirLongitudAX(destino.getLongitud());
                int y2 = destino.getYVisual() != 0 ? destino.getYVisual() : convertirLatitudAY(destino.getLatitud());

                g2d.drawLine(x1, y1, x2, y2);

                // Dibujar distancia solo si hay suficiente espacio
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

            int x1 = convertirLongitudAX(ciudadOrigenConexion.getLongitud());
            int y1 = convertirLatitudAY(ciudadOrigenConexion.getLatitud());
            int x2 = convertirLongitudAX(ciudadSeleccionada.getLongitud());
            int y2 = convertirLatitudAY(ciudadSeleccionada.getLatitud());

            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    private int convertirLongitudAX(double longitud) {
        return (int) ((longitud + 180) * (mapPanel.getWidth() / 360.0));
    }

    private int convertirLatitudAY(double latitud) {
        return (int) ((90 - latitud) * (mapPanel.getHeight() / 180.0));
    }

    private void toggleModoArrastre() {
        if (ciudadSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una ciudad primero",
                    "Modo Arrastre", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modoArrastreActivo = !modoArrastreActivo;
        if (modoArrastreActivo) {
            infoArea.append("Modo arrastre ACTIVADO para " + ciudadSeleccionada.getNombre() + "\n");
        } else {
            infoArea.append("Modo arrastre DESACTIVADO\n");
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

    private void mostrarDialogoNuevaCiudad(int x, int y) {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField nombreField = new JTextField();
        JTextField latitudField = new JTextField();
        JTextField longitudField = new JTextField();

        latitudField.setToolTipText("Ejemplo: -34.603722 para Buenos Aires");
        longitudField.setToolTipText("Ejemplo: -58.381592 para Buenos Aires");

        panel.add(new JLabel("Nombre:"));
        panel.add(nombreField);
        panel.add(new JLabel("Latitud:"));
        panel.add(latitudField);
        panel.add(new JLabel("Longitud:"));
        panel.add(longitudField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Agregar Nueva Ciudad",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = nombreField.getText().trim();
                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double lat = Double.parseDouble(latitudField.getText().trim());
                double lon = Double.parseDouble(longitudField.getText().trim());

                if (lat < -90 || lat > 90) {
                    JOptionPane.showMessageDialog(this, "La latitud debe estar entre -90 y 90 grados.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (lon < -180 || lon > 180) {
                    JOptionPane.showMessageDialog(this, "La longitud debe estar entre -180 y 180 grados.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Ciudad nuevaCiudad = new Ciudad(nextId++, nombre, lat, lon);
                grafo.agregarCiudad(nuevaCiudad);
                mapPanel.repaint();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Latitud y longitud deben ser valores numéricos válidos.\n" +
                                "Ejemplos:\n" +
                                "Latitud: -34.603722\n" +
                                "Longitud: -58.381592",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GPSPanel panel = new GPSPanel();
            panel.setLocationRelativeTo(null);
        });
    }
}