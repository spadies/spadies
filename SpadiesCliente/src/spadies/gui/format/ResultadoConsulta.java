package spadies.gui.format;

import spadies.gui.util.InfoTabla;

public class ResultadoConsulta {
  public final String nombre;
  public final Object resultado;
  public ResultadoConsulta(String nombre, Object resultado) {
    assert(resultado.getClass().equals(InfoTabla.class));
    this.nombre = nombre;
    this.resultado = resultado;
  }
}