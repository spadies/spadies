/*
 * Centro de Estudios sobre Desarrollo Económico (CEDE) - Universidad de los Andes
 *
 *********************************************************
 * SPADIES                                               *
 * Sistema para la Prevención y Análisis de la Deserción *
 * en las Instituciones de Educación Superior            *
 *********************************************************
 * Autores del código fuente (última versión):           *
 *  Andrés Córdoba Melani      acordoba@gmail.com        *
 *********************************************************
 *
 * Para información de los participantes del proyecto véase el "Acerca De" de la aplicación.
 * 
 * La modificación del código fuente está prohibida sin permiso explícito por parte de
 * los autores o del Ministerio de Educación Nacional de la República de Colombia.
 *
 */
package spadies.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
  private static final DateFormat formatoTiempo = new SimpleDateFormat("yyyy/MM/dd_HH:mm"); 
  private File logFile = null; 
  private PrintStream ps = null;
  public Logger(File f) {
    logFile = f;
  }
  public void init() throws FileNotFoundException {
    ps = new PrintStream(logFile);
  }
  public void log(String msg) {
    ps.println(tiempo() + " " + msg);
  }
  public void log(Throwable th) {
    ps.println(tiempo());
    th.printStackTrace(ps);
  }
  public void log(String msg,Throwable th) {
    ps.println(tiempo()+" "+msg);
    th.printStackTrace(ps);
  }
  private String tiempo() {
    return formatoTiempo.format(new Date());
  }
}
