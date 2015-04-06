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
package spadies.gui.util;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import spadies.util.*;
import spadies.util.variables.*;
import spadies.util.variables.CategoriasVariables.BCategoria;

public final class RutinasGUI {
  public static abstract class FileRunnable implements Runnable {
    public File file;
    public abstract void run();
  }
  private static final KeyboardFocusManager keyboardFocusManager=KeyboardFocusManager.getCurrentKeyboardFocusManager();
  public static final JFileChooser fcExportarTablaCSV=new JFileChooser(".");
  public static final JFileChooser fcExportarInfoCSV=new JFileChooser(".");
  public static final JFileChooser fcExportarReporteHTML=new JFileChooser(".");
  static {
    {
      fcExportarTablaCSV.addChoosableFileFilter(new MyFileFilter(null,"Todos los archivos (*.*)"));
      fcExportarTablaCSV.addChoosableFileFilter(new MyFileFilter(".csv","Archivos CSV (*.csv)"));
      fcExportarTablaCSV.setAcceptAllFileFilterUsed(false);
      fcExportarTablaCSV.setDialogTitle("Exportar tabla a CSV");
      fcExportarTablaCSV.setApproveButtonMnemonic(KeyEvent.VK_E);
      fcExportarTablaCSV.setApproveButtonToolTipText("Exportar la tabla a formato CSV en el archivo especificado.");
      fcExportarTablaCSV.setDialogType(JFileChooser.SAVE_DIALOG);
      fcExportarTablaCSV.setFileHidingEnabled(false);
      fcExportarTablaCSV.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fcExportarTablaCSV.setMultiSelectionEnabled(false);
    }
    {
      fcExportarInfoCSV.setDialogTitle("Exportar información a CSV");
      fcExportarInfoCSV.setApproveButtonMnemonic(KeyEvent.VK_E);
      fcExportarInfoCSV.setApproveButtonToolTipText("Exporta la información de las IES seleccionadas a formato CSV en la carpeta especificada.");
      fcExportarInfoCSV.setDialogType(JFileChooser.SAVE_DIALOG);
      fcExportarInfoCSV.setFileHidingEnabled(false);
      fcExportarInfoCSV.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fcExportarInfoCSV.setMultiSelectionEnabled(false);
    }
    {
      fcExportarReporteHTML.addChoosableFileFilter(new MyFileFilter(null,"Todos los archivos (*.*)"));
      fcExportarReporteHTML.addChoosableFileFilter(new MyFileFilter(".html","Archivos HTML (*.html)"));
      fcExportarReporteHTML.setAcceptAllFileFilterUsed(false);
      fcExportarReporteHTML.setDialogTitle("Exportar reporte a HTML");
      fcExportarReporteHTML.setApproveButtonMnemonic(KeyEvent.VK_E);
      fcExportarReporteHTML.setApproveButtonToolTipText("Exportar el reporte a formato HTML en el archivo especificado.");
      fcExportarReporteHTML.setDialogType(JFileChooser.SAVE_DIALOG);
      fcExportarReporteHTML.setFileHidingEnabled(false);
      fcExportarReporteHTML.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fcExportarReporteHTML.setMultiSelectionEnabled(false);
    }
  }
  public static void exportarTablaGEN(Component componentePadre, FileRunnable proc) {
    if (fcExportarTablaCSV.showDialog(componentePadre,"Exportar")!=JFileChooser.APPROVE_OPTION) return;
    File f=fcExportarTablaCSV.getSelectedFile();
    if (!f.exists() && !f.getName().toLowerCase().endsWith(".csv")) f=new File(f.getParentFile(),f.getName()+".csv");
    if (f.exists() && !desplegarPregunta(componentePadre,"<html>"+CajaDeHerramientas.stringToHTML("\""+f.getPath()+"\" ya existe. ¿Desea reemplazar tal archivo?")+"</html>")) return;
    try {
      proc.file = f;
      proc.run();
      desplegarInformacion(componentePadre,"<html>La tabla se exportó exitosamente a formato CSV en el archivo especificado.</html>");
    }
    catch (Throwable th) {
      desplegarError(componentePadre,"<html>Hubo un error escribiendo la tabla en el archivo especificado</html>");
    }
  }
  public static void exportarTablaCSV(Component componentePadre, final TableModel model) {
    exportarTablaGEN(componentePadre, new FileRunnable() {
      public void run() {
        try {
          FileOutputStream fos=new FileOutputStream(file);
          String w[]=new String[model.getColumnCount()];
          for (int j=0,jt=model.getRowCount(),it=model.getColumnCount(); j<jt; j++) {
            for (int i=0; i<it; i++) w[i]=(String)(model.getValueAt(j,i));
            fos.write((CajaDeHerramientas.stringToCSV(w)+"\r\n").getBytes());
          }
          fos.close();
        } catch (Throwable t) {
          throw new Error(t);
        }
      }
    });
  }
  public static Component getComponenteConFoco() {
    return keyboardFocusManager.getFocusOwner();
  }
  public static Window getWindowForComponent(Component componente, boolean retornarRootFrame) {
    for (Component cm=componente; cm!=null; cm=cm.getParent()) if (cm instanceof Window) return ((Window)cm);
    return (retornarRootFrame?JOptionPane.getRootFrame():null);
  }
  public static void configurarBoton(JButton boton, Color color, int tamanhoFuente, int width, int height) {
    boton.setOpaque(true);
    boton.setBackground(color);
    boton.setFont(boton.getFont().deriveFont(1.0f*tamanhoFuente));
    Dimension d=new Dimension((width==-1)?boton.getPreferredSize().width:width,(height==-1)?boton.getPreferredSize().height:height);
    boton.setPreferredSize(d);
    boton.setMinimumSize(d);
    boton.setMaximumSize(d);
  }
  public static void getSeleccion(Variable[] criterios, JCheckBox[] checksDiferenciacion, MyPanelSeleccion[] panelsSeleccion, EnumSet<Variable> mDiferenciados, EnumMap<Variable,Filtro> mFiltros) throws MyException {
    for (int i=0,t=panelsSeleccion.length; i<t; i++) {
      Variable v=criterios[i];
      if (v.items.length==0 && v.esVariableExtra()) continue;
      if (checksDiferenciacion!=null && checksDiferenciacion[i]!=null && mDiferenciados!=null && checksDiferenciacion[i].isSelected()) mDiferenciados.add(v);
      Item[] items=panelsSeleccion[i].getItems(),itemsSels=panelsSeleccion[i].getItemsSeleccionados();
      if (itemsSels.length==0) throw new MyException("Debe escoger por lo menos un valor en la lista \""+v.nombre+"\".");
      if (itemsSels.length!=items.length) mFiltros.put(v,new Filtro(v,itemsSels));
    }
  }
  public static void getSeleccion(Variable[] criterios, JCheckBox[] checksDiferenciacionX, JCheckBox[] checksDiferenciacionY, MyPanelSeleccion[] panelsSeleccion, EnumSet<Variable> mDiferenciadosX, EnumSet<Variable> mDiferenciadosY, EnumMap<Variable,Filtro> mFiltros) throws MyException {
    for (int i=0,t=panelsSeleccion.length; i<t; i++) {
      Variable v=criterios[i];
      if (v.items.length==0 && v.esVariableExtra()) continue;
      if (checksDiferenciacionX!=null && checksDiferenciacionX[i]!=null && mDiferenciadosX!=null && checksDiferenciacionX[i].isSelected()) mDiferenciadosX.add(v);
      if (checksDiferenciacionY!=null && checksDiferenciacionY[i]!=null && mDiferenciadosY!=null && checksDiferenciacionY[i].isSelected()) mDiferenciadosY.add(v);
      Item[] items=panelsSeleccion[i].getItems(),itemsSels=panelsSeleccion[i].getItemsSeleccionados();
      if (itemsSels.length==0) throw new MyException("Debe escoger por lo menos un valor en la lista \""+v.nombre+"\".");
      if (itemsSels.length!=items.length) mFiltros.put(v,new Filtro(v,itemsSels));
    }
  }
  public static String getStringSeleccion(MyPanelSeleccion[] arrPS, JCheckBox[] arrCB, Variable[] arrVE, String[] filtroEspecial, boolean seleccionAgregada) {
    return getStringSeleccion("",arrPS,arrCB,arrVE,filtroEspecial,seleccionAgregada);
  }
  public static String getStringSeleccion(MyPanelSeleccion[] arrPS, JCheckBox[] arrCBX, JCheckBox[] arrCBY, Variable[] arrVE, String[] filtroEspecial, boolean seleccionAgregada) {
    //return getStringSeleccion("",arrPS,arrCB,arrVE,filtroEspecial,seleccionAgregada);
    return getStringSeleccion("",arrPS,arrCBX,arrCBY,arrVE,filtroEspecial,seleccionAgregada);
  }
  public static String getStringSeleccion(String aviso, MyPanelSeleccion[] arrPS, JCheckBox[] arrCB, Variable[] arrVE, String[] filtroEspecial, boolean seleccionAgregada) {
    StringBuffer sb=new StringBuffer();
    sb.append("<html>");
    sb.append(aviso);
    sb.append("<b>Parámetros de la selección</b> <i>(se omiten los campos "+((arrCB!=null)?"no diferenciados y ":"")+"con todos sus valores seleccionados)</i><p>");
    sb.append("<p><b>Tipo de análisis:</b> "+(seleccionAgregada?"Consulta agregado nacional":"Consulta institucional")+"<p>");
    for (int i=0,t=arrPS.length; i<t; i++) {
      Item[] items=arrPS[i].getItems(),itemsSels=arrPS[i].getItemsSeleccionados();
      if (items.length==0 && arrVE[i].esVariableExtra()) continue;
      boolean dif=(arrCB!=null && arrCB[i]!=null && arrCB[i].isSelected()),selNada=(itemsSels.length==0),selTodo=(itemsSels.length==items.length);
      if (!dif && selTodo) continue;
      sb.append("<p><font color=\"000080\"><b>"+CajaDeHerramientas.stringToHTML(arrVE[i].nombre)+"</b></font>"+(dif?(" <font color=\"808000\"><b>[Diferenciado]</b></font>"):"")+"<font color=\"008000\">");
      sb.append(CajaDeHerramientas.stringToHTML("  "));
      if (selNada) {
        sb.append(CajaDeHerramientas.stringToHTML("NINGUNO"));
      }
      else if (selTodo) {
        sb.append(CajaDeHerramientas.stringToHTML("TODOS"));
      }
      else {
        int ind=0;
        for (Item it:itemsSels) sb.append(((ind++)>0?", ":"")+CajaDeHerramientas.stringToHTML(it.toString()));
      }
      sb.append("</font>");
    }
    if (filtroEspecial!=null) {
      if (filtroEspecial[0].length()>0) sb.append("<p><font color=\"000080\"><b>Apellidos y nombres que contengan el texto:</b></font><p><font color=\"008000\">"+CajaDeHerramientas.stringToHTML("  "+filtroEspecial[0])+"</font>");
      if (filtroEspecial[1].length()>0) sb.append("<p><font color=\"000080\"><b>Documentos que contengan el texto:</b></font><p><font color=\"008000\">"+CajaDeHerramientas.stringToHTML("  "+filtroEspecial[1])+"</font>");
      if (filtroEspecial[2].length()>0) sb.append("<p><font color=\"000080\"><b>Códigos en la IES que contengan el texto:</b></font><p><font color=\"008000\">"+CajaDeHerramientas.stringToHTML("  "+filtroEspecial[2])+"</font>");
    }
    sb.append("</html>");
    return sb.toString();
  }
  public static String getStringSeleccion(String aviso, MyPanelSeleccion[] arrPS, JCheckBox[] arrCBX, JCheckBox[] arrCBY, Variable[] arrVE, String[] filtroEspecial, boolean seleccionAgregada) {
    StringBuffer sb=new StringBuffer();
    sb.append("<html>");
    sb.append(aviso);
    sb.append("<b>Parámetros de la selección</b> <i>(se omiten los campos "+((arrCBX!=null)?"no diferenciados y ":"")+"con todos sus valores seleccionados)</i><p>");
    sb.append("<p><b>Tipo de análisis:</b> "+(seleccionAgregada?"Consulta agregado nacional":"Consulta institucional")+"<p>");
    for (int i=0,t=arrPS.length; i<t; i++) {
      Item[] items=arrPS[i].getItems(),itemsSels=arrPS[i].getItemsSeleccionados();
      if (items.length==0 && arrVE[i].esVariableExtra()) continue;
      boolean selNada=(itemsSels.length==0),selTodo=(itemsSels.length==items.length);
      boolean difX =(arrCBX!=null && arrCBX[i]!=null && arrCBX[i].isSelected());
      boolean difY =(arrCBY!=null && arrCBY[i]!=null && arrCBY[i].isSelected());
      if (!difX && !difY && selTodo) continue;
      sb.append("<p><font color=\"000080\"><b>"+CajaDeHerramientas.stringToHTML(arrVE[i].nombre)+"</b></font>"+
          (difX?(" <font color=\"808000\"><b>[filas]</b></font>"):"")+
          (difY?(" <font color=\"808000\"><b>[columnas]</b></font>"):"")+
          "<font color=\"008000\">");
      sb.append(CajaDeHerramientas.stringToHTML("  "));
      if (selNada) {
        sb.append(CajaDeHerramientas.stringToHTML("NINGUNO"));//TODO ???
      }
      else if (selTodo) {
        sb.append(CajaDeHerramientas.stringToHTML("TODOS"));
      }
      else {
        int ind=0;
        for (Item it:itemsSels) sb.append(((ind++)>0?", ":"")+CajaDeHerramientas.stringToHTML(it.toString()));
      }
      sb.append("</font>");
    }
    if (filtroEspecial!=null) {
      if (filtroEspecial[0].length()>0) sb.append("<p><font color=\"000080\"><b>Apellidos y nombres que contengan el texto:</b></font><p><font color=\"008000\">"+CajaDeHerramientas.stringToHTML("  "+filtroEspecial[0])+"</font>");
      if (filtroEspecial[1].length()>0) sb.append("<p><font color=\"000080\"><b>Documentos que contengan el texto:</b></font><p><font color=\"008000\">"+CajaDeHerramientas.stringToHTML("  "+filtroEspecial[1])+"</font>");
      if (filtroEspecial[2].length()>0) sb.append("<p><font color=\"000080\"><b>Códigos en la IES que contengan el texto:</b></font><p><font color=\"008000\">"+CajaDeHerramientas.stringToHTML("  "+filtroEspecial[2])+"</font>");
    }
    sb.append("</html>");
    return sb.toString();
  }
  public static void setAnchoColumna(TableColumnModel tcm, int col, int ancho) {
    TableColumn c=tcm.getColumn(col);
    c.setPreferredWidth(ancho);
    c.setMinWidth(ancho);
    c.setMaxWidth(ancho);
  }
  public static int desplegarOptionPane(Component componentePadre, Component componenteDebajo, String mensajeHTML, int tipoOpcion, int tipoMensaje, String[] opciones, int ancho, int alto) {
    MyEditorPane ep=new MyEditorPane(true,mensajeHTML);
    MyScrollPane sp=new MyScrollPane(ep,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,(ancho!=-1)?ancho:350,(alto!=-1)?alto:80);
    sp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),BorderFactory.createEtchedBorder()));
    JOptionPane optionPane=new JOptionPane(sp,tipoMensaje,tipoOpcion,null,opciones,opciones[0]);
    Dialog dialog=optionPane.createDialog(componentePadre,Constantes.nombreAplicacion);
    Component cm=componenteDebajo;
    if (cm!=null) {
      Point punto=cm.getLocationOnScreen();
      Component cmP=cm.getParent();
      if ((cmP!=null)&&(cmP instanceof JViewport)) {
        cmP=cmP.getParent();
        if ((cmP!=null)&&(cmP instanceof JScrollPane)) punto=cmP.getLocationOnScreen();
      }
      Dimension tamPantalla=Toolkit.getDefaultToolkit().getScreenSize(),tamDialog=dialog.getSize();
      int pX=Math.min(Math.max(punto.x,0),tamPantalla.width-tamDialog.width),pY=punto.y-tamDialog.height;
      if (pY<0) pY=punto.y+cm.getHeight();
      pY=Math.min(pY,tamPantalla.height-tamDialog.height);
      dialog.setLocation(pX,pY);
    }
    dialog.setVisible(true);
    return CajaDeHerramientas.searchSame(opciones,optionPane.getValue());
  }
  public static int desplegarOptionPane(Component componentePadre, String mensajeHTML, int tipoOpcion, int tipoMensaje, String[] opciones) {
    return desplegarOptionPane(componentePadre,null,mensajeHTML,tipoOpcion,tipoMensaje,opciones,-1,-1);
  }
  public static int desplegarPregunta(Component componentePadre, String mensajeHTML, boolean puedeCancelar) {
    return desplegarOptionPane(componentePadre,mensajeHTML,puedeCancelar?JOptionPane.YES_NO_CANCEL_OPTION:JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,puedeCancelar?(new String[]{"Si","No","Cancelar"}):(new String[]{"Si","No"}));
  }
  public static boolean desplegarPregunta(Component componentePadre, String mensajeHTML) {
    return (desplegarPregunta(componentePadre,mensajeHTML,false)==0);
  }
  public static void desplegarInformacion(Component componentePadre, String mensajeHTML) {
    desplegarOptionPane(componentePadre,mensajeHTML,JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,new String[]{"Aceptar"});
  }
  public static void desplegarError(Component componentePadre, String mensajeHTML) {
    desplegarOptionPane(componentePadre,mensajeHTML,JOptionPane.DEFAULT_OPTION,JOptionPane.ERROR_MESSAGE,new String[]{"Aceptar"});
  }
  public static CategorizacionVariables categorizacionVariables(final boolean dinamicas) {
    final Variable[] extras1 = new Variable[]{
        Variable.VARIABLE_EXTRA_1_EST, Variable.VARIABLE_EXTRA_2_EST,
        Variable.VARIABLE_EXTRA_3_EST, Variable.VARIABLE_EXTRA_4_EST,
        Variable.VARIABLE_EXTRA_5_EST};
    final Variable[] extras2 = new Variable[]{
        Variable.VARIABLE_EXTRA_1_EST, Variable.VARIABLE_EXTRA_2_EST,
        Variable.VARIABLE_EXTRA_3_EST, Variable.VARIABLE_EXTRA_4_EST,
        Variable.VARIABLE_EXTRA_5_EST,
        Variable.VARIABLE_EXTRA_1_PEST, Variable.VARIABLE_EXTRA_2_PEST,
        Variable.VARIABLE_EXTRA_3_PEST, Variable.VARIABLE_EXTRA_4_PEST,
        Variable.VARIABLE_EXTRA_5_PEST};
    final Variable[] extras = dinamicas?extras2:extras1;
    BCategoria[] catvar = CategoriasVariables.variablesEnCategorias(new FiltroVariables() {
      public boolean aceptarVariable(Variable v) {
        return v!=Variable.CODIGO_IES && ((v.tipo!=TipoVariable.TV_PERIODO_ESTUDIANTE)||dinamicas);
      }
    });
    java.util.List<String> titulos = new LinkedList<String>();
    java.util.List<Variable> criterios = new LinkedList<Variable>();
    java.util.List<int[]> lims = new LinkedList<int[]>();
    int pos = 0;
    for (BCategoria cat:catvar) {
      titulos.add(cat.nombre);
      int minpos = pos, maxpos = -1;
      for (Variable v:cat.vars) {
        criterios.add(v);
        maxpos = pos++;
      }
      lims.add(new int[]{minpos,maxpos});
    }
    {
      titulos.add(null);
      int minpos = pos, maxpos = -1;
      for (Variable v:extras) {
        criterios.add(v);
        maxpos = pos++;
      }
      lims.add(new int[]{minpos,maxpos});
    }
    return new CategorizacionVariables(lims.toArray(new int[0][0]), criterios.toArray(new Variable[0]), titulos.toArray(new String[0]),extras.length);
  }
  public static class CategorizacionVariables {
    public final int lims[][];
    public final Variable[] criterios;
    public final String titulos[];
    public final int textras;
    public CategorizacionVariables(int lims[][], Variable[] criterios, String titulos[], int textras) {
      this.lims = lims;
      this.criterios = criterios;
      this.titulos = titulos;
      this.textras = textras;
    }
  }
  public static final DecimalFormat df_porcentaje=new DecimalFormat("0.00%");
}
