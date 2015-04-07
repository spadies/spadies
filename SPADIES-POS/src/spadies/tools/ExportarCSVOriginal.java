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
package spadies.tools;

import java.io.File;
import java.io.IOException;

import spadies.kernel.KernelSPADIES;
import spadies.server.util.OperacionesAdministrativas;
import spadies.util.Constantes;
import spadies.util.MyException;

/**
 * Exporta a un formato de CSV usado en la sincronizacion,
 * la información contenida en los archivos en la carpeta datos. 
 * Los archivos CSV son escritos en una carpeta salidaCSV en la ruta de ejecución
 */
public class ExportarCSVOriginal {
  public static void main(String[] args) throws MyException, IOException {
    Constantes.cargarArchivoFiltroIES();
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,true);
    try {
      OperacionesAdministrativas.escribirCSVScompletos(kernel, new File("salidaCSV"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}