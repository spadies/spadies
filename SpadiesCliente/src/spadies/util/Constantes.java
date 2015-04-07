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
package spadies.util;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import spadies.util.variables.Variable;

public final class Constantes {
  public static final String S_DESCONOCIDO="Sin Clasificar";
  public static final int anhoIni=1998,anhoFin=2057;  // Hay máximo 120 identificadores para los períodos (de 0 a 119)
  public static final String versionMenor = "2.8.2";
  public static final String nombreAplicacion="SPADIES 2.8";
  public static final String nombreAplicacionLargo="SPADIES "+versionMenor;
  public enum VersionDatos {
    V_2_3_1("2.3.1"),
    V_2_5("2.5"),
    V_2_6("2.6"),
    V_2_7("2.7"),
    V_2_7_2("2.7.2"),
    V_2_8("2.8"),;
    public final String txtVersion;
    private VersionDatos(String txtVersion) {
      this.txtVersion = txtVersion;
    }
    public static VersionDatos getVersion(String txtVer) {
      for (VersionDatos ver:VersionDatos.values())
        if (ver.txtVersion.equals(txtVer)) return ver;
      return null;
    }
  }
  public static final VersionDatos verDatos=VersionDatos.V_2_8;
  public static final String versionDatos=verDatos.txtVersion;
  public static final long codigoVerificacion = 1479429497764497247L;
  public static File carpetaDatos=new File("datos");
  public static final boolean cargaDatosPersonales = false;
  public static File carpetaCSV=new File("csvs");
  public static final File archivoConfiguracion=new File("spadies.properties");
  public static final File archivoFiltro=new File("filtro.ies");
  public static final File archivoLog = new File("spadies.log");
  public static final Logger logSPADIES = new Logger(archivoLog);
  public static final Map<Integer, Variable> ord2Variable;
  static {
    Map<Integer, Variable> map = new TreeMap<Integer, Variable>();
    for (Variable v: Variable.values()) map.put(v.ordinal(), v);
    ord2Variable = Collections.unmodifiableMap(map);
  }
  public static final Set<Variable> variablesActualizables =  EnumSet.of(Variable.PROGRAMA_EST, Variable.PERIODO_INGRESO_EST, Variable.PERIODO_MATRICULADO_PER, Variable.NUMERO_SEMESTRE_PER, Variable.PERIODO_IES_FIN, Variable.PERIODO_IES_INICIO, Variable.PERIODO_GRADO_EST, Variable.ULTIMO_PERIODO_MATRICULADO_EST);
  public static String ipServidorSPADIES="spadies.uniandes.edu.co";
  public static int puertoServidorMatch=41304;
  //public static final int puertoServidorMatch=41304;
  //public static final int puertoServidorMatch=41306;
  public static final int puertoServidorConsultas=41305;
  public static final int timeoutServidorConsultas=1000*20;
  public static final int maxVariablesExtra=5;
  public static final int maxVariablesExtraDinamicas=5;
  public static Collection<String> filtroIES = Collections.emptySet();
  public static boolean displayConsultaExtendido = false;
  public static final String
    p_ipServidorSPADIES = "spadies.servidor.dir_ip",
    //p_ipServidorSPADIES = "spadies.server.address",
    p_puertoServidorMatch = "spadies.servidor.match.puerto",
    //p_puertoServidorConsultas = "spadies.servidor.consulta.puerto",
    p_carpetaCSV = "spadies.carpeta_csv",
    p_displayConsultaExtendido = "spadies.display_consulta_extendido";
  public static final Properties configuracionPredeterminada = new Properties();
  static {
    configuracionPredeterminada.put(p_ipServidorSPADIES, "spadies.uniandes.edu.co");
    configuracionPredeterminada.put(p_puertoServidorMatch, "41304");
    //configuracionPredeterminada.put(p_puertoServidorConsultas, "41305");
    configuracionPredeterminada.put(p_carpetaCSV, "csvs");
  }
  public static void guardarArchivoConfiguracion() {
    try {
      Properties prop = new Properties();
      prop.put(p_ipServidorSPADIES, ipServidorSPADIES);
      prop.put(p_puertoServidorMatch, puertoServidorMatch+"");
      prop.put(p_carpetaCSV, carpetaCSV.getPath());
      prop.store(new FileOutputStream(archivoConfiguracion), "Archivo de configuración de SPADIES");
    }
    catch (Throwable th) {
    }
  }
  public static void cargarArchivoConfiguracion() {
    Properties prop = new Properties();
    try {
      prop.load(new FileInputStream(archivoConfiguracion));
    }
    catch (Throwable th) {
      prop = configuracionPredeterminada;
    }
    String s1=prop.getProperty(p_ipServidorSPADIES),
    s2=prop.getProperty(p_carpetaCSV),
    s3=prop.getProperty(p_puertoServidorMatch),
    s4=prop.getProperty(p_displayConsultaExtendido);
    if (s1!=null && s1.length()>0) ipServidorSPADIES=s1;
    if (s2!=null && s2.length()>0) carpetaCSV=new File(s2);
    if (s3!=null && s3.length()>0 && s3.matches("\\d+")) puertoServidorMatch = Integer.parseInt(s3);
    if (s4!=null) displayConsultaExtendido = Boolean.parseBoolean(s4);
    if (!archivoConfiguracion.exists()) guardarArchivoConfiguracion();
  }
  public static void cargarArchivoFiltroIES() {
    filtroIES = Collections.emptySet();
    Collection<String> res = new TreeSet<String>();
    try {
      BufferedReader br=new BufferedReader(new FileReader(archivoFiltro));
      for (String s = br.readLine();s!=null;s = br.readLine()) {
        s = s.trim();
        if (!s.equals("")) res.add(s); 
      }
      br.close();
      filtroIES = res;
    }
    catch (Throwable th) {
    }
  }
}
