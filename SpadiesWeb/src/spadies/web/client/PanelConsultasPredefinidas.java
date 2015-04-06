package spadies.web.client;

import java.util.ArrayList;

import spadies.web.client.gui.PanelResultados;
import spadies.web.client.gui.PopupLogin;
import spadies.web.client.gui.UserChangeHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class PanelConsultasPredefinidas implements EntryPoint {
  private Usuario usr;
  private static final int heightCentral = 530*8/10,
    heightCentralMenor = heightCentral-30,
    widthVar=270,
    widthRes=700,
    width = widthVar + widthRes,
    widthTotal = 1000;
  private final PanelResultados dtp = new PanelResultados(widthRes,heightCentral,widthRes,heightCentralMenor);
  private final DockPanel mainPanel = new DockPanel();
  /*private final RadioButton rbn = new RadioButton("modoConsulta","Nacional"),
    rbi = new RadioButton("modoConsulta","Institucion");*/
/*  private HTML lblUsuario = new HTML();
  private Button btnLogin = new Button("Logearse");
  {
    lblUsuario.setStyleName("pnlUsuario");
  }
  private PopupLogin popl;*/
  private final HorizontalPanel upperPanel = new HorizontalPanel();
  private HTML [] panelesGuia = new HTML[]{
      new HTML("<img src=\"img/guia_predef1.jpg\" />"),
      new HTML("<img src=\"img/guia_predef2.jpg\" />"),
      new HTML("<img src=\"img/guia_predef3.jpg\" />")
  };
  private static class SimpleTextPopup extends PopupPanel {
    public SimpleTextPopup(String txt) {
      super(false);
      setWidget(new Label(txt));
    }
  }
  /**
   * The message displayed to the user when the server cannot be reached or
   * returns an error.
   */
  private static final String SERVER_ERROR = "An error occurred while "
      + "attempting to contact the server. Please check your network "
      + "connection and try again.";

  /**
   * Class for handling the response text associated with a request for a JSON
   * object.
   */
  private class JSONResponseTextHandler_ConsList implements RequestCallback {
    public void onError(Request request, Throwable exception) {
      displayRequestError(exception.toString());
      //resetSearchButtonCaption();
    }

    public void onResponseReceived(Request request, Response response) {
      String responseText = response.getText();
      try {
        JSONValue jsonValue = JSONParser.parse(responseText);
        displayJSONObject(jsonValue);
      } catch (JSONException e) {
        displayParseError(responseText);
      }
    }
  }

  private Tree conTree = new Tree();
  
  /*
   * RequestBuilder used to issue HTTP GET requests.
   */
  private final RequestBuilder requestBuilderConsList = new RequestBuilder(
    RequestBuilder.GET, GWT.getHostPageBaseURL() + "spadies/consultas_predefinidas");
    //RequestBuilder.GET, GWT.getHostPageBaseURL() + "Variables.compleja.js");
  private final RequestBuilder requestBuilderConsulta = new RequestBuilder(
    RequestBuilder.POST, GWT.getHostPageBaseURL() + "spadies/consultas");
    //RequestBuilder.POST, GWT.getHostPageBaseURL() + "Consulta.simple.js");
  private final RequestBuilder requestBuilderUsuario = new RequestBuilder(
      RequestBuilder.GET, GWT.getHostPageBaseURL() + "spadies/usuario");

  /**
   * Entry point for this simple application. Currently, we build the
   * application's form and wait for events.
   */
  public void onModuleLoad() {
    initializeMainForm();
  }

  private ArrayList<ArrayList<Consulta>> consultas = new ArrayList<ArrayList<Consulta>> ();
  private class Consulta {
    public String nombre;
    public int pest;
    public String query;
  }
  final ArrayList<Button> btnsCategorias = new ArrayList<Button>();
  private void populateConTree(JSONValue jsonValue) {
    JSONArray jsonArray;
    if ((jsonArray = jsonValue.isArray()) != null) {
      for (int i = 0; i < jsonArray.size(); ++i) {
        final int ii = i;
        JSONArray jsonArray2;
        JSONObject cat = jsonArray.get(i).isObject();
        /*Button pb = new Button(cat.get("nombre").isString().stringValue(),new ClickHandler() {
          public void onClick(ClickEvent event) {
            displayConsultas(ii+1);
          }
        });
        pb.setSize(width/3+"px", "70px");
        upperPanel.add(pb);
        btnsCategorias.add(pb);*/
        //TreeItem treeItem = new TreeItem(cat.get("nombre").isString().stringValue());
        ArrayList<Consulta> consultasg = new ArrayList<Consulta>();
        if ((jsonArray2=cat.get("consultas").isArray())!=null)
          for (int j = 0; j < jsonArray2.size(); ++j)
            consultasg.add(addConsulta(jsonArray2.get(j)));
        consultas.add(consultasg);
      }
    }
    DeferredCommand.addCommand(new Command() {
      public void execute() {
        if (getParamString().equals("?3")) {
          displayConsultas(3);
        } else if (getParamString().equals("?2")) {
          displayConsultas(2);
        } else {
          displayConsultas(1);
        }        
      }
    });
  }
  private String[] htmlTab = new String[]{
    "<a href=\"consultas_predefinidas.html?1\"><img src=\"imagenes/caracterizacion$.png\" alt=\"Caracterización Estudiantil\" width=\"140\" height=\"33\" border=\"0\"/></a>",
    "<a href=\"consultas_predefinidas.html?2\"><img src=\"imagenes/desercion$.png\"  alt=\"Deserción Estudiantil\" width=\"140\" height=\"33\" border=\"0\"/></a>",
    "<a href=\"consultas_predefinidas.html?3\"><img src=\"imagenes/apoyo$.png\" alt=\"Apoyo a la permanencia\" width=\"140\" height=\"33\" border=\"0\"/></a>",
  };
  public void displayConsultas(int modo) {
    dtp.clear();
    dtp.add(panelesGuia[modo-1],"Guía");
    dtp.selectTab(0);
    conTree.clear();
    for (Consulta c:consultas.get(modo-1))
      conTree.addItem(new TreeItemConsulta(c));
    for (int i = 0; i < htmlTab.length; i++) {
      String nv = i==modo-1?"_":"";
      pnlsLink[i].clear();
      pnlsLink[i].add(new HTML(htmlTab[i].replace("$", nv)));
    }
    //pnlsLink.
    //for (Button btn:btnsCategorias) btn.setStylePrimaryName("gwt-Button");
    //btnsCategorias.get(modo-1).setStyleName("gwt-Button_sel");
  }
  public static class TreeItemConsulta extends TreeItem {
    public final String queryd;
    public final int pest;
    public TreeItemConsulta(Consulta c) {
      this(c.nombre,c.query,c.pest);
    }
    public TreeItemConsulta(String nombre, String queryd, int pest) {
      super(new HTML(getChildText(nombre)));
      this.queryd = queryd;
      this.pest = pest;
    }
  }
  private Consulta addConsulta(JSONValue jsonValue) {
    JSONObject jsonObject;
    if ((jsonObject = jsonValue.isObject()) != null) {
      Consulta c = new Consulta();
      c.nombre = jsonObject.get("nombre").isString().stringValue();
      c.query = jsonObject.get("query").isString().stringValue();
      c.pest = Integer.parseInt(jsonObject.get("posp").isString().stringValue());
      return c;
    }
    return null;
  }

  private void displayError(String errorType, String errorMessage) {
    conTree.removeItems();
    conTree.setVisible(true);
    TreeItem treeItem = conTree.addItem(SafeHtmlUtils.fromString(errorType));
    treeItem.addItem(SafeHtmlUtils.fromString(errorMessage));
    treeItem.setStyleName("JSON-JSONResponseObject");
    treeItem.setState(true);
  }

  /*
   * Update the treeview of a JSON object.
   */
  private void displayJSONObject(JSONValue jsonValue) {
    conTree.removeItems();
    conTree.setVisible(true);
    conTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
      public void onSelection(SelectionEvent<TreeItem> event) {
        dtp.clear();
        final TreeItemConsulta tic = (TreeItemConsulta) event.getSelectedItem();
        dtp.poss = tic.pest;
        doConsulta(tic.queryd);
      }
    });
    //TreeItem treeItem = varTree.addItem("Variables");
    populateConTree(jsonValue);
    //treeItem.setStyleName("JSON-JSONResponseObject");
    //treeItem.setState(true);
  }

  private void displayParseError(String responseText) {
    displayError("Failed to parse JSON response", responseText);
  }

  private void displayRequestError(String message) {
    displayError("Request failed.", message);
  }

  private void displaySendError(String message) {
    displayError("Failed to send the request.", message);
  }

  private void initVariableTree() {
    try {
      requestBuilderConsList.sendRequest(null, new JSONResponseTextHandler_ConsList());
    } catch (RequestException ex) {
      displaySendError(ex.toString());
    }
  }

  /*
   * Realizacion de la consulta
   */
  private void doConsulta(String txtq) {
    final SimpleTextPopup pop = new SimpleTextPopup("Realizando consulta.");
    pop.setVisible(true);
    pop.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
      public void setPosition(int offsetWidth, int offsetHeight) {
        int left = (Window.getClientWidth() - offsetWidth) / 3;
        int top = (Window.getClientHeight() - offsetHeight) / 3;
        pop.setPopupPosition(left, top);
      }
    });
    try {
      requestBuilderConsulta.sendRequest(txtq+"&modo="+0/*(rbi.getValue()?"1":"0")*/, dtp);
    } catch (RequestException ex) {
      displaySendError(ex.toString());
    }
    pop.setVisible(false);
  }
  /*
   * Causes the text of child elements to wrap.
   */
  private static String getChildText(String text) {
    return "<span style='white-space:normal'>" + text + "</span>";
  }
  public static native String getParamString() /*-{
    return $wnd.location.search;
  }-*/;
  private Panel [] pnlsLink = null;  
  /**
   * Initialize the main form's layout and content.
   */
  private void initializeMainForm() {
    // Avoids showing an "empty" cell
    conTree.setVisible(false);

    RootPanel mainPanelSlot = RootPanel.get("mainPanel");
    if (mainPanelSlot == null) {
      Window.alert("Please define a container element whose id is 'mainPanel'");
      return;
    }
    pnlsLink = new Panel[]{RootPanel.get("enl0"),RootPanel.get("enl1"),RootPanel.get("enl2")};
    mainPanelSlot.add(mainPanel);
    mainPanel.setSize(widthTotal+"px", heightCentral+"px");
    mainPanel.add(upperPanel,DockPanel.NORTH);
    {
      VerticalPanel vp = new VerticalPanel();
      ScrollPanel scroll = new ScrollPanel(conTree);
      scroll.ensureDebugId("cwTree-dynamicTree-Wrapper");
      scroll.setSize(widthVar+"px", (heightCentral-50)+"px");
      DecoratorPanel dp = new DecoratorPanel();
      dp.setWidget(scroll);
      vp.add(dp);
      /*{
        rbn.setValue(true);
        vp.add(rbn);
        vp.add(rbi);
      }*/
      mainPanel.add(vp,DockPanel.WEST);
    }
    {
      mainPanel.add(dtp,DockPanel.CENTER);
    }
    {
      initVariableTree();
      conTree.setVisible(false);
    }
    RootPanel.get("enl").add(new HTML("<a href=\"JSON.html\"><img src=\"imagenes/consulta_personalizada.png\"  alt=\"Consulta Personalizada\" width=\"140\" height=\"33\" border=\"0\"/></a>"));
    /*popl = new PopupLogin(mainPanel, widthTotal/2, heightCentral/2, "TooltipLogin",new UserChangeHandler() {
      public void onUserChange(Usuario usr) {
        PanelConsultasPredefinidas.this.usr = usr; 
        lblUsuario.setHTML("<span>"+usr.nom+"</span>");
        btnLogin.setVisible(usr.isPublic());
      }
    });*/
    final RootPanel pnlUsuario = RootPanel.get("pnlUsuario");
    if (pnlUsuario == null) {
      Window.alert("Please define a container element whose id is 'pnlUsuario'");
      return;
    }
    /*pnlUsuario.add(lblUsuario);
    pnlUsuario.add(btnLogin);
    btnLogin.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        popl.show();
      }
    });*/
    /*{//TODO Feo
      try {
        requestBuilderUsuario.sendRequest("", new RequestCallback() {
          public void onResponseReceived(Request request, Response response) {
            String responseText = response.getText();
            try {
              JSONValue jsonValue = JSONParser.parse(responseText);
              usr = Usuario.readUsuario(jsonValue);
              lblUsuario.setText(usr.nom);
              btnLogin.setVisible(usr.isPublic());
            } catch (JSONException e) {
              displayParseError(responseText);
            }
          }
          
          public void onError(Request request, Throwable exception) {
            // TODO Auto-generated method stub
          }
        });
      } catch (RequestException e) {
      }
    }*/
    /*DeferredCommand.addCommand(new Command() {
      public void execute() {
        String initToken = History.getToken();
        if (initToken.length() == 0) {
          History.newItem("");
        }
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
          public void onValueChange(ValueChangeEvent<String> event) {
            Window.alert(event.getValue());
          }
        });
        History.fireCurrentHistoryState();      }
    });*/
  }
}