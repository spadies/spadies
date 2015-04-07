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
import javax.swing.*;
import spadies.util.variables.*;

@SuppressWarnings("serial")
public class MyDialogSeleccionarIES extends MyDialog {
  private final MyPanelSeleccion panelIES;
  private final MyButton botonAceptar=new MyButton("Aceptar",null,KeyEvent.VK_A);
  private final MyButton botonCancelar=new MyButton("Cancelar",null,KeyEvent.VK_C);
  private Item[] iesSeleccionadas=new Item[0];
  public MyDialogSeleccionarIES(JFrame padre, String titulo, String etiqueta, final Item[] ies) {
    super(padre,"SELECCIONAR IES",TipoBloqueo.TB_MODAL);
    MyLabel labelTitulo=new MyLabel(titulo),labelIES=new MyLabel(etiqueta);
    panelIES=new MyPanelSeleccion(ies,false,80,250);
    botonAceptar.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        iesSeleccionadas=panelIES.getItemsSeleccionados();
        if (iesSeleccionadas.length==0) {RutinasGUI.desplegarError(MyDialogSeleccionarIES.this,"<html>Debe escoger por lo menos una IES de la lista.</html>"); return;}
        MyDialogSeleccionarIES.this.dispose();
      }
    });
    botonCancelar.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        iesSeleccionadas=new Item[0];
        MyDialogSeleccionarIES.this.dispose();
      }
    });
    JPanel panelBotones=new MyFlowPane(FlowLayout.RIGHT,5,0,botonAceptar,botonCancelar);
    setContentPane(new MyBorderPane(false,8,8,8,8,labelTitulo,null,new MyBoxPane(BoxLayout.Y_AXIS,Box.createVerticalStrut(8),new MyFlowPane(0,0,labelIES),panelIES,Box.createVerticalStrut(8)),null,panelBotones));
    {
      pack();
      setResizable(false);
      Rectangle r=getParent().getBounds();
      Dimension d=getSize();
      setLocation((int)(r.x+((r.width-d.width)/2)),(int)(r.y+((r.height-d.height)/2)));
      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
  }
  public Item[] getIesSeleccionadas() {
    return iesSeleccionadas;
  }
}
