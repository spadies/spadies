package spadies.web.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;
import spadies.web.server.util.AuxiliaresConsulta;
import spadies.web.server.util.Usuario;

public class ServletUsuario extends ServletSPADIES {
  protected static Map<String, String> usuariosIES = new TreeMap<String, String>();
  protected static String rutPass = null;
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    ServletContext context = getServletContext();
    rutPass = context.getInitParameter("spadies.passwords");
  }
  protected static synchronized void initUsuariosIES(File fPwd) {
    if (usuariosIES.isEmpty()) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(fPwd));
        br.readLine();
        for (String s = br.readLine();s!=null;s = br.readLine()) {
          String [] sp = s.split(";");
          usuariosIES.put(sp[0], sp[1]);
        }
        br.close();
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    if("logout".equals(req.getQueryString())) {
      HttpSession session = req.getSession(true);
      session.removeAttribute("usuario");
      res.setHeader("Location", req.getHeader("Referer"));
    } else {
      doAll(req, res);
    }
  }
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    System.out.println("POST");
    //if (usuariosIES.isEmpty()) initUsuariosIES(new File(getServletContext().getRealPath("passwd.csv")));
    //System.out.println(new File(rutPass).getAbsolutePath());
    if (usuariosIES.isEmpty()) initUsuariosIES(new File(rutPass));
    BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
    String s = br.readLine();
    String [] sp = s.split("&");
    if (sp.length==2) {
      String usr = sp[0];
      String pass = sp[1];
      String passd = usuariosIES.get(usr);
      //TODO Machete horrible, especificar manera de poner estos usuarios en archivo de claves
      if (usr.equals("men") && pass.equals("clame10")) {
        AuxiliaresConsulta.setUsuarioSesion(req, new Usuario(usr, Usuario.Tipo.MEN, new Filtro[0]));
      }
      else if (passd!=null && pass.equals(passd)) {
        AuxiliaresConsulta.setUsuarioSesion(req, new Usuario(usr, Usuario.Tipo.IES, new Filtro[]{new Filtro(Variable.CODIGO_IES, new Item[]{new Item(Integer.parseInt(usr),"","")})}));
      }
    }
    doAll(req,res);
  }
  public void doAll(HttpServletRequest req, HttpServletResponse res) throws IOException {
    Usuario usr = AuxiliaresConsulta.getUsuarioSesion(req);
    res.setCharacterEncoding("UTF-8");
    res.setContentType("text/javascript");
    res.addHeader("Cache-Control", "no-cache");
    res.addHeader("Pragma", "no-cache");
    String txt = usr.toJSON();
    res.getOutputStream().write(txt.getBytes("UTF-8"));
  }
}
