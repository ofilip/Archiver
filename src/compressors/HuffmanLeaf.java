package compressors;

/**
 * List stromu statick�ho huffmanova k�du.
 */
public class HuffmanLeaf extends CodeTreeLeaf 
                         implements HuffmanNode {
	/** �etnost znaku. */
	private long f;
	
	/**
	 * Konstruktor.
	 * @param c Znak definovan� listem.
	 * @param f �etnost znaku.
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
	 * Porovn� �etnosti znak� definovan�ch uzlem.
	 * @param n Uzel k porovn�n�.
	 * @return -1 je-li �etnost znaku tohoto uzlu men�� ne� �etnost znaku uzlu n; 0, jsou-li �etnosti stejn�; 1 jinak.
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
	 * Vrac� textovou reprezentaci listu.
	 * @return Textov� reprezentace listu.
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
