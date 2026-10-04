package kr.ac.jbnu.se.tetris.boss;

import java.util.Arrays;
import java.util.Random;

public final class RandomKeyMappingGenerator {

    private final Random random;

    public RandomKeyMappingGenerator(Random random) {
        this.random = random;
    }

    public int[] generate(int[] original, int[] previous) {
        int[] shuffled = original.clone();
        for (int attempt = 0; attempt < 12; attempt++) {
            fisherYates(shuffled);
            if (!Arrays.equals(shuffled, original) && !Arrays.equals(shuffled, previous)) {
                return shuffled;
            }
        }
        int first = shuffled[0];
        System.arraycopy(shuffled, 1, shuffled, 0, shuffled.length - 1);
        shuffled[shuffled.length - 1] = first;
        if (Arrays.equals(shuffled, original) || Arrays.equals(shuffled, previous)) {
            int second = shuffled[0];
            System.arraycopy(shuffled, 1, shuffled, 0, shuffled.length - 1);
            shuffled[shuffled.length - 1] = second;
        }
        return shuffled;
    }

    private void fisherYates(int[] values) {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = values[i];
            values[i] = values[j];
            values[j] = temp;
        }
    }
}
