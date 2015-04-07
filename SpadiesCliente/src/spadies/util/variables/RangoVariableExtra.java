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
package spadies.util.variables;

import spadies.kernel.*;
import spadies.util.*;

public final class RangoVariableExtra extends RangoByte<Byte> {
  private final int indiceVariableExtra;
  private final boolean dinamica;
  public RangoVariableExtra(int pIndiceVariableExtra, boolean pDinamica) {
    indiceVariableExtra=pIndiceVariableExtra;
    this.dinamica = pDinamica;
  }
  public Byte[] getRango() {
    VariableExtra ve=dinamica?
        AmbienteVariables.getInstance().getVariablesExtrasD()[indiceVariableExtra]
        :AmbienteVariables.getInstance().getVariablesExtras()[indiceVariableExtra];
    int t=(ve!=null)?(ve.nombresValores.length):0;
    if (t==0) return new Byte[0];
    Byte[] res=new Byte[t+1];
    res[0]=-1;
    for (int i=0; i<t; i++) res[i+1]=(byte)(i);
    return res;
  }
  public Byte getRango(Byte val) {
    return val;
  }
  public String toString(Byte val) {
    if (val==-1) return Constantes.S_DESCONOCIDO;
    String res = dinamica?
        new String(AmbienteVariables.getInstance().getVariablesExtrasD()[indiceVariableExtra].nombresValores[val])
        :new String(AmbienteVariables.getInstance().getVariablesExtras()[indiceVariableExtra].nombresValores[val]);
    return res;
  }
}
