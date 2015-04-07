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

public abstract class RangoByte<T> extends Rango<T,Byte> {
  public byte rangoToByte(Byte u) {return (byte)(u+1);}
  public Byte byteToRango(byte b) {return (byte)(b-1);}
  public int getMaxRango() {
    int r=Integer.MIN_VALUE;
    for (Byte b:getRango()) if (b>r) r=b;
    return r;
  }
}
