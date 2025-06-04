package simuladorgps.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class InfoPanel extends JTextArea {

    // Constructor, aquí se configura el área de texto donde se va a mostrar la info
    public InfoPanel() {
        super(5, 80);
        setEditable(false);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setFont(new Font("Monospaced", Font.PLAIN, 12)); // Usamos fuente monoespaciada para que este ordenado
    }

    // Método para ir agregando mensajitos al panel
    public void agregarMensaje(String mensaje) {
        append(mensaje + "\n");
        setCaretPosition(getDocument().getLength());
    }

}
