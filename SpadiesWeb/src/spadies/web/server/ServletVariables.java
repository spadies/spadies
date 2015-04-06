package spadies.web.server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spadies.util.variables.CategoriasVariables;
import spadies.util.variables.Filtro;
import spadies.util.variables.FiltroVariables;
import spadies.util.variables.ImprimirVariables;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;
import spadies.web.server.util.AuxiliaresConsulta;
import spadies.web.server.util.Usuario;

public class ServletVariables extends ServletSPADIES {
  private static FiltroVariables filtroGeneral = new FiltroVariables() {
    public boolean aceptarVariable(Variable v) {
      return true;//v!=Variable.PROGRAMA_EST;
    }
  };
  public void doAll(HttpServletRequest req, HttpServletResponse res) throws IOException {
    final Usuario usr = AuxiliaresConsulta.getUsuarioSesion(req);
    res.setCharacterEncoding("UTF-8");
    res.setContentType("text/javascript");
    res.addHeader("Cache-Control", "no-cache");
    res.addHeader("Pragma", "no-cache");
    if (req.getParameter("programas")==null) {
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
    }
  }
}
