package compressors;

/**
 * Tabulka bitov�ch k�d� jednotliv�ch znak�.
 */
public class CodeTable {
	/** ��dky tabulky. */
	CodeRecord[] table;
	/** Velikost abecedy. Tj. po�et znak� s nenulovou �etnost�. */
	int alphabetSize;
	/** Celkov� po�et znak�. Tj. sou�et �etnost� znak�. */
	long characterCount;
	/** Nejv�t�� �etnost znaku. */
	long maxFreq;
	
	/**
	 * Konstruktor.
	 * @param table ��dky tabulky.
	 * @param alphabetSize Velikost abecedy.
	 * @param charCount Celkov� po�et znak�.
	 * @param maxFreq Nejv�t�� �etnost znaku.
	 */
	public CodeTable(CodeRecord[] table, int alphabetSize, long charCount, long maxFreq) {
		this.table = table;
		this.alphabetSize = alphabetSize;
		this.characterCount = charCount;
		this.maxFreq = maxFreq;
	}

	/**
	 * Skryt� bezparametrick� konstruktor.
	 */
	CodeTable() {}

	/**
	 * Vrac� pole s ��dky tabulky.
	 * @return Pole s ��dky tabulky.
	 */
	public CodeRecord[] getTable() {
		return table;
	}
	
	/**
	 * Vrac� velikost abecedy.
	 * @return Velikost abecedy.
	 */
	public int getAlphabetSize() {
		return alphabetSize;
	}
	
	/**
	 * Vrac� celkov� po�et znak�.
	 * @return Celkov� po�et znak�.
	 */
	public long getCharacterCount() {
		return characterCount;
	}
	
	/**
	 * Vrac� nejv�t�� �etnost znaku v tabulce.
	 * @return Nejv�t�� �etnost znaku.
	 */
	public long getMaxFrequency() {
		return maxFreq;
	}

	/**
	 * Vrac� textovou reprezentaci tabulky.
	 * @return Textov� reprezentace tabulky.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		for (int i=0; i<table.length; i++) {
			builder.append(table[i].toString());
			builder.append('\n');
		}
		
		return builder.toString();
	}
}
