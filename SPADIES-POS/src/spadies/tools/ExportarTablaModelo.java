/*
 * Centro de Estudios sobre Desarrollo Económico (CEDE) - Universidad de los Andes
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

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import spadies.kernel.Estudiante;
import spadies.kernel.Estudiante_DatosPersonales;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.kernel.Programa;
import spadies.util.CajaDeHerramientas;
import spadies.util.Constantes;
import spadies.util.MyException;

public class ExportarTablaModelo {
  public static void main(String[] args) throws MyException, IOException {
    boolean conPersonales = false;
    if (args.length>0 && args[0].equals("personal"))
      conPersonales = true;
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,conPersonales);
    escrituraDatos(conPersonales);
  }
  
  public static final String formateoCampoNum(int num) {
    return num==-1?"":String.valueOf(num);
  }
  
  public static final String formateoCampoNum(double num) {
    return num==-1.0?"":String.valueOf(num);
  }
  
  public static void escrituraDatos(boolean conPersonales) {
    try {
      KernelSPADIES kernel=KernelSPADIES.getInstance();
      String[] encabezadoPersonales = new String[]{"nombre","apellido","doc_tipo","doc_num","nac_dia","nac_mes","nac_ano"}; 
      String[] encabezado = new String[]{
          "ies",
          "numero_estudiante",
          "sexo",
          "edad_icfes",
          "edu_madre_icfes",
          "ing_hogar_icfes",
          "ing_hogar_icfes2",
          "viv_propia_icfes",
          "num_hermanos_icfes",
          "pos_hermanos_icfes",
          "nivel_sisben",
          "estrato",
          "personas_familia",
          "puntaje_icfes",
          "periodo_icfes",
          "remplazo_icfes",
          "trabajaba_icfes",
          "sbp_puntaje",
          "sbp_edad",
          "sbp_area",
          "sbp_edumadre",
          "sbp_ocumadre",
          "sbp_internet",
          "sbp_valmatricula",
          "prim_sem",
          "grado_per",
          "retiro_per",
          "programa_id",
          "areaD","area1","area2","area3","area4","area5","area6","area7","area8","area9",
          "prog_nivel","prog_nucleo",
          "e_graduado","e_retirado","e_activo","e_desertor",
          "segvar",
          "periodo",
          "materias_tomadas",
          "materias_aprobadas",
          "apo_aca",
          "apo_fin",
          "apo_otr",
          "ictx_n","ictx_l","ictx_m","ictx_a",
          "trepitencia",
          "riesgo_est",
          "riesgo",
          "riesgo_clase",
      };
      String[] linea = new String[(conPersonales?encabezadoPersonales.length:0)+encabezado.length];
      Arrays.fill(linea, "");
      PrintStream ps = new PrintStream("vistaIES.csv");
      ps.println((conPersonales?CajaDeHerramientas.stringToCSV(encabezadoPersonales):"")+CajaDeHerramientas.stringToCSV(encabezado));
      for (IES ies: kernel.listaIES) {
        System.out.println("*" + ies.codigo);
        int nes = 0;
        for (Estudiante e:ies.estudiantes) {
          int pos = 0;
          if (conPersonales) {
            Estudiante_DatosPersonales edp = ies.datosPersonalesEstudiantes[nes];
            linea[pos++] = String.valueOf(new String(edp.nombre));
            linea[pos++] = String.valueOf(new String(edp.apellido));
            linea[pos++] = String.valueOf(CajaDeHerramientas.tipoDocumentoToString(edp.tipoDocumento));
            linea[pos++] = String.valueOf(edp.documento==-1?"":edp.documento);
            linea[pos++] = String.valueOf(edp.diaFechaNacimiento==-1?"":edp.diaFechaNacimiento);
            linea[pos++] = String.valueOf(edp.mesFechaNacimiento==-1?"":edp.mesFechaNacimiento);
            linea[pos++] = String.valueOf(edp.anhoFechaNacimiento==-1?"":edp.anhoFechaNacimiento);
          }
          linea[pos++] = String.valueOf(ies.codigo);
          linea[pos++] = String.valueOf(nes);
          linea[pos++] = formateoCampoNum(e.getSexo());
          linea[pos++] = formateoCampoNum(e.getEdadAlPresentarElICFES());
          linea[pos++] = formateoCampoNum(e.getNivelEducativoMadre());
          linea[pos++] = formateoCampoNum(e.getIngresoHogar());
          linea[pos++] = formateoCampoNum(e.getIngresoHogar2());
          linea[pos++] = formateoCampoNum(e.getViviendaPropia());
          linea[pos++] = formateoCampoNum(e.getNumeroHermanos());
          linea[pos++] = formateoCampoNum(e.getPosicionEntreLosHermanos());
          linea[pos++] = formateoCampoNum(e.getNivelSisben());
          linea[pos++] = formateoCampoNum(e.getEstrato());
          linea[pos++] = formateoCampoNum(e.getPersonasFamilia());
          linea[pos++] = formateoCampoNum(e.getPuntajeICFES());
          linea[pos++] = formateoCampoNum(e.getPerIcfes());
          linea[pos++] = formateoCampoNum(e.getRemplazoICFES());
          linea[pos++] = formateoCampoNum(e.getTrabajabaCuandoPresentoIcfes());
          //INI SbPro
          linea[pos++] = formateoCampoNum(e.getPuntajeSBPRO());
          linea[pos++] = formateoCampoNum(e.getEdadSBPRO());
          linea[pos++] = formateoCampoNum(e.getAreaSBPRO());
          linea[pos++] = formateoCampoNum(e.getEducacionMadreSBPRO());
          linea[pos++] = formateoCampoNum(e.getOcupacionMadreSBPRO());
          linea[pos++] = formateoCampoNum(e.getInternetSBPRO());
          linea[pos++] = formateoCampoNum(e.getValorMatriculaSBPRO());
          //FIN SbPro
          linea[pos++] = ies.semestres[e.getSemestrePrimiparo()];
          int g = e.getSemestreGrado();
          linea[pos++] = g==-1?"":ies.semestres[g];
          int rf = e.getSemestreRetiroForzoso();
          linea[pos++] = rf==-1?"":ies.semestres[rf];
          linea[pos++] = e.getIndicePrograma()<0?"":("\""+new String(ies.programas[e.getIndicePrograma()].codigoSNIES)+"\"");
          {
            Programa prog = e.getIndicePrograma()<0?null:ies.programas[e.getIndicePrograma()];
            int area = prog==null||prog.area==-1?-1:prog.area;
            for (int iA = -1;iA<=9;iA++) {
              if (iA==0) continue;
              linea[pos++] = iA==area?"1":"0";
            }
            linea[pos++] = formateoCampoNum(e.getIndicePrograma()<0?-1:ies.programas[e.getIndicePrograma()].nivel);
            linea[pos++] = formateoCampoNum(e.getIndicePrograma()<0?-1:ies.programas[e.getIndicePrograma()].nucleo);
          }
          {
            byte estado = e.getEstado();
            linea[pos++] = estado==1?"1":"0";
            linea[pos++] = estado==2?"1":"0";
            linea[pos++] = estado==0?"1":"0";
            linea[pos++] = estado<0?"1":"0";
          }
          long matri=e.getSemestresMatriculadoAlDerecho();
          int segvar = 1;
          double[] reps = e.getRepitencias();
          for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
            int npos = pos;
            linea[npos++] = formateoCampoNum(segvar);
            linea[npos++] = ies.semestres[j];
            linea[npos++] = formateoCampoNum(e.getNumeroMateriasTomadas(j));
            linea[npos++] = formateoCampoNum(e.getNumeroMateriasAprobadas(j));
            linea[npos++] = e.getRecibioApoyoAcademico(j)?"1":"0";
            linea[npos++] = e.getRecibioApoyoFinanciero(j)?"1":"0";
            linea[npos++] = e.getRecibioApoyoOtro(j)?"1":"0";
            {
              byte icetex = e.getTipoApoyoICETEXRecibido(j);
              for (int i=0;i<4;i++) linea[npos++] = i==icetex?"1":"0";  
            }
            linea[npos++] = formateoCampoNum(reps[j]);
            linea[npos++] = formateoCampoNum(e.getRiesgo(j));
            linea[npos++] = formateoCampoNum(e.getRiesgoEstructural(j));
            linea[npos++] = formateoCampoNum(e.getClaseRiesgo(j));
            segvar++;
            ps.println(CajaDeHerramientas.stringToCSV(linea));
          }
          nes++;
        }
      }
      ps.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Fin escritura");
  }
  
}
