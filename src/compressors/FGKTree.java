package compressors;

import utils.Utils;

/**
 * Strom s bitov�mi k�dy pro adaptivn� huffman�v k�d (FGK k�d).
 */
public class FGKTree extends CodeTree {
	/** D�lka k�du pro naposledy vyhled�van� znak. */
	private int clen;
	/** K�d naposledy vyhled�van�ho znaku. */
	private byte[] code;
	
	/** 
	 * Konstruktor pr�zdn�ho stromu.
	 */
	public FGKTree() {
		root = new FGKLeaf('\0'); /* uzel NTF (Not yet transmitted) */
		clen = 0;
		code = new byte[1];
	}
	
	/**
	 * Zv��� �etnost znaku a p��slu�n� uprav� strom.
	 * @param c Znak, jeho� �etnost je zv��ena.
	 */
	public void IncreaseFrequency(char c) {
		IncreaseFrequency(c, false);
	}
	
	/**
	 * Zv��� �etnost znaku a p��slu�n� uprav� strom.
	 * @param c Znak, jeho� �etnost je zv��ena.
	 * @param fast Pokud je true, nen� nastaven k�d znaku.
	 */
	public void IncreaseFrequency(char c, boolean fast) {
		FGKLeaf n = Lookup(c, fast);
		FGKNode n1;
		
		if (n.isNYT()) {
			n = AddCharacter(c, n);
			n1 = n.parent;
		} else {
			n1 = n;
		}

		while (!n1.isRoot()) {
			FGKNode n2 = BestOfFreqClass(n1);
			
			n1.IncFreq();
			if (n2!=n1&&n1.getParent()!=n2) {
				SwapNodes(n1, n2);
			}
			n1 = ((FGKNode)n1).getParent();
		}
		n1.IncFreq();
	}
	
	/**
	 * Vyhled� list odpov�daj�c� znaku.
	 * @param c Vyhled�van� znak.
	 * @return List odpov�daj�c� znaku.
	 */
	FGKLeaf Lookup(char c) {
		return Lookup(c, false);
	}
	
	/**
	 * Vyhled� list odpov�daj�c� znaku.
	 * @param c Hledan� znak.
	 * @param fast Pokud false, nen� nastaven k�d nalezen�ho znaku, jinak ano.
	 * @return Vr�t� vrchol reprezentuj�c� znak, nebo NTF vrchol.
	 */
	FGKLeaf Lookup(char c, boolean fast) {
		clen = 0;
		return Lookup(c, root, 0, fast);
	}
	/**
	 * Rekurzivn� hled�n� (do hloubky) listu odpov�daj�c�mu znaku.
	 * @param c Hledan� znak.
	 * @param n 
	 * @param depth
	 * @param fast Pokud false, nen� nastaven k�d nalezen�ho znaku, jinak ano.
	 * @return Vr�t� vrchol reprezentuj�c� znak, nebo NYT vrchol.
	 */
	private FGKLeaf Lookup(char c, CodeTreeNode n, int depth, boolean fast) {
		FGKLeaf res = null;
		
		if (n instanceof FGKLeaf) {
			FGKLeaf l = (FGKLeaf)n;
			
			if (l.isNYT()) {
				clen = depth;
				if (!fast) {
					AppendCharToCode(c);
				}
				return l;
				
			} else if (l.getChar()==c) {
				clen = depth;
				return l;
			} else {
				return null;
			}
		}
		
		FGKBranching br = (FGKBranching)n;
		FGKLeaf resL = Lookup(c, br.left, depth+1, fast);

		if (resL!=null) {
			if (!fast) {
				SetCodeBit(depth, 0);
			}
			res = resL;
		} 
		if (resL==null||resL.isNYT()){
			FGKLeaf resR = Lookup(c, br.right, depth+1, fast);
			if (resR!=null) {
				if (!fast) {
					SetCodeBit(depth, 1);
				}
				res = resR;
			}
		}
		return res;
	}
	
	/**
	 * P�id� nov� uzel se znakem tak, �e nahrad� NYT vrchol v�tven�m, 
	 * lev� potomek je NTF vrchol, prav� nov� list s hodnotou c.
	 * @param c Znak p��slu��c� listu.
	 * @param nyt Vrchol NYT.
	 * @return Vr�t� ukazatel na nov� vrchol.
	 */
	private FGKLeaf AddCharacter(char c, FGKLeaf nyt) {
		FGKBranching br = new FGKBranching();
		
		if (nyt.isRoot()) {
			root = br;
			br.parent = null;
		} else {
			br.parent = nyt.parent;
			if (nyt.isLeft()) {
				nyt.parent.left = br;
			} else {
				nyt.parent.right = br;
			}
		}
		
		nyt.parent = br;
		FGKLeaf n = new FGKLeaf(c, br, nyt, br, 1, nyt.order-1); 
		br.left = nyt;
		br.right = n;
		br.order = nyt.order;
		br.previous = n;
		br.next = nyt.next;
		if (br.next!=null) {
			br.next.setPrevious(br);
		}
		nyt.next = n;
		nyt.order -= 2;
		
		return n;
	}
	
	/**
	 * Vr�t� uzel z frekven�n� t��dy uzlu n s nejvy���m po�ad�m (order).
	 * Frekven�n� (�etnostn�) t��da uzlu n je mno�ina v�ech uzl� se stejnou �etnost� znaku.
	 * @param n Uzel ur�uj�c� frekven�n� t��du.
	 * @return Uzel z frekven�n� t��dy n s nejv����m po�ad�m.
	 */
	private FGKNode BestOfFreqClass(FGKNode n) {
		return BestOfFreqClass(n, n);
	}
	
	/**
	 * Rekurzivn� hled� uzel z frekven�n� t��dy uzlu n s nejvy���m po�ad�m (order).
	 * Frekven�n� (�etnostn�) t��da uzlu n je mno�ina v�ech uzl� se stejnou �etnost� znaku.
	 * @param n Uzel ur�uj�c� frekven�n� t��du.
	 * @param curr St�vaj�c� zkouman� uzel.
	 * @return Uzel z frekven�n� t��dy n s nejv����m po�ad�m.
	 */
	private FGKNode BestOfFreqClass(FGKNode n, FGKNode curr) {
		FGKNode next = curr.getNext();
		
		if (next==null||next.getFreq()>n.getFreq()) {
			return curr;
		} else {
			return BestOfFreqClass(n, next);
		}
	}
	
	/**
	 * Vym�n� ve stromu uzly n1 a n2.
	 * P�edpoklad: n1 ani n2 nen� ko�en stromu.
	 * @param n1 Prvn� zam��ovan� uzel.
	 * @param n2 Druh� zam��ovan� uzel.
	 */
	private void SwapNodes(FGKNode n1, FGKNode n2) {
		
		if (n1.getPrevious()==n2) {
			SwapNodes(n2, n1);
		} else { 
			FGKBranching n2parent = n2.getParent();
			boolean n2left = n2.isLeft();
			int n2order = n2.getOrder();
			FGKNode n2next = n2.getNext();
			FGKNode n2prev = n2.getPrevious();
			
			if (n1.isLeft()) {
				n1.getParent().left = n2;
			} else {
				n1.getParent().right = n2;
			}
			n2.setParent( n1.getParent() );

			n2.setOrder(n1.getOrder());
			
			if (n2left) {
				n2parent.left = n1;
			} else {
				n2parent.right = n1;
			}
			
			n1.setParent(n2parent);
			n1.setOrder(n2order);
			
			n2.setPrevious( n1.getPrevious() );
			n2.getPrevious().setNext(n2);
			

			if (n1==n2prev) {
				n2.setNext(n1);
				n1.setPrevious(n2);
			} else {
				n2.setNext( n1.getNext() );
				if (n2.getNext()!=null) {
					n2.getNext().setPrevious(n2);
				}

				n1.setPrevious(n2prev);
				n2prev.setNext(n1);
			}
			
			n1.setNext(n2next);
			if (n2next!=null) {
				n2next.setPrevious(n1);
			}
		}
	}
	
	/**
	 * P�ipoj� ke k�du pr�v� nalezen�ho znaku 8 bit�.
	 * @param c Znak definuj�c� p�ipojen�ch 8 bit�.
	 */
	private void AppendCharToCode(char c) {
		char x = 0x01;
		
		for (int i=0; i<8; i++) {
			SetCodeBit(clen+i, x&c);
			x = (char) (x<<1);
		}
		clen += 8;
	}
	
	/**
	 * Nastav� bit k�du pr�v� nalezen�ho znaku.
	 * @param pos Pozice bitu.
	 * @param bit Hodnota bitu.
	 */
	private void SetCodeBit(int pos, int bit) {
		while (pos>code.length*8-1) {
			byte[] new_code = new byte[2*code.length];
			for (int i=0; i<code.length; i++) {
				new_code[i] = code[i];
			}
			code = new_code;
		}
		if (bit==0) {
			code[pos/8] &= (byte) ~(0x01 << (pos%8));
		} else {
			code[pos/8] |= (byte) (0x01 << (pos%8));
		}
	}
	
	/**
	 * Vrac� bitov� k�d naposledy hledan�ho znaku.
	 * @return Bitov� k�d naposledy hledan�ho znaku.
	 */
	public byte[] getCode() {
		return code;
	}
	
	/**
	 * Vrac� d�lku bitov�ho k�du naposledy hledan�ho znaku.
	 * @return D�lku bitov�ho k�du naposledy hledan�ho bitu.
	 */
	public int getCodeLength() {
		return clen;
	}
	
	/**
	 * Vrac� textovou reprezentaci stromu.
	 * @return Textov� reprezentace stromu.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String treeStr = super.toString();
		String codeStr = Utils.codeToString(code, clen);
		
		result.append(treeStr);
		result.append("/");
		result.append(codeStr);
		
		return result.toString();
	}
}
