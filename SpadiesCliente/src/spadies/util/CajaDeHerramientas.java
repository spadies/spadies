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
import java.lang.reflect.*;
import java.text.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public final class CajaDeHerramientas {
  // RUTINAS DE CADENAS QUE REPRESENTAN PERÍODOS
  public static byte getCodigoSemestre(String sem) {
    try {
      int res=Integer.parseInt(sem.substring(0,4))*2+Integer.parseInt(sem.substring(4,5))-1-(Constantes.anhoIni*2);
      return (byte)((res>=0 && res<=119)?res:0);  // Hay máximo 120 identificadores para los períodos (de 0 a 119)
    }
    catch (Throwable th) {
      return 0;
    }
  }
  public static String getTextoSemestre(byte codigo) {
    return (Constantes.anhoIni+(codigo>>>1))+""+((codigo&1)+1);
  }
  public static String[] getTextosSemestresEntre(int minAnho, int minSem, int maxAnho, int maxSem) {
    List<String> res=new Vector<String>();
    int a=minAnho,s=minSem;
    while (true) {
      res.add(""+a+""+s);
      if (a==maxAnho && s==maxSem) break;
      s=3-s;
      if (s==1) a++;
    }
    return res.toArray(new String[0]);
  }
  public static String[] getTextosSemestresEntre(String sem1, String sem2) {
    return getTextosSemestresEntre(Integer.parseInt(sem1.substring(0,4)),Integer.parseInt(sem1.substring(4,5)),Integer.parseInt(sem2.substring(0,4)),Integer.parseInt(sem2.substring(4,5)));
  }
  public static String[] getTextosSemestresEntre(byte codigo1, byte codigo2) {
    return getTextosSemestresEntre(getTextoSemestre(codigo1),getTextoSemestre(codigo2));
  }
  public static Byte[] getCodigosSemestresEntre(byte codigo1, byte codigo2) {
    int tam=((int)codigo2)-((int)codigo1)+1;
    Byte[] res=new Byte[tam+1];
    res[0]=-1;
    for (int i=0; i<tam; i++) res[i+1]=(byte)(codigo1+i);
    return res;
  }
  public static String textoSemestreToString(String sem) {
    return sem.substring(0,4)+"-"+sem.substring(4,5);
  }
  public static String[] textoSemestresToString(String[] arrSems) {
    String res[]=new String[arrSems.length];
    for (int i=0,t=arrSems.length; i<t; i++) res[i]=textoSemestreToString(arrSems[i]);
    return res;
  }
  public static String codigoSemestreToString(byte codigo) {
    return textoSemestreToString(getTextoSemestre(codigo));
  }
  // RUTINAS DE ARCHIVOS
  public static boolean esArchivoIES(File f) {
    if (!f.isFile()) return false;
    String s=f.getName();
    return s.endsWith(".spa") && esStringIES(s.substring(0,s.length()-".spa".length()));
  }
  public static boolean esArchivoIESFiltro(File f, Collection<String> filtro) {
    if (!f.isFile()) return false;
    String s=f.getName();
    return s.endsWith(".spa") && esStringIES(s.substring(0,s.length()-".spa".length()))
      && (filtro.isEmpty() || filtro.contains(s.substring(0,s.length()-".spa".length())));
  }
  public static boolean esCarpetaIES(File f) {
    return f.isDirectory() && esStringIES(f.getName());
  }
  public static boolean esStringIES(String s) {
    if (s.length()>6) return false;
    for (int i=0,t=s.length(); i<t; i++) if (s.charAt(i)<'0' || s.charAt(i)>'9') return false;
    return true;
  }
  public static void borrarArchivo(File f) {
    vaciarDirectorio(f);
    f.delete();
  }
  public static void vaciarDirectorio(File f) {
    if (f.isDirectory()) for (File g:f.listFiles()) borrarArchivo(g);
  }
  public static int contarArchivos(File f) {
    int r=1;
    if (f.isDirectory()) for (File g:f.listFiles()) r+=contarArchivos(g);
    return r;
  }
  // RUTINAS DE ENCRIPCIÓN Y COMPRESIÓN
  private static byte[] fabricarLlave128bits(String password) {
    try {
      return MessageDigest.getInstance("MD5").digest(password.getBytes());
    }
    catch (Throwable th) {
      return new byte[16];
    }
  }
  public static Cipher getCipherEncrypt(String password) throws Exception {
    Cipher cipher=Cipher.getInstance("AES/ECB/PKCS5Padding"); 
    cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(fabricarLlave128bits(password),"AES"));
    return cipher;
  }
  public static Cipher getCipherDecrypt(String password) throws Exception {
    Cipher cipher=Cipher.getInstance("AES/ECB/PKCS5Padding"); 
    cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(fabricarLlave128bits(password),"AES"));
    return cipher;
  }
  public static CipherOutputStream getCipherOutputStream(OutputStream os, String password) throws Exception {
    return new CipherOutputStream(os,getCipherEncrypt(password));
  }
  public static CipherInputStream getCipherInputStream(InputStream is, String password) throws Exception {
    return new CipherInputStream(is,getCipherDecrypt(password));
  }
  public static ZipOutputStream getZipOutputStream(OutputStream os) throws Exception {
    ZipOutputStream zos=new ZipOutputStream(os);
    zos.putNextEntry(new ZipEntry("archivo"));
    return zos;
  }
  public static ZipInputStream getZipInputStream(InputStream is) throws Exception {
    ZipInputStream zis=new ZipInputStream(is);
    zis.getNextEntry();
    return zis;
  }
  // RUTINAS DE MAJEJO DE ARREGLOS
  @SuppressWarnings("unchecked")
  public static <T> T[] concatenarArreglos(Class<T> clase, T[]...arreglos) {
    int g=0;
    for (T[] arr:arreglos) g+=arr.length;
    T[] res=(T[])(Array.newInstance(clase,g));
    g=0;
    for (T[] arr:arreglos) {
      System.arraycopy(arr,0,res,g,arr.length);
      g+=arr.length;
    }
    return res;
  }
  public static byte[] concatenarArreglos(byte[]...arreglos) {
    int g=0;
    for (byte[] arr:arreglos) g+=arr.length;
    byte[] res=new byte[g];
    g=0;
    for (byte[] arr:arreglos) {
      System.arraycopy(arr,0,res,g,arr.length);
      g+=arr.length;
    }
    return res;
  }
  @SuppressWarnings("unchecked")
  public static <T> T[] getSubArreglo(Class<T> clase, T[] arr, int p1, int p2) {
    p1=Math.max(p1,0);
    p2=Math.min(p2,arr.length-1);
    T[] res=(T[])(Array.newInstance(clase,p2-p1+1));
    System.arraycopy(arr,p1,res,0,p2-p1+1);
    return res;
  }
  public static byte[] getSubArreglo(byte[] arr, int p1, int p2) {
    byte[] res=new byte[p2-p1+1];
    System.arraycopy(arr,p1,res,0,p2-p1+1);
    return res;
  }
  public static int[] toIntArray(Collection<Integer> collection) {
    int t=collection.size(),res[]=new int[t],i=0;
    for (Integer x:collection) res[i++]=x;
    return res;
  }
  public static int searchSame(Object[] objetos, Object objeto) {
    if (objeto!=null) for (int i=0,t=objetos.length; i<t; i++) if (objeto==objetos[i]) return i;
    return -1;
  }
  public static int searchEquals(Object[] objetos, Object objeto) {
    if (objeto!=null) for (int i=0,t=objetos.length; i<t; i++) if (objetos[i]!=null && objetos[i].equals(objeto)) return i;
    return -1;
  }
  // RUTINAS DE MANEJO DE TEXTOS
  public static String stringToHTML(String s) {
    StringBuffer sb=new StringBuffer();
    for (int i=0,t=s.length(); i<t; i++) sb.append(charToHTML(i==0||(i+1<t&&s.charAt(i+1)==' '),s.charAt(i)));
    return sb.toString();
  }
  private static String charToHTML(boolean esEspacioEspecial, char c) {
    switch (c) {
      case ' ': return (esEspacioEspecial?"&nbsp;":" ");
      case '<': return "&lt;";
      case '>': return "&gt;";
      case '&': return "&amp;";
      case '\n': return "<p>";
      default: return String.valueOf(c);
    }
  }
  public static String[] split(String s, char sep) {
    return s.split("\\"+sep,-1);
  }
  private static List<String> csvToStringSep(String s, char sep) {
    List<String> p=new ArrayList<String>(10);
    try {
      for (int i=0,t=s.length(); i<t; ) {
        if (s.charAt(i)==sep) {
          p.add("");
          i++;
        }
        else if (s.charAt(i)!='"') {
          int j=s.indexOf(sep,i+1);
          if (j==-1) j=t;
          p.add(s.substring(i,j));
          i=j+1;
        }
        else {
          int j=s.indexOf('"',i+1);
          while (j!=-1 && j<t-1 && s.charAt(j+1)=='"') j=s.indexOf('"',j+2);
          p.add(s.substring(i+1,j).replaceAll("\"\"","\""));
          j=s.indexOf(sep,j+1);
          if (j==-1) j=t;
          i=j+1;
        }
        if (i==t) p.add("");
      }
    }
    catch (Throwable th) {
    }
    return p;
  }
  public static String[] csvToString(String s, int n, char sep) {
    List<String> p=csvToStringSep(s,sep);
    for (int i=p.size(); i<n; i++) p.add("");
    String res[]=new String[p.size()];
    {
      int i=0;
      for (String x:p) {
        x=x.trim();
        if (x.startsWith("'")) x=x.substring(1);
        res[i++]=x;
      }
    }
    return res;
  }
  public static String stringToCSV(String...w) {
    String s="";
    for (String u:w) {
      if (u.indexOf(';')!=-1 || u.indexOf('"')!=-1) {
        s+="\""+u.replaceAll("\"","\"\"")+"\";";
      }
      else {
        s+=u+";";
      }
    }
    return s;
  }
  public static long stringToLong(String s) {
    try {return Long.parseLong(s);} catch (Throwable th) {return -1L;}
  }
  public static double stringToDouble(String s) {
    try {return Double.parseDouble(s);} catch (Throwable th) {return -1.0;}
  }
  public static int stringToInteger(String s) {
    try {return Integer.parseInt(s);} catch (Throwable th) {return -1;}
  }
  public static String intToString(int x, int n) {
    String s=""+x;
    for (int i=s.length(); i<n; i++) s="0"+s;
    return s;
  }
  public static String timeToString(long msec) {
    return (msec==Long.MAX_VALUE)?"?":((msec/60000)+"m"+intToString((int)((msec/1000)%60),2)+"s"+intToString((int)(msec%1000),3)+"ms");
  }
  private static String meses[]={"ENE","FEB","MAR","ABR","MAY","JUN","JUL","AGO","SEP","OCT","NOV","DIC"};
  private static DateFormat dtf=new SimpleDateFormat("yyyy/MM/dd"),dtfs[]={
    new SimpleDateFormat("dd/MM/yyyy"),
    new SimpleDateFormat("yyyy/MM/dd"),
    new SimpleDateFormat("dd/MM/yy"),
    new SimpleDateFormat("yy/MM/dd"),
    new SimpleDateFormat("ddMMyyyy"),
    new SimpleDateFormat("yyyyMMdd"),
    new SimpleDateFormat("ddMMyy"),
    new SimpleDateFormat("yyMMdd")};
  public static String leerFecha(String s) {
    s=s.toUpperCase().trim();
    if (s.length()>=6) for (String w:s.split(" +")) if (w.length()>=6) {
      String v=w;
      for (int i=0; i<12; i++) {
        int j=v.indexOf(meses[i]);
        if (j==-1) continue;
        v=v.substring(0,j)+intToString(i+1,2)+v.substring(j+meses[i].length());
      }
      v=v.replace('-','/').replace('\\','/').replaceAll("[^0-9\\/]","");
      for (DateFormat x:dtfs) {
        try {
          String sr=dtf.format(x.parse(v));
          if (sr.length()!=10) continue;
          if (sr.startsWith("00")) sr="19"+sr.substring(2);
          return sr;
        }
        catch (Throwable th) {
        }
      }
    }
    return "";
  }
  public static String fechaToString(short anho, byte mes, byte dia) {
    if (anho==-1 || mes==-1 || dia==-1) return "";
    return intToString(dia,2)+"/"+intToString(mes,2)+"/"+intToString(anho,4);
  }
  public static byte leerTipoDocumento(String s) {
    s=s.toUpperCase().trim();
    if (s.indexOf('C')!=-1) return 0;
    if (s.indexOf('T')!=-1) return 1;
    if (s.indexOf('E')!=-1) return 2;
    if (s.indexOf('R')!=-1) return 3;
    if (s.indexOf('O')!=-1) return 4;
    return -1;
  }
  public static String tipoDocumentoToString(byte tipoDocumento) {
    switch (tipoDocumento) {
      case 0: return "C";
      case 1: return "T";
      case 2: return "E";
      case 3: return "R";
      case 4: return "O";
    }
    return "";
  }
  public static byte leerSexo(String s) {
    s=s.toUpperCase().trim();
    if (s.indexOf('M')!=-1) return 0;
    if (s.indexOf('F')!=-1) return 1;
    return -1;
  }
  public static String sexoToString(byte sexo) {
    switch (sexo) {
      case 0: return "M";
      case 1: return "F";
    }
    return "";
  }
  public static String leerNumeroNatural(String s) {
    return codifNumeros.limpiarString(s.replace('O','0').replace('l','1'));   // A muchos les encanta la "ele" en vez de 1 y la "o" en vez del cero
  }
  public static byte leerTipoApoyoIcetex(String s) {
    if (s.indexOf('L')!=-1) return 1;  // Largo
    if (s.indexOf('M')!=-1) return 2;  // Mediano
    if (s.indexOf('A')!=-1) return 3;  // ACCES
    return 0;
  }
  public static String tipoApoyoIcetexToString(byte tipoApoyo) {
    if (tipoApoyo==1) return "L";
    if (tipoApoyo==2) return "M";
    if (tipoApoyo==3) return "A";
    return "";
  }
  // COMPARADORES LEXICOGRÁFICOS DE ARREGLOS DE BYTES
  public static final Comparator<byte[]> comparadorByteArray=new Comparator<byte[]>() { // Comparador Lexicográfico (ordenado por prefijos)
    public int compare(byte[] a1, byte[] a2) {
      int t1=a1.length,t2=a2.length;
      while (t1>0 && a1[t1-1]==(byte)254) t1--;
      while (t2>0 && a2[t2-1]==(byte)254) t2--;
      for (int i=0,t=Math.min(t1,t2); i<t; i++) if (a1[i]!=a2[i]) return (a1[i]&0xFF)-(a2[i]&0xFF);
      return t1-t2;
    }
  };
  public static final Comparator<byte[]> comparadorByteArrayRev=new Comparator<byte[]>() { // Comparador Lexicográfico Reverso (ordenado por sufijos)
    public int compare(byte[] a1, byte[] a2) {
      int t1=a1.length,t2=a2.length;
      while (t1>0 && a1[a1.length-t1]==(byte)254) t1--;
      while (t2>0 && a2[a2.length-t2]==(byte)254) t2--;
      for (int i=0,t=Math.min(t1,t2); i<t; i++) if (a1[a1.length-1-i]!=a2[a2.length-1-i]) return (a1[a1.length-1-i]&0xFF)-(a2[a2.length-1-i]&0xFF);
      return t1-t2;
    }
  };
  // CODIFICADORES DE CADENAS DE TEXTO
  public static final CodificadorBytes codifLetras=new CodificadorBytes() {
    private char reemplazos[]=new char[65536]; {
      for (int i=0; i<65536; i++) reemplazos[i]=(char)i;
      for (String s:new String[]{"ÁA","ÀA","ÄA","ÉE","ÈE","ËE","ÍI","ÌI","ÏI","ÓO","ÒO","ÖO","ÚU","ÙU","ÜU","ÇC","\uFFFDÑ","?Ñ","#Ñ","~Ñ","ÐÑ","´ ","` ","' ",". ",", ","+ ","- ","* ","_ ","| ","^ "}) reemplazos[s.charAt(0)]=s.charAt(1);
    }
    public int numCodigos() {return 28;}
    public int getCodigo(byte b) {return (b>='A' && b<='Z')?(b-'A'):((b==(byte)'Ñ')?26:((b==' ')?27:-1));}
    public byte getCharacter(int c) {return (byte)((c>=0 && c<26)?('A'+c):(c==26?'Ñ':(c==27?' ':0)));}
    public String limpiarString(String s) {
      char r[]=s.toUpperCase().trim().replaceAll("/II$|/I$","").toCharArray();
      for (int i=0,t=r.length; i<t; i++) r[i]=reemplazos[r[i]];
      return new String(r).replaceAll(" +"," ").trim().replaceAll("[^A-ZÑ ]","");
    }
  };
  public static final CodificadorBytes codifLetrasServer=new CodificadorBytes() {
    private char reemplazos[]=new char[65536]; {
      for (int i=0; i<65536; i++) reemplazos[i]=(char)i;
      for (String s:new String[]{"ÁA","ÀA","ÄA","ÉE","ÈE","ËE","ÍI","ÌI","ÏI","ÓO","ÒO","ÖO","ÚU","ÙU","ÜU","ÇC","\uFFFDÑ","?Ñ","#Ñ","~Ñ","ÐÑ","´ ","` ","' ",". ",", ","+ ","- ","* ","_ ","| ","^ "}) reemplazos[s.charAt(0)]=s.charAt(1);
    }
    public int numCodigos() {return 26;}
    public int getCodigo(byte b) {return (b>='A' && b<='Z')?(b-'A'):-1;}
    public byte getCharacter(int c) {return (byte)((c>=0 && c<26)?('A'+c):0);}
    public String limpiarString(String s) {
      char r[]=s.toUpperCase().trim().replaceAll("/II$|/I$","").toCharArray();
      for (int i=0,t=r.length; i<t; i++) r[i]=reemplazos[r[i]];
      return new String(r).replaceAll("^|$|( +)","  ").replaceAll(" (DE|LA|LAS) ","").replace('Ñ','N').replaceAll("[^A-Z]","");
    }
  };
  public static final CodificadorBytes codifNombreProgramas=new CodificadorBytes() {
    private char reemplazos[]=new char[65536]; {
      for (int i=0; i<65536; i++) reemplazos[i]=(char)i;
      for (String s:new String[]{"ÁA","ÇA","ËA","ÉE","\u0191E","ÍI","ÊI","ÓO","ÎO","ÚU","ÒU","„Ñ"}) reemplazos[s.charAt(0)]=s.charAt(1);
    }
    public int numCodigos() {return 38;}
    public int getCodigo(byte b) {
      if (b>='A' && b<='Z') return (b-'A');
      if (b==(byte)'Ñ') return 26;
      if (b>='0' && b<='9') return (b-'0'+27);
      if (b==' ') return 37;
      return -1;
    }
    public byte getCharacter(int c) {
      if (c>=0 && c<26) return (byte)('A'+c);
      if (c==26) return (byte)'Ñ';
      if (c>=27 && c<37) return (byte)('0'+c-27);
      if (c==37) return (byte)' ';
      return -1;
    }
    public String limpiarString(String s) {
      char r[]=s.toUpperCase().trim().toCharArray();
      for (int i=0,t=r.length; i<t; i++) r[i]=reemplazos[r[i]];
      return new String(r).replaceAll("[^A-ZÑ0-9 ]","").replaceAll(" +"," ").trim();
    }
  };
  public static final CodificadorBytes codifNumeros=new CodificadorBytes() {
    public int numCodigos() {return 10;}
    public int getCodigo(byte b) {return (b>='0' && b<='9')?(b-'0'):-1;}
    public byte getCharacter(int c) {return (byte)((c>=0 && c<=9)?('0'+c):0);}
    public String limpiarString(String s) {return s.replaceAll("\\D","");}
  };
  // RUTINAS DE ENTRADA/SALIDA CON SOCKETS
  public static <T> T readObject(ObjectInputStream is, ObjectOutputStream os, Class<T> clase) throws MyException {
    try {
      Object obj=is.readObject();
      if (obj==null) return null;
      if (!clase.isInstance(obj)) throw (obj instanceof MyException)?((MyException)obj):new Exception("");
      return clase.cast(obj);
    }
    catch (Throwable th1) {
      th1.printStackTrace();
      MyException ex=(th1 instanceof MyException)?((MyException)th1):new MyException("Hubo un error en la comunicación con el servidor \""+Constantes.ipServidorSPADIES+"\".",th1);
      try {
        os.writeObject(ex);
        os.flush();
      }
      catch (Throwable th2) {
      }
      throw ex;
    }
  }
  //TODO necesario en algun otro lado?
  public static String md5(String str) {
    byte[] val = fabricarLlave128bits(str);
    StringBuffer res = new StringBuffer();
    for (byte b:val) {
      int valB = b&0xFF;
      if (valB<16) res.append("0");
      res.append(Integer.toHexString(valB));
    }
    return res.toString();
  }
  public static final DecimalFormat df_decimal = new DecimalFormat("0.0000");
  public static final DecimalFormat df_porcentaje = new DecimalFormat("0.00%");
  public static final DecimalFormat df_entero = new DecimalFormat("###,###");
  private static Runtime rt = Runtime.getRuntime();
  public static String usoMemoria() {
    long t = rt.totalMemory();
    return (int)Math.ceil((t-rt.freeMemory())/1024/1024)+"MB usados de "+(int)Math.ceil(t/1024/1024)+"MB";
  }
}
