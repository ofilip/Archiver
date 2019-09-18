package workspace;

import java.io.*;
import java.util.*;

import utils.Utils;

import compressors.*;

import exceptions.InvalidArchiveException;

/**
 * Seznam p��pon metod komprese.
 *
 */
enum Extensions {
	/** Metoda shannon-fano. */
	SF {
		@Override
		public String toString() { return "sf"; }
		},
	/** Statick� huffman�v k�d. */
	HUFF {
		@Override
		public String toString() { return "huff"; }
		},
	/** Adaptivn� huffman�v k�d. */
	FGK {
		@Override
		public String toString() { return "fgk"; }
		}
}

/**
 * Seznam mo�n�ch kombinac� operac�.
 */
enum Actions {
	/** Pouze komprimuj. */
	COMPRESS,
	/** Komprimuj a dekomprimuj. */
	BOTH
}

/**
 * T��da se soubory pro testov�n� komprese a dekomprese v�emi metodami.
 */
class FileNames {
	/** Soubor pro kompresi. */
	File orig;
	/** Soubor zkomprimovan� metodou SF. */
	File comp_sf;
	/** Soubor zkomprimovan� metodou HUFF. */
	File comp_huff;
	/** Soubor zkomprimovan� metodou FGK. */
	File comp_fgk;
	/** Soubor dekomprimovan� metodou SF. */
	File decomp_sf;
	/** Soubor dekomprimovan� metodou HUFF. */
	File decomp_huff;
	/** Soubor dekomprimovan� metodou FGK. */
	File decomp_fgk;
	
	/**
	 * Konstruktor.
	 * @param file_name Jm�no komprimovan�ho souboru.
	 */
	public FileNames(String file_name) {
		orig = new File(file_name);
		comp_sf = new File(file_name+".sf");
		decomp_sf = new File(file_name+".sf.decompressed");
		comp_huff = new File(file_name+".huff");
		decomp_huff = new File(file_name+".huff.decompressed");
		comp_fgk = new File(file_name+".fgk");
		decomp_fgk = new File(file_name+".fgk.decompressed");
	}
	
	/**
	 * Vrac� komprimovan� soubor.
	 * @return Komprimovan� soubor.
	 */
	public File Orig() {
		return orig;
	}
	
	/**
	 * Vrac� soubor zkomprimovan� danou metodou.
	 * @param ext Metoda komprese.
	 * @return Soubor zkomprimovan� danou metodou.
	 */
	public File Comp(Extensions ext) {
		switch (ext) {
			case SF:
				return comp_sf;
			case HUFF:
				return comp_huff;
			case FGK:
				return comp_fgk;
		}
		return null;
	}

	/**
	 * Vrac� soubor dekomprimovan� danou metodou.
	 * @param ext Metoda komprese.
	 * @return Soubor dekomprimovan� danou metodou.
	 */
	public File Decomp(Extensions ext) {
		switch (ext) {
			case SF:
				return decomp_sf;
			case HUFF:
				return decomp_huff;
			case FGK:
				return decomp_fgk;
		}
		return null;
	}
}

/**
 * Z�znam �daj� o kompresi.
 */
class StatRecord {
	/** Jm�no komprimovan�ho souboru. */
	String filename;
	/** Metoda komprese. */
	Extensions method;
	/** Velikost abecedy komprimovan�ho souboru. */
	int alphabetSize;
	/** P�vodn� velikost souboru. */
	long origSize;
	/** Velikost komprimovan�ho souboru. */
	long compSize;
	/** Doba b�hu komprese v milisekund�ch */
	long compRunningTime;
	/** Doba b�hu dekomprese v milisekund�ch. */
	long decompRunningTime;
	
	/**
	 * Konstruktor.
	 * @param filename Jm�no komprimovan�ho souboru.
	 * @param method Metoda komprese. 
	 * @param alphabetSize Velikost abecedy komprimovan�ho souboru.
	 * @param origSize P�vodn� velikost souboru.
	 * @param compSize Velikost komprimovan�ho souboru.
	 * @param compRunningTime Doba b�hu komprese v milisekund�ch.
	 * @param decompRunningTime Doba b�hu dekomprese v milisekund�ch.
	 */
	public StatRecord(String filename, Extensions method, int alphabetSize, long origSize, long compSize,
			long compRunningTime, long decompRunningTime) {
		this.filename = filename;
		this.method = method;
		this.alphabetSize = alphabetSize;
		this.origSize = origSize;
		this.compSize = compSize;
		this.compRunningTime = compRunningTime;
		this.decompRunningTime = decompRunningTime;
	}
	
	/**
	 * P�id� �asy k dob� komprese/dekomprese.  Vhodn� pro po��d�n� pr�m�rn� doby z v�ce pokus�.
	 * @param compRunningTime Milisekundy p�idan� k dob� komprese.
	 * @param decompRunningTime Milisekundy p�idan� k dob� dekomprese.
	 */
	void Add(long compRunningTime, long decompRunningTime) {
		this.compRunningTime += compRunningTime;
		this.decompRunningTime += decompRunningTime;
	}
	
	/**
	 * Vyd�l� dobu komprese a dekomprese. Vhodn� pro po��d�n� pr�m�rn� doby z v�ce pokus�.
	 * @param i ��slo, jak�m je d�leno.
	 */
	void DivideBy(double i) {
		compRunningTime /= i;
		decompRunningTime /= i;
	}
	
	/**
	 * Vrac� textovou reprezentaci z�znamu o kompresi/dekompresi v�etn� dal��ch odvozen�ch statistik.
	 * @return Textov reprezentace z�znamu.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append(filename);
		str.append(";");
		str.append(method);
		str.append(";");
		str.append(alphabetSize);
		str.append(";");
		str.append(origSize);
		str.append(";");
		str.append(compSize);
		str.append(";");
		str.append(compSize/(double)origSize);
		str.append(";");
		str.append(compRunningTime);
		str.append(";");
		str.append(origSize*1000/(double)(1024*compRunningTime));
		str.append(";");
		str.append(decompRunningTime);
		str.append(";");
		str.append(compSize*1000/(double)(1024*decompRunningTime));			
		
		return str.toString();
	}
}

/**
 * T��da pro testov�n� kompresn�ch vlastnostn�.
 */
public class CorpusCompression {
	/** File writer. */
	public static FileWriter fw;
	/** Asociativn� pole se z�znamy o jednotliv�ch testech. */
	public static Map<String, StatRecord> stats = new LinkedHashMap<String, StatRecord>(); 
	
	/**
	 * Provede test komprese a dekomprese a v�sledek p�id� do asociativn�ho pole.
	 * @param fn Jm�na soubor� pro test.
	 * @param ext Metoda komprese.
	 * @param action Proveden� akce.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Chyba archivu.
	 */
	public static void CompressDecompress(FileNames fn, Extensions ext, Actions action) throws IOException, InvalidArchiveException {
		File orig = fn.Orig();
		File comp = fn.Comp(ext);
		File decomp = fn.Decomp(ext);
		long origSize = orig.length();
		long compRunningTime = 0;
		long decompRunningTime = 0;
		long compSize = 0;
		String filename = orig.getName();
		String statsKey = filename+'/'+ext;
		Compressor cmpr;
		
		switch (ext) {
		case SF:
			cmpr = new ShannonFanoCompressor();
			break;
		case HUFF:
			cmpr = new HuffmanCompressor();
			break;
		default:
			cmpr = new FGKCompressor();
			break;
		}
		
		if (action==Actions.COMPRESS||action==Actions.BOTH) {

			long before = System.currentTimeMillis();
			
			cmpr.Compress(orig, comp);
			
			compSize = comp.length();
			
			long after = System.currentTimeMillis();
			compRunningTime = after - before;
		}
		
		if (action==Actions.BOTH) {
			long before = System.currentTimeMillis();
			
			cmpr.Decompress(comp, decomp);
			
			long after = System.currentTimeMillis();
			decompRunningTime = after - before;
			
			decomp.delete();
		}
		

		StatRecord record;
		StatRecord prevRecord = stats.get(statsKey);
		
		if (prevRecord!=null) {
			record = new StatRecord(filename, ext, prevRecord.alphabetSize, origSize, compSize, compRunningTime, decompRunningTime);
			prevRecord.Add(compRunningTime, decompRunningTime);
		} else {
			int alphabetSize = Utils.alphabetSize(Utils.GetCharacterFrequencies(orig));
			record = new StatRecord(filename, ext, alphabetSize, origSize, compSize, compRunningTime, decompRunningTime);
			stats.put(statsKey, record);
		}
		
		fw.write(record.toString());
		fw.write("\n");
		System.out.println(record.toString());
		comp.delete();
	}
	
	/**
	 * Provede test komprese/dekompre pro v�echny metody.
	 * @param fn Soubory pro testov�n�.
	 * @param action Proveden� akce.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatn� archiv.
	 */
	public static void CompressDecompressAll(FileNames fn, Actions action) throws IOException, InvalidArchiveException {
		CompressDecompress(fn, Extensions.SF, action);
		CompressDecompress(fn, Extensions.HUFF, action);
		CompressDecompress(fn, Extensions.FGK, action);
	}
	
	/**
	 * Provede test komprese/dekomprese v�emi metodami pro v�echny soubory v adres��i.
	 * @param dir Adres�� se soubory.
	 * @param action Proveden� akce.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatn� archvi.
	 */
	public static void CompressDirectory(File dir, Actions action) throws IOException, InvalidArchiveException {
		File[] files = dir.listFiles();
		
		for (File f: files) {
			if (f.isDirectory()) {
				CompressDirectory(f, action);
			} else {
				if (f.isFile()) {
					String filename = f.getPath();
					
					if (!filename.endsWith(".decompressed")&&!filename.endsWith(".sf")&&!filename.endsWith(".huff")&&!filename.endsWith(".fgk")&&!filename.endsWith(".gz")) {
						FileNames fn = new FileNames(f.getAbsolutePath());
						
						CompressDecompressAll(fn, action);
					}
				}
			}
		}
	}

	/**
	 * T�lo testu.
	 * @param args Vstupn� parametry.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatn� archiv. 
	 */
	public static void main(String[] args) throws IOException, InvalidArchiveException {
		final int cantenburyRepeats = 10;
		final int pizzaChiliRepeats = 3;
		File f;
		
		for (int i=0; i<cantenburyRepeats; i++) {
			f = new File("log_cant."+i);
			fw = new FileWriter(f);
			CompressDirectory(new File("p:\\canterbury_corpus"), Actions.BOTH);
			fw.close();
		}
		
		f = new File("log_cant.avg");
		fw = new FileWriter(f);
		for (StatRecord record: stats.values()) {
			record.DivideBy(cantenburyRepeats);
			fw.write(record.toString());
			fw.write("\n");
		}
		fw.close();
		stats.clear();
		
		for (int i=0; i<pizzaChiliRepeats; i++) {
			f = new File("log_pizz."+i);
			fw = new FileWriter(f);
			CompressDirectory(new File("p:\\pizza_chili_corpus"), Actions.BOTH);
			CompressDecompressAll(new FileNames("p:\\pizza_chili_corpus\\dblp.xml.gz"), Actions.BOTH);
			fw.close();
		}
		
		f = new File("log_pizz.avg");
		fw = new FileWriter(f);
		for (StatRecord record: stats.values()) {
			record.DivideBy(pizzaChiliRepeats);
			fw.write(record.toString());
			fw.write("\n");
		}
		fw.close();
		stats.clear();
	}
}
