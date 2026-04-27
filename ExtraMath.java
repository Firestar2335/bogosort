import java.math.BigInteger;

public class ExtraMath {

	/**
	 * Computes the factorial of n. The result must be within the range of a long
	 * @param n The number to compute the factorial of
	 * @return The result of n!
	 * @throws ArithmeticException if {@code n! > Long.MAX_VALUE} (if {@code n > 20})
	 * @throws IllegalArgumentException if {@code n < 0}
	 */
	public static long factorialInt(int n) {
		if (n > 20) {
			//                  20!= 2,432,902,008,176,640,000
			//Long.MAX_VALUE=2^63-1= 9,223,372,036,854,775,807
			//                  21!=51,090,942,171,709,440,000
			throw new ArithmeticException("Result would overflow a long");
		}
		if (n < 0) {
			throw new IllegalArgumentException("n was negative");
		}
		long p = 1l;
		for (int i = 2; i <= n; i++) {
			p = p*i;
		}
		return p;
	}


	/**
	 * Computes the factorial of n, or {@code n!}
	 * @param n
	 * @return
	 * @throws IllegalArgumentException if {@code n < 0}
	 */
	public static BigInteger factorial(long n) {
		if (n < 0) {
			throw new IllegalArgumentException("n was negative");
		}
		if (n == 0 || n == 1) {
			return BigInteger.ONE;
		}
		//else if (n <= 20) {
		//	return BigInteger.valueOf(factorialInt((int) n));
		//}
		BigInteger result = BigInteger.ONE;
		long accum = 1;
		for (long i = 2; i <= n; i++) {
			if (accum > Long.MAX_VALUE / i) {
				result = result.multiply(BigInteger.valueOf(accum));
				accum = i;
			}
			else {
				accum *= i;
			}
		}
		result = result.multiply(BigInteger.valueOf(accum));
		return result;
		//return factorial(n-1).multiply(BigInteger.valueOf(n));
	}

	/**
	 * Computes the factorial of n, or {@code n!}, potentially using a parallel algorithm
	 * @param n
	 * @return
	 * @throws IllegalArgumentException if {@code n < 0}
	 */
	public static BigInteger parallelFactorial(long n) {
		if (n < 0) {
			throw new IllegalArgumentException("n was negative");
		}
		if (n == 0 || n == 1) {
			return BigInteger.ONE;
		}
		BigInteger result = BigInteger.ONE;
		long accum = 1;
		for (long i = 2; i <= n; i++) {
			if (accum > Long.MAX_VALUE / i) {
				result = result.parallelMultiply(BigInteger.valueOf(accum));
				accum = i;
			}
			else {
				accum *= i;
			}
		}
		result = result.parallelMultiply(BigInteger.valueOf(accum));
		return result;
		//return factorial(n-1).parallelMultiply(BigInteger.valueOf(n));
	}

	/**
	 * Computes the subfactorial of n, or {@code !n}
	 * @param n
	 * @return
	 */
	public static BigInteger subfactorial(long n) {
		if (n == 0) {
			return BigInteger.ONE;
		}
		else if (n == 1 || n < 0) {
			return BigInteger.ZERO;
		}
		else {
			return addNegOne(subfactorial(n-1).multiply(BigInteger.valueOf(n)),n);
		}
	}

	/**
	 * Computes the subfactorial of n, potentially using a parallel algortihm
	 * @param n
	 * @return
	 */
	public static BigInteger parallelSubfactorial(long n) {
		if (n == 0) {
			return BigInteger.ONE;
		}
		else if (n == 1 || n < 0) {
			return BigInteger.ZERO;
		}
		else {
			return addNegOne(subfactorial(n-1).parallelMultiply(BigInteger.valueOf(n)),n);
		}
	}

	/**
	 * Computes nCr(n,k)
	 * @param n
	 * @param k
	 * @return
	 */
	public static BigInteger binomial(long n, long k) {
		if (n < 0) {
			return ((k&1l)==0) ? binomial(k-1-n,k) : binomial(k-1-n,k).negate();
		}
		else if (k < 0 || k > n) {
			return BigInteger.ZERO;
		}
		else if (k == 0 || k == n) {
			return BigInteger.ONE;
		}
		else {
			return binomial(n-1,k-1).add(binomial(n-1,k));
		}
	}

	/**
	 * Computes whether the provided {@code BigInteger} is even
	 * @param num
	 * @return
	 */
	public static boolean isEven(BigInteger num) {
		return !num.testBit(0);
	}

	/**
	 * Computes {@code a + (-1)^b}
	 * @param a
	 * @param b
	 * @return
	 */
	public static BigInteger addNegOne(BigInteger a, long b) {
		return ((b & 1l) == 0) ? a.add(BigInteger.ONE) : a.subtract(BigInteger.ONE);
	}
}
