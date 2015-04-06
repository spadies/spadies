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
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import org.jfree.chart.*;
import spadies.gui.util.*;
import spadies.gui.graficas.*;
import spadies.kernel.*;
import spadies.util.*;
import spadies.util.variables.*;

@SuppressWarnings("serial")
public class PanelInformacionBasica extends JPanel implements ActionListener {
  private static final Variable[] criterios={
    Variable.SEXO_EST,
    Variable.TRABAJABA_CUANDO_ICFES_EST,
    Variable.CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST,
    Variable.INGRESO_HOGAR_EST,
    Variable.INGRESO_HOGAR_EST2,
    Variable.NIVEL_SISBEN,
    Variable.ESTRATO,
    Variable.VIVIENDA_PROPIA_EST,
    Variable.NUMERO_HERMANOS_EST,
    Variable.NUMERO_FAMILIARES_EST,
    Variable.POSICION_ENTRE_LOS_HERMANOS_EST,
    Variable.CLASIFICACION_PUNTAJE_ICFES_EST,
    Variable.NIVEL_EDUCATIVO_MADRE_EST,
    Variable.AREA_CONOCIMIENTO_EST,
    Variable.ICETEX_RECIBIDO_PER,
    Variable.TIPO_ICETEX_RECIBIDO_PER,
    Variable.APOYO_RECIBIDO_PER};
  private static final int tam=criterios.length,numCriteriosDiferenciacionPrimiparos=tam-3;
  //private final MyLabel labelTitulo[]={new MyLabel("Criterios de diferenciación de primíparos"),new MyLabel("Apoyos entregados por semestre"),new MyLabel("Otras consultas")};
  private final MyLabel labelTitulo[]={new MyLabel("Caracterización de los primíparos"),new MyLabel("Apoyos entregados por semestre"),new MyLabel("Otras consultas")};
  //private final JButton[] botones=new JButton[tam+3];
  private final JButton[] botones=new JButton[tam+3];
  private final MyEditorPane labelSeleccion=new MyEditorPane(false);
  public PanelInformacionBasica() {
    setLayout(new BorderLayout());
    for (int j=0,jT=labelTitulo.length; j<jT; j++) {
      labelTitulo[j].setFont(new Font("Dialog",Font.ITALIC,17));
      labelTitulo[j].setForeground(new Color(0,0,80));
    }
    Collection<Component> listaComponentes=new LinkedList<Component>();
    for (int i=0,iT=tam+3/*2*/; i<iT; i++) {
      if (i==0) {
        listaComponentes.add(labelTitulo[0]);
        listaComponentes.add(Box.createVerticalStrut(10));
      }
      else if (i==numCriteriosDiferenciacionPrimiparos) {
        listaComponentes.add(Box.createVerticalStrut(15));
        listaComponentes.add(labelTitulo[1]);
        listaComponentes.add(Box.createVerticalStrut(10));
      }
      else if (i==tam) {
        listaComponentes.add(Box.createVerticalStrut(15));
        listaComponentes.add(labelTitulo[2]);
        listaComponentes.add(Box.createVerticalStrut(10));
      }
      if (i<tam) {
        botones[i]=new JButton(criterios[i].nombre);
      }
      else if (i==tam) {
        botones[i]=new JButton("Clasificación desertores por nivel de aprobación");
      }
      else if (i==tam+1) {
        botones[i]=new JButton("Porcentaje de deserción por cohorte");
      }
      else if (i==tam+2) {
        botones[i]=new JButton("Variables por importancia");
      }
      listaComponentes.add(botones[i]);
      RutinasGUI.configurarBoton(botones[i],new Color(245,245,255),14,385,20);
    }
    setLayout(new BorderLayout());
    add(new MyBorderPane(false,0,0,0,0,null,new MyScrollPane(new MyBorderPane(false,0,50,0,50,null,null,new MyBoxPane(BoxLayout.Y_AXIS,Box.createVerticalGlue(),new MyFlowPane(FlowLayout.CENTER,0,0,new MyBoxPane(BoxLayout.Y_AXIS,listaComponentes.toArray(new Component[0]))),Box.createVerticalGlue()),null,null),JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,-1,-1),new MyScrollPane(labelSeleccion,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,-1,-1),null,null),BorderLayout.CENTER);
    for (JButton b:botones) b.addActionListener(this);
  }
  public void actionPerformed(ActionEvent e) {
    int ind=CajaDeHerramientas.searchSame(botones,e.getSource());
    if (ind==-1) return;
    Component cmDespl=this;
    try {
      EnumMap<Variable,Filtro> mFiltros=new EnumMap<Variable,Filtro>(Variable.class);
      VentanaPrincipal.getInstance().getSeleccion(null,mFiltros);
      Filtro[] filtrosIES=mFiltros.values().toArray(new Filtro[0]);
      if (ind<tam) {
        new VentanaRealizarConsultaBasica(VentanaPrincipal.getInstance().estaSeleccionandoAgregado(),criterios[ind],filtrosIES).ejecutar();
      }
      else if (ind==tam) {
        new VentanaRealizarConsultaNivelAprobacionDesertores(VentanaPrincipal.getInstance().estaSeleccionandoAgregado(),filtrosIES).ejecutar();
      }
      else if (ind==tam+1) {
        new VentanaRealizarConsultaPorcentajeDesercionPorCohorte(VentanaPrincipal.getInstance().estaSeleccionandoAgregado(),filtrosIES).ejecutar();
      }
      else if (ind==tam+2) {
        new VentanaRealizarConsultaVariablesRelevantes(VentanaPrincipal.getInstance().estaSeleccionandoAgregado(),filtrosIES).ejecutar();
      }
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
  public void actualizar() {
    actualizarLabelSeleccion();
  }
  private void actualizarLabelSeleccion() {
    labelSeleccion.setText(getStringSeleccion());
  }
  private String getStringSeleccion() {
    return RutinasGUI.getStringSeleccion(VentanaPrincipal.getInstance().getPanelsSeleccion(),null,VentanaPrincipal.getInstance().getCriterios(),null,VentanaPrincipal.getInstance().estaSeleccionandoAgregado());
  }
  private class VentanaRealizarConsultaBasica extends MyDialogProgreso {
    private final boolean alMinisterio;
    private final Variable variable;
    private final Filtro[] filtrosIES;
    public VentanaRealizarConsultaBasica(boolean pAlMinisterio, Variable pVariable, Filtro[] pFiltrosIES) {
      super(VentanaPrincipal.getInstance(),"Realizando la consulta"+(pAlMinisterio?" en el Ministerio de Educación Nacional":""),false);
      alMinisterio=pAlMinisterio;
      variable=pVariable;
      filtrosIES=pFiltrosIES;
    }
    public void ejecutar() {
      setVisible(true);
      new Thread() {
        public void run() {
          Window window=VentanaRealizarConsultaBasica.this;
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
      Object[] resultado=obtenerResultado(alMinisterio,variable,filtrosIES);
      int limsSems[]=(int[])(resultado[1]),limInf=limsSems[0],limSup=limsSems[1];
      int res[][]=(int[][])(resultado[0]);
      RangoByte<Byte> rango=(RangoByte<Byte>)(variable.rango);
      Byte[] codsRango=rango.getRango();
      int tam=limSup-limInf+1,max=rango.getMaxRango(),numCods=codsRango.length;
      double resPorc[][]=new double[max+2][tam];
      String valores[][]=new String[numCods][tam],valoresPorc[][]=new String[numCods][tam];
      String encCols[]=CajaDeHerramientas.textoSemestresToString(CajaDeHerramientas.getTextosSemestresEntre((byte)limInf,(byte)limSup)),encFils[]=new String[numCods+1];
      encFils[0]=variable.nombre;
      for (int g=0; g<numCods; g++) encFils[g+1]=rango.toString(codsRango[g]);
      int totalPeriodo[]=new int[tam];
      for (int j=limInf; j<=limSup; j++) for (int g=0; g<numCods; g++) {
        int v=res[codsRango[g]+1][j-limInf];
        valores[g][j-limInf]=""+v;
        if (codsRango[g]!=-1) totalPeriodo[j-limInf]+=v;
      }
      for (int j=limInf; j<=limSup; j++) for (int g=0; g<numCods; g++) if (codsRango[g]!=-1) {
        resPorc[codsRango[g]+1][j-limInf]=totalPeriodo[j-limInf]==0?0d:(1d*res[codsRango[g]+1][j-limInf]/totalPeriodo[j-limInf]);
        valoresPorc[g][j-limInf]=RutinasGUI.df_porcentaje.format(resPorc[codsRango[g]+1][j-limInf]);
      }
      String encColsPorc[]=encCols,encFilsPorc[]=encFils;
      if (codsRango[0]==-1) {
        encFilsPorc=CajaDeHerramientas.concatenarArreglos(String.class,new String[]{variable.nombre},CajaDeHerramientas.getSubArreglo(String.class,encFilsPorc,2,encFilsPorc.length-1));
        valoresPorc=CajaDeHerramientas.getSubArreglo(String[].class,valoresPorc,1,valoresPorc.length-1);
      }
      ChartPanel gdp=FabricaGraficas.crearGraficaInformacionBasica(variable,new Object[]{res,limsSems},false);
      ChartPanel gdpPorc=FabricaGraficas.crearGraficaInformacionBasica(variable,new Object[]{resPorc,limsSems},true);
      InfoTabla tabla=new InfoTabla(valores,new String[][]{encFils},encCols);
      InfoTabla tablaPorc=new InfoTabla(valoresPorc,new String[][]{encFilsPorc},encColsPorc);
      VentanaRealizarConsultaBasica.this.dispose();
      new VentanaGrafica(VentanaPrincipal.getInstance(),((variable.tipo==TipoVariable.TV_ESTUDIANTE)?"Diferenciación primíparos":"Apoyos engregados por semestre")+": "+variable.nombre,gdp,gdpPorc,tabla,tablaPorc,new MyEditorPane(false,getStringSeleccion())).setVisible(true);
    }
  }
  private class VentanaRealizarConsultaNivelAprobacionDesertores extends MyDialogProgreso {
    private final boolean alMinisterio;
    private final Filtro[] filtrosIES;
    public VentanaRealizarConsultaNivelAprobacionDesertores(boolean pAlMinisterio, Filtro[] pFiltrosIES) {
      super(VentanaPrincipal.getInstance(),"Realizando la consulta"+(pAlMinisterio?" en el Ministerio de Educación Nacional":""),false);
      alMinisterio=pAlMinisterio;
      filtrosIES=pFiltrosIES;
    }
    public void ejecutar() {
      setVisible(true);
      new Thread() {
        public void run() {
          Window window=VentanaRealizarConsultaNivelAprobacionDesertores.this;
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
      Object[] resultado=obtenerResultadoNivelAprobacionDesertores(alMinisterio,filtrosIES);
      int res[][]=(int[][])(resultado[0]);
      RangoByte<int[]> rangoNA=(RangoByte<int[]>)(Variable.NIVEL_APROBACION_PER.rango);
      Byte[] codsRango=rangoNA.getRango();
      int tam=res[0].length,max=rangoNA.getMaxRango(),numCods=codsRango.length,tot=0;
      double resPorc[][]=new double[max+2][tam];
      for (int i=0; i<tam; i++) for (byte b:codsRango) if (b!=-1) tot+=res[b+1][i];
      if (tot==0) throw new MyException("La consulta no arrojó datos.");
      for (int i=0; i<tam; i++) for (byte b:codsRango) if (b!=-1) resPorc[b+1][i]=1d*res[b+1][i]/tot;
      String valores[][]=new String[numCods][tam],valoresPorc[][]=new String[numCods][tam];
      String encCols[]=new String[tam],encFils[]=new String[numCods+1];
      for (int i=0; i<tam; i++) encCols[i]=""+(i+1);
      encFils[0]=Variable.NIVEL_APROBACION_PER.nombre;
      int ind=0;
      for (byte b:codsRango) {
        encFils[ind+1]=rangoNA.toString(b);
        for (int i=0; i<tam; i++) {
          valores[ind][i]=""+res[b+1][i];
          if (b!=-1) valoresPorc[ind][i]=RutinasGUI.df_porcentaje.format(resPorc[b+1][i]);
        }
        ind++;
      }
      String encColsPorc[]=encCols;
      String encFilsPorc[]=CajaDeHerramientas.concatenarArreglos(String.class,new String[]{encFils[0]},CajaDeHerramientas.getSubArreglo(String.class,encFils,2,encFils.length-1));
      valoresPorc=CajaDeHerramientas.getSubArreglo(String[].class,valoresPorc,1,valoresPorc.length-1);
      ChartPanel gdp=FabricaGraficas.crearGraficaNivelAprobacionDesertores(new Object[]{res,tam},false);
      ChartPanel gdpPorc=FabricaGraficas.crearGraficaNivelAprobacionDesertores(new Object[]{resPorc,tam},true);
      InfoTabla tabla=new InfoTabla(valores,new String[][]{encFils},encCols);
      InfoTabla tablaPorc=new InfoTabla(valoresPorc,new String[][]{encFilsPorc},encColsPorc);
      VentanaRealizarConsultaNivelAprobacionDesertores.this.dispose();
      new VentanaGrafica(VentanaPrincipal.getInstance(),"Clasificación desertores por nivel de aprobación",gdp,gdpPorc,tabla,tablaPorc,new MyEditorPane(false,getStringSeleccion())).setVisible(true);
    }
  } 
  private class VentanaRealizarConsultaPorcentajeDesercionPorCohorte extends MyDialogProgreso {
    private final boolean alMinisterio;
    private final Filtro[] filtrosIES;
    public VentanaRealizarConsultaPorcentajeDesercionPorCohorte(boolean pAlMinisterio, Filtro[] pFiltrosIES) {
      super(VentanaPrincipal.getInstance(),"Realizando la consulta"+(pAlMinisterio?" en el Ministerio de Educación Nacional":""),false);
      alMinisterio=pAlMinisterio;
      filtrosIES=pFiltrosIES;
    }
    public void ejecutar() {
      setVisible(true);
      new Thread() {
        public void run() {
          Window window=VentanaRealizarConsultaPorcentajeDesercionPorCohorte.this;
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
      DecimalFormat df_nodecimales = new DecimalFormat("0"); 
      Object[] resultado=obtenerResultadoPorcentajeDesercionPorCohorte(alMinisterio,filtrosIES, true);
      Object[] resultadoC=obtenerResultadoPorcentajeDesercionPorCohorte(alMinisterio,filtrosIES, false);
      double[][] vals=(double[][])(resultado[0]);
      double[][] valsC=(double[][])(resultadoC[0]);
      int limsSems[]=(int[])(resultado[1]),limInf=limsSems[0],limSup=limsSems[1],tam=limSup-limInf+1;
      String sems[]=CajaDeHerramientas.textoSemestresToString(CajaDeHerramientas.getTextosSemestresEntre((byte)limInf,(byte)limSup));
      String tabla[][]=new String[tam][tam];
      String tablaC[][]=new String[tam][tam];
      for (int j=0; j<tam; j++) for (int i=0; i<tam; i++) {
        tabla[j][i]=(vals[j][i]==Double.MAX_VALUE)?"":(RutinasGUI.df_porcentaje.format(vals[j][i]/100));
        tablaC[j][i]=(valsC[j][i]==Double.MAX_VALUE)?"":df_nodecimales.format(valsC[j][i]);
      }
      String encCols[]=new String[tam],encFils[]=CajaDeHerramientas.concatenarArreglos(String.class,new String[]{"Cohorte"},sems);
      for (int i=0; i<tam; i++) encCols[i]=""+(i+1);
      JPanel panel=new MyBorderPane(false,0,0,0,0,null,null,new MyPanelTabla(new InfoTabla(tabla,new String[][]{encFils},encCols),-1,-1,65),null,null);
      JPanel panelC=new MyBorderPane(false,0,0,0,0,null,null,new MyPanelTabla(new InfoTabla(tablaC,new String[][]{encFils},encCols),-1,-1,65),null,null);
      VentanaRealizarConsultaPorcentajeDesercionPorCohorte.this.dispose();
      //new VentanaResultadoPanel(VentanaPrincipal.getInstance(),"Porcentaje de deserción por cohorte",panel,new MyEditorPane(false,getStringSeleccion())).setVisible(true);
      new VentanaResultadoPanel(VentanaPrincipal.getInstance(),new String[]{"Porcentaje de deserción por cohorte","Conteo de deserción por cohorte"},new JPanel[]{panel,panelC},new MyEditorPane(false,getStringSeleccion())).setVisible(true);
    }
  } 
  private class VentanaRealizarConsultaVariablesRelevantes extends MyDialogProgreso {
    private final boolean alMinisterio;
    private final Filtro[] filtrosIES;
    public VentanaRealizarConsultaVariablesRelevantes(boolean pAlMinisterio, Filtro[] pFiltrosIES) {
      super(VentanaPrincipal.getInstance(),"Realizando la consulta"+(pAlMinisterio?" en el Ministerio de Educación Nacional":""),false);
      alMinisterio=pAlMinisterio;
      filtrosIES=pFiltrosIES;
    }
    public void ejecutar() {
      setVisible(true);
      new Thread() {
        public void run() {
          Window window=VentanaRealizarConsultaVariablesRelevantes.this;
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
      Object[][] resultado = (Object[][]) obtenerResultadoVariablesRelevantes(alMinisterio,filtrosIES);
      //String sems[]=CajaDeHerramientas.textoSemestresToString(CajaDeHerramientas.getTextosSemestresEntre((byte)limInf,(byte)limSup));
      List<Variable> varsApoyos = Arrays.asList(Variable.NUMERO_APOYOS_ACADEMICOS_RECIBIDOS_EST, Variable.NUMERO_APOYOS_FINANCIEROS_RECIBIDOS_EST, Variable.NUMERO_APOYOS_OTROS_RECIBIDOS_EST, Variable.NUMERO_APOYOS_ICETEX_RECIBIDOS_EST);
      String tabla1[][]=new String[resultado.length-varsApoyos.size()][3];
      String tabla2[][]=new String[varsApoyos.size()][3];
      DecimalFormat df=new DecimalFormat("0.00");
      {
        int pos1 = 0, pos2=0;
        for (int i=0,t=resultado.length;i<t;i++) {
          Variable var = (Variable)resultado[i][0];
          boolean esApo = varsApoyos.contains(var);
          (esApo?tabla2:tabla1)[esApo?pos2++:pos1++] = new String[]{var.nombre, var.rango.toString(var.rango.byteToRango(((byte[]) resultado[i][1])[0])), df.format((Double)resultado[i][2])};
        }
      }
      String encCols[]=new String[]{"Nombre", "Mayor", "Puntaje"};
      JPanel panel1=new MyBorderPane(false,0,0,0,0,null,null,new MyPanelTabla(new InfoTabla(tabla1,new String[][]{},encCols),-1,-1,250),null,null);
      JPanel panel2=new MyBorderPane(false,0,0,0,0,null,null,new MyPanelTabla(new InfoTabla(tabla2,new String[][]{},encCols),-1,-1,250),null,null);
      VentanaRealizarConsultaVariablesRelevantes.this.dispose();
      //new VentanaResultadoPanel(VentanaPrincipal.getInstance(),"Porcentaje de deserción por cohorte",panel,new MyEditorPane(false,getStringSeleccion())).setVisible(true);
      new VentanaResultadoPanel(VentanaPrincipal.getInstance(),new String[]{"Variables relevantes", "Variables relevantes - Apoyos"},new JPanel[]{panel1,panel2},new MyEditorPane(false,getStringSeleccion())).setVisible(true);
    }
  } 

  public static Object[] obtenerResultado(boolean alMinisterio, Variable variable, Filtro[] filtrosIES) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=KernelSPADIES.getInstance().getInformacionBasicaCriterio(filtrosIES,variable);
    }
    else {
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(0,Object[].class,filtrosIES,variable);
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
  public static Object[] obtenerResultadoNivelAprobacionDesertores(boolean alMinisterio, Filtro[] filtrosIES) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=KernelSPADIES.getInstance().getTablaNivelAprobacionDesertores(filtrosIES);
    }
    else {
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(7,Object[].class,new Object[]{filtrosIES});
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
  public static Object[] obtenerResultadoPorcentajeDesercionPorCohorte(boolean alMinisterio, Filtro[] filtrosIES, boolean porcentual) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=KernelSPADIES.getInstance().getPorcentajeDesercionPorCohorte(filtrosIES, porcentual);
    }
    else {
      //TODO Implementar consulta en red
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(3,Object[].class,new Object[]{filtrosIES, porcentual});
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
  public static Object[] obtenerResultadoVariablesRelevantes(boolean alMinisterio, Filtro[] filtrosIES) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=KernelSPADIES.getInstance().getVariablesRelevantes(filtrosIES);
    }
    else {
      //throw new MyException("Consulta no soportada sobre el agregado.");
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(13,Object[].class,new Object[]{filtrosIES});
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
}
