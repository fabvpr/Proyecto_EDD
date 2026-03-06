package Proyecto;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * ─────────────────────────────────────────────────────────────────
 * CLASE: GestorAuditoria
 * Administra el registro de auditoría del sistema
 * ─────────────────────────────────────────────────────────────────
 */
public class GestorAuditoria {
    private List<RegistroAuditoria> registros;
    private List<String> historialOperaciones;
    
    // Constructor
    public GestorAuditoria() {
        this.registros = new CopyOnWriteArrayList<>();
        this.historialOperaciones = new CopyOnWriteArrayList<>();
    }
    
    // ─── REGISTRAR OPERACIÓN ───
    
    public void registrarOperacion(String usuario, String tipoOperacion, String entidad,
                                  String entidadId, String detalles) {
        RegistroAuditoria registro = new RegistroAuditoria(usuario, tipoOperacion, 
                                                          entidad, entidadId, detalles);
        registros.add(registro);
        historialOperaciones.add(registro.toString());
        
        // Mantener solo los últimos 1000 registros en memoria
        if (historialOperaciones.size() > 1000) {
            historialOperaciones.remove(0);
        }
    }
    
    // ─── OBTENER REGISTROS ───
    
    public List<RegistroAuditoria> obtenerRegistros() {
        return new ArrayList<>(registros);
    }
    
    public List<RegistroAuditoria> obtenerRegistrosPorUsuario(String usuario) {
        return registros.stream()
            .filter(r -> r.getUsuario().equals(usuario))
            .collect(Collectors.toList());
    }
    
    public List<RegistroAuditoria> obtenerRegistrosPorEntidad(String entidad) {
        return registros.stream()
            .filter(r -> r.getEntidad().equals(entidad))
            .collect(Collectors.toList());
    }
    
    public List<RegistroAuditoria> obtenerRegistrosPorOperacion(String tipoOperacion) {
        return registros.stream()
            .filter(r -> r.getTipoOperacion().equals(tipoOperacion))
            .collect(Collectors.toList());
    }
    
    public List<String> obtenerHistorial() {
        return new ArrayList<>(historialOperaciones);
    }
    
    // ─── LIMPIAR REGISTROS ───
    
    public void limpiarRegistros() {
        registros.clear();
        historialOperaciones.clear();
    }
    
    public int obtenerCantidadRegistros() {
        return registros.size();
    }
}