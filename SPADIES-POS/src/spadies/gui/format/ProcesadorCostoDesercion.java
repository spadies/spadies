package spadies.gui.format;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import spadies.gui.frames.VentanaPrincipal;
import spadies.gui.frames.VentanaTablas;
import spadies.gui.util.InfoTabla;
import spadies.gui.util.MyEditorPane;
import spadies.kernel.KernelSPADIES;
import spadies.util.CajaDeHerramientas;
import spadies.util.MyException;
import spadies.util.PuertaAlServidorDeConsultas;
import spadies.util.variables.Filtro;
import spadies.util.variables.Variable;

public class ProcesadorCostoDesercion extends AbstractProcesador {
  public static final ProcesadorCostoDesercion instance = new ProcesadorCostoDesercion();

  @Override
  public Collection<ResultadoConsulta> generarGrafica() throws Exception {
    Object[] resultado=obtenerResultado(alMinisterio,diferenciados,filtros);
    byte limInf = (Byte) resultado[5], limSup = (Byte) resultado[6]; 
    Map<byte[],int[]> resC=(Map<byte[],int[]>)(resultado[0]);
    Map<byte[],double[]> resP=(Map<byte[],double[]>)(resultado[1]);
    Integer[] codigosIESDif=(Integer[])(resultado[2]);
    String[] codigosProgramasDif=(String[])(resultado[3]);
    int tam=(Integer)(resultado[4]);
    //ChartPanel graf=FabricaGraficas.crearGraficaConteosPoblacion("",diferenciados,new Object[]{resC,codigosIESDif,codigosProgramasDif},false,true, 0xFF&limInf);
    //ChartPanel grafPorc=FabricaGraficas.crearGraficaConteosPoblacion("",diferenciados,new Object[]{resP,codigosIESDif,codigosProgramasDif},true,true, 0xFF&limInf);
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
    {
      String[] encsColsB = CajaDeHerramientas.getTextosSemestresEntre(limInf, limSup);
      for (int j=0; j<tam; j++) encColumnas[j]= encsColsB[j];//""+(j+1);
    }
    String[][] valores=new String[m][tam],valoresPorc=new String[m][tam];
    {
      int ind=0;
      for (Map.Entry<byte[],int[]> eC:resC.entrySet()) {
        int[] contC=eC.getValue();
        double[] contP=resP.get(eC.getKey());
        int tam2=tam;
        //while (tam2>0 && contC[tam2-1]==0) tam2--;
        for (int i=0; i<tam2; i++) {
          valores[ind][i]=""+contC[i];
          //valoresPorc[ind][i]=""+contP[i];
          //TODO parche???
          valoresPorc[ind][i]=(contP==null)?"":(new DecimalFormat("###,###,###,###,###,###,###").format(contP[i]));
        }
        //for (int i=tam2; i<tam; i++) valores[ind][i]="";
        //for (int i=tam2; i<tam-1; i++) valoresPorc[ind][i+1]="";
        ind++;
      }
    }
    InfoTabla tabla=new InfoTabla(valores,encFilas,encColumnas),tablaPorc=new InfoTabla(valoresPorc,encFilas,encColumnas);
    Collection<ResultadoConsulta> resp = new LinkedList<ResultadoConsulta>();
    resp.add(new ResultadoConsulta("Ausentes intersemestrales", tabla));
    resp.add(new ResultadoConsulta("Costo deserción", tablaPorc));
    return resp;
  }
  public static Object[] obtenerResultado(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      //resultado=KernelSPADIES.getInstance().getConteoPoblacion(filtros,diferenciados);
      resultado=KernelSPADIES.getInstance().getCostoDesercion(filtros,diferenciados);
    }
    else {
      // Variable.PROGRAMA_EST no debe estar ni en 'diferenciados' ni en 'filtros'
      //resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(1,Object[].class,filtros,diferenciados);
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(12,Object[].class,filtros,diferenciados);
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
}
