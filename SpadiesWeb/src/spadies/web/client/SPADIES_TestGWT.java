package spadies.web.client;

import java.util.ArrayList;
import java.util.Set;

import spadies.web.client.gui.PanelResultados;
import spadies.web.client.gui.PopupLogin;
import spadies.web.client.gui.UserChangeHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SPADIES_TestGWT implements EntryPoint {
  private Usuario usr;
  private static final int heightCentral = 530 *8/10,
    heightCentralMenor = heightCentral-30,
    widthVar=270,
    widthRes=700,
    widthTotal = 1000;
  private final PanelResultados dtp = new PanelResultados(widthRes,heightCentral,widthRes,heightCentralMenor);
  private final DockPanel mainPanel = new DockPanel();
  private final RadioButton rbn = new RadioButton("modoConsulta","Nacional"),
    rbi = new RadioButton("modoConsulta","Institucion");
  //private PopupLogin popl;
  //private HTML lblUsuario = new HTML();
  //private Button btnLogin = new Button("Iniciar sesión");
  //private Button btnLogout = new Button("Cerrar sesión");
  private VerticalPanel pnlUsuario = new VerticalPanel();
  {
    //lblUsuario.setStyleName("pnlUsuario");
  }
  private HTML panelVacio = new HTML("<img src=\"img/guia.jpg\" />");
  private String nombreUsuario=null,tipo = null;
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
  private class JSONResponseTextHandler_Variables implements RequestCallback {
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

  private Tree varTree = new Tree();
  
  private ArrayList<CheckBoxVar> cbsDif = new ArrayList<CheckBoxVar>();
  private ArrayList<ArrayList<CheckBoxValVar>> cbsFil = new ArrayList<ArrayList<CheckBoxValVar>>();
  /**
   * Contiene los checkbox de los filtros que resan blanqueados al 'Limpiar todo'
   */
  private ArrayList<CheckBox> cbsFiltro = new ArrayList<CheckBox>();

  /*
   * RequestBuilder used to issue HTTP GET requests.
   */
  private final RequestBuilder requestBuilderVars = new RequestBuilder(
    RequestBuilder.GET, GWT.getHostPageBaseURL() + "spadies/variables");
  private final RequestBuilder requestBuilderVarsP = new RequestBuilder(
      RequestBuilder.POST, GWT.getHostPageBaseURL() + "spadies/variables");
  private final RequestBuilder requestBuilderConsulta = new RequestBuilder(
    RequestBuilder.POST, GWT.getHostPageBaseURL() + "spadies/consultas");
  private final RequestBuilder requestBuilderUsuario = new RequestBuilder(
    RequestBuilder.GET, GWT.getHostPageBaseURL() + "spadies/usuario");
  private final RequestBuilder requestBuilderLogout = new RequestBuilder(
      RequestBuilder.GET, GWT.getHostPageBaseURL() + "spadies/usuario?logout");

  /**
   * Entry point for this simple application. Currently, we build the
   * application's form and wait for events.
   */
  public void onModuleLoad() {
    initializeMainForm();
  }

  public static class TreeItemVariable extends TreeItem {
    public final String codint;
    public TreeItemVariable(Widget widget, String codint) {
      super(widget);
      this.codint = codint;
    }
  }
  /*
   * Add the object presented by the JSONValue as a children to the requested
   * TreeItem.
   */
  int numVar = 0;
  private TreeItem itemProgramas;
  private ClickHandler cambioIES = new ClickHandler() {
    public void onClick(ClickEvent event) {
      itemProgramas.setState(false);
      itemProgramas.removeItems();
      itemProgramas.addItem(SafeHtmlUtils.fromString(""));
      /*for (CheckBox cb:cbsProgramas)
        cb.setValue(true);*/
      cambio_ies = true;
    }
  };
  private ArrayList<CheckBoxValVar> cbsProgramas,cbsIES;
  private boolean cambio_ies = true;
  private void addVariable(TreeItem treeItem, JSONValue jsonValue) {
    JSONObject jsonObject;
    final int actNumVar = ++numVar;
    if ((jsonObject = jsonValue.isObject()) != null) {
      String pnom = jsonObject.get("nombre").isString().stringValue();
      String pcod = jsonObject.get("idvar").isString().stringValue();
      boolean es_ies = "CODIGO_IES".equals(pcod);
      boolean es_pro = "PROGRAMA_EST".equals(pcod);
      CheckBoxVar btn = new CheckBoxVar("",Integer.parseInt(jsonObject.get("nvar").isString().stringValue()),actNumVar-1);
      btn.setValue(false);
      cbsDif.add(btn);
      final ArrayList<CheckBoxValVar> cbsFilVar = new ArrayList<CheckBoxValVar>();
      cbsFil.add(cbsFilVar);
      HorizontalPanel hp = new HorizontalPanel();
      hp.add(btn);
      HTML txtNombre = new HTML(getChildText(pnom));
      txtNombre.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          boolean nval = !cbsFilVar.get(0).getValue();
          for (CheckBoxValVar cb: cbsFilVar) cb.setValue(nval);
        }
      });
      hp.add(txtNombre);
      TreeItem itVar = new TreeItemVariable(hp,pcod);
      treeItem.addItem(itVar);
      {
        JSONArray jsonVals = jsonObject.get("valores").isArray();
        int tamVal = jsonVals.size(); 
        for (int i=0;i<tamVal;i++) {
          JSONArray elemVal = jsonVals.get(i).isArray();
          String key = elemVal.get(0).isString().stringValue();
          String val = elemVal.get(1).isString().stringValue();
          CheckBoxValVar checkBox = new CheckBoxValVar(val.replaceAll("\\<.*?>",""),key);
          checkBox.setValue(true);
          if (!es_pro) cbsFiltro.add(checkBox);
          itVar.addItem(checkBox);
          cbsFilVar.add(checkBox);
          if (es_ies) checkBox.addClickHandler(cambioIES);
        }
      }
      if (es_pro) {
        itemProgramas = itVar;
        cbsProgramas = cbsFilVar;
      } else if (es_ies)
        cbsIES = cbsFilVar;
    }
  }
  
  private static class CheckBoxVar extends CheckBox {
    public final int ord, indice;
    public CheckBoxVar(String nombre,int ord, int indice) {
      super(nombre);
      this.ord = ord;
      this.indice = indice;
    }
  }
  private static class CheckBoxValVar extends CheckBox {
    public final String cod;
    public CheckBoxValVar(String nombre,String cod) {
      super(nombre);
      this.cod = cod;
    }
  }

  private void populateVariableTree(JSONValue jsonValue) {
    JSONObject jsonObject;
    if ((jsonObject = jsonValue.isObject()) != null) {
      for (String key:jsonObject.keySet()) {
        TreeItem treeItemCat = new TreeItem(SafeHtmlUtils.fromString(getChildText(key)));
        varTree.addItem(treeItemCat);
        populateCategoriaVariable(treeItemCat, jsonObject.get(key));
      }
    }
  }
  private void populateCategoriaVariable(TreeItem treeItem, JSONValue jsonValue) {
    JSONArray jsonArray;
    if ((jsonArray = jsonValue.isArray()) != null) {
      for (int i = 0; i < jsonArray.size(); ++i) {
        addVariable(treeItem, jsonArray.get(i));
      }
    }
  }

  private void displayError(String errorType, String errorMessage) {
    varTree.removeItems();
    varTree.setVisible(true);
    TreeItem treeItem = varTree.addItem(SafeHtmlUtils.fromString(errorType));
    treeItem.addItem(SafeHtmlUtils.fromString(errorMessage));
    treeItem.setStyleName("JSON-JSONResponseObject");
    treeItem.setState(true);
  }

  /*
   * Update the treeview of a JSON object.
   */
  
  private void displayJSONObject(JSONValue jsonValue) {
    varTree.removeItems();
    varTree.setVisible(true);
    varTree.addCloseHandler(new CloseHandler<TreeItem>() {
      public void onClose(CloseEvent<TreeItem> event) {
      }
    });
    varTree.addOpenHandler(new OpenHandler<TreeItem>() {
      public void onOpen(OpenEvent<TreeItem> event) {
        if (!cambio_ies) return;
        TreeItem target = event.getTarget();
        if (target instanceof TreeItemVariable)
          if ("PROGRAMA_EST".equals(((TreeItemVariable)target).codint)){
            try {
              String s ="";
              boolean p = true;
              for (CheckBoxValVar  cb: cbsIES)
                if (cb.getValue()) {
                  if (p) p = false;
                  else s+=",";
                  s+= cb.cod;
                }
              new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL() + "spadies/variables?programas&filies="+s).sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                  cbsProgramas.clear();
                  itemProgramas.removeItems();
                  JSONValue jsonValue = JSONParser.parse(response.getText());
                  JSONArray arr;
                  if ((arr = jsonValue.isArray())!=null) {
                    for (int i = 0, t=arr.size();i<t;i++) {
                      String str = arr.get(i).isString().stringValue();
                      CheckBoxValVar cb = new CheckBoxValVar(str, str);
                      cb.setValue(true);
                      cbsProgramas.add(cb);
                      itemProgramas.addItem(cb);
                    }
                  }
                }
                
                public void onError(Request request, Throwable exception) {
                  // TODO Auto-generated method stub
                }
              });
              cambio_ies = false;
            } catch (RequestException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
      }
    });
    //TreeItem treeItem = varTree.addItem("Variables");
    populateVariableTree(jsonValue);
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
      requestBuilderVars.sendRequest(null, new JSONResponseTextHandler_Variables());
    } catch (RequestException ex) {
      displaySendError(ex.toString());
    }
  }

  /*
   * Realizacion de la consulta
   */
  private void doConsulta(int consulta) {
    ArrayList<Integer> diferenciados = new ArrayList<Integer>();  
    for (CheckBoxVar cbv:cbsDif) {
      if (cbv.getValue()) diferenciados.add(cbv.ord);
    }
    String queryd = "c="+consulta; 
    queryd+= "&diferenciados=";
    for (Integer dif:diferenciados)
      queryd+=dif+",";
    queryd+= "&fil="+inferQueryFiltro();
    queryd+= "&modo="+(rbi.getValue()?"1":"0");
    try {
      requestBuilderConsulta.sendRequest(queryd, dtp);
    } catch (RequestException ex) {
      displaySendError(ex.toString());
    }
  }

  private String inferQueryFiltro() {
    String res = "";
    for (int i=0,it=cbsDif.size();i<it;i++) {
      int ord = cbsDif.get(i).ord;
      ArrayList<CheckBoxValVar> cbf = cbsFil.get(i);
      ArrayList<String> vals = new ArrayList<String>();
      boolean nofil = true;//Todos seleccionados
      boolean nosel = true;//Ninguno seleccionado
      for (CheckBoxValVar cb: cbf) {
        if (!cb.getValue()) nofil = false;
        if (cb.getValue()) nosel = false;
        if (cb.getValue()) vals.add(cb.cod);
      }
      if (nosel) //TODO abortar consulta o mejor arrojar excepcion para que alguno mas arribe aborte
        Window.alert("Sin seleccion en variable "+cbsDif.get(i).getText());
      else if (!nofil) {
        res+="!"+ord+"--";
        for (String val:vals) res+=","+val.replace("Ñ", "%D1");
      }
    }
    return res;
  }
  /*
   * Causes the text of child elements to wrap.
   */
  private String getChildText(String text) {
    return "<span style='white-space:normal'>" + text + "</span>";
  }

  /**
   * Initialize the main form's layout and content.
   */
  private void initializeMainForm() {
    /*final SimpleTextPopup pop = new SimpleTextPopup("Cargando....");
    pop.setSize("300px","300px");
    pop.setVisible(true);
    pop.setStyleName("TooltipPopup");
    pop.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
      public void setPosition(int offsetWidth, int offsetHeight) {
        int left = (Window.getClientWidth() - offsetWidth) / 3;
        int top = (Window.getClientHeight() - offsetHeight) / 3;
        pop.setPopupPosition(left, top);
      }
    });*/
    final Button btnEst = new Button("Estudiantes",new ClickHandler() {
      public void onClick(ClickEvent event) {
        Window.open(GWT.getHostPageBaseURL() + "spadiesd/lalumno?q="+inferQueryFiltro(), "_blank", "");
      }});
    varTree.setVisible(false);
    {//TODO Feo?
      try {
        requestBuilderUsuario.sendRequest("", new RequestCallback() {
          public void onResponseReceived(Request request, Response response) {
            String responseText = response.getText();
            try {
              JSONValue jsonValue = JSONParser.parse(responseText);
              usr = Usuario.readUsuario(jsonValue); 
              //lblUsuario.setHTML(usr.isPublic()?usr.nom:("Validado como: "+usr.nom));
              btnEst.setVisible(usr.isIES());
              //btnLogin.setVisible(usr.isPublic());
              //btnLogout.setVisible(!usr.isPublic());
            } catch (JSONException e) {
              displayParseError(responseText);
            }
          }
          public void onError(Request request, Throwable exception) {
            // TODO Popup con error lectura usuario
          }
        });
      } catch (RequestException e) {
      }
    }
    RootPanel mainPanelSlot = RootPanel.get("mainPanel");
    if (mainPanelSlot == null) {
      Window.alert("Please define a container element whose id is 'mainPanel'");
      return;
    }
    mainPanelSlot.add(mainPanel);
    mainPanel.setSize(widthTotal+"px", heightCentral+"px");
    mainPanel.add(dtp,DockPanel.CENTER);
    {
      VerticalPanel vp = new VerticalPanel();
      HorizontalPanel hp = new HorizontalPanel();      
      Button[] btns = new Button[]{
          new Button("Limpiar seleccion",new ClickHandler() {
            public void onClick(ClickEvent event) {
              limpiarSelecciones();
            }
          }),
          new Button("Deserción por cohorte",new ClickHandler() {
            public void onClick(ClickEvent event) {
              doConsulta(0);
            }
          }),
          new Button("Deserción por período",new ClickHandler() {
            public void onClick(ClickEvent event) {
              doConsulta(1);
            }
          }),
          new Button("Caracterización de los estudiantes",new ClickHandler() {
            public void onClick(ClickEvent event) {
              doConsulta(2);
            }
          })
      };
      //btns[0].addMouseOverHandler(new MouseOverTooltip("Genere gráficas sobre la deserción acumulada por cohorte,  cada punto sobre la línea indica el porcentaje de estudiantes que desertaron habiendo llegado hasta el semestre al cual hace referencia el punto.", 3000));
      //btns[1].addMouseOverHandler(new MouseOverTooltip("Genere gráficas sobre la deserción en cada semestre,  cada punto sobre la línea indica el porcentaje de estudiantes que desertaron en cada semestre.", 3000));
      //btns[2].addMouseOverHandler(new MouseOverTooltip("Revise la distribución de los estudiantes según sus características socioeconómicas y académicas", 3000));
      boolean p = true;
      int widthResB = (widthRes*1000)/(widthVar+widthRes);
      for (Button btn:btns) {
        btn.setSize(p?(widthVar*1000)/(widthVar+widthRes)+"px":((widthResB/3)+"px"), "50px");
        if (p) p = false;
        hp.add(btn);
      }
      hp.setSize(widthRes+"px", "");
      vp.add(hp);
      {
        HorizontalPanel hp2 = new HorizontalPanel();
        pnlUsuario.add(new HTML("<iframe src=\"img/gen/fecha.txt\" width=\"278px\" height=\"50px\" style=\"border:0px;\">"));
        /*pnlUsuario.add(lblUsuario);
        //if (usr==null || usr.isPublic())
        btnLogin.setWidth((widthVar*1000)/(widthVar+widthRes)+"px");
        btnLogout.setWidth((widthVar*1000)/(widthVar+widthRes)+"px");
        btnLogin.setStylePrimaryName("log-Button");
        btnLogout.setStylePrimaryName("log-Button");
        pnlUsuario.add(btnLogin);
        pnlUsuario.add(btnLogout);
        btnLogout.setVisible(false);
        btnLogin.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            popl.show();
          }
        });
        btnLogout.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            try {
              requestBuilderLogout.sendRequest("", new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                  Window.open(Window.Location.getHref(), "_self", "");
                }
                public void onError(Request request, Throwable exception) {
                  // TODO Popup con error lectura usuario
                }
              });
            } catch (RequestException e) {
              // TODO Auto-generated catch block
              //e.printStackTrace();
            }
          }
        });*/
        Widget[] btns2 = new Widget[]{
            pnlUsuario,
            new Button("Grado por cohorte",new ClickHandler() {
              public void onClick(ClickEvent event) {
                doConsulta(3);
              }
            }),
            new Button("Cruce de variables",new ClickHandler() {
              public void onClick(ClickEvent event) {
                doConsulta(4);
              }
            }),
            btnEst
        };
        boolean p2 = true;
        for (Widget btn:btns2) {
          btn.setSize(p2?(widthVar*1000)/(widthVar+widthRes)+"px":((widthResB/3)+"px"), "50px");
          if (p2) p2 = false;
          hp2.add(btn);
        }
        vp.add(hp2);
      }
      mainPanel.add(vp,DockPanel.SOUTH);
    }
    {
      ScrollPanel scroll = new ScrollPanel(varTree);
      scroll.ensureDebugId("cwTree-dynamicTree-Wrapper");
      scroll.setSize(widthVar+"px", heightCentral+"px");
      VerticalPanel vp = new VerticalPanel();
      DecoratorPanel dp = new DecoratorPanel();
      dp.setWidget(scroll);
      vp.add(dp);
      {
        rbn.setValue(true);
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(rbn);
        hp.add(rbi);
        vp.add(hp);
        hp.setVisible(false);
      }
      mainPanel.add(vp,DockPanel.WEST);
    }
    {
      initVariableTree();
      varTree.setVisible(false);
    }
    RootPanel.get("loading").setVisible(false);
    dtp.add(panelVacio,"Guía");
    dtp.selectTab(0);
    /*popl = new PopupLogin(mainPanel, widthTotal/2, heightCentral/2, "TooltipLogin",new UserChangeHandler() {
      public void onUserChange(Usuario usr) {
        //lblUsuario.setHTML("<span>"+usr.nom+"</span>");
        if (usr!=null && !usr.isPublic())
          Window.open(Window.Location.getHref(), "_self", "");
      }
    });*/
    //popl.show();
  }

  private void limpiarSelecciones() {
    for (CheckBox cb:cbsDif) cb.setValue(false);
    for (CheckBox cb:cbsFiltro) cb.setValue(true);
    for (CheckBox cb:cbsProgramas) cb.setValue(true);
  }
}