package spadies.kernel;

import spadies.io.MyDataInputStream;
import spadies.util.Constantes;
import spadies.util.MyByteSequence;
import spadies.util.Constantes.VersionDatos;

public class CargadorEstudiantes {
  protected final int n;
  protected final VersionDatos version;
  protected final int tam;
  protected CargadorEstudiantes(int n,VersionDatos version) {
    this.n = n;
    this.version = version;
    this.tam = getTamanhoEnBytes();
  }

  public int getTamanhoEnBytes() {
    return getTamFijo(version)+7*n + Constantes.maxVariablesExtra + 1
      + (version.ordinal()>VersionDatos.V_2_3_1.ordinal()?Constantes.maxVariablesExtraDinamicas*n:0);
  }
  public static int getTamanhoEnBytes(final VersionDatos ver, final int nn) {
    return getTamFijo(ver)+7*nn + Constantes.maxVariablesExtra + 1
      + (ver.ordinal()>VersionDatos.V_2_3_1.ordinal()?Constantes.maxVariablesExtraDinamicas*nn:0);
  }
  public static int getTamFijo(final VersionDatos version) {
    switch (version) {
      case V_2_3_1:return 11+8;
      case V_2_5:return 11+8;
      case V_2_6:return 13+8;
      case V_2_7:return 14+8;
      case V_2_7_2:return 15+8;
      case V_2_8:return 20+8;
      default:return -1;
    }
  }
  public Estudiante cargar(MyDataInputStream is) throws Exception {
    byte[] ba = is.readByteArray(false,tam);
    switch (version) {
      case V_2_3_1:return new Estudiante_2_3_1(n,new MyByteSequence(ba));
      case V_2_5:return new Estudiante_2_5(n,new MyByteSequence(ba));
      case V_2_6:return new Estudiante_2_6(n,new MyByteSequence(ba));
      case V_2_7:return new Estudiante_2_7(n,new MyByteSequence(ba));
      case V_2_7_2:return new Estudiante_2_7_2(n,new MyByteSequence(ba));
      case V_2_8:return new Estudiante_2_8(n,new MyByteSequence(ba));
      default:return null;
    }    
  }
  public Estudiante estudianteVacio() {
    switch (version) {
      case V_2_3_1:return new Estudiante_2_3_1(n,new MyByteSequence(tam));
      case V_2_5:return new Estudiante_2_5(n,new MyByteSequence(tam));
      case V_2_6:return new Estudiante_2_6(n,new MyByteSequence(tam));
      case V_2_7:return new Estudiante_2_7(n,new MyByteSequence(tam));
      case V_2_7_2:return new Estudiante_2_7_2(n,new MyByteSequence(tam));
      case V_2_8:return new Estudiante_2_8(n,new MyByteSequence(tam));
      default:return null;
    }    
  }
  public static CargadorEstudiantes getCargador(VersionDatos version, int n) {
    return new CargadorEstudiantes(n, version);
  }
}
