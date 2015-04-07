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
import javax.swing.border.*;

@SuppressWarnings("serial")
public final class MyBorderPane extends JPanel {
  public MyBorderPane(boolean conBorde, int bArr, int bIzq, int bAbj, int bDer, Component...componentes) {
    super(new BorderLayout());
    Border b=BorderFactory.createEmptyBorder(bArr,bIzq,bAbj,bDer);
    setBorder(conBorde?BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),b):b);
    String zonas[]={BorderLayout.NORTH,BorderLayout.WEST,BorderLayout.CENTER,BorderLayout.EAST,BorderLayout.SOUTH};
    for (int i=0; i<5; i++) if (componentes[i]!=null) add(componentes[i],zonas[i]);
  }
}
