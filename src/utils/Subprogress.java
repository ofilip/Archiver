package utils;

/**
 * Indik�tor pr�b�hu operace.
 */
public class Subprogress extends Progress implements HavingSubprogress {
	/** D�l�� operace. */
	protected SubprogressOperation[] operations;
	/** Aktu�ln� prob�haj�c� d�l�� operace. */
	protected int currentOperation;
	/** Aktu�ln� po�et ji� proveden�ch krok�. */
	protected long currentlyDone;
	
	/**
	 * Kostruktor.
	 */
	public Subprogress() {}
	
	/**
	 * Konstruktor.
	 * @param operations D�l�� operace.
	 */
	public Subprogress(SubprogressOperation[] operations) {
		this(operations, true);
	}
	
	/**
	 * Konstruktor.
	 * @param operations D�l�� operace.
	 * @param cloneOperations Ur�uje, zda m� b�t pole d�l��ch operac� klonov�no.
	 */
	public Subprogress(SubprogressOperation[] operations, boolean cloneOperations) {
		initialize(operations, cloneOperations);
	}
	
	/**
	 * Spo��t� celkov� po�et krok� d�l��ch operac�.
	 * @param operations D�l�� operace.
	 * @return Celkov� po�et krok� p�edan�ch d�l��ch operac�.
	 */
	public static long countTotalTodo(SubprogressOperation[] operations) {
		long result = 0;
		
		for (SubprogressOperation op: operations) {
			result += op.getTodo();
		}
		
		return result;
	}
	
	/**
	 * Spo��t� celkov� po�et krok� d�l��ch operac�.
	 * @return Celkov� po�et krok� p�edan�ch d�l��ch operac�.
	 */
	private long countTotalTodo() {
		return countTotalTodo(operations);
	}
	
	/**
	 * Inicializuje t��du.
	 * @param operations D�l�� operace
	 * @param cloneOperations Ur�uje, zda m� b�t pole d�l��ch operac� klonov�no.
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
	 * P�id� a krok� k pr�b�hu operace.
	 * @param a Po�ek krok� pro p�id�n�.
	 */
	public synchronized void addToCurrentlyDone(long a) {
		done += a;
		currentlyDone += a;
	}
	
	/**
	 * Posune indik�tor na dal�� operaci a pat�i�n� zv��� po�et ji� proveden�ch krok�.
	 */
	public synchronized void moveToNextOperation() {
		done += getCurrentOperationTodo()-getCurrentOperationDone();
		currentOperation++;
		currentlyDone = 0;
	}
}
