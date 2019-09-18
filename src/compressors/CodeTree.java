package compressors;

/**
 * Strom reprezentující bitové kódy abecedy.
 */
public class CodeTree {
	/** Koøen stromu. */
	protected CodeTreeNode root = null;
	
	/** 
	 * Pøidat kód dle øádku z tabulky do kódu.
	 * @param rec Øádek s kódem.
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
	 * Skrytý konstruktor.
	 */
	CodeTree() {}
	
	/**
	 * Konstruktor vytváøející strom z tabulky.
	 * @param table Tabulka s kódy.
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
	 * Vrací koøen stromu.
	 * @return Koøen stromu.
	 */
	public CodeTreeNode getRoot() {
		return root;
	}
	
	/**
	 * Pøevádí strom na textovou reprezentaci.
	 * @return Textová reprezentace stromu.
	 */
	@Override
	public String toString() {
		return root.toString();
	}
}