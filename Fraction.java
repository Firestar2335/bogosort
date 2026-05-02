import java.math.*;
import java.io.PrintStream;

public class Fraction extends Number implements Comparable<Fraction> {
	private static final long DOUBLE_SIG_MASK = 0x000f_ffff_ffff_ffffl;
	private static final int FLOAT_SIG_MASK = 0x7f_ffff;

	private static final int DOUBLE_EXP_BIAS = 1023;
	private static final int FLOAT_EXP_BIAS = 127;

	private static final double SCALE = Math.log10(2);

	/** The fraction with a value of 1 */
	public static final Fraction ONE = new Fraction(1,1);

	/** The fraction with a value of 0 */
	public static final Fraction ZERO = new Fraction(0,1);

	/** The numerator */
	private final BigInteger num;
	/** The denominator. This will always be positive */
	private final BigInteger den;

	public Fraction(BigInteger numerator, BigInteger denominator) {
		this(numerator, denominator, true);
	}

	protected Fraction(BigInteger numerator, BigInteger denominator, boolean check) {
		if (numerator.equals(BigInteger.ZERO)) {
			num = BigInteger.ZERO;
			den = BigInteger.ONE;
		}
		else if (check) {
			int sign = denominator.signum();
			if (sign == 0) {
				throw new ArithmeticException("Denominator was 0");
			} else if (sign == -1) {
				denominator = denominator.negate();
				numerator = numerator.negate();
			}
			BigInteger gcd = numerator.abs().gcd(denominator);
			num = numerator.divide(gcd);
			den = denominator.divide(gcd);
		}
		else {
			num = numerator;
			den = denominator;
		}
	}

	public Fraction(long numerator, long denominator) {
		if (numerator == 0) {
			num = BigInteger.ZERO;
			den = BigInteger.ONE;
		} else {
			if (denominator == 0) {
				throw new ArithmeticException("Denominator was zero");
			} else if (denominator < 0) {
				numerator = -numerator;
				denominator = -denominator;
			}
			long gcd = gcd(numerator, denominator);
			num = BigInteger.valueOf(numerator/gcd);
			den = BigInteger.valueOf(denominator/gcd);
		}
	}

	/**
	 * Computes the greatest common divisor of {@code a} and {@code b}
	 * @param a
	 * @param b
	 * @return
	 */
	private static long gcd(long a, long b) {
		a = Math.abs(a); b = Math.abs(b);
		while (b != 0) {
			long tmp = b;
			b = a % b;
			a = tmp;
		}
		return a;
	}

	/**
	 * Computes the least common multiple of {@code a} and {@code b}
	 * @param a
	 * @param b
	 * @return
	 */
	private static long lcm(long a, long b) {
		if (a == 0 && b == 0) {
			return 0l;
		}
		a = Math.abs(a);
		b = Math.abs(b);
		return a * (b / gcd(a,b));
	}

	/**
	 * Computes the least common multiple of {@code a} and {@code b}
	 * @param a
	 * @param b
	 * @return
	 */
	private static BigInteger lcm(BigInteger a, BigInteger b) {
		if (a.equals(BigInteger.ZERO) && b.equals(BigInteger.ZERO)) {
			return BigInteger.ZERO;
		}
		a = a.abs();
		b = b.abs();
		return a.min(b).multiply(a.max(b).divide(a.gcd(b)));
	}

	/**
	 * Returns the number of decimal digits in the base 10 representation of num
	 * @param num
	 * @return
	 */
	private static long integerDigits(BigInteger num) {
		num = num.abs();
		long count = 1;
		while (num.compareTo(BigInteger.TEN) >= 0) {
			count++;
			num = num.divide(BigInteger.TEN);
		}
		return count;
		//int bits = num.bitLength();
	}

	/**
	 * Computes an estimate of the number of digits in the decimal representation of this number
	 * @param num
	 * @return
	 */
	private static long integerDigitEstimate(BigInteger num) {
		return (long) (num.bitLength() * SCALE);
	}

	public BigDecimal toBigDecimal() {
		return toBigDecimal(MathContext.DECIMAL128);
	}

	public BigDecimal toBigDecimal(MathContext mc) {
		BigDecimal num = new BigDecimal(this.num);
		BigDecimal den = new BigDecimal(this.den);

		return num.divide(den,mc);
	}

	//A number with a magnitude of at least b^max_exponent*(b-b^(1-p)/2) rounds to infinity
	//at least 2^max_exponent * (2-2^(-p))

	public double doubleValue() {
		//Round ties to even
		//if (den.equals(BigInteger.ONE)) {
		//	return num.doubleValue();
		//}
		int sign = num.signum();
		if (sign == 0) {
			return 0.0;
		}
		BigInteger[] dm = num.abs().divideAndRemainder(den);
		
		BigInteger intPart = dm[0];

		int prec = intPart.bitLength();

		int fracLength = 0;
		int leadingZeros = 0;
		BigInteger fracPart = BigInteger.ZERO;
		while (prec < Double.PRECISION) {
			dm = dm[1].shiftLeft(1).divideAndRemainder(den);
			fracPart = fracPart.shiftLeft(1).or(dm[0]);
			fracLength++;
			if (prec != 0 || leadingZeros > -Double.MIN_EXPONENT-1 || fracPart.signum() != 0) {
				prec++;
			}
			else {
				leadingZeros++;
			}
		}
		//next bit determines 
		//System.out.println(fracPart);
		//System.out.println(leadingZeros);
		int exponent;
		if (intPart.equals(BigInteger.ZERO)) {
			exponent = Math.max(Double.MIN_EXPONENT, -leadingZeros-1);
		} else {
			exponent = intPart.bitLength()-1;
		}
		if (exponent > Double.MAX_EXPONENT) {
			return (sign > 0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		}
		//System.out.println(exponent);
		long sigBits = intPart.shiftRight(exponent-Double.PRECISION+1).or(fracPart.shiftRight(exponent+fracLength+1-Double.PRECISION)).longValue();
		long expBits;
		if ((sigBits & ~DOUBLE_SIG_MASK) == 0) {
			expBits = 0l;
		} else {
			expBits = ((long) (exponent + DOUBLE_EXP_BIAS)) << (Double.PRECISION-1);
		}
		//System.out.println(sigBits);
		sigBits = sigBits & DOUBLE_SIG_MASK;

		long bits = sigBits | expBits | (sign == -1 ? 0x8000_0000_0000_0000l : 0);
		return Double.longBitsToDouble(bits);
		//return toBigDecimal().doubleValue();
	}

	public float floatValue() {
		//if (den.equals(BigInteger.ONE)) {
		//	return den.floatValue();
		//}
		int sign = num.signum();
		if (sign == 0) {
			return 0.0f;
		}
		BigInteger[] dm = num.abs().divideAndRemainder(den);
		
		BigInteger intPart = dm[0];

		int prec = intPart.bitLength();

		int fracLength = 0;
		int leadingZeros = 0;
		BigInteger fracPart = BigInteger.ZERO;
		while (prec < Float.PRECISION) {
			dm = dm[1].shiftLeft(1).divideAndRemainder(den);
			fracPart = fracPart.shiftLeft(1).or(dm[0]);
			fracLength++;
			if (prec != 0 || leadingZeros > -Float.MIN_EXPONENT-1 || fracPart.signum() != 0) {
				prec++;
			}
			else {
				leadingZeros++;
			}
		}
		int exponent;
		if (intPart.equals(BigInteger.ZERO)) {
			exponent = Math.max(Float.MIN_EXPONENT, -leadingZeros-1);
		} else {
			exponent = intPart.bitLength()-1;
		}
		if (exponent > Float.MAX_EXPONENT) {
			return (sign > 0) ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
		}
		int sigBits = intPart.shiftRight(exponent-Float.PRECISION+1).or(fracPart.shiftRight(exponent+fracLength+1-Float.PRECISION)).intValue();
		int expBits;
		if ((sigBits & ~FLOAT_SIG_MASK) == 0) {
			expBits = 0;
		} else {
			expBits = ((exponent + FLOAT_EXP_BIAS)) << (Float.PRECISION-1);
		}
		sigBits = sigBits & FLOAT_SIG_MASK;

		int bits = sigBits | expBits | (sign == -1 ? 0x8000_0000 : 0);
		return Float.intBitsToFloat(bits);
		//return toBigDecimal().floatValue();
	}

	public BigInteger toBigInteger() {
		return num.divide(den);
	}

	public long longValue() {
		return toBigInteger().longValue();
	}

	public int intValue() {
		return toBigInteger().intValue();
	}

	public short shortValue() {
		return toBigInteger().shortValue();
	}

	public byte byteValue() {
		return toBigInteger().byteValue();
	}

	/**
	 * Computes the result of {@code this + other}
	 * @param other
	 * @return
	 */
	public Fraction add(Fraction other) {
		if (den.equals(BigInteger.ONE)) {
			return new Fraction(num.multiply(other.den).add(other.num),other.den,false);
		}
		else if (other.den.equals(BigInteger.ONE)) {
			return new Fraction(num.add(other.num.multiply(den)),den,false);
		}
		else if (den.equals(other.den)) {
			BigInteger newNum = num.add(other.num);
			BigInteger g = newNum.gcd(den);
			if (g.equals(BigInteger.ONE)) {
				return new Fraction(newNum,den, false);
			} else {
				return new Fraction(newNum.divide(g),den.divide(g), false);
			}
		}
		BigInteger g = den.gcd(other.den);//lcm = lcm(den,other.den);
		if (g.equals(BigInteger.ONE)) {
			return new Fraction(num.multiply(other.den).add(other.num.multiply(den)),den.multiply(other.den),false);
		}
		BigInteger a = den.divide(g);
		BigInteger b = other.den.divide(g);

		BigInteger newNum = num.multiply(b).add(other.num.multiply(a));
		BigInteger reducingGCD = newNum.gcd(g);
		if (reducingGCD.equals(BigInteger.ONE)) {
			return new Fraction(newNum, den.multiply(b),false);
		}
		else {
			return new Fraction(newNum.divide(reducingGCD),den.divide(reducingGCD).multiply(b),false);
		}
	}

	/**
	 * Computes {@code this - other}
	 * @param other
	 * @return
	 */
	public Fraction subtract(Fraction other) {
		if (den.equals(BigInteger.ONE)) {
			return new Fraction(num.multiply(other.den).subtract(other.num),other.den,false);
		}
		else if (other.den.equals(BigInteger.ONE)) {
			return new Fraction(num.subtract(other.num.multiply(den)),den,false);
		}
		else if (den.equals(other.den)) {
			BigInteger newNum = num.subtract(other.num);
			BigInteger g = newNum.gcd(den);
			if (g.equals(BigInteger.ONE)) {
				return new Fraction(newNum,den, false);
			}
			else {
				return new Fraction(newNum.divide(g),den.divide(g), false);
			}
		}
		BigInteger g = den.gcd(other.den);//lcm = lcm(den,other.den);
		if (g.equals(BigInteger.ONE)) {
			return new Fraction(num.multiply(other.den).subtract(other.num.multiply(den)),den.multiply(other.den),false);
		}
		BigInteger a = den.divide(g);
		BigInteger b = other.den.divide(g);

		BigInteger newNum = num.multiply(b).subtract(other.num.multiply(a));
		BigInteger reducingGCD = newNum.gcd(g);
		if (reducingGCD.equals(BigInteger.ONE)) {
			return new Fraction(newNum, den.multiply(b),false);
		}
		else {
			return new Fraction(newNum.divide(reducingGCD),den.divide(reducingGCD).multiply(b),false);
		}
	}

	/**
	 * Attempts to perform {@code this-other} without reducing the denominators
	 * @param other
	 * @return
	 * @throws IllegalArgumentException if the denominators of {@code this} and {@code other} are not equal
	 */
	public Fraction subtractNoReduce(Fraction other) {
		if (!den.equals(other.den)) {
			throw new IllegalArgumentException("The denominators of the provided fractions were not equal");
		}
		return new Fraction(num.subtract(other.num),den,false);
	}

	/**
	 * Computes {@code this * other}
	 * @param other
	 * @return
	 */
	public Fraction multiply(Fraction other) {
		BigInteger gcdA = num.gcd(other.den);
		BigInteger gcdB = other.num.gcd(den);
		return new Fraction(num.divide(gcdA).multiply(other.num.divide(gcdB)),den.divide(gcdB).multiply(other.den.divide(gcdA)));
		//return new Fraction(num.multiply(other.num), den.multiply(other.den));
	}

	public Fraction multiply(long other) {
		BigInteger factor = BigInteger.valueOf(other);
		BigInteger gcd = factor.gcd(den);
		return new Fraction(num.multiply(factor.divide(gcd)),den.divide(gcd));
	}

	/**
	 * Computes {@code this / other}
	 * @param other
	 * @return
	 * @throws ArithmeticException if {@code other} is zero
	 */
	public Fraction divide(Fraction other) {
		if (other.num.equals(BigInteger.ZERO)) {
			throw new ArithmeticException("Divisor was zero");
		}
		BigInteger gcdA = num.gcd(other.num);
		BigInteger gcdB = den.gcd(other.den);
		return new Fraction(num.divide(gcdA).multiply(other.den.divide(gcdB)),den.divide(gcdB).multiply(other.num.divide(gcdA)),false);
		//return new Fraction(num.multiply(other.den),den.multiply(other.num));
	}

	/**
	 * Computes {@code this ^ exponent}
	 * @param exponent
	 * @return
	 */
	public Fraction pow(int exponent) {
		if (exponent > 0) {
			return new Fraction(num.pow(exponent),den.pow(exponent), false);
		}
		else if (exponent < 0) {
			return new Fraction(den.pow(-exponent),num.pow(-exponent), false);
		}
		else {
			return ONE;
		}
	}

	public Fraction parallelPow(long exponent) {
		if (exponent == 0) {
			return ONE;
		}
		ParPowThread numThread = new ParPowThread(num, Math.abs(exponent));
		numThread.start();
		BigInteger newDen = BogosortProb.parallelPow(den, Math.abs(exponent));
		while (numThread.isAlive()) {
			try {
				numThread.join();
			} catch (InterruptedException e) {}
		}
		if (exponent > 0) {
			return new Fraction(numThread.number, newDen, false);
		} else {
			return new Fraction(newDen, numThread.number, false);
		}
	}

	/**
	 * Computes {@code -this}
	 * @return
	 */
	public Fraction negate() {
		return new Fraction(num.negate(), den, false);
	}

	/**
	 * Computes {@code 1/this}
	 * @return
	 */
	public Fraction reciprocal() {
		return new Fraction(den,num, false);
	}

	/**
	 * Returns a fraction that is the absolute value of this fraction
	 * @return
	 */
	public Fraction abs() {
		if (num.signum() >= 0) {
			return this;
		}
		else {
			return new Fraction(num.abs(), den, false);
		}
	}

	/**
	 * Computes the signum of this {@code Fraction}
	 * @return 1 if this > 0, 0 if this == 0, -1 if this < 0
	 */
	public int signum() {
		return num.signum();
	}

	public String toString() {
		if (den.equals(BigInteger.ONE)) {
			return num.toString();
		} else {
			return num.toString() + "/" + den.toString();
		}
	}

	public void toPrintStream(PrintStream out) {
		out.print(num);
		if (!den.equals(BigInteger.ONE)) {
			out.print("/");
			out.print(den);
		}
	}

	public long stringLength() {
		if (den.equals(BigInteger.ONE)) {
			return integerDigits(num) + (num.signum() == -1 ? 1 : 0);
		}
		else {
			return integerDigits(num) + integerDigits(den) + (num.signum() == -1 ? 2 : 1);
		}
	}

	public long stringLengthEstimate() {
		if (den.equals(BigInteger.ONE)) {
			return integerDigitEstimate(num) + (num.signum() == -1 ? 1 : 0);
		}
		else {
			return integerDigitEstimate(num) + integerDigitEstimate(den) + (num.signum() == -1 ? 2 : 1);
		}
	}

	public boolean equals(Object other) {
		if (other instanceof Fraction) {
			Fraction o = (Fraction) other;
			return num.equals(o.num) && den.equals(o.den);
		}
		return false;
	}

	public int compareTo(Fraction other) {
		BigInteger newNum = num.multiply(other.den).subtract(other.num.multiply(den));
		return newNum.signum();
	}

	/**
	 * Gets the numerator of this fraction
	 * @return The numerator
	 */
	public BigInteger getNumerator() {
		return num;
	}

	/**
	 * Gets the denominator of this fraction
	 * @return The denominator
	 */
	public BigInteger getDenominator() {
		return den;
	}

	/**
	 * Determines whether this fraction is an integer
	 * @return {@code true} if this fraction represents a mathematical integer, {@code false} otherwise
	 */
	public boolean isInteger() {
		return den.equals(BigInteger.ONE);
	}

	/**
	 * Computes the remainder of the division between the numerator and the denominator
	 * @return
	 */
	public BigInteger getRemainder() {
		return num.remainder(den);
	}

	private static class ParPowThread extends Thread {
		public BigInteger number;
		public long exponent;

		public ParPowThread(BigInteger number, long exponent) {
			super();
			this.number = number;
			this.exponent = exponent;
		}

		public void run() {
			number = BogosortProb.parallelPow(number, exponent);
		}
	}
}
