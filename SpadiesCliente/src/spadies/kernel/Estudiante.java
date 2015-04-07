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

import spadies.io.*;
public interface Estudiante {
/*
n="cantidad de semestres con datos de la IES"
DATOS GENERALES
   BYTE      BITS      CAMPO
   00        00-01     Sexo
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    0    Masculino
                           2    1    Femenino
   00        02-03     Vivienda propia
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    0    No posee
                           2    1    Si posee
   00        04-05     Trabajaba cuando presentó el ICFES
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    0    No trabajaba
                           2    1    Si trabajaba
 ----------------------------------------------------------------
 | 00        06        Los datos del ICFES los provee el SNIES? |
 |                         COD  VAL  DESC                       |
 |                         0    0    NO                         |
 |                         1    1    SI                         |
 ----------------------------------------------------------------
   00        07        Remplazo
   01        00-03     Nivel educativo de la madre
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    1    Básica primaria o inferior
                           2    2    Básica secundaria
                           3    3    Media vocacional o técnica/tecnológica
                           4    4    Universitaria o superior
   01        04-07     Ingreso del hogar
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    0    [0,1) salarios mínimos
                           2    1    [1,2) salarios mínimos
                           3    2    [2,3) salarios mínimos
                           4    3    [3,5) salarios mínimos
                           5    4    [5,7) salarios mínimos
                           6    5    [7,9) salarios mínimos
                           7    6    [9,11) salarios mínimos
                           8    7    [11,13) salarios mínimos
                           9    8    [13,15) salarios mínimos
                           10   9    [15,) salarios mínimos
   02        00-03     Número de hermanos
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    1    Ninguno
                           ...
                           15   15   Catorce o más
   02        04-07     Posición entre los hermanos
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    1    Primero
                           ...
                           15   15   Quinceavo o más
   03                  Puntaje del ICFES
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    0    0/100
                           ...
                           101  100  100/100
   04                  Edad al presentar el ICFES
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    0    0 años
                           ...
                           121  120  120 años
   05-07               Programa al que pertenece el estudiante
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    0    Pertenece al programa ies.programas[0]
                           ...
                           i    i-1  Pertenece al programa ies.programas[i-1]
                           ...
   08                  Semestre en que el estudiante entró a la IES como primíparo
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    0    Entró a la IES en el semestre ies.semestres[0]
                           ...
                           i    i-1  Entró a la IES en el semestre ies.semestres[i-1]
                           ...
   09                  Último semestre en que el estudiante se retiró forzosamente de la IES
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    0    Se retiró forzosamente de la IES en el semestre ies.semestres[0]
                           ...
                           i    i-1  Se retiró forzosamente de la IES en el semestre ies.semestres[i-1]
                           ...
   10                  Semestre en que el estudiante se graduó de la IES
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    0    Se graduó de la IES en el semestre ies.semestres[0]
                           ...
                           i    i-1  Se graduó de la IES en el semestre ies.semestres[i-1]
                           ...
   11-18               Semestres en que el estudiante aparece matriculado en la IES
                           64 bits donde el i-ésimo bit (0<=i<n) es 1 si y sólo si el estudiante aparece matriculado en la IES en el semestre ies.semestres[n-1-i]
   (19+0n)-(19+1n-1)   Número de materias tomadas del estudiante cada uno de los n semestres  
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    0    0
                           ...
                           101  100  100
   (19+1n)-(19+2n-1)   Número de materias aprobadas del estudiante cada uno de los n semestres  
                           COD  VAL  DESC
                           0    -1   Desconocido
                           1    0    0
                           ...
                           101  100  100
   (19+2n)-(19+3n-1)   Apoyos recibidos por el estudiante cada uno de los n semestres
                         PRIMEROS 3 BITS (APOYOS DE LA UNIVERSIDAD):
                           BIT 0: Recibió apoyo académico
                           BIT 1: Recibió otro tipo de apoyo
                           BIT 2: Recibió apoyo financiero 
                         ÚlTIMOS 4 BITS (APOYOS DEL ICETEX):
                           COD  VAL  DESC
                           0    0    Ninguno
                           1    1    Largo
                           2    2    Mediano
                           3    3    ACCES
   (19+3n)-(19+5n-1)   Riesgo del estudiante cada uno de los n semestres
                         n números de tipo short donde el i-ésimo número (0<=i<n) es el riesgo del estudiante en el semestre ies.semestres[i] multiplicado por 10000 y redondeado al entero más cercano
                           COD    VAL    DESC
                           0      -1.0   Desconocido
                           1      0.0000 0.0000
                           ...
                           10001  1.0000 1.0000
   (19+5n)-(19+7n-1)   Riesgo estructural del estudiante cada uno de los n semestres
                         n números de tipo short donde el i-ésimo número (0<=i<n) es el riesgo estructural del estudiante en el semestre ies.semestres[i] multiplicado por 10000 y redondeado al entero más cercano
                           COD    VAL    DESC
                           0      -1.0   Desconocido
                           1      0.0000 0.0000
                           ...
                           10001  1.0000 1.0000

NO
   (19+7n+0)-(19+8n-1)   Variable extra2 #1
   (19+8n+0)-(19+9n-1)   Variable extra2 #1
   (19+9n+0)-(19+10n-1)   Variable extra2 #1
   (19+10n+0)-(19+11n-1)   Variable extra2 #1
   (19+11n+0)-(19+12n-1)   Variable extra2 #1
NO
   (19+12n+0)           Variable extra #1
                           La codificación la da ies.variablesExtras[0]
   (19+13n+1)           Variable extra #2
                           La codificación la da ies.variablesExtras[1]
   (19+12n+2)           Variable extra #3
                           La codificación la da ies.variablesExtras[2]
   (19+12n+3)           Variable extra #4
                           La codificación la da ies.variablesExtras[3]
   (19+12n+4)           Variable extra #5
                           La codificación la da ies.variablesExtras[4]
   (19+12n+5)           Estado de desercion, solo tiene informaciòn si el estudiante es desertor
                           COD    VAL    DESC
                           0      -1     Desertor del sistema
                           1      -2     Desertor de la IES
                           2      -3     Desertor del programa
-------------------------------------------------------------------------
                           
                           
   (19+7n+0)           Variable extra #1
                           La codificación la da ies.variablesExtras[0]
   (19+7n+1)           Variable extra #2
                           La codificación la da ies.variablesExtras[1]
   (19+7n+2)           Variable extra #3
                           La codificación la da ies.variablesExtras[2]
   (19+7n+3)           Variable extra #4
                           La codificación la da ies.variablesExtras[3]
   (19+7n+4)           Variable extra #5
                           La codificación la da ies.variablesExtras[4]
   (19+7n+vE)          Estado de desercion, solo tiene informaciòn si el estudiante es desertor
                           COD    VAL    DESC
                           0      -1     Desertor del sistema
                           1      -2     Desertor de la IES
                           2      -3     Desertor del programa
   (20+7n+vE)-(19+8n+vE) Variable extra dinamica #1
   (20+8n+vE)-(19+9n+vE) Variable extra dinamica #2
   (20+9n+vE)-(19+10n+vE) Variable extra dinamica #3

*/
  public boolean getSonDatosICFESproveidosPorSNIES();
  public void setSonDatosICFESproveidosPorSNIES(boolean v);
  public byte getRemplazoICFES();
  public void setRemplazoICFES(boolean v);
  public byte tieneInformacionModeloEstructural();
  public void setSexo(int v);
  public byte getSexo();
  public void setViviendaPropia(int v);
  public byte getViviendaPropia();
  public void setTrabajabaCuandoPresentoIcfes(int v);
  public byte getTrabajabaCuandoPresentoIcfes();
  public void setNivelEducativoMadre(int v);
  public byte getNivelEducativoMadre();
  public void setIngresoHogar(int v);
  public byte getIngresoHogar();
  public void setNumeroHermanos(int v);
  public byte getNumeroHermanos();
  public void setPosicionEntreLosHermanos(int v);
  public byte getPosicionEntreLosHermanos();
  public void setEstrato(int v);
  public byte getEstrato();
  public void setNivelSisben(int v);
  public byte getNivelSisben();
  public void setPersonasFamilia(int v);
  public byte getPersonasFamilia();
  public void setIngresoHogar2(int v);
  public byte getIngresoHogar2();
  public void setPuntajeICFES(int v);
  public byte getPuntajeICFES();
  public void setEdadAlPresentarElICFES(int v);
  public byte getEdadAlPresentarElICFES();
  public void setIndicePrograma(int v);
  public int getIndicePrograma();
  public void setSemestrePrimiparo(int v);
  public int getSemestrePrimiparo();
  public void setSemestreRetiroForzoso(int v);
  public int getSemestreRetiroForzoso();
  public void setSemestreGrado(int v);
  public int getSemestreGrado();
  public void setPerIcfes(int v);
  public int getPerIcfes();
  public void setPerDatIcfes(int v);
  public int getPerDatIcfes();
  
  //Modas
  
  public void setmViviendaPropia(int v);
  public byte getmViviendaPropia();

  public void setmTrabajabaCuandoPresentoIcfes(int v);
  public byte getmTrabajabaCuandoPresentoIcfes();

  public void setmNivelEducativoMadre(int v);
  public byte getmNivelEducativoMadre();

  public void setmIngresoHogar(int v);
  public byte getmIngresoHogar();

  public void setmNumeroHermanos(int v);
  public byte getmNumeroHermanos();

  public void setmPosicionEntreLosHermanos(int v);
  public byte getmPosicionEntreLosHermanos();

  public void setmEstrato(int v);
  public byte getmEstrato();

  public void setmNivelSisben(int v);
  public byte getmNivelSisben();

  public void setmPersonasFamilia(int v);
  public byte getmPersonasFamilia();

  public void setmIngresoHogar2(int v);
  public byte getmIngresoHogar2();
  //TODO revisar
  public int getUltimoSemestreMatriculado();
  public void setSemestresMatriculadoAlReves(long v);
  public long getSemestresMatriculadoAlReves();
  public long getSemestresMatriculadoAlDerecho();
  public boolean estaMatriculado(int sem, long vReves);
  public void setNumeroMateriasTomadas(int sem, int v);
  public int getNumeroMateriasTomadas(int sem);
  public void setNumeroMateriasAprobadas(int sem, int v);
  public int getNumeroMateriasAprobadas(int sem);
  public void setRecibioApoyoAcademico(int sem, boolean v);
  public boolean getRecibioApoyoAcademico(int sem);
  public void setRecibioApoyoOtro(int sem, boolean v);
  public boolean getRecibioApoyoOtro(int sem);
  public void setRecibioApoyoFinanciero(int sem, boolean v);
  public boolean getRecibioApoyoFinanciero(int sem);
  public boolean getRecibioApoyoIES(int sem);
  public void setTipoApoyoICETEXRecibido(int sem, int v);
  public byte getTipoApoyoICETEXRecibido(int sem);
  public void setRiesgo(int sem, double v);
  public double getRiesgo(int sem);
  public void setRiesgoEstructural(int sem, double v);
  public double getRiesgoEstructural(int sem);
  public void setValorVariableExtra(int indiceVariableExtra, int v);
  public void setValorVariableExtra2(int indiceVariableExtra, int sem, int v);
  public byte getValorVariableExtra(int indiceVariableExtra);
  public byte getValorVariableExtra(int indiceVariableExtra, int sem);
  // MÉTODOS ANALIZADORES
  public byte getClaseRiesgo(int sem);
  public byte getEstado();
  public byte getEstadoExtra();
  public void setEstadoDesercion(byte v);
  public double[] getRepitencias();
  // MÉTODOS DE ENTRADA/SALIDA
  public void guardar(MyDataOutputStream os) throws Exception;
  public int getSize();
}