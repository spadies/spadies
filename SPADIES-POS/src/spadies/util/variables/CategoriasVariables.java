package spadies.util.variables;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public enum CategoriasVariables { 
  VARIABLES_IES("Características de las instituciones educativas", new Variable[]{
      Variable.CARACTER_IES,
      Variable.CODIGO_IES,
      Variable.DEPARTAMENTO_IES,
      //Variable.MUNICIPIO_IES,
      Variable.ORIGEN_IES,
      Variable.INT_CLASE_IES,
      Variable.INT_BLOQUEADA_IES,
  }),
  INFORMACION_SOCIOECONOMICA("Características de los individuos y sus hogares",new Variable[]{
      Variable.SEXO_EST,
      Variable.TRABAJABA_CUANDO_ICFES_EST,
      Variable.CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST,
      Variable.INGRESO_HOGAR_EST2,
      Variable.NUMERO_FAMILIARES_EST,
      //Variable.VIVIENDA_PROPIA_EST,
      //Variable.NUMERO_HERMANOS_EST,
      //Variable.POSICION_ENTRE_LOS_HERMANOS_EST,
      Variable.ESTRATO,
      Variable.NIVEL_SISBEN,
      Variable.NIVEL_EDUCATIVO_MADRE_EST,
      Variable.CLASIFICACION_PUNTAJE_ICFES_EST,
      Variable.CLASIFICACION_PUNTAJE_ICFES_DECILES_EST,
      Variable.REPITENCIA_PER
  }),
  /*INFORMACION_ACADEMICA("Información Académica", new Variable[]{
      Variable.CLASIFICACION_PUNTAJE_ICFES_EST,
      Variable.CLASIFICACION_PUNTAJE_ICFES_DECILES_EST,
      Variable.REPITENCIA_PER
  }),*/
  INFORMACION_INSTITUCIONAL("Características programas académicos", new Variable[]{
      Variable.AREA_CONOCIMIENTO_EST,
      Variable.NUCLEO_CONOCIMIENTO_EST,
      Variable.NIVEL_FORMACION_EST,
      Variable.METODOLOGIA_EST,
      Variable.PROGRAMA_EST,
      //Variable.LICENCIATURA_EST,
      //Variable.LENGUAS_EST
  }),
  NUMERO_DE_APOYOS_RECIBIDOS_DE_LA_IES("Programas de apoyo a los estudiantes", new Variable[]{
      Variable.NUMERO_APOYOS_ACADEMICOS_RECIBIDOS_EST,
      Variable.NUMERO_APOYOS_FINANCIEROS_RECIBIDOS_EST,
      Variable.NUMERO_APOYOS_OTROS_RECIBIDOS_EST,
      Variable.NUMERO_APOYOS_ICETEX_RECIBIDOS_EST,
      Variable.NUMERO_APOYOS_ICETEX_MEDIO_RECIBIDOS_EST,
      Variable.NUMERO_APOYOS_ICETEX_LARGO_RECIBIDOS_EST,
      Variable.NUMERO_APOYOS_ICETEX_ACCES_RECIBIDOS_EST,
      Variable.TIPO_ICETEX_RECIBIDO_PER
  }),
  /*NUMERO_DE_APOYOS_POR_TIPO_ICETEX("Número de apoyos por tipo ICETEX", new Variable[]{
      Variable.NUMERO_APOYOS_ICETEX_RECIBIDOS_EST,
      Variable.NUMERO_APOYOS_ICETEX_MEDIO_RECIBIDOS_EST,
      Variable.NUMERO_APOYOS_ICETEX_LARGO_RECIBIDOS_EST,
      Variable.NUMERO_APOYOS_ICETEX_ACCES_RECIBIDOS_EST
  }),*/
  ESTADO_Y_RIESGO_DE_DESERCION("Estado del estudiante y riesgo de deserción", new Variable[]{
      Variable.CLASIFICACION_ESTADO_EST_DETALLADO,
      Variable.CLASIFICACION_RIESGO_PER
  }),
  VARIABLES_DE_TIEMPO("Eventos cronológicos del estudiante", new Variable[]{
      Variable.NUMERO_SEMESTRE_PER,
      Variable.PERIODO_INGRESO_EST,
      Variable.PERIODO_MATRICULADO_PER,
      Variable.PERIODO_IES_INICIO,
      Variable.PERIODO_IES_FIN,
      Variable.PERIODO_GRADO_EST,
      Variable.ULTIMO_PERIODO_MATRICULADO_EST
  }),
  VARIABLES_SBPRO("Saber Pro", new Variable[]{
      Variable.CLASIFICACION_PUNTAJE_SBPRO_EST,
      Variable.CLASIFICACION_PUNTAJE_SBPRO_DECILES_EST,
      Variable.CLASIFICACION_EDAD_AL_PRESENTAR_EL_SBPRO_EST,
      Variable.AREA_CONOCIMIENTO_SBPRO_EST,
      Variable.AREA_IGUAL_SBPRO_EST,
      Variable.EDUCACION_MADRE_SBPRO_EST,
      Variable.OCUPACION_MADRE_SBPRO_EST,
      Variable.INTERNET_SBPRO_EST,
      Variable.VLR_MATRICULA_SBPRO_EST
  }),
  OTROS("Otros",new Variable[]{Variable.PERIODO_ICFES_PRESENTA,Variable.PERIODO_ICFES_DATOS,Variable.REMPLAZO_ICFES});
  
  public final Variable[] vars;
  public final String nombre;

  private CategoriasVariables(String pNom, Variable[] vars) {
    this.nombre = pNom;
    this.vars = vars;
  }
  
  public static BCategoria[] variablesEnCategorias(FiltroVariables fv){    
    //BCategoria[] arregloVariables = new BCategoria[CategoriasVariables.values().length];
    List<BCategoria> arregloVariables = new LinkedList<BCategoria>();
    CategoriasVariables[] arregloValores = CategoriasVariables.values();
    for(int i = 0; i<arregloValores.length;i++){
      ArrayList<Variable>  variables = new ArrayList<Variable>();
      CategoriasVariables categoria = arregloValores[i];      
      for(Variable v:categoria.vars){
        if (fv.aceptarVariable(v))
          variables.add(v);        
      }
      if (!variables.isEmpty())
        arregloVariables.add(new BCategoria(categoria.nombre,variables.toArray(new Variable[variables.size()])));
    }
    return arregloVariables.toArray(new BCategoria[0]);
  }
  
  public static class BCategoria {
    public final Variable[] vars;
    public final String nombre;
    private BCategoria(String pNom, Variable[] vars) {
      this.nombre = pNom;
      this.vars = vars;
    }
  }
  
  public static final FiltroVariables TODO_VARIABLES= new FiltroVariables(){
    public boolean aceptarVariable(Variable v) {
      return true;
    }
  }; 
  
  public static final FiltroVariables VARIABLES_ESTATICAS= new FiltroVariables(){
    public boolean aceptarVariable(Variable v) {
      return v.tipo!=TipoVariable.TV_PERIODO_ESTUDIANTE;
    }
  };
}