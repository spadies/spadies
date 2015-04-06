package spadies.util.variables;

import spadies.util.Constantes;

//Basado parcialmente en Variables.MUNICIPIO_IES
public class RangoConteo extends RangoByte<Integer> {
  private final int liminf;
  private final int limsup;
  private final Byte[] valores;
  public RangoConteo(int pLiminf, int pLimsup) {
    liminf = pLiminf;
    limsup = pLimsup;
    valores = new Byte[limsup - liminf + 1];
    for (int i = liminf; i <= limsup; i++) {
      valores[i - liminf] = (byte) i;
    }
  }
  public Byte[] getRango() {
    return valores;
  }

  @Override
  public Byte getRango(Integer t) {
    return (byte) (t > limsup?limsup:t);
  }

  @Override
  public String toString(Byte u) {
    if (u >= liminf && u < limsup)
      return Integer.toString(u);
    else if (u==limsup)
      return Integer.toString(u) + " o mas.";
    else
      return Constantes.S_DESCONOCIDO;
  }
}
