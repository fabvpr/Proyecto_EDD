package Proyecto;

import java.util.*;
import java.util.UUID;

/**
 * GrafoRedSuministros - Optimización de rutas de transporte
 */
public class GrafoRedSuministros {

    private Map<Sede, List<Ruta>> adyacencia;

    public GrafoRedSuministros() {
        this.adyacencia = new HashMap<>();
    }

    public void agregarSede(String nombre) {
        Sede nueva = new Sede(nombre);
        this.adyacencia.putIfAbsent(nueva, new LinkedList<>());
    }

    private Sede getSede(String nombre) {
        for (Sede s : adyacencia.keySet()) {
            if (s.getNombre().equals(nombre)) {
                return s;
            }
        }
        return null;
    }

    public void conectarSedes(String origen, String destino, double peso) {
        agregarSede(origen);
        agregarSede(destino);

        Sede sOrigen  = getSede(origen);
        Sede sDestino = getSede(destino);

        if (sOrigen != null && sDestino != null) {
            // use distance/peso as distancia in Ruta constructor
            String rutaId1 = UUID.randomUUID().toString();
            String rutaId2 = UUID.randomUUID().toString();
            adyacencia.get(sOrigen).add(new Ruta(rutaId1, sOrigen, sDestino, peso));
            adyacencia.get(sDestino).add(new Ruta(rutaId2, sDestino, sOrigen, peso));
        }
    }

    public List<String> buscarRutaMasCorta(String nombreInicio, String nombreFin) {
        Sede inicio = getSede(nombreInicio);
        Sede fin    = getSede(nombreFin);

        if (inicio == null || fin == null) return null;

        Map<Sede, Double> distancias = new HashMap<>();
        Map<Sede, Sede>   padres     = new HashMap<>();
        PriorityQueue<Sede> cola = new PriorityQueue<>(
            Comparator.comparingDouble(s -> distancias.get(s))
        );

        for (Sede s : adyacencia.keySet()) {
            distancias.put(s, Double.MAX_VALUE);
        }
        distancias.put(inicio, 0.0);
        cola.add(inicio);

        while (!cola.isEmpty()) {
            Sede actual = cola.poll();
            if (actual.equals(fin)) break;

            for (Ruta ruta : adyacencia.get(actual)) {
                double nuevaDist = distancias.get(actual) + ruta.getDistancia();
                Sede destinoRuta = ruta.getSedeDestino();
                if (nuevaDist < distancias.get(destinoRuta)) {
                    distancias.put(destinoRuta, nuevaDist);
                    padres.put(destinoRuta, actual);
                    cola.add(destinoRuta);
                }
            }
        }
        if (!padres.containsKey(fin) && !fin.equals(inicio)) return null;
        return reconstruirCamino(padres, fin);
    }

    private List<String> reconstruirCamino(Map<Sede, Sede> padres, Sede fin) {
        LinkedList<String> camino = new LinkedList<>();
        Sede temp = fin;
        while (temp != null) {
            camino.addFirst(temp.getNombre());
            temp = padres.get(temp);
        }
        return camino;
    }

    public Map<Sede, List<Ruta>> getAdyacencia() {
        return adyacencia;
    }
}
