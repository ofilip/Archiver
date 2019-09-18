package compressors;

import java.io.File;
import java.io.IOException;
import utils.Subprogress;
import exceptions.InvalidArchiveException;

/**
 * Kompresor - rozhran� definuj�c� �lensk� funkce t��dy pro kompresi a dekompresi soubor�.
 */
public interface Compressor {
	/**
	 * Zkomprimuje soubor.
	 * @param in Komprimovan� soubor
	 * @param out Soubor vzniknuv�� kompres�.
	 * @throws IOException Chyba I/O.
	 */
	public void Compress(File in, File out) throws IOException;
	
	/**
	 * Zkomprimuje soubor.
	 * @param in Komprimovan� soubor
	 * @param out Soubor vzniknuv�� kompres�.
	 * @param p Indik�tor pr�b�hu operace.
	 * @throws IOException Chyba I/O.
	 */
	public void Compress(File in, File out, Subprogress p) throws IOException;
	
	/**
	 * Dekomprimuje soubor.
	 * @param in Zkomprimovan� soubor.
	 * @param out Soubor vzniknuv�� dekompres�.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatn� archiv.
	 */
	public void Decompress(File in, File out) throws IOException, InvalidArchiveException;
	
	/**
	 * Dekomprimuje soubor.
	 * @param in Zkomprimovan� soubor.
	 * @param out Soubor vzniknuv�� dekompres�.
	 * @param p Indik�tor pr�b�hu operace.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatn� archiv.
	 */
	public void Decompress(File in, File out, Subprogress p) throws IOException, InvalidArchiveException;
}
