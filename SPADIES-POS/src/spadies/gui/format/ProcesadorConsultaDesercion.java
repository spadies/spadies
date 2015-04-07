package spadies.gui.format;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.jfree.chart.ChartPanel;

import spadies.gui.graficas.FabricaGraficas;
import spadies.gui.graficas.FiltroDouble;
import spadies.gui.util.InfoTabla;
import spadies.kernel.KernelSPADIES;
import spadies.util.CajaDeHerramientas;
import spadies.util.MyException;
import spadies.util.PuertaAlServidorDeConsultas;
import spadies.util.variables.Filtro;
import spadies.util.variables.Variable;

public class ProcesadorConsultaDesercion extends ProcesadorConsultaGeneral {
  public static final ProcesadorConsultaDesercion instance = new ProcesadorConsultaDesercion();
  public Object[] obtenerResultado(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=KernelSPADIES.getInstance().getDesercion(filtros,diferenciados);
    }
    else {
      // Variable.PROGRAMA_EST no debe estar ni en 'diferenciados' ni en 'filtros'
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(1,Object[].class,filtros,diferenciados);
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
  @Override
  public FiltroDouble getFiltroRango() {
    return new FiltroDouble() {
      public boolean acepta(double x) {
        return x<=15;
      }
    };
  }
}