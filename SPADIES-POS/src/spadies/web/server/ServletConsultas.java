package spadies.web.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spadies.gui.format.Procesador;
import spadies.gui.format.ProcesadorCaracterizacionEstudiantes;
import spadies.gui.format.ProcesadorConsultaDesercion;
import spadies.gui.format.ProcesadorConsultaGrado;
import spadies.gui.format.ProcesadorCruceVariables;
import spadies.gui.format.ProcesadorDesercionPorPeriodo;
import spadies.gui.format.ResultadoConsulta;
import spadies.util.MyException;
import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;
import spadies.web.server.util.AuxiliaresConsulta;
import spadies.web.server.util.Usuario;
import spadies.web.server.util.Usuario.Tipo;

public class ServletConsultas extends ServletSPADIES {
  private static String rutaImagenes = null;
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    ServletContext context = getServletContext();
    context.getInitParameter("spadies.passwords");
    //TODO potencialmente se asigna la variable varias veces
    if (rutaImagenes==null) rutaImagenes = context.getInitParameter("spadies.web_img");
  }
  public void doAll(HttpServletRequest req, HttpServletResponse res) throws IOException {
    Usuario usr = AuxiliaresConsulta.getUsuarioSesion(req);
    res.setCharacterEncoding("UTF-8");
    res.setContentType("text/javascript");
    res.addHeader("Cache-Control", "no-cache");
    res.addHeader("Pragma", "no-cache");
    //String txt = ImprimirVariables.imprimirCategorias(CategoriasVariables.variablesEnCategorias(CategoriasVariables.TODO_VARIABLES));
    String codigoConsulta = null;
    try {
      Variable [] diferenciados = new Variable[0];
      Filtro [] filtros = new Filtro[0];
      //StringBuilder canoq = new StringBuilder();
      int tipo = -1, modo = 0;
      String qu = "";
      {//Procesamiento entrada
        BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
        String s = "";
        while((s=br.readLine())!=null) qu+=s;
        String[] sp = qu.split("&");
        List<Variable> lDif = new ArrayList<Variable>();
        List<Filtro> lFil = new ArrayList<Filtro>(/*Arrays.asList(usr.filtros)*/);
        //Filtrado del ultimo periodo cursado
        {
          Comparable[] ran = Variable.PERIODO_MATRICULADO_PER.rango.getRango();
          Item[] items = new Item[ran.length-1];
          for (int i=0,t=ran.length-1;i<t;i++)
            items[i]=new Item(ran[i],"","");
          lFil.add(new Filtro(Variable.PERIODO_MATRICULADO_PER,items));
          lFil.add(new Filtro(Variable.CARACTER_IES,new Item[]{new Item((byte)1,"",""),new Item((byte)2,"",""),new Item((byte)3,"",""),new Item((byte)4,"","")}));
        }
        for (String ss:sp) {
          if (ss.startsWith("c=")) { //Consulta
            String cons = ss.replace("c=", "");
            tipo = Integer.parseInt(cons);
            //canoq.append(tipo);
          } else if (ss.startsWith("diferenciados=")) { //Criterios diferenciacion
            lDif.addAll(AuxiliaresConsulta.leerVariables(ss.replace("diferenciados=", ""),usr.tipo.fvar));
            for (java.util.Iterator<Variable> it = lDif.iterator();it.hasNext();) {
              Variable var = it.next();
              if (!usr.getFiltroVariables().aceptarVariable(var)) it.remove();
            }
          } else if (ss.startsWith("fil=")) {
            lFil.addAll(AuxiliaresConsulta.leerFiltro(ss.replace("%D1","Ñ").replace("fil=", "")));
          } else if (ss.startsWith("modo=")) {
            String txtM = ss.replace("modo=", "");
            if (txtM.equals("1")) {
              modo = 1;
              lFil.addAll(Arrays.asList(usr.filtros));
            }
          }
        }
        if (modo==1 && usr.tipo==Tipo.IES) qu+="&"+usr.nombre;
        diferenciados = lDif.toArray(new Variable[0]);
        filtros = lFil.toArray(new Filtro[0]);
        {
          byte[] textBytes = qu.getBytes();
          MessageDigest md = MessageDigest.getInstance("md5");
          md.update(textBytes);
          byte[] codigo = md.digest();
          StringBuilder codcon = new StringBuilder();
          for (byte b:codigo) {
            String str = Integer.toString(b&0xFF,16);
            if (str.length()==1) str = "0" + str;
            codcon.append(str);
          }
          codigoConsulta = codcon.toString();
        }
      }
      File fData = new File(getServletContext().getRealPath("img/gen")+"/data"+codigoConsulta+".bin");
      if (!fData.exists()) {
        Procesador proc = null;
        Collection<ResultadoConsulta> resul = null;
        switch (tipo) {
        case 0: proc = ProcesadorConsultaDesercion.instance;break;
        case 1: proc = ProcesadorDesercionPorPeriodo.instance;break;
        case 2: proc = ProcesadorCaracterizacionEstudiantes.instance;break;
        case 3: proc = ProcesadorConsultaGrado.instance;break;
        case 4: proc = ProcesadorCruceVariables.instance;break;
        }
        proc.setParametros(false, diferenciados, filtros);
        resul = proc.generarGrafica();
        String txt = AuxiliaresConsulta.resultadoToJSON(resul, diferenciados, filtros, codigoConsulta, getServletContext().getRealPath("img/gen"));
        FileOutputStream fos = new FileOutputStream(fData);
        fos.write(txt.getBytes());
        fos.close();
      }
      {
        String txt = "";
        String s = null;
        BufferedReader br = new BufferedReader(new FileReader(fData));
        while ((s=br.readLine())!=null) txt+=s;
        br.close();
        res.getOutputStream().write(txt.getBytes("UTF-8"));
      }
    } catch (MyException e) {
      res.getOutputStream().write(("\""+e.getMessage().replace("\"", "\\\"")+"\"").getBytes("UTF-8"));
      //TODO Logear e.printStackTrace();
    } catch (Exception e) {
      res.getOutputStream().write(("\""+("Error interno: "+e.getMessage()).replace("\"", "\\\"")+"\"").getBytes("UTF-8"));
      e.printStackTrace();
      //TODO Logear e.printStackTrace();
    }
  }
}