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

public class PrincipalMatchSPA_IcfesModelo {
  private static String[] defI = new String[76], defE = new String[1253], defO = new String[64];
  static {
    Arrays.fill(defI, "");Arrays.fill(defO, "");Arrays.fill(defE, "");
  }
  private static final KernelSPADIES kernel = KernelSPADIES.getInstance();
  private static final DateFormat df=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  private static final long periodoActualizacion=1000L*60*30;  // 30 minutos
  private static final File carProductos = new File("./sal");
  public static void main(String[] args) throws Exception {
    FileOutputStream fos = new FileOutputStream(new File(carProductos, "bla"));
    //File carSisben = new File("C:/basesPuras");
    File carSisben = new File("datosNoSPA");
    //final File fSisbenTo = new File(carSisben, "sisben.csv");
    //final File fICFESTo = new File(carSisben, "_ICFES_20070827.csv");
    final File fICFESTo = new File(carSisben, "icfes_remp_sisbenymedia.csv");
    //final File fACCESTo = new File(carSisben, "2007_11_06_acces_dep.csv");
    //final File fSisbenTo = new File("C:/basesPuras/sisben_red.csv");
    //final File fSisbenToPro = new File(carSisben, "sisben.procesado");
    //final File fSisbenToInd = new File(carSisben, "sisben.ind");
    final File fICFESToPro = new File(carSisben, "icfes.procesado");
    final File fICFESToInd = new File(carSisben, "icfes.ind");
    //final File fACCESToPro = new File(carSisben, "acces.procesado");
    //final File fACCESToInd = new File(carSisben, "acces.ind");
    
    //final File fArchivoIntermedio = new File("intermedio.csv");
    final File fSalida = new File("salidaModelo.csv");
    /*{
      MyDataInputStream mds = new MyDataInputStream(new FileInputStream(fSisbenToInd));
      int tam = mds.readInt();
      int min = Integer.MAX_VALUE;
      int max = Integer.MIN_VALUE;
      for (int i=0;i<tam;i++) {
        int val = mds.readInt();
        if (val>max) max = val;
        if (val<min) min = val;
        if (val<0) System.out.println(val);
      }
      System.out.println(min);
      System.out.println(max);
      System.exit(1);
    }*/
    /*ArchivoCSV arsis = new ArchivoCSV() {
      int conteo = 0;
      public String getTitulo() {return "SISBEN";}
      public File getIn() {return fSisbenTo;}
      public File getOut() {return fSisbenToPro;}
      //public String[] getIdLinea(String[] w) {return new String[]{w[55]+" "+w[56]+" "+w[57]+" "+w[58],w[64]};}
      public String[] getIdLinea(String[] w) {return new String[]{w[0]+" "+w[1]+" "+w[2]+" "+w[3],w[4]};}
      public byte[] getDatoLinea(String[] w) {
        MyByteSequence mbs = new MyByteSequence(4);
        mbs.setInt(0, conteo++);
        return mbs.getBytes();
      }
    };
    ArchivoCSV aracc = new ArchivoCSV() {
      int conteo = 0;
      public String getTitulo() {return "ACCES";}
      public File getIn() {return fACCESTo;}
      public File getOut() {return fACCESToPro;}
      //public String[] getIdLinea(String[] w) {return new String[]{w[55]+" "+w[56]+" "+w[57]+" "+w[58],w[64]};}
      public String[] getIdLinea(String[] w) {return new String[]{w[3]+" "+w[2],w[1]};}
      public byte[] getDatoLinea(String[] w) {
        MyByteSequence mbs = new MyByteSequence(4);
        mbs.setInt(0, conteo++);
        return mbs.getBytes();
      }
    };*/
    ArchivoCSV aricf = new ArchivoCSV() {
      int conteo = 0;
      public String getTitulo() {return "ICFES";}
      public File getIn() {return fICFESTo;}
      public File getOut() {return fICFESToPro;}
      //public String[] getIdLinea(String[] w) {return new String[]{w[55]+" "+w[56]+" "+w[57]+" "+w[58],w[64]};}
      public String[] getIdLinea(String[] w) {return new String[]{w[5],w[4]};}
      public byte[] getDatoLinea(String[] w) {
        MyByteSequence mbs = new MyByteSequence(4);
        mbs.setInt(0, conteo++);
        return mbs.getBytes();
      }
    };
    {
      PreparadorDatos pd = PreparadorDatos.getInstance();
      try {
        //pd.prepararArchivoBase(arsis);
        //pd.prepararArchivoBase(aracc);
        pd.prepararArchivoBase(aricf);
      } catch (Exception e) {
        e.printStackTrace();
      }
      //if (!fSisbenToInd.exists()) indexarCSV(fSisbenTo, fSisbenToInd);
      if (!fICFESToInd.exists()) indexarCSV(fICFESTo, fICFESToInd);
      //if (!fACCESToInd.exists()) indexarCSV(fACCESTo, fACCESToInd);
    }
    try {
      kernel.cargarParaServidor(Constantes.carpetaDatos,periodoActualizacion, true);
    }
    catch (MyException ex) {
      System.out.println(ex.getMessage());
    }
    catch (Throwable th) {
      th.printStackTrace(System.out);
    }
    System.out.println("Cargados SPA.");
    int numInd = kernel.getCantidadEstudiantes(new Filtro[0]);
    System.out.println("IES: " + kernel.listaIES.length + " Individuos: " + numInd);
    {//Match con ICFES, SISBEN
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
      System.out.println("ICFES: Realizando match");
      MatcherSPADIES mICF = new MatcherSPADIES(INFO_IES,97.99,true,aricf.getOut(),new MyDataOutputStream(fos));
      byte[][] resEK = mICF.procesar(mICF.total, null);
      System.out.println("ICFES: Match completado");
      //System.out.println("SISBEN: Realizando match");
      //MatcherSPADIES mSIS = new MatcherSPADIES(INFO_IES,97.99,true,arsis.getOut(),new MyDataOutputStream(fos));
      //byte[][] resSIS = mSIS.procesar(mSIS.total, null);
      //System.out.println("SISBEN: Match completado");
      {//Impresion Salida
        int conICF = 0/*, conSIS = 0*/;
        //BufferedReader br = new BufferedReader(new FileReader(fArchivoIntermedio));
        PrintStream ps = new PrintStream(new FileOutputStream(fSalida));
        System.out.println("ECAES OBSERVATORIO: Cargando indices");
        long[] indICF = cargarIndiceCSV(fICFESToInd);
        //long[] indSIS = cargarIndiceCSV(fSisbenToInd);
        System.out.println("ICFES SISBEN: Indices cargados");
        String[] enc0 = encSPA/*,
          enc3 = encSPA*/;
        RandomAccessFile rafICF = new RandomAccessFile(fICFESTo, "r");
        rafICF.seek(0);
        String[] enc1 = splitCSV(rafICF.readLine());
        //RandomAccessFile rafSIS = new RandomAccessFile(fSisbenTo, "r");
        //rafSIS.seek(0);
        //String[] enc2 = splitCSV(rafSIS.readLine());
        String[] dat0v = new String[enc0.length],
          dat1v = new String[enc1.length]/*,
          dat2v = new String[enc2.length],
          dat3v = new String[enc3.length]*/;
        for (String[] datv:new String[][]{dat0v,dat1v/*,dat2v,dat3v*/}) Arrays.fill(datv, "");
        ps.println(stringToCSV(enc1) + stringToCSV(enc0) /*+ stringToCSV(enc2) + stringToCSV(enc3)*/);
        
        int ic = 0;
        for (IES ies:kernel.listaIES) for (int i=0, nei = ies.estudiantes.length;i<nei;i++) {
          Estudiante est = ies.estudiantes[i];
          String[] /*dat0 = splitCSV(s),*/
            dat1 = dat1v/*, dat2 = dat2v*//*, dat3 = dat3v*/;
          if (resEK[ic]!=null) {
            int ind = new MyByteSequence(resEK[ic]).getInt(0);
            rafICF.seek(indICF[ind]);
            dat1 = splitCSV(rafICF.readLine());
            conICF++;
          }
          imprimirInfoSpadiesPRE(ps, /*stringToCSV(dat0) +*/ stringToCSV(dat1)/* + stringToCSV(dat2)*/, ies, est);
          ic++;
        }
        ps.close();
        //br.close();
        System.out.println("Con ICFES: " + conICF);
        //System.out.println("Con SISBEN: " + conSIS);
      }
    }
    //XXXXXXXXXXXX
    fos.close();
    System.exit(0);
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
    "nivel",
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
    "p11","p12","p13","p14","p15","p16","p17","p18","p19","p20","p21","p22","p23","p24"
  };
  
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
      linea[pos++] = formateoCampoNum(prog==null||prog.area==-1?-1:prog.area);
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
  
  /*public static void indexarCSV(File archivoCSV, File fOut) throws IOException {
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
  }*/
  
  /*public static long[] cargarIndiceCSV(File fIn) throws IOException {
    MyDataInputStream mdis = new MyDataInputStream(new FileInputStream(fIn));
    int tam = mdis.readInt();
    long[] res = new long[tam];
    for (int i = 0;i < tam;i++) res[i] = mdis.readLong();
    return res;
  }*/
  
  /*public static int[] cargarIndiceCSV(File fIn) throws IOException {
    MyDataInputStream mdis = new MyDataInputStream(new FileInputStream(fIn));
    int tam = mdis.readInt();
    int[] res = new int[tam];
    for (int i = 0;i < tam;i++) res[i] = mdis.readInt();
    return res;
  }*/
  
  public static void indexarCSV(File archivoCSV, File fOut) throws IOException {
    FileReader fr = new FileReader(archivoCSV);
    List<Long> posic = new LinkedList<Long>();
    boolean proxf = false;
    long numc = 0;
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
      for (Long val: posic) mdo.writeLong(val);
      mdo.close();
    }
  }
  
  public static long[] cargarIndiceCSV(File fIn) throws IOException {
    MyDataInputStream mdis = new MyDataInputStream(new FileInputStream(fIn));
    int tam = mdis.readInt();
    long[] res = new long[tam];
    for (int i = 0;i < tam;i++) res[i] = mdis.readLong();
    return res;
  }
}