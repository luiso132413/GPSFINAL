package simuladorgps;

import java.io.Serializable;

public class Ciudad implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private double latitud;
    private double longitud;

    // Coordenadas visuales
    private int xVisual;
    private int yVisual;

    public Ciudad(int id, String nombre, double latitud, double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public int getXVisual() {
        return xVisual;
    }

    public int getYVisual() {
        return yVisual;
    }

    public void setXVisual(int xVisual) {
        this.xVisual = xVisual;
    }

    public void setYVisual(int yVisual) {
        this.yVisual = yVisual;
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %d, Lat: %.6f, Long: %.6f)",
                nombre, id, latitud, longitud);
    }
}
