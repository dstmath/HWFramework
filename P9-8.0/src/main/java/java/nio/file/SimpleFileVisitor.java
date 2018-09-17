package java.nio.file;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class SimpleFileVisitor<T> implements FileVisitor<T> {
    protected SimpleFileVisitor() {
    }

    public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(attrs);
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(attrs);
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult visitFileFailed(T file, IOException exc) throws IOException {
        Objects.requireNonNull(file);
        throw exc;
    }

    public FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException {
        Objects.requireNonNull(dir);
        if (exc == null) {
            return FileVisitResult.CONTINUE;
        }
        throw exc;
    }
}
