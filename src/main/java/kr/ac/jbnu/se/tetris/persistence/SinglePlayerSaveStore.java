package kr.ac.jbnu.se.tetris.persistence;

import java.util.List;

import kr.ac.jbnu.se.tetris.GamePanel;

/** 1인 저장(레거시 텍스트 포맷) 저장소 추상화. 호출자는 파일 위치와 포맷을 모른다. */
public interface SinglePlayerSaveStore {

    boolean save(GamePanel panel);

    boolean load(String path, GamePanel panel);

    /** 각 항목은 FileManager.getSaveList 형식(첫 칸이 저장 경로). */
    List<String[]> list(String userId);

    void delete(String path);
}
