package app;

import java.util.List;
import java.util.Scanner;
import modelo.*;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        CityLink sistema = new CityLink();
        try { cargarDatos(sistema); }
        catch (Exception e) { System.out.println("Error al cargar datos: " + e.getMessage()); }
        int opcion;
        do {
            System.out.println("\n========== CITYLINK - ENTREGABLE 2 ==========");
            System.out.println("1. Gestion de clientes");
            System.out.println("2. Gestion de repartidores");
            System.out.println("3. Registrar pedido y encolar");
            System.out.println("4. Atender siguiente pedido");
            System.out.println("5. Registrar entrega / cancelar pendiente");
            System.out.println("6. Deshacer ultima operacion");
            System.out.println("7. Ordenar y listar pedidos");
            System.out.println("8. Buscar pedido");
            System.out.println("9. Intercalar pedidos de ambos turnos");
            System.out.println("10. Salir");
            System.out.printf("Clientes: %d | Repartidores: %d | Pedidos: %d | Cola U: %d | Cola N: %d%n",
                sistema.getCantidadClientes(), sistema.getCantidadRepartidores(), sistema.getCantidadPedidos(),
                sistema.getPendientesUrgentes(), sistema.getPendientesNormales());
            opcion = leerEntero(teclado, "Opcion: ");
            try {
                switch (opcion) {
                    case 1: menuClientes(teclado, sistema); break;
                    case 2: menuRepartidores(teclado, sistema); break;
                    case 3: registrarPedido(teclado, sistema); break;
                    case 4: atenderSiguiente(sistema); break;
                    case 5: entregar(teclado, sistema); break;
                    case 6: System.out.println(sistema.deshacer()); break;
                    case 7: ordenar(teclado, sistema); break;
                    case 8: buscar(teclado, sistema); break;
                    case 9:
                        System.out.printf("Turno manana: %d | turno tarde: %d%n", sistema.getCantidadTurnoManana(), sistema.getCantidadTurnoTarde());
                        mostrarLista(sistema.intercalarTurnos());
                        break;
                    case 10: System.out.println("Gracias por utilizar CityLink"); break;
                    default: System.out.println("Opcion incorrecta");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        } while (opcion != 10);
        teclado.close();
    }
    /** Si el usuario escribe letras en vez de un numero, vuelve a solicitar el dato. */
    private static int leerEntero(Scanner sc, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Ingrese un numero entero valido."); }
        }
    }
    private static double leerDecimal(Scanner sc, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try { return Double.parseDouble(sc.nextLine().trim().replace(',', '.')); }
            catch (NumberFormatException e) { System.out.println("Ingrese un numero valido."); }
        }
    }
    private static String leerTexto(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine().trim();
    }
    private static void menuClientes(Scanner sc, CityLink sistema) throws Exception {
        System.out.println("1. Alta  2. Baja  3. Buscar codigo  4. Buscar nombre  5. Listar ordenados");
        int opcion = leerEntero(sc, "Accion: ");
        switch (opcion) {
            case 1:
                int codigo = leerEntero(sc, "Codigo: ");
                String nombre = leerTexto(sc, "Nombre: ");
                String telefono = leerTexto(sc, "Telefono: ");
                String zona = leerTexto(sc, "Zona: ");
                sistema.registrarCliente(new Cliente(nombre, telefono, codigo, zona));
                System.out.println("Cliente registrado"); break;
            case 2:
                sistema.eliminarCliente(leerEntero(sc, "Codigo a eliminar: "));
                System.out.println("Cliente eliminado"); break;
            case 3: System.out.println(sistema.buscarCliente(leerEntero(sc, "Codigo: "))); break;
            case 4: System.out.println(sistema.buscarCliente(leerTexto(sc, "Nombre: "))); break;
            case 5:
                for (Cliente c : sistema.listarClientes()) System.out.println(c);
                break;
            default: System.out.println("Opcion incorrecta");
        }
    }
    private static void menuRepartidores(Scanner sc, CityLink sistema) {
        System.out.println("1. Alta  2. Listar  3. Ver historial y comision");
        int opcion = leerEntero(sc, "Accion: ");
        switch (opcion) {
            case 1:
                int legajo = leerEntero(sc, "Legajo: ");
                String nombre = leerTexto(sc, "Nombre: ");
                System.out.println("1. Bicicleta  2. Moto  3. Camioneta");
                int tipo = leerEntero(sc, "Vehiculo: ");
                String patente = leerTexto(sc, "Patente: ");
                Vehiculo v;
                if (tipo == 1) v = new Bicicleta(patente, 15, 10, 50);
                else if (tipo == 2) v = new Moto(patente, 40, 30, 120);
                else if (tipo == 3) v = new Camioneta(patente, 30, 500, 300);
                else { System.out.println("Tipo invalido"); return; }
                sistema.registrarRepartidor(new Repartidor(legajo, nombre, v));
                System.out.println("Repartidor registrado"); break;
            case 2: for (Repartidor r : sistema.listarRepartidores()) System.out.println(r); break;
            case 3:
                Repartidor r = sistema.buscarRepartidor(leerEntero(sc, "Legajo: "));
                System.out.println(r);
                System.out.printf("Comision: $%.2f%n", r.calcularImporte());
                System.out.println("Entregas:");
                mostrarLista(r.getHistorialEntregas());
                break;
            default: System.out.println("Opcion incorrecta");
        }
    }
    private static void registrarPedido(Scanner sc, CityLink sistema) throws Exception {
        int codigo = leerEntero(sc, "Codigo de pedido: ");
        Cliente cliente = sistema.buscarCliente(leerEntero(sc, "Codigo de cliente: "));
        String destino = leerTexto(sc, "Destino: ");
        double peso = leerDecimal(sc, "Peso (kg): ");
        double distancia = leerDecimal(sc, "Distancia (km): ");
        System.out.println("1. Normal  2. Urgente  3. Fragil");
        int tipo = leerEntero(sc, "Tipo: ");
        String turno = leerTexto(sc, "Turno (MANANA/TARDE): ").toUpperCase();
        Pedido p;
        if (tipo == 1) p = new PedidoNormal(codigo, cliente, destino, peso, distancia);
        else if (tipo == 2) p = new PedidoUrgente(codigo, cliente, destino, peso, distancia);
        else if (tipo == 3) p = new PedidoFragil(codigo, cliente, destino, peso, distancia);
        else { System.out.println("Tipo invalido"); return; }
        sistema.registrarPedido(p, turno);
        System.out.println("Pedido registrado y encolado correctamente");
    }
    private static void atenderSiguiente(CityLink sistema) throws Exception {
        Pedido siguiente = sistema.consultarSiguiente();
        System.out.println("Siguiente: " + siguiente.getCodigo() + " (" + siguiente.getClass().getSimpleName() + ")");
        Repartidor r = sistema.atenderSiguientePedido();
        System.out.println("Asignado a " + r.getNombre() + " / Vehiculo: "
            + r.getVehiculo().getClass().getSimpleName() + " / Patente: " + r.getVehiculo().getPatente());
        System.out.println("Estado: " + siguiente.getEstado());
        System.out.printf("Costo: $%.2f | Tiempo: %.1f minutos%n", siguiente.calcularImporte(),
            r.getVehiculo().calcularTiempoEntrega(siguiente.getDistanciaKm()));
    }
    private static void entregar(Scanner sc, CityLink sistema) {
        System.out.println("1. Registrar entrega  2. Cancelar pedido pendiente");
        int opcion = leerEntero(sc, "Accion: ");
        int codigo = leerEntero(sc, "Codigo de pedido: ");
        if (opcion == 1) {
            sistema.registrarEntrega(codigo);
            System.out.println("Entrega registrada en el historial del repartidor");
        } else if (opcion == 2) {
            sistema.cancelarPedido(codigo);
            System.out.println("Pedido cancelado y quitado de la cola");
        } else System.out.println("Accion incorrecta");
    }
    private static void ordenar(Scanner sc, CityLink sistema) {
        System.out.println("Criterios: codigo / distancia / fecha / costo");
        String criterio = leerTexto(sc, "Criterio: ");
        System.out.println("Algoritmos: BURBUJA / INSERCION");
        String algoritmo = leerTexto(sc, "Algoritmo: ");
        mostrarLista(sistema.ordenarPedidos(criterio, algoritmo));
    }
    private static void buscar(Scanner sc, CityLink sistema) {
        System.out.println("1. Lineal  2. Binaria");
        int modo = leerEntero(sc, "Metodo: ");
        int codigo = leerEntero(sc, "Codigo de pedido: ");
        Pedido p;
        if (modo == 1) p = sistema.buscarPedido(codigo);
        else if (modo == 2) p = sistema.buscarPedidoBinaria(codigo);
        else { System.out.println("Metodo invalido"); return; }
        System.out.println(p);
    }
    private static void mostrarLista(List<Pedido> pedidos) {
        if (pedidos.isEmpty()) { System.out.println("No hay pedidos"); return; }
        for (Pedido p : pedidos) {
            String precio = p.getVehiculo() == null ? "Sin tarifa aun" : String.format("$%.2f", p.calcularImporte());
            System.out.printf("Codigo %d | %s | %s | %.1f km | %s | %s%n", p.getCodigo(),
                p.getClass().getSimpleName(), p.getEstado(), p.getDistanciaKm(), precio, p.getDestino());
        }
    }
    public static void cargarDatos(CityLink sistema) throws Exception {
        Cliente c1 = new Cliente("Fede", "5553334392", 101, "Centro");
        Cliente c2 = new Cliente("Juan", "2664123456", 102, "Norte");
        Cliente c3 = new Cliente("Pedro", "2664555444", 103, "Sur");
        Cliente c4 = new Cliente("Ana", "2664333222", 104, "Este");
        Cliente c5 = new Cliente("Maria", "2664111222", 105, "Oeste");
        sistema.registrarCliente(c1); sistema.registrarCliente(c2); sistema.registrarCliente(c3);
        sistema.registrarCliente(c4); sistema.registrarCliente(c5);
        sistema.registrarRepartidor(new Repartidor(1, "Carlos", new Bicicleta("BICI01", 15, 10, 50)));
        sistema.registrarRepartidor(new Repartidor(2, "Luis", new Moto("ABC123", 40, 30, 120)));
        sistema.registrarRepartidor(new Repartidor(3, "Martin", new Camioneta("DEF456", 30, 500, 300)));
        sistema.registrarPedido(new PedidoNormal(1, c1, "Centro", 3, 5), "MANANA");
        sistema.registrarPedido(new PedidoUrgente(2, c2, "Norte", 8, 10), "MANANA");
        sistema.registrarPedido(new PedidoFragil(3, c3, "Sur", 15, 12), "MANANA");
        sistema.registrarPedido(new PedidoNormal(4, c4, "Este", 4, 7), "TARDE");
        sistema.registrarPedido(new PedidoUrgente(5, c5, "Oeste", 12, 15), "TARDE");
        sistema.registrarPedido(new PedidoFragil(6, c1, "Centro", 2, 8), "MANANA");
        sistema.registrarPedido(new PedidoNormal(7, c2, "Norte", 20, 18), "TARDE");
        sistema.registrarPedido(new PedidoUrgente(8, c3, "Sur", 6, 9), "TARDE");
        // En el Entregable 2 se dejan todos pendientes para mostrar las colas.
    }
}
