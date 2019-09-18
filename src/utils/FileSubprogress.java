package utils;

import java.io.File;
import java.util.Vector;

/**
 * Indik�tor pr�b�hu operace zpracov�vaj�c� soubory.
 */
public class FileSubprogress extends Subprogress {
	/**
	 * Konstruktor.
	 */
	public FileSubprogress() {}
	
	/**
	 * Konstruktor.
	 * @param files Zpracov�van� soubory.
	 */
	public FileSubprogress(Vector<File> files) {
		super(FileSubprogressOperation.createOperations(files), false);
	}

	/**
	 * Inicializuje indik�tor pro dan� soubory.
	 * @param files Zpracov�van� soubory.
	 */
	public void initialize(Vector<File> files) {
		super.initialize(FileSubprogressOperation.createOperations(files), false);
	}
}
