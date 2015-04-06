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
import java.text.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import spadies.gui.frames.*;
import spadies.gui.util.*;
import spadies.util.*;
import spadies.util.variables.*;

@SuppressWarnings("serial")
public class Sincronizador extends MyDialog implements Observer {
  private static final DecimalFormat df=new DecimalFormat("0.00");
  public boolean puedeCerrar=true;
  private long tiempoInicial=0;
  private int numIESHechas=0,numIES=0;
  private JProgressBar barraProgreso1=new JProgressBar(JProgressBar.HORIZONTAL,0,1000);
  private JProgressBar barraProgreso2=new JProgressBar(JProgressBar.HORIZONTAL,0,1000);
  private MyLabel labelEstado1=new MyLabel("<html><b>-</b></html>");
  private MyLabel labelEstado2=new MyLabel("<html><b>-</b></html>");
  private MyButton botonCerrarAplicacion=new MyButton("Abortar sincronización (cierra la aplicación)",null,0);
  public Sincronizador() {
    super(VentanaPrincipal.getInstance(),"Progreso",MyDialog.TipoBloqueo.TB_PADRE);
    MyLabel labelDescripcion=new MyLabel("<html>Sincronizando el sistema con los datos en la carpeta "+CajaDeHerramientas.stringToHTML(Constantes.carpetaCSV.getPath())+" y con el Ministerio de Educación Nacional.</html>");
    for (JProgressBar pb:new JProgressBar[]{barraProgreso1,barraProgreso2}) pb.setStringPainted(true);
    for (MyLabel ml:new MyLabel[]{labelEstado1,labelEstado2}) {
      Dimension d=new Dimension(750,ml.getPreferredSize().height);
      ml.setMinimumSize(d);
      ml.setMaximumSize(d);
      ml.setPreferredSize(d);
      ml.setText("");
    }
    botonCerrarAplicacion.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        while (!puedeCerrar) {
          try {Thread.sleep(200);} catch (Throwable th) {}
        }
        System.exit(0);
      }
    });
    MyBorderPane panelPrincipal=new MyBorderPane(true,5,5,5,5,new MyFlowPane(0,0,labelDescripcion),null,new MyBoxPane(BoxLayout.Y_AXIS,Box.createVerticalStrut(8),labelEstado1,barraProgreso1,Box.createVerticalStrut(8),labelEstado2,barraProgreso2,Box.createVerticalStrut(8)),null,new MyFlowPane(FlowLayout.RIGHT,5,0,botonCerrarAplicacion));
    {
      setContentPane(panelPrincipal);
      pack();
      setResizable(false);
      Rectangle r=getParent().getBounds();
      Dimension d=getSize();
      setLocation((int)(r.x+((r.width-d.width)/2)),(int)(r.y+((r.height-d.height)/2)));
      setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }
  }
  private void setEstado(String estado, long hecho, long total, int paso) {
    labelEstado1.setText("<html><b>"+CajaDeHerramientas.stringToHTML(estado)+"</b></html>");
    long t=(System.currentTimeMillis()-tiempoInicial)/1000;
    int mins=(int)(t/60),secs=(int)(t%60);
    setTitle("Progreso - Tiempo transcurrido: "+mins+"m "+CajaDeHerramientas.intToString(secs,2)+"s");
    double porc=(hecho<=0 || total<=0)?0.0:(1.0*hecho*100/total);
    barraProgreso1.setValue((int)(porc*10));
    barraProgreso1.setString(df.format(porc)+"%");
    barraProgreso1.repaint();
    if (numIES==0 || hecho<0 || total<=0 || paso<=0) return;
    double porcT=1.0*numIESHechas,pesos[]={0.3,0.15,0.38,0.15,0.02};
    for (int p=1; p<paso; p++) porcT+=pesos[p-1];
    porcT+=pesos[paso-1]*hecho/total;
    actualizarLabelProgresoTotal(100.0*porcT/numIES);
  }
  private void actualizarLabelProgresoTotal(double porcTotal) {
    labelEstado2.setText("<html><b>PROGRESO TOTAL:</b> &nbsp;(<i>se han procesado "+numIESHechas+" de "+numIES+" IES</i>)</html>");
    barraProgreso2.setValue((int)(porcTotal*10));
    barraProgreso2.setString(df.format(porcTotal)+"%");
    barraProgreso2.repaint();
  }
  public void update(Observable obs, Object args) {
    Object[] val=(Object[])args;
    setEstado((String)(val[0]),(Long)(val[1]),(Long)(val[2]),(Integer)(val[3]));
  }
  public void realizarSincronizacion() {
    final Set<String> ies=new TreeSet<String>();
    final File[] arr=Constantes.carpetaCSV.listFiles();
    if (arr!=null) for (File f:arr) if (CajaDeHerramientas.esCarpetaIES(f)) ies.add(f.getName());
    if (ies.isEmpty()) {RutinasGUI.desplegarError(VentanaPrincipal.getInstance(),"No se encontraron datos de IES en la carpeta "+Constantes.carpetaCSV.getPath()); return;}
    Item[] items=new Item[ies.size()];
    {
      int ind=0;
      for (String s:ies) items[ind++]=new Item(s,s,CajaDeHerramientas.stringToHTML(s));
    }
    MyDialogSeleccionarIES mds=new MyDialogSeleccionarIES(VentanaPrincipal.getInstance(),"<html>Indique cuáles IES desea sincronizar.</html>","IES a sincronizar:",items);
    mds.setVisible(true);
    Item[] iesSeleccionadas=mds.getIesSeleccionadas();
    if (iesSeleccionadas.length==0) return;
    numIES=iesSeleccionadas.length;
    setTitle("Progreso");
    tiempoInicial=System.currentTimeMillis();
    setEstado("",0,1000,0);
    setVisible(true);
    final Set<String> iesAProcesar=new TreeSet<String>();
    for (Item it:iesSeleccionadas) iesAProcesar.add((String)(it.key));
    new Thread() {
      public void run() {
        try {Thread.sleep(100);} catch (Throwable th) {}
        sincronizar(iesAProcesar,arr);
      }
    }.start();
  }
  private void sincronizar(Set<String> iesAProcesar, File[] arr) {
    File fP=Constantes.carpetaDatos;
    Set<String> iesHechas=new TreeSet<String>();
    try {
      if (fP.exists() && fP.isFile()) throw new MyException("Borre el archivo \""+fP.getPath()+"\" antes de continuar con la sincronización.");
      fP.mkdirs();
      if (!fP.exists() || !fP.isDirectory()) throw new MyException("No se pudo crear la carpeta \""+fP.getPath()+"\".");
      if (arr!=null) for (File f:arr) if (CajaDeHerramientas.esCarpetaIES(f) && iesAProcesar.contains(f.getName())) {
        /*Clave*/
        MyDialogPassword mpd=new MyDialogPassword(this,f.getName());
        mpd.setVisible(true);
        if (mpd.getPassword()==null) {
          numIESHechas++;
          continue;
        }
        SincronizadorIES sc=new SincronizadorIES(this,f,mpd.getPassword());
        //SincronizadorIES sc=new SincronizadorIES(this,f,new char[0]);
        sc.addObserver(this);
        sc.sincronizar();
        sc.deleteObserver(this);
        sc=null; System.gc();
        numIESHechas++;
        iesHechas.add(f.getName());
      }
      actualizarLabelProgresoTotal(100.0);
      String sih="";
      for (String s:iesHechas) sih+=(sih.length()>0?", ":"")+s;
      if (sih.length()>0) RutinasGUI.desplegarInformacion(this,"La sincronización de las IES "+sih+" fue llevada a cabo exitosamente.");
      dispose();
    }
    catch (MyException ex) {
      Constantes.logSPADIES.log("Error sincronizando CSVs",ex);
      RutinasGUI.desplegarError(this,ex.getMessage());
      dispose();
    }
    finally {
      if (iesHechas.size()>0) {
        new VentanaCarga(VentanaPrincipal.getInstance()).ejecutar();
      }
    }
  }
}
