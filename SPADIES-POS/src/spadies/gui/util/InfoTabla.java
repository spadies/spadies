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
package spadies.gui.util;
/**
 * Contiene el contenido de las celdas de una tabla, para sus encabezados (de filas y de columnas) y su cuerpo.  
 */
public class InfoTabla {
  private final String valores[][],encabezadoFilas[][],encabezadoColumnas[];
  public InfoTabla(String[][] pValores, String[][] pEncabezadoFilas, String[] pEncabezadoColumnas) {
    valores=pValores;
    encabezadoFilas=pEncabezadoFilas;
    encabezadoColumnas=pEncabezadoColumnas;
  }
  public boolean esVacio() {
    return valores==null;
  }
  public int getNumEncabezadosFilas() {
    return (encabezadoFilas!=null)?encabezadoFilas.length:0;
  }
  public String getEncabezadoFila(int row, int col) {
    return (encabezadoFilas!=null)?toString(encabezadoFilas[col][row]):"";
  }
  public String getEncabezadoColumna(int col) {
    return (encabezadoColumnas!=null)?toString(encabezadoColumnas[col]):"";
  }
  public int getNumColumnas() {
    return (valores!=null && valores.length>0)?valores[0].length:1;
  }
  public int getNumFilas() {
    return (valores!=null)?valores.length:0;
  }
  public String getValor(int row, int col) {
    return (valores!=null)?toString(valores[row][col]):"";
  }
  public String toString(String s) {
    return (s==null)?"":s;
  }
  public String toJSON() {
    StringBuilder res = new StringBuilder();
    boolean p = true;
    res.append("[");
    int it = this.getNumFilas()+1,
      jt = this.getNumEncabezadosFilas()+this.getNumColumnas();
    for (int i=0;i<it;i++) {
      if (p) p = false;
      else res.append(",");
      res.append("[");
      boolean p2 = true;
      for (int j=0;j<jt;j++) {
        if (p2) p2 = false;
        else res.append(",");
        int row=i,col=j;
        String val = "";
        if (row==0 && col>=this.getNumEncabezadosFilas()) val = this.getEncabezadoColumna(col-this.getNumEncabezadosFilas());
        else if (col<this.getNumEncabezadosFilas()) val = this.getEncabezadoFila(row,col);
        else val = this.getValor(row-1,col-this.getNumEncabezadosFilas());
        res.append("\""+val+"\"");
      }
      res.append("]");
    }
    res.append("]");
    return res.toString();
  }
}