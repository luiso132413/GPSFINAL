package simuladorgps.ui;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class InfoPanel extends JTextArea {
    public InfoPanel() {
        super(5, 80);
        setEditable(false);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setFont(new Font("Monospaced", Font.PLAIN, 12));
    }

    public void agregarMensaje(String mensaje) {
        append(mensaje + "\n");
        setCaretPosition(getDocument().getLength());
    }

    public void limpiar() {
        setText("");
    }
}
