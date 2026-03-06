package Proyecto;

/**
 * ListaHistorial - Historial de operaciones y despachos
 */
public class ListaHistorial {
    private RegistroAuditoria cabeza, cola, actual;

    public void registrar(String desc) {
        RegistroAuditoria nuevo = new RegistroAuditoria(desc);
        if (cabeza == null) {
            cabeza = cola = actual = nuevo;
        } else {
            cola.setSiguiente(nuevo);
            nuevo.setAnterior(cola);
            cola = nuevo;
            actual = nuevo;
        }
    }

    public String navegarAtras() {
        if (actual != null && actual.getAnterior() != null) {
            actual = actual.getAnterior();
            return actual.getDetalles();
        }
        return (actual != null) ? "Inicio: " + actual.getDetalles() : "Vacío";
    }

    public String navegarAdelante() {
        if (actual != null && actual.getSiguiente() != null) {
            actual = actual.getSiguiente();
            return actual.getDetalles();
        }
        return (actual != null) ? "Fin: " + actual.getDetalles() : "Vacío";
    }

    public RegistroAuditoria getCabeza() { return cabeza; }
    public RegistroAuditoria getCola() { return cola; }
    public RegistroAuditoria getActual() { return actual; }
}
