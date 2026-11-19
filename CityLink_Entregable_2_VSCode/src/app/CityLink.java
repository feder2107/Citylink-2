package app;

import algoritmos.Busqueda;
import algoritmos.Intercalacion;
import algoritmos.Ordenamiento;
import excepciones.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import modelo.*;

public class CityLink {
    private final TreeMap<Integer, Cliente> clientes = new TreeMap<>();
    private final ArrayList<Pedido> pedidos = new ArrayList<>();
    private final ArrayList<Repartidor> repartidores = new ArrayList<>();
    private final Deque<Pedido> urgentes = new ArrayDeque<>();
    private final Deque<Pedido> normales = new ArrayDeque<>();
    private final Deque<Operacion> historialOperaciones = new ArrayDeque<>();
    private final ArrayList<Pedido> turnoManana = new ArrayList<>();
    private final ArrayList<Pedido> turnoTarde = new ArrayList<>();

    public void registrarCliente(Cliente cliente) {
        if (cliente == null) throw new IllegalArgumentException("El cliente no existe");
        if (clientes.containsKey(cliente.getCodigo())) throw new IllegalArgumentException("Codigo de cliente repetido");
        clientes.put(cliente.getCodigo(), cliente);
    }
    public Cliente buscarCliente(int codigo) throws ClienteNoEncontradoException {
        Cliente encontrado = clientes.get(codigo);
        if (encontrado == null) throw new ClienteNoEncontradoException("No se encontro el cliente");
        return encontrado;
    }
    public Cliente buscarCliente(String nombre) throws ClienteNoEncontradoException {
        Cliente encontrado = Busqueda.linealClienteNombre(clientes.values(), nombre);
        if (encontrado == null) throw new ClienteNoEncontradoException("No se encontro el cliente");
        return encontrado;
    }
    public void eliminarCliente(int codigo) throws ClienteNoEncontradoException {
        buscarCliente(codigo);
        for (Pedido p : pedidos) if (p.getCliente().getCodigo() == codigo)
            throw new IllegalArgumentException("El cliente tiene pedidos: no puede eliminarse");
        clientes.remove(codigo);
    }
    public List<Cliente> listarClientes() { return new ArrayList<>(clientes.values()); }
    public int getCantidadClientes() { return clientes.size(); }

    public void registrarRepartidor(Repartidor repartidor) {
        if (repartidor == null) throw new IllegalArgumentException("Repartidor inexistente");
        for (Repartidor r : repartidores) if (r.getLegajo() == repartidor.getLegajo())
            throw new IllegalArgumentException("Legajo repetido");
        repartidores.add(repartidor);
    }
    public Repartidor buscarRepartidor(int legajo) {
        for (Repartidor r : repartidores) if (r.getLegajo() == legajo) return r;
        throw new IllegalArgumentException("No se encontro el repartidor");
    }
    public List<Repartidor> listarRepartidores() { return Collections.unmodifiableList(repartidores); }
    public int getCantidadRepartidores() { return repartidores.size(); }

    /** Por defecto el pedido pertenece al turno manana. */
    public void registrarPedido(Pedido pedido) throws PedidoInvalidoException {
        registrarPedido(pedido, "MANANA");
    }
    /** Agrega pedido a la lista general, al turno y a la cola por prioridad. */
    public void registrarPedido(Pedido pedido, String turno) throws PedidoInvalidoException {
        if (pedido == null) throw new PedidoInvalidoException("Pedido nulo");
        if (!"PENDIENTE".equals(pedido.getEstado()) || pedido.getVehiculo() != null)
            throw new PedidoInvalidoException("El pedido debe ser nuevo y pendiente");
        if (Busqueda.linealPedido(pedidos, pedido.getCodigo()) != null)
            throw new PedidoInvalidoException("Codigo de pedido repetido");
        if (pedido.getCliente() == null || clientes.get(pedido.getCliente().getCodigo()) != pedido.getCliente())
            throw new PedidoInvalidoException("Primero debe registrar al mismo cliente en el sistema");
        if (pedido.getPesoKg() <= 0 || pedido.getDistanciaKm() <= 0 || pedido.getDestino() == null
                || pedido.getDestino().trim().isEmpty())
            throw new PedidoInvalidoException("Peso, distancia o destino incorrectos");
        if (!"MANANA".equalsIgnoreCase(turno) && !"TARDE".equalsIgnoreCase(turno))
            throw new PedidoInvalidoException("Turno invalido: MANANA o TARDE");
        List<Pedido> listaTurno = "MANANA".equalsIgnoreCase(turno) ? turnoManana : turnoTarde;
        pedidos.add(pedido);
        listaTurno.add(pedido);
        colaDe(pedido).offer(pedido);
        historialOperaciones.push(new Operacion("Alta de pedido " + pedido.getCodigo(), () -> {
            if (!"PENDIENTE".equals(pedido.getEstado()))
                throw new IllegalStateException("El pedido ya no esta pendiente");
            colaDe(pedido).remove(pedido);
            listaTurno.remove(pedido);
            pedidos.remove(pedido);
        }));
    }
    private Deque<Pedido> colaDe(Pedido pedido) {
        return pedido instanceof PedidoUrgente ? urgentes : normales;
    }
    public int getCantidadPedidos() { return pedidos.size(); }
    public int getPendientesUrgentes() { return urgentes.size(); }
    public int getPendientesNormales() { return normales.size(); }
    public List<Pedido> listarPedidos() { return Collections.unmodifiableList(pedidos); }
    public Pedido buscarPedido(int codigo) {
        Pedido p = Busqueda.linealPedido(pedidos, codigo);
        if (p == null) throw new IllegalArgumentException("No se encontro el pedido");
        return p;
    }
    public Pedido consultarSiguiente() throws SinPedidosPendientesException {
        Pedido p = urgentes.peek();
        if (p == null) p = normales.peek();
        if (p == null) throw new SinPedidosPendientesException("No hay pedidos pendientes");
        return p;
    }
    public Repartidor atenderSiguientePedido() throws SinPedidosPendientesException, CapacidadExcedidaException {
        Pedido pedido = consultarSiguiente();
        Repartidor elegido = null;
        for (Repartidor r : repartidores) {
            if (r.getVehiculo().puedeTransportar(pedido)) { elegido = r; break; }
        }
        if (elegido == null) throw new CapacidadExcedidaException(
            "Ningun repartidor puede transportar el primer pedido de la cola. Se conserva pendiente");
        asignarARepartidor(pedido, elegido);
        return elegido;
    }
    private void asignarARepartidor(Pedido pedido, Repartidor repartidor) throws CapacidadExcedidaException {
        if (!repartidor.getVehiculo().puedeTransportar(pedido))
            throw new CapacidadExcedidaException("Vehiculo incompatible con el pedido");
        if (!"PENDIENTE".equals(pedido.getEstado()))
            throw new IllegalArgumentException("Pedido no pendiente");
        Deque<Pedido> cola = colaDe(pedido);
        if (!cola.contains(pedido)) throw new IllegalStateException("Pedido pendiente fuera de su cola");
        int posicionOriginal = new ArrayList<>(cola).indexOf(pedido);
        repartidor.asignarPedido(pedido);
        cola.remove(pedido);
        final int posicion = posicionOriginal;
        historialOperaciones.push(new Operacion("Asignacion del pedido " + pedido.getCodigo(), () -> {
            repartidor.deshacerAsignacion(pedido);
            // Conserva la posicion FIFO incluso si fue una asignacion manual.
            List<Pedido> restantes = new ArrayList<>(cola);
            restantes.add(Math.min(posicion, restantes.size()), pedido);
            cola.clear();
            cola.addAll(restantes);
        }));
    }
    public void asignarPedido(int codigoPedido, int legajo) throws CapacidadExcedidaException {
        asignarARepartidor(buscarPedido(codigoPedido), buscarRepartidor(legajo));
    }
    public void registrarEntrega(int codigoPedido) {
        Pedido pedido = buscarPedido(codigoPedido);
        Repartidor encargado = null;
        for (Repartidor r : repartidores) if (r.getPedidos().contains(pedido)) { encargado = r; break; }
        if (encargado == null) throw new IllegalArgumentException("Pedido sin repartidor asignado");
        encargado.registrarEntrega(pedido);
        final Repartidor repartidor = encargado;
        historialOperaciones.push(new Operacion("Entrega del pedido " + pedido.getCodigo(),
            () -> repartidor.deshacerEntrega(pedido)));
    }
    public void cancelarPedido(int codigoPedido) {
        Pedido pedido = buscarPedido(codigoPedido);
        if (!"PENDIENTE".equals(pedido.getEstado()))
            throw new IllegalArgumentException("Solo pueden cancelarse pedidos pendientes");
        Deque<Pedido> cola = colaDe(pedido);
        int posicionOriginal = new ArrayList<>(cola).indexOf(pedido);
        if (posicionOriginal < 0) throw new IllegalStateException("Pedido fuera de cola");
        pedido.cambiarEstado("CANCELADO");
        cola.remove(pedido);
        historialOperaciones.push(new Operacion("Cancelacion del pedido " + pedido.getCodigo(), () -> {
            pedido.restaurarEstadoPorDeshacer("PENDIENTE");
            List<Pedido> restantes = new ArrayList<>(cola);
            restantes.add(Math.min(posicionOriginal, restantes.size()), pedido);
            cola.clear();
            cola.addAll(restantes);
        }));
    }
    public String deshacer() {
        Operacion ultima = historialOperaciones.peek();
        if (ultima == null) throw new IllegalStateException("No hay operaciones para deshacer");
        ultima.deshacer();
        historialOperaciones.pop();
        return "Se deshizo: " + ultima.getDescripcion();
    }
    public int getCantidadOperacionesReversibles() { return historialOperaciones.size(); }

    public Pedido buscarPedidoBinaria(int codigo) {
        List<Pedido> ordenados = Ordenamiento.insercion(pedidos, Ordenamiento.criterio("codigo"));
        Pedido encontrado = Busqueda.binariaPedido(ordenados, codigo);
        if (encontrado == null) throw new IllegalArgumentException("No se encontro el pedido");
        return encontrado;
    }
    public List<Pedido> ordenarPedidos(String criterio, String algoritmo) {
        Comparator<Pedido> comparador = criterio.equalsIgnoreCase("costo")
            ? Comparator.comparingDouble(p -> {
                Vehiculo vehiculo = p.getVehiculo();
                if (vehiculo == null) {
                    for (Repartidor r : repartidores) {
                        if (r.getVehiculo().puedeTransportar(p)) {
                            vehiculo = r.getVehiculo();
                            break;
                        }
                    }
                }
                if (vehiculo == null) throw new IllegalStateException("Sin vehiculo compatible para estimar costo");
                return p.calcularCosto(vehiculo);
            }) : Ordenamiento.criterio(criterio);
        if ("BURBUJA".equalsIgnoreCase(algoritmo)) return Ordenamiento.burbuja(pedidos, comparador);
        if ("INSERCION".equalsIgnoreCase(algoritmo)) return Ordenamiento.insercion(pedidos, comparador);
        throw new IllegalArgumentException("Algoritmo invalido");
    }
    public List<Pedido> intercalarTurnos() {
        Comparator<Pedido> porCodigo = Ordenamiento.criterio("codigo");
        List<Pedido> manana = Ordenamiento.insercion(turnoManana, porCodigo);
        List<Pedido> tarde = Ordenamiento.insercion(turnoTarde, porCodigo);
        return Intercalacion.intercalar(manana, tarde, porCodigo);
    }
    public int getCantidadTurnoManana() { return turnoManana.size(); }
    public int getCantidadTurnoTarde() { return turnoTarde.size(); }
    public double calcularImporteTotal() {
        double total = 0;
        for (Pedido p : pedidos) if (p.getVehiculo() != null) total += p.calcularImporte();
        return total;
    }
    public void mostrarComisiones() {
        for (Repartidor r : repartidores) System.out.printf("%s: $%.2f%n", r.getNombre(), r.calcularImporte());
    }
    public String consultarEstadoPedido(int codigo) { return buscarPedido(codigo).obtenerEstadoActual(); }
    public String consultarEstadoRepartidor(int legajo) { return buscarRepartidor(legajo).obtenerEstadoActual(); }
    public void listarPedidosConCostos() {
        if (pedidos.isEmpty()) { System.out.println("No hay pedidos"); return; }
        for (Pedido p : pedidos) {
            System.out.println(p);
            if (p.getVehiculo() != null) {
                System.out.printf("Costo: $%.2f | Tiempo: %.1f minutos%n", p.calcularImporte(),
                    p.getVehiculo().calcularTiempoEntrega(p.getDistanciaKm()));
            } else System.out.println("Sin vehiculo: costo y tiempo aun no definidos");
            System.out.println("------------------------");
        }
    }
}
