package utils;

/**
 * Tøída definující vlákno s indikátorem prùbìhu dílèích operací.
 */
public abstract class ThreadWithSubprogress extends Thread {
	/**
	 * Vrací indikátor prùbìhu dílèích operací.
	 * @return Indikátor prùbìhu dílèích operací.
	 */
	public abstract Subprogress getSubprogress();
}
