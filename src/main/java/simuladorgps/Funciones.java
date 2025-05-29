package simuladorgps;

import simuladorgps.Ciudad;
import simuladorgps.Ruta;

public class Funciones {

    public static double recorrido(double lat1, double lon1, double lat2, double lon2) {
        final int RTierra = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                + Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RTierra * c;
    }

    public static double velocidadPorHora(int hora, int minuto) {
        int totalMin = hora * 60 + minuto;
        if (totalMin >= 301 && totalMin <= 540) return 45;     // 05:01–09:00
        if (totalMin >= 541 && totalMin <= 750) return 60;     // 09:01–12:30
        if (totalMin >= 751 && totalMin <= 840) return 45;     // 12:31–14:00
        if (totalMin >= 841 && totalMin <= 1020) return 65;    // 14:01–17:00
        if (totalMin >= 1021 && totalMin <= 1140) return 35;   // 17:01–19:00
        if (totalMin >= 1141 && totalMin <= 1380) return 75;   // 19:01–23:00
        return 90; // 23:01–05:00
    }

    public static double calcularTiempo(double distanciaKm, int hora, int minuto) {
        double velocidad = velocidadPorHora(hora, minuto);
        return (distanciaKm / velocidad) * 60; // devuelve minutos
    }
}
