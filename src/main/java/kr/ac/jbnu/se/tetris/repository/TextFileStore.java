package kr.ac.jbnu.se.tetris.repository;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/** UTF-8 텍스트 파일 읽기/쓰기. 데이터 폴더(baseDir)는 생성자로 받는다. */
public final class TextFileStore {

    private static final Logger LOG = Logger.getLogger(TextFileStore.class.getName());

    private final String dir;

    public TextFileStore(Path baseDir) {
        this.dir = baseDir.toString().replace('\\', '/') + "/";
    }

    /** 항상 '/'로 끝나는 데이터 폴더 경로. */
    public String dir() {
        return dir;
    }

    /** 읽기에 실패하면 로그를 남기고 지금까지 읽은 줄(없으면 빈 목록)을 돌려준다. */
    public ArrayList<String> readLines(String path) {
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
    public boolean writeLines(String path, ArrayList<String> lines, boolean append) {
        try {
            Files.createDirectories(Paths.get(dir() + "save/"));
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
}
