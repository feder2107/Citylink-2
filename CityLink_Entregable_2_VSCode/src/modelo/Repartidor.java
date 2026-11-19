package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Repartidor implements Facturable, Rastreable {
    private int legajo;
    private String nombre;
    private Vehiculo vehiculo;
    private final List<Pedido> pedidos;
    private final List<Pedido> historialEntregas;

    public Repartidor(int legajo, String nombre, Vehiculo vehiculo) {
        if (legajo <= 0) throw new IllegalArgumentException("El legajo debe ser positivo");
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("Nombre incorrecto");
        if (vehiculo == null) throw new IllegalArgumentException("Debe tener un vehiculo");
        this.legajo = legajo;
        this.nombre = nombre;
        this.vehiculo = vehiculo;
        this.pedidos = new ArrayList<>();
        this.historialEntregas = new ArrayList<>();
    }
    public int getLegajo() { return legajo; }
    public String getNombre() { return nombre; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public List<Pedido> getPedidos() { return Collections.unmodifiableList(pedidos); }
    public List<Pedido> getHistorialEntregas() { return Collections.unmodifiableList(historialEntregas); }
    public int getCantidadPedidos() { return pedidos.size(); }

    /** Asigna un pedido pendiente si el vehiculo lo admite. */
    public void asignarPedido(Pedido pedido) {
        if (pedido == null) throw new IllegalArgumentException("El pedido no existe");
        if (!vehiculo.puedeTransportar(pedido)) throw new IllegalArgumentException("El vehiculo no puede transportarlo");
        if (!pedido.getEstado().equals("PENDIENTE") || pedido.getVehiculo() != null)
            throw new IllegalArgumentException("El pedido ya fue asignado o no esta pendiente");
        pedido.cambiarEstado("EN_CAMINO");
        pedido.setVehiculo(vehiculo);
        pedidos.add(pedido);
    }
    public void asignarPedido(Pedido pedido, Vehiculo vehiculo) {
        if (this.vehiculo != vehiculo) throw new IllegalArgumentException("El vehiculo no pertenece al repartidor");
        asignarPedido(pedido);
    }
    public void registrarEntrega(Pedido pedido) {
        if (pedido == null || !pedidos.contains(pedido))
            throw new IllegalArgumentException("El pedido no pertenece al repartidor");
        pedido.cambiarEstado("ENTREGADO");
        historialEntregas.add(pedido);
    }
    public void deshacerAsignacion(Pedido pedido) {
        if (!pedidos.remove(pedido)) throw new IllegalStateException("No estaba asignado");
        pedido.restaurarEstadoPorDeshacer("PENDIENTE");
        pedido.setVehiculo(null);
    }
    public void deshacerEntrega(Pedido pedido) {
        if (!historialEntregas.remove(pedido)) throw new IllegalStateException("No estaba en el historial");
        pedido.restaurarEstadoPorDeshacer("EN_CAMINO");
    }
    @Override public double calcularImporte() {
        double comision = 0;
        for (Pedido p : historialEntregas) comision += p.calcularImporte() * 0.10;
        return comision;
    }
    @Override public String obtenerEstadoActual() {
        return "Repartidor activo. Pedidos asignados: " + pedidos.size() + ", entregas: " + historialEntregas.size();
    }
    @Override public String toString() {
        return "Legajo: " + legajo + ", nombre: " + nombre
                + ", vehiculo: " + vehiculo.getClass().getSimpleName()
                + ", patente: " + vehiculo.getPatente()
                + ", pedidos: " + pedidos.size() + ", entregados: " + historialEntregas.size();
    }
}
