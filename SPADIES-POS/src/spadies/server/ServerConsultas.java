/*
 * Centro de Estudios sobre Desarrollo Económico (CEDE) - Universidad de los Andes
 * Octubre 18 de 2006
 *
 *********************************************************
 * SPADIES                                               *
 * Sistema para la Prevención y Análisis de la Deserción *
 * en las Instituciones de Educación Superior            *
 *********************************************************
 * Autores del código fuente (última versión):           *
 *  Alejandro Sotelo Arévalo   alejandrosotelo@gmail.com *
 *  Andrés Córdoba Melani      acordoba@gmail.com        *
 *********************************************************
 *
 * Para información de los participantes del proyecto véase el "Acerca De" de la aplicación.
 * 
 * La modificación del código fuente está prohibida sin permiso explícito por parte de
 * los autores o del Ministerio de Educación Nacional de la República de Colombia.
 *
 */
package spadies.server;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import spadies.kernel.*;
import spadies.server.kernel.*;
import spadies.server.util.ConstantesServer;
import spadies.util.*;
import spadies.util.variables.*;
import static spadies.util.CajaDeHerramientas.*;

public class ServerConsultas {
  private static final KernelSPADIES kernel=KernelSPADIES.getInstance();
  private static final DateFormat df=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  private static final int puerto=Constantes.puertoServidorConsultas;
  private static final long periodoActualizacion=1000L*60*60;  // 1 hora
  public static void main(String[] args) {
    new Thread() {
      public void run() {
        while (true) {
          long tm=System.currentTimeMillis();
          try {
            Constantes.cargarArchivoFiltroIES();
            kernel.cargarParaServidor(Constantes.carpetaDatos,periodoActualizacion);
            escrituraAcumulados();
          }
          catch (MyException ex) {
            System.out.println(ex.getMessage());
          }
          catch (Throwable th) {
            th.printStackTrace(System.out);
          }
          System.out.println(df.format(new Date())+": Sistema actualizado en "+(System.currentTimeMillis()-tm)+"ms.");
          System.out.println("Se actualizará de nuevo en "+(periodoActualizacion/(1000L*60))+" minutos");
          try {Thread.sleep(periodoActualizacion);} catch (Throwable th) {}
        }
      }
    }.start();
    ServerSocket serverSocket=null;
    try {
      TablaDepartamentos.getInstance().preparar();
      System.gc();
      serverSocket=new ServerSocket(puerto);
    }
    catch (Throwable th) {
      th.printStackTrace(System.out);
      System.exit(1);
    }
    System.out.println("EL SERVIDOR DE CONSULTAS DE "+Constantes.nombreAplicacionLargo+" ESTÁ ESCUCHANDO CONEXIONES");
    while (true) {
      try {
        final Socket clientSocket=serverSocket.accept();
        new Thread() {
          public void run() {
            long tm=System.currentTimeMillis();
            String address="",sErr="";
            try {
              address=clientSocket.getInetAddress().toString();
              clientSocket.setSoTimeout(1000*17);
              atender(clientSocket);
            }
            catch (Throwable th) {
              sErr=th.getMessage();
            }
            System.err.println(df.format(new Date())+";"+address+";"+(System.currentTimeMillis()-tm)+"ms;"+sErr.replace(';',','));
          }
        }.start();
      }
      catch (Throwable th) {
      }
    }
  }
  private static void atender(Socket cliente) throws Exception {
    ObjectOutputStream out=null;
    ObjectInputStream in=null;
    try {
      out=new ObjectOutputStream(cliente.getOutputStream());
      in=new ObjectInputStream(cliente.getInputStream());
      atender(out,in);
    }
    catch (MyException ex) {
      out.writeObject(ex);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      try {
        if (out!=null) out.close();
        if (in!=null) in.close();
        cliente.close();
      }
      catch (Throwable th) {
      }
    }
  }
  private static void atender(ObjectOutputStream out, ObjectInputStream in) throws Exception {
    if (in.readLong()!=9165465416546L) return;
    int k=in.readInt();
    switch (k) {
      case 0:
        {
          Filtro[] filtrosIES=readObject(in,out,Filtro[].class);
          Variable variable=readObject(in,out,Variable.class);
          Object[] res=kernel.getInformacionBasicaCriterio(filtrosIES,variable);
          out.writeObject(res);
        }
        break;
      case 1:
        {
          Filtro[] filtros=readObject(in,out,Filtro[].class);
          Variable[] diferenciados=readObject(in,out,Variable[].class);
          Object[] res=kernel.getConteoPoblacion(filtros,diferenciados);
          out.writeObject(res);
        }
        break;
      case 2:
        {
          Filtro[] filtros=readObject(in,out,Filtro[].class);
          Variable[] diferenciados=readObject(in,out,Variable[].class);
          Object[] res=kernel.getConteoPoblacionSemestral(filtros,diferenciados);
          out.writeObject(res);
        }
        break;
      case 3:
        {
          Filtro[] filtrosIES=readObject(in,out,Filtro[].class);
          Object[] res=kernel.getPorcentajeDesercionPorCohorte(filtrosIES);
          out.writeObject(res);
        }
        break;
      case 4:
        {
          Filtro[] filtrosIES=readObject(in,out,Filtro[].class);
          Object[] res=kernel.getDatosTablaPresentacionModelo(filtrosIES);
          out.writeObject(res);
        }
        break;
      case 5:
        {
          int codigosDepartamentos[]=readObject(in,out,int[].class),n=codigosDepartamentos.length;
          double[] res=new double[n];
          Arrays.fill(res,Double.MAX_VALUE);
          for (int i=0; i<n; i++) {
            Map<Integer,Double> tasasDesempleo=TablaDepartamentos.getInstance().getTasasDesempleoPorAnho(codigosDepartamentos[i]);
            if (tasasDesempleo==null) continue;
            double sumTasasDesempleo=0.0; int numTasasDesempleo=0;
            for (double v:tasasDesempleo.values()) {
              sumTasasDesempleo+=v;
              numTasasDesempleo++;
            }
            if (numTasasDesempleo>0) res[i]=sumTasasDesempleo/numTasasDesempleo;
          }
          out.writeObject(res);
        }
        break;
      case 6:
        {
          Byte[] res=kernel.getSemestresActivos(new Filtro[0]);
          out.writeObject(res);
        }
        break;
      case 7:
        {
          Filtro[] filtrosIES=readObject(in,out,Filtro[].class);
          Object[] res=kernel.getTablaNivelAprobacionDesertores(filtrosIES);
          out.writeObject(res);
        }
        break;
      case 8: //Cruce de variables
        {
          Filtro[] filtros=readObject(in,out,Filtro[].class);
          Variable[] diferenciados=readObject(in,out,Variable[].class);
          Object[] res=kernel.getCruceVariables(filtros,diferenciados);
          out.writeObject(res);
        }
        break;
      case 9: //Ausencia intersemestral
        {
          Filtro[] filtros=readObject(in,out,Filtro[].class);
          Variable[] diferenciados=readObject(in,out,Variable[].class);
          Object[] res=kernel.getDesercionIntersemestral(filtros,diferenciados);
          out.writeObject(res);
        }
        break;
      case 10: //Tasa desercion/retencion
        {
          Filtro[] filtros=readObject(in,out,Filtro[].class);
          //Variable[] diferenciados=readObject(in,out,Variable[].class);
          Object[] res=kernel.getTasaDesercion(filtros/*,diferenciados*/);
          out.writeObject(res);
        }
        break;
      case 11: //Desercion por cohorte
        {
          Filtro[] filtros=readObject(in,out,Filtro[].class);
          //Variable[] diferenciados=readObject(in,out,Variable[].class);
          Object[] res=kernel.getDesercionPorCohorte(filtros/*,diferenciados*/);
          out.writeObject(res);
        }
        break;
      case 12: //Costo desercion
        {
          Filtro[] filtros=readObject(in,out,Filtro[].class);
          Variable[] diferenciados=readObject(in,out,Variable[].class);
          Object[] res=kernel.getCostoDesercion(filtros,diferenciados);
          out.writeObject(res);
        }
        break;
      case 13: //Variables por importancia
        {
          Filtro[] filtros=readObject(in,out,Filtro[].class);
          Object[] res=kernel.getVariablesRelevantes(filtros);
          out.writeObject(res);
        }
        break;
      case 14: //Tasa desercion/retencion
        {
          Filtro[] filtros=readObject(in,out,Filtro[].class);
          Variable[] diferenciados=readObject(in,out,Variable[].class);
          Object[] res=kernel.getTasaDesercion(filtros,diferenciados);
          out.writeObject(res);
        }
        break;
      case 15: //Desercion por cohorte
        {
          Filtro[] filtros=readObject(in,out,Filtro[].class);
          Variable[] diferenciados=readObject(in,out,Variable[].class);
          Object[] res=kernel.getDesercionPorCohorte(filtros,diferenciados);
          out.writeObject(res);
        }
        break;
    }
    out.writeLong(4816547187991L);
    out.flush();
  }
  
  private static void escrituraAcumulados() throws IOException {
    int
    cgenero=0,
    cedad=0,
    ctrabaja=0,//0
    cingreso=0,//0
    chermanos=0,//0
    ctotalicfes=0,
    ca=0,
    cmadniv=0,
    cnumerohijo=0,//0
    cviviendapropia=0,
    repitencia=0;
    double
    genero=0,
    edad=0,
    trabaja=0,//0
    ingreso=0,//0
    hermanos=0,//0
    totalicfes=0,
    a1=0,
    a2=0,
    a3=0,
    a6=0,
    a8=0,
    a9=0,
    madniv1=0,//0
    madniv2=0,//0
    numerohijo=0,//0
    viviendapropia=0,
    crepitencia = 0;
    for (IES ies: kernel.listaIES) {
      for (Estudiante e: ies.estudiantes) {
        int sexo=e.getSexo(),edadPresentacionIcfes=e.getEdadAlPresentarElICFES(),trabajabaCuandoIcfes=e.getTrabajabaCuandoPresentoIcfes();
        int ingresoHogar=e.getIngresoHogar(),numeroHermanos=e.getNumeroHermanos(),puntajeIcfes=e.getPuntajeICFES();
        int educacionMadre=e.getNivelEducativoMadre(),posicionEntreHermanos=e.getPosicionEntreLosHermanos(),viviendaPropia=e.getViviendaPropia();
        int indicePrograma=e.getIndicePrograma();
        int areaPrograma=(indicePrograma==-1)?-1:ies.programas[indicePrograma].area;
        if (sexo!=-1) {genero+=sexo; cgenero++;}
        if (edadPresentacionIcfes!=-1) {edad=edadPresentacionIcfes; cedad++;}
        if (trabajabaCuandoIcfes!=-1) {trabaja+=trabajabaCuandoIcfes; ctrabaja++;}
        if (ingresoHogar!=-1) {ingreso+=ingresoHogar; cingreso++;}
        if (numeroHermanos!=-1) {hermanos+=numeroHermanos; chermanos++;}
        if (puntajeIcfes!=-1) {totalicfes+=puntajeIcfes; ctotalicfes++;}
        if (areaPrograma!=-1) {
          ca++;
          a1+=areaPrograma==1?1:0;
          a2+=areaPrograma==2?1:0;
          a3+=areaPrograma==3?1:0;
          a6+=areaPrograma==6?1:0;
          a8+=areaPrograma==8?1:0;
          a9+=areaPrograma==9?1:0;                
        }
        if (educacionMadre!=-1) {
          cmadniv++;
          madniv1+=educacionMadre==1?1:0;//0
          madniv2+=educacionMadre==2?1:0;//0
        }
        if (posicionEntreHermanos!=-1) {numerohijo+=posicionEntreHermanos; cnumerohijo++;}
        if (viviendaPropia!=-1) {viviendapropia+=viviendaPropia;cviviendapropia++;}

        long matri=e.getSemestresMatriculadoAlDerecho();
        double repitencias[]=e.getRepitencias();
        for (int j=0,jT=ies.n; j<jT; j++,matri>>>=1) if ((matri&1L)==1L) {
          double repitenciaL=repitencias[j];
          if (repitenciaL!=-1d) {
            repitencia+=repitenciaL;
            crepitencia++;
          }
        }
      }
    }
    genero/=cgenero;
    edad/=cedad;
    trabaja/=ctrabaja;
    ingreso/=ctrabaja;
    hermanos/=chermanos;
    totalicfes/=ctotalicfes;
    a1/=ca;
    a2/=ca;
    a3/=ca;
    a6/=ca;
    a8/=ca;
    a9/=ca;
    madniv1/=cmadniv;
    madniv2/=cmadniv;
    numerohijo/=cnumerohijo;
    viviendapropia/=cviviendapropia;
    repitencia/=crepitencia;
    {
      Properties prop = new Properties();
      prop.setProperty("genero", String.valueOf(genero));
      prop.setProperty("edad", String.valueOf(edad));
      prop.setProperty("trabaja", String.valueOf(trabaja));
      prop.setProperty("ingreso", String.valueOf(ingreso));
      prop.setProperty("hermanos", String.valueOf(hermanos));
      prop.setProperty("totalicfes", String.valueOf(totalicfes));
      /*
      ps.println(a1);
      ps.println(a2);
      ps.println(a3);
      ps.println(a6);
      ps.println(a8);
      ps.println(a9);
      ps.println(madniv1);
      ps.println(madniv2);
      */
      prop.setProperty("numerohijo", String.valueOf(numerohijo));
      prop.setProperty("viviendapropia", String.valueOf(viviendapropia));
      prop.setProperty("repitencia", String.valueOf(repitencia));
      FileOutputStream fOS = new FileOutputStream(ConstantesServer.fPROMEDIOS);
      prop.store(fOS, "");
      fOS.close();
      /*
      PrintStream ps = new PrintStream(new File("valoresPredeterminadosModelo.csv"));
      ps.println(genero);
      ps.println(edad);
      ps.println(trabaja);
      ps.println(ingreso);
      ps.println(hermanos);
      ps.println(totalicfes);
      ps.println(a1);
      ps.println(a2);
      ps.println(a3);
      ps.println(a6);
      ps.println(a8);
      ps.println(a9);
      ps.println(madniv1);
      ps.println(madniv2);
      ps.println(numerohijo);
      ps.println(viviendapropia);
      ps.println(repitencia);            
      ps.close();
      */
    }
  }
}
