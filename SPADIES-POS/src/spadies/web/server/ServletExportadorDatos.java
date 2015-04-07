package spadies.web.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class ServletExportadorDatos extends ServletSPADIES {
  private static String rutaImagenes = null;
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    ServletContext context = getServletContext();
    context.getInitParameter("spadies.passwords");
    //TODO potencialmente se asigna la variable varias veces
    if (rutaImagenes==null) rutaImagenes = context.getInitParameter("spadies.web_img");
  }
  private static final Pattern ptr_idc = Pattern.compile("[a-z0-9]+");
  private static final Pattern ptrn_int = Pattern.compile("\\d+");
  private static final Pattern ptrn_per = Pattern.compile("\\d+,\\d{2}%");
  public void doAll(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String fmt = req.getParameter("fmt");
    String idc = req.getParameter("idc");
    String emsg = null;
    if (fmt==null || idc==null)
      emsg = "Parametros faltantes";
    else if (!fmt.equals("xls"))
      emsg = "Formato no permitido";
    else if (!ptr_idc.matcher(idc).matches())
      emsg = "Codigo de consulta invalido";
    File fData = new File(getServletContext().getRealPath("img/gen")+"/data"+idc+".bin"),
      fOut = new File(getServletContext().getRealPath("img/gen")+"/data"+idc+"."+fmt);
    if (emsg==null && !fData.exists())
      emsg = "La consulta no existe.";
    if (emsg==null) {
      //Usuario usr = AuxiliaresConsulta.getUsuarioSesion(req);
      try {
        transformadorConsulta2XLS(fData, fOut);
        res.sendRedirect("../img/gen/data"+idc+"."+fmt);
      } catch (Exception e) {
        fOut.delete();
        msgError(res, "Error generando el archivo");
      }
      return;
    } else msgError(res, emsg);
  }
  public static void transformadorConsulta2XLS(File fConsulta,File fResultado) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(fConsulta));
    StringBuilder sb = new StringBuilder();
    for (String s=br.readLine();s!=null;s=br.readLine())
      sb.append(s);
    br.close();
    //System.out.println(sb.toString());
    Object val = JSONValue.parse(sb.toString());
    Workbook wb = new HSSFWorkbook();
    CellStyle percentCellStyle = wb.createCellStyle();
    percentCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00%"));
    if (val instanceof JSONArray) {
      JSONArray jarr = (JSONArray)val;
      {
        for (int i = 0; i < jarr.size(); ++i) {
          JSONObject jsonObject = (JSONObject)jarr.get(i);
          String tipo = ((String)jsonObject.get("tipo"));
          if ("t".equals(tipo)) {
            Sheet sht = wb.createSheet((String)jsonObject.get("nombre"));
            JSONArray arr = (JSONArray) jsonObject.get("valores");
            for (int j = 0; j < arr.size(); ++j) {
              JSONArray arr2 = (JSONArray) arr.get(j);
              Row row = sht.createRow(j);
              for (int k = 0; k < arr2.size(); ++k) {
                Cell cell = row.createCell(k);
                String sval = (String)arr2.get(k);
                if (ptrn_int.matcher(sval).matches())
                  cell.setCellValue(Integer.parseInt(sval));
                else if (ptrn_per.matcher(sval).matches()) {
                  cell.setCellValue(Double.parseDouble(sval.replace(',', '.').replace("%", ""))/100d);
                  cell.setCellStyle(percentCellStyle);
                }
                else
                cell.setCellValue((String)arr2.get(k));
              }
            }
            /*if ("g".equals(tipo)) {
              widget = new HTML("<img src=\""+jsonObject.get("url").isString().stringValue()+"\" />");
            }*/
          }
        }
      }
    }
    FileOutputStream fileOut = new FileOutputStream(fResultado);
    wb.write(fileOut);
    fileOut.close();
  }
  public static void main(String [] args) throws IOException {
    transformadorConsulta2XLS(new File("war/img/gen/data4e0fa75ca92410f6c63973b2d40cdf1a.bin"),new File("war/img/gen/data4e0fa75ca92410f6c63973b2d40cdf1a.xls"));
  }
  private void msgError(HttpServletResponse res, String msg) throws UnsupportedEncodingException, IOException {
    res.setStatus(503);
    res.setCharacterEncoding("UTF-8");
    res.setContentType("text/javascript");
    res.addHeader("Cache-Control", "no-cache");
    res.addHeader("Pragma", "no-cache");
    res.getOutputStream().write(msg.getBytes("UTF-8"));
  }
}