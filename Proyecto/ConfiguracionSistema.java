package Proyecto;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * ConfiguracionSistema - Singleton de configuracion de la app
 * Gestiona parametros globales y persistencia de preferencias.
 */
public class ConfiguracionSistema {

    private static ConfiguracionSistema instancia;
    private static final String PREF_NODE = "Proyecto/GestRed";
    private Preferences prefs;

    private int  nivelCriticoGlobal;
    private int  intervaloVerificacionMs;
    private boolean alertasAutomaticas;
    private String capaMapaActiva;
    private double latitudCentral;
    private double longitudCentral;
    private int    zoomInicial;
    private String nombreUsuario;
    private boolean mostrarBarraEstado;
    private boolean animacionesActivas;

    public static final String[] EDGE_COLORS = {
        "#FF4500", "#00D9FF", "#10B981", "#F59E0B",
        "#EC4899", "#A78BFA", "#3882F6", "#F97316",
        "#14B8A6", "#8B5CF6", "#EF4444", "#22C55E"
    };

    private final Map<String, String> propiedades;

    private ConfiguracionSistema() {
        this.propiedades = new HashMap<>();
        cargarDefectos();
        try {
            this.prefs = Preferences.userRoot().node(PREF_NODE);
            cargarDesdePrefs();
        } catch (Exception e) {
            System.err.println("[Config] No se pudo cargar Preferences: " + e.getMessage());
        }
    }

    public static synchronized ConfiguracionSistema getInstance() {
        if (instancia == null) {
            instancia = new ConfiguracionSistema();
        }
        return instancia;
    }

    private void cargarDefectos() {
        this.nivelCriticoGlobal      = 50;
        this.intervaloVerificacionMs = 30_000;
        this.alertasAutomaticas      = true;
        this.capaMapaActiva          = "satelite";
        this.latitudCentral          = -9.19;
        this.longitudCentral         = -75.0;
        this.zoomInicial             = 5;
        this.nombreUsuario           = "Administrador";
        this.mostrarBarraEstado      = true;
        this.animacionesActivas      = true;
    }

    private void cargarDesdePrefs() {
        if (prefs == null) return;
        nivelCriticoGlobal      = prefs.getInt("nivelCritico",     nivelCriticoGlobal);
        intervaloVerificacionMs = prefs.getInt("intervaloVerif",   intervaloVerificacionMs);
        alertasAutomaticas      = prefs.getBoolean("alertasAuto",  alertasAutomaticas);
        capaMapaActiva          = prefs.get("capaMapa",            capaMapaActiva);
        zoomInicial             = prefs.getInt("zoomInicial",      zoomInicial);
        nombreUsuario           = prefs.get("nombreUsuario",       nombreUsuario);
        mostrarBarraEstado      = prefs.getBoolean("barraEstado",  mostrarBarraEstado);
        animacionesActivas      = prefs.getBoolean("animaciones",  animacionesActivas);
    }

    public void guardar() {
        if (prefs == null) return;
        try {
            prefs.putInt("nivelCritico",    nivelCriticoGlobal);
            prefs.putInt("intervaloVerif",  intervaloVerificacionMs);
            prefs.putBoolean("alertasAuto", alertasAutomaticas);
            prefs.put("capaMapa",           capaMapaActiva);
            prefs.putInt("zoomInicial",     zoomInicial);
            prefs.put("nombreUsuario",      nombreUsuario);
            prefs.putBoolean("barraEstado", mostrarBarraEstado);
            prefs.putBoolean("animaciones", animacionesActivas);
            prefs.flush();
        } catch (Exception e) {
            System.err.println("[Config] Error al guardar: " + e.getMessage());
        }
    }

    public void restaurarDefectos() {
        cargarDefectos();
        guardar();
    }

    public boolean setNivelCriticoGlobal(int nivel) {
        if (nivel <= 0 || nivel > 100_000) return false;
        this.nivelCriticoGlobal = nivel;
        return true;
    }

    public boolean setIntervaloVerificacion(int ms) {
        if (ms < 5_000 || ms > 300_000) return false;
        this.intervaloVerificacionMs = ms;
        return true;
    }

    public boolean setZoomInicial(int zoom) {
        if (zoom < 1 || zoom > 18) return false;
        this.zoomInicial = zoom;
        return true;
    }

    public void setPropiedad(String clave, String valor) {
        propiedades.put(clave, valor);
    }

    public String getPropiedad(String clave, String defecto) {
        return propiedades.getOrDefault(clave, defecto);
    }

    public int     getNivelCriticoGlobal()            { return nivelCriticoGlobal; }
    public int     getIntervaloVerificacionMs()        { return intervaloVerificacionMs; }
    public boolean isAlertasAutomaticas()              { return alertasAutomaticas; }
    public void    setAlertasAutomaticas(boolean a)    { this.alertasAutomaticas = a; }
    public String  getCapaMapaActiva()                 { return capaMapaActiva; }
    public void    setCapaMapaActiva(String c)         { this.capaMapaActiva = c; }
    public double  getLatitudCentral()                 { return latitudCentral; }
    public void    setLatitudCentral(double l)         { this.latitudCentral = l; }
    public double  getLongitudCentral()                { return longitudCentral; }
    public void    setLongitudCentral(double l)        { this.longitudCentral = l; }
    public int     getZoomInicial()                    { return zoomInicial; }
    public String  getNombreUsuario()                  { return nombreUsuario; }
    public void    setNombreUsuario(String n)          { this.nombreUsuario = n; }
    public boolean isMostrarBarraEstado()              { return mostrarBarraEstado; }
    public void    setMostrarBarraEstado(boolean m)    { this.mostrarBarraEstado = m; }
    public boolean isAnimacionesActivas()              { return animacionesActivas; }
    public void    setAnimacionesActivas(boolean a)    { this.animacionesActivas = a; }

    @Override
    public String toString() {
        return String.format(
            "Config[nivelCritico=%d, usuario=%s, mapa=%s, zoom=%d]",
            nivelCriticoGlobal, nombreUsuario, capaMapaActiva, zoomInicial);
    }
}
