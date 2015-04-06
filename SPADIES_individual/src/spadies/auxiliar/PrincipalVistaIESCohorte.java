package spadies.auxiliar;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import spadies.kernel.Estudiante;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;

public class PrincipalVistaIESCohorte {
  private static final DecimalFormat formatoNumeros = new DecimalFormat("####.####");
  private static int numVariablesAcumuladas = 8;
  //0 Sexo
  //1 Vivienda propia
  //2 Trabajaba
  //3 Ingreso hogar
  //4 Numero hermanos
  //5 Posicion hermanos
  //6 Puntaje ICFES
  //7 Edad presentacion
  
  private static final Collection<String> encabezado = new LinkedList<String>(Arrays.asList(
    "codigo","caracter","origen","departamento",
    "cohorte","individuos","proporcion_acces",
    "sexo","vivienda","trabajaba","ingreso_hogar","numhermanos","poshermanos","puntaje_icfes","edad_presenta",
    "A1","A2","A3","A4","A5","A6","A7","A8","A9",
    "TasaDesercion")
  );
  static {
    for (int i=1;i<=22;i++) encabezado.add("da"+i);
  }
  //Nivel educativo madre
  public static void main(String[] args) throws MyException, FileNotFoundException {
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,false);
    PrintStream ps = new PrintStream("VistaIESCohorte.csv");
    ps.println(elementoATextoDelimitado(encabezado));
    for (IES ies:kernel.listaIES) {
      Filtro [] filtros = new Filtro[]{new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")})};
      Object[] res1 = null;//kernel.getDesercionPorCohorte(filtros, new Variable[]{});
      //double[][] datos1 = (double[][]) res1[1];
      Map map1 = (Map)res1[1];
      double[][] datos1 = null;
      if (map1.size()==1) datos1 = (double[][]) map1.values().iterator().next();
      else {
        datos1 = new double[ies.n][ies.n];
        for (int i=0;i<ies.n;i++) Arrays.fill(datos1[i],Double.MAX_VALUE);
      }
      Object[] res2 = null;//kernel.getTasaDesercion(filtros,new Variable[0]);
      Map map2 = (Map)res2[2];
      double [] datos2 = null;
      if (map2.size()==1) datos2 = (double[]) map2.values().iterator().next();
      else datos2 = new double[ies.n];
      Collection<String> camposIES = new LinkedList<String>();
      camposIES.add(ies.codigo+"");
      camposIES.add(ies.caracter+"");
      camposIES.add(ies.origen+"");
      camposIES.add(ies.departamento+"");
      int [][] conteoVar = new int[ies.n][numVariablesAcumuladas];
      int [][] sumaVar = new int[ies.n][numVariablesAcumuladas];
      int [][] conteoAreas = new int[ies.n][9];
      int [] totalConAreas = new int[ies.n];
      int [] individuosCohorte = new int[ies.n];
      int [] conAccesP = new int[ies.n];
      for (Estudiante e:ies.estudiantes) {
        int coh = e.getSemestrePrimiparo();
        individuosCohorte[coh]++;
        int ip = e.getIndicePrograma();
        if (e.getTipoApoyoICETEXRecibido(coh)==3) conAccesP[coh]++;
        int area = ip==-1?-1:ies.programas[ip].area;
        if (area!=-1) {
          totalConAreas[coh]++;
          conteoAreas[coh][area-1]++;
        }
        for (int i=0;i<numVariablesAcumuladas;i++) {
          int val = -1;
          switch(i) {
          case 0: val = e.getSexo();
            break;
          case 1: val = e.getViviendaPropia();
            break;
          case 2: val = e.getTrabajabaCuandoPresentoIcfes();
            break;
          case 3: val = e.getIngresoHogar();
            break;
          case 4: val = e.getNumeroHermanos();
            break;
          case 5: val = e.getPosicionEntreLosHermanos();
            break;
          case 6: val = e.getPuntajeICFES();
            break;
          case 7: val = e.getEdadAlPresentarElICFES();
            break;
          }
          if (val!=-1) {
            conteoVar[coh][i]++;
            sumaVar[coh][i]+=val;
          }
        }
      }
      for (int i=0;i<ies.n;i++) {
        Collection<String> campos = new LinkedList<String>(camposIES);
        campos.add(ies.semestres[i]);
        campos.add(""+individuosCohorte[i]);
        campos.add(""+formatoNumeros.format((1d*conAccesP[i])/individuosCohorte[i]));
        for (int j=0;j<numVariablesAcumuladas;j++) {
          campos.add(conteoVar[i][j]==0?"":""+formatoNumeros.format((1d*sumaVar[i][j])/conteoVar[i][j]));
        }
        for (int j=0;j<9;j++) {
          campos.add(totalConAreas[i]==0?"":""+formatoNumeros.format((1d*conteoAreas[i][j])/totalConAreas[i]));
        }
        campos.add(datos2[i]==0?"":formatoNumeros.format(datos2[i]));
        for (double d:datos1[i]) campos.add(d==Double.MAX_VALUE?"":formatoNumeros.format(d));
        ps.println(elementoATextoDelimitado(campos).replace('.', ','));
      }
    }
    ps.close();
  }
  private static String elementoATextoDelimitado(Collection<String> c) {
    StringBuilder sb = new StringBuilder();
    for (String s:c) {sb.append(s); sb.append(";");};
    return sb.toString();
  }
}
