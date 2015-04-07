package spadies.tools;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;

import spadies.kernel.Estudiante;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.util.Constantes;
import spadies.util.MyException;

public class Prueba {
  private static KernelSPADIES kernel = null;
  private static PrintStream ps = null;
  private static void pl(String s) {ps.println(s);};
  private static void p(String s) {ps.print(s);};
  private static final transient DecimalFormat df=new DecimalFormat("0.00%");
  public static void main(String[] args) throws MyException, IOException {
    kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,false);
    PrintStream ps = new PrintStream("SalidaEstadoPeriodos.csv");
    Collection<String> linea = new LinkedList<String>();
    linea.add("IES");
    linea.add("NPeriodos");
    linea.add("PerIni");
    linea.add("PerFin");
    linea.add("Cohorte");
    linea.add("Desertor");
    linea.add("Activo");
    linea.add("Graduado");
    linea.add("Retirado");
    p(ps,linea);
    for (IES ies: kernel.listaIES) {
      int[][] conteo = new int[ies.n][4];
      for (Estudiante e:ies.estudiantes)
        conteo[e.getSemestrePrimiparo()][e.getEstado()+1]++;
      for (int i=0;i<ies.n;i++) {
        linea.clear();
        linea.add(""+ies.codigo);
        linea.add(""+ies.semestres.length);
        linea.add(ies.semestres[0]);
        linea.add(ies.semestres[ies.semestres.length-1]);
        linea.add(ies.semestres[i]);
        linea.add(""+conteo[i][0]);
        linea.add(""+conteo[i][1]);
        linea.add(""+conteo[i][2]);
        linea.add(""+conteo[i][3]);
        p(ps,linea);
      }
    }
    ps.close();
  }
  private static void p(PrintStream ps, Collection<String> c) {
    for (String s:c) ps.print(s+";");
    ps.println();
  }

}
