package simuladorgps;

public class Ruta {
    private Ciudad origen;
    private Ciudad destino;
    private double distancia;

    public Ruta(Ciudad origen, Ciudad destino, double distancia) {
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
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
}