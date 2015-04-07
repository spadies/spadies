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
package spadies.kernel;

import spadies.io.*;

public class VariableExtra {
  public byte[] nombre={};
  public byte[][] nombresValores=new byte[0][];
  public static VariableExtra cargar(MyDataInputStream is) throws Exception {
    VariableExtra ve=new VariableExtra();
    ve.nombre=is.readByteArray(true,-1);
    int t=is.readByte();
    ve.nombresValores=new byte[t][];
    for (int i=0; i<t; i++) ve.nombresValores[i]=is.readByteArray(true,-1);
    return ve;
  }
  public static void guardar(MyDataOutputStream os, VariableExtra ve) throws Exception {
    os.writeByteArray(true,ve.nombre);
    int t=Math.min(ve.nombresValores.length,100);
    os.writeByte((byte)t);
    for (int i=0; i<t; i++) os.writeByteArray(true,ve.nombresValores[i]);
  }  
  public int getTamanhoEnBytes() {
    int r=0,t=Math.min(nombresValores.length,100);
    r+=2+nombre.length+1;
    for (int i=0; i<t; i++) r+=2+nombresValores[i].length;
    return r;
  }
}
