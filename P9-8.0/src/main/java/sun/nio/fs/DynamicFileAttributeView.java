package sun.nio.fs;

import java.io.IOException;
import java.util.Map;

interface DynamicFileAttributeView {
    Map<String, Object> readAttributes(String[] strArr) throws IOException;

    void setAttribute(String str, Object obj) throws IOException;
}
