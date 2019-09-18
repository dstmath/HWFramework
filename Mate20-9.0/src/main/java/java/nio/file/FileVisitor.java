package java.nio.file;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;

public interface FileVisitor<T> {
    FileVisitResult postVisitDirectory(T t, IOException iOException) throws IOException;

    FileVisitResult preVisitDirectory(T t, BasicFileAttributes basicFileAttributes) throws IOException;

    FileVisitResult visitFile(T t, BasicFileAttributes basicFileAttributes) throws IOException;

    FileVisitResult visitFileFailed(T t, IOException iOException) throws IOException;
}
