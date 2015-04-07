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

public final class TablaPasswordsIES {
  private static final TablaPasswordsIES instance=new TablaPasswordsIES();
  private Map<Integer,String> passwords=new TreeMap<Integer,String>();
  public static TablaPasswordsIES getInstance() {return instance;}
  private TablaPasswordsIES() {}
  public void preparar() throws Exception {
    long tmi=System.currentTimeMillis();
    System.out.println("LEYENDO LOS PASSWORDS DE LAS IES ("+ConstantesServer.fPASSWORDS_IES.getName()+")");
    passwords = lecturaPasswords();
    System.out.println("  "+ConstantesServer.fPASSWORDS_IES.getName()+" LEIDO EN "+(System.currentTimeMillis()-tmi)+"ms");
  }
  public String getPasswordIES(int codigo) {
    return passwords.get(codigo);
  }
  private static Map<Integer,String>lecturaPasswords() throws NumberFormatException, IOException {
    Map<Integer,String> res = new TreeMap<Integer, String>();
    BufferedReader br=new BufferedReader(new FileReader(ConstantesServer.fPASSWORDS_IES));
    for (String sL=br.readLine(); (sL=br.readLine())!=null; ) {
      String wL[]=csvToString(sL,0,';'),sCodigo=codifNumeros.limpiarString(wL[0]),sPassword=wL[1].trim();
      res.put(Integer.parseInt(sCodigo),sPassword);
    }
    br.close();
    return Collections.unmodifiableMap(res);
  }
}
