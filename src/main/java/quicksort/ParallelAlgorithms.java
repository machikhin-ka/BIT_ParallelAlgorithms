package quicksort;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;

public class ParallelAlgorithms {
	private final ForkJoinPool pool;
	public static final int DEFAULT_BLOCK = 100_000;

	public ParallelAlgorithms(ForkJoinPool pool) {
		this.pool = pool;
	}

	private static class ParallelFor extends RecursiveAction {
		private final int left;
		private final int right;
		private final IntConsumer func;

		public ParallelFor(int left, int right, IntConsumer func) {
			this.left = left;
			this.right = right;
			this.func = func;
		}

		@Override
		protected void compute() {
			if (left == right - 1) {
				func.accept(left);
				return;
			}
			int m = (left + right) / 2;
			invokeAll(new ParallelFor(left, m, func), new ParallelFor(m, right, func));
		}
	}

	public int[] filter(int[] array, IntUnaryOperator func) {
		int[] flags = map(array, func);
		int[] sums = scan(flags, Integer::sum);
		int[] ans = new int[sums[sums.length - 1] + flags[flags.length - 1]];
		blockedFor(0, array.length, b ->
				{
					for (int i = 0; i < DEFAULT_BLOCK; i++) {
						int index = b * DEFAULT_BLOCK + i;
						if (index > array.length - 1) return;
						if (flags[index] == 1) {
							ans[sums[index]] = array[index];
						}
					}

				}, DEFAULT_BLOCK
		);
		return ans;
	}

	private int[] scan(int[] array, IntBinaryOperator func) {
		if (array.length <= DEFAULT_BLOCK) {
			return SerialAlgorithms.serialScan(array, func);
		}
		int[] sums = getSumsBlocks(array, func);
		sums = scan(sums, func);
		return fillAns(sums, array, func);
	}

	private int[] map(int[] array, IntUnaryOperator func) {
		int[] result = new int[array.length];
		IntConsumer mapFunc = index -> result[index] = func.applyAsInt(array[index]);
		IntConsumer blockedFunc = b -> {
			for (int i = 0; i < DEFAULT_BLOCK; i++) {
				int index = b * DEFAULT_BLOCK + i;
				if (index > array.length - 1) return;
				mapFunc.accept(index);
			}
		};
		blockedFor(0, array.length, blockedFunc, DEFAULT_BLOCK);
		return result;
	}

	private void blockedFor(int left, int right, IntConsumer blockedFunc, int block) {
		parallelFor(left, (right - left) / block + 1, blockedFunc);
	}

	private void parallelFor(int left, int right, IntConsumer func) {
		doAction(new ParallelFor(left, right, func));
	}

	private int[] getSumsBlocks(int[] array, IntBinaryOperator func) {
		int[] sums = new int[array.length / DEFAULT_BLOCK + 1];
		IntConsumer blockedFunc = b -> {
			int from = b * DEFAULT_BLOCK;
			int to = (b + 1) * DEFAULT_BLOCK;
			if (to > array.length) to = array.length;
			sums[b] = Arrays.stream(Arrays.copyOfRange(array, from, to)).reduce(func).orElse(0);
		};
		blockedFor(0, array.length, blockedFunc, DEFAULT_BLOCK);
		return sums;
	}

	private int[] fillAns(int[] sums, int[] array, IntBinaryOperator func) {
		int[] ans = new int[array.length];
		IntConsumer blockedFunc = b -> {
			if (b * DEFAULT_BLOCK > array.length - 1) return;
			ans[b * DEFAULT_BLOCK] = sums[b];
			for (int i = 1; i < DEFAULT_BLOCK; i++) {
				int index = b * DEFAULT_BLOCK + i;
				if (index > array.length - 1) return;
				ans[index] = func.applyAsInt(ans[index - 1], array[index - 1]);
			}
		};
		blockedFor(0, array.length, blockedFunc, DEFAULT_BLOCK);
		return ans;
	}

	private void doAction(RecursiveAction action) {
		ForkJoinTask<Void> result = pool.submit(action);
		try {
			result.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}
