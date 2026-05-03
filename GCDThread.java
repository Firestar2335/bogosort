import java.math.BigInteger;

public class GCDThread extends ResultThread<BigInteger> {
	private final BigInteger a;
	private final int n;
	private final long exponent;
	//private BigInteger result;

	public GCDThread(BigInteger a, int n, long exponent) {
		super();
		this.a = a;
		this.n = n;
		this.exponent = exponent;
		//result = null;
	}

	public void run() {
		setResult(BogosortProb.gcdFactorial(a, n, exponent));
		//result = BogosortProb.gcdFactorial(a, n, exponent);
	}

	/*public BigInteger getResult() {
		return result;
	}*/
}
