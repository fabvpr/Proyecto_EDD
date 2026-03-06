package Proyecto;

import java.util.ArrayList;
import java.util.List;

/**
 * SedeData - Modelo de datos para cada departamento/sede del Peru
 * Almacena informacion geografica, de capacidad y productos.
 */
public class SedeData {

    private String nombre;
    private String region;
    private double latitud;
    private double longitud;
    private int capacidadMaxima;
    private int stockActual;
    private final List<Producto> productos;
    private int totalIngresos;
    private int totalDespachos;
    private boolean activa;

    public SedeData(String nombre, double latitud, double longitud,
                    String region, int capacidadMaxima) {
        this.nombre          = nombre;
        this.latitud         = latitud;
        this.longitud        = longitud;
        this.region          = region;
        this.capacidadMaxima = capacidadMaxima;
        this.stockActual     = 0;
        this.totalIngresos   = 0;
        this.totalDespachos  = 0;
        this.activa          = true;
        this.productos       = new ArrayList<>();
    }

    public void agregarProducto(Producto p) {
        productos.add(p);
        stockActual    += p.getCantidad();
        totalIngresos  += p.getCantidad();
    }

    public boolean eliminarProducto(int sku) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getSku() == sku) {
                int cant = productos.get(i).getCantidad();
                stockActual     -= cant;
                totalDespachos  += cant;
                productos.remove(i);
                return true;
            }
        }
        return false;
    }

    public int actualizarCantidadProducto(int sku, int nuevaCantidad) {
        for (Producto p : productos) {
            if (p.getSku() == sku) {
                int diff = nuevaCantidad - p.getCantidad();
                p.setCantidad(nuevaCantidad);
                stockActual += diff;
                if (diff > 0) totalIngresos  +=  diff;
                else          totalDespachos += -diff;
                return diff;
            }
        }
        return 0;
    }

    public Producto buscarProducto(int sku) {
        for (Producto p : productos) {
            if (p.getSku() == sku) return p;
        }
        return null;
    }

    public double getPorcentajeOcupacion() {
        if (capacidadMaxima <= 0) return 0.0;
        return Math.min(100.0, (stockActual * 100.0) / capacidadMaxima);
    }

    public String getEstadoOcupacion() {
        double pct = getPorcentajeOcupacion();
        if (pct >= 90) return "CRÍTICO";
        if (pct >= 70) return "ALTO";
        if (pct >= 40) return "NORMAL";
        return "BAJO";
    }

    public String getNombre()              { return nombre; }
    public void   setNombre(String n)      { this.nombre = n; }
    public String getRegion()              { return region; }
    public void   setRegion(String r)      { this.region = r; }
    public double getLatitud()             { return latitud; }
    public void   setLatitud(double lat)   { this.latitud = lat; }
    public double getLongitud()            { return longitud; }
    public void   setLongitud(double lon)  { this.longitud = lon; }
    public int  getCapacidadMaxima()       { return capacidadMaxima; }
    public void setCapacidadMaxima(int c)  { this.capacidadMaxima = c; }
    public int  getStockActual()           { return stockActual; }
    public void setStockActual(int s)      { this.stockActual = s; }
    public int  getTotalIngresos()         { return totalIngresos; }
    public int  getTotalDespachos()        { return totalDespachos; }
    public boolean isActiva()              { return activa; }
    public void    setActiva(boolean a)    { this.activa = a; }
    public List<Producto> getProductos()   { return productos; }

    @Override
    public String toString() {
        return String.format("Sede[%s | %s | stock=%d/%d]",
            nombre, region, stockActual, capacidadMaxima);
    }
}
