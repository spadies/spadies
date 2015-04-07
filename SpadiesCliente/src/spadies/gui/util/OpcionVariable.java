package spadies.gui.util;

public class OpcionVariable {
  private int numero = -1;
  private String nombre = null;
  public OpcionVariable(int numero, String nombre) {
    this.numero = numero; this.nombre = nombre;
  }
  public OpcionVariable(String nombre) {
    this(-1,nombre);
  }
  public String toString() {return nombre;}
  public int getNumero() {return numero;}
  public void setNombre(String nombre) {
    this.nombre = nombre;
  }
}