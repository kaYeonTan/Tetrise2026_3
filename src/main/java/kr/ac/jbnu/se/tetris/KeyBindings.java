package kr.ac.jbnu.se.tetris;

import java.awt.event.KeyEvent;
import java.util.Arrays;

/** 키 설정 한 벌(1인 / 2인 P1 / 2인 P2 / 공통). 편집본 복사·비교·중복 검사를 한 곳에 모은다. */
public final class KeyBindings {

    public static final int SINGLE = 0;
    public static final int P1 = 1;
    public static final int P2 = 2;
    public static final int COMMON = 3;
    public static final int TAB_COUNT = 4;

    private static final String[] TAB_NAMES = {"1인 모드", "2인 P1", "2인 P2", "공통"};
    private static final String[] ACTION_NAMES = {"왼쪽 이동", "오른쪽 이동", "천천히 내리기", "바로 떨어뜨리기",
            "시계 방향 회전", "반시계 방향 회전", "홀드"};
    private static final String PAUSE_NAME = "일시정지";

    private final int[][] sets;

    private KeyBindings(int[][] sets) {
        this.sets = sets;
    }

    /** 왼 오 아래 스페이스 회전 반대회전 홀드 순서. */
    public static int[] defaultKeys(int tab) {
        return switch (tab) {
            case SINGLE -> new int[]{KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_SPACE,
                    KeyEvent.VK_UP, KeyEvent.VK_Z, KeyEvent.VK_C};
            case P1 -> new int[]{KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_S, KeyEvent.VK_SPACE,
                    KeyEvent.VK_W, KeyEvent.VK_Q, KeyEvent.VK_E};
            case P2 -> new int[]{KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_ENTER,
                    KeyEvent.VK_UP, KeyEvent.VK_SLASH, KeyEvent.VK_PERIOD};
            case COMMON -> new int[]{KeyEvent.VK_P};
            default -> throw new IllegalArgumentException("unknown tab: " + tab);
        };
    }

    public static KeyBindings defaults() {
        return new KeyBindings(new int[][]{defaultKeys(SINGLE), defaultKeys(P1), defaultKeys(P2), defaultKeys(COMMON)});
    }

    /** 지금 게임에 적용 중인 키. */
    public static KeyBindings current() {
        return new KeyBindings(new int[][]{GamePanel.keySingle.clone(), GamePanel.keyP1.clone(),
                GamePanel.keyP2.clone(), {GamePanel.keyPause}});
    }

    public KeyBindings copy() {
        int[][] copiedSets = new int[TAB_COUNT][];
        for (int tab = 0; tab < TAB_COUNT; tab++) {
            copiedSets[tab] = sets[tab].clone();
        }
        return new KeyBindings(copiedSets);
    }

    public int rowCount(int tab) {
        return sets[tab].length;
    }

    public int get(int tab, int row) {
        return sets[tab][row];
    }

    public void set(int tab, int row, int code) {
        sets[tab][row] = code;
    }

    public boolean sameAs(KeyBindings other) {
        for (int tab = 0; tab < TAB_COUNT; tab++) {
            if (!Arrays.equals(sets[tab], other.sets[tab])) {
                return false;
            }
        }
        return true;
    }

    /** 이 편집본을 게임이 읽는 키 배열에 반영한다. */
    public void applyToGame() {
        GamePanel.keySingle = sets[SINGLE].clone();
        GamePanel.keyP1 = sets[P1].clone();
        GamePanel.keyP2 = sets[P2].clone();
        GamePanel.keyPause = sets[COMMON][0];
    }

    public static String actionName(int tab, int row) {
        return tab == COMMON ? PAUSE_NAME : ACTION_NAMES[row];
    }

    /** 같은 키를 이미 쓰는 동작의 이름("탭 · 동작"), 없으면 null. 같은 화면에서 동시에 눌릴 수 있는 탭끼리만 비교한다. */
    public String findConflict(int tab, int row, int code) {
        for (int otherTab = 0; otherTab < TAB_COUNT; otherTab++) {
            if (!sharesKeyboard(tab, otherTab)) {
                continue;
            }
            for (int otherRow = 0; otherRow < sets[otherTab].length; otherRow++) {
                boolean isSameSlot = otherTab == tab && otherRow == row;
                if (!isSameSlot && sets[otherTab][otherRow] == code) {
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
