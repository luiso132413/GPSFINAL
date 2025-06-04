package simuladorgps.ui;

import simuladorgps.Controlador.GPSController;
import javax.swing.*;
import java.awt.*;

public class GPSMainFrame extends JFrame {
    private MapPanel mapPanel;
    private ControlPanel controlPanel;
    private InfoPanel infoPanel;
    private GPSController controller;

    public GPSMainFrame() {
        setTitle("Simulador GPS con Nodos Movibles"); // Título del frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout(5, 5));

        controller = new GPSController(); // Creamos el controlador que manda la parada

        // Creamos los paneles
        mapPanel = new MapPanel(controller);
        controlPanel = new ControlPanel(controller);
        infoPanel = new InfoPanel();

        controller.setComponents(mapPanel, infoPanel);

        JScrollPane mapScrollPane = new JScrollPane(mapPanel);
        mapScrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(controlPanel, BorderLayout.NORTH);
        leftPanel.setPreferredSize(new Dimension(200, getHeight()));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane infoScrollPane = new JScrollPane(infoPanel);
        infoScrollPane.setPreferredSize(new Dimension(250, getHeight()));
        infoScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(leftPanel, BorderLayout.WEST);
        add(mapScrollPane, BorderLayout.CENTER);
        add(infoScrollPane, BorderLayout.EAST);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GPSMainFrame());
    }
}
