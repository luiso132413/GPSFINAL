package simuladorgps;
import java.io.Serializable;

public class Ruta implements Serializable {
    private static final long serialVersionUID = 1L;
    private Ciudad origen;
    private Ciudad destino;
    private double distancia;
    private double tiempo;

    public Ruta(Ciudad origen, Ciudad destino, double distancia, double tiempo) {
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
        this.tiempo = tiempo;
    }

    public Ciudad getOrigen() {
        return origen;
    }

    public Ciudad getDestino() {
        return destino;
    }

    public double getDistancia() {
        return distancia;
    }

    public double getTiempo() {
        return tiempo;
    }

    public void setOrigen(Ciudad origen) {
        this.origen = origen;
    }

    public void setDestino(Ciudad destino) {
        this.destino = destino;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public void setTiempo(double tiempo) {
        this.tiempo = tiempo;
    }
    @Override
    public String toString() {
        return String.format("%s -> %s (%.2f km, %.2f min)",
                origen.getNombre(),
                destino.getNombre(),
                distancia,
                tiempo);
    }
}