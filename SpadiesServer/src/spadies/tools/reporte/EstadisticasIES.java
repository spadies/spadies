package spadies.tools.reporte;

import java.io.File;
import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;

import spadies.kernel.Estudiante;
import spadies.kernel.IES;
import spadies.kernel.Programa;

public class EstadisticasIES {

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    if (args.length!=2) {
      System.err.println("<ruta carpeta interes> <nombre archivo salida>");
      System.exit(1);
    }
    PrintStream psGen = new PrintStream(args[1]+".csv"),
      psPer = new PrintStream(args[1]+".per.csv");
    for (String enc:new String[]{"IES","Periodo","Primíparos","Con ICFES"})
      psPer.print(enc+";");
    psPer.println();
    for (String enc:new String[]{"IES","Codigos cruzaron"})
      psGen.print(enc+";");
    psGen.println();
    for (File f:new File(args[0]).listFiles()) {
      if (f.getName().toLowerCase().endsWith(".spa")) {
        IES ies = IES.cargar(f, false);
        printRes(procesoIES(ies),psGen, psPer);
      }
    }
    

  }

  private static void printRes(ResultadoIES res, PrintStream psGen,
      PrintStream psPer) {
    {
      for (int i=0;i<res.n;i++) {
        psPer.println(res.cod+";"+res.semestres[i]+";"+res.primiparos[i]+";"+res.primiparos_icfes[i]);
      }
    }
    {
      psGen.println(res.cod+";"+res.p_crucec);
    }
  }

  private static ResultadoIES procesoIES(IES ies) {
    ResultadoIES res = new ResultadoIES(ies.n);
    res.cod = ies.codigo;
    res.semestres = ies.semestres;
    {
      Set<String> c_cruz = new TreeSet<String>(),
        c_tot = new TreeSet<String>();
      for (Programa p:ies.programas) {
        String cod = new String(p.codigoSNIES);
        boolean cruzo = p.nivel!=-1;
        c_tot.add(cod);
        if (p.nivel!=-1) c_cruz.add(cod);
      }
      res.p_totalesc = c_tot.size();
      res.p_crucec= c_cruz.size();
    }
    {
      int conteo[] = new int[ies.n],
        conteo_ic[] = new int[ies.n];
      for (Estudiante e:ies.estudiantes) {
        conteo[e.getSemestrePrimiparo()]++;
        if (e.getPuntajeICFES()!=-1) conteo_ic[e.getSemestrePrimiparo()]++;
      }
      res.primiparos = conteo;
      res.primiparos_icfes = conteo_ic;
    }
    int[] mat = new int[ies.n];
    return res;
  }
  private static class ResultadoIES {
    int cod;
    String semestres[] = new String[0];
    int p_totalesc,p_crucec;
    int n;
    int [] primiparos_icfes;
    int [] primiparos;
    public ResultadoIES(int n) {
      this.n = n;
      primiparos_icfes = new int[n];
      primiparos = new int[n];
    }
  }

}
