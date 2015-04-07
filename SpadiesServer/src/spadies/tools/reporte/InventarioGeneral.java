package spadies.tools.reporte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;

public class InventarioGeneral {
  public static void main(String [] args) throws MyException, FileNotFoundException {
    if (args.length!=0) {
      Constantes.carpetaDatos = new File(args[0]);
    }
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,false);
    PrintStream ps = new PrintStream(new File("InventarioGeneral.csv"));
    for (String enc:new String[]{"IES","Periodo","Primíparos","Matriculados","Retiros disciplinarios","Graduados","Materias tomadas","Materias aprobadas","Apoyos académicos","Apoyos financieros","Otros apoyos"})
      ps.print(enc+";");
    ps.println();
    for (IES ies:kernel.listaIES) {
      Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
      Object[] res = kernel.getTablaCantidadArchivos(new Filtro[]{filtro});
      String [][] valores = (String[][]) res[0];
      String[] encFilas = ((String[][]) res[1])[0];
      for (int i=1,t=encFilas.length;i<t;i++) {
        ps.print(ies.codigo+";"+encFilas[i]+";");
        for (int j = 0;j<9;j++) {
          ps.print(valores[i-1][j]+";");
        }
        ps.println();
      }
    }
    ps.close();
  }
}
