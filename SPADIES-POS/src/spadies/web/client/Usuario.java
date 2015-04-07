package spadies.web.client;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class Usuario {
  public final String nom, tipo;
  public Usuario(String nom,String tipo) {
    this.nom = nom;
    this.tipo = tipo;
  }
  public static Usuario readUsuario(JSONValue val) {
    JSONObject ob = val.isObject();
    JSONString nom = ob==null?null:ob.get("nombre").isString();
    JSONString tipo = ob==null?null:ob.get("tipo").isString();
    if (nom!=null && tipo!=null) return new Usuario(nom.stringValue(), tipo.stringValue());
    else return null;
  }
  public boolean isPublic() {
    return "PUBLICO".equals(tipo);
  }
  public boolean isIES() {
    return "IES".equals(tipo);
  }
}
