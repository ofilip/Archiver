package compressors;

/**
 * Vnitøní uzel stromu adaptivního huffmanova kódu.
 */
public class FGKBranching extends CodeTreeBranching 
                          implements FGKNode {
	
	/** Èetnost znaku. */
	long f;
	/** Poøadí uzlu. */
	int order;
	/** Pøedchozí uzel dle poøadí order. */
	FGKNode previous;
	/** Následující uzel dle poøadí order. */
	FGKNode next;
	/**  Rodiè uzlu. */
	FGKBranching parent;

	/**
	 * Konstruktor.
	 */
	public FGKBranching() {
		f = 0;
		left = right = previous = next = parent = null;
		order = 0;
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
	 * Vrací textovou reprezentaci uzlu.
	 * @return Textová reprezentace uzlu.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("Node<");
		str.append(order);
		str.append("/");
		str.append(f);
		str.append(">(");
		str.append(left);
		str.append(',');
		str.append(right);
		str.append(')');
		
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
