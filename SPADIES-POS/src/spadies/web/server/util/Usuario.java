package spadies.web.server.util;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import spadies.util.variables.Filtro;
import spadies.util.variables.FiltroVariables;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;

public class Usuario {
  public final String nombre;
  public final Tipo tipo;
  public final Filtro[] filtros;
  public final Map<Variable,Filtro> filtrosVar = new EnumMap<Variable,Filtro>(Variable.class);
  //public Usuario usuarioPublico = new Usuario("Publico", Tipo.PUBLICO, new Filtro[]{new Filtro(Variable.CARACTER_IES,new Item[]{new Item((byte)1,"",""),new Item((byte)2,"",""),new Item((byte)3,"",""),new Item((byte)4,"","")})});
  public static final Usuario usuarioPublico = new Usuario("Publico", Tipo.PUBLICO, new Filtro[]{new Filtro(Variable.INT_BLOQUEADA_IES,new Item[]{new Item((byte)1,"","")})});
  public Usuario(String nombre, Tipo tipo, Filtro[] filtros) {
    this.nombre = nombre;
    this.tipo = tipo;
    this.filtros = filtros;
    if(filtros!=null)
      for (Filtro f:filtros)
        filtrosVar.put(f.variable, f);
  }
  public FiltroVariables getFiltroVariables() {
    return tipo.fvar;
  }
  public int getIES() {
    return tipo==Tipo.IES?Integer.parseInt(nombre):-1;
  }
  public String toJSON() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"nombre\":");
    sb.append("\""+this.nombre+"\"");
    sb.append(",\"tipo\":");
    sb.append("\""+this.tipo.name()+"\"");
    sb.append("}");
    return sb.toString();
  }
  public static enum Tipo {
    PUBLICO(new FiltroVariables(){
      private final Set<Variable> var_n = EnumSet.copyOf(Arrays.asList(Variable.INT_CLASE_IES,Variable.INT_BLOQUEADA_IES/*,Variable.NUMERO_APOYOS_ICETEX_RECIBIDOS_EST,Variable.NUMERO_APOYOS_ICETEX_LARGO_RECIBIDOS_EST,Variable.NUMERO_APOYOS_ICETEX_MEDIO_RECIBIDOS_EST,Variable.ICETEX_RECIBIDO_PER,Variable.TIPO_ICETEX_RECIBIDO_PER*/));
      public boolean aceptarVariable(Variable v) {
        return !var_n.contains(v);
      }}),
    IES(new FiltroVariables(){
      private final Set<Variable> var_n = EnumSet.copyOf(Arrays.asList(Variable.INT_CLASE_IES,Variable.INT_BLOQUEADA_IES));
      public boolean aceptarVariable(Variable v) {
        return !var_n.contains(v);
      }}),
    MEN(new FiltroVariables(){
      public boolean aceptarVariable(Variable v) {
        return true;
      }});
    public final FiltroVariables fvar;
    private Tipo(FiltroVariables fvar) {
      this.fvar = fvar;
    }
  }
}
