/**
 * A thread that produces a result
 */
public abstract class ResultThread<T> extends Thread {
	private T result;

	public ResultThread() {
		super();
		result = null;
	}

	/**
	 * Sets the result of this computation
	 * @param value the value to set the result to
	 */
	protected void setResult(T value) {
		result = value;
	}

	/**
	 * Fetchs the current value in {@code result}
	 * @return
	 */
	protected T getCurrentResult() {
		return result;
	}

	/**
	 * Returns the result of this thread's computation. If the thread has not finished, then 
	 * {@code null} is returned
	 * @return The result of the computation if the computation has concluded, {@code null} otherwise
	 */
	public T getResult() {
		if (getState() != State.NEW && !isAlive()) {
			return result;
		}
		return null;
	}
}
