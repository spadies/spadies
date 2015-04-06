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

import java.util.*;

public final class Item implements Comparable<Item> {
  public final Comparable key;
  public final String value;
  public final String valueHTML;
  public Item(Comparable pKey, String pValue, String pValueHTML) {
    key=pKey;
    value=pValue;
    valueHTML=pValueHTML;
  }
  @SuppressWarnings("unchecked")
  public int compareTo(Item it) {
    return key.compareTo(it.key);
  }
  public static Comparable[] getKeys(Item[] items) {
    int n=items.length;
    Comparable[] res=new Comparable[n];
    for (int i=0; i<n; i++) res[i]=items[i].key;
    Arrays.sort(res);
    return res;
  }
  public String toString() {
    return value;
  }
  public String toStringHTML() {
    return valueHTML;
  }
}
