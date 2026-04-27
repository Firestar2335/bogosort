import java.math.BigInteger;

import java.util.*;

//import static ExtraMath.*;

@SuppressWarnings("unused")
public class BogosortProb {
	private static final long MAX_LENGTH = 500;
	private static final int CURRENT_N = 25;

	private static final int BATCH_SIZE = 4;
	
	public static void main(String[] args) {
		//info(600l);
		parallelInfo(30_000_000l);
		//parallelInfo(10543139l);
	}

	/**
	 * Prints info on the chances of various numbers showing up
	 * @param shufflesPerMinute
	 */
	public static void info(long shufflesPerMinute) {
		info(shufflesPerMinute,CURRENT_N);
	}

	public static void info(long shufflesPerMinute, int N) {
		long s = shufflesPerMinute / 60;
		for (int i = 0; i <= N; i++) {
			//Fraction p = parallelChanceMax(N,i,s);
			Fraction p = chanceMax(N,i,s);
			printFrac(i,p);
		}
	}

	/**
	 * Prints info on the chances of various numbers showing up
	 * @param shufflesPerMinute
	 */
	public static void parallelInfo(long shufflesPerMinute) {
		parallelInfo(shufflesPerMinute,CURRENT_N);
	}

	public static void parallelInfo(long shufflesPerMinute, int N) {
		long s = shufflesPerMinute / 60;
		Deque<ChanceMaxThread> running = new LinkedList<>();
		for (int i = N; i >= 0; i--) {
			ChanceMaxThread t = new ChanceMaxThread(N, i, s);
			t.start();
			running.addFirst(t);
		}
		while (running.size() > 0) {
			ChanceMaxThread t = running.removeFirst();
			do {
				try {
					t.join();
				} catch (InterruptedException e) {}
			} while (t.isAlive());
			printFrac(t.getK(), t.getResult());
		}
	}

	public static void parallelInfoBatched(long shufflesPerMinute) {
		parallelInfoBatched(shufflesPerMinute, CURRENT_N, BATCH_SIZE);
	}

	public static void parallelInfoBatched(long shufflesPerMinute, int N, int batchSize) {
		long s = shufflesPerMinute / 60;
		Deque<ChanceMaxThread> running = new LinkedList<>();
		for (int j = 0; j <= N; j += batchSize) {
			for (int i = Math.min(N+1-j,batchSize)-1; i >=0; i--) {
				ChanceMaxThread t = new ChanceMaxThread(N, j+i, s);
				t.start();
				running.addFirst(t);
			}
			while (running.size() > 0) {
				ChanceMaxThread t = running.removeFirst();
				do {
					try {
						t.join();
					} catch (InterruptedException e) {}
				} while (t.isAlive());
				printFrac(t.getK(), t.getResult());
			}
		}
	}

	private static void printFrac(int i, Fraction f) {
		System.out.print("The chance of a ");
		System.out.print(i);
		System.out.print(" appearing is ");
		if (f.stringLengthEstimate() > MAX_LENGTH) {
			System.out.println(f.toBigDecimal());
		}
		else {
			f.toPrintStream(System.out);
			if (!f.isInteger()) {
				System.out.print(" (");
				System.out.print(f.doubleValue());
				System.out.println(")");
			} else {
				System.out.println();
			}
		}
	}

	

	/**
	 * Performs {@code base.pow(exponent)} using parallel multiplication. 0^0 = 1
	 * @param base
	 * @param exponent
	 * @return The result of {@code base.pow(exponent)}
	 * @throws IllegalArgumentException if {@code exponent < 0}
	 */
	public static BigInteger parallelPow(BigInteger base, long exponent) {
		if (exponent < 0) {
			throw new IllegalArgumentException("Result would not have been an integer");
		}
		else if (exponent == 0) {
			return BigInteger.ONE;
		}
		else if (exponent == 1) {
			return base;
		}
		BigInteger result = BigInteger.ONE;
		while (true) {
			if ((exponent & 1) == 1) {
				result = result.parallelMultiply(base);
			}
			exponent = exponent >> 1;
			if (exponent == 0) {
				return result;
			}
			base = base.parallelMultiply(base);
		}
		/*else if ((exponent & 1) == 0) {//Even exponent
			BigInteger half = parallelPow(base,exponent >> 1);
			return half.parallelMultiply(half);
		}
		else {
			return parallelPow(base,exponent-1).parallelMultiply(base);
		}*/
	}

	/**
	 * Computes the result of {@code base.pow(exponent)}. 0^0=1
	 * @param base
	 * @param exponent
	 * @return
	 * @throw ArithmeticException if {@code exponent < 0}
	 */
	public static BigInteger pow(BigInteger base, long exponent) {
		if (exponent < 0) {
			throw new ArithmeticException("Result would not have been an integer");
		}
		else if (exponent == 0) {
			return BigInteger.ONE;
		}
		else if (exponent == 1) {
			return base;
		}
		else if (exponent < Integer.MAX_VALUE) {
			return base.pow((int)exponent);
		}
		BigInteger result = BigInteger.ONE;
		while (true) {
			if ((exponent & 1) == 1) {
				result = result.multiply(base);
			}
			exponent = exponent >> 1;
			if (exponent == 0) {
				return result;
			}
			base = base.multiply(base);
		}
		/*else if (exponent % 2 == 0) {
			BigInteger half = pow(base, exponent / 2);
			return half.multiply(half);
		}
		else {
			return pow(base, exponent - 1).multiply(base);
		}*/
	}

	/**
	 * Computes the chance that exactly k items are sorted properly in a random permutation
	 * of N elements
	 * @param N The total number of elements
	 * @param k The number of properly sorted elements
	 * @return The result of {@code P(X=k)}
	 */
	public static Fraction chance(int N, int k) {
		return new Fraction(ExtraMath.binomial(N,k).multiply(ExtraMath.subfactorial(N-k)),ExtraMath.factorial(N));
	}

	/**
	 * Computes the chance that at least k elements are sorted properly in a random permutation of 
	 * N elements
	 * @param N The total number of elements
	 * @param k The number of properly sorted elements
	 * @return The result of {@code P(X>=k)}
	 */
	public static Fraction chanceAbove(int N, int k) {
		BigInteger total = BigInteger.ZERO;
		BigInteger sub = BigInteger.ONE;//sub = !j
		BigInteger binom = BigInteger.ONE;//binom = nCr(n,j)
		for (int j = 0; j <=N-k; j++) {
			total = total.add(sub.parallelMultiply(binom));
			sub = ExtraMath.addNegOne(sub.multiply(BigInteger.valueOf(j+1)),j^1);
			binom = binom.multiply(BigInteger.valueOf(N-j)).divide(BigInteger.valueOf(j+1));
		}
		return new Fraction(total, ExtraMath.factorial(N));
	}


	/**
	 * Computes the probability that at most k elements are sorted properly in a random permutation
	 * of N elements
	 * @param N The total number of elements
	 * @param k The number of properly sorted elements
	 * @return The result of {@code P(X<=k)}
	 */
	public static Fraction chanceBelow(int N, int k) {
		BigInteger total = BigInteger.ZERO;
		BigInteger sub = ExtraMath.subfactorial(N-k);//sub=!j
		BigInteger binom = ExtraMath.binomial(N,k);//binom == nCr(n,j)
		for (int j = N-k; j <= N; j++) {
			total = total.add(sub.multiply(binom));
			sub = ExtraMath.addNegOne(sub.multiply(BigInteger.valueOf(j+1)),j^1);
			binom = binom.multiply(BigInteger.valueOf(N-j)).divide(BigInteger.valueOf(j+1));
		}
		return new Fraction(total, ExtraMath.factorial(N));
	}

	/**
	 * Computes the chance that strictly less than k elements are sorted properly in a random 
	 * permutation of N elements. 
	 * @param N The total number of elements
	 * @param k The number of sorted elements
	 * @return The result of {@code P(X<k)}
	 */
	public static Fraction chanceBelowStrict(int N, int k) {
		BigInteger total = BigInteger.ZERO;
		BigInteger sub = ExtraMath.subfactorial(N-k+1);//sub = !j
		BigInteger binom = ExtraMath.binomial(N,k-1);//binom = nCr(N,j)
		for (int j = N-k+1; j <= N; j++) {
			total = total.add(sub.multiply(binom));
			sub = ExtraMath.addNegOne(sub.multiply(BigInteger.valueOf(j+1)),j^1);
			binom = binom.multiply(BigInteger.valueOf(N-j)).divide(BigInteger.valueOf(j+1));
		}
		return new Fraction(total, ExtraMath.factorial(N));
	}

	/**
	 * Computes the chance that out of {@code n} independent random permutations of {@code N} 
	 * elements, the maximum number of elements properly sorted is {@code k}
	 * @param N The total number of elements
	 * @param k The number of properly sorted elements
	 * @param n The number of permutations sampled
	 * @return The chance that the maximum number of sorted elements is {@code k}
	 */
	public static Fraction chanceMax(int N, int k, long n) {
		BigInteger binom = ExtraMath.binomial(N,k);//binom = nCr(N,j-1)
		BigInteger sub = ExtraMath.subfactorial(N-k);//sub = !(j-1)
		BigInteger pK = binom.multiply(sub);
		BigInteger pBelow = BigInteger.ZERO;
		for (int j = N-k+1; j <= N; j++) {
			binom = binom.multiply(BigInteger.valueOf(N+1-j)).divide(BigInteger.valueOf(j));
			sub = ExtraMath.addNegOne(sub.multiply(BigInteger.valueOf(j)),j);
			pBelow = pBelow.add(binom.multiply(sub));
		}
		//pK = P(X=k)*N!
		//pBelow = P(X<k)*N!
		BigInteger fact = ExtraMath.factorial(N);
		//System.out.println("Addition complete");
		BigInteger num = pow(pBelow.add(pK),n).subtract(pow(pBelow, n));
		return new Fraction(num,pow(fact,n));
	}

	/**
	 * Computes the chance that out of {@code n} independent random permutations of {@code N} 
	 * elements, the maximum number of elements properly sorted is {@code k}. This implementation
	 * potentially uses a parallel algorithm
	 * @param N The total number of elements
	 * @param k The number of properly sorted elements
	 * @param n The number of permutations sampled
	 * @return The chance that the maximum number of sorted elements is {@code k}
	 */
	public static Fraction parallelChanceMax(int N, int k, long n) {
		BigInteger binom = ExtraMath.binomial(N,k);//binom = nCr(N,j-1)
		BigInteger sub = ExtraMath.parallelSubfactorial(N-k);//sub = !(j-1)
		BigInteger pK = binom.parallelMultiply(sub);
		BigInteger pBelow = BigInteger.ZERO;
		for (int j = N-k+1; j <= N; j++) {
			binom = binom.parallelMultiply(BigInteger.valueOf(N+1-j)).divide(BigInteger.valueOf(j));
			sub = ExtraMath.addNegOne(sub.parallelMultiply(BigInteger.valueOf(j)),j);
			pBelow = pBelow.add(binom.parallelMultiply(sub));
		}
		//pK = P(X=k)*N!
		//pBelow = P(X<k)*N!
		
		ParPowThread threadA = new ParPowThread(pBelow.add(pK),n);
		threadA.start();
		ParPowThread threadB = new ParPowThread(pBelow, n);
		threadB.start();
		BigInteger fact = ExtraMath.parallelFactorial(N);
		ParPowThread threadDen = new ParPowThread(fact,n);
		threadDen.start();
		long[] factors = pfFactorial(N);
		do {
			try {
				threadB.join();
			} catch (InterruptedException e) { }
		} while (threadB.isAlive());
		do {
			try {
				threadA.join();
			} catch (InterruptedException e) { }
		} while (threadA.isAlive());
		BigInteger numUnreduced = threadA.getResult().subtract(threadB.getResult());
		//BigInteger gcd = gcdFactorial(numUnreduced, factors, n);
		//BigInteger num = numUnreduced.divide(gcd);
		BigInteger num = numUnreduced;
		do {
			try {
				threadDen.join();
			} catch (InterruptedException e) { }
		} while (threadDen.isAlive());
		//BigInteger den = threadDen.getResult().divide(gcd);
		BigInteger den = threadDen.getResult();
		return new Fraction(num,den,false);
	}

	/**
	 * Computes the chance that the maximum of properly sorted elements in {@code n} random 
	 * permutations of {@code N} elements is at least {@code k}
	 * @param N The total number of elements
	 * @param k The desired number of properly sorted elements
	 * @param n The number of shuffles
	 * @return The chance Q(M>=k)
	 */
	public static Fraction chanceMaxAbove(int N, int k, long n) {
		Fraction f = chanceBelowStrict(N,k);
		return Fraction.ONE.subtract(f.parallelPow(n));
	}

	/**
	 * Computes {@code gcd(a, (n!)^exponent)}
	 * @param a The first number
	 * @param n The number of the factorial
	 * @param exponent The power by which to raise the factorial
	 * @return
	 */
	public static BigInteger gcdFactorial(BigInteger a, int n, long exponent) {
		BigInteger result = BigInteger.ONE;
		long[] factors = pfFactorial(n);
		BigInteger[] dm;
		for (int i = 0; i < factors.length; i++) {
			if (factors[i] == 0) {
				continue;
			}
			BigInteger p = BigInteger.valueOf(i+2);
			long max = factors[i] * exponent;
			for (long c = 0; c < max; c++) {
				dm = a.divideAndRemainder(p);
				if (!dm[1].equals(BigInteger.ZERO)) {
					break;
				}
				else {
					result = result.multiply(p);
					a = dm[0];
				}
			}
		}
		return result;
	}

	public static BigInteger gcdFactorial(BigInteger a, long[] factors, long exponent) {
		BigInteger result = BigInteger.ONE;
		Deque<DividerThread> threads = new LinkedList<>();
		for (int i = 0; i < factors.length; i++) {
			if (factors[i] == 0) {
				continue;
			}
			threads.addFirst(new DividerThread(a,i+2,factors[i] * exponent));
			threads.getFirst().start();
		}
		while (threads.size() > 0) {
			DividerThread t = threads.removeFirst();
			t.updateGCDs(result);
			do {
				try {
					t.join();
				} catch  (InterruptedException e) { }
			} while (t.isAlive());
			//System.out.println("Thread joined");
			result = result.parallelMultiply(t.getResult());
		}
		return result;
	}

	/**
	 * Produces the prime factorization of the factorial of n. It is returned in a list containing 
	 * numbers such that {@code n = sum((a[i]+2)^a[i])}. In this case, only indices 2 less than a
	 * prime number will have numbers other than 0.
	 * @param num
	 * @return
	 */
	public static long[] pfFactorial(int n) {
		int[] primes = genPrimes(n);
		long[] result = new long[primes[primes.length-1]-1];
		for (int prime : primes) {
			int sum= 0;
			int prod = prime;
			int lastTerm = 0;
			do {
				lastTerm = n / prod;
				sum += lastTerm;
				prod *= prime;
			} while (lastTerm > 0);
			result[prime-2] = sum;
		}
		return result;
	}

	/**
	 * Generates primes up to and possibly including {@code n}
	 * @param n
	 * @return
	 */
	public static int[] genPrimes(int n) {
		boolean[] nums = new boolean[n+1];
		nums[0] = true; nums[1] = true;
		for (int i = 2; i <= n; i++) {
			if (!nums[i]) {
				for (int j = i*i; j <= n; j += i) {
					nums[j] = true;
				}
			}
		}
		List<Integer> primes = new ArrayList<>();
		for (int i = 0; i < nums.length; i++) {
			if (!nums[i]) {
				primes.add(i);
			}
		}
		int[] result = new int[primes.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = primes.get(i);
		}
		return result;
	}

	private static class ParPowThread extends Thread {
		private BigInteger number;
		private final long exponent;

		public ParPowThread(BigInteger number, long exponent) {
			this.number = number;
			this.exponent = exponent;
		}

		public void run() {
			number = parallelPow(number, exponent);
		}

		public BigInteger getResult() {
			if (getState() == State.TERMINATED) {
				return number;
			} else {
				return null;
			}
		}
	}

	private static class GCDThread extends Thread {
		private final BigInteger a;
		private final int n;
		private final long exponent;
		private BigInteger result;

		public GCDThread(BigInteger a, int n, long exponent) {
			super();
			this.a = a;
			this.n = n;
			this.exponent = exponent;
			result = null;
		}

		public void run() {
			result = gcdFactorial(a, n, exponent);
		}

		public BigInteger getResult() {
			return result;
		}
	}

	private static class DividerThread extends Thread {
		private BigInteger num;
		private BigInteger prime;
		private long maxExponent;
		private BigInteger result;

		private BigInteger laterGCDs;

		public DividerThread(BigInteger num, int prime, long maxExponent) {
			super();
			this.num = num;
			this.prime = BigInteger.valueOf(prime);
			this.maxExponent = maxExponent;
			result = BigInteger.ONE;
			laterGCDs = BigInteger.ONE;
		}

		public void run() {
			if (prime.equals(BigInteger.TWO)) {
				int count = num.getLowestSetBit();
				count = Math.min(count, Math.clamp(maxExponent, Integer.MIN_VALUE, Integer.MAX_VALUE));
				if (count >= 0) {
					result = BigInteger.ONE.shiftLeft(count);
				}
			}
			else {
				powerBinarySearch();
			}
			/*else {
				powerLinearSeaarch();
			}*/
		}

		/**
		 * Updates the value of num to include factors produced by other threads
		 */
		private void doUpdate() {
			if (interrupted()) {
				synchronized (prime) {
					num = num.divide(laterGCDs);
					laterGCDs = BigInteger.ONE;
				}
			}
		}

		/**
		 * Attempts to find the proper exponent using a binary search
		 */
		private void powerBinarySearch() {
			long minExp = 0;
			long maxExp = maxExponent;
			//num = num_0 / (prime ^ minExp)
			while (minExp < maxExp) {
				doUpdate();
				long mid = (minExp+maxExp)/2;
				BigInteger divisor = parallelPow(prime, mid-minExp);//divisor = prime ^ (mid-minExp)
				BigInteger[] dm = num.divideAndRemainder(divisor);//dm[0] = num_0/(prime^mid)
				//Case 1: prime^mid does not divide num - mid is too large
				if (!dm[1].equals(BigInteger.ZERO)) {
					maxExp = mid-1;
				} else {
					dm = dm[0].divideAndRemainder(prime);//dm[0] = num_0/(prime^(mid+1))
					//Case 2: prime^mid divides num and prime^(mid+1) also divides num - mid is too small
					if (dm[1].equals(BigInteger.ZERO)) {
						num = dm[0];
						minExp = mid+1;
					}//Case 3: prime^mid divides num but prime^(mid+1) doesn't - mid is correct
					else {
						result = divisor.parallelMultiply(parallelPow(prime, minExp));
						return;
					}
				}
			}
			result = parallelPow(prime, minExp);
		}
		
		/**
		 * Searches for the proper power by trying all powers sequentially
		 */
		private void powerLinearSearch() {
			BigInteger[] dm;
			for (long c = 0; c < maxExponent; c++) {
				dm = num.divideAndRemainder(prime);
				if (!dm[1].equals(BigInteger.ZERO)) {
					break;
				}
				else {
					result = result.multiply(prime);
					num = dm[0];
				}
				doUpdate();
			}
		}

		public void updateGCDs(BigInteger current) {
			synchronized(prime) {
				laterGCDs = laterGCDs.multiply(current);
			}
			interrupt();
		}

		public BigInteger getResult() {
			if (getState() == State.TERMINATED) {
				return result;
			} else {
				return null;
			}
		}
	}

	private static class ChanceMaxThread extends Thread {
		private Fraction result;
		private final int N;
		private final int k;
		private final long n;

		public ChanceMaxThread(int N, int k, long n) {
			super();
			this.N = N;
			this.k = k;
			this.n = n;
			result = null;
		}

		public void run() {
			result = parallelChanceMax(N,k,n);
		}

		public Fraction getResult() {
			if (isAlive()) {
				return null;
			} else {
				return result;
			}
		}

		public int getK() {
			return k;
		}
	}
}
