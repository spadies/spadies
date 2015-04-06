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

public class PrincipalMatch2 {
  private static String[] defI = new String[76], defE = new String[1253], defO = new String[64];
  static {
    Arrays.fill(defI, "");Arrays.fill(defO, "");Arrays.fill(defE, "");
  }
  private static final KernelSPADIES kernel = KernelSPADIES.getInstance();
  private static final DateFormat df=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  private static final int puerto=Constantes.puertoServidorConsultas;
  private static final long periodoActualizacion=1000L*60*30;  // 30 minutos
  private static final File carProductos = new File("../matchGrandeProductos");
  private static final File carAnt = new File("../SPADIES 2.2_MATCH");
  public static void main(String[] args) throws IOException {
    FileOutputStream fos = new FileOutputStream(new File(carProductos, "bla"));
    long tm=System.currentTimeMillis();
    ArchivoCSVICFES archivoCSVIcfes = new ArchivoCSVICFES();
    try {
      kernel.cargarParaServidor(Constantes.carpetaDatos,periodoActualizacion, true);
    }
    catch (MyException ex) {
      System.out.println(ex.getMessage());
    }
    catch (Throwable th) {
      th.printStackTrace(System.out);
    }
    {//Preparacion
      PreparadorDatos pd = PreparadorDatos.getInstance();
      try {
        System.out.println("Preparando ICFES");
        pd.prepararArchivoBase(archivoCSVIcfes);
        indexarCSV(archivoCSVIcfes.getIn(), new File(archivoCSVIcfes.getIn().getParentFile(), archivoCSVIcfes.getIn().getName() + ".inds"));
      } catch (Exception e1) {
        e1.printStackTrace();
      }
      
      for (IES ies: kernel.listaIES) {
        System.out.println("preparandoParticularesIES " + ies.codigo);
        int cod = ies.codigo;
        while (cod>10000) cod/=10;
        ArchivoCSVECAES are = new ArchivoCSVECAES(String.valueOf(cod));
        ArchivoCSVObservatorio aro = new ArchivoCSVObservatorio(String.valueOf(cod));
        try {
          if (aro.getIn().exists()) {
            pd.prepararArchivoBase(aro);
            indexarCSV(aro.getIn(), new File(aro.getIn().getParentFile(), aro.getIn().getName() + ".inds"));
          }
          if (are.getIn().exists()) {
            pd.prepararArchivoBase(are);
            indexarCSV(are.getIn(), new File(are.getIn().getParentFile(), are.getIn().getName() + ".inds"));
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    //System.exit(0);
    {//Match
      ArchivoCSV ari = new ArchivoCSVICFES();
      //PrintStream ps = new PrintStream("vista_EK_OBS.csv");
      PrintStream psl = new PrintStream("vista_EK_OBS2.log");
      //ps.println(CajaDeHerramientas.stringToCSV(encabezado));
      for (IES ies: kernel.listaIES) {
        int codigoRef = ies.codigo;
        while (codigoRef>9999)codigoRef/=10;
        MyDataOutputStream mdo = new MyDataOutputStream(new FileOutputStream(new File(carProductos,"sal/match"+ies.codigo+".dat")));
        ArchivoCSV are = new ArchivoCSVECAES(String.valueOf(codigoRef)), aro = new ArchivoCSVObservatorio(String.valueOf(codigoRef));
        System.out.println((tm - System.currentTimeMillis())/1000);
        System.out.println("Procesando " + ies.codigo);
        int ne= ies.estudiantes.length;
        byte[][][] INFO_IES = new byte[3][ne][];
        int ie = 0;
        for (int i=0;i<ne;i++) {  //Preparacion datos IES
          Estudiante_DatosPersonales edp = ies.datosPersonalesEstudiantes==null?null:ies.datosPersonalesEstudiantes[i];
          INFO_IES[0][i]=codifLetrasServer.getCodigos(new String(edp.apellido)+" "+new String(edp.nombre));
          INFO_IES[1][i]=codifNumeros.getCodigos((edp.documento!=-1)?(""+edp.documento):"");
          INFO_IES[2][i]=codifNumeros.getCodigos((edp.anhoFechaNacimiento!=-1 && edp.mesFechaNacimiento!=-1 && edp.diaFechaNacimiento!=-1)?(intToString(edp.anhoFechaNacimiento%100,2)+intToString(edp.mesFechaNacimiento,2)+intToString(edp.diaFechaNacimiento,2)):"");
          ++ie;
        }
        try {
          MatcherSPADIES mi = !ari.getOut().exists()?null:new MatcherSPADIES(INFO_IES,97.99,true,ari.getOut(),new MyDataOutputStream(fos));
          MatcherSPADIES me = !are.getOut().exists()?null:new MatcherSPADIES(INFO_IES,97.99,true,are.getOut(),new MyDataOutputStream(fos));
          MatcherSPADIES mo = !aro.getOut().exists()?null:new MatcherSPADIES(INFO_IES,97.99,true,aro.getOut(),new MyDataOutputStream(fos));
          byte[][] resi = mo==null?new byte[ies.estudiantes.length][]:mi.procesar(mi.total, null);
          byte[][] rese = me==null?new byte[ies.estudiantes.length][]:me.procesar(me.total, null);
          byte[][] reso = mo==null?new byte[ies.estudiantes.length][]:mo.procesar(mo.total, null);
          int nnulosi=0, nnulose=0, nnuloso=0;
          for (int i=0,n=ies.estudiantes.length;i<n;i++) {
            if (resi[i]!=null) nnulosi++;
            if (rese[i]!=null) nnulose++;
            if (reso[i]!=null) nnuloso++;
          }
          psl.println(CajaDeHerramientas.stringToCSV(""+ies.codigo, ""+nnulosi, ""+nnulose, ""+nnuloso, ""+ies.estudiantes.length));
          mdo.writeInt(ies.estudiantes.length);
          for (byte[][] resx: new byte[][][]{resi,rese,reso}) {//Por tabla match
            for (byte[] resxin:resx) {//Por individuo
              if (resxin!=null) { 
                MyByteSequence mbs = new MyByteSequence(resxin);
                mdo.writeInt(mbs.getInt(0));
              }
              else {
                mdo.writeInt(-1);
              }
            }
          }
          //escrituraDatos(ps, ies, rese, reso);
        } catch (Exception e) {
          e.printStackTrace();
        }
        mdo.close();
        System.out.println((tm - System.currentTimeMillis())/60000);
        System.out.println("Completo " + ies.codigo);
      }
      //ps.close();
      psl.close();
    }
    //System.exit(0);
    {//Escritura
      PrintStream ps = new PrintStream(new FileOutputStream(new File(carProductos,"grasal.csv")));
      ps.println(stringToCSV(encabezado) + stringToCSV(encI) + stringToCSV(encE) + stringToCSV(encO));
      RandomAccessFile rafI = new RandomAccessFile(new File("../TEMPORAL_ICFES/proc/_ICFES+.csv"), "r");
      int[] indI = null;
      {
        MyDataInputStream mdsI = new MyDataInputStream(new FileInputStream(new File("../TEMPORAL_ICFES/proc/_ICFES+.csv.inds")));
        int tam = mdsI.readInt();
        indI = new int[tam];
        for (int i=0;i<tam;i++) indI[i] = mdsI.readInt();
        mdsI.close();
      }
      for (IES ies:kernel.listaIES) {
        //if (ies.codigo!=1813) continue;
        System.out.println(ies.codigo);
        int codRef = ies.codigo;
        while (codRef>10000) codRef/=10;
        String sCodRef = String.valueOf(codRef);
        File fE = new ArchivoCSVECAES(sCodRef).getIn();
        File fO = new ArchivoCSVObservatorio(sCodRef).getIn();
        RandomAccessFile rafE = fE.exists()?new RandomAccessFile(fE, "r"):null;
        RandomAccessFile rafO = fO.exists()?new RandomAccessFile(fO, "r"):null;
        int[] indE = null, indO = null;
        {//Cargar indices ECAES e Observatorio para la IES
          if (fE.exists()) {
            MyDataInputStream mdsE = new MyDataInputStream(new FileInputStream(new File(fE.getParent(), fE.getName()+".inds")));
            int tam = mdsE.readInt();
            indE = new int[tam];
            for (int i=0;i<tam;i++) indE[i] = mdsE.readInt(); 
            mdsE.close();            
          }
          if (fO.exists()) {
            MyDataInputStream mdsO = new MyDataInputStream(new FileInputStream(new File(fO.getParent(), fO.getName()+".inds")));
            int tam = mdsO.readInt();
            indO = new int[tam];
            for (int i=0;i<tam;i++) indO[i] = mdsO.readInt();
            mdsO.close();            
          }
        }
        int[] tabI = null, tabE = null, tabO = null;
        {//CARGAR tabals relacion
          MyDataInputStream mdsR = new MyDataInputStream(new FileInputStream(new File(carProductos,"sal/match" + ies.codigo + ".dat")));
          int tam = mdsR.readInt();
          tabI = new int[tam];
          tabO = new int[tam];
          tabE = new int[tam];
          for (int[] aLle : new int[][]{tabI, tabE, tabO}) {
            for (int i=0;i<tam;i++) aLle[i] = mdsR.readInt();
          }
          mdsR.close();
        }
        System.out.println(tabI.length + "\t" + tabO.length + "\t" + tabE.length);
        for (int i=0, ne=ies.estudiantes.length;i<ne;i++) {
          int refI = tabI[i],
            refO = tabO[i],
            refE = tabE[i];
          int refIt = refI==-1?-1:indI[refI],
            refOt = refO==-1?-1:indO[refO],
            refEt = refE==-1?-1:indE[refE];
          String[] strsI = defI,
            strsE = defE,
            strsO = defO;
          if (refIt!=-1) {
            strsI = new String[strsI.length];
            rafI.seek(refIt);
            System.arraycopy(splitCSV(rafI.readLine()), 0, strsI, 0, strsI.length);
          }
          if (refEt!=-1) {
            strsE = new String[strsE.length];
            rafE.seek(refEt);
            System.arraycopy(splitCSV(rafE.readLine()), 0, strsE, 0, strsE.length);
          }
          if (refOt!=-1) {
            strsO = new String[strsO.length];
            rafO.seek(refOt);
            System.arraycopy(splitCSV(rafO.readLine()), 0, strsO, 0, strsO.length);
          }
          escrituraDatosEstudiante(ps, i, ies, ies.estudiantes[i], ies.datosPersonalesEstudiantes[i], strsI, strsE, strsO);
          if (i%500 == 0) System.out.print(i + "\t" + ies.estudiantes.length + "\r");
        }
        System.out.println();
        if (rafE!=null) rafE.close();
        if (rafO!=null) rafO.close();
      }
      ps.close();
      rafI.close();
    }
    //!!!!!!!!!!!!!!!!!!!!!!!!!!

    fos.close();
    System.exit(0);
  }
  
  public static class ArchivoCSVObservatorio implements ArchivoCSV {
    private int contador = 0;
    private String ies;
    public ArchivoCSVObservatorio(String ies) {this.ies = ies;}
    public String getTitulo() {return "OBSERVATORIO "+ies;}
    public File getIn() {return new File(carAnt, "split/obs/graobs" + ies + ".csv");}
    public File getOut() {return new File(carProductos, "pr/graobs" + ies + ".procesado");}
    public String[] getIdLinea(String[] w) {return (w.length==0)? new String[]{"",""}:new String[]{w[49]+" "+w[51]+" "+w[52]+" "+w[53],w[0]};}
    public byte[] getDatoLinea(String[] w) {
      MyByteSequence mbs = new MyByteSequence(4);
      mbs.setInt(0, contador++);
      return mbs.getBytes();
    }
  }
  public static class ArchivoCSVECAES implements ArchivoCSV {
    private int contador = 0;
    private String ies;
    public ArchivoCSVECAES(String ies) {this.ies = ies;}
    public String getTitulo() {return "ECAES "+ies;}
    public File getIn() {return new File(carAnt , "split/ek2/ek" + ies + ".csv");}
    public File getOut() {return new File(carProductos, "pr/ek" + ies + ".procesado");}
    public String[] getIdLinea(String[] w) {return (w.length==0)? new String[]{"",""}:new String[]{w[1],w[2]};}
    public byte[] getDatoLinea(String[] w) {
      MyByteSequence mbs = new MyByteSequence(4);
      mbs.setInt(0, contador++);
      return mbs.getBytes();      
    }
  }
  public static class ArchivoCSVICFES implements ArchivoCSV {
    private int contador = 0;
    public String getTitulo() {return "ICFES directo";}
    public File getIn() {return new File("../TEMPORAL_ICFES/proc/_ICFES+.csv");}
    public File getOut() {return new File(carProductos, "_icfes_directo.procesado");}
    public String[] getIdLinea(String[] w) {return (w.length==0)? new String[]{"",""}:new String[]{w[5],w[4]};}
    public byte[] getDatoLinea(String[] w) {
      MyByteSequence mbs = new MyByteSequence(4);
      mbs.setInt(0, contador++);
      return mbs.getBytes();      
    }
  }
  
  public static final String formateoCampoNum(int num) {
    return num==-1?"":String.valueOf(num);
  }
  
  public static final String formateoCampoNum(double num) {
    return num==-1.0?"":String.valueOf(num);
  }
  private static String[] encabezado = new String[]{
      "ies",
      "numero_estudiante",
      "nombre",
      "documento",
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
      //"areaD","area1","area2","area3","area4","area5","area6","area7","area8","area9",
      "area",
      "nivel",
      "nucleo",
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
  },
  encI = splitCSV("icfes_snp;icfes_semestre;icfes_ano;icfes_estudiante_documento_tipo;icfes_estudiante_documento_numero;icfes_estudiante_nombre;icfes_estudiante_invidente;icfes_estudiante_sordo;icfes_estudiante_motriz;icfes_estudiante_genero;icfes_estudiante_valida;icfes_estudiante_colegio_codigo;icfes_municipio;icfes_idioma;icfes_interdisciplinario;icfes_estudiante_edad;icfes_estudiante_colegio_jornada;icfes_estudiante_colegio_calendario;icfes_estudiante_colegio_caracter;icfes_estudiante_colegio_idioma;icfes_estudiante_razon_presentacion;icfes_estudiante_carrera_deseada_codigo;icfes_estudiante_carrera_deseada_razon;icfes_estudiante_institucion_deseada;icfes_estudiante_institucion_razon;icfes_estudiante_etnia;icfes_familia_personas;icfes_vivienda_propia;icfes_vivienda_deuda;icfes_familia_aportantes;icfes_familia_ingreso;icfes_padre_lee;icfes_madre_lee;icfes_padre_nivel1;icfes_padre_nivel2;icfes_padre_nivel3;icfes_padre_nivel4;icfes_madre_nivel1;icfes_madre_nivel2;icfes_madre_nivel3;icfes_madre_nivel4;icfes_padre_ocupacion_f;icfes_padre_ocupacion_p;icfes_padre_ocupacion_a;icfes_padre_ocupacion_cp;icfes_padre_ocupacion_d;icfes_padre_ocupacion_ci;icfes_madre_ocupacion_f;icfes_madre_ocupacion_p;icfes_madre_ocupacion_a;icfes_madre_ocupacion_cp;icfes_madre_ocupacion_d;icfes_madre_ocupacion_i;icfes_familia_hermanos;icfes_estudiante_pension;icfes_familia_hermanos_estudios_superiores;icfes_estudiante_hijo_numero;icfes_estudiante_sostenimiento;icfes_estudiante_trabaja;icfes_estudiante_estudiara;icfes_estudiante_trabajara;icfes_estudiante_estudiara_trabajara;icfes_estudiante_estrato;icfes_estudiante_ingreso;icfes_estudiante_ingreso_valor;icfes_puntaje_biologia;icfes_puntaje_matematicas;icfes_puntaje_filosofia;icfes_puntaje_fisica;icfes_puntaje_historia;icfes_puntaje_quimica;icfes_puntaje_lenguaje;icfes_puntaje_geografia;icfes_puntaje_idioma1;icfes_puntaje_total;icfes_puntaje_clasificacion"),
  encE = splitCSV("cita_snee;eval_nombre;eval_documento;eval_semestre;exam_id;eval_estado;inst_id;resultados_evaluado_dipo_codigom;jorn_id;eval_evaluado;eval_periodo;prue_id;capr_evaluado;capr_puntaje;capr_calificacionproyecto;capr_porcentajeproyecto;capr_puntajetotal;capr_nrc;inpe_etnia;inpe_estadocivil;inpe_estrato;zona_id;informacionpersonal_dipo;infa_personasacargo;infa_nivelpadre;infa_nivelmadre;infa_ocupacionpadre;infa_ocupacionmadre;inac_anoegreso;inac_anoterminobachiller;inac_colegiotermino;inac_estudiobachillerato;inac_estudiauniversidad;inac_titulobachillerato;inac_semestreexamenestado;inac_anoexamenestado;inac_semestrecursa;inac_idiomalee;inac_idiomahabla;inac_nivelpostgrado;prac_id;ecaes_pc3;ecaes_pc4;ecaes_pc5;ecaes_pc21;ecaes_pc22;ecaes_pc43;ecaes_pc44;ecaes_pc45;ecaes_pc46;ecaes_pc48;ecaes_pc49;ecaes_pc50;ecaes_pc51;ecaes_pc52;ecaes_pc53;ecaes_pc54;ecaes_pc55;ecaes_pc56;ecaes_pc59;ecaes_pc60;ecaes_pc61;ecaes_pc62;ecaes_pc63;ecaes_pc64;ecaes_pc65;ecaes_pc66;ecaes_pc68;ecaes_pc69;ecaes_pc70;ecaes_pc71;ecaes_pc72;ecaes_pc75;ecaes_pc76;ecaes_pc77;ecaes_pc78;ecaes_pc79;ecaes_pc81;ecaes_pc82;ecaes_pc83;ecaes_pc84;ecaes_pc86;ecaes_pc87;ecaes_pc88;ecaes_pc89;ecaes_pc91;ecaes_pc92;ecaes_pc93;ecaes_pc94;ecaes_pc95;ecaes_pc97;ecaes_pc100;ecaes_pc101;ecaes_pc102;ecaes_pc103;ecaes_pc104;ecaes_pc105;ecaes_pc106;ecaes_pc107;ecaes_pc109;ecaes_pc110;ecaes_pc111;ecaes_pc112;ecaes_pc113;ecaes_pc114;ecaes_pc115;ecaes_pc116;ecaes_pc117;ecaes_pc118;ecaes_pc119;ecaes_pc120;ecaes_pc121;ecaes_pc122;ecaes_pc123;ecaes_pc125;ecaes_pc126;ecaes_pc127;ecaes_pc130;ecaes_pc131;ecaes_pc132;ecaes_pc133;ecaes_pc134;ecaes_pc140;ecaes_pc144;ecaes_pc145;ecaes_pc146;ecaes_pc147;ecaes_pc150;ecaes_pc151;ecaes_pc152;ecaes_pc153;ecaes_pc154;ecaes_pc155;ecaes_pc156;ecaes_pc158;ecaes_pc159;ecaes_pc160;ecaes_pc161;ecaes_pc162;ecaes_pc163;ecaes_pc165;ecaes_pc167;ecaes_pc168;ecaes_pc169;ecaes_pc170;ecaes_pc171;ecaes_pc173;ecaes_pc174;ecaes_pc175;ecaes_pc187;ecaes_pc188;ecaes_pc190;ecaes_pc191;ecaes_pc192;ecaes_pc207;ecaes_pc208;ecaes_pc209;ecaes_pc213;ecaes_pc214;ecaes_pc215;ecaes_pc216;ecaes_pc231;ecaes_pc232;ecaes_pc233;ecaes_pc234;ecaes_pc235;ecaes_pc236;ecaes_pc237;ecaes_pc238;ecaes_pc239;ecaes_pc240;ecaes_pc241;ecaes_pc242;ecaes_pc243;ecaes_pc244;ecaes_pc245;ecaes_pc246;ecaes_pc247;ecaes_pc248;ecaes_pc249;ecaes_pc250;ecaes_pc251;ecaes_pc252;ecaes_pc253;ecaes_pc254;ecaes_pc255;ecaes_pc256;ecaes_pc257;ecaes_pc258;ecaes_pc259;ecaes_pc260;ecaes_pc261;ecaes_pc262;ecaes_pc263;ecaes_pc264;ecaes_pc265;ecaes_pc266;ecaes_pc267;ecaes_pc268;ecaes_pc269;ecaes_pc270;ecaes_pc271;ecaes_pc272;ecaes_pc273;ecaes_pc274;ecaes_pc275;ecaes_pc276;ecaes_pc277;ecaes_pc278;ecaes_pc279;ecaes_pc280;ecaes_pc281;ecaes_pc282;ecaes_pc283;ecaes_pc284;ecaes_pc285;ecaes_pc286;ecaes_pc287;ecaes_pc288;ecaes_pc289;ecaes_pc290;ecaes_pc291;ecaes_pc292;ecaes_pc293;ecaes_pc294;ecaes_pc295;ecaes_pc296;ecaes_pc297;ecaes_pc298;ecaes_pc299;ecaes_pc300;ecaes_pc301;ecaes_pc302;ecaes_pc303;ecaes_pc304;ecaes_pc305;ecaes_pc306;ecaes_pc307;ecaes_pc308;ecaes_pc309;ecaes_pc310;ecaes_pc311;ecaes_pc312;ecaes_pc313;ecaes_pc314;ecaes_pc315;ecaes_pc316;ecaes_pc317;ecaes_pc318;ecaes_pc319;ecaes_pc320;ecaes_pc321;ecaes_pc322;ecaes_pc323;ecaes_pc324;ecaes_pc325;ecaes_pc326;ecaes_pc327;ecaes_pc328;ecaes_pc329;ecaes_pc330;ecaes_pc331;ecaes_pc332;ecaes_pc333;ecaes_pc334;ecaes_pc335;ecaes_pc336;ecaes_pc339;ecaes_pc340;ecaes_pc341;ecaes_pc342;ecaes_pc343;ecaes_pc344;ecaes_pc345;ecaes_pc346;ecaes_pc348;ecaes_pc349;ecaes_pc351;ecaes_pc352;ecaes_pc353;ecaes_pc354;ecaes_pc355;ecaes_pc356;ecaes_pc357;ecaes_pc358;ecaes_pc359;ecaes_pc360;ecaes_pc361;ecaes_pc362;ecaes_pc363;ecaes_pc364;ecaes_pc365;ecaes_pc366;ecaes_pc367;ecaes_pc368;ecaes_pc369;ecaes_pc370;ecaes_pc371;ecaes_pc372;ecaes_pc373;ecaes_pc374;ecaes_pc375;ecaes_pc376;ecaes_pc377;ecaes_pc378;ecaes_pc379;ecaes_pc380;ecaes_pc381;ecaes_pc382;ecaes_pc383;ecaes_pc384;ecaes_pc385;ecaes_pc386;ecaes_pc387;ecaes_pc388;ecaes_pc389;ecaes_pc390;ecaes_pc391;ecaes_pc392;ecaes_pc393;ecaes_pc394;ecaes_pc395;ecaes_pc396;ecaes_pc397;ecaes_pc398;ecaes_pc399;ecaes_pc400;ecaes_pc401;ecaes_pc402;ecaes_pc403;ecaes_pc404;ecaes_pc405;ecaes_pc406;ecaes_pc407;ecaes_pc408;ecaes_pc409;ecaes_pc410;ecaes_pc411;ecaes_pc412;ecaes_pc413;ecaes_pc414;ecaes_pc415;ecaes_pc416;ecaes_pc417;ecaes_pc418;ecaes_pc419;ecaes_pc420;ecaes_pc421;ecaes_pc422;ecaes_pc423;ecaes_pc426;ecaes_pc427;ecaes_pc428;ecaes_pc429;ecaes_pc430;ecaes_pc431;ecaes_pc432;ecaes_pc433;ecaes_pc434;ecaes_pc438;ecaes_pc442;ecaes_pc443;ecaes_pc444;ecaes_pc445;ecaes_pc446;ecaes_pc447;ecaes_pc448;ecaes_pc449;ecaes_pc450;ecaes_pc451;ecaes_pc452;ecaes_pc453;ecaes_pc454;ecaes_pc455;ecaes_pc456;ecaes_pc457;ecaes_pc458;ecaes_pc459;ecaes_pc460;ecaes_pc461;ecaes_pc462;ecaes_pc463;ecaes_pc464;ecaes_pc465;ecaes_pc466;ecaes_pc467;ecaes_pc468;ecaes_pc469;ecaes_pc470;ecaes_pc471;ecaes_pc472;ecaes_pc473;ecaes_pc474;ecaes_pc475;ecaes_pc476;ecaes_pc477;ecaes_pc478;ecaes_pc479;ecaes_pc480;ecaes_pc481;ecaes_pc482;ecaes_pc483;ecaes_pc484;ecaes_pc485;ecaes_pc486;ecaes_pc487;ecaes_pc488;ecaes_pc489;ecaes_pc490;ecaes_pc491;ecaes_pc492;ecaes_pc493;ecaes_pc494;ecaes_pc495;ecaes_pc496;ecaes_pc497;ecaes_pc499;ecaes_pc500;ecaes_pc502;ecaes_pc503;ecaes_pc505;ecaes_pc506;ecaes_pc507;ecaes_pc508;ecaes_pc509;ecaes_pc510;ecaes_pc511;ecaes_pc512;ecaes_pc513;ecaes_pc514;ecaes_pc515;ecaes_pc516;ecaes_pc517;ecaes_pc518;ecaes_pc519;ecaes_pc520;ecaes_pc521;ecaes_pc522;ecaes_pc523;ecaes_pc524;ecaes_pc525;ecaes_pc526;ecaes_pc527;ecaes_pc528;ecaes_pc529;ecaes_pc531;ecaes_pc532;ecaes_pc533;ecaes_pc534;ecaes_pc535;ecaes_pc536;ecaes_pc537;ecaes_pc538;ecaes_pc539;ecaes_pc540;ecaes_pc541;ecaes_pc542;ecaes_pc548;ecaes_pc549;ecaes_pc550;ecaes_pc551;ecaes_pc552;ecaes_pc553;ecaes_pc554;ecaes_pc555;ecaes_pc557;ecaes_pc558;ecaes_pc559;ecaes_pc560;ecaes_pc562;ecaes_pc563;ecaes_pc564;ecaes_pc565;ecaes_pc567;ecaes_pc568;ecaes_pc569;ecaes_pc570;ecaes_pc571;ecaes_pc572;ecaes_pc573;ecaes_pc574;ecaes_pc575;ecaes_pc576;ecaes_pc577;ecaes_pc578;ecaes_pc579;ecaes_pc580;ecaes_pc582;ecaes_pc583;ecaes_pc584;ecaes_pc585;ecaes_pc586;ecaes_pc587;ecaes_pc588;ecaes_pc589;ecaes_pc590;ecaes_pc591;ecaes_pc592;ecaes_pc593;ecaes_pc594;ecaes_pc595;ecaes_pc596;ecaes_pc597;ecaes_pc598;ecaes_pc599;ecaes_pc600;ecaes_pc601;ecaes_pc602;ecaes_pc603;ecaes_pc604;ecaes_pc605;ecaes_pc606;ecaes_pc607;ecaes_pc611;ecaes_pc614;ecaes_pc622;ecaes_pc627;ecaes_pc630;ecaes_pc631;ecaes_pc633;ecaes_pc634;ecaes_pc635;ecaes_pc636;ecaes_pc637;ecaes_pc638;ecaes_pc639;ecaes_pc640;ecaes_pc647;ecaes_pc648;ecaes_pc649;ecaes_pc650;ecaes_pc651;ecaes_pc652;ecaes_pc653;ecaes_pc654;ecaes_pc655;ecaes_pc656;ecaes_pc657;ecaes_pc658;ecaes_pc659;ecaes_pc660;ecaes_pc661;ecaes_pc662;ecaes_pc663;ecaes_pc664;ecaes_pc665;ecaes_pc666;ecaes_pc667;ecaes_pc668;ecaes_pc669;ecaes_pc670;ecaes_pc671;ecaes_pc672;ecaes_pc673;ecaes_pc674;ecaes_pc675;ecaes_pc676;ecaes_pc677;ecaes_pc678;ecaes_pc679;ecaes_pc680;ecaes_pc681;ecaes_pc682;ecaes_pc683;ecaes_pc684;ecaes_pc685;ecaes_pc686;ecaes_pc687;ecaes_pc688;ecaes_pc689;ecaes_pc690;ecaes_pc691;ecaes_pc708;ecaes_pc709;ecaes_pc710;ecaes_pc729;ecaes_pc730;ecaes_pc732;ecaes_pc733;ecaes_pc748;ecaes_pc749;ecaes_pc768;ecaes_pc769;ecaes_pc770;ecaes_pc771;ecaes_pc772;ecaes_pc773;ecaes_pc774;ecaes_pc775;ecaes_pc776;ecaes_pc777;ecaes_pc778;ecaes_pc779;ecaes_pc780;ecaes_pc781;ecaes_pc782;ecaes_pc783;ecaes_pc784;ecaes_pc785;ecaes_pc786;ecaes_pc787;ecaes_pc788;ecaes_pc789;ecaes_pc790;ecaes_pc791;ecaes_pc792;ecaes_pc793;ecaes_pc794;ecaes_pc795;ecaes_pc796;ecaes_pc797;ecaes_pc798;ecaes_pc799;ecaes_pc800;ecaes_pc801;ecaes_pc802;ecaes_pc803;ecaes_pc804;ecaes_pc805;ecaes_pc806;ecaes_pc807;ecaes_pc808;ecaes_pc809;ecaes_pc810;ecaes_pc811;ecaes_pc812;ecaes_pc813;ecaes_pc814;ecaes_pc815;ecaes_pc816;ecaes_pc817;ecaes_pc818;ecaes_pc819;ecaes_pc820;ecaes_pc821;ecaes_pc822;ecaes_pc823;ecaes_pc826;ecaes_pc827;ecaes_pc828;ecaes_pc829;ecaes_pc830;ecaes_pc831;ecaes_pc832;ecaes_pc833;ecaes_pc834;ecaes_pc835;ecaes_pc836;ecaes_pc838;ecaes_pc839;ecaes_pc841;ecaes_pc842;ecaes_pc844;ecaes_pc845;ecaes_pc846;ecaes_pc847;ecaes_pc848;ecaes_pc849;ecaes_pc852;ecaes_pc853;ecaes_pc855;ecaes_pc856;ecaes_pc857;ecaes_pc858;ecaes_pc859;ecaes_pc860;ecaes_pc861;ecaes_pc862;ecaes_pc863;ecaes_pc864;ecaes_pc865;ecaes_pc866;ecaes_pc867;ecaes_pc868;ecaes_pc869;ecaes_pc870;ecaes_pc871;ecaes_pc872;ecaes_pc873;ecaes_pc874;ecaes_pc875;ecaes_pc876;ecaes_pc877;ecaes_pc878;ecaes_pc879;ecaes_pc880;ecaes_pc881;ecaes_pc882;ecaes_pc883;ecaes_pc884;ecaes_pc885;ecaes_pc886;ecaes_pc887;ecaes_pc888;ecaes_pc889;ecaes_pc890;ecaes_pc891;ecaes_pc892;ecaes_pc893;ecaes_pc894;ecaes_pc896;ecaes_pc897;ecaes_pc899;ecaes_pc900;ecaes_pc901;ecaes_pc903;ecaes_pc904;ecaes_pc906;ecaes_pc908;ecaes_pc909;ecaes_pc910;ecaes_pc911;ecaes_pc912;ecaes_pc913;ecaes_pc914;ecaes_pc915;ecaes_pc916;ecaes_pc917;ecaes_pc918;ecaes_pc919;ecaes_pc920;ecaes_pc921;ecaes_pc922;ecaes_pc923;ecaes_pc924;ecaes_pc925;ecaes_pc927;ecaes_pc928;ecaes_pc930;ecaes_pc931;ecaes_pc932;ecaes_pc933;ecaes_pc934;ecaes_pc935;ecaes_pc936;ecaes_pc937;ecaes_pc938;ecaes_pc939;ecaes_pc940;ecaes_pc941;ecaes_pc942;ecaes_pc943;ecaes_pc944;ecaes_pc945;ecaes_pc946;ecaes_pc947;ecaes_pc948;ecaes_pc949;ecaes_pc950;ecaes_pc951;ecaes_pc952;ecaes_pc953;ecaes_pc954;ecaes_pc955;ecaes_pc956;ecaes_pc957;ecaes_pc958;ecaes_pc959;ecaes_pc960;ecaes_pc961;ecaes_pc962;ecaes_pc963;ecaes_pc964;ecaes_pc965;ecaes_pc966;ecaes_pc967;ecaes_pc968;ecaes_pc969;ecaes_pc970;ecaes_pc972;ecaes_pc973;ecaes_pc974;ecaes_pc975;ecaes_pc976;ecaes_pc977;ecaes_pc978;ecaes_pc979;ecaes_pc980;ecaes_pc981;ecaes_pc982;ecaes_pc983;ecaes_pc984;ecaes_pc985;ecaes_pc986;ecaes_pc987;ecaes_pc988;ecaes_pc989;ecaes_pc990;ecaes_pc991;ecaes_pc992;ecaes_pc993;ecaes_pc994;ecaes_pc995;ecaes_pc996;ecaes_pc997;ecaes_pc998;ecaes_pc999;ecaes_pc1000;ecaes_pc1001;ecaes_pc1002;ecaes_pc1003;ecaes_pc1004;ecaes_pc1005;ecaes_pc1006;ecaes_pc1007;ecaes_pc1009;ecaes_pc1010;ecaes_pc1011;ecaes_pc1012;ecaes_pc1013;ecaes_pc1014;ecaes_pc1015;ecaes_pc1016;ecaes_pc1017;ecaes_pc1018;ecaes_pc1019;ecaes_pc1020;ecaes_pc1021;ecaes_pc1022;ecaes_pc1023;ecaes_pc1024;ecaes_pc1025;ecaes_pc1026;ecaes_pc1027;ecaes_pc1028;ecaes_pc1029;ecaes_pc1030;ecaes_pc1031;ecaes_pc1033;ecaes_pc1034;ecaes_pc1035;ecaes_pc1036;ecaes_pc1037;ecaes_pc1038;ecaes_pc1039;ecaes_pc1040;ecaes_pc1041;ecaes_pc1042;ecaes_pc1043;ecaes_pc1044;ecaes_pc1045;ecaes_pc1046;ecaes_pc1048;ecaes_pc1050;ecaes_pc1052;ecaes_pc1053;ecaes_pc1054;ecaes_pc1055;ecaes_pc1056;ecaes_pc1057;ecaes_pc1058;ecaes_pc1059;ecaes_pc1060;ecaes_pc1061;ecaes_pc1062;ecaes_pc1063;ecaes_pc1064;ecaes_pc1065;ecaes_pc1066;ecaes_pc1067;ecaes_pc1068;ecaes_pc1069;ecaes_pc1070;ecaes_pc1071;ecaes_pc1072;ecaes_pc1073;ecaes_pc1074;ecaes_pc1075;ecaes_pc1076;ecaes_pc1077;ecaes_pc1078;ecaes_pc1079;ecaes_pc1080;ecaes_pc1081;ecaes_pc1082;ecaes_pc1083;ecaes_pc1084;ecaes_pc1085;ecaes_pc1086;ecaes_pc1087;ecaes_pc1088;ecaes_pc1089;ecaes_pc1090;ecaes_pc1091;ecaes_pc1092;ecaes_pc1093;ecaes_pc1094;ecaes_pc1095;ecaes_pc1096;ecaes_pc1097;ecaes_pc1098;ecaes_pc1099;ecaes_pc1100;ecaes_pc1101;ecaes_pc1102;ecaes_pc1103;ecaes_pc1104;ecaes_pc1105;ecaes_pc1106;ecaes_pc1107;ecaes_pc1108;ecaes_pc1109;ecaes_pc1110;ecaes_pc1111;ecaes_pc1112;ecaes_pc1113;ecaes_pc1114;ecaes_pc1115;ecaes_pc1116;ecaes_pc1117;ecaes_pc1118;ecaes_pc1119;ecaes_pc1120;ecaes_pc1121;ecaes_pc1122;ecaes_pc1123;ecaes_pc1124;ecaes_pc1125;ecaes_pc1126;ecaes_pc1127;ecaes_pc1128;ecaes_pc1129;ecaes_pc1130;ecaes_pc1131;ecaes_pc1132;ecaes_pc1133;ecaes_pc1134;ecaes_pc1135;ecaes_pc1136;ecaes_pc1137;ecaes_pc1138;ecaes_pc1139;ecaes_pc1140;ecaes_pc1141;ecaes_pc1142;ecaes_pc1143;ecaes_pc1144;ecaes_pc1145;ecaes_pc1146;ecaes_pc1147;ecaes_pc1149;ecaes_pc1150;ecaes_pc1151;ecaes_pc1152;ecaes_pc1153;ecaes_pc1154;ecaes_pc1155;ecaes_pc1156;ecaes_pc1157;ecaes_pc1158;ecaes_pc1159;ecaes_pc1160;ecaes_pc1161;ecaes_pc1162;ecaes_pc1163;ecaes_pc1164;ecaes_pc1165;ecaes_pc1166;ecaes_pc1167;ecaes_pc1168;ecaes_pc1169;ecaes_pc1170;ecaes_pc1171;ecaes_pc1172;ecaes_pc1173;ecaes_pc1174;ecaes_pc1175;ecaes_pc1176;ecaes_pc1177;ecaes_pc1178;ecaes_pc1179;ecaes_pc1180;ecaes_pc1181;ecaes_pc1182;ecaes_pc1183;ecaes_pc1184;ecaes_pc1185;ecaes_pc1186;ecaes_pc1187;ecaes_pc1188;ecaes_pc1189;ecaes_pc1190;ecaes_pc1191;ecaes_pc1192;ecaes_pc1193;ecaes_pc1194;ecaes_pc1195;ecaes_pc1196;ecaes_pc1197;ecaes_pc1198;ecaes_pc1199;ecaes_pc1200;ecaes_pc1201;ecaes_pc1202;ecaes_pc1203;ecaes_pc1204;ecaes_pc1205;ecaes_pc1206;ecaes_pc1207;ecaes_pc1209;ecaes_pc1210;ecaes_pc1211;ecaes_pc1212;ecaes_pc1214;ecaes_pc1228;ecaes_pc1229;ecaes_pc1230;ecaes_pc1231;ecaes_pc1232;ecaes_pc1233;ecaes_pc1234;ecaes_pc1235;ecaes_pc1236;ecaes_pc1237;ecaes_pc1238;ecaes_pc1239;ecaes_pc1240;ecaes_pc1241;ecaes_pc1242;ecaes_pc1243;ecaes_pc1244;ecaes_pc1245;ecaes_pc1246;ecaes_pc1247;ecaes_pc1248;ecaes_pc1249;ecaes_pc1250;ecaes_pc1251;ecaes_pc1252;ecaes_pc1253;ecaes_pc1254;ecaes_pc1255;ecaes_pc1256;ecaes_pc1257;ecaes_pc1258;ecaes_pc1259;ecaes_pc1260;ecaes_pc1261;ecaes_pc1262;ecaes_pc1263;ecaes_pc1264;ecaes_pc1265;ecaes_pc1266;ecaes_pc1268;ecaes_pc1269;ecaes_pc1271;ecaes_pc1272;ecaes_pc1273;ecaes_pc1274;ecaes_pc1275;ecaes_pc1276;ecaes_pc1277;ecaes_pc1278;ecaes_pc1279;ecaes_pc1280;ecaes_pc1281;ecaes_pc1282;ecaes_pc1283;ecaes_pc1284;ecaes_pc1285;ecaes_pc1286;ecaes_pc1287;ecaes_pc1288;ecaes_pc1289;ecaes_pc1290;ecaes_pc1291;ecaes_pc1292;ecaes_pc1293;ecaes_pc1294;ecaes_pc1295;ecaes_pc1296;ecaes_pc1297;ecaes_pc1298;ecaes_pc1299;ecaes_pc1300;ecaes_pc1301;ecaes_pc1302;ecaes_pc1303;ecaes_pc1304;ecaes_pc1305;ecaes_pc1306;ecaes_pc1307;ecaes_pc1308;ecaes_pc1309;ecaes_pc1310;ecaes_pc1311;ecaes_pc1312;ecaes_pc1313;ecaes_pc1314;ecaes_pc1315;ecaes_pc1316;ecaes_pc1317;ecaes_pc1318;ecaes_pc1319;ecaes_pc1320;ecaes_pc1321;ecaes_pc1322;ecaes_pc1323;ecaes_pc1324;ecaes_pc1325;ecaes_pc1326;ecaes_pc1327;ecaes_pc1328;ecaes_pc1329;ecaes_pc1330;ecaes_pc1331;ecaes_pc1332;ecaes_pc1335;ecaes_pc1336;ecaes_pc1337;ecaes_pc1338;ecaes_pc1339;ecaes_pc1340;ecaes_pc1341;ecaes_pc1342;ecaes_pc1343;ecaes_pc1344;ecaes_pc1345;ecaes_pc1346;ecaes_pc1347;ecaes_pc1348;ecaes_pc1349;ecaes_pc1350;ecaes_pc1351;ecaes_pc1352;ecaes_pc1353;ecaes_pc1354;ecaes_pc1355;ecaes_pc1356;ecaes_pc1357;ecaes_pc1358;ecaes_pc1359;ecaes_pc1360;ecaes_pc1361;ecaes_pc1362;ecaes_pc1363;ecaes_pc1364;ecaes_pc1365;ecaes_pc1366;ecaes_pc1367;ecaes_pc1368;ecaes_pc1369;ecaes_pc1370;ecaes_pc1371;ecaes_pc1372;ecaes_pc1373;ecaes_pc1374;ecaes_pc1375;ecaes_pc1376;ecaes_pc1377;ecaes_pc1378;ecaes_pc1379;ecaes_pc1380;ecaes_pc1381;ecaes_pc1382;ecaes_pc1383;ecaes_pc1384;ecaes_pc1385;ecaes_pc1386;ecaes_pc1387;ecaes_pc1388;ecaes_pc1389;ecaes_pc1390;ecaes_pc1391;ecaes_pc1392;ecaes_pc1393;ecaes_pc1394;ecaes_pc1395;ecaes_pc1396;ecaes_pc1397;ecaes_pc1398;ecaes_pc1399;ecaes_pc1400;ecaes_pc1401;ecaes_pc1402;ecaes_pc1403;ecaes_pc1404;ecaes_pc1405;ecaes_pc1406;ecaes_pc1407;ecaes_pc1408;ecaes_pc1409;ecaes_pc1410;ecaes_pc1411;ecaes_pc1412;ecaes_pc1413;ecaes_pc1414;ecaes_pc1415;ecaes_pc1416;ecaes_pc1417;ecaes_pc1418;ecaes_pc1419;ecaes_pc1420;ecaes_pc1421;ecaes_pc1422;ecaes_pc1423;ecaes_pc1424;ecaes_pc1425;ecaes_pc1426;ecaes_pc1427;ecaes_pc1428;ecaes_pc1429;ecaes_pc1430;ecaes_pc1431;ecaes_pc1432;ecaes_pc1433;ecaes_pc1434;ecaes_pc1435;ecaes_pc1436;ecaes_pc1437;ecaes_pc1438;ecaes_pc1439;ecaes_pc1440;ecaes_pc1441;ecaes_pc1442;ecaes_pc1443;ecaes_pc1444;ecaes_pc1445;ecaes_pc1446;ecaes_pc1447;ecaes_pc1448;ecaes_pc1449;ecaes_pc1450;ecaes_pc1451;ecaes_pc1452;ecaes_pc1453;ecaes_pc1454;ecaes_pc1455;ecaes_pc1456;ecaes_pc1457;ecaes_pc1458;ecaes_pc1459;ecaes_pc1460;ecaes_pc1461;ecaes_pc1462;ecaes_pc1463;ecaes_pc1464;ecaes_pc1465;ecaes_pc1466;ecaes_pc1467;ecaes_pc1468;ecaes_pc1469;ecaes_pc1470;ecaes_pc1471;ecaes_pc1472;ecaes_pc1473;ecaes_pc1474;ecaes_pc1475;ecaes_pc1476;ecaes_pc1477;ecaes_pc1478;ecaes_pc1479;ecaes_pc1480"),
  encO = splitCSV("Gra_NumDocumento;TDoc_Sigla;Gra_AnoFin;Gra_NombresApellidos;Gra_Birthday;Gra_Genero;Ins_Codigo;Ins_Nombre;Dep_Nombre;Mun_Nombre;Are_Nombre;Pro_Nombre;Niv_Nombre;Met_Nombre;Mod_Nombre;Pro_Titulo;Gra_RegistroDiploma;Gra_FechaGrado;Gra_ActaGrado;Gra_Libro;Gra_Folio;Gra_NumICFES;Pro_Consecutivo_A;Dep_Codigo_A;Mun_Codigo_A;Are_Codigo_A;Niv_Codigo_A;Met_Codigo_A;Mod_Codigo_A;Npr_Codigo_A;Npr_Nombre_A;Gra_Semestre;Mps_TipoCotizante;Niv_Cod_Upper;Npr_Cod_Upper;Mps_IBC_Upper;Mps_IngresoBC;Mps_NumEmpleos;Mps_Ano;Mps_Ano_Upper;Mps_NumGrados;Mps_TipoDup;Mps_Dep_Cod;Mps_Mun_Cod;Mps_BirthYear;Gra_Cargue;Gra_actualizado;Gra_Estado;Reg_Estado;Reg_PrimerApellido;Reg_Particula;Reg_SegundoApellido;Reg_PrimerNombre;Reg_SegundoNombre;Rua_TipoCotizante;Rua_ActividadEconomica;Rua_Entidad;Rua_Rango;Rua_AFP;Rua_ARP;MpsRua_IBC;Usa_Cotizante;Icetex_Programa;Ins_Origen")
  ;
  public static void escrituraDatosEstudiante(PrintStream ps, int nes, IES ies, Estudiante e, Estudiante_DatosPersonales edp, String[] strsI, String[] strsE, String[] strsO) throws IOException {
    String[] linea = new String[encabezado.length];
    Arrays.fill(linea, "");
      //ps.println(CajaDeHerramientas.stringToCSV(encabezado));
      int pos = 0;
      linea[pos++] = String.valueOf(ies.codigo);
      linea[pos++] = String.valueOf(nes);
      linea[pos++] = new String(edp.apellido) + " " + new String(edp.nombre);
      linea[pos++] = String.valueOf(edp.documento);
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
        int area = prog==null?-1:prog.area;
        /*for (int iA = -1;iA<=9;iA++) {
          if (iA==0) continue;
          linea[pos++] = iA==area?"1":"0";
        }*/
        linea[pos++] = formateoCampoNum(area);
        linea[pos++] = formateoCampoNum(prog==null?-1:prog.nivel);
        linea[pos++] = formateoCampoNum(prog==null?-1:prog.nucleo);
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
        //System.out.println(j +"_" + jT);
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
        ps.println(CajaDeHerramientas.stringToCSV(linea)+CajaDeHerramientas.stringToCSV(strsI)+CajaDeHerramientas.stringToCSV(strsE)+CajaDeHerramientas.stringToCSV(strsO));
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
}