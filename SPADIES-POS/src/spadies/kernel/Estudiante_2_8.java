package spadies.kernel;

import spadies.util.MyByteSequence;

public class Estudiante_2_8 extends EstudianteAbstract {
  //private static final int tamFijo0 = 20, tamFijo = tamFijo0+8;
  public Estudiante_2_8(int pN, MyByteSequence pDatos) {
    super(pN, pDatos);
  }
  @Override
  protected int getTamFijo() {
    return 20+8;
  }
  @Override
  protected int getTamFijo0() {
    return 20;
  }
}