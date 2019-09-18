package compressors;

/**
 * List stromu s bitovými kódy.
 */
public class CodeTreeLeaf implements CodeTreeNode {
	/** Kódovaný znak. */
	private char c;
	
	/**
	 * Konstruktor.
	 * @param c Kódovaný znak.
	 */
	public CodeTreeLeaf(char c) {
		super();
		this.c = c;
	}
	
	/**
	 * Vrací kódovaný znak.
	 * @return Kódovaný znak.
	 */
	public char getChar() {
		return c;
	}
	
	/**
	 * Vrací textovou reprezentaci uzlu.
	 * @return Textová reprezentace uzlu.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("Leaf('");
		str.append(c);
		str.append("')");
		
		return str.toString();
	}
}
