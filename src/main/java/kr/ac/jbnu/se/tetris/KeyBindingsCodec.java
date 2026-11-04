package kr.ac.jbnu.se.tetris;

import java.util.ArrayList;
import java.util.List;

/** keys.txt 형식(4줄: 1인, P1, P2, 일시정지)과 KeyBindings 사이의 변환. 형식이 바뀌어도 이곳만 고친다. */
public final class KeyBindingsCodec {

    private static final int LINE_COUNT = 4;
    private static final int ACTION_COUNT = 7;

    private KeyBindingsCodec() {
    }

    public static List<String> encode(KeyBindings keys) {
        List<String> lines = new ArrayList<>();
        for (int tab = 0; tab < KeyBindings.COMMON; tab++) {
            StringBuilder sb = new StringBuilder();
            for (int row = 0; row < keys.rowCount(tab); row++) {
                if (row > 0) {
                    sb.append(',');
                }
                sb.append(keys.get(tab, row));
            }
            lines.add(sb.toString());
        }
        lines.add(String.valueOf(keys.get(KeyBindings.COMMON, 0)));
        return lines;
    }

    /** 형식이 맞지 않으면 null. */
    public static KeyBindings decode(List<String> lines) {
        if (lines.size() < LINE_COUNT) {
            return null;
        }
        try {
            KeyBindings keys = KeyBindings.defaults();
            for (int tab = 0; tab < KeyBindings.COMMON; tab++) {
                String[] parts = lines.get(tab).split(",");
                if (parts.length != ACTION_COUNT) {
                    return null;
                }
                for (int row = 0; row < ACTION_COUNT; row++) {
                    keys.set(tab, row, Integer.parseInt(parts[row].trim()));
                }
            }
            keys.set(KeyBindings.COMMON, 0, Integer.parseInt(lines.get(3).trim()));
            return keys;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
