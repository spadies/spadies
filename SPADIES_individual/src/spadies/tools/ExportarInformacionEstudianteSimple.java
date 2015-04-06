package spadies.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import spadies.kernel.Estudiante;
import spadies.kernel.Estudiante_DatosPersonales;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.kernel.Programa;
import spadies.util.Constantes;
import spadies.util.MyException;

/**
 * Imprime un registro por estudiante
 * @author an-cordo
 *
 */
public class ExportarInformacionEstudianteSimple {
  public static void main(String[] args) throws MyException, FileNotFoundException {
    final boolean personal = true;
    if (args.length!=1) {
      System.err.println("Uso: <archivo_salida>");
      System.exit(1);
    }
    File f = new File(args[0]);
    /*if (!f.canWrite()) {
      System.err.println("Error fatal: No es posible escribir en el archivo "+f);
      System.exit(1);
    }*/
    Constantes.cargarArchivoFiltroIES();
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,personal);
    PrintStream ps = new PrintStream(f);
    ps.println(textoCSV((Object[])encabezado));
    for (IES ies:kernel.listaIES) {
      for (int i=0,it=ies.estudiantes.length;i<it;i++) {
        Estudiante_DatosPersonales edp = personal?ies.datosPersonalesEstudiantes[i]:null;
        Estudiante e = ies.estudiantes[i];
        Programa p = e.getIndicePrograma()==-1?null:ies.programas[e.getIndicePrograma()];
        int apo_ictx = 0,apo_aca = 0,apo_fin = 0,apo_otr = 0;
        {
          int jI=e.getSemestrePrimiparo(),jT=ies.n;
          if (jI!=-1) for (int j=jI; j<jT; j++) {
            if (e.getTipoApoyoICETEXRecibido(j)!=0) apo_ictx++;
            if (e.getRecibioApoyoAcademico(j)) apo_aca++;
            if (e.getRecibioApoyoFinanciero(j)) apo_fin++;
            if (e.getRecibioApoyoOtro(j)) apo_otr++;
          }
        }
        ps.println(textoCSV(
          String.valueOf(ies.codigo),
          personal?new String(edp.apellido):null,
          personal?new String(edp.nombre):null,
          personal?edp.tipoDocumento==-1?"":String.valueOf(edp.tipoDocumento):null,
          personal?edp.documento==-1?"":String.valueOf(edp.documento):null,
          personal?new String(edp.codigo):null,
          e.getSexo(),
          personal?((edp.diaFechaNacimiento==-1?"":edp.diaFechaNacimiento)+"/"+(edp.mesFechaNacimiento==-1?"":edp.mesFechaNacimiento)+"/"+(edp.anhoFechaNacimiento==-1?"":edp.anhoFechaNacimiento)):null,
          textoPer(e.getSemestrePrimiparo(),ies.semestres),
          textoPer(e.getUltimoSemestreMatriculado(),ies.semestres),
          textoPer(e.getSemestreGrado(),ies.semestres),
          textoPer(e.getSemestreRetiroForzoso(),ies.semestres),
          e.getEstado(),
          p==null?"":new String(p.codigoSNIES),
          p==null?"":new String(p.nombre),
          p==null?"":formateoDatoSPADIES(p.area),
          p==null?"":formateoDatoSPADIES(p.metodologia),
          p==null?"":formateoDatoSPADIES(p.nivel),
          p==null?"":formateoDatoSPADIES(p.nucleo),
          formateoDatoSPADIES(e.getRemplazoICFES()),
          formateoDatoSPADIES(e.getNumeroHermanos()),
          formateoDatoSPADIES(e.getPosicionEntreLosHermanos()),
          formateoDatoSPADIES(e.getViviendaPropia()),
          formateoDatoSPADIES(e.getTrabajabaCuandoPresentoIcfes()),
          formateoDatoSPADIES(e.getNivelEducativoMadre()),
          formateoDatoSPADIES(e.getIngresoHogar()),
          formateoDatoSPADIES(e.getEdadAlPresentarElICFES()),
          formateoDatoSPADIES(e.getPuntajeICFES()),
          apo_ictx,
          apo_aca,
          apo_fin,
          apo_otr
          )
        );
      }
    }
    ps.close();
  }
  private static String textoPer(int i, String[] ref) {
    return i==-1?"":ref[i];
  }
  private static String formateoDatoSPADIES(int i) {
    return i==-1?"":String.valueOf(i);
  }
  private static String textoCSV(Object ... obs) {
    StringBuilder s=new StringBuilder();
    for (Object u:obs) {
      String su = u==null?"":u.toString();
      if (su.indexOf(';')!=-1 || su.indexOf('"')!=-1) {
        s.append("\""+su.replaceAll("\"","\"\"")+"\";");
      }
      else {
        s.append(u);s.append(";");
      }
    }
    return s.toString();
  }
  private static final String[] encabezado  = new String[]{
    "ies",
    "apellidos",
    "nombres",
    "tipoDocumento",
    "documento",
    "codigoEstudiante",
    "sexo",
    "fechaNacimiento",
    "periodoPrimiparo",
    "ultimoPeriodoMatricula",
    "periodoGrado",
    "periodoRetiroForzoso",
    "estado",
    
    "prog_cod",
    "prog_nombre",
    "prog_area",
    "prog_metodologia",
    "prog_nivel",
    "prog_nucleo",

    "remplazo_icf",
    "numeroHermanos",
    "posicionEntreHermanos",
    "viviendaPropia",
    "trabajaba",
    "nivelEducativoMadre",
    "ingresoHogar",
    "edadPresentacionExamen",
    "puntajeExamen",
    "apo_ictx",
    "apo_aca",
    "apo_fin",
    "apo_otr",
  };
}
