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
package spadies.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.text.*;
import java.util.*;

import spadies.io.*;
import spadies.kernel.*;
import spadies.util.*;
import spadies.server.kernel.*;
import spadies.server.util.*;
import static spadies.util.CajaDeHerramientas.*;

public class ServerDownload {
  private static final DateFormat df=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  private static /*final*/ int puerto=Constantes.puertoServidorMatch;
  private static final List<Long> colaAtencion=Collections.synchronizedList(new LinkedList<Long>());
  private static final boolean[] tablasGrandesEncontradas=new boolean[3]; // ICFES,ICETEX,GRADUADOS
  public static void main(String[] args) {
    if (args.length == 1) {
      try {
        Integer numPuerto = Integer.parseInt(args[0]);
        puerto = numPuerto;
      } catch (NumberFormatException e) {
        puerto = Constantes.puertoServidorMatch;
      }
    }
    ServerSocket serverSocket=null;
    try {
      TablaProgramas.getInstance().preparar();
      TablaIES.getInstance().preparar();
      TablaPasswordsIES.getInstance().preparar();
      TablaDepartamentos.getInstance().preparar();
      //PreparadorDatos.getInstance().prepararArchivosBase(tablasGrandesEncontradas);
      System.gc();
      serverSocket=new ServerSocket(puerto);
    }
    catch (Throwable th) {
      th.printStackTrace(System.out);
      System.exit(1);
    }
    final ServerSocket serverSocket2=serverSocket;
    System.out.println("EL SERVIDOR DE DOWNLOAD DE "+Constantes.nombreAplicacionLargo+" ESTÁ ESCUCHANDO CONEXIONES");
new Thread() {public void run() {
    while (true) {
      try {
        final Socket clientSocket=serverSocket2.accept();
        new Thread() {
          public void run() {
            long tm=System.currentTimeMillis();
            String address="",sErr="";
            try {
              address=clientSocket.getInetAddress().toString();
              clientSocket.setSoTimeout(1000*60);
              atender(clientSocket);
            }
            catch (Throwable th) {
              sErr=th.getMessage();
              th.printStackTrace();
            }
            System.err.println(df.format(new Date())+";"+address+";"+(System.currentTimeMillis()-tm)+"ms;"+sErr.replace(';',','));
          }
        }.start();
      }
      catch (Throwable th) {
      }
    }
}}.start();
    System.out.println("INICIADO");
  }
  private static void atender(Socket cliente) throws Exception {
    long tiempo=System.currentTimeMillis();
    MyDataOutputStream out=null;
    MyDataInputStream in=null;
    try {
      out=new MyDataOutputStream(cliente.getOutputStream());
      in=new MyDataInputStream(cliente.getInputStream());
      if (in.readLong()!=1654949798791L) {
        out.writeLong(0L); out.flush();
        return;
      }
      int codigoIES=in.readInt();
      out.writeLong(3571984365291298800L); out.flush();
      out.writeLong(tiempo); out.flush();
      /*Clave*/
      String pwd=TablaPasswordsIES.getInstance().getPasswordIES(codigoIES);
      //String pwd="";
      if (pwd==null) {
        out.writeLong(0L); out.flush();
        return;
      }
      pwd+=";"+tiempo;
      out.writeLong(4897197444879L); out.flush();
      try {
        String msg=new String(CajaDeHerramientas.getCipherDecrypt(pwd).doFinal(in.readByteArray(true,-1)));
        if (!msg.equals("m. ! < sS@#tYm !ñQ/X")) throw new Exception("");
      }
      catch (Throwable th) {
        out.writeByteArray(true,"".getBytes()); out.flush();
        return;
      }
      out.writeByteArray(true,CajaDeHerramientas.getCipherEncrypt(pwd).doFinal("1W bT,^-Mn|5Q rP{a]!".getBytes())); out.flush();
      atender(out,in,codigoIES,pwd);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      try {
        if (out!=null) out.close();
        if (in!=null) in.close();
        cliente.close();
      }
      catch (Throwable th) {
      }
    }
  }
  private static void atender(MyDataOutputStream out, MyDataInputStream in, int codigoIES, String pwd) throws Exception {
    byte[] baIES;
    if (in.readLong()!=4925672961244L) return;
    out.writeLong(5709386157069L); out.flush();
    if (in.readLong()!=9713657941972L) return;
    baIES=in.readLargeByteArray(true,-1);
    IES ies=IES.byteArrayToIES(baIES,pwd);
    ies=IES.cargar(new File(ConstantesServer.fDATOS,ies.codigo+".spa"), true);
    if (ies.codigo!=codigoIES) return;
    if (in.readLong()!=4902403597593L) return;
    out.writeLong(1020579306849L); out.flush();
    Long id=0L;
    synchronized (ServerDownload.class) {colaAtencion.add(id=(Long)(System.currentTimeMillis()));}
    try {
      int posEnCola=-1;
      while (posEnCola!=0) {
        synchronized (ServerDownload.class) {posEnCola=colaAtencion.indexOf(id);}
        if (posEnCola==-1) return;
        out.writeLong(4671937598247L);
        out.writeInt(posEnCola);
        out.flush();
        if (posEnCola!=0) {
          try {Thread.sleep(10000);} catch (Throwable th) {}
        }
      }
      out.writeLong(7049449709348L); out.flush();
      {
        out.writeLong(1L); out.flush();
        out.writeLong(Long.MAX_VALUE); out.writeLong(Long.MAX_VALUE); out.flush();
        out.writeLong(Long.MAX_VALUE); out.writeLong(Long.MAX_VALUE); out.flush();
        out.writeLong(Long.MAX_VALUE); out.writeLong(Long.MAX_VALUE); out.flush();
      }
      //completarDatosIES(ies,out);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      synchronized (ServerDownload.class) {colaAtencion.remove(id);}
    }
    out.writeLong(9403879046015L); out.flush();
    baIES=IES.iesToByteArray(ies,pwd);
    out.writeInt(baIES.length); out.flush();
    out.writeLargeByteArray(true,baIES); out.flush();
    out.writeLong(1972387712038L); out.flush();
    IES.guardar(ConstantesServer.fDATOS,ies,true);
  }
}