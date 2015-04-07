package spadies.auxiliar;

import java.io.*;
import java.util.*;

import spadies.io.MyDataInputStream;
import spadies.io.MyDataOutputStream;
import spadies.kernel.*;
import spadies.server.kernel.*;
import spadies.server.kernel.PreparadorDatos.ArchivoCSV;
import spadies.util.*;
import spadies.util.variables.*;
import static spadies.util.CajaDeHerramientas.*;

public class PrincipalMatchGenerico {
  private static long tInic;
  public static int[][] stringToCols(String defNom, String defId) {
    String[] spn = defNom.split(",");
    int [] pn = new int[spn.length];
    {
      int i = 0;
      for (String s:spn) pn[i++] = Integer.parseInt(s);
    }
    String[] spi = defId.split(",");
    int [] pi = new int[spi.length];
    {
      int i = 0;
      for (String s:spi) pi[i++] = Integer.parseInt(s);
    }
    return new int[][]{pn,pi};
  }
  public static int stringToColF(String str) {
    return (str==null||str.length()==0)?-1:Integer.parseInt(str);
  }
  public static void main(String[] args) throws Exception {
    Properties prop = new Properties();
    prop.load(new FileInputStream(args[0]));
    /*
    final File f1 =new File("datosNoSPA/h04_2.csv");
    final File f2 =new File("datosNoSPA/w_a303_h1b_2.csv");
    final File fRes = new File("resultado.csv");
    final File f2Pro = new File(f2.getParentFile(),f2.getName()+".procesado"),
         f2Ind = new File(f2.getParentFile(),f2.getName()+".indcsv");
    final int f1_cols[][] = new int[][]{{6,7,5},{15}};
    final int f2_cols[][] = new int[][]{{7,8,5,6},{38}};
    */
    System.out.println(prop.keySet());
    final File f1 = new File(prop.getProperty("f1"));//new File("_ICFES.csv");
    final File f2 = new File(prop.getProperty("f2"));//new File("datosNoSPA/sisben.csv");
    final File fRes = new File(prop.getProperty("fout"));//new File("resultadoIcfesSIS.csv");
    final File f2Pro = new File(prop.getProperty("f2.pro")), //new File("datosNoSPA/sisben.procesado"),
         f2Ind = new File(prop.getProperty("f2.ind"));//new File("datosNoSPA/sisben.ind");
    final int f1_cols[][] = stringToCols(prop.getProperty("f1.cols.nom"), prop.getProperty("f1.cols.id"));//new int[][]{{5},{4}};
    final int f2_cols[][] = stringToCols(prop.getProperty("f2.cols.nom"), prop.getProperty("f2.cols.id"));
    final double umbral = Double.parseDouble(prop.getProperty("umbral"));
    //stringToColF(prop.getProperty("f2.cols.nac.modo"));
    int col_fec_m = stringToColF(prop.getProperty("f1.cols.nac.m"));
    int col_fec_d = stringToColF(prop.getProperty("f1.cols.nac.d"));
    int col_fec_y = stringToColF(prop.getProperty("f1.cols.nac.y"));
    //final int f2_cols[][] = new int[][]{{/*7,8,5,6*/},{/*38*/}};
    boolean fullouter = Boolean.parseBoolean("fullouter");
    /*{
      BufferedReader br=new BufferedReader(new FileReader(f2)); String sL=br.readLine(); int h=0;
      for (; (sL=br.readLine())!=null; h++) {
        String wL[]=csvToString(sL,0,';');
        System.out.println(wL.length);
      }
      System.exit(1);
    }*/
    FileOutputStream fos = new FileOutputStream(new File("bla"));
    tInic = System.currentTimeMillis();
    {
      ArchivoCSV arf2 = new ArchivoCSV() {
        int conteo = 0;
        public String getTitulo() {return f2.getName();}
        public File getIn() {return f2;}
        public File getOut() {return f2Pro;}
        public String[] getIdLinea(String[] w) {
          //if (w.length<82) return new String[]{"",""};
          StringBuilder nom = new StringBuilder(), doc=new StringBuilder();
          boolean p1=false,p2=false;
          for(int num:f2_cols[0]) if(!w[num].trim().equals("NT")) {
            if (!p1) p1=true;
            else nom.append(" ");
            nom.append(w[num]);
          }
          for(int num:f2_cols[1]) if(!w[num].trim().equals("NT")) {
            if (!p2) p2=true;
            else doc.append(" ");
            doc.append(w[num]);
          }
          return new String[]{nom.toString(),doc.toString()};
        }
        public byte[] getDatoLinea(String[] w) {
          MyByteSequence mbs = new MyByteSequence(4);
          mbs.setInt(0, conteo++);
          return mbs.getBytes();
        }
      };
      PreparadorDatos pd = PreparadorDatos.getInstance();
      try {
        pd.prepararArchivoBase(arf2);
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Error fatal preparando");
        System.exit(1);
      }
      System.out.println("Indexando f2");
      if (!f2Ind.exists()) {
        System.out.println("Y");
        if (f2.length()>Integer.MAX_VALUE)
          indexarCSV_long(f2, f2Ind);
        else
          indexarCSV(f2, f2Ind);         
      }
      int regs = registrosEnArchivo(f1);;
      System.out.println("Registros f1: "+regs);
      byte[][][] INFO_IES = new byte[3][regs][];
      {
        BufferedReader br = new BufferedReader(new FileReader(f1));
        br.readLine();
        int i = 0;
        for (String s = br.readLine();s!=null;s = br.readLine()) {
          String sL[] = csvToString(s,0,';');
          StringBuilder nom = new StringBuilder(),doc = new StringBuilder();
          for(int num:f1_cols[0]) {nom.append(sL[num]); nom.append(" ");}
          for(int num:f1_cols[1]) {doc.append(sL[num]); doc.append(" ");}
          INFO_IES[0][i]=codifLetrasServer.getCodigos(nom.toString());
          INFO_IES[1][i]=codifNumeros.getCodigos(doc.toString());
          if (col_fec_y==-1)
            INFO_IES[2][i]=codifNumeros.getCodigos("");
          else {
            try {
              int d = Integer.parseInt(sL[col_fec_d]),
                m = Integer.parseInt(sL[col_fec_m]),
                y = Integer.parseInt(sL[col_fec_y]);
              INFO_IES[2][i]=codifNumeros.getCodigos((y!=-1 && m!=-1 && d!=-1)?(intToString(y%100,2)+intToString(m,2)+intToString(d,2)):"");
            } catch (Exception e) {
              INFO_IES[2][i]=codifNumeros.getCodigos("");
            }
          }
          /*{
            int d = Integer.parseInt(sL[13]),
              m = Integer.parseInt(sL[14]),
              y = Integer.parseInt(sL[15]);
            System.out.println((d==99||d==88||m==99||m==88||y==9999||y==8888)?"":(CajaDeHerramientas.intToString(y%100, 2)+CajaDeHerramientas.intToString(m, 2)+CajaDeHerramientas.intToString(d, 2)));
            INFO_IES[2][i]=codifNumeros.getCodigos((d==99||d==88||m==99||m==88||y==9999||y==8888)?"":(CajaDeHerramientas.intToString(y%100, 2)+CajaDeHerramientas.intToString(m, 2)+CajaDeHerramientas.intToString(d, 2)));//TODO Particularizado
          }*/
          ++i;
        }
        br.close();
      }
      System.out.println("f2: Realizando Match");
      //MatcherSPADIES m2 = new MatcherSPADIES(INFO_IES,97.99,true,arf2.getOut(),new MyDataOutputStream(fos));
      MatcherSPADIES m2 = new MatcherSPADIES(INFO_IES,/*97.99*/umbral,true,arf2.getOut(),new MyDataOutputStream(fos));
      byte[][] res2 = m2.procesar(m2.total, null);
      System.out.println("f2: Match Completado");
      System.out.println("Cargando Indices");
      //long[] ind2 = cargarIndiceCSV(f2Ind);
      //TODO eleccion int/long
      int[] ind2 = cargarIndiceCSV_int(f2Ind);
      boolean [] visitados2 = new boolean[ind2.length];
      Arrays.fill(visitados2, false);
      {//Guardado resultado
        PrintStream ps = new PrintStream(fRes);
        RandomAccessFile raf2 = new RandomAccessFile(f2, "r");
        raf2.seek(0);
        String[] enc2 = splitCSV(raf2.readLine());
        BufferedReader br = new BufferedReader(new FileReader(f1));
        String[] enc1 = splitCSV(br.readLine());
        ps.println(stringToCSV(enc1) + stringToCSV(enc2));
        String[] dat1v = new String[enc1.length],
          dat2v = new String[enc2.length];
        Arrays.fill(dat1v, "");Arrays.fill(dat2v, "");
        for (int i=0, it = INFO_IES[0].length;i < it;i++) {
          String[] dat1 = splitCSV(br.readLine()),
            dat2 = null;
          if (res2[i]==null) {
            dat2 = dat2v;
          } else {
            int ind = new MyByteSequence(res2[i]).getInt(0);
            visitados2[ind] = true;
            raf2.seek(ind2[ind]);
            dat2 = splitCSV(raf2.readLine());
          }
          ps.println(stringToCSV(dat1) + stringToCSV(dat2));
        }
        br.close();
        if (fullouter)
          for (int i=0, it = ind2.length;i < it;i++) {
            if (visitados2[i]) continue;
            raf2.seek(ind2[i]);
            String [] dat2 = splitCSV(raf2.readLine());
            ps.println(stringToCSV(dat1v) + stringToCSV(dat2));
          }
        raf2.close();
        ps.close();
      }
    }
    fos.close();
  }

  public static final String formateoCampoNum(int num) {
    return num==-1?"":String.valueOf(num);
  }
  
  public static final String formateoCampoNum(double num) {
    return num==-1.0?"":String.valueOf(num);
  }
  /**
   * Asume que hay encabezado, el primer registro es el 0
   * @param archivoCSV
   * @param fOut
   * @throws IOException
   */
  public static void indexarCSV(File archivoCSV, File fOut) throws IOException {
    System.out.println("Indexando int");
    FileReader fr = new FileReader(archivoCSV);
    List<Integer> posic = new LinkedList<Integer>();
    boolean proxf = false;
    int numc = 0;
    for (int car = fr.read();car!=-1;car = fr.read()) {
      if (car == 10 || car == 13) proxf = true;
      else if (proxf) {
        posic.add(numc);
        proxf = false;
      }
      ++numc;
    }
    fr.close();
    {
      MyDataOutputStream mdo = new MyDataOutputStream(new FileOutputStream(fOut));
      mdo.writeInt(posic.size());
      for (Integer val: posic) mdo.writeInt(val);
      mdo.close();
    }
  }
  public static void indexarCSV_long(File archivoCSV, File fOut) throws IOException {
    System.out.println("Indexando long");
    FileReader fr = new FileReader(archivoCSV);
    List<Long> posic = new LinkedList<Long>();
    boolean proxf = false;
    long numc = 0;
    for (int car = fr.read();car!=-1;car = fr.read()) {
      if (car == 10 || car == 13) proxf = true;
      else if (proxf) {
        posic.add(numc);
        proxf = false;
      }
      ++numc;
    }
    fr.close();
    {
      MyDataOutputStream mdo = new MyDataOutputStream(new FileOutputStream(fOut));
      mdo.writeInt(posic.size());
      for (Long val: posic) mdo.writeLong(val);
      mdo.close();
    }
  }
  
  public static int[] cargarIndiceCSV_int(File fIn) throws IOException {
    MyDataInputStream mdis = new MyDataInputStream(new FileInputStream(fIn));
    int tam = mdis.readInt();
    int[] res = new int[tam];
    for (int i = 0;i < tam;i++) res[i] = mdis.readInt();
    return res;
  }
  public static long[] cargarIndiceCSV(File fIn) throws IOException {
    MyDataInputStream mdis = new MyDataInputStream(new FileInputStream(fIn));
    int tam = mdis.readInt();
    long[] res = new long[tam];
    for (int i = 0;i < tam;i++) res[i] = mdis.readLong();
    return res;
  }
  
  public static String[] splitCSV(String s) {
    List<String> p=new LinkedList<String>();
    for (int i=0,t=s.length(); i<t; ) {
      if (s.charAt(i)==';') {
        p.add("");
        i++;
      }
      else if (s.charAt(i)!='"') {
        int j=s.indexOf(';',i+1);
        if (j==-1) j=t;
        p.add(s.substring(i,j));
        i=j+1;
      }
      else {
        int j=s.indexOf('"',i+1);
        while (j!=-1 && j<t-1 && s.charAt(j+1)=='"') j=s.indexOf('"',j+2);
        p.add(s.substring(i+1,j).replaceAll("\"\"","\""));
        j=s.indexOf(';',j+1);
        if (j==-1) j=t;
        i=j+1;
      }
      if (i==t) p.add("");
    }
    return p.toArray(new String[0]);
  }
  
  public static void impresionT(String msg) {
    System.out.println(formatoT(System.currentTimeMillis()-tInic) + " " + msg);
  }
  public static String formatoT(long tO) {
    int t = (int) (tO/1000);
    int s = t%60,
      m = (t/60)%60,
      h = t/(60*60);
    String stS = String.valueOf(s),
      stM = String.valueOf(m),
      stH = String.valueOf(h);
    if (stS.length()==1) stS = "0"+stS;
    if (stM.length()==1) stM = "0"+stM;
    return stH + "h " + stM + "m " + stS + "s ";
  }  
  public static int registrosEnArchivo(File f) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(f));
    br.readLine();//Lectura encabezado
    int conteo = 0;
    while(br.readLine()!=null) conteo++;
    br.close();
    return conteo;
  }
  
  public static String stringMasLargo(String ... strings) {
    int max = Integer.MIN_VALUE;
    for (String s:strings) if (s.length()>max) max = s.length();
    for (String s:strings) if (s.length()==max) return s;
    return null;
  }
}