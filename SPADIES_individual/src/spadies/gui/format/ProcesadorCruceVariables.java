package spadies.gui.format;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import spadies.gui.util.InfoTabla;
import spadies.kernel.KernelSPADIES;
import spadies.util.CajaDeHerramientas;
import spadies.util.MyException;
import spadies.util.PuertaAlServidorDeConsultas;
import spadies.util.variables.Filtro;
import spadies.util.variables.Variable;

public class ProcesadorCruceVariables extends AbstractProcesador {
  public static final ProcesadorCruceVariables instance = new ProcesadorCruceVariables();
  public Collection<ResultadoConsulta> generarGrafica() throws Exception {
    Collection<ResultadoConsulta> res = new LinkedList<ResultadoConsulta>();
    Object[] resultado=obtenerResultado(alMinisterio,diferenciados,filtros);
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
      encFilas[0][i+1]=diferenciados[0].nombre;
      encFilas[1][i+1]=Variable.toString(diferenciados[0],it.next(),codigosIESDif,codigosProgramasDif);
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
        valoresPX[fil][col]=CajaDeHerramientas.df_porcentaje.format(cont/totX[fil]);
        valoresPY[fil][col]=CajaDeHerramientas.df_porcentaje.format(cont/totY[col]);
        valoresPT[fil][col]=CajaDeHerramientas.df_porcentaje.format(cont/tot);
      }
    }
    /*
    String[][] valores=new String[m][tam],valoresPorc=new String[m][tam];
    {
      int ind=0;
      for (Map.Entry<byte[],int[]> eC:resC.entrySet()) {
        int[] contC=eC.getValue();
        //System.out.println("_"+eC.getKey().length);
        //double[] contP=resP.get(eC.getKey());
        int tam2=tam;
        while (tam2>0 && contC[tam2-1]==0) tam2--;
        for (int i=0; i<tam2; i++) {
          //valores[ind][i]=""+contC[i];
          valores[ind][i]=""+contC[0];
          //valoresPorc[ind][i]=(contP==null)?"":(df.format(contP[i]));
        }
        for (int i=tam2; i<tam; i++) valores[ind][i]=valoresPorc[ind][i]="";
        ind++;
      }
    }
    */
    //InfoTabla tabla=new InfoTabla(valores,encFilas,encColumnas),tablaPorc=new InfoTabla(valoresPorc,encFilas,encColumnas);
    InfoTabla tabla=new InfoTabla(valores,encFilas,encColumnas),
      tablaPX=new InfoTabla(valoresPX,encFilas,encColumnas),
      tablaPY=new InfoTabla(valoresPY,encFilas,encColumnas),
      tablaPT=new InfoTabla(valoresPT,encFilas,encColumnas);
    //VentanaRealizarConsultaGrafica.this.dispose();
    /*new VentanaTablas(VentanaPrincipal.getInstance(),"Resultado del cruce",new MyEditorPane(false,getStringSeleccion()),
        new String[]{"Conteo", "Porcentaje filas", "Porcentaje columnas", "Porcentaje total"},
        new InfoTabla[]{tabla, tablaPX, tablaPY, tablaPT}).setVisible(true);*/
    res.add(new ResultadoConsulta("Tabla PX",tablaPX));
    res.add(new ResultadoConsulta("Tabla PY",tablaPY));
    res.add(new ResultadoConsulta("Tabla PT",tablaPT));
    res.add(new ResultadoConsulta("Tabla",tabla));
    System.out.println(res);
    return res;
  }

  public static Object[] obtenerResultado(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) throws MyException {
    if (diferenciados.length!=2) throw new MyException("Para el cruce de variables debe escoger unicamente dos variables de diferenciación.");
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=KernelSPADIES.getInstance().getCruceVariables(filtros,diferenciados);
    }
    else {      
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(8,Object[].class,filtros,diferenciados);
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
}
