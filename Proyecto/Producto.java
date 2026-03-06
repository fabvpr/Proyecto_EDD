package Proyecto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ─────────────────────────────────────────────────────────────────
 * CLASE: Producto
 * Representa un artículo en el inventario con validación y control
 * ─────────────────────────────────────────────────────────────────
 */
public class Producto implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    
    private int sku;                          // Identificador numérico del producto
    private String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private int cantidad;
    private int cantidadMinima;
    private String categoria;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private String estado; // ACTIVO, INACTIVO, DESCONTINUADO
    private String codigoBarras;
    private double peso;
    private String proveedor;
    
    // Constructor completo
    public Producto(String id, String nombre, String descripcion, double precio, 
                    int cantidad, int cantidadMinima, String categoria, String codigoBarras) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
        this.cantidadMinima = cantidadMinima;
        this.categoria = categoria;
        this.codigoBarras = codigoBarras;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
        this.estado = "ACTIVO";
    }

    // Constructor simplificado usado en la UI de demostración
    public Producto(int sku, String nombre, int cantidad, String categoria) {
        this.sku = sku;
        this.id = String.valueOf(sku);
        this.nombre = nombre;
        this.descripcion = "";
        this.precio = 0.0;
        this.cantidad = cantidad;
        this.cantidadMinima = 0;
        this.categoria = categoria;
        this.codigoBarras = "";
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
        this.estado = "ACTIVO";
    }
    
    // ─── GETTERS Y SETTERS ───
    
    public String getId() { 
        return id; 
    }
    
    public void setId(String id) { 
        this.id = id; 
    }
    
    public String getNombre() { 
        return nombre; 
    }
    
    public void setNombre(String nombre) { 
        this.nombre = nombre;
        this.fechaModificacion = LocalDateTime.now();
    }
    
    public String getDescripcion() { 
        return descripcion; 
    }
    
    public void setDescripcion(String descripcion) { 
        this.descripcion = descripcion;
        this.fechaModificacion = LocalDateTime.now();
    }
    
    public double getPrecio() { 
        return precio; 
    }
    
    public void setPrecio(double precio) { 
        this.precio = precio;
        this.fechaModificacion = LocalDateTime.now();
    }
    
    public int getCantidad() { 
        return cantidad; 
    }
    
    public void setCantidad(int cantidad) { 
        this.cantidad = cantidad;
        this.fechaModificacion = LocalDateTime.now();
    }
    
    public void agregarStock(int cantidad) {
        this.cantidad += cantidad;
        this.fechaModificacion = LocalDateTime.now();
    }
    
    public boolean descontarStock(int cantidad) {
        if (this.cantidad >= cantidad) {
            this.cantidad -= cantidad;
            this.fechaModificacion = LocalDateTime.now();
            return true;
        }
        return false;
    }
    
    public int getCantidadMinima() { 
        return cantidadMinima; 
    }
    
    public void setCantidadMinima(int cantidadMinima) { 
        this.cantidadMinima = cantidadMinima; 
    }
    
    public boolean esCritico() { 
        return cantidad <= cantidadMinima; 
    }
    
    public String getCategoria() { 
        return categoria; 
    }
    
    public void setCategoria(String categoria) { 
        this.categoria = categoria; 
    }
    
    public LocalDateTime getFechaCreacion() { 
        return fechaCreacion; 
    }
    
    public LocalDateTime getFechaModificacion() { 
        return fechaModificacion; 
    }
    
    public String getEstado() { 
        return estado; 
    }
    
    public void setEstado(String estado) { 
        this.estado = estado; 
    }
    
    public String getCodigoBarras() { 
        return codigoBarras; 
    }
    
    public void setCodigoBarras(String codigoBarras) { 
        this.codigoBarras = codigoBarras; 
    }
    
    public double getPeso() { 
        return peso; 
    }
    
    public void setPeso(double peso) { 
        this.peso = peso; 
    }
    
    public String getProveedor() { 
        return proveedor; 
    }
    
    public void setProveedor(String proveedor) { 
        this.proveedor = proveedor; 
    }

    // SKU accessors
    public int getSku() { 
        return sku; 
    }
    public void setSku(int sku) {
        this.sku = sku;
    }
    
    @Override
    public String toString() {
        return String.format("[SKU %d] %s (%s) - Precio: $%.2f | Stock: %d | Estado: %s", 
            sku, nombre, id, precio, cantidad, estado);
    }
    
    @Override
    public Producto clone() throws CloneNotSupportedException {
        return (Producto) super.clone();
    }
}