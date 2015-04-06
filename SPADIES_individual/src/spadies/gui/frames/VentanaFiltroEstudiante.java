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
import java.awt.event.*;
import javax.swing.*;
import spadies.gui.util.*;

@SuppressWarnings("serial")
public class VentanaFiltroEstudiante extends MyDialog {
  private final MyLabel labelTitulo=new MyLabel("Filtro de estudiantes por datos personales");
  private final JTextField campoNombreApellido=new JTextField(12);
  private final JTextField campoDocumento=new JTextField(12);
  private final JTextField campoCodigo=new JTextField(12);
  private final MyLabel labelNombreApellido=new MyLabel("Apellidos y/o nombres:");
  private final MyLabel labelDocumento=new MyLabel("Documento:");
  private final MyLabel labelCodigo=new MyLabel("Código de estudiante en la IES:");
  private MyButton botonAceptar=new MyButton("Modificar","Modifica el filtro de estudiantes por datos personales",KeyEvent.VK_M);
  private MyButton botonCancelar=new MyButton("Cancelar","Cancela la operación.",KeyEvent.VK_C);
  public VentanaFiltroEstudiante(JFrame padre, final String[] filtro) {
    super(padre,"FILTRO",TipoBloqueo.TB_MODAL);
    final JTextField campos[]={campoNombreApellido,campoDocumento,campoCodigo};
    for (int i=0; i<3; i++) campos[i].setText(filtro[i]);
    botonAceptar.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        for (int i=0; i<3; i++) filtro[i]=campos[i].getText().toUpperCase().trim();
        VentanaFiltroEstudiante.this.dispose();
      }
    });
    botonCancelar.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        VentanaFiltroEstudiante.this.dispose();
      }
    });
    JPanel panelBotones=new MyFlowPane(FlowLayout.RIGHT,5,0,botonAceptar,botonCancelar);
    setContentPane(new MyBorderPane(false,8,8,8,8,labelTitulo,null,new MyBoxPane(BoxLayout.Y_AXIS,Box.createVerticalStrut(8),new MyFlowPane(0,0,labelNombreApellido),campoNombreApellido,Box.createVerticalStrut(8),new MyFlowPane(0,0,labelDocumento),campoDocumento,Box.createVerticalStrut(8),new MyFlowPane(0,0,labelCodigo),campoCodigo,Box.createVerticalStrut(8)),null,panelBotones));
    {
      pack();
      setResizable(false);
      Rectangle r=getParent().getBounds();
      Dimension d=getSize();
      setLocation((int)(r.x+((r.width-d.width)/2)),(int)(r.y+((r.height-d.height)/2)));
      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
  }
}
