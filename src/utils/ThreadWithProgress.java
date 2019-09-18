package utils;

/**
 * T��da definuj�c� vl�kno s indik�torem pr�b�hu.
 */
public abstract class ThreadWithProgress extends Thread {
	/**
	 * Vrac� indik�tor pr�b�hu operace.
	 * @return Indik�tor pr�b�hu operace.
	 */
	public abstract Progress getProgress();
}
