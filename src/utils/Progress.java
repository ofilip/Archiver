package utils;

/**
 * Prùbìh operace.
 */
public class Progress implements HavingProgress {
	/** Aktuální poèet již provedených krokù. */
	protected long done;
	/** Celkový poèet krokù, které je tøeba provést. */
	protected long todo;
	/** Indikátor inicializace tøídy. */
	protected boolean initialized;
	
	/**
	 * Konstruktor.
	 * Po vytvoøení tøídy tímto konstruktorem je tøeba jí inicializovat.
	 */
	public Progress() {
		initialized = false;
	}
	
	/**
	 * Konstruktor. Tøída se inicializuje dle hodnoty todo.
	 * @param todo Celkový poèet krokù, které je tøeba provést.
	 */
	public Progress(long todo) {
		initialize(todo);
	}
	
	/**
	 * Inicializuje tøídu.
	 * @param todo Celkový poèet krokù, které je tøeba provést.
	 */
	public synchronized void initialize(long todo) {
		this.done = 0;
		this.todo = todo;
		this.initialized = true;
	}
	
	@Override
	public synchronized long getDone() {
		return done;
	}

	@Override
	public synchronized double getDoneRatio() {
		return Math.min(1.0, done/(double)todo);
	}

	@Override
	public synchronized long getTodo() {
		return todo;
	}

	/**
	 * Zvýší aktuální poèet již provedených krokù.
	 * @param a Poèet, o který se zvýší poèet krokù.
	 */
	public synchronized void addToDone(long a) {
		done += a;
	}
	
	/**
	 * Nastaví celkový poèet krokù, které je tøeba provést.
	 * @param todo Nový celkový poèet krokù, které je tøeba provést.
	 */
	public synchronized void setTodo(long todo) {
		this.todo = todo;
	}
	
	/**
	 * Indikuje, zda je tøída inicializována
	 * @return true, je-li tøída inicializována.
	 */
	public boolean isInitialized() {
		return initialized;
	}
}
