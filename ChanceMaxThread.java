public class ChanceMaxThread extends ResultThread<Fraction> {
	//private Fraction result;
	private final int N;
	private final int k;
	private final long n;
	private final boolean makeStr;
	private String str;

	public ChanceMaxThread(int N, int k, long n, boolean makeString) {
		super();
		this.N = N;
		this.k = k;
		this.n = n;
		makeStr = makeString;
		str = null;
		//result = null;
	}

	public ChanceMaxThread(int N, int k, long n) {
		this(N,k,n,false);
	}

	public void run() {
		//result = BogosortProb.parallelChanceMax(N,k,n);
		Fraction f = BogosortProb.parallelChanceMax(N,k,n);
		setResult(f);
		if (makeStr) {
			str = f.format();
		}
	}

	/*public Fraction getResult() {
		if (isAlive()) {
			return null;
		} else {
			return result;
		}
	}*/

	public int getK() {
		return k;
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
