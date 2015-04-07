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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.jfree.data.io.CSV;

import spadies.io.MyDataOutputStream;
import spadies.kernel.Estudiante;
import spadies.kernel.Estudiante_DatosPersonales;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.kernel.Programa;
import spadies.kernel.VariableExtra;
import spadies.server.ServerMatch;
import spadies.server.kernel.TablaDepartamentos;
import spadies.server.util.OperacionesAdministrativas;
import spadies.util.CajaDeHerramientas;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;

public class VistaProgramasIES {
  public static class MapaContador extends TreeMap<Byte,Integer> {
    public MapaContador() {
      super();
    }
    public void put(Byte v) {
      Integer antes = get(v);
      super.put(v,(antes==null?0:antes)+1);
    }
  }
  public static class ResultadoPeriodoPrograma {
    MapaContador
      sexo = new MapaContador(),
      ingreso = new MapaContador(),
      icfes = new MapaContador(),
      estado = new MapaContador(),
      riesgo = new MapaContador();
    double repitencia = 0;
    int conrepitencia = 0;
    int conteo = 0;
    public void incorporarEstudiante(Estudiante e, double rep, Byte griesgo) {
      sexo.put(e.getSexo());
      ingreso.put(e.getIngresoHogar());
      icfes.put((Byte)Variable.CLASIFICACION_PUNTAJE_ICFES_DECILES_EST.rango.getRango(e.getPuntajeICFES()));
      estado.put(e.getEstado());
      riesgo.put(griesgo);
      conteo++;
      if (rep!=-1) {
        repitencia+=rep;
        conrepitencia++;        
      }
    }
    public void consolidar() {
      if (conrepitencia!=0)
        repitencia/=conrepitencia; 
    }
  }
  public static void main(String[] args) throws MyException, IOException {
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,false);
    PrintStream ps = new PrintStream(new File("agggg.csv"));
    for (IES ies:kernel.listaIES) {
      int n = ies.n;
      //Conteos [sexo,ingreso,icfes,estado,riesgo]
      ResultadoPeriodoPrograma[][] conteos = new ResultadoPeriodoPrograma[ies.programas.length+1][ies.n];
      for (int i=0,it=conteos.length,itm=conteos.length-1;i<it;i++)
        for (int j=0;j<n;j++)
          conteos[i][j] = new ResultadoPeriodoPrograma();
      for (Estudiante e:ies.estudiantes) {
        int jI=e.getSemestrePrimiparo();
        long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
        for (int j=jI; j<n; j++,matri>>>=1) if ((matri&1L)==1L) {
          int indp = e.getIndicePrograma();
          if (indp==-1) indp = ies.programas.length;
          conteos[indp][j].incorporarEstudiante(e, e.getRepitencias()[j],e.getClaseRiesgo(j));
        }
      }
      for (ResultadoPeriodoPrograma rppm[]:conteos)
        for (ResultadoPeriodoPrograma rpp:rppm)
          rpp.consolidar();
      //Imprimir resultado 
      for (int i=0,it=conteos.length,itm=conteos.length-1;i<it;i++) {
        boolean progNull = i==itm;
        String nom_program = progNull?"":new String(ies.programas[i].nombre);
        String cod_program = progNull?"":new String(ies.programas[i].codigoSNIES);
        String nom_program_snies = progNull?"":new String(ies.programas[i].nombre);
        String ies_ = String.valueOf(ies.codigo);
        String cod_muni = String.valueOf(ies.municipio);
        for (int j=0;j<n;j++) {
          ResultadoPeriodoPrograma res = conteos[i][j];
          if (res.conteo==0)continue;
          String semestre = ies.semestres[j];
          for (String s:new String[]{nom_program,cod_program,ies_,semestre,cod_muni})
            ps.print(s+";");          
          for (Comparable v:Variable.SEXO_EST.rango.getRango()){  
             String str = res.sexo.get(v)+"";
             if(!str.equals("null"))ps.print(str+";");
             else ps.print("0;");
          }
          for (Comparable v:Variable.INGRESO_HOGAR_EST.rango.getRango()){  
            String str = res.ingreso.get(v)+"";
            if(!str.equals("null"))ps.print(str+";");
            else ps.print("0;");
          }
          for (Comparable v:Variable.CLASIFICACION_PUNTAJE_ICFES_DECILES_EST.rango.getRango()){  
            String str = res.icfes.get(v)+"";
            if(!str.equals("null"))ps.print(str+";");
            else ps.print("0;");
          }
          ps.print(res.repitencia+";");
          for (Comparable v:Variable.CLASIFICACION_ESTADO_EST_DETALLADO.rango.getRango()){  
            String str = res.estado.get(v)+"";
            if(!str.equals("null"))ps.print(str+";");
            else ps.print("0;");
          }
          for (Comparable v:Variable.CLASIFICACION_RIESGO_PER.rango.getRango()){  
            String str = res.riesgo.get(v)+"";
            if(!str.equals("null"))ps.print(str+";");
            else ps.print("0;");
          }
          ps.println();
        }
      }
    }
  }

  public static void exportarProgramasIES(KernelSPADIES kernel, Collection<Integer> instituciones) throws FileNotFoundException {
    String[] enc = new String[]{"Nombre", "CodSNIES", "Area", "Nucleo", "Nivel"};
    for (Integer codIES: instituciones) {
      PrintStream ps = new PrintStream("programas_"+codIES+".csv");
      ps.println(CajaDeHerramientas.stringToCSV(enc));
      for (Programa pr: kernel.getIES(codIES).programas) {
        String[] reg = new String[]{new String(pr.nombre), new String(pr.codigoSNIES), ""+pr.area, ""+pr.nucleo, ""+pr.nivel};
        ps.println(CajaDeHerramientas.stringToCSV(reg));
      }
      ps.close();
    }
  }
  public static void medicionImpactoVars(KernelSPADIES kernel) throws FileNotFoundException, MyException {
    {
      Variable[] vars = new Variable[]{
          Variable.SEXO_EST,
          Variable.TRABAJABA_CUANDO_ICFES_EST,
          Variable.CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST,
          Variable.INGRESO_HOGAR_EST,
          Variable.VIVIENDA_PROPIA_EST,
          Variable.NUMERO_HERMANOS_EST,
          Variable.POSICION_ENTRE_LOS_HERMANOS_EST,
          Variable.CLASIFICACION_PUNTAJE_ICFES_EST,
          Variable.NIVEL_EDUCATIVO_MADRE_EST,
          Variable.NUMERO_APOYOS_ACADEMICOS_RECIBIDOS_EST,
          Variable.NUMERO_APOYOS_FINANCIEROS_RECIBIDOS_EST,
          Variable.NUMERO_APOYOS_OTROS_RECIBIDOS_EST,
          Variable.NUMERO_APOYOS_ICETEX_RECIBIDOS_EST,
      };
      for (IES ies:kernel.listaIES) {
        //if (ies.codigo!=1101) continue;
        Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
        PrintStream ps = new PrintStream("sals_csv/"+ies.codigo+".csv");
        for (Variable var:vars) {
          Object[] res = kernel.getConteoPoblacion(new Filtro[]{filtro}, new Variable[]{var});
          //Object[] res = kernel.getConteoPoblacion(new Filtro[]{}, new Variable[]{var});
          if (res==null) {
            System.err.println(ies.codigo + "\t" + var.nombre);
            continue;
          }
          Map<byte[], int[]> resC = (Map<byte[], int[]>) res[0];
          Map<byte[], double[]> resP = (Map<byte[], double[]>) res[1];
          Integer tam = (Integer) res[4];
          //System.out.println(var.nombre);
          //String nomVal = var.rango.toString(var.rango.byteToRango(ent.getKey()[0]));
          //System.err.println(resP.size() + "\t" + resC.size());
          int numSeries = resP.size();
          double [] varInic = new double[numSeries];
          double[][] tabP = new double[numSeries][tam];//Torcido mejoraria eficiencia
          int[][] tabC = new int[numSeries][tam];
          byte[][] tabInd = new byte[numSeries][]; 
          {
            int serie = 0;
            for (Entry<byte[], double[]> ent: resP.entrySet()) {
              varInic[serie] = resC.get(ent.getKey())[0];
              tabInd[serie] = ent.getKey();
              int ti = 0;
              int[] serieC = resC.get(ent.getKey());
              for (double x: ent.getValue()) {
                tabC[serie][ti] = serieC[ti];
                tabP[serie][ti] = x;
                ti++;
              }
              serie++;
            }
          }

          int serieMayor = -1;
          int ocurrenciasSerieMayor = -1;
          {
            int [] numMayorias = new int[numSeries]; Arrays.fill(numMayorias, 0);
            for (int i=0;i<tam;i++) {
              int iMax = -1;
              double max = Double.MIN_VALUE;
              for (int j = 0;j<numSeries;j++) {
                if (tabP[j][i]>max) {
                  max = tabP[j][i];
                  iMax = j;
                }
              }
              if (iMax!=-1 && max>0) numMayorias[iMax]++;
            }
            {
              int iSerieMayor = -1;
              int max = Integer.MIN_VALUE;
              for (int i=0;i<numSeries;i++) if (numMayorias[i]>max) {
                max = numMayorias[i];
                iSerieMayor = i;
              }
              serieMayor = iSerieMayor;
              ocurrenciasSerieMayor = max;
            }
          }
          //String nomVal = var.rango.toString(var.rango.byteToRango(ent.getKey()[0]));
          //ps.println(var.nombre + ";" + var.rango.toString(var.rango.byteToRango(tabInd[serieMayor][0])) + ";" + ocurrenciasSerieMayor + ";" +tam);
          double valTo = 0;
          for (int i=0;i<numSeries;i++) {
            String[] linea =  new String[tam+1];
            linea[0] = var.rango.toString(var.rango.byteToRango(tabInd[i][0]));
            if (linea[0].equals(Constantes.S_DESCONOCIDO)) continue;
  
            for (int j=0;j<tam;j++) {
              //linea[j+1] = String.valueOf(varInic[i] * (tabP[serieMayor][j] - tabP[i][j])); 
              //linea[j+1] = String.valueOf(Math.round(varInic[i] * (tabP[serieMayor][j] - tabP[i][j])));
              //linea[j+1] = String.valueOf(tabC[i][j] * (tabP[serieMayor][j] - tabP[i][j])).replace('.', ',');
              valTo+=tabC[i][j] * (tabP[serieMayor][j] - tabP[i][j]);
            }
            //for (String campo: linea) ps.print(campo+";"); ps.println();
          }
          ps.println(var.nombre + ";" + var.rango.toString(var.rango.byteToRango(tabInd[serieMayor][0])) + ";" + ocurrenciasSerieMayor + ";" +tam + ";" + String.valueOf(valTo).replace('.', ','));
          //ps.println();
          /*
          System.out.println(tam);
          System.out.println("_"+resC.size());
          for (Entry<byte[], int[]> ent: resC.entrySet()) {
            System.out.println(ent.getKey().length + " " + ent.getValue().length);
          }
          System.out.println("%"+resP.size());
          for (Entry<byte[], double[]> ent: resP.entrySet()) {
            System.out.println(ent.getKey().length + " " + ent.getValue().length);
          }
          */
        }
        ps.close();
      }
    }
  }
  
  public static void medicionImpactoVars__TEST(KernelSPADIES kernel) throws FileNotFoundException, MyException {
    {
      Variable[] vars = new Variable[]{
          Variable.CLASIFICACION_PUNTAJE_ICFES_EST,
          Variable.NIVEL_EDUCATIVO_MADRE_EST,
          Variable.VIVIENDA_PROPIA_EST,
          Variable.NUMERO_HERMANOS_EST,
          Variable.CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST,
          Variable.AREA_CONOCIMIENTO_EST,
          //Variable.PERIODO_INGRESO_EST,
      };
      for (IES ies:kernel.listaIES) {
        //if (ies.codigo!=1101) continue;
        Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
        PrintStream ps = new PrintStream("sals_csv/"+ies.codigo+".csv");
        for (Variable var:vars) {
          Object[] res = kernel.getConteoPoblacion(new Filtro[]{filtro}, new Variable[]{var});
          if (res==null) {
            System.err.println(ies.codigo + "\t" + var.nombre);
            continue;
          }
          Map<byte[], int[]> resC = (Map<byte[], int[]>) res[0];
          Map<byte[], double[]> resP = (Map<byte[], double[]>) res[1];
          Integer tam = (Integer) res[4];
          //System.out.println(var.nombre);
          //String nomVal = var.rango.toString(var.rango.byteToRango(ent.getKey()[0]));
          //System.err.println(resP.size() + "\t" + resC.size());
          int numSeries = resP.size();
          double [] varInic = new double[numSeries];
          double[][] tabP = new double[numSeries][tam];//Torcido mejoraria eficiencia
          int[][] tabC = new int[numSeries][tam];
          byte[][] tabInd = new byte[numSeries][]; 
          {
            int serie = 0;
            for (Entry<byte[], double[]> ent: resP.entrySet()) {
              varInic[serie] = resC.get(ent.getKey())[0];
              tabInd[serie] = ent.getKey();
              int ti = 0;
              int[] serieC = resC.get(ent.getKey());
              for (double x: ent.getValue()) {
                tabC[serie][ti] = serieC[ti];
                tabP[serie][ti] = x;
                ti++;
              }
              serie++;
            }
          }
          
       
          int serieMayor = -1;
          int ocurrenciasSerieMayor = -1;
          {
            int [] numMayorias = new int[numSeries]; Arrays.fill(numMayorias, 0);
            for (int i=0;i<tam;i++) {
              int iMax = -1;
              double max = Double.MIN_VALUE;
              for (int j = 0;j<numSeries;j++) {
                if (tabP[j][i]>max) {
                  max = tabP[j][i];
                  iMax = j;
                }
              }
              if (iMax!=-1 && max>0) numMayorias[iMax]++;
            }
            {
              int iSerieMayor = -1;
              int max = Integer.MIN_VALUE;
              for (int i=0;i<numSeries;i++) if (numMayorias[i]>max) {
                max = numMayorias[i];
                iSerieMayor = i;
              }
              serieMayor = iSerieMayor;
              ocurrenciasSerieMayor = max;
            }
          }
          //String nomVal = var.rango.toString(var.rango.byteToRango(ent.getKey()[0]));
          ps.println(var.nombre + ";" + var.rango.toString(var.rango.byteToRango(tabInd[serieMayor][0])) + ";" + ocurrenciasSerieMayor + ";" +tam);
          for (int i=0;i<numSeries;i++) {
            String[] linea =  new String[tam+1];
            linea[0] = var.rango.toString(var.rango.byteToRango(tabInd[i][0]));  
            for (int j=0;j<tam;j++) {
              //linea[j+1] = String.valueOf(varInic[i] * (tabP[serieMayor][j] - tabP[i][j])); 
              //linea[j+1] = String.valueOf(Math.round(varInic[i] * (tabP[serieMayor][j] - tabP[i][j])));
              linea[j+1] = String.valueOf(tabC[i][j] * (tabP[serieMayor][j] - tabP[i][j])).replace('.', ',');
            }
            for (String campo: linea) ps.print(campo+";"); ps.println();
          }
          ps.println();
          /*
          System.out.println(tam);
          System.out.println("_"+resC.size());
          for (Entry<byte[], int[]> ent: resC.entrySet()) {
            System.out.println(ent.getKey().length + " " + ent.getValue().length);
          }
          System.out.println("%"+resP.size());
          for (Entry<byte[], double[]> ent: resP.entrySet()) {
            System.out.println(ent.getKey().length + " " + ent.getValue().length);
          }
          */
        }
        ps.close();
      }
    }
  }
  
  public static final String formateoCampoNum(int num) {
    return num==-1?"":String.valueOf(num);
  }
  
  public static final String formateoCampoNum(double num) {
    return num==-1.0?"":String.valueOf(num);
  }
  
  public static void escrituraDatos() {
    try {
      KernelSPADIES kernel=KernelSPADIES.getInstance();
      int maxN = Integer.MIN_VALUE;
      for (IES ies: kernel.listaIES) {
        //ies.n             
      }
      String[] encabezado = new String[]{
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
          "prog_nivel","prog_nucleo",
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
          "p11","p12","p13","p14","p15","p16","p17","p18","p19","p20","p21",
      };
      String[] linea = new String[encabezado.length];
      Arrays.fill(linea, "");
      PrintStream ps = new PrintStream("vistaIES.csv");
      ps.println(CajaDeHerramientas.stringToCSV(encabezado));
      for (IES ies: kernel.listaIES) {
        System.out.println("*" + ies.codigo);
        //PrintStream ps = new PrintStream(ies.codigo + "_completo.csv");
        //ps.println(CajaDeHerramientas.stringToCSV(encabezado));
        int nes = 0;
        for (Estudiante e:ies.estudiantes) {
          int pos = 0;
          linea[pos++] = String.valueOf(ies.codigo);
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
            linea[pos++] = formateoCampoNum(e.getIndicePrograma()<0?-1:ies.programas[e.getIndicePrograma()].nivel);
            linea[pos++] = formateoCampoNum(e.getIndicePrograma()<0?-1:ies.programas[e.getIndicePrograma()].nucleo);
          }
          {
            byte estado = e.getEstado();
            linea[pos++] = estado==1?"1":"0";
            linea[pos++] = estado==2?"1":"0";
            linea[pos++] = estado==0?"1":"0";
            linea[pos++] = estado<0?"1":"0";
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
      }
      ps.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Fin escritura");
  }
  
  public static void escrituraGraduados() {
    try {
      KernelSPADIES kernel=KernelSPADIES.getInstance();
      int maxN = kernel.getSemestresActivos(new spadies.util.variables.Filtro[]{}).length;
      String[] encabezado = new String[]{
          "ies_origen",
          "ies_caracter",
          "ies_departamento",
          "ies_municipio",
          "estudiante_numero",
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
          "areaD","area1","area2","area3","area4","area5","area6","area7","area8","area9",
          "materias_tomadas_total",
          "materias_aprobadas_total",
          "apoyos_aca",
          "apoyos_fin",
          "apoyos_otr",
          "ictx_n","ictx_l","ictx_m","ictx_a",
          //TODO implementacion LAZY :P :( Eventualmente "fixear" je
          "p1","p2","p3","p4","p5","p6","p7","p8","p9","p10",
          "p11","p12","p13","p14","p15","p16","p17","p18","p19","p20",
      };
      String[] linea = new String[encabezado.length];
      PrintStream ps = new PrintStream("graduados_completo.csv");
      ps.println(CajaDeHerramientas.stringToCSV(encabezado));
      Arrays.fill(linea, "");
      int nes = 0;
      for (IES ies: kernel.listaIES) {
        int pos = 0;
        System.out.println("*" + ies.codigo);
        linea[pos++] = formateoCampoNum(ies.origen);
        linea[pos++] = formateoCampoNum(ies.caracter);
        linea[pos++] = formateoCampoNum(ies.departamento);
        linea[pos++] = formateoCampoNum(ies.municipio);
        
        for (Estudiante e:ies.estudiantes) {
          if (e.getSemestreGrado()==-1) continue;
          int epos = pos;
          linea[epos++] = String.valueOf(nes++);
          linea[epos++] = formateoCampoNum(e.getSexo());
          linea[epos++] = formateoCampoNum(e.getEdadAlPresentarElICFES());
          linea[epos++] = formateoCampoNum(e.getNivelEducativoMadre());
          linea[epos++] = formateoCampoNum(e.getIngresoHogar());
          linea[epos++] = formateoCampoNum(e.getViviendaPropia());
          linea[epos++] = formateoCampoNum(e.getNumeroHermanos());
          linea[epos++] = formateoCampoNum(e.getPosicionEntreLosHermanos());
          linea[epos++] = formateoCampoNum(e.getPuntajeICFES());
          linea[epos++] = formateoCampoNum(e.getTrabajabaCuandoPresentoIcfes());
          linea[epos++] = ies.semestres[e.getSemestrePrimiparo()];
          int g = e.getSemestreGrado();
          linea[epos++] = g==-1?"":ies.semestres[g];
          /*int rf = e.getSemestreRetiroForzoso();
          linea[pos++] = rf==-1?"":ies.semestres[rf];*/
          {
            Programa prog = e.getIndicePrograma()<0?null:ies.programas[e.getIndicePrograma()];
            int area = prog==null||prog.area==-1?-1:prog.area;
            for (int iA = -1;iA<=9;iA++) {
              if (iA==0) continue;
              linea[epos++] = iA==area?"1":"0";
            }
          }
          long matri=e.getSemestresMatriculadoAlDerecho();
          int apoA=0, apoF=0, apoO=0, ictx[] = new int[4], mA = 0, mT = 0;
          Set<Integer> pres = new TreeSet<Integer>();
          for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
            int mTP= e.getNumeroMateriasTomadas(j),
              mAP= e.getNumeroMateriasAprobadas(j);
            if (mTP!= -1 && mAP!=-1) {
              mT+=mTP;
              mA+=mAP;
            }
            apoA+= e.getRecibioApoyoAcademico(j)?1:0;
            apoF+= e.getRecibioApoyoFinanciero(j)?1:0;
            apoO+= e.getRecibioApoyoOtro(j)?1:0;
            {
              byte icetex = e.getTipoApoyoICETEXRecibido(j);
              for (int i=0;i<4;i++) ictx[i] = i==icetex?1:0;  
            }
            /*
            for (int j2=0; j2<jT; j2++) {
              linea[epos++] = j2==j?"1":"0";
            }*/
            pres.add(j);
          }
          linea[epos++] = formateoCampoNum(mT);
          linea[epos++] = formateoCampoNum(mA);
          linea[epos++] = formateoCampoNum(apoA);
          linea[epos++] = formateoCampoNum(apoF);
          linea[epos++] = formateoCampoNum(apoO);
          for (int i=0;i<4;i++) linea[epos++] = formateoCampoNum(ictx[i]);
          for (int i=0;i<20;i++) {
            linea[epos++] = i<ies.minCodigoSemestre || i>ies.maxCodigoSemestre?
                "0"
                :
                  (pres.contains(i-ies.minCodigoSemestre)?"1":"0");
          }
          ps.println(CajaDeHerramientas.stringToCSV(linea));
          nes++;
        }
      }
      ps.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Fin escritura");
  }

  public static void escrituraArchivoIndividuosIes(KernelSPADIES kernel, File csvSalida) throws FileNotFoundException     {
    PrintStream ps = new PrintStream(csvSalida);
    String[] encabezado = new String[]{};
    ps.println(CajaDeHerramientas.stringToCSV(encabezado));
    for (IES ies: kernel.listaIES) {
      //System.out.println("Preparado " + ies.codigo);
      //System.out.println((System.currentTimeMillis()-tm)/1000);
      int ne= ies.estudiantes.length;
      int ie = 0;
      for (int i=0;i<ne;i++) {
        Estudiante_DatosPersonales edp = ies.datosPersonalesEstudiantes==null?null:ies.datosPersonalesEstudiantes[i];
        String doc = edp.documento==-1?"":String.valueOf(edp.documento),
            nombre = (new String(edp.apellido) + " " + new String(edp.nombre)).trim(),
            fec = (edp.diaFechaNacimiento==-1?"":edp.diaFechaNacimiento)+"/"+(edp.mesFechaNacimiento==-1?"":edp.mesFechaNacimiento)+"/"+(edp.anhoFechaNacimiento==-1?"":edp.anhoFechaNacimiento); 
        ps.println(CajaDeHerramientas.stringToCSV(""+ies.codigo, ie+"",nombre, doc, fec));
        ++ie;
      }
    }
    ps.close();
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
 
}