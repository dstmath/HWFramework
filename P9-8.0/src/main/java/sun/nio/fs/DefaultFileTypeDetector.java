package sun.nio.fs;

import java.nio.file.FileSystems;
import java.nio.file.spi.FileTypeDetector;

public class DefaultFileTypeDetector {
    private DefaultFileTypeDetector() {
    }

    public static FileTypeDetector create() {
        return ((UnixFileSystemProvider) FileSystems.getDefault().provider()).getFileTypeDetector();
    }
}
