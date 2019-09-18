package utils;

/**
 * Rozhraní definující dílèí operaci.
 */
public interface SubprogressOperation {
	/**
	 * Vrací celkový poèet krokù operace.
	 * @return Celkový poèet krokù operace.
	 */
	public long getTodo();
	
	/**
	 * Vrací jméno operace.
	 * @return Jméno operace.
	 */
	public String getName();
}
