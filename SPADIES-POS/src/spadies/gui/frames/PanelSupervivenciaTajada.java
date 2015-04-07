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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeSelectionModel;

import org.jfree.chart.ChartPanel;

import spadies.gui.graficas.FabricaGraficas;
import spadies.gui.util.InfoTabla;
import spadies.gui.util.MyBorderPane;
import spadies.gui.util.MyBoxPane;
import spadies.gui.util.MyDialogProgreso;
import spadies.gui.util.MyEditorPane;
import spadies.gui.util.MyFlowPane;
import spadies.gui.util.MyLabel;
import spadies.gui.util.MyPanelSeleccion;
import spadies.gui.util.MyPanelTabla;
import spadies.gui.util.MyScrollPane;
import spadies.gui.util.OpcionVariable;
import spadies.gui.util.RutinasGUI;
import spadies.gui.util.RutinasGUI.CategorizacionVariables;
import spadies.kernel.KernelSPADIES;
import spadies.util.CajaDeHerramientas;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.PuertaAlServidorDeConsultas;
import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;

@SuppressWarnings("serial")
public class PanelSupervivenciaTajada extends JPanel implements ActionListener {
  private static final CategorizacionVariables cvar = RutinasGUI.categorizacionVariables(false);
  private static final Variable[] criterios = cvar.criterios;
  private static final int lims[][] = cvar.lims;
  private static final String titulos[] = cvar.titulos;
  //private static final DecimalFormat df=new DecimalFormat("0.00%");
  private final int tam=criterios.length,tamNormal=tam-cvar.textras;
  private final MyPanelSeleccion[] panelsSeleccion=new MyPanelSeleccion[tam];
  private final JCheckBox[] checksDiferenciacion=new JCheckBox[tam];
  private final MyEditorPane labelSeleccion=new MyEditorPane(false);
  //private final JTabbedPane panelVariables=new JTabbedPane(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);
  //private final JComponent[] tabsExtras=new JComponent[5];
  private final JPanel panelVariables = new JPanel(new GridLayout());
  private DefaultMutableTreeNode dmt = new DefaultMutableTreeNode(new OpcionVariable("Variables"));
  private DefaultTreeModel dtm = new DefaultTreeModel(dmt);
  private final JTree menuVariables = new JTree(/*dmt*/dtm);
  private final DefaultMutableTreeNode[] nodosExtras=new DefaultMutableTreeNode[cvar.textras];
  private final JButton[] botones=new JButton[2];
  private boolean cuadrando=false;
  private final int posExtra;
  public PanelSupervivenciaTajada() {
    for (int i=0; i<tam; i++) {
      panelsSeleccion[i]=new MyPanelSeleccion(criterios[i].items,true,280,200);
      {
        JCheckBox cb=new JCheckBox("Diferenciado?",false);
        checksDiferenciacion[i]=cb;
        cb.setFont(cb.getFont().deriveFont(Font.BOLD));
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width,20));
      }
    }
    final CardLayout cl = new CardLayout();
    final JPanel panelesSelecciones = new JPanel(cl);
    menuVariables.addTreeSelectionListener(new TreeSelectionListener(){
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode dms = (DefaultMutableTreeNode)menuVariables.getLastSelectedPathComponent();
        if (dms==null) return;
        OpcionVariable opcion = (OpcionVariable) dms.getUserObject();
        int num = opcion.getNumero(); cl.show(panelesSelecciones, num==-1?"":String.valueOf(num));
      }
    });
    menuVariables.addTreeWillExpandListener(new TreeWillExpandListener(){
      public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        if(dmt.getRoot()==event.getPath().getLastPathComponent()) throw new ExpandVetoException(event);
      }
      public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {}
    });
    menuVariables.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    menuVariables.putClientProperty("JTree.lineStyle", false);

    panelesSelecciones.add(new JPanel(),"");
    panelVariables.add(menuVariables);
    panelVariables.add(panelesSelecciones);
    int pos = 0;
    for (int j=0,jT=lims.length; j<jT; j++) {
      if (titulos[j]==null) {
        for (int i=lims[j][0]; i<=lims[j][1]; i++) {
          JComponent comp=new MyBoxPane(BoxLayout.Y_AXIS);
          comp.add(new MyFlowPane(0,0,checksDiferenciacion[i]));
          comp.add(panelsSeleccion[i]);
          panelesSelecciones.add(comp, String.valueOf(i));
          DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(new OpcionVariable(i, criterios[i].nombre));
          if (i<tamNormal) {
            dtm.insertNodeInto(nodo, dmt, pos++);
            //dmt.add(nodo);
          }
          else nodosExtras[i-tamNormal] = nodo;
        }
        continue;
      }
      DefaultMutableTreeNode nOpcion = new DefaultMutableTreeNode(new OpcionVariable(titulos[j]));
      dtm.insertNodeInto(nOpcion, dmt, pos++);
      //dmt.add(nOpcion);
      int pos2 = 0;
      for (int i=lims[j][0]; i<=lims[j][1]; i++) {
        dtm.insertNodeInto(new DefaultMutableTreeNode(new OpcionVariable(i, criterios[i].nombre)), nOpcion, pos2++);
        //nOpcion.add(new DefaultMutableTreeNode(new OpcionVariable(i, criterios[i].nombre)));
        JPanel panelSub = new MyBoxPane(BoxLayout.Y_AXIS);
        //panelSub.add(new MyFlowPane(0,0,new MyLabel(criterios[i].nombre)));
        panelSub.add(new MyFlowPane(0,0,checksDiferenciacion[i]));
        panelSub.add(panelsSeleccion[i]);
        panelesSelecciones.add(panelSub, String.valueOf(i));
      }
    }
    posExtra = pos;
    menuVariables.expandRow(0);
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
    for (DefaultMutableTreeNode nodo:nodosExtras) try {
      dtm.removeNodeFromParent(nodo);
    } catch (IllegalArgumentException e) {}
    int pos = posExtra;
    //for (DefaultMutableTreeNode nodo:nodosExtras) if (dmt.isNodeChild(nodo)) dmt.remove(nodo);
    for (int i=0; i<tam; i++) {
      Variable v=criterios[i];
      if (Constantes.variablesActualizables.contains(v)) {
        panelsSeleccion[i].setValoresManteniendoSeleccion(v.items,true);
      }
      else if (i>=tamNormal) {
        panelsSeleccion[i].setValores(v.items,true);
        ((OpcionVariable)nodosExtras[i-tamNormal].getUserObject()).setNombre(v.nombre);
        if (v.items.length>0) dtm.insertNodeInto(nodosExtras[i-tamNormal], dmt, pos++);
        //if (v.items.length>0) dmt.add(nodosExtras[i-tamNormal]);
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
      cuadrando=true;
      for (int i=0; i<tam; i++) {
        panelsSeleccion[i].seleccionar(true);
        checksDiferenciacion[i].setSelected(false);
      }
      cuadrando=false;
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
        Filtro nSemPer = mFiltros.remove(Variable.NUMERO_SEMESTRE_PER);
        byte[] semPers = new byte[0];
        if (nSemPer==null)
          semPers = new byte[]{1};
        else {
          semPers = new byte[nSemPer.filtro.length];
          for (int i=0,t=nSemPer.filtro.length;i<t;i++)
            semPers[i] = (Byte) nSemPer.filtro[i];
        }
        Variable[] diferenciados=mDiferenciados.toArray(new Variable[0]);
        Filtro[] filtros=mFiltros.values().toArray(new Filtro[0]);
        new VentanaRealizarConsultaGrafica(ag,diferenciados,filtros,semPers).ejecutar();
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
    private final Variable[] diferenciados;
    private final Filtro[] filtros;
    private byte[] semPer;
    public VentanaRealizarConsultaGrafica(boolean pAlMinisterio, Variable[] pDiferenciados, Filtro[] pFiltros, byte[] pSemPer) {
      super(VentanaPrincipal.getInstance(),"Realizando la consulta"+(pAlMinisterio?" en el Ministerio de Educación Nacional":""),false);
      alMinisterio=pAlMinisterio;
      diferenciados=pDiferenciados;
      filtros=pFiltros;
      semPer=pSemPer;
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
      Object[] resultado=obtenerResultado(alMinisterio,diferenciados,filtros);
      Map<byte[],double[][]> mvals = (Map<byte[], double[][]>) resultado[0];
      Integer[] codigosIESDif=(Integer[])(resultado[1]);
      String[] codigosProgramasDif=(String[])(resultado[2]);
      int limsSems[]=(int[])(resultado[3]),limInf=limsSems[0],limSup=limsSems[1],tam=limSup-limInf+1;
      String sems[]=CajaDeHerramientas.textoSemestresToString(CajaDeHerramientas.getTextosSemestresEntre((byte)limInf,(byte)limSup));
      int ntam = 0;
      for (double[][] ser:mvals.values()) ntam+=ser.length;
      String encCols[]=new String[semPer.length];
      String encFils[]=new String[]{"Cohorte"};
      for (Object foo:mvals.keySet()) encFils = CajaDeHerramientas.concatenarArreglos(String.class,encFils,sems);
      String encFilas[][]=new String[diferenciados.length+1][ntam+1];
      encFilas[0] = encFils;
      String tabla[][]=new String[ntam][semPer.length];
      int fil = 0;
      for (int j=0,tj=diferenciados.length;j<tj;j++) encFilas[j+1][0] = diferenciados[j].nombre;
      for (byte[] llave:mvals.keySet()) {
        double[][] vals = mvals.get(llave);
        for (int j=0; j<tam; j++,fil++) {
          for (int k=0,tk=diferenciados.length;k<tk;k++)
            encFilas[k+1][fil+1] = Variable.toString(diferenciados[k],llave[k],codigosIESDif,codigosProgramasDif);
          for (int i=0,t=semPer.length; i<t; i++) {
            tabla[fil][i]=vals[j][semPer[i]-1]==Double.MAX_VALUE?"":RutinasGUI.df_porcentaje.format(vals[j][semPer[i]-1]/100);
          }
        }
      }
      for (int i=0; i<semPer.length; i++) encCols[i]=""+semPer[i];
      JPanel panel=new MyBorderPane(false,0,0,0,0,null,null,new MyPanelTabla(new InfoTabla(tabla,encFilas,encCols),-1,-1,65),null,null);
      Map<byte[], double[]> series = new TreeMap<byte[], double[]>(KernelSPADIES.compByteArrayEspecial);
      Variable[] difExtra = CajaDeHerramientas.concatenarArreglos(Variable.class, diferenciados, new Variable[]{Variable.NUMERO_SEMESTRE_PER});
      for(Entry<byte[],double[][]> vals:mvals.entrySet()) {
        double[][] ser = vals.getValue();
        for (byte i:semPer) {
          byte[] llave = CajaDeHerramientas.concatenarArreglos(vals.getKey(), new byte[]{(byte) (i+1)});
          double[] serie = new double[tam];  
          for (int j=0; j<tam; j++,fil++) {
            serie[j] = ser[j][i-1]==Double.MAX_VALUE?Double.NaN:ser[j][i-1];//TODO revisar si en el kernel hay mucha vaina que este sobre 100
          }
          series.put(llave, serie);
        }
      }
      ChartPanel grafPorc=FabricaGraficas.crearGraficaConteosPoblacion("",difExtra,new Object[]{series,codigosIESDif,codigosProgramasDif},true,true, 0xFF&limInf);
      //new VentanaResultadoPanel(VentanaPrincipal.getInstance(),"Porcentaje de deserción por cohorte",panel,new MyEditorPane(false,getStringSeleccion())).setVisible(true);
      new VentanaResultadoPanel(VentanaPrincipal.getInstance(),new String[]{"Porcentaje de deserción por cohorte","Grafica"},new JPanel[]{panel,grafPorc},new MyEditorPane(false,getStringSeleccion())).setVisible(true);
      VentanaRealizarConsultaGrafica.this.dispose();
    }
  }
  public static Object[] obtenerResultado(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=KernelSPADIES.getInstance().getConteoPoblacion3(filtros,diferenciados);
    }
    else {
      // Variable.PROGRAMA_EST no debe estar en en 'filtros'
      //resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(15,Object[].class,filtros,diferenciados);
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
}
