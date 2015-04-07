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
import spadies.io.*;
import spadies.util.*;

public abstract class EstudianteAbstract implements Estudiante {
  protected abstract int getTamFijo0();
  protected abstract int getTamFijo();
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
  private final byte n;
  private final MyByteSequence datos;
  public EstudianteAbstract(int pN, MyByteSequence pDatos) {
    assert(pDatos!=null);
    n=(byte)pN;
    //datos=(pDatos!=null)?pDatos:new MyByteSequence(tam);
    datos=pDatos;
  }
  public EstudianteAbstract(int pN, int tam) {
    n=(byte)pN;
    datos=new MyByteSequence(tam);
  }
  // MÉTODOS GET Y SET DE LOS ATRIBUTOS
  public boolean getSonDatosICFESproveidosPorSNIES() {
    return datos.getBoolean(0,6);
  }
  public void setSonDatosICFESproveidosPorSNIES(boolean v) {
    datos.setBoolean(0,6,v);
  }
  public byte getRemplazoICFES() {
    return (byte)(getPerIcfes()==-1?-1:(datos.getBoolean(0,7)?1:0));
  }
  public void setRemplazoICFES(boolean v) {
    datos.setBoolean(0,7,v);
  }
  public byte tieneInformacionModeloEstructural() {
    return (byte) (
        getSexo()==-1 || getEdadAlPresentarElICFES()==-1 || getPuntajeICFES()==-1
        || getIndicePrograma()==-1
        ?0:1);
  }
  public void setSexo(int v) {
    if (v>=-1 && v<=1) datos.setBits2(0,0,(byte)(v+1));
  }
  public byte getSexo() {
    return (byte)(datos.getBits2(0,0)-1);
  }
  public void setViviendaPropia(int v) {
    if (v>=-1 && v<=1) datos.setBits2(0,2,(byte)(v+1));
  }
  public byte getViviendaPropia() {
    return (byte)(datos.getBits2(0,2)-1);
  }
  public void setTrabajabaCuandoPresentoIcfes(int v) {
    if (v>=-1 && v<=1) datos.setBits2(0,4,(byte)(v+1));
  }
  public byte getTrabajabaCuandoPresentoIcfes() {
    return (byte)(datos.getBits2(0,4)-1);
  }
  public void setNivelEducativoMadre(int v) {
    if (v>=-1 && v<=4 && v!=0) datos.setBits4(1,0,(byte)(v==-1?0:v));
  }
  public byte getNivelEducativoMadre() {
    int v=datos.getBits4(1,0);
    return (byte)(v==0?-1:v);
  }
  public void setIngresoHogar(int v) {
    if (v>=-1 && v<=9) datos.setBits4(1,4,(byte)(v+1));
  }
  public byte getIngresoHogar() {
    return (byte)(datos.getBits4(1,4)-1);
  }
  public void setNumeroHermanos(int v) {
    if (v>=-1 && v<=15 && v!=0) datos.setBits4(2,0,(byte)(v==-1?0:v));
  }
  public byte getNumeroHermanos() {
    int v=datos.getBits4(2,0);
    return (byte)(v==0?-1:v);
  }
  public void setPosicionEntreLosHermanos(int v) {
    if (v>=-1 && v<=15 && v!=0) datos.setBits4(2,4,(byte)(v==-1?0:v));
  }
  public byte getPosicionEntreLosHermanos() {
    int v=datos.getBits4(2,4);
    return (byte)(v==0?-1:v);
  }
  public void setPuntajeICFES(int v) {
    if (v>=-1 && v<=100) datos.setByte(3,(byte)(v+1));
  }
  public byte getPuntajeICFES() {
    return (byte)(datos.getByte(3)-1);
  }
  public void setEdadAlPresentarElICFES(int v) {
    if (v>=-1 && v<=120) datos.setByte(4,(byte)(v+1));
  }
  public byte getEdadAlPresentarElICFES() {
    return (byte)(datos.getByte(4)-1);
  }
  public void setIndicePrograma(int v) {
    if (v>=-1 && v<=10000000) datos.setLittleInt(5,v+1);
  }
  public int getIndicePrograma() {
    return datos.getLittleInt(5)-1;
  }
  public void setSemestrePrimiparo(int v) {
    if (v>=-1 && v<n) datos.setByte(8,(byte)(v+1));
  }
  public int getSemestrePrimiparo() {
    return datos.getByte(8)-1;
  }
  public void setSemestreRetiroForzoso(int v) {
    if (v>=-1 && v<n) datos.setByte(9,(byte)(v+1));
  }
  public int getSemestreRetiroForzoso() {
    return datos.getByte(9)-1;
  }
  public void setSemestreGrado(int v) {
    if (v>=-1 && v<n) datos.setByte(10,(byte)(v+1));
  }
  public int getSemestreGrado() {
    return datos.getByte(10)-1;
  }
  public void setPerIcfes(int v) {
    if (v>=-1 && v<100) datos.setByte(11,(byte)(v+1));
  }
  public int getPerIcfes() {
    return datos.getByte(11)-1;
  }
  public void setPerDatIcfes(int v) {
    if (v>=-1 && v<100) datos.setByte(12,(byte)(v+1));
  }
  public int getPerDatIcfes() {
    return datos.getByte(12)-1;
  }
  public void setEstrato(int v) {
    datos.setBits4(13, 0, (byte)(v+1));
  }
  public byte getEstrato() {
    return (byte) (datos.getBits4(13, 0)-1);
  }
  public void setNivelSisben(int v) {
    datos.setBits4(13, 4, (byte)(v+1));
  }
  public byte getNivelSisben() {
    return (byte) (datos.getBits4(13, 4)-1);
  }
  public void setPersonasFamilia(int v) {
    datos.setBits4(14, 0, (byte)(v+1));
  }
  public byte getPersonasFamilia() {
    return (byte) (datos.getBits4(14, 0)-1);
  }
  public void setIngresoHogar2(int v) {
    datos.setBits4(14, 4, (byte)(v+1));
  }
  public byte getIngresoHogar2() {
    return (byte) (datos.getBits4(14, 4)-1);
  }
//Modas
  public void setmViviendaPropia(int v) {
    if (v>=-1 && v<=1) datos.setBits2(15,0,(byte)(v+1));
  }
  public byte getmViviendaPropia() {
    return (byte)(datos.getBits2(15,0)-1);
  }

  public void setmTrabajabaCuandoPresentoIcfes(int v) {
    if (v>=-1 && v<=1) datos.setBits2(15,2,(byte)(v+1));
  }
  public byte getmTrabajabaCuandoPresentoIcfes() {
    return (byte)(datos.getBits2(15,2)-1);
  }

  public void setmNivelEducativoMadre(int v) {
    if (v>=-1 && v<=4 && v!=0) datos.setBits4(15,4,(byte)(v==-1?0:v));
  }
  public byte getmNivelEducativoMadre() {
    int v=datos.getBits4(15,4);
    return (byte)(v==0?-1:v);
  }

  public void setmIngresoHogar(int v) {
    if (v>=-1 && v<=9) datos.setBits4(16,0,(byte)(v+1));
  }
  public byte getmIngresoHogar() {
    return (byte)(datos.getBits4(16,0)-1);
  }

  public void setmNumeroHermanos(int v) {
    if (v>=-1 && v<=15 && v!=0) datos.setBits4(17,0,(byte)(v==-1?0:v));
  }
  public byte getmNumeroHermanos() {
    int v=datos.getBits4(17,0);
    return (byte)(v==0?-1:v);
  }

  public void setmPosicionEntreLosHermanos(int v) {
    if (v>=-1 && v<=15 && v!=0) datos.setBits4(17,4,(byte)(v==-1?0:v));
  }
  public byte getmPosicionEntreLosHermanos() {
    int v=datos.getBits4(17,4);
    return (byte)(v==0?-1:v);
  }

  public void setmEstrato(int v) {
    datos.setBits4(18, 0, (byte)(v+1));
  }
  public byte getmEstrato() {
    return (byte) (datos.getBits4(18, 0)-1);
  }

  public void setmNivelSisben(int v) {
    datos.setBits4(18, 4, (byte)(v+1));
  }
  public byte getmNivelSisben() {
    return (byte) (datos.getBits4(18, 4)-1);
  }

  public void setmPersonasFamilia(int v) {
    datos.setBits4(19, 0, (byte)(v+1));
  }
  public byte getmPersonasFamilia() {
    return (byte) (datos.getBits4(19, 0)-1);
  }

  public void setmIngresoHogar2(int v) {
    datos.setBits4(19, 4, (byte)(v+1));
  }
  public byte getmIngresoHogar2() {
    return (byte) (datos.getBits4(19, 4)-1);
  }
  public void setPuntajeSBPRO(int v) {
    if (v>=-1 && v<=120) datos.setByte(20,(byte)(v+1));
  }
  public byte getPuntajeSBPRO() {
    return (byte)(datos.getByte(20)-1);
  }
  public void setEdadSBPRO(int v) {
    if (v>=-1 && v<=120) datos.setByte(21,(byte)(v+1));
  }

  public byte getEdadSBPRO() {
    return (byte)(datos.getByte(21)-1);
  }

  public void setAreaSBPRO(int v) {
    datos.setBits4(22, 0, (byte)(v+1));
  }

  public byte getAreaSBPRO() {
    return (byte)(datos.getBits4(22,0)-1);
  }

  public void setEducacionMadreSBPRO(int v) {
    datos.setBits4(22, 4, (byte)(v+1));
  }

  public byte getEducacionMadreSBPRO() {
    return (byte)(datos.getBits4(22,4)-1);
  }

  public void setOcupacionMadreSBPRO(int v) {
    datos.setBits4(23, 0, (byte)(v+1));
  }

  public byte getOcupacionMadreSBPRO() {
    return (byte)(datos.getBits4(23,0)-1);
  }

  public void setInternetSBPRO(int v) {
    datos.setBits2(23, 4, (byte)(v+1));
  }

  public byte getInternetSBPRO() {
    return (byte)(datos.getBits2(23,4)-1);
  }

  public void setValorMatriculaSBPRO(int v) {
    datos.setBits4(24, 0, (byte)(v+1));
  }

  public byte getValorMatriculaSBPRO() {
    return (byte)(datos.getBits4(24,0)-1);
  }
//
  
  public int getUltimoSemestreMatriculado() {
    int res = -1;
    long matri=this.getSemestresMatriculadoAlDerecho();
    for (int j=0,jT=n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) res = j;
    return res;
  }
  public void setSemestresMatriculadoAlReves(long v) {
    datos.setLong(getTamFijo0(),v);
  }
  public long getSemestresMatriculadoAlReves() {
    return datos.getLong(getTamFijo0());
  }
  public long getSemestresMatriculadoAlDerecho() {
    return Long.rotateLeft(Long.reverse(getSemestresMatriculadoAlReves()),n);
  }
  public boolean estaMatriculado(int sem, long vReves) {
    return ((vReves>>>(n-1-sem))&1L)==1L;
  }
  public void setNumeroMateriasTomadas(int sem, int v) {
    if (v>=-1 && v<=100) datos.setByte(getTamFijo()+sem,(byte)(v+1));
  }
  public int getNumeroMateriasTomadas(int sem) {
    return datos.getByte(getTamFijo()+sem)-1;
  }
  public void setNumeroMateriasAprobadas(int sem, int v) {
    if (v>=-1 && v<=100) datos.setByte(getTamFijo()+n+sem,(byte)(v+1));
  }
  public int getNumeroMateriasAprobadas(int sem) {
    return datos.getByte(getTamFijo()+n+sem)-1;
  }
  public void setRecibioApoyoAcademico(int sem, boolean v) {
    datos.setBoolean(getTamFijo()+2*n+sem,0,v);
  }
  public boolean getRecibioApoyoAcademico(int sem) {
    return datos.getBoolean(getTamFijo()+2*n+sem,0);
  }
  public void setRecibioApoyoOtro(int sem, boolean v) {
    datos.setBoolean(getTamFijo()+2*n+sem,1,v);
  }
  public boolean getRecibioApoyoOtro(int sem) {
    return datos.getBoolean(getTamFijo()+2*n+sem,1);
  }
  public void setRecibioApoyoFinanciero(int sem, boolean v) {
    datos.setBoolean(getTamFijo()+2*n+sem,2,v);
  }
  public boolean getRecibioApoyoFinanciero(int sem) {
    return datos.getBoolean(getTamFijo()+2*n+sem,2);
  }
  public boolean getRecibioApoyoIES(int sem) {
    return datos.getBits4(getTamFijo()+2*n+sem,0)!=0;
  }
  public void setTipoApoyoICETEXRecibido(int sem, int v) {
    if (v>=0 && v<=3) datos.setBits4(getTamFijo()+2*n+sem,4,(byte)v);
  }
  public byte getTipoApoyoICETEXRecibido(int sem) {
    return (byte)(datos.getBits4(getTamFijo()+2*n+sem,4));
  }
  public void setRiesgo(int sem, double v) {
    if (v==-1.0 || (v>=0.0 && v<=1.0)) datos.setShort(getTamFijo()+3*n+2*sem,(short)((v==-1.0)?0:(Math.round(v*10000)+1)));
  }
  public double getRiesgo(int sem) {
    int v=datos.getShort(getTamFijo()+3*n+2*sem);
    return (v==0)?-1.0:(1.0*(v-1)/10000);
  }
  public void setRiesgoEstructural(int sem, double v) {
    if (v==-1.0 || (v>=0.0 && v<=1.0)) datos.setShort(getTamFijo()+5*n+2*sem,(short)((v==-1.0)?0:(Math.round(v*10000)+1)));
  }
  public double getRiesgoEstructural(int sem) {
    int v=datos.getShort(getTamFijo()+5*n+2*sem);
    return (v==0)?-1.0:(1.0*(v-1)/10000);
  }
  public void setValorVariableExtra(int indiceVariableExtra, int v) {
    if (indiceVariableExtra>=0 && indiceVariableExtra<Constantes.maxVariablesExtra && v>=-1 && v<=99) datos.setByte(getTamFijo()+7*n+indiceVariableExtra,(byte)(v+1));
  }
  public void setValorVariableExtra2(int indiceVariableExtra, int sem, int v) {
    if (indiceVariableExtra>=0 && indiceVariableExtra<Constantes.maxVariablesExtraDinamicas && v>=-1 && v<=99) datos.setByte(getTamFijo()+7*n+Constantes.maxVariablesExtra+indiceVariableExtra*n+sem,(byte)(v+1));
  }
  public byte getValorVariableExtra(int indiceVariableExtra) {
    return (byte)((indiceVariableExtra>=0 && indiceVariableExtra<Constantes.maxVariablesExtra)?(datos.getByte(getTamFijo()+7*n+indiceVariableExtra)-1):-1);
  }
  public byte getValorVariableExtra(int indiceVariableExtra, int sem) {
    return (byte)((indiceVariableExtra>=0 && indiceVariableExtra<Constantes.maxVariablesExtra)?(datos.getByte(getTamFijo()+7*n+Constantes.maxVariablesExtra+indiceVariableExtra*n+sem)-1):-1);
  }
  // MÉTODOS ANALIZADORES
  public byte getClaseRiesgo(int sem) {
    double r=getRiesgo(sem);
    return (byte)(r==-1?-1:(r<0.04?1:(r<0.08?2:(r<0.12?3:(r<0.17?4:5)))));
  }
  public byte getEstado() {
    if (datos.getByte(10)!=0) return 1;      // Graduado
    if (datos.getByte(9)!=0) return 2;       // Retirado forzosamente
    if ((datos.getByte(getTamFijo0())&3)!=0) return 0;  // Activo
    return -1;                               // Desertor
  }
  public byte getEstadoExtra() {
    if (datos.getByte(10)!=0) return 1;      // Graduado
    if (datos.getByte(9)!=0) return 2;       // Retirado forzosamente
    if ((datos.getByte(getTamFijo0())&3)!=0) return 0;  // Activo
    byte  b = datos.getByte(getTamFijo()+7*n+Constantes.maxVariablesExtra);
    return b>-1?-1:b;                                // Desertor
  }
  public void setEstadoDesercion(byte v) {
    datos.setByte(getTamFijo()+7*n+Constantes.maxVariablesExtra,v);
  }
  public double[] getRepitencias() {
    double res[]=new double[n];
    Arrays.fill(res,-1d);
    int k=getSemestrePrimiparo();
    if (k==-1) return res;
    res[k]=0d;
    long matri=getSemestresMatriculadoAlDerecho()>>>(k+1);
    for (int i=k+1,j=k; i<n; i++,matri>>>=1) if ((matri&1L)==1L) {
      int mt1=getNumeroMateriasTomadas(i),mt2=getNumeroMateriasTomadas(j),ma2=getNumeroMateriasAprobadas(j);
      if (mt1!=-1 && mt2!=-1 && ma2!=-1 && mt1!=0 && mt2>=ma2) res[i]=Math.min(1d*(mt2-ma2)/mt1,1d);
      j=i;
    }
    return res;
  }
  // MÉTODOS DE ENTRADA/SALIDA
  public void guardar(MyDataOutputStream os) throws Exception {
    os.writeByteArray(false,datos.getBytes());
  }
  public int getSize() {
    return datos.getSize();
  }
}