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
import spadies.util.variables.*;

@SuppressWarnings("serial")
public final class MyPanelSeleccion extends JPanel {
  private final JTable tabla=new JTable();
  private final AbstractTableModel model;
  private Item[] items;
  private boolean[] escogidos;
  private final MyButton botonSeleccionarTodo=new MyButton("Seleccionar todo",null,0);
  private final MyButton botonLimpiarSeleccion=new MyButton("Limpiar selección",null,0);
  public MyPanelSeleccion(Item[] pItems, boolean seleccion, int prefWidth, int prefHeight) {
    this(pItems,seleccion,prefWidth,prefHeight,true);
  }
  public MyPanelSeleccion(Item[] pItems, boolean seleccion, int prefWidth, int prefHeight, boolean mostrarBotones) {
    items=pItems;
    escogidos=new boolean[items.length];
    Arrays.fill(escogidos,seleccion);
    model=new AbstractTableModel() {
      public int getRowCount() {return items.length;}
      public int getColumnCount() {return 2;}
      public Class<?> getColumnClass(int col) {return (col==0)?Boolean.class:String.class;};
      public boolean isCellEditable(int row, int col) {return col==0;}
      public Object getValueAt(int row, int col) {return (col==0)?(Boolean)escogidos[row]:("<html>"+items[row].toStringHTML()+"</html>");}
      public void setValueAt(Object val, int row, int col) {if (col==0) {escogidos[row]=(Boolean)val; fireTableCellUpdated(row,col);}}
    };
    tabla.setModel(model);
    tabla.setTableHeader(null);
    tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tabla.setSelectionBackground(Color.WHITE);
    tabla.setShowGrid(false);
    tabla.setDefaultRenderer(String.class,new DefaultTableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object val, boolean sel, boolean foc, int row, int col) {
        setToolTipText(col==1?val.toString():null);
        super.getTableCellRendererComponent(table,val,sel,foc,row,col);
        return this;
      }
    });
    actualizarTabla();
    for (JButton b:Arrays.asList(botonSeleccionarTodo,botonLimpiarSeleccion)) RutinasGUI.configurarBoton(b,new Color(230,255,230),10,-1,16);
    botonSeleccionarTodo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        seleccionar(true);
      }
    });
    botonLimpiarSeleccion.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        seleccionar(false);
      }
    });
    JScrollPane panelTabla=new MyScrollPane(tabla,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,prefWidth,prefHeight);
    panelTabla.getViewport().setOpaque(true);
    panelTabla.getViewport().setBackground(Color.WHITE);
    setLayout(new BorderLayout());
    add(panelTabla,BorderLayout.CENTER);
    if (mostrarBotones) add(new MyBorderPane(false,0,0,0,0,botonSeleccionarTodo,null,null,null,botonLimpiarSeleccion),BorderLayout.SOUTH);
  }
  public void seleccionar(boolean val) {
    Arrays.fill(escogidos,val);
    model.fireTableDataChanged();
  }
  /**
   * Asignar estado de seleccion a todos las opciones excepto la de indice <i>dif</i>
   * @param val
   * @param dif
   */
  public void seleccionar(boolean val, int dif) {
    Arrays.fill(escogidos,val);
    escogidos[dif] = !val;
    model.fireTableDataChanged();
  }
  public boolean[] getSeleccionados() {
    return escogidos;
  }
  public Item[] getItems() {
    return items;
  }
  public Item[] getItemsSeleccionados() {
    Collection<Item> c=new ArrayList<Item>(20);
    for (int i=0,t=items.length; i<t; i++) if (escogidos[i]) c.add(items[i]);
    return c.toArray(new Item[0]);
  }
  public void setValores(Item[] pItems, boolean pSeleccion) {
    items=pItems;
    escogidos=new boolean[items.length];
    Arrays.fill(escogidos,pSeleccion);
    actualizarTabla();
  }
  public void setValoresManteniendoSeleccion(Item[] pItems, boolean pSeleccion) {
    Comparable[] llavesAntiguasSeleccionadas=Item.getKeys(getItemsSeleccionados()),llavesAntiguas=Item.getKeys(items);
    items=pItems;
    escogidos=new boolean[items.length];
    for (int i=0,iT=items.length; i<iT; i++) {
      Comparable llave=items[i].key;
      if (Arrays.binarySearch(llavesAntiguas,llave)>=0) {
        escogidos[i]=(Arrays.binarySearch(llavesAntiguasSeleccionadas,llave)>=0);
      }
      else {
        escogidos[i]=pSeleccion;
      }
    }
    actualizarTabla();
  }
  private void actualizarTabla() {
    model.fireTableDataChanged();
    model.fireTableStructureChanged();
    tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    TableColumnModel tcm=tabla.getColumnModel();
    FontMetrics fm=tabla.getFontMetrics(new Font("Dialog",Font.PLAIN,12));
    for (int i=0,it=tcm.getColumnCount(),jt=model.getRowCount(),u=1; i<it; i++) {
      int ancho=(i<u?16:20);
      if (i>=u) for (int j=0; j<jt; j++) ancho=Math.max(ancho,fm.stringWidth(model.getValueAt(j,i).toString())+5);
      RutinasGUI.setAnchoColumna(tcm,i,ancho);
    }
  }
  public JTable getTabla() {
    return tabla;
  }
}
