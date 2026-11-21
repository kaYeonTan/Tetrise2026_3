package kr.ac.jbnu.se.tetris.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.ac.jbnu.se.tetris.GamePanel;

/**
 * 저장 파일(key=value 줄) 한 벌. 저장·불러오기·목록 요약이 같은 키 이름과 순서를 이곳에서만 알도록 모았다.
 * 플레이어 상태는 파일의 원문을 그대로 들고 있다가 게임에 적용할 때 해석한다.
 */
public final class SaveData {

    private static final int BOARD_ROWS = 24;
    private static final int BOARD_COLS = 10;
    private static final int PLAYER_STATE_SCORE_INDEX = 6;

    /** 한 플레이어의 저장 항목(p1/p1Next/p1Bag/p1Board 줄의 값). */
    public record PlayerData(String state, String next, String bag, String board) {
    }

    /** 저장 목록에 보여 줄 요약. */
    public record Summary(String user, String date, String stage, String players, int score) {

        /** {파일경로, 날짜, 스테이지, 점수, 인원} */
        public String[] toRow(String path) {
            return new String[]{path, date, stage, String.valueOf(score), players};
        }
    }

    private final String user;
    private final String date;
    private final int players;
    private final int stage;
    private final int bossHp;
    private final int bossTimer;
    private final int blueprint;
    private final long gameTime;
    private final PlayerData p1;
    private final PlayerData p2;

    private SaveData(String user, String date, int players, int stage, int bossHp, int bossTimer, int blueprint,
                     long gameTime, PlayerData p1, PlayerData p2) {
        this.user = user;
        this.date = date;
        this.players = players;
        this.stage = stage;
        this.bossHp = bossHp;
        this.bossTimer = bossTimer;
        this.blueprint = blueprint;
        this.gameTime = gameTime;
        this.p1 = p1;
        this.p2 = p2;
    }

    /** 지금 게임 상태를 저장 항목으로 묶는다. */
    public static SaveData fromPanel(GamePanel p) {
        PlayerData one = new PlayerData(
                playerState(p.p1Type, p.p1Rot, p.p1X, p.p1Y, p.p1Hold, p.p1HoldUsed, p.p1Score, p.p1Lines, p.p1Garbage),
                joinIntegers(p.p1Next), joinIntegers(p.p1Bag), encodeBoard(p.board1));
        PlayerData two = null;
        if (p.isTwoPlayer) {
            two = new PlayerData(
                    playerState(p.p2Type, p.p2Rot, p.p2X, p.p2Y, p.p2Hold, p.p2HoldUsed, p.p2Score, p.p2Lines, p.p2Garbage),
                    joinIntegers(p.p2Next), joinIntegers(p.p2Bag), encodeBoard(p.board2));
        }
        return new SaveData(String.valueOf(p.getLoginUser()), String.valueOf(System.currentTimeMillis()),
                p.isTwoPlayer ? 2 : 1, p.stage, p.bossHp, p.bossTimer, p.bossPattern.blueprintIndex(), p.gameTime,
                one, two);
    }

    /** key=value 줄을 Map으로 읽는다. '='가 없는 줄이 있으면 예외(불러오기 실패). */
    public static Map<String, String> parseLines(List<String> lines) {
        Map<String, String> map = new HashMap<>();
        for (String line : lines) {
            int separator = line.indexOf("=");
            map.put(line.substring(0, separator), line.substring(separator + 1));
        }
        return map;
    }

    /** 숫자 항목이 잘못됐으면 NumberFormatException. 빠진 항목은 기본값. */
    public static SaveData fromMap(Map<String, String> map) {
        return new SaveData(map.getOrDefault("user", ""), map.getOrDefault("date", "0"),
                intOf(map, "players", 1), intOf(map, "stage", 1), intOf(map, "bossHp", 0),
                intOf(map, "bossTimer", 0), intOf(map, "blueprint", -1),
                map.containsKey("gameTime") ? Long.parseLong(map.get("gameTime")) : 0L,
                playerOf(map, "p1"), playerOf(map, "p2"));
    }

    /** 저장 목록용 요약. '='가 없는 줄은 건너뛴다. */
    public static Summary summarize(List<String> lines) {
        Map<String, String> map = new HashMap<>();
        for (String line : lines) {
            int separator = line.indexOf("=");
            if (separator >= 0) {
                map.put(line.substring(0, separator), line.substring(separator + 1));
            }
        }
        int score = 0;
        for (String key : new String[]{"p1", "p2"}) {
            if (map.containsKey(key)) {
                score += Integer.parseInt(map.get(key).split(",")[PLAYER_STATE_SCORE_INDEX]);
            }
        }
        return new Summary(map.getOrDefault("user", ""), map.getOrDefault("date", "0"),
                map.getOrDefault("stage", "1"), map.getOrDefault("players", "1"), score);
    }

    public ArrayList<String> toLines() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("user=" + user);
        lines.add("date=" + date);
        lines.add("players=" + players);
        lines.add("stage=" + stage);
        lines.add("bossHp=" + bossHp);
        lines.add("bossTimer=" + bossTimer);
        lines.add("blueprint=" + blueprint);
        lines.add("gameTime=" + gameTime);
        addPlayerLines(lines, "p1", p1);
        if (players == 2) {
            addPlayerLines(lines, "p2", p2);
        }
        return lines;
    }

    /** 저장된 상태를 게임에 되돌린다. 항목이 잘못됐으면 중간에 예외가 난다. */
    public void applyTo(GamePanel p) {
        p.newGame(players == 2);
        p.stage = stage;
        p.startStage(blueprint);
        p.bossHp = bossHp;
        p.restoreBossPatternTimer(bossTimer);
        p.gameTime = gameTime;
        int[] state1 = parseIntList(p1.state());
        p.p1Type = state1[0];
        p.p1Rot = state1[1];
        p.p1X = state1[2];
        p.p1Y = state1[3];
        p.p1Hold = state1[4];
        p.p1HoldUsed = state1[5] == 1;
        p.p1Score = state1[6];
        p.p1Lines = state1[7];
        p.p1Garbage = state1[8];
        replaceAll(p.p1Next, p1.next());
        replaceAll(p.p1Bag, p1.bag());
        decodeBoard(p1.board(), p.board1);
        if (players == 2) {
            int[] state2 = parseIntList(p2.state());
            p.p2Type = state2[0];
            p.p2Rot = state2[1];
            p.p2X = state2[2];
            p.p2Y = state2[3];
            p.p2Hold = state2[4];
            p.p2HoldUsed = state2[5] == 1;
            p.p2Score = state2[6];
            p.p2Lines = state2[7];
            p.p2Garbage = state2[8];
            replaceAll(p.p2Next, p2.next());
            replaceAll(p.p2Bag, p2.bag());
            decodeBoard(p2.board(), p.board2);
        }
    }

    private static void addPlayerLines(List<String> lines, String prefix, PlayerData player) {
        lines.add(prefix + "=" + player.state());
        lines.add(prefix + "Next=" + player.next());
        lines.add(prefix + "Bag=" + player.bag());
        lines.add(prefix + "Board=" + player.board());
    }

    private static PlayerData playerOf(Map<String, String> map, String prefix) {
        return new PlayerData(map.getOrDefault(prefix, ""), map.getOrDefault(prefix + "Next", ""),
                map.getOrDefault(prefix + "Bag", ""), map.getOrDefault(prefix + "Board", ""));
    }

    private static int intOf(Map<String, String> map, String key, int fallback) {
        return map.containsKey(key) ? Integer.parseInt(map.get(key)) : fallback;
    }

    private static String playerState(int type, int rot, int x, int y, int hold, boolean holdUsed, int score,
                                      int lines, int garbage) {
        return type + "," + rot + "," + x + "," + y + "," + hold + "," + (holdUsed ? 1 : 0)
                + "," + score + "," + lines + "," + garbage;
    }

    private static int[] parseIntList(String csv) {
        if (csv.trim().isEmpty()) {
            return new int[0];
        }
        String[] parts = csv.split(",");
        int[] values = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            values[i] = Integer.parseInt(parts[i].trim());
        }
        return values;
    }

    private static String joinIntegers(ArrayList<Integer> values) {
        StringBuilder joined = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                joined.append(',');
            }
            joined.append(values.get(i));
        }
        return joined.toString();
    }

    private static String encodeBoard(int[][] board) {
        StringBuilder encoded = new StringBuilder();
        for (int row = 0; row < BOARD_ROWS; row++) {
            for (int col = 0; col < BOARD_COLS; col++) {
                encoded.append(board[row][col]);
            }
        }
        return encoded.toString();
    }

    private static void decodeBoard(String encoded, int[][] board) {
        for (int i = 0; i < BOARD_ROWS * BOARD_COLS; i++) {
            board[i / BOARD_COLS][i % BOARD_COLS] = encoded.charAt(i) - '0';
        }
    }

    private static void replaceAll(ArrayList<Integer> target, String csv) {
        target.clear();
        for (int value : parseIntList(csv)) {
            target.add(value);
        }
    }
}
