package Proyecto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ─────────────────────────────────────────────────────────────────
 * CLASE: CyberpunkButton
 * Botón personalizado con efecto neón y animaciones
 * ─────────────────────────────────────────────────────────────────
 */
public class CyberpunkButton extends JButton {
    private Color colorPrimario;
    private Color colorSecundario;
    private boolean mouseOver = false;
    private int glow = 0;
    
    // Constructor
    public CyberpunkButton(String text, Color colorPrimario) {
        super(text);
        this.colorPrimario = colorPrimario;
        this.colorSecundario = new Color(11, 14, 20); // Negro profundo
        
        configurarEstilo();
        agregarEventos();
    }
    
    // ─── CONFIGURACIÓN DE ESTILO ───
    
    private void configurarEstilo() {
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFont(new Font("Orbitron", Font.BOLD, 14));
        setForeground(colorPrimario);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    // ─── EVENTOS DE MOUSE ───
    
    private void agregarEventos() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseOver = true;
                animarGlow();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                mouseOver = false;
                glow = 0;
                repaint();
            }
        });
    }
    
    // ─── ANIMACIÓN DE GLOW ───
    
    private void animarGlow() {
        if (mouseOver) {
            glow = Math.min(glow + 2, 15);
            repaint();
            SwingUtilities.invokeLater(this::animarGlow);
        }
    }
    
    // ─── PINTAR COMPONENTE ───
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dibujar glow effect
        if (glow > 0) {
            g2d.setColor(new Color(colorPrimario.getRed(), colorPrimario.getGreen(), 
                                  colorPrimario.getBlue(), 50));
            for (int i = glow; i > 0; i--) {
                g2d.setStroke(new BasicStroke(i));
                g2d.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 15, 15);
            }
        }
        
        // Dibujar botón
        g2d.setColor(mouseOver ? colorPrimario : colorSecundario);
        g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 15, 15);
        
        g2d.setColor(colorPrimario);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 15, 15);
        
        // Dibujar texto
        g2d.setColor(colorPrimario);
        g2d.setFont(getFont());
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(getText(), x, y);
    }
}