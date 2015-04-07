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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeSelectionModel;

import spadies.gui.util.*;
import spadies.gui.util.RutinasGUI.CategorizacionVariables;
import spadies.kernel.*;
import spadies.util.*;
import spadies.util.variables.*;

@SuppressWarnings("serial")
public class PanelAnalisisRiesgo extends JPanel implements ActionListener {
  private static final CategorizacionVariables cvar = RutinasGUI.categorizacionVariables(true);
  private static final Variable[] criterios = cvar.criterios;
  private static final int lims[][] = cvar.lims;
  private static final String titulos[] = cvar.titulos;
  private final int tam=criterios.length,
    tamNormal=tam-cvar.textras;
  private final MyPanelSeleccion[] panelsSeleccion=new MyPanelSeleccion[tam];
  private final MyEditorPane labelSeleccion=new MyEditorPane(false);
  //private final JTabbedPane panelVariables=new JTabbedPane(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);
  private final JPanel panelVariables = new JPanel(new GridLayout());
  private DefaultMutableTreeNode dmt = new DefaultMutableTreeNode(new OpcionVariable("Variables"));
  private DefaultTreeModel dtm = new DefaultTreeModel(dmt);
  private final JTree menuVariables = new JTree(/*dmt*/dtm);
  //private final JTree menuVariables = new JTree(dmt);
  //private final JComponent[] tabsExtras=new JComponent[5];
  private final DefaultMutableTreeNode[] nodosExtras=new DefaultMutableTreeNode[cvar.textras];
  private final PanelFiltroEstudiante panelFiltroEstudiante=new PanelFiltroEstudiante();
  private final JButton[] botones=new JButton[2];
  private boolean cuadrando=false;
  private final int posExtra;
  public PanelAnalisisRiesgo() {
    for (int i=0; i<tam; i++) {
      panelsSeleccion[i]=new MyPanelSeleccion(criterios[i].items,true,280,200);
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
        // TODO Auto-generated method stub
      }
      public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {}
    });
    //menuVariables.setRootVisible(false);
    menuVariables.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    menuVariables.putClientProperty("JTree.lineStyle", false);

    panelesSelecciones.add(new JPanel(),"");
    //for (int i=0,li=criterios.length;i<li;i++) panelesSelecciones.add(String.valueOf(i), panelsSeleccion[i]);
    panelVariables.add(menuVariables);
    panelVariables.add(panelesSelecciones);
    int pos = 0;
    for (int j=0,jT=lims.length; j<jT; j++) {
      if (titulos[j]==null) {
        for (int i=lims[j][0]; i<=lims[j][1]; i++) {
          JComponent comp=new MyBoxPane(BoxLayout.Y_AXIS,panelsSeleccion[i]);
          panelesSelecciones.add(comp, String.valueOf(i));
          DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(new OpcionVariable(i, criterios[i].nombre));
          if (i<tamNormal) {
            dtm.insertNodeInto(nodo, dmt, pos++);
            //dmt.add(nodo);
          }
          //else tabsExtras[i-tamNormal]=comp;
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
        panelSub.add(panelsSeleccion[i]);
        panelesSelecciones.add(panelSub, String.valueOf(i));
      }
    }
    posExtra = pos;
    panelesSelecciones.add(panelFiltroEstudiante,String.valueOf(999));
    dmt.add(new DefaultMutableTreeNode(new OpcionVariable(999, "Datos personales")));
    menuVariables.expandRow(0);
    botones[0]=new JButton("Restablecer selección");
    botones[1]=new JButton("Buscar estudiantes");
    for (JButton b:botones) {
      RutinasGUI.configurarBoton(b,new Color(245,245,255),16,-1,23);
      b.addActionListener(this);
    }
    TableModelListener tml=new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        actualizarLabelSeleccion();
      }
    };
    for (int i=0; i<tam; i++) panelsSeleccion[i].getTabla().getModel().addTableModelListener(tml);
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
    //for (DefaultMutableTreeNode nodo:nodosExtras) if (dmt.isNodeChild(nodo)) dmt.remove(nodo);
    int pos = posExtra;
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
    Variable[] arrVE=CajaDeHerramientas.concatenarArreglos(Variable.class,vp.getCriterios(),criterios);
    return new Object[]{arrPS,arrVE};
  }
  private String getStringSeleccion() {
    Object[] params=getComponentesParametros();
    return RutinasGUI.getStringSeleccion((MyPanelSeleccion[])(params[0]),null,(Variable[])(params[1]),panelFiltroEstudiante.getFiltro(),VentanaPrincipal.getInstance().estaSeleccionandoAgregado());
  }
  public void actionPerformed(ActionEvent e) {
    int ind=CajaDeHerramientas.searchSame(botones,e.getSource());
    if (ind==0) {
      cuadrando=true;
      for (int i=0; i<tam; i++) panelsSeleccion[i].seleccionar(true);
      panelFiltroEstudiante.limpiarFiltro();
      cuadrando=false;
      actualizarLabelSeleccion();
    }
    else if (ind==1) {
      Component cmDespl=this;
      try {
        Object[] params=getComponentesParametros();
        MyPanelSeleccion[] arrPS=(MyPanelSeleccion[])(params[0]);
        Variable[] arrVE=(Variable[])(params[1]);
        EnumMap<Variable,Filtro> mFiltros=new EnumMap<Variable,Filtro>(Variable.class);
        RutinasGUI.getSeleccion(arrVE,null,arrPS,null,mFiltros);
        Filtro[] filtros=mFiltros.values().toArray(new Filtro[0]);
        String filtroEspecial[][]=new String[3][],w[]=panelFiltroEstudiante.getFiltro();
        for (int i=0; i<3; i++) {
          String arr[]=w[i].split(" +");
          if (arr.length==1 && arr[0].length()==0) arr=new String[0];
          filtroEspecial[i]=arr;
        }
        new VentanaRealizarBusqueda(filtros,filtroEspecial).ejecutar();
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
  private class PanelFiltroEstudiante extends JPanel {
    private final MyLabel labelTitulo=new MyLabel("<html>Filtro de estudiantes por datos personales</html>");
    private final JTextField campoNombreApellido=new JTextField(12);
    private final JTextField campoDocumento=new JTextField(12);
    private final JTextField campoCodigo=new JTextField(12);
    private final JLabel labelNombreApellido=new JLabel("Apellidos y/o nombres:");
    private final JLabel labelDocumento=new JLabel("Documento:");
    private final JLabel labelCodigo=new JLabel("Código de estudiante en la IES:");
    private MyButton botonModificar=new MyButton("Modificar","Modifica el filtro de estudiantes por datos personales",0);
    private MyButton botonLimpiar=new MyButton("Limpiar","Limpia el filtro de estudiantes por datos personales",0);
    private String[] filtro={"","",""};
    public PanelFiltroEstudiante() {
      final JTextField campos[]={campoNombreApellido,campoDocumento,campoCodigo};
      for (int i=0; i<3; i++) {
        campos[i].setEditable(false);
        campos[i].setBackground(new Color(255,245,245));
      }
      for (JButton b:Arrays.asList(botonModificar,botonLimpiar)) RutinasGUI.configurarBoton(b,new Color(230,255,230),12,-1,20);
      botonModificar.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          new VentanaFiltroEstudiante(VentanaPrincipal.getInstance(),filtro).setVisible(true);
          for (int i=0; i<3; i++) campos[i].setText(filtro[i]);
          actualizarLabelSeleccion();
        }
      });
      botonLimpiar.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          limpiarFiltro();
          actualizarLabelSeleccion();
        }
      });
      JPanel panelBotones=new MyFlowPane(FlowLayout.RIGHT,5,0,botonModificar,botonLimpiar);
      setLayout(new BorderLayout());
      add(new MyBorderPane(false,8,8,8,8,labelTitulo,null,new MyBoxPane(BoxLayout.Y_AXIS,Box.createVerticalStrut(8),new MyFlowPane(0,0,labelNombreApellido),campoNombreApellido,Box.createVerticalStrut(8),new MyFlowPane(0,0,labelDocumento),campoDocumento,Box.createVerticalStrut(8),new MyFlowPane(0,0,labelCodigo),campoCodigo,Box.createVerticalStrut(8)),null,panelBotones),BorderLayout.NORTH);
    }
    public String[] getFiltro() {
      return filtro;
    }
    public void limpiarFiltro() {
      JTextField campos[]={campoNombreApellido,campoDocumento,campoCodigo};
      for (int i=0; i<3; i++) {
        filtro[i]="";
        campos[i].setText("");
      }
    }
  }
  private class VentanaRealizarBusqueda extends MyDialogProgreso {
    private final Filtro[] filtros;
    private final String[][] filtroEspecial;
    private final int maxNumResultados=2000;
    private int numResultados=0;
    public VentanaRealizarBusqueda(Filtro[] pFiltros, String[][] pFiltroEspecial) {
      super(VentanaPrincipal.getInstance(),"Realizando la consulta",false);
      filtros=pFiltros;
      filtroEspecial=pFiltroEspecial;
    }
    public void ejecutar() {
      setVisible(true);
      new Thread() {
        public void run() {
          Window window=VentanaRealizarBusqueda.this;
          try {
            generarTabla();
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
    private EstudianteDAO[] obtenerResultado() throws MyException {
      Object[] res=KernelSPADIES.getInstance().getEstudiantes(filtros,getFiltroEspecial(),maxNumResultados);
      numResultados=(Integer)(res[1]);
      return (EstudianteDAO[])(res[0]);
    }
    private String[][] getFiltroEspecial() {
      boolean filtroEspecialEsVacio=true;
      for (int i=0; filtroEspecialEsVacio && i<3; i++) if (filtroEspecial[i].length>0) filtroEspecialEsVacio=false;
      return filtroEspecialEsVacio?null:filtroEspecial;
    }
    private void generarTabla() throws Exception {
      EstudianteDAO[] respuesta=obtenerResultado();
      int t=respuesta.length,ind=0;
      if (t==0) throw new MyException("No se encontraron estudiantes que cumplan con los criterios seleccionados.");
      String info[][]=new String[t][];
      for (EstudianteDAO e:respuesta) {
        int indProg=e.datos.getIndicePrograma();
        info[ind++]=new String[]{
          new String(e.datosPersonales.apellido),
          new String(e.datosPersonales.nombre),
          CajaDeHerramientas.tipoDocumentoToString(e.datosPersonales.tipoDocumento),
          e.datosPersonales.documento==-1?"":(""+e.datosPersonales.documento),
          ""+e.ies.codigo,
          indProg==-1?"":new String(e.ies.programas[indProg].nombre),
          new String(e.datosPersonales.codigo)};
      }
      InfoTabla tablaEst=new InfoTabla(info,new String[0][0],new String[]{"Apellido","Nombre","","Documento","IES","Programa","Código estudiante"});
      VentanaRealizarBusqueda.this.dispose();
      if (numResultados>maxNumResultados) RutinasGUI.desplegarInformacion(VentanaPrincipal.getInstance(),"Se encontraron "+numResultados+" estudiantes. Sólo se desplegaran los primeros "+maxNumResultados+" estudiantes encontrados.");
      new VentanaTablaEstudiantes(VentanaPrincipal.getInstance(),"Resultado de la consulta",tablaEst,respuesta,new MyEditorPane(false,getStringSeleccion()),filtros,getFiltroEspecial()).setVisible(true);
    }
  }
}
