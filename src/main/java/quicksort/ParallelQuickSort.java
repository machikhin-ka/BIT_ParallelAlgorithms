package quicksort;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import static java.lang.System.currentTimeMillis;

public class ParallelQuickSort {
	private final ForkJoinPool pool;
	private final ParallelAlgorithms algorithms;

	public ParallelQuickSort(ForkJoinPool pool) {
		this.pool = pool;
		this.algorithms = new ParallelAlgorithms(pool);
	}

	private class QuickSortTask extends RecursiveTask<int[]> {
		private final int[] array;

		public QuickSortTask(int[] array) {
			this.array = array;
		}

		@Override
		protected int[] compute() {
			int p = getPivot(array);
			if (array.length < ParallelAlgorithms.DEFAULT_BLOCK) {
				SerialAlgorithms.serialSort(array);
				return array;
			}
			int[] lessPivot = algorithms.filter(array, e -> e < p ? 1 : 0);
			int[] morePivot = algorithms.filter(array, e -> e > p ? 1 : 0);
			if (lessPivot.length > 1 && morePivot.length > 1) {
				QuickSortTask left = new QuickSortTask(lessPivot);
				QuickSortTask right = new QuickSortTask(morePivot);
				left.fork();
				right.compute();
				left.join();
			} else if (lessPivot.length > 1) {
				new QuickSortTask(lessPivot).compute();
			} else if (morePivot.length > 1) {
				new QuickSortTask(morePivot).compute();
			}
			return collectArray(array, p, lessPivot, morePivot);
		}

		private int getPivot(int[] arr) {
			return arr[new Random().nextInt(arr.length)];
		}

		private int[] collectArray(int[] array, int p, int[] lessPivot, int[] morePivot) {
			int i = 0;
			for (; i < lessPivot.length; i++) {
				array[i] = lessPivot[i];
			}
			for (; i < array.length - morePivot.length; i++) {
				array[i] = p;
			}
			for (int j = 0; j < morePivot.length; j++, i++) {
				array[i] = morePivot[j];
			}
			return array;
		}
	}

	public int[] sort(int[] array) {
		return pool.invoke(new QuickSortTask(array));
	}

	public static void main(String[] args) {
		int[] arr = generateArray();/*{54, 64, 95, 82, 12, 32, 63, 10, 90, 33, 54, 3432, 4334534};*/
		ParallelQuickSort parallelQuickSort = new ParallelQuickSort(new ForkJoinPool(4));

		int[] correctSort = arr.clone();
		Arrays.sort(correctSort);

		long start = currentTimeMillis();
		int[] sort = SerialAlgorithms.serialSort(arr/*, 0, arr.length - 1*/);
		System.out.println("serial: " + (currentTimeMillis() - start));
		System.out.println(Arrays.equals(correctSort, sort));
//		System.out.println(Arrays.toString(sort));

		start = currentTimeMillis();
		sort = parallelQuickSort.sort(arr/*, 0, arr.length - 1*/);
		System.out.println("parallel: " + (currentTimeMillis() - start));
		System.out.println(Arrays.equals(correctSort, sort));
//		System.out.println(Arrays.toString(sort));
	}

	private static int[] generateArray() {
		int[] array = new int[1_000_000_0];
		for (int i = 0; i < 1_000_000_0; i++) {
			array[i] = new Random().nextInt(1_000_000_0);
		}
		return array;
	}
}

