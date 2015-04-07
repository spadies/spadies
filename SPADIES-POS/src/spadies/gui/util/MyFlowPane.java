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
import javax.swing.*;

@SuppressWarnings("serial")
public final class MyFlowPane extends JPanel {
  public MyFlowPane(int alineacion, int hGap, int vGap, Component...componentes) {
    super(new FlowLayout(alineacion,hGap,vGap));
    for (Component cm:componentes) if (cm!=null) add(cm);
  }
  public MyFlowPane(int hGap, int vGap, Component...componentes) {
    this(FlowLayout.LEFT,hGap,vGap,componentes);
  }
}
