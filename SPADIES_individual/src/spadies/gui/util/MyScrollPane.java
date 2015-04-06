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
public final class MyScrollPane extends JScrollPane {
  public MyScrollPane(Component componente, int verticalScrollBarPolicy, int horizontalScrollBarPolicy, int ancho, int alto) {
    super(componente,verticalScrollBarPolicy,horizontalScrollBarPolicy);
    setWheelScrollingEnabled(true);
    if (ancho>0 && alto>0) setPreferredSize(new Dimension(ancho,alto));
  }
}
