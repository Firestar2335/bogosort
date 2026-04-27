import java.util.random.RandomGenerator;
import java.util.*;

public abstract class Sorter {

	public static void main(String[] args) {
		long shuffles = 66712660032l;
		long comps = 1667816500800l;
		System.out.println("Float rounding");
		System.out.println((long) ((float) shuffles));
		System.out.println((long) ((float) comps));
		System.out.println("Double rounding");
		System.out.println((long) ((double) shuffles));
		System.out.println((long) ((double) comps));
		System.out.println(comps/shuffles);
		//System.exit(0);
		int elems = 3;
		long trials = 100_000_000l;

		Map<Long,Long> counts = new TreeMap<>();
		long f = ExtraMath.factorialInt(elems);
		for (long i = 0; i < f; i++) {
			counts.put(i, 0l);
		}
		
		int[] sorted = new int[elems];
		for (int i = 1; i <= elems; i++) {
			sorted[i-1]=i;
		}

		int[] perm;
		Random rand = new Random();
		for (int i = 0; i < trials; i++) {
			perm = Arrays.copyOf(sorted, elems);
			shuffleInPlace(perm, rand);
			long p = numberPermutation(perm);
			counts.put(p, counts.get(p)+1);
		}

		for (Long key : counts.keySet()) {
			System.out.print(key);
			System.out.print(": ");
			System.out.println(counts.get(key));
		}
	}

	private static long numberPermutation(int[] perm) {
		int[] code = new int[perm.length];
		SortedSet<Integer> found = new TreeSet<>();
		for (int i = 0; i < perm.length; i++) {
			code[i] = perm[i] - found.headSet(perm[i]).size() - 1;
			found.add(perm[i]);
		}
		long result = 0;
		long placeValue = 1;
		for (int i = 0; i < code.length; i++) {
			result += code[code.length-i-1] * placeValue;
			placeValue *= i+1;
		}
		return result;
	}

	/**
	 * Sorts {@code arr} in place
	 * @param arr
	 */
	public abstract void sortInPlace(byte[] arr);

	/**
	 * Sorts {@code arr} in place
	 * @param arr
	 */
	public abstract void sortInPlace(char[] arr);

	/**
	 * Sorts {@code arr} in place
	 * @param arr
	 */
	public abstract void sortInPlace(short[] arr);

	/**
	 * Sorts {@code arr} in place
	 * @param arr
	 */
	public abstract void sortInPlace(int[] arr);

	/**
	 * Sorts {@code arr} in place
	 * @param arr
	 */
	public abstract void sortInPlace(long[] arr);

	/**
	 * Sorts {@code arr} in place
	 * @param arr
	 */
	public abstract void sortInPlace(float[] arr);

	/**
	 * Sorts {@code arr} in place
	 * @param arr
	 */
	public abstract void sortInPlace(double[] arr);

	/**
	 * Sorts {@code arr} in place
	 * @param arr
	 */
	public abstract <T extends Comparable<T>> void sortInPlace(T[] arr);

	/**
	 * Sorts {@code arr} in place using the provided comparator
	 * @param arr
	 * @param comp The comparator to use
	 */
	public abstract <T> void sortInPlace(T[] arr, Comparator<? super T> comp);

	//#region in-place shuffles

	/**
	 * Shuffles the array in-place, modifying {@code arr}
	 * @implNote This uses a Fisher-Yates shuffle
	 * @param arr The array to shuffle.
	 * @param rand The random number generator to get the randomness from
	 * 
	 */
	public static void shuffleInPlace(byte[] arr, RandomGenerator rand) {
		int j;
		byte tmp;
		for (int i = arr.length-1; i >= 1; i--) {
			j = rand.nextInt(i+1);
			tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
		//return arr;
	}

	/**
	 * Shuffles the array in-place, modifying {@code arr}
	 * @implNote This uses a Fisher-Yates shuffle
	 * @param arr The array to shuffle.
	 * @param rand The random number generator to get the randomness from
	 * 
	 */
	public static void shuffleInPlace(char[] arr, RandomGenerator rand) {
		int j;
		char tmp;
		for (int i = arr.length-1; i >= 1; i--) {
			j = rand.nextInt(i+1);
			tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
		//return arr;
	}

	/**
	 * Shuffles the array in-place, modifying {@code arr}
	 * @implNote This uses a Fisher-Yates shuffle
	 * @param arr The array to shuffle.
	 * @param rand The random number generator to get the randomness from
	 * 
	 */
	public static void shuffleInPlace(short[] arr, RandomGenerator rand) {
		int j;
		short tmp;
		for (int i = arr.length-1; i >= 1; i--) {
			j = rand.nextInt(i+1);
			tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
		//return arr;
	}

	/**
	 * Shuffles the array in-place, modifying {@code arr}
	 * @implNote This uses a Fisher-Yates shuffle
	 * @param arr The array to shuffle.
	 * @param rand The random number generator to get the randomness from
	 * 
	 */
	public static void shuffleInPlace(int[] arr, RandomGenerator rand) {
		int j;
		int tmp;
		for (int i = arr.length-1; i >= 1; i--) {
			j = rand.nextInt(i+1);
			tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
		//return arr;
	}

	/**
	 * Shuffles the array in-place, modifying {@code arr}
	 * @implNote This uses a Fisher-Yates shuffle
	 * @param arr The array to shuffle.
	 * @param rand The random number generator to get the randomness from
	 * 
	 */
	public static void shuffleInPlace(long[] arr, RandomGenerator rand) {
		int j;
		long tmp;
		for (int i = arr.length-1; i >= 1; i--) {
			j = rand.nextInt(i+1);
			tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
		//return arr;
	}

	/**
	 * Shuffles the array in-place, modifying {@code arr}
	 * @implNote This uses a Fisher-Yates shuffle
	 * @param arr The array to shuffle.
	 * @param rand The random number generator to get the randomness from
	 * 
	 */
	public static void shuffleInPlace(float[] arr, RandomGenerator rand) {
		int j;
		float tmp;
		for (int i = arr.length-1; i >= 1; i--) {
			j = rand.nextInt(i+1);
			tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
		//return arr;
	}

	/**
	 * Shuffles the array in-place, modifying {@code arr}
	 * @implNote This uses a Fisher-Yates shuffle
	 * @param arr The array to shuffle.
	 * @param rand The random number generator to get the randomness from
	 * 
	 */
	public static void shuffleInPlace(double[] arr, RandomGenerator rand) {
		int j;
		double tmp;
		for (int i = arr.length-1; i >= 1; i--) {
			j = rand.nextInt(i+1);
			tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
		//return arr;
	}

	/**
	 * Shuffles the array in-place, modifying {@code arr}
	 * @implNote This uses a Fisher-Yates shuffle
	 * @param arr The array to shuffle.
	 * @param rand The random number generator to get the randomness from
	 * 
	 */
	public static void shuffleInPlace(boolean[] arr, RandomGenerator rand) {
		int j;
		boolean tmp;
		for (int i = arr.length-1; i >= 1; i--) {
			j = rand.nextInt(i+1);
			tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
		//return arr;
	}

	/**
	 * Shuffles the array in-place, modifying {@code arr}
	 * @implNote This uses a Fisher-Yates shuffle
	 * @param arr The array to shuffle.
	 * @param rand The random number generator to get the randomness from
	 * 
	 */
	public static <T> void shuffleInPlace(T[] arr, RandomGenerator rand) {
		int j;
		T tmp;
		for (int i = arr.length-1; i >= 1; i--) {
			j = rand.nextInt(i+1);
			tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
		//return arr;
	}

	//#endregion in-place shuffles
	
	//#region normal sorted

	/**
	 * Determines whether the list is sorted in ascending order
	 * @param list The list to check
	 * @return {@code true} if all elements are less than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isSorted(byte[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] > list[i+1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether the list is sorted in ascending order
	 * @param list The list to check
	 * @return {@code true} if all elements are less than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isSorted(char[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] > list[i+1]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Determines whether the list is sorted in ascending order
	 * @param list The list to check
	 * @return {@code true} if all elements are less than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isSorted(short[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] > list[i+1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether the list is sorted in ascending order
	 * @param list The list to check
	 * @return {@code true} if all elements are less than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isSorted(int[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] > list[i+1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether the list is sorted in ascending order
	 * @param list The list to check
	 * @return {@code true} if all elements are less than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isSorted(long[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] > list[i+1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether the list is sorted in ascending order
	 * @param list The list to check
	 * @return {@code true} if all elements are less than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isSorted(float[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] > list[i+1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether the list is sorted in ascending order
	 * @param list The list to check
	 * @return {@code true} if all elements are less than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isSorted(double[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] > list[i+1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether the list is sorted in ascending order using the natural ordering of its 
	 * elements
	 * @param <T> The elements of the list
	 * @param list The list to check
	 * @return {@code false} if any element is strictly larger than the next element, {@code true} otherwise
	 */
	public static <T extends Comparable<T>> boolean isSorted(T[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i].compareTo(list[i+1]) > 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines if the list is sorted in ascending order according to the provided comparator
	 * @param <T> The type of the elements
	 * @param list The list to check
	 * @param comparator The comparator to use
	 * @return {@code true} if all elements are less than or equal to the following elements 
	 * according to the comparator, {@code false} otherwise.
	 */
	public static <T> boolean isSorted(T[] list, Comparator<? super T> comparator) {
		for (int i = 0; i < list.length-1; i++) {
			if (comparator.compare(list[i],list[i+1]) > 0) {
				return false;
			}
		}
		return true;
	}

	//#endregion normal sorted

	//#region reverse sorted

	/**
	 * Determines whether the list is sorted in descending order
	 * @param list The list to check
	 * @return {@code true} if all elements are greater than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isReverseSorted(byte[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] < list[i+1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether the list is sorted in descending order
	 * @param list The list to check
	 * @return {@code true} if all elements are greater than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isReverseSorted(char[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] < list[i+1]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Determines whether the list is sorted in descending order
	 * @param list The list to check
	 * @return {@code true} if all elements are greater than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isReverseSorted(short[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] < list[i+1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether the list is sorted in descending order
	 * @param list The list to check
	 * @return {@code true} if all elements are greater than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isReverseSorted(int[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] < list[i+1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether the list is sorted in descending order
	 * @param list The list to check
	 * @return {@code true} if all elements are greater than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isReverseSorted(long[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] < list[i+1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether the list is sorted in descending order
	 * @param list The list to check
	 * @return {@code true} if all elements are greater than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isReverseSorted(float[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] < list[i+1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether the list is sorted in descending order
	 * @param list The list to check
	 * @return {@code true} if all elements are greater than or equal to all following elements, 
	 * {@code false} otherwise
	 */
	public static boolean isReverseSorted(double[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i] < list[i+1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether the list is sorted in descending order using the natural ordering of its 
	 * elements
	 * @param <T> The elements of the list
	 * @param list The list to check
	 * @return {@code false} if any element is strictly less than the next element, {@code true} otherwise
	 */
	public static <T extends Comparable<T>> boolean isReverseSorted(T[] list) {
		for (int i = 0; i < list.length-1; i++) {
			if (list[i].compareTo(list[i+1]) < 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines if the list is sorted in descending order according to the provided comparator
	 * @param <T> The type of the elements
	 * @param list The list to check
	 * @param comparator The comparator to use
	 * @return {@code true} if all elements are greater than or equal to the following elements 
	 * according to the comparator, {@code false} otherwise.
	 */
	public static <T> boolean isReverseSorted(T[] list, Comparator<? super T> comparator) {
		for (int i = 0; i < list.length-1; i++) {
			if (comparator.compare(list[i],list[i+1]) < 0) {
				return false;
			}
		}
		return true;
	}

	//#endregion reverse sorted
}
