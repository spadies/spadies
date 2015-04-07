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

import spadies.server.util.*;
import spadies.util.CajaDeHerramientas;
import static spadies.util.CajaDeHerramientas.*;

public final class TablaPeriodosBloqueoIES {
  private static final TablaPeriodosBloqueoIES instance=new TablaPeriodosBloqueoIES();
  private Map<Integer,Byte> pers=new TreeMap<Integer,Byte>();
  public static TablaPeriodosBloqueoIES getInstance() {return instance;}
  private TablaPeriodosBloqueoIES() {}
  public void preparar() throws Exception {
    long tmi=System.currentTimeMillis();
    System.out.println("LEYENDO LOS PERIODOS DE BLOQUEO DE LAS IES ("+ConstantesServer.fPERBLOQUEO_IES.getName()+")");
    pers = lecturaPeriodos();
    System.out.println("  "+ConstantesServer.fPASSWORDS_IES.getName()+" LEIDO EN "+(System.currentTimeMillis()-tmi)+"ms");
  }
  public Integer getPerBloqueoIES(int codigo) {
    Byte b = pers.get(codigo);
    return b==null?null:(0xFF&b);
  }
  private static Map<Integer,Byte> lecturaPeriodos() throws NumberFormatException, IOException {
    Map<Integer,Byte> res = new TreeMap<Integer, Byte>();
    BufferedReader br=new BufferedReader(new FileReader(ConstantesServer.fPERBLOQUEO_IES));
    String enc = br.readLine();//encabezado
    for (String sL=br.readLine(); sL!=null; sL=br.readLine()) {
      String wL[]=csvToString(sL,0,';');
      res.put(Integer.parseInt(wL[0]),CajaDeHerramientas.getCodigoSemestre(wL[1]));
    }
    br.close();
    return Collections.unmodifiableMap(res);
  }
}
