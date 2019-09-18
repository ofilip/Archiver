package utils;

import java.io.*;
import java.util.*;
import compressors.*;
import exceptions.InvalidArchiveException;

/**
 * Tøída obsahující sadu statických metod využívaných napøíè aplikací.
 */
public class Utils {
	/**
	 * Rozšíøí bitový kód o jeden bit.
	 * @param code Pùvodní bitový kód.
	 * @param clen Délka pùvodního bitového kódu.
	 * @param bit Nový bit.
	 * @return Vzniklý bitový kód.
	 */
	public static byte[] AddToCode(byte[] code, int clen, int bit) {
		if (clen%8==0) {
			byte[] new_code = new byte[1+code.length];
			for (int i=0; i<code.length; i++) {
				new_code[i] = code[i];
			}
			new_code[code.length] = 0;
			code = new_code;
		}

		if (bit!=0) {
			byte b = (byte)(1 << (clen%8));
			code[code.length-1] |= b;
		}
		
		return code;
	}
	
	/**
	 * Vytvoøí èetnostní tabulku znakù pro daný soubor.
	 * @param f Vstupní soubor.
	 * @param p Indikátor prùbìhu operace.
	 * @param progressRatio Váha operace.
	 * @param progressStep Velikost kroku pro aktualizaci operace.
	 * @param interrupt Indikátor pøerušení operace.
	 * @return Èetnostní tabulka znakù pro soubor.
	 * @throws IOException Chyba I/O.
	 */
	public static long[] GetCharacterFrequencies(File f, Subprogress p, double progressRatio, int progressStep, PackedBoolean interrupt) throws IOException {
		long[] result = new long[256];
		Arrays.fill(result, 0);
		
		FileInputStream is;
		
		is = new FileInputStream(f);
		
		BufferedInputStream bis = new BufferedInputStream(is);
		
		ProgressUpdater u = new ProgressUpdater(p, progressStep, progressRatio);
		int c;
		while (!interrupt.getValue()&&-1!=(c = bis.read())) {
			result[(char)c]++;
			u.update();
		}
		
		bis.close();
		return result;
	}
	
	/**
	 * Vytvoøí èetnostní tabulku znakù pro daný soubor.
	 * @param f Vstupní soubor.
	 * @return Èetnostní tabulka znakù pro soubor.
	 * @throws IOException Chyba I/O.
	 */
	public static long[] GetCharacterFrequencies(File f) throws IOException {
		long[] result = new long[256];
		Arrays.fill(result, 0);
		
		FileInputStream is;
		
		is = new FileInputStream(f);
		
		BufferedInputStream bis = new BufferedInputStream(is);
		
		int c;
		while (-1!=(c = bis.read())) {
			result[(char)c]++;
		}
		
		bis.close();
		return result;
	}
	
	/**
	 * Spoèítá velikost abecedy na základì èetnostní tabulky znakù.
	 * @param frequencies Èetnostní tabulka znakù.
	 * @return Velikost abecedy.
	 */
	public static int alphabetSize(long[] frequencies) {
		int result = 0;
		
		for (int i=0; i<frequencies.length; i++) {
			if (frequencies[i]>0) {
				result++;
			}
		}
		
		return result;
	}
	
	/**
	 * Spoèítá poèet znakù na základì èetnostní tabulky znakù.
	 * @param frequencies Èetnostní tabulka znakù.
	 * @return Poèet znakù.
	 */
	public static long characterCount(long[] frequencies) {
		long result = 0;
		
		for (int i=0; i<frequencies.length; i++) {
			if (frequencies[i]>0) {
				result += frequencies[i];
			}
		}
		
		return result;
	}
	
	/**
	 * Zapíše hlavièku èetnostního kódu.
	 * @param out Výstupní proud pro zápis.
	 * @param codeTable Tabulka bitových kódù.
	 * @param interrupt Indikátor pøerušení.
	 * @throws IOException Chyba I/O.
	 */
	public static void writeHeader(BitOutputStream out, CodeTable codeTable, PackedBoolean interrupt) throws IOException {
		int freqBits = 0;
		CodeRecord[] table = codeTable.getTable();
		int alphabetSize = codeTable.getAlphabetSize();
		long maxFreq = codeTable.getMaxFrequency();
		long tmp = maxFreq;
		
		do {
			freqBits++;
			tmp /= 2;
		} while (tmp!=0);
		
		int fullHeaderSize = 256*freqBits;
		int partialHeaderSize = alphabetSize*(8+freqBits);
		boolean fullTable = fullHeaderSize<partialHeaderSize;
		
		if (!fullTable) {
			out.write(alphabetSize);
		} else {
			out.write(0);
		}
		
		out.write(freqBits);
		
		if (fullTable) {
			for (int i=0; i<256&&!interrupt.getValue(); i++) {
				long freq = table[i].getFrequency();
				byte[] byteArr = new byte[8];
				byteArr[0] = (byte)freq;
				freq = freq >> 8;
				byteArr[1] = (byte)freq;
				freq = freq >> 8;
				byteArr[2] = (byte)freq;
				freq = freq >> 8;
				byteArr[3] = (byte)freq;
				freq = freq >> 8;
				byteArr[4] = (byte)freq;
				freq = freq >> 8;
				byteArr[5] = (byte)freq;
				freq = freq >> 8;
				byteArr[6] = (byte)freq;
				freq = freq >> 8;
				byteArr[7] = (byte)freq;
				out.writeBits(byteArr, freqBits);
			}
		} else {
			for (int i=0; i<256&&!interrupt.getValue(); i++) {
				long freq = table[i].getFrequency();
				if (freq>0) {
					int bits = 8+freqBits;
					byte[] byteArr = new byte[9];
					byteArr[0] = (byte)table[i].getChar();
					byteArr[1] = (byte)freq;
					freq = freq >> 8;
					byteArr[2] = (byte)freq;
					freq = freq >> 8;
					byteArr[3] = (byte)freq;
					freq = freq >> 8;
					byteArr[4] = (byte)freq;
					freq = freq >> 8;
					byteArr[5] = (byte)freq;
					freq = freq >> 8;
					byteArr[6] = (byte)freq;
					freq = freq >> 8;
					byteArr[7] = (byte)freq;
					freq = freq >> 8;
					byteArr[8] = (byte)freq;					
					out.writeBits(byteArr, bits);
				}
			}
		}
	}
	
	/**
	 * Zapíše tìlo komprimovaného souboru.
	 * @param in Vstupní proud pro kódování.
	 * @param out Výstupní proud pro kompresi.
	 * @param codeTable Tabulka bitových kódù.
	 * @param p Indikátor prùbìhu operace.
	 * @param progressRatio Váha operace.
	 * @param progressStep Velikost kroku operace.
	 * @param interrupt Indikátor pøerušení operace.
	 * @throws IOException Chyba I/O.
	 */
	public static void writeCompressedBody(InputStream in, BitOutputStream out, CodeTable codeTable, Subprogress p, double progressRatio, int progressStep, PackedBoolean interrupt) throws IOException {	
		CodeRecord[] table = codeTable.getTable();
		int c;
		ProgressUpdater u = new ProgressUpdater(p, progressStep, progressRatio);
		while (!interrupt.getValue()&&-1!=(c=in.read())) {
			CodeRecord cr = table[(char)c];
			out.writeBits(cr.getCode(), cr.getCodeLength());
			u.update();
		}
	}
	
	/**
	 * Pøeète tabulku èetností znakù z hlavièky kompromovaného souboru.
	 * @param in Vstupní proud komprimovaného souboru.
	 * @param interrupt Indikátor pøerušení operace.
	 * @return Tabulk èetností znakù.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatný archiv.
	 */
	public static long[] readHeader(BitInputStream in, PackedBoolean interrupt) throws IOException, InvalidArchiveException {
		int tableLen = 0;
		int freqBits = 0; /* poèet bitù reprezentující frekvence výskytù v tabulce */
		long[] frequencies = new long[256];
		int buf;
		
		if (-1==(buf=in.read())) {
			throw new InvalidArchiveException();
		}
		tableLen += ((short)buf+256)%256;
		
		if (-1==(buf=in.read())) {
			throw new InvalidArchiveException();
		}
		freqBits += ((short)buf+256)%256;;
		
		Arrays.fill(frequencies, 0);
		if (tableLen==0) {
			for (char c=0; c<256; c++) {
				long freq = in.readBits(freqBits);
				if (freq==-1) {
					throw new InvalidArchiveException();
				}
				if (freq>0) {
					frequencies[c] = freq;
				}
			}
		} else {
			while (!interrupt.getValue()&&tableLen>0) {
				long c = in.readBits(8);
				long freq = in.readBits(freqBits);
				if (c==-1||freq==-1) {
					throw new InvalidArchiveException();
				}
				frequencies[(int)c] = freq;
				tableLen--;
			}
		}
		
		return frequencies;
	}
	
	/**
	 * Zapíše tìlo dekomprimovaného souboru.
	 * @param in Vstupní proud s komprimovaným tìlem.
	 * @param out Výstupní proud pro dekomprimovaná data.
	 * @param tree Strom s bitovým kódem.
	 * @param char_count Poèet znakù k dekompresi.
	 * @param p Indikátor prùbìhu operace.
	 * @param progressStep Délka kroku operace.
	 * @param progressRatio Váha operace.
	 * @param interrupt Indikátor pøerušení.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatný archiv.
	 */
	public static void writeDecompressedBody(BitInputStream in, BufferedOutputStream out, CodeTree tree, long char_count, Subprogress p, int progressStep, double progressRatio, PackedBoolean interrupt) throws IOException, InvalidArchiveException {
		ProgressUpdater u = new ProgressUpdater(p, progressStep, progressRatio/8);
		while (!interrupt.getValue()&&char_count>0) {
			CodeTreeNode n = tree.getRoot();
			while (!(n instanceof CodeTreeLeaf)) {
				int bit = in.readBit();
				CodeTreeBranching br = (CodeTreeBranching)n;
				
				if (bit==-1) {
					throw new InvalidArchiveException();
				}
				
				if (bit==0) {
					n = br.getLeft();
				} else {
					n = br.getRight();
				}
				
				u.update();
			}
			
			CodeTreeLeaf leaf = (CodeTreeLeaf)n;
			out.write(leaf.getChar());

			char_count--;
		}
	}
	
	/**
	 * Pøevede bitový kód s jeho délkou na textovou reprezentaci. 
	 * Bity vypisuje v poøadí od nejménì dùležitého (proto napøíklad chary vypisuje v opaèném poøadí)
	 * @param code
	 * @param clen
	 * @return Bitový kód pøevedený na String. 
	 */
	public static String codeToString(byte[] code, int clen) {
		StringBuilder result = new StringBuilder();
	
		int counter = 0;
		for (int i=0; i<code.length; i++) {
			byte b = 1;
			for (int j=0; j<8; j++) {
				if (counter==clen) {
					break;
				}
				result.append((b&code[i])==0? '0':'1');
				b = (byte)(b<<1);
				counter++;
			}
			if (counter==clen) {
				break;
			}
		}
		
		return result.toString();
	}
	
	/**
	 * Vypíše seznam souborù v pøedaných adresáøích. Je-li mezi adresáøi pøedán soubor, je vrácen také v seznamu.
	 * @param dirs Vstupní adresáøe.
	 * @param files Seznam souborù v adresáøích.
	 */
	private static void listAllFiles(Vector<File> dirs, Vector<File> files) {
		for (File f: dirs) {
			if (f.isDirectory()) {
				File[] list = f.listFiles();
				Vector<File> listVector = new Vector<File>();
				
				for (File f2: list) {
					listVector.add(f2);
				}
				listAllFiles(listVector, files);
			} else {
				files.add(f);
			}
		}
	}
	
	/**
	 * Vypíše seznam souborù v pøedaných adresáøích. Je-li mezi adresáøi pøedán soubor, je vrácen také v seznamu.
	 * @param dirs Vstupní adresáøe.
	 * @return files Seznam souborù v adresáøích.
	 */
	public static Vector<File> listAllFiles(Vector<File> dirs) {
		Vector<File> files = new Vector<File>();
		listAllFiles(dirs, files);
		return files;
	}
	
	/**
	 * Vrátí pole s èásteènými souèty velikostí souborù.
	 * @param files Soubory.
	 * @return Èásteèné souèty velikostí souborù.
	 */
	public static long[] CalculatePartialProcessedSums(Vector<File> files) {
		long[] res = new long[files.size()];
		long sum = 0;
		
		int i=0;
		for (File f: files) {
			sum += f.length();
			res[i++] = sum;
		}
		
		return res;
	}
	
	/**
	 * Extrahuje jméno souboru bez pøípony.
	 * Pøípona nesmí zaèínat teèkou na zaèátku jména souboru, takže napø. '.bin' se vrátí beze zmìny.
	 * @param f Soubor k extrakci.
	 * @return Jméno souboru bez pøípony. Pokud soubor pøíponu nemá vrací pøímo jméno souboru.
	 */
	public static String getFilenameWithoutExtension(File f) {
		return getFilenameWithoutExtension(f.getName());
	}
	
	/**
	 * Extrahuje jméno souboru bez pøípony.
	 * Pøípona nesmí zaèínat teèkou na zaèátku jména souboru, takže napø. '.bin' se vrátí beze zmìny.
	 * @param filename Jméno souboru
	 * @return Jméno souboru bez pøípony. Pokud soubor pøíponu nemá vrací pøímo jméno souboru.
	 */
	public static String getFilenameWithoutExtension(String filename) {
		int lastDotIndex = filename.lastIndexOf('.');
		
		if (lastDotIndex>=1) {
			return filename.substring(0, lastDotIndex);
		} else {
			return filename;
		}
	}
}
