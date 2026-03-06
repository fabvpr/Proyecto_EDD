package Proyecto;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ─────────────────────────────────────────────────────────────────
 * CLASE: RegistroAuditoria
 * Mantiene un registro detallado de todas las operaciones del sistema
 * ─────────────────────────────────────────────────────────────────
 */
public class RegistroAuditoria implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static int contador = 0;
    private int id;
    private LocalDateTime timestamp;
    private String usuario;
    private String tipoOperacion; // CREAR, EDITAR, ELIMINAR, VER
    private String entidad; // PRODUCTO, SEDE, RUTA
    private String entidadId;
    private String detalles;
    private String estadoAnterior;
    private String estadoNuevo;
    private RegistroAuditoria anterior;
    private RegistroAuditoria siguiente;
    
    // Constructor completo
    public RegistroAuditoria(String usuario, String tipoOperacion, String entidad, 
                            String entidadId, String detalles) {
        this.id = ++contador;
        this.timestamp = LocalDateTime.now();
        this.usuario = usuario;
        this.tipoOperacion = tipoOperacion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.detalles = detalles;
    }

    // Conveniencia: solo detalles (otros campos vacíos)
    public RegistroAuditoria(String detalles) {
        this("", "", "", "", detalles);
    }

    // enlaces para la lista doblemente enlazada
    public RegistroAuditoria getAnterior() { return anterior; }
    public RegistroAuditoria getSiguiente() { return siguiente; }
    public void setAnterior(RegistroAuditoria r) { this.anterior = r; }
    public void setSiguiente(RegistroAuditoria r) { this.siguiente = r; }
    
    // ─── GETTERS Y SETTERS ───
    
    public int getId() { 
        return id; 
    }
    
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }
    
    public String getUsuario() { 
        return usuario; 
    }
    
    public String getTipoOperacion() { 
        return tipoOperacion; 
    }
    
    public String getEntidad() { 
        return entidad; 
    }
    
    public String getEntidadId() { 
        return entidadId; 
    }
    
    public String getDetalles() { 
        return detalles; 
    }
    
    public String getEstadoAnterior() { 
        return estadoAnterior; 
    }
    
    public void setEstadoAnterior(String estado) { 
        this.estadoAnterior = estado; 
    }
    
    public String getEstadoNuevo() { 
        return estadoNuevo; 
    }
    
    public void setEstadoNuevo(String estado) { 
        this.estadoNuevo = estado; 
    }
    
    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] %s | %s | %s: %s | Usuario: %s | %s",
            id, timestamp.format(fmt), tipoOperacion, entidad, entidadId, usuario, detalles);
    }
}