package utils;

/**
 * Tøída definující vlákno s indikátorem prùbìhu.
 */
public abstract class ThreadWithProgress extends Thread {
	/**
	 * Vrací indikátor prùbìhu operace.
	 * @return Indikátor prùbìhu operace.
	 */
	public abstract Progress getProgress();
}
