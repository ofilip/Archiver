package compressors;

/**
 * Vnit�n� uzel stromu statick�ho huffmanova k�du.
 */
public class HuffmanBranching extends CodeTreeBranching 
                              implements HuffmanNode {
	/** �etnost znaku. */
	private long f;
	
	/**
	 * Konstruktor.
	 * @param left Lev� syn.
	 * @param right Prav� syn.
	 */
	public HuffmanBranching(HuffmanNode left, HuffmanNode right) {
		f = left.getFreq()+right.getFreq();
		
		this.left = left;
		this.right = right;
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
		Long f1 = getFreq();
		Long f2 = n.getFreq();
		
		return f1.compareTo(f2);
	}
	
	/**
	 * Vrac� textovou reprezentaci uzlu.
	 * @return Textov� reprezentace uzlu.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("Node[");
		str.append(getFreq());
		str.append("]('");
		str.append(left.toString());
		str.append(',');
		str.append(right.toString());
		str.append("')");
		
		return str.toString();
	}
}
