package utils;

/**
 * Interface pro tøídy, které mají prùbìh a ten se dìlí na dílèí prùbìhy.
 */
public interface HavingSubprogress {
	/**
	 * Vrací indikátor aktuální operace.
	 * @return Indikátor aktuální operace.
	 */
	public SubprogressOperation getCurrentOperation();
	
	/**
	 * Vrací èíslo aktuální operace. 
	 * @return Èíslo aktuální operace.
	 */
	public int getCurrentOperationNumber();
	
	/**
	 * Vrací celkový poèet operací. 
	 * @return Celkový poèet operací.
	 */
	public int getOperationCount();
	
	/**
	 * Vrací jméno aktuální operace.
	 * @return Jméno aktuální operace.
	 */
	public String getCurrentOperationName();
	
	/**
	 * Vrací poèet krokù provedených v rámci aktuální operace.
	 * @return Poèet krokù provedených v rámci aktuální operace.
	 */
	public long getCurrentOperationDone();
	
	/**
	 * Vrací pomìr poètu provedených krokù ku všem krokùm, které je tøeba celkem provést pro aktuální operaci.
	 * @return Pomìr poètu provedených krokù ku všem krokùm, které je tøeba celkem provést pro aktuální operaci.
	 */
	public double getCurrentOperationDoneRatio();
	
	/** 
	 * Vrací celkový poèet krokù, které je tøeba provést pro aktuální operaci.
	 * @return Celkový poèet krokù, které je tøeba provést pro aktuální operaci.
	 */
	public long getCurrentOperationTodo();
}
