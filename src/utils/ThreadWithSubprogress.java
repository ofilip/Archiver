package utils;

/**
 * T��da definuj�c� vl�kno s indik�torem pr�b�hu d�l��ch operac�.
 */
public abstract class ThreadWithSubprogress extends Thread {
	/**
	 * Vrac� indik�tor pr�b�hu d�l��ch operac�.
	 * @return Indik�tor pr�b�hu d�l��ch operac�.
	 */
	public abstract Subprogress getSubprogress();
}
