package spadies.tools;

/**
 * Esta clase esta basada en la clase DiagnosticoIES.java
 * La diferencia radica es que en esta clase se produce todo .docx en vez de 
 * generar archivos .html. Sin embargo tiene las modificaciones solicitadas por Martha el 10 de Junio del 2011
 */
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import com.javadocx.*;

import spadies.gui.format.ProcesadorConsultaDesercion;
import spadies.gui.format.ProcesadorDesercionPorPeriodo;
import spadies.gui.format.ResultadoConsulta;
import spadies.gui.util.InfoTabla;
import spadies.kernel.Estudiante;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;
import spadies.kernel.Programa;
import spadies.util.Constantes;
import spadies.util.MyException;
import spadies.util.variables.Filtro;
import spadies.util.variables.Item;
import spadies.util.variables.Variable;

public class DiagnosticoIESWord {

	private enum PruebaIES {
		SINCRONIZACION_VACIA("Sincronización vacia", new String[] { "" }) {
			private static final int umbral = 2;

			public boolean diagnosticar(IES ies, CreateDocx docx) {
				boolean res = false;
				Filtro fil = new Filtro(Variable.CODIGO_IES,
						new Item[] { new Item(ies.codigo, "", "") });
				int[] conteoCohorte = new int[ies.n];
				for (Estudiante est : ies.estudiantes)
					conteoCohorte[est.getSemestrePrimiparo()]++;
				for (int conteo : conteoCohorte)
					if (conteo >= umbral)
						res = true;
				String mensaje = new String();
				adicionarTexto(docx,
						"I. Características generales de la información");
				mensaje = "a. Sincronización de prueba: ";
				mensaje += res ? textoBien("No es una sincronización de prueba porque hay periodos con estudiantes. ")
						: textoError("Es una sincronización de prueba, ninguno de los periodos tiene estudiantes. ");
				adicionarTexto(docx, mensaje);
				mensaje = "Información sincronizada actualmente: la fecha de la última sincronización realizada por su institución es XXXXXX";
				adicionarTexto(docx, mensaje);
				mensaje = "INVENTARIO";
				adicionarTexto(docx, mensaje);
				Object[] informacionTabla = kernel
						.getTablaCantidadArchivos(new Filtro[] { fil });
				String[][] valores = (String[][]) (informacionTabla[0]);
				String[][] encabezadoFilas = (String[][]) (informacionTabla[1]);
				String[] encabezadoColumnas = (String[]) (informacionTabla[2]);
				ArrayList<ArrayList> valuesTable = new ArrayList<ArrayList>();
				ArrayList<String> values = new ArrayList<String>();
				values.add("");
				for (int i = 0; i < encabezadoColumnas.length; i++) {
					values.add(encabezadoColumnas[i]);
				}
				valuesTable.add(values);
				for (int i = 0; i < valores.length; i++) {
					values = new ArrayList<String>();
					values.add(encabezadoFilas[0][i]);
					for (int j = 0; j < valores[0].length; j++) {
						values.add(valores[i][j]);
					}
					valuesTable.add(values);
				}
				adicionarTabla(docx, valuesTable);
				docx.addBreak("line");
				// Se agrega el encabezado de la tabla
				return res;
			}
		},
		INFORMACIONCOHORTES("Información para las chohortes",
				new String[] { "" }) {
			final double umbral = 0.70d;

			public boolean diagnosticar(IES ies, CreateDocx docx) {
				String mensaje = "b. La institución cuenta con información para las cohortes:";
				mensaje += "[" + ies.semestres[0] + "-"
						+ ies.semestres[ies.semestres.length - 1] + "]";
				adicionarTexto(docx, mensaje);
				mensaje = "Información actualizada: El último periodo reportado por l IES es "
						+ textoBien(ies.semestres[ies.semestres.length - 1]);
				adicionarTexto(docx, mensaje);
				// Se calcula el porcentaje del ICFES
				int tot = 0, val = 0;
				for (Estudiante est : ies.estudiantes) {
					if (est.getPuntajeICFES() != -1)
						val++;
					tot++;
				}
				double por = (1d * val) / tot;
				String sPor = df.format(por);
				boolean res = por >= umbral;
				mensaje = "Cruce con el ICFES: El porcentaje de estudiantes que cruzaron fue "
						+ (res ? textoBien(sPor) : textoError(sPor));
				adicionarTexto(docx, mensaje);
				// Texto adicional
				mensaje = "Nota: Si el porcentaje de estudiantes que cruzan es inferior al 80%, por favor revise la calidad de la información contenida en los "
						+ "archivos de primíparos, en especial la ortografía de apellidos y nombres, tipo de documento, número de documento y fecha de nacimiento.";
				adicionarTexto(docx, mensaje);
				return res;
			}
		},
		COHORTEPRIMIPARO("Cohortes sin información de primíparos",
				new String[] { "" }) {
			private static final int umbral = 2;

			public boolean diagnosticar(IES ies, CreateDocx docx) {
				String mensaje = "c. Cohortes sin información de primíparos:";
				adicionarTexto(docx, mensaje);
				// Texto
				mensaje = "Los períodos que se resaltan en rojo en la tabla de primíparos reportados, corresponden a períodos donde la información es nula "
						+ "o no se cargó en el sistema. Es necesario corregir esta información generando nuevamente el archivo de primíparos del período "
						+ "correspondiente y reportando la información correcta. El encargado de la IES debe revisar la información de este período y en caso "
						+ "de que no exista información, es necesario que la IES envíe un comunicado de rectoría al sistema SPADIES, donde se expliquen las "
						+ "razones por las cuáles dicho período no cuenta con información de primíparos.";
				adicionarTexto(docx, mensaje);
				// Titulo tabla
				mensaje = "PRIMÍPAROS REPORTADOS EN CADA COHORTE";
				adicionarTexto(docx, mensaje);
				// Tabla Cohorte sin primiparos
				boolean res = true;
				int[] conteoCohorte = new int[ies.n];
				for (Estudiante est : ies.estudiantes)
					conteoCohorte[est.getSemestrePrimiparo()]++;
				for (int conteo : conteoCohorte)
					if (conteo < umbral)
						res = false;
				ArrayList<ArrayList> valuesTable = new ArrayList<ArrayList>();
				ArrayList<String> values = new ArrayList<String>();
				values.add("Cohorte");
				values.add("Individuos");
				valuesTable.add(values);
				for (int i = 0; i < ies.n; i++) {
					values = new ArrayList<String>();
					values.add(ies.semestres[i]);
					values.add(conteoCohorte[i] < umbral ? textoError(String
							.valueOf(conteoCohorte[i])) : textoBien(String
							.valueOf(conteoCohorte[i])));
					valuesTable.add(values);
				}
				adicionarTabla(docx, valuesTable);
				docx.addBreak("line");
				return res;
			}
		},
		ANALISISESTADO(
				"Análisis del estado de los estudiantes para cada una de las cohortes:",
				new String[] { "" }) {
			public static final double umbral = 0.02d;

			public boolean diagnosticar(IES ies, CreateDocx docx) {
				String mensaje = "d. Análisis del estado de los estudiantes para cada una de las cohortes:";
				adicionarTexto(docx, mensaje);
				mensaje = "De acuerdo con el seguimiento de información hecho por el SPADIES, a continuación presentamos una relación para cada cohorte,"
						+ " del porcentaje de población correspondiente a cada uno de los estados posibles dentro del sistema SPADIES (activo, graduado, "
						+ "retirado o desertor).";
				adicionarTexto(docx, mensaje);
				mensaje = "Para los retirados, se busca identificar los casos en que los reportes se han sobreestimado. Para tal efecto, los números en rojo "
						+ "indican que la información de retiros disciplinarios reportada por su institución para la cohorte en cuestión, es muy alta. En los casos"
						+ " más extremos que se han detectado dentro del sistema, los retiros disciplinarios no corresponden a un número superior al 2% de la población "
						+ "matriculada, razón por la cual todo dato superior al 2% se considera como una falla en el reporte de la información. Sabemos que se han "
						+ "presentado confusiones respecto a la definición de los retiros disciplinarios (antes llamados retiros forzosos), por lo que al final "
						+ "encontrará la definición de este tipo de retiros.";
				adicionarTexto(docx, mensaje);
				mensaje = "ESTADO POBLACIÓN SPADIES POR COHORTE";
				adicionarTexto(docx, mensaje);
				// Tabla
				boolean res = true;
				ArrayList<ArrayList> valuesTable = new ArrayList<ArrayList>();
				ArrayList<String> values = new ArrayList<String>();
				values.add("Cohorte");
				values.add("Graduado");
				values.add("Retirado");
				values.add("Activo");
				values.add("Desertor");
				valuesTable.add(values);
				int conteo[][] = new int[ies.n][4];
				for (Estudiante est : ies.estudiantes) {
					int ind = -1;
					switch (est.getEstado()) {
					case 1:
						ind = 0;
						break;// Graduado
					case 2:
						ind = 1;
						break;// Retirado
					case 0:
						ind = 2;
						break;// Activo
					case -1:
						ind = 3;
						break;// Desertor
					}
					conteo[est.getSemestrePrimiparo()][ind]++;
				}
				double porcentaje[][] = new double[ies.n][4];
				for (int i = 0; i < ies.n; i++) {
					int tot = 0;
					for (int val : conteo[i])
						tot += val;
					for (int j = 0; j < 4; j++)
						porcentaje[i][j] = (1d * conteo[i][j]) / tot;
				}
				// ---------------------------------
				for (int i = 0; i < ies.n; i++)
					if (porcentaje[i][1] > umbral)
						res = false;
				// ---------------------------------
				for (int i = 0; i < ies.n; i++) {
					values = new ArrayList<String>();
					values.add(ies.semestres[i]);
					for (int j = 0; j < 4; j++) {
						values.add(j + 1, df.format(porcentaje[i][j]));
						if (j == 1 && porcentaje[i][j] > umbral) {
							values.set(j + 1, textoError(values.get(j + 1)));
						}
					}
					valuesTable.add(values);
				}
				// ---------------------------------
				adicionarTabla(docx, valuesTable);
				docx.addBreak("line");
				mensaje = "Retiros disciplinarios: son todos aquellos estudiantes que por razones exclusivamente disciplinarias "
						+ "(casos de plagio, falta al reglamento, etc) han sido sancionados disciplinariamente por la institución y "
						+ "por esa razón no se encuentran matriculados en la institución dentro del período en cuestión. Todos los "
						+ "retiros disciplinarios deben estar respaldados por un acto administrativo.";
				adicionarTexto(docx, mensaje);
				mensaje = "Por favor revise la información reportada dentro de los archivos antes denominados Retiros Forzosos, y "
						+ "retire los estudiantes allí reportados que no entren dentro de la clasificación antes descrita.";
				adicionarTexto(docx, mensaje);
				return res;
			}
		},
		EVOLUCIONMATRICULA("Análisis de la evolución de la matrícula",
				new String[] { "" }) {
			public static final double limSup = 3d;
			public static final double limInf = 0.8d;

			public boolean diagnosticar(IES ies, CreateDocx docx) {
				String mensaje = "e. Análisis de la evolución de la matrícula";
				adicionarTexto(docx, mensaje);
				mensaje = "En la siguiente tabla usted encontrará una relación del número de matriculados reportados en cada período. "
						+ "En la columna “cambio” encontrará un cálculo del cambio porcentual en el número de matriculados de cada período frente "
						+ "al período inmediatamente anterior. El objetivo de este análisis es identificar los períodos en los que se presentan cambios "
						+ "importantes en el número de estudiantes reportados al sistema. Todo cambio porcentual inferior al 80% y superior al 300% se "
						+ "considera anómalo y se resalta en rojo para indicar que la información de matriculados correspondiente a ese período debe ser revisada.";
				adicionarTexto(docx, mensaje);
				mensaje = "Por favor revise la tabla e identifique los períodos en los que los cambios en la población matriculada están por fuera de lo normal y "
						+ "proceda a revisar la información de sus estudiantes en el archivo de matriculados que corresponde al período resaltado. Verifique que los "
						+ "nombres estén bien digitados y que cada estudiante cuente con información completa (apellidos, nombres, tipo de documento, número de documento, "
						+ "programa, etc).";
				adicionarTexto(docx, mensaje);
				mensaje = "PERIODOS CON MATRÍCULA ANÓMALA";
				adicionarTexto(docx, mensaje);
				// Tabla
				boolean res = true;
				int[] conteo = new int[ies.n];
				double[] razon = new double[ies.n];
				for (Estudiante e : ies.estudiantes) {
					int jI = e.getSemestrePrimiparo();
					long matri = e.getSemestresMatriculadoAlDerecho() >>> jI;
					for (int j = jI; j < ies.n; j++, matri >>>= 1)
						if ((matri & 1L) == 1L)
							conteo[j]++;
				}
				for (int i = 1; i < ies.n; i++)
					razon[i] = (conteo[i] * 1d) / (conteo[i - 1]);
				ArrayList<ArrayList> valuesTable = new ArrayList<ArrayList>();
				ArrayList<String> values = new ArrayList<String>();
				values.add("Periodo");
				values.add("Matriculados");
				values.add("Cambio");
				valuesTable.add(values);
				// --------------------------------------------
				for (int i = 0; i < ies.n; i++) {
					values = new ArrayList<String>();
					values.add(ies.semestres[i]);
					values.add(String.valueOf(conteo[i]));
					if (i != 0 && razon[i] != Double.NaN) {
						values.add(df.format(razon[i]));
						if (razon[i] < limInf || razon[i] > limSup) {
							values.set(2, textoError(values.get(2)));
							res = false;
						}
					} else {
						values.add("");
					}
					valuesTable.add(values);
				}
				// --------------------------------------------
				adicionarTabla(docx, valuesTable);
				docx.addBreak("line");
				return res;
			}
		},
		ANALISISPROGRAMAS("Análisis de la información de programas académicos",
				new String[] { "" }) {
			public boolean diagnosticar(IES ies, CreateDocx docx) {
				String mensaje = "f. Análisis de la información de programas académicos";
				adicionarTexto(docx, mensaje);
				mensaje = "A continuación presentamos un análisis de la información de programas académicos "
						+ "reportada por su institución al SPADIES. El objetivo principal de este análisis "
						+ "es verificar el cruce de los códigos SNIES incluidos en los archivos planos, con "
						+ "los códigos de programas académicos reportados por el SNIES al sistema SPADIES.";
				adicionarTexto(docx, mensaje);
				// ---------------------------------------------
				mensaje = "La siguiente tabla le indica los códigos de programa identificados con múltiples "
						+ "nombres de programa. Cada programa académico tiene un único código SNIES, por lo que ningún "
						+ "código puede identificar más de un programa. Por favor revise estos códigos y relaciónelos "
						+ "con el único programa al que pertenecen y lleve a cabo sincronización nuevamente. Si la tabla "
						+ "está vacía haga caso omiso a este ítem.";
				adicionarTexto(docx, mensaje);
				// ---------------------------------------------
				mensaje = "PROGRAMAS CUYO CÓDIGO SNIES APARECE CON VARIOS NOMBRES";
				adicionarTexto(docx, mensaje);
				// Tabla cuyo código snies aparece con varios nombres
				Map<String, Set<String>> nombresProgramaPorCodigo = new TreeMap<String, Set<String>>(), newNombresProgramaPorCodigo = new TreeMap<String, Set<String>>();
				for (Programa p : ies.programas) {
					String cod = new String(p.codigoSNIES);
					if (cod.equals(""))
						continue; // Ignorar codigos vacios
					Set<String> set = nombresProgramaPorCodigo.get(cod);
					if (set == null)
						nombresProgramaPorCodigo.put(cod,
								set = new TreeSet<String>());
					set.add(new String(p.nombre));
				}
				for (Entry<String, Set<String>> ent : nombresProgramaPorCodigo
						.entrySet())
					if (ent.getValue().size() > 1)
						newNombresProgramaPorCodigo.put(ent.getKey(),
								ent.getValue());
				ArrayList<ArrayList> valuesTable = new ArrayList<ArrayList>();
				ArrayList<String> values = new ArrayList<String>();
				values.add("Código");
				values.add("Nombres");
				valuesTable.add(values);
				{
					int i = 0;
					for (Entry<String, Set<String>> ent : newNombresProgramaPorCodigo
							.entrySet()) {
						values = new ArrayList<String>();
						values.add(ent.getKey());
						values.add(listaNoOrdenadaHTML(ent.getValue()));
						valuesTable.add(values);
					}
				}
				// --------------------------------------------
				adicionarTabla(docx, valuesTable);
				docx.addBreak("line");
				boolean returnTemp = newNombresProgramaPorCodigo.size() == 0;
				// Info Segunda tabla
				mensaje = "La tabla correspondiente a los códigos SNIES que no cruzan le indica los programas "
						+ "cuyo nombre y código SNIES reportado no concuerdan con la tabla de programas reportada "
						+ "por el SNIES. Los códigos y programas reportados en esta tabla corresponden a aquellos "
						+ "que no se pueden identificar dentro del reporte SNIES ya que el código y el nombre de "
						+ "programa reportado no coinciden. Igualmente se identifican los nombres de programas "
						+ "cuyo código SNIES no se registró dentro de los archivos planos. Para corregir la "
						+ "información, usted debe ir a los archivos de primíparos, identificar los errores y "
						+ "corregirlos. Por favor verifique tanto el código como el nombre de programa en los "
						+ "archivos planos reportados por la IES y asegúrese de que concuerden.";
				adicionarTexto(docx, mensaje);
				// ---------------------------------------------
				mensaje = "PROGRAMAS CUYO CÓDIGO SNIES NO CRUZA CON LA INFORMACIÓN REPORTADA POR EL SNIES";
				adicionarTexto(docx, mensaje);
				// ---------------------------------------------
				Collection<Programa> programasSinCruce = new LinkedList<Programa>();
				for (Programa p : ies.programas)
					if (p.nivel == -1)
						programasSinCruce.add(p);
				values = new ArrayList<String>();
				valuesTable = new ArrayList<ArrayList>();
				values.add("Codigo");
				values.add("Nombre");
				valuesTable.add(values);
				{
					int i = 0;
					for (Programa p : programasSinCruce) {
						values = new ArrayList<String>();
						values.add(new String(p.codigoSNIES));
						values.add(new String(p.nombre));
						valuesTable.add(values);
					}
				}
				adicionarTabla(docx, valuesTable);
				docx.addBreak("line");
				// ---------------------------------------------
				mensaje = "A continuación la tabla de Individuos sin programa por cohorte le muestra una relación del número de individuos "
						+ "que carecen de información de programa académico. Por favor revise esta información y complétela en el archivo de "
						+ "primíparos correspondiente a la cohorte en cuestión. La información del programa académico es importante para poder "
						+ "llevar a cabo un buen análisis del comportamiento de los programas en su institución.";
				adicionarTexto(docx, mensaje);
				// ---------------------------------------------
				mensaje = "INDIVIDUOS SIN PROGRAMA POR COHORTE";
				adicionarTexto(docx, mensaje);
				// ---------------------------------------------
				boolean res = true;
				int[] conteoSinProgramaCohorte = new int[ies.n];
				for (Estudiante est : ies.estudiantes) {
					if (est.getIndicePrograma() == -1) {
						res = false;
						conteoSinProgramaCohorte[est.getSemestrePrimiparo()]++;
					}
				}
				valuesTable = new ArrayList<ArrayList>();
				values = new ArrayList<String>();
				values.add("Cohorte");
				values.add("Individuos");
				valuesTable.add(values);
				for (int i = 0; i < ies.n; i++) {
					values = new ArrayList<String>();
					values.add(ies.semestres[i]);
					values.add(String.valueOf(conteoSinProgramaCohorte[i]));
					valuesTable.add(values);
				}
				adicionarTabla(docx, valuesTable);
				docx.addBreak("line");
				// ---------------------------------------------
				mensaje = "La tabla de Matrícula por programa/período presenta una relación del número de matriculados en cada período para "
						+ "los programas académicos reportados por la institución. Esta tabla permite analizar la evolución de la matrícula en "
						+ "el tiempo y permite identificar posibles problemas de información en el reporte de algunos programas académicos.";
				adicionarTexto(docx, mensaje);
				// ---------------------------------------------
				mensaje = "Matricula por programa/periodo";
				adicionarTexto(docx, mensaje);
				// ----------------------------------------------
				int[][] conteo = new int[ies.programas.length + 1][ies.n];
				for (Estudiante e : ies.estudiantes) {
					int jI = e.getSemestrePrimiparo();
					long matri = e.getSemestresMatriculadoAlDerecho() >>> jI;
					for (int j = jI; j < ies.n; j++, matri >>>= 1)
						if ((matri & 1L) == 1L)
							conteo[e.getIndicePrograma() + 1][j]++;
				}
				valuesTable = new ArrayList<ArrayList>();
				values = new ArrayList<String>();
				// Copia de los periodos
				values.add("Periodo");
				for (int contadorSemestres = 0; contadorSemestres < ies.semestres.length; contadorSemestres++) {
					values.add(ies.semestres[contadorSemestres]);
				}
				valuesTable.add(values);
				values = new ArrayList<String>();
				values.add("DESCONOCIDO");
				for (int ip = 0; ip < conteo.length; ip++) {
					for (int i = 0; i < ies.n; i++) {
						values.add(conteo[ip][i] == 0 ? "" : String
								.valueOf(conteo[ip][i]));
					}
					valuesTable.add(values);
					values = new ArrayList<String>();
					if (ip + 1 < conteo.length)
						values.add(new String(ies.programas[ip].nombre));
				}
				adicionarTabla(docx, valuesTable);
				docx.addBreak("line");
				return programasSinCruce.isEmpty() && returnTemp && res;
			}
		},
		VARIABLESEXPLICAN("Variables que explican la deserción en la IES",
				new String[] { "" }) {
			public boolean diagnosticar(IES ies, CreateDocx docx) {
				Filtro fil = new Filtro(Variable.CODIGO_IES,
						new Item[] { new Item(ies.codigo, "", "") });
				// ---------------------------------
				String mensaje = "II. Características generales de la deserción en la IES";
				adicionarTexto(docx, mensaje);
				// ---------------------------------
				mensaje = "a. Variables que explican la deserción en la IES";
				adicionarTexto(docx, mensaje);
				// ---------------------------------
				mensaje = "La tabla a continuación ordena de mayor a menor, según el orden de importancia, "
						+ "la variables que explican la deserción en su institución.";
				adicionarTexto(docx, mensaje);
				// --------------------------------- Tabla
				mensaje = "Variables segun importancia";
				adicionarTexto(docx, mensaje);
				ArrayList<ArrayList> valuesTable = new ArrayList<ArrayList>();
				ArrayList<String> values = new ArrayList<String>();
				values.add("Variable");
				values.add("Mejor");
				values.add("Puntaje");
				valuesTable.add(values);
				Object[][] resp;
				try {
					resp = kernel.getVariablesRelevantes(new Filtro[] { fil });
					for (int i = 0; i < resp.length; i++) {
						values = new ArrayList<String>();
						for (int j = 0; j < 3; j++) {
							values.add(j != 1 ? resp[i][j].toString()
									: ((Variable) resp[i][0]).rango
											.toString(((byte[]) resp[i][1])[0]));
						}
						valuesTable.add(values);
					}
					adicionarTabla(docx, valuesTable);
					docx.addBreak("line");
				} catch (MyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
		},
		DESERCIONCOHORTE("Deserción por cohorte", new String[] { "" }) {
			public boolean diagnosticar(IES ies, CreateDocx docx) {
				// ---------------------------------
				String mensaje = "b. Deserción por cohorte";
				adicionarTexto(docx, mensaje);
				// ---------------------------------
				mensaje = "La gráfica a continuación le muestra el comportamiento de la tasa de deserción "
						+ "por cohorte promedio para su institución.";
				adicionarTexto(docx, mensaje);
				// -------------------------------
				try {
					Filtro fil = new Filtro(Variable.CODIGO_IES,
							new Item[] { new Item(ies.codigo, "", "") });
					ProcesadorConsultaDesercion.instance.setParametros(false,
							new Variable[0], new Filtro[] { fil });
					Collection<ResultadoConsulta> graf = ProcesadorConsultaDesercion.instance
							.generarGrafica();
					int i = 0;
					for (ResultadoConsulta res : graf) {
						if (res.resultado instanceof ChartPanel) {
							String nomAr = ies.codigo + "_1" + i + ".png";
							guardarImagen(((ChartPanel)res.resultado),new
							File(carImg,nomAr));
							adicionarImagen(docx,carImg.getAbsolutePath()+"/"+nomAr);
							i++;
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
		},
		DESERCIONPERIODO("Deserción por período", new String[] { "" }){

			public boolean diagnosticar(IES ies, CreateDocx docx) {
				//-----------------------------------
				String mensaje = "c. Deserción por período";
				adicionarTexto(docx, mensaje);
				//-----------------------------------
				mensaje = "La tabla a continuación le muestra el comportamiento de la tasa de deserción por " +
						"período para cada uno de los períodos reportados por su institución al SPADIES.";
				adicionarTexto(docx, mensaje);
				//---------------------------- Imagen
				Filtro fil = new Filtro(Variable.CODIGO_IES,
						new Item[] { new Item(ies.codigo, "", "") });
				ProcesadorDesercionPorPeriodo.instance.setParametros(false,
						new Variable[0], new Filtro[] { fil });
				try {
				Collection<ResultadoConsulta> graf = ProcesadorDesercionPorPeriodo.instance.generarGrafica();
				int i = 0;
				for(ResultadoConsulta res : graf){
					if(res.resultado instanceof ChartPanel){
						String nomAr = ies.codigo + "_2"+i+".png";
						guardarImagen(((ChartPanel)res.resultado), new File(carImg,nomAr));
						adicionarImagen(docx, carImg.getAbsolutePath()+"/"+nomAr);
						i++;
					}
				}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
			
		},
		;
		private static boolean conDescripcion = true;
		public final String nombre;
		private String[] descripcion = new String[] {};

		private PruebaIES(String nombre, String[] descripcion) {
			this.nombre = nombre;
			this.descripcion = descripcion;
		}

		public abstract boolean diagnosticar(IES ies, CreateDocx docx);

		public boolean diagnosticarDetallado(IES ies, CreateDocx docx) {
			boolean res = diagnosticar(ies, docx);
			return res;
		}
	}

	/**********************/
	/******* Variables ******/
	/**********************/
	private static KernelSPADIES kernel = null;
	private static File carImg;
	private static final transient DecimalFormat df = new DecimalFormat("0.00%");

	public static void main(String[] args) throws MyException {
		Constantes.cargarArchivoFiltroIES();
		kernel = KernelSPADIES.getInstance();
		kernel.cargarParaServidor(Constantes.carpetaDatos, Long.MAX_VALUE,
				false);
		// Se especifica la carpeta en donde se van a persistir la informaci�n
		File carSal = new File("reporteIESWord");
		carImg = new File(carSal, "img");
		// Se comprueba si existe el directorio, Si no existe lo crea
		if (!carSal.exists()) {
			carSal.mkdir();
		}
		// Se comprueba si existe el directorio, Si no existe lo crea
		if (!carImg.exists()) {
			carImg.mkdir();
		}
		// Se crea el objeto con el que se manejara lo relacionado al indice
		com.javadocx.CreateDocx indiceDocx = new CreateDocx("docx");
		// Se crea un objeto para manejar los archivos de las diferentes IES
		com.javadocx.CreateDocx IESDoxc;
		// Se configura el titulo
		HashMap<String, String> paramsTitle = new HashMap<String, String>();
		paramsTitle.put("val", "1");
		paramsTitle.put("u", "single");
		paramsTitle.put("sz", "22");
		paramsTitle.put("font", "Arial");
		// Se introduce el titulo al objeto
		indiceDocx.addTitle("Indice diagnostico IES", paramsTitle);
		// Varialbes para saber el n�mero de pruebas analizadas
		int numPruebas = PruebaIES.values().length;
		ArrayList<ArrayList> valuesTableIndice = new ArrayList<ArrayList>();
		ArrayList<String> rowIndice = new ArrayList<String>();
		// Variable que guarda el encabezado de la tabla del indice
		rowIndice.add("CodigoIES");
		rowIndice.add("NombreIES");
		// Se genera la parte superior de la tabla
		for (int i = 0; i < numPruebas; i++)
			rowIndice.add(PruebaIES.values()[i].nombre);
		valuesTableIndice.add(rowIndice);
		{
			int i = 0;
			for (IES ies : kernel.listaIES) {
				/* Info del Indice del indice */
				// Se crea una nueva instancia que contenga las difernetes
				// columnas de la IES
				rowIndice = new ArrayList<String>();
				rowIndice.add(String.valueOf(ies.codigo));
				rowIndice.add(new String(ies.codigo + "-" + ies.nombre));
				// Se instancia el objeto que maneja el archivo
				IESDoxc = new CreateDocx("docx");
				// Encabezado del documento
				descripcionIES(ies, IESDoxc);
				generarEncabezado(IESDoxc, new String(ies.nombre));
				for (int j = 0; j < numPruebas; j++) {
					String sa[] = new String[1];
					rowIndice.add(PruebaIES.values()[j].diagnosticarDetallado(
							ies, IESDoxc) ? textoBien("OK") : textoError("X"));
					// ps.print(sa[0]);
				}
				valuesTableIndice.add(rowIndice);
				IESDoxc.setLanguage("es_ES");
				/* Generación del archivo */
				IESDoxc.createDocx(carSal.getAbsolutePath() + "/" + ies.codigo);
			}
		}
		adicionarTabla(indiceDocx, valuesTableIndice);
		indiceDocx.setLanguage("es_ES");
		indiceDocx.createDocx(carSal.getAbsolutePath() + "/index");
	}
	
	
	private static void guardarImagen(ChartPanel cp, File f) {
		JFreeChart chart = cp.getChart();
		BufferedImage img = new BufferedImage(700, 400,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = img.createGraphics();
		chart.draw(g2, new Rectangle2D.Double(0, 0, 700, 400));
		g2.dispose();
		try {
			ImageIO.write(img, "PNG", f);
		} catch (IOException e) {
			System.err.println("Error almacenando imagen");
			e.printStackTrace();
		}
	}

	private static void generarEncabezado(CreateDocx docx, String nombreIES) {
		String info = "A continuación presentamos un análisis de la información reportada por la "
				+ nombreIES
				+ " al SPADIES del Ministerio de Educación Nacional. El análisis de la información se lleva a cabo con el "
				+ "fin de resaltar las principales características de ésta, según la información de primíparos, matriculados, graduados y retiros disciplinarios "
				+ "reportada para cada uno de los períodos.";
		HashMap<String, String> paramsText = new HashMap<String, String>();
		paramsText.put("font", "Arial");
		docx.addText(info, paramsText);
	}
	
	private static void adicionarImagen(CreateDocx docx, String imagen){
		HashMap<String,String> paramsImage = new HashMap<String,String>();
		paramsImage.put("name", imagen);
		paramsImage.put("scaling", "60");
        paramsImage.put("spacingTop", "50");
        paramsImage.put("spacingBottom", "0");
        paramsImage.put("spacingLeft", "50");
        paramsImage.put("spacingRight", "0");
        paramsImage.put("textWrap", "1");
        paramsImage.put("border", "1");
        paramsImage.put("borderDiscontinuous", "1");

        docx.addImage(paramsImage);
	}

	private static void adicionarTexto(CreateDocx docx, String texto) {
		HashMap<String, String> paramsText = new HashMap<String, String>();
		paramsText.put("font", "Arial");
		docx.addText(texto, paramsText);
	}

	private static void adicionarTabla(CreateDocx docx,
			ArrayList<ArrayList> objetos) {
		HashMap<String, String> paramsTable = new HashMap<String, String>();
		paramsTable.put("border", "single");
		paramsTable.put("border_sz", "9");
		docx.addTable(objetos, paramsTable);
	}

	private static void descripcionIES(IES ies, CreateDocx docx) {
		String sb = new String("");
		sb += ies.codigo + "-" + new String(ies.nombre);
		adicionarTexto(docx, sb);
	}

	private static String textoError(String txt) {
		return textoTipo(txt, "FF0000");
	}

	private static String textoBien(String txt) {
		return textoTipo(txt, "#000080");
	}

	private static String listaNoOrdenadaHTML(Collection<String> col) {
		ArrayList<String> elemento = new ArrayList<String>();
		;
		for (String str : col) {
			elemento.add(str);
		}
		HashMap<String, String> paramsList = new HashMap<String, String>();
		paramsList.put("val", "1");
		CreateList list = CreateList.getInstance();
		list.createList(elemento, paramsList);
		return list.toString();
	}

	private static String textoTipo(String txt, String tipo) {
		return "<w:rPr><w:color w:val=\"" + tipo
				+ "\"/><w:lang w:val=\"es-CO\"/></w:rPr>" + txt;
	}

}
