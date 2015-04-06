package spadies.util.variables;

public class ImprimirVariables {
  
  public static String imprimirCategorias(CategoriasVariables.BCategoria[] bcategorias){
    String cadena = "{\n";
    for (int j=0;j<bcategorias.length;j++){
      cadena = cadena + "\t\""+bcategorias[j].nombre+"\": [\n";
      for (int i=0;i<bcategorias[j].vars.length;i++){
        Variable var = bcategorias[j].vars[i];
        cadena = cadena + "\t\t{\n";      
        cadena = cadena + "\t\t\t\"nvar\":\""+var.ordinal()+"\",\n";
        cadena = cadena + "\t\t\t\"idvar\":\""+var.name()+"\",\n";
        cadena = cadena + "\t\t\t\"nombre\":\""+escape(var.nombre)+"\",\n";
        cadena = cadena + "\t\t\t\"valores\":[\n";
        boolean prim = true;
        for (Comparable c:var.rango.getRango()) if (var!=Variable.PROGRAMA_EST || !((String)c).matches("\\d++")){
          cadena = cadena + "\t\t\t\t" + ((prim?"":",")+"[\"" + c + "\",\"" + escape(var==Variable.CODIGO_IES?var.rango.toStringHTML(c):var.rango.toString(c))+"\"]\n");
          prim=false;
        }
        cadena = cadena + "\t\t\t]\n";
        if(i!=bcategorias[j].vars.length-1){
          cadena = cadena + "\t\t},\n";
        }else{
          cadena = cadena + "\t\t}\n";
        }
      }
      if(j!=bcategorias.length-1){
        cadena = cadena + "\t],\n";
      }else{
        cadena = cadena + "\t]\n";        
      }
    }    
    cadena = cadena + "}";
    return cadena;
  }
  //Tomado de http://groups.google.com/group/Google-Web-Toolkit/browse_thread/thread/5f437d12ba83fff0
  public static String escape(String s){
    if(s==null) 
            return null; 
    StringBuffer sb=new StringBuffer();
    for(int i=0;i<s.length();i++){ 
            char ch=s.charAt(i); 
            switch(ch){ 
            case '"': 
                    sb.append("\\\""); 
                    break; 
            case '\\': 
                    sb.append("\\\\"); 
                    break; 
            case '\b': 
                    sb.append("\\b"); 
                    break; 
            case '\f': 
                    sb.append("\\f"); 
                    break; 
            case '\n': 
                    sb.append("\\n"); 
                    break; 
            case '\r': 
                    sb.append("\\r"); 
                    break; 
            case '\t': 
                    sb.append("\\t"); 
                    break; 
            case '/': 
                    sb.append("\\/"); 
                    break; 
            default: 
                    if(ch>='\u0000' && ch<='\u001F'){ 
                            String ss=Integer.toHexString(ch); 
                            sb.append("\\u"); 
                            for(int k=0;k<4-ss.length();k++){ 
                                    sb.append('0'); 
                            } 
                            sb.append(ss.toUpperCase()); 
                    } 
                    else{ 
                            sb.append(ch); 
                    } 
            } 
    }//for 
    return sb.toString(); 
  }   
  public static void main(String[] args){    
    System.out.println(imprimirCategorias(CategoriasVariables.variablesEnCategorias(CategoriasVariables.VARIABLES_ESTATICAS)));    
  }
}
