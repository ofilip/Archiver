package compressors;

import java.io.*;
import java.util.*;
import utils.*;
import exceptions.*;

/**
 * Kompresor pro shannon-fan�v k�d.
 */
public class ShannonFanoCompressor implements Compressor, SafelyInterruptable {
	/** Velikost kroku indikace pr�b�hu operac�. */
	private static final int PROGRESS_STEP = 1024;
	
	/* Skupiny konstant PROGRESS_COMPRESS_*_RATIO a PROGRESS_DECOMPRESS_*_RATIO
	 * mus� d�t v sou�tu PROGRESS_COMPRESS_RATIO resp. PROGRESS_DECOMPRESS_RATIO
	 */
	
	/** O�ek�van� pom�r doby komprese k dob� archivace. */
	public static final double PROGRESS_COMPRESS_RATIO = 2.5;
	
	/** O�ek�van� pom�r doby �ten� �etnost� znak� k dob� archivace. */
	private static final double PROGRESS_COMPRESS_READ_FREQUIENCIES_RATIO = 0.7;
	/** O�ek�van� pom�r doby vytv��en� tabulky k dob� archivace. */
	private static final double PROGRESS_COMPRESS_BUILD_TABLE_RATIO = 0.01;
	/** O�ek�van� pom�r doby z�pisu hlavi�ky k dob� archivace. */
	private static final double PROGRESS_COMPRESS_WRITE_HEADER_RATIO = 0.01;
	/** O�ek�van� pom�r doby z�pisu t�la komprimovan�ho souboru k dob� archivace. */
	private static final double PROGRESS_COMPRESS_WRITE_BODY_RATIO = 2.28;
	
	/** O�ek�van� pom�r doby dekomprese k dob� dearchivace. */
	public static final double PROGRESS_DECOMPRESS_RATIO = 1.5;
	
	/** O�ek�van� pom�r doby �ten� hlavi�ky k dob� archivace. */
	private static final double PROGRESS_DECOMPRESS_READ_HEADER_RATIO = 0.05;
	/** O�ek�van� pom�r doby �ten� t�la zkomprimovan�ho souboru k dob� archivace. */	
	private static final double PROGRESS_DECOMPRESS_READ_BODY_RATIO = 1.45;
	
	
	/**
	 * Rekurzivn� vytvo�� tabulku k�d�. 
	 * @param table Vytv��en� tabulka.
	 * @param freq_sum_table Tabulka ��ste�n�ch sou�t� �etnost� znak�.
	 * Tzn. na indexu i je sou�et v�ech �etnost� znak� 0..i.
	 * @param from Doln� index pr�v� zkouman�ho intervalu znak�.
	 * @param to Horn� index pr�v� zkouman�ho intervalu znak�.
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
	 * Na z�klad� neuspo��dan�ho pole z�znam� s bitov�mi k�dy vytvo�� pole z�znam� indexovan� znaky v z�znamech.
	 * @param table Neuspo��dan� pole z�znam�.
	 * @return Pole z�znam� indexovan� znaky.
	 */
	private static CodeRecord[] IndexedTable(CodeRecord[] table) {
		CodeRecord[] result = new CodeRecord[256];
		
		for (int i=0; i<256; i++) {
			result[table[i].getChar()] = table[i];
		}
		
		return result;
	}

	/**
	 * Vytvo�� tabulku bitov�ch k�d� na z�klad� tabulky �etnost�. 
	 * @param frequencies Tabulka �etnost� znak�.
	 * @return Tabulka bitov�ch k�d�.
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
	 * Vytvo�� tabulku bitov�ch k�d� pro soubor.
	 * @param in Vstupn� soubor.
	 * @param p Indik�tor pr�b�hu operace.
	 * @param interrupt Indik�tor p�eru�en� operace.
	 * @return Tabulku bitov�ch k�d� pro soubor.
	 * @throws IOException Chyba I/O.
	 */
	private static CodeTable BuildCodeTable(File in, Subprogress p, PackedBoolean interrupt) throws IOException {
		long[] frequencies = Utils.GetCharacterFrequencies(in, p, PROGRESS_COMPRESS_READ_FREQUIENCIES_RATIO, PROGRESS_STEP, interrupt);
		CodeTable table = BuildCodeTableByFrequencies(frequencies);
		
		p.addToCurrentlyDone((long)(p.getCurrentOperationTodo()*PROGRESS_COMPRESS_BUILD_TABLE_RATIO));
		
		return table;
	}
	
	/** Indik�tor p�eru�en� operace. */
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
