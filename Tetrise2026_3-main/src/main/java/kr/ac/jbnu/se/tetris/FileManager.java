package kr.ac.jbnu.se.tetris;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FileManager {

    static String DIR = "data/";

    static ArrayList<String> readLines(String path) {
        ArrayList<String> list = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) {
            return list;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0) {
                    list.add(line);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    static boolean writeLines(String path, ArrayList<String> lines, boolean append) {
        try {
            new File(DIR).mkdirs();
            new File(DIR + "save/").mkdirs();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path, append), "UTF-8"));
            for (String s : lines) {
                pw.println(s);
            }
            pw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 로그인
    public static boolean checkUser(String nick, String pw) {
        ArrayList<String> list = readLines(DIR + "users.txt");
        for (String line : list) {
            String[] data = line.split(",");
            if (data[0].equalsIgnoreCase(nick) && data[2].equals(pw)) {
                return true;
            }
        }
        return false;
    }

    // 대소문자 맞춰서 원래 닉네임 돌려줌
    public static String getNick(String nick) {
        ArrayList<String> list = readLines(DIR + "users.txt");
        for (String line : list) {
            String[] data = line.split(",");
            if (data[0].equalsIgnoreCase(nick)) {
                return data[0];
            }
        }
        return nick;
    }

    // 회원가입. 문제 있으면 메시지 리턴, 성공하면 null
    public static String addUser(String nick, String email, String pw) {
        ArrayList<String> list = readLines(DIR + "users.txt");
        for (String line : list) {
            String[] data = line.split(",");
            if (data[0].equalsIgnoreCase(nick)) {
                return "이미 사용 중인 닉네임이에요.";
            }
            if (data[1].equalsIgnoreCase(email)) {
                return "이미 가입된 이메일이에요.";
            }
        }
        ArrayList<String> add = new ArrayList<>();
        add.add(nick + "," + email + "," + pw);
        if (!writeLines(DIR + "users.txt", add, true)) {
            return "저장에 실패했어요.";
        }
        return null;
    }

    public static void addRecord(String nick, int score, int stage, int players, boolean clear) {
        ArrayList<String> add = new ArrayList<>();
        add.add(nick + "," + score + "," + stage + "," + players + "," + (clear ? 1 : 0) + "," + System.currentTimeMillis());
        writeLines(DIR + "records.txt", add, true);
    }

    public static ArrayList<String[]> getRecords() {
        ArrayList<String[]> result = new ArrayList<>();
        for (String line : readLines(DIR + "records.txt")) {
            String[] data = line.split(",");
            if (data.length == 6) {
                result.add(data);
            }
        }
        return result;
    }

    // 키설정
    public static void saveKeys() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add(join(GamePanel.keySingle));
        lines.add(join(GamePanel.keyP1));
        lines.add(join(GamePanel.keyP2));
        lines.add("" + GamePanel.keyPause);
        writeLines(DIR + "keys.txt", lines, false);
    }

    public static void loadKeys() {
        ArrayList<String> lines = readLines(DIR + "keys.txt");
        if (lines.size() < 4) {
            return;
        }
        try {
            int[] a = split(lines.get(0));
            int[] b = split(lines.get(1));
            int[] c = split(lines.get(2));
            if (a.length == 7 && b.length == 7 && c.length == 7) {
                GamePanel.keySingle = a;
                GamePanel.keyP1 = b;
                GamePanel.keyP2 = c;
                GamePanel.keyPause = Integer.parseInt(lines.get(3).trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String join(int[] arr) {
        String s = "";
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                s += ",";
            }
            s += arr[i];
        }
        return s;
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
        lines.add("gameTime=" + p.gameTime);
        lines.add("p1=" + p.p1Type + "," + p.p1Rot + "," + p.p1X + "," + p.p1Y + "," + p.p1Hold + "," + (p.p1HoldUsed ? 1 : 0)
                + "," + p.p1Score + "," + p.p1Lines + "," + p.p1Garbage);
        lines.add("p1Next=" + listToString(p.p1Next));
        lines.add("p1Bag=" + listToString(p.p1Bag));
        lines.add("p1Board=" + boardToString(p.board1));
        if (p.isTwoPlayer) {
            lines.add("p2=" + p.p2Type + "," + p.p2Rot + "," + p.p2X + "," + p.p2Y + "," + p.p2Hold + "," + (p.p2HoldUsed ? 1 : 0)
                    + "," + p.p2Score + "," + p.p2Lines + "," + p.p2Garbage);
            lines.add("p2Next=" + listToString(p.p2Next));
            lines.add("p2Bag=" + listToString(p.p2Bag));
            lines.add("p2Board=" + boardToString(p.board2));
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
            int players = 1, stage = 1, bossHp = 0, bossTimer = 0;
            long gameTime = 0;
            for (String line : lines) {
                String key = line.substring(0, line.indexOf("="));
                String value = line.substring(line.indexOf("=") + 1);
                if (key.equals("players")) players = Integer.parseInt(value);
                else if (key.equals("stage")) stage = Integer.parseInt(value);
                else if (key.equals("bossHp")) bossHp = Integer.parseInt(value);
                else if (key.equals("bossTimer")) bossTimer = Integer.parseInt(value);
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
            p.startStage();
            p.bossHp = bossHp;
            p.bossTimer = bossTimer;
            p.gameTime = gameTime;
            int[] a = split(p1);
            p.p1Type = a[0];
            p.p1Rot = a[1];
            p.p1X = a[2];
            p.p1Y = a[3];
            p.p1Hold = a[4];
            p.p1HoldUsed = a[5] == 1;
            p.p1Score = a[6];
            p.p1Lines = a[7];
            p.p1Garbage = a[8];
            p.p1Next.clear();
            for (int n : split(p1Next)) p.p1Next.add(n);
            p.p1Bag.clear();
            for (int n : split(p1Bag)) p.p1Bag.add(n);
            for (int i = 0; i < 240; i++) {
                p.board1[i / 10][i % 10] = p1Board.charAt(i) - '0';
            }
            if (players == 2) {
                // 2p도 똑같이
                int[] b = split(p2);
                p.p2Type = b[0];
                p.p2Rot = b[1];
                p.p2X = b[2];
                p.p2Y = b[3];
                p.p2Hold = b[4];
                p.p2HoldUsed = b[5] == 1;
                p.p2Score = b[6];
                p.p2Lines = b[7];
                p.p2Garbage = b[8];
                p.p2Next.clear();
                for (int n : split(p2Next)) p.p2Next.add(n);
                p.p2Bag.clear();
                for (int n : split(p2Bag)) p.p2Bag.add(n);
                for (int i = 0; i < 240; i++) {
                    p.board2[i / 10][i % 10] = p2Board.charAt(i) - '0';
                }
            }
            p.saveFile = path;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
