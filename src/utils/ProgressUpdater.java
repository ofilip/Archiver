package utils;

/**
 * Tøída pro snazší aktualizaci prùbìhu v tøídì Subprogress.
 */
public class ProgressUpdater {
	/** Aktualizovaný indikátor prùbìhu. */
	private Subprogress p;
	/** Èítaè krokù. */
	private long counter;
	/** Poèáteèní poèet již provedených krokù. */
	private long initialDone;
	/** Poèet krokù provedených tímto updaterem. */
	private double doneHere;
	/** Velikost kroku prùbìhu operace. Tj. po kolika krocích aktualizovat prùbìh. */
	private int progressStep;
	/** Váha jednoho kroku. */
	private double progressRatio;
	
	/**
	 * Konstruktor.
	 * @param p Indikátor prùbìhu k aktualizaci.
	 * @param progressStep Velikost kroku operace. Tj. po kolika krocích aktualizovat prùbìh.
	 */
	public ProgressUpdater(Subprogress p, int progressStep) {
		this(p, progressStep, 1);
	}
	
	/**
	 * Konstruktor.
	 * @param p Indikátor prùbìhu k aktualizaci.
	 * @param progressStep Velikost kroku operace. Tj. po kolika krocích aktualizovat prùbìh.
	 * @param progressRatio Váha jednoho kroku. Vhodné pro aktualizaci dílèí operace s urèitou oèekávanou dobou bìhu.
	 */	
	public ProgressUpdater(Subprogress p, int progressStep, double progressRatio) {
		this.p = p;
		this.progressStep = progressStep;
		this.progressRatio = progressRatio;
		this.counter = 0;
		this.doneHere = 0;
		this.initialDone = p.getDone();
	}
	
	/**
	 * Pøídá jeden krok operace.
	 */
	public void update() {
		if (++counter % progressStep == 0) {
			doneHere += progressStep * progressRatio;
			setProgress();
		}
	}
	
	/**
	 * Pøidá c krokù operace.
	 * @param c Poèet krokù k pøidání.
	 */
	public void update(long c) {
		long gap = progressStep - (counter % progressStep); /* kolik chybí do doplnìní na counteru na progressStep */
		
		if (c>=gap) {
			long overflow = c-gap; /* kolik zbývá po doplnìní mezery */
			long moves = 1 + overflow / progressStep;
			doneHere += moves * progressStep * progressRatio;
			setProgress();
		}
		
		counter += c;
	}
	
	/**
	 * Aktualizuje indikátor prùbìhu operace.
	 */
	private void setProgress() {
		p.addToCurrentlyDone((long)(doneHere-(p.getDone()-initialDone)));
	}
}
