package spadies.web.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spadies.kernel.KernelSPADIES;
import spadies.util.Constantes;

public abstract class ServletSPADIES extends HttpServlet {
  protected static KernelSPADIES kernel = KernelSPADIES.getInstance();
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    ServletContext context = getServletContext();
    Constantes.carpetaDatos = new File(context.getInitParameter("spadies.datos"));
    if (EstadoWebSPADIES.instance.getEstado()!=EstadoWebSPADIES.Estado.PREPARADO) EstadoWebSPADIES.instance.cargarDatosSPA(context.getInitParameter("spadies.datos"),context.getInitParameter("spadies.web_img"));
  }
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    doAll(req,res);
  }
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    doAll(req,res);
  }
  public abstract void doAll(HttpServletRequest req, HttpServletResponse res) throws IOException;
}
