package pruebas;

import app.CityLink;
import app.Main;
import algoritmos.*;
import excepciones.*;
import java.util.*;
import modelo.*;

public class PruebasCityLink {
    private static int ok = 0;
    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
        ok++;
    }
    private static void falla(Runnable bloque, String mensaje) {
        try { bloque.run(); throw new AssertionError(mensaje); }
        catch (AssertionError e) { throw e; }
        catch (RuntimeException e) { ok++; }
    }
    public static void main(String[] args) throws Exception {
        CityLink s = new CityLink();
        Main.cargarDatos(s);
        comprobar(s.getCantidadClientes() == 5, "5 clientes");
        comprobar(s.getCantidadRepartidores() == 3, "3 repartidores");
        comprobar(s.getCantidadPedidos() == 8, "8 pedidos");
        comprobar(s.getPendientesUrgentes() == 3 && s.getPendientesNormales() == 5, "2 colas");
        comprobar(s.consultarSiguiente().getCodigo() == 2, "urgente primero");
        comprobar(s.listarClientes().get(0).getCodigo() == 101, "TreeMap ordenado");
        comprobar(s.buscarCliente("fede").getCodigo() == 101, "busqueda lineal de cliente");
        comprobar(s.buscarPedido(8) == s.buscarPedidoBinaria(8), "busqueda binaria");
        comprobar(Ordenamiento.burbuja(s.listarPedidos(), Ordenamiento.criterio("distancia")).get(0).getCodigo() == 1, "burbuja distancia");
        comprobar(Ordenamiento.insercion(s.listarPedidos(), Ordenamiento.criterio("codigo")).get(0).getCodigo() == 1, "insercion codigo");
        List<Pedido> fusion = s.intercalarTurnos();
        comprobar(fusion.size() == 8 && fusion.get(0).getCodigo() == 1 && fusion.get(7).getCodigo() == 8, "intercalacion ordenada");
        falla(() -> s.buscarPedidoBinaria(987), "inexistente");
        comprobar(s.ordenarPedidos("costo", "burbuja").size() == 8, "ordenamiento costo estimado");
        Repartidor r = s.atenderSiguientePedido();
        comprobar(r.getLegajo() == 1 && s.buscarPedido(2).getEstado().equals("EN_CAMINO"), "asignacion automatica");
        comprobar(s.getPendientesUrgentes() == 2, "sale de cola urgente");
        comprobar(Math.abs(s.buscarPedido(2).calcularImporte() - 2250) < 0.001, "costo urgente real");
        s.registrarEntrega(2);
        comprobar(s.buscarPedido(2).getEstado().equals("ENTREGADO"), "entrega");
        comprobar(r.getHistorialEntregas().size() == 1, "historial entrega");
        comprobar(r.calcularImporte() == 225, "comision");
        s.deshacer();
        comprobar(s.buscarPedido(2).getEstado().equals("EN_CAMINO") && r.getHistorialEntregas().isEmpty(), "undo entrega");
        s.deshacer();
        comprobar(s.buscarPedido(2).getEstado().equals("PENDIENTE") && s.getPendientesUrgentes() == 3 && s.consultarSiguiente().getCodigo() == 2, "undo asignacion");
        s.asignarPedido(8, 2);
        comprobar(s.getPendientesUrgentes() == 2, "asignacion manual pedido al final");
        s.deshacer();
        comprobar(s.getPendientesUrgentes() == 3, "undo asignacion manual");
        comprobar(s.consultarSiguiente().getCodigo() == 2, "fifo preservado tras undo manual");
        try { s.eliminarCliente(101); throw new AssertionError("cliente con pedidos"); }
        catch (IllegalArgumentException e) { ok++; }
        falla(() -> s.registrarCliente(new Cliente("Otro", "1234", 101, "Centro")), "cliente repetido");
        CityLink vacio = new CityLink();
        try { vacio.consultarSiguiente(); throw new AssertionError("cola vacia"); }
        catch (SinPedidosPendientesException e) { ok++; }
        falla(vacio::deshacer, "pila vacia");
        comprobar(Ordenamiento.burbuja(Collections.emptyList(), Ordenamiento.criterio("codigo")).isEmpty(), "lista vacia");
        comprobar(Ordenamiento.insercion(Collections.singletonList(s.buscarPedido(1)), Ordenamiento.criterio("codigo")).size() == 1, "lista unitario");
        comprobar(Intercalacion.intercalar(Collections.emptyList(), Collections.singletonList(s.buscarPedido(1)), Ordenamiento.criterio("codigo")).size() == 1, "fusion con vacia");
        Pedido recien = new PedidoNormal(80, s.buscarCliente(101), "Centro", 1, 1);
        s.registrarPedido(recien, "TARDE");
        comprobar(s.buscarPedido(80) == recien, "alta");
        s.deshacer();
        comprobar(s.getCantidadPedidos() == 8 && s.getPendientesNormales() == 5, "undo alta");
        comprobar(s.ordenarPedidos("fecha", "insercion").size() == 8, "fecha");
        s.cancelarPedido(4);
        comprobar(s.buscarPedido(4).getEstado().equals("CANCELADO") && s.getPendientesNormales() == 4, "cancelacion");
        s.deshacer();
        comprobar(s.buscarPedido(4).getEstado().equals("PENDIENTE") && s.getPendientesNormales() == 5, "undo cancelacion");
        System.out.println("OK - " + ok + " comprobaciones superadas");
    }
}
