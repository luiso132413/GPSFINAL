package simuladorgps;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.EmptyBorder;

public class GPSPanel extends JFrame {
    private Grafo grafo = new Grafo();
    private int nextId = 1;
    private JPanel mapPanel;
    private JTextArea infoArea;
    private Ciudad ciudadSeleccionada = null;
    private Ciudad ciudadOrigenConexion = null;
    private Ciudad ciudadArrastrando = null;
    private Point puntoArrastre = null;

    public GPSPanel() {
        setTitle("Simulador GPS con Nodos Movibles");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());

        // Panel superior con botones
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton conectarBtn = new JButton("Conectar Ciudades");
        JButton modoArrastreBtn = new JButton("Modo Arrastre");

        controlPanel.add(conectarBtn);
        controlPanel.add(modoArrastreBtn);

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
                } else {
                    manejarClicCiudad(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (ciudadArrastrando != null) {
                    ciudadArrastrando = null;
                    puntoArrastre = null;
                    mapPanel.setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        mapPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (ciudadArrastrando != null && puntoArrastre != null) {
                    double nuevaLongitud = (e.getX() * 360.0 / mapPanel.getWidth()) - 180;
                    double nuevaLatitud = 90 - (e.getY() * 180.0 / mapPanel.getHeight());

                    ciudadArrastrando.setLatitud(nuevaLatitud);
                    ciudadArrastrando.setLongitud(nuevaLongitud);
                    puntoArrastre = e.getPoint();
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
        conectarBtn.addActionListener(e -> iniciarModoConexion());
        modoArrastreBtn.addActionListener(e -> toggleModoArrastre());

        setVisible(true);
    }

    private void dibujarCiudades(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Ciudad ciudad : grafo.getCiudades()) {
            int x = convertirLongitudAX(ciudad.getLongitud());
            int y = convertirLatitudAY(ciudad.getLatitud());

            // Dibujar sombra
            g2d.setColor(new Color(100, 100, 100, 50));
            g2d.fillOval(x - 9, y - 6, 20, 20);

            // Dibujar círculo de la ciudad
            if (ciudad == ciudadSeleccionada) {
                g2d.setColor(new Color(255, 100, 100));
            } else if (ciudad == ciudadArrastrando) {
                g2d.setColor(new Color(255, 200, 0));
            } else {
                g2d.setColor(new Color(70, 130, 180));
            }
            g2d.fillOval(x - 10, y - 10, 20, 20);

            // Dibujar borde
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - 10, y - 10, 20, 20);

            // Dibujar nombre e ID
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(ciudad.getNombre(), x + 15, y + 5);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString("ID: " + ciudad.getId(), x + 15, y + 20);
        }
    }

    private void dibujarRutas(Graphics g) {
        g.setColor(new Color(100, 100, 100));
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));

        for (Ciudad origen : grafo.getCiudades()) {
            for (Ruta ruta : grafo.getRutasDesde(origen)) {
                Ciudad destino = ruta.getDestino();
                int x1 = convertirLongitudAX(origen.getLongitud());
                int y1 = convertirLatitudAY(origen.getLatitud());
                int x2 = convertirLongitudAX(destino.getLongitud());
                int y2 = convertirLatitudAY(destino.getLatitud());

                g2d.drawLine(x1, y1, x2, y2);

                String distancia = String.format("%.1f km", ruta.getDistancia());
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.PLAIN, 10));
                g.drawString(distancia, (x1 + x2) / 2, (y1 + y2) / 2);
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
            JOptionPane.showMessageDialog(this, "Seleccione una ciudad primero", "Modo Arrastre",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (ciudadArrastrando == null) {
            ciudadArrastrando = ciudadSeleccionada;
            puntoArrastre = new Point(
                    convertirLongitudAX(ciudadArrastrando.getLongitud()),
                    convertirLatitudAY(ciudadArrastrando.getLatitud())
            );
            mapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            infoArea.append("Modo arrastre: Mueva la ciudad " + ciudadArrastrando.getNombre() + "\n");
        } else {
            ciudadArrastrando = null;
            puntoArrastre = null;
            mapPanel.setCursor(Cursor.getDefaultCursor());
            infoArea.append("Modo arrastre desactivado\n");
        }
    }

    private void mostrarDialogoNuevaCiudad(int x, int y) {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Conversión de coordenadas del panel a lat/lon (aproximación visual)
        double latitud = 90 - (y * 180.0 / mapPanel.getHeight());
        double longitud = (x * 360.0 / mapPanel.getWidth()) - 180;

        JTextField nombreField = new JTextField();
        JTextField latitudField = new JTextField(String.format("%.6f", latitud));
        JTextField longitudField = new JTextField(String.format("%.6f", longitud));

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

                Ciudad nuevaCiudad = new Ciudad(nextId++, nombre, lat, lon);
                grafo.agregarCiudad(nuevaCiudad);
                mapPanel.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Latitud y longitud deben ser valores numéricos válidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void manejarClicCiudad(int x, int y) {
        Ciudad ciudadClicada = null;
        for (Ciudad ciudad : grafo.getCiudades()) {
            int cx = convertirLongitudAX(ciudad.getLongitud());
            int cy = convertirLatitudAY(ciudad.getLatitud());

            if (Math.sqrt(Math.pow(x - cx, 2) + Math    .pow(y - cy, 2)) <= 15) {
                ciudadClicada = ciudad;
                break;
            }
        }

        if (ciudadArrastrando != null) return;

        if (ciudadOrigenConexion != null) {
            if (ciudadClicada != null && !ciudadClicada.equals(ciudadOrigenConexion)) {
                grafo.conectarCiudad(ciudadOrigenConexion, ciudadClicada);
                infoArea.append("Ruta creada: " + ciudadOrigenConexion.getNombre() +
                        " ↔ " + ciudadClicada.getNombre() + "\n");
            }
            ciudadOrigenConexion = null;
        } else if (ciudadClicada != null) {
            ciudadSeleccionada = ciudadClicada;
            infoArea.append("Ciudad seleccionada: " + ciudadSeleccionada.getNombre() + "\n");
        } else {
            ciudadSeleccionada = null;
        }

        mapPanel.repaint();
    }

    private void iniciarModoConexion() {
        if (ciudadSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una ciudad primero", "Conectar Ciudades",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ciudadOrigenConexion = ciudadSeleccionada;
        infoArea.append("Modo conexión: Seleccione ciudad destino para " +
                ciudadOrigenConexion.getNombre() + "\n");
        mapPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GPSPanel panel = new GPSPanel();
            panel.setLocationRelativeTo(null);
        });
    }
}