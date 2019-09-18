package compressors;

import java.io.*;
import java.util.*;
import utils.*;
import exceptions.*;

/**
 * Kompresor pro shannon-fanùv kód.
 */
public class ShannonFanoCompressor implements Compressor, SafelyInterruptable {
	/** Velikost kroku indikace prùbìhu operací. */
	private static final int PROGRESS_STEP = 1024;
	
	/* Skupiny konstant PROGRESS_COMPRESS_*_RATIO a PROGRESS_DECOMPRESS_*_RATIO
	 * musí dát v souètu PROGRESS_COMPRESS_RATIO resp. PROGRESS_DECOMPRESS_RATIO
	 */
	
	/** Oèekávaný pomìr doby komprese k dobì archivace. */
	public static final double PROGRESS_COMPRESS_RATIO = 2.5;
	
	/** Oèekávaný pomìr doby ètení èetností znakù k dobì archivace. */
	private static final double PROGRESS_COMPRESS_READ_FREQUIENCIES_RATIO = 0.7;
	/** Oèekávaný pomìr doby vytváøení tabulky k dobì archivace. */
	private static final double PROGRESS_COMPRESS_BUILD_TABLE_RATIO = 0.01;
	/** Oèekávaný pomìr doby zápisu hlavièky k dobì archivace. */
	private static final double PROGRESS_COMPRESS_WRITE_HEADER_RATIO = 0.01;
	/** Oèekávaný pomìr doby zápisu tìla komprimovaného souboru k dobì archivace. */
	private static final double PROGRESS_COMPRESS_WRITE_BODY_RATIO = 2.28;
	
	/** Oèekávaný pomìr doby dekomprese k dobì dearchivace. */
	public static final double PROGRESS_DECOMPRESS_RATIO = 1.5;
	
	/** Oèekávaný pomìr doby ètení hlavièky k dobì archivace. */
	private static final double PROGRESS_DECOMPRESS_READ_HEADER_RATIO = 0.05;
	/** Oèekávaný pomìr doby ètení tìla zkomprimovaného souboru k dobì archivace. */	
	private static final double PROGRESS_DECOMPRESS_READ_BODY_RATIO = 1.45;
	
	
	/**
	 * Rekurzivnì vytvoøí tabulku kódù. 
	 * @param table Vytváøená tabulka.
	 * @param freq_sum_table Tabulka èásteèných souètù èetností znakù.
	 * Tzn. na indexu i je souèet všech èetností znakù 0..i.
	 * @param from Dolní index právì zkoumaného intervalu znakù.
	 * @param to Horní index právì zkoumaného intervalu znakù.
	 */
	private static void CreateCode(CodeRecord[] table, long[] freq_sum_table, int from, int to) {
		if (to-from==1) {
			return;
		}
		
		int i = 0;
		long firstFreq = ((from>0)? freq_sum_table[from-1] : 0);
		long lastFreq = freq_sum_table[to-1];
		long freqSum = lastFreq - firstFreq;
		long desiredFreq = lastFreq - freqSum/2;
		long prevDiff;
		long diff = desiredFreq - freq_sum_table[0];
		
		do {
			prevDiff = diff;
			i++;
			diff = desiredFreq - freq_sum_table[i];
		} while (diff>0);
		
		if (Math.abs(diff)>prevDiff) {
			i--;
		}
		
		for (int j=from; j<=i; j++) {
			table[j].addToCode(0);
		}
		for (int j=i+1; j<to; j++) {
			table[j].addToCode(1);
		}
		CreateCode(table, freq_sum_table, from, i+1);
		CreateCode(table, freq_sum_table, i+1, to);
	}
	
	/**
	 * Na základì neuspoøádaného pole záznamù s bitovými kódy vytvoøí pole záznamù indexované znaky v záznamech.
	 * @param table Neuspoøádané pole záznamù.
	 * @return Pole záznamù indexované znaky.
	 */
	private static CodeRecord[] IndexedTable(CodeRecord[] table) {
		CodeRecord[] result = new CodeRecord[256];
		
		for (int i=0; i<256; i++) {
			result[table[i].getChar()] = table[i];
		}
		
		return result;
	}

	/**
	 * Vytvoøí tabulku bitových kódù na základì tabulky èetností. 
	 * @param frequencies Tabulka èetností znakù.
	 * @return Tabulka bitových kódù.
	 */
	private static CodeTable BuildCodeTableByFrequencies(long[] frequencies) {
		CodeRecord[] table = new CodeRecord[256];
		long[] freqSumTable = new long[256];
		int alphabetSize = Utils.alphabetSize(frequencies);
		
		for (int i=0; i<256; i++) {
			table[i] = new CodeRecord((char)i, frequencies[i]);
		}
		
		Arrays.sort(table);
		
		freqSumTable[0] = table[0].getFrequency();
		for (int i=1; i<256; i++) {
			if (table[i].getFrequency()==0) {
				alphabetSize = i;
				break;
			}
			freqSumTable[i] = freqSumTable[i-1] + table[i].getFrequency();
		}
		
		freqSumTable = Arrays.copyOf(freqSumTable, alphabetSize);
		CreateCode(table, freqSumTable, 0, alphabetSize);
				
		return new CodeTable(IndexedTable(table), alphabetSize, freqSumTable[freqSumTable.length-1], freqSumTable[0]);
	}
	
	/**
	 * Vytvoøí tabulku bitových kódù pro soubor.
	 * @param in Vstupní soubor.
	 * @param p Indikátor prùbìhu operace.
	 * @param interrupt Indikátor pøerušení operace.
	 * @return Tabulku bitových kódù pro soubor.
	 * @throws IOException Chyba I/O.
	 */
	private static CodeTable BuildCodeTable(File in, Subprogress p, PackedBoolean interrupt) throws IOException {
		long[] frequencies = Utils.GetCharacterFrequencies(in, p, PROGRESS_COMPRESS_READ_FREQUIENCIES_RATIO, PROGRESS_STEP, interrupt);
		CodeTable table = BuildCodeTableByFrequencies(frequencies);
		
		p.addToCurrentlyDone((long)(p.getCurrentOperationTodo()*PROGRESS_COMPRESS_BUILD_TABLE_RATIO));
		
		return table;
	}
	
	/** Indikátor pøerušení operace. */
	private PackedBoolean interrupt;
	
	/**
	 * Konstruktor.
	 */
	public ShannonFanoCompressor() {
		interrupt = new PackedBoolean(false);
	}
	
	@Override
	public void Decompress(File in, File out)
			throws IOException, InvalidArchiveException {
		Decompress(in, out, null);
	}
	
	@Override
	public void Decompress(File in, File out, Subprogress p) throws IOException, InvalidArchiveException {
		interrupt.setValue(false);
		if (p==null) {
			SubprogressOperation operation = new BasicSubprogressOperation((long)(PROGRESS_DECOMPRESS_RATIO*in.length()), "Compressing... "+in.getName());
			SubprogressOperation[] operations = new SubprogressOperation[1];
			operations[0] = operation;
			p = new Subprogress(operations);
		}
		
		if (in.length()==0) {
			FileOutputStream os = new FileOutputStream(out, false);
			os.close();
			p.addToCurrentlyDone(p.getCurrentOperationTodo());
			return;
		}
		
		FileInputStream is = new FileInputStream(in);;
		BufferedInputStream bis = new BufferedInputStream(is);
		BitInputStream bitis = new BitInputStream(bis);
		FileOutputStream os = new FileOutputStream(out);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		
		long characterCount = 0;
		long[] frequencies;
		CodeTable table;
		CodeTree tree;
		
		try {
			frequencies = Utils.readHeader(bitis, interrupt);
			p.addToCurrentlyDone((long)(PROGRESS_DECOMPRESS_READ_HEADER_RATIO*p.getCurrentOperationTodo()));
		} catch (InvalidArchiveException e) {
			bitis.close();
			bos.close();
			out.delete();
			throw e;
		}
		
		table = BuildCodeTableByFrequencies(frequencies);
		tree = new CodeTree(table);
		characterCount = table.getCharacterCount();
		
		try {
			Utils.writeDecompressedBody(bitis, bos, tree, characterCount, p, PROGRESS_STEP, PROGRESS_DECOMPRESS_READ_BODY_RATIO, interrupt);
		} catch (InvalidArchiveException e) {
			bitis.close();
			bos.close();
			out.delete();
			throw e;
		}
		
		bitis.close();
		bos.close();
		interrupt.setValue(false);
	}
	
	@Override
	public void Compress(File in, File out) throws IOException {
		Compress(in, out, null);
	}
	
	@Override
	public void Compress(File in, File out, Subprogress p) throws IOException {
		interrupt.setValue(false);
		if (p==null) {
			SubprogressOperation operation = new BasicSubprogressOperation((long)(PROGRESS_COMPRESS_RATIO*in.length()), "Compressing... "+in.getName());
			SubprogressOperation[] operations = new SubprogressOperation[1];
			operations[0] = operation;
			p = new Subprogress(operations);
		}
		
		if (in.length()==0) {
			FileOutputStream os = new FileOutputStream(out, false);
			os.close();
			p.addToCurrentlyDone(p.getCurrentOperationTodo());
			return;
		}
		
		CodeTable table = BuildCodeTable(in, p, interrupt);
		
		FileInputStream is;
		FileOutputStream os;
		
		is = new FileInputStream(in);
		os = new FileOutputStream(out, false);

		BufferedInputStream bis = new BufferedInputStream(is);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		BitOutputStream bitos = new BitOutputStream(bos);
		
		Utils.writeHeader(bitos, table, interrupt);
		p.addToCurrentlyDone((long)(PROGRESS_COMPRESS_WRITE_HEADER_RATIO*p.getCurrentOperationTodo()));
		Utils.writeCompressedBody(bis, bitos, table, p, PROGRESS_COMPRESS_WRITE_BODY_RATIO, PROGRESS_STEP, interrupt);
		
		bis.close();
		bitos.close();
		interrupt.setValue(false);
	}
	
	@Override
	public void safelyInterrupt() {
		interrupt.setValue(true);
	}
}
