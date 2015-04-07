package spadies.server.util;

import java.io.File;
import java.io.FileOutputStream;

import spadies.kernel.Estudiante;
import spadies.kernel.Estudiante_DatosPersonales;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.kernel.Programa;
import spadies.util.CajaDeHerramientas;

public class OperacionesAdministrativas {
  public static void escribirCSVScompletos(KernelSPADIES kernel, File carSal) throws Exception {
    for (IES ies:kernel.listaIES) {
      File fOut=new File(carSal, String.valueOf(ies.codigo));
      fOut.mkdirs();
      int n=ies.n;
      String[] sufijos={
          "primiparos",
          "matriculados",
          "graduados",
          "retirosForzosos",
          "apoyosAcademicos",
          "apoyosFinancieros",
          "apoyosOtros",
          //"variablesICFES",
          //"apoyosICETEX"
          };
      String encabezados[]={
          "apellidos;nombres;tipoDocumento;documento;nombrePrograma;codigoEstudiante;sexo;fechaNacimiento;codigoSNIESprograma",
          "apellidos;nombres;tipoDocumento;documento;nombrePrograma;materiasTomadas;materiasAprobadas",
          "apellidos;nombres;tipoDocumento;documento;nombrePrograma",
          "apellidos;nombres;tipoDocumento;documento;nombrePrograma",
          "apellidos;nombres;tipoDocumento;documento;nombrePrograma",
          "apellidos;nombres;tipoDocumento;documento;nombrePrograma",
          "apellidos;nombres;tipoDocumento;documento;nombrePrograma",
          "apellidos;nombres;tipoDocumento;documento;nombrePrograma;numeroHermanos;posicionEntreHermanos;viviendaPropia;trabajaba;nivelEducativoMadre;ingresoHogar;edadPresentacionExamen;puntajeExamen",
          "apellidos;nombres;tipoDocumento;documento;nombrePrograma;tipoCreditoICETEX"};
      FileOutputStream fos[][]=new FileOutputStream[n][sufijos.length];
      for (int j=0; j<n; j++) for (int k=0; k<sufijos.length; k++) {
        fos[j][k]=new FileOutputStream(new File(fOut,CajaDeHerramientas.textoSemestreToString(ies.semestres[j])+"-"+sufijos[k]+".csv"));
        fos[j][k].write((encabezados[k]+"\r\n").getBytes());
      }
      for (int i=0,iT=ies.estudiantes.length; i<iT; i++) {
        Estudiante e=ies.estudiantes[i];
        Estudiante_DatosPersonales edp=ies.datosPersonalesEstudiantes[i];
        int jI=e.getSemestrePrimiparo();
        if (jI==-1) continue;
        int indicePrograma=e.getIndicePrograma();
        Programa p=(indicePrograma==-1)?null:ies.programas[indicePrograma];
        int semPrimi=e.getSemestrePrimiparo(),semRetFor=e.getSemestreRetiroForzoso(),semGrad=e.getSemestreGrado();
        String comienzo=
            cadenaToCSV(new String(edp.apellido))+";"+
            cadenaToCSV(new String(edp.nombre))+";"+
            CajaDeHerramientas.tipoDocumentoToString(edp.tipoDocumento)+";"+
            (edp.documento==-1?"":("'"+edp.documento))+";"+
            cadenaToCSV(p==null?"":new String(p.nombre));
        fos[semPrimi][0].write((""+comienzo+";"+
            cadenaToCSV(edp.codigo.length==0?"":("'"+new String(edp.codigo)))+";"+
            CajaDeHerramientas.sexoToString(e.getSexo())+";"+
            ((edp.anhoFechaNacimiento==-1 || edp.mesFechaNacimiento==-1 || edp.diaFechaNacimiento==-1)?"":(CajaDeHerramientas.intToString(edp.diaFechaNacimiento,2)+"/"+CajaDeHerramientas.intToString(edp.mesFechaNacimiento,2)+"/"+CajaDeHerramientas.intToString(edp.anhoFechaNacimiento,4)))+";"+
            (p==null?"":("'"+new String(p.codigoSNIES)))+
            "\r\n").getBytes());
        /*fos[semPrimi][7].write((""+comienzo+";"+
            foo(e.getNumeroHermanos())+";"+
            foo(e.getPosicionEntreLosHermanos())+";"+
            foo(e.getViviendaPropia())+";"+
            foo(e.getTrabajabaCuandoPresentoIcfes())+";"+
            foo(e.getNivelEducativoMadre())+";"+
            foo(e.getIngresoHogar())+";"+
            foo(e.getEdadAlPresentarElICFES())+";"+
            foo(e.getPuntajeICFES())+
            "\r\n").getBytes());*/
        if (semGrad!=-1) fos[semGrad][2].write((""+comienzo+"\r\n").getBytes());
        if (semRetFor!=-1) fos[semRetFor][3].write((""+comienzo+"\r\n").getBytes());
        long matri=e.getSemestresMatriculadoAlDerecho()>>>jI;
        for (int j=jI; j<n; j++,matri>>>=1) if ((matri&1L)==1L) {
          int matTom=e.getNumeroMateriasTomadas(j),matApr=e.getNumeroMateriasAprobadas(j);
          fos[j][1].write((""+comienzo+";"+
              (matTom==-1?"":(""+matTom))+";"+
              (matApr==-1?"":(""+matApr))+
              "\r\n").getBytes());
          if (e.getRecibioApoyoAcademico(j)) fos[j][4].write((""+comienzo+"\r\n").getBytes());
          if (e.getRecibioApoyoFinanciero(j)) fos[j][5].write((""+comienzo+"\r\n").getBytes());
          if (e.getRecibioApoyoOtro(j)) fos[j][6].write((""+comienzo+"\r\n").getBytes());
          /*if (e.getTipoApoyoICETEXRecibido(j)!=0) {
            fos[j][8].write((""+comienzo+";"+
                CajaDeHerramientas.tipoApoyoIcetexToString(e.getTipoApoyoICETEXRecibido(j))+
                "\r\n").getBytes());
          }*/
        }
      }
      for (int j=0; j<n; j++) for (int k=0; k<sufijos.length; k++) fos[j][k].close();  
    }
  }
  private static String cadenaToCSV(String s) {
    return "\""+s.replaceAll("\"","\"\"")+"\"";
  }
}
