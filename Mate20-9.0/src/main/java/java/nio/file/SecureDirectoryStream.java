package java.nio.file;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Set;

public interface SecureDirectoryStream<T> extends DirectoryStream<T> {
    void deleteDirectory(T t) throws IOException;

    void deleteFile(T t) throws IOException;

    <V extends FileAttributeView> V getFileAttributeView(Class<V> cls);

    <V extends FileAttributeView> V getFileAttributeView(T t, Class<V> cls, LinkOption... linkOptionArr);

    void move(T t, SecureDirectoryStream<T> secureDirectoryStream, T t2) throws IOException;

    SeekableByteChannel newByteChannel(T t, Set<? extends OpenOption> set, FileAttribute<?>... fileAttributeArr) throws IOException;

    SecureDirectoryStream<T> newDirectoryStream(T t, LinkOption... linkOptionArr) throws IOException;
}
