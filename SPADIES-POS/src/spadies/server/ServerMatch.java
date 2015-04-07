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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.Map.Entry;

import spadies.io.*;
import spadies.kernel.*;
import spadies.util.*;
import spadies.server.kernel.*;
import spadies.server.util.*;
import static spadies.util.CajaDeHerramientas.*;

public class ServerMatch {
  private static PrintStream ps;
  private static long periodoActualizacionPassword = 5*60*1000;
  private static final DateFormat df=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  private static /*final*/ int puerto=Constantes.puertoServidorMatch;
  private static final List<Long> colaAtencion=Collections.synchronizedList(new LinkedList<Long>());
  public static final boolean[] tablasGrandesEncontradas=new boolean[4]; // ICFES,ICETEX,GRADUADOS
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
      ps = new PrintStream(new FileOutputStream(ConstantesServer.fLOG_ERROR_BLOQUEO,true));
      TablaProgramas.getInstance().preparar();
      TablaIES.getInstance().preparar();
      TablaPasswordsIES.getInstance().preparar();
      TablaPeriodosBloqueoIES.getInstance().preparar();
      TablaDepartamentos.getInstance().preparar();
      PreparadorDatos.getInstance().prepararArchivosBase(tablasGrandesEncontradas);
      System.gc();
      serverSocket=new ServerSocket(puerto);
      if (ConstantesServer.DEBUG_MODE) mensajeModoDebug();
    }
    catch (Throwable th) {
      th.printStackTrace(System.out);
      System.exit(1);
    }
    new Thread() {
      public void run() {
        while (true) {
          try {
            TablaPasswordsIES.getInstance().preparar();
            //TablaPeriodosBloqueoIES.getInstance().preparar();
          }
          catch (MyException ex) {
            System.out.println(ex.getMessage());
          }
          catch (Throwable th) {
            th.printStackTrace(System.out);
          }
          System.out.println("[PASSWORD][PERIODOS_BLOQUEO] Se actualizará de nuevo en "+(periodoActualizacionPassword/(1000L*60))+" minutos");
          try {Thread.sleep(periodoActualizacionPassword);} catch (Throwable th) {}
        }
      }
    }.start();
    System.out.println("EL SERVIDOR DE MATCHING DE "+Constantes.nombreAplicacionLargo+" ESTÁ ESCUCHANDO CONEXIONES");
    while (true) {
      try {
        final Socket clientSocket=serverSocket.accept();
        new Thread() {
          public void run() {
            byte[] addr = clientSocket.getInetAddress().getAddress();
            boolean addrOk = ((addr[0]&0xFF)==157 && (addr[1]&0xFF)==253 && ((addr[2]&0xFF)==187 ||(addr[2]&0xFF)==188)) || clientSocket.getInetAddress().isLoopbackAddress();
            long tm=System.currentTimeMillis();
            String address="",sErr="";
            try {
              if (ConstantesServer.DEBUG_MODE && !addrOk) {
                clientSocket.close();
                System.out.printf("Para %d.%d.%d.%d",addr[0]&0xFF,addr[1]&0xFF,addr[2]&0xFF,addr[3]&0xFF);
                System.out.println();
                return;
              }
              //cargarValorPromedioVariables();
              address=clientSocket.getInetAddress().toString();
              clientSocket.setSoTimeout(1000*60);
              atender(clientSocket);
            }
            catch (Throwable th) {
              sErr=th.getMessage();
              th.printStackTrace();
            }
            System.err.println(df.format(new Date())+";"+address+";"+(System.currentTimeMillis()-tm)+"ms;"+(sErr!=null?sErr.replace(';',','):""));
          }
        }.start();
      }
      catch (Throwable th) {
      }
    }
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
      if (ConstantesServer.DEBUG_MODE) pwd = ConstantesServer.DEBUG_MODE_PASSWD;
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
    if (ies.codigo!=codigoIES) return;
    if (in.readLong()!=4902403597593L) return;
    out.writeLong(1020579306849L); out.flush();
    Long id=0L;
    //boolean compatible = true;
    synchronized (ServerMatch.class) {colaAtencion.add(id=(Long)(System.currentTimeMillis()));}
    try {
      int posEnCola=-1;
      while (posEnCola!=0) {
        synchronized (ServerMatch.class) {posEnCola=colaAtencion.indexOf(id);}
        if (posEnCola==-1) return;
        out.writeLong(4671937598247L);
        out.writeInt(posEnCola);
        out.flush();
        if (posEnCola!=0) {
          try {Thread.sleep(10000);} catch (Throwable th) {}
        }
      }
      out.writeLong(7049449709348L); out.flush();
      /*{
        File fBl = new File(ConstantesServer.cDatosBloqueados,ies.codigo+".spa");
        System.out.println(fBl);
        System.out.println(fBl.exists());
        if (fBl.exists()) {
          IES iesBlo = IES.cargar(fBl, false);
          compatible = diagnosticosCompatibles(ies, iesBlo,TablaPeriodosBloqueoIES.getInstance().getPerBloqueoIES(ies.codigo));
        }
      }
      System.err.println(compatible);
      */
      completarDatosIES(ies,out);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      synchronized (ServerMatch.class) {colaAtencion.remove(id);}
    }
    out.writeLong(9403879046015L); out.flush();
    /*if (!compatible) {
      IES.guardar(ConstantesServer.fDATOSINV,ies,true);
      ies.codigo=-1;
    }*/
    baIES=IES.iesToByteArray(ies,pwd);
    out.writeInt(baIES.length); out.flush();
    out.writeLargeByteArray(true,baIES); out.flush();
    out.writeLong(1972387712038L); out.flush();
    /*if (compatible) */IES.guardar(ConstantesServer.fDATOS,ies,true);
  }
  public static void completarDatosIES(IES ies, MyDataOutputStream out) throws Exception {
    completarDatosIES(ies,out,true);
  }
  public static void completarDatosIES(IES ies, MyDataOutputStream out,boolean otros) throws Exception {
    int ne=ies.estudiantes.length;
    byte[][][] INFO_IES=new byte[3][ne][];
    boolean[] estudiantesYaConICFES=new boolean[ne];
    for (int i=0; i<ne; i++) {
      Estudiante_DatosPersonales edp=ies.datosPersonalesEstudiantes[i];
      INFO_IES[0][i]=codifLetrasServer.getCodigos(new String(edp.apellido)+" "+new String(edp.nombre));
      INFO_IES[1][i]=codifNumeros.getCodigos((edp.documento!=-1)?(""+edp.documento):"");
      INFO_IES[2][i]=codifNumeros.getCodigos((edp.anhoFechaNacimiento!=-1 && edp.mesFechaNacimiento!=-1 && edp.diaFechaNacimiento!=-1)?(intToString(edp.anhoFechaNacimiento%100,2)+intToString(edp.mesFechaNacimiento,2)+intToString(edp.diaFechaNacimiento,2)):"");
      estudiantesYaConICFES[i]=ies.estudiantes[i].getSonDatosICFESproveidosPorSNIES();
    }
    completarDatosIES_PROGRAMAS(ies);
    MatcherSPADIES m1=tablasGrandesEncontradas[0]?new MatcherSPADIES(INFO_IES,97.99,true,ConstantesServer.gICFES,out):null;
    MatcherSPADIES m2=tablasGrandesEncontradas[1]?new MatcherSPADIES(INFO_IES,97.99,false,ConstantesServer.gICETEX,out):null;
    MatcherSPADIES m3=tablasGrandesEncontradas[2]?new MatcherSPADIES(INFO_IES,97.99,true,ConstantesServer.gGRADUADOS,out):null;
    MatcherSPADIES m4=tablasGrandesEncontradas[3]?new MatcherSPADIES(INFO_IES,97.99,true,ConstantesServer.gSBPRO,out):null;
    long totM1=((m1!=null)?m1.total:0),totM2=((m2!=null)?m2.total:0),totM3=((m3!=null)?m3.total:0),totM4=((m4!=null)?m4.total:0);
    out.writeLong(Math.max(totM1+totM2+totM3+totM4,1L)); out.flush();
    if (m1!=null) {
      completarDatosIES_ICFES(ies,m1.procesar(0,estudiantesYaConICFES));
    }
    else {
      out.writeLong(Long.MAX_VALUE); out.writeLong(Long.MAX_VALUE); out.flush();
    }
    if (m2!=null) {
      completarDatosIES_ICETEX(ies,m2.procesar(totM1,null));
    }
    else {
      out.writeLong(Long.MAX_VALUE); out.writeLong(Long.MAX_VALUE); out.flush();
    }
    if (m3!=null) {
      completarDatosIES_GRADUADOS(ies,m3.procesar(totM1+totM2,null));
    }
    else {
      out.writeLong(Long.MAX_VALUE); out.writeLong(Long.MAX_VALUE); out.flush();
    }
    if (m4!=null) {
      completarDatosIES_SBPRO(ies,m4.procesar(totM1+totM2+totM3,null));
    }
    else {
      out.writeLong(Long.MAX_VALUE); out.writeLong(Long.MAX_VALUE); out.flush();
    }
    if (otros) completarDatosIES_OTROS(ies);
  }
  private static void completarDatosIES_PROGRAMAS(IES ies) {
    for (Programa p:ies.programas) if (p.codigoSNIES.length>0) {
      byte[][] datos=TablaProgramas.getInstance().getDatosPorSNIES(p.codigoSNIES);
      if (datos==null) datos=TablaProgramas.getInstance().getDatosPorConsecutivo(p.codigoSNIES);
      if (datos==null) continue;
      int area=stringToInteger(new String(datos[2])),nivel=stringToInteger(new String(datos[3])),nucleo=stringToInteger(new String(datos[4])),metodo=stringToInteger(new String(datos[5]));
      //System.out.println(new String(p.nombre)+"\t"+metodo+"\t"+nucleo);
      if (area<1 || area>9) area=-1;
      if (nivel<0 || nivel>9) nivel=-1;
      if (nucleo<1 || nucleo>70) nucleo=-1;
      if (metodo<1 || metodo>2) metodo=-1;
      p.nombre=datos[0];
      p.area=(byte)area;
      p.nivel=(byte)nivel;
      p.nucleo=(byte)nucleo;
      p.metodologia = (byte)metodo;
    }
  }
  private static void completarDatosIES_ICFES(IES ies, byte[][] res) {
    for (int i=0,ne=ies.estudiantes.length; i<ne; i++) if (res[i]!=null) {
      Estudiante e=ies.estudiantes[i];
      int semEST=e.getSemestrePrimiparo(),semICFES=-1;
      if (semEST!=-1) semEST+=ies.minCodigoSemestre;
      byte datoICFES[]=null,arr[]=res[i];
      //for (int f=0,fT=arr.length; f+5<fT; f+=6) { //Eso era con 6
      //  byte dato[]=getSubArreglo(arr,f,f+5);
      for (int f=0,fT=arr.length; f+14<fT; f+=15) {
        byte dato[]=getSubArreglo(arr,f,f+14);
        int sem=dato[0];
        if (sem>=0 && (semEST==-1 || sem<=semEST) && (semICFES==-1 || sem>semICFES)) {
          datoICFES=dato;
          semICFES=sem;
        }  
      }
      if (datoICFES==null) continue;
      MyByteSequence bs=new MyByteSequence(datoICFES);
      e.setPuntajeICFES(bs.getByte(1)-1);
      e.setEdadAlPresentarElICFES(bs.getByte(2)-1);
      e.setViviendaPropia(bs.getBits2(3,0)-1);
      e.setTrabajabaCuandoPresentoIcfes(bs.getBits2(3,2)-1);
      e.setNivelEducativoMadre(bs.getBits4(3,4)-1);
      e.setIngresoHogar(bs.getBits4(4,0)-1);
      if (e.getSexo()==-1) e.setSexo(bs.getBits2(4,4)-1);
      int numHermanos=bs.getBits4(5,0);
      int posHermanos=bs.getBits4(5,4);
      if (numHermanos==0) numHermanos=-1;
      if (posHermanos==0) posHermanos=-1;
      e.setNumeroHermanos(numHermanos);
      e.setPosicionEntreLosHermanos(posHermanos);
      e.setRemplazoICFES(bs.getBits2(4, 6)==1);
      e.setPerIcfes(bs.getByte(6)-1);
      e.setPerDatIcfes(bs.getByte(7)-1);
      e.setEstrato(bs.getBits4(8,0)-1);
      e.setNivelSisben(bs.getBits4(8,4)-1);
      e.setPersonasFamilia(bs.getBits4(9,0)-1);
      e.setIngresoHogar2(bs.getBits4(9,4)-1);
      //Modas
      e.setmViviendaPropia(bs.getBits2(10,0)-1);
      e.setmTrabajabaCuandoPresentoIcfes(bs.getBits2(10,2)-1);
      e.setmNivelEducativoMadre(bs.getBits4(10,4)-1);
      e.setmIngresoHogar(bs.getBits4(11,0)-1);
      int mnumHermanos=bs.getBits4(12,0);
      int mposHermanos=bs.getBits4(12,4);
      if (mnumHermanos==0) mnumHermanos=-1;
      if (mposHermanos==0) mposHermanos=-1;
      e.setmNumeroHermanos(mnumHermanos);
      e.setmPosicionEntreLosHermanos(mposHermanos);
      e.setmEstrato(bs.getBits4(13,0)-1);
      e.setmNivelSisben(bs.getBits4(13,4)-1);
      e.setmPersonasFamilia(bs.getBits4(14,0)-1);
      e.setmIngresoHogar2(bs.getBits4(14,4)-1);
    }
  }
  private static void completarDatosIES_ICETEX(IES ies, byte[][] res) {
    for (int i=0,ne=ies.estudiantes.length; i<ne; i++) if (res[i]!=null) {
      Estudiante e=ies.estudiantes[i];
      int semEST=e.getSemestrePrimiparo();
      if (semEST!=-1) semEST+=ies.minCodigoSemestre;
      byte[] arr=res[i];
      for (int f=0,fT=arr.length; f+1<fT; f+=2) {
        int sem=arr[f];
        if (sem>=ies.minCodigoSemestre && sem<=ies.maxCodigoSemestre && (semEST==-1 || sem>=semEST)) e.setTipoApoyoICETEXRecibido(sem-ies.minCodigoSemestre,arr[f+1]);
      }
    }
  }
  private static void completarDatosIES_GRADUADOS(IES ies, byte[][] res) {
    for (int i=0,ne=ies.estudiantes.length; i<ne; i++) if (res[i]!=null) {
      Estudiante e=ies.estudiantes[i];
      int semEST=e.getSemestrePrimiparo();
      if (semEST!=-1) semEST+=ies.minCodigoSemestre;
      int semGRAD=-1;
      byte arr[]=res[i];
      for (int f=0,fT=arr.length; f+4<fT; f+=5) {
        byte dato[]=getSubArreglo(arr,f,f+4);
        int sem=dato[0],consecutivo=new MyByteSequence(dato).getInt(1);
        byte[][] datosPrograma=TablaProgramas.getInstance().getDatosPorConsecutivo((""+consecutivo).getBytes());
        if (datosPrograma==null || !new String(datosPrograma[2]).equals(""+ies.codigo)) continue;
        if ((semEST==-1 || sem>=semEST) && (semGRAD==-1 || sem>semGRAD)) semGRAD=sem;
      }
      if (semGRAD==-1 || semGRAD<ies.minCodigoSemestre || semGRAD>ies.maxCodigoSemestre) continue;
      int indice1=semGRAD-ies.minCodigoSemestre,indice2=e.getSemestreGrado();
      if (indice2==-1) e.setSemestreGrado(indice1);
    }
  }
  private static void completarDatosIES_SBPRO(IES ies, byte[][] res) {
    for (int i=0,ne=ies.estudiantes.length; i<ne; i++) if (res[i]!=null) {
      Estudiante e=ies.estudiantes[i];
      int semEST=e.getSemestrePrimiparo(),semICFES=-1;
      if (semEST!=-1) semEST+=ies.minCodigoSemestre;
      byte dato[]=null,arr[]=res[i];
      //for (int f=0,fT=arr.length; f+5<fT; f+=6) { //Eso era con 6
      //  byte dato[]=getSubArreglo(arr,f,f+5);
      for (int f=0,fT=arr.length; f+4<fT; f+=5) {
        dato =getSubArreglo(arr,f,f+4);
        /*int sem=dato[0];
        if (sem>=0 && (semEST==-1 || sem<=semEST) && (semICFES==-1 || sem>semICFES)) {
          datoICFES=dato;
          semICFES=sem;
        }  */
      }
      if (dato==null) continue;
      MyByteSequence bs=new MyByteSequence(dato);
      e.setPuntajeSBPRO(bs.getByte(0)-1);
      e.setEdadSBPRO(bs.getByte(1)-1);
      e.setAreaSBPRO(bs.getBits4(2,0)-1);
      e.setEducacionMadreSBPRO(bs.getBits4(2,4)-1);
      e.setOcupacionMadreSBPRO(bs.getBits4(3,0)-1);
      e.setInternetSBPRO(bs.getBits2(3,4)-1);
      e.setValorMatriculaSBPRO(bs.getBits4(4,0)-1);
    }
  }
  private static void completarDatosIES_OTROS(IES ies) {
    completarDatosIES_datosIES(ies);
    completarDatosIES_costosProgramas(ies);
    completarDatosIES_adivinarSexo(ies);
    completarDatosIES_adivinarAreaPrograma(ies);
    completarDatosIES_calcularRiesgoGenerico(ies);
  }
  private static void completarDatosIES_datosIES(IES ies) {
    byte[][] datos=TablaIES.getInstance().getDatos(ies.codigo);
    if (datos==null) return;
    ies.nombre=datos[0];
    ies.departamento=(byte)(stringToInteger(new String(datos[1])));
    ies.municipio=(short)(stringToInteger(new String(datos[2])));
    ies.origen=(byte)(stringToInteger(new String(datos[3])));
    ies.caracter=(byte)(stringToInteger(new String(datos[4])));
  }
  public static void completarDatosIES_adivinarSexo(IES ies) {
    for (int i=0,ne=ies.estudiantes.length; i<ne; i++) {
      Estudiante e=ies.estudiantes[i];
      Estudiante_DatosPersonales edp=ies.datosPersonalesEstudiantes[i];
      if (e.getSexo()==-1) e.setSexo(AdivinadorSexo.adivinarSexo(new String(edp.nombre)));
    }
  }
  private static void completarDatosIES_adivinarAreaPrograma(IES ies) {
    for (Programa p:ies.programas) if (p.area==-1) p.area=AdivinadorAreasProgramas.adivinarAreaPrograma(new String(p.nombre));
  }
  //TODO machetemp private=>public
  /*private static double genero_d=0d,
  edad_d=0,
  trabaja_d=0,
  ingreso_d=0,
  hermanos_d=0,
  totalicfes_d=0,
  a1_d=0,
  a2_d=0,
  a3_d=0,
  a5_d=0,
  a6_d=0,
  a7_d=0,
  a8_d=0,
  a9_d=0,
  madniv1_d=0,
  madniv2_d=0,
  madniv3_d=0,
  numerohijo_d=0,
  viviendapropia_d=0,
  repitencia_d=0;
  */
  public static void completarDatosIES_calcularRiesgoGenerico(IES ies) {
    byte origen = ies.origen, caracter=ies.caracter,depto=ies.departamento;
    if (caracter==9) caracter = -1;
    if (caracter==5) caracter =  2;
    if (origen==7 || origen==8) origen =  2;
    if (origen==-1 || caracter==-1 || depto==-1) return;
    double prIES=0d;
    int oficial=(origen==7 || origen==8)?0:1;
    {
      int univ=caracter==1?1:0,
          iuni=caracter==2?1:0,
          itec=caracter==3?1:0;
      int 
          d8  = depto==8?1:0,
          d11 = depto==11?1:0,
          d13 = depto==13?1:0,
          d15 = depto==15?1:0,
          d17 = depto==17?1:0,
          d18 = depto==18?1:0,
          d19 = depto==19?1:0,
          d20 = depto==20?1:0,
          d23 = depto==23?1:0,
          d25 = depto==25?1:0,
          d27 = depto==27?1:0,
          d41 = depto==41?1:0,
          d44 = depto==44?1:0,
          d47 = depto==47?1:0,
          d50 = depto==50?1:0,
          d52 = depto==52?1:0,
          d54 = depto==54?1:0,
          d63 = depto==63?1:0,
          d66 = depto==66?1:0,
          d68 = depto==68?1:0,
          d70 = depto==70?1:0,
          d73 = depto==73?1:0,
          d76 = depto==76?1:0,
          d81 = depto==76?1:0,
          d86 = depto==76?1:0,
          d88 = depto==76?1:0;
      prIES =
        oficial * -0.011637 +
        itec    * -0.001878 +
        iuni    * -0.021095 +
        univ    * -0.023743 +
        d8      *  0.003363 +
        d11     *  0.004182 +
        d13     *  0.005221 +
        d15     * -0.002544 +
        d17     * -0.013947 +
        d18     * -0.015756 +
        d19     * -0.008505 +
        d20     * -0.008028 +
        d23     *  0.006932 +
        d25     *  0.015722 +
        d27     * -0.064626 +
        d41     * -0.013355 +
        d44     *  0.001232 +
        d47     *  0.014784 +
        d50     * -0.006259 +
        d52     *  0.003056 +
        d54     * -0.000789 +
        d63     *  0.012076 +
        d66     *  0.000241 +
        d68     *  0.002752 +
        d70     * -0.005101 +
        d73     *  0.010120 +
        d76     *  0.005118 +
        d81     *  0.017225 +
        d86     * -0.007537 +
        d88     *  0.023880 +
                 (-0.172046);
    }
    for (Estudiante e:ies.estudiantes) {
      {//Evalúa si tiene datos estáticos para calcular el modelo
        int sexo=e.getSexo(),edadPresentacionIcfes=e.getEdadAlPresentarElICFES(),trabajabaCuandoIcfes=e.getTrabajabaCuandoPresentoIcfes();
        int ingresoHogar=e.getIngresoHogar2(),numeroHermanos=e.getNumeroHermanos(),puntajeIcfes=e.getPuntajeICFES();
        int educacionMadre=e.getNivelEducativoMadre(),posicionEntreHermanos=e.getPosicionEntreLosHermanos(),viviendaPropia=e.getViviendaPropia();
        int indicePrograma=e.getIndicePrograma();
        int areaPrograma=(indicePrograma==-1)?-1:ies.programas[indicePrograma].area;
        int nforma = (indicePrograma==-1)?-1:ies.programas[indicePrograma].nivel;
        if (Arrays.asList((byte)3,(byte)5,(byte)6,(byte)7,(byte)9).contains(nforma)) nforma = -1;
        if (sexo==-1 || edadPresentacionIcfes==-1 || trabajabaCuandoIcfes==-1 || ingresoHogar==-1 || numeroHermanos==-1 || puntajeIcfes==-1 || areaPrograma==-1 || educacionMadre==-1 || posicionEntreHermanos==-1 || viviendaPropia==-1 || educacionMadre==-1 || nforma==-1) continue;
        double prEstructural=prIES;
        {
          double
          genero=sexo,
          edad=edadPresentacionIcfes,
          edadcuadra=edad*edad,
          trabaja=trabajabaCuandoIcfes,
          ingreso=(ingresoHogar<=2?1:(ingresoHogar<=4?2:3)),
          hermanos=numeroHermanos,
          totalicfes=puntajeIcfes,
          nforma1=(nforma==1?1:0),
          nforma2=(nforma==2?1:0),
          a1=(areaPrograma==1?1:0),
          a2=(areaPrograma==2?1:0),
          a3=(areaPrograma==3?1:0),
          a5=(areaPrograma==5?1:0),
          a6=(areaPrograma==6?1:0),
          a7=(areaPrograma==7?1:0),
          a8=(areaPrograma==8?1:0),
          madniv1=(educacionMadre==1?1:0),
          madniv2=(educacionMadre==2?1:0),
          madniv3=(educacionMadre==3?1:0)
          ;
          prEstructural+=
           -0.012885  * genero      +
            0.006060  * trabaja     +
            0.007715  * edad        +
           -0.000132  * edadcuadra  +
           -0.001621  * ingreso     +
            0.001362  * hermanos    +
            0.008225  * madniv1     +
            0.006836  * madniv2     +
            0.003452  * madniv3     +
           -0.047386  * totalicfes  +
            0.028542  * nforma1     +
            0.015249  * nforma2     +
            0.019778  * a1          +
            0.014833  * a2          +
            0.021260  * a3          +
            0.015072  * a5          +
            0.016275  * a6          +
            0.025194  * a7          +
            0.033755  * a8          
            ; 
        }
        int apoyoOtroAnterior=0,segvar=0;
        long matri=e.getSemestresMatriculadoAlDerecho();
        double repitencias[]=e.getRepitencias();
        int apoyoAcademicoT = 0;
        for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) if (e.getRecibioApoyoAcademico(j)) apoyoAcademicoT++;
        if (apoyoAcademicoT>0) apoyoAcademicoT = 1; 
        matri=e.getSemestresMatriculadoAlDerecho();
        for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
          double prPeriodo=prEstructural;
          ++segvar;
          double lsegvar=Math.log(segvar);
          prPeriodo+=
            -0.060688 * lsegvar            
            ;
          e.setRiesgoEstructural(j,1d/(1d+Math.exp(-prPeriodo)));
          {
            double repitencia=/*repitencias[j]==-1?repitencia_d:*/repitencias[j];
            if (repitencia==-1d) continue;
            int financiero=e.getRecibioApoyoFinanciero(j)?1:0;
            apoyoOtroAnterior=Math.max(apoyoOtroAnterior,(e.getRecibioApoyoAcademico(j) || e.getRecibioApoyoOtro(j)?1:0));
            int apoyoOtro=e.getRecibioApoyoOtro(j)?1:0;
            byte tipApoICETEX=e.getTipoApoyoICETEXRecibido(j);
            int ictxl=tipApoICETEX==1?1:0,
                ictxm=tipApoICETEX==2?1:0,
                ictxa=tipApoICETEX==3?1:0;
            prPeriodo+=
               0.110209 * repitencia        +
              -0.018151 * ictxl             +
              -0.011032 * ictxm             +
              -0.025902 * ictxa             +
              -0.017466 * financiero        +
              -0.001789 * apoyoAcademicoT   +
              -0.022256 * apoyoOtro
               ;
            e.setRiesgo(j,1d/(1d+Math.exp(-prPeriodo)));
          }
        }
      }
    }
  }
  private static void completarDatosIES_costosProgramas(IES ies) {
    Map<String, Double> relativo = new TreeMap<String, Double>();
    relativo.put("19981", 0.508795238);
    relativo.put("19982", 0.508795238);
    relativo.put("19991", 0.593781211);
    relativo.put("19992", 0.593781211);
    relativo.put("20001", 0.648587216);
    relativo.put("20002", 0.648587216);
    relativo.put("20011", 0.7053527);
    relativo.put("20012", 0.7053527);
    relativo.put("20021", 0.759268034);
    relativo.put("20022", 0.759268034);
    relativo.put("20031", 0.812352074);
    relativo.put("20032", 0.812352074);
    relativo.put("20041", 0.865079846);
    relativo.put("20042", 0.865079846);
    relativo.put("20051", 0.912641721);
    relativo.put("20052", 0.912641721);
    relativo.put("20061", 0.956937799);
    relativo.put("20062", 0.956937799);
    relativo.put("20071", 1.0);
    relativo.put("20072", 1.0);
    relativo.put("20081", 1.0569);//Con inflacion 2007
    relativo.put("20082", 1.0569);
    relativo.put("20091", 1.138051994);//Con inflacion 2008
    relativo.put("20092", 1.138051994);
    relativo.put("20101", 1.160609936);//Con inflacion 2009
    relativo.put("20102", 1.160609936);
    {
      double val = 1.160609936, t = 1+0.0198215394903716;
      for (int year=2011;year<=2040;year++) {
        val*=t;
        relativo.put(year+"1", val);
        relativo.put(year+"2", val);
      }
    }
    Map<String, Integer> costos2007 = new TreeMap<String, Integer>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(ConstantesServer.fCOSTOS2007));
      //System.out.println(br.readLine());
      br.readLine();
      for (String s=br.readLine();s!=null;s=br.readLine()) {
        String[] sp = CajaDeHerramientas.csvToString(s, 0, ';');
        String cod = sp[4];
        int val0 = Integer.parseInt(sp[8]);
        int val1 = Integer.parseInt(sp[9]);
        int val = Math.max(val0, val1);
        //System.out.println(cod+"\t"+val);
        costos2007.put(cod, val);
      }
      costos2007.remove("");
      br.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
    //System.out.println(costos2007);
    ies.costosProgramas = new int[ies.programas.length][ies.n];
    for (int[] costosPrograma: ies.costosProgramas)
      Arrays.fill(costosPrograma, -1);
    int i = 0;
    for (Programa p:ies.programas) {
      int[] costosPrograma = ies.costosProgramas[i++];
      Integer precio = costos2007.get(new String(p.codigoSNIES));
      if (precio==null) continue;
      //for (int per:new int[]{pers[0]-ies.minCodigoSemestre,pers[1]-ies.minCodigoSemestre}) {
      for (int per=0;per<ies.n;per++) {
        //System.out.println("OK"+per+"\t"+relativo.get(CajaDeHerramientas.getTextoSemestre((byte) per)));
        //if (0<=per && per<ies.n) costosPrograma[per] = precio;
        if (0<=per && per<ies.n) costosPrograma[per] = (int) Math.floor(precio*relativo.get(CajaDeHerramientas.getTextoSemestre((byte) per)));
      }
    }
  }
  /*protected static void cargarValorPromedioVariables() throws NumberFormatException, IOException {
    BufferedReader br = new BufferedReader(new FileReader("valoresPredeterminadosModelo.csv"));
    edad_d = Double.parseDouble(br.readLine());
    trabaja_d = Double.parseDouble(br.readLine());
    ingreso_d = Double.parseDouble(br.readLine());
    hermanos_d = Double.parseDouble(br.readLine());
    totalicfes_d = Double.parseDouble(br.readLine());
    a1_d = Double.parseDouble(br.readLine());
    a2_d = Double.parseDouble(br.readLine());
    a3_d = Double.parseDouble(br.readLine());
    a6_d = Double.parseDouble(br.readLine());
    a8_d = Double.parseDouble(br.readLine());
    a9_d = Double.parseDouble(br.readLine());
    madniv1_d = Double.parseDouble(br.readLine());
    madniv2_d = Double.parseDouble(br.readLine());
    numerohijo_d = Double.parseDouble(br.readLine());
    viviendapropia_d = Double.parseDouble(br.readLine());
    repitencia_d = Double.parseDouble(br.readLine());
    br.close();
  }*/
  private static void mensajeModoDebug() {
    String msj[] = new String[]{
      "***********************",
      "* DD  EEE BB  U U GGG *",
      "* D D E   B B U U G G *",
      "* D D EEE BB  U U GGG *",
      "* D D E   B B U U   G *",
      "* DD  EEE B B UUU GGG *",
      "***********************",
    };
    for (String s:msj) System.out.println(s);
  }
  
  protected static Map<Integer,int[]> diagnosticoIES(IES ies) {
    Map<Integer,int[]> res = new TreeMap<Integer,int[]>();
    int mc = ies.minCodigoSemestre;
    /*for (int i=0,it=ies.n;i<it;i++) {
      res.put(mc+i, new int[4]);
    }*/
    {
      int[][] conteos = new int[ies.n][4];
      for (Estudiante est:ies.estudiantes) {
        int sp = est.getSemestrePrimiparo(),sg = est.getSemestreGrado(),sr=est.getSemestreRetiroForzoso();
        if (sp!=-1) conteos[sp][0]++;//Primiparo
        if (sg!=-1) conteos[sg][2]++;//Graduado
        if (sr!=-1) conteos[sr][3]++;//Retirado
        long matri=est.getSemestresMatriculadoAlDerecho()>>>sp;
        for (int i=sp,iT=ies.n; i<iT; i++,matri>>>=1)
          if ((matri&1L)==1L)
            conteos[i][1]++;//Matriculado
      }
      for (int i=0,it=ies.n;i<it;i++) {
        res.put(mc+i, conteos[i]);
      }
    }
    /*System.out.println("--------");
    for (Entry<Integer,int[]> ent:res.entrySet()) {
      int[] val = ent.getValue();
      System.out.println(Arrays.asList(ent.getKey(),val[0],val[1],val[2],val[3]));
    }*/
    return res;
  }
  protected static boolean diagnosticosCompatibles(IES ies1,IES ies2,Integer lim) {
    System.out.println(ies1.codigo);
    System.out.println(lim);
    Map<Integer,int[]> m1t = diagnosticoIES(ies1);
    Map<Integer,int[]> m2t = diagnosticoIES(ies2);
    Map<Integer,int[]> m1 = new TreeMap<Integer, int[]>(m1t);
    Map<Integer,int[]> m2 = new TreeMap<Integer, int[]>(m2t);
    m1t.keySet().retainAll(m2t.keySet());
    for (Integer per:m1t.keySet()) if (lim==null||per<=lim){
      int[] c1 = m1.get(per),c2 = m2.get(per);
      System.out.println(Arrays.asList(c1[0],c1[1],c1[2],c1[3]));
      System.out.println(Arrays.asList(c2[0],c2[1],c2[2],c2[3]));
      for (int i=0;i<4;i++)
        if(c1[i]!=c2[i]) {
          String fec = df.format(new Date());
          ps.println(CajaDeHerramientas.stringToCSV(fec,"SINC",""+ies1.codigo,CajaDeHerramientas.codigoSemestreToString((byte)per.intValue()),
              ""+c1[0],""+c1[1],""+c1[2],""+c1[3]));
          ps.println(CajaDeHerramientas.stringToCSV(fec,"BLOQ",""+ies2.codigo,CajaDeHerramientas.codigoSemestreToString((byte)per.intValue()),
              ""+c2[0],""+c2[1],""+c2[2],""+c2[3]));
          return false;
        }
    }
    return true;
  }
}