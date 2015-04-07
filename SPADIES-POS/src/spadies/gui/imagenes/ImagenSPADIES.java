/*
 * Centro de Estudios sobre Desarrollo Económico (CEDE) - Universidad de los Andes
 * Octubre 18 de 2006
 *
 *********************************************************
 * SPADIES                                               *
 * Sistema para la Prevención y Análisis de la Deserción *
 * en las Instituciones de Educación Superior            *
 *********************************************************
 * Autores del código fuente (última versión):           *
 *  Alejandro Sotelo Arévalo   alejandrosotelo@gmail.com *
 *  Andrés Córdoba Melani      acordoba@gmail.com        *
 *********************************************************
 *
 * Para información de los participantes del proyecto véase el "Acerca De" de la aplicación.
 * 
 * La modificación del código fuente está prohibida sin permiso explícito por parte de
 * los autores o del Ministerio de Educación Nacional de la República de Colombia.
 *
 */
package spadies.gui.imagenes;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import spadies.util.*;

@SuppressWarnings("serial")
public final class ImagenSPADIES extends JLabel {
  private static BufferedImage imagenEscudo;
  static {
    try {
      ImageIcon ic=Imagenes.IM_ESCUDO_COLOMBIA.getImagen();
      int w=ic.getIconWidth(),h=ic.getIconHeight(),lim=225;
      BufferedImage bi=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
      ic.paintIcon(null,bi.createGraphics(),0,0);
      for (int i=0; i<w; i++) for (int j=0; j<h; j++) {
        int c=bi.getRGB(i,j);
        if ((c&0xFF)>lim && ((c>>>8)&0xFF)>lim && ((c>>>16)&0xFF)>lim) bi.setRGB(i,j,0x00FF0000);
      }
      imagenEscudo=bi;
    }
    catch (Throwable th) {
    }
  }
  public ImagenSPADIES(int prefWidth) {
    setPreferredSize(new Dimension(prefWidth,100));
    setMaximumSize(new Dimension(Integer.MAX_VALUE,100));
    setMinimumSize(new Dimension(0,100));
  }
  public void paintComponent(Graphics gr) {
    Graphics2D g=(Graphics2D)gr;
    Stroke stk=g.getStroke();
    int w=getWidth(),h=getHeight();
    g.setColor(new Color(210,210,210));
    g.fillRect(0,0,w,h);
    GradientPaint gradiente1=new GradientPaint(w/2,0,new Color(100,0,0),w/2,h,new Color(255,0,0));
    g.setPaint(gradiente1);
    g.fillRoundRect(0,0,w,h,40,40);
    int wAct=0;
    g.setPaint(Color.WHITE);
    {
      Font f=new Font("Arial",Font.BOLD,44);
      String s=Constantes.nombreAplicacion;
      g.setFont(f);
      Dimension d=getStringDimension(g,s);
      g.drawString(s,20,70);
      wAct+=20+d.width+10;
      g.setPaint(new Color(255,150,150));
      g.setStroke(new BasicStroke(3f));
      g.drawLine(wAct,8,wAct,92);
      g.setStroke(stk);
      wAct+=10;
    }
    if (w<510) return;
    g.setPaint(Color.WHITE);
    {
      Font f=new Font("Arial",Font.BOLD,16);
      g.setFont(f);
      int h2=28;
      for (String s:new String[]{"Sistema de Prevención","y Análisis de la Deserción","en las Instituciones de","Educación Superior"}) {
        g.drawString(s,wAct,h2); h2+=18;
      }
    }
    if (w<725) return;
    g.setPaint(Color.WHITE);
    {
      Font f=new Font("Arial",Font.BOLD,12);
      g.setFont(f);
      int h2=35;
      for (String s:new String[]{"Ministerio de","Educación Nacional","República de Colombia"}) {
        Dimension d=getStringDimension(g,s);
        g.drawString(s,w-100-d.width,h2); h2+=16;
      }
    }
    g.setPaint(Color.BLACK);
    {
      Font f=new Font("Arial",Font.BOLD,8);
      g.setFont(f);
      g.drawString("Libertad y Orden",w-90,90);
      g.drawImage(imagenEscudo,w-85,18,null);
    }
  }
  private static Dimension getStringDimension(Graphics g, String s) {
    FontMetrics fm=g.getFontMetrics(g.getFont());
    return new Dimension(fm.stringWidth(s),fm.getHeight());
  }
}
