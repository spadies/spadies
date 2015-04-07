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
package spadies.server.kernel;

import java.io.*;
import java.util.*;
import spadies.io.*;
import spadies.server.util.*;
import spadies.util.*;
import static spadies.util.CajaDeHerramientas.*;

public final class PreparadorDatos {
  private static final PreparadorDatos instance=new PreparadorDatos();
  public static PreparadorDatos getInstance() {return instance;}
  private PreparadorDatos() {}
  //TODO debia ser private, se pone asi para auxiliar
  public static interface ArchivoCSV {
    public String getTitulo();
    public File getIn();
    public File getOut();
    public String[] getIdLinea(String[] w);
    public byte[] getDatoLinea(String[] w);
  }
  public void prepararArchivosBase(boolean[] tablasGrandesEncontradas) throws Exception {
    ArchivoCSV arr[]={
      new ArchivoCSV() {
        public String getTitulo() {return "ICFES";}
        public File getIn() {return ConstantesServer.fICFES;}
        public File getOut() {return ConstantesServer.gICFES;}
        public String[] getIdLinea(String[] w) {return new String[]{w[5],w[4]};}
        public byte[] getDatoLinea(String[] w) {return prepararSalidaIcfes(w);}
      },
      new ArchivoCSV() {
        public String getTitulo() {return "ICETEX";}
        public File getIn() {return ConstantesServer.fICETEX;}
        public File getOut() {return ConstantesServer.gICETEX;}
        public String[] getIdLinea(String[] w) {return new String[]{(w[15].trim().length()>0)?w[15]:(w[11]+" "+w[14]),w[8]};}
        public byte[] getDatoLinea(String[] w) {return prepararSalidaIcetex(w);}
      },
      new ArchivoCSV() {
        public String getTitulo() {return "GRADUADOS";}
        public File getIn() {return ConstantesServer.fGRADUADOS;}
        public File getOut() {return ConstantesServer.gGRADUADOS;}
        public String[] getIdLinea(String[] w) {return new String[]{w[2],w[1]};}
        public byte[] getDatoLinea(String[] w) {return prepararSalidaGraduados(w);}
      }
    };
    for (int i=0; i<3; i++) tablasGrandesEncontradas[i]=(arr[i].getOut().exists()||arr[i].getIn().exists());
    if (tablasGrandesEncontradas[0] || tablasGrandesEncontradas[1] || tablasGrandesEncontradas[2]) {
      File fCarp=ConstantesServer.cDatosExternosProcesados;
      if (!fCarp.exists()) fCarp.mkdir();
      if (!fCarp.exists() || !fCarp.isDirectory()) throw new Exception("No se pudo crear la carpeta "+fCarp.getPath());
    }
    for (int i=0; i<3; i++) if (tablasGrandesEncontradas[i]) prepararArchivoBase(arr[i]);
    for (int i=0; i<3; i++) if (!tablasGrandesEncontradas[i]) {
      System.out.println("Warning: no se encontró ninguno de los archivos:");
      System.out.println("    "+arr[i].getOut().getPath());
      System.out.println("    "+arr[i].getIn().getPath());
    }
  }
  private static byte[] prepararSalidaIcfes(String[] w) {
    MyByteSequence res=new MyByteSequence(15);
    byte sexo=toByte(w[9],-1,0,1),numHermanos=toByte(w[53],-1,1,15),posHermanos=toByte(w[56],-1,1,15),
      m_numHermanos=toByte(w[87],-1,1,15),m_posHermanos=toByte(w[88],-1,1,15);
    if (sexo!=-1) sexo=(byte)(1-sexo);
    if (posHermanos!=-1 && numHermanos!=-1 && posHermanos>numHermanos) posHermanos=numHermanos;
    if (m_posHermanos!=-1 && m_numHermanos!=-1 && m_posHermanos>m_numHermanos) m_posHermanos=m_numHermanos;
    res.setByte(0,getCodigoSemestre(w[0].trim()));            // IdAñoSemestre
    res.setByte(1,(byte)(toByte(w[74],-1,0,100)+1));          // PuntajeIcfes
    res.setByte(2,(byte)(toByte(w[15],-1,0,120)+1));          // EdadPresentacionIcfes
    res.setBits2(3,0,(byte)(toByte(w[27],-1,0,1)+1));         // ViviendaPropia
    res.setBits2(3,2,(byte)(toByte(w[58],-1,0,1)+1));         // TrabajabaCuandoIcfes
    res.setBits4(3,4,(byte)(leerEducacionMadre(w[37],w[38],w[39],w[40])+1)); // EducacionMadre
    res.setBits4(4,0,(byte)(toByte(w[/*80*/79],-1,0,9)+1));         // IngresoHogar
    res.setBits2(4,4,(byte)(sexo+1));                         // Sexo
    res.setBits2(4,6,(byte)(stringToInteger(w[76])));         // Remplaza
    res.setBits4(5,0,(byte)(numHermanos==-1?0:numHermanos));  // NumeroHermanos
    res.setBits4(5,4,(byte)(posHermanos==-1?0:posHermanos));  // PosicionEntreHermanos
    //res.setInt(6,stringToInteger(w[11]));
    res.setByte(6,(byte) (codifPerIcfes(stringToInteger(w[75]))+1));
    res.setByte(7,(byte) (codifPerIcfes(stringToInteger(w[77]))+1));
    res.setBits4(8,0,(byte)(toByte(w[62/*78*/],-1,1,8)+1));         // Estrato
    res.setBits4(8,4,(byte)(toByte(w[78/*79*/],-1,1,5)+1));         // NivelSisben
    res.setBits4(9,0,(byte)(toByte(w[26],-1,1,12)+1));              // PersonasFamilia
    res.setBits4(9,4,(byte)(toByte(w[80],-1,1,7)+1));               // Ingreso nuevo
    
    res.setBits2(10,0,(byte)(toByte(w[85],-1,0,1)+1));         //Moda ViviendaPropia
    res.setBits2(10,2,(byte)(toByte(w[89],-1,0,1)+1));         //Moda TrabajabaCuandoIcfes
    res.setBits4(10,4,(byte)(toByte(w[81],-1,1,4)+1)); // Moda EducacionMadre
    res.setBits4(11,0,(byte)(toByte(w[82],-1,0,9)+1));         //Moda IngresoHogar
    //Vacante 11.4 4bits
    res.setBits4(12,0,(byte)(m_numHermanos==-1?0:m_numHermanos));  // NumeroHermanos
    res.setBits4(12,4,(byte)(m_posHermanos==-1?0:m_posHermanos));  // PosicionEntreHermanos
    res.setBits4(13,0,(byte)(toByte(w[90],-1,1,8)+1));         // Estrato
    res.setBits4(13,4,(byte)(toByte(w[91],-1,1,5)+1));         // NivelSisben
    res.setBits4(14,0,(byte)(toByte(w[92],-1,1,12)+1));              // PersonasFamilia
    res.setBits4(14,4,(byte)(toByte(w[83],-1,1,7)+1));               // Ingreso nuevo
    return res.getBytes();
  }
  private static int codifPerIcfes(int x) {
    if (x==-1) return -1;
    int p1=x/10,p2=x%10;
    if (p1<1990 || p1>2040 || (p2!=1 && p2!=2)) return -1;
    return (byte) ((p1-1990)*2+(p2-1));
    
  }
  private static byte[] prepararSalidaIcetex(String[] w) {
    byte[] res=new byte[2];
    res[0]=getCodigoSemestre(w[18].trim()+w[19].trim());  // IdAñoSemestre
    res[1]=leerTipoApoyoIcetex(w[21].trim());             // TipoCredito
    return res;
  }
  private static byte[] prepararSalidaGraduados(String[] w) {
    MyByteSequence res=new MyByteSequence(5);
    res.setByte(0,getCodigoSemestre(w[6].trim()+w[7].trim()));
    res.setInt(1,stringToInteger(leerNumeroNatural(w[4].trim())));
    return res.getBytes();
  }
  private static byte toByte(String val, int defecto, int min, int max) {
    int x=stringToInteger(leerNumeroNatural(val));
    return (byte)((x<min || x>max)?defecto:x);
  }
  private static byte leerEducacionMadre(String...vals) {
    for (int i=3; i>=0; i--) if (leerNumeroNatural(vals[i]).equals("1")) return (byte)(i+1);
    return -1;
  }
  //TODO debia ser private, s epone asi para auxiliar
  public void prepararArchivoBase(ArchivoCSV a) throws Exception {
    File fIn=a.getIn(),fOut=a.getOut();
    if (fOut.exists()) return;
    {
      long tmi=System.currentTimeMillis();
      System.out.println("PREPARANDO EL ARCHIVO "+fOut.getName());
      Map<byte[],byte[]> map=new TreeMap<byte[],byte[]>(comparadorByteArray);
      BufferedReader br=new BufferedReader(new FileReader(fIn)); String sL=br.readLine(); int h=0;
      for (; (sL=br.readLine())!=null; h++) {
        if ((h%25000)==0) System.out.println("  T="+timeToString(System.currentTimeMillis()-tmi)+". Procesando "+a.getTitulo()+": C="+h+"("+map.size()+")");
        String wL[]=csvToString(sL,0,';'),idL[]=a.getIdLinea(wL);
        byte[] id=concatenarArreglos(codifLetrasServer.getCodigos(idL[0]),new byte[]{(byte)255},codifNumeros.getCodigos(idL[1])),dato=a.getDatoLinea(wL),datoV=map.get(id);
        map.put(id,(datoV==null)?dato:concatenarArreglos(datoV,dato));
      }
      br.close();
      System.out.println("  T="+timeToString(System.currentTimeMillis()-tmi)+". Se procesó "+a.getTitulo()+": C="+h+"("+map.size()+")");
      System.out.println("  T="+timeToString(System.currentTimeMillis()-tmi)+". Escribiendo resultado ("+a.getTitulo()+")");
      br.close();
      MyDataOutputStream os=new MyDataOutputStream(new FileOutputStream(fOut));
      os.writeInt(map.size());
      for (Map.Entry<byte[],byte[]> e:map.entrySet()) {
        String id[]=split(new String(e.getKey()),(char)255);
        os.writeByteArray(true,id[0].getBytes());
        os.writeByteArray(true,id[1].getBytes());
        os.writeByteArray(true,e.getValue());
      }
      os.close();
      System.gc();
      System.out.println("  T="+timeToString(System.currentTimeMillis()-tmi)+". Resultado escrito ("+a.getTitulo()+")");
    }
    {
      long tmi=System.currentTimeMillis();
      System.out.println("ANALIZANDO EL ARCHIVO "+fOut.getName());
      MyDataInputStream is=new MyDataInputStream(new FileInputStream(fOut));
      MyDataOutputStream os=new MyDataOutputStream(new FileOutputStream(fOut.getPath()+".indices"));
      int T1=is.readInt(),VECES=1+(T1-1)/ConstantesServer.TAM_TANDA;
      for (int vez=1,h1=0; vez<=VECES; vez++) {
        MyMap mapNoms=new MyMap(comparadorByteArray),mapDocs=new MyMap(comparadorByteArray);
        int h1T=Math.min(vez*ConstantesServer.TAM_TANDA,T1),hT=h1T-h1;
        byte[][] datNoms=new byte[hT][],datDocs=new byte[hT][];
        for (int h=0; h1<h1T; h++,h1++) {
          @SuppressWarnings("unused")
          byte[] aNom=is.readByteArray(true,-1),aDoc=is.readByteArray(true,-1),aDato=is.readByteArray(true,-1);
          datNoms[h]=aNom;
          datDocs[h]=aDoc;
          mapNoms.add(aNom,h);
          mapDocs.add(aDoc,h);
        }
        new MySortedArray(datNoms,comparadorByteArray,mapNoms.values()).write(os);
        new MySortedArray(datNoms,comparadorByteArrayRev,mapNoms.values()).write(os);
        new MySortedArray(datDocs,comparadorByteArray,mapDocs.values()).write(os);
        new MySortedArray(datDocs,comparadorByteArrayRev,mapDocs.values()).write(os);
        mapNoms=mapDocs=null; datNoms=datDocs=null; System.gc();
      }
      os.close();
      is.close();
      System.out.println("  T="+timeToString(System.currentTimeMillis()-tmi)+". Archivo analizado ("+fOut.getName()+")");
    }
  }
}
