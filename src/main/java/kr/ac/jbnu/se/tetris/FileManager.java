package kr.ac.jbnu.se.tetris;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManager {

    private static final Logger LOG = Logger.getLogger(FileManager.class.getName());

    static String DIR = "data/";
    private static final int BOARD_ROWS = 24;
    private static final int BOARD_COLS = 10;

    /** 읽기에 실패하면 로그를 남기고 지금까지 읽은 줄(없으면 빈 목록)을 돌려준다. */
    static ArrayList<String> readLines(String path) {
        ArrayList<String> list = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) {
            return list;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    list.add(line);
                }
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "파일을 읽지 못했어요: " + path, e);
        }
        return list;
    }

    /** 저장에 성공하면 true. 실패하면 로그를 남기고 false. */
    static boolean writeLines(String path, ArrayList<String> lines, boolean append) {
        try {
            Files.createDirectories(Paths.get(DIR + "save/"));
        } catch (IOException e) {
            LOG.log(Level.WARNING, "디렉터리 생성 실패", e);
        }
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path, append), StandardCharsets.UTF_8))) {
            for (String s : lines) {
                pw.println(s);
            }
            return true;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "파일을 저장하지 못했어요: " + path, e);
            return false;
        }
    }

    public static boolean login(String nickname, String password) {
        return accountService().login(nickname, password);
    }

    /** 대소문자가 다르게 입력돼도 가입 때 저장한 닉네임을 돌려준다. */
    public static String findStoredNickname(String nickname) {
        return accountService().canonicalNick(nickname);
    }

    /** 문제가 있으면 안내 메시지를, 성공하면 null을 돌려준다. */
    public static String signUp(String nickname, String email, String password) {
        return accountService().signUp(nickname, email, password);
    }

    static AccountService accountService() {
        return new AccountService(new FileUserRepository());
    }

    public static void addRecord(String nickname, int score, int stage, int players, boolean cleared) {
        new FileRecordRepository().add(new RecordEntry(nickname, score, stage, players, cleared, System.currentTimeMillis()));
    }

    public static void saveKeys() {
        writeLines(DIR + "keys.txt", new ArrayList<>(KeyBindingsCodec.encode(KeyBindings.current())), false);
    }

    public static void loadKeys() {
        KeyBindings keys = KeyBindingsCodec.decode(readLines(DIR + "keys.txt"));
        if (keys != null) {
            keys.applyToGame();
        }
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

    // 세이브
    public static boolean saveGame(GamePanel p) {
        if (p.saveFile == null) {
            // 파일이름에 한글 넣으면 깨지는 컴퓨터 있어서 시간으로만 함
            p.saveFile = DIR + "save/save_" + System.currentTimeMillis() + ".txt";
        }
        ArrayList<String> lines = new ArrayList<>();
        lines.add("user=" + p.loginUser);
        lines.add("date=" + System.currentTimeMillis());
        lines.add("players=" + (p.isTwoPlayer ? 2 : 1));
        lines.add("stage=" + p.stage);
        lines.add("bossHp=" + p.bossHp);
        lines.add("bossTimer=" + p.bossTimer);
        lines.add("blueprint=" + p.bossPattern.blueprintIndex());
        lines.add("gameTime=" + p.gameTime);
        lines.add("p1=" + p.p1Type + "," + p.p1Rot + "," + p.p1X + "," + p.p1Y + "," + p.p1Hold + "," + (p.p1HoldUsed ? 1 : 0)
                + "," + p.p1Score + "," + p.p1Lines + "," + p.p1Garbage);
        lines.add("p1Next=" + joinIntegers(p.p1Next));
        lines.add("p1Bag=" + joinIntegers(p.p1Bag));
        lines.add("p1Board=" + encodeBoard(p.board1));
        if (p.isTwoPlayer) {
            lines.add("p2=" + p.p2Type + "," + p.p2Rot + "," + p.p2X + "," + p.p2Y + "," + p.p2Hold + "," + (p.p2HoldUsed ? 1 : 0)
                    + "," + p.p2Score + "," + p.p2Lines + "," + p.p2Garbage);
            lines.add("p2Next=" + joinIntegers(p.p2Next));
            lines.add("p2Bag=" + joinIntegers(p.p2Bag));
            lines.add("p2Board=" + encodeBoard(p.board2));
        }
        return writeLines(p.saveFile, lines, false);
    }

    public static boolean loadGame(String path, GamePanel p) {
        ArrayList<String> lines = readLines(path);
        if (lines.size() < 10) {
            return false;
        }
        try {
            String p1 = "", p1Next = "", p1Bag = "", p1Board = "";
            String p2 = "", p2Next = "", p2Bag = "", p2Board = "";
            int players = 1, stage = 1, bossHp = 0, bossTimer = 0, blueprintIndex = -1;
            long gameTime = 0;
            for (String line : lines) {
                int separator = line.indexOf("=");
                String key = line.substring(0, separator);
                String value = line.substring(separator + 1);
                switch (key) {
                    case "players" -> players = Integer.parseInt(value);
                    case "stage" -> stage = Integer.parseInt(value);
                    case "bossHp" -> bossHp = Integer.parseInt(value);
                    case "bossTimer" -> bossTimer = Integer.parseInt(value);
                    case "blueprint" -> blueprintIndex = Integer.parseInt(value);
                    case "gameTime" -> gameTime = Long.parseLong(value);
                    case "p1" -> p1 = value;
                    case "p1Next" -> p1Next = value;
                    case "p1Bag" -> p1Bag = value;
                    case "p1Board" -> p1Board = value;
                    case "p2" -> p2 = value;
                    case "p2Next" -> p2Next = value;
                    case "p2Bag" -> p2Bag = value;
                    case "p2Board" -> p2Board = value;
                    default -> { }
                }
            }
            p.newGame(players == 2);
            p.stage = stage;
            p.startStage(blueprintIndex);
            p.bossHp = bossHp;
            p.restoreBossPatternTimer(bossTimer);
            p.gameTime = gameTime;
            int[] state1 = parseIntList(p1);
            p.p1Type = state1[0];
            p.p1Rot = state1[1];
            p.p1X = state1[2];
            p.p1Y = state1[3];
            p.p1Hold = state1[4];
            p.p1HoldUsed = state1[5] == 1;
            p.p1Score = state1[6];
            p.p1Lines = state1[7];
            p.p1Garbage = state1[8];
            replaceAll(p.p1Next, p1Next);
            replaceAll(p.p1Bag, p1Bag);
            decodeBoard(p1Board, p.board1);
            if (players == 2) {
                int[] state2 = parseIntList(p2);
                p.p2Type = state2[0];
                p.p2Rot = state2[1];
                p.p2X = state2[2];
                p.p2Y = state2[3];
                p.p2Hold = state2[4];
                p.p2HoldUsed = state2[5] == 1;
                p.p2Score = state2[6];
                p.p2Lines = state2[7];
                p.p2Garbage = state2[8];
                replaceAll(p.p2Next, p2Next);
                replaceAll(p.p2Bag, p2Bag);
                decodeBoard(p2Board, p.board2);
            }
            p.saveFile = path;
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "저장 불러오기 실패", e);
            p.resetTemporaryBossEffects();
            return false;
        }
    }

    // 저장목록 {파일경로, 날짜, 스테이지, 점수, 인원}
    public static ArrayList<String[]> getSaveList(String nickname) {
        ArrayList<String[]> saves = new ArrayList<>();
        File[] files = new File(DIR + "save/").listFiles();
        if (files == null) {
            return saves;
        }
        for (File file : files) {
            if (!file.getName().endsWith(".txt")) {
                continue;
            }
            String user = "";
            String date = "0", stage = "1", players = "1";
            int score = 0;
            for (String line : readLines(file.getPath())) {
                if (line.startsWith("user=")) user = line.substring(5);
                if (line.startsWith("date=")) date = line.substring(5);
                if (line.startsWith("stage=")) stage = line.substring(6);
                if (line.startsWith("players=")) players = line.substring(8);
                if (line.startsWith("p1=") || line.startsWith("p2=")) {
                    score += Integer.parseInt(line.substring(3).split(",")[6]);
                }
            }
            if (user.equals(nickname)) {
                saves.add(new String[]{file.getPath(), date, stage, String.valueOf(score), players});
            }
        }
        saves.sort((a, b) -> Long.compare(Long.parseLong(b[1]), Long.parseLong(a[1])));
        return saves;
    }

    public static void deleteSave(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            LOG.log(Level.WARNING, "저장 파일 삭제 실패: " + path, e);
        }
    }
}
