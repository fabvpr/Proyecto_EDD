package Proyecto;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VentanaPrincipalEjecutable extends JFrame {

    static final Color BG_BASE       = new Color(0x07,0x09,0x12);
    static final Color BG_SURFACE    = new Color(0x0E,0x11,0x1F);
    static final Color BG_CARD       = new Color(0x14,0x18,0x2B);
    static final Color BG_CARD2      = new Color(0x1A,0x1F,0x36);
    static final Color BG_PANEL      = new Color(0x10,0x13,0x24);
    static final Color VIOLET        = new Color(0x6D,0x28,0xD9);
    static final Color PURPLE_SOFT   = new Color(0xA7,0x8B,0xFA);
    static final Color CYAN          = new Color(0x00,0xF2,0xFF);
    static final Color GREEN         = new Color(0x05,0xFF,0xA1);
    static final Color RED           = new Color(0xFF,0x2D,0x55);
    static final Color ORANGE        = new Color(0xFF,0x9F,0x1C);
    static final Color YELLOW        = new Color(0xF9,0xF0,0x2E);
    static final Color PINK          = new Color(0xFF,0x71,0xCE);
    static final Color WHITE_NEON    = new Color(0xFF,0xFF,0xFF);
    static final Color TXT_WHITE     = new Color(0xF0,0xF4,0xFF);
    static final Color TXT_PRIMARY   = new Color(0xC8,0xD2,0xE8);
    static final Color TXT_SECONDARY = new Color(0x6A,0x7A,0x9B);
    static final Color BORDER_DARK   = new Color(0x1E,0x25,0x40);

    static final Color[] EDGE_COLORS = {
        CYAN, PINK, new Color(0xFF,0xEB,0x3B), ORANGE, PURPLE_SOFT,
        new Color(0x3B,0x82,0xF6), new Color(0xF7,0x79,0x1A), GREEN
    };

    static final Font F_LOGO    = new Font("Monospaced",Font.BOLD,22);
    static final Font F_TITLE   = new Font("Monospaced",Font.BOLD,15);
    static final Font F_HEADING = new Font("Monospaced",Font.BOLD,13);
    static final Font F_BODY    = new Font("Monospaced",Font.PLAIN,12);
    static final Font F_SMALL   = new Font("Monospaced",Font.PLAIN,10);
    static final Font F_LABEL   = new Font("Monospaced",Font.BOLD,11);
    static final Font F_MONO    = new Font("Courier New",Font.PLAIN,12);
    static final Font F_NAV     = new Font("Monospaced",Font.BOLD,11);
    static final Font F_STAT    = new Font("Monospaced",Font.BOLD,30);
    static final Font F_NODE    = new Font("Monospaced",Font.BOLD,9);

    // ── NodoVisual ────────────────────────────────────────────────
    static class NodoVisual {
        String nombre; double x,y,tx,ty; Color color;
        float pulso=0f,selAlpha=0f,rutaNodoAlpha=0f;
        boolean seleccionado=false,enRutaOrigen=false,enRutaFin=false,enRutaMedio=false;
        static final int R=24;
        NodoVisual(String n,double x,double y,Color c){nombre=n;this.x=tx=x;this.y=ty=y;color=c;}
        boolean contiene(int px,int py){return Math.hypot(px-x,py-y)<=R+8;}
        void tick(){
            x+=(tx-x)*0.22;y+=(ty-y)*0.22;pulso+=0.05f;
            if(pulso>(float)(2*Math.PI))pulso-=(float)(2*Math.PI);
            selAlpha+=seleccionado?0.10f:-0.10f;
            rutaNodoAlpha+=(enRutaOrigen||enRutaFin||enRutaMedio)?0.09f:-0.07f;
            selAlpha=Math.max(0f,Math.min(1f,selAlpha));
            rutaNodoAlpha=Math.max(0f,Math.min(1f,rutaNodoAlpha));
        }
    }

    // ── AristaVisual ─────────────────────────────────────────────
    static class AristaVisual {
        NodoVisual a,b; double peso; Color color;
        boolean enRuta=false; float rutaAlpha=0f;
        AristaVisual(NodoVisual a,NodoVisual b,double p,Color c){this.a=a;this.b=b;peso=p;color=c;}
        void tick(){
            rutaAlpha+=enRuta?0.07f:-0.05f;
            rutaAlpha=Math.max(0f,Math.min(1f,rutaAlpha));
        }
    }

    // ── GrafoCanvas ──────────────────────────────────────────────
    class GrafoCanvas extends JPanel {
        final List<NodoVisual>   nodos   = new ArrayList<>();
        final List<AristaVisual> aristas = new ArrayList<>();
        NodoVisual dragging=null; int dragOffX,dragOffY;
        NodoVisual nodoConA=null; boolean modoConectar=false;
        List<String> rutaActual=null;
        static final int NP=30;
        float[] pT=new float[NP]; int[] pEdge=new int[NP]; float[] pSpeed=new float[NP];
        boolean particlesInit=false; float scanPhase=0f;

        GrafoCanvas(){
            setBackground(BG_BASE); setOpaque(true);
            addMouseListener(new MouseAdapter(){
                @Override public void mousePressed(MouseEvent e){
                    requestFocusInWindow();
                    NodoVisual n=nodoEnPunto(e.getX(),e.getY());
                    if(n!=null){
                        if(modoConectar)manejarConexion(n);
                        else{dragging=n;dragOffX=(int)(e.getX()-n.x);dragOffY=(int)(e.getY()-n.y);}
                    }
                }
                @Override public void mouseReleased(MouseEvent e){dragging=null;}
                @Override public void mouseClicked(MouseEvent e){
                    if(e.getClickCount()==2&&!modoConectar){
                        NodoVisual n=nodoEnPunto(e.getX(),e.getY());
                        if(n!=null)mostrarInfoNodo(n);
                    }
                }
            });
            addMouseMotionListener(new MouseMotionAdapter(){
                @Override public void mouseDragged(MouseEvent e){
                    if(dragging!=null){
                        dragging.tx=Math.max(NodoVisual.R+8,Math.min(getWidth()-NodoVisual.R-8,e.getX()-dragOffX));
                        dragging.ty=Math.max(NodoVisual.R+8,Math.min(getHeight()-NodoVisual.R-8,e.getY()-dragOffY));
                    }
                }
                @Override public void mouseMoved(MouseEvent e){
                    setCursor(nodoEnPunto(e.getX(),e.getY())!=null?Cursor.getPredefinedCursor(Cursor.HAND_CURSOR):Cursor.getDefaultCursor());
                }
            });
            new javax.swing.Timer(16,e->{
                for(NodoVisual n:nodos)n.tick();
                for(AristaVisual a:aristas)a.tick();
                tickP();scanPhase+=0.004f;repaint();
            }).start();
        }

        private void manejarConexion(NodoVisual n){
            if(nodoConA==null){nodoConA=n;n.seleccionado=true;setStatus("◈ CONECTAR — clic en DESTINO  (ESC cancela)");}
            else if(n!=nodoConA){
                String input=JOptionPane.showInputDialog(this,"Distancia (km):\n"+nodoConA.nombre+" ↔ "+n.nombre,"Nueva Conexión",JOptionPane.QUESTION_MESSAGE);
                if(input!=null){try{double d=Double.parseDouble(input.trim());if(d<=0)throw new NumberFormatException();agregarArista(nodoConA,n,d);}
                catch(NumberFormatException ex){JOptionPane.showMessageDialog(this,"Distancia positiva.","Error",JOptionPane.WARNING_MESSAGE);}}
                nodoConA.seleccionado=false;nodoConA=null;modoConectar=false;
                setStatus("◈ Arrastra nodos · 2×clic info · Panel derecho: Dijkstra");
            }
        }

        void agregarNodo(String nombre){
            int cx=getWidth()>0?getWidth()/2:500,cy=getHeight()>0?getHeight()/2:320;
            double ang=Math.random()*2*Math.PI,r=90+Math.random()*180;
            double x=Math.max(40,Math.min((getWidth()>0?getWidth():900)-40,cx+r*Math.cos(ang)));
            double y=Math.max(40,Math.min((getHeight()>0?getHeight():600)-40,cy+r*Math.sin(ang)));
            nodos.add(new NodoVisual(nombre,x,y,EDGE_COLORS[nodos.size()%EDGE_COLORS.length]));
            grafoLogico.agregarSede(nombre);actualizarCombosDijkstra();registrarEvento("Nodo: "+nombre);
        }

        void agregarArista(NodoVisual a,NodoVisual b,double peso){
            if(aristas.stream().anyMatch(ar->(ar.a==a&&ar.b==b)||(ar.a==b&&ar.b==a))){
                JOptionPane.showMessageDialog(this,"Ya existe esa conexión.","Duplicado",JOptionPane.WARNING_MESSAGE);return;
            }
            aristas.add(new AristaVisual(a,b,peso,EDGE_COLORS[aristas.size()%EDGE_COLORS.length]));
            grafoLogico.conectarSedes(a.nombre,b.nombre,peso);cntRutas++;actualizarStats();
            registrarEvento("Arista: "+a.nombre+"↔"+b.nombre+" ("+peso+"km)");
        }

        void resaltarRuta(List<String> ruta){
            this.rutaActual=ruta;
            for(NodoVisual n:nodos){n.enRutaOrigen=false;n.enRutaFin=false;n.enRutaMedio=false;}
            for(AristaVisual a:aristas)a.enRuta=false;
            if(ruta==null||ruta.size()<2)return;
            for(int i=0;i<ruta.size();i++){
                NodoVisual n=nodoPorNombre(ruta.get(i));if(n==null)continue;
                if(i==0)n.enRutaOrigen=true;else if(i==ruta.size()-1)n.enRutaFin=true;else n.enRutaMedio=true;
            }
            for(int i=0;i<ruta.size()-1;i++){
                final String na=ruta.get(i),nb=ruta.get(i+1);
                aristas.stream().filter(a->(a.a.nombre.equals(na)&&a.b.nombre.equals(nb))||(a.a.nombre.equals(nb)&&a.b.nombre.equals(na))).forEach(a->a.enRuta=true);
            }
            iniciarP();
        }

        void limpiarRuta(){
            for(NodoVisual n:nodos){n.enRutaOrigen=false;n.enRutaFin=false;n.enRutaMedio=false;}
            for(AristaVisual a:aristas)a.enRuta=false;particlesInit=false;rutaActual=null;
        }

        void limpiarGrafo(){
            nodos.clear();aristas.clear();cntRutas=0;actualizarStats();
            particlesInit=false;rutaActual=null;actualizarCombosDijkstra();registrarEvento("Grafo limpiado");
        }

        NodoVisual nodoEnPunto(int px,int py){for(int i=nodos.size()-1;i>=0;i--)if(nodos.get(i).contiene(px,py))return nodos.get(i);return null;}
        NodoVisual nodoPorNombre(String n){return nodos.stream().filter(x->x.nombre.equals(n)).findFirst().orElse(null);}

        private void mostrarInfoNodo(NodoVisual n){
            SedeData sd=sedesPeru.get(n.nombre);
            String info=sd!=null?n.nombre+"\nRegión: "+sd.region+"\nStock: "+sd.stockActual+"/"+sd.capacidadMaxima+"\nProductos: "+sd.productos.size():n.nombre+"\n(Sin datos de sede)";
            JOptionPane.showMessageDialog(this,info,"Nodo: "+n.nombre,JOptionPane.INFORMATION_MESSAGE);
        }

        private void iniciarP(){
            List<Integer> idx=new ArrayList<>();for(int i=0;i<aristas.size();i++)if(aristas.get(i).enRuta)idx.add(i);
            if(idx.isEmpty()){particlesInit=false;return;}
            Random rng=new Random();for(int i=0;i<NP;i++){pT[i]=rng.nextFloat();pEdge[i]=idx.get(i%idx.size());pSpeed[i]=0.008f+rng.nextFloat()*0.012f;}
            particlesInit=true;
        }
        private void tickP(){if(!particlesInit)return;for(int i=0;i<NP;i++){pT[i]+=pSpeed[i];if(pT[i]>1f)pT[i]-=1f;}}

        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
            pintarFondo(g2);pintarAristas(g2);pintarParticulas(g2);pintarNodos(g2);pintarOverlay(g2);
        }

        private void pintarFondo(Graphics2D g2){
            g2.setColor(BG_BASE);g2.fillRect(0,0,getWidth(),getHeight());
            // Grid de puntos radial
            int gap=42;
            for(int x=gap;x<getWidth();x+=gap)for(int y=gap;y<getHeight();y+=gap){
                float d=(float)Math.sqrt(Math.pow(x-getWidth()/2.0,2)+Math.pow(y-getHeight()/2.0,2));
                float maxD=(float)Math.sqrt(Math.pow(getWidth(),2)+Math.pow(getHeight(),2))/2f;
                float alpha=0.12f+(1f-(d/maxD))*0.22f;
                g2.setColor(new Color(0x22,0x2A,0x44,(int)(alpha*200)));g2.fillOval(x-1,y-1,2,2);
            }
            // Scanline animada
            g2.setComposite(AlphaComposite.SrcOver.derive(0.025f));g2.setColor(CYAN);
            int sy=(int)((scanPhase%1f)*getHeight());
            for(int y=sy;y<getHeight();y+=55)g2.fillRect(0,y,getWidth(),1);
            g2.setComposite(AlphaComposite.SrcOver);
            // Borde
            g2.setColor(new Color(CYAN.getRed(),CYAN.getGreen(),CYAN.getBlue(),18));
            g2.setStroke(new BasicStroke(1.5f));g2.drawRect(3,3,getWidth()-7,getHeight()-7);
        }

        private void pintarAristas(Graphics2D g2){
            for(AristaVisual a:aristas){
                int x1=(int)a.a.x,y1=(int)a.a.y,x2=(int)a.b.x,y2=(int)a.b.y;
                int mx=(x1+x2)/2,my=(y1+y2)/2;
                if(a.rutaAlpha>0.01f){
                    // RUTA BLANCA con glow multicapa
                    float[] ws={20f,13f,7f,4f,2f};float[] as={0.05f,0.10f,0.22f,0.50f,1.0f};
                    for(int i=0;i<ws.length;i++){
                        g2.setComposite(AlphaComposite.SrcOver.derive(a.rutaAlpha*as[i]));
                        g2.setColor(WHITE_NEON);g2.setStroke(new BasicStroke(ws[i],BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                        g2.drawLine(x1,y1,x2,y2);
                    }
                    g2.setComposite(AlphaComposite.SrcOver);
                    pintarTagArista(g2,mx,my,a,true);
                } else {
                    g2.setComposite(AlphaComposite.SrcOver.derive(0.45f));
                    g2.setColor(a.color);g2.setStroke(new BasicStroke(1.6f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{8,6},0));
                    g2.drawLine(x1,y1,x2,y2);g2.setComposite(AlphaComposite.SrcOver);
                    pintarTagArista(g2,mx,my,a,false);
                }
            }
        }

        private void pintarTagArista(Graphics2D g2,int mx,int my,AristaVisual a,boolean ruta){
            String dist=String.format("%.0f km",a.peso);g2.setFont(F_SMALL);FontMetrics fm=g2.getFontMetrics();int tw=fm.stringWidth(dist);
            g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));g2.setColor(BG_CARD);
            g2.fillRoundRect(mx-tw/2-5,my-9,tw+10,16,8,8);
            Color col=ruta?WHITE_NEON:a.color;
            g2.setColor(new Color(col.getRed(),col.getGreen(),col.getBlue(),110));
            g2.setStroke(new BasicStroke(1f));g2.drawRoundRect(mx-tw/2-5,my-9,tw+10,16,8,8);
            g2.setComposite(AlphaComposite.SrcOver);g2.setColor(ruta?WHITE_NEON:TXT_SECONDARY);
            g2.drawString(dist,mx-tw/2,my+3);
        }

        private void pintarParticulas(Graphics2D g2){
            if(!particlesInit||rutaActual==null)return;
            for(int i=0;i<NP;i++){
                if(pEdge[i]>=aristas.size())continue;
                AristaVisual a=aristas.get(pEdge[i]);if(!a.enRuta)continue;
                float px=(float)(a.a.x+(a.b.x-a.a.x)*pT[i]),py=(float)(a.a.y+(a.b.y-a.a.y)*pT[i]);
                float br=(float)(0.5+0.5*Math.sin(i*1.7+pT[i]*10));
                g2.setComposite(AlphaComposite.SrcOver.derive(br*0.22f));g2.setColor(WHITE_NEON);
                g2.fillOval((int)px-9,(int)py-9,18,18);
                g2.setComposite(AlphaComposite.SrcOver.derive(br*0.95f));g2.setColor(WHITE_NEON);
                g2.fillOval((int)px-3,(int)py-3,6,6);
                g2.setComposite(AlphaComposite.SrcOver.derive(br*0.35f));g2.setColor(CYAN);
                g2.fillOval((int)px-5,(int)py-5,10,10);g2.setComposite(AlphaComposite.SrcOver);
            }
        }

        private void pintarNodos(Graphics2D g2){
            for(NodoVisual n:nodos){
                int cx=(int)n.x,cy=(int)n.y,R=NodoVisual.R;
                float pf=(float)(1.0+0.07*Math.sin(n.pulso));int pr=(int)(R*pf);
                Color rutaC=n.enRutaOrigen?GREEN:n.enRutaFin?RED:n.enRutaMedio?WHITE_NEON:null;
                // Corona de ruta
                if(n.rutaNodoAlpha>0.01f&&rutaC!=null){
                    for(int i=5;i>=1;i--){
                        g2.setComposite(AlphaComposite.SrcOver.derive(n.rutaNodoAlpha*(i/5f)*0.30f));
                        g2.setColor(rutaC);g2.setStroke(new BasicStroke(i*3.2f));
                        int r2=pr+i*5;g2.drawOval(cx-r2,cy-r2,r2*2,r2*2);
                    }g2.setComposite(AlphaComposite.SrcOver);
                }
                // Glow normal
                Color gc=rutaC!=null&&n.rutaNodoAlpha>0.5f?rutaC:n.color;
                for(int i=3;i>=1;i--){
                    g2.setComposite(AlphaComposite.SrcOver.derive((n.selAlpha*0.20f+0.04f)*(i/3f)));
                    g2.setColor(gc);g2.setStroke(new BasicStroke(i*2.2f));g2.drawOval(cx-pr-i*3,cy-pr-i*3,(pr+i*3)*2,(pr+i*3)*2);
                }g2.setComposite(AlphaComposite.SrcOver);
                // Relleno
                Color f1=new Color(Math.min(255,n.color.getRed()+55),Math.min(255,n.color.getGreen()+55),Math.min(255,n.color.getBlue()+55),195);
                Color f2=new Color(n.color.getRed(),n.color.getGreen(),n.color.getBlue(),125);
                g2.setPaint(new RadialGradientPaint(cx-R/3f,cy-R/3f,R*1.4f,new float[]{0f,1f},new Color[]{f1,f2}));
                g2.fillOval(cx-pr,cy-pr,pr*2,pr*2);
                // Borde
                Color bc=rutaC!=null&&n.rutaNodoAlpha>0.3f?rutaC:n.seleccionado?Color.WHITE:n.color;
                g2.setColor(bc);g2.setStroke(new BasicStroke(rutaC!=null?2.5f:1.8f));g2.drawOval(cx-pr,cy-pr,pr*2,pr*2);
                // Centro
                g2.setColor(new Color(0,0,0,150));g2.fillOval(cx-5,cy-5,10,10);
                g2.setColor(n.enRutaOrigen?GREEN:n.enRutaFin?RED:n.enRutaMedio?WHITE_NEON:Color.WHITE);
                g2.fillOval(cx-3,cy-3,6,6);
                // Badge
                if(n.enRutaOrigen&&n.rutaNodoAlpha>0.3f)badge(g2,cx,cy-pr-8,"INICIO",GREEN);
                if(n.enRutaFin   &&n.rutaNodoAlpha>0.3f)badge(g2,cx,cy-pr-8,"FIN",   RED);
                // Sigla
                g2.setFont(F_NODE);FontMetrics fm=g2.getFontMetrics();
                String sig=n.nombre.length()>4?n.nombre.substring(0,4):n.nombre;
                g2.setColor(new Color(0,0,0,130));g2.drawString(sig,cx-fm.stringWidth(sig)/2+1,cy+fm.getAscent()/2-3+1);
                g2.setColor(Color.WHITE);g2.drawString(sig,cx-fm.stringWidth(sig)/2,cy+fm.getAscent()/2-3);
                // Etiqueta
                g2.setFont(F_SMALL);fm=g2.getFontMetrics();int lx=cx-fm.stringWidth(n.nombre)/2,ly=cy+pr+15;
                g2.setColor(new Color(BG_BASE.getRed(),BG_BASE.getGreen(),BG_BASE.getBlue(),195));
                g2.fillRoundRect(lx-4,ly-11,fm.stringWidth(n.nombre)+8,14,6,6);
                g2.setColor(rutaC!=null&&n.rutaNodoAlpha>0.3f?rutaC:n.seleccionado?n.color:TXT_PRIMARY);
                g2.drawString(n.nombre,lx,ly);
            }
        }

        private void badge(Graphics2D g2,int cx,int cy,String txt,Color col){
            g2.setFont(new Font("Monospaced",Font.BOLD,8));FontMetrics fm=g2.getFontMetrics();int tw=fm.stringWidth(txt);
            g2.setComposite(AlphaComposite.SrcOver.derive(0.88f));
            g2.setColor(new Color(col.getRed(),col.getGreen(),col.getBlue(),45));g2.fillRoundRect(cx-tw/2-4,cy-10,tw+8,12,6,6);
            g2.setColor(col);g2.setStroke(new BasicStroke(1f));g2.drawRoundRect(cx-tw/2-4,cy-10,tw+8,12,6,6);
            g2.setComposite(AlphaComposite.SrcOver);g2.setColor(col);g2.drawString(txt,cx-tw/2,cy);
        }

        private void pintarOverlay(Graphics2D g2){
            if(modoConectar){
                g2.setFont(F_LABEL);
                String msg=nodoConA==null?"◈ CONECTAR — clic en ORIGEN":"◈ CONECTAR — clic en DESTINO  (origen: "+nodoConA.nombre+")";
                FontMetrics fm=g2.getFontMetrics();int tw=fm.stringWidth(msg),tx=(getWidth()-tw)/2,ty=26;
                g2.setComposite(AlphaComposite.SrcOver.derive(0.92f));g2.setColor(BG_CARD);
                g2.fillRoundRect(tx-12,ty-15,tw+24,22,12,12);g2.setColor(PINK);g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(tx-12,ty-15,tw+24,22,12,12);g2.setComposite(AlphaComposite.SrcOver);
                g2.setColor(PINK);g2.drawString(msg,tx,ty);
            }
            if(nodos.isEmpty()){
                g2.setFont(new Font("Monospaced",Font.BOLD,14));
                String m1="◉  Agrega nodos desde el panel inferior";
                String m2="Usa  ⊕ Clic-Clic  o el formulario de conexión";
                FontMetrics fm=g2.getFontMetrics();int cx=getWidth()/2,cy=getHeight()/2;
                g2.setColor(new Color(CYAN.getRed(),CYAN.getGreen(),CYAN.getBlue(),50));
                g2.drawString(m1,cx-fm.stringWidth(m1)/2,cy-14);
                g2.setFont(F_SMALL);fm=g2.getFontMetrics();
                g2.setColor(new Color(TXT_SECONDARY.getRed(),TXT_SECONDARY.getGreen(),TXT_SECONDARY.getBlue(),70));
                g2.drawString(m2,cx-fm.stringWidth(m2)/2,cy+8);
            }
        }

        void setStatus(String msg){if(statusLabel!=null)statusLabel.setText("  "+msg);}
    }

    // ── Clases auxiliares ────────────────────────────────────────
    static class SedeData{
        String nombre,region;double latitud,longitud;int capacidadMaxima,stockActual;
        final List<Producto> productos=new ArrayList<>();
        SedeData(String n,double lat,double lon,String r,int cap){nombre=n;latitud=lat;longitud=lon;region=r;capacidadMaxima=cap;stockActual=0;}
    }
    static class AlertaStock{
        String sede,producto,prioridad;int sku,nivelActual,nivelCritico;LocalDateTime fecha;boolean atendida;
        AlertaStock(String s,String p,int sk,int a,int c){
            sede=s;producto=p;sku=sk;nivelActual=a;nivelCritico=c;fecha=LocalDateTime.now();atendida=false;
            int pct=c>0?(a*100)/c:100;prioridad=pct<=30?"CRÍTICA":pct<=60?"ALTA":pct<=80?"MEDIA":"BAJA";
        }
    }

    // ── Campos ───────────────────────────────────────────────────
    private final GrafoRedSuministros grafoLogico=new GrafoRedSuministros();
    private final ArbolInventario     bst=new ArbolInventario();
    private final ListaHistorial      historial=new ListaHistorial();

    private DefaultTableModel modeloInventario,modeloAlertas;
    private DefaultListModel<String> modeloEventos=new DefaultListModel<>();
    private JList<String> listaEventos;
    private JLabel statProductos,statRutas,statAlertas;
    private JLabel scProductos,scRutas,scAlertas,scDepartamentos,scEventos,scStock;
    private int cntProductos=0,cntRutas=0,cntAlertas=0;
    private JPanel panelContenido;private CardLayout cardLayout;

    private static final String[] NAV_LABELS={"◎ Red de Suministros","◫ Inventario","◈ Buscar Ruta","▤ Historial","⚠ Alertas Stock","▣ Estadísticas"};
    private static final String[] NAV_CARDS={"red","inventario","rutas","historial","alertas","estadisticas"};
    private final JButton[] navButtons=new JButton[6];

    private int nivelCriticoGlobal=50;
    private final Map<String,SedeData>  sedesPeru=new LinkedHashMap<>();
    private final List<AlertaStock>     alertasStock=new ArrayList<>();

    private GrafoCanvas grafoCanvas;
    private JLabel statusLabel;
    private JComboBox<String> cbDijkOrigen,cbDijkDestino;
    private JLabel lblRutaResultado;
    private JTextArea areaRutaDetalle;

    // ── Constructor ──────────────────────────────────────────────
    public VentanaPrincipalEjecutable(){
        setTitle("◈ GESTRED v9  //  Red de Suministros  //  Cyberpunk");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());getContentPane().setBackground(BG_BASE);
        inicializarDepartamentos();configurarUI();
        new javax.swing.Timer(30_000,e->verificarTodoElStock()).start();
        setExtendedState(JFrame.MAXIMIZED_BOTH);setVisible(true);
        registrarEvento("◈ Sistema GESTRED v9 iniciado");
    }

    private void inicializarDepartamentos(){
        d("Tumbes",-3.567,-80.451,"Costa",2000);d("Piura",-5.194,-80.633,"Costa",4000);
        d("Lambayeque",-6.771,-79.841,"Costa",3500);d("La Libertad",-8.109,-79.021,"Costa",5500);
        d("Ancash",-9.530,-77.528,"Costa",3000);d("Lima",-12.046,-77.043,"Costa",10000);
        d("Callao",-12.050,-77.133,"Costa",5000);d("Ica",-14.068,-75.729,"Costa",3000);
        d("Arequipa",-16.409,-71.538,"Costa",7000);d("Moquegua",-17.192,-70.934,"Costa",1500);
        d("Tacna",-18.006,-70.246,"Costa",1800);d("Cajamarca",-7.162,-78.513,"Sierra",2500);
        d("Huánuco",-9.931,-76.242,"Sierra",2000);d("Pasco",-10.686,-76.256,"Sierra",1200);
        d("Junín",-12.065,-75.204,"Sierra",2800);d("Huancavelica",-12.786,-74.974,"Sierra",1000);
        d("Ayacucho",-13.164,-74.223,"Sierra",1500);d("Apurímac",-13.633,-72.881,"Sierra",1000);
        d("Cusco",-13.532,-71.968,"Sierra",3200);d("Puno",-15.842,-70.020,"Sierra",2000);
        d("Loreto",-3.744,-73.252,"Selva",2200);d("Amazonas",-6.232,-77.869,"Selva",1200);
        d("San Martín",-6.485,-76.373,"Selva",1500);d("Ucayali",-8.379,-74.554,"Selva",1300);
        d("Madre de Dios",-12.593,-69.189,"Selva",800);
    }
    private void d(String n,double lat,double lon,String r,int cap){sedesPeru.put(n,new SedeData(n,lat,lon,r,cap));grafoLogico.agregarSede(n);}

    private void configurarUI(){
        add(crearSidebar(),BorderLayout.WEST);
        panelContenido=new JPanel();cardLayout=new CardLayout();
        panelContenido.setLayout(cardLayout);panelContenido.setBackground(BG_BASE);
        panelContenido.add(crearPanelGrafo(),"red");
        panelContenido.add(crearPanelInventario(),"inventario");
        panelContenido.add(crearPanelRutas(),"rutas");
        panelContenido.add(crearPanelHistorial(),"historial");
        panelContenido.add(crearPanelAlertas(),"alertas");
        panelContenido.add(crearPanelEstadisticas(),"estadisticas");
        add(panelContenido,BorderLayout.CENTER);add(crearBarraEstado(),BorderLayout.SOUTH);
    }

    private JPanel crearSidebar(){
        JPanel p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setBackground(BG_SURFACE);
        p.setBorder(new CompoundBorder(new MatteBorder(0,0,0,1,new Color(CYAN.getRed(),CYAN.getGreen(),CYAN.getBlue(),30)),new EmptyBorder(18,10,18,10)));
        p.setPreferredSize(new Dimension(210,0));
        JLabel logo=new JLabel("◈ GESTRED");logo.setFont(F_LOGO);logo.setForeground(CYAN);logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel ver=new JLabel("v9  ·  CYBERPUNK");ver.setFont(F_SMALL);ver.setForeground(TXT_SECONDARY);ver.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(logo);p.add(Box.createVerticalStrut(3));p.add(ver);p.add(Box.createVerticalStrut(20));p.add(sepNeon());p.add(Box.createVerticalStrut(14));
        for(int i=0;i<NAV_LABELS.length;i++){
            JButton b=navBtn(NAV_LABELS[i]);final String card=NAV_CARDS[i];final int idx=i;
            b.addActionListener(e->{cardLayout.show(panelContenido,card);for(JButton x:navButtons){x.setBackground(BG_CARD2);x.setForeground(TXT_SECONDARY);}navButtons[idx].setBackground(new Color(VIOLET.getRed(),VIOLET.getGreen(),VIOLET.getBlue(),75));navButtons[idx].setForeground(CYAN);});
            navButtons[i]=b;p.add(b);p.add(Box.createVerticalStrut(4));
        }
        p.add(Box.createVerticalGlue());p.add(sepNeon());p.add(Box.createVerticalStrut(12));
        statProductos=statLbl("◫ Productos: 0");statRutas=statLbl("◈ Rutas: 0");statAlertas=statLbl("⚠ Alertas: 0");
        for(JLabel l:new JLabel[]{statProductos,statRutas,statAlertas}){l.setAlignmentX(Component.LEFT_ALIGNMENT);p.add(l);p.add(Box.createVerticalStrut(4));}
        return p;
    }

    private JPanel sepNeon(){
        JPanel s=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(CYAN.getRed(),CYAN.getGreen(),CYAN.getBlue(),40));g2.fillRect(0,1,getWidth(),1);g2.dispose();}};
        s.setOpaque(false);s.setMaximumSize(new Dimension(Integer.MAX_VALUE,3));s.setPreferredSize(new Dimension(0,3));return s;
    }

    // ── Panel Grafo ──────────────────────────────────────────────
    private JPanel crearPanelGrafo(){
        JPanel p=new JPanel(new BorderLayout(0,0));p.setBackground(BG_BASE);
        JPanel header=new JPanel(new BorderLayout());header.setBackground(BG_BASE);header.setBorder(new EmptyBorder(11,18,7,18));
        JLabel tit=new JLabel("◎  RED DE SUMINISTROS — GRAFO INTERACTIVO");tit.setFont(F_TITLE);tit.setForeground(CYAN);
        JLabel info=new JLabel("0 nodos · 0 aristas");info.setFont(F_SMALL);info.setForeground(TXT_SECONDARY);
        header.add(tit,BorderLayout.WEST);header.add(info,BorderLayout.EAST);p.add(header,BorderLayout.NORTH);
        grafoCanvas=new GrafoCanvas();
        JPanel centro=new JPanel(new BorderLayout(0,0));centro.setBackground(BG_BASE);
        centro.add(grafoCanvas,BorderLayout.CENTER);centro.add(crearPanelDijkstra(),BorderLayout.EAST);
        p.add(centro,BorderLayout.CENTER);p.add(crearCtrlGrafo(),BorderLayout.SOUTH);
        new javax.swing.Timer(600,e->info.setText(grafoCanvas.nodos.size()+" nodos · "+grafoCanvas.aristas.size()+" aristas")).start();
        return p;
    }

    private JPanel crearPanelDijkstra(){
        JPanel p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setBackground(BG_PANEL);
        p.setBorder(new CompoundBorder(new MatteBorder(0,1,0,0,new Color(CYAN.getRed(),CYAN.getGreen(),CYAN.getBlue(),28)),new EmptyBorder(14,12,14,12)));
        p.setPreferredSize(new Dimension(238,0));

        JLabel titD=new JLabel("◈ RUTA ÓPTIMA");titD.setFont(F_HEADING);titD.setForeground(CYAN);titD.setAlignmentX(Component.LEFT_ALIGNMENT);p.add(titD);p.add(Box.createVerticalStrut(3));
        JLabel sub=new JLabel("Algoritmo de Dijkstra");sub.setFont(F_SMALL);sub.setForeground(TXT_SECONDARY);sub.setAlignmentX(Component.LEFT_ALIGNMENT);p.add(sub);p.add(Box.createVerticalStrut(11));
        p.add(sepNeon());p.add(Box.createVerticalStrut(11));

        cbDijkOrigen =darkCombo(new String[]{"(sin nodos)"});cbDijkOrigen.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
        cbDijkDestino=darkCombo(new String[]{"(sin nodos)"});cbDijkDestino.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
        p.add(lbl("Origen:"));p.add(Box.createVerticalStrut(4));p.add(cbDijkOrigen);p.add(Box.createVerticalStrut(9));
        p.add(lbl("Destino:"));p.add(Box.createVerticalStrut(4));p.add(cbDijkDestino);p.add(Box.createVerticalStrut(13));

        JButton btnCalc=neonBtn("▶  Calcular Ruta",WHITE_NEON);JButton btnLimp=neonBtn("↺  Limpiar",ORANGE);
        btnCalc.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));btnLimp.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
        p.add(btnCalc);p.add(Box.createVerticalStrut(5));p.add(btnLimp);p.add(Box.createVerticalStrut(11));
        p.add(sepNeon());p.add(Box.createVerticalStrut(11));

        lblRutaResultado=new JLabel("─ sin calcular ─");lblRutaResultado.setFont(F_SMALL);lblRutaResultado.setForeground(TXT_SECONDARY);lblRutaResultado.setAlignmentX(Component.LEFT_ALIGNMENT);p.add(lblRutaResultado);p.add(Box.createVerticalStrut(7));

        areaRutaDetalle=new JTextArea(12,0);areaRutaDetalle.setEditable(false);areaRutaDetalle.setBackground(BG_CARD);
        areaRutaDetalle.setForeground(TXT_PRIMARY);areaRutaDetalle.setFont(new Font("Monospaced",Font.PLAIN,10));
        areaRutaDetalle.setBorder(new EmptyBorder(7,7,7,7));areaRutaDetalle.setLineWrap(true);
        JScrollPane sc=new JScrollPane(areaRutaDetalle);sc.setBorder(new LineBorder(BORDER_DARK,1));sc.getViewport().setBackground(BG_CARD);
        sc.setAlignmentX(Component.LEFT_ALIGNMENT);sc.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));p.add(sc);

        p.add(Box.createVerticalStrut(10));p.add(sepNeon());p.add(Box.createVerticalStrut(8));
        // Leyenda
        for(Object[] row:new Object[][]{{GREEN,"● INICIO"},{WHITE_NEON,"● INTERMEDIO"},{RED,"● FIN"},{WHITE_NEON,"─── Ruta (blanca)"}})
            {JPanel r=leyRow((String)row[1],(Color)row[0]);p.add(r);p.add(Box.createVerticalStrut(4));}

        btnCalc.addActionListener(e->calcDijkstra());
        btnLimp.addActionListener(e->{grafoCanvas.limpiarRuta();lblRutaResultado.setForeground(TXT_SECONDARY);lblRutaResultado.setText("─ sin calcular ─");areaRutaDetalle.setText("");statusLabel.setText("  Ruta limpiada");});
        return p;
    }

    private JPanel leyRow(String txt,Color col){
        JPanel r=new JPanel(new FlowLayout(FlowLayout.LEFT,4,0));r.setBackground(BG_PANEL);r.setMaximumSize(new Dimension(Integer.MAX_VALUE,16));
        JLabel d=new JLabel("■");d.setFont(F_SMALL);d.setForeground(col);
        JLabel t=new JLabel(txt);t.setFont(F_SMALL);t.setForeground(TXT_SECONDARY);
        r.add(d);r.add(t);return r;
    }

    private void calcDijkstra(){
        String ori=(String)cbDijkOrigen.getSelectedItem(),des=(String)cbDijkDestino.getSelectedItem();
        if(ori==null||des==null||ori.startsWith("(")){lblRutaResultado.setForeground(ORANGE);lblRutaResultado.setText("⚠ Agrega nodos primero");return;}
        if(ori.equals(des)){lblRutaResultado.setForeground(ORANGE);lblRutaResultado.setText("⚠ Origen ≠ Destino");return;}
        List<String> ruta=grafoLogico.buscarRutaMasCorta(ori,des);
        if(ruta!=null&&ruta.size()>=2){
            double dist=calcDistRuta(ruta);grafoCanvas.resaltarRuta(ruta);
            lblRutaResultado.setForeground(WHITE_NEON);
            lblRutaResultado.setText("✔ "+String.format("%.0f km",dist)+" · "+ruta.size()+" nodos");
            StringBuilder sb=new StringBuilder("CAMINO:\n");
            for(int i=0;i<ruta.size();i++){
                sb.append(i==0?"  [INICIO]  ":i==ruta.size()-1?"  [ FIN  ]  ":"  [  "+i+"   ]  ").append(ruta.get(i));
                if(i<ruta.size()-1)sb.append("\n      ↓ "+String.format("%.0f km",tramoDist(ruta.get(i),ruta.get(i+1)))+"\n");
            }
            sb.append("\n──────────────────\nTOTAL: "+String.format("%.0f km",dist));
            areaRutaDetalle.setText(sb.toString());areaRutaDetalle.setForeground(TXT_PRIMARY);areaRutaDetalle.setCaretPosition(0);
            statusLabel.setText("  ✔ "+String.join("→",ruta)+" | "+String.format("%.0f km",dist));
            registrarEvento("Dijkstra: "+ori+"→"+des+" ("+String.format("%.0f km",dist)+")");
        }else{
            grafoCanvas.limpiarRuta();lblRutaResultado.setForeground(RED);lblRutaResultado.setText("❌ Sin ruta disponible");
            areaRutaDetalle.setText("Sin camino entre\n«"+ori+"» y\n«"+des+"».\n\nAgrega conexiones primero.");areaRutaDetalle.setForeground(RED);
            statusLabel.setText("  ❌ Sin ruta: "+ori+"→"+des);
        }
    }

    private double tramoDist(String a,String b){
        return grafoCanvas.aristas.stream().filter(ar->(ar.a.nombre.equals(a)&&ar.b.nombre.equals(b))||(ar.a.nombre.equals(b)&&ar.b.nombre.equals(a))).mapToDouble(ar->ar.peso).findFirst().orElse(0);
    }
    private double calcDistRuta(List<String> r){double t=0;for(int i=0;i<r.size()-1;i++)t+=tramoDist(r.get(i),r.get(i+1));return t;}

    void actualizarCombosDijkstra(){
        if(cbDijkOrigen==null)return;
        String[] nm=grafoCanvas.nodos.stream().map(n->n.nombre).toArray(String[]::new);
        String so=(String)cbDijkOrigen.getSelectedItem(),sd=(String)cbDijkDestino.getSelectedItem();
        cbDijkOrigen.setModel(new DefaultComboBoxModel<>(nm.length>0?nm:new String[]{"(sin nodos)"}));
        cbDijkDestino.setModel(new DefaultComboBoxModel<>(nm.length>0?nm:new String[]{"(sin nodos)"}));
        if(so!=null)for(int i=0;i<cbDijkOrigen.getItemCount();i++)if(cbDijkOrigen.getItemAt(i).equals(so)){cbDijkOrigen.setSelectedIndex(i);break;}
        if(sd!=null)for(int i=0;i<cbDijkDestino.getItemCount();i++)if(cbDijkDestino.getItemAt(i).equals(sd)){cbDijkDestino.setSelectedIndex(i);break;}
    }

    private JPanel crearCtrlGrafo(){
        JPanel ctrl=new JPanel(new BorderLayout(0,0));ctrl.setBackground(BG_SURFACE);
        ctrl.setBorder(new CompoundBorder(new MatteBorder(1,0,0,0,new Color(CYAN.getRed(),CYAN.getGreen(),CYAN.getBlue(),28)),new EmptyBorder(8,14,8,14)));
        JPanel fila=new JPanel(new FlowLayout(FlowLayout.LEFT,10,3));fila.setBackground(BG_SURFACE);

        JPanel gN=grupo("◉ NUEVO NODO");JTextField tfN=darkField(14);JButton baN=neonBtn("+ Agregar",CYAN);
        gN.add(lbl("Nombre:"));gN.add(tfN);gN.add(baN);fila.add(gN);fila.add(sepV());

        JPanel gA=grupo("◈ NUEVA CONEXIÓN");JTextField tfO=darkField(10),tfD=darkField(10),tfKm=darkField(6);
        JButton baC=neonBtn("+ Conectar",GREEN),baCC=neonBtn("⊕ Clic-Clic",PINK);
        gA.add(lbl("Origen:"));gA.add(tfO);gA.add(lbl("Destino:"));gA.add(tfD);gA.add(lbl("km:"));gA.add(tfKm);gA.add(baC);gA.add(baCC);
        fila.add(gA);fila.add(sepV());

        JPanel gX=grupo("◈ ACCIONES");JButton baBorrar=neonBtn("✕ Limpiar Grafo",RED);gX.add(baBorrar);fila.add(gX);
        ctrl.add(fila,BorderLayout.NORTH);
        statusLabel=new JLabel("  ◈ Arrastra nodos · 2×clic info · Panel derecho: Dijkstra");
        statusLabel.setFont(F_SMALL);statusLabel.setForeground(TXT_SECONDARY);ctrl.add(statusLabel,BorderLayout.SOUTH);

        ActionListener acN=e->{String n=tfN.getText().trim();if(n.isEmpty()){statusLabel.setText("  ⚠ Escribe el nombre");return;}
            if(grafoCanvas.nodoPorNombre(n)!=null){JOptionPane.showMessageDialog(ctrl,"Ya existe '"+n+"'.","Duplicado",JOptionPane.WARNING_MESSAGE);return;}
            grafoCanvas.agregarNodo(n);tfN.setText("");statusLabel.setText("  ✔ Nodo '"+n+"' agregado");};
        tfN.addActionListener(acN);baN.addActionListener(acN);

        ActionListener acC=e->{String o=tfO.getText().trim(),d=tfD.getText().trim(),km=tfKm.getText().trim();
            if(o.isEmpty()||d.isEmpty()||km.isEmpty()){statusLabel.setText("  ⚠ Completa Origen, Destino y km");return;}
            if(o.equalsIgnoreCase(d)){statusLabel.setText("  ⚠ Origen ≠ Destino");return;}
            NodoVisual na=grafoCanvas.nodoPorNombre(o),nb=grafoCanvas.nodoPorNombre(d);
            if(na==null){statusLabel.setText("  ⚠ Nodo '"+o+"' no existe");return;}
            if(nb==null){statusLabel.setText("  ⚠ Nodo '"+d+"' no existe");return;}
            try{double dv=Double.parseDouble(km);if(dv<=0)throw new NumberFormatException();
                grafoCanvas.agregarArista(na,nb,dv);tfO.setText("");tfD.setText("");tfKm.setText("");
                statusLabel.setText("  ✔ "+o+"↔"+d+" ("+dv+"km)");
            }catch(NumberFormatException ex){statusLabel.setText("  ⚠ Distancia positiva requerida");}};
        tfKm.addActionListener(acC);baC.addActionListener(acC);

        baCC.addActionListener(e->{grafoCanvas.modoConectar=!grafoCanvas.modoConectar;
            if(grafoCanvas.modoConectar){grafoCanvas.nodoConA=null;baCC.setText("✕ Cancelar");statusLabel.setText("  ◈ CLIC-A-CLIC activo — haz clic en ORIGEN");}
            else{if(grafoCanvas.nodoConA!=null)grafoCanvas.nodoConA.seleccionado=false;grafoCanvas.nodoConA=null;baCC.setText("⊕ Clic-Clic");statusLabel.setText("  Modo conectar cancelado");}});

        grafoCanvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"),"esc");
        grafoCanvas.getActionMap().put("esc",new AbstractAction(){@Override public void actionPerformed(ActionEvent e){
            if(grafoCanvas.modoConectar){if(grafoCanvas.nodoConA!=null)grafoCanvas.nodoConA.seleccionado=false;
                grafoCanvas.nodoConA=null;grafoCanvas.modoConectar=false;baCC.setText("⊕ Clic-Clic");statusLabel.setText("  Cancelado (ESC)");}}});

        baBorrar.addActionListener(e->{if(JOptionPane.showConfirmDialog(ctrl,"¿Limpiar todo?","Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            grafoCanvas.limpiarGrafo();statusLabel.setText("  Grafo limpiado");
            if(areaRutaDetalle!=null)areaRutaDetalle.setText("");
            if(lblRutaResultado!=null){lblRutaResultado.setForeground(TXT_SECONDARY);lblRutaResultado.setText("─ sin calcular ─");}}});
        return ctrl;
    }

    // ── Inventario ───────────────────────────────────────────────
    private JPanel crearPanelInventario(){
        JPanel p=new JPanel(new BorderLayout());p.setBackground(BG_BASE);p.setBorder(new EmptyBorder(16,16,16,16));
        p.add(tit("◫  GESTIÓN DE INVENTARIO"),BorderLayout.NORTH);
        JPanel form=new JPanel(new GridBagLayout());form.setBackground(BG_CARD);
        form.setBorder(new CompoundBorder(new LineBorder(BORDER_DARK),new EmptyBorder(14,14,14,14)));
        GridBagConstraints c=new GridBagConstraints();c.insets=new Insets(4,6,4,6);c.fill=GridBagConstraints.HORIZONTAL;
        JTextField tfS=darkField(8),tfN=darkField(14),tfC=darkField(7),tfCa=darkField(10);
        JComboBox<String> cbSede=darkCombo(sedesPeru.keySet().toArray(new String[0]));
        c.gridx=0;c.gridy=0;form.add(lbl("SKU:"),c);c.gridx=1;form.add(tfS,c);c.gridx=2;form.add(lbl("Nombre:"),c);c.gridx=3;form.add(tfN,c);
        c.gridx=0;c.gridy=1;form.add(lbl("Cantidad:"),c);c.gridx=1;form.add(tfC,c);c.gridx=2;form.add(lbl("Categoría:"),c);c.gridx=3;form.add(tfCa,c);
        c.gridx=0;c.gridy=2;form.add(lbl("Depto.:"),c);c.gridx=1;c.gridwidth=3;form.add(cbSede,c);
        JPanel bts=new JPanel(new FlowLayout(FlowLayout.CENTER,10,4));bts.setBackground(BG_CARD);
        JButton bA=neonBtn("+ Agregar",GREEN),bB=neonBtn("⌕ Buscar",CYAN),bE=neonBtn("✕ Eliminar",RED),bU=neonBtn("↺ Actualizar",PURPLE_SOFT);
        bA.addActionListener(e->agregarProd(tfS,tfN,tfC,tfCa,cbSede));bB.addActionListener(e->buscarProd(tfS.getText().trim()));
        bE.addActionListener(e->eliminarProd(tfS.getText().trim()));bU.addActionListener(e->actualizarStock(tfS.getText().trim(),tfC.getText().trim(),cbSede));
        for(JButton b:new JButton[]{bA,bB,bE,bU})bts.add(b);c.gridx=0;c.gridy=3;c.gridwidth=4;form.add(bts,c);
        p.add(form,BorderLayout.NORTH);
        String[] cols={"SKU","Producto","Cantidad","Categoría","Departamento","Stock Depto."};
        modeloInventario=new DefaultTableModel(cols,0){@Override public boolean isCellEditable(int r,int cc){return false;}};
        JTable tab=tabla(modeloInventario);JScrollPane sc=new JScrollPane(tab);sc.setBorder(new LineBorder(BORDER_DARK));sc.getViewport().setBackground(BG_CARD);
        p.add(sc,BorderLayout.CENTER);return p;
    }

    private void agregarProd(JTextField tfS,JTextField tfN,JTextField tfC,JTextField tfCa,JComboBox<String> cbS){
        try{int sku=Integer.parseInt(tfS.getText().trim());String nom=tfN.getText().trim(),cat=tfCa.getText().trim();int cnt=Integer.parseInt(tfC.getText().trim());String sede=(String)cbS.getSelectedItem();
            if(nom.isEmpty()||cat.isEmpty()){msg("Completa todos los campos.");return;}if(cnt<0){msg("Cantidad no negativa.");return;}
            if(bst.buscar(sku)!=null){msg("SKU "+sku+" ya existe.");return;}
            Producto pr=new Producto(sku,nom,cnt,cat);bst.insertar(pr);SedeData sd=sedesPeru.get(sede);sd.stockActual+=cnt;sd.productos.add(pr);
            modeloInventario.addRow(new Object[]{sku,nom,cnt,cat,sede,sd.stockActual});cntProductos++;actualizarStats();
            registrarEvento("Producto: "+nom+" SKU"+sku+" en "+sede);verificarStockCritico(sede,pr);
            tfS.setText("");tfN.setText("");tfC.setText("");tfCa.setText("");
        }catch(NumberFormatException ex){msg("SKU y cantidad enteros.");}
    }
    private void buscarProd(String s){
        try{int sku=Integer.parseInt(s);Producto pr=bst.buscar(sku);if(pr==null){msg("SKU "+sku+" no encontrado.");return;}
            StringBuilder sb=new StringBuilder("SKU: "+pr.getSku()+"\nNombre: "+pr.getNombre()+"\nCantidad: "+pr.getCantidad()+"\nCategoría: "+pr.getCategoria()+"\n\nDistribución:\n");
            sedesPeru.forEach((n,sd)->sd.productos.stream().filter(pp->pp.getSku()==sku).forEach(pp->sb.append("  • "+n+": "+pp.getCantidad()+" uds\n")));
            JTextArea ta=new JTextArea(sb.toString());ta.setEditable(false);ta.setFont(F_MONO);JOptionPane.showMessageDialog(this,new JScrollPane(ta),"Búsqueda",JOptionPane.INFORMATION_MESSAGE);
        }catch(NumberFormatException ex){msg("SKU numérico requerido.");}
    }
    private void eliminarProd(String s){
        try{int sku=Integer.parseInt(s);Producto pr=bst.buscar(sku);if(pr==null){msg("No encontrado.");return;}
            if(JOptionPane.showConfirmDialog(this,"¿Eliminar «"+pr.getNombre()+"»?","Confirmar",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            bst.eliminar(sku);sedesPeru.values().forEach(sd->sd.productos.stream().filter(pp->pp.getSku()==sku).findFirst().ifPresent(pp->{sd.stockActual-=pp.getCantidad();sd.productos.remove(pp);}));
            for(int i=0;i<modeloInventario.getRowCount();i++)if((int)modeloInventario.getValueAt(i,0)==sku){modeloInventario.removeRow(i);break;}
            cntProductos--;actualizarStats();registrarEvento("Eliminado: "+pr.getNombre());
        }catch(NumberFormatException ex){msg("SKU numérico.");}
    }
    private void actualizarStock(String s,String cs,JComboBox<String> cbS){
        try{int sku=Integer.parseInt(s),nv=Integer.parseInt(cs);if(nv<0)throw new NumberFormatException();
            String sede=(String)cbS.getSelectedItem();Producto pr=bst.buscar(sku);if(pr==null){msg("No encontrado.");return;}
            SedeData sd=sedesPeru.get(sede);int ant=pr.getCantidad();pr.setCantidad(nv);sd.stockActual=sd.stockActual-ant+nv;
            for(int i=0;i<modeloInventario.getRowCount();i++)if((int)modeloInventario.getValueAt(i,0)==sku){modeloInventario.setValueAt(nv,i,2);modeloInventario.setValueAt(sd.stockActual,i,5);break;}
            registrarEvento("Stock: "+pr.getNombre()+"→"+nv+" en "+sede);verificarStockCritico(sede,pr);
        }catch(NumberFormatException ex){msg("Enteros no negativos.");}
    }
    private void verificarStockCritico(String sede,Producto p){
        if(p.getCantidad()<nivelCriticoGlobal){
            AlertaStock a=new AlertaStock(sede,p.getNombre(),p.getSku(),p.getCantidad(),nivelCriticoGlobal);
            alertasStock.add(a);cntAlertas++;actualizarStats();
            if(modeloAlertas!=null)modeloAlertas.addRow(new Object[]{a.sede,a.producto,a.sku,a.nivelActual,a.nivelCritico,a.prioridad,a.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))});
            registrarEvento("⚠ Stock crítico: "+p.getNombre()+" en "+sede);
        }
    }
    private void verificarTodoElStock(){sedesPeru.forEach((n,sd)->sd.productos.forEach(p->verificarStockCritico(n,p)));}

    // ── Panel Rutas ──────────────────────────────────────────────
    private JPanel crearPanelRutas(){
        JPanel p=new JPanel(new BorderLayout());p.setBackground(BG_BASE);p.setBorder(new EmptyBorder(16,16,16,16));
        p.add(tit("◈  BUSCADOR DE RUTAS ÓPTIMAS  —  Dijkstra"),BorderLayout.NORTH);
        JPanel busq=new JPanel(new FlowLayout(FlowLayout.LEFT,12,8));busq.setBackground(BG_CARD);
        busq.setBorder(new CompoundBorder(new LineBorder(BORDER_DARK),new EmptyBorder(10,12,10,12)));
        JTextField tfO=darkField(14),tfD2=darkField(14);busq.add(lbl("Origen:"));busq.add(tfO);busq.add(lbl("Destino:"));busq.add(tfD2);
        JButton bBus=neonBtn("▶ Calcular",WHITE_NEON),bL=neonBtn("↺ Limpiar",ORANGE);busq.add(bBus);busq.add(bL);p.add(busq,BorderLayout.NORTH);
        JTextArea area=new JTextArea();area.setEditable(false);area.setBackground(BG_CARD2);area.setForeground(TXT_PRIMARY);area.setFont(F_MONO);area.setBorder(new EmptyBorder(12,12,12,12));
        area.setText("  Agrega nodos y conexiones en 'Red de Suministros'.\n  Luego escribe origen y destino y presiona Calcular.\n\n  También puedes usar el panel lateral del Grafo directamente.");
        JScrollPane sc=new JScrollPane(area);sc.setBorder(new LineBorder(BORDER_DARK));p.add(sc,BorderLayout.CENTER);
        bBus.addActionListener(e->{
            String ori=tfO.getText().trim(),des=tfD2.getText().trim();
            if(ori.equals(des)){area.setText("⚠  Origen y destino distintos.");area.setForeground(ORANGE);return;}
            List<String> ruta=grafoLogico.buscarRutaMasCorta(ori,des);
            if(ruta!=null&&!ruta.isEmpty()){
                double dist=calcDistRuta(ruta);
                StringBuilder sb=new StringBuilder("╔══════════════════════════════════╗\n║  ✔  RUTA ENCONTRADA             ║\n╚══════════════════════════════════╝\n\n");
                sb.append("  Desde  : ").append(ori).append("\n  Hasta  : ").append(des).append("\n\n  Recorrido:\n");
                for(int i=0;i<ruta.size();i++)sb.append(i==0?"    [INICIO]  ":i==ruta.size()-1?"    [ FIN  ]  ":"    [  "+(i)+"    ]  ").append(ruta.get(i)).append(i<ruta.size()-1?"\n          ↓ "+String.format("%.0f km",tramoDist(ruta.get(i),ruta.get(i+1)))+"\n":"\n");
                sb.append("\n  Paradas  : "+ruta.size()+"\n  Total    : "+String.format("%.0f km",dist));
                area.setText(sb.toString());area.setForeground(WHITE_NEON);
                grafoCanvas.resaltarRuta(ruta);
                cardLayout.show(panelContenido,"red");for(JButton b:navButtons)b.setBackground(BG_CARD2);
                navButtons[0].setBackground(new Color(VIOLET.getRed(),VIOLET.getGreen(),VIOLET.getBlue(),75));
                registrarEvento("Dijkstra: "+ori+"→"+des);
            }else{area.setText("  ❌  Sin ruta entre «"+ori+"» y «"+des+"».\n  Agrega las conexiones primero.");area.setForeground(RED);}
        });
        bL.addActionListener(e->{area.setText("");grafoCanvas.limpiarRuta();});return p;
    }

    // ── Historial ────────────────────────────────────────────────
    private JPanel crearPanelHistorial(){
        JPanel p=new JPanel(new BorderLayout());p.setBackground(BG_BASE);p.setBorder(new EmptyBorder(16,16,16,16));
        p.add(tit("▤  HISTORIAL DE OPERACIONES"),BorderLayout.NORTH);
        listaEventos=new JList<>(modeloEventos);listaEventos.setFont(F_BODY);listaEventos.setBackground(BG_CARD);listaEventos.setForeground(TXT_PRIMARY);
        listaEventos.setSelectionBackground(VIOLET);listaEventos.setFixedCellHeight(32);
        listaEventos.setCellRenderer(new DefaultListCellRenderer(){@Override public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean s,boolean f){
            JLabel lb=(JLabel)super.getListCellRendererComponent(l,v,i,s,f);lb.setBorder(new EmptyBorder(0,12,0,0));if(!s)lb.setBackground(i%2==0?BG_CARD:BG_CARD2);return lb;}});
        JScrollPane sc=new JScrollPane(listaEventos);sc.setBorder(new LineBorder(BORDER_DARK));sc.getViewport().setBackground(BG_CARD);p.add(sc,BorderLayout.CENTER);
        JPanel ctrl=new JPanel(new FlowLayout(FlowLayout.LEFT,10,8));ctrl.setBackground(BG_SURFACE);
        JButton bAt=neonBtn("◀ Anterior",PURPLE_SOFT),bAd=neonBtn("Siguiente ▶",PURPLE_SOFT),bM=neonBtn("+ Manual",CYAN),bLi=neonBtn("✕ Limpiar",RED);
        bAt.addActionListener(e->{historial.navegarAtras();int i=listaEventos.getSelectedIndex();if(i>0){listaEventos.setSelectedIndex(i-1);listaEventos.ensureIndexIsVisible(i-1);}});
        bAd.addActionListener(e->{historial.navegarAdelante();int i=listaEventos.getSelectedIndex();if(i<modeloEventos.size()-1){listaEventos.setSelectedIndex(i+1);listaEventos.ensureIndexIsVisible(i+1);}});
        bM.addActionListener(e->{String ev=JOptionPane.showInputDialog(this,"Descripción:");if(ev!=null&&!ev.isBlank())registrarEvento(ev.trim());});
        bLi.addActionListener(e->{if(JOptionPane.showConfirmDialog(this,"¿Limpiar historial?","Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)modeloEventos.clear();});
        for(JButton b:new JButton[]{bAt,bAd,bM,bLi})ctrl.add(b);p.add(ctrl,BorderLayout.SOUTH);return p;
    }

    // ── Alertas ──────────────────────────────────────────────────
    private JPanel crearPanelAlertas(){
        JPanel p=new JPanel(new BorderLayout());p.setBackground(BG_BASE);p.setBorder(new EmptyBorder(16,16,16,16));
        p.add(tit("⚠  ALERTAS DE STOCK CRÍTICO"),BorderLayout.NORTH);
        String[] cols={"Departamento","Producto","SKU","Stock","Nivel Crítico","Prioridad","Fecha"};
        modeloAlertas=new DefaultTableModel(cols,0){@Override public boolean isCellEditable(int r,int cc){return false;}};
        JTable tab=tabla(modeloAlertas);
        tab.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){@Override public Component getTableCellRendererComponent(JTable tb,Object v,boolean s,boolean f,int r,int cc){
            JLabel lb=(JLabel)super.getTableCellRendererComponent(tb,v,s,f,r,cc);lb.setBorder(new EmptyBorder(0,8,0,0));
            if(!s&&tb.getValueAt(r,5)!=null){switch(tb.getValueAt(r,5).toString()){
                case"CRÍTICA":lb.setBackground(new Color(0x50,0x06,0x14));lb.setForeground(RED);break;
                case"ALTA":lb.setBackground(new Color(0x50,0x26,0x00));lb.setForeground(ORANGE);break;
                case"MEDIA":lb.setBackground(new Color(0x46,0x36,0x00));lb.setForeground(YELLOW);break;
                default:lb.setBackground(BG_CARD);lb.setForeground(TXT_PRIMARY);}}
            else if(!s){lb.setBackground(BG_CARD);lb.setForeground(TXT_PRIMARY);}return lb;}});
        JScrollPane sc=new JScrollPane(tab);sc.setBorder(new LineBorder(BORDER_DARK));sc.getViewport().setBackground(BG_CARD);p.add(sc,BorderLayout.CENTER);
        JPanel ctrl=new JPanel(new FlowLayout(FlowLayout.LEFT,10,8));ctrl.setBackground(BG_SURFACE);
        JButton bAt=neonBtn("✔ Atender",GREEN),bCf=neonBtn("⚙ Config",CYAN),bRf=neonBtn("↺ Refrescar",PURPLE_SOFT);
        bAt.addActionListener(e->{int row=tab.getSelectedRow();if(row<0){msg("Selecciona una alerta.");return;}modeloAlertas.removeRow(row);if(row<alertasStock.size())alertasStock.remove(row);cntAlertas=Math.max(0,cntAlertas-1);actualizarStats();registrarEvento("Alerta atendida");});
        bCf.addActionListener(e->{String in=JOptionPane.showInputDialog(this,"Nivel crítico global:",String.valueOf(nivelCriticoGlobal));if(in==null)return;try{int n=Integer.parseInt(in.trim());if(n>0){nivelCriticoGlobal=n;registrarEvento("Nivel crítico→"+n);}}catch(NumberFormatException ex){msg("Número positivo.");}});
        bRf.addActionListener(e->{modeloAlertas.setRowCount(0);alertasStock.stream().filter(a->!a.atendida).forEach(a->modeloAlertas.addRow(new Object[]{a.sede,a.producto,a.sku,a.nivelActual,a.nivelCritico,a.prioridad,a.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}));});
        for(JButton b:new JButton[]{bAt,bCf,bRf})ctrl.add(b);p.add(ctrl,BorderLayout.SOUTH);return p;
    }

    // ── Estadísticas REACTIVAS ───────────────────────────────────
    private JPanel crearPanelEstadisticas(){
        JPanel p=new JPanel(new BorderLayout());p.setBackground(BG_BASE);p.setBorder(new EmptyBorder(16,16,16,16));
        p.add(tit("▣  ESTADÍSTICAS  —  Actualización en Tiempo Real"),BorderLayout.NORTH);
        JPanel cards=new JPanel(new GridLayout(2,3,13,13));cards.setBackground(BG_BASE);
        scProductos=new JLabel("0");scRutas=new JLabel("0");scAlertas=new JLabel("0");
        scDepartamentos=new JLabel(String.valueOf(sedesPeru.size()));scEventos=new JLabel("0");scStock=new JLabel("0");
        cards.add(sCardDyn("◫ Productos",    scProductos,    GREEN));
        cards.add(sCardDyn("◈ Conexiones",   scRutas,        CYAN));
        cards.add(sCardDyn("⚠ Alertas",      scAlertas,      RED));
        cards.add(sCardDyn("◉ Departamentos",scDepartamentos,PURPLE_SOFT));
        cards.add(sCardDyn("▤ Eventos Log",  scEventos,      ORANGE));
        cards.add(sCardDyn("▣ Stock Total",  scStock,        YELLOW));
        p.add(cards,BorderLayout.CENTER);
        JPanel inf=new JPanel(new GridLayout(1,2,13,0));inf.setBackground(BG_BASE);inf.setBorder(new EmptyBorder(13,0,0,0));
        JTextArea taTop=darkArea(),taCat=darkArea();
        inf.add(subP("▶ Top Departamentos por Stock",taTop));inf.add(subP("▶ Categorías Populares",taCat));p.add(inf,BorderLayout.SOUTH);
        // Timer reactivo
        new javax.swing.Timer(800,e->{
            if(scProductos==null)return;
            scProductos.setText(String.valueOf(cntProductos));scRutas.setText(String.valueOf(cntRutas));
            scAlertas.setText(String.valueOf(cntAlertas));scDepartamentos.setText(String.valueOf(sedesPeru.size()));
            scEventos.setText(String.valueOf(modeloEventos.size()));
            scStock.setText(String.valueOf(sedesPeru.values().stream().mapToInt(s->s.stockActual).sum()));
            String topS=sedesPeru.entrySet().stream().sorted((a,b)->Integer.compare(b.getValue().stockActual,a.getValue().stockActual)).limit(6)
                .map(ev->String.format("  %-16s %,5d uds",ev.getKey(),ev.getValue().stockActual)).collect(Collectors.joining("\n"));
            taTop.setText(topS.isEmpty()?"  (sin datos)":topS);
            Map<String,Long> cats=new HashMap<>();
            if(modeloInventario!=null)for(int i=0;i<modeloInventario.getRowCount();i++)cats.merge((String)modeloInventario.getValueAt(i,3),1L,Long::sum);
            String topC=cats.entrySet().stream().sorted((a,b)->Long.compare(b.getValue(),a.getValue())).limit(6)
                .map(ev->String.format("  %-16s %d prods",ev.getKey(),ev.getValue())).collect(Collectors.joining("\n"));
            taCat.setText(topC.isEmpty()?"  (sin datos)":topC);
        }).start();
        return p;
    }

    private JPanel crearBarraEstado(){
        JPanel b=new JPanel(new FlowLayout(FlowLayout.LEFT,16,4));b.setBackground(BG_SURFACE);
        b.setBorder(new MatteBorder(1,0,0,0,new Color(CYAN.getRed(),CYAN.getGreen(),CYAN.getBlue(),28)));
        JLabel lE=new JLabel("● ONLINE"),lF=new JLabel(),lU=new JLabel("Usuario: Administrador"),lV=new JLabel("GESTRED v9");
        lE.setFont(F_SMALL);lE.setForeground(GREEN);lF.setFont(F_SMALL);lF.setForeground(TXT_SECONDARY);
        lU.setFont(F_SMALL);lU.setForeground(TXT_SECONDARY);lV.setFont(F_SMALL);lV.setForeground(new Color(CYAN.getRed(),CYAN.getGreen(),CYAN.getBlue(),70));
        b.add(lE);b.add(vsep());b.add(lF);b.add(vsep());b.add(lU);b.add(vsep());b.add(lV);
        new javax.swing.Timer(1000,e->lF.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss")))).start();
        return b;
    }

    // ── Helpers ──────────────────────────────────────────────────
    private void registrarEvento(String d){historial.registrar(d);String ts=LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        modeloEventos.addElement("["+ts+"]  "+d);if(listaEventos!=null){int l=modeloEventos.size()-1;listaEventos.setSelectedIndex(l);listaEventos.ensureIndexIsVisible(l);}}
    private void actualizarStats(){if(statProductos==null)return;statProductos.setText("◫ Productos: "+cntProductos);statRutas.setText("◈ Rutas: "+cntRutas);statAlertas.setText("⚠ Alertas: "+cntAlertas);}
    private void msg(String m){JOptionPane.showMessageDialog(this,m,"Aviso",JOptionPane.WARNING_MESSAGE);}

    private JLabel tit(String t){JLabel l=new JLabel(t);l.setFont(F_TITLE);l.setForeground(CYAN);l.setBorder(new EmptyBorder(0,0,12,0));return l;}
    private JLabel lbl(String t){JLabel l=new JLabel(t);l.setFont(F_LABEL);l.setForeground(TXT_SECONDARY);return l;}
    private JLabel statLbl(String t){JLabel l=new JLabel(t);l.setFont(F_SMALL);l.setForeground(TXT_SECONDARY);return l;}

    private JButton neonBtn(String text,Color neon){
        JButton b=new JButton(text){@Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover()?new Color(neon.getRed(),neon.getGreen(),neon.getBlue(),55):new Color(neon.getRed(),neon.getGreen(),neon.getBlue(),16));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
            g2.setColor(new Color(neon.getRed(),neon.getGreen(),neon.getBlue(),getModel().isRollover()?200:110));
            g2.setStroke(new BasicStroke(1.4f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);g2.dispose();super.paintComponent(g);}};
        b.setFont(F_LABEL);b.setForeground(neon);b.setBackground(new Color(0,0,0,0));b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){b.setForeground(Color.WHITE);b.repaint();}@Override public void mouseExited(MouseEvent e){b.setForeground(neon);b.repaint();}});
        return b;
    }
    private JButton navBtn(String text){
        JButton b=new JButton(text);b.setFont(F_NAV);b.setForeground(TXT_SECONDARY);b.setBackground(BG_CARD2);b.setFocusPainted(false);b.setBorderPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);b.setBorder(new EmptyBorder(8,10,8,8));b.setMaximumSize(new Dimension(192,40));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){if(b.getForeground()!=CYAN){b.setForeground(CYAN);b.repaint();}}@Override public void mouseExited(MouseEvent e){if(b.getForeground()!=CYAN){b.setForeground(TXT_SECONDARY);b.repaint();}}});
        return b;
    }
    private JTextField darkField(int cols){JTextField tf=new JTextField(cols);tf.setBackground(BG_CARD2);tf.setForeground(TXT_PRIMARY);tf.setCaretColor(CYAN);tf.setFont(F_BODY);tf.setBorder(new CompoundBorder(new LineBorder(new Color(CYAN.getRed(),CYAN.getGreen(),CYAN.getBlue(),45),1),new EmptyBorder(3,6,3,6)));return tf;}
    private <T> JComboBox<T> darkCombo(T[] items){JComboBox<T> cb=new JComboBox<>(items);cb.setBackground(BG_CARD2);cb.setForeground(TXT_PRIMARY);cb.setFont(F_BODY);return cb;}
    private JTable tabla(DefaultTableModel m){
        JTable t=new JTable(m);t.setFont(F_BODY);t.setRowHeight(28);t.setBackground(BG_CARD);t.setForeground(TXT_PRIMARY);t.setGridColor(BORDER_DARK);t.setSelectionBackground(VIOLET);t.setSelectionForeground(TXT_WHITE);
        t.setShowHorizontalLines(true);t.setShowVerticalLines(false);t.getTableHeader().setFont(F_NAV);t.getTableHeader().setBackground(BG_CARD2);t.getTableHeader().setForeground(CYAN);t.getTableHeader().setBorder(new MatteBorder(0,0,1,0,BORDER_DARK));return t;
    }
    private JTextArea darkArea(){JTextArea ta=new JTextArea();ta.setEditable(false);ta.setFont(F_MONO);ta.setBackground(BG_CARD2);ta.setForeground(TXT_PRIMARY);ta.setBorder(new EmptyBorder(8,8,8,8));return ta;}
    private JSeparator vsep(){JSeparator s=new JSeparator(SwingConstants.VERTICAL);s.setPreferredSize(new Dimension(1,13));s.setForeground(BORDER_DARK);return s;}

    private JPanel sCardDyn(String titulo,JLabel valLabel,Color col){
        JPanel c=new JPanel(new BorderLayout());c.setBackground(BG_CARD);
        c.setBorder(new CompoundBorder(new LineBorder(new Color(col.getRed(),col.getGreen(),col.getBlue(),80),1),new EmptyBorder(14,16,14,16)));
        JPanel bar=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(col.getRed(),col.getGreen(),col.getBlue(),55));g2.fillRect(0,0,getWidth(),3);g2.dispose();}};
        bar.setOpaque(false);bar.setPreferredSize(new Dimension(0,3));
        JLabel lt=new JLabel(titulo);lt.setFont(F_SMALL);lt.setForeground(TXT_SECONDARY);
        valLabel.setFont(F_STAT);valLabel.setForeground(col);
        c.add(bar,BorderLayout.NORTH);c.add(lt,BorderLayout.CENTER);c.add(valLabel,BorderLayout.SOUTH);return c;
    }
    private JPanel subP(String titulo,JTextArea ta){
        JPanel p=new JPanel(new BorderLayout());p.setBackground(BG_CARD);p.setBorder(new CompoundBorder(new LineBorder(BORDER_DARK),new EmptyBorder(12,12,12,12)));
        JLabel l=new JLabel(titulo);l.setFont(F_HEADING);l.setForeground(CYAN);l.setBorder(new EmptyBorder(0,0,8,0));p.add(l,BorderLayout.NORTH);
        JScrollPane sc=new JScrollPane(ta);sc.setBorder(null);sc.getViewport().setBackground(BG_CARD2);p.add(sc,BorderLayout.CENTER);return p;
    }
    private JPanel grupo(String t){
        JPanel g=new JPanel(new FlowLayout(FlowLayout.LEFT,4,2));g.setBackground(BG_SURFACE);
        g.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(new Color(CYAN.getRed(),CYAN.getGreen(),CYAN.getBlue(),40),1),t,TitledBorder.LEFT,TitledBorder.TOP,new Font("Monospaced",Font.BOLD,8),TXT_SECONDARY),new EmptyBorder(2,4,4,4)));
        return g;
    }
    private JComponent sepV(){JPanel s=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(CYAN.getRed(),CYAN.getGreen(),CYAN.getBlue(),28));g2.fillRect(0,4,1,getHeight()-8);g2.dispose();}};s.setOpaque(false);s.setPreferredSize(new Dimension(10,52));return s;}

    public static void main(String[] args){
        try{UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());}catch(Exception ignored){}
        UIManager.put("OptionPane.background",BG_CARD);UIManager.put("Panel.background",BG_CARD);
        UIManager.put("OptionPane.messageForeground",TXT_PRIMARY);UIManager.put("Button.background",BG_CARD2);
        UIManager.put("Button.foreground",TXT_PRIMARY);UIManager.put("TextField.background",BG_CARD2);
        UIManager.put("TextField.foreground",TXT_PRIMARY);UIManager.put("ComboBox.background",BG_CARD2);UIManager.put("ComboBox.foreground",TXT_PRIMARY);
        SwingUtilities.invokeLater(VentanaPrincipalEjecutable::new);
    }
}