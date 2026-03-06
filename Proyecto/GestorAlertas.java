package Proyecto;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * GestorAlertas — Logica de gestion de alertas de stock
 */
public class GestorAlertas {

    private final List<AlertaStock>    alertas;
    private final ConfiguracionSistema config;
    private       Object               modeloTabla;
    private Consumer<AlertaStock>      onNuevaAlerta;
    private Consumer<Integer>          onContadorCambiado;

    public static final String[] COLUMNAS = {
        "Departamento","Producto","SKU","Stock","Nivel Crítico","Prioridad","Fecha"
    };

    public GestorAlertas() {
        this.alertas = new ArrayList<>();
        this.config  = ConfiguracionSistema.getInstance();
    }

    public void setModeloTabla(Object m)         { this.modeloTabla = m; }
    public void setOnNuevaAlerta(Consumer<AlertaStock> cb)  { this.onNuevaAlerta = cb; }
    public void setOnContadorCambiado(Consumer<Integer> cb) { this.onContadorCambiado = cb; }

    public boolean verificarStockCritico(String sede, Producto producto) {
        int nivel = config.getNivelCriticoGlobal();
        if (producto.getCantidad() >= nivel) return false;
        boolean yaExiste = alertas.stream().anyMatch(a ->
            !a.isAtendida() && a.getSku() == producto.getSku() && a.getSede().equals(sede));
        if (yaExiste) return false;
        AlertaStock nueva = new AlertaStock(sede, producto.getNombre(),
            producto.getCategoria(), producto.getSku(),
            producto.getCantidad(), nivel);
        alertas.add(nueva);
        try {
            if (modeloTabla instanceof javax.swing.table.DefaultTableModel) {
                javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) modeloTabla;
                agregarFilaTabla(nueva, model);
            }
        } catch (Exception e) {
            // Manejo silencioso
        }
        if (onNuevaAlerta      != null) onNuevaAlerta.accept(nueva);
        if (onContadorCambiado != null) onContadorCambiado.accept(getContadorPendientes());
        return true;
    }

    public int verificarTodasLasSedes(Map<String, SedeData> sedes) {
        int nuevas = 0;
        for (Map.Entry<String, SedeData> e : sedes.entrySet())
            for (Producto p : e.getValue().getProductos())
                if (verificarStockCritico(e.getKey(), p)) nuevas++;
        return nuevas;
    }

    public String atenderAlertaEnFila(int fila) {
        if (modeloTabla == null || fila < 0) return null;
        try {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) modeloTabla;
            if (fila >= model.getRowCount()) return null;
            Object skuObj  = model.getValueAt(fila, 2);
            Object sedeObj = model.getValueAt(fila, 0);
            if (skuObj == null || sedeObj == null) return null;
            int sku = (int) skuObj;
            String sede = sedeObj.toString();
            AlertaStock alerta = alertas.stream()
                .filter(a -> !a.isAtendida() && a.getSku() == sku && a.getSede().equals(sede))
                .findFirst().orElse(null);
            if (alerta == null) return null;
            alerta.atender("Atendida desde panel");
            model.removeRow(fila);
            if (onContadorCambiado != null) onContadorCambiado.accept(getContadorPendientes());
            return alerta.getDescripcionCorta();
        } catch (Exception e) {
            return null;
        }
    }

    public int atenderTodasCriticas() {
        List<AlertaStock> criticas = alertas.stream()
            .filter(a -> !a.isAtendida() && AlertaStock.PRIORIDAD_CRITICA.equals(a.getPrioridad()))
            .collect(Collectors.toList());
        criticas.forEach(a -> a.atender("Atendida en lote"));
        refrescarTabla();
        if (onContadorCambiado != null) onContadorCambiado.accept(getContadorPendientes());
        return criticas.size();
    }

    public void refrescarTabla() {
        if (modeloTabla == null) return;
        try {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) modeloTabla;
            model.setRowCount(0);
            alertas.stream()
                   .filter(a -> !a.isAtendida())
                   .sorted(Comparator.comparingInt(AlertaStock::getPrioridadNumerica))
                   .forEach(a -> agregarFilaTabla(a, model));
        } catch (Exception e) {
            // Manejo silencioso de errores de casting
        }
    }

    private void agregarFilaTabla(AlertaStock a, javax.swing.table.DefaultTableModel model) {
        if (model == null) return;
        model.addRow(new Object[]{
            a.getSede(), a.getProducto(), a.getSku(),
            a.getNivelActual(), a.getNivelCritico(),
            a.getPrioridad(), a.getFechaFormateada()
        });
    }

    public int getContadorPendientes() {
        return (int) alertas.stream().filter(a -> !a.isAtendida()).count();
    }

    public int getContadorTotal()      { return alertas.size(); }

    public List<AlertaStock> getAlertasPendientes() {
        return alertas.stream().filter(a -> !a.isAtendida()).collect(Collectors.toList());
    }

    public List<AlertaStock> getTodasLasAlertas() {
        return new ArrayList<>(alertas);
    }

    public Map<String, Integer> getResumenPorPrioridad() {
        Map<String, Integer> r = new LinkedHashMap<>();
        r.put(AlertaStock.PRIORIDAD_CRITICA, 0);
        r.put(AlertaStock.PRIORIDAD_ALTA,    0);
        r.put(AlertaStock.PRIORIDAD_MEDIA,   0);
        r.put(AlertaStock.PRIORIDAD_BAJA,    0);
        alertas.stream().filter(a -> !a.isAtendida())
               .forEach(a -> r.merge(a.getPrioridad(), 1, Integer::sum));
        return r;
    }

    public int limpiarAtendidas() {
        int antes = alertas.size();
        alertas.removeIf(AlertaStock::isAtendida);
        return antes - alertas.size();
    }
}
