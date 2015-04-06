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
package spadies.gui.frames;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import spadies.gui.util.*;
import spadies.kernel.*;

@SuppressWarnings("serial")
public class VentanaResultadoPanel extends JFrame implements Observer {
  private final KernelSPADIES kernel=KernelSPADIES.getInstance();
  public VentanaResultadoPanel(JFrame padre, String []titulo, JPanel[] panel, JComponent labelDescripcion) {
    //super(titulo);
    super("");
    setIconImage(padre.getIconImage());
    kernel.addObserver(this);
    JTabbedPane panelCentro=new JTabbedPane(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
    {
      for (int i=0;i<titulo.length;i++) panelCentro.addTab(titulo[i],panel[i]);
    }
    panelCentro.setSelectedIndex(0);
    Component compDerecha=(labelDescripcion==null)?null:new MyScrollPane(labelDescripcion,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,-1,-1);
    Component compPrincipal=(compDerecha==null)?panelCentro:new MySplitPane(JSplitPane.HORIZONTAL_SPLIT,true,panelCentro,compDerecha,true);
    {
      setContentPane(new MyBorderPane(false,0,0,0,0,null,null,compPrincipal,null,null));
      Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
      setResizable(true);
      setSize(new Dimension(d.width-200,d.height-200));
      if (labelDescripcion!=null) {
        MySplitPane msp=(MySplitPane)compPrincipal;
        msp.setResizeWeight(1.0);
        msp.setDividerLocation(getWidth()-300);
      }
      setLocation((int)((d.width-getWidth())/2),(int)((d.height-getHeight())/2));
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
  }
  public VentanaResultadoPanel(JFrame padre, String titulo, JPanel panel, JComponent labelDescripcion) {
    super(titulo);
    setIconImage(padre.getIconImage());
    kernel.addObserver(this);
    JTabbedPane panelCentro=new JTabbedPane(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
    {
      panelCentro.addTab(titulo,panel);
    }
    panelCentro.setSelectedIndex(0);
    Component compDerecha=(labelDescripcion==null)?null:new MyScrollPane(labelDescripcion,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,-1,-1);
    Component compPrincipal=(compDerecha==null)?panelCentro:new MySplitPane(JSplitPane.HORIZONTAL_SPLIT,true,panelCentro,compDerecha,true);
    {
      setContentPane(new MyBorderPane(false,0,0,0,0,null,null,compPrincipal,null,null));
      Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
      setResizable(true);
      setSize(new Dimension(d.width-200,d.height-200));
      if (labelDescripcion!=null) {
        MySplitPane msp=(MySplitPane)compPrincipal;
        msp.setResizeWeight(1.0);
        msp.setDividerLocation(getWidth()-300);
      }
      setLocation((int)((d.width-getWidth())/2),(int)((d.height-getHeight())/2));
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
  }
  public void update(Observable obs, Object arg) {
    if (arg.equals("CARGA")) dispose();
  }
  public void setVisible(boolean b) {
    if (b) super.setVisible(true);
    else   dispose();
  }
  public void dispose() {
    kernel.deleteObserver(this);
    super.setVisible(false);
    super.dispose();
  }
}
