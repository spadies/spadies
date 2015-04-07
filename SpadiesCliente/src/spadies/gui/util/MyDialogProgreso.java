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

import java.text.*;
import java.awt.*;
import javax.swing.*;
import spadies.util.*;

public abstract class MyDialogProgreso extends MyDialog implements MyListener {
  private JProgressBar barraProgreso=new JProgressBar(JProgressBar.HORIZONTAL,0,1000);
  public MyDialogProgreso(JFrame padre, String descripcion, boolean conBarraProgreso) {
    super(padre,Constantes.nombreAplicacion,MyDialog.TipoBloqueo.TB_PADRE);
    barraProgreso.setStringPainted(true);
    MyBorderPane panelPrincipal=new MyBorderPane(true,5,5,5,5,new MyFlowPane(0,0,new MyLabel(descripcion)),null,conBarraProgreso?new MyBoxPane(BoxLayout.Y_AXIS,Box.createVerticalStrut(8),barraProgreso):null,null,null);
    {
      setContentPane(panelPrincipal);
      pack();
      setResizable(false);
      Rectangle r=getParent().getBounds();
      Dimension d=getSize();
      setLocation((int)(r.x+((r.width-d.width)/2)),(int)(r.y+((r.height-d.height)/2)));
      setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }
  }
  public void notify(Object arg) {
    int[] prog=(int[])arg;
    double porc=(100.0*prog[0]/prog[1]);
    barraProgreso.setValue((int)(porc*10));
    barraProgreso.setString(RutinasGUI.df_porcentaje.format(porc/100));
    barraProgreso.repaint();
  }
  public abstract void ejecutar();
}
