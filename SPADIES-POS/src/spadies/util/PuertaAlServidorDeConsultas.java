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
import java.net.*;

public final class PuertaAlServidorDeConsultas {
  public static <T> T obtenerResultadoConsulta(int tipoConsulta, Class<T> claseResultado, Object...args) throws MyException {
    T resultado=null;
    Socket socket=null;
    ObjectOutputStream out=null;
    ObjectInputStream in=null;
    try {
      socket=new Socket(Constantes.ipServidorSPADIES,Constantes.puertoServidorConsultas);
      out=new ObjectOutputStream(socket.getOutputStream());
      in=new ObjectInputStream(socket.getInputStream());
      socket.setSoTimeout(Constantes.timeoutServidorConsultas);
    }
    catch (Throwable th) {
      throw new MyException("No se pudo establecer comunicación con el servidor \""+Constantes.ipServidorSPADIES+"\" por el puerto "+Constantes.puertoServidorConsultas+". Revise la configuración de su firewall para permitir a la aplicación comunicarse con el servidor del Ministerio de Educación Nacional o descargue la nueva versión de la aplicación SPADIES.");
    }
    try {
      out.writeLong(9165465416546L);
      out.flush();
      out.writeInt(tipoConsulta);
      for (Object obj:args) out.writeObject(obj);
      out.flush();
      resultado=CajaDeHerramientas.readObject(in,out,claseResultado);
      if (in.readLong()!=4816547187991L) throw new Exception("");
    }
    catch (MyException ex) {
      throw ex;
    }
    catch (Throwable th) {
      throw new MyException("Hubo un error en la comunicación con el servidor \""+Constantes.ipServidorSPADIES+"\".["+tipoConsulta+"]", th);
    }
    finally {
      try {
        if (out!=null) out.close();
        if (in!=null) in.close();
        socket.close();
      }
      catch (Throwable th) {
      }
    }
    return resultado;
  }
}
