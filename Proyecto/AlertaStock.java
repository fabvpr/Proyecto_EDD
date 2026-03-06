package Proyecto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AlertaStock - Modelo de alerta de stock critico
 * Registra alertas con prioridad automatica y estado de atencion.
 */
public class AlertaStock {

    private static int contadorGlobal = 0;
    private final int idAlerta;

    private String    sede;
    private String    producto;
    private String    categoria;
    private int       sku;
    private int       nivelActual;
    private int       nivelCritico;
    private String    prioridad;
    private LocalDateTime fecha;
    private boolean   atendida;
    private String    notaAtencion;
    private LocalDateTime fechaAtencion;

    public static final String PRIORIDAD_CRITICA = "CRITICA";
    public static final String PRIORIDAD_ALTA    = "ALTA";
    public static final String PRIORIDAD_MEDIA   = "MEDIA";
    public static final String PRIORIDAD_BAJA    = "BAJA";

    public AlertaStock(String sede, String producto, int sku,
                       int nivelActual, int nivelCritico) {
        this.idAlerta     = ++contadorGlobal;
        this.sede         = sede;
        this.producto     = producto;
        this.sku          = sku;
        this.nivelActual  = nivelActual;
        this.nivelCritico = nivelCritico;
        this.fecha        = LocalDateTime.now();
        this.atendida     = false;
        this.notaAtencion = "";
        this.categoria    = "General";
        this.prioridad    = calcularPrioridad(nivelActual, nivelCritico);
    }

    public AlertaStock(String sede, String producto, String categoria,
                       int sku, int nivelActual, int nivelCritico) {
        this(sede, producto, sku, nivelActual, nivelCritico);
        this.categoria = categoria;
    }

    public static String calcularPrioridad(int nivelActual, int nivelCritico) {
        if (nivelCritico <= 0) return PRIORIDAD_BAJA;
        int pct = (nivelActual * 100) / nivelCritico;
        if (pct <= 30) return PRIORIDAD_CRITICA;
        if (pct <= 60) return PRIORIDAD_ALTA;
        if (pct <= 80) return PRIORIDAD_MEDIA;
        return PRIORIDAD_BAJA;
    }

    public int getPrioridadNumerica() {
        switch (prioridad) {
            case PRIORIDAD_CRITICA: return 1;
            case PRIORIDAD_ALTA:    return 2;
            case PRIORIDAD_MEDIA:   return 3;
            default:                return 4;
        }
    }

    public void atender(String nota) {
        this.atendida      = true;
        this.notaAtencion  = nota != null ? nota : "";
        this.fechaAtencion = LocalDateTime.now();
    }

    public String getFechaFormateada() {
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getDescripcionCorta() {
        return String.format("Alerta [%s]: %s en %s (stock=%d, critico=%d)",
            prioridad, producto, sede, nivelActual, nivelCritico);
    }

    public String getDescripcionCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID Alerta  : #").append(idAlerta).append("\n");
        sb.append("Producto   : ").append(producto).append("\n");
        sb.append("SKU        : ").append(sku).append("\n");
        sb.append("Categoria  : ").append(categoria).append("\n");
        sb.append("Sede       : ").append(sede).append("\n");
        sb.append("Stock actual: ").append(nivelActual).append(" uds\n");
        sb.append("Nivel critico: ").append(nivelCritico).append(" uds\n");
        sb.append("Prioridad  : ").append(prioridad).append("\n");
        sb.append("Fecha      : ").append(getFechaFormateada()).append("\n");
        sb.append("Estado     : ").append(atendida ? "Atendida" : "Pendiente").append("\n");
        if (atendida && !notaAtencion.isEmpty()) {
            sb.append("Nota       : ").append(notaAtencion).append("\n");
        }
        return sb.toString();
    }

    public int    getIdAlerta()            { return idAlerta; }
    public String getSede()                { return sede; }
    public void   setSede(String s)        { this.sede = s; }
    public String getProducto()            { return producto; }
    public void   setProducto(String p)    { this.producto = p; }
    public String getCategoria()           { return categoria; }
    public void   setCategoria(String c)   { this.categoria = c; }
    public int    getSku()                 { return sku; }
    public int    getNivelActual()         { return nivelActual; }
    public void   setNivelActual(int n)    { this.nivelActual = n; }
    public int    getNivelCritico()        { return nivelCritico; }
    public void   setNivelCritico(int n)   { this.nivelCritico = n; }
    public String getPrioridad()           { return prioridad; }
    public void   recalcularPrioridad()    { this.prioridad = calcularPrioridad(nivelActual, nivelCritico); }
    public LocalDateTime getFecha()        { return fecha; }
    public boolean isAtendida()            { return atendida; }
    public String  getNotaAtencion()       { return notaAtencion; }
    public LocalDateTime getFechaAtencion(){ return fechaAtencion; }

    @Override
    public String toString() {
        return String.format("Alerta#%d[%s | %s | prio=%s | atendida=%b]",
            idAlerta, producto, sede, prioridad, atendida);
    }
}
