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
package spadies.io;

import java.io.*;
import spadies.util.*;

public final class MyDataOutputStream {
  private MyListener listener=null;
  private final OutputStream os;
  private final byte buffer[]=new byte[51200];
  private final int tam=buffer.length;
  private int pos=0,tot=0;
  public MyDataOutputStream(OutputStream pOs) {
    os=pOs;
  }
  public int getNumeroBytesEscritos() {
    return tot;
  }
  public void setListener(MyListener pListener) {
    listener=pListener;
  }
  public void writeLong(long v) throws IOException {
    writeInt((int)(v&0xFFFFFFFFL));
    writeInt((int)((v>>>32)&0xFFFFFFFFL));
  }
  public void writeInt(int v) throws IOException {
    writeByte((byte)(v&0xFF));
    writeByte((byte)((v>>>8)&0xFF));
    writeByte((byte)((v>>>16)&0xFF));
    writeByte((byte)((v>>>24)&0xFF));
  }
  public void writeLittleInt(int v) throws IOException {
    writeByte((byte)(v&0xFF));
    writeByte((byte)((v>>>8)&0xFF));
    writeByte((byte)((v>>>16)&0xFF));
  }
  public void writeShort(short v) throws IOException {
    writeByte((byte)(v&0xFF));
    writeByte((byte)((v>>>8)&0xFF));
  }
  public void writeFloat(float v) throws IOException {
    writeInt(Float.floatToIntBits(v));
  }
  public void writeByteArray(boolean writeSize, byte[] v) throws IOException {
    int t=v.length;
    if (writeSize) {
      t=Math.min(t,Short.MAX_VALUE);
      writeShort((short)t);
    }
    for (int i=0; i<t; i++) writeByte(v[i]);
  }
  public void writeLargeByteArray(boolean writeSize, byte[] v) throws IOException {
    int t=v.length;
    if (writeSize) writeInt(t);
    for (int i=0; i<t; i++) writeByte(v[i]);
  }
  public void writeByte(byte v) throws IOException {
    buffer[pos++]=v; tot++;
    if (pos==tam) flush();
  }
  public void flush() throws IOException {
    if (pos>0) {
      os.write(buffer,0,pos);
      os.flush();
    }
    pos=0;
    if (listener!=null) listener.notify(tot);
  }
  public void close() throws IOException {
    try {
      flush();
    }
    catch (Throwable th) {
    }
    os.close();
  }
}
