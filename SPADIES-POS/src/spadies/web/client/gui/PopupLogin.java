package spadies.web.client.gui;

import spadies.web.client.Usuario;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
/**
 * Basado en http://gwt.components.googlepages.com/tooltiplistener2
 */
public class PopupLogin extends PopupPanel {
  private static final String DEFAULT_TOOLTIP_STYLE = "TooltipPopup";
  private static final int DEFAULT_OFFSET_X = 10;
  private static final int DEFAULT_OFFSET_Y = 35;
  private TextBox fUser = new TextBox();
  private PasswordTextBox fPass = new PasswordTextBox();
  private Button btn = new Button("Validarse");
  private Label lblMsg = new Label();
  private Button btnc = new Button("X", new ClickHandler() {
    public void onClick(ClickEvent event) {
      PopupLogin.this.hide();
    }
  });
  private String styleName = "";
  private int offsetX;
  private int offsetY;
  private UserChangeHandler handler;
  public PopupLogin(Widget sender, int offsetX, int offsetY, 
      final String styleName, final UserChangeHandler handler) {
    super(false,true);
    this.handler = handler;
    btn.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        try {
          requestBuilderLogin.sendRequest(fUser.getText()+"&"+fPass.getText(), new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
              String responseText = response.getText();
              JSONValue jsonValue = JSONParser.parse(responseText);
              if (jsonValue.isObject()==null) {
                lblMsg.setText("Usuario o contraseña invalida.");
                fPass.setText("");
              } else {
                Usuario usr = Usuario.readUsuario(jsonValue);
                if (usr!=null) {
                  handler.onUserChange(usr);
                }
                //handler.setText(jsonValue.isObject().get("nombre").isString().stringValue());
              }
            }
            
            public void onError(Request request, Throwable exception) {
              lblMsg.setText("No fue posible contactar al servidor en este momento.  Intentelo de nuevo mas tarde.");
            }
          });
        } catch (RequestException e) {
          lblMsg.setText("Hubo un error en la respuesta del servidor.  Intentelo de nuevo mas tarde. ["+e.getMessage()+"]");
          e.printStackTrace();
        }
      }
    });
    VerticalPanel vp = new VerticalPanel();
    vp.add(new HTML("Usuario"));
    vp.add(fUser);
    vp.add(new HTML("Contraseña"));
    vp.add(fPass);
    HorizontalPanel hp = new HorizontalPanel();
    hp.add(btn);
    hp.add(btnc);
    vp.add(hp);
    vp.add(lblMsg);
    DecoratorPanel dp = new DecoratorPanel();
    dp.add(vp);
    super.add(dp);
    
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    int left = sender.getAbsoluteLeft() + this.offsetX;
    int top = sender.getAbsoluteTop() + this.offsetY;

    setPopupPosition(left, top);
    setStyleName(styleName);
  }
  private final RequestBuilder requestBuilderLogin = new RequestBuilder(
      RequestBuilder.POST, GWT.getHostPageBaseURL() + "spadies/usuario");
  public void show() {
    super.show();
    fUser.setFocus(true);
  }
}
