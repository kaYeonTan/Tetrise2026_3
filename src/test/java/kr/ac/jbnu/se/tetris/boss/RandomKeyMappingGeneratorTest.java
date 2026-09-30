package kr.ac.jbnu.se.tetris.boss;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

class RandomKeyMappingGeneratorTest {

    @Test
    void generatesPermutationDifferentFromOriginalAndPrevious() {
        int[] original = {10, 20, 30, 40, 50, 60, 70};
        RandomKeyMappingGenerator generator = new RandomKeyMappingGenerator(new Random(4));

        int[] first = generator.generate(original, null);
        int[] second = generator.generate(original, first);

        assertFalse(Arrays.equals(original, first));
        assertFalse(Arrays.equals(original, second));
        assertFalse(Arrays.equals(first, second));
        int[] sorted = second.clone();
        Arrays.sort(sorted);
        assertArrayEquals(original, sorted);
    }
}
