package algoritmos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import modelo.Pedido;

public final class Ordenamiento {
    private Ordenamiento() {}
    public static List<Pedido> burbuja(List<Pedido> origen, Comparator<Pedido> criterio) {
        List<Pedido> copia = new ArrayList<>(origen);
        for (int i = 0; i < copia.size() - 1; i++) {
            boolean intercambio = false;
            for (int j = 0; j < copia.size() - i - 1; j++) {
                if (criterio.compare(copia.get(j), copia.get(j + 1)) > 0) {
                    Pedido temporal = copia.get(j);
                    copia.set(j, copia.get(j + 1));
                    copia.set(j + 1, temporal);
                    intercambio = true;
                }
            }
            if (!intercambio) break;
        }
        return copia;
    }
    public static List<Pedido> insercion(List<Pedido> origen, Comparator<Pedido> criterio) {
        List<Pedido> copia = new ArrayList<>(origen);
        for (int i = 1; i < copia.size(); i++) {
            Pedido actual = copia.get(i);
            int j = i - 1;
            while (j >= 0 && criterio.compare(copia.get(j), actual) > 0) {
                copia.set(j + 1, copia.get(j));
                j--;
            }
            copia.set(j + 1, actual);
        }
        return copia;
    }
    public static Comparator<Pedido> criterio(String campo) {
        switch (campo.toLowerCase()) {
            case "codigo": return Comparator.comparingInt(Pedido::getCodigo);
            case "distancia": return Comparator.comparingDouble(Pedido::getDistanciaKm);
            case "fecha": return Comparator.comparing(Pedido::getFechaHora);
            case "costo": return Comparator.comparingDouble(p -> {
                if (p.getVehiculo() != null) return p.calcularImporte();
                throw new IllegalStateException("No se puede ordenar por costo: hay pedidos sin vehiculo");
            });
            default: throw new IllegalArgumentException("Criterio invalido: " + campo);
        }
    }
}
