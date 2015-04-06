package spadies.util.variables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import spadies.kernel.Estudiante;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.kernel.individual.DisjoinDataSet;
import spadies.kernel.individual.PrincipalMatch;
import spadies.util.Constantes;
import spadies.util.ConstantesInternas;
import spadies.util.MyException;

public enum SVariable {
  SEXO_EST(Variable.SEXO_EST, Modo.VM_IES_INI),
  //Variables Socioeconomicas
  TRABAJABA_CUANDO_ICFES_EST(Variable.TRABAJABA_CUANDO_ICFES_EST, Modo.VM_SE_INI),
  CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST(Variable.CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST, Modo.VM_SE_INI),
  INGRESO_HOGAR_EST2(Variable.INGRESO_HOGAR_EST2, Modo.VM_SE_INI),
  NUMERO_FAMILIARES_EST(Variable.NUMERO_FAMILIARES_EST, Modo.VM_SE_INI),
  ESTRATO(Variable.ESTRATO, Modo.VM_SE_INI),
  NIVEL_SISBEN(Variable.NIVEL_SISBEN, Modo.VM_SE_INI),
  NIVEL_EDUCATIVO_MADRE_EST(Variable.NIVEL_EDUCATIVO_MADRE_EST, Modo.VM_SE_INI),
  CLASIFICACION_PUNTAJE_ICFES_EST(Variable.CLASIFICACION_PUNTAJE_ICFES_EST, Modo.VM_SE_INI),
  CLASIFICACION_PUNTAJE_ICFES_DECILES_EST(Variable.CLASIFICACION_PUNTAJE_ICFES_DECILES_EST, Modo.VM_SE_INI),
  //Variables Temporales
  PERIODO_INGRESO_EST(Variable.PERIODO_INGRESO_EST, Modo.VM_IES_INI),
  //Variables Programa
  AREA_CONOCIMIENTO_EST(Variable.AREA_CONOCIMIENTO_EST, Modo.VM_IES_INI),
  NUCLEO_CONOCIMIENTO_EST(Variable.NUCLEO_CONOCIMIENTO_EST, Modo.VM_IES_INI),
  NIVEL_FORMACION_EST(Variable.NIVEL_FORMACION_EST, Modo.VM_IES_INI),
  METODOLOGIA_EST(Variable.METODOLOGIA_EST, Modo.VM_IES_INI),
  //Variades
  VIDAS_EST(Modo.VM_OTRO,"#apariciones", new RangoConteo(1,5)),
  ;
  //public final STipoVariable tipo;
  public String nombre;
  public final Rango rango;
  public Item[] items;
  public final Modo modo;
  public final Variable referente;
  /*private SVariable(STipoVariable pTipo, String pNombre, Rango pRango) {
    tipo=pTipo;
    nombre=pNombre;
    rango=pRango;
    generarItems();
  }*/
  private SVariable(Variable pReferente, Modo pModo) {
    modo = pModo;
    referente = pReferente;
    switch (pModo) {
    case VM_IES_INI:
      nombre = referente.nombre + " inicial.";
      rango = getRangoExtendidoVarios((RangoByte<Byte>) referente.rango);
      break;
    case VM_SE_INI:
      nombre = referente.nombre + " inicial.";
      rango = getRangoExtendidoVarios((RangoByte<Byte>) referente.rango);
      break;
    default:
      rango = null;
      break;
    }
    generarItems();
  }
  
  private SVariable(Modo pModo, String pNombre, Rango pRango) {
    modo = pModo;
    nombre = pNombre;
    rango = pRango;
    referente = null;
    generarItems();
  }
  @SuppressWarnings("unchecked")
  public void generarItems() {
    List<Item> listaItems=new ArrayList<Item>(10);
    for (Comparable key:rango.getRango()) listaItems.add(new Item(key,rango.toString(key),rango.toStringHTML(key)));
    items=listaItems.toArray(new Item[0]);
  }
  public enum STipoVariable {
    STV_SE_INI,
    STV_ESTUDIANTE,
    STV_PERIODO_ESTUDIANTE
  }
  public enum Modo {
    VM_SE_INI, //Socioeconomica inicial
    VM_IES_INI, //IES inicial
    VM_OTRO;
  }
  public static RangoByte<Byte> getRangoExtendidoVarios(final RangoByte<Byte> rangoref) {
    Byte[] r0 = rangoref.getRango();
    final Byte [] r1 = new Byte[r0.length + 1];
    System.arraycopy(r0, 0, r1, 0, r0.length);
    final byte newval = (byte) (rangoref.getMaxRango() + 1);
    r1[r0.length] = newval;
    return new RangoByte<Byte>() {
      public Byte[] getRango() {
        return r1;
      }
      public Byte getRango(Byte val) {
        return val;
      }
      public String toString(Byte val) {
        if (val.byteValue()==newval) return "Varios";
        else return rangoref.toString(val.byteValue());
      }
    };
  }
  
  public Object getValor(Object...args) {
    switch (modo) {
      case VM_IES_INI: //El del primer periodo primiparo
        {
          // args: individuo
          List<int[]> individuo = (List<int[]>) (args[0]);
          Byte multiples_valores; 
          { //Valor que representa la existencia de multiples valores para la variable.
            Byte[] x = (Byte[]) this.rango.getRango();
            multiples_valores = x[x.length-1];
          }
          Byte val = -1;
          int perest = Integer.MAX_VALUE;
          for (int[] ap:individuo) {
            IES ies = KernelSPADIES.getInstance().listaIES[ap[0]];
            Estudiante e = ies.estudiantes[ap[1]];
            int per = ies.minCodigoSemestre + e.getSemestrePrimiparo();
            Byte val_e = (Byte) this.referente.rango.getRango(this.referente.getValor(e,ies));
            if (per < perest && val_e!=null && val_e!=-1) {
              perest = per;
              val = val_e; //Todas las socieconomicas son de tipo TV_ESTUDIANTE
            } else if (per == perest) {
              if (val==null || val==-1) {
                val = val_e; 
              } else {
                if (val!=val_e) {
                  val = multiples_valores;
                }
              }
            }
          }
          return val;
        }
      case VM_SE_INI:
      {
        // args: individuo
        List<int[]> individuo = (List<int[]>) (args[0]);
        Byte multiples_valores; 
        { //Valor que representa la existencia de multiples valores para la variable.
          Byte[] x = (Byte[]) this.rango.getRango();
          multiples_valores = x[x.length-1];
        }
        Byte val = -1;
        int pericf = Integer.MAX_VALUE;
        for (int[] ap:individuo) {
          IES ies = KernelSPADIES.getInstance().listaIES[ap[0]];
          Estudiante e = ies.estudiantes[ap[1]];
          int per = e.getPerIcfes();
          Byte val_e = (Byte) this.referente.rango.getRango(this.referente.getValor(e,ies));
          if (per < pericf) {
            pericf = per;
            val = val_e; //Todas las socieconomicas son de tipo TV_ESTUDIANTE
          } else if (per == pericf) {
            if (val==null || val==-1) {
              val = val_e; 
            } else {
              if (val!=val_e) {
                val = multiples_valores;
              }
            }
          }
        }
        return val;
      }
      case VM_OTRO:
      {
        switch (this) {
        case VIDAS_EST:
          List<int[]> individuo = (List<int[]>) (args[0]);
          int conteo = 0;
          for (int[] x:individuo) conteo++;
          return conteo;
        default:
          break;
        }
      }
      default:
        return null;
    }
  }
  public String toString() {
    return this.nombre;
  }
  public static String toString(SVariable v, byte b) {
	    return v.rango.toString(v.rango.byteToRango(b));
  }
  public static void main(String [] args) throws MyException { //Test de evaluación de las variables
	  SVariable.values();
    KernelSPADIES.getInstance().cargar(Constantes.carpetaDatos, true, null);
    //PrincipalMatch.cruzarKernel(KernelSPADIES.getInstance());
    //System.out.println(DisjoinDataSet.disjoinDatasets.length);
    SVariable[] vars = SVariable.values();
    //SVariable[] vars = new SVariable[]{SVariable.SEXO_EST};
    java.util.EnumMap<SVariable, java.util.Map<Comparable, Integer>> res = new java.util.EnumMap<SVariable, java.util.Map<Comparable, Integer>>(SVariable.class);
    for (SVariable svar:vars) {
      TreeMap<Comparable, Integer> tm = new java.util.TreeMap<Comparable, Integer>();
      res.put(svar, tm);
      for (Comparable co:svar.rango.getRango()) {
        tm.put(co, 0);
      }
    }
    for(List<int[]> individuo:DisjoinDataSet.getIndividuosIdsRelativos(KernelSPADIES.getInstance())){
      if (individuo!=null) for (SVariable svar:vars) {
        Comparable co = (Comparable) svar.getValor(individuo);
        Map<Comparable, Integer> tm = res.get(svar);
        tm.put(co, tm.get(co) + 1);
      }
    }
    for (SVariable svar:vars) {
      System.out.println(svar.nombre);
      for (Entry<Comparable, Integer> es:res.get(svar).entrySet()) {
        System.out.println(svar.rango.toString(svar.rango.getRango(es.getKey()))+ "\t" + es.getValue());
      }
      System.out.println();
    }
  }
}
