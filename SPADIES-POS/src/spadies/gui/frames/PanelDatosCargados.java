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
import spadies.util.variables.*;

@SuppressWarnings("serial")
public class PanelDatosCargados extends JPanel {
  private final MyPanelTabla panelTabla=new MyPanelTabla(new InfoTabla(null,null,null),500,300,130);
  private final KernelSPADIES kernel=KernelSPADIES.getInstance();
  private final MyLabel labelTitulo=new MyLabel("<html>Número de estudiantes que aparecen como primíparos en las IES seleccionadas:<p><p><b>Conteo de estudiantes en los archivos csv de entrada:</b></html>");
  public PanelDatosCargados() {
    setLayout(new BorderLayout());
    add(new MyFlowPane(0,0,labelTitulo),BorderLayout.NORTH);
    add(new MyScrollPane(panelTabla,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,-1,-1),BorderLayout.CENTER);
  }
  public void actualizar() {
    Object[] res={null,null,null};
    int numEst=0;
    if (VentanaPrincipal.getInstance().estaSeleccionandoIES()) {
      Filtro[] filtrosIES=VentanaPrincipal.getInstance().getSeleccionPura(new EnumMap<Variable,Filtro>(Variable.class)).values().toArray(new Filtro[0]);
      res=kernel.getTablaCantidadArchivos(filtrosIES);
      numEst=kernel.getCantidadEstudiantes(filtrosIES);
    }
    panelTabla.setDatos(new InfoTabla((String[][])(res[0]),(String[][])(res[1]),(String[])(res[2])));
    labelTitulo.setText("<html>Número de estudiantes que aparecen como primíparos en las IES seleccionadas: "+(numEst==0?"-":(""+numEst))+"<p><p><b>Conteo de estudiantes en los archivos csv de entrada:</b></html>");
  }
}
