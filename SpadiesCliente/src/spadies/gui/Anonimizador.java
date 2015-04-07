package spadies.gui;

import spadies.gui.frames.VentanaCarga;
import spadies.kernel.KernelSPADIES;
import spadies.util.Constantes;

public class Anonimizador {

	public static void main(String[] args)throws Exception {
		KernelSPADIES.getInstance().cargar(Constantes.carpetaDatos, false, null);
		KernelSPADIES.getInstance().cargar(Constantes.carpetaDatos, Constantes.cargaDatosPersonales, null);
	}
}
