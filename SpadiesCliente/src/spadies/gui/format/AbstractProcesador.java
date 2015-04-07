package spadies.gui.format;

import java.util.Collection;

import spadies.util.variables.Filtro;
import spadies.util.variables.Variable;

public abstract class AbstractProcesador implements Procesador {
  boolean alMinisterio;
  protected Variable[] diferenciados;
  protected Filtro[] filtros;
  protected byte[] semPer;
  public void setParametros(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) {
    this.alMinisterio = alMinisterio;
    this.diferenciados = diferenciados;
    this.filtros = filtros;
  }
  
  public abstract Collection<ResultadoConsulta> generarGrafica() throws Exception;

}
