package java.nio.file.spi;

import java.io.IOException;
import java.nio.file.Path;

public abstract class FileTypeDetector {
    public abstract String probeContentType(Path path) throws IOException;

    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("fileTypeDetector"));
        }
        return null;
    }

    private FileTypeDetector(Void ignore) {
    }

    protected FileTypeDetector() {
        this(checkPermission());
    }
}
