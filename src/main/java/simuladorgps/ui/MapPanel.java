package simuladorgps.ui;

import simuladorgps.Controlador.GPSController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class MapPanel extends JPanel {
    private GPSController controller;

    // Constructor del panel, aquí se recibe el controlador para manejar todo
    public MapPanel(GPSController controller) {
        this.controller = controller;
        setBackground(new Color(240, 240, 240));
        setupMouseListeners(); // Configuramos los eventos del mouse
    }

      private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                controller.handleMousePress(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                controller.handleMouseRelease(e);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                controller.handleMouseDrag(e);
            }
        });
    }

    // Este metodo pinta todo en el panel
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        controller.dibujarTodo(g, getWidth(), getHeight()); // Le decimos al controlador que pinte todo
    }
}
