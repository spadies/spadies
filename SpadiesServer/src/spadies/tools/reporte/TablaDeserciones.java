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

public class TablaDeserciones {
  public static void main(String [] args) throws MyException, FileNotFoundException {
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    Constantes.cargarArchivoFiltroIES();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,false);
    {//Desercion por periodo
      PrintStream ps = new PrintStream(new File("DesercionPeriodo.csv"));
      ps.println("ies;periodo;matriculadosConsiderados;desertores;tasa");
      for (IES ies:kernel.listaIES) {
        Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
        Object[] res = kernel.getTasaDesercion(new Filtro[]{filtro}, new Variable[]{});
        int[] matriculadosConsiderados = (int[]) res[0];
        int[] desertores = (int[]) res[1];
        double [] tasa = (double[]) res[2];
        for (int i=0;i<ies.n;i++)
          ps.println(ies.codigo+";"+ies.semestres[i]+";"+matriculadosConsiderados[i]+";"+desertores[i]+";"+tasa[i]);
      }
      ps.close();
    }
    {//Desercion por cohorte
      PrintStream ps = new PrintStream(new File("DesercionCohorte.csv"));
      ps.println("ies;1;2;3;4;5;6;7;8;9;10;11;12;13;14;15;16;17;18;19;20;21;22");
      for (IES ies:kernel.listaIES) {
        Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
        Object[] res = kernel.getDesercionPorCohorte(new Filtro[]{filtro}, new Variable[0]);
        double[][] datos = (double[][]) res[1];
        
        for (int i=0;i<ies.n;i++) {
          ps.print(ies.codigo);
          for (int j=0;j<ies.n;j++) {
            ps.print(";");
            if (datos[i][j]!=Double.MAX_VALUE) ps.print(datos[i][j]);
          }
          ps.println();
        }
      }
      ps.close();
    }
  }
}
