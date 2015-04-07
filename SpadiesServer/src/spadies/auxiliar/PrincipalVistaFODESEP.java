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
package spadies.auxiliar;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import spadies.io.MyDataInputStream;
import spadies.io.MyDataOutputStream;
import spadies.kernel.*;
import spadies.server.kernel.*;
import spadies.server.kernel.PreparadorDatos.ArchivoCSV;
import spadies.util.*;
import spadies.util.variables.*;
import static spadies.util.CajaDeHerramientas.*;

public class PrincipalVistaFODESEP {
  private static final KernelSPADIES kernel=KernelSPADIES.getInstance();
  private static final DateFormat df=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  private static final int puerto=Constantes.puertoServidorConsultas;
  private static final long periodoActualizacion=1000L*60*30;  // 30 minutos
  private static boolean clean = false;
  private static long tInic;
  public static void main(String[] args) throws Exception {
    {
      if (args.length>0 && args[0].equals("CLEAN")) clean = true;
    }
    tInic = System.currentTimeMillis();
    FileOutputStream fos = new FileOutputStream(new File("bla"));
    File fBeneT = new File("datFOD_BEN/benef2.csv");
    final File fSisben = new File("datFOD_BEN/sisben.csv"),
      fSisbenPro = new File("datFOD_BEN/pros/sisben.procesado"),
      fSisbenInd = new File("datFOD_BEN/sisben.csv.indcsv");
    final File fSisbenBeneficiarios = new File("datFOD_BEN/datBEN_SIS.out.csv"),
      fSisbenBeneficiariosPro = new File("datFOD_BEN/pros/datBEN_SIS.out.procesado"),
      fSisbenBeneficiariosInd = new File("datFOD_BEN/datBEN_SIS.out.csv.indcsv");
    final File fECAES = new File("BaseOR/ecaes_sint2004a2006reduc.csv"),
      fECAESPro = new File("datFOD_BEN/pros/ecaes_sint2004a2006reduc.procesado"),
      fECAESInd = new File("datFOD_BEN/ecaes_sint2004a2006reduc.csv.indcsv");
    final File fObs = new File("graduado_base.csv"),
      fObsPro = new File("datFOD_BEN/pros/graduado_base.procesado"),
      fObsInd = new File("datFOD_BEN/graduado_base.csv.indcsv");
    /*
    final File fSisbenBeneficiariosObsGrad = new File("datFOD_BEN/datBEN_SIS_OBS_GRA.out.csv"),
      fSisbenBeneficiariosObsGradPro = new File("datFOD_BEN/pros/datBEN_SIS_OBS_GRA.out.procesado"),
      fSisbenBeneficiariosObsGradInd = new File("datFOD_BEN/datBEN_SIS_OBS_GRA.out.csv.indcsv");
      */
    final File fSisbenBeneficiariosRBase = new File("datFOD_BEN/datBEN_SIS_SPAR.out.csv");
    File fSalidaDEF = new File("grandVis.csv");
    if (clean) {
      System.out.println("Limpiando Archivos");
      fSisbenPro.delete();
      fSisbenInd.delete();
      fSisbenBeneficiarios.delete();
      fSisbenBeneficiariosPro.delete();
      fSisbenBeneficiariosInd.delete();
      fSisbenBeneficiariosRBase.delete();
    }
    if (!fSisbenBeneficiarios.exists()) {//Match SISBEN-Beneficiarios
      ArchivoCSV arsis = new ArchivoCSV() {
        int conteo = 0;
        public String getTitulo() {return "SISBEN";}
        public File getIn() {return fSisben;}
        public File getOut() {return fSisbenPro;}
        public String[] getIdLinea(String[] w) {return new String[]{w[244],w[10]};}
        public byte[] getDatoLinea(String[] w) {
          MyByteSequence mbs = new MyByteSequence(4);
          mbs.setInt(0, conteo++);
          return mbs.getBytes();
        }
      };
      PreparadorDatos pd = PreparadorDatos.getInstance();
      try {
        pd.prepararArchivoBase(arsis);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (!fSisbenInd.exists()) indexarCSV(fSisben, fSisbenInd);
      byte[][][] INFO_IES = new byte[3][10000][];//Lazye :P
      {
        BufferedReader br = new BufferedReader(new FileReader(fBeneT));
        br.readLine();
        int i = 0;
        for (String s = br.readLine();s!=null;s = br.readLine()) {
          String sL[] = csvToString(s,0,';');
          INFO_IES[0][i]=codifLetrasServer.getCodigos(sL[3] + sL[4]);
          INFO_IES[1][i]=codifNumeros.getCodigos(sL[5]);
          INFO_IES[2][i]= new byte[0];//TODO deberia
          ++i;
        }
        br.close();
      }
      PrintStream ps = new PrintStream(fSisbenBeneficiarios);
      {
        System.out.println("SISBEN: Realizando Match");
        MatcherSPADIES me = new MatcherSPADIES(INFO_IES,97.99,true,arsis.getOut(),new MyDataOutputStream(fos));
        byte[][] resi = me.procesar(me.total, null);
        System.out.println("SISBEN: Match Completado");
        System.out.println("SISBEN: Cargando Indice");
        int[] indsis = cargarIndiceCSV(fSisbenInd);
        boolean[] visitados = new boolean[indsis.length];
        Arrays.fill(visitados, false);
        System.out.println("SISBEN: Indice Cargado");
        RandomAccessFile raf = new RandomAccessFile(fSisben, "r");
        raf.seek(0);
        String[] enc2 = splitCSV(raf.readLine());
        BufferedReader br = new BufferedReader(new FileReader(fBeneT));
        String[] enc1 = splitCSV(br.readLine());
        ps.println(stringToCSV(enc1) + stringToCSV(enc2));
        for (int i=0, it = resi.length;i < it;i++) {
          String[] dat1 = splitCSV(br.readLine()),
            dat2 = null;
          if (resi[i]==null) {
            dat2 = new String[enc2.length];
            Arrays.fill(dat2, "");
          } else {
            int ind = new MyByteSequence(resi[i]).getInt(0);
            visitados[ind] = true;
            raf.seek(indsis[ind]);
            dat2 = splitCSV(raf.readLine());
          }
          ps.println(stringToCSV(dat1) + stringToCSV(dat2));
        }
        br.close();
        String[] dat1v = new String[enc1.length];
        Arrays.fill(dat1v, "");
        for (int i = 0, it = visitados.length;i < it;i++) {
          if (visitados[i] == true) continue;
          raf.seek(indsis[i]);
          String[] dat2 = splitCSV(raf.readLine());
          ps.println(stringToCSV(dat1v) + stringToCSV(dat2));
        }
        raf.close();
      }
      ps.close();
    }
    try {
      kernel.cargarParaServidor(Constantes.carpetaDatos,periodoActualizacion);
    }
    catch (MyException ex) {
      System.out.println(ex.getMessage());
    }
    catch (Throwable th) {
      th.printStackTrace(System.out);
    }
    impresionT("Cargados SPA.");
    impresionT("IES: " + kernel.listaIES.length + " Individuos: " + kernel.getCantidadEstudiantes(new Filtro[0]));
    /*{
      PrintStream ost = new PrintStream(new FileOutputStream("IES_TEMP.csv"));
      for (IES ies:kernel.listaIES) {
        ost.println(stringToCSV(ies.codigo+"", ies.municipio+"", ies.departamento+""));
      }
      ost.close();
    }*/
    if (!fSisbenBeneficiariosRBase.exists()){//Match SISBEN_Benef con SPADIES
      impresionT("Generacion UNION[SISBEN_BENEFICIARIOS, SPADIES]");
      ArchivoCSV arBENSIS = new ArchivoCSV() {
        int conteo = 0;
        public String getTitulo() {return "SISBEN U BENEFICIARIOS";}
        public File getIn() {return fSisbenBeneficiarios;}
        public File getOut() {return fSisbenBeneficiariosPro;}
        public String[] getIdLinea(String[] w) {
          return new String[]{w[273].trim().length()==0?w[3] + " " + w[4]: w[273],
              w[5].trim().length()==0?w[39]:w[5]};
        }
        public byte[] getDatoLinea(String[] w) {
          MyByteSequence mbs = new MyByteSequence(4);
          mbs.setInt(0, conteo++);
          return mbs.getBytes();
        }
      };
      PreparadorDatos pd = PreparadorDatos.getInstance();
      try {
        pd.prepararArchivoBase(arBENSIS);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (!fSisbenBeneficiariosInd.exists()) indexarCSV(fSisbenBeneficiarios, fSisbenBeneficiariosInd);
      impresionT("SISBEN_BENEFICIARIOS: Cargando Indice");
      int[] indsis = cargarIndiceCSV(fSisbenBeneficiariosInd);
      impresionT("SISBEN_BENEFICIARIOS: Indice Cargado");
      impresionT("SISBEN_BENEFICIARIOS: Individuos " + indsis.length);
      int ne= kernel.getCantidadEstudiantes(new Filtro[0]);
      int numEst = 0;
      byte[][][] INFO_IES = new byte[3][ne][];
      for (IES ies:kernel.listaIES) {
        for (int i=0, nei = ies.estudiantes.length;i<nei;i++) {
          Estudiante_DatosPersonales edp = ies.datosPersonalesEstudiantes==null?null:ies.datosPersonalesEstudiantes[i];
          INFO_IES[0][numEst]=codifLetrasServer.getCodigos(new String(edp.apellido)+" "+new String(edp.nombre));
          INFO_IES[1][numEst]=codifNumeros.getCodigos((edp.documento!=-1)?(""+edp.documento):"");
          INFO_IES[2][numEst]=codifNumeros.getCodigos((edp.anhoFechaNacimiento!=-1 && edp.mesFechaNacimiento!=-1 && edp.diaFechaNacimiento!=-1)?(intToString(edp.anhoFechaNacimiento%100,2)+intToString(edp.mesFechaNacimiento,2)+intToString(edp.diaFechaNacimiento,2)):"");
          ++numEst;
        }
      }
      PrintStream ps = new PrintStream(fSisbenBeneficiariosRBase);
      {
        impresionT("SISBEN_BENEFICIARIOS: Realizando Match");
        MatcherSPADIES me = new MatcherSPADIES(INFO_IES,97.99,true,arBENSIS.getOut(),new MyDataOutputStream(fos));
        byte[][] resi = me.procesar(me.total, null);
        impresionT("SISBEN_BENEFICIARIOS: Match Completado");
        boolean[] visitados = new boolean[indsis.length];
        Arrays.fill(visitados, false);
        impresionT("SISBEN_BENEFICIARIOS: Indice Cargado");
        RandomAccessFile raf = new RandomAccessFile(fSisbenBeneficiarios, "r");
        raf.seek(0);
        String[] enc1 = splitCSV("AUX_IES;AUX_NUMEST");
        String[] enc2 = splitCSV(raf.readLine());
        int indic = 0;
        ps.println(stringToCSV(enc1) + stringToCSV(enc2));
        int encontrados = 0;
        for (IES ies:kernel.listaIES) {
          for (int i=0, nei = ies.estudiantes.length;i<nei;i++) {
            String[] dat1 = new String[]{String.valueOf(ies.codigo), String.valueOf(i)},
              dat2 = null;
            if (resi[indic]==null) {
              dat2 = new String[enc2.length];
              Arrays.fill(dat2, "");
            } else {
              int ind = new MyByteSequence(resi[indic]).getInt(0);
              visitados[ind] = true;
              raf.seek(indsis[ind]);
              dat2 = splitCSV(raf.readLine());
              encontrados++;
            }
            ps.println(stringToCSV(dat1) + stringToCSV(dat2));             
            ++indic;
          }
        }
        impresionT("Individuos SPADIES encontrados SISBEN: " + encontrados);
        int noVis = 0;
        String[] dat1v = new String[enc1.length];
        Arrays.fill(dat1v, "");
        for (int i = 0, it = visitados.length;i < it;i++) {
          if (visitados[i] == true) continue;
          raf.seek(indsis[i]);
          String[] dat2 = splitCSV(raf.readLine());
          ps.println(stringToCSV(dat1v) + stringToCSV(dat2));
          ++noVis;
        }
        impresionT("SISBEN no en SPADIES " + noVis);
        /*
        BufferedReader br = new BufferedReader(new FileReader(fBeneT));
        String[] enc1 = splitCSV(br.readLine());
        ps.println(stringToCSV(enc1) + stringToCSV(enc2));
        for (int i=0, it = resi.length;i < it;i++) {
          String[] dat1 = splitCSV(br.readLine()),
            dat2 = null;
          if (resi[i]==null) {
            dat2 = new String[enc2.length];
            Arrays.fill(dat2, "");
          } else {
            int ind = new MyByteSequence(resi[i]).getInt(0);
            visitados[ind] = true;
            raf.seek(indsis[ind]);
            dat2 = splitCSV(raf.readLine());
          }
          ps.println(stringToCSV(dat1) + stringToCSV(dat2));
        }
        br.close();
        String[] dat1v = new String[enc1.length];
        Arrays.fill(dat1v, "");
        for (int i = 0, it = visitados.length;i < it;i++) {
          if (visitados[i] == true) continue;
          raf.seek(indsis[i]);
          String[] dat2 = splitCSV(raf.readLine());
          ps.println(stringToCSV(dat1v) + stringToCSV(dat2));
        }
        */
        raf.close();
      }
      ps.close();
    }

    {//Match con ECAES, Observatorio
      ArchivoCSV arEK = new ArchivoCSV() {
        int conteo = 0;
        public String getTitulo() {return "ECAES COMPLETO";}
        public File getIn() {return fECAES;}
        public File getOut() {return fECAESPro;}
        public String[] getIdLinea(String[] w) {return new String[]{w[1],w[2]};}
        public byte[] getDatoLinea(String[] w) {
          MyByteSequence mbs = new MyByteSequence(4);
          mbs.setInt(0, conteo++);
          return mbs.getBytes();
        }
      };
      ArchivoCSV arobs = new ArchivoCSV() {
        int conteo = 0;
        public String getTitulo() {return "OBSERVATORIO COMPLETO";}
        public File getIn() {return fObs;}
        public File getOut() {return fObsPro;}
        public String[] getIdLinea(String[] w) {return new String[]{w[49]+" "+w[51]+" "+w[52]+" "+w[53],w[0]};}
        public byte[] getDatoLinea(String[] w) {
          MyByteSequence mbs = new MyByteSequence(4);
          mbs.setInt(0, conteo++);
          return mbs.getBytes();
        }
      };
      PreparadorDatos pd = PreparadorDatos.getInstance();
      try {
        pd.prepararArchivoBase(arEK);
        pd.prepararArchivoBase(arobs);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (!fECAESInd.exists()) indexarCSV(fECAES, fECAESInd);
      if (!fObsInd.exists()) indexarCSV(fObs, fObsInd);
      int regSIS_SPA = 0;
      {
        BufferedReader br = new BufferedReader(new FileReader(fSisbenBeneficiariosRBase));
        br.readLine();
        while (br.readLine()!=null) regSIS_SPA++;
        br.close();
      }
      impresionT("Individuos para vista: " + regSIS_SPA);
      byte[][][] INFO_IES = new byte[3][regSIS_SPA][];//Lazye :P
      {
        BufferedReader br = new BufferedReader(new FileReader(fSisbenBeneficiariosRBase));
        br.readLine();
        int i = 0;
        for (String s = br.readLine();s!=null;s = br.readLine()) {
          if (s.trim().length()==0) continue;
          String sL[] = csvToString(s,0,';');
          //INFO_IES[0][i]=codifLetrasServer.getCodigos(sL[3] + sL[4]);
          if (sL[0].length()>0 && sL[1].length()>0) {
            //System.out.println(sL[0] + " " + sL[1]);
            IES ies = kernel.getIES(Integer.parseInt(sL[0]));
            Estudiante_DatosPersonales edp = ies.datosPersonalesEstudiantes==null?null:ies.datosPersonalesEstudiantes[Integer.parseInt(sL[1])];
            INFO_IES[0][i]=codifLetrasServer.getCodigos(new String(edp.apellido)+" "+new String(edp.nombre));
            INFO_IES[1][i]=codifNumeros.getCodigos((edp.documento!=-1)?(""+edp.documento):"");
            INFO_IES[2][i]=codifNumeros.getCodigos((edp.anhoFechaNacimiento!=-1 && edp.mesFechaNacimiento!=-1 && edp.diaFechaNacimiento!=-1)?(intToString(edp.anhoFechaNacimiento%100,2)+intToString(edp.mesFechaNacimiento,2)+intToString(edp.diaFechaNacimiento,2)):"");
          }
          else {
            INFO_IES[0][i]=codifLetrasServer.getCodigos(
                sL[273].trim().length()==0?sL[5] + " " + sL[6]: sL[275]
            );
            INFO_IES[1][i]=codifNumeros.getCodigos(sL[7].trim().length()==0?sL[41]:sL[7]);
            INFO_IES[2][i]= new byte[0];//TODO deberia            
          }
          ++i;
        }
        //System.out.println("Registros: " + i);
        br.close();
      }
      impresionT("ECAES: Realizando match");
      MatcherSPADIES mEK = new MatcherSPADIES(INFO_IES,97.99,true,arEK.getOut(),new MyDataOutputStream(fos));
      byte[][] resEK = mEK.procesar(mEK.total, null);
      impresionT("ECAES: Match completado");
      impresionT("Observatorio: Realizando match");
      MatcherSPADIES mO = new MatcherSPADIES(INFO_IES,97.99,true,arobs.getOut(),new MyDataOutputStream(fos));
      byte[][] resO = mO.procesar(mO.total, null);
      impresionT("Observatorio: Match completado");
      {//Impresion Salida
        int conEK = 0, conObs = 0;
        BufferedReader br = new BufferedReader(new FileReader(fSisbenBeneficiariosRBase));
        PrintStream ps = new PrintStream(new FileOutputStream(fSalidaDEF));
        impresionT("ECAES OBSERVATORIO: Cargando indices");
        int[] indEK = cargarIndiceCSV(fECAESInd);
        int[] indObs = cargarIndiceCSV(fObsInd);
        impresionT("ECAES OBSERVATORIO: Indices cargados");
        String[] enc0 = splitCSV(br.readLine()),
          enc3 = encSPA;
        RandomAccessFile rafEK = new RandomAccessFile(fECAES, "r");
        rafEK.seek(0);
        String[] enc1 = splitCSV(rafEK.readLine());
        RandomAccessFile rafO = new RandomAccessFile(fObs, "r");
        rafO.seek(0);
        String[] enc2 = splitCSV(rafO.readLine());
        String[] dat0v = new String[enc0.length],
          dat1v = new String[enc1.length],
          dat2v = new String[enc2.length],
          dat3v = new String[enc3.length];
        for (String[] datv:new String[][]{dat0v,dat1v,dat2v,dat3v}) Arrays.fill(datv, "");
        ps.println(stringToCSV(enc0) + stringToCSV(enc1) + stringToCSV(enc2) + stringToCSV(enc3));
        int ic = 0;
        for (String s = br.readLine();s != null; s = br.readLine()) {
          String[] dat0 = splitCSV(s),
            dat1 = dat1v, dat2 = dat2v/*, dat3 = dat3v*/;
          if (resEK[ic]!=null) {
            int ind = new MyByteSequence(resEK[ic]).getInt(0);
            rafEK.seek(indEK[ind]);
            dat1 = splitCSV(rafEK.readLine());
            conEK++;
          }
          if (resO[ic]!=null) {
            int ind = new MyByteSequence(resO[ic]).getInt(0);
            rafO.seek(indObs[ind]);
            dat2 = splitCSV(rafO.readLine());
            conObs++;
          }
          if (dat0[0].length()>0 && dat0[1].length()>0) {
            //System.out.println(dat0[0] + "\t" +dat0[1]);
            IES ies = kernel.getIES(Integer.parseInt(dat0[0]));
            Estudiante est = ies.estudiantes[Integer.parseInt(dat0[1])];
            imprimirInfoSpadiesPRE(ps, stringToCSV(dat0) + stringToCSV(dat1) + stringToCSV(dat2), ies, est);
          } else {
            ps.println(stringToCSV(dat0) + stringToCSV(dat1) + stringToCSV(dat2) + stringToCSV(dat3v));
          }
          ic++;
        }
        ps.close();
        br.close();
        System.out.println("Con ECAES: " + conEK);
        System.out.println("Con Observatorio: " + conObs);
      }
    }
    System.exit(0);
    /*
    {
      try {
        kernel.cargarParaServidor(Constantes.carpetaDatos,periodoActualizacion);
      }
      catch (MyException ex) {
        System.out.println(ex.getMessage());
      }
      catch (Throwable th) {
        th.printStackTrace(System.out);
      }
    }
    {//Match con beneficiarios individuos SPADIES
      ArchivoCSV arBenSIS= new ArchivoCSV() {
        int conteo = 0;
        public String getTitulo() {return "BENEFICIARIOS CON SISBEN";}
        public File getIn() {return fSisbenBeneficiarios;}
        public File getOut() {return fSisbenBeneficiariosPro;}
        public String[] getIdLinea(String[] w) {return new String[]{w[244],w[10]};}
        public byte[] getDatoLinea(String[] w) {
          MyByteSequence mbs = new MyByteSequence(4);
          mbs.setInt(0, conteo++);
          return mbs.getBytes();
        }
      };
      PreparadorDatos pd = PreparadorDatos.getInstance();
      try {
        pd.prepararArchivoBase(arBenSIS);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (!fSisbenBeneficiariosInd.exists()) indexarCSV(fSisbenBeneficiarios, fSisbenBeneficiariosInd);
      int numEst = kernel.getCantidadEstudiantes(new Filtro[0]);
      byte [][][] INFO_IES = new byte[3][numEst][];
      {//Llenado INFO_IES
        int ind = 0;
        for (IES ies:kernel.listaIES) {
          int numEstIes = ies.estudiantes.length;
          for (int i=0;i<numEstIes;i++) {
            Estudiante_DatosPersonales edp = ies.datosPersonalesEstudiantes==null?null:ies.datosPersonalesEstudiantes[i];
            INFO_IES[0][ind]=codifLetrasServer.getCodigos(new String(edp.apellido)+" "+new String(edp.nombre));
            INFO_IES[1][ind]=codifNumeros.getCodigos((edp.documento!=-1)?(""+edp.documento):"");
            INFO_IES[2][ind]=codifNumeros.getCodigos((edp.anhoFechaNacimiento!=-1 && edp.mesFechaNacimiento!=-1 && edp.diaFechaNacimiento!=-1)?(intToString(edp.anhoFechaNacimiento%100,2)+intToString(edp.mesFechaNacimiento,2)+intToString(edp.diaFechaNacimiento,2)):"");
            ++ind;
          }
        }
      }
    }
    */
    fos.close();
  }

  private static long nesT = 0;
  private static void imprimirInfoSpadiesPRE(PrintStream ps, String pre, IES ies, Estudiante e) {
    String[] linea = new String[encSPA.length];
    Arrays.fill(linea, "");
    int ipos = 0;
    linea[ipos++] = String.valueOf(ies.codigo);
    int pos = ipos;
    linea[pos++] = String.valueOf(nesT++);
    linea[pos++] = formateoCampoNum(e.getSexo());
    linea[pos++] = formateoCampoNum(e.getEdadAlPresentarElICFES());
    linea[pos++] = formateoCampoNum(e.getNivelEducativoMadre());
    linea[pos++] = formateoCampoNum(e.getIngresoHogar());
    linea[pos++] = formateoCampoNum(e.getViviendaPropia());
    linea[pos++] = formateoCampoNum(e.getNumeroHermanos());
    linea[pos++] = formateoCampoNum(e.getPosicionEntreLosHermanos());
    linea[pos++] = formateoCampoNum(e.getPuntajeICFES());
    linea[pos++] = formateoCampoNum(e.getTrabajabaCuandoPresentoIcfes());
    linea[pos++] = ies.semestres[e.getSemestrePrimiparo()];
    int g = e.getSemestreGrado();
    linea[pos++] = g==-1?"":ies.semestres[g];
    int rf = e.getSemestreRetiroForzoso();
    linea[pos++] = rf==-1?"":ies.semestres[rf];
    {
      Programa prog = e.getIndicePrograma()<0?null:ies.programas[e.getIndicePrograma()];
      int area = prog==null||prog.area==-1?-1:prog.area;
      for (int iA = -1;iA<=9;iA++) {
        if (iA==0) continue;
        linea[pos++] = iA==area?"1":"0";
      }
    }
    {
      byte estado = e.getEstado();
      linea[pos++] = estado==1?"1":"0";
      linea[pos++] = estado==2?"1":"0";
      linea[pos++] = estado==0?"1":"0";
      linea[pos++] = estado==-1?"1":"0";
    }
    long matri=e.getSemestresMatriculadoAlDerecho();
    int segvar = 1;
    double[] reps = e.getRepitencias();
    for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
      int npos = pos;
      linea[npos++] = formateoCampoNum(segvar);
      linea[npos++] = ies.semestres[j];
      linea[npos++] = formateoCampoNum(e.getNumeroMateriasTomadas(j));
      linea[npos++] = formateoCampoNum(e.getNumeroMateriasAprobadas(j));
      linea[npos++] = e.getRecibioApoyoAcademico(j)?"1":"0";
      linea[npos++] = e.getRecibioApoyoFinanciero(j)?"1":"0";
      linea[npos++] = e.getRecibioApoyoOtro(j)?"1":"0";
      {
        byte icetex = e.getTipoApoyoICETEXRecibido(j);
        for (int i=0;i<4;i++) linea[npos++] = i==icetex?"1":"0";  
      }
      linea[npos++] = formateoCampoNum(reps[j]);
      linea[npos++] = formateoCampoNum(e.getRiesgo(j));
      linea[npos++] = formateoCampoNum(e.getRiesgoEstructural(j));
      linea[npos++] = formateoCampoNum(e.getClaseRiesgo(j));
      for (int j2=0; j2<jT; j2++) {
        linea[npos++] = j2==j?"1":"0";
      }
      segvar++;
      ps.println(pre + stringToCSV(linea));
    }
  }

  public static final String formateoCampoNum(int num) {
    return num==-1?"":String.valueOf(num);
  }
  
  public static final String formateoCampoNum(double num) {
    return num==-1.0?"":String.valueOf(num);
  }
  
  private static final String[] encSPA = new String[]{
    "ies",
    "numero_estudiante",
    "sexo",
    "edad_icfes",
    "edu_madre_icfes",
    "ing_hogar_icfes",
    "viv_propia_icfes",
    "num_hermanos_icfes",
    "pos_hermanos_icfes",
    "puntaje_icfes",
    "trabajaba_icfes",
    "prim_sem",
    "grado_per",
    "retiro_per",
    "areaD","area1","area2","area3","area4","area5","area6","area7","area8","area9",
    "e_graduado","e_retirado","e_activo","e_desertor",

    "segvar",
    "periodo",
    "materias_tomadas",
    "materias_aprobadas",
    "apo_aca",
    "apo_fin",
    "apo_otr",
    "ictx_n","ictx_l","ictx_m","ictx_a",
    "trepitencia",
    "riesgo_est",
    "riesgo",
    "riesgo_clase",
    //TODO implementacion LAZY :P :( Eventualmente "fixear" je
    "p1","p2","p3","p4","p5","p6","p7","p8","p9","p10",
    "p11","p12","p13","p14","p15","p16","p17","p18","p19","p20",
  };
  public static void escrituraDatos(PrintStream ps, IES ies, byte[][] ecaes, byte[][] observatorio) {
    //PrintStream ps = new PrintStream("vista_EK_OBS.csv");
    //ps.println(CajaDeHerramientas.stringToCSV(encabezado));
    String[] linea = new String[encSPA.length];
    Arrays.fill(linea, "");
    int ipos = 0;
    linea[ipos++] = String.valueOf(ies.codigo);
    System.out.println("*" + ies.codigo);
    int nes = 0;
    for (Estudiante e:ies.estudiantes) {
      int pos = ipos;
      linea[pos++] = String.valueOf(nes);
      linea[pos++] = formateoCampoNum(e.getSexo());
      linea[pos++] = formateoCampoNum(e.getEdadAlPresentarElICFES());
      linea[pos++] = formateoCampoNum(e.getNivelEducativoMadre());
      linea[pos++] = formateoCampoNum(e.getIngresoHogar());
      linea[pos++] = formateoCampoNum(e.getViviendaPropia());
      linea[pos++] = formateoCampoNum(e.getNumeroHermanos());
      linea[pos++] = formateoCampoNum(e.getPosicionEntreLosHermanos());
      linea[pos++] = formateoCampoNum(e.getPuntajeICFES());
      linea[pos++] = formateoCampoNum(e.getTrabajabaCuandoPresentoIcfes());
      linea[pos++] = ies.semestres[e.getSemestrePrimiparo()];
      int g = e.getSemestreGrado();
      linea[pos++] = g==-1?"":ies.semestres[g];
      int rf = e.getSemestreRetiroForzoso();
      linea[pos++] = rf==-1?"":ies.semestres[rf];
      {
        Programa prog = e.getIndicePrograma()<0?null:ies.programas[e.getIndicePrograma()];
        int area = prog==null||prog.area==-1?-1:prog.area;
        for (int iA = -1;iA<=9;iA++) {
          if (iA==0) continue;
          linea[pos++] = iA==area?"1":"0";
        }
      }
      {
        byte estado = e.getEstado();
        linea[pos++] = estado==1?"1":"0";
        linea[pos++] = estado==2?"1":"0";
        linea[pos++] = estado==0?"1":"0";
        linea[pos++] = estado==-1?"1":"0";
      }
      long matri=e.getSemestresMatriculadoAlDerecho();
      int segvar = 1;
      double[] reps = e.getRepitencias();
      for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
        int npos = pos;
        linea[npos++] = formateoCampoNum(segvar);
        linea[npos++] = ies.semestres[j];
        linea[npos++] = formateoCampoNum(e.getNumeroMateriasTomadas(j));
        linea[npos++] = formateoCampoNum(e.getNumeroMateriasAprobadas(j));
        linea[npos++] = e.getRecibioApoyoAcademico(j)?"1":"0";
        linea[npos++] = e.getRecibioApoyoFinanciero(j)?"1":"0";
        linea[npos++] = e.getRecibioApoyoOtro(j)?"1":"0";
        {
          byte icetex = e.getTipoApoyoICETEXRecibido(j);
          for (int i=0;i<4;i++) linea[npos++] = i==icetex?"1":"0";  
        }
        linea[npos++] = formateoCampoNum(reps[j]);
        linea[npos++] = formateoCampoNum(e.getRiesgo(j));
        linea[npos++] = formateoCampoNum(e.getRiesgoEstructural(j));
        linea[npos++] = formateoCampoNum(e.getClaseRiesgo(j));
        for (int j2=0; j2<jT; j2++) {
          linea[npos++] = j2==j?"1":"0";
        }
        segvar++;
        ps.println(CajaDeHerramientas.stringToCSV(linea));
      }
      nes++;
    }
    System.out.println("Fin escritura");
  }
  
  /**
   * Asume que hay encabezado, el primer registro es el 0
   * @param archivoCSV
   * @param fOut
   * @throws IOException
   */
  public static void indexarCSV(File archivoCSV, File fOut) throws IOException {
    FileReader fr = new FileReader(archivoCSV);
    List<Integer> posic = new LinkedList<Integer>();
    boolean proxf = false;
    int numc = 0;
    for (int car = fr.read();car!=-1;car = fr.read()) {
      if (car == 10 || car == 13) proxf = true;
      else if (proxf) {
        posic.add(numc);
        proxf = false;
      }
      ++numc;
    }
    fr.close();
    {
      MyDataOutputStream mdo = new MyDataOutputStream(new FileOutputStream(fOut));
      mdo.writeInt(posic.size());
      for (Integer val: posic) mdo.writeInt(val);
      mdo.close();
    }
  }
  
  public static int[] cargarIndiceCSV(File fIn) throws IOException {
    MyDataInputStream mdis = new MyDataInputStream(new FileInputStream(fIn));
    int tam = mdis.readInt();
    int[] res = new int[tam];
    for (int i = 0;i < tam;i++) res[i] = mdis.readInt();
    return res;
  }
  
  public static String[] splitCSV(String s) {
    List<String> p=new LinkedList<String>();
    for (int i=0,t=s.length(); i<t; ) {
      if (s.charAt(i)==';') {
        p.add("");
        i++;
      }
      else if (s.charAt(i)!='"') {
        int j=s.indexOf(';',i+1);
        if (j==-1) j=t;
        p.add(s.substring(i,j));
        i=j+1;
      }
      else {
        int j=s.indexOf('"',i+1);
        while (j!=-1 && j<t-1 && s.charAt(j+1)=='"') j=s.indexOf('"',j+2);
        p.add(s.substring(i+1,j).replaceAll("\"\"","\""));
        j=s.indexOf(';',j+1);
        if (j==-1) j=t;
        i=j+1;
      }
      if (i==t) p.add("");
    }
    return p.toArray(new String[0]);
  }
  
  public static void impresionT(String msg) {
    System.out.println(formatoT(System.currentTimeMillis()-tInic) + " " + msg);
  }
  public static String formatoT(long tO) {
    int t = (int) (tO/1000);
    int s = t%60,
      m = (t/60)%60,
      h = t/(60*60);
    String stS = String.valueOf(s),
      stM = String.valueOf(m),
      stH = String.valueOf(h);
    if (stS.length()==1) stS = "0"+stS;
    if (stM.length()==1) stM = "0"+stM;
    return stH + "h " + stM + "m " + stS + "s ";
  }  
}