package workspace;

import java.io.*;
import java.util.Vector;

import compressors.*;
import exceptions.*;
import archiver.*;

/**
 * Pomocn� t��da s definic� soubor� pro testovac� archivaci a dearchivaci.
 */
class ArchiveFileNames {
	/** Vstupn� soubory pro archivaci. */
	Vector<File> src;
	/** V�sledn� archiv. */
	File dest;
	/** V�sledn� komprimovan� archiv. */
	File comp;
	/** V�sledn� zp�tn� dekomprimovan� archiv. */
	File decomp;
	/** Adres�� pro zp�tnou dearchivaci. */
	File unpack_dest;
	
	/**
	 * Konstruktor.
	 * @param file_name Jm�no souboru k archivaci.
	 */
	public ArchiveFileNames(String file_name) {
		this(file_name, file_name+".ar.unpacked");
	}
	
	/**
	 * Konstruktor.
	 * @param src_name Jm�no souboru k archivaci.
	 * @param unpack_dest_name Adres�� k zp�tn� dearchivaci.
	 */
	public ArchiveFileNames(String src_name, String unpack_dest_name) {
		this(new String[] {src_name}, src_name+".ar", unpack_dest_name);
	}
	
	/**
	 * Konstruktor.
	 * @param src_names Jm�na soubor� k archivaci.
	 * @param dest_name Z�klad jm�na archivu.
	 * @param unpack_dest_name Adres�� k zp�tn� dearchivaci.
	 */
	public ArchiveFileNames(String[] src_names, String dest_name, String unpack_dest_name) {
		src = new Vector<File>();
		
		for (int i=0; i<src_names.length; i++) {
			src.add(new File(src_names[i]));
		}
		this.dest = new File(dest_name);
		comp = new File(dest_name+".huff");
		decomp = new File(dest_name+".huff.decompressed");
		unpack_dest = new File(unpack_dest_name);
	}
}

/**
 * Pracovn� prostor pro testov�n� backendu programu, tj. archiveru a kompresor�.
 */
public class ArchiverWorkspace {
	/**
	 * Zabal� a rozbal� archiv.
	 * @param fn Soubory pro test.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatn� archiv.
	 */
	public static void PackUnpack(ArchiveFileNames fn) throws IOException, InvalidArchiveException {
		Archiver ar = new Archiver();
		Compressor cmpr = new HuffmanCompressor();
		
		System.out.println("Processing file '"+fn.src+"'");
		System.out.println("Packing...");
		ar.pack(fn.src, fn.dest);
		System.out.println("Compressing...");
		cmpr.Compress(fn.dest, fn.comp);
		System.out.println("Decompressing...");
		cmpr.Decompress(fn.comp, fn.decomp);
		System.out.println("Unpacking...");
		ar.unpack(fn.decomp, fn.unpack_dest);
		System.out.println("File successfully processed");
	}
	
	/**
	 * Hlavn� metoda testu.
	 * @param args Vstupn� parametry.
	 * @throws IOException Chyba I/O
	 * @throws InvalidArchiveException Neplatn� archiv.
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException, InvalidArchiveException {
		ArchiveFileNames soutoky = new ArchiveFileNames("f:\\soutoky", "f:\\temp");
		ArchiveFileNames programming = new ArchiveFileNames("f:\\programming", "f:\\temp");
		ArchiveFileNames html = new ArchiveFileNames("f:\\programming\\html", "f:\\temp");
		ArchiveFileNames cygwin = new ArchiveFileNames("c:\\cygwin", "f:\\temp");
		ArchiveFileNames canterbury = new ArchiveFileNames("p:\\canterbury_corpus", "f:\\temp");
		
		//PackUnpack(soutoky);
		//PackUnpack(canterbury);
		//PackUnpack(html);
		//PackUnpack(programming);
		PackUnpack(cygwin);
	}
}
