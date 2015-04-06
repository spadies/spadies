package spadies.util.variables;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public enum SCategoriasVariables { 
  VARIABLES_IES("Características de las instituciones educativas", new SVariable[]{
  }),
  INFORMACION_SOCIOECONOMICA("Características de los individuos y sus hogares",new SVariable[]{
      SVariable.SEXO_EST,
      SVariable.TRABAJABA_CUANDO_ICFES_EST,
      SVariable.CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST,
      SVariable.INGRESO_HOGAR_EST2,
      SVariable.NUMERO_FAMILIARES_EST,
      //SVariable.VIVIENDA_PROPIA_EST,
      //SVariable.NUMERO_HERMANOS_EST,
      //SVariable.POSICION_ENTRE_LOS_HERMANOS_EST,
      SVariable.ESTRATO,
      SVariable.NIVEL_SISBEN,
      SVariable.NIVEL_EDUCATIVO_MADRE_EST,
      SVariable.CLASIFICACION_PUNTAJE_ICFES_EST,
      SVariable.CLASIFICACION_PUNTAJE_ICFES_DECILES_EST
  }),
  /*INFORMACION_ACADEMICA("Información Académica", new Variable[]{
      Variable.CLASIFICACION_PUNTAJE_ICFES_EST,
      Variable.CLASIFICACION_PUNTAJE_ICFES_DECILES_EST,
      Variable.REPITENCIA_PER
  }),*/
  INFORMACION_INSTITUCIONAL("Características programas académicos", new SVariable[]{
      SVariable.AREA_CONOCIMIENTO_EST,
      SVariable.NUCLEO_CONOCIMIENTO_EST,
      SVariable.NIVEL_FORMACION_EST,
      SVariable.METODOLOGIA_EST,
  }),
  NUMERO_DE_APOYOS_RECIBIDOS_DE_LA_IES("Programas de apoyo a los estudiantes", new SVariable[]{
  }),
  /*NUMERO_DE_APOYOS_POR_TIPO_ICETEX("Número de apoyos por tipo ICETEX", new Variable[]{
      Variable.NUMERO_APOYOS_ICETEX_RECIBIDOS_EST,
      Variable.NUMERO_APOYOS_ICETEX_MEDIO_RECIBIDOS_EST,
      Variable.NUMERO_APOYOS_ICETEX_LARGO_RECIBIDOS_EST,
      Variable.NUMERO_APOYOS_ICETEX_ACCES_RECIBIDOS_EST
  }),*/
  ESTADO_Y_RIESGO_DE_DESERCION("Estado del estudiante y riesgo de deserción", new SVariable[]{
  }),
  VARIABLES_DE_TIEMPO("Eventos cronológicos del estudiante", new SVariable[]{
      SVariable.PERIODO_INGRESO_EST,
  }),
  OTROS("Otros",new SVariable[]{
      SVariable.VIDAS_EST
  });
  
  public final SVariable[] vars;
  public final String nombre;

  private SCategoriasVariables(String pNom, SVariable[] vars) {
    this.nombre = pNom;
    this.vars = vars;
  }
  
  public static BCategoria[] variablesEnCategorias(SFiltroVariables fv){    
    //BCategoria[] arregloVariables = new BCategoria[CategoriasVariables.values().length];
    List<BCategoria> arregloVariables = new LinkedList<BCategoria>();
    SCategoriasVariables[] arregloValores = SCategoriasVariables.values();
    for(int i = 0; i<arregloValores.length;i++){
      ArrayList<SVariable>  variables = new ArrayList<SVariable>();
      SCategoriasVariables categoria = arregloValores[i];      
      for(SVariable v:categoria.vars){
        if (fv.aceptarVariable(v))
          variables.add(v);        
      }
      if (!variables.isEmpty())
        arregloVariables.add(new BCategoria(categoria.nombre,variables.toArray(new SVariable[variables.size()])));
    }
    return arregloVariables.toArray(new BCategoria[0]);
  }
  
  public static class BCategoria {
    public final SVariable[] vars;
    public final String nombre;
    private BCategoria(String pNom, SVariable[] vars) {
      this.nombre = pNom;
      this.vars = vars;
    }
  }
  
  public static final SFiltroVariables TODO_VARIABLES= new SFiltroVariables(){
    public boolean aceptarVariable(SVariable v) {
      return true;
    }
  }; 
  
  public static final FiltroVariables VARIABLES_ESTATICAS= new FiltroVariables(){
    public boolean aceptarVariable(Variable v) {
      return v.tipo!=TipoVariable.TV_PERIODO_ESTUDIANTE;
    }
  };
}