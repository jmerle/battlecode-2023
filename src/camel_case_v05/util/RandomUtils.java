package camel_case_v05.util;

public class RandomUtils {
    public static int nextInt(int maxExclusive) {
        return (int) Math.floor(Math.random() * maxExclusive);
    }

    public static int nextInt(int minInclusive, int maxExclusive) {
        return nextInt(maxExclusive - minInclusive) + minInclusive;
    }

    public static boolean chance(double percentage) {
        return nextInt(1000) < percentage * 1000;
    }

    public static <T> T[] shuffle(T[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = nextInt(i + 1);

            T temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }

        return array;
    }
}
