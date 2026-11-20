package kr.ac.jbnu.se.tetris;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import kr.ac.jbnu.se.tetris.repository.TextFileStore;

/** data/records.txt 에 기존과 같은 형식으로 읽고 쓴다. */
public class FileRecordRepository implements RecordRepository {

    private static final String FILE_NAME = "records.txt";

    private final TextFileStore store;

    public FileRecordRepository(Path baseDir) {
        this.store = new TextFileStore(baseDir);
    }

    @Override
    public List<RecordEntry> findAll() {
        List<RecordEntry> entries = new ArrayList<>();
        for (String line : store.readLines(store.dir() + FILE_NAME)) {
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
        return store.writeLines(store.dir() + FILE_NAME, lines, true);
    }
}
