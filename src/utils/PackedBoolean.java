package utils;

/**
 * Hodnota boolean zabalen� t��dou tak, aby bylo mo�n� j� vracet p�ed referenci p�edanou volan� metod�.
 */
public class PackedBoolean {
	/** Hodnota boolean. */
	private boolean b;
	
	/** Konstanta TRUE. */
	public static final PackedBoolean TRUE = new PackedBoolean(true);
	/** Konstanta FALSE. */
	public static final PackedBoolean FALSE = new PackedBoolean(false);
	
	/**
	 * Konstruktor.
	 * @param b Hodnota boolean.
	 */
	public PackedBoolean(boolean b) {
		this.b = b;
	}
	
	/**
	 * Vrac� hodnotu boolean.
	 * @return Hodnota boolean.
	 */
	public boolean getValue() {
		return b;
	}
	
	/**
	 * Nastav� hodnotu boolean.
	 * @param b Nov� hodnota boolean.
	 */
	public void setValue(boolean b) {
		this.b = b;
	}
}

