package compressors;

/**
 * Vnitøní uzel stromu s bitovými kódy.
 */
public class CodeTreeBranching implements CodeTreeNode {
	/** Levý syn. */
	CodeTreeNode left;
	/** Pravý syn. */
	CodeTreeNode right;
	
	/**
	 * Konstruktor.
	 */
	public CodeTreeBranching() {
		super();
	}
	
	/**
	 * Vrací levého syna.
	 * @return Levý syn.
	 */
	public CodeTreeNode getLeft() {
		return left;
	}
	
	/**
	 * Vrací pravého syna.
	 * @return Pravý syn.
	 */
	public CodeTreeNode getRight() {
		return right;
	}
	
	/**
	 * Vrací textovou reprezentaci uzlu.
	 * @return Textová reprezentace uzlu.
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
