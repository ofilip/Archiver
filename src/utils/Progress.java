package utils;

/**
 * Pr�b�h operace.
 */
public class Progress implements HavingProgress {
	/** Aktu�ln� po�et ji� proveden�ch krok�. */
	protected long done;
	/** Celkov� po�et krok�, kter� je t�eba prov�st. */
	protected long todo;
	/** Indik�tor inicializace t��dy. */
	protected boolean initialized;
	
	/**
	 * Konstruktor.
	 * Po vytvo�en� t��dy t�mto konstruktorem je t�eba j� inicializovat.
	 */
	public Progress() {
		initialized = false;
	}
	
	/**
	 * Konstruktor. T��da se inicializuje dle hodnoty todo.
	 * @param todo Celkov� po�et krok�, kter� je t�eba prov�st.
	 */
	public Progress(long todo) {
		initialize(todo);
	}
	
	/**
	 * Inicializuje t��du.
	 * @param todo Celkov� po�et krok�, kter� je t�eba prov�st.
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
	 * Zv��� aktu�ln� po�et ji� proveden�ch krok�.
	 * @param a Po�et, o kter� se zv��� po�et krok�.
	 */
	public synchronized void addToDone(long a) {
		done += a;
	}
	
	/**
	 * Nastav� celkov� po�et krok�, kter� je t�eba prov�st.
	 * @param todo Nov� celkov� po�et krok�, kter� je t�eba prov�st.
	 */
	public synchronized void setTodo(long todo) {
		this.todo = todo;
	}
	
	/**
	 * Indikuje, zda je t��da inicializov�na
	 * @return true, je-li t��da inicializov�na.
	 */
	public boolean isInitialized() {
		return initialized;
	}
}
