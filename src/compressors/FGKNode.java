package compressors;

/**
 * Uzel stromu pro adaptivn� huffman�v k�d.
 */
public interface FGKNode extends CodeTreeNode {
	/** Po��te�n� po�ad� vytv��en�ho uzlu. */
	static final int INITIAL_ORDER = 512;
	
	/** 
	 * Vrac� �etnost znaku.
	 * @return �etnost znaku.
	 */
	public long getFreq();
	
	/**
	 * Vrac� po�ad� uzlu.
	 * @return Po�ad� uzlu.
	 */
	public int getOrder();
	
	/**
	 * Vrac� n�sleduj�c� uzel dle po�ad� order.
	 * @return N�sleduj�c� uzel dle po�ad� order.
	 */
	public FGKNode getNext();
	
	/**
	 * Nastav� n�sleduj�c� uzel dle po�ad� order.
	 * @param next Nov� n�sleduj�c� uzel dle po�ad� order.
	 */
	void setNext(FGKNode next);
	
	/**
	 *Vrac� p�edch�zej�c� uzel dle po�ad� order.
	 * @return P�edch�zej�c� uzel dle po�ad� order.
	 */
	public FGKNode getPrevious();
	
	/**
	 * Nastav� p�edch�zej�c� uzel dle po�ad� order.
	 * @param previous Nov� p�edch�zej�c� uzel dle po�ad� order.
	 */
	void setPrevious(FGKNode previous);
	
	/**
	 * Nastav� novou hodnotu po�ad� order.
	 * @param order Nov� hodnota po�ad� order.
	 */
	void setOrder(int order);
	
	/**
	 * Vrac� rodi�ovsk� uzel uzlu.
	 * @return Rodi�ovsk� uzel.
	 */
	public FGKBranching getParent();
	
	/**
	 * Nastav� rodi�ovsk� uzel uzlu.
	 * @param parent Nov� rodi�ovsk� uzel.
	 */
	void setParent(FGKBranching parent);
	
	/**
	 * Indikuje, zda je uzel ko�enem stromu.
	 * @return true, je-li uzel ko�enem stromu.
	 */
	public boolean isRoot();
	
	/**
	 * Indijuje, zda je uzel lev�m synem.
	 * @return true, je-li uzel lev�m synem.
	 */
	public boolean isLeft();
	
	/**
	 * Indijuje, zda je uzel prav�m synem.
	 * @return true, je-li uzel prav�m synem.
	 */
	public boolean isRigth();
	void IncFreq();
}
