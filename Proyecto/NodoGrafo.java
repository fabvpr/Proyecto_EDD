package Proyecto;

/**
 * ─────────────────────────────────────────────────────────────────
 * CLASE: NodoGrafo
 * Representa un nodo en el grafo visual con posición y propiedades
 * ─────────────────────────────────────────────────────────────────
 */
public class NodoGrafo {
    private String id;
    private Sede sede;
    private double posX;
    private double posY;
    private boolean seleccionado;
    private String color;
    private int radio;
    
    // Constructor
    public NodoGrafo(String id, Sede sede, double posX, double posY) {
        this.id = id;
        this.sede = sede;
        this.posX = posX;
        this.posY = posY;
        this.seleccionado = false;
        this.color = "#00F2FF"; // Cian por defecto
        this.radio = 20;
    }
    
    // ─── GETTERS Y SETTERS ───
    
    public String getId() { 
        return id; 
    }
    
    public Sede getSede() { 
        return sede; 
    }
    
    public double getPosX() { 
        return posX; 
    }
    
    public void setPosX(double posX) { 
        this.posX = posX; 
    }
    
    public double getPosY() { 
        return posY; 
    }
    
    public void setPosY(double posY) { 
        this.posY = posY; 
    }
    
    public boolean isSeleccionado() { 
        return seleccionado; 
    }
    
    public void setSeleccionado(boolean seleccionado) { 
        this.seleccionado = seleccionado; 
    }
    
    public String getColor() { 
        return color; 
    }
    
    public void setColor(String color) { 
        this.color = color; 
    }
    
    public int getRadio() { 
        return radio; 
    }
    
    public void setRadio(int radio) { 
        this.radio = radio; 
    }
    
    // ─── OPERACIONES DE DISTANCIA ───
    
    public double distanciaA(NodoGrafo otro) {
        double dx = this.posX - otro.posX;
        double dy = this.posY - otro.posY;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public boolean contienePunto(double x, double y) {
        double dx = x - posX;
        double dy = y - posY;
        return Math.sqrt(dx * dx + dy * dy) <= radio;
    }
}