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

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import spadies.io.*;
import spadies.util.*;

@SuppressWarnings("serial")
public class MyDialogPassword extends MyDialog {
  private final JLabel labelPassword=new JLabel("Contraseña:");
  private final JPasswordField campoPassword=new JPasswordField(12);
  private final MyButton botonAceptar=new MyButton("Aceptar",null,KeyEvent.VK_A);
  private final MyButton botonCancelar=new MyButton("Cancelar",null,KeyEvent.VK_C);
  private char[] password=null;
  public MyDialogPassword(JDialog padre, final String ies) {
    super(padre,"Autenticación sincronización",TipoBloqueo.TB_MODAL);
    MyLabel labelTitulo=new MyLabel("Digite la contraseña de la institución "+ies);
    botonAceptar.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        password=campoPassword.getPassword();
        try {
          Socket socket=null;
          MyDataOutputStream out=null;
          MyDataInputStream in=null;
          try {
            socket=new Socket(Constantes.ipServidorSPADIES,Constantes.puertoServidorMatch);
            out=new MyDataOutputStream(socket.getOutputStream());
            in=new MyDataInputStream(socket.getInputStream());
            socket.setSoTimeout(1000*17);
          }
          catch (Throwable th) {
            throw new MyException("No se pudo establecer comunicación con el servidor \""+Constantes.ipServidorSPADIES+"\" por el puerto "+Constantes.puertoServidorMatch+". Revise la configuración de su firewall para permitir a la aplicación comunicarse con el servidor del Ministerio de Educación Nacional o descargue la nueva versión de la aplicación SPADIES.",th);
          }
          try {
            out.writeLong(1654949798791L);
            out.writeInt(Integer.parseInt(ies));
            out.flush();
            if (in.readLong()!=3571984365291298800L) throw new MyException("Actualice la aplicación SPADIES para poder sincronizar con el servidor del Ministerio de Educación Nacional.");
            long tiempo=in.readLong();
            try {
              if (in.readLong()!=4897197444879L) throw new Exception("");
            }
            catch (Throwable th) {
              throw new MyException("El código de institución "+ies+" es inválido. Revise con el Ministerio de Educación Nacional el código asignado a su institución.");
            }
            out.writeByteArray(true,CajaDeHerramientas.getCipherEncrypt(new String(password)+";"+tiempo).doFinal("m. ! < sS@#tYm !ñQ/X".getBytes())); out.flush();
            try {
              String msg=new String(CajaDeHerramientas.getCipherDecrypt(new String(password)+";"+tiempo).doFinal(in.readByteArray(true,-1)));
              if (!msg.equals("1W bT,^-Mn|5Q rP{a]!")) throw new Exception("");
            }
            catch (Throwable th) {
              throw new MyException("Contraseña inválida.",th);
            }
            out.writeLong(0L); // Terminar la comunicación
            out.flush();
          }
          catch (MyException ex) {
            throw ex;
          }
          catch (Throwable th) {
            throw new MyException("Hubo un error en la comunicación con el servidor \""+Constantes.ipServidorSPADIES+"\".");
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
        catch (MyException ex) {
          Arrays.fill(password,(char)0);
          password=null;
          Constantes.logSPADIES.log("Error sincronizando",ex);
          RutinasGUI.desplegarError(MyDialogPassword.this,"<html>"+CajaDeHerramientas.stringToHTML(ex.getMessage())+"</html>");
          return;
        }
        MyDialogPassword.this.dispose();
      }
    });
    botonCancelar.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        password=null;
        MyDialogPassword.this.dispose();
      }
    });
    JPanel panelBotones=new MyFlowPane(FlowLayout.RIGHT,5,0,botonAceptar,botonCancelar);
    setContentPane(new MyBorderPane(false,8,8,8,8,labelTitulo,null,new MyBoxPane(BoxLayout.Y_AXIS,Box.createVerticalStrut(8),new MyFlowPane(0,0,labelPassword),campoPassword,Box.createVerticalStrut(8)),null,panelBotones));
    {
      pack();
      setResizable(false);
      Rectangle r=getParent().getBounds();
      Dimension d=getSize();
      setLocation((int)(r.x+((r.width-d.width)/2)),(int)(r.y+((r.height-d.height)/2)));
      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
  }
  public char[] getPassword() {
    return password;
  }
}
