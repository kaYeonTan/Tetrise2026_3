package kr.ac.jbnu.se.tetris.boss;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void actionPermutationCanBeAppliedToDifferentLocalKeyConfigurations() {
        RandomKeyMappingGenerator generator = new RandomKeyMappingGenerator(new Random(7));
        int[] permutation = generator.generatePermutation(7, null);
        int[] hostKeys = {10, 20, 30, 40, 50, 60, 70};
        int[] guestKeys = {110, 120, 130, 140, 150, 160, 170};

        int[] hostMapped = RandomKeyMappingGenerator.applyPermutation(hostKeys, permutation);
        int[] guestMapped = RandomKeyMappingGenerator.applyPermutation(guestKeys, permutation);

        for (int action = 0; action < permutation.length; action++) {
            assertEquals(hostKeys[permutation[action]], hostMapped[action]);
            assertEquals(guestKeys[permutation[action]], guestMapped[action]);
        }
    }
}
