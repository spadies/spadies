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
package spadies.gui.sincronizador;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;
import spadies.util.*;
import spadies.gui.util.*;
import spadies.io.*;
import spadies.kernel.*;
import static spadies.util.CajaDeHerramientas.*;

public final class SincronizadorIES extends Observable {
  private final static String[] sufijos={
    "primiparos",        // Información primer semestre
    "matriculados",      // Información matriculados
    "graduados",         // Información graduados
    "retirosForzosos",   // Retiros forzosos
    "apoyosAcademicos",  // Apoyos académicos
    "apoyosFinancieros", // Apoyos financieros
    "apoyosOtros",       // Otros apoyos
    "variablesICFES",    // Variables del ICFES
    "apoyosICETEX"       // Apoyos del ICETEX
  };
  private final static int TAM_BLOQUE=10,NUM_ARCHS=sufijos.length;
  private final CalificadorCadenas cc=new CalificadorCadenas();
  private final Sincronizador padre; 
  private final File carpeta;
  private final String codigoIES;
  private final char[] password;
  private final VariableExtraSinc variablesExtra[]=new VariableExtraSinc[Constantes.maxVariablesExtra];
  private final VariableExtraSinc variablesExtra2[]=new VariableExtraSinc[Constantes.maxVariablesExtraDinamicas];
  private List<EstudianteSinc> estudiantes=new ArrayList<EstudianteSinc>(10000);
  private Map<byte[],ProgramaSinc> programas=new TreeMap<byte[],ProgramaSinc>(comparadorByteArray);
  private String sems[]=null;
  private IES ies=new IES();
  private CargadorEstudiantes carg;
  public SincronizadorIES(Sincronizador pPadre, File pCarpeta, char[] pPassword) {
    padre=pPadre;
    carpeta=pCarpeta;
    codigoIES=carpeta.getName();
    password=pPassword;
    for (int h=0; h<5; h++) variablesExtra[h]=new VariableExtraSinc();
    for (int h=0; h<5; h++) variablesExtra2[h]=new VariableExtraSinc();
  }
  private void notificar(String accion, long pasado, long total, int paso) {
    setChanged();
    notifyObservers(new Object[]{accion,pasado,total,paso});
  }
  public void sincronizar() throws MyException {
    try {
      leer();
      traerDatosExternos();
      escribirRespuesta(Constantes.carpetaDatos);
    }
    catch (MyException ex) {
      throw ex;
    }
    catch (Throwable th) {
      throw new MyException("Hubo un error sincronizando el sistema con los datos en la carpeta \""+carpeta.getPath()+"\".",th);
    }
    finally {
      Arrays.fill(password,(char)0);
    }
  }
  private void leer() throws Exception {
    //TODO cambio para aceptar sems 01 02
    //TODO cambio para aceptar retiro disciplinario
    String paso="Paso 1 de 5 de la IES "+codigoIES+": Leyendo datos y reuniendo la información";
    notificar(paso,-1,-1,1);
    int minSem[]={Integer.MAX_VALUE,Integer.MAX_VALUE},maxSem[]={Integer.MIN_VALUE,Integer.MIN_VALUE};
    for (int anho=Constantes.anhoIni; anho<=Constantes.anhoFin; anho++) for (int sem=1; sem<=2; sem++) for (int i=0; i<NUM_ARCHS; i++) if (new File(carpeta,anho+"-"+sem+"-"+sufijos[i]+".csv").exists()) {
      if (anho<minSem[0] || (anho==minSem[0] && sem<minSem[1])) minSem=new int[]{anho,sem};
      if (anho>maxSem[0] || (anho==maxSem[0] && sem>maxSem[1])) maxSem=new int[]{anho,sem};
    }
    if (minSem[0]==Integer.MAX_VALUE) throw new MyException("No se encontraron archivos de datos en formato csv en la carpeta \""+carpeta.getPath()+"\".");
    sems=getTextosSemestresEntre(minSem[0],minSem[1],maxSem[0],maxSem[1]);
    if (sems.length>32) sems=getSubArreglo(String.class,sems,sems.length-32,sems.length-1);
    int numArchivos=0,contArchivos=0,numSems=sems.length;
    for (String sSem:sems) {
      int anho=Integer.parseInt(sSem.substring(0,4)),sem=Integer.parseInt(sSem.substring(4,5));
      for (int i=0; i<NUM_ARCHS; i++) if ((new File(carpeta,anho+"-"+sem+"-"+sufijos[i]+".csv")).exists()) numArchivos++;
    }
    MyListMap<byte[],EstudianteSinc> mapEstudiantes1=new MyListMap<byte[],EstudianteSinc>(comparadorByteArray);
    MyListMap<byte[],EstudianteSinc> mapEstudiantes2=new MyListMap<byte[],EstudianteSinc>(comparadorByteArray);
    // primiparos-aaaa-s.csv
    //   apellidos;nombres;tipoDocumento;documento;nombrePrograma;codigoEstudiante;sexo;fechaNacimiento;codigoSNIESprograma
    // matriculados-aaaa-s.csv
    //   apellidos;nombres;tipoDocumento;documento;nombrePrograma;materiasTomadas;materiasAprobadas
    // graduados-aaaa-s.csv
    //   apellidos;nombres;tipoDocumento;documento;nombrePrograma
    // retirosForzosos-aaaa-s.csv
    //   apellidos;nombres;tipoDocumento;documento;nombrePrograma
    // apoyosAcademicos-aaaa-s.csv
    //   apellidos;nombres;tipoDocumento;documento;nombrePrograma
    // apoyosFinancieros-aaaa-s.csv
    //   apellidos;nombres;tipoDocumento;documento;nombrePrograma
    // apoyosOtros-aaaa-s.csv
    //   apellidos;nombres;tipoDocumento;documento;nombrePrograma
    // variablesICFES-aaaa-s.csv
    //   apellidos;nombres;tipoDocumento;documento;nombrePrograma;numeroHermanos;posicionEntreHermanos;viviendaPropia;trabajaba;nivelEducativoMadre;ingresoHogar;edadPresentacionExamen;puntajeExamen
    // apoyosICETEX-aaaa-s.csv
    //   apellidos;nombres;tipoDocumento;documento;nombrePrograma;tipoCreditoICETEX
    String encabezadosA[]={
      "apellidos;nombres;tipoDocumento;documento;nombrePrograma;codigoEstudiante;sexo;fechaNacimiento;codigoSNIESprograma",
      "apellidos;nombres;tipoDocumento;documento;nombrePrograma;materiasTomadas;materiasAprobadas",
      "apellidos;nombres;tipoDocumento;documento;nombrePrograma",
      "apellidos;nombres;tipoDocumento;documento;nombrePrograma",
      "apellidos;nombres;tipoDocumento;documento;nombrePrograma",
      "apellidos;nombres;tipoDocumento;documento;nombrePrograma",
      "apellidos;nombres;tipoDocumento;documento;nombrePrograma",
      "apellidos;nombres;tipoDocumento;documento;nombrePrograma;numeroHermanos;posicionEntreHermanos;viviendaPropia;trabajaba;nivelEducativoMadre;ingresoHogar;edadPresentacionExamen;puntajeExamen",
      "apellidos;nombres;tipoDocumento;documento;nombrePrograma;tipoCreditoICETEX"
    };
    String encabezadosB[]=new String[NUM_ARCHS];
    for (int i=0; i<NUM_ARCHS; i++) encabezadosB[i]=encabezadosA[i].replace(';',',');
    for (int indSem=0; indSem<numSems; indSem++) {
      int anho=Integer.parseInt(sems[indSem].substring(0,4)),sem=Integer.parseInt(sems[indSem].substring(4,5));
      for (int i=0; i<NUM_ARCHS; i++) {
        File f=new File(carpeta,anho+"-"+sem+"-"+sufijos[i]+".csv");
        if (!f.exists()) continue;
        notificar(paso+" ("+anho+"-"+sem+"-"+sufijos[i]+".csv)",contArchivos,numArchivos,1);
        if ((contArchivos%5)==0) {
          try {Thread.sleep(25);} catch (Throwable th) {}
        }
        BufferedReader br=new BufferedReader(new FileReader(f)); String sL=br.readLine(); int hL=0;
        if (sL==null) sL="";
        sL=sL.replaceAll("\\\"","");
        while (sL.endsWith(";") || sL.endsWith(",")) sL=sL.substring(0,sL.length()-1);
        char sep='\0';
        if (i==0) {
          if (sL.trim().equals(encabezadosA[0]) || sL.trim().startsWith(encabezadosA[0]+";")) sep=';';
          if (sL.trim().equals(encabezadosB[0]) || sL.trim().startsWith(encabezadosB[0]+",")) sep=',';
          if (sep=='\0') throw new MyException("La primera línea del archivo \""+f.getPath()+"\" debe comenzar por \""+encabezadosA[0]+"\" o por \""+encabezadosB[0]+"\"");
          String wL[]=csvToString(sL,20,sep);
          for (int h=0; h<Constantes.maxVariablesExtra; h++) if (variablesExtra[h].nombre.length==0) variablesExtra[h].nombre=wL[9+h].replaceAll("\\<|\\>","").trim().getBytes();
        }
        else if (i==1) {
          if (sL.trim().equals(encabezadosA[1]) || sL.trim().startsWith(encabezadosA[1]+";")) sep=';';
          if (sL.trim().equals(encabezadosB[1]) || sL.trim().startsWith(encabezadosB[1]+",")) sep=',';
          if (sep=='\0') throw new MyException("La primera línea del archivo \""+f.getPath()+"\" debe comenzar por \""+encabezadosA[0]+"\" o por \""+encabezadosB[0]+"\"");
          String wL[]=csvToString(sL,20,sep);
          for (int h=0; h<Constantes.maxVariablesExtraDinamicas; h++) if (variablesExtra2[h].nombre.length==0) variablesExtra2[h].nombre=wL[7+h].replaceAll("\\<|\\>","").trim().getBytes();
        }
        else {
          if (sL.trim().equals(encabezadosA[i])) sep=';';
          if (sL.trim().equals(encabezadosB[i])) sep=',';
          if (sep=='\0') throw new MyException("La primera línea del archivo \""+f.getPath()+"\" debe ser \""+encabezadosA[i]+"\" o \""+encabezadosB[i]+"\""); 
        }
        for (; (sL=br.readLine())!=null; hL++) {
          //TODO verificar funcionamiento
          if (sL.trim().length()==0) continue; //Ignorar linea vacia
          String wL[]=csvToString(sL,20,sep),sApellidos=codifLetras.limpiarString(wL[0]),sNombres=codifLetras.limpiarString(wL[1]),sDoc=leerNumeroNatural(wL[3]),sPrograma=codifNombreProgramas.limpiarString(wL[4]);
          if (sApellidos.length()+sNombres.length()+sDoc.length()+sPrograma.length()==0)
            continue; //Sin informacion general
          byte sTipoDoc=leerTipoDocumento(wL[2].trim());
          EstudianteSinc e=buscarEstudiante(sApellidos,sNombres,sTipoDoc,sDoc,sPrograma,mapEstudiantes1,mapEstudiantes2);
          ProgramaSinc p=programas.get(sPrograma.getBytes());
          if (p==null) {
            programas.put(sPrograma.getBytes(),p=new ProgramaSinc());
            p.nombre=sPrograma.getBytes();
          }
          if (i==0) {
            e.semPrimiparo=(e.semPrimiparo==-1)?indSem:Math.min(e.semPrimiparo,indSem);
            e.codigo=wL[5].replace('\0',' ').trim().getBytes();
            e.sexo=leerSexo(wL[6].trim());
            e.fechaNac=leerFecha(wL[7]).getBytes();
            p.codigos.add(leerNumeroNatural(wL[8]).getBytes());
            for (int h=0; h<Constantes.maxVariablesExtra; h++) e.valoresVariablesExtras[h]=variablesExtra[h].addValor(wL[9+h].trim().getBytes());
          }
          else if (i==1) {
            e.matriculado[indSem]=true;
            e.materiasTomadas[indSem]=stringToInteger(leerNumeroNatural(wL[5]));
            e.materiasAprobadas[indSem]=stringToInteger(leerNumeroNatural(wL[6]));
            for (int h=0; h<Constantes.maxVariablesExtraDinamicas; h++) e.valoresVariablesExtras2[h][indSem]=variablesExtra2[h].addValor(wL[7+h].trim().getBytes());
          }
          else if (i==2) {
            e.semGraduado=Math.max(e.semGraduado,indSem);
          }
          else if (i==3) {
            e.semRetiroForzoso=Math.max(e.semRetiroForzoso,indSem);
          }
          else if (i==4) {
            e.apoyosAcademicos[indSem]=true;
          }
          else if (i==5) {
            e.apoyosFinancieros[indSem]=true;
          }
          else if (i==6) {
            e.apoyosOtros[indSem]=true;
          }
          else if (i==7) {
            e.numeroHermanos=stringToInteger(leerNumeroNatural(wL[5]));
            e.posicionEntreHermanos=stringToInteger(leerNumeroNatural(wL[6]));
            e.viviendaPropia=stringToInteger(leerNumeroNatural(wL[7]));
            e.trabajaba=stringToInteger(leerNumeroNatural(wL[8]));
            e.nivelEducativoMadre=stringToInteger(leerNumeroNatural(wL[9]));
            e.ingresoHogar=stringToInteger(leerNumeroNatural(wL[10]));
            e.edadPresentacionExamen=stringToInteger(leerNumeroNatural(wL[11]));
            e.puntajeExamen=stringToInteger(leerNumeroNatural(wL[12]));
          }
          else if (i==8) {
            e.apoyosICETEX[indSem]=CajaDeHerramientas.leerTipoApoyoIcetex(wL[5]);
          }
        }
        br.close();
        contArchivos++;
      }
    }
    marcarDesercionPrograma(mapEstudiantes1,mapEstudiantes2);
    VariableExtra[] arregloVariablesExtra=new VariableExtra[Constantes.maxVariablesExtra];
    VariableExtra[] arregloVariablesExtra2=new VariableExtra[Constantes.maxVariablesExtraDinamicas];
    for (int h=0; h<Constantes.maxVariablesExtra; h++) {
      variablesExtra[h].arreglarDatos();
      arregloVariablesExtra[h]=variablesExtra[h].getVariableExtra();
    }
    for (int h=0; h<Constantes.maxVariablesExtraDinamicas; h++) {
      variablesExtra2[h].arreglarDatos();
      arregloVariablesExtra2[h]=variablesExtra2[h].getVariableExtra();
    }
    programas.remove(new byte[0]);
    {//Remover programas que no aparecen en primiparos
      SortedSet<byte[]> progNomActivos = new TreeSet<byte[]>(comparadorByteArray);
      for (EstudianteSinc e:estudiantes)
        if (e.semPrimiparo!=-1)
          progNomActivos.add(e.programa);
      programas.keySet().retainAll(progNomActivos);
    }
    List<Programa> listaProgramas=new ArrayList<Programa>(100);
    for (ProgramaSinc p:programas.values()) {
      {
        Set<String> cods = new TreeSet<String>();
        for (byte[] s:p.codigos) cods.add(new String(s));
        //for (String s:cods) System.err.println(s);
      }
      p.arreglarDatos();
      listaProgramas.add(p.getPrograma());
    }
    byte[][] nombresProgramas=programas.keySet().toArray(new byte[0][]);
    List<Estudiante> listaEstudiantes=new ArrayList<Estudiante>(10000);
    List<Estudiante_DatosPersonales> listaDatosPersonalesEstudiantes=new ArrayList<Estudiante_DatosPersonales>(10000);
    int n=sems.length;
    ies.codigo=Integer.parseInt(codigoIES);
    ies.n=n;
    ies.version = Constantes.verDatos;
    carg = CargadorEstudiantes.getCargador(ies.version, ies.n);
    ies.minCodigoSemestre=getCodigoSemestre(sems[0]);
    ies.maxCodigoSemestre=getCodigoSemestre(sems[sems.length-1]);
    ies.semestres=sems;    
    for (EstudianteSinc e:estudiantes) if (e.semPrimiparo!=-1) {
      e.arreglarDatos();
      listaEstudiantes.add(e.getEstudiante(nombresProgramas,arregloVariablesExtra,arregloVariablesExtra2));
      listaDatosPersonalesEstudiantes.add(e.getDatosPersonalesEstudiante(nombresProgramas));
    }
    for (int h=0; h<Constantes.maxVariablesExtra; h++) ies.variablesExtras[h]=arregloVariablesExtra[h];
    for (int h=0; h<Constantes.maxVariablesExtraDinamicas; h++) ies.variablesExtrasD[h]=arregloVariablesExtra2[h];
    ies.programas=listaProgramas.toArray(new Programa[0]);
    ies.costosProgramas = new int[ies.programas.length][ies.n];
    for (int[] costosPrograma: ies.costosProgramas)
      Arrays.fill(costosPrograma, -1);
    ies.estudiantes=listaEstudiantes.toArray(new Estudiante[0]);
    ies.datosPersonalesEstudiantes=listaDatosPersonalesEstudiantes.toArray(new Estudiante_DatosPersonales[0]);
  }
  private void marcarDesercionPrograma(MyListMap<byte[], EstudianteSinc> mapEstudiantes1, MyListMap<byte[], EstudianteSinc> mapEstudiantes2) {
    long t0 = System.currentTimeMillis();
    for (Entry<byte[], List<EstudianteSinc>> ent : mapEstudiantes1.entrySet()) {
      byte[] nom = ent.getKey();
      List<EstudianteSinc> le = ent.getValue();
      if (nom.length<10 || le.size()==1 || le.size()>5) continue;//Si el nombre es muy pequeño o es uno solo en el grupo o son mas de 5
      List<List<EstudianteSinc>> pares = new LinkedList<List<EstudianteSinc>>();
      for (EstudianteSinc e:le) {
        List<List<EstudianteSinc>> emparejados = new LinkedList<List<EstudianteSinc>>();
        for (List<EstudianteSinc> par:pares) {
          for (EstudianteSinc est:par) {
            if (e.equals(new String(est.apellidos), new String(est.nombres), est.tipoDoc, ""+est.doc, new String(e.programa))) {
              emparejados.add(par);
              break;
            }
          }
        }
        if (emparejados.size()==0) {
          pares.add(new LinkedList(Arrays.asList(e)));
        } else if (emparejados.size()==1) {
          emparejados.iterator().next().add(e);
        } else {//Pertenece a varios grupos: Se fusionan y se agrega
          for (List<EstudianteSinc> emp:emparejados) pares.remove(emp);
          List<EstudianteSinc> nueva = new LinkedList<EstudianteSinc>();
          for (List<EstudianteSinc> emp:emparejados) nueva.addAll(emp);
          nueva.add(e);
          pares.add(nueva);
        }
      }
      for (List<EstudianteSinc> par:pares) {//Recorrer grupos armados
        if (par.size()==1) continue;
        int maxPer = -1;
        for(EstudianteSinc e:par) {
          int ultPer = -1;
          for(int i = e.n-1;i>=0 && ultPer==-1;i--)
            if(e.matriculado[i]) ultPer = i;
          if (ultPer>maxPer) maxPer = ultPer;
        }
        if (maxPer!=-1)
          for(EstudianteSinc e:par) {
            int ultPer = -1;
            for(int i = e.n-1;i>=0 && ultPer==-1;i--)
              if(e.matriculado[i]) ultPer = i;
            if (ultPer!=-1 && ultPer<maxPer) e.estDes = -3;
          }
      }
    }
  }
  private EstudianteSinc buscarEstudiante(String sApellidos, String sNombres, byte sTipoDoc, String sDoc, String sPrograma, MyListMap<byte[],EstudianteSinc> mapEstudiantes1, MyListMap<byte[],EstudianteSinc> mapEstudiantes2) throws Exception {
    byte[] k1=(sApellidos+"\0"+sNombres).getBytes();
    List<EstudianteSinc> le=mapEstudiantes1.get(k1);
    int u=Math.max(estudiantes.size()/20,14);
    if (le!=null && le.size()<u) {
      EstudianteSinc e=buscarEstudiante(sApellidos,sNombres,sTipoDoc,sDoc,sPrograma,le);
      if (e!=null) return e;
    }
    String w[]={sApellidos.replaceAll(" ",""),sNombres.replaceAll(" ","")};
    List<byte[]> k2=new ArrayList<byte[]>(2);
    for (String s:w) {
      byte[] b1=new byte[TAM_BLOQUE],b2=s.getBytes();
      System.arraycopy(b2,0,b1,0,Math.min(TAM_BLOQUE,b2.length));
      k2.add(b1);
      le=mapEstudiantes2.get(b1);
      if (le!=null && le.size()<u) {
        EstudianteSinc e=buscarEstudiante(sApellidos,sNombres,sTipoDoc,sDoc,sPrograma,le);
        if (e!=null) return e;
      }
    }
    /*
    {
      le=estudiantes;
      EstudianteSinc e=buscarEstudiante(sApellidos,sNombres,sTipoDoc,sDoc,sPrograma,le);
      if (e!=null) return e;
    }
    */
    EstudianteSinc e=new EstudianteSinc(sems.length);
    e.apellidos=sApellidos.getBytes();
    e.nombres=sNombres.getBytes();
    e.tipoDoc=sTipoDoc;
    try {e.doc=Math.max(stringToLong(leerNumeroNatural(sDoc)),-1L);} catch (Throwable th) {}
    if (e.doc>=0) e.xDoc=(""+e.doc).getBytes();
    e.programa=sPrograma.getBytes();
    mapEstudiantes1.add(k1,e);
    for (byte[] b:k2) mapEstudiantes2.add(b,e);
    estudiantes.add(e);
    return e;
  }
  private static EstudianteSinc buscarEstudiante(String sApellidos, String sNombres, byte sTipoDoc, String sDoc, String sPrograma, List<EstudianteSinc> le) {
    for (EstudianteSinc e:le) if (e.equals(sApellidos,sNombres,sTipoDoc,sDoc,sPrograma)) {
      if (e.tipoDoc==-1 && sTipoDoc>=0) e.tipoDoc=sTipoDoc;
      return e;
    }
    return null;
  }
  private void traerDatosExternos() throws Exception {
    final String paso2="Paso 2 de 5 de la IES "+codigoIES+": Enviando datos al Ministerio de Educación Nacional";
    final String paso3="Paso 3 de 5 de la IES "+codigoIES+": Complementando los datos de la institución en el Ministerio de Educación Nacional";
    final String paso4="Paso 4 de 5 de la IES "+codigoIES+": Recibiendo datos del Ministerio de Educación Nacional";
    notificar("Estableciendo comunicación con el servidor \""+Constantes.ipServidorSPADIES+"\".",-1,-1,2);
    Socket socket=null;
    MyDataOutputStream out=null;
    MyDataInputStream in=null;
    try {
      socket=new Socket(Constantes.ipServidorSPADIES,Constantes.puertoServidorMatch);
      out=new MyDataOutputStream(socket.getOutputStream());
      in=new MyDataInputStream(socket.getInputStream());
      socket.setSoTimeout(1000*40);
    }
    catch (Throwable th) {
      throw new MyException("No se pudo establecer comunicación con el servidor \""+Constantes.ipServidorSPADIES+"\" por el puerto "+Constantes.puertoServidorMatch+". Revise la configuración de su firewall para permitir a la aplicación comunicarse con el servidor del Ministerio de Educación Nacional o descargue la nueva versión de la aplicación SPADIES.");
    }
    try {
      out.writeLong(1654949798791L);
      out.writeInt(Integer.parseInt(codigoIES));
      out.flush();
      if (in.readLong()!=3571984365291298800L) throw new MyException("Actualice la aplicación SPADIES para poder sincronizar con el servidor del Ministerio de Educación Nacional.");
      long tiempo=in.readLong();
      try {
        if (in.readLong()!=4897197444879L) throw new Exception("");
      }
      catch (Throwable th) {
        throw new MyException("El código de institución "+codigoIES+" es inválido. Revise con el Ministerio de Educación Nacional el código asignado a su institución.");
      }
      out.writeByteArray(true,CajaDeHerramientas.getCipherEncrypt(new String(password)+";"+tiempo).doFinal("m. ! < sS@#tYm !ñQ/X".getBytes())); out.flush();
      try {
        String msg=new String(CajaDeHerramientas.getCipherDecrypt(new String(password)+";"+tiempo).doFinal(in.readByteArray(true,-1)));
        if (!msg.equals("1W bT,^-Mn|5Q rP{a]!")) throw new Exception("");
      }
      catch (Throwable th) {
        throw new MyException("Contraseña inválida.");
      }
      byte[] baIES;
      out.writeLong(4925672961244L); out.flush();
      try {
        if (in.readLong()!=5709386157069L) throw new Exception("");
      }
      catch (Throwable th) {
        throw new MyException("Contraseña inválida.");
      }
      out.writeLong(9713657941972L); out.flush();
      baIES=IES.iesToByteArray(ies,new String(password)+";"+tiempo);
      final int ini1=out.getNumeroBytesEscritos(),tot1=baIES.length;
      notificar(paso2,0,tot1,2);
      out.setListener(new MyListener() {
        public void notify(Object obj) {
          notificar(paso2,(long)(((Integer)obj)-ini1),tot1,2);
        }
      });
      out.writeLargeByteArray(true,baIES); out.flush();
      out.setListener(null);
      notificar(paso2,tot1,tot1,2);
      out.writeLong(4902403597593L); out.flush();
      if (in.readLong()!=1020579306849L) throw new Exception("");
      JDialog dialogColaAtencion=null;
      MyEditorPane ep=new MyEditorPane(true,"");
      {
        MyScrollPane sp=new MyScrollPane(ep,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,350,90);
        sp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),BorderFactory.createEtchedBorder()));
        dialogColaAtencion=new JDialog(padre,"Cola de atención",false);
        dialogColaAtencion.setContentPane(new MyBorderPane(false,3,3,3,3,null,null,sp,null,null));
        dialogColaAtencion.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialogColaAtencion.pack();
        dialogColaAtencion.setLocation(padre.getX(),padre.getY()+padre.getHeight());
      }
      notificar(paso3,0,100,3);
      try {
        while (true) {
          if (in.readLong()!=4671937598247L) throw new Exception("");
          int posEnCola=in.readInt();
          if (posEnCola==0) break;
          if (!dialogColaAtencion.isVisible()) dialogColaAtencion.setVisible(true);
          ep.setText("El servidor del Ministerio se encuentra ocupado atendiendo a otras instituciones. "+
              "Su puesto en la cola de atención es el # "+(posEnCola+1)+". Espere aproximadamente "+(posEnCola*5)+" minutos para ser atendido.");
          ep.repaint();
        }
      }
      catch (Exception ex) {
        throw ex;
      }
      finally {
        if (dialogColaAtencion.isVisible()) dialogColaAtencion.setVisible(false);
      }
      if (in.readLong()!=7049449709348L) throw new Exception("");
      long tot2=in.readLong();
      for (int g=0; g<3; g++) {
        if (in.readLong()!=Long.MAX_VALUE) throw new Exception("");
        for (long v; (v=in.readLong())!=Long.MAX_VALUE; ) notificar(paso3,v,tot2,3);
      }
      notificar(paso3,tot2,tot2,3);
      if (in.readLong()!=9403879046015L) throw new Exception("");
      final int ini3=in.getNumeroBytesLeidos(),tot3=in.readInt();
      notificar(paso4,0,tot3,4);
      in.setListener(new MyListener() {
        public void notify(Object obj) {
          notificar(paso4,(long)(((Integer)obj)-ini3),tot3,4);
        }
      });
      baIES=in.readLargeByteArray(true,-1);
      ies=IES.byteArrayToIES(baIES,new String(password)+";"+tiempo);
      if (ies.codigo==-1) throw new MyException("La información reportada no coincide con la información histórica.");
      in.setListener(null);
      notificar(paso4,tot3,tot3,4);
      if (in.readLong()!=1972387712038L) throw new Exception("");
    }
    catch (MyException ex) {
      throw ex;
    }
    catch (Throwable th) {
      throw new MyException("Hubo un error en la comunicación con el servidor \""+Constantes.ipServidorSPADIES+"\".",th);
    }
    finally {
      try {
        if (out!=null) out.close();
        if (in!=null) in.close();
        socket.close();
      }
      catch (Throwable th) {
      }
    }
  }
  private void escribirRespuesta(File carpetaOut) throws Exception {
    File file=IES.getFile(carpetaOut,ies);
    String paso="Paso 5 de 5 de la IES "+codigoIES+": Escribiendo la información de la IES en el archivo \""+file.getPath()+"\".";
    notificar(paso,-1,-1,5);
    padre.puedeCerrar=false;
    try {
      IES.guardar(carpetaOut,ies,true);
      notificar(paso,100,100,5);
    }
    catch (MyException ex) {
      throw ex;
    }
    catch (Throwable th) {
      throw new MyException("Hubo un error guardando el archivo \""+file.getPath()+"\".");
    }
    finally {
      padre.puedeCerrar=true;
    }
  }
  private class VariableExtraSinc {
    byte[] nombre={};
    Map<byte[],byte[]> valores=new TreeMap<byte[],byte[]>(comparadorByteArray);
    byte[] addValor(byte[] val) {
      if (val.length==0) return null;
      byte[] v=valores.get(val);
      if (v!=null) return v;
      valores.put(val,val);
      return val;
    }
    void arreglarDatos() throws Exception {
      if (nombre.length==0) valores.clear();
      //TODO Cambiar mensaje primiparos A generalidad
      if (valores.size()>100) throw new MyException("En las tablas de primíparos de la institución "+codigoIES+", la columna \""+new String(nombre)+"\" no debe contener más de 100 valores distintos. Actualmente, tal columna presenta "+valores.size()+" valores distintos.");
    }
    VariableExtra getVariableExtra() {
      VariableExtra ve=new VariableExtra();
      if (nombre.length==0 || valores.isEmpty()) return ve;
      ve.nombre=nombre;
      ve.nombresValores=valores.keySet().toArray(new byte[0][]);
      return ve;
    }
  }
  private class ProgramaSinc {
    byte[] nombre={};
    Collection<byte[]> codigos=new ArrayList<byte[]>(5);
    void arreglarDatos() {
    }
    Programa getPrograma() {
      Programa p=new Programa();
      p.nombre=nombre;
      for (byte[] b:codigos) if (b.length==21) {p.codigoSNIES=b; break;}
      if (p.codigoSNIES.length==0) for (byte[] b:codigos) if (b.length>=p.codigoSNIES.length) p.codigoSNIES=b;
      return p;
    }
  }
  private class EstudianteSinc {
    int n;
    byte[] apellidos={};
    byte[] nombres={};
    byte tipoDoc=-1;
    long doc=-1;
    byte[] xDoc={};
    byte[] programa={};
    byte[] codigo={};
    byte sexo=-1;
    byte[] fechaNac={};
    byte[][] valoresVariablesExtras=new byte[Constantes.maxVariablesExtra][];
    byte[][][] valoresVariablesExtras2=new byte[Constantes.maxVariablesExtraDinamicas][n][];
    int semPrimiparo=-1,semGraduado=-1,semRetiroForzoso=-1;
    byte estDes = -1;
    boolean matriculado[]=null;
    int materiasTomadas[]=null;
    int materiasAprobadas[]=null;
    boolean apoyosAcademicos[]=null;
    boolean apoyosFinancieros[]=null;
    boolean apoyosOtros[]=null;
    byte apoyosICETEX[]=null;
    int numeroHermanos=-1,posicionEntreHermanos=-1,viviendaPropia=-1,trabajaba=-1,nivelEducativoMadre=-1,ingresoHogar=-1,edadPresentacionExamen=-1,puntajeExamen=-1;
    EstudianteSinc(int pN) {
      n=pN;
      matriculado=new boolean[n];
      materiasTomadas=new int[n];
      materiasAprobadas=new int[n];
      Arrays.fill(materiasTomadas,-1);
      Arrays.fill(materiasAprobadas,-1);
      apoyosAcademicos=new boolean[n];
      apoyosFinancieros=new boolean[n];
      apoyosOtros=new boolean[n];
      apoyosICETEX=new byte[n];
      valoresVariablesExtras2 = new byte[Constantes.maxVariablesExtraDinamicas][n][0];
    }
    void arreglarDatos() {
      if (semPrimiparo==-1) return;
      if (semGraduado!=-1 && semGraduado<semPrimiparo) semGraduado=-1;
      if (semRetiroForzoso!=-1 && semRetiroForzoso<semPrimiparo) semRetiroForzoso=-1;
      if (semRetiroForzoso!=-1 && semGraduado!=-1) semRetiroForzoso=-1;
      for (int i=0; i<n; i++) {
        if (i>=semPrimiparo && (semGraduado==-1 || i<=semGraduado)) {
          if (i==semPrimiparo) matriculado[i]=true;
          if (materiasTomadas[i]<0 || materiasAprobadas[i]<0 || materiasTomadas[i]>100 || materiasAprobadas[i]>100) materiasTomadas[i]=materiasAprobadas[i]=-1;
          if (materiasAprobadas[i]>materiasTomadas[i]) materiasAprobadas[i]=materiasTomadas[i];
          if (materiasTomadas[i]>0 || materiasAprobadas[i]>0 || apoyosAcademicos[i] || apoyosFinancieros[i] || apoyosOtros[i]) matriculado[i]=true;
          if (matriculado[i] && semRetiroForzoso!=-1 && semRetiroForzoso<i) semRetiroForzoso=-1;
        }
        else {
          matriculado[i]=false;
          materiasTomadas[i]=materiasAprobadas[i]=-1;
          apoyosAcademicos[i]=apoyosFinancieros[i]=apoyosOtros[i]=false;
          apoyosICETEX[i]=0;
        }
      }
    }
    Estudiante getEstudiante(byte[][] nombresProgramas, VariableExtra[] arregloVariablesExtra, VariableExtra[] arregloVariablesExtra2) {
      Estudiante e=carg.estudianteVacio();//new Estudiante(n,null);
      e.setSexo(sexo);
      e.setIndicePrograma(Math.max(Arrays.<byte[]>binarySearch(nombresProgramas,programa,comparadorByteArray),-1));
      e.setSemestrePrimiparo(semPrimiparo);
      e.setSemestreRetiroForzoso(semRetiroForzoso);
      e.setSemestreGrado(semGraduado);
      long vMatri=0L;
      for (int i=0; i<n; i++) {
        vMatri<<=1;
        if (matriculado[i]) vMatri|=1L;
      }
      e.setSemestresMatriculadoAlReves(vMatri);
      for (int i=0; i<n; i++) {
        if (materiasTomadas[i]>=0) e.setNumeroMateriasTomadas(i,materiasTomadas[i]);
        if (materiasAprobadas[i]>=0) e.setNumeroMateriasAprobadas(i,materiasAprobadas[i]);
        if (apoyosAcademicos[i]) e.setRecibioApoyoAcademico(i,true);
        if (apoyosFinancieros[i]) e.setRecibioApoyoFinanciero(i,true);
        if (apoyosOtros[i]) e.setRecibioApoyoOtro(i,true);
        if (apoyosICETEX[i]!=0) e.setTipoApoyoICETEXRecibido(i,apoyosICETEX[i]);
        for (int h=0; h<Constantes.maxVariablesExtraDinamicas; h++) if (valoresVariablesExtras2[h][i]!=null && arregloVariablesExtra2[h].nombre.length>0) {
          e.setValorVariableExtra2(h,i,Math.max(-1,Arrays.<byte[]>binarySearch(arregloVariablesExtra2[h].nombresValores,valoresVariablesExtras2[h][i],comparadorByteArray)));
        }
      }
      for (int h=0; h<Constantes.maxVariablesExtra; h++) if (valoresVariablesExtras[h]!=null && arregloVariablesExtra[h].nombre.length>0) {
        e.setValorVariableExtra(h,Math.max(-1,Arrays.<byte[]>binarySearch(arregloVariablesExtra[h].nombresValores,valoresVariablesExtras[h],comparadorByteArray)));
      }

      if (numeroHermanos!=-1 || posicionEntreHermanos!=-1 || viviendaPropia!=-1 || trabajaba!=-1 || nivelEducativoMadre!=-1 || ingresoHogar!=-1 || edadPresentacionExamen!=-1 || puntajeExamen!=-1) {
        e.setSonDatosICFESproveidosPorSNIES(true);
        e.setNumeroHermanos(numeroHermanos);
        e.setPosicionEntreLosHermanos(posicionEntreHermanos);
        e.setViviendaPropia(viviendaPropia);
        e.setTrabajabaCuandoPresentoIcfes(trabajaba);
        e.setNivelEducativoMadre(nivelEducativoMadre);
        e.setIngresoHogar(ingresoHogar);
        e.setEdadAlPresentarElICFES(edadPresentacionExamen);
        e.setPuntajeICFES(puntajeExamen);
      }
      if (e.getEstado()==-1) e.setEstadoDesercion(estDes);
      return e;
    }
    Estudiante_DatosPersonales getDatosPersonalesEstudiante(byte[][] nombresProgramas) {
      Estudiante_DatosPersonales edp=new Estudiante_DatosPersonales();
      edp.apellido=apellidos;
      edp.nombre=nombres;
      edp.tipoDocumento=tipoDoc;
      edp.documento=doc;
      edp.codigo=codigo;
      String sFechaNac=new String(fechaNac);
      if (sFechaNac.length()>0) {
        int anho=Integer.parseInt(sFechaNac.substring(0,4)),mes=Integer.parseInt(sFechaNac.substring(5,7)),dia=Integer.parseInt(sFechaNac.substring(8,10));
        while (anho<1900) anho+=100;
        if (anho>1850 && anho<2100 && mes>=1 && mes<=12 && dia>=1 && dia<=31) {
          edp.anhoFechaNacimiento=(short)anho;
          edp.mesFechaNacimiento=(byte)mes;
          edp.diaFechaNacimiento=(byte)dia;
        }
      }
      return edp;
    }
    boolean equals(String sApellidos, String sNombres, byte sTipoDoc, String sDoc, String sPrograma) {
      if (programa.length>=3 && sPrograma.length()>=3 && cc.distancia(sPrograma.getBytes(),programa,codifNombreProgramas)<90.0) return false;
      String s1=(sApellidos+" "+sNombres).trim(),s2=(new String(apellidos)+" "+new String(nombres)).trim();
      double d=cc.distancia(s1.getBytes(),s2.getBytes(),codifLetras);
      for (String[] z:new String[][]{{s1,s2},{s2,s1}}) {
        int pc=z[0].indexOf(' ');
        if (pc==-1) continue;
        String s=z[0].substring(pc).trim();
        d=Math.max(d,(s.equals(z[1])?100:0)-20.0/Math.pow(s.length()+z[1].length()+1,0.8));
      }
      int a=nombres.length+apellidos.length;
      if (a<=1) return false;
      if (a<=4) return d==100.0;
      if (a>=18 && d>=99.8) return true;
      return d>=98.0 && (sTipoDoc!=tipoDoc || xDoc.length<3 || sDoc.length()<3 || cc.distancia(sDoc.getBytes(),xDoc,codifNumeros)>=80.0);
    }
  }  
  private static class CalificadorCadenas {
    private final short S[][];
    public CalificadorCadenas() {
      S=new short[50][50];
    }
    public double distancia(byte[] a1, byte[] a2, CodificadorBytes cb) {
      int n=a1.length,m=a2.length,v=Math.min(n,m),u=Math.max(n,m),s=0;
      if (v==0 || u<=1) return 0.0;
      for (int i=1; i<n; i++) {S[cb.getCodigo(a1[i-1])][cb.getCodigo(a1[i])]++; s++;}
      for (int j=1; j<m; j++) {if (--S[cb.getCodigo(a2[j-1])][cb.getCodigo(a2[j])]>=0) s--; else s++;}
      for (int i=1; i<n; i++) S[cb.getCodigo(a1[i-1])][cb.getCodigo(a1[i])]=0;
      for (int j=1; j<m; j++) S[cb.getCodigo(a2[j-1])][cb.getCodigo(a2[j])]=0;
      return 100.0*Math.max(1.0-Math.pow(1.2*s/u,5.0/Math.log10(u+1)),0);
    }
  }
}
