package spadies.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import spadies.io.MyDataInputStream;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.util.CajaDeHerramientas;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.Constantes.VersionDatos;

public class MedidorBackups {
  public static void main(String[]args) throws Exception {
    Constantes.cargarArchivoFiltroIES();
    //KernelSPADIES kernel = KernelSPADIES.getInstance();
    File backupFolder = new File(args[0]);
    for (File fBackup:backupFolder.listFiles()) if (fBackup.getName().endsWith(".zip")) {
      procesarArchivo(fBackup);
    }
    //IES.cargar(is, cargarDatosPersonales)
  }
  public static void procesarArchivo(File f) throws Exception {
    FileInputStream fis = new  FileInputStream(f);
    ZipInputStream zis = new  ZipInputStream(fis);
    int c = 0;long tam=0,tami=0;
    ZipEntry ze = null;
    while ((ze=zis.getNextEntry())!=null) if (ze.getName().endsWith(".spa")) {
      c++;tam+=ze.getSize();
      MyDataInputStream is = new MyDataInputStream(CajaDeHerramientas.getZipInputStream(CajaDeHerramientas.getCipherInputStream(zis,"SPADIES")));
      if (is.readLong()!=1834809409255049615L) throw new MyException("El archivo \""+ze.getName()+"\" no es un archivo válido para la aplicación "+Constantes.nombreAplicacionLargo);
      String ver=new String(is.readByteArray(true,-1));
      VersionDatos vers = VersionDatos.getVersion(ver);
      if (vers==null) throw new MyException("La versión del archivo \""+ze.getName()+"\" ("+ver+") no corresponde con la versión de la aplicación ("+Constantes.versionDatos+").");
      //TODO cuadrar permisos de manera sensata para las siguientes dos lineas
      //IES ies = IES.cargar(is,true,vers);
      //tami+=ies.getTamanhoEnBytes(true, true);
      zis.closeEntry();
    }
    System.out.println(f.getName()+"\t"+c+"\t"+tam+"\t"+tami);
  }
}
