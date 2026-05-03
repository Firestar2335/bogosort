import java.math.BigInteger;

import java.util.*;

//import static ExtraMath.*;

@SuppressWarnings("unused")
public class BogosortProb {
	
	private static final int CURRENT_N = 25;

	private static final int BATCH_SIZE = 4;
	
	public static void main(String[] args) {
		//info(600l);
		//parallelInfo(30_000_000l);
		//parallelInfo(10543139l);
		//parallelInfo(2326004l, 25);
		Fraction p = parallelChanceMax(25,14,6598052l);
		printFracK(14,p);
		//Fraction exp = expectedValue(8,30);
		//printFrac(exp);
	}

	/**
	 * Prints info on the chances of various numbers showing up
	 * @param shufflesPerMinute
	 */
	public static void info(long shufflesPerMinute) {
		info(shufflesPerMinute/60l,CURRENT_N);
	}

	/**
	 * 
	 * @param s Shuffles per second
	 * @param N
	 */
	public static void info(long s, int N) {
		//Fraction expectedValue = Fraction.ZERO;
		//long s = shufflesPerMinute / 60;
		for (int i = 0; i <= N; i++) {
			//Fraction p = parallelChanceMax(N,i,s);
			Fraction p = chanceMax(N,i,s);
			//expectedValue = expectedValue.add(p.multiply(i));
			printFracK(i,p);
		}
		Fraction expectedValue = expectedValue(N,s);
		System.out.print("The expected value of properly sorted elements is ");
		printFrac(expectedValue);
	}

	/**
	 * Prints info on the chances of various numbers showing up
	 * @param shufflesPerMinute
	 */
	public static void parallelInfo(long shufflesPerMinute) {
		parallelInfo(shufflesPerMinute/60,CURRENT_N);
	}

	/**
	 * 
	 * @param s Shuffles per second
	 * @param N
	 */
	public static void parallelInfo(long s, int N) {
		//Fraction expectedValue = Fraction.ZERO;
		//long s = shufflesPerMinute / 60;
		Deque<ChanceMaxThread> running = new LinkedList<>();
		for (int i = N; i >= 0; i--) {
			ChanceMaxThread t = new ChanceMaxThread(N, i, s);
			t.start();
			running.addFirst(t);
		}
		//Fraction expectedValue = expectedValue(N,s);
		ExpectedValueThread eThread = new ExpectedValueThread(N, s);
		eThread.start();
		while (running.size() > 0) {
			ChanceMaxThread t = running.removeFirst();
			do {
				try {
					t.join();
				} catch (InterruptedException e) {}
			} while (t.isAlive());
			//expectedValue = expectedValue.add(t.getResult().multiply(t.getK()));
			printFracK(t.getK(), t.getResult());
		}
		do {
			try {
				eThread.join();
			} catch (InterruptedException e) {}
		} while (eThread.isAlive());
		System.out.print("The expected value of correctly sorted elements is ");
		printFrac(eThread.getResult());
	}

	public static void parallelInfoBatched(long shufflesPerMinute) {
		parallelInfoBatched(shufflesPerMinute/60, CURRENT_N, BATCH_SIZE);
	}

	/**
	 * 
	 * @param s Shuffles per second
	 * @param N
	 * @param batchSize
	 */
	public static void parallelInfoBatched(long s, int N, int batchSize) {
		//Fraction expectedValue = Fraction.ZERO;
		//long s = shufflesPerMinute / 60;
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
				//expectedValue = expectedValue.add(t.getResult().multiply(t.getK()));
				printFracK(t.getK(), t.getResult());
			}
		}
		Fraction expectedValue = expectedValue(N,s);
		System.out.print("The expected number of properly sorted elements is ");
		printFrac(expectedValue);
	}

	private static void printFracK(int i, Fraction f) {
		System.out.print("The chance of a ");
		System.out.print(i);
		System.out.print(" appearing is ");
		printFrac(f);
	}

	private static void printFrac(Fraction f) {
		if (f.stringLengthEstimate() > Fraction.MAX_LENGTH) {
			System.out.println(f.toBigDecimal());
		}
		else {
			f.toPrintStream(System.out);
			if (!f.isInteger()) {
				System.out.print(" (");
				//System.out.print(f.doubleValue());
				System.out.print(f.toBigDecimal());
				System.out.println(")");
			} else {
				System.out.println();
			}
		}
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
		BigInteger num = ExtraMath.pow(pBelow.add(pK),n).subtract(ExtraMath.pow(pBelow, n));
		return new Fraction(num,ExtraMath.pow(fact,n));
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
		//long[] factors = pfFactorial(N);
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
		/*BigInteger result = BigInteger.ONE;
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
		return result;*/
		return gcdFactorial(a,pfFactorial(n),exponent);
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

	/**
	 * Computes the expected value of the maximum number of properly placed elements in {@code n} 
	 * independent random permutations of {@code N} elements
	 * @param N The total number of elements
	 * @param n The number of shuffles to consider
	 * @return the expected number of properly sorted elements
	 */
	public static Fraction expectedValue(int N, long n) {

		BigInteger sub = ExtraMath.subfactorial(N);//!(N-(i-1))
		BigInteger binom = BigInteger.ONE;//nCr(N,i-1)

		BigInteger curP = sub;//P(x<=i-1)
		BigInteger accum = ExtraMath.pow(curP, n);
		for (int i = 1; i < N; i++) {
			BigInteger d = BigInteger.valueOf(N-i+1);
			sub = ExtraMath.addNegOne(sub,N-i).divide(d);
			binom = binom.multiply(d).divide(BigInteger.valueOf(i));
			curP = curP.add(sub.multiply(binom));
			accum = accum.add(ExtraMath.pow(curP,n));
		}
		
		BigInteger fact = ExtraMath.factorial(N);
		/*BigInteger den = BigInteger.ONE;
		BigInteger g = accum.gcd(fact);
		while (!g.equals(BigInteger.ONE) && n > 0) {
			accum = accum.divide(g);
			den = den.multiply(fact.divide(g));
			n--;
			g = accum.gcd(fact);
		}
		if (n != 0) {
			den = den.multiply(pow(fact,n));
		}*/
		BigInteger den = ExtraMath.pow(fact,n);
		return new Fraction(BigInteger.valueOf(N).multiply(den).subtract(accum),den,false);
	}

	/**
	 * Computes the expected value of the maximum number of properly placed elements in {@code n} 
	 * independent random permutations of {@code N} elements
	 * @param N The total number of elements
	 * @param n The number of shuffles to consider
	 * @return the expected number of properly sorted elements
	 */
	public static Fraction parallelExpectedValue(int N, long n) {

		BigInteger sub = ExtraMath.subfactorial(N);//!(N-(i-1))
		BigInteger binom = BigInteger.ONE;//nCr(N,i-1)

		BigInteger curP = sub;//P(x<=i-1)
		BigInteger accum = ExtraMath.parallelPow(curP, n);
		for (int i = 1; i < N; i++) {
			BigInteger d = BigInteger.valueOf(N-i+1);
			sub = ExtraMath.addNegOne(sub,N-i).divide(d);
			binom = binom.multiply(d).divide(BigInteger.valueOf(i));
			curP = curP.add(sub.multiply(binom));
			accum = accum.add(ExtraMath.parallelPow(curP,n));
		}
		//BigInteger gcd = gcdFactorial(accum,N,n);
		//BigInteger den = parallelPow(ExtraMath.factorial(N),n).divide(gcd);
		//return new Fraction(BigInteger.valueOf(N).multiply(den).subtract(accum.divide(gcd)),den,false);
		BigInteger den = ExtraMath.parallelPow(ExtraMath.factorial(N),n);
		return new Fraction(BigInteger.valueOf(N).multiply(den).subtract(accum),den,false);
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

			//exponential search
			BigInteger divisor;// = prime;
			BigInteger[] dm;// = num.divideAndRemainder(divisor);
			//while (minExp < maxExp) {

			//}
			//main binary search
			while (minExp < maxExp) {
				doUpdate();//if other threads have gotten divisors, use them to reduce the number so that it is smaller
				//long mid = minExp + ((maxExp-minExp)>>1);
				//If there are a lot of elements, give preference to lower exponents
				long mid = minExp + (maxExp-minExp) >> ((minExp == 0 && maxExp - minExp > 1_000_000l) ? 2 : 1);
				divisor = ExtraMath.parallelPow(prime, mid-minExp);//divisor = prime ^ (mid-minExp)
				dm = num.divideAndRemainder(divisor);//dm[0] = num_0/(prime^mid)
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
						//result = divisor.parallelMultiply(parallelPow(prime, minExp));
						result = divisor.multiply(ExtraMath.pow(prime, minExp));
						return;
					}
				}
			}
			//result = parallelPow(prime, minExp);
			result = ExtraMath.pow(prime, minExp);
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
}
