package kr.ac.jbnu.se.tetris.persistence;

import java.util.List;

import kr.ac.jbnu.se.tetris.FileManager;
import kr.ac.jbnu.se.tetris.GamePanel;

/** 기존 FileManager 텍스트 저장을 그대로 사용하는 구현. */
public final class FileSinglePlayerSaveStore implements SinglePlayerSaveStore {

    @Override
    public boolean save(GamePanel panel) {
        return FileManager.saveGame(panel);
    }

    @Override
    public boolean load(String path, GamePanel panel) {
        return FileManager.loadGame(path, panel);
    }

    @Override
    public List<String[]> list(String userId) {
        return FileManager.getSaveList(userId);
    }

    @Override
    public void delete(String path) {
        FileManager.deleteSave(path);
    }
}
