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
            if (isAllowed(shuffled, original, previous)) {
                return shuffled;
            }
        }

        rotateLeftOnce(shuffled);
        if (!isAllowed(shuffled, original, previous)) {
            rotateLeftOnce(shuffled);
        }
        return shuffled;
    }

    private boolean isAllowed(int[] candidate, int[] original, int[] previous) {
        return !Arrays.equals(candidate, original)
                && !Arrays.equals(candidate, previous);
    }

    private void rotateLeftOnce(int[] values) {
        int first = values[0];
        System.arraycopy(values, 1, values, 0, values.length - 1);
        values[values.length - 1] = first;
    }

    /**
     * Creates a shuffled action-to-key-slot mapping. The returned values are
     * indexes, not platform key codes, so the mapping can be shared with a
     * remote player and applied to that player's own key configuration.
     */
    public int[] generatePermutation(int size, int[] previous) {
        if (size < 2) {
            throw new IllegalArgumentException("At least two actions are required");
        }
        int[] identity = new int[size];
        for (int i = 0; i < size; i++) {
            identity[i] = i;
        }
        return generate(identity, previous);
    }

    /** Applies an action-to-key-slot mapping to a local key configuration. */
    public static int[] applyPermutation(int[] originalKeys, int[] permutation) {
        if (originalKeys == null || permutation == null || originalKeys.length != permutation.length) {
            throw new IllegalArgumentException("Key configuration and permutation must have the same length");
        }
        int[] mapped = new int[originalKeys.length];
        boolean[] used = new boolean[originalKeys.length];
        for (int action = 0; action < permutation.length; action++) {
            int keySlot = permutation[action];
            if (keySlot < 0 || keySlot >= originalKeys.length || used[keySlot]) {
                throw new IllegalArgumentException("Invalid key permutation");
            }
            used[keySlot] = true;
            mapped[action] = originalKeys[keySlot];
        }
        return mapped;
    }

    /** Converts a legacy shuffled key-code array into an index permutation. */
    public static int[] toPermutation(int[] originalKeys, int[] shuffledKeys) {
        if (originalKeys == null || shuffledKeys == null || originalKeys.length != shuffledKeys.length) {
            return null;
        }
        int[] permutation = new int[originalKeys.length];
        boolean[] used = new boolean[originalKeys.length];
        for (int action = 0; action < shuffledKeys.length; action++) {
            int keySlot = -1;
            for (int i = 0; i < originalKeys.length; i++) {
                if (!used[i] && originalKeys[i] == shuffledKeys[action]) {
                    keySlot = i;
                    break;
                }
            }
            if (keySlot < 0) {
                return null;
            }
            used[keySlot] = true;
            permutation[action] = keySlot;
        }
        return permutation;
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
