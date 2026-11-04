package kr.ac.jbnu.se.tetris;

import java.util.List;

/** 플레이 기록 저장소. */
public interface RecordRepository {

    List<RecordEntry> findAll();

    boolean add(RecordEntry entry);
}
