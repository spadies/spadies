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
package spadies.gui.graficas;

import java.text.*;
import java.util.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.category.*;
import org.jfree.data.xy.*;
import spadies.util.*;
import spadies.util.variables.*;

public final class FabricaGraficas {
  private static final DecimalFormat df=new DecimalFormat("0.00%");
  private static TickUnitSource tick1 = new TickUnitSource() {
    public TickUnit getLargerTickUnit(TickUnit unit) {
      return new NumberTickUnit(1);
    }
    public TickUnit getCeilingTickUnit(TickUnit unit) {
      return new NumberTickUnit(1);
    }
    public TickUnit getCeilingTickUnit(double size) {
      return new NumberTickUnit(1);
    }};
  @SuppressWarnings({"serial","unchecked"})
  public static ChartPanel crearGraficaInformacionBasica(Variable variable, Object[] args, boolean porcentual) {
    boolean b=(variable.tipo==TipoVariable.TV_ESTUDIANTE);
    return crearGraficaInformacionBasica(variable,args,porcentual,
        b?"Período ingreso":"Período",
        porcentual?"Porcentaje":("Número de "+(b?"primíparos":"matriculados")),
        (b?"Primíparos":"Matriculados")+": "+variable.nombre
    );
  }
  @SuppressWarnings({"serial","unchecked"})
  public static ChartPanel crearGraficaInformacionBasica(Variable variable, Object[] args, boolean porcentual,String ejeX, String ejeY, String titulo) {
    int limsSems[]=(int[])(args[1]),limInf=limsSems[0],limSup=limsSems[1];
    RangoByte<Byte> rango=(RangoByte<Byte>)(variable.rango);
    Byte[] codsRango=rango.getRango();
    DefaultCategoryDataset series=new DefaultCategoryDataset();
    for (int j=limInf; j<=limSup; j++) for (byte b:codsRango) if (b!=-1) {
      double val=porcentual?(((double[][])(args[0]))[b+1][j-limInf]):(((int[][])(args[0]))[b+1][j-limInf]);
      series.addValue(val,rango.toString(b),CajaDeHerramientas.codigoSemestreToString((byte)j));
    }
    BarRenderer3D rend=new BarRenderer3D(1d,1d) {
      private EscogedorColores<Integer> ec=new EscogedorColores<Integer>();
      public java.awt.Paint getSeriesPaint(int series) {
        return ec.next(series);
      };
    };
    rend.setBaseItemLabelsVisible(false);
    CategoryAxis ejePer=new CategoryAxis(ejeX);
    NumberAxis ejeVal=new NumberAxis(ejeY);
    if (porcentual) ejeVal.setNumberFormatOverride(df);
    JFreeChart res=new JFreeChart(new CategoryPlot(series,ejePer,ejeVal,rend));
    ejePer.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI*0.5));
    if (titulo!=null) res.setTitle(titulo);
    return new ChartPanel(res);
  }
  public static ChartPanel crearGraficaConteosPoblacion(String titulo, Variable[] diferenciados, Object[] args, boolean porcentual){
    return crearGraficaConteosPoblacion(titulo,diferenciados,args,porcentual,null); 
  }
  public static ChartPanel crearGraficaConteosPoblacion(String titulo, Variable[] diferenciados, Object[] args, boolean porcentual, FiltroDouble fd) {
    return crearGraficaConteosPoblacion(titulo, diferenciados, args, porcentual, false, 0, fd );
  }  
  @SuppressWarnings("serial")
  public static ChartPanel crearGraficaConteosPoblacionB(String titulo, Variable[] diferenciados, Object[] args, boolean porcentual, boolean periodos) {
    Map<byte[],? extends Object> datos=(Map<byte[],? extends Object>)(args[0]);
    Integer[] codigosIESDif=(Integer[])(args[1]);
    String[] codigosProgramasDif=(String[])(args[2]);
    XYPlot plot=new XYPlot();
    XYSeriesCollection series=new XYSeriesCollection();
    for (Map.Entry<byte[],? extends Object> e:datos.entrySet()) {
      byte[] llave=e.getKey();
      byte per = llave[0];
      byte[] tempo = new byte[llave.length-1];
      System.arraycopy(llave, 1, tempo, 0, tempo.length);
      llave = tempo;
      StringBuffer nombreSerie=new StringBuffer();
      for (int i=0,t=diferenciados.length; i<t; i++) {
        nombreSerie.append((i>0?",":"")+Variable.toString(diferenciados[i],llave[i],codigosIESDif,codigosProgramasDif));
      }
      XYSeries dataSeries=new XYSeries(nombreSerie.toString());
      int offsetX = per&0xFF;
      if (!porcentual) {
        if (e.getValue().getClass()==new int[0].getClass()) {
          int cont[]=(int[])(e.getValue());
          //TODO Revisar 19981 es constante
          //for (int i=0,t=cont.length; i<t; i++) if (cont[i]>0) dataSeries.add(i+1,cont[i]);
          for (int i=0,t=cont.length; i<t; i++) if (cont[i]>0) dataSeries.add(periodos?Constantes.anhoIni+(offsetX+i)/2d:i+1d,cont[i]);
        } else {
          double cont[]=(double[])(e.getValue());
          for (int i=0,t=cont.length; i<t; i++) if (cont[i]>0) dataSeries.add(periodos?Constantes.anhoIni+(offsetX+i)/2d:i+1d,cont[i]);
        }
      }
      else {
        double cont[]=(double[])(e.getValue());
        //TODO Revisar 19981 es constante
        //for (int i=0,t=cont.length; i<t; i++) if (cont[i]>1e-8) dataSeries.add(i+1,cont[i]);
        for (int i=0,t=cont.length; i<t; i++) if (cont[i]>1e-8) dataSeries.add(periodos?Constantes.anhoIni+(offsetX+i)/2d:i+1d,cont[i]);
      }
      series.addSeries(dataSeries);
    }
    plot.setDataset(series);
    plot.setRenderer(new DefaultXYItemRenderer() {
      private EscogedorColores<Integer> ec=new EscogedorColores<Integer>();
      public java.awt.Paint getSeriesPaint(int series) {
        return ec.next(series);
      };
    });
    NumberAxis valueAxis=new NumberAxis(porcentual?"% desertores":"Conteo de matriculados");
    //NumberAxis tiempoAxis=new NumberAxis(periodos?"Período":"#Período");
    NumberAxis tiempoAxis=new NumberAxis(periodos?"Período":"# de semestres cursados");
    if (periodos) {
      tiempoAxis.setLowerBound(Constantes.anhoIni-1);
      tiempoAxis.setUpperBound(Constantes.anhoFin);
      tiempoAxis.setAutoRangeIncludesZero(false);
      tiempoAxis.setAutoRange(true);
    }
      
    tiempoAxis.setStandardTickUnits(tick1);
    if (porcentual) valueAxis.setNumberFormatOverride(df);
    plot.setDomainAxis(tiempoAxis);
    plot.setRangeAxis(valueAxis);
    JFreeChart res=new JFreeChart(plot);
    res.setTitle(titulo);
    return new ChartPanel(res);
  }
  
  public static ChartPanel crearGraficaConteosPoblacion(String titulo, String ejex, String ejey, Variable[] diferenciados, Object[] args, boolean porcentual, boolean periodos, int offsetX){
    return crearGraficaConteosPoblacion(titulo,ejex,ejey,diferenciados,args,porcentual,periodos,offsetX,null);  
   }
  @SuppressWarnings("serial")
  public static ChartPanel crearGraficaConteosPoblacion(String titulo, Variable[] diferenciados, Object[] args, boolean porcentual, boolean periodos, int offsetX){
   return crearGraficaConteosPoblacion(titulo,diferenciados,args,porcentual,periodos,offsetX,null);  
  }
  /**
   * Versión anterior del comando con nombres por defecto en ejes
   */
  public static ChartPanel crearGraficaConteosPoblacion(String titulo, Variable[] diferenciados, Object[] args, boolean porcentual, boolean periodos, int offsetX,FiltroDouble fd) {
    return crearGraficaConteosPoblacion(titulo,periodos?"Período":"# de semestres cursados",porcentual?"% desertores":"Conteo de matriculados",diferenciados,args,porcentual,periodos,offsetX,fd); 
  }
  public static ChartPanel crearGraficaConteosPoblacion(String titulo, String ejex, String ejey, Variable[] diferenciados, Object[] args, boolean porcentual, boolean periodos, int offsetX,FiltroDouble fd) {
    Map<byte[],? extends Object> datos=(Map<byte[],? extends Object>)(args[0]);
    Integer[] codigosIESDif=(Integer[])(args[1]);
    String[] codigosProgramasDif=(String[])(args[2]);
    XYPlot plot=new XYPlot();
    XYSeriesCollection series=new XYSeriesCollection();
    for (Map.Entry<byte[],? extends Object> e:datos.entrySet()) {
      byte[] llave=e.getKey();
      StringBuffer nombreSerie=new StringBuffer();
      for (int i=0,t=diferenciados.length; i<t; i++) {
        nombreSerie.append((i>0?",":"")+Variable.toString(diferenciados[i],llave[i],codigosIESDif,codigosProgramasDif));
      }
      XYSeries dataSeries=new XYSeries(nombreSerie.toString());
      if (!porcentual) {
        int cont[]=(int[])(e.getValue());
        //TODO Revisar 19981 es constante
        //for (int i=0,t=cont.length; i<t; i++) if (cont[i]>0) dataSeries.add(i+1,cont[i]);
        for (int i=0,t=cont.length; i<t; i++) 
          if ((cont[i]>0)&&(fd==null || fd.acepta(periodos?Constantes.anhoIni+(offsetX+i)/2d:i+1d)))
            dataSeries.add(periodos?Constantes.anhoIni+(offsetX+i)/2d:i+1d,cont[i]);
      }
      else {
        double cont[]=(double[])(e.getValue());
        //TODO Revisar 19981 es constante
        //for (int i=0,t=cont.length; i<t; i++) if (cont[i]>1e-8) dataSeries.add(i+1,cont[i]);
        for (int i=0,t=cont.length; i<t; i++) 
          if ((cont[i]>1e-8)&&(fd==null || fd.acepta(periodos?Constantes.anhoIni+(offsetX+i)/2d:i+1d)))
            dataSeries.add(periodos?Constantes.anhoIni+(offsetX+i)/2d:i+1d,cont[i]);
      }
      series.addSeries(dataSeries);
    }
    plot.setDataset(series);
    plot.setRenderer(new DefaultXYItemRenderer() {
      private EscogedorColores<Integer> ec=new EscogedorColores<Integer>();
      public java.awt.Paint getSeriesPaint(int series) {
        return ec.next(series);
      };
    });
    NumberAxis valueAxis=new NumberAxis(ejey);
    //NumberAxis tiempoAxis=new NumberAxis(periodos?"Período":"#Período");
    NumberAxis tiempoAxis=new NumberAxis(ejex);
    if (periodos) {
      tiempoAxis.setLowerBound(Constantes.anhoIni-1);
      tiempoAxis.setUpperBound(Constantes.anhoFin);
      tiempoAxis.setAutoRangeIncludesZero(false);
      tiempoAxis.setAutoRange(true);
    }
      
    tiempoAxis.setStandardTickUnits(tick1);
    if (porcentual) valueAxis.setNumberFormatOverride(df);
    plot.setDomainAxis(tiempoAxis);
    plot.setRangeAxis(valueAxis);
    JFreeChart res=new JFreeChart(plot);
    res.setTitle(titulo);
    return new ChartPanel(res);
  }
  @SuppressWarnings("serial")
  public static ChartPanel crearGraficaConteosPoblacionSemestral(String titulo, Variable[] diferenciados, Object[] args, boolean porcentual){
    return crearGraficaConteosPoblacionSemestral(titulo, diferenciados, args, porcentual,null);
  } 
  //TODO Revisar si necesita filtro
  @SuppressWarnings("serial")
  public static ChartPanel crearGraficaConteosPoblacionSemestral(String titulo, Variable[] diferenciados, Object[] args, boolean porcentual,FiltroDouble fv) {
    Map<byte[],? extends Object> datos=(Map<byte[],? extends Object>)(args[0]);
    Integer[] codigosIESDif=(Integer[])(args[1]);
    String[] codigosProgramasDif=(String[])(args[2]);
    int limsSems[]=(int[])(args[3]),limInf=limsSems[0],limSup=limsSems[1];
    DefaultCategoryDataset series=new DefaultCategoryDataset();
    for (Map.Entry<byte[],? extends Object> e:datos.entrySet()) {
      byte[] llave=e.getKey();
      StringBuffer nombreSerie=new StringBuffer();
      for (int i=0,t=diferenciados.length; i<t; i++) {
        nombreSerie.append((i>0?",":"")+Variable.toString(diferenciados[i],llave[i],codigosIESDif,codigosProgramasDif));
      }
      String ns=nombreSerie.toString();
      Object cont=e.getValue();
      for (int j=limInf; j<=limSup; j++) {
        double val=porcentual?(((double[])cont)[j-limInf]):(((int[])cont)[j-limInf]);        
        series.addValue(val,ns,CajaDeHerramientas.codigoSemestreToString((byte)j));
      }
    }
    BarRenderer3D rend=new BarRenderer3D(1d,1d) {
      private EscogedorColores<Integer> ec=new EscogedorColores<Integer>();
      public java.awt.Paint getSeriesPaint(int series) {
        return ec.next(series);
      };
    };
    rend.setBaseItemLabelsVisible(false);
    CategoryAxis ejePer=new CategoryAxis("Período");
    NumberAxis ejeVal=new NumberAxis(porcentual?"Porcentaje de matriculados":"Número de matriculados");
    if (porcentual) ejeVal.setNumberFormatOverride(df);
    JFreeChart res=new JFreeChart(new CategoryPlot(series,ejePer,ejeVal,rend));
    ejePer.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI*0.5));
    res.setTitle("Matriculados");
    return new ChartPanel(res);
  }
  @SuppressWarnings("serial")
  public static ChartPanel crearGraficaSupervivenciaEstudiante(String titulo, SortedMap<String, SortedMap<Integer,Double>> pSeriesDatos) {
    XYPlot plot=new XYPlot();
    XYSeriesCollection series=new XYSeriesCollection();
    for (Map.Entry<String, SortedMap<Integer, Double>> serieDa:pSeriesDatos.entrySet()) {
      XYSeries dataSeries=new XYSeries(serieDa.getKey());
      for (Map.Entry<Integer, Double> dato:serieDa.getValue().entrySet()) dataSeries.add(dato.getKey(),dato.getValue());
      series.addSeries(dataSeries);
    }
    plot.setDataset(series);
    plot.setRenderer(new DefaultXYItemRenderer() {
      private EscogedorColores<Integer> ec=new EscogedorColores<Integer>();
      public java.awt.Paint getSeriesPaint(int series) {
        return ec.next(series);
      };
    });
    NumberAxis valueAxis=new NumberAxis("%Supervivencia");
    valueAxis.setRange(0,1);
    NumberAxis tiempoAxis=new NumberAxis("#Período");
    tiempoAxis.setStandardTickUnits(tick1);
    valueAxis.setNumberFormatOverride(df);
    plot.setDomainAxis(tiempoAxis);
    plot.setRangeAxis(valueAxis);
    JFreeChart res=new JFreeChart(plot);
    res.setTitle(titulo);
    return new ChartPanel(res);
  }
  @SuppressWarnings({"serial","unchecked"})
  public static ChartPanel crearGraficaNivelAprobacionDesertores(Object[] args, boolean porcentual) {
    int tam=(Integer)(args[1]);
    RangoByte<Double> rangoNA=(RangoByte<Double>)(Variable.NIVEL_APROBACION_PER.rango);
    Byte[] codsRango=rangoNA.getRango();
    DefaultCategoryDataset series=new DefaultCategoryDataset();
    for (int j=0; j<tam; j++) for (byte b:codsRango) if (b!=-1) {
      double val=porcentual?(((double[][])(args[0]))[b+1][j]):(((int[][])(args[0]))[b+1][j]);
      series.addValue(val,rangoNA.toString(b),""+(j+1));
    }
    StackedBarRenderer rend=new StackedBarRenderer(porcentual) {
      private EscogedorColores<Integer> ec=new EscogedorColores<Integer>();
      public java.awt.Paint getSeriesPaint(int series) {
        return ec.next(series);
      };
    }; 
    rend.setBaseItemLabelsVisible(false);
    CategoryAxis ejePer=new CategoryAxis("#Período");
    NumberAxis ejeVal=new NumberAxis((porcentual?"Porcentaje":"Número")+" de desertores");
    if (porcentual) ejeVal.setNumberFormatOverride(df);
    JFreeChart res=new JFreeChart(new CategoryPlot(series,ejePer,ejeVal,rend));
    res.setTitle("Desertores por nivel de aprobación");
    return new ChartPanel(res);
  }
  
  public static final FiltroDouble filtroVacio = new FiltroDouble(){

    public boolean acepta(double x) {
      return true;
    }
    
  };
}
