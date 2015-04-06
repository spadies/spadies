package spadies.web.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartPanel;

import spadies.gui.format.ProcesadorDesercionPorPeriodo;
import spadies.gui.graficas.FabricaGraficas;
import spadies.kernel.EstudianteDAO;
import spadies.util.CajaDeHerramientas;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.variables.Variable;
import spadies.web.server.util.AuxiliaresConsulta;
import spadies.web.server.util.Usuario;
import spadies.web.server.util.Usuario.Tipo;

public class ServletDInformacionAlumno extends ServletSPADIES {
  //private static String rutaImagenes = null;
  private static final String imgPath = "img/gen";
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    ServletContext context = getServletContext();
    //context.getInitParameter("spadies.passwords");
    //TODO potencialmente se asigna la variable varias veces
    //if (rutaImagenes==null) rutaImagenes = context.getInitParameter("spadies.web_img");
  }
  public void doAll(HttpServletRequest req, HttpServletResponse res) throws IOException {
    Usuario usr = AuxiliaresConsulta.getUsuarioSesion(req);
    if (usr.tipo!=Tipo.IES) {
      res.setStatus(401);
      return;
    }
    res.setCharacterEncoding("UTF-8");
    res.setContentType("text/html");
    res.addHeader("Cache-Control", "no-cache");
    res.addHeader("Pragma", "no-cache");
    int ies = usr.getIES();
    try {
      String param = req.getParameter("ind");
      if (!param.matches("[0-9]+")); //error
      int ind = Integer.parseInt(param);
      EstudianteDAO e = kernel.getEstudiante(ies,ind);
      if (e!=null) {
        String txt = getHTMLEstudiante(e);
        res.getOutputStream().write(txt.getBytes("UTF-8"));
      } else return;//TODO error decente
    } catch (MyException e1) {
      // TODO Mensaje de error a usuario
      e1.printStackTrace();
    }
  }
  private String getHTMLEstudiante(EstudianteDAO e) {
    String imgNom = "img_e_"+Math.random()+".jpg";
    String urlImg = imgPath + "/" + imgNom;
    {
      final ChartPanel panelGrafica;
      {
        SortedMap<String,SortedMap<Integer,Double>> seriesDatos=new TreeMap<String,SortedMap<Integer,Double>>();
        {
          SortedMap<Integer,Double> supervivenciaNormal=new TreeMap<Integer,Double>(),supervivenciaEstructural=new TreeMap<Integer,Double>();
          seriesDatos.put("Supervivencia estudiante",supervivenciaNormal);
          seriesDatos.put("Supervivencia estructural estudiante",supervivenciaEstructural);
          int nPerE=0,nPer=0;
          for (int i=0,n=e.ies.n; i<n; i++) if (e.estaMatriculado[i]) {
            double sup=e.supervivencias[i],supEst=e.supervivenciasEstructurales[i];
            if (sup!=-1d) supervivenciaNormal.put(++nPer,sup);
            if (supEst!=-1d) supervivenciaEstructural.put(++nPerE,supEst);
          }
        }
        panelGrafica=FabricaGraficas.crearGraficaSupervivenciaEstudiante("",seriesDatos);
        try {
          ImageIO.write(AuxiliaresConsulta.imagen(panelGrafica.getChart(), 640, 400),"JPEG",new File(getServletContext().getRealPath(imgPath)+"/"+imgNom));
        } catch (IOException e1) {
          // TODO Poner imagen por defecto
        }
      }
    }
    final StringBuilder sb=new StringBuilder();
    {
      int indProg=e.datos.getIndicePrograma();
      int puntajeICFES=e.datos.getPuntajeICFES();
      int numeroHermanos=e.datos.getNumeroHermanos();
      int posicionEntreHermanos=e.datos.getPosicionEntreLosHermanos();
      int edadPresentacionIcfes=e.datos.getEdadAlPresentarElICFES();
      String[][] variablesBasicas1={
          {"Cohorte",CajaDeHerramientas.textoSemestreToString(e.ies.semestres[e.datos.getSemestrePrimiparo()])},
          {"Nombres",new String(e.datosPersonales.nombre)},
          {"Apellidos",new String(e.datosPersonales.apellido)},
          {"Documento",CajaDeHerramientas.tipoDocumentoToString(e.datosPersonales.tipoDocumento)+" "+(e.datosPersonales.documento==-1?"":(""+e.datosPersonales.documento))},
          {"Fecha de nacimiento (DD/MM/AAAA)",CajaDeHerramientas.fechaToString(e.datosPersonales.anhoFechaNacimiento,e.datosPersonales.mesFechaNacimiento,e.datosPersonales.diaFechaNacimiento)},
          {"Sexo",CajaDeHerramientas.sexoToString(e.datos.getSexo())},
          {"Programa",indProg==-1?"":new String(e.ies.programas[indProg].nombre)},
          {"Código de estudiante",new String(e.datosPersonales.codigo)},
          {"Puntaje (estandarizado) examen de estado",puntajeICFES==-1?Constantes.S_DESCONOCIDO:(""+puntajeICFES+"/100")},
      };
      String[][] variablesBasicas2={
          {"Nivel educativo de la madre",Variable.NIVEL_EDUCATIVO_MADRE_EST.rango.toString(Variable.NIVEL_EDUCATIVO_MADRE_EST.rango.getRango(e.datos.getNivelEducativoMadre()))},
          {"Número de hermanos",numeroHermanos==-1?Constantes.S_DESCONOCIDO:String.valueOf(numeroHermanos)},
          {"Posición entre hermanos",posicionEntreHermanos==-1?Constantes.S_DESCONOCIDO:String.valueOf(posicionEntreHermanos)},
          {"Edad de presentación del examen de estado",edadPresentacionIcfes==-1?Constantes.S_DESCONOCIDO:String.valueOf(edadPresentacionIcfes)},
          {"Vivienda propia",Variable.VIVIENDA_PROPIA_EST.rango.toString(Variable.VIVIENDA_PROPIA_EST.rango.getRango(e.datos.getViviendaPropia()))},
          {"Trabajaba al presentar el examen de estado",Variable.TRABAJABA_CUANDO_ICFES_EST.rango.toString(Variable.TRABAJABA_CUANDO_ICFES_EST.rango.getRango(e.datos.getTrabajabaCuandoPresentoIcfes()))},
          {"Ingreso familiar al presentar el examen de estado",Variable.INGRESO_HOGAR_EST.rango.toString(Variable.INGRESO_HOGAR_EST.rango.getRango(e.datos.getIngresoHogar()))},
          //{"Estado",Variable.CLASIFICACION_ESTADO_EST.rango.toString(Variable.CLASIFICACION_ESTADO_EST.rango.getRango(e.datos.getEstado()))},
          {"Estado",Variable.CLASIFICACION_ESTADO_EST.rango.toString(Variable.CLASIFICACION_ESTADO_EST.rango.getRango(e.datos.getEstado()))},
          {"Período en que se graduó",(e.datos.getSemestreGrado()==-1)?"-":CajaDeHerramientas.textoSemestreToString(e.ies.semestres[e.datos.getSemestreGrado()])}
      };
      LinkedList<String[]> variablesBasicas3=new LinkedList<String[]>();
      for (int k=0; k<5; k++) if (e.ies.variablesExtras[k].nombre.length>0) {
        int val=e.datos.getValorVariableExtra(k);
        variablesBasicas3.add(new String[]{new String(e.ies.variablesExtras[k].nombre),(val==-1)?Constantes.S_DESCONOCIDO:new String(e.ies.variablesExtras[k].nombresValores[val])});
      }
      sb.append("<html>");
      sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> ");
      sb.append("<font style=\"font-size: 18\"><b>Información del estudiante "+new String(e.datosPersonales.apellido)+", "+new String(e.datosPersonales.nombre)+"</b></font>");
      sb.append("<p><table border=0 width=100%><tr><td width=50%>");
      for (String[] varba:variablesBasicas1) sb.append("<font style=\"font-size: 14\"><b>"+varba[0]+"</b></font><p>"+CajaDeHerramientas.stringToHTML("    "+varba[1])+"<p>");
      sb.append("</td><td width=50%>");
      for (String[] varba:variablesBasicas2) sb.append("<font style=\"font-size: 14\"><b>"+varba[0]+"</b></font><p>"+CajaDeHerramientas.stringToHTML("    "+varba[1])+"<p>");
      sb.append("</td></tr>");
      if (variablesBasicas3.size()>0) {
        sb.append("<tr><td colspan=2 width=100%>");
        for (String[] varba:variablesBasicas3) sb.append("<font style=\"font-size: 14\"><b>"+varba[0]+"</b></font><p>"+CajaDeHerramientas.stringToHTML("    "+varba[1])+"<p>");
        sb.append("</td></tr>");
      }
      sb.append("</table>");
      sb.append("<div>");
      sb.append("<img src=\"../"+urlImg+"\"/>");
      sb.append("</div>");
      sb.append("<p><table border=1><tr>");
      for (String s:new String[]{"#","Período","Materias tomadas","Materias aprobadas","Repitencia","Apoyo<p>financiero","Apoyo<p>académico","Otros<p>apoyos","Apoyo<p>ICETEX","Grupo de<p>riesgo","Riesgo","Riesgo<p>estructural","Supervivencia","Supervivencia<p>estructural"}) sb.append("<td bgcolor=\"#A0A0A0\" align=center><b>"+s+"</b></td>");
      for (int i=0;i<Constantes.maxVariablesExtraDinamicas;i++)
        if (e.ies.variablesExtrasD[i].nombre.length>0)
          sb.append("<td bgcolor=\"#A0A0A0\" align=center><b>"+new String(e.ies.variablesExtrasD[i].nombre)+"</b></td>");
      sb.append("</tr>");
      for (int i=0,n=e.ies.n,nper=0; i<n; i++) if (e.estaMatriculado[i]) {
        String w[]={
           ""+(++nper),
           CajaDeHerramientas.textoSemestreToString(e.ies.semestres[i]),
           toString(e.datos.getNumeroMateriasTomadas(i)),
           toString(e.datos.getNumeroMateriasAprobadas(i)),
           toString(e.repitencias[i]),
           toString(e.datos.getRecibioApoyoFinanciero(i)),
           toString(e.datos.getRecibioApoyoAcademico(i)),
           toString(e.datos.getRecibioApoyoOtro(i)),
           Variable.TIPO_ICETEX_PER.rango.toString(Variable.TIPO_ICETEX_PER.rango.getRango(e.datos.getTipoApoyoICETEXRecibido(i))),
           toString(e.datos.getClaseRiesgo(i)),
           toString(e.datos.getRiesgo(i)),
           toString(e.datos.getRiesgoEstructural(i)),
           toString(e.supervivencias[i]),
           toString(e.supervivenciasEstructurales[i])
        };
        sb.append("<tr>");
        for (String s:w) sb.append("<td align=center>"+CajaDeHerramientas.stringToHTML(s)+"</td>");
        for (int j=0;j<Constantes.maxVariablesExtraDinamicas;j++)
          if (e.ies.variablesExtrasD[j].nombre.length>0) {
            int val=e.datos.getValorVariableExtra(j,i);
            sb.append("<td align=center>"+CajaDeHerramientas.stringToHTML((val==-1)?Constantes.S_DESCONOCIDO:new String(e.ies.variablesExtrasD[j].nombresValores[val]))+"</td>");
          }
        sb.append("</tr>");
      }
      sb.append("</table><p></html>");
    }
    return sb.toString();
  }
  private static String toString(int val) {
    return (val==-1)?"?":(""+val);
  }
  private static String toString(double val) {
    return (val==-1)?"?":CajaDeHerramientas.df_decimal.format(val);
  }
  private static String toString(boolean val) {
    return val?"Si":"No";
  }
}