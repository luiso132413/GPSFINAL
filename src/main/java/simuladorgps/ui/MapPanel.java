package simuladorgps.ui;

import simuladorgps.Controlador.GPSController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class MapPanel extends JPanel {
    private GPSController controller;

    public MapPanel(GPSController controller) {
        this.controller = controller;
        setBackground(new Color(240, 240, 240));
        setupMouseListeners();
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        controller.dibujarTodo(g, getWidth(), getHeight());
    }
}
