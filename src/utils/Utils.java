package utils;

import java.io.*;
import java.util.*;
import compressors.*;
import exceptions.InvalidArchiveException;

/**
 * T��da obsahuj�c� sadu statick�ch metod vyu��van�ch nap��� aplikac�.
 */
public class Utils {
	/**
	 * Roz���� bitov� k�d o jeden bit.
	 * @param code P�vodn� bitov� k�d.
	 * @param clen D�lka p�vodn�ho bitov�ho k�du.
	 * @param bit Nov� bit.
	 * @return Vznikl� bitov� k�d.
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
	 * Vytvo�� �etnostn� tabulku znak� pro dan� soubor.
	 * @param f Vstupn� soubor.
	 * @param p Indik�tor pr�b�hu operace.
	 * @param progressRatio V�ha operace.
	 * @param progressStep Velikost kroku pro aktualizaci operace.
	 * @param interrupt Indik�tor p�eru�en� operace.
	 * @return �etnostn� tabulka znak� pro soubor.
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
	 * Vytvo�� �etnostn� tabulku znak� pro dan� soubor.
	 * @param f Vstupn� soubor.
	 * @return �etnostn� tabulka znak� pro soubor.
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
	 * Spo��t� velikost abecedy na z�klad� �etnostn� tabulky znak�.
	 * @param frequencies �etnostn� tabulka znak�.
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
	 * Spo��t� po�et znak� na z�klad� �etnostn� tabulky znak�.
	 * @param frequencies �etnostn� tabulka znak�.
	 * @return Po�et znak�.
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
	 * Zap�e hlavi�ku �etnostn�ho k�du.
	 * @param out V�stupn� proud pro z�pis.
	 * @param codeTable Tabulka bitov�ch k�d�.
	 * @param interrupt Indik�tor p�eru�en�.
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
	 * Zap�e t�lo komprimovan�ho souboru.
	 * @param in Vstupn� proud pro k�dov�n�.
	 * @param out V�stupn� proud pro kompresi.
	 * @param codeTable Tabulka bitov�ch k�d�.
	 * @param p Indik�tor pr�b�hu operace.
	 * @param progressRatio V�ha operace.
	 * @param progressStep Velikost kroku operace.
	 * @param interrupt Indik�tor p�eru�en� operace.
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
	 * P�e�te tabulku �etnost� znak� z hlavi�ky kompromovan�ho souboru.
	 * @param in Vstupn� proud komprimovan�ho souboru.
	 * @param interrupt Indik�tor p�eru�en� operace.
	 * @return Tabulk �etnost� znak�.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatn� archiv.
	 */
	public static long[] readHeader(BitInputStream in, PackedBoolean interrupt) throws IOException, InvalidArchiveException {
		int tableLen = 0;
		int freqBits = 0; /* po�et bit� reprezentuj�c� frekvence v�skyt� v tabulce */
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
	 * Zap�e t�lo dekomprimovan�ho souboru.
	 * @param in Vstupn� proud s komprimovan�m t�lem.
	 * @param out V�stupn� proud pro dekomprimovan� data.
	 * @param tree Strom s bitov�m k�dem.
	 * @param char_count Po�et znak� k dekompresi.
	 * @param p Indik�tor pr�b�hu operace.
	 * @param progressStep D�lka kroku operace.
	 * @param progressRatio V�ha operace.
	 * @param interrupt Indik�tor p�eru�en�.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Neplatn� archiv.
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
	 * P�evede bitov� k�d s jeho d�lkou na textovou reprezentaci. 
	 * Bity vypisuje v po�ad� od nejm�n� d�le�it�ho (proto nap��klad chary vypisuje v opa�n�m po�ad�)
	 * @param code
	 * @param clen
	 * @return Bitov� k�d p�eveden� na String. 
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
	 * Vyp�e seznam soubor� v p�edan�ch adres���ch. Je-li mezi adres��i p�ed�n soubor, je vr�cen tak� v seznamu.
	 * @param dirs Vstupn� adres��e.
	 * @param files Seznam soubor� v adres���ch.
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
	 * Vyp�e seznam soubor� v p�edan�ch adres���ch. Je-li mezi adres��i p�ed�n soubor, je vr�cen tak� v seznamu.
	 * @param dirs Vstupn� adres��e.
	 * @return files Seznam soubor� v adres���ch.
	 */
	public static Vector<File> listAllFiles(Vector<File> dirs) {
		Vector<File> files = new Vector<File>();
		listAllFiles(dirs, files);
		return files;
	}
	
	/**
	 * Vr�t� pole s ��ste�n�mi sou�ty velikost� soubor�.
	 * @param files Soubory.
	 * @return ��ste�n� sou�ty velikost� soubor�.
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
	 * Extrahuje jm�no souboru bez p��pony.
	 * P��pona nesm� za��nat te�kou na za��tku jm�na souboru, tak�e nap�. '.bin' se vr�t� beze zm�ny.
	 * @param f Soubor k extrakci.
	 * @return Jm�no souboru bez p��pony. Pokud soubor p��ponu nem� vrac� p��mo jm�no souboru.
	 */
	public static String getFilenameWithoutExtension(File f) {
		return getFilenameWithoutExtension(f.getName());
	}
	
	/**
	 * Extrahuje jm�no souboru bez p��pony.
	 * P��pona nesm� za��nat te�kou na za��tku jm�na souboru, tak�e nap�. '.bin' se vr�t� beze zm�ny.
	 * @param filename Jm�no souboru
	 * @return Jm�no souboru bez p��pony. Pokud soubor p��ponu nem� vrac� p��mo jm�no souboru.
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
