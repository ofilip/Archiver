package utils;

import java.io.*;

/**
 * Bitový proud pro ètení souboru.
 */
public class BitInputStream {
	/** Rodièovský ètený stream. */
	private InputStream str;
	/** Bit count - aktuální poèet bitù zapsaných do bufferu. */
	private int bc = 8;
	/** Bitový buffer. */
	private byte buf;
	/** Maska pro aktuální bit v bufferu. */
	private byte mask = 1;
	/** Poèet pøeètených bajtù. */
	private long bytesRead = 0;
	
	/**
	 * Konstruktor.
	 * @param str Rodièovský ètený stream.
	 */
	public BitInputStream(InputStream str) {
		this.str = str;
	}
	

	/**
	 * Pøeète jeden bit.
	 * @return Vrátí 1 nebo 0 pøi úspìchu a -1, je-li dosažen konec souboru.
	 * @throws IOException Chyba I/O. 
	 */
	public int readBit() throws IOException {
		if (bc==8) {
			bc = 0;
			int c = str.read();
			bytesRead++;
			if (c==-1) {
				return -1;
			}
			buf = (byte)c;
			mask = 1;
		}
		byte bit = (byte)(buf&mask);
		mask = (byte)(mask << 1);
		bc++;
		return (bit!=0)? 1: 0;
	}
	
	/**
	 * Pøeète až 32 bitù.
	 * @return Vrátí int naplnìný count bity.
	 * @throws IOException Chyba I/O. 
	 */
	public int readBits(int count) throws IOException {
		int result = 0;
		int mask = 1;
		
		for (int i=0; i<count; i++) {
			int bit = readBit();
			if (bit==-1) {
				return -1;
			} else if (bit!=0) {
				result |= mask;
			}
			mask = mask << 1;
		}
		
		return result;
	}
	
	/**
	 * Pøeète jeden bajt.
	 * @return Pøeètený bajt.
	 * @throws IOException Chyba I/O.
	 */
	public int read() throws IOException {
		return readBits(8);
	}
	
	/**
	 * Zavøe stream.
	 * @throws IOException Chyba I/O.
	 */
	public void close() throws IOException {
		str.close();
	}
	
	/**
	 * Vrací poèet pøeètených bajtù.
	 * @return Poèet pøeètených bajtù.
	 */
	public long getReadBytes() {
		return bytesRead;
	}
}
