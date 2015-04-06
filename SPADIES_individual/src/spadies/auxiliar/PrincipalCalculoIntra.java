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
package spadies.auxiliar;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import spadies.io.MyDataInputStream;
import spadies.io.MyDataOutputStream;
import spadies.kernel.*;
import spadies.server.kernel.*;
import spadies.server.kernel.PreparadorDatos.ArchivoCSV;
import spadies.util.*;
import spadies.util.variables.*;
import static spadies.util.CajaDeHerramientas.*;

public class PrincipalCalculoIntra {
  private static final KernelSPADIES kernel=KernelSPADIES.getInstance();
  private static final DateFormat df=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  private static final int puerto=Constantes.puertoServidorConsultas;
  private static final long periodoActualizacion=1000L*60*30;  // 30 minutos
  private static final long t0=System.currentTimeMillis();
  public static void main(String[] args) throws IOException {
    final File csvIndividuosIes = new File("expIES.csv");
    final File proIndividuosIes = new File("pr/indivIES.procesado");
    final File genMatchIndividuosIes = new File("pr/salidaMatchInterno.bin");
    proIndividuosIes.delete();
    ArchivoCSV arInter=
        new ArchivoCSV() {
          public String getTitulo() {return "INDIVIDUOS_SPADIES";}
          public File getIn() {return csvIndividuosIes;}
          public File getOut() {return proIndividuosIes;}
          public String[] getIdLinea(String[] w) {if (w.length<3) System.out.println(stringToCSV(w)+"~"); return new String[]{w[2],w[3]};}
          public byte[] getDatoLinea(String[] w) {return prepararSalidaDesercionInter(w);}
        };
    try {
      kernel.cargarParaServidor(Constantes.carpetaDatos,periodoActualizacion, true);
    }
    catch (MyException ex) {
      System.out.println(ex.getMessage());
    }
    catch (Throwable th) {
      th.printStackTrace(System.out);
    }
    /*
    if (!csvIndividuosIes.exists()) {
      escrituraArchivoIndividuosIes(csvIndividuosIes);
    }
    */
    escrituraArchivoIndividuosIes(csvIndividuosIes);
    {//PREPARACION ARCHIVO INDIVIDUOS
        arInter.getOut().delete();
        {
          PreparadorDatos pd = PreparadorDatos.getInstance();
          try {
            pd.prepararArchivoBase(arInter);
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
    }
    /*if (!genMatchIndividuosIes.exists())*/{//MATCH
      MatcherSPADIESIntra mi;
      try {
        //mi = new MatcherSPADIESIntra(97.99,INFO_IES);
        mi = new MatcherSPADIESIntra(97.99,proIndividuosIes);
        mi.procesar(genMatchIndividuosIes);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    int arrRef[][] = null;
    {
      MyDataInputStream mi = new MyDataInputStream(new FileInputStream(genMatchIndividuosIes));
      try {
        int tamT = mi.readInt();
        System.out.println(tamT);
        System.out.println();
        arrRef = new int[tamT][];
        for (int i=0;i<tamT;i++) {
          int tam = mi.readInt();
          arrRef[i] = new int[tam];
          for (int j=0;j<tam;j++) arrRef[i][j] = mi.readInt();
          System.out.print(i+"\r");
        }
      } catch (Exception e) {
        System.err.println();
        e.printStackTrace();
      } finally {
        System.out.println();
        System.out.println("LEIDOS:" + mi.getNumeroBytesLeidos());
      }
      mi.close();
    }
    int [][] refs = null; 
    {
      MyDataInputStream is1=new MyDataInputStream(new FileInputStream(proIndividuosIes));
      int tam = is1.readInt();
      refs = new int[tam][];
      for (int i=0;i<tam;i++) {
        is1.readByteArray(true,-1);is1.readByteArray(true,-1);
        MyByteSequence mbs = new MyByteSequence(is1.readByteArray(true,-1));
        refs[i]= new int[]{mbs.getInt(0), mbs.getInt(4)};
      }
      is1.close();
    }
    {
      for (int [] ind: arrRef) {
        SortedMap<Integer, int[]> vid = new TreeMap<Integer, int[]>();
        int t= ind.length;
        for (int i=0;i<t;i++) {
          IES ies = kernel.mapIESporCodigo.get(refs[ind[i]][0]);
          Estudiante est = ies.estudiantes[refs[ind[i]][1]];
          int ult = ies.minCodigoSemestre + est.getUltimoSemestreMatriculado();
          vid.put(ult*100+i, refs[ind[i]]);
        }
        int[][] vida = vid.values().toArray(new int[][]{});
        for (int i=0,tam=vida.length;i<tam;i++) {
          IES ies = kernel.mapIESporCodigo.get(vida[i][0]);
          Estudiante est = ies.estudiantes[vida[i][1]];
          int esta = -1;
          /*
          -1 desSis
          -2 desIes
          -3 desPro
          */
          if (est.getEstado()==-1) {
            for (int j=i+1;j<tam;j++) {
              if (vida[i][0]==vida[j][0]) {
                esta = -3;
                break;
              } else esta = -2; 
            }
          }
          est.setEstadoDesercion((byte) esta);
        }
      }
      try {
        for (IES ies: kernel.listaIES) IES.guardar(new File("alter"), ies, true);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  private static final Map<String,Integer> mapaTipoCotizante = new TreeMap<String,Integer>();
  static {
    mapaTipoCotizante.put("APRENDICES DEL SENA",1);
    mapaTipoCotizante.put("DEPENDIENTE" , 2);
    mapaTipoCotizante.put("DESEMPLADO CON SUBSIDIO DE CCF" ,3);
    mapaTipoCotizante.put("EMPLEADA DOMESTICA" ,4);
    mapaTipoCotizante.put("EX EMPLEADO PUBLICO VINCULADO CON EMPLEADOR PRIVADO" ,5);
    mapaTipoCotizante.put("INDEPENDIENTE" ,6);
    mapaTipoCotizante.put("INDEPENDIENTE AGREMIADO" ,7);
    mapaTipoCotizante.put("MADRE COMUNITARIA CON COBERTURA FAMILIAR" ,8);
    mapaTipoCotizante.put("MADRE COMUNITARIA SIN COBERTURA FAMILIAR" ,9);
    mapaTipoCotizante.put("PENSIONADO" ,10);
    mapaTipoCotizante.put("PENSIONADO POR SUSTITUCION",11);
  }
  private static byte[] prepararSalidaObservatorio(String[] w) {
    MyByteSequence res=new MyByteSequence(9+8*4+1);
    int pos = 0;
    res.setInt(pos, stringToInteger(leerNumeroNatural(w[2].trim()+w[31].trim())));pos+=4;//AnoFin+Gra_Semestre
    {
      String[] spf = w[17].split("-");
      res.setByte(pos, (byte) stringToInteger(leerNumeroNatural(spf[2].trim())));pos+=1;
      res.setByte(pos, (byte) stringToInteger(leerNumeroNatural(spf[1].trim())));pos+=1;
      res.setInt(pos, stringToInteger(leerNumeroNatural(spf[0].trim())));pos+=4;
    }
    res.setByte(pos, (byte) stringToInteger(leerNumeroNatural(w[25].trim())));pos+=1;
    res.setByte(pos, (byte) stringToInteger(leerNumeroNatural(w[26].trim())));pos+=1;
    res.setByte(pos, (byte) stringToInteger(leerNumeroNatural(w[27].trim())));pos+=1;
    res.setByte(pos, (byte) stringToInteger(leerNumeroNatural(w[28].trim())));pos+=1;
    {
      Integer tco = mapaTipoCotizante.get(w[32].trim());
      res.setByte(pos, (byte) (tco==null?0:tco));pos+=1;
    }
    res.setInt(pos, stringToInteger(leerNumeroNatural(w[35].replace(".00", "").trim())));pos+=4;
    res.setInt(pos, stringToInteger(leerNumeroNatural(w[36].replace(".00", "").trim())));pos+=4;
    res.setByte(pos, (byte) stringToInteger(leerNumeroNatural(w[37].trim())));pos+=1;
    res.setInt(pos, stringToInteger(leerNumeroNatural(w[38].trim())));pos+=4;
    res.setByte(pos, (byte) stringToInteger(leerNumeroNatural(w[40].trim())));pos+=1;
    res.setInt(pos, stringToInteger(leerNumeroNatural(w[42].trim())));pos+=4;
    res.setInt(pos, stringToInteger(leerNumeroNatural(w[43].trim())));pos+=4;
    
    res.setInt(pos, stringToInteger(leerNumeroNatural(w[2].trim())));pos+=4;//AnoFin
    res.setBoolean(pos, 0, w[58].toUpperCase().equals("S"));
    res.setBoolean(pos, 1, w[59].toUpperCase().equals("S"));
    return res.getBytes();
  }

  private static byte[] prepararSalidaEcaes(String[] w) {
    MyByteSequence res=new MyByteSequence(2);
    //res.setByte(0,getCodigoSemestre(w[6].trim()+w[7].trim()));
    //res.setInt(1,stringToInteger(leerNumeroNatural(w[4].trim())));
    res.setByte(0, (byte) stringToInteger(leerNumeroNatural(w[4].trim())));
    res.setByte(1, (byte) (int) stringToDouble(w[3].trim().replace(',', '.')));
    return res.getBytes();
  }
  
  public static class ArchivoCSVObservatorio implements ArchivoCSV {
    private String ies;
    public ArchivoCSVObservatorio(String ies) {this.ies = ies;}
    public String getTitulo() {return "OBSERVATORIO "+ies;}
    public File getIn() {return new File("split/obs/graobs" + ies + ".csv");}
    public File getOut() {return new File("pr/obs/graobs" + ies + ".procesado");}
    public String[] getIdLinea(String[] w) {return new String[]{w[49]+" "+w[51]+" "+w[52]+" "+w[53],w[0]};}
    public byte[] getDatoLinea(String[] w) {return prepararSalidaObservatorio(w);}
  }
  public static class ArchivoCSVECAES implements ArchivoCSV {
    private String ies;
    public ArchivoCSVECAES(String ies) {this.ies = ies;}
    public String getTitulo() {return "ECAES "+ies;}
    public File getIn() {return new File("split/ek/ek" + ies + "_simple.csv");}
    public File getOut() {return new File("pr/ek/ek" + ies + ".procesado");}
    public String[] getIdLinea(String[] w) {return new String[]{w[1],w[0]};}
    public byte[] getDatoLinea(String[] w) {return prepararSalidaEcaes(w);}
  }
  
  public static final String formateoCampoNum(int num) {
    return num==-1?"":String.valueOf(num);
  }
  
  public static final String formateoCampoNum(double num) {
    return num==-1.0?"":String.valueOf(num);
  }

  private static byte[] prepararSalidaDesercionInter(String[] w) {
    MyByteSequence res=new MyByteSequence(2*4);
    int pos = 0;
    res.setInt(pos, stringToInteger(leerNumeroNatural(w[0].trim())));pos+=4;
    res.setInt(pos, stringToInteger(leerNumeroNatural(w[1].trim())));pos+=4;
    return res.getBytes();
  }
  
  public static void escrituraArchivoIndividuosIes(File csvSalida) throws FileNotFoundException     {
    PrintStream ps = new PrintStream(csvSalida);
    String[] encabezado = new String[]{};
    ps.println(CajaDeHerramientas.stringToCSV(encabezado));
    for (IES ies: kernel.listaIES) {
      //System.out.println("Preparado " + ies.codigo);
      //System.out.println((System.currentTimeMillis()-tm)/1000);
      int ne= ies.estudiantes.length;
      int ie = 0;
      for (int i=0;i<ne;i++) {
        Estudiante_DatosPersonales edp = ies.datosPersonalesEstudiantes==null?null:ies.datosPersonalesEstudiantes[i];
        String doc = edp.documento==-1?"":String.valueOf(edp.documento),
            nombre = (new String(edp.apellido) + " " + new String(edp.nombre)).trim(),
            fec = (edp.diaFechaNacimiento==-1?"":edp.diaFechaNacimiento)+"/"+(edp.mesFechaNacimiento==-1?"":edp.mesFechaNacimiento)+"/"+(edp.anhoFechaNacimiento==-1?"":edp.anhoFechaNacimiento); 
        ps.println(CajaDeHerramientas.stringToCSV(""+ies.codigo, ie+"",nombre, doc, fec));
        ++ie;
      }
    }
    ps.close();
  }
}