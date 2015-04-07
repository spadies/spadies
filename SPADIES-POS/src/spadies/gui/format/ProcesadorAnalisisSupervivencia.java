package spadies.gui.format;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.jfree.chart.ChartPanel;

import spadies.gui.frames.VentanaGrafica;
import spadies.gui.frames.VentanaPrincipal;
import spadies.gui.graficas.FabricaGraficas;
import spadies.gui.util.InfoTabla;
import spadies.gui.util.MyEditorPane;
import spadies.kernel.KernelSPADIES;
import spadies.util.CajaDeHerramientas;
import spadies.util.MyException;
import spadies.util.PuertaAlServidorDeConsultas;
import spadies.util.variables.Filtro;
import spadies.util.variables.Variable;

public class ProcesadorAnalisisSupervivencia extends AbstractProcesador {
  public static final ProcesadorAnalisisSupervivencia instance = new ProcesadorAnalisisSupervivencia(); 
  @Override
  public Collection<ResultadoConsulta> generarGrafica() throws Exception {
    Object[] resultado=obtenerResultado(alMinisterio,diferenciados,filtros);
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
          valoresPorc[ind][i]=(contP==null)?"":(CajaDeHerramientas.df_porcentaje.format(contP[i]));
        }
        for (int i=tam2; i<tam; i++) valores[ind][i]=valoresPorc[ind][i]="";
        ind++;
      }
    }
    InfoTabla tabla=new InfoTabla(valores,encFilas,encColumnas),tablaPorc=new InfoTabla(valoresPorc,encFilas,encColumnas);
    Collection<ResultadoConsulta> resp = new LinkedList<ResultadoConsulta>();
    resp.add(new ResultadoConsulta("Grafica", graf));
    resp.add(new ResultadoConsulta("Grafica porcentaje", grafPorc));
    resp.add(new ResultadoConsulta("Tabla", tabla));
    resp.add(new ResultadoConsulta("Tabla porcentaje", tablaPorc));
    //new VentanaGrafica(VentanaPrincipal.getInstance(),"Resultado de la consulta",graf,grafPorc,tabla,tablaPorc,new MyEditorPane(false,getStringSeleccion())).setVisible(true);
    return resp;
  }
  public static Object[] obtenerResultado(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=KernelSPADIES.getInstance().getConteoPoblacion(filtros,diferenciados);
    }
    else {
      // Variable.PROGRAMA_EST no debe estar ni en 'diferenciados' ni en 'filtros'
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(1,Object[].class,filtros,diferenciados);
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
}
