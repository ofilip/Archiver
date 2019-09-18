package compressors;

/**
 * List stromu adaptivn�ho huffmanova k�du.
 */
public class FGKLeaf extends CodeTreeLeaf 
                     implements FGKNode {
	/** �etnost znaku. */
	long f;
	/** Po�ad� uzlu. */
	int order;
	/** P�edchoz� uzel dle po�ad� order. */
	FGKNode previous;
	/** N�sleduj�c� uzel dle po�ad� order. */
	FGKNode next;
	/**  Rodi� uzlu. */
	FGKBranching parent;
	
	/** 
	 * Konstruktor.
	 * @param c K�dovan� znak.
	 */
	public FGKLeaf(char c) {
		this(c, null, null, null, 0, INITIAL_ORDER);
	}
	
	/**
	 * Konstruktor.
	 * @param c K�dovan� znak.
	 * @param parent Rodi� uzlu.
	 * @param previous P�edchoz� uzel dle po�ad� order.
	 * @param next N�sleduj�c� uzel dle po�ad� order.
	 * @param f �etnost znaku.
	 * @param order Po�ad� uzlu.
	 */
	public FGKLeaf(char c, FGKBranching parent, FGKNode previous, FGKNode next, long f, int order) {
		super(c);
		this.f = f;
		this.order = order;
		this.parent = parent;
		this.previous = previous;
		this.next = next;
	}
	
	/**
	 * Indikuje, zda se jedn� o uzel NYT (not yet transmitted).
	 * @return true, jedn�-li se o NYT:
	 */
	public boolean isNYT() {
		return f==0;
	}

	@Override
	public long getFreq() {
		return f;
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public FGKBranching getParent() {
		return parent;
	}
	
	@Override
	public boolean isRoot() {
		return parent==null;
	}

	@Override
	public boolean isLeft() {
		return !isRoot()&&parent.left==this;
	}

	@Override
	public boolean isRigth() {
		return !isRoot()&&parent.right==this;
	}

	@Override
	public void IncFreq() {
		f++;
	}

	@Override
	public void setParent(FGKBranching parent) {
		this.parent = parent;
	}
	
	/**
	 * Vrac� textovou reprezentaci listu.
	 * @return Textov� reprezentace listu.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("Leaf<");
		str.append(order);
		str.append("/");
		str.append(f);
		if (isNYT()) {
			str.append(">(NYT)");
		} else {
			str.append(">('");
			str.append(getChar());
			str.append("')");
		}
		
		return str.toString();
	}

	@Override
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public FGKNode getNext() {
		return next;
	}

	@Override
	public void setNext(FGKNode next) {
		this.next = next;
	}

	@Override
	public FGKNode getPrevious() {
		return previous;
	}

	@Override
	public void setPrevious(FGKNode previous) {
		this.previous = previous;
	}
}
