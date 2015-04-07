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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import spadies.kernel.Estudiante;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.util.Constantes;
import spadies.util.MyException;

/**
 * Solicitado por Martha Susana para proyecto del observatorio laboral
 * @author an-cordo
 *
 */
public class VistaProgramasIESGraduados {
  public static void main(String[] args) throws MyException, IOException {
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,false);
    PrintStream ps = new PrintStream(new File("agggga.csv"));
    ps.println("ies_nombre;ies_programa;periodo;p_nombre;p_codigo;p_area;p_nucleo;p_metodologia");
    for (IES ies:kernel.listaIES) {
      int n = ies.n;
      //Conteos [sexo,ingreso,icfes,estado,riesgo]
      int[][] conteosG = new int[ies.programas.length+1][ies.n];
      for (Estudiante e:ies.estudiantes) {
        int indp = e.getIndicePrograma();
        if (indp==-1) indp = ies.programas.length;
        int jG=e.getSemestreGrado();
        if (jG!=-1) conteosG[indp][jG]++;
      }
      //Imprimir resultado 
      for (int i=0,it=conteosG.length,itm=conteosG.length-1;i<it;i++) {
        boolean progNull = i==itm;
        String[] linea = new String[]{
            String.valueOf(ies.codigo),
            new String(ies.nombre),
            null,
            progNull?"":new String(ies.programas[i].nombre),
            progNull?"":new String(ies.programas[i].codigoSNIES),
            progNull?"":String.valueOf(ies.programas[i].area),
            progNull?"":String.valueOf(ies.programas[i].nucleo),
            null
        };
        int ultPos = linea.length-1;
        for (int j=0;j<n;j++) {
          linea[2] = ies.semestres[j];
          linea[ultPos] = String.valueOf(conteosG[i][j]);
          for (String s:linea)
            ps.print(s+";");          
          ps.println();
        }
      }
    }
  }
 
  public static final String formateoCampoNum(int num) {
    return num==-1?"":String.valueOf(num);
  }
  
  public static final String formateoCampoNum(double num) {
    return num==-1.0?"":String.valueOf(num);
  }
  
  public static String[] splitCSV(String s) {
    List<String> p=new LinkedList<String>();
    for (int i=0,t=s.length(); i<t; ) {
      if (s.charAt(i)==';') {
        p.add("");
        i++;
      }
      else if (s.charAt(i)!='"') {
        int j=s.indexOf(';',i+1);
        if (j==-1) j=t;
        p.add(s.substring(i,j));
        i=j+1;
      }
      else {
        int j=s.indexOf('"',i+1);
        while (j!=-1 && j<t-1 && s.charAt(j+1)=='"') j=s.indexOf('"',j+2);
        p.add(s.substring(i+1,j).replaceAll("\"\"","\""));
        j=s.indexOf(';',j+1);
        if (j==-1) j=t;
        i=j+1;
      }
      if (i==t) p.add("");
    }
    return p.toArray(new String[0]);
  }
 
}