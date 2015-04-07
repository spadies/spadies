package spadies.web.server.util;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import spadies.gui.format.ResultadoConsulta;
import spadies.gui.util.InfoTabla;
import spadies.util.Constantes;
import spadies.util.variables.Filtro;
import spadies.util.variables.FiltroVariables;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;

public class AuxiliaresConsulta {
  private static final String str_path = "img/gen";
  private static final int img_width = 700, img_height = 480*8/10;
  //private static final File img_path = new File(str_path);
  public static String resultadoToJSON(Collection<ResultadoConsulta> resc, Variable[] diferenciados, Filtro[] filtros, String codigoConsulta, String path) {
    StringBuilder res = new StringBuilder();
    //boolean p = true;
    double rand = Math.random();
    int congra = 0;
    res.append("[\n");
    for (ResultadoConsulta resu:resc) {
      /*if (!p)
        res.append(",\n");
      else
        p = false;*/
      res.append("{\n");
      res.append("\"nombre\":\""+resu.nombre+"\",\n");
      if (resu.resultado.getClass()==InfoTabla.class) {
        InfoTabla it = (InfoTabla) resu.resultado;
        res.append("\"tipo\":\"t\",\n");
        res.append("\"valores\":"+it.toJSON()+"\n");
      } else if (resu.resultado.getClass()==ChartPanel.class) {
        ChartPanel cp = (ChartPanel) resu.resultado;
        //TODO quitar el nivel extram un JFreeChart sin su ChartPanel
        JFreeChart chart = cp.getChart();
        File f = new File(new File(path),"img_"+(congra++)+""+rand+".jpg");
        try {
          ImageIO.write(imagen(chart, img_width, img_height),"JPEG",f);
        } catch (IOException e) {
          e.printStackTrace();
        }
        res.append("\"tipo\":\"g\",\n");
        res.append("\"url\":\""+str_path+"/"+f.getName()+"\"\n");
      }
      res.append("},\n");
    }
    res.append("{\"tipo\":\"i\",\"nombre\":\"Exportación\",\"id\":\""+codigoConsulta+"\""+",\"seleccion\":"+textoSeleccion(diferenciados,filtros)+"}");
    res.append("]\n");
    return res.toString();
  }
  
  private static String textoSeleccion(Variable[] diferenciados,Filtro[] filtros) {
    StringBuilder sb = new StringBuilder();
    EnumMap<Variable, Object[]> sel = new EnumMap<Variable, Object[]>(Variable.class);
    for (Variable var:diferenciados) sel.put(var, new Object[]{});
    sb.append("{");
    
    sb.append("}");
    return sb.toString();
  }

  public static BufferedImage imagen(JFreeChart chart,int width, int height) {
    BufferedImage img =
      new BufferedImage(width, height,
      BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = img.createGraphics();
    chart.draw(g2, new Rectangle2D.Double(0, 0, width, height));
    g2.dispose();
    return img;
  }

  public static void setUsuarioSesion(HttpServletRequest req, Usuario usr) {
    HttpSession session = req.getSession(true);
    session.setAttribute("usuario",usr);
  }
  public static Usuario getUsuarioSesion(HttpServletRequest req) {
    HttpSession session = req.getSession(true);
    Usuario usr = (Usuario) session.getAttribute("usuario");
    if (usr==null) usr = Usuario.usuarioPublico;//new Usuario("Publico", Tipo.PUBLICO, new Filtro[]{new Filtro(Variable.CARACTER_IES,new Item[]{new Item((byte)1,"",""),new Item((byte)2,"",""),new Item((byte)3,"",""),new Item((byte)4,"","")})});
    return usr;
  }
  public static List<Filtro> leerFiltro(String str) {
    List<Filtro> res = new LinkedList<Filtro>();
    String[] spn = str.split("!");
    for (String sss:spn) if (sss.length()!=0) {
      String[] sp1 = sss.split("--");
      Variable var = Constantes.ord2Variable.get(Integer.parseInt(sp1[0]));
      if (var==null) continue;
      List<Item> items = new ArrayList<Item>();
      if (var==Variable.CODIGO_IES) {//Integer
        for (String sfil:sp1[1].split(",")) if (sfil.length()!=0) {
          items.add(new Item(Integer.parseInt(sfil),"",""));
        }
      } else if (var==Variable.PROGRAMA_EST) {//String
        for (String sfil:sp1[1].split(",")) if (sfil.length()!=0) {
          items.add(new Item(sfil,"",""));
        }
      } else {//Byte
        for (String sfil:sp1[1].split(",")) if (sfil.length()!=0) {
          items.add(new Item(Byte.parseByte(sfil),"",""));
        }
      }
      res.add(new Filtro(var,items.toArray(new Item[0])));
    }
    return res;
  }
  public static List<Variable> leerVariables(String str,FiltroVariables fvar) {
    List<Variable> res = new LinkedList<Variable>();
    String[] spn = str.split(",");
    for (String sss:spn)
      if (sss.length()!=0) {
        Variable var = Constantes.ord2Variable.get(Integer.parseInt(sss));
        if (var!=null && fvar.aceptarVariable(var))
          res.add(var);
      }
    return res;
  }
}
