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
package spadies.server.util;

import java.util.*;

@SuppressWarnings("serial")
public final class MyMap extends TreeMap<byte[],List<Integer>> {
  public MyMap(Comparator<byte[]> c) {
    super(c);
  }
  public void add(byte[] a, int x) {
    List<Integer> p=get(a);
    if (p==null) put(a,p=new ArrayList<Integer>(2));
    p.add(x);
  }
}
