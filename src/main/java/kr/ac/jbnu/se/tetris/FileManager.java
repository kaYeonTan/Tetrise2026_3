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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManager {

    private static final Logger LOG = Logger.getLogger(FileManager.class.getName());

    static String DIR = "data/";

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

    /** 저장에 성공하면 true. 실패하면 로그를 남기고 false (호출자가 안내 여부를 정한다). */
    static boolean writeLines(String path, ArrayList<String> lines, boolean append) {
        new File(DIR).mkdirs();
        new File(DIR + "save/").mkdirs();
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

    // 로그인
    public static boolean checkUser(String nick, String pw) {
        return accounts().login(nick, pw);
    }

    // 대소문자 맞춰서 원래 닉네임 돌려줌
    public static String getNick(String nick) {
        return accounts().canonicalNick(nick);
    }

    // 회원가입. 문제 있으면 메시지 리턴, 성공하면 null
    public static String addUser(String nick, String email, String pw) {
        return accounts().signUp(nick, email, pw);
    }

    static AccountService accounts() {
        return new AccountService(new FileUserRepository());
    }

    public static void addRecord(String nick, int score, int stage, int players, boolean clear) {
        new FileRecordRepository().add(new RecordEntry(nick, score, stage, players, clear, System.currentTimeMillis()));
    }

    // 키설정
    public static void saveKeys() {
        writeLines(DIR + "keys.txt", new ArrayList<>(KeyBindingsCodec.encode(KeyBindings.current())), false);
    }

    public static void loadKeys() {
        KeyBindings keys = KeyBindingsCodec.decode(readLines(DIR + "keys.txt"));
        if (keys != null) {
            keys.applyToGame();
        }
    }

    static int[] split(String s) {
        if (s.trim().length() == 0) {
            return new int[0];
        }
        String[] parts = s.split(",");
        int[] arr = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            arr[i] = Integer.parseInt(parts[i].trim());
        }
        return arr;
    }

    static String listToString(ArrayList<Integer> list) {
        String s = "";
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                s += ",";
            }
            s += list.get(i);
        }
        return s;
    }

    static String boardToString(int[][] board) {
        String s = "";
        for (int r = 0; r < 24; r++) {
            for (int c = 0; c < 10; c++) {
                s += board[r][c];
            }
        }
        return s;
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
        for (int i = 0; i < (p.isTwoPlayer ? 2 : 1); i++) {
            Player pl = p.players[i];
            String k = "p" + (i + 1);
            lines.add(k + "=" + pl.type + "," + pl.rot + "," + pl.x + "," + pl.y + "," + pl.hold + "," + (pl.holdUsed ? 1 : 0)
                    + "," + pl.score + "," + pl.lines + "," + pl.garbage);
            lines.add(k + "Next=" + listToString(pl.next));
            lines.add(k + "Bag=" + listToString(pl.bag));
            lines.add(k + "Board=" + boardToString(pl.board));
        }
        return writeLines(p.saveFile, lines, false);
    }

    private static void restorePlayer(Player pl, int[] a, String next, String bag, String board) {
        pl.type = a[0];
        pl.rot = a[1];
        pl.x = a[2];
        pl.y = a[3];
        pl.hold = a[4];
        pl.holdUsed = a[5] == 1;
        pl.score = a[6];
        pl.lines = a[7];
        pl.garbage = a[8];
        pl.next.clear();
        for (int n : split(next)) pl.next.add(n);
        pl.bag.clear();
        for (int n : split(bag)) pl.bag.add(n);
        for (int i = 0; i < 240; i++) {
            pl.board[i / 10][i % 10] = board.charAt(i) - '0';
        }
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
                String key = line.substring(0, line.indexOf("="));
                String value = line.substring(line.indexOf("=") + 1);
                if (key.equals("players")) players = Integer.parseInt(value);
                else if (key.equals("stage")) stage = Integer.parseInt(value);
                else if (key.equals("bossHp")) bossHp = Integer.parseInt(value);
                else if (key.equals("bossTimer")) bossTimer = Integer.parseInt(value);
                else if (key.equals("blueprint")) blueprintIndex = Integer.parseInt(value);
                else if (key.equals("gameTime")) gameTime = Long.parseLong(value);
                else if (key.equals("p1")) p1 = value;
                else if (key.equals("p1Next")) p1Next = value;
                else if (key.equals("p1Bag")) p1Bag = value;
                else if (key.equals("p1Board")) p1Board = value;
                else if (key.equals("p2")) p2 = value;
                else if (key.equals("p2Next")) p2Next = value;
                else if (key.equals("p2Bag")) p2Bag = value;
                else if (key.equals("p2Board")) p2Board = value;
            }
            p.newGame(players == 2);
            p.stage = stage;
            p.startStage(blueprintIndex);
            p.bossHp = bossHp;
            p.restoreBossPatternTimer(bossTimer);
            p.gameTime = gameTime;
            restorePlayer(p.players[0], split(p1), p1Next, p1Bag, p1Board);
            if (players == 2) {
                restorePlayer(p.players[1], split(p2), p2Next, p2Bag, p2Board);
            }
            p.saveFile = path;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            p.resetTemporaryBossEffects();
            return false;
        }
    }

    // 저장목록 {파일경로, 날짜, 스테이지, 점수, 인원}
    public static ArrayList<String[]> getSaveList(String nick) {
        ArrayList<String[]> list = new ArrayList<>();
        File[] files = new File(DIR + "save/").listFiles();
        if (files == null) {
            return list;
        }
        for (File f : files) {
            if (!f.getName().endsWith(".txt")) {
                continue;
            }
            String user = "";
            String date = "0", stage = "1", players = "1";
            int score = 0;
            for (String line : readLines(f.getPath())) {
                if (line.startsWith("user=")) user = line.substring(5);
                if (line.startsWith("date=")) date = line.substring(5);
                if (line.startsWith("stage=")) stage = line.substring(6);
                if (line.startsWith("players=")) players = line.substring(8);
                if (line.startsWith("p1=") || line.startsWith("p2=")) {
                    score += Integer.parseInt(line.substring(3).split(",")[6]);
                }
            }
            if (!user.equals(nick)) {
                continue;
            }
            list.add(new String[]{f.getPath(), date, stage, "" + score, players});
        }
        // 최근꺼 위로
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (Long.parseLong(list.get(j)[1]) > Long.parseLong(list.get(i)[1])) {
                    String[] temp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, temp);
                }
            }
        }
        return list;
    }

    public static void deleteSave(String path) {
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
    }
}
