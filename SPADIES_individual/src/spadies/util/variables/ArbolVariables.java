package spadies.util.variables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import spadies.util.variables.CategoriasVariables.BCategoria;

public class ArbolVariables extends JTree {
  public static class NodoCategoriaVariable extends DefaultMutableTreeNode {
    String txt;
    public NodoCategoriaVariable(String txt, Collection<NodoVariable> vars) {
      this.txt = txt;
      for (NodoVariable nodo:vars) super.add(nodo);
    }
    public String toString() {return txt;}
    public Collection<Variable> diferenciados() {
      Collection<Variable> res = new LinkedList<Variable>();
      for (Object odmt:super.children) {
        NodoVariable nv = (NodoVariable) odmt;
        if (nv.diferenciado)
          res.add(nv.v);
      }
      return res;
    }
    public Collection<Filtro> filtrados() {
      Collection<Filtro> res = new LinkedList<Filtro>();
      for (Object odmt:super.children) {
        NodoVariable nv = (NodoVariable) odmt;
        Filtro fil = nv.getFiltro();
        if (fil!=null)
          res.add(fil);
      }
      return res;
    }
  }
  public static class NodoVariable extends DefaultMutableTreeNode {
    Variable v;
    boolean diferenciado = false;
    public NodoVariable(Variable v, Collection<NodoValorVariable> vvars) {
      this.v = v;
      for (NodoValorVariable nodo:vvars) super.add(nodo);
    }
    public String toString() {return v.nombre;}
    public Filtro getFiltro() {
      Collection<Comparable> dfil = new LinkedList<Comparable>();
      boolean todosFil = true; 
      if (!super.isLeaf()) for (Object odmt:super.children) {
        NodoValorVariable nvv = (NodoValorVariable) odmt;
        if (nvv.filtrado)
          dfil.add(nvv.valor);
        else
          todosFil = false;
      }
      if (todosFil)
        return null;
      else {
        Item[] items = new Item[dfil.size()];
        int i = 0;
        for (Comparable c:dfil) items[i++] = new Item(c, "", "");
        return new Filtro(v, items);
      }
    }
  }
  public static class NodoValorVariable extends DefaultMutableTreeNode {
    public final Comparable valor;
    public final String txt;
    boolean filtrado;
    private NodoValorVariable(Comparable valor, String txt, boolean filtrado) {
      this.valor = valor;
      this.txt = txt;
      this.filtrado = filtrado;
    }
    public String toString() {return txt;}
  }
  private static DefaultMutableTreeNode getEstructuraArbol(BCategoria [] categorias) {
    DefaultMutableTreeNode res = new DefaultMutableTreeNode();
    int k=0;
    for(BCategoria c :categorias) {
      Variable[] variables = c.vars;
      Collection<NodoVariable> vars = new LinkedList<NodoVariable>();
      for(Variable v:variables){
        Collection<NodoValorVariable> vvars = new LinkedList<NodoValorVariable>();
        for(Comparable cc: v.rango.getRango()){
          vvars.add(new NodoValorVariable(cc, v.rango.toString(cc), true));
        }
        vars.add(new NodoVariable(v,vvars));
      }
      res.add(new NodoCategoriaVariable(c.nombre,vars));
      k++;
    }
    return res;
  }
  public ArbolVariables(BCategoria [] categorias){
    super(getEstructuraArbol(categorias));
    ArbolVariablesRenderer renderer = new ArbolVariablesRenderer();
    super.setCellRenderer(renderer);
    super.setCellEditor(new CheckBoxNodeEditor(this));
    super.setEditable(true);
    super.setRootVisible(false);
  }
  
  public static void main(String[] args) {
    ArbolVariables arbol = new ArbolVariables(CategoriasVariables.variablesEnCategorias(CategoriasVariables.TODO_VARIABLES));
    JFrame pnlMain = new JFrame("Arbol Variables");//gui
    Container content = pnlMain.getContentPane(); //gui
    content.add(arbol); //gui
    pnlMain.setPreferredSize(new Dimension(500,500));  //gui
    pnlMain.setVisible(true);  //gui
  }
  
  static class ArbolVariablesRenderer implements TreeCellRenderer {
    private JCheckBox leafRenderer = new JCheckBox();

    private DefaultTreeCellRenderer nonLeafRenderer = new DefaultTreeCellRenderer();

    static final Color selectionBorderColor, selectionForeground, selectionBackground,
        textForeground, textBackground;
    static {
      selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
      selectionForeground = UIManager.getColor("Tree.selectionForeground");
      selectionBackground = UIManager.getColor("Tree.selectionBackground");
      textForeground = UIManager.getColor("Tree.textForeground");
      textBackground = UIManager.getColor("Tree.textBackground");
    }

    protected JCheckBox getLeafRenderer() {
      return leafRenderer;
    }

    public ArbolVariablesRenderer() {
      Font fontValue = UIManager.getFont("Tree.font");
      if (fontValue != null) {
        leafRenderer.setFont(fontValue);
      }
      Boolean booleanValue = (Boolean) UIManager
          .get("Tree.drawsFocusBorderAroundIcon");
      leafRenderer.setFocusPainted((booleanValue != null)
          && (booleanValue.booleanValue()));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row,
        boolean hasFocus) {
      Component returnValue;
      DefaultMutableTreeNode dmt = (DefaultMutableTreeNode) value;
      //Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (dmt instanceof NodoCategoriaVariable)
        return returnValue = nonLeafRenderer.getTreeCellRendererComponent(tree,
            value, selected, expanded, leaf, row, hasFocus);
      else {
        String stringValue = tree.convertValueToText(value, selected,
            expanded, leaf, row, false);
        leafRenderer.setText(stringValue);
        leafRenderer.setSelected(false);
        leafRenderer.setEnabled(tree.isEnabled());
        if (selected) {
          leafRenderer.setForeground(selectionForeground);
          leafRenderer.setBackground(selectionBackground);
        } else {
          leafRenderer.setForeground(textForeground);
          leafRenderer.setBackground(textBackground);
        }
        if (dmt instanceof NodoVariable) {
          NodoVariable node = (NodoVariable) dmt;
          leafRenderer.setText(node.toString());
          leafRenderer.setSelected(node.diferenciado);
        }
        else if (dmt instanceof NodoValorVariable) {
          NodoValorVariable node = (NodoValorVariable) dmt;
          leafRenderer.setText(node.toString());
          leafRenderer.setSelected(node.filtrado);
        }
        returnValue = leafRenderer;
      }
      return returnValue;
    }
  }

  static class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {
    ArbolVariablesRenderer renderer = new ArbolVariablesRenderer();
    ChangeEvent changeEvent = null;
    JTree tree;
    public CheckBoxNodeEditor(JTree tree) {
      this.tree = tree;
    }
    public Object getCellEditorValue() {
      JCheckBox checkbox = renderer.getLeafRenderer();
      if (lastValue instanceof NodoVariable)
        ((NodoVariable)lastValue).diferenciado = checkbox.isSelected();
      if (lastValue instanceof NodoValorVariable)
        ((NodoValorVariable)lastValue).filtrado = checkbox.isSelected();
      return lastValue;
    }
    public boolean isCellEditable(EventObject event) {
      boolean returnValue = false;
      if (event instanceof MouseEvent) {
        MouseEvent mouseEvent = (MouseEvent) event;
        TreePath path = tree.getPathForLocation(mouseEvent.getX(),
            mouseEvent.getY());
        if (path != null) {
          Object node = path.getLastPathComponent();
          if (node != null) {
            returnValue = (/*(treeNode.isLeaf()) && */(node instanceof NodoVariable) || (node instanceof NodoValorVariable));
          }
        }
      }
      return returnValue;
    }

    Object lastValue=null;
    public Component getTreeCellEditorComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row) {
      lastValue = value;

      Component editor = renderer.getTreeCellRendererComponent(tree, value,
          true, expanded, leaf, row, true);
      // editor always selected / focused
      ItemListener itemListener = new ItemListener() {
        public void itemStateChanged(ItemEvent itemEvent) {
          if (stopCellEditing()) {
            fireEditingStopped();
          }
        }
      };
      if (editor instanceof JCheckBox) {
        ((JCheckBox) editor).addItemListener(itemListener);
      }

      return editor;
    }
  }
  public Variable[] getDiferenciados() {
    List<Variable> res = new LinkedList<Variable>();
    DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) super.getModel().getRoot();
    for (Enumeration en1 = dmtn.children();en1.hasMoreElements();) {
      NodoCategoriaVariable ncv = (NodoCategoriaVariable) en1.nextElement();
      res.addAll(ncv.diferenciados());
    }
    return res.toArray(new Variable[res.size()]);
  }
  public Filtro[] getFiltrosVariable() {
    List<Filtro> res = new LinkedList<Filtro>();
    DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) super.getModel().getRoot();
    for (Enumeration en1 = dmtn.children();en1.hasMoreElements();) {
      NodoCategoriaVariable ncv = (NodoCategoriaVariable) en1.nextElement();
      res.addAll(ncv.filtrados());
    }
    return res.toArray(new Filtro[res.size()]);
  }
  
  public void repoblar(BCategoria [] categorias) {
    DefaultTreeModel dtm = (DefaultTreeModel) treeModel;
    dtm.setRoot(getEstructuraArbol(categorias));
    dtm.reload();
  }
}
