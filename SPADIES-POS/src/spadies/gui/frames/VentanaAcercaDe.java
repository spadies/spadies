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
import java.util.*;
import javax.swing.*;
import spadies.gui.util.*;
import spadies.util.*;
import spadies.gui.imagenes.*;

@SuppressWarnings("serial")
public class VentanaAcercaDe extends MyDialog {
  private static final VentanaAcercaDe instance=new VentanaAcercaDe();
  private final MyLabel labelDibujo=new MyLabel(Imagenes.IM_MINISTERIO.getImagen());
  private final MyLabel labelTitulo=new MyLabel();
  private final MyButton botonCerrar=new MyButton("Cerrar","Cerrar la ventana",KeyEvent.VK_R);
  public static VentanaAcercaDe getInstance() {
    return instance;
  }
  private VentanaAcercaDe() {
    super(VentanaPrincipal.getInstance(),"Acerca de "+Constantes.nombreAplicacionLargo,MyDialog.TipoBloqueo.TB_MODAL);
    botonCerrar.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        VentanaAcercaDe.this.dispose();
      }
    });
    {
      StringBuffer sb=new StringBuffer();
      sb.append("<html>");
      sb.append("<p>");
      sb.append("<p>");
      sb.append("<hr>");
      sb.append("<font size=\"6\" color=\"#00005B\"><i><b>"+Constantes.nombreAplicacionLargo+"</b></i></font><p>");
      sb.append("<font size=\"4\" color=\"#00005B\"><i>Sistema para la Prevención y Análisis de la Deserción</i></font><p>");
      sb.append("<font size=\"4\" color=\"#00005B\"><i>en las Instituciones de Educación Superior</i></font><p>");
      sb.append("<p>");
      sb.append("<b><font color=\"#900000\">Participantes del proyecto:</font></b>");
      sb.append("<p>");
      sb.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
      sb.append("  <tr><td colspan=\"2\" bgcolor=\"#D4D4FF\" width=\"600\"><b>Ministerio de Educación Nacional</b></td></tr>");
      sb.append("  <tr>");
      sb.append("    <td width=\"40\"></td>");
      sb.append("    <td>Carolina Guzmán, Jorge Franco, Jorge Navas, Diana Marcela Duran Muriel</td>");
      sb.append("  </tr>");
      sb.append("  <tr><td colspan=\"2\"></td></tr>");
      sb.append("  <tr><td colspan=\"2\" bgcolor=\"#D4D4FF\" width=\"600\"><b>Centro de Estudios sobre Desarrollo Económico (CEDE) - Universidad de los Andes</b></td></tr>");
      sb.append("  <tr>");
      sb.append("    <td width=\"40\"></td>");
      sb.append("    <td>Fabio Sánchez Torres, "+/*Jorge Villalobos, */"Haider Jaime Rueda</td>");
      //sb.append("    <td>Fabio Sánchez, Haider Jaime, Carmen Elisa Flórez, Jairo Núñez</td>");
      sb.append("  </tr>");
      sb.append("  <tr>");
      sb.append("    <td width=\"40\"></td>");
      //sb.append("    <td>Alejandro Sotelo, Andrés Córdoba, Paloma López de mesa</td>");
      sb.append("    <td>Andrés Córdoba Melani, Luis Omar Herrera, Martha Susana Jaimes</td>");
      sb.append("  </tr>");
      sb.append("  <tr>");
      sb.append("    <td width=\"40\"></td>");
      sb.append("    <td>Alejandro Sotelo, Lina Ruedas Silva</td>");
      //sb.append("    <td>Alvaro Jose Moreno, Alejandro Sotelo, Lina Ruedas Silva</td>");
      //sb.append("    <td>Martha Susana Jaimes, Laura Cuesta, Luis Omar Herrera Prada</td>");
      sb.append("  </tr>");
      sb.append("  <tr><td colspan=\"2\"></td></tr>");
      //sb.append("  <tr><td colspan=\"2\" bgcolor=\"#D4FFD4\" width=\"600\" align=center><b>SPADIES versión 2.4.0</b> - Noviembre 24 de 2006</td></tr>");
      //sb.append("  <tr><td colspan=\"2\" bgcolor=\"#D4FFD4\" width=\"600\" align=center><b>SPADIES versión 2.4.0</b> - Junio 13 de 2008</td></tr>");
      //sb.append("  <tr><td colspan=\"2\" bgcolor=\"#D4FFD4\" width=\"600\" align=center><b>SPADIES versión 2.4.1</b> - Agosto 26 de 2008</td></tr>");
      //sb.append("  <tr><td colspan=\"2\" bgcolor=\"#D4FFD4\" width=\"600\" align=center><b>SPADIES versión " + Constantes.versionMenor + "</b> - Noviembre 27 de 2008</td></tr>");
      sb.append("  <tr><td colspan=\"2\" bgcolor=\"#D4FFD4\" width=\"600\" align=center><b>SPADIES versión " + Constantes.versionMenor + "</b> - Agosto 25 de 2011</td></tr>");
      sb.append("  <tr><td colspan=\"2\"></td></tr>");
      sb.append("</table>");
      sb.append("<p>");
      sb.append("</html>");
      labelTitulo.setText(sb.toString());
    }
    {
      JPanel panelDibujo=new MyFlowPane(FlowLayout.CENTER,0,0,labelDibujo);
      JPanel panelTitulo=new MyBoxPane(BoxLayout.X_AXIS,Box.createHorizontalStrut(8),labelTitulo);
      JPanel panelBotones=new MyFlowPane(FlowLayout.RIGHT,5,0,botonCerrar);
      JPanel panelPrincipal=new MyBorderPane(true,8,8,8,8,panelDibujo,null,panelTitulo,null,panelBotones);
      setContentPane(panelPrincipal);
      for (JPanel p:Arrays.asList(panelDibujo,panelTitulo,panelBotones,panelPrincipal)) {
        p.setOpaque(true);
        p.setBackground(Color.WHITE);
      }
    }
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
