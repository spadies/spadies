package spadies.auxiliar;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import spadies.kernel.Estudiante;
import spadies.kernel.Estudiante_DatosPersonales;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.util.CajaDeHerramientas;
import spadies.util.Constantes;
import spadies.util.MyException;

public class SolicitudMatriculaMEN20090205 {
  private static final String enc = "ies_cod;ies_nom;est_nombre;est_apellido;est_documento;est_documento;est_cursa";
  public static void main(String [] args) throws MyException, FileNotFoundException {
    KernelSPADIES kernel = KernelSPADIES.getInstance();
    kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,true);
    String [] pers = new String[]{"20061","20062","20071","20072","20081","20082"};
    SortedSet<Byte> perInteres = new TreeSet<Byte>();
    for (String per:pers)
      perInteres.add(CajaDeHerramientas.getCodigoSemestre(per));
    SortedMap<Byte,PrintStream> pss = new TreeMap<Byte, PrintStream>();
    for (int i = 0;i<pers.length;i++)
      pss.put(CajaDeHerramientas.getCodigoSemestre(pers[i]),new PrintStream("MEN200902_MATRI_"+pers[i]+".csv"));
    for (PrintStream ps:pss.values()) ps.println(enc);
    Object[] linea = new Object[7];
    for (IES ies:kernel.listaIES) {
      int n = ies.n;
      linea[0] = ies.codigo;
      linea[1] = new String(ies.nombre);
      for (int i = 0,t=ies.estudiantes.length;i<t;i++) {
        Estudiante e = ies.estudiantes[i];
        Estudiante_DatosPersonales ep = ies.datosPersonalesEstudiantes[i];
        linea[2] = new String(new String(ep.nombre));
        linea[3] = new String(new String(ep.apellido));
        linea[4] = ep.documento==-1?"":String.valueOf(ep.documento);
        linea[5] = e.getIndicePrograma()==-1?"":new String(ies.programas[e.getIndicePrograma()].nombre);
        int jI = e.getSemestrePrimiparo();
        long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
        int semEst = 0;
        for (int j=jI; j<n; j++,matri>>>=1) if ((matri&1L)==1L) {
          semEst++;
          linea[6] = semEst;
          byte semAbs = (byte)(j+ies.minCodigoSemestre);
          if (perInteres.contains(semAbs))
            pss.get(semAbs).println(toCSV(linea));
        }
      }
    }
    for (PrintStream ps:pss.values()) ps.close();
  }
  public static final String toCSV(Object [] ob) {
    StringBuilder sb = new StringBuilder();
    for (Object o:ob) {
      sb.append(o);
      sb.append(";");
    }
    return sb.toString();
  }
}