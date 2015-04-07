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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import spadies.io.MyDataInputStream;
import spadies.io.MyDataOutputStream;
import spadies.util.CajaDeHerramientas;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.Constantes.VersionDatos;

public final class IES {
  public VersionDatos version = null; 
  /**
   * Codigo SNIES de la IES
   */
  public int codigo=0;
  /**
   * Numero de semestres que las IES reporto
   */
  public int n=0;  // Número de semestres para los que la IES dio información
  /**
   * Codigos del minimo y maximo semestre de los reportados por la IES
   */
  public int minCodigoSemestre=-1,maxCodigoSemestre=-1;
  public String[] semestres=null;
  /**
   * Nombre de la IES en bytes ASCII
   */
  public byte[] nombre={};
  /**
   * Codigo del departamento
   */
  public byte departamento=-1;
  /**
   * Codigo del municipio
   */
  public short municipio=-1;
  /**
   * Codigo del origen
   */
  public byte origen=-1;
  /**
   * Codigo del caracter
   */
  public byte caracter=-1;
  /**
   * Variables estaticas 
   */
  //public int numVariablesExtra = 0, numVariablesExtraD = 0;
  public VariableExtra[] variablesExtras=new VariableExtra[5];
  /**
   * Variables dinamicas 
   */
  public VariableExtra[] variablesExtrasD=new VariableExtra[5];
  /**
   * Programas de la IES
   */
  public Programa[] programas=null;
  /**
   * Costos de matricula, para cada programa y cada periodo de la IES
   */
  public int[][] costosProgramas=null;
  /**
   * Arreglo con la informacion estadistica de los studiantes de la IES
   */
  public Estudiante[] estudiantes=null;
  /**
   * Arreglo con la informacion de identificación de los estudiantes de la IES
   */
  public Estudiante_DatosPersonales[] datosPersonalesEstudiantes=null;
  /**
   * Arreglo con longitud igual al numero de periodo de la IES, indica que periodos deben ser mostrados en la consulta de ... 
   */
  public boolean[] periodosDesIntValida;
  /**
   * Contstructor
   */
  public IES() {
    for (int k=0; k<Constantes.maxVariablesExtra; k++) variablesExtras[k]=new VariableExtra();
    for (int k=0; k<Constantes.maxVariablesExtraDinamicas; k++) variablesExtrasD[k]=new VariableExtra();
  }
  /**
   * Carga la informacion de una IES del Stream especificado.
   * @param is Stream de lectura que contiene la información de la IES
   * @param cargarDatosPersonales Indica si han de cargarlos los datos personales de los estudiantes
   * @return La IES cargada
   * @throws Exception
   */
  public static IES cargar(MyDataInputStream is, boolean cargarDatosPersonales) throws Exception {
    return cargar(is, cargarDatosPersonales, Constantes.verDatos);
  }
  private static IES cargar(MyDataInputStream is, boolean cargarDatosPersonales, VersionDatos vers) throws Exception {
    IES ies=new IES();
    ies.version = vers;
    if (is.readLong()!=7148461346797979442L) throw new MyException("Actualice la versión de SPADIES para poder comunicarse con el servidor del Ministerio de Educación Nacional.");
    ies.codigo=is.readInt();
    int n=ies.n=is.readInt();
    ies.minCodigoSemestre=is.readByte();
    ies.maxCodigoSemestre=is.readByte();
    ies.semestres=CajaDeHerramientas.getTextosSemestresEntre((byte)(ies.minCodigoSemestre),(byte)(ies.maxCodigoSemestre));
    ies.nombre=is.readByteArray(true,-1);
    ies.departamento=is.readByte();
    ies.municipio=is.readShort();
    ies.origen=is.readByte();
    ies.caracter=is.readByte();
    if (is.readLong()!=4971477911791144911L) throw new MyException("Actualice la versión de SPADIES para poder comunicarse con el servidor del Ministerio de Educación Nacional.");
    for (int k=0; k<Constantes.maxVariablesExtra; k++) ies.variablesExtras[k]=VariableExtra.cargar(is);
    if (ies.version!=VersionDatos.V_2_3_1) for (int k=0; k<Constantes.maxVariablesExtraDinamicas; k++) ies.variablesExtrasD[k]=VariableExtra.cargar(is);
    int np=is.readInt();
    ies.programas=new Programa[np];
    for (int k=0; k<np; k++) ies.programas[k]=Programa.cargar(is);
    ies.costosProgramas = new int[np][n];
    for (int i=0;i<np;i++) for (int j=0;j<n;j++) ies.costosProgramas[i][j]=is.readInt();//Nuevo
    int ne=is.readInt();
    ies.estudiantes=new Estudiante[ne];
    CargadorEstudiantes carg = CargadorEstudiantes.getCargador(ies.version, n);
    for (int k=0; k<ne; k++) ies.estudiantes[k]=carg.cargar(is);
    byte b=is.readByte();
    if (cargarDatosPersonales && b==1) {
      ies.datosPersonalesEstudiantes=new Estudiante_DatosPersonales[ne];
      for (int k=0; k<ne; k++) ies.datosPersonalesEstudiantes[k]=Estudiante_DatosPersonales.cargar(is);
    }
    ies.periodosDesIntValida = new boolean[ies.semestres.length];
    Arrays.fill(ies.periodosDesIntValida, true);
    return ies;
  }
  public static void guardar(MyDataOutputStream os, IES ies, boolean guardarDatosPersonales) throws Exception {
    os.writeLong(7148461346797979442L);
    os.writeInt(ies.codigo);
    os.writeInt(ies.n);
    os.writeByte((byte)(ies.minCodigoSemestre));
    os.writeByte((byte)(ies.maxCodigoSemestre));
    os.writeByteArray(true,ies.nombre);
    os.writeByte(ies.departamento);
    os.writeShort(ies.municipio);
    os.writeByte(ies.origen);
    os.writeByte(ies.caracter);
    os.writeLong(4971477911791144911L);
    for (int k=0; k<Constantes.maxVariablesExtra; k++) VariableExtra.guardar(os,ies.variablesExtras[k]);
    for (int k=0; k<Constantes.maxVariablesExtraDinamicas; k++) VariableExtra.guardar(os,ies.variablesExtrasD[k]);
    os.writeInt(ies.programas.length);
    for (Programa p:ies.programas) Programa.guardar(os,p);
    for (int[] cP:ies.costosProgramas) for (int cPP:cP) os.writeInt(cPP);//Nuevo
    os.writeInt(ies.estudiantes.length);
    for (Estudiante e:ies.estudiantes) e.guardar(os);
    if (guardarDatosPersonales && ies.datosPersonalesEstudiantes!=null) {
      os.writeByte((byte)1);
      for (Estudiante_DatosPersonales edp:ies.datosPersonalesEstudiantes) Estudiante_DatosPersonales.guardar(os,edp); 
    }
    else {
      os.writeByte((byte)0);
    }
  }
  
  public static IES cargar(File file, boolean cargarDatosPersonales) throws Exception {//ORIGINAL
    MyDataInputStream is=new MyDataInputStream(CajaDeHerramientas.getZipInputStream(CajaDeHerramientas.getCipherInputStream(new FileInputStream(file),"SPADIES")));
    //MyDataInputStream is=new MyDataInputStream(CajaDeHerramientas.getZipInputStream(CajaDeHerramientas.getCipherInputStream(new FileInputStream(file),"XX20071107XX")));
    //MyDataInputStream is=new MyDataInputStream(CajaDeHerramientas.getZipInputStream(CajaDeHerramientas.getCipherInputStream(((HttpURLConnection)new URL("http://157.253.22.157/pmid5i0l37/9999.spa").openConnection()).getInputStream(),"XX20071107XX")));
    try {
      if (is.readLong()!=1834809409255049615L) throw new MyException("El archivo \""+file.getName()+"\" no es un archivo válido para la aplicación "+Constantes.nombreAplicacionLargo);
      String ver=new String(is.readByteArray(true,-1));
      VersionDatos vers = VersionDatos.getVersion(ver);
      if (vers==null) throw new MyException("La versión del archivo \""+file.getName()+"\" ("+ver+") no corresponde con la versión de la aplicación ("+Constantes.versionDatos+").");
      return cargar(is,cargarDatosPersonales,vers);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
    finally {
      try {
        is.close();
      }
      catch (Throwable th) {
      }
    }
  }

  public static File getFile(File carpeta, IES ies) {
    return new File(carpeta,ies.codigo+".spa");
  }
  public static void guardar(File carpeta, IES ies, boolean guardarDatosPersonales) throws Exception {
    File file=getFile(carpeta,ies);
    CajaDeHerramientas.borrarArchivo(file);
    if (file.exists() && file.isDirectory()) throw new MyException("Borre la carpeta \""+file.getPath()+"\".");
    MyDataOutputStream os=new MyDataOutputStream(CajaDeHerramientas.getZipOutputStream(CajaDeHerramientas.getCipherOutputStream(new FileOutputStream(file),"SPADIES")));
    //MyDataOutputStream os=new MyDataOutputStream(CajaDeHerramientas.getZipOutputStream(CajaDeHerramientas.getCipherOutputStream(new FileOutputStream(file),"XX20071107XX")));
    try {
      os.writeLong(1834809409255049615L);
      os.writeByteArray(true,ies.version.txtVersion.getBytes());
      //os.writeByteArray(true,"2.3.1".getBytes());
      guardar(os,ies,guardarDatosPersonales);
    }
    catch (Exception ex) {
      throw ex;
    }
    finally {
      try {
        os.close();
      }
      catch (Throwable th) {
      }
    }
  }
  public static byte[] iesToByteArray(IES ies, String password) throws Exception {
    ByteArrayOutputStream bos=new ByteArrayOutputStream(1000000);
    MyDataOutputStream os=new MyDataOutputStream(CajaDeHerramientas.getZipOutputStream(CajaDeHerramientas.getCipherOutputStream(bos,password)));
    try {
      guardar(os,ies,true);
    }
    catch (Exception ex) {
      throw ex;
    }
    finally {
      try {
        os.close();
      }
      catch (Throwable th) {
      }
    }
    return bos.toByteArray();
  }
  public static IES byteArrayToIES(byte[] arr, String password) throws Exception {
    ByteArrayInputStream bis=new ByteArrayInputStream(arr);
    MyDataInputStream is=new MyDataInputStream(CajaDeHerramientas.getZipInputStream(CajaDeHerramientas.getCipherInputStream(bis,password)));
    try {
      return cargar(is,true);
    }
    catch (Exception ex) {
      throw ex;
    }
    finally {
      try {
        is.close();
      }
      catch (Throwable th) {
      }
    }
  }
  /**
   * Retorna los datos personales almacenados en un archivo <i>spa</i>
   * @param file Archivo del que se cargaran los datos
   * @return Arreglo con los datos de identificacion de los estudiantes
   */
  public static Estudiante_DatosPersonales[] getDatosPersonales(File file) {
    try {
      return cargar(file,true).datosPersonalesEstudiantes;
    }
    catch (Throwable th) {
      return null;
    }
  }
  public int getTamanhoEnBytes(boolean conEncabezado, boolean guardarDatosPersonales) {
    int res=0;
    if (conEncabezado) res+=10+Constantes.versionDatos.getBytes().length;
    res+=20+nombre.length+13;
    for (int k=0; k<Constantes.maxVariablesExtra; k++) res+=variablesExtras[k].getTamanhoEnBytes();
    for (int k=0; k<Constantes.maxVariablesExtraDinamicas; k++) res+=variablesExtrasD[k].getTamanhoEnBytes();
    res+=4;
    for (Programa p:programas) res+=p.getTamanhoEnBytes();
    res+=4+estudiantes.length*(estudiantes.length==0?0:estudiantes[0].getSize());
    res+=1;
    if (guardarDatosPersonales && datosPersonalesEstudiantes!=null) {
      for (Estudiante_DatosPersonales edp:datosPersonalesEstudiantes) res+=edp.getTamanhoEnBytes(); 
    }
    return res;
  }
}
