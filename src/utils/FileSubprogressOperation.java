package utils;

import java.io.*;
import java.util.*;

/**
 * Indik�tor pr�b�hu d�l�� operace se souborem.
 */
public class FileSubprogressOperation implements SubprogressOperation {
	/** Zpracov�van� soubor. */
	private File f;
	/** Velikost zpracov�van�ho souboru. */
	private long filesize;
	/** Jm�no zpracov�van�ho souboru. */
	private String filename;
	
	/**
	 * Konstruktor.
	 * @param f Soubor ke zpracov�n�.
	 */
	public FileSubprogressOperation(File f) {
		this.f = f;
		this.filesize = f.length();
		this.filename = f.getName();
	}
	
	@Override
	public long getTodo() {
		return Math.max(1,filesize);
	}

	@Override
	public String getName() {
		return filename;
	}

	/**
	 * Vrac� zpracov�van� soubor.
	 * @return Zpracov�van� soubor.
	 */
	public File getFile() {
		return f;
	}
	
	/**
	 * Vytvo�� pole indik�tor� operac� se souborem na z�klad� pole soubor�.
	 * @param files Pole soubor� ke zpracov�n�.
	 * @return Pole indik�tor�.
	 */
	public static FileSubprogressOperation[] createOperations(Vector<File> files) {
		FileSubprogressOperation[] result = new FileSubprogressOperation[files.size()];
		
		int i = 0;
		for (File f: files) {
			result[i] = new FileSubprogressOperation(f);
			i++;
		}
		
		return result;
	}
}
