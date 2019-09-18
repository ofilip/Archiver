package compressors;

/**
 * Uzel stromu pro adaptivní huffmanùv kód.
 */
public interface FGKNode extends CodeTreeNode {
	/** Poèáteèní poøadí vytváøeného uzlu. */
	static final int INITIAL_ORDER = 512;
	
	/** 
	 * Vrací èetnost znaku.
	 * @return Èetnost znaku.
	 */
	public long getFreq();
	
	/**
	 * Vrací poøadí uzlu.
	 * @return Poøadí uzlu.
	 */
	public int getOrder();
	
	/**
	 * Vrací následující uzel dle poøadí order.
	 * @return Následující uzel dle poøadí order.
	 */
	public FGKNode getNext();
	
	/**
	 * Nastaví následující uzel dle poøadí order.
	 * @param next Nový následující uzel dle poøadí order.
	 */
	void setNext(FGKNode next);
	
	/**
	 *Vrací pøedcházející uzel dle poøadí order.
	 * @return Pøedcházející uzel dle poøadí order.
	 */
	public FGKNode getPrevious();
	
	/**
	 * Nastaví pøedcházející uzel dle poøadí order.
	 * @param previous Nový pøedcházející uzel dle poøadí order.
	 */
	void setPrevious(FGKNode previous);
	
	/**
	 * Nastaví novou hodnotu poøadí order.
	 * @param order Nová hodnota poøadí order.
	 */
	void setOrder(int order);
	
	/**
	 * Vrací rodièovský uzel uzlu.
	 * @return Rodièovský uzel.
	 */
	public FGKBranching getParent();
	
	/**
	 * Nastaví rodièovský uzel uzlu.
	 * @param parent Nový rodièovský uzel.
	 */
	void setParent(FGKBranching parent);
	
	/**
	 * Indikuje, zda je uzel koøenem stromu.
	 * @return true, je-li uzel koøenem stromu.
	 */
	public boolean isRoot();
	
	/**
	 * Indijuje, zda je uzel levým synem.
	 * @return true, je-li uzel levým synem.
	 */
	public boolean isLeft();
	
	/**
	 * Indijuje, zda je uzel pravým synem.
	 * @return true, je-li uzel pravým synem.
	 */
	public boolean isRigth();
	void IncFreq();
}
