package compressors;

/**
 * Tabulka bitových kódù jednotlivých znakù.
 */
public class CodeTable {
	/** Øádky tabulky. */
	CodeRecord[] table;
	/** Velikost abecedy. Tj. poèet znakù s nenulovou èetností. */
	int alphabetSize;
	/** Celkový poèet znakù. Tj. souèet èetností znakù. */
	long characterCount;
	/** Nejvìtší èetnost znaku. */
	long maxFreq;
	
	/**
	 * Konstruktor.
	 * @param table Øádky tabulky.
	 * @param alphabetSize Velikost abecedy.
	 * @param charCount Celkový poèet znakù.
	 * @param maxFreq Nejvìtší èetnost znaku.
	 */
	public CodeTable(CodeRecord[] table, int alphabetSize, long charCount, long maxFreq) {
		this.table = table;
		this.alphabetSize = alphabetSize;
		this.characterCount = charCount;
		this.maxFreq = maxFreq;
	}

	/**
	 * Skrytý bezparametrický konstruktor.
	 */
	CodeTable() {}

	/**
	 * Vrací pole s øádky tabulky.
	 * @return Pole s øádky tabulky.
	 */
	public CodeRecord[] getTable() {
		return table;
	}
	
	/**
	 * Vrací velikost abecedy.
	 * @return Velikost abecedy.
	 */
	public int getAlphabetSize() {
		return alphabetSize;
	}
	
	/**
	 * Vrací celkový poèet znakù.
	 * @return Celkový poèet znakù.
	 */
	public long getCharacterCount() {
		return characterCount;
	}
	
	/**
	 * Vrací nejvìtší èetnost znaku v tabulce.
	 * @return Nejvìtší èetnost znaku.
	 */
	public long getMaxFrequency() {
		return maxFreq;
	}

	/**
	 * Vrací textovou reprezentaci tabulky.
	 * @return Textová reprezentace tabulky.
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
