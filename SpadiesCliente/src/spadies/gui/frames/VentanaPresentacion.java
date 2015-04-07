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

import java.text.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import spadies.gui.graficas.*;
import spadies.gui.imagenes.*;
import spadies.gui.util.*;
import spadies.kernel.*;
import spadies.util.*;
import spadies.util.variables.*;

@SuppressWarnings("serial")
public class VentanaPresentacion extends JFrame implements Observer {
  private static final Variable[] criterios={
    Variable.SEXO_EST,
    Variable.TRABAJABA_CUANDO_ICFES_EST,
    Variable.CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST,
    Variable.INGRESO_HOGAR_EST,
    Variable.VIVIENDA_PROPIA_EST,
    Variable.NUMERO_HERMANOS_EST,
    Variable.POSICION_ENTRE_LOS_HERMANOS_EST,
    Variable.CLASIFICACION_PUNTAJE_ICFES_EST,
    Variable.NIVEL_EDUCATIVO_MADRE_EST,
    Variable.AREA_CONOCIMIENTO_EST,
    Variable.ICETEX_RECIBIDO_PER,
    Variable.TIPO_ICETEX_RECIBIDO_PER,
    Variable.APOYO_RECIBIDO_PER};
  private static final KernelSPADIES kernel=KernelSPADIES.getInstance();
  private static final VentanaPresentacion instance=new VentanaPresentacion();
  public static VentanaPresentacion getInstance() {return instance;}
  private final MyPanelSeleccion panelsSeleccion1[]=new MyPanelSeleccion[1];
  private final MyPanelSeleccion panelsSeleccion2[]=new MyPanelSeleccion[3];
  private final Variable criterios1[]={Variable.CODIGO_IES};
  private final Variable criterios2[]={Variable.DEPARTAMENTO_IES,Variable.ORIGEN_IES,Variable.CARACTER_IES};
  private final JPanel panelIES,panelAgregado;
  private final JEditorPane campoHTML=new JEditorPane("text/html","");
  private final MyLabel labelNumDiapositiva=new MyLabel("0/0");
  private final JPanel panelObjetos=new JPanel();
  private final MyButton[] botones=new MyButton[3];
  private MyPanelSeleccion panelPeriodosCohortesAg=null;
  private String[] paginas={};
  private int indicePagina=0;
  private VentanaPresentacion() {
    super("PRESENTACIÓN");
    Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
    setIconImage(Imagenes.IM_ICONO_APLICACION.getImagen().getImage());
    kernel.addObserver(this);
    campoHTML.setEditable(false);
    botones[0]=new MyButton(Imagenes.IM_ANTERIOR.getImagen(),"Anterior diapositiva");
    botones[1]=new MyButton(Imagenes.IM_HOME.getImagen(),"Primera diapositiva");
    botones[2]=new MyButton(Imagenes.IM_SIGUIENTE.getImagen(),"Siguiente diapositiva");
    botones[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {avanzar(-1);}
    });
    botones[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {avanzar(-indicePagina);}
    });
    botones[2].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {avanzar(1);}
    });
    try {
      BufferedReader br=new BufferedReader(new InputStreamReader(VentanaPresentacion.class.getResourceAsStream("presentacion/presentacionSPADIES.html")));
      String pagina="";
      for (String s; (s=br.readLine())!=null; ) pagina+=s+"\r\n";
      br.close();
      pagina=cuadrarFuentePresentacion(cuadrarImagenesPresentacion(pagina,d),d);
      paginas=pagina.substring(pagina.indexOf("<body>")+6,pagina.indexOf("</body>")).split("\\Q<hr>\\E");
    }
    catch (Throwable th) {
      th.printStackTrace();
    }
    KeyListener kl=new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        int c=e.getKeyCode();
        if (c==KeyEvent.VK_KP_RIGHT || c==KeyEvent.VK_RIGHT || c==KeyEvent.VK_S)    avanzar(1);
        else if (c==KeyEvent.VK_KP_LEFT || c==KeyEvent.VK_LEFT || c==KeyEvent.VK_A) avanzar(-1);
      }
    };
    campoHTML.addKeyListener(kl);
    for (JButton b:botones) b.addKeyListener(kl);
    panelsSeleccion1[0]=new MyPanelSeleccion(criterios1[0].items,true,180,200);
    for (int i=0; i<3; i++) panelsSeleccion2[i]=new MyPanelSeleccion(criterios2[i].items,true,180,i==0?200:100);
    for (int i=1; i<3; i++) panelsSeleccion2[i].setMaximumSize(new Dimension(1000,100));
    panelIES=new MyBoxPane(BoxLayout.Y_AXIS,new MyFlowPane(0,0,new MyLabel("<html><i><b><font color=\"#700000\">"+CajaDeHerramientas.stringToHTML(criterios1[0].nombre)+"</font></b></i></html>")),panelsSeleccion1[0]);
    Collection<Component> componentes2=new LinkedList<Component>();
    componentes2.add(new MyFlowPane(0,0,new MyLabel("<html><i><b><font color=\"#700000\">Agregado</font></b></i></html>")));
    for (int i=0; i<3; i++) {
      if (i>0) componentes2.add(Box.createVerticalStrut(5));
      componentes2.add(new MyFlowPane(0,0,new MyLabel("<html><i><b><font color=\"#007000\">"+CajaDeHerramientas.stringToHTML(criterios2[i].nombre)+"</font></b></i></html>")));
      componentes2.add(panelsSeleccion2[i]);
    }
    panelAgregado=new MyBoxPane(BoxLayout.Y_AXIS,componentes2.toArray(new Component[0]));
    JSplitPane splitDerecha=new MySplitPane(JSplitPane.VERTICAL_SPLIT,false,panelIES,panelAgregado,true);
    JPanel panelBotones=new MyFlowPane(FlowLayout.CENTER,0,0,botones[0],Box.createHorizontalStrut(20),botones[1],Box.createHorizontalStrut(20),botones[2]);
    JPanel panelArriba=new MyBorderPane(false,0,0,0,0,null,new MyLabel("<html><font color=\"A00000\" style=\"font-size: 16\"><b><i>"+Constantes.nombreAplicacion+"</i></b></font></html>"),panelBotones,labelNumDiapositiva,null);
    JPanel panelPrincipal=new MyBorderPane(false,1,1,1,1,new MyBorderPane(false,0,0,0,0,panelArriba,null,campoHTML,null,null),null,panelObjetos,null,null);
    JSplitPane splitPrincipal=new MySplitPane(JSplitPane.HORIZONTAL_SPLIT,false,panelPrincipal,splitDerecha,true);
    blanquiarPanel(panelPrincipal);
    {
      setContentPane(splitPrincipal);
      setResizable(true);
      setSize(new Dimension(d.width,d.height));
      splitDerecha.setDividerLocation(200);
      splitPrincipal.setResizeWeight(1.0);
      splitPrincipal.setDividerLocation(getWidth()-140);
      setLocation((int)((d.width-getWidth())/2),(int)((d.height-getHeight())/2));
      setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }
    avanzar(0);
    TableModelListener tml=new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        avanzar(0);
      }
    };
    panelsSeleccion1[0].getTabla().getModel().addTableModelListener(tml);
    for (MyPanelSeleccion mps:panelsSeleccion2) mps.getTabla().getModel().addTableModelListener(tml);
  }
  private void blanquiarPanel(JPanel panel) {
    panel.setOpaque(true);
    panel.setBackground(Color.WHITE);
    for (int i=0,t=panel.getComponentCount(); i<t; i++) {
      Component c=panel.getComponent(i);
      if (c instanceof JPanel) blanquiarPanel((JPanel)c);
    }
  }
  private String cuadrarImagenesPresentacion(String s, Dimension tamPantalla) {
    String res="";
    int i=-1,j=0;
    while ((i=s.indexOf("src=\"",i+1))!=-1) {
      int k=s.indexOf("\"",i+5);
      res+=s.substring(j,i)+"src=\""+URLImagenes.map.get(s.substring(i+5,k))+"\"";
      j=i=k+1;
      i=s.indexOf("width=\"",i+1); k=s.indexOf("\"",i+7);
      res+=s.substring(j,i)+"width=\""+(tamPantalla.height*Integer.parseInt(s.substring(i+7,k))/700)+"\"";
      j=i=k+1;
      i=s.indexOf("height=\"",i+1); k=s.indexOf("\"",i+8);
      res+=s.substring(j,i)+"height=\""+(tamPantalla.height*Integer.parseInt(s.substring(i+8,k))/700)+"\"";
      j=i=k+1;
    }
    res+=s.substring(j);
    return res;
  }
  private String cuadrarFuentePresentacion(String s, Dimension tamPantalla) {
    String res="";
    int i=-1,j=0;
    while ((i=s.indexOf("size=\"",i+1))!=-1) {
      int k=s.indexOf("\"",i+6);
      res+=s.substring(j,i)+"style=\"font-size: "+getTamanhoFuente(Integer.parseInt(s.substring(i+6,k)),tamPantalla)+"\"";
      j=i=k+1;
    }
    res+=s.substring(j);
    return res;
  }
  private int getTamanhoFuente(int tam, Dimension tamPantalla) {
    switch (tam) {
      case 7: return tamPantalla.height*55/1024;
      case 6: return tamPantalla.height*45/1024;
      case 5: return tamPantalla.height*35/1024;
      default: return tamPantalla.height*25/1024;
    }
  }
  private Filtro[] getFiltroIES() throws MyException {
    EnumMap<Variable,Filtro> mFiltros1=new EnumMap<Variable,Filtro>(Variable.class);
    RutinasGUI.getSeleccion(criterios1,null,panelsSeleccion1,null,mFiltros1);
    return mFiltros1.values().toArray(new Filtro[0]);
  }
  private Filtro[] getFiltroAgregado() throws MyException {
    EnumMap<Variable,Filtro> mFiltros2=new EnumMap<Variable,Filtro>(Variable.class);
    RutinasGUI.getSeleccion(criterios2,null,panelsSeleccion2,null,mFiltros2);
    return mFiltros2.values().toArray(new Filtro[0]);
  }
  private Filtro[] getFiltro(int cual) throws Exception {
    return (cual==0)?getFiltroIES():getFiltroAgregado();
  }
  @SuppressWarnings("unchecked")
  private void avanzar(int delta) {
    if (paginas.length==0) return;
    indicePagina=Math.min(Math.max(indicePagina+delta,0),paginas.length-1);
    campoHTML.setText("<html>"+paginas[indicePagina]+"</html>");
    botones[0].setEnabled(indicePagina>0);
    botones[2].setEnabled(indicePagina<paginas.length-1);
    labelNumDiapositiva.setText("<html><font style=\"font-size: 16\"><b>"+(indicePagina+1)+"/"+paginas.length+"</b></font></html>");
    panelObjetos.removeAll();
    panelObjetos.revalidate();
    panelObjetos.repaint();
    int x=indicePagina+1;
    switch (x) {
      case 15:
        {
          Object resultados[][]=new Object[2][];
          for (int vez=0; vez<2; vez++) {
            try {
              resultados[vez]=getDatosTablaPresentacionModelo(vez==1,getFiltro(vez));
            }
            catch (MyException ex) {
              resultados[vez]=null;
            }
            catch (Throwable th) {
            }
          }
          IES iesSels[]=kernel.getIES(new Filtro[]{new Filtro(criterios1[0],panelsSeleccion1[0].getItemsSeleccionados())});
          int t=iesSels.length,codigosDeptos[]=new int[t];
          for (int i=0; i<t; i++) codigosDeptos[i]=iesSels[i].departamento;
          double[] datosDeptos=new double[0];
          try {
            datosDeptos=getDatosDepartamentos(codigosDeptos);
          }
          catch (Throwable th) {
          }
          JComponent panelHTML=new JPanel();
          try {
            long conteosSimples[][][]=new long[9][2][];
            for (int i=0; i<9; i++) {
              conteosSimples[i][0]=(resultados[0]==null)?new long[2]:((long[][])(resultados[0][0]))[i];
              conteosSimples[i][1]=(resultados[1]==null)?new long[2]:((long[][])(resultados[1][0]))[i];
            }
            DecimalFormat df1=new DecimalFormat("0.00%"),df2=new DecimalFormat("0.00");
            int tY=32,tX=6;
            String tabla[][]=new String[tY][tX]; int colspan[][]=new int[tY][tX],rowspan[][]=new int[tY][tX];
            tabla[0][0]="DETERMINANTES DE LA DESERCIÓN"; colspan[0][0]=6;
            tabla[1][0]=""; colspan[1][0]=3;
            tabla[1][3]="Efecto";
            tabla[1][4]="IES";
            tabla[1][5]="Agregado";
            tabla[2][0]="Individuales"; rowspan[2][0]=3;
            {
              tabla[2][1]="Sexo (masculino)"; colspan[2][1]=2;
              tabla[3][1]="Trabajaba al presentar el ICFES"; colspan[3][1]=2;
              tabla[4][1]="Edad (años)"; colspan[4][1]=2;
              tabla[2][3]="+";
              tabla[3][3]="+";
              tabla[4][3]="+";
              for (int u=0; u<2; u++) tabla[2][4+u]=toString(df1,conteosSimples[0][u],1.0);
              for (int u=0; u<2; u++) tabla[3][4+u]=toString(df1,conteosSimples[1][u],1.0);
              for (int u=0; u<2; u++) tabla[4][4+u]=toString(df2,conteosSimples[2][u],1.0);
            }
            tabla[5][0]="Nucleo familiar"; rowspan[5][0]=4;
            {
              tabla[5][1]="Ingreso familiar"; colspan[5][1]=2;
              tabla[6][1]="Vivienda propia"; colspan[6][1]=2;
              tabla[7][1]="Número de hermanos"; colspan[7][1]=2;
              tabla[8][1]="Nivel educativo de la madre"; colspan[8][1]=2;
              tabla[5][3]="-";
              tabla[6][3]="-";
              tabla[7][3]="+";
              tabla[8][3]="-";
              for (int u=0; u<2; u++) tabla[5][4+u]=toString(df2,conteosSimples[3][u],1.0);
              for (int u=0; u<2; u++) tabla[6][4+u]=toString(df1,conteosSimples[4][u],1.0);
              for (int u=0; u<2; u++) tabla[7][4+u]=toString(df2,conteosSimples[5][u],1.0);
              for (int u=0; u<2; u++) tabla[8][4+u]=toString(df2,conteosSimples[6][u],1.0);
            }
            tabla[9][0]="Académicos"; rowspan[9][0]=2;
            {
              tabla[9][1]="Tasa de repitencia"; colspan[9][1]=2;
              tabla[10][1]="Puntaje ICFES"; colspan[10][1]=2;
              tabla[9][3]="+";
              tabla[10][3]="-";
              for (int u=0; u<2; u++) tabla[9][4+u]=toString(df1,conteosSimples[8][u],0.0001);
              for (int u=0; u<2; u++) tabla[10][4+u]=toString(df2,conteosSimples[7][u],1.0)+"/100";
            }
            tabla[11][0]="Institucionales"; rowspan[11][0]=21;
            long arrICETEX1[][]=(resultados[0]==null)?new long[3][2]:((long[][])(resultados[0][1]));
            long arrICETEX2[][]=(resultados[1]==null)?new long[3][2]:((long[][])(resultados[1][1]));
            {
              tabla[11][1]="ICETEX"; rowspan[11][1]=3;
              tabla[11][2]="Largo";
              tabla[12][2]="Mediano";
              tabla[13][2]="ACCES";
              for (int v=0; v<3; v++) {
                tabla[11+v][3]="-";
                for (int u=0; u<2; u++) tabla[11+v][4+u]=toString(df1,(u==0?arrICETEX1:arrICETEX2)[v],1.0);
              }
            }
            long arrAPOYOS1[][]=(resultados[0]==null)?new long[3][2]:((long[][])(resultados[0][2]));
            long arrAPOYOS2[][]=(resultados[1]==null)?new long[3][2]:((long[][])(resultados[1][2]));
            {
              tabla[14][1]="Apoyos de la IES"; rowspan[14][1]=3;
              tabla[14][2]="Apoyo financiero";
              tabla[15][2]="Apoyo académico";
              tabla[16][2]="Otros apoyos";
              for (int v=0; v<3; v++) {
                tabla[14+v][3]="-";
                for (int u=0; u<2; u++) tabla[14+v][4+u]=toString(df1,(u==0?arrAPOYOS1:arrAPOYOS2)[v],1.0);
              }
            }
            long conteoAreas1[]=(long[])((resultados[0]==null)?new long[10]:resultados[0][3]);
            long conteoAreas2[]=(long[])((resultados[1]==null)?new long[10]:resultados[1][3]);
            Long totConteoAreas1=(Long)((resultados[0]==null)?((Long)0L):resultados[0][4]);
            Long totConteoAreas2=(Long)((resultados[1]==null)?((Long)0L):resultados[1][4]);
            {
              tabla[17][1]="Áreas de conocimiento"; rowspan[17][1]=9;
              String w[]={"+","+","+","-","+","++","+","++","+"};
              for (int a=0; a<9; a++) {
                tabla[17+a][2]=Variable.AREA_CONOCIMIENTO_EST.rango.toString((byte)(a+1));
                tabla[17+a][3]=w[a];
                Long c1=conteoAreas1[a+1];
                Long c2=conteoAreas2[a+1];
                tabla[17+a][4]=toString(df1,new long[]{(c1==null)?0L:c1,totConteoAreas1},1.0);
                tabla[17+a][5]=toString(df1,new long[]{(c2==null)?0L:c2,totConteoAreas2},1.0);
              }
            }
            long conteoCaracteresIES1[]=(long[])((resultados[0]==null)?new long[10]:resultados[0][5]);
            long conteoCaracteresIES2[]=(long[])((resultados[1]==null)?new long[10]:resultados[1][5]);
            @SuppressWarnings("unused")
            Long totConteoCaracteresIES1=(Long)((resultados[0]==null)?((Long)0L):resultados[0][6]);
            Long totConteoCaracteresIES2=(Long)((resultados[1]==null)?((Long)0L):resultados[1][6]);
            {
              tabla[26][1]="Carácter IES"; rowspan[26][1]=6;
              String w[]={"-","-","+","-","-","-"};
              int ind=0;
              for (int c:new int[]{1,2,3,4,5,9}) {
                tabla[26+ind][2]=Variable.CARACTER_IES.rango.toString((byte)c);
                tabla[26+ind][3]=w[ind];
                Long c1=conteoCaracteresIES1[c];
                Long c2=conteoCaracteresIES2[c];
                tabla[26+ind][4]=(c1!=null && c1.longValue()>0)?"<b>\u2713</b>":"";
                tabla[26+ind][5]=toString(df1,new long[]{(c2==null)?0L:c2,totConteoCaracteresIES2},1.0);
                ind++;
              }
            }
            StringBuffer sb=new StringBuffer();
            sb.append("<html>");
            sb.append("<table border=1 width=100% cellspacing=0 cellpadding=0>");
            for (int j=0; j<tY; j++) {
              sb.append("<tr>");
              for (int i=0; i<tX; i++) if (tabla[j][i]!=null) {
                int cols=Math.max(colspan[j][i],1),rows=Math.max(rowspan[j][i],1);
                sb.append("<td");
                if (j>1) switch (i+cols) {
                  case 1:
                    sb.append(" bgcolor=\"#A0A0A0\"");
                    break;
                  case 2:
                    sb.append(" bgcolor=\"#C0C0C0\"");
                    break;
                  case 3:
                    sb.append(" bgcolor=\"#E0E0E0\"");
                    break;
                }
                if (j==0) sb.append(" bgcolor=\"#909090\"");
                if (j==1 && i>=3) sb.append(" bgcolor=\"#D0D000\"");
                if (cols>1) sb.append(" colspan="+cols);
                if (rows>1) sb.append(" rowspan="+rows);
                sb.append(" align=center>");
                if (i<3 || j<=1) sb.append("<b>");
                sb.append(tabla[j][i]);
                if (i<3 || j<=1) sb.append("</b>");
                sb.append("</td>");
              }
              sb.append("</tr>");
            }
            sb.append("</table>");
            sb.append("<p>");
            sb.append("<table border=1 width=100% cellspacing=0 cellpadding=0>");
            sb.append("<tr><td bgcolor=\"#909090\" colspan=3 align=center><b>ENTORNO SOCIOECONÓMICO</b></td></tr>");
            sb.append("<tr><td bgcolor=\"#A0A0A0\" align=center><b>IES</b></td><td bgcolor=\"#A0A0A0\" align=center><b>Departamento al que<p>pertenece la IES</b></td><td bgcolor=\"#A0A0A0\" align=center><b>Tasa de desempleo<p>departamental (promedio)</b></td></tr>");
            for (int i=0; i<t; i++) {
              IES ies=iesSels[i];
              String nombreDepto=Variable.DEPARTAMENTO_IES.rango.toString(ies.departamento);
              double tasaDesempleoPromedioDepto=(datosDeptos==null || i>=datosDeptos.length)?Double.MAX_VALUE:datosDeptos[i];
              sb.append("<tr>");
              sb.append("<td>"+ies.codigo+" - <i>"+CajaDeHerramientas.stringToHTML(new String(ies.nombre))+"</i></td>");
              sb.append("<td align=center>"+CajaDeHerramientas.stringToHTML(nombreDepto)+"</td>");
              sb.append("<td align=center>"+CajaDeHerramientas.stringToHTML((tasaDesempleoPromedioDepto==Double.MAX_VALUE)?"-":df1.format(tasaDesempleoPromedioDepto))+"</td>");
              sb.append("</tr>");
            }
            sb.append("</table>");
            sb.append("</html>");
            panelHTML=new MyScrollPane(new MyEditorPane(false,sb.toString()),JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,-1,-1);
          }
          catch (Throwable th) {
            th.printStackTrace();
            panelHTML=new MyFlowPane(0,0,new MyLabel("Hubo un error realizando la consulta."));
          }
          panelObjetos.setLayout(new GridLayout(1,1));
          panelObjetos.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
          panelObjetos.add(panelHTML);
          blanquiarPanel(panelObjetos);
        }
        break;
      case 17:
        {
          Filtro[] filtroIES=new Filtro[]{new Filtro(criterios1[0],panelsSeleccion1[0].getItemsSeleccionados())};
          Object[] res=kernel.getTablaCantidadArchivos(filtroIES);
          int numEst=kernel.getCantidadEstudiantes(filtroIES);
          panelObjetos.setLayout(new BorderLayout());
          panelObjetos.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
          panelObjetos.add(new MyFlowPane(0,0,new MyLabel("<html>Número de estudiantes que aparecen como primíparos en las IES seleccionadas: "+(numEst==-1?"-":(""+numEst))+"<p><p><b>Conteo de filas en los archivos csv de entrada:</b></html>")),BorderLayout.NORTH);
          panelObjetos.add(new MyScrollPane(new MyPanelTabla(new InfoTabla((String[][])(res[0]),(String[][])(res[1]),(String[])(res[2])),500,300,120),JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,-1,-1),BorderLayout.CENTER);
          blanquiarPanel(panelObjetos);
        }
        break;
      case 18:
        {
          Filtro[] filtroIES=new Filtro[]{new Filtro(criterios1[0],panelsSeleccion1[0].getItemsSeleccionados())};
          Object[] res=kernel.cohortes(filtroIES);
          MyPanelTabla tabla=new MyPanelTabla(new InfoTabla((String[][])(res[0]),(String[][])(res[1]),(String[])(res[2])),500,300,60);
          panelObjetos.setLayout(new BorderLayout());
          panelObjetos.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
          panelObjetos.add(tabla,BorderLayout.CENTER);
          final TableCellRenderer rend=tabla.getTabla().getDefaultRenderer(String.class);
          tabla.getTabla().setDefaultRenderer(String.class,new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
              String s=val.toString();
              boolean b=s.startsWith("!");
              if (b) s=s.substring(1);
              Component c=rend.getTableCellRendererComponent(tbl,s,sel,foc,row,col);
              if (b) c.setBackground(new Color(255,215,215));
              return c;
            }
          });
          blanquiarPanel(panelObjetos);
        }
        break;
      case 19:
        {
          if (panelPeriodosCohortesAg==null) {
            Byte idsSems[]=AmbienteVariables.getInstance().obtenerIdsSemestresAgregado();
            Collection<Item> listaItems=new LinkedList<Item>();
            for (byte b:idsSems) if (b>=0) listaItems.add(new Item(b,CajaDeHerramientas.codigoSemestreToString(b),CajaDeHerramientas.stringToHTML(CajaDeHerramientas.codigoSemestreToString(b))));
            panelPeriodosCohortesAg=new MyPanelSeleccion(listaItems.toArray(new Item[0]),true,105,200,false);
            panelPeriodosCohortesAg.getTabla().getModel().addTableModelListener(new TableModelListener() {
              public void tableChanged(TableModelEvent e) {
                avanzar(0);
              }
            });
          }
          JPanel panelGrafica[]=new JPanel[2];
          for (int vez=0; vez<2; vez++) {
            panelGrafica[vez]=new JPanel();
            try {
              Filtro[] elFiltro=getFiltro(vez);
              if (vez==1) elFiltro=CajaDeHerramientas.concatenarArreglos(Filtro.class,elFiltro,new Filtro[]{new Filtro(Variable.PERIODO_MATRICULADO_PER,panelPeriodosCohortesAg.getItemsSeleccionados())});
              panelGrafica[vez]=getGraficaConsultaGrafica(vez==1,new Variable[]{Variable.PERIODO_INGRESO_EST},elFiltro);
            }
            catch (MyException ex) {
              panelGrafica[vez]=new MyFlowPane(0,0,new MyLabel(ex.getMessage()));
            }
            catch (Throwable th) {
            }
          }
          JPanel panelObjetos2=new JPanel();
          panelObjetos2.setLayout(new GridLayout(1,2));
          for (int vez=0; vez<2; vez++) panelObjetos2.add(new MyBoxPane(BoxLayout.Y_AXIS,Box.createVerticalGlue(),new MyFlowPane(FlowLayout.CENTER,0,0,new MyLabel("<html><font color=\"A00000\" style=\"font-size: 18\"><b>"+(vez==0?"IES":"Agregado")+"</b></font></html>")),panelGrafica[vez],Box.createVerticalGlue()));
          panelObjetos.setLayout(new BoxLayout(panelObjetos,BoxLayout.X_AXIS));
          panelObjetos.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
          panelObjetos.add(panelObjetos2);
          panelObjetos.add(panelPeriodosCohortesAg);
          blanquiarPanel(panelObjetos);
        }
        break;
      case 20:
        {
          JPanel panelTabla[]=new JPanel[2];
          for (int vez=0; vez<2; vez++) {
            panelTabla[vez]=new JPanel();
            try {
              Object[] res=getTablaPorcentajeDesercionPorCohorte(vez==1,getFiltro(vez));
              if (res==null) {
                panelTabla[vez]=new MyPanelTabla(new InfoTabla(null,null,null),-1,-1,65);
              }
              else {
                double[][] vals=(double[][])(res[0]);
                int limsSems[]=(int[])(res[1]),limInf=limsSems[0],limSup=limsSems[1],tam=limSup-limInf+1;
                String sems[]=CajaDeHerramientas.textoSemestresToString(CajaDeHerramientas.getTextosSemestresEntre((byte)limInf,(byte)limSup));
                String tabla[][]=new String[tam][tam];
                DecimalFormat df=new DecimalFormat("0.00");
                for (int j=0; j<tam; j++) for (int i=0; i<tam; i++) {
                  tabla[j][i]=(vals[j][i]==Double.MAX_VALUE)?"":(df.format(vals[j][i])+"%");
                }
                String encCols[]=new String[tam],encFils[]=CajaDeHerramientas.concatenarArreglos(String.class,new String[]{"Cohorte"},sems);
                for (int i=0; i<tam; i++) encCols[i]=""+(i+1);
                panelTabla[vez]=new MyPanelTabla(new InfoTabla(tabla,new String[][]{encFils},encCols),-1,-1,65);
              }
            }
            catch (MyException ex) {
              panelTabla[vez]=new MyFlowPane(0,0,new MyLabel(ex.getMessage()));
            }
            catch (Throwable th) {
            }
          }
          panelObjetos.setLayout(new GridLayout(2,1));
          panelObjetos.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
          for (int vez=0; vez<2; vez++) panelObjetos.add(new MyBoxPane(BoxLayout.Y_AXIS,Box.createVerticalGlue(),new MyFlowPane(FlowLayout.CENTER,0,0,new MyLabel("<html><font color=\"A00000\" style=\"font-size: 18\"><b>"+(vez==0?"IES":"Agregado")+"</b></font></html>")),panelTabla[vez],Box.createVerticalGlue()));
          blanquiarPanel(panelObjetos);
        }
        break;
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:
      case 26:
      case 27:
      case 28:
      case 29:
      case 30:
      case 31:
      case 32:
      case 33:
        {
          @SuppressWarnings("unused")
          int xIni=21,xFin=33;
          JPanel panelGrafica[]=new JPanel[2];
          for (int vez=0; vez<2; vez++) {
            panelGrafica[vez]=new JPanel();
            try {
              panelGrafica[vez]=getGraficaInformacionBasica(vez==1,getFiltro(vez),x-xIni);
            }
            catch (MyException ex) {
              panelGrafica[vez]=new MyFlowPane(0,0,new MyLabel(ex.getMessage()));
            }
            catch (Throwable th) {
            }
          }
          panelObjetos.setLayout(new GridLayout(1,2));
          panelObjetos.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
          for (int vez=0; vez<2; vez++) panelObjetos.add(new MyBoxPane(BoxLayout.Y_AXIS,Box.createVerticalGlue(),new MyFlowPane(FlowLayout.CENTER,0,0,new MyLabel("<html><font color=\"A00000\" style=\"font-size: 18\"><b>"+(vez==0?"IES":"Agregado")+"</b></font></html>")),panelGrafica[vez],Box.createVerticalGlue()));
          blanquiarPanel(panelObjetos);
        }
        break;
    }
  }
  private static String toString(DecimalFormat df, long[] cuenta, double factor) {
    return (cuenta[1]==0)?"-":(df.format(factor*cuenta[0]/cuenta[1]));
  }
  @SuppressWarnings("unchecked")
  private JPanel getGraficaInformacionBasica(boolean alMinisterio, Filtro[] filtrosIES, int indiceCriterio) throws MyException {
    Variable variable=criterios[indiceCriterio];
    Object[] resultado=PanelInformacionBasica.obtenerResultado(alMinisterio,variable,filtrosIES);
    int limsSems[]=(int[])(resultado[1]),limInf=limsSems[0],limSup=limsSems[1];
    int res[][]=(int[][])(resultado[0]);
    RangoByte<Byte> rango=(RangoByte<Byte>)(variable.rango);
    Byte[] codsRango=rango.getRango();
    int tam=limSup-limInf+1,max=rango.getMaxRango(),numCods=codsRango.length;
    double resPorc[][]=new double[max+2][tam];
    int totalPeriodo[]=new int[tam];
    for (int j=limInf; j<=limSup; j++) for (int g=0; g<numCods; g++) {
      int v=res[codsRango[g]+1][j-limInf];
      if (codsRango[g]!=-1) totalPeriodo[j-limInf]+=v;
    }
    for (int j=limInf; j<=limSup; j++) for (int g=0; g<numCods; g++) if (codsRango[g]!=-1) {
      resPorc[codsRango[g]+1][j-limInf]=totalPeriodo[j-limInf]==0?0d:(1d*res[codsRango[g]+1][j-limInf]/totalPeriodo[j-limInf]);
    }
    return FabricaGraficas.crearGraficaInformacionBasica(variable,new Object[]{resPorc,limsSems},true);
  }
  @SuppressWarnings("unchecked")
  private JPanel getGraficaConsultaGrafica(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) throws MyException {
    Object[] resultado=PanelConsultasGraficas.obtenerResultado(alMinisterio,diferenciados,filtros);
    Map<byte[],double[]> resP=(Map<byte[],double[]>)(resultado[1]);
    Integer[] codigosIESDif=(Integer[])(resultado[2]);
    String[] codigosProgramasDif=(String[])(resultado[3]);
    return FabricaGraficas.crearGraficaConteosPoblacion("",diferenciados,new Object[]{resP,codigosIESDif,codigosProgramasDif},true);
  }
  public static Object[] getTablaPorcentajeDesercionPorCohorte(boolean alMinisterio, Filtro[] filtrosIES) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=kernel.getPorcentajeDesercionPorCohorte(filtrosIES);
    }
    else {
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(3,Object[].class,new Object[]{filtrosIES});
    }
    return resultado;
  }
  public static Object[] getDatosTablaPresentacionModelo(boolean alMinisterio, Filtro[] filtrosIES) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=kernel.getDatosTablaPresentacionModelo(filtrosIES);
    }
    else {
      resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(4,Object[].class,new Object[]{filtrosIES});
    }
    return resultado;
  }
  public static double[] getDatosDepartamentos(int[] codigosDepartamentos) throws MyException {
    if (codigosDepartamentos.length==0) return new double[0];
    return PuertaAlServidorDeConsultas.obtenerResultadoConsulta(5,double[].class,new Object[]{codigosDepartamentos});
  }
  public void update(Observable obs, Object arg) {
    if (!arg.equals("CARGA")) return;
    setVisible(false);
    panelsSeleccion1[0].setValores(criterios1[0].items,true);
  }
}
