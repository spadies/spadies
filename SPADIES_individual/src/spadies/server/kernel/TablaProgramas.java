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
import spadies.util.*;
import spadies.server.util.*;
import static spadies.util.CajaDeHerramientas.*;

public final class TablaProgramas {
  private static final TablaProgramas instance=new TablaProgramas();
  private final Map<byte[],byte[][]> mapaPorConsecutivo=new TreeMap<byte[],byte[][]>(comparadorByteArray);
  private final Map<byte[],byte[][]> mapaPorSNIES=new TreeMap<byte[],byte[][]>(comparadorByteArray);
  private final MyListMap<byte[],byte[][]> mapaPorIES=new MyListMap<byte[],byte[][]>(comparadorByteArray);
  private final MyListMap<byte[],byte[][]> mapaPorArea=new MyListMap<byte[],byte[][]>(comparadorByteArray);
  public static TablaProgramas getInstance() {return instance;}
  private TablaProgramas() {}
  public void preparar() throws Exception {
    long tmi=System.currentTimeMillis();
    System.out.println("LEYENDO LA LISTA DE PROGRAMAS ("+ConstantesServer.fPROGRAMAS.getName()+")");
    BufferedReader br=new BufferedReader(new FileReader(ConstantesServer.fPROGRAMAS));
    for (String sL=br.readLine(); (sL=br.readLine())!=null; ) {
      String wL[]=csvToString(sL,0,';');
      byte[] sConsecutivo=codifNumeros.limpiarString(wL[0]).getBytes(),sNombre=codifNombreProgramas.limpiarString(wL[1]).getBytes(),sProgSNIES=codifNumeros.limpiarString(wL[2]).getBytes(),sIES=codifNumeros.limpiarString(wL[3]).getBytes(),sArea=codifNumeros.limpiarString(wL[4]).getBytes(),sNivel=codifNumeros.limpiarString(wL[5]).getBytes(),sNucleo=codifNumeros.limpiarString(wL[6]).getBytes(),sMetodologia=codifNumeros.limpiarString(wL[7]).getBytes();
      byte[][] valPrograma = new byte[][]{sNombre,sIES,sArea,sNivel,sNucleo,sMetodologia};
      mapaPorConsecutivo.put(sConsecutivo,valPrograma);
      mapaPorSNIES.put(sProgSNIES,valPrograma);
      mapaPorIES.add(sIES,new byte[][]{sNombre,sProgSNIES,sArea,sNivel,sNucleo,sMetodologia});
      mapaPorArea.add(sArea,new byte[][]{sNombre,sProgSNIES,sIES,sNivel,sNucleo,sMetodologia});
    }
    System.out.println("  "+ConstantesServer.fPROGRAMAS.getName()+" LEIDO EN "+(System.currentTimeMillis()-tmi)+"ms");
  }
  public byte[][] getDatosPorConsecutivo(byte[] consecutivo) {
    return mapaPorConsecutivo.get(consecutivo);
  }
  public byte[][] getDatosPorSNIES(byte[] snies) {
    return mapaPorSNIES.get(snies);
  }
  public List<byte[][]> getDatosPorIES(byte[] ies) {
    return mapaPorIES.get(ies);
  }
  public List<byte[][]> getDatosPorArea(byte[] area) {
    return mapaPorArea.get(area);
  }
}
