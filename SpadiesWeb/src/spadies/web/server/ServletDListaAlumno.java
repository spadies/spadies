package spadies.web.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spadies.kernel.EstudianteDAO;
import spadies.util.CajaDeHerramientas;
import spadies.util.MyException;
import spadies.util.variables.Filtro;
import spadies.web.server.util.AuxiliaresConsulta;
import spadies.web.server.util.Usuario;
import spadies.web.server.util.Usuario.Tipo;
//TODO Le pueden meter JavaScript en los parametros!
public class ServletDListaAlumno extends ServletSPADIES {
  private static final int respag = 10;
  //private static String rutaImagenes = null;
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    ServletContext context = getServletContext();
    //context.getInitParameter("spadies.passwords");
    //TODO potencialmente se asigna la variable varias veces
    //if (rutaImagenes==null) rutaImagenes = context.getInitParameter("spadies.web_img");
  }
  public void doAll(HttpServletRequest req, HttpServletResponse res) throws IOException {
    Usuario usr = AuxiliaresConsulta.getUsuarioSesion(req);
    if (usr.tipo!=Tipo.IES && usr.tipo!=Tipo.MEN) {
      res.setStatus(401);
      return;
    }
    res.setCharacterEncoding("UTF-8");
    res.setContentType("text/html");
    res.addHeader("Cache-Control", "no-cache");
    res.addHeader("Pragma", "no-cache");
    //int ies = usr.getIES();
    int paginaActual = 1;
    try {
      paginaActual = Integer.parseInt(req.getParameter("pag"));
    } catch (NumberFormatException e) {}
    //nom,doc,cod
    String [] pfe = new String[]{req.getParameter("nom"),req.getParameter("doc"),req.getParameter("cod")};
    String[][] filtrosEspeciales;
    if (paginaActual<1) paginaActual=1;
    {
      for (int i=0;i<pfe.length;i++) {
        if (pfe[i]==null) pfe[i] = "";
        pfe[i]=pfe[i].toUpperCase().replaceAll(i==0?"[^A-Z]":"[^0-9]", "");
      }
      filtrosEspeciales = (pfe[0].length()==0 && pfe[1].length()==0 && pfe[2].length()==0)?
          null:new String[][]{pfe[0].split("\\s+"),pfe[1].split("\\s+"),pfe[2].split("\\s+")};
    }
    try {
      List<Filtro> filtros = new LinkedList<Filtro>(Arrays.asList(usr.filtros));//TODO Ojo si no es A
      String query = null;
      try {
        query = req.getParameter("q");
        List<Filtro> fils = AuxiliaresConsulta.leerFiltro(query);
        filtros.addAll(fils);
      } catch (Exception e) {
        query ="";//TODO arrojar error
        e.printStackTrace();//TODO remover esta linea, debugging 
      }
      Object[] resp = kernel.getEstudiantes(filtros.toArray(new Filtro[filtros.size()]), filtrosEspeciales, (paginaActual-1)*respag, respag);
      int numEncontrados = (Integer) resp[1];
      int paginas = (int) Math.ceil(numEncontrados/10);
      if (paginaActual>paginas) paginaActual=1;
      Set<Integer> pInteres = new TreeSet<Integer>(Arrays.asList(1,paginas));
      for (int i:new int[]{paginaActual,paginaActual-1,paginaActual+1}) if (i>1 && i<paginas) pInteres.add(i);
      EstudianteDAO [] ests = (EstudianteDAO[]) resp[0];
      String txt = getHTMLListaEstudiantes(ests,paginaActual,pInteres,query,pfe);
      res.getOutputStream().write(txt.getBytes("UTF-8"));
    } catch (MyException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  }
  private static String getHTMLListaEstudiantes(EstudianteDAO[] ests, int pActual, Set<Integer> pInteres,String query,String[] prevP) {
    final StringBuilder sb=new StringBuilder();
    sb.append("<html>");
    sb.append("<form action=\"\" method=\"get\">");
    /*for (String nomc:new String[]{"nom","cod","doc"})
      sb.append("<input type=\"text\" name=\"nom\" value=\""+query+"\">");*/
    sb.append("<div><span>Nombre: </span><input type=\"text\" name=\"nom\" value=\""+prevP[0]+"\"></div>");
    sb.append("<div><span>Documento: </span><input type=\"text\" name=\"doc\" value=\""+prevP[1]+"\"></div>");
    sb.append("<div><span>Codigo: </span><input type=\"text\" name=\"cod\" value=\""+prevP[2]+"\"></div>");
    sb.append("<input type=\"hidden\" name=\"q\" value=\""+query+"\">");
    sb.append("<input type=\"hidden\" name=\"p\" value=\""+pActual+"\">");
    sb.append("<input type=\"submit\" name=\"Buscar\" value=\"Buscar\">");
    sb.append("</form>");
    sb.append("<p><table border=1>");
    sb.append("<tr><td>APELLIDO</td><td>NOMBRE</td><td>T</td><td>DOC</td><td>IES</td><td>PROGRAMA</td><td>CODIGO</td></tr>");
    for (EstudianteDAO e:ests) {
      sb.append("<tr>");//tipom doc, ies, programa,codigo
      int indProg = e.datos.getIndicePrograma();
      String linkEst = "alumno?ind="+e.ind;
      for (String s:new String[]{          new String(e.datosPersonales.apellido),
          new String(e.datosPersonales.nombre),
          CajaDeHerramientas.tipoDocumentoToString(e.datosPersonales.tipoDocumento),
          e.datosPersonales.documento==-1?"":(""+e.datosPersonales.documento),
          ""+e.ies.codigo,
          indProg==-1?"":new String(e.ies.programas[indProg].nombre),
          new String(e.datosPersonales.codigo)}) {
        sb.append("<td>");sb.append(toLink(linkEst, s));sb.append("</td>");
      }
      sb.append("</tr>");
    }
    sb.append("</table></p>");
    {
      sb.append("<div>");
      int pr =0;
      for (Integer i:pInteres) {
        sb.append(((i-pr!=1)?" .. ":" ")+"<a href = \"?q="+query+"&pag="+i+"&nom="+prevP[0]+"&doc="+prevP[1]+"&cod="+prevP[2]+"\">"+i+"</a>");
        pr = i;
      }
      sb.append("</div>");
    }
    
    sb.append("</html>");
    return sb.toString();
  }
  private static String toLink(String url, String txt) {
    return "<a href=\""+url+"\">"+txt+"</a>";
  }
}