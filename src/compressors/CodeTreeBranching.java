package compressors;

/**
 * Vnit�n� uzel stromu s bitov�mi k�dy.
 */
public class CodeTreeBranching implements CodeTreeNode {
	/** Lev� syn. */
	CodeTreeNode left;
	/** Prav� syn. */
	CodeTreeNode right;
	
	/**
	 * Konstruktor.
	 */
	public CodeTreeBranching() {
		super();
	}
	
	/**
	 * Vrac� lev�ho syna.
	 * @return Lev� syn.
	 */
	public CodeTreeNode getLeft() {
		return left;
	}
	
	/**
	 * Vrac� prav�ho syna.
	 * @return Prav� syn.
	 */
	public CodeTreeNode getRight() {
		return right;
	}
	
	/**
	 * Vrac� textovou reprezentaci uzlu.
	 * @return Textov� reprezentace uzlu.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("Node(");
		str.append(left);
		str.append(',');
		str.append(right);
		str.append(')');
		
		return str.toString();
	}
}
