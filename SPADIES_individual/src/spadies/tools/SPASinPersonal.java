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

import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.util.Constantes;

/**
 * Exporta a un formato de CSV usado en la sincronizacion,
 * la información contenida en los archivos en la carpeta datos. 
 * Los archivos CSV son escritos en una carpeta salidaCSV en la ruta de ejecución
 */
public class SPASinPersonal {
  public static void main(String[] args) throws Exception {
    Constantes.cargarArchivoFiltroIES();
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,false);
    File carSal = new File("datosNP");
    if (args.length==1) carSal = new File(args[0]);
    if(!carSal.exists()) carSal.mkdirs();
    for (IES ies:kernel.listaIES)
      ies.guardar(carSal, ies, false);
  }
}