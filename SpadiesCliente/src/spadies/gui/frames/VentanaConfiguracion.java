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
import spadies.util.*;

@SuppressWarnings("serial")
public class VentanaConfiguracion extends MyDialog {
  private final MyLabel labelTitulo=new MyLabel("<html>Configuración de "+CajaDeHerramientas.stringToHTML(Constantes.nombreAplicacion)+"</html>");
  private final JTextField campoIP=new JTextField(12);
  private final JLabel labelIP=new JLabel("Dirección IP del servidor del Ministerio de Educación Nacional:");
  private MyButton botonAceptar=new MyButton("Aceptar","Modifica la configuración de "+Constantes.nombreAplicacion,KeyEvent.VK_A);
  private MyButton botonCancelar=new MyButton("Cancelar","Cancela la operación.",KeyEvent.VK_C);
  public VentanaConfiguracion(JFrame padre) {
    super(padre,"CONFIGURACIÓN",TipoBloqueo.TB_MODAL);
    campoIP.setText(Constantes.ipServidorSPADIES);
    campoIP.setFont(new Font("Courier",Font.PLAIN,14));
    botonAceptar.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Constantes.ipServidorSPADIES=campoIP.getText().trim();
        Constantes.guardarArchivoConfiguracion();
        VentanaConfiguracion.this.dispose();
      }
    });
    botonCancelar.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        VentanaConfiguracion.this.dispose();
      }
    });
    JPanel panelBotones=new MyFlowPane(FlowLayout.RIGHT,5,0,botonAceptar,botonCancelar);
    setContentPane(new MyBorderPane(false,8,8,8,8,labelTitulo,null,new MyBoxPane(BoxLayout.Y_AXIS,Box.createVerticalStrut(8),new MyFlowPane(0,0,labelIP),campoIP,Box.createVerticalStrut(8)),null,panelBotones));
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
