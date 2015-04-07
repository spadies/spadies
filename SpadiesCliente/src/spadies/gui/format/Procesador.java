package spadies.gui.format;

import java.util.Collection;

import spadies.util.MyException;
import spadies.util.variables.Filtro;
import spadies.util.variables.Variable;

public interface Procesador {
  public void setParametros(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros);
  public Collection<ResultadoConsulta> generarGrafica() throws Exception;
}
