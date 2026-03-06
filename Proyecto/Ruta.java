package Proyecto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ─────────────────────────────────────────────────────────────────
 * CLASE: Ruta
 * Representa una conexión entre dos sedes con distancia y costo
 * ─────────────────────────────────────────────────────────────────
 */
public class Ruta implements Serializable, Comparable<Ruta> {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private Sede sedeOrigen;
    private Sede sedeDestino;
    private double distancia; // en km
    private double costo; // en dinero
    private double tiempoEstimado; // en horas
    private String estado; // ACTIVA, INACTIVA, MANTENIMIENTO
    private LocalDateTime fechaCreacion;
    private int vehiculosDisponibles;
    private double capacidadVehiculo;
    private String tipoCarretera; // AUTOPISTA, RUTA_NACIONAL, LOCAL
    
    // Constructor
    public Ruta(String id, Sede sedeOrigen, Sede sedeDestino, double distancia) {
        this.id = id;
        this.sedeOrigen = sedeOrigen;
        this.sedeDestino = sedeDestino;
        this.distancia = distancia;
        this.estado = "ACTIVA";
        this.fechaCreacion = LocalDateTime.now();
        this.vehiculosDisponibles = 5;
        this.capacidadVehiculo = 1000.0; // kg
        this.tipoCarretera = "LOCAL";
        calcularCostoYTiempo();
    }
    
    // ─── CÁLCULOS AUTOMÁTICOS ───
    
    private void calcularCostoYTiempo() {
        // Costo: $0.50 por km + $10 base
        this.costo = (distancia * 0.50) + 10;
        // Tiempo: velocidad promedio 80 km/h
        this.tiempoEstimado = distancia / 80.0;
    }
    
    // ─── GETTERS Y SETTERS ───
    
    public String getId() { 
        return id; 
    }
    
    public Sede getSedeOrigen() { 
        return sedeOrigen; 
    }
    
    public Sede getSedeDestino() { 
        return sedeDestino; 
    }
    
    public double getDistancia() { 
        return distancia; 
    }
    
    public double getCosto() { 
        return costo; 
    }
    
    public double getTiempoEstimado() { 
        return tiempoEstimado; 
    }
    
    public String getEstado() { 
        return estado; 
    }
    
    public void setEstado(String estado) { 
        this.estado = estado; 
    }
    
    public int getVehiculosDisponibles() { 
        return vehiculosDisponibles; 
    }
    
    public void setVehiculosDisponibles(int cantidad) { 
        this.vehiculosDisponibles = cantidad; 
    }
    
    public double getCapacidadVehiculo() { 
        return capacidadVehiculo; 
    }
    
    public void setCapacidadVehiculo(double capacidad) { 
        this.capacidadVehiculo = capacidad; 
    }
    
    public String getTipoCarretera() { 
        return tipoCarretera; 
    }
    
    public void setTipoCarretera(String tipo) { 
        this.tipoCarretera = tipo; 
    }
    
    @Override
    public int compareTo(Ruta otra) {
        return Double.compare(this.distancia, otra.distancia);
    }
    
    @Override
    public String toString() {
        return String.format("%s -> %s | Distancia: %.2f km | Costo: $%.2f | Tiempo: %.2f h",
            sedeOrigen.getNombre(), sedeDestino.getNombre(), distancia, costo, tiempoEstimado);
    }
}