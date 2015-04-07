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
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

@SuppressWarnings("serial")
public class MyPanelTabla extends JPanel {
  private final JTable tabla=new JTable();
  private final AbstractTableModel model;
  private InfoTabla infoTabla;
  private final MyButton botonExportarCSV=new MyButton("Exportar a formato CSV",null,0);
  private final int anchoColumnas;
  public MyPanelTabla(InfoTabla pInfoTabla, int prefWidth, int prefHeight, int pAnchoColumnas) {
    anchoColumnas=pAnchoColumnas;
    infoTabla=pInfoTabla;
    model=new AbstractTableModel(){
      public int getRowCount() {return infoTabla.getNumFilas()+1;}
      public int getColumnCount() {return infoTabla.getNumEncabezadosFilas()+infoTabla.getNumColumnas();}
      public Class<?> getColumnClass(int col) {return String.class;}
      public boolean isCellEditable(int row, int col) {return false;}
      public Object getValueAt(int row, int col) {
        if (row==0 && col>=infoTabla.getNumEncabezadosFilas()) return infoTabla.getEncabezadoColumna(col-infoTabla.getNumEncabezadosFilas());
        if (col<infoTabla.getNumEncabezadosFilas()) return infoTabla.getEncabezadoFila(row,col);
        return infoTabla.getValor(row-1,col-infoTabla.getNumEncabezadosFilas());
      }
    };
    tabla.setModel(model);
    tabla.setTableHeader(null);
    tabla.setCellSelectionEnabled(false);
    tabla.setSelectionBackground(Color.WHITE);
    tabla.setDefaultRenderer(String.class,new DefaultTableCellRenderer() {
      public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
        setHorizontalAlignment(JLabel.CENTER);
        if (row==0 && col==0 && infoTabla.esVacio()) {
          setBackground(new Color(180,180,180));
          setFont(new Font("Dialog",Font.BOLD|Font.ITALIC,12));
          setText("NO HAY DATOS EN LA TABLA");
          return this;
        }
        int tipo=(row==0)?((col<infoTabla.getNumEncabezadosFilas())?1:2):((col<infoTabla.getNumEncabezadosFilas())?2:3);
        setBackground((tipo==1)?new Color(185,185,185):((tipo==2)?new Color(205,205,205):Color.WHITE));
        setFont(new Font("Dialog",tipo<=2?Font.BOLD:Font.PLAIN,12));
        setText((val==null)?"":(""+val));
        return this;
      }
    });
    actualizarTabla();
    for (JButton b:Arrays.asList(botonExportarCSV)) RutinasGUI.configurarBoton(b,new Color(230,255,230),10,-1,16);
    botonExportarCSV.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        RutinasGUI.exportarTablaCSV(MyPanelTabla.this,model);
      }
    });
    JScrollPane panelTabla=new MyScrollPane(tabla,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,prefWidth,prefHeight);
    panelTabla.getViewport().setOpaque(true);
    panelTabla.getViewport().setBackground(Color.WHITE);
    setLayout(new BorderLayout());
    add(panelTabla,BorderLayout.CENTER);
    add(new MyBorderPane(false,0,0,0,0,botonExportarCSV,null,null,null,null),BorderLayout.SOUTH);
  }
  public void setDatos(InfoTabla pInfoTabla) {
    infoTabla=pInfoTabla;
    actualizarTabla();
  }
  private void actualizarTabla() {
    model.fireTableDataChanged();
    model.fireTableStructureChanged();
    tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    TableColumnModel tcm=tabla.getColumnModel();
    if (infoTabla.esVacio()) {
      RutinasGUI.setAnchoColumna(tcm,0,700);
      return;
    }
    FontMetrics fm=tabla.getFontMetrics(new Font("Dialog",Font.BOLD,12));
    for (int i=0,it=tcm.getColumnCount(),jt=model.getRowCount(),u=infoTabla.getNumEncabezadosFilas(); i<it; i++) {
      int ancho=(i<u?40:anchoColumnas);
      if (i<u) for (int j=0; j<jt; j++) ancho=Math.max(ancho,fm.stringWidth(model.getValueAt(j,i).toString())+5);
      RutinasGUI.setAnchoColumna(tcm,i,ancho);
    }
  }
  public JTable getTabla() {
    return tabla;
  }
}
