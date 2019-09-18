package utils;

/**
 * Rozhran� definuj�c� d�l�� operaci.
 */
public interface SubprogressOperation {
	/**
	 * Vrac� celkov� po�et krok� operace.
	 * @return Celkov� po�et krok� operace.
	 */
	public long getTodo();
	
	/**
	 * Vrac� jm�no operace.
	 * @return Jm�no operace.
	 */
	public String getName();
}
