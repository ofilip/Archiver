package compressors;

/**
 * Vnitøní uzel stromu statického huffmanova kódu.
 */
public class HuffmanBranching extends CodeTreeBranching 
                              implements HuffmanNode {
	/** Èetnost znaku. */
	private long f;
	
	/**
	 * Konstruktor.
	 * @param left Levý syn.
	 * @param right Pravý syn.
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
	 * Porovná èetnosti znakù definovaných uzlem.
	 * @param n Uzel k porovnání.
	 * @return -1 je-li èetnost znaku tohoto uzlu menší než èetnost znaku uzlu n; 0, jsou-li èetnosti stejné; 1 jinak.
	 */	
	@Override
	public int compareTo(HuffmanNode n) {
		Long f1 = getFreq();
		Long f2 = n.getFreq();
		
		return f1.compareTo(f2);
	}
	
	/**
	 * Vrací textovou reprezentaci uzlu.
	 * @return Textová reprezentace uzlu.
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
