package utils;

import java.io.*;

/**
 * Bitový proud pro zápis do souboru.
 */
public class BitOutputStream {
	/** Rodièovský stream pro zápis. */
	private OutputStream str;
	/** Bitový buffer. */
	private byte buf;
	/** Aktuální poèet bitù v bufferu. */
	private int bc;
	/** Maska aktuálního bitu v bufferu. */
	private byte mask;
	/** Poèet zapsaných bajtù. */
	private long bytesWritten = 0;
	
	/**
	 * Konstruktor.
	 * @param str Rodièovský stream pro zápis.
	 */
	public BitOutputStream(OutputStream str) {
		this.str = str;
		buf = 0;
		bc = 0;
		mask = 1;
	}
	
	/**
	 * Zapíše bit_count bitù do streamu.
	 * @param bits Bity pro zápis.
	 * @param bit_count Poèet bitù pro zápis.
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
	 * Zapíše jeden bajt.
	 * @param b Bajt pro zapsání.
	 * @throws IOException Chyba I/O.
	 */
	public void write(int b) throws IOException {
		byte[] bits = new byte[1];
		bits[0] = (byte)b;
		writeBits(bits, 8);
	}
	
	/**
	 * Zavøe stream.
	 * @throws IOException Chyba I/O.
	 */
	public void close() throws IOException {
		if (bc>0) {
			str.write(buf);
		}
		str.close();
	}
	
	/**
	 * Vrátí poèet zapsaných bitù.
	 * @return Poèet zapasných bitù.
	 */
	public long getWrittenBytes() {
		return bytesWritten;
	}
}
