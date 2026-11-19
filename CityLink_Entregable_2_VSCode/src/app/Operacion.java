package app;

public class Operacion {
    private final String descripcion;
    private final Runnable deshacer;
    public Operacion(String descripcion, Runnable deshacer) {
        this.descripcion = descripcion;
        this.deshacer = deshacer;
    }
    public String getDescripcion() { return descripcion; }
    public void deshacer() { deshacer.run(); }
}
