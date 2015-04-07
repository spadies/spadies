package spadies.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jfree.chart.ChartPanel;

import spadies.gui.graficas.FabricaGraficas;
import spadies.gui.util.RutinasGUI;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.kernel.Programa;
import spadies.util.CajaDeHerramientas;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.variables.AmbienteVariables;
import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;

public class DiagnosticoSACES {
  static PrintStream ps_dc, ps_dp, ps_d3, ps_pr;
  static KernelSPADIES kernel;
  public static void main(String[] args) throws MyException, FileNotFoundException {
    System.out.println("IES\tPERIODOS\tPROGRAMAS\tEXCLUIDO");
    AmbienteVariables av = AmbienteVariables.getInstance();
    kernel = KernelSPADIES.getInstance();
    ps_dc = new PrintStream("des_coh.csv");
    ps_dp = new PrintStream("des_per.csv");
    ps_d3 = new PrintStream("des_co3.csv");
    ps_pr = new PrintStream("pro_pro.csv");
    ps_pr.println("ies;nombre;codigo");
    for (File f:Constantes.carpetaDatos.listFiles()) {
      //System.out.println(f);
      if (!f.isDirectory() && f.getName().toLowerCase().matches("\\d\\d\\d\\d\\.spa")) {
        Constantes.filtroIES = Collections.singleton(f.getName().substring(0, 4));
        kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,false);
        av.notificarCarga();
        Filtro[] filtroVacio = new Filtro[0];
        Variable[] diferenciados = new Variable[]{Variable.PROGRAMA_EST};
        av.notificarCambioSeleccion(filtroVacio);
        
        IES ies = kernel.listaIES[0];
        int codies = ies.codigo;
        {
          Map<String,Collection<String>> programas = new TreeMap<String, Collection<String>>();
          for (String p:av.getNombresProgramas())
            programas.put(p, new TreeSet<String>());
          for (Programa p:ies.programas) {
            programas.get(new String(p.nombre)).add(new String(p.codigoSNIES));
          }
          for (Entry<String, Collection<String>> e:programas.entrySet())
            for (String v:e.getValue())
              ps_pr.println(codies + ";'" + e.getKey() + ";'" + v);
        }

        String[] nomprog = av.getNombresProgramas();
        int npl = nomprog.length;
        boolean ok = npl > 0 && ies.n>2;
        System.out.println(codies + "\t" + ies.n + "\t" + npl + "\t" + (ok?"":"X"));
        if (!ok) continue;
        if (nomprog.length <= 100) { 
          calcularEstadisticas(filtroVacio,diferenciados);
        } else {
          LinkedList<Item> items= new LinkedList<Item>();
          for (int i = 0, it = (int)Math.floor(npl/100.0);i<it;i++) {
            items.clear();
            for (String s:Arrays.copyOfRange(nomprog, 100*i, 100*i + 100)) if (s!=null) {
              items.add(new Item(s, s, s));
            }
            calcularEstadisticas(new Filtro[]{new Filtro(Variable.PROGRAMA_EST,items.toArray(new Item[0]))},diferenciados);
          }
        }
      }
    }
    ps_dc.close();
    ps_dp.close();
    ps_d3.close();
    ps_pr.close();
  }
  private static void calcularEstadisticas(Filtro[] filtro,
    Variable[] diferenciados) throws MyException {
    {
      Object[] resultado = kernel.getDesercionPorCohorte(filtro,diferenciados);
      Map<byte[],double[][]> mvals = (Map<byte[], double[][]>) resultado[1],
          mvalsC = (Map<byte[], double[][]>) resultado[0],
          mvalsCna = (Map<byte[], double[][]>) resultado[2];
      Integer[] codigosIESDif=(Integer[])(resultado[4]);
      String[] codigosProgramasDif=(String[])(resultado[5]);
      int limsSems[]=(int[])(resultado[3]),limInf=limsSems[0],limSup=limsSems[1],tam=limSup-limInf+1;
      String sems[]=CajaDeHerramientas.textoSemestresToString(CajaDeHerramientas.getTextosSemestresEntre((byte)limInf,(byte)limSup));
      int ntam = 0;
      for (double[][] ser:mvalsC.values()) ntam+=ser.length;
      String encCols[]=new String[tam];
      String encFils[]=new String[]{"Cohorte"};
      for (Object foo:mvalsC.keySet()) encFils = CajaDeHerramientas.concatenarArreglos(String.class,encFils,sems);
      String encFilas[][]=new String[diferenciados.length+1][ntam+1];
      encFilas[0] = encFils;
      String tabla[][]=new String[ntam][tam];
      String tablaC[][]=new String[ntam][tam];
      String tablaCna[][]=new String[ntam][tam];
      DecimalFormat dfC=new DecimalFormat("0");
      int fil = 0;
      for (int j=0,tj=diferenciados.length;j<tj;j++) encFilas[j+1][0] = diferenciados[j].nombre;
      for (byte[] llave:mvalsC.keySet()) {
        double[][] vals = mvals.get(llave);
        double[][] valsC = mvalsC.get(llave);
        double[][] valsCna = mvalsCna.get(llave);
        for (int j=0; j<tam; j++,fil++) {
          for (int k=0,tk=diferenciados.length;k<tk;k++)
            encFilas[k+1][fil+1] = Variable.toString(diferenciados[k],llave[k],codigosIESDif,codigosProgramasDif);
          for (int i=0; i<tam; i++) {
            if (valsC[j][i]==Double.MAX_VALUE)
              tabla[fil][i]=tablaC[j][i]="";
            else {
              tabla[fil][i]=RutinasGUI.df_porcentaje.format(vals[j][i]/100);
              tablaC[fil][i]=dfC.format(valsC[j][i]);
              tablaCna[fil][i]=dfC.format(valsCna[j][i]);
            }
          }
        }
      }
      for (int i=0; i<tam; i++) encCols[i]=""+(i+1);
      for (int j=0; j<tabla.length; j++)
        ps_dc.println(kernel.listaIES[0].codigo + ";" +
           encFilas[0][j+1] + ";" + encFilas[1][j+1] + ";" +
           CajaDeHerramientas.stringToCSV(tabla[j]));
    }
    {
      Object[] resultado = kernel.getTasaDesercion(filtro,diferenciados);
      Map<byte[],int[]> resM=(Map<byte[], int[]>) resultado[0],
          resD=(Map<byte[], int[]>) resultado[1];
      Map<byte[],double[]> resT=(Map<byte[], double[]>) resultado[2];
      Integer[] codigosIESDif=(Integer[])(resultado[3]);
      String[] codigosProgramasDif=(String[])(resultado[4]);
      int tam = 0;
      for (int[] arrM:resM.values()) tam+= arrM.length;
      int limInf = (Integer) resultado[6]; 
      String encFilas[][]=new String[1+diferenciados.length][tam+1],encColumnas[]=new String[]{"NO graduados","Desertores","Deserción", "Retención"};
      String[][] valores=new String[tam][4];
      encFilas[0][0]="Periodo";
      for (int i=0,t=diferenciados.length;i<t;i++) encFilas[i+1][0]=diferenciados[i].nombre; 
      int fi = 0;
      for (byte[] llave:resM.keySet()) {
        int [] matr = resM.get(llave), des = resD.get(llave);
        double [] tasa = resT.get(llave);
        for (int i=0, t=matr.length;i<t;i++) {
          encFilas[0][fi+1] = CajaDeHerramientas.codigoSemestreToString((byte) (limInf+i));
          for (int j=0,tj=diferenciados.length;j<tj;j++)
            encFilas[j+1][fi+1] = Variable.toString(diferenciados[j],llave[j],codigosIESDif,codigosProgramasDif);
          if (tasa[i]==0)
            valores[fi] = new String[]{String.valueOf(matr[i]), String.valueOf(des[i]),"",""};
          else
            valores[fi] = new String[]{String.valueOf(matr[i]), String.valueOf(des[i]),RutinasGUI.df_porcentaje.format(tasa[i]),RutinasGUI.df_porcentaje.format(1d-tasa[i])};
          fi++;
        }
      }
      for (int j=0; j<valores.length; j++) {
        ps_dp.println(kernel.listaIES[0].codigo + ";" +
           encFilas[0][j+1] + ";" + encFilas[1][j+1] + ";" +
           CajaDeHerramientas.stringToCSV(valores[j]));
      }
    }
    {
      Object[] resultado = kernel.getDesercion(filtro,diferenciados);
      if (resultado!=null) {
        Map<byte[],int[]> resC=(Map<byte[],int[]>)(resultado[0]);
        Map<byte[],double[]> resP=(Map<byte[],double[]>)(resultado[1]);
        Integer[] codigosIESDif=(Integer[])(resultado[2]);
        String[] codigosProgramasDif=(String[])(resultado[3]);
        int tam=(Integer)(resultado[4]);
        ChartPanel graf=FabricaGraficas.crearGraficaConteosPoblacion("",diferenciados,new Object[]{resC,codigosIESDif,codigosProgramasDif},false);
        ChartPanel grafPorc=FabricaGraficas.crearGraficaConteosPoblacion("",diferenciados,new Object[]{resP,codigosIESDif,codigosProgramasDif},true);
        int t=diferenciados.length,m=resC.size();
        String encFilas[][]=new String[t][m+1],encColumnas[]=new String[tam];
        for (int i=0; i<t; i++) encFilas[i][0]=diferenciados[i].nombre;
        {
          int ind=1;
          for (byte[] llave:resC.keySet()) {
            for (int i=0; i<t; i++) encFilas[i][ind]=Variable.toString(diferenciados[i],llave[i],codigosIESDif,codigosProgramasDif);
            ind++;
          }
        }
        for (int j=0; j<tam; j++) encColumnas[j]=""+(j+1);
        String[][] valores=new String[m][tam],valoresPorc=new String[m][tam];
        {
          int ind=0;
          for (Map.Entry<byte[],int[]> eC:resC.entrySet()) {
            int[] contC=eC.getValue();
            double[] contP=resP.get(eC.getKey());
            int tam2=tam;
            while (tam2>0 && contC[tam2-1]==0) tam2--;
            for (int i=0; i<tam2; i++) {
              valores[ind][i]=""+contC[i];
              valoresPorc[ind][i]=(contP==null)?"":(RutinasGUI.df_porcentaje.format(contP[i]));
            }
            for (int i=tam2; i<tam; i++) valores[ind][i]=valoresPorc[ind][i]="";
            ind++;
          }
        }
        for (int j=0; j<valoresPorc.length; j++)
          ps_d3.println(kernel.listaIES[0].codigo + ";" +
             encFilas[0][j+1] + ";" +
             CajaDeHerramientas.stringToCSV(valoresPorc[j]));
      }
    }

    //Object[] res = kernel.getDesercionIntersemestral(new Filtro[]{new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")})}, new Variable[]{Variable.AREA_CONOCIMIENTO_EST});
    
    /*
    1.       Deserción por cohorte
    getDesercionPorCohorte(filtros,diferenciados);
    2.       Deserción por periodo
    getTasaDesercion(filtros,diferenciados);
    3.       Deserción cohorte 3
    .getDesercion(filtros,diferenciados);
    */
  }
}