package spadies.kernel.individual;

import static spadies.util.CajaDeHerramientas.stringToCSV;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;

import spadies.kernel.KernelSPADIES;
import spadies.server.kernel.PreparadorDatos;
import spadies.util.MyByteSequence;

public class PrincipalMatch {
	public static final File tmpDir = new File("tmp");
	public static final File outDir = new File("out");

	public static void cruzarKernel(KernelSPADIES kernel) {
		if (!DisjoinDataSet.load()) {
			long l=System.currentTimeMillis();
			System.out.println("Cargando");
			try {
				outDir.mkdirs();
				byte[][][] INFO_IES = kernel.getInfoBytes();
				PreparadorDatos.getInstance().prepararIndividualBase(kernel);
				MatcherSPADIES m2 = new MatcherSPADIES(INFO_IES, 97.99, true,
						new File("base"));
				byte[][] res2 = m2.procesar();
				DisjoinDataSet.init(INFO_IES[0].length);
				try (PrintStream ps = new PrintStream(new File(outDir,
						"spadies--spadies.csv"))) {// Guardado resultado
					ps.println(stringToCSV(new String[] { "id1", "id2" }));
					for (int i = 0, it = INFO_IES[0].length; i < it; i++) {
						String id = Integer.toString(i);
						if (res2[i] != null) {
							MyByteSequence mbs = new MyByteSequence(res2[i]);
							int t = mbs.getSize() / 4;
							for (int j = 0; j < t; j++) {
								DisjoinDataSet.link(i, mbs.getInt(4 * j));
								String id2 = Integer.toString(mbs.getInt(4 * j));
								ps.println(stringToCSV(new String[] { id, id2 }));
							}
						}
					}
				}
				DisjoinDataSet.save();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			System.out.println("Cargado en: "+(System.currentTimeMillis()-l));
			
		}
		int[][] consulta=new int[8][64];
		for(Individuo d:DisjoinDataSet.getIndividuosIdsRelativos(kernel))if(d!=null){
			int[] acumulador=new int[64];
			d.consultaTatiana(acumulador);
			for(int e=0;e<acumulador.length;e++){
				if(acumulador[e]>=consulta.length){
					int[][] nuevaconsulta=new int[acumulador[e]+1][64];
					for(int x=0;x<consulta.length;x++)nuevaconsulta[x]=consulta[x];
					consulta=nuevaconsulta;
				}
				consulta[acumulador[e]][e]++;
			}
		}
		for(int i=0;i<consulta[0].length;i++)System.out.print("\t"+(i+1));
		System.out.println();
		for(int e=0;e<consulta.length;e++){
			System.out.print(e);
			for(int i=0;i<consulta[e].length;i++)System.out.print("\t"+consulta[e][i]);
			System.out.println();
		}
	}
}
