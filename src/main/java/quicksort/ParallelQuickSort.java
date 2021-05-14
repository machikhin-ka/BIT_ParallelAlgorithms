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
}

