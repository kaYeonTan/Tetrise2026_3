package kr.ac.jbnu.se.tetris.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import kr.ac.jbnu.se.tetris.GamePanel;

/** data/save/*.txt 에 기존과 같은 형식으로 읽고 쓴다. */
public class FileSaveRepository implements SaveRepository {

    private static final Logger LOG = Logger.getLogger(FileSaveRepository.class.getName());

    private final TextFileStore store;

    public FileSaveRepository(Path baseDir) {
        this.store = new TextFileStore(baseDir);
    }

    @Override
    public boolean saveGame(GamePanel p) {
        if (p.saveFile == null) {
            // 파일이름에 한글 넣으면 깨지는 컴퓨터 있어서 시간으로만 함
            p.saveFile = store.dir() + "save/save_" + System.currentTimeMillis() + ".txt";
        }
        return store.writeLines(p.saveFile, SaveData.fromPanel(p).toLines(), false);
    }

    @Override
    public boolean loadGame(String path, GamePanel p) {
        ArrayList<String> lines = store.readLines(path);
        if (lines.size() < 10) {
            return false;
        }
        try {
            SaveData.fromMap(SaveData.parseLines(lines)).applyTo(p);
            p.saveFile = path;
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "저장 불러오기 실패", e);
            p.resetTemporaryBossEffects();
            return false;
        }
    }

    @Override
    public ArrayList<String[]> getSaveList(String nickname) {
        ArrayList<String[]> saves = new ArrayList<>();
        File[] files = new File(store.dir() + "save/").listFiles();
        if (files == null) {
            return saves;
        }
        for (File file : files) {
            if (!file.getName().endsWith(".txt")) {
                continue;
            }
            SaveData.Summary summary = SaveData.summarize(store.readLines(file.getPath()));
            if (summary.user().equals(nickname)) {
                saves.add(summary.toRow(file.getPath()));
            }
        }
        saves.sort((a, b) -> Long.compare(Long.parseLong(b[1]), Long.parseLong(a[1])));
        return saves;
    }

    @Override
    public void deleteSave(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            LOG.log(Level.WARNING, "저장 파일 삭제 실패: " + path, e);
        }
    }
}
