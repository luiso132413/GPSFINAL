package simuladorgps.ui;

import simuladorgps.Controlador.GPSController;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class ControlPanel extends JPanel {
    public ControlPanel(GPSController controller) {
        // Usar BoxLayout vertical para alinear los botones
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10)); // Márgenes
        setBackground(new Color(240, 240, 240)); // Fondo claro para distinguirlo

        // Añadir título (opcional)
        JLabel title = new JLabel("Simulador GPS");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);
        add(Box.createRigidArea(new Dimension(0, 10))); // Espacio después del título

        // Añadir botones con alineación izquierda y tamaño consistente
        addButton("Calcular Ruta Óptima", e -> controller.calcularRutaOptima());
        addButton("Conectar Ciudades", e -> controller.iniciarModoConexion());
        addButton("Modo Arrastre", e -> controller.toggleModoArrastre());
        addButton("Guardar", e -> controller.guardarDatos());
        addButton("Cargar", e -> controller.cargarDatos());
        addButton("Accidente", e -> controller.activarModoAccidente());

        // Espacio flexible para empujar los botones hacia arriba
        add(Box.createVerticalGlue());
    }

    private void addButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setAlignmentX(Component.LEFT_ALIGNMENT); // Alinear a la izquierda
        button.setMaximumSize(new Dimension(200, 30)); // Tamaño fijo para todos los botones
        button.setPreferredSize(new Dimension(200, 30));
        add(button);
        add(Box.createRigidArea(new Dimension(0, 5))); // Espacio entre botones
    }
}