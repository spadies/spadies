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

import java.io.*;
import java.util.*;

public final class Filtro implements Serializable {
  private static final long serialVersionUID=-5893701405490339155L;
  public final Variable variable;
  public final Comparable[] filtro;
  public Filtro(Variable pVariable, Item[] pItems) {
    variable=pVariable;
    filtro=Item.getKeys(pItems);
  }
  @SuppressWarnings("unchecked")
  public boolean pasaFiltro(Object...args) {
    Object val=variable.getValor(args);
    if (val==null) return false;
    return Arrays.binarySearch(filtro,(Comparable)(variable.rango.getRango(val)))>=0;
  }
}
