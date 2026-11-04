package kz.atu.lab10;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/** Чтение и запись списка фильмов в JSON (Jackson пишет и читает UTF-8). */
public class NoteStorage {
    private final ObjectMapper mapper = new ObjectMapper();

    public void save(Path path, List<Note> notes) throws IOException {
        Path target = path.toAbsolutePath();
        Path parent = target.getParent();
        Path tmp = Files.createTempFile(parent, "notes-", ".tmp");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), notes);
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    public List<Note> load(Path path) throws IOException {
        List<Note> list = mapper.readValue(path.toFile(), new TypeReference<List<Note>>() {});
        if (list == null) {
            throw new IOException("Файл не содержит списка заметок");
        }
        for (Note n : list) {
            if (n == null) {
                throw new IOException("Список содержит пустую запись");
            }
            if (n.getTitle() == null) n.setTitle("");
            if (n.getBody() == null) n.setBody("");
            if (n.getDirector() == null) n.setDirector("");
            if (n.getCast() == null) n.setCast("");
            n.setRating(Math.max(0, Math.min(10, n.getRating())));
            if (n.getStatus() == null || !Note.STATUSES.contains(n.getStatus())) {
                n.setStatus(Note.WANT);
            }
        }
        return list;
    }
}
