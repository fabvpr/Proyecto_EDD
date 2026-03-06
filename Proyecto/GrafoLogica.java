package Proyecto;
import java.util.*;

/**
 * ─────────────────────────────────────────────────────────────────
 * CLASE: GrafoLogica
 * Lógica de negocio para el grafo: cálculos, búsquedas, algoritmos
 * ─────────────────────────────────────────────────────────────────
 */
public class GrafoLogica {
    private Map<String, Sede> sedes;
    private List<Ruta> rutas;
    private Map<String, NodoGrafo> nodosGrafo;
    
    // Constructor
    public GrafoLogica() {
        this.sedes = new LinkedHashMap<>();
        this.rutas = new ArrayList<>();
        this.nodosGrafo = new HashMap<>();
    }
    
    // ─── GESTIÓN DE SEDES ───
    
    public void agregarSede(Sede sede) {
        if (!sedes.containsKey(sede.getId())) {
            sedes.put(sede.getId(), sede);
        }
    }
    
    public Sede obtenerSede(String id) {
        return sedes.get(id);
    }
    
    public List<Sede> obtenerTodasLasSedes() {
        return new ArrayList<>(sedes.values());
    }
    
    public boolean eliminarSede(String id) {
        // Eliminar rutas que involucren esta sede
        rutas.removeIf(r -> r.getSedeOrigen().getId().equals(id) || 
                           r.getSedeDestino().getId().equals(id));
        return sedes.remove(id) != null;
    }
    
    // ─── GESTIÓN DE RUTAS ───
    
    public void agregarRuta(Ruta ruta) {
        if (!rutas.contains(ruta)) {
            rutas.add(ruta);
        }
    }
    
    public List<Ruta> obtenerRutasDesde(String sedeId) {
        List<Ruta> resultado = new ArrayList<>();
        for (Ruta ruta : rutas) {
            if (ruta.getSedeOrigen().getId().equals(sedeId)) {
                resultado.add(ruta);
            }
        }
        return resultado;
    }
    
    public Ruta obtenerRuta(String sedeOrigenId, String sedeDestinoId) {
        return rutas.stream()
            .filter(r -> r.getSedeOrigen().getId().equals(sedeOrigenId) && 
                        r.getSedeDestino().getId().equals(sedeDestinoId))
            .findFirst()
            .orElse(null);
    }
    
    public List<Ruta> obtenerTodasLasRutas() {
        return new ArrayList<>(rutas);
    }
    
    // ─── ALGORITMO DIJKSTRA ───
    
    public List<Ruta> calcularRutaMasCorta(String sedeOrigenId, String sedeDestinoId) {
        Sede origen = sedes.get(sedeOrigenId);
        Sede destino = sedes.get(sedeDestinoId);
        
        if (origen == null || destino == null) {
            return null;
        }
        
        Map<String, Double> distancias = new HashMap<>();
        Map<String, String> anteriores = new HashMap<>();
        Set<String> visitados = new HashSet<>();
        
        // Inicializar
        for (String id : sedes.keySet()) {
            distancias.put(id, Double.MAX_VALUE);
        }
        distancias.put(sedeOrigenId, 0.0);
        
        // Algoritmo Dijkstra
        for (int i = 0; i < sedes.size(); i++) {
            // Encontrar nodo no visitado con menor distancia
            String nodoActual = null;
            double menorDist = Double.MAX_VALUE;
            
            for (String id : sedes.keySet()) {
                if (!visitados.contains(id) && distancias.get(id) < menorDist) {
                    nodoActual = id;
                    menorDist = distancias.get(id);
                }
            }
            
            if (nodoActual == null) break;
            visitados.add(nodoActual);
            
            // Relajar vecinos
            for (Ruta ruta : obtenerRutasDesde(nodoActual)) {
                String vecino = ruta.getSedeDestino().getId();
                double nuevaDist = distancias.get(nodoActual) + ruta.getDistancia();
                
                if (nuevaDist < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDist);
                    anteriores.put(vecino, nodoActual);
                }
            }
        }
        
        // Reconstruir ruta
        List<Ruta> rutaFinal = new ArrayList<>();
        String actual = sedeDestinoId;
        
        while (anteriores.containsKey(actual)) {
            String anterior = anteriores.get(actual);
            Ruta ruta = obtenerRuta(anterior, actual);
            if (ruta != null) {
                rutaFinal.add(0, ruta);
            }
            actual = anterior;
        }
        
        return rutaFinal.isEmpty() ? null : rutaFinal;
    }
    
    // ─── GESTIÓN DE NODOS GRAFO ───
    
    public void agregarNodoGrafo(NodoGrafo nodo) {
        nodosGrafo.put(nodo.getId(), nodo);
    }
    
    public NodoGrafo obtenerNodoGrafo(String sedeId) {
        return nodosGrafo.get(sedeId);
    }
    
    public Collection<NodoGrafo> obtenerTodosLosNodos() {
        return nodosGrafo.values();
    }
    
    // ─── ESTADÍSTICAS ───
    
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSedes", sedes.size());
        stats.put("totalRutas", rutas.size());
        
        double distanciaTotal = rutas.stream()
            .mapToDouble(Ruta::getDistancia)
            .sum();
        stats.put("distanciaTotal", distanciaTotal);
        
        double costoTotal = rutas.stream()
            .mapToDouble(Ruta::getCosto)
            .sum();
        stats.put("costoTotal", costoTotal);
        
        int stockTotal = sedes.values().stream()
            .mapToInt(Sede::getStockActual)
            .sum();
        stats.put("stockTotal", stockTotal);
        
        return stats;
    }
}