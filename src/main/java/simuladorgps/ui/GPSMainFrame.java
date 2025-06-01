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
        setTitle("Simulador GPS con Nodos Movibles");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());

        controller = new GPSController();

        // Crear componentes
        mapPanel = new MapPanel(controller);
        controlPanel = new ControlPanel(controller);
        infoPanel = new InfoPanel();

        // Configurar relaciones
        controller.setComponents(mapPanel, infoPanel);

        // Agregar componentes al frame
        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(mapPanel), BorderLayout.CENTER);
        add(new JScrollPane(infoPanel), BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GPSMainFrame());
    }
}
