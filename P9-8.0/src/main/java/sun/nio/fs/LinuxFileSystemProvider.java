package sun.nio.fs;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileTypeDetector;

public class LinuxFileSystemProvider extends UnixFileSystemProvider {
    LinuxFileSystem newFileSystem(String dir) {
        return new LinuxFileSystem(this, dir);
    }

    LinuxFileStore getFileStore(UnixPath path) throws IOException {
        throw new SecurityException("getFileStore");
    }

    public <V extends FileAttributeView> V getFileAttributeView(Path obj, Class<V> type, LinkOption... options) {
        return super.getFileAttributeView(obj, (Class) type, options);
    }

    public DynamicFileAttributeView getFileAttributeView(Path obj, String name, LinkOption... options) {
        return super.getFileAttributeView(obj, name, options);
    }

    public <A extends BasicFileAttributes> A readAttributes(Path file, Class<A> type, LinkOption... options) throws IOException {
        return super.readAttributes(file, type, options);
    }

    FileTypeDetector getFileTypeDetector() {
        return new MimeTypesFileTypeDetector();
    }
}
