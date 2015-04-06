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

public final class TablaIES {
  private static final TablaIES instance=new TablaIES();
  private final Map<Integer,byte[][]> mapaPorCodigo=new TreeMap<Integer,byte[][]>();
  public static TablaIES getInstance() {return instance;}
  private TablaIES() {}
  public void preparar() throws Exception {
    long tmi=System.currentTimeMillis();
    System.out.println("LEYENDO LA LISTA DE IES ("+ConstantesServer.fIES.getName()+","+ConstantesServer.fIES_ESPECIALES.getName()+")");
    for (File f:new File[]{ConstantesServer.fIES,ConstantesServer.fIES_ESPECIALES}) {
      BufferedReader br=new BufferedReader(new FileReader(f));
      for (String sL=br.readLine(); (sL=br.readLine())!=null; ) {
        String wL[]=csvToString(sL,0,';');
        byte[] sCodigo=codifNumeros.limpiarString(wL[0]).getBytes(),sNombre=wL[1].trim().getBytes(),sCaracter=codifNumeros.limpiarString(wL[2]).getBytes(),sDepartamento=codifNumeros.limpiarString(wL[3]).getBytes(),sMunicipio=codifNumeros.limpiarString(wL[5]).getBytes(),sOrigen=codifNumeros.limpiarString(wL[4]).getBytes();
        mapaPorCodigo.put(Integer.parseInt(new String(sCodigo)),new byte[][]{sNombre,sDepartamento,sMunicipio,sOrigen,sCaracter});
      }
    }
    System.out.println("  "+ConstantesServer.fIES.getName()+","+ConstantesServer.fIES_ESPECIALES.getName()+" LEIDOS EN "+(System.currentTimeMillis()-tmi)+"ms");
  }
  public byte[][] getDatos(int codigo) {
    return mapaPorCodigo.get(codigo);
  }
}
