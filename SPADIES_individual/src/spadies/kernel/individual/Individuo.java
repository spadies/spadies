package spadies.kernel.individual;

import java.util.LinkedList;

import spadies.kernel.Estudiante;
import spadies.kernel.IES;
import spadies.kernel.KernelSPADIES;

public class Individuo extends LinkedList<int[]>{
	static KernelSPADIES kernel = KernelSPADIES.getInstance();
	private static final long serialVersionUID = 1231743998177146213L;

	public int getSemestrePrimiparo(){
		int min=Integer.MAX_VALUE;
		for(int[] app:this){
			int x=kernel.listaIES[app[0]].estudiantes[app[1]].getSemestrePrimiparo();
			if(x!=-1)min=Math.min(min,x+kernel.listaIES[app[0]].minCodigoSemestre);
		}
		return min==Integer.MAX_VALUE?-1:min;
	}
	public Aparicion getPrimiparoPrimeraVez(){
		int min=Integer.MAX_VALUE;
		IES ies=null;
		Estudiante est=null;
		for(int[] app:this){
			int mcs=kernel.listaIES[app[0]].minCodigoSemestre;
			int jI=kernel.listaIES[app[0]].estudiantes[app[1]].getSemestrePrimiparo();
			if(jI!=-1&&mcs+jI<min){
				ies=kernel.listaIES[app[0]];
				est=kernel.listaIES[app[0]].estudiantes[app[1]];
				min=mcs+jI;
			}
		}
		return ies==null?null:new Aparicion(ies, est);
	}
	public Aparicion getPrimiparoUltimaVez(KernelSPADIES kernel){
		int max=-1;
		IES ies=null;
		Estudiante est=null;
		for(int[] app:this){
			int mcs=kernel.listaIES[app[0]].minCodigoSemestre;
			int jI=kernel.listaIES[app[0]].estudiantes[app[1]].getSemestrePrimiparo();
			if(mcs+jI!=-1&&mcs+jI>max){
				ies=kernel.listaIES[app[0]];
				est=kernel.listaIES[app[0]].estudiantes[app[1]];
				max=Math.max(max,mcs+jI);
			}
		}
		return ies==null?null:new Aparicion(ies, est);
	}
	public Aparicion getUltimaIESMatriculado(){
		int max=-1;
		IES ies=null;
		Estudiante est=null;
		for(int[] app:this){
			int mcs=kernel.listaIES[app[0]].minCodigoSemestre;
			int x=kernel.listaIES[app[0]].estudiantes[app[1]].getUltimoSemestreMatriculado();
			if(mcs+x!=-1&&mcs+x>max){
				ies=kernel.listaIES[app[0]];
				est=kernel.listaIES[app[0]].estudiantes[app[1]];
				max=Math.max(max,mcs+x);
			}
		}
		return ies==null?null:new Aparicion(ies, est);
	}
	
	public boolean aparecePrimiparoUltimosSemestres(KernelSPADIES kernel){
		for(int[] app:this){
			if(kernel.listaIES[app[0]].estudiantes[app[1]].getSemestrePrimiparo()>=kernel.listaIES[app[0]].n-2)return true;
		}
		return false;
	}
	
	public boolean esDesertor(KernelSPADIES kernel){
		for(int[] app:this){
			if(kernel.listaIES[app[0]].estudiantes[app[1]].getEstado()!=-1)return false;
		}
		return true;
	}
	public boolean esGraduado(KernelSPADIES kernel){
		for(int[] app:this){
			if(kernel.listaIES[app[0]].estudiantes[app[1]].getEstado()==1)return true;
		}
		return false;
	}
	public long getSemestresMatriculadoAlDerecho(KernelSPADIES kernel){
		long ret=0;
		Aparicion primiparo=getPrimiparoPrimeraVez();
		int prim= primiparo.estudiante.getSemestrePrimiparo()+primiparo.ies.minCodigoSemestre;
		for(int[] app:this){
			long matri=kernel.listaIES[app[0]].estudiantes[app[1]].getSemestresMatriculadoAlDerecho()>>>prim-kernel.listaIES[app[0]].minCodigoSemestre;
			ret |= matri;
		}
		return ret;
	}
	public boolean estaMatriculado(int periodoUniversal){
		Aparicion primiparo=getPrimiparoPrimeraVez();
		int prim= primiparo.estudiante.getSemestrePrimiparo()+primiparo.ies.minCodigoSemestre;
		for(int[] app:this){
			Estudiante e=kernel.listaIES[app[0]].estudiantes[app[1]];
			long matri=e.getSemestresMatriculadoAlDerecho()>>>prim-kernel.listaIES[app[0]].minCodigoSemestre;
			if(((1<<periodoUniversal)&matri)!=0)return true;
		}
		return false;
	}
	public void consultaTatiana(int[] acumulador){
		int prim = getSemestrePrimiparo();
		int ultimo = getUltimoSemestreMatriculado();
		for(int[] app : this){
			int x=kernel.listaIES[app[0]].estudiantes[app[1]].getSemestrePrimiparo();
			if(x!=-1){
				x+=kernel.listaIES[app[0]].minCodigoSemestre;
				for(int i=x;i<ultimo;i++){
					acumulador[i-prim]++;
				}
			}
		}
	}
	public int getUltimoSemestreMatriculado(){
		Aparicion app=getUltimaIESMatriculado();
		Aparicion pri=getPrimiparoPrimeraVez();
		if(app==null)return -1;
		return app.estudiante.getUltimoSemestreMatriculado()+app.ies.minCodigoSemestre;
	}
	/**
	 * Sustituto de IES.n
	 * @return
	 */
	public int getUltimoPeriodoFuturoEstudiante(){
		int ultimoSemestreMatriculado=-1,max=0;
		for(int[] app:this){
			int mcs=kernel.listaIES[app[0]].minCodigoSemestre;
			int x=kernel.listaIES[app[0]].estudiantes[app[1]].getUltimoSemestreMatriculado();
			if(mcs+x!=-1&&mcs+x>ultimoSemestreMatriculado){
				if(ultimoSemestreMatriculado<mcs+x){
					ultimoSemestreMatriculado=mcs+x;
					max=kernel.listaIES[app[0]].n+kernel.listaIES[app[0]].minCodigoSemestre;
				}else if(ultimoSemestreMatriculado==mcs+x){
					max=Math.max(max,kernel.listaIES[app[0]].n+kernel.listaIES[app[0]].minCodigoSemestre);
				}
				
			}
		}
		return max;
	}
}
