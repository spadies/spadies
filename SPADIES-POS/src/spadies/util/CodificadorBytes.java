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
package spadies.util;

public abstract class CodificadorBytes {
  public abstract int numCodigos();
  public abstract int getCodigo(byte b);
  public byte[] getCodigos(String s) {
    s=limpiarString(s);
    byte[] b=s.getBytes();
    int t=b.length;
    byte[] r=new byte[t];
    for (int i=0; i<t; i++) r[i]=(byte)(getCodigo(b[i]));
    return r;
  }
  public abstract byte getCharacter(int c);
  public String getCharacters(byte[] b) {
    String s="";
    for (byte a:b) s+=(char)(getCharacter(a));
    return s;
  }
  public abstract String limpiarString(String s);
}
