package utils;

/**
 * Hodnota boolean zabalená tøídou tak, aby bylo možné jí vracet pøed referenci pøedanou volané metodì.
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
	 * Vrací hodnotu boolean.
	 * @return Hodnota boolean.
	 */
	public boolean getValue() {
		return b;
	}
	
	/**
	 * Nastaví hodnotu boolean.
	 * @param b Nová hodnota boolean.
	 */
	public void setValue(boolean b) {
		this.b = b;
	}
}

