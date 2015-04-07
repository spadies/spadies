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
package spadies.kernel;

import java.util.*;

public final class EstudianteDAO {
  public final IES ies;
  public final Estudiante datos;
  public final Estudiante_DatosPersonales datosPersonales;
  public final boolean[] estaMatriculado;
  public final double[] repitencias,supervivencias,supervivenciasEstructurales;
  public final int ind;
  public EstudianteDAO(IES pIes, Estudiante pDatos, Estudiante_DatosPersonales pDatosPersonales) {
    this(pIes, pDatos, pDatosPersonales,-1);
  }
  public EstudianteDAO(IES pIes, Estudiante pDatos, Estudiante_DatosPersonales pDatosPersonales, int pInd) {
    ies=pIes;
    datos=pDatos;
    datosPersonales=pDatosPersonales;
    int n=ies.n;
    estaMatriculado=new boolean[n];
    repitencias=datos.getRepitencias();
    supervivencias=new double[n];
    supervivenciasEstructurales=new double[n];
    Arrays.fill(supervivencias,-1d);
    Arrays.fill(supervivenciasEstructurales,-1d);
    ind = pInd;
    int jI=datos.getSemestrePrimiparo();
    if (jI==-1) return;
    double supv=-1d,supvEst=-1d;
    long matri=datos.getSemestresMatriculadoAlDerecho()>>>jI;
    for (int j=jI; j<n; j++,matri>>>=1) if ((matri&1L)==1L) {
      estaMatriculado[j]=true;
      double riesgo=datos.getRiesgo(j),riesgoEst=datos.getRiesgoEstructural(j);
      if (riesgo!=-1d) supv=(supv==-1d?1d:supv)*(1d-riesgo);
      if (riesgoEst!=-1d) supvEst=(supvEst==-1d?1d:supvEst)*(1d-riesgoEst);
      supervivencias[j]=(riesgo==-1d?-1d:supv);
      supervivenciasEstructurales[j]=(riesgoEst==-1d?-1d:supvEst);
    }
  }
}
