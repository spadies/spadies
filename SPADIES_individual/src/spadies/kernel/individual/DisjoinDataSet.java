package spadies.kernel.individual;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import spadies.kernel.KernelSPADIES;

public class DisjoinDataSet {
	
	static int[] disjoinDatasets;
	public static String obtenerMD5Datos(){
		File[] archivos=new File("datos").listFiles();
		Arrays.sort(archivos,new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			for(File f:archivos){
				md.update(f.getName().getBytes());
				md.update((""+f.lastModified()).getBytes());
				md.update((""+f.length()).getBytes());
			}
			return new BigInteger(1,md.digest()).toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		String h="";
		for(File f:archivos){
			h+=(f.getName().getBytes());
			h+=((""+f.lastModified()).getBytes());
			h+=((""+f.length()).getBytes());
		}
		return h;
	}
	public static boolean load(){
		String md5=obtenerMD5Datos();
		try(ObjectInputStream ois=new ObjectInputStream(new FileInputStream("individual.ind"))){
			System.out.println(md5);
			String oldMd5=(String)ois.readObject();
			System.out.println(oldMd5);
			if(!oldMd5.equals(md5))return false;
			disjoinDatasets=(int[])ois.readObject();
			return true;
		}catch (Exception e) {
			System.err.println("Archivo individual.ind no encontrado");
		}
		return false;//False quiere decir decir que toca recalcularlo
	}
	public static void save(){
		try(ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream("individual.ind"))){
			oos.writeObject(obtenerMD5Datos());
			oos.writeObject(disjoinDatasets);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void init(int size){
		disjoinDatasets=new int[size];
		for(int e=0;e<size;e++)disjoinDatasets[e]=e;
	}
	public static void link(int id1,int id2){
		id1=getParent(id1);
		id2=getParent(id2);
		disjoinDatasets[id1]=disjoinDatasets[id2]=Math.min(id1,id2);
	}
	private static int getParent(int id){
		int x=id;
		while(disjoinDatasets[x]!=x)x=disjoinDatasets[x];
		while(disjoinDatasets[id]!=id){
			int temp=disjoinDatasets[id];
			disjoinDatasets[id]=x;
			id=temp;
		}
		return x;
	}
	/**
	 * Un arreglo de individuos. Cada individuo está representado con una lista de enteros con los índices absolutos de la(s) ies donde aparece
	 * Un individuo aparece tantas veces como lo indique el cruce. Listas de posiciones distintas para un mismo individuo tienen los mismos valores. 
	 */
	public static List<Integer>[] getRelacionIndividuos(){
		List<Integer>[] ret=new List[disjoinDatasets.length];
		for(int e=0;e<disjoinDatasets.length;e++)if(disjoinDatasets[e]==e)ret[e]=new LinkedList<>();
		for(int e=0;e<disjoinDatasets.length;e++){
			if(disjoinDatasets[e]!=e)ret[e]=ret[getParent(e)];//Apunta a la lista del papa
			ret[getParent(e)].add(e);
		}
		return ret;
	}
	/**
	 * Un arreglo de individuos. Cada individuo está representado con una lista de enteros con los índices absolutos de la(s) ies donde aparece
	 * Solo aparece un individuo una vez. Hay posiciones en null para saber cuales registros ya están representado en otro registro.
	 */
	public static List<Integer>[] getIndividuos(){
		List<Integer>[] ret=new List[disjoinDatasets.length];
		for(int e=0;e<disjoinDatasets.length;e++)if(disjoinDatasets[e]==e)ret[e]=new LinkedList<>();
		for(int e=0;e<disjoinDatasets.length;e++)ret[getParent(e)].add(e);
		return ret;
	}
	/**
	 * Crea un listado de individuos únicos. Cada individuo tiene la posición de la IES y la posición en el arreglo de datos personales dentro de esta.
	 * Solo aparece un individuo una vez. Hay posiciones en null para saber cuales registros ya están representado en otro registro.
	 * @param kernel
	 * @return
	 */
	public static Individuo[] getIndividuosIdsRelativos(KernelSPADIES kernel){
		int[][] mapaIdAbsolutosARelativos=new int[disjoinDatasets.length][2];
		for(int e=0,s=0;e<kernel.listaIES.length;e++){
			for(int i=0;i<kernel.listaIES[e].datosPersonalesEstudiantes.length;i++,s++){
				mapaIdAbsolutosARelativos[s][0]=e;
				mapaIdAbsolutosARelativos[s][1]=i;
			}
		}
		Individuo[] ret=new Individuo[disjoinDatasets.length];
		for(int e=0;e<disjoinDatasets.length;e++)if(disjoinDatasets[e]==e)ret[e]=new Individuo();
		for(int e=0;e<disjoinDatasets.length;e++)ret[getParent(e)].add(mapaIdAbsolutosARelativos[e]);
		return ret;
	}
	public static void histograma(){
		System.out.println("histograma");
		Map<Integer,Integer> hist=new TreeMap<Integer, Integer>();
		List<Integer>[] individuos=getIndividuos();
		for(List<Integer> l:individuos)if(l!=null){
			Integer n=hist.get(l.size());
			if(n==null)n=0;
			hist.put(l.size(),++n);
		}
		for(Entry<Integer,Integer> ent:hist.entrySet()){
			System.out.println(ent.getKey()+" "+ent.getValue());
		}
	}
}
