package utils;

/**
 * Interface pro t��dy, kter� maj� pr�b�h a ten se d�l� na d�l�� pr�b�hy.
 */
public interface HavingSubprogress {
	/**
	 * Vrac� indik�tor aktu�ln� operace.
	 * @return Indik�tor aktu�ln� operace.
	 */
	public SubprogressOperation getCurrentOperation();
	
	/**
	 * Vrac� ��slo aktu�ln� operace. 
	 * @return ��slo aktu�ln� operace.
	 */
	public int getCurrentOperationNumber();
	
	/**
	 * Vrac� celkov� po�et operac�. 
	 * @return Celkov� po�et operac�.
	 */
	public int getOperationCount();
	
	/**
	 * Vrac� jm�no aktu�ln� operace.
	 * @return Jm�no aktu�ln� operace.
	 */
	public String getCurrentOperationName();
	
	/**
	 * Vrac� po�et krok� proveden�ch v r�mci aktu�ln� operace.
	 * @return Po�et krok� proveden�ch v r�mci aktu�ln� operace.
	 */
	public long getCurrentOperationDone();
	
	/**
	 * Vrac� pom�r po�tu proveden�ch krok� ku v�em krok�m, kter� je t�eba celkem prov�st pro aktu�ln� operaci.
	 * @return Pom�r po�tu proveden�ch krok� ku v�em krok�m, kter� je t�eba celkem prov�st pro aktu�ln� operaci.
	 */
	public double getCurrentOperationDoneRatio();
	
	/** 
	 * Vrac� celkov� po�et krok�, kter� je t�eba prov�st pro aktu�ln� operaci.
	 * @return Celkov� po�et krok�, kter� je t�eba prov�st pro aktu�ln� operaci.
	 */
	public long getCurrentOperationTodo();
}
