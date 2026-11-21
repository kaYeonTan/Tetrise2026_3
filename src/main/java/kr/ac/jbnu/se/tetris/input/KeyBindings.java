package kr.ac.jbnu.se.tetris.input;

import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

/** 동작(KeyAction)별 키 코드 한 벌. */
public final class KeyBindings {

    private final EnumMap<KeyAction, Integer> keys;

    private KeyBindings(EnumMap<KeyAction, Integer> keys) {
        this.keys = keys;
    }

    /** 키 코드를 KeyAction 선언 순서(왼 오 아래 스페이스 회전 반대회전 홀드)로 받아 만든다. */
    private static KeyBindings of(int... codes) {
        KeyAction[] actions = KeyAction.values();
        EnumMap<KeyAction, Integer> map = new EnumMap<>(KeyAction.class);
        for (int i = 0; i < actions.length; i++) {
            map.put(actions[i], codes[i]);
        }
        return new KeyBindings(map);
    }

    public static KeyBindings defaultSingle() {
        return of(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_SPACE,
                KeyEvent.VK_UP, KeyEvent.VK_Z, KeyEvent.VK_C);
    }

    public static KeyBindings defaultP1() {
        return of(KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_S, KeyEvent.VK_SPACE,
                KeyEvent.VK_W, KeyEvent.VK_Q, KeyEvent.VK_E);
    }

    public static KeyBindings defaultP2() {
        return of(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_ENTER,
                KeyEvent.VK_UP, KeyEvent.VK_SLASH, KeyEvent.VK_PERIOD);
    }

    public int get(KeyAction action) {
        return keys.get(action);
    }

    public void set(KeyAction action, int code) {
        keys.put(action, code);
    }

    public KeyBindings copy() {
        return new KeyBindings(new EnumMap<>(keys));
    }

    /** 서로 다른 동작이 같은 키를 쓰면 true. */
    public boolean hasDuplicate() {
        Set<Integer> seen = new HashSet<>();
        for (int code : keys.values()) {
            if (!seen.add(code)) {
                return true;
            }
        }
        return false;
    }

    /** KeyAction 선언 순서의 키 코드 배열(랜덤 키 보스 패턴 등 배열 기반 코드용). */
    public int[] toArray() {
        KeyAction[] actions = KeyAction.values();
        int[] codes = new int[actions.length];
        for (int i = 0; i < actions.length; i++) {
            codes[i] = keys.get(actions[i]);
        }
        return codes;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof KeyBindings that && keys.equals(that.keys);
    }

    @Override
    public int hashCode() {
        return keys.hashCode();
    }
}
