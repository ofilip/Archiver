package utils;

/**
 * Interface pro t��dy maj�c� pr�b�h operace.
 */
public interface HavingProgress {
	/** 
	 * Vrac� aktu�ln� po�et ji� proveden�ch krok�.
	 * @return Aktu�ln� po�et ji� proveden�ch krok�.
	 */
	public long getDone();
	
	/**
	 * Vrac� pom�r po�tu proveden�ch krok� ku v�em krok�m, kter� je t�eba celkem prov�st. 
	 * @return Pom�r po�tu proveden�ch krok� ku v�em krok�m, kter� je t�eba celkem prov�st.
	 */
	public double getDoneRatio();
	
	/**
	 * Vrac� po�et krok�, kter� je t�eba celkem prov�st
	 * @return Po�et krok�, kter� je t�eba celkem prov�st.
	 */
	public long getTodo();
}
