package spadies.gui.format;

import spadies.gui.graficas.FiltroDouble;
import spadies.kernel.KernelSPADIES;
import spadies.util.MyException;
import spadies.util.variables.Filtro;
import spadies.util.variables.Variable;

public class ProcesadorConsultaGrado extends ProcesadorConsultaGeneral {
  public static final ProcesadorConsultaGrado instance = new ProcesadorConsultaGrado();
  public Object[] obtenerResultado(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=KernelSPADIES.getInstance().getGradoCohorteAcumulado(filtros,diferenciados);
    }
    else {
      // Variable.PROGRAMA_EST no debe estar ni en 'diferenciados' ni en 'filtros'
      //resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(1,Object[].class,filtros,diferenciados);
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
  @Override
  public FiltroDouble getFiltroRango() {
    return new FiltroDouble() {
      public boolean acepta(double x) {
        return 5<=x && x<=15;
      }
    };
  }
  protected String[] getTitulos() {
    return new String[]{"# de semestres cursados","% de graduados"};
  }
}