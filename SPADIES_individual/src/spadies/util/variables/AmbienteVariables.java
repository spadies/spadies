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
package spadies.util.variables;

import spadies.kernel.*;
import spadies.util.*;

public final class AmbienteVariables {
  private static final KernelSPADIES kernel=KernelSPADIES.getInstance();
  private static final AmbienteVariables instance=new AmbienteVariables();
  public static AmbienteVariables getInstance() {return instance;}
  private Integer[] codigosIES={};
  private String[] nombresProgramas={};
  private Byte[] idsSemestres={};
  private VariableExtra[] variablesExtras=new VariableExtra[Constantes.maxVariablesExtra];
  private VariableExtra[] variablesExtrasD=new VariableExtra[Constantes.maxVariablesExtraDinamicas];
  private AmbienteVariables() {}
  public void notificarCarga() {
    int n=kernel.listaIES.length;
    codigosIES=new Integer[n];
    for (int i=0; i<n; i++) codigosIES[i]=kernel.listaIES[i].codigo;
    Variable.CODIGO_IES.generarItems();
  }
  public void notificarCambioSeleccion(Filtro[] filtrosIES) {
    for (int k=0; k<Constantes.maxVariablesExtra; k++) {
      variablesExtras[k]=null;
      Variable.varsExtras[k].nombre="";
      Variable.varsExtras[k].generarItems();
    }
    for (int k=0; k<Constantes.maxVariablesExtraDinamicas; k++) {
      variablesExtrasD[k]=null;
      Variable.varsExtrasD[k].nombre="";
      Variable.varsExtrasD[k].generarItems();
    }
    if (filtrosIES!=null) {
      nombresProgramas=kernel.getProgramasActivos(filtrosIES);
      idsSemestres=kernel.getSemestresActivos(filtrosIES);
      IES[] IESpasan=kernel.getIES(filtrosIES);
      if (IESpasan.length==1) for (int k=0; k<Constantes.maxVariablesExtra; k++) {
        variablesExtras[k]=IESpasan[0].variablesExtras[k];
        Variable.varsExtras[k].nombre=new String(variablesExtras[k].nombre);
        Variable.varsExtras[k].generarItems();
      }
      if (IESpasan.length==1) for (int k=0; k<Constantes.maxVariablesExtraDinamicas; k++) {
        variablesExtrasD[k]=IESpasan[0].variablesExtrasD[k];
        Variable.varsExtrasD[k].nombre=new String(variablesExtrasD[k].nombre);
        Variable.varsExtrasD[k].generarItems();
      }
    }
    else {
      nombresProgramas=new String[]{"Todos"};
      idsSemestres=obtenerIdsSemestresAgregado();
    }
    Variable.PROGRAMA_EST.generarItems();
    Variable.PERIODO_INGRESO_EST.generarItems();
    Variable.ULTIMO_PERIODO_MATRICULADO_EST.generarItems();
    Variable.PERIODO_GRADO_EST.generarItems();
    Variable.PERIODO_IES_INICIO.generarItems();
    Variable.PERIODO_IES_FIN.generarItems();
    Variable.PERIODO_MATRICULADO_PER.generarItems();
    Variable.NUMERO_SEMESTRE_PER.generarItems();
  }
  public Integer[] getCodigosIES() {
    return codigosIES;
  }
  public String[] getNombresProgramas() {
    return nombresProgramas;
  }
  public Byte[] getIdsSemestres() {
    return idsSemestres;
  }
  public VariableExtra[] getVariablesExtras() {
    return variablesExtras;
  }
  public VariableExtra[] getVariablesExtrasD() {
    return variablesExtrasD;
  }
  private Byte[] idsSemestresAgregado=null;
  public Byte[] obtenerIdsSemestresAgregado() {
    if (idsSemestresAgregado==null) {
      try {
        idsSemestresAgregado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(6,Byte[].class);
      }
      catch (Throwable th) {
        idsSemestresAgregado=CajaDeHerramientas.getCodigosSemestresEntre(CajaDeHerramientas.getCodigoSemestre("19971"),CajaDeHerramientas.getCodigoSemestre("20082"));
      }
    }
    return idsSemestresAgregado;
  }
}
