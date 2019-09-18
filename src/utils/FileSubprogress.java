package utils;

import java.io.File;
import java.util.Vector;

/**
 * Indikátor prùbìhu operace zpracovávající soubory.
 */
public class FileSubprogress extends Subprogress {
	/**
	 * Konstruktor.
	 */
	public FileSubprogress() {}
	
	/**
	 * Konstruktor.
	 * @param files Zpracovávané soubory.
	 */
	public FileSubprogress(Vector<File> files) {
		super(FileSubprogressOperation.createOperations(files), false);
	}

	/**
	 * Inicializuje indikátor pro dané soubory.
	 * @param files Zpracovávané soubory.
	 */
	public void initialize(Vector<File> files) {
		super.initialize(FileSubprogressOperation.createOperations(files), false);
	}
}
