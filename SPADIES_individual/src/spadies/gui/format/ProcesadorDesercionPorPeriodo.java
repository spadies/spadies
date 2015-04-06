package spadies.gui.format;


import static spadies.util.CajaDeHerramientas.df_entero;
import static spadies.util.CajaDeHerramientas.df_porcentaje;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import spadies.gui.graficas.FabricaGraficas;
import spadies.gui.graficas.FiltroDouble;
import spadies.gui.util.InfoTabla;
import spadies.kernel.KernelSPADIES;
import spadies.util.CajaDeHerramientas;
import spadies.util.MyException;
import spadies.util.PuertaAlServidorDeConsultas;
import spadies.util.variables.Filtro;
import spadies.util.variables.Variable;

public class ProcesadorDesercionPorPeriodo extends AbstractProcesador {
  public final static ProcesadorDesercionPorPeriodo instance = new ProcesadorDesercionPorPeriodo();
  public Collection<ResultadoConsulta> generarGrafica() throws Exception {
    Collection<ResultadoConsulta> res = new LinkedList<ResultadoConsulta>();
    Object[] resultado=obtenerResultado(alMinisterio,diferenciados,filtros);
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
          valores[fi] = new String[]{String.valueOf(matr[i]), String.valueOf(des[i]),df_porcentaje.format(tasa[i]),df_porcentaje.format(1d-tasa[i])};
        fi++;
      }
    }
    ChartPanel grafPorc=FabricaGraficas.crearGraficaConteosPoblacion("",diferenciados,new Object[]{resT,codigosIESDif,codigosProgramasDif},true,true, 0xFF&limInf,new FiltroDouble() {
      public boolean acepta(double x) {
        return x>=2006;
      }
    });
    InfoTabla tabla=new InfoTabla(valores,encFilas,encColumnas);
    //JPanel panel=new MyBorderPane(false,0,0,0,0,null,null,new MyPanelTabla(tabla,-1,-1,65),null,null);
    //VentanaRealizarConsultaGrafica.this.dispose();
    //new VentanaGrafica(VentanaPrincipal.getInstance(),"Resultado de la consulta",graf,grafPorc,tabla,tablaPorc,new MyEditorPane(false,getStringSeleccion())).setVisible(true);
    res.add(new ResultadoConsulta("Gráfica Deserción",grafPorc));    
    res.add(new ResultadoConsulta("Tabla Deserción",tabla));
    //hasta aca es desercion por periodo
    //comienza ausencia intersemestral
    
    resultado=obtenerResultado2(alMinisterio,diferenciados,filtros);
    byte limInf2 = (Byte) resultado[5];
    byte limSup = (Byte) resultado[6]; 
    Map<byte[],int[]> resC=(Map<byte[],int[]>)(resultado[0]);
    Map<byte[],double[]> resP=(Map<byte[],double[]>)(resultado[1]);
    codigosIESDif=(Integer[])(resultado[2]);
    codigosProgramasDif=(String[])(resultado[3]);
    tam=(Integer)(resultado[4]);
    ChartPanel graf=FabricaGraficas.crearGraficaConteosPoblacion("",diferenciados,new Object[]{resC,codigosIESDif,codigosProgramasDif},false,true, 0xFF&limInf2);
    grafPorc=FabricaGraficas.crearGraficaConteosPoblacion("",diferenciados,new Object[]{resP,codigosIESDif,codigosProgramasDif},true,true, 0xFF&limInf2);
    int t=diferenciados.length,m=resC.size();
    String encFilas2[][]=new String[t][m+1],encColumnas2[]=new String[tam];
    for (int i=0; i<t; i++) encFilas2[i][0]=diferenciados[i].nombre;
    {
      int ind=1;
      for (byte[] llave:resC.keySet()) {
        for (int i=0; i<t; i++) encFilas2[i][ind]=Variable.toString(diferenciados[i],llave[i],codigosIESDif,codigosProgramasDif);
        ind++;
      }
    }
    {
      String[] encsColsB = CajaDeHerramientas.getTextosSemestresEntre(limInf2, limSup);
      for (int j=0; j<tam; j++) encColumnas2[j]= encsColsB[j];//""+(j+1);
    }
    String[][] valores2=new String[m][tam],valoresPorc=new String[m][tam];
    {
      int ind=0;
      for (Map.Entry<byte[],int[]> eC:resC.entrySet()) {
        int[] contC=eC.getValue();
        double[] contP=resP.get(eC.getKey());
        int tam2=tam;
        while (tam2>0 && contC[tam2-1]==0) tam2--;
        for (int i=0; i<tam2; i++) {
          valores2[ind][i]=df_entero.format(contC[i]);
          valoresPorc[ind][i]=(contP==null)?"":(df_porcentaje.format(contP[i]));
        }
        for (int i=tam2; i<tam; i++) valores2[ind][i]=valoresPorc[ind][i]="";
        ind++;
      }
    }
    tabla=new InfoTabla(valores2,encFilas2,encColumnas2);
    InfoTabla tablaPorc=new InfoTabla(valoresPorc,encFilas2,encColumnas2);
    
    //res.add(new ResultadoConsulta("Grafica Ausencia Intersemestral",graf));
    res.add(new ResultadoConsulta("Gráfica Ausencia",grafPorc));
    //res.add(new ResultadoConsulta("Tabla datos Ausencia Intersemestral",tabla));
    res.add(new ResultadoConsulta("Datos Ausencia",tablaPorc));

    
    return res;
  }

  public static Object[] obtenerResultado(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      //resultado=KernelSPADIES.getInstance().getConteoPoblacion(filtros,diferenciados);
      resultado=KernelSPADIES.getInstance().getTasaDesercion(filtros,diferenciados);
    }
    else {
      // Variable.PROGRAMA_EST no debe estar ni en 'diferenciados' ni en 'filtros'
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(14,Object[].class,filtros,diferenciados);
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
  
  //metodo para ausencia intersemestral
  public static Object[] obtenerResultado2(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      //resultado=KernelSPADIES.getInstance().getConteoPoblacion(filtros,diferenciados);
      resultado=KernelSPADIES.getInstance().getDesercionIntersemestral(filtros,diferenciados);
    }
    else {
      // Variable.PROGRAMA_EST no debe estar ni en 'diferenciados' ni en 'filtros'
      //resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(1,Object[].class,filtros,diferenciados);
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(9,Object[].class,filtros,diferenciados);
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }

  /*public static String resultadoToJSON(Collection<ResultadoConsulta> resc) {
    StringBuilder res = new StringBuilder();
    boolean p = true;
    double rand = Math.random();
    int congra = 0;
    res.append("[\n");
    for (ResultadoConsulta resu:resc) {
      if (!p)
        res.append(",\n");
      else
        p = false;
      res.append("{\n");
      res.append("\"nombre\":\""+resu.nombre+"\",\n");
      if (resu.resultado.getClass()==InfoTabla.class) {
        InfoTabla it = (InfoTabla) resu.resultado;
        res.append("\"tipo\":\"t\",\n");
        res.append("\"valores\":"+it.toJSON()+"\n");
      } else if (resu.resultado.getClass()==ChartPanel.class) {
        ChartPanel cp = (ChartPanel) resu.resultado;
        //TODO quitar el nivel extram un JFreeChart sin su ChartPanel
        JFreeChart chart = cp.getChart();
        File f = new File("img_"+(congra++)+""+rand+".jpg");
        try {
          ImageIO.write(imagen(chart, 900, 600),"JPEG",f);
        } catch (IOException e) {
          e.printStackTrace();
        }
        res.append("\"tipo\":\"g\",\n");
        res.append("\"url\":\""+f.getName()+"\"\n");
      }
      res.append("}\n");
    }
    res.append("]\n");
    return res.toString();
  }*/
}
