import java.math.BigInteger;

public class ParallelGCD {
	private static class Transformation {
		public final int c;
		public final int d;
		public final int e;
		public final int f;
		public final int g;
		public final boolean sign;

		public Transformation(int c, int d, int e, int f, int g, boolean sign) {
			this.c = c;
			this.d = d;
			this.e = e;
			this.f = f;
			this.g = g;
			this.sign = sign;
		}
	}

	private static final int K = 10;

	/** The transformation table */
	private static Transformation[][][] T = new Transformation[2<<K][2<<K][4*K+4];

	/** Multiplication table for unsigned k-bit integers */
	private static int[][] multTable = null;

	private static void initMultTable(int k) {
		int total = 1 << k;
		multTable = new int[1<<K][1<<K];
		for (int a = 0; a < total; a++) {
			for (int b = a; b < total; b++) {
				multTable[a][b] = a*b;
				multTable[b][a] = multTable[a][b];
			}
		}
	}

	private static void initTransformationTable(int l) {
	
	}

	static {
		//Initialize multiplication table
		initMultTable(K);
	}

	private static int plusMinusGCD(int a, int b) {
		int s = 0;
		while (b != 0) {
			while ((b&1) == 0) {
				b = b>>1;
				s++;
			}
			if (s > 0) {
				int t = a;
				a = b;
				b = t;
				s = -s;
			}
			if (((a+b)&0b10) == 0) {
				b = (a+b)>>1;
			} else {
				b = (a-b)>>1;
			}
		}
		return a;
	}

	private static long plusMinusGCD(long a, long b) {
		//long a = A;
		//long b = B;
		assert ((a&1l) == 1 && b != 0);
		//assert (a % 2 == 1 && b != 0 && Math.abs(a) <= 1l<<n && Math.abs(b) <= 1l<<n);
		int delta = 0;
		while (b != 0) {
			while ((b & 1) == 0) {
				b = b >> 1;
				delta++;
			}
			if (delta > 0) {
				long tmp = a;
				a = b;
				b = tmp;
				delta = -delta;
			}
			if (((a+b)&0b10l)==0) {
				b = (a+b)>>1;
			}
			else {
				b = (a-b)>>1;
			}
		}
		return a;
	}

	private static BigInteger plusMinusGCD(BigInteger a, BigInteger b) {
		assert (a.testBit(0) && !b.equals(BigInteger.ZERO));
		long delta = 0;
		do {
			if (!b.testBit(0)) {
				int n = b.getLowestSetBit();
				b = b.shiftRight(n);
				delta += n;
			}
			if (delta > 0) {
				BigInteger tmp = a;
				a = b;
				b = tmp;
				delta = -delta;
			}
			if (a.add(b).testBit(1)) {
				b = a.subtract(b).shiftRight(1);
			}
			else {
				b = a.add(b).shiftRight(1);
			}
		} while (!b.equals(BigInteger.ZERO));
		return a;
	}

	/*private static int[] applyTransformation(int a, int b, int delta, Transformation f) {

	}*/
}
