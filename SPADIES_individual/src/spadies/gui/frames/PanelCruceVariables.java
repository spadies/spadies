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
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

import spadies.gui.util.InfoTabla;
import spadies.gui.util.MyBorderPane;
import spadies.gui.util.MyBoxPane;
import spadies.gui.util.MyDialogProgreso;
import spadies.gui.util.MyEditorPane;
import spadies.gui.util.MyFlowPane;
import spadies.gui.util.MyPanelSeleccion;
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
import spadies.util.variables.Variable;

@SuppressWarnings("serial")
public class PanelCruceVariables extends JPanel implements ActionListener {
  private static final CategorizacionVariables cvar = RutinasGUI.categorizacionVariables(true);
  private static final Variable[] criterios = cvar.criterios;
  private static final int lims[][] = cvar.lims;
  private static final String titulos[] = cvar.titulos;
  private final int tam=criterios.length,tamNormal=tam-cvar.textras;
  private final MyPanelSeleccion[] panelsSeleccion=new MyPanelSeleccion[tam];
  private final JCheckBox[] checksEjeX=new JCheckBox[tam];
  private final JCheckBox[] checksEjeY=new JCheckBox[tam];
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
  public PanelCruceVariables() {
    for (int i=0; i<tam; i++) {
      panelsSeleccion[i]=new MyPanelSeleccion(criterios[i].items,true,280,200);
      //if (criterios[i]==Variable.NUMERO_SEMESTRE_PER) panelsSeleccion[i].seleccionar(false, 1); 
      {
        //JCheckBox cb=new JCheckBox("Diferenciado?",false);
        JCheckBox cbx=new JCheckBox("Filas",false);
        JCheckBox cby=new JCheckBox("Columnas",false);
        //TODO
        //checksDiferenciacion[i]=cbx;
        checksEjeX[i]=cbx;
        checksEjeY[i]=cby;
        cbx.setFont(cbx.getFont().deriveFont(Font.BOLD));
        cbx.setPreferredSize(new Dimension(cbx.getPreferredSize().width,20));
        cby.setFont(cby.getFont().deriveFont(Font.BOLD));
        cby.setPreferredSize(new Dimension(cby.getPreferredSize().width,20));
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
          //JComponent comp=new MyBoxPane(BoxLayout.Y_AXIS,panelsSeleccion[i]);
          JPanel comp = new MyBoxPane(BoxLayout.Y_AXIS);
          //panelSub.add(new MyFlowPane(0,0,new MyLabel(criterios[i].nombre)));
          comp.add(new MyFlowPane(0,0,checksEjeX[i]));
          comp.add(new MyFlowPane(0,0,checksEjeY[i]));
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
        panelSub.add(new MyFlowPane(0,0,checksEjeX[i]));
        panelSub.add(new MyFlowPane(0,0,checksEjeY[i]));
        panelSub.add(panelsSeleccion[i]);
        panelesSelecciones.add(panelSub, String.valueOf(i));
      }
    }
    posExtra = pos;
    menuVariables.expandRow(0);
    /*for (int j=0,jT=lims.length; j<jT; j++) {
      if (titulos[j]==null) {
        for (int i=lims[j][0]; i<=lims[j][1]; i++) {
          JComponent comp=new MyBoxPane(BoxLayout.Y_AXIS,new MyFlowPane(0,0,/xchecksDiferenciacion[i],x/checksEjeX[i],checksEjeY[i]),panelsSeleccion[i]);
          if (i<tamNormal) panelVariables.addTab(criterios[i].nombre,comp);
          else             tabsExtras[i-tamNormal]=comp;
        }
        continue;
      }
      JPanel panel=new MyBoxPane(BoxLayout.Y_AXIS);
      for (int i=lims[j][0]; i<=lims[j][1]; i++) {
        panel.add(new MyFlowPane(0,0,new MyLabel(criterios[i].nombre)));
        //panel.add(new MyFlowPane(0,0,checksDiferenciacion[i]));
        panel.add(new MyFlowPane(0,0,checksEjeX[i]));
        panel.add(new MyFlowPane(0,0,checksEjeY[i]));
        panel.add(panelsSeleccion[i]);
        if (i<lims[j][1]) panel.add(Box.createVerticalStrut(15));
      }
      panelVariables.addTab(titulos[j],panel);
    }
    */
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
    ActionListener alx=new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JCheckBox comp = (JCheckBox) e.getSource();
        if (comp.isSelected()) {
          VentanaPrincipal.getInstance().limpiarDifereciacionX();
          for (int i=0; i<tam; i++) if (comp!=checksEjeX[i]) checksEjeX[i].setSelected(false);
        }
        actualizarLabelSeleccion();
      }
    };
    ActionListener aly=new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JCheckBox comp = (JCheckBox) e.getSource();
        if (comp.isSelected()) {
          VentanaPrincipal.getInstance().limpiarDifereciacionY();
          for (int i=0; i<tam; i++) if (comp!=checksEjeY[i]) checksEjeY[i].setSelected(false);
        }
        actualizarLabelSeleccion();
      }
    };
    for (int i=0; i<tam; i++) panelsSeleccion[i].getTabla().getModel().addTableModelListener(tml);
    //for (int i=0; i<tam; i++) checksDiferenciacion[i].addActionListener(al);
    for (int i=0; i<tam; i++) checksEjeX[i].addActionListener(alx);
    for (int i=0; i<tam; i++) checksEjeY[i].addActionListener(aly);
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
    /*for (JComponent comp:tabsExtras) panelVariables.remove(comp);
    for (int i=0; i<tam; i++) {
      Variable v=criterios[i];
      if (v==Variable.PROGRAMA_EST || v==Variable.PERIODO_INGRESO_EST || v==Variable.PERIODO_MATRICULADO_PER || v==Variable.NUMERO_SEMESTRE_PER || v==Variable.ULTIMO_PERIODO_MATRICULADO_EST) {
        panelsSeleccion[i].setValoresManteniendoSeleccion(v.items,true);
      }
      else if (i>=tamNormal) {
        panelsSeleccion[i].setValores(v.items,true);
        if (v.items.length>0) panelVariables.addTab("<html><font color=\"#008000\">"+CajaDeHerramientas.stringToHTML(criterios[i].nombre)+"</font></html>",tabsExtras[i-tamNormal]);
      }
    }*/
    cuadrando=false;
    actualizarLabelSeleccion();
  }
  private void actualizarLabelSeleccion() {
    if (!cuadrando) labelSeleccion.setText(getStringSeleccion());
  }
  /**
   * 
   * @return Arreglo con:<ol>
   * <li>Areglo con los MyPanelSeleccion de las selecciones de variables</li>
   * <li>Areglo con los JCheckBox correspondientes a las diferenciaciones</li>
   * <li>Areglo con las Variable que se incluyen en este panel</li>
   * </ol>
   */
  private Object[] getComponentesParametros() {
    VentanaPrincipal vp=VentanaPrincipal.getInstance();
    MyPanelSeleccion[] arrPS=CajaDeHerramientas.concatenarArreglos(MyPanelSeleccion.class,vp.getPanelsSeleccion(),panelsSeleccion);
    //JCheckBox[] arrCB=CajaDeHerramientas.concatenarArreglos(JCheckBox.class,vp.getChecksDiferenciacion(),checksDiferenciacion);
    //TODO falta soporte Variables Agregado, IES
    //JCheckBox[] arrCB = CajaDeHerramientas.concatenarArreglos(JCheckBox.class,vp.getChecksDiferenciacion(),checksEjeX, checksEjeY);
    JCheckBox[] arrCBX = CajaDeHerramientas.concatenarArreglos(JCheckBox.class,vp.getChecksDiferenciacionX(),checksEjeX);
    JCheckBox[] arrCBY = CajaDeHerramientas.concatenarArreglos(JCheckBox.class,vp.getChecksDiferenciacionY(),checksEjeY);
    Variable[] arrVE = CajaDeHerramientas.concatenarArreglos(Variable.class,vp.getCriterios(),criterios);
    //return new Object[]{arrPS,arrCB,arrVE};
    return new Object[]{arrPS,arrCBX,arrCBY,arrVE};
  }
  private String getStringSeleccion() {
    Object[] params=getComponentesParametros();
    //return RutinasGUI.getStringSeleccion((MyPanelSeleccion[])(params[0]),(JCheckBox[])(params[1]),(Variable[])(params[2]),null,VentanaPrincipal.getInstance().estaSeleccionandoAgregado());
    return RutinasGUI.getStringSeleccion((MyPanelSeleccion[])(params[0]),(JCheckBox[])(params[1]),(JCheckBox[])(params[2]),(Variable[])(params[3]),null,VentanaPrincipal.getInstance().estaSeleccionandoAgregado());
  }
  public void actionPerformed(ActionEvent e) {
    int ind=CajaDeHerramientas.searchSame(botones,e.getSource());
    if (ind==0) {
      cuadrando=true;
      for (int i=0; i<tam; i++) {
        if (criterios[i]==Variable.NUMERO_SEMESTRE_PER) {
          panelsSeleccion[i].seleccionar(false, 1);
        }
        else panelsSeleccion[i].seleccionar(true);
        //checksDiferenciacion[i].setSelected(false);
        checksEjeX[i].setSelected(false);
        checksEjeY[i].setSelected(false);
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
        //JCheckBox[] arrCB=(JCheckBox[])(params[1]);
        JCheckBox[] arrCBX=(JCheckBox[])(params[1]);
        JCheckBox[] arrCBY=(JCheckBox[])(params[2]);
        Variable[] arrVE=(Variable[])(params[3]);
        EnumSet<Variable> mDiferenciadosX=EnumSet.noneOf(Variable.class);
        EnumSet<Variable> mDiferenciadosY=EnumSet.noneOf(Variable.class);
        EnumMap<Variable,Filtro> mFiltros=new EnumMap<Variable,Filtro>(Variable.class);
        RutinasGUI.getSeleccion(arrVE,arrCBX,arrCBY,arrPS,mDiferenciadosX,mDiferenciadosY,mFiltros);
        LinkedList<Variable> mDiferenciados=new LinkedList<Variable>();
        mDiferenciados.addAll(mDiferenciadosX);
        mDiferenciados.addAll(mDiferenciadosY);
        if (mDiferenciadosX.size()!=1) throw new MyException("Debe escoger una variable para el Eje X.");
        if (mDiferenciadosY.size()!=1) throw new MyException("Debe escoger una variable para el Eje Y.");
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
    private final Variable[] diferenciados;
    private final Filtro[] filtros;
    public VentanaRealizarConsultaGrafica(boolean pAlMinisterio, Variable[] pDiferenciados, Filtro[] pFiltros) {
      super(VentanaPrincipal.getInstance(),"Realizando la consulta"+(pAlMinisterio?" en el Ministerio de Educación Nacional":""),false);
      alMinisterio=pAlMinisterio;
      diferenciados=pDiferenciados;
      filtros=pFiltros;
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
          valoresPX[fil][col]=RutinasGUI.df_porcentaje.format(cont/totX[fil]);
          valoresPY[fil][col]=RutinasGUI.df_porcentaje.format(cont/totY[col]);
          valoresPT[fil][col]=RutinasGUI.df_porcentaje.format(cont/tot);
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
      VentanaRealizarConsultaGrafica.this.dispose();
      new VentanaTablas(VentanaPrincipal.getInstance(),"Resultado del cruce",new MyEditorPane(false,getStringSeleccion()),
          new String[]{"Conteo", "Porcentaje filas", "Porcentaje columnas", "Porcentaje total"},
          new InfoTabla[]{tabla, tablaPX, tablaPY, tablaPT}).setVisible(true);
    }
  }
  public static Object[] obtenerResultado(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=KernelSPADIES.getInstance().getCruceVariables(filtros,diferenciados);
    }
    else {
      //TODO Habilitar conectividad?!!
      //Variable.PROGRAMA_EST no debe estar ni en 'diferenciados' ni en 'filtros'
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(8,Object[].class,filtros,diferenciados);
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
  public void limpiarDifereciacionX() {
    for (JCheckBox chk:checksEjeX) chk.setSelected(false);
  }
  public void limpiarDifereciacionY() {
    for (JCheckBox chk:checksEjeY) chk.setSelected(false);
  }
}
