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
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import org.jfree.chart.*;
import spadies.gui.graficas.*;
import spadies.gui.util.*;
import spadies.gui.util.RutinasGUI.FileRunnable;
import spadies.kernel.*;
import spadies.util.*;
import spadies.util.variables.*;

@SuppressWarnings("serial")
public class VentanaTablaEstudiantes extends JFrame implements Observer {
  private static final transient NumberFormat df=new DecimalFormat("0.0000");
  private final KernelSPADIES kernel=KernelSPADIES.getInstance();
  private final EstudianteDAO[] estudiantes;
  private final MyButton botonExportarCSV=new MyButton("Exportar a formato CSV",null,0);
  private Filtro[] filtros;
  private String[][] filtrosE;
  public VentanaTablaEstudiantes(JFrame padre, String titulo, InfoTabla tabla, EstudianteDAO[] estudiantes, JComponent labelDescripcion, Filtro[] filtros, String[][] filtrosE) {
    super(titulo);
    this.estudiantes=estudiantes;
    setIconImage(padre.getIconImage());
    kernel.addObserver(this);
    JPanel panelCentro=new MyBorderPane(false,0,0,0,0,null,null,new MyPanelTablaEstudiantes(tabla,100,100),null,null);
    Component compDerecha=(labelDescripcion==null)?null:new MyScrollPane(labelDescripcion,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,-1,-1);
    Component compPrincipal=(compDerecha==null)?panelCentro:new MySplitPane(JSplitPane.HORIZONTAL_SPLIT,true,panelCentro,compDerecha,true);
    {
      setContentPane(new MyBorderPane(false,0,0,0,0,null,null,compPrincipal,null,null));
      Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
      setResizable(true);
      setSize(new Dimension(d.width-200,d.height-200));
      if (labelDescripcion!=null) {
        MySplitPane msp=(MySplitPane)compPrincipal;
        msp.setResizeWeight(1.0);
        msp.setDividerLocation(getWidth()-300);
      }
      setLocation((int)((d.width-getWidth())/2),(int)((d.height-getHeight())/2));
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    this.filtros = filtros;
    this.filtrosE = filtrosE;
  }
  public void update(Observable obs, Object arg) {
    if (arg.equals("CARGA")) dispose();
  }
  public void setVisible(boolean b) {
    if (b) super.setVisible(true);
    else   dispose();
  }
  public void dispose() {
    kernel.deleteObserver(this);
    super.setVisible(false);
    super.dispose();
  }
  public class MyPanelTablaEstudiantes extends JPanel {
    private final JTable tabla=new JTable();
    private final AbstractTableModel model;
    private final InfoTabla infoTabla;
    private final MyButton botonConsultar=new MyButton("Consultar estudiante",null,0);
    public MyPanelTablaEstudiantes(InfoTabla pInfoTabla, int prefWidth, int prefHeight) {
      infoTabla=pInfoTabla;
      model=new AbstractTableModel(){
        public int getRowCount() {return infoTabla.getNumFilas()+1;}
        public int getColumnCount() {return 1+infoTabla.getNumColumnas();}
        public Class<?> getColumnClass(int col) {return String.class;}
        public boolean isCellEditable(int row, int col) {return false;}
        public Object getValueAt(int row, int col) {
          if (row==0 && col>=1) return infoTabla.getEncabezadoColumna(col-1);
          if (col==0) return (row==0)?"#":(""+row);
          return infoTabla.getValor(row-1,col-1);
        }
      };
      tabla.setModel(model);
      tabla.setTableHeader(null);
      tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      tabla.setDefaultRenderer(String.class,new DefaultTableCellRenderer() {
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
          setHorizontalAlignment((col==0)?JLabel.CENTER:JLabel.LEFT);
          int tipo=(row==0)?((col==0)?1:2):((col==0)?2:3);
          setBackground((tipo==1)?new Color(185,185,185):((tipo==2)?new Color(205,205,205):Color.WHITE));
          setFont(new Font("Dialog",tipo<=2?Font.BOLD:Font.PLAIN,12));
          setText((val==null)?"":(""+val));
          if (sel && row>0) setBackground((col==0)?new Color(190,190,100):new Color(255,255,170));
          return this;
        }
      });
      actualizarTabla();
      for (JButton b:Arrays.asList(botonConsultar)) RutinasGUI.configurarBoton(b,new Color(230,255,230),12,-1,20);
      botonConsultar.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          consultarEstudiante();
        }
      });
      tabla.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount()==2 && e.getButton()==MouseEvent.BUTTON1) consultarEstudiante();
        }
      });
      JScrollPane panelTabla=new MyScrollPane(tabla,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,prefWidth,prefHeight);
      panelTabla.getViewport().setOpaque(true);
      panelTabla.getViewport().setBackground(Color.WHITE);
      for (JButton b:Arrays.asList(botonExportarCSV)) RutinasGUI.configurarBoton(b,new Color(230,255,230),10,-1,16);
      botonExportarCSV.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          RutinasGUI.exportarTablaGEN(VentanaTablaEstudiantes.this, new FileRunnable() {
            public void run() {
              try {
                kernel.writeEstudiantes(file,filtros,filtrosE);
              } catch (Throwable t) {
                throw new Error(t);
              }
            }
          });
          //RutinasGUI.exportarTablaCSV(VentanaTablaEstudiantes.this,model);
        }
      });
      setLayout(new BorderLayout());
      add(panelTabla,BorderLayout.CENTER);
      add(botonExportarCSV,BorderLayout.SOUTH);
      add(new MyBorderPane(false,0,0,0,0,botonConsultar,null,null,null,null),BorderLayout.NORTH);
    }
    private void actualizarTabla() {
      model.fireTableDataChanged();
      model.fireTableStructureChanged();
      tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      TableColumnModel tcm=tabla.getColumnModel();
      FontMetrics fm=tabla.getFontMetrics(new Font("Dialog",Font.PLAIN,12));
      for (int i=0,it=tcm.getColumnCount(),jt=model.getRowCount(); i<it; i++) {
        int ancho=(i==0?40:3);
        if (i>=1) for (int j=0; j<jt; j++) ancho=Math.max(ancho,fm.stringWidth(model.getValueAt(j,i).toString())+5);
        RutinasGUI.setAnchoColumna(tcm,i,ancho);
      }
    }
    public JTable getTabla() {
      return tabla;
    }
    public void consultarEstudiante() {
      int row=tabla.getSelectedRow();
      if (row<=0) {
        RutinasGUI.desplegarError(MyPanelTablaEstudiantes.this,"Debe seleccionar un estudiante de la lista.");
        return;
      }
      row--;
      if (row<estudiantes.length) new VentanaEstudiante(estudiantes[row]).setVisible(true);
    }
  }
  public class VentanaEstudiante extends JFrame implements Observer {
    private final MyButton botonExportar=new MyButton("Exportar a formato HTML",null,0);
    @SuppressWarnings("unchecked")
    public VentanaEstudiante(EstudianteDAO e) {
      super();
      super.setTitle(new String(e.datosPersonales.apellido)+", "+new String(e.datosPersonales.nombre));
      setIconImage(VentanaTablaEstudiantes.this.getIconImage());
      kernel.addObserver(this);
      final StringBuffer sb=new StringBuffer();
      {
        int indProg=e.datos.getIndicePrograma();
        int puntajeICFES=e.datos.getPuntajeICFES();
        int numeroHermanos=e.datos.getmNumeroHermanos();
        int posicionEntreHermanos=e.datos.getmPosicionEntreLosHermanos();
        int edadPresentacionIcfes=e.datos.getEdadAlPresentarElICFES();
        String[][] variablesBasicas1={
            {"Cohorte",CajaDeHerramientas.textoSemestreToString(e.ies.semestres[e.datos.getSemestrePrimiparo()])},
            {"Nombres",new String(e.datosPersonales.nombre)},
            {"Apellidos",new String(e.datosPersonales.apellido)},
            {"Documento",CajaDeHerramientas.tipoDocumentoToString(e.datosPersonales.tipoDocumento)+" "+(e.datosPersonales.documento==-1?"":(""+e.datosPersonales.documento))},
            {"Fecha de nacimiento (DD/MM/AAAA)",CajaDeHerramientas.fechaToString(e.datosPersonales.anhoFechaNacimiento,e.datosPersonales.mesFechaNacimiento,e.datosPersonales.diaFechaNacimiento)},
            {"Sexo",CajaDeHerramientas.sexoToString(e.datos.getSexo())},
            {"Programa",indProg==-1?"":new String(e.ies.programas[indProg].nombre)},
            {"Código de estudiante",new String(e.datosPersonales.codigo)},
            {"Puntaje (estandarizado) examen de estado",puntajeICFES==-1?Constantes.S_DESCONOCIDO:(""+puntajeICFES+"/100")},
        };
        String[][] variablesBasicas2={
            {"Nivel educativo de la madre",Variable.NIVEL_EDUCATIVO_MADRE_EST.rango.toString(Variable.NIVEL_EDUCATIVO_MADRE_EST.rango.getRango(e.datos.getmNivelEducativoMadre()))},
            {"Número de hermanos",numeroHermanos==-1?Constantes.S_DESCONOCIDO:String.valueOf(numeroHermanos)},
            {"Posición entre hermanos",posicionEntreHermanos==-1?Constantes.S_DESCONOCIDO:String.valueOf(posicionEntreHermanos)},
            {"Edad de presentación del examen de estado",edadPresentacionIcfes==-1?Constantes.S_DESCONOCIDO:String.valueOf(edadPresentacionIcfes)},
            {"Vivienda propia",Variable.VIVIENDA_PROPIA_EST.rango.toString(Variable.VIVIENDA_PROPIA_EST.rango.getRango(e.datos.getmViviendaPropia()))},
            {"Trabajaba al presentar el examen de estado",Variable.TRABAJABA_CUANDO_ICFES_EST.rango.toString(Variable.TRABAJABA_CUANDO_ICFES_EST.rango.getRango(e.datos.getmTrabajabaCuandoPresentoIcfes()))},
            {"Ingreso familiar al presentar el examen de estado",Variable.INGRESO_HOGAR_EST.rango.toString(Variable.INGRESO_HOGAR_EST.rango.getRango(e.datos.getmIngresoHogar()))},
            {"Ingreso familiar al presentar el examen de estado",Variable.INGRESO_HOGAR_EST2.rango.toString(Variable.INGRESO_HOGAR_EST2.rango.getRango(e.datos.getmIngresoHogar2()))},
            {"Estrato",Variable.ESTRATO.rango.toString(Variable.ESTRATO.rango.getRango(e.datos.getmEstrato()))},
            {"Nivel de SISBEN",Variable.NIVEL_SISBEN.rango.toString(Variable.NIVEL_SISBEN.rango.getRango(e.datos.getmNivelSisben()))},
            {"Personas hogar",Variable.NUMERO_FAMILIARES_EST.rango.toString(Variable.NUMERO_FAMILIARES_EST.rango.getRango(e.datos.getmPersonasFamilia()))},

            {"Estado",Variable.CLASIFICACION_ESTADO_EST.rango.toString(Variable.CLASIFICACION_ESTADO_EST.rango.getRango(e.datos.getEstado()))},
            {"Período en que se graduó",(e.datos.getSemestreGrado()==-1)?"-":CajaDeHerramientas.textoSemestreToString(e.ies.semestres[e.datos.getSemestreGrado()])}
        };
        LinkedList<String[]> variablesBasicas3=new LinkedList<String[]>();
        for (int k=0; k<5; k++) if (e.ies.variablesExtras[k].nombre.length>0) {
          int val=e.datos.getValorVariableExtra(k);
          variablesBasicas3.add(new String[]{new String(e.ies.variablesExtras[k].nombre),(val==-1)?Constantes.S_DESCONOCIDO:new String(e.ies.variablesExtras[k].nombresValores[val])});
        }
        sb.append("<html>");
        sb.append("<font style=\"font-size: 18\"><b>Información del estudiante "+new String(e.datosPersonales.apellido)+", "+new String(e.datosPersonales.nombre)+"</b></font>");
        sb.append("<p>La información individual corresponde a la moda del colegio de donde se graduó el estudiante.<p>");
        sb.append("<p><table border=0 width=100%><tr><td width=50%>");
        for (String[] varba:variablesBasicas1) sb.append("<font style=\"font-size: 14\"><b>"+varba[0]+"</b></font><p>"+CajaDeHerramientas.stringToHTML("    "+varba[1])+"<p>");
        sb.append("</td><td width=50%>");
        for (String[] varba:variablesBasicas2) sb.append("<font style=\"font-size: 14\"><b>"+varba[0]+"</b></font><p>"+CajaDeHerramientas.stringToHTML("    "+varba[1])+"<p>");
        sb.append("</td></tr>");
        if (variablesBasicas3.size()>0) {
          sb.append("<tr><td colspan=2 width=100%>");
          for (String[] varba:variablesBasicas3) sb.append("<font style=\"font-size: 14\"><b>"+varba[0]+"</b></font><p>"+CajaDeHerramientas.stringToHTML("    "+varba[1])+"<p>");
          sb.append("</td></tr>");
        }
        sb.append("</table>");
        sb.append("<p><table border=1><tr>");
        for (String s:new String[]{"#","Período","Materias tomadas","Materias aprobadas","Repitencia","Apoyo<p>financiero","Apoyo<p>académico","Otros<p>apoyos","Apoyo<p>ICETEX","Grupo de<p>riesgo","Riesgo","Riesgo<p>estructural","Supervivencia","Supervivencia<p>estructural"}) sb.append("<td bgcolor=\"#A0A0A0\" align=center><b>"+s+"</b></td>");
        for (int i=0;i<Constantes.maxVariablesExtraDinamicas;i++)
          if (e.ies.variablesExtrasD[i].nombre.length>0)
            sb.append("<td bgcolor=\"#A0A0A0\" align=center><b>"+new String(e.ies.variablesExtrasD[i].nombre)+"</b></td>");
        sb.append("</tr>");
        for (int i=0,n=e.ies.n,nper=0; i<n; i++) if (e.estaMatriculado[i]) {
          String w[]={
             ""+(++nper),
             CajaDeHerramientas.textoSemestreToString(e.ies.semestres[i]),
             toString(e.datos.getNumeroMateriasTomadas(i)),
             toString(e.datos.getNumeroMateriasAprobadas(i)),
             toString(e.repitencias[i]),
             toString(e.datos.getRecibioApoyoFinanciero(i)),
             toString(e.datos.getRecibioApoyoAcademico(i)),
             toString(e.datos.getRecibioApoyoOtro(i)),
             Variable.TIPO_ICETEX_PER.rango.toString(Variable.TIPO_ICETEX_PER.rango.getRango(e.datos.getTipoApoyoICETEXRecibido(i))),
             toString(e.datos.getClaseRiesgo(i)),
             toString(e.datos.getRiesgo(i)),
             toString(e.datos.getRiesgoEstructural(i)),
             toString(e.supervivencias[i]),
             toString(e.supervivenciasEstructurales[i])
          };
          sb.append("<tr>");
          for (String s:w) sb.append("<td align=center>"+CajaDeHerramientas.stringToHTML(s)+"</td>");
          for (int j=0;j<Constantes.maxVariablesExtraDinamicas;j++)
            if (e.ies.variablesExtrasD[j].nombre.length>0) {
              int val=e.datos.getValorVariableExtra(j,i);
              sb.append("<td align=center>"+CajaDeHerramientas.stringToHTML((val==-1)?Constantes.S_DESCONOCIDO:new String(e.ies.variablesExtrasD[j].nombresValores[val]))+"</td>");
            }
            
          sb.append("</tr>");
        }
        sb.append("</table><p></html>");
      }
      final ChartPanel panelGrafica;
      {
        SortedMap<String,SortedMap<Integer,Double>> seriesDatos=new TreeMap<String,SortedMap<Integer,Double>>();
        {
          SortedMap<Integer,Double> supervivenciaNormal=new TreeMap<Integer,Double>(),supervivenciaEstructural=new TreeMap<Integer,Double>();
          seriesDatos.put("Supervivencia estudiante",supervivenciaNormal);
          seriesDatos.put("Supervivencia estructural estudiante",supervivenciaEstructural);
          int nPerE=0,nPer=0;
          for (int i=0,n=e.ies.n; i<n; i++) if (e.estaMatriculado[i]) {
            double sup=e.supervivencias[i],supEst=e.supervivenciasEstructurales[i];
            if (sup!=-1d) supervivenciaNormal.put(++nPer,sup);
            if (supEst!=-1d) supervivenciaEstructural.put(++nPerE,supEst);
          }
        }
        panelGrafica=FabricaGraficas.crearGraficaSupervivenciaEstudiante("",seriesDatos);
      }
      for (JButton b:Arrays.asList(botonExportar)) RutinasGUI.configurarBoton(b,new Color(230,255,230),12,-1,20);
      botonExportar.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (RutinasGUI.fcExportarReporteHTML.showDialog(VentanaEstudiante.this,"Exportar")!=JFileChooser.APPROVE_OPTION) return;
          File f=RutinasGUI.fcExportarReporteHTML.getSelectedFile();
          if (!f.exists() && !f.getName().toLowerCase().endsWith(".html")) f=new File(f.getParentFile(),f.getName()+".html");
          if (f.exists() && !RutinasGUI.desplegarPregunta(VentanaEstudiante.this,"\""+f.getPath()+"\" ya existe. ¿Desea reemplazar tal archivo?")) return;
          try {
            String sF=f.getName();
            {
              int k=sF.lastIndexOf('.');
              if (k!=-1) sF=sF.substring(0,k);
            }
            sF+=".png";
            javax.imageio.ImageIO.write(panelGrafica.getChart().createBufferedImage(800,600),"png",new File(f.getParentFile(),sF));
            FileOutputStream fos=new FileOutputStream(f);
            String s=sb.toString();
            s=("<html><body bgcolor=\"F0F0F0\"><font style=\"font-size: 12\">"+s.replaceAll("[\\Q<html>\\E\\Q</html>\\E]","").replaceAll("\\Q<p>\\E","<br>")+"<img src=\""+sF+"\"></font></body></html>");
            fos.write(s.getBytes());
            fos.close();
            RutinasGUI.desplegarInformacion(VentanaEstudiante.this,"<html>El reporte se exportó exitosamente a formato HTML en el archivo especificado.</html>");
          }
          catch (Throwable th) {
            RutinasGUI.desplegarError(VentanaEstudiante.this,"Hubo un error escribiendo el reporte en el archivo especificado");
          }
        }
      });
      JPanel panelPrincipal=new MyBorderPane(false,0,0,0,0,null,null,new MyBoxPane(BoxLayout.Y_AXIS,new MyEditorPane(true,sb.toString())),null,new MyFlowPane(0,0,panelGrafica));
      JScrollPane scrollPrincipal=new MyScrollPane(panelPrincipal,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,-1,-1);
      setContentPane(new MyBorderPane(false,5,5,5,5,botonExportar,null,scrollPrincipal,null,null));
      {
        Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
        setResizable(true);
        setSize(new Dimension(d.width-50,d.height-150));
        setLocation((int)((d.width-getWidth())/2),20);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      }
    }
    public void update(Observable obs, Object arg) {
      if (arg.equals("CARGA")) dispose();
    }
    public void setVisible(boolean b) {
      if (b) super.setVisible(true);
      else   dispose();
    }
    public void dispose() {
      kernel.deleteObserver(this);
      super.setVisible(false);
      super.dispose();
    }
    private String toString(int val) {
      return (val==-1)?"?":(""+val);
    }
    private String toString(double val) {
      return (val==-1)?"?":df.format(val);
    }
    private String toString(boolean val) {
      return val?"Si":"No";
    }
  }
}
