package spadies.web.server;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;

import spadies.kernel.KernelSPADIES;
import spadies.util.MyException;
import spadies.util.variables.AmbienteVariables;
import spadies.util.variables.Filtro;

public class EstadoWebSPADIES {
  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm");
  public static final EstadoWebSPADIES instance = new EstadoWebSPADIES();
  private Estado est = Estado.SIN_DATOS_CARGADOS;
  public static enum Estado {
    SIN_DATOS_CARGADOS,
    PREPARADO
  }
  public void cargarDatosSPA(String path,String pathfecha) {
    if (est==Estado.PREPARADO) return;
    synchronized(est) {
      try {
        KernelSPADIES.getInstance().cargarParaServidor(new File(path), Long.MAX_VALUE,false);
        escrituraFecha(pathfecha);
      } catch (MyException e) {
        e.printStackTrace();
      }
      AmbienteVariables.getInstance().notificarCarga();
      AmbienteVariables.getInstance().notificarCambioSeleccion(new Filtro[0]);
      est = Estado.PREPARADO;
    }
  }
  public Estado getEstado() {
    return est;
  }
  public void escrituraFecha(String pathfecha) {
    try {
      File f = new File(new File(pathfecha),"fecha.txt");
      PrintStream ps = new PrintStream(f);
      ps.print("Actualización "+sdf.format(new java.util.Date()));
      ps.close();
    } catch (Exception e) {} 
  }
}
