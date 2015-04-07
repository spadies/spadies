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

/**
 * Representa la información personal de un estudiante
 */
public final class Estudiante_DatosPersonales {
  /**
   * Bytes de una cadena ASCII con el nombre del estudiante 
   */
  public byte[] nombre={};
  /**
   * Bytes de una cadena ASCII con el nombre del apellido 
   */
  public byte[] apellido={};
  /**
   * Bytes de una cadena ASCII con el codigo asignado por la IES al estudiante 
   */  
  public byte[] codigo={};
  /**
   * Codigo del tipo de documento, los valores posibles son:
   * <table><tr><td>-1</td><td>Desconocido</td></tr><tr><td>0</td><td>Cedula</td></tr><tr><td>1</td><td>Tarjeta de identidad</td></tr><tr><td>2</td><td>Cedula de extranjeria</td></tr><tr><td>3</td><td>Registro civil</td><tr><td>-1</td><td>Otro</td></tr></tr></table>
   */
  public byte tipoDocumento=-1;  // -1=?,0=C,1=T,2=E,3=R,4=O
  /**
   * Numero del documento
   */
  public long documento=-1;
  /**
   * Año de la fecha de nacimiento del estudiante
   */  
  public short anhoFechaNacimiento=-1;
  /**
   * Numero del mes de nacimiento del estudiante
   */
  public byte mesFechaNacimiento=-1;
  /**
   * Numero del dia de nacimiento del estudiante
   */
  public byte diaFechaNacimiento=-1;
  public boolean pasaFiltroEspecial(String[][] filtroEspecial) {
    String campos[]={new String(apellido)+" "+new String(nombre),documento==-1?"":(""+documento),new String(codigo)};
    for (int i=0; i<3; i++) {
      boolean b=true;
      for (String s:filtroEspecial[i]) if (campos[i].indexOf(s)==-1) {b=false; break;}
      if (!b) return false;
    }
    return true;
  }
  public static Estudiante_DatosPersonales cargar(MyDataInputStream is) throws Exception {
    Estudiante_DatosPersonales edp=new Estudiante_DatosPersonales();
    edp.nombre=is.readByteArray(true,-1);
    edp.apellido=is.readByteArray(true,-1);
    edp.codigo=is.readByteArray(true,-1);
    edp.tipoDocumento=is.readByte();
    edp.documento=is.readLong();
    edp.anhoFechaNacimiento=is.readShort();
    edp.mesFechaNacimiento=is.readByte();
    edp.diaFechaNacimiento=is.readByte();
    return edp;
  }
  public static void guardar(MyDataOutputStream os, Estudiante_DatosPersonales edp) throws Exception {
    os.writeByteArray(true,edp.nombre);
    os.writeByteArray(true,edp.apellido);
    os.writeByteArray(true,edp.codigo);
    os.writeByte(edp.tipoDocumento);
    os.writeLong(edp.documento);
    os.writeShort(edp.anhoFechaNacimiento);
    os.writeByte(edp.mesFechaNacimiento);
    os.writeByte(edp.diaFechaNacimiento);
  }
  /**
   * El tamaño en bytes que ocupara la informacion de la instancia al ser guardada
   * @return
   */
  public int getTamanhoEnBytes() {
    return 19+nombre.length+apellido.length+codigo.length;
  }
}
