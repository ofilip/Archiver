package compressors;

import utils.Utils;

/**
 * ��dek tabulky CodeTable.
 */
public class CodeRecord implements Comparable<CodeRecord> {
	/**
	 * Konstruktor.
	 * @param c K�dovan� znak.
	 * @param frequency �etnost znaku.
	 */
	public CodeRecord(char c, long frequency) {
		this(c, frequency, new byte[0], 0);
	}

	/**
	 * Konstruktor.
	 * @param c K�dovan� znak.
	 * @param frequency �etnost znaku.
	 * @param code K�d znaku.
	 * @param clen D�lka k�du.
	 */
	public CodeRecord(char c, long frequency, byte[] code, int clen) {
		this.c = c;
		this.f = frequency;
		this.code = code;
		this.clen = clen;
	}
	
	/**
	 * P�id� na konec k�du znaku bit.
	 * @param bit P�idan� bit.
	 */
	public void addToCode(int bit) {
		code = Utils.AddToCode(code, clen, bit);
		clen++;
	}
	
	/**
	 * Vrac� k�dovac� znak.
	 * @return K�dovan� znak.
	 */
	public char getChar() {
		return c;
	}
	
	/**
	 * Vrac� �etnost znaku.
	 * @return �etnost znaku.
	 */
	public long getFrequency() {
		return f;
	}
	
	/**
	 * Vrac� k�d znaku.
	 * @return K�d znaku.
	 */
	public byte[] getCode() {
		return code;
	}
	
	/**
	 * Vytvo�� bitovou masku pro byte obsahuj�c� p��slu�n� bit.
	 * Tzn. vrac� 0, je-li p��slu�n� bit 0 a 2^i, je-li 1. 
	 * @param i Pozice bitu v k�du.
	 * @return Bitov� maska.
	 */
	public Byte getCodeBitMask(int i) {
		byte b = code[i/8];
		byte mask = 1;
		
		mask = (byte)(mask << i%8);
		
		return (byte)(mask&b);
	}
	
	/**
	 * Vrac� d�lku k�du.
	 * @return D�lka k�du.
	 */
	public int getCodeLength() {
		return clen;
	}
	
	/** K�dovan� znak. */
	private char c;
	/** �etnost znaku. */
	private long f;
	/**
	 * Bitov� k�d znaku.
	 * K�d je uspo��d�n od nejni���ho bitu prvn�ho bajtu v poli.
	 */
	private byte[] code;
	/**
	 * Po�et bit� v k�du.
	 */
	private int clen;
	
	/**
	 * Porovn�n�.
	 * @param x CodeRecord k porovn�n�.
	 * @return true, shoduj�-li se znaky a jejich �etnosti.
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
	 * Vrac� textovou reprezentaci ��dku.
	 * @return Textov� reprezentace ��dku.
	 */
	public String codeToString() {
		return Utils.codeToString(code, clen);
	}
	
	/**
	 * Vrac� textovou reprezentaci ��dku.
	 * @return Textov� reprezentace ��dku.
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
