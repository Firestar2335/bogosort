public class ChanceThread extends ResultThread<Fraction> {
	private final int N;
	private final int k;
	private final boolean makeStr;
	private String str;

	public ChanceThread(int N, int k, boolean makeString) {
		super();
		this.N = N;
		this.k = k;
		this.makeStr = makeString;
		str = null;
	}

	public ChanceThread(int N, int k) {
		this(N, k, false);
	}

	public void run() {
		Fraction f = BogosortProb.chance(N,k);
		setResult(f);
		if (makeStr) {
			str = f.format();
		}
	}

	public int getK() {
		return k;
	}

	public String getString() {
		if (str == null) {
			Fraction r = getResult();
			if (r != null) {
				str = r.format();
			}
			//str = getCurrentResult().format();
		}
		return str;
	}
}
