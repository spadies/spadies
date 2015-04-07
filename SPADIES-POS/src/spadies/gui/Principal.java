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
package spadies.gui;

import java.awt.Color;
import java.awt.Font;
import java.io.FileNotFoundException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import spadies.gui.frames.VentanaCarga;
import spadies.gui.frames.VentanaPrincipal;
import spadies.util.Constantes;

public class Principal {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        String[] w={"Label","Button","ComboBox","CheckBox","Table","List","TextField","TextArea","EditorPane","TextPane","FileChooser","Menu","MenuItem","Popup"};
        for (String s:w) UIManager.put(s+".font",new Font("Dialog",Font.PLAIN,12));
        UIManager.put("SplitPane.dividerSize",10);
        UIManager.put("ComboBox.disabledForeground",new javax.swing.plaf.ColorUIResource(new Color(160,160,160)));
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        VentanaPrincipal.getInstance().setVisible(true);
        Constantes.cargarArchivoConfiguracion();
        Constantes.cargarArchivoFiltroIES();
        try {
          Constantes.logSPADIES.init();
        } catch (FileNotFoundException e) {
          System.err.println("Error iniciando el log de SPADIES"+"");
        }
        try {Thread.sleep(100);} catch (Throwable th) {}
        new VentanaCarga(VentanaPrincipal.getInstance()).ejecutar();
      }
    });
  }
}
