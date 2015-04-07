package spadies.tools.extra.cpp;

import java.io.File;
import java.io.FileNotFoundException;
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

public class ExtraccionSPADIES {

  /**
   * @param args
   * @throws MyException 
   * @throws FileNotFoundException 
   */
  public static void main(String[] args) throws MyException, FileNotFoundException {
    String[] enc = new String[] {
        "e_nombrec","e_documento",
        "ies","cohorte",
        "pro_cod","pro_area","pro_nivel","pro_metodo",
        "e_nacim",
        "e_genero",
        "e_grado","e_retiro",
        "e_estado",
        "matriculas",
        "e_mvistas","e_maprobadas","e_mambas",
        "e_apoa","e_apof","e_apoo",
        "ese_edadIcfes",
        "ese_ingHogar",
        "ese_nivelEducativoMadre",
        "ese_numHermanos",
        "ese_posHermanos",
        "ese_puntaje",
        "ese_trabajaba",
        "ese_vivienda",
    };
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,true);
    PrintStream ps = new PrintStream(new File("SPADIES_ccp.csv"));
    for (String cmp:enc) ps.print(cmp+";");
    ps.println();
    for (IES ies:kernel.listaIES) for (int i=0,t=ies.estudiantes.length;i<t;i++) {
      int n = ies.n;
      if (i==0) System.out.println(ies.codigo);
      Estudiante e = ies.estudiantes[i];
      Estudiante_DatosPersonales edp = ies.datosPersonalesEstudiantes[i];
      Programa prog = e.getIndicePrograma()==-1?null:ies.programas[e.getIndicePrograma()];
      int p = 0;
      String [] linea = new String[enc.length];
      Arrays.fill(linea, "");
      linea[p++] = new String(edp.apellido) + " " + new String(edp.nombre);
      linea[p++] = (edp.documento==-1?"":("'"+edp.documento));

      linea[p++] = String.valueOf(ies.codigo);
      linea[p++] = String.valueOf(ies.semestres[e.getSemestrePrimiparo()]);
      
      linea[p++] = prog==null?"":new String(prog.codigoSNIES);
      linea[p++] = prog==null?"":mataDesco(prog.area);
      linea[p++] = prog==null?"":mataDesco(prog.nivel);
      linea[p++] = prog==null?"":mataDesco(prog.metodologia);

      linea[p++] = ((edp.anhoFechaNacimiento==-1 || edp.mesFechaNacimiento==-1 || edp.diaFechaNacimiento==-1)?"":(CajaDeHerramientas.intToString(edp.diaFechaNacimiento,2)+"/"+CajaDeHerramientas.intToString(edp.mesFechaNacimiento,2)+"/"+CajaDeHerramientas.intToString(edp.anhoFechaNacimiento,4)));
      linea[p++] = CajaDeHerramientas.sexoToString(e.getSexo());
      
      linea[p++] = e.getSemestreGrado()==-1?"":String.valueOf(ies.semestres[e.getSemestreGrado()]);
      linea[p++] = e.getSemestreRetiroForzoso()==-1?"":String.valueOf(ies.semestres[e.getSemestreRetiroForzoso()]);
      linea[p++] = String.valueOf(e.getEstado());
      int matriculas = 0;
      int mvistas = 0, mmaprobadas = 0, mambas = 0;
      int apoa = 0, apof = 0, apoo = 0;
      int jI=e.getSemestrePrimiparo();
      long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
      for (int j=jI; j<n; j++,matri>>>=1) if ((matri&1L)==1L) {
        matriculas++;
        
        if (e.getNumeroMateriasAprobadas(j)!=-1 && e.getNumeroMateriasTomadas(j)!=-1) {
          if (e.getNumeroMateriasAprobadas(j)!=-1) mmaprobadas++;
          if (e.getNumeroMateriasTomadas(j)!=-1) mvistas++;
          mambas++;
        }
        
        if (e.getRecibioApoyoAcademico(j)) apoa++;
        if (e.getRecibioApoyoFinanciero(j)) apof++;
        if (e.getRecibioApoyoOtro(j)) apoo++;
      }
      linea[p++] = String.valueOf(matriculas);
      linea[p++] = String.valueOf(mvistas);
      linea[p++] = String.valueOf(mmaprobadas);
      linea[p++] = String.valueOf(mambas);
      
      linea[p++] = String.valueOf(apoa);
      linea[p++] = String.valueOf(apof);
      linea[p++] = String.valueOf(apoo);
      
      linea[p++] = mataDesco(e.getEdadAlPresentarElICFES());
      linea[p++] = mataDesco(e.getIngresoHogar());
      linea[p++] = mataDesco(e.getNivelEducativoMadre());
      linea[p++] = mataDesco(e.getNumeroHermanos());
      linea[p++] = mataDesco(e.getPosicionEntreLosHermanos());
      linea[p++] = mataDesco(e.getPuntajeICFES());
      linea[p++] = mataDesco(e.getTrabajabaCuandoPresentoIcfes());
      linea[p++] = mataDesco(e.getViviendaPropia());
      for (String cmp:linea) ps.print(cmp+";");
      ps.println();
    }
    ps.close();
  }

  private static String mataDesco(byte x) {
    return x==-1?"":String.valueOf(x&0xFF);
  }
}
