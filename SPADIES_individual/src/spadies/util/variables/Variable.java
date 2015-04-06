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
package spadies.util.variables;

import java.util.*;
import spadies.kernel.*;
import spadies.util.*;

public enum Variable {
  INT_CLASE_IES(TipoVariable.TV_IES,"Clase final",new RangoByte<Byte>() {
    public Byte[] getRango() {
      //return new Byte[]{1,2,3,7,8,9};
      return new Byte[]{-1,1,2,3};
    }
    public Byte getRango(Byte origen) {
      return origen;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  1: return "A";
        case  2: return "B";
        case  3: return "C";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  INT_BLOQUEADA_IES(TipoVariable.TV_IES,"Bloqueada",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{1,2};
    }
    public Byte getRango(Byte origen) {
      return origen;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  1: return "Bloqueada";
        case  2: return "No bloqueada";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  @SuppressWarnings("unchecked")
  CODIGO_IES(TipoVariable.TV_IES,"Institución",new Rango<Integer,Integer>() {
    public Integer[] getRango() {
      return AmbienteVariables.getInstance().getCodigosIES();
    }
    public Integer getRango(Integer codigo) {
      return codigo;
    }
    public byte rangoToByte(Integer val) {
      return -1;  // Este caso se maneja de manera especial en KernelSPADIES
    }
    public Integer byteToRango(byte b) {
      return -1;  // Este caso se maneja de manera especial en KernelSPADIES
    }
    public String toString(Integer val) {
      return ""+val;
    }
    public String toStringHTML(Integer val) {
      IES ies=KernelSPADIES.getInstance().getIES(val);
      if (ies==null) return "<b>"+val+"</b>";
      int depto=ies.departamento,municipio=ies.municipio;
      String nombreIES=CajaDeHerramientas.stringToHTML(new String(ies.nombre)),nombreMunicipio=CajaDeHerramientas.stringToHTML(MUNICIPIO_IES.rango.toString(MUNICIPIO_IES.rango.getRango(new int[]{depto,municipio})));
      return "<b>"+val+"</b>  <i><font color=\"#000080\">"+nombreIES+"</font> <font color=\"#800000\">("+nombreMunicipio+")</font></i>";
    }
  }),
  ORIGEN_IES(TipoVariable.TV_IES,"Origen de la institución",new RangoByte<Byte>() {
    public Byte[] getRango() {
      //return new Byte[]{1,2,3,7,8,9};
      return new Byte[]{1,2};
    }
    public Byte getRango(Byte origen) {
      return origen;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  1: return "Oficial";
        case  2: return "No Oficial";
      /*
        case  1: return "Oficial nacional";
        case  2: return "Oficial departamental";
        case  3: return "Oficial municipal";
        case  7: return "No oficial - Fundación";
        case  8: return "No oficial - Corporación";
        case  9: return "Régimen especial";
        */
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  CARACTER_IES(TipoVariable.TV_IES,"Carácter de la institución",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{1,2,3,4/*,5,9*/};
    }
    public Byte getRango(Byte caracter) {
      return caracter;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  1: return "Universidad";
        case  2: return "Institución universitaria";
        case  3: return "Institución tecnológica";
        case  4: return "Técnica profesional";
        //case  5: return "Escuela tecnológica";
        //case  9: return "Régimen especial";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  DEPARTAMENTO_IES(TipoVariable.TV_IES,"Departamento",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{91,5,81,8,11,13,15,17,18,85,19,20,27,23,25,94,95,41,44,47,50,52,54,86,63,66,88,68,70,73,76,97,99};
    }
    public Byte getRango(Byte depto) {
      return depto;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case 91: return "Amazonas";
        case  5: return "Antioquia";
        case 81: return "Arauca";
        case  8: return "Atlántico";
        case 11: return "Bogotá";
        case 13: return "Bolívar";
        case 15: return "Boyacá";
        case 17: return "Caldas";
        case 18: return "Caquetá";
        case 85: return "Casanare";
        case 19: return "Cauca";
        case 20: return "Cesar";
        case 27: return "Chocó";
        case 23: return "Córdoba";
        case 25: return "Cundinamarca";
        case 94: return "Guainía";
        case 95: return "Guaviare";
        case 41: return "Huila";
        case 44: return "La Guajira";
        case 47: return "Magdalena";
        case 50: return "Meta";
        case 52: return "Nariño";
        case 54: return "Norte de Santander";
        case 86: return "Putumayo";
        case 63: return "Quindío";
        case 66: return "Risaralda";
        case 88: return "San Andrés y Providencia";
        case 68: return "Santander";
        case 70: return "Sucre";
        case 73: return "Tolima";
        case 76: return "Valle del Cauca";
        case 97: return "Vaupés";
        case 99: return "Vichada";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  ESTRATO(TipoVariable.TV_ESTUDIANTE,"Estrato",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,1,2,3,4,5,6,8};
    }
    public Byte getRango(Byte nivel) {
      return nivel;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  1: return "Estrato 1";
        case  2: return "Estrato 2";
        case  3: return "Estrato 3";
        case  4: return "Estrato 4";
        case  5: return "Estrato 5";
        case  6: return "Estrato 6";
        case  8: return "Vive en una zona rural donde no hay estratificación socioeconómica";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  MUNICIPIO_IES(TipoVariable.TV_IES,"Municipio",new Rango<int[],Integer>() {
    public Integer[] getRango() {
      return new Integer[0];
    }
    public Integer getRango(int[] deptoMun) {
      int codDepto=deptoMun[0],codMun=deptoMun[1];
      return codDepto*1000+codMun;
    }
    public byte rangoToByte(Integer u) {return 0;}
    public Integer byteToRango(byte b) {return 0;}
    public String toString(Integer u) {
      switch (u.intValue()) {
      case 5001: return "MEDELLIN";
      case 5002: return "ABEJORRAL";
      case 5004: return "ABRIAQUI";
      case 5021: return "ALEJANDRIA";
      case 5030: return "AMAGA";
      case 5031: return "AMALFI";
      case 5034: return "ANDES";
      case 5036: return "ANGELOPOLIS";
      case 5038: return "ANGOSTURA";
      case 5040: return "ANORI";
      case 5042: return "ANTIOQUIA";
      case 5044: return "ANZA";
      case 5045: return "APARTADO";
      case 5051: return "ARBOLETES";
      case 5055: return "ARGELIA";
      case 5059: return "ARMENIA";
      case 5079: return "BARBOSA";
      case 5086: return "BELMIRA";
      case 5088: return "BELLO";
      case 5091: return "BETANIA";
      case 5093: return "BETULIA";
      case 5101: return "BOLIVAR";
      case 5107: return "BRICEÑO";
      case 5113: return "BURITICA";
      case 5120: return "CACERES";
      case 5125: return "CAICEDO";
      case 5129: return "CALDAS";
      case 5134: return "CAMPAMENTO";
      case 5138: return "CAÑASGORDAS";
      case 5142: return "CARACOLI";
      case 5145: return "CARAMANTA";
      case 5147: return "CAREPA";
      case 5148: return "CARMEN DE VIBORAL";
      case 5150: return "CAROLINA";
      case 5154: return "CAUCASIA";
      case 5172: return "CHIGORODO";
      case 5190: return "CISNEROS";
      case 5197: return "COCORNA";
      case 5206: return "CONCEPCION";
      case 5209: return "CONCORDIA";
      case 5212: return "COPACABANA";
      case 5224: return "CUASPUD";
      case 5234: return "DABEIBA";
      case 5237: return "DONMATIAS";
      case 5240: return "EBEJICO";
      case 5250: return "EL BAGRE";
      case 5264: return "ENTRERRIOS";
      case 5266: return "ENVIGADO";
      case 5282: return "FREDONIA";
      case 5284: return "FRONTINO";
      case 5306: return "GIRALDO";
      case 5308: return "GIRARDOTA";
      case 5310: return "GOMEZ PLATA";
      case 5313: return "GRANADA";
      case 5315: return "GUADALUPE";
      case 5318: return "GUARNE";
      case 5321: return "GUATAPE";
      case 5347: return "HELICONIA";
      case 5353: return "HISPANIA";
      case 5360: return "ITAGUI";
      case 5361: return "ITUANGO";
      case 5364: return "JARDIN";
      case 5368: return "JERICO";
      case 5376: return "LA CEJA";
      case 5380: return "LA ESTRELLA";
      case 5390: return "LA PINTADA";
      case 5400: return "LA UNION";
      case 5411: return "LIBORINA";
      case 5425: return "MACEO";
      case 5440: return "MARINILLA";
      case 5467: return "MONTEBELLO";
      case 5475: return "MURINDO";
      case 5480: return "MUTATA";
      case 5483: return "NARINO";
      case 5490: return "NECOCLI";
      case 5495: return "NECHI";
      case 5501: return "OLAYA";
      case 5541: return "PENOL";
      case 5543: return "PEQUE";
      case 5576: return "PUEBLORRICO";
      case 5579: return "PUERTO BERRIO";
      case 5585: return "PTO NARE";
      case 5591: return "PUERTO TRIUNFO";
      case 5604: return "REMEDIOS";
      case 5607: return "RETIRO";
      case 5615: return "RIONEGRO";
      case 5628: return "SABANALARGA";
      case 5631: return "SABANETA";
      case 5642: return "SALGAR";
      case 5647: return "SAN ANDRES";
      case 5649: return "SAN CARLOS";
      case 5652: return "SAN FRANCISCO";
      case 5656: return "SAN JERONIMO";
      case 5658: return "SAN JOSE DE LA MONTAÑA";
      case 5659: return "SAN JUAN DE URABA";
      case 5660: return "SAN LUIS";
      case 5664: return "SAN PEDRO";
      case 5665: return "SAN PEDRO DE URABA";
      case 5667: return "SAN RAFAEL";
      case 5670: return "SAN ROQUE";
      case 5674: return "SAN VICENTE";
      case 5679: return "SANTA BARBARA";
      case 5686: return "STA ROSA DE OSOS";
      case 5690: return "SANTO DOMINGO";
      case 5697: return "SANTUARIO";
      case 5736: return "SEGOVIA";
      case 5756: return "SONSON";
      case 5761: return "SOPETRAN";
      case 5789: return "TAMESIS";
      case 5790: return "TARAZA";
      case 5792: return "TARSO";
      case 5809: return "TITIRIBI";
      case 5819: return "TOLEDO";
      case 5837: return "TURBO";
      case 5842: return "URAMITA";
      case 5847: return "URRAO";
      case 5854: return "VALDIVIA";
      case 5856: return "VALPARAISO";
      case 5858: return "VEGACHI";
      case 5861: return "VENECIA";
      case 5873: return "VIGIA DEL FUERTE";
      case 5885: return "YALI";
      case 5887: return "YARUMAL";
      case 5890: return "YOLOMBO";
      case 5893: return "YONDO";
      case 5895: return "ZARAGOZA";
      case 8001: return "BARRANQUILLA";
      case 8078: return "BARANOA";
      case 8137: return "CAMPO DE LA CRUZ";
      case 8141: return "CANDELARIA";
      case 8296: return "GALAPA";
      case 8372: return "JUAN DE ACOSTA";
      case 8421: return "LURUACO";
      case 8433: return "MALAMBO";
      case 8436: return "MANATI";
      case 8520: return "PALMAR DE VARELA";
      case 8549: return "PIOJO";
      case 8558: return "POLONUEVO";
      case 8560: return "PONEDERA";
      case 8573: return "PUERTO COLOMBIA";
      case 8606: return "REPELON";
      case 8634: return "SABANAGRANDE";
      case 8638: return "SABANALARGA";
      case 8675: return "SANTA LUCIA";
      case 8685: return "SANTO TOMAS";
      case 8758: return "SOLEDAD";
      case 8770: return "SUAN";
      case 8832: return "TUBARA";
      case 8849: return "USIACURI";
      case 11001: return "BOGOTA";
      case 13001: return "CARTAGENA";
      case 13006: return "ACHI";
      case 13030: return "ALTOS DEL ROSARIO";
      case 13042: return "ARENAL";
      case 13052: return "ARJONA";
      case 13062: return "ARROYOHONDO";
      case 13074: return "BARRANCO DE LOBA";
      case 13140: return "CALAMAR";
      case 13160: return "CANTAGALLO";
      case 13188: return "CICUCO";
      case 13212: return "CORDOBA";
      case 13222: return "CLEMENCIA";
      case 13244: return "EL CARMEN DE BOLIVAR";
      case 13248: return "EL GUAMO";
      case 13268: return "EL PENON";
      case 13300: return "HATILLO DE LOBA";
      case 13430: return "MAGANGUE";
      case 13433: return "MAHATES";
      case 13440: return "MARGARITA";
      case 13442: return "MARIA LA BAJA";
      case 13458: return "MONTECRISTO";
      case 13468: return "MOMPOS";
      case 13473: return "MORALES";
      case 13549: return "PINILLOS";
      case 13580: return "REGIDOR";
      case 13600: return "RIO VIEJO";
      case 13620: return "SAN CRISTOBAL";
      case 13647: return "SAN ESTANISLAO";
      case 13650: return "SAN FERNANDO";
      case 13654: return "SAN JACINTO";
      case 13655: return "SAN JACINTO DEL CAUCA";
      case 13657: return "SAN JUAN NEPOMUCENO";
      case 13667: return "SAN MARTIN DE LOBA";
      case 13670: return "SAN PABLO";
      case 13673: return "SANTA CATALINA";
      case 13683: return "SANTA ROSA DE LIMA";
      case 13688: return "SANTA ROSA DEL SUR";
      case 13744: return "SIMITI";
      case 13760: return "SOPLAVIENTO";
      case 13780: return "TALAIGUA NUEVO";
      case 13810: return "TIQUISIO";
      case 13836: return "TURBACO";
      case 13838: return "TURBANA";
      case 13873: return "VILLANUEVA";
      case 13894: return "ZAMBRANO";
      case 15001: return "TUNJA";
      case 15022: return "ALMEIDA";
      case 15047: return "AQUITANIA";
      case 15051: return "ARCABUCO";
      case 15087: return "BELEN";
      case 15090: return "BERBEO";
      case 15092: return "BETEITIVA";
      case 15097: return "BOAVITA";
      case 15104: return "BOYACA";
      case 15106: return "BRICEÑO";
      case 15109: return "BUENAVISTA";
      case 15114: return "BUSBANZA";
      case 15131: return "CALDAS";
      case 15135: return "CAMPOHERMOSO";
      case 15162: return "CERINZA";
      case 15172: return "CHINAVITA";
      case 15176: return "CHIQUINQUIRA";
      case 15180: return "CHISCAS";
      case 15183: return "CHITA";
      case 15185: return "CHITARAQUE";
      case 15187: return "CHIVATA";
      case 15189: return "CIENEGA";
      case 15204: return "COMBITA";
      case 15212: return "COPER";
      case 15215: return "CORRALES";
      case 15218: return "COVARACHIA";
      case 15223: return "CUBARA";
      case 15224: return "CUCAITA";
      case 15226: return "CUITIVA";
      case 15232: return "CHIQUIZA";
      case 15236: return "CHIVOR";
      case 15238: return "DUITAMA";
      case 15244: return "EL COCUY";
      case 15248: return "EL ESPINO";
      case 15272: return "FIRAVITOBA";
      case 15276: return "FLORESTA";
      case 15293: return "GACHANTIVA";
      case 15296: return "GAMEZA";
      case 15299: return "GARAGOA";
      case 15317: return "GUACAMAYAS";
      case 15322: return "GUATEQUE";
      case 15325: return "GUAYATA";
      case 15332: return "GUICAN";
      case 15362: return "IZA";
      case 15367: return "JENESANO";
      case 15368: return "JERICO";
      case 15377: return "LABRANZAGRANDE";
      case 15380: return "LA CAPILLA";
      case 15401: return "LA VICTORIA";
      case 15403: return "LA UVITA";
      case 15407: return "VILLA DE LEIVA";
      case 15425: return "MACANAL";
      case 15442: return "MARIPI";
      case 15455: return "MIRAFLORES";
      case 15464: return "MONGUA";
      case 15466: return "MONGUI";
      case 15469: return "MONIQUIRA";
      case 15476: return "MOTAVITA";
      case 15480: return "MUZO";
      case 15491: return "NOBSA";
      case 15494: return "NUEVO COLON";
      case 15500: return "OICATA";
      case 15507: return "OTANCHE";
      case 15511: return "PACHAVITA";
      case 15514: return "PAEZ";
      case 15516: return "PAIPA";
      case 15518: return "PAJARITO";
      case 15522: return "PANQUEBA";
      case 15531: return "PAUNA";
      case 15533: return "PAYA";
      case 15537: return "PAZ DE RIO";
      case 15542: return "PESCA";
      case 15550: return "PISBA";
      case 15572: return "PUERTO BOYACA";
      case 15580: return "QUIPAMA";
      case 15599: return "RAMIRIQUI";
      case 15600: return "RAQUIRA";
      case 15621: return "RONDON";
      case 15632: return "SABOYA";
      case 15638: return "SACHICA";
      case 15646: return "SAMACA";
      case 15660: return "SAN EDUARDO";
      case 15664: return "SAN JOSE DE PARE";
      case 15667: return "SAN LUIS DE GACENO";
      case 15673: return "SAN MATEO";
      case 15676: return "SAN MIGUEL DE SEMA";
      case 15681: return "SAN PABLO DE BORBUR";
      case 15686: return "SANTANA";
      case 15690: return "SANTA MARIA";
      case 15693: return "SANTA ROSA DE VITERBO";
      case 15696: return "SANTA SOFIA";
      case 15720: return "SATIVANORTE";
      case 15723: return "SATIVASUR";
      case 15740: return "SIACHOQUE";
      case 15753: return "SOATA";
      case 15755: return "SOCOTA";
      case 15757: return "SOCHA";
      case 15759: return "SOGAMOSO";
      case 15761: return "SOMONDOCO";
      case 15762: return "SORA";
      case 15763: return "SOTAQUIRA";
      case 15764: return "SORACA";
      case 15774: return "SUSACON";
      case 15776: return "SUTAMARCHAN";
      case 15778: return "SUTATENZA";
      case 15790: return "TASCO";
      case 15798: return "TENZA";
      case 15804: return "TIBANA";
      case 15806: return "TIBASOSA";
      case 15808: return "TINJACA";
      case 15810: return "TIPACOQUE";
      case 15814: return "TOCA";
      case 15816: return "TOGUI";
      case 15820: return "TOPAGA";
      case 15822: return "TOTA";
      case 15832: return "TUNUNGUA";
      case 15835: return "TURMEQUE";
      case 15837: return "TUTA";
      case 15839: return "TUTAZA";
      case 15842: return "UMBITA";
      case 15861: return "VENTAQUEMADA";
      case 15879: return "VIRACACHA";
      case 15897: return "ZETAQUIRA";
      case 17001: return "MANIZALES";
      case 17013: return "AGUADAS";
      case 17042: return "ANSERMA";
      case 17050: return "ARANZAZU";
      case 17088: return "BELALCAZAR";
      case 17174: return "CHINCHINA";
      case 17272: return "FILADELFIA";
      case 17380: return "LA DORADA";
      case 17388: return "LA MERCED";
      case 17433: return "MANZANARES";
      case 17442: return "MARMATO";
      case 17444: return "MARQUETALIA";
      case 17446: return "MARULANDA";
      case 17486: return "NEIRA";
      case 17495: return "NORCASIA";
      case 17513: return "PACORA";
      case 17524: return "PALESTINA";
      case 17541: return "PENSILVANIA";
      case 17614: return "RIOSUCIO";
      case 17616: return "RISARALDA";
      case 17653: return "SALAMINA";
      case 17662: return "SAMANA";
      case 17665: return "SAN JOSE";
      case 17777: return "SUPIA";
      case 17867: return "VICTORIA";
      case 17873: return "VILLAMARIA";
      case 17877: return "VITERBO";
      case 18001: return "FLORENCIA";
      case 18029: return "ALBANIA";
      case 18094: return "BELEN DE ANDAQUIES";
      case 18150: return "CARTAGENA DEL CHAIRA";
      case 18205: return "CURILLO";
      case 18247: return "EL DONCELLO";
      case 18256: return "EL PAUJIL";
      case 18410: return "LA MONTAÑITA";
      case 18460: return "MILAN";
      case 18479: return "MORELIA";
      case 18592: return "PUERTO RICO";
      case 18610: return "SAN JOSE DE LA FRAGUA";
      case 18753: return "SAN VICENTE DEL CAGUAN";
      case 18756: return "SOLANO";
      case 18765: return "SOLANO";
      case 18785: return "SOLITA";
      case 18860: return "VALPARAISO";
      case 19001: return "POPAYAN";
      case 19022: return "ALMAGUER";
      case 19050: return "ARGELIA";
      case 19075: return "BALBOA";
      case 19100: return "BOLIVAR";
      case 19110: return "BUENOS AIRES";
      case 19130: return "CAJIBIO";
      case 19137: return "CALDONO";
      case 19142: return "CALOTO";
      case 19212: return "CORINTO";
      case 19256: return "EL TAMBO";
      case 19290: return "FLORENCIA";
      case 19318: return "GUAPI";
      case 19355: return "INZA";
      case 19364: return "JAMBALO";
      case 19392: return "LA SIERRA";
      case 19397: return "LA VEGA";
      case 19418: return "LOPEZ";
      case 19450: return "MERCADERES";
      case 19455: return "MIRANDA";
      case 19473: return "MORALES";
      case 19513: return "PADILLA";
      case 19517: return "PAEZ";
      case 19532: return "PATIA(EL BORDO)";
      case 19533: return "PIAMONTE";
      case 19548: return "PIENDAMO";
      case 19573: return "PUERTO TEJADA";
      case 19585: return "PURACE";
      case 19622: return "ROSAS";
      case 19693: return "SAN SEBASTIAN";
      case 19698: return "SANTANDER DE QUILICHAO";
      case 19701: return "SANTA ROSA";
      case 19743: return "SILVIA";
      case 19760: return "SOTARA";
      case 19780: return "SUAREZ";
      case 19785: return "SUCRE";
      case 19807: return "TIMBIO";
      case 19809: return "TIMBIQUI";
      case 19821: return "TORIBIO";
      case 19824: return "TOTORO";
      case 19845: return "VILLARICA";
      case 20001: return "VALLEDUPAR";
      case 20011: return "AGUACHICA";
      case 20013: return "AGUSTIN CODAZZI";
      case 20032: return "ASTREA";
      case 20045: return "BECERRIL";
      case 20060: return "BOSCONIA";
      case 20175: return "CHIMICHAGUA";
      case 20178: return "CHIRIGUANA";
      case 20228: return "CURUMANI";
      case 20238: return "EL COPEY";
      case 20250: return "EL PASO";
      case 20295: return "GAMARRA";
      case 20310: return "GONZALEZ";
      case 20383: return "LA GLORIA";
      case 20400: return "LA JAGUA IBIRICO";
      case 20443: return "MANAURE";
      case 20517: return "PAILITAS";
      case 20550: return "PELAYA";
      case 20570: return "PUEBLO BELLO";
      case 20614: return "RIO DE ORO";
      case 20621: return "LA PAZ";
      case 20710: return "SAN ALBERTO";
      case 20750: return "SAN DIEGO";
      case 20770: return "SAN MARTIN";
      case 20787: return "TAMALAMEQUE";
      case 23001: return "MONTERIA";
      case 23068: return "AYAPEL";
      case 23079: return "BUENAVISTA";
      case 23090: return "CANALETE";
      case 23162: return "CERETE";
      case 23168: return "CHIMA";
      case 23182: return "CHINU";
      case 23189: return "CIENAGA DE ORO";
      case 23300: return "COTORRA";
      case 23350: return "LA APARTADA";
      case 23417: return "LORICA";
      case 23419: return "LOS CORDOBAS";
      case 23464: return "MOMIL";
      case 23466: return "MONTELIBANO";
      case 23500: return "MONITOS";
      case 23555: return "PLANETA RICA";
      case 23570: return "PUEBLO NUEVO";
      case 23574: return "PUERTO ESCONDIDO";
      case 23580: return "PUERTO LIBERTADOR";
      case 23586: return "PURISIMA";
      case 23660: return "SAHAGUN";
      case 23670: return "SAN ANDRES SOTAVENTO";
      case 23672: return "SAN ANTERO";
      case 23675: return "SAN BERNARDO DEL VIENTO";
      case 23678: return "SAN CARLOS";
      case 23686: return "SAN PELAYO";
      case 23807: return "TIERRALTA";
      case 23855: return "VALENCIA";
      case 25001: return "AGUA DE DIOS";
      case 25019: return "ALBAN";
      case 25035: return "ANAPOIMA";
      case 25040: return "ANOLAIMA";
      case 25053: return "ARBELAEZ";
      case 25086: return "BELTRAN";
      case 25095: return "BITUIMA";
      case 25099: return "BOJACA";
      case 25120: return "CABRERA";
      case 25123: return "CACHIPAY";
      case 25126: return "CAJICA";
      case 25148: return "CAPARRAPI";
      case 25151: return "CAQUEZA";
      case 25154: return "CARMEN DE CARUPA";
      case 25168: return "CHAGUANI";
      case 25175: return "CHIA";
      case 25178: return "CHIPAQUE";
      case 25181: return "CHOACHI";
      case 25183: return "CHOCONTA";
      case 25200: return "COGUA";
      case 25214: return "COTA";
      case 25224: return "CUCUNUBA";
      case 25245: return "EL COLEGIO";
      case 25258: return "EL PEÑON";
      case 25260: return "EL ROSAL";
      case 25269: return "FACATATIVA";
      case 25279: return "FOMEQUE";
      case 25281: return "FOSCA";
      case 25286: return "FUNZA";
      case 25288: return "FUQUENE";
      case 25290: return "FUSAGASUGA";
      case 25293: return "GACHALA";
      case 25295: return "GACHANCIPA";
      case 25297: return "GACHETA";
      case 25299: return "GAMA";
      case 25307: return "GIRARDOT";
      case 25312: return "GRANADA";
      case 25317: return "GUACHETA";
      case 25320: return "GUADUAS";
      case 25322: return "GUASCA";
      case 25324: return "GUATAQUI";
      case 25326: return "GUATAVITA";
      case 25328: return "GUAYABAL DE SIQUIMA";
      case 25335: return "GUAYABETAL";
      case 25339: return "GUTIERREZ";
      case 25368: return "JERUSALEN";
      case 25372: return "JUNIN";
      case 25377: return "LA CALERA";
      case 25386: return "LA MESA";
      case 25394: return "LA PALMA";
      case 25398: return "LA PENA";
      case 25402: return "LA VEGA";
      case 25407: return "LENGUAZAQUE";
      case 25426: return "MACHETA";
      case 25430: return "MADRID";
      case 25436: return "MANTA";
      case 25438: return "MEDINA";
      case 25473: return "MOSQUERA";
      case 25483: return "NARIÑO";
      case 25486: return "NEMOCON";
      case 25488: return "NILO";
      case 25489: return "NIMAIMA";
      case 25491: return "NOCAIMA";
      case 25506: return "VENECIA";
      case 25513: return "PACHO";
      case 25518: return "PAIME";
      case 25524: return "PANDI";
      case 25530: return "PARATEBUENO";
      case 25535: return "PASCA";
      case 25572: return "PUERTO SALGAR";
      case 25580: return "PULI";
      case 25592: return "QUEBRADANEGRA";
      case 25594: return "QUETAME";
      case 25596: return "QUIPILE";
      case 25599: return "APULO";
      case 25612: return "RICAURTE";
      case 25645: return "SAN ANTONIO DEL TEQUENDAMA";
      case 25646: return "SAN ANTONIO DE TEQUENDAMA";
      case 25649: return "SAN BERNARDO";
      case 25653: return "SAN CAYETANO";
      case 25658: return "SAN FRANCISCO";
      case 25662: return "SAN JUAN DE RIOSECO";
      case 25718: return "SASAIMA";
      case 25736: return "SESQUILE";
      case 25740: return "SIBATE";
      case 25743: return "SILVANIA";
      case 25745: return "SIMIJACA";
      case 25754: return "SOACHA";
      case 25758: return "SOPO";
      case 25769: return "SUBACHOQUE";
      case 25772: return "SUESCA";
      case 25777: return "SUPATA";
      case 25779: return "SUSA";
      case 25781: return "SUTATAUSA";
      case 25785: return "TABIO";
      case 25793: return "TAUSA";
      case 25797: return "TENA";
      case 25799: return "TENJO";
      case 25805: return "TIBACUY";
      case 25807: return "TIBIRITA";
      case 25815: return "TOCAIMA";
      case 25817: return "TOCANCIPA";
      case 25823: return "TOPAIPI";
      case 25839: return "UBALA";
      case 25841: return "UBAQUE";
      case 25843: return "UBATE";
      case 25845: return "UNE";
      case 25851: return "UTICA";
      case 25862: return "VERGARA";
      case 25867: return "VIANI";
      case 25871: return "VILLAGOMEZ";
      case 25873: return "VILLAPINZON";
      case 25875: return "VILLETA";
      case 25878: return "VIOTA";
      case 25885: return "YACOPI";
      case 25898: return "ZIPACON";
      case 25899: return "ZIPAQUIRA";
      case 25999: return "EL ROSAL";
      case 27000: return "MALPELO";
      case 27001: return "QUIBDO";
      case 27006: return "ACANDI";
      case 27025: return "ALTO BAUDO";
      case 27050: return "ATRATO";
      case 27073: return "BAGADO";
      case 27075: return "BAHIA SOLANO";
      case 27077: return "BAJO BAUDO";
      case 27082: return "LITORAL DEL SAN JUAN";
      case 27086: return "BELEN DE BAJIRA";
      case 27099: return "BOJAYA";
      case 27135: return "CANTON DEL SAN PABLO";
      case 27150: return "CARMEN DEL DARIEN";
      case 27160: return "CERTEGUI";
      case 27205: return "CONDOTO";
      case 27245: return "CARMEN DE ATRATO";
      case 27250: return "LITORAL DE SAN JUAN(1993)";
      case 27361: return "ITSMINA";
      case 27372: return "JURADO";
      case 27413: return "LLORO";
      case 27425: return "MEDIO ATRATO";
      case 27430: return "MEDIO BAUDO";
      case 27450: return "MEDIO SAN JUAN";
      case 27491: return "NOVITA";
      case 27495: return "NUQUI";
      case 27580: return "RIO IRO";
      case 27600: return "RIO QUITO";
      case 27615: return "RIOSUCIO";
      case 27660: return "SAN JOSE DEL PALMAR";
      case 27745: return "SIPI";
      case 27787: return "TADO";
      case 27800: return "UNGUIA";
      case 27810: return "UNION PANAMERICANA";
      case 41001: return "NEIVA";
      case 41006: return "ACEVEDO";
      case 41013: return "AGRADO";
      case 41016: return "AIPE";
      case 41020: return "ALGECIRAS";
      case 41026: return "ALTAMIRA";
      case 41078: return "BARAYA";
      case 41132: return "CAMPOALEGRE";
      case 41206: return "COLOMBIA";
      case 41244: return "ELIAS";
      case 41298: return "GARZON";
      case 41306: return "GIGANTE";
      case 41319: return "GUADALUPE";
      case 41349: return "HOBO";
      case 41357: return "IQUIRA";
      case 41359: return "ISNOS";
      case 41378: return "LA ARGENTINA";
      case 41396: return "LA PLATA";
      case 41483: return "NATAGA";
      case 41503: return "OPORAPA";
      case 41518: return "PAICOL";
      case 41524: return "PALERMO";
      case 41530: return "PALESTINA";
      case 41548: return "PITAL";
      case 41551: return "PITALITO";
      case 41615: return "RIVERA";
      case 41660: return "SALADOBLANCO";
      case 41668: return "SAN AGUSTIN";
      case 41676: return "SANTA MARIA";
      case 41770: return "SUAZA";
      case 41791: return "TARQUI";
      case 41797: return "TESALIA";
      case 41799: return "TELLO";
      case 41801: return "TERUEL";
      case 41807: return "TIMANA";
      case 41872: return "VILLAVIEJA";
      case 41885: return "YAGUARA";
      case 44001: return "RIOHACHA";
      case 44035: return "ALBANIA";
      case 44078: return "BARRANCAS";
      case 44090: return "DIBULLA";
      case 44098: return "DISTRACCION";
      case 44110: return "EL MOLINO";
      case 44279: return "FONSECA";
      case 44378: return "HATONUEVO";
      case 44420: return "LA JAGUA DEL PILAR";
      case 44430: return "MAICAO";
      case 44560: return "MANAURE";
      case 44650: return "SAN JUAN DEL CESAR";
      case 44847: return "URIBIA";
      case 44855: return "URUMITA";
      case 44874: return "VILLANUEVA";
      case 44999: return "HATONUEVO";
      case 47001: return "SANTA MARTA";
      case 47004: return "0";
      case 47030: return "ALGARROBO";
      case 47053: return "ARACATACA";
      case 47058: return "ARIGUANI";
      case 47161: return "CERRO SAN ANTONIO";
      case 47170: return "CHIVOLO";
      case 47189: return "CIENAGA";
      case 47205: return "CONCORDIA";
      case 47245: return "EL BANCO";
      case 47258: return "EL PIÑON";
      case 47268: return "EL RETEN";
      case 47288: return "FUNDACION";
      case 47318: return "GUAMAL";
      case 47460: return "NUEVA GRANADA";
      case 47541: return "PEDRAZA";
      case 47545: return "PIJIÑO DEL CARMEN";
      case 47551: return "PIVIJAY";
      case 47555: return "PLATO";
      case 47570: return "PUEBLOVIEJO";
      case 47605: return "REMOLINO";
      case 47660: return "SAN ANGEL";
      case 47675: return "SALAMINA";
      case 47692: return "SAN SEBASTIAN DE BUENAVISTA";
      case 47703: return "SAN ZENON";
      case 47707: return "SANTA ANA";
      case 47720: return "SANTA BARBARA DE PINTO";
      case 47745: return "SITIONUEVO";
      case 47798: return "TENERIFE";
      case 47960: return "ZAPAYAN";
      case 47980: return "ZONA BANANERA";
      case 50001: return "VILLAVICENCIO";
      case 50006: return "ACACIAS";
      case 50110: return "BARRANCA DE UPIA";
      case 50124: return "CABUYARO";
      case 50150: return "CASTILLA LA NUEVA";
      case 50223: return "CUBARRAL";
      case 50226: return "CUMARAL";
      case 50245: return "EL CALVARIO";
      case 50251: return "EL CASTILLO";
      case 50270: return "EL DORADO";
      case 50287: return "FUENTE DE ORO";
      case 50313: return "GRANADA";
      case 50318: return "GUAMAL";
      case 50325: return "MAPIRIPAN";
      case 50330: return "MESETAS";
      case 50350: return "LA MACARENA";
      case 50370: return "URIBE";
      case 50400: return "LEJANIAS";
      case 50450: return "PUERTO CONCORDIA";
      case 50568: return "PUERTO GAITAN";
      case 50573: return "PUERTO LOPEZ";
      case 50577: return "PUERTO LLERAS";
      case 50590: return "PUERTO RICO";
      case 50606: return "RESTREPO";
      case 50680: return "SAN CARLOS DE GUAROA";
      case 50683: return "SAN JUAN DE ARAMA";
      case 50686: return "SAN JUANITO";
      case 50689: return "SAN MARTIN";
      case 50711: return "VISTA HERMOSA";
      case 52001: return "PASTO";
      case 52019: return "ALBAN";
      case 52022: return "ALDANA";
      case 52036: return "ANCUYA";
      case 52051: return "ARBOLEDA";
      case 52079: return "BARBACOAS";
      case 52083: return "BELEN";
      case 52110: return "BUESACO";
      case 52203: return "COLON";
      case 52207: return "CONSACA";
      case 52210: return "CONTADERO";
      case 52215: return "CORDOBA";
      case 52224: return "CUASPUD";
      case 52227: return "CUMBAL";
      case 52233: return "CUMBITARA";
      case 52240: return "CHACHAGUI(1992)";
      case 52250: return "EL CHARCO";
      case 52254: return "EL PEÑOL";
      case 52256: return "EL ROSARIO";
      case 52258: return "EL TABLON";
      case 52260: return "EL TAMBO";
      case 52287: return "FUNES";
      case 52317: return "GUACHUCAL";
      case 52320: return "GUAITARILLA";
      case 52323: return "GUALMATAN";
      case 52352: return "ILES";
      case 52354: return "IMUES";
      case 52356: return "IPIALES";
      case 52378: return "LA CRUZ";
      case 52381: return "LA FLORIDA";
      case 52385: return "LA LLANADA";
      case 52390: return "LA TOLA";
      case 52399: return "LA UNION";
      case 52405: return "LEIVA";
      case 52411: return "LINARES";
      case 52418: return "LOS ANDES";
      case 52427: return "MAGUI";
      case 52435: return "MALLAMA";
      case 52473: return "MOSQUERA";
      case 52480: return "NARIÑO";
      case 52490: return "OLAYA HERRERA";
      case 52506: return "OSPINA";
      case 52520: return "FRANCISCO PIZARRO";
      case 52540: return "POLICARPA";
      case 52560: return "POTOSI";
      case 52565: return "PROVIDENCIA1992";
      case 52573: return "PUERRES";
      case 52585: return "PUPIALES";
      case 52612: return "RICAURTE";
      case 52621: return "ROBERTO PAYAN";
      case 52678: return "SAMANIEGO";
      case 52683: return "SANDONA";
      case 52685: return "SAN BERNARDO";
      case 52687: return "SAN LORENZO";
      case 52693: return "SAN PABLO";
      case 52694: return "SAN PEDRO DE CARTAGO";
      case 52696: return "SANTA BARBARA";
      case 52699: return "SANTACRUZ";
      case 52720: return "SAPUYES";
      case 52786: return "TAMINANGO";
      case 52788: return "TANGUA";
      case 52835: return "TUMACO";
      case 52838: return "TUQUERRES";
      case 52885: return "YACUANQUER";
      case 54001: return "CUCUTA";
      case 54003: return "ABREGO";
      case 54051: return "ARBOLEDAS";
      case 54099: return "BOCHALEMA";
      case 54109: return "BUCARASICA";
      case 54125: return "CACOTA";
      case 54128: return "CACHIRA";
      case 54172: return "CHINACOTA";
      case 54174: return "CHITAGA";
      case 54206: return "CONVENCION";
      case 54223: return "CUCUTILLA";
      case 54239: return "DURANIA";
      case 54245: return "EL CARMEN";
      case 54250: return "EL TARRA";
      case 54261: return "EL ZULIA";
      case 54313: return "GRAMALOTE";
      case 54344: return "HACARI";
      case 54347: return "HERRAN";
      case 54377: return "LABATECA";
      case 54385: return "LA ESPERANZA";
      case 54398: return "LA PLAYA";
      case 54405: return "LOS PATIOS";
      case 54418: return "LOURDES";
      case 54480: return "MUTISCUA";
      case 54498: return "OCAÑA";
      case 54518: return "PAMPLONA";
      case 54520: return "PAMPLONITA";
      case 54553: return "PUERTO SANTANDER";
      case 54599: return "RAGONVALIA";
      case 54660: return "SALAZAR";
      case 54670: return "SAN CALIXTO";
      case 54673: return "SAN CAYETANO";
      case 54680: return "SANTIAGO";
      case 54720: return "SARDINATA";
      case 54743: return "SILOS";
      case 54800: return "TEORAMA";
      case 54810: return "TIBU";
      case 54820: return "TOLEDO";
      case 54871: return "VILLA CARO";
      case 54874: return "VILLA DEL ROSARIO";
      case 63001: return "ARMENIA";
      case 63111: return "BUENAVISTA";
      case 63130: return "CALARCA";
      case 63190: return "CIRCASIA";
      case 63212: return "CORDOBA";
      case 63272: return "FILANDIA";
      case 63302: return "GENOVA";
      case 63401: return "LA TEBAIDA";
      case 63470: return "MONTENEGRO";
      case 63548: return "PIJAO";
      case 63594: return "QUIMBAYA";
      case 63690: return "SALENTO";
      case 66001: return "PEREIRA";
      case 66045: return "APIA";
      case 66075: return "BALBOA";
      case 66088: return "BELEN DE UMBRIA";
      case 66170: return "DOSQUEBRADAS";
      case 66318: return "GUATICA";
      case 66383: return "LA CELIA";
      case 66400: return "LA VIRGINIA";
      case 66440: return "MARSELLA";
      case 66456: return "MISTRATO";
      case 66572: return "PUEBLO RICO";
      case 66594: return "QUINCHIA";
      case 66682: return "SANTA ROSA DE CABAL";
      case 66687: return "SANTUARIO";
      case 68001: return "BUCARAMANGA";
      case 68013: return "AGUADA";
      case 68020: return "ALBANIA";
      case 68051: return "ARATOCA";
      case 68077: return "BARBOSA";
      case 68079: return "BARICHARA";
      case 68081: return "BARRANCABERMEJA";
      case 68092: return "BETULIA";
      case 68101: return "BOLIVAR";
      case 68121: return "CABRERA";
      case 68132: return "CALIFORNIA";
      case 68147: return "CAPITANEJO";
      case 68152: return "CARCASI";
      case 68160: return "CEPITA";
      case 68162: return "CERRITO";
      case 68167: return "CHARALA";
      case 68169: return "CHARTA";
      case 68176: return "CHIMA";
      case 68179: return "CHIPATA";
      case 68190: return "CIMITARRA";
      case 68207: return "CONCEPCION";
      case 68209: return "CONFINES";
      case 68211: return "CONTRATACION";
      case 68217: return "COROMORO";
      case 68229: return "CURITI";
      case 68235: return "EL CARMEN";
      case 68245: return "EL GUACAMAYO";
      case 68250: return "EL PEÑON";
      case 68255: return "EL PLAYON";
      case 68264: return "ENCINO";
      case 68266: return "ENCISO";
      case 68271: return "FLORIAN";
      case 68276: return "FLORIDABLANCA";
      case 68296: return "GALAN";
      case 68298: return "GAMBITA";
      case 68307: return "GIRON";
      case 68318: return "GUACA";
      case 68320: return "GUADALUPE";
      case 68322: return "GUAPOTA";
      case 68324: return "GUAVATA";
      case 68327: return "GUEPSA";
      case 68344: return "HATO";
      case 68368: return "JESUS MARIA";
      case 68370: return "JORDAN";
      case 68377: return "LA BELLEZA";
      case 68385: return "LANDAZURI";
      case 68397: return "LA PAZ";
      case 68406: return "LEBRIJA";
      case 68418: return "LOS SANTOS";
      case 68425: return "MACARAVITA";
      case 68432: return "MALAGA";
      case 68444: return "MATANZA";
      case 68464: return "MOGOTES";
      case 68468: return "MOLAGAVITA";
      case 68498: return "OCAMONTE";
      case 68500: return "OIBA";
      case 68502: return "ONZAGA";
      case 68522: return "PALMAR";
      case 68524: return "PALMAS DEL SOCORRO";
      case 68533: return "PARAMO";
      case 68547: return "PIEDECUESTA";
      case 68549: return "PINCHOTE";
      case 68572: return "PUENTE NACIONAL";
      case 68573: return "PUERTO PARRA";
      case 68575: return "PUERTO WILCHES";
      case 68615: return "RIONEGRO";
      case 68655: return "SABANA DE TORRES";
      case 68669: return "SAN ANDRES";
      case 68673: return "SAN BENITO";
      case 68679: return "SAN GIL";
      case 68682: return "SAN JOAQUIN";
      case 68684: return "SAN JOSE DE MIRANDA";
      case 68686: return "SAN MIGUEL";
      case 68689: return "SAN VICENTE DE CHUCURI";
      case 68705: return "SANTA BARBARA";
      case 68720: return "SANTA HELENA OPON";
      case 68745: return "SIMACOTA";
      case 68755: return "SOCORRO";
      case 68770: return "SUAITA";
      case 68773: return "SUCRE";
      case 68780: return "SURATA";
      case 68820: return "TONA";
      case 68855: return "VALLE SAN JOSE";
      case 68861: return "VELEZ";
      case 68867: return "VETAS";
      case 68872: return "VILLANUEVA";
      case 68895: return "ZAPATOCA";
      case 70001: return "SINCELEJO";
      case 70110: return "BUENAVISTA";
      case 70124: return "CAIMITO";
      case 70204: return "COLOSO";
      case 70215: return "COROZAL";
      case 70221: return "COVEÑAS";
      case 70230: return "CHALAN";
      case 70233: return "EL ROBLE";
      case 70235: return "GALERAS";
      case 70265: return "GUARANDA";
      case 70400: return "LA UNION";
      case 70418: return "LOS PALMITOS";
      case 70429: return "MAJAGUAL";
      case 70473: return "MORROA";
      case 70508: return "OVEJAS";
      case 70523: return "PALMITO";
      case 70670: return "SAMPUES";
      case 70678: return "SAN BENITO ABAD";
      case 70702: return "SAN JUAN DE BETULIA";
      case 70708: return "SAN MARCOS";
      case 70713: return "SAN ONOFRE";
      case 70717: return "SAN PEDRO";
      case 70742: return "SINCE";
      case 70771: return "SUCRE";
      case 70820: return "TOLU";
      case 70823: return "TOLUVIEJO";
      case 73001: return "IBAGUE";
      case 73024: return "ALPUJARRA";
      case 73026: return "ALVARADO";
      case 73030: return "AMBALEMA";
      case 73043: return "ANZOATEGUI";
      case 73055: return "ARMERO";
      case 73067: return "ATACO";
      case 73124: return "CAJAMARCA";
      case 73148: return "CARMEN DE APICALA";
      case 73152: return "CASABIANCA";
      case 73168: return "CHAPARRAL";
      case 73200: return "COELLO";
      case 73217: return "COYAIMA";
      case 73226: return "CUNDAY";
      case 73236: return "DOLORES";
      case 73268: return "ESPINAL";
      case 73270: return "FALAN";
      case 73275: return "FLANDES";
      case 73283: return "FRESNO";
      case 73319: return "GUAMO";
      case 73347: return "HERVEO";
      case 73349: return "HONDA";
      case 73352: return "ICONONZO";
      case 73408: return "LERIDA";
      case 73411: return "LIBANO";
      case 73443: return "MARIQUITA";
      case 73449: return "MELGAR";
      case 73461: return "MURILLO";
      case 73483: return "NATAGAIMA";
      case 73504: return "ORTEGA";
      case 73520: return "PALOCABILDO";
      case 73547: return "PIEDRAS";
      case 73555: return "PLANADAS";
      case 73563: return "PRADO";
      case 73585: return "PURIFICACION";
      case 73616: return "RIOBLANCO";
      case 73622: return "RONCESVALLES";
      case 73624: return "ROVIRA";
      case 73671: return "SALDAÑA";
      case 73675: return "SAN ANTONIO";
      case 73678: return "SAN LUIS";
      case 73686: return "SANTA ISABEL";
      case 73770: return "SUAREZ";
      case 73854: return "VALLE DE SAN JUAN";
      case 73861: return "VENADILLO";
      case 73870: return "VILLAHERMOSA";
      case 73873: return "VILLARRICA";
      case 76001: return "CALI";
      case 76020: return "ALCALA";
      case 76036: return "ANDALUCIA";
      case 76041: return "ANSERMANUEVO";
      case 76054: return "ARGELIA";
      case 76100: return "BOLIVAR";
      case 76109: return "BUENAVENTURA";
      case 76111: return "BUGA";
      case 76113: return "BUGALAGRANDE";
      case 76122: return "CAICEDONIA";
      case 76126: return "CALIMA(DARIEN)";
      case 76130: return "CANDELARIA";
      case 76147: return "CARTAGO";
      case 76233: return "DAGUA";
      case 76243: return "EL AGUILA";
      case 76246: return "EL CAIRO";
      case 76248: return "EL CERRITO";
      case 76250: return "EL DOVIO";
      case 76275: return "FLORIDA";
      case 76306: return "GINEBRA";
      case 76318: return "GUACARI";
      case 76364: return "JAMUNDI";
      case 76377: return "LA CUMBRE";
      case 76400: return "LA UNION";
      case 76403: return "LA VICTORIA";
      case 76497: return "OBANDO";
      case 76520: return "PALMIRA";
      case 76563: return "PRADERA";
      case 76606: return "RESTREPO";
      case 76616: return "RIOFRIO";
      case 76622: return "ROLDANILLO";
      case 76670: return "SAN PEDRO";
      case 76736: return "SEVILLA";
      case 76823: return "TORO";
      case 76828: return "TRUJILLO";
      case 76834: return "TULUA";
      case 76845: return "ULLOA";
      case 76863: return "VERSALLES";
      case 76869: return "VIJES";
      case 76890: return "YOTOCO";
      case 76892: return "YUMBO";
      case 76895: return "ZARZAL";
      case 81001: return "ARAUCA";
      case 81065: return "ARAUQUITA";
      case 81220: return "CRAVO NORTE";
      case 81300: return "FORTUL";
      case 81591: return "PUERTO RONDON";
      case 81736: return "SARAVENA";
      case 81794: return "TAME";
      case 85001: return "YOPAL";
      case 85010: return "AGUAZUL";
      case 85015: return "CHAMEZA";
      case 85125: return "HATO COROZAL";
      case 85136: return "LA SALINA";
      case 85139: return "MANI";
      case 85162: return "MONTERREY";
      case 85225: return "NUNCHIA";
      case 85230: return "OROCUE";
      case 85250: return "PAZ DE ARIPORO";
      case 85263: return "PORE";
      case 85279: return "RECETOR";
      case 85300: return "SABANALARGA";
      case 85315: return "SACAMA";
      case 85325: return "SAN LUIS DE PALENQUE";
      case 85400: return "TAMARA";
      case 85410: return "TAURAMENA";
      case 85430: return "TRINIDAD";
      case 85440: return "VILLA NUEVA";
      case 86001: return "MOCOA";
      case 86219: return "COLON";
      case 86320: return "ORITO";
      case 86568: return "PUERTO ASIS";
      case 86569: return "PUERTO CAICEDO(1992)";
      case 86571: return "PUERTO GUZMAN1992";
      case 86573: return "PUERTO LEGUIZAMO";
      case 86749: return "SIBUNDOY";
      case 86755: return "SAN FRANCISCO";
      case 86757: return "SAN MIGUEL";
      case 86760: return "SANTIAGO";
      case 86865: return "VALLE DEL GUAMUEZ";
      case 86885: return "VILLAGARZON";
      case 86999: return "SANTA CATALINA";
      case 88000: return "SANTA CATALINA";
      case 88001: return "SAN ANDRES";
      case 88564: return "PROVIDENCIA";
      case 91001: return "LETICIA";
      case 91263: return "EL ENCANTO";
      case 91405: return "LA CHORRERA";
      case 91407: return "LA PEDRERA";
      case 91430: return "LA VICTORIA";
      case 91460: return "MIRITI - PARANA";
      case 91530: return "PUERTO ALEGRIA";
      case 91536: return "PUERTO ARICA";
      case 91540: return "PUERTO NARIÑO";
      case 91669: return "PUERTO SANTANDER";
      case 91798: return "TARAPACA";
      case 94001: return "INIRIDA";
      case 94109: return "MORICHAL";
      case 94343: return "BARRANCO MINAS";
      case 94663: return "MAPIRIPANA";
      case 94883: return "SAN FELIPE";
      case 94884: return "PUERTO COLOMBIA";
      case 94885: return "LA GUADALUPE";
      case 94886: return "CACAHUAL";
      case 94887: return "PANA PANA";
      case 94888: return "MORICHAL";
      case 95001: return "SAN JOSE DEL GUAVIARE";
      case 95015: return "CALAMAR";
      case 95025: return "EL RETORNO";
      case 95200: return "MIRAFLORES";
      case 97001: return "MITU";
      case 97161: return "CARURU";
      case 97511: return "PACOA";
      case 97666: return "TARAIRA1992";
      case 97777: return "PAPUNAUA";
      case 97889: return "YAVARATE";
      case 99001: return "PUERTO CARREÑO";
      case 99264: return "SANTA ROSALIA";
      case 99524: return "LA PRIMAVERA";
      case 99624: return "SANTA ROSALIA";
      case 99666: return "SANTA ROSALIA";
      case 99760: return "SAN JOSE DE OCUNE";
      case 99773: return "CUMARIBO";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  PERIODO_IES_INICIO(TipoVariable.TV_IES,"IES: Primer periodo",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return AmbienteVariables.getInstance().getIdsSemestres();
    }
    public Byte getRango(Byte id) {
      return id;
    }
    public String toString(Byte val) {
      return (val==-1)?Constantes.S_DESCONOCIDO:CajaDeHerramientas.codigoSemestreToString(val);
    }
  }),
  PERIODO_IES_FIN(TipoVariable.TV_IES,"IES: Ultimo periodo",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return AmbienteVariables.getInstance().getIdsSemestres();
    }
    public Byte getRango(Byte id) {
      return id;
    }
    public String toString(Byte val) {
      return (val==-1)?Constantes.S_DESCONOCIDO:CajaDeHerramientas.codigoSemestreToString(val);
    }
  }),

  SEXO_EST(TipoVariable.TV_ESTUDIANTE,"Sexo",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,0,1};
    }
    public Byte getRango(Byte sexo) {
      return sexo;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  0: return "Masculino";
        case  1: return "Femenino";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  VIVIENDA_PROPIA_EST(TipoVariable.TV_ESTUDIANTE,"Vivienda propia",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,0,1};
    }
    public Byte getRango(Byte viviendaPropia) {
      return viviendaPropia;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  0: return "Carece";
        case  1: return "Posee";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  TRABAJABA_CUANDO_ICFES_EST(TipoVariable.TV_ESTUDIANTE,"Trabajaba al presentar el ICFES",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,0,1};
    }
    public Byte getRango(Byte trabajabaCuandoPresentoIcfes) {
      return trabajabaCuandoPresentoIcfes;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  0: return "No trabajaba";
        case  1: return "Si trabajaba";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  NIVEL_EDUCATIVO_MADRE_EST(TipoVariable.TV_ESTUDIANTE,"Nivel educativo de la madre",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,1,2,3,4};
    }
    public Byte getRango(Byte nivelEducativoMadre) {
      return nivelEducativoMadre;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  1: return "Básica primaria o inferior";
        case  2: return "Básica secundaria";
        case  3: return "Media vocacional o técnica/tecnológica";
        case  4: return "Universitaria o superior";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  NIVEL_SISBEN(TipoVariable.TV_ESTUDIANTE,"Nivel de SISBEN",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,1,2,3,4,5};
    }
    public Byte getRango(Byte nivel) {
      return nivel;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  1: return "Nivel 1";
        case  2: return "Nivel 2";
        case  3: return "Nivel 3";
        case  4: return "Está clasificado en otro nivel";
        case  5: return "No está clasificado por el SISBEN";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  INGRESO_HOGAR_EST(TipoVariable.TV_ESTUDIANTE,"Ingreso de la familia del estudiante [viejo]",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,0,1,2,3,4,5,6,7,8,9};
    }
    public Byte getRango(Byte ingresoHogar) {
      return ingresoHogar;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  0: return "[0,1) salarios mínimos";
        case  1: return "[1,2) salarios mínimos";
        case  2: return "[2,3) salarios mínimos";
        case  3: return "[3,5) salarios mínimos";
        case  4: return "[5,7) salarios mínimos";
        case  5: return "[7,9) salarios mínimos";
        case  6: return "[9,11) salarios mínimos";
        case  7: return "[11,13) salarios mínimos";
        case  8: return "[13,15) salarios mínimos";
        case  9: return "[15,) salarios mínimos";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  INGRESO_HOGAR_EST2(TipoVariable.TV_ESTUDIANTE,"Ingreso de la familia del estudiante",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,1,2,3,4,5,6,7};
    }
    public Byte getRango(Byte ingresoHogar) {
      return ingresoHogar;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  1: return "[0,1) salarios mínimos";
        case  2: return "[1,2) salarios mínimos";
        case  3: return "[2,3) salarios mínimos";
        case  4: return "[3,5) salarios mínimos";
        case  5: return "[5,7) salarios mínimos";
        case  6: return "[7,10) salarios mínimos";
        case  7: return "[10,) salarios mínimos";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  NUMERO_FAMILIARES_EST(TipoVariable.TV_ESTUDIANTE,"Número personas familia",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,1,2,3,4,5,6,7,8,9,10,11,12};
    }
    public Byte getRango(Byte ingresoHogar) {
      return ingresoHogar;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        default: return String.valueOf(val);
      }
    }
  }),
  NUMERO_HERMANOS_EST(TipoVariable.TV_ESTUDIANTE,"Número de hermanos",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,1,2,3,4,5,6};
    }
    public Byte getRango(Byte numeroHermanos) {
      return numeroHermanos>5?6:numeroHermanos;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  1: return "Ninguno";
        case  2: return "1";
        case  3: return "2";
        case  4: return "3";
        case  5: return "4";
        case  6: return "Más de 4";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  POSICION_ENTRE_LOS_HERMANOS_EST(TipoVariable.TV_ESTUDIANTE,"Posición entre sus hermanos",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,1,2,3,4,5,6};
    }
    public Byte getRango(Byte posicionEntreLosHermanos) {
      return posicionEntreLosHermanos>5?6:posicionEntreLosHermanos;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  1: return "Primero";
        case  2: return "Segundo";
        case  3: return "Tercero";
        case  4: return "Cuarto";
        case  5: return "Quinto";
        case  6: return "Posterior al quinto";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  CLASIFICACION_PUNTAJE_ICFES_EST(TipoVariable.TV_ESTUDIANTE,"Clasificación examen de estado",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,0,1,2};
    }
    public Byte getRango(Byte puntajeICFES) {
      //return (byte)(puntajeICFES<=0?-1:(puntajeICFES<66?0:(puntajeICFES<73?1:2)));
      //return (byte)(puntajeICFES<=0?-1:(puntajeICFES<46?0:(puntajeICFES<75?1:2)));
      return (byte)(puntajeICFES<=0?-1:(puntajeICFES<61?0:(puntajeICFES<91?1:2)));
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  0: return "Bajo";
        case  1: return "Medio";
        case  2: return "Alto";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  CLASIFICACION_PUNTAJE_ICFES_DECILES_EST(TipoVariable.TV_ESTUDIANTE,"Clasificación examen de estado2",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,0,1,2,3,4,5,6,7,8,9};
    }
    public Byte getRango(Byte puntajeICFES) {
      //return (byte)(puntajeICFES<=0?-1:(puntajeICFES<66?0:(puntajeICFES<73?1:2)));
      //return (byte)(puntajeICFES<=0?-1:(puntajeICFES<46?0:(puntajeICFES<75?1:2)));
      return (byte)(puntajeICFES<=0?-1:
          (puntajeICFES<51?
              (puntajeICFES<31?(puntajeICFES<21?(puntajeICFES<11?0:1):2):(puntajeICFES<41?3:4))
            :
              (puntajeICFES<81?(puntajeICFES<71?(puntajeICFES<61?5:6):7):(puntajeICFES<91?8:9))
          )
        );
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  0: return "Decil 1";
        case  1: return "Decil 2";
        case  2: return "Decil 3";
        case  3: return "Decil 4";
        case  4: return "Decil 5";
        case  5: return "Decil 6";
        case  6: return "Decil 7";
        case  7: return "Decil 8";
        case  8: return "Decil 9";
        case  9: return "Decil 10";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST(TipoVariable.TV_ESTUDIANTE,"Edad de presentación del ICFES",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,0,1,2,3};
    }
    public Byte getRango(Byte edadAlPresentarElICFES) {
      return (byte)(edadAlPresentarElICFES==-1?-1:(edadAlPresentarElICFES<16?0:(edadAlPresentarElICFES<21?1:(edadAlPresentarElICFES<26?2:3))));
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  0: return "15 o menos años";
        case  1: return "16 a 20 años";
        case  2: return "21 a 25 años";
        case  3: return "26 o más años";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  PROGRAMA_EST(TipoVariable.TV_ESTUDIANTE,"Programa académico",new Rango<String,String>() {
    public String[] getRango() {
      return AmbienteVariables.getInstance().getNombresProgramas();
    }
    public String getRango(String programa) {
      return programa;
    }
    public byte rangoToByte(String val) {
      return -1; // Este caso se maneja de manera especial en KernelSPADIES
    }
    public String byteToRango(byte b) {
      return ""; // Este caso se maneja de manera especial en KernelSPADIES
    }
    public String toString(String val) {
      return val.length()==0?Constantes.S_DESCONOCIDO:val;
    }
  }),
  AREA_CONOCIMIENTO_EST(TipoVariable.TV_ESTUDIANTE,"Área de conocimiento",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,1,2,3,4,5,6,/*7,*/8,9};
    }
    public Byte getRango(Byte area) {
      return area;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  1: return "Agronomía, veterinaria y afines";
        case  2: return "Bellas artes";
        case  3: return "Ciencias de la educación";
        case  4: return "Ciencias de la salud";
        case  5: return "Ciencias sociales y humanas";
        //case  5: return "Ciencias sociales, derecho, ciencias políticas";
        case  6: return "Economía, administración, contaduría y afines";
        //case  7: return "Humanidades y ciencias religiosas";
        case  8: return "Ingeniería, arquitectura, urbanismo y afines";
        case  9: return "Matemáticas y ciencias naturales";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  NUCLEO_CONOCIMIENTO_EST(TipoVariable.TV_ESTUDIANTE,"Núcleo básico de conocimiento",new RangoByte<Byte>() {
    public Byte[] getRango() {
      //return new Byte[]{-1,1,2,3,4,5,6,7,8,9,11,12,13,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,39,40,41,42,44,45,46,47,48,50,53,55,56,57,58,59,61,62,64,66,68,69,70};
      return new Byte[]{-1,9,1,53,18,4,5,40,55,34,56,57,12,58,59,7,11,13,41,68,35,61,62,36,21,22,23,24,20,19,25,26,27,28,29,30,31,32,44,64,37,45,3,8,46,47,48,33,70,66,6,39,50,69,42,2};
    }
    public Byte getRango(Byte area) {
      return area;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case 1: return "AGRONOMIA";
        case 2: return "ZOOTECNIA";
        case 3: return "MEDICINA VETERINARIA";
        case 4: return "ARTES PLASTICAS, VISUALES Y AFINES";
        case 5: return "ARTES REPRESENTATIVAS";
        case 6: return "PUBLICIDAD Y AFINES";
        case 7: return "DISEÑO";
        case 8: return "MUSICA";
        case 9: return "ADMINISTRACION";
        case 11: return "ECONOMIA";
        case 12: return "CONTADURIA PUBLICA";
        case 13: return "EDUCACION";
        case 18: return "ARQUITECTURA Y AFINES";
        case 19: return "INGENIERIA BIOMEDICA Y AFINES";
        case 20: return "INGENIERIA AMBIENTAL, SANITARIA Y AFINES";
        case 21: return "INGENIERIA ADMINISTRATIVA Y AFINES";
        case 22: return "INGENIERIA AGRICOLA, FORESTAL Y AFINES";
        case 23: return "INGENIERIA AGROINDUSTRIAL, ALIMENTOS Y AFINES.";
        case 24: return "INGENIERIA AGRONOMICA, PECUARIA Y AFINES";
        case 25: return "INGENIERIA CIVIL Y AFINES";
        case 26: return "INGENIERIA DE MINAS, METALURGIA Y AFINES";
        case 27: return "INGENIERIA DE SISTEMAS, TELEMATICA Y AFINES";
        case 28: return "INGENIERIA ELECTRICA Y AFINES";
        case 29: return "INGENIERIA ELECTRONICA, TELECOMUNICACIONES Y AFINES";
        case 30: return "INGENIERIA INDUSTRIAL Y AFINES";
        case 31: return "INGENIERIA MECANICA Y AFINES";
        case 32: return "INGENIERIA QUIMICA Y AFINES";
        case 33: return "OTRAS INGENIERIAS";
        case 34: return "BIOLOGIA, MICROBIOLOGIA Y AFINES";
        case 35: return "FISICA";
        case 36: return "GEOLOGIA, OTROS PROGRAMAS DE CIENCIAS NATURALES";
        case 37: return "MATEMATICAS, ESTADISTICA Y AFINES";
        case 39: return "QUIMICA Y AFINES";
        case 40: return "BACTERIOLOGIA";
        case 41: return "ENFERMERIA";
        case 42: return "TERAPIAS";
        case 44: return "INSTRUMENTACION QUIRURGICA";
        case 45: return "MEDICINA";
        case 46: return "NUTRICION Y DIETETICA";
        case 47: return "ODONTOLOGIA";
        case 48: return "OPTOMETRIA, OTROS PROGRAMAS DE CIENCIAS DE LA SALUD";
        case 50: return "SALUD PUBLICA";
        case 53: return "ANTROPOLOGIA, ARTES LIBERALES";
        case 55: return "BIBLIOTECOLOGIA,OTROS DE CIENCIAS SOCIALES Y HUMANAS";
        case 56: return "CIENCIA POLITICA, RELACIONES INTERNACIONALES";
        case 57: return "COMUNICACION SOCIAL, PERIODISMO Y AFINES";
        case 58: return "DEPORTES, EDUCACION FISICA Y RECREACION";
        case 59: return "DERECHO Y AFINES";
        case 61: return "FORMACION RELACIONADA CON EL CAMPO MILITAR O POLICIAL";
        case 62: return "GEOGRAFIA, HISTORIA";
        case 64: return "LENGUAS MODERNAS, LITERATURA, LINGUISTICA Y AFINES";
        case 66: return "PSICOLOGIA";
        case 68: return "FILOSOFIA, TEOLOGIA Y AFINES";
        case 69: return "SOCIOLOGIA, TRABAJO SOCIAL Y AFINES";
        case 70: return "OTROS PROGRAMAS ASOCIADOS A BELLAS ARTES";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  NIVEL_FORMACION_EST(TipoVariable.TV_ESTUDIANTE,"Nivel de formación",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,1,2,/*3,*/4,/*5,6,7,9*/};
    }
    public Byte getRango(Byte nivel) {
      return nivel;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  1: return "Técnica Profesional";
        case  2: return "Tecnológica";
        //case  3: return "Especialización Tecnológica";
        case  4: return "Universitaria";
        //case  5: return "Especialización";
        //case  6: return "Maestría";
        //case  7: return "Doctorado";
        //case  9: return "Especialización Técnica Profesional";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  METODOLOGIA_EST(TipoVariable.TV_ESTUDIANTE,"Metodología del programa",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,1,2,3};
    }
    public Byte getRango(Byte metodo) {
      return metodo;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  1: return "PRESENCIAL";
        case  2: return "A DISTANCIA (TRADICIONAL)";
        case  3: return "A DISTANCIA (VIRTUAL)";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  LICENCIATURA_EST(TipoVariable.TV_ESTUDIANTE,"Licenciatura",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,0,1};
    }
    public Byte getRango(Byte area) {
      return area;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  0: return "No es licenciatura";
        case  1: return "Es licenciatura";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  LENGUAS_EST(TipoVariable.TV_ESTUDIANTE,"Lenguaje",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,0,1};
    }
    public Byte getRango(Byte area) {
      return area;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  0: return "No es lenguaje";
        case  1: return "Es lenguaje";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  NUMERO_APOYOS_ICETEX_RECIBIDOS_EST(TipoVariable.TV_ESTUDIANTE,"# de créditos recibidos del ICETEX",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0,1,2,3,4};
    }
    public Byte getRango(Byte cantidad) {
      return cantidad<4?cantidad:4;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "Ninguno";
        case  1: return "1";
        case  2: return "2";
        case  3: return "3";
        case  4: return "4 o más";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  NUMERO_APOYOS_ICETEX_LARGO_RECIBIDOS_EST(TipoVariable.TV_ESTUDIANTE,"# de créditos ICETEX - Largo plazo",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0,1,2,3,4};
    }
    public Byte getRango(Byte cantidad) {
      return cantidad<4?cantidad:4;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "Ninguno";
        case  1: return "1";
        case  2: return "2";
        case  3: return "3";
        case  4: return "4 o más";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  NUMERO_APOYOS_ICETEX_MEDIO_RECIBIDOS_EST(TipoVariable.TV_ESTUDIANTE,"# de créditos ICETEX - Mediano plazo",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0,1,2,3,4};
    }
    public Byte getRango(Byte cantidad) {
      return cantidad<4?cantidad:4;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "Ninguno";
        case  1: return "1";
        case  2: return "2";
        case  3: return "3";
        case  4: return "4 o más";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  NUMERO_APOYOS_ICETEX_ACCES_RECIBIDOS_EST(TipoVariable.TV_ESTUDIANTE,"# de créditos ICETEX - ACCES",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0,1,2,3,4};
    }
    public Byte getRango(Byte cantidad) {
      return cantidad<4?cantidad:4;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "Ninguno";
        case  1: return "1";
        case  2: return "2";
        case  3: return "3";
        case  4: return "4 o más";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  NUMERO_APOYOS_ACADEMICOS_RECIBIDOS_EST(TipoVariable.TV_ESTUDIANTE,"# de apoyos académicos recibidos de la IES",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0,1,2,3,4};
    }
    public Byte getRango(Byte cantidad) {
      return cantidad<4?cantidad:4;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "Ninguno";
        case  1: return "1";
        case  2: return "2";
        case  3: return "3";
        case  4: return "4 o más";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  NUMERO_APOYOS_FINANCIEROS_RECIBIDOS_EST(TipoVariable.TV_ESTUDIANTE,"# de apoyos financieros recibidos de la IES",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0,1,2,3,4};
    }
    public Byte getRango(Byte cantidad) {
      return cantidad<4?cantidad:4;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "Ninguno";
        case  1: return "1";
        case  2: return "2";
        case  3: return "3";
        case  4: return "4 o más";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  NUMERO_APOYOS_OTROS_RECIBIDOS_EST(TipoVariable.TV_ESTUDIANTE,"# de otros apoyos recibidos de la IES",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0,1,2,3,4};
    }
    public Byte getRango(Byte cantidad) {
      return cantidad<4?cantidad:4;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "Ninguno";
        case  1: return "1";
        case  2: return "2";
        case  3: return "3";
        case  4: return "4 o más";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  PERIODO_INGRESO_EST(TipoVariable.TV_ESTUDIANTE,"Período de ingreso",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return AmbienteVariables.getInstance().getIdsSemestres();
    }
    public Byte getRango(Byte id) {
      return id;
    }
    public String toString(Byte val) {
      return (val==-1)?Constantes.S_DESCONOCIDO:CajaDeHerramientas.codigoSemestreToString(val);
    }
  }),
  PERIODO_GRADO_EST(TipoVariable.TV_ESTUDIANTE,"Período de grado",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return AmbienteVariables.getInstance().getIdsSemestres();
    }
    public Byte getRango(Byte id) {
      return id;
    }
    public String toString(Byte val) {
      return (val==-1)?Constantes.S_DESCONOCIDO:CajaDeHerramientas.codigoSemestreToString(val);
    }
  }),
  ULTIMO_PERIODO_MATRICULADO_EST(TipoVariable.TV_ESTUDIANTE,"Ultimo período matriculado",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return AmbienteVariables.getInstance().getIdsSemestres();
    }
    public Byte getRango(Byte id) {
      return id;
    }
    public String toString(Byte val) {
      return (val==-1)?Constantes.S_DESCONOCIDO:CajaDeHerramientas.codigoSemestreToString(val);
    }
  }),
  CLASIFICACION_ESTADO_EST(TipoVariable.TV_ESTUDIANTE,"Estado",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,0,1,2};
    }
    public Byte getRango(Byte estado) {
      return estado;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return "Desertor";
        case  0: return "Activo";
        case  1: return "Graduado";
        //case  2: return "Retirado por disciplina";
        case  2: return "Retirado disciplinariamente";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  CLASIFICACION_ESTADO_EST_DETALLADO(TipoVariable.TV_ESTUDIANTE,"Estado",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-3,-2,-1,0,1,2};
    }
    public Byte getRango(Byte estado) {
      return estado;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -3: return "Desertor Programa";
        case -2: return "Desertor IES";
        case -1: return "Desertor Sistema";
        case  0: return "Activo";
        case  1: return "Graduado";
        case  2: return "Retirado disciplinariamente";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  MODELO_CALCULABLE(TipoVariable.TV_ESTUDIANTE,"Datos para modelo",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0, 1};
    }
    public Byte getRango(Byte estado) {
      return estado;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "No";
        case  1: return "Si";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  //Modas
  M_VIVIENDA_PROPIA_EST(TipoVariable.TV_ESTUDIANTE,"Vivienda propia",VIVIENDA_PROPIA_EST.rango),
  M_TRABAJABA_CUANDO_ICFES_EST(TipoVariable.TV_ESTUDIANTE,"Trabajaba al presentar el ICFES",TRABAJABA_CUANDO_ICFES_EST.rango),
  M_NIVEL_EDUCATIVO_MADRE_EST(TipoVariable.TV_ESTUDIANTE,"Nivel educativo de la madre",NIVEL_EDUCATIVO_MADRE_EST.rango),
  M_INGRESO_HOGAR_EST(TipoVariable.TV_ESTUDIANTE,"Ingreso de la familia del estudiante",INGRESO_HOGAR_EST.rango),
  M_NUMERO_HERMANOS_EST(TipoVariable.TV_ESTUDIANTE,"Número de hermanos",NUMERO_HERMANOS_EST.rango),
  M_POSICION_ENTRE_LOS_HERMANOS_EST(TipoVariable.TV_ESTUDIANTE,"Posición entre sus hermanos",POSICION_ENTRE_LOS_HERMANOS_EST.rango),
  M_ESTRATO(TipoVariable.TV_ESTUDIANTE,"Estrato",ESTRATO.rango),
  M_NIVEL_SISBEN(TipoVariable.TV_ESTUDIANTE,"Nivel de SISBEN",NIVEL_SISBEN.rango),
  M_NUMERO_FAMILIARES_EST(TipoVariable.TV_ESTUDIANTE,"Número personas familia",NUMERO_FAMILIARES_EST.rango),
  M_INGRESO_HOGAR_EST2(TipoVariable.TV_ESTUDIANTE,"Ingreso de la familia del estudiante",INGRESO_HOGAR_EST2.rango),
  
  VARIABLE_EXTRA_1_EST(TipoVariable.TV_ESTUDIANTE,"",new RangoVariableExtra(0,false)),
  VARIABLE_EXTRA_2_EST(TipoVariable.TV_ESTUDIANTE,"",new RangoVariableExtra(1,false)),
  VARIABLE_EXTRA_3_EST(TipoVariable.TV_ESTUDIANTE,"",new RangoVariableExtra(2,false)),
  VARIABLE_EXTRA_4_EST(TipoVariable.TV_ESTUDIANTE,"",new RangoVariableExtra(3,false)),
  VARIABLE_EXTRA_5_EST(TipoVariable.TV_ESTUDIANTE,"",new RangoVariableExtra(4,false)),
  VARIABLE_EXTRA_1_PEST(TipoVariable.TV_PERIODO_ESTUDIANTE,"",new RangoVariableExtra(0,true)),
  VARIABLE_EXTRA_2_PEST(TipoVariable.TV_PERIODO_ESTUDIANTE,"",new RangoVariableExtra(1,true)),
  VARIABLE_EXTRA_3_PEST(TipoVariable.TV_PERIODO_ESTUDIANTE,"",new RangoVariableExtra(2,true)),
  VARIABLE_EXTRA_4_PEST(TipoVariable.TV_PERIODO_ESTUDIANTE,"",new RangoVariableExtra(3,true)),
  VARIABLE_EXTRA_5_PEST(TipoVariable.TV_PERIODO_ESTUDIANTE,"",new RangoVariableExtra(4,true)),
  PERIODO_MATRICULADO_PER(TipoVariable.TV_PERIODO_ESTUDIANTE,"Período",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return AmbienteVariables.getInstance().getIdsSemestres();
    }
    public Byte getRango(Byte id) {
      return id;
    }
    public String toString(Byte val) {
      return (val==-1)?Constantes.S_DESCONOCIDO:CajaDeHerramientas.codigoSemestreToString(val);
    }
  }),
  NUMERO_SEMESTRE_PER(TipoVariable.TV_PERIODO_ESTUDIANTE,"Número semestre cursado",new RangoByte<Byte>() {
    public Byte[] getRango() {
      Byte[] idsSemestres=AmbienteVariables.getInstance().getIdsSemestres();
      int limInf=Integer.MAX_VALUE,limSup=Integer.MIN_VALUE;
      for (byte b:idsSemestres) if (b!=-1) {
        limInf=Math.min(limInf,b);
        limSup=Math.max(limSup,b);
      }
      if (limInf==Integer.MAX_VALUE) return new Byte[]{-1};
      int tam=limSup-limInf+1;
      Byte[] res=new Byte[tam+1];
      res[0]=-1;
      for (int i=0; i<tam; i++) res[i+1]=(byte)(i+1);
      return res;
    }
    public Byte getRango(Byte id) {
      return id;
    }
    public String toString(Byte val) {
      return (val==-1)?Constantes.S_DESCONOCIDO:(""+((int)val));
    }
  }),
  APOYO_FINANCIERO_PER(TipoVariable.TV_PERIODO_ESTUDIANTE,"Apoyo financiero",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0,1};
    }
    public Byte getRango(Byte tiene) {
      return tiene;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "No tiene";
        case  1: return "Tiene";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  APOYO_ACADEMICO_PER(TipoVariable.TV_PERIODO_ESTUDIANTE,"Apoyo académico",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0,1};
    }
    public Byte getRango(Byte tiene) {
      return tiene;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "No tiene";
        case  1: return "Tiene";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  APOYO_OTRO_PER(TipoVariable.TV_PERIODO_ESTUDIANTE,"Apoyo de otro tipo",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0,1};
    }
    public Byte getRango(Byte tiene) {
      return tiene;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "No tiene";
        case  1: return "Tiene";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  TIPO_ICETEX_PER(TipoVariable.TV_PERIODO_ESTUDIANTE,"Créditos recibidos del ICETEX",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0,1,2,3};
    }
    public Byte getRango(Byte tipo) {
      return tipo;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "No tiene";
        case  1: return "Largo plazo";
        case  2: return "Mediano plazo";
        case  3: return "ACCES";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  REPITENCIA_PER(TipoVariable.TV_PERIODO_ESTUDIANTE,"Tasa de repitencia",new RangoByte<Double>() {
    public Byte[] getRango() {
      return new Byte[]{-1,0,1,2,3,4,5,6,7,8,9,10};
    }
    public Byte getRango(Double repitencia) {
      return (byte)((repitencia==-1d)?-1:(Math.floor(repitencia*10)));
    }
    public String toString(Byte val) {
      if (val==-1) return Constantes.S_DESCONOCIDO;
      if (val==10) return "100%";
      return "["+(val*10)+","+((val+1)*10)+")%";
    }
  }),
  NIVEL_APROBACION_PER(TipoVariable.TV_PERIODO_ESTUDIANTE,"Nivel de aprobación",new RangoByte<int[]>() {
    public Byte[] getRango() {
      return new Byte[]{-1,0,1,2,3,4,5,6,7,8,9,10};
    }
    public Byte getRango(int[] materias) {
      int materiasTomadas=materias[0],materiasAprobadas=materias[1];
      if (materiasTomadas<=0 || materiasAprobadas<0) return (byte)(-1);
      return (byte)Math.min(Math.floor(10d*materiasAprobadas/materiasTomadas),10);
    }
    public String toString(Byte val) {
      if (val==-1) return Constantes.S_DESCONOCIDO;
      if (val==10) return "100%";
      return "["+(val*10)+","+((val+1)*10)+")%";
    }
  }),
  CLASIFICACION_RIESGO_PER(TipoVariable.TV_PERIODO_ESTUDIANTE,"Clasificación riesgo",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,1,2,3,4,5};
    }
    public Byte getRango(Byte claseRiesgo) {
      return claseRiesgo;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case -1: return Constantes.S_DESCONOCIDO;
        case  1: return "Bajo";
        case  2: return "Medio bajo";
        case  3: return "Medio";
        case  4: return "Medio alto";
        case  5: return "Alto";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  ICETEX_RECIBIDO_PER(TipoVariable.TV_PERIODO_ESTUDIANTE,"Recibió/no recibió apoyo del ICETEX",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0,1};
    }
    public Byte getRango(Byte recibio) {
      return recibio;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "No recibió";
        case  1: return "Recibió";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  TIPO_ICETEX_RECIBIDO_PER(TipoVariable.TV_PERIODO_ESTUDIANTE,"Tipo de crédito ICETEX recibido",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{1,2,3};
    }
    public Byte getRango(Byte tipo) {
      return tipo;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  1: return "Largo plazo";
        case  2: return "Mediano plazo";
        case  3: return "ACCES";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  APOYO_RECIBIDO_PER(TipoVariable.TV_PERIODO_ESTUDIANTE,"Recibió/no recibió apoyo de la IES",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{0,1};
    }
    public Byte getRango(Byte recibio) {
      return recibio;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
        case  0: return "No recibió";
        case  1: return "Recibió";
      }
      return Constantes.S_DESCONOCIDO;
    }
  }),
  PERIODO_ICFES_PRESENTA(TipoVariable.TV_ESTUDIANTE,"ICFES: Periodo presentación",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40};
    }
    public Byte getRango(Byte id) {
      return id;
    }
    public String toString(Byte val) {
      return (val==-1)?Constantes.S_DESCONOCIDO:(val+"");//CajaDeHerramientas.codigoSemestreToString((byte) (val==-1?-1:val-16));
    }
  }),
  PERIODO_ICFES_DATOS(TipoVariable.TV_ESTUDIANTE,"ICFES: Periodo datos",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40};
    }
    public Byte getRango(Byte id) {
      return id;
    }
    public String toString(Byte val) {
      return (val==-1)?Constantes.S_DESCONOCIDO:(val+"");//CajaDeHerramientas.codigoSemestreToString((byte) (val==-1?-1:val-16));
    }
  }),
  REMPLAZO_ICFES(TipoVariable.TV_ESTUDIANTE,"ICFES: Remplazo",new RangoByte<Byte>() {
    public Byte[] getRango() {
      return new Byte[]{-1,0,1};
    }
    public Byte getRango(Byte id) {
      return id;
    }
    public String toString(Byte val) {
      switch (val.byteValue()) {
      case  0: return "No";
      case  1: return "Si";
    }
    return Constantes.S_DESCONOCIDO;
    }
  }),
  AUX_INDPROG(TipoVariable.TV_ESTUDIANTE,"ICFES: Remplazo",new Rango<Integer,Integer>() {
    @Override
    public byte rangoToByte(Integer u) {
      throw new Error("AUX_INDPROG: Operacion no soportada");
    }
    @Override
    public Integer byteToRango(byte b) {
      throw new Error("AUX_INDPROG: Operacion no soportada");
    }
    @Override
    public String toString(Integer u) {
      throw new Error("AUX_INDPROG: Operacion no soportada");
    }
    @Override
    public Integer[] getRango() {
      //throw new Error("AUX_INDPROG: Operacion no soportada");
      return new Integer[0];
    }
    @Override
    public Integer getRango(Integer t) {
      return t;
    }
  }),;

  public static final Variable[] varsExtras={VARIABLE_EXTRA_1_EST,VARIABLE_EXTRA_2_EST,VARIABLE_EXTRA_3_EST,VARIABLE_EXTRA_4_EST,VARIABLE_EXTRA_5_EST};
  public static final Variable[] varsExtrasD={VARIABLE_EXTRA_1_PEST,VARIABLE_EXTRA_2_PEST,VARIABLE_EXTRA_3_PEST,VARIABLE_EXTRA_4_PEST,VARIABLE_EXTRA_5_PEST};
  public final TipoVariable tipo;
  public String nombre;
  public final Rango rango;
  public Item[] items;
  private Variable(TipoVariable pTipo, String pNombre, Rango pRango) {
    tipo=pTipo;
    nombre=pNombre;
    rango=pRango;
    generarItems();
  }
  @SuppressWarnings("unchecked")
  public void generarItems() {
    List<Item> listaItems=new ArrayList<Item>(10);
    for (Comparable key:rango.getRango()) listaItems.add(new Item(key,rango.toString(key),rango.toStringHTML(key)));
    items=listaItems.toArray(new Item[0]);
  }
  public static Filtro[] filtrarFiltros(Filtro[] filtros, TipoVariable tipo) {
    List<Filtro> listaFiltros=new ArrayList<Filtro>(10);
    for (Filtro ft:filtros) if (ft.variable.tipo==tipo) listaFiltros.add(ft);
    return listaFiltros.toArray(new Filtro[0]);
  }
  public Object getValor(Object...args) {
    switch (tipo) {
      case TV_IES:
        {
          // args: ies
          IES ies=(IES)(args[0]);
          switch (this) {
            case CODIGO_IES: return ies.codigo;
            case INT_BLOQUEADA_IES: return (byte)(ConstantesInternas.ies_bloqueadas.contains(ies.codigo)?1:2);
            case INT_CLASE_IES: return (byte)(ConstantesInternas.ies_tipo_a.contains(ies.codigo)?1:(ConstantesInternas.ies_tipo_b.contains(ies.codigo)?2:ConstantesInternas.ies_tipo_c.contains(ies.codigo)?3:-1));
            //case ORIGEN_IES: return ies.origen;
            case ORIGEN_IES: return (ies.origen==7||ies.origen==8?(byte)2:(byte)1);
            case CARACTER_IES: return ies.caracter==5?(byte)2:(ies.caracter==9?(byte)-1:ies.caracter);
            case DEPARTAMENTO_IES: return ies.departamento;
            //case MUNICIPIO_IES: return new int[]{ies.departamento,ies.municipio};
            case PERIODO_IES_INICIO: return (byte)ies.minCodigoSemestre;
            case PERIODO_IES_FIN: return (byte)ies.maxCodigoSemestre;
          }
        }
        break;
      case TV_ESTUDIANTE:
        {
          // args: estudiante, ies
          Estudiante e=(Estudiante)(args[0]);
          IES ies=(IES)(args[1]);
          switch (this) {
            case SEXO_EST: return e.getSexo();
            case VIVIENDA_PROPIA_EST: return e.getViviendaPropia();
            case TRABAJABA_CUANDO_ICFES_EST: return e.getTrabajabaCuandoPresentoIcfes();
            case NIVEL_EDUCATIVO_MADRE_EST: return e.getNivelEducativoMadre();
            case INGRESO_HOGAR_EST: return e.getIngresoHogar();
            case NUMERO_HERMANOS_EST: return e.getNumeroHermanos();
            case POSICION_ENTRE_LOS_HERMANOS_EST: return e.getPosicionEntreLosHermanos();
            case CLASIFICACION_PUNTAJE_ICFES_EST: return e.getPuntajeICFES();
            case CLASIFICACION_PUNTAJE_ICFES_DECILES_EST: return e.getPuntajeICFES();
            //TODO MCHT_ Alterar pertinente
            /*
            case CLASIFICACION_PUNTAJE_ICFES_EST: {
              String per = ies.semestres[e.getSemestrePrimiparo()];
              byte pun = e.getPuntajeICFES();
              if (pun<=0) return (byte)0;
              if (per.compareTo("20001")<=0) return pun;
              if (per.compareTo("20031")<=0) return (byte)(int)Math.round(8d*(pun-43)/3d+47d);
              if (per.compareTo("20051")<=0) return (byte)(int)Math.round(8d*(pun-48)/3d+47d);
              return (byte)(int)Math.round(8d*(pun-44)/5d+47d);
            }*/
            case CLASIFICACION_EDAD_AL_PRESENTAR_EL_ICFES_EST: return e.getEdadAlPresentarElICFES();
            case ESTRATO: return e.getEstrato();
            case NIVEL_SISBEN: return e.getNivelSisben();
            case INGRESO_HOGAR_EST2: return e.getIngresoHogar2();
            case NUMERO_FAMILIARES_EST: return e.getPersonasFamilia();
            //Modas
            case M_VIVIENDA_PROPIA_EST: return e.getmViviendaPropia();
            case M_TRABAJABA_CUANDO_ICFES_EST: return e.getmTrabajabaCuandoPresentoIcfes();
            case M_NIVEL_EDUCATIVO_MADRE_EST: return e.getmNivelEducativoMadre();
            case M_INGRESO_HOGAR_EST: return e.getmIngresoHogar();
            case M_NUMERO_HERMANOS_EST: return e.getmNumeroHermanos();
            case M_POSICION_ENTRE_LOS_HERMANOS_EST: return e.getmPosicionEntreLosHermanos();
            case M_ESTRATO: return e.getmEstrato();
            case M_NIVEL_SISBEN: return e.getmNivelSisben();
            case M_NUMERO_FAMILIARES_EST: return e.getmPersonasFamilia();
            case M_INGRESO_HOGAR_EST2: return e.getmIngresoHogar2();

            case PROGRAMA_EST:
              {
                int indProg=e.getIndicePrograma();
                return (indProg==-1)?"":ies.programas[indProg].toString();
              }
            case AREA_CONOCIMIENTO_EST:
              {
                int indProg=e.getIndicePrograma();
                return (byte)((indProg==-1)?-1:(ies.programas[indProg].area==7?5:ies.programas[indProg].area));
              }
            case NUCLEO_CONOCIMIENTO_EST:
            {
              int indProg=e.getIndicePrograma();
              return (byte)((indProg==-1)?-1:ies.programas[indProg].nucleo);
            }
            case NIVEL_FORMACION_EST:
            {
              int indProg=e.getIndicePrograma();
              //return (byte)((indProg==-1)?-1:ies.programas[indProg].nivel);
              return (byte)((indProg==-1||Arrays.asList((byte)3,(byte)5,(byte)6,(byte)7,(byte)9).contains(ies.programas[indProg].nivel))?-1:ies.programas[indProg].nivel);
            }
            case METODOLOGIA_EST:
            {
              int indProg=e.getIndicePrograma();
              //return (byte)((indProg==-1)?-1:ies.programas[indProg].nivel);
              return (byte)indProg==-1?-1:ies.programas[indProg].metodologia;
            }
            case LICENCIATURA_EST:
            {
              int indProg=e.getIndicePrograma();
              String nomp = indProg==-1?"":new String(ies.programas[indProg].nombre);
              return (byte)(indProg==-1?-1:(nomp.startsWith("LIC ") || nomp.startsWith("LICECIATURA ") || nomp.startsWith("LIC ") || nomp.startsWith("LICEN ") || nomp.startsWith("LICENCIADO ") || nomp.startsWith("LICENCIATURA ")?1:0));
            }
            case LENGUAS_EST:
            {
              int indProg=e.getIndicePrograma();
              String nomp = indProg==-1?"":new String(ies.programas[indProg].nombre);
              return (byte)(indProg==-1?-1:(nomp.contains("BILINGUE") || nomp.contains("INGLES") || nomp.contains("CASTELLANA") || nomp.contains("LENGUA") || nomp.contains("IDIOMA")?1:0));
            }
            case NUMERO_APOYOS_ICETEX_RECIBIDOS_EST:
              {
                int r=0,jI=e.getSemestrePrimiparo(),jT=ies.n;
                if (jI!=-1) for (int j=jI; j<jT; j++) if (e.getTipoApoyoICETEXRecibido(j)!=0) r++;
                return (byte)r;
              }
            case NUMERO_APOYOS_ICETEX_ACCES_RECIBIDOS_EST:
              {
                int r=0,jI=e.getSemestrePrimiparo();
                long matri=e.getSemestresMatriculadoAlDerecho();
                for (int j=jI,jT=ies.n; j<jT; j++,matri>>>=1)
                  if ((matri&1L)==1L && e.getTipoApoyoICETEXRecibido(j)==3) r++;
                return (byte)r;
              }
            case NUMERO_APOYOS_ICETEX_LARGO_RECIBIDOS_EST:
              {
                int r=0,jI=e.getSemestrePrimiparo(),jT=ies.n;
                if (jI!=-1) for (int j=jI; j<jT; j++) if (e.getTipoApoyoICETEXRecibido(j)==1) r++;
                return (byte)r;
              }
            case NUMERO_APOYOS_ICETEX_MEDIO_RECIBIDOS_EST:
              {
                int r=0,jI=e.getSemestrePrimiparo(),jT=ies.n;
                if (jI!=-1) for (int j=jI; j<jT; j++) if (e.getTipoApoyoICETEXRecibido(j)==2) r++;
                return (byte)r;
              }
            case NUMERO_APOYOS_ACADEMICOS_RECIBIDOS_EST:
              {
                int r=0,jI=e.getSemestrePrimiparo(),jT=ies.n;
                if (jI!=-1) for (int j=jI; j<jT; j++) if (e.getRecibioApoyoAcademico(j)) r++;
                return (byte)r;
              }
            case NUMERO_APOYOS_FINANCIEROS_RECIBIDOS_EST:
              {
                int r=0,jI=e.getSemestrePrimiparo(),jT=ies.n;
                if (jI!=-1) for (int j=jI; j<jT; j++) if (e.getRecibioApoyoFinanciero(j)) r++;
                return (byte)r;
              }
            case NUMERO_APOYOS_OTROS_RECIBIDOS_EST:
              {
                int r=0,jI=e.getSemestrePrimiparo(),jT=ies.n;
                if (jI!=-1) for (int j=jI; j<jT; j++) if (e.getRecibioApoyoOtro(j)) r++;
                return (byte)r;
              }
            case PERIODO_INGRESO_EST:
              {
                int per=e.getSemestrePrimiparo();
                return (byte)((per==-1)?-1:(ies.minCodigoSemestre+per));
              }
            case PERIODO_GRADO_EST:
              {
                int per=e.getSemestreGrado();
                return (byte)((per==-1)?-1:(ies.minCodigoSemestre+per));
              }
            case ULTIMO_PERIODO_MATRICULADO_EST:
            {
              int per=e.getUltimoSemestreMatriculado();
              return (byte)((per==-1)?-1:(ies.minCodigoSemestre+per));
            }
            //case CLASIFICACION_ESTADO_EST: return e.getEstado();
            case CLASIFICACION_ESTADO_EST: return e.getEstado();
            case CLASIFICACION_ESTADO_EST_DETALLADO: return e.getEstadoExtra();
            case MODELO_CALCULABLE: return e.tieneInformacionModeloEstructural();
            case PERIODO_ICFES_PRESENTA: return (byte)e.getPerIcfes();
            case PERIODO_ICFES_DATOS: return (byte)e.getPerDatIcfes();
            case REMPLAZO_ICFES: return (byte)e.getRemplazoICFES();
            case AUX_INDPROG: return e.getIndicePrograma();
            case VARIABLE_EXTRA_1_EST: return e.getValorVariableExtra(0);
            case VARIABLE_EXTRA_2_EST: return e.getValorVariableExtra(1);
            case VARIABLE_EXTRA_3_EST: return e.getValorVariableExtra(2);
            case VARIABLE_EXTRA_4_EST: return e.getValorVariableExtra(3);
            case VARIABLE_EXTRA_5_EST: return e.getValorVariableExtra(4);
          }
        }
        break;
      case TV_PERIODO_ESTUDIANTE:
        {
          // args: estudiante, ies, índice semestre, repitencias
          Estudiante e=(Estudiante)(args[0]);
          int sem=(Integer)(args[2]);
          switch (this) {
            case PERIODO_MATRICULADO_PER: return (byte)(((IES)(args[1])).minCodigoSemestre+sem);
            case NUMERO_SEMESTRE_PER:
              {
                int jI=e.getSemestrePrimiparo();
                return (byte)((jI==-1 || sem<jI)?-1:(sem-jI+1));
              }
            case APOYO_FINANCIERO_PER: return (byte)(e.getRecibioApoyoFinanciero(sem)?1:0);
            case APOYO_ACADEMICO_PER: return (byte)(e.getRecibioApoyoAcademico(sem)?1:0);
            case APOYO_OTRO_PER: return (byte)(e.getRecibioApoyoOtro(sem)?1:0);
            case TIPO_ICETEX_PER: return e.getTipoApoyoICETEXRecibido(sem);
            case REPITENCIA_PER: return ((double[])(args[3]))[sem];
            case NIVEL_APROBACION_PER: return new int[]{e.getNumeroMateriasTomadas(sem),e.getNumeroMateriasAprobadas(sem)};
            case CLASIFICACION_RIESGO_PER: return e.getClaseRiesgo(sem);
            case ICETEX_RECIBIDO_PER: return (byte)(e.getTipoApoyoICETEXRecibido(sem)!=0?1:0);
            case TIPO_ICETEX_RECIBIDO_PER: return e.getTipoApoyoICETEXRecibido(sem); // Cuidado con el valor 0
            case APOYO_RECIBIDO_PER: return (byte)(e.getRecibioApoyoIES(sem)?1:0);
            case VARIABLE_EXTRA_1_PEST: return e.getValorVariableExtra(0, sem);
            case VARIABLE_EXTRA_2_PEST: return e.getValorVariableExtra(1, sem);
            case VARIABLE_EXTRA_3_PEST: return e.getValorVariableExtra(2, sem);
            case VARIABLE_EXTRA_4_PEST: return e.getValorVariableExtra(3, sem);
            case VARIABLE_EXTRA_5_PEST: return e.getValorVariableExtra(4, sem);
          }
        }
        break;
    }
    return null;
  }
  @SuppressWarnings("unchecked")
  public static String toString(Variable v, byte b, Integer[] codigosIESDif, String[] codigosProgramasDif) {
    switch (v) {
      case CODIGO_IES: return codigosIESDif[b&0xFF].toString();
      case PROGRAMA_EST: return codigosProgramasDif[b&0xFF];
      default: return v.rango.toString(v.rango.byteToRango(b));
    }
  }
  public boolean esVariableExtra() {
    for (Variable v:varsExtras) if (this==v) return true;
    for (Variable v:varsExtrasD) if (this==v) return true;
    return false;
  }
}
