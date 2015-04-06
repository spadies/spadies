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

import static spadies.util.CajaDeHerramientas.df_porcentaje;
import static spadies.util.CajaDeHerramientas.stringToInteger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.jfree.data.io.CSV;

import spadies.gui.format.ProcesadorDesercionPorPeriodo;
import spadies.gui.format.ResultadoConsulta;
import spadies.gui.util.InfoTabla;
import spadies.io.MyDataOutputStream;
import spadies.kernel.Estudiante;
import spadies.kernel.Estudiante_DatosPersonales;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.kernel.Programa;
import spadies.kernel.VariableExtra;
import spadies.server.kernel.TablaDepartamentos;
import spadies.server.kernel.TablaIES;
import spadies.server.kernel.TablaProgramas;
import spadies.server.util.OperacionesAdministrativas;
import spadies.util.CajaDeHerramientas;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;

public class AuxiliarCompletarIESProgramas {
  static boolean datosPersonales = false;
  public static void main(String[] args) throws Exception {
    File outDir = new File("datos-remendados");
    outDir.mkdir();
    //TODO Cargar filtros IES
    //TablaProgramas tp = TablaProgramas.getInstance();
    //tp.preparar();
    TablaIES ti = TablaIES.getInstance();
    ti.preparar();
    //KernelSPADIES kernel = KernelSPADIES.getInstance();
    //kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,false);
    System.gc();
    //if (!carpeta.exists() || !carpeta.isDirectory()) throw new MyException("No se encuentra la carpeta \""+carpeta.getPath()+"\".");
    File[] archivos=new File("datos").listFiles();
    Arrays.sort(archivos);
    if (archivos!=null) for (File f:archivos) if (CajaDeHerramientas.esArchivoIESFiltro(f, Constantes.filtroIES)) {
      IES ies=IES.cargar(f,datosPersonales);
      String s=f.getName();
      //Integer cod=CajaDeHerramientas.stringToInteger(s.substring(0,s.indexOf('.')));
      {
        ies.nombre = new byte[0];
        ies.caracter = -1;
        ies.origen = -1;
        ies.departamento = -1;
        ies.municipio = -1;
      }
      byte[][] datos = ti.getDatos(ies.codigo);
      if (datos!=null) {
        ies.nombre=datos[0];
        ies.departamento=(byte)(stringToInteger(new String(datos[1])));
        ies.municipio=(short)(stringToInteger(new String(datos[2])));
        ies.origen=(byte)(stringToInteger(new String(datos[3])));
        ies.caracter=(byte)(stringToInteger(new String(datos[4])));
      }
      IES.guardar(outDir, ies, datosPersonales);
    }
    System.exit(1);
  }
}