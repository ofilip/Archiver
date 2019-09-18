package compressors;

/**
 * List stromu statického huffmanova kódu.
 */
public class HuffmanLeaf extends CodeTreeLeaf 
                         implements HuffmanNode {
	/** Èetnost znaku. */
	private long f;
	
	/**
	 * Konstruktor.
	 * @param c Znak definovaný listem.
	 * @param f Èetnost znaku.
	 */
	public HuffmanLeaf(char c, long f) {
		super(c);
		this.f = f;
	}

	@Override
	public long getFreq() {
		return f;
	}

	/**
	 * Porovná èetnosti znakù definovaných uzlem.
	 * @param n Uzel k porovnání.
	 * @return -1 je-li èetnost znaku tohoto uzlu menší než èetnost znaku uzlu n; 0, jsou-li èetnosti stejné; 1 jinak.
	 */
	@Override
	public int compareTo(HuffmanNode n) {
		long f1 = getFreq();
		long f2 = n.getFreq();
		
		if (f1<f2) {
			return -1;
		} else if (f1==f2) {
			return 0;
		} else {
			return 1;
		}
	}
	
	/**
	 * Vrací textovou reprezentaci listu.
	 * @return Textová reprezentace listu.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("Leaf[");
		str.append(getFreq());
		str.append("]('");
		str.append(getChar());
		str.append("')");
		
		return str.toString();
	}
}
