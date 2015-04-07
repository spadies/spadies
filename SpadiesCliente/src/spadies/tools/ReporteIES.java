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
package spadies.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import spadies.gui.util.InfoTabla;
import spadies.kernel.Estudiante;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.kernel.Programa;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.variables.AmbienteVariables;
import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;

/**
 * Exporta a un formato de CSV usado en la sincronizacion,
 * la información contenida en los archivos en la carpeta datos. 
 * Los archivos CSV son escritos en una carpeta salidaCSV en la ruta de ejecución
 */
public class ReporteIES {
  private static KernelSPADIES kernel = null;
  private static PrintStream ps = null;
  private static void pl(String s) {ps.println(s);};
  private static void p(String s) {ps.print(s);};
  private static final transient DecimalFormat df=new DecimalFormat("0.00%");
  public static void main(String[] args) throws MyException, IOException {
    Constantes.cargarArchivoFiltroIES();
    kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,true);
    File carSal = new File("reporteHTML");
    carSal.mkdir();
    for (IES ies: kernel.listaIES) {
      escribirReporteIES(ies, new File(carSal, ies.codigo + ".html"));
    }
  }
  public static void salida() throws MyException, IOException {
    kernel = KernelSPADIES.getInstance();
    File carSal = new File("reporteHTML");
    if (!carSal.exists()) carSal.mkdir();
    for (IES ies: kernel.listaIES) {
      escribirReporteIES(ies, new File(carSal, ies.codigo + ".html"));
    }
  }
  private static void escribirReporteIES(IES ies, File fSal) throws FileNotFoundException, MyException {
    Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
    AmbienteVariables.getInstance().notificarCambioSeleccion(new Filtro[]{filtro});
    ps = new PrintStream(fSal);
    pl("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
    pl("<html>");
    pl("<head>");
    pl("<style>");
    pl("<!--");
    
    pl("th {text-align: center;");
    pl("border: 1px solid black;");
    pl("background-color: #aaa;}");
    
    pl("td {text-align: right;");
    pl("border: 1px solid black;");
    pl("}");

    pl("-->");
    pl("</style>");
    p("<title>Reporte IES" + ies.codigo + "</title></head>");
    tablaIdentificacionIES(ies);
    tablaInventarioDatos(ies);
    tablaInventarioProgramas(ies);
    tablaPeriodosProgramas(ies);
    //tablaProgramasICFES(ies);
    //tablaProgramasPrimiparos(ies);
    tablaProgramasMatriculados(ies);
    tablaAux(ies);
    pl("</html>");
    ps.close();
  }
  private static void tablaInventarioDatos(IES ies) {
    Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
    Object[] res = kernel.getTablaCantidadArchivos(new Filtro[]{filtro});
    InfoTabla it = new InfoTabla((String[][])(res[0]),(String[][])(res[1]),(String[])(res[2]));
    imprimirTabla(it);
  }

  private static void tablaIdentificacionIES(IES ies) {
    Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
    int numEst = kernel.getCantidadEstudiantes(new Filtro[]{filtro});
    p("<table><caption>Datos de la IES</caption>");
    p("<tr><th>Codigo</th><td>"+ies.codigo+"</td></tr>");
    p("<tr><th>Nombre</th><td>"+new String(ies.nombre)+"</td></tr>");
    p("<tr><th>Periodos</th><td>"+ies.semestres[0]+"-"+ies.semestres[ies.semestres.length-1]+"</td></tr>");
    p("<tr><th>Sujetos</th><td>"+numEst+"</td></tr>");
    p("</table>");
  }

  private static void tablaInventarioProgramas(IES ies) throws MyException {
    String[][] dat = new String[ies.programas.length][1];
    //String[][] encF = new String[ies.programas.length+1][1];
    int i = 0;
    for(Programa p:ies.programas) {
      //encF[i+1][0] = String.valueOf(i + 1);
      dat[i] = new String[]{new String(p.codigoSNIES),
          new String(p.nombre),String.valueOf(p.area),
          String.valueOf(p.nucleo),String.valueOf(p.nivel),
          String.valueOf(p.metodologia),
      };
      i++;
    }
    InfoTabla res = new InfoTabla(dat,new String[0][0]/*encF*/,new String[]{"Codigo","Nombre","Area","Nucleo","Nivel","Metodologia"});
    imprimirTabla(res,"Programas");
  }
  private static void tablaPeriodosProgramas(IES ies) throws MyException {
    String[][] dat = new String[ies.programas.length+1][ies.n];
    String[][] encF = new String[1][ies.programas.length+2];
    for(int i = 0,t = ies.programas.length;i<t;i++) encF[0][i+2] = new String(ies.programas[i].nombre);

    SortedMap<Integer,Integer> resp = new TreeMap<Integer, Integer>();
    for (Estudiante e:ies.estudiantes) {
      int val = (e.getIndicePrograma()==-1?99999:e.getIndicePrograma())*100+e.getSemestrePrimiparo();
      Integer valp = resp.get(val);
      resp.put(val,(valp==null?0:valp)+1);
    }
    for(int i = -1,t = ies.programas.length;i<t;i++) {
      for(int j = 0;j<ies.n;j++) {
        Integer val = resp.get(100*j+(i==-1?99999:i));
        dat[i+1][j] = val==null?"":val.toString();
      }
    }
    InfoTabla res = new InfoTabla(dat,encF,ies.semestres);
    imprimirTabla(res,"Cohorte por programa");
  }
  private static void tablaProgramasMatriculados(IES ies) throws MyException {
    Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
    InfoTabla[] res = cruceVariables(new Filtro[]{filtro}, Variable.PROGRAMA_EST, Variable.PERIODO_MATRICULADO_PER);
    imprimirTabla(res[0],"Matriculas por programa");
  }
  private static void tablaProgramasPrimiparos(IES ies) throws MyException {
    Filtro filtro1 = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")}),
      filtro2 = new Filtro(Variable.NUMERO_SEMESTRE_PER, new Item[]{new Item((byte)1,"","")});
    InfoTabla[] res = cruceVariables(new Filtro[]{filtro1,filtro2}, Variable.PROGRAMA_EST, Variable.PERIODO_INGRESO_EST);
    imprimirTabla(res[0]);
  }
  private static void tablaProgramasICFES(IES ies) throws MyException {
    Filtro filtro1 = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")}),
      filtro2 = new Filtro(Variable.NUMERO_SEMESTRE_PER, new Item[]{new Item((byte)1,"","")});
    InfoTabla[] res = cruceVariables(new Filtro[]{filtro1,filtro2}, Variable.PROGRAMA_EST, Variable.CLASIFICACION_PUNTAJE_ICFES_EST);
    imprimirTabla(res[1]);
    imprimirTabla(res[0]);
  }

  private static void tablaAux(IES ies) throws MyException {
    Filtro filtro1 = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")}),
      filtro2 = new Filtro(Variable.NUMERO_SEMESTRE_PER, new Item[]{new Item((byte)1,"","")}),
      filtro3 = new Filtro(Variable.CLASIFICACION_ESTADO_EST, new Item[]{new Item((byte)-1,"",""),new Item((byte)2,"","")});
    InfoTabla[] res = cruceVariables(new Filtro[]{filtro1,filtro2,filtro3}, Variable.PERIODO_INGRESO_EST, Variable.ULTIMO_PERIODO_MATRICULADO_EST);
    imprimirTabla(res[0]);
  }
  
  private static InfoTabla[] cruceVariables(Filtro[] filtros, Variable difX, Variable difY) throws MyException {
    Variable[] diferenciados = new Variable[]{difX,difY};
    Object[] resultado = KernelSPADIES.getInstance().getCruceVariables(filtros,diferenciados);
    Map<byte[],int[]> resC=(Map<byte[],int[]>)(resultado[0]);
    Integer[] codigosIESDif=(Integer[])(resultado[1]);
    String[] codigosProgramasDif=(String[])(resultado[2]);
    Map<Byte,Integer> relX = new TreeMap<Byte,Integer>(),
    relY = new TreeMap<Byte,Integer>();
    {
      SortedSet<Byte> valsX = new TreeSet<Byte>(),
        valsY = new TreeSet<Byte>();
      for (byte[] llave:resC.keySet()) {
        valsX.add(llave[0]);
        valsY.add(llave[1]);
      }
      int ix = 0;
      for (byte codVar:valsX) {
        relX.put(codVar, ix++);
      }
      ix = 0;
      for (byte codVar:valsY) {
        relY.put(codVar, ix++);
      }
    }
    String encFilas[][]=new String[2][relX.size()+1],encColumnas[]=new String[relY.size()];
    Iterator<Byte> it = relX.keySet().iterator();
    encFilas[0][0]=encFilas[1][0]="";
    for (int i=0,ta=relX.size(); i<ta; i++) {
      Byte val = it.next();
      encFilas[0][i+1]=diferenciados[0].nombre;
      encFilas[1][i+1]=val==-1?Constantes.S_DESCONOCIDO:Variable.toString(diferenciados[0],val,codigosIESDif,codigosProgramasDif);
    }
    it = relY.keySet().iterator();
    for (int i=0,ta=relY.size(); i<ta; i++) encColumnas[i]=Variable.toString(diferenciados[1],it.next(),codigosIESDif,codigosProgramasDif);
    double totX[] = new double[relX.size()];
    double totY[] = new double[relY.size()];
    double tot = 0; 
    {
      for (Map.Entry<byte[],int[]> eC:resC.entrySet()) {
        int cont = eC.getValue()[0];
        byte[] llave = eC.getKey();
        int fil =relX.get(llave[0]), col = relY.get(llave[1]); 
        tot+=cont;
        totX[fil]+= cont;
        totY[col]+= cont;
      }
    }
    String[][] valores=new String[relX.size()][relY.size()],
      valoresPX=new String[relX.size()][relY.size()],
      valoresPY=new String[relX.size()][relY.size()],
      valoresPT=new String[relX.size()][relY.size()];
    {
      for (Map.Entry<byte[],int[]> eC:resC.entrySet()) {
        int cont = eC.getValue()[0];
        byte[] llave = eC.getKey();
        int fil =relX.get(llave[0]), col = relY.get(llave[1]); 
        valores[fil][col]=String.valueOf(cont);
        valoresPX[fil][col]=df.format(cont/totX[fil]);
        valoresPY[fil][col]=df.format(cont/totY[col]);
        valoresPT[fil][col]=df.format(cont/tot);
      }
    }
    InfoTabla tabla=new InfoTabla(valores,encFilas,encColumnas),
      tablaPX=new InfoTabla(valoresPX,encFilas,encColumnas),
      tablaPY=new InfoTabla(valoresPY,encFilas,encColumnas),
      tablaPT=new InfoTabla(valoresPT,encFilas,encColumnas);
    return new InfoTabla[]{tabla, tablaPX, tablaPY, tablaPT};
  }
  private static void imprimirTabla(InfoTabla it) {
    imprimirTabla(it,"");
  }
  private static void imprimirTabla(InfoTabla it, String titulo) {
    p("<table>");
    p("<caption>"+titulo+"</caption>");
    int nc = it.getNumColumnas(),
      nef = it.getNumEncabezadosFilas(),
      nf = it.getNumFilas();
    p("<thead><tr>");
    for (int i = 0; i < nef;i++) p("<th>"+it.getEncabezadoFila(0, i)+"</th>"); 
    for (int i = 0; i < nc;i++) p("<th>" + it.getEncabezadoColumna(i) + "</th>");
    p("</tr></thead><tbody>");
    for (int i = 0; i < nf;i++) {
      p("<tr>");
      for (int j = 0; j < nef;j++) p("<th>" + it.getEncabezadoFila(i+1, j) + "</th>");
      for (int j = 0; j < nc;j++) p("<td>" + it.getValor(i, j) + "</td>");
      p("</tr>");
    }
    p("</tbody></table>");
  }

}