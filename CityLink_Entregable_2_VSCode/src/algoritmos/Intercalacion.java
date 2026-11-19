package algoritmos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import modelo.Pedido;

public final class Intercalacion {
    private Intercalacion() {}
    public static List<Pedido> intercalar(List<Pedido> primera, List<Pedido> segunda, Comparator<Pedido> criterio) {
        List<Pedido> resultado = new ArrayList<>();
        int i = 0, j = 0;
        while (i < primera.size() && j < segunda.size()) {
            if (criterio.compare(primera.get(i), segunda.get(j)) <= 0) resultado.add(primera.get(i++));
            else resultado.add(segunda.get(j++));
        }
        while (i < primera.size()) resultado.add(primera.get(i++));
        while (j < segunda.size()) resultado.add(segunda.get(j++));
        return resultado;
    }
}
