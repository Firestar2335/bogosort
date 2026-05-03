import java.math.BigInteger;
import java.util.*;

public class Client {
	
	public static void main(String[] args) {
		Configuration config = getOptions(args);
		if (config == null) {
			return;
		}
		if (config.parallel) {
			Deque<ChanceThread> chanceThreads = new LinkedList<>();
			Deque<ChanceMaxThread> chanceMaxThreads = new LinkedList<>();
			ExpectedValueThread evThread = null;

			if (config.k != null) {
				for (int k : config.k) {
					ChanceThread t = new ChanceThread(config.N, k, true);
					t.start();
					chanceThreads.addLast(t);
				}
			}
			if (config.maxK != null) {
				for (int k : config.maxK) {
					ChanceMaxThread t = new ChanceMaxThread(config.N, k, config.n, true);
					t.start();
					chanceMaxThreads.addLast(t);
				}
			}
			if (config.ev) {
				evThread = new ExpectedValueThread(config.N, config.n, true);
				evThread.start();
			}
			while (chanceThreads.size() > 0) {
				ChanceThread t = chanceThreads.removeFirst();
				do {
					try {
						t.join();
					} catch (InterruptedException e) { }
				} while (t.isAlive());
				printChance(t.getK(), t.getString());
			}

			if (config.k != null && (config.ev || config.maxK != null)) {
				System.out.println();
			} 

			if (config.maxK != null || config.ev) {
				System.out.print("Shuffles per second: ");
				System.out.println(config.n);
				System.out.println();
			}

			while (chanceMaxThreads.size() > 0) {
				ChanceMaxThread t = chanceMaxThreads.removeFirst();
				do {
					try {
						t.join();
					} catch (InterruptedException e) { }
				} while (t.isAlive());
				printChanceMax(t.getK(), t.getString());
			}

			if (config.maxK != null && config.ev) {
				System.out.println();
			}

			if (config.ev) {
				do {
					try {
						evThread.join();
					} catch (InterruptedException e) { }
				} while (evThread.isAlive());
				printExpectedValue(evThread.getString());
			}
		}
		else {
			Fraction f;
			if (config.k != null) {
				for (int k : config.k) {
					f = BogosortProb.chance(config.N, k);
					printChance(k, f.format());
				}
			}

			if (config.k != null && (config.maxK != null || config.ev)) {
				System.out.println();
			}
			if (config.maxK != null || config.ev) {
				System.out.print("Shuffles per second: ");
				System.out.println(config.n);
				System.out.println();
			}

			if (config.maxK != null) {
				for (int k : config.maxK) {
					f = BogosortProb.chanceMax(config.N, k, config.n);
					printChanceMax(k, f.format());
				}
			}

			if (config.maxK != null && config.ev) {
				System.out.println();
			}
			if (config.ev) {
				f = BogosortProb.expectedValue(config.N, config.n);
				printExpectedValue(f.format());
			}
		}
	}

	private static void printChance(int k, String str) {
		System.out.print("The chance of exactly ");
		System.out.print(k);
		System.out.print(" being properly sorted is ");
		System.out.println(str);
	}

	private static void printChanceMax(int k, String str) {
		System.out.print("The chance of a best run of exactly ");
		System.out.print(k);
		System.out.print(" is ");
		System.out.println(str);
	}

	private static void printExpectedValue(String str) {
		System.out.print("The expected number of properly sorted elements is ");
		System.out.println(str);
	}

	private static Configuration getOptions(String[] args) {
		if (args.length == 0) {
			System.err.println("Not enough arguments were specified");
			System.exit(1);
			return null;
		}
		if (args[0].equals("-h")) {
			help();
			System.exit(0);
			return null;
		}
		int elements;
		try {
			elements = Integer.parseInt(args[0]);
			if (elements <= 0) {
				System.err.println("elements was not positive");
				System.exit(2);
				return null;
			}
		} catch (NumberFormatException e) {
			System.err.println("elements was not a number");
			System.exit(2);
			return null;
		}
		long n = -1;
		boolean expectedValue = false;
		boolean parallel = false;
		List<Integer> ks = null;
		List<Integer> maxKs = null;
		ListIterator<String> iter = new StringArrayIterator(args);
		iter.next();
		int v;
		while (iter.hasNext()) {
			switch (iter.next()) {
				case "-E": expectedValue = true; break;
				case "-p": parallel = true; break;
				case "-n": 
					if (!iter.hasNext()) {
						System.err.println("There was not a value provided for '-n'");
						System.exit(1);
						return null;
					}
					String next = iter.next();
					try {
						n = Long.parseLong(next);
						if (n <= 0) {
							System.err.println("The value provided for '-n' was not positive");
							System.exit(2);
							return null;
						}
					} catch (NumberFormatException e) {
						System.err.println("The value provided for '-n' was not a number.");
						System.exit(2);
						return null;
					}
					break;
				case "-k":
					if (!iter.hasNext()) {
						System.err.println("There was not a value provided for '-k'");
						System.exit(1);
						return null;
					}
					try {
						v = Integer.parseInt(iter.next());
						iter.previous();
					} catch (NumberFormatException e) {
						System.err.println("There was not a value provided for '-k'");
						System.exit(1);
						return null;
					}
					if (ks == null) {
						ks = new ArrayList<>();
					}
					while (iter.hasNext()) {
						try {
							v = Integer.parseInt(iter.next());
							if (v < 0 || v > elements) {
								System.err.println("One of the values provided for '-k' was not in the proper range");
								System.exit(2);
								return null;
							}
							if (!ks.contains(v)) {
								ks.add(v);
							}
						} catch (NumberFormatException e) {
							iter.previous();
							break;
						}
					}
					break;
				case "--all":
					if (ks == null) {
						ks = new ArrayList<>();
					} else {
						ks.clear();
					}
					for (int i = 0; i <= elements; i++) {
						ks.add(i);
					}
					break;
				case "-M":
					if (!iter.hasNext()) {
						System.err.println("There was not a value provided for '-M'");
						System.exit(1);
						return null;
					}
					try {
						v = Integer.parseInt(iter.next());
						iter.previous();
					} catch (NumberFormatException e) {
						System.err.println("There was not a value provided for '-M'");
						System.exit(1);
						return null;
					}
					if (maxKs == null) {
						maxKs = new ArrayList<>();
					}
					while (iter.hasNext()) {
						try {
							v = Integer.parseInt(iter.next());
							if (v < 0 || v > elements) {
								System.err.println("One of the values provided for '-k' was not in the proper range");
								System.exit(2);
								return null;
							}
							if (!maxKs.contains(v)) {
								maxKs.add(v);
							}
						} catch (NumberFormatException e) {
							iter.previous();
							break;
						}
					}
					break;
				case "--all-max":
					if (maxKs == null) {
						maxKs = new ArrayList<>();
					} else {
						maxKs.clear();
					}
					for (int i = 0; i <= elements; i++) {
						maxKs.add(i);
					}
					break;
				default:
					System.err.print("Unrecognized option: ");
					System.err.println(iter.previous());
					System.exit(3);
					return null;
			}
		}
		if (n == -1 && (maxKs != null || expectedValue)) {
			System.err.println("The number of shuffles was not specified, but it needed to be");
			System.exit(1);
			return null;
		}
		if (!expectedValue && ks == null && maxKs == null) {
			System.err.println("No computations were requested");
			System.exit(0);
			return null;
		}
		return new Configuration(elements, ks, n, maxKs, expectedValue, parallel);
	}

	/**
	 * Prints out help information
	 */
	private static void help() {
		System.out.println("Usage:\n"
						 + "java -jar bogosort.jar -h\n"
						 + "java -jar bogosort.jar [elements] [options...]\n\n"
						 + "\t elements : The total number of elements that are being shuffled\n"
						 + "\t -k number [numbers...] : Sets the numbers to calculate the probability of occuring in a single shuffle. Must be between 0 and elements\n"
						 + "\t --all : Calculate the chances for all possible values of k\n"
						 + "\t -n number : Sets the number of shuffles that are considered for the maximum chances. Must be a positive integer greater than 0.\n"
						 + "\t -M number [numbers...] : Sets the numbers to calculate for the maximum of a sample of shuffles. Must be integers between 0 and elements\n"
						 + "\t --all-max : Calculates the chance for all possible maximum values\n"
						 + "\t -E : Calculates the expected value of the maximum value of the sample of shuffles\n"
						 + "\n\t -p : indicates that the chances should be calculated in parallel");
	}

	private static class Configuration {
		/** The total number of elements */
		public final int N;
		/**The k's to calculate the chances for in a single shuffle */
		public final List<Integer> k;
		/** The number of shuffles to consider */
		public final long n;
		/** The k's to calculate the max chance for */
		public final List<Integer> maxK;
		/** Whether to calculate the expected value */
		public final boolean ev;
		/** Whether the tasks should be parallel */
		public final boolean parallel;

		public Configuration(int N, List<Integer> k, long n, List<Integer> maxK, boolean expected, boolean parallel) {
			this.N = N;
			this.k = k;
			this.n = n;
			this.maxK = maxK;
			this.ev = expected;
			this.parallel = parallel;
		}
	}

	public static void fractionTest() {
		/*System.out.println(Long.toHexString(Double.doubleToRawLongBits(0.5)));
		testFrac(new Fraction(1,2));
		testFrac(new Fraction(1,4));
		testFrac(new Fraction(1,1));
		testFrac(new Fraction(0,1));*/
		testFrac(new Fraction(BigInteger.ONE, BigInteger.ONE.shiftLeft(1074)));
		System.out.println(Double.MIN_VALUE);
		testFrac(new Fraction(BigInteger.ONE.shiftLeft(52).subtract(BigInteger.ONE),BigInteger.ONE.shiftLeft(52+1022)));
		testFrac(new Fraction(BigInteger.ONE,BigInteger.ONE.shiftLeft(1022)));
		System.out.println(Double.MIN_NORMAL);
		testFrac(new Fraction(BigInteger.ONE.shiftLeft(53).subtract(BigInteger.ONE).shiftLeft(971),BigInteger.ONE));
		System.out.println(Double.MAX_VALUE);
		
		testFrac(new Fraction(BigInteger.ONE.negate(), BigInteger.ONE.shiftLeft(1074)));
		System.out.println(-Double.MIN_VALUE);
		testFrac(new Fraction(BigInteger.ONE.shiftLeft(52).subtract(BigInteger.ONE).negate(),BigInteger.ONE.shiftLeft(52+1022)));
		testFrac(new Fraction(BigInteger.ONE.negate(),BigInteger.ONE.shiftLeft(1022)));
		System.out.println(-Double.MIN_NORMAL);
		testFrac(new Fraction(BigInteger.ONE.shiftLeft(53).subtract(BigInteger.ONE).shiftLeft(971).negate(),BigInteger.ONE));
		System.out.println(-Double.MAX_VALUE);


		testFrac(new Fraction(BigInteger.ONE,BigInteger.ONE.shiftLeft(149)));
		System.out.println(Float.MIN_VALUE);
		testFrac(new Fraction(BigInteger.ONE.shiftLeft(23).subtract(BigInteger.ONE),BigInteger.ONE.shiftLeft(149)));
		testFrac(new Fraction(BigInteger.ONE,BigInteger.ONE.shiftLeft(126)));
		System.out.println(Float.MIN_NORMAL);
		testFrac(new Fraction(BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE.shiftLeft(104)),BigInteger.ONE));
		System.out.println(Float.MAX_VALUE);

		testFrac(new Fraction(BigInteger.ONE.negate(),BigInteger.ONE.shiftLeft(149)));
		System.out.println(-Float.MIN_VALUE);
		testFrac(new Fraction(BigInteger.ONE.shiftLeft(23).subtract(BigInteger.ONE).negate(),BigInteger.ONE.shiftLeft(149)));
		testFrac(new Fraction(BigInteger.ONE.negate(),BigInteger.ONE.shiftLeft(126)));
		System.out.println(-Float.MIN_NORMAL);
		testFrac(new Fraction(BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE.shiftLeft(104)).negate(),BigInteger.ONE));
		System.out.println(-Float.MAX_VALUE);

		testFrac(new Fraction(99,8));
		testFrac(new Fraction(1,3));
	}

	private static void testFrac(Fraction frac) {
		float f = frac.floatValue();
		double d = frac.doubleValue();
		System.out.print(frac.toString());
		System.out.print(" = ");
		System.out.print(f);
		System.out.print(" = ");
		System.out.println(d);
	}

	private static class StringArrayIterator implements ListIterator<String> {
		private final String[] arr;
		/** The index of the element that will be returned by next() */
		private int index;

		private int lastIndex;

		public StringArrayIterator(String[] arr) {
			this.arr = arr;
			index = 0;
			lastIndex = -1;
		}

		public boolean hasNext() {
			return index < arr.length;
		}

		public String next() {
			if (hasNext()) {
				lastIndex = index;
				return arr[index++];
			}
			throw new NoSuchElementException("There was not a next element");
		}

		public boolean hasPrevious() {
			return index > 0;
		}

		public String previous() {
			if (hasPrevious()) {
				lastIndex = index - 1;
				return arr[--index];
			}
			throw new NoSuchElementException("There was not a previous element");
		}

		public int nextIndex() {
			return index;
		}
		
		public int previousIndex() {
			return index-1;
		}

		public void remove() {
			throw new NoSuchElementException("Structural modification is not supported");
		}

		public void add(String e) {
			throw new NoSuchElementException("Structural modification is not supported");
		}

		public void set(String e) {
			if (lastIndex == -1) {
				throw new IllegalStateException("No element has been retrieved");
			}
			arr[lastIndex] = e;
		}
	}
}
