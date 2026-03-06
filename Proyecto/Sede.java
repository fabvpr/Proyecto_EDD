package Proyecto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ─────────────────────────────────────────────────────────────────
 * CLASE: Sede
 * Representa un nodo en la red de suministros con ubicación
 * ─────────────────────────────────────────────────────────────────
 */
public class Sede implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String nombre;
    private String ciudad;
    private String pais;
    private double latitud;
    private double longitud;
    private String tipoSede; // ALMACEN, DISTRIBUCION, TIENDA, FABRICACION
    private int capacidadMaxima;
    private int stockActual;
    private List<Producto> inventario;
    private LocalDateTime fechaRegistro;
    private boolean activa;
    
    // Constructor
    public Sede(String id, String nombre, String ciudad, String pais, 
                double latitud, double longitud, String tipoSede, int capacidadMaxima) {
        this.id = id;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.pais = pais;
        this.latitud = latitud;
        this.longitud = longitud;
        this.tipoSede = tipoSede;
        this.capacidadMaxima = capacidadMaxima;
        this.inventario = new CopyOnWriteArrayList<>();
        this.fechaRegistro = LocalDateTime.now();
        this.activa = true;
        this.stockActual = 0;
    }

    // Conveniencia: crear sede solo con nombre (otros campos por defecto)
    public Sede(String nombre) {
        this(nombre, nombre, "", "", 0.0, 0.0, "", 0);
    }
    
    // ─── GETTERS Y SETTERS ───
    
    public String getId() { 
        return id; 
    }
    
    public String getNombre() { 
        return nombre; 
    }
    
    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }
    
    public String getCiudad() { 
        return ciudad; 
    }
    
    public String getPais() { 
        return pais; 
    }
    
    public double getLatitud() { 
        return latitud; 
    }
    
    public double getLongitud() { 
        return longitud; 
    }
    
    public String getTipoSede() { 
        return tipoSede; 
    }
    
    public int getCapacidadMaxima() { 
        return capacidadMaxima; 
    }
    
    public int getStockActual() { 
        return stockActual; 
    }
    
    public double getPorcentajeOcupacion() { 
        return (stockActual * 100.0) / capacidadMaxima; 
    }
    
    public List<Producto> getInventario() { 
        return new ArrayList<>(inventario); 
    }
    
    // ─── OPERACIONES DE INVENTARIO ───
    
    public void agregarProducto(Producto producto) {
        if (!inventario.contains(producto)) {
            inventario.add(producto);
            stockActual += producto.getCantidad();
        }
    }
    
    public void removerProducto(Producto producto) {
        if (inventario.remove(producto)) {
            stockActual -= producto.getCantidad();
        }
    }
    
    public Producto buscarProductoPorId(String id) {
        return inventario.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public LocalDateTime getFechaRegistro() { 
        return fechaRegistro; 
    }
    
    public boolean isActiva() { 
        return activa; 
    }
    
    public void setActiva(boolean activa) { 
        this.activa = activa; 
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) - %s, %s | Ocupación: %.1f%% | Stock: %d/%d",
            nombre, id, ciudad, pais, getPorcentajeOcupacion(), stockActual, capacidadMaxima);
    }
}