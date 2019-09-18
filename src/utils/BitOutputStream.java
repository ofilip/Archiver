package utils;

import java.io.*;

/**
 * Bitov� proud pro z�pis do souboru.
 */
public class BitOutputStream {
	/** Rodi�ovsk� stream pro z�pis. */
	private OutputStream str;
	/** Bitov� buffer. */
	private byte buf;
	/** Aktu�ln� po�et bit� v bufferu. */
	private int bc;
	/** Maska aktu�ln�ho bitu v bufferu. */
	private byte mask;
	/** Po�et zapsan�ch bajt�. */
	private long bytesWritten = 0;
	
	/**
	 * Konstruktor.
	 * @param str Rodi�ovsk� stream pro z�pis.
	 */
	public BitOutputStream(OutputStream str) {
		this.str = str;
		buf = 0;
		bc = 0;
		mask = 1;
	}
	
	/**
	 * Zap�e bit_count bit� do streamu.
	 * @param bits Bity pro z�pis.
	 * @param bit_count Po�et bit� pro z�pis.
	 * @throws IOException Chyba I/O.
	 */
	public void writeBits(byte[] bits, int bit_count) throws IOException {
		byte b = 1;
		for (int i=0; i<bit_count; i++) {
			if (i%8==0) {
				b = 1;
			}
			if (bc==8) {
				str.write(buf);
				bc = 0;
				buf = 0;
				mask = 1;
				bytesWritten++;
			}
			
			if ((b&bits[i/8])!=0) {
				buf |= mask;
			}
		    b = (byte)(b << 1);
			mask = (byte) (mask << 1);
		    bc++;
		}
	}
	
	/**
	 * Zap�e jeden bajt.
	 * @param b Bajt pro zaps�n�.
	 * @throws IOException Chyba I/O.
	 */
	public void write(int b) throws IOException {
		byte[] bits = new byte[1];
		bits[0] = (byte)b;
		writeBits(bits, 8);
	}
	
	/**
	 * Zav�e stream.
	 * @throws IOException Chyba I/O.
	 */
	public void close() throws IOException {
		if (bc>0) {
			str.write(buf);
		}
		str.close();
	}
	
	/**
	 * Vr�t� po�et zapsan�ch bit�.
	 * @return Po�et zapasn�ch bit�.
	 */
	public long getWrittenBytes() {
		return bytesWritten;
	}
}
