package quicksort;

import java.util.Arrays;
import java.util.Random;
import java.util.function.IntBinaryOperator;
import java.util.function.IntPredicate;

public class SerialAlgorithms {
	public static int[] serialSort(int[] array) {
		int p = array[new Random().nextInt(array.length)];
		int[] lessPivot = serialFilter(array, e -> e < p);
		int[] morePivot = serialFilter(array, e -> e > p);
		if (lessPivot.length > 1) lessPivot = serialSort(lessPivot);
		if (morePivot.length > 1) morePivot = serialSort(morePivot);

		return collectArray(array, p, lessPivot, morePivot);
	}

	public static int[] serialFilter(int[] array, IntPredicate func) {
		return Arrays.stream(array).filter(func).toArray();
	}

	private static int[] collectArray(int[] array, int p, int[] lessPivot, int[] morePivot) {
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

	public static int[] serialScan(int[] array, IntBinaryOperator func) {
		int[] ans = new int[array.length];
		ans[0] = 0;
		for (int i = 1; i < array.length; i++) {
			ans[i] = func.applyAsInt(ans[i - 1], array[i - 1]);
		}
		return ans;
	}
}
