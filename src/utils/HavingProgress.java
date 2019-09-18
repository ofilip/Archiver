package utils;

/**
 * Interface pro tøídy mající prùbìh operace.
 */
public interface HavingProgress {
	/** 
	 * Vrací aktuální poèet již provedených krokù.
	 * @return Aktuální poèet již provedených krokù.
	 */
	public long getDone();
	
	/**
	 * Vrací pomìr poètu provedených krokù ku všem krokùm, které je tøeba celkem provést. 
	 * @return Pomìr poètu provedených krokù ku všem krokùm, které je tøeba celkem provést.
	 */
	public double getDoneRatio();
	
	/**
	 * Vrací poèet krokù, které je tøeba celkem provést
	 * @return Poèet krokù, které je tøeba celkem provést.
	 */
	public long getTodo();
}
