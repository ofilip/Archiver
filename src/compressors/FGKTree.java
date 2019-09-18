package compressors;

import utils.Utils;

/**
 * Strom s bitovými kódy pro adaptivní huffmanùv kód (FGK kód).
 */
public class FGKTree extends CodeTree {
	/** Délka kódu pro naposledy vyhledávaný znak. */
	private int clen;
	/** Kód naposledy vyhledávaného znaku. */
	private byte[] code;
	
	/** 
	 * Konstruktor prázdného stromu.
	 */
	public FGKTree() {
		root = new FGKLeaf('\0'); /* uzel NTF (Not yet transmitted) */
		clen = 0;
		code = new byte[1];
	}
	
	/**
	 * Zvýší èetnost znaku a pøíslušnì upraví strom.
	 * @param c Znak, jehož èetnost je zvýšena.
	 */
	public void IncreaseFrequency(char c) {
		IncreaseFrequency(c, false);
	}
	
	/**
	 * Zvýší èetnost znaku a pøíslušnì upraví strom.
	 * @param c Znak, jehož èetnost je zvýšena.
	 * @param fast Pokud je true, není nastaven kód znaku.
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
	 * Vyhledá list odpovídající znaku.
	 * @param c Vyhledávaný znak.
	 * @return List odpovídající znaku.
	 */
	FGKLeaf Lookup(char c) {
		return Lookup(c, false);
	}
	
	/**
	 * Vyhledá list odpovídající znaku.
	 * @param c Hledaný znak.
	 * @param fast Pokud false, není nastaven kód nalezeného znaku, jinak ano.
	 * @return Vrátí vrchol reprezentující znak, nebo NTF vrchol.
	 */
	FGKLeaf Lookup(char c, boolean fast) {
		clen = 0;
		return Lookup(c, root, 0, fast);
	}
	/**
	 * Rekurzivní hledání (do hloubky) listu odpovídajícímu znaku.
	 * @param c Hledaný znak.
	 * @param n 
	 * @param depth
	 * @param fast Pokud false, není nastaven kód nalezeného znaku, jinak ano.
	 * @return Vrátí vrchol reprezentující znak, nebo NYT vrchol.
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
	 * Pøidá nový uzel se znakem tak, že nahradí NYT vrchol vìtvením, 
	 * levý potomek je NTF vrchol, pravý nový list s hodnotou c.
	 * @param c Znak pøíslušící listu.
	 * @param nyt Vrchol NYT.
	 * @return Vrátí ukazatel na nový vrchol.
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
	 * Vrátí uzel z frekvenèní tøídy uzlu n s nejvyšším poøadím (order).
	 * Frekvenèní (èetnostní) tøída uzlu n je množina všech uzlù se stejnou èetností znaku.
	 * @param n Uzel urèující frekvenèní tøídu.
	 * @return Uzel z frekvenèní tøídy n s nejvýšším poøadím.
	 */
	private FGKNode BestOfFreqClass(FGKNode n) {
		return BestOfFreqClass(n, n);
	}
	
	/**
	 * Rekurzivnì hledá uzel z frekvenèní tøídy uzlu n s nejvyšším poøadím (order).
	 * Frekvenèní (èetnostní) tøída uzlu n je množina všech uzlù se stejnou èetností znaku.
	 * @param n Uzel urèující frekvenèní tøídu.
	 * @param curr Stávající zkoumaný uzel.
	 * @return Uzel z frekvenèní tøídy n s nejvýšším poøadím.
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
	 * Vymìní ve stromu uzly n1 a n2.
	 * Pøedpoklad: n1 ani n2 není koøen stromu.
	 * @param n1 První zamìòovaný uzel.
	 * @param n2 Druhý zamìòovaný uzel.
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
	 * Pøipojí ke kódu právì nalezeného znaku 8 bitù.
	 * @param c Znak definující pøipojených 8 bitù.
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
	 * Nastaví bit kódu právì nalezeného znaku.
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
	 * Vrací bitový kód naposledy hledaného znaku.
	 * @return Bitový kód naposledy hledaného znaku.
	 */
	public byte[] getCode() {
		return code;
	}
	
	/**
	 * Vrací délku bitového kódu naposledy hledaného znaku.
	 * @return Délku bitového kódu naposledy hledaného bitu.
	 */
	public int getCodeLength() {
		return clen;
	}
	
	/**
	 * Vrací textovou reprezentaci stromu.
	 * @return Textová reprezentace stromu.
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
