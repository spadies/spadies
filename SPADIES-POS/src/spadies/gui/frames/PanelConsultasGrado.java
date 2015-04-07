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
package spadies.gui.frames;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.jfree.chart.*;

import spadies.gui.util.*;
import spadies.gui.graficas.*;
import spadies.kernel.*;
import spadies.util.*;
import spadies.util.variables.*;

@SuppressWarnings("serial")
public class PanelConsultasGrado extends PanelConsultasDesercion {
  public void actionPerformed(ActionEvent e) {
    int ind=CajaDeHerramientas.searchSame(botones,e.getSource());
    if (ind==0) {
      cuadrando=true;
      for (int i=0; i<tam; i++) {
        panelsSeleccion[i].seleccionar(true);
        checksDiferenciacion[i].setSelected(false);
      }
      cuadrando=false;
      actualizarLabelSeleccion();
    }
    else if (ind==1) {
      Component cmDespl=this;
      try {
        boolean ag=VentanaPrincipal.getInstance().estaSeleccionandoAgregado();
        Object[] params=getComponentesParametros();
        MyPanelSeleccion[] arrPS=(MyPanelSeleccion[])(params[0]);
        JCheckBox[] arrCB=(JCheckBox[])(params[1]);
        Variable[] arrVE=(Variable[])(params[2]);
        EnumSet<Variable> mDiferenciados=EnumSet.noneOf(Variable.class);
        EnumMap<Variable,Filtro> mFiltros=new EnumMap<Variable,Filtro>(Variable.class);
        RutinasGUI.getSeleccion(arrVE,arrCB,arrPS,mDiferenciados,mFiltros);
        if (ag) {
          mDiferenciados.remove(Variable.PROGRAMA_EST);
          mFiltros.remove(Variable.PROGRAMA_EST);
        }
        Variable[] diferenciados=mDiferenciados.toArray(new Variable[0]);
        Filtro[] filtros=mFiltros.values().toArray(new Filtro[0]);
        new VentanaRealizarConsultaGrafica(ag,diferenciados,filtros).ejecutar();
      }
      catch (MyException ex) {
        RutinasGUI.desplegarError(cmDespl,"<html>"+CajaDeHerramientas.stringToHTML(ex.getMessage())+"</html>");
      }
      catch (OutOfMemoryError err) {
        RutinasGUI.desplegarError(cmDespl,"<html>Memoria RAM insuficiente para ejecutar el proceso.</html>");
      }
      catch (Throwable th) {
        th.printStackTrace();
        RutinasGUI.desplegarError(cmDespl,"<html>Hubo un error realizando la consulta.</html>");
      }
    }
  }
  private class VentanaRealizarConsultaGrafica extends MyDialogProgreso {
    private final boolean alMinisterio;
    private final Variable[] diferenciados;
    private final Filtro[] filtros;
    public VentanaRealizarConsultaGrafica(boolean pAlMinisterio, Variable[] pDiferenciados, Filtro[] pFiltros) {
      super(VentanaPrincipal.getInstance(),"Realizando la consulta"+(pAlMinisterio?" en el Ministerio de Educación Nacional":""),false);
      alMinisterio=pAlMinisterio;
      diferenciados=pDiferenciados;
      filtros=pFiltros;
    }
    public void ejecutar() {
      setVisible(true);
      new Thread() {
        public void run() {
          Window window=VentanaRealizarConsultaGrafica.this;
          try {
            generarGrafica();
          }
          catch (MyException ex) {
            RutinasGUI.desplegarError(window,"<html>"+CajaDeHerramientas.stringToHTML(ex.getMessage())+"</html>");
            window.dispose();
          }
          catch (OutOfMemoryError err) {
            RutinasGUI.desplegarError(window,"<html>Memoria RAM insuficiente para ejecutar el proceso.</html>");
            window.dispose();
          }
          catch (Throwable th) {
            th.printStackTrace();
            RutinasGUI.desplegarError(window,"<html>Hubo un error realizando la consulta.</html>");
            window.dispose();
          }
        }
      }.start();
    }
    @SuppressWarnings("unchecked")
    private void generarGrafica() throws Exception {
      Object[] resultado=obtenerResultado(alMinisterio,diferenciados,filtros);
      Map<byte[],int[]> resC=(Map<byte[],int[]>)(resultado[0]);
      Map<byte[],double[]> resP=(Map<byte[],double[]>)(resultado[1]);
      Integer[] codigosIESDif=(Integer[])(resultado[2]);
      String[] codigosProgramasDif=(String[])(resultado[3]);
      int tam=(Integer)(resultado[4]);
      ChartPanel graf=     FabricaGraficas.crearGraficaConteosPoblacion("","# de semestres cursados","# graduados",diferenciados,new Object[]{resC,codigosIESDif,codigosProgramasDif},false,false,0,null);
      ChartPanel grafPorc= FabricaGraficas.crearGraficaConteosPoblacion("","# de semestres cursados","% graduados",diferenciados,new Object[]{resP,codigosIESDif,codigosProgramasDif},true,false,0,null);
      int t=diferenciados.length,m=resC.size();
      String encFilas[][]=new String[t][m+1],encColumnas[]=new String[tam];
      for (int i=0; i<t; i++) encFilas[i][0]=diferenciados[i].nombre;
      {
        int ind=1;
        for (byte[] llave:resC.keySet()) {
          for (int i=0; i<t; i++) encFilas[i][ind]=Variable.toString(diferenciados[i],llave[i],codigosIESDif,codigosProgramasDif);
          ind++;
        }
      }
      for (int j=0; j<tam; j++) encColumnas[j]=""+(j+1);
      String[][] valores=new String[m][tam],valoresPorc=new String[m][tam];
      {
        int ind=0;
        for (Map.Entry<byte[],int[]> eC:resC.entrySet()) {
          int[] contC=eC.getValue();
          double[] contP=resP.get(eC.getKey());
          int tam2=tam;
          while (tam2>0 && contC[tam2-1]==0) tam2--;
          for (int i=0; i<tam2; i++) {
            valores[ind][i]=""+contC[i];
            valoresPorc[ind][i]=(contP==null)?"":(RutinasGUI.df_porcentaje.format(contP[i]));
          }
          for (int i=tam2; i<tam; i++) valores[ind][i]=valoresPorc[ind][i]="";
          ind++;
        }
      }
      InfoTabla tabla=new InfoTabla(valores,encFilas,encColumnas),tablaPorc=new InfoTabla(valoresPorc,encFilas,encColumnas);
      VentanaRealizarConsultaGrafica.this.dispose();
      new VentanaGrafica(VentanaPrincipal.getInstance(),"Resultado de la consulta",graf,grafPorc,tabla,tablaPorc,new MyEditorPane(false,getStringSeleccion())).setVisible(true);
    }
  }
  public static Object[] obtenerResultado(boolean alMinisterio, Variable[] diferenciados, Filtro[] filtros) throws MyException {
    Object[] resultado=null;
    if (!alMinisterio) {
      resultado=KernelSPADIES.getInstance().getGradoCohorteAcumulado(filtros,diferenciados);
    }
    else {
      // Variable.PROGRAMA_EST no debe estar ni en 'diferenciados' ni en 'filtros'
      //resultado=PuertaAlServidorDeConsultas.obtenerResultadoConsulta(1,Object[].class,filtros,diferenciados);
    }
    if (resultado==null) throw new MyException("La consulta no arrojó datos.");
    return resultado;
  }
}