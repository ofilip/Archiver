package utils;

/**
 * Indik�tor pr�b�hu d�l�� operace.
 */
public class BasicSubprogressOperation implements SubprogressOperation {
	/** Celkov� o�et krok� k dokon�en� operace. */
	protected long todo;
	/** Jm�no operace. */
	protected String name;
	
	/**
	 * Konstruktor.
	 * @param todo Celkov� po�et krok� pot�ebn�ch k dokon�en� operace.
	 * @param name Jm�no operace.
	 */
	public BasicSubprogressOperation(long todo, String name) {
		if (todo<=0) {
			this.todo = 1;
		} else {
			this.todo = todo;
		}
		this.name = name;
	}
	
	@Override
	public long getTodo() {
		return todo;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Nastav� celkov� po�et krok� pot�ebn�ch k dokon�en� operace.
	 * @param todo Nov� celkov� po�et krok�.
	 */
	public void setTodo(long todo) {
		this.todo = todo;
	}
	
	/** 
	 * Nastav� jm�no operace.
	 * @param name Nov� jm�no operace.
	 */
	public void setName(String name) {
		this.name = name;
	}
}
