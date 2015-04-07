package spadies.gui.format;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JPanel;

import spadies.gui.frames.VentanaPrincipal;
import spadies.gui.frames.VentanaResultadoPanel;
import spadies.gui.util.InfoTabla;
import spadies.gui.util.MyBorderPane;
import spadies.gui.util.MyEditorPane;
import spadies.gui.util.MyPanelTabla;
import spadies.gui.util.RutinasGUI;
import spadies.kernel.KernelSPADIES;
import spadies.util.CajaDeHerramientas;
import spadies.util.MyException;
import spadies.util.PuertaAlServidorDeConsultas;
import spadies.util.variables.Filtro;
import spadies.util.variables.Variable;

public class ProcesadorDesercionCohorte extends AbstractProcesador {
  public static final ProcesadorDesercionCohorte instance = new ProcesadorDesercionCohorte(); 

  @Override
  public Collection<ResultadoConsulta> generarGrafica() throws Exception {
    Object[] resultado=obtenerResultado(alMinisterio,diferenciados,filtros);
    Map<byte[],double[][]> mvals = (Map<byte[], double[][]>) resultado[1],
      mvalsC = (Map<byte[], double[][]>) resultado[0],
      mvalsCna = (Map<byte[], double[][]>) resultado[2];
    Integer[] codigosIESDif=(Integer[])(resultado[4]);
    String[] codigosProgramasDif=(String[])(resultado[5]);
    //double[][] vals=(double[][])(resultado[1]);
    //double[][] valsC=(double[][])(resultado[0]);
    //double[][] valsCna=(double[][])(resultado[2]);
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
        /*
        tabla[j][i]=(vals[j][i]==Double.MAX_VALUE)?"":(df.format(vals[j][i])+"%");
        tablaC[j][i]=(valsC[j][i]==Double.MAX_VALUE)?"":valsC[j][i]+"";
        */
      }
    }
    for (int i=0; i<tam; i++) encCols[i]=""+(i+1);
    //JPanel panel=new MyBorderPane(false,0,0,0,0,null,null,new MyPanelTabla(new InfoTabla(tabla,encFilas,encCols),-1,-1,65),null,null);
    //JPanel panelC=new MyBorderPane(false,0,0,0,0,null,null,new MyPanelTabla(new InfoTabla(tablaC,encFilas,encCols),-1,-1,65),null,null);
    //JPanel panelCna=new MyBorderPane(false,0,0,0,0,null,null,new MyPanelTabla(new InfoTabla(tablaCna,encFilas,encCols),-1,-1,65),null,null);
    Collection<ResultadoConsulta> resp = new LinkedList<ResultadoConsulta>();
    resp.add(new ResultadoConsulta("Porcentaje de deserción por cohorte", new InfoTabla(tabla,encFilas,encCols)));
    resp.add(new ResultadoConsulta("Conteo de deserción por cohorte", new InfoTabla(tablaC,encFilas,encCols)));
    resp.add(new ResultadoConsulta("Conteo de deserción por cohorte no acumulado", new InfoTabla(tablaCna,encFilas,encCols)));
    //new VentanaResultadoPanel(VentanaPrincipal.getInstance(),new String[]{"Porcentaje de deserción por cohorte","Conteo de deserción por cohorte","Conteo de deserción por cohorte no acumulado"},new JPanel[]{panel,panelC, panelCna},new MyEditorPane(false,getStringSeleccion())).setVisible(true);
    return resp;
  }
  public static Object[] obtenerResultado(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=KernelSPADIES.getInstance().getDesercionPorCohorte(filtros,diferenciados);
    }
    else {
      // Variable.PROGRAMA_EST no debe estar en en 'filtros'
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(15,Object[].class,filtros,diferenciados);
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
}
