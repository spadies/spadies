package spadies.gui.format;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.jfree.chart.ChartPanel;

import spadies.gui.graficas.FabricaGraficas;
import spadies.gui.graficas.FiltroDouble;
import spadies.gui.util.InfoTabla;
import spadies.kernel.KernelSPADIES;
import spadies.util.CajaDeHerramientas;
import spadies.util.MyException;
import spadies.util.PuertaAlServidorDeConsultas;
import spadies.util.variables.Filtro;
import spadies.util.variables.Variable;

public abstract class ProcesadorConsultaGeneral extends AbstractProcesador {
  public static final ProcesadorConsultaDesercion instance = new ProcesadorConsultaDesercion();
  public Collection<ResultadoConsulta> generarGrafica() throws Exception {
    Collection<ResultadoConsulta> res = new LinkedList<ResultadoConsulta>();
    Object[] resultado=obtenerResultado(alMinisterio,diferenciados,filtros);
    Map<byte[],int[]> resC=(Map<byte[],int[]>)(resultado[0]);
    Map<byte[],double[]> resP=(Map<byte[],double[]>)(resultado[1]);
    Integer[] codigosIESDif=(Integer[])(resultado[2]);
    String[] codigosProgramasDif=(String[])(resultado[3]);
    int tam=(Integer)(resultado[4]);
    //ChartPanel graf=FabricaGraficas.crearGraficaConteosPoblacion("",diferenciados,new Object[]{resC,codigosIESDif,codigosProgramasDif},false,FabricaGraficas.filtroVacio);
    String[] titulos = getTitulos();
    ChartPanel grafPorc = titulos==null?
      FabricaGraficas.crearGraficaConteosPoblacion("",diferenciados,new Object[]{resP,codigosIESDif,codigosProgramasDif},true,getFiltroRango()):
      FabricaGraficas.crearGraficaConteosPoblacion("",titulos[0],titulos[1],diferenciados,new Object[]{resP,codigosIESDif,codigosProgramasDif},true,false,0,getFiltroRango());
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
    //VentanaRealizarConsultaGrafica.this.dispose();
    //new VentanaGrafica(VentanaPrincipal.getInstance(),"Resultado de la consulta",graf,grafPorc,tabla,tablaPorc,new MyEditorPane(false,getStringSeleccion())).setVisible(true);
    res.add(new ResultadoConsulta("Gráfica porcentajes",grafPorc));
    //res.add(new ResultadoConsulta("Grafica conteo",graf));
    res.add(new ResultadoConsulta("Tabla porcentajes",tablaPorc));
    //res.add(new ResultadoConsulta("Tabla conteo",tabla));
    return res;
  }
  public abstract Object[] obtenerResultado(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) throws MyException;
  public abstract FiltroDouble getFiltroRango();
  protected String[] getTitulos() {
    return null;
  }
}