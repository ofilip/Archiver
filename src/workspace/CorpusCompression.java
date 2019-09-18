package workspace;

import java.io.*;
import java.util.*;

import utils.Utils;

import compressors.*;

import exceptions.InvalidArchiveException;

/**
 * Seznam pøípon metod komprese.
 *
 */
enum Extensions {
	/** Metoda shannon-fano. */
	SF {
		@Override
		public String toString() { return "sf"; }
		},
	/** Statický huffmanùv kód. */
	HUFF {
		@Override
		public String toString() { return "huff"; }
		},
	/** Adaptivní huffmanùv kód. */
	FGK {
		@Override
		public String toString() { return "fgk"; }
		}
}

/**
 * Seznam možných kombinací operací.
 */
enum Actions {
	/** Pouze komprimuj. */
	COMPRESS,
	/** Komprimuj a dekomprimuj. */
	BOTH
}

/**
 * Tøída se soubory pro testování komprese a dekomprese všemi metodami.
 */
class FileNames {
	/** Soubor pro kompresi. */
	File orig;
	/** Soubor zkomprimovaný metodou SF. */
	File comp_sf;
	/** Soubor zkomprimovaný metodou HUFF. */
	File comp_huff;
	/** Soubor zkomprimovaný metodou FGK. */
	File comp_fgk;
	/** Soubor dekomprimovaný metodou SF. */
	File decomp_sf;
	/** Soubor dekomprimovaný metodou HUFF. */
	File decomp_huff;
	/** Soubor dekomprimovaný metodou FGK. */
	File decomp_fgk;
	
	/**
	 * Konstruktor.
	 * @param file_name Jméno komprimovaného souboru.
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
	 * Vrací komprimovaný soubor.
	 * @return Komprimovaný soubor.
	 */
	public File Orig() {
		return orig;
	}
	
	/**
	 * Vrací soubor zkomprimovaný danou metodou.
	 * @param ext Metoda komprese.
	 * @return Soubor zkomprimovaný danou metodou.
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
	 * Vrací soubor dekomprimovaný danou metodou.
	 * @param ext Metoda komprese.
	 * @return Soubor dekomprimovaný danou metodou.
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
 * Záznam údajù o kompresi.
 */
class StatRecord {
	/** Jméno komprimovaného souboru. */
	String filename;
	/** Metoda komprese. */
	Extensions method;
	/** Velikost abecedy komprimovaného souboru. */
	int alphabetSize;
	/** Pùvodní velikost souboru. */
	long origSize;
	/** Velikost komprimovaného souboru. */
	long compSize;
	/** Doba bìhu komprese v milisekundách */
	long compRunningTime;
	/** Doba bìhu dekomprese v milisekundách. */
	long decompRunningTime;
	
	/**
	 * Konstruktor.
	 * @param filename Jméno komprimovaného souboru.
	 * @param method Metoda komprese. 
	 * @param alphabetSize Velikost abecedy komprimovaného souboru.
	 * @param origSize Pùvodní velikost souboru.
	 * @param compSize Velikost komprimovaného souboru.
	 * @param compRunningTime Doba bìhu komprese v milisekundách.
	 * @param decompRunningTime Doba bìhu dekomprese v milisekundách.
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
	 * Pøidá èasy k dobì komprese/dekomprese.  Vhodné pro poèídání prùmìrné doby z více pokusù.
	 * @param compRunningTime Milisekundy pøidané k dobì komprese.
	 * @param decompRunningTime Milisekundy pøidané k dobì dekomprese.
	 */
	void Add(long compRunningTime, long decompRunningTime) {
		this.compRunningTime += compRunningTime;
		this.decompRunningTime += decompRunningTime;
	}
	
	/**
	 * Vydìlí dobu komprese a dekomprese. Vhodné pro poèídání prùmìrné doby z více pokusù.
	 * @param i Èíslo, jakým je dìleno.
	 */
	void DivideBy(double i) {
		compRunningTime /= i;
		decompRunningTime /= i;
	}
	
	/**
	 * Vrací textovou reprezentaci záznamu o kompresi/dekompresi vèetnì dalších odvozených statistik.
	 * @return Textov reprezentace záznamu.
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
 * Tøída pro testování kompresních vlastnostní.
 */
public class CorpusCompression {
	/** File writer. */
	public static FileWriter fw;
	/** Asociativní pole se záznamy o jednotlivých testech. */
	public static Map<String, StatRecord> stats = new LinkedHashMap<String, StatRecord>(); 
	
	/**
	 * Provede test komprese a dekomprese a výsledek pøidá do asociativního pole.
	 * @param fn Jména souborù pro test.
	 * @param ext Metoda komprese.
	 * @param action Provedené akce.
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
	 * Provede test komprese/dekompre pro všechny metody.
	 * @param fn Soubory pro testování.
	 * @param action Provedené akce.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatný archiv.
	 */
	public static void CompressDecompressAll(FileNames fn, Actions action) throws IOException, InvalidArchiveException {
		CompressDecompress(fn, Extensions.SF, action);
		CompressDecompress(fn, Extensions.HUFF, action);
		CompressDecompress(fn, Extensions.FGK, action);
	}
	
	/**
	 * Provede test komprese/dekomprese všemi metodami pro všechny soubory v adresáøi.
	 * @param dir Adresáø se soubory.
	 * @param action Provedené akce.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatný archvi.
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
	 * Tìlo testu.
	 * @param args Vstupní parametry.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatný archiv. 
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
