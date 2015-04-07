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
public class MyEditorPane extends JEditorPane {
  public MyEditorPane(boolean conAparienciaLabel) {
    super("text/html", "");
    setEditable(false);
    if (conAparienciaLabel) {
      setBackground((Color)(UIManager.get("Label.background")));
      setForeground((Color)(UIManager.get("Label.foreground")));
      setFont((Font)(UIManager.get("Label.font")));
    }
  }
  public MyEditorPane(boolean conAparienciaLabel, String textoHTML) {
    this(conAparienciaLabel);
    setText(textoHTML);
  }
  public void setText(String textoHTML) {
    String s=textoHTML;
    Font f=getFont();
    if (s.trim().length()>0) s=("<html><font face=\""+f.getFamily()+"\" style=\"font-size: "+f.getSize()+"\">"+s.replaceAll("(\\Q<html>\\E)|(\\Q</html>\\E)","").replace("<p>","<br>")+"</font></html>");
    //if (s.trim().length()>0) s=("<html><font face=\""+f.getFamily()+"\" style=\"font-size: "+f.getSize()+"\">"+s.replaceAll("[\\Q<html>\\E\\Q</html>\\E]","").replaceAll("\\Q<p>\\E","<br>")+"</font></html>");
    super.setText(s);
    setCaretPosition(0);
  }
}
