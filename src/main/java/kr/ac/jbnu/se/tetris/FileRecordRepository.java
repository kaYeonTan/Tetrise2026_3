package kr.ac.jbnu.se.tetris;

import java.util.ArrayList;
import java.util.List;

/** data/records.txt 에 기존과 같은 형식으로 읽고 쓴다. */
public class FileRecordRepository implements RecordRepository {

    private static final String FILE_NAME = "records.txt";

    @Override
    public List<RecordEntry> findAll() {
        List<RecordEntry> entries = new ArrayList<>();
        for (String line : FileManager.readLines(FileManager.DIR + FILE_NAME)) {
            RecordEntry entry = RecordEntry.fromLine(line);
            if (entry != null) {
                entries.add(entry);
            }
        }
        return entries;
    }

    @Override
    public boolean add(RecordEntry entry) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add(entry.toLine());
        return FileManager.writeLines(FileManager.DIR + FILE_NAME, lines, true);
    }
}
