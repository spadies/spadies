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

import static spadies.util.CajaDeHerramientas.codifLetrasServer;
import static spadies.util.CajaDeHerramientas.codifNumeros;
import static spadies.util.CajaDeHerramientas.intToString;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Map.Entry;

import spadies.kernel.individual.Aparicion;
import spadies.kernel.individual.DisjoinDataSet;
import spadies.kernel.individual.Individuo;
import spadies.kernel.individual.PrincipalMatch;
import spadies.util.*;
import spadies.util.variables.*;

public final class KernelSPADIES extends Observable {
  public static final boolean limtser = false;
  /**
   * Instancia unica de esta clase
   */
  private static final KernelSPADIES instance=new KernelSPADIES();
  /**
   * Metodo que permite obtener la unica instancia de la clase
   */
  public static KernelSPADIES getInstance() {return instance;}
  /**
   * Listado de IES cargadas en esta instancia de Kernel 
   */
  public IES[] listaIES={};
  /**
   * Mapa que contiene [Código SNIES de la IES] => [IES] para todas las IES cargadas en el sistema.
   */
  public Map<Integer,IES> mapIESporCodigo=new TreeMap<Integer,IES>();
  private KernelSPADIES() {}
  /**
   * Carga los archivos de extension <i>spa</i> y nombre numerico que se encuentren en la carpeta especificada.  
   * @param carpeta 
   * @param listener Parametro opcional, se le envie tras cada IES cargadas un arreglo de enteros con un consecutivo y el numero de IES cargadas hasta el momento 
   * @return Tiempo que tomo la carga en milisegundos
   * @throws MyException
   */
  public long cargar(File carpeta, MyListener listener) throws MyException {
	  System.out.println("cargar");
	  return cargar(carpeta, true, listener);
  }
  public long cargar(File carpeta, boolean cargadp, MyListener listener) throws MyException {
    long tm=System.currentTimeMillis();
    if (!carpeta.exists() || !carpeta.isDirectory()) throw new MyException("No se encuentra la carpeta \""+carpeta.getPath()+"\".");
    listaIES=new IES[0]; mapIESporCodigo.clear(); System.gc();
    List<IES> nuevaListaIES=new ArrayList<IES>(100);
    List<File> listaArchivos=new LinkedList<File>();
    {
      File[] archivos=carpeta.listFiles();
      if (archivos!=null) for (File f:archivos)
        if (CajaDeHerramientas.esArchivoIESFiltro(f, Constantes.filtroIES))
          listaArchivos.add(f);
    }
    int k=0;
    for (File f:listaArchivos) {
      try {
        nuevaListaIES.add(IES.cargar(f,cargadp));
        if (listener!=null) listener.notify(new int[]{++k,listaArchivos.size()});
      }
      catch (MyException ex) {
        throw ex;
      }
      catch (OutOfMemoryError err) {
        throw new MyException("Memoria RAM insuficiente para ejecutar el proceso.");
      }
      catch (Throwable th) {
        throw new MyException("Hubo un error cargando el archivo \""+f.getPath()+"\".");
      }
    }
    listaIES=nuevaListaIES.toArray(new IES[0]);
    for (IES ies:listaIES) mapIESporCodigo.put(ies.codigo,ies);
    System.gc();
    AmbienteVariables.getInstance().notificarCarga();
    censurarDesercionInterSemestral();
    tm=System.currentTimeMillis()-tm;
    setChanged();
    notifyObservers("CARGA");
    PrincipalMatch.cruzarKernel(this);
    return tm;
    
  }
  /**
   * By gSotelo. Para el SPADIES individual
   * @return
   */
  public byte[][][] getInfoBytes(){
	  int s=0;
	  for(IES i:listaIES)if(i.datosPersonalesEstudiantes==null)new File("datos\\"+i.codigo+".spa").delete();
	  for(IES i:listaIES)s+=i.datosPersonalesEstudiantes.length;
	  byte[][][] ret=new byte[3][s][];
	  s=0;
	  for(IES i:listaIES){
		  for(Estudiante_DatosPersonales edp:i.datosPersonalesEstudiantes){
			  ret[0][s]=codifLetrasServer.getCodigos(new String(edp.apellido)+" "+new String(edp.nombre));
			  ret[1][s]=codifNumeros.getCodigos((edp.documento!=-1)?(""+edp.documento):"");
		      ret[2][s]=codifNumeros.getCodigos((edp.anhoFechaNacimiento!=-1 && edp.mesFechaNacimiento!=-1 && edp.diaFechaNacimiento!=-1)?(intToString(edp.anhoFechaNacimiento%100,2)+intToString(edp.mesFechaNacimiento,2)+intToString(edp.diaFechaNacimiento,2)):"");
			  s++;
		  }
	  }
	  return ret;
  }
  
  /**
   * 
   * @param carpeta
   * @param periodoActualizacion
   * @see KernelSPADIES#cargarParaServidor(File, long, boolean)
   * @throws MyException
   */
  public void cargarParaServidor(File carpeta, long periodoActualizacion) throws MyException {
    cargarParaServidor(carpeta, periodoActualizacion, false);
  }
  /**
   * Carga de la carpeta especificada los archivos <i>spa</i>, 
   * @param carpeta
   * @param periodoActualizacion
   * @param cargarDatosPersonales
   * @throws MyException
   */
  public void cargarParaServidor(File carpeta, long periodoActualizacion, boolean cargarDatosPersonales) throws MyException {
    System.gc();
    if (!carpeta.exists() || !carpeta.isDirectory()) throw new MyException("No se encuentra la carpeta \""+carpeta.getPath()+"\".");
    List<String> errores=new LinkedList<String>();
    List<IES> nuevaListaIES=new ArrayList<IES>(100);
    File[] archivos=carpeta.listFiles();
    Arrays.sort(archivos);
    long tmi=System.currentTimeMillis(),tm1=tmi-periodoActualizacion*2,tm2=tmi-1000L*60*2;
    if (archivos!=null) for (File f:archivos) if (CajaDeHerramientas.esArchivoIESFiltro(f, Constantes.filtroIES)) {
      try {
        String s=f.getName();
        Integer cod=CajaDeHerramientas.stringToInteger(s.substring(0,s.indexOf('.')));
        if (cod==-1) throw new Exception("");
        if (cod==9999) continue;
        IES ies=mapIESporCodigo.get(cod);
        long ftm=f.lastModified();
        if (ies==null || (ftm>tm1 && ftm<tm2)) ies=IES.cargar(f,cargarDatosPersonales);
        nuevaListaIES.add(ies);
      }
      catch (Throwable th) {
        errores.add(f.getName());
      }
    }
    mapIESporCodigo.clear();
    listaIES=nuevaListaIES.toArray(new IES[0]);
    for (IES ies:listaIES) mapIESporCodigo.put(ies.codigo,ies);
    System.gc();
    if (!errores.isEmpty()) {
      String err="Hubo problemas cargando los siguientes archivos: ",wErr="";
      for (String s:errores) wErr+=(wErr.length()==0?"":",")+s;
      throw new MyException(err+wErr);
    }
    censurarDesercionInterSemestral();
  }
  /**
   * Evalua si un conjunto de parametros cumple con un conjunto de filtros
   * @param filtros Filtros a aplicar
   * @param args Parametros a evaluar
   * @return True si los argumentos cumplieron todos los filtros, False de los contrario
   */
  private static boolean pasaFiltros(Filtro[] filtros, Object...args) {
    for (Filtro ft:filtros) if (!ft.pasaFiltro(args)) return false;
    return true;
  }
  private static boolean pasaFiltros(SFiltro[] filtros, Object...args) {
    for (SFiltro ft:filtros) if (!ft.pasaFiltro(args)) return false;
    return true;
  }
  /**
   * Retorna una IES dado su codigo SNIES
   * @param codigo Codigo SNIES de la IES que se obtendra 
   * @return Null si no hay cargada una IES con el codigo especificado, la IES de lo contrario.
   */
  public IES getIES(Integer codigo) {
    return (codigo==null)?null:mapIESporCodigo.get(codigo);
  }
  /**
   * Returna un arreglo de las IES que cumplen con un conjunto de filtros sobre los datos de IES.
   * @param filtrosIES Filtros con los que se restringen las IES que se quieren obtener.
   * @return Arreglo de IES
   */
  public IES[] getIES(Filtro[] filtrosIES) {
    List<IES> res=new ArrayList<IES>(10);
    for (IES ies:listaIES) if (pasaFiltros(filtrosIES,ies)) res.add(ies);
    return res.toArray(new IES[0]);
  }
  /**
   * Para un conjunto de IES que retorna un arreglo con [codigo del minimo periodo en las IES seleccionadas, codigo del minimo periodo en las IES seleccionadas] 
   * @param lasIES Arreglo de IES
   * @return Arreglo que en su primera posicion tiene el codigo del minimo periodo entre las IES seleccionadas, y en la segunda el codigo del maximo periodo.  
   */
  public static int[] getLimsCodigoSemestre(IES[] lasIES) {
    int minCod=Integer.MAX_VALUE,maxCod=Integer.MIN_VALUE;
    for (IES ies:lasIES) {
      minCod=Math.min(minCod,ies.minCodigoSemestre);
      maxCod=Math.max(maxCod,ies.maxCodigoSemestre);
    }
    return new int[]{minCod,maxCod};
  }
  /**
   * Retorna el conteo de datos por periodo y tipo para las IES que cumplen el filtro especificado. 
   * @param filtrosIES Arreglo de filtros sobre atributos de las IES 
   * @return Retorna un arreglo con el siguiente formato:<ul>
   * <li>Posición 0: String[filas][columnas], contiene el cuerpo de la tabla, los conteos de cada periodo/tipo</li>
   * <li>Posición 1: String[columnas][filas] , su primer eje son las filas, su segundo las columnas</li>
   * </ul>
   */
  public Object[] getTablaCantidadArchivos(Filtro[] filtrosIES) {
    IES[] lasIES=getIES(filtrosIES);
    int limsSems[]=getLimsCodigoSemestre(lasIES),limInf=limsSems[0],limSup=limsSems[1];
    if (limInf==Integer.MAX_VALUE) return new Object[]{null,null,null};
    int tam=limSup-limInf+1,cantidades[][]=new int[tam][9];
    //primiparos,matriculados,retirosForzosos,graduados,materiasTomadas,materiasAprobadas,apoyosAcademicos,apoyosFinancieros,otrosApoyos
    for (IES ies:lasIES) {
      int mcs=ies.minCodigoSemestre;
      for (Estudiante e:ies.estudiantes) {
        int iPrimi=e.getSemestrePrimiparo();
        int iRetFo=e.getSemestreRetiroForzoso();
        int iGrado=e.getSemestreGrado();
        if (iPrimi==-1) continue;
        cantidades[mcs+iPrimi-limInf][0]++;
        if (iRetFo!=-1) cantidades[mcs+iRetFo-limInf][2]++;
        if (iGrado!=-1) cantidades[mcs+iGrado-limInf][3]++;
        long matri=e.getSemestresMatriculadoAlDerecho()>>>iPrimi;
        for (int i=iPrimi,iT=ies.n; i<iT; i++,matri>>>=1) if ((matri&1L)==1L) {
          int cod=mcs+i-limInf;
          int matTom=e.getNumeroMateriasTomadas(i);
          int matApr=e.getNumeroMateriasAprobadas(i);
          cantidades[cod][1]++;
          if (matTom!=-1) cantidades[cod][4]+=matTom;
          if (matApr!=-1) cantidades[cod][5]+=matApr;
          if (e.getRecibioApoyoAcademico(i)) cantidades[cod][6]++;
          if (e.getRecibioApoyoFinanciero(i)) cantidades[cod][7]++;
          if (e.getRecibioApoyoOtro(i)) cantidades[cod][8]++;
        }
      }
    }
    String valores[][]=new String[tam][9],encColumnas[]={"Primíparos","Matriculados"/*,"Retiros forzosos"*/,"Retiros disciplinarios","Graduados","Materias tomadas","Materias aprobadas","Apoyos académicos","Apoyos financieros","Otros apoyos"},encFilas[]=new String[tam+1];
    encFilas[0]="";
    for (int k=0; k<tam; k++) {
      encFilas[k+1]=CajaDeHerramientas.codigoSemestreToString((byte)(k+limInf));
      for (int j=0; j<9; j++) valores[k][j]=""+cantidades[k][j];
    }
    return new Object[]{valores,new String[][]{encFilas},encColumnas};
  }
  public int getCantidadEstudiantes(Filtro[] filtrosIES) {
    int r=0;
    for (IES ies:getIES(filtrosIES)) r+=ies.estudiantes.length;
    return r;
  }
  public Object[] cohortes(Filtro[] filtrosIES) {
    IES[] lasIES=getIES(filtrosIES);
    int limsSems[]=getLimsCodigoSemestre(lasIES),limInf=limsSems[0],limSup=limsSems[1];
    if (limInf==Integer.MAX_VALUE) return new Object[]{null,null,null};
    int tam=limSup-limInf+1,cohortes[][]=new int[tam][tam];
    for (IES ies:lasIES) {
      int mcs=ies.minCodigoSemestre;
      for (Estudiante e:ies.estudiantes) {
        int jI=e.getSemestrePrimiparo();
        if (jI==-1) continue;
        long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
        for (int j=jI,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) cohortes[mcs+jI-limInf][mcs+j-limInf]++;
      }
    }
    String tabla[][]=new String[tam][tam],sems[]=CajaDeHerramientas.textoSemestresToString(CajaDeHerramientas.getTextosSemestresEntre((byte)limInf,(byte)limSup));
    DecimalFormat df=new DecimalFormat("0.00");
    for (int j=0; j<tam; j++) {
      int primero=0,anterior=0;
      for (int i=0; i<tam; i++) {
        int k=cohortes[j][i];
        if (k!=0 && primero==0) primero=k;
        tabla[j][i]=(k==0)?"":(((anterior!=0 && k>anterior)?"!":"")+df.format(100.0*k/primero)+"%");
        if (k!=0) anterior=k;
      }
    }
    return new Object[]{tabla,new String[][]{CajaDeHerramientas.concatenarArreglos(String.class,new String[]{""},sems)},sems};
  }
  public Byte[] getSemestresActivos(Filtro[] filtrosIES) {
    int limsSems[]=getLimsCodigoSemestre(getIES(filtrosIES)),limInf=limsSems[0],limSup=limsSems[1];
    return (limInf==Integer.MAX_VALUE)?new Byte[]{-1}:CajaDeHerramientas.getCodigosSemestresEntre((byte)limInf,(byte)limSup);
  }
  public String[] getProgramasActivos(Filtro[] filtrosIES) {
    Set<String> res=new TreeSet<String>();
    for (IES ies:getIES(filtrosIES)) for (Programa p:ies.programas) res.add(p.toString());
    res.add("");
    return res.toArray(new String[0]);
  }
  @SuppressWarnings("unchecked")
  public Object[] getInformacionBasicaCriterio(Filtro[] filtrosIES, Variable variable) {
    // Criterios:
    //   SEXO_EST,TRABAJABA_CUANDO_ICFES_EST,CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST,INGRESO_HOGAR_EST,VIVIENDA_PROPIA_EST,NUMERO_HERMANOS_EST,POSICION_ENTRE_LOS_HERMANOS_EST,CLASIFICACION_PUNTAJE_ICFES_EST,NIVEL_EDUCATIVO_MADRE_EST,AREA_CONOCIMIENTO_EST
    //   ICETEX_RECIBIDO_PER,TIPO_ICETEX_RECIBIDO_PER,APOYO_RECIBIDO_PER
    IES[] lasIES=getIES(filtrosIES);
    int limsSems[]=getLimsCodigoSemestre(lasIES),limInf=limsSems[0],limSup=limsSems[1];
    if (limInf==Integer.MAX_VALUE) return null;
    RangoByte<Byte> rango=(RangoByte<Byte>)(variable.rango);
    int tam=limSup-limInf+1,max=rango.getMaxRango(),res[][]=new int[max+2][tam];
    for (IES ies:lasIES) {
      int mcs=ies.minCodigoSemestre;
      for (Estudiante e:ies.estudiantes) {
        int jI=e.getSemestrePrimiparo();
        if (jI!=-1) switch (variable.tipo) {
          case TV_ESTUDIANTE:
            res[rango.getRango((Byte)(variable.getValor(e,ies)))+1][mcs+jI-limInf]++;
            break;
          case TV_PERIODO_ESTUDIANTE:
            long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
            for (int j=jI,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
              byte val=(Byte)(variable.getValor(e,ies,j,null));
              if (variable==Variable.TIPO_ICETEX_RECIBIDO_PER && val==0) continue;
              res[rango.getRango(val)+1][mcs+j-limInf]++;
            }
            break;
        }
      }
    }
    return new Object[]{res,limsSems};
  }
  public Object[] getConteoPoblacion3(Filtro[] filtros, Variable[] diferenciados) throws MyException {
    Object[] pre = getConteoPoblacion2(filtros,diferenciados);
    //int tam = (Integer) pre[4];
    Map<byte[],double[]> resP = (Map<byte[], double[]>) pre[1];
    Map<byte[],double[][]> res = new TreeMap<byte[],double[][]>(compByteArrayEspecial);
    int min = 100, max=-1;
    for (byte[] key:resP.keySet()) {
      int val = key[0]&0xFF;
      if (val==-1) continue;
      if (val<min) min=val;
      if (val>max) max=val;
    }
    int tam = max - min + 1;
    for (Entry<byte[],double[]> ent:resP.entrySet()) {
      byte[] keyp = ent.getKey();
      byte val = keyp[0];
      double[] ser = ent.getValue();
      byte[] key = new byte[keyp.length-1];
      System.arraycopy(keyp, 1, key, 0, key.length);
      double[][] sern = res.get(key);
      if (sern==null) res.put(key,sern=new double[tam][tam]);
      sern[val-min] = ser;
    }
    return new Object[]{res,pre[2],pre[3],new int[]{min,max}};
  }
  public Object[] getConteoPoblacion2(Filtro[] filtros, Variable[] diferenciados) throws MyException {
    Variable[] diferenciados2 = new Variable[diferenciados.length+1];
    diferenciados2[0] = Variable.PERIODO_INGRESO_EST;
    for (int i=0;i<diferenciados.length;i++) diferenciados2[i+1] = diferenciados[i];
    //System.arraycopy(diferenciados, 0, diferenciados2, 1, diferenciados.length);
    //Object[] res = getConteoPoblacion(filtros,diferenciados2);
    Object[] res = null; //TODO Aqui se macheteo para que no sacara error
    return res; 
  }
  /**
   * 
   * @param filtros
   * @param diferenciados
   * @return {Map<byte[],int[]>,Map<byte[],double[]>,---,---,int}
   * @throws MyException
   */
  @SuppressWarnings("unchecked")
  public Object[] getConteoPoblacion(SFiltro[] filtros, SVariable[] diferenciados) throws MyException {
    Map<byte[],int[]> resC=new TreeMap<byte[],int[]>(compByteArrayEspecial);
    Map<byte[],int[]> serieRef=new TreeMap<byte[],int[]>(compByteArrayEspecial);
    int limsSems[]=getLimsCodigoSemestre(listaIES),limInf=limsSems[0],limSup=limsSems[1];
    if (limInf==Integer.MAX_VALUE) return null;
    int tam=limSup-limInf+1;
    
    cicloIndividuo:for(Individuo individuo:DisjoinDataSet.getIndividuosIdsRelativos(this))if(individuo!=null){
		byte llave[]=new byte[diferenciados.length];
		for(int h=0;h<diferenciados.length;h++){
			SVariable v=diferenciados[h];
			Rango rango=v.rango;
			Comparable ran=rango.getRango(v.getValor(individuo));
			llave[h]=rango.rangoToByte(ran);  
		}
		Aparicion iesPrimiparo=individuo.getPrimiparoPrimeraVez();
		if(iesPrimiparo==null||!pasaFiltros(filtros,individuo))continue;
        
		int jI=iesPrimiparo.estudiante.getSemestrePrimiparo()+iesPrimiparo.ies.minCodigoSemestre;
    	boolean entra=false;
        long matri=individuo.getSemestresMatriculadoAlDerecho(this);
        for (int j=jI,jT=individuo.getUltimoPeriodoFuturoEstudiante(); j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
          byte llavePer[]=llave.clone();
          int serieDatos[]=resC.get(llavePer);
          if (serieDatos==null) {
            resC.put(llavePer,serieDatos=new int[tam]);
            if (limtser && resC.size()>120) throw new MyException("Los criterios de diferenciación suministrados arrojan más de 120 series de datos. Refine la consulta.");
          }
          serieDatos[j-jI]++;
          entra=true;
        }
        if (entra) {
          int datosRef[]=serieRef.get(llave);
          if (datosRef==null) serieRef.put(llave,datosRef=new int[tam]);
          for (int i=0,iT=individuo.getUltimoPeriodoFuturoEstudiante()-jI; i<iT; i++) datosRef[i]++;
        }
    }
    if (resC.isEmpty()) return null;
    Map<byte[],double[]> resP=new TreeMap<byte[],double[]>(compByteArrayEspecial);
    for (Map.Entry<byte[],int[]> e:resC.entrySet()) {
      byte[] llavePer=e.getKey(),llaveEst=llavePer.clone();
      int[] cont=e.getValue(),contRef=serieRef.get(llaveEst);
      double[] porc=new double[tam];
      boolean b=false;
      for (int i=0; i<tam; i++) if (cont[i]>0 && contRef[i]>10) {porc[i]=1d*cont[i]/contRef[i]; b=true;}
      if (b) resP.put(llavePer,porc);
    }
    return new Object[]{resC,resP,null,null,tam};
  }
  @SuppressWarnings("unchecked")
  public Object[] getConteoPoblacionSemestral(Filtro[] filtros, Variable[] diferenciados) throws MyException {
    Integer[] codigosIESDif=null;       // Caso especial de diferenciación #1
    String[] codigosProgramasDif=null;  // Caso especial de diferenciación #2
    boolean calcularRepitencias=false;
    Filtro[] filtrosIES=Variable.filtrarFiltros(filtros,TipoVariable.TV_IES);
    Filtro[] filtrosEst=Variable.filtrarFiltros(filtros,TipoVariable.TV_ESTUDIANTE);
    Filtro[] filtrosPer=Variable.filtrarFiltros(filtros,TipoVariable.TV_PERIODO_ESTUDIANTE);
    {
      Map<Variable,Filtro> mapF=new EnumMap<Variable,Filtro>(Variable.class);
      for (Filtro ft:filtros) mapF.put(ft.variable,ft);
      for (Variable v:diferenciados) if (v==Variable.CODIGO_IES || v==Variable.PROGRAMA_EST) {
        Filtro ft=mapF.get(v);
        Comparable[] keys=(ft==null)?Item.getKeys(v.items):ft.filtro;
        int t=keys.length;
        //if (t>100) throw new MyException("No se puede diferenciar la variable \""+v.nombre+"\" por más de 100 valores.");
        switch (v) {
          case CODIGO_IES:
            IES[] pi = this.getIES(filtrosIES);
            t = pi.length;
            if (t>200) throw new MyException("No se puede diferenciar la variable \""+v.nombre+"\" por más de 100 valores.");
            codigosIESDif=new Integer[t];
            for (int i=0; i<t; i++) codigosIESDif[i]=(Integer)pi[i].codigo;
            break;
          case PROGRAMA_EST:
            String[] pa = this.getProgramasActivos(filtrosIES);
            t = pa.length;
            if (t>200) throw new MyException("No se puede diferenciar la variable \""+v.nombre+"\" por más de 100 valores.");
            codigosProgramasDif=new String[t];
            for (int i=0; i<t; i++) codigosProgramasDif[i]=(String)pa[i];
            break;
        }
      }
      calcularRepitencias=mapF.containsKey(Variable.REPITENCIA_PER)||Arrays.asList(diferenciados).contains(Variable.REPITENCIA_PER);
    }
    int m=diferenciados.length,indicesDifIES[],indicesDifEst[],indicesDifPer[];
    {
      List[] listas=new List[3];
      for (int i=0; i<3; i++) listas[i]=new ArrayList<Integer>(10);
      for (int h=0; h<m; h++) {
        switch (diferenciados[h].tipo) {
          case TV_IES:                listas[0].add((Integer)h); break;
          case TV_ESTUDIANTE:         listas[1].add((Integer)h); break;
          case TV_PERIODO_ESTUDIANTE: listas[2].add((Integer)h); break;
        }
      }
      indicesDifIES=CajaDeHerramientas.toIntArray(listas[0]);
      indicesDifEst=CajaDeHerramientas.toIntArray(listas[1]);
      indicesDifPer=CajaDeHerramientas.toIntArray(listas[2]);
    }
    Map<byte[],int[]> resC=new TreeMap<byte[],int[]>(compByteArrayEspecial);
    IES[] lasIES=getIES(filtrosIES);
    int limsSems[]=getLimsCodigoSemestre(lasIES),limInf=limsSems[0],limSup=limsSems[1];
    if (limInf==Integer.MAX_VALUE) return null;
    int tam=limSup-limInf+1;
    int[] serieRef=new int[tam];
    for (IES ies:lasIES) {
      int mcs=ies.minCodigoSemestre;
      byte llaveIES[]=new byte[m];
      for (int h:indicesDifIES) {
        Variable v=diferenciados[h];
        Rango rango=v.rango;
        Comparable ran=rango.getRango(v.getValor(ies));
        llaveIES[h]=(v==Variable.CODIGO_IES)?((byte)Arrays.binarySearch(codigosIESDif,ran)):(rango.rangoToByte(ran));
      }
      for (Estudiante e:ies.estudiantes) {
        int jI=e.getSemestrePrimiparo();
        if (jI==-1 || !pasaFiltros(filtrosEst,e,ies)) continue;
        byte llaveEst[]=llaveIES.clone();
        for (int h:indicesDifEst) {
          Variable v=diferenciados[h];
          Rango rango=v.rango;
          Comparable ran=rango.getRango(v.getValor(e,ies));
          llaveEst[h]=(v==Variable.PROGRAMA_EST)?((byte)Arrays.binarySearch(codigosProgramasDif,ran)):(rango.rangoToByte(ran));
        }
        long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
        double repitencias[]=calcularRepitencias?e.getRepitencias():null;
        for (int j=jI,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L && pasaFiltros(filtrosPer,e,ies,j,repitencias)) {
          byte llavePer[]=llaveEst.clone();
          for (int h:indicesDifPer) {
            Variable v=diferenciados[h];
            Rango rango=v.rango;
            Comparable ran=rango.getRango(v.getValor(e,ies,j,repitencias));
            llavePer[h]=rango.rangoToByte(ran);
          }
          int serieDatos[]=resC.get(llavePer);
          if (serieDatos==null) {
            resC.put(llavePer,serieDatos=new int[tam]);
            if (limtser && resC.size()>20) throw new MyException("Los criterios de diferenciación suministrados arrojan más de 20 series de datos. Refine la consulta.");
          }
          serieDatos[mcs+j-limInf]++;
          serieRef[mcs+j-limInf]++;
        }
      }
    }
    if (resC.isEmpty()) return null;
    Map<byte[],double[]> resP=new TreeMap<byte[],double[]>(compByteArrayEspecial);
    for (Map.Entry<byte[],int[]> e:resC.entrySet()) {
      byte[] llavePer=e.getKey(),llaveEst=llavePer.clone();
      for (int h:indicesDifPer) llaveEst[h]=0;
      int[] cont=e.getValue(),contRef=serieRef;
      double[] porc=new double[tam];
      boolean b=false;
      for (int i=0; i<tam; i++) if (cont[i]>0 && contRef[i]>10) {porc[i]=1d*cont[i]/contRef[i]; b=true;}
      if (b) resP.put(llavePer,porc);
    }
    return new Object[]{resC,resP,codigosIESDif,codigosProgramasDif,limsSems};
  }
  /**
   * @deprecated
   * @param filtrosIES
   * @return
   */
  public Object[] getPorcentajeDesercionPorCohorte(Filtro[] filtrosIES) {
    return getPorcentajeDesercionPorCohorte(filtrosIES, true);
  }
  public Object[] getPorcentajeDesercionPorCohorte(Filtro[] filtrosIES, boolean porcentual) {
    IES[] lasIES=listaIES;
    int limsSems[]=getLimsCodigoSemestre(lasIES),limInf=limsSems[0],limSup=limsSems[1];
    if (limInf==Integer.MAX_VALUE) return null;
    int tam=limSup-limInf+1,conteoPrimiparos[]=new int[tam],conteo[][]=new int[tam][tam];
    cicloIndividuo:for(List<int[]> individuo:DisjoinDataSet.getIndividuosIdsRelativos(this)){
		boolean tienePrimerSemestre=false;
		boolean apareceComoDesertor=true;
		int semestrePrimiparo=Integer.MAX_VALUE;
		int[] semestreDesertor=null;
    	for(int[] aparicion:individuo){
			IES ies=lasIES[aparicion[0]];
			int mcs=ies.minCodigoSemestre;
    		Estudiante e=ies.estudiantes[aparicion[1]];

			int jI=e.getSemestrePrimiparo();
			if (jI==-1) continue ;//No aparece como primiparo para esta IES
			tienePrimerSemestre=true;
			if (jI>=ies.n-2) continue cicloIndividuo;  // Estos estudiantes nunca se consideran desertores; afectarían el total si se consideraran
			semestrePrimiparo=Math.min(semestrePrimiparo,mcs+jI-limInf);
			if (e.getEstado()==-1) {
				int i=ies.n-Long.numberOfTrailingZeros(e.getSemestresMatriculadoAlReves())-jI;
				semestreDesertor=new int[]{mcs+jI-limInf,i-1};
			}else apareceComoDesertor=false;
    	}
    	if(semestrePrimiparo!=Integer.MAX_VALUE){
    		conteoPrimiparos[semestrePrimiparo]++;
        	if(!apareceComoDesertor)conteo[semestreDesertor[0]][semestreDesertor[1]]++;
    	}
    }
    
    double res[][]=new double[tam][tam];
    for (int j=0; j<tam; j++) {
      Arrays.fill(res[j],Double.MAX_VALUE);
      int t=conteoPrimiparos[j];
      if (t==0) continue;
      for (int i=0,acum=0; i<tam; i++) {
        int v=conteo[j][i];
        acum+=v;
        res[j][i]=porcentual?100d*acum/t:acum;
      }
      double ult=res[j][tam-1];
      for (int i=tam-2; i>=0 && Math.abs(res[j][i]-ult)<1e-3; i--) res[j][i+1]=Double.MAX_VALUE;
    }
    return new Object[]{res,limsSems};
  }
  @SuppressWarnings("unchecked")
  public Object[] getTablaNivelAprobacionDesertores(Filtro[] filtrosIES) {
    IES[] lasIES=getIES(filtrosIES);
    int limsSems[]=getLimsCodigoSemestre(lasIES),limInf=limsSems[0],limSup=limsSems[1];
    if (limInf==Integer.MAX_VALUE) return null;
    RangoByte<int[]> rangoNA=(RangoByte<int[]>)(Variable.NIVEL_APROBACION_PER.rango);
    int tam=limSup-limInf+1,max=rangoNA.getMaxRango(),conteo[][]=new int[max+2][tam];
    for (IES ies:lasIES) for (Estudiante e:ies.estudiantes) {
      int jI=e.getSemestrePrimiparo();
      if (jI==-1) continue;
      if (e.getEstado()==-1) {
        int i=ies.n-Long.numberOfTrailingZeros(e.getSemestresMatriculadoAlReves())-jI,semSalida=jI+i-1;
        conteo[rangoNA.getRango(new int[]{e.getNumeroMateriasTomadas(semSalida),e.getNumeroMateriasAprobadas(semSalida)})+1][i-1]++;
      }
    }
    return new Object[]{conteo};
  }
  public Object[] getDatosTablaPresentacionModelo(Filtro[] filtrosIES) {
    IES[] lasIES=getIES(filtrosIES);
    long conteosSimples[][]=new long[9][2],conteoICETEX[][]=new long[3][2],conteoApoyos[][]=new long[3][2];
    long conteoAreas[]=new long[10],totConteoAreas=0;
    long conteoCaracteresIES[]=new long[10],totConteoCaracteresIES=0;
    for (IES ies:lasIES) {
      byte caracter=ies.caracter;
      if (caracter!=-1) {
        conteoCaracteresIES[caracter]++;
        totConteoCaracteresIES++;
      }
      int n=ies.n;
      for (Estudiante e:ies.estudiantes) {
        int jI=e.getSemestrePrimiparo();
        if (jI==-1) continue;
        double repitencias[]=e.getRepitencias();
        byte aux[]={e.getSexo(),e.getTrabajabaCuandoPresentoIcfes(),e.getEdadAlPresentarElICFES(),e.getIngresoHogar(),e.getViviendaPropia(),e.getNumeroHermanos(),e.getNivelEducativoMadre(),e.getPuntajeICFES()};
        for (int k=0; k<8; k++) if (aux[k]!=-1) {
          int inc=aux[k];
          if (k==0)              inc=(aux[k]==0)?1:0;
          else if (k==1 || k==4) inc=(aux[k]==1)?1:0;
          else if (k==5)         inc=Math.max(0,aux[k]-1);
          conteosSimples[k][0]+=inc;
          conteosSimples[k][1]++;
        }
        for (int j=jI; j<n; j++) if (repitencias[j]!=-1.0) {
          conteosSimples[8][0]+=(long)Math.round(repitencias[j]*10000);
          conteosSimples[8][1]++;
        }
        int icetexRecibidos[]=new int[3];
        for (int j=jI; j<n; j++) {
          int apo=e.getTipoApoyoICETEXRecibido(j);
          if (apo>=1 && apo<=3) icetexRecibidos[apo-1]++;
        }
        for (int i=0; i<3; i++) {
          if (icetexRecibidos[i]>0) conteoICETEX[i][0]++;
          conteoICETEX[i][1]++;
        }
        int apoyosRecibidos[]=new int[3];
        for (int j=jI; j<n; j++) {
          if (e.getRecibioApoyoFinanciero(j)) apoyosRecibidos[0]++;
          if (e.getRecibioApoyoAcademico(j)) apoyosRecibidos[1]++;
          if (e.getRecibioApoyoOtro(j)) apoyosRecibidos[2]++;
        }
        for (int i=0; i<3; i++) {
          if (apoyosRecibidos[i]>0) conteoApoyos[i][0]++;
          conteoApoyos[i][1]++;
        }
        int indicePrograma=e.getIndicePrograma();
        if (indicePrograma!=-1) {
          byte area=ies.programas[indicePrograma].area;
          if (area!=-1) {
            conteoAreas[area]++;
            totConteoAreas++;
          }
        }
      }
    }
    return new Object[]{conteosSimples,conteoICETEX,conteoApoyos,conteoAreas,totConteoAreas,conteoCaracteresIES,totConteoCaracteresIES};
  }
  /**
   * 
   * @param filtros
   * @param filtroEspecial
   * @param limiteResultados
   * @return
   * @throws MyException
   */
  public Object[] getEstudiantes(Filtro[] filtros, String[][] filtroEspecial, int limiteResultados) throws MyException {
    return getEstudiantes(filtros,filtroEspecial,0,limiteResultados);
  }
  public Object[] getEstudiantes(Filtro[] filtros, String[][] filtroEspecial, int resultadoInicial, int limiteResultados) throws MyException {
    Filtro[] filtrosIES=Variable.filtrarFiltros(filtros,TipoVariable.TV_IES);
    Filtro[] filtrosEst=Variable.filtrarFiltros(filtros,TipoVariable.TV_ESTUDIANTE);
    Filtro[] filtrosPer=Variable.filtrarFiltros(filtros,TipoVariable.TV_PERIODO_ESTUDIANTE);
    boolean calcularRepitencias=false;
    for (Filtro ft:filtrosPer) if (ft.variable==Variable.REPITENCIA_PER) {calcularRepitencias=true; break;}
    Collection<EstudianteDAO> listaRes=new ArrayList<EstudianteDAO>(1000);
    int estudiantesEncontrados=0;
    for (IES ies:getIES(filtrosIES)) {
      Estudiante_DatosPersonales[] datosPersonales=IES.getDatosPersonales(new File(Constantes.carpetaDatos,ies.codigo+".spa"));
      if (filtroEspecial!=null && datosPersonales==null) throw new MyException("No se puede aplicar el filtro por datos personales para la IES "+ies.codigo+" porque se eliminaron los datos personales de los estudiantes de tal IES.");
      int ne=ies.estudiantes.length;
      for (int i=0; i<ne; i++) {
        Estudiante e=ies.estudiantes[i];
        int jI=e.getSemestrePrimiparo();
        if (jI==-1 || !pasaFiltros(filtrosEst,e,ies)) continue;
        Estudiante_DatosPersonales edp=(datosPersonales!=null && i<datosPersonales.length)?datosPersonales[i]:null;
        if (filtroEspecial!=null && (edp==null || !edp.pasaFiltroEspecial(filtroEspecial))) continue;
        long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
        double repitencias[]=calcularRepitencias?e.getRepitencias():null;
        int tF=filtrosPer.length;
        /*//PAra cada filtro que exista al menos un periodo que lo cumpla
        boolean filtrosPasados[]=new boolean[tF];
        for (int j=jI,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
          for (int k=0; k<tF; k++) if (!filtrosPasados[k] && filtrosPer[k].pasaFiltro(e,ies,j,repitencias)) filtrosPasados[k]=true;
        }*/
        //Que exista un periodo que cumpla todos los fltros
        boolean existePer = false;
        for (int j=jI,jT=ies.n; !existePer && j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
          int k;for (k=0; k<tF; k++) if (!filtrosPer[k].pasaFiltro(e,ies,j,repitencias)) break;
          if (k==tF) existePer = true;
        }
        /*
        boolean pasa=true;
        for (int k=0; pasa && k<tF; k++) if (!filtrosPasados[k]) pasa=false;
        */
        boolean pasa = existePer;
        if (!pasa) continue;
        //if (++estudiantesEncontrados<=limiteResultados) listaRes.add(new EstudianteDAO(ies,e,(edp==null)?new Estudiante_DatosPersonales():edp,i));
        ++estudiantesEncontrados;
        if (estudiantesEncontrados>resultadoInicial && listaRes.size()<limiteResultados) listaRes.add(new EstudianteDAO(ies,e,(edp==null)?new Estudiante_DatosPersonales():edp,i));
      }
    }
    return new Object[]{listaRes.toArray(new EstudianteDAO[0]),estudiantesEncontrados};
  }
  public EstudianteDAO getEstudiante(int codies, int pose) throws MyException {
    IES ies = getIES(codies);
    Estudiante_DatosPersonales[] datosPersonales=ies.datosPersonalesEstudiantes!=null?ies.datosPersonalesEstudiantes:IES.getDatosPersonales(new File(Constantes.carpetaDatos,ies.codigo+".spa"));
    if (ies!=null&&pose>=0&pose<ies.estudiantes.length)
      return new EstudianteDAO(ies,ies.estudiantes[pose],(datosPersonales==null)?new Estudiante_DatosPersonales():datosPersonales[pose]);
    else
      return null;
  }
  public void writeEstudiantes(File fOut,Filtro[] filtros, String[][] filtroEspecial) throws MyException {
    try {
      PrintStream ps = new PrintStream(fOut);
      Filtro[] filtrosIES=Variable.filtrarFiltros(filtros,TipoVariable.TV_IES);
      Filtro[] filtrosEst=Variable.filtrarFiltros(filtros,TipoVariable.TV_ESTUDIANTE);
      Filtro[] filtrosPer=Variable.filtrarFiltros(filtros,TipoVariable.TV_PERIODO_ESTUDIANTE);
      boolean calcularRepitencias=false;
      for (Filtro ft:filtrosPer) if (ft.variable==Variable.REPITENCIA_PER) {calcularRepitencias=true; break;}
      Collection<EstudianteDAO> listaRes=new ArrayList<EstudianteDAO>(1000);
      int estudiantesEncontrados=0;
      ps.println(CajaDeHerramientas.stringToCSV(new String[]{"apellido","nombre","doc_tipo","doc_num","ies","programa","codigo"}));
      for (IES ies:getIES(filtrosIES)) {
        Estudiante_DatosPersonales[] datosPersonales=IES.getDatosPersonales(new File(Constantes.carpetaDatos,ies.codigo+".spa"));
        if (filtroEspecial!=null && datosPersonales==null) throw new MyException("No se puede aplicar el filtro por datos personales para la IES "+ies.codigo+" porque se eliminaron los datos personales de los estudiantes de tal IES.");
        int ne=ies.estudiantes.length;
        for (int i=0; i<ne; i++) {
          Estudiante e=ies.estudiantes[i];
          int jI=e.getSemestrePrimiparo();
          if (jI==-1 || !pasaFiltros(filtrosEst,e,ies)) continue;
          Estudiante_DatosPersonales edp=(datosPersonales!=null && i<datosPersonales.length)?datosPersonales[i]:null;
          if (filtroEspecial!=null && (edp==null || !edp.pasaFiltroEspecial(filtroEspecial))) continue;
          long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
          double repitencias[]=calcularRepitencias?e.getRepitencias():null;
          int tF=filtrosPer.length;
          /*//PAra cada filtro que exista al menos un periodo que lo cumpla
          boolean filtrosPasados[]=new boolean[tF];
          for (int j=jI,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
            for (int k=0; k<tF; k++) if (!filtrosPasados[k] && filtrosPer[k].pasaFiltro(e,ies,j,repitencias)) filtrosPasados[k]=true;
          }*/
          //Que exista un periodo que cumpla todos los fltros
          boolean existePer = false;
          for (int j=jI,jT=ies.n; !existePer && j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
            int k;for (k=0; k<tF; k++) if (!filtrosPer[k].pasaFiltro(e,ies,j,repitencias)) break;
            if (k==tF) existePer = true;
          }
          /*
          boolean pasa=true;
          for (int k=0; pasa && k<tF; k++) if (!filtrosPasados[k]) pasa=false;
          */
          boolean pasa = existePer;
          if (!pasa) continue;
          int indProg = e.getIndicePrograma();
          String[] w = new String[]{
            new String(edp.apellido),
            new String(edp.nombre),
            CajaDeHerramientas.tipoDocumentoToString(edp.tipoDocumento),
            edp.documento==-1?"":(""+edp.documento),
            ""+ies.codigo,
            indProg==-1?"":new String(ies.programas[indProg].nombre),
            new String(edp.codigo)};
          ps.println(CajaDeHerramientas.stringToCSV(w));
        }
      }
      ps.close();
    } catch (Exception e) {
      throw new MyException("Error escribiendo el archivo "+fOut);
    }
  }
  
  @SuppressWarnings("unchecked")
  public Object[] getDesercionIntersemestral(Filtro[] filtros, Variable[] diferenciados) throws MyException {
    Integer[] codigosIESDif=null;       // Caso especial de diferenciación #1
    String[] codigosProgramasDif=null;  // Caso especial de diferenciación #2
    boolean calcularRepitencias=false;
    Filtro[] filtrosIES=Variable.filtrarFiltros(filtros,TipoVariable.TV_IES);
    Filtro[] filtrosEst=Variable.filtrarFiltros(filtros,TipoVariable.TV_ESTUDIANTE);
    Filtro[] filtrosPer=Variable.filtrarFiltros(filtros,TipoVariable.TV_PERIODO_ESTUDIANTE);
    {
      Map<Variable,Filtro> mapF=new EnumMap<Variable,Filtro>(Variable.class);
      for (Filtro ft:filtros) mapF.put(ft.variable,ft);
      for (Variable v:diferenciados) if (v==Variable.CODIGO_IES || v==Variable.PROGRAMA_EST) {
        Filtro ft=mapF.get(v);
        Comparable[] keys=(ft==null)?Item.getKeys(v.items):ft.filtro;
        int t=keys.length;
        //if (t>100) throw new MyException("No se puede diferenciar la variable \""+v.nombre+"\" por más de 100 valores.");
        switch (v) {
          case CODIGO_IES:
            IES[] pi = this.getIES(filtrosIES);
            t = pi.length;
            if (t>200) throw new MyException("No se puede diferenciar la variable \""+v.nombre+"\" por más de 100 valores.");
            codigosIESDif=new Integer[t];
            for (int i=0; i<t; i++) codigosIESDif[i]=(Integer)pi[i].codigo;
            break;
          case PROGRAMA_EST:
            String[] pa = this.getProgramasActivos(filtrosIES);
            t = pa.length;
            if (t>200) throw new MyException("No se puede diferenciar la variable \""+v.nombre+"\" por más de 100 valores.");
            codigosProgramasDif=new String[t];
            for (int i=0; i<t; i++) codigosProgramasDif[i]=(String)pa[i];
            break;
        }
      }
      calcularRepitencias=mapF.containsKey(Variable.REPITENCIA_PER)||Arrays.asList(diferenciados).contains(Variable.REPITENCIA_PER);
    }
    int m=diferenciados.length,indicesDifIES[],indicesDifEst[],indicesDifPer[];
    {
      List[] listas=new List[3];
      for (int i=0; i<3; i++) listas[i]=new ArrayList<Integer>(10);
      for (int h=0; h<m; h++) {
        switch (diferenciados[h].tipo) {
          case TV_IES:                listas[0].add((Integer)h); break;
          case TV_ESTUDIANTE:         listas[1].add((Integer)h); break;
          case TV_PERIODO_ESTUDIANTE: listas[2].add((Integer)h); break;
        }
      }
      indicesDifIES=CajaDeHerramientas.toIntArray(listas[0]);
      indicesDifEst=CajaDeHerramientas.toIntArray(listas[1]);
      indicesDifPer=CajaDeHerramientas.toIntArray(listas[2]);
    }
    Map<byte[],int[]> resC=new TreeMap<byte[],int[]>(compByteArrayEspecial);
    Map<byte[],double[]> resP=new TreeMap<byte[],double[]>(compByteArrayEspecial);
    Map<byte[],int[]> serieRef=new TreeMap<byte[],int[]>(compByteArrayEspecial);
    IES[] lasIES=getIES(filtrosIES);
    int limsSems[]=getLimsCodigoSemestre(lasIES),limInf=limsSems[0],limSup=limsSems[1];
    if (limInf==Integer.MAX_VALUE) return null;
    int tam=limSup-limInf+1;
    for (IES ies:lasIES) {
      byte llaveIES[]=new byte[m];
      for (int h:indicesDifIES) {
        Variable v=diferenciados[h];
        Rango rango=v.rango;
        Comparable ran=rango.getRango(v.getValor(ies));
        llaveIES[h]=(v==Variable.CODIGO_IES)?((byte)Arrays.binarySearch(codigosIESDif,ran)):(rango.rangoToByte(ran));
      }
      for (Estudiante e:ies.estudiantes) {
        int jI=e.getSemestrePrimiparo();
        if (jI==-1 || !pasaFiltros(filtrosEst,e,ies)) continue;
        byte llaveEst[]=llaveIES.clone();
        for (int h:indicesDifEst) {
          Variable v=diferenciados[h];
          Rango rango=v.rango;
          Comparable ran=rango.getRango(v.getValor(e,ies));
          llaveEst[h]=(v==Variable.PROGRAMA_EST)?((byte)Arrays.binarySearch(codigosProgramasDif,ran)):(rango.rangoToByte(ran));
        }
        boolean entra=false;
        long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
        double repitencias[]=calcularRepitencias?e.getRepitencias():null;
        for (int j=jI,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L && pasaFiltros(filtrosPer,e,ies,j,repitencias)) {
          byte llavePer[]=llaveEst.clone();
          for (int h:indicesDifPer) {
            Variable v=diferenciados[h];
            Rango rango=v.rango;
            Comparable ran=rango.getRango(v.getValor(e,ies,j,repitencias));
            llavePer[h]=rango.rangoToByte(ran);
          }
          int serieDatos[]=resC.get(llavePer);
          if (serieDatos==null) {
            resC.put(llavePer,serieDatos=new int[tam]);
            if (limtser && resC.size()>120) throw new MyException("Los criterios de diferenciación suministrados arrojan más de 120 series de datos. Refine la consulta.");
          }
          double serieDatosP[]=resP.get(llavePer);
          if (serieDatosP==null) {
            resP.put(llavePer,serieDatosP=new double[tam]);
            //if (resP.size()>120) throw new MyException("Los criterios de diferenciación suministrados arrojan más de 120 series de datos. Refine la consulta.");
          }
          //serieDatos[j-jI]++;
          long matri2 = matri;
          if (ies.periodosDesIntValida[j] && j+1<jT && (e.getSemestreGrado()==-1 || j<e.getUltimoSemestreMatriculado()))
            serieDatosP[j+ies.minCodigoSemestre-limInf/*e.getSemestrePrimiparo()+*//*j-jI*/]++;
          //Tiene proximo semestre y esta matriculado en el. O esta graduado y este no es su ultimo periodo
          //if ((j+1<jT&&((matri2>>>=1)&1L)==1L)||(e.getSemestreGrado()!=-1 && j<e.getUltimoSemestreMatriculado())) serieDatos[j-jI]++;
          matri2 = matri;
          //Hay un proximo semestre Y no esta matriculado en el Y (NO SE GRADUO O ESTE NO ES EL ULTIMO SEMESTRE VISTO)
          if (ies.periodosDesIntValida[j] && j+1<jT&&((matri2>>>=1)&1L)!=1L&&(e.getSemestreGrado()==-1 || j<e.getUltimoSemestreMatriculado())) serieDatos[j+ies.minCodigoSemestre-limInf/*e.getSemestrePrimiparo()+*//*j-jI*/]++;
          entra=true;
        }
        if (entra) {
          int datosRef[]=serieRef.get(llaveEst);
          if (datosRef==null) serieRef.put(llaveEst,datosRef=new int[tam]);
          for (int i=0,iT=ies.n-jI; i<iT; i++) datosRef[i]++;
        }
      }
    }
    if (resC.isEmpty()) return null;
    {
      Iterator<Entry<byte[],int[]>> itC = resC.entrySet().iterator();
      Iterator<Entry<byte[],double[]>> itP = resP.entrySet().iterator();
      for (int i=0,iT=resC.size();i<iT;i++) {
        Entry<byte[], int[]> enC = itC.next();
        Entry<byte[], double[]> enP = itP.next();
        int[] seC = enC.getValue();
        double[] seP = enP.getValue();
        for (int j=0;j<seC.length;j++) {
          seP[j] = seC[j]/seP[j]; 
        }
      }
    }
    
    /*
    Map<byte[],double[]> resP=new TreeMap<byte[],double[]>(compByteArrayEspecial);
    for (Map.Entry<byte[],int[]> e:resC.entrySet()) {
      byte[] llavePer=e.getKey(),llaveEst=llavePer.clone();
      for (int h:indicesDifPer) llaveEst[h]=0;
      int[] cont=e.getValue(),contRef=serieRef.get(llaveEst);
      double[] porc=new double[tam];
      boolean b=false;
      for (int i=0; i<tam; i++) if (cont[i]>0 && contRef[i]>10) {porc[i]=1d*cont[i]/contRef[i]; b=true;}
      if (b) resP.put(llavePer,porc);
    }
    */
    //CANTIDAD, PORCENTAJE, ??_IES, ??_PROGRAMAS, cuantos periodos
    return new Object[]{resC,resP,codigosIESDif,codigosProgramasDif,tam, (byte)limInf, (byte)limSup};
  }
  @SuppressWarnings("unchecked")
  public Object[] getCostoDesercion(Filtro[] filtros, Variable[] diferenciados) throws MyException {
    Integer[] codigosIESDif=null;       // Caso especial de diferenciación #1
    String[] codigosProgramasDif=null;  // Caso especial de diferenciación #2
    boolean calcularRepitencias=false;
    {
      Map<Variable,Filtro> mapF=new EnumMap<Variable,Filtro>(Variable.class);
      for (Filtro ft:filtros) mapF.put(ft.variable,ft);
      for (Variable v:diferenciados) if (v==Variable.CODIGO_IES || v==Variable.PROGRAMA_EST) {
        Filtro ft=mapF.get(v);
        Comparable[] keys=(ft==null)?Item.getKeys(v.items):ft.filtro;
        int t=keys.length;
        if (v!=Variable.CODIGO_IES && t>100) throw new MyException("No se puede diferenciar la variable \""+v.nombre+"\" por más de 100 valores.");
        switch (v) {
          case CODIGO_IES:
            codigosIESDif=new Integer[t];
            for (int i=0; i<t; i++) codigosIESDif[i]=(Integer)keys[i];
            break;
          case PROGRAMA_EST:
            codigosProgramasDif=new String[t];
            for (int i=0; i<t; i++) codigosProgramasDif[i]=(String)keys[i];
            break;
        }
      }
      calcularRepitencias=mapF.containsKey(Variable.REPITENCIA_PER)||Arrays.asList(diferenciados).contains(Variable.REPITENCIA_PER);
    }
    Filtro[] filtrosIES=Variable.filtrarFiltros(filtros,TipoVariable.TV_IES);
    Filtro[] filtrosEst=Variable.filtrarFiltros(filtros,TipoVariable.TV_ESTUDIANTE);
    Filtro[] filtrosPer=Variable.filtrarFiltros(filtros,TipoVariable.TV_PERIODO_ESTUDIANTE);
    int m=diferenciados.length,indicesDifIES[],indicesDifEst[],indicesDifPer[];
    {
      List[] listas=new List[3];
      for (int i=0; i<3; i++) listas[i]=new ArrayList<Integer>(10);
      for (int h=0; h<m; h++) {
        switch (diferenciados[h].tipo) {
          case TV_IES:                listas[0].add((Integer)h); break;
          case TV_ESTUDIANTE:         listas[1].add((Integer)h); break;
          case TV_PERIODO_ESTUDIANTE: listas[2].add((Integer)h); break;
        }
      }
      indicesDifIES=CajaDeHerramientas.toIntArray(listas[0]);
      indicesDifEst=CajaDeHerramientas.toIntArray(listas[1]);
      indicesDifPer=CajaDeHerramientas.toIntArray(listas[2]);
    }
    Map<byte[],int[]> resC=new TreeMap<byte[],int[]>(compByteArrayEspecial);
    Map<byte[],double[]> resP=new TreeMap<byte[],double[]>(compByteArrayEspecial);
    Map<byte[],int[]> serieRef=new TreeMap<byte[],int[]>(compByteArrayEspecial);
    IES[] lasIES=getIES(filtrosIES);
    int limsSems[]=getLimsCodigoSemestre(lasIES),limInf=limsSems[0],limSup=limsSems[1];
    if (limInf==Integer.MAX_VALUE) return null;
    int tam=limSup-limInf+1;
    for (IES ies:lasIES) {
      byte llaveIES[]=new byte[m];
      for (int h:indicesDifIES) {
        Variable v=diferenciados[h];
        Rango rango=v.rango;
        Comparable ran=rango.getRango(v.getValor(ies));
        llaveIES[h]=(v==Variable.CODIGO_IES)?((byte)Arrays.binarySearch(codigosIESDif,ran)):(rango.rangoToByte(ran));
      }
      for (Estudiante e:ies.estudiantes) {
        int jI=e.getSemestrePrimiparo();
        if (jI==-1 || !pasaFiltros(filtrosEst,e,ies)) continue;
        byte llaveEst[]=llaveIES.clone();
        for (int h:indicesDifEst) {
          Variable v=diferenciados[h];
          Rango rango=v.rango;
          Comparable ran=rango.getRango(v.getValor(e,ies));
          llaveEst[h]=(v==Variable.PROGRAMA_EST)?((byte)Arrays.binarySearch(codigosProgramasDif,ran)):(rango.rangoToByte(ran));
        }
        long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
        double repitencias[]=calcularRepitencias?e.getRepitencias():null;
        for (int j=jI,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L && pasaFiltros(filtrosPer,e,ies,j,repitencias)) {
          byte llavePer[]=llaveEst.clone();
          for (int h:indicesDifPer) {
            Variable v=diferenciados[h];
            Rango rango=v.rango;
            Comparable ran=rango.getRango(v.getValor(e,ies,j,repitencias));
            llavePer[h]=rango.rangoToByte(ran);
          }
          int serieDatos[]=resC.get(llavePer);
          if (serieDatos==null) {
            resC.put(llavePer,serieDatos=new int[tam]);
            if (limtser && resC.size()>120) throw new MyException("Los criterios de diferenciación suministrados arrojan más de 120 series de datos. Refine la consulta.");
          }
          double serieDatosP[]=resP.get(llavePer);
          if (serieDatosP==null) {
            resP.put(llavePer,serieDatosP=new double[tam]);
            if (limtser && resP.size()>120) throw new MyException("Los criterios de diferenciación suministrados arrojan más de 120 series de datos. Refine la consulta.");
          }
          //serieDatos[j-jI]++;
          long matri2 = matri;
          //if (ies.periodosDesIntValida[j] && j+1<jT && (e.getSemestreGrado()==-1 || j<e.getUltimoSemestreMatriculado())) serieDatosP[j+ies.minCodigoSemestre-limInf]++;
          //Tiene proximo semestre y esta matriculado en el. O esta graduado y este no es su ultimo periodo
          matri2 = matri;
          //Hay un proximo semestre Y no esta matriculado en el Y (NO SE GRADUO O ESTE NO ES EL ULTIMO SEMESTRE VISTO)
          //if (ies.periodosDesIntValida[j] && j+1<jT&&((matri2>>>=1)&1L)!=1L&&(e.getSemestreGrado()==-1 || j<e.getUltimoSemestreMatriculado())) serieDatos[j+ies.minCodigoSemestre-limInf]++;
          if (ies.periodosDesIntValida[j] && j+1<jT&&((matri2>>>=1)&1L)!=1L&&(e.getSemestreGrado()==-1 || j<e.getUltimoSemestreMatriculado())) {
            int ip = e.getIndicePrograma();
            int costo = -1;
            if (ip!=-1) costo = ies.costosProgramas[ip][j+1];
            serieDatos[j+ies.minCodigoSemestre-limInf]++;
            if (costo!=-1) serieDatosP[1+j+ies.minCodigoSemestre-limInf]+=costo;
          }
        }
      }
    }
    if (resC.isEmpty()) return null;
    //CANTIDAD, PORCENTAJE, ??_IES, ??_PROGRAMAS, cuantos periodos
    return new Object[]{resC,resP,codigosIESDif,codigosProgramasDif,tam, (byte)limInf, (byte)limSup};
  }
 
  @SuppressWarnings("unchecked")
  public Object[] getCruceVariables(Filtro[] filtros, Variable[] diferenciados) throws MyException {
    assert(diferenciados.length==2);
    Integer[] codigosIESDif=null;       // Caso especial de diferenciación #1
    String[] codigosProgramasDif=null;  // Caso especial de diferenciación #2
    boolean calcularRepitencias=false;
    Filtro[] filtrosIES=Variable.filtrarFiltros(filtros,TipoVariable.TV_IES);
    Filtro[] filtrosEst=Variable.filtrarFiltros(filtros,TipoVariable.TV_ESTUDIANTE);
    Filtro[] filtrosPer=Variable.filtrarFiltros(filtros,TipoVariable.TV_PERIODO_ESTUDIANTE);
    {
      Map<Variable,Filtro> mapF=new EnumMap<Variable,Filtro>(Variable.class);
      for (Filtro ft:filtros) mapF.put(ft.variable,ft);
      for (Variable v:diferenciados) if (v==Variable.CODIGO_IES || v==Variable.PROGRAMA_EST) {
        Filtro ft=mapF.get(v);
        Comparable[] keys=(ft==null)?Item.getKeys(v.items):ft.filtro;
        int t=keys.length;
        //if (t>100) throw new MyException("No se puede diferenciar la variable \""+v.nombre+"\" por más de 100 valores.");
        //TODO Validacion no mas de 256 valores por variable  
        switch (v) {
          case CODIGO_IES:
            IES[] pi = this.getIES(filtrosIES);
            t = pi.length;
            if (t>200) throw new MyException("No se puede diferenciar la variable \""+v.nombre+"\" por más de 200 valores.");
            codigosIESDif=new Integer[t];
            for (int i=0; i<t; i++) codigosIESDif[i]=(Integer)pi[i].codigo;
            break;
          case PROGRAMA_EST:
            String[] pa = this.getProgramasActivos(filtrosIES);
            t = pa.length;
            if (t>200) throw new MyException("No se puede diferenciar la variable \""+v.nombre+"\" por más de 200 valores.");
            codigosProgramasDif=new String[t];
            for (int i=0; i<t; i++) codigosProgramasDif[i]=(String)pa[i];
            break;
        }
      }
      calcularRepitencias=mapF.containsKey(Variable.REPITENCIA_PER)||Arrays.asList(diferenciados).contains(Variable.REPITENCIA_PER);
    }
    int m=diferenciados.length,indicesDifIES[],indicesDifEst[],indicesDifPer[];
    {
      List[] listas=new List[3];
      for (int i=0; i<3; i++) listas[i]=new ArrayList<Integer>(10);
      for (int h=0; h<m; h++) {
        switch (diferenciados[h].tipo) {
          case TV_IES:                listas[0].add((Integer)h); break;
          case TV_ESTUDIANTE:         listas[1].add((Integer)h); break;
          case TV_PERIODO_ESTUDIANTE: listas[2].add((Integer)h); break;
        }
      }
      indicesDifIES=CajaDeHerramientas.toIntArray(listas[0]);
      indicesDifEst=CajaDeHerramientas.toIntArray(listas[1]);
      indicesDifPer=CajaDeHerramientas.toIntArray(listas[2]);
    }
    Map<byte[],int[]> resC=new TreeMap<byte[],int[]>(compByteArrayEspecial);
    Map<byte[],int[]> serieRef=new TreeMap<byte[],int[]>(compByteArrayEspecial);
    IES[] lasIES=getIES(filtrosIES);
    int limsSems[]=getLimsCodigoSemestre(lasIES),limInf=limsSems[0],limSup=limsSems[1];
    if (limInf==Integer.MAX_VALUE) return null;
    int tam=limSup-limInf+1;
    for (IES ies:lasIES) {
      byte llaveIES[]=new byte[m];
      for (int h:indicesDifIES) {
        Variable v=diferenciados[h];
        Rango rango=v.rango;
        Comparable ran=rango.getRango(v.getValor(ies));
        llaveIES[h]=(v==Variable.CODIGO_IES)?((byte)Arrays.binarySearch(codigosIESDif,ran)):(rango.rangoToByte(ran));
      }
      for (Estudiante e:ies.estudiantes) {
        int jI=e.getSemestrePrimiparo();
        if (jI==-1 || !pasaFiltros(filtrosEst,e,ies)) continue;
        byte llaveEst[]=llaveIES.clone();
        for (int h:indicesDifEst) {
          Variable v=diferenciados[h];
          Rango rango=v.rango;
          Comparable ran=rango.getRango(v.getValor(e,ies));
          llaveEst[h]=(v==Variable.PROGRAMA_EST)?((byte)Arrays.binarySearch(codigosProgramasDif,ran)):(rango.rangoToByte(ran));
        }
        boolean entra=false;
        long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
        double repitencias[]=calcularRepitencias?e.getRepitencias():null;
        for (int j=jI,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L && pasaFiltros(filtrosPer,e,ies,j,repitencias)) {
          byte llavePer[]=llaveEst.clone();
          for (int h:indicesDifPer) {
            Variable v=diferenciados[h];
            Rango rango=v.rango;
            Comparable ran=rango.getRango(v.getValor(e,ies,j,repitencias));
            llavePer[h]=rango.rangoToByte(ran);
          }
          int serieDatos[]=resC.get(llavePer);
          if (serieDatos==null) {
            resC.put(llavePer,serieDatos=new int[tam]);
            //if (resC.size()>120) throw new MyException("Los criterios de diferenciación suministrados arrojan más de 120 series de datos. Refine la consulta.");
          }
          //serieDatos[j-jI]++;
          serieDatos[0]++;
          entra=true;
        }
        if (entra) {
          int datosRef[]=serieRef.get(llaveEst);
          if (datosRef==null) serieRef.put(llaveEst,datosRef=new int[tam]);
          for (int i=0,iT=ies.n-jI; i<iT; i++) datosRef[i]++;
        }
      }
    }
    if (resC.isEmpty()) return null;
    Map<byte[],double[]> resP=new TreeMap<byte[],double[]>(compByteArrayEspecial);
    for (Map.Entry<byte[],int[]> e:resC.entrySet()) {
      byte[] llavePer=e.getKey(),llaveEst=llavePer.clone();
      for (int h:indicesDifPer) llaveEst[h]=0;
      int[] cont=e.getValue(),contRef=serieRef.get(llaveEst);
      double[] porc=new double[tam];
      boolean b=false;
      for (int i=0; i<tam; i++) if (cont[i]>0 && contRef[i]>10) {porc[i]=1d*cont[i]/contRef[i]; b=true;}
      if (b) resP.put(llavePer,porc);
    }
    //CANTIDAD, ??_IES, ??_PROGRAMAS, cuantos periodos
    return new Object[]{resC,/*resP,*/codigosIESDif,codigosProgramasDif/*,tam*/};
  }
  
  public Object[][] getVariablesRelevantes(Filtro[] filtrosIES) throws MyException {
    Variable[] vars = new Variable[]{
        Variable.SEXO_EST,
        Variable.TRABAJABA_CUANDO_ICFES_EST,
        Variable.CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST,
        Variable.INGRESO_HOGAR_EST,
        Variable.VIVIENDA_PROPIA_EST,
        Variable.NUMERO_HERMANOS_EST,
        Variable.POSICION_ENTRE_LOS_HERMANOS_EST,
        Variable.CLASIFICACION_PUNTAJE_ICFES_EST,
        Variable.NIVEL_EDUCATIVO_MADRE_EST,
        Variable.NUMERO_APOYOS_ACADEMICOS_RECIBIDOS_EST,
        Variable.NUMERO_APOYOS_FINANCIEROS_RECIBIDOS_EST,
        Variable.NUMERO_APOYOS_OTROS_RECIBIDOS_EST,
        Variable.NUMERO_APOYOS_ICETEX_RECIBIDOS_EST,
    };
    List<Object[]> resp = new LinkedList<Object[]>();
    for (Variable var:vars) {
      Object[] res = null;//this.getConteoPoblacion(filtrosIES, new Variable[]{var});
      if (res==null) {
        continue;
      }
      Map<byte[], int[]> resC = (Map<byte[], int[]>) res[0];
      Map<byte[], double[]> resP = (Map<byte[], double[]>) res[1];
      Integer tam = (Integer) res[4];
      int numSeries = resP.size();
      double [] varInic = new double[numSeries];
      double[][] tabP = new double[numSeries][tam];//Torcido mejoraria eficiencia
      int[][] tabC = new int[numSeries][tam];
      byte[][] tabInd = new byte[numSeries][]; 
      {
        int serie = 0;
        for (Entry<byte[], double[]> ent: resP.entrySet()) {
          varInic[serie] = resC.get(ent.getKey())[0];
          tabInd[serie] = ent.getKey();
          int ti = 0;
          int[] serieC = resC.get(ent.getKey());
          for (double x: ent.getValue()) {
            tabC[serie][ti] = serieC[ti];
            tabP[serie][ti] = x;
            ti++;
          }
          serie++;
        }
      }

      int serieMayor = -1;
      {
        int [] numMayorias = new int[numSeries]; Arrays.fill(numMayorias, 0);
        for (int i=0;i<tam;i++) {
          int iMax = -1;
          double max = Double.MIN_VALUE;
          for (int j = 0;j<numSeries;j++) {
            if (tabP[j][i]>max) {
              max = tabP[j][i];
              iMax = j;
            }
          }
          if (iMax!=-1 && max>0) numMayorias[iMax]++;
        }
        {
          int iSerieMayor = -1;
          int max = Integer.MIN_VALUE;
          for (int i=0;i<numSeries;i++) if (numMayorias[i]>max) {
            max = numMayorias[i];
            iSerieMayor = i;
          }
          serieMayor = iSerieMayor;
        }
      }
      double valTo = 0;
      for (int i=0;i<numSeries;i++) {
        String nomValVar = var.rango.toString(var.rango.byteToRango(tabInd[i][0]));
        if (nomValVar.equals(Constantes.S_DESCONOCIDO)) continue;
        for (int j=0;j<tam;j++)
          valTo+=tabC[i][j] * (tabP[serieMayor][j] - tabP[i][j]);
      }
      resp.add(new Object[]{var, tabInd[serieMayor], valTo});
      //ps.println(var.nombre + ";" + var.rango.toString(var.rango.byteToRango(tabInd[serieMayor][0])) + ";" + ocurrenciasSerieMayor + ";" +tam + ";" + String.valueOf(valTo).replace('.', ','));
    }
    Collections.sort(resp, new Comparator<Object[]>(){
      public int compare(Object[] o1, Object[] o2) {
        return -((Comparable)o1[2]).compareTo(o2[2]);
      }
    });
    return resp.toArray(new Object[][]{});
  }

  public Object[] getTasaDesercion(SFiltro[] filtros) throws MyException {
    Object [] respre = getTasaDesercion(filtros, new SVariable[0]);
    return new Object[]{((Map)respre[0]).get(new byte[0]),((Map)respre[1]).get(new byte[0]),((Map)respre[2]).get(new byte[0]),respre[5],respre[6]};
  }
  public Object[] getTasaDesercion(SFiltro[] filtros, SVariable[] diferenciados) throws MyException {
    Integer[] codigosIESDif=null;       // Caso especial de diferenciación #1
    String[] codigosProgramasDif=null;  // Caso especial de diferenciación #2

    int limInf0 = Integer.MAX_VALUE, limSup0 = Integer.MIN_VALUE;
    Map<byte[],int[]> resM=new TreeMap<byte[],int[]>(compByteArrayEspecial),
      resD=new TreeMap<byte[],int[]>(compByteArrayEspecial);
    Map<byte[],double[]> resT=new TreeMap<byte[],double[]>(compByteArrayEspecial);

    int limsSems[]=getLimsCodigoSemestre(listaIES),
      limInf=limInf0==Integer.MAX_VALUE?limsSems[0]:Math.max(limsSems[0],limInf0),
      limSup=limSup0==Integer.MIN_VALUE?limsSems[1]:Math.min(limsSems[1],limSup0);
    int tamRes = limSup - limInf+1;
    //int [] matriculadosConsiderados = new int[tamRes], desertores = new int[tamRes];
    if (limInf==Integer.MAX_VALUE) return null;
    
    int m=diferenciados.length;
    
    cicloIndividuo:for(Individuo individuo:DisjoinDataSet.getIndividuosIdsRelativos(this))if(individuo!=null){
	    	byte llave[]=new byte[m];
	    	for(int h=0;h<m;h++){
				SVariable v=diferenciados[h];
				Rango rango=v.rango;
				Comparable ran=rango.getRango(v.getValor(individuo));
				llave[h]=rango.rangoToByte(ran);  
			}
	    	Aparicion iesPrimiparo=individuo.getPrimiparoPrimeraVez();
	    	
	    	if(iesPrimiparo==null)continue;

	    	int jI=iesPrimiparo.estudiante.getSemestrePrimiparo();
			if (!pasaFiltros(filtros,individuo)) continue;
	        
	        long matri=individuo.getSemestresMatriculadoAlDerecho(this);
	        int [] desertores = resD.get(llave);
	        int [] matriculadosConsiderados = resM.get(llave);
	        if (desertores==null) resD.put(llave, desertores = new int[tamRes]);
	        if (matriculadosConsiderados==null) resM.put(llave, matriculadosConsiderados = new int[tamRes]);
	        for (int j=jI,jT=limSup; j<jT-2; j++,matri>>>=1) if ((matri&1L)==1L) {
	          if (!individuo.esGraduado(this)) matriculadosConsiderados[iesPrimiparo.ies.minCodigoSemestre+j-limInf]++;
	        }
	        //if ((ies.minCodigoSemestre+e.getUltimoSemestreMatriculado()+2>limSup)||(ies.minCodigoSemestre+e.getUltimoSemestreMatriculado()+2<limInf)) continue;
	        if (individuo.esDesertor(this)) desertores[individuo.getUltimoSemestreMatriculado()+2-limInf]++;
    }
    if (tamRes>2) {
      for (byte[] llave:resM.keySet()) {
        double [] tasa = new double[tamRes];
        int [] matriculadosConsiderados = resM.get(llave), desertores = resD.get(llave);
        for (int i=2;i<tamRes;i++) tasa[i] = matriculadosConsiderados[i-2]==0||desertores[i]==0?0d:(1d*desertores[i])/matriculadosConsiderados[i-2];
        resT.put(llave, tasa);
      }
    }
    return new Object[]{resM,resD,resT,codigosIESDif,codigosProgramasDif,tamRes, limInf};
    //return new Object[]{matriculadosConsiderados,desertores,tasa,/*codigosIESDif,codigosProgramasDif,*/tamRes, limInf};
  }

  @SuppressWarnings("unchecked")
  public Object[] getDesercionPorCohorte(SFiltro[] filtros) throws MyException {
    Object[] respre = getDesercionPorCohorte(filtros, new SVariable[0]);
    Map<byte[],double[][]> mres = (Map<byte[], double[][]>) respre[0],
      mresP = (Map<byte[], double[][]>) respre[1],
      mresC = (Map<byte[], double[][]>) respre[2];
    return new Object[]{mres.get(new byte[0]),mresP.get(new byte[0]),mresC.get(new byte[0]),respre[3]};
  }
  public Object[] getDesercionPorCohorte(SFiltro[] filtros, final SVariable[] diferenciados) throws MyException {
    
	Map<byte[],int[][]> conteos = new TreeMap<byte[], int[][]>(compByteArrayEspecial);
    Map<byte[],int[]> conteosPrimiparos = new TreeMap<byte[], int[]>(compByteArrayEspecial);
    Map<byte[],double[][]> mres = new TreeMap<byte[],double[][]>(compByteArrayEspecial),
      mresP = new TreeMap<byte[],double[][]>(compByteArrayEspecial),
      mresC = new TreeMap<byte[],double[][]>(compByteArrayEspecial);
    int limsSems[]=getLimsCodigoSemestre(listaIES),limInf=limsSems[0],limSup=limsSems[1];
    
    int tamRes = limSup - limInf+1;
    if (limInf==Integer.MAX_VALUE) return null;
    
    cicloIndividuo:for(Individuo individuo:DisjoinDataSet.getIndividuosIdsRelativos(this))if(individuo!=null){
		byte llave[]=new byte[diferenciados.length];
		for(int h=0;h<diferenciados.length;h++){
			SVariable v=diferenciados[h];
			Rango rango=v.rango;
			Comparable ran=rango.getRango(v.getValor(individuo));
			llave[h]=rango.rangoToByte(ran);  
		}
      
		Aparicion iesPrimiparo=individuo.getPrimiparoPrimeraVez();
		if(iesPrimiparo==null)continue;
        
		int mcs=iesPrimiparo.ies.minCodigoSemestre;
		int jI=iesPrimiparo.estudiante.getSemestrePrimiparo();
		
		if (!pasaFiltros(filtros,individuo)) continue;
		
		if (individuo.aparecePrimiparoUltimosSemestres(this)) continue;  // Estos estudiantes nunca se consideran desertores; afectarían el total si se consideraran
		
		int[] conteoPrimiparos = conteosPrimiparos.get(llave);
		if (conteoPrimiparos==null) conteosPrimiparos.put(llave, conteoPrimiparos = new int[tamRes]);
		
		int[][] conteo = conteos.get(llave);
		if (conteo==null) conteos.put(llave, conteo = new int[tamRes][tamRes]);
		
		conteoPrimiparos[mcs+jI-limInf]++;
		
		if (individuo.esDesertor(this) /*e.getEstado()==-1*/) {
          int i=individuo.getUltimoSemestreMatriculado()-individuo.getSemestrePrimiparo()+1;
		  if (i>=1 && i<=tamRes)conteo[mcs+jI-limInf][i-1]++;
		}
    }
    for (byte[] llave:conteos.keySet()) {
        int[] conteoPrimiparos = conteosPrimiparos.get(llave);
        int[][] conteo = conteos.get(llave);
        double res[][]=new double[tamRes][tamRes];
        double resP[][]=new double[tamRes][tamRes];
        double resC[][]=new double[tamRes][tamRes];
        for (int j=0; j<tamRes; j++) {
          Arrays.fill(res[j],Double.MAX_VALUE);
          int t=conteoPrimiparos[j];
          if (t==0) continue;
          for (int i=0,acum=0; i<tamRes; i++) {
            int v=conteo[j][i];
            acum+=v;
            res[j][i]=acum;
            resP[j][i]=100d*acum/t;
            resC[j][i]=v;
          }
          //double ult=resP[j][tamRes-1];
          //for (int i=tamRes-2; i>=0 && Math.abs(resP[j][i]-ult)<1e-3; i--) res[j][i+1]=resP[j][i+1]=Double.MAX_VALUE;
          for (int i=Math.max(tamRes-j-2,0); i>=0 && i<tamRes; i++) res[j][i]=resP[j][i]=Double.MAX_VALUE;
        }
        mres.put(llave,res);
        mresP.put(llave,resP);
        mresC.put(llave,resC);
      }
    System.out.println();
      return new Object[]{mres,mresP,mresC,new int[]{limInf,limSup},null,null};
  }
  public static final Comparator<byte[]> compByteArrayEspecial=new ComparadorEspecialArregloBytes();
  private static final class ComparadorEspecialArregloBytes implements Comparator<byte[]>,Serializable {
    private static final long serialVersionUID=9155885853505243356L;
    public int compare(byte[] a1, byte[] a2) {
      for (int i=0,t=a1.length; i<t; i++) if (a1[i]!=a2[i]) return (a1[i]&0xFF)-(a2[i]&0xFF);
      return 0;
    }
  };
  public Object[] getGradoCohorteAcumulado(SFiltro[] filtros, SVariable[] diferenciados) throws MyException {
    //return getCohorteEstadoAcumulado(filtros,diferenciados,(byte)1);
	  return null;
  }
  public Object[] getDesercion(SFiltro[] filtros, SVariable[] diferenciados) throws MyException {
    return getCohorteEstadoAcumulado(filtros,diferenciados);
  }
  public Object[] getCohorteEstadoAcumulado(SFiltro[] filtros, SVariable[] diferenciados) throws MyException {
    Map<byte[],int[]> resC=new TreeMap<byte[],int[]>(compByteArrayEspecial);
    Map<byte[],int[]> serieRef=new TreeMap<byte[],int[]>(compByteArrayEspecial);
    int limsSems[]=getLimsCodigoSemestre(listaIES),limInf=limsSems[0],limSup=limsSems[1];
    if (limInf==Integer.MAX_VALUE) return null;
    int tam=limSup-limInf+1;
    cicloIndividuo:for(Individuo individuo:DisjoinDataSet.getIndividuosIdsRelativos(this))if(individuo!=null){
		byte llave[]=new byte[diferenciados.length];
		for(int h=0;h<diferenciados.length;h++){
			SVariable v=diferenciados[h];
			Rango rango=v.rango;
			Comparable ran=rango.getRango(v.getValor(individuo));
			llave[h]=rango.rangoToByte(ran);  
		}
		
		Aparicion iesPrimiparo=individuo.getPrimiparoPrimeraVez();
		if(iesPrimiparo==null)continue;
        
		if (!pasaFiltros(filtros,individuo)) continue;
		
		int jI=iesPrimiparo.estudiante.getSemestrePrimiparo()+iesPrimiparo.ies.minCodigoSemestre;
		int datosRef[]=serieRef.get(llave);//Potencial de desertores con esas caracteristicas en cada numero de periodo.
		if (datosRef==null) serieRef.put(llave,datosRef=new int[tam]);
		
		for (int i=0,iT=individuo.getUltimoPeriodoFuturoEstudiante()-jI-2; i<iT; i++) datosRef[i]++;
		
		if (individuo.esDesertor(this)) {
			int serieDatos[]=resC.get(llave);
			if (serieDatos==null) {
				resC.put(llave,serieDatos=new int[tam]);
				if (limtser && resC.size()>120) throw new MyException("Los criterios de diferenciación suministrados arrojan más de 120 series de datos. Refine la consulta.");
			}
			Aparicion ultimaIes=individuo.getUltimaIESMatriculado();
			int jUlt = ultimaIes.estudiante.getUltimoSemestreMatriculado()+ultimaIes.ies.minCodigoSemestre;
			for (int j=jUlt,jT=individuo.getUltimoPeriodoFuturoEstudiante()-2; j<jT; j++) serieDatos[j-jI]++;
		}
    }
    
    if (resC.isEmpty()) return null;
    Map<byte[],double[]> resP=new TreeMap<byte[],double[]>(compByteArrayEspecial);
    for (Map.Entry<byte[],int[]> e:resC.entrySet()) {
      byte[] llavePer=e.getKey(),llaveEst=llavePer.clone();
      //for (int h:indicesDifPer) llaveEst[h]=0;
      int[] cont=e.getValue(),contRef=serieRef.get(llaveEst);
      double[] porc=new double[tam];
      boolean b=false;
      for (int i=0; i<tam; i++) if (cont[i]>0 && contRef[i]>10) {porc[i]=1d*cont[i]/contRef[i]; b=true;}
      if (b) resP.put(llavePer,porc);
    }
    System.out.println();
    return new Object[]{resC,resP,null,null,tam};
  }
  @SuppressWarnings("unchecked")
  private void censurarDesercionInterSemestral() {
    {
      try {
        //res = getDesercionIntersemestral(new Filtro[]{new Filtro(Variable.CODIGO_IES,new Item[]{new Item(new Integer(ies.codigo),"","")})}, new Variable[]{});
        for (Integer codIES: this.mapIESporCodigo.keySet()) {
          Filtro fil = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(codIES,"","")});
          Object[] res = getDesercionIntersemestral(new Filtro[]{fil}, new Variable[]{});
          if (res==null) continue;
          Map<byte[],double[]> resP=(Map<byte[],double[]>)(res[1]);
          IES ies = getIES(codIES);
          int nper = 0;
          int min = ies.minCodigoSemestre;
          int max = ies.maxCodigoSemestre;
          double[] serd = resP.values().iterator().next();
          //System.err.println(serd.length + "\t" + min + "\t" + max);
          if (max-min+1!=serd.length) System.err.println("Alerta 1983");
          //for (int i = min;i<=max;i++) {
          for (int i = 0, iT = serd.length;i<iT;i++) {
            //ies.periodosDesIntValida[i-min] = serd[i-min]<0.5;
            ies.periodosDesIntValida[i] = serd[i]<0.5;
            ++nper;
          }
        }
      } catch (MyException e) {
        e.printStackTrace();
        return;
      }
    }
  }
}