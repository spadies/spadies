package spadies.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import spadies.util.variables.Item;
import spadies.util.variables.SCategoriasVariables;
import spadies.util.variables.SCategoriasVariables.BCategoria;
import spadies.util.variables.SFiltro;
import spadies.util.variables.SVariable;

public class SVariablesPanel extends JScrollPane{
	private static final long serialVersionUID = -6971371742855523615L;
	private JTree tree;
	private DefaultMutableTreeNode carpetaRaiz = new DefaultMutableTreeNode("Variables");
	private PanelFiltro[] panelesFiltros;
	public JScrollPane panelAuxiliar=new JScrollPane();
	public SVariablesPanel() {
		SVariable[] variables=SVariable.values();
		panelesFiltros=new PanelFiltro[variables.length];
		DefaultTreeModel modelo = new DefaultTreeModel(carpetaRaiz);
		tree=new JTree(modelo);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.putClientProperty("JTree.lineStyle", false);
		int pos = 0;
		for(BCategoria cat:SCategoriasVariables.variablesEnCategorias(SCategoriasVariables.TODO_VARIABLES)){
			DefaultMutableTreeNode carp = new DefaultMutableTreeNode(cat.nombre);
			modelo.insertNodeInto(carp, carpetaRaiz, pos++);
			for(SVariable var:cat.vars)modelo.insertNodeInto(new DefaultMutableTreeNode(var), carp, 0);
		}
		for(int s=0;s<variables.length;s++)panelesFiltros[s]=new PanelFiltro(variables[s]);
		tree.expandRow(0);
		tree.setSelectionRow(1);
		tree.addTreeSelectionListener(tsl);
		setViewportView(tree);
		panelAuxiliar.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panelAuxiliar.setViewportView(panelesFiltros[0]);
	}
	private TreeSelectionListener tsl=new TreeSelectionListener() {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode c=(DefaultMutableTreeNode)e.getPath().getLastPathComponent();
			if(c.getUserObject() instanceof SVariable){
				SVariable var=(SVariable)c.getUserObject();
				for(PanelFiltro fp:panelesFiltros)if(fp.variable==var){
					panelAuxiliar.setViewportView(fp);
					panelAuxiliar.repaint();
					break;
				}
			}
		}
	};
	public void limpiarSeleccion(){
		for(PanelFiltro pf:panelesFiltros)pf.limpiarSeleccion();
	}
	public SVariable[] getDiferenciados(){
		List<SVariable> ret=new LinkedList<>();
		for(PanelFiltro pf:panelesFiltros)if(pf.isDiferenciado())ret.add(pf.variable);
		return ret.toArray(new SVariable[0]);
	}
	public SFiltro[] getFiltros(){
		List<SFiltro> ret=new LinkedList<>();
		for(PanelFiltro pf:panelesFiltros)if(!pf.isComplete())ret.add(new SFiltro(pf.variable,pf.getSelectedItems()));
		return ret.toArray(new SFiltro[0]);
	}
	static class PanelFiltro extends JPanel{
		private static final long serialVersionUID = -1032908148708702520L;
		JCheckBox cbDiferenciado=new JCheckBox("Diferenciado"),checkBoxes[]; 
		SVariable variable;
		Item[] items;
		public PanelFiltro(SVariable variable){
			setLayout(new BorderLayout());
			this.variable=variable;
			items=variable.items;
			checkBoxes=new JCheckBox[items.length];
			JPanel aux=new JPanel(new GridLayout(items.length,1)),temp=new JPanel(new BorderLayout());
			for(int e=0;e<items.length;e++){
				checkBoxes[e]=new JCheckBox(items[e].value,true);
				checkBoxes[e].setBackground(Color.WHITE);
				aux.add(checkBoxes[e]);
			}
			
			aux.setBackground(Color.WHITE);
			temp.add(cbDiferenciado,BorderLayout.NORTH);
			temp.add(aux,BorderLayout.CENTER);
			add(temp,BorderLayout.NORTH);
			JPanel voidPanel=new JPanel();
			voidPanel.setBackground(Color.WHITE);
			add(voidPanel,BorderLayout.CENTER);
		}
		public boolean isDiferenciado(){
			return cbDiferenciado.isSelected();
		}
		public boolean isComplete(){
			for(JCheckBox jb:checkBoxes)if(!jb.isSelected())return false;
			return true;
		}
		public Item[] getSelectedItems(){
			List<Item> ret=new LinkedList<>();
			for(int e=0;e<items.length;e++)if(checkBoxes[e].isSelected())ret.add(items[e]);
			return ret.toArray(new Item[0]);
		}
		public void limpiarSeleccion(){
			cbDiferenciado.setSelected(false);
			for(int e=0;e<items.length;e++)checkBoxes[e].setSelected(true);
		}
	}
}
