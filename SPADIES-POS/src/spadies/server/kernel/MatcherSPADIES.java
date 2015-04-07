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
package spadies.server.kernel;

import java.io.*;
import java.util.*;
import spadies.io.*;
import spadies.server.util.*;
import static spadies.util.CajaDeHerramientas.*;

public class MatcherSPADIES {
  private final CalificadorMatcher califLetrasServer=new CalificadorMatcher(codifLetrasServer.numCodigos());
  private final CalificadorMatcher califNumeros=new CalificadorMatcher(codifNumeros.numCodigos());
  private final byte[][][] INFO_IES,INFO_BASE;
  private final double calificacionMinima;
  private final boolean competirMejores;
  private final File archivoBase;
  private final MyDataOutputStream os;
  private final int T1,T2,VECES,maxIntentos;
  public final long total1,total2,total;
  private long progreso1=0,progreso2=0;
  public MatcherSPADIES(byte[][][] pINFO_IES, double pCalificacionMinima, boolean pCompetirMejores, File pArchivoBase, MyDataOutputStream pOs) throws Exception {
    INFO_IES=pINFO_IES;
    INFO_BASE=new byte[3][ConstantesServer.TAM_TANDA][];
    calificacionMinima=pCalificacionMinima;
    competirMejores=pCompetirMejores;
    archivoBase=pArchivoBase;
    os=pOs;
    {
      MyDataInputStream is1=new MyDataInputStream(new FileInputStream(archivoBase));
      T1=is1.readInt();
      is1.close();
    }
    T2=INFO_IES[0].length;
    VECES=1+(T1-1)/ConstantesServer.TAM_TANDA;
    maxIntentos=Math.max(ConstantesServer.TAM_TANDA/20000,10);
    total1=T1;
    total2=1L*VECES*T2;
    total=total1+total2;
  }
  private void writeProgreso(long progreso) throws Exception {
    os.writeLong(progreso); os.flush();
  }
  public byte[][] procesar(long progresoLlevado, boolean[] filasAexcluir) throws Exception {
    writeProgreso(Long.MAX_VALUE);
    MyDataInputStream is1=new MyDataInputStream(new FileInputStream(archivoBase)); is1.readInt();
    MyDataInputStream is2=new MyDataInputStream(new FileInputStream(archivoBase.getPath()+".indices"));
    byte[][] RESP_IES=new byte[T2][];
    double[] RESP_IES_CALIF=new double[T2];
    for (int vez=1,h1=0; vez<=VECES; vez++) {
      for (int h=0,h1T=Math.min(vez*ConstantesServer.TAM_TANDA,T1); h1<h1T; h++,h1++,progreso1++) {
        if ((h1%40000)==0) writeProgreso(progresoLlevado+progreso1+progreso2);
        INFO_BASE[0][h]=is1.readByteArray(true,-1);  // Nombre
        INFO_BASE[1][h]=is1.readByteArray(true,-1);  // Documento
        INFO_BASE[2][h]=is1.readByteArray(true,-1);  // Información adicional
      }
      writeProgreso(progresoLlevado+progreso1+progreso2);
      MySortedArray arrNoms=new MySortedArray(INFO_BASE[0],comparadorByteArray,is2);
      MySortedArray arrNomsRev=new MySortedArray(INFO_BASE[0],comparadorByteArrayRev,is2);
      MySortedArray arrDocs=new MySortedArray(INFO_BASE[1],comparadorByteArray,is2);
      MySortedArray arrDocsRev=new MySortedArray(INFO_BASE[1],comparadorByteArrayRev,is2);
      for (int h2=0; h2<T2; h2++,progreso2++) {
        if ((h2%5000)==0) writeProgreso(progresoLlevado+progreso1+progreso2);
        if (filasAexcluir!=null && filasAexcluir[h2]) continue;
        if (INFO_IES[0][h2].length>0) {
          mejor=RESP_IES[h2]; mejorCalificacion=RESP_IES_CALIF[h2]; visitados=new TreeSet<Integer>();
          comboMatch(INFO_IES[0][h2],INFO_IES[1][h2],INFO_IES[2][h2],arrNoms,arrNomsRev,arrDocs,arrDocsRev);
          RESP_IES[h2]=mejor; RESP_IES_CALIF[h2]=mejorCalificacion; visitados=null;
        }
      }
      for (int y=0; y<3; y++) Arrays.fill(INFO_BASE[y],null);
      arrNoms=arrNomsRev=arrDocs=arrDocsRev=null;
      System.gc();
      writeProgreso(progresoLlevado+progreso1+progreso2);
    }
    is1.close();
    is2.close();
    writeProgreso(Long.MAX_VALUE);
    return RESP_IES;
  }
  private byte[] mejor; private double mejorCalificacion; private Set<Integer> visitados;
  private void comboMatch(byte[] bNom2, byte[] bDoc2, byte[] bNac2, MySortedArray arrNoms, MySortedArray arrNomsRev, MySortedArray arrDocs, MySortedArray arrDocsRev) {
    byte[][] qArr={bNom2,bNom2,bDoc2,bDoc2};
    MySortedArray[] saArr={arrNoms,arrNomsRev,arrDocs,arrDocsRev};
    for (int f=0; f<4; f+=2) {
      byte[] q=qArr[f]; MySortedArray sa=saArr[f];
      if (q.length>=3) {
        int ind=sa.search(q);
        if (ind>=0) match(sa,new int[]{ind,ind+1},bNom2,bDoc2,bNac2,Integer.MAX_VALUE,false);
      }
      if (competirMejores && mejorCalificacion>=99.5) return;
    }
    for (int f=0; f<4; f++) {
      byte[] q=qArr[f]; MySortedArray sa=saArr[f];
      int t=q.length;
      byte[] qZ1=new byte[t+1],qZ2=new byte[t+1];
      Arrays.fill(qZ1,(byte)254); Arrays.fill(qZ2,(byte)255);
      boolean bDer=(sa==arrNoms||sa==arrDocs);
      for (int i=0,iMin=(sa==arrNoms||sa==arrNomsRev)?5:4; i<t; i++) {
        if (bDer) qZ1[i]=qZ2[i]=q[i];
        else      qZ1[t-1-i]=qZ2[t-1-i]=q[t-1-i];
        if (i>=iMin && match(sa,sa.searchLimits(qZ1,qZ2),bNom2,bDoc2,bNac2,maxIntentos,false)) break;
      }
      if (competirMejores && mejorCalificacion>=99.5) return;
    }
    if (bNac2.length>0) match(arrDocs,arrDocs.searchLimits(bNac2,concatenarArreglos(bNac2,new byte[]{(byte)255})),bNom2,bDoc2,bNac2,Integer.MAX_VALUE,true);
  }
  private boolean match(MySortedArray sa, int[] lims, byte[] bNom2, byte[] bDoc2, byte[] bNac2, int max, boolean fueFechaNac) {
    int limInf=lims[0],limSup=lims[1];
    if (limSup-limInf>max) return false;
    for (int i=limInf; i<limSup; i++) for (int ind:sa.values[i]) {
      byte[] bNom1=INFO_BASE[0][ind],bDoc1=INFO_BASE[1][ind],bDat1=INFO_BASE[2][ind];
      if (fueFechaNac) {
        boolean z=(bNom1.length>=5 && bNom2.length>=5),z1=z,z2=z;
        for (int y=0; z1 && y<5; y++) z1=(bNom1[y]==bNom2[y]);
        if (!z1) for (int y=0; z2 && y<5; y++) z2=(bNom1[bNom1.length-1-y]==bNom2[bNom2.length-1-y]);
        if (!z1 && !z2) continue;
      }
      if (!visitados.add(ind)) continue;
      double dist=(bDoc2.length>=3 && comparadorByteArray.compare(bNom1,bNom2)==0 && comparadorByteArray.compare(bDoc1,bDoc2)==0)?100.0:0.0;
      if (dist==0.0) {
        double dist1=califLetrasServer.distancia(bNom1,bNom2)*0.98,dist2=califNumeros.distancia(bDoc1,bDoc2);
        if (bNac2.length==0 && bDoc1.length==11 && bDoc2.length>=3 && bDoc2.length<=9) dist2=Math.max(dist2,80.0);
        if (bNac2.length==6 && bDoc1.length>=6) {//Tarjeta de identidad vs Fecha nacimiento
          dist2=Math.max(dist2,califNumeros.distancia(bNac2,getSubArreglo(bDoc1,0,5))*0.99);
          if (comparadorByteArray.compare(getSubArreglo(bNac2,2,5),getSubArreglo(bDoc1,2,5))==0) dist2=Math.max(dist2,90.0);
        }
        dist=(dist1>=80 && dist2>=80.0)?(dist1+(100-dist1)*0.9*(dist2-76)/24.0):dist1;
      }
      if (dist>calificacionMinima) {
        if (competirMejores) {
          if (dist>mejorCalificacion) mejor=null;
          if (dist>=mejorCalificacion) {mejor=(mejor==null)?bDat1:concatenarArreglos(mejor,bDat1); mejorCalificacion=dist;}
        }
        else {
          mejor=(mejor==null)?bDat1:concatenarArreglos(mejor,bDat1);
          if (dist>=mejorCalificacion) mejorCalificacion=dist;
        }
      }
    }
    return true;
  }
  private static class CalificadorMatcher {
    private final short S[][];
    public CalificadorMatcher(int numCodigos) {
      S=new short[numCodigos][numCodigos];
    }
    public double distancia(byte[] a1, byte[] a2) {
      int n=a1.length,m=a2.length,v=Math.min(n,m),u=Math.max(n,m),s=0;
      if (v==0 || u<=1) return 0.0;
      for (int i=1; i<n; i++) {S[a1[i-1]][a1[i]]++; s++;}
      for (int j=1; j<m; j++) {if (--S[a2[j-1]][a2[j]]>=0) s--; else s++;}
      for (int i=1; i<n; i++) S[a1[i-1]][a1[i]]=0;
      for (int j=1; j<m; j++) S[a2[j-1]][a2[j]]=0;
      return 100.0*Math.max(1.0-Math.pow(1.2*s/u,5.0/Math.log10(u+1)),0);
    }
  }
}
