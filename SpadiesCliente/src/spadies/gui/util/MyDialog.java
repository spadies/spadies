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
package spadies.gui.util;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public abstract class MyDialog extends JDialog {
  public static enum TipoBloqueo {TB_NADA,TB_PADRE,TB_MODAL};
  private static MyEventQueue eventQueue = new MyEventQueue();
  static {
    Toolkit.getDefaultToolkit().getSystemEventQueue().push(eventQueue);
  }
  private final TipoBloqueo tipoBloqueo;
  protected MyDialog(Frame padre, String titulo, TipoBloqueo pTipoBloqueo) {
    super(padre,titulo,pTipoBloqueo==TipoBloqueo.TB_MODAL);
    tipoBloqueo=pTipoBloqueo;
  }
  protected MyDialog(Dialog padre, String titulo, TipoBloqueo pTipoBloqueo) {
    super(padre,titulo,pTipoBloqueo==TipoBloqueo.TB_MODAL);
    tipoBloqueo=pTipoBloqueo;
  }
  public void setVisible(boolean visible) {
    Component cm=getParent();
    if (!(visible && !isVisible() && tipoBloqueo==TipoBloqueo.TB_PADRE && cm!=null && (cm instanceof Window))) {
      super.setVisible(visible);
      return;
    }
    final Window w=(Window)cm;
    final Component x=RutinasGUI.getComponenteConFoco();
    final boolean bV=w.getFocusableWindowState();
    w.setFocusableWindowState(false);
    eventQueue.bloquear(w);
    super.setVisible(true);
    addWindowListener(new WindowAdapter() {
      public void windowClosed(WindowEvent pEvento){
        removeWindowListener(this);
        eventQueue.desbloquear(w);
        w.setFocusableWindowState(bV);
        if (x!=null) x.requestFocusInWindow();
      }
    });
  }
  public void dispose() {
    setVisible(false);
    super.dispose();
  }
  // Basado en http://www.javaworld.com/javatips/jw-javatip89_p.html
  private static final class MyEventQueue extends EventQueue {
    private final Collection<Window> ventanasBloqueadas=new ArrayList<Window>(5);
    public MyEventQueue() {}
    public void bloquear(Window w) {
      if (!ventanasBloqueadas.contains(w)) ventanasBloqueadas.add(w);
    }
    public void desbloquear(Window w) {
      ventanasBloqueadas.remove(w);
    }
    protected void dispatchEvent(AWTEvent e) {
      Object obj=e.getSource();
      Window w=((obj instanceof Component)?RutinasGUI.getWindowForComponent((Component)obj,false):null);
      if (w==null || !ventanasBloqueadas.contains(w)) {
        super.dispatchEvent(e);
        return;
      }
      if ((e instanceof MouseEvent) || (e instanceof KeyEvent)) {
        int id=e.getID();
        if (id==MouseEvent.MOUSE_PRESSED || id==KeyEvent.KEY_PRESSED) Toolkit.getDefaultToolkit().beep();
        return;
      }
      super.dispatchEvent(e);
    }
  }
}
