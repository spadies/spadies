package spadies.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import spadies.util.CajaDeHerramientas;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.PuertaAlServidorDeConsultas;
import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;

public class PruebaStress {
  private int nhilos = -1;
  private Collection<EspecificacionConsulta> pruebas = new LinkedList<EspecificacionConsulta>();
  private HiloPrueba[] hilos = null;
  private List<RegistroConsulta> regs = Collections.synchronizedList(new LinkedList<RegistroConsulta>());
  private File fReporteAgregado;
  public static void main(String [] args) throws IOException, MyException {
    //Constantes.ipServidorSPADIES = "157.253.187.107";
    Constantes.ipServidorSPADIES = "200.41.9.229";
    for (int i=1;i<=20;i++) {
      new PruebaStress(new File("pruebasConsultasSPADIES.txt"),i, new File("rep0_"+i+".txt")).runPrueba();
    }
    //PuertaAlServidorDeConsultas.obtenerResultadoConsulta(tipoConsulta, claseResultado, args)
  }
  public PruebaStress(File fData, int nhilos, File fReporteAgregado) throws IOException, MyException {
    this.nhilos = nhilos;
    this.fReporteAgregado = fReporteAgregado;
    hilos = new HiloPrueba[nhilos];
    BufferedReader br = new BufferedReader(new FileReader(fData));
    int con = 0;
    for (String s=br.readLine();s!=null;s=br.readLine()) {
      EspecificacionConsulta ec = EspecificacionConsulta.parsearConsulta(String.valueOf(con++), s);
      pruebas.add(ec);
    }
    br.close();
    for (int i=0;i<this.nhilos;i++)
      hilos[i] = new HiloPrueba(regs, pruebas);
  }
  public void runPrueba() {
    for (HiloPrueba hp:hilos) hp.start();
    for (HiloPrueba hp:hilos)
      while (hp.isAlive()) {
        try {
          hp.join();
        } catch (InterruptedException e) {}
      }
    /*for (RegistroConsulta reg:regs)
      System.out.println(reg.ec.tipo+"\t"+reg.tiempo+"\t"+reg.error);*/
    {
      PrintStream ps;
      try {
        ps = new PrintStream(fReporteAgregado);
        for (RegistroConsulta reg:regs) {
          ps.println(reg.ec.nombre+"\t"+reg.ec.tipo+"\t"+reg.tiempo+"\t"+(reg.error?1:0));
        }
        ps.close();
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  private static class HiloPrueba extends Thread {
    private Collection<EspecificacionConsulta> consultas;
    private Collection<RegistroConsulta> resultado;
    public HiloPrueba(Collection<RegistroConsulta> resultado, Collection<EspecificacionConsulta> consultas) {
      this.resultado = resultado;
      this.consultas = consultas;
    }
    public void run() {
      for (EspecificacionConsulta ec:consultas) {
        RegistroConsulta reg = null;
        long tIni = System.currentTimeMillis();
        try {
          ec.realizarConsulta();
          reg = new RegistroConsulta(ec, System.currentTimeMillis()-tIni,false);
        } catch (Throwable t) {
          reg = new RegistroConsulta(ec, System.currentTimeMillis()-tIni,true);
          //t.printStackTrace();
        }
        resultado.add(reg);
      }
    }
  }
  public static enum TipoConsulta {
    CONTEO_POBLACION(1),
    CONTEO_POBLACION_SEMESTRAL(2),
    CRUCE_VARIABLES(8),
    DESERCION_PERIODO(14);
    public final int idc;
    private TipoConsulta(int idc) {
      this.idc = idc;
    }
  }
  public static class EspecificacionConsulta {
    TipoConsulta tipo;
    Object[] parametros;
    String nombre;
    public EspecificacionConsulta(String nombre, TipoConsulta tipo, Filtro[] filtros, Variable[] diferenciados) {
      this.nombre = nombre;
      this.tipo = tipo;
      this.parametros = new Object[]{filtros,diferenciados};
    }
    public void realizarConsulta() throws MyException {
      PuertaAlServidorDeConsultas.obtenerResultadoConsulta(tipo.idc, Object[].class, parametros);
    }
    public static EspecificacionConsulta parsearConsulta(String nombre, String s) {
      String sp[] = CajaDeHerramientas.csvToString(s, 0, ';');
      //String sConsulta = sp[0],sFiltros=sp[1],sVariables=sp[2];
      TipoConsulta.valueOf(sp[0]);
      Filtro[] fil = parsearFiltros(sp[1]);
      Variable[] vars = parsearVariables(sp[2]);
      return new EspecificacionConsulta(nombre,TipoConsulta.valueOf(sp[0]),parsearFiltros(sp[1]),parsearVariables(sp[2]));
    }
    private static Variable[] parsearVariables(String s) {
      String sp[] = CajaDeHerramientas.csvToString(s, 0, ',');
      Collection<Variable> vars = new LinkedList<Variable>();
      for (String ss:sp)
        vars.add(Variable.valueOf(ss));
      return vars.toArray(new Variable[vars.size()]);
    }
    private static Filtro[] parsearFiltros(String s) {
      String sp[] = CajaDeHerramientas.csvToString(s, 0, ',');
      Collection<Filtro> fils = new LinkedList<Filtro>();
      for (String ss:sp) {
        fils.add(parsearFiltro(ss));
      }
      return fils.toArray(new Filtro[fils.size()]);
    }
    private static Filtro parsearFiltro(String s) {
      String sp[] = CajaDeHerramientas.csvToString(s, 0, '=');
      Variable var = Variable.valueOf(sp[0]);
      Collection<Byte> vals = new LinkedList<Byte>();
      for (String val:CajaDeHerramientas.csvToString(sp[1], 0, ' ')) {
        byte b = (byte) Integer.parseInt(val);
        vals.add(b);
      }
      Item [] items = new Item[vals.size()];
      {
        int i = 0;
        for (byte b:vals) items[i++] = new Item(b,"", "");
      }
      return new Filtro(var,items);
    }
  }
  public static class RegistroConsulta {
    public final EspecificacionConsulta ec;
    public final long tiempo;
    public final boolean error;
    public RegistroConsulta(EspecificacionConsulta ec, long tiempo, boolean error) {
      this.ec = ec;
      this.tiempo = tiempo;
      this.error = error;
    }
    public RegistroConsulta(EspecificacionConsulta ec, boolean error) {
      this(ec,-1,error);
    }
  }
}
