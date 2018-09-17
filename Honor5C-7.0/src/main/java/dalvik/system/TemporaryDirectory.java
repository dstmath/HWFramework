package dalvik.system;

import java.io.File;

public class TemporaryDirectory {
    public static void setUpDirectory(String baseDir) {
    }

    public static synchronized void setUpDirectory(File baseDir) {
        synchronized (TemporaryDirectory.class) {
        }
    }
}
