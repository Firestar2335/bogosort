public class ExpectedValueThread  extends ResultThread<Fraction> {
	//private Fraction result;
	private final int N;
	private final long n;
	private final boolean makeStr;
	private String str;

	public ExpectedValueThread(int N, long n) {
		this(N, n, false);
	}

	public ExpectedValueThread(int N, long n, boolean makeString) {
		super();
		this.N = N;
		this.n = n;
		makeStr = makeString;
		str = null;
		//this.result = null;
	}

	/*public Fraction getResult() {
		return result;
	}*/

	public void run() {
		//result = BogosortProb.parallelExpectedValue(N,n);
		Fraction f = BogosortProb.parallelExpectedValue(N,n);
		setResult(f);
		if (makeStr) {
			str = f.format();
		}
	}

	public String getString() {
		if (str == null) {
			Fraction r = getResult();
			if (r != null) {
				str = r.format();
			}
		}
		return str;
	}
}
