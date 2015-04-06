package spadies.web.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spadies.kernel.KernelSPADIES;
import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;
import spadies.web.server.util.AuxiliaresConsulta;
import spadies.web.server.util.Usuario;

import static spadies.web.server.ServletConsultasPredefinidas.CategoriasConsultasPre.*;

public class ServletConsultasPredefinidas extends ServletSPADIES {
  private static EnumMap<CategoriasConsultasPre, Collection<ConsultasPre>> categorias = new EnumMap<CategoriasConsultasPre, Collection<ConsultasPre>>(CategoriasConsultasPre.class);
  static {
    for (CategoriasConsultasPre ccp:CategoriasConsultasPre.values())
      categorias.put(ccp, new LinkedList<ConsultasPre>());
    for (ConsultasPre cp: ConsultasPre.values())
      categorias.get(cp.cat).add(cp);
  }
  public void doAll(HttpServletRequest req, HttpServletResponse res) throws IOException {
    Usuario usr = AuxiliaresConsulta.getUsuarioSesion(req);
    res.setCharacterEncoding("UTF-8");
    res.setContentType("text/javascript");
    res.addHeader("Cache-Control", "no-cache");
    res.addHeader("Pragma", "no-cache");
    res.getOutputStream().write(getJSONCategorias().getBytes("UTF-8"));
  }
  public String getJSONCategorias() {
    StringBuilder sb = new StringBuilder();
    boolean p = true;
    sb.append("[");
    Set<Entry<CategoriasConsultasPre, Collection<ConsultasPre>>> xx = categorias.entrySet();
    for (Entry<CategoriasConsultasPre, Collection<ConsultasPre>> e:categorias.entrySet()) {
      if (!p) sb.append(",");
      else p = false;
      sb.append(getJSONCategoria(e.getKey(), e.getValue()));
    }
    sb.append("]");
    return sb.toString();
  }
  public String getJSONCategoria(CategoriasConsultasPre ccp, Collection<ConsultasPre> cons) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"nombre\":");
    sb.append("\"" + ccp.nombre + "\",");
    sb.append("\"consultas\":");
    sb.append(getJSONConsultas(cons));
    sb.append("}");
    return sb.toString();
  }
  public String getJSONConsultas(Collection<ConsultasPre> cons) {
    StringBuilder sb = new StringBuilder();
    boolean p = true;
    sb.append("[");
    for (ConsultasPre c:cons) {
      if (!p) sb.append(",");
      else p = false;
      sb.append(c.toJSON());
    }
    sb.append("]");
    return sb.toString();
  }
  protected static enum CategoriasConsultasPre {
    CAR("Caracterización de los Estudiantes"),
    DES("Deserción estudiantil"),
    //TODO Guachada BR
    APO("Apoyos para asegurar la permanencia de los<br>estudiantes");
    public final String nombre;
    private CategoriasConsultasPre(String nombre) {
      this.nombre = nombre;
    }
  }
  private static enum ConsultasPre {
    INGRESO_HOGARES("Ingreso de la familia",0,2,new Variable[]{Variable.INGRESO_HOGAR_EST},CAR),
    PUNTAJE_ICFES("Puntaje del ICFES",0,2,new Variable[]{Variable.CLASIFICACION_PUNTAJE_ICFES_EST},CAR),
    CONSULTA_GASTO("Sexo",0,2,new Variable[]{Variable.SEXO_EST},CAR), //Caracterización de los estudiantes - sexo
    NIVEL_EDUCATIVO_MADRE("Nivel educativo de la madre",0,2,new Variable[]{Variable.NIVEL_EDUCATIVO_MADRE_EST},CAR),
    TRABAJABA_ICFES("Trabajaba cuando presentó el ICFES",0,2,new Variable[]{Variable.TRABAJABA_CUANDO_ICFES_EST},CAR),
    PROPIEDAD_DE_LA_VIVIENDA("Propiedad de la vivienda",0,2,new Variable[]{Variable.VIVIENDA_PROPIA_EST},CAR),
    
    DESERCION_POR_COHORTE_SEGUN_NIVEL_DE_FORMACION("Deserción por cohorte según Nivel de formación",0,0,new Variable[]{Variable.NIVEL_FORMACION_EST},DES),
    DESERCION_POR_COHORTE_OFICIAL_Y_NO_OFICIAL("Deserción por cohorte según IES oficiales y no oficiales",0,0,new Variable[]{Variable.ORIGEN_IES},DES),
    //DESERCION_POR_COHORTE_NUCLEO("Deserción por cohorte según Núcleo",0,0,new Variable[]{Variable.NUCLEO_CONOCIMIENTO_EST},DES),
    DESERCION_POR_COHORTE_DEPARTAMENTO("Deserción por cohorte según Departamento",0,0,new Variable[]{Variable.DEPARTAMENTO_IES},
        new Filtro[]{new Filtro(Variable.DEPARTAMENTO_IES,new Item[]{new Item((byte)5,"",""),new Item((byte)8,"",""),new Item((byte)11,"",""),new Item((byte)13,"",""),new Item((byte)15,"",""),new Item((byte)17,"",""),new Item((byte)18,"",""),new Item((byte)19,"",""),new Item((byte)20,"",""),new Item((byte)23,"",""),new Item((byte)27,"",""),new Item((byte)41,"",""),new Item((byte)47,"",""),new Item((byte)50,"",""),new Item((byte)52,"",""),new Item((byte)54,"",""),new Item((byte)63,"",""),new Item((byte)66,"",""),new Item((byte)68,"",""),new Item((byte)73,"",""),new Item((byte)76,"",""),new Item((byte)85,"",""),new Item((byte)88,"",""),new Item((byte)91,"",""),new Item((byte)94,"",""),new Item((byte)95,"",""),new Item((byte)97,"",""),new Item((byte)99,"",""),new Item((byte)25,"","")})},DES),
    DESERCION_POR_COHORTE_SEGUN_AREA_DE_CONOCIMIENTO("Deserción por cohorte según Area de conocimiento",0,0,new Variable[]{Variable.AREA_CONOCIMIENTO_EST},
        new Filtro[]{new Filtro(Variable.AREA_CONOCIMIENTO_EST,new Item[]{new Item((byte)1,"",""),new Item((byte)2,"",""),new Item((byte)3,"",""),new Item((byte)4,"",""),new Item((byte)5,"",""),new Item((byte)6,"",""),new Item((byte)8,"",""),new Item((byte)9,"","")})},DES),
    DESERCION_POR_COHORTE_SEGUN_NIVEL_INGRESO("Deserción por cohorte según Ingreso de la familia",0,0,new Variable[]{Variable.INGRESO_HOGAR_EST},DES),
    DESERCION_POR_COHORTE_SEGUN_ICFES("Deserción por cohorte según Puntaje ICFES",0,0,new Variable[]{Variable.CLASIFICACION_PUNTAJE_ICFES_EST},DES),
    DESERCION_POR_COHORTE_SEGUN_SEXO("Deserción por cohorte según Sexo",0,0,new Variable[]{Variable.SEXO_EST},DES),
    DESERCION_POR_NIVEL_EDUCATIVO_MADRE("Deserción por cohorte según Nivel educativo de la madre",0,0,new Variable[]{Variable.NIVEL_EDUCATIVO_MADRE_EST},DES),
    DESERCION_POR_TRABAJABA_ICFES("Deserción por cohorte según Trabajaba cuando presentó el ICFES",0,0,new Variable[]{Variable.TRABAJABA_CUANDO_ICFES_EST},DES),
    DESERCION_POR_PROPIEDAD_DE_LA_VIVIENDA("Deserción por cohorte según Propiedad de la vivienda",0,0,new Variable[]{Variable.VIVIENDA_PROPIA_EST},DES),
    //DESERCION_POR_PERIODO_OFICIAL_Y_NO_OFICIAL("Deserción por período según IES oficiales y no oficiales",0,1,new Variable[]{Variable.ORIGEN_IES},DES),
    /*DESERCION_POR_ASPECTOS_DESERCION("Aspectos determinantes de la deserción",0,0,new Variable[]{Variable.TRABAJABA_CUANDO_ICFES_EST}),*/
        
    //NUMERO_DE_ESTUDIANTES_CON_APOYO_ICETEX_ACCES("Estudiantes con crédito del ICETEX - ACCES",0,2,new Variable[]{Variable.ICETEX_RECIBIDO_PER},APO),
    NUMERO_DE_ESTUDIANTES_CON_APOYO_FINANCIEROS("Estudiantes con apoyos financiero",0,2,new Variable[]{Variable.APOYO_FINANCIERO_PER},APO),
    NUMERO_DE_ESTUDIANTES_CON_APOYO_ACADEMICO("Estudiantes con apoyos académicos",0,2,new Variable[]{Variable.APOYO_ACADEMICO_PER},APO),
    NUMERO_DE_ESTUDIANTES_CON_OTROS_APOYOS("Estudiantes con otros apoyos de las IES",0,2,new Variable[]{Variable.APOYO_OTRO_PER},APO),
    DESERCION_POR_COHORTE_ICETEX_ACCES("Deserción de estudiantes según Número de semestres con crédito ICETEX - ACCES",0,0,new Variable[]{Variable.NUMERO_APOYOS_ICETEX_ACCES_RECIBIDOS_EST},APO),
    DESERCION_POR_COHORTE_APOYO_FINANCIEROS("Deserción de estudiantes según Número de semestres con apoyos financieros",0,0,new Variable[]{Variable.NUMERO_APOYOS_FINANCIEROS_RECIBIDOS_EST},APO),
    DESERCION_POR_COHORTE_CON_APOYO_ACADEMICO("Deserción de estudiantes según Número de semestres con apoyos académicos",0,0,new Variable[]{Variable.NUMERO_APOYOS_ACADEMICOS_RECIBIDOS_EST},APO),
    DESERCION_POR_COHORTE_CON_OTROS_APOYOS("Deserción de estudiantes según Número de semestres con otros apoyos de las IES",0,0,new Variable[]{Variable.NUMERO_APOYOS_OTROS_RECIBIDOS_EST},APO);
    
    String nombre;
    int pest;
    int tipoc;
    Variable[] dif;
    Filtro[] fil;
    CategoriasConsultasPre cat;
    private ConsultasPre(String nombre,int pest,int tipoc,Variable[]dif, Filtro[] fil,CategoriasConsultasPre cat) {
      this.nombre = nombre;
      this.pest = pest;
      this.tipoc = tipoc;
      this.dif = dif;
      this.fil = fil;
      this.cat = cat;
    }
    private ConsultasPre(String nombre,int pest,int tipoc,Variable[]dif,CategoriasConsultasPre cat) {
      this(nombre,pest,tipoc,dif,filtroVariableNoDesconocido(dif),cat);
    }
    private static Filtro[] filtroVariableNoDesconocido(Variable[] dif) {
      Filtro[] res = new Filtro[dif.length];
      for (int i=0,t=res.length;i<t;i++) {
        List<Comparable> lst = new LinkedList(Arrays.asList(dif[i].rango.getRango()));
        lst.remove(new Byte((byte) -1));
        Item []items = new Item[lst.size()];
        int j = 0;
        for (Comparable val:lst) items[j++] = new Item(val,"","");
        res[i] = new Filtro(dif[i], items);
      }
      return res;
    }
    public String getQueryString() {
      StringBuilder sb = new StringBuilder();
      sb.append("c=");
      sb.append(tipoc);
      sb.append("&diferenciados=");
      for (Variable var:dif) {
        sb.append(var.ordinal());
        sb.append(",");
      }
      sb.append("&fil=");
      for (Filtro filt:fil) {
        sb.append("!");
        sb.append(filt.variable.ordinal());
        sb.append("--");
        for (Comparable com:filt.filtro) {
          sb.append(com);
          sb.append(",");
        }
      }
      return sb.toString();
    }
    public String toJSON() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append("\"nombre\":");
      sb.append("\""+this.nombre+"\",");
      sb.append("\"posp\":");
      sb.append("\""+this.pest+"\",");
      sb.append("\"query\":");
      sb.append("\""+this.getQueryString()+"\"");
      sb.append("}");
      return sb.toString();
    }
  }
}
