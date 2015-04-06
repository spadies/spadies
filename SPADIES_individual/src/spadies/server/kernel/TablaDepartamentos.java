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
import static spadies.util.CajaDeHerramientas.*;

public final class TablaDepartamentos {
  private static final TablaDepartamentos instance=new TablaDepartamentos();
  private final Map<Integer,Map<Integer,Double>> tasasDesempleo=new TreeMap<Integer,Map<Integer,Double>>();
  public static TablaDepartamentos getInstance() {return instance;}
  private TablaDepartamentos() {}
  public void preparar() throws Exception {
    long tmi=System.currentTimeMillis();
    System.out.println("LEYENDO LA LISTA DE TASAS DE DESEMPLEO DEPARTAMENTAL ("+ConstantesServer.fDEPARTAMENTOS_DESEMPLEO.getName()+")");
    BufferedReader br=new BufferedReader(new FileReader(ConstantesServer.fDEPARTAMENTOS_DESEMPLEO));
    for (String sL=br.readLine(); (sL=br.readLine())!=null; ) {
      String wL[]=csvToString(sL,0,';'),sCodigo=wL[0].trim(),sAnho=wL[1].trim(),sTasaDesempleo=wL[2].trim().replace(',','.');
      int codigo=Integer.parseInt(sCodigo);
      Map<Integer,Double> map=tasasDesempleo.get(codigo);
      if (map==null) tasasDesempleo.put(codigo,map=new TreeMap<Integer,Double>());
      map.put(Integer.parseInt(sAnho),Double.parseDouble(sTasaDesempleo));
    }
    System.out.println("  "+ConstantesServer.fDEPARTAMENTOS_DESEMPLEO.getName()+" LEIDO EN "+(System.currentTimeMillis()-tmi)+"ms");
  }
  public Map<Integer,Double> getTasasDesempleoPorAnho(int codigo) {
    return tasasDesempleo.get(codigo);
  }
}
