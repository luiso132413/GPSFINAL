package simuladorgps.ui;

import simuladorgps.Controlador.GPSController;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class ControlPanel extends JPanel {
    public ControlPanel(GPSController controller) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        setBorder(new EmptyBorder(5, 5, 5, 5));

        addButton("Calcular Ruta Óptima", e -> controller.calcularRutaOptima());
        addButton("Conectar Ciudades", e -> controller.iniciarModoConexion());
        addButton("Modo Arrastre", e -> controller.toggleModoArrastre());
        addButton("Guardar", e -> controller.guardarDatos());
        addButton("Cargar", e -> controller.cargarDatos());
        addButton("Accidente", e -> controller.activarModoAccidente());
    }

    private void addButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        add(button);
    }
}