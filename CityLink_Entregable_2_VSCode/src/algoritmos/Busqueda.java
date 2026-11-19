package algoritmos;

import java.util.List;
import modelo.Cliente;
import modelo.Pedido;

public final class Busqueda {
    private Busqueda() {}
    public static Pedido linealPedido(List<Pedido> pedidos, int codigo) {
        for (Pedido pedido : pedidos) if (pedido.getCodigo() == codigo) return pedido;
        return null;
    }
    public static Cliente linealClienteNombre(Iterable<Cliente> clientes, String nombre) {
        if (nombre == null) return null;
        for (Cliente cliente : clientes)
            if (cliente.getNombre().equalsIgnoreCase(nombre.trim())) return cliente;
        return null;
    }
    public static Pedido binariaPedido(List<Pedido> ordenadosPorCodigo, int codigo) {
        int izquierda = 0;
        int derecha = ordenadosPorCodigo.size() - 1;
        while (izquierda <= derecha) {
            int medio = izquierda + (derecha - izquierda) / 2;
            Pedido actual = ordenadosPorCodigo.get(medio);
            if (actual.getCodigo() == codigo) return actual;
            if (actual.getCodigo() < codigo) izquierda = medio + 1;
            else derecha = medio - 1;
        }
        return null;
    }
}
