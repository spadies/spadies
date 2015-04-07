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
 * Informacion de un programa academico
 */
public final class Programa {
  /**
   * Bytes de una cadena ASCII con el nombre del programa 
   */
  public byte[] nombre={};
  public byte[] codigoSNIES={};
  /**
   * Codigo del area 
   */
  public byte area=-1;
  /**
   * Codigo del nivel 
   */
  public byte nivel=-1;
  /**
   * Metodologia del programa
   */
  public byte metodologia=-1;
  
  public byte duracion=-1;
  public byte nucleo=-1;
  public String toString() {
    return new String(nombre.length>0?nombre:codigoSNIES);
  }
  public static Programa cargar(MyDataInputStream is) throws Exception {
    Programa p=new Programa();
    p.nombre=is.readByteArray(true,-1);
    p.codigoSNIES=is.readByteArray(true,-1);
    p.area=is.readByte();
    p.nivel=is.readByte();
    p.metodologia=is.readByte();
    p.duracion=is.readByte();
    p.nucleo=is.readByte();
    return p;
  }
  public static void guardar(MyDataOutputStream os, Programa p) throws Exception {
    os.writeByteArray(true,p.nombre);
    os.writeByteArray(true,p.codigoSNIES);
    os.writeByte(p.area);
    os.writeByte(p.nivel);
    os.writeByte(p.metodologia);
    os.writeByte(p.duracion);
    os.writeByte(p.nucleo);
  }
  public int getTamanhoEnBytes() {
    return 8+nombre.length+codigoSNIES.length;
  }
}
