package workspace;

import java.io.*;
import java.util.Vector;

import compressors.*;
import exceptions.*;
import archiver.*;

/**
 * Pomocná tøída s definicí souborù pro testovací archivaci a dearchivaci.
 */
class ArchiveFileNames {
	/** Vstupní soubory pro archivaci. */
	Vector<File> src;
	/** Výsledný archiv. */
	File dest;
	/** Výsledný komprimovaný archiv. */
	File comp;
	/** Výsledný zpìtnì dekomprimovaný archiv. */
	File decomp;
	/** Adresáø pro zpìtnou dearchivaci. */
	File unpack_dest;
	
	/**
	 * Konstruktor.
	 * @param file_name Jméno souboru k archivaci.
	 */
	public ArchiveFileNames(String file_name) {
		this(file_name, file_name+".ar.unpacked");
	}
	
	/**
	 * Konstruktor.
	 * @param src_name Jméno souboru k archivaci.
	 * @param unpack_dest_name Adresáø k zpìtné dearchivaci.
	 */
	public ArchiveFileNames(String src_name, String unpack_dest_name) {
		this(new String[] {src_name}, src_name+".ar", unpack_dest_name);
	}
	
	/**
	 * Konstruktor.
	 * @param src_names Jména souborù k archivaci.
	 * @param dest_name Základ jména archivu.
	 * @param unpack_dest_name Adresáø k zpìtné dearchivaci.
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
 * Pracovní prostor pro testování backendu programu, tj. archiveru a kompresorù.
 */
public class ArchiverWorkspace {
	/**
	 * Zabalí a rozbalí archiv.
	 * @param fn Soubory pro test.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatný archiv.
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
	 * Hlavní metoda testu.
	 * @param args Vstupní parametry.
	 * @throws IOException Chyba I/O
	 * @throws InvalidArchiveException Neplatný archiv.
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
