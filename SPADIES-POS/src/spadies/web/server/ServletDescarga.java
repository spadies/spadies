package spadies.web.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletDescarga extends ServletUsuario {
  private File archDescarga;
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    ServletContext context = getServletContext();
    archDescarga = new File(context.getInitParameter("spadies.descarga"));
  }
  public void doAll(HttpServletRequest req, HttpServletResponse res) throws IOException {
    //final Usuario usr = AuxiliaresConsulta.getUsuarioSesion(req);
    if (usuariosIES.isEmpty()) initUsuariosIES(new File(rutPass));
    res.addHeader("Cache-Control", "no-cache");
    res.addHeader("Pragma", "no-cache");
    String ies = req.getParameter("ies");
    String pwd = req.getParameter("pwd");
    if (ies!=null && pwd!=null) {
      /*Enumeration en = req.getParameterNames();
      System.out.println("--");
      while (en.hasMoreElements())
        System.out.println(en.nextElement()+"_");
      System.out.println("--X");
      System.out.println();*/
      String pwdd = ies!=null?usuariosIES.get(ies):null;
      System.out.println(usuariosIES);
      System.out.println(ies);
      System.out.println(pwd);
      if (pwd!=null && pwdd!=null && pwd.equals(pwdd)) {
        res.setContentType("application/zip");
        res.setHeader("Content-disposition", "attachment; filename="+archDescarga.getName());
        InputStream in = null;
        ServletOutputStream out = res.getOutputStream();
        try {
            in = new BufferedInputStream(new FileInputStream(archDescarga));
            int ch;
            while ((ch = in.read()) !=-1)
                out.print((char)ch);
        } finally {
            if (in != null) in.close();
        }
      } else {
        res.sendRedirect(req.getHeader("Referer"));
      }
    } else {
      res.setCharacterEncoding("UTF-8");
      res.setContentType("text/html");
    }
    /*if (req.getParameter("programas")==null) {
      String txt = ImprimirVariables.imprimirCategorias(CategoriasVariables.variablesEnCategorias(new FiltroVariables() {
        public boolean aceptarVariable(Variable v) {
          return filtroGeneral.aceptarVariable(v) && usr.tipo.fvar.aceptarVariable(v);
        }
      }));
      res.getOutputStream().write(txt.getBytes("UTF-8"));
    } else {
      String sfil = req.getParameter("filies");
      List<Item> items = new LinkedList<Item>();
      if (sfil!=null) {
        for (String sf:sfil.split(",")) {
          items.add(new Item(Integer.parseInt(sf), "", ""));
        }
      }
      Filtro[] filtrosIES = new Filtro[]{new Filtro(Variable.CODIGO_IES, items.toArray(new Item[0]))};
      StringBuilder sb  =new StringBuilder("[");
      boolean p = true;
      for (String pr:kernel.getProgramasActivos(filtrosIES)) if (pr.length()!=0 && !pr.matches("[\\d\\s]+")){
        if (!p) sb.append(",");
        else p = false;
        sb.append("\"");
        sb.append(pr);
        sb.append("\"");
      }
      sb.append("]");
      res.getOutputStream().write(sb.toString().getBytes("UTF-8"));
    }*/
  }
}
