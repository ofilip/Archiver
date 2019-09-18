package compressors;

import utils.Utils;

/**
 * Øádek tabulky CodeTable.
 */
public class CodeRecord implements Comparable<CodeRecord> {
	/**
	 * Konstruktor.
	 * @param c Kódovaný znak.
	 * @param frequency Èetnost znaku.
	 */
	public CodeRecord(char c, long frequency) {
		this(c, frequency, new byte[0], 0);
	}

	/**
	 * Konstruktor.
	 * @param c Kódovaný znak.
	 * @param frequency Èetnost znaku.
	 * @param code Kód znaku.
	 * @param clen Délka kódu.
	 */
	public CodeRecord(char c, long frequency, byte[] code, int clen) {
		this.c = c;
		this.f = frequency;
		this.code = code;
		this.clen = clen;
	}
	
	/**
	 * Pøidá na konec kódu znaku bit.
	 * @param bit Pøidaný bit.
	 */
	public void addToCode(int bit) {
		code = Utils.AddToCode(code, clen, bit);
		clen++;
	}
	
	/**
	 * Vrací kódovací znak.
	 * @return Kódovaný znak.
	 */
	public char getChar() {
		return c;
	}
	
	/**
	 * Vrací èetnost znaku.
	 * @return Èetnost znaku.
	 */
	public long getFrequency() {
		return f;
	}
	
	/**
	 * Vrací kód znaku.
	 * @return Kód znaku.
	 */
	public byte[] getCode() {
		return code;
	}
	
	/**
	 * Vytvoøí bitovou masku pro byte obsahující pøíslušný bit.
	 * Tzn. vrací 0, je-li pøíslušný bit 0 a 2^i, je-li 1. 
	 * @param i Pozice bitu v kódu.
	 * @return Bitová maska.
	 */
	public Byte getCodeBitMask(int i) {
		byte b = code[i/8];
		byte mask = 1;
		
		mask = (byte)(mask << i%8);
		
		return (byte)(mask&b);
	}
	
	/**
	 * Vrací délku kódu.
	 * @return Délka kódu.
	 */
	public int getCodeLength() {
		return clen;
	}
	
	/** Kódovaný znak. */
	private char c;
	/** Èetnost znaku. */
	private long f;
	/**
	 * Bitový kód znaku.
	 * Kód je uspoøádán od nejnižšího bitu prvního bajtu v poli.
	 */
	private byte[] code;
	/**
	 * Poèet bitù v kódu.
	 */
	private int clen;
	
	/**
	 * Porovnání.
	 * @param x CodeRecord k porovnání.
	 * @return true, shodují-li se znaky a jejich èetnosti.
	 */
	@Override
	public int compareTo(CodeRecord x) {
		long f1 = getFrequency();
		long f2 = x.getFrequency();
		char c1 = getChar();
		char c2 = x.getChar();
		
		if (f1<f2) {
			return 1;
		}
		else if (f1==f2) {
			if (c1<c2) {
				return -1;
			} else if (c1==c2) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return -1;
		}
	}
	
	/**
	 * Vrací textovou reprezentaci øádku.
	 * @return Textová reprezentace øádku.
	 */
	public String codeToString() {
		return Utils.codeToString(code, clen);
	}
	
	/**
	 * Vrací textovou reprezentaci øádku.
	 * @return Textová reprezentace øádku.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		
		result.append('\'');
		result.append(c);
		result.append("'\t");
		result.append(getFrequency());
		result.append("\t\'");
		result.append(codeToString());
		result.append('\'');
		
		return result.toString();
	}
}
