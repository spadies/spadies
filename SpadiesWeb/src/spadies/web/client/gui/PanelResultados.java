package spadies.web.client.gui;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class PanelResultados extends DecoratedTabPanel implements RequestCallback {
  public int poss = -1;
  public PanelResultados(int width, int height,int deckWidth, int deckHeight) {
    super();
    this.setSize(width+"px", height+"px");
    this.getDeckPanel().setSize(deckWidth+"px", deckHeight+"px");
  }

  public void onError(Request request, Throwable exception) {
    // TODO Auto-generated method stub
  }

  public void onResponseReceived(Request request, Response response) {
    String responseText = response.getText();
    try {
      JSONValue jsonValue = JSONParser.parse(responseText);
      JSONString str = jsonValue.isString();
      if (str==null)
        displayJSONObject_Consulta(jsonValue);
      else {
        //System.out.println("Se presento un error realizando la consulta:" + str);
        PopupPanel pop = new PopupPanel(true,true);
        pop.add(new Label("Se presento un error realizando la consulta:" + str.stringValue()));
        pop.setPopupPosition(PanelResultados.this.getAbsoluteLeft()+(PanelResultados.this.getOffsetWidth()-pop.getOffsetWidth())/2, PanelResultados.this.getAbsoluteTop()+(PanelResultados.this.getOffsetHeight()-pop.getOffsetHeight())/2);
        pop.show();
      }
    } catch (JSONException e) {
      //TODO
      //displayParseError(responseText);
    }
  }
  private void displayJSONObject_Consulta(JSONValue jsonValue) {
    this.clear();
    this.setVisible(true);
    populatePanelResultado(jsonValue);
  }
  private void populatePanelResultado(JSONValue jsonValue) {
    JSONArray jsonArray;
    JSONObject jsonObject;
    if ((jsonArray = jsonValue.isArray()) != null) {
      for (int i = 0; i < jsonArray.size(); ++i) {
        jsonObject = jsonArray.get(i).isObject();
        String tipo = jsonObject.get("tipo").isString().stringValue();
        Widget widget = null;
        if ("i".equals(tipo)) {
          String idc = jsonObject.get("id").isString().stringValue();
          widget = new Anchor("Exportar a Excel","spadiesd/exporta?fmt=xls&idc="+idc,"_blank");
        } else if ("t".equals(tipo)) {
          FlexTable ft = new FlexTable();
          JSONArray arr = jsonObject.get("valores").isArray();
          for (int j = 0; j < arr.size(); ++j) {
            JSONArray arr2 = arr.get(j).isArray();
            ft.insertRow(j);
            for (int k = 0; k < arr2.size(); ++k) {
              String val = arr2.get(k).isString().stringValue();
              ft.addCell(j);
              ft.setText(j, k, val);
              //ft.setWidth(k==0?"200px":"90px");
              //ft.getFlexCellFormatter().setStyleName(j,k,"spadies-tabn-celda");
              ft.getFlexCellFormatter().setStyleName(j,k,k==0?"spadies-tabn-celda2":"spadies-tabn-celda");
            }
          }
          ft.addStyleName("spadies-tabn");
          widget = new ScrollPanel(ft);
        } else if ("g".equals(tipo)) {
          widget = new HTML("<img src=\""+jsonObject.get("url").isString().stringValue()+"\" />");
        }
        this.add(widget, jsonObject.get("nombre").isString().stringValue());
      }
      this.selectTab(poss==-1?0:poss);
    }
  }
}
