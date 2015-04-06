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

import javax.swing.*;

@SuppressWarnings("serial")
public class MyLabel extends JLabel {
  public MyLabel() {
    super();
  }
  public MyLabel(Icon icon) {
    super(icon);
  }
  public MyLabel(String texto) {
    super(texto);
  }
  public MyLabel(String texto, int alineacionHorizontal) {
    super(texto,alineacionHorizontal);
  }
  public MyLabel(String texto, String toolTipText) {
    super(texto);
    setToolTipText(toolTipText);
  }
  public MyLabel(String texto, String toolTipText, int alineacionHorizontal) {
    super(texto,alineacionHorizontal);
    setToolTipText(toolTipText);
  }
}
