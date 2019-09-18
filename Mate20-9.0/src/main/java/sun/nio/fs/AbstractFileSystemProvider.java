package sun.nio.fs;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;

abstract class AbstractFileSystemProvider extends FileSystemProvider {
    /* access modifiers changed from: package-private */
    public abstract DynamicFileAttributeView getFileAttributeView(Path path, String str, LinkOption... linkOptionArr);

    /* access modifiers changed from: package-private */
    public abstract boolean implDelete(Path path, boolean z) throws IOException;

    protected AbstractFileSystemProvider() {
    }

    private static String[] split(String attribute) {
        String[] s = new String[2];
        int pos = attribute.indexOf(58);
        if (pos == -1) {
            s[0] = "basic";
            s[1] = attribute;
        } else {
            int pos2 = pos + 1;
            s[0] = attribute.substring(0, pos);
            s[1] = pos2 == attribute.length() ? "" : attribute.substring(pos2);
            int i = pos2;
        }
        return s;
    }

    public final void setAttribute(Path file, String attribute, Object value, LinkOption... options) throws IOException {
        String[] s = split(attribute);
        if (s[0].length() != 0) {
            DynamicFileAttributeView view = getFileAttributeView(file, s[0], options);
            if (view != null) {
                view.setAttribute(s[1], value);
                return;
            }
            throw new UnsupportedOperationException("View '" + s[0] + "' not available");
        }
        throw new IllegalArgumentException(attribute);
    }

    public final Map<String, Object> readAttributes(Path file, String attributes, LinkOption... options) throws IOException {
        String[] s = split(attributes);
        if (s[0].length() != 0) {
            DynamicFileAttributeView view = getFileAttributeView(file, s[0], options);
            if (view != null) {
                return view.readAttributes(s[1].split(","));
            }
            throw new UnsupportedOperationException("View '" + s[0] + "' not available");
        }
        throw new IllegalArgumentException(attributes);
    }

    public final void delete(Path file) throws IOException {
        implDelete(file, true);
    }

    public final boolean deleteIfExists(Path file) throws IOException {
        return implDelete(file, false);
    }
}
