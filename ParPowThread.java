import java.math.BigInteger;

public class ParPowThread extends ResultThread<BigInteger>{
	//private BigInteger number;
	private final long exponent;

	public ParPowThread(BigInteger number, long exponent) {
		super();
		//this.number = number;
		this.exponent = exponent;
		setResult(number);
	}

	public void run() {
		//number = ExtraMath.parallelPow(number, exponent);
		setResult(ExtraMath.parallelPow(getCurrentResult(), exponent));
	}

	/*public BigInteger getResult() {
		if (getState() == State.TERMINATED) {
			return number;
		} else {
			return null;
		}
	}*/
}