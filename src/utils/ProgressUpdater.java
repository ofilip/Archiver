package utils;

/**
 * T��da pro snaz�� aktualizaci pr�b�hu v t��d� Subprogress.
 */
public class ProgressUpdater {
	/** Aktualizovan� indik�tor pr�b�hu. */
	private Subprogress p;
	/** ��ta� krok�. */
	private long counter;
	/** Po��te�n� po�et ji� proveden�ch krok�. */
	private long initialDone;
	/** Po�et krok� proveden�ch t�mto updaterem. */
	private double doneHere;
	/** Velikost kroku pr�b�hu operace. Tj. po kolika kroc�ch aktualizovat pr�b�h. */
	private int progressStep;
	/** V�ha jednoho kroku. */
	private double progressRatio;
	
	/**
	 * Konstruktor.
	 * @param p Indik�tor pr�b�hu k aktualizaci.
	 * @param progressStep Velikost kroku operace. Tj. po kolika kroc�ch aktualizovat pr�b�h.
	 */
	public ProgressUpdater(Subprogress p, int progressStep) {
		this(p, progressStep, 1);
	}
	
	/**
	 * Konstruktor.
	 * @param p Indik�tor pr�b�hu k aktualizaci.
	 * @param progressStep Velikost kroku operace. Tj. po kolika kroc�ch aktualizovat pr�b�h.
	 * @param progressRatio V�ha jednoho kroku. Vhodn� pro aktualizaci d�l�� operace s ur�itou o�ek�vanou dobou b�hu.
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
	 * P��d� jeden krok operace.
	 */
	public void update() {
		if (++counter % progressStep == 0) {
			doneHere += progressStep * progressRatio;
			setProgress();
		}
	}
	
	/**
	 * P�id� c krok� operace.
	 * @param c Po�et krok� k p�id�n�.
	 */
	public void update(long c) {
		long gap = progressStep - (counter % progressStep); /* kolik chyb� do dopln�n� na counteru na progressStep */
		
		if (c>=gap) {
			long overflow = c-gap; /* kolik zb�v� po dopln�n� mezery */
			long moves = 1 + overflow / progressStep;
			doneHere += moves * progressStep * progressRatio;
			setProgress();
		}
		
		counter += c;
	}
	
	/**
	 * Aktualizuje indik�tor pr�b�hu operace.
	 */
	private void setProgress() {
		p.addToCurrentlyDone((long)(doneHere-(p.getDone()-initialDone)));
	}
}
