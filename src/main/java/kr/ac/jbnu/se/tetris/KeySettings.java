package kr.ac.jbnu.se.tetris;

import java.awt.event.KeyEvent;
import java.util.Objects;

import kr.ac.jbnu.se.tetris.input.KeyAction;

/**
 * 설정 화면용 키 설정 한 벌(1인 / 2인 P1 / 2인 P2 키 묶음 + 공통 일시정지 키).
 * 편집본(draft) 복사·비교·중복 검사를 한 곳에 모은다.
 */
public final class KeySettings {

    public static final int SINGLE = 0;
    public static final int P1 = 1;
    public static final int P2 = 2;
    public static final int COMMON = 3;
    public static final int TAB_COUNT = 4;

    private static final String[] TAB_NAMES = {"1인 모드", "2인 P1", "2인 P2", "공통"};
    private static final String PAUSE_NAME = "일시정지";
    private static final KeyAction[] ACTIONS = KeyAction.values();

    /** 인덱스는 SINGLE, P1, P2. */
    private final kr.ac.jbnu.se.tetris.input.KeyBindings[] sets;
    private int pause;

    private KeySettings(kr.ac.jbnu.se.tetris.input.KeyBindings[] sets, int pause) {
        this.sets = sets;
        this.pause = pause;
    }

    /** 탭별 기본 키(KeyAction 선언 순서의 배열). */
    public static int[] defaultKeys(int tab) {
        return switch (tab) {
            case SINGLE -> kr.ac.jbnu.se.tetris.input.KeyBindings.defaultSingle().toArray();
            case P1 -> kr.ac.jbnu.se.tetris.input.KeyBindings.defaultP1().toArray();
            case P2 -> kr.ac.jbnu.se.tetris.input.KeyBindings.defaultP2().toArray();
            case COMMON -> new int[]{KeyEvent.VK_P};
            default -> throw new IllegalArgumentException("unknown tab: " + tab);
        };
    }

    public static KeySettings defaults() {
        return new KeySettings(new kr.ac.jbnu.se.tetris.input.KeyBindings[]{
                kr.ac.jbnu.se.tetris.input.KeyBindings.defaultSingle(),
                kr.ac.jbnu.se.tetris.input.KeyBindings.defaultP1(),
                kr.ac.jbnu.se.tetris.input.KeyBindings.defaultP2()}, KeyEvent.VK_P);
    }

    /** 지금 게임에 적용 중인 키의 복사본. */
    public static KeySettings current() {
        return new KeySettings(new kr.ac.jbnu.se.tetris.input.KeyBindings[]{
                GamePanel.keySingle().copy(), GamePanel.keyP1().copy(), GamePanel.keyP2().copy()},
                GamePanel.keyPause());
    }

    public KeySettings copy() {
        return new KeySettings(new kr.ac.jbnu.se.tetris.input.KeyBindings[]{
                sets[SINGLE].copy(), sets[P1].copy(), sets[P2].copy()}, pause);
    }

    public int rowCount(int tab) {
        return tab == COMMON ? 1 : ACTIONS.length;
    }

    public int get(int tab, int row) {
        return tab == COMMON ? pause : sets[tab].get(ACTIONS[row]);
    }

    public void set(int tab, int row, int code) {
        if (tab == COMMON) {
            pause = code;
        } else {
            sets[tab].set(ACTIONS[row], code);
        }
    }

    /** 한 키 묶음 안에서 같은 키를 쓰는 동작이 있으면 true. */
    public boolean hasDuplicate() {
        for (kr.ac.jbnu.se.tetris.input.KeyBindings set : sets) {
            if (set.hasDuplicate()) {
                return true;
            }
        }
        return false;
    }

    /** 이 편집본을 게임이 읽는 키에 반영한다. */
    public void applyToGame() {
        GamePanel.setKeys(sets[SINGLE], sets[P1], sets[P2], pause);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof KeySettings that
                && pause == that.pause
                && sets[SINGLE].equals(that.sets[SINGLE])
                && sets[P1].equals(that.sets[P1])
                && sets[P2].equals(that.sets[P2]);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sets[SINGLE], sets[P1], sets[P2], pause);
    }

    public static String actionName(int tab, int row) {
        return tab == COMMON ? PAUSE_NAME : ACTIONS[row].displayName();
    }

    /** 같은 키를 이미 쓰는 동작의 이름("탭 · 동작"), 없으면 null. 같은 화면에서 동시에 눌릴 수 있는 탭끼리만 비교한다. */
    public String findConflict(int tab, int row, int code) {
        for (int otherTab = 0; otherTab < TAB_COUNT; otherTab++) {
            if (!sharesKeyboard(tab, otherTab)) {
                continue;
            }
            for (int otherRow = 0; otherRow < rowCount(otherTab); otherRow++) {
                boolean isSameSlot = otherTab == tab && otherRow == row;
                if (!isSameSlot && get(otherTab, otherRow) == code) {
                    return TAB_NAMES[otherTab] + " · " + actionName(otherTab, otherRow);
                }
            }
        }
        return null;
    }

    private static boolean sharesKeyboard(int tab, int other) {
        return other == tab || other == COMMON || tab == COMMON
                || (tab == P1 && other == P2) || (tab == P2 && other == P1);
    }

    /** 키 지정 대상으로 쓸 수 없는 보조키. */
    public static boolean isIgnoredKey(int code) {
        return code == KeyEvent.VK_SHIFT || code == KeyEvent.VK_CONTROL || code == KeyEvent.VK_ALT
                || code == KeyEvent.VK_META || code == KeyEvent.VK_UNDEFINED;
    }
}
