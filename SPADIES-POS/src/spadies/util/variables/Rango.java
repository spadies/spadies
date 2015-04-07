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

import spadies.util.*;

public abstract class Rango<T,U extends Comparable<U>> {
  public abstract U[] getRango();
  public abstract U getRango(T t);
  public abstract byte rangoToByte(U u);
  public abstract U byteToRango(byte b);
  public abstract String toString(U u);
  public String toStringHTML(U u) {
    return CajaDeHerramientas.stringToHTML(toString(u));
  }
}
