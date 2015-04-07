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

public class PrincipalVistaAnaRicaurte {
  private static boolean clean = false;
  private static long tInic;
  public static void main(String[] args) throws Exception {
    {
      if (args.length>0 && args[0].equals("CLEAN")) clean = true;
    }
    tInic = System.currentTimeMillis();
    FileOutputStream fos = new FileOutputStream(new File("bla"));
    
    File fBaseOrigen = new File("../datFOD_BEN/graduado_base.csv");
    
    File carBaseVFOD = new File("../datFOD_BEN");
    /*   
    final File fSisben = new File(carBaseVFOD,"sisben.csv"),
      fSisbenPro = new File(carBaseVFOD,"pros/sisben.procesado"),
      fSisbenInd = new File(carBaseVFOD,"sisben.csv.indcsv");
    */
    final File fECAES = new File(carBaseVFOD,"ecaes_sint2004a2006_nodetalle.csv"),
      fECAESPro = new File(carBaseVFOD,"pros/ecaes_sint2004a2006_nd.procesado"),
      fECAESInd = new File(carBaseVFOD,"pros/ecaes_sint2004a2006_nd.indcsv");
    final File fObs = new File(carBaseVFOD,"graduado_base.csv")/*,
      fObsPro = new File(carBaseVFOD,"pros/graduado_base.procesado"),
      fObsInd = new File(carBaseVFOD,"pros/graduado_base.csv.indcsv")*/;
    final File fIcf = new File(carBaseVFOD,"_ICFES.csv"),
      fIcfPro = new File(carBaseVFOD,"pros/_ICFES.procesado"),
      fIcfInd = new File(carBaseVFOD,"pros/_ICFES.indcsv");
    /*
    final File fSisbenBeneficiariosObsGrad = new File("datFOD_BEN/datBEN_SIS_OBS_GRA.out.csv"),
      fSisbenBeneficiariosObsGradPro = new File("datFOD_BEN/pros/datBEN_SIS_OBS_GRA.out.procesado"),
      fSisbenBeneficiariosObsGradInd = new File("datFOD_BEN/datBEN_SIS_OBS_GRA.out.csv.indcsv");
      */
    
    File fSalidaDEF = new File("vistaAnaMariaRicaurte.csv");
    /*if (clean) {
      System.out.println("Limpiando Archivos");
      fSisbenPro.delete();
      fSisbenInd.delete();
      fBenefTotal.delete();
      fBenefVariosInd.delete();
      fSisbenBeneficiarios.delete();
      fSisbenBeneficiariosPro.delete();
      fSisbenBeneficiariosInd.delete();
      fSisbenBeneficiariosRBase.delete();
    }*/
    {//Match con ECAES, Observatorio
      ArchivoCSV arEK = new ArchivoCSV() {
        int conteo = 0;
        public String getTitulo() {return "ECAES INCOMPLETO";}
        public File getIn() {return fECAES;}
        public File getOut() {return fECAESPro;}
        public String[] getIdLinea(String[] w) {return new String[]{w[1],w[2]};}
        public byte[] getDatoLinea(String[] w) {
          MyByteSequence mbs = new MyByteSequence(4);
          mbs.setInt(0, conteo++);
          return mbs.getBytes();
        }
      };
      ArchivoCSV aricf = new ArchivoCSV() {
        int conteo = 0;
        public String getTitulo() {return "ICFES";}
        public File getIn() {return fIcf;}
        public File getOut() {return fIcfPro;}
        public String[] getIdLinea(String[] w) {return new String[]{w[5],w[4]};}
        public byte[] getDatoLinea(String[] w) {
          MyByteSequence mbs = new MyByteSequence(4);
          mbs.setInt(0, conteo++);
          return mbs.getBytes();
        }
      };
      /*
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
      */
      PreparadorDatos pd = PreparadorDatos.getInstance();
      try {
        pd.prepararArchivoBase(arEK);
        pd.prepararArchivoBase(aricf);
        //pd.prepararArchivoBase(arobs);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (!fECAESInd.exists()) indexarCSV(fECAES, fECAESInd);
      if (!fIcfInd.exists()) indexarCSV(fIcf, fIcfInd);
      //if (!fObsInd.exists()) indexarCSV(fObs, fObsInd);
      int regOrigen = 0;
      {
        BufferedReader br = new BufferedReader(new FileReader(fBaseOrigen));
        br.readLine(); //Lectura encabezado
        while (br.readLine()!=null) regOrigen++;
        br.close();
      }
      impresionT("Individuos para vista: " + regOrigen);
      byte[][][] INFO_IES = new byte[3][regOrigen][];
      {
        BufferedReader br = new BufferedReader(new FileReader(fBaseOrigen));
        br.readLine();
        int i = 0;
        for (String s = br.readLine();s!=null;s = br.readLine()) {
          if (s.trim().length()==0) continue;
          String sL[] = csvToString(s,0,';');
          INFO_IES[0][i]=codifLetrasServer.getCodigos(sL[49]+" "+sL[51]+" "+sL[52]+" "+sL[53]);
          INFO_IES[1][i]=codifNumeros.getCodigos(sL[0]);
          INFO_IES[2][i]=codifNumeros.getCodigos("");
          ++i;
        }
        br.close();
      }
      impresionT("ECAES: Realizando match");
      MatcherSPADIES mEK = new MatcherSPADIES(INFO_IES,97.99,true,arEK.getOut(),new MyDataOutputStream(fos));
      byte[][] resEK = mEK.procesar(mEK.total, null);
      impresionT("ECAES: Match completado");
      impresionT("ICFES: Realizando match");
      MatcherSPADIES mI = new MatcherSPADIES(INFO_IES,97.99,true,aricf.getOut(),new MyDataOutputStream(fos));
      byte[][] resI = mI.procesar(mI.total, null);
      impresionT("ICFES: Match completado");
      {//Impresion Salida
        int conEK = 0, conIcf = 0;
        BufferedReader br = new BufferedReader(new FileReader(fBaseOrigen));
        PrintStream ps = new PrintStream(new FileOutputStream(fSalidaDEF));
        impresionT("ECAES ICFES: Cargando indices");
        int[] indEK = cargarIndiceCSV(fECAESInd);
        int[] indIcf = cargarIndiceCSV(fIcfInd);
        impresionT("ECAES ICFES: Indices cargados");
        String[] enc0 = splitCSV(br.readLine());
        RandomAccessFile rafEK = new RandomAccessFile(fECAES, "r");
        rafEK.seek(0);
        String[] enc1 = splitCSV(rafEK.readLine());
        RandomAccessFile rafI = new RandomAccessFile(fIcf, "r");
        rafI.seek(0);
        String[] enc2 = splitCSV(rafI.readLine());
        String[] dat0v = new String[enc0.length],
          dat1v = new String[enc1.length],
          dat2v = new String[enc2.length];
        for (String[] datv:new String[][]{dat0v,dat1v,dat2v}) Arrays.fill(datv, "");
        ps.println(stringToCSV(enc0) + stringToCSV(enc1) + stringToCSV(enc2));
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
          if (resI[ic]!=null) {
            int ind = new MyByteSequence(resI[ic]).getInt(0);
            rafI.seek(indIcf[ind]);
            dat2 = splitCSV(rafI.readLine());
            conIcf++;
          }
          ps.println(stringToCSV(dat0) + stringToCSV(dat1) + stringToCSV(dat2));
          ic++;
        }
        ps.close();
        br.close();
        impresionT("Escritura completada");
        impresionT("Con ECAES: " + conEK);
        impresionT("Con ICFES: " + conIcf);
      }
    }
    fos.close();
    System.exit(0);
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
  public static int registrosEnArchivo(File f) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(f));
    br.readLine();//Lectura encabezado
    int conteo = 0;
    while(br.readLine()!=null) conteo++;
    br.close();
    return conteo;
  }
  
  public static String stringMasLargo(String ... strings) {
    int max = Integer.MIN_VALUE;
    for (String s:strings) if (s.length()>max) max = s.length();
    for (String s:strings) if (s.length()==max) return s;
    return null;
  }
}