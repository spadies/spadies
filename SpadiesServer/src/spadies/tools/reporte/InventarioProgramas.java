package spadies.tools.reporte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import spadies.kernel.Estudiante;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.kernel.Programa;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;

public class InventarioProgramas {
  public static void main(String [] args) throws MyException, FileNotFoundException {
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,false);
    PrintStream ps = new PrintStream(new File("Programas.csv"));
    for (String enc:new String[]{"IES","prog_cod","prog_nombre","prog_area","prog_metod","prog_nivel","prog_nucleo","gentes"})
      ps.print(enc+";");
    ps.println();
    for (IES ies:kernel.listaIES) {
      int i=0, conteosP [] = new int[ies.programas.length];
      for (Estudiante e:ies.estudiantes) if (e.getIndicePrograma()!=-1) conteosP[e.getIndicePrograma()]++;
      for (Programa p:ies.programas){
        for (String campo:new String[]{
            String.valueOf(ies.codigo),
            new String(p.codigoSNIES), new String(p.nombre),
            imp(p.area),imp(p.metodologia),imp(p.nivel),imp(p.nucleo),String.valueOf(conteosP[i++])
        }) ps.print(campo+";");
        ps.println();
      }
    }
    ps.close();
  }

  public static String imp(byte b) {
    return b==-1?"":String.valueOf(b&0xFF);
  }
}
