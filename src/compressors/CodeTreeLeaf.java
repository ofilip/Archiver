package compressors;

/**
 * List stromu s bitov�mi k�dy.
 */
public class CodeTreeLeaf implements CodeTreeNode {
	/** K�dovan� znak. */
	private char c;
	
	/**
	 * Konstruktor.
	 * @param c K�dovan� znak.
	 */
	public CodeTreeLeaf(char c) {
		super();
		this.c = c;
	}
	
	/**
	 * Vrac� k�dovan� znak.
	 * @return K�dovan� znak.
	 */
	public char getChar() {
		return c;
	}
	
	/**
	 * Vrac� textovou reprezentaci uzlu.
	 * @return Textov� reprezentace uzlu.
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
