package utils;

import java.io.*;

/**
 * Bitov� proud pro �ten� souboru.
 */
public class BitInputStream {
	/** Rodi�ovsk� �ten� stream. */
	private InputStream str;
	/** Bit count - aktu�ln� po�et bit� zapsan�ch do bufferu. */
	private int bc = 8;
	/** Bitov� buffer. */
	private byte buf;
	/** Maska pro aktu�ln� bit v bufferu. */
	private byte mask = 1;
	/** Po�et p�e�ten�ch bajt�. */
	private long bytesRead = 0;
	
	/**
	 * Konstruktor.
	 * @param str Rodi�ovsk� �ten� stream.
	 */
	public BitInputStream(InputStream str) {
		this.str = str;
	}
	

	/**
	 * P�e�te jeden bit.
	 * @return Vr�t� 1 nebo 0 p�i �sp�chu a -1, je-li dosa�en konec souboru.
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
	 * P�e�te a� 32 bit�.
	 * @return Vr�t� int napln�n� count bity.
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
	 * P�e�te jeden bajt.
	 * @return P�e�ten� bajt.
	 * @throws IOException Chyba I/O.
	 */
	public int read() throws IOException {
		return readBits(8);
	}
	
	/**
	 * Zav�e stream.
	 * @throws IOException Chyba I/O.
	 */
	public void close() throws IOException {
		str.close();
	}
	
	/**
	 * Vrac� po�et p�e�ten�ch bajt�.
	 * @return Po�et p�e�ten�ch bajt�.
	 */
	public long getReadBytes() {
		return bytesRead;
	}
}
