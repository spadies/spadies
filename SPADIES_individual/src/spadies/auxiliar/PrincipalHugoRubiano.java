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

public class PrincipalHugoRubiano {
  public static void main(String[] args) throws Exception {
    long tInic = System.currentTimeMillis();
    File salIntermedio = new File("HR_SPADIES.csv");
    {
      KernelSPADIES kernel = KernelSPADIES.getInstance();
      Constantes.cargarArchivoFiltroIES();
      kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE, true);
      Collection<String> encabezado = Arrays.asList(
          "ies_codigo","ies_nombre","ies_origen","ies_caracter","ies_departamento",
          "nombre","documento",
          "ind_cohorte","ind_ultimo_matriculado","ind_estado","ind_nacimiento",
          "prog_cod","prog_nombre","prog_area","prog_nivel","prog_nucleo","prog_metodologia"
      );
      PrintStream ps = new PrintStream(salIntermedio);
      for (String s:encabezado) ps.print(s+";");
      ps.println();
      for (IES ies: kernel.listaIES) {
        Collection<String> lineaIES = Arrays.asList(
          ""+ies.codigo,
          new String(ies.nombre),
          ""+ies.origen,
          ""+ies.caracter,
          ""+ies.departamento
        );
        for (int i=0;i<ies.estudiantes.length;i++) {
          Collection<String> linea = new LinkedList<String>(lineaIES);
          Estudiante e = ies.estudiantes[i];
          Estudiante_DatosPersonales edp = ies.datosPersonalesEstudiantes[i];
          linea.addAll(Arrays.asList(
            new String(edp.apellido) + " " + new String(edp.nombre),
            String.valueOf(edp.documento)
          ));
          linea.addAll(Arrays.asList(
            ies.semestres[e.getSemestrePrimiparo()],
            ies.semestres[e.getUltimoSemestreMatriculado()],
            ""+e.getEstado(),
            (edp.diaFechaNacimiento==-1?"":edp.diaFechaNacimiento)+"/"+(edp.mesFechaNacimiento==-1?"":edp.mesFechaNacimiento)+"/"+(edp.anhoFechaNacimiento==-1?"":edp.anhoFechaNacimiento)
          ));
          Programa p = e.getIndicePrograma()==-1?null:ies.programas[e.getIndicePrograma()];
          linea.addAll(Arrays.asList(
            p==null?"":new String(p.codigoSNIES),
            p==null?"":new String(p.nombre),
            p==null?"":(""+p.area),
            p==null?"":(""+p.nivel),
            p==null?"":(""+p.nucleo),
            p==null?"":(""+p.metodologia)
          ));
          for (String s:linea) ps.print(s+";");
          ps.println();
        }
      }
      ps.close();
      kernel = null;
      System.gc();
    }

    FileOutputStream fos = new FileOutputStream(new File("bla"));
    final File carSisben = new File("datosNoSPA"),
      fICFESTo = new File(carSisben, "_ICFES_20070827.csv"),
      fICFESToPro = new File(carSisben, "icfes.procesado"),
      fICFESToInd = new File(carSisben, "icfes.ind");
    final File fSalida = new File("hr_spadies_icfes.csv");
    {//Match SISBEN-Encuesta
      int regs = registrosEnArchivo(salIntermedio);
      System.out.println("Registros SPADIES: " + regs);
      byte[][][] INFO_IES = new byte[3][regs][];
      {
        BufferedReader br = new BufferedReader(new FileReader(salIntermedio));
        br.readLine();//Encabezado
        int i = 0;
        for (String s = br.readLine();s!=null;s = br.readLine()) {
          String sL[] = csvToString(s,0,';');
          INFO_IES[0][i]=codifLetrasServer.getCodigos(sL[5]);
          INFO_IES[1][i]=codifNumeros.getCodigos(sL[6]);
          INFO_IES[2][i]= new byte[0];
          ++i;
        }
        br.close();
      }
      PrintStream ps = new PrintStream(fSalida);
      int encontrados = 0;
      {
        System.out.println("ICFES: Realizando Match");
        MatcherSPADIES m = new MatcherSPADIES(INFO_IES,97.99,true,fICFESToPro,new MyDataOutputStream(fos));
        byte[][] res = m.procesar(m.total, null);
        System.out.println("ICFES: Match Completado");
        System.out.println("ICFES: Cargando Indice");
        long/*int*/[] indsis = cargarIndiceCSV(fICFESToInd);
        boolean[] visitados = new boolean[indsis.length];
        Arrays.fill(visitados, false);
        System.out.println("ICFES: Indice Cargado");
        RandomAccessFile raf = new RandomAccessFile(fICFESTo, "r");
        raf.seek(0);
        String[] enc2 = splitCSV(raf.readLine());
        BufferedReader br = new BufferedReader(new FileReader(salIntermedio));
        String[] enc1 = splitCSV(br.readLine());
        ps.println(stringToCSV(enc1) + stringToCSV(enc2));
        for (int i=0, it = res.length;i < it;i++) {
          String[] dat1 = splitCSV(br.readLine()),
            dat2 = null;
          if (res[i]==null) {
            dat2 = new String[enc2.length];
            Arrays.fill(dat2, "");
          } else {
            int ind = new MyByteSequence(res[i]).getInt(0);
            visitados[ind] = true;
            raf.seek(indsis[ind]);
            dat2 = splitCSV(raf.readLine());
            encontrados++;
          }
          ps.println(stringToCSV(dat1) + stringToCSV(dat2));
        }
        System.out.println("Individuos SPADIES encontrados ICFES: " + encontrados);
        /*{
          String[] dat1v = new String[enc1.length];
          Arrays.fill(dat1v, "");
          int noVis = 0;
          for (int i = 0, it = visitados.length;i < it;i++) {
            if (visitados[i] == true) continue;
            raf.seek(indsis[i]);
            String[] dat2 = splitCSV(raf.readLine());
            ps.println(stringToCSV(dat1v) + stringToCSV(dat2));
            ++noVis;
          }
          System.out.println("SISBEN no en TABACO " + noVis);
        }*/
        br.close();
        raf.close();
      }
      ps.close();
    }
    fos.close();
    System.out.println("Proceso completado en: " + (System.currentTimeMillis()-tInic)/1000);
  }

  private static long nesT = 0;
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
  /*
  public static int[] cargarIndiceCSV(File fIn) throws IOException {
    MyDataInputStream mdis = new MyDataInputStream(new FileInputStream(fIn));
    int tam = mdis.readInt();
    int[] res = new int[tam];
    for (int i = 0;i < tam;i++) res[i] = mdis.readInt();
    return res;
  }
  */
  public static long[] cargarIndiceCSV(File fIn) throws IOException {
    MyDataInputStream mdis = new MyDataInputStream(new FileInputStream(fIn));
    int tam = mdis.readInt();
    long[] res = new long[tam];
    for (int i = 0;i < tam;i++) res[i] = mdis.readLong();
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