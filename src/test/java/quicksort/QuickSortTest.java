package quicksort;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickSortTest {
	private static ParallelQuickSort quickSort;
	private static int[] array;
	private static int[] correctSort;

	@BeforeAll
	static void warmUp() {
		array = generateArray(1_000_000_0);

		correctSort = array.clone();
		Arrays.sort(correctSort);

		System.out.println("Warm up is started");

		quickSort = new ParallelQuickSort(new ForkJoinPool(4));
		quickSort.sort(array);
		SerialAlgorithms.serialSort(array);

		System.out.println("Warm up is ended");
	}

	@Test
	void testQuickSortWithOneThreads() {
		quickSort = new ParallelQuickSort(new ForkJoinPool(1));

		long start = System.currentTimeMillis();
		int[] sort = quickSort.sort(array);
		System.out.println("1 threads: " + (System.currentTimeMillis() - start));

		assertTrue(Arrays.equals(correctSort, sort));
	}

	@Test
	void testQuickSortWithTwoThreads() {
		quickSort = new ParallelQuickSort(new ForkJoinPool(2));

		long start = System.currentTimeMillis();
		int[] sort = quickSort.sort(array);
		System.out.println("2 threads: " + (System.currentTimeMillis() - start));

		assertTrue(Arrays.equals(correctSort, sort));
	}

	@Test
	void testQuickSortWithThreeThreads() {
		quickSort = new ParallelQuickSort(new ForkJoinPool(3));

		long start = System.currentTimeMillis();
		int[] sort = quickSort.sort(array);
		System.out.println("3 threads: " + (System.currentTimeMillis() - start));

		assertTrue(Arrays.equals(correctSort, sort));
	}

	@Test
	void testQuickSortWithFourThreads() {
		quickSort = new ParallelQuickSort(new ForkJoinPool(4));

		long start = System.currentTimeMillis();
		int[] sort = quickSort.sort(array);
		System.out.println("4 threads: " + (System.currentTimeMillis() - start));

		assertTrue(Arrays.equals(correctSort, sort));
	}

	@Test
	void testSerialFilter() {
		long start = System.currentTimeMillis();
		int[] sort = SerialAlgorithms.serialSort(array);
		System.out.println("serial: " + (System.currentTimeMillis() - start));

		assertTrue(Arrays.equals(correctSort, sort));
	}

	private static int[] generateArray(int size) {
		int[] array = new int[size];
		Random generator = new Random(10);
		for (int i = 0; i < size; i++) {
			array[i] = generator.nextInt(size);
		}
		return array;
	}

}