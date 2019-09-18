package utils;

import java.io.*;
import java.util.*;

/**
 * Indikátor prùbìhu dílèí operace se souborem.
 */
public class FileSubprogressOperation implements SubprogressOperation {
	/** Zpracovávaný soubor. */
	private File f;
	/** Velikost zpracovávaného souboru. */
	private long filesize;
	/** Jméno zpracovávaného souboru. */
	private String filename;
	
	/**
	 * Konstruktor.
	 * @param f Soubor ke zpracování.
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
	 * Vrací zpracovávaný soubor.
	 * @return Zpracovávaný soubor.
	 */
	public File getFile() {
		return f;
	}
	
	/**
	 * Vytvoøí pole indikátorù operací se souborem na základì pole souborù.
	 * @param files Pole souborù ke zpracování.
	 * @return Pole indikátorù.
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
