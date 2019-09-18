package compressors;

import java.io.File;
import java.io.IOException;
import utils.Subprogress;
import exceptions.InvalidArchiveException;

/**
 * Kompresor - rozhraní definující èlenské funkce tøídy pro kompresi a dekompresi souborù.
 */
public interface Compressor {
	/**
	 * Zkomprimuje soubor.
	 * @param in Komprimovaný soubor
	 * @param out Soubor vzniknuvší kompresí.
	 * @throws IOException Chyba I/O.
	 */
	public void Compress(File in, File out) throws IOException;
	
	/**
	 * Zkomprimuje soubor.
	 * @param in Komprimovaný soubor
	 * @param out Soubor vzniknuvší kompresí.
	 * @param p Indikátor prùbìhu operace.
	 * @throws IOException Chyba I/O.
	 */
	public void Compress(File in, File out, Subprogress p) throws IOException;
	
	/**
	 * Dekomprimuje soubor.
	 * @param in Zkomprimovaný soubor.
	 * @param out Soubor vzniknuvší dekompresí.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatný archiv.
	 */
	public void Decompress(File in, File out) throws IOException, InvalidArchiveException;
	
	/**
	 * Dekomprimuje soubor.
	 * @param in Zkomprimovaný soubor.
	 * @param out Soubor vzniknuvší dekompresí.
	 * @param p Indikátor prùbìhu operace.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatný archiv.
	 */
	public void Decompress(File in, File out, Subprogress p) throws IOException, InvalidArchiveException;
}
