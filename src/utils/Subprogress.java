package utils;

/**
 * Indikátor prùbìhu operace.
 */
public class Subprogress extends Progress implements HavingSubprogress {
	/** Dílèí operace. */
	protected SubprogressOperation[] operations;
	/** Aktuální probíhající dílèí operace. */
	protected int currentOperation;
	/** Aktuální poèet již provedených krokù. */
	protected long currentlyDone;
	
	/**
	 * Kostruktor.
	 */
	public Subprogress() {}
	
	/**
	 * Konstruktor.
	 * @param operations Dílèí operace.
	 */
	public Subprogress(SubprogressOperation[] operations) {
		this(operations, true);
	}
	
	/**
	 * Konstruktor.
	 * @param operations Dílèí operace.
	 * @param cloneOperations Urèuje, zda má být pole dílèích operací klonováno.
	 */
	public Subprogress(SubprogressOperation[] operations, boolean cloneOperations) {
		initialize(operations, cloneOperations);
	}
	
	/**
	 * Spoèítá celkový poèet krokù dílèích operací.
	 * @param operations Dílèí operace.
	 * @return Celkový poèet krokù pøedaných dílèích operací.
	 */
	public static long countTotalTodo(SubprogressOperation[] operations) {
		long result = 0;
		
		for (SubprogressOperation op: operations) {
			result += op.getTodo();
		}
		
		return result;
	}
	
	/**
	 * Spoèítá celkový poèet krokù dílèích operací.
	 * @return Celkový poèet krokù pøedaných dílèích operací.
	 */
	private long countTotalTodo() {
		return countTotalTodo(operations);
	}
	
	/**
	 * Inicializuje tøídu.
	 * @param operations Dílèí operace
	 * @param cloneOperations Urèuje, zda má být pole dílèích operací klonováno.
	 */
	public synchronized void initialize(SubprogressOperation[] operations, boolean cloneOperations) {
		if (cloneOperations) {
			this.operations = operations; 
		} else {
			this.operations = operations.clone();
		}
		this.currentOperation = 0;
		this.currentlyDone = 0;
		this.todo = countTotalTodo();
		initialized = true;
	}
	
	@Override
	public synchronized SubprogressOperation getCurrentOperation() {
		return operations[currentOperation];
	}
	
	@Override
	public synchronized int getCurrentOperationNumber() {
		return currentOperation;
	}

	@Override
	public synchronized int getOperationCount() {
		return operations.length;
	}

	@Override
	public synchronized String getCurrentOperationName() {
		return getCurrentOperation().getName();
	}

	@Override
	public synchronized long getCurrentOperationDone() {
		return currentlyDone;
	}

	@Override
	public synchronized double getCurrentOperationDoneRatio() {
		return Math.min(1.0, currentlyDone/(double)getCurrentOperationTodo());
	}

	@Override
	public synchronized long getCurrentOperationTodo() {
		return getCurrentOperation().getTodo();
	}
	
	/**
	 * Pøidá a krokù k prùbìhu operace.
	 * @param a Poèek krokù pro pøidání.
	 */
	public synchronized void addToCurrentlyDone(long a) {
		done += a;
		currentlyDone += a;
	}
	
	/**
	 * Posune indikátor na další operaci a patøiènì zvýší poèet již provedených krokù.
	 */
	public synchronized void moveToNextOperation() {
		done += getCurrentOperationTodo()-getCurrentOperationDone();
		currentOperation++;
		currentlyDone = 0;
	}
}
