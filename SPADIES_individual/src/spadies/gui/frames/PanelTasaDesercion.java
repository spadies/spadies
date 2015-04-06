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
package spadies.gui.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jfree.chart.ChartPanel;

import spadies.gui.SVariablesPanel;
import spadies.gui.graficas.FabricaGraficas;
import spadies.gui.util.InfoTabla;
import spadies.gui.util.MyBorderPane;
import spadies.gui.util.MyDialogProgreso;
import spadies.gui.util.MyEditorPane;
import spadies.gui.util.MyPanelSeleccion;
import spadies.gui.util.MyPanelTabla;
import spadies.gui.util.MyScrollPane;
import spadies.gui.util.RutinasGUI;
import spadies.gui.util.RutinasGUI.CategorizacionVariables;
import spadies.kernel.KernelSPADIES;
import spadies.util.CajaDeHerramientas;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.PuertaAlServidorDeConsultas;
import spadies.util.variables.Filtro;
import spadies.util.variables.SFiltro;
import spadies.util.variables.SVariable;
import spadies.util.variables.Variable;

@SuppressWarnings("serial")
public class PanelTasaDesercion extends JPanel implements ActionListener {
  private static final CategorizacionVariables cvar = RutinasGUI.categorizacionVariables(false);
  private static final Variable[] criterios = cvar.criterios;
  private final int tam=criterios.length,tamNormal=tam-cvar.textras;
  private final MyPanelSeleccion[] panelsSeleccion=new MyPanelSeleccion[tam];
  private final JCheckBox[] checksDiferenciacion=new JCheckBox[tam];
  private final MyEditorPane labelSeleccion=new MyEditorPane(false);
  //private final JTabbedPane panelVariables=new JTabbedPane(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);
  //private final JComponent[] tabsExtras=new JComponent[5];
  private SVariablesPanel selector=new SVariablesPanel();
  private final JPanel panelVariables = new JPanel(new GridLayout());
  private final JButton[] botones=new JButton[2];
  private boolean cuadrando=false;
  public PanelTasaDesercion() {
    for (int i=0; i<tam; i++) {
      panelsSeleccion[i]=new MyPanelSeleccion(criterios[i].items,true,280,200);
      {
        JCheckBox cb=new JCheckBox("Diferenciado?",false);
        checksDiferenciacion[i]=cb;
        cb.setFont(cb.getFont().deriveFont(Font.BOLD));
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width,20));
      }
    }
    
    panelVariables.add(selector);
    panelVariables.add(selector.panelAuxiliar);
    
    botones[0]=new JButton("Restablecer selección");
    botones[1]=new JButton("Generar tablas");
    for (JButton b:botones) {
      RutinasGUI.configurarBoton(b,new Color(245,245,255),16,-1,23);
      b.addActionListener(this);
    }
    TableModelListener tml=new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        actualizarLabelSeleccion();
      }
    };
    ActionListener al=new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actualizarLabelSeleccion();
      }
    };
    for (int i=0; i<tam; i++) panelsSeleccion[i].getTabla().getModel().addTableModelListener(tml);
    for (int i=0; i<tam; i++) checksDiferenciacion[i].addActionListener(al);
    JPanel panelBotones=new JPanel(new GridLayout(1,2));
    for (JButton b:botones) panelBotones.add(b);
    setLayout(new BorderLayout());
    add(new MyBorderPane(false,0,0,0,0,null,panelVariables,new MyScrollPane(labelSeleccion,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,-1,-1),null,null),BorderLayout.CENTER);
    add(panelBotones,BorderLayout.SOUTH);
  }
  public void actualizar() {
    cuadrando=true;
    for (int i=0; i<tam; i++) {
      Variable v=criterios[i];
      if (Constantes.variablesActualizables.contains(v)) {
        panelsSeleccion[i].setValoresManteniendoSeleccion(v.items,true);
      }
      else if (i>=tamNormal) {
        panelsSeleccion[i].setValores(v.items,true);
      }
    }
    cuadrando=false;
    actualizarLabelSeleccion();
  }
  private void actualizarLabelSeleccion() {
    if (!cuadrando) labelSeleccion.setText(getStringSeleccion());
  }
  private Object[] getComponentesParametros() {
    VentanaPrincipal vp=VentanaPrincipal.getInstance();
    MyPanelSeleccion[] arrPS=CajaDeHerramientas.concatenarArreglos(MyPanelSeleccion.class,vp.getPanelsSeleccion(),panelsSeleccion);
    JCheckBox[] arrCB=CajaDeHerramientas.concatenarArreglos(JCheckBox.class,vp.getChecksDiferenciacion(),checksDiferenciacion);
    Variable[] arrVE=CajaDeHerramientas.concatenarArreglos(Variable.class,vp.getCriterios(),criterios);
    return new Object[]{arrPS,arrCB,arrVE};
  }
  private String getStringSeleccion() {
    Object[] params=getComponentesParametros();
    return RutinasGUI.getStringSeleccion((MyPanelSeleccion[])(params[0]),(JCheckBox[])(params[1]),(Variable[])(params[2]),null,VentanaPrincipal.getInstance().estaSeleccionandoAgregado());
  }
  public void actionPerformed(ActionEvent e) {
    int ind=CajaDeHerramientas.searchSame(botones,e.getSource());
    if (ind==0) {
    	selector.limpiarSeleccion();
      actualizarLabelSeleccion();
    }
    else if (ind==1) {
      Component cmDespl=this;
      try {
        boolean ag=VentanaPrincipal.getInstance().estaSeleccionandoAgregado();
        Object[] params=getComponentesParametros();
        MyPanelSeleccion[] arrPS=(MyPanelSeleccion[])(params[0]);
        JCheckBox[] arrCB=(JCheckBox[])(params[1]);
        Variable[] arrVE=(Variable[])(params[2]);
        EnumSet<Variable> mDiferenciados=EnumSet.noneOf(Variable.class);
        EnumMap<Variable,Filtro> mFiltros=new EnumMap<Variable,Filtro>(Variable.class);
        RutinasGUI.getSeleccion(arrVE,arrCB,arrPS,mDiferenciados,mFiltros);
        if (ag) {
          mDiferenciados.remove(Variable.PROGRAMA_EST);
          mFiltros.remove(Variable.PROGRAMA_EST);
        }
        Variable[] diferenciados=mDiferenciados.toArray(new Variable[0]);
        Filtro[] filtros=mFiltros.values().toArray(new Filtro[0]);
        new VentanaRealizarConsultaGrafica(ag,diferenciados,filtros).ejecutar();
      }
      catch (MyException ex) {
        RutinasGUI.desplegarError(cmDespl,"<html>"+CajaDeHerramientas.stringToHTML(ex.getMessage())+"</html>");
      }
      catch (OutOfMemoryError err) {
        RutinasGUI.desplegarError(cmDespl,"<html>Memoria RAM insuficiente para ejecutar el proceso.</html>");
      }
      catch (Throwable th) {
        th.printStackTrace();
        RutinasGUI.desplegarError(cmDespl,"<html>Hubo un error realizando la consulta.</html>");
      }
    }
  }
  private class VentanaRealizarConsultaGrafica extends MyDialogProgreso {
    private final boolean alMinisterio;
    public VentanaRealizarConsultaGrafica(boolean pAlMinisterio, Variable[] pDiferenciados, Filtro[] pFiltros) {
      super(VentanaPrincipal.getInstance(),"Realizando la consulta"+(pAlMinisterio?" en el Ministerio de Educación Nacional":""),false);
      alMinisterio=pAlMinisterio;
    }
    public void ejecutar() {
      setVisible(true);
      new Thread() {
        public void run() {
          Window window=VentanaRealizarConsultaGrafica.this;
          try {
            generarGrafica();
          }
          catch (MyException ex) {
            RutinasGUI.desplegarError(window,"<html>"+CajaDeHerramientas.stringToHTML(ex.getMessage())+"</html>");
            window.dispose();
          }
          catch (OutOfMemoryError err) {
            RutinasGUI.desplegarError(window,"<html>Memoria RAM insuficiente para ejecutar el proceso.</html>");
            window.dispose();
          }
          catch (Throwable th) {
            th.printStackTrace();
            RutinasGUI.desplegarError(window,"<html>Hubo un error realizando la consulta.</html>");
            window.dispose();
          }
        }
      }.start();
    }
    @SuppressWarnings("unchecked")
    private void generarGrafica() throws Exception {
    SVariable[] diferenciados=selector.getDiferenciados();
    SFiltro[] filtros=selector.getFiltros();
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
            encFilas[j+1][fi+1] = SVariable.toString(diferenciados[j],llave[j]);
          if (tasa[i]==0)
            valores[fi] = new String[]{String.valueOf(matr[i]), String.valueOf(des[i]),"",""};
          else
            valores[fi] = new String[]{String.valueOf(matr[i]), String.valueOf(des[i]),RutinasGUI.df_porcentaje.format(tasa[i]),RutinasGUI.df_porcentaje.format(1d-tasa[i])};
          fi++;
        }
      }
      ChartPanel grafPorc=FabricaGraficas.crearGraficaConteosPoblacion("",diferenciados,new Object[]{resT,codigosIESDif,codigosProgramasDif},true,true, 0xFF&limInf);
      InfoTabla tabla=new InfoTabla(valores,encFilas,encColumnas);
      JPanel panel=new MyBorderPane(false,0,0,0,0,null,null,new MyPanelTabla(tabla,-1,-1,65),null,null);
      new VentanaResultadoPanel(VentanaPrincipal.getInstance(),new String[]{"Tasa de deserción","Gráfica tasa"},new JPanel[]{panel,grafPorc},new MyEditorPane(false,getStringSeleccion())).setVisible(true);
      VentanaRealizarConsultaGrafica.this.dispose();
    }
  }
  public static Object[] obtenerResultado(boolean alMinisterio, SVariable[] diferenciados, SFiltro[] filtros) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=KernelSPADIES.getInstance().getTasaDesercion(filtros,diferenciados);
    }
    else {
      // Variable.PROGRAMA_EST no debe estar ni en 'diferenciados' ni en 'filtros'
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(14,Object[].class,filtros,diferenciados);
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
}
