package compressors;

/**
 * Strom reprezentuj�c� bitov� k�dy abecedy.
 */
public class CodeTree {
	/** Ko�en stromu. */
	protected CodeTreeNode root = null;
	
	/** 
	 * P�idat k�d dle ��dku z tabulky do k�du.
	 * @param rec ��dek s k�dem.
	 */
	private void AddCode(CodeRecord rec) {
		char c = rec.getChar();
		int len = rec.getCodeLength();
		
		if (root==null) {
			if (len==0) {
				root = new CodeTreeLeaf(c);
				return;
			} else {
				root = new CodeTreeBranching();
			}
		}
		
		int pos = 0;
		CodeTreeBranching n = (CodeTreeBranching)root;
		CodeTreeBranching prevN = null;
		byte bit;
		

		
		do {
			bit = rec.getCodeBitMask(pos);
			prevN = n;
			n = (CodeTreeBranching)(bit==0? n.left: n.right);
			pos++;
		} while (n!=null&&pos<len);
		
		pos--;
		n = prevN;
		
		while (pos<len-1) {
			CodeTreeBranching newN = new CodeTreeBranching();
			bit = rec.getCodeBitMask(pos);
			if (bit==0) {
				n.left = newN;
			} else {
				n.right = newN;
			}
			n = newN;
			pos++;
		};
		
		bit = rec.getCodeBitMask(pos);
		if (bit==0) {
			n.left = new CodeTreeLeaf(c);
		} else {
			n.right = new CodeTreeLeaf(c);
		}
	}
	
	/**
	 * Skryt� konstruktor.
	 */
	CodeTree() {}
	
	/**
	 * Konstruktor vytv��ej�c� strom z tabulky.
	 * @param table Tabulka s k�dy.
	 */
	public CodeTree(CodeTable table) {
		CodeRecord[] codeArray = table.getTable();
		
		for (int i=0; i<codeArray.length; i++) {
			if (codeArray[i].getFrequency()>0) {
				AddCode(codeArray[i]);
			}
		}
	}
	
	/**
	 * Vrac� ko�en stromu.
	 * @return Ko�en stromu.
	 */
	public CodeTreeNode getRoot() {
		return root;
	}
	
	/**
	 * P�ev�d� strom na textovou reprezentaci.
	 * @return Textov� reprezentace stromu.
	 */
	@Override
	public String toString() {
		return root.toString();
	}
}