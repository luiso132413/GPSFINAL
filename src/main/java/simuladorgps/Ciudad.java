package simuladorgps;
import java.io.Serializable;

public class Ciudad implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String nombre;
    private double latitud;
    private double longitud;

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

    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    // En tu clase Ciudad
    @Override
    public String toString() {
        return String.format("%s (ID: %d, Lat: %.6f, Long: %.6f)",
                nombre, id, latitud, longitud);
    }
}
