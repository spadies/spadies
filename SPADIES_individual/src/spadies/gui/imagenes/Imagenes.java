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
package spadies.gui.imagenes;

import java.net.*;
import javax.swing.*;

public enum Imagenes {
  IM_ICONO_APLICACION("otros/iconoAplicacion.png"),
  IM_MINISTERIO("otros/ministerio.jpg"),
  IM_SIGUIENTE("jlfgr/Forward24.gif"),
  IM_ANTERIOR("jlfgr/Back24.gif"),
  IM_HOME("jlfgr/Home24.gif"),
  IM_ESCUDO_UNIVERSIDAD("otros/_ImEscudoUniversidad.png"),
  IM_ESCUDO_COLOMBIA("otros/_ImEscudoColombia.png"),
  IM_PRESENTACION01("otros/_ImPresentacion01.png"),
  IM_PRESENTACION02("otros/_ImPresentacion02.png"),
  IM_PRESENTACION03("otros/_ImPresentacion03.png"),
  IM_PRESENTACION04("otros/_ImPresentacion04.png");
  private final String nombreImagen;
  private Imagenes(String pNombre) {
    nombreImagen=pNombre;
    URLImagenes.map.put(nombreImagen.substring(nombreImagen.lastIndexOf('/')+1),getURL());
  }
  public URL getURL() {
    return Imagenes.class.getResource(nombreImagen);
  }
  public ImageIcon getImagen() {
    URL urlImagen=getURL();
    return (urlImagen==null)?null:new ImageIcon(urlImagen);
  }
}
