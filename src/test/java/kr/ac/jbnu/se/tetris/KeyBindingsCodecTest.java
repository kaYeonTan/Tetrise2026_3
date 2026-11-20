package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class KeyBindingsCodecTest {

    @Test
    void roundTripKeepsEveryKey() {
        KeySettings original = KeySettings.defaults();
        original.set(KeySettings.P1, 3, 90);
        KeySettings decoded = KeyBindingsCodec.decode(KeyBindingsCodec.encode(original));
        assertNotNull(decoded);
        assertTrue(original.equals(decoded));
    }

    @Test
    void encodeKeepsLegacyFourLineFormat() {
        List<String> lines = KeyBindingsCodec.encode(KeySettings.defaults());
        assertEquals(4, lines.size());
        assertEquals("37,39,40,32,38,90,67", lines.get(0));
        assertEquals("80", lines.get(3));
    }

    @Test
    void decodeRejectsBrokenInput() {
        assertNull(KeyBindingsCodec.decode(List.of("1,2,3", "1", "1", "80")));
        assertNull(KeyBindingsCodec.decode(List.of("a,b,c,d,e,f,g", "1,2,3,4,5,6,7", "1,2,3,4,5,6,7", "80")));
        assertNull(KeyBindingsCodec.decode(List.of("1,2,3,4,5,6,7")));
    }
}
