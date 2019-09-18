package utils;

/**
 * Indikátor prùbìhu dílèí operace.
 */
public class BasicSubprogressOperation implements SubprogressOperation {
	/** Celkový oèet krokù k dokonèení operace. */
	protected long todo;
	/** Jméno operace. */
	protected String name;
	
	/**
	 * Konstruktor.
	 * @param todo Celkový poèet krokù potøebných k dokonèení operace.
	 * @param name Jméno operace.
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
	 * Nastaví celkový poèet krokù potøebných k dokonèení operace.
	 * @param todo Nový celkový poèet krokù.
	 */
	public void setTodo(long todo) {
		this.todo = todo;
	}
	
	/** 
	 * Nastaví jméno operace.
	 * @param name Nové jméno operace.
	 */
	public void setName(String name) {
		this.name = name;
	}
}
