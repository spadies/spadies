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

import java.text.*;

import javax.swing.JFrame;

import spadies.gui.util.*;
import spadies.util.*;
import spadies.kernel.*;

@SuppressWarnings("serial")
public class VentanaCarga extends MyDialogProgreso {
  private static final DecimalFormat df=new DecimalFormat("0.000");
  public VentanaCarga(JFrame frm) {
    super(frm,"Cargando la información de la carpeta "+Constantes.carpetaDatos.getPath(),true);
  }
  public void ejecutar() {
    setVisible(true);
    new Thread() {
      public void run() {
        try {
          long tiempo=KernelSPADIES.getInstance().cargar(Constantes.carpetaDatos,Constantes.cargaDatosPersonales, VentanaCarga.this);
          RutinasGUI.desplegarInformacion(VentanaCarga.this,"<html>Se cargaron exitosamente los datos de la carpeta "+CajaDeHerramientas.stringToHTML(Constantes.carpetaDatos.getPath())+" en "+df.format(0.001*tiempo).replace(',','.')+" segundos. Memoria: "+CajaDeHerramientas.usoMemoria()+"</html>");
        }
        catch (MyException ex) {
          RutinasGUI.desplegarError(VentanaCarga.this,"<html>"+CajaDeHerramientas.stringToHTML(ex.getMessage())+"</html>");
        }
        VentanaCarga.this.dispose();
      }
    }.start();
  }
}
