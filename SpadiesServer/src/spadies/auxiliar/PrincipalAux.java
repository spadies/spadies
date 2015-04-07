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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
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
import spadies.gui.frames.PanelConsultasDesercion;
import spadies.gui.util.InfoTabla;
import spadies.io.MyDataOutputStream;
import spadies.kernel.Estudiante;
import spadies.kernel.Estudiante_DatosPersonales;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.kernel.Programa;
import spadies.kernel.VariableExtra;
import spadies.server.ServerMatch;
import spadies.server.kernel.TablaDepartamentos;
import spadies.server.kernel.TablaProgramas;
import spadies.server.util.OperacionesAdministrativas;
import spadies.util.CajaDeHerramientas;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;

public class PrincipalAux {
  enum ArchivoUN {
    PRI("Primiparos",new File("un_primiparos.csv"),
        "PERIODO_ING;SEXO;EST_NACIMIENTO"),
    MAT("Matriculados",new File("un_matriculados.csv"),
        "PERIODO_MAT;APOYO_ACA;APOYO_FIN;APOYO_OTRO"),
    GRA("Graduados",new File("un_graduados.csv"),
        "PERIODO_GRA"),
    DES("Desertores",new File("un_desertores_ultimoPeriodoMatricula.csv"),
        "PERIODO_DES");
    private String nombre = "";
    public final File fSal;
    public final String[] encExtra;
    private ArchivoUN(String nombre, File fSal, String encExtra) {
      this.encExtra = encExtra.split(";");
      this.fSal = fSal;
      this.nombre = nombre;
    }
    public String toString() {
      return nombre;
    }
  }
  public static String procs(byte b) {return b==-1?"":String.valueOf(b);}
  public static void main(String[] args) throws Exception {
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,false);
    {
      PrintStream psx = new PrintStream(new File("desercion3-prog.csv"));
      for(IES ies:kernel.listaIES) {
        for (int ii=0,tt=ies.programas.length;ii<tt;ii++) {
          //if (ii!=-1) psp.println(ies.codigo+";"+ii+";"+new String(ies.programas[ii].codigoSNIES)+";"+new String(ies.programas[ii].nombre));
          System.out.println(ies.codigo+"\t"+ii+"/"+tt);
          Filtro[] filtro = new Filtro[]{
              new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")}),
              new Filtro(Variable.AUX_INDPROG, new Item[]{new Item(ii,"","")})
          };
          try {
            Object[] resultado = PanelConsultasDesercion.obtenerResultado(false, new Variable[]{}, filtro);
            Map<byte[],int[]> resC=(Map<byte[], int[]>) resultado[0];
            Map<byte[],double[]> resP=(Map<byte[], double[]>) resultado[1];
            //Map<byte[],double[]> resT=(Map<byte[], double[]>) resultado[2];
            Integer[] codigosIESDif=(Integer[])(resultado[2]);
            String[] codigosProgramasDif=(String[])(resultado[3]);
            for (double[] arrP:resP.values()) {
              Programa p = ies.programas[ii];
              String s = ies.codigo + ";" + ii + ";" + new String(p.nombre) + ";'" + new String(p.codigoSNIES);  
              for (double d :arrP)
                s+=";"+Double.toString(d).replace('.', ',');
              psx.println(s);
            }
          } catch (Exception e) {
            System.err.println(e.getMessage());
          }
        }
      }
      psx.close();
    }
    System.exit(0);
    {
      /*
        insheet using PRIM-VARIOS.csv, delimiter(";") clear
        drop if inlist(-1,estrato,icfes)
        contract _all
        replace icfes = icfes + 1
        bysort ies estrato :egen t = sum(_freq)
        gen p = _freq/t
       */
      PrintStream pse = new PrintStream("PRIM-VARIOS.csv");
      pse.println("ies;estrato;icfes");
      for(IES ies:kernel.listaIES) {
        System.out.println(ies.codigo);
        SortedMap<Comparable, int[]> smic = new TreeMap<Comparable, int[]>();
        for (Estudiante e:ies.estudiantes) {
          pse.println(
              ies.codigo 
              + ";" + Variable.ESTRATO.rango.getRango(Variable.ESTRATO.getValor(e,ies))
              + ";" + Variable.CLASIFICACION_PUNTAJE_ICFES_EST.rango.getRango(Variable.CLASIFICACION_PUNTAJE_ICFES_EST.getValor(e,ies))
              );
        }
      }
      pse.close();
      if (true) return;
    }
    {
      /*
        insheet using PRIM-VARIOS.csv, delimiter(";") clear
        replace num_icetex = 1 if num_icetex > 1
        contract _all,freq(conteo)
        recode ingreso icfes (-1=.)
        replace icfes = 1 + icfes
        label define ling 1 "[0,1) salarios mínimos" 2 "[1,2) salarios mínimos" ///
        3 "[2,3) salarios mínimos" 4 "[3,5) salarios mínimos" 5 "[5,7) salarios mínimos" ///
        6 "[7,10) salarios mínimos" 7 "[10,) salarios mínimos"
        label values ingreso ling 
        saveold primiparos-ingresos+icfes+cohorte+icetex,replace
       */
      PrintStream pse = new PrintStream("PRIM-VARIOS.csv");
      pse.println("ies;cohorte;ingreso;icfes;num_icetex");
      System.out.println(Variable.CLASIFICACION_PUNTAJE_ICFES_DECILES_EST.rango.toString((byte)1));
      for(IES ies:kernel.listaIES) {
        System.out.println(ies.codigo);
        SortedMap<Comparable, int[]> smic = new TreeMap<Comparable, int[]>();
        for (Estudiante e:ies.estudiantes) {
          pse.println(
              ies.codigo 
              + ";" + ies.semestres[e.getSemestrePrimiparo()]
              + ";" + Variable.INGRESO_HOGAR_EST2.rango.getRango(Variable.INGRESO_HOGAR_EST2.getValor(e,ies))
              + ";" + Variable.CLASIFICACION_PUNTAJE_ICFES_DECILES_EST.rango.getRango(Variable.CLASIFICACION_PUNTAJE_ICFES_DECILES_EST.getValor(e,ies))
              + ";" + Variable.NUMERO_APOYOS_ICETEX_RECIBIDOS_EST.rango.getRango(Variable.NUMERO_APOYOS_ICETEX_RECIBIDOS_EST.getValor(e,ies))
              );
        }
      }
      pse.close();
      if (true) return;
    }
    {
      /*
        insheet using PRIM-ESTRATO.csv, delimiter(";") clear
        recode estrato (-1=9)
        reshape wide conteo, i(ies cohorte) j(estrato)
        keep if cohorte>=20121 & cohorte<=20142
        collapse (sum) conteo*, by(ies)
        outsheet using expoest.csv, delimiter(";") replace
       */
      PrintStream pse = new PrintStream("PRIM-ESTRATO.csv");
      pse.println("ies;estrato;cohorte;conteo");
      System.out.println(Variable.CLASIFICACION_PUNTAJE_ICFES_DECILES_EST.rango.toString((byte)1));
      for(IES ies:kernel.listaIES) {
        System.out.println(ies.codigo);
        SortedMap<Comparable, int[]> smic = new TreeMap<Comparable, int[]>();
        for (Estudiante e:ies.estudiantes) {
          Byte v = (Byte) Variable.ESTRATO.rango.getRango(Variable.ESTRATO.getValor(e,ies));
          if (1!=(Byte) Variable.CLASIFICACION_PUNTAJE_ICFES_DECILES_EST.rango.getRango(Variable.CLASIFICACION_PUNTAJE_ICFES_DECILES_EST.getValor(e,ies)))
            continue; //ICFES decil 2
          //String coh = Variable.PERIODO_INGRESO_EST.rango.toString((Comparable) Variable.PERIODO_INGRESO_EST.getValor(e,ies));
          int[] aric = smic.get(v);
          if (aric==null) smic.put(v, aric = new int[ies.n]);
          aric[e.getSemestrePrimiparo()]++;
        }
        for (Entry<Comparable, int[]> e:smic.entrySet())
          for (int i=0;i<ies.n;i++)
            pse.println(ies.codigo+";"+e.getKey()+";"+ies.semestres[i]+";"+e.getValue()[i]);
      }
      pse.close();
      if (true) return;
    }
    {
      /*
       insheet using expoicf.csv, delimiter(";") clear
        recode icfes (-1=9)
        reshape wide conteo, i(ies cohorte) j(icfes)
        outsheet using expoicf2.csv, delimiter(";") replace
        insheet using expoing.csv, delimiter(";") clear
        recode ingresos (-1=9)
        reshape wide conteo, i(ies cohorte) j(ingresos)
        outsheet using expoing2.csv, delimiter(";") replace

       */
      PrintStream psicf = new PrintStream("expoicf.csv");
      psicf.println("ies;icfes;cohorte;conteo");
      PrintStream psing = new PrintStream("expoing.csv");
      psing.println("ies;ingresos;cohorte;conteo");
      for(IES ies:kernel.listaIES) {
        System.out.println(ies.codigo);
        SortedMap<Comparable, int[]> smic = new TreeMap<Comparable, int[]>(),
            smin = new TreeMap<Comparable, int[]>();
        for (Estudiante e:ies.estudiantes) {
          Byte vic = (Byte) Variable.CLASIFICACION_PUNTAJE_ICFES_EST.rango.getRango(Variable.CLASIFICACION_PUNTAJE_ICFES_EST.getValor(e,ies));
          Byte vin = (Byte) Variable.INGRESO_HOGAR_EST2.rango.getRango(Variable.INGRESO_HOGAR_EST2.getValor(e,ies));
          //String coh = Variable.PERIODO_INGRESO_EST.rango.toString((Comparable) Variable.PERIODO_INGRESO_EST.getValor(e,ies));
          int[] aric = smic.get(vic);
          if (aric==null) smic.put(vic, aric = new int[ies.n]);
          int[] arin = smin.get(vin);
          if (arin==null) smin.put(vin, arin = new int[ies.n]);
          aric[e.getSemestrePrimiparo()]++;
          arin[e.getSemestrePrimiparo()]++;
        }
        for (Entry<Comparable, int[]> e:smic.entrySet())
          for (int i=0;i<ies.n;i++)
            psicf.println(ies.codigo+";"+e.getKey()+";"+ies.semestres[i]+";"+e.getValue()[i]);
        for (Entry<Comparable, int[]> e:smin.entrySet())
          for (int i=0;i<ies.n;i++)
            psing.println(ies.codigo+";"+e.getKey()+";"+ies.semestres[i]+";"+e.getValue()[i]);
      }
      psicf.close();
      psing.close();
      if (true) return;
    }
    
    PrintStream psx = new PrintStream(new File("solFranco.csv")),
      psp = new PrintStream(new File("solFrancoP.csv"));
    for(IES ies:kernel.listaIES) {
      for (int ii=-1,tt=ies.programas.length;ii<tt;ii++) {
        if (ii!=-1) psp.println(ies.codigo+";"+ii+";"+new String(ies.programas[ii].codigoSNIES)+";"+new String(ies.programas[ii].nombre));
        System.out.println(ies.codigo+"\t"+ii+"/"+tt);
        Filtro[] filtro = new Filtro[]{
            new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")}),
            new Filtro(Variable.AUX_INDPROG, new Item[]{new Item(ii,"","")})
        };
        try {
          Object[] resultado = ProcesadorDesercionPorPeriodo.obtenerResultado(false, new Variable[]{}, filtro);
          Map<byte[],int[]> resM=(Map<byte[], int[]>) resultado[0],
            resD=(Map<byte[], int[]>) resultado[1];
          Map<byte[],double[]> resT=(Map<byte[], double[]>) resultado[2];
          Integer[] codigosIESDif=(Integer[])(resultado[3]);
          String[] codigosProgramasDif=(String[])(resultado[4]);
          int tam = 0;
          for (int[] arrM:resM.values()) tam+= arrM.length;
          //int liminf = (Integer) res[6];
          //System.out.println(liminf);
          Variable[] diferenciados = new Variable[0];
          int limInf = (Integer) resultado[6]; 
          String encFilas[][]=new String[1+diferenciados.length][tam+1],encColumnas[]=new String[]{"NO graduados","Desertores","Deserción", "Retención"};
          String[][] valores=new String[tam][4];
          encFilas[0][0]="Periodo";
          for (int i=0,t=diferenciados.length;i<t;i++) encFilas[i+1][0]=diferenciados[i].nombre; 
          int fi = 0;
          for (byte[] llave:resM.keySet()) {
            int [] matr = resM.get(llave), des = resD.get(llave);
            double [] tasa = resT.get(llave);
            for (int i=0, t=matr.length;i<t;i++) {
              encFilas[0][fi+1] = CajaDeHerramientas.codigoSemestreToString((byte) (limInf+i));
              for (int j=0,tj=diferenciados.length;j<tj;j++)
                encFilas[j+1][fi+1] = Variable.toString(diferenciados[j],llave[j],codigosIESDif,codigosProgramasDif);
              if (tasa[i]==0)
                valores[fi] = new String[]{String.valueOf(matr[i]), String.valueOf(des[i]),"",""};
              else
                valores[fi] = new String[]{String.valueOf(matr[i]), String.valueOf(des[i]),df_porcentaje.format(tasa[i]),df_porcentaje.format(1d-tasa[i])};
              fi++;
            }
          }
          InfoTabla it = new InfoTabla(valores,encFilas,encColumnas);
          int tx = it.getNumColumnas(), ty = it.getNumFilas();
          for (int y=0;y<ty;y++) {
            String per = it.getEncabezadoFila(y, 0);
            //if ("2009-1".compareTo(per)>0 || "2010-1".compareTo(per)<0) continue;
            if ("2012-1".compareTo(per)>0) continue;
            psx.println(ies.codigo+";"+ii+";"+per+";"+it.getValor(y, 1)+";"+it.getValor(y, 2));
            System.out.println(per);
          }
          /*ProcesadorDesercionPorPeriodo.instance.setParametros(false, new Variable[]{}, filtro);
          Collection<ResultadoConsulta> res = ProcesadorDesercionPorPeriodo.instance.generarGrafica();
          for (ResultadoConsulta r:res) if (r.nombre.startsWith("Tabla Dese")) {
            InfoTabla it = (InfoTabla) r.resultado;
            int tx = it.getNumColumnas(), ty = it.getNumFilas();
            for (int y=0;y<ty;y++) {
              System.out.println(it.getEncabezadoFila(y, 0));
            }
          }*/
        } catch (Exception e) {
          System.err.println(e.getMessage());
        }
      }
    }
  }
  public static void mainx(String[] args) throws Exception {
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,false);
    {
      for(IES ies:kernel.listaIES)
        for(Programa p: ies.programas)
          System.out.println(ies.codigo + ";" + new String(p.codigoSNIES) + ";" + new String(p.nombre));
    }
    System.exit(1);
    if (false) {
      PrintStream ps = new PrintStream(new File("xxxxy.csv"));
      TablaProgramas tp = TablaProgramas.getInstance();
      tp.preparar();
      ps.println(CajaDeHerramientas.stringToCSV("codigo","codigo","nombre","area","nucleo","nivel"));
      for (File f: new File("D:/SPADIES/aplicativo/2014-03-03-enmendado/spadies_v2.8/datos").listFiles()) {
        IES ies = IES.cargar(f, false);
        List<byte[][]> pr = tp.getDatosPorIES(Integer.toString(ies.codigo).getBytes());
        for (Programa p:ies.programas) {
          byte[][] pro = tp.getDatosPorSNIES(p.codigoSNIES);
          if (pro==null)pro = tp.getDatosPorConsecutivo(p.codigoSNIES);
          if (pro==null) ps.println("X");
          else ps.println(CajaDeHerramientas.stringToCSV(new String(pro[0]),new String(pro[3]),new String(pro[4])));
        }
      //  if (pr!=null) for (byte[][] b : pr)
        //  ps.println(CajaDeHerramientas.stringToCSV(new String(b[0]),new String(b[3]),new String(b[4])));
      }
      ps.close();
      System.exit(1);
    }
    {
      //File fref = new File("D:/SPADIES/aplicativo/2014-03-03-enmendado/spadies_v2.8/datos");
      File fref = new File("ies_out");
      PrintStream ps = new PrintStream(new File("xxxx.csv"));
      ps.println(CajaDeHerramientas.stringToCSV("codigo","codigo","nombre","area","nucleo","nivel"));
      for (File f: fref.listFiles()) {
        IES ies = IES.cargar(f, false);
        for (Programa p:ies.programas) {
          ps.println(CajaDeHerramientas.stringToCSV(ies.codigo+"",new String(p.codigoSNIES),new String(p.nombre),p.area+"",p.nucleo+"",p.nivel+""));
        }
      }
      ps.close();
      System.exit(1);
    }
    for (Item item:Variable.NUCLEO_CONOCIMIENTO_EST.items)
      System.out.println(item.key + ";" + item.value);
    System.exit(1);
    {
      Set<Byte> s1 = new TreeSet<Byte>(), s2 = new TreeSet<Byte>();
      for (IES ies:kernel.listaIES) for (Estudiante e:ies.estudiantes) {
        s1.add(e.getNivelSisben());
        s2.add(e.getEstrato());
      }
      System.out.println(s1);
      System.out.println(s2);
    }
    System.exit(1);
    {
      Set<Byte> s1 = new TreeSet<Byte>(), s2 = new TreeSet<Byte>();
      for (IES ies:kernel.listaIES) for (Estudiante e:ies.estudiantes) {
        s1.add(e.getNivelSisben());
        s2.add(e.getEstrato());
      }
      System.out.println(s1);
      System.out.println(s2);
    }
    System.exit(1);
    {
      PrintStream ps = new PrintStream(new File("registros_periodo.csv"));
      ps.println("nest;ies;area;periodo;numper;est");
      int nest = 0;
      for (IES ies:kernel.listaIES) {
        for (Estudiante e:ies.estudiantes) {
          int ip = e.getIndicePrograma();
          byte area = (ip==-1?(byte)-1:ies.programas[ip].area);
          int jI=e.getSemestrePrimiparo();
          long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
          int conteo = 0;
          nest++;
          for (int j=jI; j<ies.n; j++,matri>>>=1) if ((matri&1L)==1L) {
            ps.println(nest+";"+ies.codigo+";"+area+";"+ies.semestres[j]+";"+(++conteo)+";"+e.getEstado());
          }
        }
      }
      ps.close();
    }
    /*{
      PrintStream ps = new PrintStream(new File("prim_matr_grad_periodo_ies_area.csv"));
      ps.println("ies;area;per;primiparos;matriculados;graduados");
      for (IES ies:kernel.listaIES) {
        Map<Byte,int[][]> resp = new TreeMap<Byte, int[][]>();
        for (Programa p: ies.programas)
          resp.put(p.area, new int[3][ies.n]);
        resp.put((byte) -1, new int[3][ies.n]);
        for (Estudiante e:ies.estudiantes) {
          int ip = e.getIndicePrograma();
          byte area = (ip==-1?(byte)-1:ies.programas[ip].area);
          int[][] datarea = resp.get(area); 
          datarea[0][e.getSemestrePrimiparo()]++;
          int sg = e.getSemestreGrado();
          if (sg!=-1)
            datarea[2][sg]++;
          int jI=e.getSemestrePrimiparo();
          long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
          for (int j=jI; j<ies.n; j++,matri>>>=1) if ((matri&1L)==1L) {
            datarea[1][j]++;
          }
        }
        for (Entry<Byte, int[][]> ent:resp.entrySet()) {
          byte area = ent.getKey();
          int[][] dat = ent.getValue();
          for (int i=0;i<ies.n;i++)
            ps.println(ies.codigo+";"+area+";"+ies.semestres[i]+";"+dat[0][i]+";"+dat[1][i]+";"+dat[2][i]);
        }
      }
      ps.close();
    }*/
    /*{
      PrintStream ps = new PrintStream(new File("desercion_periodo_ies_area.csv"));
      ps.println("ies;area;per;valor");
      for (IES ies:kernel.listaIES) {
        Object[] res = kernel.getDesercionIntersemestral(new Filtro[]{new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")})}, new Variable[]{Variable.AREA_CONOCIMIENTO_EST});
        if (res==null) {
          System.out.println(ies.codigo);
          continue;
        }
        Map<byte[],double[]> resP = (Map<byte[], double[]>) res[1];
        for (Entry<byte[],double[]> ent:resP.entrySet()) {
          int ar = ent.getKey()[0]-1;
          double[] val = ent.getValue();
          for (int i=0;i<ies.n;i++)
            ps.println(ies.codigo+";"+ar+";"+ies.semestres[i]+";"+val[i]);
        }
      }
      ps.close();
    }*/
    /*{
      PrintStream ps = new PrintStream(new File("primiparos_ies_area_periodo.csv"));
      ps.println("ies;area;per;conteo");
      for (IES ies:kernel.listaIES) {
        System.out.println(ies.codigo);
        Map<Byte,int[]> res = new TreeMap<Byte, int[]>();
        for (Programa p:ies.programas)
          res.put(p.area, new int[ies.n]);
        res.put((byte)-1, new int[ies.n]);
        System.out.println(res);
        for (Estudiante e:ies.estudiantes) {
          int per = e.getSemestrePrimiparo(), ip=e.getIndicePrograma();
          byte area = (ip==-1)?-1:ies.programas[ip].area;
          //System.out.println(area + "\t"+per);
          res.get(area)[per]++;
        }
        for (Entry<Byte,int[]> ent:res.entrySet())
          for (int i=0;i<ies.n;i++)
            ps.println(ies.codigo+";"+ent.getKey()+";"+ies.semestres[i]+";"+ent.getValue()[i]);
          
      }
      ps.close();
    }*/
    /*{
      PrintStream ps = new PrintStream(new File("desper_area.csv"));
      for (IES ies:kernel.listaIES) {
        Object[] res = KernelSPADIES.getInstance().getDesercion(new Filtro[]{new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")})},new Variable[]{Variable.AREA_CONOCIMIENTO_EST,Variable.NIVEL_FORMACION_EST});
        if (res==null) continue;
        Map<byte[],double[]>  resP = (Map<byte[], double[]>) res[1];
        for (Entry<byte[],double[]> ent:resP.entrySet()) {
          ps.print(ies.codigo+";"+ent.getKey()[0]+";"+ent.getKey()[1]);
          for (double d:ent.getValue())
            ps.print(";"+d);
          ps.println();
        }
      }
      ps.close();
    }*/
    /*{
      PrintStream ps = new PrintStream(new File("programas.csv"));
      ps.println("ies;per;cod;nompies;cont");
      for (IES ies:kernel.listaIES) {
        int x=0;
        Map<Integer,int[]> res= new TreeMap<Integer, int[]>();
        for (Estudiante e:ies.estudiantes) {
          int pri = e.getSemestrePrimiparo();
          int gra = e.getSemestreGrado();
          long matri=e.getSemestresMatriculadoAlDerecho();
          int ip = e.getIndicePrograma();
          int[] arr = res.get(ip);
          if (arr==null) res.put(ip, arr=new int[ies.n]);
          for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1) if (j>=pri && (matri&1L)==1L) {
            arr[j]++;
          }
          x++;
        }
        for (Entry<Integer,int[]> ent:res.entrySet()) {
          int ip = ent.getKey();
          int[]arr = ent.getValue();
          String cp = ip==-1?"":new String(ies.programas[ip].codigoSNIES);
          String np = ip==-1?"":new String(ies.programas[ip].nombre);
          for (int j=0,jT=ies.n; j<jT; j++)
            ps.println(ies.codigo+";"+ies.semestres[j]+";"+cp+";"+np+";"+arr[j]);
        }
      }
      ps.close();
    }*/
    /*for(IES ies:kernel.listaIES) {
      int[][] re = new int[ies.n][4];
      for (Estudiante est:ies.estudiantes) {
        byte niv = (Byte) Variable.NIVEL_FORMACION_EST.getValor(est,ies);
        int pos = 0+(niv==1?1:0)+(niv==2?2:0)+(niv==4?3:0);
        re[est.getSemestrePrimiparo()][pos]++;
      }
      for (int i =0;i<ies.n;i++)
        for (int j = 0;j<4;j++)
          System.out.println(ies.codigo+";"+CajaDeHerramientas.codigoSemestreToString((byte) (ies.minCodigoSemestre+i))+";"+j+";"+re[i][j]);
    }*/
    System.exit(1);
    {
      PrintStream psx = new PrintStream(new File("solFranco.csv")),
        psp = new PrintStream(new File("solFrancoP.csv"));
      for(IES ies:kernel.listaIES) {
        for (int ii=-1,tt=ies.programas.length;ii<tt;ii++) {
          if (ii!=-1) psp.println(ies.codigo+";"+ii+";"+new String(ies.programas[ii].codigoSNIES)+";"+new String(ies.programas[ii].nombre));
          System.out.println(ies.codigo+"\t"+ii+"/"+tt);
          Filtro[] filtro = new Filtro[]{
              new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")}),
              new Filtro(Variable.AUX_INDPROG, new Item[]{new Item(ii,"","")})
          };
          try {
            Object[] resultado = ProcesadorDesercionPorPeriodo.obtenerResultado(false, new Variable[]{}, filtro);
            Map<byte[],int[]> resM=(Map<byte[], int[]>) resultado[0],
              resD=(Map<byte[], int[]>) resultado[1];
            Map<byte[],double[]> resT=(Map<byte[], double[]>) resultado[2];
            Integer[] codigosIESDif=(Integer[])(resultado[3]);
            String[] codigosProgramasDif=(String[])(resultado[4]);
            int tam = 0;
            for (int[] arrM:resM.values()) tam+= arrM.length;
            //int liminf = (Integer) res[6];
            //System.out.println(liminf);
            Variable[] diferenciados = new Variable[0];
            int limInf = (Integer) resultado[6]; 
            String encFilas[][]=new String[1+diferenciados.length][tam+1],encColumnas[]=new String[]{"NO graduados","Desertores","Deserción", "Retención"};
            String[][] valores=new String[tam][4];
            encFilas[0][0]="Periodo";
            for (int i=0,t=diferenciados.length;i<t;i++) encFilas[i+1][0]=diferenciados[i].nombre; 
            int fi = 0;
            for (byte[] llave:resM.keySet()) {
              int [] matr = resM.get(llave), des = resD.get(llave);
              double [] tasa = resT.get(llave);
              for (int i=0, t=matr.length;i<t;i++) {
                encFilas[0][fi+1] = CajaDeHerramientas.codigoSemestreToString((byte) (limInf+i));
                for (int j=0,tj=diferenciados.length;j<tj;j++)
                  encFilas[j+1][fi+1] = Variable.toString(diferenciados[j],llave[j],codigosIESDif,codigosProgramasDif);
                if (tasa[i]==0)
                  valores[fi] = new String[]{String.valueOf(matr[i]), String.valueOf(des[i]),"",""};
                else
                  valores[fi] = new String[]{String.valueOf(matr[i]), String.valueOf(des[i]),df_porcentaje.format(tasa[i]),df_porcentaje.format(1d-tasa[i])};
                fi++;
              }
            }
            InfoTabla it = new InfoTabla(valores,encFilas,encColumnas);
            int tx = it.getNumColumnas(), ty = it.getNumFilas();
            for (int y=0;y<ty;y++) {
              String per = it.getEncabezadoFila(y, 0);
              if ("2009-1".compareTo(per)>0 || "2010-1".compareTo(per)<0) continue;
              psx.println(ies.codigo+";"+ii+";"+per+";"+it.getValor(y, 1)+";"+it.getValor(y, 2));
              System.out.println(per);
            }
            /*ProcesadorDesercionPorPeriodo.instance.setParametros(false, new Variable[]{}, filtro);
            Collection<ResultadoConsulta> res = ProcesadorDesercionPorPeriodo.instance.generarGrafica();
            for (ResultadoConsulta r:res) if (r.nombre.startsWith("Tabla Dese")) {
              InfoTabla it = (InfoTabla) r.resultado;
              int tx = it.getNumColumnas(), ty = it.getNumFilas();
              for (int y=0;y<ty;y++) {
                System.out.println(it.getEncabezadoFila(y, 0));
              }
            }*/
          } catch (Exception e) {
            System.err.println(e.getMessage());
          }
        }
      }
    }
    /*Collection<Integer> col = new LinkedList<Integer>();
    for(IES ies:kernel.listaIES) col.add(ies.programas.length);
    System.out.println(Collections.min(col));
    System.out.println(Collections.max(col));*/
    /*Set<Byte> cars = new TreeSet<Byte>();
    for(IES ies:kernel.listaIES) cars.add(ies.caracter);
    System.out.println(cars);
    */
    /*{
      PrintStream ps = new PrintStream(new File("queies.csv"));
      for (IES ies:kernel.listaIES)
        ps.println(ies.codigo+";"+ies.caracter);
      ps.close();
    }*/
    /*{
      PrintStream ps = new PrintStream(new File("clee.csv"));
      for (IES ies:kernel.listaIES) {
        int x=0;
        for (Estudiante e:ies.estudiantes) {
          int pri = e.getSemestrePrimiparo();
          int gra = e.getSemestreGrado();
          long matri=e.getSemestresMatriculadoAlDerecho();
          int ip = e.getIndicePrograma();
          String np = ip==-1?"":new String(ies.programas[ip].nombre);
          for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1)
            if (j>=pri) {//ERROR ERROR
              ps.println(ies.codigo+";"+x+";"+np+";"+ies.semestres[j]+";"+(gra==-1?"":ies.semestres[gra])+";"+e.getIngresoHogar()); 
            }
          x++;
        }
      }
      ps.close();
    }*/
    /*{
      PrintStream ps = new PrintStream(new File("salidiego.csv"));
      ps.println("ies;puntaje;ingreso_hog;edad_icfes;educacion_madre;sexo");
      for (IES ies:kernel.listaIES) {
        for (Estudiante e:ies.estudiantes) {
          String np = e.getIndicePrograma()==-1?"":new String(ies.programas[e.getIndicePrograma()].nombre);
          String [] cmp = new String[]{ies.codigo+"",procs(e.getPuntajeICFES()),procs(e.getIngresoHogar()),procs(e.getEdadAlPresentarElICFES()),procs(e.getNivelEducativoMadre()),procs(e.getSexo()),np};
          for(String s:cmp) ps.print(s+";");
          ps.println();
        }
      }
      ps.close();
    }*/
    /*{
      Set<Integer> set = new TreeSet<Integer>(Arrays.asList(37,38,39));
      for (IES ies:kernel.listaIES) {
        for (Estudiante e:ies.estudiantes) {
          byte pic = e.getPuntajeICFES();
          if (pic!=-1 && set.contains(e.getPerIcfes())) {
            e.setPuntajeICFES(101-pic);
          }
        }
        IES.guardar(new File("datos_corr_x"), ies, true);
      }
    }*/
    /*{
      Set<Byte> s1 = new TreeSet<Byte>();
      Set<Integer> s2 = new TreeSet<Integer>();
      for (IES ies:kernel.listaIES)
        for (Estudiante e:ies.estudiantes) {
          s1.add(e.getPuntajeICFES());
          s2.add(e.getPerIcfes());
        }
      System.out.println(s1);
      System.out.println(s2);
    }*/
    System.exit(1);
    {
      PrintStream ps = new PrintStream(new File("cruceICFES_Programas.csv"));
      for (IES ies:kernel.listaIES) {
        Map<Integer, int[]> conteo = new TreeMap<Integer, int[]>();
        for (Estudiante e: ies.estudiantes) {
          int in = e.getIndicePrograma();
          int[] cp = conteo.get(in);
          if (cp==null) conteo.put(in, cp = new int[]{0,0});
          cp[0]++;
          if (e.getPuntajeICFES()!=-1) cp[1]++;
        }
        for (Entry <Integer, int[]> ent:conteo.entrySet()) {
          int in = ent.getKey();
          int[] cp = ent.getValue();
          ps.println(ies.codigo+";"+(in!=-1?ies.programas[in]:"-")+";"+cp[0]+";"+cp[1]);
        }
      }
    }
    System.exit(0);
    {
      IES ies = kernel.getIES(1119);
      Map<Byte,Integer> set = new TreeMap<Byte,Integer>();
      for (Estudiante e:ies.estudiantes) {
        e.setValorVariableExtra(5, 0);
        for (int i=0;i<=0;i++)
          for (int j=1;j<2;j++) {
            byte v = e.getValorVariableExtra(i,j);
            Integer pre = set.get(v);
            set.put(v,pre==null?1:(1+pre));
          }
      }
      System.out.println(set);
    }
    /*for (IES ies:kernel.listaIES) {
      System.out.println(ies.codigo);
      for (VariableExtra ve:ies.variablesExtrasD) {
        System.out.println("-"+new String(ve.nombre));
      }
    }*/
    System.exit(0);
    /*
    IES ies_ = IES.cargar(new File("w:/1833.spa"), true);
    ies_.nombre = "UNIVERSIDAD DEL SINU - ELIAS BECHARA ZAINUM - UNISINU -".getBytes();
    IES.guardar(new File("w:"),ies_,true);
    System.exit(0);
    */
    /*{
      for (Variable var:new Variable[]{
          Variable.AREA_CONOCIMIENTO_EST,Variable.NIVEL_FORMACION_EST, Variable.METODOLOGIA_EST,
          Variable.CLASIFICACION_ESTADO_EST, Variable.CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST,
          Variable.INGRESO_HOGAR_EST, Variable.NIVEL_EDUCATIVO_MADRE_EST, Variable.NUMERO_HERMANOS_EST, Variable.POSICION_ENTRE_LOS_HERMANOS_EST
         }) {
        System.out.println(var.nombre);
        for (Comparable c:var.rango.getRango()) {
          System.out.println(c + " " + var.rango.toString(c));
        }
        
      }
    }
    System.exit(1);*/
    /*
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,true);
    TablaDepartamentos.getInstance().preparar();
    ServerMatch.completarDatosIES_calcularRiesgoGenerico(kernel.getIES(1101), false);
    ServerMatch.completarDatosIES_calcularRiesgoGenerico(kernel.getIES(1101), true);
    System.exit(1);
    */
    /*for (IES ies: kernel.listaIES) {
      System.out.println(ies.codigo + ";" + new String(ies.nombre) + ";" +
          Variable.DEPARTAMENTO_IES.rango.toString(ies.departamento)+";"+
          Variable.MUNICIPIO_IES.rango.toString((int)ies.municipio)+";"+
          Variable.ORIGEN_IES.rango.toString(ies.origen)+";"+
          Variable.CARACTER_IES.rango.toString(ies.caracter)+";"
          );  
    }*/
    /*for (IES ies:kernel.listaIES) {
      int conteoTiene = 0;
      for (Estudiante e:ies.estudiantes) {
        if (e.getPuntajeICFES()!=-1) conteoTiene++;
        int ind = -1;
      }
      System.out.println(ies.codigo + ";" + conteoTiene + ";" + ies.estudiantes.length);
    }*/
    /*{
      PrintStream ps = new PrintStream(new File("Programas.csv"));
      for (String enc:new String[]{"IES","SNIES","NOMBRE","AREA","NIVEL"})
        ps.print(enc+";");
      ps.println();
      for (IES ies:kernel.listaIES) {
        for (Programa p: ies.programas) {
          ps.println(ies.codigo + ";" + new String(p.codigoSNIES) + ";" + new String(p.nombre)+ ";" + p.area+ ";" + p.nivel);
        }
      }
      ps.close();
    }*/    
    /*{
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
    }*/
    /*
    try {
      for (IES ies:kernel.listaIES) IES.guardar(new File("datos_red"), ies,false);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    */
    /*{
      PrintStream ps = new PrintStream("vistGrado.csv");
      for (IES ies: kernel.listaIES)
        for (Estudiante es: ies.estudiantes)
          if (es.getSemestreGrado()!=-1)
            ps.println(ies.semestres[es.getSemestrePrimiparo()]+";"+
                ies.semestres[es.getUltimoSemestreMatriculado()]+";"+
                ies.semestres[es.getSemestreGrado()]);
      ps.close();
    }*/
    /*
    {
      String[] encGeneral = "IES;IES_ESTUDIANTE;NUM_ESTUDIANTE;PROG_CODIGO;PROG_NOMBRE;EST_APE;EST_NOM;EST_DOC_TIPO;EST_DOC_NUM".split(";");
      SortedMap<ArchivoUN,PrintStream> oses = new TreeMap<ArchivoUN,PrintStream>();
      for (ArchivoUN ar:ArchivoUN.values()) {
        PrintStream ps = new PrintStream(ar.fSal);
        oses.put(ar, ps);
        for (String cmp:encGeneral) ps.print(cmp+";");
        for (String cmp:ar.encExtra) ps.print(cmp+";");
        ps.println();
      }
      int n=0;
      for (IES ies:kernel.listaIES) for (int i=0,t=ies.estudiantes.length;i<t;i++) {
        if (i==0) System.out.println(ies.codigo);
        Estudiante e = ies.estudiantes[i];
        Estudiante_DatosPersonales edp = ies.datosPersonalesEstudiantes[i];
        Programa prog = e.getIndicePrograma()==-1?null:ies.programas[e.getIndicePrograma()]; 
        String[] comunes = new String[] {
            String.valueOf(ies.codigo),String.valueOf(i),String.valueOf(n++),
            prog==null?"":new String(prog.codigoSNIES),
            prog==null?"":new String(prog.nombre),
            new String(edp.apellido),
            new String(edp.nombre),
            CajaDeHerramientas.tipoDocumentoToString(edp.tipoDocumento),
            (edp.documento==-1?"":("'"+edp.documento))
        };
        for (ArchivoUN ar:ArchivoUN.values()) {
          switch (ar) {
          case GRA: if (e.getSemestreGrado()==-1) continue; break;
          case DES: if (e.getEstado()!=-1) continue; break;
          }
          String [] linea = new String[encGeneral.length+ar.encExtra.length];
          Arrays.fill(linea, "");
          System.arraycopy(comunes, 0, linea, 0, comunes.length);
          PrintStream ps = oses.get(ar);
          switch (ar) {
          case PRI:
            linea[comunes.length+0] = ies.semestres[e.getSemestrePrimiparo()];
            linea[comunes.length+1] = CajaDeHerramientas.sexoToString(e.getSexo());
            linea[comunes.length+2] = ((edp.anhoFechaNacimiento==-1 || edp.mesFechaNacimiento==-1 || edp.diaFechaNacimiento==-1)?"":(CajaDeHerramientas.intToString(edp.diaFechaNacimiento,2)+"/"+CajaDeHerramientas.intToString(edp.mesFechaNacimiento,2)+"/"+CajaDeHerramientas.intToString(edp.anhoFechaNacimiento,4)));
            break;
          case MAT:
            {
              int jI=e.getSemestrePrimiparo();
              long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
              for (int j=jI; j<n; j++,matri>>>=1) if ((matri&1L)==1L) {
                linea[comunes.length+0] = ies.semestres[j];
                linea[comunes.length+1] = e.getRecibioApoyoAcademico(j)?"1":"0";
                linea[comunes.length+2] = e.getRecibioApoyoFinanciero(j)?"1":"0";
                linea[comunes.length+3] = e.getRecibioApoyoOtro(j)?"1":"0";
                for (String cmp:linea) {
                  ps.print(cmp+";");
                }
                ps.println();
              }
            }
            continue;
          case GRA:
            linea[comunes.length+0] = ies.semestres[e.getSemestreGrado()];
            break;
          case DES:
            linea[comunes.length+0] = ies.semestres[e.getUltimoSemestreMatriculado()];
            break;
          }
          for (String cmp:linea) {
            ps.print(cmp+";");
          }
          ps.println();
        }
      }
      for (PrintStream ps:oses.values()) ps.close();
    }*/
    //for (IES ies: kernel.listaIES) System.out.println(ies.codigo + ";" + CajaDeHerramientas.codigoSemestreToString((byte) ies.minCodigoSemestre) + ";" + CajaDeHerramientas.codigoSemestreToString((byte) ies.maxCodigoSemestre));
    /*
    SortedMap<Integer,Integer> sm = 
    for (IES ies:kernel.listaIES)
      for (Estudiante est:ies.estudiantes) {
        if (est.) sm.add(CajaDeHerramientas.codigoSemestreToString(codigo))
      }*/
    /*for (IES ies:kernel.listaIES) if (ies.datosPersonalesEstudiantes==null) System.out.println(ies.codigo);
    if (false) {
      Map<String, Double> relativo = new TreeMap<String, Double>();
      relativo.put("19981", 0.508795238);
      relativo.put("19982", 0.508795238);
      relativo.put("19991", 0.593781211);
      relativo.put("19992", 0.593781211);
      relativo.put("20001", 0.648587216);
      relativo.put("20002", 0.648587216);
      relativo.put("20011", 0.7053527);
      relativo.put("20012", 0.7053527);
      relativo.put("20021", 0.759268034);
      relativo.put("20022", 0.759268034);
      relativo.put("20031", 0.812352074);
      relativo.put("20032", 0.812352074);
      relativo.put("20041", 0.865079846);
      relativo.put("20042", 0.865079846);
      relativo.put("20051", 0.912641721);
      relativo.put("20052", 0.912641721);
      relativo.put("20061", 0.956937799);
      relativo.put("20062", 0.956937799);
      relativo.put("20071", 1.0);
      relativo.put("20072", 1.0);
      relativo.put("20081", 1.0);
      relativo.put("20082", 1.0);
      Map<Integer, Integer> costos2007IES = new TreeMap<Integer, Integer>(); 
      int[] pers = new int[]{
        0xFF&CajaDeHerramientas.getCodigoSemestre("20071"),
        0xFF&CajaDeHerramientas.getCodigoSemestre("20072")
      };
      BufferedReader br = new BufferedReader(new FileReader("C:/Documents and Settings/Adriana Rueda/Mis documentos/PromedioIES.csv"));
      br.readLine();
      for (String s=br.readLine();s!=null;s=br.readLine()) {
        //System.out.println(s);
        String[] sp = splitCSV(s);
        int cod = Integer.parseInt(sp[0]);
        int val = Integer.parseInt(sp[1]);
        costos2007IES.put(cod, val);
      }
      System.out.println(costos2007IES.size());
      for (IES ies:kernel.listaIES) {
        Integer precio = costos2007IES.get(ies.codigo);
        if (precio!=null) for (int i=0;i<ies.n;i++) {
          for (int j=0;j<ies.costosProgramas.length;j++) {
            if (ies.costosProgramas[j][i]==-1) ies.costosProgramas[j][i] = (int) (relativo.get(CajaDeHerramientas.getTextoSemestre((byte) (i+ies.minCodigoSemestre))) * precio);
          }
        }
        try {
          IES.guardar(new File("dt"), ies, true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }*/
    //escrituraDatos();
    /*{
      System.out.println("IES;DEPARTAMENTO");
      for (IES ies:kernel.listaIES)
        System.out.println(ies.codigo+";"+ies.departamento+";"+Variable.DEPARTAMENTO_IES.rango.toString(ies.departamento));
    }*/
    /*{
      System.out.println("AUX_IES;AUX_NUMEST;NIVEL_PROGRAMA");
      for (IES ies:kernel.listaIES)
        for (int i=0, nei = ies.estudiantes.length;i<nei;i++){
          int ip = ies.estudiantes[i].getIndicePrograma();
          byte nivel = ip==-1?-1:ies.programas[ip].nivel;
          System.out.println(ies.codigo+";"+i+";"+nivel);
        }
    }*/
    {
      PrintStream ps = new PrintStream(new File("DesercionCohorte.csv"));
      ps.println("TengoAfanLosEncabezadosDeberianSerInferiblesAPartirDeLosDatos");
      for (IES ies:kernel.listaIES) {
        Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
        Object[] res = kernel.getDesercionPorCohorte(new Filtro[]{filtro}, new Variable[]{});
        double[][] datos = (double[][]) res[1];
        
        for (int i=0;i<ies.n;i++) {
          ps.print(ies.codigo);
          ps.print(";");
          ps.print(ies.caracter);
          ps.print(";");
          ps.print(ies.semestres[i]);
          for (int j=0;j<ies.n;j++) {
            ps.print(";");
            if (datos[i][j]!=Double.MAX_VALUE) ps.print(datos[i][j]);
          }
          ps.println();
        }
      }
      ps.close();
    }
    /*{
      PrintStream ps = new PrintStream(new File("LQPHaider.csv"));
      ps.println("TengoAfanLosEncabezadosDeberianSerInferiblesAPartirDeLosDatos");
      int pers = 10;
      for (IES ies:kernel.listaIES) {
        int [] prims = new int[ies.n];
        for (Estudiante e:ies.estudiantes) prims[e.getSemestrePrimiparo()]++;
        //if (ies.codigo!=1712) continue;
        //if (ies.codigo!=1111) continue;
        Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
        Object[] res = kernel.getDesercionPorCohorte(new Filtro[]{filtro});
        double[][] datos = (double[][]) res[1];
        ps.print(ies.codigo);
        ps.print(";");
        ps.print(new String(ies.nombre));
        int ti = datos.length;
        int tj = datos[0].length;
        int n = 0;
        double suma = 0;
        if (tj<pers) ps.print(";");
        else for (int i=0;i<ti-2;i++) {
          if (prims[i]<=1) continue;
          double dat = datos[i][pers-1];
          if (dat!=Double.MAX_VALUE) {
            //System.out.println(dat);
            suma+=dat; 
            n++;
          }
        }
        if (n==0) ps.print(";");
        else ps.print(";"+(suma/n));
        ps.println();
      }
      ps.close();
    }*/
    /*{
      PrintStream ps = new PrintStream(new File("LoQuePïdeMartha_TasaDesercion.csv"));
      ps.println("ies;periodo;matriculadosConsiderados;desertores;tasa");
      for (IES ies:kernel.listaIES) {
        Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
        Object[] res = kernel.getTasaDesercion(new Filtro[]{filtro});
        int[] matriculadosConsiderados = (int[]) res[0];
        int[] desertores = (int[]) res[1];
        double [] tasa = (double[]) res[2];
        for (int i=0;i<ies.n;i++)
          ps.println(ies.codigo+";"+ies.semestres[i]+";"+matriculadosConsiderados[i]+";"+desertores[i]+";"+tasa[i]);
      }
      ps.close();
    }*/
    /*
    try {
      OperacionesAdministrativas.escribirCSVScompletos(kernel, new File("salidaCSV"));
    } catch (Exception e2) {
      e2.printStackTrace();
    }*/
    /*
    {
      Map<String, Double> relativo = new TreeMap<String, Double>();
      relativo.put("19981", 0.508795238);
      relativo.put("19982", 0.508795238);
      relativo.put("19991", 0.593781211);
      relativo.put("19992", 0.593781211);
      relativo.put("20001", 0.648587216);
      relativo.put("20002", 0.648587216);
      relativo.put("20011", 0.7053527);
      relativo.put("20012", 0.7053527);
      relativo.put("20021", 0.759268034);
      relativo.put("20022", 0.759268034);
      relativo.put("20031", 0.812352074);
      relativo.put("20032", 0.812352074);
      relativo.put("20041", 0.865079846);
      relativo.put("20042", 0.865079846);
      relativo.put("20051", 0.912641721);
      relativo.put("20052", 0.912641721);
      relativo.put("20061", 0.956937799);
      relativo.put("20062", 0.956937799);
      relativo.put("20071", 1.0);
      relativo.put("20072", 1.0);
      Map<String, Integer> costos2007 = new TreeMap<String, Integer>(); 
      int[] pers = new int[]{
        0xFF&CajaDeHerramientas.getCodigoSemestre("20071"),
        0xFF&CajaDeHerramientas.getCodigoSemestre("20072")
      };
      BufferedReader br = new BufferedReader(new FileReader("C:/Documents and Settings/Adriana Rueda/Mis documentos/Costos2007_codigo_recuperado.csv"));
      br.readLine();
      for (String s=br.readLine();s!=null;s=br.readLine()) {
        //System.out.println(s);
        String[] sp = splitCSV(s);
        String cod = sp[4];
        int val0 = Integer.parseInt(sp[8]);
        int val1 = Integer.parseInt(sp[9]);
        int val = Math.max(val0, val1);
        costos2007.put(cod, val);
        costos2007.remove("");
      }
      System.out.println(costos2007.size());
      for (IES ies:kernel.listaIES) {
        ies.costosProgramas = new int[ies.programas.length][ies.n];
        for (int[] costosPrograma: ies.costosProgramas)
          Arrays.fill(costosPrograma, -1);
        int i = 0;
        for (Programa p:ies.programas) {
          int[] costosPrograma = ies.costosProgramas[i++];
          Integer precio = costos2007.get(new String(p.codigoSNIES));
          if (precio==null) continue;
          //for (int per:new int[]{pers[0]-ies.minCodigoSemestre,pers[1]-ies.minCodigoSemestre}) {
          for (int per=0;per<ies.n;per++) {
            //System.out.println("OK"+per);
            //if (0<=per && per<ies.n) costosPrograma[per] = precio;
            if (0<=per && per<ies.n) costosPrograma[per] = (int) Math.floor(precio*relativo.get(CajaDeHerramientas.getTextoSemestre((byte) per)));
          }
        }
        try {
          IES.guardar(new File("dt"), ies, true);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
*/
    /*for (IES ies:kernel.listaIES)
      for (Programa p:ies.programas ) {
        System.out.println(ies.codigo+";'"+new String(p.codigoSNIES)+";"+new String(p.nombre)+";"+p.area+";"+p.duracion+";"+p.jornada+";"+p.nivel+";"+p.nucleo);
      }*/
    /*{
      PrintStream ps = new PrintStream("2102-20042.csv");
      for (IES ies:kernel.listaIES) {
        if (ies.codigo!=2102) continue;
        for (int i=0,t=ies.estudiantes.length;i<t;i++) {
          Estudiante_DatosPersonales dp = ies.datosPersonalesEstudiantes[i];
          if (ies.estudiantes[i].getSemestrePrimiparo()==13) ps.println(new String(dp.apellido)+new String(dp.nombre)+";"+dp.documento);
        }
      }
      ps.close();
    }*/
    /*for (IES ies:kernel.listaIES)
      try {
        IES.guardar(new File("datosWeb"), ies, true);
      } catch (Exception e3) {
        // TODO Auto-generated catch block
        e3.printStackTrace();
      }*/
    //for (IES ies:kernel.listaIES) if (ies.departamento!=11) System.out.println(ies.codigo);
    /*for (IES ies:kernel.listaIES) {
      Set<Integer> nivs = new TreeSet<Integer>();
      for (Programa p:ies.programas) nivs.add(p.nivel&0xFF);
      System.out.println(ies.codigo+";"+ies.caracter+";"+nivs);
    }*/
    /*{
      BufferedReader br = new BufferedReader(new FileReader("paMD5.txt"));
      try {
        for (String s=br.readLine();s!=null;s=br.readLine()) {
          System.out.println(CajaDeHerramientas.md5(s).toUpperCase());
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }*/
    
    {
      String noms[] = {"ANDRES","MARTA","BOORIS","HAIDER","LUIS","ALVARO","ELKIN","PALOMA","LINA"};
      String apes[] = {"RUEDA","CORDOBA","PIRANEQUE","JAIME","HERRERA","GARAVITO","LOPEZ","MESA","MORENO","SANCHEZ","SOTELO","GUZMAN","FRANCO"};
      //IES ies = kernel.getIES(1101);
      System.out.println("X");
      IES ies = kernel.getIES(1712);
      ies.codigo = 9999;
      ies.nombre = "IES EJEMPLO".getBytes();
      ies.departamento = 11;
      ies.municipio = 1;
      int i = 0;
      for (Estudiante_DatosPersonales edp: ies.datosPersonalesEstudiantes) {
        edp.nombre = noms[i%noms.length].getBytes();
        edp.apellido = apes[i%apes.length].getBytes();
        edp.documento = 99999999-i;
        edp.codigo = String.valueOf(edp.documento).getBytes();
        i++;
      }
      int j = 0;
      for (Programa prog: ies.programas) {
        prog.nombre = ("PROG"+(++j)).getBytes();
      }
      VariableExtra varEx = new VariableExtra();
      varEx.nombre = "Suerte".getBytes();
      varEx.nombresValores = new byte[][]{"Si".getBytes()};
      ies.variablesExtras[0] = varEx;
      try {
        IES.guardar(new File("."), ies, true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    /*{
      Byte[] semAct = kernel.getSemestresActivos(new Filtro[]{});
      int tam = semAct.length-1;
      Map<String, Integer> conteo =  new TreeMap<String, Integer>();
      for (IES ies:kernel.listaIES) {
        //int [] matricula = new int[ies.n];
        int [] matricula = new int[tam];
        //Arrays.fill(a, val)
        for (Estudiante e:ies.estudiantes) {
          int pri = e.getSemestrePrimiparo();
          //byte[] repr = new byte[ies.maxCodigoSemestre-pri+1];
          StringBuffer repr = new StringBuffer(ies.maxCodigoSemestre-pri+1);
          long matri=e.getSemestresMatriculadoAlDerecho();
          for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1)
            if (j>=pri) {
              repr.append((matri&1L)==1L?1:0); 
            }
          String key = repr.toString();
          Integer previo = conteo.get(key);
          conteo.put(key, 1+(previo==null?0:previo.intValue()));
        }
      }
      System.out.println(conteo);
    }*/
    //for (IES ies:kernel.listaIES) System.out.println(ies.codigo + ";" + ies.estudiantes.length);
    //for (IES ies:kernel.listaIES) for (Programa p:ies.programas) System.out.println(p.nombre.length);
    //final File csvIndividuosIes = new File("expIES.csv");
    //escrituraArchivoIndividuosIes(kernel, csvIndividuosIes);
    /*
    for (IES ies:kernel.listaIES)
      System.out.println(ies.codigo+"\t"+ies.semestres[0]+"\t"+ies.semestres[ies.semestres.length-1]);
    */
    /*{
      Byte[] semAct = kernel.getSemestresActivos(new Filtro[]{});
      int tam = semAct.length-1;
      for (IES ies:kernel.listaIES) {
        //int [] matricula = new int[ies.n];
        int [] matricula = new int[tam];
        Arrays.fill(matricula, -1);
        //Arrays.fill(a, val)
        for (Estudiante e:ies.estudiantes) {
          long matri=e.getSemestresMatriculadoAlDerecho();
          for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
            matricula[ies.minCodigoSemestre+j]++;
          }
        }
        String resto = "";
        for (int i=0;i<tam;i++) resto+=";"+matricula[i];
        System.out.println(ies.codigo + ";" + new String(ies.nombre) + resto);
      }
    }*/
    /*{
      for (IES ies:kernel.listaIES) {
        int [] matricula = new int[ies.n];
        int [] desertoresUltimo = new int[ies.n];
        for (Estudiante e:ies.estudiantes) {
          long matri=e.getSemestresMatriculadoAlDerecho();
          for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
            matricula[j]++;
          }
        }
        for (int i=0;i<ies.n;i++) {
          if (matricula[i]<=1) System.out.println(ies.codigo+"\t"+ies.semestres[i]+"\t"+matricula[i]);
        }
      }

    }*/
    /*
    {
      PrintStream ps = new PrintStream("Porololo.csv");
      for (IES ies:kernel.listaIES) {
        int [] matricula = new int[ies.n];
        int [] desertoresUltimo = new int[ies.n];
        for (Estudiante e:ies.estudiantes) {
          if (e.getEstado()==1) continue;
          if (e.getEstado()==-1) desertoresUltimo[e.getUltimoSemestreMatriculado()]++;
          long matri=e.getSemestresMatriculadoAlDerecho();
          for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
            matricula[j]++;
          }
        }
        for (int i=0;i<ies.n;i++) {
          ps.println(ies.codigo+";"+ies.semestres[i]+";"+matricula[i]+";"+desertoresUltimo[i]);
        }
      }
      ps.close();
    }
    */
    //medicionImpactoVars(kernel);
    //exportarProgramasIES(kernel, new TreeSet<Integer>(Arrays.asList(new Integer[]{1101, 1824,4111,1830,2847})));
    //exportarProgramasIES(kernel, new TreeSet<Integer>(Arrays.asList(new Integer[]{1812})));
    //for (IES ies:kernel.listaIES) if (ies.codigo==1101) {System.out.println(ies.municipio);System.out.println(ies.departamento);}
    //for (IES ies:kernel.listaIES) if (ies.departamento==11 && ies.municipio==1) System.out.println(ies.codigo);
    /*
    for (IES ies:kernel.listaIES) if (ies.codigo==2812) for (int i=0, t=ies.estudiantes.length;i<t;i++) {
      Estudiante e = ies.estudiantes[i];
      Estudiante_DatosPersonales edp = ies.datosPersonalesEstudiantes[i];
      if (!ies.semestres[e.getSemestrePrimiparo()].equals("20002")) continue;
      System.out.println(ies.semestres[e.getSemestrePrimiparo()]+"\t"+new String(edp.nombre) +"\t" + edp.documento);      
    }*/
    /*for (IES ies:kernel.listaIES) {
      String nomMun = Variable.MUNICIPIO_IES.rango.toString(new Integer((int)(ies.departamento*1000+ies.municipio)));
      System.out.println(ies.codigo + "\t" + new String(ies.nombre) + "\t" + nomMun);
    }*/
    /*{
      TreeMap<Integer, List<Integer>> res = new TreeMap<Integer, List<Integer>>();
      for (int i:new int[]{1,2,3,4,5,9}) res.put(i, new LinkedList<Integer>());
      for (IES ies: kernel.listaIES) res.get(0xFF&ies.caracter).add(ies.codigo);
      System.out.println(res);
    }*/
    /*{
      PrintStream ps = new PrintStream("resumenPuntajes.csv"); 
      for (IES ies: kernel.listaIES) {
        for (Estudiante e: ies.estudiantes) {
          ps.println(ies.codigo + ";" + ies.semestres[0] + ";" + ies.semestres[ies.semestres.length-1] + ";" + ies.semestres[e.getSemestrePrimiparo()] +";" + e.getPuntajeICFES());
        }
      }
      ps.close();
    }*/
    System.exit(1);
    if (false) {
      IES ies = kernel.getIES(2832);
      ies.nombre = "UNIVERSIDAD DE SANTANDER".getBytes();
      try {
        IES.guardar(new File("."), ies, true);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    if (false) try {
      OperacionesAdministrativas.escribirCSVScompletos(kernel, new File("../salidaCSV"));
    } catch (Exception e2) {
      e2.printStackTrace();
    }
    //System.exit(1);
    //exportarProgramasIES(kernel, new TreeSet<Integer>(Arrays.asList(new Integer[]{1812})));
    //System.exit(0);
    if (false) {//Anonimizador
      int numies = 1;
      for (int cies:new int[]{3713,1714}) {
        IES ies = KernelSPADIES.getInstance().getIES(cies);
        ies.nombre = ("IES " + (numies)).getBytes();
        ies.codigo = numies++;
        try {
          IES.guardar(new File("."), ies, true);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    /*{
      System.out.println("UF");
      for (IES ies:KernelSPADIES.getInstance().listaIES)
        if (ies.departamento==11 && ies.municipio==1) System.out.println(ies.codigo);
    }*/
    if (false) for (IES ies: kernel.listaIES) {
      if(ies.origen==1 || ies.origen==2 ||ies.origen==3) {
        System.out.println(ies.codigo);
      }
    }
    if (false) {//Mutilador retiros forzosos
      for (IES ies:KernelSPADIES.getInstance().listaIES) {
        if (Arrays.asList(1204).contains(ies.codigo)) {
          for (Estudiante est: ies.estudiantes) {
            est.setSemestreRetiroForzoso(-1);
          }
          try {
            IES.guardar(new File("."), ies, true);
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }
    if (false) {//Asgiunador nucleos a mano, obsoleto
      try {
        Map<String, Integer> nucMap = new TreeMap<String, Integer>(); 
        BufferedReader br = new BufferedReader(new FileReader("C:/Documents and Settings/Adriana Rueda/Escritorio/progNuc.csv"));
        br.readLine();
        for (String s = br.readLine();s!=null;s = br.readLine()) {
          String[] spl = CajaDeHerramientas.csvToString(s, 0, ';');
          /*
          String ano = spl[0].substring(0,4),
            per = spl[0].substring(4,5);
          */
          String ies = spl[0],
            cod = spl[3];
          String sNuc = spl[7].trim();
          Integer nuc = sNuc.length()==0?-1:Integer.parseInt(sNuc);
          if (nuc<0 || cod.length()<10) continue;
          nucMap.put(cod, nuc);
        }
        br.close();
        for (IES ies:KernelSPADIES.getInstance().listaIES) {
          for (Programa pro:ies.programas) {
            if (pro.nucleo==0) pro.nucleo = -1;
            Integer nuc = nucMap.get(new String(pro.codigoSNIES));
            if (nuc!=null) {
              pro.nucleo = (byte)(int)nuc;
              System.out.println(pro.nucleo);
            }
          }
          try {
            IES.guardar(new File("datos_nucleo"), ies, true);
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
        System.out.println("X");
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
    }
    if (false) {//Anonimizador2
      int codnIes = 0;
      List<Integer> iesAAnom = Arrays.asList(1101,1106,1120,1201,1203,1707,1710,1714,1722,2825,2832);
      for (IES ies:kernel.listaIES) {
        if (!iesAAnom.contains(ies.codigo)) continue;
        ies.codigo = ++codnIes;
        ies.nombre = ("ANOM"+codnIes).getBytes();
        try {
          IES.guardar(new File("."), ies, true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    if (false) {//Calculador manual de riego, obsoleto
      try {
        TablaDepartamentos.getInstance().preparar();
      } catch (Exception e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      for (IES ies: kernel.listaIES) {
        try {
          ServerMatch.completarDatosIES_calcularRiesgoGenerico(ies);
/*          ServerMatch.completarDatosIES_calcularRiesgoGenerico(ies, true);
          ServerMatch.completarDatosIES_calcularRiesgoGenerico(ies, false);*/
          IES.guardar(new File("datConMod"), ies, true);
          System.out.println(ies.codigo);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      System.out.println("ug");
    }
    if (false) {//Generacion proemdios temp, toca obsoletiarlo
      System.out.println("X");
      int
      cgenero=0,
      cedad=0,
      ctrabaja=0,//0
      cingreso=0,//0
      chermanos=0,//0
      ctotalicfes=0,
      ca=0,
      cmadniv=0,
      cnumerohijo=0,//0
      cviviendapropia=0,
      repitencia=0;
      double
      genero=0,
      edad=0,
      trabaja=0,//0
      ingreso=0,//0
      hermanos=0,//0
      totalicfes=0,
      a1=0,
      a2=0,
      a3=0,
      a6=0,
      a8=0,
      a9=0,
      madniv1=0,//0
      madniv2=0,//0
      numerohijo=0,//0
      viviendapropia=0,
      crepitencia = 0;
      for (IES ies: kernel.listaIES) {
        for (Estudiante e: ies.estudiantes) {
          int sexo=e.getSexo(),edadPresentacionIcfes=e.getEdadAlPresentarElICFES(),trabajabaCuandoIcfes=e.getTrabajabaCuandoPresentoIcfes();
          int ingresoHogar=e.getIngresoHogar(),numeroHermanos=e.getNumeroHermanos(),puntajeIcfes=e.getPuntajeICFES();
          int educacionMadre=e.getNivelEducativoMadre(),posicionEntreHermanos=e.getPosicionEntreLosHermanos(),viviendaPropia=e.getViviendaPropia();
          int indicePrograma=e.getIndicePrograma();
          int areaPrograma=(indicePrograma==-1)?-1:ies.programas[indicePrograma].area;
          if (sexo!=-1) {genero+=sexo; cgenero++;}
          if (edadPresentacionIcfes!=-1) {edad=edadPresentacionIcfes; cedad++;}
          if (trabajabaCuandoIcfes!=-1) {trabaja+=trabajabaCuandoIcfes; ctrabaja++;}
          if (ingresoHogar!=-1) {ingreso+=ingresoHogar; cingreso++;}
          if (numeroHermanos!=-1) {hermanos+=numeroHermanos; chermanos++;}
          if (puntajeIcfes!=-1) {totalicfes+=puntajeIcfes; ctotalicfes++;}
          if (areaPrograma!=-1) {
            ca++;
            a1+=areaPrograma==1?1:0;
            a2+=areaPrograma==2?1:0;
            a3+=areaPrograma==3?1:0;
            a6+=areaPrograma==6?1:0;
            a8+=areaPrograma==8?1:0;
            a9+=areaPrograma==9?1:0;                
          }
          if (educacionMadre!=-1) {
            cmadniv++;
            madniv1+=educacionMadre==1?1:0;//0
            madniv2+=educacionMadre==2?1:0;//0
          }
          if (posicionEntreHermanos!=-1) {numerohijo+=posicionEntreHermanos; cnumerohijo++;}
          if (viviendaPropia!=-1) {viviendapropia+=viviendaPropia;cviviendapropia++;}

          long matri=e.getSemestresMatriculadoAlDerecho();
          double repitencias[]=e.getRepitencias();
          for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
            double repitenciaL=repitencias[j];
            if (repitenciaL!=-1d) {
              repitencia+=repitenciaL;
              crepitencia++;
            }
          }
        }
      }
      System.out.println(genero);
      System.out.println(cgenero);
      genero/=cgenero;
      edad/=cedad;
      trabaja/=ctrabaja;
      ingreso/=ctrabaja;
      hermanos/=chermanos;
      totalicfes/=ctotalicfes;
      a1/=ca;
      a2/=ca;
      a3/=ca;
      a6/=ca;
      a8/=ca;
      a9/=ca;
      madniv1/=cmadniv;
      madniv2/=cmadniv;
      numerohijo/=cnumerohijo;
      viviendapropia/=cviviendapropia;
      repitencia/=crepitencia;
      try {
        PrintStream ps = new PrintStream(new File("valoresPredeterminadosModelo.csv"));
        ps.println(genero);
        ps.println(edad);
        ps.println(trabaja);
        ps.println(ingreso);
        ps.println(hermanos);
        ps.println(totalicfes);
        ps.println(a1);
        ps.println(a2);
        ps.println(a3);
        ps.println(a6);
        ps.println(a8);
        ps.println(a9);
        ps.println(madniv1);
        ps.println(madniv2);
        ps.println(numerohijo);
        ps.println(viviendapropia);
        ps.println(repitencia);            
        ps.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      System.out.println("C");
    }
    //if (true) return;
    //escrituraGraduados();
  }

  public static void exportarProgramasIES(KernelSPADIES kernel, Collection<Integer> instituciones) throws FileNotFoundException {
    String[] enc = new String[]{"Nombre", "CodSNIES", "Area", "Nucleo", "Nivel"};
    for (Integer codIES: instituciones) {
      PrintStream ps = new PrintStream("programas_"+codIES+".csv");
      ps.println(CajaDeHerramientas.stringToCSV(enc));
      for (Programa pr: kernel.getIES(codIES).programas) {
        String[] reg = new String[]{new String(pr.nombre), new String(pr.codigoSNIES), ""+pr.area, ""+pr.nucleo, ""+pr.nivel};
        ps.println(CajaDeHerramientas.stringToCSV(reg));
      }
      ps.close();
    }
  }
  public static void medicionImpactoVars(KernelSPADIES kernel) throws FileNotFoundException, MyException {
    {
      Variable[] vars = new Variable[]{
          Variable.SEXO_EST,
          Variable.TRABAJABA_CUANDO_ICFES_EST,
          Variable.CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST,
          Variable.INGRESO_HOGAR_EST,
          Variable.VIVIENDA_PROPIA_EST,
          Variable.NUMERO_HERMANOS_EST,
          Variable.POSICION_ENTRE_LOS_HERMANOS_EST,
          Variable.CLASIFICACION_PUNTAJE_ICFES_EST,
          Variable.NIVEL_EDUCATIVO_MADRE_EST,
          Variable.NUMERO_APOYOS_ACADEMICOS_RECIBIDOS_EST,
          Variable.NUMERO_APOYOS_FINANCIEROS_RECIBIDOS_EST,
          Variable.NUMERO_APOYOS_OTROS_RECIBIDOS_EST,
          Variable.NUMERO_APOYOS_ICETEX_RECIBIDOS_EST,
      };
      for (IES ies:kernel.listaIES) {
        //if (ies.codigo!=1101) continue;
        Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
        PrintStream ps = new PrintStream("sals_csv/"+ies.codigo+".csv");
        for (Variable var:vars) {
          Object[] res = kernel.getConteoPoblacion(new Filtro[]{filtro}, new Variable[]{var});
          //Object[] res = kernel.getConteoPoblacion(new Filtro[]{}, new Variable[]{var});
          if (res==null) {
            System.err.println(ies.codigo + "\t" + var.nombre);
            continue;
          }
          Map<byte[], int[]> resC = (Map<byte[], int[]>) res[0];
          Map<byte[], double[]> resP = (Map<byte[], double[]>) res[1];
          Integer tam = (Integer) res[4];
          //System.out.println(var.nombre);
          //String nomVal = var.rango.toString(var.rango.byteToRango(ent.getKey()[0]));
          //System.err.println(resP.size() + "\t" + resC.size());
          int numSeries = resP.size();
          double [] varInic = new double[numSeries];
          double[][] tabP = new double[numSeries][tam];//Torcido mejoraria eficiencia
          int[][] tabC = new int[numSeries][tam];
          byte[][] tabInd = new byte[numSeries][]; 
          {
            int serie = 0;
            for (Entry<byte[], double[]> ent: resP.entrySet()) {
              varInic[serie] = resC.get(ent.getKey())[0];
              tabInd[serie] = ent.getKey();
              int ti = 0;
              int[] serieC = resC.get(ent.getKey());
              for (double x: ent.getValue()) {
                tabC[serie][ti] = serieC[ti];
                tabP[serie][ti] = x;
                ti++;
              }
              serie++;
            }
          }

          int serieMayor = -1;
          int ocurrenciasSerieMayor = -1;
          {
            int [] numMayorias = new int[numSeries]; Arrays.fill(numMayorias, 0);
            for (int i=0;i<tam;i++) {
              int iMax = -1;
              double max = Double.MIN_VALUE;
              for (int j = 0;j<numSeries;j++) {
                if (tabP[j][i]>max) {
                  max = tabP[j][i];
                  iMax = j;
                }
              }
              if (iMax!=-1 && max>0) numMayorias[iMax]++;
            }
            {
              int iSerieMayor = -1;
              int max = Integer.MIN_VALUE;
              for (int i=0;i<numSeries;i++) if (numMayorias[i]>max) {
                max = numMayorias[i];
                iSerieMayor = i;
              }
              serieMayor = iSerieMayor;
              ocurrenciasSerieMayor = max;
            }
          }
          //String nomVal = var.rango.toString(var.rango.byteToRango(ent.getKey()[0]));
          //ps.println(var.nombre + ";" + var.rango.toString(var.rango.byteToRango(tabInd[serieMayor][0])) + ";" + ocurrenciasSerieMayor + ";" +tam);
          double valTo = 0;
          for (int i=0;i<numSeries;i++) {
            String[] linea =  new String[tam+1];
            linea[0] = var.rango.toString(var.rango.byteToRango(tabInd[i][0]));
            if (linea[0].equals(Constantes.S_DESCONOCIDO)) continue;
  
            for (int j=0;j<tam;j++) {
              //linea[j+1] = String.valueOf(varInic[i] * (tabP[serieMayor][j] - tabP[i][j])); 
              //linea[j+1] = String.valueOf(Math.round(varInic[i] * (tabP[serieMayor][j] - tabP[i][j])));
              //linea[j+1] = String.valueOf(tabC[i][j] * (tabP[serieMayor][j] - tabP[i][j])).replace('.', ',');
              valTo+=tabC[i][j] * (tabP[serieMayor][j] - tabP[i][j]);
            }
            //for (String campo: linea) ps.print(campo+";"); ps.println();
          }
          ps.println(var.nombre + ";" + var.rango.toString(var.rango.byteToRango(tabInd[serieMayor][0])) + ";" + ocurrenciasSerieMayor + ";" +tam + ";" + String.valueOf(valTo).replace('.', ','));
          //ps.println();
          /*
          System.out.println(tam);
          System.out.println("_"+resC.size());
          for (Entry<byte[], int[]> ent: resC.entrySet()) {
            System.out.println(ent.getKey().length + " " + ent.getValue().length);
          }
          System.out.println("%"+resP.size());
          for (Entry<byte[], double[]> ent: resP.entrySet()) {
            System.out.println(ent.getKey().length + " " + ent.getValue().length);
          }
          */
        }
        ps.close();
      }
    }
  }
  
  public static void medicionImpactoVars__TEST(KernelSPADIES kernel) throws FileNotFoundException, MyException {
    {
      Variable[] vars = new Variable[]{
          Variable.CLASIFICACION_PUNTAJE_ICFES_EST,
          Variable.NIVEL_EDUCATIVO_MADRE_EST,
          Variable.VIVIENDA_PROPIA_EST,
          Variable.NUMERO_HERMANOS_EST,
          Variable.CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST,
          Variable.AREA_CONOCIMIENTO_EST,
          //Variable.PERIODO_INGRESO_EST,
      };
      for (IES ies:kernel.listaIES) {
        //if (ies.codigo!=1101) continue;
        Filtro filtro = new Filtro(Variable.CODIGO_IES, new Item[]{new Item(ies.codigo,"","")});
        PrintStream ps = new PrintStream("sals_csv/"+ies.codigo+".csv");
        for (Variable var:vars) {
          Object[] res = kernel.getConteoPoblacion(new Filtro[]{filtro}, new Variable[]{var});
          if (res==null) {
            System.err.println(ies.codigo + "\t" + var.nombre);
            continue;
          }
          Map<byte[], int[]> resC = (Map<byte[], int[]>) res[0];
          Map<byte[], double[]> resP = (Map<byte[], double[]>) res[1];
          Integer tam = (Integer) res[4];
          //System.out.println(var.nombre);
          //String nomVal = var.rango.toString(var.rango.byteToRango(ent.getKey()[0]));
          //System.err.println(resP.size() + "\t" + resC.size());
          int numSeries = resP.size();
          double [] varInic = new double[numSeries];
          double[][] tabP = new double[numSeries][tam];//Torcido mejoraria eficiencia
          int[][] tabC = new int[numSeries][tam];
          byte[][] tabInd = new byte[numSeries][]; 
          {
            int serie = 0;
            for (Entry<byte[], double[]> ent: resP.entrySet()) {
              varInic[serie] = resC.get(ent.getKey())[0];
              tabInd[serie] = ent.getKey();
              int ti = 0;
              int[] serieC = resC.get(ent.getKey());
              for (double x: ent.getValue()) {
                tabC[serie][ti] = serieC[ti];
                tabP[serie][ti] = x;
                ti++;
              }
              serie++;
            }
          }
          
       
          int serieMayor = -1;
          int ocurrenciasSerieMayor = -1;
          {
            int [] numMayorias = new int[numSeries]; Arrays.fill(numMayorias, 0);
            for (int i=0;i<tam;i++) {
              int iMax = -1;
              double max = Double.MIN_VALUE;
              for (int j = 0;j<numSeries;j++) {
                if (tabP[j][i]>max) {
                  max = tabP[j][i];
                  iMax = j;
                }
              }
              if (iMax!=-1 && max>0) numMayorias[iMax]++;
            }
            {
              int iSerieMayor = -1;
              int max = Integer.MIN_VALUE;
              for (int i=0;i<numSeries;i++) if (numMayorias[i]>max) {
                max = numMayorias[i];
                iSerieMayor = i;
              }
              serieMayor = iSerieMayor;
              ocurrenciasSerieMayor = max;
            }
          }
          //String nomVal = var.rango.toString(var.rango.byteToRango(ent.getKey()[0]));
          ps.println(var.nombre + ";" + var.rango.toString(var.rango.byteToRango(tabInd[serieMayor][0])) + ";" + ocurrenciasSerieMayor + ";" +tam);
          for (int i=0;i<numSeries;i++) {
            String[] linea =  new String[tam+1];
            linea[0] = var.rango.toString(var.rango.byteToRango(tabInd[i][0]));  
            for (int j=0;j<tam;j++) {
              //linea[j+1] = String.valueOf(varInic[i] * (tabP[serieMayor][j] - tabP[i][j])); 
              //linea[j+1] = String.valueOf(Math.round(varInic[i] * (tabP[serieMayor][j] - tabP[i][j])));
              linea[j+1] = String.valueOf(tabC[i][j] * (tabP[serieMayor][j] - tabP[i][j])).replace('.', ',');
            }
            for (String campo: linea) ps.print(campo+";"); ps.println();
          }
          ps.println();
          /*
          System.out.println(tam);
          System.out.println("_"+resC.size());
          for (Entry<byte[], int[]> ent: resC.entrySet()) {
            System.out.println(ent.getKey().length + " " + ent.getValue().length);
          }
          System.out.println("%"+resP.size());
          for (Entry<byte[], double[]> ent: resP.entrySet()) {
            System.out.println(ent.getKey().length + " " + ent.getValue().length);
          }
          */
        }
        ps.close();
      }
    }
  }
  
  public static final String formateoCampoNum(int num) {
    return num==-1?"":String.valueOf(num);
  }
  
  public static final String formateoCampoNum(double num) {
    return num==-1.0?"":String.valueOf(num);
  }
  
  public static void escrituraDatos() {
    try {
      KernelSPADIES kernel=KernelSPADIES.getInstance();
      int maxN = Integer.MIN_VALUE;
      for (IES ies: kernel.listaIES) {
        //ies.n             
      }
      String[] encabezado = new String[]{
          "ies",
          "numero_estudiante",
          "sexo",
          "edad_icfes",
          "edu_madre_icfes",
          "ing_hogar_icfes",
          "viv_propia_icfes",
          "num_hermanos_icfes",
          "pos_hermanos_icfes",
          "puntaje_icfes",
          "trabajaba_icfes",
          "prim_sem",
          "grado_per",
          "retiro_per",
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
          //TODO implementacion LAZY :P :( Eventualmente "fixear" je
          "p1","p2","p3","p4","p5","p6","p7","p8","p9","p10",
          "p11","p12","p13","p14","p15","p16","p17","p18","p19","p20","p21",
      };
      String[] linea = new String[encabezado.length];
      Arrays.fill(linea, "");
      PrintStream ps = new PrintStream("vistaIES.csv");
      ps.println(CajaDeHerramientas.stringToCSV(encabezado));
      for (IES ies: kernel.listaIES) {
        System.out.println("*" + ies.codigo);
        //PrintStream ps = new PrintStream(ies.codigo + "_completo.csv");
        //ps.println(CajaDeHerramientas.stringToCSV(encabezado));
        int nes = 0;
        for (Estudiante e:ies.estudiantes) {
          int pos = 0;
          linea[pos++] = String.valueOf(ies.codigo);
          linea[pos++] = String.valueOf(nes);
          linea[pos++] = formateoCampoNum(e.getSexo());
          linea[pos++] = formateoCampoNum(e.getEdadAlPresentarElICFES());
          linea[pos++] = formateoCampoNum(e.getNivelEducativoMadre());
          linea[pos++] = formateoCampoNum(e.getIngresoHogar());
          linea[pos++] = formateoCampoNum(e.getViviendaPropia());
          linea[pos++] = formateoCampoNum(e.getNumeroHermanos());
          linea[pos++] = formateoCampoNum(e.getPosicionEntreLosHermanos());
          linea[pos++] = formateoCampoNum(e.getPuntajeICFES());
          linea[pos++] = formateoCampoNum(e.getTrabajabaCuandoPresentoIcfes());
          linea[pos++] = ies.semestres[e.getSemestrePrimiparo()];
          int g = e.getSemestreGrado();
          linea[pos++] = g==-1?"":ies.semestres[g];
          int rf = e.getSemestreRetiroForzoso();
          linea[pos++] = rf==-1?"":ies.semestres[rf];
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
            for (int j2=0; j2<jT; j2++) {
              linea[npos++] = j2==j?"1":"0";
            }
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
  
  public static void escrituraGraduados() {
    try {
      KernelSPADIES kernel=KernelSPADIES.getInstance();
      int maxN = kernel.getSemestresActivos(new spadies.util.variables.Filtro[]{}).length;
      String[] encabezado = new String[]{
          "ies_origen",
          "ies_caracter",
          "ies_departamento",
          "ies_municipio",
          "estudiante_numero",
          "sexo",
          "edad_icfes",
          "edu_madre_icfes",
          "ing_hogar_icfes",
          "viv_propia_icfes",
          "num_hermanos_icfes",
          "pos_hermanos_icfes",
          "puntaje_icfes",
          "trabajaba_icfes",
          "prim_sem",
          "grado_per",
          "areaD","area1","area2","area3","area4","area5","area6","area7","area8","area9",
          "materias_tomadas_total",
          "materias_aprobadas_total",
          "apoyos_aca",
          "apoyos_fin",
          "apoyos_otr",
          "ictx_n","ictx_l","ictx_m","ictx_a",
          //TODO implementacion LAZY :P :( Eventualmente "fixear" je
          "p1","p2","p3","p4","p5","p6","p7","p8","p9","p10",
          "p11","p12","p13","p14","p15","p16","p17","p18","p19","p20",
      };
      String[] linea = new String[encabezado.length];
      PrintStream ps = new PrintStream("graduados_completo.csv");
      ps.println(CajaDeHerramientas.stringToCSV(encabezado));
      Arrays.fill(linea, "");
      int nes = 0;
      for (IES ies: kernel.listaIES) {
        int pos = 0;
        System.out.println("*" + ies.codigo);
        linea[pos++] = formateoCampoNum(ies.origen);
        linea[pos++] = formateoCampoNum(ies.caracter);
        linea[pos++] = formateoCampoNum(ies.departamento);
        linea[pos++] = formateoCampoNum(ies.municipio);
        
        for (Estudiante e:ies.estudiantes) {
          if (e.getSemestreGrado()==-1) continue;
          int epos = pos;
          linea[epos++] = String.valueOf(nes++);
          linea[epos++] = formateoCampoNum(e.getSexo());
          linea[epos++] = formateoCampoNum(e.getEdadAlPresentarElICFES());
          linea[epos++] = formateoCampoNum(e.getNivelEducativoMadre());
          linea[epos++] = formateoCampoNum(e.getIngresoHogar());
          linea[epos++] = formateoCampoNum(e.getViviendaPropia());
          linea[epos++] = formateoCampoNum(e.getNumeroHermanos());
          linea[epos++] = formateoCampoNum(e.getPosicionEntreLosHermanos());
          linea[epos++] = formateoCampoNum(e.getPuntajeICFES());
          linea[epos++] = formateoCampoNum(e.getTrabajabaCuandoPresentoIcfes());
          linea[epos++] = ies.semestres[e.getSemestrePrimiparo()];
          int g = e.getSemestreGrado();
          linea[epos++] = g==-1?"":ies.semestres[g];
          /*int rf = e.getSemestreRetiroForzoso();
          linea[pos++] = rf==-1?"":ies.semestres[rf];*/
          {
            Programa prog = e.getIndicePrograma()<0?null:ies.programas[e.getIndicePrograma()];
            int area = prog==null||prog.area==-1?-1:prog.area;
            for (int iA = -1;iA<=9;iA++) {
              if (iA==0) continue;
              linea[epos++] = iA==area?"1":"0";
            }
          }
          long matri=e.getSemestresMatriculadoAlDerecho();
          int apoA=0, apoF=0, apoO=0, ictx[] = new int[4], mA = 0, mT = 0;
          Set<Integer> pres = new TreeSet<Integer>();
          for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
            int mTP= e.getNumeroMateriasTomadas(j),
              mAP= e.getNumeroMateriasAprobadas(j);
            if (mTP!= -1 && mAP!=-1) {
              mT+=mTP;
              mA+=mAP;
            }
            apoA+= e.getRecibioApoyoAcademico(j)?1:0;
            apoF+= e.getRecibioApoyoFinanciero(j)?1:0;
            apoO+= e.getRecibioApoyoOtro(j)?1:0;
            {
              byte icetex = e.getTipoApoyoICETEXRecibido(j);
              for (int i=0;i<4;i++) ictx[i] = i==icetex?1:0;  
            }
            /*
            for (int j2=0; j2<jT; j2++) {
              linea[epos++] = j2==j?"1":"0";
            }*/
            pres.add(j);
          }
          linea[epos++] = formateoCampoNum(mT);
          linea[epos++] = formateoCampoNum(mA);
          linea[epos++] = formateoCampoNum(apoA);
          linea[epos++] = formateoCampoNum(apoF);
          linea[epos++] = formateoCampoNum(apoO);
          for (int i=0;i<4;i++) linea[epos++] = formateoCampoNum(ictx[i]);
          for (int i=0;i<20;i++) {
            linea[epos++] = i<ies.minCodigoSemestre || i>ies.maxCodigoSemestre?
                "0"
                :
                  (pres.contains(i-ies.minCodigoSemestre)?"1":"0");
          }
          ps.println(CajaDeHerramientas.stringToCSV(linea));
          nes++;
        }
      }
      ps.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Fin escritura");
  }

  public static void escrituraArchivoIndividuosIes(KernelSPADIES kernel, File csvSalida) throws FileNotFoundException     {
    PrintStream ps = new PrintStream(csvSalida);
    String[] encabezado = new String[]{};
    ps.println(CajaDeHerramientas.stringToCSV(encabezado));
    for (IES ies: kernel.listaIES) {
      //System.out.println("Preparado " + ies.codigo);
      //System.out.println((System.currentTimeMillis()-tm)/1000);
      int ne= ies.estudiantes.length;
      int ie = 0;
      for (int i=0;i<ne;i++) {
        Estudiante_DatosPersonales edp = ies.datosPersonalesEstudiantes==null?null:ies.datosPersonalesEstudiantes[i];
        String doc = edp.documento==-1?"":String.valueOf(edp.documento),
            nombre = (new String(edp.apellido) + " " + new String(edp.nombre)).trim(),
            fec = (edp.diaFechaNacimiento==-1?"":edp.diaFechaNacimiento)+"/"+(edp.mesFechaNacimiento==-1?"":edp.mesFechaNacimiento)+"/"+(edp.anhoFechaNacimiento==-1?"":edp.anhoFechaNacimiento); 
        ps.println(CajaDeHerramientas.stringToCSV(""+ies.codigo, ie+"",nombre, doc, fec));
        ++ie;
      }
    }
    ps.close();
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